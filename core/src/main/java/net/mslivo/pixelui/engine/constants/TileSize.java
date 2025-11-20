package net.mslivo.pixelui.engine.constants;

public class TileSize {

    public final int TS;
    public final int TS_HALF;
    public final int TS_1_AND_HALF;
    public final int TS2;
    public final int TS3;
    public final int TS4;
    public final float TSF;
    public final float TLF_HALF;
    public final float TSF2;
    public final float TSF3;
    public final float TSF4;

    public int abs(int tiles) {
        return (tiles * TS);
    }

    public float absF(float tiles) {
        return (tiles * TS);
    }

    public TileSize(int tileSize) {
        this.TS = tileSize;
        this.TS_HALF = tileSize / 2;
        this.TS_1_AND_HALF = tileSize + (tileSize/2);
        this.TS2 = tileSize * 2;
        this.TS3 = tileSize * 3;
        this.TS4 = tileSize * 4;
        this.TSF = (float) tileSize;
        this.TLF_HALF = TSF / 2f;
        this.TSF2 = TSF * 2f;
        this.TSF3 = TSF * 3f;
        this.TSF4 = TSF * 4f;
    }

}
