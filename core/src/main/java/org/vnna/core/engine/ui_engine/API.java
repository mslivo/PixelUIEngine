package org.vnna.core.engine.ui_engine;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import org.vnna.core.engine.media_manager.MediaManager;
import org.vnna.core.engine.media_manager.color.FColor;
import org.vnna.core.engine.media_manager.media.CMediaCursor;
import org.vnna.core.engine.media_manager.media.CMediaFont;
import org.vnna.core.engine.media_manager.media.CMediaGFX;
import org.vnna.core.engine.media_manager.media.CMediaImage;
import org.vnna.core.engine.tools.Tools;
import org.vnna.core.engine.ui_engine.gui.Window;
import org.vnna.core.engine.ui_engine.gui.WindowGenerator;
import org.vnna.core.engine.ui_engine.gui.actions.*;
import org.vnna.core.engine.ui_engine.gui.components.Component;
import org.vnna.core.engine.ui_engine.gui.components.button.Button;
import org.vnna.core.engine.ui_engine.gui.components.button.ImageButton;
import org.vnna.core.engine.ui_engine.gui.components.button.TextButton;
import org.vnna.core.engine.ui_engine.gui.components.checkbox.CheckBox;
import org.vnna.core.engine.ui_engine.gui.components.checkbox.CheckBoxStyle;
import org.vnna.core.engine.ui_engine.gui.components.combobox.ComboBox;
import org.vnna.core.engine.ui_engine.gui.components.image.Image;
import org.vnna.core.engine.ui_engine.gui.components.inventory.Inventory;
import org.vnna.core.engine.ui_engine.gui.components.knob.Knob;
import org.vnna.core.engine.ui_engine.gui.components.list.List;
import org.vnna.core.engine.ui_engine.gui.components.map.Map;
import org.vnna.core.engine.ui_engine.gui.components.map.MapOverlay;
import org.vnna.core.engine.ui_engine.gui.components.progressbar.ProgressBar;
import org.vnna.core.engine.ui_engine.gui.components.scrollbar.ScrollBar;
import org.vnna.core.engine.ui_engine.gui.components.scrollbar.ScrollBarHorizontal;
import org.vnna.core.engine.ui_engine.gui.components.scrollbar.ScrollBarVertical;
import org.vnna.core.engine.ui_engine.gui.components.shape.Shape;
import org.vnna.core.engine.ui_engine.gui.components.shape.ShapeType;
import org.vnna.core.engine.ui_engine.gui.components.tabbar.Tab;
import org.vnna.core.engine.ui_engine.gui.components.tabbar.TabBar;
import org.vnna.core.engine.ui_engine.gui.components.text.Text;
import org.vnna.core.engine.ui_engine.gui.components.textfield.TextField;
import org.vnna.core.engine.ui_engine.gui.components.viewport.GameViewPort;
import org.vnna.core.engine.ui_engine.gui.contextmenu.ContextMenu;
import org.vnna.core.engine.ui_engine.gui.contextmenu.ContextMenuItem;
import org.vnna.core.engine.ui_engine.gui.hotkeys.HotKey;
import org.vnna.core.engine.ui_engine.gui.notification.Notification;
import org.vnna.core.engine.ui_engine.gui.notification.STATE_NOTIFICATION;
import org.vnna.core.engine.ui_engine.gui.tool.MouseTool;
import org.vnna.core.engine.ui_engine.gui.tooltip.ToolTip;
import org.vnna.core.engine.ui_engine.gui.tooltip.ToolTipImage;
import org.vnna.core.engine.ui_engine.media.GUIBaseMedia;
import org.vnna.core.engine.ui_engine.misc.GraphInfo;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class API {

    public final _Notification notifications = new _Notification();

    public final _ContextMenu contextMenu = new _ContextMenu();

    public final _Windows windows = new _Windows();

    public final _Components components = new _Components();

    public final _Camera camera = new _Camera();

    public final _ToolTip toolTip = new _ToolTip();

    public final _Config config = new _Config();

    public final _Input input = new _Input();

    public final _MouseTool mouseTool = new _MouseTool();


    public final _HotKey hotkey = new _HotKey();

    private InputState inputState;

    private MediaManager mediaManager;

    private HashMap<Class, WindowGenerator> windowGeneratorCache;

    public final _Presets presets = new _Presets();

    public API(InputState inputState, MediaManager mediaManager) {
        this.inputState = inputState;
        this.mediaManager = mediaManager;
        this.windowGeneratorCache = new HashMap<>();
    }

    public void addNotification(Notification notification) {
        if (notification == null) return;
        inputState.notifications.add(notification);
        if (inputState.notifications.size() > config.notificationsMax) {
            inputState.notifications.remove(0);
        }
    }

    public ArrayList<Notification> findNotificationsByName(String name) {
        ArrayList<Notification> result = new ArrayList<>();
        if (name == null) return result;
        for (Notification notification : inputState.notifications) {
            if (name.equals(notification.name)) {
                result.add(notification);
            }
        }
        return result;
    }

    public Notification findNotificationByName(String name) {
        if (name == null) return null;
        ArrayList<Notification> result = findNotificationsByName(name);
        return result.size() > 0 ? result.get(0) : null;
    }

    public void addDelayedOneshotAction(UpdateAction updateAction) {
        if (updateAction == null) return;
        this.inputState.delayedOneshotActions.add(updateAction);
    }

    public void sendMessageToWindows(String message_type, Object... p) {
        for (Window window : inputState.windows) {
            for (MessageReceiverAction messageReceiver : window.messageReceiverActions) {
                if (messageReceiver.messageType.equals(message_type)) {
                    messageReceiver.onMessageReceived(p);
                }
            }

        }
    }

    public void windowsEnforceScreenBounds() {
        inputState.windows.forEach(window -> {
            UICommons.window_enforceScreenBounds(inputState, window);
        });
    }

    public class _HotKey {

        public HotKey create(int[] keyCodes, HotKeyAction hotKeyAction) {
            if (keyCodes == null || keyCodes.length == 0) return null;
            if (hotKeyAction == null) return null;
            HotKey hotKey = new HotKey();
            hotKey.pressed = false;
            setKeyCodes(hotKey, keyCodes);
            setHotKeyAction(hotKey, hotKeyAction);
            setName(hotKey, "");
            setData(hotKey, null);
            return hotKey;
        }

        public void setKeyCodes(HotKey hotKey, int[] keyCodes) {
            if (hotKey == null) return;
            hotKey.keyCodes = Arrays.copyOf(keyCodes, keyCodes.length);
        }


        public void setHotKeyAction(HotKey hotKey, HotKeyAction hotKeyAction) {
            if (hotKey == null) return;
            hotKey.hotKeyAction = hotKeyAction;
        }

        public void setName(HotKey hotKey, String name) {
            if (hotKey == null) return;
            hotKey.name = Tools.Text.validString(name);
        }

        public void setData(HotKey hotKey, Object data) {
            if (hotKey == null) return;
            hotKey.data = data;
        }

        public String getKeysAsString(HotKey hotKey) {
            String result = "";
            String[] names = new String[hotKey.keyCodes.length];
            for (int i = 0; i < hotKey.keyCodes.length; i++) {
                names[i] = Input.Keys.toString(hotKey.keyCodes[i]);
            }
            return String.join("+", names);
        }
    }


    public class _MouseTool {

        public MouseTool create(String name, Object data, CMediaCursor cursor) {
            return create(name, data, cursor, cursor, null);
        }

        public MouseTool create(String name, Object data, CMediaCursor cursor, CMediaCursor cursorDown) {
            return create(name, data, cursor, cursorDown, null);
        }

        public MouseTool create(String name, Object data, CMediaCursor cursor, MouseToolAction mouseToolAction) {
            return create(name, data, cursor, cursor, mouseToolAction);
        }

        public MouseTool create(String name, Object data, CMediaCursor cursor, CMediaCursor cursorDown, MouseToolAction mouseToolAction) {
            MouseTool mouseTool = new MouseTool();
            setName(mouseTool, name);
            setData(mouseTool, data);
            setCursor(mouseTool, cursor);
            setCursorDown(mouseTool, cursorDown);
            setMouseToolAction(mouseTool, mouseToolAction);
            return mouseTool;
        }

        public void setName(MouseTool mouseTool, String name) {
            if (mouseTool == null) return;
            mouseTool.name = Tools.Text.validString(name);
        }

        public void setData(MouseTool mouseTool, Object data) {
            if (mouseTool == null) return;
            mouseTool.data = data;
        }

        public void setCursor(MouseTool mouseTool, CMediaCursor cursor) {
            if (mouseTool == null) return;
            mouseTool.cursor = cursor;
        }

        public void setCursorDown(MouseTool mouseTool, CMediaCursor cursorDown) {
            if (mouseTool == null) return;
            mouseTool.cursorDown = cursorDown;
        }

        public void setMouseToolAction(MouseTool mouseTool, MouseToolAction mouseToolAction) {
            if (mouseTool == null) return;
            mouseTool.mouseToolAction = mouseToolAction;
        }

    }

    public class _Presets {

        public TextField list_CreateSearchBar(List list) {
            return list_CreateSearchBar(list, null, false, false);
        }

        public TextField list_CreateSearchBar(List list, ScrollBarVertical scrollBarVertical) {
            return list_CreateSearchBar(list, scrollBarVertical, false, false);
        }

        public TextField list_CreateSearchBar(List list, ScrollBarVertical scrollBarVertical, boolean searchTooltips, boolean searchArrayLists) {
            ArrayList originalList = list.items;
            ArrayList itemsSearched = new ArrayList(list.items);
            components.setSize(list, list.width, list.height - 1);
            components.setPosition(list, list.x, list.y + 1);
            if (scrollBarVertical != null) {
                components.setSize(scrollBarVertical, scrollBarVertical.width, scrollBarVertical.height - 1);
                components.setPosition(scrollBarVertical, scrollBarVertical.x, scrollBarVertical.y + 1);
            }
            TextField textField = components.textField.create(list.x, list.y - 1, list.width + 1, "");
            components.textField.setTextFieldAction(textField, new TextFieldAction() {


                @Override
                public void onContentChange(String searchText, boolean valid) {
                    if (valid) {

                        if (searchText.trim().isEmpty()) {
                            components.list.setItems(list, originalList);
                        } else {
                            itemsSearched.clear();
                            searchItems(list, originalList, itemsSearched, searchText, searchTooltips, searchArrayLists);
                            components.list.setItems(list, itemsSearched);
                        }


                    }
                }
            });

            return textField;
        }


        private void searchItems(List list, ArrayList searchList, ArrayList resultList, String searchText, boolean searchTooltips, boolean searchArrayLists) {
            for (Object item : searchList) {
                if (searchArrayLists && item instanceof ArrayList) {
                    searchItems(list, (ArrayList) item, resultList, searchText, searchTooltips, searchArrayLists);
                } else if (list.listAction.text(item).trim().toLowerCase().contains(searchText.trim().toLowerCase())) {
                    resultList.add(item);
                } else if (searchTooltips) {
                    ToolTip tooltip = list.listAction.toolTip(item);
                    if (tooltip != null) {
                        linesLoop:
                        for (String line : tooltip.lines) {
                            if (line.trim().toLowerCase().contains(searchText.trim().toLowerCase())) {
                                resultList.add(item);
                                break linesLoop;
                            }
                        }
                    }
                }
            }
        }

        public Text[][] text_CreateTable(int x, int y, String[] column1Text, int col1Width) {
            Text[][] ret = new Text[2][column1Text.length];

            for (int iy = 0; iy < column1Text.length; iy++) {
                Text text1 = components.text.create(x, y + ((column1Text.length - 1) - iy), Tools.Text.toArray(column1Text[iy]));
                ret[0][iy] = text1;
                Text text2 = components.text.create(x + col1Width, y + (column1Text.length - 1 - iy), new String[]{});
                ret[1][iy] = text2;
            }
            return ret;
        }

        public ArrayList<Component> text_createScrollAbleText(int x, int y, int width, int height, String[] text) {
            ArrayList<Component> result = new ArrayList<>();

            // Cut text to fit
            Text textField = components.text.create(x, y, null);
            components.setSize(textField, width - 1, height);
            ScrollBarVertical scrollBarVertical = components.scrollBar.verticalScrollbar.create(x + width - 1, y, height);
            String[] textAll;
            String[] textDisplayedLines = new String[height];

            // Cut Text to Fit
            if (text != null) {
                ArrayList<String> textList = new ArrayList<>();
                int pixelWidth = ((width - 1) * UIEngine.TILE_SIZE);
                for (int i = 0; i < text.length; i++) {
                    if (text[i].trim().length() > 0) {
                        String[] split = text[i].split(" ");
                        if (split.length > 0) {
                            String currentLine = "";
                            for (int i2 = 0; i2 < split.length; i2++) {
                                if (mediaManager.textWidth(config.getDefaultFont(), currentLine + split[i2] + " ") >= pixelWidth) {
                                    textList.add(currentLine);
                                    currentLine = split[i2] + " ";
                                } else {
                                    currentLine += split[i2] + " ";
                                }
                            }
                            if (currentLine.trim().length() > 0) {
                                textList.add(currentLine);
                            }
                        }
                    } else {
                        textList.add("");
                    }
                }
                textAll = textList.toArray(new String[]{});
            } else {
                textAll = new String[]{};
            }

            // Actions

            components.text.setTextAction(textField, new TextAction() {
                @Override
                public void onMouseScroll(float scrolled) {
                    float scrollAmount = (-1 / (float) Tools.Calc.lowerBounds(text.length, 1)) * input.mouseScrolledAmount();

                    if (!scrollBarVertical.disabled) {
                        components.scrollBar.setScrolled(scrollBarVertical, Tools.Calc.inBounds(
                                scrollBarVertical.scrolled + scrollAmount, 0f, 1f));
                        scrollBarVertical.scrollBarAction.onScrolled(scrollBarVertical.scrolled);
                    }
                }
            });
            components.scrollBar.setScrollBarAction(scrollBarVertical, new ScrollBarAction() {
                @Override
                public void onScrolled(float scrolledPct) {
                    float scrolled = 1f - scrolledPct;

                    int scrolledTextIndex;
                    if (textAll.length > height) {
                        scrolledTextIndex = MathUtils.round((textAll.length - height) * scrolled);
                    } else {
                        scrolledTextIndex = 0;
                    }

                    for (int iy = 0; iy < height; iy++) {
                        int textIndex = scrolledTextIndex + iy;
                        if (textIndex < textAll.length) {
                            textDisplayedLines[iy] = textAll[textIndex];
                        } else {
                            textDisplayedLines[iy] = "";
                        }
                    }
                }
            });

            // Init
            components.scrollBar.setScrolled(scrollBarVertical, 1f);
            if (textAll.length <= height) {
                components.setDisabled(scrollBarVertical, true);
            }

            scrollBarVertical.scrollBarAction.onScrolled(1f);
            components.text.setLines(textField, textDisplayedLines);


            result.add(scrollBarVertical);
            result.add(textField);
            return result;
        }

        public Text text_CreateClickableText(int x, int y, String[] text, Consumer<Integer> onClick) {
            Text hlText = components.text.create(x, y, text, GUIBaseMedia.FONT_BLACK);
            components.text.setTextAction(hlText, new TextAction() {
                @Override
                public void onMouseClick(int button) {
                    onClick.accept(button);
                }
            });
            components.addUpdateAction(hlText, new UpdateAction(0) {
                @Override
                public void onUpdate() {
                    if (Tools.Calc.pointRectsCollide(
                            input.mouseXGUI(),
                            input.mouseYGUI(),
                            components.getAbsoluteX(hlText),
                            components.getAbsoluteY(hlText),
                            hlText.width * UIEngine.TILE_SIZE,
                            hlText.height * UIEngine.TILE_SIZE
                    )) {
                        components.text.setFont(hlText, GUIBaseMedia.FONT_WHITE);
                    } else {
                        components.text.setFont(hlText, GUIBaseMedia.FONT_BLACK);
                    }
                }
            });
            return hlText;
        }


        public HotKeyAction hotkey_CreateForButton(Button button) {
            HotKeyAction hotKeyAction = null;
            if (button.toggleMode) {
                hotKeyAction = new HotKeyAction() {
                    @Override
                    public void onPress() {
                        components.button.setPressed(button, !button.pressed);
                        button.buttonAction.onToggle(button.pressed);
                    }
                };
            } else {
                hotKeyAction = new HotKeyAction() {
                    @Override
                    public void onPress() {
                        button.buttonAction.onPress();
                        components.button.setPressed(button, true);
                    }

                    @Override
                    public void onRelease() {
                        button.buttonAction.onRelease();
                        components.button.setPressed(button, false);
                    }
                };
            }

            return hotKeyAction;
        }

        public void checkbox_MakeExclusive(CheckBox[] checkboxes, Consumer<CheckBox> checkedFunction) {
            for (CheckBox checkbox : checkboxes) {
                components.checkBox.setCheckBoxAction(checkbox, new CheckBoxAction() {
                    @Override
                    public void onCheck(boolean checked) {
                        if (checked) {
                            for (CheckBox checkbox2 : checkboxes) {
                                if (checkbox2 != checkbox) components.checkBox.setChecked(checkbox2, false);
                            }
                            checkedFunction.accept(checkbox);
                        } else {
                            components.checkBox.setChecked(checkbox, true);
                        }

                    }
                });
            }
        }

        public ScrollBarVertical list_CreateScrollBar(List list) {
            ScrollBarVertical scrollBarVertical = components.scrollBar.verticalScrollbar.create(list.x + list.width, list.y, list.height, new ScrollBarAction() {
                @Override
                public void onScrolled(float scrolled) {
                    components.list.setScrolled(list, 1f - scrolled);
                }
            });

            components.setOffset(scrollBarVertical, list.offset_x, list.offset_y);

            components.addUpdateAction(scrollBarVertical, new UpdateAction() {
                float scrolledLast = -1;

                @Override
                public void onUpdate() {
                    if (scrolledLast != list.scrolled) {
                        components.scrollBar.setScrolled(scrollBarVertical, 1 - list.scrolled);
                        scrolledLast = list.scrolled;
                    }
                    // disable scrollbar
                    if (list.items.size() <= list.height) {
                        components.setDisabled(scrollBarVertical, true);
                        components.scrollBar.setScrolled(scrollBarVertical, 1f);
                    } else {
                        components.setDisabled(scrollBarVertical, false);
                    }
                }
            });
            return scrollBarVertical;
        }

        public ArrayList<Component> image_CreateSeparatorHorizontal(int x, int y, int size) {
            ArrayList<Component> returnComponents = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int index = i == 0 ? 0 : i == (size - 1) ? 2 : 1;
                Image image = components.image.create(x + i, y, GUIBaseMedia.GUI_SEPARATOR_HORIZONTAL, index);
                components.setColor(image, config.componentsDefaultColor);
                returnComponents.add(image);
            }
            return returnComponents;
        }

        public ArrayList<Component> image_CreateSeparatorVertical(int x, int y, int size) {
            ArrayList<Component> returnComponents = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int index = i == 0 ? 1 : i == (size - 1) ? 0 : 1;
                Image image = components.image.create(x, y + i, GUIBaseMedia.GUI_SEPARATOR_VERTICAL, index);
                components.setColor(image, config.componentsDefaultColor);
                returnComponents.add(image);
            }
            return returnComponents;
        }

        public Window modal_CreateColorRequester(String caption, Consumer<FColor> selectColorFunction, FColor initColor) {
            return modal_CreateColorRequester(caption, selectColorFunction, initColor, GUIBaseMedia.GUI_COLOR_SELECTOR);
        }

        public Window modal_CreateColorRequester(String caption, Consumer<FColor> selectColorFunction, FColor initColor, CMediaImage colors) {

            TextureRegion colorTexture = mediaManager.getCMediaImage(colors);

            final int colorTextureWidthTiles = colorTexture.getRegionWidth() / 8;
            final int colorTextureHeightTiles = colorTexture.getRegionHeight() / 8;

            Window modal = windows.create(0, 0, colorTextureWidthTiles + 1, colorTextureHeightTiles + 4, caption, GUIBaseMedia.GUI_ICON_COLOR);
            ImageButton closeButton = presets.button_CreateWindowCloseButton(modal);
            components.button.setButtonAction(closeButton, new ButtonAction() {
                @Override
                public void onRelease() {
                    selectColorFunction.accept(null);
                    removeCurrentModalWindow();
                }
            });
            windows.addComponent(modal, closeButton);

            TextButton ok = components.button.textButton.create(0, 0, colorTextureWidthTiles, 1, "OK", null);
            components.button.setButtonAction(ok, new ButtonAction() {
                @Override
                public void onRelease() {
                    selectColorFunction.accept(Tools.Colors.create(ok.color_r, ok.color_g, ok.color_b));
                    removeCurrentModalWindow();
                }
            });
            components.setColor(ok, initColor);


            Map colorMap = components.map.create(0, 2, colorTextureWidthTiles, colorTextureHeightTiles);


            MapOverlay cursorOverlay = components.map.mapOverlay.create(GUIBaseMedia.GUI_COLOR_SELECTOR_OVERLAY, UIEngine.TILE_SIZE * 8, UIEngine.TILE_SIZE * 4, false);
            components.map.addOverlay(colorMap, cursorOverlay);


            if (!colorTexture.getTexture().getTextureData().isPrepared())
                colorTexture.getTexture().getTextureData().prepare();
            Pixmap pixmap = colorTexture.getTexture().getTextureData().consumePixmap();

            Color pixelColor = new Color();
            for (int x = 0; x < colorTexture.getRegionWidth(); x++) {
                for (int y = 0; y < colorTexture.getRegionHeight(); y++) {
                    pixelColor.set(pixmap.getPixel(colorTexture.getRegionX() + x, colorTexture.getRegionY() + y));
                    components.map.drawPixel(colorMap, x, y, pixelColor.r, pixelColor.g, pixelColor.b, 1f);
                    if (initColor != null && pixelColor.r == initColor.r && pixelColor.g == initColor.g && pixelColor.b == initColor.b) {
                        components.map.mapOverlay.setPosition(cursorOverlay, x, colorTexture.getRegionHeight() - y);
                    }

                }
            }

            components.map.update(colorMap);

            final boolean[] drag = {false};
            components.map.setMapAction(colorMap, new MapAction() {

                @Override
                public void onPress(int x, int y) {
                    drag[0] = true;
                }

                @Override
                public void onRelease() {
                    drag[0] = false;
                }
            });
            components.addUpdateAction(colorMap, new UpdateAction(10, true) {
                int xLast = 0, yLast = 0;

                @Override
                public void onUpdate() {
                    if (drag[0]) {
                        int x = input.mouseXGUI() - components.getAbsoluteX(colorMap);
                        int yInv = (input.mouseYGUI() - components.getAbsoluteY(colorMap));
                        int y = colorTexture.getRegionHeight() - yInv;
                        if (x < 0 || y < 0 || x > colorTexture.getRegionWidth() || y > colorTexture.getRegionHeight()) {
                            return;
                        }
                        if (x != xLast || y != yLast) {

                            components.setColor(ok, components.map.getPixel(colorMap, x, y));
                            components.button.textButton.setFont(ok, Tools.Colors.getBrightness(Tools.Colors.create(ok.color_r, ok.color_g, ok.color_b)) < 0.5 ? GUIBaseMedia.FONT_WHITE : GUIBaseMedia.FONT_BLACK);

                            components.map.mapOverlay.setPosition(cursorOverlay, x - 2, yInv - 2);
                            xLast = x;
                            yLast = y;
                        }
                    }
                }
            });


            Component[] componentl = new Component[]{colorMap, ok};
            components.setOffset(ok, UIEngine.TILE_SIZE / 2, UIEngine.TILE_SIZE / 2);
            components.setOffset(colorMap, UIEngine.TILE_SIZE / 2, UIEngine.TILE_SIZE / 2);
            windows.addComponents(modal, componentl);

            return modal;
        }

        public Window modal_CreateTextInput(String caption, String text, String originalText, Consumer<String> inputResultFunction) {
            return modal_CreateTextInput(caption, text, originalText, inputResultFunction, Integer.MAX_VALUE, 0);
        }

        public Window modal_CreateTextInput(String caption, String text, String originalText, Consumer<String> inputResultFunction, int maxInputLength) {
            return modal_CreateTextInput(caption, text, originalText, inputResultFunction, maxInputLength, 0);

        }

        public Window modal_CreateTextInput(String caption, String text, String originalText, Consumer<String> inputResultFunction, int maxInputLength, int wndMinWidth) {
            final int WIDTH = Tools.Calc.lowerBounds(MathUtils.round(mediaManager.textWidth(config.defaultFont, text) / (float) UIEngine.TILE_SIZE) + 2, Tools.Calc.lowerBounds(wndMinWidth, 8));
            final int HEIGHT = 6;
            Window modal = windows.create(0, 0, WIDTH, HEIGHT, caption, GUIBaseMedia.GUI_ICON_INFORMATION);
            Text textC = components.text.create(0, 3, Tools.Text.toArray(text));
            TextField input = components.textField.create(0, 2, WIDTH - 1, originalText != null ? originalText : "", null, maxInputLength);


            components.textField.setTextFieldAction(input, new TextFieldAction() {
                @Override
                public void onTyped(char character) {
                    if (character == '\n') {
                        inputResultFunction.accept(input.content);
                        removeCurrentModalWindow();
                    }
                }
            });

            Button okC = components.button.textButton.create(0, 0, WIDTH - 1, 1, "OK", new ButtonAction() {
                @Override
                public void onRelease() {
                    inputResultFunction.accept(input.content);
                    removeCurrentModalWindow();
                }
            });


            Component[] componentsl = new Component[]{textC, okC, input};
            components.setOffset(componentsl, UIEngine.TILE_SIZE / 2, UIEngine.TILE_SIZE / 2);
            components.setOffset(input, UIEngine.TILE_SIZE / 2, 0);
            windows.addComponents(modal, componentsl);

            components.textField.focus(input);
            return modal;
        }

        public Window modal_CreateMessageRequester(String caption, String[] lines, Runnable closeFunction) {
            int longest = 0;
            for (String line : lines) {
                int len = mediaManager.textWidth(config.defaultFont, line);
                if (len > longest) {
                    longest = len;
                }
            }
            final int WIDTH = Tools.Calc.lowerBounds(MathUtils.round(longest / (float) UIEngine.TILE_SIZE) + 2, 12);
            final int HEIGHT = 4 + lines.length;
            Window modal = windows.create(0, 0, WIDTH, HEIGHT, caption, GUIBaseMedia.GUI_ICON_INFORMATION);

            Text[] texts = new Text[lines.length];
            for (int i = 0; i < lines.length; i++) {
                texts[i] = components.text.create(0, HEIGHT - 3 - i, Tools.Text.toArray(lines[i]));
            }

            Button okBtn = components.button.textButton.create(0, 0, WIDTH - 1, 1, "OK", new ButtonAction() {
                @Override
                public void onRelease() {
                    if (closeFunction != null) {
                        closeFunction.run();
                    }
                    removeCurrentModalWindow();
                }
            });
            components.button.centerContent(okBtn);

            ArrayList<Component> componentsl = new ArrayList<>();
            componentsl.addAll(Arrays.asList(texts));
            componentsl.add(okBtn);

            components.setOffset(componentsl, UIEngine.TILE_SIZE / 2, UIEngine.TILE_SIZE / 2);
            windows.addComponents(modal, componentsl);
            return modal;
        }

        public GraphInfo map_drawGraph(Map map, int itemCount, Function<Integer, Long> getIndexValue) {
            return map_drawGraph(map, itemCount, 1, 1, getIndexValue, Tools.Colors.WHITE, Tools.Colors.GREEN_BRIGHT, Tools.Colors.ORANGE_BRIGHT, Tools.Colors.RED_BRIGHT, null, true);
        }

        public GraphInfo map_drawGraph(Map map, int itemCount, int steps, int stepSize, Function<Integer, Long> getValueAtIndex, FColor colorBackGround, FColor colorRising, FColor colorEven, FColor colorFalling, int[] hiAndLowValueReference, boolean drawBackGroundLines) {
            int mapWidth = map.width * UIEngine.TILE_SIZE;
            int mapHeight = map.height * UIEngine.TILE_SIZE;
            int[] indexAtPosition = new int[mapWidth];
            long[] valueAtPosition = new long[mapWidth];
            long lowestValue = Integer.MAX_VALUE;
            long highestValue = Integer.MIN_VALUE;
            // Get Values
            ArrayList<Integer> indexes = new ArrayList<>();
            ArrayList<Long> values = new ArrayList<>();
            int startIndex = (itemCount - 1) - (steps * stepSize);
            int indexAndValueCount = 0;
            long valueBefore = (startIndex - stepSize) > 0 ? getValueAtIndex.apply((startIndex - stepSize)) : Long.MIN_VALUE;
            for (int i = startIndex; i < itemCount; i += stepSize) {
                if (i > 0) {
                    long value = getValueAtIndex.apply(i);
                    lowestValue = value < lowestValue ? value : lowestValue;
                    highestValue = value > highestValue ? value : highestValue;
                    indexes.add(i);
                    values.add(value);
                    indexAndValueCount++;
                }
            }
            long loReference = hiAndLowValueReference != null && hiAndLowValueReference.length == 2 ? hiAndLowValueReference[0] : lowestValue;
            long hiReference = hiAndLowValueReference != null && hiAndLowValueReference.length == 2 ? hiAndLowValueReference[1] : highestValue;


            // Draw Background
            FColor colorBackGroundDarker = Tools.Colors.createDarker(colorBackGround, 0.02f);
            for (int iy = 0; iy < mapHeight; iy++) {
                FColor color = drawBackGroundLines ? (iy % 4 == 0 ? colorBackGroundDarker : colorBackGround) : colorBackGround;
                for (int ix = 0; ix < mapWidth; ix++) {
                    components.map.drawPixel(map, ix, iy, color.r, color.g, color.b, color.a);
                }
            }

            if (values.size() == 0) {
                // No values available
                components.map.update(map);
                return null;
            }

            // Calculate index/value at position
            for (int ix = 0; ix < mapWidth; ix++) {
                int indexAndValueIndex = MathUtils.round((ix / (float) mapWidth) * (indexAndValueCount - 1));
                indexAtPosition[ix] = indexes.get(indexAndValueIndex);
                valueAtPosition[ix] = values.get(indexAndValueIndex);
            }

            // Draw Bars
            long lastValue = valueBefore;
            int lastIndex = -1;
            final float SHADING = 0.1f;
            FColor color = colorEven;
            FColor colorBrighter = Tools.Colors.createBrighter(color, SHADING);
            FColor colorDarker = Tools.Colors.createDarker(color, SHADING);
            for (int ix = 0; ix < mapWidth; ix++) {
                int index = indexAtPosition[ix];
                long value = valueAtPosition[ix];
                boolean indexChange = false;
                boolean nextIndexChange = (ix + 1) < mapWidth ? indexAtPosition[ix + 1] != index : false;
                if (index != lastIndex) {
                    if (value > lastValue) {
                        color = colorRising;
                    } else if (value < lastValue) {
                        color = colorFalling;
                    } else {
                        color = colorEven;
                    }
                    colorBrighter = Tools.Colors.createBrighter(color, SHADING);
                    colorDarker = Tools.Colors.createDarker(color, SHADING);
                    indexChange = true;
                    lastIndex = index;
                }


                float heightPct = (value - loReference) / (float) (hiReference - loReference);
                int heightPixels = Tools.Calc.lowerBounds(MathUtils.round(mapHeight * heightPct), 2);
                for (int iy = 0; iy < heightPixels; iy++) {
                    int y = mapHeight - iy;
                    if (iy == heightPixels - 1) {
                        components.map.drawPixel(map, ix, y, colorBrighter.r, colorBrighter.g, colorBrighter.b, colorBrighter.a);
                    } else {
                        components.map.drawPixel(map, ix, y, color.r, color.g, color.b, color.a);
                    }
                }

                // Draw Shading
                if (indexChange && ix != 0) {
                    for (int iy = 0; iy < heightPixels; iy++) {
                        int y = mapHeight - iy;
                        components.map.drawPixel(map, ix, y, colorBrighter.r, colorBrighter.g, colorBrighter.b, colorBrighter.a);
                    }
                } else if (nextIndexChange) {
                    for (int iy = 0; iy < heightPixels; iy++) {
                        int y = mapHeight - iy;
                        components.map.drawPixel(map, ix, y, colorDarker.r, colorDarker.g, colorDarker.b, colorDarker.a);
                    }
                }


                lastValue = value;
            }

            components.map.update(map);
            return new GraphInfo(lowestValue, highestValue, indexAtPosition, valueAtPosition);
        }

        public Window modal_CreateYesNoRequester(String caption, String text, Consumer<Boolean> choiceFunction) {
            return modal_CreateYesNoRequester(caption, text, choiceFunction, "Yes", "No");
        }

        public Window modal_CreateYesNoRequester(String caption, String text, Consumer<Boolean> choiceFunction, String yes, String no) {

            int textWidthMin = Math.max(
                    (mediaManager.textWidth(config.defaultFont, caption) + 8),
                    mediaManager.textWidth(config.defaultFont, text)
            );

            int width = Tools.Calc.lowerBounds(MathUtils.round(textWidthMin / (float) UIEngine.TILE_SIZE) + 2, 12);
            if (width % 2 == 0) width++;
            Window modal = windows.create(0, 0, width, 5, caption, GUIBaseMedia.GUI_ICON_QUESTION);

            int width1 = MathUtils.round(width / 2f) - 1;
            int width2 = width - width1 - 1;

            Text textC = components.text.create(0, 2, new String[]{text});
            int xOffset = 0;
            Button yesC = components.button.textButton.create(xOffset, 0, width1, 1, yes, new ButtonAction() {
                @Override
                public void onRelease() {
                    if (choiceFunction != null) choiceFunction.accept(true);
                    removeCurrentModalWindow();
                }
            });
            components.button.centerContent(yesC);
            xOffset += width1;
            Button noC = components.button.textButton.create(xOffset, 0, width2, 1, no, new ButtonAction() {
                @Override
                public void onRelease() {
                    if (choiceFunction != null) choiceFunction.accept(false);
                    removeCurrentModalWindow();
                }
            });
            components.button.centerContent(noC);

            Component[] componentsl = new Component[]{textC, yesC, noC};
            components.setOffset(componentsl, UIEngine.TILE_SIZE / 2, UIEngine.TILE_SIZE / 2);
            windows.addComponents(modal, componentsl);
            return modal;
        }

        public ImageButton button_CreateWindowCloseButton(Window window) {
            return button_CreateWindowCloseButton(window, null);
        }

        public ImageButton button_CreateWindowCloseButton(Window window, Consumer<Window> closeFunction) {
            ImageButton closeButton = components.button.imageButton.create(window.width - 1, window.height - 1, 1, 1, GUIBaseMedia.GUI_ICON_CLOSE);
            components.setName(closeButton, "WndCloseBtn");
            components.button.setButtonAction(closeButton, new ButtonAction() {

                @Override
                public void onRelease() {
                    removeWindow(window);
                    if (closeFunction != null) closeFunction.accept(window);
                }
            });
            return closeButton;
        }

        public TextField textField_createDecimalInputField(int x, int y, int width, float min, float max, Consumer<Float> onChange) {
            TextField textField = components.textField.create(x, y, width);
            HashSet<Character> numbers = new HashSet<>();
            numbers.addAll(Arrays.asList(new Character[]{'-', ',', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'}));
            components.textField.setAllowedCharacters(textField, numbers);
            components.textField.setTextFieldAction(textField, new TextFieldAction() {
                @Override
                public boolean isContentValid(String newContent) {
                    Float value = null;
                    try {
                        value = Float.parseFloat(newContent);
                    } catch (Exception e) {
                    }
                    if (value != null && value >= min && value <= max) {
                        return true;
                    }
                    return false;
                }

                @Override
                public void onEnter(String content, boolean valid) {
                    if (textField.contentValid) onChange.accept(Float.parseFloat(content));
                }
            });
            return textField;
        }

        public TextField textField_createIntegerInputField(int x, int y, int width, int min, int max, Consumer<Integer> onChange) {
            TextField textField = components.textField.create(x, y, width);
            HashSet<Character> numbers = new HashSet<>();
            numbers.addAll(Arrays.asList(new Character[]{'-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'}));
            components.textField.setAllowedCharacters(textField, numbers);
            components.textField.setTextFieldAction(textField, new TextFieldAction() {
                @Override
                public boolean isContentValid(String newContent) {
                    Integer value = null;
                    try {
                        value = Integer.parseInt(newContent);
                    } catch (Exception e) {
                    }
                    if (value != null && value >= min && value <= max) {
                        return true;
                    }
                    return false;
                }

                @Override
                public void onEnter(String content, boolean valid) {
                    if (textField.contentValid) onChange.accept(Integer.parseInt(content));
                }
            });
            return textField;
        }

        public ArrayList<Component> image_createBorder(int x, int y, int width, int height) {
            return image_createBorder(x, y, width, height, 0);
        }

        public ArrayList<Component> image_createBorder(int x, int y, int width, int height, int gap) {
            ArrayList<Component> borders = new ArrayList<>();
            width = Tools.Calc.lowerBounds(width, 1);
            height = Tools.Calc.lowerBounds(height, 1);


            for (int ix = 0; ix < width; ix++) {

                borders.add(components.image.create(x + ix, y, GUIBaseMedia.GUI_BORDERS, 2));

                if (ix >= gap) {
                    borders.add(components.image.create(x + ix, y + (height - 1), GUIBaseMedia.GUI_BORDERS, 3));
                }
            }

            for (int iy = 0; iy < height; iy++) {
                borders.add(components.image.create(x, y + iy, GUIBaseMedia.GUI_BORDERS, 0));
                borders.add(components.image.create(x + (width - 1), y + iy, GUIBaseMedia.GUI_BORDERS, 1));
            }

            return borders;
        }


        public ArrayList<Component> tabBar_createExtendableTabBar(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight) {
            ArrayList<Component> ret = new ArrayList<>();

            width = Tools.Calc.lowerBounds(width, 1);
            TabBar tabBar = components.tabBar.create(x, y, width, tabs, selectedTab, tabBarAction, border, borderHeight, 2);
            ImageButton extendButton = components.button.imageButton.create(x, y, 2, 1, GUIBaseMedia.GUI_ICON_EXTEND);

            updateExtendableTabBarButton(tabBar, extendButton);

            ret.add(extendButton);
            ret.add(tabBar);

            return ret;
        }

        private void updateExtendableTabBarButton(TabBar tabBar, ImageButton extendButton) {
            ArrayList<Tab> invisibleTabs = new ArrayList<>();
            for (Tab tab : tabBar.tabs) {
                if (!components.tabBar.isTabVisible(tabBar, tab)) {
                    invisibleTabs.add(tab);
                }
            }
            if (invisibleTabs.size() > 0) {
                components.button.setButtonAction(extendButton, new ButtonAction() {
                    @Override
                    public void onRelease() {
                        ArrayList<ContextMenuItem> contextMenuItems = new ArrayList<>();
                        for (Tab tab : invisibleTabs) {
                            contextMenuItems.add(contextMenu.item.create(tab.title, new ContextMenuItemAction() {
                                @Override
                                public void onSelect() {
                                    components.tabBar.removeTab(tabBar, tab);
                                    components.tabBar.addTab(tabBar, tab, 0);
                                    components.tabBar.selectTab(tabBar, 0);
                                    updateExtendableTabBarButton(tabBar, extendButton);
                                }
                            }, tab.icon));
                        }

                        ContextMenu selectTabMenu = contextMenu.create(contextMenuItems.toArray(new ContextMenuItem[0]));
                        openContextMenu(selectTabMenu);
                    }
                });
                components.setDisabled(extendButton, false);

            } else {
                components.button.setButtonAction(extendButton, null);
                components.setDisabled(extendButton, true);
            }


        }

    }


    public void removeNotification(Notification notification) {
        inputState.notifications.remove(notification);
    }

    public void removeAllNotifications() {
        inputState.notifications.clear();
    }

    public ArrayList<Notification> getNotifications() {
        return inputState.notifications;
    }

    public void openContextMenu(ContextMenu contextMenu) {
        openContextMenu(contextMenu, inputState.mouse_x_gui, inputState.mouse_y_gui);
    }

    public void openContextMenu(ContextMenu contextMenu, int x, int y) {
        if (contextMenu == null) return;
        inputState.displayedContextMenu = contextMenu;
        inputState.displayedContextMenu.x = x;
        inputState.displayedContextMenu.y = y;
        int textwidth = 0;
        for (ContextMenuItem item : inputState.displayedContextMenu.items) {
            int w = mediaManager.textWidth(item.font, item.text);
            if (item.icon != null) w = w + UIEngine.TILE_SIZE;
            if (w > textwidth) textwidth = w;
        }
        inputState.displayedContextMenuWidth = (textwidth + UIEngine.TILE_SIZE) / UIEngine.TILE_SIZE;
    }

    public void closeContextMenu() {
        inputState.displayedContextMenu = null;
        inputState.displayedContextMenuWidth = 0;
    }

    public ContextMenu getContextMenu() {
        return inputState.displayedContextMenu;
    }

    public ArrayList<Window> getWindows() {
        return inputState.windows;
    }

    public ArrayList<Component> getScreenComponents() {
        return inputState.screenComponents;
    }

    public void freezeGUI() {
        inputState.guiFrozen = true;
    }

    public boolean isGUIFrozen() {
        return inputState.guiFrozen;
    }

    public void unfreezeGUI() {
        inputState.guiFrozen = false;
    }

    public Window getModalWindow() {
        return inputState.modalWindow;
    }

    public void addWindow(Window window) {
        if (window == null) return;
        inputState.addWindowQueue.add(window);
    }

    public void removeWindow(Window window) {
        if (inputState.windows.contains(window)) {
            inputState.removeWindowQueue.add(window);
        }
    }

    public void removeAllWindows() {
        for (Window window : inputState.windows) removeWindow(window);
        UICommons.resetGUIVariables(inputState);
    }

    public boolean closeWindow(Window window) {
        if (window == null) return false;
        ArrayList<Component> result = windows.findComponentsByName(window, "WndCloseBtn");
        if (result.size() == 1) {
            if (result.get(0) instanceof Button) {
                Button button = (Button) result.get(0);
                if (button.buttonAction != null) {
                    button.buttonAction.onPress();
                    button.buttonAction.onRelease();
                    return true;
                }
            }
        }
        return false;
    }

    public void closeWindows(Window[] windows) {
        if (windows == null) return;
        for (Window window : inputState.windows) closeWindow(window);
    }

    public void closeAllWindows() {
        for (Window window : inputState.windows) {
            closeWindow(window);
        }
        UICommons.resetGUIVariables(inputState);
    }

    public void addWindowAsModal(Window modalWindow) {
        if (modalWindow == null) return;
        if (inputState.modalWindow == null) {
            UICommons.resetGUIVariables(inputState);
            windows.setAlwaysOnTop(modalWindow, true);
            windows.setVisible(modalWindow, true);
            windows.setFolded(modalWindow, false);
            windows.center(modalWindow);
            windows.setEnforceScreenBounds(modalWindow, true);
            inputState.modalWindow = modalWindow;
            addWindow(modalWindow);
        } else {
            inputState.modalWindowQueue.add(modalWindow);
        }

    }

    public void removeCurrentModalWindow() {
        if (inputState.modalWindow != null) {
            removeWindow(inputState.modalWindow);
            inputState.modalWindow = null;
            addNextModal();
        }
    }

    public boolean closeCurrentModalWindow() {
        if (inputState.modalWindow != null) {
            if (closeWindow(inputState.modalWindow)) {
                inputState.modalWindow = null;
                addNextModal();
                return true;
            }
        }
        return false;
    }

    private void addNextModal() {
        if (inputState.modalWindow == null && inputState.modalWindowQueue.size() > 0) {
            addWindowAsModal(inputState.modalWindowQueue.pollFirst());
        }
    }

    public void addScreenComponent(Component component) {
        if (component == null) return;
        if (!inputState.screenComponents.contains(component)) {
            inputState.addScreenComponentsQueue.add(component);
        }
    }

    public void addScreenComponents(Component[] components) {
        if (components == null) return;
        for (Component component : components) addScreenComponent(component);
    }

    public void removeScreenComponent(Component component) {
        if (component == null) return;
        inputState.removeScreenComponentsQueue.remove(component);
    }

    public void removeScreenComponents(Component[] components) {
        if (components == null) return;
        for (Component component : components) {
            removeScreenComponent(component);
        }
    }

    public void removeAllScreenComponents() {
        for (Component component : inputState.screenComponents) removeScreenComponent(component);
        UICommons.resetGUIVariables(inputState);
    }

    public void removeEverything() {
        removeAllWindows();
        removeAllScreenComponents();
        removeAllNotifications();
        UICommons.resetGUIVariables(inputState);
    }

    public void setMouseTool(MouseTool mouseTool) {
        inputState.mouseTool = mouseTool;
    }

    public void displayTemporaryCursor(CMediaCursor temporaryCursor) {
        if (temporaryCursor == null) return;
        inputState.temporaryCursor = temporaryCursor;
        inputState.displayTemporaryCursor = true;
    }

    public MouseTool getMouseTool() {
        return inputState.mouseTool;
    }

    public boolean isMouseTool(String name) {
        if (name == null) return false;
        return inputState.mouseTool != null ? name.equals(inputState.mouseTool.name) : false;
    }

    public ArrayList<HotKey> getHotKeys() {
        return inputState.hotKeys;
    }

    public void addHotKey(HotKey hotKey) {
        if (hotKey == null) return;
        inputState.addHotKeyQueue.add(hotKey);
        return;
    }

    public void removeHotKey(HotKey hotKey) {
        if (hotKey == null) return;
        inputState.removeHotKeyQueue.remove(hotKey);
    }

    public ArrayList<HotKey> findHotKeysByName(String name) {
        ArrayList<HotKey> result = new ArrayList<>();
        if (name == null) return result;
        for (HotKey hotKey : inputState.hotKeys) {
            if (name.equals(hotKey.name)) {
                result.add(hotKey);
            }
        }
        return result;
    }

    public HotKey findHotKeyByName(String name) {
        if (name == null) return null;
        ArrayList<HotKey> result = findHotKeysByName(name);
        return result.size() > 0 ? result.get(0) : null;
    }


    public void removeAllHotKeys() {
        for (HotKey hotKey : inputState.hotKeys) removeHotKey(hotKey);
    }

    public void removeHotKeys(HotKey[] hotKeys) {
        if (hotKeys == null) return;
        for (HotKey hotKey : hotKeys) removeHotKey(hotKey);
    }

    public ArrayList<Window> findWindowsByName(String name) {
        ArrayList<Window> result = new ArrayList<>();
        if (name == null) return result;
        for (Window window : inputState.windows) {
            if (name.equals(window.name)) {
                result.add(window);
            }
        }
        return result;
    }

    public Window findWindowByName(String name) {
        if (name == null) return null;
        ArrayList<Window> result = findWindowsByName(name);
        return result.size() > 0 ? result.get(0) : null;
    }

    public Window getLastActiveWindow() {
        return inputState.lastActiveWindow;
    }

    public class _Config {

        private boolean windowsDefaultEnforceScreenBounds = false;
        private FColor windowsDefaultColor = Tools.Colors.WHITE;
        private FColor componentsDefaultColor = Tools.Colors.WHITE;
        private FColor tooltipDefaultColor = Tools.Colors.WHITE;

        private FColor contextMenuDefaultColor = Tools.Colors.WHITE;

        private CMediaCursor cursorGui = GUIBaseMedia.GUI_CURSOR_SYSTEM_ARROW;
        private int gameviewportDefaultUpdateTime = 200;
        private CMediaFont tooltipDefaultFont = GUIBaseMedia.FONT_BLACK;
        private CMediaFont defaultFont = GUIBaseMedia.FONT_BLACK;
        private float dragTransparency = 0.8f;
        private int buttonHoldTimer = 8;
        private float knobSensitivity = 0.7f;
        private boolean foldWindowsOnDoubleClick = true;
        private int notificationsMax = 20;
        private int notificationsDefaultDisplayTime = 3000;
        private CMediaFont notificationsDefaultFont = GUIBaseMedia.FONT_WHITE;
        private FColor notificationsDefaultColor = Tools.Colors.create(0, 0, 0, 1f);
        private int notificationsFadeoutTime = 200;
        private float notificationsScrollSpeed = 1;
        private int mapOverlayDefaultFadeoutTime = 200;
        private HashSet<Character> textFieldDefaultAllowedCharacters = new HashSet();
        private int tooltipFadeInTime = 50;
        private int tooltipFadeInDelayTime = 25;

        public boolean isWindowsDefaultEnforceScreenBounds() {
            return windowsDefaultEnforceScreenBounds;
        }

        public boolean getWindowsDefaultEnforceScreenBounds() {
            return windowsDefaultEnforceScreenBounds;
        }

        public void setWindowsDefaultEnforceScreenBounds(boolean windowsDefaultEnforceScreenBounds) {
            this.windowsDefaultEnforceScreenBounds = windowsDefaultEnforceScreenBounds;
        }

        public FColor getWindowsDefaultColor() {
            return windowsDefaultColor;
        }

        public void setWindowsDefaultColor(FColor windowsDefaultColor) {
            if (windowsDefaultColor == null) return;
            this.windowsDefaultColor = Tools.Colors.create(windowsDefaultColor);
        }

        public FColor getComponentsDefaultColor() {
            return componentsDefaultColor;
        }

        public void setComponentsDefaultColor(FColor componentsDefaultColor) {
            if (componentsDefaultColor == null) return;
            this.componentsDefaultColor = Tools.Colors.create(componentsDefaultColor);
        }

        public FColor getTooltipDefaultColor() {
            return tooltipDefaultColor;
        }

        public void setTooltipDefaultColor(FColor tooltipDefaultColor) {
            if (tooltipDefaultColor == null) return;
            this.tooltipDefaultColor = Tools.Colors.create(tooltipDefaultColor);
        }

        public CMediaCursor getCursorGui() {
            return cursorGui;
        }

        public void setCursorGui(CMediaCursor cursorGui) {
            if (cursorGui == null) return;
            ;
            this.cursorGui = cursorGui;
        }

        public int getGameviewportDefaultUpdateTime() {
            return gameviewportDefaultUpdateTime;
        }

        public void setGameviewportDefaultUpdateTime(int gameviewportDefaultUpdateTime) {
            this.gameviewportDefaultUpdateTime = Tools.Calc.lowerBounds(gameviewportDefaultUpdateTime, 0);
        }

        public CMediaFont getTooltipDefaultFont() {
            return tooltipDefaultFont;
        }

        public void setTooltipDefaultFont(CMediaFont tooltipDefaultFont) {
            if (tooltipDefaultFont == null) return;
            this.tooltipDefaultFont = tooltipDefaultFont;
        }

        public CMediaFont getDefaultFont() {
            return defaultFont;
        }

        public void setDefaultFont(CMediaFont defaultFont) {
            if (defaultFont == null) return;
            this.defaultFont = defaultFont;
        }

        public float getDragTransparency() {
            return dragTransparency;
        }

        public void setDragTransparency(float dragTransparency) {
            this.dragTransparency = Tools.Calc.inBounds(dragTransparency, 0f, 1f);
        }

        public int getButtonHoldTimer() {
            return buttonHoldTimer;
        }

        public void setButtonHoldTimer(int buttonHoldTimer) {
            this.buttonHoldTimer = Tools.Calc.lowerBounds(buttonHoldTimer, 0);
        }

        public float getKnobSensitivity() {
            return knobSensitivity;
        }

        public void setKnobSensitivity(float knobSensitivity) {
            this.knobSensitivity = Tools.Calc.inBounds(knobSensitivity, 0f, 1f);
        }

        public boolean isFoldWindowsOnDoubleClick() {
            return foldWindowsOnDoubleClick;
        }

        public void setFoldWindowsOnDoubleClick(boolean foldWindowsOnDoubleClick) {
            this.foldWindowsOnDoubleClick = foldWindowsOnDoubleClick;
        }

        public int getNotificationsMax() {
            return notificationsMax;
        }

        public void setNotificationsMax(int notificationsMax) {
            this.notificationsMax = Tools.Calc.lowerBounds(notificationsMax, 0);
        }

        public int getNotificationsDefaultDisplayTime() {
            return notificationsDefaultDisplayTime;
        }

        public void setNotificationsDefaultDisplayTime(int notificationsDefaultDisplayTime) {
            this.notificationsDefaultDisplayTime = Tools.Calc.lowerBounds(notificationsDefaultDisplayTime, 0);
        }

        public CMediaFont getNotificationsDefaultFont() {
            return notificationsDefaultFont;
        }

        public void setNotificationsDefaultFont(CMediaFont notificationsDefaultFont) {
            if (notificationsDefaultFont == null) return;
            this.notificationsDefaultFont = notificationsDefaultFont;
        }

        public FColor getNotificationsDefaultColor() {
            return notificationsDefaultColor;
        }

        public void setNotificationsDefaultColor(FColor notificationsDefaultColor) {
            if (notificationsDefaultColor == null) return;
            this.notificationsDefaultColor = Tools.Colors.create(notificationsDefaultColor);
        }

        public int getNotificationsFadeoutTime() {
            return notificationsFadeoutTime;
        }

        public void setNotificationsFadeoutTime(int notificationsFadeoutTime) {
            this.notificationsFadeoutTime = Tools.Calc.lowerBounds(notificationsFadeoutTime, 0);
        }

        public float getNotificationsScrollSpeed() {
            return notificationsScrollSpeed;
        }

        public void setNotificationsScrollSpeed(float notificationsScrollSpeed) {
            this.notificationsScrollSpeed = Tools.Calc.lowerBounds(notificationsScrollSpeed, 0.1f);
        }

        public int getMapOverlayDefaultFadeoutTime() {
            return mapOverlayDefaultFadeoutTime;
        }

        public void setMapOverlayDefaultFadeoutTime(int mapOverlayDefaultFadeoutTime) {
            this.mapOverlayDefaultFadeoutTime = Tools.Calc.lowerBounds(mapOverlayDefaultFadeoutTime, 0);
        }

        public HashSet<Character> getTextFieldDefaultAllowedCharacters() {
            return textFieldDefaultAllowedCharacters;
        }

        public void setTextFieldDefaultAllowedCharacters(HashSet<Character> textFieldDefaultAllowedCharacters) {
            if (textFieldDefaultAllowedCharacters == null) return;
            this.textFieldDefaultAllowedCharacters.clear();
            this.textFieldDefaultAllowedCharacters.addAll(textFieldDefaultAllowedCharacters);
        }

        public int getTooltipFadeInTime() {
            return tooltipFadeInTime;
        }

        public void setTooltipFadeInTime(int tooltipFadeInTime) {
            this.tooltipFadeInTime = Tools.Calc.lowerBounds(tooltipFadeInTime, 0);
        }

        public int getTooltipFadeInDelayTime() {
            return tooltipFadeInDelayTime;
        }

        public void setTooltipFadeInDelayTime(int tooltipFadeInDelayTime) {
            this.tooltipFadeInDelayTime = Tools.Calc.lowerBounds(tooltipFadeInDelayTime, 0);
        }

        public FColor getContextMenuDefaultColor() {
            return contextMenuDefaultColor;
        }

        public void setContextMenuDefaultColor(FColor contextMenuDefaultColor) {
            this.contextMenuDefaultColor = contextMenuDefaultColor;
        }


        public _Config() {
            this.textFieldDefaultAllowedCharacters.addAll(Arrays.asList(new Character[]{
                    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    ' ', '_', '.', ',', '!', '?'
            }));
        }

        public void loadConfig(_Config config) {
            setWindowsDefaultEnforceScreenBounds(config.getWindowsDefaultEnforceScreenBounds());
            setWindowsDefaultColor(config.getWindowsDefaultColor());
            setComponentsDefaultColor(config.getComponentsDefaultColor());
            setTooltipDefaultColor(config.getTooltipDefaultColor());
            setTooltipDefaultFont(config.getTooltipDefaultFont());
            setDefaultFont(config.getDefaultFont());
            setDragTransparency(config.getDragTransparency());
            setButtonHoldTimer(config.getButtonHoldTimer());
            setKnobSensitivity(config.getKnobSensitivity());
            setFoldWindowsOnDoubleClick(config.isFoldWindowsOnDoubleClick());
            setNotificationsMax(config.getNotificationsMax());
            setNotificationsDefaultDisplayTime(config.getNotificationsDefaultDisplayTime());
            setNotificationsDefaultFont(config.getNotificationsDefaultFont());
            setNotificationsDefaultColor(config.getNotificationsDefaultColor());
            setNotificationsFadeoutTime(config.getNotificationsFadeoutTime());
            setMapOverlayDefaultFadeoutTime(config.getMapOverlayDefaultFadeoutTime());
            setTextFieldDefaultAllowedCharacters(config.getTextFieldDefaultAllowedCharacters());
            setTooltipFadeInTime(config.getTooltipFadeInTime());
            setTooltipFadeInDelayTime(config.getTooltipFadeInDelayTime());
            setNotificationsScrollSpeed(config.getNotificationsScrollSpeed());
        }


    }

    public class _Input {

        public boolean mouseBusyWithGUI() {
            if (inputState.lastGUIMouseHover != null) return true;
            if (inputState.draggedWindow != null) return true;
            if (inputState.pressedButton != null) return true;
            if (inputState.scrolledScrollBarHorizontal != null) return true;
            if (inputState.scrolledScrollBarVertical != null) return true;
            if (inputState.turnedKnob != null) return true;
            if (inputState.pressedMap != null) return true;
            if (inputState.pressedGameViewPort != null) return true;
            if (inputState.inventoryDrag_Item != null) return true;
            if (inputState.listDrag_List != null) return true;
            return false;
        }


        public Object lastGUIMouseHover() {
            return inputState.lastGUIMouseHover;
        }

        public int mouseXGUI() {
            return inputState.mouse_x_gui;
        }

        public int mouseYGUI() {
            return inputState.mouse_y_gui;
        }

        public int mouseX() {
            return inputState.mouse_x;
        }

        public int mouseY() {
            return inputState.mouse_y;
        }

        public int mouseXDelta() {
            return inputState.mouse_x_delta;
        }

        public int mouseYDelta() {
            return inputState.mouse_y_delta;
        }

        public boolean keyDown() {
            return inputState.inputEvents.keyDown;
        }

        public boolean keyUp() {
            return inputState.inputEvents.keyUp;
        }

        public boolean keyTyped() {
            return inputState.inputEvents.keyTyped;
        }

        public boolean mouseDown() {
            return inputState.inputEvents.mouseDown;
        }

        public boolean mouseDoubleClick() {
            return inputState.inputEvents.mouseDoubleClick;
        }

        public boolean mouseUp() {
            return inputState.inputEvents.mouseUp;
        }

        public boolean mouseDragged() {
            return inputState.inputEvents.mouseDragged;
        }

        public boolean mouseMoved() {
            return inputState.inputEvents.mouseMoved;
        }

        public boolean mouseScrolled() {
            return inputState.inputEvents.mouseScrolled;
        }

        public ArrayList<Character> keysTyped() {
            return inputState.inputEvents.keysTyped;
        }

        public ArrayList<Integer> keyCodesUp() {
            return inputState.inputEvents.keyCodesUp;
        }

        public ArrayList<Integer> keyCodesDown() {
            return inputState.inputEvents.keyCodesDown;
        }

        public int mouseButton() {
            return inputState.inputEvents.mouseButton;
        }

        public float mouseScrolledAmount() {
            return inputState.inputEvents.mouseScrolledAmount;
        }
    }

    public class _Notification {
        public Notification create(String text) {
            return create(text, null, null, null, null);
        }

        public Notification create(String text, FColor color) {
            return create(text, color, null, null, null);
        }

        public Notification create(String text, FColor color, CMediaFont font) {
            return create(text, color, font, null, null);
        }

        public Notification create(String text, FColor color, CMediaFont font, Integer displayTime) {
            return create(text, color, font, displayTime, null);
        }

        public Notification create(String text, FColor color, CMediaFont font, Integer displayTime, NotificationAction notificationAction) {
            Notification notification = new Notification();
            setText(notification, text);
            setDisplayTime(notification, displayTime);
            setColor(notification, color);
            setFont(notification, font);
            setNotificationAction(notification, notificationAction);
            setName(notification, "");
            setData(notification, null);
            notification.timer = 0;
            int textWidth = mediaManager.textWidth(notification.font, notification.text);
            if (textWidth > inputState.internalResolutionWidth) {
                int tooMuch = (textWidth - inputState.internalResolutionWidth);
                notification.state = STATE_NOTIFICATION.INIT_SCROLL;
                notification.scroll = -(tooMuch / 2) - 4;
                notification.scrollMax = (tooMuch / 2) + 4;
            } else {
                notification.state = STATE_NOTIFICATION.INIT_DISPLAY;
                notification.scroll = notification.scrollMax = 0;
            }
            return notification;
        }

        public void setName(Notification notification, String name) {
            if (notification == null) return;
            notification.name = Tools.Text.validString(name);
        }

        public void setData(Notification notification, Object data) {
            if (notification == null) return;
            notification.data = data;
        }

        public void setNotificationAction(Notification notification, NotificationAction notificationAction) {
            if (notification == null) return;
            notification.notificationAction = notificationAction;
        }

        public void setDisplayTime(Notification notification, Integer displayTime) {
            if (notification == null) return;
            notification.displayTime = Tools.Calc.lowerBounds(displayTime == null ? config.notificationsDefaultDisplayTime : displayTime, 0);
        }

        public void setColor(Notification notification, FColor color) {
            if (notification == null || color == null) return;
            notification.color_r = color.r;
            notification.color_g = color.g;
            notification.color_b = color.b;
            notification.color_a = color.a;
        }

        public void setFont(Notification notification, CMediaFont font) {
            if (notification == null) return;
            notification.font = font == null ? config.notificationsDefaultFont : font;
        }

        public void setText(Notification notification, String text) {
            if (notification == null) return;
            notification.text = Tools.Text.validString(text);
        }

    }

    public class _ContextMenu {

        public final _ContextMenuItem item = new _ContextMenuItem();

        public ContextMenu create(ContextMenuItem[] contextMenuItems) {
            return create(contextMenuItems, config.contextMenuDefaultColor);
        }

        public ContextMenu create(ContextMenuItem[] contextMenuItems, FColor color) {
            ContextMenu contextMenu = new ContextMenu();
            contextMenu.items = new ArrayList<>();
            setContextMenuItems(contextMenu, contextMenuItems);
            setColor(contextMenu, color);
            return contextMenu;
        }

        public void setColor(ContextMenu contextMenu, FColor color) {
            if (contextMenu == null || color == null) return;
            contextMenu.color_r = color.r;
            contextMenu.color_g = color.g;
            contextMenu.color_b = color.b;
            contextMenu.color_a = color.a;
        }

        public void setContextMenuItems(ContextMenu contextMenu, ContextMenuItem[] contextMenuItems) {
            if (contextMenu == null || contextMenuItems == null) return;
            contextMenu.items.clear();
            contextMenu.items.addAll(Arrays.asList(contextMenuItems));
        }

        public ArrayList<ContextMenuItem> findContextMenuItemsByName(ContextMenu contextMenu, String name) {
            ArrayList<ContextMenuItem> result = new ArrayList<>();
            if (contextMenu == null || name == null) return result;
            for (ContextMenuItem contextMenuItem : contextMenu.items) {
                if (name.equals(contextMenuItem.name)) result.add(contextMenuItem);
            }
            return result;
        }

        public ContextMenuItem findContextMenuItemByName(ContextMenu contextMenu, String name) {
            if (contextMenu == null || name == null) return null;
            ArrayList<ContextMenuItem> result = findContextMenuItemsByName(contextMenu, name);
            return result.size() > 0 ? result.get(0) : null;
        }

        public class _ContextMenuItem {

            private ContextMenuItemAction defaultContextMenuItemAction() {
                return new ContextMenuItemAction() {
                };
            }

            public ContextMenuItem create(String text) {
                return create(text, defaultContextMenuItemAction(), null, config.contextMenuDefaultColor, null);
            }

            public ContextMenuItem create(String text, ContextMenuItemAction contextMenuItemAction) {
                return create(text, contextMenuItemAction, null, config.contextMenuDefaultColor, null);
            }

            public ContextMenuItem create(String text, ContextMenuItemAction contextMenuItemAction, CMediaGFX icon) {
                return create(text, contextMenuItemAction, icon, config.contextMenuDefaultColor, null);
            }

            public ContextMenuItem create(String text, ContextMenuItemAction contextMenuItemAction, CMediaGFX icon, FColor color) {
                return create(text, contextMenuItemAction, icon, color, null);
            }

            public ContextMenuItem create(String text, ContextMenuItemAction contextMenuItemAction, CMediaGFX icon, FColor color, CMediaFont font) {
                ContextMenuItem contextMenuItem = new ContextMenuItem();
                setText(contextMenuItem, text);
                setFont(contextMenuItem, font);
                setColor(contextMenuItem, color);
                setIcon(contextMenuItem, icon);
                setIconIndex(contextMenuItem, 0);
                setName(contextMenuItem, "");
                setData(contextMenuItem, null);
                setContextMenuItemAction(contextMenuItem, contextMenuItemAction);
                return contextMenuItem;
            }

            public void setName(ContextMenuItem contextMenuItem, String name) {
                if (contextMenuItem == null) return;
                contextMenuItem.name = Tools.Text.validString(name);

            }

            public void setData(ContextMenuItem contextMenuItem, Object data) {
                if (contextMenuItem == null) return;
                contextMenuItem.data = data;
            }

            public void setColor(ContextMenuItem contextMenuItem, FColor color) {
                if (contextMenuItem == null || color == null) return;
                contextMenuItem.color_r = color.r;
                contextMenuItem.color_g = color.g;
                contextMenuItem.color_b = color.b;
                contextMenuItem.color_a = color.a;
            }

            public void setFont(ContextMenuItem contextMenuItem, CMediaFont font) {
                if (contextMenuItem == null) return;
                contextMenuItem.font = font == null ? config.defaultFont : font;
            }

            public void setContextMenuItemAction(ContextMenuItem contextMenuItem, ContextMenuItemAction contextMenuItemAction) {
                if (contextMenuItem == null) return;
                contextMenuItem.contextMenuItemAction = contextMenuItemAction;
            }

            public void setText(ContextMenuItem contextMenuItem, String text) {
                if (contextMenuItem == null) return;
                contextMenuItem.text = Tools.Text.validString(text);
            }

            public void setIcon(ContextMenuItem contextMenuItem, CMediaGFX icon) {
                if (contextMenuItem == null) return;
                contextMenuItem.icon = icon;
            }

            public void setIconIndex(ContextMenuItem contextMenuItem, int index) {
                if (contextMenuItem == null) return;
                contextMenuItem.iconIndex = Tools.Calc.lowerBounds(index, 0);
            }

        }

    }

    public class _Windows {

        private WindowAction defaultWindowAction() {
            return new WindowAction() {
            };
        }

        public Window create(int x, int y, int width, int height) {
            return create(x, y, width, height, "", null, false, true, true, true, defaultWindowAction(), null, config.windowsDefaultColor, null);
        }

        public Window create(int x, int y, int width, int height, String title) {
            return create(x, y, width, height, title, null, false, true, true, true, defaultWindowAction(), null, config.windowsDefaultColor, null);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon) {
            return create(x, y, width, height, title, icon, false, true, true, true, defaultWindowAction(), null, config.windowsDefaultColor, null);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, boolean alwaysOnTop) {
            return create(x, y, width, height, title, icon, alwaysOnTop, true, true, true, defaultWindowAction(), null, config.windowsDefaultColor, null);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, boolean alwaysOnTop, boolean moveAble) {
            return create(x, y, width, height, title, icon, alwaysOnTop, moveAble, true, true, defaultWindowAction(), null, config.windowsDefaultColor, null);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, boolean alwaysOnTop, boolean moveAble, boolean hasTitleBar) {
            return create(x, y, width, height, title, icon, alwaysOnTop, moveAble, hasTitleBar, true, defaultWindowAction(), null, config.windowsDefaultColor, null);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, boolean alwaysOnTop, boolean moveAble, boolean hasTitleBar, boolean hidden) {
            return create(x, y, width, height, title, icon, alwaysOnTop, moveAble, hasTitleBar, hidden, defaultWindowAction(), null, config.windowsDefaultColor, null);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, boolean alwaysOnTop, boolean moveAble, boolean hasTitleBar, boolean hidden, WindowAction windowAction) {
            return create(x, y, width, height, title, icon, alwaysOnTop, moveAble, hasTitleBar, hidden, windowAction, null, config.windowsDefaultColor, null);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, boolean alwaysOnTop, boolean moveAble, boolean hasTitleBar, boolean hidden, WindowAction windowAction, Component[] components) {
            return create(x, y, width, height, title, icon, alwaysOnTop, moveAble, hasTitleBar, hidden, windowAction, components, config.windowsDefaultColor, null);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, boolean alwaysOnTop, boolean moveAble, boolean hasTitleBar, boolean hidden, WindowAction windowAction, Component[] components, FColor color) {
            return create(x, y, width, height, title, icon, alwaysOnTop, moveAble, hasTitleBar, hidden, windowAction, components, color, null);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, boolean alwaysOnTop, boolean moveAble, boolean hasTitleBar, boolean visible, WindowAction windowAction, Component[] components, FColor color, CMediaFont font) {
            Window window = new Window();
            setPosition(window, x, y);
            setSize(window, width, height);
            setTitle(window, title);
            setAlwaysOnTop(window, alwaysOnTop);
            setMoveAble(window, moveAble);
            setColor(window, color);
            setFont(window, font);
            setHasTitleBar(window, hasTitleBar);
            setVisible(window, visible);
            setWindowAction(window, windowAction);
            setIcon(window, icon);
            setIconIndex(window, 0);
            setName(window, "");
            setData(window, null);
            setEnforceScreenBounds(window, config.getWindowsDefaultEnforceScreenBounds());
            window.components = new ArrayList<>();
            window.font = config.defaultFont;
            window.addComponentsQueue = new ArrayDeque<>();
            window.removeComponentsQueue = new ArrayDeque<>();
            window.messageReceiverActions = new ArrayList<>();
            window.updateActions = new ArrayList<>();
            addComponents(window, components);
            return window;
        }

        public void removeMessageReceiverAction(Window window, MessageReceiverAction messageReceiver) {
            window.messageReceiverActions.remove(messageReceiver);
        }

        public void addMessageReceiverAction(Window window, MessageReceiverAction messageReceiver) {
            window.messageReceiverActions.add(messageReceiver);
        }

        public void setEnforceScreenBounds(Window window, boolean enforceScreenBounds) {
            if (window == null) return;
            window.enforceScreenBounds = enforceScreenBounds;
        }

        public void setIcon(Window window, CMediaGFX icon) {
            if (window == null) return;
            window.icon = icon;
        }

        public void setIconIndex(Window window, int iconIndex) {
            if (window == null) return;
            window.iconIndex = Tools.Calc.lowerBounds(iconIndex, 0);
        }

        public void setVisible(Window window, boolean visible) {
            if (window == null) return;
            window.visible = visible;
        }

        public void setHasTitleBar(Window window, boolean hasTitleBar) {
            if (window == null) return;
            window.hasTitleBar = hasTitleBar;
        }

        public void setWindowAction(Window window, WindowAction windowAction) {
            if (window == null) return;
            window.windowAction = windowAction;
        }

        public void setColorEverything(Window window, FColor color) {
            setColorEverythingExcept(window, color, null, true, true, true);
        }

        public void setColorEverything(Window window, FColor color, boolean setColor1, boolean setColor2) {
            setColorEverythingExcept(window, color, null, setColor1, setColor2, true);
        }

        public void setColorEverythingExcept(Window window, FColor color, Class[] exceptions) {
            setColorEverythingExcept(window, color, exceptions, false, false);
        }

        public void setColorEverythingExcept(Window window, FColor color, Class[] exceptions, boolean setColor1, boolean setColor2) {
            setColorEverythingExcept(window, color, exceptions, setColor1, setColor2, false);
        }

        public void setColorEverythingExcept(Window window, FColor color, Class[] exceptions, boolean setColor1, boolean setColor2, boolean includeWindow) {
            if (window == null) return;

            for (Component component : window.addComponentsQueue) {

                if (exceptions == null || !Arrays.stream(exceptions).anyMatch(exceptionClass -> exceptionClass == component.getClass())) {
                    if (setColor1) components.setColor(component, color);
                    if (setColor2) components.setColor2(component, color);
                }
            }
            for (Component component : window.components) {
                if (exceptions == null || !Arrays.stream(exceptions).anyMatch(exceptionClass -> exceptionClass == component.getClass())) {
                    if (setColor1) components.setColor(component, color);
                    if (setColor2) components.setColor2(component, color);
                }
            }

            if (includeWindow) setColor(window, color);
        }

        public void setColorEverythingInclude(Window window, FColor color, Class[] inclusions) {
            setColorEverythingInclude(window, color, inclusions, false, false);
        }

        public void setColorEverythingInclude(Window window, FColor color, Class[] inclusions, boolean setColor1, boolean setColor2) {
            setColorEverythingInclude(window, color, inclusions, setColor1, setColor2, false);
        }

        public int getRealWidth(Window window) {
            return UICommons.window_getRealWidth(window);
        }

        public int getRealHeight(Window window) {
            return UICommons.window_getRealHeight(window);
        }

        public void setColorEverythingInclude(Window window, FColor color, Class[] inclusions, boolean setColor1, boolean setColor2, boolean includeWindow) {
            if (window == null) return;

            for (Component component : window.addComponentsQueue) {
                if (inclusions != null && Arrays.stream(inclusions).anyMatch(inclusionClass -> inclusionClass == component.getClass())) {
                    if (setColor1) components.setColor(component, color);
                    if (setColor2) components.setColor2(component, color);
                }
            }
            for (Component component : window.components) {
                if (inclusions != null && Arrays.stream(inclusions).anyMatch(inclusionClass -> inclusionClass == component.getClass())) {
                    if (setColor1) components.setColor(component, color);
                    if (setColor2) components.setColor2(component, color);
                }
            }

            if (includeWindow) setColor(window, color);
        }


        public Window createFromGenerator(Class windowGeneratorClass, Object... p) {
            if (windowGeneratorClass == null) return null;
            WindowGenerator windowGenerator = windowGeneratorCache.get(windowGeneratorClass);
            if (windowGenerator == null) {
                try {
                    windowGenerator = (WindowGenerator) windowGeneratorClass.getDeclaredConstructor(API.class).newInstance(API.this);
                } catch (Exception e) {
                    return null;
                }
                windowGeneratorCache.put(windowGeneratorClass, windowGenerator);
            }

            Window window = windowGenerator.create(p);
            return window;
        }


        public void addComponent(Window window, Component component) {
            if (window == null || component == null) return;
            window.addComponentsQueue.add(component);
            return;
        }

        public void addComponents(Window window, Component[] components) {
            if (window == null || components == null) return;
            for (Component component : components) addComponent(window, component);
        }

        public void addComponents(Window window, ArrayList<Component> components) {
            if (window == null || components == null) return;
            for (Component component : components) addComponent(window, component);
        }

        public void removeComponent(Window window, Component component) {
            if (window == null || component == null) return;
            if (window.components.contains(component)) {
                window.removeComponentsQueue.add(component);
            }
        }

        public void removeComponents(Window window, Component[] components) {
            if (window == null || components == null) return;
            for(Component component : components) removeComponent(window, component);
        }

        public void removeComponents(Window window, ArrayList<Component> components) {
            if (window == null || components == null) return;
            for(Component component : components) removeComponent(window, component);
        }

        public ArrayList<Component> findComponentsByName(Window window, String name) {
            ArrayList<Component> result = new ArrayList<>();
            if (window == null || name == null) return result;
            if (window == null) return result;
            for (Component component : window.components) {
                if (name.equals(component.name)) result.add(component);
            }
            for (Component component : window.addComponentsQueue) {
                if (name.equals(component.name)) result.add(component);
            }
            return result;
        }

        public Component findComponentByName(Window window, String name) {
            if (window == null || name == null) return null;
            ArrayList<Component> result = findComponentsByName(window, name);
            return result.size() > 0 ? result.get(0) : null;
        }

        public void bringToFront(Window window) {
            if (window == null) return;
            UICommons.window_bringToFront(inputState, window);
        }

        public void center(Window window) {
            if (window == null) return;
            window.x = inputState.internalResolutionWidth / 2 - UICommons.window_getRealWidth(window) / 2;
            window.y = inputState.internalResolutionHeight / 2 - UICommons.window_getRealHeight(window) / 2;
        }

        public void setFont(Window window, CMediaFont font) {
            if (window == null) return;
            window.font = font == null ? config.defaultFont : font;
        }

        public void addUpdateAction(Window window, UpdateAction updateAction) {
            if (window == null || updateAction == null) return;
            window.updateActions.add(updateAction);
        }

        public void removeUpdateAction(Window window, UpdateAction updateAction) {
            if (window == null || updateAction == null) return;
            window.updateActions.remove(updateAction);
        }

        public void removeAllUpdateActions(Window window) {
            if (window == null) return;
            window.updateActions.clear();
        }

        public void setName(Window window, String name) {
            if (window == null) return;
            window.name = Tools.Text.validString(name);
        }

        public void setData(Window window, Object data) {
            if (window == null) return;
            window.data = data;
        }

        public void setColor(Window window, FColor color) {
            if (window == null || color == null) return;
            window.color_r = color.r;
            window.color_g = color.g;
            window.color_b = color.b;
            window.color_a = color.a;
        }

        public void setTransparency(Window window, float transparency) {
            if (window == null) return;
            window.color_a = transparency;
        }

        public void setAlwaysOnTop(Window window, boolean alwaysOnTop) {
            if (window == null) return;
            window.alwaysOnTop = alwaysOnTop;
        }

        public void setFolded(Window window, boolean folded) {
            if (window == null) return;
            window.folded = folded;
        }

        public void setMoveAble(Window window, boolean moveAble) {
            if (window == null) return;
            window.moveAble = moveAble;
        }

        public void setPosition(Window window, int x, int y) {
            if (window == null) return;
            window.x = x;
            window.y = y;
        }

        public void move(Window window, int xRel, int yRel) {
            if (window == null) return;
            setPosition(window, window.x + xRel, window.y + yRel);
        }

        public void setSize(Window window, int width, int height) {
            if (window == null) return;
            window.width = Tools.Calc.lowerBounds(width, 2);
            window.height = Tools.Calc.lowerBounds(height, 2);
        }

        public void setTitle(Window window, String title) {
            if (window == null) return;
            window.title = Tools.Text.validString(title);
        }
    }

    public class _ToolTip {

        public _ToolTipImage toolTipImage = new _ToolTipImage();

        public class _ToolTipImage {

            public ToolTipImage create(CMediaImage image, int offset_x, int offset_y) {
                ToolTipImage toolTipImage = new ToolTipImage();
                toolTipImage.image = image;
                toolTipImage.offset_x = offset_x;
                toolTipImage.offset_y = offset_y;
                return toolTipImage;
            }

        }

        private ToolTipAction defaultToolTipAction() {
            return new ToolTipAction() {
            };
        }


        public ToolTip create(String[] lines) {
            return create(lines, true, config.tooltipDefaultColor, defaultToolTipAction(), null, config.tooltipDefaultFont);
        }

        public ToolTip create(String[] lines, boolean displayFistLineAsTitle) {
            return create(lines, displayFistLineAsTitle, config.tooltipDefaultColor, defaultToolTipAction(), null, config.tooltipDefaultFont);
        }

        public ToolTip create(String[] lines, boolean displayFistLineAsTitle, FColor color) {
            return create(lines, displayFistLineAsTitle, color, defaultToolTipAction(), null, config.tooltipDefaultFont);
        }

        public ToolTip create(String[] lines, boolean displayFistLineAsTitle, FColor color, ToolTipAction toolTipAction) {
            return create(lines, displayFistLineAsTitle, color, toolTipAction, null, config.tooltipDefaultFont);
        }

        public ToolTip create(String[] lines, boolean displayFistLineAsTitle, FColor color, ToolTipAction toolTipAction, ToolTipImage[] images) {
            return create(lines, displayFistLineAsTitle, color, toolTipAction, images, config.tooltipDefaultFont);
        }

        public ToolTip create(String[] lines, boolean displayFistLineAsTitle, FColor color, ToolTipAction toolTipAction, ToolTipImage[] images, CMediaFont font) {
            ToolTip tooltip = new ToolTip();
            tooltip.images = new ArrayList<>();
            setDisplayFistLineAsTitle(tooltip, displayFistLineAsTitle);
            setLines(tooltip, lines);
            setColor(tooltip, color);
            setToolTipAction(tooltip, toolTipAction);
            setFont(tooltip, font);
            setImages(tooltip, images);
            return tooltip;
        }

        public void setImages(ToolTip toolTip, ToolTipImage[] images) {
            if (toolTip == null) return;
            toolTip.images.clear();
            if (images != null) {
                for (ToolTipImage image : images) {
                    addImage(toolTip, image);
                }
            }
        }

        public void addImage(ToolTip toolTip, ToolTipImage toolTipImage) {
            if (toolTip == null) return;
            if (toolTipImage != null) {
                toolTip.images.add(toolTipImage);
            }
        }

        public void setToolTipAction(ToolTip toolTip, ToolTipAction toolTipAction) {
            toolTip.toolTipAction = toolTipAction;
        }

        public void setDisplayFistLineAsTitle(ToolTip tooltip, boolean firstLineIsTitle) {
            if (tooltip == null) return;
            tooltip.displayFistLineAsTitle = firstLineIsTitle;
        }

        public void setLines(ToolTip tooltip, String[] lines) {
            tooltip.lines = Tools.Text.validString(lines);
        }

        public void setColor(ToolTip tooltip, FColor color) {
            if (tooltip == null || color == null) return;
            tooltip.color_r = color.r;
            tooltip.color_g = color.g;
            tooltip.color_b = color.b;
            tooltip.color_a = color.a;
        }

        public void setFont(ToolTip tooltip, CMediaFont font) {
            if (tooltip == null) return;
            tooltip.font = font == null ? config.tooltipDefaultFont : font;
        }


    }

    public int resolutionWidth() {
        return inputState.internalResolutionWidth;
    }

    public int resolutionHeight() {
        return inputState.internalResolutionHeight;
    }

    public class _Camera {

        public OrthographicCamera camera() {
            return inputState.camera_game;
        }

        public boolean pointVisible(float x, float y) {
            setTestingCameraTo(inputState.camera_x, inputState.camera_y, inputState.internalResolutionWidth, inputState.internalResolutionHeight, inputState.camera_z);
            if (inputState.camera_frustum.frustum.pointInFrustum(x, y, 0f)) {
                return true;
            }
            for (GameViewPort gameViewPort : inputState.gameViewPorts) {
                setTestingCameraTo(gameViewPort.camera_x, gameViewPort.camera_y, gameViewPort.width * UIEngine.TILE_SIZE, gameViewPort.height * UIEngine.TILE_SIZE, gameViewPort.camera_zoom);
                if (inputState.camera_frustum.frustum.pointInFrustum(x, y, 0f)) return true;
            }
            return false;

        }

        public boolean boundsVisible(float x, float y, float halfWidth, float halfHeight) {
            setTestingCameraTo(inputState.camera_x, inputState.camera_y, inputState.internalResolutionWidth, inputState.internalResolutionHeight, inputState.camera_z);
            if (inputState.camera_frustum.frustum.boundsInFrustum(x, y, 0f, halfWidth, halfHeight, 0f)) {
                return true;
            }
            for (GameViewPort gameViewPort : inputState.gameViewPorts) {
                setTestingCameraTo(gameViewPort.camera_x, gameViewPort.camera_y, gameViewPort.width * UIEngine.TILE_SIZE, gameViewPort.height * UIEngine.TILE_SIZE, gameViewPort.camera_zoom);
                if (inputState.camera_frustum.frustum.boundsInFrustum(x, y, 0f, halfWidth, halfHeight, 0f)) return true;
            }
            return false;
        }

        public boolean sphereVisible(float x, float y, float radius) {
            setTestingCameraTo(inputState.camera_x, inputState.camera_y, inputState.internalResolutionWidth, inputState.internalResolutionHeight, inputState.camera_z);
            if (inputState.camera_frustum.frustum.sphereInFrustum(x, y, 0f, radius)) {
                return true;
            }
            for (GameViewPort gameViewPort : inputState.gameViewPorts) {
                setTestingCameraTo(gameViewPort.camera_x, gameViewPort.camera_y, gameViewPort.width * UIEngine.TILE_SIZE, gameViewPort.height * UIEngine.TILE_SIZE, gameViewPort.camera_zoom);
                if (inputState.camera_frustum.frustum.sphereInFrustum(x, y, 0f, radius)) return true;
            }
            return false;
        }

        private void setTestingCameraTo(float x, float y, float z, float width, float height) {
            inputState.camera_frustum.position.x = x;
            inputState.camera_frustum.position.y = y;
            inputState.camera_frustum.zoom = z;
            inputState.camera_frustum.viewportWidth = width;
            inputState.camera_frustum.viewportHeight = height;
            inputState.camera_frustum.update();
            return;
        }

        public float viewPortStretchFactorWidth() {
            return inputState.viewport_screen.getWorldWidth() / (float) inputState.viewport_screen.getScreenWidth();
        }

        public float viewPortStretchFactorHeight() {
            return inputState.viewport_screen.getWorldHeight() / (float) inputState.viewport_screen.getScreenHeight();
        }

        public void moveAbs(float x, float y) {
            moveAbs(x, y, inputState.camera_z);
            return;
        }

        public void moveAbs(float x, float y, float z) {
            inputState.camera_x = x;
            inputState.camera_y = y;
            inputState.camera_z = z;
            return;
        }

        public void moveRel(float x, float y) {
            moveRel(x, y, inputState.camera_z);
            return;
        }

        public void moveRel(float x, float y, float z) {
            inputState.camera_x += x;
            inputState.camera_y += y;
            inputState.camera_y += z;
            return;
        }

        public void xRel(float x) {
            inputState.camera_x += x;
            return;
        }

        public void xAbs(float x) {
            inputState.camera_x = x;
            return;
        }

        public void yRel(float y) {
            inputState.camera_y += y;
            return;
        }

        public void yAbs(float zoom) {
            inputState.camera_y = zoom;
            return;
        }

        public void zRel(float z) {
            inputState.camera_z += z;
            return;
        }

        public void zAbs(float z) {
            inputState.camera_z = z;
            return;
        }

        public void zoomRel(float z) {
            inputState.camera_zoom += z;
            return;
        }

        public void zoomAbs(float z) {
            inputState.camera_zoom = z;
            return;
        }

        public float zoom() {
            return inputState.camera_zoom;
        }

        public float z() {
            return inputState.camera_z;
        }

        public float x() {
            return inputState.camera_x;
        }

        public float y() {
            return inputState.camera_y;
        }

        public float width() {
            return inputState.camera_width;
        }

        public float height() {
            return inputState.camera_height;
        }

    }

    public class _Components {

        public final _Shape shape = new _Shape();

        public final _Button button = new _Button();

        public final _TabBar tabBar = new _TabBar();

        public final _Inventory inventory = new _Inventory();

        public final _ScrollBar scrollBar = new _ScrollBar();

        public final _List list = new _List();

        public final _TextField textField = new _TextField();

        public final _Map map = new _Map();

        public final _Knob knob = new _Knob();

        public final _Text text = new _Text();

        public final _Image image = new _Image();

        public final _ComboBox comboBox = new _ComboBox();

        public final _ProgressBar progressBar = new _ProgressBar();

        public final _CheckBox checkBox = new _CheckBox();

        public final _GameViewPort gameViewPort = new _GameViewPort();

        public void setToolTip(Component component, ToolTip tooltip) {
            if (component == null) return;
            component.toolTip = tooltip;
        }

        public void forceToolTipUpdate(Component component) {
            if (component == null) return;
            component.updateToolTip = true;
        }

        public void setPosition(Component component, int x, int y) {
            if (component == null) return;
            component.x = x;
            component.y = y;
        }

        public void setOffset(Component component, int x, int y) {
            if (component == null) return;
            component.offset_x = x;
            component.offset_y = y;
        }

        public void setOffset(Collection<Component> components, int x, int y) {
            if (components == null) return;
            for (Component component : components) {
                setOffset(component, x, y);
            }
        }

        public void setOffset(Component[] components, int x, int y) {
            if (components == null) return;
            for (Component component : components) {
                setOffset(component, x, y);
            }
        }

        public void setDisabled(Component component, boolean disabled) {
            if (component == null) return;
            component.disabled = disabled;
        }

        public void setDisabled(Component[] components, boolean disabled) {
            if (components == null) return;
            for (Component component : components) {
                setDisabled(component, disabled);
            }
        }

        public void addUpdateAction(Component component, UpdateAction updateAction) {
            if (component == null || updateAction == null) return;
            component.updateActions.add(updateAction);
        }

        public void removeUpdateAction(Component component, UpdateAction updateAction) {
            if (component == null || updateAction == null) return;
            component.updateActions.remove(updateAction);
        }

        public void removeAllUpdateActions(Component component) {
            if (component == null) return;
            component.updateActions.clear();
        }

        public void setName(Component component, String name) {
            if (component == null) return;
            component.name = Tools.Text.validString(name);
        }

        public void setCustomData(Component component, Object customData) {
            if (component == null) return;
            component.data = customData;
        }

        public void setSize(Component component, int width, int height) {
            if (component == null) return;
            component.width = Tools.Calc.lowerBounds(width, 1);
            component.height = Tools.Calc.lowerBounds(height, 1);
        }

        public void setDimensions(Component component, int x, int y, int width, int height) {
            if (component == null) return;
            setPosition(component, x, y);
            setSize(component, width, height);
        }

        public void setColor(Component component, FColor color) {
            if (component == null || color == null) return;
            component.color_r = color.r;
            component.color_g = color.g;
            component.color_b = color.b;
            component.color_a = color.a;
        }

        public void setColor2(Component component, FColor color) {
            if (component == null || color == null) return;
            component.color2_r = color.r;
            component.color2_g = color.g;
            component.color2_b = color.b;
            component.color2_a = color.a;
        }

        public void setColor(Collection<Component> components, FColor color) {
            if (components == null) return;
            for (Component component : components) setColor(component, color);

        }

        public void setColor2(Collection<Component> components, FColor color2) {
            if (components == null) return;
            for (Component component : components) setColor2(component, color2);
        }

        public void setColor(Component[] components, FColor color) {
            if (components == null) return;
            for (Component component : components) setColor(component, color);
        }

        public void setColor2(Component[] components, FColor color2) {
            if (components == null) return;
            for (Component component : components) setColor2(component, color2);
        }

        public void setColor1And2(Component component, FColor color) {
            if (component == null) return;
            setColor(component, color);
            setColor2(component, color);
        }

        public void setColor1And2(Collection<Component> components, FColor color) {
            if (components == null) return;
            for (Component component : components) {
                setColor(component, color);
                setColor2(component, color);
            }
        }

        public void setColor1And2(Component[] components, FColor color) {
            if (components == null) return;
            for (Component component : components) {
                setColor(component, color);
                setColor2(component, color);
            }
        }

        public void setTransparency(Component component, float transparency) {
            if (component == null) return;
            component.color_a = transparency;
        }

        public void setTransparency(Component[] components, float transparency) {
            if (components == null) return;
            for (Component component : components) {
                if (component != null) setTransparency(component, transparency);
            }
        }

        private void setComponentInitValues(Component component) {
            component.x = component.y = 0;
            component.width = component.height = 1;
            component.color_r = config.componentsDefaultColor.r;
            component.color_g = config.componentsDefaultColor.g;
            component.color_b = config.componentsDefaultColor.b;
            component.color_a = config.componentsDefaultColor.a;
            component.color2_r = config.componentsDefaultColor.r;
            component.color2_g = config.componentsDefaultColor.g;
            component.color2_b = config.componentsDefaultColor.b;
            component.color2_a = config.componentsDefaultColor.a;
            component.disabled = false;
            component.updateActions = new ArrayList<>();
            component.data = null;
            component.name = "";
            component.offset_x = component.offset_y = 0;
            component.visible = true;
            component.updateToolTip = false;
            component.addedToTab = null;
            component.addedToWindow = null;
            component.toolTip = null;
        }

        public void setVisible(Component component, boolean visible) {
            if (component == null) return;
            component.visible = visible;
        }

        public void setVisible(Component[] components, boolean visible) {
            if (components == null) return;
            for (Component component : components) setVisible(component, visible);
        }

        public int getAbsoluteX(Component component) {
            if (component == null) return 0;
            return UICommons.component_getParentWindowX(component) + (component.x * UIEngine.TILE_SIZE) + component.offset_x;
        }

        public int getAbsoluteY(Component component) {
            if (component == null) return 0;
            return UICommons.component_getParentWindowY(component) + (component.y * UIEngine.TILE_SIZE) + component.offset_y;
        }

        public int getRealWidth(Component component) {
            if (component == null) return 0;
            return component.width * UIEngine.TILE_SIZE;
        }

        public int getRealHeight(Component component) {
            if (component == null) return 0;
            return component.height * UIEngine.TILE_SIZE;
        }

        public class _GameViewPort {

            public GameViewPort create(int x, int y, int width, int height, float camPositionX, float camPositionY) {
                return create(x, y, width, height, camPositionX, camPositionY, 1f, null, null);
            }

            public GameViewPort create(int x, int y, int width, int height, float camPositionX, float camPositionY, float camZoom) {
                return create(x, y, width, height, camPositionX, camPositionY, camZoom, null, null);
            }

            public GameViewPort create(int x, int y, int width, int height, float camPositionX, float camPositionY, float camZoom, int updateTime) {
                return create(x, y, width, height, camPositionX, camPositionY, camZoom, updateTime, null);
            }

            public GameViewPort create(int x, int y, int width, int height, float camPositionX, float camPositionY, float camZoom, Integer updateTime, GameViewPortAction gameViewPortAction) {
                GameViewPort gameViewPort = new GameViewPort();
                gameViewPort.updateTimer = 0;
                gameViewPort.frameBuffer = new FrameBuffer(Pixmap.Format.RGB888, width * UIEngine.TILE_SIZE, height * UIEngine.TILE_SIZE, false);
                Texture texture = gameViewPort.frameBuffer.getColorBufferTexture();
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                gameViewPort.textureRegion = new TextureRegion(texture, width * UIEngine.TILE_SIZE, height * UIEngine.TILE_SIZE);
                gameViewPort.textureRegion.flip(false, true);
                setComponentInitValues(gameViewPort);
                setPosition(gameViewPort, x, y);
                setSize(gameViewPort, width, height);
                setCamPosition(gameViewPort, camPositionX, camPositionY);
                setCamZoom(gameViewPort, camZoom);
                setUpdateTime(gameViewPort, updateTime);
                setGameViewPortAction(gameViewPort, gameViewPortAction);
                setColor(gameViewPort, Tools.Colors.WHITE);
                return gameViewPort;
            }

            public void setGameViewPortAction(GameViewPort gameViewPort, GameViewPortAction gameViewPortAction) {
                if (gameViewPort == null) return;
                gameViewPort.gameViewPortAction = gameViewPortAction;
            }

            public void setUpdateTime(GameViewPort gameViewPort, Integer updateTime) {
                if (gameViewPort == null) return;
                gameViewPort.updateTime = Tools.Calc.lowerBounds(updateTime == null ? config.gameviewportDefaultUpdateTime : updateTime, 0);
            }

            public void setCamZoom(GameViewPort gameViewPort, float camZoom) {
                if (gameViewPort == null) return;
                gameViewPort.camera_zoom = Tools.Calc.lowerBounds(camZoom, 0f);
            }

            public void setCamPosition(GameViewPort gameViewPort, float x, float y) {
                if (gameViewPort == null) return;
                setCamPosition(gameViewPort, x, y, gameViewPort.camera_z);
            }

            public void setCamPosition(GameViewPort gameViewPort, float x, float y, float z) {
                if (gameViewPort == null) return;
                gameViewPort.camera_x = x;
                gameViewPort.camera_y = y;
                gameViewPort.camera_z = z;
            }

        }

        public class _ProgressBar {

            public ProgressBar create(int x, int y, int width) {
                return create(x, y, width, 0f, false, false, null, null);
            }

            public ProgressBar create(int x, int y, int width, float progress) {
                return create(x, y, width, progress, false, false, null, null);
            }

            public ProgressBar create(int x, int y, int width, float progress, boolean progressText) {
                return create(x, y, width, progress, progressText, false, null, null);
            }

            public ProgressBar create(int x, int y, int width, float progress, boolean progressText, boolean progressText2Decimal) {
                return create(x, y, width, 0f, progressText, progressText2Decimal, null, null);
            }

            public ProgressBar create(int x, int y, int width, float progress, boolean progressText, boolean progressText2Decimal, CMediaFont font) {
                return create(x, y, width, 0f, progressText, progressText2Decimal, font, null);
            }

            public ProgressBar create(int x, int y, int width, float progress, boolean progressText, boolean progressText2Decimal, CMediaFont font, FColor color) {
                ProgressBar progressBar = new ProgressBar();
                setComponentInitValues(progressBar);
                setPosition(progressBar, x, y);
                setSize(progressBar, width, 1);
                setColor(progressBar, color);
                setProgress(progressBar, progress);
                setProgressText(progressBar, progressText);
                setProgressText2Decimal(progressBar, progressText2Decimal);
                setFont(progressBar, font);
                return progressBar;
            }

            public void setFont(ProgressBar progressBar, CMediaFont font) {
                if (progressBar == null) return;
                progressBar.font = font == null ? config.defaultFont : font;
            }

            public void setProgress(ProgressBar progressBar, float progress) {
                if (progressBar == null) return;
                progressBar.progress = Tools.Calc.inBounds(progress, 0f, 1f);
            }

            public void setProgressText(ProgressBar progressBar, boolean progressText) {
                if (progressBar == null) return;
                progressBar.progressText = progressText;
            }

            public void setProgressText2Decimal(ProgressBar progressBar, boolean progressText2Decimal) {
                if (progressBar == null) return;
                progressBar.progressText2Decimal = progressText2Decimal;
            }

        }

        public class _Shape {

            public Shape create(int x, int y, int width, int height, ShapeType shapeType) {
                return create(x, y, width, height, shapeType, config.getComponentsDefaultColor());
            }

            public Shape create(int x, int y, int width, int height, ShapeType shapeType, FColor color) {
                Shape shape = new Shape();
                setComponentInitValues(shape);
                setPosition(shape, x, y);
                setSize(shape, width, height);
                setColor(shape, color);
                setShapeType(shape, shapeType);
                return shape;
            }

            public void setShapeType(Shape shape, ShapeType shapeType) {
                if (shape == null) return;
                shape.shapeType = shapeType;
            }

        }

        public class _Button {

            public final _TextButton textButton = new _TextButton();

            public final _ImageButton imageButton = new _ImageButton();

            private ButtonAction defaultButtonAction() {
                return new ButtonAction() {
                };
            }

            public void setButtonAction(Button button, ButtonAction buttonAction) {
                if (button == null) return;
                button.buttonAction = buttonAction;
            }

            public void setPressed(Button button, boolean pressed) {
                if (button == null) return;
                button.pressed = pressed;
            }

            public void setPressed(Button[] buttons, boolean pressed) {
                for (Button button : buttons) {
                    setPressed(button, pressed);
                }
            }

            public void setCanHold(Button button, boolean canHold) {
                if (button == null) return;
                button.canHold = canHold;
                if (button.canHold && button.toggleMode) setToggleMode(button, false);
            }

            public void setToggleMode(Button button, boolean toggleMode) {
                if (button == null) return;
                button.toggleMode = toggleMode;
                if (button.toggleMode && button.canHold) setCanHold(button, false);
            }

            public void setOffsetContent(Button button, int x, int y) {
                if (button == null) return;
                button.offset_content_x = x;
                button.offset_content_y = y;
            }


            public void setOffsetContent(Button[] buttons, int x, int y) {
                if (buttons == null) return;
                for (Button button : buttons) {
                    setOffsetContent(button, x, y);
                }
            }

            private void setButtonValues(Button button, ButtonAction buttonAction, boolean canHold, boolean toggleMode, boolean pressed, int contentOffsetX, int contentOffsetY) {
                setButtonAction(button, buttonAction);
                setPressed(button, pressed);
                setCanHold(button, canHold);
                setToggleMode(button, toggleMode);
                setOffsetContent(button, contentOffsetX, contentOffsetY);
            }

            public void centerContent(Button[] buttons) {
                for (Button button : buttons) {
                    centerContent(button);
                }
            }

            public void disableAndRemoveAction(Button button) {
                setDisabled(button, true);
                setButtonAction(button, null);
            }

            public void disableAndRemoveAction(Button[] buttons) {
                for (Button button : buttons) {
                    disableAndRemoveAction(button);
                }
            }

            public void centerContent(Button button) {
                if (button == null) return;
                if (button.getClass() == ImageButton.class) {
                    ImageButton imageButton = (ImageButton) button;
                    if (imageButton.image == null) return;
                    setOffsetContent(imageButton,
                            ((imageButton.width * UIEngine.TILE_SIZE) - mediaManager.imageWidth(imageButton.image)) / 2,
                            ((imageButton.height * UIEngine.TILE_SIZE) - mediaManager.imageHeight(imageButton.image)) / 2);
                } else if (button.getClass() == TextButton.class) {
                    TextButton textButton = (TextButton) button;
                    if (textButton.text == null) return;
                    int iconWidth = textButton.icon != null ? UIEngine.TILE_SIZE : 0;

                    int contentWidth = mediaManager.textWidth(textButton.font, textButton.text) + iconWidth;
                    int contentHeight = mediaManager.textHeight(textButton.font, textButton.text);
                    setOffsetContent(textButton,
                            (((textButton.width * UIEngine.TILE_SIZE) - contentWidth) / 2) + 1,
                            (((textButton.height * UIEngine.TILE_SIZE) - contentHeight) / 2) - ((UIEngine.TILE_SIZE / 2) - 3));

                }
            }

            public class _TextButton {
                public TextButton create(int x, int y, int width, int height, String text) {
                    return create(x, y, width, height, text, defaultButtonAction(), null, false, false, false, 0, 0, null);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction) {
                    return create(x, y, width, height, text, buttonAction, null, false, false, false, 0, 0, null);
                }


                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon) {
                    return create(x, y, width, height, text, buttonAction, icon, false, false, false, 0, 0, null);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon, boolean canHold) {
                    return create(x, y, width, height, text, buttonAction, icon, canHold, false, false, 0, 0, null);
                }


                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon, boolean canHold, boolean toggleMode) {
                    return create(x, y, width, height, text, buttonAction, icon, canHold, toggleMode, false, 0, 0, null);
                }


                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon, boolean canHold, boolean toggleMode, boolean pressed) {
                    return create(x, y, width, height, text, buttonAction, icon, canHold, toggleMode, pressed, 0, 0, null);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon, boolean canHold, boolean toggleMode, boolean pressed, int contentOffsetX, int contentOffsetY) {
                    return create(x, y, width, height, text, buttonAction, icon, canHold, toggleMode, pressed, contentOffsetX, contentOffsetY, null);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon, boolean canHold, boolean toggleMode, boolean pressed, int contentOffsetX, int contentOffsetY, CMediaFont font) {
                    TextButton textButton = new TextButton();
                    setComponentInitValues(textButton);
                    setButtonValues(textButton, buttonAction, canHold, toggleMode, pressed, contentOffsetX, contentOffsetY);
                    setPosition(textButton, x, y);
                    setSize(textButton, width, height);
                    setText(textButton, text);
                    setFont(textButton, font);
                    setIcon(textButton, icon);
                    setIconArray(textButton, 0);
                    centerContent(textButton);
                    return textButton;
                }

                public void setIcon(TextButton textButton, CMediaGFX icon) {
                    if (textButton == null) return;
                    textButton.icon = icon;
                }

                public void setIconArray(TextButton textButton, int iconArray) {
                    if (textButton == null) return;
                    textButton.iconArray = Tools.Calc.lowerBounds(iconArray, 0);
                }


                public void setText(TextButton textButton, String text) {
                    if (textButton == null) return;
                    textButton.text = Tools.Text.validString(text);
                }

                public void setFont(TextButton textButton, CMediaFont font) {
                    if (textButton == null) return;
                    textButton.font = font == null ? config.defaultFont : font;
                }

            }

            public class _ImageButton {

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image) {
                    return create(x, y, width, height, image, 0, defaultButtonAction(), false, false, false, 0, 0);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex) {
                    return create(x, y, width, height, image, arrayIndex, defaultButtonAction(), false, false, false, 0, 0);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex, ButtonAction buttonAction) {
                    return create(x, y, width, height, image, arrayIndex, buttonAction, false, false, false, 0, 0);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex, ButtonAction buttonAction, boolean canHold) {
                    return create(x, y, width, height, image, arrayIndex, buttonAction, canHold, false, false, 0, 0);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex, ButtonAction buttonAction, boolean canHold, boolean toggleMode) {
                    return create(x, y, width, height, image, arrayIndex, buttonAction, canHold, toggleMode, false, 0, 0);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex, ButtonAction buttonAction, boolean canHold, boolean toggleMode, boolean pressed) {
                    return create(x, y, width, height, image, arrayIndex, buttonAction, canHold, toggleMode, pressed, 0, 0);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex, ButtonAction buttonAction, boolean canHold, boolean toggleMode, boolean pressed, int contentOffsetX, int contentOffsetY) {
                    ImageButton imageButton = new ImageButton();
                    setComponentInitValues(imageButton);
                    setButtonValues(imageButton, buttonAction, canHold, toggleMode, pressed, contentOffsetX, contentOffsetY);
                    setPosition(imageButton, x, y);
                    setSize(imageButton, width, height);
                    setImage(imageButton, image);
                    setArrayIndex(imageButton, arrayIndex);
                    setColor2(imageButton, Tools.Colors.WHITE);
                    centerContent(imageButton);
                    return imageButton;
                }

                public void setImage(ImageButton imageButton, CMediaGFX image) {
                    if (imageButton == null) return;
                    imageButton.image = image;
                }

                public void setArrayIndex(ImageButton imageButton, int arrayIndex) {
                    if (imageButton == null) return;
                    imageButton.arrayIndex = Tools.Calc.lowerBounds(arrayIndex, 0);
                }

            }

        }

        public class _CheckBox {

            public CheckBox create(int x, int y, String text) {
                return create(x, y, text, CheckBoxStyle.CHECKBOX, null, null, false);
            }

            public CheckBox create(int x, int y, String text, CheckBoxStyle checkBoxStyle) {
                return create(x, y, text, checkBoxStyle, null, null, false);
            }

            public CheckBox create(int x, int y, String text, CheckBoxStyle checkBoxStyle, CheckBoxAction checkBoxAction) {
                return create(x, y, text, checkBoxStyle, checkBoxAction, null, false);
            }

            public CheckBox create(int x, int y, String text, CheckBoxStyle checkBoxStyle, CheckBoxAction checkBoxAction, CMediaFont font) {
                return create(x, y, text, checkBoxStyle, checkBoxAction, font, false);
            }

            public CheckBox create(int x, int y, String text, CheckBoxStyle checkBoxStyle, CheckBoxAction checkBoxAction, CMediaFont font, boolean checked) {
                CheckBox checkBox = new CheckBox();
                setComponentInitValues(checkBox);
                setColor(checkBox, Tools.Colors.WHITE);
                setPosition(checkBox, x, y);
                setSize(checkBox, 1, 1);
                setText(checkBox, text);
                setCheckBoxStyle(checkBox, checkBoxStyle);
                setCheckBoxAction(checkBox, checkBoxAction);
                setFont(checkBox, font);
                setChecked(checkBox, checked);
                return checkBox;
            }

            public void setText(CheckBox checkBox, String text) {
                if (checkBox == null) return;
                checkBox.text = Tools.Text.validString(text);
            }

            public void setFont(CheckBox checkBox, CMediaFont font) {
                if (checkBox == null) return;
                checkBox.font = font == null ? config.defaultFont : font;
            }

            public void setChecked(CheckBox checkBox, boolean checked) {
                if (checkBox == null) return;
                checkBox.checked = checked;
            }

            public void setCheckBoxStyle(CheckBox checkBox, CheckBoxStyle checkBoxStyle) {
                if (checkBox == null) return;
                checkBox.checkBoxStyle = checkBoxStyle;
            }

            public void setCheckBoxAction(CheckBox checkBox, CheckBoxAction checkBoxAction) {
                if (checkBox == null) return;
                checkBox.checkBoxAction = checkBoxAction;
            }

        }

        public class _TabBar {

            public final _Tab tab = new _Tab();

            public class _Tab {

                private TabAction defaultTabAction() {
                    return new TabAction() {
                    };
                }

                public Tab create(String title) {
                    return create(title, null, null, defaultTabAction(), 0, null);
                }

                public Tab create(String title, CMediaGFX icon) {
                    return create(title, icon, null, null, 0, null);
                }

                public Tab create(String title, CMediaGFX icon, Component[] components) {
                    return create(title, icon, components, defaultTabAction(), 0, null);
                }

                public Tab create(String title, CMediaGFX icon, Component[] components, TabAction tabAction, int width) {
                    return create(title, icon, components, tabAction, width, null);
                }

                public Tab create(String title, CMediaGFX icon, Component[] components, TabAction tabAction, int width, CMediaFont font) {
                    Tab tab = new Tab();
                    tab.components = new ArrayList<>();
                    setTitle(tab, title);
                    setTabAction(tab, tabAction);
                    setIcon(tab, icon);
                    setIconIndex(tab, 0);
                    setFont(tab, font);
                    setContentOffset(tab, 0);
                    removeAllTabComponents(tab);
                    setName(tab, "");
                    setData(tab, null);
                    addTabComponents(tab, components);
                    if (width == 0) {
                        setWidthAuto(tab);
                    } else {
                        setWidth(tab, width);
                    }
                    return tab;
                }

                public void setName(Tab tab, String name) {
                    if (tab == null) return;
                    tab.name = Tools.Text.validString(name);
                }

                public void setData(Tab tab, Object data) {
                    if (tab == null) return;
                    tab.data = data;
                }

                public void setContentOffset(Tab tab, int content_offset_x) {
                    if (tab == null) return;
                    tab.content_offset_x = content_offset_x;
                }

                public void centerTitle(Tab tab) {
                    if (tab == null) return;
                    tab.content_offset_x = ((tab.width * UIEngine.TILE_SIZE) / 2) - ((mediaManager.textWidth(tab.font, tab.title) + (tab.icon != null ? UIEngine.TILE_SIZE : 0)) / 2) - 2;
                }

                public void setIconIndex(Tab tab, int iconIndex) {
                    if (tab == null) return;
                    tab.iconIndex = Tools.Calc.lowerBounds(iconIndex, 0);
                }

                public void setTabComponents(Tab tab, Component[] components) {
                    if (tab == null || components == null) return;
                    removeAllTabComponents(tab);
                    for (Component component : components) addTabComponent(tab, component);
                }

                public void removeAllTabComponents(Tab tab) {
                    if (tab == null) return;
                    for (Component component : tab.components) removeTabComponent(tab, component);
                }

                public void addTabComponent(Tab tab, Component component) {
                    if (tab == null || component == null) return;
                    if (component.addedToTab != null) return; // component can only be on one tab
                    component.addedToTab = tab;
                    tab.components.add(component);
                    return;
                }

                public void addTabComponents(Tab tab, Component[] components) {
                    if (tab == null || components == null) return;
                    for (Component component : components) addTabComponent(tab, component);
                }

                public void addTabComponents(Tab tab, ArrayList<Component> components) {
                    if (tab == null || components == null) return;
                    for (Component component : components) addTabComponent(tab, component);
                }

                public void removeTabComponents(Tab tab, Component[] components) {
                    if (tab == null || components == null) return;
                    for (Component component : components) removeTabComponent(tab, component);
                }

                public void removeTabComponents(Tab tab, ArrayList<Component> components) {
                    if (tab == null || components == null) return;
                    for (Component component : components) removeTabComponent(tab, component);
                }

                public void removeTabComponent(Tab tab, Component component) {
                    if (tab == null || component == null) return;
                    component.addedToTab = null;
                    tab.components.remove(component);
                }

                public void setIcon(Tab tab, CMediaGFX icon) {
                    if (tab == null) return;
                    tab.icon = icon;
                }

                public void setTitle(Tab tab, String title) {
                    if (tab == null) return;
                    tab.title = Tools.Text.validString(title);
                }

                public void setFont(Tab tab, CMediaFont font) {
                    if (tab == null) return;
                    tab.font = font == null ? config.defaultFont : font;
                }

                public void setTabAction(Tab tab, TabAction tabAction) {
                    if (tab == null) return;
                    tab.tabAction = tabAction;
                }

                public void setWidth(Tab tab, int width) {
                    if (tab == null) return;
                    tab.width = Tools.Calc.lowerBounds(width, 1);
                }

                public void setWidthAuto(Tab tab) {
                    if (tab == null) return;
                    int width = MathUtils.round((mediaManager.textWidth(tab.font, tab.title) + (tab.icon != null ? UIEngine.TILE_SIZE : 0) + UIEngine.TILE_SIZE) / (float) UIEngine.TILE_SIZE);
                    setWidth(tab, width);
                }

            }

            public TabBar create(int x, int y, int width, Tab[] tabs) {
                return create(x, y, width, tabs, 0, null, false, 0, 0);
            }

            public TabBar create(int x, int y, int width, Tab[] tabs, int selectedTab) {
                return create(x, y, width, tabs, selectedTab, null, false, 0, 0);
            }

            public TabBar create(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction) {
                return create(x, y, width, tabs, selectedTab, tabBarAction, false, 0, 0);
            }

            public TabBar create(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight) {
                return create(x, y, width, tabs, selectedTab, tabBarAction, border, borderHeight, 0);
            }

            public TabBar create(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight, int tabOffset) {
                TabBar tabBar = new TabBar();
                tabBar.tabs = new ArrayList<>();
                setComponentInitValues(tabBar);
                setPosition(tabBar, x, y);
                setSize(tabBar, width, 1);
                removeAllTabs(tabBar);
                setTabs(tabBar, tabs);
                selectTab(tabBar, selectedTab);
                setTabBarAction(tabBar, tabBarAction);
                setBorder(tabBar, border);
                setBorderHeight(tabBar, borderHeight);
                setTabOffset(tabBar, tabOffset);
                return tabBar;
            }

            public void setTabOffset(TabBar tabBar, int tabOffset) {
                tabBar.tabOffset = Tools.Calc.lowerBounds(tabOffset, 0);
            }

            public void setBorder(TabBar tabBar, boolean border) {
                tabBar.border = border;
            }

            public void setBorderHeight(TabBar tabBar, int borderHeight) {
                tabBar.borderHeight = Tools.Calc.lowerBounds(borderHeight, 0);
            }

            public boolean isTabVisible(TabBar tabBar, Tab tab) {
                if (tabBar == null || tab == null) return false;
                int xOffset = 0;
                for (Tab tabB : tabBar.tabs) {
                    xOffset += tabB.width;
                    if (tabB == tab) {
                        if (xOffset > tabBar.width) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                }
                return false;
            }

            public int getCombinedTabsWidth(TabBar tabBar) {
                int width = 0;
                for (Tab tab : tabBar.tabs) {
                    width += tab.width;
                }
                return width;
            }

            public void setTabBarAction(TabBar tabBar, TabBarAction tabBarAction) {
                if (tabBar == null) return;
                tabBar.tabBarAction = tabBarAction;
            }

            public Tab getSelectedTab(TabBar tabBar) {
                if (tabBar == null) return null;
                return UICommons.tabBar_getSelectedTab(tabBar);
            }

            public int getSelectedTabIndex(TabBar tabBar) {
                if (tabBar == null) return 0;
                return tabBar.selectedTab;
            }

            public Tab getTab(TabBar tabBar, int index) {
                if (tabBar == null) return null;
                return tabBar.tabs.get(Tools.Calc.inBounds(index, 0, tabBar.tabs.size() - 1));
            }

            public Tab[] getTabs(TabBar tabBar) {
                return tabBar.tabs.toArray(new Tab[0]);
            }

            public void selectTab(TabBar tabBar, int index) {
                if (tabBar == null) return;
                UICommons.tabBar_selectTab(tabBar, index);
            }

            public void selectTab(TabBar tabBar, Tab tab) {
                if (tabBar == null) return;
                for (int i = 0; i < tabBar.tabs.size(); i++) {
                    if (tabBar.tabs.get(i) == tab) {
                        UICommons.tabBar_selectTab(tabBar, i);
                    }
                }
            }

            public void removeAllTabs(TabBar tabBar) {
                if (tabBar == null) return;
                for (Tab tab : tabBar.tabs) removeTab(tabBar, tab);
            }

            public void addTab(TabBar tabBar, Tab tab) {
                if (tabBar == null || tab == null) return;
                tab.tabBar = tabBar;
                tabBar.tabs.add(tab);
            }

            public void addTab(TabBar tabBar, Tab tab, int index) {
                if (tabBar == null || tab == null) return;
                tab.tabBar = tabBar;
                tabBar.tabs.add(index, tab);
            }

            public void removeTab(TabBar tabBar, Tab tab) {
                if (tabBar == null || tab == null) return;
                if (tabBar.tabs.contains(tab)) {
                    tab.tabBar = null;
                    tabBar.tabs.remove(tab);
                }
            }

            public void setTabs(TabBar tabBar, Tab[] tabs) {
                if (tabBar == null || tabs == null) return;
                removeAllTabs(tabBar);
                for (Tab tab : tabs) addTab(tabBar, tab);
            }

            public ArrayList<Tab> findTabsByName(TabBar tabBar, String name) {
                ArrayList<Tab> result = new ArrayList<>();
                if (tabBar == null || name == null) return result;
                for (Tab tab : tabBar.tabs) {
                    if (name.equals(tab.name)) result.add(tab);
                }
                return result;
            }

            public Tab findTabByName(TabBar tabBar, String name) {
                if (tabBar == null || name == null) return null;
                ArrayList<Tab> result = findTabsByName(tabBar, name);
                return result.size() > 0 ? result.get(0) : null;
            }

        }

        public class _Inventory {

            private InventoryAction defaultInventoryAction() {
                return new InventoryAction() {
                };
            }

            public Inventory create(int x, int y, Object[][] items) {
                return create(x, y, items, defaultInventoryAction(), false, false, false, false);
            }

            public Inventory create(int x, int y, Object[][] items, InventoryAction inventoryAction) {
                return create(x, y, items, inventoryAction, false, false, false, false);
            }

            public Inventory create(int x, int y, Object[][] items, InventoryAction inventoryAction, boolean dragEnabled) {
                return create(x, y, items, inventoryAction, dragEnabled, false, false, false);
            }

            public Inventory create(int x, int y, Object[][] items, InventoryAction inventoryAction, boolean dragEnabled, boolean dragOutEnabled) {
                return create(x, y, items, inventoryAction, dragEnabled, dragOutEnabled, false, false);
            }

            public Inventory create(int x, int y, Object[][] items, InventoryAction inventoryAction, boolean dragEnabled, boolean dragOutEnabled, boolean dragInEnabled) {
                return create(x, y, items, inventoryAction, dragEnabled, dragOutEnabled, dragInEnabled, false);
            }


            public Inventory create(int x, int y, Object[][] items, InventoryAction inventoryAction, boolean dragEnabled, boolean dragOutEnabled, boolean dragInEnabled, boolean doubleSized) {
                Inventory inventory = new Inventory();
                setComponentInitValues(inventory);
                setPosition(inventory, x, y);
                setItems(inventory, items);
                setInventoryAction(inventory, inventoryAction);
                setDragEnabled(inventory, dragEnabled);
                setDragOutEnabled(inventory, dragOutEnabled);
                setDragInEnabled(inventory, dragInEnabled);
                setDoubleSized(inventory, doubleSized);
                updateDimensions(inventory);
                return inventory;
            }

            public void setDoubleSized(Inventory inventory, boolean doubleSized) {
                inventory.doubleSized = doubleSized;
                updateDimensions(inventory);
            }

            public boolean isPositionValid(Inventory inventory, int x, int y) {
                if (inventory == null) return false;
                return UICommons.inventory_positionValid(inventory, x, y);
            }

            public void setDragInEnabled(Inventory inventory, boolean dragInEnabled) {
                if (inventory == null) return;
                inventory.dragInEnabled = dragInEnabled;
            }

            public void setDragOutEnabled(Inventory inventory, boolean dragOutEnabled) {
                if (inventory == null) return;
                inventory.dragOutEnabled = dragOutEnabled;
            }

            public void setDragEnabled(Inventory inventory, boolean dragEnabled) {
                if (inventory == null) return;
                inventory.dragEnabled = dragEnabled;
            }

            public void setInventoryAction(Inventory inventory, InventoryAction inventoryAction) {
                if (inventory == null) return;
                inventory.inventoryAction = inventoryAction;
            }

            public void setItems(Inventory inventory, Object[][] items) {
                if (inventory == null || items == null) return;
                inventory.items = items;
            }

            public void updateDimensions(Inventory inventory) {
                if (inventory == null) return;
                int factor = inventory.doubleSized ? 2 : 1;
                if (inventory.items != null) {
                    inventory.width = inventory.items.length * factor;
                    inventory.height = inventory.items[0].length * factor;
                }
            }

        }

        public class _TextField {

            private TextFieldAction defaultTextFieldAction() {
                return new TextFieldAction() {
                };
            }

            public TextField create(int x, int y, int width) {
                return create(x, y, width, "", defaultTextFieldAction(), 32, null, null);
            }


            public TextField create(int x, int y, int width, String content) {
                return create(x, y, width, content, defaultTextFieldAction(), 32, null, null);
            }


            public TextField create(int x, int y, int width, String content, TextFieldAction textFieldAction) {
                return create(x, y, width, content, textFieldAction, 32, null, null);
            }

            public TextField create(int x, int y, int width, String content, TextFieldAction textFieldAction, int contentMaxLength) {
                return create(x, y, width, content, textFieldAction, contentMaxLength, null, null);
            }

            public TextField create(int x, int y, int width, String content, TextFieldAction textFieldAction, int contentMaxLength, HashSet<Character> allowedCharacters) {
                return create(x, y, width, content, textFieldAction, contentMaxLength, allowedCharacters, null);
            }

            public TextField create(int x, int y, int width, String content, TextFieldAction textFieldAction, int contentMaxLength, HashSet<Character> allowedCharacters, CMediaFont font) {
                TextField textField = new TextField();
                textField.allowedCharacters = new HashSet<>();
                textField.offset = 0;

                setComponentInitValues(textField);
                setColor(textField, Tools.Colors.WHITE);
                setPosition(textField, x, y);
                setSize(textField, width, 1);
                setFont(textField, font);
                setContentMaxLength(textField, contentMaxLength);
                setAllowedCharacters(textField, allowedCharacters);
                setContent(textField, content);
                setTextFieldAction(textField, textFieldAction);

                setMarkerPosition(textField, textField.content.length());
                textField.contentValid = textField.textFieldAction == null ? true : textField.textFieldAction.isContentValid(textField.content);
                return textField;
            }

            public void setMarkerPosition(TextField textField, int position) {
                if (textField == null) return;
                UICommons.textfield_setMarkerPosition(mediaManager, textField, position);
            }

            public void setContent(TextField textField, String content) {
                if (textField == null) return;
                UICommons.textfield_setContent(textField, content);
            }

            public void setFont(TextField textField, CMediaFont font) {
                if (textField == null) return;
                textField.font = font == null ? config.defaultFont : font;
            }

            public void setTextFieldAction(TextField textField, TextFieldAction textFieldAction) {
                if (textField == null) return;
                textField.textFieldAction = textFieldAction;
            }

            public void setContentMaxLength(TextField textField, int contentMaxLength) {
                if (textField == null) return;
                textField.contentMaxLength = Tools.Calc.lowerBounds(contentMaxLength, 0);
            }

            public void setAllowedCharacters(TextField textField, HashSet<Character> allowedCharacters) {
                if (textField == null) return;
                textField.allowedCharacters.clear();
                textField.allowedCharacters.addAll(allowedCharacters == null ? config.textFieldDefaultAllowedCharacters : allowedCharacters);
            }

            public void unFocus(TextField textField) {
                if (textField == null) return;
                if (inputState.focusedTextField == textField) {
                    inputState.focusedTextField.focused = false;
                    inputState.focusedTextField = null;
                }
            }

            public void focus(TextField textField) {
                if (textField == null) return;
                inputState.focusedTextField = textField;
                textField.focused = true;
            }


        }

        public class _Map {

            public final _MapOverlay mapOverlay = new _MapOverlay();

            private MapAction defaultMapAction() {
                return new MapAction() {
                };
            }

            public Map create(int x, int y, int width, int height) {
                return create(x, y, width, height, defaultMapAction(), null);
            }

            public Map create(int x, int y, int width, int height, MapAction mapAction) {
                return create(x, y, width, height, mapAction, null);
            }

            public Map create(int x, int y, int width, int height, MapAction mapAction, MapOverlay[] mapOverlays) {
                Map map = new Map();
                setComponentInitValues(map);
                setColor(map, Tools.Colors.WHITE);
                map.overlays = new ArrayList<>();
                map.pMap = new Pixmap(width * UIEngine.TILE_SIZE, height * UIEngine.TILE_SIZE, Pixmap.Format.RGBA8888);
                setPosition(map, x, y);
                setSize(map, width, height);
                setMapAction(map, mapAction);
                addOverlays(map, mapOverlays);
                update(map);
                return map;
            }

            public void setMapAction(Map map, MapAction mapAction) {
                if (map == null) return;
                map.mapAction = mapAction;
            }

            public void update(Map map) {
                if (map == null) return;
                map.texture = new Texture(map.pMap);
                return;
            }

            public FColor getPixel(Map map, int x, int y) {
                if (map == null) return null;
                return Tools.Colors.createFromInt(map.pMap.getPixel(x, y));
            }

            public void drawPixel(Map map, int x, int y, FColor fColor) {
                if (fColor == null) return;
                drawPixel(map, x, y, fColor.r, fColor.g, fColor.b, fColor.a);
            }

            public void fillMap(Map map, float r, float g, float b, float a) {
                if (map == null) return;
                map.pMap.setColor(r, g, b, a);
                for (int iy = 0; iy < map.pMap.getHeight(); iy++) {
                    map.pMap.drawLine(0, iy, map.pMap.getWidth(), iy);
                }
            }

            public void drawPixel(Map map, int x, int y, float r, float g, float b, float a) {
                if (map == null) return;
                map.pMap.setColor(r, g, b, a);
                map.pMap.drawPixel(x, y);
            }

            public void drawLine(Map map, int x1, int y1, int x2, int y2, FColor fColor) {
                if (fColor == null) return;

                drawLine(map, x1, y1, x2, y2, fColor.r, fColor.g, fColor.b, fColor.a);
            }

            public void drawLine(Map map, int x1, int y1, int x2, int y2, float r, float g, float b, float a) {
                if (map == null) return;
                map.pMap.setColor(r, g, b, a);
                map.pMap.drawLine(x1, y1, x2, y2);
            }

            public void drawRect(Map map, int x1, int y1, int width, int height, FColor fColor) {
                if (fColor == null) return;
                drawRect(map, x1, y1, width, height, fColor.r, fColor.g, fColor.b, fColor.a);
            }

            public void drawRect(Map map, int x1, int y1, int width, int height, float r, float g, float b, float a) {
                if (map == null) return;
                map.pMap.setColor(r, g, b, a);
                map.pMap.drawRectangle(x1, y1, width, height);
            }

            public void drawCircle(Map map, int x, int y, int radius, FColor fColor) {
                if (fColor == null) return;
                drawCircle(map, x, y, radius, fColor.r, fColor.g, fColor.b, fColor.a);
            }

            public void drawCircle(Map map, int x, int y, int radius, float r, float g, float b, float a) {
                if (map == null) return;
                map.pMap.setColor(r, g, b, a);
                map.pMap.drawCircle(x, y, radius);
            }

            public void addOverlays(Map map, MapOverlay[] mapOverlays) {
                if (map == null || mapOverlays == null) return;
                for (MapOverlay mapOverlay : mapOverlays) {
                    addOverlay(map, mapOverlay);
                }
            }

            public void addOverlay(Map map, MapOverlay mapOverlay) {
                if (map == null || mapOverlay == null) return;
                if (!map.overlays.contains(mapOverlay)) map.overlays.add(mapOverlay);
            }

            public void removeOverlay(Map map, MapOverlay mapOverlay) {
                if (map == null || mapOverlay == null) return;
                map.overlays.remove(mapOverlay);
            }

            public ArrayList<MapOverlay> findMapOverlaysByName(Map map, String name) {
                ArrayList<MapOverlay> result = new ArrayList<>();
                if (map == null || name == null) return result;
                for (MapOverlay mapOverlay : map.overlays) {
                    if (name.equals(mapOverlay.name)) result.add(mapOverlay);
                }
                return result;
            }

            public MapOverlay findMapOverlayByName(Map map, String name) {
                if (map == null || name == null) return null;
                ArrayList<MapOverlay> result = findMapOverlaysByName(map, name);
                return result.size() > 0 ? result.get(0) : null;
            }

            public void removeAllOverlays(Map map) {
                if (map == null) return;
                for (MapOverlay mapOverlay : map.overlays) removeOverlay(map, mapOverlay);
            }

            public class _MapOverlay {
                public MapOverlay create(CMediaGFX image, int x, int y) {
                    return create(image, x, y, false, null, 0);
                }

                public MapOverlay create(CMediaGFX image, int x, int y, boolean fadeOut) {
                    return create(image, x, y, fadeOut, null, 0);
                }

                public MapOverlay create(CMediaGFX image, int x, int y, boolean fadeOut, FColor color) {
                    return create(image, x, y, fadeOut, color, 0);
                }

                public MapOverlay create(CMediaGFX image, int x, int y, boolean fadeOut, FColor color, int arrayIndex) {
                    MapOverlay mapOverlay = new MapOverlay();
                    setFadeOutTime(mapOverlay, config.mapOverlayDefaultFadeoutTime);
                    setImage(mapOverlay, image);
                    setPosition(mapOverlay, x, y);
                    setFadeOut(mapOverlay, fadeOut);
                    setColor(mapOverlay, color);
                    setArrayIndex(mapOverlay, arrayIndex);
                    setName(mapOverlay, "");
                    setData(mapOverlay, null);
                    mapOverlay.timer = fadeOut ? System.currentTimeMillis() : 0;
                    return mapOverlay;
                }

                public void setFadeOutTime(MapOverlay mapOverlay, int fadeoutTime) {
                    if (mapOverlay == null) return;
                    mapOverlay.fadeOutTime = Tools.Calc.lowerBounds(fadeoutTime, 0);
                }

                public void setPosition(MapOverlay mapOverlay, int x, int y) {
                    if (mapOverlay == null) return;
                    mapOverlay.x = x;
                    mapOverlay.y = y;
                }

                public void setImage(MapOverlay mapOverlay, CMediaGFX image) {
                    if (mapOverlay == null) return;
                    mapOverlay.image = image;
                }

                public void setFadeOut(MapOverlay mapOverlay, boolean fadeOut) {
                    if (mapOverlay == null) return;
                    mapOverlay.fadeOut = fadeOut;
                }

                public void setColor(MapOverlay mapOverlay, FColor color) {
                    if (mapOverlay == null || color == null) return;
                    mapOverlay.color_r = color.r;
                    mapOverlay.color_g = color.g;
                    mapOverlay.color_b = color.b;
                    mapOverlay.color_a = color.a;
                }

                public void setArrayIndex(MapOverlay mapOverlay, int arrayIndex) {
                    if (mapOverlay == null) return;
                    mapOverlay.arrayIndex = Tools.Calc.lowerBounds(arrayIndex, 0);
                }

                public void setName(MapOverlay mapOverlay, String name) {
                    if (mapOverlay == null) return;
                    mapOverlay.name = Tools.Text.validString(name);
                }

                public void setData(MapOverlay mapOverlay, Object data) {
                    if (mapOverlay == null) return;
                    mapOverlay.data = data;
                }
            }

        }

        public class _Knob {

            private KnobAction defaultKnobAction() {
                return new KnobAction() {
                };
            }

            public Knob create(int x, int y) {
                return create(x, y, defaultKnobAction(), false, 0f);
            }

            public Knob create(int x, int y, KnobAction knobAction) {
                return create(x, y, knobAction, false, 0f);
            }

            public Knob create(int x, int y, KnobAction knobAction, boolean endless) {
                return create(x, y, knobAction, endless, 0f);
            }

            public Knob create(int x, int y, KnobAction knobAction, boolean endless, float turned) {
                Knob knob = new Knob();
                setComponentInitValues(knob);
                setPosition(knob, x, y);
                setSize(knob, 2, 2);
                setEndless(knob, endless);
                setTurned(knob, turned);
                setKnobAction(knob, knobAction);
                setColor2(knob, Tools.Colors.BLACK);
                return knob;
            }

            public void setTurned(Knob knob, float turned) {
                if (knob == null) return;
                knob.turned = Tools.Calc.inBounds(turned, 0f, 1f);
            }

            public void setKnobAction(Knob knob, KnobAction knobAction) {
                if (knob == null) return;
                knob.knobAction = knobAction;
            }

            public void setEndless(Knob knob, boolean endless) {
                if (knob == null) return;
                knob.endless = endless;
            }

        }

        public class _Text {

            private TextAction defaultTextAction() {
                return new TextAction() {
                };
            }

            public Text create(int x, int y, String[] lines) {
                return create(x, y, lines, null, defaultTextAction());
            }

            public Text create(int x, int y, String[] lines, CMediaFont font) {
                return create(x, y, lines, font, defaultTextAction());
            }

            public Text create(int x, int y, String[] lines, CMediaFont font, TextAction textAction) {
                Text textC = new Text();
                setComponentInitValues(textC);
                setPosition(textC, x, y);
                setLines(textC, lines);
                setFont(textC, font);
                setSizeAuto(textC);
                setTextAction(textC, textAction);
                return textC;
            }

            public void setTextAction(Text text, TextAction textAction) {
                if (text == null) return;
                text.textAction = textAction;
            }

            public void setLines(Text text, String[] lines) {
                if (text == null) return;
                text.lines = Tools.Text.validString(lines);
            }

            public void setFont(Text[] textComponents, CMediaFont font) {
                for (Text text : textComponents) {
                    setFont(text, font);
                }
            }


            public void setFont(Text text, CMediaFont font) {
                if (text == null) return;
                text.font = font == null ? config.defaultFont : font;
            }

            public void setSizeAuto(Text[] textComponents) {
                for (Text text : textComponents) {
                    setSizeAuto(text);
                }
            }

            public void setSizeAuto(Text text) {
                if (text == null) return;
                int width = 0;
                for (String line : text.lines) {
                    int widthT = mediaManager.textWidth(text.font, line);
                    if (widthT > width) width = widthT;
                }
                width = width / UIEngine.TILE_SIZE;
                int height = text.lines.length;
                setSize(text, width, height);
            }

        }

        public class _Image {

            private ImageAction defaultImageAction() {
                return new ImageAction() {
                };
            }

            public Image create(int x, int y, CMediaGFX image) {
                return create(x, y, image, 0, defaultImageAction());
            }

            public Image create(int x, int y, CMediaGFX image, int arrayIndex) {
                return create(x, y, image, arrayIndex, defaultImageAction());
            }

            public Image create(int x, int y, CMediaGFX image, int arrayIndex, ImageAction imageAction) {
                Image imageC = new Image();
                setComponentInitValues(imageC);
                setPosition(imageC, x, y);
                setImage(imageC, image);
                setArrayIndex(imageC, arrayIndex);
                setSizeAuto(imageC);
                setColor(imageC, Tools.Colors.WHITE);
                setImageAction(imageC, imageAction);
                return imageC;
            }

            public void setImageAction(Image image, ImageAction imageAction) {
                if (image == null) return;
                image.imageAction = imageAction;
            }

            public void setArrayIndex(Image image, int arrayIndex) {
                if (image == null) return;
                image.arrayIndex = Tools.Calc.lowerBounds(arrayIndex, 0);
            }

            public void setImage(Image imageC, CMediaGFX image) {
                if (imageC == null) return;
                imageC.image = image;
            }

            public void setSizeAuto(Image image) {
                if (image == null || image.image == null) return;
                int width = mediaManager.imageWidth(image.image) / UIEngine.TILE_SIZE;
                int height = mediaManager.imageHeight(image.image) / UIEngine.TILE_SIZE;
                setSize(image, width, height);
            }

        }

        public class _ComboBox {

            private ComboBoxAction defaultComboBoxAction() {
                return new ComboBoxAction() {
                };
            }

            public ComboBox create(int x, int y, int width) {
                return create(x, y, width, null, false, defaultComboBoxAction(), null);
            }

            public ComboBox create(int x, int y, int width, ArrayList items) {
                return create(x, y, width, items, false, defaultComboBoxAction(), null);
            }

            public ComboBox create(int x, int y, int width, ArrayList items, boolean useIcons) {
                return create(x, y, width, items, useIcons, defaultComboBoxAction(), null);
            }

            public ComboBox create(int x, int y, int width, ArrayList items, boolean useIcons, ComboBoxAction comboBoxAction) {
                return create(x, y, width, items, useIcons, comboBoxAction, null);
            }

            public ComboBox create(int x, int y, int width, ArrayList items, boolean useIcons, ComboBoxAction comboBoxAction, CMediaFont font) {
                ComboBox comboBox = new ComboBox();
                setComponentInitValues(comboBox);
                setPosition(comboBox, x, y);
                setSize(comboBox, width, 1);
                setItems(comboBox, items);
                setUseIcons(comboBox, useIcons);
                setComboBoxAction(comboBox, comboBoxAction);
                setSelectedItem(comboBox, null);
                setFont(comboBox, font);
                close(comboBox);
                return comboBox;
            }

            public void setItems(ComboBox comboBox, ArrayList items) {
                if (comboBox == null || items == null) return;
                comboBox.items = items;
            }

            public void setComboBoxAction(ComboBox comboBox, ComboBoxAction comboBoxAction) {
                if (comboBox == null) return;
                comboBox.comboBoxAction = comboBoxAction;
            }

            public void setSelectedItem(ComboBox comboBox, Object selectedItem) {
                if (comboBox == null) return;
                if (comboBox.items != null && comboBox.items.contains(selectedItem))
                    comboBox.selectedItem = selectedItem;
            }

            public void setUseIcons(ComboBox comboBox, boolean useIcons) {
                if (comboBox == null) return;
                comboBox.useIcons = useIcons;
            }

            public void setFont(ComboBox comboBox, CMediaFont font) {
                if (comboBox == null) return;
                comboBox.font = font == null ? config.defaultFont : font;
            }

            public void open(ComboBox comboBox) {
                if (comboBox == null) return;
                comboBox.menuOpen = true;
            }

            public void close(ComboBox comboBox) {
                if (comboBox == null) return;
                comboBox.menuOpen = false;
            }

        }

        public class _ScrollBar {

            public final _HorizontalScrollbar horizontalScrollbar = new _HorizontalScrollbar();

            public final _VerticalScrollbar verticalScrollbar = new _VerticalScrollbar();

            private ScrollBarAction defaultScrollBarAction() {
                return new ScrollBarAction() {
                };
            }

            public void setScrolled(ScrollBar scrollBar, float scrolled) {
                if (scrollBar == null) return;
                scrollBar.scrolled = Tools.Calc.inBounds(scrolled, 0f, 1f);
            }

            public void setScrollBarAction(ScrollBar scrollBar, ScrollBarAction scrollBarAction) {
                if (scrollBar == null) return;
                scrollBar.scrollBarAction = scrollBarAction;
            }

            public class _HorizontalScrollbar {

                public ScrollBarHorizontal create(int x, int y, int length) {
                    return create(x, y, length, defaultScrollBarAction(), 0f);
                }

                public ScrollBarHorizontal create(int x, int y, int length, ScrollBarAction scrollBarAction) {
                    return create(x, y, length, scrollBarAction, 0f);
                }

                public ScrollBarHorizontal create(int x, int y, int length, ScrollBarAction scrollBarAction, float scrolled) {
                    ScrollBarHorizontal scrollBarHorizontal = new ScrollBarHorizontal();
                    setComponentInitValues(scrollBarHorizontal);
                    setPosition(scrollBarHorizontal, x, y);
                    setSize(scrollBarHorizontal, length, 1);
                    setScrollBarAction(scrollBarHorizontal, scrollBarAction);
                    setScrolled(scrollBarHorizontal, scrolled);
                    return scrollBarHorizontal;
                }

            }

            public class _VerticalScrollbar {

                public ScrollBarVertical create(int x, int y, int length) {
                    return create(x, y, length, defaultScrollBarAction(), 0f);
                }

                public ScrollBarVertical create(int x, int y, int length, ScrollBarAction scrollBarAction) {
                    return create(x, y, length, scrollBarAction, 0f);
                }

                public ScrollBarVertical create(int x, int y, int length, ScrollBarAction scrollBarAction, float scrolled) {
                    ScrollBarVertical scrollBarVertical = new ScrollBarVertical();
                    setComponentInitValues(scrollBarVertical);
                    setPosition(scrollBarVertical, x, y);
                    setSize(scrollBarVertical, 1, length);
                    setScrollBarAction(scrollBarVertical, scrollBarAction);
                    setScrolled(scrollBarVertical, scrolled);
                    return scrollBarVertical;
                }

            }

        }

        public class _List {

            private ListAction defaultListAction() {
                return new ListAction() {
                };
            }

            public List create(int x, int y, int width, int height) {
                return create(x, y, width, height, null, defaultListAction(), false, false, false, false, null);
            }

            public List create(int x, int y, int width, int height, ArrayList items) {
                return create(x, y, width, height, items, defaultListAction(), false, false, false, false, null);
            }

            public List create(int x, int y, int width, int height, ArrayList items, ListAction listAction) {
                return create(x, y, width, height, items, listAction, false, false, false, false, null);
            }

            public List create(int x, int y, int width, int height, ArrayList items, ListAction listAction, boolean multiSelect) {
                return create(x, y, width, height, items, listAction, multiSelect, false, false, false, null);
            }

            public List create(int x, int y, int width, int height, ArrayList items, ListAction listAction, boolean multiSelect, boolean dragEnabled) {
                return create(x, y, width, height, items, listAction, multiSelect, dragEnabled, false, false, null);
            }

            public List create(int x, int y, int width, int height, ArrayList items, ListAction listAction, boolean multiSelect, boolean dragEnabled, boolean dragOutEnabled) {
                return create(x, y, width, height, items, listAction, multiSelect, dragEnabled, dragOutEnabled, false, null);
            }


            public List create(int x, int y, int width, int height, ArrayList items, ListAction listAction, boolean multiSelect, boolean dragEnabled, boolean dragOutEnabled, boolean dragInEnabled) {
                return create(x, y, width, height, items, listAction, multiSelect, dragEnabled, dragOutEnabled, dragInEnabled, null);
            }

            public List create(int x, int y, int width, int height, ArrayList items, ListAction listAction, boolean multiSelect, boolean dragEnabled, boolean dragOutEnabled, boolean dragInEnabled, CMediaFont font) {
                List list = new List();
                setComponentInitValues(list);
                setPosition(list, x, y);
                setSize(list, width, height);
                setItems(list, items);
                setListAction(list, listAction);
                setMultiSelected(list, multiSelect);
                setScrolled(list, 0f);
                setDragEnabled(list, dragEnabled);
                setDragInEnabled(list, dragInEnabled);
                setDragOutEnabled(list, dragOutEnabled);
                setFont(list, font);
                return list;
            }

            public void setDragInEnabled(List list, boolean dragInEnabled) {
                if (list == null) return;
                list.dragInEnabled = dragInEnabled;
            }

            public void setDragOutEnabled(List list, boolean dragOutEnabled) {
                if (list == null) return;
                list.dragOutEnabled = dragOutEnabled;
            }

            public void setDragEnabled(List list, boolean dragEnabled) {
                if (list == null) return;
                list.dragEnabled = dragEnabled;
            }

            public void setItems(List list, ArrayList items) {
                if (list == null) return;
                list.items = items;
            }

            public void setScrolled(List list, float scrolled) {
                if (list == null) return;
                list.scrolled = Tools.Calc.inBounds(scrolled, 0f, 1f);
            }

            public void setListAction(List list, ListAction listAction) {
                if (list == null) return;
                list.listAction = listAction;
            }

            public void setFont(List list, CMediaFont font) {
                if (list == null) return;
                list.font = font == null ? config.defaultFont : font;
            }

            public void setSelectedItem(List list, Object selectedItem) {
                if (list == null) return;
                if (list.items != null && list.items.contains(selectedItem)) list.selectedItem = selectedItem;
            }

            public void setMultiSelected(List list, boolean multiSelect) {
                if (list == null) return;
                list.multiSelect = multiSelect;
            }

            public void setSelectedItems(List list, Object[] selectedItems) {
                if (list == null || selectedItems == null) return;
                list.selectedItems.clear();
                for (Object item : selectedItems) {
                    if (item != null) {
                        list.selectedItems.add(item);
                    }
                }
            }


        }

    }


}
