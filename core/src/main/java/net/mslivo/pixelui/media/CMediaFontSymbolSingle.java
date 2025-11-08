package net.mslivo.pixelui.media;

import net.mslivo.pixelui.utils.misc.Copyable;

public final class CMediaFontSymbolSingle extends CMediaFontSymbol implements Copyable<CMediaFontSymbolSingle> {
    public int id;

    public CMediaFontSymbolSingle() {
        super();
        this.id = 0;
    }

    public CMediaFontSymbolSingle(int id, String file) {
        this(id, file,0,0);
    }

    public CMediaFontSymbolSingle(int id, String file, int y_offset) {
        this(id, file,y_offset,0);
    }

    public CMediaFontSymbolSingle(int id, String file, int y_offset, int x_advance) {
        super(file, y_offset, x_advance);
        this.id = id;
    }

    @Override
    public CMediaFontSymbolSingle copy(){
        CMediaFontSymbolSingle copy = new CMediaFontSymbolSingle();
        copy.copyFields(this);
        copy.id = this.id;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CMediaFontSymbolSingle that = (CMediaFontSymbolSingle) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id;
        return result;
    }
}
