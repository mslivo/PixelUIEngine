package net.mslivo.core.engine.ui_engine;

import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.media.CMediaSprite;
import net.mslivo.core.engine.ui_engine.constants.VIEWPORT_MODE;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.ui.Window;
import net.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.Contextmenu;
import net.mslivo.core.engine.ui_engine.ui.hotkeys.HotKey;
import net.mslivo.core.engine.ui_engine.ui.mousetool.MouseTool;
import net.mslivo.core.engine.ui_engine.ui.notification.Notification;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;

import java.util.ArrayList;

/*
    - Collections related functions are provided like
        - add(X)
        - adds(X[]) -> add(X)
        - removeX(X)
        - removeXs(X[]) ->  removeX()
        - removeAllX() -> remove(X[])
        - Optional: ArrayList<X> findXsByName
        - Optional: X findXByName

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

    public ArrayList<Notification> notifications() {
        return new ArrayList<>(uiEngineState.notifications);
    }

    public void addNotification(Notification notification) {
        if (notification == null) return;
        UICommonUtils.notification_addToScreen(uiEngineState, notification, uiConfig.notification_max);
    }

    public void addNotifications(Notification[] notifications) {
        if (notifications == null) return;
        for (int i = 0; i < notifications.length; i++) addNotification(notifications[i]);
    }

    public void removeNotification(Notification notification) {
        if (notification == null) return;
        UICommonUtils.notification_removeFromScreen(uiEngineState, notification);
    }

    public void removeNotifications(Notification[] notifications) {
        if (notifications == null) return;
        for (int i = 0; i < notifications.length; i++) removeNotification(notifications[i]);
    }

    public void removeAllNotifications() {
        removeNotifications(uiEngineState.notifications.toArray(new Notification[]{}));
    }

    public ArrayList<Notification> findNotificationsByName(String name) {
        if (name == null) return new ArrayList<>();
        ArrayList<Notification> result = new ArrayList<>();
        for (int i = 0; i < uiEngineState.notifications.size(); i++)
            if (name.equals(uiEngineState.notifications.get(i).name)) result.add(uiEngineState.notifications.get(i));
        return result;
    }

    public Notification findNotificationByName(String name) {
        if (name == null) return null;
        ArrayList<Notification> result = findNotificationsByName(name);
        return result.getFirst();
    }

    public boolean isNotificationAddedToScreen(Notification notification) {
        if (notification == null) return false;
        return notification.addedToScreen;
    }

    /* #################### Context Menu #################### */

    public Contextmenu contextMenu() {
        return uiEngineState.openContextMenu;
    }

    public void openContextMenu(Contextmenu contextMenu) {
        UICommonUtils.contextMenu_openAtMousePosition(uiEngineState, mediaManager, contextMenu);
    }

    public void openContextMenu(Contextmenu contextMenu, int x, int y) {
        if (contextMenu == null) return;
        UICommonUtils.contextMenu_open(uiEngineState, mediaManager, contextMenu, x, y);
    }

    public void closeContextMenu(Contextmenu contextMenu) {
        UICommonUtils.contextMenu_close(uiEngineState, contextMenu);
    }

    public boolean isContextMenuOpen(Contextmenu contextMenu) {
        return UICommonUtils.contextMenu_isOpen(uiEngineState, contextMenu);
    }

    /* #################### Windows #################### */

    public ArrayList<Window> windows() {
        return new ArrayList<>(uiEngineState.windows);
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
        removeWindows(uiEngineState.windows.toArray(new Window[]{}));
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
        closeWindows(uiEngineState.windows.toArray(new Window[]{}));
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
        for (int i = 0; i < uiEngineState.windows.size(); i++)
            UICommonUtils.window_receiveMessage(uiEngineState.windows.get(i), type, parameters);
    }

    public void windowsEnforceScreenBounds() {
        for (int i = 0; i < uiEngineState.windows.size(); i++)
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

    public ArrayList<Component> screenComponents() {
        return new ArrayList<>(uiEngineState.screenComponents);
    }

    public void addScreenComponent(Component component) {
        if (component == null) return;
        UICommonUtils.component_addToScreen(component, uiEngineState);
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
        removeScreenComponents(uiEngineState.screenComponents.toArray(new Component[]{}));
    }

    public ArrayList<Component> findScreenComponentsByName(String name) {
        if (name == null) return new ArrayList<>();
        ArrayList<Component> result = new ArrayList<>();
        for (int i = 0; i < uiEngineState.screenComponents.size(); i++)
            if (name.equals(uiEngineState.screenComponents.get(i).name))
                result.add(uiEngineState.screenComponents.get(i));
        return result;
    }

    public Component findScreenComponentByName(String name) {
        if (name == null) return null;
        ArrayList<Component> result = findScreenComponentsByName(name);
        return result.size() > 0 ? result.getFirst() : null;
    }

    /* #################### MouseTool #################### */

    public MouseTool mouseTool() {
        return uiEngineState.mouseTool;
    }

    public void setMouseTool(MouseTool mouseTool) {
        uiEngineState.mouseTool = mouseTool;
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

    public ArrayList<HotKey> hotKeys() {
        return new ArrayList<>(uiEngineState.hotKeys);
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
        uiEngineState.hotKeys.remove(hotKey);
    }

    public void removeHotKeys(HotKey[] hotKeys) {
        if (hotKeys == null) return;
        for (int i = 0; i < hotKeys.length; i++) removeHotKey(hotKeys[i]);
    }

    public void removeAllHotKeys() {
        removeHotKeys(uiEngineState.hotKeys.toArray(new HotKey[]{}));
    }

    public ArrayList<HotKey> findHotKeysByName(String name) {
        if (name == null) return new ArrayList<>();
        ArrayList<HotKey> result = new ArrayList<>();
        for (int i = 0; i < uiEngineState.hotKeys.size(); i++)
            if (name.equals(uiEngineState.hotKeys.get(i).name)) result.add(uiEngineState.hotKeys.get(i));
        return result;
    }

    public HotKey findHotKeyByName(String name) {
        if (name == null) return null;
        ArrayList<HotKey> result = findHotKeysByName(name);
        return result.size() > 0 ? result.getFirst() : null;
    }

    public ArrayList<Window> findWindowsByName(String name) {
        if (name == null) return new ArrayList<>();
        ArrayList<Window> result = new ArrayList<>();
        for (int i = 0; i < uiEngineState.windows.size(); i++)
            if (name.equals(uiEngineState.windows.get(i).name)) result.add(uiEngineState.windows.get(i));
        return result;
    }

    public Window findWindowByName(String name) {
        if (name == null) return null;
        ArrayList<Window> result = findWindowsByName(name);
        return result.size() > 0 ? result.getFirst() : null;
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

    public boolean isAppToolTipDisplayed() {
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

    public int resolutionHeight() {
        return uiEngineState.resolutionHeight;
    }

    public int TS() {
        return uiEngineState.sizeSize.TS;
    }

    public int TS(int size) {
        return uiEngineState.sizeSize.TL(size);
    }

    public int TS_HALF() {
        return uiEngineState.sizeSize.TS_HALF;
    }

    public int TS2() {
        return uiEngineState.sizeSize.TS2;
    }

    public int TS3() {
        return uiEngineState.sizeSize.TS3;
    }

    public int TS4() {
        return uiEngineState.sizeSize.TS4;
    }

    public float TSF() {
        return uiEngineState.sizeSize.TSF;
    }

    public float TSF(float size) {
        return uiEngineState.sizeSize.TLF(size);
    }

    public float TSF_HALF() {
        return uiEngineState.sizeSize.TLF_HALF;
    }

    public float TSF2() {
        return uiEngineState.sizeSize.TSF2;
    }

    public float TSF3() {
        return uiEngineState.sizeSize.TSF3;
    }

    public float TSF4() {
        return uiEngineState.sizeSize.TSF4;
    }

}
