package net.mslivo.core.engine.tools.particles;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.tools.particles.particles.PrimitiveParticle;
import net.mslivo.core.engine.ui_engine.rendering.PrimitiveRenderer;

public final class PrimitiveParticleSystem<T> extends ParticleSystem<T> {

    private static final int PRIMITIVE_ADD_VERTEXES_MAX = 2;
    private final Color primitiveRendererBackupColor;

    public PrimitiveParticleSystem(Class<T> dataClass, ParticleUpdater<T> particleUpdater) {
        this(dataClass, particleUpdater, Integer.MAX_VALUE);
    }

    public PrimitiveParticleSystem(Class<T> dataClass, ParticleUpdater<T> particleUpdater, int maxParticles) {
        super(dataClass, particleUpdater, maxParticles);
        this.primitiveRendererBackupColor = new Color(Color.CLEAR);
    }

    public void render(PrimitiveRenderer primitiveRenderer) {
        if (super.numParticles == 0) return;
        this.primitiveRendererBackupColor.set(primitiveRenderer.getVertexColor());
        for (int i = 0; i < particles.size(); i++) {
            PrimitiveParticle<T> primitiveParticle = (PrimitiveParticle) particles.get(i);
            if (!primitiveParticle.visible) continue;
            // check for correct type
            if (primitiveRenderer.getPrimitiveType() != primitiveParticle.primitiveType) {
                primitiveRenderer.end();
                primitiveRenderer.begin(primitiveParticle.primitiveType);
            }

            primitiveRenderer.setVertexColor(primitiveParticle.r, primitiveParticle.g, primitiveParticle.b, primitiveParticle.a);
            primitiveRenderer.vertex(primitiveParticle.x, primitiveParticle.y);

            // Additional Vertexes
            for (int iv = 0; iv < primitiveParticle.numAdditionalVertexes; iv++) {
                primitiveRenderer.setVertexColor(primitiveParticle.vtx_r[iv], primitiveParticle.vtx_g[iv], primitiveParticle.vtx_b[iv], primitiveParticle.vtx_a[iv]);
                primitiveRenderer.vertex((primitiveParticle.x + primitiveParticle.vtx_x[iv]), (primitiveParticle.y + primitiveParticle.vtx_y[iv]));
            }
        }
        primitiveRenderer.setVertexColor(primitiveRendererBackupColor);
    }


    public PrimitiveParticle<T> addPrimitiveParticle(int particleType, float x1, float y1, float r1, float g1, float b1, float a1) {
        if (!canAddParticle())
            return null;
        PrimitiveParticle<T> particle = getNextPrimitiveParticle(particleType,
                x1, y1, r1, g1, b1, a1,
                true);
        super.addParticleToSystem(particle);
        return particle;
    }

    public PrimitiveParticle<T> addPrimitiveParticle(int particleType,
                                                     float x1, float y1, float r1, float g1, float b1, float a1,
                                                     float x2, float y2, float r2, float g2, float b2, float a2) {
        if (!canAddParticle())
            return null;
        PrimitiveParticle<T> particle = getNextPrimitiveParticle(particleType,
                x1, y1, r1, g1, b1, a1,
                x2, y2, r2, g2, b2, a2,
                true);
        super.addParticleToSystem(particle);
        return particle;
    }

    public PrimitiveParticle<T> addPrimitiveParticle(int particleType,
                                                     float x1, float y1, float r1, float g1, float b1, float a1,
                                                     float x2, float y2, float r2, float g2, float b2, float a2,
                                                     float x3, float y3, float r3, float g3, float b3, float a3) {
        if (!canAddParticle())
            return null;
        PrimitiveParticle<T> particle = getNextPrimitiveParticle(particleType,
                x1, y1, r1, g1, b1, a1,
                x2, y2, r2, g2, b2, a2,
                x3, y3, r3, g3, b3, a3,
                true);
        super.addParticleToSystem(particle);
        return particle;
    }


    private PrimitiveParticle<T> getNextPrimitiveParticle(int primitiveType,
                                                          float x, float y, float r, float g, float b, float a,
                                                          boolean visible) {
        PrimitiveParticle<T> particle = (PrimitiveParticle<T>) getParticleFromPool(PrimitiveParticle.class);
        if (particle == null) {
            particle = new PrimitiveParticle<>();
            primitiveParticleCreateArrays(particle);
        }

        super.particleSetParticleData(particle, x, y, r, g, b, a, visible);
        particle.primitiveType = primitiveType;
        particle.numAdditionalVertexes = 0;
        return particle;
    }

    private PrimitiveParticle<T> getNextPrimitiveParticle(int primitiveType,
                                                          float x, float y, float r, float g, float b, float a,
                                                          float vtx_x1, float vtx_y1, float vtx_r1, float vtx_g1, float vtx_b1, float vtx_a1,
                                                          boolean visible) {
        PrimitiveParticle<T> particle = (PrimitiveParticle<T>) getParticleFromPool(PrimitiveParticle.class);
        if (particle == null) {
            particle = new PrimitiveParticle<>();
            primitiveParticleCreateArrays(particle);
        }

        super.particleSetParticleData(particle, x, y, r, g, b, a, visible);
        particle.primitiveType = primitiveType;


        particle.vtx_x[0] = vtx_x1;
        particle.vtx_y[0] = vtx_y1;
        particle.vtx_r[0] = vtx_r1;
        particle.vtx_g[0] = vtx_g1;
        particle.vtx_b[0] = vtx_b1;
        particle.vtx_a[0] = vtx_a1;

        particle.numAdditionalVertexes = 1;
        return particle;
    }

    private PrimitiveParticle<T> getNextPrimitiveParticle(int primitiveType,
                                                          float x, float y, float r, float g, float b, float a,
                                                          float vtx_x1, float vtx_y1, float vtx_r1, float vtx_g1, float vtx_b1, float vtx_a1,
                                                          float vtx_x2, float vtx_y2, float vtx_r2, float vtx_g2, float vtx_b2, float vtx_a2,
                                                          boolean visible) {
        PrimitiveParticle<T> particle = (PrimitiveParticle<T>) getParticleFromPool(PrimitiveParticle.class);
        if (particle == null) {
            particle = new PrimitiveParticle<>();
            primitiveParticleCreateArrays(particle);
        }

        super.particleSetParticleData(particle, x, y, r, g, b, a, visible);
        particle.primitiveType = primitiveType;


        particle.vtx_x[0] = vtx_x1;
        particle.vtx_y[0] = vtx_y1;
        particle.vtx_r[0] = vtx_r1;
        particle.vtx_g[0] = vtx_g1;
        particle.vtx_b[0] = vtx_b1;
        particle.vtx_a[0] = vtx_a1;

        particle.vtx_x[1] = vtx_x2;
        particle.vtx_y[1] = vtx_y2;
        particle.vtx_r[1] = vtx_r2;
        particle.vtx_g[1] = vtx_g2;
        particle.vtx_b[1] = vtx_b2;
        particle.vtx_a[1] = vtx_a2;

        particle.numAdditionalVertexes = 2;
        return particle;
    }

    private void primitiveParticleCreateArrays(PrimitiveParticle<T> primitiveParticle) {
        primitiveParticle.vtx_x = new float[PRIMITIVE_ADD_VERTEXES_MAX];
        primitiveParticle.vtx_y = new float[PRIMITIVE_ADD_VERTEXES_MAX];
        primitiveParticle.vtx_r = new float[PRIMITIVE_ADD_VERTEXES_MAX];
        primitiveParticle.vtx_g = new float[PRIMITIVE_ADD_VERTEXES_MAX];
        primitiveParticle.vtx_b = new float[PRIMITIVE_ADD_VERTEXES_MAX];
        primitiveParticle.vtx_a = new float[PRIMITIVE_ADD_VERTEXES_MAX];
    }
}