// Usable Vertex Shader Variables: vec4 a_position | vec4 v_color | vec4 v_tweak | vec2 v_texCoord
// Usable Fragment Shader Variables: vec4 v_color | vec4 v_tweak | vec2 v_texCoord | sampler2D u_texture | vec2 u_textureSize

// BEGIN VERTEX

void main(){
}

// END VERTEX

// BEGIN FRAGMENT

vec4 colorTintAdd(vec4 color, vec4 modColor){
    color.rgb = clamp(color.rgb+(modColor.rgb-0.5), 0.0, 1.0);
    color.a *= modColor.a;
    return color;
}

void main(){
    gl_FragColor = colorTintAdd(texture2D(u_texture, v_texCoord), v_color);
}

// END FRAGMENT
