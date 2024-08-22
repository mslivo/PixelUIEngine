package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntSet;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.constants.BUTTON_MODE;
import net.mslivo.core.engine.ui_engine.constants.CHECKBOX_STYLE;
import net.mslivo.core.engine.ui_engine.constants.SHAPE_TYPE;
import net.mslivo.core.engine.ui_engine.rendering.ColorMap;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.Window;
import net.mslivo.core.engine.ui_engine.ui.actions.*;
import net.mslivo.core.engine.ui_engine.ui.components.Component;
import net.mslivo.core.engine.ui_engine.ui.components.button.ImageButton;
import net.mslivo.core.engine.ui_engine.ui.components.button.TextButton;
import net.mslivo.core.engine.ui_engine.ui.components.canvas.Canvas;
import net.mslivo.core.engine.ui_engine.ui.components.canvas.CanvasImage;
import net.mslivo.core.engine.ui_engine.ui.components.checkbox.Checkbox;
import net.mslivo.core.engine.ui_engine.ui.components.combobox.Combobox;
import net.mslivo.core.engine.ui_engine.ui.components.combobox.ComboboxItem;
import net.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import net.mslivo.core.engine.ui_engine.ui.components.image.Image;
import net.mslivo.core.engine.ui_engine.ui.components.knob.Knob;
import net.mslivo.core.engine.ui_engine.ui.components.list.List;
import net.mslivo.core.engine.ui_engine.ui.components.progressbar.Progressbar;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollbarHorizontal;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollbarVertical;
import net.mslivo.core.engine.ui_engine.ui.components.shape.Shape;
import net.mslivo.core.engine.ui_engine.ui.components.tabbar.Tab;
import net.mslivo.core.engine.ui_engine.ui.components.tabbar.Tabbar;
import net.mslivo.core.engine.ui_engine.ui.components.text.Text;
import net.mslivo.core.engine.ui_engine.ui.components.textfield.Textfield;
import net.mslivo.core.engine.ui_engine.ui.components.viewport.AppViewport;
import net.mslivo.core.engine.ui_engine.rendering.NestedFrameBuffer;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;
import net.mslivo.core.engine.ui_engine.ui.tooltip.TooltipCanvasSegment;

import java.util.ArrayList;
import java.util.HashSet;

public final class APIComponent {
    private final API api;
    private final UIEngineState uiEngineState;
    private final MediaManager mediaManager;
    private final UIConfig uiConfig;

    public final APIShape shape;
    public final APIButton button;
    public final APITabbar tabbar;
    public final APIGrid grid;
    public final APIScrollbar scrollbar;
    public final APIList list;
    public final APITextfield textfield;
    public final APICanvas canvas;
    public final APIKnob knob;
    public final APIText text;
    public final APIImage image;
    public final APICombobox comboBox;
    public final APIProgressbar progressbar;
    public final APICheckbox checkbox;
    public final APIAppViewport appViewport;

    APIComponent(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.uiConfig= uiEngineState.config;
        this.shape = new APIShape();
        this.button = new APIButton();
        this.tabbar = new APITabbar();
        this.grid = new APIGrid();
        this.scrollbar = new APIScrollbar();
        this.list = new APIList();
        this.textfield = new APITextfield();
        this.canvas = new APICanvas();
        this.knob = new APIKnob();
        this.text = new APIText();
        this.image = new APIImage();
        this.comboBox = new APICombobox();
        this.progressbar = new APIProgressbar();
        this.checkbox = new APICheckbox();
        this.appViewport = new APIAppViewport();
    }


    public final class APIAppViewport {

        APIAppViewport() {

        }

        public AppViewport create(int x, int y, int width, int height) {
            return create(x, y, width, height, null, 0, 0, 1f, uiConfig.component_appViewportDefaultUpdateTime);
        }

        public AppViewport create(int x, int y, int width, int height, AppViewPortAction appViewPortAction) {
            return create(x, y, width, height, appViewPortAction, 0, 0, 1f, uiConfig.component_appViewportDefaultUpdateTime);
        }

        public AppViewport create(int x, int y, int width, int height, AppViewPortAction appViewPortAction, float camPositionX, float camPositionY) {
            return create(x, y, width, height, appViewPortAction, camPositionX, camPositionY, 1f, uiConfig.component_appViewportDefaultUpdateTime);
        }

        public AppViewport create(int x, int y, int width, int height, AppViewPortAction appViewPortAction, float camPositionX, float camPositionY, float camZoom) {
            return create(x, y, width, height, appViewPortAction, camPositionX, camPositionY, camZoom, uiConfig.component_appViewportDefaultUpdateTime);
        }

        public AppViewport create(int x, int y, int width, int height, AppViewPortAction appViewPortAction, float camPositionX, float camPositionY, float camZoom, int updateTime) {
            AppViewport appViewPort = new AppViewport();
            appViewPort.updateTimer = 0;
            setComponentCommonInitValuesInternal(appViewPort, x, y, width, height, Color.GRAY);
            int viewportWidth = appViewPort.width * api.TS();
            int viewportHeight = appViewPort.height * api.TS();
            appViewPort.frameBuffer = new NestedFrameBuffer(Pixmap.Format.RGB888, viewportWidth, viewportHeight, true);
            Texture texture = appViewPort.frameBuffer.getColorBufferTexture();
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            appViewPort.textureRegion = new TextureRegion(texture, viewportWidth, viewportHeight);
            appViewPort.textureRegion.flip(false, true);
            appViewPort.camera = new OrthographicCamera(viewportWidth, viewportHeight);
            appViewPort.camera.setToOrtho(false, viewportWidth, viewportHeight);
            appViewPort.camera.position.set(camPositionX, camPositionY, 0f);
            appViewPort.camera.zoom = Math.max(camZoom, 0f);
            appViewPort.camera.update();
            appViewPort.updateTime = updateTime;
            appViewPort.appViewPortAction = appViewPortAction;
            return appViewPort;
        }

        public void setAppViewPortAction(AppViewport appViewPort, AppViewPortAction appViewPortAction) {
            if (appViewPort == null) return;
            appViewPort.appViewPortAction = appViewPortAction;
        }

        public void setUpdateTime(AppViewport appViewPort, int updateTime) {
            if (appViewPort == null) return;
            appViewPort.updateTime = Math.max(updateTime, 0);
        }

        public void setCamPosition(AppViewport appViewPort, float x, float y) {
            if (appViewPort == null) return;
            UICommonUtils.camera_setPosition(appViewPort.camera, x, y);
        }


        public void moveCam(AppViewport appViewPort, float x, float y) {
            if (appViewPort == null) return;
            UICommonUtils.camera_setPosition(appViewPort.camera,
                    (appViewPort.camera.position.x + x),
                    (appViewPort.camera.position.y + y)
            );
        }

        public void setCamX(AppViewport appViewPort, float x) {
            if (appViewPort == null) return;
            UICommonUtils.camera_setPosition(appViewPort.camera,
                    x,
                    appViewPort.camera.position.y
            );
        }

        public void moveCamX(AppViewport appViewPort, float x) {
            if (appViewPort == null) return;
            UICommonUtils.camera_setPosition(appViewPort.camera,
                    (appViewPort.camera.position.x + x),
                    appViewPort.camera.position.y
            );
        }

        public void setCamY(AppViewport appViewPort, float y) {
            if (appViewPort == null) return;
            UICommonUtils.camera_setPosition(appViewPort.camera,
                    appViewPort.camera.position.x,
                    y
            );
        }

        public void moveCamY(AppViewport appViewPort, float y) {
            if (appViewPort == null) return;
            UICommonUtils.camera_setPosition(appViewPort.camera,
                    appViewPort.camera.position.x,
                    (appViewPort.camera.position.y + y)
            );
        }

        public void setCamZoom(AppViewport appViewPort, float zoom) {
            if (appViewPort == null) return;
            UICommonUtils.camera_setZoom(appViewPort.camera, zoom);
        }

    }

    public final class APIProgressbar {

        APIProgressbar() {

        }

        public Progressbar create(int x, int y, int width) {
            return create(x, y, width, 0f, true, false);
        }

        public Progressbar create(int x, int y, int width, float progress) {
            return create(x, y, width, progress, true, false);
        }

        public Progressbar create(int x, int y, int width, float progress, boolean progressText) {
            return create(x, y, width, progress, progressText, false);
        }

        public Progressbar create(int x, int y, int width, float progress, boolean progressText, boolean progressText2Decimal) {
            Progressbar progressBar = new Progressbar();
            setComponentCommonInitValuesInternal(progressBar, x, y, width, 1, uiConfig.component_defaultColor);
            progressBar.progress = Math.clamp(progress, 0f, 1f);
            progressBar.progressText = progressText;
            progressBar.progressText2Decimal = progressText2Decimal;
            progressBar.font = uiConfig.component_defaultFont;
            return progressBar;
        }

        public void setFont(Progressbar progressBar, CMediaFont font) {
            if (progressBar == null) return;
            progressBar.font = font;
        }

        public void setProgress(Progressbar progressBar, float progress) {
            if (progressBar == null) return;
            UICommonUtils.progressbar_setProgress(progressBar, progress);
        }

        public void setProgressText(Progressbar progressBar, boolean progressText) {
            if (progressBar == null) return;
            progressBar.progressText = progressText;
        }

        public void setProgressText2Decimal(Progressbar progressBar, boolean progressText2Decimal) {
            if (progressBar == null) return;
            progressBar.progressText2Decimal = progressText2Decimal;
        }

    }

    public final class APIShape {

        APIShape() {
        }

        public Shape create(int x, int y, int width, int height, SHAPE_TYPE shapeType) {
            Shape shape = new Shape();
            setComponentCommonInitValuesInternal(shape, x, y, width, height, uiConfig.component_defaultColor);
            shape.shapeType = shapeType;
            return shape;
        }

        public void setShapeType(Shape shape, SHAPE_TYPE shapeType) {
            if (shape == null) return;
            shape.shapeType = shapeType;
        }

    }

    public final class APIButton {

        public final APITextButton textButton;
        public final APIImageButton imageButton;

        APIButton() {
            this.textButton = new APITextButton();
            this.imageButton = new APIImageButton();
        }

        private ButtonAction defaultButtonAction() {
            return new ButtonAction() {
            };
        }

        public final class APITextButton {

            APITextButton() {
            }

            public TextButton create(int x, int y, int width, int height, String text) {
                return create(x, y, width, height, text, defaultButtonAction(), null, 0, BUTTON_MODE.DEFAULT, false);
            }

            public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction) {
                return create(x, y, width, height, text, buttonAction, null, 0, BUTTON_MODE.DEFAULT, false);
            }

            public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaSprite icon, int iconIndex) {
                return create(x, y, width, height, text, buttonAction, icon, iconIndex, BUTTON_MODE.DEFAULT, false);
            }

            public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaSprite icon, int iconIndex, BUTTON_MODE buttonMode) {
                return create(x, y, width, height, text, buttonAction, icon, iconIndex, buttonMode, false);
            }

            public TextButton create(int x, int y, int width, int height, String text, ButtonAction buttonAction, CMediaSprite icon, int iconIndex, BUTTON_MODE buttonMode, boolean togglePressed) {
                TextButton textButton = new TextButton();
                setComponentCommonInitValuesInternal(textButton, x, y, width, height);
                setButtonCommonInitValuesInternal(textButton, buttonAction, buttonMode, togglePressed);
                textButton.text = Tools.Text.validString(text);
                textButton.font = uiConfig.component_defaultFont;
                textButton.icon = icon;
                textButton.iconIndex = iconIndex;
                UICommonUtils.button_centerContent(uiEngineState,mediaManager, textButton);
                return textButton;
            }

            public void setIcon(TextButton textButton, CMediaSprite icon) {
                if (textButton == null) return;
                textButton.icon = icon;
            }

            public void setIconIndex(TextButton textButton, int iconIndex) {
                if (textButton == null) return;
                textButton.iconIndex = Math.max(iconIndex, 0);
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

        public final class APIImageButton {

            APIImageButton() {

            }

            public ImageButton create(int x, int y, int width, int height, CMediaSprite image) {
                return create(x, y, width, height, image, 0, defaultButtonAction(), BUTTON_MODE.DEFAULT, false);
            }

            public ImageButton create(int x, int y, int width, int height, CMediaSprite image, int arrayIndex) {
                return create(x, y, width, height, image, arrayIndex, defaultButtonAction(), BUTTON_MODE.DEFAULT, false);
            }

            public ImageButton create(int x, int y, int width, int height, CMediaSprite image, int arrayIndex, ButtonAction buttonAction) {
                return create(x, y, width, height, image, arrayIndex, buttonAction, BUTTON_MODE.DEFAULT, false);
            }

            public ImageButton create(int x, int y, int width, int height, CMediaSprite image, int arrayIndex, ButtonAction buttonAction, BUTTON_MODE buttonMode) {
                return create(x, y, width, height, image, arrayIndex, buttonAction, buttonMode, false);
            }

            public ImageButton create(int x, int y, int width, int height, CMediaSprite image, int arrayIndex, ButtonAction buttonAction, BUTTON_MODE buttonMode, boolean togglePressed) {
                ImageButton imageButton = new ImageButton();
                setComponentCommonInitValuesInternal(imageButton, x, y, width, height, uiConfig.component_defaultColor, Color.GRAY);
                setButtonCommonInitValuesInternal(imageButton, buttonAction, buttonMode, togglePressed);
                imageButton.image = image;
                imageButton.arrayIndex = arrayIndex;
                UICommonUtils.button_centerContent(uiEngineState,mediaManager, imageButton);
                return imageButton;
            }

            public void setImage(ImageButton imageButton, CMediaSprite image) {
                if (imageButton == null) return;
                imageButton.image = image;
            }

            public void setArrayIndex(ImageButton imageButton, int arrayIndex) {
                if (imageButton == null) return;
                imageButton.arrayIndex = Math.max(arrayIndex, 0);
            }

        }

        public void setButtonAction(net.mslivo.core.engine.ui_engine.ui.components.button.Button button, ButtonAction buttonAction) {
            if (button == null) return;
            button.buttonAction = buttonAction;
        }

        public void press(net.mslivo.core.engine.ui_engine.ui.components.button.Button button) {
            if (button == null) return;
            UICommonUtils.button_press(button);
        }

        public void press(net.mslivo.core.engine.ui_engine.ui.components.button.Button[] buttons, boolean pressed) {
            if (buttons == null) return;
            for (int i = 0; i < buttons.length; i++) press(buttons[i]);
        }

        public void release(net.mslivo.core.engine.ui_engine.ui.components.button.Button button) {
            if (button == null) return;
            UICommonUtils.button_release(button);
        }

        public void release(net.mslivo.core.engine.ui_engine.ui.components.button.Button[] buttons) {
            if (buttons == null) return;
            for (int i = 0; i < buttons.length; i++) release(buttons[i]);
        }

        public void toggle(net.mslivo.core.engine.ui_engine.ui.components.button.Button button) {
            if (button == null) return;
            UICommonUtils.button_toggle(button);
        }

        public void toggle(net.mslivo.core.engine.ui_engine.ui.components.button.Button[] buttons) {
            if (buttons == null) return;
            for (int i = 0; i < buttons.length; i++) toggle(buttons[i]);
        }

        public void toggle(net.mslivo.core.engine.ui_engine.ui.components.button.Button button, boolean pressed) {
            if (button == null) return;
            UICommonUtils.button_toggle(button, pressed);
        }

        public void toggle(net.mslivo.core.engine.ui_engine.ui.components.button.Button[] buttons, boolean pressed) {
            if (buttons == null) return;
            for (int i = 0; i < buttons.length; i++) toggle(buttons[i], pressed);
        }

        public void setButtonMode(net.mslivo.core.engine.ui_engine.ui.components.button.Button button, BUTTON_MODE buttonMode) {
            if (button == null) return;
            button.mode = buttonMode;
        }

        public void setContentOffset(net.mslivo.core.engine.ui_engine.ui.components.button.Button button, int x, int y) {
            if (button == null) return;
            button.contentOffset_x = x;
            button.contentOffset_y = y;
        }

        public void setContentOffset(net.mslivo.core.engine.ui_engine.ui.components.button.Button[] buttons, int x, int y) {
            if (buttons == null) return;
            for (int i = 0; i < buttons.length; i++) setContentOffset(buttons[i], x, y);
        }

        public void setToggleDisabled(net.mslivo.core.engine.ui_engine.ui.components.button.Button button, boolean disabled) {
            button.toggleDisabled = disabled;
        }

        public void centerContent(net.mslivo.core.engine.ui_engine.ui.components.button.Button button) {
            UICommonUtils.button_centerContent(uiEngineState,mediaManager, button);
        }

        public void centerContent(net.mslivo.core.engine.ui_engine.ui.components.button.Button[] buttons) {
            if (buttons == null) return;
            for (int i = 0; i < buttons.length; i++) centerContent(buttons[i]);
        }

        private void setButtonCommonInitValuesInternal(net.mslivo.core.engine.ui_engine.ui.components.button.Button button, ButtonAction buttonAction, BUTTON_MODE buttonMode, boolean togglePressed) {
            button.buttonAction = buttonAction;
            button.mode = buttonMode;
            button.contentOffset_x = 0;
            button.contentOffset_y = 0;
            button.pressed = button.mode == BUTTON_MODE.TOGGLE ? togglePressed : false;
            button.toggleDisabled = false;
        }

    }

    public final class APICheckbox {

        APICheckbox() {
        }

        public Checkbox create(int x, int y, String text) {
            return create(x, y, text, CHECKBOX_STYLE.CHECKBOX, null, false);
        }

        public Checkbox create(int x, int y, String text, CHECKBOX_STYLE checkBoxStyle) {
            return create(x, y, text, checkBoxStyle, null, false);
        }

        public Checkbox create(int x, int y, String text, CHECKBOX_STYLE checkBoxStyle, CheckboxAction checkBoxAction) {
            return create(x, y, text, checkBoxStyle, checkBoxAction, false);
        }

        public Checkbox create(int x, int y, String text, CHECKBOX_STYLE checkBoxStyle, CheckboxAction checkBoxAction, boolean checked) {
            Checkbox checkBox = new Checkbox();
            setComponentCommonInitValuesInternal(checkBox, x, y, 1, 1, Color.GRAY);
            checkBox.text = Tools.Text.validString(text);
            checkBox.checkBoxStyle = checkBoxStyle;
            checkBox.checkBoxAction = checkBoxAction;
            checkBox.font = uiConfig.component_defaultFont;
            checkBox.checked = checked;
            return checkBox;
        }

        public void setText(Checkbox checkBox, String text) {
            if (checkBox == null) return;
            checkBox.text = Tools.Text.validString(text);
        }

        public void setFont(Checkbox checkBox, CMediaFont font) {
            if (checkBox == null) return;
            checkBox.font = font;
        }

        public void check(Checkbox checkBox) {
            if (checkBox == null) return;
            UICommonUtils.checkbox_check(checkBox);
        }

        public void unCheck(Checkbox checkBox) {
            if (checkBox == null) return;
            UICommonUtils.checkbox_unCheck(checkBox);
        }

        public void setChecked(Checkbox checkBox, boolean checked) {
            if (checkBox == null) return;
            if (checked) {
                check(checkBox);
            } else {
                unCheck(checkBox);
            }
        }

        public void setCheckBoxStyle(Checkbox checkBox, CHECKBOX_STYLE checkBoxStyle) {
            if (checkBox == null) return;
            checkBox.checkBoxStyle = checkBoxStyle;
        }

        public void setCheckBoxAction(Checkbox checkBox, CheckboxAction checkBoxAction) {
            if (checkBox == null) return;
            checkBox.checkBoxAction = checkBoxAction;
        }

    }

    public final class APITabbar {

        public final APITab tab;

        APITabbar() {
            tab = new APITab();
        }

        public Tabbar create(int x, int y, int width, Tab[] tabs) {
            return create(x, y, width, tabs, 0, null, false, 0, 0, false);
        }

        public Tabbar create(int x, int y, int width, Tab[] tabs, int selectedTab) {
            return create(x, y, width, tabs, selectedTab, null, false, 0, 0, false);
        }

        public Tabbar create(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction) {
            return create(x, y, width, tabs, selectedTab, tabBarAction, false, 0, 0, false);
        }

        public Tabbar create(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight) {
            return create(x, y, width, tabs, selectedTab, tabBarAction, border, borderHeight, 0, false);
        }

        public Tabbar create(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight, int tabOffset) {
            return create(x, y, width, tabs, selectedTab, tabBarAction, border, borderHeight, tabOffset, false);
        }

        public Tabbar create(int x, int y, int width, Tab[] tabs, int selectedTab, TabBarAction tabBarAction, boolean border, int borderHeight, int tabOffset, boolean bigIconMode) {
            Tabbar tabBar = new Tabbar();
            setComponentCommonInitValuesInternal(tabBar, x, y, width, (bigIconMode ? 2 : 1));
            tabBar.tabBarAction = tabBarAction;
            tabBar.border = border;
            tabBar.borderHeight = Math.max(borderHeight, 0);
            tabBar.tabOffset = Math.max(tabOffset, 0);
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
            tabBar.selectedTab = Math.clamp(selectedTab, 0, tabBar.tabs.size() - 1);
            return tabBar;
        }

        public void setTabOffset(Tabbar tabBar, int tabOffset) {
            if (tabBar == null) return;
            tabBar.tabOffset = Math.max(tabOffset, 0);
        }

        public void setBigIconMode(Tabbar tabBar, boolean bigIconMode) {
            if (tabBar == null) return;
            tabBar.bigIconMode = bigIconMode;
        }

        public void setBorder(Tabbar tabBar, boolean border) {
            tabBar.border = border;
        }

        public void setBorderHeight(Tabbar tabBar, int borderHeight) {
            tabBar.borderHeight = Math.max(borderHeight, 0);
        }

        public void setTabBarAction(Tabbar tabBar, TabBarAction tabBarAction) {
            if (tabBar == null) return;
            tabBar.tabBarAction = tabBarAction;
        }

        public Tab selectedTab(Tabbar tabBar) {
            if (tabBar == null) return null;
            return UICommonUtils.tabBar_getSelectedTab(tabBar);
        }

        public int selectedTabIndex(Tabbar tabBar) {
            if (tabBar == null) return 0;
            return tabBar.selectedTab;
        }

        public Tab tab(Tabbar tabBar, int index) {
            if (tabBar == null) return null;
            return tabBar.tabs.get(Math.clamp(index, 0, tabBar.tabs.size() - 1));
        }

        public Tab[] tabs(Tabbar tabBar) {
            return tabBar.tabs.toArray(new Tab[0]);
        }

        public void selectTab(Tabbar tabBar, int index) {
            if (tabBar == null) return;
            UICommonUtils.tabBar_selectTab(tabBar, index);
        }

        public void selectTab(Tabbar tabBar, Tab tab) {
            if (tabBar == null) return;
            UICommonUtils.tabBar_selectTab(tabBar, tab);
        }

        public void addTab(Tabbar tabBar, Tab tab) {
            if (tabBar == null || tab == null) return;
            UICommonUtils.tabBar_addTab(tabBar, tab);
        }

        public void addTab(Tabbar tabBar, Tab tab, int index) {
            if (tabBar == null || tab == null) return;
            UICommonUtils.tabBar_addTab(tabBar, tab, index);
        }

        public void addTabs(Tabbar tabBar, Tab[] tabs) {
            if (tabBar == null || tabs == null) return;
            for (int i = 0; i < tabs.length; i++) addTab(tabBar, tabs[i]);
        }

        public void removeTab(Tabbar tabBar, Tab tab) {
            if (tabBar == null || tab == null) return;
            UICommonUtils.tabBar_removeTab(tabBar, tab);
        }

        public void removeTabs(Tabbar tabBar, Tab[] tabs) {
            if (tabBar == null || tabs == null) return;
            for (int i = 0; i < tabs.length; i++) removeTab(tabBar, tabs[i]);
        }

        public void removeAllTabs(Tabbar tabBar) {
            if (tabBar == null) return;
            removeTabs(tabBar, tabBar.tabs.toArray(new Tab[]{}));
        }

        public ArrayList<Tab> findTabsByName(Tabbar tabBar, String name) {
            if (tabBar == null || name == null) return new ArrayList<>();
            ArrayList<Tab> result = new ArrayList<>();
            for (int i = 0; i < tabBar.tabs.size(); i++)
                if (name.equals(tabBar.tabs.get(i).name)) result.add(tabBar.tabs.get(i));
            return result;
        }

        public Tab findTabByName(Tabbar tabBar, String name) {
            if (tabBar == null || name == null) return null;
            ArrayList<Tab> result = findTabsByName(tabBar, name);
            return result.size() > 0 ? result.getFirst() : null;
        }

        public boolean isTabVisible(Tabbar tabBar, Tab tab) {
            if (tabBar == null || tab == null) return false;
            int xOffset = 0;
            for (int i = 0; i < tabBar.tabs.size(); i++) {
                xOffset += tabBar.tabs.get(i).width;
                if (tabBar.tabs.get(i) == tab) return xOffset <= tabBar.width;
            }
            return false;
        }

        public int tabsWidth(Tabbar tabBar) {
            if (tabBar == null) return 0;
            int width = 0;
            for (int i = 0; i < tabBar.tabs.size(); i++) width += tabBar.tabs.get(i).width;
            return width;
        }

        public final class APITab {

            private TabAction defaultTabAction() {
                return new TabAction() {
                };
            }

            public Tab create(String title) {
                return create(title, null, 0, null, defaultTabAction(), 0);
            }

            public Tab create(String title, CMediaSprite icon, int iconIndex) {
                return create(title, icon, iconIndex, null, defaultTabAction(), 0);
            }

            public Tab create(String title, CMediaSprite icon, int iconIndex, Component[] components) {
                return create(title, icon, iconIndex, components, defaultTabAction(), 0);
            }

            public Tab create(String title, CMediaSprite icon, int iconIndex, Component[] components, TabAction tabAction) {
                return create(title, icon, iconIndex, components, tabAction, 0);
            }

            public Tab create(String title, CMediaSprite icon, int iconIndex, Component[] components, TabAction tabAction, int width) {
                Tab tab = new Tab();
                tab.title = Tools.Text.validString(title);
                tab.tabAction = tabAction;
                tab.icon = icon;
                tab.iconIndex = iconIndex;
                tab.font = uiConfig.component_defaultFont;
                tab.name = "";
                tab.data = null;
                if (width == 0) {
                    tab.width = MathUtils.round((mediaManager.getCMediaFontTextWidth(tab.font, tab.title) + (tab.icon != null ? api.TS() : 0) + api.TS()) / api.TSF());
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

            public void setIconIndex(Tab tab, int iconIndex) {
                if (tab == null) return;
                tab.iconIndex = Math.max(iconIndex, 0);
            }

            public void addTabComponent(Tab tab, Component component) {
                if (tab == null || component == null) return;
                UICommonUtils.tab_addComponent(tab, component);
            }

            public void setTabComponents(Tab tab, Component[] components) {
                if (tab == null || components == null) return;
                removeAllTabComponents(tab);
                for (int i = 0; i < components.length; i++) addTabComponent(tab, components[i]);
            }

            public void addTabComponents(Tab tab, Component[] components) {
                if (tab == null || components == null) return;
                for (int i = 0; i < components.length; i++) addTabComponent(tab, components[i]);
            }

            public void removeTabComponent(Tab tab, Component component) {
                if (tab == null || component == null) return;
                UICommonUtils.tab_removeComponent(tab, component);
            }

            public void removeTabComponents(Tab tab, Component[] components) {
                if (tab == null || components == null) return;
                for (int i = 0; i < components.length; i++) removeTabComponent(tab, components[i]);
            }

            public void removeAllTabComponents(Tab tab) {
                if (tab == null) return;
                removeTabComponents(tab, tab.components.toArray(new Component[]{}));
            }

            public void setIcon(Tab tab, CMediaSprite icon) {
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
                tab.width = Math.max(width, 1);
            }

        }
    }

    public final class APIGrid {

        public APIGrid() {
        }

        private GridAction defaultGridAction() {
            return new GridAction() {
            };
        }

        public Grid create(int x, int y, Object[][] items) {
            return create(x, y, items, defaultGridAction(), false, false, false, false, false);
        }

        public Grid create(int x, int y, Object[][] items, GridAction gridAction) {
            return create(x, y, items, gridAction, false, false, false, false, false);
        }

        public Grid create(int x, int y, Object[][] items, GridAction gridAction, boolean multiSelect) {
            return create(x, y, items, gridAction, multiSelect, false, false, false, false);
        }

        public Grid create(int x, int y, Object[][] items, GridAction gridAction, boolean multiSelect, boolean dragEnabled) {
            return create(x, y, items, gridAction, multiSelect, dragEnabled, false, false, false);
        }

        public Grid create(int x, int y, Object[][] items, GridAction gridAction, boolean multiSelect, boolean dragEnabled, boolean dragOutEnabled) {
            return create(x, y, items, gridAction, multiSelect, dragEnabled, dragOutEnabled, false, false);
        }

        public Grid create(int x, int y, Object[][] items, GridAction gridAction, boolean multiSelect, boolean dragEnabled, boolean dragOutEnabled, boolean dragInEnabled) {
            return create(x, y, items, gridAction, multiSelect, dragEnabled, dragOutEnabled, dragInEnabled, false);
        }

        public Grid create(int x, int y, Object[][] items, GridAction gridAction, boolean multiSelect, boolean dragEnabled, boolean dragOutEnabled, boolean dragInEnabled, boolean doubleSized) {
            Grid grid = new Grid();
            int width = 1;
            int height = 1;
            if (items != null) {
                width = items.length * (doubleSized ? 2 : 1);
                height = items[0].length * (doubleSized ? 2 : 1);
            }
            setComponentCommonInitValuesInternal(grid, x, y, width, height);
            grid.selectedItem = null;
            grid.selectedItems = new HashSet();
            grid.items = items;
            grid.gridAction = gridAction;
            grid.dragEnabled = dragEnabled;
            grid.dragInEnabled = dragInEnabled;
            grid.dragOutEnabled = dragOutEnabled;
            grid.doubleSized = doubleSized;
            grid.multiSelect = multiSelect;
            return grid;
        }

        public void setDoubleSized(Grid grid, boolean doubleSized) {
            grid.doubleSized = doubleSized;
            UICommonUtils.grid_updateSize(grid);
        }

        public boolean isPositionValid(Grid grid, int x, int y) {
            if (grid == null) return false;
            return UICommonUtils.grid_positionValid(grid, x, y);
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
            UICommonUtils.grid_setItems(grid, items);
        }


    }

    public final class APITextfield {

        APITextfield() {
        }

        private TextFieldAction defaultTextFieldAction() {
            return new TextFieldAction() {
            };
        }

        public Textfield create(int x, int y, int width) {
            return create(x, y, width, "", defaultTextFieldAction(), 32,
                    uiConfig.component_textFieldDefaultAllowedCharacters);
        }


        public Textfield create(int x, int y, int width, String content) {
            return create(x, y, width, content, defaultTextFieldAction(), 32,
                    uiConfig.component_textFieldDefaultAllowedCharacters);
        }


        public Textfield create(int x, int y, int width, String content, TextFieldAction textFieldAction) {
            return create(x, y, width, content, textFieldAction, 32,
                    uiConfig.component_textFieldDefaultAllowedCharacters);
        }

        public Textfield create(int x, int y, int width, String content, TextFieldAction textFieldAction, int contentMaxLength) {
            return create(x, y, width, content, textFieldAction, contentMaxLength,
                    uiConfig.component_textFieldDefaultAllowedCharacters);
        }

        public Textfield create(int x, int y, int width, String content, TextFieldAction textFieldAction, int contentMaxLength, char[] allowedCharacters) {
            Textfield textField = new Textfield();
            setComponentCommonInitValuesInternal(textField, x, y, width, 1, Color.GRAY);
            textField.font = uiConfig.component_defaultFont;
            textField.allowedCharacters = new IntSet();
            for (int i = 0; i < allowedCharacters.length; i++)
                textField.allowedCharacters.add(allowedCharacters[i]);
            textField.offset = 0;
            textField.content = Tools.Text.validString(content);
            textField.textFieldAction = textFieldAction;
            textField.markerPosition = textField.content.length();
            textField.contentMaxLength = Math.max(contentMaxLength, 0);
            textField.contentValid = textField.textFieldAction == null || textField.textFieldAction.isContentValid(textField.content);
            return textField;
        }

        public void setMarkerPosition(Textfield textField, int position) {
            if (textField == null) return;
            UICommonUtils.textField_setMarkerPosition(uiEngineState,mediaManager, textField, position);
        }

        public void setContent(Textfield textField, String content) {
            if (textField == null) return;
            UICommonUtils.textField_setContent(textField, content);
        }

        public void setFont(Textfield textField, CMediaFont font) {
            if (textField == null) return;
            textField.font = font;
        }

        public void setTextFieldAction(Textfield textField, TextFieldAction textFieldAction) {
            if (textField == null) return;
            textField.textFieldAction = textFieldAction;
            UICommonUtils.textField_setContent(textField, textField.content); // Trigger validation
        }

        public void setContentMaxLength(Textfield textField, int contentMaxLength) {
            if (textField == null) return;
            textField.contentMaxLength = Math.max(contentMaxLength, 0);
        }

        public void setAllowedCharacters(Textfield textField, char[] allowedCharacters) {
            if (textField == null) return;
            textField.allowedCharacters.clear();
            if (allowedCharacters != null) {
                for (int i = 0; i < allowedCharacters.length; i++)
                    textField.allowedCharacters.add(allowedCharacters[i]);
            }
        }

        public void unFocus(Textfield textField) {
            if (textField == null) return;
            UICommonUtils.textField_unFocus(uiEngineState, textField);
        }

        public void focus(Textfield textField) {
            if (textField == null) return;
            UICommonUtils.textField_focus(uiEngineState, textField);
        }

        public boolean isFocused(Textfield textField) {
            return UICommonUtils.textField_isFocused(uiEngineState, textField);
        }

        public boolean isContentValid(Textfield textField) {
            if (textField == null) return false;
            return textField.contentValid;
        }
    }

    public final class APICanvas {

        public final APICanvasImage canvasImage;

        APICanvas() {
            this.canvasImage = new APICanvasImage();
        }


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
            setComponentCommonInitValuesInternal(canvas, x, y, width, height, Color.GRAY);
            canvas.colorMap = new ColorMap();
            int widthPx =  api.TS(width);
            int heightPx =  api.TS(height);
            canvas.colorMap.width = widthPx;
            canvas.colorMap.height = heightPx;
            canvas.colorMap.r = new float[widthPx][heightPx];
            canvas.colorMap.g = new float[widthPx][heightPx];
            canvas.colorMap.b = new float[widthPx][heightPx];
            canvas.colorMap.a = new float[widthPx][heightPx];

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

        public boolean isPointInBounds(Canvas canvas, int x, int y) {
            if (canvas == null) return false;
            return UICommonUtils.colorMap_inBounds(canvas.colorMap, x,y);
        }

        public void addCanvasImage(Canvas canvas, CanvasImage canvasImage) {
            if (canvas == null || canvasImage == null) return;
            UICommonUtils.canvas_addCanvasImage(canvas, canvasImage);
        }

        public void addCanvasImages(Canvas canvas, CanvasImage[] canvasImages) {
            if (canvas == null || canvasImages == null) return;
            for (int i = 0; i < canvasImages.length; i++) addCanvasImage(canvas, canvasImages[i]);
        }

        public void removeCanvasImage(Canvas canvas, CanvasImage canvasImage) {
            if (canvas == null || canvasImage == null) return;
            UICommonUtils.canvas_removeCanvasImage(canvas, canvasImage);
        }

        public void removeCanvasImages(Canvas canvas, CanvasImage[] canvasImages) {
            if (canvas == null || canvasImages == null) return;
            for (int i = 0; i < canvasImages.length; i++) removeCanvasImage(canvas, canvasImages[i]);
        }

        public void removeAllCanvasImages(Canvas canvas) {
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

        /* Draw Functions */

        public void point(Canvas canvas, int x, int y, float r, float g, float b, float a) {
            if (canvas == null) return;
            UICommonUtils.colorMap_set(canvas.colorMap, x, y, r, g, b, a);
        }

        public void point(Canvas canvas, int x, int y, Color color) {
            point(canvas, x, y, color.r, color.g, color.b, color.a);
        }

        public void clear(Canvas canvas, float r, float g, float b, float a) {
            if (canvas == null) return;
            UICommonUtils.colorMap_clear(canvas.colorMap, r, g, b, a);
        }

        public void clear(Canvas canvas, Color color) {
            clear(canvas, color.r, color.g, color.b, color.a);
        }

        public void copy(Canvas canvas, ColorMap colorMap) {
            if(canvas == null) return;
            UICommonUtils.colorMap_copy(colorMap, canvas.colorMap);
        }

        public Color getPoint(Canvas canvas, int x, int y) {
            if (canvas == null) return null;
            return UICommonUtils.colorMap_getPointAsColor(canvas.colorMap, x,y);
        }

        public Color getColor(Canvas canvas, int x, int y) {
            if (canvas == null) return null;
            return UICommonUtils.colorMap_getPointAsColor(canvas.colorMap, x,y);
        }

        public float getR(Canvas canvas, int x, int y) {
            if (canvas == null) return 0f;
            return UICommonUtils.colorMap_r(canvas.colorMap, x,y);
        }

        public float getG(Canvas canvas, int x, int y) {
            if (canvas == null) return 0f;
            return UICommonUtils.colorMap_g(canvas.colorMap, x,y);
        }

        public float getB(Canvas canvas, int x, int y) {
            if (canvas == null) return 0f;
            return UICommonUtils.colorMap_b(canvas.colorMap, x,y);
        }
        public float getA(Canvas canvas, int x, int y) {
            if (canvas == null) return 0f;
            return UICommonUtils.colorMap_a(canvas.colorMap, x,y);
        }

        public final class APICanvasImage {

            APICanvasImage() {
            }

            public CanvasImage create(CMediaSprite image, int x, int y) {
                return create(image, x, y, 0, false, uiConfig.component_mapOverlayDefaultFadeoutSpeed);
            }

            public CanvasImage create(CMediaSprite image, int x, int y, int arrayIndex) {
                return create(image, x, y, arrayIndex, false, uiConfig.component_mapOverlayDefaultFadeoutSpeed);
            }

            public CanvasImage create(CMediaSprite image, int x, int y, int arrayIndex, boolean fadeOut) {
                return create(image, x, y, arrayIndex, fadeOut, uiConfig.component_mapOverlayDefaultFadeoutSpeed);
            }

            public CanvasImage create(CMediaSprite image, int x, int y, int arrayIndex, boolean fadeOut, float fadeOutSpeed) {
                CanvasImage canvasImage = new CanvasImage();
                canvasImage.image = image;
                canvasImage.x = x;
                canvasImage.y = y;
                canvasImage.fadeOut = fadeOut;
                canvasImage.fadeOutSpeed = Math.max(fadeOutSpeed, 0);
                canvasImage.color = new Color(Color.GRAY);
                canvasImage.arrayIndex = Math.max(arrayIndex, 0);
                canvasImage.name = Tools.Text.validString("");
                canvasImage.data = null;
                canvasImage.addedToCanvas = null;
                return canvasImage;
            }

            public void setFadeOut(CanvasImage canvasImage, boolean fadeOut) {
                if (canvasImage == null) return;
                canvasImage.fadeOut = fadeOut;
            }

            public void setFadeOutTime(CanvasImage canvasImage, float fadeoutSpeed) {
                if (canvasImage == null) return;
                canvasImage.fadeOutSpeed = Math.max(fadeoutSpeed, 0);
            }

            public void setPosition(CanvasImage canvasImage, int x, int y) {
                if (canvasImage == null) return;
                canvasImage.x = x;
                canvasImage.y = y;
            }

            public void move(CanvasImage canvasImage, int x, int y) {
                if (canvasImage == null) return;
                setPosition(canvasImage, canvasImage.x + x, canvasImage.y + y);
            }

            public void setImage(CanvasImage canvasImage, CMediaSprite image) {
                if (canvasImage == null) return;
                canvasImage.image = image;
            }

            public void setColor(CanvasImage canvasImage, Color color) {
                if (canvasImage == null) return;
                canvasImage.color.set(color);
            }

            public void setArrayIndex(CanvasImage canvasImage, int arrayIndex) {
                if (canvasImage == null) return;
                canvasImage.arrayIndex = Math.max(arrayIndex, 0);
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

    public final class APIKnob {

        APIKnob() {
        }

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
            setComponentCommonInitValuesInternal(knob, x, y, 2, 2, uiConfig.component_defaultColor, Color.BLACK);
            knob.endless = endless;
            knob.turned = Math.clamp(turned, 0f, 1f);
            knob.knobAction = knobAction;
            return knob;
        }

        public void setTurned(Knob knob, float turned) {
            if (knob == null) return;
            UICommonUtils.knob_turnKnob(knob, turned);
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

    public final class APIText {

        APIText() {
        }

        private TextAction defaultTextAction() {
            return new TextAction() {
            };
        }

        public Text create(int x, int y, String[] lines) {
            return create(x, y, lines, defaultTextAction());
        }

        public Text create(int x, int y, String[] lines, TextAction textAction) {
            Text text = new Text();
            text.font = uiConfig.component_defaultFont;
            int width = 1;
            int height = 1;
            if (lines != null && text.font != null) {
                for (int i = 0; i < lines.length; i++) {
                    int widthT = mediaManager.getCMediaFontTextWidth(text.font, lines[i]);
                    if (widthT > width) width = widthT;
                }
                width = width / api.TS();
                height = lines.length;
            }
            setComponentCommonInitValuesInternal(text, x, y, width, height);
            text.textAction = textAction;
            text.lines = Tools.Text.validStringArray(lines);
            return text;
        }

        public void setTextAction(Text text, TextAction textAction) {
            if (text == null) return;
            text.textAction = textAction;
        }

        public void setLines(Text text, String... lines) {
            if (text == null) return;
            UICommonUtils.text_setLines(uiEngineState,mediaManager, text, lines);
        }

        public void setFont(Text text, CMediaFont font) {
            if (text == null) return;
            text.font = font;
        }

    }

    public final class APIImage {

        APIImage() {
        }

        private ImageAction defaultImageAction() {
            return new ImageAction() {
            };
        }

        public Image create(int x, int y, CMediaSprite image) {
            return create(x, y, image, 0, defaultImageAction());
        }

        public Image create(int x, int y, CMediaSprite image, int arrayIndex) {
            return create(x, y, image, arrayIndex, defaultImageAction());
        }


        public Image create(int x, int y, CMediaSprite image, int arrayIndex, ImageAction imageAction) {
            Image imageC = new Image();
            int width = image != null ? mediaManager.getCMediaSpriteWidth(image) / api.TS() : 0;
            int height = image != null ? mediaManager.getCMediaSpriteHeight(image) / api.TS() : 0;
            setComponentCommonInitValuesInternal(imageC, x, y, width, height, Color.GRAY);
            imageC.image = image;
            imageC.arrayIndex = Math.max(arrayIndex, 0);
            imageC.imageAction = imageAction;
            return imageC;
        }

        public void setImageAction(Image image, ImageAction imageAction) {
            if (image == null) return;
            image.imageAction = imageAction;
        }

        public void setArrayIndex(Image image, int arrayIndex) {
            if (image == null) return;
            image.arrayIndex = Math.max(arrayIndex, 0);
        }

        public void setImage(Image imageC, CMediaSprite image) {
            if (imageC == null) return;
            UICommonUtils.image_setImage(uiEngineState,mediaManager, imageC, image);
        }

    }

    public final class APICombobox {

        public final APIComboboxItem item;

        APICombobox() {
            this.item = new APIComboboxItem();
        }

        private ComboBoxAction defaultComboBoxAction() {
            return new ComboBoxAction() {
            };
        }

        public Combobox create(int x, int y, int width) {
            return create(x, y, width, null, defaultComboBoxAction(), false, null);
        }

        public Combobox create(int x, int y, int width, ComboboxItem[] combobBoxItems) {
            return create(x, y, width, combobBoxItems, defaultComboBoxAction(), false, null);
        }

        public Combobox create(int x, int y, int width, ComboboxItem[] combobBoxItems, ComboBoxAction comboBoxAction) {
            return create(x, y, width, combobBoxItems, comboBoxAction, false, null);
        }

        public Combobox create(int x, int y, int width, ComboboxItem[] combobBoxItems, ComboBoxAction comboBoxAction, boolean useIcons) {
            return create(x, y, width, combobBoxItems, comboBoxAction, useIcons, null);
        }

        public Combobox create(int x, int y, int width, ComboboxItem[] combobBoxItems, ComboBoxAction comboBoxAction, boolean useIcons, ComboboxItem selectedItem) {
            Combobox comboBox = new Combobox();
            setComponentCommonInitValuesInternal(comboBox, x, y, width, 1);
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

        public void setComboBoxAction(Combobox comboBox, ComboBoxAction comboBoxAction) {
            if (comboBox == null) return;
            comboBox.comboBoxAction = comboBoxAction;
        }

        public void setUseIcons(Combobox comboBox, boolean useIcons) {
            if (comboBox == null) return;
            comboBox.useIcons = useIcons;
        }

        public void addComboBoxItem(Combobox comboBox, ComboboxItem comboBoxItem) {
            if (comboBox == null || comboBoxItem == null) return;
            UICommonUtils.comboBox_addItem(comboBox, comboBoxItem);
        }

        public void addComboBoxItems(Combobox comboBox, ComboboxItem[] comboBoxItems) {
            if (comboBox == null || comboBoxItems == null) return;
            for (int i = 0; i < comboBoxItems.length; i++) addComboBoxItem(comboBox, comboBoxItems[i]);
        }

        public void removeComboBoxItem(Combobox comboBox, ComboboxItem comboBoxItem) {
            if (comboBox == null || comboBoxItem == null) return;
            UICommonUtils.comboBox_removeItem(comboBox, comboBoxItem);
        }

        public void removeComboBoxItems(Combobox comboBox, ComboboxItem[] comboBoxItems) {
            if (comboBox == null || comboBoxItems == null) return;
            for (int i = 0; i < comboBoxItems.length; i++) removeComboBoxItem(comboBox, comboBoxItems[i]);
        }

        public void removeAllComboBoxItems(Combobox comboBox) {
            if (comboBox == null) return;
            removeComboBoxItems(comboBox, comboBox.comboBoxItems.toArray(new ComboboxItem[]{}));
        }

        public boolean isItemSelected(Combobox comboBox, ComboboxItem comboBoxItem) {
            if (comboBox == null || comboBoxItem == null) return false;
            return comboBox.selectedItem != null ? comboBox.selectedItem == comboBoxItem : false;
        }

        public void selectItem(ComboboxItem selectItem) {
            if (selectItem == null) return;
            UICommonUtils.comboBox_selectItem(uiEngineState, selectItem);
        }

        public void open(Combobox comboBox) {
            if (comboBox == null) return;
            UICommonUtils.comboBox_open(uiEngineState, comboBox);
        }

        public void close(Combobox comboBox) {
            if (comboBox == null) return;
            UICommonUtils.comboBox_close(uiEngineState, comboBox);
        }

        public boolean isOpen(Combobox comboBox) {
            return UICommonUtils.comboBox_isOpen(uiEngineState, comboBox);
        }

        public void setSelectedItemByText(Combobox comboBox, String text) {
            if (comboBox == null || text == null) return;
            for (int i = 0; i < comboBox.comboBoxItems.size(); i++) {
                if (comboBox.comboBoxItems.get(i).text.equals(text)) {
                    UICommonUtils.comboBox_selectItem(uiEngineState, comboBox.comboBoxItems.get(i));
                    return;
                }
            }
        }

        public boolean isSelectedItemText(Combobox comboBox, String text) {
            if (comboBox == null || text == null) return false;
            return comboBox.selectedItem != null ? comboBox.selectedItem.text.equals(text) : false;
        }

        public final class APIComboboxItem {

            APIComboboxItem() {
            }

            private ComboBoxItemAction defaultComboBoxItemAction() {
                return new ComboBoxItemAction() {
                };
            }

            public ComboboxItem create(String text) {
                return create(text, defaultComboBoxItemAction(), null, 0);
            }

            public ComboboxItem create(String text, ComboBoxItemAction comboBoxItemAction) {
                return create(text, comboBoxItemAction, null, 0);
            }

            public ComboboxItem create(String text, ComboBoxItemAction comboBoxItemAction, CMediaSprite icon, int iconIndex) {
                ComboboxItem comboBoxItem = new ComboboxItem();
                comboBoxItem.text = Tools.Text.validString(text);
                comboBoxItem.font = uiConfig.component_defaultFont;
                comboBoxItem.color = new Color(uiConfig.component_defaultColor);
                comboBoxItem.icon = icon;
                comboBoxItem.iconIndex = Math.max(iconIndex, 0);
                comboBoxItem.comboBoxItemAction = comboBoxItemAction;
                comboBoxItem.name = "";
                comboBoxItem.data = null;
                return comboBoxItem;
            }

            public void setName(ComboboxItem comboBoxItem, String name) {
                if (comboBoxItem == null) return;
                comboBoxItem.name = Tools.Text.validString(name);
            }

            public void setData(ComboboxItem comboBoxItem, Object data) {
                if (comboBoxItem == null) return;
                comboBoxItem.data = data;
            }

            public void setColor(ComboboxItem comboBoxItem, Color color) {
                if (comboBoxItem == null || color == null) return;
                comboBoxItem.color.set(color);
            }

            public void setFont(ComboboxItem comboBoxItem, CMediaFont font) {
                if (comboBoxItem == null) return;
                comboBoxItem.font = font;
            }

            public void setComboBoxItemAction(ComboboxItem comboBoxItem, ComboBoxItemAction comboBoxItemAction) {
                if (comboBoxItem == null) return;
                comboBoxItem.comboBoxItemAction = comboBoxItemAction;
            }

            public void setText(ComboboxItem comboBoxItem, String text) {
                if (comboBoxItem == null) return;
                comboBoxItem.text = Tools.Text.validString(text);
            }

            public void setIcon(ComboboxItem comboBoxItem, CMediaSprite icon) {
                if (comboBoxItem == null) return;
                comboBoxItem.icon = icon;
            }

            public void setIconIndex(ComboboxItem comboBoxItem, int index) {
                if (comboBoxItem == null) return;
                comboBoxItem.iconIndex = Math.max(index, 0);
            }

        }
    }

    public final class APIScrollbar {

        public final APIScrollbarHorizontal scrollbarHorizontal;

        public final APIScrollbarVertical scrollbarVertical;

        APIScrollbar() {
            this.scrollbarHorizontal = new APIScrollbarHorizontal();
            this.scrollbarVertical = new APIScrollbarVertical();
        }

        private ScrollBarAction defaultScrollBarAction() {
            return new ScrollBarAction() {
            };
        }

        public final class APIScrollbarHorizontal {

            APIScrollbarHorizontal() {
            }

            public ScrollbarHorizontal create(int x, int y, int length) {
                return create(x, y, length, defaultScrollBarAction(), 0f);
            }

            public ScrollbarHorizontal create(int x, int y, int length, ScrollBarAction scrollBarAction) {
                return create(x, y, length, scrollBarAction, 0f);
            }

            public ScrollbarHorizontal create(int x, int y, int length, ScrollBarAction scrollBarAction, float scrolled) {
                ScrollbarHorizontal scrollBarHorizontal = new ScrollbarHorizontal();
                setComponentCommonInitValuesInternal(scrollBarHorizontal, x, y, length, 1);
                scrollBarHorizontal.scrollBarAction = scrollBarAction;
                scrollBarHorizontal.scrolled = Math.clamp(scrolled, 0f, 1f);
                return scrollBarHorizontal;
            }

        }

        public final class APIScrollbarVertical {

            APIScrollbarVertical() {
            }

            public ScrollbarVertical create(int x, int y, int length) {
                return create(x, y, length, defaultScrollBarAction(), 0f);
            }

            public ScrollbarVertical create(int x, int y, int length, ScrollBarAction scrollBarAction) {
                return create(x, y, length, scrollBarAction, 0f);
            }

            public ScrollbarVertical create(int x, int y, int length, ScrollBarAction scrollBarAction, float scrolled) {
                ScrollbarVertical scrollBarVertical = new ScrollbarVertical();
                setComponentCommonInitValuesInternal(scrollBarVertical, x, y, 1, length);
                scrollBarVertical.scrollBarAction = scrollBarAction;
                scrollBarVertical.scrolled = Math.clamp(scrolled, 0f, 1f);
                return scrollBarVertical;
            }

        }

        public void setScrolled(net.mslivo.core.engine.ui_engine.ui.components.scrollbar.Scrollbar scrollBar, float scrolled) {
            if (scrollBar == null) return;
            UICommonUtils.scrollBar_scroll(scrollBar, scrolled);
        }

        public void setScrollBarAction(net.mslivo.core.engine.ui_engine.ui.components.scrollbar.Scrollbar scrollBar, ScrollBarAction scrollBarAction) {
            if (scrollBar == null) return;
            scrollBar.scrollBarAction = scrollBarAction;
        }

    }

    public final class APIList {

        APIList() {
        }

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
            setComponentCommonInitValuesInternal(list, x, y, width, height);
            list.selectedItem = null;
            list.selectedItems = new HashSet<>();
            list.items = items;
            list.listAction = listAction;
            list.multiSelect = multiSelect;
            list.scrolled = 0f;
            list.dragEnabled = dragEnabled;
            list.dragInEnabled = dragInEnabled;
            list.dragOutEnabled = dragOutEnabled;
            list.font = uiConfig.component_defaultFont;
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
            UICommonUtils.list_scroll(list, scrolled);
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
            UICommonUtils.list_setMultiSelect(list, multiSelect);
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

    public void setToolTip(Component component, Tooltip tooltip) {
        if (component == null) return;
        component.toolTip = tooltip;
    }

    public void setPosition(Component component, int x, int y) {
        if (component == null) return;
        component.x = x;
        component.y = y;
    }

    public void setPositionGrid(Component component, int x, int y) {
        if (component == null) return;
        setPosition(component, x * api.TS(), y * api.TS());
    }

    public void moveX(Component[] components, int x) {
        if (components == null) return;
        for (int i = 0; i < components.length; i++) moveX(components[i], x);
    }

    public void moveX(Component component, int x) {
        if (component == null) return;
        setPosition(component, component.x + x, component.y);
    }

    public void moveY(Component[] components, int y) {
        if (components == null) return;
        for (int i = 0; i < components.length; i++) moveY(components[i], y);
    }

    public void moveY(Component component, int y) {
        if (component == null) return;
        setPosition(component, component.x, component.y + y);
    }

    public void move(Component[] components, int x, int y) {
        if (components == null) return;
        for (int i = 0; i < components.length; i++) move(components[i], x, y);
    }

    public void move(Component component, int x, int y) {
        if (component == null) return;
        setPosition(component, component.x + x, component.y + y);
    }

    public void setDisabled(Component component, boolean disabled) {
        if (component == null) return;
        UICommonUtils.component_setDisabled(uiEngineState, component, disabled);
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
        UICommonUtils.component_setSize(uiEngineState, component, width, height);
    }

    public void setColor(Component[] components, Color color) {
        if (components == null) return;
        for (int i = 0; i < components.length; i++) setColor(components[i], color);
    }

    public void setColor(Component component, Color color) {
        if (component == null || color == null) return;
        component.color.set(color);
    }

    public void setColor2(Component[] components, Color color2) {
        if (components == null) return;
        for (int i = 0; i < components.length; i++) setColor2(components[i], color2);
    }

    public void setColor2(Component component, Color color) {
        if (component == null || color == null) return;
        component.color2.set(color);
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

    public void setAlpha(Component component, float alpha) {
        if (component == null) return;
        component.color.set(component.color.r,component.color.g,component.color.b,alpha);
    }

    public void setAlpha(Component[] components, float alpha) {
        if (components == null) return;
        for (int i = 0; i < components.length; i++) setAlpha(components[i], alpha);
    }

    public void setVisible(Component component, boolean visible) {
        if (component == null) return;
        component.visible = visible;
    }

    public void setVisible(Component[] components, boolean visible) {
        if (components == null) return;
        for (int i = 0; i < components.length; i++) setVisible(components[i], visible);
    }

    public int absoluteX(Component component) {
        if (component == null) return 0;
        return UICommonUtils.component_getAbsoluteX(component);
    }

    public int absoluteY(Component component) {
        if (component == null) return 0;
        return UICommonUtils.component_getAbsoluteY(component);
    }

    public int realWidth(Component component) {
        if (component == null) return 0;
        return api.TS(component.width);
    }

    public int realHeight(Component component) {
        if (component == null) return 0;
        return api.TS(component.height);
    }

    public boolean isAddedToWindow(Component component, Window window) {
        if (component == null || window == null) return false;
        return component.addedToWindow != null && component.addedToWindow == window;
    }

    public boolean isAddedToScreen(Component component) {
        if (component == null) return false;
        return component.addedToScreen;
    }

    public void forceToolTipUpdate(Component component) {
        if (component == null) return;
        component.updateToolTip = true;
    }

    private void setComponentCommonInitValuesInternal(Component component, int x, int y, int width, int height) {
        setComponentCommonInitValuesInternal(component, x, y, width, height, uiConfig.component_defaultColor, uiConfig.component_defaultColor);
    }

    private void setComponentCommonInitValuesInternal(Component component, int x, int y, int width, int height, Color color) {
        setComponentCommonInitValuesInternal(component, x, y, width, height, color, color);
    }

    private void setComponentCommonInitValuesInternal(Component component, int x, int y, int width, int height, Color color1, Color color2) {
        // Align to grid per default
        component.x = (x * api.TS());
        component.y = (y * api.TS());
        component.width = width;
        component.height = height;
        component.color = new Color(color1);
        component.color2 = new Color(color2);
        component.disabled = false;
        component.updateActions = new ArrayList<>();
        component.data = null;
        component.name = "";
        component.visible = true;
        component.updateToolTip = false;
        component.addedToTab = null;
        component.addedToWindow = null;
        component.toolTip = null;
        component.addedToScreen = false;
    }

}
