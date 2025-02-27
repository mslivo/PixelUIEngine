package net.mslivo.core.engine.ui_engine.rendering.shader;

import net.mslivo.core.engine.tools.Tools;

public final class SpriteShader extends ShaderCommon{

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
            
                    $VERTEX_DECLARATIONS
            
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
                       $VERTEX_MAIN
            
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
            
            """+HSL_FUNCTIONS+"""
            
            $FRAGMENT_DECLARATIONS
            
            void main() {
                // Get texCoords
                HIGH vec2 texCoords = v_texCoords;
            
                // Modify texCoords
                $FRAGMENT_MAIN_TEXCOORDS
            
                // Get Color & Apply Mult.
                vec4 fragColor = texture2D( u_texture, texCoords);
                fragColor.rgb = clamp(fragColor.rgb*(1.0+((v_color.rgb-0.5)*2.0)),0.0,1.0);
                
                // HSL Tweaks
                vec4 hsl = rgb2hsl(fragColor);
                hsl.x = fract(hsl.x + ((v_tweak.x-0.5)*2.0));
                hsl.y = max(hsl.y + ((v_tweak.y-0.5)*2.0),0.0);
                hsl.z = clamp(hsl.z + ((v_tweak.z-0.5)*2.0),0.0,1.0);
                fragColor = hsl2rgb(hsl);
                
                // Custom Code
                $FRAGMENT_MAIN_FRAGCOLOR
            
                // Apply Alpha and Finish
                fragColor.a *= v_color.a;
                gl_FragColor = fragColor;
            }
            """;

    public final String vertexShaderSource;
    public final String fragmentShaderSource;

    public SpriteShader(String vertexDeclarations, String vertexMain, String fragmentDeclarations, String fragmentMainTexCoords, String fragmentMainFragColor) {

        this.vertexShaderSource = VERTEX_SHADER_TEMPLATE
                .replace("$VERTEX_DECLARATIONS", Tools.Text.validString(vertexDeclarations))
                .replace("$VERTEX_MAIN", Tools.Text.validString(vertexMain));

        this.fragmentShaderSource = FRAGMENT_SHADER_TEMPLATE
                .replace("$FRAGMENT_DECLARATIONS", Tools.Text.validString(fragmentDeclarations))
                .replace("$FRAGMENT_MAIN_TEXCOORDS", Tools.Text.validString(fragmentMainTexCoords))
                .replace("$FRAGMENT_MAIN_FRAGCOLOR", Tools.Text.validString(fragmentMainFragColor));

    }
}
