package net.mslivo.core.engine.media_manager;

import java.io.Serializable;
import java.util.Objects;

public final class CMediaFontSymbol implements Serializable {
    public int id;
    public String file;

    public CMediaFontSymbol(){
    }

    public CMediaFontSymbol(int id, String file) {
        this.id = id;
        this.file = file;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        CMediaFontSymbol symbol = (CMediaFontSymbol) o;
        return id == symbol.id && Objects.equals(file, symbol.file);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + Objects.hashCode(file);
        return result;
    }
}
