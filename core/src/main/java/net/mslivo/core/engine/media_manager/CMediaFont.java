package net.mslivo.core.engine.media_manager;

import com.badlogic.gdx.graphics.Color;

import java.io.Serializable;
import java.util.Objects;

public final class CMediaFont extends CMedia implements Serializable {
    private final int hash;
    public final int offset_x;
    public final int offset_y;
    public final boolean markupEnabled;
    public final Color outlineColor;
    public final boolean outlineOnly;
    public final CMediaFontSymbol[] symbols;

    CMediaFont(String filename, int offset_x, int offset_y, boolean markupEnabled, Color outlineColor, boolean outlineOnly, CMediaFontSymbol[] symbols) {
        super(filename);
        this.offset_x = offset_x;
        this.offset_y = offset_y;
        this.markupEnabled = markupEnabled;
        this.outlineColor = outlineColor != null ? outlineColor.cpy() : Color.CLEAR.cpy();
        this.outlineOnly = outlineOnly;
        if(symbols != null) {
            this.symbols = new CMediaFontSymbol[symbols.length];
            for (int i = 0; i < symbols.length; i++)
                this.symbols[i] = new CMediaFontSymbol(symbols[i].id, symbols[i].file);
        }else{
            this.symbols = new CMediaFontSymbol[0];
        }
        this.hash = Objects.hash(filename, offset_x, offset_y, markupEnabled);
    }

}
