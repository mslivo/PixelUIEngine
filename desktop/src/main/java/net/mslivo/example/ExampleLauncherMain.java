package net.mslivo.example;


import com.badlogic.gdx.math.MathUtils;
import net.mslivo.pixelui.utils.PixelUILaunchConfig;
import net.mslivo.pixelui.utils.Tools;

public class ExampleLauncherMain {

    public static void main(String[] args) {

        int tests = 1_000_000;
        long time;

        for(int i2=0;i2<10;i2++) {
            time = System.nanoTime();
            for (int i = 0; i < tests; i++) {
                Tools.Calc.distanceFast(MathUtils.random(0, 100), MathUtils.random(0, 100), MathUtils.random(0, 100), MathUtils.random(0, 100));
            }
            System.out.println("distanceFast: "+(System.nanoTime()-time)/1000);
            time = System.nanoTime();
            for (int i = 0; i < tests; i++) {
                Tools.Calc.distance(MathUtils.random(0, 100), MathUtils.random(0, 100), MathUtils.random(0, 100), MathUtils.random(0, 100));
            }
            System.out.println("distance    : "+(System.nanoTime()-time)/1000);

        }


        PixelUILaunchConfig pixelUILaunchConfig = new PixelUILaunchConfig();
        pixelUILaunchConfig.appTile = ExampleMainConstants.APP_TITLE;
        pixelUILaunchConfig.resolutionWidth = ExampleMainConstants.INTERNAL_RESOLUTION_WIDTH;
        pixelUILaunchConfig.resolutionHeight = ExampleMainConstants.INTERNAL_RESOLUTION_HEIGHT;

        Tools.App.launch(new ExampleMain(),pixelUILaunchConfig);
    }
}
