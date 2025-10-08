package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.media.CMediaFont;
import net.mslivo.pixelui.media.CMediaSprite;
import net.mslivo.pixelui.media.UIEngineBaseMedia_8x8;

public final class UIEngineConfig {

    private static final Color DEFAULT_COlOR = Color.valueOf("CECECE");
    private static final Color DEFAULT_COlOR_BRIGHT = Color.valueOf("FFFFFF");
    private static final Color DEFAULT_COLOR_FONT = Color.valueOf("000000");

    public CMediaFont ui_font;
    public Color ui_font_defaultColor;
    public CMediaSprite ui_cursor;
    public boolean ui_keyInteractionsDisabled;
    public boolean ui_mouseInteractionsDisabled;
    public boolean ui_foldWindowsOnDoubleClick;
    public AnimationTimerFunction ui_animationTimerFunction;
    public float input_emulatedMouseCursorSpeed;
    public boolean input_hardwareMouseEnabled;
    public boolean input_keyboardMouseEnabled;
    public int[] input_keyboardMouseButtonsUp;
    public int[] input_keyboardMouseButtonsDown;
    public int[] input_keyboardMouseButtonsLeft;
    public int[] input_keyboardMouseButtonsRight;
    public int[] input_keyboardMouseButtonsMouse1;
    public int[] input_keyboardMouseButtonsMouse2;
    public int[] input_keyboardMouseButtonsMouse3;
    public int[] input_keyboardMouseButtonsMouse4;
    public int[] input_keyboardMouseButtonsMouse5;
    public int[] input_keyboardMouseButtonsScrollUp;
    public int[] input_keyboardMouseButtonsScrollDown;
    public boolean input_gamePadMouseEnabled;
    public float input_gamePadMouseJoystickDeadZone;
    public boolean input_gamePadMouseStickLeftEnabled;
    public boolean input_gamePadMouseStickRightEnabled;
    public int[] input_gamePadMouseButtonsMouse1;
    public int[] input_gamePadMouseButtonsMouse2;
    public int[] input_gamePadMouseButtonsMouse3;
    public int[] input_gamePadMouseButtonsMouse4;
    public int[] input_gamePadMouseButtonsMouse5;
    public int[] input_gamePadMouseButtonsScrollUp;
    public int[] input_gamePadMouseButtonsScrollDown;
    public boolean window_defaultEnforceScreenBounds;
    public Color window_defaultColor;
    public Color component_defaultColor;
    public Color contextMenu_defaultColor;
    public int component_appViewportDefaultUpdateTime;
    public float component_listDragAlpha;
    public float component_gridDragAlpha;
    public float component_knobSensitivity;
    public float component_scrollbarSensitivity;
    public float component_mapOverlayDefaultFadeoutSpeed;
    public int notification_max;
    public int notification_defaultDisplayTime;
    public Color notification_defaultColor;
    public int notification_foldTime;
    public int notification_tooltip_defaultDisplayTime;
    public int notification_tooltip_fadeoutTime;
    public Color tooltip_defaultCellColor;
    public float tooltip_FadeInSpeed;
    public int tooltip_FadeInDelay;
    public float tooltip_FadeoutSpeed;
    public char[] component_textFieldDefaultAllowedCharacters;
    public Color component_textFieldDefaultMarkerColor;
    public char[] mouseTextInput_defaultLowerCaseCharacters;
    public char[] mouseTextInput_defaultUpperCaseCharacters;
    public Color mouseTextInput_defaultColor;

    public UIEngineConfig() {

        // Initialize Default Values
        // ##### UI Default Values #####
        ui_font = UIEngineBaseMedia_8x8.UI_FONT;
        ui_font_defaultColor = DEFAULT_COLOR_FONT.cpy();
        ui_cursor = UIEngineBaseMedia_8x8.UI_CURSOR_ARROW;
        ui_keyInteractionsDisabled = false;
        ui_mouseInteractionsDisabled = false;
        ui_foldWindowsOnDoubleClick = true;
        ui_animationTimerFunction = new AnimationTimerFunction() {
            float delta = 0.016f;
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
        // ##### Input Default Values #####
        input_emulatedMouseCursorSpeed = 4.0f;
        input_hardwareMouseEnabled = true;
        input_keyboardMouseEnabled = false;
        input_keyboardMouseButtonsUp = null;
        input_keyboardMouseButtonsDown = null;
        input_keyboardMouseButtonsLeft = null;
        input_keyboardMouseButtonsRight = null;
        input_keyboardMouseButtonsMouse1 = null;
        input_keyboardMouseButtonsMouse2 = null;
        input_keyboardMouseButtonsMouse3 = null;
        input_keyboardMouseButtonsMouse4 = null;
        input_keyboardMouseButtonsMouse5 = null;
        input_keyboardMouseButtonsScrollUp = null;
        input_keyboardMouseButtonsScrollDown = null;
        input_gamePadMouseEnabled = false;
        input_gamePadMouseJoystickDeadZone = 0.3f;
        input_gamePadMouseStickLeftEnabled = false;
        input_gamePadMouseStickRightEnabled = false;
        input_gamePadMouseButtonsMouse1 = null;
        input_gamePadMouseButtonsMouse2 = null;
        input_gamePadMouseButtonsMouse3 = null;
        input_gamePadMouseButtonsMouse4 = null;
        input_gamePadMouseButtonsMouse5 = null;
        input_gamePadMouseButtonsScrollUp = null;
        input_gamePadMouseButtonsScrollDown = null;
        // ##### Window & Component Default Values #####
        window_defaultEnforceScreenBounds = true;
        window_defaultColor = DEFAULT_COlOR.cpy();
        component_defaultColor = DEFAULT_COlOR.cpy();
        contextMenu_defaultColor = DEFAULT_COlOR_BRIGHT.cpy();
        component_appViewportDefaultUpdateTime = 0;
        component_listDragAlpha = 0.8f;
        component_gridDragAlpha = 0.8f;
        component_knobSensitivity = 1f;
        component_scrollbarSensitivity = 1f;
        component_mapOverlayDefaultFadeoutSpeed = 0.05f;
        component_textFieldDefaultAllowedCharacters = new char[]{
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
        component_textFieldDefaultMarkerColor = Color.valueOf("8FD3FF");
        notification_max = 20;
        notification_defaultDisplayTime = 120;
        notification_defaultColor = DEFAULT_COlOR.cpy();
        notification_foldTime = 12;
        notification_tooltip_defaultDisplayTime = 140;
        notification_tooltip_fadeoutTime = 12;
        tooltip_defaultCellColor = DEFAULT_COlOR_BRIGHT.cpy();
        tooltip_FadeInSpeed = 0.2f;
        tooltip_FadeoutSpeed = 0.2f;
        tooltip_FadeInDelay = 20;
        // ##### MouseTextInput Default Values #####
        mouseTextInput_defaultLowerCaseCharacters = new char[]{
                'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l',
                'm', 'n', 'o', 'p', 'q', 'r',
                's', 't', 'u', 'v', 'w',
                'x', 'y', 'z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
        };
        mouseTextInput_defaultUpperCaseCharacters = new char[]{
                'A', 'B', 'C', 'D', 'E', 'F',
                'G', 'H', 'I', 'J', 'K', 'L',
                'M', 'N', 'O', 'P', 'Q', 'R',
                'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z',
                '!', '?', '.', '+', '-', '=', '&', '%', '*', '$'
        };
        mouseTextInput_defaultColor = DEFAULT_COlOR.cpy();
    }

    public interface AnimationTimerFunction {
        void updateAnimationTimer();

        float getAnimationTimer();
    }

}