package net.mslivo.core.engine.ui_engine.rendering.shader;

import net.mslivo.core.engine.tools.Tools;

public final class PrimitiveShader extends ShaderCommon{
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
            attribute vec4 a_vertexColor;
            attribute vec4 a_tweak;
            uniform mat4 u_projTrans;
            varying vec4 fragColorVar;
            const HIGH float float_correction = 0.0019607842; // float precision correction
         
            """+HSL_FUNCTIONS+"""
            
            $VERTEX_DECLARATIONS
         
            void main() {
                // Get Attributes
                vec4 v_color = a_color;
                v_color.rgb = min(v_color.rgb+float_correction,1.0); // RGB 0.5 float precision correction
                v_color.a = v_color.a * (255.0/254.0);
                vec4 v_tweak = a_tweak;
                v_tweak.xyz = min(v_tweak.xyz+float_correction,1.0);
                
                // Get Color & Apply Mult.
                vec4 vertexColor = a_vertexColor;
                vertexColor.rgb = clamp(vertexColor.rgb*(1.0+((v_color.rgb-0.5)*2.0)),0.0,1.0);
                    
                // HSL Tweaks
                vec4 hsl = rgb2hsl(vertexColor);
                hsl.x = fract(hsl.x + ((v_tweak.x-0.5)*2.0));
                hsl.y = max(hsl.y + ((v_tweak.y-0.5)*2.0),0.0);
                hsl.z = clamp(hsl.z + ((v_tweak.z-0.5)*2.0),0.0,1.0);
                vertexColor = hsl2rgb(hsl);
                
                // Custom Code
                $VERTEX_MAIN_VERTEXCOLOR
              
                // Apply Alpha & Finish
                vertexColor.a *= v_color.a;
                gl_PointSize = 1.0;
                gl_Position = u_projTrans * a_position;
                fragColorVar = vertexColor;
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
            
                varying vec4 fragColorVar;
                
                $FRAGMENT_DECLARATIONS
            
                void main() {
                   vec4 fragColor = fragColorVar;
                   
                   $FRAGMENT_MAIN_FRAGCOLOR
                   
                   gl_FragColor = fragColor;
                }
            """;


    public final String vertexShaderSource;
    public final String fragmentShaderSource;

    public PrimitiveShader(String vertexDeclarations, String vertexMainVertexColor, String fragmentDeclarations, String fragmentMainFragColor) {

        this.vertexShaderSource = VERTEX_SHADER_TEMPLATE
                .replace("$VERTEX_DECLARATIONS", Tools.Text.validString(vertexDeclarations))
                .replace("$VERTEX_MAIN_VERTEXCOLOR", Tools.Text.validString(vertexMainVertexColor));

        this.fragmentShaderSource = FRAGMENT_SHADER_TEMPLATE
                .replace("$FRAGMENT_DECLARATIONS", Tools.Text.validString(fragmentDeclarations))
                .replace("$FRAGMENT_MAIN_FRAGCOLOR", Tools.Text.validString(fragmentMainFragColor));

    }

}
