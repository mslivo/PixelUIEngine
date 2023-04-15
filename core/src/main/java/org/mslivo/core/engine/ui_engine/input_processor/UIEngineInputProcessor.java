package org.mslivo.core.engine.ui_engine.input_processor;

import com.badlogic.gdx.InputProcessor;

public class UIEngineInputProcessor implements InputProcessor {

    public static final long DOUBLECLICK_TIME_MS = 180;
    private final InputEvents inputEvents;

    private long lastClickTime;

    public UIEngineInputProcessor(InputEvents inputEvents) {
        this.inputEvents = inputEvents;
        this.lastClickTime = System.currentTimeMillis();
    }

    @Override
    public boolean keyDown(int keycode) {
        this.inputEvents.keyDown = true;
        this.inputEvents.keyDownKeyCodes.add(keycode);
        this.inputEvents.keysDown[keycode] = true;
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        this.inputEvents.keyUp = true;
        this.inputEvents.keyUpKeyCodes.add(keycode);
        this.inputEvents.keysDown[keycode] = false;
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        this.inputEvents.keyTyped = true;
        this.inputEvents.keyTypedCharacters.add(character);
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        this.inputEvents.mouseDown = true;
        this.inputEvents.mouseDownButtons.add(button);
        this.inputEvents.mouseButtonsDown[button] = true;
        if (button == 0 && (System.currentTimeMillis() - lastClickTime) < DOUBLECLICK_TIME_MS) {
            this.inputEvents.mouseDoubleClick = true;
        }
        lastClickTime = System.currentTimeMillis();
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        this.inputEvents.mouseUp = true;
        this.inputEvents.mouseUpButtons.add(button);
        this.inputEvents.mouseButtonsDown[button] = false;
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
