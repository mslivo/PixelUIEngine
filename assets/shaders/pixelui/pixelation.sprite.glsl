VERTEX:
FRAGMENT:import colorTintAdd

void main(){

	HIGH vec2 texCoords = v_texCoord;
	float pixelSize = 2.0 + floor(v_tweak.x * 14.0);
	texCoords = texCoords * u_textureSize;
    texCoords = mix(texCoords, floor((texCoords / pixelSize) + 0.5) * pixelSize, step(0.001, v_tweak.x));
    texCoords = texCoords / u_textureSize;
	vec4 fragColor = colorTintAdd(texture2D( u_texture, texCoords),v_color);
	
}