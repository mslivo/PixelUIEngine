package net.mslivo.pixelui.theme;

import net.mslivo.pixelui.media.*;

public class BaseTheme8x8 extends UIEngineTheme {
    private static final String DIR_THEME = MediaManager.DIR_GRAPHICS + "pixelui/base8x8/";

    public BaseTheme8x8() {
        super(8);
        final int TL = ts.TS;
        final int TL2 = ts.TS2;
        final int TL1_2 = ts.TS_1_AND_HALF;

        UI_WINDOW = new CMediaArray(DIR_THEME + "ui/window.png", TL, TL);
        UI_BUTTON = new CMediaArray(DIR_THEME + "ui/button.png", TL, TL);
        UI_BUTTON_PRESSED = new CMediaArray(DIR_THEME + "ui/button_pressed.png", TL, TL);
        UI_SCROLLBAR_VERTICAL = new CMediaArray(DIR_THEME + "ui/scrollbar_vertical.png", TL, TL);
        UI_SCROLLBAR_HORIZONTAL = new CMediaArray(DIR_THEME + "ui/scrollbar_horizontal.png", TL, TL);
        UI_SCROLLBAR_BUTTON_VERTICAL = new CMediaArray(DIR_THEME + "ui/scrollbar_button_vertical.png", TL, TL);
        UI_SCROLLBAR_BUTTON_HORIZONAL = new CMediaArray(DIR_THEME + "ui/scrollbar_button_horizontal.png", TL, TL);
        UI_LIST = new CMediaImage(DIR_THEME + "ui/list.png");
        UI_LIST_CELL = new CMediaImage(DIR_THEME + "ui/list_cell.png");
        UI_LIST_CELL_SELECTED = new CMediaImage(DIR_THEME + "ui/list_cell_selected.png");
        UI_LIST_DRAG = new CMediaArray(DIR_THEME + "ui/list_drag.png", TL, TL2);
        UI_COMBO_BOX = new CMediaArray(DIR_THEME + "ui/combobox.png", TL, TL);
        UI_COMBO_BOX_TOP = new CMediaArray(DIR_THEME + "ui/combobox_top.png", TL, TL);
        UI_COMBO_BOX_OPEN = new CMediaArray(DIR_THEME + "ui/combobox_open.png", TL, TL);
        UI_COMBO_BOX_LIST = new CMediaArray(DIR_THEME + "ui/combobox_list.png", TL, TL);
        UI_COMBO_BOX_CELL = new CMediaArray(DIR_THEME + "ui/combobox_cell.png", TL, TL);
        UI_COMBO_BOX_LIST_CELL = new CMediaArray(DIR_THEME + "ui/combobox_list_cell.png", TL, TL);
        UI_COMBO_BOX_LIST_CELL_SELECTED = new CMediaArray(DIR_THEME + "ui/combobox_list_cell_selected.png", TL, TL);
        UI_TAB_BORDERS = new CMediaArray(DIR_THEME + "ui/tab_border.png", TL, TL);
        UI_BORDERS = new CMediaArray(DIR_THEME + "ui/border.png", TL, TL);
        UI_TAB = new CMediaArray(DIR_THEME + "ui/tab.png", TL, TL);
        UI_TAB_SELECTED = new CMediaArray(DIR_THEME + "ui/tab_selected.png", TL, TL);
        UI_TAB_BIGICON = new CMediaImage(DIR_THEME + "ui/tab_bigicon.png");
        UI_TAB_BIGICON_SELECTED = new CMediaImage(DIR_THEME + "ui/tab__bigicon_selected.png");
        UI_KNOB_BACKGROUND = new CMediaImage(DIR_THEME + "ui/knob_background.png");
        UI_KNOB = new CMediaArray(DIR_THEME + "ui/knob.png", TL2, TL2);
        UI_KNOB_ENDLESS = new CMediaArray(DIR_THEME + "ui/knob_endless.png", TL2, TL2);
        UI_SEPARATOR_HORIZONTAL = new CMediaArray(DIR_THEME + "ui/separator_horizontal.png", TL, TL);
        UI_SEPARATOR_VERTICAL = new CMediaArray(DIR_THEME + "ui/separator_vertical.png", TL, TL);
        UI_TOOLTIP_CELL = new CMediaArray(DIR_THEME + "ui/tooltip_cell.png", TL, TL);
        UI_TOOLTIP = new CMediaArray(DIR_THEME + "ui/tooltip.png", TL, TL);
        UI_TOOLTIP_TOP = new CMediaArray(DIR_THEME + "ui/tooltip_top.png", TL, TL);
        UI_TOOLTIP_SEGMENT_BORDER = new CMediaImage(DIR_THEME + "ui/tooltip_segment_border.png");
        UI_TOOLTIP_LINE_HORIZONTAL = new CMediaImage(DIR_THEME + "ui/tooltip_line_horizontal.png");
        UI_TOOLTIP_LINE_VERTICAL = new CMediaImage(DIR_THEME + "ui/tooltip_line_vertical.png");
        UI_CONTEXT_MENU = new CMediaArray(DIR_THEME + "ui/context_menu.png", TL, TL);
        UI_CONTEXT_MENU_TOP = new CMediaArray(DIR_THEME + "ui/context_menu_top.png", TL, TL);
        UI_CONTEXT_MENU_CELL = new CMediaArray(DIR_THEME + "ui/context_menu_cell.png", TL, TL);
        UI_CONTEXT_MENU_CELL_SELECTED = new CMediaArray(DIR_THEME + "ui/context_menu_cell_selected.png", TL, TL);
        UI_TEXT_FIELD = new CMediaArray(DIR_THEME + "ui/textfield.png", TL, TL);
        UI_TEXT_FIELD_CELL_VALIDATION = new CMediaArray(DIR_THEME + "ui/textfield_cell_validation.png", TL, TL);
        UI_TEXT_FIELD_CELL = new CMediaArray(DIR_THEME + "ui/textfield_cell.png", TL, TL);
        UI_TEXT_FIELD_CARET = new CMediaAnimation(DIR_THEME + "ui/textfield_caret.png", 1, TL, 0.4f);
        UI_GRID = new CMediaArray(DIR_THEME + "ui/grid.png", TL, TL);
        UI_GRID_DRAGGED = new CMediaArray(DIR_THEME + "ui/grid_dragged.png", TL, TL);
        UI_GRID_CELL = new CMediaArray(DIR_THEME + "ui/grid_cell.png", TL, TL);
        UI_GRID_CELL_SELECTED = new CMediaArray(DIR_THEME + "ui/grid_cell_selected.png", TL, TL);
        UI_GRID_X2 = new CMediaArray(DIR_THEME + "ui/grid_x2.png", TL2, TL2);
        UI_GRID_DRAGGED_X2 = new CMediaArray(DIR_THEME + "ui/grid_dragged_x2.png", TL2, TL2);
        UI_GRID_CELL_X2 = new CMediaArray(DIR_THEME + "ui/grid_cell_x2.png", TL2, TL2);
        UI_GRID_CELL_SELECTED_X2 = new CMediaArray(DIR_THEME + "ui/grid_cell_selected_x2.png", TL2, TL2);
        UI_PROGRESSBAR = new CMediaArray(DIR_THEME + "ui/progressbar.png", TL, TL);
        UI_PROGRESSBAR_BAR = new CMediaArray(DIR_THEME + "ui/progressbar_bar.png", TL, TL);
        UI_NOTIFICATION_BAR = new CMediaImage(DIR_THEME + "ui/notification_bar.png");
        UI_CHECKBOX_CHECKBOX = new CMediaArray(DIR_THEME + "ui/checkbox.png", TL, TL);
        UI_CHECKBOX_CHECKBOX_CELL = new CMediaImage(DIR_THEME + "ui/checkbox_cell.png");
        UI_CHECKBOX_RADIO = new CMediaArray(DIR_THEME + "ui/radio.png", TL, TL);
        UI_CHECKBOX_RADIO_CELL = new CMediaImage(DIR_THEME + "ui/radio_cell.png");
        UI_MOUSETEXTINPUT_BUTTON = new CMediaArray(DIR_THEME + "ui/mousetextinput_button.png", TL1_2, TL1_2);
        UI_MOUSETEXTINPUT_CONFIRM = new CMediaImage(DIR_THEME + "ui/mousetextinput_confirm.png");
        UI_MOUSETEXTINPUT_DELETE = new CMediaImage(DIR_THEME + "ui/mousetextinput_delete.png");
        UI_MOUSETEXTINPUT_LOWERCASE = new CMediaImage(DIR_THEME + "ui/mousetextinput_lowercase.png");
        UI_MOUSETEXTINPUT_UPPERCASE = new CMediaImage(DIR_THEME + "ui/mousetextinput_uppercase.png");
        UI_MOUSETEXTINPUT_SELECTED = new CMediaImage(DIR_THEME + "ui/mousetextinput_selected.png");

        // Cursors
         UI_CURSOR_ARROW = new CMediaImage(DIR_THEME + "cursors/arrow.png");

        // Icons
        UI_ICON_CLOSE = new CMediaImage(DIR_THEME + "icons/close.icon.png");
        UI_ICON_COLOR_PICKER = new CMediaImage(DIR_THEME + "icons/color_picker.icon.png");
        UI_ICON_INFORMATION = new CMediaImage(DIR_THEME + "icons/information.icon.png");
        UI_ICON_QUESTION = new CMediaImage(DIR_THEME + "icons/question.icon.png");
        UI_ICON_EXTEND = new CMediaImage(DIR_THEME + "icons/extend.icon.png");
        UI_ICON_BACK = new CMediaImage(DIR_THEME + "icons/back.icon.png");
        UI_ICON_FORWARD = new CMediaImage(DIR_THEME + "icons/forward.icon.png");
        UI_ICON_KEY_DELETE = new CMediaImage(DIR_THEME + "icons/key_delete.icon.png");
        UI_ICON_KEY_CASE = new CMediaImage(DIR_THEME + "icons/key_case.icon.png");


        // Fonts
         UI_FONT = new CMediaFont(DIR_THEME + "fonts/font.fnt");

        // Misc
         UI_PIXEL = new CMediaImage(DIR_THEME + "misc/pixel.png");
         UI_PIXEL_TRANSPARENT = new CMediaImage(DIR_THEME + "misc/pixel_transparent.png");
    }

}
