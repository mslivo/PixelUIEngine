package net.mslivo.core.engine.ui_engine.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;

public class PrimitiveRenderer {

    private static final String TWEAK_ATTRIBUTE = "a_tweak";
    private static final String VERTEX_COLOR_ATTRIBUTE = "a_vertexColor";

    private static final String VERTEX_SHADER = """
            attribute vec4 $POSITION_ATTRIBUTE;
            attribute vec4 $COLOR_ATTRIBUTE;
            attribute vec4 $VERTEXCOLOR_ATTRIBUTE;
            attribute vec4 $TWEAK_ATTRIBUTE;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec4 fragColor;
            const vec3 forward = vec3(1.0 / 3.0);
            
            vec3 rgbToLabColor(vec3 start) {
               vec3 lab = mat3(+0.2104542553, +1.9779984951, +0.0259040371, +0.7936177850, -2.4285922050, +0.7827717662, -0.0040720468, +0.4505937099, -0.8086757660) *
                          pow(mat3(0.4121656120, 0.2118591070, 0.0883097947, 0.5362752080, 0.6807189584, 0.2818474174, 0.0514575653, 0.1074065790, 0.6302613616)
                          * (start.rgb * start.rgb), forward);
               lab.x = pow(lab.x, 1.48);
               lab.yz = lab.yz * 0.5 + 0.5;
               return lab;
            }
            
            vec3 rgbToLabFragment(vec3 start) {
               vec3 lab = mat3(+0.2104542553, +1.9779984951, +0.0259040371, +0.7936177850, -2.4285922050, +0.7827717662, -0.0040720468, +0.4505937099, -0.8086757660) *
                          pow(mat3(0.4121656120, 0.2118591070, 0.0883097947, 0.5362752080, 0.6807189584, 0.2818474174, 0.0514575653, 0.1074065790, 0.6302613616)
                          * (start.rgb * start.rgb), forward);
               lab.x = (pow(lab.x, 1.51)-0.5)*2.0;
               return lab;
            }
            
            void main()
            {
              // Tint Color
              vec4 v_color = $COLOR_ATTRIBUTE;
              v_color.w = v_color.w * (255.0/254.0);
              v_color.rgb = rgbToLabColor(v_color.rgb);
              
              // Tweak
              vec4 v_tweak = $TWEAK_ATTRIBUTE;
              
              // Position
              gl_PointSize = 1.0;
              gl_Position = u_projTrans * $POSITION_ATTRIBUTE;
                            
              // Draw
              vec4 tgt = $VERTEXCOLOR_ATTRIBUTE;
              
              vec3 lab = rgbToLabFragment(tgt.xyz);
              
              //float contrast = (v_tweak.w * (1.5 * 255.0 / 254.0) - 0.75);
              float contrast = clamp(v_tweak.w-0.5,0.0,1.0);
              lab.xyz = lab.xyz / (contrast * abs(lab.xyz) + (1.0 - contrast));

              lab.x = pow(clamp(lab.x * v_tweak.x + v_color.x, 0.0, 1.0),0.666666);
              lab.yz = clamp((lab.yz * v_tweak.yz + v_color.yz - 0.5) * 2.0, -1.0, 1.0);
              lab = mat3(1.0, 1.0, 1.0, +0.3963377774, -0.1055613458, -0.0894841775, +0.2158037573, -0.0638541728, -1.2914855480) * lab;

              fragColor = vec4(sqrt(clamp(mat3(+4.0767245293, -1.2681437731, -0.0041119885, -3.3072168827, +2.6093323231, -0.7034763098, +0.2307590544, -0.3411344290, +1.7068625689) *
                             (lab * lab * lab),0.0, 1.0)), v_color.a * tgt.a);
            }
            """
            .replace("$POSITION_ATTRIBUTE", ShaderProgram.POSITION_ATTRIBUTE)
            .replace("$COLOR_ATTRIBUTE", ShaderProgram.COLOR_ATTRIBUTE)
            .replace("$VERTEXCOLOR_ATTRIBUTE", VERTEX_COLOR_ATTRIBUTE)
            .replace("$TWEAK_ATTRIBUTE", TWEAK_ATTRIBUTE);

    private static final String FRAGMENT_SHADER = """
                #ifdef GL_ES
                #define LOWP lowp
                 precision mediump float;
                #else
                 #define LOWP
                #endif
                varying LOWP vec4 fragColor;
                
                void main() {                   
                   gl_FragColor = fragColor;
                }
            """;


    private static final String COLOR_ATTRIBUTE = "a_color";
    private static final String ERROR_END_BEGIN = "PrimitiveRenderer.end must be called before begin.";
    private static final String ERROR_BEGIN_END = "PrimitiveRenderer.begin must be called before end.";
    private static final String ERROR_BEGIN_DRAW = "PrimitiveRenderer.begin must be called before drawing.";
    private static final int VERTEX_SIZE = 6;
    private static final int VERTEX_SIZE_X2 = VERTEX_SIZE*2;
    private static final int VERTEX_SIZE_X3 = VERTEX_SIZE*3;
    private static final int ARRAY_RESIZE_STEP = 8192;

    private static final float TWEAK_RESET = Color.toFloatBits(0.5f, 0.5f, 0.5f, 0f);
    private static final float COLOR_RESET = Color.toFloatBits(0.5f, 0.5f, 0.5f, 1f);

    public int renderCalls;
    public int totalRenderCalls;
    private final Color tempColor;
    private int primitiveType;
    private final Matrix4 projectionMatrix;
    private float vertexColor;
    private float color;
    private ShaderProgram shader;
    private Mesh mesh;
    private float vertices[];
    private int idx;
    private float tweak;
    private int srcRGB;
    private int dstRGB;
    private int srcAlpha;
    private int dstAlpha;
    private int u_projTrans;
    private boolean drawing;
    private float backup_tweak;
    private float backup_color;
    private int backup_srcRGB;
    private int backup_dstRGB;
    private int backup_srcAlpha;
    private int backup_dstAlpha;

    public PrimitiveRenderer() {
        this(10240);
    }

    public PrimitiveRenderer(int size) {
        this.shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (!shader.isCompiled()) throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
        this.u_projTrans = shader.getUniformLocation("u_projTrans");
        this.drawing = false;
        this.primitiveType = GL20.GL_POINTS;
        this.color = COLOR_RESET;
        this.vertexColor = rgbPacked(1f, 1f, 1f, 1f);
        this.tweak = TWEAK_RESET;
        this.srcRGB = GL20.GL_SRC_ALPHA;
        this.dstRGB = GL20.GL_ONE_MINUS_SRC_ALPHA;
        this.srcAlpha = GL20.GL_SRC_ALPHA;
        this.dstAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;
        this.tempColor = new Color(Color.GRAY);
        this.idx = 0;
        this.projectionMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.backup_tweak = 0;
        this.backup_color = 0f;
        this.backup_srcRGB = GL20.GL_SRC_ALPHA;
        this.backup_dstRGB = GL20.GL_ONE_MINUS_SRC_ALPHA;
        this.backup_srcAlpha = GL20.GL_SRC_ALPHA;
        this.backup_dstAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;
        this.vertices = createVerticesArray(ARRAY_RESIZE_STEP * VERTEX_SIZE, null);
        this.mesh = createMesh(ARRAY_RESIZE_STEP * VERTEX_SIZE);
    }

    public void setProjectionMatrix(Matrix4 projection) {
        this.projectionMatrix.set(projection);
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
        shader.setUniformMatrix(u_projTrans, this.projectionMatrix);


        // Blending
        if (!Gdx.gl.glIsEnabled(GL20.GL_BLEND)) Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);

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

        try {
            vertices[idx] = x;
            vertices[idx+1] = y;
            vertices[idx+2] = 0;
            vertices[idx+3] = vertexColor;
            vertices[idx+4] = color;
            vertices[idx+5] = tweak;

            idx += VERTEX_SIZE;
        } catch (ArrayIndexOutOfBoundsException _) {
            resizeArray();
        }
    }

    public void vertex(float x1, float y1, float x2, float y2) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_DRAW);

        try {
            vertices[idx] = x1;
            vertices[idx+1] = y1;
            vertices[idx+2] = 0;
            vertices[idx+3] = vertexColor;
            vertices[idx+4] = color;
            vertices[idx+5] = tweak;

            vertices[idx+6] = x2;
            vertices[idx+7] = y2;
            vertices[idx+8] = 0;
            vertices[idx+9] = vertexColor;
            vertices[idx+10] = color;
            vertices[idx+11] = tweak;

            idx += VERTEX_SIZE_X2;
        } catch (ArrayIndexOutOfBoundsException _) {
            resizeArray();
        }
    }

    public void vertex(float x1, float y1, float x2, float y2, float x3, float y3) {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_DRAW);

        try {
            vertices[idx] = x1;
            vertices[idx+1] = y1;
            vertices[idx+2] = 0;
            vertices[idx+3] = vertexColor;
            vertices[idx+4] = color;
            vertices[idx+5] = tweak;

            vertices[idx+6] = x2;
            vertices[idx+7] = y2;
            vertices[idx+8] = 0;
            vertices[idx+9] = vertexColor;
            vertices[idx+10] = color;
            vertices[idx+11] = tweak;

            vertices[idx+12] = x3;
            vertices[idx+13] = y3;
            vertices[idx+14] = 0;
            vertices[idx+15] = vertexColor;
            vertices[idx+16] = color;
            vertices[idx+17] = tweak;

            idx += VERTEX_SIZE_X3;
        } catch (ArrayIndexOutOfBoundsException _) {
            resizeArray();
        }
    }

    public boolean isDrawing() {
        return drawing;
    }

    private void resizeArray() {
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
        return new Mesh(Mesh.VertexDataType.VertexArray, true, size, 0, new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
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
        if (srcRGB == srcFuncColor && dstRGB == dstFuncColor && srcAlpha == srcFuncAlpha && dstAlpha == dstFuncAlpha)
            return;
        this.srcRGB = srcFuncColor;
        this.dstRGB = dstFuncColor;
        this.srcAlpha = srcFuncAlpha;
        this.dstAlpha = dstFuncAlpha;
        if (drawing) {
            flush();
            Gdx.gl.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
        }
    }

    public int getBlendSrcFunc() {
        return srcRGB;
    }

    public int getBlendDstFunc() {
        return dstRGB;
    }

    public int getBlendSrcFuncAlpha() {
        return srcAlpha;
    }

    public int getBlendDstFuncAlpha() {
        return dstAlpha;
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
        this.color = colorPacked(color.r, color.g, color.b, color.a);
    }

    public void setColor(Color color, float alpha) {
        this.color = colorPacked(color.r, color.g, color.b, alpha);
    }

    public void setColor(float l, float a, float b, float alpha) {
        this.color = colorPacked(l, a, b, alpha);
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
        this.vertexColor = colorPacked(color.r, color.g, color.b, color.a);
    }

    public void setVertexColor(Color color, float alpha) {
        this.vertexColor = colorPacked(color.r, color.g, color.b, alpha);
    }

    public void setVertexColor(float r, float g, float b, float alpha) {
        this.vertexColor = colorPacked(r, g, b, alpha);
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

    public void setTweak(float L, float A, float B, float contrast) {
        tweak = colorPacked(L, A, B, contrast);
    }

    public void setPackedTweak(final float tweak) {
        this.tweak = tweak;
    }

    public void setTweakL(float L) {
        int c = NumberUtils.floatToIntColor(tweak);
        float Contrast = ((c & 0xff000000) >>> 24) / 255f;
        float B = ((c & 0x00ff0000) >>> 16) / 255f;
        float A = ((c & 0x0000ff00) >>> 8) / 255f;
        tweak = colorPacked(L, A, B, Contrast);
    }

    public void setTweakA(float A) {
        int c = NumberUtils.floatToIntColor(tweak);
        float Contrast = ((c & 0xff000000) >>> 24) / 255f;
        float B = ((c & 0x00ff0000) >>> 16) / 255f;
        float L = ((c & 0x000000ff)) / 255f;
        tweak = colorPacked(L, A, B, Contrast);
    }

    public void setTweakB(float B) {
        int c = NumberUtils.floatToIntColor(tweak);
        float Contrast = ((c & 0xff000000) >>> 24) / 255f;
        float A = ((c & 0x0000ff00) >>> 8) / 255f;
        float L = ((c & 0x000000ff)) / 255f;
        tweak = colorPacked(L, A, B, Contrast);
    }

    public void setTweakContrast(float contrast) {
        int c = NumberUtils.floatToIntColor(tweak);
        float b = ((c & 0x00ff0000) >>> 16) / 255f;
        float g = ((c & 0x0000ff00) >>> 8) / 255f;
        float r = ((c & 0x000000ff)) / 255f;
        tweak = colorPacked(r, g, b, contrast);
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

    public float getTweakContrast() {
        int c = NumberUtils.floatToIntColor(this.tweak);
        return ((c & 0xff000000) >>> 24) / 255f;
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
        setPackedTweak(TWEAK_RESET);
    }

    public void setColorReset() {
        setPackedColor(COLOR_RESET);
    }

    public void setBlendFunctionReset() {
        setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
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
        this.backup_tweak = this.tweak;
        this.backup_srcRGB = this.srcRGB;
        this.backup_dstRGB = this.dstRGB;
        this.backup_srcAlpha = this.srcAlpha;
        this.backup_dstAlpha = this.dstAlpha;
    }

    public void loadState() {
        setPackedColor(this.backup_color);
        setPackedTweak(this.backup_tweak);
        setBlendFunctionSeparate(backup_srcRGB, backup_dstRGB, backup_srcAlpha, backup_dstAlpha);
    }

    private static float colorPacked(float red, float green, float blue, float alpha) {
        return NumberUtils.intBitsToFloat(((int) (alpha * 255) << 24 & 0xFE000000) | ((int) (blue * 255) << 16 & 0xFF0000)
                | ((int) (green * 255) << 8 & 0xFF00) | ((int) (red * 255) & 0xFF));
    }

}
