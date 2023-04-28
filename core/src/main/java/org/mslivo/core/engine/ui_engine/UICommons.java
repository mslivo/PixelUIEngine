package org.mslivo.core.engine.ui_engine;

import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.ui_engine.gui.Window;
import org.mslivo.core.engine.ui_engine.gui.components.Component;
import org.mslivo.core.engine.ui_engine.gui.components.combobox.ComboBox;
import org.mslivo.core.engine.ui_engine.gui.components.inventory.Inventory;
import org.mslivo.core.engine.ui_engine.gui.components.map.MapOverlay;
import org.mslivo.core.engine.ui_engine.gui.components.tabbar.Tab;
import org.mslivo.core.engine.ui_engine.gui.components.tabbar.TabBar;
import org.mslivo.core.engine.ui_engine.gui.components.textfield.TextField;
import org.mslivo.core.engine.ui_engine.gui.components.viewport.GameViewPort;
import org.mslivo.core.engine.ui_engine.gui.contextmenu.ContextMenuItem;
import org.mslivo.core.engine.ui_engine.misc.ProgressBarPercentText;

class UICommons {

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
        if (tabBar == null) {
            return null;
        }
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

    static void textfield_setMarkerPosition(MediaManager mediaManager, TextField textField, int position) {
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

    static void textfield_setContent(TextField textField, String content) {
        textField.content = Tools.Text.validString(content);
        textField.markerPosition = Tools.Calc.inBounds(textField.markerPosition, 0, textField.content.length());
    }

    public static void removeComponentReferences(InputState inputState, Component component) {
        if (component.getClass() == GameViewPort.class) inputState.gameViewPorts.remove(component);
        if (component.addedToTab != null) component.addedToTab.components.remove(component);
        if (inputState.lastGUIMouseHover == component) inputState.lastGUIMouseHover = null;
        component.addedToTab = null;
        component.addedToWindow = null;
    }

    public static void removeWindowReferences(InputState inputState, Window window) {
        if (inputState.modalWindow != null && inputState.modalWindow == window) inputState.modalWindow = null;
        if (inputState.lastActiveWindow == window) inputState.lastActiveWindow = null;
        if (inputState.lastGUIMouseHover == window) inputState.lastGUIMouseHover = null;
    }

    public static void setComponentReferences(InputState inputState, Window window, Component component) {
        if (component.getClass() == GameViewPort.class) inputState.gameViewPorts.add((GameViewPort) component);
        component.addedToWindow = window;
    }

    public static void setWindowReferences(InputState inputState, Window window) {
        // Placeholder method
    }

    public static void removeTabReferences(Tab tab){
        tab.tabBar = null;
    }

    public static void removeContextMenuItemReferences(ContextMenuItem contextMenuItem){
        contextMenuItem.contextMenu = null;
    }

    public static void removeMapOverlayReferences(MapOverlay mapOverlay){
        mapOverlay.map = null;
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
        inputState.openComboBox = null;

        // ContextMenu
        inputState.openContextMenu = null;
        inputState.displayedContextMenuWidth = 0;

    }

    public static boolean comboBox_isOpen(InputState inputState, ComboBox comboBox){
        return inputState.openComboBox != null && inputState.openComboBox == comboBox;
    }

    public static void comboBox_open(InputState inputState, ComboBox comboBox){
        // Close other Comboboxes
        if(inputState.openComboBox != null){
            comboBox_close(inputState, inputState.openComboBox);
        }
        // Open this one
        inputState.openComboBox = comboBox;
        if(inputState.openComboBox.comboBoxAction != null) inputState.openComboBox.comboBoxAction.onOpen();
    }

    public static void comboBox_close(InputState inputState, ComboBox comboBox){
        if(comboBox_isOpen(inputState, comboBox)){
            if(inputState.openComboBox.comboBoxAction != null) inputState.openComboBox.comboBoxAction.onClose();
            inputState.openComboBox = null;
        }
    }

    public static boolean textField_isFocused(InputState inputState, TextField textField){
        return inputState.focusedTextField != null && inputState.focusedTextField == textField;
    }

    public static void textField_focus(InputState inputState, TextField textField){
        // Unfocus other textfields
        if(inputState.focusedTextField != null){
            textField_unFocus(inputState, inputState.focusedTextField);
        }
        // Focus this one
        inputState.focusedTextField = textField;
        if(inputState.focusedTextField.textFieldAction != null) inputState.focusedTextField.textFieldAction.onFocus();
    }

    public static void textField_unFocus(InputState inputState, TextField textField){
        if(textField_isFocused(inputState, textField)){
            if(inputState.focusedTextField.textFieldAction != null) inputState.focusedTextField.textFieldAction.onUnFocus();
            inputState.focusedTextField = null;
        }
    }

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

}
