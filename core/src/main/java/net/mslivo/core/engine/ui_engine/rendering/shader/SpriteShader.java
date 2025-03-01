package net.mslivo.core.engine.ui_engine.rendering.shader;

import com.badlogic.gdx.files.FileHandle;
import net.mslivo.core.engine.tools.Tools;

import java.io.BufferedReader;
import java.io.IOException;

public final class SpriteShader extends ShaderCommon {

    private static final String HSL_FRAGMENT_CODE = """
                vec4 hsl = rgb2hsl(fragColor);
                hsl.x = fract(hsl.x + ((v_tweak.x-0.5)*2.0));
                hsl.y = max(hsl.y + ((v_tweak.y-0.5)*2.0),0.0);
                hsl.z = clamp(hsl.z + ((v_tweak.z-0.5)*2.0),0.0,1.0);
                fragColor = hsl2rgb(hsl);
            """;

    private static final String COLOR_FRAGMENT_CODE = """
                fragColor.rgb = clamp(fragColor.rgb*(1.0+((v_color.rgb-0.5)*2.0)),0.0,1.0);
                fragColor.a *= v_color.a;
            """;

    private static final String VERTEX_SHADER_TEMPLATE = """
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
            
                    attribute vec4 a_position;
                    attribute vec4 a_color;
                    attribute vec2 a_texCoord;
                    attribute vec4 a_tweak;
                    uniform mat4 u_projTrans;
                    varying vec4 v_color;
                    varying vec4 v_tweak;
                    varying vec2 v_texCoords;
                    const HIGH float float_correction = 0.0019607842;
            
                    #VERTEX_DECLARATIONS
            
                    void main()
                    {
                       // Get Attributes
                       v_color = a_color;
                       v_color.rgb = min(v_color.rgb+float_correction,1.0); // RGB 0.5 float precision correction
                       v_color.a = v_color.a * (255.0/254.0);
                       v_tweak = a_tweak;
                       v_texCoords = a_texCoord;
                       v_tweak.xyz = min(v_tweak.xyz+float_correction,1.0); // HSL 0.5 float precision correction
            
                       // Custom Code
                       #VERTEX_MAIN
            
                       gl_Position = u_projTrans * a_position;
                    }
            """;

    private static final String FRAGMENT_SHADER_TEMPLATE = """
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
            
            varying vec4 v_color;
            varying vec4 v_tweak;
            varying vec2 v_texCoords;
            
            uniform sampler2D u_texture;
            uniform vec2 u_textureSize;
            
            #HSL_FUNCTIONS
            
            #FRAGMENT_DECLARATIONS
            
            void main() {
                // Get texCoords
                HIGH vec2 texCoords = v_texCoords;
            
                // Modify texCoords
                #FRAGMENT_MAIN_TEXCOORDS
            
                // Get Fragment
                vec4 fragColor = texture2D( u_texture, texCoords);
            
                // Custom Code
            
                #FRAGMENT_MAIN_FRAGCOLOR
            
                // Color Mult
                #FRAGMENT_COLOR
            
                // HSL Tweaks
                #FRAGMENT_HSL
            
                // Done
            
                gl_FragColor = fragColor;
            }
            """;

    public final String vertexShaderSource;
    public final String fragmentShaderSource;

    public SpriteShader(FileHandle fromFile) {
        BufferedReader reader = fromFile.reader(1024);
        String line;
        StringBuilder[] builders = new StringBuilder[5];
        for (int i = 0; i < builders.length; i++) builders[i] = new StringBuilder();

        int builderIndex = 0;
        boolean hslEnabled = true;
        boolean colorEnabled = true;
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isBlank())
                    continue;

                if (line.equals("#VERTEX_DECLARATIONS")) {
                    builderIndex = 0;
                } else if (line.equals("#VERTEX_MAIN")) {
                    builderIndex = 1;
                } else if (line.equals("#FRAGMENT_DECLARATIONS")) {
                    builderIndex = 2;
                } else if (line.equals("#FRAGMENT_MAIN_TEXCOORDS")) {
                    builderIndex = 3;
                } else if (line.equals("#FRAGMENT_MAIN_FRAGCOLOR")) {
                    builderIndex = 4;
                } else if (line.startsWith("#") && line.contains("HSL_ENABLED") && line.contains("COLOR_ENABLED")) {
                    String[] words = line.split(" ");
                    if(words.length == 4) {
                        hslEnabled = words[1].equals("true");
                        colorEnabled = words[3].equals("true");
                    }
                } else {
                    builders[builderIndex].append(line).append(System.lineSeparator());
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.vertexShaderSource = createVertexShader(builders[0].toString(),builders[1].toString());

        this.fragmentShaderSource = createFragmentShader(builders[2].toString(),builders[3].toString(),builders[4].toString(), hslEnabled, colorEnabled);
    }

    public SpriteShader(String vertexDeclarations, String vertexMain, String fragmentDeclarations, String fragmentMainTexCoords, String fragmentMainFragColor, boolean hslEnabled, boolean colorEnabled) {
        this.vertexShaderSource = createVertexShader(vertexDeclarations, vertexMain);
        this.fragmentShaderSource = createFragmentShader(fragmentDeclarations, fragmentMainTexCoords, fragmentMainFragColor, hslEnabled, hslEnabled);
    }

    private String createVertexShader(String vertexDeclarations, String vertexMain) {
        return VERTEX_SHADER_TEMPLATE
                .replace("#VERTEX_DECLARATIONS", Tools.Text.validString(vertexDeclarations))
                .replace("#VERTEX_MAIN", Tools.Text.validString(vertexMain));
    }

    private String createFragmentShader(String fragmentDeclarations, String fragmentMainTexCoords, String fragmentMainFragColor, boolean hslEnabled, boolean colorEnabled) {
        return FRAGMENT_SHADER_TEMPLATE
                .replace("#FRAGMENT_DECLARATIONS", Tools.Text.validString(fragmentDeclarations))
                .replace("#FRAGMENT_MAIN_TEXCOORDS", Tools.Text.validString(fragmentMainTexCoords))
                .replace("#FRAGMENT_MAIN_FRAGCOLOR", Tools.Text.validString(fragmentMainFragColor))
                .replace("#FRAGMENT_COLOR", colorEnabled ? COLOR_FRAGMENT_CODE : "")
                .replace("#HSL_FUNCTIONS", hslEnabled ? HSL_FUNCTIONS : "")
                .replace("#FRAGMENT_HSL", hslEnabled ? HSL_FRAGMENT_CODE : "");
    }
}
