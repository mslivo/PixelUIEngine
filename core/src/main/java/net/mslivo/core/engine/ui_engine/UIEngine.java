package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;
import net.mslivo.core.engine.media_manager.*;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.constants.*;
import net.mslivo.core.engine.ui_engine.media.UIEngineBaseMedia_8x8;
import net.mslivo.core.engine.ui_engine.rendering.NestedFrameBuffer;
import net.mslivo.core.engine.ui_engine.rendering.PrimitiveRenderer;
import net.mslivo.core.engine.ui_engine.rendering.SpriteRenderer;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.state.input.UIInputEvents;
import net.mslivo.core.engine.ui_engine.state.input.UIInputProcessor;
import net.mslivo.core.engine.ui_engine.ui.Window;
import net.mslivo.core.engine.ui_engine.ui.actions.CommonActions;
import net.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;
import net.mslivo.core.engine.ui_engine.ui.components.button.Button;
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
import net.mslivo.core.engine.ui_engine.ui.contextmenu.Contextmenu;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextmenuItem;
import net.mslivo.core.engine.ui_engine.ui.hotkeys.HotKey;
import net.mslivo.core.engine.ui_engine.ui.mousetextinput.MouseTextInput;
import net.mslivo.core.engine.ui_engine.ui.notification.Notification;
import net.mslivo.core.engine.ui_engine.ui.tooltip.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * UI Engine
 * Handles UI Elements, Input, Cameras
 * App needs to be implemented inside the uiAdapter
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public final class UIEngine<T extends UIEngineAdapter> {
    private static final int FONT_MAXWIDTH_NONE = -1;

    // Basic Configuration
    private final T uiAdapter;
    private final UIEngineState uiEngineState;
    private final API api;
    private final MediaManager mediaManager;

    public T getAdapter() {
        return uiAdapter;
    }

    public UIEngine(T uiAdapter, MediaManager mediaManager, int resolutionWidth, int resolutionHeight) {
        this(uiAdapter, mediaManager, resolutionWidth, resolutionHeight, VIEWPORT_MODE.PIXEL_PERFECT, true);
    }

    public UIEngine(T uiAdapter, MediaManager mediaManager, int resolutionWidth, int resolutionHeight, VIEWPORT_MODE viewportMode) {
        this(uiAdapter, mediaManager, resolutionWidth, resolutionHeight, viewportMode, true);
    }

    public UIEngine(T uiAdapter, MediaManager mediaManager, int resolutionWidth, int resolutionHeight, VIEWPORT_MODE viewportMode, boolean gamePadSupport) {
        if (uiAdapter == null || mediaManager == null) {
            throw new RuntimeException("Cannot initialize UIEngine: missing parameters");
        }
        this.uiAdapter = uiAdapter;
        this.mediaManager = mediaManager;
        /* Setup */
        this.uiEngineState = initializeInputState(resolutionWidth, resolutionHeight, viewportMode, gamePadSupport, TILE_SIZE.MODE_8x8);
        this.api = new API(this.uiEngineState, mediaManager);

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        /*  Call Adapter Init */
        this.uiAdapter.init(this.api, this.mediaManager);
    }


    private UIEngineState initializeInputState(int resolutionWidth, int resolutionHeight, VIEWPORT_MODE viewportMode, boolean gamePadSupport, TILE_SIZE tileSize) {
        UIEngineState newUIEngineState = new UIEngineState();

        //  ----- Paramters
        newUIEngineState.resolutionWidth = Math.max(resolutionWidth, 16);
        newUIEngineState.resolutionHeight = Math.max(resolutionHeight, 16);
        newUIEngineState.resolutionWidthHalf = MathUtils.round(resolutionWidth / 2f);
        newUIEngineState.resolutionHeightHalf = MathUtils.round(resolutionHeight / 2f);
        newUIEngineState.viewportMode = viewportMode != null ? viewportMode : VIEWPORT_MODE.PIXEL_PERFECT;
        newUIEngineState.gamePadSupport = gamePadSupport;
        newUIEngineState.tileSize = tileSize;

        // ----- Config
        newUIEngineState.config = new UIConfig(newUIEngineState);

        // -----  App
        newUIEngineState.camera_app = new OrthographicCamera(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.camera_app.setToOrtho(false, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.camera_app.position.set(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeightHalf, 0);
        newUIEngineState.camera_app.zoom = 1f;
        newUIEngineState.camera_app.update();
        newUIEngineState.frameBuffer_app = new NestedFrameBuffer(Pixmap.Format.RGB888, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight, true);
        newUIEngineState.frameBuffer_app.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // -----  GUI
        newUIEngineState.spriteRenderer_ui = new SpriteRenderer(this.mediaManager);
        newUIEngineState.spriteRenderer_ui.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

        newUIEngineState.primitiveRenderer_ui = new PrimitiveRenderer();
        newUIEngineState.primitiveRenderer_ui.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

        newUIEngineState.camera_ui = new OrthographicCamera(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.camera_ui.setToOrtho(false, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.camera_ui.update();
        newUIEngineState.frameBuffer_ui = new NestedFrameBuffer(Pixmap.Format.RGBA8888, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight, false);
        newUIEngineState.frameBuffer_ui.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        // ----- UpScaler
        newUIEngineState.upscaleFactor_screen = UICommonUtils.viewport_determineUpscaleFactor(newUIEngineState.viewportMode, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.textureFilter_screen = UICommonUtils.viewport_determineUpscaleTextureFilter(newUIEngineState.viewportMode);
        newUIEngineState.frameBuffer_screen = new NestedFrameBuffer(Pixmap.Format.RGBA8888, newUIEngineState.resolutionWidth * newUIEngineState.upscaleFactor_screen, newUIEngineState.resolutionHeight * newUIEngineState.upscaleFactor_screen, false);
        newUIEngineState.frameBuffer_screen.getColorBufferTexture().setFilter(newUIEngineState.textureFilter_screen, newUIEngineState.textureFilter_screen);
        // ----- Screen
        newUIEngineState.viewport_screen = UICommonUtils.viewport_createViewport(newUIEngineState.viewportMode, newUIEngineState.camera_ui, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.viewport_screen.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // -----  GUI
        newUIEngineState.windows = new ArrayList<>();
        newUIEngineState.screenComponents = new ArrayList<>();
        newUIEngineState.openContextMenu = null;
        newUIEngineState.pressedContextMenuItem = null;
        newUIEngineState.displayedContextMenuWidth = 0;
        newUIEngineState.openMouseTextInput = null;
        newUIEngineState.mTextInputMouse1Pressed = false;
        newUIEngineState.mTextInputMouse2Pressed = false;
        newUIEngineState.mTextInputMouse3Pressed = false;
        newUIEngineState.mTextInputGamePadLeft = false;
        newUIEngineState.mTextInputGamePadRight = false;
        newUIEngineState.mTextInputScrollTimer = 0;
        newUIEngineState.mTextInputScrollTime = 0;
        newUIEngineState.mTextInputScrollSpeed = 0;
        newUIEngineState.mTextInputTranslatedMouse1Down = false;
        newUIEngineState.mTextInputTranslatedMouse2Down = false;
        newUIEngineState.mTextInputUnlock = false;
        newUIEngineState.mTextInputAPICharacterQueue = new IntArray();
        newUIEngineState.keyboardInteractedUIObjectFrame = null;
        newUIEngineState.mouseInteractedUIObjectFrame = null;
        newUIEngineState.modalWindow = null;
        newUIEngineState.modalWindowQueue = new ArrayDeque<>();
        newUIEngineState.pressedTextField = null;
        newUIEngineState.pressedTextFieldMouseX = 0;
        newUIEngineState.focusedTextField = null;
        newUIEngineState.notifications = new ArrayList<>();
        newUIEngineState.hotKeys = new ArrayList<>();
        newUIEngineState.appViewPorts = new ArrayList<>();
        newUIEngineState.singleUpdateActions = new ArrayList<>();
        newUIEngineState.singleUpdateActionsRemoveQ = new ArrayDeque<>();
        // ----- Temp GUI Variables
        newUIEngineState.draggedWindow = null;
        newUIEngineState.draggedWindow_offset = new GridPoint2();
        newUIEngineState.pressedButton = null;
        newUIEngineState.pressedKnob = null;
        newUIEngineState.tooltip = null;
        newUIEngineState.tooltip_fadePct = 0f;
        newUIEngineState.tooltip_wait_delay = false;
        newUIEngineState.tooltip_delay_timer = 0;
        newUIEngineState.pressedScrollBarVertical = null;
        newUIEngineState.pressedScrollBarHorizontal = null;
        newUIEngineState.draggedGridItem = null;
        newUIEngineState.draggedGrid = null;
        newUIEngineState.draggedGridOffset = new GridPoint2();
        newUIEngineState.draggedGridFrom = new GridPoint2();
        newUIEngineState.pressedGrid = null;
        newUIEngineState.pressedGridItem = null;
        newUIEngineState.draggedListItem = null;
        newUIEngineState.draggedList = null;
        newUIEngineState.draggedListOffsetX = new GridPoint2();
        newUIEngineState.draggedListFromIndex = 0;
        newUIEngineState.pressedList = null;
        newUIEngineState.pressedListItem = null;
        newUIEngineState.pressedAppViewPort = null;
        newUIEngineState.tooltip_lastHoverObject = null;
        newUIEngineState.pressedCanvas = null;
        newUIEngineState.openComboBox = null;
        newUIEngineState.pressedComboBoxItem = null;
        newUIEngineState.pressedCheckBox = null;
        // ----- Controls
        newUIEngineState.currentControlMode = MOUSE_CONTROL_MODE.DISABLED;
        newUIEngineState.mouse_ui = new GridPoint2(newUIEngineState.resolutionWidthHalf, newUIEngineState.resolutionHeightHalf);
        newUIEngineState.mouse_app = new GridPoint2(0, 0);
        newUIEngineState.mouse_delta = new Vector2(0, 0);
        newUIEngineState.lastUIMouseHover = null;
        newUIEngineState.cursor = null;
        newUIEngineState.cursorArrayIndex = 0;
        newUIEngineState.mouseTool = null;
        newUIEngineState.overrideCursor = null;
        newUIEngineState.overrideCursorArrayIndex = 0;
        newUIEngineState.displayOverrideCursor = false;
        newUIEngineState.vector_fboCursor = new Vector3(0, 0, 0);
        newUIEngineState.vector2_unproject = new Vector2(0, 0);
        newUIEngineState.mouse_emulated = new Vector2(newUIEngineState.resolutionWidthHalf, newUIEngineState.resolutionHeightHalf);
        newUIEngineState.emulatedMouseLastMouseClick = 0;
        newUIEngineState.keyBoardMouseSpeedUp = new Vector2(0, 0);
        newUIEngineState.emulatedMouseIsButtonDown = new boolean[]{false, false, false, false, false};
        newUIEngineState.keyBoardTranslatedKeysDown = new boolean[256];
        newUIEngineState.gamePadTranslatedButtonsDown = new boolean[15];
        newUIEngineState.gamePadTranslatedStickLeft = new Vector2(0, 0);
        newUIEngineState.gamePadTranslatedStickRight = new Vector2(0, 0);

        // ---- Misc
        newUIEngineState.animationTimer_ui = 0f;
        newUIEngineState.fontTempColor = new Color(Color.CLEAR);
        newUIEngineState.inputEvents = new UIInputEvents();
        newUIEngineState.inputProcessor = new UIInputProcessor(newUIEngineState.inputEvents, newUIEngineState.gamePadSupport);
        newUIEngineState.itemInfo_listIndex = 0;
        newUIEngineState.itemInfo_tabBarTabIndex = 0;
        newUIEngineState.itemInfo_gridPos = new GridPoint2();
        newUIEngineState.itemInfo_listValid = false;
        newUIEngineState.itemInfo_tabBarValid = false;
        newUIEngineState.itemInfo_gridValid = false;
        return newUIEngineState;
    }

    public void resize(int width, int height) {
        uiEngineState.viewport_screen.update(width, height, true);
    }

    public void update() {
        // UI
        this.updateMouseControl();
        this.updateUI(); // Main UI Updates happen here
        this.updateMouseCursor();

        // Update Game
        this.uiAdapter.update();

        // Reset Input Events
        this.uiEngineState.inputEvents.reset();
    }

    private void updateMouseControl() {
        if (!uiEngineState.config.input_gamePadMouseEnabled && !uiEngineState.config.input_keyboardMouseEnabled && !uiEngineState.config.input_hardwareMouseEnabled) {
            mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE.DISABLED);
            mouseControl_chokeAllMouseEvents();
        } else {
            if (uiEngineState.config.input_gamePadMouseEnabled && mouseControl_gamePadMouseTranslateAndChokeEvents()) {
                mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE.GAMEPAD);
            } else if (uiEngineState.config.input_keyboardMouseEnabled && mouseControl_keyboardMouseTranslateAndChokeEvents()) {
                mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE.KEYBOARD);
            } else if (uiEngineState.config.input_hardwareMouseEnabled && mouseControl_hardwareMouseDetectUse()) {
                mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE.HARDWARE_MOUSE);
            }
        }

        if (uiEngineState.openMouseTextInput != null) {
            // Translate to Text Input
            mouseControl_updateMouseTextInput();
        } else {
            // Translate to MouseGUI position
            switch (uiEngineState.currentControlMode) {
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
        if (nextControlMode != null && nextControlMode != uiEngineState.currentControlMode) {
            // Clean up current control mode
            if (uiEngineState.currentControlMode.emulated) {
                if (uiEngineState.currentControlMode == MOUSE_CONTROL_MODE.GAMEPAD) {
                    // Gamepad
                    for (int i = 0; i < uiEngineState.gamePadTranslatedButtonsDown.length; i++)
                        uiEngineState.gamePadTranslatedButtonsDown[i] = false;
                    uiEngineState.gamePadTranslatedStickLeft.set(0f, 0f);
                    uiEngineState.gamePadTranslatedStickRight.set(0f, 0f);
                }
                if (uiEngineState.currentControlMode == MOUSE_CONTROL_MODE.KEYBOARD) {
                    // Keyboard
                    for (int i = 0; i < uiEngineState.keyBoardTranslatedKeysDown.length; i++)
                        uiEngineState.keyBoardTranslatedKeysDown[i] = false;
                    uiEngineState.keyBoardMouseSpeedUp.set(0f, 0f);
                }
                // Simulated
                for (int i = 0; i <= 4; i++) uiEngineState.emulatedMouseIsButtonDown[i] = false;
                uiEngineState.emulatedMouseLastMouseClick = 0;
            }

            // Set Next ControlMode
            if (nextControlMode.emulated) {
                this.uiEngineState.mouse_emulated.set(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y);
            }
            uiEngineState.currentControlMode = nextControlMode;
        }
    }


    private void mouseControl_updateMouseTextInput() {
        if (uiEngineState.openMouseTextInput == null) return;
        MouseTextInput mouseTextInput = uiEngineState.openMouseTextInput;
        char[] characters = mouseTextInput.upperCase ? mouseTextInput.charactersUC : mouseTextInput.charactersLC;

        int scrollDirection = 0;
        boolean mouse1Pressed = false;
        boolean mouse3Pressed = false;
        boolean mouse2Pressed = false;
        switch (uiEngineState.currentControlMode) {
            case HARDWARE_MOUSE -> {
                int deltaX = Gdx.input.getX() - uiEngineState.mTextInputMouseX;
                if (deltaX > 6) {
                    scrollDirection = 1;
                    uiEngineState.mTextInputMouseX = Gdx.input.getX();
                } else if (deltaX < -6) {
                    scrollDirection = -1;
                    uiEngineState.mTextInputMouseX = Gdx.input.getX();
                }
                if (uiEngineState.inputEvents.mouseDown) {
                    // Choke Events & Translate
                    int indexOfLeft = uiEngineState.inputEvents.mouseDownButtons.indexOf(KeyCode.Mouse.LEFT);
                    if (indexOfLeft != -1) {
                        uiEngineState.inputEvents.mouseButtonsDown[KeyCode.Mouse.LEFT] = false;
                        uiEngineState.inputEvents.mouseDownButtons.removeIndex(indexOfLeft);
                        uiEngineState.mTextInputTranslatedMouse1Down = true;
                    }
                    int indexOfRight = uiEngineState.inputEvents.mouseDownButtons.indexOf(KeyCode.Mouse.RIGHT);
                    if (indexOfRight != -1) {
                        uiEngineState.inputEvents.mouseButtonsDown[KeyCode.Mouse.RIGHT] = false;
                        uiEngineState.inputEvents.mouseDownButtons.removeIndex(indexOfRight);
                        uiEngineState.mTextInputTranslatedMouse2Down = true;
                    }
                    int indexOfMiddle = uiEngineState.inputEvents.mouseDownButtons.indexOf(KeyCode.Mouse.MIDDLE);
                    if (indexOfMiddle != -1) {
                        uiEngineState.inputEvents.mouseButtonsDown[KeyCode.Mouse.MIDDLE] = false;
                        uiEngineState.inputEvents.mouseDownButtons.removeIndex(indexOfMiddle);
                        uiEngineState.mTextInputTranslatedMouse3Down = true;
                    }
                    uiEngineState.inputEvents.mouseDown = uiEngineState.inputEvents.mouseDownButtons.size > 0;
                }
                if (uiEngineState.inputEvents.mouseUp) {
                    // Choke Events & Translate
                    int indexOfLeft = uiEngineState.inputEvents.mouseUpButtons.indexOf(KeyCode.Mouse.LEFT);
                    if (indexOfLeft != -1) {
                        uiEngineState.inputEvents.mouseUpButtons.removeIndex(indexOfLeft);
                        uiEngineState.mTextInputTranslatedMouse1Down = false;
                    }
                    int indexOfRight = uiEngineState.inputEvents.mouseUpButtons.indexOf(KeyCode.Mouse.RIGHT);
                    if (indexOfRight != -1) {
                        uiEngineState.inputEvents.mouseUpButtons.removeIndex(indexOfRight);
                        uiEngineState.mTextInputTranslatedMouse2Down = false;
                    }
                    int indexOfMiddle = uiEngineState.inputEvents.mouseUpButtons.indexOf(KeyCode.Mouse.MIDDLE);
                    if (indexOfMiddle != -1) {
                        uiEngineState.inputEvents.mouseUpButtons.removeIndex(indexOfMiddle);
                        uiEngineState.mTextInputTranslatedMouse3Down = false;
                    }
                    uiEngineState.inputEvents.mouseUp = uiEngineState.inputEvents.mouseUpButtons.size > 0;
                }
                mouse1Pressed = uiEngineState.mTextInputTranslatedMouse1Down;
                mouse2Pressed = uiEngineState.mTextInputTranslatedMouse2Down;
                mouse3Pressed = uiEngineState.mTextInputTranslatedMouse3Down;
            }
            case GAMEPAD -> {
                boolean stickLeft = uiEngineState.config.input_gamePadMouseStickLeftEnabled;
                boolean stickRight = uiEngineState.config.input_gamePadMouseStickRightEnabled;
                final float sensitivity = 0.4f;
                boolean leftGamePad = (stickLeft && uiEngineState.gamePadTranslatedStickLeft.x < -sensitivity) || (stickRight && uiEngineState.gamePadTranslatedStickRight.x < -sensitivity);
                boolean rightGamePad = (stickLeft && uiEngineState.gamePadTranslatedStickLeft.x > sensitivity) || (stickRight && uiEngineState.gamePadTranslatedStickRight.x > sensitivity);
                mouse1Pressed = mouseControl_isTranslatedKeyCodeDown(uiEngineState.gamePadTranslatedButtonsDown, uiEngineState.config.input_gamePadMouseButtonsMouse1);
                mouse2Pressed = mouseControl_isTranslatedKeyCodeDown(uiEngineState.gamePadTranslatedButtonsDown, uiEngineState.config.input_gamePadMouseButtonsMouse2);
                mouse3Pressed = mouseControl_isTranslatedKeyCodeDown(uiEngineState.gamePadTranslatedButtonsDown, uiEngineState.config.input_gamePadMouseButtonsMouse3);

                if (leftGamePad) {
                    if (!uiEngineState.mTextInputGamePadLeft) {
                        scrollDirection = -1;
                        uiEngineState.mTextInputGamePadLeft = true;
                    }
                } else {
                    uiEngineState.mTextInputGamePadLeft = false;
                }
                if (rightGamePad) {
                    if (!uiEngineState.mTextInputGamePadRight) {
                        scrollDirection = 1;
                        uiEngineState.mTextInputGamePadRight = true;
                    }
                } else {
                    uiEngineState.mTextInputGamePadRight = false;
                }

                // Continue Scroll
                if (leftGamePad || rightGamePad) {
                    uiEngineState.mTextInputScrollTimer++;
                    if (uiEngineState.mTextInputScrollTimer > uiEngineState.mTextInputScrollTime) {
                        uiEngineState.mTextInputGamePadLeft = false;
                        uiEngineState.mTextInputGamePadRight = false;
                        uiEngineState.mTextInputScrollTimer = 0;
                        uiEngineState.mTextInputScrollSpeed++;
                        if (uiEngineState.mTextInputScrollSpeed >= 3) {
                            uiEngineState.mTextInputScrollTime = 2;
                        } else if (uiEngineState.mTextInputScrollSpeed == 2) {
                            uiEngineState.mTextInputScrollTime = 5;
                        } else if (uiEngineState.mTextInputScrollSpeed == 1) {
                            uiEngineState.mTextInputScrollTime = 10;
                        }
                    }
                } else {
                    uiEngineState.mTextInputScrollTimer = 0;
                    uiEngineState.mTextInputScrollTime = 20;
                    uiEngineState.mTextInputScrollSpeed = 0;
                }
            }
            case KEYBOARD -> {
                // Not Needed since you are already using a keyboard to type
            }
        }

        // Unlock on first press
        if (!uiEngineState.mTextInputUnlock) {
            if (mouse1Pressed) {
                mouse1Pressed = false;
            } else {
                uiEngineState.mTextInputUnlock = true;
            }
        }

        // Scroll Forward/Backwards
        if (scrollDirection != 0) {
            mouseTextInput.selectedIndex = Math.clamp(mouseTextInput.selectedIndex + scrollDirection, 0, (characters.length - 1));
        }

        // Confirm Character from Input
        boolean enterRegularCharacter = false;
        boolean changeCaseMouse2 = false;
        boolean deleteCharacterMouse3 = false;
        if (mouse1Pressed && !uiEngineState.mTextInputMouse1Pressed) uiEngineState.mTextInputMouse1Pressed = true;
        if (!mouse1Pressed && uiEngineState.mTextInputMouse1Pressed) {
            enterRegularCharacter = true;
            uiEngineState.mTextInputMouse1Pressed = false;
        }

        if (mouse3Pressed && !uiEngineState.mTextInputMouse2Pressed) uiEngineState.mTextInputMouse2Pressed = true;
        if (!mouse3Pressed && uiEngineState.mTextInputMouse2Pressed) {
            // Change case from Mouse 2
            changeCaseMouse2 = true;
            uiEngineState.mTextInputMouse2Pressed = false;
        }

        if (mouse2Pressed && !uiEngineState.mTextInputMouse3Pressed) uiEngineState.mTextInputMouse3Pressed = true;
        if (!mouse2Pressed && uiEngineState.mTextInputMouse3Pressed) {
            // Delete from Mouse 3
            deleteCharacterMouse3 = true;
            uiEngineState.mTextInputMouse3Pressed = false;
        }

        // Confirm Character from API Queue
        if (!enterRegularCharacter && !uiEngineState.mTextInputAPICharacterQueue.isEmpty()) {
            UICommonUtils.mouseTextInput_selectCharacter(uiEngineState.openMouseTextInput, (char) uiEngineState.mTextInputAPICharacterQueue.removeIndex(uiEngineState.mTextInputAPICharacterQueue.size - 1));
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
                    uiEngineState.inputEvents.keyDown = true;
                    uiEngineState.inputEvents.keyDownKeyCodes.add(KeyCode.Key.BACKSPACE);
                    uiEngineState.inputEvents.keyUp = true;
                    uiEngineState.inputEvents.keyUpKeyCodes.add(KeyCode.Key.BACKSPACE);
                    if (mouseTextInput.mouseTextInputAction != null)
                        mouseTextInput.mouseTextInputAction.onDelete();
                }
                // Control Confirm
                case '\n' -> {
                    boolean close = mouseTextInput.mouseTextInputAction == null || mouseTextInput.mouseTextInputAction.onConfirm();
                    if (close) UICommonUtils.mouseTextInput_close(uiEngineState);
                }
                // Default Text Character
                default -> {
                    uiEngineState.inputEvents.keyTyped = true;
                    uiEngineState.inputEvents.keyTypedCharacters.add(c);
                    if (mouseTextInput.mouseTextInputAction != null)
                        mouseTextInput.mouseTextInputAction.onEnterCharacter(c);
                }
            }
        }
    }

    private void mouseControl_chokeAllMouseEvents() {
        // clear all mouse inputs
        uiEngineState.inputEvents.mouseMoved = false;
        uiEngineState.inputEvents.mouseDragged = false;
        uiEngineState.inputEvents.mouseUp = false;
        uiEngineState.inputEvents.mouseUpButtons.clear();
        uiEngineState.inputEvents.mouseDown = false;
        uiEngineState.inputEvents.mouseDownButtons.clear();
        uiEngineState.inputEvents.mouseDoubleClick = false;
        Arrays.fill(uiEngineState.inputEvents.mouseButtonsDown, false);
        uiEngineState.mouse_ui.x = 0;
        uiEngineState.mouse_ui.y = 0;
        uiEngineState.mouse_delta.x = 0;
        uiEngineState.mouse_delta.y = 0;
    }

    private boolean mouseControl_hardwareMouseDetectUse() {
        return uiEngineState.inputEvents.mouseDown || uiEngineState.inputEvents.mouseUp ||
                uiEngineState.inputEvents.mouseMoved || uiEngineState.inputEvents.mouseDragged || uiEngineState.inputEvents.mouseScrolled;
    }

    private int[] mouseControl_keyboardMouseGetButtons(int index) {
        return switch (index) {
            case 0 -> uiEngineState.config.input_keyboardMouseButtonsUp;
            case 1 -> uiEngineState.config.input_keyboardMouseButtonsDown;
            case 2 -> uiEngineState.config.input_keyboardMouseButtonsLeft;
            case 3 -> uiEngineState.config.input_keyboardMouseButtonsRight;
            case 4 -> uiEngineState.config.input_keyboardMouseButtonsMouse1;
            case 5 -> uiEngineState.config.input_keyboardMouseButtonsMouse2;
            case 6 -> uiEngineState.config.input_keyboardMouseButtonsMouse3;
            case 7 -> uiEngineState.config.input_keyboardMouseButtonsMouse4;
            case 8 -> uiEngineState.config.input_keyboardMouseButtonsMouse5;
            case 9 -> uiEngineState.config.input_keyboardMouseButtonsScrollUp;
            case 10 -> uiEngineState.config.input_keyboardMouseButtonsScrollDown;
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }


    private int[] mouseControl_gamePadMouseGetButtons(int index) {
        return switch (index) {
            case 0 -> uiEngineState.config.input_gamePadMouseButtonsMouse1;
            case 1 -> uiEngineState.config.input_gamePadMouseButtonsMouse2;
            case 2 -> uiEngineState.config.input_gamePadMouseButtonsMouse3;
            case 3 -> uiEngineState.config.input_gamePadMouseButtonsMouse4;
            case 4 -> uiEngineState.config.input_gamePadMouseButtonsMouse5;
            case 5 -> uiEngineState.config.input_gamePadMouseButtonsScrollUp;
            case 6 -> uiEngineState.config.input_gamePadMouseButtonsScrollDown;
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
                    if (uiEngineState.inputEvents.gamePadButtonDown) {
                        IntArray buttonDownKeyCodes = uiEngineState.inputEvents.gamePadButtonDownKeyCodes;
                        for (int ikc = buttonDownKeyCodes.size - 1; ikc >= 0; ikc--) {
                            if (buttonDownKeyCodes.get(ikc) == keyCode) {
                                buttonDownKeyCodes.removeIndex(ikc);
                                uiEngineState.inputEvents.gamePadButtonDown = !buttonDownKeyCodes.isEmpty();
                                uiEngineState.inputEvents.gamePadButtonsDown[keyCode] = false;
                                uiEngineState.gamePadTranslatedButtonsDown[keyCode] = true;
                                gamepadMouseUsed = true;
                            }
                        }
                    }
                    if (uiEngineState.inputEvents.gamePadButtonUp) {
                        IntArray upKeyCodes = uiEngineState.inputEvents.gamePadButtonUpKeyCodes;
                        for (int ikc = upKeyCodes.size - 1; ikc >= 0; ikc--) {
                            if (upKeyCodes.get(ikc) == keyCode) {
                                upKeyCodes.removeIndex(ikc);
                                uiEngineState.inputEvents.gamePadButtonUp = !upKeyCodes.isEmpty();
                                uiEngineState.gamePadTranslatedButtonsDown[keyCode] = false;
                                gamepadMouseUsed = true;
                            }
                        }
                    }
                }
            }
        }
        // Joystick Events Left
        if (uiEngineState.config.input_gamePadMouseStickLeftEnabled) {
            if (uiEngineState.inputEvents.gamePadLeftXMoved) {
                uiEngineState.gamePadTranslatedStickLeft.x = uiEngineState.inputEvents.gamePadLeftX;
                uiEngineState.inputEvents.gamePadLeftX = 0;
                uiEngineState.inputEvents.gamePadLeftXMoved = false;
                gamepadMouseUsed = true;
            }
            if (uiEngineState.inputEvents.gamePadLeftYMoved) {
                uiEngineState.gamePadTranslatedStickLeft.y = uiEngineState.inputEvents.gamePadLeftY;
                uiEngineState.inputEvents.gamePadLeftY = 0;
                uiEngineState.inputEvents.gamePadLeftYMoved = false;
                gamepadMouseUsed = true;
            }
        } else {
            uiEngineState.gamePadTranslatedStickLeft.x = 0;
            uiEngineState.gamePadTranslatedStickLeft.y = 0;
        }
        // Joystick Events Right
        if (uiEngineState.config.input_gamePadMouseStickRightEnabled) {
            if (uiEngineState.inputEvents.gamePadRightXMoved) {
                uiEngineState.gamePadTranslatedStickRight.x = uiEngineState.inputEvents.gamePadRightX;
                uiEngineState.inputEvents.gamePadRightX = 0;
                uiEngineState.inputEvents.gamePadRightXMoved = false;
                gamepadMouseUsed = true;
            }
            if (uiEngineState.inputEvents.gamePadRightYMoved) {
                uiEngineState.gamePadTranslatedStickRight.y = uiEngineState.inputEvents.gamePadRightY;
                uiEngineState.inputEvents.gamePadRightY = 0;
                uiEngineState.inputEvents.gamePadRightYMoved = false;
                gamepadMouseUsed = true;
            }
        } else {
            uiEngineState.gamePadTranslatedStickRight.x = 0;
            uiEngineState.gamePadTranslatedStickRight.y = 0;
        }

        return gamepadMouseUsed;
    }

    private boolean mouseControl_keyboardMouseTranslateAndChokeEvents() {
        if (uiEngineState.focusedTextField != null) return false; // Disable during Textfield Input
        boolean keyboardMouseUsed = false;
        // Remove Key down input events and set to temporary variable keyBoardTranslatedKeysDown
        for (int i = 0; i <= 10; i++) {
            int[] buttons = mouseControl_keyboardMouseGetButtons(i);
            if (buttons != null) {
                if (uiEngineState.inputEvents.keyDown) {
                    for (int i2 = 0; i2 < buttons.length; i2++) {
                        int keyCode = buttons[i2];
                        IntArray downKeyCodes = uiEngineState.inputEvents.keyDownKeyCodes;
                        for (int ikc = downKeyCodes.size - 1; ikc >= 0; ikc--) {
                            int downKeyCode = downKeyCodes.get(ikc);
                            if (downKeyCode == keyCode) {
                                downKeyCodes.removeIndex(ikc);
                                uiEngineState.inputEvents.keyDown = !downKeyCodes.isEmpty();
                                uiEngineState.inputEvents.keysDown[keyCode] = false;
                                uiEngineState.keyBoardTranslatedKeysDown[keyCode] = true;
                                keyboardMouseUsed = true;
                            }

                        }
                    }
                }
                if (uiEngineState.inputEvents.keyUp) {
                    for (int i2 = 0; i2 < buttons.length; i2++) {
                        int keyCode = buttons[i2];
                        IntArray upKeyCodes = uiEngineState.inputEvents.keyUpKeyCodes;
                        for (int ikc = upKeyCodes.size - 1; ikc >= 0; ikc--) {
                            int upKeyCode = upKeyCodes.get(ikc);
                            if (upKeyCode == keyCode) {
                                upKeyCodes.removeIndex(ikc);
                                uiEngineState.inputEvents.keyUp = !upKeyCodes.isEmpty();
                                uiEngineState.keyBoardTranslatedKeysDown[keyCode] = false;
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
            cursorChangeX *= uiEngineState.config.input_emulatedMouseCursorSpeed;
            cursorChangeY *= uiEngineState.config.input_emulatedMouseCursorSpeed;
            if (buttonLeft) deltaX -= cursorChangeX;
            if (buttonRight) deltaX += cursorChangeX;
            if (buttonUp) deltaY -= cursorChangeY;
            if (buttonDown) deltaY += cursorChangeY;
        }

        // Set to final
        uiEngineState.mouse_emulated.x = Math.clamp(uiEngineState.mouse_emulated.x + deltaX, 0, uiEngineState.resolutionWidth);
        uiEngineState.mouse_emulated.y = Math.clamp(uiEngineState.mouse_emulated.y - deltaY, 0, uiEngineState.resolutionHeight);
        uiEngineState.mouse_delta.x = deltaX;
        uiEngineState.mouse_delta.y = -deltaY;
        uiEngineState.mouse_ui.x = MathUtils.round(uiEngineState.mouse_emulated.x);
        uiEngineState.mouse_ui.y = MathUtils.round(uiEngineState.mouse_emulated.y);

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
            if (uiEngineState.emulatedMouseIsButtonDown[i] != buttonMouseDown) {
                uiEngineState.emulatedMouseIsButtonDown[i] = buttonMouseDown;
                if (uiEngineState.emulatedMouseIsButtonDown[i]) {
                    uiEngineState.inputEvents.mouseDown = true;
                    uiEngineState.inputEvents.mouseDownButtons.add(i);
                    anyButtonChanged = true;
                    if (i == Input.Buttons.LEFT) {
                        // DoubleClick
                        if ((System.currentTimeMillis() - uiEngineState.emulatedMouseLastMouseClick) < UIInputProcessor.DOUBLE_CLICK_TIME) {
                            uiEngineState.inputEvents.mouseDoubleClick = true;
                        }
                        uiEngineState.emulatedMouseLastMouseClick = System.currentTimeMillis();
                    }

                } else {
                    uiEngineState.inputEvents.mouseUp = true;
                    uiEngineState.inputEvents.mouseUpButtons.add(i);
                    anyButtonChanged = true;
                }
            }
            uiEngineState.inputEvents.mouseButtonsDown[i] = uiEngineState.emulatedMouseIsButtonDown[i];
        }
        if (!anyButtonChanged) {
            uiEngineState.inputEvents.mouseDown = false;
            uiEngineState.inputEvents.mouseUp = false;
            uiEngineState.inputEvents.mouseDoubleClick = false;
            uiEngineState.inputEvents.mouseDownButtons.clear();
            uiEngineState.inputEvents.mouseUpButtons.clear();
        }

        // Simluate Mouse Move Events
        if (deltaX != 0 || deltaY != 0) {
            uiEngineState.inputEvents.mouseMoved = true;
            uiEngineState.inputEvents.mouseDragged = false;
            draggedLoop:
            for (int i = 0; i <= 4; i++) {
                if (uiEngineState.emulatedMouseIsButtonDown[i]) {
                    uiEngineState.inputEvents.mouseDragged = true;
                    uiEngineState.inputEvents.mouseMoved = false;
                    break;
                }
            }
        } else {
            uiEngineState.inputEvents.mouseDragged = false;
            uiEngineState.inputEvents.mouseMoved = false;
        }

        // Simluate Mouse Scroll Events
        uiEngineState.inputEvents.mouseScrolled = buttonScrolledUp || buttonScrolledDown;
        uiEngineState.inputEvents.mouseScrolledAmount = buttonScrolledUp ? -1 : buttonScrolledDown ? 1 : 0;
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
        boolean[] translatedButtons = uiEngineState.gamePadTranslatedButtonsDown;
        boolean stickLeft = uiEngineState.config.input_gamePadMouseStickLeftEnabled;
        boolean stickRight = uiEngineState.config.input_gamePadMouseStickRightEnabled;

        float joystickDeadZone = uiEngineState.config.input_gamePadMouseJoystickDeadZone;
        boolean buttonLeft = (stickLeft && uiEngineState.gamePadTranslatedStickLeft.x < -joystickDeadZone) || (stickRight && uiEngineState.gamePadTranslatedStickRight.x < -joystickDeadZone);
        boolean buttonRight = (stickLeft && uiEngineState.gamePadTranslatedStickLeft.x > joystickDeadZone) || (stickRight && uiEngineState.gamePadTranslatedStickRight.x > joystickDeadZone);
        boolean buttonUp = (stickLeft && uiEngineState.gamePadTranslatedStickLeft.y > joystickDeadZone) || (stickRight && uiEngineState.gamePadTranslatedStickRight.y > joystickDeadZone);
        boolean buttonDown = (stickLeft && uiEngineState.gamePadTranslatedStickLeft.y < -joystickDeadZone) || (stickRight && uiEngineState.gamePadTranslatedStickRight.y < -joystickDeadZone);
        boolean buttonMouse1Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input_gamePadMouseButtonsMouse1);
        boolean buttonMouse2Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input_gamePadMouseButtonsMouse2);
        boolean buttonMouse3Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input_gamePadMouseButtonsMouse3);
        boolean buttonMouse4Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input_gamePadMouseButtonsMouse4);
        boolean buttonMouse5Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input_gamePadMouseButtonsMouse5);
        boolean buttonScrolledUp = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input_gamePadMouseButtonsScrollUp);
        boolean buttonScrolledDown = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input_gamePadMouseButtonsScrollDown);

        float cursorChangeX = 0f;
        if (buttonLeft || buttonRight) {
            cursorChangeX = Math.max(Math.abs(uiEngineState.gamePadTranslatedStickLeft.x), Math.abs(uiEngineState.gamePadTranslatedStickRight.x));
            cursorChangeX = (cursorChangeX - joystickDeadZone) / (1f - joystickDeadZone);
        }
        float cursorChangeY = 0f;
        if (buttonUp || buttonDown) {
            cursorChangeY = Math.max(Math.abs(uiEngineState.gamePadTranslatedStickLeft.y), Math.abs(uiEngineState.gamePadTranslatedStickRight.y));
            cursorChangeY = (cursorChangeY - joystickDeadZone) / (1f - joystickDeadZone);
        }
        // Translate to mouse events
        mouseControl_emulateMouseEvents(buttonLeft, buttonRight, buttonUp, buttonDown,
                buttonMouse1Down, buttonMouse2Down, buttonMouse3Down, buttonMouse4Down, buttonMouse5Down,
                buttonScrolledUp, buttonScrolledDown, cursorChangeX, cursorChangeY
        );
    }

    private void mouseControl_updateKeyBoardMouse() {
        if (uiEngineState.focusedTextField != null) return; // Disable during Textfield Input

        // Swallow & Translate keyboard events
        boolean[] translatedKeys = uiEngineState.keyBoardTranslatedKeysDown;

        boolean buttonLeft = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input_keyboardMouseButtonsLeft);
        boolean buttonRight = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input_keyboardMouseButtonsRight);
        boolean buttonUp = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input_keyboardMouseButtonsUp);
        boolean buttonDown = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input_keyboardMouseButtonsDown);
        boolean buttonMouse1Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input_keyboardMouseButtonsMouse1);
        boolean buttonMouse2Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input_keyboardMouseButtonsMouse2);
        boolean buttonMouse3Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input_keyboardMouseButtonsMouse3);
        boolean buttonMouse4Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input_keyboardMouseButtonsMouse4);
        boolean buttonMouse5Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input_keyboardMouseButtonsMouse5);
        boolean buttonScrolledUp = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input_keyboardMouseButtonsScrollUp);
        boolean buttonScrolledDown = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input_keyboardMouseButtonsScrollDown);

        final float SPEEDUP_SPEED = 0.1f;
        if (buttonLeft || buttonRight) {
            uiEngineState.keyBoardMouseSpeedUp.x = Math.clamp(uiEngineState.keyBoardMouseSpeedUp.x < 1f ? uiEngineState.keyBoardMouseSpeedUp.x + SPEEDUP_SPEED : uiEngineState.keyBoardMouseSpeedUp.x, 0f, 1f);
        } else {
            uiEngineState.keyBoardMouseSpeedUp.set(0, uiEngineState.keyBoardMouseSpeedUp.y);
        }
        if (buttonUp || buttonDown) {
            uiEngineState.keyBoardMouseSpeedUp.y = Math.clamp(uiEngineState.keyBoardMouseSpeedUp.y < 1f ? uiEngineState.keyBoardMouseSpeedUp.y + SPEEDUP_SPEED : uiEngineState.keyBoardMouseSpeedUp.y, 0f, 1f);
        } else {
            uiEngineState.keyBoardMouseSpeedUp.set(uiEngineState.keyBoardMouseSpeedUp.x, 0);
        }

        // Translate to mouse events
        mouseControl_emulateMouseEvents(buttonLeft, buttonRight, buttonUp, buttonDown,
                buttonMouse1Down, buttonMouse2Down, buttonMouse3Down, buttonMouse4Down, buttonMouse5Down,
                buttonScrolledUp, buttonScrolledDown, uiEngineState.keyBoardMouseSpeedUp.x, uiEngineState.keyBoardMouseSpeedUp.y
        );
    }

    private void mouseControl_enforceUIMouseBounds() {
        uiEngineState.mouse_ui.x = Math.clamp(uiEngineState.mouse_ui.x, 0, uiEngineState.resolutionWidth);
        uiEngineState.mouse_ui.y = Math.clamp(uiEngineState.mouse_ui.y, 0, uiEngineState.resolutionHeight);
    }

    private void mouseControl_updateGameMouseXY() {
        // MouseXGUI/MouseYGUI -> To MouseX/MouseY
        uiEngineState.vector_fboCursor.x = uiEngineState.mouse_ui.x;
        uiEngineState.vector_fboCursor.y = Gdx.graphics.getHeight() - uiEngineState.mouse_ui.y;
        uiEngineState.vector_fboCursor.z = 1;
        uiEngineState.camera_app.unproject(uiEngineState.vector_fboCursor, 0, 0, uiEngineState.resolutionWidth, uiEngineState.resolutionHeight);
        this.uiEngineState.mouse_app.x = (int) uiEngineState.vector_fboCursor.x;
        this.uiEngineState.mouse_app.y = (int) uiEngineState.vector_fboCursor.y;
    }

    private void mouseControl_updateHardwareMouse() {
        // --- GUI CURSOR ---
        // ScreenCursor To WorldCursor
        uiEngineState.vector2_unproject.x = Gdx.input.getX();
        uiEngineState.vector2_unproject.y = Gdx.input.getY();

        uiEngineState.viewport_screen.unproject(uiEngineState.vector2_unproject);
        // WorldCursor to  FBOCursor
        uiEngineState.vector_fboCursor.x = uiEngineState.vector2_unproject.x;
        uiEngineState.vector_fboCursor.y = Gdx.graphics.getHeight() - uiEngineState.vector2_unproject.y;
        uiEngineState.vector_fboCursor.z = 1;
        uiEngineState.camera_ui.unproject(uiEngineState.vector_fboCursor, 0, 0, uiEngineState.resolutionWidth, uiEngineState.resolutionHeight);

        // Set to final
        uiEngineState.mouse_delta.x = MathUtils.round(uiEngineState.vector_fboCursor.x - uiEngineState.mouse_ui.x);
        uiEngineState.mouse_delta.y = MathUtils.round(uiEngineState.vector_fboCursor.y - uiEngineState.mouse_ui.y);
        uiEngineState.mouse_ui.x = Math.clamp(MathUtils.round(uiEngineState.vector_fboCursor.x), 0, uiEngineState.resolutionWidth);
        uiEngineState.mouse_ui.y = Math.clamp(MathUtils.round(uiEngineState.vector_fboCursor.y), 0, uiEngineState.resolutionHeight);
    }

    private void mouseControl_updateLastUIMouseHover() {
        uiEngineState.lastUIMouseHover = UICommonUtils.component_getUIObjectAtPosition(uiEngineState, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y);
    }


    private void updateMouseCursor() {
        /* Update Cursor*/
        if (uiEngineState.lastUIMouseHover != null) {
            // 1. GUI Cursor
            uiEngineState.cursor = uiEngineState.config.ui_cursor;
            uiEngineState.cursorArrayIndex = 0;
        } else {
            // 2. Manually overidden Cursor
            if (uiEngineState.displayOverrideCursor) {
                uiEngineState.cursor = uiEngineState.overrideCursor;
                uiEngineState.cursorArrayIndex = uiEngineState.overrideCursorArrayIndex;
                uiEngineState.displayOverrideCursor = false;
            } else {
                if (uiEngineState.mouseTool != null) {
                    // 3. Mouse Tool cursor
                    if (uiEngineState.inputEvents.mouseButtonsDown[Input.Buttons.LEFT]) {
                        uiEngineState.cursor = uiEngineState.mouseTool.cursorDown;
                    } else {
                        uiEngineState.cursor = uiEngineState.mouseTool.cursor;
                    }
                    uiEngineState.cursorArrayIndex = uiEngineState.mouseTool.cursorArrayIndex;
                } else {
                    // no mouse tool set - display no cursor
                    uiEngineState.cursor = null;
                }
            }
        }

    }

    private void updateUI_keyInteractions() {
        UICommonUtils.setKeyboardInteractedUIObject(uiEngineState, null);
        if (uiEngineState.config.ui_keyInteractionsDisabled) return;

        if (uiEngineState.inputEvents.keyTyped) {
            boolean processKeyTyped = true;
            Textfield focusedTextField = uiEngineState.focusedTextField;
            if (focusedTextField == null) processKeyTyped = false;

            if (processKeyTyped) {
                // Into Temp variable because focuseTextField can change after executing actions
                for (int ic = 0; ic < uiEngineState.inputEvents.keyTypedCharacters.size; ic++) {
                    char keyTypedCharacter = (char) uiEngineState.inputEvents.keyTypedCharacters.get(ic);
                    UICommonUtils.textField_typeCharacter(uiEngineState, mediaManager, focusedTextField, keyTypedCharacter);
                }
                // MouseTextInput open = focus on last typed character
                if (uiEngineState.openMouseTextInput != null) {
                    char typedChar = (char) uiEngineState.inputEvents.keyTypedCharacters.get(uiEngineState.inputEvents.keyTypedCharacters.size - 1);
                    UICommonUtils.mouseTextInput_selectCharacter(uiEngineState.openMouseTextInput, typedChar);
                }
                UICommonUtils.setKeyboardInteractedUIObject(uiEngineState, focusedTextField);
            }

        }
        if (uiEngineState.inputEvents.keyDown) {
            boolean processKeyDown = true;
            Textfield focusedTextField = uiEngineState.focusedTextField;
            if (focusedTextField == null) processKeyDown = false;

            if (focusedTextField != null) {
                // TextField Control Keys
                for (int ik = 0; ik < uiEngineState.inputEvents.keyDownKeyCodes.size; ik++) {
                    int keyDownKeyCode = uiEngineState.inputEvents.keyDownKeyCodes.get(ik);
                    if (UICommonUtils.textField_isControlKey(keyDownKeyCode)) {
                        // Repeat certain Control Keys
                        if (UICommonUtils.textField_isRepeatedControlKey(keyDownKeyCode)) {
                            uiEngineState.focusedTextField_repeatedKey = keyDownKeyCode;
                            uiEngineState.focusedTextField_repeatedKeyTimer = System.currentTimeMillis();
                        }
                        UICommonUtils.textField_executeControlKey(uiEngineState, mediaManager, focusedTextField, keyDownKeyCode);
                    }
                    UICommonUtils.setKeyboardInteractedUIObject(uiEngineState, focusedTextField);
                }
            } else {
                // Hotkeys
                for (int ihk = 0; ihk < uiEngineState.hotKeys.size(); ihk++) {
                    HotKey hotKey = uiEngineState.hotKeys.get(ihk);
                    if (!hotKey.pressed) {
                        boolean hotKeyPressed = true;
                        for (int ikc = 0; ikc < hotKey.keyCodes.length; ikc++) {
                            if (!uiEngineState.inputEvents.keysDown[hotKey.keyCodes[ikc]]) {
                                hotKeyPressed = false;
                                break;
                            }
                        }
                        if (hotKeyPressed) UICommonUtils.hotkey_press(hotKey);
                    }
                }
            }
        }
        if (uiEngineState.inputEvents.keyUp) {

            // Hotkeys
            for (int ik = 0; ik < uiEngineState.inputEvents.keyUpKeyCodes.size; ik++) {
                int keyUpKeyCode = uiEngineState.inputEvents.keyUpKeyCodes.get(ik);
                // Reset RepeatKey
                if (UICommonUtils.textField_isRepeatedControlKey(keyUpKeyCode)) {
                    uiEngineState.focusedTextField_repeatedKey = KeyCode.NONE;
                    uiEngineState.focusedTextField_repeatedKeyTimer = 0;
                }
                // Reset Hotkeys
                for (int ihk = 0; ihk < uiEngineState.hotKeys.size(); ihk++) {
                    HotKey hotKey = uiEngineState.hotKeys.get(ihk);
                    if (hotKey.pressed) {
                        hkLoop:
                        for (int ikc = 0; ikc < hotKey.keyCodes.length; ikc++) {
                            if (hotKey.keyCodes[ikc] == keyUpKeyCode) {
                                hotKey.pressed = false;
                                if (hotKey.hotKeyAction != null) UICommonUtils.hotkey_release(hotKey);
                                break hkLoop;
                            }
                        }
                    }
                }
            }
        }
    }


    private void updateUI_mouseInteractions() {
        UICommonUtils.setMouseInteractedUIObject(uiEngineState, null);
        if (uiEngineState.config.ui_mouseInteractionsDisabled) return;
        // ------ MOUSE DOUBLE CLICK ------
        if (uiEngineState.inputEvents.mouseDoubleClick) {
            boolean processMouseDoubleClick = true;
            Object lastUIMouseHover = uiEngineState.lastUIMouseHover;
            if (lastUIMouseHover != null) {
                if (UICommonUtils.window_isModalOpen(uiEngineState) && lastUIMouseHover != uiEngineState.modalWindow) {
                    processMouseDoubleClick = false;
                }
            } else {
                processMouseDoubleClick = false;
            }

            if (processMouseDoubleClick) {
                if (lastUIMouseHover instanceof Window window) {
                    for (int ib = 0; ib < uiEngineState.inputEvents.mouseDownButtons.size; ib++) {
                        int mouseDownButton = uiEngineState.inputEvents.mouseDownButtons.get(ib);
                        if (uiEngineState.config.ui_foldWindowsOnDoubleClick && mouseDownButton == Input.Buttons.LEFT) {
                            if (window.hasTitleBar && Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y, window.x, window.y + TS(window.height - 1), TS(window.width), TS())) {
                                if (window.folded) {
                                    UICommonUtils.window_unFold(window);
                                } else {
                                    UICommonUtils.window_fold(window);
                                }
                            }
                        }
                    }
                }

                // Execute Common Actions
                for (int ib = 0; ib < uiEngineState.inputEvents.mouseDownButtons.size; ib++) {
                    int mouseDownButton = uiEngineState.inputEvents.mouseDownButtons.get(ib);
                    actions_executeOnMouseDoubleClickCommonAction(lastUIMouseHover, mouseDownButton);
                }

                UICommonUtils.setMouseInteractedUIObject(uiEngineState, lastUIMouseHover);
            }
        }
        // ------ MOUSE DOWN ------
        if (uiEngineState.inputEvents.mouseDown) {
            boolean processMouseClick = true;
            Object lastUIMouseHover = uiEngineState.lastUIMouseHover;
            /* Modal ? */
            if (lastUIMouseHover != null) {
                if (UICommonUtils.window_isModalOpen(uiEngineState)) {
                    /* Modal Active? */
                    if (lastUIMouseHover instanceof Window window) {
                        if (window != uiEngineState.modalWindow) processMouseClick = false;
                    } else if (lastUIMouseHover instanceof Component component) {
                        if (component.addedToWindow == null) {
                            processMouseClick = false;
                        } else if (component.addedToWindow != uiEngineState.modalWindow) {
                            processMouseClick = false;
                        }
                    }
                } else {
                    /* Hidden ? */
                    if (lastUIMouseHover instanceof Window window) {
                        if (!window.visible) processMouseClick = false;
                    } else if (lastUIMouseHover instanceof Component component) {
                        if (component.addedToWindow != null && !component.addedToWindow.visible)
                            processMouseClick = false;
                    }
                }
            } else {
                processMouseClick = false;
            }

            if (processMouseClick) {
                Window moveWindow = null;
                boolean isMouseLeftButton = uiEngineState.inputEvents.mouseButtonsDown[Input.Buttons.LEFT];
                if (isMouseLeftButton) {
                    // Mouse Action
                    switch (lastUIMouseHover) {
                        case Window window -> {
                            if (window.moveAble) moveWindow = window;
                        }
                        case Button button -> {
                            uiEngineState.pressedButton = button;
                            switch (button.mode) {
                                case DEFAULT -> UICommonUtils.button_press(button);
                                case TOGGLE -> UICommonUtils.button_toggle(button);
                            }
                        }
                        case ContextmenuItem contextMenuItem -> {
                            uiEngineState.pressedContextMenuItem = contextMenuItem;
                        }
                        case ScrollbarVertical scrollBarVertical -> {
                            UICommonUtils.scrollBar_pressButton(scrollBarVertical);
                            UICommonUtils.scrollBar_scroll(scrollBarVertical,
                                    UICommonUtils.scrollBar_calculateScrolled(uiEngineState, scrollBarVertical, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y));
                            uiEngineState.pressedScrollBarVertical = scrollBarVertical;
                        }
                        case ScrollbarHorizontal scrollBarHorizontal -> {
                            UICommonUtils.scrollBar_pressButton(scrollBarHorizontal);
                            UICommonUtils.scrollBar_scroll(scrollBarHorizontal,
                                    UICommonUtils.scrollBar_calculateScrolled(uiEngineState, scrollBarHorizontal, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y));
                            uiEngineState.pressedScrollBarHorizontal = scrollBarHorizontal;
                        }
                        case Combobox comboBox -> {
                            if (UICommonUtils.comboBox_isOpen(uiEngineState, comboBox)) {
                                if (Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y,
                                        UICommonUtils.component_getAbsoluteX(comboBox), UICommonUtils.component_getAbsoluteY(comboBox),
                                        TS(comboBox.width), TS())) {
                                    // Clicked on Combobox itself -> close
                                    UICommonUtils.comboBox_close(uiEngineState, comboBox);
                                } else {
                                    // Clicked on Item
                                    for (int i = 0; i < comboBox.comboBoxItems.size(); i++) {
                                        if (Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y,
                                                UICommonUtils.component_getAbsoluteX(comboBox),
                                                UICommonUtils.component_getAbsoluteY(comboBox) - TS(i) - TS(),
                                                TS(comboBox.width),
                                                TS())) {
                                            uiEngineState.pressedComboBoxItem = comboBox.comboBoxItems.get(i);
                                        }
                                    }
                                }


                            } else {
                                // Open this combobox
                                UICommonUtils.comboBox_open(uiEngineState, comboBox);
                            }
                        }
                        case Knob knob -> {
                            uiEngineState.pressedKnob = knob;
                            if (knob.knobAction != null) knob.knobAction.onPress();
                        }
                        case Canvas canvas -> {
                            if (canvas.canvasAction != null) canvas.canvasAction.onPress(
                                    UICommonUtils.component_getRelativeMouseX(uiEngineState.mouse_ui.x, canvas),
                                    UICommonUtils.component_getRelativeMouseY(uiEngineState.mouse_ui.y, canvas));
                            uiEngineState.pressedCanvas = canvas;
                        }
                        case AppViewport appViewPort -> {
                            if (appViewPort.appViewPortAction != null) appViewPort.appViewPortAction.onPress(
                                    UICommonUtils.component_getRelativeMouseX(uiEngineState.mouse_ui.x, appViewPort),
                                    UICommonUtils.component_getRelativeMouseY(uiEngineState.mouse_ui.y, appViewPort));
                            uiEngineState.pressedAppViewPort = appViewPort;
                        }
                        case Textfield textField -> {
                            uiEngineState.pressedTextFieldMouseX = UICommonUtils.component_getRelativeMouseX(uiEngineState.mouse_ui.x, textField);
                            uiEngineState.pressedTextField = textField;
                        }
                        case Grid grid -> {
                            int tileSize = grid.doubleSized ? TS2() : TS();
                            int x_grid = UICommonUtils.component_getAbsoluteX(grid);
                            int y_grid = UICommonUtils.component_getAbsoluteY(grid);
                            int inv_x = (uiEngineState.mouse_ui.x - x_grid) / tileSize;
                            int inv_y = (uiEngineState.mouse_ui.y - y_grid) / tileSize;
                            if (UICommonUtils.grid_positionValid(grid, inv_x, inv_y)) {
                                Object pressedGridItem = grid.items[inv_x][inv_y];
                                if (pressedGridItem != null && grid.dragEnabled) {
                                    uiEngineState.draggedGridFrom.x = inv_x;
                                    uiEngineState.draggedGridFrom.y = inv_y;
                                    uiEngineState.draggedGridOffset.x = uiEngineState.mouse_ui.x - (x_grid + (inv_x * tileSize));
                                    uiEngineState.draggedGridOffset.y = uiEngineState.mouse_ui.y - (y_grid + (inv_y * tileSize));
                                    uiEngineState.draggedGridItem = grid.items[inv_x][inv_y];
                                    uiEngineState.draggedGrid = grid;
                                }
                                uiEngineState.pressedGrid = grid;
                                uiEngineState.pressedGridItem = pressedGridItem;
                            }
                        }
                        case List list -> {
                            UICommonUtils.list_updateItemInfoAtMousePosition(uiEngineState, list);
                            Object pressedListItem = null;
                            if (uiEngineState.itemInfo_listValid) {
                                pressedListItem = (uiEngineState.itemInfo_listIndex < list.items.size()) ? list.items.get(uiEngineState.itemInfo_listIndex) : null;
                            }
                            if (pressedListItem != null && list.dragEnabled) {
                                uiEngineState.draggedListFromIndex = uiEngineState.itemInfo_listIndex;
                                uiEngineState.draggedListOffsetX.x = uiEngineState.mouse_ui.x - (UICommonUtils.component_getAbsoluteX(list));
                                uiEngineState.draggedListOffsetX.y = (uiEngineState.mouse_ui.y - UICommonUtils.component_getAbsoluteY(list)) % 8;
                                uiEngineState.draggedListItem = pressedListItem;
                                uiEngineState.draggedList = list;
                            }
                            uiEngineState.pressedList = list;
                            uiEngineState.pressedListItem = pressedListItem;
                        }
                        case Tabbar tabBar -> {
                            UICommonUtils.tabBar_updateItemInfoAtMousePosition(uiEngineState, tabBar);
                            if (uiEngineState.itemInfo_tabBarValid) {
                                if (tabBar.selectedTab != uiEngineState.itemInfo_tabBarTabIndex)
                                    UICommonUtils.tabBar_selectTab(tabBar, uiEngineState.itemInfo_tabBarTabIndex);
                            } else {
                                if (tabBar.addedToWindow != null) moveWindow = tabBar.addedToWindow;
                            }
                        }
                        case Checkbox checkBox -> {
                            uiEngineState.pressedCheckBox = checkBox;
                        }
                        case null, default -> {
                        }
                    }

                    // Additonal Actions
                    // -> Bring clicked window to top
                    if (moveWindow != null) {
                        uiEngineState.draggedWindow = moveWindow;
                        uiEngineState.draggedWindow_offset.x = uiEngineState.mouse_ui.x - uiEngineState.draggedWindow.x;
                        uiEngineState.draggedWindow_offset.y = uiEngineState.mouse_ui.y - uiEngineState.draggedWindow.y;
                        // Move on top ?
                        UICommonUtils.window_bringToFront(uiEngineState, uiEngineState.draggedWindow);
                    }

                    // Unfocus focused textfields
                    if (uiEngineState.focusedTextField != null && lastUIMouseHover != uiEngineState.focusedTextField) {
                        UICommonUtils.textField_unFocus(uiEngineState, uiEngineState.focusedTextField);
                    }
                }

                // Close opened ComboBoxes
                if (uiEngineState.openComboBox != null && lastUIMouseHover != uiEngineState.openComboBox) {
                    UICommonUtils.comboBox_close(uiEngineState, uiEngineState.openComboBox);
                }

                // Close opened ContextMenus
                if (uiEngineState.openContextMenu != null) {
                    if (!(lastUIMouseHover instanceof ContextmenuItem contextMenuItem) || contextMenuItem.addedToContextMenu != uiEngineState.openContextMenu) {
                        UICommonUtils.contextMenu_close(uiEngineState, uiEngineState.openContextMenu);
                    }
                }

                // Execute Common Actions
                for (int ib = 0; ib < uiEngineState.inputEvents.mouseDownButtons.size; ib++) {
                    int mouseDownButton = uiEngineState.inputEvents.mouseDownButtons.get(ib);
                    actions_executeOnMouseClickCommonAction(lastUIMouseHover, mouseDownButton);
                }

                UICommonUtils.setMouseInteractedUIObject(uiEngineState, lastUIMouseHover);
            }
        }
        // ------ MOUSE UP ------
        if (uiEngineState.inputEvents.mouseUp) {
            Object lastUIMouseHover = uiEngineState.lastUIMouseHover;
            boolean processMouseUpPressed = true;
            boolean processMouseUpDragged = true;
            // Press interaction
            Object pressedUIObject = UICommonUtils.getPressedUIReference(uiEngineState);
            if (pressedUIObject == null) processMouseUpPressed = false;
            // Drag interaction
            Object draggedUIObject = UICommonUtils.getDraggedUIReference(uiEngineState);
            if (draggedUIObject == null) processMouseUpDragged = false;


            if (processMouseUpPressed) {
                switch (pressedUIObject) {
                    case Canvas canvas -> {
                        if (canvas.canvasAction != null) canvas.canvasAction.onRelease();
                        UICommonUtils.resetPressedCanvasReference(uiEngineState);
                    }
                    case ContextmenuItem contextMenuItem -> {
                        UICommonUtils.contextMenu_selectItem(uiEngineState, contextMenuItem);
                        UICommonUtils.contextMenu_close(uiEngineState, contextMenuItem.addedToContextMenu);
                    }
                    case ComboboxItem comboBoxItem -> {
                        UICommonUtils.comboBox_selectItem(uiEngineState, comboBoxItem);
                        if (uiEngineState.currentControlMode.emulated && comboBoxItem.addedToComboBox != null) {
                            // emulated: move mouse back to combobox on item select
                            uiEngineState.mouse_emulated.y = UICommonUtils.component_getAbsoluteY(comboBoxItem.addedToComboBox) + TS_HALF();
                        }
                        UICommonUtils.resetPressedComboBoxItemReference(uiEngineState);
                    }
                    case Checkbox checkBox -> {
                        checkBox.checked = !checkBox.checked;
                        if (checkBox.checkBoxAction != null) checkBox.checkBoxAction.onCheck(checkBox.checked);
                        UICommonUtils.resetPressedCheckBoxReference(uiEngineState);
                    }
                    case Textfield textField -> {
                        // Set Marker to mouse position
                        int mouseX = uiEngineState.pressedTextFieldMouseX;
                        char[] fieldContent = textField.content.substring(textField.offset).toCharArray();
                        String testString = "";
                        boolean found = false;
                        charLoop:
                        for (int i = 0; i < fieldContent.length; i++) {
                            testString += fieldContent[i];
                            if (render_textWidth(textField.font, testString) > mouseX) {
                                UICommonUtils.textField_setMarkerPosition(uiEngineState, mediaManager, textField,
                                        textField.offset + i);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            // Set to end
                            UICommonUtils.textField_setMarkerPosition(uiEngineState, mediaManager, textField,
                                    textField.offset + fieldContent.length);
                        }
                        // Set Focus
                        UICommonUtils.textField_focus(uiEngineState, textField);
                        UICommonUtils.resetPressedTextFieldReference(uiEngineState);
                    }
                    case AppViewport appViewPort -> {
                        if (appViewPort.appViewPortAction != null)
                            appViewPort.appViewPortAction.onRelease();
                        UICommonUtils.resetPressedAppViewPortReference(uiEngineState);
                    }
                    case Button button -> {
                        UICommonUtils.button_release(button);
                        UICommonUtils.resetPressedButtonReference(uiEngineState);
                    }
                    case ScrollbarVertical scrollBarVertical -> {
                        UICommonUtils.scrollBar_releaseButton(scrollBarVertical);
                        UICommonUtils.resetPressedScrollBarVerticalReference(uiEngineState);
                    }
                    case ScrollbarHorizontal scrollBarHorizontal -> {
                        UICommonUtils.scrollBar_releaseButton(scrollBarHorizontal);
                        UICommonUtils.resetPressedScrollBarHorizontalReference(uiEngineState);
                    }
                    case Knob knob -> {
                        if (knob.knobAction != null) knob.knobAction.onRelease();
                        UICommonUtils.resetPressedKnobReference(uiEngineState);
                    }
                    case Grid grid -> {
                        boolean select = grid.gridAction != null ? grid.gridAction.onItemSelected(uiEngineState.pressedGridItem) : true;
                        if (uiEngineState.pressedGridItem != null) {
                            if (select) {
                                if (grid.multiSelect) {
                                    if (!grid.selectedItems.contains(uiEngineState.pressedGridItem)) {
                                        grid.selectedItems.add(uiEngineState.pressedGridItem);
                                    } else {
                                        grid.selectedItems.remove(uiEngineState.pressedGridItem);
                                    }
                                    grid.selectedItem = null;
                                } else {
                                    grid.selectedItems.clear();
                                    grid.selectedItem = uiEngineState.pressedGridItem;
                                }
                            }
                        } else {
                            if (select) {
                                grid.selectedItems.clear();
                                grid.selectedItem = null;
                            }
                        }
                        UICommonUtils.resetPressedGridReference(uiEngineState);
                    }
                    case List list -> {
                        boolean select = list.listAction != null ? list.listAction.onItemSelected(uiEngineState.pressedListItem) : true;
                        if (uiEngineState.pressedListItem != null) {
                            if (select) {
                                if (list.multiSelect) {
                                    if (!list.selectedItems.contains(uiEngineState.pressedListItem)) {
                                        list.selectedItems.add(uiEngineState.pressedListItem);
                                    } else {
                                        list.selectedItems.remove(uiEngineState.pressedListItem);
                                    }
                                    list.selectedItem = null;
                                } else {
                                    list.selectedItems.clear();
                                    list.selectedItem = uiEngineState.pressedListItem;
                                }
                            }
                        } else {
                            if (select) {
                                list.selectedItems.clear();
                                list.selectedItem = null;
                            }
                        }
                        UICommonUtils.resetPressedListReference(uiEngineState);
                    }
                    case null, default -> {
                    }
                }
                UICommonUtils.setMouseInteractedUIObject(uiEngineState, pressedUIObject);
            }
            if (processMouseUpDragged) {
                switch (draggedUIObject) {
                    case Window __ -> {
                        uiEngineState.draggedWindow_offset.set(0, 0);
                        uiEngineState.draggedWindow = null;
                    }
                    case List list -> {
                        int dragFromIndex = uiEngineState.draggedListFromIndex;
                        Object dragItem = uiEngineState.draggedListItem;
                        if (lastUIMouseHover != null) {
                            if (lastUIMouseHover instanceof List hoverList) {
                                if (UICommonUtils.list_canDragIntoList(uiEngineState, hoverList)) {
                                    UICommonUtils.list_updateItemInfoAtMousePosition(uiEngineState, hoverList);
                                    if (uiEngineState.itemInfo_listValid) {
                                        int toIndex = uiEngineState.itemInfo_listIndex;
                                        if (hoverList.listAction != null)
                                            hoverList.listAction.onDragFromList(list, dragFromIndex, toIndex);
                                    }
                                }
                            } else if (lastUIMouseHover instanceof Grid hoverGrid) {
                                if (UICommonUtils.grid_canDragIntoGrid(uiEngineState, hoverGrid)) {
                                    UICommonUtils.grid_updateItemInfoAtMousePosition(uiEngineState, hoverGrid);
                                    if (uiEngineState.itemInfo_gridValid) {
                                        if (hoverGrid.gridAction != null)
                                            hoverGrid.gridAction.onDragFromList(list, dragFromIndex,
                                                    uiEngineState.itemInfo_gridPos.x, uiEngineState.itemInfo_gridPos.y);
                                    }
                                }
                            }
                        } else if (UICommonUtils.list_canDragIntoScreen(list)) {
                            if (list.listAction != null) list.listAction.onDragIntoApp(
                                    dragItem,
                                    dragFromIndex,
                                    uiEngineState.mouse_ui.x,
                                    uiEngineState.mouse_ui.y
                            );
                        }
                        // reset
                        UICommonUtils.resetDraggedListReference(uiEngineState);
                    }
                    case Grid grid -> {
                        int dragFromX = uiEngineState.draggedGridFrom.x;
                        int dragFromY = uiEngineState.draggedGridFrom.y;
                        Object dragItem = uiEngineState.draggedGridItem;
                        if (lastUIMouseHover != null) {
                            if (lastUIMouseHover instanceof Grid hoverGrid) {
                                if (UICommonUtils.grid_canDragIntoGrid(uiEngineState, hoverGrid)) {
                                    UICommonUtils.grid_updateItemInfoAtMousePosition(uiEngineState, hoverGrid);
                                    if (uiEngineState.itemInfo_gridValid) {
                                        if (hoverGrid.gridAction != null)
                                            hoverGrid.gridAction.onDragFromGrid(grid,
                                                    dragFromX, dragFromY,
                                                    uiEngineState.itemInfo_gridPos.x, uiEngineState.itemInfo_gridPos.y);
                                    }
                                }
                            } else if (uiEngineState.lastUIMouseHover instanceof List hoverList) {
                                if (UICommonUtils.list_canDragIntoList(uiEngineState, hoverList)) {
                                    UICommonUtils.list_updateItemInfoAtMousePosition(uiEngineState, hoverList);
                                    if (uiEngineState.itemInfo_listValid) {
                                        int toIndex = uiEngineState.itemInfo_listIndex;
                                        if (hoverList.listAction != null)
                                            hoverList.listAction.onDragFromGrid(grid, dragFromX, dragFromY, toIndex);
                                    }
                                }
                            }
                        } else if (UICommonUtils.grid_canDragIntoScreen(grid)) {
                            if (grid.gridAction != null)
                                grid.gridAction.onDragIntoApp(
                                        dragItem,
                                        dragFromX, dragFromY,
                                        uiEngineState.mouse_ui.x,
                                        uiEngineState.mouse_ui.y
                                );
                        }
                        // reset
                        UICommonUtils.resetDraggedGridReference(uiEngineState);
                    }
                    case null, default -> {
                    }
                }

                UICommonUtils.setMouseInteractedUIObject(uiEngineState, draggedUIObject);
            }

        }
        // ------ MOUSE DRAGGED ------
        if (uiEngineState.inputEvents.mouseDragged) {
            Object lastUIMouseHover = uiEngineState.lastUIMouseHover;
            boolean processMouseDraggedPressed = true;
            boolean processMouseDraggedDragged = true;
            // Press interaction
            Object pressedUIObject = UICommonUtils.getPressedUIReference(uiEngineState);
            if (pressedUIObject == null) processMouseDraggedPressed = false;
            // Drag interaction
            Object draggedUIObject = UICommonUtils.getDraggedUIReference(uiEngineState);
            if (draggedUIObject == null) processMouseDraggedDragged = false;

            if (processMouseDraggedPressed) {
                switch (pressedUIObject) {
                    case ScrollbarVertical scrolledScrollBarVertical -> {
                        UICommonUtils.scrollBar_scroll(scrolledScrollBarVertical, UICommonUtils.scrollBar_calculateScrolled(uiEngineState, scrolledScrollBarVertical, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y));
                    }
                    case ScrollbarHorizontal scrolledScrollBarHorizontal -> {
                        UICommonUtils.scrollBar_scroll(scrolledScrollBarHorizontal, UICommonUtils.scrollBar_calculateScrolled(uiEngineState, scrolledScrollBarHorizontal, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y));
                    }
                    case Knob turnedKnob -> {
                        float amount = (uiEngineState.mouse_delta.y / 100f) * uiEngineState.config.component_knobSensitivity;
                        float newValue = turnedKnob.turned + amount;
                        UICommonUtils.knob_turnKnob(turnedKnob, newValue);
                        if (uiEngineState.currentControlMode.emulated) {
                            // emulated: keep mouse position steady
                            UICommonUtils.emulatedMouse_setPositionComponent(uiEngineState, turnedKnob);
                        }
                    }
                    case null, default -> {
                    }
                }
                UICommonUtils.setMouseInteractedUIObject(uiEngineState, pressedUIObject);
            }
            if (processMouseDraggedDragged) {
                switch (draggedUIObject) {
                    case Window draggedWindow -> {
                        UICommonUtils.window_setPosition(uiEngineState, draggedWindow,
                                uiEngineState.mouse_ui.x - uiEngineState.draggedWindow_offset.x,
                                uiEngineState.mouse_ui.y - uiEngineState.draggedWindow_offset.y);

                        if (draggedWindow.windowAction != null)
                            draggedWindow.windowAction.onMove(draggedWindow.x, draggedWindow.y);
                    }
                    case null, default -> {
                    }
                }
                UICommonUtils.setMouseInteractedUIObject(uiEngineState, draggedUIObject);
            }

        }
        // ------ MOUSE MOVED ------
        if (uiEngineState.inputEvents.mouseMoved) {

        }
        // ------ MOUSE SCROLLED ------
        if (uiEngineState.inputEvents.mouseScrolled) {
            boolean processMouseScrolled = true;
            Object lastUIMouseHover = uiEngineState.lastUIMouseHover;
            if (lastUIMouseHover == null) processMouseScrolled = false;

            if (processMouseScrolled) {
                switch (lastUIMouseHover) {
                    case List list -> {
                        int size = list.items != null ? list.items.size() : 0;
                        float amount = (1 / (float) Math.max(size, 1)) * uiEngineState.inputEvents.mouseScrolledAmount;
                        UICommonUtils.list_scroll(list, list.scrolled + amount);
                    }
                    case Knob knob -> {
                        float amount = ((-1 / 20f) * uiEngineState.inputEvents.mouseScrolledAmount) * uiEngineState.config.component_knobSensitivity;
                        float newValue = knob.turned + amount;
                        UICommonUtils.knob_turnKnob(knob, newValue);
                    }
                    case ScrollbarHorizontal scrollBarHorizontal -> {
                        float amount = ((-1 / 20f) * uiEngineState.inputEvents.mouseScrolledAmount) * uiEngineState.config.component_scrollbarSensitivity;
                        UICommonUtils.scrollBar_scroll(scrollBarHorizontal, scrollBarHorizontal.scrolled + amount);
                    }
                    case ScrollbarVertical scrollBarVertical -> {
                        float amount = ((-1 / 20f) * uiEngineState.inputEvents.mouseScrolledAmount) * uiEngineState.config.component_scrollbarSensitivity;
                        UICommonUtils.scrollBar_scroll(scrollBarVertical, scrollBarVertical.scrolled + amount);
                    }
                    case null, default -> {
                    }
                }

                // Execute Common Actions
                actions_executeOnMouseScrollCommonAction(lastUIMouseHover, uiEngineState.inputEvents.mouseScrolledAmount);

                UICommonUtils.setMouseInteractedUIObject(uiEngineState, lastUIMouseHover);
            }
        }
    }

    private void updateUI_continuousComponentActivities() {
        // TextField Repeat
        if (uiEngineState.focusedTextField_repeatedKey != KeyCode.NONE) {
            long time = (System.currentTimeMillis() - uiEngineState.focusedTextField_repeatedKeyTimer);
            if (time > 500) {
                UICommonUtils.textField_executeControlKey(uiEngineState, mediaManager, uiEngineState.focusedTextField, uiEngineState.focusedTextField_repeatedKey);
            }
        }
    }

    private void updateUI() {

        updateUI_animationTimer();

        updateUI_mouseInteractions();

        updateUI_keyInteractions();

        updateUI_continuousComponentActivities();

        updateUI_executeUpdateActions();

        updateUI_notifications();

        updateUI_toolTip();

    }

    private void updateUI_animationTimer() {
        if (uiEngineState.config.ui_animationTimerFunction != null) {
            uiEngineState.config.ui_animationTimerFunction.updateAnimationTimer();
        }
    }

    private void updateUI_executeUpdateActions() {
        // for(int i) is used to avoid iterator creation and avoid concurrentModification
        // If UpdateActions are removing/adding other update actions they are caught on the next update/frame

        // ScreenComponent UpdateActions
        long currentTimeMillis = System.currentTimeMillis();
        for (int i = 0; i < uiEngineState.screenComponents.size(); i++) {
            Component component = uiEngineState.screenComponents.get(i);
            for (int i2 = 0; i2 < component.updateActions.size(); i2++) {
                actions_executeUpdateAction(component.updateActions.get(i2), currentTimeMillis);
            }
        }
        for (int i = 0; i < uiEngineState.windows.size(); i++) {
            // Window UpdateActions
            Window window = uiEngineState.windows.get(i);
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
        for (int i = 0; i < uiEngineState.singleUpdateActions.size(); i++) {
            UpdateAction updateAction = uiEngineState.singleUpdateActions.get(i);
            if (this.actions_executeUpdateAction(updateAction, currentTimeMillis)) {
                uiEngineState.singleUpdateActionsRemoveQ.push(updateAction);
            }
        }
        UpdateAction removeUpdateAction;
        while ((removeUpdateAction = uiEngineState.singleUpdateActionsRemoveQ.pollFirst()) != null) {
            uiEngineState.singleUpdateActions.remove(removeUpdateAction);
        }
    }

    private void updateUI_toolTip() {
        // Anything dragged ?
        boolean showComponentToolTip = uiEngineState.draggedList == null && uiEngineState.draggedGrid == null;

        // hovering over a component ?
        if (showComponentToolTip) {
            showComponentToolTip = (uiEngineState.lastUIMouseHover instanceof Component);
        }
        // modal active and component does not belong to modal ?
        if (showComponentToolTip) {
            showComponentToolTip = uiEngineState.modalWindow == null || ((Component) uiEngineState.lastUIMouseHover).addedToWindow == uiEngineState.modalWindow;
        }
        if (showComponentToolTip) {
            Component hoverComponent = (Component) uiEngineState.lastUIMouseHover;
            Object toolTipSubItem = null;
            if (hoverComponent instanceof List list) {
                if (list.listAction != null) {
                    UICommonUtils.list_updateItemInfoAtMousePosition(uiEngineState, list);
                    if (uiEngineState.itemInfo_listValid) {
                        toolTipSubItem = uiEngineState.itemInfo_listIndex < list.items.size() ? list.items.get(uiEngineState.itemInfo_listIndex) : null;
                    }
                }
            } else if (hoverComponent instanceof Grid grid) {
                int tileSize = grid.doubleSized ? TS2() : TS();
                if (grid.gridAction != null) {
                    int x_grid = UICommonUtils.component_getAbsoluteX(grid);
                    int y_grid = UICommonUtils.component_getAbsoluteY(grid);
                    int inv_x = (uiEngineState.mouse_ui.x - x_grid) / tileSize;
                    int inv_y = (uiEngineState.mouse_ui.y - y_grid) / tileSize;
                    if (UICommonUtils.grid_positionValid(grid, inv_x, inv_y)) {
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
                    updateComponentToolTip = uiEngineState.tooltip_lastHoverObject != toolTipSubItem;
                } else {
                    // Check on component change
                    updateComponentToolTip = uiEngineState.tooltip_lastHoverObject != hoverComponent;
                }
            }

            if (updateComponentToolTip) {
                uiEngineState.tooltip_wait_delay = true;
                uiEngineState.tooltip_delay_timer = 0;
                if (hoverComponent instanceof List list) {
                    // check for list item tooltips
                    uiEngineState.tooltip = list.listAction.toolTip(toolTipSubItem);
                    uiEngineState.tooltip_lastHoverObject = toolTipSubItem;
                } else if (hoverComponent instanceof Grid grid && toolTipSubItem != null) {
                    // check for Grid item tooltip
                    uiEngineState.tooltip = grid.gridAction.toolTip(toolTipSubItem);
                    uiEngineState.tooltip_lastHoverObject = toolTipSubItem;
                } else {
                    // take component tooltip
                    uiEngineState.tooltip = hoverComponent.toolTip;
                    uiEngineState.tooltip_lastHoverObject = hoverComponent;
                }
            }
        } else {
            // Set App Tooltip
            if (uiEngineState.lastUIMouseHover == null && uiEngineState.appToolTip != null) {
                if (uiEngineState.tooltip != uiEngineState.appToolTip) {
                    uiEngineState.tooltip_wait_delay = true;
                    uiEngineState.tooltip_delay_timer = 0;
                    uiEngineState.tooltip = uiEngineState.appToolTip;
                }
            } else {
                uiEngineState.tooltip = null;
                uiEngineState.tooltip_lastHoverObject = null;
            }
        }


        // Fade In
        if (uiEngineState.tooltip != null) {
            if (uiEngineState.tooltip_wait_delay) {
                uiEngineState.tooltip_delay_timer += uiEngineState.config.tooltip_FadeInDelay;
                if (uiEngineState.tooltip_delay_timer >= uiEngineState.config.tooltip_FadeInDelay) {
                    uiEngineState.tooltip_wait_delay = false;
                    uiEngineState.tooltip_delay_timer = 0;
                    uiEngineState.tooltip_fadePct = 0f;
                    if (uiEngineState.tooltip.toolTipAction != null)
                        uiEngineState.tooltip.toolTipAction.onDisplay();
                }
            } else if (uiEngineState.tooltip_fadePct < 1f) {
                uiEngineState.tooltip_fadePct = Math.clamp(uiEngineState.tooltip_fadePct + uiEngineState.config.tooltip_FadeInSpeed, 0f, 1f);
            } else {
                if (uiEngineState.tooltip.toolTipAction != null) {
                    uiEngineState.tooltip.toolTipAction.onUpdate();
                }
            }

            uiEngineState.fadeOutTooltip = uiEngineState.tooltip;
        } else {
            if (uiEngineState.fadeOutTooltip != null) {
                if (uiEngineState.tooltip_fadePct > 0f) {
                    uiEngineState.tooltip_fadePct = Math.clamp(uiEngineState.tooltip_fadePct - uiEngineState.config.tooltip_FadeOutSpeed, 0f, 1f);
                } else {
                    uiEngineState.fadeOutTooltip = null;
                }
            }
        }
    }


    private void updateUI_notifications() {
        if (uiEngineState.notifications.size() > 0) {
            Notification notification = uiEngineState.notifications.getFirst();
            switch (notification.state) {
                case INIT_SCROLL -> {
                    notification.timer = 0;
                    notification.state = STATE_NOTIFICATION.SCROLL;
                }
                case INIT_DISPLAY -> {
                    notification.timer = 0;
                    notification.state = STATE_NOTIFICATION.DISPLAY;
                }
                case SCROLL -> {
                    notification.timer++;
                    if (notification.timer > 30) {
                        notification.scroll += MathUtils.round(uiEngineState.config.notification_scrollSpeed);
                        if (notification.scroll >= notification.scrollMax) {
                            notification.timer = 0;
                            notification.state = STATE_NOTIFICATION.DISPLAY;
                        }
                        notification.timer = 30;
                    }
                }
                case DISPLAY -> {
                    notification.timer++;
                    if (notification.timer > notification.displayTime) {
                        notification.timer = 0;
                        notification.state = STATE_NOTIFICATION.FADEOUT;
                    }
                }
                case FADEOUT -> {
                    notification.timer++;
                    if (notification.timer > uiEngineState.config.notification_fadeoutTime) {
                        notification.timer = 0;
                        notification.state = STATE_NOTIFICATION.FINISHED;
                        UICommonUtils.notification_removeFromScreen(uiEngineState, notification);
                    }
                }
                case FINISHED -> {
                }
            }
        }
    }


    private void actions_executeOnMouseClickCommonAction(Object uiObject, int button) {
        if (uiObject == null) return;
        CommonActions commonActions = actions_getUIObjectCommonActions(uiObject);
        if (commonActions != null) commonActions.onMouseClick(button);
        if (uiObject instanceof Component component) {
            // Execute for parent window too
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
            case Combobox comboBox -> comboBox.comboBoxAction;
            case AppViewport appViewPort -> appViewPort.appViewPortAction;
            case Image image -> image.imageAction;
            case Grid grid -> grid.gridAction;
            case List list -> list.listAction;
            case Canvas canvas -> canvas.canvasAction;
            case ScrollbarVertical scrollBarVertical -> scrollBarVertical.scrollBarAction;
            case ScrollbarHorizontal scrollBarHorizontal -> scrollBarHorizontal.scrollBarAction;
            case Tabbar tabBar -> tabBar.tabBarAction;
            case Text text -> text.textAction;
            case Textfield textField -> textField.textFieldAction;
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


    public void render() {
        render(true);
    }

    public void render(boolean drawToScreen) {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        // Draw App
        {
            // Draw Main FrameBuffer
            uiEngineState.frameBuffer_app.begin();
            this.uiAdapter.render(uiEngineState.camera_app, null);
            uiEngineState.frameBuffer_app.end();
            // Draw UI AppViewport FrameBuffers
            for (int i = 0; i < this.uiEngineState.appViewPorts.size(); i++) {
                renderGameViewPortFrameBuffer(uiEngineState.appViewPorts.get(i));
            }
        }


        { // Draw GUI
            uiEngineState.frameBuffer_ui.begin();
            Gdx.gl.glClearColor(0, 0, 0, 0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            this.uiAdapter.renderUIBefore(uiEngineState.camera_ui);
            this.renderUI();
            this.uiAdapter.renderUIAfter(uiEngineState.camera_ui);
            uiEngineState.frameBuffer_ui.end();
        }

        { // Draw to Screen Buffer, Combine GUI+App Buffer and Upscale
            uiEngineState.frameBuffer_screen.begin();
            this.uiAdapter.renderComposite(uiEngineState.camera_ui,
                    spriteRenderer,
                    uiEngineState.frameBuffer_app.getFlippedTextureRegion(), uiEngineState.frameBuffer_ui.getFlippedTextureRegion(),
                    uiEngineState.resolutionWidth, uiEngineState.resolutionHeight,
                    UICommonUtils.window_isModalOpen(uiEngineState)
            );
            uiEngineState.frameBuffer_screen.end();
        }

        {
            // Draw to Screen
            if (drawToScreen) {
                uiEngineState.viewport_screen.apply();
                spriteRenderer.begin();
                Gdx.gl.glClearColor(0, 0, 0, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                spriteRenderer.draw(uiEngineState.frameBuffer_screen.getFlippedTextureRegion(), 0, 0, uiEngineState.resolutionWidth, uiEngineState.resolutionHeight);
                spriteRenderer.end();
            }
        }


    }


    private void renderGameViewPortFrameBuffer(AppViewport appViewPort) {
        if (render_isComponentNotRendered(appViewPort)) return;
        if (System.currentTimeMillis() - appViewPort.updateTimer > appViewPort.updateTime) {
            // draw to frambuffer
            appViewPort.frameBuffer.begin();
            this.uiAdapter.render(appViewPort.camera, appViewPort);
            appViewPort.frameBuffer.end();
            appViewPort.updateTimer = System.currentTimeMillis();
        }
    }


    private void renderUI() {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final PrimitiveRenderer primitiveRenderer = uiEngineState.primitiveRenderer_ui;

        spriteRenderer.setProjectionMatrix(uiEngineState.camera_ui.combined);
        primitiveRenderer.setProjectionMatrix(uiEngineState.camera_ui.combined);

        spriteRenderer.begin();

        // Draw Screen Components
        for (int i = 0; i < uiEngineState.screenComponents.size(); i++) {
            Component component = uiEngineState.screenComponents.get(i);
            render_drawComponent(component);
        }

        // Draw Screen Components Top Layer
        for (int i = 0; i < uiEngineState.screenComponents.size(); i++) {
            Component component = uiEngineState.screenComponents.get(i);
            render_drawComponentTopLayer(component);
        }

        // Draw Windows
        for (int i = 0; i < uiEngineState.windows.size(); i++) {
            Window window = uiEngineState.windows.get(i);
            render_drawWindow(window);
        }

        // Notifications
        render_drawNotifications();

        // Context Menu
        render_drawContextMenu();

        // Tooltip
        render_drawTooltip();

        // OnScreenTextInput
        render_mouseTextInput();

        // Cursor
        render_drawCursorDragAndDrop();

        render_drawCursor();

        spriteRenderer.end();

        spriteRenderer.setTweakAndColorReset();
        primitiveRenderer.setTweakAndColorReset();
    }


    private void render_mouseTextInput() {
        if (uiEngineState.openMouseTextInput == null) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        MouseTextInput mouseTextInput = uiEngineState.openMouseTextInput;
        Color color = uiEngineState.openMouseTextInput.color;
        Color color2 = uiEngineState.openMouseTextInput.color2;
        final int CHARACTERS = 4;
        char[] chars = mouseTextInput.upperCase ? mouseTextInput.charactersUC : mouseTextInput.charactersLC;

        // 4 to the left
        for (int i = 1; i <= CHARACTERS; i++) {
            int index = mouseTextInput.selectedIndex - i;
            if (index >= 0 && index < chars.length) {
                render_mouseTextInputCharacter(mouseTextInput.font, chars[index], mouseTextInput.x - (i * 12), mouseTextInput.y - ((i * i) / 2), color, mouseTextInput.upperCase, false);
            }
        }
        // 4 to the right
        for (int i = 1; i <= CHARACTERS; i++) {
            int index = mouseTextInput.selectedIndex + i;
            if (index >= 0 && index < chars.length) {
                render_mouseTextInputCharacter(mouseTextInput.font, chars[index], mouseTextInput.x + (i * 12), mouseTextInput.y - ((i * i) / 2), color, mouseTextInput.upperCase, false);
            }
        }
        // 1 in center
        render_mouseTextInputCharacter(mouseTextInput.font, chars[mouseTextInput.selectedIndex], mouseTextInput.x, mouseTextInput.y, color, mouseTextInput.upperCase, uiEngineState.mTextInputMouse1Pressed);

        // Selection
        spriteRenderer.setColor(color2, color.a);
        spriteRenderer.drawCMediaImage(UIEngineBaseMedia_8x8.UI_OSTEXTINPUT_SELECTED, mouseTextInput.x - 1, mouseTextInput.y - 1);
        spriteRenderer.setTweakAndColorReset();
    }

    private void render_mouseTextInputCharacter(CMediaFont font, char c, int x, int y, Color color, boolean upperCase, boolean pressed) {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        spriteRenderer.setColor(color);

        int pressedIndex = pressed ? 1 : 0;
        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_OSTEXTINPUT_CHARACTER, x, y, pressedIndex);
        if (c == '\n') {
            spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_OSTEXTINPUT_CONFIRM, x, y, pressedIndex);
        }
        if (c == '\t') {
            CMediaArray caseGraphic = upperCase ? UIEngineBaseMedia_8x8.UI_OSTEXTINPUT_UPPERCASE : UIEngineBaseMedia_8x8.UI_OSTEXTINPUT_LOWERCASE;
            spriteRenderer.drawCMediaArray(caseGraphic, x, y, pressedIndex);
        }
        if (c == '\b') {
            spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_OSTEXTINPUT_DELETE, x, y, pressedIndex);
        } else {
            int offset = pressed ? 1 : 0;
            render_drawFont(font, String.valueOf(c), 1.0f, x + 2 + offset, y + 2 - offset);
        }

        spriteRenderer.setTweakAndColorReset();
    }

    private void render_drawCursor() {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        if (uiEngineState.cursor != null) {
            int center_x = mediaManager.getCMediaSpriteWidth(uiEngineState.cursor) / 2;
            int center_y = mediaManager.getCMediaSpriteHeight(uiEngineState.cursor) / 2;
            spriteRenderer.drawCMediaSprite(uiEngineState.cursor,
                    (uiEngineState.mouse_ui.x - center_x), (uiEngineState.mouse_ui.y - center_y), 0, UICommonUtils.ui_getAnimationTimer(uiEngineState));
        }
        spriteRenderer.setTweakAndColorReset();
    }


    private boolean render_isComponentNotRendered(Component component) {
        if (!component.visible) return true;
        if (component.addedToWindow != null && !component.addedToWindow.visible) return true;
        return UICommonUtils.component_isHiddenByTab(component);
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

    private int render_getListDragCMediaIndex(int x, int width) {
        return x == 0 ? 0 : x == (width - 1) ? 2 : 1;
    }


    private int render_getWindowCMediaIndex(int x, int y, int width, int height, boolean hasTitleBar) {
        if (hasTitleBar && y == (height - 1)) {
            if (x == 0) {
                return 12;
            } else if (x == (width - 1)) {
                return 14;
            }
            return 13;
        }
        return render_get16TilesCMediaIndex(x, y, width, height);
    }

    private int render_get16TilesCMediaIndex(int x, int y, int width, int height) {
        if (width == 1 && height == 1) return 3;
        if (width == 1) {
            if (y == 0) return 7;
            if (y == height - 1) return 15;
            return 11;
        } else if (height == 1) {
            if (x == 0) return 12;
            if (x == width - 1) return 14;
            return 13;
        } else {
            if (x == 0 && y == 0) return 8;
            if (x == width - 1 && y == height - 1) return 2;
            if (x == width - 1 && y == 0) return 10;
            if (x == 0 && y == height - 1) return 0;
            if (x == 0) return 4;
            if (x == width - 1) return 6;
            if (y == 0) return 9;
            if (y == height - 1) return 1;
            return 5;
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

    private float componentAlpha(Component component) {
        return (component.addedToWindow != null ? (component.color.a * component.addedToWindow.color.a) : component.color.a);
    }

    private void render_drawComponentTopLayer(Component component) {
        if (render_isComponentNotRendered(component)) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final float componentAlpha = componentAlpha(component);
        final boolean componentGrayScale = component.disabled || (UICommonUtils.window_isModalOpen(uiEngineState) && uiEngineState.modalWindow != component.addedToWindow);
        ;

        render_setColor(spriteRenderer, component.color, componentAlpha, componentGrayScale);

        switch (component) {
            case Combobox comboBox -> {
                // Menu
                if (UICommonUtils.comboBox_isOpen(uiEngineState, comboBox)) {
                    int width = comboBox.width;
                    int height = comboBox.comboBoxItems.size();
                    /* Menu */
                    for (int iy = 0; iy < height; iy++) {
                        ComboboxItem comboBoxItem = comboBox.comboBoxItems.get(iy);
                        for (int ix = 0; ix < width; ix++) {
                            int index = render_get9TilesCMediaIndex(ix, iy, width, height);//x==0 ? 0 : (x == (width-1)) ? 2 : 1;
                            CMediaArray comboMenuGraphic;
                            if (Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y, UICommonUtils.component_getAbsoluteX(comboBox), UICommonUtils.component_getAbsoluteY(comboBox) - TS() - TS(iy), TS(comboBox.width), TS())) {
                                comboMenuGraphic = UIEngineBaseMedia_8x8.UI_COMBOBOX_LIST_SELECTED;
                            } else {
                                comboMenuGraphic = UIEngineBaseMedia_8x8.UI_COMBOBOX_LIST;
                            }
                            spriteRenderer.saveState();
                            render_setColor(spriteRenderer, comboBoxItem.color, componentAlpha, componentGrayScale);
                            spriteRenderer.drawCMediaArray(comboMenuGraphic, UICommonUtils.component_getAbsoluteX(comboBox) + TS(ix), UICommonUtils.component_getAbsoluteY(comboBox) - TS(iy) - TS(), index);
                            spriteRenderer.loadState();
                        }
                    }

                    /* Text */
                    for (int i = 0; i < comboBox.comboBoxItems.size(); i++) {
                        ComboboxItem comboBoxItem = comboBox.comboBoxItems.get(i);
                        render_drawFont(comboBoxItem.font, comboBoxItem.text, componentAlpha, UICommonUtils.component_getAbsoluteX(comboBox), UICommonUtils.component_getAbsoluteY(comboBox) - TS(i) - TS(), 2, 1, TS(comboBox.width), comboBoxItem.icon, comboBoxItem.iconIndex);
                    }
                }
            }
            default -> {
            }
        }
        spriteRenderer.setTweakAndColorReset();
    }

    private void render_drawContextMenu() {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final boolean contextMenuGrayScale = UICommonUtils.window_isModalOpen(uiEngineState);

        if (uiEngineState.openContextMenu != null) {
            float alpha = uiEngineState.openContextMenu.color_a;

            Contextmenu contextMenu = uiEngineState.openContextMenu;
            int width = uiEngineState.displayedContextMenuWidth;
            int height = contextMenu.items.size();


            /* Menu */
            for (int iy = 0; iy < height; iy++) {
                ContextmenuItem contextMenuItem = contextMenu.items.get(iy);
                for (int ix = 0; ix < width; ix++) {
                    int index = render_get9TilesCMediaIndex(ix, iy, width, height);//x==0 ? 0 : (x == (width-1)) ? 2 : 1;
                    CMediaArray contextMenuGraphic;
                    if (Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y, contextMenu.x, contextMenu.y - TS() - TS(iy), TS(uiEngineState.displayedContextMenuWidth), TS())) {
                        contextMenuGraphic = UIEngineBaseMedia_8x8.UI_CONTEXT_MENU_SELECTED;
                    } else {
                        contextMenuGraphic = UIEngineBaseMedia_8x8.UI_CONTEXT_MENU;
                    }
                    spriteRenderer.saveState();
                    render_setColor(spriteRenderer, contextMenuItem.color, alpha, contextMenuGrayScale);
                    spriteRenderer.drawCMediaArray(contextMenuGraphic, contextMenu.x + TS(ix), contextMenu.y - TS(iy) - TS(), index);
                    spriteRenderer.loadState();
                }
            }

            /* Text */
            for (int iy = 0; iy < contextMenu.items.size(); iy++) {
                ContextmenuItem item = contextMenu.items.get(iy);
                render_drawFont(item.font, item.text, alpha, contextMenu.x, contextMenu.y - TS(iy) - TS(), 2, 1, TS(width), item.icon, item.iconIndex);
            }

        }


        spriteRenderer.setTweakAndColorReset();
    }

    private void render_drawTooltip() {
        Tooltip tooltip = uiEngineState.fadeOutTooltip != null ? uiEngineState.fadeOutTooltip : uiEngineState.tooltip;
        if (tooltip == null) return;
        if (tooltip.segments.isEmpty()) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final PrimitiveRenderer primitiveRenderer = uiEngineState.primitiveRenderer_ui;
        ArrayList<TooltipSegment> segments = tooltip.segments;

        // Determine Dimensions
        int tooltip_width = tooltip.minWidth;
        int tooltip_height = 0;
        for (int is = 0; is < segments.size(); is++) {
            TooltipSegment segment = segments.get(is);
            tooltip_width = Math.max(tooltip_width, segment.width);
            if (!segment.merge) tooltip_height += segment.height;
        }
        if (tooltip_width == 0 || tooltip_height == 0) return;
        // Determine Position

        int lineLengthAbs = TS(tooltip.lineLength);

        DIRECTION direction = switch (tooltip.direction) {
            case RIGHT ->
                    uiEngineState.mouse_ui.x + lineLengthAbs > uiEngineState.resolutionWidth - TS(tooltip_width) ? DIRECTION.LEFT : DIRECTION.RIGHT;
            case LEFT ->
                    uiEngineState.mouse_ui.x - TS(tooltip_width + tooltip.lineLength) < 0 ? DIRECTION.RIGHT : DIRECTION.LEFT;
            case UP ->
                    uiEngineState.mouse_ui.y + lineLengthAbs > uiEngineState.resolutionHeight - TS(tooltip_height) ? DIRECTION.DOWN : DIRECTION.UP;
            case DOWN -> uiEngineState.mouse_ui.y - TS(tooltip_height) < 0 ? DIRECTION.UP : DIRECTION.DOWN;
        };

        int tooltip_x = switch (direction) {
            case RIGHT ->
                    Math.clamp(uiEngineState.mouse_ui.x + lineLengthAbs, 0, uiEngineState.resolutionWidth - TS(tooltip_width));
            case LEFT ->
                    Math.clamp(uiEngineState.mouse_ui.x - TS(tooltip_width + tooltip.lineLength), 0, uiEngineState.resolutionWidth - TS(tooltip_width));
            case UP, DOWN ->
                    Math.clamp(uiEngineState.mouse_ui.x - (TS(tooltip_width) / 2), 0, uiEngineState.resolutionWidth - TS(tooltip_width));
        };

        int tooltip_y = switch (direction) {
            case RIGHT, LEFT ->
                    Math.clamp(uiEngineState.mouse_ui.y - (TS(tooltip_height) / 2), 0, uiEngineState.resolutionHeight - TS(tooltip_height));
            case UP ->
                    Math.clamp(uiEngineState.mouse_ui.y + TS(tooltip.lineLength), 0, uiEngineState.resolutionHeight - TS(tooltip_height));
            case DOWN ->
                    Math.clamp(uiEngineState.mouse_ui.y - TS(tooltip_height + tooltip.lineLength), 0, uiEngineState.resolutionHeight - TS(tooltip_height));
        };


        // Draw tooltip
        int iy = tooltip_height;
        for (int is = 0; is < tooltip.segments.size(); is++) {
            TooltipSegment segment = segments.get(is);
            float segmentAlpha = segment.color.a * uiEngineState.tooltip_fadePct;
            float borderAlpha = tooltip.color_border.a * uiEngineState.tooltip_fadePct;
            // Background
            if (!segment.merge) {
                iy -= segment.height;
                int width_reference = tooltip_width;
                int height_reference = tooltip_height;
                for (int ty = 0; ty < segment.height; ty++) {
                    int y_combined = iy + ty;
                    boolean drawBottomborder = false;

                    if (ty == 0) {
                        if (segment.border) {
                            drawBottomborder = y_combined != 0;
                        } else {
                            int isPlus1 = is + 1;
                            drawBottomborder = isPlus1 < segment.height && tooltip.segments.get(isPlus1).border;
                        }
                    }

                    // Background
                    if (!segment.clear) {
                        for (int tx = 0; tx < tooltip_width; tx++) {
                            spriteRenderer.setColor(segment.color, segmentAlpha);
                            spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TOOLTIP, tooltip_x + TS(tx), tooltip_y + TS(y_combined), render_get16TilesCMediaIndex(tx, y_combined, width_reference, height_reference));
                        }
                    }

                    // Border
                    for (int tx = 0; tx < tooltip_width; tx++) {
                        spriteRenderer.setColor(tooltip.color_border, borderAlpha);
                        // tooltip border
                        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TOOLTIP_BORDER, tooltip_x + TS(tx), tooltip_y + TS(y_combined), render_get16TilesCMediaIndex(tx, y_combined, width_reference, tooltip_height));
                        // segmentborder
                        if (drawBottomborder) {
                            spriteRenderer.drawCMediaImage(UIEngineBaseMedia_8x8.UI_TOOLTIP_SEGMENT_BORDER, tooltip_x + TS(tx), tooltip_y + TS(y_combined));
                        }
                    }
                }
            }

            switch (segment) {
                case TooltipTextSegment textSegment -> {
                    // Text
                    int text_width = render_textWidth(textSegment.font, textSegment.text);
                    int text_y = tooltip_y + TS(iy);
                    int text_x = tooltip_x + switch (textSegment.alignment) {
                        case LEFT -> 1;
                        case CENTER -> MathUtils.round(TS(tooltip_width) / 2f) - MathUtils.round(text_width / 2f);
                        case RIGHT -> TS(tooltip_width) - text_width - 3;
                    };
                    render_drawFont(textSegment.font, textSegment.text, segmentAlpha, text_x, text_y, 1, 1, FONT_MAXWIDTH_NONE, null, 0);
                }
                case TooltipImageSegment imageSegment -> {
                    int image_width = mediaManager.getCMediaSpriteWidth(imageSegment.image);
                    int image_height = mediaManager.getCMediaSpriteHeight(imageSegment.image);
                    int image_y = tooltip_y + TS(iy) + MathUtils.round((TS(segment.height) - image_height) / 2f);
                    int image_x = tooltip_x + switch (imageSegment.alignment) {
                        case LEFT -> 2;
                        case CENTER -> MathUtils.round(TS(tooltip_width) / 2f) - MathUtils.round(image_width / 2f);
                        case RIGHT -> TS(tooltip_width) - image_width - 2;
                    };
                    spriteRenderer.setColorReset();
                    spriteRenderer.drawCMediaSprite(imageSegment.image, image_x, image_y, imageSegment.arrayIndex, UICommonUtils.ui_getAnimationTimer(uiEngineState));
                }
                case TooltipCanvasSegment canvasSegment -> {
                    int width = TS(canvasSegment.width);
                    int height = TS(canvasSegment.height);

                    spriteRenderer.end();
                    primitiveRenderer.begin();

                    int canvas_x = tooltip_x + switch (canvasSegment.alignment) {
                        case LEFT -> 0;
                        case CENTER ->
                                MathUtils.round(TS(tooltip_width) / 2f) - MathUtils.round(TS(canvasSegment.width) / 2f);
                        case RIGHT -> TS(tooltip_width) - TS(canvasSegment.width);
                    };

                    for (int icx = 0; icx < width; icx++) {
                        for (int icy = 0; icy < height; icy++) {
                            float a = canvasSegment.colorMap.a[icx][icy];
                            if (a == 0) continue;
                            float r = canvasSegment.colorMap.r[icx][icy];
                            float g = canvasSegment.colorMap.g[icx][icy];
                            float b = canvasSegment.colorMap.b[icx][icy];
                            int vx = canvas_x + icx + 1;
                            int vy = tooltip_y + TS(iy) + icy + 1;
                            primitiveRenderer.setColor(0.5f, 0.5f, 0.5f, a * segmentAlpha);
                            primitiveRenderer.setVertexColor(r, g, b, a);
                            primitiveRenderer.vertex(vx, vy);
                        }
                    }
                    primitiveRenderer.end();
                    spriteRenderer.begin();

                }
                case null, default -> {
                }
            }
        }

        // Draw line
        spriteRenderer.setColor(tooltip.color_line);
        for (int i = 0; i < tooltip.lineLength; i++) {
            int xOffset = switch (direction) {
                case LEFT -> -TS(i + 1);
                case RIGHT -> TS(i);
                case UP, DOWN -> 0;
            };
            int yOffset = switch (direction) {
                case LEFT, RIGHT -> 0;
                case UP -> TS(i);
                case DOWN -> -TS(i + 1);
            };
            CMediaImage sprite = switch (direction) {
                case LEFT, RIGHT -> UIEngineBaseMedia_8x8.UI_TOOLTIP_LINE_HORIZONTAL;
                case UP, DOWN -> UIEngineBaseMedia_8x8.UI_TOOLTIP_LINE_VERTICAL;
            };
            spriteRenderer.drawCMediaImage(sprite, uiEngineState.mouse_ui.x + xOffset, uiEngineState.mouse_ui.y + yOffset);
        }
        switch (direction) {
            case LEFT, RIGHT -> {


            }
            case UP, DOWN -> {
                int yOffset = direction == DIRECTION.UP ? 0 : -TS2();
                spriteRenderer.drawCMediaImage(UIEngineBaseMedia_8x8.UI_TOOLTIP_LINE_VERTICAL, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y + yOffset);
            }
        }

        spriteRenderer.setTweakAndColorReset();
        primitiveRenderer.setTweakAndColorReset();
    }

    private void render_drawNotifications() {
        if (uiEngineState.notifications.size() == 0) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        int width = (uiEngineState.resolutionWidth % TS() == 0) ? (uiEngineState.resolutionWidth / TS()) : ((uiEngineState.resolutionWidth / TS()) + 1);
        final boolean notificationGrayScale = UICommonUtils.window_isModalOpen(uiEngineState);

        int y = 0;
        int yOffsetSlideFade = 0;
        for (int i = 0; i < uiEngineState.notifications.size(); i++) {
            Notification notification = uiEngineState.notifications.get(i);
            if (notification.state == STATE_NOTIFICATION.FADEOUT) {
                float fadeoutProgress = (notification.timer / (float) uiEngineState.config.notification_fadeoutTime);
                yOffsetSlideFade = yOffsetSlideFade + MathUtils.round(TS() * fadeoutProgress);
            }
            spriteRenderer.saveState();
            render_setColor(spriteRenderer, notification.color, notificationGrayScale);
            for (int ix = 0; ix < width; ix++) {
                spriteRenderer.drawCMediaImage(UIEngineBaseMedia_8x8.UI_NOTIFICATION_BAR, TS(ix), uiEngineState.resolutionHeight - TS() - TS(y) + yOffsetSlideFade);
            }
            spriteRenderer.loadState();
            int xOffset = (TS(width) / 2) - (render_textWidth(notification.font, notification.text) / 2) - notification.scroll;
            render_drawFont(notification.font, notification.text, notification.color.a, xOffset, (uiEngineState.resolutionHeight - TS() - TS(y)) + 1 + yOffsetSlideFade);
            y = y + 1;

        }

        spriteRenderer.setTweakAndColorReset();
    }

    private void render_drawWindow(Window window) {
        if (!window.visible) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final boolean windowGrayScale = (UICommonUtils.window_isModalOpen(uiEngineState) && uiEngineState.modalWindow != window);

        render_setColor(spriteRenderer, window.color, windowGrayScale);

        for (int ix = 0; ix < window.width; ix++) {
            if (!window.folded) {
                for (int iy = 0; iy < window.height; iy++) {
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_WINDOW, window.x + TS(ix), window.y + TS(iy), render_getWindowCMediaIndex(ix, iy, window.width, window.height, window.hasTitleBar));
                }
            } else {
                spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_WINDOW, window.x + TS(ix), window.y + (TS(window.height - 1)), render_getWindowCMediaIndex(ix, (window.height - 1), window.width, window.height, window.hasTitleBar));
            }
        }


        if (window.hasTitleBar) {
            render_drawFont(window.font, window.title, window.color.a, window.x, window.y + TS(window.height) - TS(), 1, 1, TS(window.width - 1), window.icon, window.iconIndex);
        }
        // Draw Components
        for (int i = 0; i < window.components.size(); i++) {
            Component component = window.components.get(i);
            if (!window.folded) {
                render_drawComponent(component);
            } else {
                if ((component.y / TS()) == (window.height - 1)) {
                    // draw title bar components only if folded
                    render_drawComponent(component);
                }
            }
        }


        // Draw Component TopLayer
        for (int i = 0; i < window.components.size(); i++) {
            Component component = window.components.get(i);
            if (!window.folded) render_drawComponentTopLayer(component);
        }

        spriteRenderer.setTweakAndColorReset();
    }

    private void render_setColor(PrimitiveRenderer primitiveRenderer, Color color, boolean grayScale) {
        render_setColor(primitiveRenderer, color, color.a, grayScale);
    }

    private void render_setColor(PrimitiveRenderer primitiveRenderer, Color color, float alpha, boolean grayScale) {
        if (grayScale) {
            float avg = ((color.r + color.g + color.b) / 3f) * 0.8f;
            primitiveRenderer.setColor(avg, avg, avg, alpha);
            primitiveRenderer.setTweak(0.5f, 0f, 0f, 0.5f);
        } else {
            primitiveRenderer.setColor(color, alpha);
            primitiveRenderer.setTweak(0.5f, 0.5f, 0.5f, 0.5f);
        }
    }

    private void render_setColor(SpriteRenderer spriteRenderer, Color color, boolean grayScale) {
        render_setColor(spriteRenderer, color, color.a, grayScale);
    }

    private void render_setColor(SpriteRenderer spriteRenderer, Color color, float alpha, boolean grayScale) {
        if (grayScale) {
            float avg = ((color.r + color.g + color.b) / 3f) * 0.8f;
            spriteRenderer.setColor(avg, avg, avg, alpha);
            spriteRenderer.setTweak(0.5f, 0f, 0f, 0.5f);
        } else {
            spriteRenderer.setColor(color, alpha);
            spriteRenderer.setTweak(0.5f, 0.5f, 0.5f, 0.5f);

        }
    }

    private void render_drawComponent(Component component) {
        if (render_isComponentNotRendered(component)) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final PrimitiveRenderer primitiveRenderer = uiEngineState.primitiveRenderer_ui;
        final float componentAlpha = componentAlpha(component);
        final boolean componentGrayScale = component.disabled || (UICommonUtils.window_isModalOpen(uiEngineState) && uiEngineState.modalWindow != component.addedToWindow);
        ;

        render_setColor(spriteRenderer, component.color, componentAlpha, componentGrayScale);

        switch (component) {
            case Button button -> {
                CMediaArray buttonGraphic = (button.pressed ? UIEngineBaseMedia_8x8.UI_BUTTON_PRESSED : UIEngineBaseMedia_8x8.UI_BUTTON);
                int pressed_offset = button.pressed ? 1 : 0;
                for (int ix = 0; ix < button.width; ix++) {
                    for (int iy = 0; iy < button.height; iy++) {
                        spriteRenderer.drawCMediaArray(buttonGraphic, UICommonUtils.component_getAbsoluteX(button) + TS(ix), UICommonUtils.component_getAbsoluteY(button) + TS(iy), render_get16TilesCMediaIndex(ix, iy, button.width, button.height));
                    }
                }
                if (button instanceof TextButton textButton) {
                    if (textButton.text != null) {
                        render_drawFont(textButton.font, textButton.text, componentAlpha, UICommonUtils.component_getAbsoluteX(textButton) + textButton.contentOffset_x + pressed_offset, UICommonUtils.component_getAbsoluteY(button) + textButton.contentOffset_y - pressed_offset, 1, 2, TS(button.width), textButton.icon, textButton.iconIndex);
                    }
                } else if (button instanceof ImageButton imageButton) {
                    spriteRenderer.saveState();
                    spriteRenderer.setColor(imageButton.color2, componentAlpha);
                    spriteRenderer.drawCMediaSprite(imageButton.image, UICommonUtils.component_getAbsoluteX(imageButton) + imageButton.contentOffset_x + pressed_offset, UICommonUtils.component_getAbsoluteY(imageButton) + imageButton.contentOffset_y - pressed_offset, imageButton.arrayIndex, UICommonUtils.ui_getAnimationTimer(uiEngineState));
                    spriteRenderer.loadState();
                }
            }
            case Image image -> {
                if (image.image != null) {
                    spriteRenderer.drawCMediaSprite(image.image, UICommonUtils.component_getAbsoluteX(image), UICommonUtils.component_getAbsoluteY(image), image.arrayIndex, UICommonUtils.ui_getAnimationTimer(uiEngineState));
                }
            }
            case Text text -> {
                int textHeight = TS(text.height - 1);
                if (text.lines != null && text.lines.length > 0) {
                    for (int i = 0; i < text.lines.length; i++) {
                        render_drawFont(text.font, text.lines[i], componentAlpha, UICommonUtils.component_getAbsoluteX(text), UICommonUtils.component_getAbsoluteY(text) + textHeight - TS(i), 1, 1, FONT_MAXWIDTH_NONE, null, 0);
                    }
                }
            }
            case ScrollbarVertical scrollBarVertical -> {
                for (int i = 0; i < scrollBarVertical.height; i++) {
                    int index = (i == 0 ? 2 : (i == (scrollBarVertical.height - 1) ? 0 : 1));
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_SCROLLBAR_VERTICAL, UICommonUtils.component_getAbsoluteX(scrollBarVertical), UICommonUtils.component_getAbsoluteY(scrollBarVertical) + TS(i), index);
                }
                int buttonYOffset = MathUtils.round(scrollBarVertical.scrolled * TS(scrollBarVertical.height - 1));
                spriteRenderer.saveState();
                render_setColor(spriteRenderer, scrollBarVertical.color2, componentAlpha, componentGrayScale);
                spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_SCROLLBAR_BUTTON_VERTICAL, UICommonUtils.component_getAbsoluteX(scrollBarVertical), UICommonUtils.component_getAbsoluteY(scrollBarVertical) + buttonYOffset, (scrollBarVertical.buttonPressed ? 1 : 0));
                spriteRenderer.loadState();
            }
            case ScrollbarHorizontal scrollBarHorizontal -> {
                for (int i = 0; i < scrollBarHorizontal.width; i++) {
                    int index = (i == 0 ? 0 : (i == (scrollBarHorizontal.width - 1) ? 2 : 1));
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_SCROLLBAR_HORIZONTAL, UICommonUtils.component_getAbsoluteX(scrollBarHorizontal) + TS(i), UICommonUtils.component_getAbsoluteY(scrollBarHorizontal), index);
                }
                int buttonXOffset = MathUtils.round(scrollBarHorizontal.scrolled * TS(scrollBarHorizontal.width - 1));
                spriteRenderer.saveState();
                render_setColor(spriteRenderer, scrollBarHorizontal.color2, componentAlpha, componentGrayScale);
                spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_SCROLLBAR_BUTTON_HORIZONAL, UICommonUtils.component_getAbsoluteX(scrollBarHorizontal) + buttonXOffset, UICommonUtils.component_getAbsoluteY(scrollBarHorizontal), (scrollBarHorizontal.buttonPressed ? 1 : 0));
                spriteRenderer.loadState();
            }
            case List list -> {
                boolean itemsValid = (list.items != null && list.items.size() > 0 && list.listAction != null);
                int itemFrom = 0;
                if (itemsValid) {
                    itemFrom = MathUtils.round(list.scrolled * ((list.items.size()) - (list.height)));
                    itemFrom = Math.max(itemFrom, 0);
                }
                boolean dragEnabled = false;
                boolean dragValid = false;
                int drag_x = -1, drag_y = -1;
                if ((uiEngineState.draggedList != null || uiEngineState.draggedGrid != null) && list == uiEngineState.lastUIMouseHover) {
                    dragEnabled = true;
                    dragValid = UICommonUtils.list_canDragIntoList(uiEngineState, list);
                    if (dragValid) {
                        drag_x = UICommonUtils.component_getAbsoluteX(list);
                        int y_list = UICommonUtils.component_getAbsoluteY(list);
                        drag_y = y_list + TS((uiEngineState.mouse_ui.y - y_list) / TS());
                    }
                }

                boolean listGrayScale = componentGrayScale || (dragEnabled && !dragValid);
                render_setColor(spriteRenderer, component.color, componentAlpha, listGrayScale);

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

                    Color cellColor = null;
                    if (list.listAction != null && list.items != null && itemIndex < list.items.size()) {
                        cellColor = list.listAction.cellColor(item);
                        if (cellColor != null) {
                            spriteRenderer.saveState();
                            render_setColor(spriteRenderer, cellColor, cellColor.a, listGrayScale);
                        }
                    }

                    for (int ix = 0; ix < list.width; ix++) {
                        CMediaImage listSelectedGraphic = selected ? UIEngineBaseMedia_8x8.UI_LIST_SELECTED : UIEngineBaseMedia_8x8.UI_LIST;
                        spriteRenderer.drawCMediaImage(listSelectedGraphic, UICommonUtils.component_getAbsoluteX(list) + TS(ix), UICommonUtils.component_getAbsoluteY(list) + TS(itemOffsetY));
                    }
                    if (cellColor != null) spriteRenderer.loadState();

                    // Text
                    if (item != null) {
                        String text = list.listAction.text(item);
                        render_drawFont(list.font, text, componentAlpha, UICommonUtils.component_getAbsoluteX(list), UICommonUtils.component_getAbsoluteY(list) + TS(itemOffsetY), 1, 2, TS(list.width), list.listAction.icon(item), list.listAction.iconIndex(item));
                    }
                }

                if (dragEnabled && dragValid) {
                    for (int ix = 0; ix < list.width; ix++) {
                        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_LIST_DRAG, drag_x + TS(ix), drag_y, render_getListDragCMediaIndex(ix, list.width));
                    }
                }
            }
            case Combobox comboBox -> {
                // Box
                Color comboBoxColor = comboBox.selectedItem != null ? comboBox.selectedItem.color : comboBox.color;
                for (int ix = 0; ix < comboBox.width; ix++) {
                    int index = ix == 0 ? 0 : (ix == comboBox.width - 1 ? 2 : 1);
                    CMediaArray comboMedia = UICommonUtils.comboBox_isOpen(uiEngineState, comboBox) ? UIEngineBaseMedia_8x8.UI_COMBOBOX_OPEN : UIEngineBaseMedia_8x8.UI_COMBOBOX;
                    render_setColor(spriteRenderer, comboBoxColor, componentAlpha, componentGrayScale);
                    spriteRenderer.drawCMediaArray(comboMedia, UICommonUtils.component_getAbsoluteX(comboBox) + TS(ix), UICommonUtils.component_getAbsoluteY(comboBox), index);
                }
                // Text
                if (comboBox.selectedItem != null && comboBox.comboBoxAction != null) {
                    render_drawFont(comboBox.selectedItem.font, comboBox.selectedItem.text, componentAlpha, UICommonUtils.component_getAbsoluteX(comboBox), UICommonUtils.component_getAbsoluteY(comboBox), 2, 1, TS(comboBox.width - 1), comboBox.selectedItem.icon, comboBox.selectedItem.iconIndex);
                }
            }
            case Knob knob -> {
                spriteRenderer.drawCMediaImage(UIEngineBaseMedia_8x8.UI_KNOB_BACKGROUND, UICommonUtils.component_getAbsoluteX(knob), UICommonUtils.component_getAbsoluteY(knob));
                render_setColor(spriteRenderer, knob.color2, componentAlpha, componentGrayScale);
                if (knob.endless) {
                    int index = MathUtils.round(knob.turned * 31);
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_KNOB_ENDLESS, UICommonUtils.component_getAbsoluteX(knob), UICommonUtils.component_getAbsoluteY(knob), index);
                } else {
                    int index = MathUtils.round(knob.turned * 25);
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_KNOB, UICommonUtils.component_getAbsoluteX(knob), UICommonUtils.component_getAbsoluteY(knob), index);
                }
            }
            case Canvas canvas -> {
                int width = TS(canvas.width);
                int height = TS(canvas.height);

                spriteRenderer.end();
                primitiveRenderer.begin();
                render_setColor(primitiveRenderer, Color.GRAY, componentGrayScale);
                for (int icx = 0; icx < width; icx++) {
                    for (int icy = 0; icy < height; icy++) {
                        float a = canvas.colorMap.a[icx][icy];
                        if (a == 0f) continue;
                        float r = canvas.colorMap.r[icx][icy];
                        float g = canvas.colorMap.g[icx][icy];
                        float b = canvas.colorMap.b[icx][icy];
                        primitiveRenderer.setColor(0.5f, 0.5f, 0.5f, a * componentAlpha);
                        primitiveRenderer.setVertexColor(r, g, b, a);
                        primitiveRenderer.vertex(UICommonUtils.component_getAbsoluteX(canvas) + icx + 1, UICommonUtils.component_getAbsoluteY(canvas) + icy + 1);
                    }
                }
                primitiveRenderer.setTweakAndColorReset();
                primitiveRenderer.end();
                spriteRenderer.begin();

                for (int i = (canvas.canvasImages.size() - 1); i >= 0; i--) {
                    CanvasImage canvasImage = canvas.canvasImages.get(i);
                    if (canvasImage.fadeOut) {
                        canvasImage.color.a = Math.clamp(canvasImage.color.a - canvasImage.fadeOutSpeed, 0f, 1f);
                        if (canvasImage.color.a <= 0) {
                            canvas.canvasImages.remove(i);
                            continue;
                        }
                    }
                    if (UICommonUtils.canvas_isImageInsideCanvas(uiEngineState, canvas, canvasImage.x, canvasImage.y)) {
                        spriteRenderer.saveState();
                        render_setColor(spriteRenderer, canvasImage.color, (canvasImage.color.a * componentAlpha), componentGrayScale);
                        int imageWidthOffset = mediaManager.getCMediaSpriteWidth(canvasImage.image) / 2;
                        int imageHeightOffset = mediaManager.getCMediaSpriteHeight(canvasImage.image) / 2;
                        spriteRenderer.drawCMediaSprite(canvasImage.image,
                                UICommonUtils.component_getAbsoluteX(canvas) + canvasImage.x - imageWidthOffset,
                                UICommonUtils.component_getAbsoluteY(canvas) + canvasImage.y - imageHeightOffset,
                                canvasImage.arrayIndex, UICommonUtils.ui_getAnimationTimer(uiEngineState));
                        spriteRenderer.loadState();
                    }

                }
            }
            case Textfield textField -> {
                for (int ix = 0; ix < textField.width; ix++) {
                    int index = ix == (textField.width - 1) ? 2 : (ix == 0) ? 0 : 1;
                    CMediaArray textFieldGraphic = uiEngineState.focusedTextField == textField ? UIEngineBaseMedia_8x8.UI_TEXTFIELD_FOCUSED : UIEngineBaseMedia_8x8.UI_TEXTFIELD;
                    spriteRenderer.drawCMediaArray(textFieldGraphic, UICommonUtils.component_getAbsoluteX(textField) + TS(ix), UICommonUtils.component_getAbsoluteY(textField), index);

                    if (!textField.contentValid) {
                        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TEXTFIELD_VALIDATION_OVERLAY, UICommonUtils.component_getAbsoluteX(textField) + TS(ix), UICommonUtils.component_getAbsoluteY(textField), index);
                    }
                }

                if (textField.content != null) {
                    render_drawFont(textField.font, textField.content.substring(textField.offset), componentAlpha, UICommonUtils.component_getAbsoluteX(textField), UICommonUtils.component_getAbsoluteY(textField), 1, 2, TS(textField.width) - 4, null, 0);
                    if (UICommonUtils.textField_isFocused(uiEngineState, textField)) {
                        int xOffset = render_textWidth(textField.font, textField.content.substring(textField.offset, textField.markerPosition)) + 2;
                        if (xOffset < TS(textField.width)) {
                            spriteRenderer.drawCMediaAnimation(UIEngineBaseMedia_8x8.UI_TEXTFIELD_CARET, UICommonUtils.component_getAbsoluteX(textField) + xOffset, UICommonUtils.component_getAbsoluteY(textField), UICommonUtils.ui_getAnimationTimer(uiEngineState));
                        }
                    }
                }
            }
            case Grid grid -> {
                int tileSize = grid.doubleSized ? TS2() : TS();
                int gridWidth = grid.items.length;
                int gridHeight = grid.items[0].length;

                boolean dragEnabled = false;
                boolean dragValid = false;
                int drag_x = -1, drag_y = -1;
                if ((uiEngineState.draggedList != null || uiEngineState.draggedGrid != null) && grid == uiEngineState.lastUIMouseHover) {
                    dragEnabled = true;
                    dragValid = UICommonUtils.grid_canDragIntoGrid(uiEngineState, grid);
                    if (dragValid) {
                        int x_grid = UICommonUtils.component_getAbsoluteX(grid);
                        int y_grid = UICommonUtils.component_getAbsoluteY(grid);
                        int m_x = uiEngineState.mouse_ui.x - x_grid;
                        int m_y = uiEngineState.mouse_ui.y - y_grid;
                        if (m_x > 0 && m_x < (grid.width * tileSize) && m_y > 0 && m_y < (grid.height * tileSize)) {
                            int inv_x = m_x / tileSize;
                            int inv_y = m_y / tileSize;
                            if (UICommonUtils.grid_positionValid(grid, inv_x, inv_y)) {
                                drag_x = inv_x;
                                drag_y = inv_y;
                            }
                        }
                    }
                }

                boolean gridGrayScale = componentGrayScale || (dragEnabled && !dragValid);
                render_setColor(spriteRenderer, component.color, componentAlpha, gridGrayScale);

                for (int ix = 0; ix < gridWidth; ix++) {
                    for (int iy = 0; iy < gridHeight; iy++) {
                        if (grid.items != null) {
                            CMediaArray cellGraphic;
                            boolean selected = grid.items[ix][iy] != null && grid.items[ix][iy] == grid.selectedItem;
                            if (dragEnabled && dragValid && drag_x == ix && drag_y == iy) {
                                cellGraphic = grid.doubleSized ? UIEngineBaseMedia_8x8.UI_GRID_DRAGGED_X2 : UIEngineBaseMedia_8x8.UI_GRID_DRAGGED;
                            } else {
                                if (selected) {
                                    cellGraphic = grid.doubleSized ? UIEngineBaseMedia_8x8.UI_GRID_SELECTED_X2 : UIEngineBaseMedia_8x8.UI_GRID_SELECTED;
                                } else {
                                    cellGraphic = grid.doubleSized ? UIEngineBaseMedia_8x8.UI_GRID_X2 : UIEngineBaseMedia_8x8.UI_GRID;
                                }
                            }

                            // Draw Cell
                            Color cellColor = null;
                            if (grid.gridAction != null) {
                                cellColor = grid.gridAction.cellColor(grid.items[ix][iy], ix, iy);
                                if (cellColor != null) {
                                    spriteRenderer.saveState();
                                    render_setColor(spriteRenderer, cellColor, componentAlpha, gridGrayScale);
                                }
                            }

                            int index = grid.doubleSized ? render_get16TilesCMediaIndex(ix, iy, grid.width / 2, grid.height / 2) : render_get16TilesCMediaIndex(ix, iy, grid.width, grid.height);
                            spriteRenderer.drawCMediaArray(cellGraphic, UICommonUtils.component_getAbsoluteX(grid) + (ix * tileSize), UICommonUtils.component_getAbsoluteY(grid) + (iy * tileSize), index);

                            if (cellColor != null) spriteRenderer.loadState();

                            // Draw Icon
                            CMediaSprite cellIcon = (grid.items[ix][iy] != null && grid.gridAction != null) ? grid.gridAction.icon(grid.items[ix][iy]) : null;
                            if (cellIcon != null) {
                                spriteRenderer.saveState();
                                spriteRenderer.setColor(0.5f, 0.5f, 0.5f, componentAlpha);
                                int iconIndex = grid.gridAction != null ? grid.gridAction.iconIndex(grid.items[ix][iy]) : 0;
                                spriteRenderer.drawCMediaSprite(cellIcon, UICommonUtils.component_getAbsoluteX(grid) + (ix * tileSize), UICommonUtils.component_getAbsoluteY(grid) + (iy * tileSize), iconIndex, UICommonUtils.ui_getAnimationTimer(uiEngineState));
                                spriteRenderer.loadState();
                            }
                        }
                    }
                }

            }
            case Tabbar tabBar -> {
                int tabXOffset = tabBar.tabOffset;
                int topBorder;
                for (int i = 0; i < tabBar.tabs.size(); i++) {
                    Tab tab = tabBar.tabs.get(i);
                    int tabWidth = tabBar.bigIconMode ? 2 : tab.width;
                    if ((tabXOffset + tabWidth) > tabBar.width) break;

                    boolean selected = i == tabBar.selectedTab;

                    if (tabBar.bigIconMode) {
                        CMediaImage tabGraphic = selected ? UIEngineBaseMedia_8x8.UI_TAB_BIGICON_SELECTED : UIEngineBaseMedia_8x8.UI_TAB_BIGICON;
                        spriteRenderer.drawCMediaImage(tabGraphic, UICommonUtils.component_getAbsoluteX(tabBar) + TS(tabXOffset), UICommonUtils.component_getAbsoluteY(tabBar));
                        // Icon
                        if (tab.icon != null) {
                            int selected_offset = selected ? 0 : 1;
                            spriteRenderer.drawCMediaSprite(tab.icon, UICommonUtils.component_getAbsoluteX(tabBar) + TS(tabXOffset) + selected_offset, UICommonUtils.component_getAbsoluteY(tabBar) - selected_offset, tab.iconIndex, UICommonUtils.ui_getAnimationTimer(uiEngineState));
                        }
                    } else {
                        CMediaArray tabGraphic = selected ? UIEngineBaseMedia_8x8.UI_TAB_SELECTED : UIEngineBaseMedia_8x8.UI_TAB;
                        for (int ix = 0; ix < tabWidth; ix++) {
                            spriteRenderer.drawCMediaArray(tabGraphic, UICommonUtils.component_getAbsoluteX(tabBar) + TS(ix) + TS(tabXOffset), UICommonUtils.component_getAbsoluteY(tabBar), render_getTabCMediaIndex(ix, tab.width));
                        }
                    }

                    if (!tabBar.bigIconMode) {
                        render_drawFont(tab.font, tab.title, componentAlpha, UICommonUtils.component_getAbsoluteX(tabBar) + TS(tabXOffset), UICommonUtils.component_getAbsoluteY(tabBar), 2, 1, TS(tabWidth), tab.icon, tab.iconIndex);
                    }
                    tabXOffset += tabWidth;
                }

                topBorder = tabBar.width - tabXOffset;

                // Top Border Top
                for (int ix = 0; ix < topBorder; ix++) {
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TAB_BORDERS, UICommonUtils.component_getAbsoluteX(tabBar) + TS(tabXOffset + ix), UICommonUtils.component_getAbsoluteY(tabBar), 2);
                }

                if (tabBar.border) {
                    // Bottom
                    for (int ix = 0; ix < tabBar.width; ix++) {
                        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TAB_BORDERS, UICommonUtils.component_getAbsoluteX(tabBar) + TS(ix), UICommonUtils.component_getAbsoluteY(tabBar) - TS(tabBar.borderHeight), 2);
                    }
                    // Left/Right
                    for (int iy = 0; iy < tabBar.borderHeight; iy++) {
                        int yOffset = TS(iy + 1);
                        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TAB_BORDERS, UICommonUtils.component_getAbsoluteX(tabBar), UICommonUtils.component_getAbsoluteY(tabBar) - yOffset, 0);
                        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TAB_BORDERS, UICommonUtils.component_getAbsoluteX(tabBar) + TS(tabBar.width - 1), UICommonUtils.component_getAbsoluteY(tabBar) - yOffset, 1);
                    }
                }
            }
            case Shape shape -> {
                if (shape.shapeType != null) {
                    CMediaImage shapeImage = switch (shape.shapeType) {
                        case OVAL -> UIEngineBaseMedia_8x8.UI_SHAPE_OVAL;
                        case RECT -> UIEngineBaseMedia_8x8.UI_SHAPE_RECT;
                        case DIAMOND -> UIEngineBaseMedia_8x8.UI_SHAPE_DIAMOND;
                        case TRIANGLE_LEFT_DOWN -> UIEngineBaseMedia_8x8.UI_SHAPE_TRIANGLE_LEFT_DOWN;
                        case TRIANGLE_RIGHT_DOWN -> UIEngineBaseMedia_8x8.UI_SHAPE_TRIANGLE_RIGHT_DOWN;
                        case TRIANGLE_LEFT_UP -> UIEngineBaseMedia_8x8.UI_SHAPE_TRIANGLE_LEFT_UP;
                        case TRIANGLE_RIGHT_UP -> UIEngineBaseMedia_8x8.UI_SHAPE_TRIANGLE_RIGHT_UP;
                    };

                    spriteRenderer.drawCMediaImage(shapeImage, UICommonUtils.component_getAbsoluteX(shape), UICommonUtils.component_getAbsoluteY(shape),
                            0, 0, TS(shape.width), TS(shape.height));
                }
            }
            case Progressbar progressBar -> {
                // Background
                for (int ix = 0; ix < progressBar.width; ix++) {
                    int index = ix == 0 ? 0 : ix == (progressBar.width - 1) ? 2 : 1;
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_PROGRESSBAR, UICommonUtils.component_getAbsoluteX(progressBar) + TS(ix), UICommonUtils.component_getAbsoluteY(progressBar), index);
                }

                // Bar
                spriteRenderer.saveState();
                render_setColor(spriteRenderer, progressBar.color2, componentAlpha, componentGrayScale);
                int pixels = MathUtils.round(progressBar.progress * TS(progressBar.width));
                for (int ix = 0; ix < progressBar.width; ix++) {
                    int xOffset = TS(ix);
                    int index = ix == 0 ? 0 : ix == (progressBar.width - 1) ? 2 : 1;
                    if (xOffset < pixels) {
                        if (pixels - xOffset < TS()) {
                            spriteRenderer.drawCMediaArrayCut(UIEngineBaseMedia_8x8.UI_PROGRESSBAR_BAR, UICommonUtils.component_getAbsoluteX(progressBar) + xOffset, UICommonUtils.component_getAbsoluteY(progressBar), index, pixels - xOffset, TS());
                        } else {
                            spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_PROGRESSBAR_BAR, UICommonUtils.component_getAbsoluteX(progressBar) + xOffset, UICommonUtils.component_getAbsoluteY(progressBar), index);
                        }
                    }
                }
                spriteRenderer.loadState();

                if (progressBar.progressText) {
                    String percentTxt = progressBar.progressText2Decimal ? UICommonUtils.progressBar_getProgressText2Decimal(progressBar.progress) : UICommonUtils.progressBar_getProgressText(progressBar.progress);
                    int xOffset = (TS(progressBar.width) / 2) - (render_textWidth(progressBar.font, percentTxt) / 2);
                    render_drawFont(progressBar.font, percentTxt, componentAlpha, UICommonUtils.component_getAbsoluteX(progressBar) + xOffset, UICommonUtils.component_getAbsoluteY(progressBar), 0, 1, FONT_MAXWIDTH_NONE, null, 0);
                }
            }
            case Checkbox checkBox -> {
                CMediaArray checkBoxGraphic = checkBox.checkBoxStyle == CHECKBOX_STYLE.CHECKBOX ? UIEngineBaseMedia_8x8.UI_CHECKBOX_CHECKBOX : UIEngineBaseMedia_8x8.UI_CHECKBOX_RADIO;
                spriteRenderer.drawCMediaArray(checkBoxGraphic, UICommonUtils.component_getAbsoluteX(checkBox), UICommonUtils.component_getAbsoluteY(checkBox), checkBox.checked ? 1 : 0);
                render_drawFont(checkBox.font, checkBox.text, componentAlpha, UICommonUtils.component_getAbsoluteX(checkBox) + TS(), UICommonUtils.component_getAbsoluteY(checkBox), 1, 1, FONT_MAXWIDTH_NONE, null, 0);
            }
            case AppViewport appViewPort -> {
                spriteRenderer.draw(appViewPort.textureRegion, UICommonUtils.component_getAbsoluteX(appViewPort), UICommonUtils.component_getAbsoluteY(appViewPort));
            }
            default -> {
            }
        }

        spriteRenderer.setTweakAndColorReset();
    }

    private void render_drawCursorDragAndDrop() {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        if (uiEngineState.draggedGrid != null) {
            Grid dragGrid = uiEngineState.draggedGrid;
            int dragOffsetX = uiEngineState.draggedGridOffset.x;
            int dragOffsetY = uiEngineState.draggedGridOffset.y;
            Object dragItem = uiEngineState.draggedGridItem;
            if (dragGrid.gridAction != null) {
                float dragAlpha = dragGrid.color.a * uiEngineState.config.component_listDragAlpha;
                spriteRenderer.setColor(dragGrid.color, dragAlpha);
                CMediaSprite icon = dragGrid.gridAction.icon(dragItem);
                if (icon != null)
                    spriteRenderer.drawCMediaSprite(icon, uiEngineState.mouse_ui.x - dragOffsetX, uiEngineState.mouse_ui.y - dragOffsetY, dragGrid.gridAction.iconIndex(dragItem), UICommonUtils.ui_getAnimationTimer(uiEngineState));
            }
        } else if (uiEngineState.draggedList != null) {
            List dragList = uiEngineState.draggedList;
            int dragOffsetX = uiEngineState.draggedListOffsetX.x;
            int dragOffsetY = uiEngineState.draggedListOffsetX.y;
            Object dragItem = uiEngineState.draggedListItem;

            if (dragList.listAction != null) {
                float dragAlpha = dragList.color.a * uiEngineState.config.component_listDragAlpha;
                // List
                spriteRenderer.setColor(dragList.color, dragAlpha);
                for (int ix = 0; ix < dragList.width; ix++) {
                    spriteRenderer.drawCMediaSprite(UIEngineBaseMedia_8x8.UI_LIST_SELECTED, uiEngineState.mouse_ui.x - dragOffsetX + TS(ix), uiEngineState.mouse_ui.y - dragOffsetY, 0, UICommonUtils.ui_getAnimationTimer(uiEngineState));
                }
                // Text
                String text = dragList.listAction.text(dragItem);
                render_drawFont(dragList.font, text, dragAlpha, uiEngineState.mouse_ui.x - dragOffsetX, uiEngineState.mouse_ui.y - dragOffsetY, 2, 1,
                        TS(dragList.width), dragList.listAction.icon(dragItem), dragList.listAction.iconIndex(dragItem));
            }
        }

        spriteRenderer.setTweakAndColorReset();
    }

    private int render_textWidth(CMediaFont font, String text) {
        if (font == null || text == null || text.length() == 0) return 0;
        return mediaManager.getCMediaFontTextWidth(font, text);
    }

    private void render_drawFont(CMediaFont font, String text, float alpha, int x, int y) {
        render_drawFont(font, text, alpha, x, y, 0, 0, FONT_MAXWIDTH_NONE, null, 0);
    }

    private void render_drawFont(CMediaFont font, String text, float alpha, int x, int y, int textXOffset, int textYOffset, int maxWidth, CMediaSprite icon, int iconIndex) {
        if (font == null) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        boolean withIcon = icon != null;
        if (withIcon) {
            spriteRenderer.saveState();
            spriteRenderer.setColor(0.5f, 0.5f, 0.5f, alpha);
            spriteRenderer.drawCMediaSprite(icon, x, y, iconIndex, UICommonUtils.ui_getAnimationTimer(uiEngineState));
            spriteRenderer.loadState();
        }

        uiEngineState.fontTempColor.set(mediaManager.getCMediaFont(font).getColor());
        mediaManager.getCMediaFont(font).setColor(0.5f, 0.5f, 0.5f, alpha);
        if (maxWidth == FONT_MAXWIDTH_NONE) {
            spriteRenderer.drawCMediaFont(font, x + (withIcon ? TS() : 0) + textXOffset, y + textYOffset, text);
        } else {
            if (withIcon) maxWidth += TS();
            spriteRenderer.drawCMediaFont(font, x + (withIcon ? TS() : 0) + textXOffset, y + textYOffset, text,
                    maxWidth);
        }
        mediaManager.getCMediaFont(font).setColor(uiEngineState.fontTempColor);
    }

    public void shutdown() {
        this.uiAdapter.shutdown();

        // Lists
        uiEngineState.windows.clear();

        uiEngineState.modalWindowQueue.clear();
        uiEngineState.hotKeys.clear();
        uiEngineState.singleUpdateActions.clear();
        uiEngineState.screenComponents.clear();
        uiEngineState.notifications.clear();
        uiEngineState.appViewPorts.clear();
        uiEngineState.spriteRenderer_ui.dispose();


        // Textures
        uiEngineState.frameBuffer_app.dispose();
        uiEngineState.frameBuffer_ui.dispose();
        uiEngineState.frameBuffer_screen.dispose();

    }

    public int getResolutionWidth() {
        return uiEngineState.resolutionWidth;
    }

    public int getResolutionHeight() {
        return uiEngineState.resolutionHeight;
    }

    public VIEWPORT_MODE getViewportMode() {
        return uiEngineState.viewportMode;
    }

    public int getViewPortScreenX() {
        return uiEngineState.viewport_screen.getScreenX();
    }

    public int getViewPortScreenY() {
        return uiEngineState.viewport_screen.getScreenY();
    }

    public int getViewPortScreenWidth() {
        return uiEngineState.viewport_screen.getScreenWidth();
    }

    public int getViewPortScreenHeight() {
        return uiEngineState.viewport_screen.getScreenHeight();
    }

    public boolean isGamePadSupport() {
        return uiEngineState.gamePadSupport;
    }

    public NestedFrameBuffer getFrameBufferScreen() {
        return uiEngineState.frameBuffer_screen;
    }

    public NestedFrameBuffer getFrameBufferApp() {
        return uiEngineState.frameBuffer_app;
    }

    public NestedFrameBuffer getFrameBufferUI() {
        return uiEngineState.frameBuffer_ui;
    }

    private int TS(int size) {
        return uiEngineState.tileSize.TL(size);
    }

    private int TS() {
        return uiEngineState.tileSize.TS;
    }

    private int TS_HALF() {
        return uiEngineState.tileSize.TS_HALF;
    }

    private int TS2() {
        return uiEngineState.tileSize.TS2;
    }


}
