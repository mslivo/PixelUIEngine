package net.mslivo.pixelui.utils;

public final class PixelUILaunchConfig {

    public String appTile = "Pixel UI Game";
    public int resolutionWidth = 320;
    public int resolutionHeight = 240;
    public String iconPath = null;
    public GLEmulation windowsGLEmulation = GLEmulation.GL32_VULKAN;
    public GLEmulation linuxGLEmulation = GLEmulation.GL32_VULKAN;
    public GLEmulation macOSGLEmulation = GLEmulation.GL32_OPENGL;
    public int fps = 60;
    public int idleFPS = 60;
    public boolean vSync = false;
    public int r,g,b,a = 8;
    public int depth = 16;
    public int stencil = 0;
    public int samples = 0;
    public boolean resizeAble = true;
    public boolean decorated = true;
    public boolean maximized = true;

    public PixelUILaunchConfig() {
    }

    public enum GLEmulation {
        GL32_VULKAN,
        GL32_OPENGL
    }

}