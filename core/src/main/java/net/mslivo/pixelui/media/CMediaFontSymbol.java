package net.mslivo.pixelui.media;

import java.io.Serializable;
import java.util.Objects;

public sealed abstract class CMediaFontSymbol implements Serializable permits CMediaFontArraySymbol, CMediaFontSingleSymbol {
    public String file;
    public int y_offset;
    public int x_advance;

    public CMediaFontSymbol(){
        super();
        this.y_offset = 0;
        this.x_advance = 0;
    }

    public CMediaFontSymbol(String file, int y_offset, int  x_advance) {
        this.file = file;
        this.y_offset = y_offset;
        this.x_advance = x_advance;
    }

    public CMediaFontSymbol copy(){
        CMediaFontSymbol copy = switch (this){
            case CMediaFontArraySymbol cMediaFontArraySymbol -> cMediaFontArraySymbol.copy();
            case CMediaFontSingleSymbol cMediaFontSingleSymbol -> cMediaFontSingleSymbol.copy();
        };
        return copy;
    }

    protected void copyFields(CMediaFontSymbol copyFrom){
        this.file = copyFrom.file;
        this.y_offset = copyFrom.y_offset;
        this.x_advance = copyFrom.x_advance;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;

        CMediaFontSymbol that = (CMediaFontSymbol) object;
        return y_offset == that.y_offset && x_advance == that.x_advance && Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(file);
        result = 31 * result + y_offset;
        result = 31 * result + x_advance;
        return result;
    }
}
