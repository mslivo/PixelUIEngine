package org.vnna.core.engine.ui_engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import org.vnna.core.engine.media_manager.MediaManager;
import org.vnna.core.engine.media_manager.color.FColor;
import org.vnna.core.engine.media_manager.media.*;
import org.vnna.core.engine.tools.Tools;
import org.vnna.core.engine.ui_engine.gui.Window;
import org.vnna.core.engine.ui_engine.gui.actions.UpdateAction;
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
import org.vnna.core.engine.ui_engine.gui.components.progressbar.ProgressBar;
import org.vnna.core.engine.ui_engine.gui.components.scrollbar.ScrollBarHorizontal;
import org.vnna.core.engine.ui_engine.gui.components.scrollbar.ScrollBarVertical;
import org.vnna.core.engine.ui_engine.gui.components.shape.Shape;
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
import org.vnna.core.engine.ui_engine.gui.tooltip.ToolTip;
import org.vnna.core.engine.ui_engine.gui.tooltip.ToolTipImage;
import org.vnna.core.engine.ui_engine.input_processor.InputEvents;
import org.vnna.core.engine.ui_engine.input_processor.UIEngineInputProcessor;
import org.vnna.core.engine.ui_engine.media.GUIBaseMedia;
import org.vnna.core.engine.ui_engine.misc.ViewportMode;
import org.vnna.core.engine.ui_engine.render.PixelPerfectViewport;
import org.vnna.core.engine.ui_engine.render.shaders.GrayScaleShader;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Input and Render Engine
 * Handles Cameras, Hardware Systems, GUI and Drawing of GUI Elements
 * 2 External Hooks allow interpreting of hardware/gui to translate them to EngineInputs and Drawing of the GameState
 */
public class UIEngine<T extends UIAdapter> {

    private T uiAdapter;

    /* Input  */

    private InputState inputState;

    private API api;

    private long timer_windowHiddenChecker;

    public static final int TILE_SIZE = 8;


    /* Render */
    private MediaManager mediaManager;

    public T getAdapter() {
        return uiAdapter;
    }

    public UIEngine(T uiAdapter, MediaManager mediaManager, int internalResolutionWidth, int internalResolutionHeight, ViewportMode viewportMode) {
        if (uiAdapter == null || mediaManager == null) {
            throw new RuntimeException("Cannot initialize IREngine: invalid parameters");
        }
        this.uiAdapter = uiAdapter;
        this.mediaManager = mediaManager;


        /* Input Init */
        this.inputState = initializeInputState(internalResolutionWidth, internalResolutionHeight, viewportMode);
        this.api = new API(this.inputState, mediaManager);
        this.timer_windowHiddenChecker = System.currentTimeMillis();


        /* Hook Init */

        this.uiAdapter.init(this.api, this.mediaManager);
    }

    private int determineUpscaleFactor(int internalResolutionWidth, int internalResolutionHeight) {
        int upSampling = 1;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int testWidth = (int) (screenSize.getWidth());
        int testHeight = (int) (screenSize.getHeight());
        while ((internalResolutionWidth * upSampling) < testWidth && (internalResolutionHeight * upSampling) < testHeight) {
            upSampling++;
        }
        return upSampling;
    }

    private InputState initializeInputState(int internalResolutionWidth, int internalResolutionHeight, ViewportMode viewportMode) {
        InputState newInputState = new InputState();

        //  ----- Parameters

        newInputState.internalResolutionWidth = Tools.Calc.lowerBounds(internalResolutionWidth, TILE_SIZE * 2);
        newInputState.internalResolutionHeight = Tools.Calc.lowerBounds(internalResolutionHeight, TILE_SIZE * 2);
        newInputState.viewportMode = viewportMode;

        // -----  Game
        newInputState.spriteBatch_game = new SpriteBatch(8191);
        newInputState.spriteBatch_game.setBlendFunction(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        newInputState.camera_x = newInputState.camera_y = newInputState.camera_z = 0;
        newInputState.camera_zoom = 1f;
        newInputState.camera_width = newInputState.internalResolutionWidth;
        newInputState.camera_height = newInputState.internalResolutionHeight;
        newInputState.camera_game = new OrthographicCamera(newInputState.camera_width, newInputState.camera_height);
        newInputState.camera_game.setToOrtho(false, newInputState.camera_width, newInputState.camera_height);
        newInputState.camera_game.position.set(newInputState.camera_x, newInputState.camera_y, newInputState.camera_z);
        newInputState.camera_game.zoom = newInputState.camera_zoom;
        newInputState.frameBuffer_game = new FrameBuffer(Pixmap.Format.RGB888, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight, false);
        newInputState.frameBuffer_game.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        newInputState.texture_game = new TextureRegion(newInputState.frameBuffer_game.getColorBufferTexture());
        newInputState.texture_game.flip(false, true);
        newInputState.camera_frustum = new OrthographicCamera(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.camera_frustum.setToOrtho(false, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        // -----  GUI
        newInputState.spriteBatch_gui = new SpriteBatch(8191);
        newInputState.spriteBatch_gui.setBlendFunctionSeparate(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA, GL30.GL_ONE, GL30.GL_ONE_MINUS_SRC_ALPHA);
        newInputState.camera_gui = new OrthographicCamera(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.camera_gui.setToOrtho(false, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.frameBuffer_gui = new FrameBuffer(Pixmap.Format.RGBA8888, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight, false);
        newInputState.frameBuffer_gui.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        newInputState.texture_gui = new TextureRegion(newInputState.frameBuffer_gui.getColorBufferTexture());
        newInputState.texture_gui.flip(false, true);
        // ----- UpScaler
        newInputState.factor_upScale = determineUpscaleFactor(internalResolutionWidth, internalResolutionHeight);
        newInputState.frameBuffer_upScale = new FrameBuffer(Pixmap.Format.RGBA8888, newInputState.internalResolutionWidth * newInputState.factor_upScale, newInputState.internalResolutionHeight * newInputState.factor_upScale, false);
        newInputState.frameBuffer_upScale.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        newInputState.texture_upScale = new TextureRegion(newInputState.frameBuffer_upScale.getColorBufferTexture());
        newInputState.texture_upScale.flip(false, true);
        // ----- Screen
        newInputState.spriteBatch_screen = new SpriteBatch(1);
        newInputState.camera_screen = new OrthographicCamera(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.camera_screen.setToOrtho(false);
        newInputState.viewport_screen = switch (viewportMode) {
            case FIT ->
                    new FitViewport(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight, newInputState.camera_screen);
            case PIXEL_PERFECT ->
                    new PixelPerfectViewport(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight, newInputState.camera_screen, 1);
            case STRETCH ->
                    new StretchViewport(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight, newInputState.camera_screen);
        };
        newInputState.viewport_screen.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        // -----  Input


        newInputState.inputEvents = new InputEvents();
        newInputState.inputProcessor = new UIEngineInputProcessor(newInputState.inputEvents);
        Gdx.input.setInputProcessor(newInputState.inputProcessor);


        // -----  GUI
        newInputState.windows = new ArrayList<>();

        newInputState.screenComponents = new ArrayList<>();
        newInputState.addWindowQueue = new ArrayDeque<>();
        newInputState.removeWindowQueue = new ArrayDeque<>();
        newInputState.addScreenComponentsQueue = new ArrayDeque<>();
        newInputState.removeScreenComponentsQueue = new ArrayDeque<>();
        newInputState.addHotKeyQueue = new ArrayDeque<>();
        newInputState.removeHotKeyQueue = new ArrayDeque<>();
        newInputState.displayedContextMenu = null;
        newInputState.displayedContextMenuWidth = 0;
        newInputState.modalWindow = null;
        newInputState.modalWindowQueue = new ArrayDeque<>();
        newInputState.focusedTextField = null;
        newInputState.notifications = new ArrayList<>();
        newInputState.hotKeys = new ArrayList<>();
        newInputState.guiFrozen = false;
        newInputState.gameViewPorts = new ArrayList<>();
        newInputState.delayedOneshotActions = new ArrayList<>();
        // ----- Temp GUI Variables
        newInputState.draggedWindow = null;
        newInputState.pressedButton = null;
        newInputState.pressedButton_timer_hold = 0;
        newInputState.turnedKnob = null;
        newInputState.tooltip = null;
        newInputState.tooltip_fadeIn_pct = 0f;
        newInputState.tooltip_wait_delay = false;
        newInputState.tooltip_delay_timer = 0;
        newInputState.tooltip_fadeIn_timer = 0;
        newInputState.scrolledScrollBarVertical = null;
        newInputState.scrolledScrollBarHorizontal = null;
        newInputState.inventoryDrag_Item = null;
        newInputState.inventoryDrag_Inventory = null;
        newInputState.InventoryDrag_offset_x = newInputState.InventoryDrag_offset_y = 0;
        newInputState.inventoryDrag_from_x = newInputState.inventoryDrag_from_y = 0;
        newInputState.listDrag_Item = null;
        newInputState.listDrag_List = null;
        newInputState.listDrag_offset_x = newInputState.listDrag_offset_y = 0;
        newInputState.listDrag_from_index = 0;
        newInputState.tooltip_lastHoverObject = null;
        newInputState.pressedMap = null;
        newInputState.hotKeyPressedKeys = new boolean[256];
        newInputState.pressedMouseTool = null;
        newInputState.openComboBox = null;

        // ----- Mouse
        newInputState.mouse_x_gui = newInputState.mouse_y_gui = 0;
        newInputState.mouse_x = newInputState.mouse_y = 0;
        newInputState.mouse_x_delta = newInputState.mouse_y_delta = 0;
        newInputState.lastGUIMouseHover = null;
        newInputState.cursor_setNext = null;
        newInputState.cursor_current = null;
        newInputState.mouseTool = null;
        newInputState.vector_fboCursor = new Vector3(0, 0, 0);
        newInputState.vector2_unproject = new Vector2(0, 0);
        // -----  Other
        ShaderProgram.pedantic = false;
        newInputState.grayScaleShader = new ShaderProgram(GrayScaleShader.VERTEX, GrayScaleShader.FRAGMENT);

        newInputState.tempColorStack = new Color[8];
        for (int i = 0; i < 8; i++) newInputState.tempColorStack[i] = new Color(1, 1, 1, 1);
        newInputState.tempColorStackPointer = 0;
        ScreenUtils.clear(0, 0, 0, 1);
        return newInputState;
    }

    public void resize(int width, int height) {
        inputState.viewport_screen.update(width, height, true);
    }

    public void update() {
        this.updateMouse();
        this.updateLastGUIMouseHover();
        if (!this.inputState.guiFrozen) this.updateGUI(); // GUI specific updates

        this.uiAdapter.update();

        this.updateGameCamera();
        this.updateMouseCursor();
        this.inputState.inputEvents.reset();
    }

    private void updateGUI() {

        /* Key Action */
        if (inputState.inputEvents.keyTyped) {
            if (inputState.focusedTextField != null) {
                for (char key : inputState.inputEvents.keysTyped) {

                    if (inputState.focusedTextField.textFieldAction != null)
                        inputState.focusedTextField.textFieldAction.onTyped(key);

                    if (key == '\b') { // BACKSPACE
                        if (!inputState.focusedTextField.content.isEmpty() && inputState.focusedTextField.markerPosition > 0) {

                            String newContent = inputState.focusedTextField.content.substring(0, inputState.focusedTextField.markerPosition - 1) + inputState.focusedTextField.content.substring(inputState.focusedTextField.markerPosition);
                            UICommons.textfield_setMarkerPosition(mediaManager, inputState.focusedTextField, inputState.focusedTextField.markerPosition - 1);
                            UICommons.textfield_setContent(inputState.focusedTextField, newContent);

                            if (inputState.focusedTextField.textFieldAction != null) {
                                inputState.focusedTextField.contentValid = inputState.focusedTextField.textFieldAction.isContentValid(newContent);
                                inputState.focusedTextField.textFieldAction.onContentChange(newContent, inputState.focusedTextField.contentValid);
                            }
                        }
                    } else if (key == '\u007F') { // DEL
                        if (!inputState.focusedTextField.content.isEmpty() && inputState.focusedTextField.markerPosition < inputState.focusedTextField.content.length()) {

                            String newContent = inputState.focusedTextField.content.substring(0, inputState.focusedTextField.markerPosition) + inputState.focusedTextField.content.substring(inputState.focusedTextField.markerPosition + 1);

                            UICommons.textfield_setContent(inputState.focusedTextField, newContent);

                            if (inputState.focusedTextField.textFieldAction != null) {
                                inputState.focusedTextField.contentValid = inputState.focusedTextField.textFieldAction.isContentValid(newContent);
                                inputState.focusedTextField.textFieldAction.onContentChange(newContent, inputState.focusedTextField.contentValid);
                            }
                        }
                    } else if (key == '\n') { // ENTER
                        inputState.focusedTextField.focused = false;
                        if (inputState.focusedTextField.textFieldAction != null) {
                            inputState.focusedTextField.textFieldAction.onEnter(inputState.focusedTextField.content, inputState.focusedTextField.contentValid);
                        }
                        inputState.focusedTextField = null;
                    } else {
                        if (inputState.focusedTextField.allowedCharacters == null || inputState.focusedTextField.allowedCharacters.contains(key)) {
                            if (inputState.focusedTextField.content.length() < inputState.focusedTextField.contentMaxLength) {
                                String newContent = inputState.focusedTextField.content.substring(0, inputState.focusedTextField.markerPosition) + key + inputState.focusedTextField.content.substring(inputState.focusedTextField.markerPosition);
                                UICommons.textfield_setContent(inputState.focusedTextField, newContent);
                                UICommons.textfield_setMarkerPosition(mediaManager, inputState.focusedTextField, inputState.focusedTextField.markerPosition + 1);
                                if (inputState.focusedTextField.textFieldAction != null) {
                                    inputState.focusedTextField.contentValid = inputState.focusedTextField.textFieldAction.isContentValid(newContent);
                                    inputState.focusedTextField.textFieldAction.onContentChange(newContent, inputState.focusedTextField.contentValid);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (inputState.inputEvents.keyDown) {
            boolean processKey = true;
            if (inputState.modalWindow != null) {
                if (inputState.focusedTextField != null) {
                    if (inputState.focusedTextField.addedToWindow != inputState.modalWindow) processKey = false;
                } else {
                    processKey = false;
                }
            }

            if (processKey) {
                if (inputState.focusedTextField != null) {
                    for (int keyCode : inputState.inputEvents.keyCodesDown) {
                        if (keyCode == Input.Keys.LEFT) {
                            UICommons.textfield_setMarkerPosition(mediaManager, inputState.focusedTextField, inputState.focusedTextField.markerPosition - 1);
                        } else if (keyCode == Input.Keys.RIGHT) {
                            UICommons.textfield_setMarkerPosition(mediaManager, inputState.focusedTextField, inputState.focusedTextField.markerPosition + 1);
                        } else if (keyCode == Input.Keys.HOME) {
                            UICommons.textfield_setMarkerPosition(mediaManager, inputState.focusedTextField, inputState.focusedTextField.content.length());
                        } else if (keyCode == Input.Keys.END) {
                            UICommons.textfield_setMarkerPosition(mediaManager, inputState.focusedTextField, 0);
                        }
                    }
                } else {
                    // Hotkeys
                    for (int keyCode : inputState.inputEvents.keyCodesDown) {
                        inputState.hotKeyPressedKeys[keyCode] = true;
                    }
                    hkLoop:
                    for (HotKey hotKey : inputState.hotKeys) {
                        boolean hotkeyPressed = true;
                        kcLoop:
                        for (int hotKeyCode : hotKey.keyCodes) {
                            if (!inputState.hotKeyPressedKeys[hotKeyCode]) {
                                hotkeyPressed = false;
                                break kcLoop;
                            }
                        }
                        if (hotkeyPressed) {
                            hotKey.pressed = true;
                            if (hotKey.hotKeyAction != null) hotKey.hotKeyAction.onPress();
                        }
                    }
                }
            }


        }

        if (inputState.inputEvents.keyUp) {
            // Reset Hotkeys
            for (int keyCode : inputState.inputEvents.keyCodesUp) {
                inputState.hotKeyPressedKeys[keyCode] = false;
            }
            hkLoop:
            for (HotKey hotKey : inputState.hotKeys) {
                if (hotKey.pressed) {
                    kcLoop:
                    for (int keyCode : hotKey.keyCodes) {
                        if (inputState.hotKeyPressedKeys[keyCode] == false) {
                            hotKey.pressed = false;
                            if (hotKey.hotKeyAction != null) hotKey.hotKeyAction.onRelease();
                            break kcLoop;
                        }
                    }
                }
            }
        }

        /* Mouse Action */
        if (inputState.inputEvents.mouseDoubleClick) {
            boolean processMouseClick = true;
            if (inputState.lastGUIMouseHover != null) {
                if (inputState.modalWindow != null && inputState.lastGUIMouseHover != inputState.modalWindow) {
                    processMouseClick = false;
                }
            } else {
                // Tool
                if (inputState.mouseTool != null && inputState.mouseTool.mouseToolAction != null) {
                    inputState.mouseTool.mouseToolAction.onDoubleClick(inputState.inputEvents.mouseButton, inputState.mouse_x, inputState.mouse_y);
                }
                processMouseClick = false;
            }

            if (processMouseClick) {
                if (inputState.lastGUIMouseHover.getClass() == Window.class) {
                    Window window = (Window) inputState.lastGUIMouseHover;
                    if (api.config.isFoldWindowsOnDoubleClick() && inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        if (window.hasTitleBar && Tools.Calc.pointRectsCollide(inputState.mouse_x_gui, inputState.mouse_y_gui, window.x, window.y + ((window.height - 1) * TILE_SIZE), UICommons.window_getRealWidth(window), TILE_SIZE)) {
                            window.folded = !window.folded;
                            if (window.windowAction != null) {
                                if (window.folded) {
                                    window.windowAction.onFold();
                                } else {
                                    window.windowAction.onUnfold();
                                }
                            }
                        }
                    }
                    if (window.windowAction != null)
                        window.windowAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover instanceof Button) {
                    Button button = (Button) inputState.lastGUIMouseHover;
                    if (button.buttonAction != null) {
                        button.buttonAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == CheckBox.class) {
                    CheckBox checkBox = (CheckBox) inputState.lastGUIMouseHover;
                    if (checkBox.checkBoxAction != null) {
                        checkBox.checkBoxAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);

                    }
                } else if (inputState.lastGUIMouseHover.getClass() == ComboBox.class) {
                    ComboBox comboBox = (ComboBox) inputState.lastGUIMouseHover;
                    if (comboBox.comboBoxAction != null) {
                        comboBox.comboBoxAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == GameViewPort.class) {
                    GameViewPort gameViewPort = (GameViewPort) inputState.lastGUIMouseHover;
                    if (gameViewPort.gameViewPortAction != null) {
                        gameViewPort.gameViewPortAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == Inventory.class) {
                    Inventory inventory = (Inventory) inputState.lastGUIMouseHover;
                    if (inventory.inventoryAction != null) {
                        inventory.inventoryAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == Knob.class) {
                    Knob knob = (Knob) inputState.lastGUIMouseHover;
                    if (knob.knobAction != null) {
                        knob.knobAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == List.class) {
                    List list = (List) inputState.lastGUIMouseHover;
                    if (list.listAction != null) {
                        list.listAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == Map.class) {
                    Map map = (Map) inputState.lastGUIMouseHover;
                    if (map.mapAction != null) {
                        map.mapAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == ScrollBarHorizontal.class) {
                    ScrollBarHorizontal scrollBarHorizontal = (ScrollBarHorizontal) inputState.lastGUIMouseHover;
                    if (scrollBarHorizontal.scrollBarAction != null) {
                        scrollBarHorizontal.scrollBarAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == ScrollBarVertical.class) {
                    ScrollBarVertical scrollBarVertical = (ScrollBarVertical) inputState.lastGUIMouseHover;
                    if (scrollBarVertical.scrollBarAction != null) {
                        scrollBarVertical.scrollBarAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == TabBar.class) {
                    TabBar tabBar = (TabBar) inputState.lastGUIMouseHover;
                    if (tabBar.tabBarAction != null) {
                        tabBar.tabBarAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == Image.class) {
                    Image image = (Image) inputState.lastGUIMouseHover;
                    if (image.imageAction != null)
                        image.imageAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);

                } else if (inputState.lastGUIMouseHover.getClass() == Text.class) {
                    Text text = (Text) inputState.lastGUIMouseHover;
                    if (text.textAction != null) text.textAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);

                } else if (inputState.lastGUIMouseHover.getClass() == Notification.class) {
                    Notification notification = (Notification) inputState.lastGUIMouseHover;
                    if (notification.notificationAction != null) {
                        notification.notificationAction.onMouseDoubleClick(inputState.inputEvents.mouseButton);
                    }
                }
            }
        }
        if (inputState.inputEvents.mouseDown) {
            boolean processMouseClick = true;
            /* Modal ? */
            if (inputState.lastGUIMouseHover != null) {
                if (inputState.modalWindow != null) {
                    /* Modal Active? */
                    if (inputState.lastGUIMouseHover.getClass() == Window.class) {
                        Window window = (Window) inputState.lastGUIMouseHover;
                        if (window != inputState.modalWindow) processMouseClick = false;
                    } else if (inputState.lastGUIMouseHover instanceof Component) {
                        Component component = (Component) inputState.lastGUIMouseHover;
                        if (component.addedToWindow == null) {
                            processMouseClick = false;
                        } else if (component.addedToWindow != inputState.modalWindow) {
                            processMouseClick = false;
                        }
                    }
                } else {
                    /* Hidden ? */
                    if (inputState.lastGUIMouseHover.getClass() == Window.class) {
                        Window window = (Window) inputState.lastGUIMouseHover;
                        if (!window.visible) processMouseClick = false;
                    } else if (inputState.lastGUIMouseHover instanceof Component) {
                        Component component = (Component) inputState.lastGUIMouseHover;
                        if (component.addedToWindow != null && !component.addedToWindow.visible)
                            processMouseClick = false;
                    }
                }

            } else {
                // Tool
                if (inputState.mouseTool != null && inputState.mouseTool.mouseToolAction != null) {
                    inputState.mouseTool.mouseToolAction.onClick(inputState.inputEvents.mouseButton, inputState.mouse_x, inputState.mouse_y);
                    inputState.pressedMouseTool = inputState.mouseTool;
                }
                processMouseClick = false;
            }

            if (processMouseClick) {
                Window moveWindow = null;
                if (inputState.lastGUIMouseHover.getClass() == Window.class) {
                    Window window = (Window) inputState.lastGUIMouseHover;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        if (window.moveAble) moveWindow = window;
                    }
                    if (window.windowAction != null)
                        window.windowAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == ContextMenuItem.class) {
                    ContextMenuItem contextMenuItem = (ContextMenuItem) inputState.lastGUIMouseHover;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        if (contextMenuItem.contextMenuItemAction != null) {
                            contextMenuItem.contextMenuItemAction.onSelect();
                            inputState.displayedContextMenu = null;
                        }
                    }
                } else if (inputState.lastGUIMouseHover instanceof Button) {
                    Button button = (Button) inputState.lastGUIMouseHover;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        inputState.pressedButton = button;

                        if (button.toggleMode) {
                            inputState.pressedButton.pressed = !inputState.pressedButton.pressed;
                        } else {
                            inputState.pressedButton.pressed = true;
                        }
                        if (button.buttonAction != null) {
                            button.buttonAction.onPress();
                            if (button.toggleMode) {
                                button.buttonAction.onToggle(inputState.pressedButton.pressed);
                            }
                            inputState.pressedButton_timer_hold = 0;
                        }
                    }
                    if (button.buttonAction != null)
                        button.buttonAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == ScrollBarVertical.class) {
                    ScrollBarVertical scrollBarVertical = (ScrollBarVertical) inputState.lastGUIMouseHover;

                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        inputState.scrolledScrollBarVertical = scrollBarVertical;
                        inputState.scrolledScrollBarVertical.buttonPressed = true;
                        if (inputState.scrolledScrollBarVertical.scrollBarAction != null)
                            inputState.scrolledScrollBarVertical.scrollBarAction.onPress();
                    }
                    if (scrollBarVertical.scrollBarAction != null)
                        scrollBarVertical.scrollBarAction.onMouseClick(inputState.inputEvents.mouseButton);

                } else if (inputState.lastGUIMouseHover.getClass() == ScrollBarHorizontal.class) {
                    ScrollBarHorizontal scrollBarHorizontal = (ScrollBarHorizontal) inputState.lastGUIMouseHover;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        inputState.scrolledScrollBarHorizontal = scrollBarHorizontal;
                        inputState.scrolledScrollBarHorizontal.buttonPressed = true;
                        if (inputState.scrolledScrollBarHorizontal.scrollBarAction != null)
                            inputState.scrolledScrollBarHorizontal.scrollBarAction.onPress();
                    }
                    if (scrollBarHorizontal.scrollBarAction != null)
                        scrollBarHorizontal.scrollBarAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == List.class) {
                    List list = (List) inputState.lastGUIMouseHover;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        int[] itemInfo = list_getInfoAtPointer(list);
                        Object item = null;
                        if (itemInfo != null) item = list.items.get(itemInfo[0]);
                        if (item != null) {
                            if (list.multiSelect) {
                                if (list.selectedItems.contains(item)) {
                                    list.selectedItems.remove(item);
                                } else {
                                    list.selectedItems.add(item);
                                }
                                if (list.listAction != null) list.listAction.onItemsSelected(list.selectedItems);
                            } else {
                                list.selectedItem = item;
                                if (list.listAction != null) list.listAction.onItemSelected(list.selectedItem);
                            }
                            if (list.dragEnabled) {
                                inputState.listDrag_from_index = itemInfo[0];
                                inputState.listDrag_offset_x = inputState.mouse_x_gui - (UICommons.component_getAbsoluteX(list));
                                inputState.listDrag_offset_y = inputState.mouse_y_gui - (UICommons.component_getAbsoluteY(list) + (itemInfo[1] * TILE_SIZE));
                                inputState.listDrag_Item = item;
                                inputState.listDrag_List = list;
                            }
                        } else {
                            if (list.multiSelect) {
                                list.selectedItems.clear();
                            } else {
                                list.selectedItem = null;
                            }
                            if (list.listAction != null) list.listAction.onItemSelected(null);
                        }
                    }
                    if (list.listAction != null)
                        list.listAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == ComboBox.class) {
                    ComboBox combobox = (ComboBox) inputState.lastGUIMouseHover;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        if (combobox.menuOpen) {
                            for (int h = 0; h < combobox.items.size(); h++) {
                                if (Tools.Calc.pointRectsCollide(inputState.mouse_x_gui, inputState.mouse_y_gui,
                                        UICommons.component_getParentWindowX(combobox) + (combobox.x * TILE_SIZE) + combobox.offset_x,
                                        UICommons.component_getParentWindowY(combobox) + (combobox.y * TILE_SIZE) + combobox.offset_y - (h * TILE_SIZE) - TILE_SIZE,
                                        combobox.width * TILE_SIZE,
                                        TILE_SIZE
                                )) {
                                    if (combobox.comboBoxAction != null) {
                                        Object item = combobox.items.get(h);
                                        combobox.selectedItem = item;
                                        combobox.comboBoxAction.onItemSelected(item);
                                    }
                                }
                            }
                            combobox.menuOpen = false;
                            if (combobox.comboBoxAction != null) combobox.comboBoxAction.onClose();
                        } else {
                            if (inputState.openComboBox != null) {
                                inputState.openComboBox.menuOpen = false;
                                inputState.openComboBox = null;
                            }
                            combobox.menuOpen = true;
                            inputState.openComboBox = combobox;
                            if (combobox.comboBoxAction != null) combobox.comboBoxAction.onOpen();
                        }
                    }
                    if (combobox.comboBoxAction != null)
                        combobox.comboBoxAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == Knob.class) {
                    Knob knob = (Knob) inputState.lastGUIMouseHover;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        inputState.turnedKnob = knob;
                        if (knob.knobAction != null) knob.knobAction.onPress();
                    }
                    if (knob.knobAction != null) knob.knobAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == Map.class) {
                    Map map = (Map) inputState.lastGUIMouseHover;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        int x = inputState.mouse_x_gui - (UICommons.component_getParentWindowX(map) + (map.x * TILE_SIZE) + map.offset_x);
                        int y = inputState.mouse_y_gui - (UICommons.component_getParentWindowY(map) + (map.y * TILE_SIZE) + map.offset_y);
                        if (map.mapAction != null) map.mapAction.onPress(x, y);
                        inputState.pressedMap = map;
                    }
                    if (map.mapAction != null) map.mapAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == GameViewPort.class) {
                    GameViewPort gameViewPort = (GameViewPort) inputState.lastGUIMouseHover;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        int x = inputState.mouse_x_gui - (UICommons.component_getParentWindowX(gameViewPort) + (gameViewPort.x * TILE_SIZE) + gameViewPort.offset_x);
                        int y = inputState.mouse_y_gui - (UICommons.component_getParentWindowY(gameViewPort) + (gameViewPort.y * TILE_SIZE) + gameViewPort.offset_y);

                        if (gameViewPort.gameViewPortAction != null) {
                            gameViewPort.gameViewPortAction.onPress(x, y);
                        }
                        inputState.pressedGameViewPort = gameViewPort;
                    }
                    if (gameViewPort.gameViewPortAction != null)
                        gameViewPort.gameViewPortAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == TextField.class) {
                    TextField textField = (TextField) inputState.lastGUIMouseHover;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        if (inputState.focusedTextField != null && inputState.focusedTextField != textField) {
                            inputState.focusedTextField.focused = false;
                            if (inputState.focusedTextField.textFieldAction != null)
                                inputState.focusedTextField.textFieldAction.onUnFocus();
                        }
                        inputState.focusedTextField = textField;
                        inputState.focusedTextField.focused = true;
                        if (textField.textFieldAction != null) textField.textFieldAction.onFocus();

                        // Set Marker to mouse position
                        int mouseX = inputState.mouse_x_gui - UICommons.component_getAbsoluteX(inputState.focusedTextField);
                        char[] fieldContent = inputState.focusedTextField.content.substring(inputState.focusedTextField.offset).toCharArray();
                        String testString = "";
                        boolean found = false;
                        charLoop:
                        for (int i = 0; i < fieldContent.length; i++) {
                            testString += fieldContent[i];
                            if (mediaManager.textWidth(inputState.focusedTextField.font, testString) > mouseX) {
                                UICommons.textfield_setMarkerPosition(mediaManager, inputState.focusedTextField,
                                        inputState.focusedTextField.offset + i);
                                found = true;
                                break charLoop;
                            }
                        }
                        if (!found) {
                            // Set to end
                            UICommons.textfield_setMarkerPosition(mediaManager, inputState.focusedTextField,
                                    inputState.focusedTextField.offset + fieldContent.length);
                        }
                    }
                    if (textField.textFieldAction != null)
                        textField.textFieldAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == Inventory.class) {
                    Inventory inventory = (Inventory) inputState.lastGUIMouseHover;
                    int tileSize = inventory.doubleSized ? TILE_SIZE * 2 : TILE_SIZE;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        int x_inventory = UICommons.component_getAbsoluteX(inventory);
                        int y_inventory = UICommons.component_getAbsoluteY(inventory);
                        int inv_x = (inputState.mouse_x_gui - x_inventory) / tileSize;
                        int inv_y = (inputState.mouse_y_gui - y_inventory) / tileSize;
                        if (UICommons.inventory_positionValid(inventory, inv_x, inv_y)) {
                            inventory.selectedItem = inventory.items[inv_x][inv_y];
                            if (inventory.inventoryAction != null) {
                                inventory.inventoryAction.onItemSelected(inventory.items[inv_x][inv_y], inv_x, inv_y);
                            }
                            if (inventory.dragEnabled) {
                                inputState.inventoryDrag_from_x = inv_x;
                                inputState.inventoryDrag_from_y = inv_y;
                                inputState.InventoryDrag_offset_x = inputState.mouse_x_gui - (x_inventory + (inv_x * tileSize));
                                inputState.InventoryDrag_offset_y = inputState.mouse_y_gui - (y_inventory + (inv_y * tileSize));
                                inputState.inventoryDrag_Item = inventory.items[inv_x][inv_y];
                                inputState.inventoryDrag_Inventory = inventory;
                            }
                        }
                    }
                    if (inventory.inventoryAction != null)
                        inventory.inventoryAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == TabBar.class) {
                    TabBar tabBar = (TabBar) inputState.lastGUIMouseHover;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        Integer selectedTab = tabBar_getInfoAtPointer(tabBar);

                        if (selectedTab != null && tabBar.selectedTab != selectedTab) {
                            Tab currentTab = UICommons.tabBar_getSelectedTab(tabBar);
                            for (Component component : currentTab.components) {
                                if (component.getClass() == ComboBox.class) {
                                    ComboBox comboBox = (ComboBox) component;
                                    comboBox.menuOpen = false;
                                }
                            }

                            Tab newTab = tabBar.tabs.get(selectedTab);
                            UICommons.tabBar_selectTab(tabBar, selectedTab);
                            if (newTab.tabAction != null) newTab.tabAction.onSelect();
                            if (tabBar.tabBarAction != null) tabBar.tabBarAction.onChangeTab(selectedTab);
                        }
                    }
                    if (tabBar.tabBarAction != null)
                        tabBar.tabBarAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == CheckBox.class) {
                    CheckBox checkBox = (CheckBox) inputState.lastGUIMouseHover;
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        checkBox.checked = !checkBox.checked;
                        if (checkBox.checkBoxAction != null) checkBox.checkBoxAction.onCheck(checkBox.checked);
                    }
                    if (checkBox.checkBoxAction != null)
                        checkBox.checkBoxAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == Image.class) {
                    Image image = (Image) inputState.lastGUIMouseHover;
                    if (image.imageAction != null) image.imageAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == Text.class) {
                    Text text = (Text) inputState.lastGUIMouseHover;
                    if (text.textAction != null) text.textAction.onMouseClick(inputState.inputEvents.mouseButton);
                } else if (inputState.lastGUIMouseHover.getClass() == Notification.class) {
                    Notification notification = (Notification) inputState.lastGUIMouseHover;
                    if (notification.notificationAction != null) {
                        notification.notificationAction.onMouseClick(inputState.inputEvents.mouseButton);
                    }
                } else if (inputState.lastGUIMouseHover instanceof Component) {
                    // Move window below if nothing else
                    if (inputState.inputEvents.mouseButton == Input.Buttons.LEFT) {
                        Component component = (Component) inputState.lastGUIMouseHover;
                        if (component.addedToWindow != null) moveWindow = component.addedToWindow;
                    }
                }

                if (moveWindow != null) {
                    inputState.draggedWindow = moveWindow;
                    inputState.draggedWindow_x_offset = inputState.mouse_x_gui - inputState.draggedWindow.x;
                    inputState.draggedWindow_y_offset = inputState.mouse_y_gui - inputState.draggedWindow.y;
                    // Move on top ?
                    UICommons.window_bringToFront(inputState, inputState.draggedWindow);
                }


            }

            // close contextmenu
            if (inputState.displayedContextMenu != null) {
                inputState.displayedContextMenu = null;
            }
            if (inputState.focusedTextField != null && inputState.lastGUIMouseHover != inputState.focusedTextField) {
                inputState.focusedTextField.focused = false;
                if (inputState.focusedTextField.textFieldAction != null)
                    inputState.focusedTextField.textFieldAction.onUnFocus();
                inputState.focusedTextField = null;
            }


        }
        if (inputState.inputEvents.mouseUp) {
            if (inputState.draggedWindow != null) {
                inputState.draggedWindow = null;
                inputState.draggedWindow_x_offset = 0;
                inputState.draggedWindow_y_offset = 0;
            }
            if (inputState.pressedMap != null) {
                if (inputState.pressedMap.mapAction != null) inputState.pressedMap.mapAction.onRelease();
                inputState.pressedMap = null;
            }
            if (inputState.pressedGameViewPort != null) {
                if (inputState.pressedGameViewPort.gameViewPortAction != null)
                    inputState.pressedGameViewPort.gameViewPortAction.onRelease();
                inputState.pressedGameViewPort = null;
            }
            if (inputState.pressedButton != null) {
                if (!inputState.pressedButton.toggleMode)
                    inputState.pressedButton.pressed = false;
                if (inputState.pressedButton.buttonAction != null) inputState.pressedButton.buttonAction.onRelease();
                inputState.pressedButton = null;
            }
            if (inputState.scrolledScrollBarVertical != null) {
                inputState.scrolledScrollBarVertical.buttonPressed = false;
                if (inputState.scrolledScrollBarVertical.scrollBarAction != null)
                    inputState.scrolledScrollBarVertical.scrollBarAction.onRelease();

                inputState.scrolledScrollBarVertical = null;
            }
            if (inputState.scrolledScrollBarHorizontal != null) {
                inputState.scrolledScrollBarHorizontal.buttonPressed = false;
                if (inputState.scrolledScrollBarHorizontal.scrollBarAction != null)
                    inputState.scrolledScrollBarHorizontal.scrollBarAction.onRelease();
                inputState.scrolledScrollBarHorizontal = null;
            }
            if (inputState.turnedKnob != null) {
                if (inputState.turnedKnob.knobAction != null)
                    inputState.turnedKnob.knobAction.onRelease();
                inputState.turnedKnob = null;
            }
            if (inputState.inventoryDrag_Item != null) {
                if (inputState.lastGUIMouseHover != null) {
                    if (inputState.lastGUIMouseHover.getClass() == Inventory.class) {
                        Inventory inventory = (Inventory) inputState.lastGUIMouseHover;
                        if (inventory_canDragIntoInventory(inventory)) {
                            int[] info = inventory_getInventoryInfoAtMousePointer(inventory);
                            if (info != null) {
                                inventory.inventoryAction.onDragFromInventory(inputState.inventoryDrag_Inventory, inputState.inventoryDrag_from_x, inputState.inventoryDrag_from_y, info[0], info[1]);
                            }
                        }
                    } else if (inputState.lastGUIMouseHover.getClass() == List.class) {
                        List list = (List) inputState.lastGUIMouseHover;
                        if (list_canDragIntoList(list)) {
                            int[] itemInfo = list_getInfoAtPointer(list);
                            int toIndex = itemInfo != null ? itemInfo[0] : (list.items.size() != 0 ? list.items.size() - 1 : 0);
                            if (list.listAction != null)
                                list.listAction.onDragFromInventory(inputState.inventoryDrag_Inventory, inputState.inventoryDrag_from_x, inputState.inventoryDrag_from_y, toIndex);
                        }
                    }
                } else {
                    if (inputState.inventoryDrag_Inventory.inventoryAction != null) {
                        inputState.inventoryDrag_Inventory.inventoryAction.onDragIntoScreen(
                                inputState.inventoryDrag_Item,
                                inputState.inventoryDrag_from_x, inputState.inventoryDrag_from_y,
                                api.input.mouseX(),
                                api.input.mouseY()
                        );
                    }
                }

                inputState.inventoryDrag_Item = null;
                inputState.inventoryDrag_Inventory = null;
                inputState.InventoryDrag_offset_x = inputState.InventoryDrag_offset_y = 0;
                inputState.inventoryDrag_from_x = inputState.inventoryDrag_from_y = 0;
            }
            if (inputState.listDrag_Item != null) {
                // Drag code
                if (inputState.lastGUIMouseHover != null) {
                    if (inputState.lastGUIMouseHover.getClass() == List.class) {
                        List list = (List) inputState.lastGUIMouseHover;
                        if (list_canDragIntoList(list)) {
                            int[] itemInfo = list_getInfoAtPointer(list);
                            int toIndex = itemInfo != null ? itemInfo[0] : (list.items.size() != 0 ? list.items.size() - 1 : 0);
                            if (list.listAction != null)
                                list.listAction.onDragFromList(inputState.listDrag_List, inputState.listDrag_from_index, toIndex);
                        }
                    } else if (inputState.lastGUIMouseHover.getClass() == Inventory.class) {
                        Inventory inventory = (Inventory) inputState.lastGUIMouseHover;
                        if (inventory_canDragIntoInventory(inventory)) {
                            int[] info = inventory_getInventoryInfoAtMousePointer(inventory);
                            if (info != null) {
                                inventory.inventoryAction.onDragFromList(inputState.listDrag_List, inputState.listDrag_from_index, info[0], info[1]);
                            }
                        }
                    }
                } else {
                    if (inputState.listDrag_List.listAction != null) {
                        inputState.listDrag_List.listAction.onDragIntoScreen(
                                inputState.listDrag_Item,
                                inputState.listDrag_from_index,
                                api.input.mouseX(),
                                api.input.mouseY()
                        );
                    }
                }

                inputState.listDrag_Item = null;
                inputState.listDrag_List = null;
                inputState.listDrag_offset_x = inputState.listDrag_offset_y = 0;
                inputState.listDrag_from_index = 0;
            }

            if (inputState.pressedMouseTool != null) {
                if (inputState.pressedMouseTool.mouseToolAction != null)
                    inputState.pressedMouseTool.mouseToolAction.onRelease(inputState.inputEvents.mouseButton, inputState.mouse_x, inputState.mouse_y);
                inputState.pressedMouseTool = null;
            }

        }

        if (inputState.inputEvents.mouseDragged) {
            if (inputState.draggedWindow != null) {
                inputState.draggedWindow.x = inputState.mouse_x_gui - inputState.draggedWindow_x_offset;
                inputState.draggedWindow.y = inputState.mouse_y_gui - inputState.draggedWindow_y_offset;
                if (inputState.draggedWindow.windowAction != null)
                    inputState.draggedWindow.windowAction.onMove(inputState.draggedWindow.x, inputState.draggedWindow.y);
            }
            if (inputState.scrolledScrollBarVertical != null) {
                int mouseYrel = inputState.mouse_y_gui - UICommons.component_getParentWindowY(inputState.scrolledScrollBarVertical) - (inputState.scrolledScrollBarVertical.y * TILE_SIZE) - inputState.scrolledScrollBarVertical.offset_y;
                float newScrolled = (mouseYrel / ((float) (inputState.scrolledScrollBarVertical.height * TILE_SIZE)));
                inputState.scrolledScrollBarVertical.scrolled = Tools.Calc.inBounds(newScrolled, 0f, 1f);
                if (inputState.scrolledScrollBarVertical.scrollBarAction != null)
                    inputState.scrolledScrollBarVertical.scrollBarAction.onScrolled(inputState.scrolledScrollBarVertical.scrolled);

            }
            if (inputState.scrolledScrollBarHorizontal != null) {
                int mouseXrel = inputState.mouse_x_gui - UICommons.component_getParentWindowX(inputState.scrolledScrollBarHorizontal) - (inputState.scrolledScrollBarHorizontal.x * TILE_SIZE) - inputState.scrolledScrollBarHorizontal.offset_x;
                float newScrolled = (mouseXrel / ((float) (inputState.scrolledScrollBarHorizontal.width * TILE_SIZE)));
                inputState.scrolledScrollBarHorizontal.scrolled = Tools.Calc.inBounds(newScrolled, 0f, 1f);
                if (inputState.scrolledScrollBarHorizontal.scrollBarAction != null)
                    inputState.scrolledScrollBarHorizontal.scrollBarAction.onScrolled(inputState.scrolledScrollBarHorizontal.scrolled);

            }
            if (inputState.turnedKnob != null) {
                Knob knob = inputState.turnedKnob;
                float amount = -(inputState.mouse_y_delta / (500 - (400 * api.config.getKnobSensitivity())));
                float newValue = knob.turned + amount;
                knob_turnKnob(knob, newValue, amount);

            }
            if (inputState.pressedMouseTool != null) {
                if (inputState.pressedMouseTool.mouseToolAction != null)
                    inputState.pressedMouseTool.mouseToolAction.onDrag(inputState.mouse_x, inputState.mouse_y);
            }


        }
        if (inputState.inputEvents.mouseMoved) {


        }

        if (inputState.inputEvents.mouseScrolled) {
            if (inputState.lastGUIMouseHover != null) {
                if (inputState.lastGUIMouseHover.getClass() == List.class) {
                    List list = (List) inputState.lastGUIMouseHover;
                    float amount = (1 / (float) Tools.Calc.lowerBounds(list.items.size(), 1)) * inputState.inputEvents.mouseScrolledAmount;
                    list.scrolled = Tools.Calc.inBounds(list.scrolled + amount, 0f, 1f);
                    if (list.listAction != null) {
                        list.listAction.onScrolled(list.scrolled);
                        list.listAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover instanceof Button) {
                    Button button = (Button) inputState.lastGUIMouseHover;
                    if (button.buttonAction != null) {
                        button.buttonAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == CheckBox.class) {
                    CheckBox checkBox = (CheckBox) inputState.lastGUIMouseHover;
                    if (checkBox.checkBoxAction != null) {
                        checkBox.checkBoxAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == ComboBox.class) {
                    ComboBox comboBox = (ComboBox) inputState.lastGUIMouseHover;
                    if (comboBox.comboBoxAction != null) {
                        comboBox.comboBoxAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == GameViewPort.class) {
                    GameViewPort gameViewPort = (GameViewPort) inputState.lastGUIMouseHover;
                    if (gameViewPort.gameViewPortAction != null) {
                        gameViewPort.gameViewPortAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == Inventory.class) {
                    Inventory inventory = (Inventory) inputState.lastGUIMouseHover;
                    if (inventory.inventoryAction != null) {
                        inventory.inventoryAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == Knob.class) {
                    Knob knob = (Knob) inputState.lastGUIMouseHover;
                    float amount = ((-1 / 20f) * inputState.inputEvents.mouseScrolledAmount) * api.config.getKnobSensitivity();
                    float newValue = knob.turned + amount;
                    knob_turnKnob(knob, newValue, amount);

                    if (knob.knobAction != null) {
                        knob.knobAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == Map.class) {
                    Map map = (Map) inputState.lastGUIMouseHover;
                    if (map.mapAction != null) {
                        map.mapAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == ScrollBarHorizontal.class) {
                    ScrollBarHorizontal scrollBarHorizontal = (ScrollBarHorizontal) inputState.lastGUIMouseHover;
                    float amount = ((-1 / 20f) * inputState.inputEvents.mouseScrolledAmount) * api.config.getKnobSensitivity();
                    scrollBarHorizontal.scrolled = Tools.Calc.inBounds(scrollBarHorizontal.scrolled + amount, 0f, 1f);
                    if (scrollBarHorizontal.scrollBarAction != null) {
                        scrollBarHorizontal.scrollBarAction.onScrolled(scrollBarHorizontal.scrolled);
                        scrollBarHorizontal.scrollBarAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == ScrollBarVertical.class) {
                    ScrollBarVertical scrollBarVertical = (ScrollBarVertical) inputState.lastGUIMouseHover;
                    float amount = ((-1 / 20f) * inputState.inputEvents.mouseScrolledAmount) * api.config.getKnobSensitivity();
                    scrollBarVertical.scrolled = Tools.Calc.inBounds(scrollBarVertical.scrolled + amount, 0f, 1f);
                    if (scrollBarVertical.scrollBarAction != null) {
                        scrollBarVertical.scrollBarAction.onScrolled(scrollBarVertical.scrolled);
                        scrollBarVertical.scrollBarAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == TabBar.class) {
                    TabBar tabBar = (TabBar) inputState.lastGUIMouseHover;
                    if (tabBar.tabBarAction != null) {
                        tabBar.tabBarAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == Image.class) {
                    Image image = (Image) inputState.lastGUIMouseHover;
                    if (image.imageAction != null) {
                        image.imageAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }

                } else if (inputState.lastGUIMouseHover.getClass() == Text.class) {
                    Text text = (Text) inputState.lastGUIMouseHover;
                    if (text.textAction != null) {
                        text.textAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == TextField.class) {
                    TextField textField = (TextField) inputState.lastGUIMouseHover;
                    if (textField.textFieldAction != null) {
                        textField.textFieldAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == Window.class) {
                    Window window = (Window) inputState.lastGUIMouseHover;
                    if (window.windowAction != null) {
                        window.windowAction.onMouseScroll(inputState.inputEvents.mouseScrolledAmount);
                    }
                }

            }

        }

        /* UpdateActions */
        {
            long currentTimeMillis = System.currentTimeMillis();
            inputState.screenComponents.forEach(component -> {
                for (UpdateAction updateAction : component.updateActions)
                    this.executeUpdateAction(updateAction, currentTimeMillis);
            });

            inputState.windows.forEach(window -> {
                for (UpdateAction updateAction : window.updateActions)
                    this.executeUpdateAction(updateAction, currentTimeMillis);
                window.components.forEach(component -> {
                    for (UpdateAction updateAction : component.updateActions)
                        this.executeUpdateAction(updateAction, currentTimeMillis);
                });
            });

            Iterator<UpdateAction> actionsIt = inputState.delayedOneshotActions.iterator();
            while (actionsIt.hasNext()) {
                UpdateAction updateAction = actionsIt.next();
                if (this.executeUpdateAction(updateAction, currentTimeMillis)) {
                    actionsIt.remove();
                }
            }

        }

        /* Button Hold Actions */
        {
            if (inputState.pressedButton != null) {
                if (inputState.pressedButton.canHold) {
                    inputState.pressedButton_timer_hold = inputState.pressedButton_timer_hold + 1;
                    if (inputState.pressedButton_timer_hold > api.config.getButtonHoldTimer()) {
                        if (inputState.pressedButton.buttonAction != null)
                            inputState.pressedButton.buttonAction.onHold();
                        inputState.pressedButton_timer_hold = 0;
                    }
                }
            }
        }


        /* Add/Remove windows & components */
        updateWindowAndComponentAddRemove();

        /* Update Notifications */
        updateNotifications();

        /* Update Tooltip */
        updateToolTip();

        /* Enforce Screen bounds */
        inputState.windows.forEach(window -> {
            if (window.enforceScreenBounds) {
                UICommons.window_enforceScreenBounds(inputState, window);
            }
        });
    }


    private void updateToolTip() {
        boolean showToolTip = true;
        // Anything dragged ?
        if (showToolTip) {
            showToolTip = inputState.listDrag_Item == null && inputState.inventoryDrag_Item == null;
        }
        // hovering over a component ?
        if (showToolTip) {
            showToolTip = (inputState.lastGUIMouseHover instanceof Component);
        }
        // modal active and component does not belong to modal ?
        if (showToolTip) {
            showToolTip = inputState.modalWindow != null ? ((Component) inputState.lastGUIMouseHover).addedToWindow == inputState.modalWindow : true;
        }

        if (showToolTip) {
            Component hoverComponent = (Component) inputState.lastGUIMouseHover;
            Object toolTipSubItem = null;
            if (hoverComponent.getClass() == List.class) {
                List list = (List) inputState.lastGUIMouseHover;
                if (list.listAction != null) {
                    int[] info = list_getInfoAtPointer(list);
                    if (info != null) {
                        toolTipSubItem = list.items.get(info[0]);
                    }
                }
            } else if (hoverComponent.getClass() == Inventory.class) {
                Inventory inventory = (Inventory) hoverComponent;
                int tileSize = inventory.doubleSized ? TILE_SIZE * 2 : TILE_SIZE;
                if (inventory.inventoryAction != null) {
                    int x_inventory = UICommons.component_getAbsoluteX(inventory);
                    int y_inventory = UICommons.component_getAbsoluteY(inventory);
                    int inv_x = (inputState.mouse_x_gui - x_inventory) / tileSize;
                    int inv_y = (inputState.mouse_y_gui - y_inventory) / tileSize;
                    if (UICommons.inventory_positionValid(inventory, inv_x, inv_y)) {
                        toolTipSubItem = inventory.items[inv_x][inv_y];
                    }
                }
            }


            boolean updateToolTip;

            if (hoverComponent.updateToolTip) {
                updateToolTip = true;
                hoverComponent.updateToolTip = false;
            } else {
                if (hoverComponent.getClass() == List.class || hoverComponent.getClass() == Inventory.class) {
                    // Check on subitem change
                    updateToolTip = inputState.tooltip_lastHoverObject != toolTipSubItem;
                } else {
                    // Check on component change
                    updateToolTip = inputState.tooltip_lastHoverObject != hoverComponent;
                }
            }

            if (updateToolTip) {
                inputState.tooltip_wait_delay = true;
                inputState.tooltip_delay_timer = System.currentTimeMillis();
                if (hoverComponent.getClass() == List.class && toolTipSubItem != null) {
                    // check for list item tooltips
                    List list = (List) hoverComponent;
                    inputState.tooltip = list.listAction.toolTip(toolTipSubItem);
                    inputState.tooltip_lastHoverObject = toolTipSubItem;
                } else if (hoverComponent.getClass() == Inventory.class && toolTipSubItem != null) {
                    // check for inventory item tooltip
                    Inventory inventory = (Inventory) hoverComponent;
                    inputState.tooltip = inventory.inventoryAction.toolTip(toolTipSubItem);
                    inputState.tooltip_lastHoverObject = toolTipSubItem;
                } else {
                    // take component tooltip
                    inputState.tooltip = hoverComponent.toolTip;
                    inputState.tooltip_lastHoverObject = hoverComponent;
                }
            }
        } else {
            inputState.tooltip = null;
            inputState.tooltip_lastHoverObject = null;
        }

        // Fade In/Out

        if (inputState.tooltip != null) {
            if (inputState.tooltip_wait_delay) {
                if ((System.currentTimeMillis() - inputState.tooltip_delay_timer) > api.config.getTooltipFadeInDelayTime()) {
                    inputState.tooltip_wait_delay = false;
                    inputState.tooltip_fadeIn_pct = 0f;
                    inputState.tooltip_fadeIn_timer = System.currentTimeMillis();
                    if (inputState.tooltip.toolTipAction != null) {
                        inputState.tooltip.toolTipAction.onDisplay();
                    }
                }
            } else if (inputState.tooltip_fadeIn_pct < 1f) {
                inputState.tooltip_fadeIn_pct = Tools.Calc.upperBounds(((System.currentTimeMillis() - inputState.tooltip_fadeIn_timer) / (float) api.config.getTooltipFadeInTime()), 1f);
            } else {
                if (inputState.tooltip.toolTipAction != null) {
                    inputState.tooltip.toolTipAction.onUpdate();
                }
            }
        }

    }

    private void updateMouse() {

        // --- GAME CURSOR ---
        // ScreenCursor To WorldCursor
        inputState.vector2_unproject.x = Gdx.input.getX();
        inputState.vector2_unproject.y = Gdx.input.getY();
        inputState.viewport_screen.unproject(inputState.vector2_unproject);
        // WorldCursor to  FBOCursor
        inputState.vector_fboCursor.x = inputState.vector2_unproject.x;
        inputState.vector_fboCursor.y = Gdx.graphics.getHeight() - inputState.vector2_unproject.y;
        inputState.vector_fboCursor.z = 1;
        inputState.camera_game.unproject(inputState.vector_fboCursor, 0, 0, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
        // Set to final
        this.inputState.mouse_x = (int) inputState.vector_fboCursor.x;
        this.inputState.mouse_y = (int) inputState.vector_fboCursor.y;


        // --- GUI CURSOR ---
        // ScreenCursor To WorldCursor
        inputState.vector2_unproject.x = Gdx.input.getX();
        inputState.vector2_unproject.y = Gdx.input.getY();
        inputState.viewport_screen.unproject(inputState.vector2_unproject);
        // WorldCursor to  FBOCursor
        inputState.vector_fboCursor.x = inputState.vector2_unproject.x;
        inputState.vector_fboCursor.y = Gdx.graphics.getHeight() - inputState.vector2_unproject.y;
        inputState.vector_fboCursor.z = 1;
        inputState.camera_gui.unproject(inputState.vector_fboCursor, 0, 0, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
        // Set to final
        this.inputState.mouse_x_gui = (int) inputState.vector_fboCursor.x;
        this.inputState.mouse_y_gui = (int) inputState.vector_fboCursor.y;

        // MOUSE DELTA
        this.inputState.mouse_x_delta = Gdx.input.getDeltaX();
        this.inputState.mouse_y_delta = Gdx.input.getDeltaY();

    }

    private void updateLastGUIMouseHover() {
        inputState.lastGUIMouseHover = findCurrentLastGUIMouseHover();


    }


    private void updateWindowAndComponentAddRemove() {

        Window windowTmp;
        Component componentTmp;
        HotKey hotkeyTmp;
        while ((windowTmp = inputState.addWindowQueue.pollFirst()) != null) {
            inputState.windows.add(0, windowTmp);
            if (windowTmp.windowAction != null) windowTmp.windowAction.onAdd();
            UICommons.window_bringToFront(inputState, windowTmp);
        }
        while ((windowTmp = inputState.removeWindowQueue.pollFirst()) != null) {
            for (Component windowTmpComponent : windowTmp.components) {
                windowTmpComponent.addedToWindow = null;
                if (windowTmpComponent.getClass() == GameViewPort.class)
                    inputState.gameViewPorts.remove(windowTmpComponent);
            }
            windowTmp.components.clear();
            inputState.windows.remove(windowTmp);
            if (windowTmp.windowAction != null) windowTmp.windowAction.onRemove();

            // Remove if the window was the current modal
            if (inputState.modalWindow != null && inputState.modalWindow == windowTmp) inputState.modalWindow = null;

            // Reset if it was last active
            if (inputState.lastActiveWindow == windowTmp) inputState.lastActiveWindow = null;
        }

        while ((componentTmp = inputState.addScreenComponentsQueue.pollFirst()) != null) {
            if (componentTmp.getClass() == GameViewPort.class)
                inputState.gameViewPorts.add((GameViewPort) componentTmp);

            inputState.screenComponents.add(componentTmp);
        }
        while ((componentTmp = inputState.removeScreenComponentsQueue.pollFirst()) != null) {
            if (componentTmp.getClass() == GameViewPort.class) inputState.gameViewPorts.remove(componentTmp);
            inputState.screenComponents.remove(componentTmp);
        }

        for (Window window : inputState.windows) {
            while ((componentTmp = window.addComponentsQueue.pollFirst()) != null) {
                if (componentTmp.getClass() == GameViewPort.class)
                    inputState.gameViewPorts.add((GameViewPort) componentTmp);
                if (componentTmp.addedToWindow == null) {
                    componentTmp.addedToWindow = window;
                    window.components.add(componentTmp);
                }
            }
            while ((componentTmp = window.removeComponentsQueue.pollFirst()) != null) {
                if (componentTmp.getClass() == GameViewPort.class) inputState.gameViewPorts.remove(componentTmp);
                componentTmp.addedToWindow = null;
                window.components.remove(componentTmp);
            }
        }

        while ((hotkeyTmp = inputState.addHotKeyQueue.pollFirst()) != null) {
            inputState.hotKeys.add(hotkeyTmp);
        }
        while ((hotkeyTmp = inputState.removeHotKeyQueue.pollFirst()) != null) {
            inputState.hotKeys.remove(hotkeyTmp);
        }

    }

    private void updateNotifications() {
        if (inputState.notifications.size() > 0) {
            Notification notification = inputState.notifications.get(0);
            switch (notification.state) {
                case INIT_SCROLL -> {
                    notification.timer = System.currentTimeMillis();
                    notification.state = STATE_NOTIFICATION.SCROLL;
                }
                case INIT_DISPLAY -> {
                    notification.timer = System.currentTimeMillis();
                    notification.state = STATE_NOTIFICATION.DISPLAY;
                }
                case SCROLL -> {
                    if (System.currentTimeMillis() - notification.timer > 500) {
                        notification.scroll += api.config.getNotificationsScrollSpeed();
                        if (notification.scroll >= notification.scrollMax) {
                            notification.state = STATE_NOTIFICATION.DISPLAY;
                            notification.timer = System.currentTimeMillis();
                        }
                    }
                }
                case DISPLAY -> {
                    if (System.currentTimeMillis() - notification.timer > notification.displayTime) {
                        notification.state = STATE_NOTIFICATION.FADEOUT;
                        notification.timer = System.currentTimeMillis();
                    }
                    break;
                }
                case FADEOUT -> {
                    if ((System.currentTimeMillis() - notification.timer > api.config.getNotificationsFadeoutTime())) {
                        inputState.notifications.remove(0);
                    }
                }
            }


        }

    }

    private void updateGameCamera() {
        inputState.camera_game.position.set(inputState.camera_x, inputState.camera_y, inputState.camera_z);
        inputState.camera_game.zoom = inputState.camera_zoom;
        inputState.camera_game.viewportWidth = inputState.camera_width;
        inputState.camera_game.viewportHeight = inputState.camera_height;
        inputState.camera_game.update();
    }

    private void updateMouseCursor() {
        /* Update Cursor*/
        if (inputState.lastGUIMouseHover != null) {
            // 1. GUI Cursor
            inputState.cursor_setNext = api.config.getCursorGui();
        } else {
            // 2. Temporary Cursor
            if (inputState.displayTemporaryCursor) {
                inputState.cursor_setNext = inputState.temporaryCursor;
                inputState.displayTemporaryCursor = false;
            } else {
                if (inputState.mouseTool != null) {
                    // 3. Mouse Tool cursor
                    if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) || Gdx.input.isButtonPressed(Input.Buttons.RIGHT) || Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
                        inputState.cursor_setNext = inputState.mouseTool.cursorDown;
                    } else {
                        inputState.cursor_setNext = inputState.mouseTool.cursor;
                    }
                } else {
                    // no mouse tool set - display gui cursor
                    inputState.cursor_setNext = api.config.getCursorGui();
                }
            }
        }


        // Set Cursor
        if (inputState.cursor_current != inputState.cursor_setNext) {
            inputState.cursor_current = inputState.cursor_setNext;
            if (inputState.cursor_current.getClass() == CMediaImageCursor.class) {
                Gdx.graphics.setCursor(mediaManager.getCMediaImageCursor((CMediaImageCursor) inputState.cursor_current));
            } else if (inputState.cursor_current.getClass() == CMediaSystemCursor.class) {
                Gdx.graphics.setSystemCursor(mediaManager.getCMediaImageSystemCursor((CMediaSystemCursor) inputState.cursor_current));
            }
        }


    }

    private boolean executeUpdateAction(UpdateAction updateAction, long currentTimeMillis) {
        if (updateAction.interval == 0) {
            updateAction.onUpdate();
            return true;
        } else if ((currentTimeMillis - updateAction.lastUpdate) > updateAction.interval) {
            updateAction.onUpdate();
            updateAction.lastUpdate = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private Object findCurrentLastGUIMouseHover() {
        // Notification Collision
        for (int i = 0; i < inputState.notifications.size(); i++) {
            Notification notification = inputState.notifications.get(i);
            if (notification.notificationAction != null && Tools.Calc.pointRectsCollide(inputState.mouse_x_gui, inputState.mouse_y_gui,
                    0, inputState.internalResolutionWidth - ((i + 1) * TILE_SIZE),
                    inputState.internalResolutionWidth, TILE_SIZE)) {
                return notification;
            }
        }

        // Context Menu collision
        if (inputState.displayedContextMenu != null) {
            for (int i = 0; i < inputState.displayedContextMenu.items.size(); i++) {
                if (Tools.Calc.pointRectsCollide(inputState.mouse_x_gui, inputState.mouse_y_gui, inputState.displayedContextMenu.x, inputState.displayedContextMenu.y - (TILE_SIZE) - (i * TILE_SIZE), inputState.displayedContextMenuWidth * TILE_SIZE, TILE_SIZE)) {
                    return inputState.displayedContextMenu.items.get(i);
                }
            }
        }
        // Combobox Open Menu collision
        if (inputState.openComboBox != null && inputState.openComboBox.menuOpen) {
            if (Tools.Calc.pointRectsCollide(inputState.mouse_x_gui, inputState.mouse_y_gui, UICommons.component_getAbsoluteX(inputState.openComboBox), UICommons.component_getAbsoluteY(inputState.openComboBox) - (inputState.openComboBox.items.size() * TILE_SIZE), inputState.openComboBox.width * TILE_SIZE, (inputState.openComboBox.items.size() * TILE_SIZE))) {
                return inputState.openComboBox;
            }
        }

        // Window / WindowComponent collision
        windowLoop:
        for (int i = inputState.windows.size() - 1; i >= 0; i--) {
            Window window = inputState.windows.get(i);
            if (!window.visible) continue windowLoop;

            int wndX = window.x;
            int wndY = window.y + (window.folded ? ((window.height - 1) * TILE_SIZE) : 0);
            int wndWidth = UICommons.window_getRealWidth(window);
            int wndHeight = UICommons.window_getRealHeight(window);

            boolean collidesWithWindow = Tools.Calc.pointRectsCollide(inputState.mouse_x_gui, inputState.mouse_y_gui, wndX, wndY, wndWidth, wndHeight);
            if (collidesWithWindow) {
                for (int ic = window.components.size() - 1; ic >= 0; ic--) {
                    Component component = window.components.get(ic);
                    if (mouseCollidesWithComponent(component)) {
                        return component;
                    }
                }
                return window;
            }
        }

        // Screen component collision
        for (Component component : inputState.screenComponents) {
            if (mouseCollidesWithComponent(component)) {
                return component;
            }
        }
        return null;
    }


    private boolean isHiddenByTab(Component component) {
        if (component.addedToTab == null) return false;

        Tab selectedTab = UICommons.tabBar_getSelectedTab(component.addedToTab.tabBar);
        if (selectedTab != null && selectedTab == component.addedToTab) {
            if (component.addedToTab.tabBar.addedToTab != null) {
                if (isHiddenByTab(component.addedToTab.tabBar)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }


    private boolean mouseCollidesWithComponent(Component component) {
        if (!component.visible) return false;
        if (component.disabled) return false;
        if (isHiddenByTab(component)) return false;

        if (Tools.Calc.pointRectsCollide(inputState.mouse_x_gui, inputState.mouse_y_gui, UICommons.component_getAbsoluteX(component), UICommons.component_getAbsoluteY(component), component.width * TILE_SIZE, component.height * TILE_SIZE)) {
            inputState.lastGUIMouseHover = component;
            return true;
        }

        return false;
    }

    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);

        // Draw Game
        {
            // Draw GUI GameViewPort FrameBuffers
            for (GameViewPort gameViewPort : this.inputState.gameViewPorts) {
                renderGameViewPortFrameBuffer(gameViewPort);
            }

            // Draw Main FrameBuffer
            inputState.spriteBatch_game.setProjectionMatrix(this.inputState.camera_game.combined);
            inputState.frameBuffer_game.begin();
            this.uiAdapter.render(inputState.spriteBatch_game, true);
            inputState.frameBuffer_game.end();
        }


        { // Draw GUI
            inputState.frameBuffer_gui.begin();
            Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
            inputState.spriteBatch_gui.setProjectionMatrix(this.inputState.camera_gui.combined);
            this.uiAdapter.renderBeforeGUI(inputState.spriteBatch_gui);
            this.renderGUI();
            this.uiAdapter.renderAfterGUI(inputState.spriteBatch_gui);
            inputState.frameBuffer_gui.end();
        }


        {
            inputState.spriteBatch_screen.setProjectionMatrix(inputState.camera_screen.combined);
            // Render to Upscaled Buffer
            inputState.frameBuffer_upScale.begin();
            inputState.spriteBatch_screen.begin();
            inputState.spriteBatch_screen.draw(inputState.texture_game, 0, 0, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
            inputState.spriteBatch_screen.draw(inputState.texture_gui, 0, 0, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
            inputState.spriteBatch_screen.end();
            inputState.frameBuffer_upScale.end();
            // Render Final Screen
            inputState.viewport_screen.apply();
            inputState.spriteBatch_screen.begin();
            inputState.spriteBatch_screen.draw(inputState.texture_upScale, 0, 0, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
            inputState.spriteBatch_screen.end();
        }


    }


    private void renderGameViewPortFrameBuffer(GameViewPort gameViewPort) {
        if (!render_isComponentRendered(gameViewPort)) return;

        if (System.currentTimeMillis() - gameViewPort.updateTimer > gameViewPort.updateTime) {
            // save camera settings
            float x = inputState.camera_x;
            float y = inputState.camera_y;
            float z = inputState.camera_z;
            float zoom = inputState.camera_zoom;
            int width = inputState.camera_width;
            int height = inputState.camera_height;

            // set camera
            inputState.camera_x = gameViewPort.camera_x;
            inputState.camera_y = gameViewPort.camera_y;
            inputState.camera_z = gameViewPort.camera_z;
            inputState.camera_zoom = gameViewPort.camera_zoom;
            inputState.camera_width = gameViewPort.width * TILE_SIZE;
            inputState.camera_height = gameViewPort.height * TILE_SIZE;
            updateGameCamera();
            // draw to frambuffer
            inputState.spriteBatch_game.setProjectionMatrix(inputState.camera_game.combined);
            gameViewPort.frameBuffer.begin();
            this.uiAdapter.render(inputState.spriteBatch_game, false);
            gameViewPort.frameBuffer.end();

            // reset camera position back
            inputState.camera_x = x;
            inputState.camera_y = y;
            inputState.camera_z = z;
            inputState.camera_zoom = zoom;
            inputState.camera_width = width;
            inputState.camera_height = height;
            updateGameCamera();
            gameViewPort.updateTimer = System.currentTimeMillis();
        }
    }


    private void renderGUI() {
        inputState.animation_timer_gui = inputState.animation_timer_gui + Gdx.graphics.getDeltaTime();

        inputState.spriteBatch_gui.begin();
        render_batchSetColorWhite();

        if (inputState.modalWindow != null || inputState.guiFrozen) render_enableGrayScaleShader(true);


        /* Draw Screen Components */
        for (Component component : inputState.screenComponents) {
            render_drawComponent(component);
            render_drawComponentTopLayer(null, component);
        }

        /* Draw Windows */
        for (Window window : inputState.windows) {
            if (inputState.modalWindow != null && inputState.modalWindow == window) render_enableGrayScaleShader(false);
            render_drawWindow(window);
        }

        render_enableGrayScaleShader(false);

        /* Notifications */
        render_drawNotifications();

        /* Context Menu */
        render_drawContextMenu();

        /* Tooltip */
        render_drawTooltip();

        /* Cursor */
        render_drawCursorListDrags();

        inputState.spriteBatch_gui.end();
    }


    private boolean render_isComponentRendered(Component component) {
        if (!component.visible) return false;
        if (component.addedToWindow != null && !component.addedToWindow.visible) return false;
        if (isHiddenByTab(component)) return false;
        return true;
    }

    private Integer tabBar_getInfoAtPointer(TabBar tabBar) {
        int x_bar = UICommons.component_getAbsoluteX(tabBar);
        int y_bar = UICommons.component_getAbsoluteY(tabBar);

        int tabXOffset = tabBar.tabOffset;
        selectTabLoop:
        for (int i = 0; i < tabBar.tabs.size(); i++) {
            Tab tab = tabBar.tabs.get(i);
            if ((tabXOffset + tab.width) > tabBar.width) {
                break selectTabLoop;
            }

            if (Tools.Calc.pointRectsCollide(inputState.mouse_x_gui, inputState.mouse_y_gui, x_bar + (tabXOffset * TILE_SIZE), y_bar, tab.width * TILE_SIZE, TILE_SIZE)) {
                return i;
            }
            tabXOffset = tabXOffset + tab.width;
        }


        return null;
    }

    private int tab_getCMediaIndex(int x, int width) {
        if (width == 1) {
            return 3;
        } else {
            if (x == 0) {
                return 0;
            } else if (x == width - 1) {
                return 2;
            } else {
                return 1;
            }
        }


    }

    private void knob_turnKnob(Knob knob, float newValue, float amount) {
        if (!knob.endless) {
            knob.turned = Tools.Calc.inBounds(newValue, 0f, 1f);
            if (knob.knobAction != null) knob.knobAction.onTurned(knob.turned, amount);
        } else {
            if (newValue > 1) {
                newValue = newValue - 1f;
            } else if (newValue < 0) {
                newValue = 1f - Math.abs(newValue);
            }
            knob.turned = Tools.Calc.inBounds(newValue, 0f, 1f);
            if (knob.knobAction != null) knob.knobAction.onTurned(knob.turned, amount);
        }
    }

    private boolean list_canDragIntoList(List list) {
        if (inputState.listDrag_Item != null) {
            if (inputState.listDrag_List == null || list == null) return false;
            if (inputState.listDrag_List == list) return true; // into itself
            return list.dragInEnabled &&
                    !list.disabled && !inputState.listDrag_List.disabled && list.dragInEnabled && inputState.listDrag_List.dragOutEnabled &&
                    list.listAction != null && list.listAction.canDragFromList(inputState.listDrag_List);
        } else if (inputState.inventoryDrag_Item != null) {
            if (inputState.inventoryDrag_Inventory == null || list == null) return false;
            return list.dragInEnabled &&
                    !list.disabled && !inputState.inventoryDrag_Inventory.disabled && list.dragInEnabled && inputState.inventoryDrag_Inventory.dragOutEnabled &&
                    list.listAction != null && list.listAction.canDragFromInventory(inputState.inventoryDrag_Inventory);
        } else {
            return false;
        }
    }


    private int[] list_getInfoAtPointer(List list) {
        if (!(list.items != null && list.items.size() > 0 && list.listAction != null)) return null;
        int itemFrom = MathUtils.round(list.scrolled * ((list.items.size()) - (list.height)));
        itemFrom = Tools.Calc.lowerBounds(itemFrom, 0);
        int x_list = UICommons.component_getAbsoluteX(list);
        int y_list = UICommons.component_getAbsoluteY(list);
        for (int i = 0; i < list.height; i++) {
            int itemIndex = itemFrom + i;
            if (itemIndex < list.items.size()) {
                int itemOffsetY = ((list.height - 1) - i);
                if (Tools.Calc.pointRectsCollide(inputState.mouse_x_gui, inputState.mouse_y_gui, x_list, y_list + itemOffsetY * TILE_SIZE, TILE_SIZE * list.width, TILE_SIZE)) {
                    return new int[]{itemIndex, itemOffsetY};
                }
            }
        }
        return null;
    }

    private int[] inventory_getInventoryInfoAtMousePointer(Inventory inventory) {
        int tileSize = inventory.doubleSized ? TILE_SIZE * 2 : TILE_SIZE;
        int x_inventory = UICommons.component_getAbsoluteX(inventory);
        int y_inventory = UICommons.component_getAbsoluteY(inventory);
        int inv_to_x = (inputState.mouse_x_gui - x_inventory) / tileSize;
        int inv_to_y = (inputState.mouse_y_gui - y_inventory) / tileSize;
        if (UICommons.inventory_positionValid(inventory, inv_to_x, inv_to_y)) {
            return new int[]{inv_to_x, inv_to_y};
        }
        return null;
    }


    private boolean inventory_canDragIntoInventory(Inventory inventory) {
        if (inputState.inventoryDrag_Item != null) {
            if (inputState.inventoryDrag_Inventory == null || inventory == null) return false;
            if (inputState.inventoryDrag_Inventory == inventory) return true; // into itself
            return inventory.dragInEnabled &&
                    !inventory.disabled && !inputState.inventoryDrag_Inventory.disabled && inventory.dragInEnabled && inputState.inventoryDrag_Inventory.dragOutEnabled &&
                    inventory.inventoryAction != null && inventory.inventoryAction.canDragFromInventory(inputState.inventoryDrag_Inventory);
        } else if (inputState.listDrag_Item != null) {
            if (inputState.listDrag_List == null || inventory == null) return false;
            return inventory.dragInEnabled &&
                    !inventory.disabled && !inputState.listDrag_List.disabled && inventory.dragInEnabled && inputState.listDrag_List.dragOutEnabled &&
                    inventory.inventoryAction != null && inventory.inventoryAction.canDragFromList(inputState.listDrag_List);
        } else {
            return false;
        }
    }

    private int render_getWindowCMediaIndex(int x, int y, int width, int height, boolean hasTitlebar) {
        if (x == 0 && y == 0) return 2;
        if (x == width - 1 && y == height - 1) return hasTitlebar ? 6 : 11;
        if (x == width - 1 && y == 0) return 8;
        if (x == 0 && y == height - 1) return hasTitlebar ? 0 : 9;
        if (x == 0) return 1;
        if (x == width - 1) return 7;
        if (y == 0) return 5;
        if (y == height - 1) return hasTitlebar ? 3 : 10;

        return 4;
    }

    private int render_getListDragCMediaIndex(int x, int width) {
        return x == 0 ? 0 : x == (width - 1) ? 2 : 1;
    }

    private int render_getComponent16TilesCMediaIndex(int x, int y, int width, int height) {
        if (width == 1 && height == 1) return 12;
        if (width == 1) {
            if (y == 0) {
                return 15;
            } else if (y == height - 1) {
                return 13;
            } else {
                return 14;
            }
        } else if (height == 1) {
            if (x == 0) {
                return 3;
            } else if (x == width - 1) {
                return 11;
            } else {
                return 7;
            }
        } else {
            if (x == 0 && y == 0) {
                return 2;
            } else if (x == width - 1 && y == height - 1) {
                return 8;
            } else if (x == width - 1 && y == 0) {
                return 10;
            } else if (x == 0 && y == height - 1) {
                return 0;
            } else {
                if (x == 0) {
                    return 1;
                } else if (x == width - 1) {
                    return 9;
                } else if (y == 0) {
                    return 6;
                } else if (y == height - 1) {
                    return 4;
                } else {
                    return 5;
                }
            }
        }
    }

    private int render_getComponent9TilesCMediaIndex(int x, int y, int width, int height) {
        if (x == 0 && y == 0) return 0;
        if (x == width - 1 && y == 0) return 6;
        if (x == 0 && y == (height - 1)) return 2;
        if (x == width - 1 && y == (height - 1)) return 8;

        if (y == 0) return 3;
        if (y == (height - 1)) return 5;
        if (x == 0) return 1;
        if (x == width - 1) return 7;


        return 4;
    }

    private void render_drawContextMenu() {

        if (inputState.displayedContextMenu != null) {
            ContextMenu contextMenu = inputState.displayedContextMenu;

            int width = inputState.displayedContextMenuWidth;
            int height = contextMenu.items.size();

            render_batchSaveColor();
            render_batchSetColor(contextMenu.color_r, contextMenu.color_g, contextMenu.color_b, contextMenu.color_a);

            /* Menu */
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int index = render_getComponent9TilesCMediaIndex(x, y, width, height);//x==0 ? 0 : (x == (width-1)) ? 2 : 1;
                    ContextMenuItem item = contextMenu.items.get(y);
                    CMediaArray cMenuTexture = null;
                    if (Tools.Calc.pointRectsCollide(inputState.mouse_x_gui, inputState.mouse_y_gui, contextMenu.x, contextMenu.y - (TILE_SIZE) - (y * TILE_SIZE), inputState.displayedContextMenuWidth * TILE_SIZE, TILE_SIZE)) {
                        cMenuTexture = GUIBaseMedia.GUI_CONTEXT_MENU_SELECTED;
                    } else {
                        cMenuTexture = GUIBaseMedia.GUI_CONTEXT_MENU;
                    }
                    render_batchSaveColor();
                    render_batchSetColor(item.color_r, item.color_g, item.color_b, item.color_a);
                    render_drawCMediaImage(cMenuTexture, contextMenu.x + x * TILE_SIZE, contextMenu.y - (y * TILE_SIZE) - TILE_SIZE, index);
                    render_batchLoadColor();
                }
            }

            /* Text */
            for (int y = 0; y < contextMenu.items.size(); y++) {
                ContextMenuItem item = contextMenu.items.get(y);
                render_drawFont(item.font, item.text, contextMenu.color_a, contextMenu.x, contextMenu.y - (y * TILE_SIZE) - TILE_SIZE, 1, 2, item.icon, item.iconIndex, (width) * TILE_SIZE);
            }


            render_batchLoadColor();
        }


    }

    private void render_drawTooltip() {
        if (inputState.tooltip == null) return;
        if (inputState.tooltip_wait_delay) return;
        if (inputState.tooltip.lines == null || inputState.tooltip.lines.length == 0) return;

        ToolTip tooltip = inputState.tooltip;

        render_batchSaveColor();

        int text_width_max = 0;
        for (String line : tooltip.lines) {
            int line_width = mediaManager.textWidth(tooltip.font, line);
            if (line_width > text_width_max) text_width_max = line_width;
        }
        int tooltip_width = MathUtils.round((text_width_max + (TILE_SIZE * 2)) / TILE_SIZE);
        int tooltip_height = tooltip.lines.length;

        tooltip_width = Tools.Calc.lowerBounds(tooltip_width, 1);
        tooltip_height = Tools.Calc.lowerBounds(tooltip_height, 1);

        int tooltip_x = 0;
        int tooltip_y = 0;
        // Direction
        int direction = 1;


        /* Left or right ? */
        /* Up or down */
        boolean collidesLeft = true;
        boolean collidesRight = true;
        boolean collidesUp = true;
        boolean collidesDown = true;


        if (inputState.mouse_x_gui + ((tooltip_width + 2) * TILE_SIZE) <= inputState.internalResolutionWidth) {
            collidesRight = false;
            //direction = 1;
        }
        if (inputState.mouse_x_gui - ((tooltip_width + 2) * TILE_SIZE) >= 0) {
            collidesLeft = false;
            //direction = 2;
        }
        if (inputState.mouse_y_gui - ((tooltip_height + 2) * TILE_SIZE) >= 0) {
            collidesDown = false;
            //direction = 3;
        }
        if (inputState.mouse_y_gui + ((tooltip_height + 2) * TILE_SIZE) <= inputState.internalResolutionHeight) { // Push down
            collidesUp = false;
            //direction = 4;
        }

        if (collidesUp) direction = 4;
        if (collidesDown) direction = 3;
        if (collidesLeft) direction = 1;
        if (collidesRight) direction = 2;

        switch (direction) {
            case 1 -> {
                tooltip_x = inputState.mouse_x_gui + (2 * TILE_SIZE);
                tooltip_y = inputState.mouse_y_gui - ((tooltip_height * TILE_SIZE) / 2);
            }
            case 2 -> {
                tooltip_x = inputState.mouse_x_gui - ((tooltip_width + 2) * TILE_SIZE);
                tooltip_y = inputState.mouse_y_gui - ((tooltip_height * TILE_SIZE) / 2);
            }
            case 3 -> {
                tooltip_x = inputState.mouse_x_gui - ((tooltip_width * TILE_SIZE) / 2);
                tooltip_y = inputState.mouse_y_gui + ((2) * TILE_SIZE);
            }
            case 4 -> {
                tooltip_x = inputState.mouse_x_gui - ((tooltip_width * TILE_SIZE) / 2);
                tooltip_y = inputState.mouse_y_gui - ((tooltip_height + 2) * TILE_SIZE);
            }
        }


        // Draw
        render_batchSetColor(tooltip.color_r, tooltip.color_g, tooltip.color_b, tooltip.color_a * inputState.tooltip_fadeIn_pct);

        // Lines
        switch (direction) {
            case 1:
                render_drawCMediaImage(GUIBaseMedia.GUI_TOOLTIP_LINE_X, inputState.mouse_x_gui, inputState.mouse_y_gui);
                render_drawCMediaImage(GUIBaseMedia.GUI_TOOLTIP_LINE_X, inputState.mouse_x_gui + TILE_SIZE, inputState.mouse_y_gui);
                break;
            case 2:
                render_drawCMediaImage(GUIBaseMedia.GUI_TOOLTIP_LINE_X, inputState.mouse_x_gui - TILE_SIZE, inputState.mouse_y_gui);
                render_drawCMediaImage(GUIBaseMedia.GUI_TOOLTIP_LINE_X, inputState.mouse_x_gui - (TILE_SIZE * 2), inputState.mouse_y_gui);
                break;
            case 3:
                render_drawCMediaImage(GUIBaseMedia.GUI_TOOLTIP_LINE_Y, inputState.mouse_x_gui, inputState.mouse_y_gui);
                render_drawCMediaImage(GUIBaseMedia.GUI_TOOLTIP_LINE_Y, inputState.mouse_x_gui, inputState.mouse_y_gui + TILE_SIZE);
                break;
            case 4:
                render_drawCMediaImage(GUIBaseMedia.GUI_TOOLTIP_LINE_Y, inputState.mouse_x_gui, inputState.mouse_y_gui - TILE_SIZE);
                render_drawCMediaImage(GUIBaseMedia.GUI_TOOLTIP_LINE_Y, inputState.mouse_x_gui, inputState.mouse_y_gui - (TILE_SIZE * 2));
                break;
        }

        // Box
        for (int tx = 0; tx < tooltip_width; tx++) {
            for (int ty = 0; ty < tooltip_height; ty++) {
                if (tooltip.displayFistLineAsTitle && ty == (tooltip_height - 1)) {
                    int titleIndex = (tx == 0 ? 0 : ((tx == tooltip_width - 1) ? 2 : 1));
                    render_drawCMediaImage(GUIBaseMedia.GUI_TOOLTIP_TITLE, tooltip_x + (tx * TILE_SIZE), tooltip_y + (ty * TILE_SIZE), titleIndex);
                } else {
                    render_drawCMediaImage(GUIBaseMedia.GUI_TOOLTIP, tooltip_x + (tx * TILE_SIZE), tooltip_y + (ty * TILE_SIZE), render_getComponent16TilesCMediaIndex(tx, ty, tooltip_width, tooltip_height));
                }
            }
        }


        //Text
        for (int ty = 0; ty < tooltip_height; ty++) {
            String lineTxt = tooltip.lines[tooltip.lines.length - ty - 1];
            if (tooltip.displayFistLineAsTitle && ty == (tooltip_height - 1)) {
                int text_width = mediaManager.textWidth(tooltip.font, lineTxt);
                render_drawFont(tooltip.font, lineTxt, tooltip.color_a * inputState.tooltip_fadeIn_pct, tooltip_x + ((tooltip_width / 2) * TILE_SIZE) - (text_width / 2), tooltip_y + (ty * TILE_SIZE), 0, 1);
            } else {
                render_drawFont(tooltip.font, lineTxt, tooltip.color_a * inputState.tooltip_fadeIn_pct, tooltip_x, tooltip_y + (ty * TILE_SIZE), 1, 1);
            }
        }

        // Images
        for (ToolTipImage toolTipImage : tooltip.images) {
            render_drawCMediaImage(toolTipImage.image, tooltip_x + toolTipImage.offset_x, tooltip_y + toolTipImage.offset_y);

        }


        render_batchLoadColor();
        return;
    }

    private void render_drawNotifications() {
        if (inputState.notifications.size() == 0) return;
        int width = (inputState.internalResolutionWidth % TILE_SIZE == 0) ? (inputState.internalResolutionWidth / TILE_SIZE) : ((inputState.internalResolutionWidth / TILE_SIZE) + 1);

        render_batchSaveColor();

        int y = 0;
        int yOffsetSlideFade = 0;
        for (Notification notification : inputState.notifications) {
            if (notification.state == STATE_NOTIFICATION.FADEOUT) {
                float fadeoutProgress = ((System.currentTimeMillis() - notification.timer) / (float) api.config.getNotificationsFadeoutTime());
                yOffsetSlideFade = yOffsetSlideFade + MathUtils.round(TILE_SIZE * (fadeoutProgress));
            }
            render_batchSetColor(notification.color_r, notification.color_g, notification.color_b, notification.color_a);
            for (int x = 0; x < width; x++) {
                render_drawCMediaImage(GUIBaseMedia.GUI_NOTIFICATION_BAR, (x * TILE_SIZE), inputState.internalResolutionHeight - TILE_SIZE - (y * TILE_SIZE) + yOffsetSlideFade);
            }
            float xoffset = ((width * TILE_SIZE) / 2) - (mediaManager.textWidth(notification.font, notification.text) / 2) - notification.scroll;
            render_drawFont(notification.font, notification.text, notification.color_a, MathUtils.round(xoffset), (inputState.internalResolutionHeight - TILE_SIZE - (y * TILE_SIZE)) + 1 + yOffsetSlideFade);
            y = y + 1;
        }

        render_batchLoadColor();
        return;
    }

    private void render_drawWindow(Window window) {
        if (!window.visible) return;
        render_batchSaveColor();
        render_batchSetColor(window.color_r, window.color_g, window.color_b, window.color_a);
        for (int wx = 0; wx < window.width; wx++) {
            if (!window.folded) {
                for (int wy = 0; wy < window.height; wy++) {
                    render_drawCMediaImage(GUIBaseMedia.GUI_WINDOW, window.x + (wx * TILE_SIZE), window.y + (wy * TILE_SIZE), render_getWindowCMediaIndex(wx, wy, window.width, window.height, window.hasTitleBar));
                }
            } else {
                render_drawCMediaImage(GUIBaseMedia.GUI_WINDOW, window.x + (wx * TILE_SIZE), window.y + ((window.height - 1) * TILE_SIZE), render_getWindowCMediaIndex(wx, (window.height - 1), window.width, window.height, window.hasTitleBar));
            }
        }

        if (window.hasTitleBar) {
            render_drawFont(window.font, window.title, window.color_a, window.x, window.y + (window.height * TILE_SIZE) - TILE_SIZE, 1, 2, window.icon, window.iconIndex, (window.width - 1) * TILE_SIZE);
        }
        // Draw Components

        if (!window.folded) {
            for (Component component : window.components) {
                render_drawComponent(component);
            }
        } else {
            for (Component component : window.components) {
                if (component.y == window.height - 1) render_drawComponent(component);
            }
        }

        // Draw combobox menu
        if (!window.folded) {
            for (Component component : window.components) {
                render_drawComponentTopLayer(window, component);
            }
        }

        render_batchLoadColor();
    }

    private void render_drawComponentTopLayer(Window window, Component component) {
        if (!render_isComponentRendered(component)) ;
        float alpha = (window != null ? (component.color_a * window.color_a) : component.color_a);
        render_batchSetColor(component.color_r, component.color_g, component.color_b, alpha);
        if (component.getClass() == ComboBox.class) {
            ComboBox combobox = (ComboBox) component;
            // Menu
            if (combobox.menuOpen) {
                int width = combobox.width;
                int height = combobox.items.size();
                /* Menu */
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int index = render_getComponent9TilesCMediaIndex(x, y, width, height);//x==0 ? 0 : (x == (width-1)) ? 2 : 1;
                        CMediaArray cMenuTexture = null;
                        if (Tools.Calc.pointRectsCollide(inputState.mouse_x_gui, inputState.mouse_y_gui, UICommons.component_getAbsoluteX(combobox), UICommons.component_getAbsoluteY(combobox) - (TILE_SIZE) - (y * TILE_SIZE), combobox.width * TILE_SIZE, TILE_SIZE)) {
                            cMenuTexture = GUIBaseMedia.GUI_COMBOBOX_LIST_SELECTED;
                        } else {
                            cMenuTexture = GUIBaseMedia.GUI_COMBOBOX_LIST;
                        }
                        render_drawCMediaImage(cMenuTexture, UICommons.component_getAbsoluteX(combobox) + (x * TILE_SIZE), UICommons.component_getAbsoluteY(combobox) - (y * TILE_SIZE) - TILE_SIZE, index);
                    }
                }

                /* Text */
                for (int y = 0; y < combobox.items.size(); y++) {
                    Object item = combobox.items.get(y);
                    render_drawFont(combobox.font, combobox.comboBoxAction.text(item), alpha, UICommons.component_getAbsoluteX(combobox), UICommons.component_getAbsoluteY(combobox) - (y * TILE_SIZE) - TILE_SIZE, 1, 2, combobox.comboBoxAction.icon(item), combobox.comboBoxAction.iconArrayIndex(item), (combobox.width * TILE_SIZE));
                }
            }

        }

    }

    private void render_drawComponent(Component component) {
        if (!render_isComponentRendered(component)) return;

        float alpha = (component.addedToWindow != null ? (component.color_a * component.addedToWindow.color_a) : component.color_a);
        float alpha2 = (component.addedToWindow != null ? (component.color2_a * component.addedToWindow.color_a) : component.color2_a);
        boolean disableShaderState = render_GrayScaleShaderEnabled();
        if (component.disabled) render_enableGrayScaleShader(true);

        render_batchSetColor(component.color_r, component.color_g, component.color_b, alpha);

        if (component instanceof Button) {
            Button button = (Button) component;
            CMediaArray buttonMedia = (button.pressed ? GUIBaseMedia.GUI_BUTTON_PRESSED : GUIBaseMedia.GUI_BUTTON);
            int pressed_offset = button.pressed ? 1 : 0;

            for (int wx = 0; wx < button.width; wx++) {
                for (int wy = 0; wy < button.height; wy++) {
                    render_drawCMediaImage(buttonMedia, UICommons.component_getAbsoluteX(button) + (wx * TILE_SIZE), UICommons.component_getAbsoluteY(button) + (wy * TILE_SIZE), render_getComponent16TilesCMediaIndex(wx, wy, button.width, button.height));
                }
            }
            if (button.getClass() == TextButton.class) {
                TextButton textButton = (TextButton) button;
                if (textButton.text != null) {
                    render_drawFont(textButton.font, textButton.text, alpha2, UICommons.component_getAbsoluteX(textButton) + textButton.offset_content_x + pressed_offset, UICommons.component_getAbsoluteY(button) + textButton.offset_content_y - pressed_offset, 1, 2, textButton.icon, textButton.iconArray, (button.width) * TILE_SIZE);
                }
            } else if (button.getClass() == ImageButton.class) {
                ImageButton imageButton = (ImageButton) button;
                render_batchSaveColor();
                render_batchSetColor(imageButton.color2_r, imageButton.color2_g, imageButton.color2_b, alpha2);
                render_drawCMediaImage(imageButton.image, UICommons.component_getAbsoluteX(imageButton) + imageButton.offset_content_x + pressed_offset, UICommons.component_getAbsoluteY(imageButton) + imageButton.offset_content_y - pressed_offset, imageButton.arrayIndex);
                render_batchLoadColor();
            }
        } else if (component.getClass() == Image.class) {
            Image image = (Image) component;
            if (image.image != null) {
                render_drawCMediaImage(image.image, UICommons.component_getAbsoluteX(image), UICommons.component_getAbsoluteY(image), image.arrayIndex);
            }
        } else if (component.getClass() == Text.class) {
            Text text = (Text) component;
            int textHeight = ((text.height - 1) * TILE_SIZE);
            if (text.lines != null && text.lines.length > 0) {
                for (int i = 0; i < text.lines.length; i++) {
                    render_drawFont(text.font, text.lines[i], alpha, UICommons.component_getAbsoluteX(text), UICommons.component_getAbsoluteY(text) + textHeight - (i * TILE_SIZE), 1, 1);
                }
            }
        } else if (component.getClass() == ScrollBarVertical.class) {
            ScrollBarVertical scrollBarVertical = (ScrollBarVertical) component;
            for (int i = 0; i < scrollBarVertical.height; i++) {
                int index = (i == 0 ? 2 : (i == (scrollBarVertical.height - 1) ? 0 : 1));
                render_drawCMediaImage(GUIBaseMedia.GUI_SCROLLBAR_VERTICAL, UICommons.component_getAbsoluteX(scrollBarVertical), UICommons.component_getAbsoluteY(scrollBarVertical) + (i * TILE_SIZE), index);
                int buttonYOffset = MathUtils.round(scrollBarVertical.scrolled * ((scrollBarVertical.height - 1) * TILE_SIZE));
                render_batchSaveColor();
                render_batchSetColor(scrollBarVertical.color2_r, scrollBarVertical.color2_g, scrollBarVertical.color2_b, alpha2);
                render_drawCMediaImage(GUIBaseMedia.GUI_SCROLLBAR_BUTTON_VERTICAL, UICommons.component_getAbsoluteX(scrollBarVertical), UICommons.component_getAbsoluteY(scrollBarVertical) + buttonYOffset, (scrollBarVertical.buttonPressed ? 1 : 0));
                render_batchLoadColor();
            }
        } else if (component.getClass() == ScrollBarHorizontal.class) {
            ScrollBarHorizontal scrollBarHorizontal = (ScrollBarHorizontal) component;
            for (int i = 0; i < scrollBarHorizontal.width; i++) {
                int index = (i == 0 ? 0 : (i == (scrollBarHorizontal.width - 1) ? 2 : 1));
                render_drawCMediaImage(GUIBaseMedia.GUI_SCROLLBAR_HORIZONTAL, UICommons.component_getAbsoluteX(scrollBarHorizontal) + (i * TILE_SIZE), UICommons.component_getAbsoluteY(scrollBarHorizontal), index);
                int buttonXOffset = MathUtils.round(scrollBarHorizontal.scrolled * ((scrollBarHorizontal.width - 1) * TILE_SIZE));
                render_batchSaveColor();
                render_batchSetColor(scrollBarHorizontal.color2_r, scrollBarHorizontal.color2_g, scrollBarHorizontal.color2_b, alpha2);
                render_drawCMediaImage(GUIBaseMedia.GUI_SCROLLBAR_BUTTON_HORIZONAL, UICommons.component_getAbsoluteX(scrollBarHorizontal) + buttonXOffset, UICommons.component_getAbsoluteY(scrollBarHorizontal), (scrollBarHorizontal.buttonPressed ? 1 : 0));
                render_batchLoadColor();
            }
        } else if (component.getClass() == List.class) {
            List list = (List) component;
            boolean itemsValid = (list.items != null && list.items.size() > 0 && list.listAction != null);
            int itemFrom = 0;
            if (itemsValid) {
                itemFrom = MathUtils.round(list.scrolled * ((list.items.size()) - (list.height)));
                itemFrom = Tools.Calc.lowerBounds(itemFrom, 0);
            }
            boolean dragEnabled = false;
            boolean dragValid = false;
            int drag_x = -1, drag_y = -1;
            if ((inputState.listDrag_Item != null || inputState.inventoryDrag_Item != null) && list == inputState.lastGUIMouseHover) {
                dragEnabled = true;
                dragValid = list_canDragIntoList(list);
                if (dragValid) {
                    drag_x = UICommons.component_getAbsoluteX(list);
                    int y_list = UICommons.component_getAbsoluteY(list);
                    drag_y = y_list + ((inputState.mouse_y_gui - y_list) / TILE_SIZE) * TILE_SIZE;
                }
            }

            boolean grayScaleBefore = render_GrayScaleShaderEnabled();
            if (dragEnabled && !dragValid) {
                render_enableGrayScaleShader(true);
            }

            // List
            for (int y = 0; y < list.height; y++) {
                int itemIndex = itemFrom + y;
                int itemOffsetY = (((list.height - 1)) - (y));
                Object item = null;
                if (list.items != null && list.items.size() > 0 && list.listAction != null) {
                    if (itemIndex < list.items.size()) {
                        item = list.items.get(itemIndex);
                    }
                }

                boolean selected = item != null ? (list.multiSelect ? list.selectedItems.contains(item) : (list.selectedItem == item)) : false;

                // Cell

                FColor cellColor = null;
                if (list.listAction != null && list.items != null) {
                    if (itemIndex < list.items.size()) {
                        cellColor = list.listAction.cellColor(item);
                        if (cellColor != null) {
                            render_batchSaveColor();
                            render_batchSetColor(cellColor.r, cellColor.g, cellColor.b, 1);
                        }
                    }
                }
                for (int x = 0; x < list.width; x++) {
                    this.render_drawCMediaImage(selected ? GUIBaseMedia.GUI_LIST_SELECTED : GUIBaseMedia.GUI_LIST, UICommons.component_getAbsoluteX(list) + (x * TILE_SIZE), UICommons.component_getAbsoluteY(list) + itemOffsetY * TILE_SIZE);
                }
                if (cellColor != null) render_batchLoadColor();

                // Text
                if (item != null) {
                    String text = list.listAction.text(item);
                    render_drawFont(list.font, text, alpha, UICommons.component_getAbsoluteX(list), UICommons.component_getAbsoluteY(list) + itemOffsetY * TILE_SIZE, 1, 2, list.listAction.icon(item), list.listAction.iconArrayIndex(item), (list.width * TILE_SIZE));
                }
            }

            if (dragEnabled && dragValid) {
                for (int x = 0; x < list.width; x++) {
                    this.render_drawCMediaImage(GUIBaseMedia.GUI_LIST_DRAG, drag_x + (x * TILE_SIZE), drag_y, render_getListDragCMediaIndex(x, list.width));
                }
            }

            render_enableGrayScaleShader(grayScaleBefore);


        } else if (component.getClass() == ComboBox.class) {
            ComboBox combobox = (ComboBox) component;

            // Box
            for (int w = 0; w < combobox.width; w++) {
                int index = w == 0 ? 0 : (w == combobox.width - 1 ? 2 : 1);
                CMediaGFX comboMedia = combobox.menuOpen ? GUIBaseMedia.GUI_COMBOBOX_OPEN : GUIBaseMedia.GUI_COMBOBOX;
                this.render_drawCMediaImage(comboMedia, UICommons.component_getAbsoluteX(combobox) + (w * TILE_SIZE), UICommons.component_getAbsoluteY(combobox), index);
            }
            // Text
            if (combobox.selectedItem != null && combobox.comboBoxAction != null) {
                render_drawFont(combobox.font, combobox.comboBoxAction.text(combobox.selectedItem), alpha, UICommons.component_getAbsoluteX(combobox), UICommons.component_getAbsoluteY(combobox), 1, 2, combobox.comboBoxAction.icon(combobox.selectedItem), combobox.comboBoxAction.iconArrayIndex(combobox.selectedItem), (combobox.width - 2) * TILE_SIZE);
            }
        } else if (component.getClass() == Knob.class) {
            Knob knob = (Knob) component;

            render_drawCMediaImage(GUIBaseMedia.GUI_KNOB_BACKGROUND, UICommons.component_getAbsoluteX(knob), UICommons.component_getAbsoluteY(knob));
            render_batchSaveColor();
            render_batchSetColor(knob.color2_r, knob.color2_g, knob.color2_b, knob.color2_a);
            if (knob.endless) {
                int index = MathUtils.round(knob.turned * 36);
                render_drawCMediaImage(GUIBaseMedia.GUI_KNOB_ENDLESS, UICommons.component_getAbsoluteX(knob), UICommons.component_getAbsoluteY(knob), index);
            } else {
                int index = MathUtils.round(knob.turned * 28);
                render_drawCMediaImage(GUIBaseMedia.GUI_KNOB, UICommons.component_getAbsoluteX(knob), UICommons.component_getAbsoluteY(knob), index);
            }
            render_batchLoadColor();
        } else if (component.getClass() == Map.class) {
            Map map = (Map) component;
            inputState.spriteBatch_gui.draw(map.texture, UICommons.component_getAbsoluteX(map), UICommons.component_getAbsoluteY(map));

            map.overlays.removeIf(mapOverlay -> {
                if (mapOverlay.fadeOut) {
                    mapOverlay.color_a = 1 - ((System.currentTimeMillis() - mapOverlay.timer) / (float) mapOverlay.fadeOutTime);
                    if (mapOverlay.color_a <= 0) return true;
                }
                render_batchSaveColor();
                render_batchSetColor(mapOverlay.color_r, mapOverlay.color_g, mapOverlay.color_b, alpha * mapOverlay.color_a);
                render_drawCMediaImage(mapOverlay.image, UICommons.component_getAbsoluteX(map) + mapOverlay.x, UICommons.component_getAbsoluteY(map) + mapOverlay.y, mapOverlay.arrayIndex);
                render_batchLoadColor();
                return false;
            });

        } else if (component.getClass() == TextField.class) {
            TextField textField = (TextField) component;
            for (int wx = 0; wx < textField.width; wx++) {
                int index = wx == (textField.width - 1) ? 2 : (wx == 0) ? 0 : 1;

                render_drawCMediaImage(inputState.focusedTextField == textField ? GUIBaseMedia.GUI_TEXTFIELD_FOCUSED : GUIBaseMedia.GUI_TEXTFIELD, UICommons.component_getAbsoluteX(textField) + (wx * TILE_SIZE), UICommons.component_getAbsoluteY(textField), index);


                if (!textField.contentValid) {
                    render_batchSaveColor();
                    render_batchSetColor(0.90588236f, 0.29803923f, 0.23529412f, 0.2f);
                    render_drawCMediaImage(GUIBaseMedia.GUI_TEXTFIELD_VALIDATION_OVERLAY, UICommons.component_getAbsoluteX(textField) + (wx * TILE_SIZE), UICommons.component_getAbsoluteY(textField), index);
                    render_batchLoadColor();
                }

                if (textField.content != null) {
                    render_drawFont(textField.font, textField.content.substring(textField.offset), alpha, UICommons.component_getAbsoluteX(textField), UICommons.component_getAbsoluteY(textField), 2, 2, (textField.width * TILE_SIZE) - 4);
                }
                if (textField.focused) {
                    int xOffset = mediaManager.textWidth(textField.font, textField.content.substring(textField.offset, textField.markerPosition)) + 2;
                    if (xOffset < textField.width * TILE_SIZE) {
                        render_drawCMediaImage(GUIBaseMedia.GUI_TEXTFIELD_CARET, UICommons.component_getAbsoluteX(textField) + xOffset, UICommons.component_getAbsoluteY(textField));
                    }
                }
            }

        } else if (component.getClass() == Inventory.class) {
            Inventory inventory = (Inventory) component;

            int tileSize = inventory.doubleSized ? TILE_SIZE * 2 : TILE_SIZE;
            int inventoryWidth = inventory.items.length;
            int inventoryHeight = inventory.items[0].length;


            boolean dragEnabled = false;
            boolean dragValid = false;
            int drag_x = -1, drag_y = -1;
            if ((inputState.inventoryDrag_Item != null || inputState.listDrag_Item != null) && inventory == inputState.lastGUIMouseHover) {
                dragEnabled = true;
                dragValid = inventory_canDragIntoInventory(inventory);
                if (dragValid) {
                    int x_inventory = UICommons.component_getAbsoluteX(inventory);
                    int y_inventory = UICommons.component_getAbsoluteY(inventory);
                    int m_x = inputState.mouse_x_gui - x_inventory;
                    int m_y = inputState.mouse_y_gui - y_inventory;
                    if (m_x > 0 && m_x < (inventory.width * tileSize) && m_y > 0 && m_y < (inventory.height * tileSize)) {
                        int inv_x = m_x / tileSize;
                        int inv_y = m_y / tileSize;
                        if (UICommons.inventory_positionValid(inventory, inv_x, inv_y)) {
                            drag_x = inv_x;
                            drag_y = inv_y;
                        }
                    }
                }
            }

            boolean grayScaleBefore = render_GrayScaleShaderEnabled();
            if (dragEnabled && !dragValid) render_enableGrayScaleShader(true);

            for (int x = 0; x < inventoryWidth; x++) {
                for (int y = 0; y < inventoryHeight; y++) {
                    if (inventory.items != null && x <= inventoryWidth && y <= inventoryHeight) {
                        CMediaGFX cellMedia;
                        boolean selected = (inventory.items[x][y] != null && inventory.items[x][y] == inventory.selectedItem) ? true : false;
                        if (dragEnabled && dragValid && drag_x == x && drag_y == y) {
                            cellMedia = inventory.doubleSized ? GUIBaseMedia.GUI_INVENTORY_DRAGGED_X2 : GUIBaseMedia.GUI_INVENTORY_DRAGGED;
                        } else {
                            if (selected) {
                                cellMedia = inventory.doubleSized ? GUIBaseMedia.GUI_INVENTORY_SELECTED_X2 : GUIBaseMedia.GUI_INVENTORY_SELECTED;
                            } else {
                                cellMedia = inventory.doubleSized ? GUIBaseMedia.GUI_INVENTORY_X2 : GUIBaseMedia.GUI_INVENTORY;
                            }
                        }

                        render_batchSaveColor();
                        render_batchSetColorWhite();
                        if (inventory.inventoryAction != null) {
                            FColor cellColor = inventory.inventoryAction.cellColor(inventory.items[x][y], x, y);
                            if (cellColor != null) {
                                render_batchSetColor(cellColor.r, cellColor.g, cellColor.b, 1f);
                            }
                        }

                        int index = inventory.doubleSized ? render_getComponent16TilesCMediaIndex(x, y, inventory.width / 2, inventory.height / 2) : render_getComponent16TilesCMediaIndex(x, y, inventory.width, inventory.height);
                        render_drawCMediaImage(cellMedia, UICommons.component_getAbsoluteX(inventory) + (x * tileSize), UICommons.component_getAbsoluteY(inventory) + (y * tileSize), index);

                        render_batchSetColorWhite();

                        CMediaGFX icon = (inventory.items[x][y] != null && inventory.inventoryAction != null) ? inventory.inventoryAction.icon(inventory.items[x][y]) : null;

                        if (icon != null) {
                            int iconIndex = inventory.inventoryAction != null ? inventory.inventoryAction.iconArrayIndex(inventory.items[x][y]) : 0;
                            render_drawCMediaImage(icon, UICommons.component_getAbsoluteX(inventory) + (x * tileSize), UICommons.component_getAbsoluteY(inventory) + (y * tileSize), iconIndex);
                        }
                        render_batchLoadColor();

                    }
                }
            }
            render_enableGrayScaleShader(grayScaleBefore);
        } else if (component.getClass() == TabBar.class) {
            TabBar tabBar = (TabBar) component;
            int tabXOffset = tabBar.tabOffset;
            int topBorder = 0;
            drawTabBarLoop:
            for (int i = 0; i < tabBar.tabs.size(); i++) {
                Tab tab = tabBar.tabs.get(i);
                if ((tabXOffset + tab.width) > tabBar.width) {
                    break drawTabBarLoop;
                }
                CMediaGFX tabGraphics = i == tabBar.selectedTab ? GUIBaseMedia.GUI_TAB_SELECTED : GUIBaseMedia.GUI_TAB;

                for (int x = 0; x < tab.width; x++) {
                    render_drawCMediaImage(tabGraphics, UICommons.component_getAbsoluteX(tabBar) + (x * TILE_SIZE) + (tabXOffset * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar), tab_getCMediaIndex(x, tab.width));
                }

                render_drawFont(tab.font, tab.title, alpha, UICommons.component_getAbsoluteX(tabBar) + (tabXOffset * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar), 2, 1, tab.icon, tab.iconIndex);
                tabXOffset += tab.width;

            }

            topBorder = tabBar.width - tabXOffset;
            for (int ix = 0; ix < topBorder; ix++) {
                render_drawCMediaImage(GUIBaseMedia.GUI_TAB_BORDERS, UICommons.component_getAbsoluteX(tabBar) + ((tabXOffset + ix) * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar), 2);
            }

            if (tabBar.border) {


                for (int ix = 0; ix < tabBar.width; ix++) {
                    render_drawCMediaImage(GUIBaseMedia.GUI_TAB_BORDERS, UICommons.component_getAbsoluteX(tabBar) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar) - (tabBar.borderHeight * TILE_SIZE), 2);
                }

                for (int iy = 0; iy < tabBar.borderHeight; iy++) {
                    render_drawCMediaImage(GUIBaseMedia.GUI_TAB_BORDERS, UICommons.component_getAbsoluteX(tabBar), UICommons.component_getAbsoluteY(tabBar) - ((iy + 1) * TILE_SIZE), 0);
                    render_drawCMediaImage(GUIBaseMedia.GUI_TAB_BORDERS, UICommons.component_getAbsoluteX(tabBar) + ((tabBar.width - 1) * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar) - ((iy + 1) * TILE_SIZE), 1);


                }

            }

        } else if (component instanceof Shape) {
            Shape shape = (Shape) component;
            if (shape.shapeType != null) {
                CMediaImage shapeImage = switch (shape.shapeType) {
                    case OVAL -> GUIBaseMedia.GUI_SHAPE_OVAL;
                    case RECT -> GUIBaseMedia.GUI_SHAPE_RECT;
                    case DIAMOND -> GUIBaseMedia.GUI_SHAPE_DIAMOND;
                    case TRIANGLE_LEFT_DOWN -> GUIBaseMedia.GUI_SHAPE_TRIANGLE_LEFT_DOWN;
                    case TRIANGLE_RIGHT_DOWN -> GUIBaseMedia.GUI_SHAPE_TRIANGLE_RIGHT_DOWN;
                    case TRIANGLE_LEFT_UP -> GUIBaseMedia.GUI_SHAPE_TRIANGLE_LEFT_UP;
                    case TRIANGLE_RIGHT_UP -> GUIBaseMedia.GUI_SHAPE_TRIANGLE_RIGHT_UP;
                };
                mediaManager.drawCMediaImage(inputState.spriteBatch_gui, shapeImage, UICommons.component_getAbsoluteX(shape), UICommons.component_getAbsoluteY(shape),
                        0, 0, shape.width * TILE_SIZE, shape.height * TILE_SIZE);
            }
        } else if (component instanceof ProgressBar) {
            ProgressBar progressBar = (ProgressBar) component;
            // Bar Background
            for (int i = 0; i < progressBar.width; i++) {
                int index = i == 0 ? 0 : i == (progressBar.width - 1) ? 2 : 1;
                render_drawCMediaImage(GUIBaseMedia.GUI_PROGRESSBAR, UICommons.component_getAbsoluteX(progressBar) + (i * TILE_SIZE), UICommons.component_getAbsoluteY(progressBar), index);
            }

            // Bar Bar
            render_batchSaveColor();
            render_batchSetColor(progressBar.color2_r, progressBar.color2_g, progressBar.color2_b, alpha2);
            int pixels = MathUtils.round(progressBar.progress * (progressBar.width * TILE_SIZE));
            for (int i = 0; i < progressBar.width; i++) {
                int xOffset = i * TILE_SIZE;
                int index = i == 0 ? 0 : i == (progressBar.width - 1) ? 2 : 1;
                if (xOffset < pixels) {
                    if (pixels - xOffset < TILE_SIZE) {
                        mediaManager.drawCMediaArrayCut(inputState.spriteBatch_gui, GUIBaseMedia.GUI_PROGRESSBAR_BAR, UICommons.component_getAbsoluteX(progressBar) + xOffset, UICommons.component_getAbsoluteY(progressBar), index, pixels - xOffset, TILE_SIZE);
                    } else {
                        mediaManager.drawCMediaArray(inputState.spriteBatch_gui, GUIBaseMedia.GUI_PROGRESSBAR_BAR, UICommons.component_getAbsoluteX(progressBar) + xOffset, UICommons.component_getAbsoluteY(progressBar), index);
                    }
                }
            }
            render_batchLoadColor();

            if (progressBar.progressText) {
                String percentTxt = progressBar.progressText2Decimal ? Tools.Text.formatPercent2Decimal(progressBar.progress) : Tools.Text.formatPercent(progressBar.progress);
                int xOffset = ((progressBar.width * TILE_SIZE) / 2) - (mediaManager.textWidth(progressBar.font, percentTxt) / 2);
                render_drawFont(progressBar.font, percentTxt, alpha, UICommons.component_getAbsoluteX(progressBar) + xOffset, UICommons.component_getAbsoluteY(progressBar), 0, 2);
            }


        } else if (component.getClass() == CheckBox.class) {
            CheckBox checkBox = (CheckBox) component;

            CMediaArray tex = checkBox.checkBoxStyle == CheckBoxStyle.CHECKBOX ? GUIBaseMedia.GUI_CHECKBOX_CHECKBOX : GUIBaseMedia.GUI_CHECKBOX_RADIO;

            render_drawCMediaImage(tex, UICommons.component_getAbsoluteX(checkBox), UICommons.component_getAbsoluteY(checkBox), checkBox.checked ? 1 : 0);

            render_drawFont(checkBox.font, checkBox.text, alpha, UICommons.component_getAbsoluteX(checkBox) + TILE_SIZE, UICommons.component_getAbsoluteY(checkBox), 1, 2);

        } else if (component.getClass() == GameViewPort.class) {
            GameViewPort gameViewPort = (GameViewPort) component;
            //inputState.spriteBatch_gui.setColor(1, 1, 1, 1f);
            inputState.spriteBatch_gui.draw(gameViewPort.textureRegion, UICommons.component_getAbsoluteX(gameViewPort), UICommons.component_getAbsoluteY(gameViewPort));
        }


        render_enableGrayScaleShader(disableShaderState);
        return;
    }

    private void render_drawCursorListDrags() {
        render_batchSaveColor();

        if (inputState.inventoryDrag_Item != null) {
            if (inputState.inventoryDrag_Inventory != null && inputState.inventoryDrag_Inventory.inventoryAction != null) {
                render_batchSetColor(1, 1, 1, api.config.getDragTransparency());
                CMediaGFX icon = inputState.inventoryDrag_Inventory.inventoryAction.icon(inputState.inventoryDrag_Item);
                render_drawCMediaImage(icon, inputState.mouse_x_gui - inputState.InventoryDrag_offset_x, inputState.mouse_y_gui - inputState.InventoryDrag_offset_y, inputState.inventoryDrag_Inventory.inventoryAction.iconArrayIndex(inputState.inventoryDrag_Item));
            }
        } else if (inputState.listDrag_Item != null) {
            if (inputState.listDrag_List.listAction != null) {

                // List
                render_batchSetColor(inputState.listDrag_List.color_r, inputState.listDrag_List.color_g, inputState.listDrag_List.color_b, Math.min(inputState.listDrag_List.color_a, api.config.getDragTransparency()));
                for (int x = 0; x < inputState.listDrag_List.width; x++) {
                    this.render_drawCMediaImage(GUIBaseMedia.GUI_LIST_SELECTED, inputState.mouse_x_gui - inputState.listDrag_offset_x + (x * TILE_SIZE), inputState.mouse_y_gui - inputState.listDrag_offset_y);
                }

                // Text
                String text = inputState.listDrag_List.listAction.text(inputState.listDrag_Item);
                render_drawFont(inputState.listDrag_List.font, text, inputState.listDrag_List.color_a, inputState.mouse_x_gui - inputState.listDrag_offset_x, inputState.mouse_y_gui - inputState.listDrag_offset_y, 2, 2, inputState.listDrag_List.listAction.icon(inputState.listDrag_Item), inputState.listDrag_List.listAction.iconArrayIndex(inputState.listDrag_Item), (inputState.listDrag_List.width * TILE_SIZE));
            }
        }


        render_batchSetColorWhite();
        render_batchLoadColor();
        return;
    }


    private boolean render_GrayScaleShaderEnabled() {
        return inputState.spriteBatch_gui.getShader() == inputState.grayScaleShader;
    }

    private void render_enableGrayScaleShader(boolean enable) {
        if (enable) {
            if (!render_GrayScaleShaderEnabled()) inputState.spriteBatch_gui.setShader(inputState.grayScaleShader);
        } else {
            if (render_GrayScaleShaderEnabled()) inputState.spriteBatch_gui.setShader(null);
        }

    }


    private void render_drawFont(CMediaFont font, String text, float alpha, int x, int y) {
        render_drawFont(font, text, alpha, x, y, 0, 0, null, 0, -1);
    }


    private void render_drawFont(CMediaFont font, String text, float alpha, int x, int y, int textXOffset, int textYOffset) {
        render_drawFont(font, text, alpha, x, y, textXOffset, textYOffset, null, 0, -1);
    }

    private void render_drawFont(CMediaFont font, String text, float alpha, int x, int y, int textXOffset, int textYOffset, int maxWidth) {
        render_drawFont(font, text, alpha, x, y, textXOffset, textYOffset, null, 0, maxWidth);
    }

    private void render_drawFont(CMediaFont font, String text, float alpha, int x, int y, int textXOffset, int textYOffset, CMediaGFX icon, int iconIndex) {
        render_drawFont(font, text, alpha, x, y, textXOffset, textYOffset, icon, iconIndex, -1);
    }

    private void render_drawFont(CMediaFont font, String text, float alpha, int x, int y, int textXOffset, int textYOffset, CMediaGFX icon, int iconIndex, int maxWidth) {

        boolean withIcon = icon != null;
        if (withIcon) {
            render_batchSaveColor();
            render_batchSetColorWhite();
            render_drawCMediaImage(icon, x, y, iconIndex);
            if (maxWidth != -1) maxWidth = maxWidth - TILE_SIZE;
            render_batchLoadColor();
        }

        render_fontSaveColor(font);
        render_fontSetColor(font, 1, 1, 1, alpha);
        if (maxWidth == -1) {
            mediaManager.drawCMediaFont(inputState.spriteBatch_gui, font, x + (withIcon ? TILE_SIZE : 0) + textXOffset, y + textYOffset, text);
        } else {
            mediaManager.drawCMediaFont(inputState.spriteBatch_gui, font, x + (withIcon ? TILE_SIZE : 0) + textXOffset, y + textYOffset, text, maxWidth);
        }
        render_fontLoadColor(font);
    }

    private void render_fontSetColor(CMediaFont font, float r, float g, float b, float a) {
        mediaManager.getCMediaFont(font).setColor(r, g, b, a);
    }

    private void render_fontSetColorWhite() {
        inputState.spriteBatch_gui.setColor(1, 1, 1, 1);
    }

    private Color render_fontGetColor(CMediaFont font) {
        return mediaManager.getCMediaFont(font).getColor();
    }

    private void render_fontLoadColor(CMediaFont font) {
        inputState.tempColorStackPointer = inputState.tempColorStackPointer - 1;
        mediaManager.getCMediaFont(font).setColor(inputState.tempColorStack[inputState.tempColorStackPointer]);
    }

    private void render_fontSaveColor(CMediaFont font) {
        BitmapFont bmpFont = mediaManager.getCMediaFont(font);
        inputState.tempColorStack[inputState.tempColorStackPointer] = new Color(bmpFont.getColor());
        inputState.tempColorStackPointer = inputState.tempColorStackPointer + 1;
    }

    private void render_batchSetColor(float r, float g, float b, float a) {
        inputState.spriteBatch_gui.setColor(r, g, b, a);
    }

    private void render_batchSetColorWhite() {
        inputState.spriteBatch_gui.setColor(1, 1, 1, 1);
    }

    private Color render_batchGetColor() {
        return inputState.spriteBatch_gui.getColor();
    }

    private void render_batchSaveColor() {
        inputState.tempColorStack[inputState.tempColorStackPointer] = new Color(inputState.spriteBatch_gui.getColor());
        inputState.tempColorStackPointer = inputState.tempColorStackPointer + 1;
    }

    private void render_batchLoadColor() {
        inputState.tempColorStackPointer = inputState.tempColorStackPointer - 1;
        inputState.spriteBatch_gui.setColor(inputState.tempColorStack[inputState.tempColorStackPointer]);
    }

    private void render_drawCMediaImage(CMediaGFX cMedia, int x, int y) {
        mediaManager.drawCMediaGFX(inputState.spriteBatch_gui, cMedia, x, y, inputState.animation_timer_gui, 0);
    }

    private void render_drawCMediaImage(CMediaGFX cMedia, int x, int y, int arrayIndex) {
        mediaManager.drawCMediaGFX(inputState.spriteBatch_gui, cMedia, x, y, inputState.animation_timer_gui, arrayIndex);
    }

    public void shutdown() {
        // Lists
        inputState.windows.clear();
        inputState.addWindowQueue.clear();
        inputState.removeHotKeyQueue.clear();
        inputState.modalWindowQueue.clear();
        inputState.addScreenComponentsQueue.clear();
        inputState.removeScreenComponentsQueue.clear();
        inputState.addHotKeyQueue.clear();
        inputState.removeHotKeyQueue.clear();
        inputState.hotKeys.clear();
        inputState.delayedOneshotActions.clear();
        inputState.screenComponents.clear();
        inputState.notifications.clear();
        inputState.gameViewPorts.clear();
        UICommons.resetGUIVariables(inputState);


        // GFX
        inputState.spriteBatch_game.dispose();
        inputState.spriteBatch_gui.dispose();
        inputState.spriteBatch_screen.dispose();

        inputState.texture_game.getTexture().dispose();
        inputState.texture_gui.getTexture().dispose();
        if (inputState.viewportMode == ViewportMode.FIT || inputState.viewportMode == ViewportMode.STRETCH) {
            inputState.texture_upScale.getTexture().dispose();
        }
        inputState.grayScaleShader.dispose();

        inputState = null;

        this.uiAdapter.shutdown();
    }


}
