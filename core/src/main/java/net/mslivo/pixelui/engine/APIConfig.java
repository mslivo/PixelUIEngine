package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.pixelui.media.MediaManager;
import net.mslivo.pixelui.media.CMediaFont;
import net.mslivo.pixelui.media.CMediaSprite;

public final class APIConfig {
    private final API api;
    private final UIEngineState uiEngineState;
    private final UICommonUtils uiCommonUtils;
    private final MediaManager mediaManager;
    private final UIEngineConfig uiEngineConfig;

    public final APIUIConfig ui;
    public final APIInputConfig input;
    public final APIWindowConfig window;
    public final APIComponentConfig component;
    public final APINotificationsConfig notification;
    public final APITooltipConfig tooltip;
    public final APIMouseTextInputConfig mouseTextInput;
    public final APIContextMenuConfig contextMenu;

    APIConfig(API api, UIEngineState uiEngineState, UICommonUtils uiCommonUtils, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.uiCommonUtils = uiCommonUtils;
        this.mediaManager = mediaManager;
        this.uiEngineConfig = uiEngineState.config;

        this.ui = new APIUIConfig();
        this.input = new APIInputConfig();
        this.window = new APIWindowConfig();
        this.component = new APIComponentConfig();
        this.notification = new APINotificationsConfig();
        this.tooltip = new APITooltipConfig();
        this.mouseTextInput = new APIMouseTextInputConfig();
        this.contextMenu = new APIContextMenuConfig();
    }

    public final class APIUIConfig {
        APIUIConfig(){
        }

        public CMediaSprite getCursor() {
            return uiEngineConfig.ui_cursor;
        }

        public void setCursor(CMediaSprite ui_cursor) {
            uiEngineConfig.ui_cursor = ui_cursor;
        }

        public boolean isKeyInteractionsDisabled() {
            return uiEngineConfig.ui_keyInteractionsDisabled;
        }

        public void setKeyInteractionsDisabled(boolean ui_keyInteractionsDisabled) {
            uiEngineConfig.ui_keyInteractionsDisabled = ui_keyInteractionsDisabled;
        }

        public boolean isMouseInteractionsDisabled() {
            return uiEngineConfig.ui_mouseInteractionsDisabled;
        }

        public void setMouseInteractionsDisabled(boolean ui_mouseInteractionsDisabled) {
            uiEngineConfig.ui_mouseInteractionsDisabled = ui_mouseInteractionsDisabled;
        }

        public boolean isFoldWindowsOnDoubleClick() {
            return uiEngineConfig.ui_foldWindowsOnDoubleClick;
        }

        public void setFoldWindowsOnDoubleClick(boolean ui_foldWindowsOnDoubleClick) {
            uiEngineConfig.ui_foldWindowsOnDoubleClick = ui_foldWindowsOnDoubleClick;
        }

        public UIEngineConfig.AnimationTimerFunction getAnimationTimerFunction() {
            return uiEngineConfig.ui_animationTimerFunction;
        }

        public void setAnimationTimerFunction(UIEngineConfig.AnimationTimerFunction ui_animationTimerFunction) {
            uiEngineConfig.ui_animationTimerFunction = ui_animationTimerFunction;
        }

        public void setFont(CMediaFont cMediaFont){
            uiEngineConfig.ui_font = cMediaFont;
        }

        public CMediaFont getFont(){
            return uiEngineConfig.ui_font;
        }

        public void setFontDefaultColor(Color color){
            uiEngineConfig.ui_font_defaultColor.set(color);
        }

        public Color getFontDefaultColor(){
            return uiEngineConfig.ui_font_defaultColor.cpy();
        }

    }

    public final class APIInputConfig {
        APIInputConfig(){
        }

        public float getEmulatedMouseCursorSpeed() {
            return uiEngineConfig.input_emulatedMouseCursorSpeed;
        }

        public void setEmulatedMouseCursorSpeed(float input_emulatedMouseCursorSpeed) {
            uiEngineConfig.input_emulatedMouseCursorSpeed = input_emulatedMouseCursorSpeed;
        }

        public boolean isHardwareMouseEnabled() {
            return uiEngineConfig.input_hardwareMouseEnabled;
        }

        public void setHardwareMouseEnabled(boolean input_hardwareMouseEnabled) {
            uiEngineConfig.input_hardwareMouseEnabled = input_hardwareMouseEnabled;
        }

        public boolean isInput_keyboardMouseEnabled() {
            return uiEngineConfig.input_keyboardMouseEnabled;
        }

        public void setKeyboardMouseEnabled(boolean input_keyboardMouseEnabled) {
            uiEngineConfig.input_keyboardMouseEnabled = input_keyboardMouseEnabled;
        }

        public int[] getKeyboardMouseButtonsUp() {
            return uiEngineConfig.input_keyboardMouseButtonsUp;
        }

        public void setKeyboardMouseButtonsUp(int[] input_keyboardMouseButtonsUp) {
            uiEngineConfig.input_keyboardMouseButtonsUp = input_keyboardMouseButtonsUp;
        }

        public int[] getKeyboardMouseButtonsDown() {
            return uiEngineConfig.input_keyboardMouseButtonsDown;
        }

        public void setKeyboardMouseButtonsDown(int[] input_keyboardMouseButtonsDown) {
            uiEngineConfig.input_keyboardMouseButtonsDown = input_keyboardMouseButtonsDown;
        }

        public int[] getKeyboardMouseButtonsLeft() {
            return uiEngineConfig.input_keyboardMouseButtonsLeft;
        }

        public void setKeyboardMouseButtonsLeft(int[] input_keyboardMouseButtonsLeft) {
            uiEngineConfig.input_keyboardMouseButtonsLeft = input_keyboardMouseButtonsLeft;
        }

        public int[] getKeyboardMouseButtonsRight() {
            return uiEngineConfig.input_keyboardMouseButtonsRight;
        }

        public void setKeyboardMouseButtonsRight(int[] input_keyboardMouseButtonsRight) {
            uiEngineConfig.input_keyboardMouseButtonsRight = input_keyboardMouseButtonsRight;
        }

        public int[] getKeyboardMouseButtonsMouse1() {
            return uiEngineConfig.input_keyboardMouseButtonsMouse1;
        }

        public void setKeyboardMouseButtonsMouse1(int[] input_keyboardMouseButtonsMouse1) {
            uiEngineConfig.input_keyboardMouseButtonsMouse1 = input_keyboardMouseButtonsMouse1;
        }

        public int[] getKeyboardMouseButtonsMouse2() {
            return uiEngineConfig.input_keyboardMouseButtonsMouse2;
        }

        public void setKeyboardMouseButtonsMouse2(int[] input_keyboardMouseButtonsMouse2) {
            uiEngineConfig.input_keyboardMouseButtonsMouse2 = input_keyboardMouseButtonsMouse2;
        }

        public int[] getKeyboardMouseButtonsMouse3() {
            return uiEngineConfig.input_keyboardMouseButtonsMouse3;
        }

        public void setKeyboardMouseButtonsMouse3(int[] input_keyboardMouseButtonsMouse3) {
            uiEngineConfig.input_keyboardMouseButtonsMouse3 = input_keyboardMouseButtonsMouse3;
        }

        public int[] getKeyboardMouseButtonsMouse4() {
            return uiEngineConfig.input_keyboardMouseButtonsMouse4;
        }

        public void setKeyboardMouseButtonsMouse4(int[] input_keyboardMouseButtonsMouse4) {
            uiEngineConfig.input_keyboardMouseButtonsMouse4 = input_keyboardMouseButtonsMouse4;
        }

        public int[] getKeyboardMouseButtonsMouse5() {
            return uiEngineConfig.input_keyboardMouseButtonsMouse5;
        }

        public void setKeyboardMouseButtonsMouse5(int[] input_keyboardMouseButtonsMouse5) {
            uiEngineConfig.input_keyboardMouseButtonsMouse5 = input_keyboardMouseButtonsMouse5;
        }

        public int[] getKeyboardMouseButtonsScrollUp() {
            return uiEngineConfig.input_keyboardMouseButtonsScrollUp;
        }

        public void setKeyboardMouseButtonsScrollUp(int[] input_keyboardMouseButtonsScrollUp) {
            uiEngineConfig.input_keyboardMouseButtonsScrollUp = input_keyboardMouseButtonsScrollUp;
        }

        public int[] getKeyboardMouseButtonsScrollDown() {
            return uiEngineConfig.input_keyboardMouseButtonsScrollDown;
        }

        public void setKeyboardMouseButtonsScrollDown(int[] input_keyboardMouseButtonsScrollDown) {
            uiEngineConfig.input_keyboardMouseButtonsScrollDown = input_keyboardMouseButtonsScrollDown;
        }

        public boolean isGamePadMouseEnabled() {
            return uiEngineConfig.input_gamePadMouseEnabled;
        }

        public void setGamePadMouseEnabled(boolean input_gamePadMouseEnabled) {
            uiEngineConfig.input_gamePadMouseEnabled = input_gamePadMouseEnabled;
        }

        public float getGamePadMouseJoystickDeadZone() {
            return uiEngineConfig.input_gamePadMouseJoystickDeadZone;
        }

        public void setGamePadMouseJoystickDeadZone(float input_gamePadMouseJoystickDeadZone) {
            uiEngineConfig.input_gamePadMouseJoystickDeadZone = input_gamePadMouseJoystickDeadZone;
        }

        public boolean isInput_gamePadMouseStickLeftEnabled() {
            return uiEngineConfig.input_gamePadMouseStickLeftEnabled;
        }

        public void setGamePadMouseStickLeftEnabled(boolean input_gamePadMouseStickLeftEnabled) {
            uiEngineConfig.input_gamePadMouseStickLeftEnabled = input_gamePadMouseStickLeftEnabled;
        }

        public boolean isGamePadMouseStickRightEnabled() {
            return uiEngineConfig.input_gamePadMouseStickRightEnabled;
        }

        public void setGamePadMouseStickRightEnabled(boolean input_gamePadMouseStickRightEnabled) {
            uiEngineConfig.input_gamePadMouseStickRightEnabled = input_gamePadMouseStickRightEnabled;
        }

        public int[] getGamePadMouseButtonsMouse1() {
            return uiEngineConfig.input_gamePadMouseButtonsMouse1;
        }

        public void setGamePadMouseButtonsMouse1(int[] input_gamePadMouseButtonsMouse1) {
            uiEngineConfig.input_gamePadMouseButtonsMouse1 = input_gamePadMouseButtonsMouse1;
        }

        public int[] getGamePadMouseButtonsMouse2() {
            return uiEngineConfig.input_gamePadMouseButtonsMouse2;
        }

        public void setGamePadMouseButtonsMouse2(int[] input_gamePadMouseButtonsMouse2) {
            uiEngineConfig.input_gamePadMouseButtonsMouse2 = input_gamePadMouseButtonsMouse2;
        }

        public int[] getGamePadMouseButtonsMouse3() {
            return uiEngineConfig.input_gamePadMouseButtonsMouse3;
        }

        public void setGamePadMouseButtonsMouse3(int[] input_gamePadMouseButtonsMouse3) {
            uiEngineConfig.input_gamePadMouseButtonsMouse3 = input_gamePadMouseButtonsMouse3;
        }

        public int[] getGamePadMouseButtonsMouse4() {
            return uiEngineConfig.input_gamePadMouseButtonsMouse4;
        }

        public void setGamePadMouseButtonsMouse4(int[] input_gamePadMouseButtonsMouse4) {
            uiEngineConfig.input_gamePadMouseButtonsMouse4 = input_gamePadMouseButtonsMouse4;
        }

        public int[] getGamePadMouseButtonsMouse5() {
            return uiEngineConfig.input_gamePadMouseButtonsMouse5;
        }

        public void setGamePadMouseButtonsMouse5(int[] input_gamePadMouseButtonsMouse5) {
            uiEngineConfig.input_gamePadMouseButtonsMouse5 = input_gamePadMouseButtonsMouse5;
        }

        public int[] getGamePadMouseButtonsScrollUp() {
            return uiEngineConfig.input_gamePadMouseButtonsScrollUp;
        }

        public void setGamePadMouseButtonsScrollUp(int[] input_gamePadMouseButtonsScrollUp) {
            uiEngineConfig.input_gamePadMouseButtonsScrollUp = input_gamePadMouseButtonsScrollUp;
        }

        public int[] getGamePadMouseButtonsScrollDown() {
            return uiEngineConfig.input_gamePadMouseButtonsScrollDown;
        }

        public void setGamePadMouseButtonsScrollDown(int[] input_gamePadMouseButtonsScrollDown) {
            uiEngineConfig.input_gamePadMouseButtonsScrollDown = input_gamePadMouseButtonsScrollDown;
        }
    }

    public final class APIWindowConfig {
        APIWindowConfig(){
        }

        public boolean isDefaultEnforceScreenBounds() {
            return uiEngineConfig.window_defaultEnforceScreenBounds;
        }

        public void setDefaultEnforceScreenBounds(boolean windows_defaultEnforceScreenBounds) {
            uiEngineConfig.window_defaultEnforceScreenBounds = windows_defaultEnforceScreenBounds;
        }

        public Color getDefaultColor() {
            return uiEngineConfig.window_defaultColor;
        }

        public void setDefaultColor(Color windows_defaultColor) {
            uiEngineConfig.window_defaultColor = windows_defaultColor;
        }

    }

    public final class APIComponentConfig {
        APIComponentConfig(){
        }
        public Color getDefaultColor() {
            return uiEngineConfig.component_defaultColor.cpy();
        }

        public void setDefaultColor(Color components_defaultColor) {
            uiEngineConfig.component_defaultColor = components_defaultColor.cpy();
        }

        public int getAppViewportDefaultUpdateTime() {
            return uiEngineConfig.component_appViewportDefaultUpdateTime;
        }

        public void setAppViewportDefaultUpdateTime(int appViewport_defaultUpdateTime) {
            uiEngineConfig.component_appViewportDefaultUpdateTime = appViewport_defaultUpdateTime;
        }

        public float getListDragAlpha() {
            return uiEngineConfig.component_listDragAlpha;
        }

        public void setListDragAlpha(float list_dragAlpha) {
            uiEngineConfig.component_listDragAlpha = list_dragAlpha;
        }

        public float getGridDragAlpha() {
            return uiEngineConfig.component_gridDragAlpha;
        }

        public void setGridDragAlpha(float grid_dragAlpha) {
            uiEngineConfig.component_gridDragAlpha = grid_dragAlpha;
        }

        public float getKnobSensitivity() {
            return uiEngineConfig.component_knobSensitivity;
        }

        public void setKnobSensitivity(float knob_sensitivity) {
            uiEngineConfig.component_knobSensitivity = knob_sensitivity;
        }

        public void setTextFieldDefaultMarkerColor(Color color) {
            uiEngineConfig.component_textFieldDefaultMarkerColor = color;
        }

        public Color getTextFieldDefaultMarkerColor(Color color) {
            return color.cpy();
        }

        public float getScrollbarSensitivity() {
            return uiEngineConfig.component_scrollbarSensitivity;
        }

        public void setScrollbarSensitivity(float scrollbar_sensitivity) {
            uiEngineConfig.component_scrollbarSensitivity = scrollbar_sensitivity;
        }

        public float getMapOverlayDefaultFadeoutSpeed() {
            return uiEngineConfig.component_mapOverlayDefaultFadeoutSpeed;
        }

        public void setMapOverlayDefaultFadeoutSpeed(float mapOverlayDefaultFadeoutSpeed) {
            uiEngineConfig.component_mapOverlayDefaultFadeoutSpeed = mapOverlayDefaultFadeoutSpeed;
        }

        public char[] getTextfieldDefaultAllowedCharacters() {
            return uiEngineConfig.component_textFieldDefaultAllowedCharacters;
        }

        public void setTextfieldDefaultAllowedCharacters(char[] textField_defaultAllowedCharacters) {
            uiEngineConfig.component_textFieldDefaultAllowedCharacters = textField_defaultAllowedCharacters;
        }

    }

    public final class APINotificationsConfig {
        APINotificationsConfig(){
        }

        public final APINotificationsTopConfig top = new APINotificationsTopConfig();
        public final APINotificationsTooltipConfig tooltip = new APINotificationsTooltipConfig();

        public final class APINotificationsTopConfig {
            public int getMax() {
                return uiEngineConfig.notification_max;
            }

            public void setMax(int notifications_max) {
                uiEngineConfig.notification_max = notifications_max;
            }

            public int getDefaultDisplayTime() {
                return uiEngineConfig.notification_defaultDisplayTime;
            }

            public void setDefaultDisplayTime(int notifications_defaultDisplayTime) {
                uiEngineConfig.notification_defaultDisplayTime = notifications_defaultDisplayTime;
            }

            public Color getDefaultColor() {
                return uiEngineConfig.notification_defaultColor.cpy();
            }

            public void setDefaultColor(Color notifications_defaultColor) {
                uiEngineConfig.notification_defaultColor = notifications_defaultColor.cpy();
            }

            public int getFoldTime() {
                return uiEngineConfig.notification_foldTime;
            }

            public void setFoldTime(int notifications_fadeoutTime) {
                uiEngineConfig.notification_foldTime = Math.max(notifications_fadeoutTime,0);
            }
        }

        public final class APINotificationsTooltipConfig {
            public int getDefaultDisplayTime() {
                return uiEngineConfig.notification_tooltip_defaultDisplayTime;
            }

            public void setDefaultDisplayTime(int notifications_defaultDisplayTime) {
                uiEngineConfig.notification_tooltip_defaultDisplayTime = notifications_defaultDisplayTime;
            }

            public int getFadeoutTime() {
                return uiEngineConfig.notification_tooltip_fadeoutTime;
            }

            public void setFadeoutTime(int notifications_fadeoutTime) {
                uiEngineConfig.notification_tooltip_fadeoutTime = Math.max(notifications_fadeoutTime,0);
            }
        }

    }

    public final class APITooltipConfig {

        APITooltipConfig(){
        }

        public Color getDefaultCellColor() {
            return uiEngineConfig.tooltip_defaultCellColor.cpy();
        }

        public void setDefaultCellColor(Color tooltip_defaultColor) {
            uiEngineConfig.tooltip_defaultCellColor = tooltip_defaultColor.cpy();
        }

        public float getFadeInTime() {
            return uiEngineConfig.tooltip_FadeInSpeed;
        }

        public void setFadeInSpeed(float fadeInSpeed) {
            uiEngineConfig.tooltip_FadeInSpeed = fadeInSpeed;
        }

        public int getFadeInDelay() {
            return uiEngineConfig.tooltip_FadeInDelay;
        }

        public void setFadeInDelay(int fadeInDelay) {
            uiEngineConfig.tooltip_FadeInDelay = fadeInDelay;
        }

        public float getFadeOutSpeed() {
            return uiEngineConfig.tooltip_FadeoutSpeed;
        }

        public void setFadeOutSpeed(float fadeoutSpeed) {
            uiEngineConfig.tooltip_FadeoutSpeed = fadeoutSpeed;
        }
    }

    public final class APIContextMenuConfig {

        APIContextMenuConfig(){
        }

        public Color getDefaultColor() {
            return uiEngineConfig.contextMenu_defaultColor.cpy();
        }

        public void setDefaultColor(Color tooltip_defaultColor) {
            uiEngineConfig.contextMenu_defaultColor = tooltip_defaultColor.cpy();
        }

    }

    public final class APIMouseTextInputConfig {

        APIMouseTextInputConfig(){
        }


        public char[] getDefaultLowerCaseCharacters() {
            return uiEngineConfig.mouseTextInput_defaultLowerCaseCharacters;
        }

        public void setDefaultLowerCaseCharacters(char[] mouseTextInput_defaultLowerCaseCharacters) {
            uiEngineConfig.mouseTextInput_defaultLowerCaseCharacters = mouseTextInput_defaultLowerCaseCharacters;
        }

        public char[] getDefaultUpperCaseCharacters() {
            return uiEngineConfig.mouseTextInput_defaultUpperCaseCharacters;
        }

        public void setDefaultUpperCaseCharacters(char[] mouseTextInput_defaultUpperCaseCharacters) {
            uiEngineConfig.mouseTextInput_defaultUpperCaseCharacters = mouseTextInput_defaultUpperCaseCharacters;
        }
    }


}
