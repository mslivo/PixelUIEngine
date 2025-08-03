package net.mslivo.example;


import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.tools.PixelUILaunchConfig;
import net.mslivo.core.engine.tools.Tools;

public class ExampleLauncherMain {

    public static void main(String[] args) {

        for(int i=0;i<1000;i++){
            MathUtils.random(0.5f,2f);
        }

        for(int i=0;i < 186;i++){
            System.out.println(MathUtils.random(0.5f,2f));
        }


        Tools.App.launch(
                new ExampleMain(),
                new PixelUILaunchConfig(
                        ExampleMainConstants.APP_TITLE,
                        ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH,
                        ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT, null,
                        PixelUILaunchConfig.GLEmulation.GL32_VULKAN,
                        PixelUILaunchConfig.GLEmulation.GL32_VULKAN,
                        PixelUILaunchConfig.GLEmulation.GL32_OPENGL,
                        60, false
                )
        );
    }
}
