package net.mslivo.example.ui.windows;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import net.mslivo.pixelui.engine.*;
import net.mslivo.pixelui.engine.actions.*;
import net.mslivo.pixelui.engine.constants.*;
import net.mslivo.pixelui.media.CMediaArray;
import net.mslivo.pixelui.media.CMediaImage;
import net.mslivo.pixelui.media.CMediaSprite;
import net.mslivo.pixelui.media.MediaManager;
import net.mslivo.pixelui.utils.Tools;
import net.mslivo.pixelui.rendering.NestedFrameBuffer;
import net.mslivo.pixelui.rendering.SpriteRenderer;
import net.mslivo.example.ui.media.ExampleBaseMedia;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ExampleWindowGeneratorP implements WindowGenerator.P1<MediaManager> {

    private MediaManager mediaManager;

    @Override
    public Window createWindow(API api, MediaManager mediaManager) {
        this.mediaManager = mediaManager;

        /* Window */
        boolean aot = Tools.Calc.randomChance();
        Window window = api.window.create(0, 0, 40, 18, "Example-Window " + (aot ? "AOT" : ""), new WindowAction() {
            @Override
            public CMediaSprite icon() {
                return ExampleBaseMedia.ICON_EXAMPLE_WINDOW;
            }
        });

        if (aot)
            api.window.setAlwaysOnTop(window, aot);

        api.window.addComponent(window, api.widgets.button.createWindowCloseButton(window));
        //api.windows.setPosition(window,MathUtils.random(0,inputState.internal_resolution_w-window.width*16),MathUtils.random(0,inputState.internal_resolution_h-window.height*16));
        api.window.center(window);

        /* List / Inventory */
        Array<Component> components_tab1 = createTab1(api, window);
        /* Button, Text, Image */
        Array<Component> components_tab2 = createTab2(api, window);
        /* Slider */
        Array<Component> components_tab3 = createTab3(api, window);
        /* Font */
        Array<Component> components_tab4 = createTab4(api, window);

        Array<Tab> tabs = new Array<>();
        tabs.add(api.component.tabbar.tab.create("Tab I", components_tab1.toArray(Component[]::new), new TabAction() {
            @Override
            public CMediaSprite icon() {
                return ExampleBaseMedia.ICON_EXAMPLE_BULLET_BLUE;
            }
        }));

        Tab tab2 = api.component.tabbar.tab.create("Tab II", components_tab2.toArray(Component[]::new), new TabAction() {
            @Override
            public CMediaSprite icon() {
                return ExampleBaseMedia.ICON_EXAMPLE_BULLET_BLUE;
            }
        });
        tabs.add(tab2);

        api.component.tabbar.tab.setDisabled(tab2, true);

        tabs.add(api.component.tabbar.tab.create("Tab III", components_tab3.toArray(Component[]::new), new TabAction() {
            @Override
            public CMediaSprite icon() {
                return ExampleBaseMedia.ICON_EXAMPLE_BULLET_BLUE;
            }
        }));
        tabs.add(api.component.tabbar.tab.create("Font", components_tab4.toArray(Component[]::new), new TabAction() {
            @Override
            public CMediaSprite icon() {
                return ExampleBaseMedia.ICON_EXAMPLE_BULLET_BLUE;
            }
        }));


        Array<Component> tabBarComponents = api.widgets.tabBar.createExtendableTabBar(1, window.height - 3, window.width - 2, tabs.toArray(Tab[]::new),
                0, null, true, window.height - 4, false);

        api.window.addComponents(window, tabBarComponents.toArray(Component[]::new));

        return window;
    }


    private Array<Component> createTab4(API api, Window window) {

        String[] text = {
                "[#69357eff]You: whats up[]",
                "[#863a9cff]Carp: It's just another day in the pond.[]",
                "[#69357eff]You: how come?[]",
                "[#863a9cff]Carp: Im just enjoying the warm sunshine.[]"
        };


        Array<Component> textComponent = api.widgets.text.createScrollAbleText(1,8,10,6,text);




        Text text1 = api.component.text.create(1, 5, 0, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        Text text2 = api.component.text.create(1, 4, 0, "abcdefghijklmnopqrstuvwxyz");
        Text text3 = api.component.text.create(1, 3, 0, "0123456789");
        Text text4 = api.component.text.create(1, 2, 0, "!\"#$%&'()*+,-./A:A;A<=>?@A[\\]A^_A`A{A|A}A~A£¥¦§\"©'·×÷€");
        Text text5 = api.component.text.create(1, 1, 0, "A.A");


        Array<Component> components = new Array<>();
        components.addAll(text1, text2, text3, text4, text5);
        components.addAll(textComponent);
        api.window.addComponents(window, components.toArray(Component[]::new));
        return components;
    }

    private Array<Component> createTab3(API api, Window window) {

        ScrollbarVertical scrollBarVertical = api.component.scrollbar.scrollbarVertical.create(2, 2, 12, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                window.color.a = scrolled;
            }
        }, window.color.a);

        Knob knob = api.component.knob.create(4, 2, null, false, 1f);
        //api.component.setColor1And2(knob, api.config.component.getDefaultColor(), Color.BLACK);

        KnobAction knobAction = new KnobAction() {
            @Override
            public void onTurned(float turned, float amount) {
                api.component.setAlpha(knob, 0.2f + (turned * 0.8f));
            }
        };

        api.component.knob.setKnobAction(knob, knobAction);
        api.component.setColor(knob, Color.YELLOW);
        api.component.setColor2(knob, Color.BLUE);

        Knob knobe = api.component.knob.create(4, 5, null, true);

        // Shape
        Shape oval = api.component.shape.create(11, 3, 4, 4, SHAPE_TYPE.OVAL);
        api.component.setDisabled(oval, false);
        api.component.setColor(oval, Color.GREEN);

        Shape rect = api.component.shape.create(11, 8, 2, 2, SHAPE_TYPE.RECT);
        api.component.setColor(rect, Color.YELLOW);

        Shape triangle = api.component.shape.create(14, 8, 2, 2, SHAPE_TYPE.DIAMOND, SHAPE_ROTATION.DEGREE_0);
        api.component.setColor(triangle, Color.BLUE);

        ScrollbarHorizontal scrollBarHorizontalR = api.component.scrollbar.scrollbarHorizontal.create(4, 8, 6, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                oval.color.r = scrolled;
            }
        }, oval.color.r);
        api.component.setColor(scrollBarHorizontalR, new Color(1f, 0f, 0f, 1f));
        api.component.setColor2(scrollBarHorizontalR, new Color(0.8f, 0f, 0f, 1f));
        ScrollbarHorizontal scrollBarHorizontalG = api.component.scrollbar.scrollbarHorizontal.create(4, 10, 6, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                oval.color.g = scrolled;
            }
        }, oval.color.g);
        api.component.setColor(scrollBarHorizontalG, new Color(0f, 1f, 0f, 1f));
        api.component.setColor2(scrollBarHorizontalG, new Color(0f, 0.8f, 0f, 1f));
        ScrollbarHorizontal scrollBarHorizontalB = api.component.scrollbar.scrollbarHorizontal.create(4, 12, 6, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                oval.color.b = scrolled;
            }
        }, oval.color.b);

        api.component.setColor(scrollBarHorizontalB, new Color(0f, 0f, 1f, 1f));
        api.component.setColor2(scrollBarHorizontalB, new Color(0f, 0f, 0.8f, 1f));


        Textfield textField = api.component.textfield.create(18, 11, 10, "", null, 128);
        Button notiBtn = api.component.button.textButton.create(18, 9, 10, 2, "Notification");
        api.component.button.centerContent(notiBtn);
        api.component.addUpdateAction(notiBtn, new UpdateAction() {
            @Override
            public void onUpdate() {
                api.component.setDisabled(notiBtn, textField.content.length() == 0);
            }
        });
        api.component.button.setButtonAction(notiBtn, new ButtonAction() {
            @Override
            public void onRelease() {
                Notification notification = api.notification.create(textField.content);
                api.notification.setColor(notification, new Color(MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), 1f));

                api.notification.setNotificationAction(notification, new NotificationAction() {
                    @Override
                    public void onMouseDoubleClick(int button) {
                        if (button == Input.Buttons.LEFT) {
                            api.removeNotification(notification);
                        }
                    }
                });

                api.addNotification(notification);
                api.addNotification(api.notification.tooltip.create(32, 32, api.toolTip.create("abc")));
            }
        });


        Progressbar progressBar = api.component.progressbar.create(18, 3, 8, 0, true, true);

        api.component.setColor2(progressBar, Color.BLUE);
        api.component.progressbar.setFontColor(progressBar, Color.WHITE);

        ScrollbarHorizontal pgScrollbar = api.component.scrollbar.scrollbarHorizontal.create(18, 5, 8, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                api.component.progressbar.setProgress(progressBar, scrolled);
            }
        });

        Textfield number = api.widgets.textfield.createIntegerInputField(18, 7, 4, -100, 100, integer -> api.addNotification(api.notification.create("Input: " + integer)));


        ComboboxItem comboboxItem = api.component.comboBox.item.create("Test");


        ComboboxItem comboboxItem2 = api.component.comboBox.item.create("2");


        Combobox comboBox = api.component.comboBox.create(25, 7, 7, new ComboboxItem[]{
                comboboxItem,
                comboboxItem2,
                api.component.comboBox.item.create("3"),

        });

        api.component.setColor(comboBox, Color.RED);
        api.component.setColor2(comboBox, Color.BLUE);


        Button modal1 = api.component.button.textButton.create(28, 5, 5, 1, "Modal 1", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.widgets.modal.createYesNoRequester("Caption ", "Modal 1 Example Text", null));
                api.addWindowAsModal(api.widgets.modal.createYesNoRequester("FollowUp 1 ", "Modal 1 Example Text", null));
                api.addWindowAsModal(api.widgets.modal.createYesNoRequester("FollowUp 2 ", "Modal 1 Example Text", null));
            }
        });


        Button modal2 = api.component.button.textButton.create(28, 4, 5, 1, "Modal 2", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.widgets.modal.createTextInputModal("Caption", "Enter Text", "", new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        System.out.println(s);
                    }
                }));
            }
        });

        final Color[] lastColor = {null};

        Button modal3 = api.component.button.textButton.create(28, 3, 5, 1, "Modal 3", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.widgets.modal.createColorPickerModal("Caption", fColor -> lastColor[0] = fColor, lastColor[0]));
            }
        });


        Button modal4 = api.component.button.textButton.create(28, 2, 5, 1, "Modal 4", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.widgets.modal.createTouchTextInputModal("Caption", "Input Please", "", new Consumer<String>() {
                            @Override
                            public void accept(String s) {

                            }
                        }, 1, 20,
                        api.config.mouseTextInput.getDefaultLowerCaseCharacters(),
                        api.config.mouseTextInput.getDefaultUpperCaseCharacters(),
                        14
                ));
            }
        });

        Button soundBtn = api.component.button.textButton.create(34, 2, 3, 1, "SFX", new ButtonAction() {
            @Override
            public void onRelease() {

            }
        });

        Checkbox checkBox = api.component.checkbox.create(10, 2, "CheckBox");

        Checkbox checkBox2 = api.component.checkbox.create(18, 2, "Radio", CHECKBOX_STYLE.RADIO);

        AppViewport appViewPort = api.component.appViewport.create(29, 9, 4, 4, new AppViewPortAction() {
        }, 100, 100, 1);

        Textfield osKeyBoardTextInput = api.component.textfield.create(18, 13, 10, "", null, 128);
        api.component.textfield.setTextFieldAction(osKeyBoardTextInput, new TextFieldAction() {
            @Override
            public void onEnter(String content, boolean valid) {
                api.closeMouseTextInput();
            }

            @Override
            public void onFocus() {
                MouseTextInput mouseTextInput = api.mouseTextInput.create(
                        api.component.absoluteX(osKeyBoardTextInput),
                        api.component.absoluteY(osKeyBoardTextInput)
                        , new MouseTextInputAction() {
                            @Override
                            public boolean onConfirm() {
                                api.component.textfield.unFocus(osKeyBoardTextInput);
                                return true;
                            }
                        }, null
                );

                api.mouseTextInput.setColor(mouseTextInput, Color.BLUE);
                api.mouseTextInput.setColor2(mouseTextInput, Color.RED);
                api.mouseTextInput.setFontColor(mouseTextInput, Color.YELLOW);
                api.mouseTextInput.enterCharacters(mouseTextInput, "abc");
                api.component.textfield.setMarkerPosition(osKeyBoardTextInput, osKeyBoardTextInput.content.length());
                api.openMouseTextInput(mouseTextInput);
            }
        });

        Array<Component> components = new Array<>(new Component[]{appViewPort, comboBox, checkBox, checkBox2,
                modal1, modal2, modal3, modal4, soundBtn,
                number, progressBar, pgScrollbar, notiBtn, textField, scrollBarVertical, knob, knobe, scrollBarHorizontalR,
                scrollBarHorizontalG, scrollBarHorizontalB, oval, rect, triangle, osKeyBoardTextInput});
        api.window.addComponents(window, components.toArray(Component[]::new));
        return components;
    }


    private Array<Component> createTab2(API api, Window window) {
        Array<Component> components = new Array<>();

        // Text Buttons Tab

        Tab tabTextButton = api.component.tabbar.tab.create("Buttons 1");


        TextButton textBtn1 = api.component.button.textButton.create(3, 3, 3, 2, "Toggle", new ButtonAction() {
            @Override
            public void onToggle(boolean value) {

            }
        }, BUTTON_MODE.TOGGLE);
        api.component.tabbar.tab.addTabComponent(tabTextButton, textBtn1);
        api.component.button.centerContent(textBtn1);
        api.component.setColor(textBtn1, Color.ORANGE);


        TextButton textBtn3 = api.component.button.textButton.create(3, 7, 5, 1, "Text 2", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.widgets.modal.createTextInputModal("Enter Text", "Please Enter some Text", "", s -> api.addNotification(api.notification.create(s))));
            }

            @Override
            public CMediaSprite icon() {
                return ExampleBaseMedia.ICON_EXAMPLE_1;
            }
        });

        TextButton textBtn4 = api.component.button.textButton.create(3, 9, 6, 2, "Text 1", new ButtonAction() {
            @Override
            public void onRelease() {

                api.openContextMenu(api.contextMenu.create(
                        new ContextMenuItem[]{
                                api.contextMenu.item.create("Item 1y", new ContextMenuItemAction() {
                                    @Override
                                    public void onSelect() {
                                        api.addNotification(api.notification.create("1"));
                                    }
                                }),
                                api.contextMenu.item.create("Item 2 ---", new ContextMenuItemAction() {
                                    @Override
                                    public void onSelect() {
                                        api.addNotification(api.notification.create("2"));

                                    }
                                }),
                                api.contextMenu.item.create("Item 3 -----", new ContextMenuItemAction() {
                                    @Override
                                    public void onSelect() {
                                        api.addNotification(api.notification.create("3"));
                                        api.setAppToolTip(null);
                                        api.removeAllWindows();
                                    }
                                }),
                        }, null));
            }
        });


        api.component.setColor(textBtn4, new Color(0.6f, 0.5f, 0.5f, 1f));
        api.component.button.centerContent(textBtn4);

        TextButton textBtn5 = api.component.button.textButton.create(10, 9, 8, 2, "Viewport", new ButtonAction() {
            @Override
            public void onRelease() {
                switch (api.viewportMode()) {
                    case VIEWPORT_MODE.PIXEL_PERFECT -> api.setViewportMode(VIEWPORT_MODE.FIT);
                    case VIEWPORT_MODE.FIT -> api.setViewportMode(VIEWPORT_MODE.STRETCH);
                    case VIEWPORT_MODE.STRETCH -> api.setViewportMode(VIEWPORT_MODE.PIXEL_PERFECT);
                }
            }
        });

        NestedFrameBuffer nestedFrameBuffer = new NestedFrameBuffer(Pixmap.Format.RGBA8888, 32, 32);
        FrameBufferViewport frameBufferViewport = api.component.frameBufferViewport.create(24, 5, nestedFrameBuffer);
        OrthographicCamera camera2 = new OrthographicCamera(32, 32);
        camera2.setToOrtho(false, 32, 32);
        camera2.update();


        SpriteRenderer spriteRenderer2 = new SpriteRenderer(mediaManager);
        api.component.addUpdateAction(frameBufferViewport, new UpdateAction() {
            float deg1 = 0f;
            float deg2 = 0f;

            @Override
            public void onUpdate() {
                nestedFrameBuffer.begin();
                Gdx.gl.glClearColor(Math.abs(MathUtils.sin(deg1)), 1f - Math.abs(MathUtils.sin(deg2)), 0f, 1f);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                spriteRenderer2.setProjectionMatrix(camera2.combined);
                spriteRenderer2.begin();
                spriteRenderer2.drawCMediaImage(ExampleBaseMedia.ICON_EXAMPLE_1, 12 + MathUtils.sin(deg1) * 8f, 12 + MathUtils.cos(deg2) * 8f);
                deg1 += 0.05f;
                deg2 += 0.08f;
                spriteRenderer2.end();
                nestedFrameBuffer.end();

            }
        });


        api.window.addComponents(window, new Component[]{textBtn1, textBtn3, textBtn4, textBtn5, frameBufferViewport});
        api.component.tabbar.tab.addTabComponents(tabTextButton, new Component[]{textBtn1, textBtn3, textBtn4, textBtn5, frameBufferViewport});

        // Image Buttons Tab

        Tab tabImageButton = api.component.tabbar.tab.create("Buttons 2");

        ImageButton imageButton1 = api.component.button.imageButton.create(3, 11, 2, 1, ExampleBaseMedia.EXAMPLE_ANIMATION);

        ImageButton imageButton2 = api.component.button.imageButton.create(6, 11, 2, 1, ExampleBaseMedia.BUTTON_ANIM_EXAMPLE_ARRAY, 0, null, BUTTON_MODE.DEFAULT);
        ButtonAction imageButton2Action = new ButtonAction() {
            @Override
            public void onRelease() {
                int index = imageButton2.arrayIndex + 1;
                if (index > mediaManager.arraySize((CMediaArray) imageButton2.image) - 1) {
                    index = 0;
                }

                api.component.button.imageButton.setArrayIndex(imageButton2, index);
            }
        };
        api.component.button.setButtonAction(imageButton2, imageButton2Action);

        ImageButton imageButton3 = api.component.button.imageButton.create(3, 8, 2, 2, ExampleBaseMedia.ICON_EXAMPLE_1, 0, null, BUTTON_MODE.TOGGLE);
        ImageButton imageButton4 = api.component.button.imageButton.create(5, 8, 2, 2, ExampleBaseMedia.ICON_EXAMPLE_2, 0, null, BUTTON_MODE.TOGGLE);
        ImageButton imageButton5 = api.component.button.imageButton.create(3, 6, 2, 2, ExampleBaseMedia.ICON_EXAMPLE_3, 0, null, BUTTON_MODE.TOGGLE);
        ImageButton imageButton6 = api.component.button.imageButton.create(5, 6, 2, 2, ExampleBaseMedia.ICON_EXAMPLE_4, 0, null, BUTTON_MODE.TOGGLE);

        api.component.button.centerContent(imageButton3);
        api.component.button.centerContent(imageButton4);
        api.component.button.centerContent(imageButton5);
        api.component.button.centerContent(imageButton6);

        api.component.setColor(imageButton3, Color.GREEN);
        api.component.setColor(imageButton4, Color.BLUE);
        api.component.setColor(imageButton5, Color.YELLOW);
        api.component.setColor(imageButton6, Color.valueOf("F57D4A"));

        api.widgets.button.makeExclusiveToggleButtons(new Button[]{
                imageButton3, imageButton4, imageButton5, imageButton6
        }, new BiConsumer<Button, Boolean>() {
            @Override
            public void accept(Button button, Boolean aBoolean) {

            }
        });


        int segment_w = 8;
        int segment_h = 3;

        NestedFrameBuffer tt_nestedFrameBuffer = new NestedFrameBuffer(Pixmap.Format.RGBA8888, api.TS(segment_w), api.TS(segment_h));
        OrthographicCamera tt_camera = new OrthographicCamera(api.TS(segment_w), api.TS(segment_h));
        tt_camera.setToOrtho(false, api.TS(segment_w), api.TS(segment_h));
        tt_camera.update();

        TooltipFramebufferViewportSegment frameBufferSegment = api.toolTip.segment.framebuffer.create(tt_nestedFrameBuffer, Color.GRAY, Color.GRAY, SEGMENT_ALIGNMENT.CENTER, segment_w, segment_h);
        SpriteRenderer tt_spriteRenderer = new SpriteRenderer(mediaManager);


        api.component.button.setButtonAction(imageButton4, new ButtonAction() {
            @Override
            public Tooltip onShowTooltip() {
                return api.toolTip.create(
                        new TooltipSegment[]{
                                api.toolTip.segment.text.create("color[#FF00FF00]Title[][#00FF00AA]Test[]", Color.WHITE, Color.BLUE, SEGMENT_ALIGNMENT.CENTER, false, false),
                                api.toolTip.segment.text.create("555555", Color.WHITE, Color.BLACK, SEGMENT_ALIGNMENT.CENTER, false, false, true),
                                api.toolTip.segment.text.create("555555", Color.WHITE, Color.BLACK, SEGMENT_ALIGNMENT.CENTER, false, false, true),
                                api.toolTip.segment.text.create("1", Color.WHITE, Color.RED, SEGMENT_ALIGNMENT.CENTER, false, false),
                                api.toolTip.segment.text.create("2", Color.WHITE, Color.BLACK, SEGMENT_ALIGNMENT.CENTER, false, false),
                                api.toolTip.segment.text.create("3", Color.WHITE, Color.BLUE, SEGMENT_ALIGNMENT.CENTER, false, false),
                                api.toolTip.segment.text.create("555555", Color.WHITE, Color.BLUE, SEGMENT_ALIGNMENT.RIGHT, false, true),
                                api.toolTip.segment.image.create(ExampleBaseMedia.EXAMPLE_ANIMATION_3, 0, true, false, Color.MAGENTA, Color.RED, SEGMENT_ALIGNMENT.CENTER, false, true),
                                api.toolTip.segment.text.create("555555", Color.WHITE, Color.PURPLE, SEGMENT_ALIGNMENT.RIGHT, false, true),
                                frameBufferSegment,
                        }, new ToolTipAction() {
                            @Override
                            public void onDisplay() {
                            }

                            @Override
                            public void onUpdate() {
                                tt_nestedFrameBuffer.begin();
                                Gdx.gl.glClearColor(0f, 1f, 0f, 1f);
                                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                                tt_spriteRenderer.setProjectionMatrix(tt_camera.combined);
                                tt_spriteRenderer.begin();
                                tt_spriteRenderer.drawCMediaImage(ExampleBaseMedia.ICON_EXAMPLE_1, 8, 8);
                                tt_spriteRenderer.end();
                                tt_nestedFrameBuffer.end();
                            }

                            @Override
                            public void onRemove() {
                            }
                        }, 0, Color.RED, Color.BLUE, 5, DIRECTION.RIGHT

                );
            }
        });


        Array<Component> border = api.widgets.image.createBorder(10, 4, 4, 4);
        api.window.addComponents(window, border.toArray(Component[]::new));
        api.component.tabbar.tab.addTabComponents(tabImageButton, border.toArray(Component[]::new));

        api.window.addComponents(window, new Component[]{imageButton1, imageButton2, imageButton3, imageButton4, imageButton5, imageButton6});
        api.component.tabbar.tab.addTabComponents(tabImageButton, new Component[]{imageButton1, imageButton2, imageButton3, imageButton4, imageButton5, imageButton6});

        Array<String> items = new Array<>();
        for (int i = 0; i < 40; i++)
            items.add("Item " + i);
        APIWidgets.APICompositeGrid.PageAbleReadOnlyGrid pageGrid = api.widgets.grid.createPageableReadOnlyGrid(16, 4, 8, 4, items, null, false, true, false);
        api.widgets.grid.pageableReadOnlyGridSetGridAction(pageGrid, new GridAction() {
            @Override
            public int iconIndex(Object listItem) {
                return 0;
            }

            @Override
            public Tooltip toolTip(Object listItem) {
                return api.toolTip.create(listItem.toString());
            }

            @Override
            public CMediaSprite icon(Object listItem) {
                return ExampleBaseMedia.ICON_EXAMPLE_1;
            }

            @Override
            public Color iconColor(Object item) {
                return pageGrid.grid.selectedItem == item ? Color.LIGHT_GRAY : Color.GRAY;
            }
        });
        api.component.tabbar.tab.addTabComponents(tabImageButton, pageGrid.allComponents);
        api.window.addComponents(window, pageGrid.allComponents);


        /* Text / Image / Seperator */

        Tab tabTextImage = api.component.tabbar.tab.create("Text/Image");

        Image image1 = api.component.image.create(3, 4, ExampleBaseMedia.EXAMPLE_ANIMATION_2);

        Text text = api.component.text.create(12, 10, 0, "Lorem [#FF0000]ipsum dolor sit amet, consetetur\nsadipscing elitr, sed diam nonumy eirmod");

        Text text2 = api.component.text.create(12, 7, 0, "Lorem ipsum dolor sit[#FF00FF] amet, consetetur\nsadipscing elitr, sed diam nonumy eirmod");
        api.component.text.setFontColor(text2, Color.WHITE);
        api.window.addComponents(window, new Component[]{image1, text, text2});
        api.component.tabbar.tab.addTabComponents(tabTextImage, new Component[]{image1, text, text2});


        // Tab Bar


        Tabbar tabbar = api.component.tabbar.create(2, window.height - 5, window.width - 4, new Tab[]{tabTextButton, tabImageButton, tabTextImage},
                0, new TabBarAction() {
                    @Override
                    public void onChangeTab(int index, Tab tab) {
                    }
                }, true, window.height - 7, 0, false);
        api.window.addComponent(window, tabbar);


        components.add(tabbar);

        return components;
    }


    private Array<Component> createTab1(API api, Window window) {

        Array<Component> components = new Array<>();

        // List Tab
        Array<ListItem> listItems1 = new Array<>();
        addRandomItemsToList(listItems1, "L1 ");


        List list1 = api.component.list.create(2, 2, 6, 12, listItems1, null, true, true, true);
        ListAction list1Action = new ListAction<ListItem>() {

            @Override
            public CMediaSprite icon(ListItem listItem) {
                return listItem.icon;
            }

            @Override
            public String text(ListItem listItem) {
                return (listItem.text);
            }

            @Override
            public void onDragFromList(List fromList, int fromIndex, int toIndex) {
                moveToList(fromList.items, fromIndex, list1.items, toIndex);
            }

            @Override
            public void onDragFromGrid(Grid fromInventory, int from_x, int from_y, int toIndex) {
                moveFromInventoryToList(fromInventory.items, from_x, from_y, list1.items, toIndex);
            }

            @Override
            public boolean canDragFromGrid(Grid fromInventory) {
                return true;
            }

            @Override
            public boolean canDragFromList(List list) {
                return true;
            }

            @Override
            public boolean onItemSelected(ListItem listItem) {
                api.addNotification(api.notification.create("Selected: " + listItem));
                return true;
            }

            @Override
            public Tooltip toolTip(ListItem listItem) {
                if (listItem == null) return null;
                return api.toolTip.create(new TooltipSegment[]{
                        api.toolTip.segment.text.create(listItem.text)
                });
            }

            @Override
            public Color cellColor(ListItem listItem) {
                return listItem != null ? Color.valueOf("65F7B3") : Color.BLUE;
            }
        };
        api.component.list.setListAction(list1, list1Action);

        Array<ListItem> listItems2 = new Array<>();
        addRandomItemsToList(listItems2, "L2 ");


        List list2 = api.component.list.create(10, 2, 6, 12, listItems2, null, false, true, true, true);
        ListAction list2Action = new ListAction<ListItem>() {
            @Override
            public CMediaSprite icon(ListItem listItem) {
                return listItem.icon;
            }

            @Override
            public String text(ListItem listItem) {
                return (listItem.text);
            }

            @Override
            public void onDragFromList(List fromList, int fromIndex, int toIndex) {
                moveToList(fromList.items, fromIndex, list2.items, toIndex);
            }

            @Override
            public void onDragFromGrid(Grid fromInventory, int from_x, int from_y, int toIndex) {
                moveFromInventoryToList(fromInventory.items, from_x, from_y, list2.items, toIndex);
            }


            @Override
            public boolean canDragFromGrid(Grid fromInventory) {
                return true;
            }

            @Override
            public boolean canDragFromList(List list) {
                return true;
            }

            @Override
            public boolean onItemSelected(ListItem listItem) {
                api.addNotification(api.notification.create("Selected: " + listItem, null, true));
                return true;
            }

            @Override
            public Tooltip toolTip(ListItem listItem) {
                if (listItem == null) return null;
                return api.toolTip.create(new TooltipSegment[]{
                        api.toolTip.segment.text.create(listItem.text)
                });
            }
        };
        api.component.list.setListAction(list2, list2Action);

        api.window.addComponent(window, list1);
        Scrollbar list1ScrollBar = api.widgets.list.createScrollBar(list1);
        api.window.addComponent(window, list1ScrollBar);

        api.window.addComponent(window, list2);
        Scrollbar list2ScrollBar = api.widgets.list.createScrollBar(list2);
        api.window.addComponent(window, list2ScrollBar);


        ListItem[][] invItems = new ListItem[6][12];
        addRandomItemsToInventory(invItems, "I1");

        Grid grid1 = api.component.grid.create(18, 2, invItems, null, true, true, true);

        GridAction gridAction1 = new GridAction<ListItem>() {
            @Override
            public CMediaSprite icon(ListItem listItem) {
                return listItem.icon;
            }

            @Override
            public void onDragFromGrid(Grid fromInventory, int from_x, int from_y, int to_x, int to_y) {
                moveToInventory(fromInventory.items, from_x, from_y, grid1.items, to_x, to_y);
            }

            @Override
            public void onDragFromList(List fromList, int fromIndex, int to_x, int to_y) {
                moveFromListToInventory(fromList.items, fromIndex, invItems, to_x, to_y);
            }

            @Override
            public Tooltip toolTip(ListItem listItem) {
                return api.toolTip.create(new TooltipSegment[]{
                        api.toolTip.segment.text.create(listItem.text)
                });
            }

            @Override
            public boolean canDragFromList(List fromList) {
                return true;
            }

            @Override
            public boolean canDragFromGrid(Grid fromInventory) {
                return true;
            }

            @Override
            public boolean onItemSelected(ListItem listItem) {
                api.addNotification(api.notification.create("Selected: " + listItem));
                return true;
            }

            @Override
            public Color iconColor(ListItem item) {
                return grid1.selectedItems.contains(item) ? Color.LIGHT_GRAY : Color.GRAY;
            }
        };
        api.component.grid.setGridAction(grid1, gridAction1);
        api.window.addComponent(window, grid1);


        ListItem[][] invItems2 = new ListItem[6][12];
        addRandomItemsToInventory(invItems2, "I2");

        Grid grid2 = api.component.grid.create(25, 2, invItems2, null, true, true, true);

        GridAction gridAction2 = new GridAction<ListItem>() {
            @Override
            public CMediaSprite icon(ListItem listItem) {
                return listItem.icon;
            }

            @Override
            public void onDragFromGrid(Grid fromInventory, int from_x, int from_y, int to_x, int to_y) {
                moveToInventory(fromInventory.items, from_x, from_y, grid2.items, to_x, to_y);
            }

            @Override
            public void onDragFromList(List fromList, int fromIndex, int to_x, int to_y) {
                moveFromListToInventory(fromList.items, fromIndex, invItems2, to_x, to_y);
            }

            @Override
            public Tooltip toolTip(ListItem listItem) {
                return api.toolTip.create(new TooltipSegment[]{
                        api.toolTip.segment.text.create(listItem.text)
                });
            }

            @Override
            public boolean canDragFromList(List fromList) {
                return true;
            }

            @Override
            public boolean canDragFromGrid(Grid fromInventory) {
                return true;
            }

            @Override
            public boolean onItemSelected(ListItem listItem) {
                api.addNotification(api.notification.create("Selected: " + listItem));
                return true;
            }

            @Override
            public Color iconColor(ListItem item) {
                return grid2.selectedItems.contains(item) ? Color.LIGHT_GRAY : Color.GRAY;
            }

        };
        api.component.grid.setGridAction(grid2, gridAction2);
        api.window.addComponent(window, grid2);

        ListItem[][] invItems3 = new ListItem[3][5];
        addRandomItemsToBigInventory(invItems3, "I3");

        Grid grid3 = api.component.grid.create(32, 2, invItems3, null, false, true, true, false, true);

        GridAction gridAction3 = new GridAction<ListItem>() {
            @Override
            public CMediaSprite icon(ListItem listItem) {
                return listItem.icon;
            }

            @Override
            public void onDragFromGrid(Grid fromInventory, int from_x, int from_y, int to_x, int to_y) {
                moveToInventory(fromInventory.items, from_x, from_y, grid3.items, to_x, to_y);
            }

            @Override
            public void onDragFromList(List fromList, int fromIndex, int to_x, int to_y) {
                moveFromListToInventory(fromList.items, fromIndex, invItems3, to_x, to_y);
            }

            @Override
            public Tooltip toolTip(ListItem listItem) {
                return api.toolTip.create(new TooltipSegment[]{
                        api.toolTip.segment.text.create(listItem.text)
                });
            }

            @Override
            public boolean canDragFromList(List fromList) {
                return true;
            }

            @Override
            public boolean canDragFromGrid(Grid fromInventory) {
                return true;
            }

            @Override
            public void onDragIntoApp(ListItem listItem, int from_x, int from_y, int to_x, int to_y) {
                invItems3[from_x][from_y] = null;
                api.addNotification(api.notification.create(listItem.text + " " + to_x + "," + to_y));
            }

            @Override
            public boolean canDragIntoApp() {
                return true;
            }

            @Override
            public Color iconColor(ListItem item) {
                return item == grid3.selectedItem ? Color.LIGHT_GRAY : Color.GRAY;
            }

        };
        api.component.grid.setGridAction(grid3, gridAction3);
        api.window.addComponent(window, grid3);

        components.addAll(list1, list1ScrollBar, list2, list2ScrollBar, grid1, grid2, grid3);

        return components;

    }


    private void moveToList(Array fromList, int fromIndex, Array toList, int toIndex) {
        Object object = fromList.get(fromIndex);
        if (fromList == toList && toIndex == toList.size) toIndex--;
        fromList.removeIndex(fromIndex);
        toList.insert(toIndex, object);
    }

    private void moveToInventory(Object[][] fromInventory, int from_x, int from_y, Object[][] toInventory, int to_x, int to_y) {
        if (toInventory[to_x][to_y] == null) {
            toInventory[to_x][to_y] = fromInventory[from_x][from_y];
            fromInventory[from_x][from_y] = null;
        } else {
            Object temp = toInventory[to_x][to_y];
            toInventory[to_x][to_y] = fromInventory[from_x][from_y];
            fromInventory[from_x][from_y] = temp;
        }
    }

    private void moveFromListToInventory(Array fromList, int fromIndex, Object[][] toInventory, int to_x, int to_y) {
        if (toInventory[to_x][to_y] == null) {
            Object object = fromList.get(fromIndex);
            toInventory[to_x][to_y] = object;
            fromList.removeIndex(fromIndex);
        }
    }

    private void moveFromInventoryToList(Object[][] fromInventory, int from_x, int from_y, Array toList, int toIndex) {
        Object object = fromInventory[from_x][from_y];
        fromInventory[from_x][from_y] = null;
        toList.insert(toIndex, object);
    }


    private void addRandomItemsToInventory(Object[][] inventory, String prefix) {
        int rnd;
        rnd = MathUtils.random(1, 10);
        for (int i = 1; i <= rnd; i++)
            inventory[MathUtils.random(0, inventory.length - 1)][MathUtils.random(0, inventory[0].length - 1)] = new ListItem(prefix + "Item " + i, ExampleBaseMedia.ICON_EXAMPLE_1);
        rnd = MathUtils.random(1, 10);
        for (int i = 1; i <= rnd; i++)
            inventory[MathUtils.random(0, inventory.length - 1)][MathUtils.random(0, inventory[0].length - 1)] = new ListItem(prefix + "Item " + i, ExampleBaseMedia.ICON_EXAMPLE_2);
        rnd = MathUtils.random(1, 10);
        for (int i = 1; i <= rnd; i++)
            inventory[MathUtils.random(0, inventory.length - 1)][MathUtils.random(0, inventory[0].length - 1)] = new ListItem(prefix + "Item " + i, ExampleBaseMedia.ICON_EXAMPLE_3);
    }

    private void addRandomItemsToBigInventory(Object[][] inventory, String prefix) {
        int rnd;
        rnd = MathUtils.random(1, 10);
        for (int i = 1; i <= rnd; i++)
            inventory[MathUtils.random(0, inventory.length - 1)][MathUtils.random(0, inventory[0].length - 1)] = new ListItem(prefix + "Item " + i, ExampleBaseMedia.ICON_EXAMPLE_DOUBLE);
    }

    private void addRandomItemsToList(Array list, String prefix) {
        int rnd;
        rnd = MathUtils.random(5, 8);
        for (int i = 1; i <= rnd; i++)
            list.add(new ListItem(prefix + "Item " + i, switch (MathUtils.random(1, 3)) {
                case 1 -> ExampleBaseMedia.ICON_EXAMPLE_1;
                case 2 -> ExampleBaseMedia.ICON_EXAMPLE_2;
                case 3 -> ExampleBaseMedia.ICON_EXAMPLE_3;
                default -> throw new IllegalStateException("Unexpected value: " + MathUtils.random(1, 3));
            }));
    }


    class ListItem {
        public String text;

        public CMediaImage icon;

        public ListItem(String text, CMediaImage icon) {
            this.text = text;
            this.icon = icon;
        }
    }


}
