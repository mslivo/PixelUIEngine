package org.mslivo.core.engine.ui_engine.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.NumberUtils;

public class UISpriteBatch extends SpriteBatch {
    private static final String VERTEX = """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;
                       
            void main()
            {
                v_color = a_color;
                v_color.a = v_color.a * (255.0/254.0);
                v_texCoords = a_texCoord0;
                gl_Position = u_projTrans * a_position;
            }
            """;
    private static final String FRAGMENT_TETTINGER = """
                   #ifdef GL_ES
                   #define LOWP lowp
                   precision mediump float;
                   #else
                   #define LOWP\s
                   #endif
                   varying vec2 v_texCoords;
                   varying LOWP vec4 v_color;
                   uniform sampler2D u_texture;
                   vec3 applyHue(vec3 rgb, float hue)
                   {
                       vec3 k = vec3(0.57735);
                       float c = cos(hue);
                       //Rodrigues' rotation formula
                       return rgb * c + cross(k, rgb) * sin(hue) + k * dot(k, rgb) * (1.0 - c);
                   }
                   void main()
                   {
                       float hue = 6.2831853 * (v_color.x - 0.5);
                       float saturation = v_color.y * 2.0;
                       float brightness = v_color.z - 0.5;
                       vec4 tgt = texture2D( u_texture, v_texCoords );
                       tgt.rgb = applyHue(tgt.rgb, hue);
                       tgt.rgb = vec3(
                        (dot(tgt.rgb, vec3(0.375, 0.5, 0.125)) + brightness), // lightness
                        ((tgt.r - tgt.b) * saturation), // warmth
                        ((tgt.g - tgt.b) * saturation)); // mildness
                       gl_FragColor = clamp(vec4(
                        dot(tgt.rgb, vec3(1.0, 0.625, -0.5)), // back to red
                        dot(tgt.rgb, vec3(1.0, -0.375, 0.5)), // back to green
                        dot(tgt.rgb, vec3(1.0, -0.375, -0.5)), // back to blue
                        tgt.a * v_color.w), 0.0, 1.0); // keep alpha, then clamp
                   }
            """;
    private static final String FRAGMENT_GPT = """
            #ifdef GL_ES
            #define LOWP lowp
            precision mediump float;
            #else
            #define LOWP
            #endif

            varying LOWP vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;

            vec3 hsl2rgb(vec3 c) {
                vec3 rgb = clamp(abs(mod(c.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0, 0.0, 1.0);
                return c.z + c.y * (rgb - 0.5) * (1.0 - abs(2.0 * c.z - 1.0));
            }

            void main()
            {
                vec3 hsl = v_color.rgb;
                vec3 rgb = hsl2rgb(hsl);
                gl_FragColor = vec4(rgb, v_color.a) * texture2D(u_texture, v_texCoords);
            }
            """;

    private Color rgba;
    private float rgba_packed;
    private Color hsla;

    public UISpriteBatch() {
        super(8191, new ShaderProgram(VERTEX, FRAGMENT_TETTINGER));
        this.rgba = new Color(1f, 1f, 1f, 1f);
        this.hsla = new Color(0f, 0f, 1f, 1f);
        this.rgba_packed = this.rgba.toFloatBits();
    }

    @Override
    public void setColor(Color color) {
        this.setColor(color.r, color.g, color.b, color.a);
    }

    @Override
    public void setPackedColor(float packedColor) {
        int c = NumberUtils.floatToIntColor(packedColor);
        this.setColor(((c & 0xff000000) >>> 24) / 255f,
        ((c & 0x00ff0000) >>> 16) / 255f,
        ((c & 0x0000ff00) >>> 8) / 255f,
        ((c & 0x000000ff)) / 255f);
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        this.rgba.set(r, g, b, a);
        this.rgba_packed = this.rgba.toFloatBits();
        updateHSLAfromRGBA();
        super.setPackedColor(Color.toFloatBits(0.5f,0.5f,0.5f, 1f));
    }

    @Override
    public Color getColor() {
        return this.rgba;
    }

    @Override
    public float getPackedColor() {
        return rgba_packed;
    }

    private void updateHSLAfromRGBA() {
        float x, y, z, w;
        if (rgba.g < rgba.b) {
            x = rgba.b;
            y = rgba.g;
            z = -1f;
            w = 2f / 3f;
        } else {
            x = rgba.g;
            y = rgba.b;
            z = 0f;
            w = -1f / 3f;
        }
        if (rgba.r < x) {
            z = w;
            w = rgba.r;
        } else {
            w = x;
            x = rgba.r;
        }
        float d = x - Math.min(w, y);
        float l = x * (1f - 0.5f * d / (x + 1e-10f));
        hsla.set(Math.abs(z + (w - y) / (6f * d + 1e-10f)), (x - l) / (Math.min(l, 1f - l) + 1e-10f), l, rgba.a);
    }

    /*
    private static float rgb2hsl(final float r, final float g, final float b, final float a) {
        float x, y, z, w;
        if (g < b) {
            x = b;
            y = g;
            z = -1f;
            w = 2f / 3f;
        } else {
            x = g;
            y = b;
            z = 0f;
            w = -1f / 3f;
        }
        if (r < x) {
            z = w;
            w = r;
        } else {
            w = x;
            x = r;
        }
        float d = x - Math.min(w, y);
        float l = x * (1f - 0.5f * d / (x + 1e-10f));
        return Color.toFloatBits(Math.abs(z + (w - y) / (6f * d + 1e-10f)), (x - l) / (Math.min(l, 1f - l) + 1e-10f), l, a);
    }*/

}
