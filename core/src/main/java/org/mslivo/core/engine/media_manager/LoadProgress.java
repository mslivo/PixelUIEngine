package org.mslivo.core.engine.media_manager;

public interface LoadProgress {

    void onLoadImage(String name, int fileNr, int filesMax);

    void onPrepareMedia(String name, int fileNr, int filesMax);
}
