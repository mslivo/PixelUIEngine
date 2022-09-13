package org.vnna.core.engine.ui_engine.render.shaders;

public class SharpeningShader {

    /*
            // Example:
            spriteBatch.setShader(inputState.sharpeningShader);
            inputState.sharpeningShader.setUniformf("renderSize", resolutionWidth, resolutionHeight);
            inputState.sharpeningShader.setUniformf("amount", amount);
     */

    public static final String VERTEX = """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;
                        
            void main()
            {
               v_color = a_color;
               v_color.a = v_color.a * (255.0/254.0);
               v_texCoords = a_texCoord0;
               gl_Position =  u_projTrans * a_position;
            }
            """;


    public static final String FRAGMENT = """
                    #ifdef GL_ES
                        #define PRECISION mediump
                        precision PRECISION float;
                        precision PRECISION int;
                    #else
                        #define PRECISION
                    #endif
                                
                    varying vec2 v_texCoords;
                    uniform sampler2D u_texture;
                    uniform float u_time;
                    uniform vec2 renderSize;
                    uniform float amount;
                    
                    vec4 sharpenFilter(in vec2 fragCoord, float strength){
                        vec2 uv = fragCoord;
                        float dx = 1.0 / renderSize.x;
                        float dy = 1.0 / renderSize.y;
                    
                    	vec4 f =
                    	texture2D(u_texture, uv + vec2( -1.0 * dx , -1.0 * dy)) * -1.0 +
                    	texture2D(u_texture, uv + vec2( 0.0 * dx , -1.0 * dy)) * -1.0 +
                    	texture2D(u_texture, uv + vec2( 1.0 * dx , -1.0 * dy)) * -1.0 +
                    	texture2D(u_texture, uv + vec2( -1.0 * dx , 0.0 * dy)) * -1.0 +
                    	texture2D(u_texture, uv + vec2( 0.0 * dx , 0.0 * dy)) *  9.0 +
                    	texture2D(u_texture, uv + vec2( 1.0 * dx , 0.0 * dy)) * -1.0 +
                    	texture2D(u_texture, uv + vec2( -1.0 * dx , 1.0 * dy)) * -1.0 +
                    	texture2D(u_texture, uv + vec2( 0.0 * dx , 1.0 * dy)) * -1.0 +
                    	texture2D(u_texture, uv + vec2( 1.0 * dx , 1.0 * dy)) * -1.0;
                    	
                    	//return f;
                    	return mix(texture2D(u_texture, uv + vec2( 0.0 * dx , 0.0 * dy)),
                    	            f , 
                    	            strength);
                    }
                     
                    void main () {                       
                    
                        gl_FragColor = sharpenFilter(v_texCoords, amount);
                               
                    }
            """;


}
