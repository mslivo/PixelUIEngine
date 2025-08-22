Usable Vertex Shader Variables:
vec4 a_position
vec4 v_color
vec4 v_tweak
vec2 v_texCoord

BEGIN VERTEX

END VERTEX

Useable Fragment Shader Variables:
vec4 v_color
vec4 v_tweak
vec2 v_texCoord
sampler2D u_texture
vec2 u_textureSize

BEGIN FRAGMENT

    uniform sampler2D u_lut;
    uniform vec2 u_lutSize;

    vec3 lut(vec3 color, sampler2D lutTexture, vec2 lutTextureSize) {
        float lutSize = lutTextureSize.y;
        float sliceSize = 1.0 / lutSize;
        float slicePixelSize = sliceSize / lutSize;
        float width = lutSize - 1.0;
        float sliceInnerSize = slicePixelSize * width;

        float zSlice0 = floor(color.z * width);
        float zSlice1 = min(zSlice0 + 1.0, width);
        float xOffset = slicePixelSize * 0.5 + color.x * sliceInnerSize;
        float yRange = (color.y * width + 0.5) / lutSize;
        float s0 = xOffset + (zSlice0 * sliceSize);
        float s1 = xOffset + (zSlice1 * sliceSize);

        vec3 slice0Color = texture2D(lutTexture, vec2(s0, yRange)).rgb;
        vec3 slice1Color = texture2D(lutTexture, vec2(s1, yRange)).rgb;

        float zOffset = mod(color.z * width, 1.0);

        return mix(slice0Color, slice1Color, zOffset);
    }



    void main(){

        vec4 color = texture2D(u_texture, v_texCoord);
        vec4 lutColor = vec4(lut(color.rgb,u_lut, u_lutSize),color.a);

        gl_FragColor = mix(color,lutColor,v_tweak.x);

    }

END FRAGMENT