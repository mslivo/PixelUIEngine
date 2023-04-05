package org.mslivo.core.engine.ui_engine.input_processor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

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

    public boolean[] keysDown;

    public boolean[] mouseButtonsDown;

    public ArrayList<Character> keyTypedCharacters;

    public ArrayList<Integer> keyUpKeyCodes;

    public ArrayList<Integer> keyDownKeyCodes;

    public int mouseButton;

    public float mouseScrolledAmount;

    public InputEvents(){
        keyDown = false;
        keyUp = false;
        keyTyped = false;
        keyUpKeyCodes = new ArrayList<>();
        keyDownKeyCodes = new ArrayList<>();
        keyTypedCharacters = new ArrayList<>();
        mouseDown = false;
        mouseUp = false;
        mouseDragged = false;
        mouseMoved = false;
        mouseScrolled = false;
        mouseDoubleClick = false;
        mouseButton = -1;
        mouseScrolledAmount = -1;
        keysDown = new boolean[256];
        mouseButtonsDown = new boolean[5];
    }

    public void reset(){
        /* Keys */
        keyDown = false;
        keyUp = false;
        keyTyped = false;
        keyUpKeyCodes.clear();
        keyDownKeyCodes.clear();
        keyTypedCharacters.clear();;
        /* Mouse */
        mouseDown = false;
        mouseUp = false;
        mouseDragged = false;
        mouseScrolled = false;
        mouseMoved = false;
        mouseDoubleClick = false;
    }

}
