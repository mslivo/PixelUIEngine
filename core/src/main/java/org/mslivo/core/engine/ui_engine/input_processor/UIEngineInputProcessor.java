package org.mslivo.core.engine.ui_engine.input_processor;

import com.badlogic.gdx.InputProcessor;

public class UIEngineInputProcessor implements InputProcessor {

    private InputEvents inputEvents;

    private long lastClickTime;

    public UIEngineInputProcessor(InputEvents inputEvents){
        this.inputEvents = inputEvents;
        this.lastClickTime = System.currentTimeMillis();
    }

    @Override
    public boolean keyDown(int keycode) {
        this.inputEvents.keyDown = true;
        this.inputEvents.keyCodesDown.add(keycode);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        this.inputEvents.keyUp = true;
        this.inputEvents.keyCodesUp.add(keycode);
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        this.inputEvents.keyTyped = true;
        this.inputEvents.keysTyped.add(character);
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        this.inputEvents.mouseDown = true;
        this.inputEvents.mouseButton = button;
        if(button == 0 && (System.currentTimeMillis()-lastClickTime) < 180){
            this.inputEvents.mouseDoubleClick = true;
        }
        lastClickTime = System.currentTimeMillis();
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        this.inputEvents.mouseUp = true;
        this.inputEvents.mouseButton = button;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        this.inputEvents.mouseDragged = true;
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        this.inputEvents.mouseMoved = true;
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        this.inputEvents.mouseScrolled = true;
        this.inputEvents.mouseScrolledAmount = amountY;
        return false;
    }


}
