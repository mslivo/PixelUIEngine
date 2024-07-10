package net.mslivo.example;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.tools.particles.immediate.PrimitiveParticle;
import net.mslivo.core.engine.tools.particles.immediate.PrimitiveParticleSystem;

public class ParticleTest extends PrimitiveParticleSystem<Object> {

    public ParticleTest() {
        super(10000);
    }

    public void addParticle() {
        super.addPointParticle(0, 0, Color.RED);

    }

    public static void main(String[] args) {
        ParticleTest particleTest = new ParticleTest();
        for (int i = 0; i < 1000; i++) {
            particleTest.addParticle();
        }
        long nano = System.nanoTime();

        while (particleTest.particleCount() > 0) {
            particleTest.update();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(particleTest.particleCount());
        }

        System.out.println("finished "+((System.nanoTime()-nano)/1000));

    }

    @Override
    protected boolean updateParticle(PrimitiveParticle<Object> particle) {
        particle.color_a.set(0, particle.color_a.get(0) - MathUtils.random(0.001f,0.01f));
        if (particle.color_a.get(0) < 0) {
            return false;
        }

        return true;
    }
}
