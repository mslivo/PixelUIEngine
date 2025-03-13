VERTEX:
FRAGMENT:import colorModAdd lut

uniform sampler2D u_lut;
uniform vec2 u_lutSize;

void main(){

    vec4 color = colorModAdd(texture2D(u_texture, v_texCoords),v_color);
    vec4 lutColor = vec4(lut(color.rgb,u_lut, u_lutSize),color.a);

    vec4 fragColor = mix(color,lutColor,v_tweak.w);

}