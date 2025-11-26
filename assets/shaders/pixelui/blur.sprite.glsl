// Usable Vertex Shader Variables:  vec4 a_position | vec4 v_color | vec4 v_tweak | vec2 v_texCoord
// Usable Fragment Shader Variables: vec4 v_color | vec4 v_tweak | vec2 v_texCoord | sampler2D u_texture | vec2 u_textureSize

// BEGIN VERTEX

void main(){
}

// END VERTEX

// BEGIN FRAGMENT

vec4 blur(vec2 texCoords, sampler2D texture, vec2 textureSize) {
    vec2 texelSize = 1.0 / textureSize;
    vec4 color = texture2D(texture, texCoords) * 4.0;// Center pixel
    color += texture2D(texture, texCoords + vec2(texelSize.x, 0.0));
    color += texture2D(texture, texCoords - vec2(texelSize.x, 0.0));
    color += texture2D(texture, texCoords + vec2(0.0, texelSize.y));
    color += texture2D(texture, texCoords - vec2(0.0, texelSize.y));
    color += texture2D(texture, texCoords + vec2(texelSize.x, texelSize.y));
    color += texture2D(texture, texCoords - vec2(texelSize.x, texelSize.y));
    color += texture2D(texture, texCoords + vec2(texelSize.x, -texelSize.y));
    color += texture2D(texture, texCoords - vec2(texelSize.x, -texelSize.y));
    return color / 12.0;// Normalize
}

void main(){

    vec4 color = texture2D(u_texture, v_texCoord);
    vec4 blurred = blur(v_texCoord, u_texture, u_textureSize);

    gl_FragColor = mix(color, blurred, v_tweak.x);
}

// END FRAGMENT