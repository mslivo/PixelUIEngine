#VERTEX
#FRAGMENT

const HIGH float eps = 1.0e-10;

vec4 rgb2hsl(vec4 c)
{
    const vec4 J = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, J.wz), vec4(c.gb, J.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float l = q.x * (1.0 - 0.5 * d / (q.x + eps));
    return vec4(abs(q.z + (q.w - q.y) / (6.0 * d + eps)), (q.x - l) / (min(l, 1.0 - l) + eps), l, c.a);
}

vec4 hsl2rgb(vec4 c)
{
    const vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.x + K.xyz) * 6.0 - K.www);
    float v = (c.z + c.y * min(c.z, 1.0 - c.z));
    return vec4(v * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), 2.0 * (1.0 - c.z / (v + eps))), c.w);
}


void main(){

    vec4 color = colorMod(texture2D(u_texture, v_texCoords));

    vec4 hsl = rgb2hsl(color);
    hsl.x = fract(hsl.x * (1.0+(v_tweak.x-0.5)));
    hsl.y = hsl.y * (1.0+((v_tweak.y-0.5)*2.0));
    hsl.z = clamp(hsl.z * (1.0+((v_tweak.z-0.5)*2.0)),0.0,1.0);
    vec4 fragColor = hsl2rgb(hsl);

}