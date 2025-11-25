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

abstract class CommonRenderer {

    // -------- Common Constants --------
    public static final String PROJTRANS_UNIFORM = "u_projTrans";

    protected static final String ERROR_END_BEGIN = ".end() must be called before begin.";
    protected static final String ERROR_BEGIN_END = ".begin() must be called before end.";

    protected static final int RGB_SRC = 0, RGB_DST = 1, ALPHA_SRC = 2, ALPHA_DST = 3;

    private static final int[] BLEND_RESET =  new int[]{
            GL32.GL_SRC_ALPHA,
            GL32.GL_ONE_MINUS_SRC_ALPHA,
            GL32.GL_ONE,
            GL32.GL_ONE_MINUS_SRC_ALPHA
    };

    // -------- State --------

    protected int[] blend, blend_save;

    protected boolean blendingEnabled = true;

    private boolean drawing = false;

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
    protected CommonRenderer(ShaderProgram defaultShader) {
        this.projectionMatrix.setToOrtho2D(0, 0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );

        this.tempColor = new Color(Color.CLEAR);

        this.blend = Arrays.copyOf(BLEND_RESET, BLEND_RESET.length);
        this.blend_save = Arrays.copyOf(this.blend, this.blend.length);

        this.defaultShader = defaultShader != null ? defaultShader : provideDefaultShader();
        this.shader = this.defaultShader;
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

    // -------- State Save/Load --------
    public void saveState() {

        System.arraycopy(this.blend, 0, this.blend_save, 0, 4);
        this.saveStateImpl();
    }

    public void loadState() {
        setBlendFunctionSeparate(
                blend_save[RGB_SRC],
                blend_save[RGB_DST],
                blend_save[ALPHA_SRC],
                blend_save[ALPHA_DST]
        );
        this.loadStateImpl();
    }






    // -------- Blending --------
    public void setBlendingEnabled(boolean enabled) {
        this.blendingEnabled = enabled;
    }

    public void isBlendingEnabled(boolean enabled) {
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
            this.flush();
            this.setBlendFuncImpl(src, dst);
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
            this.flush();
            this.setBlendFuncSeparateImpl(srcColor, dstColor, srcAlpha, dstAlpha);
        }
    }

    public void setBlendFunctionComposite() {
        this.setBlendFuncImpl(GL32.GL_ONE, GL32.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void reset(){
        System.arraycopy(BLEND_RESET,0,this.blend,0,this.blend.length);
        this.resetImpl();
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

    protected void setDrawing(boolean drawing){
        this.drawing = drawing;
    }

    protected static float colorPackedRGBA(float r, float g, float b, float a) {
        return NumberUtils.intBitsToFloat(
                ((int)(a * 255) << 24 & 0xFE000000) |
                        ((int)(b * 255) << 16 & 0xFF0000) |
                        ((int)(g * 255) << 8  & 0xFF00) |
                        ((int)(r * 255)       & 0xFF)
        );
    }

    protected abstract void flush();
    public abstract void begin();
    public abstract void end();
    protected abstract void resetImpl();
    protected abstract void setBlendFuncSeparateImpl(int srcColor, int dstColor, int srcAlpha, int dstAlpha);
    protected abstract void setBlendFuncImpl(int srcColor, int dstColor);
    protected abstract void saveStateImpl();
    protected abstract void loadStateImpl();
    protected abstract ShaderProgram provideDefaultShader();

}
