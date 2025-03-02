#HSL_ENABLED false COLOR_ENABLED true
#VERTEX_DECLARATIONS
#VERTEX_MAIN
#FRAGMENT_DECLARATIONS
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
#FRAGMENT_MAIN_TEXCOORDS
#FRAGMENT_MAIN_FRAGCOLOR
vec2 uv = texCoords * 2.0 - 1.0;  // Map UV to range [-1, 1]
float dist = length(uv);  // Distance from center
float vignette = smoothstep(0.6, 1.2, dist); // Adjust for a smooth transition

// Blend original and blurred image based on vignette
vec4 original = texture2D(u_texture, texCoords);
vec4 blurred = blur(texCoords);

fragColor = mix(original, blurred, vignette * v_tweak.w);