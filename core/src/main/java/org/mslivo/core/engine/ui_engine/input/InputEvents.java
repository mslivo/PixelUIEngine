package org.mslivo.core.engine.ui_engine.input;

import com.badlogic.gdx.utils.IntArray;

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
    public final IntArray mouseUpButtons;
    public final IntArray mouseDownButtons;
    public float mouseScrolledAmount;
    public int mouseUpButtonIndex,mouseDownButtonIndex;
    /* --- Keyboard --- */
    public boolean keyDown;
    public boolean keyUp;
    public boolean keyTyped;
    public final boolean[] keysDown;
    public final IntArray keyTypedCharacters;
    public final IntArray keyUpKeyCodes;
    public final IntArray keyDownKeyCodes;
    public int keyTypedCharacterIndex, keyUpKeyCodeIndex, keyDownKeyCodeIndex;
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
    public boolean gamePadLeftTriggerMoved;
    public float gamePadLeftTrigger;
    public boolean gamePadRightTriggerMoved;
    public float gamePadRightTrigger;
    public boolean gamePadButtonDown;
    public boolean gamePadButtonUp;
    public IntArray gamePadButtonDownKeyCodes;
    public IntArray gamePadButtonUpKeyCodes;
    public final boolean[] gamePadButtonsDown;
    public int gamePadButtonDownIndex,gamePadButtonUpIndex;

    public InputEvents() {
        lastUsedInputMethod = InputMethod.NONE;
        // Mouse
        mouseDownButtons = new IntArray();
        mouseButtonsDown = new boolean[5];
        // Keyboard
        keyUpKeyCodes = new IntArray();
        keyDownKeyCodes = new IntArray();
        keyTypedCharacters = new IntArray();
        keysDown = new boolean[256];
        mouseUpButtons = new IntArray();
        // GamePad
        gamePadButtonDownKeyCodes = new IntArray();
        gamePadButtonUpKeyCodes = new IntArray();
        gamePadButtonsDown = new boolean[18];
        this.reset();
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
        gamePadLeftTriggerMoved = false;
        gamePadRightTriggerMoved = false;
        gamePadButtonDown = false;
        gamePadButtonUp = false;
        gamePadButtonDownKeyCodes.clear();
        gamePadButtonUpKeyCodes.clear();
        // API Return Indexes
        mouseUpButtonIndex = 0;
        mouseDownButtonIndex = 0;
        keyTypedCharacterIndex = 0;
        keyUpKeyCodeIndex = 0;
        keyDownKeyCodeIndex = 0;
        gamePadButtonDownIndex = 0;
        gamePadButtonUpIndex = 0;
    }

}
