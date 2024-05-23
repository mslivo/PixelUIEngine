package net.mslivo.core.engine.ui_engine;

import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.ui_engine.constants.INPUT_METHOD;
import net.mslivo.core.engine.ui_engine.constants.KeyCode;
import net.mslivo.core.engine.ui_engine.constants.MOUSE_CONTROL_MODE;
import net.mslivo.core.engine.ui_engine.state.UIConfig;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.Window;
import net.mslivo.core.engine.ui_engine.ui.components.Component;
import net.mslivo.core.engine.ui_engine.ui.hotkeys.HotKey;

public final class APIInput {

    private final API api;
    private final UIEngineState uiEngineState;
    private final UIConfig uiConfig;
    private final MediaManager mediaManager;
    public final APIMouse mouse;
    public final APIKeyboard keyboard;
    public final APIGamepad gamepad;

    APIInput(API api, UIEngineState uiEngineState, MediaManager mediaManager){
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.uiConfig = uiEngineState.uiEngineConfig;
        this.mediaManager = mediaManager;
        this.mouse = new APIMouse();
        this.keyboard = new APIKeyboard();
        this.gamepad = new APIGamepad();
    }

    public INPUT_METHOD lastUsedInputMethod() {
        return uiEngineState.inputEvents.lastUsedInputMethod;
    }

    public final class APIMouse {
        public final APIEvent event;
        public final APIState state;
        public final APIEmulated emulated;

        APIMouse(){
            event = new APIEvent();
            state = new APIState();
            emulated = new APIEmulated();
        }

        public final class APIEmulated {

            public void setPosition(int x, int y) {
                UICommonUtils.emulatedMouse_setPosition(uiEngineState, x, y);
            }

            public void setPositionComponent(Component component) {
                UICommonUtils.emulatedMouse_setPositionComponent(uiEngineState, component);
            }

            public void setPositionNextComponent() {
                UICommonUtils.emulatedMouse_setPositionNextComponent(uiEngineState, false);
            }

            public void setPositionPreviousComponent() {
                UICommonUtils.emulatedMouse_setPositionNextComponent(uiEngineState, true);
            }

        }

        private String mouseUIObjectName(Object mouseObject) {
            if (mouseObject != null) {
                if (mouseObject instanceof Component component) {
                    return component.name;
                } else if (mouseObject instanceof Window window) {
                    return window.name;
                }
            }
            return "";
        }

        public MOUSE_CONTROL_MODE currentControlMode() {
            return uiEngineState.currentControlMode;
        }

        public Object hoverUIObject() {
            return uiEngineState.lastUIMouseHover;
        }

        public boolean isHoveringOverUIObject() {
            return hoverUIObject() != null;
        }

        public String hoverUIObjectName() {
            return mouseUIObjectName(hoverUIObject());
        }

        public Object useUIObject() {
            return uiEngineState.mouseInteractedUIObjectFrame != null ? uiEngineState.mouseInteractedUIObjectFrame : null;
        }

        public boolean isUsingUIObject() {
            return useUIObject() != null;
        }

        public String useUIObjectName() {
            return mouseUIObjectName(useUIObject());
        }

        public final class APIEvent {
            /* ---- MOUSE EVENTS --- */
            public boolean buttonDown() {
                return uiEngineState.inputEvents.mouseDown;
            }

            public boolean doubleClick() {
                return uiEngineState.inputEvents.mouseDoubleClick;
            }

            public boolean buttonUp() {
                return uiEngineState.inputEvents.mouseUp;
            }

            public boolean dragged() {
                return uiEngineState.inputEvents.mouseDragged;
            }

            public boolean moved() {
                return uiEngineState.inputEvents.mouseMoved;
            }

            public boolean scrolled() {
                return uiEngineState.inputEvents.mouseScrolled;
            }

            public float scrolledAmount() {
                return uiEngineState.inputEvents.mouseScrolledAmount;
            }

            public boolean buttonUpHasNext() {
                return uiEngineState.inputEvents.mouseUpButtonIndex < uiEngineState.inputEvents.mouseUpButtons.size;
            }

            public int buttonUpNext() {
                return buttonUpHasNext() ? uiEngineState.inputEvents.mouseUpButtons.get(uiEngineState.inputEvents.mouseUpButtonIndex++) : KeyCode.NONE;
            }

            public boolean buttonDownHasNext() {
                return uiEngineState.inputEvents.mouseDownButtonIndex < uiEngineState.inputEvents.mouseDownButtons.size;
            }

            public int buttonDownNext() {
                return buttonDownHasNext() ? uiEngineState.inputEvents.mouseDownButtons.get(uiEngineState.inputEvents.mouseDownButtonIndex++) : KeyCode.NONE;
            }
        }

        public final class APIState {
            public int x() {
                return uiEngineState.mouse_app.x;
            }

            public int y() {
                return uiEngineState.mouse_app.y;
            }

            public int xUI() {
                return uiEngineState.mouse_ui.x;
            }

            public int yUI() {
                return uiEngineState.mouse_ui.y;
            }

            public float xDelta() {
                return uiEngineState.mouse_delta.x;
            }

            public float yDelta() {
                return uiEngineState.mouse_delta.y;
            }

            public boolean isButtonUp(int keyCode) {
                return !uiEngineState.inputEvents.mouseButtonsDown[keyCode];
            }

            public boolean isAnyButtonUp(int[] keyCodes) {
                for (int i = 0; i < keyCodes.length; i++) {
                    if (isButtonUp(keyCodes[i])) return true;
                }
                return false;
            }

            public boolean isButtonDown(int button) {
                return uiEngineState.inputEvents.mouseButtonsDown[button];
            }

            public boolean isAnyButtonDown(int[] keyCodes) {
                for (int i = 0; i < keyCodes.length; i++) {
                    if (isButtonDown(keyCodes[i])) return true;
                }
                return false;
            }
        }

    }

    public final class APIKeyboard {

        public final APIEvent event;
        public final APIState state;

        APIKeyboard(){
            this.event = new APIEvent();
            this.state = new APIState();
        }

        private String keyBoardGUIObjectName(Object keyBoardobject) {
            if (keyBoardobject != null) {
                if (keyBoardobject instanceof Component component) {
                    return component.name;
                } else if (keyBoardobject instanceof HotKey hotKey) {
                    return hotKey.name;
                }
            }
            return "";
        }

        public Object useUIObject() {
            return uiEngineState.keyboardInteractedUIObjectFrame != null ? uiEngineState.keyboardInteractedUIObjectFrame : null;
        }

        public boolean isUsingUIObject() {
            return useUIObject() != null;
        }

        public String keyBoardUsingUIObjectName() {
            return keyBoardGUIObjectName(useUIObject());
        }

        public final class APIEvent {
            public boolean keyDown() {
                return uiEngineState.inputEvents.keyDown;
            }

            public boolean keyUp() {
                return uiEngineState.inputEvents.keyUp;
            }

            public boolean keyTyped() {
                return uiEngineState.inputEvents.keyTyped;
            }

            public boolean keyTypedCharacter(Character character) {
                for (int i = 0; i < uiEngineState.inputEvents.keyTypedCharacters.size; i++)
                    if (character == uiEngineState.inputEvents.keyTypedCharacters.get(i)) return true;
                return false;
            }

            public boolean keyUpHasNext() {
                return uiEngineState.inputEvents.keyUpKeyCodeIndex < uiEngineState.inputEvents.keyUpKeyCodes.size;
            }

            public int keyUpNext() {
                return keyUpHasNext() ? uiEngineState.inputEvents.keyUpKeyCodes.get(uiEngineState.inputEvents.keyUpKeyCodeIndex++) : KeyCode.NONE;
            }

            public boolean keyDownHasNext() {
                return uiEngineState.inputEvents.keyDownKeyCodeIndex < uiEngineState.inputEvents.keyDownKeyCodes.size;
            }

            public int keyDownNext() {
                return keyDownHasNext() ? uiEngineState.inputEvents.keyDownKeyCodes.get(uiEngineState.inputEvents.keyDownKeyCodeIndex++) : KeyCode.NONE;
            }

            public boolean keyTypedHasNext() {
                return uiEngineState.inputEvents.keyTypedCharacterIndex < uiEngineState.inputEvents.keyTypedCharacters.size;
            }

            public char keyTypedNext() {
                return (char) (keyTypedHasNext() ? uiEngineState.inputEvents.keyTypedCharacters.get(uiEngineState.inputEvents.keyTypedCharacterIndex++) : -1);
            }
        }

        public final class APIState {
            public boolean isKeyDown(int keyCode) {
                return uiEngineState.inputEvents.keysDown[keyCode];
            }

            public boolean isAnyKeyDown(int[] keyCodes) {
                for (int i = 0; i < keyCodes.length; i++) {
                    if (isKeyDown(keyCodes[i])) return true;
                }
                return false;
            }

            public boolean isKeyUp(int keyCode) {
                return !uiEngineState.inputEvents.keysDown[keyCode];
            }

            public boolean isAnyKeyUp(int[] keyCodes) {
                for (int i = 0; i < keyCodes.length; i++) {
                    if (isKeyUp(keyCodes[i])) return true;
                }
                return false;
            }
        }

    }

    public final class APIGamepad {

        public final APIEvent event;
        public final APIState state;

        APIGamepad(){
            this.event = new APIEvent();
            this.state = new APIState();
        }

        public final class APIEvent {
            public boolean buttonDown() {
                return uiEngineState.inputEvents.gamePadButtonDown;
            }

            public boolean buttonUp() {
                return uiEngineState.inputEvents.gamePadButtonUp;
            }

            public boolean connected() {
                return uiEngineState.inputEvents.gamePadConnected;
            }

            public boolean disconnected() {
                return uiEngineState.inputEvents.gamePadDisconnected;
            }

            public boolean leftXMoved() {
                return uiEngineState.inputEvents.gamePadLeftXMoved;
            }

            public boolean leftYMoved() {
                return uiEngineState.inputEvents.gamePadLeftYMoved;
            }

            public boolean leftMoved() {
                return leftXMoved() || leftYMoved();
            }

            public boolean rightXMoved() {
                return uiEngineState.inputEvents.gamePadRightXMoved;
            }

            public boolean rightYMoved() {
                return uiEngineState.inputEvents.gamePadRightYMoved;
            }

            public boolean rightMoved() {
                return rightXMoved() || rightYMoved();
            }

            public boolean leftTriggerMoved() {
                return uiEngineState.inputEvents.gamePadLeftTriggerMoved;
            }

            public boolean rightTriggerMoved() {
                return uiEngineState.inputEvents.gamePadRightTriggerMoved;
            }

            public boolean buttonDownHasNext() {
                return uiEngineState.inputEvents.gamePadButtonDownIndex < uiEngineState.inputEvents.gamePadButtonDownKeyCodes.size;
            }

            public int buttonDownNext() {
                return buttonDownHasNext() ? uiEngineState.inputEvents.gamePadButtonDownKeyCodes.get(uiEngineState.inputEvents.gamePadButtonDownIndex++) : KeyCode.NONE;
            }

            public boolean buttonUpHasNext() {
                return uiEngineState.inputEvents.gamePadButtonUpIndex < uiEngineState.inputEvents.gamePadButtonUpKeyCodes.size;
            }

            public int buttonUpNext() {
                return buttonUpHasNext() ? uiEngineState.inputEvents.gamePadButtonUpKeyCodes.get(uiEngineState.inputEvents.gamePadButtonUpIndex++) : KeyCode.NONE;
            }
        }

        public final class APIState {
            public boolean isButtonDown(int keyCode) {
                return uiEngineState.inputEvents.gamePadButtonsDown[keyCode];
            }

            public boolean isAnyButtonDown(int[] keyCodes) {
                for (int i = 0; i < keyCodes.length; i++) {
                    if (isButtonDown(keyCodes[i])) return true;
                }
                return false;
            }

            public boolean isButtonUp(int keyCode) {
                return !uiEngineState.inputEvents.gamePadButtonsDown[keyCode];
            }

            public boolean isAnyButtonUp(int[] keyCodes) {
                for (int i = 0; i < keyCodes.length; i++) {
                    if (isButtonUp(keyCodes[i])) return true;
                }
                return false;
            }

            public float leftTrigger() {
                return uiEngineState.inputEvents.gamePadLeftTrigger;
            }

            public float rightTrigger() {
                return uiEngineState.inputEvents.gamePadRightTrigger;
            }

            public float leftX() {
                return uiEngineState.inputEvents.gamePadLeftX;
            }

            public float leftY() {
                return uiEngineState.inputEvents.gamePadLeftY;
            }

            public float rightX() {
                return uiEngineState.inputEvents.gamePadRightX;
            }

            public float rightY() {
                return uiEngineState.inputEvents.gamePadRightY;
            }
        }

    }


}
