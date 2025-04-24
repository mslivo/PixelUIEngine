package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public sealed abstract class CMediaFontSymbol implements Serializable permits CMediaFontArraySymbol, CMediaFontSingleSymbol {
    public String file;
    public int y_offset;

    public CMediaFontSymbol(){
    }

    public CMediaFontSymbol(CMediaFontSymbol other) {
        this.file = other.file;
        this.y_offset = other.y_offset;
    }

    public CMediaFontSymbol(String file, int y_offset) {
        this.file = file;
        this.y_offset = y_offset;
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
