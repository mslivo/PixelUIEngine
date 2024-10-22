package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.BooleanArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.LongArray;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaImage;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.constants.BUTTON_MODE;
import net.mslivo.core.engine.ui_engine.media.UIEngineBaseMedia_8x8;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.Window;
import net.mslivo.core.engine.ui_engine.ui.actions.*;
import net.mslivo.core.engine.ui_engine.ui.components.Component;
import net.mslivo.core.engine.ui_engine.ui.components.button.Button;
import net.mslivo.core.engine.ui_engine.ui.components.button.ImageButton;
import net.mslivo.core.engine.ui_engine.ui.components.button.TextButton;
import net.mslivo.core.engine.ui_engine.ui.components.canvas.Canvas;
import net.mslivo.core.engine.ui_engine.ui.components.canvas.CanvasImage;
import net.mslivo.core.engine.ui_engine.ui.components.checkbox.Checkbox;
import net.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import net.mslivo.core.engine.ui_engine.ui.components.image.Image;
import net.mslivo.core.engine.ui_engine.ui.components.list.List;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollbarVertical;
import net.mslivo.core.engine.ui_engine.ui.components.tabbar.Tab;
import net.mslivo.core.engine.ui_engine.ui.components.tabbar.Tabbar;
import net.mslivo.core.engine.ui_engine.ui.components.text.Text;
import net.mslivo.core.engine.ui_engine.ui.components.textfield.Textfield;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.Contextmenu;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextmenuItem;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;
import net.mslivo.core.engine.ui_engine.ui.tooltip.TooltipTextSegment;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public final class APIComposites {
    private static final char[] numbersAllowedCharacters = new char[]{'-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final char[] decimalsAllowedCharacters = new char[]{'-', ',', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private final API api;
    private final UIEngineState uiEngineState;
    private final MediaManager mediaManager;
    private final UIConfig uiConfig;

    public final APICompositeList list;
    public final APICompositeGrid grid;
    public final APICompositeImage image;
    public final APICompositeText text;
    public final APICompositeHotkey hotkey;
    public final APICompositeCheckbox checkBox;
    public final APICompositeModal modal;
    public final APICompositeCanvas canvas;
    public final APICompositeButton button;
    public final APICompositeTextfield textfield;
    public final APICompositeTabbar tabBar;

    APIComposites(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.uiConfig = uiEngineState.config;
        this.list = new APICompositeList();
        this.image = new APICompositeImage();
        this.text = new APICompositeText();
        this.hotkey = new APICompositeHotkey();
        this.checkBox = new APICompositeCheckbox();
        this.modal = new APICompositeModal();
        this.canvas = new APICompositeCanvas();
        this.button = new APICompositeButton();
        this.textfield = new APICompositeTextfield();
        this.tabBar = new APICompositeTabbar();
        this.grid = new APICompositeGrid();
    }

    public final class APICompositeGrid {
        APICompositeGrid() {
        }

        private static final String PAGE_TEXT = "%s/%s";

        public Component[] createPageableReadOnlyGrid(int x, int y, int width, int height, ArrayList items, GridAction gridAction){
            return createPageableReadOnlyGrid(x,y,width,height,items,gridAction, false, false);
        }

        public Component[] createPageableReadOnlyGrid(int x, int y, int width, int height, ArrayList items, GridAction gridAction, boolean multiselect, boolean doubleSized){

            int gridHeight = height-1;
            final AtomicInteger currentPage = new AtomicInteger(0);

            ArrayList<Object[][]> pages = new ArrayList<>();

            Grid grid = api.component.grid.create(x, y+1, null, null, multiselect,false, false, false,doubleSized);
            ImageButton backButton = api.component.button.imageButton.create(0,0,1,1, UIEngineBaseMedia_8x8.UI_ICON_BACK);
            Text pageText = api.component.text.create(0,0, new String[]{});
            ImageButton forwardButton = api.component.button.imageButton.create(0,0, 1,1, UIEngineBaseMedia_8x8.UI_ICON_FORWARD);


            api.component.button.setButtonAction(backButton, new ButtonAction() {
                @Override
                public void onRelease() {
                    currentPage.set(Math.max(currentPage.get()-1,0));
                    pageableGridUpdateButtons(x,y, backButton, forwardButton, pageText, currentPage.get(), pages.size()-1);
                    api.component.grid.setItems(grid, pages.get(currentPage.get()));
                }
            });
            api.component.button.setButtonAction(forwardButton, new ButtonAction() {
                @Override
                public void onRelease() {
                    currentPage.set(Math.min(currentPage.get()+1,pages.size()-1));
                    pageableGridUpdateButtons(x,y, backButton, forwardButton, pageText, currentPage.get(), pages.size()-1);
                    api.component.grid.setItems(grid, pages.get(currentPage.get()));
                }
            });

            api.component.addUpdateAction(grid, new UpdateAction(0,true) {
                int itemsSizeLast = -1;
                @Override
                public void onUpdate() {
                    if( itemsSizeLast != items.size()){
                        pageableGridUpdateGridResize(pages, width, gridHeight, items);
                        currentPage.set(Math.clamp(currentPage.get(), 0, pages.size()-1));
                        api.component.grid.setItems(grid, pages.get(currentPage.get()));
                        pageableGridUpdateButtons(x,y, backButton, forwardButton, pageText, currentPage.get(), pages.size()-1);
                        itemsSizeLast = items.size();
                    }

                }
            });

            pageableReadOnlyGridSetGridAction(grid, gridAction);

            return new Component[]{
                    grid, backButton, pageText, forwardButton
            };
        }

        public void pageableReadOnlyGridSetGridAction(Grid grid, GridAction gridAction){
            if(gridAction != null){
                grid.gridAction = new GridAction() {
                    @Override
                    public boolean canDragFromGrid(Grid fromGrid) {
                        return false;
                    }

                    @Override
                    public boolean canDragFromList(List fromList) {
                        return false;
                    }

                    @Override
                    public boolean canDragIntoApp() {
                        return false;
                    }

                    @Override
                    public Color cellColor(Object listItem) {
                        return gridAction.cellColor(listItem);
                    }

                    @Override
                    public CMediaSprite icon(Object listItem) {
                        return gridAction.icon(listItem);
                    }

                    @Override
                    public int iconIndex(Object listItem) {
                        return gridAction.iconIndex(listItem);
                    }

                    @Override
                    public void onDragFromGrid(Grid fromGrid, int from_x, int from_y, int to_x, int to_y) {
                        return;
                    }

                    @Override
                    public void onDragFromList(List fromList, int fromIndex, int to_x, int to_y) {
                        return;
                    }

                    @Override
                    public void onDragIntoApp(Object listItem, int from_x, int from_y, int screenX, int screenY) {
                        return;
                    }

                    @Override
                    public boolean onItemSelected(Object listItem) {
                        return gridAction.onItemSelected(listItem);
                    }

                    @Override
                    public Tooltip toolTip(Object listItem) {
                        return gridAction.toolTip(listItem);
                    }
                };
            }else{
                grid.gridAction = null;
                return;
            }
        }

        private void pageableGridUpdateButtons(int x, int y, ImageButton backButton, ImageButton forwardButton, Text text, int currentPage, int pagesMax){
            String pageStringMax = String.format(PAGE_TEXT, pagesMax+1,pagesMax+1);
            String pageString = String.format(PAGE_TEXT, currentPage+1,pagesMax+1);

            api.component.setPositionGrid(backButton, x, y);
            api.component.setPositionGrid(text, x+1, y);
            int textWidthTiles = MathUtils.ceil((mediaManager.getCMediaFontTextWidth(api.config.ui.getFont(), pageStringMax)+1)/api.TSF());
            api.component.setPositionGrid(forwardButton, (x+1)+textWidthTiles, y);
            api.component.text.setLines(text, Tools.Text.toArray(pageString));
            return;
        }

        private void pageableGridUpdateGridResize(ArrayList<Object[][]> pages, int width, int height, ArrayList items){
            int pageSize = width*height;
            int pagesCount = MathUtils.floor(items.size()/(float)pageSize);
            if(items.size()%pageSize > 0 || items.size() == 0) pagesCount++;

            pages.clear();
            for(int i=0;i<pagesCount;i++){
                Object[][] page = new Object[width][height];
                pages.add(page);
            }

            int pageIndex = 0;
            int ix = 0;
            int iy = height-1;
            for(int i=0;i<items.size();i++){
                Object[][] page = pages.get(pageIndex);

                page[ix][iy] = items.get(i);
                ix++;
                if(ix > width-1) {
                    ix = 0;
                    iy--;
                    if(iy < 0){
                        iy = height-1;
                        pageIndex++;
                    }
                }
            }
        }
    }

    public final class APICompositeList {
        APICompositeList() {
        }

        public Textfield createSearchBar(List list) {
            return createSearchBar(list, null, false, false);
        }

        public Textfield createSearchBar(List list, ScrollbarVertical scrollBarVertical) {
            return createSearchBar(list, scrollBarVertical, false, false);
        }

        public Textfield createSearchBar(List list, ScrollbarVertical scrollBarVertical, boolean searchTooltips, boolean searchArrayLists) {
            if (list == null) return null;
            ArrayList originalList = list.items;
            ArrayList itemsSearched = new ArrayList(list.items);
            api.component.setSize(list, list.width, list.height - 1);
            api.component.setPosition(list, list.x, list.y + 1);
            if (scrollBarVertical != null) {
                api.component.setSize(scrollBarVertical, scrollBarVertical.width, scrollBarVertical.height - 1);
                api.component.setPosition(scrollBarVertical, scrollBarVertical.x, scrollBarVertical.y + 1);
            }
            Textfield textField = api.component.textfield.create(list.x, list.y - 1, list.width + 1, "");
            api.component.textfield.setTextFieldAction(textField, new TextFieldAction() {


                @Override
                public void onContentChange(String searchText, boolean valid) {
                    if (valid) {

                        if (searchText.trim().isEmpty()) {
                            api.component.list.setItems(list, originalList);
                        } else {
                            itemsSearched.clear();
                            searchBarSearchItemsInternal(list, originalList, itemsSearched, searchText, searchTooltips, searchArrayLists);
                            api.component.list.setItems(list, itemsSearched);
                        }


                    }
                }
            });

            return textField;
        }

        public ScrollbarVertical createScrollBar(List list) {
            ScrollbarVertical scrollBarVertical = api.component.scrollbar.scrollbarVertical.create(0, 0, list.height, new ScrollBarAction() {
                @Override
                public void onScrolled(float scrolled) {
                    api.component.list.setScrolled(list, 1f - scrolled);
                }
            });
            api.component.setPosition(scrollBarVertical, list.x + (list.width * api.TS()), list.y);

            api.component.addUpdateAction(scrollBarVertical, new UpdateAction() {
                float scrolledLast = -1;

                @Override
                public void onUpdate() {
                    if (scrolledLast != list.scrolled) {
                        api.component.scrollbar.setScrolled(scrollBarVertical, 1 - list.scrolled);
                        scrolledLast = list.scrolled;
                    }
                    // disable scrollbar
                    if (list.items != null && list.items.size() <= list.height) {
                        api.component.setDisabled(scrollBarVertical, true);
                        api.component.scrollbar.setScrolled(scrollBarVertical, 1f);
                    } else {
                        api.component.setDisabled(scrollBarVertical, false);
                    }
                }
            });
            return scrollBarVertical;
        }


        private void searchBarSearchItemsInternal(List list, ArrayList searchList, ArrayList resultList, String searchText, boolean searchTooltips, boolean searchArrayLists) {
            for (int i = 0; i < searchList.size(); i++) {
                Object item = searchList.get(i);
                if (searchArrayLists && item instanceof ArrayList itemList) {
                    searchBarSearchItemsInternal(list, itemList, resultList, searchText, searchTooltips, searchArrayLists);
                } else if (list.listAction != null && list.listAction.text(item).trim().toLowerCase().contains(searchText.trim().toLowerCase())) {
                    resultList.add(item);
                } else if (searchTooltips) {
                    Tooltip tooltip = list.listAction.toolTip(item);
                    if (tooltip != null) {
                        linesLoop:
                        for (int i2 = 0; i2 < tooltip.segments.size(); i2++) {
                            if (tooltip.segments.get(i2) instanceof TooltipTextSegment textSegment && textSegment.text.trim().toLowerCase().contains(searchText.trim().toLowerCase())) {
                                resultList.add(item);
                                break linesLoop;
                            }
                        }
                    }
                }
            }
        }
    }

    public final class APICompositeImage {
        APICompositeImage() {
        }

        public ArrayList<Component> createSeparatorHorizontal(int x, int y, int size) {
            ArrayList<Component> returnComponents = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int index = i == 0 ? 0 : i == (size - 1) ? 2 : 1;
                Image image = api.component.image.create(x + i, y, UIEngineBaseMedia_8x8.UI_SEPARATOR_HORIZONTAL, index);
                api.component.setColor(image, uiConfig.component_defaultColor);
                returnComponents.add(image);
            }
            return returnComponents;
        }

        public ArrayList<Component> createSeparatorVertical(int x, int y, int size) {
            ArrayList<Component> returnComponents = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int index = i == 0 ? 1 : i == (size - 1) ? 0 : 1;
                Image image = api.component.image.create(x, y + i, UIEngineBaseMedia_8x8.UI_SEPARATOR_VERTICAL, index);
                api.component.setColor(image, uiConfig.component_defaultColor);
                returnComponents.add(image);
            }
            return returnComponents;
        }

        public ArrayList<Component> createBorder(int x, int y, int width, int height) {
            return createBorder(x, y, width, height, 0);
        }

        public ArrayList<Component> createBorder(int x, int y, int width, int height, int gap) {
            ArrayList<Component> borders = new ArrayList<>();
            width = Math.max(width, 1);
            height = Math.max(height, 1);

            for (int ix = 0; ix < width; ix++) {

                borders.add(api.component.image.create(x + ix, y, UIEngineBaseMedia_8x8.UI_BORDERS, 2));

                if (ix >= gap) {
                    borders.add(api.component.image.create(x + ix, y + (height - 1), UIEngineBaseMedia_8x8.UI_BORDERS, 3));
                }
            }

            for (int iy = 0; iy < height; iy++) {
                borders.add(api.component.image.create(x, y + iy, UIEngineBaseMedia_8x8.UI_BORDERS, 0));
                borders.add(api.component.image.create(x + (width - 1), y + iy, UIEngineBaseMedia_8x8.UI_BORDERS, 1));
            }

            return borders;
        }
    }

    public final class APICompositeText {
        APICompositeText() {
        }

        public Text[][] createTable(int x, int y, String[] column1Text, int col1Width) {
            Text[][] ret = new Text[2][column1Text.length];

            for (int iy = 0; iy < column1Text.length; iy++) {
                Text text1 = api.component.text.create(x, y + ((column1Text.length - 1) - iy), Tools.Text.toArray(column1Text[iy]));
                ret[0][iy] = text1;
                Text text2 = api.component.text.create(x + col1Width, y + (column1Text.length - 1 - iy), new String[]{});
                ret[1][iy] = text2;
            }
            return ret;
        }

        public ArrayList<Component> createScrollAbleText(int x, int y, int width, int height, String[] text) {
            ArrayList<Component> result = new ArrayList<>();

            Text textField = api.component.text.create(x, y, null);
            api.component.setSize(textField, width - 1, height);
            ScrollbarVertical scrollBarVertical = api.component.scrollbar.scrollbarVertical.create(x + width - 1, y, height);
            String[] textConverted;
            String[] textDisplayedLines = new String[height];

            // Cut Text to Fit
            if (text != null) {
                ArrayList<String> textList = new ArrayList<>();
                int pixelWidth = ((width - 1) * api.TS());
                for (int i = 0; i < text.length; i++) {
                    String textLine = text[i];
                    textLine = Tools.Text.validString(textLine);
                    if (textLine.trim().length() > 0) {
                        String[] split = textLine.split(" ");
                        if (split.length > 0) {
                            StringBuilder currentLine = new StringBuilder();
                            for (int i2 = 0; i2 < split.length; i2++) {
                                String value = split[i2];
                                if (mediaManager.getCMediaFontTextWidth(uiConfig.ui_font, currentLine + value + " ") >= pixelWidth) {
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
            api.component.text.setTextAction(textField, new TextAction() {
                @Override
                public void onMouseScroll(float scrolled) {
                    float scrollAmount = (-1 / (float) Math.max(textConverted.length, 1)) * api.input.mouse.event.scrolledAmount();
                    UICommonUtils.scrollBar_scroll(scrollBarVertical, scrollBarVertical.scrolled + scrollAmount);
                }
            });
            api.component.scrollbar.setScrollBarAction(scrollBarVertical, new ScrollBarAction() {
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
            UICommonUtils.scrollBar_scroll(scrollBarVertical, 1f);
            if (textConverted.length <= height) {
                api.component.setDisabled(scrollBarVertical, true);
            }

            api.component.text.setLines(textField, textDisplayedLines);


            result.add(scrollBarVertical);
            result.add(textField);
            return result;
        }

        public Text createClickableURL(int x, int y, String url) {
            return createClickableURL(x, y, url, Tools.Text.toArray(url), Color.BLACK, Tools.Text.toArray(url), Color.BLUE);
        }

        public Text createClickableURL(int x, int y, String url, String[] text, Color fontColor, String[] textHover, Color fontColorHover) {
            return createClickableText(x, y, text, fontColor, button -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, textHover, fontColorHover);
        }

        public Text createClickableText(int x, int y, String[] text, CMediaFont font, IntConsumer onClick) {
            return createClickableText(x, y, text, Color.BLACK, onClick, text, Color.BLUE);
        }

        public Text createClickableText(int x, int y, String[] text, Color fontColor, IntConsumer onClick, String[] textHover, Color fontColorHover) {
            Text hlText = api.component.text.create(x, y, text);
            api.component.text.setTextAction(hlText, new TextAction() {
                @Override
                public void onMouseClick(int button) {
                    onClick.accept(button);
                }
            });
            api.component.addUpdateAction(hlText, new UpdateAction(0) {
                @Override
                public void onUpdate() {
                    if (Tools.Calc.pointRectsCollide(
                            api.input.mouse.state.xUI(),
                            api.input.mouse.state.yUI(),
                            api.component.absoluteX(hlText),
                            api.component.absoluteY(hlText),
                            hlText.width * api.TS(),
                            hlText.height * api.TS()
                    )) {
                        api.component.text.setFontColor(hlText, fontColorHover);
                        api.component.text.setLines(hlText, textHover);
                    } else {
                        api.component.text.setFontColor(hlText, fontColor);
                        api.component.text.setLines(hlText, text);
                    }
                }
            });
            return hlText;
        }
    }

    public final class APICompositeHotkey {
        APICompositeHotkey() {
        }

        public HotKeyAction createForButton(Button button) {
            return new HotKeyAction() {
                @Override
                public void onPress() {
                    UICommonUtils.button_press(button);
                }

                @Override
                public void onRelease() {
                    UICommonUtils.button_release(button);
                }
            };
        }
    }

    public final class APICompositeCheckbox {
        APICompositeCheckbox() {
        }

        public void makeExclusiveRadioButtons(Checkbox[] checkboxes, BiConsumer<Checkbox, Boolean> checkedFunction) {
            if (checkboxes == null || checkedFunction == null) return;
            for (int i = 0; i < checkboxes.length; i++) {
                Checkbox checkbox = checkboxes[i];
                api.component.checkbox.setCheckBoxAction(checkbox, new CheckboxAction() {
                    @Override
                    public void onCheck(boolean checked) {
                        if (checked) {
                            for (int i2 = 0; i2 < checkboxes.length; i2++) {
                                if (checkboxes[i2] != checkbox) {
                                    Checkbox otherCheckbox = checkboxes[i2];
                                    api.component.checkbox.setChecked(otherCheckbox, false);
                                    checkedFunction.accept(otherCheckbox, false);
                                }
                            }
                            checkedFunction.accept(checkbox, true);
                        } else {
                            api.component.checkbox.setChecked(checkbox, true);
                        }
                    }
                });
            }
        }


    }

    public final class APICompositeModal {
        APICompositeModal() {
        }

        public Window createColorPickerModal(String caption, Consumer<Color> selectColorFunction, Color initColor) {
            return createColorPickerModal(caption, selectColorFunction, initColor, UIEngineBaseMedia_8x8.UI_COLOR_SELECTOR);
        }

        public Window createColorPickerModal(String caption, Consumer<Color> selectColorFunction, Color initColor, CMediaImage colors) {

            TextureRegion colorTexture = mediaManager.getCMediaImage(colors);

            final int colorTextureWidthTiles = colorTexture.getRegionWidth() / 8;
            final int colorTextureHeightTiles = colorTexture.getRegionHeight() / 8;

            Window modal = api.window.create(0, 0, colorTextureWidthTiles + 1, colorTextureHeightTiles + 4, caption, UIEngineBaseMedia_8x8.UI_ICON_COLOR_PICKER, 0);
            ImageButton closeButton = api.composites.button.createWindowCloseButton(modal);
            api.component.button.setButtonAction(closeButton, new ButtonAction() {
                @Override
                public void onRelease() {
                    selectColorFunction.accept(null);
                    api.removeCurrentModalWindow();
                }
            });
            api.window.addComponent(modal, closeButton);

            TextButton ok = api.component.button.textButton.create(0, 0, colorTextureWidthTiles, 1, "OK", null);
            api.component.button.setButtonAction(ok, new ButtonAction() {
                @Override
                public void onRelease() {
                    selectColorFunction.accept(ok.color);
                    api.removeCurrentModalWindow();
                }
            });
            api.component.setColor(ok, initColor);


            Canvas colorCanvas = api.component.canvas.create(0, 2, colorTextureWidthTiles, colorTextureHeightTiles);


            CanvasImage cursorOverlay = api.component.canvas.canvasImage.create(UIEngineBaseMedia_8x8.UI_COLOR_SELECTOR_OVERLAY, api.TS() * 8, api.TS() * 4);
            api.component.canvas.addCanvasImage(colorCanvas, cursorOverlay);


            if (!colorTexture.getTexture().getTextureData().isPrepared())
                colorTexture.getTexture().getTextureData().prepare();
            Pixmap pixmap = colorTexture.getTexture().getTextureData().consumePixmap();

            Color pixelColor = new Color();
            for (int x = 0; x < colorTexture.getRegionWidth(); x++) {
                for (int y = 0; y < colorTexture.getRegionHeight(); y++) {
                    pixelColor.set(pixmap.getPixel(colorTexture.getRegionX() + x, colorTexture.getRegionY() + y));
                    api.component.canvas.point(colorCanvas, x, y, pixelColor.r, pixelColor.g, pixelColor.b, 1f);
                    if (initColor != null && pixelColor.r == initColor.r && pixelColor.g == initColor.g && pixelColor.b == initColor.b) {
                        api.component.canvas.canvasImage.setPosition(cursorOverlay, x - 1, y - 1);
                    }
                }
            }

            final boolean[] drag = {false};
            api.component.canvas.setCanvasAction(colorCanvas, new CanvasAction() {

                @Override
                public void onPress(int x, int y) {
                    drag[0] = true;
                }

                @Override
                public void onRelease() {
                    drag[0] = false;
                }
            });
            api.component.addUpdateAction(colorCanvas, new UpdateAction(10, true) {
                int xLast = -1, yLast = -1;
                Color currentColor = new Color();

                @Override
                public void onUpdate() {
                    if (drag[0]) {
                        int x = api.input.mouse.state.xUI() - api.component.absoluteX(colorCanvas);
                        int y = (api.input.mouse.state.yUI() - api.component.absoluteY(colorCanvas));
                        if (x < 0 || y < 0 || x >= colorTexture.getRegionWidth() || y >= colorTexture.getRegionHeight()) {
                            return;
                        }
                        if (x != xLast || y != yLast) {
                            currentColor.r = api.component.canvas.getR(colorCanvas, x, y - 1);
                            currentColor.g = api.component.canvas.getG(colorCanvas, x, y - 1);
                            currentColor.b = api.component.canvas.getB(colorCanvas, x, y - 1);
                            currentColor.a = 1f;
                            if (currentColor != null) {
                                api.component.setColor(ok, currentColor);
                                float colorBrightness = (0.299f * currentColor.r) + (0.587f * currentColor.g) + (0.114f * currentColor.b);
                                api.component.button.textButton.setFontColor(ok, colorBrightness < 0.5 ? Color.WHITE : Color.BLACK);
                                api.component.canvas.canvasImage.setPosition(cursorOverlay, x - 1, y - 1);
                                xLast = x;
                                yLast = y;
                            }
                        }
                    }
                }
            });


            Component[] componentl = new Component[]{colorCanvas, ok};
            api.component.move(ok, api.TS_HALF(), api.TS_HALF());
            api.component.move(colorCanvas, api.TS_HALF(), api.TS_HALF());
            api.window.addComponents(modal, componentl);

            return modal;
        }

        public Window createTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction) {
            return createTextInputModalInternal(caption, text, originalText, inputResultFunction, 0, Integer.MAX_VALUE, true, false, null, null, 11);
        }

        public Window createTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength) {
            return createTextInputModalInternal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, true, false, null, null, 11);
        }

        public Window createTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength, boolean showOKButton) {
            return createTextInputModalInternal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, showOKButton, false, null, null, 11);
        }

        public Window createTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength, boolean showOKButton, int windowMinWidth) {
            return createTextInputModalInternal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, showOKButton, false, null, null, windowMinWidth);
        }

        public Window createTouchTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction) {
            return createTextInputModalInternal(caption, text, originalText, inputResultFunction, 0, Integer.MAX_VALUE, true, true, uiConfig.mouseTextInput_defaultLowerCaseCharacters, uiConfig.mouseTextInput_defaultUpperCaseCharacters, 11);
        }

        public Window createTouchTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength) {
            return createTextInputModalInternal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, true, true, uiConfig.mouseTextInput_defaultLowerCaseCharacters, uiConfig.mouseTextInput_defaultUpperCaseCharacters, 11);
        }

        public Window createTouchTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength, char[] lowerCaseCharacters, char[] upperCaseCharacters) {
            return createTextInputModalInternal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, true, true, lowerCaseCharacters, upperCaseCharacters, 11);
        }

        public Window createTouchTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength, char[] lowerCaseCharacters, char[] upperCaseCharacters, int windowMinWidth) {
            return createTextInputModalInternal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, true, true, lowerCaseCharacters, upperCaseCharacters, windowMinWidth);
        }

        public Window createMessageModal(String caption, String[] lines, Runnable closeFunction) {
            int longest = 0;
            for (int i = 0; i < lines.length; i++) {
                int len = mediaManager.getCMediaFontTextWidth(uiConfig.ui_font, lines[i]);
                if (len > longest) longest = len;
            }
            ArrayList<Component> componentsList = new ArrayList<>();
            final int WIDTH = Math.max(MathUtils.round(longest / (float) api.TS()) + 2, 12);
            final int HEIGHT = 4 + lines.length;
            Window modal = api.window.create(0, 0, WIDTH, HEIGHT, caption, UIEngineBaseMedia_8x8.UI_ICON_INFORMATION, 0);

            Text[] texts = new Text[lines.length];
            for (int i = 0; i < lines.length; i++) {
                texts[i] = api.component.text.create(0, HEIGHT - 3 - i, Tools.Text.toArray(lines[i]));
                componentsList.add(texts[i]);
            }

            Button okBtn = api.component.button.textButton.create(0, 0, WIDTH - 1, 1, "OK", new ButtonAction() {
                @Override
                public void onRelease() {
                    if (closeFunction != null) {
                        closeFunction.run();
                    }
                    api.removeCurrentModalWindow();
                }
            });
            api.component.button.centerContent(okBtn);
            componentsList.add(okBtn);


            Component[] componentsArr = componentsList.toArray(new Component[]{});
            api.component.move(componentsArr, api.TS_HALF(), api.TS_HALF());
            api.window.addComponents(modal, componentsArr);
            return modal;
        }

        public Window createYesNoRequester(String caption, String text, Consumer<Boolean> choiceFunction) {
            return createYesNoRequester(caption, text, choiceFunction, "Yes", "No");
        }

        public Window createYesNoRequester(String caption, String text, Consumer<Boolean> choiceFunction, String yes, String no) {

            int textWidthMin = Math.max(
                    (mediaManager.getCMediaFontTextWidth(uiConfig.ui_font, caption) + 8),
                    mediaManager.getCMediaFontTextWidth(uiConfig.ui_font, text)
            );

            int width = Math.max(MathUtils.round(textWidthMin / (float) api.TS()) + 2, 12);
            if (width % 2 == 0) width++;
            Window modal = api.window.create(0, 0, width, 5, caption, UIEngineBaseMedia_8x8.UI_ICON_QUESTION, 0);

            int width1 = MathUtils.round(width / 2f) - 1;
            int width2 = width - width1 - 1;

            Text textC = api.component.text.create(0, 2, new String[]{text});
            int xOffset = 0;
            Button yesC = api.component.button.textButton.create(xOffset, 0, width1, 1, yes, new ButtonAction() {
                @Override
                public void onRelease() {
                    if (choiceFunction != null) choiceFunction.accept(Boolean.TRUE);
                    api.removeCurrentModalWindow();
                }
            });
            api.component.button.centerContent(yesC);
            xOffset += width1;
            Button noC = api.component.button.textButton.create(xOffset, 0, width2, 1, no, new ButtonAction() {
                @Override
                public void onRelease() {
                    if (choiceFunction != null) choiceFunction.accept(Boolean.FALSE);
                    api.removeCurrentModalWindow();
                }
            });
            api.component.button.centerContent(noC);

            Component[] componentsl = new Component[]{textC, yesC, noC};
            api.component.move(componentsl, api.TS_HALF(), api.TS_HALF());
            api.window.addComponents(modal, componentsl);
            return modal;
        }

        private Window createTextInputModalInternal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength, boolean showOKButton, boolean showTouchInputs, char[] lowerCaseCharacters, char[] upperCaseCharacters, int windowMinWidth) {
            int maxCharacters = 0;
            if (showTouchInputs) {
                if (lowerCaseCharacters == null || upperCaseCharacters == null) return null;
                maxCharacters = Math.min(lowerCaseCharacters.length, upperCaseCharacters.length);
            }

            showOKButton = showTouchInputs ? true : showOKButton;
            originalText = Tools.Text.validString(originalText);
            windowMinWidth = Math.max(windowMinWidth, 11);
            int wnd_width = Math.clamp(
                    MathUtils.round(mediaManager.getCMediaFontTextWidth(uiConfig.ui_font, text) / (float) api.TS()) + 2,
                    windowMinWidth, Integer.MAX_VALUE);
            int wnd_height = 5;
            if (showOKButton) wnd_height++;
            if (showTouchInputs) {
                wnd_height += 1 + (wnd_width % 2 == 0 ? 3 : 1);
                int ixt = 0;
                for (int i = 0; i < maxCharacters; i++) {
                    ixt += 2;
                    if (ixt > (wnd_width - 2)) {
                        wnd_height += 2;
                        ixt = 0;
                    }
                }

            }

            Window modalWnd = api.window.create(0, 0, wnd_width, wnd_height, caption, UIEngineBaseMedia_8x8.UI_ICON_INFORMATION, 0);
            ArrayList<Component> componentsList = new ArrayList<>();

            Text textC = api.component.text.create(0, showOKButton ? 3 : 2, Tools.Text.toArray(text));
            api.component.move(textC, api.TS_HALF(), api.TS_HALF());
            componentsList.add(textC);

            Textfield inputTextField = api.component.textfield.create(0, showOKButton ? 2 : 1, wnd_width - 1, originalText, null, maxInputLength);
            componentsList.add(inputTextField);
            api.component.move(inputTextField, api.TS_HALF(), 0);

            Button okBtn = null;
            if (showOKButton) {
                okBtn = api.component.button.textButton.create(0, 0, wnd_width - 1, 1, "OK", new ButtonAction() {
                    @Override
                    public void onRelease() {
                        if (inputTextField.content.length() >= minInputLength) {
                            if (inputResultFunction != null) inputResultFunction.accept(inputTextField.content);
                            api.removeCurrentModalWindow();
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
                        TextButton charButtonLC = api.component.button.textButton.create(ix, iy, 2, 2, String.valueOf(cl), new ButtonAction() {
                            @Override
                            public void onRelease() {
                                api.component.textfield.setContent(inputTextField, inputTextField.content + cl);
                                api.component.textfield.setMarkerPosition(inputTextField, inputTextField.content.length());
                            }
                        });
                        api.component.move(charButtonLC, api.TS_HALF(), api.TS_HALF());
                        componentsList.add(charButtonLC);
                        lowerCaseButtonsList.add(charButtonLC);
                        TextButton charButtonUC = api.component.button.textButton.create(ix, iy, 2, 2, String.valueOf(cu), new ButtonAction() {
                            @Override
                            public void onRelease() {
                                api.component.textfield.setContent(inputTextField, inputTextField.content + cu);
                                api.component.textfield.setMarkerPosition(inputTextField, inputTextField.content.length());
                            }
                        });
                        api.component.move(charButtonUC, api.TS_HALF(), api.TS_HALF());
                        componentsList.add(charButtonUC);
                        api.component.setVisible(charButtonUC, false);
                        upperCaseButtonsList.add(charButtonUC);
                    }
                    ix += 2;
                    if (ix >= (wnd_width - 2)) {
                        ix = 0;
                        iy -= 2;
                    }
                }

                // Add Case Button
                ImageButton caseButton = api.component.button.imageButton.create(ix, iy, 2, 2, UIEngineBaseMedia_8x8.UI_ICON_KEY_CASE, 0,
                        new ButtonAction() {
                            @Override
                            public void onToggle(boolean value) {
                                for (int i2 = 0; i2 < lowerCaseButtonsList.size(); i2++)
                                    api.component.setVisible(lowerCaseButtonsList.get(i2), !value);
                                for (int i2 = 0; i2 < upperCaseButtonsList.size(); i2++)
                                    api.component.setVisible(upperCaseButtonsList.get(i2), value);
                            }
                        }, BUTTON_MODE.TOGGLE);
                api.component.move(caseButton, api.TS_HALF(), api.TS_HALF());
                componentsList.add(caseButton);
                ix += 2;
                if (ix >= (wnd_width - 2)) {
                    ix = 0;
                    iy -= 2;
                }
                // Add Delete Button
                ImageButton delButton = api.component.button.imageButton.create(ix, iy, 2, 2, UIEngineBaseMedia_8x8.UI_ICON_KEY_DELETE, 0,
                        new ButtonAction() {
                            @Override
                            public void onRelease() {
                                if (inputTextField.content.length() > 0) {
                                    api.component.textfield.setContent(inputTextField, inputTextField.content.substring(0, inputTextField.content.length() - 1));
                                    api.component.textfield.setMarkerPosition(inputTextField, inputTextField.content.length());
                                }
                            }
                        }, BUTTON_MODE.DEFAULT);
                api.component.move(delButton, api.TS_HALF(), api.TS_HALF());
                componentsList.add(delButton);


            }


            Button finalOkBtn = okBtn;
            api.component.textfield.setTextFieldAction(inputTextField, new TextFieldAction() {
                @Override
                public void onEnter(String content, boolean valid) {
                    if (valid) {
                        if (inputResultFunction != null) inputResultFunction.accept(inputTextField.content);
                        api.removeCurrentModalWindow();
                    } else {
                        api.component.textfield.focus(inputTextField);
                    }
                }

                @Override
                public void onContentChange(String newContent, boolean valid) {
                    if (finalOkBtn != null) api.component.setDisabled(finalOkBtn, !valid);
                }

                @Override
                public boolean isContentValid(String newContent) {
                    return newContent.length() >= minInputLength;
                }

                @Override
                public void onUnFocus() {
                    api.component.textfield.focus(inputTextField);
                }
            });
            api.component.move(okBtn, api.TS_HALF(), api.TS_HALF());


            //
            api.window.addComponents(modalWnd, componentsList.toArray(new Component[]{}));
            api.window.setWindowAction(modalWnd, new WindowAction() {
                @Override
                public void onAdd() {
                    api.component.textfield.focus(inputTextField);
                }
            });

            UICommonUtils.textField_setContent(inputTextField, originalText);
            return modalWnd;
        }

    }

    public final class APICompositeCanvas {
        APICompositeCanvas() {
        }

        public GraphInfo drawGraph(Canvas canvas, int itemCount, int steps, int stepSize, Color colorBackGround, DrawGraphFunctions drawGraphFunctions, int[] hiAndLowValueReference, boolean drawBackGroundLines) {
            int mapWidth = canvas.width * api.TS();
            int mapHeight = canvas.height * api.TS();
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
                    api.component.canvas.point(canvas, ix, iy, color.r, color.g, color.b, color.a);
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
                int heightPixels = Math.max(MathUtils.round(mapHeight * heightPct), 2);
                for (int iy = 0; iy < heightPixels; iy++) {
                    int y = mapHeight - iy;
                    if (iy == heightPixels - 1) {
                        api.component.canvas.point(canvas, ix, y, colorBrighter.r, colorBrighter.g, colorBrighter.b, colorBrighter.a);
                    } else {
                        api.component.canvas.point(canvas, ix, y, color.r, color.g, color.b, color.a);
                    }
                }

                // Draw Shading
                if (indexChange && ix != 0) {
                    for (int iy = 0; iy < heightPixels; iy++) {
                        int y = mapHeight - iy;
                        api.component.canvas.point(canvas, ix, y, colorBrighter.r, colorBrighter.g, colorBrighter.b, colorBrighter.a);
                    }
                } else if (nextIndexChange) {
                    for (int iy = 0; iy < heightPixels; iy++) {
                        int y = mapHeight - iy;
                        api.component.canvas.point(canvas, ix, y, colorDarker.r, colorDarker.g, colorDarker.b, colorDarker.a);
                    }
                }


                lastValue = value;
            }

            return new GraphInfo(lowestValue, highestValue, indexAtPosition, valueAtPosition);
        }

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
    }

    public final class APICompositeButton {
        APICompositeButton() {
        }

        public ImageButton createWindowCloseButton(Window window) {
            return createWindowCloseButton(window, null);
        }

        public ImageButton createWindowCloseButton(Window window, Consumer<Window> closeFunction) {
            ImageButton closeButton = api.component.button.imageButton.create(window.width - 1, window.height - 1, 1, 1, UIEngineBaseMedia_8x8.UI_ICON_CLOSE);
            api.component.setName(closeButton, UICommonUtils.WND_CLOSE_BUTTON);
            api.component.button.setButtonAction(closeButton, new ButtonAction() {

                @Override
                public void onRelease() {
                    api.removeWindow(window);
                    if (closeFunction != null) closeFunction.accept(window);
                }
            });
            return closeButton;
        }

        public void makeExclusiveToggleButtons(Button[] buttons, BiConsumer<Button, Boolean> toggleFunction) {
            if (buttons == null || toggleFunction == null) return;
            int toggledButtonIndex = -1;
            for(int i = 0; i < buttons.length; i++){
                if(buttons[i].pressed){
                    toggledButtonIndex = i;
                    break;
                }
            }
            for (int i = 0; i < buttons.length; i++) {
                Button button = buttons[i];
                api.component.button.setButtonMode(button, BUTTON_MODE.TOGGLE);
                api.component.button.setButtonAction(button, new ButtonAction() {
                    @Override
                    public void onToggle(boolean value) {
                        if (value) {
                            api.component.button.setToggleDisabled(button, true);
                            for (int i = 0; i < buttons.length; i++) {
                                if (buttons[i] != button) {
                                    Button otherButton = buttons[i];
                                    api.component.button.setToggleDisabled(otherButton, false);
                                    api.component.button.toggle(otherButton, false);
                                    toggleFunction.accept(otherButton, false);
                                }
                            }
                            toggleFunction.accept(button, true);
                        }
                    }
                });
                if(i == toggledButtonIndex){
                    api.component.button.setToggleDisabled(button, true);
                }
            }
        }

    }

    public final class APICompositeTextfield {
        APICompositeTextfield() {
        }

        public Textfield createDecimalInputField(int x, int y, int width, float min, float max, DoubleConsumer onChange) {
            Textfield textField = api.component.textfield.create(x, y, width);
            api.component.textfield.setAllowedCharacters(textField, decimalsAllowedCharacters);
            api.component.textfield.setTextFieldAction(textField, new TextFieldAction() {
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
                        api.component.textfield.focus(textField);
                    }
                }
            });
            return textField;
        }

        public Textfield createIntegerInputField(int x, int y, int width, int min, int max, IntConsumer onChange) {
            Textfield textField = api.component.textfield.create(x, y, width);
            api.component.textfield.setAllowedCharacters(textField, numbersAllowedCharacters);
            api.component.textfield.setTextFieldAction(textField, new TextFieldAction() {
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
                        api.component.textfield.focus(textField);
                    }
                }
            });
            return textField;
        }

    }

    public final class APICompositeTabbar {
        APICompositeTabbar() {
        }

        public ArrayList<Component> createExtendableTabBar(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight, boolean bigIconMode) {
            ArrayList<Component> ret = new ArrayList<>();

            width = Math.max(width, 1);
            Tabbar tabBar = api.component.tabbar.create(x, y, width, tabs, selectedTab, tabBarAction, border, borderHeight, 2, bigIconMode);
            ImageButton extendButton = api.component.button.imageButton.create(x, y, 2, bigIconMode ? 2 : 1, UIEngineBaseMedia_8x8.UI_ICON_EXTEND);

            updateExtendableTabBarButtonInternal(tabBar, extendButton);

            ret.add(extendButton);
            ret.add(tabBar);

            return ret;
        }

        private void updateExtendableTabBarButtonInternal(Tabbar tabBar, ImageButton extendButton) {
            ArrayList<Tab> invisibleTabs = new ArrayList<>();
            for (int i = 0; i < tabBar.tabs.size(); i++)
                if (!api.component.tabbar.isTabVisible(tabBar, tabBar.tabs.get(i)))
                    invisibleTabs.add(tabBar.tabs.get(i));
            if (invisibleTabs.size() > 0) {
                api.component.button.setButtonAction(extendButton, new ButtonAction() {
                    @Override
                    public void onRelease() {
                        ArrayList<ContextmenuItem> contextMenuItems = new ArrayList<>();
                        for (int i2 = 0; i2 < invisibleTabs.size(); i2++) {
                            Tab invisibleTab = invisibleTabs.get(i2);
                            contextMenuItems.add(api.contextMenu.item.create(invisibleTab.title, new ContextMenuItemAction() {
                                @Override
                                public void onSelect() {
                                    api.component.tabbar.removeTab(tabBar, invisibleTab);
                                    api.component.tabbar.addTab(tabBar, invisibleTab, 0);
                                    api.component.tabbar.selectTab(tabBar, 0);
                                    updateExtendableTabBarButtonInternal(tabBar, extendButton);
                                }
                            }, invisibleTab.icon, 0));
                        }
                        Contextmenu selectTabMenu = api.contextMenu.create(contextMenuItems.toArray(new ContextmenuItem[0]));
                        api.openContextMenu(selectTabMenu);
                    }
                });
                api.component.setDisabled(extendButton, false);

            } else {
                api.component.button.setButtonAction(extendButton, null);
                api.component.setDisabled(extendButton, true);
            }


        }
    }


}
