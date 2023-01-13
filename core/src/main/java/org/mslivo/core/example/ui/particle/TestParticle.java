package org.mslivo.core.example.ui.particle;

import com.badlogic.gdx.math.MathUtils;
import org.mslivo.core.engine.tools.particlesystem.ParticleSystem;
import org.mslivo.core.engine.tools.particlesystem.particle.Particle;
import org.mslivo.core.example.ui.media.ExampleBaseMedia;

public class TestParticle extends Particle {

    private int timer;
    public TestParticle(float x, float y, int imageIndex) {
        super(switch (imageIndex){
            case 1 -> ExampleBaseMedia.GUI_ICON_EXAMPLE_1;
            case 2 -> ExampleBaseMedia.GUI_ICON_EXAMPLE_2;
            case 3 -> ExampleBaseMedia.GUI_ICON_EXAMPLE_3;
            case 4 -> ExampleBaseMedia.GUI_ICON_EXAMPLE_4;
            default -> ExampleBaseMedia.GUI_ICON_EXAMPLE_1;
        }, x, y);
        this.timer = 0;
    }

    @Override
    public boolean update(ParticleSystem particleSystem, Particle particle, int index) {
        TestParticle testParticle = (TestParticle)particle;
        testParticle.x += MathUtils.random(-2f, 2f);
        testParticle.y += MathUtils.random(-2f, 2f);
        testParticle.timer++;
        if(testParticle.timer > 100){
            return false;
        }
        return true;
    }
}
