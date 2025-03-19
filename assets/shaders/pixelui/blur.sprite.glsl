VERTEX:
FRAGMENT:

vec4 blur(vec2 texCoords, sampler2D texture, vec2 textureSize) {
    vec2 texelSize = 1.0 / textureSize;
    vec4 color = texture2D(texture, texCoords) * 4.0; // Center pixel
    color += texture2D(texture, texCoords + vec2(texelSize.x, 0.0));
    color += texture2D(texture, texCoords - vec2(texelSize.x, 0.0));
    color += texture2D(texture, texCoords + vec2(0.0, texelSize.y));
    color += texture2D(texture, texCoords - vec2(0.0, texelSize.y));
    color += texture2D(texture, texCoords + vec2(texelSize.x, texelSize.y));
    color += texture2D(texture, texCoords - vec2(texelSize.x, texelSize.y));
    color += texture2D(texture, texCoords + vec2(texelSize.x, -texelSize.y));
    color += texture2D(texture, texCoords - vec2(texelSize.x, -texelSize.y));
    return color / 12.0; // Normalize
}

void main(){

    vec4 color = texture2D(u_texture, v_texCoord);
    vec4 blurred = blur(v_texCoord, u_texture, u_textureSize);

    gl_FragColor = mix(color, blurred, v_tweak.x);
}