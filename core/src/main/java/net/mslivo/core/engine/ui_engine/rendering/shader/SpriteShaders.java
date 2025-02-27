package net.mslivo.core.engine.ui_engine.rendering.shader;

public non-sealed class SpriteShaders extends Shaders {

    public static final SpriteShader defaultShader = new SpriteShader(null, null, null, null, null);

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
            null
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
    );


}
