package net.mslivo.pixelui.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Arrays;

/**
 * Contains ONLY the common functionality shared by SpriteRenderer and PrimitiveRenderer.
 * No VBOs, no draw(), no texture logic, no primitive logic.
 * Only:
 * - color
 * - tweak
 * - blend
 * - matrix handling
 * - shader switching
 * - uniform cache
 * - save/load state
 */
public abstract class CommonRenderer {

    // -------- Common Constants --------
    public static final String PROJTRANS_UNIFORM = "u_projTrans";

    protected static final String ERROR_END_BEGIN = ".end() must be called before begin.";
    protected static final String ERROR_BEGIN_END = ".begin() must be called before end.";

    protected static final int RGB_SRC = 0, RGB_DST = 1, ALPHA_SRC = 2, ALPHA_DST = 3;

    // -------- State --------
    protected float color, color_save, color_reset;
    protected float tweak, tweak_save, tweak_reset;
    protected int[] blend, blend_save, blend_reset;

    protected boolean blendingEnabled = true;

    protected boolean drawing = false;

    // -------- Matrices --------
    protected final Matrix4 projectionMatrix = new Matrix4();
    protected final Matrix4 transformMatrix = new Matrix4();
    protected final Matrix4 combinedMatrix = new Matrix4();

    // -------- Shader --------
    protected ShaderProgram shader;
    protected ShaderProgram defaultShader;

    protected final Color tempColor;
    // Uniform caching per shader
    protected final ObjectMap<ShaderProgram, ObjectIntMap<String>> uniformLocationCache = new ObjectMap<>();

    // -------- Constructor --------
    protected CommonRenderer() {
        this.projectionMatrix.setToOrtho2D(0, 0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );

        this.color_reset = colorPackedRGBA(0.5f, 0.5f, 0.5f, 1f);
        this.color = this.color_reset;
        this.color_save = this.color_reset;

        this.tweak_reset = colorPackedRGBA(0.5f, 0.5f, 0.5f, 1f);
        this.tweak = this.tweak_reset;
        this.tweak_save = this.tweak_reset;

        this.tempColor = new Color(Color.CLEAR);

        this.blend_reset = new int[]{
                GL32.GL_SRC_ALPHA,
                GL32.GL_ONE_MINUS_SRC_ALPHA,
                GL32.GL_ONE,
                GL32.GL_ONE_MINUS_SRC_ALPHA
        };
        this.blend = Arrays.copyOf(this.blend_reset, 4);
        this.blend_save = Arrays.copyOf(this.blend_reset, 4);
    }

    // -------- Shader Helpers --------
    protected int uniformLocation(String uniform) {
        ObjectIntMap<String> map = uniformLocationCache.get(shader);
        if (map == null) {
            map = new ObjectIntMap<>();
            uniformLocationCache.put(shader, map);
        }
        int loc = map.get(uniform, -1);
        if (loc == -1) {
            loc = shader.getUniformLocation(uniform);
            map.put(uniform, loc);
        }
        return loc;
    }

    protected void setupMatrices() {
        combinedMatrix.set(projectionMatrix).mul(transformMatrix);
        shader.setUniformMatrix(uniformLocation(PROJTRANS_UNIFORM), combinedMatrix);
    }

    public void setShader(ShaderProgram shader) {
        ShaderProgram next = shader != null ? shader : defaultShader;
        if (this.shader == next) return;

        this.shader = next;

        if (drawing) {
            flush();
            this.shader.bind();
            setupMatrices();
        }
    }

    protected abstract void flush();

    // -------- State Save/Load --------
    public void saveState() {
        this.color_save = this.color;
        this.tweak_save = this.tweak;
        System.arraycopy(this.blend, 0, this.blend_save, 0, 4);
    }

    public void loadState() {
        this.color = this.color_save;
        this.tweak = this.tweak_save;
        setBlendFunctionSeparate(
                blend_save[RGB_SRC],
                blend_save[RGB_DST],
                blend_save[ALPHA_SRC],
                blend_save[ALPHA_DST]
        );
    }

    // -------- Colors --------
    public void setColor(float r, float g, float b, float a) {
        this.color = colorPackedRGBA(r, g, b, a);
    }

    public void setColor(Color color){
        this.color = colorPackedRGBA(color.r,color.g,color.b,color.a);
    }

    public void setColor(Color color, float a){
        this.color = colorPackedRGBA(color.r,color.g,color.b,a);
    }

    public void setPackedColor(float packed) {
        this.color = packed;
    }

    public float getPackedColor() {
        return this.color;
    }

    public void setColorReset() {
        this.color = this.color_reset;
    }

    public void setColorResetValues(float r, float g, float b, float a) {
        this.color_reset = colorPackedRGBA(r, g, b, a);
        this.color = this.color_reset;
    }

    // -------- Tweaks --------
    public void setTweak(float t1, float t2, float t3, float t4) {
        this.tweak = colorPackedRGBA(t1, t2, t3, t4);
    }

    public void setPackedTweak(float packed) {
        this.tweak = packed;
    }

    public float getPackedTweak() {
        return this.tweak;
    }

    public Color getColor(){
        Color.abgr8888ToColor(this.tempColor,getPackedColor());
        return this.tempColor;
    }

    public void setTweakReset() {
        this.tweak = this.tweak_reset;
    }

    public void setTweakResetValues(float h, float s, float l, float c) {
        this.tweak_reset = colorPackedRGBA(h, s, l, c);
        this.tweak = this.tweak_reset;
    }

    // -------- Blending --------
    public void setBlendingEnabled(boolean enabled) {
        this.blendingEnabled = enabled;
    }

    public void setBlendFunction(int src, int dst) {
        if (blend[RGB_SRC] == src && blend[RGB_DST] == dst &&
                blend[ALPHA_SRC] == src && blend[ALPHA_DST] == dst)
            return;

        blend[RGB_SRC] = src;
        blend[RGB_DST] = dst;
        blend[ALPHA_SRC] = src;
        blend[ALPHA_DST] = dst;

        if (drawing) {
            flush();
            Gdx.gl.glBlendFunc(src, dst);
        }
    }

    public void setBlendFunctionSeparate(int srcColor, int dstColor, int srcAlpha, int dstAlpha) {
        if (blend[RGB_SRC] == srcColor && blend[RGB_DST] == dstColor &&
                blend[ALPHA_SRC] == srcAlpha && blend[ALPHA_DST] == dstAlpha)
            return;

        blend[RGB_SRC] = srcColor;
        blend[RGB_DST] = dstColor;
        blend[ALPHA_SRC] = srcAlpha;
        blend[ALPHA_DST] = dstAlpha;

        if (drawing) {
            flush();
            Gdx.gl.glBlendFuncSeparate(srcColor, dstColor, srcAlpha, dstAlpha);
        }
    }

    public void setBlendFunctionReset() {
        setBlendFunctionSeparate(
                blend_reset[RGB_SRC],
                blend_reset[RGB_DST],
                blend_reset[ALPHA_SRC],
                blend_reset[ALPHA_DST]
        );
    }

    public void setBlendFunctionResetValuesSeparate(int srcC, int dstC, int srcA, int dstA) {
        blend_reset[RGB_SRC] = srcC;
        blend_reset[RGB_DST] = dstC;
        blend_reset[ALPHA_SRC] = srcA;
        blend_reset[ALPHA_DST] = dstA;
        setBlendFunctionReset();
    }

    public void setBlendFunctionResetValues(int src, int dst) {
        blend_reset[RGB_SRC] = src;
        blend_reset[RGB_DST] = dst;
        blend_reset[ALPHA_SRC] = src;
        blend_reset[ALPHA_DST] = dst;
        setBlendFunctionReset();
    }

    public void setBlendFunctionLayer() {
        this.setBlendFunctionSeparate(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA, GL32.GL_ONE, GL32.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setBlendFunctionComposite() {
        this.setBlendFunction(GL32.GL_ONE, GL32.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void setAllReset(){
        this.setColorReset();
        this.setTweakReset();
        this.setBlendFunctionReset();
    }

    // -------- Matrices --------
    public void setProjectionMatrix(Matrix4 projection) {
        if (Arrays.equals(projectionMatrix.val, projection.val)) return;

        if (drawing) flush();
        this.projectionMatrix.set(projection);
        if (drawing) setupMatrices();
    }

    // -------- Drawing Status --------
    public boolean isDrawing() {
        return drawing;
    }


    protected float colorPackedRGBA(float r, float g, float b, float a) {
        return NumberUtils.intBitsToFloat(
                ((int)(a * 255) << 24 & 0xFE000000) |
                        ((int)(b * 255) << 16 & 0xFF0000) |
                        ((int)(g * 255) << 8  & 0xFF00) |
                        ((int)(r * 255)       & 0xFF)
        );
    }
}
