package org.mslivo.core.engine.ui_engine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import org.mslivo.core.engine.ui_engine.UIEngine;

public class UIEngineInputProcessor implements InputProcessor, ControllerListener {

    private final InputEvents inputEvents;

    private long lastClickTime;

    public UIEngineInputProcessor(InputEvents inputEvents) {
        this.inputEvents = inputEvents;
        this.lastClickTime = System.currentTimeMillis();
        Gdx.input.setInputProcessor(this);
        Controllers.addListener(this);
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
        if (button == 0 && (System.currentTimeMillis() - lastClickTime) < UIEngine.DOUBLECLICK_TIME_MS) {
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


    @Override
    public void connected(Controller controller) {
        this.inputEvents.gamePadConnected = true;
    }

    @Override
    public void disconnected(Controller controller) {
        this.inputEvents.gamePadDisconnected = true;
    }

    @Override
    public boolean buttonDown(Controller controller, int button) {
        int keyCode = gamePadMapButtonToKeyCode(controller.getMapping(), button);
        this.inputEvents.gamePadButtonDown = true;
        this.inputEvents.gamePadButtonDownKeyCodes.add(keyCode);
        this.inputEvents.gamePadButtonsDown[keyCode] = true;
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int button) {
        int keyCode = gamePadMapButtonToKeyCode(controller.getMapping(), button);
        this.inputEvents.gamePadButtonUp = true;
        this.inputEvents.gamePadButtonUpKeyCodes.add(keyCode);
        this.inputEvents.gamePadButtonsDown[keyCode] = false;
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axis, float amount) {
        ControllerMapping mapping = controller.getMapping();

        if(axis == mapping.axisLeftX){
            this.inputEvents.gamePadLeftXMoved = true;
            this.inputEvents.gamePadLeftX = amount;
        }else if(axis == mapping.axisLeftY){
            this.inputEvents.gamePadLeftYMoved = true;
            this.inputEvents.gamePadLeftY = -amount;
        }else if(axis == mapping.axisRightX){
            this.inputEvents.gamePadRightXMoved = true;
            this.inputEvents.gamePadRightX = amount;
        }else if(axis == mapping.axisRightY){
            this.inputEvents.gamePadRightYMoved = true;
            this.inputEvents.gamePadRightY = -amount;
        }

        return false;
    }

    private int gamePadMapButtonToKeyCode(ControllerMapping mapping, int button) {
        if (button == mapping.buttonA) {
            return KeyCode.GamePad.A;
        } else if (button == mapping.buttonB) {
            return KeyCode.GamePad.B;
        } else if (button == mapping.buttonX) {
            return KeyCode.GamePad.X;
        } else if (button == mapping.buttonY) {
            return KeyCode.GamePad.Y;
        } else if (button == mapping.buttonL1) {
            return KeyCode.GamePad.L1;
        } else if (button == mapping.buttonL2) {
            return KeyCode.GamePad.L2;
        } else if (button == mapping.buttonR1) {
            return KeyCode.GamePad.R1;
        } else if (button == mapping.buttonR2) {
            return KeyCode.GamePad.R2;
        } else if (button == mapping.buttonDpadUp) {
            return KeyCode.GamePad.DPAD_UP;
        } else if (button == mapping.buttonDpadDown) {
            return KeyCode.GamePad.DPAD_DOWN;
        } else if (button == mapping.buttonDpadLeft) {
            return KeyCode.GamePad.DPAD_LEFT;
        } else if (button == mapping.buttonDpadRight) {
            return KeyCode.GamePad.DPAD_RIGHT;
        } else if (button == mapping.buttonLeftStick) {
            return KeyCode.GamePad.STICK_LEFT;
        } else if (button == mapping.buttonRightStick) {
            return KeyCode.GamePad.STICK_RIGHT;
        } else if (button == mapping.buttonStart) {
            return KeyCode.GamePad.START;
        } else if (button == mapping.buttonBack) {
            return KeyCode.GamePad.BACK;
        } else {
            return -1;
        }
    }

}
