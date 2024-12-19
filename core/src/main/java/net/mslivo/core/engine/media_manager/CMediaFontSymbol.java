package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public sealed abstract class CMediaFontSymbol implements Serializable permits CMediaFontArraySymbol, CMediaFontSingleSymbol {
    public String file;

    public CMediaFontSymbol(){
    }

    public CMediaFontSymbol(String file) {
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        CMediaFontSymbol symbol = (CMediaFontSymbol) o;
        return Objects.equals(file, symbol.file);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(file);
    }
}
