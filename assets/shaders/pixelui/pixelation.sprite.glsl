Usable Vertex Shader Variables: vec4 a_position, vec4 v_color, vec4 v_tweak, vec2 v_texCoord

BEGIN VERTEX

END VERTEX

Useable Fragment Shader Variables: vec4 v_color, vec4 v_tweak, vec2 v_texCoord, sampler2D u_texture, vec2 u_textureSize

BEGIN FRAGMENT

    void main(){

        HIGH vec2 texCoords = v_texCoord;
        float pixelSize = 2.0 + floor(v_tweak.x * 14.0);
        texCoords = texCoords * u_textureSize;
        texCoords = mix(texCoords, floor((texCoords / pixelSize) + 0.5) * pixelSize, step(0.001, v_tweak.x));
        texCoords = texCoords / u_textureSize;
        gl_FragColor = texture2D( u_texture, texCoords);

    }

END FRAGMENT