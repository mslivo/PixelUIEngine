package net.mslivo.core.engine.ui_engine.state;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.media.CMediaCursor;
import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.ui_engine.UIEngineBaseMedia_8x8;

import java.io.Serializable;

public class UIConfig implements Serializable, Cloneable {
    public CMediaCursor ui_cursor;
    public boolean ui_keyInteractionsDisabled;
    public boolean ui_mouseInteractionsDisabled;
    public boolean ui_foldWindowsOnDoubleClick;
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
    public CMediaFont window_defaultFont;
    public Color component_defaultColor;
    public CMediaFont component_defaultFont;
    public int component_appViewportDefaultUpdateTime;
    public float component_listDragAlpha;
    public float component_gridDragAlpha;
    public float component_knobSensitivity;
    public float component_scrollbarSensitivity;
    public int component_mapOverlayDefaultFadeoutTime;
    public int notification_max;
    public int notification_defaultDisplayTime;
    public CMediaFont notification_defaultFont;
    public Color notification_defaultColor;
    public int notification_fadeoutTime;
    public float notification_scrollSpeed;
    public Color tooltip_defaultColor;
    public Color tooltip_defaultBorderColor;
    public CMediaFont tooltip_defaultFont;
    public int tooltip_FadeInTime;
    public int tooltip_FadeInDelayTime;
    public char[] component_textFieldDefaultAllowedCharacters;
    public CMediaFont mouseTextInput_defaultFont;
    public char[] mouseTextInput_defaultLowerCaseCharacters;
    public char[] mouseTextInput_defaultUpperCaseCharacters;
    public Color mouseTextInput_defaultColor;

    public UIConfig() {
        // ##### UI Default Values #####
        ui_cursor = UIEngineBaseMedia_8x8.UI_CURSOR_ARROW;
        ui_keyInteractionsDisabled = false;
        ui_mouseInteractionsDisabled = false;
        ui_foldWindowsOnDoubleClick = true;
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
        window_defaultColor = Color.WHITE.cpy();
        window_defaultFont = UIEngineBaseMedia_8x8.UI_FONT_BLACK;
        component_defaultColor = Color.WHITE.cpy();
        component_defaultFont = UIEngineBaseMedia_8x8.UI_FONT_BLACK;
        component_appViewportDefaultUpdateTime = 0;
        component_listDragAlpha = 0.8f;
        component_gridDragAlpha = 0.8f;
        component_knobSensitivity = 1f;
        component_scrollbarSensitivity = 1f;
        component_mapOverlayDefaultFadeoutTime = 200;
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
                '!', '?', '.', '+', '-', '=', '&', '%', '*', '$'
        };
        notification_max = 20;
        notification_defaultDisplayTime = 3000;
        notification_defaultFont = UIEngineBaseMedia_8x8.UI_FONT_WHITE;
        notification_defaultColor = Color.DARK_GRAY.cpy();
        notification_fadeoutTime = 200;
        notification_scrollSpeed = 1;
        tooltip_defaultColor = Color.WHITE.cpy();
        tooltip_defaultBorderColor = Color.valueOf("7F7F7F").cpy();
        tooltip_defaultFont = UIEngineBaseMedia_8x8.UI_FONT_BLACK;
        tooltip_FadeInTime = 50;
        tooltip_FadeInDelayTime = 25;
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
        mouseTextInput_defaultFont = UIEngineBaseMedia_8x8.UI_FONT_BLACK;
        mouseTextInput_defaultColor = Color.WHITE.cpy();
    }


}