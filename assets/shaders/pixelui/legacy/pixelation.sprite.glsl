// Usable Vertex Shader Variables: vec4 a_position | vec4 v_color | vec4 v_tweak | vec4 v_vertexColor
// Usable Fragment Shader Variables: vec4 v_color | vec4 v_tweak | vec4 v_vertexColor

// BEGIN VERTEX

// END VERTEX

// BEGIN FRAGMENT

    void main(){

        vec2 texCoords = v_texCoord;
        float pixelSize = 2.0 + floor(v_tweak.x * 14.0);
        texCoords = texCoords * u_textureSize;
        texCoords = mix(texCoords, floor((texCoords / pixelSize) + 0.5) * pixelSize, step(0.001, v_tweak.x));
        texCoords = texCoords / u_textureSize;
        gl_FragColor = texture2D( u_texture, texCoords);

    }

// END FRAGMENT