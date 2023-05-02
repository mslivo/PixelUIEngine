package org.mslivo.core.engine.ui_engine.misc;

public class GrayScaleShader {
    public static final String VERTEX = """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;

            uniform mat4 u_projTrans;

            varying vec4 v_color;
            varying vec2 v_texCoords;

            void main() {
                v_color = a_color;
                v_texCoords = a_texCoord0;
                gl_Position = u_projTrans * a_position;
            }""";

    public static final String FRAGMENT = """
            #ifdef GL_ES
                precision mediump float;
            #endif

            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;

            void main() {
              vec4 c = v_color * texture2D(u_texture, v_texCoords);
              float grey = (c.r + c.g + c.b) / 4.0;
              gl_FragColor = vec4(grey, grey, grey, c.a);
            }""";
}
