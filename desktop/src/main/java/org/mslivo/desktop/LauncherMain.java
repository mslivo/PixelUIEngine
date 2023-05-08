package org.mslivo.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.ui_engine.misc.ViewportMode;
import org.mslivo.core.example.ExampleMain;
import org.mslivo.core.example.ExampleStartParameters;
import org.mslivo.core.example.data.ExampleData;

import java.nio.file.Path;

public class LauncherMain {

    public static ExampleData createTestData() {
        ExampleData exampleData = new ExampleData();
        return exampleData;
    }

    private static final String appTitle = "Engine Example";

    public static void main(String[] args) throws Exception {

        // Create Data
        Tools.logInProgress("Generating DataFile");
        ExampleData exampleData = createTestData();
        Tools.logDone();

        // Save to AppDAta
        Path testDataFile = Path.of(System.getProperty("user.home") + "/" + appTitle + "/save/test.data");
        Tools.logInProgress("Writing DataFile");
        Tools.File.writeObjectToFile(exampleData, testDataFile);
        Tools.logDone();

        // Start Engine

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        ExampleStartParameters exampleStartParameters = new ExampleStartParameters();
        exampleStartParameters.dataFile = testDataFile.toAbsolutePath().toString();
        exampleStartParameters.internalResolutionWidth = 160;
        exampleStartParameters.internalResolutionHeight = 144;
        // List of 16:9 resolutions
        // 256x144, 384x216, 512x288, 640x360, 768x432
        exampleStartParameters.viewportMode = ViewportMode.PIXEL_PERFECT;

        config.setResizable(true);
        config.setWindowedMode(exampleStartParameters.internalResolutionWidth, exampleStartParameters.internalResolutionHeight);
        config.setTitle(appTitle);
        config.setDecorated(true);
        config.setMaximized(true);
        config.setWindowPosition(-1, -1);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);

        new Lwjgl3Application(new ExampleMain(exampleStartParameters), config);

    }
}
