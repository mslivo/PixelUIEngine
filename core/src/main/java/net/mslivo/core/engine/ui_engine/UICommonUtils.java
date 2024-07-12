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
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.constants.BUTTON_MODE;
import net.mslivo.core.engine.ui_engine.constants.KeyCode;
import net.mslivo.core.engine.ui_engine.constants.VIEWPORT_MODE;
import net.mslivo.core.engine.ui_engine.rendering.NestedFrameBuffer;
import net.mslivo.core.engine.ui_engine.rendering.PixelPerfectViewport;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.Window;
import net.mslivo.core.engine.ui_engine.ui.components.Component;
import net.mslivo.core.engine.ui_engine.ui.components.button.Button;
import net.mslivo.core.engine.ui_engine.ui.components.button.ImageButton;
import net.mslivo.core.engine.ui_engine.ui.components.button.TextButton;
import net.mslivo.core.engine.ui_engine.ui.components.canvas.Canvas;
import net.mslivo.core.engine.ui_engine.ui.components.canvas.CanvasImage;
import net.mslivo.core.engine.ui_engine.ui.components.checkbox.Checkbox;
import net.mslivo.core.engine.ui_engine.ui.components.combobox.Combobox;
import net.mslivo.core.engine.ui_engine.ui.components.combobox.ComboboxItem;
import net.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import net.mslivo.core.engine.ui_engine.ui.components.image.Image;
import net.mslivo.core.engine.ui_engine.ui.components.knob.Knob;
import net.mslivo.core.engine.ui_engine.ui.components.list.List;
import net.mslivo.core.engine.ui_engine.ui.components.progressbar.Progressbar;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.Scrollbar;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollbarHorizontal;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollbarVertical;
import net.mslivo.core.engine.ui_engine.ui.components.tabbar.Tab;
import net.mslivo.core.engine.ui_engine.ui.components.tabbar.Tabbar;
import net.mslivo.core.engine.ui_engine.ui.components.text.Text;
import net.mslivo.core.engine.ui_engine.ui.components.textfield.Textfield;
import net.mslivo.core.engine.ui_engine.ui.components.viewport.AppViewport;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.Contextmenu;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextmenuItem;
import net.mslivo.core.engine.ui_engine.ui.hotkeys.HotKey;
import net.mslivo.core.engine.ui_engine.ui.mousetextinput.MouseTextInput;
import net.mslivo.core.engine.ui_engine.ui.notification.Notification;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;
import net.mslivo.core.engine.ui_engine.ui.tooltip.TooltipImageSegment;
import net.mslivo.core.engine.ui_engine.ui.tooltip.TooltipSegment;
import net.mslivo.core.engine.ui_engine.ui.tooltip.TooltipTextSegment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

final class UICommonUtils {
    public static final String WND_CLOSE_BUTTON = "wnd_close_btn";
    private static IntSet textFieldControlKeys = new IntSet();
    private static IntSet textFieldRepeatedControlKeys = new IntSet();
    private static ArrayList<Component> windowComponentsVisibleOrder = new ArrayList<>();
    private static HashSet<Component> windowComponentsVisibleOrderSet = new HashSet<>();

    static {
        textFieldControlKeys.addAll(KeyCode.Key.LEFT, KeyCode.Key.RIGHT, KeyCode.Key.BACKSPACE, KeyCode.Key.FORWARD_DEL, Input.Keys.HOME, Input.Keys.END, Input.Keys.ENTER);
        textFieldRepeatedControlKeys.addAll(KeyCode.Key.LEFT, KeyCode.Key.RIGHT, KeyCode.Key.BACKSPACE, KeyCode.Key.FORWARD_DEL);
    }

    static void emulatedMouse_setPosition(UIEngineState uiEngineState, int x, int y) {
        if (!uiEngineState.currentControlMode.emulated) return;
        // not possibe with hardware mouse - would be resetted instantly
        uiEngineState.mouse_emulated.x = x;
        uiEngineState.mouse_emulated.y = y;
    }

    static void emulatedMouse_setPositionComponent(UIEngineState uiEngineState, Component component) {
        if (component == null) return;
        if (component.addedToWindow == null && !component.addedToScreen) return;
        int x = component_getAbsoluteX(component) + (uiEngineState.sizeSize.TL(component.width) / 2);
        int y = component_getAbsoluteY(component) + (uiEngineState.sizeSize.TL(component.height) / 2);
        emulatedMouse_setPosition(uiEngineState, x, y);
    }

    private static boolean emulatedMouse_isInteractAbleComponent(Component component) {
        if (!(component instanceof Image || component instanceof Text)) {
            if (component.visible && !component.disabled && !component_isHiddenByTab(component)) return true;
        }
        return false;
    }

    static void emulatedMouse_setPositionNextComponent(UIEngineState uiEngineState, boolean backwards) {
        Window activeWindow = window_findTopInteractableWindow(uiEngineState);
        if (activeWindow != null && activeWindow.folded) {
            emulatedMouse_setPosition(uiEngineState,
                    activeWindow.x + (uiEngineState.sizeSize.TL(activeWindow.width) / 2),
                    activeWindow.y + (uiEngineState.sizeSize.TL(activeWindow.height) - 4)
            );
            return;
        }
        windowComponentsVisibleOrder.clear();
        windowComponentsVisibleOrderSet.clear();
        int fromX = activeWindow != null ? activeWindow.x : 0;
        int fromY = activeWindow != null ? activeWindow.y : 0;
        int toX = activeWindow != null ? fromX + uiEngineState.sizeSize.TL(activeWindow.width) : uiEngineState.resolutionWidth;
        int toY = activeWindow != null ? fromY + uiEngineState.sizeSize.TL(activeWindow.height) : uiEngineState.resolutionHeight;

        int nearestIndex = -1;
        float nearestDistance = Float.MAX_VALUE;

        for (int iy = toY; iy >= fromY; iy -= uiEngineState.sizeSize.TS) {
            for (int ix = fromX; ix <= toX; ix += uiEngineState.sizeSize.TS) {
                Object object = UICommonUtils.component_getUIObjectAtPosition(uiEngineState, ix, iy);
                if (!windowComponentsVisibleOrderSet.contains(object) && object instanceof Component component && emulatedMouse_isInteractAbleComponent(component)) {
                    windowComponentsVisibleOrder.add(component);
                    windowComponentsVisibleOrderSet.add(component);
                    float distance = Tools.Calc.distanceFast(component_getAbsoluteX(component) + (uiEngineState.sizeSize.TL(component.width) / 2),
                            component_getAbsoluteY(component) + (uiEngineState.sizeSize.TL(component.height) / 2), uiEngineState.mouse_emulated.x, uiEngineState.mouse_emulated.y);
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

            emulatedMouse_setPositionComponent(uiEngineState, windowComponentsVisibleOrder.get(nearestIndex));
        }

        return;
    }


    static Window window_findTopInteractableWindow(UIEngineState uiEngineState) {
        if (uiEngineState.windows.isEmpty()) return null;
        for (int i = (uiEngineState.windows.size() - 1); i >= 0; i--) {
            Window window = uiEngineState.windows.get(i);
            if (window.visible) return window;
        }
        return null;
    }


    static boolean window_isModalOpen(UIEngineState uiEngineState) {
        return uiEngineState.modalWindow != null;
    }

    static void window_fold(Window window) {
        window.folded = true;
        if (window.windowAction != null) window.windowAction.onFold();
    }

    static void window_unFold(Window window) {
        window.folded = false;
        if (window.windowAction != null) window.windowAction.onUnfold();
    }

    static void window_receiveMessage(Window window, int type, Object... parameters) {
        if (window == null) return;
        if (window.windowAction != null) {
            window.windowAction.onMessageReceived(type, parameters);
        }
    }

    static void window_bringToFront(UIEngineState uiEngineState, Window window) {
        if (uiEngineState.windows.size() == 1) return;
        if (window.alwaysOnTop) {
            if (uiEngineState.windows.getLast() != window) {
                uiEngineState.windows.remove(window);
                uiEngineState.windows.add(window);
            }
        } else {
            int index = uiEngineState.windows.size() - 1;
            searchIndex:
            while (index > 0) {
                if (!uiEngineState.windows.get(index).alwaysOnTop) {
                    break searchIndex;
                }
                index = index - 1;
            }
            uiEngineState.windows.remove(window);
            uiEngineState.windows.add(index, window);
        }
    }


    static void window_setPosition(UIEngineState uiEngineState, Window window, int x, int y) {
        window.x = x;
        window.y = y;
        if (window.enforceScreenBounds) window_enforceScreenBounds(uiEngineState, window);
    }


    static void window_enforceScreenBounds(UIEngineState uiEngineState, Window window) {
        int wndWidth = uiEngineState.sizeSize.TL(window.width);
        window.x = Math.clamp(window.x, 0, uiEngineState.resolutionWidth - wndWidth);
        if (window.folded) {
            window.y = Math.clamp(window.y, -(uiEngineState.sizeSize.TL(window.height - 1)), uiEngineState.resolutionHeight - uiEngineState.sizeSize.TL(window.height));
        } else {
            window.y = Math.clamp(window.y, 0, uiEngineState.resolutionHeight - uiEngineState.sizeSize.TL(window.height));
        }
    }

    static void window_addToScreenAsModal(UIEngineState uiEngineState, Window window) {
        if (uiEngineState.modalWindow == null) {
            window.alwaysOnTop = true;
            window.visible = true;
            window.folded = false;
            window.enforceScreenBounds = true;
            uiEngineState.modalWindow = window;
            UICommonUtils.window_center(uiEngineState, window);
            UICommonUtils.window_addToScreen(uiEngineState, window);
        } else {
            uiEngineState.modalWindowQueue.add(window);
        }
    }

    static void window_center(UIEngineState uiEngineState, Window window) {
        int centerX = (uiEngineState.resolutionWidthHalf) - (uiEngineState.sizeSize.TL(window.width) / 2);
        int centerY = (uiEngineState.resolutionHeightHalf) - ((window.folded ? uiEngineState.sizeSize.TS : uiEngineState.sizeSize.TL(window.height)) / 2);
        window_setPosition(uiEngineState, window, centerX, centerY);
    }

    static void window_addToScreen(UIEngineState uiEngineState, Window window) {
        if (window.addedToScreen) return;
        window.addedToScreen = true;
        uiEngineState.windows.add(window);
        if (window.windowAction != null) window.windowAction.onAdd();
        window_enforceScreenBounds(uiEngineState, window);
    }

    static boolean window_close(UIEngineState uiEngineState, Window window) {
        for (int i = 0; i < window.components.size(); i++) {
            if (window.components.get(i).name.equals(WND_CLOSE_BUTTON) && window.components.get(i) instanceof Button closeButton) {
                if (closeButton.buttonAction != null) {
                    UICommonUtils.button_press(closeButton);
                    UICommonUtils.button_release(closeButton);
                    return true;
                }
            }
        }
        return false;
    }

    static void window_removeFromScreen(UIEngineState uiEngineState, Window window) {
        if (!window.addedToScreen) return;

        // Remove References
        if (uiEngineState.lastUIMouseHover == window) uiEngineState.lastUIMouseHover = null;
        if (UICommonUtils.window_isModalOpen(uiEngineState) && uiEngineState.modalWindow == window)
            uiEngineState.modalWindow = null;
        for (int i = 0; i < window.components.size(); i++)
            if (window.components.get(i) instanceof AppViewport appViewPort)
                uiEngineState.appViewPorts.remove(appViewPort);
        window_resetReferences(uiEngineState, window);

        // Remove
        window.addedToScreen = false;
        uiEngineState.windows.remove(window);
        if (window.windowAction != null) window.windowAction.onRemove();

        // Add Next Modal from Queue queue
        if (uiEngineState.modalWindowQueue.size() > 0)
            window_addToScreenAsModal(uiEngineState, uiEngineState.modalWindowQueue.poll());
    }

    static void component_setSize(UIEngineState uiEngineState, Component component, int width, int height) {
        component.width = Math.max(width, 1);
        component.height = Math.max(height, 1);

        if (component instanceof AppViewport appViewPort) {
            appViewPort_resizeCameraTextureAndFrameBuffer(uiEngineState, appViewPort);
        }
        if (component instanceof Canvas canvas) {
            canvas_resizeMap(uiEngineState, canvas);
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

    static Object component_getUIObjectAtPosition(UIEngineState uiEngineState, int x, int y) {
        // Notification Collision
        for (int i = 0; i < uiEngineState.notifications.size(); i++) {
            Notification notification = uiEngineState.notifications.get(i);
            if(!notification.clickAble) continue;
            if (Tools.Calc.pointRectsCollide(x, y,
                    0, uiEngineState.resolutionHeight - uiEngineState.sizeSize.TL(i + 1),
                    uiEngineState.resolutionWidth, uiEngineState.sizeSize.TS)) {
                return notification;
            }
        }

        // Context Menu Item collision
        if (uiEngineState.openContextMenu != null) {
            for (int i = 0; i < uiEngineState.openContextMenu.items.size(); i++) {
                if (Tools.Calc.pointRectsCollide(x, y, uiEngineState.openContextMenu.x, uiEngineState.openContextMenu.y - (uiEngineState.sizeSize.TS) - uiEngineState.sizeSize.TL(i), uiEngineState.sizeSize.TL(uiEngineState.displayedContextMenuWidth), uiEngineState.sizeSize.TS)) {
                    return uiEngineState.openContextMenu.items.get(i);
                }
            }
        }

        // Combobox Open Menu collision
        if (uiEngineState.openComboBox != null) {
            if (Tools.Calc.pointRectsCollide(x, y, UICommonUtils.component_getAbsoluteX(uiEngineState.openComboBox), UICommonUtils.component_getAbsoluteY(uiEngineState.openComboBox) - (uiEngineState.sizeSize.TL(uiEngineState.openComboBox.comboBoxItems.size())), uiEngineState.sizeSize.TL(uiEngineState.openComboBox.width), uiEngineState.sizeSize.TL(uiEngineState.openComboBox.comboBoxItems.size()))) {
                return uiEngineState.openComboBox;
            }
        }

        // Window / WindowComponent collision
        windowLoop:
        for (int i = uiEngineState.windows.size() - 1; i >= 0; i--) { // use for(i) to avoid iterator creation
            Window window = uiEngineState.windows.get(i);
            if (!window.visible) continue windowLoop;

            int wndX = window.x;
            int wndY = window.y + (window.folded ? uiEngineState.sizeSize.TL(window.height - 1) : 0);
            int wndWidth = uiEngineState.sizeSize.TL(window.width);
            int wndHeight = window.folded ? uiEngineState.sizeSize.TS : uiEngineState.sizeSize.TL(window.height);

            boolean collidesWithWindow = Tools.Calc.pointRectsCollide(x, y, wndX, wndY, wndWidth, wndHeight);
            if (collidesWithWindow) {
                for (int ic = window.components.size() - 1; ic >= 0; ic--) {
                    Component component = window.components.get(ic);
                    if (component_isComponentAtPosition(uiEngineState, x, y, component)) {
                        return component;
                    }
                }
                return window;
            }
        }

        // Screen component collision
        for (int i = 0; i < uiEngineState.screenComponents.size(); i++) {
            Component screenComponent = uiEngineState.screenComponents.get(i);
            if (component_isComponentAtPosition(uiEngineState, x, y, screenComponent)) return screenComponent;
        }
        return null;
    }

    static boolean component_isComponentAtPosition(UIEngineState uiEngineState, int x, int y, Component component) {
        if (!component.visible) return false;
        if (component.disabled) return false;
        if (UICommonUtils.component_isHiddenByTab(component)) return false;

        if (Tools.Calc.pointRectsCollide(x, y, UICommonUtils.component_getAbsoluteX(component), UICommonUtils.component_getAbsoluteY(component), uiEngineState.sizeSize.TL(component.width), uiEngineState.sizeSize.TL(component.height))) {
            return true;
        }
        return false;
    }

    static boolean component_isHiddenByTab(Component component) {
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

    static void hotkey_press(HotKey hotKey) {
        hotKey.pressed = true;
        if (hotKey.hotKeyAction != null) hotKey.hotKeyAction.onPress();
    }

    static void hotkey_release(HotKey hotKey) {
        hotKey.pressed = false;
        if (hotKey.hotKeyAction != null) hotKey.hotKeyAction.onRelease();
    }

    static void checkbox_check(Checkbox checkBox) {
        if (checkBox.checked) return;
        checkBox.checked = true;
        if (checkBox.checkBoxAction != null) checkBox.checkBoxAction.onCheck(true);
    }

    static void checkbox_unCheck(Checkbox checkBox) {
        if (!checkBox.checked) return;
        checkBox.checked = false;
        if (checkBox.checkBoxAction != null) checkBox.checkBoxAction.onCheck(false);
    }

    static void window_resetReferences(UIEngineState uiEngineState, Window window) {
        if (uiEngineState.draggedWindow == window) {
            uiEngineState.draggedWindow = null;
            uiEngineState.draggedWindow_offset.set(0, 0);
        }
    }

    static void component_resetReferences(UIEngineState uiEngineState, Component component) {
        if (uiEngineState.pressedButton == component) uiEngineState.pressedButton = null;
        if (uiEngineState.pressedScrollBarVertical == component) uiEngineState.pressedScrollBarVertical = null;
        if (uiEngineState.pressedScrollBarHorizontal == component) uiEngineState.pressedScrollBarHorizontal = null;
        if (uiEngineState.pressedKnob == component) uiEngineState.pressedKnob = null;
        if (uiEngineState.pressedCanvas == component) uiEngineState.pressedCanvas = null;
        if (uiEngineState.pressedAppViewPort == component) uiEngineState.pressedAppViewPort = null;
        if (uiEngineState.pressedTextField == component) {
            uiEngineState.pressedTextField = null;
            uiEngineState.pressedTextFieldMouseX = 0;
        }
        if (uiEngineState.focusedTextField == component) {
            uiEngineState.focusedTextField = null;
            uiEngineState.focusedTextField_repeatedKey = KeyCode.NONE;
            uiEngineState.focusedTextField_repeatedKeyTimer = 0;
        }
        if (uiEngineState.draggedGrid == component) {
            uiEngineState.draggedGrid = null;
            uiEngineState.draggedGridFrom.set(0, 0);
            uiEngineState.draggedGridOffset.set(0, 0);
            uiEngineState.draggedGridItem = null;
            uiEngineState.pressedGrid = null;
            uiEngineState.pressedGridItem = null;
        }
        if (uiEngineState.draggedList == component) {
            uiEngineState.draggedList = null;
            uiEngineState.draggedListFromIndex = 0;
            uiEngineState.draggedListOffsetX.set(0, 0);
            uiEngineState.draggedListItem = null;
            uiEngineState.pressedList = null;
            uiEngineState.pressedListItem = null;
        }
        if (uiEngineState.openComboBox == component) {
            uiEngineState.openComboBox = null;
            uiEngineState.pressedComboBoxItem = null;
        }
        if (uiEngineState.pressedCheckBox == component) {
            uiEngineState.pressedCheckBox = null;
        }
    }

    static Object getDraggedUIReference(UIEngineState uiEngineState) {
        if (uiEngineState.draggedWindow != null) return uiEngineState.draggedWindow;
        if (uiEngineState.draggedGrid != null) return uiEngineState.draggedGrid;
        if (uiEngineState.draggedList != null) return uiEngineState.draggedList;
        return null;
    }

    static Object getPressedUIReference(UIEngineState uiEngineState) {
        if (uiEngineState.pressedButton != null) return uiEngineState.pressedButton;
        if (uiEngineState.pressedScrollBarHorizontal != null) return uiEngineState.pressedScrollBarHorizontal;
        if (uiEngineState.pressedScrollBarVertical != null) return uiEngineState.pressedScrollBarVertical;
        if (uiEngineState.pressedKnob != null) return uiEngineState.pressedKnob;
        if (uiEngineState.pressedCanvas != null) return uiEngineState.pressedCanvas;
        if (uiEngineState.pressedTextField != null) return uiEngineState.pressedTextField;
        if (uiEngineState.pressedAppViewPort != null) return uiEngineState.pressedAppViewPort;
        if (uiEngineState.pressedGrid != null) return uiEngineState.pressedGrid;
        if (uiEngineState.pressedList != null) return uiEngineState.pressedList;
        if (uiEngineState.pressedContextMenuItem != null) return uiEngineState.pressedContextMenuItem;
        if (uiEngineState.pressedComboBoxItem != null) return uiEngineState.pressedComboBoxItem;
        if (uiEngineState.pressedCheckBox != null) return uiEngineState.pressedCheckBox;
        return null;
    }

    static void setMouseInteractedUIObject(UIEngineState uiEngineState, Object object) {
        uiEngineState.mouseInteractedUIObjectFrame = object;
    }

    static void setKeyboardInteractedUIObject(UIEngineState uiEngineState, Object object) {
        uiEngineState.keyboardInteractedUIObjectFrame = object;
    }

    static void notification_addToScreen(UIEngineState uiEngineState, Notification notification, int notificationsMax) {
        if (notification.addedToScreen) return;
        notification.addedToScreen = true;
        uiEngineState.notifications.add(notification);
        // Remove first if too many
        if (uiEngineState.notifications.size() > notificationsMax)
            notification_removeFromScreen(uiEngineState, uiEngineState.notifications.getFirst());
    }

    static void notification_removeFromScreen(UIEngineState uiEngineState, Notification notification) {
        if (!notification.addedToScreen) return;
        notification.addedToScreen = false;
        uiEngineState.notifications.remove(notification);
    }

    static boolean contextMenu_openAtMousePosition(UIEngineState uiEngineState, MediaManager mediaManager, Contextmenu contextMenu) {
        boolean success = contextMenu_open(uiEngineState, mediaManager, contextMenu, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y);
        if (success && (uiEngineState.currentControlMode.emulated)) {
            // emulated mode: move mouse onto the opened menu
            uiEngineState.mouse_emulated.x += uiEngineState.sizeSize.TS_HALF;
            uiEngineState.mouse_emulated.y -= uiEngineState.sizeSize.TS_HALF;
        }
        return success;
    }

    static boolean contextMenu_open(UIEngineState uiEngineState, MediaManager mediaManager, Contextmenu contextMenu, int x, int y) {
        if (contextMenu.items.size() == 0) return false;
        // Close open ContextMenus
        if (uiEngineState.openContextMenu != null) {
            contextMenu_close(uiEngineState, uiEngineState.openContextMenu);
        }
        // Open this one
        contextMenu.x = x;
        contextMenu.y = y;
        int textwidth = 0;
        for (int i = 0; i < contextMenu.items.size(); i++) {
            ContextmenuItem contextMenuItem = contextMenu.items.get(i);
            int w = mediaManager.getCMediaFontTextWidth(contextMenuItem.font, contextMenuItem.text);
            if (contextMenuItem.icon != null) w = w + uiEngineState.sizeSize.TS;
            if (w > textwidth) textwidth = w;
        }
        uiEngineState.displayedContextMenuWidth = (textwidth + uiEngineState.sizeSize.TS) / uiEngineState.sizeSize.TS;
        uiEngineState.openContextMenu = contextMenu;
        if (uiEngineState.openContextMenu.contextMenuAction != null)
            uiEngineState.openContextMenu.contextMenuAction.onOpen();
        return true;
    }

    static void contextMenu_close(UIEngineState uiEngineState, Contextmenu contextMenu) {
        if (contextMenu_isOpen(uiEngineState, contextMenu)) {
            uiEngineState.openContextMenu = null;
            uiEngineState.displayedContextMenuWidth = 0;
            uiEngineState.pressedContextMenuItem = null;
            if (contextMenu.contextMenuAction != null) contextMenu.contextMenuAction.onClose();
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
        return tabBar.tabs.get(Math.clamp(tabBar.selectedTab, 0, tabBar.tabs.size() - 1));
    }

    static void tabBar_selectTab(Tabbar tabBar, Tab tab) {
        if (tab.addedToTabBar != tabBar) return;
        for (int i = 0; i < tabBar.tabs.size(); i++) {
            if (tabBar.tabs.get(i) == tab) {
                UICommonUtils.tabBar_selectTab(tabBar, i);
                return;
            }
        }
    }

    static void tabBar_selectTab(Tabbar tabBar, int index) {
        tabBar.selectedTab = Math.clamp(index, 0, tabBar.tabs.size() - 1);
        Tab tab = tabBar.tabs.get(tabBar.selectedTab);
        if (tab.tabAction != null) tab.tabAction.onSelect();
        if (tabBar.tabBarAction != null)
            tabBar.tabBarAction.onChangeTab(index, tab);
    }


    static void button_press(Button button) {
        if (button.pressed || button.mode != BUTTON_MODE.DEFAULT) return;
        button.pressed = true;
        if (button.buttonAction != null) button.buttonAction.onPress();
    }

    static void button_release(Button button) {
        if (!button.pressed || button.mode != BUTTON_MODE.DEFAULT) return;
        button.pressed = false;
        if (button.buttonAction != null) button.buttonAction.onRelease();
    }

    static void button_toggle(Button button) {
        button_toggle(button, !button.pressed);
    }

    static void progressbar_setProgress(Progressbar progressBar, float progress) {
        progressBar.progress = Math.clamp(progress, 0f, 1f);
    }

    static void button_toggle(Button button, boolean pressed) {
        if (button.toggleDisabled || button.pressed == pressed || button.mode != BUTTON_MODE.TOGGLE) return;
        button.pressed = pressed;
        if (button.buttonAction != null) button.buttonAction.onToggle(button.pressed);
    }

    static void button_centerContent(UIEngineState uiEngineState, MediaManager mediaManager, Button button) {
        if (button == null) return;
        if (button instanceof ImageButton imageButton) {
            if (imageButton.image == null) return;
            imageButton.contentOffset_x = MathUtils.round((uiEngineState.sizeSize.TL(imageButton.width) - mediaManager.getCMediaSpriteWidth(imageButton.image)) / 2f);
            imageButton.contentOffset_y = MathUtils.round((uiEngineState.sizeSize.TL(imageButton.height) - mediaManager.getCMediaSpriteHeight(imageButton.image)) / 2f);

        } else if (button instanceof TextButton textButton) {
            if (textButton.text == null) return;
            int iconWidth = textButton.icon != null ? uiEngineState.sizeSize.TS : 0;
            int contentWidth = mediaManager.getCMediaFontTextWidth(textButton.font, textButton.text) + 1 + iconWidth;
            int contentHeight = mediaManager.getCMediaFontTextHeight(textButton.font, textButton.text);
            textButton.contentOffset_x = MathUtils.round((uiEngineState.sizeSize.TL(textButton.width) - contentWidth) / 2f);
            textButton.contentOffset_y = MathUtils.round(((uiEngineState.sizeSize.TL(textButton.height) - contentHeight)) / 2f) - 2;
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

    static void textField_setMarkerPosition(UIEngineState uiEngineState, MediaManager mediaManager, Textfield textField, int position) {
        textField.markerPosition = Math.clamp(position, 0, textField.content.length());
        if (textField.markerPosition < textField.offset) {
            while (textField.markerPosition < textField.offset) {
                textField.offset--;
            }
        } else {
            String subContent = textField.content.substring(textField.offset, textField.markerPosition);
            int width = uiEngineState.sizeSize.TL(textField.width) - 4;
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

    static void textField_executeControlKey(UIEngineState uiEngineState, MediaManager mediaManager, Textfield textField, int keyCode) {
        switch (keyCode) {
            case Input.Keys.LEFT -> textField_setMarkerPosition(uiEngineState,mediaManager, textField, textField.markerPosition - 1);
            case Input.Keys.RIGHT -> textField_setMarkerPosition(uiEngineState,mediaManager, textField, textField.markerPosition + 1);
            case Input.Keys.BACKSPACE -> {
                if (!textField.content.isEmpty() && textField.markerPosition > 0) {
                    String newContent = textField.content.substring(0, textField.markerPosition - 1) + textField.content.substring(textField.markerPosition);
                    UICommonUtils.textField_setMarkerPosition(uiEngineState,mediaManager, textField, textField.markerPosition - 1);
                    UICommonUtils.textField_setContent(textField, newContent);
                }
            }
            case Input.Keys.FORWARD_DEL -> {
                if (!textField.content.isEmpty() && textField.markerPosition < textField.content.length()) {
                    String newContent = textField.content.substring(0, textField.markerPosition) + textField.content.substring(textField.markerPosition + 1);
                    UICommonUtils.textField_setContent(textField, newContent);
                }
            }
            case Input.Keys.HOME -> UICommonUtils.textField_setMarkerPosition(uiEngineState,mediaManager, textField, 0);
            case Input.Keys.END ->
                    UICommonUtils.textField_setMarkerPosition(uiEngineState,mediaManager, textField, textField.content.length());
            case Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> {
                UICommonUtils.textField_unFocus(uiEngineState, textField); // Unfocus
                if (textField.textFieldAction != null)
                    textField.textFieldAction.onEnter(textField.content, textField.contentValid);
            }
            default -> {
            }
        }
    }

    static void textField_typeCharacter(UIEngineState uiEngineState, MediaManager mediaManager, Textfield textField, char character) {
        if (textField.allowedCharacters == null || textField.allowedCharacters.contains(character)) {
            String newContent = textField.content.substring(0, textField.markerPosition) + character + textField.content.substring(textField.markerPosition);
            UICommonUtils.textField_setContent(textField, newContent);
            UICommonUtils.textField_setMarkerPosition(uiEngineState,mediaManager, textField, textField.markerPosition + 1);
            if (textField.textFieldAction != null)
                textField.textFieldAction.onTyped(character);
        }
    }

    static void textField_setContent(Textfield textField, String content) {
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

    static void component_addToWindow(Component component, UIEngineState uiEngineState, Window window) {
        if (component.addedToWindow != null) return;
        if (component.addedToScreen) return;
        if (component instanceof AppViewport appViewPort) uiEngineState.appViewPorts.add(appViewPort);
        component.addedToWindow = window;
        window.components.add(component);
    }

    static void component_addToScreen(Component component, UIEngineState uiEngineState) {
        if (component.addedToWindow != null) return;
        if (component.addedToScreen) return;
        if (component instanceof AppViewport appViewPort) uiEngineState.appViewPorts.add(appViewPort);
        component.addedToScreen = true;
        uiEngineState.screenComponents.add(component);
    }

    static void component_removeFromScreen(Component component, UIEngineState uiEngineState) {
        if (component.addedToWindow != null) return;
        if (!component.addedToScreen) return;

        // Remove References
        if (uiEngineState.lastUIMouseHover == component) uiEngineState.lastUIMouseHover = null;
        if (component.addedToTab != null) tab_removeComponent(component.addedToTab, component);
        if (component instanceof AppViewport appViewPort) uiEngineState.appViewPorts.remove(appViewPort);
        component_resetReferences(uiEngineState, component);

        // Remove
        component.addedToScreen = true;
        uiEngineState.screenComponents.remove(component);
    }

    static void component_removeFromWindow(Component component, Window window, UIEngineState uiEngineState) {
        if (component.addedToWindow != window) return;
        if (component.addedToScreen) return;

        // Remove References
        if (uiEngineState.lastUIMouseHover == component) uiEngineState.lastUIMouseHover = null;
        if (component.addedToTab != null) tab_removeComponent(component.addedToTab, component);
        if (component instanceof AppViewport appViewPort) uiEngineState.appViewPorts.remove(appViewPort);
        component_resetReferences(uiEngineState, component);

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

    static void tabBar_addTab(Tabbar tabBar, Tab tab) {
        if (tab.addedToTabBar != null) return;
        tab.addedToTabBar = tabBar;
        tabBar.tabs.add(tab);
    }

    static void tabBar_addTab(Tabbar tabBar, Tab tab, int index) {
        if (tab.addedToTabBar != null) return;
        tab.addedToTabBar = tabBar;
        tabBar.tabs.add(index, tab);
    }

    static void tabBar_removeTab(Tabbar tabBar, Tab tab) {
        if (tab.addedToTabBar != tabBar) return;
        tab.addedToTabBar = null;
        tabBar.tabs.remove(tab);
    }

    static void contextMenu_addItem(Contextmenu contextMenu, ContextmenuItem contextMenuItem) {
        if (contextMenuItem.addedToContextMenu != null) return;
        contextMenuItem.addedToContextMenu = contextMenu;
        contextMenu.items.add(contextMenuItem);
    }

    static void contextMenu_removeItem(Contextmenu contextMenu, ContextmenuItem contextMenuItem) {
        if (contextMenuItem.addedToContextMenu != contextMenu) return;
        contextMenuItem.addedToContextMenu = null;
        contextMenu.items.remove(contextMenuItem);
    }

    static void contextMenu_selectItem(UIEngineState uiEngineState, ContextmenuItem contextMenuItem) {
        if (contextMenuItem.addedToContextMenu == null) return;
        Contextmenu ContextMenu = contextMenuItem.addedToContextMenu;
        if (contextMenuItem.contextMenuItemAction != null)
            contextMenuItem.contextMenuItemAction.onSelect();
        if (ContextMenu.contextMenuAction != null)
            ContextMenu.contextMenuAction.onItemSelected(contextMenuItem);
        UICommonUtils.contextMenu_close(uiEngineState, ContextMenu);
    }

    static void comboBox_selectItem(UIEngineState uiEngineState, ComboboxItem comboBoxItem) {
        if (comboBoxItem.addedToComboBox == null) return;
        Combobox comboBox = comboBoxItem.addedToComboBox;
        comboBox.selectedItem = comboBoxItem;
        if (comboBoxItem.comboBoxItemAction != null)
            comboBoxItem.comboBoxItemAction.onSelect();
        if (comboBox.comboBoxAction != null)
            comboBox.comboBoxAction.onItemSelected(comboBoxItem);
        UICommonUtils.comboBox_close(uiEngineState, comboBox);
    }

    static void comboBox_addItem(Combobox comboBox, ComboboxItem comboBoxItem) {
        if (comboBoxItem.addedToComboBox != null) return;
        comboBoxItem.addedToComboBox = comboBox;
        comboBox.comboBoxItems.add(comboBoxItem);
    }

    static void comboBox_removeItem(Combobox comboBox, ComboboxItem comboBoxItem) {
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

    static void canvas_resizeMap(UIEngineState uiEngineState, Canvas canvas) {
        int newWidth = uiEngineState.sizeSize.TL(canvas.width);
        int newHeight = uiEngineState.sizeSize.TL(canvas.height);
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

    static void toolTip_setImageSegmentImage(UIEngineState uiEngineState, MediaManager mediaManager, TooltipImageSegment tooltipImageSegment, CMediaSprite image) {
        tooltipImageSegment.image = image;
        if (tooltipImageSegment.image != null) {
            tooltipImageSegment.width = MathUtils.round((mediaManager.getCMediaSpriteWidth(image) + uiEngineState.sizeSize.TS) / uiEngineState.sizeSize.TSF);
            tooltipImageSegment.height = MathUtils.round((mediaManager.getCMediaSpriteHeight(image) + uiEngineState.sizeSize.TS) / uiEngineState.sizeSize.TSF);
        } else {
            tooltipImageSegment.width = 1;
            tooltipImageSegment.height = 1;
        }
    }

    static void toolTip_setTextSegmentText(UIEngineState uiEngineState, MediaManager mediaManager, TooltipTextSegment tooltipTextSegment, String text) {
        tooltipTextSegment.text = Tools.Text.validString(text);
        tooltipTextSegment.width = MathUtils.round((mediaManager.getCMediaFontTextWidth(tooltipTextSegment.font, tooltipTextSegment.text) + uiEngineState.sizeSize.TS) / uiEngineState.sizeSize.TSF);
        tooltipTextSegment.height = 1;
    }

    static void toolTip_addTooltipSegment(Tooltip toolTip, TooltipSegment segment) {
        if (segment.addedToTooltip != null) return;
        segment.addedToTooltip = toolTip;
        toolTip.segments.add(segment);
    }

    static void toolTip_removeTooltipSegment(Tooltip toolTip, TooltipSegment segment) {
        if (segment.addedToTooltip != toolTip) return;
        segment.addedToTooltip = null;
        toolTip.segments.remove(segment);
    }

    static boolean comboBox_isOpen(UIEngineState uiEngineState, Combobox comboBox) {
        return uiEngineState.openComboBox != null && uiEngineState.openComboBox == comboBox;
    }

    static boolean contextMenu_isOpen(UIEngineState uiEngineState, Contextmenu contextMenu) {
        return uiEngineState.openContextMenu != null && uiEngineState.openContextMenu == contextMenu;
    }

    static void comboBox_open(UIEngineState uiEngineState, Combobox comboBox) {
        // Close other Comboboxes
        if (uiEngineState.openComboBox != null) {
            comboBox_close(uiEngineState, uiEngineState.openComboBox);
        }
        // Open this one
        uiEngineState.openComboBox = comboBox;
        if (comboBox.comboBoxAction != null) comboBox.comboBoxAction.onOpen();
    }

    static void comboBox_close(UIEngineState uiEngineState, Combobox comboBox) {
        if (comboBox_isOpen(uiEngineState, comboBox)) {
            uiEngineState.openComboBox = null;
            uiEngineState.pressedComboBoxItem = null;
            if (comboBox.comboBoxAction != null) comboBox.comboBoxAction.onClose();
        }
    }

    static boolean textField_isFocused(UIEngineState uiEngineState, Textfield textField) {
        return uiEngineState.focusedTextField != null && uiEngineState.focusedTextField == textField;
    }

    static void textField_focus(UIEngineState uiEngineState, Textfield textField) {
        // Unfocus other textfields
        if (uiEngineState.focusedTextField != null && uiEngineState.focusedTextField != textField) {
            textField_unFocus(uiEngineState, uiEngineState.focusedTextField);
        }
        // Focus this one
        uiEngineState.focusedTextField = textField;
        if (textField.textFieldAction != null) textField.textFieldAction.onFocus();
    }

    static void textField_unFocus(UIEngineState uiEngineState, Textfield textField) {
        if (textField_isFocused(uiEngineState, textField)) {
            uiEngineState.focusedTextField = null;
            uiEngineState.focusedTextField_repeatedKey = KeyCode.NONE;
            uiEngineState.focusedTextField_repeatedKeyTimer = 0;
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
        return list.dragEnabled && list.dragOutEnabled && list.listAction != null && list.listAction.canDragIntoApp();
    }

    static boolean grid_canDragIntoScreen(Grid grid) {
        return grid.dragEnabled && grid.dragOutEnabled && grid.gridAction != null && grid.gridAction.canDragIntoApp();
    }

    static boolean list_canDragIntoList(UIEngineState uiEngineState, List list) {
        List draggedList = uiEngineState.draggedList;
        Grid draggedGrid = uiEngineState.draggedGrid;

        if (draggedList != null && draggedList == list && draggedList.dragEnabled) return true; // Into itself

        if (list.dragInEnabled && !list.disabled && list.listAction != null) {
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

    static void tabBar_updateItemInfoAtMousePosition(UIEngineState uiEngineState, Tabbar tabBar) {
        int x_bar = UICommonUtils.component_getAbsoluteX(tabBar);
        int y_bar = UICommonUtils.component_getAbsoluteY(tabBar);

        int tabXOffset = tabBar.tabOffset;
        for (int i = 0; i < tabBar.tabs.size(); i++) {
            Tab tab = tabBar.tabs.get(i);
            int tabWidth = tabBar.bigIconMode ? 2 : tab.width;
            if ((tabXOffset + tabWidth) > tabBar.width) {
                break;
            }

            int tabHeight = tabBar.bigIconMode ? uiEngineState.sizeSize.TS2 : uiEngineState.sizeSize.TS;
            if (Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y, x_bar + (tabXOffset * uiEngineState.sizeSize.TS), y_bar, tabWidth * uiEngineState.sizeSize.TS, tabHeight)) {
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

    static void list_updateItemInfoAtMousePosition(UIEngineState uiEngineState, List list) {
        if (list.items != null && list.listAction != null) {
            int itemFrom = MathUtils.round(list.scrolled * ((list.items.size()) - (list.height)));
            itemFrom = Math.max(itemFrom, 0);
            int x_list = UICommonUtils.component_getAbsoluteX(list);
            int y_list = UICommonUtils.component_getAbsoluteY(list);
            // insert between other items
            for (int iy = 0; iy < list.height; iy++) {
                int itemIndex = itemFrom + iy;
                if (itemIndex < list.items.size()) {
                    int itemOffsetY = ((list.height - 1) - iy);
                    if (Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y,
                            x_list, y_list + uiEngineState.sizeSize.TL(itemOffsetY), uiEngineState.sizeSize.TL(list.width), uiEngineState.sizeSize.TS)) {
                        uiEngineState.itemInfo_listIndex = itemIndex;
                        uiEngineState.itemInfo_listValid = true;
                        return;
                    }
                }
            }
            // Insert at end
            if (Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y, x_list, y_list, uiEngineState.sizeSize.TL(list.width), uiEngineState.sizeSize.TL(list.height))) {
                uiEngineState.itemInfo_listIndex = list.items.size();
                uiEngineState.itemInfo_listValid = true;
                return;
            }

        }

        uiEngineState.itemInfo_listIndex = 0;
        uiEngineState.itemInfo_listValid = false;
        return;
    }

    static void grid_updateItemInfoAtMousePosition(UIEngineState uiEngineState, Grid grid) {
        int tileSize = grid.doubleSized ? uiEngineState.sizeSize.TS2 : uiEngineState.sizeSize.TS;
        int x_grid = UICommonUtils.component_getAbsoluteX(grid);
        int y_grid = UICommonUtils.component_getAbsoluteY(grid);
        int inv_to_x = (uiEngineState.mouse_ui.x - x_grid) / tileSize;
        int inv_to_y = (uiEngineState.mouse_ui.y - y_grid) / tileSize;
        if (UICommonUtils.grid_positionValid(grid, inv_to_x, inv_to_y)) {
            uiEngineState.itemInfo_gridPos.x = inv_to_x;
            uiEngineState.itemInfo_gridPos.y = inv_to_y;
            uiEngineState.itemInfo_gridValid = true;
            return;
        }
        uiEngineState.itemInfo_gridPos.set(0, 0);
        uiEngineState.itemInfo_gridValid = false;
        return;
    }


    static boolean grid_canDragIntoGrid(UIEngineState uiEngineState, Grid grid) {
        List draggedList = uiEngineState.draggedList;
        Grid draggedGrid = uiEngineState.draggedGrid;

        if (draggedGrid != null && draggedGrid == grid && draggedGrid.dragEnabled) return true; // Into itself

        if (grid.dragInEnabled && !grid.disabled && grid.gridAction != null) {
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


    static void mouseTextInput_close(UIEngineState uiEngineState) {
        // mouseTextInput Keyboard
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

    static void text_setLines(UIEngineState uiEngineState, MediaManager mediaManager, Text text, String[] lines) {
        text.lines = Tools.Text.validStringArray(lines);
        UICommonUtils.text_updateSize(uiEngineState, mediaManager, text);
    }

    static void text_updateSize(UIEngineState uiEngineState, MediaManager mediaManager, Text text) {
        int width = 0;
        for (int i = 0; i < text.lines.length; i++) {
            int widthT = mediaManager.getCMediaFontTextWidth(text.font, text.lines[i]);
            if (widthT > width) width = widthT;
        }
        text.width = width / uiEngineState.sizeSize.TS;
        text.height = text.lines.length;
    }

    static void image_setImage(UIEngineState uiEngineState, MediaManager mediaManager, Image imageC, CMediaSprite image) {
        imageC.image = image;
        UICommonUtils.image_updateSize(uiEngineState, mediaManager, imageC);
    }

    static void image_updateSize(UIEngineState uiEngineState, MediaManager mediaManager, Image imageC) {
        imageC.width = imageC.image != null ? mediaManager.getCMediaSpriteWidth(imageC.image) / uiEngineState.sizeSize.TS : 0;
        imageC.height = imageC.image != null ? mediaManager.getCMediaSpriteHeight(imageC.image) / uiEngineState.sizeSize.TS : 0;
    }

    static void mouseTextInput_selectIndex(MouseTextInput mouseTextInput, int index) {
        int maxCharacters = Math.min(mouseTextInput.charactersLC.length, mouseTextInput.charactersUC.length);
        mouseTextInput.selectedIndex = Math.clamp(index, 0, (maxCharacters - 1));
    }

    static void scrollBar_scroll(Scrollbar scrollBar, float scrolled) {
        scrollBar.scrolled = Math.clamp(scrolled, 0f, 1f);
        if (scrollBar.scrollBarAction != null) scrollBar.scrollBarAction.onScrolled(scrollBar.scrolled);
    }

    static void scrollBar_pressButton(Scrollbar scrollBar) {
        if (scrollBar.buttonPressed) return;
        scrollBar.buttonPressed = true;
        if (scrollBar.scrollBarAction != null) scrollBar.scrollBarAction.onPress(scrollBar.scrolled);
    }

    static void scrollBar_releaseButton(Scrollbar scrollBar) {
        if (!scrollBar.buttonPressed) return;
        scrollBar.buttonPressed = false;
        if (scrollBar.scrollBarAction != null) scrollBar.scrollBarAction.onRelease(scrollBar.scrolled);
    }

    static float scrollBar_calculateScrolled(UIEngineState uiEngineState, Scrollbar scrollBar, int mouse_ui_x, int mouse_ui_y) {
        int relativePos;
        float maxPos;
        float buttonOffset;
        switch (scrollBar) {
            case ScrollbarHorizontal scrollBarHorizontal -> {
                relativePos = mouse_ui_x - UICommonUtils.component_getAbsoluteX(scrollBarHorizontal);
                maxPos = uiEngineState.sizeSize.TL(scrollBarHorizontal.width) - uiEngineState.sizeSize.TS;
                buttonOffset = (1 / (float) scrollBarHorizontal.width) / 2f;
            }
            case ScrollbarVertical scrollBarVertical -> {
                relativePos = mouse_ui_y - UICommonUtils.component_getAbsoluteY(scrollBarVertical);
                maxPos = uiEngineState.sizeSize.TL(scrollBarVertical.height) - uiEngineState.sizeSize.TS;
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

    static void canvas_setAllPoints(UIEngineState uiEngineState, Canvas canvas, float r, float g, float b, float a) {
        int width = uiEngineState.sizeSize.TL(canvas.width);
        int height = uiEngineState.sizeSize.TL(canvas.height);
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

    static boolean canvas_pointValid(Canvas canvas, int x, int y) {
        return x >= 0 && x <= canvas.map.length && y >= 0 && y <= canvas.map.length - 1;
    }

    static Color canvas_getPoint(Canvas canvas, int x, int y) {
        if (!canvas_pointValid(canvas, x, y)) return null;
        return canvas.map[x][y];
    }

    static void appViewPort_resizeCameraTextureAndFrameBuffer(UIEngineState uiEngineState, AppViewport appViewPort) {
        // Clean Up
        if (appViewPort.camera != null) appViewPort.camera = null;
        if (appViewPort.textureRegion != null) appViewPort.textureRegion.getTexture().dispose();
        if (appViewPort.frameBuffer != null) appViewPort.frameBuffer.dispose();

        int viewportWidth = uiEngineState.sizeSize.TL(appViewPort.width);
        int viewportHeight = uiEngineState.sizeSize.TL(appViewPort.height);
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

    static void viewport_changeViewPortMode(UIEngineState uiEngineState, VIEWPORT_MODE viewPortMode) {
        if (viewPortMode == null || viewPortMode == uiEngineState.viewportMode) return;
        uiEngineState.upscaleFactor_screen = UICommonUtils.viewport_determineUpscaleFactor(viewPortMode, uiEngineState.resolutionWidth, uiEngineState.resolutionHeight);
        uiEngineState.textureFilter_screen = UICommonUtils.viewport_determineUpscaleTextureFilter(viewPortMode);
        // frameBuffer_upScale
        uiEngineState.frameBuffer_screen.dispose();
        uiEngineState.frameBuffer_screen = new NestedFrameBuffer(Pixmap.Format.RGBA8888, uiEngineState.resolutionWidth * uiEngineState.upscaleFactor_screen, uiEngineState.resolutionHeight * uiEngineState.upscaleFactor_screen, false);
        uiEngineState.frameBuffer_screen.getColorBufferTexture().setFilter(uiEngineState.textureFilter_screen, uiEngineState.textureFilter_screen);
        // viewport_screen
        uiEngineState.viewport_screen = UICommonUtils.viewport_createViewport(viewPortMode, uiEngineState.camera_ui, uiEngineState.resolutionWidth, uiEngineState.resolutionHeight);
        uiEngineState.viewport_screen.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        // viewportMode
        uiEngineState.viewportMode = viewPortMode;

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
        camera.zoom = Math.max(zoom, 0f);
        camera.update();
    }

    static float ui_getAnimationTimer(UIEngineState state){
        if(state.config.ui_animationTimerFunction == null) return 0f;
        return state.config.ui_animationTimerFunction.getAnimationTimer();
    }

}
