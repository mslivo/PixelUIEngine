package net.mslivo.core.engine.ui_engine.rendering.shader;

import com.badlogic.gdx.files.FileHandle;
import net.mslivo.core.engine.tools.Tools;

public final class PrimitiveShader extends ShaderCommon {


    private static final String EXTENSION = ".primitive.glsl";

    private static final String VERTEX_SHADER_TEMPLATE = FLOAT_DECLARATIONS +"""
            
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
                        v_vertexColor = a_vertexColor;
                        
                        
                        gl_Position = u_projTrans * a_position;
                        
                        // Custom Code
                        #VERTEX_MAIN
                    }
            """;

    private static final String FRAGMENT_SHADER_TEMPLATE = FLOAT_DECLARATIONS +"""
            
                varying vec4 v_color;
                varying vec4 v_tweak;
                varying vec4 v_vertexColor;
            
                #FRAGMENT_DECLARATIONS
            
                void main() {
            
                   #FRAGMENT_MAIN
                   
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
