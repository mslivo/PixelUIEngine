package net.mslivo.pixelui.engine;

import com.badlogic.gdx.utils.Array;
import net.mslivo.pixelui.engine.constants.TileSize;
import net.mslivo.pixelui.media.CMediaSprite;
import net.mslivo.pixelui.media.MediaManager;
import net.mslivo.pixelui.engine.constants.VIEWPORT_MODE;
import net.mslivo.pixelui.engine.actions.common.UpdateAction;
import net.mslivo.pixelui.theme.UIEngineTheme;

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
    private final UICommonUtils uiCommonUtils;
    private final MediaManager mediaManager;
    private final UIEngineConfig uiEngineConfig;

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
    public final UIEngineConfig config;
    public final APIWidgets widgets;

    API(UIEngineState uiEngineState, MediaManager mediaManager) {
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.uiCommonUtils = new UICommonUtils(this.uiEngineState, this.mediaManager);
        this.uiEngineConfig = this.uiEngineState.config;
        // init sub APIs
        this.window = new APIWindow(this, this.uiEngineState, this.uiCommonUtils, mediaManager);
        this.component = new APIComponent(this, this.uiEngineState, this.uiCommonUtils, mediaManager);
        this.input = new APIInput(this, this.uiEngineState, this.uiCommonUtils, mediaManager);
        this.contextMenu = new APIContextMenu(this, this.uiEngineState, this.uiCommonUtils, mediaManager);
        this.notification = new APINotification(this, this.uiEngineState, this.uiCommonUtils, mediaManager);
        this.toolTip = new APITooltip(this, this.uiEngineState, this.uiCommonUtils, mediaManager);
        this.hotkey = new APIHotkey(this, this.uiEngineState, this.uiCommonUtils, mediaManager);
        this.mouseTool = new APIMouseTool(this, this.uiEngineState, this.uiCommonUtils, mediaManager);
        this.mouseTextInput = new APIMouseTextInput(this, this.uiEngineState, this.uiCommonUtils, mediaManager);
        this.camera = new APICamera(this, this.uiEngineState, this.uiCommonUtils, mediaManager);
        this.widgets = new APIWidgets(this, this.uiEngineState, this.uiCommonUtils, mediaManager);
        this.config = this.uiEngineConfig;
    }

    /* #################### Notifications #################### */

    public Array<Notification> topNotifications() {
        return new Array<>(uiEngineState.notifications);
    }

    public Array<TooltipNotification> tooltipNotifications() {
        return new Array<>(uiEngineState.tooltipNotifications);
    }

    public void addNotification(GenericNotification genericNotification) {
        if (genericNotification == null) return;
        uiCommonUtils.notification_addToScreen(genericNotification, uiEngineConfig.notification.maxNotifications);
    }

    public void addNotifications(GenericNotification[] genericNotifications) {
        if (genericNotifications == null) return;
        for (int i = 0; i < genericNotifications.length; i++) addNotification(genericNotifications[i]);
    }

    public void removeNotification(GenericNotification genericNotification) {
        if (genericNotification == null) return;
        uiCommonUtils.notification_removeFromScreen(genericNotification);
    }

    public void removeNotifications(GenericNotification[] genericNotifications) {
        if (genericNotifications == null) return;
        for (int i = 0; i < genericNotifications.length; i++) removeNotification(genericNotifications[i]);
    }

    public void removeAllNotifications() {
        removeNotifications(uiEngineState.notifications.toArray(Notification[]::new));
        removeNotifications(uiEngineState.tooltipNotifications.toArray(TooltipNotification[]::new));
    }

    public Array<GenericNotification> findNotifications(Predicate<Notification> findBy) {
        Array<GenericNotification> result = new Array<>();
        result.addAll(uiCommonUtils.findMultiple(uiEngineState.notifications, findBy));
        result.addAll(uiCommonUtils.findMultiple(uiEngineState.tooltipNotifications, findBy));
        return result;
    }

    public GenericNotification findNotification(Predicate<Notification> findBy) {
        GenericNotification result = uiCommonUtils.find(uiEngineState.notifications, findBy);
        if(result != null)
            return result;
        result = uiCommonUtils.find(uiEngineState.tooltipNotifications, findBy);
        if(result != null)
            return result;
        return null;
    }

    public boolean isNotificationAddedToScreen(GenericNotification genericNotification) {
        if (genericNotification == null) return false;
        return genericNotification.addedToScreen;
    }

    /* #################### Context Menu #################### */

    public ContextMenu contextMenu() {
        return uiEngineState.openContextMenu;
    }

    public void openContextMenu(ContextMenu contextMenu) {
        uiCommonUtils.contextMenu_openAtMousePosition(  contextMenu);
    }

    public void openContextMenu(ContextMenu contextMenu, int x, int y) {
        if (contextMenu == null) return;
        uiCommonUtils.contextMenu_open(  contextMenu, x, y);
    }

    public void closeContextMenu(ContextMenu contextMenu) {
        uiCommonUtils.contextMenu_close( contextMenu);
    }

    public boolean isContextMenuOpen(ContextMenu contextMenu) {
        return uiCommonUtils.contextMenu_isOpen( contextMenu);
    }

    /* #################### MouseTextInput #################### */

    public void openMouseTextInput(MouseTextInput mouseTextInput){
        if(mouseTextInput == null) return;
        uiCommonUtils.mouseTextInput_open( mouseTextInput);
    }

    public void closeMouseTextInput(){
        uiCommonUtils.mouseTextInput_close(uiEngineState);
    }

    /* #################### Windows #################### */

    public Array<Window> windows() {
        return new Array<>(uiEngineState.windows);
    }

    public void addWindow(Window window) {
        if (window == null) return;
        uiCommonUtils.window_addToScreen( window);
    }

    public void addWindows(Window[] windows) {
        if (windows == null) return;
        for (int i = 0; i < windows.length; i++) addWindow(windows[i]);
    }

    public void removeWindow(Window window) {
        if (window == null) return;
        uiCommonUtils.window_removeFromScreen( window);
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
        return uiCommonUtils.window_close( window);
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
        uiCommonUtils.window_receiveMessage(window, type, parameters);
    }

    public void sendMessageToWindows(Window[] windows, int type, Object... parameters) {
        if (windows == null) return;
        for (int i = 0; i < windows.length; i++) uiCommonUtils.window_receiveMessage(windows[i], type, parameters);
    }

    public void sendMessageToAllWindows(int type, Object... parameters) {
        for (int i = 0; i < uiEngineState.windows.size; i++)
            uiCommonUtils.window_receiveMessage(uiEngineState.windows.get(i), type, parameters);
    }

    public void windowsEnforceScreenBounds() {
        for (int i = 0; i < uiEngineState.windows.size; i++)
            uiCommonUtils.window_enforceScreenBounds( uiEngineState.windows.get(i));
    }

    /* #################### Modal #################### */

    public Window modalWindow() {
        return uiEngineState.modalWindow;
    }

    public void addWindowAsModal(Window modalWindow) {
        if (modalWindow == null) return;
        uiCommonUtils.window_addToScreenAsModal( modalWindow);
    }

    public void removeCurrentModalWindow() {
        if (uiEngineState.modalWindow == null) return;
        uiCommonUtils.window_removeFromScreen(uiEngineState.modalWindow);
    }

    public boolean closeCurrentModalWindow() {
        if (uiCommonUtils.window_isModalOpen(uiEngineState)) closeWindow(uiEngineState.modalWindow);
        return false;
    }

    public boolean isModalOpen() {
        return uiCommonUtils.window_isModalOpen(uiEngineState);
    }

    /* #################### Screen Components #################### */

    public Array<Component> screenComponents() {
        return new Array<>(uiEngineState.screenComponents);
    }

    public void addScreenComponent(Component component) {
        if (component == null) return;
        uiCommonUtils.component_addToScreen(component, uiEngineState);
    }

    public void moveScreenComponentToTop(Component component){
        if (component == null) return;
        uiCommonUtils.component_screenMoveToTop(component, uiEngineState);
    }

    public void addScreenComponents(Component[] components) {
        if (components == null) return;
        for (int i = 0; i < components.length; i++) addScreenComponent(components[i]);
    }

    public void removeScreenComponent(Component component) {
        if (component == null) return;
        uiCommonUtils.component_removeFromScreen(component, uiEngineState);
    }

    public void removeScreenComponents(Component[] components) {
        if (components == null) return;
        for (int i = 0; i < components.length; i++) removeScreenComponent(components[i]);
    }

    public void removeAllScreenComponents() {
        removeScreenComponents(uiEngineState.screenComponents.toArray(Component[]::new));
    }

    public Array<Component> findScreenComponents(Predicate<Component> findBy) {
        return findScreenComponents(findBy, Component.class);
    }

    public <T extends Component> Array<T> findScreenComponents(Predicate<Component> findBy, Class<T> tClass) {
        return (Array<T>)uiCommonUtils.findMultiple(uiEngineState.screenComponents, findBy);
    }

    public Component findScreenComponent(Predicate<Component> findBy) {
        return  uiCommonUtils.find(uiEngineState.screenComponents, findBy);
    }

    public <T extends Component> T findScreenComponent(Predicate<Component> findBy, Class<T> tClass) {
        return (T) uiCommonUtils.find(uiEngineState.screenComponents, findBy);
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
        return uiCommonUtils.find(uiEngineState.hotKeys, findBy);
    }

    public Array<HotKey> findHotKeys(Predicate<HotKey> findBy){
        return uiCommonUtils.findMultiple(uiEngineState.hotKeys, findBy);
    }

    public Window findWindow(Predicate<Window> findBy){
        return uiCommonUtils.find(uiEngineState.windows, findBy);
    }

    public Array<Window> findWindows(Predicate<Window> findBy){
        return uiCommonUtils.findMultiple(uiEngineState.windows, findBy);
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
        uiCommonUtils.viewport_changeViewPortMode( viewPortMode);
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
        return uiCommonUtils.ui_getAnimationTimer(uiEngineState);
    }

    public TileSize tileSize() {
        return uiEngineState.theme.ts;
    }

    public UIEngineTheme theme() {
        return uiEngineState.theme;
    }

}
