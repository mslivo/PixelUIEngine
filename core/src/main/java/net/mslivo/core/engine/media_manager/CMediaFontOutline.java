package net.mslivo.core.engine.media_manager;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.ui_engine.ui.components.combobox.Combobox;

import java.util.Objects;

public class CMediaFontOutline {
    public Color color;
    public int directions;
    public boolean withSymbols;
    public boolean outlineOnly;

    public CMediaFontOutline(){
    }

    public CMediaFontOutline(CMediaFontOutline other) {
        this.color = new Color(other.color);
        this.directions = other.directions;
        this.withSymbols = other.withSymbols;
        this.outlineOnly = other.outlineOnly;
    }

    public CMediaFontOutline(Color color, int directions, boolean withSymbols, boolean outlineOnly) {
        this.color = new Color(color);
        this.directions = directions;
        this.withSymbols = withSymbols;
        this.outlineOnly = outlineOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        CMediaFontOutline that = (CMediaFontOutline) o;
        return directions == that.directions && withSymbols == that.withSymbols && outlineOnly == that.outlineOnly && Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(color);
        result = 31 * result + directions;
        result = 31 * result + Boolean.hashCode(withSymbols);
        result = 31 * result + Boolean.hashCode(outlineOnly);
        return result;
    }
}