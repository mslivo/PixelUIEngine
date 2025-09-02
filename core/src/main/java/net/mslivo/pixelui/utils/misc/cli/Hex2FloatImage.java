package net.mslivo.pixelui.utils.misc.cli;

import javax.imageio.ImageIO;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;

public class Hex2FloatImage implements ClipboardOwner {

    public static void main(String[] args) throws Exception {

        new Hex2FloatImage().hex2FloatImage("D:\\Code\\waves\\dev\\watercolor.png");

    }

    public void hex2FloatImage(String path) throws Exception {
        BufferedImage image = ImageIO.read(new File(path));

        boolean color = true;
        int skip = 0;
        System.out.println("public enum WATER_COLOR {");

            int value = 1;
        for (int x = 0; x < image.getWidth(); x++) {
            int c = image.getRGB(x, 0);
            int red = (c & 0x00ff0000) >> 16;
            int green = (c & 0x0000ff00) >> 8;
            int blue = c & 0x000000ff;
            int alpha = (c >> 24) & 0xff;


            System.out.print("VALUE_"+(value++)+"("+(red/256f)+"f,"+(green/256f)+"f,"+(blue/256f)+"f,"+(alpha/256f)+"f)");

            if(x != image.getWidth()-1) System.out.print(",");
            skip++;
            if(skip >= 10){
                System.out.println();
                skip = 0;
            }
        }

        System.out.println(";");
        System.out.println("");
        System.out.println("");
        System.out.println("public final float r,g,b,a;");
        System.out.println("");
        System.out.println("WATER_COLOR(float r,float g, float b, float a){");
        System.out.println("        this.r = r;");
        System.out.println("        this.g = g;");
        System.out.println("        this.b = b;");
        System.out.println("        this.a = a;");
        System.out.println("}");

        System.out.println("}");



    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {

    }
}
