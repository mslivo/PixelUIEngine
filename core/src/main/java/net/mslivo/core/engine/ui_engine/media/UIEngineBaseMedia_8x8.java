package net.mslivo.core.engine.ui_engine.media;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import net.mslivo.core.engine.media_manager.*;

/**
 * These Medias need to be loaded on startup for the UI to work no matter what
 */
public class UIEngineBaseMedia_8x8 {
    private static final String DIR_UI_GRAPHICS = MediaManager.DIR_GRAPHICS + "pixelui_8x8/";
    private static final int TL = 8;
    private static final int TL2 = TL*2;

    // UI Elements
    public static final CMediaArray UI_WINDOW = new CMediaArray(DIR_UI_GRAPHICS + "ui/window.png", TL, TL);
    public static final CMediaArray UI_BUTTON = new CMediaArray(DIR_UI_GRAPHICS + "ui/button.png", TL, TL);
    public static final CMediaArray UI_BUTTON_PRESSED = new CMediaArray(DIR_UI_GRAPHICS + "ui/button_pressed.png", TL, TL);
    public static final CMediaArray UI_SCROLLBAR_VERTICAL = new CMediaArray(DIR_UI_GRAPHICS + "ui/scrollbar_vertical.png", TL, TL);
    public static final CMediaArray UI_SCROLLBAR_HORIZONTAL = new CMediaArray(DIR_UI_GRAPHICS + "ui/scrollbar_horizontal.png", TL, TL);
    public static final CMediaArray UI_SCROLLBAR_BUTTON_VERTICAL = new CMediaArray(DIR_UI_GRAPHICS + "ui/scrollbar_button_vertical.png", TL, TL);
    public static final CMediaArray UI_SCROLLBAR_BUTTON_HORIZONAL = new CMediaArray(DIR_UI_GRAPHICS + "ui/scrollbar_button_horizontal.png", TL, TL);
    public static final CMediaImage UI_LIST = new CMediaImage(DIR_UI_GRAPHICS + "ui/list.png");
    public static final CMediaImage UI_LIST_CELL = new CMediaImage(DIR_UI_GRAPHICS + "ui/list_cell.png");
    public static final CMediaImage UI_LIST_CELL_SELECTED = new CMediaImage(DIR_UI_GRAPHICS + "ui/list_cell_selected.png");
    public static final CMediaArray UI_LIST_DRAG = new CMediaArray(DIR_UI_GRAPHICS + "ui/list_drag.png", TL, TL * 2);
    public static final CMediaArray UI_COMBOBOX = new CMediaArray(DIR_UI_GRAPHICS + "ui/combobox.png", TL, TL);
    public static final CMediaArray UI_COMBOBOX_TOP = new CMediaArray(DIR_UI_GRAPHICS + "ui/combobox_top.png", TL, TL);
    public static final CMediaArray UI_COMBOBOX_OPEN = new CMediaArray(DIR_UI_GRAPHICS + "ui/combobox_open.png", TL, TL);
    public static final CMediaArray UI_COMBOBOX_LIST = new CMediaArray(DIR_UI_GRAPHICS + "ui/combobox_list.png", TL, TL);
    public static final CMediaArray UI_COMBOBOX_CELL = new CMediaArray(DIR_UI_GRAPHICS + "ui/combobox_cell.png", TL, TL);
    public static final CMediaArray UI_COMBOBOX_LIST_CELL = new CMediaArray(DIR_UI_GRAPHICS + "ui/combobox_list_cell.png", TL, TL);
    public static final CMediaArray UI_COMBOBOX_LIST_CELL_SELECTED = new CMediaArray(DIR_UI_GRAPHICS + "ui/combobox_list_cell_selected.png", TL, TL);
    public static final CMediaArray UI_TAB_BORDERS = new CMediaArray(DIR_UI_GRAPHICS + "ui/tab_border.png", TL, TL);
    public static final CMediaArray UI_BORDERS = new CMediaArray(DIR_UI_GRAPHICS + "ui/border.png", TL, TL);
    public static final CMediaArray UI_TAB = new CMediaArray(DIR_UI_GRAPHICS + "ui/tab.png", TL, TL);
    public static final CMediaArray UI_TAB_SELECTED = new CMediaArray(DIR_UI_GRAPHICS + "ui/tab_selected.png", TL, TL);
    public static final CMediaImage UI_TAB_BIGICON = new CMediaImage(DIR_UI_GRAPHICS + "ui/tab_bigicon.png");
    public static final CMediaImage UI_TAB_BIGICON_SELECTED = new CMediaImage(DIR_UI_GRAPHICS + "ui/tab__bigicon_selected.png");
    public static final CMediaImage UI_KNOB_BACKGROUND = new CMediaImage(DIR_UI_GRAPHICS + "ui/knob_background.png");
    public static final CMediaArray UI_KNOB = new CMediaArray(DIR_UI_GRAPHICS + "ui/knob.png", TL2, TL2);
    public static final CMediaArray UI_KNOB_ENDLESS = new CMediaArray(DIR_UI_GRAPHICS + "ui/knob_endless.png", TL2, TL2);
    public static final CMediaArray UI_SEPARATOR_HORIZONTAL = new CMediaArray(DIR_UI_GRAPHICS + "ui/separator_horizontal.png", TL, TL);
    public static final CMediaArray UI_SEPARATOR_VERTICAL = new CMediaArray(DIR_UI_GRAPHICS + "ui/separator_vertical.png", TL, TL);
    public static final CMediaArray UI_TOOLTIP_CELL = new CMediaArray(DIR_UI_GRAPHICS + "ui/tooltip_cell.png", TL, TL);
    public static final CMediaArray UI_TOOLTIP = new CMediaArray(DIR_UI_GRAPHICS + "ui/tooltip.png", TL, TL);
    public static final CMediaArray UI_TOOLTIP_TOP = new CMediaArray(DIR_UI_GRAPHICS + "ui/tooltip_top.png", TL, TL);
    public static final CMediaImage UI_TOOLTIP_SEGMENT_BORDER = new CMediaImage(DIR_UI_GRAPHICS + "ui/tooltip_segment_border.png");
    public static final CMediaImage UI_TOOLTIP_LINE_HORIZONTAL = new CMediaImage(DIR_UI_GRAPHICS + "ui/tooltip_line_horizontal.png");
    public static final CMediaImage UI_TOOLTIP_LINE_VERTICAL = new CMediaImage(DIR_UI_GRAPHICS + "ui/tooltip_line_vertical.png");
    public static final CMediaArray UI_CONTEXT_MENU = new CMediaArray(DIR_UI_GRAPHICS + "ui/context_menu.png", TL, TL);
    public static final CMediaArray UI_CONTEXT_MENU_TOP = new CMediaArray(DIR_UI_GRAPHICS + "ui/context_menu_top.png", TL, TL);
    public static final CMediaArray UI_CONTEXT_MENU_CELL = new CMediaArray(DIR_UI_GRAPHICS + "ui/context_menu_cell.png", TL, TL);
    public static final CMediaArray UI_CONTEXT_MENU_CELL_SELECTED = new CMediaArray(DIR_UI_GRAPHICS + "ui/context_menu_cell_selected.png", TL, TL);
    public static final CMediaArray UI_TEXTFIELD = new CMediaArray(DIR_UI_GRAPHICS + "ui/textfield.png", TL, TL);
    public static final CMediaArray UI_TEXTFIELD_CELL_VALIDATION = new CMediaArray(DIR_UI_GRAPHICS + "ui/textfield_cell_validation.png", TL, TL);
    public static final CMediaArray UI_TEXTFIELD_CELL = new CMediaArray(DIR_UI_GRAPHICS + "ui/textfield_cell.png", TL, TL);
    public static final CMediaAnimation UI_TEXTFIELD_CARET = new CMediaAnimation(DIR_UI_GRAPHICS + "ui/textfield_caret.png", 1, TL, 0.4f);
    public static final CMediaArray UI_GRID = new CMediaArray(DIR_UI_GRAPHICS + "ui/grid.png", TL, TL);
    public static final CMediaArray UI_GRID_DRAGGED = new CMediaArray(DIR_UI_GRAPHICS + "ui/grid_dragged.png", TL, TL);
    public static final CMediaArray UI_GRID_CELL = new CMediaArray(DIR_UI_GRAPHICS + "ui/grid_cell.png", TL, TL);
    public static final CMediaArray UI_GRID_CELL_SELECTED = new CMediaArray(DIR_UI_GRAPHICS + "ui/grid_cell_selected.png", TL, TL);
    public static final CMediaArray UI_GRID_X2 = new CMediaArray(DIR_UI_GRAPHICS + "ui/grid_x2.png",  TL2,TL2);
    public static final CMediaArray UI_GRID_DRAGGED_X2 = new CMediaArray(DIR_UI_GRAPHICS + "ui/grid_dragged_x2.png",  TL2,TL2);
    public static final CMediaArray UI_GRID_CELL_X2 = new CMediaArray(DIR_UI_GRAPHICS + "ui/grid_cell_x2.png",  TL2,TL2);
    public static final CMediaArray UI_GRID_CELL_SELECTED_X2 = new CMediaArray(DIR_UI_GRAPHICS + "ui/grid_cell_selected_x2.png", TL2,TL2);
    public static final CMediaArray UI_PROGRESSBAR = new CMediaArray(DIR_UI_GRAPHICS + "ui/progressbar.png", TL, TL);
    public static final CMediaArray UI_PROGRESSBAR_BAR = new CMediaArray(DIR_UI_GRAPHICS + "ui/progressbar_bar.png", TL, TL);
    public static final CMediaImage UI_NOTIFICATION_BAR = new CMediaImage(DIR_UI_GRAPHICS + "ui/notification_bar.png");
    public static final CMediaArray UI_CHECKBOX_CHECKBOX = new CMediaArray(DIR_UI_GRAPHICS + "ui/checkbox.png", TL, TL);
    public static final CMediaImage UI_CHECKBOX_CHECKBOX_CELL = new CMediaImage(DIR_UI_GRAPHICS + "ui/checkbox_cell.png");
    public static final CMediaArray UI_CHECKBOX_RADIO = new CMediaArray(DIR_UI_GRAPHICS + "ui/radio.png", TL, TL);
    public static final CMediaImage UI_CHECKBOX_RADIO_CELL = new CMediaImage(DIR_UI_GRAPHICS + "ui/radio_cell.png");
    public static final CMediaArray UI_MOUSETEXTINPUT_BUTTON = new CMediaArray(DIR_UI_GRAPHICS + "ui/mousetextinput_button.png", 12, 12);
    public static final CMediaArray UI_MOUSETEXTINPUT_CONFIRM = new CMediaArray(DIR_UI_GRAPHICS + "ui/mousetextinput_confirm.png", 12, 12);
    public static final CMediaArray UI_MOUSETEXTINPUT_DELETE = new CMediaArray(DIR_UI_GRAPHICS + "ui/mousetextinput_delete.png", 12, 12);
    public static final CMediaArray UI_MOUSETEXTINPUT_LOWERCASE = new CMediaArray(DIR_UI_GRAPHICS + "ui/mousetextinput_lowercase.png", 12, 12);
    public static final CMediaArray UI_MOUSETEXTINPUT_UPPERCASE = new CMediaArray(DIR_UI_GRAPHICS + "ui/mousetextinput_uppercase.png", 12, 12);
    public static final CMediaImage UI_MOUSETEXTINPUT_SELECTED = new CMediaImage(DIR_UI_GRAPHICS + "ui/mousetextinput_selected.png");

    // Cursors
    public static final CMediaImage UI_CURSOR_ARROW = new CMediaImage(DIR_UI_GRAPHICS + "cursors/arrow.png");

    // Icons
    public static final CMediaImage UI_ICON_CLOSE = new CMediaImage(DIR_UI_GRAPHICS + "icons/close.icon.png");
    public static final CMediaImage UI_ICON_COLOR_PICKER = new CMediaImage(DIR_UI_GRAPHICS + "icons/color_picker.icon.png");
    public static final CMediaImage UI_ICON_INFORMATION = new CMediaImage(DIR_UI_GRAPHICS + "icons/information.icon.png");
    public static final CMediaImage UI_ICON_QUESTION = new CMediaImage(DIR_UI_GRAPHICS + "icons/question.icon.png");
    public static final CMediaImage UI_ICON_EXTEND = new CMediaImage(DIR_UI_GRAPHICS + "icons/extend.icon.png");
    public static final CMediaImage UI_ICON_BACK = new CMediaImage(DIR_UI_GRAPHICS + "icons/back.icon.png");
    public static final CMediaImage UI_ICON_FORWARD = new CMediaImage(DIR_UI_GRAPHICS + "icons/forward.icon.png");
    public static final CMediaImage UI_ICON_KEY_DELETE = new CMediaImage(DIR_UI_GRAPHICS + "icons/key_delete.icon.png");
    public static final CMediaImage UI_ICON_KEY_CASE = new CMediaImage(DIR_UI_GRAPHICS + "icons/key_case.icon.png");


    // Fonts
    public static final CMediaFont UI_FONT = new CMediaFont(DIR_UI_GRAPHICS + "fonts/font.fnt", 0, 5,true);

    // Misc
    public static final CMediaImage UI_PIXEL = new CMediaImage(DIR_UI_GRAPHICS + "misc/pixel.png");
    public static final CMediaImage UI_PIXEL_TRANSPARENT = new CMediaImage(DIR_UI_GRAPHICS + "misc/pixel_transparent.png");
    public static final CMediaImage UI_COLOR_SELECTOR = new CMediaImage(DIR_UI_GRAPHICS + "misc/colors.png");
    public static final CMediaImage UI_COLOR_SELECTOR_OVERLAY = new CMediaImage(DIR_UI_GRAPHICS + "misc/colors_overlay.png");


    public static final CMedia[] ALL = new CMedia[]{
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
            UI_COMBOBOX,
            UI_COMBOBOX_TOP,
            UI_TAB_BORDERS,
            UI_BORDERS,
            UI_TAB,
            UI_TAB_SELECTED,
            UI_TAB_BIGICON,
            UI_TAB_BIGICON_SELECTED,
            UI_COMBOBOX_OPEN,
            UI_COMBOBOX_LIST,
            UI_COMBOBOX_CELL,
            UI_COMBOBOX_LIST_CELL,
            UI_COMBOBOX_LIST_CELL_SELECTED,
            UI_KNOB_BACKGROUND,
            UI_KNOB,
            UI_KNOB_ENDLESS,
            UI_SEPARATOR_HORIZONTAL,
            UI_SEPARATOR_VERTICAL,
            UI_TOOLTIP_CELL,
            UI_TOOLTIP_SEGMENT_BORDER,
            UI_TOOLTIP,
            UI_TOOLTIP_TOP,
            UI_TOOLTIP_LINE_HORIZONTAL,
            UI_TOOLTIP_LINE_VERTICAL,
            UI_CONTEXT_MENU,
            UI_CONTEXT_MENU_TOP,
            UI_CONTEXT_MENU_CELL,
            UI_CONTEXT_MENU_CELL_SELECTED,
            UI_TEXTFIELD,
            UI_TEXTFIELD_CELL_VALIDATION,
            UI_TEXTFIELD_CELL,
            UI_TEXTFIELD_CARET,
            UI_GRID,
            UI_GRID_DRAGGED,
            UI_GRID_CELL,UI_GRID_CELL_SELECTED,
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
            UI_CURSOR_ARROW,
            UI_PIXEL_TRANSPARENT,
            UI_ICON_CLOSE,
            UI_ICON_COLOR_PICKER,
            UI_ICON_INFORMATION,
            UI_ICON_QUESTION,
            UI_ICON_EXTEND,
            UI_ICON_KEY_DELETE,
            UI_ICON_KEY_CASE,
            UI_COLOR_SELECTOR,
            UI_COLOR_SELECTOR_OVERLAY,
            UI_MOUSETEXTINPUT_BUTTON,
            UI_MOUSETEXTINPUT_CONFIRM,
            UI_MOUSETEXTINPUT_UPPERCASE,
            UI_MOUSETEXTINPUT_LOWERCASE,
            UI_MOUSETEXTINPUT_DELETE,
            UI_MOUSETEXTINPUT_SELECTED,
            UI_FONT,
            UI_PIXEL,
            UI_ICON_BACK,
            UI_ICON_FORWARD
    };
}
