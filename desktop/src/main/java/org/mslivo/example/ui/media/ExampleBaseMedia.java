package org.mslivo.example.ui.media;

import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.CMedia;
import org.mslivo.core.engine.media_manager.media.CMediaAnimation;
import org.mslivo.core.engine.media_manager.media.CMediaArray;
import org.mslivo.core.engine.media_manager.media.CMediaImage;
import org.mslivo.core.engine.ui_engine.UIEngine;

public class ExampleBaseMedia {
    private static final String DIR_EXAMPLE_GRAPHICS = MediaManager.DIR_GRAPHICS+ "example/";
    public static final CMediaAnimation GUI_ICON_BUTTON_ANIM_EXAMPLE = MediaManager.create_CMediaAnimation(DIR_EXAMPLE_GRAPHICS + "example_animation.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE, 0.1f);
    public static final CMediaArray GUI_ICON_BUTTON_ANIM_EXAMPLE_ARRAY = MediaManager.create_CMediaArray(DIR_EXAMPLE_GRAPHICS + "example_array.png", UIEngine.TILE_SIZE * 2, UIEngine.TILE_SIZE);
    public static final CMediaImage GUI_ICON_EXAMPLE_1 = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_1.png");
    public static final CMediaImage GUI_ICON_EXAMPLE_2 = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_2.png");
    public static final CMediaImage GUI_ICON_EXAMPLE_3 = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_3.png");
    public static final CMediaImage GUI_ICON_EXAMPLE_4 = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_4.png");
    public static final CMediaImage GUI_ICON_EXAMPLE_DOUBLE = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_double.png");
    public static final CMediaImage GUI_ICON_EXAMPLE_BULLET_GREEN = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_bullet_green.png");
    public static final CMediaImage GUI_ICON_EXAMPLE_BULLET_BLUE = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_bullet_blue.png");
    public static final CMediaImage GUI_ICON_EXAMPLE_BULLET_ORANGE = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_bullet_orange.png");
    public static final CMediaImage GUI_ICON_EXAMPLE_WINDOW = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_window.png");
    public static final CMediaAnimation GUI_ICON_EXAMPLE_ANIMATION_2 = MediaManager.create_CMediaAnimation(DIR_EXAMPLE_GRAPHICS + "example_animation_2.png", UIEngine.TILE_SIZE * 8, UIEngine.TILE_SIZE * 8, 0.1f);
    public static final CMediaAnimation GUI_BACKGROUND = MediaManager.create_CMediaAnimation(DIR_EXAMPLE_GRAPHICS + "background.png", 32, 32, 0.2f);

    public static CMedia[] ALL = new CMedia[]{
            GUI_ICON_BUTTON_ANIM_EXAMPLE,
            GUI_ICON_BUTTON_ANIM_EXAMPLE_ARRAY,
            GUI_ICON_EXAMPLE_1,
            GUI_ICON_EXAMPLE_2,
            GUI_ICON_EXAMPLE_3,
            GUI_ICON_EXAMPLE_4,
            GUI_ICON_EXAMPLE_DOUBLE,
            GUI_ICON_EXAMPLE_BULLET_GREEN,
            GUI_ICON_EXAMPLE_BULLET_BLUE,
            GUI_ICON_EXAMPLE_BULLET_ORANGE,
            GUI_ICON_EXAMPLE_WINDOW,
            GUI_ICON_EXAMPLE_ANIMATION_2,
            GUI_BACKGROUND

    };

}
