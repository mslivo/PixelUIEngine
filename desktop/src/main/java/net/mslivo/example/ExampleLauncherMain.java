package net.mslivo.example;


import com.monstrous.gdx.webgpu.backends.desktop.WgDesktopApplication;
import com.monstrous.gdx.webgpu.backends.desktop.WgDesktopApplicationConfiguration;
import net.mslivo.pixelui.utils.PixelUILaunchConfig;
import net.mslivo.pixelui.utils.Tools;

public class ExampleLauncherMain {

    static void main(String[] args) {

        WgDesktopApplicationConfiguration config = new WgDesktopApplicationConfiguration();
        config.setTitle(ExampleMainConstants.APP_TITLE);
        config.setWindowSizeLimits(ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH,ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT,-1,-1);
        config.useVsync(false);

        new WgDesktopApplication(new ExampleMain(),config);
    }
}
