package net.mslivo.pixelui.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.Queue;
import net.mslivo.pixelui.engine.actions.UpdateAction;
import net.mslivo.pixelui.engine.actions.common.CommonActions;
import net.mslivo.pixelui.engine.constants.*;
import net.mslivo.pixelui.media.*;
import net.mslivo.pixelui.rendering.NestedFrameBuffer;
import net.mslivo.pixelui.rendering.PrimitiveRenderer;
import net.mslivo.pixelui.rendering.ShaderParser;
import net.mslivo.pixelui.rendering.SpriteRenderer;
import net.mslivo.pixelui.utils.Tools;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;


/**
 * UI Engine
 * Handles UI Elements, Input, Cameras
 * App needs to be implemented inside the uiAdapter
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public final class UIEngine<T extends UIEngineAdapter> implements Disposable {
    private static final int FONT_MAXWIDTH_NONE = -1;

    // Basic Configuration
    private final T uiAdapter;
    private final UIEngineState uiEngineState;
    private final UICommonUtils uiCommonUtils;
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

        // Setup State & State Utils
        this.uiEngineState = initializeInputState(resolutionWidth, resolutionHeight, viewportMode, gamePadSupport, TILE_SIZE.MODE_8x8);
        this.uiCommonUtils = new UICommonUtils(this.uiEngineState, this.mediaManager);
        // Setup API
        this.api = new API(this.uiEngineState, mediaManager);

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT);
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
        newUIEngineState.config = new UIEngineConfig();

        // -----  App
        newUIEngineState.camera_app = new OrthographicCamera(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.camera_app.setToOrtho(false, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.camera_app.position.set(newUIEngineState.resolutionWidthHalf, newUIEngineState.resolutionHeightHalf, 0);
        newUIEngineState.camera_app.zoom = 1f;
        newUIEngineState.camera_app.update();
        newUIEngineState.frameBuffer_app = new NestedFrameBuffer(Pixmap.Format.RGB888, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight, true);
        newUIEngineState.frameBuffer_app.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // -----  GUI
        newUIEngineState.spriteRenderer_ui = new SpriteRenderer(this.mediaManager, ShaderParser.parse(Tools.File.findResource("shaders/pixelui/hsl.sprite.glsl")));
        newUIEngineState.spriteRenderer_ui.setTweakResetValues(0.5f, 0.5f, 0.5f, 0f);
        newUIEngineState.primitiveRenderer_ui = new PrimitiveRenderer(ShaderParser.parse(Tools.File.findResource("shaders/pixelui/hsl.primitive.glsl")));
        newUIEngineState.primitiveRenderer_ui.setTweakResetValues(0.5f, 0.5f, 0.5f, 0f);

        newUIEngineState.camera_ui = new OrthographicCamera(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.camera_ui.setToOrtho(false, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.camera_ui.update();
        newUIEngineState.frameBufferComponent_ui = new NestedFrameBuffer(Pixmap.Format.RGBA8888, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight, false);
        newUIEngineState.frameBufferComponent_ui.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        newUIEngineState.frameBufferModal_ui = new NestedFrameBuffer(Pixmap.Format.RGBA8888, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight, false);
        newUIEngineState.frameBufferModal_ui.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        // ----- Composite
        newUIEngineState.frameBuffer_composite = new NestedFrameBuffer(Pixmap.Format.RGBA8888, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight, false);
        newUIEngineState.frameBuffer_composite.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // ----- Screen
        newUIEngineState.viewport_screen = uiCommonUtils.viewport_createViewport(newUIEngineState.viewportMode, newUIEngineState.camera_ui, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.viewport_screen.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        if (viewportMode.upscale) {
            newUIEngineState.upScaleFactor_screen = uiCommonUtils.viewport_determineUpscaleFactor(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
            newUIEngineState.frameBuffer_upScaled_screen = new NestedFrameBuffer(Pixmap.Format.RGBA8888, newUIEngineState.resolutionWidth * newUIEngineState.upScaleFactor_screen, newUIEngineState.resolutionHeight * newUIEngineState.upScaleFactor_screen, false);
            newUIEngineState.frameBuffer_upScaled_screen.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        // -----  GUI
        newUIEngineState.windows = new Array<>();
        newUIEngineState.screenComponents = new Array<>();
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
        newUIEngineState.keyboardInteractedUIObjectFrame = null;
        newUIEngineState.mouseInteractedUIObjectFrame = null;
        newUIEngineState.forceTooltipUpdateComponents = new Array<>();
        newUIEngineState.modalWindow = null;
        newUIEngineState.modalWindowQueue = new Queue<>();
        newUIEngineState.pressedTextField = null;
        newUIEngineState.pressedTextFieldInitCaretPosition = 0;
        newUIEngineState.focusedTextField = null;
        newUIEngineState.notifications = new Array<>();
        newUIEngineState.tooltipNotifications = new Array<>();
        newUIEngineState.hotKeys = new Array<>();
        newUIEngineState.appViewPorts = new Array<>();
        newUIEngineState.singleUpdateActions = new Array<>();
        newUIEngineState.singleUpdateActionsRemoveQueue = new Queue<>();
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
        newUIEngineState.draggedListOffset = new GridPoint2();
        newUIEngineState.draggedListFromIndex = 0;
        newUIEngineState.pressedList = null;
        newUIEngineState.pressedListItem = null;
        newUIEngineState.pressedAppViewPort = null;
        newUIEngineState.tooltip_lastHoverObject = null;
        newUIEngineState.pressedFramebufferViewport = null;
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
        newUIEngineState.inputEvents = new UIInputEvents();
        newUIEngineState.inputProcessor = new UIInputProcessor(newUIEngineState.inputEvents, newUIEngineState.gamePadSupport);
        newUIEngineState.itemInfo_listIndex = 0;
        newUIEngineState.itemInfo_tabBarTabIndex = 0;
        newUIEngineState.itemInfo_gridPos = new GridPoint2();
        newUIEngineState.itemInfo_listValid = false;
        newUIEngineState.itemInfo_tabBarValid = false;
        newUIEngineState.itemInfo_gridValid = false;
        newUIEngineState.tempFontColor = new Color(Color.CLEAR);
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

        // Confirm Character from API Queue if nothing was pressed
        if (!enterRegularCharacter && !mouseTextInput.enterCharacterQueue.isEmpty()) {
            uiCommonUtils.mouseTextInput_selectCharacter(uiEngineState.openMouseTextInput, (char) mouseTextInput.enterCharacterQueue.removeIndex(0));
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
                    mouseTextInput.mouseTextInputAction.onChangeCase(mouseTextInput.upperCase);
                }
                // Control Delete
                case '\b' -> {
                    uiEngineState.inputEvents.keyDown = true;
                    uiEngineState.inputEvents.keyDownKeyCodes.add(KeyCode.Key.BACKSPACE);
                    uiEngineState.inputEvents.keyUp = true;
                    uiEngineState.inputEvents.keyUpKeyCodes.add(KeyCode.Key.BACKSPACE);
                    mouseTextInput.mouseTextInputAction.onDelete();
                }
                // Control Confirm
                case '\n' -> {
                    boolean close = mouseTextInput.mouseTextInputAction == null || mouseTextInput.mouseTextInputAction.onConfirm();
                    if (close) uiCommonUtils.mouseTextInput_close(uiEngineState);
                }
                // Default Text Character
                default -> {
                    uiEngineState.inputEvents.keyTyped = true;
                    uiEngineState.inputEvents.keyTypedCharacters.add(c);
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
        uiEngineState.lastUIMouseHover = uiCommonUtils.component_getUIObjectAtPosition(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y);
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

    private boolean updateUI_keyInteractionsKeyProcessKey(Textfield focusedTextField) {
        if (focusedTextField != null) {
            if (uiCommonUtils.window_isModalOpen(uiEngineState)) {
                if (focusedTextField.addedToWindow == null) {
                    return false;
                } else if (focusedTextField.addedToWindow != uiEngineState.modalWindow) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private void updateUI_keyInteractions() {
        uiCommonUtils.setKeyboardInteractedUIObject(null);
        if (uiEngineState.config.ui_keyInteractionsDisabled) return;
        // Key
        if (uiEngineState.inputEvents.keyTyped) {
            final Textfield focusedTextField = uiEngineState.focusedTextField;
            final boolean processKeyTyped = updateUI_keyInteractionsKeyProcessKey(focusedTextField);

            if (processKeyTyped) {
                // Into Temp variable because focuseTextField can change after executing actions
                for (int ic = 0; ic < uiEngineState.inputEvents.keyTypedCharacters.size; ic++) {
                    char keyTypedCharacter = (char) uiEngineState.inputEvents.keyTypedCharacters.get(ic);
                    uiCommonUtils.textField_typeCharacter(focusedTextField, keyTypedCharacter);
                }
                // MouseTextInput open = focus on last typed character
                if (uiEngineState.openMouseTextInput != null) {
                    char typedChar = (char) uiEngineState.inputEvents.keyTypedCharacters.get(uiEngineState.inputEvents.keyTypedCharacters.size - 1);
                    uiCommonUtils.mouseTextInput_selectCharacter(uiEngineState.openMouseTextInput, typedChar);
                }
                uiCommonUtils.setKeyboardInteractedUIObject(focusedTextField);
            }

        }
        if (uiEngineState.inputEvents.keyDown) {
            final Textfield focusedTextField = uiEngineState.focusedTextField;
            final boolean processKeyDown = updateUI_keyInteractionsKeyProcessKey(focusedTextField);

            if (processKeyDown) {
                // TextField Control Keys
                for (int ik = 0; ik < uiEngineState.inputEvents.keyDownKeyCodes.size; ik++) {
                    int keyDownKeyCode = uiEngineState.inputEvents.keyDownKeyCodes.get(ik);
                    if (uiCommonUtils.textField_isControlKey(keyDownKeyCode)) {
                        // Repeat certain Control Keys
                        if (uiCommonUtils.textField_isRepeatedControlKey(keyDownKeyCode)) {
                            uiEngineState.focusedTextField_repeatedKey = keyDownKeyCode;
                            uiEngineState.focusedTextField_repeatedKeyTimer = -20;
                        }
                        uiCommonUtils.textField_executeControlKey(focusedTextField, keyDownKeyCode);
                    } else if (keyDownKeyCode == KeyCode.Key.V && uiEngineState.inputEvents.keysDown[KeyCode.Key.CONTROL_LEFT]) { // paste
                        String pasteContent = getClipboardContent();
                        if (pasteContent != null) {
                            char[] contentChars = pasteContent.toCharArray();
                            for (char i = 0; i < contentChars.length; i++) {
                                uiCommonUtils.textField_typeCharacter(focusedTextField, contentChars[i]);
                            }
                        }

                    }

                    uiCommonUtils.setKeyboardInteractedUIObject(focusedTextField);
                }
            } else {
                // Hotkeys
                for (int ihk = 0; ihk < uiEngineState.hotKeys.size; ihk++) {
                    HotKey hotKey = uiEngineState.hotKeys.get(ihk);
                    if (!hotKey.pressed) {
                        boolean hotKeyPressed = true;
                        for (int ikc = 0; ikc < hotKey.keyCodes.length; ikc++) {
                            if (!uiEngineState.inputEvents.keysDown[hotKey.keyCodes[ikc]]) {
                                hotKeyPressed = false;
                                break;
                            }
                        }
                        if (hotKeyPressed) uiCommonUtils.hotkey_press(hotKey);
                    }
                }
            }
        }
        if (uiEngineState.inputEvents.keyUp) {


            // Hotkeys
            for (int ik = 0; ik < uiEngineState.inputEvents.keyUpKeyCodes.size; ik++) {
                int keyUpKeyCode = uiEngineState.inputEvents.keyUpKeyCodes.get(ik);
                // Reset RepeatKey
                if (uiCommonUtils.textField_isRepeatedControlKey(keyUpKeyCode)) {
                    uiEngineState.focusedTextField_repeatedKey = KeyCode.NONE;
                    uiEngineState.focusedTextField_repeatedKeyTimer = 0;
                }
                // Reset Hotkeys
                for (int ihk = 0; ihk < uiEngineState.hotKeys.size; ihk++) {
                    HotKey hotKey = uiEngineState.hotKeys.get(ihk);
                    if (hotKey.pressed) {
                        hkLoop:
                        for (int ikc = 0; ikc < hotKey.keyCodes.length; ikc++) {
                            if (hotKey.keyCodes[ikc] == keyUpKeyCode) {
                                hotKey.pressed = false;
                                uiCommonUtils.hotkey_release(hotKey);
                                break hkLoop;
                            }
                        }
                    }
                }
            }
        }
    }

    private String getClipboardContent() {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable contents = clipboard.getContents(null);

        if (contents == null || !contents.isDataFlavorSupported(DataFlavor.stringFlavor))
            return null;

        try {
            return (String) contents.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean updateUI_mouseInteractionProcessMouseclick(Object lastUIMouseHover) {
        if (lastUIMouseHover != null) {

            if (lastUIMouseHover instanceof Component component) {
                if (component.disabled || !component.visible)
                    return false;

                if (uiCommonUtils.window_isModalOpen(uiEngineState)) {
                    if (component.addedToWindow == null) {
                        return false;
                    } else if (component.addedToWindow != uiEngineState.modalWindow) {
                        return false;
                    }
                }
            } else if (lastUIMouseHover instanceof Window window) {
                if (!window.visible)
                    return false;

                if (uiCommonUtils.window_isModalOpen(uiEngineState)) {
                    if (window != uiEngineState.modalWindow) {
                        return false;
                    }
                }

            }
        } else {
            return false;
        }

        return true;
    }

    private void updateUI_mouseInteractions() {
        uiCommonUtils.setMouseInteractedUIObject(null);
        if (uiEngineState.config.ui_mouseInteractionsDisabled) return;
        // ------ MOUSE DOUBLE CLICK ------
        if (uiEngineState.inputEvents.mouseDoubleClick) {
            final Object lastUIMouseHover = uiEngineState.lastUIMouseHover;
            final boolean processMouseDoubleClick = updateUI_mouseInteractionProcessMouseclick(uiEngineState.lastUIMouseHover);

            if (processMouseDoubleClick) {
                if (lastUIMouseHover instanceof Window window) {
                    for (int ib = 0; ib < uiEngineState.inputEvents.mouseDownButtons.size; ib++) {
                        int mouseDownButton = uiEngineState.inputEvents.mouseDownButtons.get(ib);
                        if (uiEngineState.config.ui_foldWindowsOnDoubleClick && mouseDownButton == Input.Buttons.LEFT) {
                            if (window.hasTitleBar && Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y, window.x, window.y + TS(window.height - 1), TS(window.width), TS())) {
                                if (window.folded) {
                                    uiCommonUtils.window_unFold(window);
                                } else {
                                    uiCommonUtils.window_fold(window);
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

                uiCommonUtils.setMouseInteractedUIObject(lastUIMouseHover);
            }
        }
        // ------ MOUSE DOWN ------
        if (uiEngineState.inputEvents.mouseDown) {
            final Object lastUIMouseHover = uiEngineState.lastUIMouseHover;
            final boolean processMouseclick = updateUI_mouseInteractionProcessMouseclick(uiEngineState.lastUIMouseHover);

            if (processMouseclick) {
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
                                case DEFAULT -> uiCommonUtils.button_press(button);
                                case TOGGLE -> uiCommonUtils.button_toggle(button);
                            }
                        }
                        case ContextMenuItem contextMenuItem -> {
                            uiEngineState.pressedContextMenuItem = contextMenuItem;
                        }
                        case ScrollbarVertical scrollBarVertical -> {
                            uiCommonUtils.scrollBar_pressButton(scrollBarVertical);
                            uiCommonUtils.scrollBar_scroll(scrollBarVertical,
                                    uiCommonUtils.scrollBar_calculateScrolled(scrollBarVertical, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y));
                            uiEngineState.pressedScrollBarVertical = scrollBarVertical;
                        }
                        case ScrollbarHorizontal scrollBarHorizontal -> {
                            uiCommonUtils.scrollBar_pressButton(scrollBarHorizontal);
                            uiCommonUtils.scrollBar_scroll(scrollBarHorizontal,
                                    uiCommonUtils.scrollBar_calculateScrolled(scrollBarHorizontal, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y));
                            uiEngineState.pressedScrollBarHorizontal = scrollBarHorizontal;
                        }
                        case Combobox comboBox -> {
                            if (uiCommonUtils.comboBox_isOpen(comboBox)) {
                                if (Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y,
                                        uiCommonUtils.component_getAbsoluteX(comboBox), uiCommonUtils.component_getAbsoluteY(comboBox),
                                        TS(comboBox.width), TS())) {
                                    // Clicked on Combobox itself -> close
                                    uiCommonUtils.comboBox_close(comboBox);
                                } else {
                                    // Clicked on Item
                                    for (int i = 0; i < comboBox.items.size; i++) {
                                        if (Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y,
                                                uiCommonUtils.component_getAbsoluteX(comboBox),
                                                uiCommonUtils.component_getAbsoluteY(comboBox) - TS(i) - TS(),
                                                TS(comboBox.width),
                                                TS())) {
                                            uiEngineState.pressedComboBoxItem = comboBox.items.get(i);
                                        }
                                    }
                                }


                            } else {
                                // Open this combobox
                                uiCommonUtils.comboBox_open(comboBox);
                            }
                        }
                        case Knob knob -> {
                            uiEngineState.pressedKnob = knob;
                            knob.knobAction.onPress();
                        }
                        case FrameBufferViewport frameBufferViewport -> {
                            frameBufferViewport.frameBufferViewportAction.onPress(
                                    uiCommonUtils.component_getRelativeMouseX(uiEngineState.mouse_ui.x, frameBufferViewport),
                                    uiCommonUtils.component_getRelativeMouseY(uiEngineState.mouse_ui.y, frameBufferViewport));
                            uiEngineState.pressedFramebufferViewport = frameBufferViewport;
                        }
                        case AppViewport appViewPort -> {
                            appViewPort.appViewPortAction.onPress(
                                    uiCommonUtils.component_getRelativeMouseX(uiEngineState.mouse_ui.x, appViewPort),
                                    uiCommonUtils.component_getRelativeMouseY(uiEngineState.mouse_ui.y, appViewPort));
                            uiEngineState.pressedAppViewPort = appViewPort;
                        }
                        case Textfield textField -> {
                            int textFieldMouseX = uiCommonUtils.component_getRelativeMouseX(uiEngineState.mouse_ui.x, textField);
                            uiEngineState.pressedTextField = textField;

                            int textPosition = uiCommonUtils.textField_findTextPosition(textField, textFieldMouseX);

                            uiEngineState.pressedTextFieldInitCaretPosition = textPosition;
                            textField.markedContentBegin = textPosition;
                            textField.markedContentEnd = textPosition;
                            uiCommonUtils.textField_setCaretPosition(textField, textPosition);

                            // Set Focus
                            uiCommonUtils.textField_focus(textField);

                        }
                        case Grid grid -> {
                            int tileSize = grid.bigMode ? TS2() : TS();
                            int x_grid = uiCommonUtils.component_getAbsoluteX(grid);
                            int y_grid = uiCommonUtils.component_getAbsoluteY(grid);
                            int inv_x = (uiEngineState.mouse_ui.x - x_grid) / tileSize;
                            int inv_y = (uiEngineState.mouse_ui.y - y_grid) / tileSize;
                            if (uiCommonUtils.grid_positionValid(grid, inv_x, inv_y)) {
                                Object pressedGridItem = grid.items[inv_x][inv_y];
                                if (pressedGridItem != null && grid.dragEnabled) {
                                    uiEngineState.draggedGridFrom.set(inv_x, inv_y);
                                    uiEngineState.draggedGridOffset.set(uiEngineState.mouse_ui.x - (x_grid + (inv_x * tileSize)), uiEngineState.mouse_ui.y - (y_grid + (inv_y * tileSize)));
                                    uiEngineState.draggedGridItem = grid.items[inv_x][inv_y];
                                    uiEngineState.draggedGrid = grid;
                                }
                                uiEngineState.pressedGrid = grid;
                                uiEngineState.pressedGridItem = pressedGridItem;
                            }
                        }
                        case List list -> {
                            uiCommonUtils.list_updateItemInfoAtMousePosition(list);
                            Object pressedListItem = null;
                            if (uiEngineState.itemInfo_listValid) {
                                pressedListItem = (uiEngineState.itemInfo_listIndex < list.items.size) ? list.items.get(uiEngineState.itemInfo_listIndex) : null;
                            }
                            if (pressedListItem != null && list.dragEnabled) {
                                uiEngineState.draggedListFromIndex = uiEngineState.itemInfo_listIndex;
                                uiEngineState.draggedListOffset.set(uiEngineState.mouse_ui.x - (uiCommonUtils.component_getAbsoluteX(list)),
                                        (uiEngineState.mouse_ui.y - uiCommonUtils.component_getAbsoluteY(list)) % 8);
                                uiEngineState.draggedListItem = pressedListItem;
                                uiEngineState.draggedList = list;
                            }
                            uiEngineState.pressedList = list;
                            uiEngineState.pressedListItem = pressedListItem;
                        }
                        case Tabbar tabBar -> {
                            uiCommonUtils.tabBar_updateItemInfoAtMousePosition(tabBar);
                            if (uiEngineState.itemInfo_tabBarValid) {
                                if (tabBar.selectedTab != uiEngineState.itemInfo_tabBarTabIndex)
                                    uiCommonUtils.tabBar_selectTab(tabBar, uiEngineState.itemInfo_tabBarTabIndex);
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
                        uiCommonUtils.window_bringToFront(uiEngineState.draggedWindow);
                    }

                    // Unfocus focused textfields
                    if (uiEngineState.focusedTextField != null && lastUIMouseHover != uiEngineState.focusedTextField) {
                        uiCommonUtils.textField_unFocus(uiEngineState.focusedTextField);
                    }
                }

                // Close opened ComboBoxes
                if (uiEngineState.openComboBox != null && lastUIMouseHover != uiEngineState.openComboBox) {
                    uiCommonUtils.comboBox_close(uiEngineState.openComboBox);
                }

                // Close opened ContextMenus
                if (uiEngineState.openContextMenu != null) {
                    if (!(lastUIMouseHover instanceof ContextMenuItem contextMenuItem) || contextMenuItem.addedToContextMenu != uiEngineState.openContextMenu) {
                        uiCommonUtils.contextMenu_close(uiEngineState.openContextMenu);
                    }
                }

                // Execute Common Actions
                for (int ib = 0; ib < uiEngineState.inputEvents.mouseDownButtons.size; ib++) {
                    int mouseDownButton = uiEngineState.inputEvents.mouseDownButtons.get(ib);
                    actions_executeOnMousePressCommonAction(lastUIMouseHover, mouseDownButton);
                }

                uiCommonUtils.setMouseInteractedUIObject(lastUIMouseHover);
            }
        }
        // ------ MOUSE UP ------
        if (uiEngineState.inputEvents.mouseUp) {
            Object lastUIMouseHover = uiEngineState.lastUIMouseHover;
            boolean processMouseUpPressed = true;
            boolean processMouseUpDragged = true;
            // Press interaction
            Object pressedUIObject = uiCommonUtils.getPressedUIReference(uiEngineState);
            if (pressedUIObject == null) processMouseUpPressed = false;
            // Drag interaction
            Object draggedUIObject = uiCommonUtils.getDraggedUIReference(uiEngineState);
            if (draggedUIObject == null) processMouseUpDragged = false;


            if (processMouseUpPressed) {
                switch (pressedUIObject) {
                    case ContextMenuItem contextMenuItem -> {
                        uiCommonUtils.contextMenu_selectItem(contextMenuItem);
                        uiCommonUtils.contextMenu_close(contextMenuItem.addedToContextMenu);
                    }
                    case ComboboxItem comboBoxItem -> {
                        uiCommonUtils.comboBox_selectItem(comboBoxItem);
                        if (uiEngineState.currentControlMode.emulated && comboBoxItem.addedToComboBox != null) {
                            // emulated: move mouse back to combobox on item select
                            uiEngineState.mouse_emulated.y = uiCommonUtils.component_getAbsoluteY(comboBoxItem.addedToComboBox) + TS_HALF();
                        }
                        uiCommonUtils.resetPressedComboBoxItemReference(uiEngineState);
                    }
                    case Checkbox checkBox -> {
                        checkBox.checked = !checkBox.checked;
                        checkBox.checkBoxAction.onCheck(checkBox.checked);
                        uiCommonUtils.resetPressedCheckBoxReference(uiEngineState);
                    }
                    case Textfield textField -> {
                        uiCommonUtils.resetPressedTextFieldReference(uiEngineState);
                    }
                    case AppViewport appViewPort -> {
                        appViewPort.appViewPortAction.onRelease();
                        uiCommonUtils.resetPressedAppViewPortReference(uiEngineState);
                    }
                    case Button button -> {
                        uiCommonUtils.button_release(button);
                        uiCommonUtils.resetPressedButtonReference(uiEngineState);
                    }
                    case ScrollbarVertical scrollBarVertical -> {
                        uiCommonUtils.scrollBar_releaseButton(scrollBarVertical);
                        uiCommonUtils.resetPressedScrollBarVerticalReference(uiEngineState);
                    }
                    case ScrollbarHorizontal scrollBarHorizontal -> {
                        uiCommonUtils.scrollBar_releaseButton(scrollBarHorizontal);
                        uiCommonUtils.resetPressedScrollBarHorizontalReference(uiEngineState);
                    }
                    case Knob knob -> {
                        knob.knobAction.onRelease();
                        uiCommonUtils.resetPressedKnobReference(uiEngineState);
                    }
                    case FrameBufferViewport frameBufferViewport -> {
                        frameBufferViewport.frameBufferViewportAction.onRelease();
                        uiCommonUtils.resetPressedFrameBufferViewPortReference(uiEngineState);
                    }
                    case Grid grid -> {
                        uiCommonUtils.grid_updateItemInfoAtMousePosition(grid);

                        if (uiEngineState.draggedGrid == null || uiEngineState.itemInfo_gridPos.equals(uiEngineState.draggedGridFrom)) { // Only when not dragged elsewhere
                            boolean select = grid.gridAction.onItemSelected(uiEngineState.pressedGridItem);

                            if (uiEngineState.pressedGridItem != null) {
                                if (select) {
                                    if (grid.multiSelect) {
                                        Array selectedNew = new Array();
                                        selectedNew.addAll(grid.selectedItems);

                                        if (grid.selectedItems.contains(uiEngineState.pressedGridItem)) {
                                            selectedNew.removeValue(uiEngineState.pressedGridItem, true);
                                        } else {
                                            selectedNew.add(uiEngineState.pressedGridItem);
                                        }

                                        uiCommonUtils.grid_setSelectedItems(grid, selectedNew.toArray());
                                    } else {
                                        uiCommonUtils.grid_setSelectedItem(grid, uiEngineState.pressedGridItem);
                                    }
                                }
                            } else {
                                if (select) {
                                    grid.selectedItems.clear();
                                    grid.selectedItem = null;
                                }
                            }
                        }
                        uiCommonUtils.resetPressedGridReference(uiEngineState);
                    }
                    case List list -> {
                        uiCommonUtils.list_updateItemInfoAtMousePosition(list);
                        if (uiEngineState.draggedList == null || uiEngineState.itemInfo_listIndex == uiEngineState.draggedListFromIndex) { // Only when not dragged elsewhere
                            boolean select = list.listAction.onItemSelected(uiEngineState.pressedListItem);
                            if (uiEngineState.pressedListItem != null) {
                                if (select) {
                                    if (list.multiSelect) {
                                        Array selectedNew = new Array();
                                        selectedNew.addAll(list.selectedItems);

                                        if (list.selectedItems.contains(uiEngineState.pressedListItem))
                                            selectedNew.removeValue(uiEngineState.pressedListItem, true);
                                        else selectedNew.add(uiEngineState.pressedListItem);

                                        uiCommonUtils.list_setSelectedItems(list, selectedNew.toArray());
                                    } else {
                                        uiCommonUtils.list_setSelectedItem(list, uiEngineState.pressedListItem);
                                    }
                                }
                            } else {
                                if (select) {
                                    list.selectedItems.clear();
                                    list.selectedItem = null;
                                }
                            }
                        }
                        uiCommonUtils.resetPressedListReference(uiEngineState);
                    }
                    case null, default -> {
                    }
                }
                uiCommonUtils.setMouseInteractedUIObject(pressedUIObject);
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
                                if (uiCommonUtils.list_canDragIntoList(hoverList)) {
                                    uiCommonUtils.list_updateItemInfoAtMousePosition(hoverList);
                                    if (uiEngineState.itemInfo_listValid) {
                                        int toIndex = uiEngineState.itemInfo_listIndex;
                                        hoverList.listAction.onDragFromList(list, dragFromIndex, toIndex);
                                    }
                                }
                            } else if (lastUIMouseHover instanceof Grid hoverGrid) {
                                if (uiCommonUtils.grid_canDragIntoGrid(hoverGrid)) {
                                    uiCommonUtils.grid_updateItemInfoAtMousePosition(hoverGrid);
                                    if (uiEngineState.itemInfo_gridValid) {
                                        hoverGrid.gridAction.onDragFromList(list, dragFromIndex,
                                                uiEngineState.itemInfo_gridPos.x, uiEngineState.itemInfo_gridPos.y);
                                    }
                                }
                            }
                        } else if (uiCommonUtils.list_canDragIntoScreen(list)) {
                            list.listAction.onDragIntoApp(
                                    dragItem, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y
                            );
                        }
                        // reset
                        uiCommonUtils.resetDraggedListReference(uiEngineState);
                    }
                    case Grid grid -> {
                        int dragFromX = uiEngineState.draggedGridFrom.x;
                        int dragFromY = uiEngineState.draggedGridFrom.y;
                        Object dragItem = uiEngineState.draggedGridItem;
                        if (lastUIMouseHover != null) {
                            if (lastUIMouseHover instanceof Grid hoverGrid) {
                                if (uiCommonUtils.grid_canDragIntoGrid(hoverGrid)) {
                                    uiCommonUtils.grid_updateItemInfoAtMousePosition(hoverGrid);
                                    if (uiEngineState.itemInfo_gridValid) {
                                        hoverGrid.gridAction.onDragFromGrid(grid,
                                                dragFromX, dragFromY,
                                                uiEngineState.itemInfo_gridPos.x, uiEngineState.itemInfo_gridPos.y);
                                    }
                                }
                            } else if (uiEngineState.lastUIMouseHover instanceof List hoverList) {
                                if (uiCommonUtils.list_canDragIntoList(hoverList)) {
                                    uiCommonUtils.list_updateItemInfoAtMousePosition(hoverList);
                                    if (uiEngineState.itemInfo_listValid) {
                                        int toIndex = uiEngineState.itemInfo_listIndex;
                                        hoverList.listAction.onDragFromGrid(grid, dragFromX, dragFromY, toIndex);
                                    }
                                }
                            }
                        } else if (uiCommonUtils.grid_canDragIntoScreen(grid)) {
                            grid.gridAction.onDragIntoApp(
                                    dragItem, dragFromX, dragFromY, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y
                            );
                        }
                        // reset
                        uiCommonUtils.resetDraggedGridReference(uiEngineState);
                    }
                    case null, default -> {
                    }
                }

                uiCommonUtils.setMouseInteractedUIObject(draggedUIObject);
            }

            // Execute Common Actions
            for (int ib = 0; ib < uiEngineState.inputEvents.mouseUpButtons.size; ib++) {
                int mouseDownButton = uiEngineState.inputEvents.mouseUpButtons.get(ib);
                actions_executeOnMouseReleaseCommonAction(lastUIMouseHover, mouseDownButton);
            }
        }
        // ------ MOUSE DRAGGED ------
        if (uiEngineState.inputEvents.mouseDragged) {
            Object lastUIMouseHover = uiEngineState.lastUIMouseHover;
            boolean processMouseDraggedPressed = true;
            boolean processMouseDraggedDragged = true;
            // Press interaction
            Object pressedUIObject = uiCommonUtils.getPressedUIReference(uiEngineState);
            if (pressedUIObject == null) processMouseDraggedPressed = false;
            // Drag interaction
            Object draggedUIObject = uiCommonUtils.getDraggedUIReference(uiEngineState);
            if (draggedUIObject == null) processMouseDraggedDragged = false;

            if (processMouseDraggedPressed) {
                switch (pressedUIObject) {
                    case ScrollbarVertical scrolledScrollBarVertical -> {
                        uiCommonUtils.scrollBar_scroll(scrolledScrollBarVertical, uiCommonUtils.scrollBar_calculateScrolled(scrolledScrollBarVertical, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y));
                    }
                    case ScrollbarHorizontal scrolledScrollBarHorizontal -> {
                        uiCommonUtils.scrollBar_scroll(scrolledScrollBarHorizontal, uiCommonUtils.scrollBar_calculateScrolled(scrolledScrollBarHorizontal, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y));
                    }
                    case Knob turnedKnob -> {
                        final float BASE_SENSITIVITY = 1 / 50f;
                        float amount = (uiEngineState.mouse_delta.y * BASE_SENSITIVITY) * uiEngineState.config.component_knobSensitivity;
                        float newValue = turnedKnob.turned + amount;
                        uiCommonUtils.knob_turnKnob(turnedKnob, newValue);
                        if (uiEngineState.currentControlMode.emulated) {
                            // emulated: keep mouse position steady
                            uiCommonUtils.emulatedMouse_setPositionComponent(turnedKnob);
                        }
                    }
                    case Textfield textField -> {
                        int textFieldMouseX = uiCommonUtils.component_getRelativeMouseX(uiEngineState.mouse_ui.x, textField);
                        int textPosition = uiCommonUtils.textField_findTextPosition(textField, textFieldMouseX);
                        textField.markedContentBegin = Math.min(textPosition, uiEngineState.pressedTextFieldInitCaretPosition);
                        textField.markedContentEnd = Math.max(textPosition, uiEngineState.pressedTextFieldInitCaretPosition);

                        if (textFieldMouseX < 0) {
                            textPosition -= 1;
                        }

                        uiCommonUtils.textField_setCaretPosition(textField, textPosition);
                    }
                    case null, default -> {
                    }
                }
                uiCommonUtils.setMouseInteractedUIObject(pressedUIObject);
            }
            if (processMouseDraggedDragged) {
                switch (draggedUIObject) {
                    case Window draggedWindow -> {
                        uiCommonUtils.window_setPosition(draggedWindow,
                                uiEngineState.mouse_ui.x - uiEngineState.draggedWindow_offset.x,
                                uiEngineState.mouse_ui.y - uiEngineState.draggedWindow_offset.y);

                        draggedWindow.windowAction.onMove(draggedWindow.x, draggedWindow.y);
                    }
                    case null, default -> {
                    }
                }
                uiCommonUtils.setMouseInteractedUIObject(draggedUIObject);
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
                final float BASE_SENSITIVITY = (-1 / 20f);
                switch (lastUIMouseHover) {
                    case List list -> {
                        int size = list.items != null ? list.items.size : 0;
                        float amount = (1 / (float) Math.max(size, 1)) * uiEngineState.inputEvents.mouseScrolledAmount;
                        uiCommonUtils.list_scroll(list, list.scrolled + amount);
                    }
                    case Knob knob -> {
                        float amount = (BASE_SENSITIVITY * uiEngineState.inputEvents.mouseScrolledAmount) * uiEngineState.config.component_knobSensitivity;
                        float newValue = knob.turned + amount;
                        uiCommonUtils.knob_turnKnob(knob, newValue);
                    }
                    case ScrollbarHorizontal scrollBarHorizontal -> {
                        float amount = (BASE_SENSITIVITY * uiEngineState.inputEvents.mouseScrolledAmount) * uiEngineState.config.component_scrollbarSensitivity;
                        uiCommonUtils.scrollBar_scroll(scrollBarHorizontal, scrollBarHorizontal.scrolled + amount);
                    }
                    case ScrollbarVertical scrollBarVertical -> {
                        float amount = (BASE_SENSITIVITY * uiEngineState.inputEvents.mouseScrolledAmount) * uiEngineState.config.component_scrollbarSensitivity;
                        uiCommonUtils.scrollBar_scroll(scrollBarVertical, scrollBarVertical.scrolled + amount);
                    }
                    case null, default -> {
                    }
                }

                // Execute Common Actions
                actions_executeOnMouseScrollCommonAction(lastUIMouseHover, uiEngineState.inputEvents.mouseScrolledAmount);

                uiCommonUtils.setMouseInteractedUIObject(lastUIMouseHover);
            }
        }
    }

    private void updateUI_continuousComponentActivities() {
        // TextField Repeat
        if (uiEngineState.focusedTextField_repeatedKey != KeyCode.NONE) {
            uiEngineState.focusedTextField_repeatedKeyTimer++;
            if (uiEngineState.focusedTextField_repeatedKeyTimer > 2) {
                uiCommonUtils.textField_executeControlKey(uiEngineState.focusedTextField, uiEngineState.focusedTextField_repeatedKey);
                uiEngineState.focusedTextField_repeatedKeyTimer = 0;
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
        for (int i = 0; i < uiEngineState.screenComponents.size; i++) {
            Component component = uiEngineState.screenComponents.get(i);
            for (int i2 = 0; i2 < component.updateActions.size; i2++) {
                actions_executeUpdateAction(component.updateActions.get(i2));
            }
        }
        for (int i = 0; i < uiEngineState.windows.size; i++) {
            // Window UpdateActions
            Window window = uiEngineState.windows.get(i);
            for (int i2 = 0; i2 < window.updateActions.size; i2++) {
                actions_executeUpdateAction(window.updateActions.get(i2));
            }
            // Window Component UpdateActions
            for (int i2 = 0; i2 < window.components.size; i2++) {
                Component component = window.components.get(i2);
                for (int i3 = 0; i3 < component.updateActions.size; i3++) {
                    actions_executeUpdateAction(component.updateActions.get(i3));
                }
            }
        }

        // Tooltip
        if (uiEngineState.tooltip != null) {
            for (int i = 0; i < uiEngineState.tooltip.updateActions.size; i++) {
                actions_executeUpdateAction(uiEngineState.tooltip.updateActions.get(i));
            }
        }

        // Engine SingleUpdateActions
        for (int i = 0; i < uiEngineState.singleUpdateActions.size; i++) {
            UpdateAction updateAction = uiEngineState.singleUpdateActions.get(i);
            if (this.actions_executeUpdateAction(updateAction)) {
                uiEngineState.singleUpdateActionsRemoveQueue.addLast(updateAction);
            }
        }
        while (!uiEngineState.singleUpdateActionsRemoveQueue.isEmpty()) {
            final UpdateAction removeUpdateAction = uiEngineState.singleUpdateActionsRemoveQueue.removeFirst();
            uiEngineState.singleUpdateActions.removeValue(removeUpdateAction, true);
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
                uiCommonUtils.list_updateItemInfoAtMousePosition(list);
                if (uiEngineState.itemInfo_listValid) {
                    toolTipSubItem = uiEngineState.itemInfo_listIndex < list.items.size ? list.items.get(uiEngineState.itemInfo_listIndex) : null;
                }
            } else if (hoverComponent instanceof Grid grid) {
                int tileSize = grid.bigMode ? TS2() : TS();
                int x_grid = uiCommonUtils.component_getAbsoluteX(grid);
                int y_grid = uiCommonUtils.component_getAbsoluteY(grid);
                int inv_x = (uiEngineState.mouse_ui.x - x_grid) / tileSize;
                int inv_y = (uiEngineState.mouse_ui.y - y_grid) / tileSize;
                if (uiCommonUtils.grid_positionValid(grid, inv_x, inv_y)) {
                    toolTipSubItem = grid.items[inv_x][inv_y];
                }
            }

            boolean updateComponentToolTip;
            if (uiEngineState.forceTooltipUpdateComponents.contains(hoverComponent, true)) {
                updateComponentToolTip = true;
                uiEngineState.forceTooltipUpdateComponents.removeValue(hoverComponent, true);
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
                    if (toolTipSubItem != null) {
                        uiEngineState.tooltip = list.listAction.toolTip(toolTipSubItem);
                    } else {
                        uiEngineState.tooltip = null;
                    }
                    uiEngineState.tooltip_lastHoverObject = toolTipSubItem;
                } else if (hoverComponent instanceof Grid grid && toolTipSubItem != null) {
                    // check for Grid item tooltip
                    if (toolTipSubItem != null) {
                        uiEngineState.tooltip = grid.gridAction.toolTip(toolTipSubItem);
                    } else {
                        uiEngineState.tooltip = null;
                    }
                    uiEngineState.tooltip_lastHoverObject = toolTipSubItem;
                } else {
                    // take component tooltip
                    uiEngineState.tooltip = actions_getUIObjectCommonActions(hoverComponent).onShowTooltip();
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
                    uiEngineState.tooltip.toolTipAction.onDisplay();
                }
            } else if (uiEngineState.tooltip_fadePct < 1f) {
                uiEngineState.tooltip_fadePct = Math.clamp(uiEngineState.tooltip_fadePct + uiEngineState.config.tooltip_FadeInSpeed, 0f, 1f);
            } else {
                uiEngineState.tooltip.toolTipAction.onUpdate();
            }

            uiEngineState.fadeOutTooltip = uiEngineState.tooltip;
        } else {
            if (uiEngineState.fadeOutTooltip != null) {
                if (uiEngineState.tooltip_fadePct > 0f) {
                    uiEngineState.tooltip_fadePct = Math.clamp(uiEngineState.tooltip_fadePct - uiEngineState.config.tooltip_FadeoutSpeed, 0f, 1f);
                } else {
                    uiEngineState.fadeOutTooltip.toolTipAction.onRemove();
                    uiEngineState.fadeOutTooltip = null;
                }
            }
        }
    }


    private void updateUI_notifications() {
        if (!uiEngineState.notifications.isEmpty()) {
            Notification notification = uiEngineState.notifications.first();

            switch (notification.state) {
                case INIT_SCROLL -> {
                    notification.timer = 0;
                    notification.state = TOP_NOTIFICATION_STATE.SCROLL;
                }
                case INIT_DISPLAY -> {
                    notification.timer = 0;
                    notification.state = TOP_NOTIFICATION_STATE.DISPLAY;
                }
                case SCROLL -> {
                    notification.timer++;
                    if (notification.timer > 30) {
                        notification.scroll += 1;
                        if (notification.scroll >= notification.scrollMax) {
                            notification.state = TOP_NOTIFICATION_STATE.DISPLAY;
                            notification.timer = 0;
                        } else {
                            notification.timer = 30;
                        }
                    }
                }
                case DISPLAY -> {
                    notification.timer++;
                    if (notification.timer > notification.displayTime) {
                        notification.state = TOP_NOTIFICATION_STATE.FOLD;
                        notification.timer = 0;
                    }
                }
                case FOLD -> {
                    notification.timer++;
                    if (notification.timer > uiEngineState.config.notification_foldTime) {
                        notification.timer = 0;
                        notification.state = TOP_NOTIFICATION_STATE.FINISHED;
                        uiCommonUtils.notification_removeFromScreen(notification);
                    }
                }
                case FINISHED -> {
                }
            }
        }
        if (!uiEngineState.tooltipNotifications.isEmpty()) {
            for (int i = 0; i < uiEngineState.tooltipNotifications.size; i++) {
                TooltipNotification tooltipNotification = uiEngineState.tooltipNotifications.get(i);
                switch (tooltipNotification.state) {
                    case INIT -> {
                        tooltipNotification.state = TOOLTIP_NOTIFICATION_STATE.DISPLAY;
                    }
                    case DISPLAY -> {
                        tooltipNotification.timer++;
                        if (tooltipNotification.timer > tooltipNotification.displayTime) {
                            tooltipNotification.state = TOOLTIP_NOTIFICATION_STATE.FADE;
                            tooltipNotification.timer = 0;
                        }
                    }
                    case FADE -> {
                        tooltipNotification.timer++;
                        if (tooltipNotification.timer > api.config.notification.tooltip.getFadeoutTime()) {
                            uiCommonUtils.notification_removeFromScreen(tooltipNotification);
                            tooltipNotification.state = TOOLTIP_NOTIFICATION_STATE.FINISHED;
                            tooltipNotification.timer = 0;
                        }
                    }
                    case FINISHED -> {
                    }
                }
            }
        }
    }


    private void actions_executeOnMousePressCommonAction(Object uiObject, int button) {
        if (uiObject == null) return;
        CommonActions commonActions = actions_getUIObjectCommonActions(uiObject);
        if (commonActions != null) commonActions.onMousePress(button);
        if (uiObject instanceof Component component) {
            // Execute for parent window too
            actions_executeOnMousePressCommonAction(component.addedToWindow, button);
        }
    }

    private void actions_executeOnMouseReleaseCommonAction(Object uiObject, int button) {
        if (uiObject == null) return;
        CommonActions commonActions = actions_getUIObjectCommonActions(uiObject);
        if (commonActions != null) commonActions.onMouseRelease(button);
        if (uiObject instanceof Component component) {
            // Execute for parent window too
            actions_executeOnMousePressCommonAction(component.addedToWindow, button);
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
            case Checkbox checkbox -> checkbox.checkBoxAction;
            case Combobox comboBox -> comboBox.comboBoxAction;
            case AppViewport appViewPort -> appViewPort.appViewPortAction;
            case Image image -> image.imageAction;
            case Grid grid -> grid.gridAction;
            case List list -> list.listAction;
            case FrameBufferViewport frameBufferViewport -> frameBufferViewport.frameBufferViewportAction;
            case ScrollbarVertical scrollBarVertical -> scrollBarVertical.scrollBarAction;
            case ScrollbarHorizontal scrollBarHorizontal -> scrollBarHorizontal.scrollBarAction;
            case Tabbar tabBar -> tabBar.tabBarAction;
            case Text text -> text.textAction;
            case Textfield textField -> textField.textFieldAction;
            case Progressbar progressbar -> progressbar.progressBarAction;
            case Shape shape -> shape.shapeAction;
            case Knob knob -> knob.knobAction;
            case null, default -> null;
        };
    }

    private boolean actions_executeUpdateAction(UpdateAction updateAction) {
        updateAction.timer++;
        if (updateAction.timer >= updateAction.interval) {
            updateAction.onUpdate();
            updateAction.timer = 0;
            return true;
        }
        return false;
    }

    public void render() {
        render(true);
    }

    public void render(boolean drawToScreen) {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        { // Draw App Layer
            // Draw Main FrameBuffer
            uiEngineState.frameBuffer_app.beginGlClear();
            this.uiAdapter.render(uiEngineState.camera_app, null);
            uiEngineState.frameBuffer_app.end();
            // Draw UI AppViewport FrameBuffers
            for (int i = 0; i < this.uiEngineState.appViewPorts.size; i++) {
                renderGameViewPortFrameBuffer(uiEngineState.appViewPorts.get(i));
            }
        }

        { // Draw GUI Layer
            uiEngineState.frameBufferComponent_ui.beginGlClear();
            this.renderUIComponentLayer();
            uiEngineState.frameBufferComponent_ui.end();

            uiEngineState.frameBufferModal_ui.beginGlClear();
            this.renderUIModalLayer();
            uiEngineState.frameBufferModal_ui.end();
        }

        { // Draw Composite Image
            uiEngineState.frameBuffer_composite.begin();


            this.uiAdapter.renderComposite(uiEngineState.camera_ui,
                    spriteRenderer,
                    uiEngineState.frameBuffer_app.getFlippedTextureRegion(),
                    uiEngineState.frameBufferComponent_ui.getFlippedTextureRegion(), uiEngineState.frameBufferModal_ui.getFlippedTextureRegion(),
                    uiEngineState.resolutionWidth, uiEngineState.resolutionHeight,
                    uiCommonUtils.window_isModalOpen(uiEngineState)
            );

            uiEngineState.frameBuffer_composite.end();
        }

        {
            // Draw Composite Image to Screen
            if (drawToScreen) {
                spriteRenderer.setProjectionMatrix(uiEngineState.camera_ui.combined);
                spriteRenderer.begin();

                if (uiEngineState.viewportMode.upscale) {
                    // Upscale Composite
                    uiEngineState.frameBuffer_upScaled_screen.begin();
                    render_glClear();
                    spriteRenderer.draw(uiEngineState.frameBuffer_composite.getFlippedTextureRegion(), 0, 0, uiEngineState.resolutionWidth, uiEngineState.resolutionHeight);
                    spriteRenderer.flush();
                    uiEngineState.frameBuffer_upScaled_screen.end();
                }

                render_glClear();
                uiEngineState.viewport_screen.apply();
                spriteRenderer.setProjectionMatrix(uiEngineState.camera_ui.combined);

                switch (uiEngineState.viewportMode) {
                    case STRETCH, FIT ->
                            spriteRenderer.draw(uiEngineState.frameBuffer_upScaled_screen.getFlippedTextureRegion(), 0, 0, uiEngineState.resolutionWidth, uiEngineState.resolutionHeight);
                    case PIXEL_PERFECT ->
                            spriteRenderer.draw(uiEngineState.frameBuffer_composite.getFlippedTextureRegion(), 0, 0, uiEngineState.resolutionWidth, uiEngineState.resolutionHeight);
                }

                spriteRenderer.end();
            }
        }


    }

    private void render_glClear() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT);
    }

    private void renderGameViewPortFrameBuffer(AppViewport appViewPort) {
        if (render_isComponentNotRendered(appViewPort)) return;
        appViewPort.updateTimer++;
        if (appViewPort.updateTimer > appViewPort.updateTime) {
            // draw to frambuffer
            appViewPort.frameBuffer.beginGlClear();
            this.uiAdapter.render(appViewPort.camera, appViewPort);
            appViewPort.frameBuffer.end();
            appViewPort.updateTimer = 0;
        }
    }

    private void renderUIModalLayer() {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        spriteRenderer.setProjectionMatrix(uiEngineState.camera_ui.combined);
        spriteRenderer.setBlendFunctionLayer();

        spriteRenderer.begin();

        // Modal Windows
        for (int i = 0; i < uiEngineState.windows.size; i++) {
            Window window = uiEngineState.windows.get(i);
            render_drawWindow(window, true);
        }

        // Notifications
        render_drawTooltipNotifications();

        render_drawTopNotifications();

        // Context Menu
        render_drawContextMenu();

        // Tooltip
        render_drawCursorTooltip();

        // OnScreenTextInput
        render_mouseTextInput();

        // Cursor Drag & Drop
        render_drawCursorDragAndDrop();

        // Cursor
        render_drawCursor();

        spriteRenderer.end();
    }

    private void renderUIComponentLayer() {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final PrimitiveRenderer primitiveRenderer = uiEngineState.primitiveRenderer_ui;

        spriteRenderer.setProjectionMatrix(uiEngineState.camera_ui.combined);
        spriteRenderer.setBlendFunctionLayer();
        primitiveRenderer.setProjectionMatrix(uiEngineState.camera_ui.combined);
        primitiveRenderer.setBlendFunctionLayer();

        spriteRenderer.begin();

        // Draw Screen Components
        for (int i = 0; i < uiEngineState.screenComponents.size; i++) {
            Component component = uiEngineState.screenComponents.get(i);
            render_drawComponent(component);
        }


        // Draw Screen Components Top Layer
        for (int i = 0; i < uiEngineState.screenComponents.size; i++) {
            Component component = uiEngineState.screenComponents.get(i);
            render_drawComponentTopLayer(component);
        }

        // Draw Windows
        for (int i = 0; i < uiEngineState.windows.size; i++) {
            Window window = uiEngineState.windows.get(i);
            render_drawWindow(window, false);

        }


        spriteRenderer.end();

        spriteRenderer.setAllReset();
    }

    private void render_mouseTextInput() {
        if (uiEngineState.openMouseTextInput == null) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final MouseTextInput mouseTextInput = uiEngineState.openMouseTextInput;
        final Color color1 = uiEngineState.openMouseTextInput.color;
        final Color color2 = uiEngineState.openMouseTextInput.color2;
        final Color colorFont = uiEngineState.openMouseTextInput.fontColor;
        final float textInputAlpha = color1.a;
        final int CHARACTERS = 4;
        char[] chars = mouseTextInput.upperCase ? mouseTextInput.charactersUC : mouseTextInput.charactersLC;

        // 4 to the left
        for (int i = 1; i <= CHARACTERS; i++) {
            int index = mouseTextInput.selectedIndex - i;
            if (index >= 0 && index < chars.length) {
                render_mouseTextInputCharacter(chars[index], mouseTextInput.x - (i * 12), mouseTextInput.y - ((i * i) / 2), color1, colorFont, textInputAlpha, mouseTextInput.upperCase, false);
            }
        }
        // 4 to the right
        for (int i = 1; i <= CHARACTERS; i++) {
            int index = mouseTextInput.selectedIndex + i;
            if (index >= 0 && index < chars.length) {
                render_mouseTextInputCharacter(chars[index], mouseTextInput.x + (i * 12), mouseTextInput.y - ((i * i) / 2), color1, colorFont, textInputAlpha, mouseTextInput.upperCase, false);
            }
        }
        // 1 in center
        render_mouseTextInputCharacter(chars[mouseTextInput.selectedIndex], mouseTextInput.x, mouseTextInput.y, color1, colorFont, textInputAlpha, mouseTextInput.upperCase, uiEngineState.mTextInputMouse1Pressed);

        // Selection
        render_setColor(spriteRenderer, color2, textInputAlpha, false);
        spriteRenderer.drawCMediaImage(UIEngineBaseMedia_8x8.UI_MOUSETEXTINPUT_SELECTED, mouseTextInput.x - 1, mouseTextInput.y - 1);
        spriteRenderer.setAllReset();
    }

    private void render_mouseTextInputCharacter(char c, int x, int y, Color color1, Color colorFont, float textInputAlpha, boolean upperCase, boolean pressed) {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final int pressedIndex = pressed ? 1 : 0;

        render_setColor(spriteRenderer, color1, textInputAlpha, false);
        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_MOUSETEXTINPUT_BUTTON, pressedIndex, x, y);

        switch (c) {
            case '\n', '\t', '\b' -> {
                render_setColor(spriteRenderer, colorFont, colorFont.a * color1.a, false);
                CMediaArray specialCharacterSprite = switch (c) {
                    case '\n' -> UIEngineBaseMedia_8x8.UI_MOUSETEXTINPUT_CONFIRM;
                    case '\t' ->
                            upperCase ? UIEngineBaseMedia_8x8.UI_MOUSETEXTINPUT_UPPERCASE : UIEngineBaseMedia_8x8.UI_MOUSETEXTINPUT_LOWERCASE;
                    case '\b' -> UIEngineBaseMedia_8x8.UI_MOUSETEXTINPUT_DELETE;
                    default -> throw new IllegalStateException("Unexpected value: " + c);
                };
                spriteRenderer.drawCMediaArray(specialCharacterSprite, pressedIndex, x, y);
            }
            default -> {
                int offset = pressed ? 1 : 0;
                render_drawFont(String.valueOf(c), x + 2 + offset, y + 2 - offset, colorFont, textInputAlpha, false);
            }
        }

        spriteRenderer.setAllReset();
    }

    private void render_drawCursor() {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        if (uiEngineState.cursor != null) {
            int center_x = mediaManager.spriteWidth(uiEngineState.cursor) / 2;
            int center_y = mediaManager.spriteHeight(uiEngineState.cursor) / 2;
            spriteRenderer.drawCMediaSprite(uiEngineState.cursor, uiEngineState.cursorArrayIndex, uiCommonUtils.ui_getAnimationTimer(uiEngineState),
                    (uiEngineState.mouse_ui.x - center_x), (uiEngineState.mouse_ui.y - center_y));
        }
        spriteRenderer.setAllReset();
    }


    private boolean render_isComponentNotRendered(Component component) {
        if (!component.visible) return true;
        if (component.addedToWindow != null && !component.addedToWindow.visible) return true;
        return uiCommonUtils.component_isHiddenByTab(component);
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
            if (y == 0) return 15;
            if (y == height - 1) return 7;
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

    private int render_get3TilesCMediaIndex(int x, int width) {
        if (x == 0) return 0;
        if (x == width - 1) return 2;
        return 1;
    }


    private float componentAlpha(Component component) {
        return (component.addedToWindow != null ? (component.color.a * component.addedToWindow.color.a) : component.color.a);
    }

    private boolean componentGrayScale(Component component) {
        return component.disabled;
    }

    private void render_drawComponentTopLayer(Component component) {
        if (render_isComponentNotRendered(component)) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final float componentAlpha = componentAlpha(component);
        final boolean componentGrayScale = componentGrayScale(component);

        render_setColor(spriteRenderer, component.color, componentAlpha, componentGrayScale);

        switch (component) {
            case Combobox comboBox -> {
                // Menu
                if (uiCommonUtils.comboBox_isOpen(comboBox)) {
                    int widthPx = TS(comboBox.width);
                    for (int i = 0; i < comboBox.items.size; i++) {
                        int itemWidth = mediaManager.fontTextWidth(uiEngineState.config.ui_font, comboBox.items.get(i).text) + 2;
                        if (comboBox.items.get(i).comboBoxItemAction.icon() != null)
                            itemWidth += api.TS();
                        widthPx = Math.max(widthPx, itemWidth);
                    }
                    int width = MathUtils.ceil((widthPx) / uiEngineState.tileSize.TSF);
                    int height = comboBox.items.size;

                    /* Menu */
                    for (int iy = 0; iy < height; iy++) {
                        ComboboxItem comboBoxItem = comboBox.items.get(iy);
                        boolean selected = Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y, uiCommonUtils.component_getAbsoluteX(comboBox), uiCommonUtils.component_getAbsoluteY(comboBox) - TS() - TS(iy), widthPx, TS());

                        for (int ix = 0; ix < width; ix++) {
                            int index = render_get9TilesCMediaIndex(ix, iy, width, height);
                            CMediaArray comboBoxCellGraphic = selected ? UIEngineBaseMedia_8x8.UI_COMBOBOX_LIST_CELL_SELECTED : UIEngineBaseMedia_8x8.UI_COMBOBOX_LIST_CELL;

                            // Cell
                            spriteRenderer.saveState();
                            render_setColor(spriteRenderer, comboBoxItem.comboBoxItemAction.cellColor(), componentAlpha, componentGrayScale);
                            spriteRenderer.drawCMediaArray(comboBoxCellGraphic, index, uiCommonUtils.component_getAbsoluteX(comboBox) + TS(ix), uiCommonUtils.component_getAbsoluteY(comboBox) - TS(iy) - TS());
                            spriteRenderer.loadState();

                            // Cell - Underline
                            spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_COMBOBOX_LIST, index, uiCommonUtils.component_getAbsoluteX(comboBox) + TS(ix), uiCommonUtils.component_getAbsoluteY(comboBox) - TS(iy) - TS());

                            // Cell Content
                            render_drawFont(comboBoxItem.text, uiCommonUtils.component_getAbsoluteX(comboBox), uiCommonUtils.component_getAbsoluteY(comboBox) - TS(iy) - TS(), comboBoxItem.fontColor, componentAlpha, componentGrayScale, 2, 2, widthPx,
                                    comboBoxItem.comboBoxItemAction.icon(), comboBoxItem.comboBoxItemAction.iconIndex(), comboBoxItem.comboBoxItemAction.iconColor(),
                                    comboBoxItem.comboBoxItemAction.iconFlipX(), comboBoxItem.comboBoxItemAction.iconFlipY());

                        }
                    }
                    // Top
                    if (!comboBox.items.isEmpty()) {
                        for (int ix = 0; ix < width; ix++) {
                            int index = render_get3TilesCMediaIndex(ix, width);
                            spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_COMBOBOX_TOP, index, uiCommonUtils.component_getAbsoluteX(comboBox) + TS(ix), uiCommonUtils.component_getAbsoluteY(comboBox));
                        }
                    }

                }
            }
            default -> {
            }
        }
        spriteRenderer.setAllReset();
    }

    private void render_drawContextMenu() {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        if (uiEngineState.openContextMenu != null) {

            final ContextMenu contextMenu = uiEngineState.openContextMenu;
            final int width = uiEngineState.displayedContextMenuWidth;
            final int height = contextMenu.items.size;
            final float contextMenuAlpha = contextMenu.color.a;

            /* Menu */
            for (int iy = 0; iy < height; iy++) {
                ContextMenuItem contextMenuItem = contextMenu.items.get(iy);
                for (int ix = 0; ix < width; ix++) {
                    int index = render_get9TilesCMediaIndex(ix, iy, width, height);
                    boolean selected = Tools.Calc.pointRectsCollide(uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y, contextMenu.x, contextMenu.y - TS() - TS(iy), TS(uiEngineState.displayedContextMenuWidth), TS());
                    CMediaArray contextMenuCellGraphic = selected ? UIEngineBaseMedia_8x8.UI_CONTEXT_MENU_CELL_SELECTED : UIEngineBaseMedia_8x8.UI_CONTEXT_MENU_CELL;

                    // Cell
                    spriteRenderer.saveState();
                    render_setColor(spriteRenderer, contextMenuItem.contextMenuItemAction.cellColor(), contextMenuAlpha, false);
                    spriteRenderer.drawCMediaArray(contextMenuCellGraphic, index, contextMenu.x + TS(ix), contextMenu.y - TS(iy) - TS());
                    spriteRenderer.loadState();

                    // Cell Underline
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_CONTEXT_MENU, index, contextMenu.x + TS(ix), contextMenu.y - TS(iy) - TS());

                    // Cell Content
                    render_drawFont(contextMenuItem.text, contextMenu.x, contextMenu.y - TS(iy) - TS(), contextMenuItem.fontColor, contextMenuAlpha, false, 2, 2, TS(width),
                            contextMenuItem.contextMenuItemAction.icon(), contextMenuItem.contextMenuItemAction.iconIndex(), contextMenuItem.contextMenuItemAction.iconColor(),
                            contextMenuItem.contextMenuItemAction.iconFlipX(), contextMenuItem.contextMenuItemAction.iconFlipY());

                }
            }

            // Top
            if (!contextMenu.items.isEmpty()) {
                for (int ix = 0; ix < width; ix++) {
                    int index = render_get3TilesCMediaIndex(ix, width);
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_CONTEXT_MENU_TOP, index, contextMenu.x + TS(ix), contextMenu.y);
                }
            }


        }


        spriteRenderer.setAllReset();
    }

    private int tooltipWidth(Tooltip tooltip) {
        int width = tooltip.minWidth;
        for (int is = 0; is < tooltip.segments.size; is++) {
            TooltipSegment segment = tooltip.segments.get(is);
            width = Math.max(width, segment.width);
        }
        return width;
    }

    private int tooltipHeight(Tooltip tooltip) {
        int height = 0;
        for (int is = 0; is < tooltip.segments.size; is++) {
            TooltipSegment segment = tooltip.segments.get(is);
            if (!segment.merge)
                height += segment.height;
        }
        return height;
    }

    private void render_drawTooltip(int x, int y, Tooltip tooltip, float alpha) {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        // Determine Dimensions
        final int tooltip_width = tooltipWidth(tooltip);
        if (tooltip_width == 0) return;
        final int tooltip_height = tooltipHeight(tooltip);
        if (tooltip_height == 0) return;


        // Draw tooltip
        int iy = tooltip_height;
        for (int is = 0; is < tooltip.segments.size; is++) {
            TooltipSegment segment = tooltip.segments.get(is);
            final float segmentAlpha = segment.cellColor.a * alpha;
            final float borderAlpha = tooltip.color_border.a * alpha;
            final float contentAlpha = segment.contentColor.a * alpha;

            // Segment Background
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
                            final int isPlus1 = is + 1;
                            drawBottomborder = isPlus1 < tooltip.segments.size && tooltip.segments.get(is).border;
                        }
                    }

                    // Background
                    if (!segment.clear) {
                        render_setColor(spriteRenderer, segment.cellColor, segmentAlpha, false);
                        for (int tx = 0; tx < tooltip_width; tx++) {
                            spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TOOLTIP_CELL, render_get16TilesCMediaIndex(tx, y_combined, width_reference, height_reference), x + TS(tx), y + TS(y_combined));
                        }
                    }

                    // Border
                    render_setColor(spriteRenderer, tooltip.color_border, borderAlpha, false);
                    for (int tx = 0; tx < tooltip_width; tx++) {
                        // tooltip border
                        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TOOLTIP, render_get16TilesCMediaIndex(tx, y_combined, width_reference, tooltip_height), x + TS(tx), y + TS(y_combined));
                        // segmentborder
                        if (drawBottomborder) {
                            spriteRenderer.drawCMediaImage(UIEngineBaseMedia_8x8.UI_TOOLTIP_SEGMENT_BORDER, x + TS(tx), y + TS(y_combined));
                        }
                    }


                }

                // Top Border
                render_setColor(spriteRenderer, tooltip.color_border, borderAlpha, false);
                for (int tx = 0; tx < tooltip_width; tx++) {
                    // tooltip border
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TOOLTIP_TOP, render_get3TilesCMediaIndex(tx, width_reference), x + TS(tx), y + TS(tooltip_height));
                }

            }


            // Content
            spriteRenderer.setColorReset();

            switch (segment) {
                case TooltipTextSegment textSegment -> {
                    // Text
                    int text_width = render_textWidth(textSegment.text);
                    int text_y = y + TS(iy);
                    int text_x = x + switch (textSegment.alignment) {
                        case LEFT -> 1;
                        case CENTER -> MathUtils.round(TS(tooltip_width) / 2f) - MathUtils.round(text_width / 2f);
                        case RIGHT -> TS(tooltip_width) - text_width - 3;
                    };

                    render_drawFont(textSegment.text, text_x, text_y, textSegment.contentColor, contentAlpha, false, 1, 2);
                }
                case TooltipImageSegment imageSegment -> {
                    int image_width = mediaManager.spriteWidth(imageSegment.image);
                    int image_height = mediaManager.spriteHeight(imageSegment.image);
                    int image_y = y + TS(iy) + MathUtils.round((TS(segment.height) - image_height) / 2f);
                    int image_x = x + switch (imageSegment.alignment) {
                        case LEFT -> 2;
                        case CENTER -> MathUtils.round(TS(tooltip_width) / 2f) - MathUtils.round(image_width / 2f);
                        case RIGHT -> TS(tooltip_width) - image_width - 2;
                    };
                    render_setColor(spriteRenderer, imageSegment.contentColor, contentAlpha, false);
                    int width = mediaManager.spriteWidth(imageSegment.image);
                    int height = mediaManager.spriteHeight(imageSegment.image);
                    spriteRenderer.drawCMediaSprite(imageSegment.image, imageSegment.arrayIndex, uiCommonUtils.ui_getAnimationTimer(uiEngineState), image_x, image_y,
                            width, height, 0, 0, width, height, imageSegment.flipX, imageSegment.flipY
                    );
                }
                case TooltipFramebufferViewportSegment framebufferViewportSegment -> {
                    if (framebufferViewportSegment.frameBuffer != null) {
                        int width = TS(framebufferViewportSegment.width);
                        int height = TS(framebufferViewportSegment.height);
                        int segment_x = x + switch (framebufferViewportSegment.alignment) {
                            case LEFT -> 0;
                            case CENTER ->
                                    MathUtils.round(TS(tooltip_width) / 2f) - MathUtils.round(TS(framebufferViewportSegment.width) / 2f);
                            case RIGHT -> TS(tooltip_width) - TS(framebufferViewportSegment.width);
                        };
                        int segment_y = y + TS(iy);

                        spriteRenderer.setColor(framebufferViewportSegment.contentColor);
                        spriteRenderer.draw(framebufferViewportSegment.frameBuffer.getFlippedTextureRegion(),
                                segment_x, segment_y,
                                width, height
                        );
                        spriteRenderer.setColorReset();
                    }

                }
                case null, default -> {
                }
            }
        }

        spriteRenderer.setAllReset();
    }

    private void render_drawCursorTooltip() {
        final Tooltip tooltip = uiEngineState.fadeOutTooltip != null ? uiEngineState.fadeOutTooltip : uiEngineState.tooltip;
        if (tooltip == null) return;
        if (tooltip.segments.isEmpty()) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final float lineAlpha = tooltip.color_line.a * uiEngineState.tooltip_fadePct;
        final int tooltip_width = tooltipWidth(tooltip);
        if (tooltip_width == 0) return;
        final int tooltip_height = tooltipHeight(tooltip);
        if (tooltip_height == 0) return;


        final int lineLengthAbs = TS(tooltip.lineLength);
        final DIRECTION direction = switch (tooltip.direction) {
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
                    Math.clamp(uiEngineState.mouse_ui.x + lineLengthAbs, 0, Math.max(uiEngineState.resolutionWidth - TS(tooltip_width), 0));
            case LEFT ->
                    Math.clamp(uiEngineState.mouse_ui.x - TS(tooltip_width + tooltip.lineLength), 0, Math.max(uiEngineState.resolutionWidth - TS(tooltip_width), 0));
            case UP, DOWN ->
                    Math.clamp(uiEngineState.mouse_ui.x - (TS(tooltip_width) / 2), 0, Math.max(uiEngineState.resolutionWidth - TS(tooltip_width), 0));
        };

        int tooltip_y = switch (direction) {
            case RIGHT, LEFT ->
                    Math.clamp(uiEngineState.mouse_ui.y - (TS(tooltip_height) / 2), 0, Math.max(uiEngineState.resolutionHeight - TS(tooltip_height) - 1, 0));
            case UP ->
                    Math.clamp(uiEngineState.mouse_ui.y + TS(tooltip.lineLength), 0, Math.max(uiEngineState.resolutionHeight - TS(tooltip_height) - 1, 0));
            case DOWN ->
                    Math.clamp(uiEngineState.mouse_ui.y - TS(tooltip_height + tooltip.lineLength), 0, Math.max(uiEngineState.resolutionHeight - TS(tooltip_height) - 1, 0));
        };

        // Draw Tooltip
        render_drawTooltip(tooltip_x, tooltip_y, tooltip, uiEngineState.tooltip_fadePct);


        // Draw line
        render_setColor(spriteRenderer, tooltip.color_line, lineAlpha, false);
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
                //
            }
            case UP, DOWN -> {
                int yOffset = direction == DIRECTION.UP ? 0 : -TS2();
                spriteRenderer.drawCMediaImage(UIEngineBaseMedia_8x8.UI_TOOLTIP_LINE_VERTICAL, uiEngineState.mouse_ui.x, uiEngineState.mouse_ui.y + yOffset);
            }
        }
    }

    private void render_drawTooltipNotifications() {
        if (uiEngineState.tooltipNotifications.isEmpty()) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        for (int i = 0; i < uiEngineState.tooltipNotifications.size; i++) {
            TooltipNotification tooltipNotification = uiEngineState.tooltipNotifications.get(i);

            switch (tooltipNotification.state) {
                case INIT -> {
                }
                case DISPLAY, FADE -> {

                }
            }

            float alpha = 1f;
            if (tooltipNotification.state == TOOLTIP_NOTIFICATION_STATE.FADE) {
                alpha = (1f - (tooltipNotification.timer / (float) api.config.notification.tooltip.getFadeoutTime()));
            }

            if (tooltipNotification.tooltip != null) {
                render_drawTooltip(tooltipNotification.x, tooltipNotification.y, tooltipNotification.tooltip, alpha);
            }
        }


        spriteRenderer.setAllReset();
    }

    private void render_drawTopNotifications() {
        if (uiEngineState.notifications.isEmpty()) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        final int width = (uiEngineState.resolutionWidth % TS() == 0) ? (uiEngineState.resolutionWidth / TS()) : ((uiEngineState.resolutionWidth / TS()) + 1);
        int y = 0;
        int yOffsetSlideFade = 0;
        for (int i = 0; i < uiEngineState.notifications.size; i++) {
            Notification notification = uiEngineState.notifications.get(i);
            final float notificationAlpha = notification.color.a;

            if (notification.state == TOP_NOTIFICATION_STATE.FOLD) {
                float fadeoutProgress = (notification.timer / (float) uiEngineState.config.notification_foldTime);
                yOffsetSlideFade = yOffsetSlideFade + MathUtils.round(TS() * fadeoutProgress);
            }
            spriteRenderer.saveState();
            render_setColor(spriteRenderer, notification.color, notificationAlpha, false);
            for (int ix = 0; ix < width; ix++) {
                spriteRenderer.drawCMediaImage(UIEngineBaseMedia_8x8.UI_NOTIFICATION_BAR, TS(ix), uiEngineState.resolutionHeight - TS() - TS(y) + yOffsetSlideFade);
            }
            spriteRenderer.loadState();
            int xOffset = (TS(width) / 2) - (render_textWidth(notification.text) / 2) - notification.scroll;
            render_drawFont(notification.text, xOffset, (uiEngineState.resolutionHeight - TS() - TS(y)) + 1 + yOffsetSlideFade, notification.fontColor, notificationAlpha, false);
            y = y + 1;
        }

        spriteRenderer.setAllReset();


    }

    private void render_drawWindow(Window window, boolean modals) {
        if (!window.visible) return;
        if (modals) {
            if (uiEngineState.modalWindow != window)
                return;
        } else {
            if (uiEngineState.modalWindow == window)
                return;
        }

        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final float windowAlpha = window.color.a;

        render_setColor(spriteRenderer, window.color, windowAlpha, false);

        for (int ix = 0; ix < window.width; ix++) {
            if (!window.folded) {
                for (int iy = 0; iy < window.height; iy++) {
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_WINDOW, render_getWindowCMediaIndex(ix, iy, window.width, window.height, window.hasTitleBar), window.x + TS(ix), window.y + TS(iy));
                }
            } else {
                spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_WINDOW, render_getWindowCMediaIndex(ix, (window.height - 1), window.width, window.height, window.hasTitleBar), window.x + TS(ix), window.y + (TS(window.height - 1)));
            }
        }


        if (window.hasTitleBar) {
            render_drawFont(window.title, window.x, window.y + TS(window.height) - TS(),
                    window.fontColor, windowAlpha, false, 1, 1, TS(window.width - 1),
                    window.windowAction.icon(), window.windowAction.iconIndex(), window.windowAction.iconColor(),
                    window.windowAction.iconFlipX(), window.windowAction.iconFlipY());
        }


        // Draw Components
        for (int i = 0; i < window.components.size; i++) {
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
        for (int i = 0; i < window.components.size; i++) {
            Component component = window.components.get(i);
            if (!window.folded) render_drawComponentTopLayer(component);
        }


        spriteRenderer.setAllReset();

    }

    private void render_setColor(PrimitiveRenderer primitiveRenderer, Color color, float alpha, boolean grayScale) {
        float saturation, lightness;
        if (grayScale) {
            saturation = 0f;
            lightness = 0.45f;
        } else {
            saturation = 0.5f;
            lightness = 0.5f;
        }

        primitiveRenderer.setColor(color, alpha);
        primitiveRenderer.setTweak(0.5f, saturation, lightness, 0f);
    }

    private void render_setColor(SpriteRenderer spriteRenderer, Color color, float alpha, boolean grayScale) {
        float saturation, lightness;
        if (grayScale) {
            saturation = 0f;
            lightness = 0.45f;
        } else {
            saturation = 0.5f;
            lightness = 0.5f;
        }

        spriteRenderer.setColor(color, alpha);
        spriteRenderer.setTweak(0.5f, saturation, lightness, 0.0f);
    }


    private void render_drawComponent(Component component) {
        if (render_isComponentNotRendered(component)) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final PrimitiveRenderer primitiveRenderer = uiEngineState.primitiveRenderer_ui;
        final float componentAlpha = componentAlpha(component);
        final boolean componentGrayScale = componentGrayScale(component);

        render_setColor(spriteRenderer, component.color, componentAlpha, componentGrayScale);


        switch (component) {
            case Button button -> {
                CMediaArray buttonGraphic = (button.pressed ? UIEngineBaseMedia_8x8.UI_BUTTON_PRESSED : UIEngineBaseMedia_8x8.UI_BUTTON);
                int pressed_offset = button.pressed ? 1 : 0;
                for (int ix = 0; ix < button.width; ix++) {
                    for (int iy = 0; iy < button.height; iy++) {
                        spriteRenderer.drawCMediaArray(buttonGraphic, render_get16TilesCMediaIndex(ix, iy, button.width, button.height), uiCommonUtils.component_getAbsoluteX(button) + TS(ix), uiCommonUtils.component_getAbsoluteY(button) + TS(iy));
                    }
                }
                if (button instanceof TextButton textButton) {
                    if (textButton.text != null) {
                        render_drawFont(textButton.text, uiCommonUtils.component_getAbsoluteX(textButton) + textButton.contentOffset_x + pressed_offset, uiCommonUtils.component_getAbsoluteY(button) + textButton.contentOffset_y - pressed_offset,
                                textButton.fontColor, componentAlpha, componentGrayScale, 1, 2, -1,
                                textButton.buttonAction.icon(), textButton.buttonAction.iconIndex(), textButton.buttonAction.iconColor(),
                                textButton.buttonAction.iconFlipX(), textButton.buttonAction.iconFlipY());
                    }
                } else if (button instanceof ImageButton imageButton) {
                    spriteRenderer.saveState();
                    render_setColor(spriteRenderer, imageButton.color2, componentAlpha, componentGrayScale);
                    if (imageButton.image != null)
                        spriteRenderer.drawCMediaSprite(imageButton.image, imageButton.arrayIndex, uiCommonUtils.ui_getAnimationTimer(uiEngineState), uiCommonUtils.component_getAbsoluteX(imageButton) + imageButton.contentOffset_x + pressed_offset, uiCommonUtils.component_getAbsoluteY(imageButton) + imageButton.contentOffset_y - pressed_offset);
                    spriteRenderer.loadState();
                }


            }
            case Image image -> {
                if (image.image != null) {
                    final int srcWidth, srcHeight, width, height;
                    srcWidth = mediaManager.spriteWidth(image.image);
                    srcHeight = mediaManager.spriteHeight(image.image);
                    width = image.stretchToSize ? api.TS(image.width) : srcWidth;
                    height = image.stretchToSize ? api.TS(image.height) : srcHeight;
                    spriteRenderer.drawCMediaSprite(image.image, image.arrayIndex, uiCommonUtils.ui_getAnimationTimer(uiEngineState),
                            uiCommonUtils.component_getAbsoluteX(image), uiCommonUtils.component_getAbsoluteY(image),
                            width, height, 0, 0, srcWidth, srcHeight,
                            image.flipX, image.flipY
                    );
                }
            }
            case FrameBufferViewport frameBufferViewport -> {
                if (frameBufferViewport.frameBuffer != null) {
                    final int srcWidth, srcHeight, width, height;
                    final Texture texture = frameBufferViewport.frameBuffer.getColorBufferTexture();
                    srcWidth = texture.getWidth();
                    srcHeight = texture.getHeight();
                    width = frameBufferViewport.stretchToSize ? api.TS(frameBufferViewport.width) : srcWidth;
                    height = frameBufferViewport.stretchToSize ? api.TS(frameBufferViewport.height) : srcHeight;

                    spriteRenderer.draw(texture, uiCommonUtils.component_getAbsoluteX(frameBufferViewport), uiCommonUtils.component_getAbsoluteY(frameBufferViewport),
                            width, height, 0, 0, width, height, frameBufferViewport.flipX, !frameBufferViewport.flipY
                    );
                }
            }
            case Text text -> {
                if (text.text != null && !text.text.isEmpty()) {
                    render_drawFont(text.text, uiCommonUtils.component_getAbsoluteX(text), uiCommonUtils.component_getAbsoluteY(text),
                            text.fontColor, componentAlpha, componentGrayScale,
                            1, 1, TS(text.width),
                            text.textAction.icon(), text.textAction.iconIndex(), text.textAction.iconColor(),
                            text.textAction.iconFlipX(), text.textAction.iconFlipY());
                }
            }
            case ScrollbarVertical scrollBarVertical -> {
                for (int i = 0; i < scrollBarVertical.height; i++) {
                    int index = (i == 0 ? 2 : (i == (scrollBarVertical.height - 1) ? 0 : 1));
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_SCROLLBAR_VERTICAL, index, uiCommonUtils.component_getAbsoluteX(scrollBarVertical), uiCommonUtils.component_getAbsoluteY(scrollBarVertical) + TS(i));
                }
                int buttonYOffset = MathUtils.round(scrollBarVertical.scrolled * TS(scrollBarVertical.height - 1));
                spriteRenderer.saveState();
                render_setColor(spriteRenderer, scrollBarVertical.color2, componentAlpha, componentGrayScale);
                spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_SCROLLBAR_BUTTON_VERTICAL, (scrollBarVertical.buttonPressed ? 1 : 0), uiCommonUtils.component_getAbsoluteX(scrollBarVertical), uiCommonUtils.component_getAbsoluteY(scrollBarVertical) + buttonYOffset);
                spriteRenderer.loadState();
            }
            case ScrollbarHorizontal scrollBarHorizontal -> {
                for (int i = 0; i < scrollBarHorizontal.width; i++) {
                    int index = (i == 0 ? 0 : (i == (scrollBarHorizontal.width - 1) ? 2 : 1));
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_SCROLLBAR_HORIZONTAL, index, uiCommonUtils.component_getAbsoluteX(scrollBarHorizontal) + TS(i), uiCommonUtils.component_getAbsoluteY(scrollBarHorizontal));
                }
                int buttonXOffset = MathUtils.round(scrollBarHorizontal.scrolled * TS(scrollBarHorizontal.width - 1));
                spriteRenderer.saveState();
                render_setColor(spriteRenderer, scrollBarHorizontal.color2, componentAlpha, componentGrayScale);
                spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_SCROLLBAR_BUTTON_HORIZONAL, (scrollBarHorizontal.buttonPressed ? 1 : 0), uiCommonUtils.component_getAbsoluteX(scrollBarHorizontal) + buttonXOffset, uiCommonUtils.component_getAbsoluteY(scrollBarHorizontal));
                spriteRenderer.loadState();
            }
            case List list -> {

                boolean itemsValid = (list.items != null && list.items.size > 0);
                int itemFrom = 0;
                if (itemsValid) {
                    itemFrom = MathUtils.round(list.scrolled * ((list.items.size) - (list.height)));
                    itemFrom = Math.max(itemFrom, 0);
                }
                boolean dragEnabled = false;
                boolean dragValid = false;
                int drag_x = -1, drag_y = -1;

                if ((uiEngineState.draggedList != null || uiEngineState.draggedGrid != null) && list == uiEngineState.lastUIMouseHover) {
                    dragEnabled = true;
                    dragValid = uiCommonUtils.list_canDragIntoList(list);
                    if (dragValid) {
                        drag_x = uiCommonUtils.component_getAbsoluteX(list);
                        int y_list = uiCommonUtils.component_getAbsoluteY(list);
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
                    if (list.items != null && list.items.size > 0) {
                        if (itemIndex < list.items.size) {
                            item = list.items.get(itemIndex);
                        }
                    }

                    boolean selected = item != null && (list.multiSelect ? list.selectedItems.contains(item) : (list.selectedItem == item));

                    // Cell
                    spriteRenderer.saveState();
                    Color cellColor = item != null ? list.listAction.cellColor(item) : list.color2;
                    render_setColor(spriteRenderer, cellColor, componentAlpha, listGrayScale);
                    for (int ix = 0; ix < list.width; ix++) {
                        CMediaImage listSelectedGraphic = selected ? UIEngineBaseMedia_8x8.UI_LIST_CELL_SELECTED : UIEngineBaseMedia_8x8.UI_LIST_CELL;
                        spriteRenderer.drawCMediaImage(listSelectedGraphic, uiCommonUtils.component_getAbsoluteX(list) + TS(ix), uiCommonUtils.component_getAbsoluteY(list) + TS(itemOffsetY));
                    }
                    spriteRenderer.loadState();

                    // Cell UnderLine
                    for (int ix = 0; ix < list.width; ix++) {
                        spriteRenderer.drawCMediaImage(UIEngineBaseMedia_8x8.UI_LIST, uiCommonUtils.component_getAbsoluteX(list) + TS(ix), uiCommonUtils.component_getAbsoluteY(list) + TS(itemOffsetY));
                    }

                    // Cell Content
                    if (item != null) {
                        String text = list.listAction.text(item);
                        render_drawFont(text, uiCommonUtils.component_getAbsoluteX(list), uiCommonUtils.component_getAbsoluteY(list) + TS(itemOffsetY),
                                list.fontColor, componentAlpha, componentGrayScale, 1, 2, TS(list.width),
                                list.listAction.icon(item), list.listAction.iconIndex(item), list.listAction.iconColor(item),
                                list.listAction.iconFlipX(), list.listAction.iconFlipY());
                    }
                }

                if (dragEnabled && dragValid) {
                    for (int ix = 0; ix < list.width; ix++) {
                        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_LIST_DRAG, render_getListDragCMediaIndex(ix, list.width), drag_x + TS(ix), drag_y);
                    }
                }


            }
            case Combobox comboBox -> {

                // Cell
                spriteRenderer.saveState();
                Color cellColor = comboBox.selectedItem != null ? comboBox.selectedItem.comboBoxItemAction.cellColor() : comboBox.color2;
                render_setColor(spriteRenderer, cellColor, componentAlpha, componentGrayScale);
                for (int ix = 0; ix < comboBox.width; ix++) {
                    int index = ix == 0 ? 0 : (ix == comboBox.width - 1 ? 2 : 1);
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_COMBOBOX_CELL, index, uiCommonUtils.component_getAbsoluteX(comboBox) + TS(ix), uiCommonUtils.component_getAbsoluteY(comboBox));
                }

                // ComboBox
                for (int ix = 0; ix < comboBox.width; ix++) {
                    int index = ix == 0 ? 0 : (ix == comboBox.width - 1 ? 2 : 1);
                    CMediaArray comboMedia = uiCommonUtils.comboBox_isOpen(comboBox) ? UIEngineBaseMedia_8x8.UI_COMBOBOX_OPEN : UIEngineBaseMedia_8x8.UI_COMBOBOX;
                    spriteRenderer.drawCMediaArray(comboMedia, index, uiCommonUtils.component_getAbsoluteX(comboBox) + TS(ix), uiCommonUtils.component_getAbsoluteY(comboBox));
                }


                spriteRenderer.loadState();
                // Cell Content
                if (comboBox.selectedItem != null) {
                    render_drawFont(comboBox.selectedItem.text, uiCommonUtils.component_getAbsoluteX(comboBox), uiCommonUtils.component_getAbsoluteY(comboBox),
                            comboBox.selectedItem.fontColor, componentAlpha, componentGrayScale, 1, 2, TS(comboBox.width - 1),
                            comboBox.selectedItem.comboBoxItemAction.icon(), comboBox.selectedItem.comboBoxItemAction.iconIndex(), comboBox.selectedItem.comboBoxItemAction.iconColor(),
                            comboBox.selectedItem.comboBoxItemAction.iconFlipX(), comboBox.selectedItem.comboBoxItemAction.iconFlipY());
                }
            }
            case Knob knob -> {
                spriteRenderer.drawCMediaImage(UIEngineBaseMedia_8x8.UI_KNOB_BACKGROUND, uiCommonUtils.component_getAbsoluteX(knob), uiCommonUtils.component_getAbsoluteY(knob));
                render_setColor(spriteRenderer, knob.color2, componentAlpha, componentGrayScale);
                if (knob.endless) {
                    int index = MathUtils.round(knob.turned * 31);
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_KNOB_ENDLESS, index, uiCommonUtils.component_getAbsoluteX(knob), uiCommonUtils.component_getAbsoluteY(knob));
                } else {
                    int index = MathUtils.round(knob.turned * 25);
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_KNOB, index, uiCommonUtils.component_getAbsoluteX(knob), uiCommonUtils.component_getAbsoluteY(knob));
                }
            }
            case Textfield textField -> {

                for (int ix = 0; ix < textField.width; ix++) {
                    int index = ix == (textField.width - 1) ? 2 : (ix == 0) ? 0 : 1;
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TEXTFIELD, index, uiCommonUtils.component_getAbsoluteX(textField) + TS(ix), uiCommonUtils.component_getAbsoluteY(textField));
                }

                spriteRenderer.saveState();
                render_setColor(spriteRenderer, textField.color2, componentAlpha, componentGrayScale);
                for (int ix = 0; ix < textField.width; ix++) {
                    int index = ix == (textField.width - 1) ? 2 : (ix == 0) ? 0 : 1;
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TEXTFIELD_CELL, index, uiCommonUtils.component_getAbsoluteX(textField) + TS(ix), uiCommonUtils.component_getAbsoluteY(textField));
                }

                if (!textField.contentValid) {
                    render_setColor(spriteRenderer, Color.GRAY, componentAlpha, false);
                    for (int ix = 0; ix < textField.width; ix++) {
                        int index = ix == (textField.width - 1) ? 2 : (ix == 0) ? 0 : 1;
                        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TEXTFIELD_CELL_VALIDATION, index, uiCommonUtils.component_getAbsoluteX(textField) + TS(ix), uiCommonUtils.component_getAbsoluteY(textField));
                    }
                }
                spriteRenderer.loadState();

                if (textField.content != null) {
                    //String contentString = textField.content.substring(textField.offset);


                    // Marker
                    int begin = Math.max(textField.markedContentBegin - textField.offset, 0);
                    int end = Math.max(textField.markedContentEnd - textField.offset, 0);
                    if ((end - begin) > 0) {
                        int drawFrom = render_textWidth(textField.content, 0, begin);
                        int drawTo = Math.min(drawFrom + render_textWidth(textField.content, begin, end), TS(textField.width));
                        int drawWidth = drawTo - drawFrom;
                        if (drawWidth > 0) {
                            spriteRenderer.saveState();
                            render_setColor(spriteRenderer, textField.markerColor, componentAlpha, false);
                            int drawXFrom = uiCommonUtils.component_getAbsoluteX(textField) + drawFrom + 1;
                            drawWidth++;
                            spriteRenderer.drawCMediaImage(UIEngineBaseMedia_8x8.UI_PIXEL,
                                    drawXFrom,
                                    uiCommonUtils.component_getAbsoluteY(textField),
                                    drawWidth, 8
                            );
                            spriteRenderer.loadState();
                        }
                    }

                    // Text
                    render_drawFont(textField.content, uiCommonUtils.component_getAbsoluteX(textField), uiCommonUtils.component_getAbsoluteY(textField),
                            textField.fontColor, componentAlpha, componentGrayScale, 1, 2, TS(textField.width),null, 0, null,false,false, textField.offset, textField.content.length());

                    // Caret
                    if (uiCommonUtils.textField_isFocused(textField)) {
                        int xOffset = render_textWidth(textField.content, textField.offset, textField.caretPosition) + 1;
                        if (xOffset < TS(textField.width)) {
                            spriteRenderer.drawCMediaAnimation(UIEngineBaseMedia_8x8.UI_TEXTFIELD_CARET, uiCommonUtils.ui_getAnimationTimer(uiEngineState), uiCommonUtils.component_getAbsoluteX(textField) + xOffset, uiCommonUtils.component_getAbsoluteY(textField));
                        }
                    }


                }
            }
            case Grid grid -> {
                int tileSize = grid.bigMode ? TS2() : TS();
                int gridWidth = grid.items.length;
                int gridHeight = grid.items[0].length;

                boolean dragEnabled = false;
                boolean dragValid = false;
                int drag_x = -1, drag_y = -1;
                if ((uiEngineState.draggedList != null || uiEngineState.draggedGrid != null) && grid == uiEngineState.lastUIMouseHover) {
                    dragEnabled = true;
                    dragValid = uiCommonUtils.grid_canDragIntoGrid(grid);
                    if (dragValid) {
                        int x_grid = uiCommonUtils.component_getAbsoluteX(grid);
                        int y_grid = uiCommonUtils.component_getAbsoluteY(grid);
                        int m_x = uiEngineState.mouse_ui.x - x_grid;
                        int m_y = uiEngineState.mouse_ui.y - y_grid;
                        if (m_x > 0 && m_x < (grid.width * tileSize) && m_y > 0 && m_y < (grid.height * tileSize)) {
                            int inv_x = m_x / tileSize;
                            int inv_y = m_y / tileSize;
                            if (uiCommonUtils.grid_positionValid(grid, inv_x, inv_y)) {
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
                        Object item = grid.items[ix][iy];

                        CMediaArray cellGraphic;
                        CMediaArray gridGraphic = grid.bigMode ? UIEngineBaseMedia_8x8.UI_GRID_X2 : UIEngineBaseMedia_8x8.UI_GRID;
                        boolean selected = grid.multiSelect ? grid.selectedItems.contains(item) : item != null && item == grid.selectedItem;
                        if (dragEnabled && dragValid && drag_x == ix && drag_y == iy) {
                            cellGraphic = grid.bigMode ? UIEngineBaseMedia_8x8.UI_GRID_DRAGGED_X2 : UIEngineBaseMedia_8x8.UI_GRID_DRAGGED;
                        } else {
                            if (selected) {
                                cellGraphic = grid.bigMode ? UIEngineBaseMedia_8x8.UI_GRID_CELL_SELECTED_X2 : UIEngineBaseMedia_8x8.UI_GRID_CELL_SELECTED;
                            } else {
                                cellGraphic = grid.bigMode ? UIEngineBaseMedia_8x8.UI_GRID_CELL_X2 : UIEngineBaseMedia_8x8.UI_GRID_CELL;
                            }
                        }

                        // Cell
                        Color cellColor = item != null ? grid.gridAction.cellColor(item) : grid.color2;
                        spriteRenderer.saveState();
                        render_setColor(spriteRenderer, cellColor, componentAlpha, gridGrayScale);
                        int index = grid.bigMode ? render_get16TilesCMediaIndex(ix, iy, grid.width / 2, grid.height / 2) : render_get16TilesCMediaIndex(ix, iy, grid.width, grid.height);
                        spriteRenderer.drawCMediaArray(cellGraphic, index, uiCommonUtils.component_getAbsoluteX(grid) + (ix * tileSize), uiCommonUtils.component_getAbsoluteY(grid) + (iy * tileSize));
                        spriteRenderer.loadState();

                        // Draw Grid
                        spriteRenderer.drawCMediaArray(gridGraphic, index, uiCommonUtils.component_getAbsoluteX(grid) + (ix * tileSize), uiCommonUtils.component_getAbsoluteY(grid) + (iy * tileSize));

                        // Icon
                        if (item != null) {
                            render_drawIcon(grid.gridAction.icon(item), uiCommonUtils.component_getAbsoluteX(grid) + (ix * tileSize), uiCommonUtils.component_getAbsoluteY(grid) + (iy * tileSize),
                                    grid.gridAction.iconColor(item), componentAlpha, componentGrayScale, grid.gridAction.iconIndex(item), grid.bigMode,
                                    grid.gridAction.iconFlipX(), grid.gridAction.iconFlipY());
                        }
                    }
                }

            }
            case Tabbar tabBar -> {
                int tabXOffset = tabBar.tabOffset;
                int topBorder;
                for (int i = 0; i < tabBar.tabs.size; i++) {
                    Tab tab = tabBar.tabs.get(i);
                    int tabWidth = tabBar.bigIconMode ? 2 : tab.width;
                    if ((tabXOffset + tabWidth) > tabBar.width) break;

                    boolean selected = i == tabBar.selectedTab;

                    if (tabBar.bigIconMode) {
                        CMediaImage tabGraphic = selected ? UIEngineBaseMedia_8x8.UI_TAB_BIGICON_SELECTED : UIEngineBaseMedia_8x8.UI_TAB_BIGICON;
                        spriteRenderer.drawCMediaImage(tabGraphic, uiCommonUtils.component_getAbsoluteX(tabBar) + TS(tabXOffset), uiCommonUtils.component_getAbsoluteY(tabBar));
                        int selected_offset = selected ? 0 : 1;
                        render_drawIcon(tab.tabAction.icon(), uiCommonUtils.component_getAbsoluteX(tabBar) + TS(tabXOffset) + selected_offset, uiCommonUtils.component_getAbsoluteY(tabBar) - selected_offset,
                                tab.tabAction.iconColor(), componentAlpha, componentGrayScale,
                                tab.tabAction.iconIndex(), true,
                                tab.tabAction.iconFlipX(), tab.tabAction.iconFlipY());
                    } else {
                        CMediaArray tabGraphic = selected ? UIEngineBaseMedia_8x8.UI_TAB_SELECTED : UIEngineBaseMedia_8x8.UI_TAB;
                        for (int ix = 0; ix < tabWidth; ix++) {
                            spriteRenderer.drawCMediaArray(tabGraphic, render_getTabCMediaIndex(ix, tab.width), uiCommonUtils.component_getAbsoluteX(tabBar) + TS(ix) + TS(tabXOffset), uiCommonUtils.component_getAbsoluteY(tabBar));
                        }
                    }

                    if (!tabBar.bigIconMode) {
                        render_drawFont(tab.title, uiCommonUtils.component_getAbsoluteX(tabBar) + TS(tabXOffset), uiCommonUtils.component_getAbsoluteY(tabBar),
                                tab.fontColor, componentAlpha, componentGrayScale, 2, 1, TS(tabWidth),
                                tab.tabAction.icon(), tab.tabAction.iconIndex(), tab.tabAction.iconColor(),
                                tab.tabAction.iconFlipX(), tab.tabAction.iconFlipY());
                    }
                    tabXOffset += tabWidth;
                }

                topBorder = tabBar.width - tabXOffset;

                // Top Border Top
                for (int ix = 0; ix < topBorder; ix++) {
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TAB_BORDERS, 2, uiCommonUtils.component_getAbsoluteX(tabBar) + TS(tabXOffset + ix), uiCommonUtils.component_getAbsoluteY(tabBar));
                }

                if (tabBar.border) {
                    // Bottom
                    for (int ix = 0; ix < tabBar.width; ix++) {
                        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TAB_BORDERS, 2, uiCommonUtils.component_getAbsoluteX(tabBar) + TS(ix), uiCommonUtils.component_getAbsoluteY(tabBar) - TS(tabBar.borderHeight));
                    }
                    // Left/Right
                    for (int iy = 0; iy < tabBar.borderHeight; iy++) {
                        int yOffset = TS(iy + 1);
                        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TAB_BORDERS, 0, uiCommonUtils.component_getAbsoluteX(tabBar), uiCommonUtils.component_getAbsoluteY(tabBar) - yOffset);
                        spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_TAB_BORDERS, 1, uiCommonUtils.component_getAbsoluteX(tabBar) + TS(tabBar.width - 1), uiCommonUtils.component_getAbsoluteY(tabBar) - yOffset);
                    }
                }
            }
            case Shape shape -> {
                if (shape.shapeType != null) {
                    spriteRenderer.end();
                    primitiveRenderer.begin(GL32.GL_TRIANGLES);
                    render_setColor(primitiveRenderer, shape.color, componentAlpha, componentGrayScale);
                    primitiveRenderer.setVertexColor(shape.color2);
                    final int cx = uiCommonUtils.component_getAbsoluteX(shape);
                    final int cy = uiCommonUtils.component_getAbsoluteY(shape);
                    final int cw = TS(shape.width);
                    final int ch = TS(shape.height);
                    final int cw2 = cw / 2;
                    final int ch2 = ch / 2;
                    final int center_x = cx + cw2;
                    final int center_y = cy + ch2;

                    switch (shape.shapeType) {
                        case RECT -> {
                            primitiveRenderer.vertex(cx, cy);
                            primitiveRenderer.vertex(cx + cw, cy);
                            primitiveRenderer.vertex(cx + cw, cy + ch);

                            primitiveRenderer.vertex(cx, cy);
                            primitiveRenderer.vertex(cx, cy + ch);
                            primitiveRenderer.vertex(cx + cw, cy + ch);
                        }
                        case OVAL -> {
                            final float RES = MathUtils.PI2 / 45f;
                            for (float ir = 0; ir <= MathUtils.PI2; ir += RES) {
                                primitiveRenderer.vertex(center_x, center_y);
                                primitiveRenderer.vertex(center_x + (MathUtils.cos(ir) * cw2), center_y + MathUtils.sin(ir) * ch2);
                                primitiveRenderer.vertex(center_x + (MathUtils.cos(ir + RES) * cw2), center_y + MathUtils.sin(ir + RES) * ch2);
                            }
                        }
                        case RIGHT_TRIANGLE -> {
                            switch (shape.shapeRotation) {
                                case DEGREE_0 -> {
                                    primitiveRenderer.vertex(cx, cy);
                                    primitiveRenderer.vertex(cx, cy + ch);
                                    primitiveRenderer.vertex(cx + cw, cy);
                                }
                                case DEGREE_90 -> {
                                    primitiveRenderer.vertex(cx, cy);
                                    primitiveRenderer.vertex(cx, cy + ch);
                                    primitiveRenderer.vertex(cx + cw, cy + ch);
                                }
                                case DEGREE_180 -> {
                                    primitiveRenderer.vertex(cx, cy + ch);
                                    primitiveRenderer.vertex(cx + cw, cy + ch);
                                    primitiveRenderer.vertex(cx + cw, cy);
                                }
                                case DEGREE_270 -> {
                                    primitiveRenderer.vertex(cx, cy);
                                    primitiveRenderer.vertex(cx + cw, cy);
                                    primitiveRenderer.vertex(cx + cw, cy + ch);
                                }
                            }
                        }
                        case ISOSCELES_TRIANGLE -> {
                            primitiveRenderer.begin(GL32.GL_TRIANGLES);
                            switch (shape.shapeRotation) {
                                case DEGREE_0 -> {
                                    primitiveRenderer.vertex(cx, cy);
                                    primitiveRenderer.vertex(cx + cw, cy);
                                    primitiveRenderer.vertex(cx + cw2, cy + ch);
                                }
                                case DEGREE_90 -> {
                                    primitiveRenderer.vertex(cx, cy);
                                    primitiveRenderer.vertex(cx, cy + ch);
                                    primitiveRenderer.vertex(cx + cw, cy + ch2);
                                }
                                case DEGREE_180 -> {
                                    primitiveRenderer.vertex(cx + cw2, cy);
                                    primitiveRenderer.vertex(cx, cy + ch);
                                    primitiveRenderer.vertex(cx + cw, cy + ch);
                                }
                                case DEGREE_270 -> {
                                    primitiveRenderer.vertex(cx, cy + ch2);
                                    primitiveRenderer.vertex(cx + cw, cy + ch);
                                    primitiveRenderer.vertex(cx + cw, cy);
                                }
                            }
                        }
                        case DIAMOND -> {
                            primitiveRenderer.vertex(cx + cw2, cy);
                            primitiveRenderer.vertex(cx, cy + ch2);
                            primitiveRenderer.vertex(cx + cw, cy + ch2);

                            primitiveRenderer.vertex(cx + cw2, cy + ch);
                            primitiveRenderer.vertex(cx, cy + ch2);
                            primitiveRenderer.vertex(cx + cw, cy + ch2);
                        }

                    }

                    primitiveRenderer.end();
                    spriteRenderer.begin();
                }
            }
            case Progressbar progressBar -> {
                // Background
                for (int ix = 0; ix < progressBar.width; ix++) {
                    int index = ix == 0 ? 0 : ix == (progressBar.width - 1) ? 2 : 1;
                    spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_PROGRESSBAR, index, uiCommonUtils.component_getAbsoluteX(progressBar) + TS(ix), uiCommonUtils.component_getAbsoluteY(progressBar));
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
                            spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_PROGRESSBAR_BAR, index, uiCommonUtils.component_getAbsoluteX(progressBar) + xOffset, uiCommonUtils.component_getAbsoluteY(progressBar), pixels - xOffset, TS());
                        } else {
                            spriteRenderer.drawCMediaArray(UIEngineBaseMedia_8x8.UI_PROGRESSBAR_BAR, index, uiCommonUtils.component_getAbsoluteX(progressBar) + xOffset, uiCommonUtils.component_getAbsoluteY(progressBar));
                        }
                    }
                }
                spriteRenderer.loadState();

                if (progressBar.progressText) {
                    String percentTxt = progressBar.progressText2Decimal ? uiCommonUtils.progressBar_getProgressText2Decimal(progressBar.progress) : uiCommonUtils.progressBar_getProgressText(progressBar.progress);
                    int xOffset = (TS(progressBar.width) / 2) - (render_textWidth(percentTxt) / 2);
                    render_drawFont(percentTxt, uiCommonUtils.component_getAbsoluteX(progressBar) + xOffset, uiCommonUtils.component_getAbsoluteY(progressBar), progressBar.fontColor, componentAlpha, componentGrayScale, 0, 2);
                }
            }
            case Checkbox checkBox -> {
                CMediaArray checkBoxGraphic = checkBox.checkBoxStyle == CHECKBOX_STYLE.CHECKBOX ? UIEngineBaseMedia_8x8.UI_CHECKBOX_CHECKBOX : UIEngineBaseMedia_8x8.UI_CHECKBOX_RADIO;
                CMediaImage checkBoxCellGraphic = checkBox.checkBoxStyle == CHECKBOX_STYLE.CHECKBOX ? UIEngineBaseMedia_8x8.UI_CHECKBOX_CHECKBOX_CELL : UIEngineBaseMedia_8x8.UI_CHECKBOX_RADIO_CELL;


                spriteRenderer.saveState();
                render_setColor(spriteRenderer, checkBox.color2, componentAlpha, componentGrayScale);
                spriteRenderer.drawCMediaImage(checkBoxCellGraphic, uiCommonUtils.component_getAbsoluteX(checkBox), uiCommonUtils.component_getAbsoluteY(checkBox));
                spriteRenderer.loadState();

                spriteRenderer.drawCMediaArray(checkBoxGraphic, (checkBox.checked ? 1 : 0), uiCommonUtils.component_getAbsoluteX(checkBox), uiCommonUtils.component_getAbsoluteY(checkBox));

                render_drawFont(checkBox.text, uiCommonUtils.component_getAbsoluteX(checkBox) + TS(), uiCommonUtils.component_getAbsoluteY(checkBox), checkBox.fontColor, componentAlpha, componentGrayScale, 1, 1);
            }
            case AppViewport appViewPort -> {
                spriteRenderer.draw(appViewPort.textureRegion, uiCommonUtils.component_getAbsoluteX(appViewPort), uiCommonUtils.component_getAbsoluteY(appViewPort));
            }
            default -> {
            }
        }


        spriteRenderer.setAllReset();

    }

    private void render_drawCursorDragAndDrop() {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        if (uiEngineState.draggedGrid != null) {
            final Grid dragGrid = uiEngineState.draggedGrid;
            final int dragOffsetX = uiEngineState.draggedGridOffset.x;
            final int dragOffsetY = uiEngineState.draggedGridOffset.y;
            final Object dragItem = uiEngineState.draggedGridItem;
            final float dragAlpha = componentAlpha(dragGrid) * uiEngineState.config.component_gridDragAlpha;
            render_drawIcon(dragGrid.gridAction.icon(dragItem), uiEngineState.mouse_ui.x - dragOffsetX, uiEngineState.mouse_ui.y - dragOffsetY,
                    dragGrid.gridAction.iconColor(dragItem), dragAlpha, false,
                    dragGrid.gridAction.iconIndex(dragItem), dragGrid.bigMode,
                    dragGrid.gridAction.iconFlipX(), dragGrid.gridAction.iconFlipY());
        } else if (uiEngineState.draggedList != null) {
            final List dragList = uiEngineState.draggedList;
            final int dragOffsetX = uiEngineState.draggedListOffset.x;
            final int dragOffsetY = uiEngineState.draggedListOffset.y;
            final Object dragItem = uiEngineState.draggedListItem;
            final float dragAlpha = componentAlpha(dragList) * uiEngineState.config.component_listDragAlpha;
            String text = dragList.listAction.text(dragItem);
            render_drawFont(text, uiEngineState.mouse_ui.x - dragOffsetX, uiEngineState.mouse_ui.y - dragOffsetY,
                    dragList.fontColor, dragAlpha, false, 2, 1,
                    TS(dragList.width), dragList.listAction.icon(dragItem), dragList.listAction.iconIndex(dragItem), dragList.listAction.iconColor(dragItem),
                    dragList.listAction.iconFlipX(), dragList.listAction.iconFlipY());

        }

        spriteRenderer.setAllReset();
    }

    private int render_textWidth(String text) {
        return render_textWidth(text, 0, text.length());
    }

    private int render_textWidth(String text, int start, int end) {
        if (text == null || text.length() == 0) return 0;
        return mediaManager.fontTextWidth(uiEngineState.config.ui_font, text, start, end);
    }

    private void render_drawFont(String text, int x, int y, Color color, float alpha, boolean iconGrayScale) {
        render_drawFont(text, x, y, color, alpha, iconGrayScale, 0, 0, FONT_MAXWIDTH_NONE, null, 0, null, false, false, 0, text.length());
    }

    private void render_drawFont(String text, int x, int y, Color color, float alpha, boolean iconGrayScale, int textXOffset, int textYOffset) {
        render_drawFont(text, x, y, color, alpha, iconGrayScale, textXOffset, textYOffset, FONT_MAXWIDTH_NONE, null, 0, null, false, false, 0, text.length());
    }

    private void render_drawFont(String text, int x, int y, Color color, float alpha, boolean iconGrayScale, int textXOffset, int textYOffset, int maxWidth) {
        render_drawFont(text, x, y,color, alpha, iconGrayScale, textXOffset, textYOffset, maxWidth, null,0,null,false,false , 0, text.length());
    }

    private void render_drawFont(String text, int x, int y, float alpha, boolean iconGrayScale, Color color, int textXOffset, int textYOffset, int maxWidth) {
        render_drawFont(text, x, y, color, alpha, iconGrayScale, textXOffset, textYOffset, maxWidth, null, 0, null, false, false, 0, text.length());
    }

    private void render_drawFont(String text, int x, int y, Color color, float alpha, boolean iconGrayScale, int textXOffset, int textYOffset, int maxWidth, CMediaSprite icon, int iconIndex, Color iconColor, boolean iconFlipX, boolean iconFlipY) {
        render_drawFont(text, x, y, color, alpha, iconGrayScale, textXOffset, textYOffset, maxWidth, icon, iconIndex, iconColor, iconFlipX, iconFlipY, 0, text.length());
    }

    private void render_drawFont(String text, int x, int y, Color color, float alpha, boolean iconGrayScale, int textXOffset, int textYOffset, int maxWidth, CMediaSprite icon, int iconIndex, Color iconColor, boolean iconFlipX, boolean iconFlipY, int textOffset, int textLength) {
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final BitmapFont font = mediaManager.font(uiEngineState.config.ui_font);
        final boolean withIcon = icon != null;
        if (withIcon) {
            render_drawIcon(icon, x, y, iconColor, alpha, iconGrayScale, iconIndex, false, iconFlipX, iconFlipY);
        }

        spriteRenderer.saveState();
        spriteRenderer.setColor(Color.GRAY, alpha);
        font.setColor(color.r, color.g, color.b, 1f);

        if (withIcon) maxWidth -= TS();
        spriteRenderer.drawCMediaFont(uiEngineState.config.ui_font, x + (withIcon ? TS() : 0) + textXOffset, y + textYOffset, text, textOffset, textLength, false, false, maxWidth);


        spriteRenderer.loadState();
    }

    private void render_drawIcon(CMediaSprite icon, int x, int y, Color color, float iconAlpha, boolean iconGrayscale, int arrayIndex, boolean bigMode, boolean flipX, boolean flipY) {
        if (icon == null) return;
        final SpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        spriteRenderer.saveState();
        render_setColor(spriteRenderer, color, iconAlpha, iconGrayscale);
        int scale = bigMode ? TS2() : TS();

        int width = mediaManager.spriteWidth(icon);
        int height = mediaManager.spriteHeight(icon);
        int renderWidth = Math.min(width, scale);
        int renderHeight = Math.min(height, scale);
        int xOffset = (scale - renderWidth) / 2;
        int yOffset = (scale - renderHeight) / 2;

        spriteRenderer.drawCMediaSprite(icon, arrayIndex, uiCommonUtils.ui_getAnimationTimer(uiEngineState),
                x + xOffset, y + yOffset, renderWidth, renderHeight, 0, 0, width, height, flipX, flipY);
        spriteRenderer.loadState();
    }

    @Override
    public void dispose() {
        this.uiAdapter.dispose();

        // Renderers
        uiEngineState.spriteRenderer_ui.dispose();
        uiEngineState.primitiveRenderer_ui.dispose();

        // FrameBuffers
        for (int i = uiEngineState.appViewPorts.size - 1; i >= 0; i--) {
            uiEngineState.appViewPorts.get(i).frameBuffer.dispose();
            uiEngineState.appViewPorts.removeIndex(i);
        }

        uiEngineState.frameBuffer_app.dispose();
        uiEngineState.frameBufferComponent_ui.dispose();
        uiEngineState.frameBufferModal_ui.dispose();
        uiEngineState.frameBuffer_composite.dispose();
        if (uiEngineState.frameBuffer_upScaled_screen != null) {
            uiEngineState.frameBuffer_upScaled_screen.dispose();
        }

        // Tooltips
        ObjectSet<Tooltip> toolTips = new ObjectSet<>();
        if (uiEngineState.tooltip != null)
            toolTips.add(uiEngineState.tooltip);
        if (uiEngineState.appToolTip != null)
            toolTips.add(uiEngineState.appToolTip);
        if (uiEngineState.fadeOutTooltip != null)
            toolTips.add(uiEngineState.fadeOutTooltip);

        ObjectSet.ObjectSetIterator<Tooltip> iterator = toolTips.iterator();
        while (iterator.hasNext) {
            final Tooltip tooltip = iterator.next();
            if (tooltip.toolTipAction != null)
                tooltip.toolTipAction.onRemove();
        }

        // Notifications
        for (int i = uiEngineState.notifications.size - 1; i >= 0; i--) {
            if (uiEngineState.notifications.get(i).notificationAction != null)
                uiEngineState.notifications.get(i).notificationAction.onRemove();
            uiEngineState.notifications.removeIndex(i);
        }

        for (int i = uiEngineState.tooltipNotifications.size - 1; i >= 0; i--) {
            if (uiEngineState.tooltipNotifications.get(i).tooltip.toolTipAction != null)
                uiEngineState.tooltipNotifications.get(i).tooltip.toolTipAction.onRemove();
            uiEngineState.tooltipNotifications.removeIndex(i);
        }

        // Windows
        for (int i = uiEngineState.windows.size - 1; i >= 0; i--) {
            if (uiEngineState.windows.get(i).windowAction != null)
                uiEngineState.windows.get(i).windowAction.onRemove();
            uiEngineState.windows.removeIndex(i);
        }
        for (int i = uiEngineState.modalWindowQueue.size - 1; i >= 0; i--) {
            if (uiEngineState.modalWindowQueue.get(i).windowAction != null)
                uiEngineState.modalWindowQueue.get(i).windowAction.onRemove();
            uiEngineState.modalWindowQueue.removeIndex(i);
        }


        // Misc
        uiCommonUtils.resetAllReferences(uiEngineState);

        uiEngineState.screenComponents.clear();
        uiEngineState.hotKeys.clear();
        uiEngineState.singleUpdateActions.clear();
        uiEngineState.forceTooltipUpdateComponents.clear();

        System.gc();
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

    public NestedFrameBuffer getFrameBufferComposite() {
        return uiEngineState.frameBuffer_composite;
    }

    public NestedFrameBuffer getFrameBufferApp() {
        return uiEngineState.frameBuffer_app;
    }

    public NestedFrameBuffer getFrameBufferUIComponent() {
        return uiEngineState.frameBufferComponent_ui;
    }


    public NestedFrameBuffer getFrameBufferUIModal() {
        return uiEngineState.frameBufferModal_ui;
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
