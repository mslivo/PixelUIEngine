package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.media.CMediaCursor;
import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;

public final class APIConfig {
    private API api;
    private UIEngineState uiEngineState;
    private MediaManager mediaManager;
    public final APIUIConfig ui;
    public final APIInputConfig input;
    public final APIWindowConfig window;
    public final APIComponentConfig component;
    public final APINotificationsConfig notification;
    public final APITooltipConfig tooltip;
    public final APIMouseTextInputConfig mouseTextInput;

    APIConfig(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.ui = new APIUIConfig();
        this.input = new APIInputConfig();
        this.window = new APIWindowConfig();
        this.component = new APIComponentConfig();
        this.notification = new APINotificationsConfig();
        this.tooltip = new APITooltipConfig();
        this.mouseTextInput = new APIMouseTextInputConfig();
    }


    public final class APIUIConfig {
        APIUIConfig(){
        }

        public CMediaCursor getCursor() {
            return uiEngineState.uiEngineConfig.ui_cursor;
        }

        public void setCursor(CMediaCursor ui_cursor) {
            uiEngineState.uiEngineConfig.ui_cursor = ui_cursor;
        }

        public boolean isKeyInteractionsDisabled() {
            return uiEngineState.uiEngineConfig.ui_keyInteractionsDisabled;
        }

        public void setKeyInteractionsDisabled(boolean ui_keyInteractionsDisabled) {
            uiEngineState.uiEngineConfig.ui_keyInteractionsDisabled = ui_keyInteractionsDisabled;
        }

        public boolean isMouseInteractionsDisabled() {
            return uiEngineState.uiEngineConfig.ui_mouseInteractionsDisabled;
        }

        public void setMouseInteractionsDisabled(boolean ui_mouseInteractionsDisabled) {
            uiEngineState.uiEngineConfig.ui_mouseInteractionsDisabled = ui_mouseInteractionsDisabled;
        }

        public boolean isFoldWindowsOnDoubleClick() {
            return uiEngineState.uiEngineConfig.ui_foldWindowsOnDoubleClick;
        }

        public void setFoldWindowsOnDoubleClick(boolean ui_foldWindowsOnDoubleClick) {
            uiEngineState.uiEngineConfig.ui_foldWindowsOnDoubleClick = ui_foldWindowsOnDoubleClick;
        }

    }

    public final class APIInputConfig {
        APIInputConfig(){
        }

        public float getEmulatedMouseCursorSpeed() {
            return uiEngineState.uiEngineConfig.input_emulatedMouseCursorSpeed;
        }

        public void setEmulatedMouseCursorSpeed(float input_emulatedMouseCursorSpeed) {
            uiEngineState.uiEngineConfig.input_emulatedMouseCursorSpeed = input_emulatedMouseCursorSpeed;
        }

        public boolean isHardwareMouseEnabled() {
            return uiEngineState.uiEngineConfig.input_hardwareMouseEnabled;
        }

        public void setHardwareMouseEnabled(boolean input_hardwareMouseEnabled) {
            uiEngineState.uiEngineConfig.input_hardwareMouseEnabled = input_hardwareMouseEnabled;
        }

        public boolean isInput_keyboardMouseEnabled() {
            return uiEngineState.uiEngineConfig.input_keyboardMouseEnabled;
        }

        public void setKeyboardMouseEnabled(boolean input_keyboardMouseEnabled) {
            uiEngineState.uiEngineConfig.input_keyboardMouseEnabled = input_keyboardMouseEnabled;
        }

        public int[] getKeyboardMouseButtonsUp() {
            return uiEngineState.uiEngineConfig.input_keyboardMouseButtonsUp;
        }

        public void setKeyboardMouseButtonsUp(int[] input_keyboardMouseButtonsUp) {
            uiEngineState.uiEngineConfig.input_keyboardMouseButtonsUp = input_keyboardMouseButtonsUp;
        }

        public int[] getKeyboardMouseButtonsDown() {
            return uiEngineState.uiEngineConfig.input_keyboardMouseButtonsDown;
        }

        public void setKeyboardMouseButtonsDown(int[] input_keyboardMouseButtonsDown) {
            uiEngineState.uiEngineConfig.input_keyboardMouseButtonsDown = input_keyboardMouseButtonsDown;
        }

        public int[] getKeyboardMouseButtonsLeft() {
            return uiEngineState.uiEngineConfig.input_keyboardMouseButtonsLeft;
        }

        public void setKeyboardMouseButtonsLeft(int[] input_keyboardMouseButtonsLeft) {
            uiEngineState.uiEngineConfig.input_keyboardMouseButtonsLeft = input_keyboardMouseButtonsLeft;
        }

        public int[] getKeyboardMouseButtonsRight() {
            return uiEngineState.uiEngineConfig.input_keyboardMouseButtonsRight;
        }

        public void setKeyboardMouseButtonsRight(int[] input_keyboardMouseButtonsRight) {
            uiEngineState.uiEngineConfig.input_keyboardMouseButtonsRight = input_keyboardMouseButtonsRight;
        }

        public int[] getKeyboardMouseButtonsMouse1() {
            return uiEngineState.uiEngineConfig.input_keyboardMouseButtonsMouse1;
        }

        public void setKeyboardMouseButtonsMouse1(int[] input_keyboardMouseButtonsMouse1) {
            uiEngineState.uiEngineConfig.input_keyboardMouseButtonsMouse1 = input_keyboardMouseButtonsMouse1;
        }

        public int[] getKeyboardMouseButtonsMouse2() {
            return uiEngineState.uiEngineConfig.input_keyboardMouseButtonsMouse2;
        }

        public void setKeyboardMouseButtonsMouse2(int[] input_keyboardMouseButtonsMouse2) {
            uiEngineState.uiEngineConfig.input_keyboardMouseButtonsMouse2 = input_keyboardMouseButtonsMouse2;
        }

        public int[] getKeyboardMouseButtonsMouse3() {
            return uiEngineState.uiEngineConfig.input_keyboardMouseButtonsMouse3;
        }

        public void setKeyboardMouseButtonsMouse3(int[] input_keyboardMouseButtonsMouse3) {
            uiEngineState.uiEngineConfig.input_keyboardMouseButtonsMouse3 = input_keyboardMouseButtonsMouse3;
        }

        public int[] getKeyboardMouseButtonsMouse4() {
            return uiEngineState.uiEngineConfig.input_keyboardMouseButtonsMouse4;
        }

        public void setKeyboardMouseButtonsMouse4(int[] input_keyboardMouseButtonsMouse4) {
            uiEngineState.uiEngineConfig.input_keyboardMouseButtonsMouse4 = input_keyboardMouseButtonsMouse4;
        }

        public int[] getKeyboardMouseButtonsMouse5() {
            return uiEngineState.uiEngineConfig.input_keyboardMouseButtonsMouse5;
        }

        public void setKeyboardMouseButtonsMouse5(int[] input_keyboardMouseButtonsMouse5) {
            uiEngineState.uiEngineConfig.input_keyboardMouseButtonsMouse5 = input_keyboardMouseButtonsMouse5;
        }

        public int[] getKeyboardMouseButtonsScrollUp() {
            return uiEngineState.uiEngineConfig.input_keyboardMouseButtonsScrollUp;
        }

        public void setKeyboardMouseButtonsScrollUp(int[] input_keyboardMouseButtonsScrollUp) {
            uiEngineState.uiEngineConfig.input_keyboardMouseButtonsScrollUp = input_keyboardMouseButtonsScrollUp;
        }

        public int[] getKeyboardMouseButtonsScrollDown() {
            return uiEngineState.uiEngineConfig.input_keyboardMouseButtonsScrollDown;
        }

        public void setKeyboardMouseButtonsScrollDown(int[] input_keyboardMouseButtonsScrollDown) {
            uiEngineState.uiEngineConfig.input_keyboardMouseButtonsScrollDown = input_keyboardMouseButtonsScrollDown;
        }

        public boolean isGamePadMouseEnabled() {
            return uiEngineState.uiEngineConfig.input_gamePadMouseEnabled;
        }

        public void setGamePadMouseEnabled(boolean input_gamePadMouseEnabled) {
            uiEngineState.uiEngineConfig.input_gamePadMouseEnabled = input_gamePadMouseEnabled;
        }

        public float getGamePadMouseJoystickDeadZone() {
            return uiEngineState.uiEngineConfig.input_gamePadMouseJoystickDeadZone;
        }

        public void setGamePadMouseJoystickDeadZone(float input_gamePadMouseJoystickDeadZone) {
            uiEngineState.uiEngineConfig.input_gamePadMouseJoystickDeadZone = input_gamePadMouseJoystickDeadZone;
        }

        public boolean isInput_gamePadMouseStickLeftEnabled() {
            return uiEngineState.uiEngineConfig.input_gamePadMouseStickLeftEnabled;
        }

        public void setGamePadMouseStickLeftEnabled(boolean input_gamePadMouseStickLeftEnabled) {
            uiEngineState.uiEngineConfig.input_gamePadMouseStickLeftEnabled = input_gamePadMouseStickLeftEnabled;
        }

        public boolean isGamePadMouseStickRightEnabled() {
            return uiEngineState.uiEngineConfig.input_gamePadMouseStickRightEnabled;
        }

        public void setGamePadMouseStickRightEnabled(boolean input_gamePadMouseStickRightEnabled) {
            uiEngineState.uiEngineConfig.input_gamePadMouseStickRightEnabled = input_gamePadMouseStickRightEnabled;
        }

        public int[] getGamePadMouseButtonsMouse1() {
            return uiEngineState.uiEngineConfig.input_gamePadMouseButtonsMouse1;
        }

        public void setGamePadMouseButtonsMouse1(int[] input_gamePadMouseButtonsMouse1) {
            uiEngineState.uiEngineConfig.input_gamePadMouseButtonsMouse1 = input_gamePadMouseButtonsMouse1;
        }

        public int[] getGamePadMouseButtonsMouse2() {
            return uiEngineState.uiEngineConfig.input_gamePadMouseButtonsMouse2;
        }

        public void setGamePadMouseButtonsMouse2(int[] input_gamePadMouseButtonsMouse2) {
            uiEngineState.uiEngineConfig.input_gamePadMouseButtonsMouse2 = input_gamePadMouseButtonsMouse2;
        }

        public int[] getGamePadMouseButtonsMouse3() {
            return uiEngineState.uiEngineConfig.input_gamePadMouseButtonsMouse3;
        }

        public void setGamePadMouseButtonsMouse3(int[] input_gamePadMouseButtonsMouse3) {
            uiEngineState.uiEngineConfig.input_gamePadMouseButtonsMouse3 = input_gamePadMouseButtonsMouse3;
        }

        public int[] getGamePadMouseButtonsMouse4() {
            return uiEngineState.uiEngineConfig.input_gamePadMouseButtonsMouse4;
        }

        public void setGamePadMouseButtonsMouse4(int[] input_gamePadMouseButtonsMouse4) {
            uiEngineState.uiEngineConfig.input_gamePadMouseButtonsMouse4 = input_gamePadMouseButtonsMouse4;
        }

        public int[] getGamePadMouseButtonsMouse5() {
            return uiEngineState.uiEngineConfig.input_gamePadMouseButtonsMouse5;
        }

        public void setGamePadMouseButtonsMouse5(int[] input_gamePadMouseButtonsMouse5) {
            uiEngineState.uiEngineConfig.input_gamePadMouseButtonsMouse5 = input_gamePadMouseButtonsMouse5;
        }

        public int[] getGamePadMouseButtonsScrollUp() {
            return uiEngineState.uiEngineConfig.input_gamePadMouseButtonsScrollUp;
        }

        public void setGamePadMouseButtonsScrollUp(int[] input_gamePadMouseButtonsScrollUp) {
            uiEngineState.uiEngineConfig.input_gamePadMouseButtonsScrollUp = input_gamePadMouseButtonsScrollUp;
        }

        public int[] getGamePadMouseButtonsScrollDown() {
            return uiEngineState.uiEngineConfig.input_gamePadMouseButtonsScrollDown;
        }

        public void setGamePadMouseButtonsScrollDown(int[] input_gamePadMouseButtonsScrollDown) {
            uiEngineState.uiEngineConfig.input_gamePadMouseButtonsScrollDown = input_gamePadMouseButtonsScrollDown;
        }
    }

    public final class APIWindowConfig {
        APIWindowConfig(){
        }

        public boolean isDefaultEnforceScreenBounds() {
            return uiEngineState.uiEngineConfig.window_defaultEnforceScreenBounds;
        }

        public void setDefaultEnforceScreenBounds(boolean windows_defaultEnforceScreenBounds) {
            uiEngineState.uiEngineConfig.window_defaultEnforceScreenBounds = windows_defaultEnforceScreenBounds;
        }

        public Color getDefaultColor() {
            return uiEngineState.uiEngineConfig.window_defaultColor;
        }

        public void setDefaultColor(Color windows_defaultColor) {
            uiEngineState.uiEngineConfig.window_defaultColor = windows_defaultColor;
        }

        public CMediaFont getDefaultFont() {
            return uiEngineState.uiEngineConfig.window_defaultFont;
        }

        public void setDefaultFont(CMediaFont windows_defaultFont) {
            uiEngineState.uiEngineConfig.window_defaultFont = windows_defaultFont;
        }
    }

    public final class APIComponentConfig {
        APIComponentConfig(){
        }
        public Color getDefaultColor() {
            return uiEngineState.uiEngineConfig.component_defaultColor;
        }

        public void setDefaultColor(Color components_defaultColor) {
            uiEngineState.uiEngineConfig.component_defaultColor = components_defaultColor;
        }

        public CMediaFont getDefaultFont() {
            return uiEngineState.uiEngineConfig.component_defaultFont;
        }

        public void setDefaultFont(CMediaFont components_defaultFont) {
            uiEngineState.uiEngineConfig.component_defaultFont = components_defaultFont;
        }

        public int getAppViewportDefaultUpdateTime() {
            return uiEngineState.uiEngineConfig.component_appViewportDefaultUpdateTime;
        }

        public void setAppViewportDefaultUpdateTime(int appViewport_defaultUpdateTime) {
            uiEngineState.uiEngineConfig.component_appViewportDefaultUpdateTime = appViewport_defaultUpdateTime;
        }

        public float getListDragAlpha() {
            return uiEngineState.uiEngineConfig.component_listDragAlpha;
        }

        public void setListDragAlpha(float list_dragAlpha) {
            uiEngineState.uiEngineConfig.component_listDragAlpha = list_dragAlpha;
        }

        public float getGridDragAlpha() {
            return uiEngineState.uiEngineConfig.component_gridDragAlpha;
        }

        public void setGridDragAlpha(float grid_dragAlpha) {
            uiEngineState.uiEngineConfig.component_gridDragAlpha = grid_dragAlpha;
        }

        public float getKnobSensitivity() {
            return uiEngineState.uiEngineConfig.component_knobSensitivity;
        }

        public void setKnobSensitivity(float knob_sensitivity) {
            uiEngineState.uiEngineConfig.component_knobSensitivity = knob_sensitivity;
        }

        public float getScrollbarSensitivity() {
            return uiEngineState.uiEngineConfig.component_scrollbarSensitivity;
        }

        public void setScrollbarSensitivity(float scrollbar_sensitivity) {
            uiEngineState.uiEngineConfig.component_scrollbarSensitivity = scrollbar_sensitivity;
        }

        public int getMapOverlayDefaultFadeoutTime() {
            return uiEngineState.uiEngineConfig.component_mapOverlayDefaultFadeoutTime;
        }

        public void setMapOverlayDefaultFadeoutTime(int mapOverlay_defaultFadeoutTime) {
            uiEngineState.uiEngineConfig.component_mapOverlayDefaultFadeoutTime = mapOverlay_defaultFadeoutTime;
        }

        public char[] getTextfieldDefaultAllowedCharacters() {
            return uiEngineState.uiEngineConfig.component_textFieldDefaultAllowedCharacters;
        }

        public void setTextfieldDefaultAllowedCharacters(char[] textField_defaultAllowedCharacters) {
            uiEngineState.uiEngineConfig.component_textFieldDefaultAllowedCharacters = textField_defaultAllowedCharacters;
        }

    }

    public final class APINotificationsConfig {
        APINotificationsConfig(){
        }

        public int getMax() {
            return uiEngineState.uiEngineConfig.notification_max;
        }

        public void setMax(int notifications_max) {
            uiEngineState.uiEngineConfig.notification_max = notifications_max;
        }

        public int getNotifications_defaultDisplayTime() {
            return uiEngineState.uiEngineConfig.notification_defaultDisplayTime;
        }

        public void setDefaultDisplayTime(int notifications_defaultDisplayTime) {
            uiEngineState.uiEngineConfig.notification_defaultDisplayTime = notifications_defaultDisplayTime;
        }

        public CMediaFont getNotifications_defaultFont() {
            return uiEngineState.uiEngineConfig.notification_defaultFont;
        }

        public void setDefaultFont(CMediaFont notifications_defaultFont) {
            uiEngineState.uiEngineConfig.notification_defaultFont = notifications_defaultFont;
        }

        public Color getNotifications_defaultColor() {
            return uiEngineState.uiEngineConfig.notification_defaultColor;
        }

        public void setDefaultColor(Color notifications_defaultColor) {
            uiEngineState.uiEngineConfig.notification_defaultColor = notifications_defaultColor;
        }

        public int getNotifications_fadeoutTime() {
            return uiEngineState.uiEngineConfig.notification_fadeoutTime;
        }

        public void setFadeoutTime(int notifications_fadeoutTime) {
            uiEngineState.uiEngineConfig.notification_fadeoutTime = notifications_fadeoutTime;
        }

        public float getNotifications_scrollSpeed() {
            return uiEngineState.uiEngineConfig.notification_scrollSpeed;
        }

        public void setScrollSpeed(float notifications_scrollSpeed) {
            uiEngineState.uiEngineConfig.notification_scrollSpeed = notifications_scrollSpeed;
        }
    }

    public final class APITooltipConfig {

        APITooltipConfig(){
        }

        public Color getDefaultColor() {
            return uiEngineState.uiEngineConfig.tooltip_defaultColor;
        }

        public void setDefaultColor(Color tooltip_defaultColor) {
            uiEngineState.uiEngineConfig.tooltip_defaultColor = tooltip_defaultColor;
        }

        public CMediaFont getDefaultFont() {
            return uiEngineState.uiEngineConfig.tooltip_defaultFont;
        }

        public void setDefaultFont(CMediaFont tooltip_defaultFont) {
            uiEngineState.uiEngineConfig.tooltip_defaultFont = tooltip_defaultFont;
        }

        public int getFadeInTime() {
            return uiEngineState.uiEngineConfig.tooltip_FadeInTime;
        }

        public void setFadeInTime(int tooltip_FadeInTime) {
            uiEngineState.uiEngineConfig.tooltip_FadeInTime = tooltip_FadeInTime;
        }

        public int getFadeInDelayTime() {
            return uiEngineState.uiEngineConfig.tooltip_FadeInDelayTime;
        }

        public void setFadeInDelayTime(int tooltip_FadeInDelayTime) {
            uiEngineState.uiEngineConfig.tooltip_FadeInDelayTime = tooltip_FadeInDelayTime;
        }

    }

    public final class APIMouseTextInputConfig {

        APIMouseTextInputConfig(){
        }

        public CMediaFont getDefaultFont() {
            return uiEngineState.uiEngineConfig.mouseTextInput_defaultFont;
        }

        public void setDefaultFont(CMediaFont mouseTextInput_defaultFont) {
            uiEngineState.uiEngineConfig.mouseTextInput_defaultFont = mouseTextInput_defaultFont;
        }

        public char[] getDefaultLowerCaseCharacters() {
            return uiEngineState.uiEngineConfig.mouseTextInput_defaultLowerCaseCharacters;
        }

        public void setDefaultLowerCaseCharacters(char[] mouseTextInput_defaultLowerCaseCharacters) {
            uiEngineState.uiEngineConfig.mouseTextInput_defaultLowerCaseCharacters = mouseTextInput_defaultLowerCaseCharacters;
        }

        public char[] getDefaultUpperCaseCharacters() {
            return uiEngineState.uiEngineConfig.mouseTextInput_defaultUpperCaseCharacters;
        }

        public void setDefaultUpperCaseCharacters(char[] mouseTextInput_defaultUpperCaseCharacters) {
            uiEngineState.uiEngineConfig.mouseTextInput_defaultUpperCaseCharacters = mouseTextInput_defaultUpperCaseCharacters;
        }
    }


}
