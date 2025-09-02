package net.mslivo.pixelui.media;

import java.util.Arrays;

public final class CMediaFontArraySymbol extends CMediaFontSymbol {
    public int[] ids;
    public int regionWidth;
    public int regionHeight;
    public int frameOffset;
    public int frameLength;

    public CMediaFontArraySymbol() {
    }

    public CMediaFontArraySymbol(int[] ids, String file, int y_offset, int x_advance, int regionWidth, int regionHeight) {
        this(ids, file, y_offset, x_advance,regionWidth,regionHeight, 0, Integer.MAX_VALUE);
    }

    public CMediaFontArraySymbol(int[] ids, String file, int y_offset, int x_advance, int regionWidth, int regionHeight, int frameOffset, int frameLength) {
        super(file, y_offset, x_advance);
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

    public CMediaFontArraySymbol copy(){
        CMediaFontArraySymbol copy = new CMediaFontArraySymbol();
        copy.copyFields(this);
        copy.ids = new int[this.ids.length];
        for(int i=0;i<this.ids.length;i++)
            copy.ids[i] = this.ids[i];
        copy.regionWidth = this.regionWidth;
        copy.regionHeight = this.regionHeight;
        copy.frameOffset = this.frameOffset;
        copy.frameLength = this.frameLength;
        return copy;
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
