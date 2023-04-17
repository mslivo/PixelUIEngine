package org.mslivo.core.engine.ui_engine.input;

import java.util.ArrayList;

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

    public ArrayList<Character> keyTypedCharacters;

    public ArrayList<Integer> keyUpKeyCodes;

    public ArrayList<Integer> keyDownKeyCodes;

    public ArrayList<Integer> mouseUpButtons;

    public ArrayList<Integer> mouseDownButtons;

    public float mouseScrolledAmount;

    public InputEvents() {
        keyDown = false;
        keyUp = false;
        keyTyped = false;
        keyUpKeyCodes = new ArrayList<>();
        keyDownKeyCodes = new ArrayList<>();
        keyTypedCharacters = new ArrayList<>();
        mouseUpButtons = new ArrayList<>();
        mouseDownButtons = new ArrayList<>();
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

    public void reset() {
        /* Keys */
        keyDown = false;
        keyUp = false;
        keyTyped = false;
        keyUpKeyCodes.clear();
        keyDownKeyCodes.clear();
        keyTypedCharacters.clear();
        mouseUpButtons.clear();
        mouseDownButtons.clear();
        /* Mouse */
        mouseDown = false;
        mouseUp = false;
        mouseDragged = false;
        mouseScrolled = false;
        mouseMoved = false;
        mouseDoubleClick = false;
    }

}
