package net.mslivo.core.engine.tools;

public class PixelUILaunchConfig {

    public enum GLEmulation {
        GL32_VULKAN,
        GL32_OPENGL
    }

    public final String appTile;
    public final int resolutionWidth;
    public final int resolutionHeight;
    public final String iconPath;
    public final GLEmulation windowsGLEmulation, linuxGLEmulation, macOSGLEmulation;
    public final int fps;
    public final boolean vSync;

    public PixelUILaunchConfig(String appTile, int resolutionWidth, int resolutionHeight, String iconPath,
                               GLEmulation windowsGLEmulation, GLEmulation linuxGLEmulation, GLEmulation macOSGLEmulation,
                               int fps, boolean vSync) {
        this.appTile = appTile;
        this.resolutionWidth = resolutionWidth;
        this.resolutionHeight = resolutionHeight;
        this.iconPath = iconPath;
        this.windowsGLEmulation = windowsGLEmulation;
        this.linuxGLEmulation = linuxGLEmulation;
        this.macOSGLEmulation = macOSGLEmulation;
        this.fps = fps;
        this.vSync = vSync;
    }
}
