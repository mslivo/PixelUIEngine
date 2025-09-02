package net.mslivo.pixelui.media_manager;

public interface LoadProgress {

    void onLoadStep(String name, int step, int stepsMax);

}
