package net.mslivo.example;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.tools.particles.immediate.PrimitiveParticle;
import net.mslivo.core.engine.tools.particles.immediate.PrimitiveParticleSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
            particleTest.updateParallel();
            System.out.println(particleTest.particleCount());
        }

        System.out.println("finished " + ((System.nanoTime() - nano) / 1000));


    }

    @Override
    protected boolean updateParticle(PrimitiveParticle<Object> particle) {
        particle.color_a.set(0, particle.color_a.get(0) - MathUtils.random(0.01f, 0.02f));
        if (particle.color_a.get(0) < 0) {
            return false;
        }
        try {
            try (ZipFile zipFile = new ZipFile("E:\\Downloads\\04b_30(2).zip")) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    // Check if entry is a directory
                    if (!entry.isDirectory()) {
                        try (InputStream inputStream = zipFile.getInputStream(entry)) {
                            // Read and process the entry contents using the inputStream
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
