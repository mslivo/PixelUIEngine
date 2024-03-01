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
import org.mslivo.core.engine.ui_engine.ui.Window;
import org.mslivo.core.engine.ui_engine.ui.WindowGenerator;
import org.mslivo.core.engine.ui_engine.ui.actions.*;
import org.mslivo.core.engine.ui_engine.ui.components.Component;
import org.mslivo.core.engine.ui_engine.ui.components.button.Button;
import org.mslivo.core.engine.ui_engine.ui.components.button.ButtonMode;
import org.mslivo.core.engine.ui_engine.ui.components.button.ImageButton;
import org.mslivo.core.engine.ui_engine.ui.components.button.TextButton;
import org.mslivo.core.engine.ui_engine.ui.components.checkbox.CheckBox;
import org.mslivo.core.engine.ui_engine.ui.components.checkbox.CheckBoxStyle;
import org.mslivo.core.engine.ui_engine.ui.components.combobox.ComboBox;
import org.mslivo.core.engine.ui_engine.ui.components.combobox.ComboBoxItem;
import org.mslivo.core.engine.ui_engine.ui.components.image.Image;
import org.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import org.mslivo.core.engine.ui_engine.ui.components.knob.Knob;
import org.mslivo.core.engine.ui_engine.ui.components.list.List;
import org.mslivo.core.engine.ui_engine.ui.components.progressbar.ProgressBar;
import org.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollBar;
import org.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollBarHorizontal;
import org.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollBarVertical;
import org.mslivo.core.engine.ui_engine.ui.components.shape.Shape;
import org.mslivo.core.engine.ui_engine.ui.components.shape.ShapeType;
import org.mslivo.core.engine.ui_engine.ui.components.tabbar.Tab;
import org.mslivo.core.engine.ui_engine.ui.components.tabbar.TabBar;
import org.mslivo.core.engine.ui_engine.ui.components.text.Text;
import org.mslivo.core.engine.ui_engine.ui.components.textfield.TextField;
import org.mslivo.core.engine.ui_engine.ui.components.viewport.GameViewPort;
import org.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenuItem;
import org.mslivo.core.engine.ui_engine.ui.notification.Notification;
import org.mslivo.core.engine.ui_engine.ui.ostextinput.MouseTextInputAction;
import org.mslivo.core.engine.ui_engine.ui.tooltip.ToolTip;
import org.mslivo.core.engine.ui_engine.UIBaseMedia;
import org.mslivo.core.engine.ui_engine.enums.VIEWPORT_MODE;
import org.mslivo.example.data.ExampleData;
import org.mslivo.example.engine.ExampleEngineAdapter;
import org.mslivo.example.ui.media.ExampleBaseMedia;

import java.util.ArrayList;
import java.util.Arrays;
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
        Window window = api.window.create(0, 0, 40, 18, title, ExampleBaseMedia.GUI_ICON_EXAMPLE_WINDOW, 0);
        api.window.setEnforceScreenBounds(window, true);
        api.window.addComponent(window, api.preConfigured.button_CreateWindowCloseButton(window));
        //api.windows.setPosition(window,MathUtils.random(0,inputState.internal_resolution_w-window.width*16),MathUtils.random(0,inputState.internal_resolution_h-window.height*16));
        api.window.center(window);

        /* List / Inventory */
        ArrayList<Component> components_tab1 = createTab1(window);
        /* Button, Text, Image */
        ArrayList<Component> components_tab2 = createTab2(window);
        /* Slider */
        ArrayList<Component> components_tab3 = createTab3(window);
        /* Font */
        ArrayList<Component> components_tab4 = createTab4(window);

        ArrayList<Tab> tabs = new ArrayList<>();
        tabs.add(api.component.tabBar.tab.create("Tab I", ExampleBaseMedia.GUI_ICON_EXAMPLE_BULLET_BLUE,0, components_tab1.toArray(new Component[]{})));
        tabs.add(api.component.tabBar.tab.create("Tab II", ExampleBaseMedia.GUI_ICON_EXAMPLE_BULLET_GREEN,0, components_tab2.toArray(new Component[]{})));
        tabs.add(api.component.tabBar.tab.create("Tab III", ExampleBaseMedia.GUI_ICON_EXAMPLE_BULLET_ORANGE,0, components_tab3.toArray(new Component[]{})));
        tabs.add(api.component.tabBar.tab.create("Font", ExampleBaseMedia.GUI_ICON_EXAMPLE_BULLET_ORANGE,0, components_tab4.toArray(new Component[]{})));


        ArrayList<Component> tabBarComponents = api.preConfigured.tabBar_createExtendableTabBar(1, window.height - 3, window.width - 2, tabs.toArray(new Tab[]{}),
                0, null, true, window.height - 4, false);

        api.window.addComponents(window, tabBarComponents.toArray(new Component[]{}));

        return window;
    }


    private ArrayList<Component> createTab4(Window window) {
        Text text1 = api.component.text.create(2, 6, new String[]{"ABCDEFGHIJKLMNOPQRSTUVWXYZ"});
        Text text2 = api.component.text.create(2, 5, new String[]{"abcdefghijklmnopqrstuvwxyz"});
        Text text3 = api.component.text.create(2, 4, new String[]{"0123456789"});
        Text text4 = api.component.text.create(2, 3, new String[]{"¡!\"#$%&'()*+,-./:;<=>¿?@[\\]^_`{|}~¢£¤¥¦§¨©´·×÷€"});
        Text text5 = api.component.text.create(2, 2, new String[]{"pPqQwyg"});

        ArrayList<Component> components = new ArrayList<>(Arrays.asList(text1, text2, text3, text4, text5));
        api.window.addComponents(window, components.toArray(new Component[]{}));
        return components;
    }

    private ArrayList<Component> createTab3(Window window) {

        ScrollBarVertical scrollBarVertical = api.component.scrollBar.verticalScrollbar.create(2, 2, 12, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                window.color_a = scrolled;
            }
        }, window.color_a);

        Knob knob = api.component.knob.create(4, 2, null, false);
        api.component.knob.setTurned(knob, knob.color_a);
        KnobAction knobAction = new KnobAction() {
            @Override
            public void onTurned(float turned, float amount) {
                api.component.setAlpha(knob, 0.2f + (turned * 0.8f));
            }
        };
        api.component.knob.setKnobAction(knob, knobAction);
        api.component.setColor(knob, Color.BLUE);

        Knob knobe = api.component.knob.create(4, 5, null, true);
        api.component.setColor(knobe, Color.RED);

        // Shape
        Shape oval = api.component.shape.create(11, 3, 4, 4, ShapeType.OVAL);
        api.component.setColor(oval,Color.GREEN);

        Shape rect = api.component.shape.create(11, 8, 2, 2, ShapeType.RECT);
        api.component.setColor(rect,Color.YELLOW);

        Shape triangle = api.component.shape.create(14, 8, 2, 2, ShapeType.TRIANGLE_LEFT_DOWN);
        api.component.setColor(triangle,Color.BLUE);

        ScrollBarHorizontal scrollBarHorizontalR = api.component.scrollBar.horizontalScrollbar.create(4, 8, 6, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                oval.color_r = scrolled;
            }
        }, oval.color_r);
        api.component.setColor1And2(scrollBarHorizontalR, Color.RED);
        ScrollBarHorizontal scrollBarHorizontalG = api.component.scrollBar.horizontalScrollbar.create(4, 10, 6, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                oval.color_g = scrolled;
            }
        }, oval.color_g);
        api.component.setColor1And2(scrollBarHorizontalG, Color.GREEN);
        ScrollBarHorizontal scrollBarHorizontalB = api.component.scrollBar.horizontalScrollbar.create(4, 12, 6, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                oval.color_b = scrolled;
            }
        }, oval.color_b);
        api.component.setColor1And2(scrollBarHorizontalB, Color.BLUE);


        TextField textField = api.component.textField.create(18, 11, 10, "", null, 128);
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
                api.notification.setColor(notification, MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), 1f);

                api.notification.setNotificationAction(notification, new NotificationAction() {
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


        ProgressBar progressBar = api.component.progressBar.create(18, 3, 8, 0, true, true);
        api.component.setColor(progressBar, Color.BLUE);
        api.component.setColor2(progressBar, Color.RED);
        api.component.progressBar.setFont(progressBar, UIBaseMedia.UI_FONT_WHITE);

        ScrollBarHorizontal pgScrollbar = api.component.scrollBar.horizontalScrollbar.create(18, 5, 8, new ScrollBarAction() {
            @Override
            public void onScrolled(float scrolled) {
                api.component.progressBar.setProgress(progressBar, scrolled);
            }
        });

        TextField number = api.preConfigured.textField_createIntegerInputField(18, 7, 4, -100, 100, integer -> api.addNotification(api.notification.create("Input: " + integer)));

        ArrayList<ComboBoxItem> list = new ArrayList(Arrays.asList("One", "Two", "Three"));
        ComboBox comboBox = api.component.comboBox.create(25, 7, 6, new ComboBoxItem[]{
                api.component.comboBox.item.create("One"),
                api.component.comboBox.item.create("Two"),
                api.component.comboBox.item.create("Three"),

        });


        Button modal1 = api.component.button.textButton.create(28, 5, 5, 1, "Modal 1", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.preConfigured.modal_CreateYesNoRequester("Caption ", "Modal 1 Example Text", null));
                api.addWindowAsModal(api.preConfigured.modal_CreateYesNoRequester("FollowUp 1 ", "Modal 1 Example Text", null));
                api.addWindowAsModal(api.preConfigured.modal_CreateYesNoRequester("FollowUp 2 ", "Modal 1 Example Text", null));
            }
        });


        Button modal2 = api.component.button.textButton.create(28, 4, 5, 1, "Modal 2", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.preConfigured.modal_CreateMessageModal("Caption", new String[]{"Modal 2 Example Text"}, null));
            }
        });

        final Color[] lastColor = {null};

        Button modal3 = api.component.button.textButton.create(28, 3, 5, 1, "Modal 3", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.preConfigured.modal_CreateColorModal("Caption", fColor -> lastColor[0] = fColor, lastColor[0]));
            }
        });


        Button modal4 = api.component.button.textButton.create(28, 2, 5, 1, "Modal 4", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.preConfigured.modal_CreateTouchTextInputModal("Caption", "Input Please", "", new Consumer<String>() {
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

        CheckBox checkBox = api.component.checkBox.create(10, 2, "CheckBox");

        CheckBox checkBox2 = api.component.checkBox.create(18, 2, "Radio", CheckBoxStyle.RADIO);

        GameViewPort gameViewPort = api.component.gameViewPort.create(29, 9, 4, 4, null,100, 100, 1);

        TextField osKeyBoardTextInput = api.component.textField.create(18, 13, 10, "", null, 128);
        api.component.textField.setTextFieldAction(osKeyBoardTextInput, new TextFieldAction() {
            @Override
            public void onEnter(String content, boolean valid) {
                api.mouseTextInput.close();
            }

            @Override
            public void onFocus() {
                api.mouseTextInput.open(
                        api.component.getAbsoluteX(osKeyBoardTextInput),
                        api.component.getAbsoluteY(osKeyBoardTextInput)
                        , new MouseTextInputAction() {
                            @Override
                            public boolean onConfirm() {
                                api.component.textField.unFocus(osKeyBoardTextInput);
                                return true;
                            }
                        }, null
                );
                api.component.textField.setMarkerPosition(osKeyBoardTextInput, osKeyBoardTextInput.content.length());
            }
        });

        ArrayList<Component> components = new ArrayList<>(Arrays.asList(gameViewPort, comboBox, checkBox, checkBox2,
                modal1, modal2, modal3, modal4, soundBtn,
                number, progressBar, pgScrollbar, notiBtn, textField, scrollBarVertical, knob, knobe, scrollBarHorizontalR,
                scrollBarHorizontalG, scrollBarHorizontalB, oval, rect, triangle, osKeyBoardTextInput));
        api.window.addComponents(window, components.toArray(new Component[]{}));
        return components;
    }


    private ArrayList<Component> createTab2(Window window) {
        ArrayList<Component> components = new ArrayList<>();

        // Text Buttons Tab

        Tab tabTextButton = api.component.tabBar.tab.create("Buttons 1");


        TextButton textBtn1 = api.component.button.textButton.create(3, 3, 6, 2, "Toggle", new ButtonAction() {
            @Override
            public void onToggle(boolean value) {

            }
        }, null, ButtonMode.TOGGLE);
        api.component.tabBar.tab.addTabComponent(tabTextButton, textBtn1);
        api.component.button.centerContent(textBtn1);
        api.component.setColor(textBtn1, Color.ORANGE);


        TextButton textBtn3 = api.component.button.textButton.create(3, 7, 5, 1, "Text 2", new ButtonAction() {
            @Override
            public void onRelease() {
                api.addWindowAsModal(api.preConfigured.modal_CreateTextInputModal("Enter Text", "Please Enter some Text", "", s -> api.addNotification(api.notification.create(s))));
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
                                        api.setGameToolTip(null);
                                        api.removeAllWindows();
                                    }
                                }),
                        }
                        , null, 1f));
            }
        }, ExampleBaseMedia.GUI_ICON_EXAMPLE_1);
        api.component.setColor(textBtn4, Color.LIGHT_GRAY);
        api.component.button.centerContent(textBtn4);

        TextButton textBtn5 = api.component.button.textButton.create(10, 9, 8, 1, "Viewport", new ButtonAction() {
            @Override
            public void onRelease() {
                switch (api.viewportMode()) {
                    case PIXEL_PERFECT -> api.setViewportMode(VIEWPORT_MODE.FIT);
                    case FIT -> api.setViewportMode(VIEWPORT_MODE.STRETCH);
                    case STRETCH -> api.setViewportMode(VIEWPORT_MODE.PIXEL_PERFECT);
                }
            }
        });

        api.window.addComponents(window, new Component[]{textBtn1, textBtn3, textBtn4, textBtn5});
        api.component.tabBar.tab.addTabComponents(tabTextButton, new Component[]{textBtn1, textBtn3, textBtn4, textBtn5});

        // Image Buttons Tab

        Tab tabImageButton = api.component.tabBar.tab.create("Buttons 2");

        ImageButton imageButton1 = api.component.button.imageButton.create(3, 11, 2, 1, ExampleBaseMedia.GUI_ICON_BUTTON_ANIM_EXAMPLE);

        ImageButton imageButton2 = api.component.button.imageButton.create(6, 11, 2, 1, ExampleBaseMedia.GUI_ICON_BUTTON_ANIM_EXAMPLE_ARRAY, 0,null,ButtonMode.DEFAULT,0,0);
        ButtonAction imageButton2Action = new ButtonAction() {
            @Override
            public void onRelease() {
                int index = imageButton2.arrayIndex + 1;
                if (index > mediaManager.getCMediaArraySize((CMediaArray) imageButton2.image) - 1) {
                    index = 0;
                }

                api.component.button.imageButton.setArrayIndex(imageButton2, index);
            }
        };
        api.component.button.setButtonAction(imageButton2, imageButton2Action);

        ImageButton imageButton3 = api.component.button.imageButton.create(3, 8, 2, 2, ExampleBaseMedia.GUI_ICON_EXAMPLE_1, 0, null, ButtonMode.TOGGLE,0,0,true);
        ImageButton imageButton4 = api.component.button.imageButton.create(5, 8, 2, 2, ExampleBaseMedia.GUI_ICON_EXAMPLE_2, 0, null, ButtonMode.TOGGLE);
        ImageButton imageButton5 = api.component.button.imageButton.create(3, 6, 2, 2, ExampleBaseMedia.GUI_ICON_EXAMPLE_3, 0, null, ButtonMode.TOGGLE);
        ImageButton imageButton6 = api.component.button.imageButton.create(5, 6, 2, 2, ExampleBaseMedia.GUI_ICON_EXAMPLE_4, 0, null, ButtonMode.TOGGLE);

        api.component.button.centerContent(imageButton3);
        api.component.button.centerContent(imageButton4);
        api.component.button.centerContent(imageButton5);
        api.component.button.centerContent(imageButton6);

        api.component.setColor(imageButton3, Color.GREEN);
        api.component.setColor(imageButton4, Color.BLUE);
        api.component.setColor(imageButton5, Color.YELLOW);
        api.component.setColor(imageButton6, Color.ORANGE);

        ToolTip imageToolTip = api.toolTip.create(Tools.Text.toArray("ToolTip With Images"), null,null,true, 3, 4);

        api.toolTip.addToolTipImage(imageToolTip, api.toolTip.toolTipImage.create(ExampleBaseMedia.GUI_ICON_EXAMPLE_1, 0, 0));
        api.toolTip.addToolTipImage(imageToolTip, api.toolTip.toolTipImage.create(ExampleBaseMedia.GUI_ICON_EXAMPLE_2, 1, 1));
        api.toolTip.addToolTipImage(imageToolTip, api.toolTip.toolTipImage.create(ExampleBaseMedia.GUI_ICON_EXAMPLE_3, 3, 2));

        api.component.setToolTip(imageButton3, imageToolTip);
        api.component.setToolTip(imageButton4, api.toolTip.create(Tools.Text.toArray("Button 2", "ppPPP", "yyGG", "WWAXY")));
        api.component.setToolTip(imageButton5, api.toolTip.create(Tools.Text.toArray("Button 3", "Four")));
        api.component.setToolTip(imageButton6, api.toolTip.create(Tools.Text.toArray("Button 3", "Buttons")));


        ArrayList<Component> border = api.preConfigured.image_createBorder(10, 4, 4, 4);
        api.window.addComponents(window, border.toArray(new Component[]{}));
        api.component.tabBar.tab.addTabComponents(tabImageButton, border.toArray(new Component[]{}));

        api.window.addComponents(window, new Component[]{imageButton1, imageButton2, imageButton3, imageButton4, imageButton5, imageButton6});
        api.component.tabBar.tab.addTabComponents(tabImageButton, new Component[]{imageButton1, imageButton2, imageButton3, imageButton4, imageButton5, imageButton6});

        /* Text / Image / Seperator */

        Tab tabTextImage = api.component.tabBar.tab.create("Text/Image");

        Image image1 = api.component.image.create(3, 4, ExampleBaseMedia.GUI_ICON_EXAMPLE_ANIMATION_2);

        Text text = api.component.text.create(12, 10, Tools.Text.toArray("Lorem ipsum dolor sit amet, consetetur\nsadipscing elitr, sed diam nonumy eirmod"));

        Text text2 = api.component.text.create(12, 7, Tools.Text.toArray("Lorem ipsum dolor sit amet, consetetur\nsadipscing elitr, sed diam nonumy eirmod"));
        api.component.text.setFont(text2,UIBaseMedia.UI_FONT_WHITE);
        api.window.addComponents(window, new Component[]{image1, text, text2});
        api.component.tabBar.tab.addTabComponents(tabTextImage, new Component[]{image1, text, text2});


        // Tab Bar


        TabBar tabbar = api.component.tabBar.create(2, window.height - 5, window.width - 4, new Tab[]{tabTextButton, tabImageButton, tabTextImage},
                0, null, true, window.height - 7, 0, false);
        api.window.addComponent(window, tabbar);


        components.add(tabbar);

        return components;
    }


    private ArrayList<Component> createTab1(Window window) {

        ArrayList<Component> components = new ArrayList<>();

        // List Tab
        ArrayList<ListItem> listItems1 = new ArrayList<>();
        addRandomItemsToList(listItems1, "L1 ");


        List list1 = api.component.list.create(2, 2, 6, 12, listItems1, null, false, true, true, true);
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
            public void onItemSelected(ListItem listItem) {
                api.addNotification(api.notification.create("Selected: " +listItem));
            }

            @Override
            public ToolTip toolTip(ListItem listItem) {
                return api.toolTip.create(Tools.Text.toArray(listItem.text));
            }
        };
        api.component.list.setListAction(list1, list1Action);

        ArrayList<ListItem> listItems2 = new ArrayList<>();
        addRandomItemsToList(listItems2, "L2 ");

        List list2 = api.component.list.create(10, 2, 6, 12, listItems2, null, false, true, true, true);
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
            public void onItemSelected(ListItem listItem) {
                api.addNotification(api.notification.create("Selected: " +listItem));
            }

            @Override
            public ToolTip toolTip(ListItem listItem) {
                return api.toolTip.create(Tools.Text.toArray(listItem.text));
            }
        };
        api.component.list.setListAction(list2, list2Action);

        api.window.addComponent(window, list1);
        ScrollBar list1ScrollBar = api.preConfigured.list_CreateScrollBar(list1);
        api.window.addComponent(window, list1ScrollBar);

        api.window.addComponent(window, list2);
        ScrollBar list2ScrollBar = api.preConfigured.list_CreateScrollBar(list2);
        api.window.addComponent(window, list2ScrollBar);


        ListItem[][] invItems = new ListItem[6][12];
        addRandomItemsToInventory(invItems, "I1");

        Grid grid1 = api.component.grid.create(18, 2, invItems, null, true, true, true, false);

        GridAction gridAction1 = new GridAction<ListItem>() {
            @Override
            public CMediaGFX icon(ListItem listItem) {
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
            public ToolTip toolTip(ListItem listItem) {
                return api.toolTip.create(Tools.Text.toArray(listItem.text,"An item"));
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
            public void onItemSelected(ListItem listItem) {
                api.addNotification(api.notification.create("Selected: " +listItem));
            }
        };
        api.component.grid.setGridAction(grid1, gridAction1);
        api.window.addComponent(window, grid1);


        ListItem[][] invItems2 = new ListItem[6][12];
        addRandomItemsToInventory(invItems2, "I2");

        Grid grid2 = api.component.grid.create(25, 2, invItems2, null, true, true, true, false);

        GridAction gridAction2 = new GridAction<ListItem>() {
            @Override
            public CMediaGFX icon(ListItem listItem) {
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
            public ToolTip toolTip(ListItem listItem) {
                return api.toolTip.create(Tools.Text.toArray(listItem.text,"An item"));
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
            public void onItemSelected(ListItem listItem) {
                api.addNotification(api.notification.create("Selected: " +listItem));
            }
        };
        api.component.grid.setGridAction(grid2, gridAction2);
        api.window.addComponent(window, grid2);

        ListItem[][] invItems3 = new ListItem[3][5];
        addRandomItemsToBigInventory(invItems3, "I3");

        Grid grid3 = api.component.grid.create(32, 2, invItems3, null, true, false, false, true);

        GridAction gridAction3 = new GridAction<ListItem>() {
            @Override
            public CMediaGFX icon(ListItem listItem) {
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
            public ToolTip toolTip(ListItem listItem) {
                return api.toolTip.create(Tools.Text.toArray(listItem.text,"An item"));
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
            public void onDragIntoScreen(ListItem listItem, int x, int y, int screenX, int screenY) {
                invItems3[x][y] = null;
                api.addNotification(api.notification.create(listItem.text + " " + screenX + "," + screenY));
            }

            @Override
            public boolean canDragIntoScreen() {
                return true;
            }
        };
        api.component.grid.setGridAction(grid3, gridAction3);
        api.window.addComponent(window, grid3);

        components.addAll(Arrays.asList(new Component[]{
                list1,list1ScrollBar,list2,list2ScrollBar, grid1, grid2, grid3
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
