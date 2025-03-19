vec4 colorTintMul(vec4 color, vec4 modColor){
    color.rgb = clamp(color.rgb*(1.0+((modColor.rgb-0.5)*2.0)),0.0,1.0);
    color.a *= modColor.a;
    return color;
}