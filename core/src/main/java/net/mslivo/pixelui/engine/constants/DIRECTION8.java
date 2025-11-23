package net.mslivo.pixelui.engine.constants;

public enum DIRECTION8 {
    UP("Up", 0, 1),
    UP_LEFT("Up Left", -1, 1),
    UP_RIGHT("Up Right", 1, 1),
    DOWN("Down", 0, -1),
    DOWN_LEFT("Down Left", -1, -1),
    DOWN_RIGHT("Down Right", 1, -1),
    LEFT("Left", -1, 0),
    RIGHT("Right", 1, 0),

    NONE("None", 0, 0);

    public final String text;
    public final int dx;
    public final int dy;
    public static final DIRECTION8[] VALUES = DIRECTION8.values();

    DIRECTION8(String text, int dx, int dy) {
        this.text = text;
        this.dx = dx;
        this.dy = dy;
    }

    public DIRECTION8 flipX() {
        return switch (this) {
            case UP -> UP;
            case UP_LEFT -> UP_RIGHT;
            case UP_RIGHT -> UP_LEFT;
            case DOWN -> DOWN;
            case DOWN_LEFT -> DOWN_RIGHT;
            case DOWN_RIGHT -> DOWN_LEFT;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case NONE -> NONE;
        };
    }

    public DIRECTION8 flipY() {
        return switch (this) {
            case UP -> DOWN;
            case UP_LEFT -> DOWN_LEFT;
            case UP_RIGHT -> DOWN_RIGHT;
            case DOWN -> UP;
            case DOWN_LEFT -> UP_LEFT;
            case DOWN_RIGHT -> UP_RIGHT;
            case LEFT -> LEFT;
            case RIGHT -> RIGHT;
            case NONE -> NONE;
        };
    }

    public DIRECTION8 turnLeft() {
        return switch (this) {
            case UP -> UP_LEFT;
            case UP_LEFT -> LEFT;
            case LEFT -> DOWN_LEFT;
            case DOWN_LEFT -> DOWN;
            case DOWN -> DOWN_RIGHT;
            case DOWN_RIGHT -> RIGHT;
            case RIGHT -> UP_RIGHT;
            case UP_RIGHT -> UP;
            case NONE -> NONE;
        };
    }

    public DIRECTION8 turnRight() {
        return switch (this) {
            case UP -> UP_RIGHT;
            case UP_RIGHT -> RIGHT;
            case RIGHT -> DOWN_RIGHT;
            case DOWN_RIGHT -> DOWN;
            case DOWN -> DOWN_LEFT;
            case DOWN_LEFT -> LEFT;
            case LEFT -> UP_LEFT;
            case UP_LEFT -> UP;
            case NONE -> NONE;
        };
    }

}
