VERTEX:
FRAGMENT:import colorModAdd blur

void main(){

    vec4 color = texture2D(u_texture, v_texCoords);
    vec4 blurred = blur(v_texCoords, u_texture, u_textureSize);

    vec4 fragColor = colorModAdd(mix(color, blurred, v_tweak.x),v_color);
}