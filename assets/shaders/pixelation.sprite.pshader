#HSL_ENABLED true COLOR_ENABLED true
#VERTEX_DECLARATIONS
#VERTEX_MAIN
#FRAGMENT_DECLARATIONS
#FRAGMENT_MAIN_TEXCOORDS
// Pixelation
float pixelSize = 2.0 + floor(v_tweak.w * 14.0);
texCoords = texCoords * u_textureSize;
texCoords = mix(texCoords, floor((texCoords / pixelSize) + 0.5) * pixelSize, step(0.001, v_tweak.w));
texCoords = texCoords / u_textureSize;
#FRAGMENT_MAIN_FRAGCOLOR
