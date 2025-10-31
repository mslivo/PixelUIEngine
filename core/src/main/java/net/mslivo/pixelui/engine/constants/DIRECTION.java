package net.mslivo.pixelui.engine.constants;

import net.mslivo.pixelui.engine.UIEngine;

public enum DIRECTION {
    UP("Up",0,1),
    DOWN("Down",0,-1),
    LEFT("Left",-1,0),
    RIGHT("Right",1,0),
    NONE("None",0,0);

    public final String text;
    public final int dx;
    public final int dy;
    public static final DIRECTION[] VALUES = DIRECTION.values();

    DIRECTION(String text, int dx, int dy) {
        this.text = text;
        this.dx = dx;
        this.dy = dy;
    }

    public DIRECTION flipX() {
        return switch (this) {
            case UP -> UP;
            case DOWN -> DOWN;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case NONE -> NONE;
        };
    }

    public DIRECTION flipY() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> LEFT;
            case RIGHT -> RIGHT;
            case NONE -> NONE;
        };
    }

    public DIRECTION turnLeft() {
        return switch (this) {
            case UP -> LEFT;
            case DOWN -> RIGHT;
            case LEFT -> DOWN;
            case RIGHT -> UP;
            case NONE -> NONE;
        };
    }

    public DIRECTION turnRight() {
        return switch (this) {
            case UP -> RIGHT;
            case DOWN -> LEFT;
            case LEFT -> UP;
            case RIGHT -> DOWN;
            case NONE -> NONE;
        };
    }

}
