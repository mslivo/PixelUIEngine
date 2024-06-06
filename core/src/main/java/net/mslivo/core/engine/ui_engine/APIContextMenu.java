package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.actions.ContextMenuItemAction;
import net.mslivo.core.engine.ui_engine.ui.actions.ContextmenuAction;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.Contextmenu;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextmenuItem;

import java.util.ArrayList;

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

    private ContextmenuAction defaultContextMenuAction() {
        return new ContextmenuAction() {
        };
    }

    public Contextmenu create(ContextmenuItem[] contextMenuItems) {
        return create(contextMenuItems, defaultContextMenuAction(), 1f);
    }

    public Contextmenu create(ContextmenuItem[] contextMenuItems, ContextmenuAction contextMenuAction) {
        return create(contextMenuItems, contextMenuAction, 1f);
    }

    public Contextmenu create(ContextmenuItem[] contextMenuItems, ContextmenuAction contextMenuAction, float alpha) {
        Contextmenu contextMenu = new Contextmenu();
        contextMenu.items = new ArrayList<>();
        if (contextMenuItems != null) {
            for (int i = 0; i < contextMenuItems.length; i++) {
                if (contextMenuItems[i].addedToContextMenu == null) {
                    contextMenu.items.add(contextMenuItems[i]);
                    contextMenuItems[i].addedToContextMenu = contextMenu;
                }
            }
        }
        contextMenu.color_a = Math.clamp(alpha, 0f, 1f);
        contextMenu.contextMenuAction = contextMenuAction;
        return contextMenu;
    }

    public void setContextMenuAction(Contextmenu contextMenu, ContextmenuAction contextMenuAction) {
        if (contextMenu == null) return;
        contextMenu.contextMenuAction = contextMenuAction;
    }

    public void setAlpha(Contextmenu contextMenu, float alpha) {
        if (contextMenu == null) return;
        contextMenu.color_a = Math.clamp(alpha, 0f, 1f);
    }

    public void addContextMenuItem(Contextmenu contextMenu, ContextmenuItem contextMenuItem) {
        if (contextMenu == null || contextMenuItem == null) return;
        UICommonUtils.contextMenu_addItem(contextMenu, contextMenuItem);
    }

    public void addContextMenuItems(Contextmenu contextMenu, ContextmenuItem[] contextMenuItems) {
        if (contextMenu == null || contextMenuItems == null) return;
        for (int i = 0; i < contextMenuItems.length; i++) addContextMenuItem(contextMenu, contextMenuItems[i]);
    }

    public void removeContextMenuItem(Contextmenu contextMenu, ContextmenuItem contextMenuItem) {
        if (contextMenu == null || contextMenuItem == null) return;
        UICommonUtils.contextMenu_removeItem(contextMenu, contextMenuItem);
    }

    public void removeContextMenuItems(Contextmenu contextMenu, ContextmenuItem[] contextMenuItems) {
        if (contextMenu == null || contextMenuItems == null) return;
        for (int i = 0; i < contextMenuItems.length; i++)
            removeContextMenuItem(contextMenu, contextMenuItems[i]);
    }

    public void removeAllContextMenuItems(Contextmenu contextMenu) {
        if (contextMenu == null) return;
        removeContextMenuItems(contextMenu, contextMenu.items.toArray(new ContextmenuItem[]{}));
    }

    public ArrayList<ContextmenuItem> findContextMenuItemsByName(Contextmenu contextMenu, String name) {
        if (contextMenu == null || name == null) return new ArrayList<>();
        ArrayList<ContextmenuItem> result = new ArrayList<>();
        for (int i = 0; i < contextMenu.items.size(); i++)
            if (name.equals(contextMenu.items.get(i).name)) result.add(contextMenu.items.get(i));
        return result;
    }

    public ContextmenuItem findContextMenuItemByName(Contextmenu contextMenu, String name) {
        if (contextMenu == null || name == null) return null;
        ArrayList<ContextmenuItem> result = findContextMenuItemsByName(contextMenu, name);
        return result.size() > 0 ? result.getFirst() : null;
    }


    public final class APIContextMenuItem {

        APIContextMenuItem() {
        }

        private ContextMenuItemAction defaultContextMenuItemAction() {
            return new ContextMenuItemAction() {
            };
        }

        public ContextmenuItem create(String text) {
            return create(text, defaultContextMenuItemAction(), null, 0);
        }

        public ContextmenuItem create(String text, ContextMenuItemAction contextMenuItemAction) {
            return create(text, contextMenuItemAction, null, 0);
        }

        public ContextmenuItem create(String text, ContextMenuItemAction contextMenuItemAction, CMediaSprite icon, int iconIndex) {
            ContextmenuItem contextMenuItem = new ContextmenuItem();
            contextMenuItem.text = Tools.Text.validString(text);
            contextMenuItem.font = uiConfig.component_defaultFont;
            contextMenuItem.color_r = uiConfig.component_defaultColor.r;
            contextMenuItem.color_g = uiConfig.component_defaultColor.g;
            contextMenuItem.color_b = uiConfig.component_defaultColor.b;
            contextMenuItem.icon = icon;
            contextMenuItem.iconIndex = iconIndex;
            contextMenuItem.name = "";
            contextMenuItem.data = null;
            contextMenuItem.contextMenuItemAction = contextMenuItemAction;
            contextMenuItem.addedToContextMenu = null;
            return contextMenuItem;
        }

        public void setName(ContextmenuItem contextMenuItem, String name) {
            if (contextMenuItem == null) return;
            contextMenuItem.name = Tools.Text.validString(name);

        }

        public void setData(ContextmenuItem contextMenuItem, Object data) {
            if (contextMenuItem == null) return;
            contextMenuItem.data = data;
        }

        public void setColor(ContextmenuItem contextMenuItem, Color color) {
            if (contextMenuItem == null || color == null) return;
            setColor(contextMenuItem, color.r, color.b, color.g);
        }

        public void setColor(ContextmenuItem contextMenuItem, float r, float g, float b) {
            if (contextMenuItem == null) return;
            contextMenuItem.color_r = r;
            contextMenuItem.color_g = g;
            contextMenuItem.color_b = b;
        }

        public void setFont(ContextmenuItem contextMenuItem, CMediaFont font) {
            if (contextMenuItem == null) return;
            contextMenuItem.font = font;
        }

        public void setContextMenuItemAction(ContextmenuItem contextMenuItem, ContextMenuItemAction contextMenuItemAction) {
            if (contextMenuItem == null) return;
            contextMenuItem.contextMenuItemAction = contextMenuItemAction;
        }

        public void setText(ContextmenuItem contextMenuItem, String text) {
            if (contextMenuItem == null) return;
            contextMenuItem.text = Tools.Text.validString(text);
        }

        public void setIcon(ContextmenuItem contextMenuItem, CMediaSprite icon) {
            if (contextMenuItem == null) return;
            contextMenuItem.icon = icon;
        }

        public void setIconIndex(ContextmenuItem contextMenuItem, int index) {
            if (contextMenuItem == null) return;
            contextMenuItem.iconIndex = Math.max(index, 0);
        }

        public void selectItem(ContextmenuItem contextMenuItem) {
            if (contextMenuItem == null) return;
            UICommonUtils.contextMenu_selectItem(uiEngineState, contextMenuItem);
        }

    }
}

