package org.mslivo.core.engine.ui_engine.misc.config;

import com.badlogic.gdx.graphics.Color;
import org.mslivo.core.engine.media_manager.media.CMediaCursor;
import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.ui_engine.media.GUIBaseMedia;

import java.io.Serializable;

public class Config implements Serializable {
    public CMediaCursor ui_cursor;
    public boolean ui_keyInteractionsDisabled;
    public boolean ui_mouseInteractionsDisabled;
    public int ui_doubleClickTime;
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
    public boolean windows_defaultEnforceScreenBounds;
    public Color windows_defaultColor;
    public CMediaFont windows_defaultFont;
    public Color components_defaultColor;
    public CMediaFont components_defaultFont;
    public int gameViewport_defaultUpdateTime;
    public float list_dragAlpha;
    public float inventory_dragAlpha;
    public float knob_sensitivity;
    public float scrollbar_sensitivity;
    public int button_holdTimer;
    public int mapOverlay_defaultFadeoutTime;
    public int notifications_max;
    public int notifications_defaultDisplayTime;
    public CMediaFont notifications_defaultFont;
    public Color notifications_defaultColor;
    public int notifications_fadeoutTime;
    public float notifications_scrollSpeed;
    public Color tooltip_defaultColor;
    public CMediaFont tooltip_defaultFont;
    public int tooltip_FadeInTime;
    public int tooltip_FadeInDelayTime;
    public char[] textField_defaultAllowedCharacters;
    public CMediaFont mouseTextInput_defaultFont;
    public char[] mouseTextInput_defaultLowerCaseCharacters;
    public char[] mouseTextInput_defaultUpperCaseCharacters;

    public Config() {
        // ##### UI Default Values #####
        ui_cursor = GUIBaseMedia.GUI_CURSOR_ARROW;
        ui_keyInteractionsDisabled = false;
        ui_mouseInteractionsDisabled = false;
        ui_doubleClickTime = 180;
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
        windows_defaultEnforceScreenBounds = false;
        windows_defaultColor = Color.WHITE.cpy();
        windows_defaultFont = GUIBaseMedia.FONT_BLACK;
        components_defaultColor = Color.WHITE.cpy();
        components_defaultFont = GUIBaseMedia.FONT_BLACK;
        gameViewport_defaultUpdateTime = 200;
        list_dragAlpha = 0.8f;
        inventory_dragAlpha = 0.8f;
        knob_sensitivity = 1f;
        scrollbar_sensitivity = 1f;
        button_holdTimer = 8;
        mapOverlay_defaultFadeoutTime = 200;
        notifications_max = 20;
        notifications_defaultDisplayTime = 3000;
        notifications_defaultFont = GUIBaseMedia.FONT_WHITE;
        notifications_defaultColor = Color.DARK_GRAY.cpy();
        notifications_fadeoutTime = 200;
        notifications_scrollSpeed = 1;
        tooltip_defaultColor = Color.WHITE.cpy();
        tooltip_defaultFont = GUIBaseMedia.FONT_BLACK;
        tooltip_FadeInTime = 50;
        tooltip_FadeInDelayTime = 25;
        textField_defaultAllowedCharacters = new char[]{
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
        mouseTextInput_defaultFont = GUIBaseMedia.FONT_BLACK;
    }


}