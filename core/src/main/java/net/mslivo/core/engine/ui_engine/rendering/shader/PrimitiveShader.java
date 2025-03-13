package net.mslivo.core.engine.ui_engine.rendering.shader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import net.mslivo.core.engine.tools.Tools;

public final class PrimitiveShader extends ShaderCommon {

    private static final String EXTENSION = ".primitive.glsl";

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
                    attribute vec4 a_tweak;
                    attribute vec4 a_vertexColor;
                    
                    varying vec4 v_color;
                    varying vec4 v_tweak;
                    varying vec4 v_vertexColor;
                    
                    uniform mat4 u_projTrans;
                    const HIGH float FLOAT_CORRECTION = (255.0/254.0);
            
                    #VERTEX_DECLARATIONS
            
                    void main() {
                        // Get Attributes
                        gl_PointSize = 1.0;
                        v_color = (a_color*FLOAT_CORRECTION);
                        v_tweak = (a_tweak*FLOAT_CORRECTION);
                        v_vertexColor = a_vertexColor;
                        vec4 v_position = a_position;

                        // Custom Code
                        #VERTEX_MAIN
            
                        // Done
                        gl_Position = u_projTrans * v_position;
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
                varying vec4 v_vertexColor;
            
                #FRAGMENT_DECLARATIONS
            
                void main() {
            
                   #FRAGMENT_MAIN
            
                   gl_FragColor = fragColor;
                }
            """;

    public PrimitiveShader(FileHandle shaderFile) {
        this(validateFile(shaderFile).readString());
    }

    private static FileHandle validateFile(FileHandle fileHandle){
        if(!fileHandle.file().getName().endsWith(EXTENSION))
            throw new RuntimeException("Primitive Shader file is not of type *"+EXTENSION);
        return fileHandle;
    }

    public PrimitiveShader(String shader) {
        super();
        ParseShaderResult parseShaderResult = parseShader(shader);
        this.vertexShaderSource = createVertexShader(parseShaderResult.vertexDeclarations(), parseShaderResult.vertexMain());
        this.fragmentShaderSource = createFragmentShader(parseShaderResult.fragmentDeclarations(), parseShaderResult.fragmentMain());
    }

    private String createVertexShader(String vertexDeclarations, String vertexMainVertexColor) {
        return VERTEX_SHADER_TEMPLATE
                .replace("#VERTEX_DECLARATIONS", Tools.Text.validString(vertexDeclarations))
                .replace("#VERTEX_MAIN", Tools.Text.validString(vertexMainVertexColor));
    }

    private String createFragmentShader(String fragmentDeclarations, String fragmentMain) {
        return FRAGMENT_SHADER_TEMPLATE
                .replace("#FRAGMENT_DECLARATIONS", Tools.Text.validString(fragmentDeclarations))
                .replace("#FRAGMENT_MAIN", Tools.Text.validString(fragmentMain));
    }

}
