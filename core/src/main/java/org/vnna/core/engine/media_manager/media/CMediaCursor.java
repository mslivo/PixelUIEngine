package org.vnna.core.engine.media_manager.media;

import java.io.Serializable;

public abstract class CMediaCursor extends CMedia implements Serializable {

    public CMediaCursor(String filename) {
        super(filename);
    }

}
