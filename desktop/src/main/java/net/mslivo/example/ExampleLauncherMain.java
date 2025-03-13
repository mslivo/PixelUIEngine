package net.mslivo.example;


import net.mslivo.core.engine.tools.PixelUILaunchConfig;
import net.mslivo.core.engine.tools.Tools;

public class ExampleLauncherMain {

    public static void main(String[] args) {
        Tools.App.launch(
                new ExampleMain(),
                new PixelUILaunchConfig(
                        ExampleMainConstants.APP_TITLE,
                        ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH,
                        ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT, null,
                        PixelUILaunchConfig.GLEmulation.GL32_VULKAN,
                        PixelUILaunchConfig.GLEmulation.GL32_VULKAN,
                        PixelUILaunchConfig.GLEmulation.GL32_OPENGL,
                        0, false
                )
                );
    }
}
