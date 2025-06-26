package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.actions.ContextMenuItemAction;
import net.mslivo.core.engine.ui_engine.ui.actions.ContextMenuAction;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenu;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenuItem;

import java.util.ArrayList;
import java.util.function.Predicate;

public final class APIContextMenu {
    private final API api;
    private final UIEngineState uiEngineState;
    private final MediaManager mediaManager;
    private final UIConfig uiConfig;
    public final APIContextMenuItem item;

    APIContextMenu(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.uiConfig = uiEngineState.config;
        this.mediaManager = mediaManager;
        this.item = new APIContextMenuItem();
    }

    public ContextMenuAction DEFAULT_CONTEXT_MENU_ACTION  = new ContextMenuAction() {};

    public ContextMenu create(ContextMenuItem[] contextMenuItems) {
        return create(contextMenuItems, DEFAULT_CONTEXT_MENU_ACTION);
    }

    public ContextMenu create(ContextMenuItem[] contextMenuItems, ContextMenuAction contextMenuAction) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.items = new Array<>();
        if (contextMenuItems != null) {
            for (int i = 0; i < contextMenuItems.length; i++) {
                if (contextMenuItems[i].addedToContextMenu == null) {
                    contextMenu.items.add(contextMenuItems[i]);
                    contextMenuItems[i].addedToContextMenu = contextMenu;
                }
            }
        }
        contextMenu.color = new Color(uiConfig.contextMenu_defaultColor);
        contextMenu.contextMenuAction = contextMenuAction != null ? contextMenuAction : DEFAULT_CONTEXT_MENU_ACTION;
        return contextMenu;
    }

    public void setContextMenuAction(ContextMenu contextMenu, ContextMenuAction contextMenuAction) {
        if (contextMenu == null) return;
        contextMenu.contextMenuAction = contextMenuAction;
    }

    public void setColor(ContextMenu contextMenu, Color color) {
        if (contextMenu == null) return;
        contextMenu.color.set(color);
    }

    public void addContextMenuItem(ContextMenu contextMenu, ContextMenuItem contextMenuItem) {
        if (contextMenu == null || contextMenuItem == null) return;
        UICommonUtils.contextMenu_addItem(contextMenu, contextMenuItem);
    }

    public void addContextMenuItems(ContextMenu contextMenu, ContextMenuItem[] contextMenuItems) {
        if (contextMenu == null || contextMenuItems == null) return;
        for (int i = 0; i < contextMenuItems.length; i++) addContextMenuItem(contextMenu, contextMenuItems[i]);
    }

    public void removeContextMenuItem(ContextMenu contextMenu, ContextMenuItem contextMenuItem) {
        if (contextMenu == null || contextMenuItem == null) return;
        UICommonUtils.contextMenu_removeItem(contextMenu, contextMenuItem);
    }

    public void removeContextMenuItems(ContextMenu contextMenu, ContextMenuItem[] contextMenuItems) {
        if (contextMenu == null || contextMenuItems == null) return;
        for (int i = 0; i < contextMenuItems.length; i++)
            removeContextMenuItem(contextMenu, contextMenuItems[i]);
    }

    public void removeAllContextMenuItems(ContextMenu contextMenu) {
        if (contextMenu == null) return;
        removeContextMenuItems(contextMenu, contextMenu.items.toArray(ContextMenuItem[]::new));
    }

    public Array<ContextMenuItem> findContextMenuItems(ContextMenu contextMenu, Predicate<ContextMenuItem> findBy) {
        if (contextMenu == null) return new Array<>();
        return UICommonUtils.findMultiple(contextMenu.items, findBy);
    }

    public ContextMenuItem findContextMenuItem(ContextMenu contextMenu, Predicate<ContextMenuItem> findBy) {
        if (contextMenu == null) return null;
        return UICommonUtils.find(contextMenu.items, findBy);
    }

    public final class APIContextMenuItem {

        APIContextMenuItem() {
        }

        public ContextMenuItemAction DEFAULT_CONTEXT_MENU_ITEM_ACTION = new ContextMenuItemAction() {};

        public ContextMenuItem create(String text) {
            return create(text, DEFAULT_CONTEXT_MENU_ITEM_ACTION);
        }

        public ContextMenuItem create(String text, ContextMenuItemAction contextMenuItemAction) {
            ContextMenuItem contextMenuItem = new ContextMenuItem();
            contextMenuItem.text = Tools.Text.validString(text);
            contextMenuItem.fontColor = uiConfig.ui_font_defaultColor.cpy();
            contextMenuItem.name = "";
            contextMenuItem.data = null;
            contextMenuItem.contextMenuItemAction = contextMenuItemAction;
            contextMenuItem.addedToContextMenu = null;
            return contextMenuItem;
        }

        public void setName(ContextMenuItem contextMenuItem, String name) {
            if (contextMenuItem == null) return;
            contextMenuItem.name = Tools.Text.validString(name);

        }

        public void setData(ContextMenuItem contextMenuItem, Object data) {
            if (contextMenuItem == null) return;
            contextMenuItem.data = data;
        }


        public void setFontColor(ContextMenuItem contextMenuItem, Color color) {
            if (contextMenuItem == null) return;
            contextMenuItem.fontColor.set(color);
        }

        public void setContextMenuItemAction(ContextMenuItem contextMenuItem, ContextMenuItemAction contextMenuItemAction) {
            if (contextMenuItem == null) return;
            contextMenuItem.contextMenuItemAction = contextMenuItemAction;
        }

        public void setText(ContextMenuItem contextMenuItem, String text) {
            if (contextMenuItem == null) return;
            contextMenuItem.text = Tools.Text.validString(text);
        }

        public void selectItem(ContextMenuItem contextMenuItem) {
            if (contextMenuItem == null) return;
            UICommonUtils.contextMenu_selectItem(uiEngineState, contextMenuItem);
        }

    }
}

