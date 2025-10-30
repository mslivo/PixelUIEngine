package net.mslivo.pixelui.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.mslivo.pixelui.engine.constants.BUTTON_MODE;
import net.mslivo.pixelui.engine.constants.KeyCode;
import net.mslivo.pixelui.engine.constants.MOUSE_CONTROL_MODE;
import net.mslivo.pixelui.engine.constants.VIEWPORT_MODE;
import net.mslivo.pixelui.media.CMediaSprite;
import net.mslivo.pixelui.media.MediaManager;
import net.mslivo.pixelui.rendering.NestedFrameBuffer;
import net.mslivo.pixelui.rendering.PixelPerfectViewport;
import net.mslivo.pixelui.utils.Tools;

import java.util.HashSet;
import java.util.function.Predicate;

public class UICommonUtils {
    public static final String WND_CLOSE_BUTTON = "wnd_close_btn";
    private static IntSet textFieldControlKeys = new IntSet();
    private static IntSet textFieldRepeatedControlKeys = new IntSet();
    private static Array<Component> windowComponentsVisibleOrder = new Array<>();
    private static HashSet<Component> windowComponentsVisibleOrderSet = new HashSet<>();

    static {
        textFieldControlKeys.addAll(KeyCode.Key.LEFT, KeyCode.Key.RIGHT, KeyCode.Key.BACKSPACE, KeyCode.Key.FORWARD_DEL, Input.Keys.HOME, Input.Keys.END, Input.Keys.ENTER);
        textFieldRepeatedControlKeys.addAll(KeyCode.Key.LEFT, KeyCode.Key.RIGHT, KeyCode.Key.BACKSPACE, KeyCode.Key.FORWARD_DEL);
    }

    private final UIEngineState uiEngineState;
    private final MediaManager mediaManager;
    private StringBuilder testStringBuilder = new StringBuilder();

    UICommonUtils(UIEngineState uiEngineState, MediaManager mediaManager) {
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
    }

    public void emulatedMouse_setPosition(int x, int y) {
        if (!uiEngineState.currentControlMode.emulated) return;
        // not possibe with hardware mouse - would be resetted instantly
        uiEngineState.mouse_emulated.x = x;
        uiEngineState.mouse_emulated.y = y;
    }

    public void emulatedMouse_setPositionComponent(Component component) {
        if (component == null) return;
        if (component.addedToWindow == null && !component.addedToScreen) return;
        int x = component_getAbsoluteX(component) + (uiEngineState.tileSize.TL(component.width) / 2);
        int y = component_getAbsoluteY(component) + (uiEngineState.tileSize.TL(component.height) / 2);
        emulatedMouse_setPosition(x, y);
    }

    private boolean emulatedMouse_isInteractAbleComponent(Component component) {
        if (!(component instanceof Image || component instanceof Text)) {
            if (component.visible && !component.disabled && !component_isHiddenByTab(component)) return true;
        }
        return false;
    }

    public void emulatedMouse_setPositionNextComponent(boolean backwards) {
        Window activeWindow = window_findTopInteractableWindow(uiEngineState);
        if (activeWindow != null && activeWindow.folded) {
            emulatedMouse_setPosition(
                    activeWindow.x + (uiEngineState.tileSize.TL(activeWindow.width) / 2),
                    activeWindow.y + (uiEngineState.tileSize.TL(activeWindow.height) - 4)
            );
            return;
        }
        windowComponentsVisibleOrder.clear();
        windowComponentsVisibleOrderSet.clear();
        int fromX = activeWindow != null ? activeWindow.x : 0;
        int fromY = activeWindow != null ? activeWindow.y : 0;
        int toX = activeWindow != null ? fromX + uiEngineState.tileSize.TL(activeWindow.width) : uiEngineState.resolutionWidth;
        int toY = activeWindow != null ? fromY + uiEngineState.tileSize.TL(activeWindow.height) : uiEngineState.resolutionHeight;

        int nearestIndex = -1;
        float nearestDistance = Float.MAX_VALUE;

        for (int iy = toY; iy >= fromY; iy -= uiEngineState.tileSize.TS) {
            for (int ix = fromX; ix <= toX; ix += uiEngineState.tileSize.TS) {
                Object object = component_getUIObjectAtPosition(ix, iy);
                if (!windowComponentsVisibleOrderSet.contains(object) && object instanceof Component component && emulatedMouse_isInteractAbleComponent(component)) {
                    windowComponentsVisibleOrder.add(component);
                    windowComponentsVisibleOrderSet.add(component);
                    float distance = Tools.Calc.distance(component_getAbsoluteX(component) + (uiEngineState.tileSize.TL(component.width) / 2),
                            component_getAbsoluteY(component) + (uiEngineState.tileSize.TL(component.height) / 2), uiEngineState.mouse_emulated.x, uiEngineState.mouse_emulated.y);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestIndex = windowComponentsVisibleOrder.size - 1;
                    }
                }
            }
        }

        if (nearestIndex != -1) {
            if (backwards) {
                nearestIndex--;
                if (nearestIndex < 0) nearestIndex = (windowComponentsVisibleOrder.size - 1);
            } else {
                nearestIndex++;
                if (nearestIndex > (windowComponentsVisibleOrder.size - 1)) nearestIndex = 0;
            }

            emulatedMouse_setPositionComponent(windowComponentsVisibleOrder.get(nearestIndex));
        }

        return;
    }


    static Window window_findTopInteractableWindow(UIEngineState uiEngineState) {
        if (uiEngineState.windows.isEmpty()) return null;
        for (int i = (uiEngineState.windows.size - 1); i >= 0; i--) {
            Window window = uiEngineState.windows.get(i);
            if (window.visible) return window;
        }
        return null;
    }


    public boolean window_isModalOpen(UIEngineState uiEngineState) {
        return uiEngineState.modalWindow != null;
    }

    public void window_fold(Window window) {
        window.folded = true;
        window.windowAction.onFold();
    }

    public void window_unFold(Window window) {
        window.folded = false;
        window.windowAction.onUnfold();
    }

    public void window_receiveMessage(Window window, int type, Object... parameters) {
        if (window == null) return;
        window.windowAction.onMessageReceived(type, parameters);
    }

    public void window_bringToFront(Window window) {
        if (uiEngineState.windows.size == 1) return;

        int currentIndex = uiEngineState.windows.indexOf(window, true);

        int targetIndex = 0;
        if (window.alwaysOnTop) {
            targetIndex = uiEngineState.windows.size - 1;
        } else {
            for (int i = (uiEngineState.windows.size - 1); i >= 0; i--) {
                if (i != currentIndex && !uiEngineState.windows.get(i).alwaysOnTop) {
                    targetIndex = i;
                    break;
                }
            }
        }

        if (currentIndex != targetIndex)
            uiEngineState.windows.swap(currentIndex, targetIndex);

    }


    public void window_setPosition(Window window, int x, int y) {
        window.x = x;
        window.y = y;
        if (window.enforceScreenBounds) window_enforceScreenBounds(window);
    }


    public void window_enforceScreenBounds(Window window) {
        int wndWidth = uiEngineState.tileSize.TL(window.width);
        window.x = Math.clamp(window.x, 0, uiEngineState.resolutionWidth - wndWidth);
        if (window.folded) {
            window.y = Math.clamp(window.y, -(uiEngineState.tileSize.TL(window.height - 1)), uiEngineState.resolutionHeight - uiEngineState.tileSize.TL(window.height));
        } else {
            window.y = Math.clamp(window.y, 0, uiEngineState.resolutionHeight - uiEngineState.tileSize.TL(window.height));
        }
    }

    public void window_addToScreenAsModal(Window window) {
        if (uiEngineState.modalWindow == null) {
            // Closned opened comboxes/menus
            resetAllReferences(uiEngineState);
            // Add to Screen
            window.alwaysOnTop = true;
            window.visible = true;
            window.folded = false;
            window.enforceScreenBounds = true;
            uiEngineState.modalWindow = window;
            window_center(window);
            window_addToScreen(window);
        } else {
            uiEngineState.modalWindowQueue.addLast(window);
        }
    }


    public void window_center(Window window) {
        int centerX = (uiEngineState.resolutionWidthHalf) - (uiEngineState.tileSize.TL(window.width) / 2);
        int centerY = (uiEngineState.resolutionHeightHalf) - ((window.folded ? uiEngineState.tileSize.TS : uiEngineState.tileSize.TL(window.height)) / 2);
        window_setPosition(window, centerX, centerY);
    }

    public void window_addToScreen(Window window) {
        if (window.addedToScreen) return;
        window.addedToScreen = true;
        uiEngineState.windows.add(window);
        window.windowAction.onDisplay();
        window_enforceScreenBounds(window);
        window_bringToFront(window);
    }

    public boolean window_close(Window window) {
        for (int i = 0; i < window.components.size; i++) {
            if (window.components.get(i).name.equals(WND_CLOSE_BUTTON) && window.components.get(i) instanceof Button closeButton) {
                button_press(closeButton);
                button_release(closeButton);
                return true;
            }
        }
        return false;
    }

    public void window_removeFromScreen(Window window) {
        if (!window.addedToScreen) return;

        // Remove References
        if (uiEngineState.lastUIMouseHover == window) uiEngineState.lastUIMouseHover = null;
        if (window_isModalOpen(uiEngineState) && uiEngineState.modalWindow == window)
            uiEngineState.modalWindow = null;
        for (int i = window.components.size - 1; i >= 0; i--) {
            final Component component = window.components.get(i);
            component_removeFromWindow(component, window, uiEngineState);
            if (component instanceof AppViewport appViewPort)
                uiEngineState.appViewPorts.removeValue(appViewPort, true);
        }

        window_resetReferences(window);

        // Remove
        window.addedToScreen = false;
        uiEngineState.windows.removeValue(window, true);
        window.windowAction.onRemove();

        // Add Next Modal from Queue queue
        if (!uiEngineState.modalWindowQueue.isEmpty())
            window_addToScreenAsModal(uiEngineState.modalWindowQueue.removeLast());
    }

    public void component_setSize(Component component, int width, int height) {
        component.width = Math.max(width, 1);
        component.height = Math.max(height, 1);

        if (component instanceof AppViewport appViewPort) {
            appViewPort_resizeCameraTextureAndFrameBuffer(appViewPort);
        }
    }

    public int component_getParentWindowX(Component component) {
        return component.addedToWindow != null ? component.addedToWindow.x : 0;
    }

    public int component_getParentWindowY(Component component) {
        return component.addedToWindow != null ? component.addedToWindow.y : 0;
    }

    public int component_getAbsoluteX(Component component) {
        return component_getParentWindowX(component) + component.x;
    }

    public int component_getAbsoluteY(Component component) {
        return component_getParentWindowY(component) + component.y;
    }

    public int component_getRelativeMouseX(int mouse_ui_x, Component component) {
        return mouse_ui_x - component_getAbsoluteX(component);
    }

    public int component_getRelativeMouseY(int mouse_ui_y, Component component) {
        return mouse_ui_y - component_getAbsoluteY(component);
    }

    public Object component_getUIObjectAtPosition(int x, int y) {
        // Notification Collision
        for (int i = 0; i < uiEngineState.notifications.size; i++) {
            Notification notification = uiEngineState.notifications.get(i);
            if (!notification.uiInteractionEnabled) continue;
            if (Tools.Calc.pointRectsCollide(x, y,
                    0, uiEngineState.resolutionHeight - uiEngineState.tileSize.TL(i + 1),
                    uiEngineState.resolutionWidth, uiEngineState.tileSize.TS)) {
                return notification;
            }
        }

        // Context Menu Item collision
        if (uiEngineState.openContextMenu != null) {
            for (int i = 0; i < uiEngineState.openContextMenu.items.size; i++) {
                if (Tools.Calc.pointRectsCollide(x, y, uiEngineState.openContextMenu.x, uiEngineState.openContextMenu.y - (uiEngineState.tileSize.TS) - uiEngineState.tileSize.TL(i), uiEngineState.tileSize.TL(uiEngineState.displayedContextMenuWidth), uiEngineState.tileSize.TS)) {
                    return uiEngineState.openContextMenu.items.get(i);
                }
            }
        }

        // Combobox Open Menu collision
        if (uiEngineState.openComboBox != null) {
            if (Tools.Calc.pointRectsCollide(
                    x, y,
                    component_getAbsoluteX(uiEngineState.openComboBox),
                    component_getAbsoluteY(uiEngineState.openComboBox) - (uiEngineState.tileSize.TL(uiEngineState.openComboBox.items.size)),
                    uiEngineState.tileSize.TL(uiEngineState.openComboBox.width), uiEngineState.tileSize.TL(uiEngineState.openComboBox.items.size))) {
                return uiEngineState.openComboBox;
            }
        }

        // Window / WindowComponent collision
        for (int i = uiEngineState.windows.size - 1; i >= 0; i--) { // use for(i) to avoid iterator creation
            Window window = uiEngineState.windows.get(i);
            if (!window.visible) continue;

            int wndX = window.x;
            int wndY = window.y + (window.folded ? uiEngineState.tileSize.TL(window.height - 1) : 0);
            int wndWidth = uiEngineState.tileSize.TL(window.width);
            int wndHeight = window.folded ? uiEngineState.tileSize.TS : uiEngineState.tileSize.TL(window.height);

            boolean collidesWithWindow = Tools.Calc.pointRectsCollide(x, y, wndX, wndY, wndWidth, wndHeight);
            if (collidesWithWindow) {
                for (int ic = window.components.size - 1; ic >= 0; ic--) {
                    Component component = window.components.get(ic);
                    if (component_isComponentAtPosition(x, y, component)) {
                        return component;
                    }
                }
                return window;
            }
        }

        // Screen component collision
        for (int isc = uiEngineState.screenComponents.size - 1; isc >= 0; isc--) {
            Component screenComponent = uiEngineState.screenComponents.get(isc);
            if (component_isComponentAtPosition(x, y, screenComponent)) return screenComponent;
        }
        return null;
    }

    public boolean component_isComponentAtPosition(int x, int y, Component component) {
        if (!component.visible) return false;
        if (component_isHiddenByTab(component)) return false;

        if (Tools.Calc.pointRectsCollide(x, y, component_getAbsoluteX(component), component_getAbsoluteY(component), uiEngineState.tileSize.TL(component.width), uiEngineState.tileSize.TL(component.height))) {
            return true;
        }
        return false;
    }

    public boolean component_isHiddenByTab(Component component) {
        if (component.addedToTab == null) return false;
        Tab selectedTab = UICommonUtils.tabBar_getSelectedTab(component.addedToTab.addedToTabBar);
        if (selectedTab != null && selectedTab == component.addedToTab) {
            if (component.addedToTab.addedToTabBar.addedToTab != null) {
                return component_isHiddenByTab(component.addedToTab.addedToTabBar);
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void hotkey_press(HotKey hotKey) {
        hotKey.pressed = true;
        hotKey.hotKeyAction.onPress();
    }

    public void hotkey_release(HotKey hotKey) {
        hotKey.pressed = false;
        hotKey.hotKeyAction.onRelease();
    }

    public void checkbox_check(Checkbox checkBox) {
        if (checkBox.checked) return;
        checkBox.checked = true;
        checkBox.checkBoxAction.onCheck(true);
    }

    public void checkbox_unCheck(Checkbox checkBox) {
        if (!checkBox.checked) return;
        checkBox.checked = false;
        checkBox.checkBoxAction.onCheck(false);
    }

    public void setMouseInteractedUIObject(Object object) {
        uiEngineState.mouseInteractedUIObjectFrame = object;
    }

    public void setKeyboardInteractedUIObject(Object object) {
        uiEngineState.keyboardInteractedUIObjectFrame = object;
    }

    public void notification_addToScreen(GenericNotification genericNotification, int notificationsMax) {
        if (genericNotification.addedToScreen) return;
        genericNotification.addedToScreen = true;

        switch (genericNotification) {
            case Notification notification -> {
                uiEngineState.notifications.add(notification);
                // Remove first if too many
                if (uiEngineState.notifications.size > notificationsMax)
                    notification_removeFromScreen(uiEngineState.notifications.first());
                notification.notificationAction.onDisplay();

            }
            case TooltipNotification tooltipNotification -> {
                uiEngineState.tooltipNotifications.add(tooltipNotification);
            }
        }

    }

    public void notification_removeFromScreen(GenericNotification genericNotification) {
        if (!genericNotification.addedToScreen) return;
        genericNotification.addedToScreen = false;
        switch (genericNotification) {
            case Notification notification -> {
                uiEngineState.notifications.removeValue(notification, true);
                notification.notificationAction.onRemove();
            }
            case TooltipNotification tooltipNotification -> {
                uiEngineState.tooltipNotifications.removeValue(tooltipNotification, true);
            }
        }
    }

    public boolean contextMenu_openAtMousePosition(ContextMenu contextMenu) {
        boolean success = contextMenu_open(contextMenu, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y);
        if (success && (uiEngineState.currentControlMode.emulated)) {
            // emulated mode: move mouse onto the opened menu
            uiEngineState.mouse_emulated.x += uiEngineState.tileSize.TS_HALF;
            uiEngineState.mouse_emulated.y -= uiEngineState.tileSize.TS_HALF;
        }
        return success;
    }

    public boolean contextMenu_open(ContextMenu contextMenu, int x, int y) {
        if (contextMenu.items.isEmpty()) return false;
        // Close open ContextMenus
        if (uiEngineState.openContextMenu != null) {
            contextMenu_close(uiEngineState.openContextMenu);
        }
        // Open this one
        contextMenu.x = x;
        contextMenu.y = y;
        int textwidth = 0;
        for (int i = 0; i < contextMenu.items.size; i++) {
            ContextMenuItem contextMenuItem = contextMenu.items.get(i);
            int w = mediaManager.fontTextWidth(uiEngineState.config.ui.font, contextMenuItem.text);
            if (contextMenuItem.contextMenuItemAction.icon() != null) w = w + uiEngineState.tileSize.TS;
            if (w > textwidth) textwidth = w;
        }
        uiEngineState.displayedContextMenuWidth = (textwidth + uiEngineState.tileSize.TS) / uiEngineState.tileSize.TS;
        uiEngineState.openContextMenu = contextMenu;
        uiEngineState.openContextMenu.contextMenuAction.onDisplay();
        return true;
    }

    public void contextMenu_close(ContextMenu contextMenu) {
        if (contextMenu_isOpen(contextMenu)) {
            resetOpenContextMenuReference(uiEngineState);
        }
    }


    static String progressBar_getProgressText(float progress) {
        return Tools.Text.formatPercent(progress);
    }

    static String progressBar_getProgressText2Decimal(float progress) {
        return Tools.Text.formatPercentDecimal(progress);
    }


    static Tab tabBar_getSelectedTab(Tabbar tabBar) {
        if (tabBar == null) return null;
        return tabBar.tabs.get(Math.clamp(tabBar.selectedTab, 0, tabBar.tabs.size - 1));
    }

    public void tabBar_selectTab(Tabbar tabBar, Tab tab) {
        if (tab.addedToTabBar != tabBar) return;
        for (int i = 0; i < tabBar.tabs.size; i++) {
            if (tabBar.tabs.get(i) == tab) {
                tabBar_selectTab(tabBar, i);
                return;
            }
        }
    }

    public void tabBar_selectTab(Tabbar tabBar, int index) {
        int selectTab = Math.clamp(index, 0, tabBar.tabs.size - 1);
        Tab tab = tabBar.tabs.get(selectTab);
        if (tab.disabled)
            return;
        tabBar.selectedTab = selectTab;
        tab.tabAction.onSelect();
        tabBar.tabBarAction.onChangeTab(index, tab);

    }


    public void button_press(Button button) {
        if (button.pressed || button.mode != BUTTON_MODE.DEFAULT) return;
        button.pressed = true;
        button.buttonAction.onPress();
    }

    public void button_release(Button button) {
        if (!button.pressed || button.mode != BUTTON_MODE.DEFAULT) return;
        button.pressed = false;
        button.buttonAction.onRelease();
    }

    public void button_toggle(Button button) {
        button_toggle(button, !button.pressed);
    }

    public void progressbar_setProgress(Progressbar progressBar, float progress) {
        progressBar.progress = Math.clamp(progress, 0f, 1f);
    }

    public void button_toggle(Button button, boolean pressed) {
        if (button.toggleDisabled || button.pressed == pressed || button.mode != BUTTON_MODE.TOGGLE) return;
        if (button.buttonAction.onToggle(pressed)) {
            button.pressed = pressed;
        }
    }

    public void button_centerContent(MediaManager mediaManager, Button button) {
        if (button == null) return;
        if (button instanceof ImageButton imageButton) {
            if (imageButton.image == null) return;
            imageButton.contentOffset_x = MathUtils.round((uiEngineState.tileSize.TL(imageButton.width) - mediaManager.spriteWidth(imageButton.image)) / 2f);
            imageButton.contentOffset_y = MathUtils.round((uiEngineState.tileSize.TL(imageButton.height) - mediaManager.spriteHeight(imageButton.image)) / 2f);

        } else if (button instanceof TextButton textButton) {
            if (textButton.text == null) return;
            int iconWidth = textButton.buttonAction.icon() != null ? uiEngineState.tileSize.TS : 0;
            int contentWidth = mediaManager.fontTextWidth(uiEngineState.config.ui.font, textButton.text) + iconWidth;
            int contentHeight = mediaManager.fontTextHeight(uiEngineState.config.ui.font, textButton.text);
            textButton.contentOffset_x = MathUtils.round((uiEngineState.tileSize.TL(textButton.width) - contentWidth) / 2f);
            textButton.contentOffset_y = MathUtils.round(((uiEngineState.tileSize.TL(textButton.height) - contentHeight)) / 2f) - 2;
        }
    }

    public boolean grid_positionValid(Grid grid, int x, int y) {
        if (grid.items != null) {
            return x >= 0 && x < grid.items.length && y >= 0 && y < grid.items[0].length;
        }
        return false;
    }

    public void grid_setItems(Grid grid, Object[][] items) {
        grid.items = items;
        grid_updateSize(grid);
    }

    public void grid_updateSize(Grid grid) {
        int factor = grid.bigMode ? 2 : 1;
        if (grid.items != null) {
            grid.width = grid.items.length * factor;
            grid.height = grid.items[0].length * factor;
        } else {
            grid.width = 1;
            grid.height = 1;
        }
    }

    public int textField_findTextPosition(Textfield textField, int mouseX) {
        testStringBuilder.setLength(0);
        int checkRange = textField.content.length() - textField.offset;
        for (int i = 0; i < checkRange; i++) {
            int realOffset = textField.offset + i;
            testStringBuilder.append(textField.content.charAt(realOffset));
            if (mediaManager.fontTextWidth(uiEngineState.config.ui.font, testStringBuilder.toString()) > mouseX) {
                return Math.min(realOffset, textField.content.length());
            }
        }
        // If mouse past end, return end position
        return Math.min(textField.offset + textField.content.length(), textField.content.length());
    }

    public void textField_resetMarkedContent(Textfield textField) {
        textField_setMarkedContent(textField,0,0);
    }

    public void textField_setMarkedContent(Textfield textField, int begin, int end) {
        textField.markedContentBegin = Math.max(begin,0);
        textField.markedContentEnd = Math.min(end,textField.content.length());
    }

    public void textField_setCaretPosition(Textfield textField, int position) {
        textField.caretPosition = Math.clamp(position, 0, textField.content.length());
        if (textField.caretPosition < textField.offset) {
            while (textField.caretPosition < textField.offset) {
                textField.offset--;
            }
        } else {
            String subContent = textField.content.substring(textField.offset, textField.caretPosition);
            int width = uiEngineState.tileSize.TL(textField.width) - 4;
            if (mediaManager.fontTextWidth(uiEngineState.config.ui.font, subContent) > width) {
                while (mediaManager.fontTextWidth(uiEngineState.config.ui.font, subContent) > width) {
                    textField.offset++;
                    subContent = textField.content.substring(textField.offset, textField.caretPosition);
                }
            }
        }
    }

    public boolean textField_isControlKey(int keyCode) {
        return textFieldControlKeys.contains(keyCode);
    }

    public boolean textField_isRepeatedControlKey(int keyCode) {
        return textFieldRepeatedControlKeys.contains(keyCode);
    }

    public void textField_executeControlKey(Textfield textField, int keyCode, boolean shiftPressed) {

        boolean contentIsMarked = textField_isContentMarked(textField);
        int caret = textField.caretPosition;

        switch (keyCode) {
            case Input.Keys.LEFT -> {
                if (caret > 0) {
                    int newCaret = caret - 1;

                    if (shiftPressed) {
                        int anchor;
                        if (contentIsMarked) {
                            // Keep the side opposite to caret as anchor
                            anchor = (caret == textField.markedContentBegin)
                                    ? textField.markedContentEnd
                                    : textField.markedContentBegin;
                        } else {
                            anchor = caret;
                        }

                        textField_setMarkedContent(
                                textField,
                                Math.min(anchor, newCaret),
                                Math.max(anchor, newCaret)
                        );
                    } else {
                        if(contentIsMarked){
                            textField_resetMarkedContent(textField);
                            newCaret++;
                        }
                    }

                    textField_setCaretPosition(textField, newCaret);
                }
            }

            case Input.Keys.RIGHT -> {
                if (caret < textField.content.length()) {
                    int newCaret = caret + 1;

                    if (shiftPressed) {
                        int anchor;
                        if (contentIsMarked) {
                            // Keep the side opposite to caret as anchor
                            anchor = (caret == textField.markedContentEnd)
                                    ? textField.markedContentBegin
                                    : textField.markedContentEnd;
                        } else {
                            anchor = caret;
                        }

                        textField_setMarkedContent(
                                textField,
                                Math.min(anchor, newCaret),
                                Math.max(anchor, newCaret)
                        );
                    } else {
                        if(contentIsMarked){
                            textField_resetMarkedContent(textField);
                            newCaret--;
                        }
                    }

                    textField_setCaretPosition(textField, newCaret);
                }
            }
            case Input.Keys.BACKSPACE -> {
                if (!textField.content.isEmpty()) {
                    if (contentIsMarked) {
                        textField_removeMarkedContent(textField);
                    } else if (textField.caretPosition > 0) {
                        String newContent = textField.content.substring(0, textField.caretPosition - 1) + textField.content.substring(textField.caretPosition);
                        textField_setCaretPosition(textField, textField.caretPosition - 1);
                        textField_setContent(textField, newContent);
                    }

                }
            }
            case Input.Keys.FORWARD_DEL -> {
                if (!textField.content.isEmpty()) {
                    if (contentIsMarked) {
                        textField_removeMarkedContent(textField);
                    } else if (textField.caretPosition < textField.content.length()) {
                        String newContent = textField.content.substring(0, textField.caretPosition) + textField.content.substring(textField.caretPosition + 1);
                        textField_setContent(textField, newContent);
                    }
                }
            }
            case Input.Keys.HOME -> textField_setCaretPosition(textField, 0);
            case Input.Keys.END -> textField_setCaretPosition(textField, textField.content.length());
            case Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> {
                textField_unFocus(textField); // Unfocus
                textField.textFieldAction.onEnter(textField.content, textField.contentValid);
            }
            default -> {
            }
        }
    }

    public String textField_getMarkedContent(Textfield textField) {
        int from = Math.clamp(textField.markedContentBegin, 0, textField.content.length());
        int to = Math.min(textField.markedContentEnd, textField.content.length());
        if (to - from <= 0)
            return "";
        String markedContent = textField.content.substring(from, to);
        return markedContent;
    }

    public boolean textField_isContentMarked(Textfield textField){
        return Math.abs(textField.markedContentBegin-textField.markedContentEnd) > 0;
    }

    public String textField_removeMarkedContent(Textfield textField) {
        int from = Math.clamp(textField.markedContentBegin, 0, textField.content.length());
        int to = Math.min(textField.markedContentEnd, textField.content.length());
        if (to - from <= 0)
            return "";
        String cutContent = textField_getMarkedContent(textField);
        textField_resetMarkedContent(textField);
        String newContent = textField.content.substring(0, from) + textField.content.substring(to, textField.content.length());
        textField_setContent(textField, newContent);
        textField.caretPosition = Math.clamp(from, 0, textField.content.length());
        return cutContent;
    }

    public void textField_typeCharacter(Textfield textField, char character) {
        if (textField.allowedCharacters == null || textField.allowedCharacters.contains(character)) {
            textField_removeMarkedContent(textField);
            String newContent = textField.content.substring(0, textField.caretPosition) + character + textField.content.substring(textField.caretPosition);
            textField_setContent(textField, newContent);
            textField_setCaretPosition(textField, textField.caretPosition + 1);
            textField_resetMarkedContent(textField);
            textField.textFieldAction.onTyped(character);
        }
    }

    public void textField_setContent(Textfield textField, String content) {
        if (content.length() > textField.contentMaxLength) content = content.substring(0, textField.contentMaxLength);
        textField.content = Tools.Text.validString(content);
        textField.caretPosition = Math.clamp(textField.caretPosition, 0, textField.content.length());
        textField.contentValid = textField.textFieldAction.isContentValid(content);
        textField.textFieldAction.onContentChange(textField.content, textField.contentValid);
    }

    public void component_setDisabled(Component component, boolean disabled) {
        if (disabled) {
            if (component instanceof Combobox combobox) comboBox_close(combobox);
        }
        component.disabled = disabled;
    }

    public void component_addToWindow(Component component, UIEngineState uiEngineState, Window window) {
        if (component.addedToWindow != null) return;
        if (component.addedToScreen) return;
        if (component instanceof AppViewport appViewPort) uiEngineState.appViewPorts.add(appViewPort);
        component.addedToWindow = window;
        window.components.add(component);
    }

    public void component_addToScreen(Component component, UIEngineState uiEngineState) {
        if (component.addedToWindow != null) return;
        if (component.addedToScreen) return;
        if (component instanceof AppViewport appViewPort) uiEngineState.appViewPorts.add(appViewPort);
        component.addedToScreen = true;
        uiEngineState.screenComponents.add(component);
    }

    public void component_screenMoveToTop(Component component, UIEngineState uiEngineState) {
        for (int i = 0; i < uiEngineState.screenComponents.size; i++) {
            if (uiEngineState.screenComponents.get(i) == component) {
                uiEngineState.screenComponents.removeValue(component, true);
                uiEngineState.screenComponents.add(component);
            }
        }
    }

    public void component_windowMoveToTop(Component component, UIEngineState uiEngineState) {
        if (component.addedToWindow == null)
            return;
        Window window = component.addedToWindow;
        for (int i = 0; i < window.components.size; i++) {
            if (window.components.get(i) == component) {
                window.components.removeValue(component, true);
                window.components.add(component);
            }
        }
    }


    public void component_removeFromScreen(Component component, UIEngineState uiEngineState) {
        if (component.addedToWindow != null) return;
        if (!component.addedToScreen) return;

        // Remove References
        if (uiEngineState.lastUIMouseHover == component) uiEngineState.lastUIMouseHover = null;
        if (component.addedToTab != null) tab_removeComponent(component.addedToTab, component);
        if (component instanceof AppViewport appViewPort) uiEngineState.appViewPorts.removeValue(appViewPort, true);
        component_resetReferences(component);

        // Remove
        component.addedToScreen = true;
        uiEngineState.screenComponents.removeValue(component, true);
    }

    public void component_removeFromWindow(Component component, Window window, UIEngineState uiEngineState) {
        if (component.addedToWindow != window) return;
        if (component.addedToScreen) return;

        // Remove References
        if (uiEngineState.lastUIMouseHover == component) uiEngineState.lastUIMouseHover = null;
        if (component.addedToTab != null) tab_removeComponent(component.addedToTab, component);
        if (component instanceof AppViewport appViewPort) uiEngineState.appViewPorts.removeValue(appViewPort, true);
        component_resetReferences(component);

        // Remove
        component.addedToWindow.components.removeValue(component, true);
        component.addedToWindow = null;
    }

    public void tab_removeComponent(Tab tab, Component component) {
        if (component.addedToTab != tab) return;
        component.addedToTab.components.removeValue(component, true);
        component.addedToTab = tab;
    }

    public void tab_addComponent(Tab tab, Component component) {
        if (component.addedToTab != null) return;
        component.addedToTab = tab;
        tab.components.add(component);
    }

    public void tabBar_addTab(Tabbar tabBar, Tab tab) {
        if (tab.addedToTabBar != null) return;
        tab.addedToTabBar = tabBar;
        tabBar.tabs.add(tab);
    }

    public void tabBar_addTab(Tabbar tabBar, Tab tab, int index) {
        if (tab.addedToTabBar != null) return;
        tab.addedToTabBar = tabBar;
        tabBar.tabs.insert(index, tab);
    }

    public void tabBar_removeTab(Tabbar tabBar, Tab tab) {
        if (tab.addedToTabBar != tabBar) return;
        tab.addedToTabBar = null;
        tabBar.tabs.removeValue(tab, true);
    }

    public void contextMenu_addItem(ContextMenu contextMenu, ContextMenuItem contextMenuItem) {
        if (contextMenuItem.addedToContextMenu != null) return;
        contextMenuItem.addedToContextMenu = contextMenu;
        contextMenu.items.add(contextMenuItem);
    }

    public void contextMenu_removeItem(ContextMenu contextMenu, ContextMenuItem contextMenuItem) {
        if (contextMenuItem.addedToContextMenu != contextMenu) return;
        contextMenuItem.addedToContextMenu = null;
        contextMenu.items.removeValue(contextMenuItem, true);
    }

    public void contextMenu_selectItem(ContextMenuItem contextMenuItem) {
        if (contextMenuItem.addedToContextMenu == null) return;
        ContextMenu ContextMenu = contextMenuItem.addedToContextMenu;
        contextMenuItem.contextMenuItemAction.onSelect();
        ContextMenu.contextMenuAction.onItemSelected(contextMenuItem);
        contextMenu_close(ContextMenu);
    }

    public void comboBox_selectItem(ComboboxItem comboBoxItem) {
        if (comboBoxItem.addedToComboBox == null) return;
        Combobox comboBox = comboBoxItem.addedToComboBox;
        comboBox.selectedItem = comboBoxItem;
        comboBoxItem.comboBoxItemAction.onSelect();
        comboBox.comboBoxAction.onItemSelected(comboBoxItem);
        comboBox_close(comboBox);
    }

    public void comboBox_addItem(Combobox comboBox, ComboboxItem comboBoxItem) {
        if (comboBoxItem.addedToComboBox != null) return;
        comboBoxItem.addedToComboBox = comboBox;
        comboBox.items.add(comboBoxItem);
    }

    public void comboBox_removeItem(Combobox comboBox, ComboboxItem comboBoxItem) {
        if (comboBoxItem.addedToComboBox != comboBox) return;
        if (comboBox.selectedItem == comboBoxItem) comboBox.selectedItem = null;
        comboBoxItem.addedToComboBox = null;
        comboBox.items.removeValue(comboBoxItem, true);
    }

    public void tooltip_setImageSegmentImage(TooltipImageSegment tooltipImageSegment, CMediaSprite image) {
        tooltipImageSegment.image = image;
        if (tooltipImageSegment.image != null) {
            tooltipImageSegment.width = MathUtils.round((mediaManager.spriteWidth(image) + uiEngineState.tileSize.TS) / uiEngineState.tileSize.TSF);
            tooltipImageSegment.height = MathUtils.round((mediaManager.spriteHeight(image) + uiEngineState.tileSize.TS) / uiEngineState.tileSize.TSF);
        } else {
            tooltipImageSegment.width = 1;
            tooltipImageSegment.height = 1;
        }
    }

    public void tooltip_setTextSegmentText(TooltipTextSegment tooltipTextSegment, String text) {
        tooltipTextSegment.text = Tools.Text.validString(text);
        tooltipTextSegment.width = MathUtils.round((mediaManager.fontTextWidth(uiEngineState.config.ui.font, tooltipTextSegment.text) + uiEngineState.tileSize.TS) / uiEngineState.tileSize.TSF);
        tooltipTextSegment.height = 1;
    }

    public void tooltip_addTooltipSegment(Tooltip toolTip, TooltipSegment segment) {
        if (segment.addedToTooltip != null) return;
        segment.addedToTooltip = toolTip;
        toolTip.segments.add(segment);
    }

    public void tooltip_removeTooltipSegment(Tooltip toolTip, TooltipSegment segment) {
        if (segment.addedToTooltip != toolTip) return;
        segment.addedToTooltip = null;
        toolTip.segments.removeValue(segment, true);
    }

    public void tooltip_resizeSegment(TooltipSegment tooltipSegment, int width, int height) {
        tooltipSegment.width = Math.max(width, 0);
        tooltipSegment.height = Math.max(height, 0);
    }

    public boolean comboBox_isOpen(Combobox comboBox) {
        return uiEngineState.openComboBox != null && uiEngineState.openComboBox == comboBox;
    }

    public boolean contextMenu_isOpen(ContextMenu contextMenu) {
        return uiEngineState.openContextMenu != null && uiEngineState.openContextMenu == contextMenu;
    }

    public void comboBox_open(Combobox comboBox) {
        // Close other Comboboxes
        if (uiEngineState.openComboBox != null) {
            comboBox_close(uiEngineState.openComboBox);
        }
        // Open this one
        uiEngineState.openComboBox = comboBox;
        comboBox.comboBoxAction.onDisplay();
    }

    public void comboBox_close(Combobox comboBox) {
        if (comboBox_isOpen(comboBox)) {
            resetOpenComboBoxReference(uiEngineState);
            comboBox.comboBoxAction.onRemove();
        }
    }

    public boolean textField_isFocused(Textfield textField) {
        return uiEngineState.focusedTextField != null && uiEngineState.focusedTextField == textField;
    }

    public void textField_focus(Textfield textField) {
        // Unfocus other textfields
        if (uiEngineState.focusedTextField != null && uiEngineState.focusedTextField != textField) {
            textField_unFocus(uiEngineState.focusedTextField);
        }
        // Focus this one
        uiEngineState.focusedTextField = textField;
        textField.textFieldAction.onFocus();
    }

    public void textField_unFocus(Textfield textField) {
        if (textField_isFocused(textField)) {
            textField_resetMarkedContent(textField);
            resetFocusedTextFieldReference(uiEngineState);
            textField.textFieldAction.onUnFocus();
        }
    }

    public void list_setSelectedItem(List list, Object selectedItem) {
        // Clear selecteditem/items after mode switch
        if (selectedItem != null && list.items.contains(selectedItem, true)) {
            if (list.multiSelect) {
                list.selectedItems.add(selectedItem);
            } else {
                list.selectedItem = selectedItem;
            }
        } else {
            list.selectedItem = null;
        }
    }


    public void list_setSelectedItems(List list, Object[] selectedItems) {
        if (list.multiSelect) {
            list.selectedItems.clear();
            if (selectedItems != null) {
                for (int i = 0; i < selectedItems.length; i++) {
                    if (selectedItems[i] != null && list.items.contains(selectedItems[i], true)) {
                        list.selectedItems.add(selectedItems[i]);
                    }
                }
            }
        } else {
            if (selectedItems != null && selectedItems[0] != null && list.items.contains(selectedItems[0], true)) {
                list.selectedItem = selectedItems[0];
            } else {
                list.selectedItem = null;
            }
        }
    }


    public void list_setMultiSelect(List list, boolean multiSelect) {
        // Clear selecteditem/items after mode switch
        list.multiSelect = multiSelect;
        if (multiSelect) {
            list.selectedItem = null;
        } else {
            list.selectedItems.clear();
        }
    }


    public void knob_turnKnob(Knob knob, float newValue) {
        if (knob.endless) {
            if (newValue > 1) {
                newValue = newValue - 1f;
            } else if (newValue < 0) {
                newValue = 1f - Math.abs(newValue);
            }
        }
        float oldValue = knob.turned;
        knob.turned = Math.clamp(newValue, 0f, 1f);
        knob.knobAction.onTurned(knob.turned, (newValue - oldValue));
    }

    public boolean list_canDragIntoScreen(List list) {
        return list.dragEnabled && list.dragOutEnabled && list.listAction.canDragIntoApp();
    }

    public boolean grid_canDragIntoScreen(Grid grid) {
        return grid.dragEnabled && grid.dragOutEnabled && grid.gridAction.canDragIntoApp();
    }

    public boolean list_canDragIntoList(List list) {
        List draggedList = uiEngineState.draggedList;
        Grid draggedGrid = uiEngineState.draggedGrid;

        if (draggedList != null && draggedList == list && draggedList.dragEnabled) return true; // Into itself

        if (list.dragInEnabled && !list.disabled) {
            if (draggedGrid != null) {
                return !uiEngineState.draggedGrid.disabled && uiEngineState.draggedGrid.dragOutEnabled &&
                        list.listAction.canDragFromGrid(uiEngineState.draggedGrid);
            } else if (draggedList != null) {
                return !draggedList.disabled && draggedList.dragOutEnabled &&
                        list.listAction.canDragFromList(draggedList);
            }
        }
        return false;
    }

    public void tabBar_updateItemInfoAtMousePosition(Tabbar tabBar) {
        int x_bar = component_getAbsoluteX(tabBar);
        int y_bar = component_getAbsoluteY(tabBar);

        int tabXOffset = tabBar.tabOffset;
        for (int i = 0; i < tabBar.tabs.size; i++) {
            Tab tab = tabBar.tabs.get(i);
            int tabWidth = tabBar.bigIconMode ? 2 : tab.width;
            if ((tabXOffset + tabWidth) > tabBar.width) {
                break;
            }

            int tabHeight = tabBar.bigIconMode ? uiEngineState.tileSize.TS2 : uiEngineState.tileSize.TS;
            if (Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y, x_bar + (tabXOffset * uiEngineState.tileSize.TS), y_bar, tabWidth * uiEngineState.tileSize.TS, tabHeight)) {
                uiEngineState.itemInfo_tabBarTabIndex = i;
                uiEngineState.itemInfo_tabBarValid = true;
                return;
            }
            tabXOffset = tabXOffset + tabWidth;
        }


        uiEngineState.itemInfo_tabBarTabIndex = 0;
        uiEngineState.itemInfo_tabBarValid = false;
        return;
    }

    public void list_updateItemInfoAtMousePosition(List list) {
        if (list.items != null) {
            int itemFrom = MathUtils.round(list.scrolled * ((list.items.size) - (list.height)));
            itemFrom = Math.max(itemFrom, 0);
            int x_list = component_getAbsoluteX(list);
            int y_list = component_getAbsoluteY(list);
            // insert between other items
            for (int iy = 0; iy < list.height; iy++) {
                int itemIndex = itemFrom + iy;
                if (itemIndex < list.items.size) {
                    int itemOffsetY = ((list.height - 1) - iy);
                    if (Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y,
                            x_list, y_list + uiEngineState.tileSize.TL(itemOffsetY), uiEngineState.tileSize.TL(list.width), uiEngineState.tileSize.TS)) {
                        uiEngineState.itemInfo_listIndex = itemIndex;
                        uiEngineState.itemInfo_listValid = true;
                        return;
                    }
                }
            }
            // Insert at end
            if (Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y, x_list, y_list, uiEngineState.tileSize.TL(list.width), uiEngineState.tileSize.TL(list.height))) {
                uiEngineState.itemInfo_listIndex = list.items.size;
                uiEngineState.itemInfo_listValid = true;
                return;
            }

        }

        uiEngineState.itemInfo_listIndex = 0;
        uiEngineState.itemInfo_listValid = false;
        return;
    }

    public void grid_updateItemInfoAtMousePosition(Grid grid) {
        int tileSize = grid.bigMode ? uiEngineState.tileSize.TS2 : uiEngineState.tileSize.TS;
        int x_grid = component_getAbsoluteX(grid);
        int y_grid = component_getAbsoluteY(grid);
        int inv_to_x = (uiEngineState.mouse_ui.x - x_grid) / tileSize;
        int inv_to_y = (uiEngineState.mouse_ui.y - y_grid) / tileSize;
        if (grid_positionValid(grid, inv_to_x, inv_to_y)) {
            uiEngineState.itemInfo_gridPos.x = inv_to_x;
            uiEngineState.itemInfo_gridPos.y = inv_to_y;
            uiEngineState.itemInfo_gridValid = true;
            return;
        }
        uiEngineState.itemInfo_gridPos.set(0, 0);
        uiEngineState.itemInfo_gridValid = false;
        return;
    }

    public void grid_setSelectedItems(Grid grid, Object[] selectedItems) {
        if (grid.multiSelect) {
            grid.selectedItems.clear();
            if (selectedItems != null) {
                for (int i = 0; i < selectedItems.length; i++) {
                    if (selectedItems[i] != null && grid_contains(grid, selectedItems[i])) {
                        grid.selectedItems.add(selectedItems[i]);
                    }
                }
            }
        } else {
            if (selectedItems != null && selectedItems[0] != null && grid_contains(grid, selectedItems[0])) {
                grid.selectedItem = selectedItems[0];
            } else {
                grid.selectedItem = null;
            }
        }
    }

    public void grid_setSelectedItem(Grid grid, Object selectedItem) {
        if (selectedItem != null && grid_contains(grid, selectedItem)) {
            if (grid.multiSelect) {
                grid.selectedItems.add(selectedItem);
            } else {
                grid.selectedItem = selectedItem;
            }
        } else {
            grid.selectedItem = null;
        }
    }

    public boolean grid_contains(Grid grid, Object object) {
        for (int ix = 0; ix < grid.items.length; ix++) {
            for (int iy = 0; iy < grid.items[0].length; iy++) {
                if (grid.items[ix][iy] == object)
                    return true;
            }
        }
        return false;
    }

    public boolean grid_canDragIntoGrid(Grid grid) {
        List draggedList = uiEngineState.draggedList;
        Grid draggedGrid = uiEngineState.draggedGrid;

        if (draggedGrid != null && draggedGrid == grid && draggedGrid.dragEnabled) return true; // Into itself

        if (grid.dragInEnabled && !grid.disabled) {
            if (uiEngineState.draggedGridItem != null) {
                return !uiEngineState.draggedGrid.disabled && uiEngineState.draggedGrid.dragOutEnabled &&
                        grid.gridAction.canDragFromGrid(uiEngineState.draggedGrid);
            } else if (uiEngineState.draggedListItem != null) {
                return !draggedList.disabled && draggedList.dragOutEnabled &&
                        grid.gridAction.canDragFromList(draggedList);
            }
        }
        return false;
    }


    public void mouseTextInput_selectCharacter(MouseTextInput mouseTextInput, char selectChar) {
        findCharLoop:
        for (int i = 0; i < mouseTextInput.charactersLC.length; i++) {
            if (mouseTextInput.charactersLC[i] == selectChar || mouseTextInput.charactersUC[i] == selectChar) {
                mouseTextInput_selectIndex(mouseTextInput, i);
                mouseTextInput.upperCase = mouseTextInput.charactersUC[i] == selectChar;
                break findCharLoop;
            }
        }
    }

    public boolean mouseTextInput_isOpen() {
        return uiEngineState.openMouseTextInput != null;
    }

    public void mouseTextInput_open(MouseTextInput mouseTextInput) {
        if (mouseTextInput_isOpen()) return;
        uiEngineState.mTextInputMouseX = Gdx.input.getX();
        uiEngineState.mTextInputUnlock = false;
        uiEngineState.openMouseTextInput = mouseTextInput;
        uiEngineState.openMouseTextInput.mouseTextInputAction.onDisplay();
    }

    public void mouseTextInput_close(UIEngineState uiEngineState) {
        // mouseTextInput Keyboard
        resetMouseTextInputReference(uiEngineState);
    }

    public void resetMouseTextInputReference(UIEngineState uiEngineState) {
        if (mouseTextInput_isOpen()) {
            final MouseTextInput mouseTextInput = uiEngineState.openMouseTextInput;
            uiEngineState.openMouseTextInput = null;
            uiEngineState.mTextInputMouse1Pressed = false;
            uiEngineState.mTextInputMouse2Pressed = false;
            uiEngineState.mTextInputMouse3Pressed = false;
            uiEngineState.mTextInputGamePadLeft = false;
            uiEngineState.mTextInputGamePadRight = false;
            uiEngineState.mTextInputScrollTimer = 0;
            uiEngineState.mTextInputScrollTime = 0;
            uiEngineState.mTextInputScrollSpeed = 0;
            uiEngineState.mTextInputTranslatedMouse1Down = false;
            uiEngineState.mTextInputTranslatedMouse2Down = false;
            uiEngineState.mTextInputTranslatedMouse3Down = false;
            uiEngineState.mTextInputUnlock = false;
            mouseTextInput.mouseTextInputAction.onRemove();
        }
    }


    public void mouseTextInput_setCharacters(MouseTextInput mouseTextInput, char[] charactersLC, char[] charactersUC) {
        if (charactersLC == null || charactersUC == null) return;
        int maxCharacters = Math.min(charactersLC.length, charactersUC.length);
        mouseTextInput.charactersLC = new char[maxCharacters + 3];
        mouseTextInput.charactersUC = new char[maxCharacters + 3];
        for (int i = 0; i < maxCharacters; i++) {
            if (Character.isISOControl(charactersLC[i]) || Character.isISOControl(charactersUC[i]))
                throw new RuntimeException("ISO Control character not allowed");
            mouseTextInput.charactersLC[i] = charactersLC[i];
            mouseTextInput.charactersUC[i] = charactersUC[i];
        }
        // Control Buttons
        mouseTextInput.charactersLC[maxCharacters] = mouseTextInput.charactersUC[maxCharacters] = '\t';
        mouseTextInput.charactersLC[maxCharacters + 1] = mouseTextInput.charactersUC[maxCharacters + 1] = '\b';
        mouseTextInput.charactersLC[maxCharacters + 2] = mouseTextInput.charactersUC[maxCharacters + 2] = '\n';

    }

    public void text_setText(Text textC, String text) {
        textC.text = Tools.Text.validString(text);
    }

    public void image_setImage(Image imageC, CMediaSprite image) {
        imageC.image = image;
        image_updateSize(imageC);
    }


    public void image_updateSize(Image imageC) {
        imageC.width = imageC.image != null ? mediaManager.spriteWidth(imageC.image) / uiEngineState.tileSize.TS : 0;
        imageC.height = imageC.image != null ? mediaManager.spriteHeight(imageC.image) / uiEngineState.tileSize.TS : 0;
    }

    public void framebufferViewport_setFrameBuffer(FrameBufferViewport frameBufferViewport, NestedFrameBuffer nestedFrameBuffer) {
        frameBufferViewport.frameBuffer = nestedFrameBuffer;
        framebufferViewport_updateSize(frameBufferViewport);
    }

    public void framebufferViewport_updateSize(FrameBufferViewport frameBufferViewport) {
        frameBufferViewport.width = frameBufferViewport.frameBuffer != null ? frameBufferViewport.frameBuffer.getWidth() / uiEngineState.tileSize.TS : 0;
        frameBufferViewport.height = frameBufferViewport.frameBuffer != null ? frameBufferViewport.frameBuffer.getHeight() / uiEngineState.tileSize.TS : 0;
    }


    public void mouseTextInput_selectIndex(MouseTextInput mouseTextInput, int index) {
        int maxCharacters = Math.min(mouseTextInput.charactersLC.length, mouseTextInput.charactersUC.length);
        mouseTextInput.selectedIndex = Math.clamp(index, 0, (maxCharacters - 1));
    }

    public void scrollBar_scroll(Scrollbar scrollBar, float scrolled) {
        scrollBar.scrolled = Math.clamp(scrolled, 0f, 1f);
        scrollBar.scrollBarAction.onScrolled(scrollBar.scrolled);
    }

    public void scrollBar_pressButton(Scrollbar scrollBar) {
        if (scrollBar.buttonPressed) return;
        scrollBar.buttonPressed = true;
        scrollBar.scrollBarAction.onPress(scrollBar.scrolled);
    }

    public void scrollBar_releaseButton(Scrollbar scrollBar) {
        if (!scrollBar.buttonPressed) return;
        scrollBar.buttonPressed = false;
        scrollBar.scrollBarAction.onRelease(scrollBar.scrolled);
    }

    public float scrollBar_calculateScrolled(Scrollbar scrollBar, int mouse_ui_x, int mouse_ui_y) {
        int relativePos;
        float maxPos;
        float buttonOffset;
        switch (scrollBar) {
            case ScrollbarHorizontal scrollBarHorizontal -> {
                relativePos = mouse_ui_x - component_getAbsoluteX(scrollBarHorizontal);
                maxPos = uiEngineState.tileSize.TL(scrollBarHorizontal.width) - uiEngineState.tileSize.TS;
                buttonOffset = (1 / (float) scrollBarHorizontal.width) / 2f;
            }
            case ScrollbarVertical scrollBarVertical -> {
                relativePos = mouse_ui_y - component_getAbsoluteY(scrollBarVertical);
                maxPos = uiEngineState.tileSize.TL(scrollBarVertical.height) - uiEngineState.tileSize.TS;
                buttonOffset = (1 / (float) scrollBarVertical.height) / 2f;
            }
            default -> throw new IllegalStateException("Unexpected value: " + scrollBar);
        }

        return (relativePos / maxPos) - buttonOffset;
    }

    public void list_scroll(List list, float scrolled) {
        list.scrolled = Math.clamp(scrolled, 0f, 1f);
        list.listAction.onScrolled(list.scrolled);
    }


    public void appViewPort_resizeCameraTextureAndFrameBuffer(AppViewport appViewPort) {
        // Clean Up
        if (appViewPort.camera != null) appViewPort.camera = null;
        if (appViewPort.textureRegion != null) appViewPort.textureRegion.getTexture().dispose();
        if (appViewPort.frameBuffer != null) appViewPort.frameBuffer.dispose();

        int viewportWidth = uiEngineState.tileSize.TL(appViewPort.width);
        int viewportHeight = uiEngineState.tileSize.TL(appViewPort.height);
        // FrameBuffer
        appViewPort.frameBuffer = new NestedFrameBuffer(Pixmap.Format.RGB888, viewportWidth, viewportHeight, false);
        // Texture
        Texture texture = appViewPort.frameBuffer.getColorBufferTexture();
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        appViewPort.textureRegion = new TextureRegion(texture, viewportWidth, viewportHeight);
        appViewPort.textureRegion.flip(false, true);
        // Camera
        float x = appViewPort.camera.position.x;
        float y = appViewPort.camera.position.y;
        float z = appViewPort.camera.position.z;
        float zoom = appViewPort.camera.zoom;
        appViewPort.camera = new OrthographicCamera(viewportWidth, viewportHeight);
        appViewPort.camera.setToOrtho(false, viewportWidth, viewportHeight);
        camera_setZoom(appViewPort.camera, zoom);
        camera_setPosition(appViewPort.camera, x, y);
    }

    public int viewport_determineUpscaleFactor(int internalResolutionWidth, int internalResolutionHeight) {
        int upSampling = 1;
        int testWidth = Gdx.graphics.getDisplayMode().width;
        int testHeight = Gdx.graphics.getDisplayMode().height;
        while ((internalResolutionWidth * upSampling) < testWidth && (internalResolutionHeight * upSampling) < testHeight) {
            upSampling++;
        }
        return upSampling;
    }

    public void viewport_changeViewPortMode(VIEWPORT_MODE viewportMode) {
        if (viewportMode == null || viewportMode == uiEngineState.viewportMode) return;

        if (uiEngineState.viewportMode.upscale && !viewportMode.upscale) {
            uiEngineState.upScaleFactor_screen = 1;
            uiEngineState.frameBuffer_upScaled_screen.dispose();
            uiEngineState.frameBuffer_upScaled_screen = null;
        }
        if (!uiEngineState.viewportMode.upscale && viewportMode.upscale) {
            uiEngineState.upScaleFactor_screen = viewport_determineUpscaleFactor(uiEngineState.resolutionWidth, uiEngineState.resolutionHeight);
            uiEngineState.frameBuffer_upScaled_screen = new NestedFrameBuffer(Pixmap.Format.RGBA8888, uiEngineState.resolutionWidth * uiEngineState.upScaleFactor_screen, uiEngineState.resolutionHeight * uiEngineState.upScaleFactor_screen, false);
            uiEngineState.frameBuffer_upScaled_screen.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        // viewport_screen
        uiEngineState.viewport_screen = UICommonUtils.viewport_createViewport(viewportMode, uiEngineState.camera_ui, uiEngineState.resolutionWidth, uiEngineState.resolutionHeight);
        uiEngineState.viewport_screen.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        // viewportMode
        uiEngineState.viewportMode = viewportMode;
    }


    static Viewport viewport_createViewport(VIEWPORT_MODE viewportMode, OrthographicCamera camera_screen, int internalResolutionWidth, int internalResolutionHeight) {
        return switch (viewportMode) {
            case FIT -> new FitViewport(internalResolutionWidth, internalResolutionHeight, camera_screen);
            case PIXEL_PERFECT ->
                    new PixelPerfectViewport(internalResolutionWidth, internalResolutionHeight, camera_screen, 1);
            case STRETCH -> new StretchViewport(internalResolutionWidth, internalResolutionHeight, camera_screen);
        };
    }

    public void camera_setPosition(OrthographicCamera camera, float x, float y) {
        camera.position.set(x, y, 0f);
        camera.update();
    }

    public void camera_setZoom(OrthographicCamera camera, float zoom) {
        camera.zoom = Math.max(zoom, 0f);
        camera.update();
    }

    static float ui_getAnimationTimer(UIEngineState state) {
        if (state.config.ui.animationTimerFunction == null) return 0f;
        return state.config.ui.animationTimerFunction.getAnimationTimer();
    }

    public void window_resetReferences(Window window) {
        if (uiEngineState.draggedWindow == window) {
            uiEngineState.draggedWindow = null;
            uiEngineState.draggedWindow_offset.set(0, 0);
        }
    }

    public void component_resetReferences(Component component) {
        if (uiEngineState.pressedButton == component) resetPressedButtonReference(uiEngineState);
        if (uiEngineState.pressedScrollBarVertical == component) resetPressedScrollBarVerticalReference(uiEngineState);
        if (uiEngineState.pressedScrollBarHorizontal == component)
            resetPressedScrollBarHorizontalReference(uiEngineState);
        if (uiEngineState.pressedKnob == component) resetPressedKnobReference(uiEngineState);
        if (uiEngineState.pressedFramebufferViewport == component)
            resetPressedFrameBufferViewPortReference(uiEngineState);
        if (uiEngineState.pressedAppViewPort == component) resetPressedAppViewPortReference(uiEngineState);
        if (uiEngineState.pressedTextField == component) resetPressedTextFieldReference(uiEngineState);
        if (uiEngineState.focusedTextField == component) resetFocusedTextFieldReference(uiEngineState);
        if (uiEngineState.pressedCheckBox == component) resetPressedCheckBoxReference(uiEngineState);

        if (uiEngineState.draggedGrid == component) {
            resetDraggedGridReference(uiEngineState);
            resetPressedGridReference(uiEngineState);
        }
        if (uiEngineState.draggedList == component) {
            resetDraggedListReference(uiEngineState);
            resetPressedListReference(uiEngineState);
        }
        if (uiEngineState.openComboBox == component)
            resetOpenComboBoxReference(uiEngineState);
    }

    public void resetAllReferences(UIEngineState uiEngineState) {
        resetPressedButtonReference(uiEngineState);
        resetPressedScrollBarVerticalReference(uiEngineState);
        resetPressedScrollBarHorizontalReference(uiEngineState);
        resetPressedKnobReference(uiEngineState);
        resetPressedFrameBufferViewPortReference(uiEngineState);
        resetPressedAppViewPortReference(uiEngineState);
        resetPressedTextFieldReference(uiEngineState);
        resetFocusedTextFieldReference(uiEngineState);
        resetDraggedGridReference(uiEngineState);
        resetPressedGridReference(uiEngineState);
        resetDraggedListReference(uiEngineState);
        resetPressedListReference(uiEngineState);
        resetOpenComboBoxReference(uiEngineState);
        resetPressedCheckBoxReference(uiEngineState);
        resetOpenContextMenuReference(uiEngineState);
        resetMouseTextInputReference(uiEngineState);
    }


    public void resetOpenContextMenuReference(UIEngineState uiEngineState) {
        if (uiEngineState.openContextMenu != null) {
            ContextMenu contextMenu = uiEngineState.openContextMenu;
            resetPressedContextMenuItemReference(uiEngineState);
            uiEngineState.openContextMenu = null;
            uiEngineState.displayedContextMenuWidth = 0;
            contextMenu.contextMenuAction.onRemove();
        }
    }

    public void resetPressedContextMenuItemReference(UIEngineState uiEngineState) {
        uiEngineState.pressedContextMenuItem = null;
    }

    public void resetPressedCheckBoxReference(UIEngineState uiEngineState) {
        uiEngineState.pressedCheckBox = null;
    }

    public void resetPressedAppViewPortReference(UIEngineState uiEngineState) {
        uiEngineState.pressedAppViewPort = null;
    }

    public void resetPressedFrameBufferViewPortReference(UIEngineState uiEngineState) {
        uiEngineState.pressedFramebufferViewport = null;
    }

    public void resetPressedKnobReference(UIEngineState uiEngineState) {
        uiEngineState.pressedKnob = null;
    }

    public void resetPressedScrollBarVerticalReference(UIEngineState uiEngineState) {
        uiEngineState.pressedScrollBarVertical = null;
    }

    public void resetPressedScrollBarHorizontalReference(UIEngineState uiEngineState) {
        uiEngineState.pressedScrollBarHorizontal = null;
    }

    public void resetPressedButtonReference(UIEngineState uiEngineState) {
        if (uiEngineState.pressedButton != null && uiEngineState.pressedButton.mode == BUTTON_MODE.DEFAULT)
            uiEngineState.pressedButton.pressed = false;
        uiEngineState.pressedButton = null;
    }

    public void resetPressedTextFieldReference(UIEngineState uiEngineState) {
        uiEngineState.pressedTextField = null;
        uiEngineState.pressedTextFieldInitCaretPosition = 0;
    }

    public void resetOpenComboBoxReference(UIEngineState uiEngineState) {
        if (uiEngineState.openComboBox != null) {
            resetPressedComboBoxItemReference(uiEngineState);
            final Combobox combobox = uiEngineState.openComboBox;
            uiEngineState.openComboBox = null;
            combobox.comboBoxAction.onRemove();
        }
    }

    public void resetPressedComboBoxItemReference(UIEngineState uiEngineState) {
        uiEngineState.pressedComboBoxItem = null;
    }

    public void resetFocusedTextFieldReference(UIEngineState uiEngineState) {
        uiEngineState.focusedTextField = null;
        uiEngineState.focusedTextField_repeatedKey = KeyCode.NONE;
        uiEngineState.focusedTextField_repeatedKeyTimer = 0;
    }

    public void resetDraggedGridReference(UIEngineState uiEngineState) {
        uiEngineState.draggedGrid = null;
        uiEngineState.draggedGridFrom.set(0, 0);
        uiEngineState.draggedGridOffset.set(0, 0);
        uiEngineState.draggedGridItem = null;
    }

    public void resetDraggedListReference(UIEngineState uiEngineState) {
        uiEngineState.draggedList = null;
        uiEngineState.draggedListFromIndex = 0;
        uiEngineState.draggedListOffset.set(0, 0);
        uiEngineState.draggedListItem = null;
    }

    public void resetPressedGridReference(UIEngineState uiEngineState) {
        uiEngineState.pressedGrid = null;
        uiEngineState.pressedGridItem = null;
    }

    public void resetPressedListReference(UIEngineState uiEngineState) {
        uiEngineState.pressedList = null;
        uiEngineState.pressedListItem = null;
    }


    public Object getDraggedUIReference(UIEngineState uiEngineState) {
        if (uiEngineState.draggedWindow != null) return uiEngineState.draggedWindow;
        if (uiEngineState.draggedGrid != null) return uiEngineState.draggedGrid;
        if (uiEngineState.draggedList != null) return uiEngineState.draggedList;
        return null;
    }

    public Object getPressedUIReference(UIEngineState uiEngineState) {
        if (uiEngineState.pressedButton != null) return uiEngineState.pressedButton;
        if (uiEngineState.pressedScrollBarHorizontal != null) return uiEngineState.pressedScrollBarHorizontal;
        if (uiEngineState.pressedScrollBarVertical != null) return uiEngineState.pressedScrollBarVertical;
        if (uiEngineState.pressedKnob != null) return uiEngineState.pressedKnob;
        if (uiEngineState.pressedFramebufferViewport != null) return uiEngineState.pressedFramebufferViewport;
        if (uiEngineState.pressedTextField != null) return uiEngineState.pressedTextField;
        if (uiEngineState.pressedAppViewPort != null) return uiEngineState.pressedAppViewPort;
        if (uiEngineState.pressedGrid != null) return uiEngineState.pressedGrid;
        if (uiEngineState.pressedList != null) return uiEngineState.pressedList;
        if (uiEngineState.pressedContextMenuItem != null) return uiEngineState.pressedContextMenuItem;
        if (uiEngineState.pressedComboBoxItem != null) return uiEngineState.pressedComboBoxItem;
        if (uiEngineState.pressedCheckBox != null) return uiEngineState.pressedCheckBox;
        return null;
    }

    public Color color_darker(Color color) {
        float amount = 0.7f;
        return new Color(color.r * amount, color.g * amount, color.b * amount, color.a);
    }

    public Color color_brigther(Color color) {
        float amount = 1.3f;
        return new Color(color.r * amount, color.g * amount, color.b * amount, color.a);
    }

    public <T> T find(Array<T> array, Predicate predicate) {
        for (int i = 0; i < array.size; i++) {
            final T object = array.get(i);
            if (predicate.test(object))
                return object;
        }
        return null;
    }

    public <T> Array<T> findMultiple(Array<T> array, Predicate predicate) {
        Array<T> result = new Array<>();
        for (int i = 0; i < array.size; i++) {
            final T object = array.get(i);
            if (predicate.test(object))
                result.add(object);
        }
        return result;
    }


}
