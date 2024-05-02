package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.media.CMediaSprite;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.enums.VIEWPORT_MODE;
import net.mslivo.core.engine.ui_engine.input.KeyCode;
import net.mslivo.core.engine.ui_engine.render.misc.NestedFrameBuffer;
import net.mslivo.core.engine.ui_engine.render.misc.PixelPerfectViewport;
import net.mslivo.core.engine.ui_engine.ui.Window;
import net.mslivo.core.engine.ui_engine.ui.actions.MessageReceiverAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;
import net.mslivo.core.engine.ui_engine.ui.components.button.Button;
import net.mslivo.core.engine.ui_engine.ui.components.button.ButtonMode;
import net.mslivo.core.engine.ui_engine.ui.components.button.ImageButton;
import net.mslivo.core.engine.ui_engine.ui.components.button.TextButton;
import net.mslivo.core.engine.ui_engine.ui.components.canvas.Canvas;
import net.mslivo.core.engine.ui_engine.ui.components.canvas.CanvasImage;
import net.mslivo.core.engine.ui_engine.ui.components.checkbox.CheckBox;
import net.mslivo.core.engine.ui_engine.ui.components.combobox.ComboBox;
import net.mslivo.core.engine.ui_engine.ui.components.combobox.ComboBoxItem;
import net.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import net.mslivo.core.engine.ui_engine.ui.components.image.Image;
import net.mslivo.core.engine.ui_engine.ui.components.knob.Knob;
import net.mslivo.core.engine.ui_engine.ui.components.list.List;
import net.mslivo.core.engine.ui_engine.ui.components.progressbar.ProgressBar;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollBar;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollBarHorizontal;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollBarVertical;
import net.mslivo.core.engine.ui_engine.ui.components.tabbar.Tab;
import net.mslivo.core.engine.ui_engine.ui.components.tabbar.TabBar;
import net.mslivo.core.engine.ui_engine.ui.components.text.Text;
import net.mslivo.core.engine.ui_engine.ui.components.textfield.TextField;
import net.mslivo.core.engine.ui_engine.ui.components.viewport.AppViewPort;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenu;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenuItem;
import net.mslivo.core.engine.ui_engine.ui.hotkeys.HotKey;
import net.mslivo.core.engine.ui_engine.ui.notification.Notification;
import net.mslivo.core.engine.ui_engine.ui.ostextinput.MouseTextInput;
import net.mslivo.core.engine.ui_engine.ui.tooltip.ToolTip;
import net.mslivo.core.engine.ui_engine.ui.tooltip.ToolTipImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

class UICommons {

    private static IntSet textFieldControlKeys = new IntSet();
    private static IntSet textFieldRepeatedControlKeys = new IntSet();
    private static ArrayList<Component> windowComponentsVisibleOrder = new ArrayList<>();
    private static HashSet<Component> windowComponentsVisibleOrderSet = new HashSet<>();

    static {
        textFieldControlKeys.addAll(KeyCode.Key.LEFT, KeyCode.Key.RIGHT, KeyCode.Key.BACKSPACE, KeyCode.Key.FORWARD_DEL, Input.Keys.HOME, Input.Keys.END, Input.Keys.ENTER);
        textFieldRepeatedControlKeys.addAll(KeyCode.Key.LEFT, KeyCode.Key.RIGHT, KeyCode.Key.BACKSPACE, KeyCode.Key.FORWARD_DEL);
    }

    static void emulatedMouse_setPosition(InputState inputState, int x, int y) {
        if (!inputState.currentControlMode.emulated) return;
        // not possibe with hardware mouse - would be resetted instantly
        inputState.mouse_emulated.x = x;
        inputState.mouse_emulated.y = y;
    }

    static void emulatedMouse_setPositionComponent(InputState inputState, Component component) {
        if (component == null) return;
        if (component.addedToWindow == null && !component.addedToScreen) return;
        int x = component_getAbsoluteX(component) + (component_getAbsoluteWidth(component) / 2);
        int y = component_getAbsoluteY(component) + (component_getAbsoluteHeight(component) / 2);
        emulatedMouse_setPosition(inputState, x, y);
    }

    private static boolean emulatedMouse_isInteractAbleComponent(Component component) {
        if (!(component instanceof Image || component instanceof Text)) {
            if (component.visible && !component.disabled && !component_isHiddenByTab(component)) {
                return true;
            }
        }
        return false;
    }

    static void emulatedMouse_setPositionNextComponent(InputState inputState, boolean backwards) {
        Window activeWindow = window_findTopInteractableWindow(inputState);
        if (activeWindow != null && activeWindow.folded) {
            emulatedMouse_setPosition(inputState,
                    activeWindow.x + (window_getAbsoluteWidth(activeWindow) / 2),
                    activeWindow.y + (window_getAbsoluteHeight(activeWindow) - 4)
            );
            return;
        }
        windowComponentsVisibleOrder.clear();
        windowComponentsVisibleOrderSet.clear();
        int fromX = activeWindow != null ? activeWindow.x : 0;
        int fromY = activeWindow != null ? activeWindow.y : 0;
        int toX = activeWindow != null ? fromX + UICommons.window_getAbsoluteWidth(activeWindow) : inputState.resolutionWidth_ui;
        int toY = activeWindow != null ? fromY + UICommons.window_getAbsoluteHeight(activeWindow) : inputState.resolutionHeight_ui;

        int nearestIndex = -1;
        float nearestDistance = Float.MAX_VALUE;

        for (int iy = toY; iy >= fromY; iy -= UIEngine.TILE_SIZE) {
            for (int ix = fromX; ix <= toX; ix += UIEngine.TILE_SIZE) {
                Object object = UICommons.component_getUIObjectAtPosition(inputState, ix, iy);
                if (!windowComponentsVisibleOrderSet.contains(object) && object instanceof Component component && emulatedMouse_isInteractAbleComponent(component)) {
                    windowComponentsVisibleOrder.add(component);
                    windowComponentsVisibleOrderSet.add(component);
                    float distance = Tools.Calc.distanceFast(component_getAbsoluteX(component) + (component_getAbsoluteWidth(component) / 2),
                            component_getAbsoluteY(component) + (component_getAbsoluteHeight(component) / 2), inputState.mouse_emulated.x, inputState.mouse_emulated.y);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestIndex = windowComponentsVisibleOrder.size() - 1;
                    }
                }
            }
        }

        if (nearestIndex != -1) {
            if (backwards) {
                nearestIndex--;
                if (nearestIndex < 0) nearestIndex = (windowComponentsVisibleOrder.size() - 1);
            } else {
                nearestIndex++;
                if (nearestIndex > (windowComponentsVisibleOrder.size() - 1)) nearestIndex = 0;
            }

            emulatedMouse_setPositionComponent(inputState, windowComponentsVisibleOrder.get(nearestIndex));
        }

        return;
    }


    static Window window_findTopInteractableWindow(InputState inputState) {
        if (inputState.windows.isEmpty()) return null;
        for (int i = (inputState.windows.size() - 1); i >= 0; i--) {
            Window window = inputState.windows.get(i);
            if (window.visible) return window;
        }
        return null;
    }

    static int window_getAbsoluteWidth(Window window) {
        return window.width * UIEngine.TILE_SIZE;
    }

    static boolean window_isModalOpen(InputState inputState) {
        return inputState.modalWindow != null;
    }

    static void window_fold(Window window) {
        window.folded = true;
        if (window.windowAction != null) window.windowAction.onFold();
    }

    static void window_unFold(Window window) {
        window.folded = false;
        if (window.windowAction != null) window.windowAction.onUnfold();
    }

    static void window_receiveMessage(Window window, String message_type, Object... content) {
        if (message_type == null) return;
        for (int i2 = 0; i2 < window.messageReceiverActions.size(); i2++) {
            MessageReceiverAction messageReceiverAction = window.messageReceiverActions.get(i2);
            if (messageReceiverAction.messageType.equals(message_type)) {
                messageReceiverAction.onMessageReceived(content);
            }
        }
    }

    static void window_bringToFront(InputState inputState, Window window) {
        if (inputState.windows.size() == 1) return;
        if (window.alwaysOnTop) {
            if (inputState.windows.getLast() != window) {
                inputState.windows.remove(window);
                inputState.windows.add(window);
            }
        } else {
            int index = inputState.windows.size() - 1;
            searchIndex:
            while (index > 0) {
                if (!inputState.windows.get(index).alwaysOnTop) {
                    break searchIndex;
                }
                index = index - 1;
            }
            inputState.windows.remove(window);
            inputState.windows.add(index, window);
        }
    }

    static int window_getAbsoluteHeight(Window window) {
        return window.height * UIEngine.TILE_SIZE;
    }

    static void window_setPosition(InputState inputState, Window window, int x, int y) {
        window.x = x;
        window.y = y;
        if (window.enforceScreenBounds) window_enforceScreenBounds(inputState, window);
    }


    static void window_enforceScreenBounds(InputState inputState, Window window) {
        int wndWidth = window_getAbsoluteWidth(window);
        window.x = Math.clamp(window.x, 0, inputState.resolutionWidth_ui - wndWidth);
        if (window.folded) {
            window.y = Math.clamp(window.y, -((window.height - 1) * UIEngine.TILE_SIZE), inputState.resolutionHeight_ui - (window.height) * UIEngine.TILE_SIZE);
        } else {
            window.y = Math.clamp(window.y, 0, inputState.resolutionHeight_ui - window_getAbsoluteHeight(window));
        }
    }

    static void window_addToScreenAsModal(InputState inputState, Window window) {
        if (inputState.modalWindow == null) {
            window.alwaysOnTop = true;
            window.visible = true;
            window.folded = false;
            window.enforceScreenBounds = true;
            inputState.modalWindow = window;
            UICommons.window_center(inputState, window);
            UICommons.window_addToScreen(inputState, window);
        } else {
            inputState.modalWindowQueue.add(window);
        }
    }

    static void window_center(InputState inputState, Window window) {
        int centerX = (inputState.resolutionWidth_ui / 2) - (UICommons.window_getAbsoluteWidth(window) / 2);
        int centerY = (inputState.resolutionHeight_ui / 2) - ((window.folded ? UIEngine.TILE_SIZE : UICommons.window_getAbsoluteHeight(window)) / 2);
        window_setPosition(inputState, window, centerX, centerY);
    }

    static void window_addToScreen(InputState inputState, Window window) {
        if (window.addedToScreen) return;
        window.addedToScreen = true;
        inputState.windows.add(window);
        if (window.windowAction != null) window.windowAction.onAdd();
        window_enforceScreenBounds(inputState, window);
    }

    static boolean window_close(InputState inputState, Window window) {
        for (int i = 0; i < window.components.size(); i++) {
            if (window.components.get(i).name.equals(UIEngine.WND_CLOSE_BUTTON) && window.components.get(i) instanceof Button closeButton) {
                if (closeButton.buttonAction != null) {
                    UICommons.button_press(closeButton);
                    UICommons.button_release(closeButton);
                    return true;
                }
            }
        }
        return false;
    }

    static void window_removeFromScreen(InputState inputState, Window window) {
        if (!window.addedToScreen) return;

        // Remove References
        if (inputState.lastUIMouseHover == window) inputState.lastUIMouseHover = null;
        if (UICommons.window_isModalOpen(inputState) && inputState.modalWindow == window) inputState.modalWindow = null;
        for (int i = 0; i < window.components.size(); i++)
            if (window.components.get(i) instanceof AppViewPort appViewPort)
                inputState.appViewPorts.remove(appViewPort);
        window_resetReferences(inputState, window);

        // Remove
        window.addedToScreen = false;
        inputState.windows.remove(window);
        if (window.windowAction != null) window.windowAction.onRemove();

        // Add Next Modal from Queue queue
        if (inputState.modalWindowQueue.size() > 0)
            window_addToScreenAsModal(inputState, inputState.modalWindowQueue.poll());
    }

    static void component_setSize(InputState inputState, Component component, int width, int height) {
        component.width = Math.clamp(width, 1, Integer.MAX_VALUE);
        component.height = Math.clamp(height, 1, Integer.MAX_VALUE);

        if (component instanceof AppViewPort appViewPort) {
            appViewPort_resizeCameraTextureAndFrameBuffer(appViewPort);
        }
        if (component instanceof Canvas canvas) {
            canvas_resizeMap(canvas);
        }
    }

    static int component_getParentWindowX(Component component) {
        return component.addedToWindow != null ? component.addedToWindow.x : 0;
    }

    static int component_getParentWindowY(Component component) {
        return component.addedToWindow != null ? component.addedToWindow.y : 0;
    }

    static int component_getAbsoluteX(Component component) {
        return component_getParentWindowX(component) + component.x;
    }

    static int component_getAbsoluteY(Component component) {
        return component_getParentWindowY(component) + component.y;
    }

    static int component_getRelativeMouseX(int mouse_ui_x, Component component) {
        return mouse_ui_x - component_getAbsoluteY(component);
    }

    static int component_getRelativeMouseY(int mouse_ui_y, Component component) {
        return mouse_ui_y - component_getAbsoluteY(component);
    }


    static int component_getAbsoluteWidth(Component component) {
        return component.width * UIEngine.TILE_SIZE;
    }

    static int component_getAbsoluteHeight(Component component) {
        return component.height * UIEngine.TILE_SIZE;
    }

    static Object component_getUIObjectAtPosition(InputState inputState, int x, int y) {
        // Notification Collision
        for (int i = 0; i < inputState.notifications.size(); i++) {
            Notification notification = inputState.notifications.get(i);
            if (notification.notificationAction != null && Tools.Calc.pointRectsCollide(x, y,
                    0, inputState.resolutionWidth_ui - ((i + 1) * UIEngine.TILE_SIZE),
                    inputState.resolutionWidth_ui, UIEngine.TILE_SIZE)) {
                return notification;
            }
        }

        // Context Menu Item collision
        if (inputState.openContextMenu != null) {
            for (int i = 0; i < inputState.openContextMenu.items.size(); i++) {
                if (Tools.Calc.pointRectsCollide(x, y, inputState.openContextMenu.x, inputState.openContextMenu.y - (UIEngine.TILE_SIZE) - (i * UIEngine.TILE_SIZE), inputState.displayedContextMenuWidth * UIEngine.TILE_SIZE, UIEngine.TILE_SIZE)) {
                    return inputState.openContextMenu.items.get(i);
                }
            }
        }

        // Combobox Open Menu collision
        if (inputState.openComboBox != null) {
            if (Tools.Calc.pointRectsCollide(x, y, UICommons.component_getAbsoluteX(inputState.openComboBox), UICommons.component_getAbsoluteY(inputState.openComboBox) - (inputState.openComboBox.comboBoxItems.size() * UIEngine.TILE_SIZE), inputState.openComboBox.width * UIEngine.TILE_SIZE, (inputState.openComboBox.comboBoxItems.size() * UIEngine.TILE_SIZE))) {
                return inputState.openComboBox;
            }
        }

        // Window / WindowComponent collision
        windowLoop:
        for (int i = inputState.windows.size() - 1; i >= 0; i--) { // use for(i) to avoid iterator creation
            Window window = inputState.windows.get(i);
            if (!window.visible) continue windowLoop;

            int wndX = window.x;
            int wndY = window.y + (window.folded ? ((window.height - 1) * UIEngine.TILE_SIZE) : 0);
            int wndWidth = UICommons.window_getAbsoluteWidth(window);
            int wndHeight = window.folded ? UIEngine.TILE_SIZE : UICommons.window_getAbsoluteHeight(window);

            boolean collidesWithWindow = Tools.Calc.pointRectsCollide(x, y, wndX, wndY, wndWidth, wndHeight);
            if (collidesWithWindow) {
                for (int ic = window.components.size() - 1; ic >= 0; ic--) {
                    Component component = window.components.get(ic);
                    if (component_isComponentAtPosition(x, y, component)) {
                        return component;
                    }
                }
                return window;
            }
        }

        // Screen component collision
        for (int i = 0; i < inputState.screenComponents.size(); i++) {
            Component screenComponent = inputState.screenComponents.get(i);
            if (component_isComponentAtPosition(x, y, screenComponent)) return screenComponent;
        }
        return null;
    }

    static boolean component_isComponentAtPosition(int x, int y, Component component) {
        if (!component.visible) return false;
        if (component.disabled) return false;
        if (UICommons.component_isHiddenByTab(component)) return false;

        if (Tools.Calc.pointRectsCollide(x, y, UICommons.component_getAbsoluteX(component), UICommons.component_getAbsoluteY(component), component.width * UIEngine.TILE_SIZE, component.height * UIEngine.TILE_SIZE)) {
            return true;
        }
        return false;
    }

    static boolean component_isHiddenByTab(Component component) {
        if (component.addedToTab == null) return false;
        Tab selectedTab = UICommons.tabBar_getSelectedTab(component.addedToTab.addedToTabBar);
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

    static void hotkey_press(HotKey hotKey) {
        hotKey.pressed = true;
        if (hotKey.hotKeyAction != null) hotKey.hotKeyAction.onPress();
    }

    static void hotkey_release(HotKey hotKey) {
        hotKey.pressed = false;
        if (hotKey.hotKeyAction != null) hotKey.hotKeyAction.onRelease();
    }

    static void checkbox_check(CheckBox checkBox) {
        if (checkBox.checked) return;
        checkBox.checked = true;
        if (checkBox.checkBoxAction != null) checkBox.checkBoxAction.onCheck(true);
    }

    static void checkbox_unCheck(CheckBox checkBox) {
        if (!checkBox.checked) return;
        checkBox.checked = false;
        if (checkBox.checkBoxAction != null) checkBox.checkBoxAction.onCheck(false);
    }

    static void window_resetReferences(InputState inputState, Window window) {
        if(inputState.draggedWindow == window){
            inputState.draggedWindow = null;
            inputState.draggedWindow_offset.set(0,0);
        }
    }

    static void component_resetReferences(InputState inputState, Component component) {
        if (inputState.pressedButton == component) inputState.pressedButton = null;
        if (inputState.pressedScrollBarVertical == component) inputState.pressedScrollBarVertical = null;
        if (inputState.pressedScrollBarHorizontal == component) inputState.pressedScrollBarHorizontal = null;
        if (inputState.pressedKnob == component) inputState.pressedKnob = null;
        if (inputState.pressedCanvas == component) inputState.pressedCanvas = null;
        if (inputState.pressedAppViewPort == component) inputState.pressedAppViewPort = null;
        if (inputState.pressedTextField == component) {
            inputState.pressedTextField = null;
            inputState.pressedTextFieldMouseX = 0;
        }
        if (inputState.focusedTextField == component) {
            inputState.focusedTextField = null;
            inputState.focusedTextField_repeatedKey = KeyCode.NONE;
            inputState.focusedTextField_repeatedKeyTimer = 0;
        }
        if (inputState.draggedGrid == component) {
            inputState.draggedGrid = null;
            inputState.draggedGridFrom.set(0, 0);
            inputState.draggedGridOffset.set(0, 0);
            inputState.draggedGridItem = null;
            inputState.pressedGrid = null;
            inputState.pressedGridItem = null;
        }
        if (inputState.draggedList == component) {
            inputState.draggedList = null;
            inputState.draggedListFromIndex = 0;
            inputState.draggedListOffsetX.set(0, 0);
            inputState.draggedListItem = null;
            inputState.pressedList = null;
            inputState.pressedListItem = null;
        }
        if (inputState.openComboBox == component) {
            inputState.openComboBox = null;
            inputState.pressedComboBoxItem = null;
        }
        if (inputState.pressedCheckBox == component) {
            inputState.pressedCheckBox = null;
        }
    }

    static Object getDraggedUIReference(InputState inputState) {
        if (inputState.draggedWindow != null) return inputState.draggedWindow;
        if (inputState.draggedGrid != null) return inputState.draggedGrid;
        if (inputState.draggedList != null) return inputState.draggedList;
        return null;
    }

    static Object getPressedUIReference(InputState inputState) {
        if (inputState.pressedButton != null) return inputState.pressedButton;
        if (inputState.pressedScrollBarHorizontal != null) return inputState.pressedScrollBarHorizontal;
        if (inputState.pressedScrollBarVertical != null) return inputState.pressedScrollBarVertical;
        if (inputState.pressedKnob != null) return inputState.pressedKnob;
        if (inputState.pressedCanvas != null) return inputState.pressedCanvas;
        if (inputState.pressedTextField != null) return inputState.pressedTextField;
        if (inputState.pressedAppViewPort != null) return inputState.pressedAppViewPort;
        if (inputState.pressedGrid != null) return inputState.pressedGrid;
        if (inputState.pressedList != null) return inputState.pressedList;
        if (inputState.pressedContextMenuItem != null) return inputState.pressedContextMenuItem;
        if (inputState.pressedComboBoxItem != null) return inputState.pressedComboBoxItem;
        if (inputState.pressedCheckBox != null) return inputState.pressedCheckBox;
        return null;
    }

    static void setMouseInteractedUIObject(InputState inputState, Object object) {
        inputState.mouseInteractedUIObjectFrame = object;
    }

    static void setKeyboardInteractedUIObject(InputState inputState, Object object) {
        inputState.keyboardInteractedUIObjectFrame = object;
    }

    static void notification_addToScreen(InputState inputState, Notification notification, int notificationsMax) {
        if (notification.addedToScreen) return;
        notification.addedToScreen = true;
        inputState.notifications.add(notification);
        // Remove first if too many
        if (inputState.notifications.size() > notificationsMax)
            notification_removeFromScreen(inputState, inputState.notifications.getFirst());
    }

    static void notification_removeFromScreen(InputState inputState, Notification notification) {
        if (!notification.addedToScreen) return;
        notification.addedToScreen = false;
        inputState.notifications.remove(notification);
    }

    static boolean contextMenu_openAtMousePosition(InputState inputState, MediaManager mediaManager, ContextMenu contextMenu) {
        boolean success = contextMenu_open(inputState, mediaManager, contextMenu, inputState.mouse_ui.x, inputState.mouse_ui.y);
        if (success && (inputState.currentControlMode.emulated)) {
            // emulated mode: move mouse onto the opened menu
            inputState.mouse_emulated.x += UIEngine.TILE_SIZE_2;
            inputState.mouse_emulated.y -= UIEngine.TILE_SIZE_2;
        }
        return success;
    }

    static boolean contextMenu_open(InputState inputState, MediaManager mediaManager, ContextMenu contextMenu, int x, int y) {
        if (contextMenu.items.size() == 0) return false;
        // Close open ContextMenus
        if (inputState.openContextMenu != null) {
            contextMenu_close(inputState, inputState.openContextMenu);
        }
        // Open this one
        contextMenu.x = x;
        contextMenu.y = y;
        int textwidth = 0;
        for (int i = 0; i < contextMenu.items.size(); i++) {
            ContextMenuItem contextMenuItem = contextMenu.items.get(i);
            int w = mediaManager.getCMediaFontTextWidth(contextMenuItem.font, contextMenuItem.text);
            if (contextMenuItem.icon != null) w = w + UIEngine.TILE_SIZE;
            if (w > textwidth) textwidth = w;
        }
        inputState.displayedContextMenuWidth = (textwidth + UIEngine.TILE_SIZE) / UIEngine.TILE_SIZE;
        inputState.openContextMenu = contextMenu;
        if (inputState.openContextMenu.contextMenuAction != null) inputState.openContextMenu.contextMenuAction.onOpen();
        return true;
    }

    static void contextMenu_close(InputState inputState, ContextMenu contextMenu) {
        if (contextMenu_isOpen(inputState, contextMenu)) {
            inputState.openContextMenu = null;
            inputState.displayedContextMenuWidth = 0;
            inputState.pressedContextMenuItem = null;
            if (contextMenu.contextMenuAction != null) contextMenu.contextMenuAction.onClose();
        }
    }

    static String progressBar_getProgressText(float progress) {
        return Tools.Text.formatPercent(progress);
    }

    static String progressBar_getProgressText2Decimal(float progress) {
        return Tools.Text.formatPercentDecimal(progress);
    }


    static Tab tabBar_getSelectedTab(TabBar tabBar) {
        if (tabBar == null) return null;
        return tabBar.tabs.get(Math.clamp(tabBar.selectedTab, 0, tabBar.tabs.size() - 1));
    }

    static void tabBar_selectTab(TabBar tabBar, Tab tab) {
        if (tab.addedToTabBar != tabBar) return;
        for (int i = 0; i < tabBar.tabs.size(); i++) {
            if (tabBar.tabs.get(i) == tab) {
                UICommons.tabBar_selectTab(tabBar, i);
                return;
            }
        }
    }

    static void tabBar_selectTab(TabBar tabBar, int index) {
        tabBar.selectedTab = Math.clamp(index, 0, tabBar.tabs.size() - 1);
        Tab tab = tabBar.tabs.get(tabBar.selectedTab);
        if (tab.tabAction != null) tab.tabAction.onSelect();
        if (tabBar.tabBarAction != null)
            tabBar.tabBarAction.onChangeTab(index, tab);
    }


    static void button_press(Button button) {
        if (button.pressed || button.mode != ButtonMode.DEFAULT) return;
        button.pressed = true;
        if (button.buttonAction != null) button.buttonAction.onPress();
    }

    static void button_release(Button button) {
        if (!button.pressed || button.mode != ButtonMode.DEFAULT) return;
        button.pressed = false;
        if (button.buttonAction != null) button.buttonAction.onRelease();
    }

    static void button_toggle(Button button) {
        button_toggle(button, !button.pressed);
    }

    static void progressbar_setProgress(ProgressBar progressBar, float progress) {
        progressBar.progress = Math.clamp(progress, 0f, 1f);
    }

    static void button_toggle(Button button, boolean pressed) {
        if (button.toggleDisabled || button.pressed == pressed || button.mode != ButtonMode.TOGGLE) return;
        button.pressed = pressed;
        if (button.buttonAction != null) button.buttonAction.onToggle(button.pressed);
    }

    static void button_centerContent(MediaManager mediaManager, Button button) {
        if (button == null) return;
        if (button instanceof ImageButton imageButton) {
            if (imageButton.image == null) return;
            imageButton.contentOffset_x = MathUtils.round(((imageButton.width * UIEngine.TILE_SIZE) - mediaManager.getCMediaSpriteWidth(imageButton.image)) / 2f);
            imageButton.contentOffset_y = MathUtils.round(((imageButton.height * UIEngine.TILE_SIZE) - mediaManager.getCMediaSpriteHeight(imageButton.image)) / 2f);

        } else if (button instanceof TextButton textButton) {
            if (textButton.text == null) return;
            int iconWidth = textButton.icon != null ? UIEngine.TILE_SIZE : 0;
            int contentWidth = mediaManager.getCMediaFontTextWidth(textButton.font, textButton.text) + 1 + iconWidth;
            int contentHeight = mediaManager.getCMediaFontTextHeight(textButton.font, textButton.text);
            textButton.contentOffset_x = MathUtils.round(((textButton.width * UIEngine.TILE_SIZE) - contentWidth) / 2f);
            textButton.contentOffset_y = MathUtils.round((((textButton.height * UIEngine.TILE_SIZE) - contentHeight)) / 2f) - 2;
        }
    }

    static boolean grid_positionValid(Grid grid, int x, int y) {
        if (grid.items != null) {
            return x >= 0 && x < grid.items.length && y >= 0 && y < grid.items[0].length;
        }
        return false;
    }

    static void grid_setItems(Grid grid, Object[][] items) {
        grid.items = items;
        grid_updateSize(grid);
    }

    static void grid_updateSize(Grid grid) {
        int factor = grid.doubleSized ? 2 : 1;
        if (grid.items != null) {
            grid.width = grid.items.length * factor;
            grid.height = grid.items[0].length * factor;
        } else {
            grid.width = 1;
            grid.height = 1;
        }
    }

    static void textField_setMarkerPosition(MediaManager mediaManager, TextField textField, int position) {
        textField.markerPosition = Math.clamp(position, 0, textField.content.length());
        if (textField.markerPosition < textField.offset) {
            while (textField.markerPosition < textField.offset) {
                textField.offset--;
            }
        } else {
            String subContent = textField.content.substring(textField.offset, textField.markerPosition);
            int width = (textField.width * UIEngine.TILE_SIZE) - 4;
            if (mediaManager.getCMediaFontTextWidth(textField.font, subContent) > width) {
                while (mediaManager.getCMediaFontTextWidth(textField.font, subContent) > width) {
                    textField.offset++;
                    subContent = textField.content.substring(textField.offset, textField.markerPosition);
                }
            }
        }
    }

    static boolean textField_isControlKey(int keyCode) {
        return textFieldControlKeys.contains(keyCode);
    }

    static boolean textField_isRepeatedControlKey(int keyCode) {
        return textFieldRepeatedControlKeys.contains(keyCode);
    }

    static void textField_executeControlKey(InputState inputState, MediaManager mediaManager, TextField textField, int keyCode) {
        switch (keyCode) {
            case Input.Keys.LEFT -> textField_setMarkerPosition(mediaManager, textField, textField.markerPosition - 1);
            case Input.Keys.RIGHT -> textField_setMarkerPosition(mediaManager, textField, textField.markerPosition + 1);
            case Input.Keys.BACKSPACE -> {
                if (!textField.content.isEmpty() && textField.markerPosition > 0) {
                    String newContent = textField.content.substring(0, textField.markerPosition - 1) + textField.content.substring(textField.markerPosition);
                    UICommons.textField_setMarkerPosition(mediaManager, textField, textField.markerPosition - 1);
                    UICommons.textField_setContent(textField, newContent);
                }
            }
            case Input.Keys.FORWARD_DEL -> {
                if (!textField.content.isEmpty() && textField.markerPosition < textField.content.length()) {
                    String newContent = textField.content.substring(0, textField.markerPosition) + textField.content.substring(textField.markerPosition + 1);
                    UICommons.textField_setContent(textField, newContent);
                }
            }
            case Input.Keys.HOME -> UICommons.textField_setMarkerPosition(mediaManager, textField, 0);
            case Input.Keys.END ->
                    UICommons.textField_setMarkerPosition(mediaManager, textField, textField.content.length());
            case Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> {
                UICommons.textField_unFocus(inputState, textField); // Unfocus
                if (textField.textFieldAction != null)
                    textField.textFieldAction.onEnter(textField.content, textField.contentValid);
            }
            default -> {
            }
        }
    }

    static void textField_typeCharacter(MediaManager mediaManager, TextField textField, char character) {
        if (textField.allowedCharacters == null || textField.allowedCharacters.contains(character)) {
            String newContent = textField.content.substring(0, textField.markerPosition) + character + textField.content.substring(textField.markerPosition);
            UICommons.textField_setContent(textField, newContent);
            UICommons.textField_setMarkerPosition(mediaManager, textField, textField.markerPosition + 1);
            if (textField.textFieldAction != null)
                textField.textFieldAction.onTyped(character);
        }
    }

    static void textField_setContent(TextField textField, String content) {
        if (content.length() > textField.contentMaxLength) content = content.substring(0, textField.contentMaxLength);
        textField.content = Tools.Text.validString(content);
        textField.markerPosition = Math.clamp(textField.markerPosition, 0, textField.content.length());
        if (textField.textFieldAction != null) {
            textField.contentValid = textField.textFieldAction.isContentValid(content);
            textField.textFieldAction.onContentChange(textField.content, textField.contentValid);
        } else {
            textField.contentValid = true;
        }
    }

    static void component_addToWindow(Component component, InputState inputState, Window window) {
        if (component.addedToWindow != null) return;
        if (component.addedToScreen) return;
        if (component instanceof AppViewPort appViewPort) inputState.appViewPorts.add(appViewPort);
        component.addedToWindow = window;
        window.components.add(component);
    }

    static void component_addToScreen(Component component, InputState inputState) {
        if (component.addedToWindow != null) return;
        if (component.addedToScreen) return;
        if (component instanceof AppViewPort appViewPort) inputState.appViewPorts.add(appViewPort);
        component.addedToScreen = true;
        inputState.screenComponents.add(component);
    }

    static void component_removeFromScreen(Component component, InputState inputState) {
        if (component.addedToWindow != null) return;
        if (!component.addedToScreen) return;

        // Remove References
        if (inputState.lastUIMouseHover == component) inputState.lastUIMouseHover = null;
        if (component.addedToTab != null) tab_removeComponent(component.addedToTab, component);
        if (component instanceof AppViewPort appViewPort) inputState.appViewPorts.remove(appViewPort);
        component_resetReferences(inputState, component);

        // Remove
        component.addedToScreen = true;
        inputState.screenComponents.remove(component);
    }

    static void component_removeFromWindow(Component component, Window window, InputState inputState) {
        if (component.addedToWindow != window) return;
        if (component.addedToScreen) return;

        // Remove References
        if (inputState.lastUIMouseHover == component) inputState.lastUIMouseHover = null;
        if (component.addedToTab != null) tab_removeComponent(component.addedToTab, component);
        if (component instanceof AppViewPort appViewPort) inputState.appViewPorts.remove(appViewPort);
        component_resetReferences(inputState, component);

        // Remove
        component.addedToWindow = null;
        component.addedToWindow.components.remove(component);
    }

    static void tab_removeComponent(Tab tab, Component component) {
        if (component.addedToTab != tab) return;
        component.addedToTab.components.remove(component);
        component.addedToTab = tab;
    }

    static void tab_addComponent(Tab tab, Component component) {
        if (component.addedToTab != null) return;
        component.addedToTab = tab;
        tab.components.add(component);
    }

    static void tabBar_addTab(TabBar tabBar, Tab tab) {
        if (tab.addedToTabBar != null) return;
        tab.addedToTabBar = tabBar;
        tabBar.tabs.add(tab);
    }

    static void tabBar_addTab(TabBar tabBar, Tab tab, int index) {
        if (tab.addedToTabBar != null) return;
        tab.addedToTabBar = tabBar;
        tabBar.tabs.add(index, tab);
    }

    static void tabBar_removeTab(TabBar tabBar, Tab tab) {
        if (tab.addedToTabBar != tabBar) return;
        tab.addedToTabBar = null;
        tabBar.tabs.remove(tab);
    }

    static void contextMenu_addItem(ContextMenu contextMenu, ContextMenuItem contextMenuItem) {
        if (contextMenuItem.addedToContextMenu != null) return;
        contextMenuItem.addedToContextMenu = contextMenu;
        contextMenu.items.add(contextMenuItem);
    }

    static void contextMenu_removeItem(ContextMenu contextMenu, ContextMenuItem contextMenuItem) {
        if (contextMenuItem.addedToContextMenu != contextMenu) return;
        contextMenuItem.addedToContextMenu = null;
        contextMenu.items.remove(contextMenuItem);
    }

    static void contextMenu_selectItem(InputState inputState, ContextMenuItem contextMenuItem) {
        if (contextMenuItem.addedToContextMenu == null) return;
        ContextMenu ContextMenu = contextMenuItem.addedToContextMenu;
        if (contextMenuItem.contextMenuItemAction != null)
            contextMenuItem.contextMenuItemAction.onSelect();
        if (ContextMenu.contextMenuAction != null)
            ContextMenu.contextMenuAction.onItemSelected(contextMenuItem);
        UICommons.contextMenu_close(inputState, ContextMenu);
    }

    static void comboBox_selectItem(InputState inputState, ComboBoxItem comboBoxItem) {
        if (comboBoxItem.addedToComboBox == null) return;
        ComboBox comboBox = comboBoxItem.addedToComboBox;
        comboBox.selectedItem = comboBoxItem;
        if (comboBoxItem.comboBoxItemAction != null)
            comboBoxItem.comboBoxItemAction.onSelect();
        if (comboBox.comboBoxAction != null)
            comboBox.comboBoxAction.onItemSelected(comboBoxItem);
        UICommons.comboBox_close(inputState, comboBox);
    }

    static void comboBox_addItem(ComboBox comboBox, ComboBoxItem comboBoxItem) {
        if (comboBoxItem.addedToComboBox != null) return;
        comboBoxItem.addedToComboBox = comboBox;
        comboBox.comboBoxItems.add(comboBoxItem);
    }

    static void comboBox_removeItem(ComboBox comboBox, ComboBoxItem comboBoxItem) {
        if (comboBoxItem.addedToComboBox != comboBox) return;
        if (comboBox.selectedItem == comboBoxItem) comboBox.selectedItem = null;
        comboBoxItem.addedToComboBox = null;
        comboBox.comboBoxItems.remove(comboBoxItem);
    }


    static void canvas_addCanvasImage(Canvas canvas, CanvasImage canvasImage) {
        if (canvasImage.addedToCanvas != null) return;
        canvasImage.addedToCanvas = canvas;
        canvas.canvasImages.add(canvasImage);
    }

    static void canvas_resizeMap(Canvas canvas) {
        int newWidth = canvas.width * UIEngine.TILE_SIZE;
        int newHeight = canvas.height * UIEngine.TILE_SIZE;
        Color[][] newMap;
        newMap = Arrays.copyOf(canvas.map, newWidth);
        for (int ix = 0; ix < newWidth; ix++) {
            newMap[ix] = Arrays.copyOf(canvas.map[ix], newHeight);
        }
        canvas.map = newMap;
    }

    static void canvas_removeCanvasImage(Canvas canvas, CanvasImage canvasImage) {
        if (canvasImage.addedToCanvas != canvas) return;
        canvasImage.addedToCanvas = null;
        canvas.canvasImages.remove(canvasImage);
    }


    static void toolTip_addToolTipImage(ToolTip toolTip, ToolTipImage toolTipImage) {
        if (toolTipImage.addedToToolTip != null) return;
        toolTipImage.addedToToolTip = toolTip;
        toolTip.images.add(toolTipImage);
    }

    static void toolTip_removeToolTipImage(ToolTip toolTip, ToolTipImage toolTipImage) {
        if (toolTipImage.addedToToolTip != toolTip) return;
        toolTipImage.addedToToolTip = null;
        toolTip.images.remove(toolTipImage);
    }

    static boolean comboBox_isOpen(InputState inputState, ComboBox comboBox) {
        return inputState.openComboBox != null && inputState.openComboBox == comboBox;
    }

    static boolean contextMenu_isOpen(InputState inputState, ContextMenu contextMenu) {
        return inputState.openContextMenu != null && inputState.openContextMenu == contextMenu;
    }

    static void comboBox_open(InputState inputState, ComboBox comboBox) {
        // Close other Comboboxes
        if (inputState.openComboBox != null) {
            comboBox_close(inputState, inputState.openComboBox);
        }
        // Open this one
        inputState.openComboBox = comboBox;
        if (comboBox.comboBoxAction != null) comboBox.comboBoxAction.onOpen();
    }

    static void comboBox_close(InputState inputState, ComboBox comboBox) {
        if (comboBox_isOpen(inputState, comboBox)) {
            inputState.openComboBox = null;
            if (comboBox.comboBoxAction != null) comboBox.comboBoxAction.onClose();
        }
    }

    static boolean textField_isFocused(InputState inputState, TextField textField) {
        return inputState.focusedTextField != null && inputState.focusedTextField == textField;
    }

    static void textField_focus(InputState inputState, TextField textField) {
        // Unfocus other textfields
        if (inputState.focusedTextField != null && inputState.focusedTextField != textField) {
            textField_unFocus(inputState, inputState.focusedTextField);
        }
        // Focus this one
        inputState.focusedTextField = textField;
        if (textField.textFieldAction != null) textField.textFieldAction.onFocus();
    }

    static void textField_unFocus(InputState inputState, TextField textField) {
        if (textField_isFocused(inputState, textField)) {
            inputState.focusedTextField = null;
            inputState.focusedTextField_repeatedKey = KeyCode.NONE;
            inputState.focusedTextField_repeatedKeyTimer = 0;
            if (textField.textFieldAction != null)
                textField.textFieldAction.onUnFocus();
        }
    }

    static void list_setMultiSelect(List list, boolean multiSelect) {
        // Clear selecteditem/items after mode switch
        list.multiSelect = multiSelect;
        if (multiSelect) {
            list.selectedItem = null;
        } else {
            list.selectedItems.clear();
        }
    }


    static void knob_turnKnob(Knob knob, float newValue) {
        if (knob.endless) {
            if (newValue > 1) {
                newValue = newValue - 1f;
            } else if (newValue < 0) {
                newValue = 1f - Math.abs(newValue);
            }
        }
        float oldValue = knob.turned;
        knob.turned = Math.clamp(newValue, 0f, 1f);
        if (knob.knobAction != null) knob.knobAction.onTurned(knob.turned, (newValue - oldValue));
    }

    static boolean list_canDragIntoScreen(List list) {
        return list.dragEnabled && list.dragOutEnabled && list.listAction != null && list.listAction.canDragIntoScreen();
    }

    static boolean grid_canDragIntoScreen(Grid grid) {
        return grid.dragEnabled && grid.dragOutEnabled && grid.gridAction != null && grid.gridAction.canDragIntoScreen();
    }

    static boolean list_canDragIntoList(InputState inputState, List list) {
        List draggedList = inputState.draggedList;
        Grid draggedGrid = inputState.draggedGrid;

        if (draggedList != null && draggedList == list && draggedList.dragEnabled) return true; // Into itself

        if (list.dragInEnabled && !list.disabled && list.listAction != null) {
            if (draggedGrid != null) {
                return !inputState.draggedGrid.disabled && inputState.draggedGrid.dragOutEnabled &&
                        list.listAction.canDragFromGrid(inputState.draggedGrid);
            } else if (draggedList != null) {
                return !draggedList.disabled && draggedList.dragOutEnabled &&
                        list.listAction.canDragFromList(draggedList);
            }
        }
        return false;
    }

    static void tabBar_updateItemInfoAtMousePosition(InputState inputState, TabBar tabBar) {
        int x_bar = UICommons.component_getAbsoluteX(tabBar);
        int y_bar = UICommons.component_getAbsoluteY(tabBar);

        int tabXOffset = tabBar.tabOffset;
        for (int i = 0; i < tabBar.tabs.size(); i++) {
            Tab tab = tabBar.tabs.get(i);
            int tabWidth = tabBar.bigIconMode ? 2 : tab.width;
            if ((tabXOffset + tabWidth) > tabBar.width) {
                break;
            }

            int tabHeight = tabBar.bigIconMode ? (UIEngine.TILE_SIZE * 2) : UIEngine.TILE_SIZE;
            if (Tools.Calc.pointRectsCollide(inputState.mouse_ui.x, inputState.mouse_ui.y, x_bar + (tabXOffset * UIEngine.TILE_SIZE), y_bar, tabWidth * UIEngine.TILE_SIZE, tabHeight)) {
                inputState.itemInfo_tabBarTabIndex = i;
                inputState.itemInfo_tabBarValid = true;
                return;
            }
            tabXOffset = tabXOffset + tabWidth;
        }


        inputState.itemInfo_tabBarTabIndex = 0;
        inputState.itemInfo_tabBarValid = false;
        return;
    }

    static void list_updateItemInfoAtMousePosition(InputState inputState, List list) {
        if (list.items != null && list.listAction != null) {
            int itemFrom = MathUtils.round(list.scrolled * ((list.items.size()) - (list.height)));
            itemFrom = Math.clamp(itemFrom, 0, Integer.MAX_VALUE);
            int x_list = UICommons.component_getAbsoluteX(list);
            int y_list = UICommons.component_getAbsoluteY(list);
            // insert between other items
            for (int iy = 0; iy < list.height; iy++) {
                int itemIndex = itemFrom + iy;
                if (itemIndex < list.items.size()) {
                    int itemOffsetY = ((list.height - 1) - iy);
                    if (Tools.Calc.pointRectsCollide(inputState.mouse_ui.x, inputState.mouse_ui.y,
                            x_list, y_list + itemOffsetY * UIEngine.TILE_SIZE, UIEngine.TILE_SIZE * list.width, UIEngine.TILE_SIZE)) {
                        inputState.itemInfo_listIndex = itemIndex;
                        inputState.itemInfo_listValid = true;
                        return;
                    }
                }
            }
            // Insert at end
            if (Tools.Calc.pointRectsCollide(inputState.mouse_ui.x, inputState.mouse_ui.y, x_list, y_list, UIEngine.TILE_SIZE * list.width, UIEngine.TILE_SIZE * list.height)) {
                inputState.itemInfo_listIndex = list.items.size();
                inputState.itemInfo_listValid = true;
                return;
            }

        }

        inputState.itemInfo_listIndex = 0;
        inputState.itemInfo_listValid = false;
        return;
    }

    static void grid_updateItemInfoAtMousePosition(InputState inputState, Grid grid) {
        int tileSize = grid.doubleSized ? UIEngine.TILE_SIZE * 2 : UIEngine.TILE_SIZE;
        int x_grid = UICommons.component_getAbsoluteX(grid);
        int y_grid = UICommons.component_getAbsoluteY(grid);
        int inv_to_x = (inputState.mouse_ui.x - x_grid) / tileSize;
        int inv_to_y = (inputState.mouse_ui.y - y_grid) / tileSize;
        if (UICommons.grid_positionValid(grid, inv_to_x, inv_to_y)) {
            inputState.itemInfo_gridPos.x = inv_to_x;
            inputState.itemInfo_gridPos.y = inv_to_y;
            inputState.itemInfo_gridValid = true;
            return;
        }
        inputState.itemInfo_gridPos.set(0, 0);
        inputState.itemInfo_gridValid = false;
        return;
    }


    static boolean grid_canDragIntoGrid(InputState inputState, Grid grid) {
        List draggedList = inputState.draggedList;
        Grid draggedGrid = inputState.draggedGrid;

        if (draggedGrid != null && draggedGrid == grid && draggedGrid.dragEnabled) return true; // Into itself

        if (grid.dragInEnabled && !grid.disabled && grid.gridAction != null) {
            if (inputState.draggedGridItem != null) {
                return !inputState.draggedGrid.disabled && inputState.draggedGrid.dragOutEnabled &&
                        grid.gridAction.canDragFromGrid(inputState.draggedGrid);
            } else if (inputState.draggedListItem != null) {
                return !draggedList.disabled && draggedList.dragOutEnabled &&
                        grid.gridAction.canDragFromList(draggedList);
            }
        }
        return false;
    }


    static void mouseTextInput_selectCharacter(MouseTextInput mouseTextInput, char selectChar) {
        findCharLoop:
        for (int i = 0; i < mouseTextInput.charactersLC.length; i++) {
            if (mouseTextInput.charactersLC[i] == selectChar || mouseTextInput.charactersUC[i] == selectChar) {
                mouseTextInput_selectIndex(mouseTextInput, i);
                mouseTextInput.upperCase = mouseTextInput.charactersUC[i] == selectChar;
                break findCharLoop;
            }
        }
    }


    static void mouseTextInput_close(InputState inputState) {
        // mouseTextInput Keyboard
        inputState.openMouseTextInput = null;
        inputState.mTextInputMouse1Pressed = false;
        inputState.mTextInputMouse2Pressed = false;
        inputState.mTextInputMouse3Pressed = false;
        inputState.mTextInputGamePadLeft = false;
        inputState.mTextInputGamePadRight = false;
        inputState.mTextInputScrollTimer = 0;
        inputState.mTextInputScrollTime = 0;
        inputState.mTextInputScrollSpeed = 0;
        inputState.mTextInputTranslatedMouse1Down = false;
        inputState.mTextInputTranslatedMouse2Down = false;
        inputState.mTextInputTranslatedMouse3Down = false;
        inputState.mTextInputUnlock = false;
    }


    static void mouseTextInput_setCharacters(MouseTextInput mouseTextInput, char[] charactersLC, char[] charactersUC) {
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

    static void text_setLines(MediaManager mediaManager, Text text, String[] lines) {
        text.lines = Tools.Text.validStringArray(lines);
        UICommons.text_updateSize(mediaManager, text);
    }

    static void tooltip_setLines(ToolTip toolTip, String[] lines) {
        toolTip.lines = Tools.Text.validStringArray(lines);
    }

    static void text_updateSize(MediaManager mediaManager, Text text) {
        int width = 0;
        for (int i = 0; i < text.lines.length; i++) {
            int widthT = mediaManager.getCMediaFontTextWidth(text.font, text.lines[i]);
            if (widthT > width) width = widthT;
        }
        text.width = width / UIEngine.TILE_SIZE;
        text.height = text.lines.length;
    }

    static void image_setImage(MediaManager mediaManager, Image imageC, CMediaSprite image) {
        imageC.image = image;
        UICommons.image_updateSize(mediaManager, imageC);
    }

    static void image_updateSize(MediaManager mediaManager, Image imageC) {
        imageC.width = imageC.image != null ? mediaManager.getCMediaSpriteWidth(imageC.image) / UIEngine.TILE_SIZE : 0;
        imageC.height = imageC.image != null ? mediaManager.getCMediaSpriteHeight(imageC.image) / UIEngine.TILE_SIZE : 0;
    }

    static void mouseTextInput_selectIndex(MouseTextInput mouseTextInput, int index) {
        int maxCharacters = Math.min(mouseTextInput.charactersLC.length, mouseTextInput.charactersUC.length);
        mouseTextInput.selectedIndex = Math.clamp(index, 0, (maxCharacters - 1));
    }

    static void scrollBar_scroll(ScrollBar scrollBar, float scrolled) {
        scrollBar.scrolled = Math.clamp(scrolled, 0f, 1f);
        if (scrollBar.scrollBarAction != null) scrollBar.scrollBarAction.onScrolled(scrollBar.scrolled);
    }

    static void scrollBar_pressButton(ScrollBar scrollBar) {
        if (scrollBar.buttonPressed) return;
        scrollBar.buttonPressed = true;
        if (scrollBar.scrollBarAction != null) scrollBar.scrollBarAction.onPress(scrollBar.scrolled);
    }

    static void scrollBar_releaseButton(ScrollBar scrollBar) {
        if (!scrollBar.buttonPressed) return;
        scrollBar.buttonPressed = false;
        if (scrollBar.scrollBarAction != null) scrollBar.scrollBarAction.onRelease(scrollBar.scrolled);
    }

    static float scrollBar_calculateScrolled(ScrollBar scrollBar, int mouse_ui_x, int mouse_ui_y) {
        int relativePos;
        float maxPos;
        float buttonOffset;
        switch (scrollBar) {
            case ScrollBarHorizontal scrollBarHorizontal -> {
                relativePos = mouse_ui_x - UICommons.component_getAbsoluteX(scrollBarHorizontal);
                maxPos = (scrollBarHorizontal.width * UIEngine.TILE_SIZE) - 8;
                buttonOffset = (1 / (float) scrollBarHorizontal.width) / 2f;
            }
            case ScrollBarVertical scrollBarVertical -> {
                relativePos = mouse_ui_y - UICommons.component_getAbsoluteY(scrollBarVertical);
                maxPos = (scrollBarVertical.height * UIEngine.TILE_SIZE) - 8;
                buttonOffset = (1 / (float) scrollBarVertical.height) / 2f;
            }
            default -> throw new IllegalStateException("Unexpected value: " + scrollBar);
        }

        return (relativePos / maxPos) - buttonOffset;
    }

    static void list_scroll(List list, float scrolled) {
        list.scrolled = Math.clamp(scrolled, 0f, 1f);
        if (list.listAction != null) list.listAction.onScrolled(list.scrolled);
    }

    static void canvas_setAllPoints(Canvas canvas, float r, float g, float b, float a) {
        int width = canvas.width * UIEngine.TILE_SIZE;
        int height = canvas.height * UIEngine.TILE_SIZE;
        for (int ix = 0; ix <= width; ix++) {
            for (int iy = 0; iy <= height; iy++) {
                canvas_setPoint(canvas, ix, iy, r, g, b, a);
            }
        }
    }

    static boolean canvas_isInsideCanvas(Canvas canvas, int x, int y) {
        return x >= 0 && x < canvas.map.length && y >= 0 && y < canvas.map[0].length;
    }

    static void canvas_setPoint(Canvas canvas, int x, int y, float r, float g, float b, float a) {
        if (!canvas_isInsideCanvas(canvas, x, y)) return;
        canvas.map[x][y].set(
                r,
                g,
                b,
                a);
    }

    static Color canvas_getPoint(Canvas canvas, int x, int y) {
        if (x < 0 || x > (canvas.map.length - 1) || y < 0 || y > (canvas.map.length - 1)) return null;
        return canvas.map[x][y];
    }

    static void appViewPort_resizeCameraTextureAndFrameBuffer(AppViewPort appViewPort) {
        // Clean Up
        if (appViewPort.camera != null) appViewPort.camera = null;
        if (appViewPort.textureRegion != null) appViewPort.textureRegion.getTexture().dispose();
        if (appViewPort.frameBuffer != null) appViewPort.frameBuffer.dispose();

        int viewportWidth = appViewPort.width * UIEngine.TILE_SIZE;
        int viewportHeight = appViewPort.height * UIEngine.TILE_SIZE;
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

    static int viewport_determineUpscaleFactor(VIEWPORT_MODE viewPortMode, int internalResolutionWidth, int internalResolutionHeight) {
        switch (viewPortMode) {
            case PIXEL_PERFECT -> {
                return 1;
            }
            case FIT, STRETCH -> {
                int upSampling = 1;
                int testWidth = Gdx.graphics.getDisplayMode().width;
                int testHeight = Gdx.graphics.getDisplayMode().height;
                while ((internalResolutionWidth * upSampling) < testWidth && (internalResolutionHeight * upSampling) < testHeight) {
                    upSampling++;
                }
                return upSampling;
            }

            default -> throw new IllegalStateException("Unexpected value: " + viewPortMode);
        }
    }

    static void viewport_changeViewPortMode(InputState inputState, VIEWPORT_MODE viewPortMode) {
        if (viewPortMode == null || viewPortMode == inputState.viewportMode) return;
        inputState.upscaleFactor_screen = UICommons.viewport_determineUpscaleFactor(viewPortMode, inputState.resolutionWidth, inputState.resolutionHeight);
        inputState.textureFilter_screen = UICommons.viewport_determineUpscaleTextureFilter(viewPortMode);
        // frameBuffer_upScale
        inputState.frameBuffer_screen.dispose();
        inputState.frameBuffer_screen = new NestedFrameBuffer(Pixmap.Format.RGBA8888, inputState.resolutionWidth * inputState.upscaleFactor_screen, inputState.resolutionHeight * inputState.upscaleFactor_screen, false);
        inputState.frameBuffer_screen.getColorBufferTexture().setFilter(inputState.textureFilter_screen, inputState.textureFilter_screen);
        // texture_upScale
        inputState.texture_screen.getTexture().dispose();
        inputState.texture_screen = new TextureRegion(inputState.frameBuffer_screen.getColorBufferTexture());
        inputState.texture_screen.flip(false, true);
        // viewport_screen
        inputState.viewport_screen = UICommons.viewport_createViewport(viewPortMode, inputState.camera_ui, inputState.resolutionWidth, inputState.resolutionHeight);
        inputState.viewport_screen.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        // viewportMode
        inputState.viewportMode = viewPortMode;

    }


    static Viewport viewport_createViewport(VIEWPORT_MODE viewportMode, OrthographicCamera camera_screen, int internalResolutionWidth, int internalResolutionHeight) {
        return switch (viewportMode) {
            case FIT -> new FitViewport(internalResolutionWidth, internalResolutionHeight, camera_screen);
            case PIXEL_PERFECT ->
                    new PixelPerfectViewport(internalResolutionWidth, internalResolutionHeight, camera_screen, 1);
            case STRETCH -> new StretchViewport(internalResolutionWidth, internalResolutionHeight, camera_screen);
        };
    }

    static Texture.TextureFilter viewport_determineUpscaleTextureFilter(VIEWPORT_MODE viewportMode) {
        return switch (viewportMode) {
            case PIXEL_PERFECT -> Texture.TextureFilter.Nearest;
            case FIT, STRETCH -> Texture.TextureFilter.Linear;
        };
    }

    static void camera_setPosition(OrthographicCamera camera, float x, float y) {
        camera.position.set(x, y, 0f);
        camera.update();
    }

    static void camera_setZoom(OrthographicCamera camera, float zoom) {
        camera.zoom = Math.clamp(zoom, 0f, Float.MAX_VALUE);
        camera.update();
    }

}
