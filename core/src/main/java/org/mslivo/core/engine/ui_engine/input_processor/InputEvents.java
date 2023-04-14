package org.mslivo.core.engine.ui_engine.input_processor;

public class InputEvents {

    public boolean keyDown;

    public boolean keyUp;

    public boolean keyTyped;

    public boolean mouseDown;

    public boolean mouseDoubleClick;

    public boolean mouseUp;

    public boolean mouseDragged;

    public boolean mouseMoved;

    public boolean mouseScrolled;

    public final boolean[] keysDown;

    public final boolean[] mouseButtonsDown;

    public char keyTypedCharacter;

    public int keyUpKeyCode;

    public int keyDownKeyCode;

    public int mouseUpButton;

    public int mouseDownButton;

    public float mouseScrolledAmount;

    public InputEvents(){
        keyDown = false;
        keyUp = false;
        keyTyped = false;
        keyUpKeyCode = -1;
        keyDownKeyCode = -1;
        keyTypedCharacter = ' ';
        mouseUpButton = -1;
        mouseDownButton = -1;
        mouseDown = false;
        mouseUp = false;
        mouseDragged = false;
        mouseMoved = false;
        mouseScrolled = false;
        mouseDoubleClick = false;
        mouseScrolledAmount = -1;
        keysDown = new boolean[256];
        mouseButtonsDown = new boolean[5];
    }

    public void reset(){
        /* Keys */
        keyDown = false;
        keyUp = false;
        keyTyped = false;
        keyUpKeyCode = '\0';
        keyDownKeyCode = -1;
        keyUpKeyCode = -1;
        /* Mouse */
        mouseDown = false;
        mouseUp = false;
        mouseDragged = false;
        mouseScrolled = false;
        mouseMoved = false;
        mouseDoubleClick = false;
    }

}
