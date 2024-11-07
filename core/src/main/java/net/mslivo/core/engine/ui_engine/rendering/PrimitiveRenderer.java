package net.mslivo.core.engine.ui_engine.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;

import java.util.Arrays;

public class PrimitiveRenderer {

    private static final String TWEAK_ATTRIBUTE = "a_tweak";
    private static final String VERTEX_COLOR_ATTRIBUTE = "a_vertexColor";

    private static final String VERTEX_SHADER = """
            attribute vec4 $POSITION_ATTRIBUTE;
            attribute vec4 $COLOR_ATTRIBUTE;
            attribute vec4 $VERTEXCOLOR_ATTRIBUTE;
            attribute vec4 $TWEAK_ATTRIBUTE;
            
            uniform mat4 u_projTrans;
            
            varying vec4 fragColor;
            
            const vec3 forward = vec3(1.0 / 3.0);
            const float twoThird = 2.0 / 3.0;
            
            const mat3 rgbToLabMatrix = mat3(
                +0.2104542553, +1.9779984951, +0.0259040371,
                +0.7936177850, -2.4285922050, +0.7827717662,
                -0.0040720468, +0.4505937099, -0.8086757660
            );
            
            const mat3 rgbToXyzMatrix = mat3(
                0.4121656120, 0.2118591070, 0.0883097947,
                0.5362752080, 0.6807189584, 0.2818474174,
                0.0514575653, 0.1074065790, 0.6302613616
            );
            
            const mat3 labToRgbMatrix = mat3(
                1.0, 1.0, 1.0,
                +0.3963377774, -0.1055613458, -0.0894841775,
                +0.2158037573, -0.0638541728, -1.2914855480
            );
            
            const mat3 xyzToRgbMatrix = mat3(
                +4.0767245293, -1.2681437731, -0.0041119885,
                -3.3072168827, +2.6093323231, -0.7034763098,
                +0.2307590544, -0.3411344290, +1.7068625689
            );
            
            vec3 rgbToLabColor(vec3 color) {
                vec3 xyz = rgbToXyzMatrix * (color * color);
                vec3 lab = rgbToLabMatrix * pow(xyz, forward);
                lab.x = pow(lab.x, 1.48);
                lab.yz = lab.yz * 0.5 + 0.5;
                return lab;
            }
            
            vec3 rgbToLabFragment(vec3 color) {
                vec3 xyz = rgbToXyzMatrix * (color * color);
                vec3 lab = rgbToLabMatrix * pow(xyz, forward);
                lab.x = (pow(lab.x, 1.51) - 0.5) * 2.0;
                return lab;
            }
            
            void main() {
                gl_PointSize = 1.0;
            
                // Tint Color
                vec4 v_color = $COLOR_ATTRIBUTE;
                v_color.w *= 255.0 / 254.0;
                v_color.rgb = rgbToLabColor(v_color.rgb);
            
                // Position
            
                gl_Position = u_projTrans * $POSITION_ATTRIBUTE;
            
                // Draw
                vec3 tgtLab = rgbToLabFragment($VERTEXCOLOR_ATTRIBUTE.rgb);
                vec3 tweak = $TWEAK_ATTRIBUTE.rgb;
                vec3 color = v_color.rgb;
            
                tgtLab.x = pow(clamp(tgtLab.x * $TWEAK_ATTRIBUTE.x + color.x, 0.0, 1.0), twoThird);
                tgtLab.yz = clamp((tgtLab.yz * tweak.yz + color.yz - 0.5) * 2.0, -1.0, 1.0);
                vec3 lab = labToRgbMatrix * tgtLab;
            
                fragColor = vec4(sqrt(clamp(xyzToRgbMatrix * (lab * lab * lab), 0.0, 1.0)), v_color.a * $VERTEXCOLOR_ATTRIBUTE.a);
            }
            """
            .replace("$POSITION_ATTRIBUTE", ShaderProgram.POSITION_ATTRIBUTE)
            .replace("$COLOR_ATTRIBUTE", ShaderProgram.COLOR_ATTRIBUTE)
            .replace("$VERTEXCOLOR_ATTRIBUTE", VERTEX_COLOR_ATTRIBUTE)
            .replace("$TWEAK_ATTRIBUTE", TWEAK_ATTRIBUTE);

    private static final String FRAGMENT_SHADER = """
                #ifdef GL_ES
                    #define LOW lowp
                    #define MED mediump
                    #define HIGH highp
                    precision mediump float;
                #else
                    #define MED
                    #define LOW
                    #define HIGH
                #endif
            
                varying vec4 fragColor;
            
                void main() {                   
                   gl_FragColor = fragColor;
                }
            """;


    private static final String COLOR_ATTRIBUTE = "a_color";
    private static final String ERROR_END_BEGIN = "PrimitiveRenderer.end must be called before begin.";
    private static final String ERROR_BEGIN_END = "PrimitiveRenderer.begin must be called before end.";
    private static final String ERROR_BEGIN_DRAW = "PrimitiveRenderer.begin must be called before drawing.";
    private static final int VERTEX_SIZE = 6;
    private static final int VERTEX_SIZE_X2 = VERTEX_SIZE * 2;
    private static final int VERTEX_SIZE_X3 = VERTEX_SIZE * 3;
    private static final int ARRAY_RESIZE_STEP = 8192;
    private static final int RGB_SRC = 0, RGB_DST = 1, ALPHA_SRC = 2, ALPHA_DST = 3;

    private final Color tempColor;
    private int primitiveType;
    private ShaderProgram shader;
    private Mesh mesh;
    private float vertices[];
    private int idx;
    private int u_projTrans;
    private boolean drawing;
    private int renderCalls;
    private int totalRenderCalls;

    private final Matrix4 projectionMatrix;
    private float color;
    private float vertexColor;
    private float tweak;
    private int[] blend;

    private float backup_tweak;
    private float backup_color;
    private float backup_vertexColor;
    private int[] backup_blend;

    private float reset_tweak;
    private float reset_color;
    private float reset_vertexColor;
    private int[] reset_blend;

    public PrimitiveRenderer() {
        this(10240);
    }

    public PrimitiveRenderer(int size) {
        this.shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (!shader.isCompiled()) throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
        this.u_projTrans = shader.getUniformLocation("u_projTrans");
        this.drawing = false;
        this.primitiveType = GL20.GL_POINTS;
        this.tempColor = new Color(Color.GRAY);
        this.idx = 0;
        this.vertices = createVerticesArray(ARRAY_RESIZE_STEP * VERTEX_SIZE, null);
        this.mesh = createMesh(ARRAY_RESIZE_STEP * VERTEX_SIZE);
        this.renderCalls = this.totalRenderCalls = 0;
        this.projectionMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.reset_tweak = colorPackedRGBA(0.5f, 0.5f, 0.5f, 0.0f);
        this.reset_color = colorPackedRGBA(0.5f, 0.5f, 0.5f, 1f);
        this.reset_vertexColor = colorPackedRGBA(1f, 1f, 1f, 1f);
        this.reset_blend = new int[]{GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA};

        this.color = reset_color;
        this.vertexColor = reset_vertexColor;
        this.tweak = reset_tweak;
        this.blend = new int[]{this.reset_blend[RGB_SRC], this.reset_blend[RGB_DST], this.reset_blend[ALPHA_SRC], this.reset_blend[ALPHA_DST]};

        this.backup_color = this.color;
        this.backup_vertexColor = this.vertexColor;
        this.backup_tweak = this.tweak;
        this.backup_blend = new int[]{this.blend[RGB_SRC], this.blend[RGB_DST], this.blend[ALPHA_SRC], this.blend[ALPHA_DST]};
    }

    public void setProjectionMatrix(Matrix4 projection) {
        if (Arrays.equals(projectionMatrix.val, projection.val)) return;
        if (drawing) flush();
        this.projectionMatrix.set(projection);
        if (drawing) setupMatrices();
    }

    protected void setupMatrices() {
        shader.setUniformMatrix(u_projTrans, this.projectionMatrix);
    }

    public Matrix4 getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public void begin() {
        begin(GL20.GL_POINTS);
    }

    public void begin(int primitiveType) {
        if (drawing) throw new IllegalStateException(ERROR_END_BEGIN);
        this.primitiveType = primitiveType;
        this.renderCalls = 0;
        Gdx.gl.glDepthMask(false);

        shader.bind();
        setupMatrices();


        // Blending
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(this.blend[RGB_SRC], this.blend[RGB_DST], this.blend[ALPHA_SRC], this.blend[ALPHA_DST]);

        this.drawing = true;
    }

    public void end() {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);
        if (idx > 0) flush();
        Gdx.gl.glDepthMask(true);
        this.drawing = false;
    }

    private void flush() {
        if (idx == 0) return;

        renderCalls++;
        totalRenderCalls++;

        mesh.setVertices(vertices, 0, idx);
        mesh.render(shader, this.primitiveType);

        idx = 0;
    }

    public void dispose() {
        this.mesh.dispose();
    }


    public void vertex(float x, float y) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_DRAW);

        checkAndResize(VERTEX_SIZE);

        vertices[idx] = (x + 0.5f);
        vertices[idx + 1] = (y + 0.5f);
        vertices[idx + 2] = 0;
        vertices[idx + 3] = vertexColor;
        vertices[idx + 4] = color;
        vertices[idx + 5] = tweak;

        idx += VERTEX_SIZE;

    }

    public void vertex(float x1, float y1, float x2, float y2) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_DRAW);

        checkAndResize(VERTEX_SIZE_X2);


        vertices[idx] = (x1 + 0.5f);
        vertices[idx + 1] = (y1 + 0.5f);
        vertices[idx + 2] = 0;
        vertices[idx + 3] = vertexColor;
        vertices[idx + 4] = color;
        vertices[idx + 5] = tweak;

        vertices[idx + 6] = (x2 + 0.5f);
        vertices[idx + 7] = (y2 + 0.5f);
        vertices[idx + 8] = 0;
        vertices[idx + 9] = vertexColor;
        vertices[idx + 10] = color;
        vertices[idx + 11] = tweak;

        idx += VERTEX_SIZE_X2;

    }

    public void vertex(float x1, float y1, float x2, float y2, float x3, float y3) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_DRAW);

        checkAndResize(VERTEX_SIZE_X3);

        vertices[idx] = (x1 + 0.5f);
        vertices[idx + 1] = (y1 + 0.5f);
        vertices[idx + 2] = 0;
        vertices[idx + 3] = vertexColor;
        vertices[idx + 4] = color;
        vertices[idx + 5] = tweak;

        vertices[idx + 6] = (x2 + 0.5f);
        vertices[idx + 7] = (y2 + 0.5f);
        vertices[idx + 8] = 0;
        vertices[idx + 9] = vertexColor;
        vertices[idx + 10] = color;
        vertices[idx + 11] = tweak;

        vertices[idx + 12] = (x3 + 0.5f);
        vertices[idx + 13] = (y3 + 0.5f);
        vertices[idx + 14] = 0;
        vertices[idx + 15] = vertexColor;
        vertices[idx + 16] = color;
        vertices[idx + 17] = tweak;

        idx += VERTEX_SIZE_X3;

    }

    public boolean isDrawing() {
        return drawing;
    }

    private void checkAndResize(int sizeNeeded) {
        if ((idx + sizeNeeded) < this.vertices.length)
            return;

        int verticesSizeNew = this.vertices.length + (ARRAY_RESIZE_STEP * VERTEX_SIZE);
        this.vertices = createVerticesArray(verticesSizeNew, this.vertices);

        this.mesh.dispose();
        int meshSizeNew = mesh.getMaxVertices() + (ARRAY_RESIZE_STEP * VERTEX_SIZE);
        this.mesh = createMesh(meshSizeNew);
    }

    private float[] createVerticesArray(int size, float[] copyFrom) {
        float[] newVertices = new float[size];
        // Copy from Old if exists
        if (copyFrom != null) {
            System.arraycopy(copyFrom, 0, newVertices, 0, Math.min(copyFrom.length, newVertices.length));
        }
        return newVertices;
    }

    private Mesh createMesh(int size) {
        Mesh.VertexDataType vertexDataType = (Gdx.gl30 != null) ? Mesh.VertexDataType.VertexBufferObjectWithVAO : Mesh.VertexDataType.VertexArray;
        return new Mesh(vertexDataType, true, size, 0, new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, VERTEX_COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, TWEAK_ATTRIBUTE));
    }

    private float rgbPacked(float red, float green, float blue, float alpha) {
        return Float.intBitsToFloat(((int) (alpha * 255) << 24 & 0xFE000000) | ((int) (blue * 255) << 16 & 0xFF0000)
                | ((int) (green * 255) << 8 & 0xFF00) | ((int) (red * 255) & 0xFF));
    }

    public int getPrimitiveType() {
        return primitiveType;
    }

    public void setBlendFunction(int srcFunc, int dstFunc) {
        setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
    }

    public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
        if (this.blend[RGB_SRC] == srcFuncColor && this.blend[RGB_DST] == dstFuncColor && this.blend[ALPHA_SRC] == srcFuncAlpha && this.blend[ALPHA_DST] == dstFuncAlpha)
            return;

        this.blend[RGB_SRC] = srcFuncColor;
        this.blend[RGB_DST] = dstFuncColor;
        this.blend[ALPHA_SRC] = srcFuncAlpha;
        this.blend[ALPHA_DST] = dstFuncAlpha;
        if (drawing) {
            flush();
            Gdx.gl.glBlendFuncSeparate(blend[RGB_SRC], blend[RGB_DST], blend[ALPHA_SRC], blend[ALPHA_DST]);
        }
    }

    public int getBlendSrcFunc() {
        return blend[RGB_SRC];
    }

    public int getBlendDstFunc() {
        return blend[RGB_DST];
    }

    public int getBlendSrcFuncAlpha() {
        return blend[ALPHA_SRC];
    }

    public int getBlendDstFuncAlpha() {
        return blend[ALPHA_DST];
    }

    public void setShader(ShaderProgram shader) {
        if (drawing) {
            flush();
        }
        this.shader = shader;
        this.u_projTrans = shader.getUniformLocation("u_projTrans");
        this.shader.bind();
    }

    public ShaderProgram getShader() {
        return this.shader;
    }


    // ####### Getter / Setters #######

    // ----- Tint Color -----


    public void setColor(Color color) {
        this.color = colorPackedRGBA(color.r, color.g, color.b, color.a);
    }

    public void setColor(Color color, float alpha) {
        this.color = colorPackedRGBA(color.r, color.g, color.b, alpha);
    }

    public void setColor(float l, float a, float b, float alpha) {
        this.color = colorPackedRGBA(l, a, b, alpha);
    }

    public void setPackedColor(final float color) {
        this.color = color;
    }

    public Color getColor() {
        Color.abgr8888ToColor(tempColor, color);
        return tempColor;
    }

    public float getPackedColor() {
        return this.color;
    }

    public float getR() {
        int c = NumberUtils.floatToIntColor(this.color);
        return ((c & 0x000000ff)) / 255f;
    }

    public float getG() {
        int c = NumberUtils.floatToIntColor(this.color);
        return ((c & 0x0000ff00) >>> 8) / 255f;
    }

    public float getB() {
        int c = NumberUtils.floatToIntColor(this.color);
        return ((c & 0x00ff0000) >>> 16) / 255f;
    }

    public float getAlpha() {
        int c = NumberUtils.floatToIntColor(this.color);
        return ((c & 0xff000000) >>> 24) / 255f;
    }

    // ----- Vertex Color -----

    public void setVertexColor(Color color) {
        this.vertexColor = colorPackedRGBA(color.r, color.g, color.b, color.a);
    }

    public void setVertexColor(Color color, float alpha) {
        this.vertexColor = colorPackedRGBA(color.r, color.g, color.b, alpha);
    }

    public void setVertexColor(float r, float g, float b, float alpha) {
        this.vertexColor = colorPackedRGBA(r, g, b, alpha);
    }

    public void setPackedVertexColor(final float vertexColor) {
        this.vertexColor = vertexColor;
    }

    public Color getVertexColor() {
        Color.abgr8888ToColor(tempColor, vertexColor);
        return tempColor;
    }

    public float getPackedVertexColor() {
        return this.vertexColor;
    }

    public float getVertexR() {
        int c = NumberUtils.floatToIntColor(this.color);
        return ((c & 0x000000ff)) / 255f;
    }

    public float getVertexG() {
        int c = NumberUtils.floatToIntColor(this.color);
        return ((c & 0x0000ff00) >>> 8) / 255f;
    }

    public float getVertexB() {
        int c = NumberUtils.floatToIntColor(this.color);
        return ((c & 0x00ff0000) >>> 16) / 255f;
    }

    public float getVertexAlpha() {
        int c = NumberUtils.floatToIntColor(this.color);
        return ((c & 0xff000000) >>> 24) / 255f;
    }

    // ----- Tweak -----

    public void setTweak(float L, float A, float B) {
        tweak = colorPackedRGB(L, A, B);
    }

    public void setPackedTweak(final float tweak) {
        this.tweak = tweak;
    }

    public void setTweakL(float L) {
        int c = NumberUtils.floatToIntColor(tweak);
        float B = ((c & 0x00ff0000) >>> 16) / 255f;
        float A = ((c & 0x0000ff00) >>> 8) / 255f;
        tweak = colorPackedRGB(L, A, B);
    }

    public void setTweakA(float A) {
        int c = NumberUtils.floatToIntColor(tweak);
        float B = ((c & 0x00ff0000) >>> 16) / 255f;
        float L = ((c & 0x000000ff)) / 255f;
        tweak = colorPackedRGB(L, A, B);
    }

    public void setTweakB(float B) {
        int c = NumberUtils.floatToIntColor(tweak);
        float A = ((c & 0x0000ff00) >>> 8) / 255f;
        float L = ((c & 0x000000ff)) / 255f;
        tweak = colorPackedRGB(L, A, B);
    }

    public float getTweakL() {
        int c = NumberUtils.floatToIntColor(this.tweak);
        return ((c & 0x000000ff)) / 255f;
    }

    public float getTweakA() {
        int c = NumberUtils.floatToIntColor(this.tweak);
        return ((c & 0x0000ff00) >>> 8) / 255f;
    }

    public float getTweakB() {
        int c = NumberUtils.floatToIntColor(this.tweak);
        return ((c & 0x00ff0000) >>> 16) / 255f;
    }

    public float getPackedTweak() {
        return this.tweak;
    }

    public Color getTweak() {
        Color.abgr8888ToColor(tempColor, tweak);
        return tempColor;
    }

    // ---- RESET & STATE ----

    public void setTweakReset() {
        setPackedTweak(reset_tweak);
    }

    public void setColorReset() {
        setPackedColor(reset_color);
    }

    public void setVertexColorReset() {
        setPackedColor(reset_vertexColor);
    }

    public void setBlendFunctionReset() {
        setBlendFunctionSeparate(reset_blend[RGB_SRC], reset_blend[RGB_DST], reset_blend[ALPHA_SRC], reset_blend[ALPHA_DST]);
    }

    public void setTweakAndColorReset() {
        setTweakReset();
        setColorReset();
    }

    public void setAllReset() {
        setTweakReset();
        setColorReset();
        setBlendFunctionReset();
    }

    public void saveState() {
        this.backup_color = this.color;
        this.backup_vertexColor = this.vertexColor;
        this.backup_tweak = this.tweak;
        System.arraycopy(this.blend, 0, this.backup_blend, 0, 4);
    }

    public void loadState() {
        setPackedColor(this.backup_color);
        setPackedVertexColor(this.backup_vertexColor);
        setPackedTweak(this.backup_tweak);
        setBlendFunctionSeparate(this.backup_blend[RGB_SRC], this.backup_blend[RGB_DST], this.backup_blend[ALPHA_SRC], this.backup_blend[ALPHA_DST]);
    }

    public void setColorResetValues(float r, float g, float b, float a) {
        this.reset_color = colorPackedRGBA(r, g, b, a);
        this.setColorReset();
    }

    public void setVertexColorResetValues(float r, float g, float b, float a) {
        this.reset_vertexColor = colorPackedRGBA(r, g, b, a);
        this.setVertexColorReset();
    }

    public void setTweakResetValues(float l, float a, float b) {
        this.reset_tweak = colorPackedRGB(l, a, b);
        this.setTweakReset();
    }

    public void setBlendFunctionSeparateResetValues(int blend_rgb_src, int blend_rgb_dst, int blend_alpha_src, int blend_alpha_blend) {
        this.reset_blend[RGB_SRC] = blend_rgb_src;
        this.reset_blend[RGB_DST] = blend_rgb_dst;
        this.reset_blend[ALPHA_SRC] = blend_alpha_src;
        this.reset_blend[ALPHA_DST] = blend_alpha_blend;
        this.setBlendFunctionReset();
    }

    public void setBlendFunctionResetValues(int blend_src, int blend_dst) {
        this.reset_blend[RGB_SRC] = blend_src;
        this.reset_blend[RGB_DST] = blend_dst;
        this.reset_blend[ALPHA_SRC] = blend_src;
        this.reset_blend[ALPHA_DST] = blend_dst;
        this.setBlendFunctionReset();
    }

    private static float colorPackedRGBA(float red, float green, float blue, float alpha) {
        return NumberUtils.intBitsToFloat(((int) (alpha * 255) << 24 & 0xFE000000) | ((int) (blue * 255) << 16 & 0xFF0000)
                | ((int) (green * 255) << 8 & 0xFF00) | ((int) (red * 255) & 0xFF));
    }

    private static float colorPackedRGB(float red, float green, float blue) {
        return NumberUtils.intBitsToFloat(((int) (blue * 255) << 16 & 0xFF0000)
                | ((int) (green * 255) << 8 & 0xFF00) | ((int) (red * 255) & 0xFF));
    }

    public int getRenderCalls() {
        return this.renderCalls;
    }

    public int getTotalRenderCalls() {
        return this.totalRenderCalls;
    }
}
