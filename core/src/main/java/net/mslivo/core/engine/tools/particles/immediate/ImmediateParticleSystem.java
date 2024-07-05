package net.mslivo.core.engine.tools.particles.immediate;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.tools.particles.ParticleDataProvider;
import net.mslivo.core.engine.ui_engine.rendering.ImmediateRenderer;
import org.lwjgl.opengl.GL20;

import java.util.ArrayDeque;
import java.util.ArrayList;

/*
 * Particle System must be extended and implemented
 */
public abstract class ImmediateParticleSystem<T> {

    private final ArrayList<ImmediateParticle> particles;
    private final ArrayDeque<ImmediateParticle> deleteQueue;
    private final int particleLimit;
    private final ArrayDeque<ImmediateParticle<T>> particlePool;
    private final ParticleDataProvider<T> particleDataProvider;
    private final ImmediateParticleRenderHook<T> particleRenderHook;
    private final Color backupColor;
    private int backupPrimitiveType;

    public interface ImmediateParticleConsumer<T,O> {
        default void accept(ImmediateParticle<T> particle){};
        default void accept(ImmediateParticle<T> particle, O data){};
    }

    public ImmediateParticleSystem(int particleLimit) {
        this(particleLimit, null, null);
    }

    public ImmediateParticleSystem(int particleLimit, ParticleDataProvider<T> particleDataProvider) {
        this(particleLimit, particleDataProvider, null);
    }

    public ImmediateParticleSystem(int particleLimit, ParticleDataProvider<T> particleDataProvider, ImmediateParticleRenderHook<T> particleRenderHook) {
        this.particles = new ArrayList<>();
        this.deleteQueue = new ArrayDeque<>();
        this.particleLimit = Math.max(particleLimit, 0);
        this.particleDataProvider = particleDataProvider != null ? particleDataProvider : new ParticleDataProvider<T>() {
        };
        this.particleRenderHook = particleRenderHook != null ? particleRenderHook : new ImmediateParticleRenderHook<T>() {
        };
        this.particlePool = new ArrayDeque<>(particleLimit);
        this.backupColor = new Color(Color.WHITE);
        this.backupPrimitiveType = 0;
    }

    /* ------- Point ------- */

    protected ImmediateParticle<T> addPointParticle(float x1, float y1, float r1, float g1, float b1, float a1) {
        ImmediateParticle<T> particle = particleNew(ImmediateParticleType.IMMEDIATE_POINTS, true);
        addVertex(particle, x1, y1, r1, g1, b1, a1);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Line ------- */

    protected ImmediateParticle<T> addLineParticle(
            float x1, float y1, float r1, float g1, float b1, float a1,
            float x2, float y2, float r2, float g2, float b2, float a2
    ) {
        ImmediateParticle<T> particle = particleNew(ImmediateParticleType.IMMEDIATE_LINES, true);
        addVertex(particle, x1, y1, r1, g1, b1, a1);
        addVertex(particle, x2, y2, r2, g2, b2, a2);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Triangle ------- */

    protected ImmediateParticle<T> addTriangleParticle(
            float x1, float y1, float r1, float g1, float b1, float a1,
            float x2, float y2, float r2, float g2, float b2, float a2,
            float x3, float y3, float r3, float g3, float b3, float a3
    ) {
        ImmediateParticle<T> particle = particleNew(ImmediateParticleType.IMMEDIATE_TRIANGLES, true);
        addVertex(particle, x1, y1, r1, g1, b1, a1);
        addVertex(particle, x2, y2, r2, g2, b2, a2);
        addVertex(particle, x3, y3, r3, g3, b3, a3);
        addParticleToSystem(particle);
        return particle;
    }

    /* ------- Public Methods ------- */

    public void update() {
        if (particles.size() == 0) return;
        for (int i = 0; i < particles.size(); i++) {
            ImmediateParticle<T> particle = particles.get(i);
            if (!updateParticle(particle, i)) {
                deleteQueue.add(particle);
            }
        }
        deleteQueuedParticles();
    }

    public void render(ImmediateRenderer immediateRenderer) {
        if (particles.size() == 0) return;
        this.backupPrimitiveType = immediateRenderer.getPrimitiveType();
        backupColor.r = immediateRenderer.getVertexColor().r;
        backupColor.g = immediateRenderer.getVertexColor().g;
        backupColor.b = immediateRenderer.getVertexColor().b;
        backupColor.a = immediateRenderer.getVertexColor().a;

        for (int i = 0; i < particles.size(); i++) {
            ImmediateParticle<T> particle = particles.get(i);
            if (!particle.visible) continue;
            particleRenderHook.beforeRenderParticle(immediateRenderer, particle);

            switch (particle.type) {
                case IMMEDIATE_POINTS -> {
                    configureImmediateRenderer(immediateRenderer, GL20.GL_POINT);
                    immediateRenderer.setVertexColor(particle.color_r.get(0), particle.color_g.get(0), particle.color_b.get(0), particle.color_a.get(0));
                    immediateRenderer.vertex(particle.x.get(0), particle.y.get(0));
                }
                default -> {
                    throw new RuntimeException("Particle Type " + particle.type.name() + " not supported by " + this.getClass().getSimpleName());
                }
            }
            particleRenderHook.afterRenderParticle(immediateRenderer, particle);
        }

        // Set Back
        immediateRenderer.setVertexColor(backupColor);
        if (backupPrimitiveType != immediateRenderer.getPrimitiveType()) {
            configureImmediateRenderer(immediateRenderer, backupPrimitiveType);
        }
    }

    private void configureImmediateRenderer(ImmediateRenderer renderer, int primitiveType) {
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

    private void addParticleToSystem(ImmediateParticle<T> particle) {
        if (particle == null) return;
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

    public void forEveryParticle(ImmediateParticleConsumer<T,?> consumer) {
        forEveryParticle(consumer, null);
    }

    public <O> void forEveryParticle(ImmediateParticleConsumer<T,O> consumer, O data) {
        if(data != null) {
            for (int i = 0; i < particles.size(); i++) consumer.accept(particles.get(i), data);
        }else{
            for (int i = 0; i < particles.size(); i++) consumer.accept(particles.get(i));
        }
    }


    /* ------- Private Methods ------- */

    private void deleteQueuedParticles() {
        ImmediateParticle<T> deleteParticle;
        while ((deleteParticle = deleteQueue.poll()) != null) {
            removeParticleFromSystem(deleteParticle);
        }
    }

    private void removeParticleFromSystem(ImmediateParticle<T> particle) {
        if (particle == null) return;
        onParticleDestroy(particle);
        this.particles.remove(particle);
        // add back to pool
        this.particlePool.add(particle);
    }

    private void addVertex(ImmediateParticle<T> particle, float x, float y, float r, float g, float b, float a) {
        particle.x.add(x);
        particle.y.add(y);
        particle.color_r.add(r);
        particle.color_g.add(g);
        particle.color_b.add(b);
        particle.color_a.add(a);
    }


    private ImmediateParticle particleNew(ImmediateParticleType type, boolean visible) {
        if (!canAddParticle()) return null;
        ImmediateParticle<T> particle = particlePool.size() > 0 ? particlePool.pop() : new ImmediateParticle<>();
        particle.type = type;
        particle.x.clear();
        particle.y.clear();
        particle.color_r.clear();
        particle.color_g.clear();
        particle.color_b.clear();
        particle.color_a.clear();
        particle.visible = visible;
        particle.data = particle.data == null ? particleDataProvider.provideNewInstance() : particle.data;
        if (particle.data != null) particleDataProvider.resetInstance(particle.data);
        return particle;
    }

    /* ------- Abstract Methods ------- */

    protected void onParticleCreate(ImmediateParticle<T> particle) {
    }

    protected void onParticleDestroy(ImmediateParticle<T> particle) {
    }

    protected abstract boolean updateParticle(ImmediateParticle<T> particle, int index);

}
