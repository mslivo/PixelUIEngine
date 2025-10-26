package net.mslivo.pixelui.media;

import com.badlogic.gdx.graphics.Color;

import java.util.Objects;

public class CMediaFontOutline {
    public Color color;
    public int directions;
    public boolean outlineSymbols;
    public boolean outlineOnly;

    public CMediaFontOutline(){
        super();
        this.color = new Color(0f,0f,0f,0f);
        this.directions = 0;
        this.outlineSymbols = false;
        this.outlineOnly = false;
    }

    public CMediaFontOutline(Color color, int directions, boolean outlineSymbols, boolean outlineOnly) {
        this.color = new Color(color);
        this.directions = directions;
        this.outlineSymbols = outlineSymbols;
        this.outlineOnly = outlineOnly;
    }

    public CMediaFontOutline copy(){
        CMediaFontOutline copy = new CMediaFontOutline();
        copy.color = new Color(this.color);
        copy.directions = this.directions;
        copy.outlineSymbols = this.outlineSymbols;
        copy.outlineOnly = this.outlineOnly;
        return copy;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        CMediaFontOutline that = (CMediaFontOutline) o;
        return directions == that.directions && outlineSymbols == that.outlineSymbols && outlineOnly == that.outlineOnly && Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(color);
        result = 31 * result + directions;
        result = 31 * result + Boolean.hashCode(outlineSymbols);
        result = 31 * result + Boolean.hashCode(outlineOnly);
        return result;
    }
}