VERTEX:
FRAGMENT:import colorTintAdd rgb2hsl hsl2rgb

void main(){

    vec4 color = colorTintAdd(texture2D(u_texture, v_texCoord),v_color);

    vec4 hsl = rgb2hsl(color);
    hsl.x = fract(hsl.x + (v_tweak.x-0.5));
    hsl.y = clamp(hsl.y + ((v_tweak.y-0.5)*2.0),0.0,1.0);
    hsl.z = clamp(hsl.z + ((v_tweak.z-0.5)*2.0),0.0,1.0);
    gl_FragColor = hsl2rgb(hsl);

}