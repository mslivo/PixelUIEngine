package net.mslivo.core.engine.tools.particles.primitive;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.tools.particles.ParticleDataProvider;
import net.mslivo.core.engine.tools.particles.sprite.SpriteParticle;
import net.mslivo.core.engine.ui_engine.rendering.PrimitiveRenderer;
import org.lwjgl.opengl.GL20;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.Consumer;

/*
 * Particle System must be extended and implemented
 */
public abstract class PrimitiveParticleSystem<T> {

    private static final int VERTEXES_MAX = 3;
    private static final float[] ARRAY_RESET = new float[]{0f,0f,0f};
    private final ArrayList<PrimitiveParticle> particles;
    private final ArrayDeque<PrimitiveParticle> deleteQueue;
    private final int particleLimit;
    private final ArrayDeque<PrimitiveParticle<T>> particlePool;
    private final ParticleDataProvider<T> particleDataProvider;
    private final PrimitiveParticleRenderHook<T> particleRenderHook;
    private final Color backupColor;
    private int backupPrimitiveType;
    private final PrimitiveParticleConsumer<Object> parallelConsumer;
    public static final PrimitiveParticleSystem.PrimitiveParticleRenderTest DEFAULT_RENDER_TEST = new PrimitiveParticleSystem.PrimitiveParticleRenderTest() {
        @Override
        public boolean renderPrimitiveParticle(PrimitiveParticle spriteParticle) {
            return true;
        }
    };

    public interface PrimitiveParticleRenderTest<T> {
        default boolean renderPrimitiveParticle(PrimitiveParticle<T> spriteParticle){
            return true;
        }
    }


    public interface PrimitiveParticleConsumer<O> extends Consumer<PrimitiveParticle> {
        default void accept(PrimitiveParticle particle) {
        }

        default void accept(PrimitiveParticle particle, O data) {
        }
    }

    public PrimitiveParticleSystem(int particleLimit) {
        this(particleLimit, null, null);
    }

    public PrimitiveParticleSystem(int particleLimit, ParticleDataProvider<T> particleDataProvider) {
        this(particleLimit, particleDataProvider, null);
    }

    public PrimitiveParticleSystem(int particleLimit, ParticleDataProvider<T> particleDataProvider, PrimitiveParticleRenderHook<T> particleRenderHook) {
        this.particles = new ArrayList<>();
        this.deleteQueue = new ArrayDeque<>();
        this.particleLimit = Math.max(particleLimit, 0);
        this.particleDataProvider = particleDataProvider != null ? particleDataProvider : new ParticleDataProvider<T>() {
        };
        this.particleRenderHook = particleRenderHook != null ? particleRenderHook : new PrimitiveParticleRenderHook<T>() {
        };
        this.parallelConsumer = new PrimitiveParticleConsumer<>() {
            @Override
            public void accept(PrimitiveParticle particle) {
                if (particle != null && !PrimitiveParticleSystem.this.updateParticle(particle)) {
                    synchronized (deleteQueue) {
                        deleteQueue.add(particle);
                    }
                }
            }
        };
        this.particlePool = new ArrayDeque<>(particleLimit);
        this.backupColor = new Color(Color.CLEAR);
        this.backupPrimitiveType = 0;
    }

    /* ------- Point ------- */

    protected PrimitiveParticle<T> addPointParticle(float x1, float y1, float r1, float g1, float b1, float a1) {
        PrimitiveParticle<T> particle = particleNew(GL20.GL_POINTS, true);
        if(particle == null) return null;
        addVertex(particle, x1, y1, r1, g1, b1, a1);
        addParticleToSystem(particle);
        return particle;
    }

    protected PrimitiveParticle<T> addPointParticle(float x1, float y1, Color color1) {
        return addPointParticle(x1, y1, color1.r, color1.g, color1.b, color1.a);
    }

    protected PrimitiveParticle<T> addPointParticle(float x1, float y1, Color color1, float a1) {
        return addPointParticle(x1, y1, color1.r, color1.g, color1.b, a1);
    }

    /* ------- Line ------- */

    protected PrimitiveParticle<T> addLineParticle(
            float x1, float y1, float r1, float g1, float b1, float a1,
            float x2, float y2, float r2, float g2, float b2, float a2
    ) {
        PrimitiveParticle<T> particle = particleNew(GL20.GL_LINES, true);
        if(particle == null) return null;
        addVertex(particle, x1, y1, r1, g1, b1, a1);
        addVertex(particle, x2, y2, r2, g2, b2, a2);
        addParticleToSystem(particle);
        return particle;
    }

    protected PrimitiveParticle<T> addLineParticle(
            float x1, float y1, Color color1,
            float x2, float y2, Color color2
    ) {
        return addLineParticle(
                x1, y1, color1.r, color1.g, color1.b, color1.a,
                x2, y2, color2.r, color2.g, color2.b, color2.a);
    }

    protected PrimitiveParticle<T> addLineParticle(
            float x1, float y1, Color color1, float a1,
            float x2, float y2, Color color2, float a2
    ) {
        return addLineParticle(
                x1, y1, color1.r, color1.g, color1.b, a1,
                x2, y2, color2.r, color2.g, color2.b, a2);
    }

    /* ------- Triangle ------- */

    protected PrimitiveParticle<T> addTriangleParticle(
            float x1, float y1, float r1, float g1, float b1, float a1,
            float x2, float y2, float r2, float g2, float b2, float a2,
            float x3, float y3, float r3, float g3, float b3, float a3
    ) {
        PrimitiveParticle<T> particle = particleNew(GL20.GL_TRIANGLES, true);
        if(particle == null) return null;
        addVertex(particle, x1, y1, r1, g1, b1, a1);
        addVertex(particle, x2, y2, r2, g2, b2, a2);
        addVertex(particle, x3, y3, r3, g3, b3, a3);
        addParticleToSystem(particle);
        return particle;
    }

    protected PrimitiveParticle<T> addTriangleParticle(
            float x1, float y1, Color color1,
            float x2, float y2, Color color2,
            float x3, float y3, Color color3
    ) {
        return addTriangleParticle(
                x1, y1, color1.r, color1.g, color1.b, color1.a,
                x2, y2, color2.r, color2.g, color2.b, color2.a,
                x3, y3, color3.r, color3.g, color3.b, color3.a
        );
    }

    protected PrimitiveParticle<T> addTriangleParticle(
            float x1, float y1, Color color1, float a1,
            float x2, float y2, Color color2, float a2,
            float x3, float y3, Color color3, float a3
    ) {
        return addTriangleParticle(
                x1, y1, color1.r, color1.g, color1.b, a1,
                x2, y2, color2.r, color2.g, color2.b, a2,
                x3, y3, color3.r, color3.g, color3.b, a3
        );
    }

    /* ------- Public Methods ------- */

    public void updateParallel() {
        if (particles.size() == 0) return;
        particles.parallelStream().forEach(this.parallelConsumer);
        deleteQueuedParticles();
    }

    public void update() {
        if (particles.size() == 0) return;
        for (int i = 0; i < particles.size(); i++) {
            PrimitiveParticle<T> particle = particles.get(i);
            if (!updateParticle(particle)) {
                deleteQueue.add(particle);
            }
        }
        deleteQueuedParticles();
    }

    public void render(PrimitiveRenderer primitiveRenderer) {
        render(primitiveRenderer, DEFAULT_RENDER_TEST);
    }


    public void render(PrimitiveRenderer primitiveRenderer, PrimitiveParticleRenderTest renderTest) {
        if (particles.size() == 0) return;
        this.backupPrimitiveType = primitiveRenderer.getPrimitiveType();
        backupColor.r = primitiveRenderer.getVertexColor().r;
        backupColor.g = primitiveRenderer.getVertexColor().g;
        backupColor.b = primitiveRenderer.getVertexColor().b;
        backupColor.a = primitiveRenderer.getVertexColor().a;

        for (int i = 0; i < particles.size(); i++) {
            PrimitiveParticle<T> particle = particles.get(i);
            if (!particle.visible) continue;
            if(!renderTest.renderPrimitiveParticle(particle)) continue;
            particleRenderHook.beforeRenderParticle(primitiveRenderer, particle);

            switch (particle.primitiveType) {
                case GL20.GL_POINTS -> {
                    configureImmediateRenderer(primitiveRenderer, GL20.GL_POINTS);
                    // Vertex 1
                    primitiveRenderer.setVertexColor(particle.r[0], particle.g[0],particle.b[0],particle.a[0]);
                    primitiveRenderer.vertex(particle.x[0], particle.y[0]);
                }
                case GL20.GL_LINES -> {
                    configureImmediateRenderer(primitiveRenderer, GL20.GL_LINES);
                    // Vertex 1
                    primitiveRenderer.setVertexColor(particle.r[0], particle.g[0], particle.b[0], particle.a[0]);
                    primitiveRenderer.vertex(particle.x[0], particle.y[0]);
                    // Vertex 2
                    primitiveRenderer.setVertexColor(particle.r[1], particle.g[1], particle.b[1], particle.a[1]);
                    primitiveRenderer.vertex(particle.x[1], particle.y[1]);
                }
                case GL20.GL_TRIANGLES -> {
                    configureImmediateRenderer(primitiveRenderer, GL20.GL_TRIANGLES);
                    configureImmediateRenderer(primitiveRenderer, GL20.GL_LINES);
                    // Vertex 1
                    primitiveRenderer.setVertexColor(particle.r[0], particle.g[0], particle.b[0], particle.a[0]);
                    primitiveRenderer.vertex(particle.x[0], particle.y[0]);
                    // Vertex 2
                    primitiveRenderer.setVertexColor(particle.r[1], particle.g[1], particle.b[1], particle.a[1]);
                    primitiveRenderer.vertex(particle.x[1], particle.y[1]);
                    // Vertex 3
                    primitiveRenderer.setVertexColor(particle.r[2], particle.g[2], particle.b[2], particle.a[2]);
                    primitiveRenderer.vertex(particle.x[2], particle.y[2]);
                }
                default -> {
                    throw new RuntimeException("Primitive Particle Type \"" + particle.primitiveType + "\" not supported by " + this.getClass().getSimpleName());
                }
            }
            particleRenderHook.afterRenderParticle(primitiveRenderer, particle);
        }

        // Set Back
        primitiveRenderer.setVertexColor(backupColor);
        if (backupPrimitiveType != primitiveRenderer.getPrimitiveType()) {
            configureImmediateRenderer(primitiveRenderer, backupPrimitiveType);
        }
    }

    private void configureImmediateRenderer(PrimitiveRenderer renderer, int primitiveType) {
        if (renderer.isDrawing()) {
            if (renderer.getPrimitiveType() != primitiveType) {
                renderer.end();
                renderer.begin(primitiveType);
            }
        } else {
            renderer.begin(primitiveType);
        }
    }

    public boolean canAddParticle() {
        return this.particles.size() < particleLimit;
    }

    private void addParticleToSystem(PrimitiveParticle<T> particle) {
        onParticleCreate(particle);
        particles.add(particle);
        return;
    }

    public void shutdown() {
        removeAllParticles();
        particlePool.clear();
    }

    public void removeAllParticles() {
        deleteQueue.addAll(particles);
        deleteQueuedParticles();
    }

    public int particleCount() {
        return this.particles.size();
    }

    public void forEachParticle(PrimitiveParticleConsumer consumer) {
        for (int i = 0; i < particles.size(); i++) consumer.accept(particles.get(i));
    }

    public <O> void forEachParticle(PrimitiveParticleConsumer<O> consumer, O data) {
        for (int i = 0; i < particles.size(); i++) consumer.accept(particles.get(i), data);
    }

    /* ------- Private Methods ------- */

    private void deleteQueuedParticles() {
        PrimitiveParticle<T> deleteParticle;
        while ((deleteParticle = deleteQueue.poll()) != null) {
            removeParticleFromSystem(deleteParticle);
        }
    }

    private void removeParticleFromSystem(PrimitiveParticle<T> particle) {
        if (particle == null) return;
        onParticleDestroy(particle);
        this.particles.remove(particle);
        // add back to pool
        this.particlePool.add(particle);
    }

    private void addVertex(PrimitiveParticle<T> particle, float x, float y, float r, float g, float b, float a) {
        particle.x[particle.vertexes] = x;
        particle.y[particle.vertexes] = y;
        particle.r[particle.vertexes] = r;
        particle.g[particle.vertexes] = g;
        particle.b[particle.vertexes] = b;
        particle.a[particle.vertexes] = a;
        particle.vertexes++;
    }


    private PrimitiveParticle particleNew(int primitiveType, boolean visible) {
        if (!canAddParticle()) return null;
        PrimitiveParticle<T> particle = particlePool.poll();
        if (particle == null){
            particle = new PrimitiveParticle<>();
            particle.x = new float[VERTEXES_MAX];
            particle.y = new float[VERTEXES_MAX];
            particle.r = new float[VERTEXES_MAX];
            particle.g = new float[VERTEXES_MAX];
            particle.b = new float[VERTEXES_MAX];
            particle.a = new float[VERTEXES_MAX];
        }
        System.arraycopy(ARRAY_RESET, 0,particle.x,0,VERTEXES_MAX);
        System.arraycopy(ARRAY_RESET, 0,particle.y,0,VERTEXES_MAX);
        System.arraycopy(ARRAY_RESET, 0,particle.r,0,VERTEXES_MAX);
        System.arraycopy(ARRAY_RESET, 0,particle.g,0,VERTEXES_MAX);
        System.arraycopy(ARRAY_RESET, 0,particle.b,0,VERTEXES_MAX);
        System.arraycopy(ARRAY_RESET, 0,particle.a,0,VERTEXES_MAX);

        particle.primitiveType = primitiveType;
        particle.vertexes = 0;
        particle.visible = visible;
        particle.data = particle.data == null ? particleDataProvider.provideNewInstance() : particle.data;
        if (particle.data != null) particleDataProvider.resetInstance(particle.data);
        return particle;
    }

    /* ------- Abstract Methods ------- */

    protected void onParticleCreate(PrimitiveParticle<T> particle) {
    }

    protected void onParticleDestroy(PrimitiveParticle<T> particle) {
    }

    protected abstract boolean updateParticle(PrimitiveParticle<T> particle);

}
