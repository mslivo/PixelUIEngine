#HSL_ENABLED true COLOR_ENABLED true
#VERTEX
#FRAGMENT
void main(){

	vec4 textureColor = texture2D(u_texture, v_texCoords);
    vec3 color = textureColor.rgb;
    
    vec3 luma = vec3(0.299, 0.587, 0.114);
    float centerLuma = dot(color, luma);
	
	vec2 texelSize = 1.0/u_textureSize;
    
    float lumaTL = dot(texture2D(u_texture, v_texCoords - texelSize).rgb, luma);
    float lumaTR = dot(texture2D(u_texture, v_texCoords + vec2(texelSize.x, -texelSize.y)).rgb, luma);
    float lumaBL = dot(texture2D(u_texture, v_texCoords + vec2(-texelSize.x, texelSize.y)).rgb, luma);
    float lumaBR = dot(texture2D(u_texture, v_texCoords + texelSize).rgb, luma);
    
    float lumaMin = min(centerLuma, min(min(lumaTL, lumaTR), min(lumaBL, lumaBR)));
    float lumaMax = max(centerLuma, max(max(lumaTL, lumaTR), max(lumaBL, lumaBR)));
    
	
	vec4 fragColor;
	
	if (lumaMax - lumaMin < 0.1) {
        fragColor = vec4(color, 1.0);
    }else {
	    vec2 dir = vec2(
        -((lumaTL + lumaBL) - (lumaTR + lumaBR)),
        ((lumaTL + lumaTR) - (lumaBL + lumaBR))
		);
		
		dir = normalize(dir);
	
	    vec3 edgeSample1 = texture2D(u_texture, v_texCoords + dir * texelSize).rgb;
		vec3 edgeSample2 = texture2D(u_texture, v_texCoords - dir * texelSize).rgb;
		vec3 finalColor = mix(color, (edgeSample1 + edgeSample2) * 0.5, v_tweak.w);
	    
		fragColor = vec4(finalColor,textureColor.a);
		
	}
}