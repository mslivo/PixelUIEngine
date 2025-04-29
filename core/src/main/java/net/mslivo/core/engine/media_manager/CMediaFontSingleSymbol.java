package net.mslivo.core.engine.media_manager;

public final class CMediaFontSingleSymbol extends CMediaFontSymbol {
    public int id;

    public CMediaFontSingleSymbol() {
    }

    public CMediaFontSingleSymbol(int id, String file) {
        this(id, file,0);
    }

    public CMediaFontSingleSymbol(int id, String file, int yoffset) {
        super(file, yoffset);
        this.id = id;
    }

    public CMediaFontSingleSymbol copy(){
        CMediaFontSingleSymbol copy = new CMediaFontSingleSymbol();
        copy.copyFields(this);
        copy.id = this.id;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CMediaFontSingleSymbol that = (CMediaFontSingleSymbol) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id;
        return result;
    }
}
