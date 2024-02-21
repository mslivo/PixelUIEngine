package org.mslivo.core.engine.ui_engine;

import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.*;

/**
 * These Medias need to be loaded on startup for the UI to work no matter what
 */
public class UIBaseMedia {
    private static final String DIR_UI_GRAPHICS = MediaManager.DIR_GRAPHICS+"ui/";

    // Graphics
    public static final CMediaArray UI_WINDOW = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "window.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_BUTTON = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "button.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_BUTTON_PRESSED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "button_pressed.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_SCROLLBAR_VERTICAL = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "scrollbar_vertical.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_SCROLLBAR_HORIZONTAL = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "scrollbar_horizontal.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_SCROLLBAR_BUTTON_VERTICAL = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "scrollbar_button_vertical.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_SCROLLBAR_BUTTON_HORIZONAL = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "scrollbar_button_horizontal.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaImage UI_LIST = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "list.png");
    public static final CMediaImage UI_LIST_SELECTED = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "list_selected.png");
    public static final CMediaArray UI_LIST_DRAG = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "list_drag.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray UI_COMBOBOX = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "combobox.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_TAB_BORDERS = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "tab_border.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_BORDERS = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "border.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_TAB = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "tab.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_TAB_SELECTED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "tab_selected.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaImage UI_TAB_BIGICON = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "tab_bigicon.png");
    public static final CMediaImage UI_TAB_BIGICON_SELECTED = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "tab__bigicon_selected.png");
    public static final CMediaArray UI_COMBOBOX_OPEN = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "combobox_open.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_COMBOBOX_LIST = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "combobox_list.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_COMBOBOX_LIST_SELECTED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "combobox_list_selected.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_KNOB_BACKGROUND = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "knob_background.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray UI_KNOB = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "knob.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray UI_KNOB_ENDLESS = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "knob_endless.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray UI_SEPARATOR_HORIZONTAL = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "separator_horizontal.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_SEPARATOR_VERTICAL = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "separator_vertical.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_TOOLTIP = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "tooltip.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaImage UI_TOOLTIP_LINE_X = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "tooltip_line_x.png");
    public static final CMediaImage UI_TOOLTIP_LINE_Y = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "tooltip_line_y.png");
    public static final CMediaArray UI_TOOLTIP_TITLE = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "tooltip_title.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_CONTEXT_MENU = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "context_menu.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_CONTEXT_MENU_SELECTED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "context_menu_selected.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_TEXTFIELD = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "textfield.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_TEXTFIELD_VALIDATION_OVERLAY = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "textfield_validation_overlay.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_TEXTFIELD_FOCUSED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "textfield_focused.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaAnimation UI_TEXTFIELD_CARET = MediaManager.create_CMediaAnimation(DIR_UI_GRAPHICS + "textfield_caret.png", 1, UIEngine.TILE_SIZE, 0.4f);
    public static final CMediaArray UI_INVENTORY = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "inventory.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_INVENTORY_DRAGGED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "inventory_dragged.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_INVENTORY_SELECTED = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "inventory_selected.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_INVENTORY_X2 = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "inventory_x2.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray UI_INVENTORY_DRAGGED_X2 = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "inventory_dragged_x2.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray UI_INVENTORY_SELECTED_X2 = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "inventory_selected_x2.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray UI_PROGRESSBAR = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "progressbar.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_PROGRESSBAR_BAR = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "progressbar_bar.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaImage UI_NOTIFICATION_BAR = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "notification_bar.png");
    public static final CMediaArray UI_CHECKBOX_CHECKBOX = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "checkbox.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_CHECKBOX_RADIO = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "radio.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray UI_OSTEXTINPUT_CHARACTER = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ostextinput_character.png",12,12);
    public static final CMediaArray UI_OSTEXTINPUT_CONFIRM = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ostextinput_confirm.png",12,12);
    public static final CMediaArray UI_OSTEXTINPUT_DELETE = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ostextinput_delete.png",12,12);
    public static final CMediaArray UI_OSTEXTINPUT_LOWERCASE = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ostextinput_lowercase.png",12,12);
    public static final CMediaArray UI_OSTEXTINPUT_UPPERCASE = MediaManager.create_CMediaArray(DIR_UI_GRAPHICS + "ostextinput_uppercase.png",12,12);
    public static final CMediaImage UI_OSTEXTINPUT_SELECTED = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "ostextinput_selected.png");

    //Cursors
    public static final CMediaCursor UI_CURSOR_ARROW = MediaManager.create_CMediaCursor(DIR_UI_GRAPHICS +"cursors/arrow.png", 16, 16);
    public static final CMediaCursor UI_CURSOR_TRANSPARENT = MediaManager.create_CMediaCursor(DIR_UI_GRAPHICS +"cursors/transparent.png", 16, 16);

    // Icons
    public static final CMediaImage UI_ICON_CLOSE = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/close.png");
    public static final CMediaImage UI_ICON_COLOR = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/color.png");
    public static final CMediaImage UI_ICON_INFORMATION = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/information.png");
    public static final CMediaImage UI_ICON_QUESTION = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/question.png");
    public static final CMediaImage UI_ICON_EXTEND = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/extend.png");

    public static final CMediaImage UI_ICON_KEY_DELETE = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/delete.png");
    public static final CMediaImage UI_ICON_KEY_CASE = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "icons/case.png");

    // Shapes
    public static final CMediaImage UI_SHAPE_RECT = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "shape_rect.png");
    public static final CMediaImage UI_SHAPE_DIAMOND = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "shape_diamond.png");
    public static final CMediaImage UI_SHAPE_OVAL = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "shape_oval.png");
    public static final CMediaImage UI_SHAPE_TRIANGLE_LEFT_DOWN = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "shape_right_triangle_ld.png");
    public static final CMediaImage UI_SHAPE_TRIANGLE_RIGHT_DOWN = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "shape_right_triangle_rd.png");
    public static final CMediaImage UI_SHAPE_TRIANGLE_LEFT_UP = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "shape_right_triangle_lu.png");
    public static final CMediaImage UI_SHAPE_TRIANGLE_RIGHT_UP = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "shape_right_triangle_ru.png");
    public static final CMediaImage UI_COLOR_SELECTOR = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "colors.png");
    public static final CMediaImage UI_COLOR_SELECTOR_OVERLAY = MediaManager.create_CMediaImage(DIR_UI_GRAPHICS + "colors_overlay.png");

    // Fonts
    public static final CMediaFont UI_FONT_BLACK = MediaManager.create_CMediaFont(DIR_UI_GRAPHICS + "fonts/font_black.fnt", 0, 5);
    public static final CMediaFont UI_FONT_WHITE = MediaManager.create_CMediaFont(DIR_UI_GRAPHICS + "fonts/font_white.fnt", 0, 5);


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
            UI_TOOLTIP_LINE_X,
            UI_TOOLTIP_LINE_Y,
            UI_TOOLTIP_TITLE,
            UI_CONTEXT_MENU,
            UI_CONTEXT_MENU_SELECTED,
            UI_TEXTFIELD,
            UI_TEXTFIELD_VALIDATION_OVERLAY,
            UI_TEXTFIELD_FOCUSED,
            UI_TEXTFIELD_CARET,
            UI_INVENTORY,
            UI_INVENTORY_DRAGGED,
            UI_INVENTORY_SELECTED,
            UI_INVENTORY_X2,
            UI_INVENTORY_DRAGGED_X2,
            UI_INVENTORY_SELECTED_X2,
            UI_PROGRESSBAR,
            UI_PROGRESSBAR_BAR,
            UI_NOTIFICATION_BAR,
            UI_CHECKBOX_CHECKBOX,
            UI_CHECKBOX_RADIO,
            UI_CURSOR_ARROW,
            UI_CURSOR_TRANSPARENT,
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
    };
}
