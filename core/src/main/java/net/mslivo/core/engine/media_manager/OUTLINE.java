package net.mslivo.core.engine.media_manager;

public class OUTLINE {
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;
    public static final int LEFT_UP = 16;
    public static final int RIGHT_UP = 32;
    public static final int RIGHT_DOWN = 64;
    public static final int LEFT_DOWN = 128;

    public static final int ALL = UP | DOWN | LEFT | RIGHT  | LEFT_UP | RIGHT_UP | RIGHT_DOWN | LEFT_DOWN;
    public static final int SHADOW = RIGHT | RIGHT_DOWN | DOWN;
}
