#VERTEX
#FRAGMENT
vec4 blur(vec2 uv) {
    vec2 texelSize = 1.0 / u_textureSize;
    vec4 color = texture2D(u_texture, uv) * 4.0; // Center pixel
    color += colorMod(texture2D(u_texture, uv + vec2(texelSize.x, 0.0)),v_color);
    color += colorMod(texture2D(u_texture, uv - vec2(texelSize.x, 0.0)),v_color);
    color += colorMod(texture2D(u_texture, uv + vec2(0.0, texelSize.y)),v_color);
    color += colorMod(texture2D(u_texture, uv - vec2(0.0, texelSize.y)),v_color);
    color += colorMod(texture2D(u_texture, uv + vec2(texelSize.x, texelSize.y)),v_color);
    color += colorMod(texture2D(u_texture, uv - vec2(texelSize.x, texelSize.y)),v_color);
    color += colorMod(texture2D(u_texture, uv + vec2(texelSize.x, -texelSize.y)),v_color);
    color += colorMod(texture2D(u_texture, uv - vec2(texelSize.x, -texelSize.y)),v_color);
    return color / 12.0; // Normalize
}

void main(){

    vec4 color = colorMod(texture2D(u_texture, v_texCoords),v_color);
    vec4 blurred = blur(v_texCoords);

    vec4 fragColor = mix(color, blurred, v_tweak.x);
}