package org.mslivo.core.engine.tools.colorhex2float;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Hex2FloatImage {

    public static void main(String[] args) throws Exception {

        hex2FloatImage("E:\\Code\\gradient.png");

    }

    public static void hex2FloatImage(String path) throws Exception {
        BufferedImage image = ImageIO.read(new File(path));
        for (int x = 0; x < image.getWidth(); x++) {
            int c = image.getRGB(x, 0);
            int red = (c & 0x00ff0000) >> 16;
            int green = (c & 0x0000ff00) >> 8;
            int blue = c & 0x000000ff;
            String hex = String.format("#%02x%02x%02x", red, green, blue);
            System.out.println("\"" + hex + "\",");
        }


    }

}
