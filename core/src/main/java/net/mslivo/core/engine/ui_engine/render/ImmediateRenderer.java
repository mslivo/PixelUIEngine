package net.mslivo.core.engine.ui_engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;

public class ImmediateRenderer {

    private static final String VERTEX = """
                attribute vec4 a_position;
                attribute vec4 a_vertexColor;
                attribute vec4 a_color;
                attribute vec4 a_hslt;
                uniform mat4 u_projTrans;
                varying vec4 v_color;
                const float eps = 1.0e-10;

                vec4 rgb2hsl(vec4 c)
                {
                    const vec4 J = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
                    vec4 p = mix(vec4(c.bg, J.wz), vec4(c.gb, J.xy), step(c.b, c.g));
                    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
                    float d = q.x - min(q.w, q.y);
                    float l = q.x * (1.0 - 0.5 * d / (q.x + eps));
                    return vec4(abs(q.z + (q.w - q.y) / (6.0 * d + eps)), (q.x - l) / (min(l, 1.0 - l) + eps), l, c.a);
                }

                vec4 hsl2rgb(vec4 c)
                {
                    const vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
                    vec3 p = abs(fract(c.x + K.xyz) * 6.0 - K.www);
                    float v = (c.z + c.y * min(c.z, 1.0 - c.z));
                    return vec4(v * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), 2.0 * (1.0 - c.z / (v + eps))), c.w);
                }

                void main() {
                   vec4 vertexColor = a_vertexColor;
                   vertexColor.a *= 255.0 / 254.0;
                   
                   vec4 vcolor = a_color;
                   vcolor.a *= 255.0 / 254.0;
                   
                   vec4 hslt = a_hslt;
                   hslt.a *= 255.0 / 254.0;
                   
                   gl_PointSize = 1.0;
                   gl_Position = u_projTrans * a_position;
                   
                   vec4 tgt = rgb2hsl(vertexColor); // convert to HSL
                   
                   tgt.x = fract(tgt.x+hslt.x); // hslt Hue
                   tgt.y *= (hslt.y*2.0); // hslt Saturation
                   tgt.z += (hslt.z-0.5) * 2.0; // hslt Lightness
                   vec4 color = hsl2rgb(tgt); // convert back to RGB 
                   v_color = mix(color, (color*vcolor), hslt.w); // mixed with tinted color based on hslt Tint
                   v_color.rgb = mix(vec3(dot(v_color.rgb, vec3(0.3333))), v_color.rgb, (hslt.y*2.0));  // remove colors based on hslt.saturation
                }
            """;
    private static final String FRAGMENT = """
                #ifdef GL_ES
                #define LOWP lowp
                 precision mediump float;
                #else
                 #define LOWP
                #endif
                varying LOWP vec4 v_color;
                
                void main() {                   
                   gl_FragColor = v_color;
                }
            """;

    private static final String ERROR_END_BEGIN = "ImmediateRenderer.end must be called before begin.";
    private static final String ERROR_BEGIN_END = "ImmediateRenderer.begin must be called before end.";
    public static final String HSLT_ATTRIBUTE = "a_hslt";
    public static final String COLOR_ATTRIBUTE = "a_color";
    public static final String VERTEX_COLOR_ATTRIBUTE = "a_vertexColor";
    private static final int VERTEX_SIZE = 6;
    private static final int MESH_SIZE_VERTICES = 5000 * VERTEX_SIZE;
    private static final int MESH_SIZE_INDICES = 0;
    private static final float HSLT_RESET = Color.toFloatBits(0f, 0.5f, 0.5f, 1f);
    private static final float COLOR_RESET = Color.toFloatBits(1f, 1f, 1f, 1f);

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
    private float hslt;
    private int srcRGB;
    private int dstRGB;
    private int srcAlpha;
    private int dstAlpha;
    private int u_projTrans;
    private boolean drawing;
    public float backup_hslt;
    public float backup_color;

    public ImmediateRenderer() {
        this.shader = new ShaderProgram(VERTEX, FRAGMENT);
        if (!shader.isCompiled()) throw new GdxRuntimeException("Error compiling shader: " + shader.getLog());
        this.u_projTrans = shader.getUniformLocation("u_projTrans");
        this.primitiveType = GL20.GL_POINTS;
        this.color = COLOR_RESET;
        this.vertexColor = rgbPacked(1f, 1f, 1f, 1f);
        this.hslt = HSLT_RESET;
        this.srcRGB = GL20.GL_SRC_ALPHA;
        this.dstRGB = GL20.GL_ONE_MINUS_SRC_ALPHA;
        this.srcAlpha = GL20.GL_SRC_ALPHA;
        this.dstAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;
        this.tempColor = new Color(Color.WHITE);
        this.idx = 0;
        this.projectionMatrix = new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.backup_hslt = 0;
        this.backup_color = 0f;
        this.drawing = false;

        this.vertices = new float[MESH_SIZE_VERTICES];
        this.mesh = createMesh(MESH_SIZE_VERTICES);
    }

    public void setProjectionMatrix(Matrix4 projection) {
        this.projectionMatrix.set(projection);
    }

    public Matrix4 getProjectionMatrix(Matrix4 projection) {
        return this.projectionMatrix;
    }

    public void begin() {
        begin(GL20.GL_POINTS);
    }

    public void begin(int primitiveType) {
        if (drawing) throw new IllegalStateException(ERROR_END_BEGIN);
        this.primitiveType = primitiveType;
        this.renderCalls = 0;

        shader.bind();
        shader.setUniformMatrix(u_projTrans, this.projectionMatrix);

        this.drawing = true;
    }

    public void end() {
        if (!drawing) throw new IllegalStateException(ERROR_BEGIN_END);
        if (idx > 0) flush();
        this.drawing = false;
    }

    private void flush(){
        if (idx == 0) return;

        renderCalls++;
        totalRenderCalls++;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);

        mesh.setVertices(vertices, 0, idx);
        mesh.render(shader, this.primitiveType);
        idx = 0;
    }

    public void dispose() {
        this.mesh.dispose();
    }

    public void vertex(float x, float y, float z) {
        if (!drawing) throw new IllegalStateException("ImmediateRenderer.begin must be called before draw.");
        checkMeshSize();
        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = z;
        vertices[idx + 3] = vertexColor;
        vertices[idx + 4] = color;
        vertices[idx + 5] = hslt;
        idx += VERTEX_SIZE;
    }

    public void vertex(float x, float y) {
        vertex(x, y, 0f);
    }

    public boolean isDrawing() {
        return drawing;
    }

    private void checkMeshSize() {
        if ((idx + VERTEX_SIZE) > mesh.getMaxVertices()) {
            int newSize = mesh.getMaxVertices() + MESH_SIZE_VERTICES;
            float[] newVertices = new float[newSize];
            System.arraycopy(vertices, 0, newVertices, 0, vertices.length);
            this.vertices = newVertices;
            Mesh newMesh = createMesh(newSize);
            mesh.dispose();
            mesh = newMesh;
        }
    }

    private Mesh createMesh(int vertices) {
        return new Mesh(false, vertices, MESH_SIZE_INDICES,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, VERTEX_COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, COLOR_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, HSLT_ATTRIBUTE)
        );
    }

    private float rgbPacked(float red, float green, float blue, float alpha) {
        return Float.intBitsToFloat(((int) (alpha * 255) << 24 & 0xFE000000) | ((int) (blue * 255) << 16 & 0xFF0000)
                | ((int) (green * 255) << 8 & 0xFF00) | ((int) (red * 255) & 0xFF));
    }

    public int getPrimitiveType() {
        return primitiveType;
    }

    public Color getColor() {
        Color.abgr8888ToColor(tempColor, color);
        return tempColor;
    }

    public float getPackedColor() {
        return color;
    }

    public Color getVertexColor() {
        Color.abgr8888ToColor(tempColor, vertexColor);
        return tempColor;
    }

    public float getPackedVertexColor() {
        return vertexColor;
    }

    public void setVertexColor(Color vertexColor) {
        setVertexColor(vertexColor.r, vertexColor.g, vertexColor.b, vertexColor.a);
    }

    public void setVertexColor(float r, float g, float b) {
        setVertexColor(r, g, b, 1f);
    }

    public void setVertexColor(float r, float g, float b, float a) {
        this.vertexColor = rgbPacked(r, g, b, a);
    }

    public void setPackedVertexColor(float vertexColor) {
        this.vertexColor = color;
    }

    public void setColor(Color color) {
        setColor(color.r, color.g, color.b, color.a);
    }

    public void setPackedColor(float color) {
        this.color = color;
    }

    public void setColor(float r, float g, float b) {
        setColor(r, g, b, 1f);
    }

    public void setColor(float r, float g, float b, float a) {
        this.color = rgbPacked(r, g, b, a);
    }

    public float getHue() {
        int c = NumberUtils.floatToIntColor(hslt);
        float a = ((c & 0xff000000) >>> 24) / 255f;
        float b = ((c & 0x00ff0000) >>> 16) / 255f;
        float g = ((c & 0x0000ff00) >>> 8) / 255f;
        float r = ((c & 0x000000ff)) / 255f;
        return ((c & 0x000000ff)) / 255f;
    }

    public float getSaturation() {
        int c = NumberUtils.floatToIntColor(hslt);
        return ((c & 0x0000ff00) >>> 8) / 255f;
    }

    public float getLightness() {
        int c = NumberUtils.floatToIntColor(hslt);
        return ((c & 0x00ff0000) >>> 16) / 255f;
    }

    public float getTint() {
        int c = NumberUtils.floatToIntColor(hslt);
        return ((c & 0xff000000) >>> 24) / 255f;
    }

    public void setHue(float hue) {
        int c = NumberUtils.floatToIntColor(hslt);
        float a = ((c & 0xff000000) >>> 24) / 255f;
        float b = ((c & 0x00ff0000) >>> 16) / 255f;
        float g = ((c & 0x0000ff00) >>> 8) / 255f;
        hslt = rgbPacked(hue, g, b, a);
    }

    public void setSaturation(float saturation) {
        int c = NumberUtils.floatToIntColor(hslt);
        float a = ((c & 0xff000000) >>> 24) / 255f;
        float b = ((c & 0x00ff0000) >>> 16) / 255f;
        float r = ((c & 0x000000ff)) / 255f;
        hslt = rgbPacked(r, saturation, b, a);
    }

    public void setLightness(float lightness) {
        int c = NumberUtils.floatToIntColor(hslt);
        float a = ((c & 0xff000000) >>> 24) / 255f;
        float g = ((c & 0x0000ff00) >>> 8) / 255f;
        float r = ((c & 0x000000ff)) / 255f;
        hslt = rgbPacked(r, g, lightness, a);
    }

    public void setTint(float tint) {
        int c = NumberUtils.floatToIntColor(hslt);
        float b = ((c & 0x00ff0000) >>> 16) / 255f;
        float g = ((c & 0x0000ff00) >>> 8) / 255f;
        float r = ((c & 0x000000ff)) / 255f;
        hslt = rgbPacked(r, g, b, tint);
    }

    public void setHSLT(float hue, float saturation, float lightness, float tint) {
        hslt = rgbPacked(hue, saturation, lightness, tint);
    }

    public void setPackedHSLT(final float hsltPacked) {
        this.hslt = hsltPacked;
    }

    public float getPackedHSLT() {
        return hslt;
    }

    public void setHSLTReset() {
        this.hslt = HSLT_RESET;
    }

    public void setBlendFunction(int srcFunc, int dstFunc) {
        setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
    }

    public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
        if (srcRGB == srcFuncColor && dstRGB == dstFuncColor && srcAlpha == srcFuncAlpha && dstAlpha == dstFuncAlpha)
            return;
        flush();
        this.srcRGB = srcFuncColor;
        this.dstRGB = dstFuncColor;
        this.srcAlpha = srcFuncAlpha;
        this.dstAlpha = dstFuncAlpha;
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

    public void saveBackup(){
        this.backup_color = this.color;
        this.backup_hslt = this.hslt;
    }

    public void loadBackup(){
        this.color = backup_color;
        this.hslt = backup_hslt;
    }
}
