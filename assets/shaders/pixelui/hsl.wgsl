struct VertexInput {
    @location(0) a_position : vec4<f32>,
    @location(1) a_color    : vec4<f32>,
    @location(2) a_tweak    : vec4<f32>,
    @location(3) a_texCoord : vec2<f32>,
};

struct Uniforms {
    projectionViewTransform : mat4x4<f32>,
};

@group(0) @binding(0)
var<uniform> uniforms : Uniforms;

struct VertexOutput {
    @builtin(position) Position : vec4<f32>,
    @location(0) v_color        : vec4<f32>,
    @location(1) v_tweak        : vec4<f32>,
    @location(2) v_texCoord     : vec2<f32>,
};

const FLOAT_CORRECTION : f32 = 255.0 / 254.0;

@vertex
fn vs_main(input : VertexInput) -> VertexOutput {
    var out : VertexOutput;

    out.v_color    = input.a_color * FLOAT_CORRECTION;
    out.v_tweak    = input.a_tweak * FLOAT_CORRECTION;
    out.v_texCoord = input.a_texCoord;

    out.Position = uniforms.u_projTrans * input.a_position;

    return out;
}

struct FragmentInput {
    @location(0) v_color    : vec4<f32>,
    @location(1) v_tweak    : vec4<f32>,
    @location(2) v_texCoord : vec2<f32>,
};

@group(0) @binding(1)
var u_texture : texture_2d<f32>;

@group(0) @binding(2)
var u_sampler : sampler;

const FLOAT_CORRECTION : f32 = 255.0 / 254.0;

fn colorTintAdd(color : vec4<f32>, modColor : vec4<f32>) -> vec4<f32> {
    var c = color;
    c.rgb = clamp(c.rgb + (modColor.rgb - vec3<f32>(0.5)), vec3<f32>(0.0), vec3<f32>(1.0));
    c.a = c.a * modColor.a;
    return c;
}

fn rgb2hsl(c : vec4<f32>) -> vec4<f32> {
    let eps = 1e-10;
    let J = vec4<f32>(0.0, -1.0/3.0, 2.0/3.0, -1.0);
    let p = mix(vec4<f32>(c.bg, J.wz), vec4<f32>(c.gb, J.xy), select(0.0, 1.0, c.b <= c.g));
    let q = mix(vec4<f32>(p.xyw, c.r), vec4<f32>(c.r, p.yzx), select(0.0, 1.0, p.x <= c.r));
    let d = q.x - min(q.w, q.y);
    let l = q.x * (1.0 - 0.5 * d / (q.x + eps));
    return vec4<f32>(
        abs(q.z + (q.w - q.y) / (6.0 * d + eps)),
        (q.x - l) / (min(l, 1.0 - l) + eps),
        l,
        c.a
    );
}

fn hsl2rgb(c : vec4<f32>) -> vec4<f32> {
    let eps = 1e-10;
    let K = vec4<f32>(1.0, 2.0/3.0, 1.0/3.0, 3.0);
    let p = abs(fract(c.x + K.xyz) * 6.0 - K.www);
    let v = (c.z + c.y * min(c.z, 1.0 - c.z));
    return vec4<f32>(
        v * mix(
            vec3<f32>(K.xxx),
            clamp(p - vec3<f32>(K.xxx), vec3<f32>(0.0), vec3<f32>(1.0)),
            2.0 * (1.0 - c.z / (v + eps))
        ),
        c.w
    );
}

@fragment
fn fs_main(input : FragmentInput) -> @location(0) vec4<f32> {

    let texColor = textureSample(u_texture, u_sampler, input.v_texCoord);
    var color = colorTintAdd(texColor, input.v_color);

    var hsl = rgb2hsl(color);

    hsl.x = fract(hsl.x + (input.v_tweak.x - 0.5));
    hsl.y = clamp(hsl.y + ((input.v_tweak.y - 0.5) * 2.0), 0.0, 1.0);
    hsl.z = clamp(hsl.z + ((input.v_tweak.z - 0.5) * 2.0), 0.0, 1.0);

    return hsl2rgb(hsl);
}
