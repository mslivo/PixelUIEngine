package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.engine.constants.KeyCode;
import net.mslivo.pixelui.media.CMediaFont;
import net.mslivo.pixelui.media.CMediaSprite;
import net.mslivo.pixelui.media.UIEngineBaseMedia_8x8;

public final class UIEngineConfig {

    private static final Color DEFAULT_COlOR = Color.valueOf("CECECE");
    private static final Color DEFAULT_COlOR_BRIGHT = Color.valueOf("FFFFFF");
    private static final Color DEFAULT_COLOR_FONT = Color.valueOf("000000");

    public final UI ui = new UI();
    public final Input input = new Input();
    public final Window window = new Window();
    public final Component component = new Component();
    public final Notification notification = new Notification();
    public final Tooltip tooltip = new Tooltip();
    public final MouseTextInput mouseTextInput = new MouseTextInput();

    public class Input {
        public float emulatedMouseCursorSpeed = 4.0f;
        public boolean hardwareMouseEnabled = true;
        public boolean keyboardMouseEnabled = false;
        public int[] keyboardMouseButtonsUp = {KeyCode.Key.UP};
        public int[] keyboardMouseButtonsDown = {KeyCode.Key.DOWN};
        public int[] keyboardMouseButtonsLeft = {KeyCode.Key.LEFT};
        public int[] keyboardMouseButtonsRight = {KeyCode.Key.RIGHT};
        public int[] keyboardMouseButtonsMouse1 = {KeyCode.Key.CONTROL_LEFT};
        public int[] keyboardMouseButtonsMouse2 = {KeyCode.Key.ALT_LEFT};
        public int[] keyboardMouseButtonsMouse3 = {KeyCode.Key.SHIFT_LEFT};
        public int[] keyboardMouseButtonsMouse4 = null;
        public int[] keyboardMouseButtonsMouse5 = null;
        public int[] keyboardMouseButtonsScrollUp = {KeyCode.Key.PAGE_UP};
        public int[] keyboardMouseButtonsScrollDown = {KeyCode.Key.PAGE_DOWN};
        public boolean gamePadMouseEnabled = false;
        public float gamePadMouseJoystickDeadZone = 0.3f;
        public boolean gamePadMouseStickLeftEnabled = true;
        public boolean gamePadMouseStickRightEnabled = true;
        public int[] gamePadMouseButtonsMouse1 = {KeyCode.GamePad.A};
        public int[] gamePadMouseButtonsMouse2 = {KeyCode.GamePad.B};
        public int[] gamePadMouseButtonsMouse3 = {KeyCode.GamePad.Y};
        public int[] gamePadMouseButtonsMouse4 = null;
        public int[] gamePadMouseButtonsMouse5 = null;
        public int[] gamePadMouseButtonsScrollUp = {KeyCode.GamePad.DPAD_UP};
        public int[] gamePadMouseButtonsScrollDown = {KeyCode.GamePad.DPAD_DOWN};
    }

    public class UI {
        public CMediaFont font= UIEngineBaseMedia_8x8.UI_FONT;
        public Color fontDefaultColor = DEFAULT_COLOR_FONT.cpy();
        public CMediaSprite cursor = UIEngineBaseMedia_8x8.UI_CURSOR_ARROW;
        public boolean keyInteractionsDisabled = false;
        public boolean mouseInteractionsDisabled = false;
        public boolean foldWindowsOnDoubleClick= true;
        public AnimationTimerFunction animationTimerFunction = new AnimationTimerFunction() {
            final float delta = 1/60f;
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

    public class Window {
        public boolean defaultEnforceScreenBounds  = true;
        public Color defaultColor  = DEFAULT_COlOR.cpy();
    }

    public class Component {
        public Color defaultColor = DEFAULT_COlOR.cpy();
        public Color contextMenuDefaultColor = DEFAULT_COlOR_BRIGHT.cpy();
        public int appViewportDefaultUpdateTime = 0;
        public float listDragAlpha = 0.8f;
        public float gridDragAlpha = 0.8f;
        public float knobSensitivity = 1f;
        public float scrollbarSensitivity = 1f;
        public float mapOverlayDefaultFadeoutSpeed = 0.05f;
        public char[] textFieldDefaultAllowedCharacters= new char[]{
                'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l',
                'm', 'n', 'o', 'p', 'q', 'r',
                's', 't', 'u', 'v', 'w',
                'x', 'y', 'z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L',
                'M', 'N', 'O', 'P', 'Q', 'R',
                'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z',
                '!', '?', '.', '+', '-', '=', '&', '%', '*', '$', '/', ':', ';', ',',
                '"', '(', ')', '_',
                ' '
        };
        public Color textFieldDefaultMarkerColor  = Color.valueOf("8FD3FF");
    }

    public class Notification {
        public int maxNotifications = 20;
        public int defaultDisplayTime = 120;
        public Color defaultColor = DEFAULT_COlOR.cpy();
        public int foldTime = 12;
        public int toolTipNotificationDefaultDisplayTime = 140;
        public int toolTipNotificationFadeoutTime = 12;
    }

    public class Tooltip {
        public Color defaultCellColor = DEFAULT_COlOR_BRIGHT.cpy();
        public float fadeInSpeed = 0.2f;
        public int fadeInDelay = 20;
        public float fadeOutSpeed = 0.2f;
    }

    public class MouseTextInput {
        public char[]  defaultLowerCaseCharacters = {
            'a', 'b', 'c', 'd', 'e', 'f',
                    'g', 'h', 'i', 'j', 'k', 'l',
                    'm', 'n', 'o', 'p', 'q', 'r',
                    's', 't', 'u', 'v', 'w',
                    'x', 'y', 'z',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
        };
        public char[] defaultUpperCaseCharacters = {
            'A', 'B', 'C', 'D', 'E', 'F',
                    'G', 'H', 'I', 'J', 'K', 'L',
                    'M', 'N', 'O', 'P', 'Q', 'R',
                    'S', 'T', 'U', 'V', 'W',
                    'X', 'Y', 'Z',
                    '!', '?', '.', '+', '-', '=', '&', '%', '*', '$'
        };
        public Color defaultColor = DEFAULT_COlOR.cpy();
    }

    public interface AnimationTimerFunction {
        void updateAnimationTimer();

        float getAnimationTimer();
    }

}