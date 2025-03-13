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

    private static final HashMap<String, String> functions = new HashMap<>();
    static {
        functions.put("colorModAdd", """
                    vec4 colorModAdd(vec4 color, vec4 modColor){
                        color.rgb = clamp(color.rgb+(modColor.rgb-0.5),0.0,1.0);
                        color.a *= modColor.a;
                        return color;
                    }
                """);
        functions.put("colorModMul", """
                    vec4 colorModMul(vec4 color, vec4 modColor){
                        color.rgb = clamp(color.rgb*((modColor.rgb-0.5)*2.0),0.0,1.0);
                        color.a *= modColor.a;
                        return color;
                    }
                """);
        functions.put("rgb2hsl", """
                vec4 rgb2hsl(vec4 c)
                {
                    const vec4 J = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
                    vec4 p = mix(vec4(c.bg, J.wz), vec4(c.gb, J.xy), step(c.b, c.g));
                    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
                    float d = q.x - min(q.w, q.y);
                    float l = q.x * (1.0 - 0.5 * d / (q.x + eps));
                    return vec4(abs(q.z + (q.w - q.y) / (6.0 * d + eps)), (q.x - l) / (min(l, 1.0 - l) + eps), l, c.a);
                }
                """);
        functions.put("hsl2rgb", """
                vec4 hsl2rgb(vec4 c)
                {
                    const vec4 J = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
                    vec4 p = mix(vec4(c.bg, J.wz), vec4(c.gb, J.xy), step(c.b, c.g));
                    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
                    float d = q.x - min(q.w, q.y);
                    float l = q.x * (1.0 - 0.5 * d / (q.x + eps));
                    return vec4(abs(q.z + (q.w - q.y) / (6.0 * d + eps)), (q.x - l) / (min(l, 1.0 - l) + eps), l, c.a);
                }
                """);
    }

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

    private String parseImports(String line){
        StringBuilder result = new StringBuilder();
        line = line.substring(line.indexOf(":")+1);
        if(line.isBlank())
            return result.toString();
        String[] imports = line.split(" ");
        if(imports.length < 2 || !imports[0].equals("import"))
            return result.toString();

        for(int i=1;i<imports.length;i++){
            String importFunction = imports[i].trim();
            FileHandle file = Tools.File.findResource("shaders/pixelui/functions/"+importFunction+".function.glsl");
            if(!file.exists())
                throw new RuntimeException("Shader import function "+importFunction+" does not exist");

            result.append(file.readString()).append(System.lineSeparator());
        }

        return result.toString();
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
                builders[builderIndex.index].append(parseImports(lineCleaned));
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
                builders[builderIndex.index].append(parseImports(lineCleaned));
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
