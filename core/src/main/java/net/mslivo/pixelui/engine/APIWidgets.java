package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import net.mslivo.pixelui.engine.actions.*;
import net.mslivo.pixelui.media.*;
import net.mslivo.pixelui.utils.Tools;
import net.mslivo.pixelui.engine.constants.BUTTON_MODE;

import java.awt.*;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public final class APIWidgets {
    private static final char[] numbersAllowedCharacters = new char[]{'-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static final char[] decimalsAllowedCharacters = new char[]{'-', ',', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private final API api;
    private final UIEngineState uiEngineState;
    private final UICommonUtils uiCommonUtils;
    private final MediaManager mediaManager;
    private final UIEngineConfig uiEngineConfig;

    public final APICompositeList list;
    public final APICompositeGrid grid;
    public final APICompositeImage image;
    public final APICompositeText text;
    public final APICompositeHotkey hotkey;
    public final APICompositeCheckbox checkBox;
    public final APICompositeModal modal;
    public final APICompositeButton button;
    public final APICompositeTextfield textfield;
    public final APICompositeTabbar tabBar;

    APIWidgets(API api, UIEngineState uiEngineState, UICommonUtils uiCommonUtils, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.uiCommonUtils = uiCommonUtils;
        this.mediaManager = mediaManager;
        this.uiEngineConfig = uiEngineState.config;
        
        this.list = new APICompositeList();
        this.image = new APICompositeImage();
        this.text = new APICompositeText();
        this.hotkey = new APICompositeHotkey();
        this.checkBox = new APICompositeCheckbox();
        this.modal = new APICompositeModal();
        this.button = new APICompositeButton();
        this.textfield = new APICompositeTextfield();
        this.tabBar = new APICompositeTabbar();
        this.grid = new APICompositeGrid();
    }

    public final class APICompositeGrid {
        APICompositeGrid() {
        }

        private static final String PAGE_TEXT_PAGE_OF = "%s/%s";
        private static final String PAGE_TEXT_PAGE = "%s";

        public class PageAbleReadOnlyGrid {
            public final Grid grid;
            public final ImageButton backButton;
            public final ImageButton forwardButton;
            public final Text text;
            public final Component[] allComponents;
            public final Array items;
            public final Array<Object[][]> pages;
            public final AtomicInteger currentPage;
            public final int width, height;
            public final int x, y;
            public final boolean placeButtonsTop;
            public final boolean displayPagesOf;
            public final boolean doubleSized;

            public PageAbleReadOnlyGrid(Grid grid, ImageButton backButton, ImageButton forwardButton, Text text, Array items, Array<Object[][]> pages, int x, int y, int width, int height, boolean doubleSized, boolean placeButtonsTop, boolean displayPagesOf) {
                this.backButton = backButton;
                this.forwardButton = forwardButton;
                this.grid = grid;
                this.doubleSized = doubleSized;
                this.displayPagesOf = displayPagesOf;
                this.pages = pages;
                this.placeButtonsTop = placeButtonsTop;
                this.text = text;
                this.items = items;
                this.allComponents = new Component[]{
                        grid, backButton, forwardButton, text
                };
                this.width = width;
                this.height = height;
                this.x = x;
                this.y = y;
                this.currentPage = new AtomicInteger();
            }
        }

        public PageAbleReadOnlyGrid createPageableReadOnlyGrid(int x, int y, int width, int height, Array items, GridAction gridAction) {
            return createPageableReadOnlyGrid(x, y, width, height, items, gridAction, false, false, true);
        }

        private int pageAbleReadOnlyGridControlsY(int y, int height, boolean placeButtonTop, boolean doubleSized) {
            return (y + (placeButtonTop ? ((height) * (doubleSized ? 2 : 1)) : 0));
        }


        public PageAbleReadOnlyGrid createPageableReadOnlyGrid(int x, int y, int width, int height, Array items, GridAction gridAction, boolean doubleSized, boolean placeButtonTop, boolean displayPagesOf) {

            Array<Object[][]> pages = new Array<>();
            Grid grid = api.component.grid.create(x, y + (placeButtonTop ? 0 : 1), null, null, false, false, false, false, doubleSized);

            int y_controls = pageAbleReadOnlyGridControlsY(y, height, placeButtonTop, doubleSized);

            ImageButton backButton = api.component.button.imageButton.create(0, y_controls, 1, 1, UIEngineBaseMedia_8x8.UI_ICON_BACK);
            Text pageText = api.component.text.create(x, y_controls, 2, "");
            ImageButton forwardButton = api.component.button.imageButton.create(0, y_controls, 1, 1, UIEngineBaseMedia_8x8.UI_ICON_FORWARD);

            PageAbleReadOnlyGrid pageGrid = new PageAbleReadOnlyGrid(grid, backButton, forwardButton, pageText, items, pages, x, y, width, height, doubleSized, placeButtonTop, displayPagesOf);

            api.component.button.setButtonAction(backButton, new ButtonAction() {
                @Override
                public void onRelease() {
                    pageGrid.currentPage.set(Math.max(pageGrid.currentPage.get() - 1, 0));
                    pageableGridUpdatePages(pageGrid);
                    pageableGridUpdateButtonsText(pageGrid);
                }
            });
            api.component.button.setButtonAction(forwardButton, new ButtonAction() {
                @Override
                public void onRelease() {
                    pageGrid.currentPage.set(Math.min(pageGrid.currentPage.get() + 1, pages.size - 1));
                    pageableGridUpdatePages(pageGrid);
                    pageableGridUpdateButtonsText(pageGrid);
                }
            });

            api.component.addUpdateAction(grid, new UpdateAction(0, true) {
                Array refList = new Array(items);

                @Override
                public void onUpdate() {
                    if (!Objects.equals(items, refList)) {
                        pageableGridUpdatePages(pageGrid);
                        pageableGridUpdateButtonsText(pageGrid);
                        refList = new Array(items);
                    }

                }
            });

            pageableGridUpdatePages(pageGrid);
            pageableGridUpdateButtonsText(pageGrid);
            pageableReadOnlyGridSetGridAction(pageGrid, gridAction);

            return pageGrid;
        }

        public void pageableReadOnlyGridSetGridAction(PageAbleReadOnlyGrid pageGrid, GridAction gridAction) {
            if (gridAction == null) return;
            pageGrid.grid.gridAction = new GridAction() {
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
                public Color iconColor(Object item) {
                    return gridAction.iconColor(item);
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
                public void onDragIntoApp(Object listItem, int from_x, int from_y, int to_x, int to_y) {
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

                @Override
                public boolean iconFlipX() {
                    return gridAction.iconFlipX();
                }

                @Override
                public boolean iconFlipY() {
                    return gridAction.iconFlipY();
                }
            };
        }

        public void pageAbleGridSetSelectedItem(APICompositeGrid.PageAbleReadOnlyGrid pageGrid, Object item) {
            if (item == null) {
                api.component.grid.setSelectedItem(pageGrid.grid, null);
            } else {
                for (int i = 0; i < pageGrid.pages.size; i++) {
                    Object[][] array = pageGrid.pages.get(i);
                    for (int ix = 0; ix < array.length; ix++) {
                        for (int iy = 0; iy < array[0].length; iy++) {
                            if (array[ix][iy] == item) {
                                pageGrid.currentPage.set(i);
                                api.component.grid.setItems(pageGrid.grid, pageGrid.pages.get(pageGrid.currentPage.get()));
                                api.component.grid.setSelectedItem(pageGrid.grid, item);
                                pageableGridUpdateButtonsText(pageGrid);
                                return;
                            }
                        }
                    }
                }
            }
        }

        private void pageableGridUpdatePages(PageAbleReadOnlyGrid pageGrid) {
            int pageSize = pageGrid.width * pageGrid.height;
            int pagesCount = MathUtils.floor(pageGrid.items.size / (float) pageSize);
            if (pageGrid.items.size % pageSize > 0 || pageGrid.items.size == 0) pagesCount++;

            pageGrid.pages.clear();
            for (int i = 0; i < pagesCount; i++) {
                Object[][] page = new Object[pageGrid.width][pageGrid.height];
                pageGrid.pages.add(page);
            }

            int pageIndex = 0;
            int ix = 0;
            int iy = pageGrid.height - 1;
            for (int i = 0; i < pageGrid.items.size; i++) {
                Object[][] page = pageGrid.pages.get(pageIndex);
                page[ix][iy] = pageGrid.items.get(i);
                ix++;
                if (ix > pageGrid.width - 1) {
                    ix = 0;
                    iy--;
                    if (iy < 0) {
                        iy = pageGrid.height - 1;
                        pageIndex++;
                    }
                }
            }

            pageGrid.currentPage.set(Math.clamp(pageGrid.currentPage.get(), 0, pagesCount - 1));
            api.component.grid.setItems(pageGrid.grid, pageGrid.pages.get(pageGrid.currentPage.get()));
        }

        private void pageableGridUpdateButtonsText(APICompositeGrid.PageAbleReadOnlyGrid pageGrid) {
            String pageString, pageStringMax;

            if (pageGrid.displayPagesOf) {
                pageStringMax = String.format(PAGE_TEXT_PAGE_OF, pageGrid.pages.size, pageGrid.pages.size);
                pageString = String.format(PAGE_TEXT_PAGE_OF, pageGrid.currentPage.get() + 1, pageGrid.pages.size);
            } else {
                pageStringMax = String.format(PAGE_TEXT_PAGE, pageGrid.pages.size);
                pageString = String.format(PAGE_TEXT_PAGE, pageGrid.currentPage.get() + 1);
            }

            int y_controls = pageAbleReadOnlyGridControlsY(pageGrid.y, pageGrid.height, pageGrid.placeButtonsTop, pageGrid.doubleSized);

            api.component.setPositionGrid(pageGrid.backButton, pageGrid.x, y_controls);
            api.component.setPositionGrid(pageGrid.text, pageGrid.x + 1, y_controls);
            int textWidthTiles = MathUtils.ceil((mediaManager.fontTextWidth(api.config.ui.getFont(), pageStringMax) + 1) / api.TSF());
            api.component.setPositionGrid(pageGrid.forwardButton, (pageGrid.x + 1) + textWidthTiles, y_controls);
            api.component.text.setText(pageGrid.text, pageString);
            api.component.setWidth(pageGrid.text, textWidthTiles);
            return;
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
            Array originalList = list.items;
            Array itemsSearched = new Array(list.items);
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
                    if (list.items == null || list.items.size <= list.height) {
                        api.component.setDisabled(scrollBarVertical, true);
                        api.component.scrollbar.setScrolled(scrollBarVertical, 1f);
                    } else {
                        api.component.setDisabled(scrollBarVertical, false);
                    }
                }
            });
            return scrollBarVertical;
        }


        private void searchBarSearchItemsInternal(List list, Array searchList, Array resultList, String searchText, boolean searchTooltips, boolean searchArrayLists) {
            for (int i = 0; i < searchList.size; i++) {
                Object item = searchList.get(i);
                if (searchArrayLists && item instanceof Array array) {
                    searchBarSearchItemsInternal(list, array, resultList, searchText, searchTooltips, searchArrayLists);
                } else if (list.listAction.text(item).trim().toLowerCase().contains(searchText.trim().toLowerCase())) {
                    resultList.add(item);
                } else if (searchTooltips) {
                    Tooltip tooltip = list.listAction.toolTip(item);
                    if (tooltip != null) {
                        linesLoop:
                        for (int i2 = 0; i2 < tooltip.segments.size; i2++) {
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

        public Array<Component> createSeparatorHorizontal(int x, int y, int size) {
            Array<Component> returnComponents = new Array<>();
            for (int i = 0; i < size; i++) {
                int index = i == 0 ? 0 : i == (size - 1) ? 2 : 1;
                Image image = api.component.image.create(x + i, y, UIEngineBaseMedia_8x8.UI_SEPARATOR_HORIZONTAL, index);
                returnComponents.add(image);
            }
            return returnComponents;
        }

        public Array<Component> createSeparatorVertical(int x, int y, int size) {
            Array<Component> returnComponents = new Array<>();
            for (int i = 0; i < size; i++) {
                int index = i == 0 ? 1 : i == (size - 1) ? 0 : 1;
                Image image = api.component.image.create(x, y + i, UIEngineBaseMedia_8x8.UI_SEPARATOR_VERTICAL, index);
                returnComponents.add(image);
            }
            return returnComponents;
        }

        public Array<Component> createBorder(int x, int y, int width, int height) {
            return createBorder(x, y, width, height, 0);
        }

        public Array<Component> createBorder(int x, int y, int width, int height, int gap) {
            Array<Component> borders = new Array<>();
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

        public Text[][] createTable(int x, int y, String[] column1Text, int col1Width, int col2Width) {
            Text[][] ret = new Text[2][column1Text.length];

            for (int iy = 0; iy < column1Text.length; iy++) {
                Text text1 = api.component.text.create(x, y + ((column1Text.length - 1) - iy), col1Width, column1Text[iy]);
                ret[0][iy] = text1;
                Text text2 = api.component.text.create(x + col1Width, y + (column1Text.length - 1 - iy), col2Width, "");
                ret[1][iy] = text2;
            }
            return ret;
        }
        public Array<Component> createScrollAbleText(int x, int y, int width, int height, String[] text) {
            Array<Component> result = new Array<>();

            final ScrollbarVertical scrollBarVertical =
                    api.component.scrollbar.scrollbarVertical.create(x + width - 1, y, height);

            final String[] textDisplayedLines = new String[height];

            // Convert input text into wrapped lines that fit into (width - 1) columns
            final String[] textConverted;
            if (text != null) {
                Array<String> textList = new Array<>();
                final int pixelWidth =  api.TS(width - 1);

                for (int i = 0; i < text.length; i++) {
                    String textLine = Tools.Text.validString(text[i]);
                    if (textLine.trim().length() > 0) {
                        String[] words = textLine.split("\\s+");
                        if (words.length > 0) {
                            StringBuilder currentLine = new StringBuilder();
                            for (int w = 0; w < words.length; w++) {
                                String value = words[w];
                                // Predict candidate line (add a trailing space while building)
                                String candidate = currentLine.length() == 0
                                        ? value + " "
                                        : currentLine.toString() + value + " ";

                                if (mediaManager.fontTextWidth(uiEngineConfig.ui_font, candidate) >= pixelWidth) {
                                    // Flush current line
                                    String flushed = currentLine.toString().trim();
                                    if (flushed.length() > 0) textList.add(flushed);
                                    // Start a new line with the word
                                    currentLine.setLength(0);
                                    currentLine.append(value).append(' ');
                                } else {
                                    currentLine.setLength(0);
                                    currentLine.append(candidate);
                                }
                            }
                            String last = currentLine.toString().trim();
                            if (last.length() > 0) textList.add(last);
                        }
                    } else {
                        textList.add("");
                    }
                }
                textConverted = textList.toArray(String[]::new);
            } else {
                textConverted = new String[]{};
            }

            // Create text rows
            final Text[] texts = new Text[height];
            for (int i = 0; i < height; i++) {
                int text_y = y + ((height-1)-i);
                texts[i] = api.component.text.create(x, text_y, width - 1, null);
                // Per-row mouse scroll handler: use the provided 'scrolled' delta
                api.component.text.setTextAction(texts[i], new TextAction() {
                    @Override
                    public void onMouseScroll(float scrolled) {
                        // How many extra lines beyond the viewport?
                        int extra = Math.max(textConverted.length - height, 0);
                        // If nothing to scroll, ignore
                        if (extra == 0) return;

                        // One wheel notch ≈ one line. Negative to make wheel-up scroll up.
                        float step = -scrolled / (float) extra;

                        float target = scrollBarVertical.scrolled + step;
                        // Clamp to [0, 1]
                        if (target < 0f) target = 0f;
                        else if (target > 1f) target = 1f;

                        uiCommonUtils.scrollBar_scroll(scrollBarVertical, target);
                    }
                });
                result.add(texts[i]);
            }

            // Scrollbar action → map to which lines to show
            api.component.scrollbar.setScrollBarAction(scrollBarVertical, new ScrollBarAction() {
                @Override
                public void onScrolled(float scrolledPct) {
                    // Convert to "top-aligned" fraction (0 = top, 1 = bottom)
                    float scrolled = 1f-scrolledPct;

                    int extra = Math.max(textConverted.length - height, 0);
                    int scrolledTextIndex = (extra > 0)
                            ? MathUtils.clamp(MathUtils.round(extra * scrolled), 0, extra)
                            : 0;


                    for (int iy = 0; iy < height; iy++) {
                        int textIndex = scrolledTextIndex + iy;
                        textDisplayedLines[iy] = (textIndex < textConverted.length)
                                ? textConverted[textIndex]
                                : "";
                    }

                    for (int i = 0; i < texts.length; i++) {
                        api.component.text.setText(texts[i], textDisplayedLines[i]);
                    }
                }
            });

            // Initialize: show top
            uiCommonUtils.scrollBar_scroll(scrollBarVertical, 1f);

            // Disable scrollbar if not needed
            if (textConverted.length <= height) {
                api.component.setDisabled(scrollBarVertical, true);
            }

            result.add(scrollBarVertical);
            return result;
        }


        public Text createClickableURL(int x, int y, String url) {
            return createClickableURL(x, y, url, url, Color.BLACK, url, Color.BLUE);
        }

        public Text createClickableURL(int x, int y, String url, String text, Color fontColor, String textHover, Color fontColorHover) {
            return createClickableText(x, y, text, fontColor, button -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, textHover, fontColorHover);
        }

        public Text createClickableText(int x, int y, String text, CMediaFont font, IntConsumer onClick) {
            return createClickableText(x, y, text, Color.BLACK, onClick, text, Color.BLUE);
        }

        public Text createClickableText(int x, int y, String text, Color fontColor, IntConsumer onClick, String textHover, Color fontColorHover) {
            Text hlText = api.component.text.create(x, y, 0, text);
            api.component.text.setTextAction(hlText, new TextAction() {
                @Override
                public void onMousePress(int button) {
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
                        api.component.text.setText(hlText, textHover);
                    } else {
                        api.component.text.setFontColor(hlText, fontColor);
                        api.component.text.setText(hlText, text);
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
                    uiCommonUtils.button_press(button);
                }

                @Override
                public void onRelease() {
                    uiCommonUtils.button_release(button);
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
            return createColorPickerModal(caption, selectColorFunction, initColor, UIEngineBaseMedia_8x8.UI_COLOR_PICKER);
        }

        public Window createColorPickerModal(String caption, Consumer<Color> selectColorFunction, Color initColor, CMediaImage colors) {

            throw new RuntimeException("todo");
            /*
            TextureRegion colorTexture = mediaManager.image(colors);

            final int colorTextureWidthTiles = colorTexture.getRegionWidth() / 8;
            final int colorTextureHeightTiles = colorTexture.getRegionHeight() / 8;

            Window modal = api.window.create(0, 0, colorTextureWidthTiles + 1, colorTextureHeightTiles + 4, caption);
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
             */
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
            return createTextInputModalInternal(caption, text, originalText, inputResultFunction, 0, Integer.MAX_VALUE, true, true, uiEngineConfig.mouseTextInput_defaultLowerCaseCharacters, uiEngineConfig.mouseTextInput_defaultUpperCaseCharacters, 11);
        }

        public Window createTouchTextInputModal(String caption, String text, String originalText, Consumer<String> inputResultFunction, int minInputLength, int maxInputLength) {
            return createTextInputModalInternal(caption, text, originalText, inputResultFunction, minInputLength, maxInputLength, true, true, uiEngineConfig.mouseTextInput_defaultLowerCaseCharacters, uiEngineConfig.mouseTextInput_defaultUpperCaseCharacters, 11);
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
                int len = mediaManager.fontTextWidth(uiEngineConfig.ui_font, lines[i]);
                if (len > longest) longest = len;
            }
            Array<Component> componentsList = new Array<>();
            final int WIDTH = Math.max(MathUtils.round(longest / (float) api.TS()) + 2, 12);
            final int HEIGHT = 4 + lines.length;
            Window modal = api.window.create(0, 0, WIDTH, HEIGHT, caption);

            Text[] texts = new Text[lines.length];
            for (int i = 0; i < lines.length; i++) {
                texts[i] = api.component.text.create(0, HEIGHT - 3 - i, 0, lines[i]);
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


            Component[] componentsArr = componentsList.toArray(Component[]::new);
            api.component.move(componentsArr, api.TS_HALF(), api.TS_HALF());
            api.window.addComponents(modal, componentsArr);
            return modal;
        }

        public Window createYesNoRequester(String caption, String text, Consumer<Boolean> choiceFunction) {
            return createYesNoRequester(caption, text, choiceFunction, "Yes", "No");
        }

        public Window createYesNoRequester(String caption, String text, Consumer<Boolean> choiceFunction, String yes, String no) {

            int textWidthMin = Math.max(
                    (mediaManager.fontTextWidth(uiEngineConfig.ui_font, caption) + 8),
                    mediaManager.fontTextWidth(uiEngineConfig.ui_font, text)
            );

            int width = Math.max(MathUtils.round(textWidthMin / (float) api.TS()) + 2, 12);
            if (width % 2 == 0) width++;
            Window modal = api.window.create(0, 0, width, 5, caption);

            int width1 = MathUtils.round(width / 2f) - 1;
            int width2 = width - width1 - 1;

            Text textC = api.component.text.create(0, 2, 0, text);
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
                    MathUtils.round(mediaManager.fontTextWidth(uiEngineConfig.ui_font, text) / (float) api.TS()) + 2,
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

            Window modalWnd = api.window.create(0, 0, wnd_width, wnd_height, caption);
            Array<Component> componentsList = new Array<>();

            Text textC = api.component.text.create(0, showOKButton ? 3 : 2, 0, text);
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


            Array<Button> lowerCaseButtonsList = new Array<>();
            Array<Button> upperCaseButtonsList = new Array<>();
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
                                for (int i2 = 0; i2 < lowerCaseButtonsList.size; i2++)
                                    api.component.setVisible(lowerCaseButtonsList.get(i2), !value);
                                for (int i2 = 0; i2 < upperCaseButtonsList.size; i2++)
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
            api.window.addComponents(modalWnd, componentsList.toArray(Component[]::new));
            api.window.setWindowAction(modalWnd, new WindowAction() {
                @Override
                public void onDisplay() {
                    api.component.textfield.focus(inputTextField);
                }
            });

            uiCommonUtils.textField_setContent(inputTextField, originalText);
            return modalWnd;
        }

    }

    /*
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

     */

    public final class APICompositeButton {
        APICompositeButton() {
        }

        public ImageButton createWindowCloseButton(Window window) {
            return createWindowCloseButton(window, null);
        }

        public ImageButton createWindowCloseButton(Window window, Consumer<Window> closeFunction) {
            ImageButton closeButton = api.component.button.imageButton.create(window.width - 1, window.height - 1, 1, 1, UIEngineBaseMedia_8x8.UI_ICON_CLOSE);
            api.component.setName(closeButton, uiCommonUtils.WND_CLOSE_BUTTON);
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
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i].pressed) {
                    toggledButtonIndex = i;
                    break;
                }
            }
            for (int i = 0; i < buttons.length; i++) {
                Button button = buttons[i];
                api.component.button.setButtonMode(button, BUTTON_MODE.TOGGLE);

                final ButtonAction previousAction = button.buttonAction;

                api.component.button.setButtonAction(button, new ButtonAction() {
                    @Override
                    public void onPress() {
                        previousAction.onPress();
                    }

                    @Override
                    public void onRelease() {
                        previousAction.onRelease();
                    }

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

                    @Override
                    public void onMousePress(int mButton) {
                        previousAction.onMousePress(mButton);
                    }

                    @Override
                    public void onMouseDoubleClick(int mButton) {
                        previousAction.onMouseDoubleClick(mButton);
                    }

                    @Override
                    public void onMouseScroll(float scrolled) {
                        previousAction.onMouseScroll(scrolled);
                    }

                    @Override
                    public Tooltip onShowTooltip() {
                        return previousAction.onShowTooltip();
                    }

                    @Override
                    public CMediaSprite icon() {
                        return previousAction.icon();
                    }

                    @Override
                    public int iconIndex() {
                        return previousAction.iconIndex();
                    }

                    @Override
                    public Color iconColor() {
                        return previousAction.iconColor();
                    }

                    @Override
                    public boolean iconFlipX() {
                        return previousAction.iconFlipX();
                    }

                    @Override
                    public boolean iconFlipY() {
                        return previousAction.iconFlipY();
                    }
                });

                if (i == toggledButtonIndex) {
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

        public Array<Component> createExtendableTabBar(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight, boolean bigIconMode) {
            Array<Component> ret = new Array<>();

            width = Math.max(width, 1);
            Tabbar tabBar = api.component.tabbar.create(x, y, width, tabs, selectedTab, tabBarAction, border, borderHeight, 2, bigIconMode);
            ImageButton extendButton = api.component.button.imageButton.create(x, y, 2, bigIconMode ? 2 : 1, UIEngineBaseMedia_8x8.UI_ICON_EXTEND);

            updateExtendableTabBarButtonInternal(tabBar, extendButton);

            ret.add(extendButton);
            ret.add(tabBar);

            return ret;
        }

        private void updateExtendableTabBarButtonInternal(Tabbar tabBar, ImageButton extendButton) {
            Array<Tab> invisibleTabs = new Array<>();
            for (int i = 0; i < tabBar.tabs.size; i++)
                if (!api.component.tabbar.isTabVisible(tabBar, tabBar.tabs.get(i)))
                    invisibleTabs.add(tabBar.tabs.get(i));
            if (!invisibleTabs.isEmpty()) {
                api.component.button.setButtonAction(extendButton, new ButtonAction() {
                    @Override
                    public void onRelease() {
                        Array<ContextMenuItem> contextMenuItems = new Array<>();
                        for (int i2 = 0; i2 < invisibleTabs.size; i2++) {
                            Tab invisibleTab = invisibleTabs.get(i2);
                            contextMenuItems.add(api.contextMenu.item.create(invisibleTab.title, new ContextMenuItemAction() {
                                @Override
                                public void onSelect() {
                                    api.component.tabbar.removeTab(tabBar, invisibleTab);
                                    api.component.tabbar.addTab(tabBar, invisibleTab, 0);
                                    api.component.tabbar.selectTab(tabBar, 0);
                                    updateExtendableTabBarButtonInternal(tabBar, extendButton);
                                }
                            }));
                        }
                        ContextMenu selectTabMenu = api.contextMenu.create(contextMenuItems.toArray(ContextMenuItem[]::new));
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
