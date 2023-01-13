package org.mslivo.core.engine.ui_engine.input_processor;

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

    public ArrayList<Character> keysTyped;

    public ArrayList<Integer> keyCodesUp;

    public ArrayList<Integer> keyCodesDown;

    public int mouseButton;

    public float mouseScrolledAmount;

    public InputEvents(){
        keyDown = false;
        keyUp = false;
        keyTyped = false;
        keyCodesUp = new ArrayList<>();
        keyCodesDown = new ArrayList<>();
        keysTyped = new ArrayList<>();
        mouseDown = false;
        mouseUp = false;
        mouseDragged = false;
        mouseMoved = false;
        mouseScrolled = false;
        mouseDoubleClick = false;
        mouseButton = -1;
        mouseScrolledAmount = -1;
    }

    public void reset(){
        /* Keys */
        keyDown = false;
        keyUp = false;
        keyTyped = false;
        keyCodesUp.clear();
        keyCodesDown.clear();
        keysTyped.clear();;

        /* Mouse */
        mouseDown = false;
        mouseUp = false;
        mouseDragged = false;
        mouseScrolled = false;
        mouseMoved = false;
        mouseDoubleClick = false;
    }


}
