package org.vnna.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.vnna.core.engine.tools.Tools;
import org.vnna.core.engine.ui_engine.misc.ViewportMode;
import org.vnna.core.example.ExampleMain;
import org.vnna.core.example.ExampleStartParameters;
import org.vnna.core.example.data.ExampleData;

import java.nio.file.Path;

public class LauncherMain {

    public static ExampleData createTestData(){
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
        Path testDataFile = Path.of(Tools.File.findAppDataDirectory() + "/" + appTitle + "/save/test.data");
        Tools.logInProgress("Writing DataFile");
        Tools.File.writeObjectToFile(exampleData, testDataFile);
        Tools.logDone();

        // Start Engine

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        ExampleStartParameters exampleStartParameters = new ExampleStartParameters();
        exampleStartParameters.dataFile = testDataFile.toAbsolutePath().toString();
        exampleStartParameters.internalResolutionWidth= 640;
        exampleStartParameters.internalResolutionHeight= 360;
        // List of 16:9 resolutions
        // 256x144, 384x216, 512x288, 640x360, 768x432
        exampleStartParameters.viewportMode = ViewportMode.FIT;
        exampleStartParameters.stretchModeUpSampling = 2;

        config.setResizable(true);
        config.setWindowedMode(exampleStartParameters.internalResolutionWidth, exampleStartParameters.internalResolutionHeight);
        config.setTitle(appTitle);
        config.setDecorated(true);
        config.setMaximized(true);
        config.setWindowPosition(-1, -1);
        config.setBackBufferConfig(8, 8, 8, 8, 32, 0, 8);

        new Lwjgl3Application(new ExampleMain(exampleStartParameters), config);

    }
}
