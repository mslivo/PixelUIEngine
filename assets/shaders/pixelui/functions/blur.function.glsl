vec4 blur(vec2 texCoords, sampler2D texture, vec2 textureSize) {
    vec2 texelSize = 1.0 / textureSize;
    vec4 color = texture2D(texture, texCoords) * 4.0; // Center pixel
    color += colorMod(texture2D(texture, texCoords + vec2(texelSize.x, 0.0)),v_color);
    color += colorMod(texture2D(texture, texCoords - vec2(texelSize.x, 0.0)),v_color);
    color += colorMod(texture2D(texture, texCoords + vec2(0.0, texelSize.y)),v_color);
    color += colorMod(texture2D(texture, texCoords - vec2(0.0, texelSize.y)),v_color);
    color += colorMod(texture2D(texture, texCoords + vec2(texelSize.x, texelSize.y)),v_color);
    color += colorMod(texture2D(texture, texCoords - vec2(texelSize.x, texelSize.y)),v_color);
    color += colorMod(texture2D(texture, texCoords + vec2(texelSize.x, -texelSize.y)),v_color);
    color += colorMod(texture2D(texture, texCoords - vec2(texelSize.x, -texelSize.y)),v_color);
    return color / 12.0; // Normalize
}