package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.CMediaArray;
import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.media_manager.media.CMediaGFX;
import org.mslivo.core.engine.media_manager.media.CMediaImage;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.ui_engine.gui.Window;
import org.mslivo.core.engine.ui_engine.gui.actions.CommonActions;
import org.mslivo.core.engine.ui_engine.gui.actions.UpdateAction;
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
import org.mslivo.core.engine.ui_engine.gui.components.progressbar.ProgressBar;
import org.mslivo.core.engine.ui_engine.gui.components.scrollbar.ScrollBar;
import org.mslivo.core.engine.ui_engine.gui.components.scrollbar.ScrollBarHorizontal;
import org.mslivo.core.engine.ui_engine.gui.components.scrollbar.ScrollBarVertical;
import org.mslivo.core.engine.ui_engine.gui.components.shape.Shape;
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
import org.mslivo.core.engine.ui_engine.gui.ostextinput.OnScreenTextInput;
import org.mslivo.core.engine.ui_engine.gui.tool.MouseTool;
import org.mslivo.core.engine.ui_engine.gui.tooltip.ToolTip;
import org.mslivo.core.engine.ui_engine.gui.tooltip.ToolTipImage;
import org.mslivo.core.engine.ui_engine.input.InputEvents;
import org.mslivo.core.engine.ui_engine.input.KeyCode;
import org.mslivo.core.engine.ui_engine.input.UIEngineInputProcessor;
import org.mslivo.core.engine.ui_engine.media.GUIBaseMedia;
import org.mslivo.core.engine.ui_engine.misc.MouseControlMode;
import org.mslivo.core.engine.ui_engine.misc.render.GrayScaleShader;
import org.mslivo.core.engine.ui_engine.misc.render.NestedFrameBuffer;
import org.mslivo.core.engine.ui_engine.misc.render.ViewportMode;

import java.util.ArrayDeque;
import java.util.ArrayList;


/**
 * UI Engine
 * Handles GUI Elements, Input, Cameras
 * Game needs to be implemented inside the uiAdapter
 */
public class UIEngine<T extends UIAdapter> {

    /* Attributes */
    private final T uiAdapter;

    private InputState inputState;

    private final API api;

    private final MediaManager mediaManager;

    /* Constants */
    public static final int TILE_SIZE = 8;
    public static final float TILE_SIZE_F = TILE_SIZE;
    public static final int TILE_SIZE_2 = TILE_SIZE / 2;
    public static final float TILE_SIZE_F2 = TILE_SIZE / 2;
    public static final String WND_CLOSE_BUTTON = "wnd_close_btn";
    public static final int DOUBLECLICK_TIME_MS = 180;
    public static final int COLORSTACK_SIZE = 8;
    private static final int FONT_MAXWIDTH_NONE = -1;

    public T getAdapter() {
        return uiAdapter;
    }

    public UIEngine(T uiAdapter, MediaManager mediaManager, int internalResolutionWidth, int internalResolutionHeight, ViewportMode viewportMode, boolean gamePadSupport) {
        if (uiAdapter == null || mediaManager == null) {
            throw new RuntimeException("Cannot initialize IREngine: invalid parameters");
        }
        this.uiAdapter = uiAdapter;
        this.mediaManager = mediaManager;
        /* Setup */
        this.inputState = initializeInputState(internalResolutionWidth, internalResolutionHeight, viewportMode, gamePadSupport);
        this.api = new API(this.inputState, mediaManager);
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        render_glClear();
        /*  Call Adapter Init */
        this.uiAdapter.init(this.api, this.mediaManager);
    }

    private void render_glClear() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
    }

    private Texture.TextureFilter determineUpscaleTextureFilter(ViewportMode viewportMode) {
        return switch (viewportMode) {
            case PIXEL_PERFECT -> Texture.TextureFilter.Nearest;
            case FIT, STRETCH -> Texture.TextureFilter.Linear;
        };
    }

    private InputState initializeInputState(int internalResolutionWidth, int internalResolutionHeight, ViewportMode viewportMode, boolean gamePadSupport) {
        InputState newInputState = new InputState();

        //  ----- Parameters

        newInputState.internalResolutionWidth = Tools.Calc.lowerBounds(internalResolutionWidth, TILE_SIZE * 2);
        newInputState.internalResolutionHeight = Tools.Calc.lowerBounds(internalResolutionHeight, TILE_SIZE * 2);
        newInputState.viewportMode = viewportMode;
        newInputState.gamePadSupport = gamePadSupport;
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
        newInputState.frameBuffer_game = new NestedFrameBuffer(Pixmap.Format.RGB888, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight, false);
        newInputState.frameBuffer_game.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        newInputState.texture_game = new TextureRegion(newInputState.frameBuffer_game.getColorBufferTexture());
        newInputState.texture_game.flip(false, true);

        // -----  GUI
        newInputState.spriteBatch_gui = new SpriteBatch(8191);
        newInputState.spriteBatch_gui.setBlendFunctionSeparate(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA, GL30.GL_ONE, GL30.GL_ONE_MINUS_SRC_ALPHA);
        newInputState.camera_gui = new OrthographicCamera(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.camera_gui.setToOrtho(false, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.frameBuffer_gui = new NestedFrameBuffer(Pixmap.Format.RGBA8888, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight, false);
        newInputState.frameBuffer_gui.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        newInputState.texture_gui = new TextureRegion(newInputState.frameBuffer_gui.getColorBufferTexture());
        newInputState.texture_gui.flip(false, true);
        // ----- UpScaler
        newInputState.upscaleFactor_screen = UICommons.viewport_determineUpscaleFactor(viewportMode, internalResolutionWidth, internalResolutionHeight);
        newInputState.textureFilter_screen = UICommons.viewport_determineUpscaleTextureFilter(viewportMode);
        newInputState.frameBuffer_screen = new NestedFrameBuffer(Pixmap.Format.RGBA8888, newInputState.internalResolutionWidth * newInputState.upscaleFactor_screen, newInputState.internalResolutionHeight * newInputState.upscaleFactor_screen, false);
        newInputState.frameBuffer_screen.getColorBufferTexture().setFilter(newInputState.textureFilter_screen, newInputState.textureFilter_screen);
        newInputState.texture_screen = new TextureRegion(newInputState.frameBuffer_screen.getColorBufferTexture());
        newInputState.texture_screen.flip(false, true);
        // ----- Screen
        newInputState.spriteBatch_screen = new SpriteBatch(1);
        newInputState.camera_screen = new OrthographicCamera(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.camera_screen.setToOrtho(false);
        newInputState.viewport_screen = UICommons.viewport_createViewport(viewportMode, newInputState.camera_screen, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.viewport_screen.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // -----  GUI
        newInputState.windows = new ArrayList<>();
        newInputState.screenComponents = new ArrayList<>();
        newInputState.openContextMenu = null;
        newInputState.displayedContextMenuWidth = 0;
        newInputState.openMouseTextInput = null;
        newInputState.mTextInputConfirmPressed = false;
        newInputState.mTextInputChangeCasePressed = false;
        newInputState.mTextInputDeletePressed = false;
        newInputState.mTextInputKeyBoardGamePadLeft = false;
        newInputState.mTextInputKeyBoardGamePadRight = false;
        newInputState.mTextInputScrollTimer = 0;
        newInputState.mTextInputScrollTime = 0;
        newInputState.mTextInputScrollSpeed = 0;
        newInputState.mTextInputTranslatedMouse1Down = false;
        newInputState.mTextInputTranslatedMouse2Down = false;
        newInputState.mTextInputUnlock = false;
        newInputState.usedTextFieldThisUpdate = false;
        newInputState.usedHotKeyThisUpdate = false;
        newInputState.modalWindow = null;
        newInputState.modalWindowQueue = new ArrayDeque<>();
        newInputState.focusedTextField = null;
        newInputState.notifications = new ArrayList<>();
        newInputState.hotKeys = new ArrayList<>();
        newInputState.gameViewPorts = new ArrayList<>();
        newInputState.singleUpdateActions = new ArrayList<>();
        newInputState.singleUpdateActionsRemoveQ = new ArrayDeque<>();
        // ----- Temp GUI Variables
        newInputState.draggedWindow = null;
        newInputState.draggedWindow_offset = new GridPoint2();
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
        newInputState.inventoryDrag_offset = new GridPoint2();
        newInputState.inventoryDrag_from = new GridPoint2();
        newInputState.listDrag_Item = null;
        newInputState.listDrag_List = null;
        newInputState.listDrag_offset = new GridPoint2();
        newInputState.listDrag_from_index = 0;
        newInputState.tooltip_lastHoverObject = null;
        newInputState.pressedMap = null;
        newInputState.openComboBox = null;

        // ----- Controls
        newInputState.currentControlMode = MouseControlMode.KEYBOARD;
        newInputState.mouse_gui = new GridPoint2(internalResolutionWidth / 2, internalResolutionHeight / 2);
        newInputState.mouse = new GridPoint2(0, 0);
        newInputState.mouse_delta = new GridPoint2(0, 0);
        newInputState.lastGUIMouseHover = null;
        newInputState.cursor = null;
        newInputState.mouseTool = null;
        newInputState.mouseToolPressed = false;
        newInputState.vector_fboCursor = new Vector3(0, 0, 0);
        newInputState.vector2_unproject = new Vector2(0, 0);
        newInputState.simulatedMouseGUIPosition = new Vector2(internalResolutionWidth / 2, internalResolutionHeight / 2);
        newInputState.hardwareMouseLastPosition = new GridPoint2(0, 0);
        newInputState.simulatedMouseLastMouseClick = 0;
        newInputState.keyBoardMouseSpeedUp = new Vector2(0,0);
        newInputState.simulatedMouseIsButtonDown = new boolean[]{false, false, false, false, false};
        newInputState.keyBoardTranslatedKeysDown = new boolean[256];
        newInputState.gamePadTranslatedButtonsDown = new boolean[15];
        newInputState.gamePadTranslatedStickLeft = new Vector2(0, 0);
        newInputState.gamePadTranslatedStickRight = new Vector2(0, 0);

        // ---- Misc
        newInputState.animation_timer_gui = 0f;
        newInputState.colorStack = new Color[COLORSTACK_SIZE];
        for (int i = 0; i < COLORSTACK_SIZE; i++) newInputState.colorStack[i] = new Color();
        for (int i = 0; i < COLORSTACK_SIZE; i++) newInputState.colorStack[i] = new Color(1, 1, 1, 1);
        newInputState.colorStackPointer = 0;
        ShaderProgram.pedantic = false;
        newInputState.grayScaleShader = new ShaderProgram(GrayScaleShader.VERTEX, GrayScaleShader.FRAGMENT);
        newInputState.camera_frustum = new OrthographicCamera(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.camera_frustum.setToOrtho(false, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.inputEvents = new InputEvents();
        newInputState.inputProcessor = new UIEngineInputProcessor(newInputState.inputEvents, newInputState.gamePadSupport);

        newInputState.itemInfo_listIndex = 0;
        newInputState.itemInfo_tabBarTabIndex = 0;
        newInputState.itemInfo_inventoryPos = new GridPoint2();
        newInputState.itemInfo_listValid = false;
        newInputState.itemInfo_tabBarValid = false;
        newInputState.itemInfo_inventoryValid = false;

        return newInputState;
    }

    public void resize(int width, int height) {
        inputState.viewport_screen.update(width, height, true);
    }

    public void update() {
        // GUI

        this.updateMouseControl(); // Map Keyboard/Gamepad controls to mouse controls
        this.updateLastGUIMouseHover(); // Determine object that is targeted by cursor
        this.updateGUI(); // Main GUI Update happen here
        this.updateGameCamera();
        this.updateMouseCursor();

        // Update Game
        this.uiAdapter.update();

        // Reset Input Events
        this.inputState.inputEvents.reset(); // Reset Inputs
    }


    private void updateKeyInteractions() {
        inputState.usedTextFieldThisUpdate = false;
        inputState.usedHotKeyThisUpdate = false;
        if (api.config.isUiKeyInteractionsDisabled()) return;

        if (inputState.inputEvents.keyTyped) {
            if (inputState.focusedTextField != null) {
                TextField focusedTextField = inputState.focusedTextField; // Into Temp variable because focuseTextField can change after executing actions
                for (int ic = 0; ic < inputState.inputEvents.keyTypedCharacters.size(); ic++) {
                    Character keyTypedCharacter = inputState.inputEvents.keyTypedCharacters.get(ic);
                    if (keyTypedCharacter == '\b') { // BACKSPACE
                        if (!focusedTextField.content.isEmpty() && focusedTextField.markerPosition > 0) {
                            String newContent = focusedTextField.content.substring(0, focusedTextField.markerPosition - 1) + focusedTextField.content.substring(focusedTextField.markerPosition);
                            UICommons.textField_setMarkerPosition(mediaManager, focusedTextField, focusedTextField.markerPosition - 1);
                            UICommons.textField_setContent(inputState.focusedTextField, newContent);
                            if (focusedTextField.textFieldAction != null)
                                focusedTextField.textFieldAction.onContentChange(newContent, focusedTextField.contentValid);
                        }
                    } else if (keyTypedCharacter == '\u007F') { // DEL
                        if (!inputState.focusedTextField.content.isEmpty() && focusedTextField.markerPosition < focusedTextField.content.length()) {
                            String newContent = focusedTextField.content.substring(0, focusedTextField.markerPosition) + focusedTextField.content.substring(focusedTextField.markerPosition + 1);
                            UICommons.textField_setContent(focusedTextField, newContent);
                            if (focusedTextField.textFieldAction != null)
                                focusedTextField.textFieldAction.onContentChange(newContent, focusedTextField.contentValid);
                        }
                    } else if (keyTypedCharacter == '\n') { // Enter
                        if (focusedTextField.textFieldAction != null)
                            focusedTextField.textFieldAction.onEnter(focusedTextField.content, focusedTextField.contentValid);
                        UICommons.textField_unFocus(inputState, focusedTextField); // Unfocus
                    } else {
                        if (focusedTextField.allowedCharacters == null || focusedTextField.allowedCharacters.contains(keyTypedCharacter)) {
                            String newContent = focusedTextField.content.substring(0, focusedTextField.markerPosition) + keyTypedCharacter + focusedTextField.content.substring(focusedTextField.markerPosition);
                            UICommons.textField_setContent(focusedTextField, newContent);
                            UICommons.textField_setMarkerPosition(mediaManager, focusedTextField, focusedTextField.markerPosition + 1);
                            if (focusedTextField.textFieldAction != null)
                                focusedTextField.textFieldAction.onContentChange(newContent, focusedTextField.contentValid);
                        }
                    }

                    // Execute Typed Character Action
                    if (focusedTextField.textFieldAction != null)
                        focusedTextField.textFieldAction.onTyped(keyTypedCharacter);

                    inputState.usedTextFieldThisUpdate = true;
                }
            }
        }
        if (inputState.inputEvents.keyDown) {
            if (inputState.focusedTextField != null) {
                TextField focusedTextField = inputState.focusedTextField;
                for (int ik = 0; ik < inputState.inputEvents.keyDownKeyCodes.size(); ik++) {
                    int keyDownKeyCode = inputState.inputEvents.keyDownKeyCodes.get(ik);
                    if (keyDownKeyCode == Input.Keys.LEFT) {
                        UICommons.textField_setMarkerPosition(mediaManager, focusedTextField, focusedTextField.markerPosition - 1);
                    } else if (keyDownKeyCode == Input.Keys.RIGHT) {
                        UICommons.textField_setMarkerPosition(mediaManager, focusedTextField, focusedTextField.markerPosition + 1);
                    } else if (keyDownKeyCode == Input.Keys.HOME) {
                        UICommons.textField_setMarkerPosition(mediaManager, focusedTextField, focusedTextField.content.length());
                    } else if (keyDownKeyCode == Input.Keys.END) {
                        UICommons.textField_setMarkerPosition(mediaManager, focusedTextField, 0);
                    }
                    inputState.usedTextFieldThisUpdate = true;
                }
            } else {
                // Hotkeys
                for (int ihk = 0; ihk < inputState.hotKeys.size(); ihk++) {
                    HotKey hotKey = inputState.hotKeys.get(ihk);
                    boolean hotKeyPressed = true;
                    hkLoop:
                    for (int ikc = 0; ikc < hotKey.keyCodes.length; ikc++) {
                        if(inputState.inputEvents.keysDown[hotKey.keyCodes[ikc]]){
                            inputState.usedHotKeyThisUpdate = true;
                        }else{
                            hotKeyPressed = false;
                            break hkLoop;
                        }
                    }
                    if (hotKeyPressed) {
                        hotKey.pressed = true;
                        if (hotKey.hotKeyAction != null) hotKey.hotKeyAction.onPress();
                    }
                }
            }
        }
        if (inputState.inputEvents.keyUp) {
            // Reset Hotkeys
            for (int ihk = 0; ihk < inputState.hotKeys.size(); ihk++) {
                HotKey hotKey = inputState.hotKeys.get(ihk);
                if (hotKey.pressed) {
                    boolean hotKeyPressed = true;
                    hkLoop:
                    for (int ikc = 0; ikc < hotKey.keyCodes.length; ikc++) {
                        if (!inputState.inputEvents.keysDown[hotKey.keyCodes[ikc]]) {
                            hotKeyPressed = false;
                            break hkLoop;
                        }
                    }
                    if (!hotKeyPressed) {
                        hotKey.pressed = false;
                        if (hotKey.hotKeyAction != null) hotKey.hotKeyAction.onRelease();
                    }
                }

            }


        }
    }


    private void updateMouseInteractions() {
        if (api.config.isUiMouseInteractionsDisabled()) return;

        if (inputState.inputEvents.mouseDoubleClick) {
            boolean processMouseClick = true;
            if (inputState.lastGUIMouseHover != null) {
                if (inputState.modalWindow != null && inputState.lastGUIMouseHover != inputState.modalWindow) {
                    processMouseClick = false;
                }
            } else {
                processMouseClick = false;
            }

            if (processMouseClick) {
                if (inputState.lastGUIMouseHover.getClass() == Window.class) {
                    Window window = (Window) inputState.lastGUIMouseHover;
                    for (int ib = 0; ib < inputState.inputEvents.mouseDownButtons.size(); ib++) {
                        int mouseDownButton = inputState.inputEvents.mouseDownButtons.get(ib);
                        if (api.config.isFoldWindowsOnDoubleClick() && mouseDownButton == Input.Buttons.LEFT) {
                            if (window.hasTitleBar && window.foldable && Tools.Calc.pointRectsCollide(inputState.mouse_gui.x, inputState.mouse_gui.y, window.x, window.y + ((window.height - 1) * TILE_SIZE), UICommons.window_getRealWidth(window), TILE_SIZE)) {
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
                    }
                }

                // Execute Common Actions
                for (int ib = 0; ib < inputState.inputEvents.mouseDownButtons.size(); ib++) {
                    int mouseDownButton = inputState.inputEvents.mouseDownButtons.get(ib);
                    executeOnMouseDoubleClickCommonAction(inputState.lastGUIMouseHover, mouseDownButton);
                }
            } else {
                // Tool
                if (inputState.mouseTool != null && inputState.mouseTool.mouseToolAction != null) {
                    for (int ib = 0; ib < inputState.inputEvents.mouseDownButtons.size(); ib++) {
                        int mouseDownButton = inputState.inputEvents.mouseDownButtons.get(ib);
                        inputState.mouseTool.mouseToolAction.onDoubleClick(mouseDownButton, inputState.mouse.x, inputState.mouse.y);
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
                    } else if (inputState.lastGUIMouseHover instanceof Component component) {
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
                    } else if (inputState.lastGUIMouseHover instanceof Component component) {
                        if (component.addedToWindow != null && !component.addedToWindow.visible)
                            processMouseClick = false;
                    }
                }
            } else {
                processMouseClick = false;
            }

            if (processMouseClick) {
                Window moveWindow = null;
                boolean isMouseLeftButton = false;
                for (int ib = 0; ib < inputState.inputEvents.mouseDownButtons.size(); ib++) {
                    int mouseDownButton = inputState.inputEvents.mouseDownButtons.get(ib);
                    if (mouseDownButton == Input.Buttons.LEFT) {
                        isMouseLeftButton = true;
                        break;
                    }
                }

                if (isMouseLeftButton) {
                    // Mouse Action
                    if (inputState.lastGUIMouseHover.getClass() == Window.class) {
                        Window window = (Window) inputState.lastGUIMouseHover;
                        if (window.moveAble) moveWindow = window;
                    } else if (inputState.lastGUIMouseHover.getClass() == ContextMenuItem.class) {
                        ContextMenuItem contextMenuItem = (ContextMenuItem) inputState.lastGUIMouseHover;
                        ContextMenu contextMenu = contextMenuItem.addedToContextMenu;
                        if (contextMenuItem.contextMenuItemAction != null)
                            contextMenuItem.contextMenuItemAction.onSelect();
                        if (contextMenu.contextMenuAction != null)
                            contextMenu.contextMenuAction.onItemSelected(contextMenuItem);
                        UICommons.contextMenu_close(contextMenuItem.addedToContextMenu, inputState);
                    } else if (inputState.lastGUIMouseHover instanceof Button button) {
                        inputState.pressedButton = button;
                        if (button.mode == ButtonMode.TOGGLE) {
                            button.pressed = !button.pressed;
                        } else {
                            button.pressed = true;
                        }

                        if (button.buttonAction != null) {
                            button.buttonAction.onPress();
                            if (button.mode == ButtonMode.TOGGLE) button.buttonAction.onToggle(button.pressed);
                            inputState.pressedButton_timer_hold = 0;
                        }
                    } else if (inputState.lastGUIMouseHover.getClass() == ScrollBarVertical.class) {
                        ScrollBarVertical scrollBarVertical = (ScrollBarVertical) inputState.lastGUIMouseHover;
                        scrollBarVertical.buttonPressed = true;
                        if (scrollBarVertical.scrollBarAction != null)
                            scrollBarVertical.scrollBarAction.onPress(scrollBarVertical.scrolled);
                        inputState.scrolledScrollBarVertical = scrollBarVertical;
                    } else if (inputState.lastGUIMouseHover.getClass() == ScrollBarHorizontal.class) {
                        ScrollBarHorizontal scrollBarHorizontal = (ScrollBarHorizontal) inputState.lastGUIMouseHover;
                        scrollBarHorizontal.buttonPressed = true;
                        inputState.scrolledScrollBarHorizontal = scrollBarHorizontal;
                        if (scrollBarHorizontal.scrollBarAction != null)
                            scrollBarHorizontal.scrollBarAction.onPress(scrollBarHorizontal.scrolled);
                    } else if (inputState.lastGUIMouseHover.getClass() == List.class) {
                        List list = (List) inputState.lastGUIMouseHover;
                        UICommons.list_updateItemInfoAtMousePosition(inputState, list);
                        Object selectedListItem = null;
                        if (inputState.itemInfo_listValid) {
                            selectedListItem = (inputState.itemInfo_listIndex < list.items.size()) ? list.items.get(inputState.itemInfo_listIndex) : null;
                        }
                        if (selectedListItem != null) {
                            if (list.multiSelect) {
                                if (list.selectedItems.contains(selectedListItem)) {
                                    list.selectedItems.remove(selectedListItem);
                                } else {
                                    list.selectedItems.add(selectedListItem);
                                }
                                if (list.listAction != null) list.listAction.onItemsSelected(list.selectedItems);
                            } else {
                                list.selectedItem = selectedListItem;
                                if (list.listAction != null) list.listAction.onItemSelected(list.selectedItem);
                            }
                            if (list.dragEnabled) {
                                inputState.listDrag_from_index = inputState.itemInfo_listIndex;
                                inputState.listDrag_offset.x = inputState.mouse_gui.x - (UICommons.component_getAbsoluteX(list));
                                inputState.listDrag_offset.y = (inputState.mouse_gui.y - UICommons.component_getAbsoluteY(list)) % 8;
                                inputState.listDrag_Item = selectedListItem;
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
                    } else if (inputState.lastGUIMouseHover.getClass() == ComboBox.class) {
                        ComboBox combobox = (ComboBox) inputState.lastGUIMouseHover;

                        if (UICommons.comboBox_isOpen(combobox, inputState)) {
                            for (int i = 0; i < combobox.items.size(); i++) {
                                if (Tools.Calc.pointRectsCollide(inputState.mouse_gui.x, inputState.mouse_gui.y,
                                        UICommons.component_getParentWindowX(combobox) + (combobox.x * TILE_SIZE) + combobox.offset_x,
                                        UICommons.component_getParentWindowY(combobox) + (combobox.y * TILE_SIZE) + combobox.offset_y - (i * TILE_SIZE) - TILE_SIZE,
                                        combobox.width * TILE_SIZE,
                                        TILE_SIZE
                                )) {
                                    ComboBoxItem comboBoxItem = combobox.items.get(i);
                                    combobox.selectedItem = comboBoxItem;
                                    if (comboBoxItem.comboBoxItemAction != null)
                                        comboBoxItem.comboBoxItemAction.onSelect();
                                    if (combobox.comboBoxAction != null)
                                        combobox.comboBoxAction.onItemSelected(comboBoxItem);
                                    if (inputState.currentControlMode == MouseControlMode.KEYBOARD) {
                                        // keyboard mode: move mouse back to combobox on item select
                                        inputState.mouse_gui.y = UICommons.component_getAbsoluteY(combobox) + TILE_SIZE_2;
                                    }
                                }
                            }

                            UICommons.comboBox_close(combobox, inputState);
                        } else {
                            // Open this combobox
                            UICommons.comboBox_open(combobox, inputState);
                        }

                    } else if (inputState.lastGUIMouseHover.getClass() == Knob.class) {
                        Knob knob = (Knob) inputState.lastGUIMouseHover;

                        inputState.turnedKnob = knob;
                        if (knob.knobAction != null) knob.knobAction.onPress();

                    } else if (inputState.lastGUIMouseHover.getClass() == Map.class) {
                        Map map = (Map) inputState.lastGUIMouseHover;

                        int x = inputState.mouse_gui.x - (UICommons.component_getParentWindowX(map) + (map.x * TILE_SIZE) + map.offset_x);
                        int y = inputState.mouse_gui.y - (UICommons.component_getParentWindowY(map) + (map.y * TILE_SIZE) + map.offset_y);
                        if (map.mapAction != null) map.mapAction.onPress(x, y);
                        inputState.pressedMap = map;

                    } else if (inputState.lastGUIMouseHover.getClass() == GameViewPort.class) {
                        GameViewPort gameViewPort = (GameViewPort) inputState.lastGUIMouseHover;

                        int x = inputState.mouse_gui.x - (UICommons.component_getParentWindowX(gameViewPort) + (gameViewPort.x * TILE_SIZE) + gameViewPort.offset_x);
                        int y = inputState.mouse_gui.y - (UICommons.component_getParentWindowY(gameViewPort) + (gameViewPort.y * TILE_SIZE) + gameViewPort.offset_y);

                        if (gameViewPort.gameViewPortAction != null) {
                            gameViewPort.gameViewPortAction.onPress(x, y);
                        }
                        inputState.pressedGameViewPort = gameViewPort;

                    } else if (inputState.lastGUIMouseHover.getClass() == TextField.class) {
                        TextField textField = (TextField) inputState.lastGUIMouseHover;
                        // Set Marker to mouse position
                        int mouseX = inputState.mouse_gui.x - UICommons.component_getAbsoluteX(textField);
                        char[] fieldContent = textField.content.substring(textField.offset).toCharArray();
                        String testString = "";
                        boolean found = false;
                        charLoop:
                        for (int i = 0; i < fieldContent.length; i++) {
                            testString += fieldContent[i];
                            if (mediaManager.textWidth(textField.font, testString) > mouseX) {
                                UICommons.textField_setMarkerPosition(mediaManager, textField,
                                        textField.offset + i);
                                found = true;
                                break charLoop;
                            }
                        }
                        if (!found) {
                            // Set to end
                            UICommons.textField_setMarkerPosition(mediaManager, textField,
                                    textField.offset + fieldContent.length);
                        }
                        // Set Focus
                        UICommons.textField_focus(inputState, textField);
                    } else if (inputState.lastGUIMouseHover.getClass() == Inventory.class) {
                        Inventory inventory = (Inventory) inputState.lastGUIMouseHover;
                        int tileSize = inventory.doubleSized ? TILE_SIZE * 2 : TILE_SIZE;

                        int x_inventory = UICommons.component_getAbsoluteX(inventory);
                        int y_inventory = UICommons.component_getAbsoluteY(inventory);
                        int inv_x = (inputState.mouse_gui.x - x_inventory) / tileSize;
                        int inv_y = (inputState.mouse_gui.y - y_inventory) / tileSize;
                        if (UICommons.inventory_positionValid(inventory, inv_x, inv_y)) {
                            Object selectInvItem = inventory.items[inv_x][inv_y];
                            if (selectInvItem != null) {
                                inventory.selectedItem = selectInvItem;
                                inventory.inventoryAction.onItemSelected(selectInvItem, inv_x, inv_y);
                                if (inventory.dragEnabled) {
                                    inputState.inventoryDrag_from.x = inv_x;
                                    inputState.inventoryDrag_from.y = inv_y;
                                    inputState.inventoryDrag_offset.x = inputState.mouse_gui.x - (x_inventory + (inv_x * tileSize));
                                    inputState.inventoryDrag_offset.y = inputState.mouse_gui.y - (y_inventory + (inv_y * tileSize));
                                    inputState.inventoryDrag_Item = inventory.items[inv_x][inv_y];
                                    inputState.inventoryDrag_Inventory = inventory;
                                }
                            } else {
                                inventory.selectedItem = null;
                                inventory.inventoryAction.onItemSelected(null, inv_x, inv_y);
                            }
                        }

                    } else if (inputState.lastGUIMouseHover.getClass() == TabBar.class) {
                        TabBar tabBar = (TabBar) inputState.lastGUIMouseHover;
                        UICommons.tabBar_updateItemInfoAtMousePosition(inputState, tabBar);
                        if (inputState.itemInfo_tabBarValid && tabBar.selectedTab != inputState.itemInfo_tabBarTabIndex) {
                            Tab newTab = tabBar.tabs.get(inputState.itemInfo_tabBarTabIndex);
                            UICommons.tabBar_selectTab(tabBar, inputState.itemInfo_tabBarTabIndex);
                            if (newTab.tabAction != null) newTab.tabAction.onSelect();
                            if (tabBar.tabBarAction != null)
                                tabBar.tabBarAction.onChangeTab(inputState.itemInfo_tabBarTabIndex);
                        }

                    } else if (inputState.lastGUIMouseHover.getClass() == CheckBox.class) {
                        CheckBox checkBox = (CheckBox) inputState.lastGUIMouseHover;
                        checkBox.checked = !checkBox.checked;
                        if (checkBox.checkBoxAction != null) checkBox.checkBoxAction.onCheck(checkBox.checked);
                    }

                    // Additonal Actions
                    // -> Bring clicked window to top
                    if (moveWindow != null) {
                        inputState.draggedWindow = moveWindow;
                        inputState.draggedWindow_offset.x = inputState.mouse_gui.x - inputState.draggedWindow.x;
                        inputState.draggedWindow_offset.y = inputState.mouse_gui.y - inputState.draggedWindow.y;
                        // Move on top ?
                        UICommons.window_bringToFront(inputState, inputState.draggedWindow);
                    }
                    // Hide displayed context Menus
                    if (inputState.openContextMenu != null) {
                        UICommons.contextMenu_close(inputState.openContextMenu, inputState);
                    }
                    // Close opened Comboboxes
                    if (inputState.openComboBox != null &&
                            inputState.lastGUIMouseHover != inputState.openComboBox // dont close immediately on opening
                    ) {
                        UICommons.comboBox_close(inputState.openComboBox, inputState);
                    }
                    // Unfocus focused textfields
                    if (inputState.focusedTextField != null && inputState.lastGUIMouseHover != inputState.focusedTextField) {
                        UICommons.textField_unFocus(inputState, inputState.focusedTextField);
                    }
                }

                // Execute Common Actions
                for (int ib = 0; ib < inputState.inputEvents.mouseDownButtons.size(); ib++) {
                    int mouseDownButton = inputState.inputEvents.mouseDownButtons.get(ib);
                    executeOnMouseClickCommonAction(inputState.lastGUIMouseHover, mouseDownButton);
                }


            } else {
                // Tool
                if (inputState.mouseTool != null && inputState.mouseTool.mouseToolAction != null) {
                    for (int ib = 0; ib < inputState.inputEvents.mouseDownButtons.size(); ib++) {
                        int mouseDownButton = inputState.inputEvents.mouseDownButtons.get(ib);
                        inputState.mouseTool.mouseToolAction.onPress(mouseDownButton, inputState.mouse.x, inputState.mouse.y);
                        inputState.mouseToolPressed = true;
                    }
                }
            }
        }
        if (inputState.inputEvents.mouseUp) {
            if (inputState.draggedWindow != null) {
                inputState.draggedWindow = null;
                inputState.draggedWindow_offset.x = 0;
                inputState.draggedWindow_offset.y = 0;
            }
            if (inputState.pressedMap != null) {
                Map pressedMap = inputState.pressedMap;
                if (pressedMap.mapAction != null) pressedMap.mapAction.onRelease();
                inputState.pressedMap = null;
            }
            if (inputState.pressedGameViewPort != null) {
                GameViewPort pressedGameViewPort = inputState.pressedGameViewPort;
                if (pressedGameViewPort.gameViewPortAction != null)
                    pressedGameViewPort.gameViewPortAction.onRelease();
                inputState.pressedGameViewPort = null;
            }
            if (inputState.pressedButton != null) {
                Button pressedButton = inputState.pressedButton;
                if (pressedButton.mode != ButtonMode.TOGGLE) pressedButton.pressed = false;
                if (pressedButton.buttonAction != null) pressedButton.buttonAction.onRelease();
                inputState.pressedButton = null;
            }
            if (inputState.scrolledScrollBarVertical != null) {
                ScrollBarVertical scrolledBarVertical = inputState.scrolledScrollBarVertical;
                scrolledBarVertical.buttonPressed = false;
                if (scrolledBarVertical.scrollBarAction != null)
                    scrolledBarVertical.scrollBarAction.onRelease(scrolledBarVertical.scrolled);
                inputState.scrolledScrollBarVertical = null;
            }
            if (inputState.scrolledScrollBarHorizontal != null) {
                ScrollBarHorizontal scrolledBarHorizontal = inputState.scrolledScrollBarHorizontal;
                scrolledBarHorizontal.buttonPressed = false;
                if (scrolledBarHorizontal.scrollBarAction != null)
                    scrolledBarHorizontal.scrollBarAction.onRelease(scrolledBarHorizontal.scrolled);
                inputState.scrolledScrollBarHorizontal = null;
            }
            if (inputState.turnedKnob != null) {
                Knob turnedKnob = inputState.turnedKnob;
                if (turnedKnob.knobAction != null)
                    turnedKnob.knobAction.onRelease();
                inputState.turnedKnob = null;
            }

            if (inputState.inventoryDrag_Inventory != null) {
                Inventory dragInventory = inputState.inventoryDrag_Inventory;
                int dragFromX = inputState.inventoryDrag_from.x;
                int dragFromY = inputState.inventoryDrag_from.y;
                Object dragItem = inputState.inventoryDrag_Item;

                if (inputState.lastGUIMouseHover != null) {
                    if (inputState.lastGUIMouseHover.getClass() == Inventory.class) {
                        Inventory inventory = (Inventory) inputState.lastGUIMouseHover;
                        if (UICommons.inventory_canDragIntoInventory(inputState, inventory)) {
                            UICommons.inventory_updateItemInfoAtMousePosition(inputState, inventory);
                            if (inputState.itemInfo_inventoryValid) {
                                if (inventory.inventoryAction != null)
                                    inventory.inventoryAction.onDragFromInventory(dragInventory,
                                            dragFromX, dragFromY,
                                            inputState.itemInfo_inventoryPos.x, inputState.itemInfo_inventoryPos.y);
                            }
                        }
                    } else if (inputState.lastGUIMouseHover.getClass() == List.class) {
                        List list = (List) inputState.lastGUIMouseHover;
                        if (UICommons.list_canDragIntoList(inputState, list)) {
                            UICommons.list_updateItemInfoAtMousePosition(inputState, list);
                            if (inputState.itemInfo_listValid) {
                                int toIndex = inputState.itemInfo_listIndex;
                                if (list.listAction != null)
                                    list.listAction.onDragFromInventory(dragInventory, dragFromX, dragFromY, toIndex);
                            }
                        }
                    }
                } else if (UICommons.inventory_canDragIntoScreen(dragInventory)) {
                    if (dragInventory.inventoryAction != null) dragInventory.inventoryAction.onDragIntoScreen(
                            dragItem,
                            dragFromX, dragFromY,
                            api.input.state.mouseX(),
                            api.input.state.mouseY()
                    );
                }
                inputState.inventoryDrag_Inventory = null;
                inputState.inventoryDrag_offset.x = inputState.inventoryDrag_offset.y = 0;
                inputState.inventoryDrag_from.x = inputState.inventoryDrag_from.y = 0;
                inputState.inventoryDrag_Item = null;
            }
            if (inputState.listDrag_List != null) {
                List dragList = inputState.listDrag_List;
                int dragFromIndex = inputState.listDrag_from_index;
                Object dragItem = inputState.listDrag_Item;
                // Drag code
                if (inputState.lastGUIMouseHover != null) {
                    if (inputState.lastGUIMouseHover.getClass() == List.class) {
                        List list = (List) inputState.lastGUIMouseHover;
                        if (UICommons.list_canDragIntoList(inputState, list)) {
                            UICommons.list_updateItemInfoAtMousePosition(inputState, list);
                            if (inputState.itemInfo_listValid) {
                                int toIndex = inputState.itemInfo_listIndex;
                                if (list.listAction != null)
                                    list.listAction.onDragFromList(dragList, dragFromIndex, toIndex);
                            }
                        }
                    } else if (inputState.lastGUIMouseHover.getClass() == Inventory.class) {
                        Inventory inventory = (Inventory) inputState.lastGUIMouseHover;
                        if (UICommons.inventory_canDragIntoInventory(inputState, inventory)) {
                            UICommons.inventory_updateItemInfoAtMousePosition(inputState, inventory);
                            if (inputState.itemInfo_inventoryValid) {
                                if (inventory.inventoryAction != null)
                                    inventory.inventoryAction.onDragFromList(dragList, dragFromIndex,
                                            inputState.itemInfo_inventoryPos.x, inputState.itemInfo_inventoryPos.y);
                            }
                        }
                    }
                } else if (UICommons.list_canDragIntoScreen(dragList)) {
                    if (dragList.listAction != null) dragList.listAction.onDragIntoScreen(
                            dragItem,
                            dragFromIndex,
                            api.input.state.mouseX(),
                            api.input.state.mouseY()
                    );
                }
                inputState.listDrag_List = null;
                inputState.listDrag_offset.x = inputState.listDrag_offset.y = 0;
                inputState.listDrag_from_index = 0;
                inputState.listDrag_Item = null;
            }

            if (inputState.mouseToolPressed && inputState.mouseTool != null) {
                MouseTool pressedMouseTool = inputState.mouseTool;
                if (pressedMouseTool.mouseToolAction != null) {
                    for (int ib = 0; ib < inputState.inputEvents.mouseUpButtons.size(); ib++) {
                        int mouseUpButton = inputState.inputEvents.mouseUpButtons.get(ib);
                        pressedMouseTool.mouseToolAction.onRelease(mouseUpButton, inputState.mouse.x, inputState.mouse.y);
                    }
                }
                inputState.mouseToolPressed = false;
            }
        }
        if (inputState.inputEvents.mouseDragged) {
            if (inputState.draggedWindow != null) {
                Window draggedWindow = inputState.draggedWindow;
                draggedWindow.x = inputState.mouse_gui.x - inputState.draggedWindow_offset.x;
                draggedWindow.y = inputState.mouse_gui.y - inputState.draggedWindow_offset.y;
                if (draggedWindow.windowAction != null)
                    draggedWindow.windowAction.onMove(draggedWindow.x, draggedWindow.y);
            }
            if (inputState.scrolledScrollBarVertical != null) {
                ScrollBarVertical draggedBarVertical = inputState.scrolledScrollBarVertical;
                int mouseYrel = inputState.mouse_gui.y - UICommons.component_getParentWindowY(draggedBarVertical) - (draggedBarVertical.y * TILE_SIZE) - draggedBarVertical.offset_y;
                float newScrolled = (mouseYrel / ((float) (draggedBarVertical.height * TILE_SIZE)));
                draggedBarVertical.scrolled = Tools.Calc.inBounds(newScrolled, 0f, 1f);
                if (draggedBarVertical.scrollBarAction != null)
                    draggedBarVertical.scrollBarAction.onScrolled(draggedBarVertical.scrolled);
            }
            if (inputState.scrolledScrollBarHorizontal != null) {
                ScrollBarHorizontal draggedBarHorizontal = inputState.scrolledScrollBarHorizontal;
                int mouseXrel = inputState.mouse_gui.x - UICommons.component_getParentWindowX(draggedBarHorizontal) - (draggedBarHorizontal.x * TILE_SIZE) - draggedBarHorizontal.offset_x;
                float newScrolled = (mouseXrel / ((float) (draggedBarHorizontal.width * TILE_SIZE)));
                draggedBarHorizontal.scrolled = Tools.Calc.inBounds(newScrolled, 0f, 1f);
                if (draggedBarHorizontal.scrollBarAction != null)
                    draggedBarHorizontal.scrollBarAction.onScrolled(inputState.scrolledScrollBarHorizontal.scrolled);
            }
            if (inputState.turnedKnob != null) {
                Knob knob = inputState.turnedKnob;
                float amount = -(inputState.mouse_delta.y / 100f) * api.config.getKnobSensitivity();
                float newValue = knob.turned + amount;
                UICommons.knob_turnKnob(knob, newValue, amount);
                if (inputState.currentControlMode == MouseControlMode.KEYBOARD) {
                    // keyboard mode: keep mouse position steady
                    inputState.mouse_gui.y += inputState.mouse_delta.y;
                }
            }

            if (inputState.mouseToolPressed && inputState.mouseTool != null) {
                MouseTool draggedMouseTool = inputState.mouseTool;
                if (draggedMouseTool.mouseToolAction != null)
                    draggedMouseTool.mouseToolAction.onDrag(inputState.mouse.x, inputState.mouse.y);
            }

        }
        if (inputState.inputEvents.mouseMoved) {
            if (inputState.mouseTool != null) {
                MouseTool movedMouseTool = inputState.mouseTool;
                if (movedMouseTool.mouseToolAction != null)
                    movedMouseTool.mouseToolAction.onMove(inputState.mouse.x, inputState.mouse.y);
            }
        }
        if (inputState.inputEvents.mouseScrolled) {
            if (inputState.lastGUIMouseHover != null) {
                if (inputState.lastGUIMouseHover.getClass() == List.class) {
                    List list = (List) inputState.lastGUIMouseHover;
                    int size = list.items != null ? list.items.size() : 0;
                    float amount = (1 / (float) Tools.Calc.lowerBounds(size, 1)) * inputState.inputEvents.mouseScrolledAmount;
                    list.scrolled = Tools.Calc.inBounds(list.scrolled + amount, 0f, 1f);
                    if (list.listAction != null) {
                        list.listAction.onScrolled(list.scrolled);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == Knob.class) {
                    Knob knop = (Knob) inputState.lastGUIMouseHover;
                    float amount = ((-1 / 20f) * inputState.inputEvents.mouseScrolledAmount) * api.config.getKnobSensitivity();
                    float newValue = knop.turned + amount;
                    UICommons.knob_turnKnob(knop, newValue, amount);
                } else if (inputState.lastGUIMouseHover.getClass() == ScrollBarHorizontal.class) {
                    ScrollBarHorizontal scrollBarHorizontal = (ScrollBarHorizontal) inputState.lastGUIMouseHover;
                    float amount = ((-1 / 20f) * inputState.inputEvents.mouseScrolledAmount) * api.config.getScrollBarSensitivity();
                    scrollBarHorizontal.scrolled = Tools.Calc.inBounds(scrollBarHorizontal.scrolled + amount, 0f, 1f);
                    if (scrollBarHorizontal.scrollBarAction != null) {
                        scrollBarHorizontal.scrollBarAction.onScrolled(scrollBarHorizontal.scrolled);
                    }
                } else if (inputState.lastGUIMouseHover.getClass() == ScrollBarVertical.class) {
                    ScrollBarVertical scrollBarVertical = (ScrollBarVertical) inputState.lastGUIMouseHover;
                    float amount = ((-1 / 20f) * inputState.inputEvents.mouseScrolledAmount) * api.config.getScrollBarSensitivity();
                    scrollBarVertical.scrolled = Tools.Calc.inBounds(scrollBarVertical.scrolled + amount, 0f, 1f);
                    if (scrollBarVertical.scrollBarAction != null) {
                        scrollBarVertical.scrollBarAction.onScrolled(scrollBarVertical.scrolled);
                    }
                }

                // Execute Common Actions
                executeOnMouseScrollCommonAction(inputState.lastGUIMouseHover, inputState.inputEvents.mouseScrolledAmount);

            }

        }
    }

    private void updateButtonHoldActions() {
        /* Button Hold Interactions */
        if (inputState.pressedButton != null) {
            if (inputState.pressedButton.mode == ButtonMode.HOLD) {
                inputState.pressedButton_timer_hold = inputState.pressedButton_timer_hold + 1;
                if (inputState.pressedButton_timer_hold > api.config.getButtonHoldTimer()) {
                    if (inputState.pressedButton.buttonAction != null)
                        inputState.pressedButton.buttonAction.onHold();
                    inputState.pressedButton_timer_hold = 0;
                }
            }
        }
    }

    private void updateGUI() {

        updateMouseInteractions();

        updateKeyInteractions();

        updateButtonHoldActions();

        updateUpdateActions();

        updateEnforceWindowScreenBounds();

        updateNotifications();

        updateToolTip();

    }

    private void updateUpdateActions() {
        // for(int i) is used to avoid iterator creation and avoid concurrentModification
        // If UpdateActions are removing/adding other update actions they are caught on the next update/frame

        // ScreenComponent UpdateActions
        long currentTimeMillis = System.currentTimeMillis();
        for (int i = 0; i < inputState.screenComponents.size(); i++) {
            Component component = inputState.screenComponents.get(i);
            for (int i2 = 0; i2 < component.updateActions.size(); i2++) {
                executeUpdateAction(component.updateActions.get(i2), currentTimeMillis);
            }
        }
        for (int i = 0; i < inputState.windows.size(); i++) {
            // Window UpdateActions
            Window window = inputState.windows.get(i);
            for (int i2 = 0; i2 < window.updateActions.size(); i2++) {
                executeUpdateAction(window.updateActions.get(i2), currentTimeMillis);
            }
            // Window Component UpdateActions
            for (int i2 = 0; i2 < window.components.size(); i2++) {
                Component component = window.components.get(i2);
                for (int i3 = 0; i3 < component.updateActions.size(); i3++) {
                    executeUpdateAction(component.updateActions.get(i3), currentTimeMillis);
                }
            }
        }

        // Engine SingleUpdateActions
        for (int i = 0; i < inputState.singleUpdateActions.size(); i++) {
            UpdateAction updateAction = inputState.singleUpdateActions.get(i);
            if (this.executeUpdateAction(updateAction, currentTimeMillis)) {
                inputState.singleUpdateActionsRemoveQ.push(updateAction);
            }
        }
        UpdateAction removeUpdateAction;
        while ((removeUpdateAction = inputState.singleUpdateActionsRemoveQ.pollFirst()) != null) {
            inputState.singleUpdateActions.remove(removeUpdateAction);
        }
    }

    private void updateEnforceWindowScreenBounds() {
        for (int i = 0; i < inputState.windows.size(); i++) {
            Window window = inputState.windows.get(i);
            if (window.enforceScreenBounds) UICommons.window_enforceScreenBounds(inputState, window);
        }
    }

    private void updateToolTip() {
        // Anything dragged ?
        boolean showComponentToolTip = inputState.listDrag_List == null && inputState.inventoryDrag_Inventory == null;

        // hovering over a component ?
        if (showComponentToolTip) {
            showComponentToolTip = (inputState.lastGUIMouseHover instanceof Component);
        }
        // modal active and component does not belong to modal ?
        if (showComponentToolTip) {
            showComponentToolTip = inputState.modalWindow == null || ((Component) inputState.lastGUIMouseHover).addedToWindow == inputState.modalWindow;
        }

        if (showComponentToolTip) {
            Component hoverComponent = (Component) inputState.lastGUIMouseHover;
            Object toolTipSubItem = null;
            if (hoverComponent.getClass() == List.class) {
                List list = (List) inputState.lastGUIMouseHover;
                if (list.listAction != null) {
                    UICommons.list_updateItemInfoAtMousePosition(inputState, list);
                    if (inputState.itemInfo_listValid) {
                        toolTipSubItem = inputState.itemInfo_listIndex < list.items.size() ? list.items.get(inputState.itemInfo_listIndex) : null;
                    }
                }
            } else if (hoverComponent.getClass() == Inventory.class) {
                Inventory inventory = (Inventory) hoverComponent;
                int tileSize = inventory.doubleSized ? TILE_SIZE * 2 : TILE_SIZE;
                if (inventory.inventoryAction != null) {
                    int x_inventory = UICommons.component_getAbsoluteX(inventory);
                    int y_inventory = UICommons.component_getAbsoluteY(inventory);
                    int inv_x = (inputState.mouse_gui.x - x_inventory) / tileSize;
                    int inv_y = (inputState.mouse_gui.y - y_inventory) / tileSize;
                    if (UICommons.inventory_positionValid(inventory, inv_x, inv_y)) {
                        toolTipSubItem = inventory.items[inv_x][inv_y];
                    }
                }
            }


            boolean updateComponentToolTip;
            if (hoverComponent.updateToolTip) {
                updateComponentToolTip = true;
                hoverComponent.updateToolTip = false;
            } else {
                if (hoverComponent.getClass() == List.class || hoverComponent.getClass() == Inventory.class) {
                    // Check on subitem change
                    updateComponentToolTip = inputState.tooltip_lastHoverObject != toolTipSubItem;
                } else {
                    // Check on component change
                    updateComponentToolTip = inputState.tooltip_lastHoverObject != hoverComponent;
                }
            }

            if (updateComponentToolTip) {
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
            // Set Game Tooltip
            if (inputState.lastGUIMouseHover == null && inputState.gameToolTip != null) {
                if (inputState.tooltip != inputState.gameToolTip) {
                    inputState.tooltip = inputState.gameToolTip;
                    inputState.tooltip_wait_delay = true;
                    inputState.tooltip_delay_timer = System.currentTimeMillis();
                }
            } else {
                inputState.tooltip = null;
                inputState.tooltip_lastHoverObject = null;
            }
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


    private void updateMouseControl() {

        updateMouseControlMode();

        if (inputState.currentControlMode != MouseControlMode.DISABLED) {
            // Translate Keys
            switch (inputState.currentControlMode) {
                case HARDWARE_MOUSE -> {
                }
                case GAMEPAD -> gamePadMouseTranslateAndChokeEvents();
                case KEYBOARD -> keyboardMouseTranslateAndChokeEvents();
                case DISABLED -> throw new RuntimeException(); // invalid state for this function
            }

            // Update OnScreenTextinput or Mouse Cursor
            if (inputState.openMouseTextInput != null) {
                // Translate to Text Input
                updateMouseTextInputControl();
            } else {
                // Translate to MouseGUI position
                switch (inputState.currentControlMode) {
                    case HARDWARE_MOUSE -> updateHardwareMouseControl();
                    case KEYBOARD -> updateKeyBoardMouseControl();
                    case GAMEPAD -> updateGamePadMouseControl();
                    case DISABLED -> throw new RuntimeException(); // invalid state for this function
                }
            }
        } else {
            chockeAllMouseEvents();
        }

        // Translate MouseXGUI/MouseYGUI to Game X/Y
        updateGUIMouseBounds();
        updateGameMouseXY();
    }


    private void updateMouseTextInputControl() {
        if (inputState.openMouseTextInput == null) return;
        OnScreenTextInput onScreenTextInput = inputState.openMouseTextInput;
        char[] characters = onScreenTextInput.upperCase ? onScreenTextInput.charactersUC : onScreenTextInput.charactersLC;

        int scrollDirection = 0;
        boolean confirmPressed = false;
        boolean changeCasePressed = false;
        boolean deletePressed = false;
        boolean leftKeyBoardGamePad = false;
        boolean rightKeyBoardGamePad = false;
        switch (inputState.currentControlMode) {
            case HARDWARE_MOUSE -> {
                int deltaX = Gdx.input.getX() - inputState.mTextInputMouseX;
                if (deltaX > 6) {
                    scrollDirection = 1;
                    inputState.mTextInputMouseX = Gdx.input.getX();
                } else if (deltaX < -6) {
                    scrollDirection = -1;
                    inputState.mTextInputMouseX = Gdx.input.getX();
                }
                if (inputState.inputEvents.mouseDown) {
                    int indexOfLeft = inputState.inputEvents.mouseDownButtons.indexOf(KeyCode.Mouse.LEFT);
                    int indexOfRight = inputState.inputEvents.mouseDownButtons.indexOf(KeyCode.Mouse.RIGHT);
                    if (indexOfLeft != -1) {
                        // Choke Events
                        inputState.inputEvents.mouseButtonsDown[KeyCode.Mouse.LEFT] = false;
                        inputState.inputEvents.mouseDownButtons.remove(indexOfLeft);
                        // Translate
                        inputState.mTextInputTranslatedMouse1Down = true;
                    }
                    if (indexOfRight != -1) {
                        // Choke Events
                        inputState.inputEvents.mouseButtonsDown[KeyCode.Mouse.RIGHT] = false;
                        inputState.inputEvents.mouseDownButtons.remove(indexOfRight);
                        // Translate
                        inputState.mTextInputTranslatedMouse2Down = true;
                    }
                    inputState.inputEvents.mouseDown = inputState.inputEvents.mouseDownButtons.size() > 0;
                }
                if (inputState.inputEvents.mouseUp) {
                    int indexOfLeft = inputState.inputEvents.mouseUpButtons.indexOf(KeyCode.Mouse.LEFT);
                    int indexOfRight = inputState.inputEvents.mouseUpButtons.indexOf(KeyCode.Mouse.RIGHT);
                    if (indexOfLeft != -1) {
                        // Choke Events
                        inputState.inputEvents.mouseUpButtons.remove(indexOfLeft);
                        // Translate
                        inputState.mTextInputTranslatedMouse1Down = false;
                    }
                    if (indexOfRight != -1) {
                        // Choke Events
                        inputState.inputEvents.mouseUpButtons.remove(indexOfRight);
                        inputState.mTextInputTranslatedMouse2Down = false;
                    }
                    inputState.inputEvents.mouseUp = inputState.inputEvents.mouseUpButtons.size() > 0;
                }
                confirmPressed = inputState.mTextInputTranslatedMouse1Down;
                deletePressed = inputState.mTextInputTranslatedMouse2Down;
                // Change Case
                if (inputState.inputEvents.mouseScrolled) {
                    if (inputState.inputEvents.mouseScrolledAmount > 0 && onScreenTextInput.upperCase)
                        changeCasePressed = true;
                    if (inputState.inputEvents.mouseScrolledAmount < 0 && !onScreenTextInput.upperCase)
                        changeCasePressed = true;
                    inputState.inputEvents.mouseScrolled = false;
                    inputState.inputEvents.mouseScrolledAmount = 0f;
                }

            }
            case KEYBOARD -> {
                leftKeyBoardGamePad = isTranslatedKeyCodeDown(inputState.keyBoardTranslatedKeysDown, api.config.getKeyboardMouseButtonsLeft());
                rightKeyBoardGamePad = isTranslatedKeyCodeDown(inputState.keyBoardTranslatedKeysDown, api.config.getKeyboardMouseButtonsRight());
                confirmPressed = isTranslatedKeyCodeDown(inputState.keyBoardTranslatedKeysDown, api.config.getKeyboardMouseButtonsMouse1());
                deletePressed = isTranslatedKeyCodeDown(inputState.keyBoardTranslatedKeysDown, api.config.getKeyboardMouseButtonsMouse2());
                if (isTranslatedKeyCodeDown(inputState.keyBoardTranslatedKeysDown, api.config.getKeyboardMouseButtonsScrollUp())) {
                    changeCasePressed = !onScreenTextInput.upperCase;
                } else if (isTranslatedKeyCodeDown(inputState.keyBoardTranslatedKeysDown, api.config.getKeyboardMouseButtonsScrollDown())) {
                    changeCasePressed = onScreenTextInput.upperCase;
                }
            }
            case GAMEPAD -> {
                boolean stickLeft = api.config.isGamePadMouseStickLeftEnabled();
                boolean stickRight = api.config.isGamePadMouseStickRightEnabled();
                final float sensitivity = 0.4f;
                leftKeyBoardGamePad = (stickLeft && inputState.gamePadTranslatedStickLeft.x < -sensitivity) || (stickRight && inputState.gamePadTranslatedStickRight.x < -sensitivity);
                rightKeyBoardGamePad = (stickLeft && inputState.gamePadTranslatedStickLeft.x > sensitivity) || (stickRight && inputState.gamePadTranslatedStickRight.x > sensitivity);
                confirmPressed = isTranslatedKeyCodeDown(inputState.gamePadTranslatedButtonsDown, api.config.getGamePadMouseButtonsMouse1());
                deletePressed = isTranslatedKeyCodeDown(inputState.gamePadTranslatedButtonsDown, api.config.getGamePadMouseButtonsMouse2());
                if (isTranslatedKeyCodeDown(inputState.gamePadTranslatedButtonsDown, api.config.getGamePadMouseButtonsScrollUp())) {
                    changeCasePressed = !onScreenTextInput.upperCase;
                } else if (isTranslatedKeyCodeDown(inputState.gamePadTranslatedButtonsDown, api.config.getGamePadMouseButtonsScrollDown())) {
                    changeCasePressed = onScreenTextInput.upperCase;
                }
            }
        }

        if (inputState.currentControlMode == MouseControlMode.GAMEPAD || inputState.currentControlMode == MouseControlMode.KEYBOARD) {
            if (leftKeyBoardGamePad) {
                if (inputState.mTextInputKeyBoardGamePadLeft == false) {
                    scrollDirection = -1;
                    inputState.mTextInputKeyBoardGamePadLeft = true;
                }
            } else {
                inputState.mTextInputKeyBoardGamePadLeft = false;
            }
            if (rightKeyBoardGamePad) {
                if (inputState.mTextInputKeyBoardGamePadRight == false) {
                    scrollDirection = 1;
                    inputState.mTextInputKeyBoardGamePadRight = true;
                }
            } else {
                inputState.mTextInputKeyBoardGamePadRight = false;
            }

            // Continue Scroll
            if (leftKeyBoardGamePad || rightKeyBoardGamePad) {
                inputState.mTextInputScrollTimer++;
                if (inputState.mTextInputScrollTimer > inputState.mTextInputScrollTime) {
                    inputState.mTextInputKeyBoardGamePadLeft = false;
                    inputState.mTextInputKeyBoardGamePadRight = false;
                    inputState.mTextInputScrollTimer = 0;
                    inputState.mTextInputScrollSpeed++;
                    if (inputState.mTextInputScrollSpeed >= 3) {
                        inputState.mTextInputScrollTime = 2;
                    } else if (inputState.mTextInputScrollSpeed >= 2) {
                        inputState.mTextInputScrollTime = 5;
                    } else if (inputState.mTextInputScrollSpeed >= 1) {
                        inputState.mTextInputScrollTime = 10;
                    }
                }
            } else {
                inputState.mTextInputScrollTimer = 0;
                inputState.mTextInputScrollTime = 20;
                inputState.mTextInputScrollSpeed = 0;
            }
        }

        // Unlock on first press
        if (!inputState.mTextInputUnlock) {
            if (confirmPressed) {
                confirmPressed = false;
            } else {
                inputState.mTextInputUnlock = true;
            }
        }

        // Confirm Character / Change Case
        boolean confirmCharacter = false;
        boolean changeCase = false;
        boolean deleteCharacter = false;
        if (confirmPressed && !inputState.mTextInputConfirmPressed) inputState.mTextInputConfirmPressed = true;
        if (!confirmPressed && inputState.mTextInputConfirmPressed) {
            confirmCharacter = true;
            inputState.mTextInputConfirmPressed = false;
        }

        if (changeCasePressed && !inputState.mTextInputChangeCasePressed) inputState.mTextInputChangeCasePressed = true;
        if (!changeCasePressed && inputState.mTextInputChangeCasePressed) {
            confirmCharacter = true;
            changeCase = true;
            inputState.mTextInputChangeCasePressed = false;
        }

        if (deletePressed && !inputState.mTextInputDeletePressed) inputState.mTextInputDeletePressed = true;
        if (!deletePressed && inputState.mTextInputDeletePressed) {
            confirmCharacter = true;
            deleteCharacter = true;
            inputState.mTextInputDeletePressed = false;
        }

        // Set Index
        if (inputState.inputEvents.keyTyped) {
            char typedChar = inputState.inputEvents.keyTypedCharacters.get(inputState.inputEvents.keyTypedCharacters.size() - 1);
            findCharLoop:
            for (int i = 0; i < onScreenTextInput.charactersLC.length; i++) {
                if (onScreenTextInput.charactersLC[i] == typedChar) {
                    onScreenTextInput.selectedIndex = i;
                    onScreenTextInput.upperCase = false;
                    break findCharLoop;
                } else if (onScreenTextInput.charactersUC[i] == typedChar) {
                    onScreenTextInput.selectedIndex = i;
                    onScreenTextInput.upperCase = true;
                    break findCharLoop;
                }

            }
        } else if (scrollDirection != 0) {
            onScreenTextInput.selectedIndex = Tools.Calc.inBounds(onScreenTextInput.selectedIndex + scrollDirection, 0, (characters.length - 1));
        }

        if (confirmCharacter) {
            char c;
            if (changeCase) {
                c = '\t';
            } else if (deleteCharacter) {
                c = '\b';
            } else {
                c = characters[onScreenTextInput.selectedIndex];
            }
            switch (c) {
                case '\b' -> {
                    inputState.inputEvents.keyTyped = true;
                    inputState.inputEvents.keyTypedCharacters.add('\b');
                    if (onScreenTextInput.mouseTextInputAction != null)
                        onScreenTextInput.mouseTextInputAction.onDelete();
                }
                case '\t' -> {
                    onScreenTextInput.upperCase = !onScreenTextInput.upperCase;
                    if (onScreenTextInput.mouseTextInputAction != null)
                        onScreenTextInput.mouseTextInputAction.onChangeCase(onScreenTextInput.upperCase);
                }
                case '\n' -> {
                    boolean close = onScreenTextInput.mouseTextInputAction != null ? onScreenTextInput.mouseTextInputAction.onConfirm() : true;
                    inputState.openMouseTextInput = close ? null : inputState.openMouseTextInput;
                }
                default -> {
                    inputState.inputEvents.keyTyped = true;
                    inputState.inputEvents.keyTypedCharacters.add(c);
                    if (onScreenTextInput.mouseTextInputAction != null)
                        onScreenTextInput.mouseTextInputAction.onEnterCharacter(c);
                }
            }

        }


        return;
    }

    private void chockeAllMouseEvents() {
        // clear all mouse inputs
        inputState.inputEvents.mouseMoved = false;
        inputState.inputEvents.mouseDragged = false;
        inputState.inputEvents.mouseUp = false;
        inputState.inputEvents.mouseUpButtons.clear();
        inputState.inputEvents.mouseDown = false;
        inputState.inputEvents.mouseDownButtons.clear();
        inputState.inputEvents.mouseDoubleClick = false;
        for (int i = 0; i < inputState.inputEvents.mouseButtonsDown.length; i++) {
            inputState.inputEvents.mouseButtonsDown[i] = false;
        }

        inputState.mouse_gui.x = 0;
        inputState.mouse_gui.y = 0;
        inputState.mouse_delta.x = 0;
        inputState.mouse_delta.y = 0;

    }

    private void updateMouseControlMode() {
        boolean hardwareMouse = api.config.isHardwareMouseEnabled();
        boolean keyboardMouse = api.config.isKeyboardMouseEnabled();
        boolean gamePadMouse = api.config.isGamePadMouseEnabled();

        MouseControlMode nextControlMode = null;

        if (!hardwareMouse && !keyboardMouse && !gamePadMouse) {
            nextControlMode = MouseControlMode.DISABLED;
        } else {
            if (hardwareMouse && !keyboardMouse && !gamePadMouse) {
                nextControlMode = MouseControlMode.HARDWARE_MOUSE;
            } else if (keyboardMouse && !gamePadMouse && !hardwareMouse) {
                nextControlMode = MouseControlMode.KEYBOARD;
            } else if (gamePadMouse && !hardwareMouse && !keyboardMouse) {
                nextControlMode = MouseControlMode.GAMEPAD;
            } else {
                switch (inputState.currentControlMode) {
                    case HARDWARE_MOUSE -> {
                        if (keyboardMouse && keyboardMouseDetectUse()) nextControlMode = MouseControlMode.KEYBOARD;
                        if (gamePadMouse && gamePadMouseDetectUse()) nextControlMode = MouseControlMode.GAMEPAD;
                    }
                    case KEYBOARD -> {
                        if (hardwareMouse && hardwareMouseDetectUse())
                            nextControlMode = MouseControlMode.HARDWARE_MOUSE;
                        if (gamePadMouse && gamePadMouseDetectUse()) nextControlMode = MouseControlMode.GAMEPAD;
                    }
                    case GAMEPAD -> {
                        if (hardwareMouse && hardwareMouseDetectUse())
                            nextControlMode = MouseControlMode.HARDWARE_MOUSE;
                        if (keyboardMouse && keyboardMouseDetectUse()) nextControlMode = MouseControlMode.KEYBOARD;
                    }
                }
            }
        }


        if (nextControlMode != null && nextControlMode != inputState.currentControlMode) {
            // Clean up current control mode
            switch (inputState.currentControlMode) {
                case HARDWARE_MOUSE -> {
                    // Save last position, for reuse detection
                    inputState.hardwareMouseLastPosition.x = Gdx.input.getX();
                    inputState.hardwareMouseLastPosition.y = Gdx.input.getY();
                }
                case GAMEPAD, KEYBOARD -> {
                    // Reset temporary variables
                    if (inputState.currentControlMode == MouseControlMode.GAMEPAD) {
                        for (int i = 0; i < inputState.keyBoardTranslatedKeysDown.length; i++)
                            inputState.keyBoardTranslatedKeysDown[i] = false;
                    } else if (inputState.currentControlMode == MouseControlMode.KEYBOARD) {
                        for (int i = 0; i < inputState.gamePadTranslatedButtonsDown.length; i++)
                            inputState.gamePadTranslatedButtonsDown[i] = false;
                        inputState.keyBoardMouseSpeedUp.set(0f,0f);
                    }
                    for (int i = 0; i <= 4; i++) inputState.simulatedMouseIsButtonDown[i] = false;
                    inputState.simulatedMouseLastMouseClick = 0;
                }
            }
            // Set Next ControlMode
            switch (nextControlMode) {
                case GAMEPAD, KEYBOARD -> {
                    // Set simulated mouse position to current
                    this.inputState.simulatedMouseGUIPosition.set(inputState.mouse_gui.x, inputState.mouse_gui.y);
                }
            }
            inputState.currentControlMode = nextControlMode;
        }
    }

    private boolean hardwareMouseDetectUse() {
        return (Gdx.input.getX() != inputState.hardwareMouseLastPosition.x || Gdx.input.getY() != inputState.hardwareMouseLastPosition.y);
    }

    private boolean keyboardMouseDetectUse() {
        for (int i = 0; i <= 10; i++) {
            int[] buttons = keyboardMouseButtons(i);
            if (buttons != null) {
                for (int i2 = 0; i2 < buttons.length; i2++) {
                    if (inputState.inputEvents.keysDown[buttons[i2]]) return true;
                }
            }
        }
        return false;
    }


    private boolean gamePadMouseDetectUse() {
        if (api.config.isGamePadMouseStickLeftEnabled()) {
            if (inputState.inputEvents.gamePadLeftXMoved || inputState.inputEvents.gamePadLeftYMoved) {
                return inputState.inputEvents.gamePadLeftX < -api.config.getGamePadMouseJoystickDeadZone() ||
                        inputState.inputEvents.gamePadLeftX > api.config.getGamePadMouseJoystickDeadZone();
            }
        }
        if (api.config.isGamePadMouseStickRightEnabled()) {
            if (inputState.inputEvents.gamePadRightXMoved || inputState.inputEvents.gamePadRightYMoved) {
                return inputState.inputEvents.gamePadRightX < -api.config.getGamePadMouseJoystickDeadZone() ||
                        inputState.inputEvents.gamePadRightX > api.config.getGamePadMouseJoystickDeadZone();
            }
        }

        for (int i = 0; i <= 6; i++) {
            int[] buttons = gamePadMouseButtons(i);
            if (buttons != null) {
                for (int i2 = 0; i2 < buttons.length; i2++) {
                    if (inputState.inputEvents.gamePadButtonsDown[buttons[i2]]) return true;
                }
            }
        }
        return false;
    }

    private int[] keyboardMouseButtons(int index) {
        return switch (index) {
            case 0 -> api.config.getKeyboardMouseButtonsUp();
            case 1 -> api.config.getKeyBoardControlButtonsDown();
            case 2 -> api.config.getKeyboardMouseButtonsLeft();
            case 3 -> api.config.getKeyboardMouseButtonsRight();
            case 4 -> api.config.getKeyboardMouseButtonsMouse1();
            case 5 -> api.config.getKeyboardMouseButtonsMouse2();
            case 6 -> api.config.getKeyboardMouseButtonsMouse3();
            case 7 -> api.config.getKeyboardMouseButtonsMouse4();
            case 8 -> api.config.getKeyboardMouseButtonsMouse5();
            case 9 -> api.config.getKeyboardMouseButtonsScrollUp();
            case 10 -> api.config.getKeyboardMouseButtonsScrollDown();
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }


    private int[] gamePadMouseButtons(int index) {
        return switch (index) {
            case 0 -> api.config.getGamePadMouseButtonsMouse1();
            case 1 -> api.config.getGamePadMouseButtonsMouse2();
            case 2 -> api.config.getGamePadMouseButtonsMouse3();
            case 3 -> api.config.getGamePadMouseButtonsMouse4();
            case 4 -> api.config.getGamePadMouseButtonsMouse5();
            case 5 -> api.config.getGamePadMouseButtonsScrollUp();
            case 6 -> api.config.getGamePadMouseButtonsScrollDown();
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }

    private void gamePadMouseTranslateAndChokeEvents() {
        // Remove Key down input events and set to temporary variable keyBoardTranslatedKeysDown
        for (int i = 0; i <= 6; i++) {
            int[] buttons = gamePadMouseButtons(i);
            if (buttons != null) {
                for (int i2 = 0; i2 < buttons.length; i2++) {
                    int keyCode = buttons[i2];
                    if (inputState.inputEvents.gamePadButtonDown) {
                        ArrayList<Integer> downKeyCodes = inputState.inputEvents.gamePadButtonDownKeyCodes;
                        for (int ikc = downKeyCodes.size() - 1; ikc >= 0; ikc--) {
                            if (downKeyCodes.get(ikc) == keyCode) {
                                downKeyCodes.remove(ikc);
                                inputState.inputEvents.gamePadButtonDown = !downKeyCodes.isEmpty();
                                inputState.inputEvents.gamePadButtonsDown[keyCode] = false;
                                inputState.gamePadTranslatedButtonsDown[keyCode] = true;
                            }
                        }
                    }
                    if (inputState.inputEvents.gamePadButtonUp) {
                        ArrayList<Integer> upKeyCodes = inputState.inputEvents.gamePadButtonUpKeyCodes;
                        for (int ikc = upKeyCodes.size() - 1; ikc >= 0; ikc--) {
                            if (upKeyCodes.get(ikc) == keyCode) {
                                upKeyCodes.remove(ikc);
                                inputState.inputEvents.gamePadButtonUp = !upKeyCodes.isEmpty();
                                inputState.gamePadTranslatedButtonsDown[keyCode] = false;
                            }
                        }
                    }
                }
            }
        }
        // Joystick Events Left
        if (api.config.isGamePadMouseStickLeftEnabled()) {
            if (inputState.inputEvents.gamePadLeftXMoved) {
                inputState.gamePadTranslatedStickLeft.x = inputState.inputEvents.gamePadLeftX;
                inputState.inputEvents.gamePadLeftX = 0;
                inputState.inputEvents.gamePadLeftXMoved = false;
            }
            if (inputState.inputEvents.gamePadLeftYMoved) {
                inputState.gamePadTranslatedStickLeft.y = inputState.inputEvents.gamePadLeftY;
                inputState.inputEvents.gamePadLeftY = 0;
                inputState.inputEvents.gamePadLeftYMoved = false;
            }
        } else {
            inputState.gamePadTranslatedStickLeft.x = 0;
            inputState.gamePadTranslatedStickLeft.y = 0;
        }
        // Joystick Events Right
        if (api.config.isGamePadMouseStickRightEnabled()) {
            if (inputState.inputEvents.gamePadRightXMoved) {
                inputState.gamePadTranslatedStickRight.x = inputState.inputEvents.gamePadRightX;
                inputState.inputEvents.gamePadRightX = 0;
                inputState.inputEvents.gamePadRightXMoved = false;
            }
            if (inputState.inputEvents.gamePadRightYMoved) {
                inputState.gamePadTranslatedStickRight.y = inputState.inputEvents.gamePadRightY;
                inputState.inputEvents.gamePadRightY = 0;
                inputState.inputEvents.gamePadRightYMoved = false;
            }
        } else {
            inputState.gamePadTranslatedStickRight.x = 0;
            inputState.gamePadTranslatedStickRight.y = 0;
        }


    }

    private void keyboardMouseTranslateAndChokeEvents() {
        // Remove Key down input events and set to temporary variable keyBoardTranslatedKeysDown
        for (int i = 0; i <= 10; i++) {
            int[] buttons = keyboardMouseButtons(i);
            if (buttons != null) {
                for (int i2 = 0; i2 < buttons.length; i2++) {
                    int keyCode = buttons[i2];
                    if (inputState.inputEvents.keyDown) {
                        ArrayList<Integer> downKeyCodes = inputState.inputEvents.keyDownKeyCodes;
                        for (int ikc = downKeyCodes.size() - 1; ikc >= 0; ikc--) {
                            if (downKeyCodes.get(ikc) == keyCode) {
                                downKeyCodes.remove(ikc);
                                inputState.inputEvents.keyDown = !downKeyCodes.isEmpty();
                                inputState.inputEvents.keysDown[keyCode] = false;
                                inputState.keyBoardTranslatedKeysDown[keyCode] = true;
                            }
                        }
                    }
                    if (inputState.inputEvents.keyUp) {
                        ArrayList<Integer> upKeyCodes = inputState.inputEvents.keyUpKeyCodes;
                        for (int ikc = upKeyCodes.size() - 1; ikc >= 0; ikc--) {
                            if (upKeyCodes.get(ikc) == keyCode) {
                                upKeyCodes.remove(ikc);
                                inputState.inputEvents.keyUp = !upKeyCodes.isEmpty();
                                inputState.keyBoardTranslatedKeysDown[keyCode] = false;
                            }
                        }
                    }
                }
            }
        }
    }



    private void translateSimulatedMouseEvents(boolean buttonLeft, boolean buttonRight, boolean buttonUp, boolean buttonDown,
                                               boolean buttonMouse1Down, boolean buttonMouse2Down, boolean buttonMouse3Down, boolean buttonMouse4Down, boolean buttonMouse5Down,
                                               boolean buttonScrolledUp, boolean buttonScrolledDown, float cursorSpeedX, float cursorSpeedY
    ) {
        float deltaX = 0;
        float deltaY = 0;
        if (buttonLeft || buttonRight || buttonUp || buttonDown) {
            float moveSpeedX = api.config.getSimulatedMouseCursorSpeed() * cursorSpeedX;
            if (buttonLeft) deltaX -= moveSpeedX;
            if (buttonRight) deltaX += moveSpeedX;
            float moveSpeedY = api.config.getSimulatedMouseCursorSpeed() * cursorSpeedY;
            if (buttonUp) deltaY -= moveSpeedY;
            if (buttonDown) deltaY += moveSpeedY;
        } else {
            // Magnet Mode (Mouse sticks to UI elements)
        }

        // Set to final
        inputState.simulatedMouseGUIPosition.x = Tools.Calc.inBounds(inputState.simulatedMouseGUIPosition.x + deltaX, 0, inputState.internalResolutionWidth);
        inputState.simulatedMouseGUIPosition.y = Tools.Calc.inBounds(inputState.simulatedMouseGUIPosition.y - deltaY, 0, inputState.internalResolutionHeight);
        int newCursorPositionX = MathUtils.round(inputState.simulatedMouseGUIPosition.x);
        int newCursorPositionY = MathUtils.round(inputState.simulatedMouseGUIPosition.y);
        inputState.mouse_delta.x = newCursorPositionX - inputState.mouse_gui.x;
        inputState.mouse_delta.y = newCursorPositionY - inputState.mouse_gui.y;
        inputState.mouse_gui.x = newCursorPositionX;
        inputState.mouse_gui.y = newCursorPositionY;

        // Simluate Mouse Button Press Events
        boolean anyButtonChanged = false;
        for (int i = 0; i <= 4; i++) {
            boolean buttonMouseDown = switch (i) {
                case 0 -> buttonMouse1Down;
                case 1 -> buttonMouse2Down;
                case 2 -> buttonMouse3Down;
                case 3 -> buttonMouse4Down;
                case 4 -> buttonMouse5Down;
                default -> throw new IllegalStateException("Unexpected value: " + i);
            };
            if (inputState.simulatedMouseIsButtonDown[i] != buttonMouseDown) {
                inputState.simulatedMouseIsButtonDown[i] = buttonMouseDown;
                if (inputState.simulatedMouseIsButtonDown[i]) {
                    inputState.inputEvents.mouseDown = true;
                    inputState.inputEvents.mouseDownButtons.add(i);
                    anyButtonChanged = true;
                    if (i == Input.Buttons.LEFT) {
                        // DoubleClick
                        if ((System.currentTimeMillis() - inputState.simulatedMouseLastMouseClick) < DOUBLECLICK_TIME_MS) {
                            inputState.inputEvents.mouseDoubleClick = true;
                        }
                        inputState.simulatedMouseLastMouseClick = System.currentTimeMillis();
                    }

                } else {
                    inputState.inputEvents.mouseUp = true;
                    inputState.inputEvents.mouseUpButtons.add(i);
                    anyButtonChanged = true;
                }
            }
            inputState.inputEvents.mouseButtonsDown[i] = inputState.simulatedMouseIsButtonDown[i];
        }
        if (!anyButtonChanged) {
            inputState.inputEvents.mouseDown = false;
            inputState.inputEvents.mouseUp = false;
            inputState.inputEvents.mouseDoubleClick = false;
            inputState.inputEvents.mouseDownButtons.clear();
            inputState.inputEvents.mouseUpButtons.clear();
        }

        // Simluate Mouse Move Events
        if (deltaX != 0 || deltaY != 0) {
            inputState.inputEvents.mouseMoved = true;
            inputState.inputEvents.mouseDragged = false;
            draggedLoop:
            for (int i = 0; i <= 4; i++) {
                if (inputState.simulatedMouseIsButtonDown[i]) {
                    inputState.inputEvents.mouseDragged = true;
                    inputState.inputEvents.mouseMoved = false;
                    break draggedLoop;
                }
            }
        } else {
            inputState.inputEvents.mouseDragged = false;
            inputState.inputEvents.mouseMoved = false;
        }

        // Simluate Mouse Scroll Events
        inputState.inputEvents.mouseScrolled = buttonScrolledUp || buttonScrolledDown;
        inputState.inputEvents.mouseScrolledAmount = buttonScrolledUp ? -1 : buttonScrolledDown ? 1 : 0;
    }

    private boolean isTranslatedKeyCodeDown(boolean[] translatedKeys, int[] keys) {
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                if (translatedKeys[keys[i]]) {
                    return true;
                }
            }
        }
        return false;
    }


    private void updateGamePadMouseControl() {

        // Swallow & Translate Gamepad Events
        boolean[] translatedButtons = inputState.gamePadTranslatedButtonsDown;
        boolean stickLeft = api.config.isGamePadMouseStickLeftEnabled();
        boolean stickRight = api.config.isGamePadMouseStickRightEnabled();

        float joystickDeadZone = api.config.getGamePadMouseJoystickDeadZone();
        boolean buttonLeft = (stickLeft && inputState.gamePadTranslatedStickLeft.x < -joystickDeadZone) || (stickRight && inputState.gamePadTranslatedStickRight.x < -joystickDeadZone);
        boolean buttonRight = (stickLeft && inputState.gamePadTranslatedStickLeft.x > joystickDeadZone) || (stickRight && inputState.gamePadTranslatedStickRight.x > joystickDeadZone);
        boolean buttonUp = (stickLeft && inputState.gamePadTranslatedStickLeft.y > joystickDeadZone) || (stickRight && inputState.gamePadTranslatedStickRight.y > joystickDeadZone);
        boolean buttonDown = (stickLeft && inputState.gamePadTranslatedStickLeft.y < -joystickDeadZone) || (stickRight && inputState.gamePadTranslatedStickRight.y < -joystickDeadZone);
        boolean buttonMouse1Down = isTranslatedKeyCodeDown(translatedButtons, api.config.getGamePadMouseButtonsMouse1());
        boolean buttonMouse2Down = isTranslatedKeyCodeDown(translatedButtons, api.config.getGamePadMouseButtonsMouse2());
        boolean buttonMouse3Down = isTranslatedKeyCodeDown(translatedButtons, api.config.getGamePadMouseButtonsMouse3());
        boolean buttonMouse4Down = isTranslatedKeyCodeDown(translatedButtons, api.config.getGamePadMouseButtonsMouse4());
        boolean buttonMouse5Down = isTranslatedKeyCodeDown(translatedButtons, api.config.getGamePadMouseButtonsMouse5());
        boolean buttonScrolledUp = isTranslatedKeyCodeDown(translatedButtons, api.config.getGamePadMouseButtonsScrollUp());
        boolean buttonScrolledDown = isTranslatedKeyCodeDown(translatedButtons, api.config.getGamePadMouseButtonsScrollDown());

        float cursorSpeedX = 0f;
        if (buttonLeft || buttonRight) {
            cursorSpeedX = Math.max(Math.abs(inputState.gamePadTranslatedStickLeft.x), Math.abs(inputState.gamePadTranslatedStickRight.x));
            cursorSpeedX = (cursorSpeedX - joystickDeadZone) / (1f - joystickDeadZone);
        }
        float cursorSpeedY = 0f;
        if (buttonUp || buttonDown) {
            cursorSpeedY = Math.max(Math.abs(inputState.gamePadTranslatedStickLeft.y), Math.abs(inputState.gamePadTranslatedStickRight.y));
            cursorSpeedY = (cursorSpeedY - joystickDeadZone) / (1f - joystickDeadZone);
        }
        // Translate to mouse events
        translateSimulatedMouseEvents(buttonLeft, buttonRight, buttonUp, buttonDown,
                buttonMouse1Down, buttonMouse2Down, buttonMouse3Down, buttonMouse4Down, buttonMouse5Down,
                buttonScrolledUp, buttonScrolledDown, cursorSpeedX, cursorSpeedY
        );
    }

    private void updateKeyBoardMouseControl() {

        if (inputState.focusedTextField != null)
            return; // Stop Keyboard control if the user wants to type into a textfield


        // Swallow & Translate keyboard events
        boolean[] translatedKeys = inputState.keyBoardTranslatedKeysDown;

        boolean buttonLeft = isTranslatedKeyCodeDown(translatedKeys, api.config.getKeyboardMouseButtonsLeft());
        boolean buttonRight = isTranslatedKeyCodeDown(translatedKeys, api.config.getKeyboardMouseButtonsRight());
        boolean buttonUp = isTranslatedKeyCodeDown(translatedKeys, api.config.getKeyboardMouseButtonsUp());
        boolean buttonDown = isTranslatedKeyCodeDown(translatedKeys, api.config.getKeyBoardControlButtonsDown());
        boolean buttonMouse1Down = isTranslatedKeyCodeDown(translatedKeys, api.config.getKeyboardMouseButtonsMouse1());
        boolean buttonMouse2Down = isTranslatedKeyCodeDown(translatedKeys, api.config.getKeyboardMouseButtonsMouse2());
        boolean buttonMouse3Down = isTranslatedKeyCodeDown(translatedKeys, api.config.getKeyboardMouseButtonsMouse3());
        boolean buttonMouse4Down = isTranslatedKeyCodeDown(translatedKeys, api.config.getKeyboardMouseButtonsMouse4());
        boolean buttonMouse5Down = isTranslatedKeyCodeDown(translatedKeys, api.config.getKeyboardMouseButtonsMouse5());
        boolean buttonScrolledUp = isTranslatedKeyCodeDown(translatedKeys, api.config.getKeyboardMouseButtonsScrollUp());
        boolean buttonScrolledDown = isTranslatedKeyCodeDown(translatedKeys, api.config.getKeyboardMouseButtonsScrollDown());

        final float SPEEDUP_SPEED = 0.1f;
        if (buttonLeft || buttonRight){
            inputState.keyBoardMouseSpeedUp.x = Tools.Calc.inBounds(inputState.keyBoardMouseSpeedUp.x < 1f ? inputState.keyBoardMouseSpeedUp.x + SPEEDUP_SPEED : inputState.keyBoardMouseSpeedUp.x, 0f, 1f);
        }else{
            inputState.keyBoardMouseSpeedUp.set(0,inputState.keyBoardMouseSpeedUp.y);
        }
        if(buttonUp || buttonDown){
            inputState.keyBoardMouseSpeedUp.y = Tools.Calc.inBounds(inputState.keyBoardMouseSpeedUp.y < 1f ? inputState.keyBoardMouseSpeedUp.y + SPEEDUP_SPEED : inputState.keyBoardMouseSpeedUp.y, 0f, 1f);
        }else{
            inputState.keyBoardMouseSpeedUp.set(inputState.keyBoardMouseSpeedUp.x,0);
        }

        // Translate to mouse events
        translateSimulatedMouseEvents(buttonLeft, buttonRight, buttonUp, buttonDown,
                buttonMouse1Down, buttonMouse2Down, buttonMouse3Down, buttonMouse4Down, buttonMouse5Down,
                buttonScrolledUp, buttonScrolledDown, inputState.keyBoardMouseSpeedUp.x, inputState.keyBoardMouseSpeedUp.y
        );
    }

    private void updateGUIMouseBounds() {
        if (inputState.mouse_gui.x < 0) inputState.mouse_gui.x = 0;
        if (inputState.mouse_gui.x > inputState.internalResolutionWidth)
            inputState.mouse_gui.x = inputState.internalResolutionWidth;
        if (inputState.mouse_gui.y < 0) inputState.mouse_gui.y = 0;
        if (inputState.mouse_gui.y > inputState.internalResolutionHeight)
            inputState.mouse_gui.y = inputState.internalResolutionHeight;
    }

    private void updateGameMouseXY() {
        // MouseXGUI/MouseYGUI -> To MouseX/MouseY
        inputState.vector_fboCursor.x = inputState.mouse_gui.x;
        inputState.vector_fboCursor.y = Gdx.graphics.getHeight() - inputState.mouse_gui.y;
        inputState.vector_fboCursor.z = 1;
        inputState.camera_game.unproject(inputState.vector_fboCursor, 0, 0, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
        this.inputState.mouse.x = (int) inputState.vector_fboCursor.x;
        this.inputState.mouse.y = (int) inputState.vector_fboCursor.y;
    }

    private void updateHardwareMouseControl() {
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
        inputState.mouse_delta.x = MathUtils.round(inputState.vector_fboCursor.x - inputState.mouse_gui.x);
        inputState.mouse_delta.y = MathUtils.round(inputState.vector_fboCursor.y - inputState.mouse_gui.y);
        inputState.mouse_gui.x = Tools.Calc.inBounds(MathUtils.round(inputState.vector_fboCursor.x), 0, inputState.internalResolutionWidth);
        inputState.mouse_gui.y = Tools.Calc.inBounds(MathUtils.round(inputState.vector_fboCursor.y), 0, inputState.internalResolutionHeight);
    }

    private void updateLastGUIMouseHover() {
        inputState.lastGUIMouseHover = findCurrentLastGUIMouseHover();
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
                        notification.scroll += MathUtils.round(api.config.getNotificationsScrollSpeed());
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
                }
                case FADEOUT -> {
                    if ((System.currentTimeMillis() - notification.timer > api.config.getNotificationsFadeoutTime())) {
                        UICommons.notification_removeFromScreen(inputState, notification);
                    }
                }
            }
        }
    }


    private void executeOnMouseClickCommonAction(Object uiObject, int button) {
        if (uiObject == null) return;
        CommonActions commonActions = getUIObjectCommonActions(uiObject);
        if (commonActions != null) commonActions.onMouseClick(button);
        if (uiObject instanceof Component component) {
            executeOnMouseClickCommonAction(component.addedToWindow, button);
        }
    }


    private void executeOnMouseDoubleClickCommonAction(Object uiObject, int button) {
        if (uiObject == null) return;
        CommonActions commonActions = getUIObjectCommonActions(uiObject);
        if (commonActions != null) commonActions.onMouseDoubleClick(button);
        if (uiObject instanceof Component component) {
            executeOnMouseDoubleClickCommonAction(component.addedToWindow, button);
        }
    }

    private void executeOnMouseScrollCommonAction(Object uiObject, float scrolled) {
        if (uiObject == null) return;
        CommonActions commonActions = getUIObjectCommonActions(uiObject);
        if (commonActions != null) commonActions.onMouseScroll(scrolled);
        if (uiObject instanceof Component component) {
            executeOnMouseScrollCommonAction(component.addedToWindow, scrolled);
        }
    }

    private CommonActions getUIObjectCommonActions(Object uiObject) {
        if (uiObject.getClass() == Window.class) { // Not a component
            return ((Window) uiObject).windowAction;
        } else if (uiObject.getClass() == Notification.class) { // Not a component
            return ((Notification) uiObject).notificationAction;
        } else if (uiObject.getClass() == Button.class) {
            return ((Button) uiObject).buttonAction;
        } else if (uiObject.getClass() == ComboBox.class) {
            return ((ComboBox) uiObject).comboBoxAction;
        } else if (uiObject.getClass() == GameViewPort.class) {
            return ((GameViewPort) uiObject).gameViewPortAction;
        } else if (uiObject.getClass() == Image.class) {
            return ((Image) uiObject).imageAction;
        } else if (uiObject.getClass() == Inventory.class) {
            return ((Inventory) uiObject).inventoryAction;
        } else if (uiObject.getClass() == List.class) {
            return ((List) uiObject).listAction;
        } else if (uiObject.getClass() == Map.class) {
            return ((Map) uiObject).mapAction;
        } else if (uiObject.getClass() == ScrollBarHorizontal.class || uiObject.getClass() == ScrollBarVertical.class) {
            return ((ScrollBar) uiObject).scrollBarAction;
        } else if (uiObject.getClass() == TabBar.class) {
            return ((TabBar) uiObject).tabBarAction;
        } else if (uiObject.getClass() == Text.class) {
            return ((Text) uiObject).textAction;
        } else if (uiObject.getClass() == TextField.class) {
            return ((TextField) uiObject).textFieldAction;
        } else {
            return null;
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
            inputState.cursor = api.config.getCursorGui();
        } else {
            // 2. Manually overidden Cursor
            if (inputState.displayOverrideCursor) {
                inputState.cursor = inputState.overrideCursor;
                inputState.displayOverrideCursor = false;
            } else {
                if (inputState.mouseTool != null) {
                    // 3. Mouse Tool cursor
                    if (inputState.inputEvents.mouseButtonsDown[Input.Buttons.LEFT]) {
                        inputState.cursor = inputState.mouseTool.cursorDown;
                    } else {
                        inputState.cursor = inputState.mouseTool.cursor;
                    }
                } else {
                    // no mouse tool set - display no cursor
                    inputState.cursor = null;
                }
            }
        }

    }

    private boolean executeUpdateAction(UpdateAction updateAction, long currentTimeMillis) {
        if ((currentTimeMillis - updateAction.lastUpdate) > updateAction.interval) {
            updateAction.onUpdate();
            updateAction.lastUpdate = currentTimeMillis;
            return true;
        }
        return false;
    }

    private Object findCurrentLastGUIMouseHover() {
        // Notification Collision
        for (int i = 0; i < inputState.notifications.size(); i++) {
            Notification notification = inputState.notifications.get(i);
            if (notification.notificationAction != null && Tools.Calc.pointRectsCollide(inputState.mouse_gui.x, inputState.mouse_gui.y,
                    0, inputState.internalResolutionWidth - ((i + 1) * TILE_SIZE),
                    inputState.internalResolutionWidth, TILE_SIZE)) {
                return notification;
            }
        }

        // Context Menu Item collision
        if (inputState.openContextMenu != null) {
            for (int i = 0; i < inputState.openContextMenu.items.size(); i++) {
                if (Tools.Calc.pointRectsCollide(inputState.mouse_gui.x, inputState.mouse_gui.y, inputState.openContextMenu.x, inputState.openContextMenu.y - (TILE_SIZE) - (i * TILE_SIZE), inputState.displayedContextMenuWidth * TILE_SIZE, TILE_SIZE)) {
                    return inputState.openContextMenu.items.get(i);
                }
            }
        }

        // Combobox Open Menu collision
        if (inputState.openComboBox != null) {
            if (Tools.Calc.pointRectsCollide(inputState.mouse_gui.x, inputState.mouse_gui.y, UICommons.component_getAbsoluteX(inputState.openComboBox), UICommons.component_getAbsoluteY(inputState.openComboBox) - (inputState.openComboBox.items.size() * TILE_SIZE), inputState.openComboBox.width * TILE_SIZE, (inputState.openComboBox.items.size() * TILE_SIZE))) {
                return inputState.openComboBox;
            }
        }

        // Window / WindowComponent collision
        windowLoop:
        for (int i = inputState.windows.size() - 1; i >= 0; i--) { // use for(i) to avoid iterator creation
            Window window = inputState.windows.get(i);
            if (!window.visible) continue windowLoop;

            int wndX = window.x;
            int wndY = window.y + (window.folded ? ((window.height - 1) * TILE_SIZE) : 0);
            int wndWidth = UICommons.window_getRealWidth(window);
            int wndHeight = UICommons.window_getRealHeight(window);

            boolean collidesWithWindow = Tools.Calc.pointRectsCollide(inputState.mouse_gui.x, inputState.mouse_gui.y, wndX, wndY, wndWidth, wndHeight);
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
        for (int i = 0; i < inputState.screenComponents.size(); i++) { // use for(i) to avoid iterator creation
            Component screenComponent = inputState.screenComponents.get(i);
            if (mouseCollidesWithComponent(screenComponent)) return screenComponent;
        }
        return null;
    }


    private boolean mouseCollidesWithComponent(Component component) {
        if (!component.visible) return false;
        if (component.disabled) return false;
        if (UICommons.component_isHiddenByTab(component)) return false;

        if (Tools.Calc.pointRectsCollide(inputState.mouse_gui.x, inputState.mouse_gui.y, UICommons.component_getAbsoluteX(component), UICommons.component_getAbsoluteY(component), component.width * TILE_SIZE, component.height * TILE_SIZE)) {
            inputState.lastGUIMouseHover = component;
            return true;
        }

        return false;
    }

    public void render() {
        render(false);
    }

    public void render(boolean frameBuffersOnly) {

        // Draw Game
        {
            // Draw GUI GameViewPort FrameBuffers
            for (int i = 0; i < this.inputState.gameViewPorts.size(); i++) {
                renderGameViewPortFrameBuffer(inputState.gameViewPorts.get(i));
            }

            // Draw Main FrameBuffer
            inputState.spriteBatch_game.setProjectionMatrix(this.inputState.camera_game.combined);
            inputState.frameBuffer_game.begin();
            this.uiAdapter.render(inputState.spriteBatch_game, true);
            inputState.frameBuffer_game.end();
        }


        { // Draw GUI
            inputState.frameBuffer_gui.begin();
            render_glClear();
            inputState.spriteBatch_gui.setProjectionMatrix(this.inputState.camera_gui.combined);
            this.uiAdapter.renderUIBefore(inputState.spriteBatch_gui);
            this.renderGUI();
            this.uiAdapter.renderUIAfter(inputState.spriteBatch_gui);
            inputState.frameBuffer_gui.end();
        }


        {
            // Draw to Upscale Buffer
            inputState.spriteBatch_screen.setProjectionMatrix(inputState.camera_screen.combined);
            inputState.frameBuffer_screen.begin();
            this.uiAdapter.renderFinalScreen(inputState.spriteBatch_screen,
                    inputState.texture_game, inputState.texture_gui,
                    inputState.internalResolutionWidth, inputState.internalResolutionHeight
            );
            inputState.frameBuffer_screen.end();
            // Draw to Screen
            if (!frameBuffersOnly) {
                inputState.viewport_screen.apply();
                render_glClear();
                inputState.spriteBatch_screen.begin();
                inputState.spriteBatch_screen.draw(inputState.texture_screen, 0, 0, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
                inputState.spriteBatch_screen.end();
            }
        }


    }


    private void renderGameViewPortFrameBuffer(GameViewPort gameViewPort) {
        if (render_isComponentNotRendered(gameViewPort)) return;

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
        render_batchSetColorWhite(1f);

        if (inputState.modalWindow != null) render_enableGrayScaleShader(true);

        /* Draw Screen Components */
        for (int i = 0; i < inputState.screenComponents.size(); i++) {
            Component component = inputState.screenComponents.get(i);
            render_drawComponent(component);
        }

        /* Draw Screen Components Top Layer */
        for (int i = 0; i < inputState.screenComponents.size(); i++) {
            Component component = inputState.screenComponents.get(i);
            render_drawComponentTopLayer(null, component);
        }

        /* Draw Windows */
        for (int i = 0; i < inputState.windows.size(); i++) {
            Window window = inputState.windows.get(i);
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

        /* OnScreenTextInput */
        render_drawOnScreenTextInput();

        /* Cursor */
        render_drawCursorListDrags();

        render_drawCursor();

        inputState.spriteBatch_gui.end();
    }


    private void render_drawOnScreenTextInput() {
        if (inputState.openMouseTextInput == null) return;
        OnScreenTextInput onScreenTextInput = inputState.openMouseTextInput;
        render_batchSetColorWhite(onScreenTextInput.color_a);
        final int CHARACTERS = 4;
        char[] chars = onScreenTextInput.upperCase ? onScreenTextInput.charactersUC : onScreenTextInput.charactersLC;

        // 4 to the left
        for (int i = 1; i <= CHARACTERS; i++) {
            int index = onScreenTextInput.selectedIndex - i;
            if (index >= 0 && index < chars.length) {
                render_drawOnScreenTextInputCharacter(onScreenTextInput.font, chars[index], onScreenTextInput.x - (i * 12), onScreenTextInput.y - ((i * i) / 2), onScreenTextInput.upperCase, false);
            }
        }
        // 4 to the right
        for (int i = 1; i <= CHARACTERS; i++) {
            int index = onScreenTextInput.selectedIndex + i;
            if (index >= 0 && index < chars.length) {
                render_drawOnScreenTextInputCharacter(onScreenTextInput.font, chars[index], onScreenTextInput.x + (i * 12), onScreenTextInput.y - ((i * i) / 2), onScreenTextInput.upperCase, false);
            }
        }
        // 1 in center
        render_drawOnScreenTextInputCharacter(onScreenTextInput.font, chars[onScreenTextInput.selectedIndex], onScreenTextInput.x, onScreenTextInput.y, onScreenTextInput.upperCase, inputState.mTextInputConfirmPressed);

        // Selection
        render_batchSetColor(onScreenTextInput.color_r, onScreenTextInput.color_g, onScreenTextInput.color_b, onScreenTextInput.color_a);
        render_drawCMediaGFX(GUIBaseMedia.GUI_OSTEXTINPUT_SELECTED, onScreenTextInput.x - 1, onScreenTextInput.y - 1);
        render_batchSetColorWhite(1f);
    }

    private void render_drawOnScreenTextInputCharacter(CMediaFont font, char c, int x, int y, boolean upperCase, boolean pressed) {
        int pressedIndex = pressed ? 1 : 0;
        render_drawCMediaGFX(GUIBaseMedia.GUI_OSTEXTINPUT_CHARACTER, x, y, pressedIndex);
        if (c == '\n') {
            render_drawCMediaGFX(GUIBaseMedia.GUI_OSTEXTINPUT_CONFIRM, x, y, pressedIndex);
        }
        if (c == '\t') {
            render_drawCMediaGFX(upperCase ? GUIBaseMedia.GUI_OSTEXTINPUT_UPPERCASE : GUIBaseMedia.GUI_OSTEXTINPUT_LOWERCASE, x, y, pressedIndex);
        }
        if (c == '\b') {
            render_drawCMediaGFX(GUIBaseMedia.GUI_OSTEXTINPUT_DELETE, x, y, pressedIndex);
        } else {
            int offset = pressed ? 1 : 0;
            render_drawFont(font, String.valueOf(c), 1.0f, x + 2 + offset, y + 2 - offset);
        }
    }

    private void render_drawCursor() {
        render_drawCMediaGFX(inputState.cursor, inputState.mouse_gui.x, inputState.mouse_gui.y);
        render_batchSetColorWhite(1f);
    }


    private boolean render_isComponentNotRendered(Component component) {
        if (!component.visible) return true;
        if (component.addedToWindow != null && !component.addedToWindow.visible) return true;
        return UICommons.component_isHiddenByTab(component);
    }

    private int render_getTabCMediaIndex(int x, int width) {
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

    private int render_getWindowCMediaIndex(int x, int y, int width, int height, boolean hasTitleBar) {
        if (hasTitleBar) {
            if (y == (height - 1)) {
                if (x == 0) {
                    return 12;
                } else if (x == width - 1) {
                    return 14;
                } else {
                    return 13;
                }
            } else {
                return render_get16TilesCMediaIndex(x, y, width, height);
            }
        } else {
            return render_get16TilesCMediaIndex(x, y, width, height);
        }
    }


    private int render_getListDragCMediaIndex(int x, int width) {
        return x == 0 ? 0 : x == (width - 1) ? 2 : 1;
    }

    private int render_get16TilesCMediaIndex(int x, int y, int width, int height) {
        if (width == 1 && height == 1) return 3;
        if (width == 1) {
            if (y == 0) {
                return 7;
            } else if (y == height - 1) {
                return 15;
            } else {
                return 11;
            }
        } else if (height == 1) {
            if (x == 0) {
                return 12;
            } else if (x == width - 1) {
                return 14;
            } else {
                return 13;
            }
        } else {
            if (x == 0 && y == 0) {
                return 8;
            } else if (x == width - 1 && y == height - 1) {
                return 2;
            } else if (x == width - 1 && y == 0) {
                return 10;
            } else if (x == 0 && y == height - 1) {
                return 0;
            } else {
                if (x == 0) {
                    return 4;
                } else if (x == width - 1) {
                    return 6;
                } else if (y == 0) {
                    return 9;
                } else if (y == height - 1) {
                    return 1;
                } else {
                    return 5;
                }
            }
        }
    }

    private int render_get9TilesCMediaIndex(int x, int y, int width, int height) {
        if (x == 0 && y == 0) return 0;
        if (x == width - 1 && y == 0) return 2;
        if (x == 0 && y == (height - 1)) return 6;
        if (x == width - 1 && y == (height - 1)) return 8;
        if (y == 0) return 1;
        if (y == (height - 1)) return 7;
        if (x == 0) return 3;
        if (x == width - 1) return 5;
        return 4;
    }

    private void render_drawComponentTopLayer(Window window, Component component) {
        if (render_isComponentNotRendered(component)) return;
        float alpha = (window != null ? (component.color_a * window.color_a) : component.color_a);
        render_batchSetColor(component.color_r, component.color_g, component.color_b, alpha);
        if (component.getClass() == ComboBox.class) {
            ComboBox combobox = (ComboBox) component;
            // Menu
            if (UICommons.comboBox_isOpen(combobox, inputState)) {
                int width = combobox.width;
                int height = combobox.items.size();
                /* Menu */
                for (int iy = 0; iy < height; iy++) {
                    ComboBoxItem comboBoxItem = combobox.items.get(iy);
                    for (int ix = 0; ix < width; ix++) {
                        int index = render_get9TilesCMediaIndex(ix, iy, width, height);//x==0 ? 0 : (x == (width-1)) ? 2 : 1;
                        CMediaArray cMenuTexture;
                        if (Tools.Calc.pointRectsCollide(inputState.mouse_gui.x, inputState.mouse_gui.y, UICommons.component_getAbsoluteX(combobox), UICommons.component_getAbsoluteY(combobox) - (TILE_SIZE) - (iy * TILE_SIZE), combobox.width * TILE_SIZE, TILE_SIZE)) {
                            cMenuTexture = GUIBaseMedia.GUI_COMBOBOX_LIST_SELECTED;
                        } else {
                            cMenuTexture = GUIBaseMedia.GUI_COMBOBOX_LIST;
                        }
                        render_batchSaveColor();
                        render_batchSetColor(comboBoxItem.color_r, comboBoxItem.color_g, comboBoxItem.color_b, alpha);
                        render_drawCMediaGFX(cMenuTexture, UICommons.component_getAbsoluteX(combobox) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(combobox) - (iy * TILE_SIZE) - TILE_SIZE, index);
                        render_batchLoadColor();
                    }
                }

                /* Text */
                for (int i = 0; i < combobox.items.size(); i++) {
                    ComboBoxItem comboBoxItem = combobox.items.get(i);
                    render_drawFont(comboBoxItem.font, comboBoxItem.text, alpha, UICommons.component_getAbsoluteX(combobox), UICommons.component_getAbsoluteY(combobox) - (i * TILE_SIZE) - TILE_SIZE, 2, 1, (combobox.width * TILE_SIZE), comboBoxItem.icon, comboBoxItem.iconIndex);
                }
            }

        }

        render_batchSetColorWhite(1f);
    }

    private void render_drawContextMenu() {
        if (inputState.openContextMenu != null) {
            float alpha = inputState.openContextMenu.color_a;

            ContextMenu contextMenu = inputState.openContextMenu;
            int width = inputState.displayedContextMenuWidth;
            int height = contextMenu.items.size();


            /* Menu */
            for (int iy = 0; iy < height; iy++) {
                ContextMenuItem contextMenuItem = contextMenu.items.get(iy);
                for (int ix = 0; ix < width; ix++) {
                    int index = render_get9TilesCMediaIndex(ix, iy, width, height);//x==0 ? 0 : (x == (width-1)) ? 2 : 1;
                    CMediaArray cMenuTexture;
                    if (Tools.Calc.pointRectsCollide(inputState.mouse_gui.x, inputState.mouse_gui.y, contextMenu.x, contextMenu.y - (TILE_SIZE) - (iy * TILE_SIZE), inputState.displayedContextMenuWidth * TILE_SIZE, TILE_SIZE)) {
                        cMenuTexture = GUIBaseMedia.GUI_CONTEXT_MENU_SELECTED;
                    } else {
                        cMenuTexture = GUIBaseMedia.GUI_CONTEXT_MENU;
                    }
                    render_batchSaveColor();
                    render_batchSetColor(contextMenuItem.color_r, contextMenuItem.color_g, contextMenuItem.color_b, alpha);
                    render_drawCMediaGFX(cMenuTexture, contextMenu.x + (ix * TILE_SIZE), contextMenu.y - (iy * TILE_SIZE) - TILE_SIZE, index);
                    render_batchLoadColor();
                }
            }

            /* Text */
            for (int iy = 0; iy < contextMenu.items.size(); iy++) {
                ContextMenuItem item = contextMenu.items.get(iy);
                render_drawFont(item.font, item.text, alpha, contextMenu.x, contextMenu.y - (iy * TILE_SIZE) - TILE_SIZE, 2, 1, (width * TILE_SIZE), item.icon, item.iconIndex);
            }

        }


        render_batchSetColorWhite(1f);
    }

    private void render_drawTooltip() {
        if (inputState.tooltip == null) return;
        if (inputState.tooltip_wait_delay) return;
        if (inputState.tooltip.lines == null || inputState.tooltip.lines.length == 0) return;

        ToolTip tooltip = inputState.tooltip;

        int text_width_max = 0;
        for (int i = 0; i < tooltip.lines.length; i++) {
            String line = tooltip.lines[i];
            int line_width = mediaManager.textWidth(tooltip.font, line);
            if (line_width > text_width_max) text_width_max = line_width;
        }

        int tooltip_width = Tools.Calc.lowerBounds(MathUtils.ceil((text_width_max + (TILE_SIZE)) / (float) TILE_SIZE), tooltip.minWidth);
        int tooltip_height = Tools.Calc.lowerBounds(tooltip.lines.length, tooltip.minHeight);

        for (int i = 0; i < tooltip.images.size(); i++) {
            ToolTipImage toolTipImage = tooltip.images.get(i);
            int imageWidthMin = toolTipImage.x + MathUtils.ceil(mediaManager.imageWidth(toolTipImage.image) / UIEngine.TILE_SIZE_F);
            int imageHeightMin = toolTipImage.y + MathUtils.ceil(mediaManager.imageHeight(toolTipImage.image) / UIEngine.TILE_SIZE_F);
            if (imageWidthMin > tooltip_width) tooltip_width = imageWidthMin;
            if (imageHeightMin > tooltip_height) tooltip_height = imageHeightMin;
        }

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


        if (inputState.mouse_gui.x + ((tooltip_width + 2) * TILE_SIZE) <= inputState.internalResolutionWidth) {
            collidesRight = false;
            //direction = 1;
        }
        if (inputState.mouse_gui.x - ((tooltip_width + 2) * TILE_SIZE) >= 0) {
            collidesLeft = false;
            //direction = 2;
        }
        if (inputState.mouse_gui.y - ((tooltip_height + 2) * TILE_SIZE) >= 0) {
            collidesDown = false;
            //direction = 3;
        }
        if (inputState.mouse_gui.y + ((tooltip_height + 2) * TILE_SIZE) <= inputState.internalResolutionHeight) { // Push down
            collidesUp = false;
            //direction = 4;
        }

        if (collidesUp) direction = 4;
        if (collidesDown) direction = 3;
        if (collidesLeft) direction = 1;
        if (collidesRight) direction = 2;

        switch (direction) {
            case 1 -> {
                tooltip_x = inputState.mouse_gui.x + (2 * TILE_SIZE);
                tooltip_y = inputState.mouse_gui.y - ((tooltip_height * TILE_SIZE) / 2);
            }
            case 2 -> {
                tooltip_x = inputState.mouse_gui.x - ((tooltip_width + 2) * TILE_SIZE);
                tooltip_y = inputState.mouse_gui.y - ((tooltip_height * TILE_SIZE) / 2);
            }
            case 3 -> {
                tooltip_x = inputState.mouse_gui.x - ((tooltip_width * TILE_SIZE) / 2);
                tooltip_y = inputState.mouse_gui.y + ((2) * TILE_SIZE);
            }
            case 4 -> {
                tooltip_x = inputState.mouse_gui.x - ((tooltip_width * TILE_SIZE) / 2);
                tooltip_y = inputState.mouse_gui.y - ((tooltip_height + 2) * TILE_SIZE);
            }
        }


        // Draw
        float alpha = tooltip.color_a * inputState.tooltip_fadeIn_pct;
        render_batchSetColor(tooltip.color_r, tooltip.color_g, tooltip.color_b, alpha);

        // Lines
        switch (direction) {
            case 1 -> {
                render_drawCMediaGFX(GUIBaseMedia.GUI_TOOLTIP_LINE_X, inputState.mouse_gui.x, inputState.mouse_gui.y);
                render_drawCMediaGFX(GUIBaseMedia.GUI_TOOLTIP_LINE_X, inputState.mouse_gui.x + TILE_SIZE, inputState.mouse_gui.y);
            }
            case 2 -> {
                render_drawCMediaGFX(GUIBaseMedia.GUI_TOOLTIP_LINE_X, inputState.mouse_gui.x - TILE_SIZE, inputState.mouse_gui.y);
                render_drawCMediaGFX(GUIBaseMedia.GUI_TOOLTIP_LINE_X, inputState.mouse_gui.x - (TILE_SIZE * 2), inputState.mouse_gui.y);
            }
            case 3 -> {
                render_drawCMediaGFX(GUIBaseMedia.GUI_TOOLTIP_LINE_Y, inputState.mouse_gui.x, inputState.mouse_gui.y);
                render_drawCMediaGFX(GUIBaseMedia.GUI_TOOLTIP_LINE_Y, inputState.mouse_gui.x, inputState.mouse_gui.y + TILE_SIZE);
            }
            case 4 -> {
                render_drawCMediaGFX(GUIBaseMedia.GUI_TOOLTIP_LINE_Y, inputState.mouse_gui.x, inputState.mouse_gui.y - TILE_SIZE);
                render_drawCMediaGFX(GUIBaseMedia.GUI_TOOLTIP_LINE_Y, inputState.mouse_gui.x, inputState.mouse_gui.y - (TILE_SIZE * 2));
            }
        }

        // Box
        for (int tx = 0; tx < tooltip_width; tx++) {
            for (int ty = 0; ty < tooltip_height; ty++) {
                if (tooltip.displayFistLineAsTitle && ty == (tooltip_height - 1)) {
                    int titleIndex = (tx == 0 ? 0 : ((tx == tooltip_width - 1) ? 2 : 1));
                    render_drawCMediaGFX(GUIBaseMedia.GUI_TOOLTIP_TITLE, tooltip_x + (tx * TILE_SIZE), tooltip_y + (ty * TILE_SIZE), titleIndex);
                } else {
                    render_drawCMediaGFX(GUIBaseMedia.GUI_TOOLTIP, tooltip_x + (tx * TILE_SIZE), tooltip_y + (ty * TILE_SIZE), render_get16TilesCMediaIndex(tx, ty, tooltip_width, tooltip_height));
                }
            }
        }


        //Text
        for (int ty = 0; ty < tooltip_height; ty++) {
            int lineIndex = tooltip_height - ty - 1;
            if (lineIndex < tooltip.lines.length) {
                String lineTxt = tooltip.lines[lineIndex];
                if (tooltip.displayFistLineAsTitle && ty == (tooltip_height - 1)) {
                    int text_width = mediaManager.textWidth(tooltip.font, lineTxt);
                    int text_x = tooltip_x + MathUtils.round((tooltip_width * TILE_SIZE) / 2f) - MathUtils.round(text_width / 2f);
                    int text_y = tooltip_y + (ty * TILE_SIZE);
                    render_drawFont(tooltip.font, lineTxt, tooltip.color_a * inputState.tooltip_fadeIn_pct, text_x, text_y, 1, 1);
                } else {
                    render_drawFont(tooltip.font, lineTxt, tooltip.color_a * inputState.tooltip_fadeIn_pct, tooltip_x, tooltip_y + (ty * TILE_SIZE), 2, 1);
                }
            }
        }

        // Images
        for (int i = 0; i < tooltip.images.size(); i++) {
            ToolTipImage toolTipImage = tooltip.images.get(i);
            render_batchSaveColor();
            render_batchSetColor(toolTipImage.color_r, toolTipImage.color_g, toolTipImage.color_b, alpha);
            int toolTipImageX = tooltip_x + (toolTipImage.x * UIEngine.TILE_SIZE);
            int toolTipImageY = tooltip_y + (toolTipImage.y * UIEngine.TILE_SIZE);
            render_drawCMediaGFX(toolTipImage.image, toolTipImageX, toolTipImageY);
            render_batchLoadColor();
        }


        render_batchSetColorWhite(1f);
    }

    private void render_drawNotifications() {
        if (inputState.notifications.size() == 0) return;
        int width = (inputState.internalResolutionWidth % TILE_SIZE == 0) ? (inputState.internalResolutionWidth / TILE_SIZE) : ((inputState.internalResolutionWidth / TILE_SIZE) + 1);


        int y = 0;
        int yOffsetSlideFade = 0;
        for (int i = 0; i < inputState.notifications.size(); i++) {
            Notification notification = inputState.notifications.get(i);
            if (notification.state == STATE_NOTIFICATION.FADEOUT) {
                float fadeoutProgress = ((System.currentTimeMillis() - notification.timer) / (float) api.config.getNotificationsFadeoutTime());
                yOffsetSlideFade = yOffsetSlideFade + MathUtils.round(TILE_SIZE * (fadeoutProgress));
            }
            render_batchSaveColor();
            render_batchSetColor(notification.color_r, notification.color_g, notification.color_b, notification.color_a);
            for (int ix = 0; ix < width; ix++) {
                render_drawCMediaGFX(GUIBaseMedia.GUI_NOTIFICATION_BAR, (ix * TILE_SIZE), inputState.internalResolutionHeight - TILE_SIZE - (y * TILE_SIZE) + yOffsetSlideFade);
            }
            int xOffset = ((width * TILE_SIZE) / 2) - (mediaManager.textWidth(notification.font, notification.text) / 2) - notification.scroll;
            render_drawFont(notification.font, notification.text, notification.color_a, xOffset, (inputState.internalResolutionHeight - TILE_SIZE - (y * TILE_SIZE)) + 1 + yOffsetSlideFade);
            y = y + 1;
            render_batchLoadColor();
        }

        render_batchSetColorWhite(1f);
    }

    private void render_drawWindow(Window window) {
        if (!window.visible) return;
        render_batchSetColor(window.color_r, window.color_g, window.color_b, window.color_a);
        for (int ix = 0; ix < window.width; ix++) {
            if (!window.folded) {
                for (int iy = 0; iy < window.height; iy++) {
                    render_drawCMediaGFX(GUIBaseMedia.GUI_WINDOW, window.x + (ix * TILE_SIZE), window.y + (iy * TILE_SIZE), render_getWindowCMediaIndex(ix, iy, window.width, window.height, window.hasTitleBar));
                }
            } else {
                render_drawCMediaGFX(GUIBaseMedia.GUI_WINDOW, window.x + (ix * TILE_SIZE), window.y + ((window.height - 1) * TILE_SIZE), render_getWindowCMediaIndex(ix, (window.height - 1), window.width, window.height, window.hasTitleBar));
            }
        }

        if (window.hasTitleBar) {
            render_drawFont(window.font, window.title, window.color_a, window.x, window.y + (window.height * TILE_SIZE) - TILE_SIZE, 1, 1, (window.width - 1) * TILE_SIZE, window.icon, window.iconIndex);
        }
        // Draw Components
        for (int i = 0; i < window.components.size(); i++) {
            Component component = window.components.get(i);
            if (!window.folded) {
                render_drawComponent(component);
            } else {
                if (component.y == window.height - 1) {
                    // draw title bar components only if folded
                    render_drawComponent(component);
                }
            }
        }

        // Draw Component TopLayer
        for (int i = 0; i < window.components.size(); i++) {
            Component component = window.components.get(i);
            if (!window.folded) render_drawComponentTopLayer(window, component);
        }

        render_batchSetColorWhite(1f);
    }


    private void render_drawComponent(Component component) {
        if (render_isComponentNotRendered(component)) return;

        float alpha = (component.addedToWindow != null ? (component.color_a * component.addedToWindow.color_a) : component.color_a);
        float alpha2 = (component.addedToWindow != null ? (component.color2_a * component.addedToWindow.color_a) : component.color2_a);
        boolean disableShaderState = render_GrayScaleShaderEnabled();
        if (component.disabled) render_enableGrayScaleShader(true);

        render_batchSetColor(component.color_r, component.color_g, component.color_b, alpha);

        if (component instanceof Button button) {
            CMediaArray buttonMedia = (button.pressed ? GUIBaseMedia.GUI_BUTTON_PRESSED : GUIBaseMedia.GUI_BUTTON);
            int pressed_offset = button.pressed ? 1 : 0;

            for (int ix = 0; ix < button.width; ix++) {
                for (int iy = 0; iy < button.height; iy++) {
                    render_drawCMediaGFX(buttonMedia, UICommons.component_getAbsoluteX(button) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(button) + (iy * TILE_SIZE), render_get16TilesCMediaIndex(ix, iy, button.width, button.height));
                }
            }
            if (button.getClass() == TextButton.class) {
                TextButton textButton = (TextButton) button;
                if (textButton.text != null) {
                    render_drawFont(textButton.font, textButton.text, alpha2, UICommons.component_getAbsoluteX(textButton) + textButton.offset_content_x + pressed_offset, UICommons.component_getAbsoluteY(button) + textButton.offset_content_y - pressed_offset, 1, 2, button.width * TILE_SIZE, textButton.icon, textButton.iconArrayIndex);
                }
            } else if (button.getClass() == ImageButton.class) {
                ImageButton imageButton = (ImageButton) button;
                render_batchSaveColor();
                render_batchSetColor(imageButton.color2_r, imageButton.color2_g, imageButton.color2_b, alpha2);
                render_drawCMediaGFX(imageButton.image, UICommons.component_getAbsoluteX(imageButton) + imageButton.offset_content_x + pressed_offset, UICommons.component_getAbsoluteY(imageButton) + imageButton.offset_content_y - pressed_offset, imageButton.arrayIndex);
                render_batchLoadColor();
            }
        } else if (component.getClass() == Image.class) {
            Image image = (Image) component;
            if (image.image != null) {
                render_drawCMediaGFX(image.image, UICommons.component_getAbsoluteX(image), UICommons.component_getAbsoluteY(image), image.arrayIndex, image.animationOffset);
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
                render_drawCMediaGFX(GUIBaseMedia.GUI_SCROLLBAR_VERTICAL, UICommons.component_getAbsoluteX(scrollBarVertical), UICommons.component_getAbsoluteY(scrollBarVertical) + (i * TILE_SIZE), index);
                int buttonYOffset = MathUtils.round(scrollBarVertical.scrolled * ((scrollBarVertical.height - 1) * TILE_SIZE));
                render_batchSaveColor();
                render_batchSetColor(scrollBarVertical.color2_r, scrollBarVertical.color2_g, scrollBarVertical.color2_b, alpha2);
                render_drawCMediaGFX(GUIBaseMedia.GUI_SCROLLBAR_BUTTON_VERTICAL, UICommons.component_getAbsoluteX(scrollBarVertical), UICommons.component_getAbsoluteY(scrollBarVertical) + buttonYOffset, (scrollBarVertical.buttonPressed ? 1 : 0));
                render_batchLoadColor();
            }
        } else if (component.getClass() == ScrollBarHorizontal.class) {
            ScrollBarHorizontal scrollBarHorizontal = (ScrollBarHorizontal) component;
            for (int i = 0; i < scrollBarHorizontal.width; i++) {
                int index = (i == 0 ? 0 : (i == (scrollBarHorizontal.width - 1) ? 2 : 1));
                render_drawCMediaGFX(GUIBaseMedia.GUI_SCROLLBAR_HORIZONTAL, UICommons.component_getAbsoluteX(scrollBarHorizontal) + (i * TILE_SIZE), UICommons.component_getAbsoluteY(scrollBarHorizontal), index);
                int buttonXOffset = MathUtils.round(scrollBarHorizontal.scrolled * ((scrollBarHorizontal.width - 1) * TILE_SIZE));
                render_batchSaveColor();
                render_batchSetColor(scrollBarHorizontal.color2_r, scrollBarHorizontal.color2_g, scrollBarHorizontal.color2_b, alpha2);
                render_drawCMediaGFX(GUIBaseMedia.GUI_SCROLLBAR_BUTTON_HORIZONAL, UICommons.component_getAbsoluteX(scrollBarHorizontal) + buttonXOffset, UICommons.component_getAbsoluteY(scrollBarHorizontal), (scrollBarHorizontal.buttonPressed ? 1 : 0));
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
            if ((inputState.listDrag_List != null || inputState.inventoryDrag_Inventory != null) && list == inputState.lastGUIMouseHover) {
                dragEnabled = true;
                dragValid = UICommons.list_canDragIntoList(inputState, list);
                if (dragValid) {
                    drag_x = UICommons.component_getAbsoluteX(list);
                    int y_list = UICommons.component_getAbsoluteY(list);
                    drag_y = y_list + ((inputState.mouse_gui.y - y_list) / TILE_SIZE) * TILE_SIZE;
                }
            }

            boolean grayScaleBefore = render_GrayScaleShaderEnabled();
            if (dragEnabled && !dragValid) {
                render_enableGrayScaleShader(true);
            }

            // List
            for (int iy = 0; iy < list.height; iy++) {
                int itemIndex = itemFrom + iy;
                int itemOffsetY = (((list.height - 1)) - (iy));
                Object item = null;
                if (list.items != null && list.items.size() > 0 && list.listAction != null) {
                    if (itemIndex < list.items.size()) {
                        item = list.items.get(itemIndex);
                    }
                }

                boolean selected = item != null && (list.multiSelect ? list.selectedItems.contains(item) : (list.selectedItem == item));

                // Cell
                Color cellColor = null;
                if (list.listAction != null && list.items != null) {
                    if (itemIndex < list.items.size()) {
                        cellColor = list.listAction.cellColor(item);
                        if (cellColor != null) {
                            render_batchSaveColor();
                            render_batchSetColor(cellColor.r, cellColor.g, cellColor.b, 1);
                        }
                    }
                }
                for (int ix = 0; ix < list.width; ix++) {
                    this.render_drawCMediaGFX(selected ? GUIBaseMedia.GUI_LIST_SELECTED : GUIBaseMedia.GUI_LIST, UICommons.component_getAbsoluteX(list) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(list) + itemOffsetY * TILE_SIZE);
                }
                if (cellColor != null) render_batchLoadColor();

                // Text
                if (item != null) {
                    String text = list.listAction.text(item);
                    render_drawFont(list.font, text, alpha, UICommons.component_getAbsoluteX(list), UICommons.component_getAbsoluteY(list) + itemOffsetY * TILE_SIZE, 1, 2, list.width * TILE_SIZE, list.listAction.icon(item), list.listAction.iconArrayIndex(item));
                }
            }

            if (dragEnabled && dragValid) {
                for (int ix = 0; ix < list.width; ix++) {
                    this.render_drawCMediaGFX(GUIBaseMedia.GUI_LIST_DRAG, drag_x + (ix * TILE_SIZE), drag_y, render_getListDragCMediaIndex(ix, list.width));
                }
            }

            render_enableGrayScaleShader(grayScaleBefore);


        } else if (component.getClass() == ComboBox.class) {
            ComboBox combobox = (ComboBox) component;

            // Box
            for (int ix = 0; ix < combobox.width; ix++) {
                int index = ix == 0 ? 0 : (ix == combobox.width - 1 ? 2 : 1);
                CMediaGFX comboMedia = UICommons.comboBox_isOpen(combobox, inputState) ? GUIBaseMedia.GUI_COMBOBOX_OPEN : GUIBaseMedia.GUI_COMBOBOX;

                // Item color or default color
                float color_r = combobox.selectedItem != null ? combobox.selectedItem.color_r : combobox.color_r;
                float color_g = combobox.selectedItem != null ? combobox.selectedItem.color_g : combobox.color_g;
                float color_b = combobox.selectedItem != null ? combobox.selectedItem.color_b : combobox.color_b;

                render_batchSaveColor();
                render_batchSetColor(color_r, color_g, color_b, alpha2);
                this.render_drawCMediaGFX(comboMedia, UICommons.component_getAbsoluteX(combobox) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(combobox), index);
                render_batchLoadColor();

            }
            // Text
            if (combobox.selectedItem != null && combobox.comboBoxAction != null) {
                render_drawFont(combobox.selectedItem.font, combobox.selectedItem.text, alpha, UICommons.component_getAbsoluteX(combobox), UICommons.component_getAbsoluteY(combobox), 2, 1, (combobox.width - 1) * TILE_SIZE, combobox.selectedItem.icon, combobox.selectedItem.iconIndex);
            }
        } else if (component.getClass() == Knob.class) {
            Knob knob = (Knob) component;

            render_drawCMediaGFX(GUIBaseMedia.GUI_KNOB_BACKGROUND, UICommons.component_getAbsoluteX(knob), UICommons.component_getAbsoluteY(knob));
            render_batchSaveColor();
            render_batchSetColor(knob.color2_r, knob.color2_g, knob.color2_b, alpha2);
            if (knob.endless) {
                int index = MathUtils.round(knob.turned * 36);
                render_drawCMediaGFX(GUIBaseMedia.GUI_KNOB_ENDLESS, UICommons.component_getAbsoluteX(knob), UICommons.component_getAbsoluteY(knob), index);
            } else {
                int index = MathUtils.round(knob.turned * 28);
                render_drawCMediaGFX(GUIBaseMedia.GUI_KNOB, UICommons.component_getAbsoluteX(knob), UICommons.component_getAbsoluteY(knob), index);
            }
            render_batchLoadColor();
        } else if (component.getClass() == Map.class) {
            Map map = (Map) component;
            inputState.spriteBatch_gui.draw(map.texture, UICommons.component_getAbsoluteX(map), UICommons.component_getAbsoluteY(map));

            map.mapOverlays.removeIf(mapOverlay -> {
                if (mapOverlay.fadeOut) {
                    mapOverlay.color_a = 1 - ((System.currentTimeMillis() - mapOverlay.timer) / (float) mapOverlay.fadeOutTime);
                    if (mapOverlay.color_a <= 0) return true;
                }
                render_batchSaveColor();
                render_batchSetColor(mapOverlay.color_r, mapOverlay.color_g, mapOverlay.color_b, alpha * mapOverlay.color_a);
                render_drawCMediaGFX(mapOverlay.image, UICommons.component_getAbsoluteX(map) + mapOverlay.x, UICommons.component_getAbsoluteY(map) + mapOverlay.y, mapOverlay.arrayIndex);
                render_batchLoadColor();
                return false;
            });

        } else if (component.getClass() == TextField.class) {
            TextField textField = (TextField) component;
            for (int ix = 0; ix < textField.width; ix++) {
                int index = ix == (textField.width - 1) ? 2 : (ix == 0) ? 0 : 1;

                render_drawCMediaGFX(inputState.focusedTextField == textField ? GUIBaseMedia.GUI_TEXTFIELD_FOCUSED : GUIBaseMedia.GUI_TEXTFIELD, UICommons.component_getAbsoluteX(textField) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(textField), index);


                if (!textField.contentValid) {
                    render_batchSaveColor();
                    render_batchSetColor(0.90588236f, 0.29803923f, 0.23529412f, 0.2f);
                    render_drawCMediaGFX(GUIBaseMedia.GUI_TEXTFIELD_VALIDATION_OVERLAY, UICommons.component_getAbsoluteX(textField) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(textField), index);
                    render_batchLoadColor();
                }

                if (textField.content != null) {
                    render_drawFont(textField.font, textField.content.substring(textField.offset), alpha, UICommons.component_getAbsoluteX(textField), UICommons.component_getAbsoluteY(textField), 1, 2, (textField.width * TILE_SIZE) - 4);
                    if (UICommons.textField_isFocused(inputState, textField)) {
                        int xOffset = mediaManager.textWidth(textField.font, textField.content.substring(textField.offset, textField.markerPosition)) + 2;
                        if (xOffset < textField.width * TILE_SIZE) {
                            render_drawCMediaGFX(GUIBaseMedia.GUI_TEXTFIELD_CARET, UICommons.component_getAbsoluteX(textField) + xOffset, UICommons.component_getAbsoluteY(textField));
                        }
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
            if ((inputState.listDrag_List != null || inputState.inventoryDrag_Inventory != null) && inventory == inputState.lastGUIMouseHover) {
                dragEnabled = true;
                dragValid = UICommons.inventory_canDragIntoInventory(inputState, inventory);
                if (dragValid) {
                    int x_inventory = UICommons.component_getAbsoluteX(inventory);
                    int y_inventory = UICommons.component_getAbsoluteY(inventory);
                    int m_x = inputState.mouse_gui.x - x_inventory;
                    int m_y = inputState.mouse_gui.y - y_inventory;
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

            for (int ix = 0; ix < inventoryWidth; ix++) {
                for (int iy = 0; iy < inventoryHeight; iy++) {
                    if (inventory.items != null) {
                        CMediaGFX cellMedia;
                        boolean selected = inventory.items[ix][iy] != null && inventory.items[ix][iy] == inventory.selectedItem;
                        if (dragEnabled && dragValid && drag_x == ix && drag_y == iy) {
                            cellMedia = inventory.doubleSized ? GUIBaseMedia.GUI_INVENTORY_DRAGGED_X2 : GUIBaseMedia.GUI_INVENTORY_DRAGGED;
                        } else {
                            if (selected) {
                                cellMedia = inventory.doubleSized ? GUIBaseMedia.GUI_INVENTORY_SELECTED_X2 : GUIBaseMedia.GUI_INVENTORY_SELECTED;
                            } else {
                                cellMedia = inventory.doubleSized ? GUIBaseMedia.GUI_INVENTORY_X2 : GUIBaseMedia.GUI_INVENTORY;
                            }
                        }

                        render_batchSaveColor();

                        // Draw Cell
                        Color cellColor = inventory.inventoryAction != null ? inventory.inventoryAction.cellColor(inventory.items[ix][iy], ix, iy) : null;
                        if (cellColor != null) {
                            render_batchSetColor(cellColor.r, cellColor.g, cellColor.b, 1f);
                        } else {
                            render_batchSetColorWhite(alpha);
                        }
                        int index = inventory.doubleSized ? render_get16TilesCMediaIndex(ix, iy, inventory.width / 2, inventory.height / 2) : render_get16TilesCMediaIndex(ix, iy, inventory.width, inventory.height);
                        render_drawCMediaGFX(cellMedia, UICommons.component_getAbsoluteX(inventory) + (ix * tileSize), UICommons.component_getAbsoluteY(inventory) + (iy * tileSize), index);

                        // Draw Icon
                        CMediaGFX icon = (inventory.items[ix][iy] != null && inventory.inventoryAction != null) ? inventory.inventoryAction.icon(inventory.items[ix][iy]) : null;

                        if (icon != null) {
                            render_batchSetColorWhite(alpha);
                            int iconIndex = inventory.inventoryAction != null ? inventory.inventoryAction.iconArrayIndex(inventory.items[ix][iy]) : 0;
                            render_drawCMediaGFX(icon, UICommons.component_getAbsoluteX(inventory) + (ix * tileSize), UICommons.component_getAbsoluteY(inventory) + (iy * tileSize), iconIndex);
                        }
                        render_batchLoadColor();

                    }
                }
            }
            render_enableGrayScaleShader(grayScaleBefore);
        } else if (component.getClass() == TabBar.class) {
            TabBar tabBar = (TabBar) component;
            int tabXOffset = tabBar.tabOffset;
            int topBorder;
            for (int i = 0; i < tabBar.tabs.size(); i++) {
                Tab tab = tabBar.tabs.get(i);
                int tabWidth = tabBar.bigIconMode ? 2 : tab.width;
                if ((tabXOffset + tabWidth) > tabBar.width) break;

                boolean selected = i == tabBar.selectedTab;
                CMediaGFX tabGraphic;
                if (tabBar.bigIconMode) {
                    tabGraphic = selected ? GUIBaseMedia.GUI_TAB_BIGICON_SELECTED : GUIBaseMedia.GUI_TAB_BIGICON;
                } else {
                    tabGraphic = selected ? GUIBaseMedia.GUI_TAB_SELECTED : GUIBaseMedia.GUI_TAB;
                }

                if (tabBar.bigIconMode) {
                    render_drawCMediaGFX(tabGraphic, UICommons.component_getAbsoluteX(tabBar) + (tabXOffset * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar));
                    // Icon
                    if (tab.icon != null) {
                        int selected_offset = selected ? 0 : 1;
                        render_drawCMediaGFX(tab.icon, UICommons.component_getAbsoluteX(tabBar) + (tabXOffset * TILE_SIZE) + selected_offset, UICommons.component_getAbsoluteY(tabBar) - selected_offset, tab.iconIndex);
                    }
                } else {
                    for (int ix = 0; ix < tabWidth; ix++) {
                        render_drawCMediaGFX(tabGraphic, UICommons.component_getAbsoluteX(tabBar) + (ix * TILE_SIZE) + (tabXOffset * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar), render_getTabCMediaIndex(ix, tab.width));
                    }
                }

                if (!tabBar.bigIconMode) {
                    render_drawFont(tab.font, tab.title, alpha, UICommons.component_getAbsoluteX(tabBar) + (tabXOffset * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar), 2, 1, tabWidth * UIEngine.TILE_SIZE, tab.icon, tab.iconIndex);
                }
                tabXOffset += tabWidth;
            }

            topBorder = tabBar.width - tabXOffset;

            // Top Border Top
            for (int ix = 0; ix < topBorder; ix++) {
                render_drawCMediaGFX(GUIBaseMedia.GUI_TAB_BORDERS, UICommons.component_getAbsoluteX(tabBar) + ((tabXOffset + ix) * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar), 2);
            }

            if (tabBar.border) {
                // Bottom
                for (int ix = 0; ix < tabBar.width; ix++) {
                    render_drawCMediaGFX(GUIBaseMedia.GUI_TAB_BORDERS, UICommons.component_getAbsoluteX(tabBar) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar) - (tabBar.borderHeight * TILE_SIZE), 2);
                }
                // Left/Right
                for (int iy = 0; iy < tabBar.borderHeight; iy++) {
                    int yOffset = (iy + 1) * TILE_SIZE;
                    render_drawCMediaGFX(GUIBaseMedia.GUI_TAB_BORDERS, UICommons.component_getAbsoluteX(tabBar), UICommons.component_getAbsoluteY(tabBar) - yOffset, 0);
                    render_drawCMediaGFX(GUIBaseMedia.GUI_TAB_BORDERS, UICommons.component_getAbsoluteX(tabBar) + ((tabBar.width - 1) * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar) - yOffset, 1);
                }
            }

        } else if (component instanceof Shape shape) {
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
        } else if (component instanceof ProgressBar progressBar) {
            // Bar Background
            for (int ix = 0; ix < progressBar.width; ix++) {
                int index = ix == 0 ? 0 : ix == (progressBar.width - 1) ? 2 : 1;
                render_drawCMediaGFX(GUIBaseMedia.GUI_PROGRESSBAR, UICommons.component_getAbsoluteX(progressBar) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(progressBar), index);
            }

            // Bar Bar
            render_batchSaveColor();
            render_batchSetColor(progressBar.color2_r, progressBar.color2_g, progressBar.color2_b, alpha2);
            int pixels = MathUtils.round(progressBar.progress * (progressBar.width * TILE_SIZE));
            for (int ix = 0; ix < progressBar.width; ix++) {
                int xOffset = ix * TILE_SIZE;
                int index = ix == 0 ? 0 : ix == (progressBar.width - 1) ? 2 : 1;
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
                String percentTxt = progressBar.progressText2Decimal ? UICommons.progressBar_getProgressText2Decimal(progressBar.progress) : UICommons.progressBar_getProgressText(progressBar.progress);
                int xOffset = ((progressBar.width * TILE_SIZE) / 2) - (mediaManager.textWidth(progressBar.font, percentTxt) / 2);
                render_drawFont(progressBar.font, percentTxt, alpha, UICommons.component_getAbsoluteX(progressBar) + xOffset, UICommons.component_getAbsoluteY(progressBar), 0, 1);
            }


        } else if (component.getClass() == CheckBox.class) {
            CheckBox checkBox = (CheckBox) component;

            CMediaArray tex = checkBox.checkBoxStyle == CheckBoxStyle.CHECKBOX ? GUIBaseMedia.GUI_CHECKBOX_CHECKBOX : GUIBaseMedia.GUI_CHECKBOX_RADIO;

            render_drawCMediaGFX(tex, UICommons.component_getAbsoluteX(checkBox), UICommons.component_getAbsoluteY(checkBox), checkBox.checked ? 1 : 0);

            render_drawFont(checkBox.font, checkBox.text, alpha, UICommons.component_getAbsoluteX(checkBox) + TILE_SIZE, UICommons.component_getAbsoluteY(checkBox), 1, 1);

        } else if (component.getClass() == GameViewPort.class) {
            GameViewPort gameViewPort = (GameViewPort) component;
            //inputState.spriteBatch_gui.setColor(1, 1, 1, 1f);
            inputState.spriteBatch_gui.draw(gameViewPort.textureRegion, UICommons.component_getAbsoluteX(gameViewPort), UICommons.component_getAbsoluteY(gameViewPort));
        }


        render_enableGrayScaleShader(disableShaderState);
        render_batchSetColorWhite(1f);
    }

    private void render_drawCursorListDrags() {
        if (inputState.inventoryDrag_Inventory != null) {
            Inventory dragInventory = inputState.inventoryDrag_Inventory;
            int dragOffsetX = inputState.inventoryDrag_offset.x;
            int dragOffsetY = inputState.inventoryDrag_offset.y;
            Object dragItem = inputState.inventoryDrag_Item;
            if (dragInventory != null && dragInventory.inventoryAction != null) {
                render_batchSetColorWhite(api.config.getDragAlpha());
                CMediaGFX icon = dragInventory.inventoryAction.icon(dragItem);
                render_drawCMediaGFX(icon, inputState.mouse_gui.x - dragOffsetX, inputState.mouse_gui.y - dragOffsetY, dragInventory.inventoryAction.iconArrayIndex(dragItem));
            }
        } else if (inputState.listDrag_List != null) {
            List dragList = inputState.listDrag_List;
            int dragOffsetX = inputState.listDrag_offset.x;
            int dragOffsetY = inputState.listDrag_offset.y;
            Object dragItem = inputState.listDrag_Item;
            if (dragList.listAction != null) {
                // List
                render_batchSetColor(dragList.color_r, dragList.color_g, dragList.color_b, Math.min(dragList.color_a, api.config.getDragAlpha()));
                for (int ix = 0; ix < dragList.width; ix++) {
                    this.render_drawCMediaGFX(GUIBaseMedia.GUI_LIST_SELECTED, inputState.mouse_gui.x - dragOffsetX + (ix * TILE_SIZE), inputState.mouse_gui.y - dragOffsetY);
                }
                // Text
                String text = dragList.listAction.text(dragItem);
                render_drawFont(dragList.font, text, dragList.color_a, inputState.mouse_gui.x - dragOffsetX, inputState.mouse_gui.y - dragOffsetY, 2, 1,
                        dragList.width * TILE_SIZE, dragList.listAction.icon(dragItem), dragList.listAction.iconArrayIndex(dragItem));
            }
        }


        render_batchSetColorWhite(1f);
    }


    private boolean render_GrayScaleShaderEnabled() {
        return inputState.spriteBatch_gui.getShader() == inputState.grayScaleShader;
    }

    private void render_enableGrayScaleShader(boolean enable) {
        inputState.spriteBatch_gui.setShader(enable ? inputState.grayScaleShader : null);
    }


    private void render_drawFont(CMediaFont font, String text, float alpha, int x, int y) {
        render_drawFont(font, text, alpha, x, y, 0, 0, FONT_MAXWIDTH_NONE, null, 0);
    }


    private void render_drawFont(CMediaFont font, String text, float alpha, int x, int y, int textXOffset, int textYOffset) {
        render_drawFont(font, text, alpha, x, y, textXOffset, textYOffset, FONT_MAXWIDTH_NONE, null, 0);
    }

    private void render_drawFont(CMediaFont font, String text, float alpha, int x, int y, int textXOffset, int textYOffset, int maxWidth) {
        render_drawFont(font, text, alpha, x, y, textXOffset, textYOffset, maxWidth, null, 0);
    }

    private void render_drawFont(CMediaFont font, String text, float alpha, int x, int y, int textXOffset, int textYOffset, int maxWidth, CMediaGFX icon, int iconIndex) {
        boolean withIcon = icon != null;
        if (withIcon) {
            render_batchSaveColor();
            render_batchSetColorWhite(alpha);
            render_drawCMediaGFX(icon, x, y, iconIndex);
            render_batchLoadColor();
        }

        render_fontSaveColor(font);
        render_fontSetAlpha(font, alpha);
        if (maxWidth == FONT_MAXWIDTH_NONE) {
            maxWidth -= ((withIcon ? TILE_SIZE : 0) + textXOffset);
            mediaManager.drawCMediaFont(inputState.spriteBatch_gui, font, x + (withIcon ? TILE_SIZE : 0) + textXOffset, y + textYOffset, text);
        } else {
            mediaManager.drawCMediaFont(inputState.spriteBatch_gui, font, x + (withIcon ? TILE_SIZE : 0) + textXOffset, y + textYOffset, text, maxWidth);
        }
        render_fontLoadColor(font);
    }

    private void render_fontSetAlpha(CMediaFont font, float a) {
        mediaManager.getCMediaFont(font).setColor(1, 1, 1, a);
    }

    private void render_fontSetColorWhite() {
        inputState.spriteBatch_gui.setColor(1, 1, 1, 1);
    }

    private Color render_fontGetColor(CMediaFont font) {
        return mediaManager.getCMediaFont(font).getColor();
    }


    private void render_batchSetColor(float r, float g, float b, float a) {
        inputState.spriteBatch_gui.setColor(r, g, b, a);
    }

    private void render_batchSetColorWhite(float alpha) {
        inputState.spriteBatch_gui.setColor(1, 1, 1, alpha);
    }

    private Color render_batchGetColor() {
        return inputState.spriteBatch_gui.getColor();
    }

    private void render_fontSaveColor(CMediaFont font) {
        BitmapFont bmpFont = mediaManager.getCMediaFont(font);
        Color color = bmpFont.getColor();
        render_colorStackPush(color.r, color.g, color.b, color.a);
    }

    private void render_fontLoadColor(CMediaFont font) {
        inputState.colorStackPointer--;
        mediaManager.getCMediaFont(font).setColor(inputState.colorStack[inputState.colorStackPointer]);
    }

    private void render_batchSaveColor() {
        Color color = inputState.spriteBatch_gui.getColor();
        render_colorStackPush(color.r, color.g, color.b, color.a);
    }

    private void render_batchLoadColor() {
        inputState.colorStackPointer--;
        inputState.spriteBatch_gui.setColor(inputState.colorStack[inputState.colorStackPointer]);
    }

    private void render_colorStackPush(float r, float g, float b, float a) {
        inputState.colorStack[inputState.colorStackPointer].r = r;
        inputState.colorStack[inputState.colorStackPointer].g = g;
        inputState.colorStack[inputState.colorStackPointer].b = b;
        inputState.colorStack[inputState.colorStackPointer].a = a;
        inputState.colorStackPointer++;
        if (inputState.colorStackPointer > inputState.colorStack.length)
            throw new RuntimeException("colorStackPointer overFlow");
    }

    private void render_drawCMediaGFX(CMediaGFX cMedia, int x, int y) {
        render_drawCMediaGFX(cMedia, x, y, 0, 0);
    }

    private void render_drawCMediaGFX(CMediaGFX cMedia, int x, int y, int arrayIndex) {
        render_drawCMediaGFX(cMedia, x, y, arrayIndex, 0);
    }

    private void render_drawCMediaGFX(CMediaGFX cMedia, int x, int y, int arrayIndex, float animation_timer_offset) {
        mediaManager.drawCMediaGFX(inputState.spriteBatch_gui, cMedia, x, y, arrayIndex, (inputState.animation_timer_gui + animation_timer_offset));
    }

    private void render_drawCMediaGFX(CMediaGFX cMedia, int x, int y, int arrayIndex, float animation_timer_offset, int area_x, int area_y, int area_w, int area_h) {
        mediaManager.drawCMediaGFX(inputState.spriteBatch_gui, cMedia, x, y, arrayIndex, (inputState.animation_timer_gui + animation_timer_offset));
    }

    public void shutdown() {
        // Lists
        inputState.windows.clear();

        inputState.modalWindowQueue.clear();
        inputState.hotKeys.clear();
        inputState.singleUpdateActions.clear();
        inputState.screenComponents.clear();
        inputState.notifications.clear();
        inputState.gameViewPorts.clear();

        // GFX
        inputState.spriteBatch_game.dispose();
        inputState.spriteBatch_gui.dispose();
        inputState.spriteBatch_screen.dispose();

        inputState.texture_game.getTexture().dispose();
        inputState.texture_gui.getTexture().dispose();
        if (inputState.viewportMode == ViewportMode.FIT || inputState.viewportMode == ViewportMode.STRETCH) {
            inputState.texture_screen.getTexture().dispose();
        }
        inputState.grayScaleShader.dispose();

        inputState = null;

        this.uiAdapter.shutdown();
    }

    public int getInternalResolutionWidth() {
        return inputState.internalResolutionWidth;
    }

    public int getInternalResolutionHeight() {
        return inputState.internalResolutionHeight;
    }

    public ViewportMode getViewportMode() {
        return inputState.viewportMode;
    }

    public int getViewPortScreenX() {
        return inputState.viewport_screen.getScreenX();
    }

    public int getViewPortScreenY() {
        return inputState.viewport_screen.getScreenY();
    }

    public int getViewPortScreenWidth() {
        return inputState.viewport_screen.getScreenWidth();
    }

    public int getViewPortScreenHeight() {
        return inputState.viewport_screen.getScreenHeight();
    }

    public boolean isGamePadSupport() {
        return inputState.gamePadSupport;
    }

    public TextureRegion getTextureScreen() {
        return inputState.texture_screen;
    }

    public TextureRegion getTextureGame() {
        return inputState.texture_game;
    }

    public TextureRegion getTextureGUI() {
        return inputState.texture_game;
    }

        /*private void calculateMagnetModeMouseMovements(){
        float deltaX = 0f;
        float deltaY = 0f;
        boolean magnetPossible = false;
        if (inputState.lastGUIMouseHover != null) {
            if (inputState.modalWindow != null) {
                if (inputState.lastGUIMouseHover.getClass() == Window.class && inputState.lastGUIMouseHover == inputState.modalWindow) {
                    magnetPossible = true;
                } else if (inputState.lastGUIMouseHover instanceof Component component) {
                    if (component.addedToWindow == inputState.modalWindow) magnetPossible = true;
                }
            } else {
                magnetPossible = true;
            }
        }
        if (magnetPossible) {
            boolean magnetActive = false;
            int magnet_x = 0, magnet_y = 0;
            if (inputState.lastGUIMouseHover.getClass() == Window.class) {
                Window window = (Window) inputState.lastGUIMouseHover;
                if (window.moveAble && window.hasTitleBar && window.visible) {
                    if (Tools.Calc.pointRectsCollide(inputState.mouse_gui.x, inputState.mouse_gui.y,
                            window.x, window.y + (window.height - 1) * UIEngine.TILE_SIZE,
                            window.width * UIEngine.TILE_SIZE,
                            UIEngine.TILE_SIZE)
                    ) {
                        magnet_x = inputState.mouse_gui.x;
                        magnet_y = (window.y + (window.height) * UIEngine.TILE_SIZE) - UIEngine.TILE_SIZE_2;
                        magnetActive = true;
                    }
                }
            } else if (inputState.lastGUIMouseHover.getClass() == ContextMenuItem.class) {
                ContextMenuItem contextMenuItem = (ContextMenuItem) inputState.lastGUIMouseHover;
                magnet_x = inputState.mouse_gui.x;
                magnet_y = contextMenuItem.addedToContextMenu.y - (((contextMenuItem.addedToContextMenu.y - inputState.mouse_gui.y) / UIEngine.TILE_SIZE) * UIEngine.TILE_SIZE) - UIEngine.TILE_SIZE_2;
                magnetActive = true;
            } else if (inputState.lastGUIMouseHover instanceof Component component) {
                if (!component.disabled && component.visible) {
                    if (inputState.lastGUIMouseHover.getClass() == List.class) {
                        List list = (List) inputState.lastGUIMouseHover;
                        magnet_x = inputState.mouse_gui.x;
                        magnet_y = UICommons.component_getAbsoluteY(list)
                                + (((inputState.mouse_gui.y - UICommons.component_getAbsoluteY(list)) / UIEngine.TILE_SIZE) * UIEngine.TILE_SIZE) + UIEngine.TILE_SIZE_2;
                        magnetActive = true;
                    }
                    if (inputState.lastGUIMouseHover.getClass() == Inventory.class) {
                        Inventory inventory = (Inventory) inputState.lastGUIMouseHover;
                        int cellSize = inventory.doubleSized ? UIEngine.TILE_SIZE * 2 : UIEngine.TILE_SIZE;
                        int cellSize2 = cellSize / 2;
                        magnet_x = UICommons.component_getAbsoluteX(inventory)
                                + (((inputState.mouse_gui.x - UICommons.component_getAbsoluteX(inventory)) / cellSize) * cellSize) + cellSize2;
                        magnet_y = UICommons.component_getAbsoluteY(inventory)
                                + (((inputState.mouse_gui.y - UICommons.component_getAbsoluteY(inventory)) / cellSize) * cellSize) + cellSize2;
                        magnetActive = true;
                    } else if (inputState.lastGUIMouseHover.getClass() == ScrollBarVertical.class) {
                        ScrollBarVertical scrollBarVertical = (ScrollBarVertical) inputState.lastGUIMouseHover;
                        magnet_x = UICommons.component_getAbsoluteX(scrollBarVertical) + UIEngine.TILE_SIZE_2;
                        magnet_y = inputState.mouse_gui.y;
                        magnetActive = true;
                    } else if (inputState.lastGUIMouseHover.getClass() == ScrollBarHorizontal.class) {
                        ScrollBarHorizontal scrollBarHorizontal = (ScrollBarHorizontal) inputState.lastGUIMouseHover;
                        magnet_x = inputState.mouse_gui.x;
                        magnet_y = UICommons.component_getAbsoluteY(scrollBarHorizontal) + UIEngine.TILE_SIZE_2;
                        magnetActive = true;
                    } else if (inputState.lastGUIMouseHover.getClass() == TextField.class) {
                        TextField textField = (TextField) inputState.lastGUIMouseHover;
                        magnet_x = inputState.mouse_gui.x;
                        magnet_y = UICommons.component_getAbsoluteY(textField) + UIEngine.TILE_SIZE_2;
                        magnetActive = true;
                    } else if (inputState.lastGUIMouseHover.getClass() == ComboBox.class) {
                        ComboBox comboBox = (ComboBox) inputState.lastGUIMouseHover;
                        if (!UICommons.comboBox_isOpen(comboBox, inputState)) {
                            magnet_x = inputState.mouse_gui.x;
                            magnet_y = UICommons.component_getAbsoluteY(comboBox) + UIEngine.TILE_SIZE_2;
                            magnetActive = true;
                        } else {
                            magnet_x = inputState.mouse_gui.x;
                            magnet_y = UICommons.component_getAbsoluteY(comboBox) - (((UICommons.component_getAbsoluteY(comboBox) - inputState.mouse_gui.y) / UIEngine.TILE_SIZE) * UIEngine.TILE_SIZE) - UIEngine.TILE_SIZE_2;
                            magnetActive = true;
                        }
                    } else if (inputState.lastGUIMouseHover.getClass() == CheckBox.class) {
                        CheckBox checkBox = (CheckBox) inputState.lastGUIMouseHover;
                        magnet_x = UICommons.component_getAbsoluteX(checkBox) + UIEngine.TILE_SIZE_2;
                        magnet_y = UICommons.component_getAbsoluteY(checkBox) + UIEngine.TILE_SIZE_2;
                        magnetActive = true;
                    } else if (inputState.lastGUIMouseHover.getClass() == TabBar.class) {
                        TabBar tabBar = (TabBar) inputState.lastGUIMouseHover;
                        int xTab = tabBar.tabOffset;
                        for (int i = 0; i < tabBar.tabs.size(); i++) {
                            Tab tab = tabBar.tabs.get(i);
                            if (Tools.Calc.pointRectsCollide(inputState.mouse_gui.x, inputState.mouse_gui.y,
                                    UICommons.component_getAbsoluteX(tabBar) + (xTab * UIEngine.TILE_SIZE),
                                    UICommons.component_getAbsoluteY(tabBar),
                                    tab.width * UIEngine.TILE_SIZE,
                                    tabBar.height * UIEngine.TILE_SIZE
                            )
                            ) {
                                magnet_x = UICommons.component_getAbsoluteX(tabBar) + (xTab * UIEngine.TILE_SIZE) + (tab.width * UIEngine.TILE_SIZE_2);
                                magnet_y = UICommons.component_getAbsoluteY(tabBar) + UIEngine.TILE_SIZE_2;
                                magnetActive = true;
                            }
                            xTab += tab.width;
                        }
                    } else if (inputState.lastGUIMouseHover instanceof Button button) {
                        magnet_x = UICommons.component_getAbsoluteX(button) + (button.width * UIEngine.TILE_SIZE_2);
                        magnet_y = UICommons.component_getAbsoluteY(button) + (button.height * UIEngine.TILE_SIZE_2);
                        magnetActive = true;
                    }
                }
            }
            // Move Cursor
            if (magnetActive) {
                if (inputState.mouse_gui.x < magnet_x) {
                    deltaX = (magnet_x - inputState.mouse_gui.x) / 4f;
                } else if (inputState.mouse_gui.x > magnet_x) {
                    deltaX = -(inputState.mouse_gui.x - magnet_x) / 4f;
                }
                if (inputState.mouse_gui.y < magnet_y) {
                    deltaY = -(magnet_y - inputState.mouse_gui.y) / 4f;
                } else if (inputState.mouse_gui.y > magnet_y) {
                    deltaY = (inputState.mouse_gui.y - magnet_y) / 4f;
                }
            }
        }
    }*/
}
