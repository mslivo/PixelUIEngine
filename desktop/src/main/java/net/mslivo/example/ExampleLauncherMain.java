package net.mslivo.example;



import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.tools.particle_new.ParticleSystem;

public class ExampleLauncherMain {

    public static void main(String[] args) {


        Tools.App.launch(
                new ExampleMain(),
                ExampleMainConstants.APP_TITLE,
                ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH,
                ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT, null,60, false);


    }
}
