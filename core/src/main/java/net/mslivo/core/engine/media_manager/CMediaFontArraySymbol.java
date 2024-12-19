package net.mslivo.core.engine.media_manager;

import java.util.Arrays;

public final class CMediaFontArraySymbol extends CMediaFontSymbol {
    public int[] ids;
    public int regionWidth;
    public int regionHeight;
    public int frameOffset;
    public int frameLength;

    public CMediaFontArraySymbol() {
    }

    public CMediaFontArraySymbol(int[] ids, String file, int regionWidth, int regionHeight) {
        this(ids, file,regionWidth,regionHeight, 0, Integer.MAX_VALUE);
    }

    public CMediaFontArraySymbol(int[] ids, String file, int regionWidth, int regionHeight, int frameOffset, int frameLength) {
        super(file);
        if(ids != null){
            this.ids = new int[ids.length];
            System.arraycopy(ids,0,this.ids,0,ids.length);
        }else{
            this.ids= new int[]{};
        }
        this.regionWidth = regionWidth;
        this.regionHeight = regionHeight;
        this.frameOffset = frameOffset;
        this.frameLength = frameLength;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CMediaFontArraySymbol that = (CMediaFontArraySymbol) o;
        return regionWidth == that.regionWidth && regionHeight == that.regionHeight && frameOffset == that.frameOffset && frameLength == that.frameLength && Arrays.equals(ids, that.ids);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(ids);
        result = 31 * result + regionWidth;
        result = 31 * result + regionHeight;
        result = 31 * result + frameOffset;
        result = 31 * result + frameLength;
        return result;
    }
}
