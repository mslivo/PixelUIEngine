package net.mslivo.pixelui.rendering;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import net.mslivo.pixelui.utils.Tools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ShaderParser {

    private static final SHADER_TEMPLATE[] SHADER_TEMPLATE_VALUES = SHADER_TEMPLATE.values();

    public static ShaderProgram parse(Path shaderFile) {
        try {
            final String fileName = shaderFile.getFileName().toString();
            return parse(fileName, findTemplate(fileName), Files.readString(shaderFile));
        } catch (Exception e) {
            Tools.App.handleException(e);
        }
        return null;
    }

    public static ShaderProgram parse(FileHandle fileHandle) {
        try {
            final String fileName = fileHandle.name();
            return parse(fileName, findTemplate(fileName), fileHandle.readString());
        } catch (Exception e) {
            Tools.App.handleException(e);
        }
        return null;
    }

    public static ShaderProgram parse(SHADER_TEMPLATE template, String source) {
        return parse(null, template, source);
    }

    private static ShaderProgram parse(String fileName, SHADER_TEMPLATE template, String source) {

        final ParseShaderResult parseResult = parseShaderSource(source);
        final String vertexShader = createVertexShader(template, parseResult.vertexDeclarations, parseResult.vertexMain);
        final String fragmentShader = createFragmentShader(template, parseResult.fragmentDeclarations, parseResult.fragmentMain);

        return compileShader(fileName, vertexShader, fragmentShader);
    }

    private static SHADER_TEMPLATE findTemplate(String fileName) {
        for (int i = 0; i < SHADER_TEMPLATE_VALUES.length; i++) {
            if (fileName.toLowerCase().endsWith(SHADER_TEMPLATE_VALUES[i].extension)) {
                return SHADER_TEMPLATE_VALUES[i];
            }
        }
        throw new RuntimeException("No template found for extension \"" + fileName + "\"");
    }

    private static String createVertexShader(SHADER_TEMPLATE template, String vertexDeclarations, String vertexMain) {
        return template.vertexTemplate
                .replace("#VERTEX_DECLARATIONS", Tools.Text.validString(vertexDeclarations))
                .replace("#VERTEX_MAIN", Tools.Text.validString(vertexMain));
    }

    private static String createFragmentShader(SHADER_TEMPLATE template, String fragmentDeclarations, String fragmentMain) {
        return template.fragmentTemplate
                .replace("#FRAGMENT_DECLARATIONS", Tools.Text.validString(fragmentDeclarations))
                .replace("#FRAGMENT_MAIN", Tools.Text.validString(fragmentMain));
    }

    private static ShaderProgram compileShader(String fileName, String vertexShaderSource, String fragmentShaderSource) {
        ShaderProgram result = new ShaderProgram(vertexShaderSource, fragmentShaderSource);
        Array<String> errorLines = new Array<>();
        String shader = null;
        if (!result.isCompiled()) {
            String log = result.getLog();
            String[] lines = log.lines().toList().toArray(new String[]{});

            String shaderSource = null;
            if (lines[0].toLowerCase().contains("vertex shader")) {
                shaderSource = vertexShaderSource;
                shader = "vertex-shader";
            } else if (lines[0].toLowerCase().contains("fragment shader")) {
                shaderSource = fragmentShaderSource;
                shader = "fragment shader";
            }

            IntArray lineNumbers = new IntArray();
            Array lineErrors = new Array();
            for (int i = 0; i < lines.length; i++) {
                String[] lineSplit = lines[i].split(":");
                if (lineSplit[0].toLowerCase().equals("error") && lineSplit.length >= 5) {
                    lineNumbers.add(Integer.valueOf(lineSplit[2]) - 1);
                    lineErrors.add(lineSplit[3] + " " + lineSplit[4]);
                }
            }

            if (shaderSource != null) {
                lines = shaderSource.lines().toList().toArray(new String[]{});
                for (int i = 0; i < lines.length; i++) {
                    String line = (i + 1) + ":" + lines[i];

                    int errorIndex = lineNumbers.indexOf(i);
                    if (errorIndex != -1) {
                        errorLines.add(line);
                        line += lineErrors.get(errorIndex);
                        String error = (fileName != null ? "Error in " + shader + " \"" + fileName + "\"" : "Error in " + shader);
                        throw new RuntimeException(error + ": " + line);
                    }


                }

            }

        }
        return result;
    }


    private static ParseShaderResult parseShaderSource(String shaderSource) {

        StringBuilder[] builders = new StringBuilder[4];
        for (int i = 0; i < builders.length; i++) builders[i] = new StringBuilder();

        BUILDER_INDEX builderIndex = BUILDER_INDEX.NONE;
        int openBrackets = 0;
        List<String> lines = shaderSource.lines().toList();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            String lineTrimmed = line.trim();
            String lineCleaned = lineTrimmed.replace(" ", "");

            if (lineTrimmed.startsWith("// BEGIN VERTEX")) {
                builderIndex = BUILDER_INDEX.VERTEX_DECLARATIONS;
                continue;
            }
            if (builderIndex == BUILDER_INDEX.VERTEX_DECLARATIONS && lineCleaned.startsWith("voidmain(){")) {
                builderIndex = BUILDER_INDEX.VERTEX_MAIN;
                continue;
            }
            if (builderIndex == BUILDER_INDEX.VERTEX_MAIN && lineCleaned.equals("}")) {
                builderIndex = BUILDER_INDEX.VERTEX_DECLARATIONS;
                continue;
            }
            if (builderIndex == BUILDER_INDEX.VERTEX_DECLARATIONS && lineTrimmed.equals("// END VERTEX")) {
                builderIndex = BUILDER_INDEX.NONE;
                continue;
            }
            if (lineTrimmed.startsWith("// BEGIN FRAGMENT")) {
                builderIndex = BUILDER_INDEX.FRAGMENT_DECLARATIONS;
                continue;
            }
            if (builderIndex == BUILDER_INDEX.FRAGMENT_DECLARATIONS && lineCleaned.startsWith("voidmain(){")) {
                builderIndex = BUILDER_INDEX.FRAGMENT_MAIN;
                continue;
            }
            if (builderIndex == BUILDER_INDEX.FRAGMENT_DECLARATIONS && lineTrimmed.equals("// END FRAGMENT")) {
                builderIndex = BUILDER_INDEX.NONE;
                continue;
            }
            if (builderIndex == BUILDER_INDEX.FRAGMENT_MAIN) {
                if (!lineCleaned.startsWith("//")) {
                    if (lineCleaned.endsWith("{")) {
                        openBrackets++;
                    }
                    if (lineCleaned.startsWith("}")) {
                        if (openBrackets > 0) {
                            openBrackets--;
                        } else {
                            builderIndex = BUILDER_INDEX.FRAGMENT_DECLARATIONS;
                            continue;
                        }
                    }
                }
            }

            if (builderIndex != BUILDER_INDEX.NONE)
                builders[builderIndex.index].append(line).append(System.lineSeparator());
        }


        return new ParseShaderResult(
                builders[BUILDER_INDEX.VERTEX_DECLARATIONS.index].toString(), builders[BUILDER_INDEX.VERTEX_MAIN.index].toString(),
                builders[BUILDER_INDEX.FRAGMENT_DECLARATIONS.index].toString(), builders[BUILDER_INDEX.FRAGMENT_MAIN.index].toString()
        );

    }

    private record ParseShaderResult(String vertexDeclarations, String vertexMain, String fragmentDeclarations,
                                     String fragmentMain) {
    }


    private enum BUILDER_INDEX {
        NONE(-1),
        VERTEX_DECLARATIONS(0),
        VERTEX_MAIN(1),
        FRAGMENT_DECLARATIONS(2),
        FRAGMENT_MAIN(3);

        public final int index;

        BUILDER_INDEX(int index) {
            this.index = index;
        }
    }

    public enum SHADER_TEMPLATE {
        SPRITE(".sprite.glsl",
                """
                        
                                attribute vec4 a_position;
                                attribute vec4 a_color;
                                attribute vec4 a_tweak;
                                attribute vec2 a_texCoord;
                        
                                varying vec4 v_color;
                                varying vec4 v_tweak;
                                varying vec2 v_texCoord;
                        
                                uniform mat4 u_projTrans;
                        
                                #VERTEX_DECLARATIONS
                        
                                void main()
                                {
                                   // Get Attributes
                                   v_color = (a_color * FLOAT_CORRECTION);
                                   v_tweak = (a_tweak * FLOAT_CORRECTION);
                                   v_texCoord = a_texCoord;
                                   gl_Position = u_projTrans * a_position;
                        
                                   // Custom Code
                                   #VERTEX_MAIN
                                }
                        """,
                """
                        varying vec4 v_color;
                        varying vec4 v_tweak;
                        varying vec2 v_texCoord;
                        
                        uniform sampler2D u_texture;
                        uniform vec2 u_textureSize;
                        
                        #FRAGMENT_DECLARATIONS
                        
                        void main() {
                        
                            // Custom Code
                            #FRAGMENT_MAIN
                        
                        }
                        """),
        PRIMITIVE(".primitive.glsl", """
                
                        attribute vec4 a_position;
                        attribute vec4 a_color;
                        attribute vec4 a_tweak;
                        attribute vec4 a_vertexColor;
                
                        varying vec4 v_color;
                        varying vec4 v_tweak;
                        varying vec4 v_vertexColor;
                
                        uniform mat4 u_projTrans;
                
                        #VERTEX_DECLARATIONS
                
                        void main() {
                            gl_PointSize = 1.0;
                
                            // Get Attributes
                            v_color = (a_color*FLOAT_CORRECTION);
                            v_tweak = (a_tweak*FLOAT_CORRECTION);
                            v_vertexColor = (a_vertexColor*FLOAT_CORRECTION);
                
                            gl_Position = u_projTrans * a_position;
                
                            // Custom Code
                            #VERTEX_MAIN
                        }
                """,
                """
                        
                            varying vec4 v_color;
                            varying vec4 v_tweak;
                            varying vec4 v_vertexColor;
                        
                            #FRAGMENT_DECLARATIONS
                        
                            void main() {
                        
                               #FRAGMENT_MAIN
                        
                            }
                        """),
        SIMPLE_PRIMITIVE(".primitive.glsl", """
                
                        attribute vec4 a_position;
                        attribute vec4 a_vertexColor;
                
                        varying vec4 v_vertexColor;
                
                        uniform mat4 u_projTrans;
                
                        #VERTEX_DECLARATIONS
                
                        void main() {
                            gl_PointSize = 1.0;
                
                            // Get Attributes
                            v_vertexColor = (a_vertexColor*FLOAT_CORRECTION);
                
                            gl_Position = u_projTrans * a_position;
                
                            // Custom Code
                            #VERTEX_MAIN
                        }
                """,
                """
                        
                            varying vec4 v_vertexColor;
                        
                            #FRAGMENT_DECLARATIONS
                        
                            void main() {
                        
                               #FRAGMENT_MAIN
                        
                            }
                        """),


        ;

        private static final String COMMON_DECLARATIONS = """             
                        #ifdef GL_ES
                            precision highp float;
                            precision highp int;
                        #endif
                
                       const float FLOAT_CORRECTION = (255.0/254.0);
                """;

        public final String vertexTemplate, fragmentTemplate;
        public final String extension;

        SHADER_TEMPLATE(String extension, String vertexTemplate, String fragmentTemplate) {
            this.extension = extension;
            this.vertexTemplate = COMMON_DECLARATIONS + vertexTemplate;
            this.fragmentTemplate = COMMON_DECLARATIONS + fragmentTemplate;
        }
    }
}
