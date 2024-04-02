package net.mslivo.core.engine.ui_engine.ui.components.canvas;

import net.mslivo.core.engine.media_manager.media.CMediaSprite;

public class CanvasImage {
    public CMediaSprite image;
    public int x;
    public int y;
    public String name;
    public Object data;
    public float color_r, color_g, color_b, color_a;
    public int arrayIndex;
    public boolean fadeOut;
    public int fadeOutTime;
    public long timer;
    public Canvas addedToCanvas;
}
