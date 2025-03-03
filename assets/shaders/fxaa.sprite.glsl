#HSL_ENABLED true COLOR_ENABLED true
#VERTEX
#FRAGMENT
void main(){

	vec4 textureColor = texture2D(u_texture, v_texCoords);
	float scanline = sin(v_texCoords.y * textureSize.y * 3.14159) * 0.1;
	vec4 fragColor = textureColor-scanline;

}