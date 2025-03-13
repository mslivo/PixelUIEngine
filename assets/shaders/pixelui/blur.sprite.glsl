VERTEX:
FRAGMENT:import colorTintAdd blur

void main(){

    vec4 color = texture2D(u_texture, v_texCoord);
    vec4 blurred = blur(v_texCoord, u_texture, u_textureSize);

    gl_FragColor = colorTintAdd(mix(color, blurred, v_tweak.x),v_color);
}