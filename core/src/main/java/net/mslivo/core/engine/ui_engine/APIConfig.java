package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;

public final class APIConfig {
    private final API api;
    private final UIEngineState uiEngineState;
    private final MediaManager mediaManager;
    private final UIConfig uiConfig;

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
        this.uiConfig = uiEngineState.config;
        this.ui = new APIUIConfig();
        this.input = new APIInputConfig();
        this.window = new APIWindowConfig();
        this.component = new APIComponentConfig();
        this.notification = new APINotificationsConfig();
        this.tooltip = new APITooltipConfig();
        this.mouseTextInput = new APIMouseTextInputConfig();
    }

    public void setAllDefaultFont(CMediaFont default_font){
        window.setDefaultFont(default_font);
        component.setDefaultFont(default_font);
        tooltip.setDefaultFont(default_font);
        mouseTextInput.setDefaultFont(default_font);
    }

    public final class APIUIConfig {
        APIUIConfig(){
        }

        public CMediaSprite getCursor() {
            return uiConfig.ui_cursor;
        }

        public void setCursor(CMediaSprite ui_cursor) {
            uiConfig.ui_cursor = ui_cursor;
        }

        public boolean isKeyInteractionsDisabled() {
            return uiConfig.ui_keyInteractionsDisabled;
        }

        public void setKeyInteractionsDisabled(boolean ui_keyInteractionsDisabled) {
            uiConfig.ui_keyInteractionsDisabled = ui_keyInteractionsDisabled;
        }

        public boolean isMouseInteractionsDisabled() {
            return uiConfig.ui_mouseInteractionsDisabled;
        }

        public void setMouseInteractionsDisabled(boolean ui_mouseInteractionsDisabled) {
            uiConfig.ui_mouseInteractionsDisabled = ui_mouseInteractionsDisabled;
        }

        public boolean isFoldWindowsOnDoubleClick() {
            return uiConfig.ui_foldWindowsOnDoubleClick;
        }

        public void setFoldWindowsOnDoubleClick(boolean ui_foldWindowsOnDoubleClick) {
            uiConfig.ui_foldWindowsOnDoubleClick = ui_foldWindowsOnDoubleClick;
        }

        public UIConfig.AnimationTimerFunction getAnimationTimerFunction() {
            return uiConfig.ui_animationTimerFunction;
        }

        public void setAnimationTimerFunction(UIConfig.AnimationTimerFunction ui_animationTimerFunction) {
            uiConfig.ui_animationTimerFunction = ui_animationTimerFunction;
        }

    }

    public final class APIInputConfig {
        APIInputConfig(){
        }

        public float getEmulatedMouseCursorSpeed() {
            return uiConfig.input_emulatedMouseCursorSpeed;
        }

        public void setEmulatedMouseCursorSpeed(float input_emulatedMouseCursorSpeed) {
            uiConfig.input_emulatedMouseCursorSpeed = input_emulatedMouseCursorSpeed;
        }

        public boolean isHardwareMouseEnabled() {
            return uiConfig.input_hardwareMouseEnabled;
        }

        public void setHardwareMouseEnabled(boolean input_hardwareMouseEnabled) {
            uiConfig.input_hardwareMouseEnabled = input_hardwareMouseEnabled;
        }

        public boolean isInput_keyboardMouseEnabled() {
            return uiConfig.input_keyboardMouseEnabled;
        }

        public void setKeyboardMouseEnabled(boolean input_keyboardMouseEnabled) {
            uiConfig.input_keyboardMouseEnabled = input_keyboardMouseEnabled;
        }

        public int[] getKeyboardMouseButtonsUp() {
            return uiConfig.input_keyboardMouseButtonsUp;
        }

        public void setKeyboardMouseButtonsUp(int[] input_keyboardMouseButtonsUp) {
            uiConfig.input_keyboardMouseButtonsUp = input_keyboardMouseButtonsUp;
        }

        public int[] getKeyboardMouseButtonsDown() {
            return uiConfig.input_keyboardMouseButtonsDown;
        }

        public void setKeyboardMouseButtonsDown(int[] input_keyboardMouseButtonsDown) {
            uiConfig.input_keyboardMouseButtonsDown = input_keyboardMouseButtonsDown;
        }

        public int[] getKeyboardMouseButtonsLeft() {
            return uiConfig.input_keyboardMouseButtonsLeft;
        }

        public void setKeyboardMouseButtonsLeft(int[] input_keyboardMouseButtonsLeft) {
            uiConfig.input_keyboardMouseButtonsLeft = input_keyboardMouseButtonsLeft;
        }

        public int[] getKeyboardMouseButtonsRight() {
            return uiConfig.input_keyboardMouseButtonsRight;
        }

        public void setKeyboardMouseButtonsRight(int[] input_keyboardMouseButtonsRight) {
            uiConfig.input_keyboardMouseButtonsRight = input_keyboardMouseButtonsRight;
        }

        public int[] getKeyboardMouseButtonsMouse1() {
            return uiConfig.input_keyboardMouseButtonsMouse1;
        }

        public void setKeyboardMouseButtonsMouse1(int[] input_keyboardMouseButtonsMouse1) {
            uiConfig.input_keyboardMouseButtonsMouse1 = input_keyboardMouseButtonsMouse1;
        }

        public int[] getKeyboardMouseButtonsMouse2() {
            return uiConfig.input_keyboardMouseButtonsMouse2;
        }

        public void setKeyboardMouseButtonsMouse2(int[] input_keyboardMouseButtonsMouse2) {
            uiConfig.input_keyboardMouseButtonsMouse2 = input_keyboardMouseButtonsMouse2;
        }

        public int[] getKeyboardMouseButtonsMouse3() {
            return uiConfig.input_keyboardMouseButtonsMouse3;
        }

        public void setKeyboardMouseButtonsMouse3(int[] input_keyboardMouseButtonsMouse3) {
            uiConfig.input_keyboardMouseButtonsMouse3 = input_keyboardMouseButtonsMouse3;
        }

        public int[] getKeyboardMouseButtonsMouse4() {
            return uiConfig.input_keyboardMouseButtonsMouse4;
        }

        public void setKeyboardMouseButtonsMouse4(int[] input_keyboardMouseButtonsMouse4) {
            uiConfig.input_keyboardMouseButtonsMouse4 = input_keyboardMouseButtonsMouse4;
        }

        public int[] getKeyboardMouseButtonsMouse5() {
            return uiConfig.input_keyboardMouseButtonsMouse5;
        }

        public void setKeyboardMouseButtonsMouse5(int[] input_keyboardMouseButtonsMouse5) {
            uiConfig.input_keyboardMouseButtonsMouse5 = input_keyboardMouseButtonsMouse5;
        }

        public int[] getKeyboardMouseButtonsScrollUp() {
            return uiConfig.input_keyboardMouseButtonsScrollUp;
        }

        public void setKeyboardMouseButtonsScrollUp(int[] input_keyboardMouseButtonsScrollUp) {
            uiConfig.input_keyboardMouseButtonsScrollUp = input_keyboardMouseButtonsScrollUp;
        }

        public int[] getKeyboardMouseButtonsScrollDown() {
            return uiConfig.input_keyboardMouseButtonsScrollDown;
        }

        public void setKeyboardMouseButtonsScrollDown(int[] input_keyboardMouseButtonsScrollDown) {
            uiConfig.input_keyboardMouseButtonsScrollDown = input_keyboardMouseButtonsScrollDown;
        }

        public boolean isGamePadMouseEnabled() {
            return uiConfig.input_gamePadMouseEnabled;
        }

        public void setGamePadMouseEnabled(boolean input_gamePadMouseEnabled) {
            uiConfig.input_gamePadMouseEnabled = input_gamePadMouseEnabled;
        }

        public float getGamePadMouseJoystickDeadZone() {
            return uiConfig.input_gamePadMouseJoystickDeadZone;
        }

        public void setGamePadMouseJoystickDeadZone(float input_gamePadMouseJoystickDeadZone) {
            uiConfig.input_gamePadMouseJoystickDeadZone = input_gamePadMouseJoystickDeadZone;
        }

        public boolean isInput_gamePadMouseStickLeftEnabled() {
            return uiConfig.input_gamePadMouseStickLeftEnabled;
        }

        public void setGamePadMouseStickLeftEnabled(boolean input_gamePadMouseStickLeftEnabled) {
            uiConfig.input_gamePadMouseStickLeftEnabled = input_gamePadMouseStickLeftEnabled;
        }

        public boolean isGamePadMouseStickRightEnabled() {
            return uiConfig.input_gamePadMouseStickRightEnabled;
        }

        public void setGamePadMouseStickRightEnabled(boolean input_gamePadMouseStickRightEnabled) {
            uiConfig.input_gamePadMouseStickRightEnabled = input_gamePadMouseStickRightEnabled;
        }

        public int[] getGamePadMouseButtonsMouse1() {
            return uiConfig.input_gamePadMouseButtonsMouse1;
        }

        public void setGamePadMouseButtonsMouse1(int[] input_gamePadMouseButtonsMouse1) {
            uiConfig.input_gamePadMouseButtonsMouse1 = input_gamePadMouseButtonsMouse1;
        }

        public int[] getGamePadMouseButtonsMouse2() {
            return uiConfig.input_gamePadMouseButtonsMouse2;
        }

        public void setGamePadMouseButtonsMouse2(int[] input_gamePadMouseButtonsMouse2) {
            uiConfig.input_gamePadMouseButtonsMouse2 = input_gamePadMouseButtonsMouse2;
        }

        public int[] getGamePadMouseButtonsMouse3() {
            return uiConfig.input_gamePadMouseButtonsMouse3;
        }

        public void setGamePadMouseButtonsMouse3(int[] input_gamePadMouseButtonsMouse3) {
            uiConfig.input_gamePadMouseButtonsMouse3 = input_gamePadMouseButtonsMouse3;
        }

        public int[] getGamePadMouseButtonsMouse4() {
            return uiConfig.input_gamePadMouseButtonsMouse4;
        }

        public void setGamePadMouseButtonsMouse4(int[] input_gamePadMouseButtonsMouse4) {
            uiConfig.input_gamePadMouseButtonsMouse4 = input_gamePadMouseButtonsMouse4;
        }

        public int[] getGamePadMouseButtonsMouse5() {
            return uiConfig.input_gamePadMouseButtonsMouse5;
        }

        public void setGamePadMouseButtonsMouse5(int[] input_gamePadMouseButtonsMouse5) {
            uiConfig.input_gamePadMouseButtonsMouse5 = input_gamePadMouseButtonsMouse5;
        }

        public int[] getGamePadMouseButtonsScrollUp() {
            return uiConfig.input_gamePadMouseButtonsScrollUp;
        }

        public void setGamePadMouseButtonsScrollUp(int[] input_gamePadMouseButtonsScrollUp) {
            uiConfig.input_gamePadMouseButtonsScrollUp = input_gamePadMouseButtonsScrollUp;
        }

        public int[] getGamePadMouseButtonsScrollDown() {
            return uiConfig.input_gamePadMouseButtonsScrollDown;
        }

        public void setGamePadMouseButtonsScrollDown(int[] input_gamePadMouseButtonsScrollDown) {
            uiConfig.input_gamePadMouseButtonsScrollDown = input_gamePadMouseButtonsScrollDown;
        }
    }

    public final class APIWindowConfig {
        APIWindowConfig(){
        }

        public boolean isDefaultEnforceScreenBounds() {
            return uiConfig.window_defaultEnforceScreenBounds;
        }

        public void setDefaultEnforceScreenBounds(boolean windows_defaultEnforceScreenBounds) {
            uiConfig.window_defaultEnforceScreenBounds = windows_defaultEnforceScreenBounds;
        }

        public Color getDefaultColor() {
            return uiConfig.window_defaultColor;
        }

        public void setDefaultColor(Color windows_defaultColor) {
            uiConfig.window_defaultColor = windows_defaultColor;
        }

        public CMediaFont getDefaultFont() {
            return uiConfig.window_defaultFont;
        }

        public void setDefaultFont(CMediaFont windows_defaultFont) {
            uiConfig.window_defaultFont = windows_defaultFont;
        }
    }

    public final class APIComponentConfig {
        APIComponentConfig(){
        }
        public Color getDefaultColor() {
            return uiConfig.component_defaultColor;
        }

        public void setDefaultColor(Color components_defaultColor) {
            uiConfig.component_defaultColor = components_defaultColor;
        }

        public CMediaFont getDefaultFont() {
            return uiConfig.component_defaultFont;
        }

        public void setDefaultFont(CMediaFont components_defaultFont) {
            uiConfig.component_defaultFont = components_defaultFont;
        }

        public int getAppViewportDefaultUpdateTime() {
            return uiConfig.component_appViewportDefaultUpdateTime;
        }

        public void setAppViewportDefaultUpdateTime(int appViewport_defaultUpdateTime) {
            uiConfig.component_appViewportDefaultUpdateTime = appViewport_defaultUpdateTime;
        }

        public float getListDragAlpha() {
            return uiConfig.component_listDragAlpha;
        }

        public void setListDragAlpha(float list_dragAlpha) {
            uiConfig.component_listDragAlpha = list_dragAlpha;
        }

        public float getGridDragAlpha() {
            return uiConfig.component_gridDragAlpha;
        }

        public void setGridDragAlpha(float grid_dragAlpha) {
            uiConfig.component_gridDragAlpha = grid_dragAlpha;
        }

        public float getKnobSensitivity() {
            return uiConfig.component_knobSensitivity;
        }

        public void setKnobSensitivity(float knob_sensitivity) {
            uiConfig.component_knobSensitivity = knob_sensitivity;
        }

        public float getScrollbarSensitivity() {
            return uiConfig.component_scrollbarSensitivity;
        }

        public void setScrollbarSensitivity(float scrollbar_sensitivity) {
            uiConfig.component_scrollbarSensitivity = scrollbar_sensitivity;
        }

        public float getMapOverlayDefaultFadeoutSpeed() {
            return uiConfig.component_mapOverlayDefaultFadeoutSpeed;
        }

        public void setMapOverlayDefaultFadeoutSpeed(float mapOverlayDefaultFadeoutSpeed) {
            uiConfig.component_mapOverlayDefaultFadeoutSpeed = mapOverlayDefaultFadeoutSpeed;
        }

        public char[] getTextfieldDefaultAllowedCharacters() {
            return uiConfig.component_textFieldDefaultAllowedCharacters;
        }

        public void setTextfieldDefaultAllowedCharacters(char[] textField_defaultAllowedCharacters) {
            uiConfig.component_textFieldDefaultAllowedCharacters = textField_defaultAllowedCharacters;
        }

    }

    public final class APINotificationsConfig {
        APINotificationsConfig(){
        }

        public int getMax() {
            return uiConfig.notification_max;
        }

        public void setMax(int notifications_max) {
            uiConfig.notification_max = notifications_max;
        }

        public int getDefaultDisplayTime() {
            return uiConfig.notification_defaultDisplayTime;
        }

        public void setDefaultDisplayTime(int notifications_defaultDisplayTime) {
            uiConfig.notification_defaultDisplayTime = notifications_defaultDisplayTime;
        }

        public CMediaFont getDefaultFont() {
            return uiConfig.notification_defaultFont;
        }

        public void setDefaultFont(CMediaFont notifications_defaultFont) {
            uiConfig.notification_defaultFont = notifications_defaultFont;
        }

        public Color getDefaultColor() {
            return uiConfig.notification_defaultColor;
        }

        public void setDefaultColor(Color notifications_defaultColor) {
            uiConfig.notification_defaultColor = notifications_defaultColor;
        }

        public int getFadeoutTime() {
            return uiConfig.notification_fadeoutTime;
        }

        public void setFadeoutTime(int notifications_fadeoutTime) {
            uiConfig.notification_fadeoutTime = Math.max(notifications_fadeoutTime,0);
        }

        public float getScrollSpeed() {
            return uiConfig.notification_scrollSpeed;
        }

        public void setScrollSpeed(float notifications_scrollSpeed) {
            uiConfig.notification_scrollSpeed = notifications_scrollSpeed;
        }
    }

    public final class APITooltipConfig {

        APITooltipConfig(){
        }

        public Color getDefaultColor() {
            return uiConfig.tooltip_defaultColor;
        }

        public void setDefaultColor(Color tooltip_defaultColor) {
            uiConfig.tooltip_defaultColor = tooltip_defaultColor;
        }

        public CMediaFont getDefaultFont() {
            return uiConfig.tooltip_defaultFont;
        }

        public void setDefaultFont(CMediaFont tooltip_defaultFont) {
            uiConfig.tooltip_defaultFont = tooltip_defaultFont;
        }

        public float getFadeInTime() {
            return uiConfig.tooltip_FadeInSpeed;
        }

        public void setFadeInSpeed(float fadeInSpeed) {
            uiConfig.tooltip_FadeInSpeed = fadeInSpeed;
        }

        public int getFadeInDelay() {
            return uiConfig.tooltip_FadeInDelay;
        }

        public void setFadeInDelay(int fadeInDelay) {
            uiConfig.tooltip_FadeInDelay = fadeInDelay;
        }

        public float getFadeOutSpeed() {
            return uiConfig.tooltip_FadeOutSpeed;
        }

        public void setFadeOutSpeed(float fadeOutSpeed) {
            uiConfig.tooltip_FadeOutSpeed = fadeOutSpeed;
        }
    }

    public final class APIMouseTextInputConfig {

        APIMouseTextInputConfig(){
        }

        public CMediaFont getDefaultFont() {
            return uiConfig.mouseTextInput_defaultFont;
        }

        public void setDefaultFont(CMediaFont mouseTextInput_defaultFont) {
            uiConfig.mouseTextInput_defaultFont = mouseTextInput_defaultFont;
        }

        public char[] getDefaultLowerCaseCharacters() {
            return uiConfig.mouseTextInput_defaultLowerCaseCharacters;
        }

        public void setDefaultLowerCaseCharacters(char[] mouseTextInput_defaultLowerCaseCharacters) {
            uiConfig.mouseTextInput_defaultLowerCaseCharacters = mouseTextInput_defaultLowerCaseCharacters;
        }

        public char[] getDefaultUpperCaseCharacters() {
            return uiConfig.mouseTextInput_defaultUpperCaseCharacters;
        }

        public void setDefaultUpperCaseCharacters(char[] mouseTextInput_defaultUpperCaseCharacters) {
            uiConfig.mouseTextInput_defaultUpperCaseCharacters = mouseTextInput_defaultUpperCaseCharacters;
        }
    }


}
