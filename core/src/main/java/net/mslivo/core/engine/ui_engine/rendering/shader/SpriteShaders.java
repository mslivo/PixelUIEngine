package net.mslivo.core.engine.ui_engine.rendering.shader;

public non-sealed class SpriteShaders extends Shaders {

    public static final SpriteShader defaultShader = new SpriteShader(null, null, null, null, null, true,true);

    public static final SpriteShader pixelationShader = new SpriteShader(
            null, null,
            null,
            """
                    // Pixelation
                    float pixelSize = 2.0 + floor(v_tweak.w * 14.0);
                    texCoords = texCoords * u_textureSize;
                    texCoords = mix(texCoords, floor((texCoords / pixelSize) + 0.5) * pixelSize, step(0.001, v_tweak.w));
                    texCoords = texCoords / u_textureSize;
                    """,
            null,
            true,
            true
    );


    public static final SpriteShader blurShader = new SpriteShader(
            null,
            null,
            null,
            null,
            """                
                    // Blur
                    vec2 texelSize = 1.0/u_textureSize;
                    
                    vec4 colorBlur = fragColor * 0.2;
                    colorBlur += texture2D(u_texture, texCoords + vec2(texelSize.x, 0.0)) * 0.125;
                    colorBlur += texture2D(u_texture, texCoords - vec2(texelSize.x, 0.0)) * 0.125;
                    colorBlur += texture2D(u_texture, texCoords + vec2(0.0, texelSize.y)) * 0.125;
                    colorBlur += texture2D(u_texture, texCoords - vec2(0.0, texelSize.y)) * 0.125;
                    
                    colorBlur += texture2D(u_texture, texCoords + vec2(texelSize.x, texelSize.y)) * 0.075;
                    colorBlur += texture2D(u_texture, texCoords - vec2(texelSize.x, texelSize.y)) * 0.075;
                    colorBlur += texture2D(u_texture, texCoords + vec2(texelSize.x, -texelSize.y)) * 0.075;
                    colorBlur += texture2D(u_texture, texCoords - vec2(texelSize.x, -texelSize.y)) * 0.075;
                    
                    fragColor = mix(fragColor,colorBlur,v_tweak.w);
                    """
            ,true,true
    );

    public static final SpriteShader vignetteBlurShader = new SpriteShader(null,null,
            """
                    vec4 blur(vec2 uv) {
                        vec2 texelSize = 1.0 / u_textureSize;
                        vec4 color = texture2D(u_texture, uv) * 4.0; // Center pixel
                        color += texture2D(u_texture, uv + vec2(texelSize.x, 0.0));
                        color += texture2D(u_texture, uv - vec2(texelSize.x, 0.0));
                        color += texture2D(u_texture, uv + vec2(0.0, texelSize.y));
                        color += texture2D(u_texture, uv - vec2(0.0, texelSize.y));
                        color += texture2D(u_texture, uv + vec2(texelSize.x, texelSize.y));
                        color += texture2D(u_texture, uv - vec2(texelSize.x, texelSize.y));
                        color += texture2D(u_texture, uv + vec2(texelSize.x, -texelSize.y));
                        color += texture2D(u_texture, uv - vec2(texelSize.x, -texelSize.y));
                        return color / 12.0; // Normalize
                    }
                    """
            ,null,
            """
                     vec2 uv = texCoords * 2.0 - 1.0;  // Map UV to range [-1, 1]
                     float dist = length(uv);  // Distance from center
                     float vignette = smoothstep(0.6, 1.2, dist); // Adjust for a smooth transition
                  
                     // Blend original and blurred image based on vignette
                     vec4 original = texture2D(u_texture, texCoords);
                     vec4 blurred = blur(texCoords);
                  
                     fragColor = mix(original, blurred, vignette * v_tweak.w);
                       
                   """,true,true);


}
