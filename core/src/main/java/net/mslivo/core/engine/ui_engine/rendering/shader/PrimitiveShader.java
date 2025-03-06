package net.mslivo.core.engine.ui_engine.rendering.shader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import net.mslivo.core.engine.tools.Tools;

public final class PrimitiveShader extends ShaderCommon {

    private static final String VERTEX_HSL_CODE = """
            v_tweak.xyz = min(v_tweak.xyz+FLOAT_CORRECTION,1.0);
            
            vec4 hsl = rgb2hsl(vertexColor);
                hsl.x = fract(hsl.x + ((v_tweak.x-0.5)*2.0));
                hsl.y = max(hsl.y + ((v_tweak.y-0.5)*2.0),0.0);
                hsl.z = clamp(hsl.z + ((v_tweak.z-0.5)*2.0),0.0,1.0);
                vertexColor = hsl2rgb(hsl);
            """;

    private static final String VERTEX_COLOR_CODE = """
            v_color.rgb = min(v_color.rgb+FLOAT_CORRECTION,1.0); // RGB 0.5 float precision correction
            v_color.a = v_color.a * (255.0/254.0);
            
            vertexColor.rgb = clamp(vertexColor.rgb*(1.0+((v_color.rgb-0.5)*2.0)),0.0,1.0);
            vertexColor.a *= v_color.a;
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
                    attribute vec4 a_vertexColor;
                    attribute vec4 a_tweak;
                    uniform mat4 u_projTrans;
                    varying vec4 v_fragColor;
                    const HIGH float FLOAT_CORRECTION = 0.0019607842;
            
                    #HSL_FUNCTIONS
            
                    #VERTEX_DECLARATIONS
            
                    void main() {
                        // Get Attributes
                        vec4 v_color = a_color;
                        vec4 v_tweak = a_tweak;
                        vec4 vertexColor = a_vertexColor;
            
                        // Custom Code
                        #VERTEX_MAIN
            
                        // Color Mult
                        #VERTEX_COLOR_CODE
            
                        // HSL Tweaks
                        #VERTEX_HSL_CODE
            
                        // Done
                        gl_PointSize = 1.0;
                        gl_Position = u_projTrans * a_position;
                        v_fragColor = vertexColor;
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
            
                varying vec4 v_fragColor;
            
                #FRAGMENT_DECLARATIONS
            
                void main() {
            
                   #FRAGMENT_MAIN
            
                   gl_FragColor = fragColor;
                }
            """;

    public PrimitiveShader(FileHandle shaderFile) {
        this(shaderFile.readString());
    }

    public ShaderProgram compileShader(){
        ShaderProgram shaderProgram = new ShaderProgram(vertexShaderSource(), fragmentShaderSource());
        if(!shaderProgram.isCompiled())
            throw new RuntimeException(shaderProgram.getLog());
        return shaderProgram;
    }

    public PrimitiveShader(String shader) {
        super();
        ParseShaderResult parseShaderResult = parseShader(shader);
        this.vertexShaderSource = createVertexShader(parseShaderResult.vertexDeclarations(), parseShaderResult.vertexMain(), parseShaderResult.colorEnabled(), parseShaderResult.hslEnabled());
        this.fragmentShaderSource = createFragmentShader(parseShaderResult.fragmentDeclarations(), parseShaderResult.fragmentMain());
    }

    private String createVertexShader(String vertexDeclarations, String vertexMainVertexColor, boolean colorEnabled, boolean hslEnabled) {
        return VERTEX_SHADER_TEMPLATE
                .replace("#VERTEX_DECLARATIONS", Tools.Text.validString(vertexDeclarations))
                .replace("#VERTEX_MAIN", Tools.Text.validString(vertexMainVertexColor))
                .replace("#VERTEX_COLOR_CODE", colorEnabled ? VERTEX_COLOR_CODE : "")
                .replace("#HSL_FUNCTIONS", hslEnabled ? HSL_FUNCTIONS : "")
                .replace("#VERTEX_HSL_CODE", hslEnabled ? VERTEX_HSL_CODE : "");
    }

    private String createFragmentShader(String fragmentDeclarations, String fragmentMain) {
        return FRAGMENT_SHADER_TEMPLATE
                .replace("#FRAGMENT_DECLARATIONS", Tools.Text.validString(fragmentDeclarations))
                .replace("#FRAGMENT_MAIN", Tools.Text.validString(fragmentMain));
    }

}
