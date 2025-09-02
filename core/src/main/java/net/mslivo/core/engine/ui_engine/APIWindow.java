package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
import net.mslivo.core.engine.ui_engine.ui.actions.WindowAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;
import net.mslivo.core.engine.ui_engine.ui.generator.*;
import net.mslivo.core.engine.ui_engine.ui.window.Window;

import java.util.function.Predicate;

public final class APIWindow {

    private final API api;
    private final UIEngineState uiEngineState;
    private final UIConfig uiConfig;
    private final MediaManager mediaManager;

    APIWindow(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.uiConfig = uiEngineState.config;
    }

    public final WindowAction DEFAULT_WINDOW_ACTION = new WindowAction() {
    };

    public Window create(int x, int y, int width, int height) {
        return create(x, y, width, height, "", DEFAULT_WINDOW_ACTION, false, true, true, true);
    }

    public Window create(int x, int y, int width, int height, String title) {
        return create(x, y, width, height, title, DEFAULT_WINDOW_ACTION, false, true, true, true);
    }

    public Window create(int x, int y, int width, int height, String title, WindowAction windowAction) {
        return create(x, y, width, height, title, windowAction, false, true, true, true);
    }

    public Window create(int x, int y, int width, int height, String title, WindowAction windowAction, boolean alwaysOnTop) {
        return create(x, y, width, height, title, windowAction, alwaysOnTop, true, true, true);
    }

    public Window create(int x, int y, int width, int height, String title, WindowAction windowAction, boolean alwaysOnTop, boolean moveAble) {
        return create(x, y, width, height, title, windowAction, alwaysOnTop, moveAble, true, true);
    }

    public Window create(int x, int y, int width, int height, String title, WindowAction windowAction, boolean alwaysOnTop, boolean moveAble, boolean hasTitleBar) {
        return create(x, y, width, height, title, windowAction, alwaysOnTop, moveAble, hasTitleBar, true);
    }

    public Window create(int x, int y, int width, int height, String title, WindowAction windowAction, boolean alwaysOnTop, boolean moveAble, boolean hasTitleBar, boolean visible) {
        Window window = new Window();
        window.x = x;
        window.y = y;
        window.width = Math.max(width, 2);
        window.height = Math.max(height, 2);
        window.title = Tools.Text.validString(title);
        window.alwaysOnTop = alwaysOnTop;
        window.moveAble = moveAble;
        window.color = new Color(uiConfig.window_defaultColor);
        window.fontColor = uiConfig.ui_font_defaultColor.cpy();
        window.hasTitleBar = hasTitleBar;
        window.visible = visible;
        window.windowAction = windowAction != null ? windowAction : DEFAULT_WINDOW_ACTION;
        window.name = "";
        window.data = null;
        window.enforceScreenBounds = uiConfig.window_defaultEnforceScreenBounds;
        window.updateActions = new Array<>();
        window.addedToScreen = false;
        window.components = new Array<>();
        return window;
    }

    public void setEnforceScreenBounds(Window window, boolean enforceScreenBounds) {
        if (window == null) return;
        window.enforceScreenBounds = enforceScreenBounds;
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
        window.windowAction = windowAction != null ? windowAction : DEFAULT_WINDOW_ACTION;
    }

    public boolean isAddedToScreen(Window window) {
        if (window == null) return false;
        return window.addedToScreen;
    }

    public void setColorEverything(Window window, Color color) {
        setColorInternal(window, color, 2, null,
                true, true, true, true);
    }

    public void setColorEverything(Window window, Color color, boolean windowColor, boolean componentColor1, boolean componentColor2, boolean comboBoxItems) {
        setColorInternal(window, color, 2, null,
                windowColor, componentColor1, componentColor2, comboBoxItems);
    }

    public void setColorEverythingExcept(Window window, Color color, Class[] exceptions) {
        setColorInternal(window, color, 2, exceptions,
                true, true, true, true);
    }

    public void setColorEverythingExcept(Window window, Color color, Class[] exceptions, boolean windowColor, boolean componentColor1, boolean componentColor2, boolean comboBoxItems) {
        setColorInternal(window, color, 2, exceptions,
                windowColor, componentColor1, componentColor2, comboBoxItems);
    }


    public void setColorEverythingInclude(Window window, Color color, Class[] inclusions) {
        setColorInternal(window, color, 1, inclusions,
                true, true, true, true);
    }

    public void setColorEverythingInclude(Window window, Color color, Class[] inclusions, boolean windowColor, boolean componentColor1, boolean componentColor2, boolean comboBoxItems) {
        setColorInternal(window, color, 1, inclusions,
                windowColor, componentColor1, componentColor2, comboBoxItems);
    }

    public int realWidth(Window window) {
        return api.TS(window.width);
    }

    public int realHeight(Window window) {
        return api.TS(window.height);
    }

    public void setColorEverythingInclude(Window window, Color color, Class[] inclusions, boolean setColor1, boolean setColor2, boolean includeWindow) {
        if (window == null) return;
        if (inclusions != null) {
            for (int i = 0; i < window.components.size; i++) {
                Component component = window.components.get(i);
                classLoop:
                for (int i2 = 0; i2 < inclusions.length; i2++) {
                    if (component.getClass() == inclusions[i2]) {
                        if (setColor1) api.component.setColor(component, color);
                        if (setColor2) api.component.setColor2(component, color);
                        break classLoop;
                    }
                }
            }
        }

        if (includeWindow) setColor(window, color);
    }

    public Window createFromGenerator(WindowGeneratorP0 windowGenerator) {
        if (windowGenerator == null) return null;
        return windowGenerator.createWindow(api);
    }

    public <P1> Window createFromGenerator(WindowGeneratorP1<P1> windowGenerator, P1 p1) {
        if (windowGenerator == null) return null;
        return windowGenerator.createWindow(api, p1);
    }

    public <P1, P2> Window createFromGenerator(WindowGeneratorP2<P1, P2> windowGenerator, P1 p1, P2 p2) {
        if (windowGenerator == null) return null;
        return windowGenerator.createWindow(api, p1, p2);
    }

    public <P1, P2, P3> Window createFromGenerator(WindowGeneratorP3<P1, P2, P3> windowGenerator, P1 p1, P2 p2, P3 p3) {
        if (windowGenerator == null) return null;
        return windowGenerator.createWindow(api, p1, p2, p3);
    }

    public <P1, P2, P3, P4> Window createFromGenerator(WindowGeneratorP4<P1, P2, P3, P4> windowGenerator, P1 p1, P2 p2, P3 p3, P4 p4) {
        if (windowGenerator == null) return null;
        return windowGenerator.createWindow(api, p1, p2, p3, p4);
    }

    public <P1, P2, P3, P4, P5> Window createFromGenerator(WindowGeneratorP5<P1, P2, P3, P4, P5> windowGenerator, P1 p1, P2 p2, P3 p3, P4 p4, P5 p5) {
        if (windowGenerator == null) return null;
        return windowGenerator.createWindow(api, p1, p2, p3, p4, p5);
    }

    public void addComponent(Window window, Component component) {
        if (window == null || component == null) return;
        UICommonUtils.component_addToWindow(component, uiEngineState, window);
    }

    public void addComponents(Window window, Component[] components) {
        if (window == null || components == null) return;
        for (int i = 0; i < components.length; i++) addComponent(window, components[i]);
    }

    public void moveComponentToTop(Component component) {
        if (component == null) return;
        UICommonUtils.component_windowMoveToTop(component, uiEngineState);
    }

    public void removeComponent(Window window, Component component) {
        if (window == null || component == null) return;
        UICommonUtils.component_removeFromWindow(component, window, uiEngineState);
    }

    public void removeComponents(Window window, Component[] components) {
        if (window == null || components == null) return;
        for (int i = 0; i < components.length; i++) removeComponent(window, components[i]);
    }

    public void removeAllComponents(Window window) {
        if (window == null) return;
        removeComponents(window, window.components.toArray(Component[]::new));
    }

    public Array<Component> findComponents(Window window, Predicate<Window> findBy) {
        if (window == null) return new Array<>();
        return UICommonUtils.findMultiple(window.components, findBy);
    }

    public Component findComponent(Window window, Predicate<Window> findBy) {
        if (window == null) return null;
        return UICommonUtils.find(window.components, findBy);
    }

    public void bringToFront(Window window) {
        if (window == null) return;
        UICommonUtils.window_bringToFront(uiEngineState, window);
    }

    public void center(Window window) {
        if (window == null) return;
        UICommonUtils.window_center(uiEngineState, window);
    }

    public void setFontColor(Window window, Color color) {
        if (window == null) return;
        window.fontColor.set(color);
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
        window.updateActions.removeValue(updateAction, true);
    }

    public void removeUpdateActions(Window window, UpdateAction[] updateActions) {
        if (window == null || updateActions == null) return;
        for (int i = 0; i < updateActions.length; i++) removeUpdateAction(window, updateActions[i]);
    }

    public void removeAllUpdateActions(Window window) {
        if (window == null) return;
        removeUpdateActions(window, window.updateActions.toArray(UpdateAction[]::new));
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
        window.color.set(color);
    }

    public void setAlpha(Window window, float alpha) {
        if (window == null) return;
        window.color.set(window.color.r, window.color.g, window.color.b, alpha);
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
        UICommonUtils.window_setPosition(uiEngineState, window, x, y);
    }

    public void setPositionGrid(Window window, int x, int y) {
        if (window == null) return;
        setPosition(window, x * api.TS(), y * api.TS());
    }

    public void move(Window window, int x, int y) {
        if (window == null) return;
        setPosition(window, window.x + x, window.y + y);
    }

    public void moveX(Window window, int x) {
        if (window == null) return;
        setPosition(window, window.x + x, window.y);
    }

    public void moveY(Window window, int y) {
        if (window == null) return;
        setPosition(window, window.x, window.y + y);
    }

    public void setSize(Window window, int width, int height) {
        if (window == null) return;
        window.width = Math.max(width, 2);
        window.height = Math.max(height, 2);
    }

    public void setTitle(Window window, String title) {
        if (window == null) return;
        window.title = Tools.Text.validString(title);
    }

    private void setColorInternal(Window window, Color color, int setColorMode, Class[] classes,
                                  boolean windowColor, boolean componentColor1, boolean componentColor2, boolean comboBoxItemColor) {
        if (classes == null) classes = new Class[]{};
        if (windowColor) setColor(window, color);
        for (int i = 0; i < window.components.size; i++) {
            Component c = window.components.get(i);
            boolean match = setColorMode == 1 ? false : true;
            classLoop:
            for (int i2 = 0; i2 < classes.length; i2++) {
                if (classes[i2] == c.getClass()) {
                    match = setColorMode == 1 ? true : false;
                    break classLoop;
                }
            }
            if (match) {
                if (componentColor1) api.component.setColor(c, color);
                if (componentColor2) api.component.setColor2(c, color);
            }
        }
    }

}
