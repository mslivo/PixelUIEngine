package net.mslivo.core.engine.tools.cli;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Hex2FloatImage {

    public static void main(String[] args) throws Exception {

        hex2FloatImage("D:\\Code\\waves\\assets\\sprites\\gradient.png");

    }

    public static void hex2FloatImage(String path) throws Exception {
        BufferedImage image = ImageIO.read(new File(path));

        boolean color = true;
        int skip = 0;
        if(color) {
            System.out.println("public static final Color[] VALUES = new Color[]{");
        }else{
            System.out.println("public static final String[] VALUES = new String[]{");
        }
        for (int x = 0; x < image.getWidth(); x++) {
            int c = image.getRGB(x, 0);
            int red = (c & 0x00ff0000) >> 16;
            int green = (c & 0x0000ff00) >> 8;
            int blue = c & 0x000000ff;
            String hex;
            if(color){
                hex = "Color.valueOf(\""+String.format("#%02x%02x%02x)", red, green, blue)+"\")";
            }else{
                hex = "\""+String.format("#%02x%02x%02x", red, green, blue)+"\"";
            }

            System.out.print(hex);
            if(x != image.getWidth()-1) System.out.print(",");
            skip++;
            if(skip >= 10){
                System.out.println();
                skip = 0;
            }
        }
        System.out.println("};");


    }

}
