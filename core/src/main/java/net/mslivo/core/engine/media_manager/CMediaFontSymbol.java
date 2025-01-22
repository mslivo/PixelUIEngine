package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public sealed abstract class CMediaFontSymbol implements Serializable permits CMediaFontArraySymbol, CMediaFontSingleSymbol {
    public String file;
    public int yoffset;

    public CMediaFontSymbol(){
    }

    public CMediaFontSymbol(String file, int yoffset) {
        this.file = file;
        this.yoffset = yoffset;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CMediaFontSymbol symbol = (CMediaFontSymbol) o;
        return yoffset == symbol.yoffset && Objects.equals(file, symbol.file);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(file);
        result = 31 * result + yoffset;
        return result;
    }
}
