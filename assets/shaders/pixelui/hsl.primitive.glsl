VERTEX:import colorTintAdd rgb2hsl hsl2rgb

void main(){

    v_vertexColor = colorTintAdd(v_vertexColor, v_color);

    vec4 hsl = rgb2hsl(v_vertexColor);
    hsl.x = fract(hsl.x + (v_tweak.x-0.5));
    hsl.y = clamp(hsl.y + ((v_tweak.y-0.5)*2.0),0.0,1.0);
    hsl.z = clamp(hsl.z + ((v_tweak.z-0.5)*2.0),0.0,1.0);
    v_vertexColor = hsl2rgb(hsl);

}

FRAGMENT:

void main(){
    vec4 fragColor = v_vertexColor;
}