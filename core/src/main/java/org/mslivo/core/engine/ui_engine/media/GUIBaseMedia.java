package org.mslivo.core.engine.ui_engine.media;

import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.*;
import org.mslivo.core.engine.ui_engine.UIEngine;

/**
 * These Medias get loaded on startup no matter what
 */
public class GUIBaseMedia {

    private static final String guiDirectory = "gui/";

    // Graphics
    public static final CMediaArray GUI_WINDOW = MediaManager.create_CMediaArray(guiDirectory + "window.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_BUTTON = MediaManager.create_CMediaArray(guiDirectory + "button.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_BUTTON_PRESSED = MediaManager.create_CMediaArray(guiDirectory + "button_pressed.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_SCROLLBAR_VERTICAL = MediaManager.create_CMediaArray(guiDirectory + "scrollbar_vertical.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_SCROLLBAR_HORIZONTAL = MediaManager.create_CMediaArray(guiDirectory + "scrollbar_horizontal.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_SCROLLBAR_BUTTON_VERTICAL = MediaManager.create_CMediaArray(guiDirectory + "scrollbar_button_vertical.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_SCROLLBAR_BUTTON_HORIZONAL = MediaManager.create_CMediaArray(guiDirectory + "scrollbar_button_horizontal.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaImage GUI_LIST = MediaManager.create_CMediaImage(guiDirectory + "list.png");
    public static final CMediaImage GUI_LIST_SELECTED = MediaManager.create_CMediaImage(guiDirectory + "list_selected.png");
    public static final CMediaArray GUI_LIST_DRAG = MediaManager.create_CMediaArray(guiDirectory + "list_drag.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray GUI_COMBOBOX = MediaManager.create_CMediaArray(guiDirectory + "combobox.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_TAB_BORDERS = MediaManager.create_CMediaArray(guiDirectory + "tab_border.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_BORDERS = MediaManager.create_CMediaArray(guiDirectory + "border.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_TAB = MediaManager.create_CMediaArray(guiDirectory + "tab.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_TAB_SELECTED = MediaManager.create_CMediaArray(guiDirectory + "tab_selected.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaImage GUI_TAB_BIGICON = MediaManager.create_CMediaImage(guiDirectory + "tab_bigicon.png");
    public static final CMediaImage GUI_TAB_BIGICON_SELECTED = MediaManager.create_CMediaImage(guiDirectory + "tab__bigicon_selected.png");
    public static final CMediaArray GUI_COMBOBOX_OPEN = MediaManager.create_CMediaArray(guiDirectory + "combobox_open.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_COMBOBOX_LIST = MediaManager.create_CMediaArray(guiDirectory + "combobox_list.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_COMBOBOX_LIST_SELECTED = MediaManager.create_CMediaArray(guiDirectory + "combobox_list_selected.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_KNOB_BACKGROUND = MediaManager.create_CMediaArray(guiDirectory + "knob_background.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray GUI_KNOB = MediaManager.create_CMediaArray(guiDirectory + "knob.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray GUI_KNOB_ENDLESS = MediaManager.create_CMediaArray(guiDirectory + "knob_endless.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray GUI_SEPARATOR_HORIZONTAL = MediaManager.create_CMediaArray(guiDirectory + "separator_horizontal.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_SEPARATOR_VERTICAL = MediaManager.create_CMediaArray(guiDirectory + "separator_vertical.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_TOOLTIP = MediaManager.create_CMediaArray(guiDirectory + "tooltip.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaImage GUI_TOOLTIP_LINE_X = MediaManager.create_CMediaImage(guiDirectory + "tooltip_line_x.png");
    public static final CMediaImage GUI_TOOLTIP_LINE_Y = MediaManager.create_CMediaImage(guiDirectory + "tooltip_line_y.png");
    public static final CMediaArray GUI_TOOLTIP_TITLE = MediaManager.create_CMediaArray(guiDirectory + "tooltip_title.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_CONTEXT_MENU = MediaManager.create_CMediaArray(guiDirectory + "context_menu.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_CONTEXT_MENU_SELECTED = MediaManager.create_CMediaArray(guiDirectory + "context_menu_selected.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_TEXTFIELD = MediaManager.create_CMediaArray(guiDirectory + "textfield.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_TEXTFIELD_VALIDATION_OVERLAY = MediaManager.create_CMediaArray(guiDirectory + "textfield_validation_overlay.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_TEXTFIELD_FOCUSED = MediaManager.create_CMediaArray(guiDirectory + "textfield_focused.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaAnimation GUI_TEXTFIELD_CARET = MediaManager.create_CMediaAnimation(guiDirectory + "textfield_caret.png", 1, UIEngine.TILE_SIZE, 0.4f);
    public static final CMediaArray GUI_INVENTORY = MediaManager.create_CMediaArray(guiDirectory + "inventory.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_INVENTORY_DRAGGED = MediaManager.create_CMediaArray(guiDirectory + "inventory_dragged.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_INVENTORY_SELECTED = MediaManager.create_CMediaArray(guiDirectory + "inventory_selected.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_INVENTORY_X2 = MediaManager.create_CMediaArray(guiDirectory + "inventory_x2.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray GUI_INVENTORY_DRAGGED_X2 = MediaManager.create_CMediaArray(guiDirectory + "inventory_dragged_x2.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray GUI_INVENTORY_SELECTED_X2 = MediaManager.create_CMediaArray(guiDirectory + "inventory_selected_x2.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE * 2);
    public static final CMediaArray GUI_PROGRESSBAR = MediaManager.create_CMediaArray(guiDirectory + "progressbar.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_PROGRESSBAR_BAR = MediaManager.create_CMediaArray(guiDirectory + "progressbar_bar.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);

    public static final CMediaImage GUI_NOTIFICATION_BAR = MediaManager.create_CMediaImage(guiDirectory + "notification_bar.png");
    public static final CMediaArray GUI_CHECKBOX_CHECKBOX = MediaManager.create_CMediaArray(guiDirectory + "checkbox.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaArray GUI_CHECKBOX_RADIO = MediaManager.create_CMediaArray(guiDirectory + "radio.png", UIEngine.TILE_SIZE, UIEngine.TILE_SIZE);
    public static final CMediaImage GUI_OSTEXTINPUT_CHARACTER = MediaManager.create_CMediaImage(guiDirectory + "ostextinput_character.png");
    public static final CMediaImage GUI_OSTEXTINPUT_CONFIRM = MediaManager.create_CMediaImage(guiDirectory + "ostextinput_confirm.png");
    public static final CMediaImage GUI_OSTEXTINPUT_DELETE = MediaManager.create_CMediaImage(guiDirectory + "ostextinput_delete.png");
    public static final CMediaArray GUI_OSTEXTINPUT_CASE = MediaManager.create_CMediaArray(guiDirectory + "ostextinput_case.png",12,12);
    public static final CMediaImage GUI_OSTEXTINPUT_SELECTED = MediaManager.create_CMediaImage(guiDirectory + "ostextinput_selected.png");
    //Cursors
    public static final CMediaCursor GUI_CURSOR_ARROW = MediaManager.create_CMediaCursor("gui/cursors/arrow.png", 16, 16);
    public static final CMediaCursor GUI_CURSOR_TRANSPARENT = MediaManager.create_CMediaCursor("gui/cursors/transparent.png", 16, 16);
    // Icons
    public static final CMediaImage GUI_ICON_CLOSE = MediaManager.create_CMediaImage(guiDirectory + "icons/close.png");
    public static final CMediaImage GUI_ICON_COLOR = MediaManager.create_CMediaImage(guiDirectory + "icons/color.png");
    public static final CMediaImage GUI_ICON_INFORMATION = MediaManager.create_CMediaImage(guiDirectory + "icons/information.png");
    public static final CMediaImage GUI_ICON_QUESTION = MediaManager.create_CMediaImage(guiDirectory + "icons/question.png");
    public static final CMediaImage GUI_ICON_EXTEND = MediaManager.create_CMediaImage(guiDirectory + "icons/extend.png");
    // Shapes
    public static final CMediaImage GUI_SHAPE_RECT = MediaManager.create_CMediaImage(guiDirectory + "shape_rect.png");
    public static final CMediaImage GUI_SHAPE_DIAMOND = MediaManager.create_CMediaImage(guiDirectory + "shape_diamond.png");
    public static final CMediaImage GUI_SHAPE_OVAL = MediaManager.create_CMediaImage(guiDirectory + "shape_oval.png");
    public static final CMediaImage GUI_SHAPE_TRIANGLE_LEFT_DOWN = MediaManager.create_CMediaImage(guiDirectory + "shape_right_triangle_ld.png");
    public static final CMediaImage GUI_SHAPE_TRIANGLE_RIGHT_DOWN = MediaManager.create_CMediaImage(guiDirectory + "shape_right_triangle_rd.png");
    public static final CMediaImage GUI_SHAPE_TRIANGLE_LEFT_UP = MediaManager.create_CMediaImage(guiDirectory + "shape_right_triangle_lu.png");
    public static final CMediaImage GUI_SHAPE_TRIANGLE_RIGHT_UP = MediaManager.create_CMediaImage(guiDirectory + "shape_right_triangle_ru.png");
    public static final CMediaImage GUI_COLOR_SELECTOR = MediaManager.create_CMediaImage(guiDirectory + "colors.png");
    public static final CMediaImage GUI_COLOR_SELECTOR_OVERLAY = MediaManager.create_CMediaImage(guiDirectory + "colors_overlay.png");

    // Fonts
    public static final CMediaFont FONT_BLACK = MediaManager.create_CMediaFont(guiDirectory + "fonts/font_black.fnt", 0, 5);
    public static final CMediaFont FONT_WHITE = MediaManager.create_CMediaFont(guiDirectory + "fonts/font_white.fnt", 0, 5);


    public static final CMedia[] ALL = new CMedia[]{
            GUI_WINDOW,
            GUI_BUTTON,
            GUI_BUTTON_PRESSED,
            GUI_SCROLLBAR_VERTICAL,
            GUI_SCROLLBAR_HORIZONTAL,
            GUI_SCROLLBAR_BUTTON_VERTICAL,
            GUI_SCROLLBAR_BUTTON_HORIZONAL,
            GUI_LIST,
            GUI_LIST_SELECTED,
            GUI_LIST_DRAG,
            GUI_COMBOBOX,
            GUI_TAB_BORDERS,
            GUI_BORDERS,
            GUI_TAB,
            GUI_TAB_SELECTED,
            GUI_TAB_BIGICON,
            GUI_TAB_BIGICON_SELECTED,
            GUI_COMBOBOX_OPEN,
            GUI_COMBOBOX_LIST,
            GUI_COMBOBOX_LIST_SELECTED,
            GUI_KNOB_BACKGROUND,
            GUI_KNOB,
            GUI_KNOB_ENDLESS,
            GUI_SEPARATOR_HORIZONTAL,
            GUI_SEPARATOR_VERTICAL,
            GUI_TOOLTIP,
            GUI_TOOLTIP_LINE_X,
            GUI_TOOLTIP_LINE_Y,
            GUI_TOOLTIP_TITLE,
            GUI_CONTEXT_MENU,
            GUI_CONTEXT_MENU_SELECTED,
            GUI_TEXTFIELD,
            GUI_TEXTFIELD_VALIDATION_OVERLAY,
            GUI_TEXTFIELD_FOCUSED,
            GUI_TEXTFIELD_CARET,
            GUI_INVENTORY,
            GUI_INVENTORY_DRAGGED,
            GUI_INVENTORY_SELECTED,
            GUI_INVENTORY_X2,
            GUI_INVENTORY_DRAGGED_X2,
            GUI_INVENTORY_SELECTED_X2,
            GUI_PROGRESSBAR,
            GUI_PROGRESSBAR_BAR,
            GUI_NOTIFICATION_BAR,
            GUI_CHECKBOX_CHECKBOX,
            GUI_CHECKBOX_RADIO,
            GUI_CURSOR_ARROW,
            GUI_CURSOR_TRANSPARENT,
            GUI_ICON_CLOSE,
            GUI_ICON_COLOR,
            GUI_ICON_INFORMATION,
            GUI_ICON_QUESTION,
            GUI_ICON_EXTEND,
            GUI_SHAPE_RECT,
            GUI_SHAPE_DIAMOND,
            GUI_SHAPE_OVAL,
            GUI_SHAPE_TRIANGLE_LEFT_DOWN,
            GUI_SHAPE_TRIANGLE_RIGHT_DOWN,
            GUI_SHAPE_TRIANGLE_LEFT_UP,
            GUI_SHAPE_TRIANGLE_RIGHT_UP,
            GUI_COLOR_SELECTOR,
            GUI_COLOR_SELECTOR_OVERLAY,
            GUI_OSTEXTINPUT_CHARACTER,
            GUI_OSTEXTINPUT_CONFIRM,
            GUI_OSTEXTINPUT_CASE,
            GUI_OSTEXTINPUT_DELETE,
            GUI_OSTEXTINPUT_SELECTED,
            FONT_BLACK,
            FONT_WHITE,
    };
}
