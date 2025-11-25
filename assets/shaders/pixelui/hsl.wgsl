struct VertexInput {
    @location(0) a_position : vec2f,
    @location(1) a_texCoord : vec2f,
    @location(3) a_tweak    : vec4f,
    @location(5) a_color    : vec4f,
};

struct Uniforms {
    u_projTrans : mat4x4<f32>,
    u_textureSize : vec2f
};

@group(0) @binding(0)
var<uniform> uniforms : Uniforms;

struct VertexOutput {
    @builtin(position) Position : vec4f,
    @location(0) v_color        : vec4f,
    @location(1) v_texCoord     : vec2f,
    @location(2) v_tweak        : vec4f,
};

const FLOAT_CORRECTION : f32 = 255.0 / 254.0;

@vertex
fn vs_main(input : VertexInput) -> VertexOutput {
    var out : VertexOutput;

    out.v_color    = input.a_color * FLOAT_CORRECTION;
    out.v_tweak    = input.a_tweak * FLOAT_CORRECTION;
    out.v_texCoord = input.a_texCoord;

    out.Position = uniforms.u_projTrans * vec4f(input.a_position,0.0,1.0);

    return out;
}

struct FragmentInput {
    @location(0) v_color    : vec4f,
    @location(1) v_texCoord : vec2f,
    @location(2) v_tweak    : vec4f,
};

@group(0) @binding(1)
var texture : texture_2d<f32>;

@group(0) @binding(2)
var textureSampler : sampler;


fn colorTintAdd(color : vec4f, modColor : vec4f) -> vec4f {
    let newRgb = clamp(color.rgb + (modColor.rgb - vec3<f32>(0.5)),
                       vec3<f32>(0.0), vec3<f32>(1.0));

    return vec4f(newRgb, color.a * modColor.a);
}

fn rgb2hsl(c : vec4f) -> vec4f {
    let eps = 1e-10;
    let J = vec4f(0.0, -1.0/3.0, 2.0/3.0, -1.0);
    let p = mix(vec4f(c.bg, J.wz), vec4f(c.gb, J.xy), select(0.0, 1.0, c.b <= c.g));
    let q = mix(vec4f(p.xyw, c.r), vec4f(c.r, p.yzx), select(0.0, 1.0, p.x <= c.r));
    let d = q.x - min(q.w, q.y);
    let l = q.x * (1.0 - 0.5 * d / (q.x + eps));
    return vec4f(
        abs(q.z + (q.w - q.y) / (6.0 * d + eps)),
        (q.x - l) / (min(l, 1.0 - l) + eps),
        l,
        c.a
    );
}

fn hsl2rgb(c : vec4f) -> vec4f {
    let eps = 1e-10;
    let K = vec4f(1.0, 2.0/3.0, 1.0/3.0, 3.0);
    let p = abs(fract(c.x + K.xyz) * 6.0 - K.www);
    let v = (c.z + c.y * min(c.z, 1.0 - c.z));
    return vec4f(
        v * mix(
            vec3<f32>(K.xxx),
            clamp(p - vec3<f32>(K.xxx), vec3<f32>(0.0), vec3<f32>(1.0)),
            2.0 * (1.0 - c.z / (v + eps))
        ),
        c.w
    );
}

@fragment
fn fs_main(input : FragmentInput) -> @location(0) vec4f {

    let texColor = textureSample(texture, textureSampler, input.v_texCoord);
    var color = colorTintAdd(texColor, input.v_color);

    var hsl = rgb2hsl(color);

    hsl.x = fract(hsl.x + (input.v_tweak.x - 0.5));
    hsl.y = clamp(hsl.y + ((input.v_tweak.y - 0.5) * 2.0), 0.0, 1.0);
    hsl.z = clamp(hsl.z + ((input.v_tweak.z - 0.5) * 2.0), 0.0, 1.0);

    return hsl2rgb(hsl);
}
