package org.mslivo.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.example.ExampleMain;
import org.mslivo.core.example.ExampleMainConstants;

import java.nio.file.Path;

public class LauncherMain {

    private static final String appTitle = "Engine Example";

    public static void main(String[] args) {


        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();


        config.setResizable(true);
        config.setWindowedMode(ExampleMainConstants.internalResolutionWidth, ExampleMainConstants.internalResolutionHeight);
        config.setWindowSizeLimits(ExampleMainConstants.internalResolutionWidth, ExampleMainConstants.internalResolutionHeight, -1,-1);
        config.setTitle(appTitle);
        config.setDecorated(true);
        config.setMaximized(true);
        config.setForegroundFPS(60);
        config.useVsync(false);
        config.setWindowPosition(-1, -1);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);

        try {
            new Lwjgl3Application(new ExampleMain(), config);
        }catch (Exception e){
            Tools.Log.toFile(e, Path.of("exception.log"));
        }
    }
}
