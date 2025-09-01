// Usable Vertex Shader Variables: vec4 a_position | vec4 v_color | vec4 v_tweak | vec4 v_vertexColor
// Usable Fragment Shader Variables: vec4 v_color | vec4 v_tweak | vec4 v_vertexColor

// BEGIN VERTEX

    vec4 colorTintAdd(vec4 color, vec4 modColor){
         color.rgb = clamp(color.rgb+(modColor.rgb-0.5),0.0,1.0);
         color.a *= modColor.a;
         return color;
    }


    vec4 hsl2rgb(vec4 c)
    {
        const highp float eps = 1.0e-10;
        const vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
        vec3 p = abs(fract(c.x + K.xyz) * 6.0 - K.www);
        float v = (c.z + c.y * min(c.z, 1.0 - c.z));
        return vec4(v * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), 2.0 * (1.0 - c.z / (v + eps))), c.w);
    }

    vec4 rgb2hsl(vec4 c)
    {
        const highp float eps = 1.0e-10;
        const vec4 J = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
        vec4 p = mix(vec4(c.bg, J.wz), vec4(c.gb, J.xy), step(c.b, c.g));
        vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
        float d = q.x - min(q.w, q.y);
        float l = q.x * (1.0 - 0.5 * d / (q.x + eps));
        return vec4(abs(q.z + (q.w - q.y) / (6.0 * d + eps)), (q.x - l) / (min(l, 1.0 - l) + eps), l, c.a);
    }

    void main(){

        v_vertexColor = colorTintAdd(v_vertexColor, v_color);

        vec4 hsl = rgb2hsl(v_vertexColor);
        hsl.x = fract(hsl.x + (v_tweak.x-0.5));
        hsl.y = clamp(hsl.y + ((v_tweak.y-0.5)*2.0),0.0,1.0);
        hsl.z = clamp(hsl.z + ((v_tweak.z-0.5)*2.0),0.0,1.0);
        v_vertexColor = hsl2rgb(hsl);

    }


// END VERTEX

// BEGIN FRAGMENT

    void main(){
        gl_FragColor = v_vertexColor;
    }

// END FRAGMENT