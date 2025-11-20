package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.engine.constants.KeyCode;
import net.mslivo.pixelui.media.CMediaFont;
import net.mslivo.pixelui.media.CMediaSprite;
import net.mslivo.pixelui.theme.UIEngineTheme;

public final class UIEngineConfig {

    private static final Color DEFAULT_COlOR = Color.valueOf("CECECE");
    private static final Color DEFAULT_COlOR_BRIGHT = Color.valueOf("FFFFFF");
    private static final Color DEFAULT_COLOR_FONT = Color.valueOf("000000");

    public static final int GAMEPAD_MOUSE_BUTTONS = 7;
    public static final int KEYBOARD_MOUSE_BUTTONS = 11;

    private final UIEngineTheme theme;
    public final UIConfig ui;
    public final InputConfig input;
    public final WindowConfig window;
    public final ComponentConfig component;
    public final Notification notification;
    public final TooltipConfig tooltip;
    public final MouseTextInputConfig mouseTextInput;

    public UIEngineConfig(UIEngineTheme theme) {
        this.theme = theme;
        this.ui = new UIConfig();
        this.input = new InputConfig();
        this.window = new WindowConfig();
        this.component = new ComponentConfig();
        this.notification = new Notification();
        this.tooltip = new TooltipConfig();
        this.mouseTextInput = new MouseTextInputConfig();
    }

    public class InputConfig {
        public boolean hardwareMouseEnabled = true;

        public boolean keyboardMouseEnabled = false;
        public float keyboardMouseCursorSpeed = 3.0f;
        public float keyboardMouseCursorSpeedUpFactor = 2.0f;
        public float keyboardMouseCursorSmoothing = 0.25f;
        public int[] keyboardMouseButtonsUp = {KeyCode.Key.UP};
        public int[] keyboardMouseButtonsDown = {KeyCode.Key.DOWN};
        public int[] keyboardMouseButtonsLeft = {KeyCode.Key.LEFT};
        public int[] keyboardMouseButtonsRight = {KeyCode.Key.RIGHT};
        public int[] keyboardMouseButtonsMouse1 = {KeyCode.Key.CONTROL_LEFT};
        public int[] keyboardMouseButtonsMouse2 = {KeyCode.Key.ALT_LEFT};
        public int[] keyboardMouseButtonsMouse3 = null;
        public int[] keyboardMouseButtonsMouse4 = null;
        public int[] keyboardMouseButtonsMouse5 = null;
        public int[] keyboardMouseButtonsScrollUp = {KeyCode.Key.PAGE_UP};
        public int[] keyboardMouseButtonsScrollDown = {KeyCode.Key.PAGE_DOWN};
        public int[] keyboardMouseButtonsCursorSpeedUp = {KeyCode.Key.SHIFT_LEFT};

        public boolean gamePadMouseEnabled = false;
        public float gamepadMouseCursorSpeed = 3.0f;
        public float gamepadMouseCursorSpeedUpFactor = 2.0f;
        public float gamePadMouseJoystickDeadZone = 0.3f;
        public boolean gamePadMouseStickLeftEnabled = true;
        public boolean gamePadMouseStickRightEnabled = true;
        public int[] gamePadMouseButtonsMouse1 = {KeyCode.GamePad.A};
        public int[] gamePadMouseButtonsMouse2 = {KeyCode.GamePad.B};
        public int[] gamePadMouseButtonsMouse3 = null;
        public int[] gamePadMouseButtonsMouse4 = null;
        public int[] gamePadMouseButtonsMouse5 = null;
        public int[] gamePadMouseButtonsScrollUp = {KeyCode.GamePad.DPAD_UP};
        public int[] gamePadMouseButtonsScrollDown = {KeyCode.GamePad.DPAD_DOWN};
        public int[] gamePadMouseButtonsCursorSpeedUp = {KeyCode.GamePad.X};

        public int[] gamepadMouseButtons(int index) {
            index = Math.clamp(index, 0, GAMEPAD_MOUSE_BUTTONS);
            return switch (index) {
                case 0 -> this.gamePadMouseButtonsMouse1;
                case 1 -> this.gamePadMouseButtonsMouse2;
                case 2 -> this.gamePadMouseButtonsMouse3;
                case 3 -> this.gamePadMouseButtonsMouse4;
                case 4 -> this.gamePadMouseButtonsMouse5;
                case 5 -> this.gamePadMouseButtonsScrollUp;
                case 6 -> this.gamePadMouseButtonsScrollDown;
                case 7 -> this.gamePadMouseButtonsCursorSpeedUp;
                default -> throw new IllegalStateException("Unexpected value: " + index);
            };
        }

        public int[] keyboardMouseButtons(int index) {
            index = Math.clamp(index, 0, KEYBOARD_MOUSE_BUTTONS);
            return switch (index) {
                case 0 -> this.keyboardMouseButtonsUp;
                case 1 -> this.keyboardMouseButtonsDown;
                case 2 -> this.keyboardMouseButtonsLeft;
                case 3 -> this.keyboardMouseButtonsRight;
                case 4 -> this.keyboardMouseButtonsMouse1;
                case 5 -> this.keyboardMouseButtonsMouse2;
                case 6 -> this.keyboardMouseButtonsMouse3;
                case 7 -> this.keyboardMouseButtonsMouse4;
                case 8 -> this.keyboardMouseButtonsMouse5;
                case 9 -> this.keyboardMouseButtonsScrollUp;
                case 10 -> this.keyboardMouseButtonsScrollDown;
                case 11 -> this.keyboardMouseButtonsCursorSpeedUp;
                default -> throw new IllegalStateException("Unexpected value: " + index);
            };
        }

    }

    public class UIConfig {
        public CMediaFont font = theme.UI_FONT;
        public Color fontDefaultColor = DEFAULT_COLOR_FONT.cpy();
        public CMediaSprite cursor = theme.UI_CURSOR_ARROW;
        public boolean keyInteractionsDisabled = false;
        public boolean mouseInteractionsDisabled = false;
        public boolean foldWindowsOnDoubleClick = true;

        public AnimationTimerFunction animationTimerFunction = new AnimationTimerFunction() {
            final float delta = 1 / 60f;
            float animationTimer = 0;

            @Override
            public void updateAnimationTimer() {
                animationTimer += delta;
                return;
            }

            @Override
            public float getAnimationTimer() {
                return animationTimer;
            }
        };
    }

    public class WindowConfig {
        public boolean defaultEnforceScreenBounds = true;
        public Color defaultColor = DEFAULT_COlOR.cpy();
    }

    public class ComponentConfig {
        public Color defaultColor = DEFAULT_COlOR.cpy();
        public Color contextMenuDefaultColor = DEFAULT_COlOR_BRIGHT.cpy();
        public int appViewportDefaultUpdateTime = 0;
        public float listDragAlpha = 0.8f;
        public float gridDragAlpha = 0.8f;
        public float knobSensitivity = 1f;
        public float scrollbarSensitivity = 1f;
        public char[] textFieldDefaultAllowedCharacters = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '!', '?', '.', '+', '-', '=', '&', '%', '*', '$', '/', ':', ';', ',', '"', '(', ')', '_', ' '};
        public Color textFieldDefaultMarkerColor = Color.valueOf("8FD3FF");
    }

    public class Notification {
        public int maxNotifications = 20;
        public int defaultDisplayTime = 120;
        public Color defaultColor = DEFAULT_COlOR.cpy();
        public int foldTime = 12;
        public int toolTipNotificationDefaultDisplayTime = 140;
        public int toolTipNotificationFadeoutTime = 12;
    }

    public class TooltipConfig {
        public Color defaultCellColor = DEFAULT_COlOR_BRIGHT.cpy();
        public float fadeInSpeed = 0.2f;
        public int fadeInDelay = 20;
        public float fadeOutSpeed = 0.2f;
    }

    public class MouseTextInputConfig {
        public char[] defaultLowerCaseCharacters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        public char[] defaultUpperCaseCharacters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ' ', '!', '?', '.', '+', '-', '=', '&', '%', '*', '$'};
        public Color defaultColor = DEFAULT_COlOR.cpy();
        public int charsPerRow = 8;
    }


    public interface AnimationTimerFunction {
        void updateAnimationTimer();

        float getAnimationTimer();
    }

}