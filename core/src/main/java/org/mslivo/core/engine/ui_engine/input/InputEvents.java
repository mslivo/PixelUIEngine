package org.mslivo.core.engine.ui_engine.input;

import java.util.ArrayList;

public class InputEvents {
    public InputMethod lastUsedInputMethod;
    /* --- Hardware Mouse --- */
    public boolean mouseDown;
    public boolean mouseDoubleClick;
    public boolean mouseUp;
    public boolean mouseDragged;
    public boolean mouseMoved;
    public boolean mouseScrolled;
    public final boolean[] mouseButtonsDown;
    public final ArrayList<Integer> mouseUpButtons;
    public final ArrayList<Integer> mouseDownButtons;
    public float mouseScrolledAmount;

    /* --- Keyboard --- */
    public boolean keyDown;
    public boolean keyUp;
    public boolean keyTyped;
    public final boolean[] keysDown;
    public final ArrayList<Character> keyTypedCharacters;
    public final ArrayList<Integer> keyUpKeyCodes;
    public final ArrayList<Integer> keyDownKeyCodes;


    /* --- GamePad --- */
    public boolean gamePadConnected;
    public boolean gamePadDisconnected;
    public boolean gamePadLeftXMoved;
    public boolean gamePadLeftYMoved;

    public float gamePadLeftX;
    public float gamePadLeftY;
    public boolean gamePadRightXMoved;

    public boolean gamePadRightYMoved;
    public float gamePadRightX;
    public float gamePadRightY;
    public boolean gamePadButtonDown;
    public boolean gamePadButtonUp;
    public ArrayList<Integer> gamePadButtonDownKeyCodes;
    public ArrayList<Integer> gamePadButtonUpKeyCodes;
    public final boolean[] gamePadButtonsDown;

    public InputEvents() {
        lastUsedInputMethod = InputMethod.NONE;
        // Mouse
        mouseDownButtons = new ArrayList<>();
        mouseDown = false;
        mouseUp = false;
        mouseDragged = false;
        mouseMoved = false;
        mouseScrolled = false;
        mouseDoubleClick = false;
        mouseScrolledAmount = -1;
        mouseButtonsDown = new boolean[5];
        // Keyboard
        keyDown = false;
        keyUp = false;
        keyTyped = false;
        keyUpKeyCodes = new ArrayList<>();
        keyDownKeyCodes = new ArrayList<>();
        keyTypedCharacters = new ArrayList<>();
        keysDown = new boolean[256];
        mouseUpButtons = new ArrayList<>();
        // GamePad
        gamePadConnected = false;
        gamePadDisconnected = false;
        gamePadLeftXMoved = false;
        gamePadLeftYMoved = false;
        gamePadLeftX = 0f;
        gamePadLeftY = 0f;
        gamePadRightXMoved = false;
        gamePadRightYMoved = false;
        gamePadRightX = 0f;
        gamePadRightY = 0f;
        gamePadButtonDown = false;
        gamePadButtonUp = false;
        gamePadButtonDownKeyCodes = new ArrayList<>();
        gamePadButtonUpKeyCodes = new ArrayList<>();
        gamePadButtonsDown = new boolean[18];
    }

    public void reset() {
        // Keys
        keyDown = false;
        keyUp = false;
        keyTyped = false;
        keyUpKeyCodes.clear();
        keyDownKeyCodes.clear();
        keyTypedCharacters.clear();
        mouseUpButtons.clear();
        mouseDownButtons.clear();
        // Mouse
        mouseDown = false;
        mouseUp = false;
        mouseDragged = false;
        mouseScrolled = false;
        mouseMoved = false;
        mouseDoubleClick = false;
        // GamePad
        gamePadConnected = false;
        gamePadDisconnected = false;
        gamePadLeftXMoved = false;
        gamePadLeftYMoved = false;
        gamePadRightXMoved = false;
        gamePadRightYMoved = false;
        gamePadButtonDown = false;
        gamePadButtonUp = false;
        gamePadButtonDownKeyCodes.clear();
        gamePadButtonUpKeyCodes.clear();
    }

}
