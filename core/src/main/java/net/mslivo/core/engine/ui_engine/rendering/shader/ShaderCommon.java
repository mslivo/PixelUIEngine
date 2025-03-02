package net.mslivo.core.engine.ui_engine.rendering.shader;

import com.badlogic.gdx.files.FileHandle;

import java.io.BufferedReader;
import java.io.IOException;

public class ShaderCommon {

    protected static String HSL_FUNCTIONS = """
            const HIGH float eps = 1.0e-10;
            
            vec4 rgb2hsl(vec4 c)
            {
                const vec4 J = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
                vec4 p = mix(vec4(c.bg, J.wz), vec4(c.gb, J.xy), step(c.b, c.g));
                vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
                float d = q.x - min(q.w, q.y);
                float l = q.x * (1.0 - 0.5 * d / (q.x + eps));
                return vec4(abs(q.z + (q.w - q.y) / (6.0 * d + eps)), (q.x - l) / (min(l, 1.0 - l) + eps), l, c.a);
            }
            
            vec4 hsl2rgb(vec4 c)
            {
                const vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
                vec3 p = abs(fract(c.x + K.xyz) * 6.0 - K.www);
                float v = (c.z + c.y * min(c.z, 1.0 - c.z));
                return vec4(v * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), 2.0 * (1.0 - c.z / (v + eps))), c.w);
            }
            """;


    protected record ParseShaderResult(String vertexDeclarations, String vertexMain, String fragmentDeclarations, String fragmentMain,boolean colorEnabled, boolean hslEnabled){

    }

    protected ParseShaderResult parseShader(FileHandle fileHandle){
        BufferedReader reader = fileHandle.reader(1024);
        String line;
        StringBuilder[] builders = new StringBuilder[4];
        for (int i = 0; i < builders.length; i++) builders[i] = new StringBuilder();

        int builderIndex = 0;
        boolean hslEnabled = true;
        boolean colorEnabled = true;
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isBlank())
                    continue;
                String lineNoSpace = line.replace(" ","");

                if (line.startsWith("#") && line.contains("HSL_ENABLED") && line.contains("COLOR_ENABLED")) {
                    String[] words = line.split(" ");
                    if (words.length == 4) {
                        if (words[0].equals("HSL_ENABLED")) {
                            hslEnabled = words[1].equals("true");
                        } else if (words[0].equals("COLOR_ENABLED")) {
                            colorEnabled = words[1].equals("true");
                        }
                        if (words[2].equals("HSL_ENABLED")) {
                            hslEnabled = words[3].equals("true");
                        } else if (words[2].equals("COLOR_ENABLED")) {
                            colorEnabled = words[3].equals("true");
                        }
                        continue;
                    }
                }
                if (line.equals("#VERTEX")) {
                    builderIndex = 0;
                    continue;
                }
                if (builderIndex == 0 && lineNoSpace.equals("voidmain(){")) {
                    builderIndex = 1;
                    continue;
                }
                if(builderIndex == 1 && lineNoSpace.equals("}")) {
                    builderIndex = 0;
                    continue;
                }
                if (line.equals("#FRAGMENT")) {
                    builderIndex = 2;
                    continue;
                }
                if (builderIndex == 2 && lineNoSpace.equals("voidmain(){")) {
                    builderIndex = 3;
                    continue;
                }
                if(builderIndex == 3 && lineNoSpace.equals("}")) {
                    builderIndex = 2;
                    continue;
                }

                builders[builderIndex].append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return new ParseShaderResult(
                builders[0].toString(),builders[1].toString(),builders[2].toString(),builders[3].toString(), colorEnabled,hslEnabled
        );

    }
}
