package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.CMediaCursor;
import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.media_manager.media.CMediaGFX;
import org.mslivo.core.engine.media_manager.media.CMediaImage;
import org.mslivo.core.engine.tools.Tools;
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
import org.mslivo.core.engine.ui_engine.gui.components.map.Map;
import org.mslivo.core.engine.ui_engine.gui.components.map.MapOverlay;
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
import org.mslivo.core.engine.ui_engine.gui.contextmenu.ContextMenu;
import org.mslivo.core.engine.ui_engine.gui.contextmenu.ContextMenuItem;
import org.mslivo.core.engine.ui_engine.gui.hotkeys.HotKey;
import org.mslivo.core.engine.ui_engine.gui.notification.Notification;
import org.mslivo.core.engine.ui_engine.gui.notification.STATE_NOTIFICATION;
import org.mslivo.core.engine.ui_engine.gui.tool.MouseTool;
import org.mslivo.core.engine.ui_engine.gui.tooltip.ToolTip;
import org.mslivo.core.engine.ui_engine.gui.tooltip.ToolTipImage;
import org.mslivo.core.engine.ui_engine.media.GUIBaseMedia;
import org.mslivo.core.engine.ui_engine.misc.ControlMode;
import org.mslivo.core.engine.ui_engine.misc.FColor;
import org.mslivo.core.engine.ui_engine.misc.GraphInfo;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/*
    - Collections related functions are provided like
        - add(X)
        - adds(X[]) -> add(X)
        - removeX(X)
        - removeXs(X[]) ->  removeX()
        - removeAllX() -> remove(X[])
        - Optional: ArrayList<X> findXsByName
        - Optional: X findXByName

    - Color related functions are provided like:
        - setColor(X, float r, float g, float b, float a)
        - setColor(X, FColor color) -> setColor(X, float r, float g, float b, float a)

 */
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

    public final _PreConfigured preConfigured = new _PreConfigured();

    private final InputState inputState;

    private final MediaManager mediaManager;

    private final HashMap<Class, WindowGenerator> windowGeneratorCache;

    public API(InputState inputState, MediaManager mediaManager) {
        this.inputState = inputState;
        this.mediaManager = mediaManager;
        this.windowGeneratorCache = new HashMap<>();
    }


    public static class _HotKey {

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


    public static class _MouseTool {

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

    public class _PreConfigured {

        private final HashSet<Character> numbersAllowedCharacters = new HashSet<>(Arrays.asList('-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));

        private final HashSet<Character> decimalsAllowedCharacters = new HashSet<>(Arrays.asList('-', ',', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));

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

            Text textField = components.text.create(x, y, null);
            components.setSize(textField, width - 1, height);
            ScrollBarVertical scrollBarVertical = components.scrollBar.verticalScrollbar.create(x + width - 1, y, height);
            String[] textConverted;
            String[] textDisplayedLines = new String[height];

            // Cut Text to Fit
            if (text != null) {
                ArrayList<String> textList = new ArrayList<>();
                int pixelWidth = ((width - 1) * UIEngine.TILE_SIZE);
                for (String s : text) {
                    if (s.trim().length() > 0) {
                        String[] split = s.split(" ");
                        if (split.length > 0) {
                            StringBuilder currentLine = new StringBuilder();
                            for (String value : split) {
                                if (mediaManager.textWidth(config.getDefaultFont(), currentLine + value + " ") >= pixelWidth) {
                                    textList.add(currentLine.toString());
                                    currentLine = new StringBuilder(value + " ");
                                } else {
                                    currentLine.append(value).append(" ");
                                }
                            }
                            if (currentLine.toString().trim().length() > 0) {
                                textList.add(currentLine.toString());
                            }
                        }
                    } else {
                        textList.add("");
                    }
                }
                textConverted = textList.toArray(new String[]{});
            } else {
                textConverted = new String[]{};
            }

            // Actions
            components.text.setTextAction(textField, new TextAction() {
                @Override
                public void onMouseScroll(float scrolled) {
                    float scrollAmount = (-1 / (float) Tools.Calc.lowerBounds(textConverted.length, 1)) * input.mouseScrolledAmount();
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
                    if (textConverted.length > height) {
                        scrolledTextIndex = MathUtils.round((textConverted.length - height) * scrolled);
                    } else {
                        scrolledTextIndex = 0;
                    }

                    for (int iy = 0; iy < height; iy++) {
                        int textIndex = scrolledTextIndex + iy;
                        if (textIndex < textConverted.length) {
                            textDisplayedLines[iy] = textConverted[textIndex];
                        } else {
                            textDisplayedLines[iy] = "";
                        }
                    }
                }
            });

            // Init
            components.scrollBar.setScrolled(scrollBarVertical, 1f);
            if (textConverted.length <= height) {
                components.setDisabled(scrollBarVertical, true);
            }

            scrollBarVertical.scrollBarAction.onScrolled(1f);
            components.text.setLines(textField, textDisplayedLines);


            result.add(scrollBarVertical);
            result.add(textField);
            return result;
        }

        public Text text_CreateClickableURL(int x, int y, String text, String url) {
            return text_CreateClickableText(x, y, new String[]{text}, new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) {
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
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
            HotKeyAction hotKeyAction;
            if (button.mode == ButtonMode.TOGGLE) {
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
            ImageButton closeButton = preConfigured.button_CreateWindowCloseButton(modal);
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
            components.map.addMapOverlay(colorMap, cursorOverlay);


            if (!colorTexture.getTexture().getTextureData().isPrepared())
                colorTexture.getTexture().getTextureData().prepare();
            Pixmap pixmap = colorTexture.getTexture().getTextureData().consumePixmap();

            Color pixelColor = new Color();
            for (int x = 0; x < colorTexture.getRegionWidth(); x++) {
                for (int y = 0; y < colorTexture.getRegionHeight(); y++) {
                    pixelColor.set(pixmap.getPixel(colorTexture.getRegionX() + x, colorTexture.getRegionY() + y));
                    components.map.drawPixel(colorMap, x, y, pixelColor.r, pixelColor.g, pixelColor.b, 1f);
                    if (initColor != null && pixelColor.r == initColor.r && pixelColor.g == initColor.g && pixelColor.b == initColor.b) {
                        components.map.mapOverlay.setPosition(cursorOverlay, x - 3, colorTexture.getRegionHeight() - y + 1);
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
                        if (x < 0 || y < 0 || x >= colorTexture.getRegionWidth() || y >= colorTexture.getRegionHeight()) {
                            return;
                        }
                        if (x != xLast || y != yLast) {
                            components.setColor(ok, components.map.getPixelColor(colorMap, x, y - 1));
                            components.button.textButton.setFont(ok, Tools.Colors.getBrightness(Tools.Colors.create(ok.color_r, ok.color_g, ok.color_b)) < 0.5 ? GUIBaseMedia.FONT_WHITE : GUIBaseMedia.FONT_BLACK);
                            components.map.mapOverlay.setPosition(cursorOverlay, x - 1, yInv - 1);
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
            return modal_CreateTextInput(caption, text, originalText, inputResultFunction, 0,Integer.MAX_VALUE, true,0);
        }

        public Window modal_CreateTextInput(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength) {
            return modal_CreateTextInput(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, true,0);
        }

        public Window modal_CreateTextInput(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength, boolean showOKButton, int wndMinWidth) {
            final int WIDTH = Tools.Calc.lowerBounds(MathUtils.round(mediaManager.textWidth(config.defaultFont, text) / (float) UIEngine.TILE_SIZE) + 2, Tools.Calc.lowerBounds(wndMinWidth, 8));
            final int HEIGHT = showOKButton ? 6 : 5;
            originalText = originalText != null ? originalText : "";
            Window modal = windows.create(0, 0, WIDTH, HEIGHT, caption, GUIBaseMedia.GUI_ICON_INFORMATION);
            Text textC = components.text.create(0, showOKButton ? 3 : 2, Tools.Text.toArray(text));

            TextField inputTextField = components.textField.create(0, showOKButton ? 2 : 1, WIDTH - 1, originalText, null, maxInputLength);

            ArrayList<Component> componentsList = new ArrayList<>();
            componentsList.add(textC);
            componentsList.add(inputTextField);


            Button okBtn = null;
            if (showOKButton) {
                okBtn = components.button.textButton.create(0, 0, WIDTH - 1, 1, "OK", new ButtonAction() {
                    @Override
                    public void onRelease() {
                        if(inputResultFunction != null) inputResultFunction.accept(inputTextField.content);
                        removeCurrentModalWindow();
                    }
                });
                componentsList.add(okBtn);
            }

            Button finalOkBtn = okBtn;
            components.textField.setTextFieldAction(inputTextField, new TextFieldAction() {

                @Override
                public void onEnter(String content, boolean valid) {
                    if (valid) {
                        if(inputResultFunction != null) inputResultFunction.accept(inputTextField.content);
                        removeCurrentModalWindow();
                    }else{
                        components.textField.focus(inputTextField);
                    }
                }

                @Override
                public void onContentChange(String newContent, boolean valid) {
                    if (finalOkBtn != null) components.setDisabled(finalOkBtn, valid);
                }

                @Override
                public boolean isContentValid(String newContent) {
                    return newContent.length() >= minInputLength;
                }

            });


            Component[] componentArr = componentsList.toArray(new Component[]{});
            components.setOffset(componentArr, UIEngine.TILE_SIZE / 2, UIEngine.TILE_SIZE / 2);
            components.setOffset(inputTextField, UIEngine.TILE_SIZE / 2, 0);
            windows.addComponents(modal, componentArr);
            windows.setWindowAction(modal, new WindowAction() {
                @Override
                public void onAdd() {
                    components.textField.focus(inputTextField);
                }
            });
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


            Component[] componentsArr = new Component[]{okBtn};
            components.setOffset(componentsArr, UIEngine.TILE_SIZE / 2, UIEngine.TILE_SIZE / 2);
            windows.addComponents(modal, componentsArr);
            return modal;
        }

        public GraphInfo map_drawGraph(Map map, int itemCount, Function<Integer, Long> getIndexValue) {
            BiFunction<Long, Long, FColor> colorFunction = (value, lastValue) -> {
                if (value > lastValue) {
                    return Tools.Colors.GREEN_BRIGHT;
                } else if (value < lastValue) {
                    return Tools.Colors.RED_BRIGHT;
                } else {
                    return Tools.Colors.ORANGE_BRIGHT;
                }
            };
            return map_drawGraph(map, itemCount, 1, 1, getIndexValue, Tools.Colors.WHITE, colorFunction, null, true);
        }

        public GraphInfo map_drawGraph(Map map, int itemCount, int steps, int stepSize, Function<Integer, Long> getValueAtIndex, FColor colorBackGround, BiFunction<Long, Long, FColor> colorFunction, int[] hiAndLowValueReference, boolean drawBackGroundLines) {
            int mapWidth = map.width * UIEngine.TILE_SIZE;
            int mapHeight = map.height * UIEngine.TILE_SIZE;
            int[] indexAtPosition = new int[mapWidth];
            long[] valueAtPosition = new long[mapWidth];
            boolean[] dataAvailableAtPosition = new boolean[mapWidth];
            long lowestValue = Integer.MAX_VALUE;
            long highestValue = Integer.MIN_VALUE;
            // Get Values
            ArrayList<Integer> indexes = new ArrayList<>();
            ArrayList<Long> values = new ArrayList<>();
            ArrayList<Boolean> dataAvailables = new ArrayList<>();
            int startIndex = (itemCount - 1) - (steps * stepSize);
            int indexAndValueCount = 0;
            long valueBefore = (startIndex - stepSize) > 0 ? getValueAtIndex.apply((startIndex - stepSize)) : Long.MIN_VALUE;
            boolean oneValueFound = false;
            for (int i = startIndex; i < itemCount; i += stepSize) {
                if (i >= 0) {
                    long value = getValueAtIndex.apply(i);
                    lowestValue = Math.min(value, lowestValue);
                    highestValue = Math.max(value, highestValue);
                    indexes.add(i);
                    values.add(value);
                    dataAvailables.add(true);
                    oneValueFound = true;
                } else {
                    indexes.add(i);
                    values.add(0L);
                    dataAvailables.add(false);
                }
                indexAndValueCount++;
            }
            if (!oneValueFound) {
                lowestValue = 0;
                highestValue = 0;
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
                dataAvailableAtPosition[ix] = dataAvailables.get(indexAndValueIndex);
            }

            // Draw Bars
            long lastValue = valueBefore;
            int lastIndex = -1;
            final float SHADING = 0.1f;
            FColor color = colorFunction.apply(lastValue, valueBefore);
            FColor colorBrighter = Tools.Colors.createBrighter(color, SHADING);
            FColor colorDarker = Tools.Colors.createDarker(color, SHADING);
            drawLoop:
            for (int ix = 0; ix < mapWidth; ix++) {
                int index = indexAtPosition[ix];
                long value = valueAtPosition[ix];
                boolean dataAvailable = dataAvailableAtPosition[ix];

                if (!dataAvailable) continue drawLoop;

                boolean indexChange = false;
                boolean nextIndexChange = (ix + 1) < mapWidth && indexAtPosition[ix + 1] != index;
                if (index != lastIndex) {
                    color = colorFunction.apply(value, lastValue);
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
            components.setName(closeButton, UIEngine.WND_CLOSE_BUTTON);
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
            components.textField.setAllowedCharacters(textField, decimalsAllowedCharacters);
            components.textField.setTextFieldAction(textField, new TextFieldAction() {
                @Override
                public boolean isContentValid(String newContent) {
                    Float value = null;
                    try {
                        value = Float.parseFloat(newContent);
                    } catch (Exception e) {
                    }
                    return (value != null && value >= min && value <= max);
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
            components.textField.setAllowedCharacters(textField, numbersAllowedCharacters);
            components.textField.setTextFieldAction(textField, new TextFieldAction() {
                @Override
                public boolean isContentValid(String newContent) {
                    Integer value = null;
                    try {
                        value = Integer.parseInt(newContent);
                    } catch (Exception e) {
                    }
                    return value != null && value >= min && value <= max;
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

        public ArrayList<Component> tabBar_createExtendableTabBar(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight, boolean bigIconMode) {
            ArrayList<Component> ret = new ArrayList<>();

            width = Tools.Calc.lowerBounds(width, 1);
            TabBar tabBar = components.tabBar.create(x, y, width, tabs, selectedTab, tabBarAction, border, borderHeight, 2, bigIconMode);
            ImageButton extendButton = components.button.imageButton.create(x, y, 2, bigIconMode ? 2 : 1, GUIBaseMedia.GUI_ICON_EXTEND);

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

    public void setGameToolTip(ToolTip toolTip) {
        inputState.gameToolTip = toolTip;
    }

    public boolean isGameToolTipDisplayed() {
        return inputState.gameToolTip != null;
    }

    public void executeSingleUpdateAction(UpdateAction updateAction) {
        if (updateAction == null) return;
        this.inputState.singleUpdateActions.add(updateAction);
    }

    public void addNotification(Notification notification) {
        if (notification == null) return;
        UICommons.notification_addToScreen(inputState, notification, config.notificationsMax);
    }

    public void addNotifications(Notification[] notifications) {
        if (notifications == null) return;
        for (Notification notification : notifications) addNotification(notification);
    }

    public void removeNotification(Notification notification) {
        if (notification == null) return;
        UICommons.notification_removeFromScreen(inputState, notification);
    }

    public void removeNotifications(Notification[] notifications) {
        if (notifications == null) return;
        for (Notification notification : notifications) removeNotification(notification);
    }

    public void removeAllNotifications() {
        removeNotifications(inputState.notifications.toArray(new Notification[]{}));
    }

    public ArrayList<Notification> findNotificationsByName(String name) {
        if (name == null) return new ArrayList<>();
        return new ArrayList<>(inputState.notifications.stream().filter(notification -> name.equals(notification.name)).toList());
    }

    public Notification findNotificationByName(String name) {
        if (name == null) return null;
        ArrayList<Notification> result = findNotificationsByName(name);
        return result.size() > 0 ? result.get(0) : null;
    }

    public boolean isNotificationAddedToScreen(Notification notification) {
        if (notification == null) return false;
        return notification.addedToScreen;
    }


    public ArrayList<Notification> getNotifications() {
        return new ArrayList<>(inputState.notifications);
    }

    public void openContextMenu(ContextMenu contextMenu) {
        UICommons.contextMenu_openAtMousePosition(contextMenu, inputState, mediaManager);
    }

    public void openContextMenu(ContextMenu contextMenu, int x, int y) {
        if (contextMenu == null) return;
        UICommons.contextMenu_open(contextMenu, inputState, mediaManager, x, y);
    }

    public void closeContextMenu(ContextMenu contextMenu) {
        UICommons.contextMenu_close(contextMenu, inputState);
    }

    public boolean isContextMenuOpen(ContextMenu contextMenu) {
        return UICommons.contextMenu_isOpen(contextMenu, inputState);
    }

    public ArrayList<Window> getWindows() {
        return new ArrayList<>(inputState.windows);
    }

    public void sendMessageToWindows(String message_type, Object... content) {
        if (message_type == null) return;
        for (int i = 0; i < inputState.windows.size(); i++) {
            Window window = inputState.windows.get(i);
            for (int i2 = 0; i2 < window.messageReceiverActions.size(); i2++) {
                MessageReceiverAction messageReceiverAction = window.messageReceiverActions.get(i2);
                if (messageReceiverAction.messageType.equals(message_type)) {
                    messageReceiverAction.onMessageReceived(content);
                }
            }
        }
    }

    public void windowsEnforceScreenBounds() {
        for (int i = 0; i < inputState.windows.size(); i++) {
            Window window = inputState.windows.get(i);
            UICommons.window_enforceScreenBounds(inputState, window);
        }
    }

    public ArrayList<Component> getScreenComponents() {
        return new ArrayList<>(inputState.screenComponents);
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
        UICommons.window_addToScreen(inputState, window);
    }

    public void addWindows(Window[] windows) {
        if (windows == null) return;
        for (Window window : windows) addWindow(window);
    }

    public void removeWindow(Window window) {
        if (window == null) return;
        UICommons.window_removeFromScreen(inputState, window);
    }

    public void removeWindows(Window[] windows) {
        if (windows == null) return;
        for (Window window : windows) removeWindow(window);
    }

    public void removeAllWindows() {
        removeWindows(inputState.windows.toArray(new Window[]{}));
        UICommons.resetGUITempVariables(inputState);
    }

    public boolean closeWindow(Window window) {
        if (window == null) return false;
        ArrayList<Component> result = windows.findComponentsByName(window, UIEngine.WND_CLOSE_BUTTON);
        if (result.size() == 1) {
            if (result.get(0) instanceof Button closeButton) {
                if (closeButton.buttonAction != null) {
                    closeButton.buttonAction.onPress();
                    closeButton.buttonAction.onRelease();
                    return true;
                }
            }
        }
        return false;
    }

    public void closeWindows(Window[] windows) {
        if (windows == null) return;
        for (Window window : windows) closeWindow(window);
    }

    public void closeAllWindows() {
        closeWindows(inputState.windows.toArray(new Window[]{}));
        UICommons.resetGUITempVariables(inputState);
    }

    public void addWindowAsModal(Window modalWindow) {
        if (modalWindow == null) return;
        if (inputState.modalWindow == null) {
            UICommons.resetGUITempVariables(inputState);
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

    private void addNextModal() {
        if (inputState.modalWindow == null && inputState.modalWindowQueue.size() > 0) {
            addWindowAsModal(inputState.modalWindowQueue.pollFirst());
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

    public void addScreenComponent(Component component) {
        if (component == null) return;
        UICommons.component_addToScreen(component, inputState);
    }

    public void addScreenComponents(Component[] components) {
        if (components == null) return;
        for (Component component : components) addScreenComponent(component);
    }

    public void removeScreenComponent(Component component) {
        if (component == null) return;
        UICommons.component_removeFromScreen(component, inputState);
    }

    public void removeScreenComponents(Component[] components) {
        if (components == null) return;
        for (Component component : components) removeScreenComponent(component);
    }

    public void removeAllScreenComponents() {
        removeScreenComponents(inputState.screenComponents.toArray(new Component[]{}));
        UICommons.resetGUITempVariables(inputState);
    }

    public ArrayList<Component> findScreenComponentsByName(String name) {
        if (name == null) return new ArrayList<>();
        return new ArrayList<>(inputState.screenComponents.stream().filter(component -> name.equals(component.name)).toList());
    }

    public Component findScreenComponentByName( String name) {
        if (name == null) return null;
        ArrayList<Component> result = findScreenComponentsByName(name);
        return result.size() > 0 ? result.get(0) : null;
    }

    public void removeEverything() {
        removeAllWindows();
        removeAllScreenComponents();
        removeAllNotifications();
        UICommons.resetGUITempVariables(inputState);
    }

    public void setMouseTool(MouseTool mouseTool) {
        inputState.mouseTool = mouseTool;
    }

    public void overrideCursor(CMediaCursor temporaryCursor) {
        if (temporaryCursor == null) return;
        inputState.overrideCursor = temporaryCursor;
        inputState.displayOverrideCursor = true;
    }

    public MouseTool getMouseTool() {
        return inputState.mouseTool;
    }

    public boolean isMouseTool(String name) {
        if (name == null) return false;
        return inputState.mouseTool != null && name.equals(inputState.mouseTool.name);
    }

    public ArrayList<HotKey> getHotKeys() {
        return new ArrayList<>(inputState.hotKeys);
    }

    public void addHotKey(HotKey hotKey) {
        if (hotKey == null) return;
        inputState.hotKeys.add(hotKey);
    }

    public void addHotKeys(HotKey[] hotKeys) {
        if (hotKeys == null) return;
        for (HotKey hotKey : hotKeys) addHotKey(hotKey);
    }

    public void removeHotKey(HotKey hotKey) {
        if (hotKey == null) return;
        inputState.hotKeys.remove(hotKey);
    }

    public void removeHotKeys(HotKey[] hotKeys) {
        if (hotKeys == null) return;
        for (HotKey hotKey : hotKeys) removeHotKey(hotKey);
    }

    public void removeAllHotKeys() {
        removeHotKeys(inputState.hotKeys.toArray(new HotKey[]{}));
    }

    public ArrayList<HotKey> findHotKeysByName(String name) {
        if (name == null) return new ArrayList<>();
        return new ArrayList<>(inputState.hotKeys.stream().filter(hotKey -> name.equals(hotKey)).toList());
    }

    public HotKey findHotKeyByName(String name) {
        if (name == null) return null;
        ArrayList<HotKey> result = findHotKeysByName(name);
        return result.size() > 0 ? result.get(0) : null;
    }


    public ArrayList<Window> findWindowsByName(String name) {
        if (name == null) return new ArrayList<>();
        return new ArrayList<>(inputState.windows.stream().filter(window -> name.equals(window.name)).toList());
    }

    public Window findWindowByName(String name) {
        if (name == null) return null;
        ArrayList<Window> result = findWindowsByName(name);
        return result.size() > 0 ? result.get(0) : null;
    }

    public Window getLastActiveWindow() {
        return inputState.lastActiveWindow;
    }

    public static class _Config {
        private boolean keyBoardControlEnabled = false;
        private boolean mouseControlEnabled = true;
        private float keyBoardControlCursorSpeed = 4.0f;
        private boolean keyBoardControlMagnetModeEnabled = true;
        private int keyBoardControlButtonUp = Input.Keys.UP;
        private int keyBoardControlButtonDown = Input.Keys.DOWN;
        private int keyBoardControlButtonLeft = Input.Keys.LEFT;
        private int keyBoardControlButtonRight = Input.Keys.RIGHT;
        private int keyBoardControlButtonMouse1 = Input.Keys.CONTROL_LEFT;
        private int keyBoardControlButtonMouse2 = Input.Keys.CONTROL_RIGHT;
        private int keyBoardControlButtonMouse3 = Input.Keys.UNKNOWN;
        private int keyBoardControlButtonMouse4 = Input.Keys.UNKNOWN;
        private int keyBoardControlButtonMouse5 = Input.Keys.UNKNOWN;
        private int keyBoardControlButtonScrollUp = Input.Keys.PAGE_UP;
        private int keyBoardControlButtonScrollDown = Input.Keys.PAGE_DOWN;
        private boolean windowsDefaultEnforceScreenBounds = false;
        private FColor windowsDefaultColor = Tools.Colors.WHITE;
        private FColor componentsDefaultColor = Tools.Colors.WHITE;
        private FColor tooltipDefaultColor = Tools.Colors.WHITE;
        private CMediaCursor cursorGui = GUIBaseMedia.GUI_CURSOR_ARROW;
        private int gameViewportDefaultUpdateTime = 200;
        private CMediaFont tooltipDefaultFont = GUIBaseMedia.FONT_BLACK;
        private CMediaFont defaultFont = GUIBaseMedia.FONT_BLACK;
        private float dragAlpha = 0.8f;
        private int buttonHoldTimer = 8;
        private float knobSensitivity = 1f;
        private boolean foldWindowsOnDoubleClick = true;
        private int notificationsMax = 20;
        private int notificationsDefaultDisplayTime = 3000;
        private CMediaFont notificationsDefaultFont = GUIBaseMedia.FONT_WHITE;
        private FColor notificationsDefaultColor = Tools.Colors.GRAY_DARK;
        private int notificationsFadeoutTime = 200;
        private float notificationsScrollSpeed = 1;
        private int mapOverlayDefaultFadeoutTime = 200;
        private final HashSet<Character> textFieldDefaultAllowedCharacters = new HashSet<>();
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
            this.cursorGui = cursorGui;
        }

        public int getGameViewportDefaultUpdateTime() {
            return gameViewportDefaultUpdateTime;
        }

        public void setGameViewportDefaultUpdateTime(int gameViewportDefaultUpdateTime) {
            this.gameViewportDefaultUpdateTime = Tools.Calc.lowerBounds(gameViewportDefaultUpdateTime, 0);
        }

        public boolean isMouseControlEnabled() {
            return mouseControlEnabled;
        }

        public void setMouseControlEnabled(boolean mouseControlEnabled) {
            this.mouseControlEnabled = mouseControlEnabled;
        }

        public int getKeyBoardControlButtonMouse2() {
            return keyBoardControlButtonMouse2;
        }

        public void setKeyBoardControlButtonMouse2(int keyBoardControlButtonMouse2) {
            this.keyBoardControlButtonMouse2 = keyBoardControlButtonMouse2;
        }

        public int getKeyBoardControlButtonMouse3() {
            return keyBoardControlButtonMouse3;
        }

        public void setKeyBoardControlButtonMouse3(int keyBoardControlButtonMouse3) {
            this.keyBoardControlButtonMouse3 = keyBoardControlButtonMouse3;
        }

        public int getKeyBoardControlButtonMouse4() {
            return keyBoardControlButtonMouse4;
        }

        public void setKeyBoardControlButtonMouse4(int keyBoardControlButtonMouse4) {
            this.keyBoardControlButtonMouse4 = keyBoardControlButtonMouse4;
        }

        public int getKeyBoardControlButtonMouse5() {
            return keyBoardControlButtonMouse5;
        }

        public void setKeyBoardControlButtonMouse5(int keyBoardControlButtonMouse5) {
            this.keyBoardControlButtonMouse5 = keyBoardControlButtonMouse5;
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

        public float getDragAlpha() {
            return dragAlpha;
        }

        public void setDragAlpha(float dragAlpha) {
            this.dragAlpha = Tools.Calc.inBounds(dragAlpha, 0f, 1f);
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

        public boolean isKeyBoardControlEnabled() {
            return keyBoardControlEnabled;
        }

        public void setKeyBoardControlEnabled(boolean keyBoardControlEnabled) {
            this.keyBoardControlEnabled = keyBoardControlEnabled;
        }

        public float getKeyBoardControlCursorSpeed() {
            return keyBoardControlCursorSpeed;
        }

        public void setKeyBoardControlCursorSpeed(float keyBoardControlCursorSpeed) {
            this.keyBoardControlCursorSpeed = keyBoardControlCursorSpeed;
        }

        public boolean isKeyBoardControlMagnetModeEnabled() {
            return keyBoardControlMagnetModeEnabled;
        }

        public void setKeyBoardControlMagnetModeEnabled(boolean keyBoardControlMagnetModeEnabled) {
            this.keyBoardControlMagnetModeEnabled = keyBoardControlMagnetModeEnabled;
        }

        public int getKeyBoardControlButtonUp() {
            return keyBoardControlButtonUp;
        }

        public void setKeyBoardControlButtonUp(int keyBoardControlButtonUp) {
            this.keyBoardControlButtonUp = keyBoardControlButtonUp;
        }

        public int getKeyBoardControlButtonDown() {
            return keyBoardControlButtonDown;
        }

        public void setKeyBoardControlButtonDown(int keyBoardControlButtonDown) {
            this.keyBoardControlButtonDown = keyBoardControlButtonDown;
        }

        public int getKeyBoardControlButtonLeft() {
            return keyBoardControlButtonLeft;
        }

        public void setKeyBoardControlButtonLeft(int keyBoardControlButtonLeft) {
            this.keyBoardControlButtonLeft = keyBoardControlButtonLeft;
        }

        public int getKeyBoardControlButtonRight() {
            return keyBoardControlButtonRight;
        }

        public void setKeyBoardControlButtonRight(int keyBoardControlButtonRight) {
            this.keyBoardControlButtonRight = keyBoardControlButtonRight;
        }

        public int getKeyBoardControlButtonMouse1() {
            return keyBoardControlButtonMouse1;
        }

        public void setKeyBoardControlButtonMouse1(int keyBoardControlButtonMouse1) {
            this.keyBoardControlButtonMouse1 = keyBoardControlButtonMouse1;
        }

        public int getKeyBoardControlButtonScrollUp() {
            return keyBoardControlButtonScrollUp;
        }

        public void setKeyBoardControlButtonScrollUp(int keyBoardControlButtonScrollUp) {
            this.keyBoardControlButtonScrollUp = keyBoardControlButtonScrollUp;
        }

        public int getKeyBoardControlButtonScrollDown() {
            return keyBoardControlButtonScrollDown;
        }

        public void setKeyBoardControlButtonScrollDown(int keyBoardControlButtonScrollDown) {
            this.keyBoardControlButtonScrollDown = keyBoardControlButtonScrollDown;
        }

        public _Config() {
            this.textFieldDefaultAllowedCharacters.addAll(Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    ' ', '_', '.', ',', '!', '?'));
        }

        public void loadConfig(_Config config) {
            setWindowsDefaultEnforceScreenBounds(config.getWindowsDefaultEnforceScreenBounds());
            setWindowsDefaultColor(config.getWindowsDefaultColor());
            setComponentsDefaultColor(config.getComponentsDefaultColor());
            setTooltipDefaultColor(config.getWindowsDefaultColor());
            setCursorGui(config.getCursorGui());
            setGameViewportDefaultUpdateTime(config.getGameViewportDefaultUpdateTime());
            setTooltipDefaultFont(config.getTooltipDefaultFont());
            setDefaultFont(config.getDefaultFont());
            setDragAlpha(config.getDragAlpha());
            setButtonHoldTimer(config.getButtonHoldTimer());
            setKnobSensitivity(config.getKnobSensitivity());
            setFoldWindowsOnDoubleClick(config.isFoldWindowsOnDoubleClick());
            setNotificationsMax(config.getNotificationsMax());
            setNotificationsDefaultDisplayTime(config.getNotificationsDefaultDisplayTime());
            setNotificationsDefaultFont(config.getNotificationsDefaultFont());
            setNotificationsDefaultColor(config.getNotificationsDefaultColor());
            setNotificationsFadeoutTime(config.getNotificationsFadeoutTime());
            setNotificationsScrollSpeed(config.getNotificationsScrollSpeed());
            setMapOverlayDefaultFadeoutTime(config.getMapOverlayDefaultFadeoutTime());
            setTextFieldDefaultAllowedCharacters(config.getTextFieldDefaultAllowedCharacters());
            setTooltipFadeInTime(config.getTooltipFadeInTime());
            setTooltipFadeInDelayTime(config.getTooltipFadeInDelayTime());
            setKeyBoardControlEnabled(config.isKeyBoardControlEnabled());
            setKeyBoardControlCursorSpeed(config.getKeyBoardControlCursorSpeed());
            setKeyBoardControlMagnetModeEnabled(config.isKeyBoardControlMagnetModeEnabled());
            setKeyBoardControlButtonUp(config.getKeyBoardControlButtonUp());
            setKeyBoardControlButtonDown(config.getKeyBoardControlButtonDown());
            setKeyBoardControlButtonLeft(config.getKeyBoardControlButtonLeft());
            setKeyBoardControlButtonRight(config.getKeyBoardControlButtonRight());
            setKeyBoardControlButtonMouse1(config.getKeyBoardControlButtonMouse1());
            setKeyBoardControlButtonMouse2(config.getKeyBoardControlButtonMouse2());
            setKeyBoardControlButtonMouse3(config.getKeyBoardControlButtonMouse3());
            setKeyBoardControlButtonMouse4(config.getKeyBoardControlButtonMouse4());
            setKeyBoardControlButtonMouse5(config.getKeyBoardControlButtonMouse5());
            setKeyBoardControlButtonScrollDown(config.getKeyBoardControlButtonScrollDown());
            setKeyBoardControlButtonScrollUp(config.getKeyBoardControlButtonScrollUp());
            setMouseControlEnabled(config.isMouseControlEnabled());
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
            if (inputState.inventoryDrag_Inventory != null) return true;
            if (inputState.listDrag_List != null) return true;
            return false;
        }

        /* Events */

        public Object lastGUIMouseHover() {
            return inputState.lastGUIMouseHover;
        }

        public int mouseXGUI() {
            return inputState.mouse_gui.x;
        }

        public int mouseYGUI() {
            return inputState.mouse_gui.y;
        }

        public int mouseX() {
            return inputState.mouse.x;
        }

        public int mouseY() {
            return inputState.mouse.y;
        }

        public int mouseXDelta() {
            return inputState.mouse_delta.x;
        }

        public int mouseYDelta() {
            return inputState.mouse_delta.y;
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

        /* Key Up */

        public ArrayList<Integer> keyUpKeys() {
            return new ArrayList<>(inputState.inputEvents.keyUpKeyCodes);
        }

        public boolean keyUpKey(int keyCode) {
            for (int i = 0; i < inputState.inputEvents.keyUpKeyCodes.size(); i++) {
                if (keyCode == inputState.inputEvents.keyUpKeyCodes.get(i)) return true;
            }
            return false;
        }

        public boolean isKeyUp(int keyCode) {
            return !inputState.inputEvents.keysDown[keyCode];
        }


        /* Key Down */

        public ArrayList<Integer> keyDownKeys() {
            return new ArrayList<>(inputState.inputEvents.keyDownKeyCodes);
        }

        public boolean keyDownKey(int keyCode) {
            for (int i = 0; i < inputState.inputEvents.keyDownKeyCodes.size(); i++) {
                if (keyCode == inputState.inputEvents.keyDownKeyCodes.get(i)) return true;
            }
            return false;
        }

        public boolean isKeyDown(int keyCode) {
            return inputState.inputEvents.keysDown[keyCode];
        }

        /* Character Typed */

        public boolean keyTypedCharacter(Character character) {
            for (int i = 0; i < inputState.inputEvents.keyTypedCharacters.size(); i++) {
                if (character == inputState.inputEvents.keyTypedCharacters.get(i)) return true;
            }
            return false;
        }

        public ArrayList<Character> keyTypedCharacters() {
            return new ArrayList<>(inputState.inputEvents.keyTypedCharacters);
        }


        /* Mouse Up */
        public ArrayList<Integer> mouseUpButtons() {
            return new ArrayList<>(inputState.inputEvents.mouseUpButtons);
        }

        public boolean mouseUpButton(int button) {
            for (int i = 0; i < inputState.inputEvents.mouseUpButtons.size(); i++) {
                if (button == inputState.inputEvents.mouseUpButtons.get(i)) return true;
            }
            return false;
        }

        public boolean isMouseButtonUp(int button) {
            return !inputState.inputEvents.mouseButtonsDown[button];
        }

        /* Mouse Down */

        public ArrayList<Integer> mouseDownButtons() {
            return new ArrayList<>(inputState.inputEvents.mouseDownButtons);
        }

        public boolean mouseDownButton(int button) {
            for (int i = 0; i < inputState.inputEvents.mouseDownButtons.size(); i++) {
                if (button == inputState.inputEvents.mouseDownButtons.get(i)) return true;
            }
            return false;
        }

        public boolean isMouseButtonDown(int button) {
            return inputState.inputEvents.mouseButtonsDown[button];
        }

        /* Other */

        public float mouseScrolledAmount() {
            return inputState.inputEvents.mouseScrolledAmount;
        }

        public boolean isMouseControlActive() {
            return inputState.controlMode == ControlMode.MOUSE;
        }

        public ControlMode getControlMode() {
            return inputState.controlMode;
        }

        public void setMousePosition(int x, int y) {
            inputState.lastGUIMouseHover = null;
            inputState.mouse_gui.x = x;
            inputState.mouse_gui.y = y;
        }
    }

    public class _Notification {
        public Notification create(String text) {
            return create(text, config.notificationsDefaultColor, config.notificationsDefaultFont, config.notificationsDefaultDisplayTime, null);
        }

        public Notification create(String text, FColor color) {
            return create(text, color, config.notificationsDefaultFont, config.notificationsDefaultDisplayTime, null);
        }

        public Notification create(String text, FColor color, CMediaFont font) {
            return create(text, color, font, config.notificationsDefaultDisplayTime, null);
        }

        public Notification create(String text, FColor color, CMediaFont font, int displayTime) {
            return create(text, color, font, displayTime, null);
        }

        public Notification create(String text, FColor color, CMediaFont font, int displayTime, NotificationAction notificationAction) {
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

        public void setDisplayTime(Notification notification, int displayTime) {
            if (notification == null) return;
            notification.displayTime = Tools.Calc.lowerBounds(displayTime, 0);
        }

        public void setColor(Notification notification, FColor color) {
            if (notification == null || color == null) return;
            setColor(notification, color.r, color.g, color.b, color.a);
        }

        public void setColor(Notification notification, float r, float g, float b, float a) {
            if (notification == null) return;
            notification.color_r = r;
            notification.color_g = g;
            notification.color_b = b;
            notification.color_a = a;
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

        private ContextMenuAction defaultContextMenuAction() {
            return new ContextMenuAction() {
            };
        }

        public final _ContextMenuItem item = new _ContextMenuItem();

        public ContextMenu create(ContextMenuItem[] contextMenuItems) {
            return create(contextMenuItems, defaultContextMenuAction(), 1f);
        }

        public ContextMenu create(ContextMenuItem[] contextMenuItems, ContextMenuAction contextMenuAction) {
            return create(contextMenuItems, defaultContextMenuAction(), 1f);
        }

        public ContextMenu create(ContextMenuItem[] contextMenuItems, ContextMenuAction contextMenuAction, float alpha) {
            ContextMenu contextMenu = new ContextMenu();
            contextMenu.items = new ArrayList<>();
            setAlpha(contextMenu, alpha);
            addContextMenuItems(contextMenu, contextMenuItems);
            setContextMenuAction(contextMenu, contextMenuAction);
            return contextMenu;
        }

        public void setContextMenuAction(ContextMenu contextMenu, ContextMenuAction contextMenuAction) {
            if (contextMenu == null) return;
            contextMenu.contextMenuAction = contextMenuAction;
        }

        public void setAlpha(ContextMenu contextMenu, float alpha) {
            if (contextMenu == null) return;
            contextMenu.color_a = Tools.Calc.inBounds(alpha, 0f, 1f);
        }

        public void addContextMenuItem(ContextMenu contextMenu, ContextMenuItem contextMenuItem) {
            if (contextMenu == null || contextMenuItem == null) return;
            UICommons.contextMenu_addItem(contextMenu, contextMenuItem);
        }

        public void addContextMenuItems(ContextMenu contextMenu, ContextMenuItem[] contextMenuItems) {
            if (contextMenu == null || contextMenuItems == null) return;
            for (ContextMenuItem contextMenuItem : contextMenuItems) addContextMenuItem(contextMenu, contextMenuItem);
        }

        public void removeContextMenuItem(ContextMenu contextMenu, ContextMenuItem contextMenuItem) {
            if (contextMenu == null || contextMenuItem == null) return;
            UICommons.contextMenu_removeItem(contextMenu, contextMenuItem);
        }

        public void removeContextMenuItems(ContextMenu contextMenu, ContextMenuItem[] contextMenuItems) {
            if (contextMenu == null || contextMenuItems == null) return;
            for (ContextMenuItem contextMenuItem : contextMenuItems)
                removeContextMenuItem(contextMenu, contextMenuItem);
        }

        public void removeAllContextMenuItems(ContextMenu contextMenu) {
            if (contextMenu == null) return;
            removeContextMenuItems(contextMenu, contextMenu.items.toArray(new ContextMenuItem[]{}));
        }

        public ArrayList<ContextMenuItem> findContextMenuItemsByName(ContextMenu contextMenu, String name) {
            if (contextMenu == null || name == null) return new ArrayList<>();
            return new ArrayList(contextMenu.items.stream().filter(contextMenuItem -> name.equals(contextMenuItem)).toList());
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
                return create(text, defaultContextMenuItemAction(), null, config.componentsDefaultColor, null);
            }

            public ContextMenuItem create(String text, ContextMenuItemAction contextMenuItemAction) {
                return create(text, contextMenuItemAction, null, config.componentsDefaultColor, null);
            }

            public ContextMenuItem create(String text, ContextMenuItemAction contextMenuItemAction, CMediaGFX icon) {
                return create(text, contextMenuItemAction, icon, config.componentsDefaultColor, null);
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
                contextMenuItem.addedToContextMenu = null;
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
                setColor(contextMenuItem, color.r, color.b, color.g);
            }

            public void setColor(ContextMenuItem contextMenuItem, float r, float g, float b) {
                if (contextMenuItem == null) return;
                contextMenuItem.color_r = r;
                contextMenuItem.color_g = g;
                contextMenuItem.color_b = b;
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
            window.messageReceiverActions = new ArrayList<>();
            window.updateActions = new ArrayList<>();
            window.addedToScreen = false;
            addComponents(window, components);
            return window;
        }

        public void addMessageReceiverAction(Window window, MessageReceiverAction messageReceiverAction) {
            if (window == null || messageReceiverAction == null) return;
            window.messageReceiverActions.add(messageReceiverAction);
        }

        public void addMessageReceiverActions(Window window, MessageReceiverAction[] messageReceiverActions) {
            if (window == null || messageReceiverActions == null) return;
            for (MessageReceiverAction messageReceiverAction : messageReceiverActions)
                addMessageReceiverAction(window, messageReceiverAction);
        }

        public void removeMessageReceiverAction(Window window, MessageReceiverAction messageReceiverAction) {
            if (window == null || messageReceiverAction == null) return;
            window.messageReceiverActions.remove(messageReceiverAction);
        }

        public void removeMessageReceiverActions(Window window, MessageReceiverAction[] messageReceiverActions) {
            if (window == null || messageReceiverActions == null) return;
            for (MessageReceiverAction messageReceiverAction : messageReceiverActions)
                removeMessageReceiverAction(window, messageReceiverAction);
        }

        public void removeAllMessageReceiverActions(Window window) {
            if (window == null) return;
            removeMessageReceiverActions(window, window.messageReceiverActions.toArray(new MessageReceiverAction[]{}));
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

        public boolean isAddedToScreen(Window window) {
            if (window == null) return false;
            return window.addedToScreen;
        }

        private void setColorFunction(Window window, FColor color, int setColorMode, Class[] classes,
                                      boolean windowColor, boolean componentColor1, boolean componentColor2, boolean comboBoxItemColor) {
            if (classes == null) classes = new Class[]{};
            if (windowColor) setColor(window, color);
            for (Component component : window.components) {
                boolean match = switch (setColorMode) {
                    // 1 = INCLUDE Mode
                    case 1 -> Arrays.stream(classes).anyMatch(componentClass -> componentClass == component.getClass());
                    // 2 = EXCEPT Mode
                    case 2 ->
                            Arrays.stream(classes).noneMatch(componentClass -> componentClass == component.getClass());
                    default -> throw new IllegalStateException("Unexpected value: " + setColorMode);
                };
                if (match) {
                    if (componentColor1) components.setColor(component, color);
                    if (componentColor2) components.setColor2(component, color);
                    if (component.getClass() == ComboBox.class) {
                        ComboBox comboBox = (ComboBox) component;
                        for (ComboBoxItem comboBoxItem : comboBox.items)
                            components.comboBox.item.setColor(comboBoxItem, color);
                    }
                }
            }
        }

        public void setColorEverything(Window window, FColor color) {
            setColorFunction(window, color, 2, null,
                    true, true, true, true);
        }

        public void setColorEverything(Window window, FColor color, boolean windowColor, boolean componentColor1, boolean componentColor2, boolean comboBoxItems) {
            setColorFunction(window, color, 2, null,
                    windowColor, componentColor1, componentColor2, comboBoxItems);
        }

        public void setColorEverythingExcept(Window window, FColor color, Class[] exceptions) {
            setColorFunction(window, color, 2, exceptions,
                    true, true, true, true);
        }

        public void setColorEverythingExcept(Window window, FColor color, Class[] exceptions, boolean windowColor, boolean componentColor1, boolean componentColor2, boolean comboBoxItems) {
            setColorFunction(window, color, 2, exceptions,
                    windowColor, componentColor1, componentColor2, comboBoxItems);
        }


        public void setColorEverythingInclude(Window window, FColor color, Class[] inclusions) {
            setColorFunction(window, color, 1, inclusions,
                    true, true, true, true);
        }

        public void setColorEverythingInclude(Window window, FColor color, Class[] inclusions, boolean windowColor, boolean componentColor1, boolean componentColor2, boolean comboBoxItems) {
            setColorFunction(window, color, 1, inclusions,
                    windowColor, componentColor1, componentColor2, comboBoxItems);
        }

        public int getRealWidth(Window window) {
            return UICommons.window_getRealWidth(window);
        }

        public int getRealHeight(Window window) {
            return UICommons.window_getRealHeight(window);
        }

        public void setColorEverythingInclude(Window window, FColor color, Class[] inclusions, boolean setColor1, boolean setColor2, boolean includeWindow) {
            if (window == null) return;

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

            return windowGenerator.create(p);
        }


        public void addComponent(Window window, Component component) {
            if (window == null || component == null) return;
            UICommons.component_addToWindow(component, inputState, window);
        }

        public void addComponents(Window window, Component[] components) {
            if (window == null || components == null) return;
            for (Component component : components) addComponent(window, component);
        }

        public void removeComponent(Window window, Component component) {
            if (window == null || component == null) return;
            UICommons.component_removeFromWindow(component, inputState, window);
        }

        public void removeComponents(Window window, Component[] components) {
            if (window == null || components == null) return;
            for (Component component : components) removeComponent(window, component);
        }

        public void removeAllComponents(Window window) {
            if (window == null) return;
            removeComponents(window, window.components.toArray(new Component[]{}));
        }

        public ArrayList<Component> findComponentsByName(Window window, String name) {
            if (window == null || name == null) return new ArrayList<>();
            return new ArrayList<>(window.components.stream().filter(component -> name.equals(component.name)).toList());
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

        public void addUpdateActions(Window window, UpdateAction[] updateActions) {
            if (window == null || updateActions == null) return;
            for (UpdateAction updateAction : updateActions) addUpdateAction(window, updateAction);
        }

        public void removeUpdateAction(Window window, UpdateAction updateAction) {
            if (window == null || updateAction == null) return;
            window.updateActions.remove(updateAction);
        }

        public void removeUpdateActions(Window window, UpdateAction[] updateActions) {
            if (window == null || updateActions == null) return;
            for (UpdateAction updateAction : updateActions) removeUpdateAction(window, updateAction);
        }

        public void removeAllUpdateActions(Window window) {
            if (window == null) return;
            removeUpdateActions(window, window.updateActions.toArray(new UpdateAction[]{}));
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
            setColor(window, color.r, color.g, color.b, color.a);
        }

        public void setColor(Window window, float r, float g, float b, float a) {
            if (window == null) return;
            window.color_r = r;
            window.color_g = g;
            window.color_b = b;
            window.color_a = a;
        }

        public void setAlpha(Window window, float transparency) {
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

        public final _ToolTipImage toolTipImage = new _ToolTipImage();

        public class _ToolTipImage {

            public ToolTipImage create(CMediaGFX image, int offset_x, int offset_y) {
                return create(image, offset_x, offset_y, Tools.Colors.WHITE);
            }

            public ToolTipImage create(CMediaGFX image, int offset_x, int offset_y, FColor color) {
                ToolTipImage toolTipImage = new ToolTipImage();
                setImage(toolTipImage, image);
                setPosition(toolTipImage, offset_x, offset_y);
                setColor(toolTipImage, color);
                return toolTipImage;
            }

            public void setImage(ToolTipImage toolTipImage, CMediaGFX image) {
                if (toolTipImage == null) return;
                toolTipImage.image = image;
            }

            public void setPosition(ToolTipImage toolTipImage, int x, int y) {
                if (toolTipImage == null) return;
                toolTipImage.x = x;
                toolTipImage.y = y;
            }

            public void setColor(ToolTipImage toolTipImage, FColor color) {
                if (toolTipImage == null) return;
                setColor(toolTipImage, color.r, color.g, color.b, color.a);
            }

            public void setColor(ToolTipImage toolTipImage, float r, float g, float b, float a) {
                if (toolTipImage == null) return;
                toolTipImage.color_r = r;
                toolTipImage.color_g = g;
                toolTipImage.color_b = b;
                toolTipImage.color_a = a;
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
            addToolTipImages(tooltip, images);
            return tooltip;
        }

        public void addToolTipImage(ToolTip toolTip, ToolTipImage toolTipImage) {
            if (toolTip == null || toolTipImage == null) return;
            UICommons.toolTip_addToolTipImage(toolTip, toolTipImage);
        }

        public void addToolTipImages(ToolTip toolTip, ToolTipImage[] images) {
            if (toolTip == null || images == null) return;
            for (ToolTipImage toolTipImage : images) addToolTipImage(toolTip, toolTipImage);
        }

        public void removeToolTipImage(ToolTip toolTip, ToolTipImage toolTipImage) {
            if (toolTip == null || toolTipImage == null) return;
            UICommons.toolTip_removeToolTipImage(toolTip, toolTipImage);
        }

        public void removeToolTipImages(ToolTip toolTip, ToolTipImage[] toolTipImages) {
            if (toolTip == null || toolTipImages == null) return;
            for (ToolTipImage toolTipImage : toolTipImages) removeToolTipImage(toolTip, toolTipImage);
        }

        public void removeAllToolTipImages(ToolTip toolTip) {
            if (toolTip == null) return;
            removeToolTipImages(toolTip, toolTip.images.toArray(new ToolTipImage[]{}));
        }

        public void setToolTipAction(ToolTip toolTip, ToolTipAction toolTipAction) {
            toolTip.toolTipAction = toolTipAction;
        }

        public void setDisplayFistLineAsTitle(ToolTip tooltip, boolean firstLineIsTitle) {
            if (tooltip == null) return;
            tooltip.displayFistLineAsTitle = firstLineIsTitle;
        }

        public void setLines2(ToolTip tooltip, String... lines) {
            setLines(tooltip, lines);
        }

        public void setLines(ToolTip tooltip, String[] lines) {
            if (tooltip == null) return;
            tooltip.lines = Tools.Text.validString(lines);
        }

        public void setColor(ToolTip tooltip, FColor color) {
            if (tooltip == null || color == null) return;
            setColor(tooltip, color.r, color.g, color.b, color.a);
        }

        public void setColor(ToolTip tooltip, float r, float g, float b, float a) {
            if (tooltip == null) return;
            tooltip.color_r = r;
            tooltip.color_g = g;
            tooltip.color_b = b;
            tooltip.color_a = a;
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
            setTestingCameraTo(inputState.camera_x, inputState.camera_y, inputState.camera_z, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
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
            setTestingCameraTo(inputState.camera_x, inputState.camera_y, inputState.camera_z, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
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
            setTestingCameraTo(inputState.camera_x, inputState.camera_y, inputState.camera_z, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
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
        }

        public float viewPortStretchFactorWidth() {
            return inputState.viewport_screen.getWorldWidth() / (float) inputState.viewport_screen.getScreenWidth();
        }

        public float viewPortStretchFactorHeight() {
            return inputState.viewport_screen.getWorldHeight() / (float) inputState.viewport_screen.getScreenHeight();
        }

        public void moveAbs(float x, float y) {
            moveAbs(x, y, inputState.camera_z);
        }

        public void moveAbs(float x, float y, float z) {
            inputState.camera_x = x;
            inputState.camera_y = y;
            inputState.camera_z = z;
        }

        public void moveRel(float x, float y) {
            moveRel(x, y, inputState.camera_z);
        }

        public void moveRel(float x, float y, float z) {
            inputState.camera_x += x;
            inputState.camera_y += y;
            inputState.camera_y += z;
        }

        public void xRel(float x) {
            inputState.camera_x += x;
        }

        public void xAbs(float x) {
            inputState.camera_x = x;
        }

        public void yRel(float y) {
            inputState.camera_y += y;
        }

        public void yAbs(float zoom) {
            inputState.camera_y = zoom;
        }

        public void zRel(float z) {
            inputState.camera_z += z;
        }

        public void zAbs(float z) {
            inputState.camera_z = z;
        }

        public void zoomRel(float z) {
            inputState.camera_zoom += z;
        }

        public void zoomAbs(float z) {
            inputState.camera_zoom = z;
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

        public void addUpdateActions(Component component, UpdateAction[] updateActions) {
            if (component == null || updateActions == null) return;
            for (UpdateAction updateAction : updateActions) addUpdateAction(component, updateAction);
        }

        public void removeUpdateAction(Component component, UpdateAction updateAction) {
            if (component == null || updateAction == null) return;
            component.updateActions.remove(updateAction);
        }

        public void removeUpdateActions(Component component, UpdateAction[] updateActions) {
            if (component == null || updateActions == null) return;
            for (UpdateAction updateAction : updateActions) removeUpdateAction(component, updateAction);
        }

        public void removeAllUpdateActions(Component component) {
            if (component == null) return;
            removeUpdateActions(component, component.updateActions.toArray(new UpdateAction[]{}));
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

        public void setColor(Component[] components, FColor color) {
            if (components == null) return;
            for (Component component : components) setColor(component, color);
        }

        public void setColor(Component component, FColor color) {
            if (component == null || color == null) return;
            setColor(component, color.r, color.g, color.b, color.a);
        }

        public void setColor(Component component, float r, float g, float b, float a) {
            if (component == null) return;
            component.color_r = r;
            component.color_g = g;
            component.color_b = b;
            component.color_a = a;
        }

        public void setColor2(Component[] components, FColor color2) {
            if (components == null) return;
            for (Component component : components) setColor2(component, color2);
        }

        public void setColor2(Component component, FColor color) {
            if (component == null || color == null) return;
            setColor2(component, color.r, color.g, color.b, color.a);
        }

        public void setColor2(Component component, float r, float g, float b, float a) {
            if (component == null) return;
            component.color2_r = r;
            component.color2_g = g;
            component.color2_b = b;
            component.color2_a = a;
        }

        public void setColor1And2(Component component, FColor color) {
            if (component == null) return;
            setColor(component, color);
            setColor2(component, color);
        }

        public void setColor1And2(Component[] components, FColor color) {
            if (components == null) return;
            for (Component component : components) {
                setColor(component, color);
                setColor2(component, color);
            }
        }

        public void setAlpha(Component component, float alpha) {
            if (component == null) return;
            component.color_a = alpha;
        }

        public void setAlpha(Component[] components, float alpha) {
            if (components == null) return;
            for (Component component : components) setAlpha(component, alpha);
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
            component.addedToScreen = false;
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

        public boolean isAddedToWindow(Component component, Window window) {
            if (component == null || window == null) return false;
            return component.addedToWindow != null && component.addedToWindow == window;
        }

        public boolean isAddedToScreen(Component component) {
            if (component == null) return false;
            return component != null && component.addedToScreen;
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
                gameViewPort.updateTime = Tools.Calc.lowerBounds(updateTime == null ? config.gameViewportDefaultUpdateTime : updateTime, 0);
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

            public void setButtonMode(Button button, ButtonMode buttonMode) {
                if (button == null) return;
                button.mode = buttonMode;
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

            private void setButtonValues(Button button, ButtonAction buttonAction, ButtonMode buttonMode, int contentOffsetX, int contentOffsetY) {
                setButtonAction(button, buttonAction);
                setButtonMode(button, buttonMode);
                setPressed(button, false);
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
                    return create(x, y, width, height, text, defaultButtonAction(), null, ButtonMode.DEFAULT, 0, 0, null);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction) {
                    return create(x, y, width, height, text, buttonAction, null, ButtonMode.DEFAULT, 0, 0, null);
                }


                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon) {
                    return create(x, y, width, height, text, buttonAction, icon, ButtonMode.DEFAULT, 0, 0, null);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon, ButtonMode buttonMode) {
                    return create(x, y, width, height, text, buttonAction, icon, buttonMode, 0, 0, null);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon, ButtonMode buttonMode, int contentOffsetX, int contentOffsetY) {
                    return create(x, y, width, height, text, buttonAction, icon, buttonMode, contentOffsetX, contentOffsetY, null);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon, ButtonMode buttonMode, int contentOffsetX, int contentOffsetY, CMediaFont font) {
                    TextButton textButton = new TextButton();
                    setComponentInitValues(textButton);
                    setButtonValues(textButton, buttonAction, buttonMode, contentOffsetX, contentOffsetY);
                    setPosition(textButton, x, y);
                    setSize(textButton, width, height);
                    setText(textButton, text);
                    setFont(textButton, font);
                    setIcon(textButton, icon);
                    setIconArrayIndex(textButton, 0);
                    centerContent(textButton);
                    return textButton;
                }

                public void setIcon(TextButton textButton, CMediaGFX icon) {
                    if (textButton == null) return;
                    textButton.icon = icon;
                }

                public void setIconArrayIndex(TextButton textButton, int iconArrayIndex) {
                    if (textButton == null) return;
                    textButton.iconArrayIndex = Tools.Calc.lowerBounds(iconArrayIndex, 0);
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
                    return create(x, y, width, height, image, 0, defaultButtonAction(), ButtonMode.DEFAULT, 0, 0);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex) {
                    return create(x, y, width, height, image, arrayIndex, defaultButtonAction(), ButtonMode.DEFAULT, 0, 0);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex, ButtonAction buttonAction) {
                    return create(x, y, width, height, image, arrayIndex, buttonAction, ButtonMode.DEFAULT, 0, 0);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex, ButtonAction buttonAction,  ButtonMode buttonMode) {
                    return create(x, y, width, height, image, arrayIndex, buttonAction, buttonMode, 0, 0);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex, ButtonAction buttonAction, ButtonMode buttonMode, int contentOffsetX, int contentOffsetY) {
                    ImageButton imageButton = new ImageButton();
                    setComponentInitValues(imageButton);
                    setButtonValues(imageButton, buttonAction, buttonMode, contentOffsetX, contentOffsetY);
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

            public TabBar create(int x, int y, int width, Tab[] tabs) {
                return create(x, y, width, tabs, 0, null, false, 0, 0, false);
            }

            public TabBar create(int x, int y, int width, Tab[] tabs, int selectedTab) {
                return create(x, y, width, tabs, selectedTab, null, false, 0, 0, false);
            }

            public TabBar create(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction) {
                return create(x, y, width, tabs, selectedTab, tabBarAction, false, 0, 0, false);
            }

            public TabBar create(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight) {
                return create(x, y, width, tabs, selectedTab, tabBarAction, border, borderHeight, 0, false);
            }

            public TabBar create(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight, int tabOffset) {
                return create(x, y, width, tabs, selectedTab, tabBarAction, border, borderHeight, tabOffset, false);
            }

            public TabBar create(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight, int tabOffset, boolean bigIconMode) {
                TabBar tabBar = new TabBar();
                tabBar.tabs = new ArrayList<>();
                setComponentInitValues(tabBar);
                setPosition(tabBar, x, y);
                setSize(tabBar, width, bigIconMode ? 2 : 1);
                removeAllTabs(tabBar);
                addTabs(tabBar, tabs);
                selectTab(tabBar, selectedTab);
                setTabBarAction(tabBar, tabBarAction);
                setBorder(tabBar, border);
                setBorderHeight(tabBar, borderHeight);
                setTabOffset(tabBar, tabOffset);
                setBigIconMode(tabBar, bigIconMode);
                return tabBar;
            }

            public void setTabOffset(TabBar tabBar, int tabOffset) {
                if (tabBar == null) return;
                tabBar.tabOffset = Tools.Calc.lowerBounds(tabOffset, 0);
            }

            public void setBigIconMode(TabBar tabBar, boolean bigIconMode) {
                if (tabBar == null) return;
                tabBar.bigIconMode = bigIconMode;
            }

            public void setBorder(TabBar tabBar, boolean border) {
                tabBar.border = border;
            }

            public void setBorderHeight(TabBar tabBar, int borderHeight) {
                tabBar.borderHeight = Tools.Calc.lowerBounds(borderHeight, 0);
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

            public void addTab(TabBar tabBar, Tab tab) {
                if (tabBar == null || tab == null) return;
                UICommons.tabBar_addTab(tabBar, tab);
            }

            public void addTab(TabBar tabBar, Tab tab, int index) {
                if (tabBar == null || tab == null) return;
                if (tab.addedToTabBar == null && !tabBar.tabs.contains(tab)) {
                    UICommons.tabBar_addTab(tabBar, tab, index);
                    tabBar.tabs.add(index, tab);
                }
            }

            public void addTabs(TabBar tabBar, Tab[] tabs) {
                if (tabBar == null || tabs == null) return;
                for (Tab tab : tabs) addTab(tabBar, tab);
            }

            public void removeTab(TabBar tabBar, Tab tab) {
                if (tabBar == null || tab == null) return;
                UICommons.tabBar_removeTab(tabBar, tab);
            }

            public void removeTabs(TabBar tabBar, Tab[] tabs) {
                if (tabBar == null || tabs == null) return;
                for (Tab tab : tabs) removeTab(tabBar, tab);
            }

            public void removeAllTabs(TabBar tabBar) {
                if (tabBar == null) return;
                removeTabs(tabBar, tabBar.tabs.toArray(new Tab[]{}));
            }

            public ArrayList<Tab> findTabsByName(TabBar tabBar, String name) {
                if (tabBar == null || name == null) return new ArrayList<>();
                return new ArrayList<>(tabBar.tabs.stream().filter(tab1 -> name.equals(tab1.name)).toList());
            }

            public Tab findTabByName(TabBar tabBar, String name) {
                if (tabBar == null || name == null) return null;
                ArrayList<Tab> result = findTabsByName(tabBar, name);
                return result.size() > 0 ? result.get(0) : null;
            }

            public boolean isTabVisible(TabBar tabBar, Tab tab) {
                if (tabBar == null || tab == null) return false;
                int xOffset = 0;
                for (Tab tabB : tabBar.tabs) {
                    xOffset += tabB.width;
                    if (tabB == tab) {
                        return xOffset <= tabBar.width;
                    }
                }
                return false;
            }

            public int getTabsWidth(TabBar tabBar) {
                int width = 0;
                for (Tab tab : tabBar.tabs) {
                    width += tab.width;
                }
                return width;
            }

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
                        updateWidthAuto(tab);
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

                public void addTabComponent(Tab tab, Component component) {
                    if (tab == null || component == null) return;
                    UICommons.tab_addComponent(tab, component);
                }

                public void addTabComponents(Tab tab, Component[] components) {
                    if (tab == null || components == null) return;
                    for (Component component : components) addTabComponent(tab, component);
                }

                public void removeTabComponent(Tab tab, Component component) {
                    if (tab == null || component == null) return;
                    UICommons.tab_removeComponent(tab, component);
                }

                public void removeTabComponents(Tab tab, Component[] components) {
                    if (tab == null || components == null) return;
                    for (Component component : components) removeTabComponent(tab, component);
                }

                public void removeAllTabComponents(Tab tab) {
                    if (tab == null) return;
                    removeTabComponents(tab, tab.components.toArray(new Component[]{}));
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

                public void updateWidthAuto(Tab tab) {
                    if (tab == null) return;
                    int width = MathUtils.round((mediaManager.textWidth(tab.font, tab.title) + (tab.icon != null ? UIEngine.TILE_SIZE : 0) + UIEngine.TILE_SIZE) / (float) UIEngine.TILE_SIZE);
                    setWidth(tab, width);
                }

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
                updateSize(inventory);
                return inventory;
            }

            public void setDoubleSized(Inventory inventory, boolean doubleSized) {
                inventory.doubleSized = doubleSized;
                updateSize(inventory);
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
                updateSize(inventory);
            }

            private void updateSize(Inventory inventory) {
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
                textField.contentValid = textField.textFieldAction == null || textField.textFieldAction.isContentValid(textField.content);
                return textField;
            }

            public void setMarkerPosition(TextField textField, int position) {
                if (textField == null) return;
                UICommons.textField_setMarkerPosition(mediaManager, textField, position);
            }

            public void setContent(TextField textField, String content) {
                if (textField == null) return;
                UICommons.textField_setContent(textField, content);
            }

            public void setFont(TextField textField, CMediaFont font) {
                if (textField == null) return;
                textField.font = font == null ? config.defaultFont : font;
            }

            public void setTextFieldAction(TextField textField, TextFieldAction textFieldAction) {
                if (textField == null) return;
                textField.textFieldAction = textFieldAction;
                UICommons.textField_setContent(textField, textField.content); // Trigger validation
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
                UICommons.textField_unFocus(inputState, textField);
            }

            public void focus(TextField textField) {
                if (textField == null) return;
                UICommons.textField_focus(inputState, textField);
            }

            public boolean isFocused(TextField textField) {
                return UICommons.textField_isFocused(inputState, textField);
            }

            public boolean isContentValid(TextField textField) {
                if (textField == null) return false;
                return textField.contentValid;
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
                map.mapOverlays = new ArrayList<>();
                setComponentInitValues(map);
                setColor(map, Tools.Colors.WHITE);
                map.pMap = new Pixmap(width * UIEngine.TILE_SIZE, height * UIEngine.TILE_SIZE, Pixmap.Format.RGBA8888);
                setPosition(map, x, y);
                setSize(map, width, height);
                setMapAction(map, mapAction);
                addMapOverlays(map, mapOverlays);
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
            }

            public FColor getPixelColor(Map map, int x, int y) {
                if (map == null) return null;
                return Tools.Colors.createFromInt(map.pMap.getPixel(x, y));
            }

            public void clearMap(Map map, FColor fColor) {
                clearMap(map, fColor);
            }

            public void clearMap(Map map, float r, float g, float b, float a) {
                if (map == null) return;
                map.pMap.setColor(r, g, b, a);
                for (int iy = 0; iy < map.pMap.getHeight(); iy++) {
                    map.pMap.drawLine(0, iy, map.pMap.getWidth(), iy);
                }
            }

            public void drawPixel(Map map, int x, int y, FColor fColor) {
                drawPixel(map, x, y, fColor.r, fColor.g, fColor.b, fColor.a);
            }

            public void drawPixel(Map map, int x, int y, float r, float g, float b, float a) {
                if (map == null) return;
                map.pMap.setColor(r, g, b, a);
                map.pMap.drawPixel(x, y);
            }

            public void drawLine(Map map, int x1, int y1, int x2, int y2, FColor fColor) {
                drawLine(map, x1, y1, x2, y2, fColor.r, fColor.g, fColor.b, fColor.a);
            }

            public void drawLine(Map map, int x1, int y1, int x2, int y2, float r, float g, float b, float a) {
                if (map == null) return;
                map.pMap.setColor(r, g, b, a);
                map.pMap.drawLine(x1, y1, x2, y2);
            }

            public void drawRect(Map map, int x1, int y1, int width, int height, FColor fColor) {
                drawRect(map, x1, y1, width, height, fColor.r, fColor.g, fColor.b, fColor.a);
            }

            public void drawRect(Map map, int x1, int y1, int width, int height, float r, float g, float b, float a) {
                if (map == null) return;
                map.pMap.setColor(r, g, b, a);
                map.pMap.drawRectangle(x1, y1, width, height);
            }

            public void drawCircle(Map map, int x, int y, int radius, FColor fColor) {
                drawCircle(map, x, y, radius, fColor.r, fColor.g, fColor.b, fColor.a);
            }

            public void drawCircle(Map map, int x, int y, int radius, float r, float g, float b, float a) {
                if (map == null) return;
                map.pMap.setColor(r, g, b, a);
                map.pMap.drawCircle(x, y, radius);
            }

            public void addMapOverlay(Map map, MapOverlay mapOverlay) {
                if (map == null || mapOverlay == null) return;
                UICommons.map_addMapOverlay(map, mapOverlay);
            }

            public void addMapOverlays(Map map, MapOverlay[] mapOverlays) {
                if (map == null || mapOverlays == null) return;
                for (MapOverlay mapOverlay : mapOverlays) addMapOverlay(map, mapOverlay);
            }

            public void removeMapOverlay(Map map, MapOverlay mapOverlay) {
                if (map == null || mapOverlay == null) return;
                UICommons.map_removeMapOverlay(map, mapOverlay);
            }

            public void removeMapOverlays(Map map, MapOverlay[] mapOverlays) {
                if (map == null || mapOverlays == null) return;
                for (MapOverlay mapOverlay : mapOverlays) removeMapOverlay(map, mapOverlay);
            }

            public void removeAllMapOverlays(Map map) {
                if (map == null) return;
                removeMapOverlays(map, map.mapOverlays.toArray(new MapOverlay[]{}));
            }

            public ArrayList<MapOverlay> findMapOverlaysByName(Map map, String name) {
                if (map == null || name == null) return new ArrayList<>();
                return new ArrayList<>(map.mapOverlays.stream().filter(mapOverlay1 -> name.equals(mapOverlay1.name)).toList());
            }

            public MapOverlay findMapOverlayByName(Map map, String name) {
                if (map == null || name == null) return null;
                ArrayList<MapOverlay> result = findMapOverlaysByName(map, name);
                return result.size() > 0 ? result.get(0) : null;
            }

            public class _MapOverlay {
                public MapOverlay create(CMediaGFX image, int x, int y) {
                    return create(image, x, y, false, Tools.Colors.WHITE, 0);
                }

                public MapOverlay create(CMediaGFX image, int x, int y, boolean fadeOut) {
                    return create(image, x, y, fadeOut, Tools.Colors.WHITE, 0);
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
                    mapOverlay.addedToMap = null;
                    return mapOverlay;
                }

                public void setFadeOut(MapOverlay mapOverlay, boolean fadeOut) {
                    if (mapOverlay == null) return;
                    mapOverlay.fadeOut = fadeOut;
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

                public void setColor(MapOverlay mapOverlay, FColor color) {
                    setColor(mapOverlay, color.r, color.b, color.g, color.a);
                }

                public void setColor(MapOverlay mapOverlay, float r, float g, float b, float a) {
                    if (mapOverlay == null) return;
                    mapOverlay.color_r = r;
                    mapOverlay.color_g = g;
                    mapOverlay.color_b = b;
                    mapOverlay.color_a = a;
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
                return create(x, y, lines, config.defaultFont, defaultTextAction());
            }

            public Text create(int x, int y, String[] lines, CMediaFont font) {
                return create(x, y, lines, font, defaultTextAction());
            }

            public Text create(int x, int y, String[] lines, CMediaFont font, TextAction textAction) {
                Text textC = new Text();
                setComponentInitValues(textC);
                setPosition(textC, x, y);
                setFont(textC, font);
                setTextAction(textC, textAction);
                setLines(textC, lines);
                return textC;
            }

            public void setTextAction(Text text, TextAction textAction) {
                if (text == null) return;
                text.textAction = textAction;
            }

            public void setLines2(Text text, String... lines) {
                setLines(text, lines);
            }

            public void setLines(Text text, String[] lines) {
                if (text == null) return;
                text.lines = Tools.Text.validString(lines);
                updateSize(text);
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

            private void updateSize(Text text) {
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
                return create(x, y, image, 0, 0f, defaultImageAction());
            }

            public Image create(int x, int y, CMediaGFX image, int arrayIndex) {
                return create(x, y, image, arrayIndex, 0f, defaultImageAction());
            }

            public Image create(int x, int y, CMediaGFX image, int arrayIndex, float animation_offset) {
                return create(x, y, image, arrayIndex, animation_offset, defaultImageAction());
            }

            public Image create(int x, int y, CMediaGFX image, int arrayIndex, float animation_offset, ImageAction imageAction) {
                Image imageC = new Image();
                setComponentInitValues(imageC);
                setPosition(imageC, x, y);
                setImage(imageC, image);
                setArrayIndex(imageC, arrayIndex);
                setAnimationOffset(imageC, animation_offset);
                setColor(imageC, Tools.Colors.WHITE);
                setImageAction(imageC, imageAction);
                return imageC;
            }

            public void setAnimationOffset(Image image, float animationOffset) {
                if (image == null) return;
                image.animationOffset = animationOffset;
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
                updateSize(imageC);
            }

            private void updateSize(Image image) {
                if (image == null) return;
                int width = image.image != null ? mediaManager.imageWidth(image.image) / UIEngine.TILE_SIZE : 0;
                int height = image.image != null ? mediaManager.imageHeight(image.image) / UIEngine.TILE_SIZE : 0;
                setSize(image, width, height);
            }

        }

        public class _ComboBox {

            public final _ComboBox._ComboBoxItem item = new _ComboBox._ComboBoxItem();

            private ComboBoxAction defaultComboBoxAction() {
                return new ComboBoxAction() {
                };
            }

            public ComboBox create(int x, int y, int width) {
                return create(x, y, width, null, false, defaultComboBoxAction(), null);
            }

            public ComboBox create(int x, int y, int width, ComboBoxItem[] items) {
                return create(x, y, width, items, false, defaultComboBoxAction(), null);
            }

            public ComboBox create(int x, int y, int width, ComboBoxItem[] items, boolean useIcons) {
                return create(x, y, width, items, useIcons, defaultComboBoxAction(), null);
            }

            public ComboBox create(int x, int y, int width, ComboBoxItem[] items, boolean useIcons, ComboBoxAction comboBoxAction) {
                return create(x, y, width, items, useIcons, comboBoxAction, null);
            }

            public ComboBox create(int x, int y, int width, ComboBoxItem[] items, boolean useIcons, ComboBoxAction comboBoxAction, CMediaFont font) {
                ComboBox comboBox = new ComboBox();
                comboBox.items = new ArrayList<>();
                setComponentInitValues(comboBox);
                setPosition(comboBox, x, y);
                setSize(comboBox, width, 1);
                setUseIcons(comboBox, useIcons);
                setComboBoxAction(comboBox, comboBoxAction);
                addComboBoxItems(comboBox, items);
                setSelectedItem(comboBox, null);
                close(comboBox);
                return comboBox;
            }

            public void setComboBoxAction(ComboBox comboBox, ComboBoxAction comboBoxAction) {
                if (comboBox == null) return;
                comboBox.comboBoxAction = comboBoxAction;
            }

            public void setUseIcons(ComboBox comboBox, boolean useIcons) {
                if (comboBox == null) return;
                comboBox.useIcons = useIcons;
            }

            public void addComboBoxItem(ComboBox comboBox, ComboBoxItem comboBoxItem) {
                if (comboBox == null || comboBoxItem == null) return;
                UICommons.comboBox_addItem(comboBox, comboBoxItem);
            }

            public void addComboBoxItems(ComboBox comboBox, ComboBoxItem[] comboBoxItems) {
                if (comboBox == null || comboBoxItems == null) return;
                for (ComboBoxItem comboBoxItem : comboBoxItems) addComboBoxItem(comboBox, comboBoxItem);
            }

            public void removeComboBoxItem(ComboBox comboBox, ComboBoxItem comboBoxItem) {
                if (comboBox == null || comboBoxItem == null) return;
                UICommons.comboBox_removeItem(comboBox, comboBoxItem);
            }

            public void removeComboBoxItems(ComboBox comboBox, ComboBoxItem[] comboBoxItems) {
                if (comboBox == null || comboBoxItems == null) return;
                for (ComboBoxItem comboBoxItem : comboBoxItems) removeComboBoxItem(comboBox, comboBoxItem);
            }

            public void removeAllComboBoxItems(ComboBox comboBox) {
                if (comboBox == null) return;
                removeComboBoxItems(comboBox, comboBox.items.toArray(new ComboBoxItem[]{}));
            }

            public boolean isItemSelected(ComboBox comboBox, ComboBoxItem comboBoxItem) {
                if (comboBox == null || comboBoxItem == null) return false;
                return comboBox.selectedItem != null ? comboBox.selectedItem == comboBoxItem : false;
            }

            public boolean isSelectedItemText(ComboBox comboBox, String text) {
                if (comboBox == null || text == null) return false;
                return comboBox.selectedItem != null ? comboBox.selectedItem.text.equals(text) : false;
            }

            public void setSelectedItem(ComboBox comboBox, ComboBoxItem selectItem) {
                if (comboBox == null) return;
                if (selectItem != null) {
                    if (comboBox.items != null && comboBox.items.contains(selectItem))
                        comboBox.selectedItem = selectItem;
                } else {
                    comboBox.selectedItem = null;
                }
            }

            public void setSelectedItemByText(ComboBox comboBox, String text) {
                if (comboBox == null || text == null) return;
                for (ComboBoxItem comboBoxItem : comboBox.items) {
                    if (comboBoxItem.text.equals(text)) {
                        setSelectedItem(comboBox, comboBoxItem);
                        return;
                    }
                }
            }

            public void open(ComboBox comboBox) {
                if (comboBox == null) return;
                UICommons.comboBox_open(comboBox, inputState);
            }

            public void close(ComboBox comboBox) {
                if (comboBox == null) return;
                UICommons.comboBox_close(comboBox, inputState);
            }

            public boolean isOpen(ComboBox comboBox) {
                return UICommons.comboBox_isOpen(comboBox, inputState);
            }

            public class _ComboBoxItem {

                private ComboBoxItemAction defaultComboBoxItem() {
                    return new ComboBoxItemAction() {
                    };
                }

                public ComboBoxItem create(String text) {
                    return create(text, defaultComboBoxItem(), null, config.componentsDefaultColor, null);
                }

                public ComboBoxItem create(String text, ComboBoxItemAction contextMenuItemAction) {
                    return create(text, defaultComboBoxItem(), null, config.componentsDefaultColor, null);
                }

                public ComboBoxItem create(String text, ComboBoxItemAction contextMenuItemAction, CMediaGFX icon) {
                    return create(text, defaultComboBoxItem(), icon, config.componentsDefaultColor, null);
                }

                public ComboBoxItem create(String text, ComboBoxItemAction contextMenuItemAction, CMediaGFX icon, FColor color) {
                    return create(text, defaultComboBoxItem(), icon, color, null);
                }

                public ComboBoxItem create(String text, ComboBoxItemAction contextMenuItemAction, CMediaGFX icon, FColor color, CMediaFont font) {
                    ComboBoxItem comboBoxItem = new ComboBoxItem();
                    setText(comboBoxItem, text);
                    setFont(comboBoxItem, font);
                    setColor(comboBoxItem, color);
                    setIcon(comboBoxItem, icon);
                    setIconIndex(comboBoxItem, 0);
                    setName(comboBoxItem, "");
                    setData(comboBoxItem, null);
                    setComboBoxItemAction(comboBoxItem, contextMenuItemAction);
                    comboBoxItem.addedToComboBox = null;
                    return comboBoxItem;
                }

                public void setName(ComboBoxItem comboBoxItem, String name) {
                    if (comboBoxItem == null) return;
                    comboBoxItem.name = Tools.Text.validString(name);

                }

                public void setData(ComboBoxItem comboBoxItem, Object data) {
                    if (comboBoxItem == null) return;
                    comboBoxItem.data = data;
                }

                public void setColor(ComboBoxItem comboBoxItem, FColor color) {
                    if (comboBoxItem == null || color == null) return;
                    comboBoxItem.color_r = color.r;
                    comboBoxItem.color_g = color.g;
                    comboBoxItem.color_b = color.b;
                }

                public void setFont(ComboBoxItem comboBoxItem, CMediaFont font) {
                    if (comboBoxItem == null) return;
                    comboBoxItem.font = font == null ? config.defaultFont : font;
                }

                public void setComboBoxItemAction(ComboBoxItem comboBoxItem, ComboBoxItemAction comboBoxItemAction) {
                    if (comboBoxItem == null) return;
                    comboBoxItem.comboBoxItemAction = comboBoxItemAction;
                }

                public void setText(ComboBoxItem comboBoxItem, String text) {
                    if (comboBoxItem == null) return;
                    comboBoxItem.text = Tools.Text.validString(text);
                }

                public void setIcon(ComboBoxItem comboBoxItem, CMediaGFX icon) {
                    if (comboBoxItem == null) return;
                    comboBoxItem.icon = icon;
                }

                public void setIconIndex(ComboBoxItem comboBoxItem, int index) {
                    if (comboBoxItem == null) return;
                    comboBoxItem.iconIndex = Tools.Calc.lowerBounds(index, 0);
                }

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
                list.selectedItem = null;
                list.selectedItems = new HashSet<>();
                setComponentInitValues(list);
                setPosition(list, x, y);
                setSize(list, width, height);
                setItems(list, items);
                setListAction(list, listAction);
                setMultiSelect(list, multiSelect);
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

            public void setMultiSelect(List list, boolean multiSelect) {
                if (list == null) return;
                UICommons.list_setMultiSelect(list, multiSelect);
            }

            public void setSelectedItem(List list, Object selectedItem) {
                if (list == null) return;
                if (list.multiSelect) return;
                if (list.items != null && list.items.contains(selectedItem)) list.selectedItem = selectedItem;
            }

            public void setSelectedItems(List list, Object[] selectedItems) {
                if (list == null || selectedItems == null) return;
                if (!list.multiSelect) return;
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
