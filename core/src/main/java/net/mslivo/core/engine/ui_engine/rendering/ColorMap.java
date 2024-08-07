package net.mslivo.core.engine.ui_engine.rendering;

public class ColorMap {

    public final int width;
    public final int height;

    public final float r[][];
    public final float g[][];
    public final float b[][];
    public final float a[][];

    public ColorMap(int width, int height) {
        this(width, height,null);
    }

    public ColorMap(int width, int height, ColorMap copyFromMap) {
        this.width = width;
        this.height = height;
        this.r = new float[width][height];
        this.g = new float[width][height];
        this.b = new float[width][height];
        this.a = new float[width][height];
        if(copyFromMap != null){
            for(int ix=0;ix<width;ix++) {
                for (int iy = 0; iy < height; iy++) {
                    if(ix < copyFromMap.width && iy < copyFromMap.height) {
                        r[ix][iy] = copyFromMap.r[ix][iy];
                        g[ix][iy] = copyFromMap.g[ix][iy];
                        b[ix][iy] = copyFromMap.b[ix][iy];
                        a[ix][iy] = copyFromMap.a[ix][iy];
                    }
                }
            }

        }
    }
}
