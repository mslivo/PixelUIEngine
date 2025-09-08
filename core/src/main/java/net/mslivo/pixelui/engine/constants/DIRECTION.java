package net.mslivo.pixelui.engine.constants;

public enum DIRECTION {
    UP("Up",0,1), DOWN("Down",0,-1), LEFT("Left",-1,0), RIGHT("Right",1,0);

    public final String text;
    public final int dx;
    public final int dy;

    DIRECTION(String text, int dx, int dy) {
        this.text = text;
        this.dx = dx;
        this.dy = dy;
    }

    public DIRECTION turnLeft() {
        return switch (this) {
            case UP -> LEFT;
            case DOWN -> RIGHT;
            case LEFT -> DOWN;
            case RIGHT -> UP;
        };
    }

    public DIRECTION turnRight() {
        return switch (this) {
            case UP -> RIGHT;
            case DOWN -> LEFT;
            case LEFT -> UP;
            case RIGHT -> DOWN;
        };
    }

}
