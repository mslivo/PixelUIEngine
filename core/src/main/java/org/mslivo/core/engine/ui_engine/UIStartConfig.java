package org.mslivo.core.engine.ui_engine;

import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.ui_engine.enums.VIEWPORT_MODE;

public class UIStartConfig implements Cloneable {
    public int resolutionWidth, resolutionHeight;
    public VIEWPORT_MODE viewportMode;
    public boolean gamePadSupport;
    public int uiScale;
    public boolean spriteRenderer;
    public boolean immediateRenderer;

    public UIStartConfig(int resolutionWidth, int resolutionHeight) {
        this(resolutionWidth, resolutionHeight, VIEWPORT_MODE.PIXEL_PERFECT, true, 1, true, true);
    }
    public UIStartConfig(int resolutionWidth, int resolutionHeight, VIEWPORT_MODE viewportMode) {
        this(resolutionWidth, resolutionHeight, viewportMode, true, 1, true, true);
    }

    public UIStartConfig(int resolutionWidth, int resolutionHeight, VIEWPORT_MODE viewportMode, boolean gamePadSupport) {
        this(resolutionWidth, resolutionHeight, viewportMode, gamePadSupport, 1, true, true);
    }

    public UIStartConfig(int resolutionWidth, int resolutionHeight, VIEWPORT_MODE viewportMode, boolean gamePadSupport, int uiScale) {
        this(resolutionWidth, resolutionHeight, viewportMode, gamePadSupport, uiScale, true, true);
    }

    public UIStartConfig(int resolutionWidth, int resolutionHeight, VIEWPORT_MODE viewportMode, boolean gamePadSupport, int uiScale, boolean spriteRenderer, boolean immediateRenderer) {
        this.resolutionWidth = resolutionWidth;
        this.resolutionHeight = resolutionHeight;
        this.viewportMode = viewportMode;
        this.gamePadSupport = gamePadSupport;
        this.uiScale = Tools.Calc.inBounds(uiScale,1,10);
        this.spriteRenderer = spriteRenderer;
        this.immediateRenderer = immediateRenderer;
    }

    public UIStartConfig clone()  {
        try {
            return (UIStartConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
