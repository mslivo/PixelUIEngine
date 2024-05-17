package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.studiohartman.jamepad.ControllerAxis;
import net.mslivo.core.engine.ui_engine.constants.INPUT_METHOD;
import net.mslivo.core.engine.ui_engine.constants.KeyCode;
import net.mslivo.core.engine.ui_engine.state.UIInputEvents;

public class UIEngineInputProcessor implements InputProcessor, ControllerListener {
    public static final int DOUBLE_CLICK_TIME = 180;
    private final UIInputEvents inputEvents;
    private long lastClickTime;
    private final boolean gamePadSupport;

    public UIEngineInputProcessor(UIInputEvents inputEvents, boolean gamePadSupport) {
        this.inputEvents = inputEvents;
        this.lastClickTime = System.currentTimeMillis();
        this.gamePadSupport = gamePadSupport;
        Gdx.input.setInputProcessor(this);
        if (gamePadSupport) {
            Controllers.addListener(this);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        this.inputEvents.keyDown = true;
        this.inputEvents.keyDownKeyCodes.add(keycode);
        this.inputEvents.keysDown[keycode] = true;
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.KEYBOARD;
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        this.inputEvents.keyUp = true;
        this.inputEvents.keyUpKeyCodes.add(keycode);
        this.inputEvents.keysDown[keycode] = false;
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.KEYBOARD;
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        this.inputEvents.keyTyped = true;
        this.inputEvents.keyTypedCharacters.add(character);
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.KEYBOARD;
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        this.inputEvents.mouseDown = true;
        this.inputEvents.mouseDownButtons.add(button);
        this.inputEvents.mouseButtonsDown[button] = true;
        if (button == 0 && (System.currentTimeMillis() - lastClickTime) < DOUBLE_CLICK_TIME) {
            this.inputEvents.mouseDoubleClick = true;
        }
        lastClickTime = System.currentTimeMillis();
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.HARDWARE_MOUSE;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        this.inputEvents.mouseUp = true;
        this.inputEvents.mouseUpButtons.add(button);
        this.inputEvents.mouseButtonsDown[button] = false;
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.HARDWARE_MOUSE;
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        this.inputEvents.mouseDragged = true;
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.HARDWARE_MOUSE;
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        this.inputEvents.mouseMoved = true;
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.HARDWARE_MOUSE;
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        this.inputEvents.mouseScrolled = true;
        this.inputEvents.mouseScrolledAmount = amountY;
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.HARDWARE_MOUSE;
        return false;
    }


    @Override
    public void connected(Controller controller) {
        if (!gamePadSupport) return;
        this.inputEvents.gamePadConnected = true;
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.GAMEPAD;
    }

    @Override
    public void disconnected(Controller controller) {
        if (!gamePadSupport) return;
        this.inputEvents.gamePadDisconnected = true;
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.GAMEPAD;
    }

    @Override
    public boolean buttonDown(Controller controller, int button) {
        if (!gamePadSupport) return false;
        int keyCode = gamePadMapButtonToKeyCode(controller.getMapping(), button);
        this.inputEvents.gamePadButtonDown = true;
        this.inputEvents.gamePadButtonDownKeyCodes.add(keyCode);
        this.inputEvents.gamePadButtonsDown[keyCode] = true;
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.GAMEPAD;
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int button) {
        if (!gamePadSupport) return false;
        int keyCode = gamePadMapButtonToKeyCode(controller.getMapping(), button);
        this.inputEvents.gamePadButtonUp = true;
        this.inputEvents.gamePadButtonUpKeyCodes.add(keyCode);
        this.inputEvents.gamePadButtonsDown[keyCode] = false;
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.GAMEPAD;
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axis, float amount) {
        if (!gamePadSupport) return false;
        ControllerAxis controllerAxis = axis < ControllerAxis.values().length ? ControllerAxis.values()[axis] : null;
        if (controllerAxis != null) {
            switch (controllerAxis) {
                case LEFTX -> {
                    this.inputEvents.gamePadLeftXMoved = true;
                    this.inputEvents.gamePadLeftX = amount;
                }
                case LEFTY -> {
                    this.inputEvents.gamePadLeftYMoved = true;
                    this.inputEvents.gamePadLeftY = -amount;
                }
                case RIGHTX -> {
                    this.inputEvents.gamePadRightXMoved = true;
                    this.inputEvents.gamePadRightX = amount;
                }
                case RIGHTY -> {
                    this.inputEvents.gamePadRightYMoved = true;
                    this.inputEvents.gamePadRightY = -amount;
                }
                case TRIGGERLEFT -> {
                    this.inputEvents.gamePadLeftTriggerMoved = true;
                    this.inputEvents.gamePadLeftTrigger = amount;
                }
                case TRIGGERRIGHT -> {
                    this.inputEvents.gamePadRightTriggerMoved = true;
                    this.inputEvents.gamePadRightTrigger = amount;
                }
            }
        }
        this.inputEvents.lastUsedInputMethod = INPUT_METHOD.GAMEPAD;
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
        } else if (button == 5) {
            return KeyCode.GamePad.HOME;
        } else {
            return KeyCode.GamePad.UNKNOWN;
        }
    }

}
