package net.mslivo.example.ui.media;

import net.mslivo.core.engine.media_manager.*;

public class ExampleBaseMedia {
    private static final String DIR_EXAMPLE_GRAPHICS = MediaManager.DIR_GRAPHICS + "example/";
    public static final CMediaArray BUTTON_ANIM_EXAMPLE_ARRAY = MediaManager.create_CMediaArray(DIR_EXAMPLE_GRAPHICS + "example_array.png", 16, 8);
    public static final CMediaImage ICON_EXAMPLE_1 = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_1.png");
    public static final CMediaImage ICON_EXAMPLE_2 = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_2.png");
    public static final CMediaImage ICON_EXAMPLE_3 = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_3.png");
    public static final CMediaImage ICON_EXAMPLE_4 = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_4.png");
    public static final CMediaImage ICON_EXAMPLE_DOUBLE = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_double.png");
    public static final CMediaImage ICON_EXAMPLE_BULLET_GREEN = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_bullet_green.png");
    public static final CMediaImage ICON_EXAMPLE_BULLET_BLUE = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_bullet_blue.png");
    public static final CMediaImage ICON_EXAMPLE_BULLET_ORANGE = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_bullet_orange.png");
    public static final CMediaImage ICON_EXAMPLE_WINDOW = MediaManager.create_CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_window.png");
    public static final CMediaAnimation EXAMPLE_ANIMATION_2 = MediaManager.create_CMediaAnimation(DIR_EXAMPLE_GRAPHICS + "example_animation_2.png",  8*8,  8*8, 0.1f);
    public static final CMediaAnimation EXAMPLE_ANIMATION = MediaManager.create_CMediaAnimation(DIR_EXAMPLE_GRAPHICS + "example_animation.png", 16, 8, 0.1f);
    public static final CMediaAnimation BACKGROUND = MediaManager.create_CMediaAnimation(DIR_EXAMPLE_GRAPHICS + "background.png", 16, 16, 0.2f);
    public static final CMedia[] ALL = new CMedia[]{
            EXAMPLE_ANIMATION,
            BUTTON_ANIM_EXAMPLE_ARRAY,
            ICON_EXAMPLE_1,
            ICON_EXAMPLE_2,
            ICON_EXAMPLE_3,
            ICON_EXAMPLE_4,
            ICON_EXAMPLE_DOUBLE,
            ICON_EXAMPLE_BULLET_GREEN,
            ICON_EXAMPLE_BULLET_BLUE,
            ICON_EXAMPLE_BULLET_ORANGE,
            ICON_EXAMPLE_WINDOW,
            EXAMPLE_ANIMATION_2,
            BACKGROUND
    };

}
