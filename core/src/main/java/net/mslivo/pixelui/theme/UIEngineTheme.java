package net.mslivo.pixelui.theme;

import net.mslivo.pixelui.engine.constants.TileSize;
import net.mslivo.pixelui.media.*;

public abstract class UIEngineTheme {

    public final TileSize ts;

    public UIEngineTheme(int tileSize){
        this.ts = new TileSize(tileSize);
    }

    public CMediaArray UI_WINDOW;
    public CMediaArray UI_BUTTON;
    public CMediaArray UI_BUTTON_PRESSED;
    public CMediaArray UI_SCROLLBAR_VERTICAL;
    public CMediaArray UI_SCROLLBAR_HORIZONTAL;
    public CMediaArray UI_SCROLLBAR_BUTTON_VERTICAL;
    public CMediaArray UI_SCROLLBAR_BUTTON_HORIZONAL;
    public CMediaImage UI_LIST;
    public CMediaImage UI_LIST_CELL;
    public CMediaImage UI_LIST_CELL_SELECTED;
    public CMediaArray UI_LIST_DRAG;
    public CMediaArray UI_COMBO_BOX;
    public CMediaArray UI_COMBO_BOX_TOP;
    public CMediaArray UI_COMBO_BOX_OPEN;
    public CMediaArray UI_COMBO_BOX_LIST;
    public CMediaArray UI_COMBO_BOX_CELL;
    public CMediaArray UI_COMBO_BOX_LIST_CELL;
    public CMediaArray UI_COMBO_BOX_LIST_CELL_SELECTED;
    public CMediaArray UI_TAB_BORDERS;
    public CMediaArray UI_BORDERS;
    public CMediaArray UI_TAB;
    public CMediaArray UI_TAB_SELECTED;
    public CMediaImage UI_TAB_BIGICON;
    public CMediaImage UI_TAB_BIGICON_SELECTED;
    public CMediaImage UI_KNOB_BACKGROUND;
    public CMediaArray UI_KNOB;
    public CMediaArray UI_KNOB_ENDLESS;
    public CMediaArray UI_SEPARATOR_HORIZONTAL;
    public CMediaArray UI_SEPARATOR_VERTICAL;
    public CMediaArray UI_TOOLTIP_CELL;
    public CMediaArray UI_TOOLTIP;
    public CMediaArray UI_TOOLTIP_TOP;
    public CMediaImage UI_TOOLTIP_SEGMENT_BORDER;
    public CMediaImage UI_TOOLTIP_LINE_HORIZONTAL;
    public CMediaImage UI_TOOLTIP_LINE_VERTICAL;
    public CMediaArray UI_CONTEXT_MENU;
    public CMediaArray UI_CONTEXT_MENU_TOP;
    public CMediaArray UI_CONTEXT_MENU_CELL;
    public CMediaArray UI_CONTEXT_MENU_CELL_SELECTED;
    public CMediaArray UI_TEXT_FIELD;
    public CMediaArray UI_TEXT_FIELD_CELL_VALIDATION;
    public CMediaArray UI_TEXT_FIELD_CELL;
    public CMediaAnimation UI_TEXT_FIELD_CARET;
    public CMediaArray UI_GRID;
    public CMediaArray UI_GRID_DRAGGED;
    public CMediaArray UI_GRID_CELL;
    public CMediaArray UI_GRID_CELL_SELECTED;
    public CMediaArray UI_GRID_X2;
    public CMediaArray UI_GRID_DRAGGED_X2;
    public CMediaArray UI_GRID_CELL_X2;
    public CMediaArray UI_GRID_CELL_SELECTED_X2;
    public CMediaArray UI_PROGRESSBAR;
    public CMediaArray UI_PROGRESSBAR_BAR;
    public CMediaImage UI_NOTIFICATION_BAR;
    public CMediaArray UI_CHECKBOX_CHECKBOX;
    public CMediaImage UI_CHECKBOX_CHECKBOX_CELL;
    public CMediaArray UI_CHECKBOX_RADIO;
    public CMediaImage UI_CHECKBOX_RADIO_CELL;
    public CMediaArray UI_MOUSETEXTINPUT_BUTTON;
    public CMediaImage UI_MOUSETEXTINPUT_CONFIRM;
    public CMediaImage UI_MOUSETEXTINPUT_DELETE;
    public CMediaImage UI_MOUSETEXTINPUT_LOWERCASE;
    public CMediaImage UI_MOUSETEXTINPUT_UPPERCASE;
    public CMediaImage UI_MOUSETEXTINPUT_SELECTED;

    // Cursors
    public CMediaImage UI_CURSOR_ARROW;

    // Icons
    public CMediaImage UI_ICON_CLOSE;
    public CMediaImage UI_ICON_COLOR_PICKER;
    public CMediaImage UI_ICON_INFORMATION;
    public CMediaImage UI_ICON_QUESTION;
    public CMediaImage UI_ICON_EXTEND;
    public CMediaImage UI_ICON_BACK;
    public CMediaImage UI_ICON_FORWARD;
    public CMediaImage UI_ICON_KEY_DELETE;
    public CMediaImage UI_ICON_KEY_CASE;

    // Fonts
    public CMediaFont UI_FONT;

    // Misc
    public CMediaImage UI_PIXEL;
    public CMediaImage UI_PIXEL_TRANSPARENT;

    public CMedia[] cMedia(){
        return new CMedia[]{
                UI_WINDOW,
                UI_BUTTON,
                UI_BUTTON_PRESSED,
                UI_SCROLLBAR_VERTICAL,
                UI_SCROLLBAR_HORIZONTAL,
                UI_SCROLLBAR_BUTTON_VERTICAL,
                UI_SCROLLBAR_BUTTON_HORIZONAL,
                UI_LIST,
                UI_LIST_CELL,
                UI_LIST_CELL_SELECTED,
                UI_LIST_DRAG,
                UI_COMBO_BOX,
                UI_COMBO_BOX_TOP,
                UI_COMBO_BOX_OPEN,
                UI_COMBO_BOX_LIST,
                UI_COMBO_BOX_CELL,
                UI_COMBO_BOX_LIST_CELL,
                UI_COMBO_BOX_LIST_CELL_SELECTED,
                UI_TAB_BORDERS,
                UI_BORDERS,
                UI_TAB,
                UI_TAB_SELECTED,
                UI_TAB_BIGICON,
                UI_TAB_BIGICON_SELECTED,
                UI_KNOB_BACKGROUND,
                UI_KNOB,
                UI_KNOB_ENDLESS,
                UI_SEPARATOR_HORIZONTAL,
                UI_SEPARATOR_VERTICAL,
                UI_TOOLTIP_CELL,
                UI_TOOLTIP,
                UI_TOOLTIP_TOP,
                UI_TOOLTIP_SEGMENT_BORDER,
                UI_TOOLTIP_LINE_HORIZONTAL,
                UI_TOOLTIP_LINE_VERTICAL,
                UI_CONTEXT_MENU,
                UI_CONTEXT_MENU_TOP,
                UI_CONTEXT_MENU_CELL,
                UI_CONTEXT_MENU_CELL_SELECTED,
                UI_TEXT_FIELD,
                UI_TEXT_FIELD_CELL_VALIDATION,
                UI_TEXT_FIELD_CELL,
                UI_TEXT_FIELD_CARET,
                UI_GRID,
                UI_GRID_DRAGGED,
                UI_GRID_CELL,
                UI_GRID_CELL_SELECTED,
                UI_GRID_X2,
                UI_GRID_DRAGGED_X2,
                UI_GRID_CELL_X2,
                UI_GRID_CELL_SELECTED_X2,
                UI_PROGRESSBAR,
                UI_PROGRESSBAR_BAR,
                UI_NOTIFICATION_BAR,
                UI_CHECKBOX_CHECKBOX,
                UI_CHECKBOX_CHECKBOX_CELL,
                UI_CHECKBOX_RADIO,
                UI_CHECKBOX_RADIO_CELL,
                UI_MOUSETEXTINPUT_BUTTON,
                UI_MOUSETEXTINPUT_CONFIRM,
                UI_MOUSETEXTINPUT_DELETE,
                UI_MOUSETEXTINPUT_LOWERCASE,
                UI_MOUSETEXTINPUT_UPPERCASE,
                UI_MOUSETEXTINPUT_SELECTED,
                UI_CURSOR_ARROW,
                UI_ICON_CLOSE,
                UI_ICON_COLOR_PICKER,
                UI_ICON_INFORMATION,
                UI_ICON_QUESTION,
                UI_ICON_EXTEND,
                UI_ICON_BACK,
                UI_ICON_FORWARD,
                UI_ICON_KEY_DELETE,
                UI_ICON_KEY_CASE,
                UI_FONT,
                UI_PIXEL,
                UI_PIXEL_TRANSPARENT
        };
    }


}
