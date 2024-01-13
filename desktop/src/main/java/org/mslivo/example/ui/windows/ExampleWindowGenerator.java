package org.mslivo.example.ui.windows;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.CMediaArray;
import org.mslivo.core.engine.media_manager.media.CMediaGFX;
import org.mslivo.core.engine.media_manager.media.CMediaImage;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.tools.engine.GameEngine;
import org.mslivo.core.engine.tools.sound.SoundPlayer;
import org.mslivo.core.engine.ui_engine.API;
import org.mslivo.core.engine.ui_engine.gui.Window;
import org.mslivo.core.engine.ui_engine.gui.WindowGenerator;
import org.mslivo.core.engine.ui_engine.gui.actions.*;
import org.mslivo.core.engine.ui_engine.gui.components.Component;
import org.mslivo.core.engine.ui_engine.gui.components.button.Button;
import org.mslivo.core.engine.ui_engine.gui.components.button.ButtonMode;
import org.mslivo.core.engine.ui_engine.gui.components.button.ImageButton;
import org.mslivo.core.engine.ui_engine.gui.components.button.TextButton;
import org.mslivo.core.engine.ui_engine.gui.components.checkbox.CheckBox;
import org.mslivo.core.engine.ui_engine.gui.components.checkbox.CheckBoxStyle;
import org.mslivo.core.engine.ui_engine.gui.components.combobox.ComboBox;
import org.mslivo.core.engine.ui_engine.gui.components.combobox.ComboBoxItem;
import org.mslivo.core.engine.ui_engine.gui.components.image.Image;
import org.mslivo.core.engine.ui_engine.gui.components.inventory.Inventory;
import org.mslivo.core.engine.ui_engine.gui.components.knob.Knob;
import org.mslivo.core.engine.ui_engine.gui.components.list.List;
import org.mslivo.core.engine.ui_engine.gui.components.progressbar.ProgressBar;
import org.mslivo.core.engine.ui_engine.gui.components.scrollbar.ScrollBar;
import org.mslivo.core.engine.ui_engine.gui.components.scrollbar.ScrollBarHorizontal;
import org.mslivo.core.engine.ui_engine.gui.components.scrollbar.ScrollBarVertical;
import org.mslivo.core.engine.ui_engine.gui.components.shape.Shape;
import org.mslivo.core.engine.ui_engine.gui.components.shape.ShapeType;
import org.mslivo.core.engine.ui_engine.gui.components.tabbar.Tab;
import org.mslivo.core.engine.ui_engine.gui.components.tabbar.TabBar;
import org.mslivo.core.engine.ui_engine.gui.components.text.Text;
import org.mslivo.core.engine.ui_engine.gui.components.textfield.TextField;
import org.mslivo.core.engine.ui_engine.gui.components.viewport.GameViewPort;
import org.mslivo.core.engine.ui_engine.gui.contextmenu.ContextMenuItem;
import org.mslivo.core.engine.ui_engine.gui.notification.Notification;
import org.mslivo.core.engine.ui_engine.gui.ostextinput.MouseTextInputAction;
import org.mslivo.core.engine.ui_engine.gui.tooltip.ToolTip;
import org.mslivo.core.engine.ui_engine.media.GUIBaseMedia;
import org.mslivo.core.engine.ui_engine.misc.render.ViewportMode;
import org.mslivo.example.data.ExampleData;
import org.mslivo.example.engine.ExampleEngineAdapter;
import org.mslivo.example.ui.media.ExampleBaseMedia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Consumer;

public class ExampleWindowGenerator extends WindowGenerator {

    public ExampleWindowGenerator(API api) {
        super(api);
    }

    private GameEngine<ExampleEngineAdapter, ExampleData> gameEngine;

    private MediaManager mediaManager;

    private SoundPlayer soundPlayer;

    @Override
    public Window create(Object[] p) {
        String title = (String) p[0];
        this.gameEngine = (GameEngine) p[1];
        this.mediaManager = (MediaManager) p[2];

        this.soundPlayer = new SoundPlayer(mediaManager);
        /* Window */
        Window window = api.windows.create(0, 0, 40, 18, title, ExampleBaseMedia.GUI_ICON_EXAMPLE_WINDOW, false, true, true);
        api.windows.addComponent(window, api.preConfigured.button_CreateWindowCloseButton(window));
        //api.windows.setPosition(window,MathUtils.random(0,inputState.internal_resolution_w-window.width*16),MathUtils.random(0,inputState.internal_resolution_h-window.height*16));
        api.windows.center(window);

        /* List / Inventory */
        ArrayList<Component> components_tab1 = createTab1(window);
        /* Button, Text, Image */
        ArrayList<Component> components_tab2 = createTab2(window);
        /* Slider */
        ArrayList<Component> components_tab3 = createTab3(window);
        /* Font */
        ArrayList<Component> components_tab4 = createTab4(window);

        ArrayList<Tab> tabs = new ArrayList<>();
        tabs.add(api.components.tabBar.tab.create("Tab I", ExampleBaseMedia.GUI_ICON_EXAMPLE_BULLET_BLUE, components_tab1.toArray(new Component[]{})));
        tabs.add(api.components.tabBar.tab.create("Tab II", ExampleBaseMedia.GUI_ICON_EXAMPLE_BULLET_GREEN, components_tab2.toArray(new Component[]{})));
        tabs.add(api.components.tabBar.tab.create("Tab III", ExampleBaseMedia.GUI_ICON_EXAMPLE_BULLET_ORANGE, components_tab3.toArray(new Component[]{})));
        tabs.add(api.components.tabBar.tab.create("Font", ExampleBaseMedia.GUI_ICON_EXAMPLE_BULLET_ORANGE, components_tab4.toArray(new Component[]{})));


        ArrayList<Component> tabBarComponents = api.preConfigured.tabBar_createExtendableTabBar(1, window.height - 3, window.width - 2, tabs.toArray(new Tab[]{}),
                0, null, true, window.height - 4, false);

        api.windows.addComponents(window, tabBarComponents.toArray(new Component[]{}));

        return window;
    }


    private ArrayList<Component> createTab4(Window window) {
        Text text1 = api.components.text.create(2, 6, new String[]{"ABCDEFGHIJKLMNOPQRSTUVWXYZ"});
        Text text2 = api.components.text.create(2, 5, new String[]{"abcdefghijklmnopqrstuvwxyz"});
        Text text3 = api.components.text.create(2, 4, new String[]{"0123456789"});
        Text text4 = api.components.text.create(2, 3, new String[]{"¡!\"#$%&'()*+,-./:;<=>¿?@[\\]^_`{|}~¢£¤¥¦§¨©´·×÷€"});
        Text text5 = api.components.text.create(2, 2, new String[]{"pPqQwyg"});

        ArrayList<Component> components = new ArrayList<>(Arrays.asList(text1, text2, text3, text4, text5));
        api.windows.addComponents(window, components.toArray(new Component[]{}));
        return components;
    }

    private ArrayList<Component> createTab3(Window window) {

        ScrollBarVertical scrollBarVertical = api.components.scrollBar.verticalScrollbar.create(2, 2, 12, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                window.color_a = scrolled;
            }
        }, window.color_a);

        Knob knob = api.components.knob.create(4, 2, null, false);
        api.components.knob.setTurned(knob, knob.color_a);
        KnobAction knobAction = new KnobAction() {
            @Override
            public void onTurned(float turned, float amount) {
                api.components.setAlpha(knob, 0.2f + (turned * 0.8f));
            }
        };
        api.components.knob.setKnobAction(knob, knobAction);
        api.components.setColor(knob, Color.BLUE);

        Knob knobe = api.components.knob.create(4, 5, null, true);
        api.components.setColor(knobe, Color.RED);

        // Shape
        Shape oval = api.components.shape.create(11, 3, 4, 4, ShapeType.OVAL, Color.GREEN);

        Shape rect = api.components.shape.create(11, 8, 2, 2, ShapeType.RECT, Color.YELLOW);

        Shape triangle = api.components.shape.create(14, 8, 2, 2, ShapeType.TRIANGLE_LEFT_DOWN, Color.BLUE);

        ScrollBarHorizontal scrollBarHorizontalR = api.components.scrollBar.horizontalScrollbar.create(4, 8, 6, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                oval.color_r = scrolled;
            }
        }, oval.color_r);
        api.components.setColor1And2(scrollBarHorizontalR, Color.RED);
        ScrollBarHorizontal scrollBarHorizontalG = api.components.scrollBar.horizontalScrollbar.create(4, 10, 6, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                oval.color_g = scrolled;
            }
        }, oval.color_g);
        api.components.setColor1And2(scrollBarHorizontalG, Color.GREEN);
        ScrollBarHorizontal scrollBarHorizontalB = api.components.scrollBar.horizontalScrollbar.create(4, 12, 6, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                oval.color_b = scrolled;
            }
        }, oval.color_b);
        api.components.setColor1And2(scrollBarHorizontalB, Color.BLUE);


        TextField textField = api.components.textField.create(18, 11, 10, "", null, 128);
        Button notiBtn = api.components.button.textButton.create(18, 9, 10, 2, "Notification");
        api.components.button.centerContent(notiBtn);
        api.components.addUpdateAction(notiBtn, new UpdateAction() {
            @Override
            public void onUpdate() {
                api.components.setDisabled(notiBtn, textField.content.length() == 0);
            }
        });
        api.components.button.setButtonAction(notiBtn, new ButtonAction() {
            @Override
            public void onRelease() {
                Notification notification = api.notifications.create(textField.content, new Color(
                        MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), 1f
                ));

                api.notifications.setNotificationAction(notification, new NotificationAction() {
                    @Override
                    public void onMouseDoubleClick(int button) {
                        if (button == Input.Buttons.LEFT) {
                            api.removeNotification(notification);
                        }
                    }
                });

                api.addNotification(notification);
            }
        });


        ProgressBar progressBar = api.components.progressBar.create(18, 3, 8, 0, true, true, GUIBaseMedia.FONT_WHITE, Color.BLUE);
        api.components.setColor2(progressBar, Color.RED);

        ScrollBarHorizontal pgScrollbar = api.components.scrollBar.horizontalScrollbar.create(18, 5, 8, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                api.components.progressBar.setProgress(progressBar, scrolled);
            }
        });


        TextField number = api.preConfigured.textField_createIntegerInputField(18, 7, 4, -100, 100, integer -> api.addNotification(api.notifications.create("Input: " + integer)));


        ArrayList<ComboBoxItem> list = new ArrayList(Arrays.asList("One", "Two", "Three"));
        ComboBox comboBox = api.components.comboBox.create(25, 7, 6, new ComboBoxItem[]{
                api.components.comboBox.item.create("One", null, null, Color.RED),
                api.components.comboBox.item.create("Two", null, null, Color.YELLOW),
                api.components.comboBox.item.create("Three", null, null, Color.GREEN),

        });


        Button modal1 = api.components.button.textButton.create(28, 5, 5, 1, "Modal 1", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.preConfigured.modal_CreateYesNoRequester("Caption ", "Modal 1 Example Text", null));
                api.addWindowAsModal(api.preConfigured.modal_CreateYesNoRequester("FollowUp 1 ", "Modal 1 Example Text", null));
                api.addWindowAsModal(api.preConfigured.modal_CreateYesNoRequester("FollowUp 2 ", "Modal 1 Example Text", null));
            }
        });


        Button modal2 = api.components.button.textButton.create(28, 4, 5, 1, "Modal 2", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.preConfigured.modal_CreateMessageModal("Caption", new String[]{"Modal 2 Example Text"}, null));
            }
        });

        final Color[] lastColor = {null};

        Button modal3 = api.components.button.textButton.create(28, 3, 5, 1, "Modal 3", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.preConfigured.modal_CreateColorModal("Caption", fColor -> lastColor[0] = fColor, lastColor[0]));
            }
        });


        Button modal4 = api.components.button.textButton.create(28, 2, 5, 1, "Modal 4", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.preConfigured.modal_CreateTouchTextInputModal("Caption", "Input Please", "", new Consumer<String>() {
                            @Override
                            public void accept(String s) {

                            }
                        }, 1, 20,
                        api.config.getDefaultLowerCaseCharacters(),
                        api.config.getDefaultUpperCaseCharacters(),
                        14
                ));
            }
        });

        Button soundBtn = api.components.button.textButton.create(34, 2, 3, 1, "SFX", new ButtonAction() {
            @Override
            public void onRelease() {

            }
        });

        CheckBox checkBox = api.components.checkBox.create(10, 2, "CheckBox");

        CheckBox checkBox2 = api.components.checkBox.create(18, 2, "Radio", CheckBoxStyle.RADIO);

        GameViewPort gameViewPort = api.components.gameViewPort.create(29, 9, 4, 4, 100, 100, 1);

        TextField osKeyBoardTextInput = api.components.textField.create(18, 13, 10, "", null, 128);
        api.components.textField.setTextFieldAction(osKeyBoardTextInput, new TextFieldAction() {
            @Override
            public void onEnter(String content, boolean valid) {
                api.mouseTextInput.close();
            }

            @Override
            public void onFocus() {
                api.mouseTextInput.open(
                        api.components.getAbsoluteX(osKeyBoardTextInput),
                        api.components.getAbsoluteY(osKeyBoardTextInput)
                        , new MouseTextInputAction() {
                            @Override
                            public boolean onConfirm() {
                                api.components.textField.unFocus(osKeyBoardTextInput);
                                return true;
                            }
                        }, null
                );
                api.components.textField.setMarkerPosition(osKeyBoardTextInput, osKeyBoardTextInput.content.length());
            }
        });

        ArrayList<Component> components = new ArrayList<>(Arrays.asList(gameViewPort, comboBox, checkBox, checkBox2,
                modal1, modal2, modal3, modal4, soundBtn,
                number, progressBar, pgScrollbar, notiBtn, textField, scrollBarVertical, knob, knobe, scrollBarHorizontalR,
                scrollBarHorizontalG, scrollBarHorizontalB, oval, rect, triangle, osKeyBoardTextInput));
        api.windows.addComponents(window, components.toArray(new Component[]{}));
        return components;
    }


    private ArrayList<Component> createTab2(Window window) {
        ArrayList<Component> components = new ArrayList<>();

        // Text Buttons Tab

        Tab tabTextButton = api.components.tabBar.tab.create("Buttons 1");
        api.components.tabBar.tab.updateWidthAuto(tabTextButton);


        TextButton textBtn1 = api.components.button.textButton.create(3, 3, 6, 2, "Toggle", new ButtonAction() {
            @Override
            public void onToggle(boolean value) {
                api.input.setMousePosition(100, 100);
            }
        }, null, ButtonMode.TOGGLE);
        api.components.tabBar.tab.addTabComponent(tabTextButton, textBtn1);
        api.components.button.centerContent(textBtn1);
        api.components.setColor(textBtn1, Color.ORANGE);


        TextButton textBtn3 = api.components.button.textButton.create(3, 7, 5, 1, "Text 2", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.preConfigured.modal_CreateTextInputModal("Enter Text", "Please Enter some Text", "", s -> api.addNotification(api.notifications.create(s))));
            }
        });

        TextButton textBtn4 = api.components.button.textButton.create(3, 9, 6, 2, "Text 1", new ButtonAction() {
            @Override
            public void onRelease() {

                api.openContextMenu(api.contextMenu.create(
                        new ContextMenuItem[]{
                                api.contextMenu.item.create("Item 1y", new ContextMenuItemAction() {
                                    @Override
                                    public void onSelect() {
                                        api.addNotification(api.notifications.create("1"));
                                    }
                                }),
                                api.contextMenu.item.create("Item 2 ---", new ContextMenuItemAction() {
                                    @Override
                                    public void onSelect() {
                                        api.addNotification(api.notifications.create("2"));

                                    }
                                }),
                                api.contextMenu.item.create("Item 3 -----", new ContextMenuItemAction() {
                                    @Override
                                    public void onSelect() {
                                        api.addNotification(api.notifications.create("3"));
                                        api.setGameToolTip(null);
                                        api.removeAllWindows();
                                    }
                                }),
                        }
                        , null, 1f));
            }
        }, ExampleBaseMedia.GUI_ICON_EXAMPLE_1);
        api.components.setColor(textBtn4, Color.LIGHT_GRAY);
        api.components.button.centerContent(textBtn4);

        TextButton textBtn5 = api.components.button.textButton.create(10, 9, 8, 1, "Viewport", new ButtonAction() {
            @Override
            public void onRelease() {
                switch (api.viewportMode()) {
                    case PIXEL_PERFECT -> api.setViewportMode(ViewportMode.FIT);
                    case FIT -> api.setViewportMode(ViewportMode.STRETCH);
                    case STRETCH -> api.setViewportMode(ViewportMode.PIXEL_PERFECT);
                }
            }
        });

        api.windows.addComponents(window, new Component[]{textBtn1, textBtn3, textBtn4, textBtn5});
        api.components.tabBar.tab.addTabComponents(tabTextButton, new Component[]{textBtn1, textBtn3, textBtn4, textBtn5});

        // Image Buttons Tab

        Tab tabImageButton = api.components.tabBar.tab.create("Buttons 2");
        api.components.tabBar.tab.updateWidthAuto(tabImageButton);

        ImageButton imageButton1 = api.components.button.imageButton.create(3, 11, 2, 1, ExampleBaseMedia.GUI_ICON_BUTTON_ANIM_EXAMPLE);

        ImageButton imageButton2 = api.components.button.imageButton.create(6, 11, 2, 1, ExampleBaseMedia.GUI_ICON_BUTTON_ANIM_EXAMPLE_ARRAY, 0);
        ButtonAction imageButton2Action = new ButtonAction() {
            @Override
            public void onRelease() {
                int index = imageButton2.arrayIndex + 1;
                if (index > mediaManager.getCMediaArraySize((CMediaArray) imageButton2.image) - 1) {
                    index = 0;
                }

                api.components.button.imageButton.setArrayIndex(imageButton2, index);
            }
        };
        api.components.button.setButtonAction(imageButton2, imageButton2Action);

        ImageButton imageButton3 = api.components.button.imageButton.create(3, 8, 2, 2, ExampleBaseMedia.GUI_ICON_EXAMPLE_1, 0, null, ButtonMode.TOGGLE);
        ImageButton imageButton4 = api.components.button.imageButton.create(5, 8, 2, 2, ExampleBaseMedia.GUI_ICON_EXAMPLE_2, 0, null, ButtonMode.TOGGLE);
        ImageButton imageButton5 = api.components.button.imageButton.create(3, 6, 2, 2, ExampleBaseMedia.GUI_ICON_EXAMPLE_3, 0, null, ButtonMode.TOGGLE);
        ImageButton imageButton6 = api.components.button.imageButton.create(5, 6, 2, 2, ExampleBaseMedia.GUI_ICON_EXAMPLE_4, 0, null, ButtonMode.TOGGLE);

        api.components.button.centerContent(imageButton3);
        api.components.button.centerContent(imageButton4);
        api.components.button.centerContent(imageButton5);
        api.components.button.centerContent(imageButton6);

        api.components.setColor(imageButton3, Color.GREEN);
        api.components.setColor(imageButton4, Color.BLUE);
        api.components.setColor(imageButton5, Color.YELLOW);
        api.components.setColor(imageButton6, Color.ORANGE);

        ToolTip imageToolTip = api.toolTip.create(Tools.Text.toArray("ToolTip With Images"), true, null, null, 3, 4);

        api.toolTip.addToolTipImage(imageToolTip, api.toolTip.toolTipImage.create(ExampleBaseMedia.GUI_ICON_EXAMPLE_1, 0, 0));
        api.toolTip.addToolTipImage(imageToolTip, api.toolTip.toolTipImage.create(ExampleBaseMedia.GUI_ICON_EXAMPLE_2, 1, 1));
        api.toolTip.addToolTipImage(imageToolTip, api.toolTip.toolTipImage.create(ExampleBaseMedia.GUI_ICON_EXAMPLE_3, 3, 2));

        api.components.setToolTip(imageButton3, imageToolTip);
        api.components.setToolTip(imageButton4, api.toolTip.create(Tools.Text.toArray("Button 2", "ppPPP", "yyGG", "WWAXY")));
        api.components.setToolTip(imageButton5, api.toolTip.create(Tools.Text.toArray("Button 3", "Four")));
        api.components.setToolTip(imageButton6, api.toolTip.create(Tools.Text.toArray("Button 3", "Buttons")));


        ArrayList<Component> border = api.preConfigured.image_createBorder(10, 4, 4, 4);
        api.windows.addComponents(window, border.toArray(new Component[]{}));
        api.components.tabBar.tab.addTabComponents(tabImageButton, border.toArray(new Component[]{}));

        api.windows.addComponents(window, new Component[]{imageButton1, imageButton2, imageButton3, imageButton4, imageButton5, imageButton6});
        api.components.tabBar.tab.addTabComponents(tabImageButton, new Component[]{imageButton1, imageButton2, imageButton3, imageButton4, imageButton5, imageButton6});

        /* Text / Image / Seperator */

        Tab tabTextImage = api.components.tabBar.tab.create("Text/Image");
        api.components.tabBar.tab.updateWidthAuto(tabTextImage);

        Image image1 = api.components.image.create(3, 4, ExampleBaseMedia.GUI_ICON_EXAMPLE_ANIMATION_2);

        Text text = api.components.text.create(12, 10, Tools.Text.toArray("Lorem ipsum dolor sit amet, consetetur\nsadipscing elitr, sed diam nonumy eirmod"));

        Text text2 = api.components.text.create(12, 7, Tools.Text.toArray("Lorem ipsum dolor sit amet, consetetur\nsadipscing elitr, sed diam nonumy eirmod"), GUIBaseMedia.FONT_WHITE);
        api.windows.addComponents(window, new Component[]{image1, text, text2});
        api.components.tabBar.tab.addTabComponents(tabTextImage, new Component[]{image1, text, text2});


        // Tab Bar


        TabBar tabbar = api.components.tabBar.create(2, window.height - 5, window.width - 4, new Tab[]{tabTextButton, tabImageButton, tabTextImage},
                0, null, true, window.height - 7, 0, false);
        api.windows.addComponent(window, tabbar);


        components.add(tabbar);

        return components;
    }


    private ArrayList<Component> createTab1(Window window) {

        ArrayList<Component> components = new ArrayList<>();

        // List Tab
        ArrayList<ListItem> listItems1 = new ArrayList<>();
        addRandomItemsToList(listItems1, "L1 ");


        List list1 = api.components.list.create(2, 2, 6, 12, listItems1, null, false, true, true, true);
        ListAction list1Action = new ListAction<ListItem>() {
            @Override
            public CMediaGFX icon(ListItem listItem) {
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
            public void onDragFromInventory(Inventory fromInventory, int from_x, int from_y, int toIndex) {
                moveFromInventoryToList(fromInventory.items, from_x, from_y, list1.items, toIndex);
            }

            @Override
            public boolean canDragFromInventory(Inventory fromInventory) {
                return true;
            }

            @Override
            public boolean canDragFromList(List list) {
                return true;
            }

            @Override
            public void onItemSelected(ListItem listItem) {
                api.addNotification(api.notifications.create("Selected: " +listItem));
            }

            @Override
            public ToolTip toolTip(ListItem listItem) {
                return api.toolTip.create(Tools.Text.toArray(listItem.text));
            }
        };
        api.components.list.setListAction(list1, list1Action);

        ArrayList<ListItem> listItems2 = new ArrayList<>();
        addRandomItemsToList(listItems2, "L2 ");

        List list2 = api.components.list.create(10, 2, 6, 12, listItems2, null, false, true, true, true);
        ListAction list2Action = new ListAction<ListItem>() {
            @Override
            public CMediaGFX icon(ListItem listItem) {
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
            public void onDragFromInventory(Inventory fromInventory, int from_x, int from_y, int toIndex) {
                moveFromInventoryToList(fromInventory.items, from_x, from_y, list2.items, toIndex);
            }


            @Override
            public boolean canDragFromInventory(Inventory fromInventory) {
                return true;
            }

            @Override
            public boolean canDragFromList(List list) {
                return true;
            }

            @Override
            public void onItemSelected(ListItem listItem) {
                api.addNotification(api.notifications.create("Selected: " +listItem));
            }

            @Override
            public ToolTip toolTip(ListItem listItem) {
                return api.toolTip.create(Tools.Text.toArray(listItem.text));
            }
        };
        api.components.list.setListAction(list2, list2Action);

        api.windows.addComponent(window, list1);
        ScrollBar list1ScrollBar = api.preConfigured.list_CreateScrollBar(list1);
        api.windows.addComponent(window, list1ScrollBar);

        api.windows.addComponent(window, list2);
        ScrollBar list2ScrollBar = api.preConfigured.list_CreateScrollBar(list2);
        api.windows.addComponent(window, list2ScrollBar);


        ListItem[][] invItems = new ListItem[6][12];
        addRandomItemsToInventory(invItems, "I1");

        Inventory inventory1 = api.components.inventory.create(18, 2, invItems, null, true, true, true, false);

        InventoryAction inventoryAction1 = new InventoryAction<ListItem>() {
            @Override
            public CMediaGFX icon(ListItem listItem) {
                return listItem.icon;
            }

            @Override
            public void onDragFromInventory(Inventory fromInventory, int from_x, int from_y, int to_x, int to_y) {
                moveToInventory(fromInventory.items, from_x, from_y, inventory1.items, to_x, to_y);
            }

            @Override
            public void onDragFromList(List fromList, int fromIndex, int to_x, int to_y) {
                moveFromListToInventory(fromList.items, fromIndex, invItems, to_x, to_y);
            }

            @Override
            public ToolTip toolTip(ListItem listItem) {
                return api.toolTip.create(Tools.Text.toArray(listItem.text,"An item"));
            }

            @Override
            public boolean canDragFromList(List fromList) {
                return true;
            }

            @Override
            public boolean canDragFromInventory(Inventory fromInventory) {
                return true;
            }

            @Override
            public void onItemSelected(ListItem listItem) {
                api.addNotification(api.notifications.create("Selected: " +listItem));
            }
        };
        api.components.inventory.setInventoryAction(inventory1, inventoryAction1);
        api.windows.addComponent(window, inventory1);


        ListItem[][] invItems2 = new ListItem[6][12];
        addRandomItemsToInventory(invItems2, "I2");

        Inventory inventory2 = api.components.inventory.create(25, 2, invItems2, null, true, true, true, false);

        InventoryAction inventoryAction2 = new InventoryAction<ListItem>() {
            @Override
            public CMediaGFX icon(ListItem listItem) {
                return listItem.icon;
            }

            @Override
            public void onDragFromInventory(Inventory fromInventory, int from_x, int from_y, int to_x, int to_y) {
                moveToInventory(fromInventory.items, from_x, from_y, inventory2.items, to_x, to_y);
            }

            @Override
            public void onDragFromList(List fromList, int fromIndex, int to_x, int to_y) {
                moveFromListToInventory(fromList.items, fromIndex, invItems2, to_x, to_y);
            }

            @Override
            public ToolTip toolTip(ListItem listItem) {
                return api.toolTip.create(Tools.Text.toArray(listItem.text,"An item"));
            }

            @Override
            public boolean canDragFromList(List fromList) {
                return true;
            }

            @Override
            public boolean canDragFromInventory(Inventory fromInventory) {
                return true;
            }
            @Override
            public void onItemSelected(ListItem listItem) {
                api.addNotification(api.notifications.create("Selected: " +listItem));
            }
        };
        api.components.inventory.setInventoryAction(inventory2, inventoryAction2);
        api.windows.addComponent(window, inventory2);

        ListItem[][] invItems3 = new ListItem[3][5];
        addRandomItemsToBigInventory(invItems3, "I3");

        Inventory inventory3 = api.components.inventory.create(32, 2, invItems3, null, true, false, false, true);

        InventoryAction inventoryAction3 = new InventoryAction<ListItem>() {
            @Override
            public CMediaGFX icon(ListItem listItem) {
                return listItem.icon;
            }

            @Override
            public void onDragFromInventory(Inventory fromInventory, int from_x, int from_y, int to_x, int to_y) {
                moveToInventory(fromInventory.items, from_x, from_y, inventory3.items, to_x, to_y);
            }

            @Override
            public void onDragFromList(List fromList, int fromIndex, int to_x, int to_y) {
                moveFromListToInventory(fromList.items, fromIndex, invItems3, to_x, to_y);
            }

            @Override
            public ToolTip toolTip(ListItem listItem) {
                return api.toolTip.create(Tools.Text.toArray(listItem.text,"An item"));
            }

            @Override
            public boolean canDragFromList(List fromList) {
                return true;
            }

            @Override
            public boolean canDragFromInventory(Inventory fromInventory) {
                return true;
            }

            @Override
            public void onDragIntoScreen(ListItem listItem, int x, int y, int screenX, int screenY) {
                invItems3[x][y] = null;
                api.addNotification(api.notifications.create(listItem.text + " " + screenX + "," + screenY));
            }

            @Override
            public boolean canDragIntoScreen() {
                return true;
            }
        };
        api.components.inventory.setInventoryAction(inventory3, inventoryAction3);
        api.windows.addComponent(window, inventory3);

        components.addAll(Arrays.asList(new Component[]{
                list1,list1ScrollBar,list2,list2ScrollBar,inventory1,inventory2,inventory3
        }));

        return components;

    }


    private void moveToList(ArrayList fromList, int fromIndex, ArrayList toList, int toIndex) {
        Object object = fromList.get(fromIndex);
        if(fromList == toList && toIndex == toList.size()) toIndex--;
        fromList.remove(fromIndex);
        toList.add(toIndex, object);
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

    private void moveFromListToInventory(ArrayList fromList, int fromIndex, Object[][] toInventory, int to_x, int to_y) {

        if (toInventory[to_x][to_y] == null) {
            Object object = fromList.get(fromIndex);
            toInventory[to_x][to_y] = object;
            fromList.remove(fromIndex);
        }
    }

    private void moveFromInventoryToList(Object[][] fromInventory, int from_x, int from_y, ArrayList toList, int toIndex) {
        Object object = fromInventory[from_x][from_y];
        fromInventory[from_x][from_y] = null;
        toList.add(toIndex, object);
    }


    private void addRandomItemsToInventory(Object[][] inventory, String prefix) {
        int rnd;
        rnd = MathUtils.random(1, 10);
        for (int i = 1; i <= rnd; i++)
            inventory[MathUtils.random(0, inventory.length - 1)][MathUtils.random(0, inventory[0].length - 1)] = new ListItem(prefix + "Item " + i, ExampleBaseMedia.GUI_ICON_EXAMPLE_1);
        rnd = MathUtils.random(1, 10);
        for (int i = 1; i <= rnd; i++)
            inventory[MathUtils.random(0, inventory.length - 1)][MathUtils.random(0, inventory[0].length - 1)] = new ListItem(prefix + "Item " + i, ExampleBaseMedia.GUI_ICON_EXAMPLE_2);
        rnd = MathUtils.random(1, 10);
        for (int i = 1; i <= rnd; i++)
            inventory[MathUtils.random(0, inventory.length - 1)][MathUtils.random(0, inventory[0].length - 1)] = new ListItem(prefix + "Item " + i, ExampleBaseMedia.GUI_ICON_EXAMPLE_3);
    }

    private void addRandomItemsToBigInventory(Object[][] inventory, String prefix) {
        int rnd;
        rnd = MathUtils.random(1, 10);
        for (int i = 1; i <= rnd; i++)
            inventory[MathUtils.random(0, inventory.length - 1)][MathUtils.random(0, inventory[0].length - 1)] = new ListItem(prefix + "Item " + i, ExampleBaseMedia.GUI_ICON_EXAMPLE_DOUBLE);
    }

    private void addRandomItemsToList(ArrayList list, String prefix) {
        int rnd;
        rnd = MathUtils.random(5, 8);
        for (int i = 1; i <= rnd; i++)
            list.add(new ListItem(prefix + "Item " + i, switch (MathUtils.random(1,3)){
                case 1 -> ExampleBaseMedia.GUI_ICON_EXAMPLE_1;
                case 2 -> ExampleBaseMedia.GUI_ICON_EXAMPLE_2;
                case 3 -> ExampleBaseMedia.GUI_ICON_EXAMPLE_3;
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
