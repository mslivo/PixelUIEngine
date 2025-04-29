package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public sealed abstract class CMediaFontSymbol implements Serializable permits CMediaFontArraySymbol, CMediaFontSingleSymbol {
    public String file;
    public int y_offset;

    public CMediaFontSymbol(){
    }

    public CMediaFontSymbol(String file, int y_offset) {
        this.file = file;
        this.y_offset = y_offset;
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
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CMediaFontSymbol symbol = (CMediaFontSymbol) o;
        return y_offset == symbol.y_offset && Objects.equals(file, symbol.file);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(file);
        result = 31 * result + y_offset;
        return result;
    }
}
