package net.mslivo.core.engine.ui_engine.media;

import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.media.*;

/**
 * These Medias need to be loaded on startup for the UI to work no matter what
 */
public class UIEngineBaseMedia_8x8 {
    private static final String DIR_UI_GRAPHICS = MediaManager.DIR_GRAPHICS + "pixelui_8/";
    private static final int TL = 8;

    // UI Elements
    public static final CMediaArray UI_WINDOW = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/window.png", TL, TL);
    public static final CMediaArray UI_BUTTON = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/button.png", TL, TL);
    public static final CMediaArray UI_BUTTON_PRESSED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/button_pressed.png", TL, TL);
    public static final CMediaArray UI_SCROLLBAR_VERTICAL = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/scrollbar_vertical.png", TL, TL);
    public static final CMediaArray UI_SCROLLBAR_HORIZONTAL = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/scrollbar_horizontal.png", TL, TL);
    public static final CMediaArray UI_SCROLLBAR_BUTTON_VERTICAL = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/scrollbar_button_vertical.png", TL, TL);
    public static final CMediaArray UI_SCROLLBAR_BUTTON_HORIZONAL = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/scrollbar_button_horizontal.png", TL, TL);
    public static final CMediaImage UI_LIST = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/list.png");
    public static final CMediaImage UI_LIST_SELECTED = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/list_selected.png");
    public static final CMediaArray UI_LIST_DRAG = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/list_drag.png", TL, TL * 2);
    public static final CMediaArray UI_COMBOBOX = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/combobox.png", TL, TL);
    public static final CMediaArray UI_TAB_BORDERS = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/tab_border.png", TL, TL);
    public static final CMediaArray UI_BORDERS = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/border.png", TL, TL);
    public static final CMediaArray UI_TAB = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/tab.png", TL, TL);
    public static final CMediaArray UI_TAB_SELECTED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/tab_selected.png", TL, TL);
    public static final CMediaImage UI_TAB_BIGICON = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/tab_bigicon.png");
    public static final CMediaImage UI_TAB_BIGICON_SELECTED = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/tab__bigicon_selected.png");
    public static final CMediaArray UI_COMBOBOX_OPEN = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/combobox_open.png", TL, TL);
    public static final CMediaArray UI_COMBOBOX_LIST = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/combobox_list.png", TL, TL);
    public static final CMediaArray UI_COMBOBOX_LIST_SELECTED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/combobox_list_selected.png", TL, TL);
    public static final CMediaImage UI_KNOB_BACKGROUND = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/knob_background.png");
    public static final CMediaArray UI_KNOB = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/knob.png", TL * 2, TL * 2);
    public static final CMediaArray UI_KNOB_ENDLESS = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/knob_endless.png", TL * 2, TL * 2);
    public static final CMediaArray UI_SEPARATOR_HORIZONTAL = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/separator_horizontal.png", TL, TL);
    public static final CMediaArray UI_SEPARATOR_VERTICAL = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/separator_vertical.png", TL, TL);
    public static final CMediaArray UI_TOOLTIP = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/tooltip.png", TL, TL);
    public static final CMediaArray UI_TOOLTIP_BORDER = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/tooltip_border.png", TL, TL);
    public static final CMediaArray UI_TOOLTIP_SEGMENT_BORDER = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/tooltip_segment_border.png", TL, TL);
    public static final CMediaImage UI_TOOLTIP_LINE = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/tooltip_line.png");
    public static final CMediaArray UI_CONTEXT_MENU = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/context_menu.png", TL, TL);
    public static final CMediaArray UI_CONTEXT_MENU_SELECTED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/context_menu_selected.png", TL, TL);
    public static final CMediaArray UI_TEXTFIELD = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/textfield.png", TL, TL);
    public static final CMediaArray UI_TEXTFIELD_VALIDATION_OVERLAY = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/textfield_validation_overlay.png", TL, TL);
    public static final CMediaArray UI_TEXTFIELD_FOCUSED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/textfield_focused.png", TL, TL);
    public static final CMediaAnimation UI_TEXTFIELD_CARET = MediaManager.create_CMediaAnimation(DIR_UI_GRAPHICS + "ui/textfield_caret.png", 1, TL, 0.4f);
    public static final CMediaArray UI_GRID = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/grid.png", TL, TL);
    public static final CMediaArray UI_GRID_DRAGGED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/grid_dragged.png", TL, TL);
    public static final CMediaArray UI_GRID_SELECTED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/grid_selected.png", TL, TL);
    public static final CMediaArray UI_GRID_X2 = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/grid_x2.png", TL * 2, TL * 2);
    public static final CMediaArray UI_GRID_DRAGGED_X2 = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/grid_dragged_x2.png", TL * 2, TL * 2);
    public static final CMediaArray UI_GRID_SELECTED_X2 = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/grid_selected_x2.png", TL * 2, TL * 2);
    public static final CMediaArray UI_PROGRESSBAR = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/progressbar.png", TL, TL);
    public static final CMediaArray UI_PROGRESSBAR_BAR = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/progressbar_bar.png", TL, TL);
    public static final CMediaImage UI_NOTIFICATION_BAR = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/notification_bar.png");
    public static final CMediaArray UI_CHECKBOX_CHECKBOX = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/checkbox.png", TL, TL);
    public static final CMediaArray UI_CHECKBOX_RADIO = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/radio.png", TL, TL);
    public static final CMediaArray UI_OSTEXTINPUT_CHARACTER = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/ostextinput_character.png", 12, 12);
    public static final CMediaArray UI_OSTEXTINPUT_CONFIRM = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/ostextinput_confirm.png", 12, 12);
    public static final CMediaArray UI_OSTEXTINPUT_DELETE = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/ostextinput_delete.png", 12, 12);
    public static final CMediaArray UI_OSTEXTINPUT_LOWERCASE = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/ostextinput_lowercase.png", 12, 12);
    public static final CMediaArray UI_OSTEXTINPUT_UPPERCASE = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ui/ostextinput_uppercase.png", 12, 12);
    public static final CMediaImage UI_OSTEXTINPUT_SELECTED = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/ostextinput_selected.png");
    public static final CMediaImage UI_SHAPE_RECT = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/shape_rect.png");
    public static final CMediaImage UI_SHAPE_DIAMOND = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/shape_diamond.png");
    public static final CMediaImage UI_SHAPE_OVAL = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/shape_oval.png");
    public static final CMediaImage UI_SHAPE_TRIANGLE_LEFT_DOWN = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/shape_right_triangle_ld.png");
    public static final CMediaImage UI_SHAPE_TRIANGLE_RIGHT_DOWN = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/shape_right_triangle_rd.png");
    public static final CMediaImage UI_SHAPE_TRIANGLE_LEFT_UP = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/shape_right_triangle_lu.png");
    public static final CMediaImage UI_SHAPE_TRIANGLE_RIGHT_UP = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ui/shape_right_triangle_ru.png");

    // Cursors
    public static final CMediaImage UI_CURSOR_ARROW = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "cursors/arrow.png");

    // Icons
    public static final CMediaImage UI_ICON_CLOSE = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/close.png");
    public static final CMediaImage UI_ICON_COLOR = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/color.png");
    public static final CMediaImage UI_ICON_INFORMATION = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/information.png");
    public static final CMediaImage UI_ICON_QUESTION = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/question.png");
    public static final CMediaImage UI_ICON_EXTEND = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/extend.png");

    public static final CMediaImage UI_ICON_KEY_DELETE = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/delete.png");
    public static final CMediaImage UI_ICON_KEY_CASE = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/case.png");


    // Fonts
    public static final CMediaFont UI_FONT_BLACK = MediaManager.create_CMediaFont(DIR_UI_GRAPHICS + "fonts/font_black.fnt", 0, 5);
    public static final CMediaFont UI_FONT_WHITE = MediaManager.create_CMediaFont(DIR_UI_GRAPHICS + "fonts/font_white.fnt", 0, 5);

    // Misc
    public static final CMediaImage UI_PIXEL = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "misc/pixel.png");
    public static final CMediaImage UI_PIXEL_TRANSPARENT = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "misc/pixel_transparent.png");
    public static final CMediaImage UI_COLOR_SELECTOR = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "misc/colors.png");
    public static final CMediaImage UI_COLOR_SELECTOR_OVERLAY = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "misc/colors_overlay.png");


    public static final CMedia[] ALL = new CMedia[]{
            UI_WINDOW,
            UI_BUTTON,
            UI_BUTTON_PRESSED,
            UI_SCROLLBAR_VERTICAL,
            UI_SCROLLBAR_HORIZONTAL,
            UI_SCROLLBAR_BUTTON_VERTICAL,
            UI_SCROLLBAR_BUTTON_HORIZONAL,
            UI_LIST,
            UI_LIST_SELECTED,
            UI_LIST_DRAG,
            UI_COMBOBOX,
            UI_TAB_BORDERS,
            UI_BORDERS,
            UI_TAB,
            UI_TAB_SELECTED,
            UI_TAB_BIGICON,
            UI_TAB_BIGICON_SELECTED,
            UI_COMBOBOX_OPEN,
            UI_COMBOBOX_LIST,
            UI_COMBOBOX_LIST_SELECTED,
            UI_KNOB_BACKGROUND,
            UI_KNOB,
            UI_KNOB_ENDLESS,
            UI_SEPARATOR_HORIZONTAL,
            UI_SEPARATOR_VERTICAL,
            UI_TOOLTIP,
            UI_TOOLTIP_SEGMENT_BORDER,
            UI_TOOLTIP_BORDER,
            UI_TOOLTIP_LINE,
            UI_CONTEXT_MENU,
            UI_CONTEXT_MENU_SELECTED,
            UI_TEXTFIELD,
            UI_TEXTFIELD_VALIDATION_OVERLAY,
            UI_TEXTFIELD_FOCUSED,
            UI_TEXTFIELD_CARET,
            UI_GRID,
            UI_GRID_DRAGGED,
            UI_GRID_SELECTED,
            UI_GRID_X2,
            UI_GRID_DRAGGED_X2,
            UI_GRID_SELECTED_X2,
            UI_PROGRESSBAR,
            UI_PROGRESSBAR_BAR,
            UI_NOTIFICATION_BAR,
            UI_CHECKBOX_CHECKBOX,
            UI_CHECKBOX_RADIO,
            UI_CURSOR_ARROW,
            UI_PIXEL_TRANSPARENT,
            UI_ICON_CLOSE,
            UI_ICON_COLOR,
            UI_ICON_INFORMATION,
            UI_ICON_QUESTION,
            UI_ICON_EXTEND,
            UI_ICON_KEY_DELETE,
            UI_ICON_KEY_CASE,
            UI_SHAPE_RECT,
            UI_SHAPE_DIAMOND,
            UI_SHAPE_OVAL,
            UI_SHAPE_TRIANGLE_LEFT_DOWN,
            UI_SHAPE_TRIANGLE_RIGHT_DOWN,
            UI_SHAPE_TRIANGLE_LEFT_UP,
            UI_SHAPE_TRIANGLE_RIGHT_UP,
            UI_COLOR_SELECTOR,
            UI_COLOR_SELECTOR_OVERLAY,
            UI_OSTEXTINPUT_CHARACTER,
            UI_OSTEXTINPUT_CONFIRM,
            UI_OSTEXTINPUT_UPPERCASE,
            UI_OSTEXTINPUT_LOWERCASE,
            UI_OSTEXTINPUT_DELETE,
            UI_OSTEXTINPUT_SELECTED,
            UI_FONT_BLACK,
            UI_FONT_WHITE,
            UI_PIXEL
    };
}
