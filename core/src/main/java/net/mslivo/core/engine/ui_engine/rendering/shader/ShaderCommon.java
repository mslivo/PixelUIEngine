package net.mslivo.core.engine.ui_engine.rendering.shader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import net.mslivo.core.engine.tools.Tools;

import java.util.HashMap;
import java.util.List;

public class ShaderCommon {

    protected final static String FLOAT_DECLARATIONS = """
                   #ifdef GL_ES
                       #define LOW lowp
                       #define MED mediump
                       #define HIGH highp
                       precision mediump float;
                   #else
                       #define MED
                       #define LOW
                       #define HIGH
                   #endif
                   const HIGH float FLOAT_CORRECTION = (255.0/254.0);
                   
            """;

    protected String vertexShaderSource;
    protected String fragmentShaderSource;

    public ShaderCommon() {
    }

    public String fragmentShaderSource() {
        return fragmentShaderSource;
    }

    public String vertexShaderSource() {
        return vertexShaderSource;
    }

    protected record ParseShaderResult(String vertexDeclarations, String vertexMain, String fragmentDeclarations,
                                       String fragmentMain) {

    }

    protected enum BUILDER_INDEX {
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

    protected ParseShaderResult parseShader(String shaderSource) {

        StringBuilder[] builders = new StringBuilder[4];
        for (int i = 0; i < builders.length; i++) builders[i] = new StringBuilder();

        BUILDER_INDEX builderIndex = BUILDER_INDEX.NONE;
        int openBrackets = 0;
        List<String> lines = shaderSource.lines().toList();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.isBlank())
                continue;
            String lineCleaned = line.trim().replaceAll("\\s+"," ");

            if (lineCleaned.startsWith("VERTEX:")) {
                builderIndex = BUILDER_INDEX.VERTEX_DECLARATIONS;
                continue;
            }
            if (builderIndex == BUILDER_INDEX.VERTEX_DECLARATIONS && lineCleaned.startsWith("void main(){")) {
                builderIndex = BUILDER_INDEX.VERTEX_MAIN;
                continue;
            }
            if (builderIndex == BUILDER_INDEX.VERTEX_MAIN && lineCleaned.equals("}")) {
                builderIndex = BUILDER_INDEX.VERTEX_DECLARATIONS;
                continue;
            }
            if (lineCleaned.startsWith("FRAGMENT:")) {
                builderIndex = BUILDER_INDEX.FRAGMENT_DECLARATIONS;
                continue;
            }
            if (builderIndex == BUILDER_INDEX.FRAGMENT_DECLARATIONS && lineCleaned.startsWith("void main(){")) {
                builderIndex = BUILDER_INDEX.FRAGMENT_MAIN;
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

    public ShaderProgram compile(){
        ShaderProgram shaderProgram = new ShaderProgram(vertexShaderSource, fragmentShaderSource);
        if(!shaderProgram.isCompiled()) {
            String log = shaderProgram.getLog();
            String[] lines = log.lines().toList().toArray(new String[]{});

            String shaderSource = null;
            if(lines[0].toLowerCase().contains("vertex shader")){
                shaderSource = this.vertexShaderSource;
                System.out.println("Vertex shader:");
            }else if(lines[0].toLowerCase().contains("fragment shader")){
                shaderSource = this.fragmentShaderSource;
                System.out.println("Fragment shader:");
            }

            IntArray lineNumbers = new IntArray();
            Array lineErrors = new Array();
            for(int i=0;i<lines.length;i++){
                String[] lineSplit = lines[i].split(":");
                if(lineSplit[0].toLowerCase().equals("error") && lineSplit.length >= 5){
                    lineNumbers.add(Integer.valueOf(lineSplit[2])-1);
                    lineErrors.add(lineSplit[3]+" "+lineSplit[4]);
                }
            }


            if(shaderSource != null){
                lines = shaderSource.lines().toList().toArray(new String[]{});
                for(int i=0;i<lines.length;i++){
                    String line = (i+1)+":"+lines[i];

                    int errorIndex = lineNumbers.indexOf(i);
                    if(errorIndex != -1){
                        line += Tools.Text.Colors.RED+" !!! ERROR !!! -> "+lineErrors.get(errorIndex)+Tools.Text.Colors.RESET;

                    }

                    System.out.println(line);
                }

            }


            throw new RuntimeException(shaderProgram.getLog());
        }
        return shaderProgram;
    }


}
