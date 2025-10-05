package net.mslivo.example;


import net.mslivo.pixelui.utils.PixelUILaunchConfig;
import net.mslivo.pixelui.utils.Tools;

public class ExampleLauncherMain {

    public static void main(String[] args) {


        PixelUILaunchConfig pixelUILaunchConfig = new PixelUILaunchConfig();
        pixelUILaunchConfig.appTile = ExampleMainConstants.APP_TITLE;
        pixelUILaunchConfig.resolutionWidth = ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH;
        pixelUILaunchConfig.resolutionHeight = ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT;

        Tools.App.launch(new ExampleMain(), pixelUILaunchConfig);
    }
}
