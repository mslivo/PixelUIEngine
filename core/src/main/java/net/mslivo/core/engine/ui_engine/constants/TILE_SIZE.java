package net.mslivo.core.engine.ui_engine.constants;

public enum TILE_SIZE {
    MODE_8x8(8);

    public final int TL;
    public final int TL_HALF;
    public final int TL2;
    public final int TL3;
    public final int TL4;
    public final float TLF;
    public final float TLF_HALF;
    public final float TLF2;
    public final float TLF3;
    public final float TLF4;

    public int TL(int size){
        return (size*TL);
    }

    public float TLF(float size){
        return (size*TL);
    }

    TILE_SIZE(int tileSize) {
        this.TL = tileSize;
        this.TL_HALF = tileSize / 2;
        this.TL2 = tileSize * 2;
        this.TL3 = tileSize * 3;
        this.TL4 = tileSize * 4;
        this.TLF = (float) tileSize;
        this.TLF_HALF = TLF / 2f;
        this.TLF2 = TLF * 2f;
        this.TLF3 = TLF * 3f;
        this.TLF4 = TLF * 4f;
    }
}
