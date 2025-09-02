package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.utils.Array;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.ui_engine.constants.VIEWPORT_MODE;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.ui.window.Window;
import net.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;
import net.mslivo.core.engine.ui_engine.ui.components.ContextMenu;
import net.mslivo.core.engine.ui_engine.ui.hotkeys.HotKey;
import net.mslivo.core.engine.ui_engine.ui.mousetextinput.MouseTextInput;
import net.mslivo.core.engine.ui_engine.ui.mousetool.MouseTool;
import net.mslivo.core.engine.ui_engine.ui.notification.CommonNotification;
import net.mslivo.core.engine.ui_engine.ui.notification.TooltipNotification;
import net.mslivo.core.engine.ui_engine.ui.notification.Notification;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;

import java.util.function.Predicate;

/*
    - Collections related functions are provided like
        - add(X)
        - adds(X[]) -> add(X)
        - removeX(X)
        - removeXs(X[]) ->  removeX()
        - removeAllX() -> remove(X[])

    - Color related functions are provided like:
        - setColor(X, float r, float g, float b, float a)
        - setColor(X, Color color) -> setColor(X, float r, float g, float b, float a)

    - "create" functions must never trigger ActionListener Events either directly or via UICommons
 */
public final class API {

    // ##### PRIVATE Fields #####
    private final UIEngineState uiEngineState;
    private final MediaManager mediaManager;
    private final UIConfig uiConfig;

    // ##### API Components #####
    public final APIWindow window;
    public final APIComponent component;
    public final APIInput input;
    public final APIContextMenu contextMenu;
    public final APINotification notification;
    public final APITooltip toolTip;
    public final APIHotkey hotkey;
    public final APIMouseTool mouseTool;
    public final APIMouseTextInput mouseTextInput;

    public final APICamera camera;
    public final APIConfig config;
    public final APIComposites composites;

    public API(UIEngineState uiEngineState, MediaManager mediaManager) {
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.uiConfig = uiEngineState.config;
        this.window = new APIWindow(this, uiEngineState, mediaManager);
        this.component = new APIComponent(this, uiEngineState, mediaManager);
        this.input = new APIInput(this, uiEngineState, mediaManager);
        this.contextMenu = new APIContextMenu(this, uiEngineState, mediaManager);
        this.notification = new APINotification(this, uiEngineState, mediaManager);
        this.toolTip = new APITooltip(this, uiEngineState, mediaManager);
        this.hotkey = new APIHotkey(this, uiEngineState, mediaManager);
        this.mouseTool = new APIMouseTool(this, uiEngineState, mediaManager);
        this.mouseTextInput = new APIMouseTextInput(this, uiEngineState, mediaManager);
        this.camera = new APICamera(this, uiEngineState, mediaManager);
        this.config = new APIConfig(this, uiEngineState, mediaManager);
        this.composites = new APIComposites(this, uiEngineState, mediaManager);
    }

    /* #################### Notifications #################### */

    public Array<Notification> topNotifications() {
        return new Array<>(uiEngineState.notifications);
    }

    public Array<TooltipNotification> tooltipNotifications() {
        return new Array<>(uiEngineState.tooltipNotifications);
    }

    public void addNotification(CommonNotification commonNotification) {
        if (commonNotification == null) return;
        UICommonUtils.notification_addToScreen(uiEngineState, commonNotification, uiConfig.notification_max);
    }

    public void addNotifications(CommonNotification[] commonNotifications) {
        if (commonNotifications == null) return;
        for (int i = 0; i < commonNotifications.length; i++) addNotification(commonNotifications[i]);
    }

    public void removeNotification(CommonNotification commonNotification) {
        if (commonNotification == null) return;
        UICommonUtils.notification_removeFromScreen(uiEngineState, commonNotification);
    }

    public void removeNotifications(CommonNotification[] commonNotifications) {
        if (commonNotifications == null) return;
        for (int i = 0; i < commonNotifications.length; i++) removeNotification(commonNotifications[i]);
    }

    public void removeAllNotifications() {
        removeNotifications(uiEngineState.notifications.toArray(Notification[]::new));
        removeNotifications(uiEngineState.tooltipNotifications.toArray(TooltipNotification[]::new));
    }

    public Array<CommonNotification> findNotifications(Predicate<Notification> findBy) {
        Array<CommonNotification> result = new Array<>();
        result.addAll(UICommonUtils.findMultiple(uiEngineState.notifications, findBy));
        result.addAll(UICommonUtils.findMultiple(uiEngineState.tooltipNotifications, findBy));
        return result;
    }

    public CommonNotification findNotification(Predicate<Notification> findBy) {
        CommonNotification result = UICommonUtils.find(uiEngineState.notifications, findBy);
        if(result != null)
            return result;
        result = UICommonUtils.find(uiEngineState.tooltipNotifications, findBy);
        if(result != null)
            return result;
        return null;
    }

    public boolean isNotificationAddedToScreen(CommonNotification commonNotification) {
        if (commonNotification == null) return false;
        return commonNotification.addedToScreen;
    }

    /* #################### Context Menu #################### */

    public ContextMenu contextMenu() {
        return uiEngineState.openContextMenu;
    }

    public void openContextMenu(ContextMenu contextMenu) {
        UICommonUtils.contextMenu_openAtMousePosition(uiEngineState, mediaManager, contextMenu);
    }

    public void openContextMenu(ContextMenu contextMenu, int x, int y) {
        if (contextMenu == null) return;
        UICommonUtils.contextMenu_open(uiEngineState, mediaManager, contextMenu, x, y);
    }

    public void closeContextMenu(ContextMenu contextMenu) {
        UICommonUtils.contextMenu_close(uiEngineState, contextMenu);
    }

    public boolean isContextMenuOpen(ContextMenu contextMenu) {
        return UICommonUtils.contextMenu_isOpen(uiEngineState, contextMenu);
    }

    /* #################### MouseTextInput #################### */

    public void openMouseTextInput(MouseTextInput mouseTextInput){
        if(mouseTextInput == null) return;
        UICommonUtils.mouseTextInput_open(uiEngineState, mouseTextInput);
    }

    public void closeMouseTextInput(){
        UICommonUtils.mouseTextInput_close(uiEngineState);
    }

    /* #################### Windows #################### */

    public Array<Window> windows() {
        return new Array<>(uiEngineState.windows);
    }

    public void addWindow(Window window) {
        if (window == null) return;
        UICommonUtils.window_addToScreen(uiEngineState, window);
    }

    public void addWindows(Window[] windows) {
        if (windows == null) return;
        for (int i = 0; i < windows.length; i++) addWindow(windows[i]);
    }

    public void removeWindow(Window window) {
        if (window == null) return;
        UICommonUtils.window_removeFromScreen(uiEngineState, window);
    }

    public void removeWindows(Window[] windows) {
        if (windows == null) return;
        for (int i = 0; i < windows.length; i++) removeWindow(windows[i]);
    }

    public void removeAllWindows() {
        removeWindows(uiEngineState.windows.toArray(Window[]::new));
    }

    public boolean closeWindow(Window window) {
        if (window == null) return false;
        return UICommonUtils.window_close(uiEngineState, window);
    }

    public void closeWindows(Window[] windows) {
        if (windows == null) return;
        for (int i = 0; i < windows.length; i++) closeWindow(windows[i]);
    }

    public void closeAllWindows() {
        closeWindows(uiEngineState.windows.toArray(Window[]::new));
    }

    public void sendMessageToWindow(Window window, int type, Object... parameters) {
        if (window == null) return;
        UICommonUtils.window_receiveMessage(window, type, parameters);
    }

    public void sendMessageToWindows(Window[] windows, int type, Object... parameters) {
        if (windows == null) return;
        for (int i = 0; i < windows.length; i++) UICommonUtils.window_receiveMessage(windows[i], type, parameters);
    }

    public void sendMessageToAllWindows(int type, Object... parameters) {
        for (int i = 0; i < uiEngineState.windows.size; i++)
            UICommonUtils.window_receiveMessage(uiEngineState.windows.get(i), type, parameters);
    }

    public void windowsEnforceScreenBounds() {
        for (int i = 0; i < uiEngineState.windows.size; i++)
            UICommonUtils.window_enforceScreenBounds(uiEngineState, uiEngineState.windows.get(i));
    }

    /* #################### Modal #################### */

    public Window modalWindow() {
        return uiEngineState.modalWindow;
    }

    public void addWindowAsModal(Window modalWindow) {
        if (modalWindow == null) return;
        UICommonUtils.window_addToScreenAsModal(uiEngineState, modalWindow);
    }

    public void removeCurrentModalWindow() {
        if (uiEngineState.modalWindow == null) return;
        UICommonUtils.window_removeFromScreen(uiEngineState, uiEngineState.modalWindow);
    }

    public boolean closeCurrentModalWindow() {
        if (UICommonUtils.window_isModalOpen(uiEngineState)) closeWindow(uiEngineState.modalWindow);
        return false;
    }

    public boolean isModalOpen() {
        return UICommonUtils.window_isModalOpen(uiEngineState);
    }

    /* #################### Screen Components #################### */

    public Array<Component> screenComponents() {
        return new Array<>(uiEngineState.screenComponents);
    }

    public void addScreenComponent(Component component) {
        if (component == null) return;
        UICommonUtils.component_addToScreen(component, uiEngineState);
    }

    public void moveScreenComponentToTop(Component component){
        if (component == null) return;
        UICommonUtils.component_screenMoveToTop(component, uiEngineState);
    }

    public void addScreenComponents(Component[] components) {
        if (components == null) return;
        for (int i = 0; i < components.length; i++) addScreenComponent(components[i]);
    }

    public void removeScreenComponent(Component component) {
        if (component == null) return;
        UICommonUtils.component_removeFromScreen(component, uiEngineState);
    }

    public void removeScreenComponents(Component[] components) {
        if (components == null) return;
        for (int i = 0; i < components.length; i++) removeScreenComponent(components[i]);
    }

    public void removeAllScreenComponents() {
        removeScreenComponents(uiEngineState.screenComponents.toArray(Component[]::new));
    }

    public Array<Component> findScreenComponents(Predicate<Component> findBy) {
        return UICommonUtils.findMultiple(uiEngineState.screenComponents, findBy);
    }

    public Component findScreenComponent(Predicate<Component> findBy) {
        return UICommonUtils.find(uiEngineState.screenComponents, findBy);
    }

    /* #################### MouseTool #################### */

    public void setMouseTool(MouseTool mouseTool) {
        uiEngineState.mouseTool = mouseTool;
    }

    public MouseTool mouseTool() {
        return uiEngineState.mouseTool;
    }

    public boolean isMouseTool(MouseTool mouseTool) {
        if (mouseTool == null) return false;
        return uiEngineState.mouseTool == mouseTool;
    }

    public boolean isMouseToolName(String name) {
        if (name == null) return false;
        return uiEngineState.mouseTool != null && name.equals(uiEngineState.mouseTool.name);
    }

    /* #################### HotKeys #################### */

    public Array<HotKey> hotKeys() {
        return new Array<>(uiEngineState.hotKeys);
    }

    public void addHotKey(HotKey hotKey) {
        if (hotKey == null) return;
        uiEngineState.hotKeys.add(hotKey);
    }

    public void addHotKeys(HotKey[] hotKeys) {
        if (hotKeys == null) return;
        for (int i = 0; i < hotKeys.length; i++) addHotKey(hotKeys[i]);
    }

    public void removeHotKey(HotKey hotKey) {
        if (hotKey == null) return;
        uiEngineState.hotKeys.removeValue(hotKey, true);
    }

    public void removeHotKeys(HotKey[] hotKeys) {
        if (hotKeys == null) return;
        for (int i = 0; i < hotKeys.length; i++) removeHotKey(hotKeys[i]);
    }

    public void removeAllHotKeys() {
        removeHotKeys(uiEngineState.hotKeys.toArray(HotKey[]::new));
    }

    public HotKey findHotKey(Predicate<HotKey> findBy){
        return UICommonUtils.find(uiEngineState.hotKeys, findBy);
    }

    public Array<HotKey> findHotKeys(Predicate<HotKey> findBy){
        return UICommonUtils.findMultiple(uiEngineState.hotKeys, findBy);
    }

    public Window findWindow(Predicate<Window> findBy){
        return UICommonUtils.find(uiEngineState.windows, findBy);
    }

    public Array<Window> findWindows(Predicate<Window> findBy){
        return UICommonUtils.findMultiple(uiEngineState.windows, findBy);
    }

    /* #################### Misc #################### */

    public void executeSingleUpdateAction(UpdateAction updateAction) {
        if (updateAction == null) return;
        this.uiEngineState.singleUpdateActions.add(updateAction);
    }

    public void overrideCursor(CMediaSprite overrideCursor) {
        if (overrideCursor == null) return;
        overrideCursor(overrideCursor, 0);
    }

    public void overrideCursor(CMediaSprite temporaryCursor, int arrayIndex) {
        if (temporaryCursor == null) return;
        uiEngineState.overrideCursor = temporaryCursor;
        uiEngineState.displayOverrideCursor = true;
        uiEngineState.overrideCursorArrayIndex = Math.max(0, arrayIndex);
    }

    public void setAppToolTip(Tooltip toolTip) {
        uiEngineState.appToolTip = toolTip;
    }

    public boolean isAnyAppToolTipDisplayed() {
        return uiEngineState.appToolTip != null;
    }

    public Tooltip appToolTip() {
        return uiEngineState.appToolTip;
    }

    public boolean isAppToolTipDisplayed(String name){
        return uiEngineState.appToolTip != null && uiEngineState.appToolTip.name.equals(name);
    }

    public boolean isAppToolTipDisplayed(){
        return uiEngineState.appToolTip != null;
    }

    public VIEWPORT_MODE viewportMode() {
        return uiEngineState.viewportMode;
    }

    public void setViewportMode(VIEWPORT_MODE viewPortMode) {
        if (viewPortMode == null) return;
        UICommonUtils.viewport_changeViewPortMode(uiEngineState, viewPortMode);
    }

    public int resolutionWidth() {
        return uiEngineState.resolutionWidth;
    }

    public int resolutionWidthHalf() {
        return uiEngineState.resolutionWidthHalf;
    }

    public int resolutionHeight() {
        return uiEngineState.resolutionHeight;
    }

    public int resolutionHeightHalf() {
        return uiEngineState.resolutionHeightHalf;
    }

    public float animationTimer(){
        return UICommonUtils.ui_getAnimationTimer(uiEngineState);
    }

    public int TS() {
        return uiEngineState.tileSize.TS;
    }

    public int TS(int size) {
        return uiEngineState.tileSize.TL(size);
    }

    public int TS_HALF() {
        return uiEngineState.tileSize.TS_HALF;
    }

    public int TS2() {
        return uiEngineState.tileSize.TS2;
    }

    public int TS3() {
        return uiEngineState.tileSize.TS3;
    }

    public int TS4() {
        return uiEngineState.tileSize.TS4;
    }

    public float TSF() {
        return uiEngineState.tileSize.TSF;
    }

    public float TSF(float size) {
        return uiEngineState.tileSize.TLF(size);
    }

    public float TSF_HALF() {
        return uiEngineState.tileSize.TLF_HALF;
    }

    public float TSF2() {
        return uiEngineState.tileSize.TSF2;
    }

    public float TSF3() {
        return uiEngineState.tileSize.TSF3;
    }

    public float TSF4() {
        return uiEngineState.tileSize.TSF4;
    }

}
