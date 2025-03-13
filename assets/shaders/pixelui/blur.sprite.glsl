VERTEX:
FRAGMENT:import colorModAdd blur

void main(){

    vec4 color = colorModAdd(texture2D(u_texture, v_texCoords),v_color);
    vec4 blurred = blur(v_texCoords, u_texture, u_textureSize);

    vec4 fragColor = mix(color, blurred, v_tweak.x);
}