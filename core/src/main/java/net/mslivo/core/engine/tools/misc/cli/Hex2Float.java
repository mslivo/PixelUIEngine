package net.mslivo.core.engine.tools.misc.cli;


/**
 * Created by Admin on 08.03.2019.
 */
public class Hex2Float {

    public static void main(String[] args){
        final String arg = args.length >= 1 ? args[0] : "#333454";
        System.out.println(hex2Float(arg));
    }

    public static String hex2Float(String hex){
        if(hex.startsWith("#")) hex = hex.substring(1);
        byte[] bytes = toByteArray(hex);
        float c1 = Integer.parseInt(hex.substring(0,2),16)/255f;
        float c2 = Integer.parseInt(hex.substring(2,4),16)/255f;
        float c3 = Integer.parseInt(hex.substring(4,6),16)/255f;
        return c1+"f,"+c2+"f,"+c3+"f,1f";

    }

    public static byte[] toByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

}
