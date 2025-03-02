#HSL_ENABLED true COLOR_ENABLED true
#VERTEX
#FRAGMENT
void main(){
	vec4 fragColor = texture2D( u_texture, v_texCoords);
}