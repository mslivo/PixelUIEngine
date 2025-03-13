package net.mslivo.core.engine.ui_engine.rendering.shader;

import com.badlogic.gdx.files.FileHandle;
import net.mslivo.core.engine.tools.Tools;

public final class SpriteShader extends ShaderCommon {

    private static final String EXTENSION = ".sprite.glsl";

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
                    attribute vec2 a_texCoord;
                    
                    varying vec4 v_color;
                    varying vec4 v_tweak;
                    varying HIGH vec2 v_texCoord;
                    
                    uniform mat4 u_projTrans;
                    const HIGH float FLOAT_CORRECTION = (255.0/254.0);
            
                    #VERTEX_DECLARATIONS

                    void main()
                    {
                       // Get Attributes
                       v_color = (a_color * FLOAT_CORRECTION);
                       v_tweak = (a_tweak * FLOAT_CORRECTION);
                       v_texCoord = a_texCoord;
                       vec4 v_position = a_position;
                       
                       // Custom Code
                       #VERTEX_MAIN

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
            varying HIGH vec2 v_texCoord;
            
            uniform sampler2D u_texture;
            uniform vec2 u_textureSize;
            
            #FRAGMENT_DECLARATIONS
            
            void main() {
            
                // Custom Code
                #FRAGMENT_MAIN
            
                // Done
                gl_FragColor = fragColor;
            }
            """;


    public SpriteShader(FileHandle shaderFile) {
        this(validateFile(shaderFile).readString());
    }

    private static FileHandle validateFile(FileHandle fileHandle){
        if(!fileHandle.file().getName().endsWith(EXTENSION))
            throw new RuntimeException("Primitive Shader file is not of type *"+EXTENSION);
        return fileHandle;
    }

    public SpriteShader(String shader) {
        super();
        ParseShaderResult parseShaderResult = parseShader(shader);
        this.vertexShaderSource = createVertexShader(parseShaderResult.vertexDeclarations(), parseShaderResult.vertexMain());
        this.fragmentShaderSource = createFragmentShader(parseShaderResult.fragmentDeclarations(), parseShaderResult.fragmentMain());
    }

    private String createVertexShader(String vertexDeclarations, String vertexMain) {
        return VERTEX_SHADER_TEMPLATE
                .replace("#VERTEX_DECLARATIONS", Tools.Text.validString(vertexDeclarations))
                .replace("#VERTEX_MAIN", Tools.Text.validString(vertexMain));
    }

    private String createFragmentShader(String fragmentDeclarations, String fragmentMain) {
        return FRAGMENT_SHADER_TEMPLATE
                .replace("#FRAGMENT_DECLARATIONS", Tools.Text.validString(fragmentDeclarations))
                .replace("#FRAGMENT_MAIN", Tools.Text.validString(fragmentMain));
    }
}
