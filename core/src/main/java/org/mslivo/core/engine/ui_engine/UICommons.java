package org.mslivo.core.engine.ui_engine;

import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.ui_engine.gui.Window;
import org.mslivo.core.engine.ui_engine.gui.components.Component;
import org.mslivo.core.engine.ui_engine.gui.components.combobox.ComboBox;
import org.mslivo.core.engine.ui_engine.gui.components.combobox.ComboBoxItem;
import org.mslivo.core.engine.ui_engine.gui.components.inventory.Inventory;
import org.mslivo.core.engine.ui_engine.gui.components.map.Map;
import org.mslivo.core.engine.ui_engine.gui.components.map.MapOverlay;
import org.mslivo.core.engine.ui_engine.gui.components.tabbar.Tab;
import org.mslivo.core.engine.ui_engine.gui.components.tabbar.TabBar;
import org.mslivo.core.engine.ui_engine.gui.components.textfield.TextField;
import org.mslivo.core.engine.ui_engine.gui.components.viewport.GameViewPort;
import org.mslivo.core.engine.ui_engine.gui.contextmenu.ContextMenu;
import org.mslivo.core.engine.ui_engine.gui.contextmenu.ContextMenuItem;
import org.mslivo.core.engine.ui_engine.gui.notification.Notification;
import org.mslivo.core.engine.ui_engine.gui.tooltip.ToolTip;
import org.mslivo.core.engine.ui_engine.gui.tooltip.ToolTipImage;
import org.mslivo.core.engine.ui_engine.misc.ControlMode;
import org.mslivo.core.engine.ui_engine.misc.ProgressBarPercentText;

class UICommons {

    static void window_bringToFront(InputState inputState, Window window) {
        if (inputState.windows.size() == 1) return;
        if (window.alwaysOnTop) {
            if (inputState.windows.get(inputState.windows.size() - 1) != window) {
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

        inputState.lastActiveWindow = window;
    }

    public static void window_addToScreen(InputState inputState, Window window) {
        if (window.addedToScreen) return;
        window.addedToScreen = true;
        inputState.windows.add(window);
        if (window.windowAction != null) window.windowAction.onAdd();
    }

    public static void window_removeFromScreen(InputState inputState, Window window) {
        if (!window.addedToScreen) return;
        if (inputState.modalWindow != null && inputState.modalWindow == window) inputState.modalWindow = null;
        if (inputState.lastActiveWindow == window) inputState.lastActiveWindow = null;
        if (inputState.lastGUIMouseHover == window) inputState.lastGUIMouseHover = null;
        window.addedToScreen = false;
        inputState.windows.remove(window);
        if (window.windowAction != null) window.windowAction.onRemove();
    }


    public static void notification_addToScreen(InputState inputState, Notification notification, int notificationsMax) {
        if (notification.addedToScreen) return;
        notification.addedToScreen = true;
        inputState.notifications.add(notification);
        // Remove first if too many
        if (inputState.notifications.size() > notificationsMax) notification_removeFromScreen(inputState, inputState.notifications.get(0));
    }

    public static void notification_removeFromScreen(InputState inputState, Notification notification) {
        if (!notification.addedToScreen) return;
        notification.addedToScreen = false;
        inputState.notifications.remove(notification);
    }

    public static boolean contextMenu_openAtMousePosition(ContextMenu contextMenu, InputState inputState, MediaManager mediaManager) {
        boolean success = contextMenu_open(contextMenu, inputState, mediaManager, inputState.mouse_gui.x,inputState.mouse_gui.y);
        if(success && inputState.controlMode == ControlMode.KEYBOARD){
            inputState.mouse_gui.x += 4;
            inputState.mouse_gui.y -= 4;
        }
        return success;
    }

    public static boolean contextMenu_open(ContextMenu contextMenu, InputState inputState, MediaManager mediaManager, int x, int y) {
        if (contextMenu.items.size() == 0) return false;
        // Close open ContextMenus
        if (inputState.openContextMenu != null) {
            contextMenu_close(inputState.openContextMenu, inputState);
        }
        // Open this one
        contextMenu.x = inputState.mouse_gui.x;
        contextMenu.y = inputState.mouse_gui.y;
        int textwidth = 0;
        for (ContextMenuItem item : contextMenu.items) {
            int w = mediaManager.textWidth(item.font, item.text);
            if (item.icon != null) w = w + UIEngine.TILE_SIZE;
            if (w > textwidth) textwidth = w;
        }
        inputState.displayedContextMenuWidth = (textwidth + UIEngine.TILE_SIZE) / UIEngine.TILE_SIZE;
        inputState.openContextMenu = contextMenu;
        if(inputState.openContextMenu.contextMenuAction != null) inputState.openContextMenu.contextMenuAction.onOpen();
        return true;
    }

    public static void contextMenu_close(ContextMenu contextMenu, InputState inputState) {
        if(contextMenu_isOpen(contextMenu, inputState)) {
            inputState.openContextMenu = null;
            inputState.displayedContextMenuWidth = 0;
            if(contextMenu.contextMenuAction != null) contextMenu.contextMenuAction.onClose();
        }
    }

    public static String progressBar_getProgressText(float progress) {
        return ProgressBarPercentText.progressText[(int) (progress * 100)];
    }

    public static String progressBar_getProgressText2Decimal(float progress) {
        return ProgressBarPercentText.progressText2Decimal[(int) (progress * 10000)];
    }

    static int component_getParentWindowX(Component component) {
        return component.addedToWindow != null ? component.addedToWindow.x : 0;
    }

    static int component_getParentWindowY(Component component) {
        return component.addedToWindow != null ? component.addedToWindow.y : 0;
    }

    static int component_getAbsoluteX(Component component) {
        return component_getParentWindowX(component) + (component.x * UIEngine.TILE_SIZE) + component.offset_x;
    }

    static int component_getAbsoluteY(Component component) {
        return component_getParentWindowY(component) + (component.y * UIEngine.TILE_SIZE) + component.offset_y;
    }

    static Tab tabBar_getSelectedTab(TabBar tabBar) {
        if (tabBar == null) return null;
        return tabBar.tabs.get(Tools.Calc.inBounds(tabBar.selectedTab, 0, tabBar.tabs.size() - 1));
    }

    static void tabBar_selectTab(TabBar tabBar, int index) {
        tabBar.selectedTab = Tools.Calc.inBounds(index, 0, tabBar.tabs.size() - 1);
    }

    static int window_getRealWidth(Window window) {
        return window.width * UIEngine.TILE_SIZE;
    }

    static int window_getRealHeight(Window window) {
        if (!window.folded) {
            return window.height * UIEngine.TILE_SIZE;
        } else {
            return UIEngine.TILE_SIZE;
        }
    }

    static void window_enforceScreenBounds(InputState inputState, Window window) {
        int wndWidth = window_getRealWidth(window);
        int wndheight = window_getRealHeight(window);
        window.x = Tools.Calc.inBounds(window.x, 0, inputState.internalResolutionWidth - wndWidth);
        window.y = Tools.Calc.inBounds(window.y, 0, inputState.internalResolutionHeight - wndheight);
    }


    static boolean inventory_positionValid(Inventory inventory, int x, int y) {
        if (inventory.items != null) {
            return x >= 0 && x < inventory.items.length && y >= 0 && y < inventory.items[0].length;
        }
        return false;
    }

    static void textField_setMarkerPosition(MediaManager mediaManager, TextField textField, int position) {
        textField.markerPosition = Tools.Calc.inBounds(position, 0, textField.content.length());

        if (textField.markerPosition < textField.offset) {
            while (textField.markerPosition < textField.offset) {
                textField.offset--;
            }
        } else {
            String subContent = textField.content.substring(textField.offset, textField.markerPosition);
            int width = (textField.width * UIEngine.TILE_SIZE) - 4;
            if (mediaManager.textWidth(textField.font, subContent) > width) {
                while (mediaManager.textWidth(textField.font, subContent) > width) {
                    textField.offset++;
                    subContent = textField.content.substring(textField.offset, textField.markerPosition);
                }
            }
        }
    }

    static void textField_setContent(TextField textField, String content) {
        textField.content = Tools.Text.validString(content);
        textField.markerPosition = Tools.Calc.inBounds(textField.markerPosition, 0, textField.content.length());
    }


    public static void component_addToWindow(Component component, InputState inputState, Window window) {
        if (component.addedToWindow != null) return;
        if (component.addedToScreen) return;
        component_setCommonReferences(component, inputState);
        component.addedToWindow = window;
        window.components.add(component);
    }

    public static void component_removeFromWindow(Component component, InputState inputState, Window window) {
        if (component.addedToWindow == window) {
            component_removeCommonReferences(component, inputState);
            component.addedToWindow.components.remove(component);
            component.addedToWindow = null;
        }
    }

    public static void component_addToScreen(Component component, InputState inputState) {
        if (component.addedToWindow != null) return;
        if (component.addedToScreen) return;
        component_setCommonReferences(component, inputState);
        component.addedToScreen = true;
        inputState.screenComponents.add(component);
    }

    public static void component_removeFromScreen(Component component, InputState inputState) {
        if (!component.addedToScreen) return;
        component_removeCommonReferences(component, inputState);
        component.addedToScreen = true;
        inputState.screenComponents.remove(component);
    }

    private static void component_setCommonReferences(Component component, InputState inputState) {
        if (component.getClass() == GameViewPort.class) inputState.gameViewPorts.add((GameViewPort) component);
    }

    private static void component_removeCommonReferences(Component component, InputState inputState) {
        if (component.addedToTab != null) tab_removeComponent(component.addedToTab, component);
        if (component.getClass() == GameViewPort.class) inputState.gameViewPorts.remove((GameViewPort) component);
        if (inputState.lastGUIMouseHover == component) inputState.lastGUIMouseHover = null;
    }


    public static void tab_removeComponent(Tab tab, Component component) {
        if (component.addedToTab != tab) return;
        component.addedToTab.components.remove(component);
        component.addedToTab = tab;
    }

    public static void tab_addComponent(Tab tab, Component component) {
        if (component.addedToTab != null) return;
        component.addedToTab = tab;
        tab.components.add(component);
    }


    public static void tabBar_addTab(TabBar tabBar, Tab tab) {
        if (tab.addedToTabBar != null) return;
        tab.addedToTabBar = tabBar;
        tabBar.tabs.add(tab);
    }

    public static void tabBar_addTab(TabBar tabBar, Tab tab, int index) {
        if (tab.addedToTabBar != null) return;
        tab.addedToTabBar = tabBar;
        tabBar.tabs.add(index, tab);
    }

    public static void tabBar_removeTab(TabBar tabBar, Tab tab) {
        if (tab.addedToTabBar != tabBar) return;
        tab.addedToTabBar = null;
        tabBar.tabs.remove(tab);
    }

    public static void contextMenu_addItem(ContextMenu contextMenu, ContextMenuItem contextMenuItem) {
        if (contextMenuItem.addedToContextMenu != null) return;
        contextMenuItem.addedToContextMenu = contextMenu;
        contextMenu.items.add(contextMenuItem);
    }

    public static void contextMenu_removeItem(ContextMenu contextMenu, ContextMenuItem contextMenuItem) {
        if (contextMenuItem.addedToContextMenu != contextMenu) return;
        contextMenuItem.addedToContextMenu = null;
        contextMenu.items.remove(contextMenuItem);
    }


    public static void comboBox_addItem(ComboBox comboBox, ComboBoxItem comboBoxItem) {
        if (comboBoxItem.addedToComboBox != null) return;
        comboBoxItem.addedToComboBox = comboBox;
        comboBox.items.add(comboBoxItem);
    }

    public static void comboBox_removeItem(ComboBox comboBox, ComboBoxItem comboBoxItem) {
        if (comboBoxItem.addedToComboBox != comboBox) return;
        if (comboBox.selectedItem == comboBoxItem) comboBox.selectedItem = null;
        comboBoxItem.addedToComboBox = null;
        comboBox.items.remove(comboBoxItem);
    }


    public static void map_addMapOverlay(Map map, MapOverlay mapOverlay) {
        if (mapOverlay.addedToMap != null) return;
        mapOverlay.addedToMap = map;
        map.mapOverlays.add(mapOverlay);
    }

    public static void map_removeMapOverlay(Map map, MapOverlay mapOverlay) {
        if (mapOverlay.addedToMap != map) return;
        mapOverlay.addedToMap = null;
        map.mapOverlays.remove(mapOverlay);
    }

    public static void toolTip_addToolTipImage(ToolTip toolTip, ToolTipImage toolTipImage) {
        if (toolTipImage.addedToToolTip != null) return;
        toolTipImage.addedToToolTip = toolTip;
        toolTip.images.add(toolTipImage);
    }

    public static void toolTip_removeToolTipImage(ToolTip toolTip, ToolTipImage toolTipImage) {
        if (toolTipImage.addedToToolTip != toolTip) return;
        toolTipImage.addedToToolTip = null;
        toolTip.images.remove(toolTipImage);
    }


    static void resetGUIVariables(InputState inputState) {
        // Window
        inputState.draggedWindow = null;
        inputState.draggedWindow_offset.x = inputState.draggedWindow_offset.y = 0;
        // Buton
        inputState.pressedButton = null;
        inputState.pressedButton_timer_hold = 0;
        // Scrollbar
        inputState.scrolledScrollBarVertical = null;
        inputState.scrolledScrollBarHorizontal = null;

        // ToolTip
        inputState.tooltip = null;
        inputState.tooltip_fadeIn_pct = 0f;
        inputState.tooltip_wait_delay = false;
        inputState.tooltip_delay_timer = 0;
        inputState.tooltip_fadeIn_timer = 0;
        inputState.tooltip_lastHoverObject = null;
        inputState.gameToolTip = null;

        // Knob
        inputState.turnedKnob = null;

        // Map
        inputState.pressedMap = null;

        // Viewport
        inputState.pressedGameViewPort = null;

        // Textfield
        inputState.focusedTextField = null;

        // Inventory
        inputState.inventoryDrag_Inventory = null;
        inputState.inventoryDrag_from.x = inputState.inventoryDrag_from.y = 0;
        inputState.inventoryDrag_offset.x = inputState.inventoryDrag_offset.y = 0;
        inputState.inventoryDrag_Item = null;

        // List
        inputState.listDrag_List = null;
        inputState.listDrag_from_index = 0;
        inputState.listDrag_offset.x = inputState.listDrag_offset.y = 0;
        inputState.listDrag_Item = null;

        // ComboBox
        if(inputState.openComboBox != null) UICommons.comboBox_close(inputState.openComboBox, inputState);

        // ContextMenu
        if(inputState.openContextMenu != null) UICommons.contextMenu_close(inputState.openContextMenu, inputState);
    }

    public static boolean comboBox_isOpen(ComboBox comboBox, InputState inputState) {
        return inputState.openComboBox != null && inputState.openComboBox == comboBox;
    }

    public static boolean contextMenu_isOpen(ContextMenu contextMenu, InputState inputState) {
        return inputState.openContextMenu != null && inputState.openContextMenu == contextMenu;
    }

    public static void comboBox_open(ComboBox comboBox, InputState inputState) {
        // Close other Comboboxes
        if (inputState.openComboBox != null) {
            comboBox_close(inputState.openComboBox, inputState);
        }
        // Open this one
        inputState.openComboBox = comboBox;
        if (inputState.openComboBox.comboBoxAction != null) inputState.openComboBox.comboBoxAction.onOpen();
    }

    public static void comboBox_close(ComboBox comboBox, InputState inputState) {
        if (comboBox_isOpen(comboBox, inputState)) {
            inputState.openComboBox = null;
            if (comboBox.comboBoxAction != null) comboBox.comboBoxAction.onClose();
        }
    }

    public static boolean textField_isFocused(InputState inputState, TextField textField) {
        return inputState.focusedTextField != null && inputState.focusedTextField == textField;
    }

    public static void textField_focus(InputState inputState, TextField textField) {
        // Unfocus other textfields
        if (inputState.focusedTextField != null) {
            textField_unFocus(inputState, inputState.focusedTextField);
        }
        // Focus this one
        inputState.focusedTextField = textField;
        if (inputState.focusedTextField.textFieldAction != null) inputState.focusedTextField.textFieldAction.onFocus();
    }

    public static void textField_unFocus(InputState inputState, TextField textField) {
        if (textField_isFocused(inputState, textField)) {
            if (inputState.focusedTextField.textFieldAction != null)
                inputState.focusedTextField.textFieldAction.onUnFocus();
            inputState.focusedTextField = null;
        }
    }


}
