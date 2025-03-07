package net.mslivo.core.engine.ui_engine.rendering.shader;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.util.List;

public class ShaderCommon {

    protected String vertexShaderSource;
    protected String fragmentShaderSource;
    protected boolean hslEnabled;
    protected boolean colorEnabled;

    protected static final String HSL_FUNCTIONS = """
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

    public ShaderCommon() {
    }

    public String fragmentShaderSource() {
        return fragmentShaderSource;
    }

    public String vertexShaderSource() {
        return vertexShaderSource;
    }

    public boolean colorEnabled() {
        return colorEnabled;
    }

    public boolean hslEnabled() {
        return hslEnabled;
    }

    protected record ParseShaderResult(String vertexDeclarations, String vertexMain, String fragmentDeclarations,
                                       String fragmentMain, boolean colorEnabled, boolean hslEnabled) {

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

    public ShaderProgram compile(){
        ShaderProgram shaderProgram = new ShaderProgram(vertexShaderSource, fragmentShaderSource);
        if(!shaderProgram.isCompiled())
            throw new RuntimeException(shaderProgram.getLog());
        return shaderProgram;
    }

    protected ParseShaderResult parseShader(String shaderSource) {

        StringBuilder[] builders = new StringBuilder[4];
        for (int i = 0; i < builders.length; i++) builders[i] = new StringBuilder();

        BUILDER_INDEX builderIndex = BUILDER_INDEX.NONE;
        this.hslEnabled = false;
        this.colorEnabled = false;
        int openBrackets = 0;
        List<String> lines = shaderSource.lines().toList();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.isBlank())
                continue;
            String lineCleaned = line.trim().replace(" ", "");

            if (lineCleaned.startsWith("#") && lineCleaned.contains("HSL_ENABLED") && lineCleaned.contains("COLOR_ENABLED")) {
                this.hslEnabled = lineCleaned.contains("HSL_ENABLEDtrue");
                this.colorEnabled = lineCleaned.contains("COLOR_ENABLEDtrue");
                continue;
            }
            if (lineCleaned.equals("#VERTEX")) {
                builderIndex = BUILDER_INDEX.VERTEX_DECLARATIONS;
                continue;
            }
            if (builderIndex == BUILDER_INDEX.VERTEX_DECLARATIONS && lineCleaned.equals("voidmain(){")) {
                builderIndex = BUILDER_INDEX.VERTEX_MAIN;
                continue;
            }
            if (builderIndex == BUILDER_INDEX.VERTEX_MAIN && lineCleaned.equals("}")) {
                builderIndex = BUILDER_INDEX.VERTEX_DECLARATIONS;
                continue;
            }
            if (lineCleaned.equals("#FRAGMENT")) {
                builderIndex = BUILDER_INDEX.FRAGMENT_DECLARATIONS;
                continue;
            }
            if (builderIndex == BUILDER_INDEX.FRAGMENT_DECLARATIONS && lineCleaned.equals("voidmain(){")) {
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
                builders[BUILDER_INDEX.FRAGMENT_DECLARATIONS.index].toString(), builders[BUILDER_INDEX.FRAGMENT_MAIN.index].toString(), colorEnabled, hslEnabled
        );

    }

}
