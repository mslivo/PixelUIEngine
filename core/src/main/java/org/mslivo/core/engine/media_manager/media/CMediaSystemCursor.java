package org.mslivo.core.engine.media_manager.media;

import com.badlogic.gdx.graphics.Cursor;

import java.io.Serializable;

public class CMediaSystemCursor extends CMediaCursor implements Serializable {

    public Cursor.SystemCursor systemCursor;

    public CMediaSystemCursor(Cursor.SystemCursor systemCursor) {
        super(systemCursor.name());
        this.systemCursor = systemCursor;
    }
}
