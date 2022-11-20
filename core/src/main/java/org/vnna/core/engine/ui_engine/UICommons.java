package org.vnna.core.engine.ui_engine;

import org.vnna.core.engine.media_manager.MediaManager;
import org.vnna.core.engine.tools.Tools;
import org.vnna.core.engine.ui_engine.gui.Window;
import org.vnna.core.engine.ui_engine.gui.components.Component;
import org.vnna.core.engine.ui_engine.gui.components.inventory.Inventory;
import org.vnna.core.engine.ui_engine.gui.components.tabbar.Tab;
import org.vnna.core.engine.ui_engine.gui.components.tabbar.TabBar;
import org.vnna.core.engine.ui_engine.gui.components.textfield.TextField;

import static java.awt.SystemColor.window;

class UICommons {

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
        if(tabBar == null){
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
        int wndheight =  window_getRealHeight(window);
        window.x = Tools.Calc.inBounds(window.x, 0, inputState.internalResolutionWidth - wndWidth);
        window.y = Tools.Calc.inBounds(window.y, 0, inputState.internalResolutionHeight - wndheight);
    }


    static boolean inventory_positionValid(Inventory inventory, int x, int y) {
        if (inventory.items != null) {
            if (x >= 0 && x < inventory.items.length && y >= 0 && y < inventory.items[0].length) {
                return true;
            }
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

    static void resetGUIVariables(InputState inputState) {
        inputState.draggedWindow = null;
        inputState.pressedButton = null;
        inputState.pressedButton_timer_hold = 0;
        inputState.turnedKnob = null;
        inputState.tooltip = null;
        inputState.tooltip_fadeIn_pct = 0f;
        inputState.tooltip_wait_delay = false;
        inputState.tooltip_delay_timer = 0;
        inputState.tooltip_fadeIn_timer = 0;
        inputState.scrolledScrollBarVertical = null;
        inputState.scrolledScrollBarHorizontal = null;
        inputState.inventoryDrag_Item = null;
        inputState.inventoryDrag_Inventory = null;
        inputState.InventoryDrag_offset_x = inputState.InventoryDrag_offset_y = 0;
        inputState.inventoryDrag_from_x = inputState.inventoryDrag_from_y = 0;
        inputState.listDrag_Item = null;
        inputState.listDrag_List = null;
        inputState.listDrag_offset_x = inputState.listDrag_offset_y = 0;
        inputState.listDrag_from_index = 0;
        inputState.tooltip_lastHoverObject = null;
        inputState.pressedMap = null;
        inputState.contextMenu = null;
        inputState.contextMenuWidth = 0;
    }


    static void window_bringToFront(InputState inputState, Window window){
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
