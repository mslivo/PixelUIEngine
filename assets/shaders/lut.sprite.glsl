#HSL_ENABLED true COLOR_ENABLED true
#VERTEX
#FRAGMENT

uniform sampler2D u_lut;
uniform vec2 u_lutSize;

vec4 applyLUT(vec3 color) {
	float lutSize = u_lutSize.y;
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

	vec4 slice0Color = texture2D(u_lut, vec2(s0, yRange));
	vec4 slice1Color = texture2D(u_lut, vec2(s1, yRange));

	float zOffset = mod(color.z * width, 1.0);

	return mix(slice0Color, slice1Color, zOffset);
}

void main(){

    vec4 color = texture2D(u_texture, v_texCoords);

    vec4 fragColor = mix(color,applyLUT(color.rgb),v_tweak.w);

}