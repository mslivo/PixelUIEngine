package net.mslivo.example.ui.media;

import net.mslivo.pixelui.media.*;

public class ExampleBaseMedia {
    private static final String DIR_EXAMPLE_GRAPHICS = MediaManager.DIR_GRAPHICS + "example/";
    public static final CMediaArray BUTTON_ANIM_EXAMPLE_ARRAY = new CMediaArray(DIR_EXAMPLE_GRAPHICS + "example_array.png", 16, 8);
    public static final CMediaImage ICON_EXAMPLE_1 = new CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_1.png");
    public static final CMediaImage ICON_EXAMPLE_2 = new CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_2.png");
    public static final CMediaImage ICON_EXAMPLE_3 = new CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_3.png");
    public static final CMediaImage ICON_EXAMPLE_4 = new CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_4.png");
    public static final CMediaImage ICON_EXAMPLE_DOUBLE = new CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_double.png");
    public static final CMediaImage ICON_EXAMPLE_BULLET_GREEN = new CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_bullet_green.png");
    public static final CMediaImage ICON_EXAMPLE_BULLET_BLUE = new CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_bullet_blue.png");
    public static final CMediaImage ICON_EXAMPLE_BULLET_ORANGE = new CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_bullet_orange.png");
    public static final CMediaImage ICON_EXAMPLE_WINDOW = new CMediaImage(DIR_EXAMPLE_GRAPHICS + "example_icon_window.png");
    public static final CMediaAnimation EXAMPLE_ANIMATION_2 = new CMediaAnimation(DIR_EXAMPLE_GRAPHICS + "example_animation_2.png",  8*8,  8*8, 0.1f);
    public static final CMediaAnimation EXAMPLE_ANIMATION_3 = new CMediaAnimation(DIR_EXAMPLE_GRAPHICS + "example_animation_2.png",  8*8,  8*8, 0.01f);
    public static final CMediaImage EXAMPLE_TEST = new CMediaImage(DIR_EXAMPLE_GRAPHICS + "test.png", false);

    public static final CMediaAnimation EXAMPLE_ANIMATION = new CMediaAnimation(DIR_EXAMPLE_GRAPHICS + "example_animation.png", 16, 8, 0.1f);
    public static final CMediaAnimation BACKGROUND = new CMediaAnimation(DIR_EXAMPLE_GRAPHICS + "background.png", 16, 16, 0.2f);
    public static final CMedia[] ALL = new CMedia[]{
            EXAMPLE_ANIMATION,EXAMPLE_ANIMATION_3,EXAMPLE_TEST,
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
