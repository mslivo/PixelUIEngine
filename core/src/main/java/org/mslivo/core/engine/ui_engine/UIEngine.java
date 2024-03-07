package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;
import org.mslivo.core.engine.media_manager.MediaManager;
import org.mslivo.core.engine.media_manager.media.CMediaArray;
import org.mslivo.core.engine.media_manager.media.CMediaFont;
import org.mslivo.core.engine.media_manager.media.CMediaGFX;
import org.mslivo.core.engine.media_manager.media.CMediaImage;
import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.ui_engine.config.Config;
import org.mslivo.core.engine.ui_engine.enums.MOUSE_CONTROL_MODE;
import org.mslivo.core.engine.ui_engine.enums.VIEWPORT_MODE;
import org.mslivo.core.engine.ui_engine.input.InputEvents;
import org.mslivo.core.engine.ui_engine.input.KeyCode;
import org.mslivo.core.engine.ui_engine.input.UIEngineInputProcessor;
import org.mslivo.core.engine.ui_engine.render.GrayScaleShader;
import org.mslivo.core.engine.ui_engine.render.NestedFrameBuffer;
import org.mslivo.core.engine.ui_engine.render.ShaderBatch;
import org.mslivo.core.engine.ui_engine.ui.Window;
import org.mslivo.core.engine.ui_engine.ui.actions.CommonActions;
import org.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;
import org.mslivo.core.engine.ui_engine.ui.components.button.Button;
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
import org.mslivo.core.engine.ui_engine.ui.components.progressbar.ProgressBar;
import org.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollBarHorizontal;
import org.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollBarVertical;
import org.mslivo.core.engine.ui_engine.ui.components.shape.Shape;
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
import org.mslivo.core.engine.ui_engine.ui.tool.MouseTool;
import org.mslivo.core.engine.ui_engine.ui.tooltip.ToolTip;
import org.mslivo.core.engine.ui_engine.ui.tooltip.ToolTipImage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * UI Engine
 * Handles UI Elements, Input, Cameras
 * Game needs to be implemented inside the uiAdapter
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class UIEngine<T extends UIAdapter> {

    // Attributes
    private final T uiAdapter;
    private InputState inputState;
    private final API api;
    private final MediaManager mediaManager;

    // Constants
    public static final int TILE_SIZE = 8;
    public static final float TILE_SIZE_F = TILE_SIZE;
    public static final int TILE_SIZE_2 = TILE_SIZE / 2;
    public static final float TILE_SIZE_F2 = TILE_SIZE / 2f;
    public static final String WND_CLOSE_BUTTON = "wnd_close_btn";
    private static final int FONT_MAXWIDTH_NONE = -1;

    public T getAdapter() {
        return uiAdapter;
    }

    public UIEngine(T uiAdapter, MediaManager mediaManager, int internalResolutionWidth, int internalResolutionHeight) {
        this(uiAdapter, mediaManager, internalResolutionWidth, internalResolutionHeight, VIEWPORT_MODE.PIXEL_PERFECT, true, true, true);
    }

    public UIEngine(T uiAdapter, MediaManager mediaManager, int internalResolutionWidth, int internalResolutionHeight, VIEWPORT_MODE viewportMode) {
        this(uiAdapter, mediaManager, internalResolutionWidth, internalResolutionHeight, viewportMode, true, true);
    }

    public UIEngine(T uiAdapter, MediaManager mediaManager, int internalResolutionWidth, int internalResolutionHeight, VIEWPORT_MODE viewportMode, boolean spriteRenderer, boolean immediateRenderer) {
        this(uiAdapter, mediaManager, internalResolutionWidth, internalResolutionHeight, viewportMode, spriteRenderer, immediateRenderer, true);
    }

    public UIEngine(T uiAdapter, MediaManager mediaManager, int internalResolutionWidth, int internalResolutionHeight, VIEWPORT_MODE viewportMode, boolean spriteRenderer, boolean immediateRenderer, boolean gamePadSupport) {
        if (uiAdapter == null || mediaManager == null) {
            throw new RuntimeException("Cannot initialize IREngine: invalid parameters");
        }
        this.uiAdapter = uiAdapter;
        this.mediaManager = mediaManager;
        /* Setup */
        this.inputState = initializeInputState(internalResolutionWidth, internalResolutionHeight, viewportMode, spriteRenderer, immediateRenderer, gamePadSupport);
        this.api = new API(this.inputState, mediaManager);
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        /*  Call Adapter Init */
        this.uiAdapter.init(this.api, this.mediaManager);
    }


    private InputState initializeInputState(int internalResolutionWidth, int internalResolutionHeight, VIEWPORT_MODE viewPortMode, boolean spriteRenderer, boolean shaderRenderer, boolean gamePadSupport) {
        InputState newInputState = new InputState();

        //  ----- Parameters
        newInputState.internalResolutionWidth = Tools.Calc.lowerBounds(internalResolutionWidth, TILE_SIZE * 2);
        newInputState.internalResolutionHeight = Tools.Calc.lowerBounds(internalResolutionHeight, TILE_SIZE * 2);
        newInputState.viewportMode = viewPortMode;
        newInputState.spriteRenderer = spriteRenderer;
        newInputState.shaderRenderer = shaderRenderer;
        newInputState.gamePadSupport = gamePadSupport;
        // ----- Config
        newInputState.config = new Config();
        // -----  Game
        if (spriteRenderer) {
            newInputState.spriteBatch_game = new SpriteBatch(8191);
            newInputState.spriteBatch_game.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            newInputState.spriteBatch_game = null;
        }
        if (shaderRenderer) {
            newInputState.shaderBatch_game = new ShaderBatch();
        } else {
            newInputState.shaderBatch_game = null;
        }
        newInputState.camera_game = new OrthographicCamera(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.camera_game.setToOrtho(false, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.camera_game.position.set(0, 0, 0);
        newInputState.camera_game.zoom = 1f;
        newInputState.frameBuffer_game = new NestedFrameBuffer(Pixmap.Format.RGB888, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight, false);
        newInputState.frameBuffer_game.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        newInputState.texture_game = new TextureRegion(newInputState.frameBuffer_game.getColorBufferTexture());
        newInputState.texture_game.flip(false, true);

        // -----  GUI
        newInputState.spriteBatch_ui = new SpriteBatch(8191);
        newInputState.spriteBatch_ui.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        if (shaderRenderer) {
            newInputState.shaderBatch_ui = new ShaderBatch();
        }
        newInputState.camera_ui = new OrthographicCamera(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.camera_ui.setToOrtho(false, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.frameBuffer_ui = new NestedFrameBuffer(Pixmap.Format.RGBA8888, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight, false);
        newInputState.frameBuffer_ui.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        newInputState.texture_ui = new TextureRegion(newInputState.frameBuffer_ui.getColorBufferTexture());
        newInputState.texture_ui.flip(false, true);
        // ----- UpScaler
        newInputState.upscaleFactor_screen = UICommons.viewport_determineUpscaleFactor(viewPortMode, internalResolutionWidth, internalResolutionHeight);
        newInputState.textureFilter_screen = UICommons.viewport_determineUpscaleTextureFilter(viewPortMode);
        newInputState.frameBuffer_screen = new NestedFrameBuffer(Pixmap.Format.RGBA8888, newInputState.internalResolutionWidth * newInputState.upscaleFactor_screen, newInputState.internalResolutionHeight * newInputState.upscaleFactor_screen, false);
        newInputState.frameBuffer_screen.getColorBufferTexture().setFilter(newInputState.textureFilter_screen, newInputState.textureFilter_screen);
        newInputState.texture_screen = new TextureRegion(newInputState.frameBuffer_screen.getColorBufferTexture());
        newInputState.texture_screen.flip(false, true);
        // ----- Screen
        newInputState.spriteBatch_screen = new SpriteBatch(1);
        newInputState.camera_screen = new OrthographicCamera(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.camera_screen.setToOrtho(false);
        newInputState.viewport_screen = UICommons.viewport_createViewport(viewPortMode, newInputState.camera_screen, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.viewport_screen.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // -----  GUI
        newInputState.windows = new ArrayList<>();
        newInputState.screenComponents = new ArrayList<>();
        newInputState.openContextMenu = null;
        newInputState.pressedContextMenuItem = null;
        newInputState.displayedContextMenuWidth = 0;
        newInputState.openMouseTextInput = null;
        newInputState.mTextInputMouse1Pressed = false;
        newInputState.mTextInputMouse2Pressed = false;
        newInputState.mTextInputMouse3Pressed = false;
        newInputState.mTextInputGamePadLeft = false;
        newInputState.mTextInputGamePadRight = false;
        newInputState.mTextInputScrollTimer = 0;
        newInputState.mTextInputScrollTime = 0;
        newInputState.mTextInputScrollSpeed = 0;
        newInputState.mTextInputTranslatedMouse1Down = false;
        newInputState.mTextInputTranslatedMouse2Down = false;
        newInputState.mTextInputUnlock = false;
        newInputState.mTextInputAPICharacterQueue = new IntArray();
        newInputState.keyboardInteractedUIObjectFrame = null;
        newInputState.mouseInteractedUIObjectFrame = null;
        newInputState.modalWindow = null;
        newInputState.modalWindowQueue = new ArrayDeque<>();
        newInputState.pressedTextField = null;
        newInputState.pressedTextFieldMouseX = 0;
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
        newInputState.turnedKnob = null;
        newInputState.tooltip = null;
        newInputState.tooltip_fadeIn_pct = 0f;
        newInputState.tooltip_wait_delay = false;
        newInputState.tooltip_delay_timer = 0;
        newInputState.tooltip_fadeIn_timer = 0;
        newInputState.scrolledScrollBarVertical = null;
        newInputState.scrolledScrollBarHorizontal = null;
        newInputState.draggedGridItem = null;
        newInputState.draggedGrid = null;
        newInputState.draggedGridOffset = new GridPoint2();
        newInputState.draggedGridFrom = new GridPoint2();
        newInputState.pressedGrid = null;
        newInputState.pressedGridItem = null;
        newInputState.draggedListItem = null;
        newInputState.draggedList = null;
        newInputState.draggedListOffsetX = new GridPoint2();
        newInputState.draggedListFromIndex = 0;
        newInputState.pressedList = null;
        newInputState.pressedListItem = null;
        newInputState.tooltip_lastHoverObject = null;
        newInputState.pressedCanvas = null;
        newInputState.openComboBox = null;
        newInputState.pressedComboBoxItem = null;
        newInputState.pressedCheckBox = null;
        // ----- Controls
        newInputState.currentControlMode = MOUSE_CONTROL_MODE.DISABLED;
        newInputState.mouse_ui = new GridPoint2(internalResolutionWidth / 2, internalResolutionHeight / 2);
        newInputState.mouse_game = new GridPoint2(0, 0);
        newInputState.mouse_delta = new Vector2(0, 0);
        newInputState.lastUIMouseHover = null;
        newInputState.cursor = null;
        newInputState.mouseTool = null;
        newInputState.mouseToolPressed = false;
        newInputState.vector_fboCursor = new Vector3(0, 0, 0);
        newInputState.vector2_unproject = new Vector2(0, 0);
        newInputState.mouse_emulated = new Vector2(internalResolutionWidth / 2f, internalResolutionHeight / 2f);
        newInputState.emulatedMouseLastMouseClick = 0;
        newInputState.keyBoardMouseSpeedUp = new Vector2(0, 0);
        newInputState.emulatedMouseIsButtonDown = new boolean[]{false, false, false, false, false};
        newInputState.keyBoardTranslatedKeysDown = new boolean[256];
        newInputState.gamePadTranslatedButtonsDown = new boolean[15];
        newInputState.gamePadTranslatedStickLeft = new Vector2(0, 0);
        newInputState.gamePadTranslatedStickRight = new Vector2(0, 0);

        // ---- Misc
        newInputState.animation_timer_ui = 0f;
        newInputState.tempSaveColor = new Color(Color.WHITE);
        ShaderProgram.pedantic = false;
        newInputState.grayScaleShader = new ShaderProgram(GrayScaleShader.VERTEX, GrayScaleShader.FRAGMENT);
        newInputState.camera_frustum = new OrthographicCamera(newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.camera_frustum.setToOrtho(false, newInputState.internalResolutionWidth, newInputState.internalResolutionHeight);
        newInputState.inputEvents = new InputEvents();
        newInputState.inputProcessor = new UIEngineInputProcessor(newInputState.inputEvents, newInputState.gamePadSupport);

        newInputState.itemInfo_listIndex = 0;
        newInputState.itemInfo_tabBarTabIndex = 0;
        newInputState.itemInfo_gridPos = new GridPoint2();
        newInputState.itemInfo_listValid = false;
        newInputState.itemInfo_tabBarValid = false;
        newInputState.itemInfo_gridValid = false;

        return newInputState;
    }

    public void resize(int width, int height) {
        inputState.viewport_screen.update(width, height, true);
    }

    public void update() {
        // UI
        this.updateMouseControl();
        this.updateUI(); // Main UI Updates happen here
        this.updateCameras();
        this.updateMouseCursor();

        // Update Game
        this.uiAdapter.update();

        // Reset Input Events
        this.inputState.inputEvents.reset();
    }

    private void updateMouseControl() {
        if (!inputState.config.input_gamePadMouseEnabled && !inputState.config.input_keyboardMouseEnabled && !inputState.config.input_hardwareMouseEnabled) {
            mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE.DISABLED);
            mouseControl_chokeAllMouseEvents();
        } else {
            if (inputState.config.input_gamePadMouseEnabled && mouseControl_gamePadMouseTranslateAndChokeEvents()) {
                mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE.GAMEPAD);
            } else if (inputState.config.input_keyboardMouseEnabled && mouseControl_keyboardMouseTranslateAndChokeEvents()) {
                mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE.KEYBOARD);
            } else if (inputState.config.input_hardwareMouseEnabled && mouseControl_hardwareMouseDetectUse()) {
                mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE.HARDWARE_MOUSE);
            }
        }

        if (inputState.openMouseTextInput != null) {
            // Translate to Text Input
            mouseControl_updateMouseTextInput();
        } else {
            // Translate to MouseGUI position
            switch (inputState.currentControlMode) {
                case GAMEPAD -> mouseControl_updateGamePadMouse();
                case KEYBOARD -> mouseControl_updateKeyBoardMouse();
                case HARDWARE_MOUSE -> mouseControl_updateHardwareMouse();
                case DISABLED -> {
                }
            }
        }

        mouseControl_enforceUIMouseBounds(); // Enforce UI mouse screen bounds
        mouseControl_updateGameMouseXY(); // Translate UI mouse x,y to Game mouse x,y
        mouseControl_updateLastUIMouseHover(); // Determine object that is below the cursor
    }

    private void mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE nextControlMode) {
        if (nextControlMode != null && nextControlMode != inputState.currentControlMode) {
            // Clean up current control mode
            if (inputState.currentControlMode.emulated) {
                if (inputState.currentControlMode == MOUSE_CONTROL_MODE.GAMEPAD) {
                    // Gamepad
                    for (int i = 0; i < inputState.gamePadTranslatedButtonsDown.length; i++)
                        inputState.gamePadTranslatedButtonsDown[i] = false;
                    inputState.gamePadTranslatedStickLeft.set(0f, 0f);
                    inputState.gamePadTranslatedStickRight.set(0f, 0f);
                }
                if (inputState.currentControlMode == MOUSE_CONTROL_MODE.KEYBOARD) {
                    // Keyboard
                    for (int i = 0; i < inputState.keyBoardTranslatedKeysDown.length; i++)
                        inputState.keyBoardTranslatedKeysDown[i] = false;
                    inputState.keyBoardMouseSpeedUp.set(0f, 0f);
                }
                // Simulated
                for (int i = 0; i <= 4; i++) inputState.emulatedMouseIsButtonDown[i] = false;
                inputState.emulatedMouseLastMouseClick = 0;
            }

            // Set Next ControlMode
            if (nextControlMode.emulated) {
                this.inputState.mouse_emulated.set(inputState.mouse_ui.x, inputState.mouse_ui.y);
            }
            inputState.currentControlMode = nextControlMode;
        }
    }


    private void mouseControl_updateMouseTextInput() {
        if (inputState.openMouseTextInput == null) return;
        MouseTextInput mouseTextInput = inputState.openMouseTextInput;
        char[] characters = mouseTextInput.upperCase ? mouseTextInput.charactersUC : mouseTextInput.charactersLC;

        int scrollDirection = 0;
        boolean mouse1Pressed = false;
        boolean mouse3Pressed = false;
        boolean mouse2Pressed = false;
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
                    // Choke Events & Translate
                    int indexOfLeft = inputState.inputEvents.mouseDownButtons.indexOf(KeyCode.Mouse.LEFT);
                    if (indexOfLeft != -1) {
                        inputState.inputEvents.mouseButtonsDown[KeyCode.Mouse.LEFT] = false;
                        inputState.inputEvents.mouseDownButtons.removeIndex(indexOfLeft);
                        inputState.mTextInputTranslatedMouse1Down = true;
                    }
                    int indexOfRight = inputState.inputEvents.mouseDownButtons.indexOf(KeyCode.Mouse.RIGHT);
                    if (indexOfRight != -1) {
                        inputState.inputEvents.mouseButtonsDown[KeyCode.Mouse.RIGHT] = false;
                        inputState.inputEvents.mouseDownButtons.removeIndex(indexOfRight);
                        inputState.mTextInputTranslatedMouse2Down = true;
                    }
                    int indexOfMiddle = inputState.inputEvents.mouseDownButtons.indexOf(KeyCode.Mouse.MIDDLE);
                    if (indexOfMiddle != -1) {
                        inputState.inputEvents.mouseButtonsDown[KeyCode.Mouse.MIDDLE] = false;
                        inputState.inputEvents.mouseDownButtons.removeIndex(indexOfMiddle);
                        inputState.mTextInputTranslatedMouse3Down = true;
                    }
                    inputState.inputEvents.mouseDown = inputState.inputEvents.mouseDownButtons.size > 0;
                }
                if (inputState.inputEvents.mouseUp) {
                    // Choke Events & Translate
                    int indexOfLeft = inputState.inputEvents.mouseUpButtons.indexOf(KeyCode.Mouse.LEFT);
                    if (indexOfLeft != -1) {
                        inputState.inputEvents.mouseUpButtons.removeIndex(indexOfLeft);
                        inputState.mTextInputTranslatedMouse1Down = false;
                    }
                    int indexOfRight = inputState.inputEvents.mouseUpButtons.indexOf(KeyCode.Mouse.RIGHT);
                    if (indexOfRight != -1) {
                        inputState.inputEvents.mouseUpButtons.removeIndex(indexOfRight);
                        inputState.mTextInputTranslatedMouse2Down = false;
                    }
                    int indexOfMiddle = inputState.inputEvents.mouseUpButtons.indexOf(KeyCode.Mouse.MIDDLE);
                    if (indexOfMiddle != -1) {
                        inputState.inputEvents.mouseUpButtons.removeIndex(indexOfMiddle);
                        inputState.mTextInputTranslatedMouse3Down = false;
                    }
                    inputState.inputEvents.mouseUp = inputState.inputEvents.mouseUpButtons.size > 0;
                }
                mouse1Pressed = inputState.mTextInputTranslatedMouse1Down;
                mouse2Pressed = inputState.mTextInputTranslatedMouse2Down;
                mouse3Pressed = inputState.mTextInputTranslatedMouse3Down;
            }
            case GAMEPAD -> {
                boolean stickLeft = inputState.config.input_gamePadMouseStickLeftEnabled;
                boolean stickRight = inputState.config.input_gamePadMouseStickRightEnabled;
                final float sensitivity = 0.4f;
                boolean leftGamePad = (stickLeft && inputState.gamePadTranslatedStickLeft.x < -sensitivity) || (stickRight && inputState.gamePadTranslatedStickRight.x < -sensitivity);
                boolean rightGamePad = (stickLeft && inputState.gamePadTranslatedStickLeft.x > sensitivity) || (stickRight && inputState.gamePadTranslatedStickRight.x > sensitivity);
                mouse1Pressed = mouseControl_isTranslatedKeyCodeDown(inputState.gamePadTranslatedButtonsDown, inputState.config.input_gamePadMouseButtonsMouse1);
                mouse2Pressed = mouseControl_isTranslatedKeyCodeDown(inputState.gamePadTranslatedButtonsDown, inputState.config.input_gamePadMouseButtonsMouse2);
                mouse3Pressed = mouseControl_isTranslatedKeyCodeDown(inputState.gamePadTranslatedButtonsDown, inputState.config.input_gamePadMouseButtonsMouse3);

                if (leftGamePad) {
                    if (!inputState.mTextInputGamePadLeft) {
                        scrollDirection = -1;
                        inputState.mTextInputGamePadLeft = true;
                    }
                } else {
                    inputState.mTextInputGamePadLeft = false;
                }
                if (rightGamePad) {
                    if (!inputState.mTextInputGamePadRight) {
                        scrollDirection = 1;
                        inputState.mTextInputGamePadRight = true;
                    }
                } else {
                    inputState.mTextInputGamePadRight = false;
                }

                // Continue Scroll
                if (leftGamePad || rightGamePad) {
                    inputState.mTextInputScrollTimer++;
                    if (inputState.mTextInputScrollTimer > inputState.mTextInputScrollTime) {
                        inputState.mTextInputGamePadLeft = false;
                        inputState.mTextInputGamePadRight = false;
                        inputState.mTextInputScrollTimer = 0;
                        inputState.mTextInputScrollSpeed++;
                        if (inputState.mTextInputScrollSpeed >= 3) {
                            inputState.mTextInputScrollTime = 2;
                        } else if (inputState.mTextInputScrollSpeed == 2) {
                            inputState.mTextInputScrollTime = 5;
                        } else if (inputState.mTextInputScrollSpeed == 1) {
                            inputState.mTextInputScrollTime = 10;
                        }
                    }
                } else {
                    inputState.mTextInputScrollTimer = 0;
                    inputState.mTextInputScrollTime = 20;
                    inputState.mTextInputScrollSpeed = 0;
                }
            }
            case KEYBOARD -> {
                // Not Needed since you are already using a keyboard to type
            }
        }

        // Unlock on first press
        if (!inputState.mTextInputUnlock) {
            if (mouse1Pressed) {
                mouse1Pressed = false;
            } else {
                inputState.mTextInputUnlock = true;
            }
        }

        // Scroll Forward/Backwards
        if (scrollDirection != 0) {
            mouseTextInput.selectedIndex = Tools.Calc.inBounds(mouseTextInput.selectedIndex + scrollDirection, 0, (characters.length - 1));
        }

        // Confirm Character from Input
        boolean enterRegularCharacter = false;
        boolean changeCaseMouse2 = false;
        boolean deleteCharacterMouse3 = false;
        if (mouse1Pressed && !inputState.mTextInputMouse1Pressed) inputState.mTextInputMouse1Pressed = true;
        if (!mouse1Pressed && inputState.mTextInputMouse1Pressed) {
            enterRegularCharacter = true;
            inputState.mTextInputMouse1Pressed = false;
        }

        if (mouse3Pressed && !inputState.mTextInputMouse2Pressed) inputState.mTextInputMouse2Pressed = true;
        if (!mouse3Pressed && inputState.mTextInputMouse2Pressed) {
            // Change case from Mouse 2
            changeCaseMouse2 = true;
            inputState.mTextInputMouse2Pressed = false;
        }

        if (mouse2Pressed && !inputState.mTextInputMouse3Pressed) inputState.mTextInputMouse3Pressed = true;
        if (!mouse2Pressed && inputState.mTextInputMouse3Pressed) {
            // Delete from Mouse 3
            deleteCharacterMouse3 = true;
            inputState.mTextInputMouse3Pressed = false;
        }

        // Confirm Character from API Queue
        if (!enterRegularCharacter && !inputState.mTextInputAPICharacterQueue.isEmpty()) {
            UICommons.mouseTextInput_selectCharacter(inputState.openMouseTextInput, (char) inputState.mTextInputAPICharacterQueue.removeIndex(inputState.mTextInputAPICharacterQueue.size - 1));
            enterRegularCharacter = true;
        }

        if (changeCaseMouse2 || deleteCharacterMouse3 || enterRegularCharacter) {
            char c;
            if (changeCaseMouse2) {
                c = '\t';
            } else if (deleteCharacterMouse3) {
                c = '\b';
            } else {
                c = characters[mouseTextInput.selectedIndex];
            }

            switch (c) {
                // Control ChangeCase
                case '\t' -> {
                    mouseTextInput.upperCase = !mouseTextInput.upperCase;
                    if (mouseTextInput.mouseTextInputAction != null)
                        mouseTextInput.mouseTextInputAction.onChangeCase(mouseTextInput.upperCase);
                }
                // Control Delete
                case '\b' -> {
                    inputState.inputEvents.keyDown = true;
                    inputState.inputEvents.keyDownKeyCodes.add(KeyCode.Key.BACKSPACE);
                    inputState.inputEvents.keyUp = true;
                    inputState.inputEvents.keyUpKeyCodes.add(KeyCode.Key.BACKSPACE);
                    if (mouseTextInput.mouseTextInputAction != null)
                        mouseTextInput.mouseTextInputAction.onDelete();
                }
                // Control Confirm
                case '\n' -> {
                    boolean close = mouseTextInput.mouseTextInputAction == null || mouseTextInput.mouseTextInputAction.onConfirm();
                    inputState.openMouseTextInput = close ? null : inputState.openMouseTextInput;
                }
                // Default Text Character
                default -> {
                    inputState.inputEvents.keyTyped = true;
                    inputState.inputEvents.keyTypedCharacters.add(c);
                    if (mouseTextInput.mouseTextInputAction != null)
                        mouseTextInput.mouseTextInputAction.onEnterCharacter(c);
                }
            }
        }
    }

    private void mouseControl_chokeAllMouseEvents() {
        // clear all mouse inputs
        inputState.inputEvents.mouseMoved = false;
        inputState.inputEvents.mouseDragged = false;
        inputState.inputEvents.mouseUp = false;
        inputState.inputEvents.mouseUpButtons.clear();
        inputState.inputEvents.mouseDown = false;
        inputState.inputEvents.mouseDownButtons.clear();
        inputState.inputEvents.mouseDoubleClick = false;
        Arrays.fill(inputState.inputEvents.mouseButtonsDown, false);
        inputState.mouse_ui.x = 0;
        inputState.mouse_ui.y = 0;
        inputState.mouse_delta.x = 0;
        inputState.mouse_delta.y = 0;
    }

    private boolean mouseControl_hardwareMouseDetectUse() {
        return inputState.inputEvents.mouseDown || inputState.inputEvents.mouseUp ||
                inputState.inputEvents.mouseMoved || inputState.inputEvents.mouseDragged || inputState.inputEvents.mouseScrolled;
    }

    private int[] mouseControl_keyboardMouseGetButtons(int index) {
        return switch (index) {
            case 0 -> inputState.config.input_keyboardMouseButtonsUp;
            case 1 -> inputState.config.input_keyboardMouseButtonsDown;
            case 2 -> inputState.config.input_keyboardMouseButtonsLeft;
            case 3 -> inputState.config.input_keyboardMouseButtonsRight;
            case 4 -> inputState.config.input_keyboardMouseButtonsMouse1;
            case 5 -> inputState.config.input_keyboardMouseButtonsMouse2;
            case 6 -> inputState.config.input_keyboardMouseButtonsMouse3;
            case 7 -> inputState.config.input_keyboardMouseButtonsMouse4;
            case 8 -> inputState.config.input_keyboardMouseButtonsMouse5;
            case 9 -> inputState.config.input_keyboardMouseButtonsScrollUp;
            case 10 -> inputState.config.input_keyboardMouseButtonsScrollDown;
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }


    private int[] mouseControl_gamePadMouseGetButtons(int index) {
        return switch (index) {
            case 0 -> inputState.config.input_gamePadMouseButtonsMouse1;
            case 1 -> inputState.config.input_gamePadMouseButtonsMouse2;
            case 2 -> inputState.config.input_gamePadMouseButtonsMouse3;
            case 3 -> inputState.config.input_gamePadMouseButtonsMouse4;
            case 4 -> inputState.config.input_gamePadMouseButtonsMouse5;
            case 5 -> inputState.config.input_gamePadMouseButtonsScrollUp;
            case 6 -> inputState.config.input_gamePadMouseButtonsScrollDown;
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }

    private boolean mouseControl_gamePadMouseTranslateAndChokeEvents() {
        // Remove Key down input events and set to temporary variable keyBoardTranslatedKeysDown
        boolean gamepadMouseUsed = false;
        for (int i = 0; i <= 6; i++) {
            int[] buttons = mouseControl_gamePadMouseGetButtons(i);
            if (buttons != null) {
                for (int i2 = 0; i2 < buttons.length; i2++) {
                    int keyCode = buttons[i2];
                    if (inputState.inputEvents.gamePadButtonDown) {
                        IntArray buttonDownKeyCodes = inputState.inputEvents.gamePadButtonDownKeyCodes;
                        for (int ikc = buttonDownKeyCodes.size - 1; ikc >= 0; ikc--) {
                            if (buttonDownKeyCodes.get(ikc) == keyCode) {
                                buttonDownKeyCodes.removeIndex(ikc);
                                inputState.inputEvents.gamePadButtonDown = !buttonDownKeyCodes.isEmpty();
                                inputState.inputEvents.gamePadButtonsDown[keyCode] = false;
                                inputState.gamePadTranslatedButtonsDown[keyCode] = true;
                                gamepadMouseUsed = true;
                            }
                        }
                    }
                    if (inputState.inputEvents.gamePadButtonUp) {
                        IntArray upKeyCodes = inputState.inputEvents.gamePadButtonUpKeyCodes;
                        for (int ikc = upKeyCodes.size - 1; ikc >= 0; ikc--) {
                            if (upKeyCodes.get(ikc) == keyCode) {
                                upKeyCodes.removeIndex(ikc);
                                inputState.inputEvents.gamePadButtonUp = !upKeyCodes.isEmpty();
                                inputState.gamePadTranslatedButtonsDown[keyCode] = false;
                                gamepadMouseUsed = true;
                            }
                        }
                    }
                }
            }
        }
        // Joystick Events Left
        if (inputState.config.input_gamePadMouseStickLeftEnabled) {
            if (inputState.inputEvents.gamePadLeftXMoved) {
                inputState.gamePadTranslatedStickLeft.x = inputState.inputEvents.gamePadLeftX;
                inputState.inputEvents.gamePadLeftX = 0;
                inputState.inputEvents.gamePadLeftXMoved = false;
                gamepadMouseUsed = true;
            }
            if (inputState.inputEvents.gamePadLeftYMoved) {
                inputState.gamePadTranslatedStickLeft.y = inputState.inputEvents.gamePadLeftY;
                inputState.inputEvents.gamePadLeftY = 0;
                inputState.inputEvents.gamePadLeftYMoved = false;
                gamepadMouseUsed = true;
            }
        } else {
            inputState.gamePadTranslatedStickLeft.x = 0;
            inputState.gamePadTranslatedStickLeft.y = 0;
        }
        // Joystick Events Right
        if (inputState.config.input_gamePadMouseStickRightEnabled) {
            if (inputState.inputEvents.gamePadRightXMoved) {
                inputState.gamePadTranslatedStickRight.x = inputState.inputEvents.gamePadRightX;
                inputState.inputEvents.gamePadRightX = 0;
                inputState.inputEvents.gamePadRightXMoved = false;
                gamepadMouseUsed = true;
            }
            if (inputState.inputEvents.gamePadRightYMoved) {
                inputState.gamePadTranslatedStickRight.y = inputState.inputEvents.gamePadRightY;
                inputState.inputEvents.gamePadRightY = 0;
                inputState.inputEvents.gamePadRightYMoved = false;
                gamepadMouseUsed = true;
            }
        } else {
            inputState.gamePadTranslatedStickRight.x = 0;
            inputState.gamePadTranslatedStickRight.y = 0;
        }

        return gamepadMouseUsed;
    }

    private boolean mouseControl_keyboardMouseTranslateAndChokeEvents() {
        if (inputState.focusedTextField != null) return false; // Disable during Textfield Input
        boolean keyboardMouseUsed = false;
        // Remove Key down input events and set to temporary variable keyBoardTranslatedKeysDown
        for (int i = 0; i <= 10; i++) {
            int[] buttons = mouseControl_keyboardMouseGetButtons(i);
            if (buttons != null) {
                if (inputState.inputEvents.keyDown) {
                    for (int i2 = 0; i2 < buttons.length; i2++) {
                        int keyCode = buttons[i2];
                        IntArray downKeyCodes = inputState.inputEvents.keyDownKeyCodes;
                        for (int ikc = downKeyCodes.size - 1; ikc >= 0; ikc--) {
                            int downKeyCode = downKeyCodes.get(ikc);
                            if (downKeyCode == keyCode) {
                                downKeyCodes.removeIndex(ikc);
                                inputState.inputEvents.keyDown = !downKeyCodes.isEmpty();
                                inputState.inputEvents.keysDown[keyCode] = false;
                                inputState.keyBoardTranslatedKeysDown[keyCode] = true;
                                keyboardMouseUsed = true;
                            }

                        }
                    }
                }
                if (inputState.inputEvents.keyUp) {
                    for (int i2 = 0; i2 < buttons.length; i2++) {
                        int keyCode = buttons[i2];
                        IntArray upKeyCodes = inputState.inputEvents.keyUpKeyCodes;
                        for (int ikc = upKeyCodes.size - 1; ikc >= 0; ikc--) {
                            int upKeyCode = upKeyCodes.get(ikc);
                            if (upKeyCode == keyCode) {
                                upKeyCodes.removeIndex(ikc);
                                inputState.inputEvents.keyUp = !upKeyCodes.isEmpty();
                                inputState.keyBoardTranslatedKeysDown[keyCode] = false;
                                keyboardMouseUsed = true;
                            }
                        }
                    }
                }
            }
        }
        return keyboardMouseUsed;
    }


    private void mouseControl_emulateMouseEvents(boolean buttonLeft, boolean buttonRight, boolean buttonUp, boolean buttonDown,
                                                 boolean buttonMouse1Down, boolean buttonMouse2Down, boolean buttonMouse3Down, boolean buttonMouse4Down, boolean buttonMouse5Down,
                                                 boolean buttonScrolledUp, boolean buttonScrolledDown, float cursorChangeX, float cursorChangeY
    ) {
        float deltaX = 0;
        float deltaY = 0;
        if (buttonLeft || buttonRight || buttonUp || buttonDown) {
            cursorChangeX *= inputState.config.input_emulatedMouseCursorSpeed;
            cursorChangeY *= inputState.config.input_emulatedMouseCursorSpeed;
            if (buttonLeft) deltaX -= cursorChangeX;
            if (buttonRight) deltaX += cursorChangeX;
            if (buttonUp) deltaY -= cursorChangeY;
            if (buttonDown) deltaY += cursorChangeY;
        }

        // Set to final
        inputState.mouse_emulated.x = Tools.Calc.inBounds(inputState.mouse_emulated.x + deltaX, 0, inputState.internalResolutionWidth);
        inputState.mouse_emulated.y = Tools.Calc.inBounds(inputState.mouse_emulated.y - deltaY, 0, inputState.internalResolutionHeight);
        inputState.mouse_delta.x = deltaX;
        inputState.mouse_delta.y = -deltaY;
        inputState.mouse_ui.x = MathUtils.round(inputState.mouse_emulated.x);
        inputState.mouse_ui.y = MathUtils.round(inputState.mouse_emulated.y);

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
            if (inputState.emulatedMouseIsButtonDown[i] != buttonMouseDown) {
                inputState.emulatedMouseIsButtonDown[i] = buttonMouseDown;
                if (inputState.emulatedMouseIsButtonDown[i]) {
                    inputState.inputEvents.mouseDown = true;
                    inputState.inputEvents.mouseDownButtons.add(i);
                    anyButtonChanged = true;
                    if (i == Input.Buttons.LEFT) {
                        // DoubleClick
                        if ((System.currentTimeMillis() - inputState.emulatedMouseLastMouseClick) < UIEngineInputProcessor.DOUBLE_CLICK_TIME) {
                            inputState.inputEvents.mouseDoubleClick = true;
                        }
                        inputState.emulatedMouseLastMouseClick = System.currentTimeMillis();
                    }

                } else {
                    inputState.inputEvents.mouseUp = true;
                    inputState.inputEvents.mouseUpButtons.add(i);
                    anyButtonChanged = true;
                }
            }
            inputState.inputEvents.mouseButtonsDown[i] = inputState.emulatedMouseIsButtonDown[i];
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
                if (inputState.emulatedMouseIsButtonDown[i]) {
                    inputState.inputEvents.mouseDragged = true;
                    inputState.inputEvents.mouseMoved = false;
                    break;
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

    private boolean mouseControl_isTranslatedKeyCodeDown(boolean[] translatedKeys, int[] keys) {
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                if (translatedKeys[keys[i]]) {
                    return true;
                }
            }
        }
        return false;
    }


    private void mouseControl_updateGamePadMouse() {

        // Swallow & Translate Gamepad Events
        boolean[] translatedButtons = inputState.gamePadTranslatedButtonsDown;
        boolean stickLeft = inputState.config.input_gamePadMouseStickLeftEnabled;
        boolean stickRight = inputState.config.input_gamePadMouseStickRightEnabled;

        float joystickDeadZone = inputState.config.input_gamePadMouseJoystickDeadZone;
        boolean buttonLeft = (stickLeft && inputState.gamePadTranslatedStickLeft.x < -joystickDeadZone) || (stickRight && inputState.gamePadTranslatedStickRight.x < -joystickDeadZone);
        boolean buttonRight = (stickLeft && inputState.gamePadTranslatedStickLeft.x > joystickDeadZone) || (stickRight && inputState.gamePadTranslatedStickRight.x > joystickDeadZone);
        boolean buttonUp = (stickLeft && inputState.gamePadTranslatedStickLeft.y > joystickDeadZone) || (stickRight && inputState.gamePadTranslatedStickRight.y > joystickDeadZone);
        boolean buttonDown = (stickLeft && inputState.gamePadTranslatedStickLeft.y < -joystickDeadZone) || (stickRight && inputState.gamePadTranslatedStickRight.y < -joystickDeadZone);
        boolean buttonMouse1Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, inputState.config.input_gamePadMouseButtonsMouse1);
        boolean buttonMouse2Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, inputState.config.input_gamePadMouseButtonsMouse2);
        boolean buttonMouse3Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, inputState.config.input_gamePadMouseButtonsMouse3);
        boolean buttonMouse4Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, inputState.config.input_gamePadMouseButtonsMouse4);
        boolean buttonMouse5Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, inputState.config.input_gamePadMouseButtonsMouse5);
        boolean buttonScrolledUp = mouseControl_isTranslatedKeyCodeDown(translatedButtons, inputState.config.input_gamePadMouseButtonsScrollUp);
        boolean buttonScrolledDown = mouseControl_isTranslatedKeyCodeDown(translatedButtons, inputState.config.input_gamePadMouseButtonsScrollDown);

        float cursorChangeX = 0f;
        if (buttonLeft || buttonRight) {
            cursorChangeX = Math.max(Math.abs(inputState.gamePadTranslatedStickLeft.x), Math.abs(inputState.gamePadTranslatedStickRight.x));
            cursorChangeX = (cursorChangeX - joystickDeadZone) / (1f - joystickDeadZone);
        }
        float cursorChangeY = 0f;
        if (buttonUp || buttonDown) {
            cursorChangeY = Math.max(Math.abs(inputState.gamePadTranslatedStickLeft.y), Math.abs(inputState.gamePadTranslatedStickRight.y));
            cursorChangeY = (cursorChangeY - joystickDeadZone) / (1f - joystickDeadZone);
        }
        // Translate to mouse events
        mouseControl_emulateMouseEvents(buttonLeft, buttonRight, buttonUp, buttonDown,
                buttonMouse1Down, buttonMouse2Down, buttonMouse3Down, buttonMouse4Down, buttonMouse5Down,
                buttonScrolledUp, buttonScrolledDown, cursorChangeX, cursorChangeY
        );
    }

    private void mouseControl_updateKeyBoardMouse() {
        if (inputState.focusedTextField != null) return; // Disable during Textfield Input

        // Swallow & Translate keyboard events
        boolean[] translatedKeys = inputState.keyBoardTranslatedKeysDown;

        boolean buttonLeft = mouseControl_isTranslatedKeyCodeDown(translatedKeys, inputState.config.input_keyboardMouseButtonsLeft);
        boolean buttonRight = mouseControl_isTranslatedKeyCodeDown(translatedKeys, inputState.config.input_keyboardMouseButtonsRight);
        boolean buttonUp = mouseControl_isTranslatedKeyCodeDown(translatedKeys, inputState.config.input_keyboardMouseButtonsUp);
        boolean buttonDown = mouseControl_isTranslatedKeyCodeDown(translatedKeys, inputState.config.input_keyboardMouseButtonsDown);
        boolean buttonMouse1Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, inputState.config.input_keyboardMouseButtonsMouse1);
        boolean buttonMouse2Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, inputState.config.input_keyboardMouseButtonsMouse2);
        boolean buttonMouse3Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, inputState.config.input_keyboardMouseButtonsMouse3);
        boolean buttonMouse4Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, inputState.config.input_keyboardMouseButtonsMouse4);
        boolean buttonMouse5Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, inputState.config.input_keyboardMouseButtonsMouse5);
        boolean buttonScrolledUp = mouseControl_isTranslatedKeyCodeDown(translatedKeys, inputState.config.input_keyboardMouseButtonsScrollUp);
        boolean buttonScrolledDown = mouseControl_isTranslatedKeyCodeDown(translatedKeys, inputState.config.input_keyboardMouseButtonsScrollDown);

        final float SPEEDUP_SPEED = 0.1f;
        if (buttonLeft || buttonRight) {
            inputState.keyBoardMouseSpeedUp.x = Tools.Calc.inBounds(inputState.keyBoardMouseSpeedUp.x < 1f ? inputState.keyBoardMouseSpeedUp.x + SPEEDUP_SPEED : inputState.keyBoardMouseSpeedUp.x, 0f, 1f);
        } else {
            inputState.keyBoardMouseSpeedUp.set(0, inputState.keyBoardMouseSpeedUp.y);
        }
        if (buttonUp || buttonDown) {
            inputState.keyBoardMouseSpeedUp.y = Tools.Calc.inBounds(inputState.keyBoardMouseSpeedUp.y < 1f ? inputState.keyBoardMouseSpeedUp.y + SPEEDUP_SPEED : inputState.keyBoardMouseSpeedUp.y, 0f, 1f);
        } else {
            inputState.keyBoardMouseSpeedUp.set(inputState.keyBoardMouseSpeedUp.x, 0);
        }

        // Translate to mouse events
        mouseControl_emulateMouseEvents(buttonLeft, buttonRight, buttonUp, buttonDown,
                buttonMouse1Down, buttonMouse2Down, buttonMouse3Down, buttonMouse4Down, buttonMouse5Down,
                buttonScrolledUp, buttonScrolledDown, inputState.keyBoardMouseSpeedUp.x, inputState.keyBoardMouseSpeedUp.y
        );
    }

    private void mouseControl_enforceUIMouseBounds() {
        if (inputState.mouse_ui.x < 0) inputState.mouse_ui.x = 0;
        if (inputState.mouse_ui.x > inputState.internalResolutionWidth)
            inputState.mouse_ui.x = inputState.internalResolutionWidth;
        if (inputState.mouse_ui.y < 0) inputState.mouse_ui.y = 0;
        if (inputState.mouse_ui.y > inputState.internalResolutionHeight)
            inputState.mouse_ui.y = inputState.internalResolutionHeight;
    }

    private void mouseControl_updateGameMouseXY() {
        // MouseXGUI/MouseYGUI -> To MouseX/MouseY
        inputState.vector_fboCursor.x = inputState.mouse_ui.x;
        inputState.vector_fboCursor.y = Gdx.graphics.getHeight() - inputState.mouse_ui.y;
        inputState.vector_fboCursor.z = 1;
        inputState.camera_game.unproject(inputState.vector_fboCursor, 0, 0, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
        this.inputState.mouse_game.x = (int) inputState.vector_fboCursor.x;
        this.inputState.mouse_game.y = (int) inputState.vector_fboCursor.y;
    }

    private void mouseControl_updateHardwareMouse() {
        // --- GUI CURSOR ---
        // ScreenCursor To WorldCursor
        inputState.vector2_unproject.x = Gdx.input.getX();
        inputState.vector2_unproject.y = Gdx.input.getY();

        inputState.viewport_screen.unproject(inputState.vector2_unproject);
        // WorldCursor to  FBOCursor
        inputState.vector_fboCursor.x = inputState.vector2_unproject.x;
        inputState.vector_fboCursor.y = Gdx.graphics.getHeight() - inputState.vector2_unproject.y;
        inputState.vector_fboCursor.z = 1;
        inputState.camera_ui.unproject(inputState.vector_fboCursor, 0, 0, inputState.internalResolutionWidth, inputState.internalResolutionHeight);

        // Set to final
        inputState.mouse_delta.x = MathUtils.round(inputState.vector_fboCursor.x - inputState.mouse_ui.x);
        inputState.mouse_delta.y = MathUtils.round(inputState.vector_fboCursor.y - inputState.mouse_ui.y);
        inputState.mouse_ui.x = Tools.Calc.inBounds(MathUtils.round(inputState.vector_fboCursor.x), 0, inputState.internalResolutionWidth);
        inputState.mouse_ui.y = Tools.Calc.inBounds(MathUtils.round(inputState.vector_fboCursor.y), 0, inputState.internalResolutionHeight);
    }

    private void mouseControl_updateLastUIMouseHover() {
        inputState.lastUIMouseHover = UICommons.component_getComponentAtPosition(inputState, inputState.mouse_ui.x, inputState.mouse_ui.y);
    }


    private void updateCameras() {
        // Game Camera
        inputState.camera_game.update();
        // Viewport Camera
        for (int i = 0; i < inputState.gameViewPorts.size(); i++) inputState.gameViewPorts.get(i).camera.update();
    }

    private void updateMouseCursor() {
        /* Update Cursor*/
        if (inputState.lastUIMouseHover != null) {
            // 1. GUI Cursor
            inputState.cursor = inputState.config.ui_cursor;
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

    private void updateUI_keyInteractions() {
        inputState.keyboardInteractedUIObjectFrame = null;
        if (inputState.config.ui_keyInteractionsDisabled) return;

        if (inputState.inputEvents.keyTyped) {
            if (inputState.focusedTextField != null) {
                TextField focusedTextField = inputState.focusedTextField; // Into Temp variable because focuseTextField can change after executing actions
                for (int ic = 0; ic < inputState.inputEvents.keyTypedCharacters.size; ic++) {
                    char keyTypedCharacter = (char) inputState.inputEvents.keyTypedCharacters.get(ic);
                    UICommons.textField_typeCharacter(mediaManager, focusedTextField, keyTypedCharacter);
                }
                // MouseTextInput open = focus on last typed character
                if (inputState.openMouseTextInput != null) {
                    char typedChar = (char) inputState.inputEvents.keyTypedCharacters.get(inputState.inputEvents.keyTypedCharacters.size - 1);
                    UICommons.mouseTextInput_selectCharacter(inputState.openMouseTextInput, typedChar);
                }
                inputState.keyboardInteractedUIObjectFrame = focusedTextField;
            }

        }
        if (inputState.inputEvents.keyDown) {
            if (inputState.focusedTextField != null) {
                // TextField Control Keys
                TextField focusedTextField = inputState.focusedTextField;
                for (int ik = 0; ik < inputState.inputEvents.keyDownKeyCodes.size; ik++) {
                    int keyDownKeyCode = inputState.inputEvents.keyDownKeyCodes.get(ik);
                    if (UICommons.textField_isControlKey(keyDownKeyCode)) {
                        // Repeat certain Control Keys
                        if (UICommons.textField_isRepeatedControlKey(keyDownKeyCode)) {
                            inputState.focusedTextField_repeatedKey = keyDownKeyCode;
                            inputState.focusedTextField_repeatedKeyTimer = System.currentTimeMillis();
                        }
                        UICommons.textField_executeControlKey(inputState, mediaManager, focusedTextField, keyDownKeyCode);
                    }
                    inputState.keyboardInteractedUIObjectFrame = focusedTextField;
                }
            } else {
                // Hotkeys
                for (int ihk = 0; ihk < inputState.hotKeys.size(); ihk++) {
                    HotKey hotKey = inputState.hotKeys.get(ihk);
                    if (!hotKey.pressed) {
                        boolean hotKeyPressed = true;
                        for (int ikc = 0; ikc < hotKey.keyCodes.length; ikc++) {
                            if (!inputState.inputEvents.keysDown[hotKey.keyCodes[ikc]]) {
                                hotKeyPressed = false;
                                break;
                            }
                        }
                        if (hotKeyPressed) UICommons.hotkey_press(hotKey);
                    }
                }
            }
        }
        if (inputState.inputEvents.keyUp) {
            for (int ik = 0; ik < inputState.inputEvents.keyUpKeyCodes.size; ik++) {
                int keyUpKeyCode = inputState.inputEvents.keyUpKeyCodes.get(ik);
                // Reset RepeatKey
                if (UICommons.textField_isRepeatedControlKey(keyUpKeyCode)) {
                    inputState.focusedTextField_repeatedKey = KeyCode.NONE;
                    inputState.focusedTextField_repeatedKeyTimer = 0;
                }
                // Reset Hotkeys
                for (int ihk = 0; ihk < inputState.hotKeys.size(); ihk++) {
                    HotKey hotKey = inputState.hotKeys.get(ihk);
                    if (hotKey.pressed) {
                        hkLoop:
                        for (int ikc = 0; ikc < hotKey.keyCodes.length; ikc++) {
                            if (hotKey.keyCodes[ikc] == keyUpKeyCode) {
                                hotKey.pressed = false;
                                if (hotKey.hotKeyAction != null) UICommons.hotkey_release(hotKey);
                                break hkLoop;
                            }
                        }
                    }
                }
            }
        }
    }


    private void updateUI_mouseInteractions() {
        inputState.mouseInteractedUIObjectFrame = null;
        if (inputState.config.ui_mouseInteractionsDisabled) return;
        // ------ MOUSE DOUBLE CLICK ------
        if (inputState.inputEvents.mouseDoubleClick) {
            boolean processMouseDoubleClick = true;
            if (inputState.lastUIMouseHover != null) {
                if (inputState.modalWindow != null && inputState.lastUIMouseHover != inputState.modalWindow) {
                    processMouseDoubleClick = false;
                }
            } else {
                processMouseDoubleClick = false;
            }

            if (processMouseDoubleClick) {
                if (inputState.lastUIMouseHover instanceof Window window) {
                    for (int ib = 0; ib < inputState.inputEvents.mouseDownButtons.size; ib++) {
                        int mouseDownButton = inputState.inputEvents.mouseDownButtons.get(ib);
                        if (inputState.config.ui_foldWindowsOnDoubleClick && mouseDownButton == Input.Buttons.LEFT) {
                            if (window.hasTitleBar && Tools.Calc.pointRectsCollide(inputState.mouse_ui.x, inputState.mouse_ui.y, window.x, window.y + ((window.height - 1) * TILE_SIZE), UICommons.window_getRealWidth(window), TILE_SIZE)) {
                                if (window.folded) {
                                    UICommons.window_unFold(window);
                                } else {
                                    UICommons.window_fold(window);
                                }
                            }
                        }
                    }
                }

                // Execute Common Actions
                for (int ib = 0; ib < inputState.inputEvents.mouseDownButtons.size; ib++) {
                    int mouseDownButton = inputState.inputEvents.mouseDownButtons.get(ib);
                    actions_executeOnMouseDoubleClickCommonAction(inputState.lastUIMouseHover, mouseDownButton);
                }

                inputState.mouseInteractedUIObjectFrame = inputState.lastUIMouseHover;
            } else {
                // Tool
                if (inputState.mouseTool != null && inputState.mouseTool.mouseToolAction != null) {
                    for (int ib = 0; ib < inputState.inputEvents.mouseDownButtons.size; ib++) {
                        int mouseDownButton = inputState.inputEvents.mouseDownButtons.get(ib);
                        inputState.mouseTool.mouseToolAction.onDoubleClick(mouseDownButton, inputState.mouse_game.x, inputState.mouse_game.y);
                    }

                }
            }
        }
        // ------ MOUSE DOWN ------
        if (inputState.inputEvents.mouseDown) {
            boolean processMouseClick = true;
            /* Modal ? */
            if (inputState.lastUIMouseHover != null) {
                if (inputState.modalWindow != null) {
                    /* Modal Active? */
                    if (inputState.lastUIMouseHover instanceof Window window) {
                        if (window != inputState.modalWindow) processMouseClick = false;
                    } else if (inputState.lastUIMouseHover instanceof Component component) {
                        if (component.addedToWindow == null) {
                            processMouseClick = false;
                        } else if (component.addedToWindow != inputState.modalWindow) {
                            processMouseClick = false;
                        }
                    }
                } else {
                    /* Hidden ? */
                    if (inputState.lastUIMouseHover instanceof Window window) {
                        if (!window.visible) processMouseClick = false;
                    } else if (inputState.lastUIMouseHover instanceof Component component) {
                        if (component.addedToWindow != null && !component.addedToWindow.visible)
                            processMouseClick = false;
                    }
                }
            } else {
                processMouseClick = false;
            }

            if (processMouseClick) {
                Window moveWindow = null;
                boolean isMouseLeftButton = inputState.inputEvents.mouseButtonsDown[Input.Buttons.LEFT];
                if (isMouseLeftButton) {
                    // Mouse Action
                    switch (inputState.lastUIMouseHover) {
                        case Window window -> {
                            if (window.moveAble) moveWindow = window;
                        }
                        case Button button -> {
                            inputState.pressedButton = button;
                            switch (button.mode){
                                case DEFAULT -> UICommons.button_release(button);
                                case TOGGLE -> UICommons.button_toggle(button);
                            }
                            UICommons.button_press(inputState.pressedButton);
                        }
                        case ContextMenuItem contextMenuItem -> {
                            inputState.pressedContextMenuItem = contextMenuItem;
                        }
                        case ScrollBarVertical scrollBarVertical -> {
                            UICommons.scrollBar_pressButton(scrollBarVertical);
                            UICommons.scrollBar_scroll(scrollBarVertical,
                                    UICommons.scrollBar_calculateScrolled(scrollBarVertical, inputState.mouse_ui.x, inputState.mouse_ui.y));
                            inputState.scrolledScrollBarVertical = scrollBarVertical;
                        }
                        case ScrollBarHorizontal scrollBarHorizontal -> {
                            UICommons.scrollBar_pressButton(scrollBarHorizontal);
                            UICommons.scrollBar_scroll(scrollBarHorizontal,
                                    UICommons.scrollBar_calculateScrolled(scrollBarHorizontal, inputState.mouse_ui.x, inputState.mouse_ui.y));
                            inputState.scrolledScrollBarHorizontal = scrollBarHorizontal;
                        }
                        case ComboBox comboBox -> {
                            if (UICommons.comboBox_isOpen(inputState, comboBox)) {
                                if (Tools.Calc.pointRectsCollide(inputState.mouse_ui.x, inputState.mouse_ui.y,
                                        UICommons.component_getAbsoluteX(comboBox), UICommons.component_getAbsoluteY(comboBox),
                                        (comboBox.width * TILE_SIZE), TILE_SIZE)) {
                                    // Clicked on Combobox itself -> close
                                    UICommons.comboBox_close(inputState, comboBox);
                                } else {
                                    // Clicked on Item
                                    for (int i = 0; i < comboBox.comboBoxItems.size(); i++) {
                                        if (Tools.Calc.pointRectsCollide(inputState.mouse_ui.x, inputState.mouse_ui.y,
                                                UICommons.component_getAbsoluteX(comboBox),
                                                UICommons.component_getAbsoluteY(comboBox) - (i * TILE_SIZE) - TILE_SIZE,
                                                comboBox.width * TILE_SIZE,
                                                TILE_SIZE
                                        )) {
                                            inputState.pressedComboBoxItem = comboBox.comboBoxItems.get(i);
                                        }
                                    }
                                }


                            } else {
                                // Open this combobox
                                UICommons.comboBox_open(inputState, comboBox);
                            }
                        }
                        case Knob knob -> {
                            inputState.turnedKnob = knob;
                            if (knob.knobAction != null) knob.knobAction.onPress();
                        }
                        case Canvas canvas -> {
                            if (canvas.canvasAction != null) canvas.canvasAction.onPress(
                                    UICommons.component_getRelativeMouseX(inputState.mouse_ui.x, canvas),
                                    UICommons.component_getRelativeMouseY(inputState.mouse_ui.y, canvas));
                            inputState.pressedCanvas = canvas;
                        }
                        case GameViewPort gameViewPort -> {
                            if (gameViewPort.gameViewPortAction != null) gameViewPort.gameViewPortAction.onPress(
                                    UICommons.component_getRelativeMouseX(inputState.mouse_ui.x, gameViewPort),
                                    UICommons.component_getRelativeMouseY(inputState.mouse_ui.y, gameViewPort));
                            inputState.pressedGameViewPort = gameViewPort;
                        }
                        case TextField textField -> {
                            inputState.pressedTextFieldMouseX = UICommons.component_getRelativeMouseX(inputState.mouse_ui.x, textField);
                            inputState.pressedTextField = textField;
                        }
                        case Grid grid -> {
                            int tileSize = grid.doubleSized ? TILE_SIZE * 2 : TILE_SIZE;
                            int x_grid = UICommons.component_getAbsoluteX(grid);
                            int y_grid = UICommons.component_getAbsoluteY(grid);
                            int inv_x = (inputState.mouse_ui.x - x_grid) / tileSize;
                            int inv_y = (inputState.mouse_ui.y - y_grid) / tileSize;
                            if (UICommons.grid_positionValid(grid, inv_x, inv_y)) {
                                Object pressedGridItem = grid.items[inv_x][inv_y];
                                if (pressedGridItem != null && grid.dragEnabled) {
                                    inputState.draggedGridFrom.x = inv_x;
                                    inputState.draggedGridFrom.y = inv_y;
                                    inputState.draggedGridOffset.x = inputState.mouse_ui.x - (x_grid + (inv_x * tileSize));
                                    inputState.draggedGridOffset.y = inputState.mouse_ui.y - (y_grid + (inv_y * tileSize));
                                    inputState.draggedGridItem = grid.items[inv_x][inv_y];
                                    inputState.draggedGrid = grid;
                                }
                                inputState.pressedGrid = grid;
                                inputState.pressedGridItem = pressedGridItem;
                            }
                        }
                        case List list -> {
                            UICommons.list_updateItemInfoAtMousePosition(inputState, list);
                            Object pressedListItem = null;
                            if (inputState.itemInfo_listValid) {
                                pressedListItem = (inputState.itemInfo_listIndex < list.items.size()) ? list.items.get(inputState.itemInfo_listIndex) : null;
                            }
                            if (pressedListItem != null && list.dragEnabled) {
                                inputState.draggedListFromIndex = inputState.itemInfo_listIndex;
                                inputState.draggedListOffsetX.x = inputState.mouse_ui.x - (UICommons.component_getAbsoluteX(list));
                                inputState.draggedListOffsetX.y = (inputState.mouse_ui.y - UICommons.component_getAbsoluteY(list)) % 8;
                                inputState.draggedListItem = pressedListItem;
                                inputState.draggedList = list;
                            }
                            inputState.pressedList = list;
                            inputState.pressedListItem = pressedListItem;
                        }
                        case TabBar tabBar -> {
                            UICommons.tabBar_updateItemInfoAtMousePosition(inputState, tabBar);
                            if (inputState.itemInfo_tabBarValid && tabBar.selectedTab != inputState.itemInfo_tabBarTabIndex) {
                                UICommons.tabBar_selectTab(tabBar, inputState.itemInfo_tabBarTabIndex);
                            }
                        }
                        case CheckBox checkBox -> {
                            inputState.pressedCheckBox = checkBox;
                        }
                        case null, default -> {
                        }
                    }

                    // Additonal Actions
                    // -> Bring clicked window to top
                    if (moveWindow != null) {
                        inputState.draggedWindow = moveWindow;
                        inputState.draggedWindow_offset.x = inputState.mouse_ui.x - inputState.draggedWindow.x;
                        inputState.draggedWindow_offset.y = inputState.mouse_ui.y - inputState.draggedWindow.y;
                        // Move on top ?
                        UICommons.window_bringToFront(inputState, inputState.draggedWindow);
                    }


                    // Unfocus focused textfields
                    if (inputState.focusedTextField != null && inputState.lastUIMouseHover != inputState.focusedTextField) {
                        UICommons.textField_unFocus(inputState, inputState.focusedTextField);
                    }
                }

                // Close opened ComboBoxes
                if (inputState.openComboBox != null && inputState.lastUIMouseHover != inputState.openComboBox) {
                    //if (!(inputState.lastGUIMouseHover instanceof ComboBoxItem comboBoxItem) || comboBoxItem.addedToComboBox != inputState.openComboBox) {
                    UICommons.comboBox_close(inputState, inputState.openComboBox);
                    //}
                }

                // Hide displayed ContextMenus
                if (inputState.openContextMenu != null) {
                    if (!(inputState.lastUIMouseHover instanceof ContextMenuItem contextMenuItem) || contextMenuItem.addedToContextMenu != inputState.openContextMenu) {
                        UICommons.contextMenu_close(inputState, inputState.openContextMenu);
                    }
                }

                // Execute Common Actions
                for (int ib = 0; ib < inputState.inputEvents.mouseDownButtons.size; ib++) {
                    int mouseDownButton = inputState.inputEvents.mouseDownButtons.get(ib);
                    actions_executeOnMouseClickCommonAction(inputState.lastUIMouseHover, mouseDownButton);
                }

                inputState.mouseInteractedUIObjectFrame = inputState.lastUIMouseHover;
            } else {
                // Tool
                if (inputState.mouseTool != null && inputState.mouseTool.mouseToolAction != null) {
                    for (int ib = 0; ib < inputState.inputEvents.mouseDownButtons.size; ib++) {
                        int mouseDownButton = inputState.inputEvents.mouseDownButtons.get(ib);
                        inputState.mouseTool.mouseToolAction.onPress(mouseDownButton, inputState.mouse_game.x, inputState.mouse_game.y);
                        inputState.mouseToolPressed = true;
                    }
                }
            }
        }
        // ------ MOUSE UP ------
        if (inputState.inputEvents.mouseUp) {
            // Drag Interaction
            Object draggedUIObject = UICommons.getDraggedUIReference(inputState);
            if (draggedUIObject != null) {
                switch (draggedUIObject) {
                    case List list -> {
                        int dragFromIndex = inputState.draggedListFromIndex;
                        Object dragItem = inputState.draggedListItem;
                        if (inputState.lastUIMouseHover != null) {
                            if (inputState.lastUIMouseHover instanceof List hoverList) {
                                if (UICommons.list_canDragIntoList(inputState, hoverList)) {
                                    UICommons.list_updateItemInfoAtMousePosition(inputState, hoverList);
                                    if (inputState.itemInfo_listValid) {
                                        int toIndex = inputState.itemInfo_listIndex;
                                        if (hoverList.listAction != null)
                                            hoverList.listAction.onDragFromList(list, dragFromIndex, toIndex);
                                    }
                                }
                            } else if (inputState.lastUIMouseHover instanceof Grid hoverGrid) {
                                if (UICommons.grid_canDragIntoGrid(inputState, hoverGrid)) {
                                    UICommons.grid_updateItemInfoAtMousePosition(inputState, hoverGrid);
                                    if (inputState.itemInfo_gridValid) {
                                        if (hoverGrid.gridAction != null)
                                            hoverGrid.gridAction.onDragFromList(list, dragFromIndex,
                                                    inputState.itemInfo_gridPos.x, inputState.itemInfo_gridPos.y);
                                    }
                                }
                            }
                        } else if (UICommons.list_canDragIntoScreen(list)) {
                            if (list.listAction != null) list.listAction.onDragIntoScreen(
                                    dragItem,
                                    dragFromIndex,
                                    inputState.mouse_ui.x,
                                    inputState.mouse_ui.y
                            );
                        }
                        inputState.draggedListOffsetX.x = inputState.draggedListOffsetX.y = 0;
                        inputState.draggedListFromIndex = 0;
                        inputState.draggedListItem = null;
                        inputState.draggedList = null;
                    }
                    case Grid grid -> {
                        int dragFromX = inputState.draggedGridFrom.x;
                        int dragFromY = inputState.draggedGridFrom.y;
                        Object dragItem = inputState.draggedGridItem;
                        if (inputState.lastUIMouseHover != null) {
                            if (inputState.lastUIMouseHover instanceof Grid hoverGrid) {
                                if (UICommons.grid_canDragIntoGrid(inputState, hoverGrid)) {
                                    UICommons.grid_updateItemInfoAtMousePosition(inputState, hoverGrid);
                                    if (inputState.itemInfo_gridValid) {
                                        if (hoverGrid.gridAction != null)
                                            hoverGrid.gridAction.onDragFromGrid(grid,
                                                    dragFromX, dragFromY,
                                                    inputState.itemInfo_gridPos.x, inputState.itemInfo_gridPos.y);
                                    }
                                }
                            } else if (inputState.lastUIMouseHover instanceof List hoverList) {
                                if (UICommons.list_canDragIntoList(inputState, hoverList)) {
                                    UICommons.list_updateItemInfoAtMousePosition(inputState, hoverList);
                                    if (inputState.itemInfo_listValid) {
                                        int toIndex = inputState.itemInfo_listIndex;
                                        if (hoverList.listAction != null)
                                            hoverList.listAction.onDragFromGrid(grid, dragFromX, dragFromY, toIndex);
                                    }
                                }
                            }
                        } else if (UICommons.grid_canDragIntoScreen(grid)) {
                            if (grid.gridAction != null)
                                grid.gridAction.onDragIntoScreen(
                                        dragItem,
                                        dragFromX, dragFromY,
                                        inputState.mouse_ui.x,
                                        inputState.mouse_ui.y
                                );
                        }
                        inputState.draggedGridOffset.x = inputState.draggedGridOffset.y = 0;
                        inputState.draggedGridFrom.x = inputState.draggedGridFrom.y = 0;
                        inputState.draggedGridItem = null;
                        inputState.draggedGrid = null;

                    }
                    case null, default -> {
                    }
                }

                inputState.mouseInteractedUIObjectFrame = draggedUIObject;
            }
            // Active UI Element Interaction
            Object usedUIObject = UICommons.getUsedUIReference(inputState);
            if (usedUIObject != null) {
                switch (usedUIObject) {
                    case Window window -> {
                        inputState.draggedWindow_offset.x = 0;
                        inputState.draggedWindow_offset.y = 0;
                        inputState.draggedWindow = null;
                    }
                    case Canvas canvas -> {
                        if (canvas.canvasAction != null) canvas.canvasAction.onRelease();
                        inputState.pressedCanvas = null;
                    }
                    case ContextMenuItem contextMenuItem -> {
                        UICommons.contextMenu_selectItem(inputState, contextMenuItem);
                        inputState.pressedContextMenuItem = null;
                    }
                    case CheckBox checkBox -> {
                        checkBox.checked = !checkBox.checked;
                        if (checkBox.checkBoxAction != null) checkBox.checkBoxAction.onCheck(checkBox.checked);
                        inputState.pressedCheckBox = null;
                    }
                    case ComboBoxItem comboBoxItem -> {
                        UICommons.comboBox_selectItem(inputState, comboBoxItem);
                        if (inputState.currentControlMode.emulated && comboBoxItem.addedToComboBox != null) {
                            // emulated: move mouse back to combobox on item select
                            inputState.mouse_emulated.y = UICommons.component_getAbsoluteY(comboBoxItem.addedToComboBox) + TILE_SIZE_2;
                        }
                        inputState.pressedComboBoxItem = null;
                    }
                    case TextField textField -> {
                        // Set Marker to mouse position
                        int mouseX = inputState.pressedTextFieldMouseX;
                        char[] fieldContent = textField.content.substring(textField.offset).toCharArray();
                        String testString = "";
                        boolean found = false;
                        charLoop:
                        for (int i = 0; i < fieldContent.length; i++) {
                            testString += fieldContent[i];
                            if (render_textWidth(textField.font, testString) > mouseX) {
                                UICommons.textField_setMarkerPosition(mediaManager, textField,
                                        textField.offset + i);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            // Set to end
                            UICommons.textField_setMarkerPosition(mediaManager, textField,
                                    textField.offset + fieldContent.length);
                        }
                        // Set Focus
                        UICommons.textField_focus(inputState, textField);
                        inputState.pressedTextField = null;
                    }
                    case GameViewPort gameViewPort -> {
                        if (gameViewPort.gameViewPortAction != null)
                            gameViewPort.gameViewPortAction.onRelease();
                        inputState.pressedGameViewPort = null;
                    }
                    case Button button -> {
                        UICommons.button_release(button);
                        inputState.pressedButton = null;
                    }
                    case ScrollBarVertical scrollBarVertical -> {
                        UICommons.scrollBar_releaseButton(scrollBarVertical);
                        inputState.scrolledScrollBarVertical = null;
                    }
                    case ScrollBarHorizontal scrollBarHorizontal -> {
                        UICommons.scrollBar_releaseButton(scrollBarHorizontal);
                        inputState.scrolledScrollBarHorizontal = null;
                    }
                    case Knob knob -> {
                        if (knob.knobAction != null) knob.knobAction.onRelease();
                        inputState.turnedKnob = null;
                    }
                    case Grid grid -> {
                        boolean isHoverObject = inputState.lastUIMouseHover == usedUIObject;
                        if (isHoverObject) {
                            if (inputState.pressedGridItem != null) {
                                inputState.pressedGrid.gridAction.onItemSelected(inputState.pressedGridItem);
                            } else {
                                inputState.pressedGrid.gridAction.onItemSelected(null);
                            }
                        }
                        inputState.pressedGrid = null;
                        inputState.pressedGridItem = null;
                    }
                    case List list -> {
                        boolean isHoverObject = inputState.lastUIMouseHover == usedUIObject;
                        if (isHoverObject) {
                            if (inputState.pressedListItem != null) {
                                if (list.multiSelect) {
                                    if (list.selectedItems.contains(inputState.pressedListItem)) {
                                        list.selectedItems.remove(inputState.pressedListItem);
                                    } else {
                                        list.selectedItems.add(inputState.pressedListItem);
                                    }
                                    if (list.listAction != null)
                                        list.listAction.onItemsSelected(list.selectedItems);
                                } else {
                                    list.selectedItem = inputState.pressedListItem;
                                    if (list.listAction != null)
                                        list.listAction.onItemSelected(list.selectedItem);
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
                        inputState.pressedListItem = null;
                        inputState.pressedList = null;
                    }
                    case null, default -> {
                    }
                }
                inputState.mouseInteractedUIObjectFrame = usedUIObject;
            }


            // MouseTool Interaction
            if (inputState.mouseToolPressed && inputState.mouseTool != null) {
                MouseTool pressedMouseTool = inputState.mouseTool;
                if (pressedMouseTool.mouseToolAction != null) {
                    for (int ib = 0; ib < inputState.inputEvents.mouseUpButtons.size; ib++) {
                        int mouseUpButton = inputState.inputEvents.mouseUpButtons.get(ib);
                        pressedMouseTool.mouseToolAction.onRelease(mouseUpButton, inputState.mouse_game.x, inputState.mouse_game.y);
                    }
                }
                inputState.mouseToolPressed = false;
            }
        }
        // ------ MOUSE DRAGGED ------
        if (inputState.inputEvents.mouseDragged) {
            // Active UI Element Interaction
            Object usedUIObject = UICommons.getUsedUIReference(inputState);
            switch (usedUIObject) {
                case Window draggedWindow -> {
                    UICommons.window_setPosition(inputState, draggedWindow,
                            inputState.mouse_ui.x - inputState.draggedWindow_offset.x,
                            inputState.mouse_ui.y - inputState.draggedWindow_offset.y);

                    if (draggedWindow.windowAction != null)
                        draggedWindow.windowAction.onMove(draggedWindow.x, draggedWindow.y);
                }
                case ScrollBarVertical scrolledScrollBarVertical -> {
                    UICommons.scrollBar_scroll(scrolledScrollBarVertical, UICommons.scrollBar_calculateScrolled(scrolledScrollBarVertical, inputState.mouse_ui.x, inputState.mouse_ui.y));
                }
                case ScrollBarHorizontal scrolledScrollBarHorizontal -> {
                    UICommons.scrollBar_scroll(scrolledScrollBarHorizontal, UICommons.scrollBar_calculateScrolled(scrolledScrollBarHorizontal, inputState.mouse_ui.x, inputState.mouse_ui.y));
                }
                case Knob turnedKnob -> {
                    float amount = (inputState.mouse_delta.y / 100f) * inputState.config.component_knobSensitivity;
                    float newValue = turnedKnob.turned + amount;
                    UICommons.knob_turnKnob(turnedKnob, newValue);
                    if (inputState.currentControlMode.emulated) {
                        // emulated: keep mouse position steady
                        UICommons.emulatedMouse_setPositionComponent(inputState, turnedKnob);
                    }
                }
                case null, default -> {
                }
            }

            // Mouse Tool Interaction
            if (inputState.mouseToolPressed && inputState.mouseTool != null) {
                MouseTool draggedMouseTool = inputState.mouseTool;
                if (draggedMouseTool.mouseToolAction != null)
                    draggedMouseTool.mouseToolAction.onDrag(inputState.mouse_game.x, inputState.mouse_game.y);
            }

        }
        // ------ MOUSE MOVED ------
        if (inputState.inputEvents.mouseMoved) {
            // Mouse Tool Interaction
            if (inputState.mouseTool != null) {
                MouseTool movedMouseTool = inputState.mouseTool;
                if (movedMouseTool.mouseToolAction != null)
                    movedMouseTool.mouseToolAction.onMove(inputState.mouse_game.x, inputState.mouse_game.y);
            }
        }
        // ------ MOUSE SCROLLED ------
        if (inputState.inputEvents.mouseScrolled) {
            if (inputState.lastUIMouseHover != null) {
                switch (inputState.lastUIMouseHover) {
                    case List list -> {
                        int size = list.items != null ? list.items.size() : 0;
                        float amount = (1 / (float) Tools.Calc.lowerBounds(size, 1)) * inputState.inputEvents.mouseScrolledAmount;
                        UICommons.list_scroll(list, list.scrolled + amount);
                    }
                    case Knob knob -> {
                        float amount = ((-1 / 20f) * inputState.inputEvents.mouseScrolledAmount) * inputState.config.component_knobSensitivity;
                        float newValue = knob.turned + amount;
                        UICommons.knob_turnKnob(knob, newValue);
                    }
                    case ScrollBarHorizontal scrollBarHorizontal -> {
                        float amount = ((-1 / 20f) * inputState.inputEvents.mouseScrolledAmount) * inputState.config.component_scrollbarSensitivity;
                        UICommons.scrollBar_scroll(scrollBarHorizontal, scrollBarHorizontal.scrolled + amount);
                    }
                    case ScrollBarVertical scrollBarVertical -> {
                        float amount = ((-1 / 20f) * inputState.inputEvents.mouseScrolledAmount) * inputState.config.component_scrollbarSensitivity;
                        UICommons.scrollBar_scroll(scrollBarVertical, scrollBarVertical.scrolled + amount);
                    }
                    case null, default -> {
                    }
                }

                // Execute Common Actions
                actions_executeOnMouseScrollCommonAction(inputState.lastUIMouseHover, inputState.inputEvents.mouseScrolledAmount);

                inputState.mouseInteractedUIObjectFrame = inputState.lastUIMouseHover;
            }
        }
    }

    private void updateUI_continuousComponentActivities() {
        // TextField Repeat
        if (inputState.focusedTextField_repeatedKey != KeyCode.NONE) {
            long time = (System.currentTimeMillis() - inputState.focusedTextField_repeatedKeyTimer);
            if (time > 500) {
                UICommons.textField_executeControlKey(inputState, mediaManager, inputState.focusedTextField, inputState.focusedTextField_repeatedKey);
            }
        }
    }

    private void updateUI() {

        updateUI_mouseInteractions();

        updateUI_keyInteractions();

        updateUI_continuousComponentActivities();

        updateUI_executeUpdateActions();

        updateUI_notifications();

        updateUI_toolTip();

    }

    private void updateUI_executeUpdateActions() {
        // for(int i) is used to avoid iterator creation and avoid concurrentModification
        // If UpdateActions are removing/adding other update actions they are caught on the next update/frame

        // ScreenComponent UpdateActions
        long currentTimeMillis = System.currentTimeMillis();
        for (int i = 0; i < inputState.screenComponents.size(); i++) {
            Component component = inputState.screenComponents.get(i);
            for (int i2 = 0; i2 < component.updateActions.size(); i2++) {
                actions_executeUpdateAction(component.updateActions.get(i2), currentTimeMillis);
            }
        }
        for (int i = 0; i < inputState.windows.size(); i++) {
            // Window UpdateActions
            Window window = inputState.windows.get(i);
            for (int i2 = 0; i2 < window.updateActions.size(); i2++) {
                actions_executeUpdateAction(window.updateActions.get(i2), currentTimeMillis);
            }
            // Window Component UpdateActions
            for (int i2 = 0; i2 < window.components.size(); i2++) {
                Component component = window.components.get(i2);
                for (int i3 = 0; i3 < component.updateActions.size(); i3++) {
                    actions_executeUpdateAction(component.updateActions.get(i3), currentTimeMillis);
                }
            }
        }

        // Engine SingleUpdateActions
        for (int i = 0; i < inputState.singleUpdateActions.size(); i++) {
            UpdateAction updateAction = inputState.singleUpdateActions.get(i);
            if (this.actions_executeUpdateAction(updateAction, currentTimeMillis)) {
                inputState.singleUpdateActionsRemoveQ.push(updateAction);
            }
        }
        UpdateAction removeUpdateAction;
        while ((removeUpdateAction = inputState.singleUpdateActionsRemoveQ.pollFirst()) != null) {
            inputState.singleUpdateActions.remove(removeUpdateAction);
        }
    }

    private void updateUI_toolTip() {
        // Anything dragged ?
        boolean showComponentToolTip = inputState.draggedList == null && inputState.draggedGrid == null;

        // hovering over a component ?
        if (showComponentToolTip) {
            showComponentToolTip = (inputState.lastUIMouseHover instanceof Component);
        }
        // modal active and component does not belong to modal ?
        if (showComponentToolTip) {
            showComponentToolTip = inputState.modalWindow == null || ((Component) inputState.lastUIMouseHover).addedToWindow == inputState.modalWindow;
        }

        if (showComponentToolTip) {
            Component hoverComponent = (Component) inputState.lastUIMouseHover;
            Object toolTipSubItem = null;
            if (hoverComponent instanceof List list) {
                if (list.listAction != null) {
                    UICommons.list_updateItemInfoAtMousePosition(inputState, list);
                    if (inputState.itemInfo_listValid) {
                        toolTipSubItem = inputState.itemInfo_listIndex < list.items.size() ? list.items.get(inputState.itemInfo_listIndex) : null;
                    }
                }
            } else if (hoverComponent instanceof Grid grid) {
                int tileSize = grid.doubleSized ? TILE_SIZE * 2 : TILE_SIZE;
                if (grid.gridAction != null) {
                    int x_grid = UICommons.component_getAbsoluteX(grid);
                    int y_grid = UICommons.component_getAbsoluteY(grid);
                    int inv_x = (inputState.mouse_ui.x - x_grid) / tileSize;
                    int inv_y = (inputState.mouse_ui.y - y_grid) / tileSize;
                    if (UICommons.grid_positionValid(grid, inv_x, inv_y)) {
                        toolTipSubItem = grid.items[inv_x][inv_y];
                    }
                }
            }

            boolean updateComponentToolTip;
            if (hoverComponent.updateToolTip) {
                updateComponentToolTip = true;
                hoverComponent.updateToolTip = false;
            } else {
                if (hoverComponent instanceof List || hoverComponent instanceof Grid) {
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
                if (hoverComponent instanceof List list && toolTipSubItem != null) {
                    // check for list item tooltips
                    inputState.tooltip = list.listAction.toolTip(toolTipSubItem);
                    inputState.tooltip_lastHoverObject = toolTipSubItem;
                } else if (hoverComponent instanceof Grid grid && toolTipSubItem != null) {
                    // check for Grid item tooltip
                    inputState.tooltip = grid.gridAction.toolTip(toolTipSubItem);
                    inputState.tooltip_lastHoverObject = toolTipSubItem;
                } else {
                    // take component tooltip
                    inputState.tooltip = hoverComponent.toolTip;
                    inputState.tooltip_lastHoverObject = hoverComponent;
                }
            }
        } else {
            // Set Game Tooltip
            if (inputState.lastUIMouseHover == null && inputState.gameToolTip != null) {
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
                if ((System.currentTimeMillis() - inputState.tooltip_delay_timer) > inputState.config.tooltip_FadeInDelayTime) {
                    inputState.tooltip_wait_delay = false;
                    inputState.tooltip_fadeIn_pct = 0f;
                    inputState.tooltip_fadeIn_timer = System.currentTimeMillis();
                    if (inputState.tooltip.toolTipAction != null) {
                        inputState.tooltip.toolTipAction.onDisplay();
                    }
                }
            } else if (inputState.tooltip_fadeIn_pct < 1f) {
                inputState.tooltip_fadeIn_pct = Tools.Calc.upperBounds(((System.currentTimeMillis() - inputState.tooltip_fadeIn_timer) / (float) inputState.config.tooltip_FadeInTime), 1f);
            } else {
                if (inputState.tooltip.toolTipAction != null) {
                    inputState.tooltip.toolTipAction.onUpdate();
                }
            }
        }
    }


    private void updateUI_notifications() {
        if (inputState.notifications.size() > 0) {
            Notification notification = inputState.notifications.getFirst();
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
                        notification.scroll += MathUtils.round(inputState.config.notification_scrollSpeed);
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
                    if ((System.currentTimeMillis() - notification.timer > inputState.config.notification_fadeoutTime)) {
                        UICommons.notification_removeFromScreen(inputState, notification);
                    }
                }
            }
        }
    }


    private void actions_executeOnMouseClickCommonAction(Object uiObject, int button) {
        if (uiObject == null) return;
        CommonActions commonActions = actions_getUIObjectCommonActions(uiObject);
        if (commonActions != null) commonActions.onMouseClick(button);
        if (uiObject instanceof Component component) {
            actions_executeOnMouseClickCommonAction(component.addedToWindow, button);
        }
    }


    private void actions_executeOnMouseDoubleClickCommonAction(Object uiObject, int button) {
        if (uiObject == null) return;
        CommonActions commonActions = actions_getUIObjectCommonActions(uiObject);
        if (commonActions != null) commonActions.onMouseDoubleClick(button);
        if (uiObject instanceof Component component) {
            actions_executeOnMouseDoubleClickCommonAction(component.addedToWindow, button);
        }
    }

    private void actions_executeOnMouseScrollCommonAction(Object uiObject, float scrolled) {
        if (uiObject == null) return;
        CommonActions commonActions = actions_getUIObjectCommonActions(uiObject);
        if (commonActions != null) commonActions.onMouseScroll(scrolled);
        if (uiObject instanceof Component component) {
            actions_executeOnMouseScrollCommonAction(component.addedToWindow, scrolled);
        }
    }

    private CommonActions actions_getUIObjectCommonActions(Object uiObject) {
        return switch (uiObject) {
            case Window window -> window.windowAction;
            case Notification notification -> notification.notificationAction;
            case Button button -> button.buttonAction;
            case ComboBox comboBox -> comboBox.comboBoxAction;
            case GameViewPort gameViewPort -> gameViewPort.gameViewPortAction;
            case Image image -> image.imageAction;
            case Grid grid -> grid.gridAction;
            case List list -> list.listAction;
            case Canvas canvas -> canvas.canvasAction;
            case ScrollBarVertical scrollBarVertical -> scrollBarVertical.scrollBarAction;
            case ScrollBarHorizontal scrollBarHorizontal -> scrollBarHorizontal.scrollBarAction;
            case TabBar tabBar -> tabBar.tabBarAction;
            case Text text -> text.textAction;
            case TextField textField -> textField.textFieldAction;
            case null, default -> null;
        };
    }

    private boolean actions_executeUpdateAction(UpdateAction updateAction, long currentTimeMillis) {
        if ((currentTimeMillis - updateAction.lastUpdate) > updateAction.interval) {
            updateAction.onUpdate();
            updateAction.lastUpdate = currentTimeMillis;
            return true;
        }
        return false;
    }

    private void render_setUIProjectionMatrix(OrthographicCamera camera){
        if (inputState.spriteRenderer)
            inputState.spriteBatch_ui.setProjectionMatrix(camera.combined);
        if (inputState.shaderRenderer)
            inputState.shaderBatch_ui.setProjectionMatrix(camera.combined);
    }

    private void render_setGameProjectionMatrix(OrthographicCamera camera){
        if (inputState.spriteRenderer)
            inputState.spriteBatch_game.setProjectionMatrix(camera.combined);
        if (inputState.shaderRenderer)
            inputState.shaderBatch_game.setProjectionMatrix(camera.combined);
    }
    public void render() {
        render(true);
    }

    public void render(boolean drawToScreen) {

        // Draw Game
        {
            // Draw Main FrameBuffer
            inputState.frameBuffer_game.begin();
            render_setGameProjectionMatrix(inputState.camera_game);
            this.uiAdapter.render(inputState.spriteBatch_game, inputState.shaderBatch_game, null);
            inputState.frameBuffer_game.end();
            // Draw GUI GameViewPort FrameBuffers
            for (int i = 0; i < this.inputState.gameViewPorts.size(); i++) {
                renderGameViewPortFrameBuffer(inputState.gameViewPorts.get(i));
            }
        }


        { // Draw GUI
            inputState.frameBuffer_ui.begin();
            render_setUIProjectionMatrix(inputState.camera_ui);
            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            this.uiAdapter.renderBeforeUI(inputState.spriteBatch_ui, inputState.shaderBatch_ui);
            this.renderUI();
            this.uiAdapter.renderAfterUI(inputState.spriteBatch_ui, inputState.shaderBatch_ui);
            inputState.frameBuffer_ui.end();
        }

        { // Draw to Screen Buffer, Combine GUI+Game Buffer and Upscale
            inputState.frameBuffer_screen.begin();
            inputState.spriteBatch_screen.setProjectionMatrix(inputState.camera_screen.combined);
            this.uiAdapter.renderFinalScreen(inputState.spriteBatch_screen,
                    inputState.texture_game, inputState.texture_ui,
                    inputState.internalResolutionWidth, inputState.internalResolutionHeight
            );
            inputState.frameBuffer_screen.end();
        }

        {

            // Draw to Screen
            if (drawToScreen) {
                inputState.viewport_screen.apply();
                inputState.spriteBatch_screen.begin();
                inputState.spriteBatch_screen.draw(inputState.texture_screen, 0, 0, inputState.internalResolutionWidth, inputState.internalResolutionHeight);
                inputState.spriteBatch_screen.end();
            }
        }


    }


    private void renderGameViewPortFrameBuffer(GameViewPort gameViewPort) {
        if (render_isComponentNotRendered(gameViewPort)) return;
        if (System.currentTimeMillis() - gameViewPort.updateTimer > gameViewPort.updateTime) {
            // draw to frambuffer
            gameViewPort.frameBuffer.begin();
            render_setGameProjectionMatrix(gameViewPort.camera);
            this.uiAdapter.render(inputState.spriteBatch_game, inputState.shaderBatch_game, gameViewPort);
            gameViewPort.frameBuffer.end();
            gameViewPort.updateTimer = System.currentTimeMillis();
        }
    }


    private void renderUI() {
        inputState.animation_timer_ui = inputState.animation_timer_ui + Gdx.graphics.getDeltaTime();

        inputState.spriteBatch_ui.begin();
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
        render_mouseTextInput();

        /* Cursor */
        render_drawCursorListDrags();

        render_drawCursor();

        inputState.spriteBatch_ui.end();
    }


    private void render_mouseTextInput() {
        if (inputState.openMouseTextInput == null) return;
        MouseTextInput mouseTextInput = inputState.openMouseTextInput;
        render_batchSetColorWhite(mouseTextInput.alpha);
        final int CHARACTERS = 4;
        char[] chars = mouseTextInput.upperCase ? mouseTextInput.charactersUC : mouseTextInput.charactersLC;

        // 4 to the left
        for (int i = 1; i <= CHARACTERS; i++) {
            int index = mouseTextInput.selectedIndex - i;
            if (index >= 0 && index < chars.length) {
                render_mouseTextInputCharacter(mouseTextInput.font, chars[index], mouseTextInput.x - (i * 12), mouseTextInput.y - ((i * i) / 2), mouseTextInput.upperCase, false);
            }
        }
        // 4 to the right
        for (int i = 1; i <= CHARACTERS; i++) {
            int index = mouseTextInput.selectedIndex + i;
            if (index >= 0 && index < chars.length) {
                render_mouseTextInputCharacter(mouseTextInput.font, chars[index], mouseTextInput.x + (i * 12), mouseTextInput.y - ((i * i) / 2), mouseTextInput.upperCase, false);
            }
        }
        // 1 in center
        render_mouseTextInputCharacter(mouseTextInput.font, chars[mouseTextInput.selectedIndex], mouseTextInput.x, mouseTextInput.y, mouseTextInput.upperCase, inputState.mTextInputMouse1Pressed);

        // Selection
        render_drawCMediaGFX(UIBaseMedia.UI_OSTEXTINPUT_SELECTED, mouseTextInput.x - 1, mouseTextInput.y - 1);
    }

    private void render_mouseTextInputCharacter(CMediaFont font, char c, int x, int y, boolean upperCase, boolean pressed) {
        int pressedIndex = pressed ? 1 : 0;
        render_drawCMediaGFX(UIBaseMedia.UI_OSTEXTINPUT_CHARACTER, x, y, pressedIndex);
        if (c == '\n') {
            render_drawCMediaGFX(UIBaseMedia.UI_OSTEXTINPUT_CONFIRM, x, y, pressedIndex);
        }
        if (c == '\t') {
            render_drawCMediaGFX(upperCase ? UIBaseMedia.UI_OSTEXTINPUT_UPPERCASE : UIBaseMedia.UI_OSTEXTINPUT_LOWERCASE, x, y, pressedIndex);
        }
        if (c == '\b') {
            render_drawCMediaGFX(UIBaseMedia.UI_OSTEXTINPUT_DELETE, x, y, pressedIndex);
        } else {
            int offset = pressed ? 1 : 0;
            render_drawFont(font, String.valueOf(c), 1.0f, x + 2 + offset, y + 2 - offset);
        }
    }

    private void render_drawCursor() {
        render_drawCMediaGFX(inputState.cursor, inputState.mouse_ui.x, inputState.mouse_ui.y);
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
        switch (component) {
            case ComboBox comboBox -> {
                // Menu
                if (UICommons.comboBox_isOpen(inputState, comboBox)) {
                    int width = comboBox.width;
                    int height = comboBox.comboBoxItems.size();
                    /* Menu */
                    for (int iy = 0; iy < height; iy++) {
                        ComboBoxItem comboBoxItem = comboBox.comboBoxItems.get(iy);
                        for (int ix = 0; ix < width; ix++) {
                            int index = render_get9TilesCMediaIndex(ix, iy, width, height);//x==0 ? 0 : (x == (width-1)) ? 2 : 1;
                            CMediaArray cMenuTexture;
                            if (Tools.Calc.pointRectsCollide(inputState.mouse_ui.x, inputState.mouse_ui.y, UICommons.component_getAbsoluteX(comboBox), UICommons.component_getAbsoluteY(comboBox) - (TILE_SIZE) - (iy * TILE_SIZE), comboBox.width * TILE_SIZE, TILE_SIZE)) {
                                cMenuTexture = UIBaseMedia.UI_COMBOBOX_LIST_SELECTED;
                            } else {
                                cMenuTexture = UIBaseMedia.UI_COMBOBOX_LIST;
                            }
                            render_saveTempColorBatch();
                            render_batchSetColor(comboBoxItem.color_r, comboBoxItem.color_g, comboBoxItem.color_b, alpha);
                            render_drawCMediaGFX(cMenuTexture, UICommons.component_getAbsoluteX(comboBox) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(comboBox) - (iy * TILE_SIZE) - TILE_SIZE, index);
                            render_loadTempColorBatch();
                        }
                    }

                    /* Text */
                    for (int i = 0; i < comboBox.comboBoxItems.size(); i++) {
                        ComboBoxItem comboBoxItem = comboBox.comboBoxItems.get(i);
                        render_drawFont(comboBoxItem.font, comboBoxItem.text, alpha, UICommons.component_getAbsoluteX(comboBox), UICommons.component_getAbsoluteY(comboBox) - (i * TILE_SIZE) - TILE_SIZE, 2, 1, (comboBox.width * TILE_SIZE), comboBoxItem.icon, comboBoxItem.iconIndex);
                    }
                }
            }
            default -> {
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
                    if (Tools.Calc.pointRectsCollide(inputState.mouse_ui.x, inputState.mouse_ui.y, contextMenu.x, contextMenu.y - (TILE_SIZE) - (iy * TILE_SIZE), inputState.displayedContextMenuWidth * TILE_SIZE, TILE_SIZE)) {
                        cMenuTexture = UIBaseMedia.UI_CONTEXT_MENU_SELECTED;
                    } else {
                        cMenuTexture = UIBaseMedia.UI_CONTEXT_MENU;
                    }
                    render_saveTempColorBatch();
                    render_batchSetColor(contextMenuItem.color_r, contextMenuItem.color_g, contextMenuItem.color_b, alpha);
                    render_drawCMediaGFX(cMenuTexture, contextMenu.x + (ix * TILE_SIZE), contextMenu.y - (iy * TILE_SIZE) - TILE_SIZE, index);
                    render_loadTempColorBatch();
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
            int line_width = render_textWidth(tooltip.font, line);
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


        if (inputState.mouse_ui.x + ((tooltip_width + 2) * TILE_SIZE) <= inputState.internalResolutionWidth) {
            collidesRight = false;
            //direction = 1;
        }
        if (inputState.mouse_ui.x - ((tooltip_width + 2) * TILE_SIZE) >= 0) {
            collidesLeft = false;
            //direction = 2;
        }
        if (inputState.mouse_ui.y - ((tooltip_height + 2) * TILE_SIZE) >= 0) {
            collidesDown = false;
            //direction = 3;
        }
        if (inputState.mouse_ui.y + ((tooltip_height + 2) * TILE_SIZE) <= inputState.internalResolutionHeight) { // Push down
            collidesUp = false;
            //direction = 4;
        }

        if (collidesUp) direction = 4;
        if (collidesDown) direction = 3;
        if (collidesLeft) direction = 1;
        if (collidesRight) direction = 2;

        switch (direction) {
            case 1 -> {
                tooltip_x = inputState.mouse_ui.x + (2 * TILE_SIZE);
                tooltip_y = inputState.mouse_ui.y - ((tooltip_height * TILE_SIZE) / 2);
            }
            case 2 -> {
                tooltip_x = inputState.mouse_ui.x - ((tooltip_width + 2) * TILE_SIZE);
                tooltip_y = inputState.mouse_ui.y - ((tooltip_height * TILE_SIZE) / 2);
            }
            case 3 -> {
                tooltip_x = inputState.mouse_ui.x - ((tooltip_width * TILE_SIZE) / 2);
                tooltip_y = inputState.mouse_ui.y + ((2) * TILE_SIZE);
            }
            case 4 -> {
                tooltip_x = inputState.mouse_ui.x - ((tooltip_width * TILE_SIZE) / 2);
                tooltip_y = inputState.mouse_ui.y - ((tooltip_height + 2) * TILE_SIZE);
            }
        }


        // Draw
        float alpha = tooltip.color_a * inputState.tooltip_fadeIn_pct;
        render_batchSetColor(tooltip.color_r, tooltip.color_g, tooltip.color_b, alpha);

        // Lines
        switch (direction) {
            case 1 -> {
                render_drawCMediaGFX(UIBaseMedia.UI_TOOLTIP_LINE_X, inputState.mouse_ui.x, inputState.mouse_ui.y);
                render_drawCMediaGFX(UIBaseMedia.UI_TOOLTIP_LINE_X, inputState.mouse_ui.x + TILE_SIZE, inputState.mouse_ui.y);
            }
            case 2 -> {
                render_drawCMediaGFX(UIBaseMedia.UI_TOOLTIP_LINE_X, inputState.mouse_ui.x - TILE_SIZE, inputState.mouse_ui.y);
                render_drawCMediaGFX(UIBaseMedia.UI_TOOLTIP_LINE_X, inputState.mouse_ui.x - (TILE_SIZE * 2), inputState.mouse_ui.y);
            }
            case 3 -> {
                render_drawCMediaGFX(UIBaseMedia.UI_TOOLTIP_LINE_Y, inputState.mouse_ui.x, inputState.mouse_ui.y);
                render_drawCMediaGFX(UIBaseMedia.UI_TOOLTIP_LINE_Y, inputState.mouse_ui.x, inputState.mouse_ui.y + TILE_SIZE);
            }
            case 4 -> {
                render_drawCMediaGFX(UIBaseMedia.UI_TOOLTIP_LINE_Y, inputState.mouse_ui.x, inputState.mouse_ui.y - TILE_SIZE);
                render_drawCMediaGFX(UIBaseMedia.UI_TOOLTIP_LINE_Y, inputState.mouse_ui.x, inputState.mouse_ui.y - (TILE_SIZE * 2));
            }
        }

        // Box
        for (int tx = 0; tx < tooltip_width; tx++) {
            for (int ty = 0; ty < tooltip_height; ty++) {
                if (tooltip.displayFistLineAsTitle && ty == (tooltip_height - 1)) {
                    int titleIndex = (tx == 0 ? 0 : ((tx == tooltip_width - 1) ? 2 : 1));
                    render_drawCMediaGFX(UIBaseMedia.UI_TOOLTIP_TITLE, tooltip_x + (tx * TILE_SIZE), tooltip_y + (ty * TILE_SIZE), titleIndex);
                } else {
                    render_drawCMediaGFX(UIBaseMedia.UI_TOOLTIP, tooltip_x + (tx * TILE_SIZE), tooltip_y + (ty * TILE_SIZE), render_get16TilesCMediaIndex(tx, ty, tooltip_width, tooltip_height));
                }
            }
        }


        //Text
        for (int ty = 0; ty < tooltip_height; ty++) {
            int lineIndex = tooltip_height - ty - 1;
            if (lineIndex < tooltip.lines.length) {
                String lineTxt = tooltip.lines[lineIndex];
                if (tooltip.displayFistLineAsTitle && ty == (tooltip_height - 1)) {
                    int text_width = render_textWidth(tooltip.font, lineTxt);
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
            render_saveTempColorBatch();
            render_batchSetColor(toolTipImage.color_r, toolTipImage.color_g, toolTipImage.color_b, alpha);
            int toolTipImageX = tooltip_x + (toolTipImage.x * UIEngine.TILE_SIZE);
            int toolTipImageY = tooltip_y + (toolTipImage.y * UIEngine.TILE_SIZE);
            render_drawCMediaGFX(toolTipImage.image, toolTipImageX, toolTipImageY);
            render_loadTempColorBatch();
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
                float fadeoutProgress = ((System.currentTimeMillis() - notification.timer) / (float) inputState.config.notification_fadeoutTime);
                yOffsetSlideFade = yOffsetSlideFade + MathUtils.round(TILE_SIZE * (fadeoutProgress));
            }
            render_saveTempColorBatch();
            render_batchSetColor(notification.color_r, notification.color_g, notification.color_b, notification.color_a);
            for (int ix = 0; ix < width; ix++) {
                render_drawCMediaGFX(UIBaseMedia.UI_NOTIFICATION_BAR, (ix * TILE_SIZE), inputState.internalResolutionHeight - TILE_SIZE - (y * TILE_SIZE) + yOffsetSlideFade);
            }
            int xOffset = ((width * TILE_SIZE) / 2) - (render_textWidth(notification.font, notification.text) / 2) - notification.scroll;
            render_drawFont(notification.font, notification.text, notification.color_a, xOffset, (inputState.internalResolutionHeight - TILE_SIZE - (y * TILE_SIZE)) + 1 + yOffsetSlideFade);
            y = y + 1;
            render_loadTempColorBatch();
        }

        render_batchSetColorWhite(1f);
    }

    private void render_drawWindow(Window window) {
        if (!window.visible) return;
        render_batchSetColor(window.color_r, window.color_g, window.color_b, window.color_a);
        for (int ix = 0; ix < window.width; ix++) {
            if (!window.folded) {
                for (int iy = 0; iy < window.height; iy++) {
                    render_drawCMediaGFX(UIBaseMedia.UI_WINDOW, window.x + (ix * TILE_SIZE), window.y + (iy * TILE_SIZE), render_getWindowCMediaIndex(ix, iy, window.width, window.height, window.hasTitleBar));
                }
            } else {
                render_drawCMediaGFX(UIBaseMedia.UI_WINDOW, window.x + (ix * TILE_SIZE), window.y + ((window.height - 1) * TILE_SIZE), render_getWindowCMediaIndex(ix, (window.height - 1), window.width, window.height, window.hasTitleBar));
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

        switch (component) {
            case Button button -> {
                CMediaArray buttonMedia = (button.pressed ? UIBaseMedia.UI_BUTTON_PRESSED : UIBaseMedia.UI_BUTTON);
                int pressed_offset = button.pressed ? 1 : 0;

                for (int ix = 0; ix < button.width; ix++) {
                    for (int iy = 0; iy < button.height; iy++) {
                        render_drawCMediaGFX(buttonMedia, UICommons.component_getAbsoluteX(button) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(button) + (iy * TILE_SIZE), render_get16TilesCMediaIndex(ix, iy, button.width, button.height));
                    }
                }
                if (button instanceof TextButton textButton) {
                    if (textButton.text != null) {
                        render_drawFont(textButton.font, textButton.text, alpha2, UICommons.component_getAbsoluteX(textButton) + textButton.offset_content_x + pressed_offset, UICommons.component_getAbsoluteY(button) + textButton.offset_content_y - pressed_offset, 1, 2, button.width * TILE_SIZE, textButton.icon, textButton.iconIndex);
                    }
                } else if (button instanceof ImageButton imageButton) {
                    render_saveTempColorBatch();
                    render_batchSetColor(imageButton.color2_r, imageButton.color2_g, imageButton.color2_b, alpha2);
                    render_drawCMediaGFX(imageButton.image, UICommons.component_getAbsoluteX(imageButton) + imageButton.offset_content_x + pressed_offset, UICommons.component_getAbsoluteY(imageButton) + imageButton.offset_content_y - pressed_offset, imageButton.arrayIndex);
                    render_loadTempColorBatch();
                }
            }
            case Image image -> {
                if (image.image != null) {
                    render_drawCMediaGFX(image.image, UICommons.component_getAbsoluteX(image), UICommons.component_getAbsoluteY(image), image.arrayIndex, image.animationOffset);
                }
            }
            case Text text -> {
                int textHeight = ((text.height - 1) * TILE_SIZE);
                if (text.lines != null && text.lines.length > 0) {
                    for (int i = 0; i < text.lines.length; i++) {
                        render_drawFont(text.font, text.lines[i], alpha, UICommons.component_getAbsoluteX(text), UICommons.component_getAbsoluteY(text) + textHeight - (i * TILE_SIZE), 1, 1);
                    }
                }
            }
            case ScrollBarVertical scrollBarVertical -> {
                for (int i = 0; i < scrollBarVertical.height; i++) {
                    int index = (i == 0 ? 2 : (i == (scrollBarVertical.height - 1) ? 0 : 1));
                    render_drawCMediaGFX(UIBaseMedia.UI_SCROLLBAR_VERTICAL, UICommons.component_getAbsoluteX(scrollBarVertical), UICommons.component_getAbsoluteY(scrollBarVertical) + (i * TILE_SIZE), index);
                    int buttonYOffset = MathUtils.round(scrollBarVertical.scrolled * ((scrollBarVertical.height - 1) * TILE_SIZE));
                    render_saveTempColorBatch();
                    render_batchSetColor(scrollBarVertical.color2_r, scrollBarVertical.color2_g, scrollBarVertical.color2_b, alpha2);
                    render_drawCMediaGFX(UIBaseMedia.UI_SCROLLBAR_BUTTON_VERTICAL, UICommons.component_getAbsoluteX(scrollBarVertical), UICommons.component_getAbsoluteY(scrollBarVertical) + buttonYOffset, (scrollBarVertical.buttonPressed ? 1 : 0));
                    render_loadTempColorBatch();
                }
            }
            case ScrollBarHorizontal scrollBarHorizontal -> {
                for (int i = 0; i < scrollBarHorizontal.width; i++) {
                    int index = (i == 0 ? 0 : (i == (scrollBarHorizontal.width - 1) ? 2 : 1));
                    render_drawCMediaGFX(UIBaseMedia.UI_SCROLLBAR_HORIZONTAL, UICommons.component_getAbsoluteX(scrollBarHorizontal) + (i * TILE_SIZE), UICommons.component_getAbsoluteY(scrollBarHorizontal), index);
                    int buttonXOffset = MathUtils.round(scrollBarHorizontal.scrolled * ((scrollBarHorizontal.width - 1) * TILE_SIZE));
                    render_saveTempColorBatch();
                    render_batchSetColor(scrollBarHorizontal.color2_r, scrollBarHorizontal.color2_g, scrollBarHorizontal.color2_b, alpha2);
                    render_drawCMediaGFX(UIBaseMedia.UI_SCROLLBAR_BUTTON_HORIZONAL, UICommons.component_getAbsoluteX(scrollBarHorizontal) + buttonXOffset, UICommons.component_getAbsoluteY(scrollBarHorizontal), (scrollBarHorizontal.buttonPressed ? 1 : 0));
                    render_loadTempColorBatch();
                }
            }
            case List list -> {
                boolean itemsValid = (list.items != null && list.items.size() > 0 && list.listAction != null);
                int itemFrom = 0;
                if (itemsValid) {
                    itemFrom = MathUtils.round(list.scrolled * ((list.items.size()) - (list.height)));
                    itemFrom = Tools.Calc.lowerBounds(itemFrom, 0);
                }
                boolean dragEnabled = false;
                boolean dragValid = false;
                int drag_x = -1, drag_y = -1;
                if ((inputState.draggedList != null || inputState.draggedGrid != null) && list == inputState.lastUIMouseHover) {
                    dragEnabled = true;
                    dragValid = UICommons.list_canDragIntoList(inputState, list);
                    if (dragValid) {
                        drag_x = UICommons.component_getAbsoluteX(list);
                        int y_list = UICommons.component_getAbsoluteY(list);
                        drag_y = y_list + ((inputState.mouse_ui.y - y_list) / TILE_SIZE) * TILE_SIZE;
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
                                render_saveTempColorBatch();
                                render_batchSetColor(cellColor.r, cellColor.g, cellColor.b, 1);
                            }
                        }
                    }
                    for (int ix = 0; ix < list.width; ix++) {
                        this.render_drawCMediaGFX(selected ? UIBaseMedia.UI_LIST_SELECTED : UIBaseMedia.UI_LIST, UICommons.component_getAbsoluteX(list) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(list) + itemOffsetY * TILE_SIZE);
                    }
                    if (cellColor != null) render_loadTempColorBatch();

                    // Text
                    if (item != null) {
                        String text = list.listAction.text(item);
                        render_drawFont(list.font, text, alpha, UICommons.component_getAbsoluteX(list), UICommons.component_getAbsoluteY(list) + itemOffsetY * TILE_SIZE, 1, 2, list.width * TILE_SIZE, list.listAction.icon(item), list.listAction.iconIndex(item));
                    }
                }

                if (dragEnabled && dragValid) {
                    for (int ix = 0; ix < list.width; ix++) {
                        this.render_drawCMediaGFX(UIBaseMedia.UI_LIST_DRAG, drag_x + (ix * TILE_SIZE), drag_y, render_getListDragCMediaIndex(ix, list.width));
                    }
                }
                render_enableGrayScaleShader(grayScaleBefore);
            }
            case ComboBox comboBox -> {
                // Box
                for (int ix = 0; ix < comboBox.width; ix++) {
                    int index = ix == 0 ? 0 : (ix == comboBox.width - 1 ? 2 : 1);
                    CMediaGFX comboMedia = UICommons.comboBox_isOpen(inputState, comboBox) ? UIBaseMedia.UI_COMBOBOX_OPEN : UIBaseMedia.UI_COMBOBOX;

                    // Item color or default color
                    float color_r = comboBox.selectedItem != null ? comboBox.selectedItem.color_r : comboBox.color_r;
                    float color_g = comboBox.selectedItem != null ? comboBox.selectedItem.color_g : comboBox.color_g;
                    float color_b = comboBox.selectedItem != null ? comboBox.selectedItem.color_b : comboBox.color_b;

                    render_saveTempColorBatch();
                    render_batchSetColor(color_r, color_g, color_b, alpha2);
                    this.render_drawCMediaGFX(comboMedia, UICommons.component_getAbsoluteX(comboBox) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(comboBox), index);
                    render_loadTempColorBatch();

                }
                // Text
                if (comboBox.selectedItem != null && comboBox.comboBoxAction != null) {
                    render_drawFont(comboBox.selectedItem.font, comboBox.selectedItem.text, alpha, UICommons.component_getAbsoluteX(comboBox), UICommons.component_getAbsoluteY(comboBox), 2, 1, (comboBox.width - 1) * TILE_SIZE, comboBox.selectedItem.icon, comboBox.selectedItem.iconIndex);
                }
            }
            case Knob knob -> {
                render_drawCMediaGFX(UIBaseMedia.UI_KNOB_BACKGROUND, UICommons.component_getAbsoluteX(knob), UICommons.component_getAbsoluteY(knob));
                render_saveTempColorBatch();
                render_batchSetColor(knob.color2_r, knob.color2_g, knob.color2_b, alpha2);
                if (knob.endless) {
                    int index = MathUtils.round(knob.turned * 31);
                    render_drawCMediaGFX(UIBaseMedia.UI_KNOB_ENDLESS, UICommons.component_getAbsoluteX(knob), UICommons.component_getAbsoluteY(knob), index);
                } else {
                    int index = MathUtils.round(knob.turned * 25);
                    render_drawCMediaGFX(UIBaseMedia.UI_KNOB, UICommons.component_getAbsoluteX(knob), UICommons.component_getAbsoluteY(knob), index);
                }
                render_loadTempColorBatch();
            }
            case Canvas canvas -> {
                inputState.spriteBatch_ui.draw(canvas.texture, UICommons.component_getAbsoluteX(canvas), UICommons.component_getAbsoluteY(canvas));

                canvas.canvasImages.removeIf(mapOverlay -> {
                    if (mapOverlay.fadeOut) {
                        mapOverlay.color_a = 1 - ((System.currentTimeMillis() - mapOverlay.timer) / (float) mapOverlay.fadeOutTime);
                        if (mapOverlay.color_a <= 0) return true;
                    }
                    render_saveTempColorBatch();
                    render_batchSetColor(mapOverlay.color_r, mapOverlay.color_g, mapOverlay.color_b, alpha * mapOverlay.color_a);
                    render_drawCMediaGFX(mapOverlay.image, UICommons.component_getAbsoluteX(canvas) + mapOverlay.x, UICommons.component_getAbsoluteY(canvas) + mapOverlay.y, mapOverlay.arrayIndex);
                    render_loadTempColorBatch();
                    return false;
                });
            }
            case TextField textField -> {
                for (int ix = 0; ix < textField.width; ix++) {
                    int index = ix == (textField.width - 1) ? 2 : (ix == 0) ? 0 : 1;

                    render_drawCMediaGFX(inputState.focusedTextField == textField ? UIBaseMedia.UI_TEXTFIELD_FOCUSED : UIBaseMedia.UI_TEXTFIELD, UICommons.component_getAbsoluteX(textField) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(textField), index);

                    if (!textField.contentValid) {
                        render_saveTempColorBatch();
                        render_batchSetColor(0.90588236f, 0.29803923f, 0.23529412f, (alpha * 0.2f));
                        render_drawCMediaGFX(UIBaseMedia.UI_TEXTFIELD_VALIDATION_OVERLAY, UICommons.component_getAbsoluteX(textField) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(textField), index);
                        render_loadTempColorBatch();
                    }

                    if (textField.content != null) {
                        render_drawFont(textField.font, textField.content.substring(textField.offset), alpha, UICommons.component_getAbsoluteX(textField), UICommons.component_getAbsoluteY(textField), 1, 2, (textField.width * TILE_SIZE) - 4);
                        if (UICommons.textField_isFocused(inputState, textField)) {
                            int xOffset = render_textWidth(textField.font, textField.content.substring(textField.offset, textField.markerPosition)) + 2;
                            if (xOffset < textField.width * TILE_SIZE) {
                                render_drawCMediaGFX(UIBaseMedia.UI_TEXTFIELD_CARET, UICommons.component_getAbsoluteX(textField) + xOffset, UICommons.component_getAbsoluteY(textField));
                            }
                        }
                    }
                }
            }
            case Grid grid -> {
                int tileSize = grid.doubleSized ? TILE_SIZE * 2 : TILE_SIZE;
                int gridWidth = grid.items.length;
                int gridHeight = grid.items[0].length;

                boolean dragEnabled = false;
                boolean dragValid = false;
                int drag_x = -1, drag_y = -1;
                if ((inputState.draggedList != null || inputState.draggedGrid != null) && grid == inputState.lastUIMouseHover) {
                    dragEnabled = true;
                    dragValid = UICommons.grid_canDragIntoGrid(inputState, grid);
                    if (dragValid) {
                        int x_grid = UICommons.component_getAbsoluteX(grid);
                        int y_grid = UICommons.component_getAbsoluteY(grid);
                        int m_x = inputState.mouse_ui.x - x_grid;
                        int m_y = inputState.mouse_ui.y - y_grid;
                        if (m_x > 0 && m_x < (grid.width * tileSize) && m_y > 0 && m_y < (grid.height * tileSize)) {
                            int inv_x = m_x / tileSize;
                            int inv_y = m_y / tileSize;
                            if (UICommons.grid_positionValid(grid, inv_x, inv_y)) {
                                drag_x = inv_x;
                                drag_y = inv_y;
                            }
                        }
                    }
                }

                boolean grayScaleBefore = render_GrayScaleShaderEnabled();
                if (dragEnabled && !dragValid) render_enableGrayScaleShader(true);

                for (int ix = 0; ix < gridWidth; ix++) {
                    for (int iy = 0; iy < gridHeight; iy++) {
                        if (grid.items != null) {
                            CMediaGFX cellMedia;
                            boolean selected = grid.items[ix][iy] != null && grid.items[ix][iy] == grid.selectedItem;
                            if (dragEnabled && dragValid && drag_x == ix && drag_y == iy) {
                                cellMedia = grid.doubleSized ? UIBaseMedia.UI_GRID_DRAGGED_X2 : UIBaseMedia.UI_GRID_DRAGGED;
                            } else {
                                if (selected) {
                                    cellMedia = grid.doubleSized ? UIBaseMedia.UI_GRID_SELECTED_X2 : UIBaseMedia.UI_GRID_SELECTED;
                                } else {
                                    cellMedia = grid.doubleSized ? UIBaseMedia.UI_GRID_X2 : UIBaseMedia.UI_GRID;
                                }
                            }

                            render_saveTempColorBatch();

                            // Draw Cell
                            Color cellColor = grid.gridAction != null ? grid.gridAction.cellColor(grid.items[ix][iy], ix, iy) : null;
                            if (cellColor != null) {
                                render_batchSetColor(cellColor.r, cellColor.g, cellColor.b, 1f);
                            } else {
                                render_batchSetColorWhite(alpha);
                            }
                            int index = grid.doubleSized ? render_get16TilesCMediaIndex(ix, iy, grid.width / 2, grid.height / 2) : render_get16TilesCMediaIndex(ix, iy, grid.width, grid.height);
                            render_drawCMediaGFX(cellMedia, UICommons.component_getAbsoluteX(grid) + (ix * tileSize), UICommons.component_getAbsoluteY(grid) + (iy * tileSize), index);

                            // Draw Icon
                            CMediaGFX icon = (grid.items[ix][iy] != null && grid.gridAction != null) ? grid.gridAction.icon(grid.items[ix][iy]) : null;

                            if (icon != null) {
                                render_batchSetColorWhite(alpha);
                                int iconIndex = grid.gridAction != null ? grid.gridAction.iconIndex(grid.items[ix][iy]) : 0;
                                render_drawCMediaGFX(icon, UICommons.component_getAbsoluteX(grid) + (ix * tileSize), UICommons.component_getAbsoluteY(grid) + (iy * tileSize), iconIndex);
                            }
                            render_loadTempColorBatch();
                        }
                    }
                }
                render_enableGrayScaleShader(grayScaleBefore);
            }
            case TabBar tabBar -> {
                int tabXOffset = tabBar.tabOffset;
                int topBorder;
                for (int i = 0; i < tabBar.tabs.size(); i++) {
                    Tab tab = tabBar.tabs.get(i);
                    int tabWidth = tabBar.bigIconMode ? 2 : tab.width;
                    if ((tabXOffset + tabWidth) > tabBar.width) break;

                    boolean selected = i == tabBar.selectedTab;
                    CMediaGFX tabGraphic;
                    if (tabBar.bigIconMode) {
                        tabGraphic = selected ? UIBaseMedia.UI_TAB_BIGICON_SELECTED : UIBaseMedia.UI_TAB_BIGICON;
                    } else {
                        tabGraphic = selected ? UIBaseMedia.UI_TAB_SELECTED : UIBaseMedia.UI_TAB;
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
                    render_drawCMediaGFX(UIBaseMedia.UI_TAB_BORDERS, UICommons.component_getAbsoluteX(tabBar) + ((tabXOffset + ix) * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar), 2);
                }

                if (tabBar.border) {
                    // Bottom
                    for (int ix = 0; ix < tabBar.width; ix++) {
                        render_drawCMediaGFX(UIBaseMedia.UI_TAB_BORDERS, UICommons.component_getAbsoluteX(tabBar) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar) - (tabBar.borderHeight * TILE_SIZE), 2);
                    }
                    // Left/Right
                    for (int iy = 0; iy < tabBar.borderHeight; iy++) {
                        int yOffset = (iy + 1) * TILE_SIZE;
                        render_drawCMediaGFX(UIBaseMedia.UI_TAB_BORDERS, UICommons.component_getAbsoluteX(tabBar), UICommons.component_getAbsoluteY(tabBar) - yOffset, 0);
                        render_drawCMediaGFX(UIBaseMedia.UI_TAB_BORDERS, UICommons.component_getAbsoluteX(tabBar) + ((tabBar.width - 1) * TILE_SIZE), UICommons.component_getAbsoluteY(tabBar) - yOffset, 1);
                    }
                }
            }
            case Shape shape -> {
                if (shape.shapeType != null) {
                    CMediaImage shapeImage = switch (shape.shapeType) {
                        case OVAL -> UIBaseMedia.UI_SHAPE_OVAL;
                        case RECT -> UIBaseMedia.UI_SHAPE_RECT;
                        case DIAMOND -> UIBaseMedia.UI_SHAPE_DIAMOND;
                        case TRIANGLE_LEFT_DOWN -> UIBaseMedia.UI_SHAPE_TRIANGLE_LEFT_DOWN;
                        case TRIANGLE_RIGHT_DOWN -> UIBaseMedia.UI_SHAPE_TRIANGLE_RIGHT_DOWN;
                        case TRIANGLE_LEFT_UP -> UIBaseMedia.UI_SHAPE_TRIANGLE_LEFT_UP;
                        case TRIANGLE_RIGHT_UP -> UIBaseMedia.UI_SHAPE_TRIANGLE_RIGHT_UP;
                    };
                    mediaManager.drawCMediaImage(inputState.spriteBatch_ui, shapeImage, UICommons.component_getAbsoluteX(shape), UICommons.component_getAbsoluteY(shape),
                            0, 0, shape.width * TILE_SIZE, shape.height * TILE_SIZE);
                }
            }
            case ProgressBar progressBar -> {
                // Background
                for (int ix = 0; ix < progressBar.width; ix++) {
                    int index = ix == 0 ? 0 : ix == (progressBar.width - 1) ? 2 : 1;
                    render_drawCMediaGFX(UIBaseMedia.UI_PROGRESSBAR, UICommons.component_getAbsoluteX(progressBar) + (ix * TILE_SIZE), UICommons.component_getAbsoluteY(progressBar), index);
                }

                // Bar
                render_saveTempColorBatch();
                render_batchSetColor(progressBar.color2_r, progressBar.color2_g, progressBar.color2_b, alpha2);
                int pixels = MathUtils.round(progressBar.progress * (progressBar.width * TILE_SIZE));
                for (int ix = 0; ix < progressBar.width; ix++) {
                    int xOffset = ix * TILE_SIZE;
                    int index = ix == 0 ? 0 : ix == (progressBar.width - 1) ? 2 : 1;
                    if (xOffset < pixels) {
                        if (pixels - xOffset < TILE_SIZE) {
                            mediaManager.drawCMediaArrayCut(inputState.spriteBatch_ui, UIBaseMedia.UI_PROGRESSBAR_BAR, UICommons.component_getAbsoluteX(progressBar) + xOffset, UICommons.component_getAbsoluteY(progressBar), index, pixels - xOffset, TILE_SIZE);
                        } else {
                            mediaManager.drawCMediaArray(inputState.spriteBatch_ui, UIBaseMedia.UI_PROGRESSBAR_BAR, UICommons.component_getAbsoluteX(progressBar) + xOffset, UICommons.component_getAbsoluteY(progressBar), index);
                        }
                    }
                }
                render_loadTempColorBatch();

                if (progressBar.progressText) {
                    String percentTxt = progressBar.progressText2Decimal ? UICommons.progressBar_getProgressText2Decimal(progressBar.progress) : UICommons.progressBar_getProgressText(progressBar.progress);
                    int xOffset = ((progressBar.width * TILE_SIZE) / 2) - (render_textWidth(progressBar.font, percentTxt) / 2);
                    render_drawFont(progressBar.font, percentTxt, alpha, UICommons.component_getAbsoluteX(progressBar) + xOffset, UICommons.component_getAbsoluteY(progressBar), 0, 1);
                }
            }
            case CheckBox checkBox -> {
                CMediaArray tex = checkBox.checkBoxStyle == CheckBoxStyle.CHECKBOX ? UIBaseMedia.UI_CHECKBOX_CHECKBOX : UIBaseMedia.UI_CHECKBOX_RADIO;
                render_drawCMediaGFX(tex, UICommons.component_getAbsoluteX(checkBox), UICommons.component_getAbsoluteY(checkBox), checkBox.checked ? 1 : 0);
                render_drawFont(checkBox.font, checkBox.text, alpha, UICommons.component_getAbsoluteX(checkBox) + TILE_SIZE, UICommons.component_getAbsoluteY(checkBox), 1, 1);
            }
            case GameViewPort gameViewPort -> {
                inputState.spriteBatch_ui.draw(gameViewPort.textureRegion, UICommons.component_getAbsoluteX(gameViewPort), UICommons.component_getAbsoluteY(gameViewPort));
            }
            default -> {
            }
        }

        render_enableGrayScaleShader(disableShaderState);
        render_batchSetColorWhite(1f);
    }

    private void render_drawCursorListDrags() {
        if (inputState.draggedGrid != null) {
            Grid dragGrid = inputState.draggedGrid;
            int dragOffsetX = inputState.draggedGridOffset.x;
            int dragOffsetY = inputState.draggedGridOffset.y;
            Object dragItem = inputState.draggedGridItem;
            if (dragGrid.gridAction != null) {
                render_batchSetColorWhite(inputState.config.component_gridDragAlpha);
                CMediaGFX icon = dragGrid.gridAction.icon(dragItem);
                render_drawCMediaGFX(icon, inputState.mouse_ui.x - dragOffsetX, inputState.mouse_ui.y - dragOffsetY, dragGrid.gridAction.iconIndex(dragItem));
            }
        } else if (inputState.draggedList != null) {
            List dragList = inputState.draggedList;
            int dragOffsetX = inputState.draggedListOffsetX.x;
            int dragOffsetY = inputState.draggedListOffsetX.y;
            Object dragItem = inputState.draggedListItem;
            if (dragList.listAction != null) {
                // List
                render_batchSetColor(dragList.color_r, dragList.color_g, dragList.color_b, Math.min(dragList.color_a, inputState.config.component_listDragAlpha));
                for (int ix = 0; ix < dragList.width; ix++) {
                    this.render_drawCMediaGFX(UIBaseMedia.UI_LIST_SELECTED, inputState.mouse_ui.x - dragOffsetX + (ix * TILE_SIZE), inputState.mouse_ui.y - dragOffsetY);
                }
                // Text
                String text = dragList.listAction.text(dragItem);
                render_drawFont(dragList.font, text, dragList.color_a, inputState.mouse_ui.x - dragOffsetX, inputState.mouse_ui.y - dragOffsetY, 2, 1,
                        dragList.width * TILE_SIZE, dragList.listAction.icon(dragItem), dragList.listAction.iconIndex(dragItem));
            }
        }

        render_batchSetColorWhite(1f);
    }


    private boolean render_GrayScaleShaderEnabled() {
        return inputState.spriteBatch_ui.getShader() == inputState.grayScaleShader;
    }

    private void render_enableGrayScaleShader(boolean enable) {
        inputState.spriteBatch_ui.setShader(enable ? inputState.grayScaleShader : null);
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

    private int render_textWidth(CMediaFont font, String text) {
        if (font == null || text == null || text.length() == 0) return 0;
        return mediaManager.textWidth(font, text);
    }

    private void render_drawFont(CMediaFont font, String text, float alpha, int x, int y, int textXOffset, int textYOffset, int maxWidth, CMediaGFX icon, int iconIndex) {
        if (font == null) return;
        boolean withIcon = icon != null;
        if (withIcon) {
            render_saveTempColorBatch();
            render_batchSetColorWhite(alpha);
            render_drawCMediaGFX(icon, x, y, iconIndex);
            render_loadTempColorBatch();
        }

        render_saveTempColorFont(font);
        render_fontSetAlpha(font, alpha);
        if (maxWidth == FONT_MAXWIDTH_NONE) {
            mediaManager.drawCMediaFont(inputState.spriteBatch_ui, font, x + (withIcon ? TILE_SIZE : 0) + textXOffset, y + textYOffset, text);
        } else {
            mediaManager.drawCMediaFont(inputState.spriteBatch_ui, font, x + (withIcon ? TILE_SIZE : 0) + textXOffset, y + textYOffset, text, maxWidth);
        }
        render_loadTempColorFont(font);
    }

    private void render_fontSetAlpha(CMediaFont font, float a) {
        mediaManager.getCMediaFont(font).setColor(1, 1, 1, a);
    }

    private void render_fontSetColorWhite() {
        inputState.spriteBatch_ui.setColor(1, 1, 1, 1);
    }

    private Color render_fontGetColor(CMediaFont font) {
        return mediaManager.getCMediaFont(font).getColor();
    }


    private void render_batchSetColor(float r, float g, float b, float a) {
        inputState.spriteBatch_ui.setColor(r, g, b, a);
    }

    private void render_batchSetColorWhite(float alpha) {
        inputState.spriteBatch_ui.setColor(1, 1, 1, alpha);
    }

    private Color render_batchGetColor() {
        return inputState.spriteBatch_ui.getColor();
    }


    private void render_saveTempColorBatch() {
        inputState.tempSaveColor.set(inputState.spriteBatch_ui.getColor());
    }

    private void render_loadTempColorBatch() {
        inputState.spriteBatch_ui.setColor(inputState.tempSaveColor);
    }

    private void render_saveTempColorFont(CMediaFont font) {
        inputState.tempSaveColor.set(mediaManager.getCMediaFont(font).getColor());
    }

    private void render_loadTempColorFont(CMediaFont font) {
        mediaManager.getCMediaFont(font).setColor(inputState.tempSaveColor);
    }

    private void render_drawCMediaGFX(CMediaGFX cMedia, int x, int y) {
        render_drawCMediaGFX(cMedia, x, y, 0, 0);
    }

    private void render_drawCMediaGFX(CMediaGFX cMedia, int x, int y, int arrayIndex) {
        render_drawCMediaGFX(cMedia, x, y, arrayIndex, 0);
    }

    private void render_drawCMediaGFX(CMediaGFX cMedia, int x, int y, int arrayIndex, float animation_timer_offset) {
        mediaManager.drawCMediaGFX(inputState.spriteBatch_ui, cMedia, x, y, arrayIndex, (inputState.animation_timer_ui + animation_timer_offset));
    }

    private void render_drawCMediaGFX(CMediaGFX cMedia, int x, int y, int arrayIndex, float animation_timer_offset, int area_x, int area_y, int area_w, int area_h) {
        mediaManager.drawCMediaGFX(inputState.spriteBatch_ui, cMedia, x, y, arrayIndex, (inputState.animation_timer_ui + animation_timer_offset));
    }

    public void shutdown() {
        this.uiAdapter.shutdown();

        // Lists
        inputState.windows.clear();

        inputState.modalWindowQueue.clear();
        inputState.hotKeys.clear();
        inputState.singleUpdateActions.clear();
        inputState.screenComponents.clear();
        inputState.notifications.clear();
        inputState.gameViewPorts.clear();

        // SpriteBatch
        if (inputState.spriteRenderer) inputState.spriteBatch_game.dispose();
        inputState.spriteBatch_ui.dispose();

        // ImmediateRenderer
        if (inputState.shaderRenderer) inputState.shaderBatch_game.dispose();
        if (inputState.shaderRenderer) inputState.shaderBatch_ui.dispose();

        // Textures
        inputState.spriteBatch_screen.dispose();
        inputState.texture_game.getTexture().dispose();
        inputState.texture_ui.getTexture().dispose();
        inputState.texture_screen.getTexture().dispose();

        // Shaders
        inputState.grayScaleShader.dispose();

        inputState = null;
    }

    public int getInternalResolutionWidth() {
        return inputState.internalResolutionWidth;
    }

    public int getInternalResolutionHeight() {
        return inputState.internalResolutionHeight;
    }

    public VIEWPORT_MODE getViewportMode() {
        return inputState.viewportMode;
    }

    public boolean isSpriteRendererEnabled() {
        return inputState.spriteRenderer;
    }

    public boolean isImmediateRendererEnabled() {
        return inputState.shaderRenderer;
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

    public TextureRegion getTextureUI() {
        return inputState.texture_game;
    }
}
