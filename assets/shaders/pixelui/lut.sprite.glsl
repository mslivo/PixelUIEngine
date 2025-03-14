VERTEX:
FRAGMENT:import colorTintAdd lut

uniform sampler2D u_lut;
uniform vec2 u_lutSize;

void main(){

    vec4 color = colorTintAdd(texture2D(u_texture, v_texCoord),v_color);
    vec4 lutColor = vec4(lut(color.rgb,u_lut, u_lutSize),color.a);

    gl_FragColor = mix(color,lutColor,v_tweak.w);

}