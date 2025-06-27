package net.mslivo.core.engine.tools;

public record PixelUILaunchConfig(String appTile, int resolutionWidth, int resolutionHeight, String iconPath,
                                  GLEmulation windowsGLEmulation,
                                  GLEmulation linuxGLEmulation,
                                  GLEmulation macOSGLEmulation,
                                  int fps, boolean vSync) {

    public enum GLEmulation {
        GL32_VULKAN,
        GL32_OPENGL
    }

}