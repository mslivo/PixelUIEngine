package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BooleanArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.LongArray;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.CMediaCursor;
import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.media_manager.media.CMediaGFX;
import org.mslivo.core.engine.media_manager.media.CMediaImage;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.ui_engine.enums.MOUSE_CONTROL_MODE;
import org.mslivo.core.engine.ui_engine.enums.VIEWPORT_MODE;
import org.mslivo.core.engine.ui_engine.input.InputMethod;
import org.mslivo.core.engine.ui_engine.input.KeyCode;
import org.mslivo.core.engine.ui_engine.render.NestedFrameBuffer;
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
import org.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import org.mslivo.core.engine.ui_engine.ui.components.image.Image;
import org.mslivo.core.engine.ui_engine.ui.components.knob.Knob;
import org.mslivo.core.engine.ui_engine.ui.components.list.List;
import org.mslivo.core.engine.ui_engine.ui.components.map.Canvas;
import org.mslivo.core.engine.ui_engine.ui.components.map.CanvasImage;
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
import org.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenu;
import org.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenuItem;
import org.mslivo.core.engine.ui_engine.ui.hotkeys.HotKey;
import org.mslivo.core.engine.ui_engine.ui.notification.Notification;
import org.mslivo.core.engine.ui_engine.ui.notification.STATE_NOTIFICATION;
import org.mslivo.core.engine.ui_engine.ui.ostextinput.MouseTextInput;
import org.mslivo.core.engine.ui_engine.ui.ostextinput.MouseTextInputAction;
import org.mslivo.core.engine.ui_engine.ui.tool.MouseTool;
import org.mslivo.core.engine.ui_engine.ui.tooltip.ToolTip;
import org.mslivo.core.engine.ui_engine.ui.tooltip.ToolTipImage;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

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
        - setColor(X, Color color) -> setColor(X, float r, float g, float b, float a)

    - "create" functions must never trigger ActionListener Events either directly or via UICommons
 */
public class API {
    public final _Notification notification = new _Notification();
    public final _ContextMenu contextMenu = new _ContextMenu();
    public final _Window window = new _Window();

    public final _Component component = new _Component();
    public final _Camera camera = new _Camera();
    public final _ToolTip toolTip = new _ToolTip();
    public final _Config config = new _Config();
    public final _Input input = new _Input();
    public final _MouseTool mouseTool = new _MouseTool();
    public final _HotKey hotkey = new _HotKey();
    public final _MouseTextInput mouseTextInput = new _MouseTextInput();
    public final _PreConfigured preConfigured = new _PreConfigured();

    // Private
    private final InputState inputState;
    private final MediaManager mediaManager;

    public API(InputState inputState, MediaManager mediaManager) {
        this.inputState = inputState;
        this.mediaManager = mediaManager;
    }


    public static class _HotKey {

        public HotKey create(int[] keyCodes, HotKeyAction hotKeyAction) {
            if (keyCodes == null || keyCodes.length == 0) return null;
            if (hotKeyAction == null) return null;
            HotKey hotKey = new HotKey();
            hotKey.pressed = false;
            hotKey.keyCodes = Arrays.copyOf(keyCodes, keyCodes.length);
            hotKey.hotKeyAction = hotKeyAction;
            hotKey.name = "";
            hotKey.data = null;
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
                names[i] = com.badlogic.gdx.Input.Keys.toString(hotKey.keyCodes[i]);
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
            mouseTool.name = name;
            mouseTool.data = data;
            mouseTool.cursor = cursor;
            mouseTool.cursorDown = cursorDown;
            mouseTool.mouseToolAction = mouseToolAction;
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

        public class GraphInfo {
            public final long lowestValue;
            public final long highestValue;
            public final int[] indexAtPosition;
            public final long[] valueAtPosition;

            public GraphInfo(long lowestValue, long highestValue, int[] indexAtPosition, long[] valueAtPosition) {
                this.lowestValue = lowestValue;
                this.highestValue = highestValue;
                this.indexAtPosition = indexAtPosition;
                this.valueAtPosition = valueAtPosition;
            }
        }

        public interface DrawGraphFunctions {
            Color getColorForValues(long value, long lastValue);

            long getValueAtIndex(int index);
        }

        private final char[] numbersAllowedCharacters = new char[]{'-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        private final char[] decimalsAllowedCharacters = new char[]{'-', ',', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        public TextField list_CreateSearchBar(List list) {
            return list_CreateSearchBar(list, null, false, false);
        }

        public TextField list_CreateSearchBar(List list, ScrollBarVertical scrollBarVertical) {
            return list_CreateSearchBar(list, scrollBarVertical, false, false);
        }

        public TextField list_CreateSearchBar(List list, ScrollBarVertical scrollBarVertical, boolean searchTooltips, boolean searchArrayLists) {
            if (list == null) return null;
            ArrayList originalList = list.items;
            ArrayList itemsSearched = new ArrayList(list.items);
            component.setSize(list, list.width, list.height - 1);
            component.setPosition(list, list.x, list.y + 1);
            if (scrollBarVertical != null) {
                component.setSize(scrollBarVertical, scrollBarVertical.width, scrollBarVertical.height - 1);
                component.setPosition(scrollBarVertical, scrollBarVertical.x, scrollBarVertical.y + 1);
            }
            TextField textField = component.textField.create(list.x, list.y - 1, list.width + 1, "");
            component.textField.setTextFieldAction(textField, new TextFieldAction() {


                @Override
                public void onContentChange(String searchText, boolean valid) {
                    if (valid) {

                        if (searchText.trim().isEmpty()) {
                            component.list.setItems(list, originalList);
                        } else {
                            itemsSearched.clear();
                            searchItems(list, originalList, itemsSearched, searchText, searchTooltips, searchArrayLists);
                            component.list.setItems(list, itemsSearched);
                        }


                    }
                }
            });

            return textField;
        }


        private void searchItems(List list, ArrayList searchList, ArrayList resultList, String searchText, boolean searchTooltips, boolean searchArrayLists) {
            for (int i = 0; i < searchList.size(); i++) {
                Object item = searchList.get(i);
                if (searchArrayLists && item instanceof ArrayList itemList) {
                    searchItems(list, itemList, resultList, searchText, searchTooltips, searchArrayLists);
                } else if (list.listAction != null && list.listAction.text(item).trim().toLowerCase().contains(searchText.trim().toLowerCase())) {
                    resultList.add(item);
                } else if (searchTooltips) {
                    ToolTip tooltip = list.listAction.toolTip(item);
                    if (tooltip != null) {
                        linesLoop:
                        for (int i2 = 0; i2 < tooltip.lines.length; i2++) {
                            if (tooltip.lines[i] != null && tooltip.lines[i].trim().toLowerCase().contains(searchText.trim().toLowerCase())) {
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
                Text text1 = component.text.create(x, y + ((column1Text.length - 1) - iy), Tools.Text.toArray(column1Text[iy]));
                ret[0][iy] = text1;
                Text text2 = component.text.create(x + col1Width, y + (column1Text.length - 1 - iy), new String[]{});
                ret[1][iy] = text2;
            }
            return ret;
        }

        public ArrayList<Component> text_createScrollAbleText(int x, int y, int width, int height, String[] text) {
            ArrayList<Component> result = new ArrayList<>();

            Text textField = component.text.create(x, y, null);
            component.setSize(textField, width - 1, height);
            ScrollBarVertical scrollBarVertical = component.scrollBar.verticalScrollbar.create(x + width - 1, y, height);
            String[] textConverted;
            String[] textDisplayedLines = new String[height];

            // Cut Text to Fit
            if (text != null) {
                ArrayList<String> textList = new ArrayList<>();
                int pixelWidth = ((width - 1) * UIEngine.TILE_SIZE);
                for (int i = 0; i < text.length; i++) {
                    String textLine = text[i];
                    textLine = Tools.Text.validString(textLine);
                    if (textLine.trim().length() > 0) {
                        String[] split = textLine.split(" ");
                        if (split.length > 0) {
                            StringBuilder currentLine = new StringBuilder();
                            for (int i2 = 0; i2 < split.length; i2++) {
                                String value = split[i2];
                                if (mediaManager.textWidth(inputState.config.component_defaultFont, currentLine + value + " ") >= pixelWidth) {
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
            component.text.setTextAction(textField, new TextAction() {
                @Override
                public void onMouseScroll(float scrolled) {
                    float scrollAmount = (-1 / (float) Tools.Calc.lowerBounds(textConverted.length, 1)) * input.mouse.event.scrolledAmount();
                    UICommons.scrollBar_scroll(scrollBarVertical, scrollBarVertical.scrolled + scrollAmount);
                }
            });
            component.scrollBar.setScrollBarAction(scrollBarVertical, new ScrollBarAction() {
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
            UICommons.scrollBar_scroll(scrollBarVertical, 1f);
            if (textConverted.length <= height) {
                component.setDisabled(scrollBarVertical, true);
            }

            component.text.setLines(textField, textDisplayedLines);


            result.add(scrollBarVertical);
            result.add(textField);
            return result;
        }

        public Text text_CreateClickableURL(int x, int y, String url) {
            return text_CreateClickableURL(x, y, url, Tools.Text.toArray(url), UIBaseMedia.UI_FONT_BLACK, Tools.Text.toArray(url), UIBaseMedia.UI_FONT_BLACK);
        }

        public Text text_CreateClickableURL(int x, int y, String url, String[] text, CMediaFont font, String[] textHover, CMediaFont fontHover) {
            return text_CreateClickableText(x, y, text, font, button -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, textHover, fontHover);
        }


        public Text text_CreateClickableText(int x, int y, String[] text, CMediaFont font, IntConsumer onClick) {
            return text_CreateClickableText(x, y, text, font, onClick, text, font);
        }

        public Text text_CreateClickableText(int x, int y, String[] text, CMediaFont font, IntConsumer onClick, String[] textHover, CMediaFont fontHover) {
            Text hlText = component.text.create(x, y, text);
            component.text.setTextAction(hlText, new TextAction() {
                @Override
                public void onMouseClick(int button) {
                    onClick.accept(button);
                }
            });
            component.addUpdateAction(hlText, new UpdateAction(0) {
                @Override
                public void onUpdate() {
                    if (Tools.Calc.pointRectsCollide(
                            input.mouse.state.xUI(),
                            input.mouse.state.yUI(),
                            component.getAbsoluteX(hlText),
                            component.getAbsoluteY(hlText),
                            hlText.width * UIEngine.TILE_SIZE,
                            hlText.height * UIEngine.TILE_SIZE
                    )) {
                        component.text.setFont(hlText, fontHover);
                        component.text.setLines(hlText, textHover);
                    } else {
                        component.text.setFont(hlText, font);
                        component.text.setLines(hlText, text);
                    }
                }
            });
            return hlText;
        }


        public HotKeyAction hotkey_CreateForButton(Button button) {
            HotKeyAction hotKeyAction = new HotKeyAction() {
                @Override
                public void onPress() {
                    UICommons.button_press(button);
                }

                @Override
                public void onRelease() {
                    UICommons.button_release(button);
                }
            };
            return hotKeyAction;
        }

        public void checkbox_MakeExclusive(CheckBox[] checkboxes, Consumer<CheckBox> checkedFunction) {
            if (checkboxes == null || checkedFunction == null) return;
            for (int i = 0; i < checkboxes.length; i++) {
                int iF = i;
                component.checkBox.setCheckBoxAction(checkboxes[i], new CheckBoxAction() {
                    @Override
                    public void onCheck(boolean checked) {
                        if (checked) {
                            //noinspection ForLoopReplaceableByForEach
                            for (int i = 0; i < checkboxes.length; i++)
                                if (checkboxes[i] != checkboxes[iF])
                                    component.checkBox.setChecked(checkboxes[i], false);
                            checkedFunction.accept(checkboxes[iF]);
                        } else {
                            component.checkBox.setChecked(checkboxes[iF], true);
                        }
                    }
                });
            }
        }

        public ScrollBarVertical list_CreateScrollBar(List list) {
            ScrollBarVertical scrollBarVertical = component.scrollBar.verticalScrollbar.create(list.x + list.width, list.y, list.height, new ScrollBarAction() {
                @Override
                public void onScrolled(float scrolled) {
                    component.list.setScrolled(list, 1f - scrolled);
                }
            });

            component.setOffset(scrollBarVertical, list.offset_x, list.offset_y);

            component.addUpdateAction(scrollBarVertical, new UpdateAction() {
                float scrolledLast = -1;

                @Override
                public void onUpdate() {
                    if (scrolledLast != list.scrolled) {
                        component.scrollBar.setScrolled(scrollBarVertical, 1 - list.scrolled);
                        scrolledLast = list.scrolled;
                    }
                    // disable scrollbar
                    if (list.items != null && list.items.size() <= list.height) {
                        component.setDisabled(scrollBarVertical, true);
                        component.scrollBar.setScrolled(scrollBarVertical, 1f);
                    } else {
                        component.setDisabled(scrollBarVertical, false);
                    }
                }
            });
            return scrollBarVertical;
        }

        public ArrayList<Component> image_CreateSeparatorHorizontal(int x, int y, int size) {
            ArrayList<Component> returnComponents = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int index = i == 0 ? 0 : i == (size - 1) ? 2 : 1;
                Image image = component.image.create(x + i, y, UIBaseMedia.UI_SEPARATOR_HORIZONTAL, index);
                component.setColor(image, inputState.config.component_defaultColor);
                returnComponents.add(image);
            }
            return returnComponents;
        }

        public ArrayList<Component> image_CreateSeparatorVertical(int x, int y, int size) {
            ArrayList<Component> returnComponents = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int index = i == 0 ? 1 : i == (size - 1) ? 0 : 1;
                Image image = component.image.create(x, y + i, UIBaseMedia.UI_SEPARATOR_VERTICAL, index);
                component.setColor(image, inputState.config.component_defaultColor);
                returnComponents.add(image);
            }
            return returnComponents;
        }

        public Window modal_CreateColorModal(String caption, Consumer<Color> selectColorFunction, Color initColor) {
            return modal_CreateColorModal(caption, selectColorFunction, initColor, UIBaseMedia.UI_COLOR_SELECTOR);
        }

        public Window modal_CreateColorModal(String caption, Consumer<Color> selectColorFunction, Color initColor, CMediaImage colors) {

            TextureRegion colorTexture = mediaManager.getCMediaImage(colors);

            final int colorTextureWidthTiles = colorTexture.getRegionWidth() / 8;
            final int colorTextureHeightTiles = colorTexture.getRegionHeight() / 8;

            Window modal = window.create(0, 0, colorTextureWidthTiles + 1, colorTextureHeightTiles + 4, caption, UIBaseMedia.UI_ICON_COLOR, 0);
            ImageButton closeButton = preConfigured.button_CreateWindowCloseButton(modal);
            component.button.setButtonAction(closeButton, new ButtonAction() {
                @Override
                public void onRelease() {
                    selectColorFunction.accept(null);
                    removeCurrentModalWindow();
                }
            });
            window.addComponent(modal, closeButton);

            TextButton ok = component.button.textButton.create(0, 0, colorTextureWidthTiles, 1, "OK", null);
            component.button.setButtonAction(ok, new ButtonAction() {
                @Override
                public void onRelease() {
                    selectColorFunction.accept(new Color(ok.color_r, ok.color_g, ok.color_b, 1f));
                    removeCurrentModalWindow();
                }
            });
            component.setColor(ok, initColor);


            Canvas colorCanvas = component.canvas.create(0, 2, colorTextureWidthTiles, colorTextureHeightTiles);


            CanvasImage cursorOverlay = component.canvas.canvasImage.create(UIBaseMedia.UI_COLOR_SELECTOR_OVERLAY, UIEngine.TILE_SIZE * 8, UIEngine.TILE_SIZE * 4);
            component.canvas.addCanvasImage(colorCanvas, cursorOverlay);


            if (!colorTexture.getTexture().getTextureData().isPrepared())
                colorTexture.getTexture().getTextureData().prepare();
            Pixmap pixmap = colorTexture.getTexture().getTextureData().consumePixmap();

            Color pixelColor = new Color();
            for (int x = 0; x < colorTexture.getRegionWidth(); x++) {
                for (int y = 0; y < colorTexture.getRegionHeight(); y++) {
                    pixelColor.set(pixmap.getPixel(colorTexture.getRegionX() + x, colorTexture.getRegionY() + y));
                    component.canvas.setPoint(colorCanvas, x, y, pixelColor.r, pixelColor.g, pixelColor.b, 1f);
                    if (initColor != null && pixelColor.r == initColor.r && pixelColor.g == initColor.g && pixelColor.b == initColor.b) {
                        component.canvas.canvasImage.setPosition(cursorOverlay, x - 3, colorTexture.getRegionHeight() - y + 1);
                    }
                }
            }

            final boolean[] drag = {false};
            component.canvas.setCanvasAction(colorCanvas, new CanvasAction() {

                @Override
                public void onPress(int x, int y) {
                    drag[0] = true;
                }

                @Override
                public void onRelease() {
                    drag[0] = false;
                }
            });
            component.addUpdateAction(colorCanvas, new UpdateAction(10, true) {
                int xLast = -1, yLast = -1;
                Color currentColor = new Color();

                @Override
                public void onUpdate() {
                    if (drag[0]) {
                        int x = input.mouse.state.xUI() - component.getAbsoluteX(colorCanvas);
                        int yInv = (input.mouse.state.yUI() - component.getAbsoluteY(colorCanvas));
                        int y = colorTexture.getRegionHeight() - yInv;
                        if (x < 0 || y < 0 || x >= colorTexture.getRegionWidth() || y >= colorTexture.getRegionHeight()) {
                            return;
                        }
                        if (x != xLast || y != yLast) {
                            currentColor = component.canvas.getPoint(colorCanvas, x, y - 1);
                            component.setColor(ok, currentColor);
                            float colorBrightness = (0.299f * currentColor.r) + (0.587f * currentColor.g) + (0.114f * currentColor.b);
                            component.button.textButton.setFont(ok, colorBrightness < 0.5 ? UIBaseMedia.UI_FONT_WHITE : UIBaseMedia.UI_FONT_BLACK);
                            component.canvas.canvasImage.setPosition(cursorOverlay, x - 1, yInv - 1);
                            xLast = x;
                            yLast = y;
                        }
                    }
                }
            });


            Component[] componentl = new Component[]{colorCanvas, ok};
            component.setOffset(ok, UIEngine.TILE_SIZE / 2, UIEngine.TILE_SIZE / 2);
            component.setOffset(colorCanvas, UIEngine.TILE_SIZE / 2, UIEngine.TILE_SIZE / 2);
            window.addComponents(modal, componentl);

            return modal;
        }

        public Window modal_CreateTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction) {
            return modal_CreateTextInputModal(caption, text, originalText, inputResultFunction, 0, Integer.MAX_VALUE, true, false, null, null, 11);
        }

        public Window modal_CreateTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength) {
            return modal_CreateTextInputModal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, true, false, null, null, 11);
        }

        public Window modal_CreateTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength, boolean showOKButton) {
            return modal_CreateTextInputModal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, showOKButton, false, null, null, 11);
        }

        public Window modal_CreateTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength, boolean showOKButton, int windowMinWidth) {
            return modal_CreateTextInputModal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, showOKButton, false, null, null, windowMinWidth);
        }

        public Window modal_CreateTouchTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction) {
            return modal_CreateTextInputModal(caption, text, originalText, inputResultFunction, 0, Integer.MAX_VALUE, true, true, inputState.config.mouseTextInput_defaultLowerCaseCharacters, inputState.config.mouseTextInput_defaultUpperCaseCharacters, 11);
        }

        public Window modal_CreateTouchTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength) {
            return modal_CreateTextInputModal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, true, true, inputState.config.mouseTextInput_defaultLowerCaseCharacters, inputState.config.mouseTextInput_defaultUpperCaseCharacters, 11);
        }

        public Window modal_CreateTouchTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength, char[] lowerCaseCharacters, char[] upperCaseCharacters) {
            return modal_CreateTextInputModal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, true, true, lowerCaseCharacters, upperCaseCharacters, 11);
        }

        public Window modal_CreateTouchTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength, char[] lowerCaseCharacters, char[] upperCaseCharacters, int windowMinWidth) {
            return modal_CreateTextInputModal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, true, true, lowerCaseCharacters, upperCaseCharacters, windowMinWidth);
        }

        private Window modal_CreateTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength, boolean showOKButton, boolean showTouchInputs, char[] lowerCaseCharacters, char[] upperCaseCharacters, int windowMinWidth) {
            int maxCharacters = 0;
            if (showTouchInputs) {
                if (lowerCaseCharacters == null || upperCaseCharacters == null) return null;
                maxCharacters = Math.min(lowerCaseCharacters.length, upperCaseCharacters.length);
            }

            showOKButton = showTouchInputs ? true : showOKButton;
            originalText = Tools.Text.validString(originalText);
            windowMinWidth = Tools.Calc.lowerBounds(windowMinWidth, 11);
            int wnd_width = Tools.Calc.lowerBounds(MathUtils.round(mediaManager.textWidth(inputState.config.component_defaultFont, text) / (float) UIEngine.TILE_SIZE) + 2, windowMinWidth);
            int wnd_height = 6;
            if (showOKButton) wnd_height++;
            if (showTouchInputs) {
                wnd_height += (wnd_width % 2 == 0 ? 3 : 1);
                int ixt = 0;
                for (int i = 0; i < maxCharacters; i++) {
                    ixt += 2;
                    if (ixt > (wnd_width - 2)) {
                        wnd_height += 2;
                        ixt = 0;
                    }
                }

            }

            Window modalWnd = window.create(0, 0, wnd_width, wnd_height, caption, UIBaseMedia.UI_ICON_INFORMATION, 0);
            ArrayList<Component> componentsList = new ArrayList<>();

            Text textC = component.text.create(0, showOKButton ? 3 : 2, Tools.Text.toArray(text));
            componentsList.add(textC);

            TextField inputTextField = component.textField.create(0, showOKButton ? 2 : 1, wnd_width - 1, originalText, null, maxInputLength);
            componentsList.add(inputTextField);

            Button okBtn = null;
            if (showOKButton) {
                okBtn = component.button.textButton.create(0, 0, wnd_width - 1, 1, "OK", new ButtonAction() {
                    @Override
                    public void onRelease() {
                        if (inputTextField.content.length() >= minInputLength) {
                            if (inputResultFunction != null) inputResultFunction.accept(inputTextField.content);
                            removeCurrentModalWindow();
                        }
                    }
                });
                componentsList.add(okBtn);
            }


            ArrayList<Button> lowerCaseButtonsList = new ArrayList<>();
            ArrayList<Button> upperCaseButtonsList = new ArrayList<>();
            if (showTouchInputs) {
                int ix = 0;
                int iy = wnd_height - 4;
                for (int i = 0; i < maxCharacters; i++) {
                    char cl = lowerCaseCharacters[i];
                    char cu = upperCaseCharacters[i];
                    if (!Character.isISOControl(cl) && !Character.isISOControl(cu)) {
                        TextButton charButtonLC = component.button.textButton.create(ix, iy, 2, 2, String.valueOf(cl), new ButtonAction() {
                            @Override
                            public void onRelease() {
                                component.textField.setContent(inputTextField, inputTextField.content + cl);
                                component.textField.setMarkerPosition(inputTextField, inputTextField.content.length());
                            }
                        });
                        componentsList.add(charButtonLC);
                        lowerCaseButtonsList.add(charButtonLC);
                        TextButton charButtonUC = component.button.textButton.create(ix, iy, 2, 2, String.valueOf(cu), new ButtonAction() {
                            @Override
                            public void onRelease() {
                                component.textField.setContent(inputTextField, inputTextField.content + cu);
                                component.textField.setMarkerPosition(inputTextField, inputTextField.content.length());
                            }
                        });
                        componentsList.add(charButtonUC);
                        component.setVisible(charButtonUC, false);
                        upperCaseButtonsList.add(charButtonUC);
                    }
                    ix += 2;
                    if (ix >= (wnd_width - 2)) {
                        ix = 0;
                        iy -= 2;
                    }
                }

                // Add Case Button
                ImageButton caseButton = component.button.imageButton.create(ix, iy, 2, 2, UIBaseMedia.UI_ICON_KEY_CASE, 0,
                        new ButtonAction() {
                            @Override
                            public void onToggle(boolean value) {
                                for (int i2 = 0; i2 < lowerCaseButtonsList.size(); i2++)
                                    component.setVisible(lowerCaseButtonsList.get(i2), !value);
                                for (int i2 = 0; i2 < upperCaseButtonsList.size(); i2++)
                                    component.setVisible(upperCaseButtonsList.get(i2), value);
                            }
                        }, ButtonMode.TOGGLE);
                componentsList.add(caseButton);
                ix += 2;
                if (ix >= (wnd_width - 2)) {
                    ix = 0;
                    iy -= 2;
                }
                // Add Delete Button
                ImageButton delButton = component.button.imageButton.create(ix, iy, 2, 2, UIBaseMedia.UI_ICON_KEY_DELETE, 0,
                        new ButtonAction() {
                            @Override
                            public void onRelease() {
                                if (inputTextField.content.length() > 0) {
                                    component.textField.setContent(inputTextField, inputTextField.content.substring(0, inputTextField.content.length() - 1));
                                    component.textField.setMarkerPosition(inputTextField, inputTextField.content.length());
                                }
                            }
                        }, ButtonMode.DEFAULT);
                componentsList.add(delButton);


            }


            Button finalOkBtn = okBtn;
            component.textField.setTextFieldAction(inputTextField, new TextFieldAction() {
                @Override
                public void onEnter(String content, boolean valid) {
                    if (valid) {
                        if (inputResultFunction != null) inputResultFunction.accept(inputTextField.content);
                        removeCurrentModalWindow();
                    } else {
                        component.textField.focus(inputTextField);
                    }
                }

                @Override
                public void onContentChange(String newContent, boolean valid) {
                    if (finalOkBtn != null) component.setDisabled(finalOkBtn, !valid);
                }

                @Override
                public boolean isContentValid(String newContent) {
                    return newContent.length() >= minInputLength;
                }

                @Override
                public void onUnFocus() {
                    component.textField.focus(inputTextField);
                }
            });


            Component[] componentArr = componentsList.toArray(new Component[]{});
            component.setOffset(componentArr, UIEngine.TILE_SIZE / 2, UIEngine.TILE_SIZE / 2);
            component.setOffset(inputTextField, UIEngine.TILE_SIZE / 2, 0);
            window.addComponents(modalWnd, componentArr);
            window.setWindowAction(modalWnd, new WindowAction() {
                @Override
                public void onAdd() {
                    component.textField.focus(inputTextField);
                }
            });

            UICommons.textField_setContent(inputTextField, originalText);
            return modalWnd;
        }

        public Window modal_CreateMessageModal(String caption, String[] lines, Runnable closeFunction) {
            int longest = 0;
            for (int i = 0; i < lines.length; i++) {
                int len = mediaManager.textWidth(inputState.config.component_defaultFont, lines[i]);
                if (len > longest) longest = len;
            }
            ArrayList<Component> componentsList = new ArrayList<>();
            final int WIDTH = Tools.Calc.lowerBounds(MathUtils.round(longest / (float) UIEngine.TILE_SIZE) + 2, 12);
            final int HEIGHT = 4 + lines.length;
            Window modal = window.create(0, 0, WIDTH, HEIGHT, caption, UIBaseMedia.UI_ICON_INFORMATION, 0);

            Text[] texts = new Text[lines.length];
            for (int i = 0; i < lines.length; i++) {
                texts[i] = component.text.create(0, HEIGHT - 3 - i, Tools.Text.toArray(lines[i]));
                componentsList.add(texts[i]);
            }

            Button okBtn = component.button.textButton.create(0, 0, WIDTH - 1, 1, "OK", new ButtonAction() {
                @Override
                public void onRelease() {
                    if (closeFunction != null) {
                        closeFunction.run();
                    }
                    removeCurrentModalWindow();
                }
            });
            component.button.centerContent(okBtn);
            componentsList.add(okBtn);


            Component[] componentsArr = componentsList.toArray(new Component[]{});
            component.setOffset(componentsArr, UIEngine.TILE_SIZE / 2, UIEngine.TILE_SIZE / 2);
            window.addComponents(modal, componentsArr);
            return modal;
        }


        public GraphInfo map_drawGraph(Canvas canvas, int itemCount, int steps, int stepSize, Color colorBackGround, DrawGraphFunctions drawGraphFunctions, int[] hiAndLowValueReference, boolean drawBackGroundLines) {
            int mapWidth = canvas.width * UIEngine.TILE_SIZE;
            int mapHeight = canvas.height * UIEngine.TILE_SIZE;
            int[] indexAtPosition = new int[mapWidth];
            long[] valueAtPosition = new long[mapWidth];
            boolean[] dataAvailableAtPosition = new boolean[mapWidth];
            long lowestValue = Integer.MAX_VALUE;
            long highestValue = Integer.MIN_VALUE;
            // Get Values
            IntArray indexes = new IntArray();
            LongArray values = new LongArray();
            BooleanArray dataAvailables = new BooleanArray();
            int startIndex = (itemCount - 1) - (steps * stepSize);
            int indexAndValueCount = 0;
            long valueBefore = (startIndex - stepSize) > 0 ? drawGraphFunctions.getValueAtIndex((startIndex - stepSize)) : Long.MIN_VALUE;
            boolean oneValueFound = false;
            for (int i = startIndex; i < itemCount; i += stepSize) {
                if (i >= 0) {
                    long value = drawGraphFunctions.getValueAtIndex(i);
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
            Color colorBackGroundDarker = colorBackGround.sub(0.02f, 0.02f, 0.02f, 0f).cpy();
            for (int iy = 0; iy < mapHeight; iy++) {
                Color color = drawBackGroundLines ? (iy % 4 == 0 ? colorBackGroundDarker : colorBackGround) : colorBackGround;
                for (int ix = 0; ix < mapWidth; ix++) {
                    component.canvas.setPoint(canvas, ix, iy, color.r, color.g, color.b, color.a);
                }
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
            Color color = drawGraphFunctions.getColorForValues(lastValue, valueBefore);
            Color colorBrighter = color.add(SHADING, SHADING, SHADING, 0f).cpy();
            Color colorDarker = color.sub(SHADING, SHADING, SHADING, 0f).cpy();
            drawLoop:
            for (int ix = 0; ix < mapWidth; ix++) {
                int index = indexAtPosition[ix];
                long value = valueAtPosition[ix];
                boolean dataAvailable = dataAvailableAtPosition[ix];

                if (!dataAvailable) continue drawLoop;

                boolean indexChange = false;
                boolean nextIndexChange = (ix + 1) < mapWidth && indexAtPosition[ix + 1] != index;
                if (index != lastIndex) {
                    color = drawGraphFunctions.getColorForValues(value, lastValue);
                    colorBrighter = color.add(SHADING, SHADING, SHADING, 0f).cpy();
                    colorDarker = color.sub(SHADING, SHADING, SHADING, 0f).cpy();
                    indexChange = true;
                    lastIndex = index;
                }


                float heightPct = (value - loReference) / (float) (hiReference - loReference);
                int heightPixels = Tools.Calc.lowerBounds(MathUtils.round(mapHeight * heightPct), 2);
                for (int iy = 0; iy < heightPixels; iy++) {
                    int y = mapHeight - iy;
                    if (iy == heightPixels - 1) {
                        component.canvas.setPoint(canvas, ix, y, colorBrighter.r, colorBrighter.g, colorBrighter.b, colorBrighter.a);
                    } else {
                        component.canvas.setPoint(canvas, ix, y, color.r, color.g, color.b, color.a);
                    }
                }

                // Draw Shading
                if (indexChange && ix != 0) {
                    for (int iy = 0; iy < heightPixels; iy++) {
                        int y = mapHeight - iy;
                        component.canvas.setPoint(canvas, ix, y, colorBrighter.r, colorBrighter.g, colorBrighter.b, colorBrighter.a);
                    }
                } else if (nextIndexChange) {
                    for (int iy = 0; iy < heightPixels; iy++) {
                        int y = mapHeight - iy;
                        component.canvas.setPoint(canvas, ix, y, colorDarker.r, colorDarker.g, colorDarker.b, colorDarker.a);
                    }
                }


                lastValue = value;
            }

            return new GraphInfo(lowestValue, highestValue, indexAtPosition, valueAtPosition);
        }

        public Window modal_CreateYesNoRequester(String caption, String text, Consumer<Boolean> choiceFunction) {
            return modal_CreateYesNoRequester(caption, text, choiceFunction, "Yes", "No");
        }

        public Window modal_CreateYesNoRequester(String caption, String text, Consumer<Boolean> choiceFunction, String yes, String no) {

            int textWidthMin = Math.max(
                    (mediaManager.textWidth(inputState.config.component_defaultFont, caption) + 8),
                    mediaManager.textWidth(inputState.config.component_defaultFont, text)
            );

            int width = Tools.Calc.lowerBounds(MathUtils.round(textWidthMin / (float) UIEngine.TILE_SIZE) + 2, 12);
            if (width % 2 == 0) width++;
            Window modal = window.create(0, 0, width, 5, caption, UIBaseMedia.UI_ICON_QUESTION, 0);

            int width1 = MathUtils.round(width / 2f) - 1;
            int width2 = width - width1 - 1;

            Text textC = component.text.create(0, 2, new String[]{text});
            int xOffset = 0;
            Button yesC = component.button.textButton.create(xOffset, 0, width1, 1, yes, new ButtonAction() {
                @Override
                public void onRelease() {
                    if (choiceFunction != null) choiceFunction.accept(true);
                    removeCurrentModalWindow();
                }
            });
            component.button.centerContent(yesC);
            xOffset += width1;
            Button noC = component.button.textButton.create(xOffset, 0, width2, 1, no, new ButtonAction() {
                @Override
                public void onRelease() {
                    if (choiceFunction != null) choiceFunction.accept(false);
                    removeCurrentModalWindow();
                }
            });
            component.button.centerContent(noC);

            Component[] componentsl = new Component[]{textC, yesC, noC};
            component.setOffset(componentsl, UIEngine.TILE_SIZE / 2, UIEngine.TILE_SIZE / 2);
            window.addComponents(modal, componentsl);
            return modal;
        }

        public ImageButton button_CreateWindowCloseButton(Window window) {
            return button_CreateWindowCloseButton(window, null);
        }

        public ImageButton button_CreateWindowCloseButton(Window window, Consumer<Window> closeFunction) {
            ImageButton closeButton = component.button.imageButton.create(window.width - 1, window.height - 1, 1, 1, UIBaseMedia.UI_ICON_CLOSE);
            component.setName(closeButton, UIEngine.WND_CLOSE_BUTTON);
            component.button.setButtonAction(closeButton, new ButtonAction() {

                @Override
                public void onRelease() {
                    removeWindow(window);
                    if (closeFunction != null) closeFunction.accept(window);
                }
            });
            return closeButton;
        }

        public TextField textField_createDecimalInputField(int x, int y, int width, float min, float max, DoubleConsumer onChange) {
            TextField textField = component.textField.create(x, y, width);
            component.textField.setAllowedCharacters(textField, decimalsAllowedCharacters);
            component.textField.setTextFieldAction(textField, new TextFieldAction() {
                @Override
                public boolean isContentValid(String newContent) {
                    float value;
                    try {
                        value = Float.parseFloat(newContent);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    return (value >= min && value <= max);
                }

                @Override
                public void onEnter(String content, boolean valid) {
                    float value;
                    try {
                        value = Float.parseFloat(content);
                        onChange.accept(value);
                    } catch (NumberFormatException e) {
                        component.textField.focus(textField);
                    }
                }
            });
            return textField;
        }

        public TextField textField_createIntegerInputField(int x, int y, int width, int min, int max, IntConsumer onChange) {
            TextField textField = component.textField.create(x, y, width);
            component.textField.setAllowedCharacters(textField, numbersAllowedCharacters);
            component.textField.setTextFieldAction(textField, new TextFieldAction() {
                @Override
                public boolean isContentValid(String newContent) {
                    int value;
                    try {
                        value = Integer.parseInt(newContent);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    return (value >= min && value <= max);
                }

                @Override
                public void onEnter(String content, boolean valid) {
                    int value;
                    try {
                        value = Integer.parseInt(content);
                        onChange.accept(value);
                    } catch (NumberFormatException e) {
                        component.textField.focus(textField);
                    }
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

                borders.add(component.image.create(x + ix, y, UIBaseMedia.UI_BORDERS, 2));

                if (ix >= gap) {
                    borders.add(component.image.create(x + ix, y + (height - 1), UIBaseMedia.UI_BORDERS, 3));
                }
            }

            for (int iy = 0; iy < height; iy++) {
                borders.add(component.image.create(x, y + iy, UIBaseMedia.UI_BORDERS, 0));
                borders.add(component.image.create(x + (width - 1), y + iy, UIBaseMedia.UI_BORDERS, 1));
            }

            return borders;
        }

        public ArrayList<Component> tabBar_createExtendableTabBar(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight, boolean bigIconMode) {
            ArrayList<Component> ret = new ArrayList<>();

            width = Tools.Calc.lowerBounds(width, 1);
            TabBar tabBar = component.tabBar.create(x, y, width, tabs, selectedTab, tabBarAction, border, borderHeight, 2, bigIconMode);
            ImageButton extendButton = component.button.imageButton.create(x, y, 2, bigIconMode ? 2 : 1, UIBaseMedia.UI_ICON_EXTEND);

            updateExtendableTabBarButton(tabBar, extendButton);

            ret.add(extendButton);
            ret.add(tabBar);

            return ret;
        }

        private void updateExtendableTabBarButton(TabBar tabBar, ImageButton extendButton) {
            ArrayList<Tab> invisibleTabs = new ArrayList<>();
            for (int i = 0; i < tabBar.tabs.size(); i++)
                if (!component.tabBar.isTabVisible(tabBar, tabBar.tabs.get(i))) invisibleTabs.add(tabBar.tabs.get(i));
            if (invisibleTabs.size() > 0) {
                component.button.setButtonAction(extendButton, new ButtonAction() {
                    @Override
                    public void onRelease() {
                        ArrayList<ContextMenuItem> contextMenuItems = new ArrayList<>();
                        for (int i2 = 0; i2 < invisibleTabs.size(); i2++) {
                            Tab invisibleTab = invisibleTabs.get(i2);
                            contextMenuItems.add(contextMenu.item.create(invisibleTab.title, new ContextMenuItemAction() {
                                @Override
                                public void onSelect() {
                                    component.tabBar.removeTab(tabBar, invisibleTab);
                                    component.tabBar.addTab(tabBar, invisibleTab, 0);
                                    component.tabBar.selectTab(tabBar, 0);
                                    updateExtendableTabBarButton(tabBar, extendButton);
                                }
                            }, invisibleTab.icon, 0));
                        }
                        ContextMenu selectTabMenu = contextMenu.create(contextMenuItems.toArray(new ContextMenuItem[0]));
                        openContextMenu(selectTabMenu);
                    }
                });
                component.setDisabled(extendButton, false);

            } else {
                component.button.setButtonAction(extendButton, null);
                component.setDisabled(extendButton, true);
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
        UICommons.notification_addToScreen(inputState, notification, inputState.config.notification_max);
    }

    public void addNotifications(Notification[] notifications) {
        if (notifications == null) return;
        for (int i = 0; i < notifications.length; i++) addNotification(notifications[i]);
    }

    public void removeNotification(Notification notification) {
        if (notification == null) return;
        UICommons.notification_removeFromScreen(inputState, notification);
    }

    public void removeNotifications(Notification[] notifications) {
        if (notifications == null) return;
        for (int i = 0; i < notifications.length; i++) removeNotification(notifications[i]);
    }

    public void removeAllNotifications() {
        removeNotifications(inputState.notifications.toArray(new Notification[]{}));
    }

    public ArrayList<Notification> findNotificationsByName(String name) {
        if (name == null) return new ArrayList<>();
        ArrayList<Notification> result = new ArrayList<>();
        for (int i = 0; i < inputState.notifications.size(); i++)
            if (name.equals(inputState.notifications.get(i).name)) result.add(inputState.notifications.get(i));
        return result;
    }

    public Notification findNotificationByName(String name) {
        if (name == null) return null;
        ArrayList<Notification> result = findNotificationsByName(name);
        return result.getFirst();
    }

    public boolean isNotificationAddedToScreen(Notification notification) {
        if (notification == null) return false;
        return notification.addedToScreen;
    }


    public ArrayList<Notification> getNotification() {
        return new ArrayList<>(inputState.notifications);
    }

    public void openContextMenu(ContextMenu contextMenu) {
        UICommons.contextMenu_openAtMousePosition(inputState,mediaManager,contextMenu);
    }

    public void openContextMenu(ContextMenu contextMenu, int x, int y) {
        if (contextMenu == null) return;
        UICommons.contextMenu_open(inputState , mediaManager, contextMenu, x, y);
    }

    public void closeContextMenu(ContextMenu contextMenu) {
        UICommons.contextMenu_close(inputState, contextMenu);
    }

    public boolean isContextMenuOpen(ContextMenu contextMenu) {
        return UICommons.contextMenu_isOpen(inputState, contextMenu);
    }

    public ArrayList<Window> getWindow() {
        return new ArrayList<>(inputState.windows);
    }

    public void sendMessageToWindows(String message_type, Object... content) {
        if (message_type == null) return;
        for (int i = 0; i < inputState.windows.size(); i++) {
            Window window = inputState.windows.get(i);
            UICommons.window_receiveMessage(window, message_type, content);
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

    public Window getModalWindow() {
        return inputState.modalWindow;
    }

    public void addWindow(Window window) {
        if (window == null) return;
        UICommons.window_addToScreen(inputState, window);
    }

    public void addWindows(Window[] windows) {
        if (windows == null) return;
        for (int i = 0; i < windows.length; i++) addWindow(windows[i]);
    }

    public void removeWindow(Window window) {
        if (window == null) return;
        UICommons.window_removeFromScreen(inputState, window);
    }

    public void removeWindows(Window[] windows) {
        if (windows == null) return;
        for (int i = 0; i < windows.length; i++) removeWindow(windows[i]);
    }

    public void removeAllWindows() {
        removeWindows(inputState.windows.toArray(new Window[]{}));
    }

    public boolean closeWindow(Window window) {
        if (window == null) return false;
        ArrayList<Component> result = this.window.findComponentsByName(window, UIEngine.WND_CLOSE_BUTTON);
        if (result.size() == 1) {
            if (result.getFirst() instanceof Button closeButton) {
                if (closeButton.buttonAction != null) {
                    UICommons.button_press(closeButton);
                    UICommons.button_release(closeButton);
                    return true;
                }
            }
        }
        return false;
    }

    public void closeWindows(Window[] windows) {
        if (windows == null) return;
        for (int i = 0; i < windows.length; i++) closeWindow(windows[i]);
    }

    public void closeAllWindows() {
        closeWindows(inputState.windows.toArray(new Window[]{}));
    }

    public void addWindowAsModal(Window modalWindow) {
        if (modalWindow == null) return;
        if (inputState.modalWindow == null) {
            window.setAlwaysOnTop(modalWindow, true);
            window.setVisible(modalWindow, true);
            window.setFolded(modalWindow, false);
            window.center(modalWindow);
            window.setEnforceScreenBounds(modalWindow, true);
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
        for (int i = 0; i < components.length; i++) addScreenComponent(components[i]);
    }

    public void removeScreenComponent(Component component) {
        if (component == null) return;
        UICommons.component_removeFromScreen(component, inputState);
    }

    public void removeScreenComponents(Component[] components) {
        if (components == null) return;
        for (int i = 0; i < components.length; i++) removeScreenComponent(components[i]);
    }

    public void removeAllScreenComponents() {
        removeScreenComponents(inputState.screenComponents.toArray(new Component[]{}));
    }

    public ArrayList<Component> findScreenComponentsByName(String name) {
        if (name == null) return new ArrayList<>();
        ArrayList<Component> result = new ArrayList<>();
        for (int i = 0; i < inputState.screenComponents.size(); i++)
            if (name.equals(inputState.screenComponents.get(i).name)) result.add(inputState.screenComponents.get(i));
        return result;
    }

    public Component findScreenComponentByName(String name) {
        if (name == null) return null;
        ArrayList<Component> result = findScreenComponentsByName(name);
        return result.size() > 0 ? result.getFirst() : null;
    }

    public void removeEverything() {
        removeAllWindows();
        removeAllScreenComponents();
        removeAllNotifications();
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
        for (int i = 0; i < hotKeys.length; i++) addHotKey(hotKeys[i]);
    }

    public void removeHotKey(HotKey hotKey) {
        if (hotKey == null) return;
        inputState.hotKeys.remove(hotKey);
    }

    public void removeHotKeys(HotKey[] hotKeys) {
        if (hotKeys == null) return;
        for (int i = 0; i < hotKeys.length; i++) removeHotKey(hotKeys[i]);
    }

    public void removeAllHotKeys() {
        removeHotKeys(inputState.hotKeys.toArray(new HotKey[]{}));
    }

    public ArrayList<HotKey> findHotKeysByName(String name) {
        if (name == null) return new ArrayList<>();
        ArrayList<HotKey> result = new ArrayList<>();
        for (int i = 0; i < inputState.hotKeys.size(); i++)
            if (name.equals(inputState.hotKeys.get(i).name)) result.add(inputState.hotKeys.get(i));
        return result;
    }

    public HotKey findHotKeyByName(String name) {
        if (name == null) return null;
        ArrayList<HotKey> result = findHotKeysByName(name);
        return result.size() > 0 ? result.getFirst() : null;
    }


    public ArrayList<Window> findWindowsByName(String name) {
        if (name == null) return new ArrayList<>();
        ArrayList<Window> result = new ArrayList<>();
        for (int i = 0; i < inputState.windows.size(); i++)
            if (name.equals(inputState.windows.get(i).name)) result.add(inputState.windows.get(i));
        return result;
    }

    public Window findWindowByName(String name) {
        if (name == null) return null;
        ArrayList<Window> result = findWindowsByName(name);
        return result.size() > 0 ? result.getFirst() : null;
    }


    public class _MouseTextInput {
        private MouseTextInputAction defaultMouseTextInputConfirmAction() {
            return new MouseTextInputAction() {
            };
        }

        public void open(int x, int y) {
            open(x, y, defaultMouseTextInputConfirmAction(),
                    null,
                    inputState.config.mouseTextInput_defaultLowerCaseCharacters,
                    inputState.config.mouseTextInput_defaultUpperCaseCharacters);
        }

        public void open(int x, int y, MouseTextInputAction onConfirm) {
            open(x, y, onConfirm,
                    null,
                    inputState.config.mouseTextInput_defaultLowerCaseCharacters,
                    inputState.config.mouseTextInput_defaultUpperCaseCharacters);
        }



        public void open(int x, int y, MouseTextInputAction onConfirm, Character selectedCharacter) {
            open(x, y, onConfirm,
                    selectedCharacter,
                    inputState.config.mouseTextInput_defaultLowerCaseCharacters,
                    inputState.config.mouseTextInput_defaultUpperCaseCharacters);
        }

        public void open(int x, int y, MouseTextInputAction mouseTextInputAction, Character selectedCharacter, char[] charactersLC, char[] charactersUC) {
            if (charactersLC == null || charactersUC == null) return;
            if (inputState.openMouseTextInput != null) return;
            MouseTextInput mouseTextInput = new org.mslivo.core.engine.ui_engine.ui.ostextinput.MouseTextInput();
            mouseTextInput.color_r = inputState.config.mouseTextInput_defaultColor.r;
            mouseTextInput.color_g = inputState.config.mouseTextInput_defaultColor.g;
            mouseTextInput.color_b = inputState.config.mouseTextInput_defaultColor.b;
            mouseTextInput.color_a = inputState.config.mouseTextInput_defaultColor.a;
            mouseTextInput.color2_r = 0f;
            mouseTextInput.color2_g = 0f;
            mouseTextInput.color2_b = 0f;
            mouseTextInput.font = inputState.config.mouseTextInput_defaultFont;
            mouseTextInput.x = x - 6;
            mouseTextInput.y = y - 12;
            mouseTextInput.mouseTextInputAction = mouseTextInputAction;
            mouseTextInput.upperCase = false;
            mouseTextInput.selectedIndex = 0;
            int maxCharacters = Math.min(charactersLC.length, charactersUC.length);
            mouseTextInput.charactersLC = new char[maxCharacters + 3];
            mouseTextInput.charactersUC = new char[maxCharacters + 3];
            for (int i = 0; i < maxCharacters; i++) {
                mouseTextInput.charactersLC[i] = charactersLC[i];
                mouseTextInput.charactersUC[i] = charactersUC[i];
                if(selectedCharacter != null && (mouseTextInput.charactersLC[i] == selectedCharacter || mouseTextInput.charactersUC[i] == selectedCharacter)){
                    mouseTextInput.selectedIndex = i;
                    mouseTextInput.upperCase = mouseTextInput.charactersUC[i] == selectedCharacter;
                }
            }
            mouseTextInput.charactersLC[maxCharacters] = mouseTextInput.charactersUC[maxCharacters] = '\t';
            mouseTextInput.charactersLC[maxCharacters + 1] = mouseTextInput.charactersUC[maxCharacters + 1] = '\b';
            mouseTextInput.charactersLC[maxCharacters + 2] = mouseTextInput.charactersUC[maxCharacters + 2] =  '\n';
            inputState.mTextInputMouseX = Gdx.input.getX();
            inputState.mTextInputUnlock = false;
            inputState.openMouseTextInput = mouseTextInput;
        }

        public void close() {
            inputState.openMouseTextInput = null;
        }

        public boolean isUpperCase() {
            if (inputState.openMouseTextInput == null) return false;
            return inputState.openMouseTextInput.upperCase;
        }

        public void enterChangeCase() {
            enterChangeCase(!inputState.openMouseTextInput.upperCase);
        }

        public void enterChangeCase(boolean upperCase) {
            if (inputState.openMouseTextInput == null) return;
            if (inputState.openMouseTextInput.upperCase != upperCase) {
                enterCharacter('\t');
            }
        }

        public void enterDelete() {
            if (inputState.openMouseTextInput == null) return;
            enterCharacter('\b');
        }

        public void enterConfirm() {
            if (inputState.openMouseTextInput == null) return;
            enterCharacter('\n');
        }

        public void enterCharacters(String text) {
            if (inputState.openMouseTextInput == null) return;
            char[] characters = text.toCharArray();
            for (int i = 0; i < characters.length; i++) enterCharacter(characters[i]);
        }

        public void enterCharacter(char character) {
            if (inputState.openMouseTextInput == null) return;
            inputState.mTextInputAPICharacterQueue.add(character);
        }

        public void selectCharacter(char character) {
            if (inputState.openMouseTextInput == null) return;
            UICommons.mouseTextInput_selectCharacter(inputState.openMouseTextInput, character);
        }

        public void selectIndex(int index) {
            if (inputState.openMouseTextInput == null) return;
            UICommons.mouseTextInput_selectIndex(inputState.openMouseTextInput, index);
        }

        public void setCharacters(char[] charactersLC, char[] charactersUC) {
            if (inputState.openMouseTextInput == null) return;
            if (charactersLC == null || charactersUC == null) return;
            UICommons.mouseTextInput_setCharacters(inputState.openMouseTextInput, charactersLC, charactersUC);
        }

        public void setAlpha(float a) {
            if (inputState.openMouseTextInput == null) return;
            inputState.openMouseTextInput.color_a = a;
        }

        public void setColor(Color color) {
            if (inputState.openMouseTextInput == null) return;
            setColor(color.r,color.g,color.b,color.a);
        }

        public void setColor(float r, float g, float b, float a) {
            if (inputState.openMouseTextInput == null) return;
            inputState.openMouseTextInput.color_r = r;
            inputState.openMouseTextInput.color_g = g;
            inputState.openMouseTextInput.color_b = b;
            inputState.openMouseTextInput.color_a = a;
        }

        public void setColor2(Color color2) {
            if (inputState.openMouseTextInput == null) return;
            setColor2(color2.r,color2.g,color2.b);
        }

        public void setColor2(float r, float g, float b) {
            if (inputState.openMouseTextInput == null) return;
            inputState.openMouseTextInput.color2_r = r;
            inputState.openMouseTextInput.color2_g = g;
            inputState.openMouseTextInput.color2_b = b;
        }

        public void setPosition(int x, int y) {
            if (inputState.openMouseTextInput == null) return;
            inputState.openMouseTextInput.x = x - 6;
            inputState.openMouseTextInput.y = y - 12;
        }

        public void setMouseTextInputAction(MouseTextInputAction mouseTextInputAction) {
            if (inputState.openMouseTextInput == null) return;
            inputState.openMouseTextInput.mouseTextInputAction = mouseTextInputAction;
        }

        public void setFont(CMediaFont font) {
            if (inputState.openMouseTextInput == null) return;
            inputState.openMouseTextInput.font = font;
        }


    }


    public class _Config {
        public final _UIConfig ui = new _UIConfig();
        public final _InputConfig input = new _InputConfig();
        public final _WindowConfig window = new _WindowConfig();
        public final _ComponentConfig component = new _ComponentConfig();
        public final _NotificationsConfig notification = new _NotificationsConfig();
        public final _ToolTipConfig tooltip = new _ToolTipConfig();
        public final _MouseTextInputConfig mouseTextInput = new _MouseTextInputConfig();

        public class _UIConfig {
            public CMediaCursor getCursor() {
                return inputState.config.ui_cursor;
            }

            public void setCursor(CMediaCursor ui_cursor) {
                inputState.config.ui_cursor = ui_cursor;
            }

            public boolean isKeyInteractionsDisabled() {
                return inputState.config.ui_keyInteractionsDisabled;
            }

            public void setKeyInteractionsDisabled(boolean ui_keyInteractionsDisabled) {
                inputState.config.ui_keyInteractionsDisabled = ui_keyInteractionsDisabled;
            }

            public boolean isMouseInteractionsDisabled() {
                return inputState.config.ui_mouseInteractionsDisabled;
            }

            public void setMouseInteractionsDisabled(boolean ui_mouseInteractionsDisabled) {
                inputState.config.ui_mouseInteractionsDisabled = ui_mouseInteractionsDisabled;
            }

            public boolean isFoldWindowsOnDoubleClick() {
                return inputState.config.ui_foldWindowsOnDoubleClick;
            }

            public void setFoldWindowsOnDoubleClick(boolean ui_foldWindowsOnDoubleClick) {
                inputState.config.ui_foldWindowsOnDoubleClick = ui_foldWindowsOnDoubleClick;
            }

        }

        public class _InputConfig {
            public float getEmulatedMouseCursorSpeed() {
                return inputState.config.input_emulatedMouseCursorSpeed;
            }

            public void setEmulatedMouseCursorSpeed(float input_emulatedMouseCursorSpeed) {
                inputState.config.input_emulatedMouseCursorSpeed = input_emulatedMouseCursorSpeed;
            }

            public boolean isHardwareMouseEnabled() {
                return inputState.config.input_hardwareMouseEnabled;
            }

            public void setHardwareMouseEnabled(boolean input_hardwareMouseEnabled) {
                inputState.config.input_hardwareMouseEnabled = input_hardwareMouseEnabled;
            }

            public boolean isInput_keyboardMouseEnabled() {
                return inputState.config.input_keyboardMouseEnabled;
            }

            public void setKeyboardMouseEnabled(boolean input_keyboardMouseEnabled) {
                inputState.config.input_keyboardMouseEnabled = input_keyboardMouseEnabled;
            }

            public int[] getKeyboardMouseButtonsUp() {
                return inputState.config.input_keyboardMouseButtonsUp;
            }

            public void setKeyboardMouseButtonsUp(int[] input_keyboardMouseButtonsUp) {
                inputState.config.input_keyboardMouseButtonsUp = input_keyboardMouseButtonsUp;
            }

            public int[] getKeyboardMouseButtonsDown() {
                return inputState.config.input_keyboardMouseButtonsDown;
            }

            public void setKeyboardMouseButtonsDown(int[] input_keyboardMouseButtonsDown) {
                inputState.config.input_keyboardMouseButtonsDown = input_keyboardMouseButtonsDown;
            }

            public int[] getKeyboardMouseButtonsLeft() {
                return inputState.config.input_keyboardMouseButtonsLeft;
            }

            public void setKeyboardMouseButtonsLeft(int[] input_keyboardMouseButtonsLeft) {
                inputState.config.input_keyboardMouseButtonsLeft = input_keyboardMouseButtonsLeft;
            }

            public int[] getKeyboardMouseButtonsRight() {
                return inputState.config.input_keyboardMouseButtonsRight;
            }

            public void setKeyboardMouseButtonsRight(int[] input_keyboardMouseButtonsRight) {
                inputState.config.input_keyboardMouseButtonsRight = input_keyboardMouseButtonsRight;
            }

            public int[] getKeyboardMouseButtonsMouse1() {
                return inputState.config.input_keyboardMouseButtonsMouse1;
            }

            public void setKeyboardMouseButtonsMouse1(int[] input_keyboardMouseButtonsMouse1) {
                inputState.config.input_keyboardMouseButtonsMouse1 = input_keyboardMouseButtonsMouse1;
            }

            public int[] getKeyboardMouseButtonsMouse2() {
                return inputState.config.input_keyboardMouseButtonsMouse2;
            }

            public void setKeyboardMouseButtonsMouse2(int[] input_keyboardMouseButtonsMouse2) {
                inputState.config.input_keyboardMouseButtonsMouse2 = input_keyboardMouseButtonsMouse2;
            }

            public int[] getKeyboardMouseButtonsMouse3() {
                return inputState.config.input_keyboardMouseButtonsMouse3;
            }

            public void setKeyboardMouseButtonsMouse3(int[] input_keyboardMouseButtonsMouse3) {
                inputState.config.input_keyboardMouseButtonsMouse3 = input_keyboardMouseButtonsMouse3;
            }

            public int[] getKeyboardMouseButtonsMouse4() {
                return inputState.config.input_keyboardMouseButtonsMouse4;
            }

            public void setKeyboardMouseButtonsMouse4(int[] input_keyboardMouseButtonsMouse4) {
                inputState.config.input_keyboardMouseButtonsMouse4 = input_keyboardMouseButtonsMouse4;
            }

            public int[] getKeyboardMouseButtonsMouse5() {
                return inputState.config.input_keyboardMouseButtonsMouse5;
            }

            public void setKeyboardMouseButtonsMouse5(int[] input_keyboardMouseButtonsMouse5) {
                inputState.config.input_keyboardMouseButtonsMouse5 = input_keyboardMouseButtonsMouse5;
            }

            public int[] getKeyboardMouseButtonsScrollUp() {
                return inputState.config.input_keyboardMouseButtonsScrollUp;
            }

            public void setKeyboardMouseButtonsScrollUp(int[] input_keyboardMouseButtonsScrollUp) {
                inputState.config.input_keyboardMouseButtonsScrollUp = input_keyboardMouseButtonsScrollUp;
            }

            public int[] getKeyboardMouseButtonsScrollDown() {
                return inputState.config.input_keyboardMouseButtonsScrollDown;
            }

            public void setKeyboardMouseButtonsScrollDown(int[] input_keyboardMouseButtonsScrollDown) {
                inputState.config.input_keyboardMouseButtonsScrollDown = input_keyboardMouseButtonsScrollDown;
            }

            public boolean isGamePadMouseEnabled() {
                return inputState.config.input_gamePadMouseEnabled;
            }

            public void setGamePadMouseEnabled(boolean input_gamePadMouseEnabled) {
                inputState.config.input_gamePadMouseEnabled = input_gamePadMouseEnabled;
            }

            public float getGamePadMouseJoystickDeadZone() {
                return inputState.config.input_gamePadMouseJoystickDeadZone;
            }

            public void setGamePadMouseJoystickDeadZone(float input_gamePadMouseJoystickDeadZone) {
                inputState.config.input_gamePadMouseJoystickDeadZone = input_gamePadMouseJoystickDeadZone;
            }

            public boolean isInput_gamePadMouseStickLeftEnabled() {
                return inputState.config.input_gamePadMouseStickLeftEnabled;
            }

            public void setGamePadMouseStickLeftEnabled(boolean input_gamePadMouseStickLeftEnabled) {
                inputState.config.input_gamePadMouseStickLeftEnabled = input_gamePadMouseStickLeftEnabled;
            }

            public boolean isGamePadMouseStickRightEnabled() {
                return inputState.config.input_gamePadMouseStickRightEnabled;
            }

            public void setGamePadMouseStickRightEnabled(boolean input_gamePadMouseStickRightEnabled) {
                inputState.config.input_gamePadMouseStickRightEnabled = input_gamePadMouseStickRightEnabled;
            }

            public int[] getGamePadMouseButtonsMouse1() {
                return inputState.config.input_gamePadMouseButtonsMouse1;
            }

            public void setGamePadMouseButtonsMouse1(int[] input_gamePadMouseButtonsMouse1) {
                inputState.config.input_gamePadMouseButtonsMouse1 = input_gamePadMouseButtonsMouse1;
            }

            public int[] getGamePadMouseButtonsMouse2() {
                return inputState.config.input_gamePadMouseButtonsMouse2;
            }

            public void setGamePadMouseButtonsMouse2(int[] input_gamePadMouseButtonsMouse2) {
                inputState.config.input_gamePadMouseButtonsMouse2 = input_gamePadMouseButtonsMouse2;
            }

            public int[] getGamePadMouseButtonsMouse3() {
                return inputState.config.input_gamePadMouseButtonsMouse3;
            }

            public void setGamePadMouseButtonsMouse3(int[] input_gamePadMouseButtonsMouse3) {
                inputState.config.input_gamePadMouseButtonsMouse3 = input_gamePadMouseButtonsMouse3;
            }

            public int[] getGamePadMouseButtonsMouse4() {
                return inputState.config.input_gamePadMouseButtonsMouse4;
            }

            public void setGamePadMouseButtonsMouse4(int[] input_gamePadMouseButtonsMouse4) {
                inputState.config.input_gamePadMouseButtonsMouse4 = input_gamePadMouseButtonsMouse4;
            }

            public int[] getGamePadMouseButtonsMouse5() {
                return inputState.config.input_gamePadMouseButtonsMouse5;
            }

            public void setGamePadMouseButtonsMouse5(int[] input_gamePadMouseButtonsMouse5) {
                inputState.config.input_gamePadMouseButtonsMouse5 = input_gamePadMouseButtonsMouse5;
            }

            public int[] getGamePadMouseButtonsScrollUp() {
                return inputState.config.input_gamePadMouseButtonsScrollUp;
            }

            public void setGamePadMouseButtonsScrollUp(int[] input_gamePadMouseButtonsScrollUp) {
                inputState.config.input_gamePadMouseButtonsScrollUp = input_gamePadMouseButtonsScrollUp;
            }

            public int[] getGamePadMouseButtonsScrollDown() {
                return inputState.config.input_gamePadMouseButtonsScrollDown;
            }

            public void setGamePadMouseButtonsScrollDown(int[] input_gamePadMouseButtonsScrollDown) {
                inputState.config.input_gamePadMouseButtonsScrollDown = input_gamePadMouseButtonsScrollDown;
            }
        }

        public class _WindowConfig {
            public boolean isDefaultEnforceScreenBounds() {
                return inputState.config.window_defaultEnforceScreenBounds;
            }

            public void setDefaultEnforceScreenBounds(boolean windows_defaultEnforceScreenBounds) {
                inputState.config.window_defaultEnforceScreenBounds = windows_defaultEnforceScreenBounds;
            }

            public Color getDefaultColor() {
                return inputState.config.window_defaultColor;
            }

            public void setDefaultColor(Color windows_defaultColor) {
                inputState.config.window_defaultColor = windows_defaultColor;
            }

            public CMediaFont getDefaultFont() {
                return inputState.config.window_defaultFont;
            }

            public void setDefaultFont(CMediaFont windows_defaultFont) {
                inputState.config.window_defaultFont = windows_defaultFont;
            }
        }

        public class _ComponentConfig {
            public Color getDefaultColor() {
                return inputState.config.component_defaultColor;
            }

            public void setDefaultColor(Color components_defaultColor) {
                inputState.config.component_defaultColor = components_defaultColor;
            }

            public CMediaFont getDefaultFont() {
                return inputState.config.component_defaultFont;
            }

            public void setDefaultFont(CMediaFont components_defaultFont) {
                inputState.config.component_defaultFont = components_defaultFont;
            }

            public int getGameViewportDefaultUpdateTime() {
                return inputState.config.component_gameViewportDefaultUpdateTime;
            }

            public void setGameViewportDefaultUpdateTime(int gameViewport_defaultUpdateTime) {
                inputState.config.component_gameViewportDefaultUpdateTime = gameViewport_defaultUpdateTime;
            }

            public float getListDragAlpha() {
                return inputState.config.component_listDragAlpha;
            }

            public void setListDragAlpha(float list_dragAlpha) {
                inputState.config.component_listDragAlpha = list_dragAlpha;
            }

            public float getGridDragAlpha() {
                return inputState.config.component_gridDragAlpha;
            }

            public void setGridDragAlpha(float grid_dragAlpha) {
                inputState.config.component_gridDragAlpha = grid_dragAlpha;
            }

            public float getKnobSensitivity() {
                return inputState.config.component_knobSensitivity;
            }

            public void setKnobSensitivity(float knob_sensitivity) {
                inputState.config.component_knobSensitivity = knob_sensitivity;
            }

            public float getScrollbarSensitivity() {
                return inputState.config.component_scrollbarSensitivity;
            }

            public void setScrollbarSensitivity(float scrollbar_sensitivity) {
                inputState.config.component_scrollbarSensitivity = scrollbar_sensitivity;
            }

            public int getMapOverlayDefaultFadeoutTime() {
                return inputState.config.component_mapOverlayDefaultFadeoutTime;
            }

            public void setMapOverlayDefaultFadeoutTime(int mapOverlay_defaultFadeoutTime) {
                inputState.config.component_mapOverlayDefaultFadeoutTime = mapOverlay_defaultFadeoutTime;
            }

            public char[] getTextfieldDefaultAllowedCharacters() {
                return inputState.config.component_textFieldDefaultAllowedCharacters;
            }

            public void setTextfieldDefaultAllowedCharacters(char[] textField_defaultAllowedCharacters) {
                inputState.config.component_textFieldDefaultAllowedCharacters = textField_defaultAllowedCharacters;
            }

        }

        public class _NotificationsConfig {
            public int getMax() {
                return inputState.config.notification_max;
            }

            public void setMax(int notifications_max) {
                inputState.config.notification_max = notifications_max;
            }

            public int getNotifications_defaultDisplayTime() {
                return inputState.config.notification_defaultDisplayTime;
            }

            public void setDefaultDisplayTime(int notifications_defaultDisplayTime) {
                inputState.config.notification_defaultDisplayTime = notifications_defaultDisplayTime;
            }

            public CMediaFont getNotifications_defaultFont() {
                return inputState.config.notification_defaultFont;
            }

            public void setDefaultFont(CMediaFont notifications_defaultFont) {
                inputState.config.notification_defaultFont = notifications_defaultFont;
            }

            public Color getNotifications_defaultColor() {
                return inputState.config.notification_defaultColor;
            }

            public void setDefaultColor(Color notifications_defaultColor) {
                inputState.config.notification_defaultColor = notifications_defaultColor;
            }

            public int getNotifications_fadeoutTime() {
                return inputState.config.notification_fadeoutTime;
            }

            public void setFadeoutTime(int notifications_fadeoutTime) {
                inputState.config.notification_fadeoutTime = notifications_fadeoutTime;
            }

            public float getNotifications_scrollSpeed() {
                return inputState.config.notification_scrollSpeed;
            }

            public void setScrollSpeed(float notifications_scrollSpeed) {
                inputState.config.notification_scrollSpeed = notifications_scrollSpeed;
            }
        }

        public class _ToolTipConfig {
            public Color getDefaultColor() {
                return inputState.config.tooltip_defaultColor;
            }

            public void setDefaultColor(Color tooltip_defaultColor) {
                inputState.config.tooltip_defaultColor = tooltip_defaultColor;
            }

            public CMediaFont getDefaultFont() {
                return inputState.config.tooltip_defaultFont;
            }

            public void setDefaultFont(CMediaFont tooltip_defaultFont) {
                inputState.config.tooltip_defaultFont = tooltip_defaultFont;
            }

            public int getFadeInTime() {
                return inputState.config.tooltip_FadeInTime;
            }

            public void setFadeInTime(int tooltip_FadeInTime) {
                inputState.config.tooltip_FadeInTime = tooltip_FadeInTime;
            }

            public int getFadeInDelayTime() {
                return inputState.config.tooltip_FadeInDelayTime;
            }

            public void setFadeInDelayTime(int tooltip_FadeInDelayTime) {
                inputState.config.tooltip_FadeInDelayTime = tooltip_FadeInDelayTime;
            }

        }

        public class _MouseTextInputConfig {
            public CMediaFont getDefaultFont() {
                return inputState.config.mouseTextInput_defaultFont;
            }

            public void setDefaultFont(CMediaFont mouseTextInput_defaultFont) {
                inputState.config.mouseTextInput_defaultFont = mouseTextInput_defaultFont;
            }

            public char[] getDefaultLowerCaseCharacters() {
                return inputState.config.mouseTextInput_defaultLowerCaseCharacters;
            }

            public void setDefaultLowerCaseCharacters(char[] mouseTextInput_defaultLowerCaseCharacters) {
                inputState.config.mouseTextInput_defaultLowerCaseCharacters = mouseTextInput_defaultLowerCaseCharacters;
            }

            public char[] getDefaultUpperCaseCharacters() {
                return inputState.config.mouseTextInput_defaultUpperCaseCharacters;
            }

            public void setDefaultUpperCaseCharacters(char[] mouseTextInput_defaultUpperCaseCharacters) {
                inputState.config.mouseTextInput_defaultUpperCaseCharacters = mouseTextInput_defaultUpperCaseCharacters;
            }
        }
    }

    public class _Input {
        public final _Mouse mouse = new _Mouse();
        public final _KeyBoard keyboard = new _KeyBoard();
        public final _GamePad gamepad = new _GamePad();

        public InputMethod lastUsedInputMethod() {
            return inputState.inputEvents.lastUsedInputMethod;
        }

        public class _Mouse {
            public final _Event event = new _Event();
            public final _State state = new _State();

            public final _Emulated emulated = new _Emulated();

            public class _Emulated {
                public void setPosition(int x, int y) {
                    UICommons.emulatedMouse_setPosition(inputState, x, y);
                }

                public void setPositionComponent(Component component) {
                    UICommons.emulatedMouse_setPositionComponent(inputState, component);
                }
            }


            private String mouseUIObjectName(Object mouseObject) {
                if (mouseObject != null) {
                    if (mouseObject instanceof Component component) {
                        return component.name;
                    } else if (mouseObject instanceof Window window) {
                        return window.name;
                    }
                }
                return "";
            }

            public MOUSE_CONTROL_MODE currentControlMode() {
                return inputState.currentControlMode;
            }

            public Object hoverUIObject() {
                return inputState.lastUIMouseHover;
            }

            public boolean isHoveringOverUIObject() {
                return hoverUIObject() != null;
            }

            public String hoverUIObjectName() {
                return mouseUIObjectName(hoverUIObject());
            }

            public Object useUIObject() {
                return inputState.mouseInteractedUIObjectFrame != null ? inputState.mouseInteractedUIObjectFrame : null;
            }

            public boolean isUsingUIObject() {
                return useUIObject() != null;
            }

            public String useUIObjectName() {
                return mouseUIObjectName(useUIObject());
            }

            public class _Event {
                /* ---- MOUSE EVENTS --- */
                public boolean buttonDown() {
                    return inputState.inputEvents.mouseDown;
                }

                public boolean doubleClick() {
                    return inputState.inputEvents.mouseDoubleClick;
                }

                public boolean buttonUp() {
                    return inputState.inputEvents.mouseUp;
                }

                public boolean dragged() {
                    return inputState.inputEvents.mouseDragged;
                }

                public boolean moved() {
                    return inputState.inputEvents.mouseMoved;
                }

                public boolean scrolled() {
                    return inputState.inputEvents.mouseScrolled;
                }

                public float scrolledAmount() {
                    return inputState.inputEvents.mouseScrolledAmount;
                }

                public boolean upHasNextButton() {
                    return inputState.inputEvents.mouseUpButtonIndex < inputState.inputEvents.mouseUpButtons.size;
                }

                public int upNextButton() {
                    return upHasNextButton() ? inputState.inputEvents.mouseUpButtons.get(inputState.inputEvents.mouseUpButtonIndex++) : KeyCode.NONE;
                }

                public boolean downHasNextButton() {
                    return inputState.inputEvents.mouseDownButtonIndex < inputState.inputEvents.mouseDownButtons.size;
                }

                public int downNextButton() {
                    return downHasNextButton() ? inputState.inputEvents.mouseDownButtons.get(inputState.inputEvents.mouseDownButtonIndex++) : KeyCode.NONE;
                }
            }

            public class _State {
                public int x() {
                    return inputState.mouse_game.x;
                }

                public int y() {
                    return inputState.mouse_game.y;
                }

                public int xUI() {
                    return inputState.mouse_ui.x;
                }

                public int yUI() {
                    return inputState.mouse_ui.y;
                }

                public float xDelta() {
                    return inputState.mouse_delta.x;
                }

                public float yDelta() {
                    return inputState.mouse_delta.y;
                }

                public boolean isButtonUp(int keyCode) {
                    return !inputState.inputEvents.mouseButtonsDown[keyCode];
                }

                public boolean isAnyButtonUp(int[] keyCodes) {
                    for (int i = 0; i < keyCodes.length; i++) {
                        if (isButtonUp(keyCodes[i])) return true;
                    }
                    return false;
                }

                public boolean isButtonDown(int button) {
                    return inputState.inputEvents.mouseButtonsDown[button];
                }

                public boolean isAnyButtonDown(int[] keyCodes) {
                    for (int i = 0; i < keyCodes.length; i++) {
                        if (isButtonDown(keyCodes[i])) return true;
                    }
                    return false;
                }
            }

        }

        public class _KeyBoard {

            public final _Event event = new _Event();
            public final _State state = new _State();

            private String keyBoardGUIObjectName(Object keyBoardobject) {
                if (keyBoardobject != null) {
                    if (keyBoardobject instanceof Component component) {
                        return component.name;
                    } else if (keyBoardobject instanceof HotKey hotKey) {
                        return hotKey.name;
                    }
                }
                return "";
            }

            public Object useUIObject() {
                return inputState.keyboardInteractedUIObjectFrame != null ? inputState.keyboardInteractedUIObjectFrame : null;
            }

            public boolean isUsingUIObject() {
                return useUIObject() != null;
            }

            public String keyBoardUsingUIObjectName() {
                return keyBoardGUIObjectName(useUIObject());
            }

            public class _Event {
                public boolean keyDown() {
                    return inputState.inputEvents.keyDown;
                }

                public boolean keyUp() {
                    return inputState.inputEvents.keyUp;
                }

                public boolean keyTyped() {
                    return inputState.inputEvents.keyTyped;
                }

                public boolean keyTypedCharacter(Character character) {
                    for (int i = 0; i < inputState.inputEvents.keyTypedCharacters.size; i++)
                        if (character == inputState.inputEvents.keyTypedCharacters.get(i)) return true;
                    return false;
                }

                public boolean keyUpHasNext() {
                    return inputState.inputEvents.keyUpKeyCodeIndex < inputState.inputEvents.keyUpKeyCodes.size;
                }

                public int keyUpNext() {
                    return keyUpHasNext() ? inputState.inputEvents.keyUpKeyCodes.get(inputState.inputEvents.keyUpKeyCodeIndex++) : KeyCode.NONE;
                }

                public boolean keyDownHasNext() {
                    return inputState.inputEvents.keyDownKeyCodeIndex < inputState.inputEvents.keyDownKeyCodes.size;
                }

                public int keyDownNext() {
                    return keyDownHasNext() ? inputState.inputEvents.keyDownKeyCodes.get(inputState.inputEvents.keyDownKeyCodeIndex++) : KeyCode.NONE;
                }

                public boolean keyTypedHasNext() {
                    return inputState.inputEvents.keyTypedCharacterIndex < inputState.inputEvents.keyTypedCharacters.size;
                }

                public char keyTypedNext() {
                    return (char) (keyTypedHasNext() ? inputState.inputEvents.keyTypedCharacters.get(inputState.inputEvents.keyTypedCharacterIndex++) : -1);
                }
            }

            public class _State {
                public boolean isKeyDown(int keyCode) {
                    return inputState.inputEvents.keysDown[keyCode];
                }

                public boolean isAnyKeyDown(int[] keyCodes) {
                    for (int i = 0; i < keyCodes.length; i++) {
                        if (isKeyDown(keyCodes[i])) return true;
                    }
                    return false;
                }

                public boolean isKeyUp(int keyCode) {
                    return !inputState.inputEvents.keysDown[keyCode];
                }

                public boolean isAnyKeyUp(int[] keyCodes) {
                    for (int i = 0; i < keyCodes.length; i++) {
                        if (isKeyUp(keyCodes[i])) return true;
                    }
                    return false;
                }
            }

        }

        public class _GamePad {

            public final _Event event = new _Event();
            public final _State state = new _State();

            public class _Event {
                public boolean buttonDown() {
                    return inputState.inputEvents.gamePadButtonDown;
                }

                public boolean buttonUp() {
                    return inputState.inputEvents.gamePadButtonUp;
                }

                public boolean connected() {
                    return inputState.inputEvents.gamePadConnected;
                }

                public boolean disconnected() {
                    return inputState.inputEvents.gamePadDisconnected;
                }

                public boolean leftXMoved() {
                    return inputState.inputEvents.gamePadLeftXMoved;
                }

                public boolean leftYMoved() {
                    return inputState.inputEvents.gamePadLeftYMoved;
                }

                public boolean leftMoved() {
                    return leftXMoved() || leftYMoved();
                }

                public boolean rightXMoved() {
                    return inputState.inputEvents.gamePadRightXMoved;
                }

                public boolean rightYMoved() {
                    return inputState.inputEvents.gamePadRightYMoved;
                }

                public boolean rightMoved() {
                    return rightXMoved() || rightYMoved();
                }

                public boolean leftTriggerMoved() {
                    return inputState.inputEvents.gamePadLeftTriggerMoved;
                }

                public boolean rightTriggerMoved() {
                    return inputState.inputEvents.gamePadRightTriggerMoved;
                }

                public boolean buttonDownHasNext() {
                    return inputState.inputEvents.gamePadButtonDownIndex < inputState.inputEvents.gamePadButtonDownKeyCodes.size;
                }

                public int buttonDownNext() {
                    return buttonDownHasNext() ? inputState.inputEvents.gamePadButtonDownKeyCodes.get(inputState.inputEvents.gamePadButtonDownIndex++) : KeyCode.NONE;
                }

                public boolean buttonUpHasNext() {
                    return inputState.inputEvents.gamePadButtonUpIndex < inputState.inputEvents.gamePadButtonUpKeyCodes.size;
                }

                public int buttonUpNext() {
                    return buttonUpHasNext() ? inputState.inputEvents.gamePadButtonUpKeyCodes.get(inputState.inputEvents.gamePadButtonUpIndex++) : KeyCode.NONE;
                }
            }

            public class _State {
                public boolean isButtonDown(int keyCode) {
                    return inputState.inputEvents.gamePadButtonsDown[keyCode];
                }

                public boolean isAnyButtonDown(int[] keyCodes) {
                    for (int i = 0; i < keyCodes.length; i++) {
                        if (isButtonDown(keyCodes[i])) return true;
                    }
                    return false;
                }

                public boolean isButtonUp(int keyCode) {
                    return !inputState.inputEvents.gamePadButtonsDown[keyCode];
                }

                public boolean isAnyButtonUp(int[] keyCodes) {
                    for (int i = 0; i < keyCodes.length; i++) {
                        if (isButtonUp(keyCodes[i])) return true;
                    }
                    return false;
                }

                public float leftTrigger() {
                    return inputState.inputEvents.gamePadLeftTrigger;
                }

                public float rightTrigger() {
                    return inputState.inputEvents.gamePadRightTrigger;
                }

                public float leftX() {
                    return inputState.inputEvents.gamePadLeftX;
                }

                public float leftY() {
                    return inputState.inputEvents.gamePadLeftY;
                }

                public float rightX() {
                    return inputState.inputEvents.gamePadRightX;
                }

                public float rightY() {
                    return inputState.inputEvents.gamePadRightY;
                }
            }

        }


    }

    public class _Notification {

        private NotificationAction defaultNotificationAction() {
            return new NotificationAction() {
            };
        }

        public Notification create(String text) {
            return create(text, defaultNotificationAction(), inputState.config.notification_defaultDisplayTime);
        }
        public Notification create(String text, NotificationAction notificationAction) {
            return create(text, notificationAction, inputState.config.notification_defaultDisplayTime);
        }

        public Notification create(String text, NotificationAction notificationAction, int displayTime) {
            Notification notification = new Notification();
            notification.text = Tools.Text.validString(text);
            notification.displayTime = displayTime;
            notification.color_r = inputState.config.notification_defaultColor.r;
            notification.color_g = inputState.config.notification_defaultColor.g;
            notification.color_b = inputState.config.notification_defaultColor.b;
            notification.color_a = inputState.config.notification_defaultColor.a;
            notification.font = inputState.config.notification_defaultFont;
            notification.notificationAction = notificationAction;
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

        public void setColor(Notification notification, Color color) {
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
            notification.font = font;
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
            return create(contextMenuItems, contextMenuAction, 1f);
        }

        public ContextMenu create(ContextMenuItem[] contextMenuItems, ContextMenuAction contextMenuAction, float alpha) {
            ContextMenu contextMenu = new ContextMenu();
            contextMenu.items = new ArrayList<>();
            if (contextMenuItems != null) {
                for (int i = 0; i < contextMenuItems.length; i++) {
                    if (contextMenuItems[i].addedToContextMenu == null) {
                        contextMenu.items.add(contextMenuItems[i]);
                        contextMenuItems[i].addedToContextMenu = contextMenu;
                    }
                }
            }
            contextMenu.color_a = Tools.Calc.inBounds(alpha, 0f, 1f);
            contextMenu.contextMenuAction = contextMenuAction;
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
            for (int i = 0; i < contextMenuItems.length; i++) addContextMenuItem(contextMenu, contextMenuItems[i]);
        }

        public void removeContextMenuItem(ContextMenu contextMenu, ContextMenuItem contextMenuItem) {
            if (contextMenu == null || contextMenuItem == null) return;
            UICommons.contextMenu_removeItem(contextMenu, contextMenuItem);
        }

        public void removeContextMenuItems(ContextMenu contextMenu, ContextMenuItem[] contextMenuItems) {
            if (contextMenu == null || contextMenuItems == null) return;
            for (int i = 0; i < contextMenuItems.length; i++)
                removeContextMenuItem(contextMenu, contextMenuItems[i]);
        }

        public void removeAllContextMenuItems(ContextMenu contextMenu) {
            if (contextMenu == null) return;
            removeContextMenuItems(contextMenu, contextMenu.items.toArray(new ContextMenuItem[]{}));
        }

        public ArrayList<ContextMenuItem> findContextMenuItemsByName(ContextMenu contextMenu, String name) {
            if (contextMenu == null || name == null) return new ArrayList<>();
            ArrayList<ContextMenuItem> result = new ArrayList<>();
            for (int i = 0; i < contextMenu.items.size(); i++)
                if (name.equals(contextMenu.items.get(i).name)) result.add(contextMenu.items.get(i));
            return result;
        }

        public ContextMenuItem findContextMenuItemByName(ContextMenu contextMenu, String name) {
            if (contextMenu == null || name == null) return null;
            ArrayList<ContextMenuItem> result = findContextMenuItemsByName(contextMenu, name);
            return result.size() > 0 ? result.getFirst() : null;
        }

        public class _ContextMenuItem {

            private ContextMenuItemAction defaultContextMenuItemAction() {
                return new ContextMenuItemAction() {
                };
            }

            public ContextMenuItem create(String text) {
                return create(text, defaultContextMenuItemAction(), null, 0);
            }

            public ContextMenuItem create(String text, ContextMenuItemAction contextMenuItemAction) {
                return create(text, contextMenuItemAction, null, 0);
            }

            public ContextMenuItem create(String text, ContextMenuItemAction contextMenuItemAction, CMediaGFX icon, int iconIndex) {
                ContextMenuItem contextMenuItem = new ContextMenuItem();
                contextMenuItem.text = Tools.Text.validString(text);
                contextMenuItem.font = inputState.config.component_defaultFont;
                contextMenuItem.color_r = inputState.config.component_defaultColor.r;
                contextMenuItem.color_g = inputState.config.component_defaultColor.g;
                contextMenuItem.color_b = inputState.config.component_defaultColor.b;
                contextMenuItem.icon = icon;
                contextMenuItem.iconIndex = iconIndex;
                contextMenuItem.name = "";
                contextMenuItem.data = null;
                contextMenuItem.contextMenuItemAction = contextMenuItemAction;
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

            public void setColor(ContextMenuItem contextMenuItem, Color color) {
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
                contextMenuItem.font = font;
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

            public void selectItem(ContextMenuItem contextMenuItem) {
                if (contextMenuItem == null) return;
                UICommons.contextMenu_selectItem(inputState, contextMenuItem);
            }

        }

    }

    public class _Window {

        private WindowAction defaultWindowAction() {
            return new WindowAction() {
            };
        }

        public Window create(int x, int y, int width, int height) {
            return create(x,y,width, height,"",null,0,null, false,true, true, true);
        }

        public Window create(int x, int y, int width, int height, String title) {
            return create(x,y,width, height,title,null,0,null, false,true, true, true);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, int iconIndex) {
            return create(x,y,width, height,title,icon,iconIndex,null, false,true, true, true);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, int iconIndex, WindowAction windowAction) {
            return create(x,y,width, height,title,icon,iconIndex,windowAction, false,true, true, true);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, int iconIndex, WindowAction windowAction, boolean alwaysOnTop) {
            return create(x,y,width, height,title,icon,iconIndex,windowAction, alwaysOnTop,true, true, true);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, int iconIndex, WindowAction windowAction, boolean alwaysOnTop, boolean moveAble) {
            return create(x,y,width, height,title,icon,iconIndex,windowAction, alwaysOnTop,moveAble, true, true);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, int iconIndex, WindowAction windowAction, boolean alwaysOnTop, boolean moveAble, boolean hasTitleBar) {
            return create(x,y,width, height,title,icon,iconIndex,windowAction, alwaysOnTop,moveAble, hasTitleBar, true);
        }

        public Window create(int x, int y, int width, int height, String title, CMediaGFX icon, int iconIndex, WindowAction windowAction, boolean alwaysOnTop, boolean moveAble, boolean hasTitleBar, boolean visible) {
            Window window = new Window();
            window.x = x;
            window.y = y;
            window.width = Tools.Calc.lowerBounds(width, 2);
            window.height = Tools.Calc.lowerBounds(height, 2);
            window.title = Tools.Text.validString(title);
            window.alwaysOnTop = alwaysOnTop;
            window.moveAble = moveAble;
            window.color_r = inputState.config.window_defaultColor.r;
            window.color_g = inputState.config.window_defaultColor.g;
            window.color_b = inputState.config.window_defaultColor.b;
            window.color_a = inputState.config.window_defaultColor.a;
            window.font = inputState.config.window_defaultFont;
            window.hasTitleBar = hasTitleBar;
            window.visible = visible;
            window.windowAction = windowAction;
            window.icon = icon;
            window.iconIndex = iconIndex;
            window.name = "";
            window.data = null;
            window.enforceScreenBounds = inputState.config.window_defaultEnforceScreenBounds;
            window.messageReceiverActions = new ArrayList<>();
            window.updateActions = new ArrayList<>();
            window.addedToScreen = false;
            window.components = new ArrayList<>();
            return window;
        }


        public void addMessageReceiverAction(Window window, MessageReceiverAction messageReceiverAction) {
            if (window == null || messageReceiverAction == null) return;
            window.messageReceiverActions.add(messageReceiverAction);
        }

        public void addMessageReceiverActions(Window window, MessageReceiverAction[] messageReceiverActions) {
            if (window == null || messageReceiverActions == null) return;
            for (int i = 0; i < messageReceiverActions.length; i++)
                addMessageReceiverAction(window, messageReceiverActions[i]);
        }

        public void removeMessageReceiverAction(Window window, MessageReceiverAction messageReceiverAction) {
            if (window == null || messageReceiverAction == null) return;
            window.messageReceiverActions.remove(messageReceiverAction);
        }

        public void removeMessageReceiverActions(Window window, MessageReceiverAction[] messageReceiverActions) {
            if (window == null || messageReceiverActions == null) return;
            for (int i = 0; i < messageReceiverActions.length; i++)
                removeMessageReceiverAction(window, messageReceiverActions[i]);
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

        private void setColorFunction(Window window, Color color, int setColorMode, Class[] classes,
                                      boolean windowColor, boolean componentColor1, boolean componentColor2, boolean comboBoxItemColor) {
            if (classes == null) classes = new Class[]{};
            if (windowColor) setColor(window, color);
            for (int i = 0; i < window.components.size(); i++) {
                Component component = window.components.get(i);
                boolean match = setColorMode == 1 ? false : true;
                classLoop:
                for (int i2 = 0; i2 < classes.length; i2++) {
                    if (classes[i2] == component.getClass()) {
                        match = setColorMode == 1 ? true : false;
                        break classLoop;
                    }
                }
                if (match) {
                    if (componentColor1) API.this.component.setColor(component, color);
                    if (componentColor2) API.this.component.setColor2(component, color);
                    if (component instanceof ComboBox comboBox) {
                        for (int i2 = 0; i2 < comboBox.comboBoxItems.size(); i2++)
                            API.this.component.comboBox.item.setColor(comboBox.comboBoxItems.get(i2), color);
                    }
                }
            }
        }

        public void setColorEverything(Window window, Color color) {
            setColorFunction(window, color, 2, null,
                    true, true, true, true);
        }

        public void setColorEverything(Window window, Color color, boolean windowColor, boolean componentColor1, boolean componentColor2, boolean comboBoxItems) {
            setColorFunction(window, color, 2, null,
                    windowColor, componentColor1, componentColor2, comboBoxItems);
        }

        public void setColorEverythingExcept(Window window, Color color, Class[] exceptions) {
            setColorFunction(window, color, 2, exceptions,
                    true, true, true, true);
        }

        public void setColorEverythingExcept(Window window, Color color, Class[] exceptions, boolean windowColor, boolean componentColor1, boolean componentColor2, boolean comboBoxItems) {
            setColorFunction(window, color, 2, exceptions,
                    windowColor, componentColor1, componentColor2, comboBoxItems);
        }


        public void setColorEverythingInclude(Window window, Color color, Class[] inclusions) {
            setColorFunction(window, color, 1, inclusions,
                    true, true, true, true);
        }

        public void setColorEverythingInclude(Window window, Color color, Class[] inclusions, boolean windowColor, boolean componentColor1, boolean componentColor2, boolean comboBoxItems) {
            setColorFunction(window, color, 1, inclusions,
                    windowColor, componentColor1, componentColor2, comboBoxItems);
        }

        public int getRealWidth(Window window) {
            return UICommons.window_getRealWidth(window);
        }

        public int getRealHeight(Window window) {
            return UICommons.window_getRealHeight(window);
        }

        public void setColorEverythingInclude(Window window, Color color, Class[] inclusions, boolean setColor1, boolean setColor2, boolean includeWindow) {
            if (window == null) return;
            if (inclusions != null) {
                for (int i = 0; i < window.components.size(); i++) {
                    Component component = window.components.get(i);
                    classLoop:
                    for (int i2 = 0; i2 < inclusions.length; i2++) {
                        if (component.getClass() == inclusions[i2]) {
                            if (setColor1) API.this.component.setColor(component, color);
                            if (setColor2) API.this.component.setColor2(component, color);
                            break classLoop;
                        }
                    }
                }
            }

            if (includeWindow) setColor(window, color);
        }


        public Window createFromGenerator(WindowGenerator windowGenerator, Object... params) {
            if (windowGenerator == null) return null;
            return windowGenerator.create(params);
        }

        public void addComponent(Window window, Component component) {
            if (window == null || component == null) return;
            UICommons.component_addToWindow(component, inputState, window);
        }

        public void addComponents(Window window, Component[] components) {
            if (window == null || components == null) return;
            for (int i = 0; i < components.length; i++) addComponent(window, components[i]);
        }

        public void removeComponent(Window window, Component component) {
            if (window == null || component == null) return;
            UICommons.component_removeFromWindow(component, window, inputState);
        }

        public void removeComponents(Window window, Component[] components) {
            if (window == null || components == null) return;
            for (int i = 0; i < components.length; i++) removeComponent(window, components[i]);
        }

        public void removeAllComponents(Window window) {
            if (window == null) return;
            removeComponents(window, window.components.toArray(new Component[]{}));
        }

        public ArrayList<Component> findComponentsByName(Window window, String name) {
            if (window == null || name == null) return new ArrayList<>();
            ArrayList<Component> result = new ArrayList<>();
            for (int i = 0; i < window.components.size(); i++)
                if (name.equals(window.components.get(i).name)) result.add(window.components.get(i));
            return result;
        }

        public Component findComponentByName(Window window, String name) {
            if (window == null || name == null) return null;
            ArrayList<Component> result = findComponentsByName(window, name);
            return result.size() > 0 ? result.getFirst() : null;
        }

        public void bringToFront(Window window) {
            if (window == null) return;
            UICommons.window_bringToFront(inputState, window);
        }

        public void center(Window window) {
            if (window == null) return;
            int centerX = (inputState.internalResolutionWidth / 2) - (UICommons.window_getRealWidth(window) / 2);
            int centerY = (inputState.internalResolutionHeight / 2) - ((window.folded ? UIEngine.TILE_SIZE : UICommons.window_getRealHeight(window)) / 2);
            setPosition(window, centerX, centerY);
        }

        public void setFont(Window window, CMediaFont font) {
            if (window == null) return;
            window.font = font;
        }

        public void addUpdateAction(Window window, UpdateAction updateAction) {
            if (window == null || updateAction == null) return;
            window.updateActions.add(updateAction);
        }

        public void addUpdateActions(Window window, UpdateAction[] updateActions) {
            if (window == null || updateActions == null) return;
            for (int i = 0; i < updateActions.length; i++) addUpdateAction(window, updateActions[i]);
        }

        public void removeUpdateAction(Window window, UpdateAction updateAction) {
            if (window == null || updateAction == null) return;
            window.updateActions.remove(updateAction);
        }

        public void removeUpdateActions(Window window, UpdateAction[] updateActions) {
            if (window == null || updateActions == null) return;
            for (int i = 0; i < updateActions.length; i++) removeUpdateAction(window, updateActions[i]);
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

        public void setColor(Window window, Color color) {
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
            UICommons.window_setPosition(inputState, window, x, y);
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

            public ToolTipImage create(CMediaGFX image) {
                return create(image, 0,0);
            }

            public ToolTipImage create(CMediaGFX image, int x, int y) {
                ToolTipImage toolTipImage = new ToolTipImage();
                toolTipImage.image = image;
                toolTipImage.x = x;
                toolTipImage.y = y;
                toolTipImage.color_r = Color.WHITE.r;
                toolTipImage.color_g = Color.WHITE.g;
                toolTipImage.color_b = Color.WHITE.b;
                toolTipImage.color_a = Color.WHITE.a;
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

            public void setColor(ToolTipImage toolTipImage, Color color) {
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
            return create(lines, null, defaultToolTipAction(), true, 1, 1);
        }


        public ToolTip create(String[] lines, ToolTipImage[] images) {
            return create(lines, images, defaultToolTipAction(), true, 1, 1);
        }

        public ToolTip create(String[] lines, ToolTipImage[] images, ToolTipAction toolTipAction) {
            return create(lines, images, toolTipAction, true, 1, 1);
        }

        public ToolTip create(String[] lines, ToolTipImage[] images, ToolTipAction toolTipAction, boolean displayFistLineAsTitle) {
            return create(lines, images, toolTipAction, displayFistLineAsTitle, 1, 1);
        }

        public ToolTip create(String[] lines, ToolTipImage[] images, ToolTipAction toolTipAction, boolean displayFistLineAsTitle, int minWidth, int minHeight) {
            ToolTip tooltip = new ToolTip();
            tooltip.lines = Tools.Text.validStringArrayCopy(lines);
            tooltip.images = new ArrayList<>();
            if(images != null) {
                for (int i = 0; i < images.length; i++) {
                    tooltip.images.add(images[0]);
                    images[i].addedToToolTip = tooltip;
                }
            }
            tooltip.toolTipAction = toolTipAction;
            tooltip.displayFistLineAsTitle = displayFistLineAsTitle;
            tooltip.minWidth = Tools.Calc.lowerBounds(minWidth,1);
            tooltip.minHeight = Tools.Calc.lowerBounds(minHeight,1);
            tooltip.font = inputState.config.tooltip_defaultFont;
            tooltip.color_r = inputState.config.component_defaultColor.r;
            tooltip.color_g = inputState.config.component_defaultColor.g;
            tooltip.color_b = inputState.config.component_defaultColor.b;
            tooltip.color_a = inputState.config.component_defaultColor.a;
            return tooltip;
        }

        public void addToolTipImage(ToolTip toolTip, ToolTipImage toolTipImage) {
            if (toolTip == null || toolTipImage == null) return;
            UICommons.toolTip_addToolTipImage(toolTip, toolTipImage);
        }

        public void addToolTipImages(ToolTip toolTip, ToolTipImage[] toolTipImages) {
            if (toolTip == null || toolTipImages == null) return;
            for (int i = 0; i < toolTipImages.length; i++) addToolTipImage(toolTip, toolTipImages[i]);
        }

        public void removeToolTipImage(ToolTip toolTip, ToolTipImage toolTipImage) {
            if (toolTip == null || toolTipImage == null) return;
            UICommons.toolTip_removeToolTipImage(toolTip, toolTipImage);
        }

        public void removeToolTipImages(ToolTip toolTip, ToolTipImage[] toolTipImages) {
            if (toolTip == null || toolTipImages == null) return;
            for (int i = 0; i < toolTipImages.length; i++) removeToolTipImage(toolTip, toolTipImages[i]);
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

        public void setLines(ToolTip tooltip, String[] lines) {
            if (tooltip == null) return;
            UICommons.tooltip_setLines(tooltip, lines);
        }

        public void setSizeMin(ToolTip tooltip, int minWidth, int minHeight) {
            if (tooltip == null) return;
            tooltip.minWidth = Tools.Calc.lowerBounds(minWidth, 1);
            tooltip.minHeight = Tools.Calc.lowerBounds(minHeight, 1);
        }

        public void setColor(ToolTip tooltip, Color color) {
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
            tooltip.font = font;
        }

    }

    public int resolutionWidth() {
        return inputState.internalResolutionWidth;
    }

    public int resolutionHeight() {
        return inputState.internalResolutionHeight;
    }

    public VIEWPORT_MODE viewportMode() {
        return inputState.viewportMode;
    }

    public void setViewportMode(VIEWPORT_MODE VIEWPORTMODE) {
        if (VIEWPORTMODE == null || VIEWPORTMODE == inputState.viewportMode) return;
        inputState.upscaleFactor_screen = UICommons.viewport_determineUpscaleFactor(VIEWPORTMODE, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
        inputState.textureFilter_screen = UICommons.viewport_determineUpscaleTextureFilter(VIEWPORTMODE);
        // frameBuffer_upScale
        inputState.frameBuffer_screen.dispose();
        inputState.frameBuffer_screen = new NestedFrameBuffer(Pixmap.Format.RGBA8888, inputState.internalResolutionWidth * inputState.upscaleFactor_screen, inputState.internalResolutionHeight * inputState.upscaleFactor_screen, false);
        inputState.frameBuffer_screen.getColorBufferTexture().setFilter(inputState.textureFilter_screen, inputState.textureFilter_screen);
        // texture_upScale
        inputState.texture_screen.getTexture().dispose();
        inputState.texture_screen = new TextureRegion(inputState.frameBuffer_screen.getColorBufferTexture());
        inputState.texture_screen.flip(false, true);
        // viewport_screen
        inputState.viewport_screen = UICommons.viewport_createViewport(VIEWPORTMODE, inputState.camera_screen, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
        inputState.viewport_screen.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        // viewportMode
        inputState.viewportMode = VIEWPORTMODE;
    }

    public class _Camera {

        public boolean pointVisible(float x, float y) {
            setTestingCamera(inputState.camera_game);
            if (inputState.camera_frustum.frustum.pointInFrustum(x, y, 0f)) {
                return true;
            }
            for (int i = 0; i < inputState.gameViewPorts.size(); i++) {
                GameViewPort gameViewPort = inputState.gameViewPorts.get(i);
                setTestingCamera(gameViewPort.camera);
                if (inputState.camera_frustum.frustum.pointInFrustum(x, y, 0f)) return true;
            }
            return false;
        }

        public boolean boundsVisible(float x, float y, float halfWidth, float halfHeight) {
            setTestingCamera(inputState.camera_game);
            if (inputState.camera_frustum.frustum.boundsInFrustum(x, y, 0f, halfWidth, halfHeight, 0f)) {
                return true;
            }
            for (int i = 0; i < inputState.gameViewPorts.size(); i++) {
                GameViewPort gameViewPort = inputState.gameViewPorts.get(i);
                setTestingCamera(gameViewPort.camera);
                if (inputState.camera_frustum.frustum.boundsInFrustum(x, y, 0f, halfWidth, halfHeight, 0f))
                    return true;
            }
            return false;
        }

        public boolean sphereVisible(float x, float y, float radius) {
            setTestingCamera(inputState.camera_game);
            if (inputState.camera_frustum.frustum.sphereInFrustum(x, y, 0f, radius)) {
                return true;
            }
            for (int i = 0; i < inputState.gameViewPorts.size(); i++) {
                GameViewPort gameViewPort = inputState.gameViewPorts.get(i);
                setTestingCamera(gameViewPort.camera);
                if (inputState.camera_frustum.frustum.sphereInFrustum(x, y, 0f, radius)) return true;
            }
            return false;
        }

        private void setTestingCamera(OrthographicCamera camera) {
            inputState.camera_frustum.position.set(camera.position);
            inputState.camera_frustum.zoom = camera.zoom;
            inputState.camera_frustum.viewportWidth = camera.viewportWidth;
            inputState.camera_frustum.viewportHeight = camera.viewportHeight;
            inputState.camera_frustum.update();
        }

        public float viewPortStretchFactorWidth() {
            return inputState.viewport_screen.getWorldWidth() / (float) inputState.viewport_screen.getScreenWidth();
        }

        public float viewPortStretchFactorHeight() {
            return inputState.viewport_screen.getWorldHeight() / (float) inputState.viewport_screen.getScreenHeight();
        }

        public void move(float x, float y) {
            move(x, y, 0f);
        }

        public void move(float x, float y, float z) {
            setPosition(inputState.camera_game.position.x += x, inputState.camera_game.position.y += y, inputState.camera_game.position.z += z);
        }

        public void setPosition(float x, float y) {
            setPosition(x, y, inputState.camera_game.position.z);
        }

        public void setPosition(float x, float y, float z) {
            inputState.camera_game.position.set(x, y, z);
        }

        public void setX(float x) {
            inputState.camera_game.position.x = x;
        }

        public void moveX(float x) {
            inputState.camera_game.position.x += x;
        }

        public void setY(float y) {
            inputState.camera_game.position.y = y;
        }

        public void moveY(float y) {
            inputState.camera_game.position.y += y;
        }

        public void setZ(float z) {
            inputState.camera_game.position.z = z;
        }

        public void moveZ(float z) {
            inputState.camera_game.position.z += z;
        }

        public void setZoom(float zoom) {
            inputState.camera_game.zoom = zoom;
        }

        public float x() {
            return inputState.camera_game.position.x;
        }

        public float y() {
            return inputState.camera_game.position.y;
        }

        public float z() {
            return inputState.camera_game.position.z;
        }

        public float zoom() {
            return inputState.camera_game.zoom;
        }

        public Matrix4 combined(){
            return inputState.camera_game.combined;
        }
    }

    public class _Component {
        public final _Shape shape = new _Shape();

        public final _Button button = new _Button();
        public final _TabBar tabBar = new _TabBar();
        public final _Grid grid = new _Grid();
        public final _ScrollBar scrollBar = new _ScrollBar();

        public final _List list = new _List();
        public final _TextField textField = new _TextField();
        public final _Canvas canvas = new _Canvas();
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
            for (int i = 0; i < components.length; i++) setOffset(components[i], x, y);
        }

        public void setDisabled(Component component, boolean disabled) {
            if (component == null) return;
            component.disabled = disabled;
        }

        public void setDisabled(Component[] components, boolean disabled) {
            if (components == null) return;
            for (int i = 0; i < components.length; i++) setDisabled(components, disabled);
        }

        public void addUpdateAction(Component component, UpdateAction updateAction) {
            if (component == null || updateAction == null) return;
            component.updateActions.add(updateAction);
        }

        public void addUpdateActions(Component component, UpdateAction[] updateActions) {
            if (component == null || updateActions == null) return;
            for (int i = 0; i < updateActions.length; i++) addUpdateAction(component, updateActions[i]);
        }

        public void removeUpdateAction(Component component, UpdateAction updateAction) {
            if (component == null || updateAction == null) return;
            component.updateActions.remove(updateAction);
        }

        public void removeUpdateActions(Component component, UpdateAction[] updateActions) {
            if (component == null || updateActions == null) return;
            for (int i = 0; i < updateActions.length; i++) removeUpdateAction(component, updateActions[i]);
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

            if (component instanceof GameViewPort gameViewPort) {
                UICommons.gameViewPort_resizeCameraTextureAndFrameBuffer(gameViewPort);
            }
            if (component instanceof Canvas canvas) {
                UICommons.canvas_resizeMap(canvas);
            }
        }

        public void setDimensions(Component component, int x, int y, int width, int height) {
            if (component == null) return;
            setPosition(component, x, y);
            setSize(component, width, height);
        }

        public void setColor(Component[] components, Color color) {
            if (components == null) return;
            for (int i = 0; i < components.length; i++) setColor(components[i], color);
        }

        public void setColor(Component component, Color color) {
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

        public void setColor2(Component[] components, Color color2) {
            if (components == null) return;
            for (int i = 0; i < components.length; i++) setColor2(components[i], color2);
        }

        public void setColor2(Component component, Color color) {
            if (component == null || color == null) return;
            setColor2(component, color.r, color.g, color.b);
        }

        public void setColor2(Component component, float r, float g, float b) {
            if (component == null) return;
            component.color2_r = r;
            component.color2_g = g;
            component.color2_b = b;
        }

        public void setColor1And2(Component component, Color color) {
            if (component == null) return;
            setColor(component, color);
            setColor2(component, color);
        }

        public void setColor1And2(Component[] components, Color color) {
            if (components == null) return;
            for (int i = 0; i < components.length; i++) {
                setColor(components[i], color);
                setColor2(components[i], color);
            }
        }

        public void setAlpha(Component[] components, float alpha) {
            if (components == null) return;
            for (int i = 0; i < components.length; i++) setAlpha(components[i], alpha);
        }

        public void setAlpha(Component component, float alpha) {
            if (component == null) return;
            component.color_a = alpha;
        }

        private void setComponentCommonInitValues(Component component, int x, int y, int width, int height) {
            setComponentCommonInitValues(component, x, y, width, height, inputState.config.component_defaultColor, inputState.config.component_defaultColor);
        }

        private void setComponentCommonInitValues(Component component, int x, int y, int width, int height, Color color) {
            setComponentCommonInitValues(component, x, y, width, height, color, color);
        }

        private void setComponentCommonInitValues(Component component, int x, int y, int width, int height, Color color1, Color color2) {
            component.x = x;
            component.y = y;
            component.width = width;
            component.height = height;
            component.color_a = 1f;
            component.color_r = color1.r;
            component.color_g = color1.g;
            component.color_b = color1.b;
            component.color2_r = color2.r;
            component.color2_g = color2.g;
            component.color2_b = color2.b;
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
            for (int i = 0; i < components.length; i++) setVisible(components[i], visible);
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

            public GameViewPort create(int x, int y, int width, int height) {
                return create(x, y, width, height, null, 0, 0, 1f, inputState.config.component_gameViewportDefaultUpdateTime);
            }

            public GameViewPort create(int x, int y, int width, int height, GameViewPortAction gameViewPortAction) {
                return create(x, y, width, height, gameViewPortAction, 0, 0, 1f, inputState.config.component_gameViewportDefaultUpdateTime);
            }

            public GameViewPort create(int x, int y, int width, int height, GameViewPortAction gameViewPortAction, float camPositionX, float camPositionY) {
                return create(x, y, width, height, gameViewPortAction, camPositionX, camPositionY, 1f, inputState.config.component_gameViewportDefaultUpdateTime);
            }

            public GameViewPort create(int x, int y, int width, int height, GameViewPortAction gameViewPortAction, float camPositionX, float camPositionY, float camZoom) {
                return create(x, y, width, height, gameViewPortAction, camPositionX, camPositionY, camZoom, inputState.config.component_gameViewportDefaultUpdateTime);
            }

            public GameViewPort create(int x, int y, int width, int height, GameViewPortAction gameViewPortAction, float camPositionX, float camPositionY, float camZoom, int updateTime) {
                GameViewPort gameViewPort = new GameViewPort();
                gameViewPort.updateTimer = 0;
                setComponentCommonInitValues(gameViewPort, x, y, width, height, Color.WHITE);
                int viewportWidth = gameViewPort.width * UIEngine.TILE_SIZE;
                int viewportHeight = gameViewPort.height * UIEngine.TILE_SIZE;
                gameViewPort.frameBuffer = new NestedFrameBuffer(Pixmap.Format.RGB888, viewportWidth, viewportHeight, false);
                Texture texture = gameViewPort.frameBuffer.getColorBufferTexture();
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                gameViewPort.textureRegion = new TextureRegion(texture, viewportWidth, viewportHeight);
                gameViewPort.textureRegion.flip(false, true);
                gameViewPort.camera = new OrthographicCamera(viewportWidth, viewportHeight);
                gameViewPort.camera.setToOrtho(false, viewportWidth, viewportHeight);
                gameViewPort.camera.position.set(camPositionX, camPositionY, 0f);
                gameViewPort.camera.zoom = camZoom;
                gameViewPort.updateTime = updateTime;
                gameViewPort.gameViewPortAction = gameViewPortAction;
                return gameViewPort;
            }

            public void setGameViewPortAction(GameViewPort gameViewPort, GameViewPortAction gameViewPortAction) {
                if (gameViewPort == null) return;
                gameViewPort.gameViewPortAction = gameViewPortAction;
            }

            public void setUpdateTime(GameViewPort gameViewPort, int updateTime) {
                if (gameViewPort == null) return;
                gameViewPort.updateTime = Tools.Calc.lowerBounds(updateTime, 0);
            }

            public void camMove(GameViewPort gameViewPort, float x, float y) {
                if (gameViewPort == null) return;
                camMove(gameViewPort, x, y, 0f);
            }

            public void camMove(GameViewPort gameViewPort, float x, float y, float z) {
                if (gameViewPort == null) return;
                setCamPosition(gameViewPort, gameViewPort.camera.position.x += x, gameViewPort.camera.position.y += y, gameViewPort.camera.position.z += z);
            }

            public void setCamPosition(GameViewPort gameViewPort, float x, float y) {
                if (gameViewPort == null) return;
                setCamPosition(gameViewPort, x, y, gameViewPort.camera.position.z);
            }

            public void setCamPosition(GameViewPort gameViewPort, float x, float y, float z) {
                if (gameViewPort == null) return;
                gameViewPort.camera.position.set(x, y, z);
            }

            public void setCamX(GameViewPort gameViewPort, float x) {
                if (gameViewPort == null) return;
                gameViewPort.camera.position.x = x;
            }

            public void moveCamX(GameViewPort gameViewPort, float x) {
                if (gameViewPort == null) return;
                gameViewPort.camera.position.x += x;
            }

            public void setCamY(GameViewPort gameViewPort, float y) {
                if (gameViewPort == null) return;
                gameViewPort.camera.position.y = y;
            }

            public void moveCamY(GameViewPort gameViewPort, float y) {
                if (gameViewPort == null) return;
                gameViewPort.camera.position.y += y;
            }

            public void setCamZ(GameViewPort gameViewPort, float z) {
                if (gameViewPort == null) return;
                gameViewPort.camera.position.z = z;
            }

            public void moveCamZ(GameViewPort gameViewPort, float z) {
                if (gameViewPort == null) return;
                gameViewPort.camera.position.z += z;
            }

            public void setCamZoom(GameViewPort gameViewPort, float zoom) {
                if (gameViewPort == null) return;
                gameViewPort.camera.zoom = zoom;
            }

            public void camZoom(GameViewPort gameViewPort, float zoom) {
                if (gameViewPort == null) return;
                gameViewPort.camera.zoom += zoom;
            }

            public float camX(GameViewPort gameViewPort) {
                if (gameViewPort == null) return 0f;
                return gameViewPort.camera.position.x;
            }

            public float camY(GameViewPort gameViewPort) {
                if (gameViewPort == null) return 0f;
                return gameViewPort.camera.position.y;
            }

            public float camZ(GameViewPort gameViewPort) {
                if (gameViewPort == null) return 0f;
                return gameViewPort.camera.position.z;
            }

            public float camZoom(GameViewPort gameViewPort) {
                if (gameViewPort == null) return 0f;
                return gameViewPort.camera.zoom;
            }

        }

        public class _ProgressBar {

            public ProgressBar create(int x, int y, int width) {
                return create(x,y,width,0f, true, false);
            }

            public ProgressBar create(int x, int y, int width, float progress) {
                return create(x,y,width,progress,true, false);
            }

            public ProgressBar create(int x, int y, int width, float progress, boolean progressText) {
                return create(x,y,width,progress,progressText, false);
            }

            public ProgressBar create(int x, int y, int width, float progress, boolean progressText, boolean progressText2Decimal) {
                ProgressBar progressBar = new ProgressBar();
                setComponentCommonInitValues(progressBar, x, y, width, 1, inputState.config.component_defaultColor);
                progressBar.progress = Tools.Calc.inBounds(progress, 0f, 1f);
                progressBar.progressText = progressText;
                progressBar.progressText2Decimal = progressText2Decimal;
                progressBar.font = inputState.config.component_defaultFont;
                return progressBar;
            }

            public void setFont(ProgressBar progressBar, CMediaFont font) {
                if (progressBar == null) return;
                progressBar.font = font;
            }

            public void setProgress(ProgressBar progressBar, float progress) {
                if (progressBar == null) return;
                UICommons.progressbar_setProgress(progressBar, progress);
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
                Shape shape = new Shape();
                setComponentCommonInitValues(shape, x, y, width, height, inputState.config.component_defaultColor);
                shape.shapeType = shapeType;
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

            private void setButtonCommonInitValues(Button button, ButtonAction buttonAction, ButtonMode buttonMode, int contentOffsetX, int contentOffsetY, boolean togglePressed) {
                button.buttonAction = buttonAction;
                button.mode = buttonMode;
                button.offset_content_x = contentOffsetX;
                button.offset_content_y = contentOffsetY;
                button.pressed = button.mode == ButtonMode.TOGGLE ? togglePressed : false;
                button.toggleDisabled = false;
            }

            private ButtonAction defaultButtonAction() {
                return new ButtonAction() {
                };
            }

            public void setButtonAction(Button button, ButtonAction buttonAction) {
                if (button == null) return;
                button.buttonAction = buttonAction;
            }

            public void press(Button button) {
                if (button == null) return;
                UICommons.button_press(button);
            }

            public void press(Button[] buttons, boolean pressed) {
                if (buttons == null) return;
                for (int i = 0; i < buttons.length; i++) press(buttons[i]);
            }

            public void release(Button button) {
                if (button == null) return;
                UICommons.button_release(button);
            }

            public void release(Button[] buttons, boolean pressed) {
                if (buttons == null) return;
                for (int i = 0; i < buttons.length; i++) release(buttons[i]);
            }

            public void toggle(Button button) {
                if (button == null) return;
                UICommons.button_toggle(button);
            }

            public void toggle(Button[] buttons) {
                if (button == null) return;
                for (int i = 0; i < buttons.length; i++) toggle(buttons[i]);
            }

            public void toggle(Button button, boolean pressed) {
                if (button == null) return;
                UICommons.button_toggle(button, pressed);
            }

            public void toggle(Button[] buttons, boolean pressed) {
                if (buttons == null) return;
                for (int i = 0; i < buttons.length; i++) toggle(buttons[i], pressed);
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

            public void setToggleDisabled(Button button, boolean disabled){
                button.toggleDisabled = disabled;
            }

            public void setOffsetContent(Button[] buttons, int x, int y) {
                if (buttons == null) return;
                for (int i = 0; i < buttons.length; i++) setOffsetContent(buttons[i], x, y);
            }


            public void centerContent(Button[] buttons) {
                if (buttons == null) return;
                for (int i = 0; i < buttons.length; i++) centerContent(buttons[i]);
            }

            public void disableAndRemoveAction(Button button) {
                setDisabled(button, true);
                setButtonAction(button, null);
            }

            public void disableAndRemoveAction(Button[] buttons) {
                if (buttons == null) return;
                for (int i = 0; i < buttons.length; i++) disableAndRemoveAction(buttons[i]);
            }

            public void centerContent(Button button) {
                UICommons.button_centerContent(mediaManager, button);
            }

            public class _TextButton {
                public TextButton create(int x, int y, int width, int height, String text) {
                    return create(x, y, width, height, text, defaultButtonAction(), null,0, ButtonMode.DEFAULT, 0, 0, false);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction) {
                    return create(x, y, width, height, text, buttonAction, null,0, ButtonMode.DEFAULT, 0, 0, false);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon, int iconIndex) {
                    return create(x, y, width, height, text, buttonAction, icon,iconIndex, ButtonMode.DEFAULT, 0, 0, false);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon, int iconIndex, ButtonMode buttonMode) {
                    return create(x, y, width, height, text, buttonAction, icon,iconIndex, buttonMode, 0, 0, false);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon, int iconIndex, ButtonMode buttonMode, int contentOffsetX, int contentOffsetY) {
                    return create(x, y, width, height, text, buttonAction, icon,iconIndex, buttonMode, contentOffsetX, contentOffsetY, false);
                }

                public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaGFX icon, int iconIndex, ButtonMode buttonMode, int contentOffsetX, int contentOffsetY, boolean togglePressed) {
                    TextButton textButton = new TextButton();
                    setComponentCommonInitValues(textButton, x, y, width, height);
                    setButtonCommonInitValues(textButton, buttonAction, buttonMode, contentOffsetX, contentOffsetY, togglePressed);
                    textButton.text = Tools.Text.validString(text);
                    textButton.font = inputState.config.component_defaultFont;
                    textButton.icon = icon;
                    textButton.iconIndex = iconIndex;
                    UICommons.button_centerContent(mediaManager, textButton);
                    return textButton;
                }

                public void setIcon(TextButton textButton, CMediaGFX icon) {
                    if (textButton == null) return;
                    textButton.icon = icon;
                }

                public void setIconIndex(TextButton textButton, int iconIndex) {
                    if (textButton == null) return;
                    textButton.iconIndex = Tools.Calc.lowerBounds(iconIndex, 0);
                }


                public void setText(TextButton textButton, String text) {
                    if (textButton == null) return;
                    textButton.text = Tools.Text.validString(text);
                }

                public void setFont(TextButton textButton, CMediaFont font) {
                    if (textButton == null) return;
                    textButton.font = font;
                }

            }

            public class _ImageButton {

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image) {
                    return create(x, y, width, height, image, 0, defaultButtonAction(), ButtonMode.DEFAULT, 0, 0, false);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex) {
                    return create(x, y, width, height, image, arrayIndex, defaultButtonAction(), ButtonMode.DEFAULT, 0, 0, false);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex, ButtonAction buttonAction) {
                    return create(x, y, width, height, image, arrayIndex, buttonAction, ButtonMode.DEFAULT, 0, 0, false);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex, ButtonAction buttonAction, ButtonMode buttonMode) {
                    return create(x, y, width, height, image, arrayIndex, buttonAction, buttonMode, 0, 0, false);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex, ButtonAction buttonAction, ButtonMode buttonMode, int contentOffsetX, int contentOffsetY) {
                    return create(x, y, width, height, image, arrayIndex, buttonAction, buttonMode, contentOffsetX, contentOffsetY, false);
                }

                public ImageButton create(int x, int y, int width, int height, CMediaGFX image, int arrayIndex, ButtonAction buttonAction, ButtonMode buttonMode, int contentOffsetX, int contentOffsetY, boolean togglePressed) {
                    ImageButton imageButton = new ImageButton();
                    setComponentCommonInitValues(imageButton, x, y, width, height, inputState.config.component_defaultColor, Color.WHITE);
                    setButtonCommonInitValues(imageButton, buttonAction, buttonMode, contentOffsetX, contentOffsetY, togglePressed);
                    imageButton.image = image;
                    imageButton.arrayIndex = arrayIndex;
                    UICommons.button_centerContent(mediaManager, imageButton);
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
                return create(x, y, text, CheckBoxStyle.CHECKBOX, null, false);
            }

            public CheckBox create(int x, int y, String text, CheckBoxStyle checkBoxStyle) {
                return create(x, y, text, checkBoxStyle, null, false);
            }

            public CheckBox create(int x, int y, String text, CheckBoxStyle checkBoxStyle, CheckBoxAction checkBoxAction) {
                return create(x, y, text, checkBoxStyle, checkBoxAction, false);
            }

            public CheckBox create(int x, int y, String text, CheckBoxStyle checkBoxStyle, CheckBoxAction checkBoxAction, boolean checked) {
                CheckBox checkBox = new CheckBox();
                setComponentCommonInitValues(checkBox, x, y, 1, 1, Color.WHITE);
                checkBox.text = Tools.Text.validString(text);
                checkBox.checkBoxStyle = checkBoxStyle;
                checkBox.checkBoxAction = checkBoxAction;
                checkBox.font = inputState.config.component_defaultFont;
                checkBox.checked = checked;
                return checkBox;
            }

            public void setText(CheckBox checkBox, String text) {
                if (checkBox == null) return;
                checkBox.text = Tools.Text.validString(text);
            }

            public void setFont(CheckBox checkBox, CMediaFont font) {
                if (checkBox == null) return;
                checkBox.font = font;
            }

            public void setChecked(CheckBox checkBox, boolean checked) {
                if (checkBox == null) return;
                if (checked) {
                    UICommons.checkbox_check(checkBox);
                } else {
                    UICommons.checkbox_unCheck(checkBox);
                }
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
                setComponentCommonInitValues(tabBar, x, y, width, (bigIconMode ? 2 : 1));
                tabBar.tabBarAction = tabBarAction;
                tabBar.border = border;
                tabBar.borderHeight = Tools.Calc.lowerBounds(borderHeight, 0);
                tabBar.tabOffset = Tools.Calc.lowerBounds(tabOffset, 0);
                tabBar.bigIconMode = bigIconMode;
                tabBar.tabs = new ArrayList<>();
                if (tabs != null) {
                    for (int i = 0; i < tabs.length; i++) {
                        if (tabs[i].addedToTabBar == null) {
                            tabBar.tabs.add(tabs[i]);
                            tabs[i].addedToTabBar = tabBar;
                        }
                    }
                }
                tabBar.selectedTab = Tools.Calc.inBounds(selectedTab, 0, tabBar.tabs.size() - 1);
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
                UICommons.tabBar_selectTab(tabBar, tab);
            }

            public void addTab(TabBar tabBar, Tab tab) {
                if (tabBar == null || tab == null) return;
                UICommons.tabBar_addTab(tabBar, tab);
            }

            public void addTab(TabBar tabBar, Tab tab, int index) {
                if (tabBar == null || tab == null) return;
                UICommons.tabBar_addTab(tabBar, tab, index);
            }

            public void addTabs(TabBar tabBar, Tab[] tabs) {
                if (tabBar == null || tabs == null) return;
                for (int i = 0; i < tabs.length; i++) addTab(tabBar, tabs[i]);
            }

            public void removeTab(TabBar tabBar, Tab tab) {
                if (tabBar == null || tab == null) return;
                UICommons.tabBar_removeTab(tabBar, tab);
            }

            public void removeTabs(TabBar tabBar, Tab[] tabs) {
                if (tabBar == null || tabs == null) return;
                for (int i = 0; i < tabs.length; i++) removeTab(tabBar, tabs[i]);
            }

            public void removeAllTabs(TabBar tabBar) {
                if (tabBar == null) return;
                removeTabs(tabBar, tabBar.tabs.toArray(new Tab[]{}));
            }

            public ArrayList<Tab> findTabsByName(TabBar tabBar, String name) {
                if (tabBar == null || name == null) return new ArrayList<>();
                ArrayList<Tab> result = new ArrayList<>();
                for (int i = 0; i < tabBar.tabs.size(); i++)
                    if (name.equals(tabBar.tabs.get(i).name)) result.add(tabBar.tabs.get(i));
                return result;
            }

            public Tab findTabByName(TabBar tabBar, String name) {
                if (tabBar == null || name == null) return null;
                ArrayList<Tab> result = findTabsByName(tabBar, name);
                return result.size() > 0 ? result.getFirst() : null;
            }

            public boolean isTabVisible(TabBar tabBar, Tab tab) {
                if (tabBar == null || tab == null) return false;
                int xOffset = 0;
                for (int i = 0; i < tabBar.tabs.size(); i++) {
                    xOffset += tabBar.tabs.get(i).width;
                    if (tabBar.tabs.get(i) == tab) return xOffset <= tabBar.width;
                }
                return false;
            }

            public int getTabsWidth(TabBar tabBar) {
                if (tabBar == null) return 0;
                int width = 0;
                for (int i = 0; i < tabBar.tabs.size(); i++) width += tabBar.tabs.get(i).width;
                return width;
            }

            public class _Tab {

                private TabAction defaultTabAction() {
                    return new TabAction() {
                    };
                }

                public Tab create(String title) {
                    return create(title, null, 0, null, defaultTabAction(), 0);
                }

                public Tab create(String title, CMediaGFX icon, int iconIndex) {
                    return create(title, icon, iconIndex, null, defaultTabAction(), 0);
                }


                public Tab create(String title, CMediaGFX icon, int iconIndex, Component[] components) {
                    return create(title, icon, iconIndex, components, defaultTabAction(), 0);
                }

                public Tab create(String title, CMediaGFX icon, int iconIndex, Component[] components, TabAction tabAction) {
                    return create(title, icon, iconIndex, components, tabAction, 0);
                }

                public Tab create(String title, CMediaGFX icon, int iconIndex, Component[] components, TabAction tabAction, int width) {
                    Tab tab = new Tab();
                    tab.title = Tools.Text.validString(title);
                    tab.tabAction = tabAction;
                    tab.icon = icon;
                    tab.iconIndex = iconIndex;
                    tab.font = inputState.config.component_defaultFont;
                    tab.content_offset_x = 0;
                    tab.name = "";
                    tab.data = null;
                    if (width == 0) {
                        tab.width = MathUtils.round((mediaManager.textWidth(tab.font, tab.title) + (tab.icon != null ? UIEngine.TILE_SIZE : 0) + UIEngine.TILE_SIZE) / (float) UIEngine.TILE_SIZE);
                    } else {
                        tab.width = width;
                    }
                    tab.components = new ArrayList<>();
                    if (components != null) {
                        for (int i = 0; i < components.length; i++) {
                            if (components[i].addedToTab == null) {
                                tab.components.add(components[i]);
                                components[i].addedToTab = tab;
                            }
                        }
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
                    for (int i = 0; i < components.length; i++) addTabComponent(tab, components[i]);
                }

                public void addTabComponent(Tab tab, Component component) {
                    if (tab == null || component == null) return;
                    UICommons.tab_addComponent(tab, component);
                }

                public void addTabComponents(Tab tab, Component[] components) {
                    if (tab == null || components == null) return;
                    for (int i = 0; i < components.length; i++) addTabComponent(tab, components[i]);
                }

                public void removeTabComponent(Tab tab, Component component) {
                    if (tab == null || component == null) return;
                    UICommons.tab_removeComponent(tab, component);
                }

                public void removeTabComponents(Tab tab, Component[] components) {
                    if (tab == null || components == null) return;
                    for (int i = 0; i < components.length; i++) removeTabComponent(tab, components[i]);
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
                    tab.font = font;
                }

                public void setTabAction(Tab tab, TabAction tabAction) {
                    if (tab == null) return;
                    tab.tabAction = tabAction;
                }

                public void setWidth(Tab tab, int width) {
                    if (tab == null) return;
                    tab.width = Tools.Calc.lowerBounds(width, 1);
                }

            }
        }

        public class _Grid {

            private GridAction defaultGridAction() {
                return new GridAction() {
                };
            }

            public Grid create(int x, int y, Object[][] items) {
                return create(x, y, items, defaultGridAction(), false, false, false, false);
            }

            public Grid create(int x, int y, Object[][] items, GridAction gridAction) {
                return create(x, y, items, gridAction, false, false, false, false);
            }

            public Grid create(int x, int y, Object[][] items, GridAction gridAction, boolean dragEnabled) {
                return create(x, y, items, gridAction, dragEnabled, false, false, false);
            }

            public Grid create(int x, int y, Object[][] items, GridAction gridAction, boolean dragEnabled, boolean dragOutEnabled) {
                return create(x, y, items, gridAction, dragEnabled, dragOutEnabled, false, false);
            }

            public Grid create(int x, int y, Object[][] items, GridAction gridAction, boolean dragEnabled, boolean dragOutEnabled, boolean dragInEnabled) {
                return create(x, y, items, gridAction, dragEnabled, dragOutEnabled, dragInEnabled, false);
            }

            public Grid create(int x, int y, Object[][] items, GridAction gridAction, boolean dragEnabled, boolean dragOutEnabled, boolean dragInEnabled, boolean doubleSized) {
                Grid grid = new Grid();
                int width = 1;
                int height = 1;
                if (items != null) {
                    width = items.length * (doubleSized ? 2 : 1);
                    height = items[0].length * (doubleSized ? 2 : 1);
                }
                setComponentCommonInitValues(grid, x, y, width, height);
                grid.items = items;
                grid.gridAction = gridAction;
                grid.dragEnabled = dragEnabled;
                grid.dragInEnabled = dragInEnabled;
                grid.dragOutEnabled = dragOutEnabled;
                grid.doubleSized = doubleSized;
                return grid;
            }

            public void setDoubleSized(Grid grid, boolean doubleSized) {
                grid.doubleSized = doubleSized;
                UICommons.grid_updateSize(grid);
            }

            public boolean isPositionValid(Grid grid, int x, int y) {
                if (grid == null) return false;
                return UICommons.grid_positionValid(grid, x, y);
            }

            public void setDragInEnabled(Grid grid, boolean dragInEnabled) {
                if (grid == null) return;
                grid.dragInEnabled = dragInEnabled;
            }

            public void setDragOutEnabled(Grid grid, boolean dragOutEnabled) {
                if (grid == null) return;
                grid.dragOutEnabled = dragOutEnabled;
            }

            public void setDragEnabled(Grid grid, boolean dragEnabled) {
                if (grid == null) return;
                grid.dragEnabled = dragEnabled;
            }

            public void setGridAction(Grid grid, GridAction gridAction) {
                if (grid == null) return;
                grid.gridAction = gridAction;
            }

            public void setItems(Grid grid, Object[][] items) {
                if (grid == null || items == null) return;
                UICommons.grid_setItems(grid, items);
            }


        }

        public class _TextField {

            private TextFieldAction defaultTextFieldAction() {
                return new TextFieldAction() {
                };
            }

            public TextField create(int x, int y, int width) {
                return create(x, y, width, "", defaultTextFieldAction(), 32,
                        inputState.config.component_textFieldDefaultAllowedCharacters);
            }


            public TextField create(int x, int y, int width, String content) {
                return create(x, y, width, content, defaultTextFieldAction(), 32,
                        inputState.config.component_textFieldDefaultAllowedCharacters);
            }


            public TextField create(int x, int y, int width, String content, TextFieldAction textFieldAction) {
                return create(x, y, width, content, textFieldAction, 32,
                        inputState.config.component_textFieldDefaultAllowedCharacters);
            }

            public TextField create(int x, int y, int width, String content, TextFieldAction textFieldAction, int contentMaxLength) {
                return create(x, y, width, content, textFieldAction, contentMaxLength,
                        inputState.config.component_textFieldDefaultAllowedCharacters);
            }

            public TextField create(int x, int y, int width, String content, TextFieldAction textFieldAction, int contentMaxLength, char[] allowedCharacters) {
                TextField textField = new TextField();
                setComponentCommonInitValues(textField, x, y, width, 1, Color.WHITE);
                textField.font = inputState.config.component_defaultFont;
                textField.allowedCharacters = new IntSet();
                for (int i = 0; i < allowedCharacters.length; i++)
                    textField.allowedCharacters.add(allowedCharacters[i]);
                textField.offset = 0;
                textField.content = Tools.Text.validString(content);
                textField.textFieldAction = textFieldAction;
                textField.markerPosition = 0;
                textField.contentMaxLength = Tools.Calc.lowerBounds(contentMaxLength, 0);
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
                textField.font = font;
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

            public void setAllowedCharacters(TextField textField, char[] allowedCharacters) {
                if (textField == null) return;
                textField.allowedCharacters.clear();
                if (allowedCharacters != null) {
                    for (int i = 0; i < allowedCharacters.length; i++)
                        textField.allowedCharacters.add(allowedCharacters[i]);
                }
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

        public class _Canvas {

            public final _CanvasImage canvasImage = new _CanvasImage();

            private CanvasAction defaultCanvasAction() {
                return new CanvasAction() {
                };
            }

            public Canvas create(int x, int y, int width, int height) {
                return create(x, y, width, height, defaultCanvasAction(), null);
            }

            public Canvas create(int x, int y, int width, int height, CanvasAction canvasAction) {
                return create(x, y, width, height, canvasAction, null);
            }

            public Canvas create(int x, int y, int width, int height, CanvasAction canvasAction, CanvasImage[] canvasImages) {
                Canvas canvas = new Canvas();
                setComponentCommonInitValues(canvas, x, y, width, height, Color.WHITE);
                canvas.map = new Color[width*UIEngine.TILE_SIZE][height*UIEngine.TILE_SIZE];
                for(int ix = 0; ix<canvas.map.length; ix++)
                    for(int iy = 0; iy<canvas.map[0].length; iy++)
                        canvas.map[ix][iy] = new Color(1f,1f,1f,1f);
                canvas.canvasAction = canvasAction;
                canvas.canvasImages = new ArrayList<>();
                if (canvasImages != null) {
                    for (int i = 0; i < canvasImages.length; i++) {
                        if (canvasImages[i].addedToCanvas == null) {
                            canvas.canvasImages.add(canvasImages[i]);
                            canvasImages[i].addedToCanvas = canvas;
                        }
                    }
                }

                return canvas;
            }

            public void setCanvasAction(Canvas canvas, CanvasAction canvasAction) {
                if (canvas == null) return;
                canvas.canvasAction = canvasAction;
            }

            public Color getPoint(Canvas canvas, int x, int y) {
                if (canvas == null) return null;
                Color color = UICommons.canvas_getPoint(canvas,x,y);
                return color != null ? new Color(color) : null;
            }

            public void setAllPoints(Canvas canvas, Color color) {
                setAllPoints(canvas, color.r, color.g, color.b, color.a);
            }

            public void setAllPoints(Canvas canvas, float r, float g, float b, float a) {
                if (canvas == null) return;
                UICommons.canvas_setAllPoints(canvas, r, g, b, a);
            }

            public void setPoint(Canvas canvas, int x, int y, Color color) {
                setPoint(canvas, x, y, color.r, color.g, color.b, color.a);
            }

            public void setPoint(Canvas canvas, int x, int y, float r, float g, float b, float a) {
                if (canvas == null) return;
                UICommons.canvas_setPoint(canvas,x,y,r,g,b,a);
            }

            public void addCanvasImage(Canvas canvas, CanvasImage canvasImage) {
                if (canvas == null || canvasImage == null) return;
                UICommons.canvas_addCanvasImage(canvas, canvasImage);
            }

            public void addCanvasImages(Canvas canvas, CanvasImage[] canvasImages) {
                if (canvas == null || canvasImages == null) return;
                for (int i = 0; i < canvasImages.length; i++) addCanvasImage(canvas, canvasImages[i]);
            }

            public void removeCanvasImage(Canvas canvas, CanvasImage canvasImage) {
                if (canvas == null || canvasImage == null) return;
                UICommons.canvas_removeCanvasImage(canvas, canvasImage);
            }

            public void removeCanvasImages(Canvas canvas, CanvasImage[] canvasImages) {
                if (canvas == null || canvasImages == null) return;
                for (int i = 0; i < canvasImages.length; i++) removeCanvasImage(canvas, canvasImages[i]);
            }

            public void removeAllMapOverlays(Canvas canvas) {
                if (canvas == null) return;
                removeCanvasImages(canvas, canvas.canvasImages.toArray(new CanvasImage[]{}));
            }

            public ArrayList<CanvasImage> findMapOverlaysByName(Canvas canvas, String name) {
                if (canvas == null || name == null) return new ArrayList<>();
                ArrayList<CanvasImage> result = new ArrayList<>();
                for (int i = 0; i < canvas.canvasImages.size(); i++)
                    if (name.equals(canvas.canvasImages.get(i).name)) result.add(canvas.canvasImages.get(i));
                return result;
            }

            public CanvasImage findMapOverlayByName(Canvas canvas, String name) {
                if (canvas == null || name == null) return null;
                ArrayList<CanvasImage> result = findMapOverlaysByName(canvas, name);
                return result.size() > 0 ? result.getFirst() : null;
            }

            public class _CanvasImage {
                public CanvasImage create(CMediaGFX image, int x, int y) {
                    return create(image, x, y,  0, false, inputState.config.component_mapOverlayDefaultFadeoutTime);
                }

                public CanvasImage create(CMediaGFX image, int x, int y, int arrayIndex) {
                    return create(image, x, y, arrayIndex, false, inputState.config.component_mapOverlayDefaultFadeoutTime);
                }

                public CanvasImage create(CMediaGFX image, int x, int y, int arrayIndex, boolean fadeOut) {
                    return create(image, x, y, arrayIndex, fadeOut, inputState.config.component_mapOverlayDefaultFadeoutTime);
                }

                public CanvasImage create(CMediaGFX image, int x, int y, int arrayIndex, boolean fadeOut, int fadeOutTime) {
                    CanvasImage canvasImage = new CanvasImage();
                    canvasImage.image = image;
                    canvasImage.x = x;
                    canvasImage.y = y;
                    canvasImage.fadeOut = fadeOut;
                    canvasImage.fadeOutTime = fadeOutTime;
                    canvasImage.color_r = Color.WHITE.r;
                    canvasImage.color_g = Color.WHITE.g;
                    canvasImage.color_b = Color.WHITE.b;
                    canvasImage.color_a = Color.WHITE.a;
                    canvasImage.arrayIndex = Tools.Calc.lowerBounds(arrayIndex, 0);
                    canvasImage.name = Tools.Text.validString("");
                    canvasImage.data = null;
                    canvasImage.timer = fadeOut ? System.currentTimeMillis() : 0;
                    canvasImage.addedToCanvas = null;
                    return canvasImage;
                }

                public void setFadeOut(CanvasImage canvasImage, boolean fadeOut) {
                    if (canvasImage == null) return;
                    canvasImage.fadeOut = fadeOut;
                }

                public void setFadeOutTime(CanvasImage canvasImage, int fadeoutTime) {
                    if (canvasImage == null) return;
                    canvasImage.fadeOutTime = Tools.Calc.lowerBounds(fadeoutTime, 0);
                }

                public void setPosition(CanvasImage canvasImage, int x, int y) {
                    if (canvasImage == null) return;
                    canvasImage.x = x;
                    canvasImage.y = y;
                }

                public void setImage(CanvasImage canvasImage, CMediaGFX image) {
                    if (canvasImage == null) return;
                    canvasImage.image = image;
                }

                public void setColor(CanvasImage canvasImage, Color color) {
                    setColor(canvasImage, color.r, color.b, color.g, color.a);
                }

                public void setColor(CanvasImage canvasImage, float r, float g, float b, float a) {
                    if (canvasImage == null) return;
                    canvasImage.color_r = r;
                    canvasImage.color_g = g;
                    canvasImage.color_b = b;
                    canvasImage.color_a = a;
                }

                public void setArrayIndex(CanvasImage canvasImage, int arrayIndex) {
                    if (canvasImage == null) return;
                    canvasImage.arrayIndex = Tools.Calc.lowerBounds(arrayIndex, 0);
                }

                public void setName(CanvasImage canvasImage, String name) {
                    if (canvasImage == null) return;
                    canvasImage.name = Tools.Text.validString(name);
                }

                public void setData(CanvasImage canvasImage, Object data) {
                    if (canvasImage == null) return;
                    canvasImage.data = data;
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
                setComponentCommonInitValues(knob, x, y, 2, 2, inputState.config.component_defaultColor, Color.BLACK);
                knob.endless = endless;
                knob.turned = Tools.Calc.inBounds(turned, 0f, 1f);
                knob.knobAction = knobAction;
                return knob;
            }

            public void setTurned(Knob knob, float turned) {
                if (knob == null) return;
                UICommons.knob_turnKnob(knob, turned);
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
                return create(x, y, lines , defaultTextAction());
            }

            public Text create(int x, int y, String[] lines, TextAction textAction) {
                Text text = new Text();
                text.font = inputState.config.component_defaultFont;
                int width = 1;
                int height = 1;
                if (lines != null && text.font != null) {
                    for (int i = 0; i < lines.length; i++) {
                        int widthT = mediaManager.textWidth(text.font, lines[i]);
                        if (widthT > width) width = widthT;
                    }
                    width = width / UIEngine.TILE_SIZE;
                    height = lines.length;
                }
                setComponentCommonInitValues(text, x, y, width, height);
                text.textAction = textAction;
                text.lines = Tools.Text.validStringArrayCopy(lines);
                return text;
            }

            public void setTextAction(Text text, TextAction textAction) {
                if (text == null) return;
                text.textAction = textAction;
            }

            public void setLines(Text text, String... lines) {
                if (text == null) return;
                UICommons.text_setLines(mediaManager, text, lines);
            }

            public void setFont(Text text, CMediaFont font) {
                if (text == null) return;
                text.font = font;
            }

        }

        public class _Image {

            private ImageAction defaultImageAction() {
                return new ImageAction() {
                };
            }

            public Image create(int x, int y, CMediaGFX image) {
                return create(x, y, image, 0, defaultImageAction(), 0f);
            }

            public Image create(int x, int y, CMediaGFX image, int arrayIndex) {
                return create(x, y, image, arrayIndex,defaultImageAction(), 0f);
            }

            public Image create(int x, int y, CMediaGFX image, int arrayIndex, float animation_offset) {
                return create(x, y, image, arrayIndex, defaultImageAction(), animation_offset);
            }

            public Image create(int x, int y, CMediaGFX image,int arrayIndex, ImageAction imageAction, float animation_offset) {
                Image imageC = new Image();
                int width = image != null ? mediaManager.imageWidth(image) / UIEngine.TILE_SIZE : 0;
                int height = image != null ? mediaManager.imageHeight(image) / UIEngine.TILE_SIZE : 0;
                setComponentCommonInitValues(imageC, x, y, width, height, Color.WHITE);
                imageC.image = image;
                imageC.arrayIndex = Tools.Calc.lowerBounds(arrayIndex, 0);
                imageC.animationOffset = animation_offset;
                imageC.imageAction = imageAction;
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
                UICommons.image_setImage(mediaManager, imageC, image);
            }

        }

        public class _ComboBox {

            public final _ComboBox._ComboBoxItem item = new _ComboBox._ComboBoxItem();

            private ComboBoxAction defaultComboBoxAction() {
                return new ComboBoxAction() {
                };
            }

            public ComboBox create(int x, int y, int width) {
                return create(x, y, width, null, defaultComboBoxAction(), false, null);
            }

            public ComboBox create(int x, int y, int width, ComboBoxItem[] combobBoxItems) {
                return create(x, y, width, combobBoxItems,defaultComboBoxAction() , false, null);
            }

            public ComboBox create(int x, int y, int width, ComboBoxItem[] combobBoxItems, ComboBoxAction comboBoxAction) {
                return create(x, y, width, combobBoxItems, comboBoxAction, false, null);
            }

            public ComboBox create(int x, int y, int width, ComboBoxItem[] combobBoxItems, ComboBoxAction comboBoxAction, boolean useIcons) {
                return create(x, y, width, combobBoxItems, comboBoxAction, useIcons, null);
            }

            public ComboBox create(int x, int y, int width, ComboBoxItem[] combobBoxItems, ComboBoxAction comboBoxAction, boolean useIcons, ComboBoxItem selectedItem) {
                ComboBox comboBox = new ComboBox();
                setComponentCommonInitValues(comboBox, x, y, width, 1);
                comboBox.useIcons = useIcons;
                comboBox.comboBoxAction = comboBoxAction;
                comboBox.comboBoxItems = new ArrayList<>();
                if (combobBoxItems != null) {
                    for (int i = 0; i < combobBoxItems.length; i++) {
                        if (combobBoxItems[i].addedToComboBox == null) {
                            comboBox.comboBoxItems.add(combobBoxItems[i]);
                            combobBoxItems[i].addedToComboBox = comboBox;
                        }
                    }
                }
                comboBox.selectedItem = selectedItem != null && selectedItem.addedToComboBox == comboBox ? selectedItem : null;
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
                for (int i = 0; i < comboBoxItems.length; i++) addComboBoxItem(comboBox, comboBoxItems[i]);
            }

            public void removeComboBoxItem(ComboBox comboBox, ComboBoxItem comboBoxItem) {
                if (comboBox == null || comboBoxItem == null) return;
                UICommons.comboBox_removeItem(comboBox, comboBoxItem);
            }

            public void removeComboBoxItems(ComboBox comboBox, ComboBoxItem[] comboBoxItems) {
                if (comboBox == null || comboBoxItems == null) return;
                for (int i = 0; i < comboBoxItems.length; i++) removeComboBoxItem(comboBox, comboBoxItems[i]);
            }

            public void removeAllComboBoxItems(ComboBox comboBox) {
                if (comboBox == null) return;
                removeComboBoxItems(comboBox, comboBox.comboBoxItems.toArray(new ComboBoxItem[]{}));
            }

            public boolean isItemSelected(ComboBox comboBox, ComboBoxItem comboBoxItem) {
                if (comboBox == null || comboBoxItem == null) return false;
                return comboBox.selectedItem != null ? comboBox.selectedItem == comboBoxItem : false;
            }

            public void selectItem(ComboBoxItem selectItem) {
                if (selectItem == null) return;
                UICommons.comboBox_selectItem(inputState, selectItem);
            }

            public void open(ComboBox comboBox) {
                if (comboBox == null) return;
                UICommons.comboBox_open(inputState, comboBox);
            }

            public void close(ComboBox comboBox) {
                if (comboBox == null) return;
                UICommons.comboBox_close(inputState, comboBox);
            }

            public boolean isOpen(ComboBox comboBox) {
                return UICommons.comboBox_isOpen(inputState, comboBox);
            }

            public void setSelectedItemByText(ComboBox comboBox, String text) {
                if (comboBox == null || text == null) return;
                for (int i = 0; i < comboBox.comboBoxItems.size(); i++) {
                    if (comboBox.comboBoxItems.get(i).text.equals(text)) {
                        UICommons.comboBox_selectItem(inputState, comboBox.comboBoxItems.get(i));
                        return;
                    }
                }
            }

            public boolean isSelectedItemText(ComboBox comboBox, String text) {
                if (comboBox == null || text == null) return false;
                return comboBox.selectedItem != null ? comboBox.selectedItem.text.equals(text) : false;
            }

            public class _ComboBoxItem {

                private ComboBoxItemAction defaultComboBoxItemAction() {
                    return new ComboBoxItemAction() {
                    };
                }

                public ComboBoxItem create(String text) {
                    return create(text, defaultComboBoxItemAction(), null, 0);
                }

                public ComboBoxItem create(String text, ComboBoxItemAction comboBoxItemAction) {
                    return create(text, comboBoxItemAction, null, 0);
                }

                public ComboBoxItem create(String text, ComboBoxItemAction comboBoxItemAction, CMediaGFX icon, int iconIndex) {
                    ComboBoxItem comboBoxItem = new ComboBoxItem();
                    comboBoxItem.text = Tools.Text.validString(text);
                    comboBoxItem.font = inputState.config.component_defaultFont;
                    comboBoxItem.color_r = inputState.config.component_defaultColor.r;
                    comboBoxItem.color_g = inputState.config.component_defaultColor.g;
                    comboBoxItem.color_b = inputState.config.component_defaultColor.b;
                    comboBoxItem.icon = icon;
                    comboBoxItem.iconIndex = Tools.Calc.lowerBounds(iconIndex, 0);
                    comboBoxItem.comboBoxItemAction = comboBoxItemAction;
                    comboBoxItem.name = "";
                    comboBoxItem.data = null;
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

                public void setColor(ComboBoxItem comboBoxItem, Color color) {
                    if (comboBoxItem == null || color == null) return;
                    comboBoxItem.color_r = color.r;
                    comboBoxItem.color_g = color.g;
                    comboBoxItem.color_b = color.b;
                }

                public void setFont(ComboBoxItem comboBoxItem, CMediaFont font) {
                    if (comboBoxItem == null) return;
                    comboBoxItem.font = font;
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
                UICommons.scrollBar_scroll(scrollBar, scrolled);
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
                    setComponentCommonInitValues(scrollBarHorizontal, x, y, length, 1);
                    scrollBarHorizontal.scrollBarAction = scrollBarAction;
                    scrollBarHorizontal.scrolled = Tools.Calc.inBounds(scrolled, 0f, 1f);
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
                    setComponentCommonInitValues(scrollBarVertical, x, y, 1, length);
                    scrollBarVertical.scrollBarAction = scrollBarAction;
                    scrollBarVertical.scrolled = Tools.Calc.inBounds(scrolled, 0f, 1f);
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
                return create(x, y, width, height, null, defaultListAction(), false, false, false, false);
            }

            public List create(int x, int y, int width, int height, ArrayList items) {
                return create(x, y, width, height, items, defaultListAction(), false, false, false, false);
            }

            public List create(int x, int y, int width, int height, ArrayList items, ListAction listAction) {
                return create(x, y, width, height, items, listAction, false, false, false, false);
            }

            public List create(int x, int y, int width, int height, ArrayList items, ListAction listAction, boolean multiSelect) {
                return create(x, y, width, height, items, listAction, multiSelect, false, false, false);
            }

            public List create(int x, int y, int width, int height, ArrayList items, ListAction listAction, boolean multiSelect, boolean dragEnabled) {
                return create(x, y, width, height, items, listAction, multiSelect, dragEnabled, false, false);
            }

            public List create(int x, int y, int width, int height, ArrayList items, ListAction listAction, boolean multiSelect, boolean dragEnabled, boolean dragOutEnabled) {
                return create(x, y, width, height, items, listAction, multiSelect, dragEnabled, dragOutEnabled, false);
            }


            public List create(int x, int y, int width, int height, ArrayList items, ListAction listAction, boolean multiSelect, boolean dragEnabled, boolean dragOutEnabled, boolean dragInEnabled) {
                List list = new List();
                setComponentCommonInitValues(list, x, y, width, height);
                list.selectedItem = null;
                list.selectedItems = new HashSet<>();
                list.items = items;
                list.listAction = listAction;
                list.multiSelect = multiSelect;
                list.scrolled = 0f;
                list.dragEnabled = dragEnabled;
                list.dragInEnabled = dragInEnabled;
                list.dragOutEnabled = dragOutEnabled;
                list.font = inputState.config.component_defaultFont;
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
                UICommons.list_scroll(list, scrolled);
            }

            public void setListAction(List list, ListAction listAction) {
                if (list == null) return;
                list.listAction = listAction;
            }

            public void setFont(List list, CMediaFont font) {
                if (list == null) return;
                list.font = font;
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
                for (int i = 0; i < selectedItems.length; i++)
                    if (selectedItems[i] != null) list.selectedItems.add(selectedItems[i]);
            }


        }

    }


}
