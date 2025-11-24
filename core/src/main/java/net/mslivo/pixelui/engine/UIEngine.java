package net.mslivo.pixelui.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.Queue;
import com.monstrous.gdx.webgpu.graphics.WgShaderProgram;
import com.monstrous.gdx.webgpu.graphics.g2d.WgSpriteBatch;
import net.mslivo.pixelui.engine.actions.common.CommonActions;
import net.mslivo.pixelui.engine.actions.common.UpdateAction;
import net.mslivo.pixelui.engine.constants.*;
import net.mslivo.pixelui.media.CMediaArray;
import net.mslivo.pixelui.media.CMediaImage;
import net.mslivo.pixelui.media.CMediaSprite;
import net.mslivo.pixelui.media.MediaManager;
import net.mslivo.pixelui.rendering.SpriteRenderer;
import net.mslivo.pixelui.rendering.WgSpriteRenderer;
import net.mslivo.pixelui.rendering.XWgFrameBuffer;
import net.mslivo.pixelui.theme.UIEngineTheme;
import net.mslivo.pixelui.utils.Tools;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;


/**
 * UI Engine
 * Handles UI Elements, Input, Cameras
 * App needs to be implemented inside the uiAdapter
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public final class UIEngine<T extends UIEngineAdapter> implements Disposable {
    private static final int FONT_MAX_WIDTH_NONE = -1;
    static final char M_TEXTINPUT_CHAR_CHANGE_CASE = '\u0014'; // Device Control 4, unused ascii
    static final char M_TEXTINPUT_CHAR_BACK_ = '\b';
    static final char M_TEXTINPUT_CHAR_ACCEPT = '\n';

    // Basic Configuration
    private final T uiAdapter;
    private final UIEngineState uiEngineState;
    private final UICommonUtils uiCommonUtils;
    private final API api;
    private final MediaManager mediaManager;

    public T getAdapter() {
        return uiAdapter;
    }

    public UIEngine(T uiAdapter, MediaManager mediaManager, UIEngineTheme theme, int resolutionWidth, int resolutionHeight) {
        this(uiAdapter, mediaManager, theme, resolutionWidth, resolutionHeight, VIEWPORT_MODE.PIXEL_PERFECT, true);
    }

    public UIEngine(T uiAdapter, MediaManager mediaManager, UIEngineTheme theme, int resolutionWidth, int resolutionHeight, VIEWPORT_MODE viewportMode) {
        this(uiAdapter, mediaManager, theme, resolutionWidth, resolutionHeight, viewportMode, true);
    }

    public UIEngine(T uiAdapter, MediaManager mediaManager, UIEngineTheme theme, int resolutionWidth, int resolutionHeight, VIEWPORT_MODE viewportMode, boolean gamePadSupport) {
        if (uiAdapter == null || mediaManager == null | theme == null) {
            throw new RuntimeException("Cannot initialize UIEngine: missing parameters");
        }
        this.uiAdapter = uiAdapter;
        this.mediaManager = mediaManager;

        // Setup State & State Utils
        this.uiEngineState = initializeInputState(resolutionWidth, resolutionHeight, viewportMode, theme, gamePadSupport);
        this.uiCommonUtils = new UICommonUtils(this.uiEngineState, this.mediaManager);
        // Setup API
        this.api = new API(this.uiEngineState, mediaManager);

        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL32.GL_COLOR_BUFFER_BIT);
        /*  Call Adapter Init */
        this.uiAdapter.init(this.api, this.mediaManager);
    }


    private UIEngineState initializeInputState(int resolutionWidth, int resolutionHeight, VIEWPORT_MODE viewportMode, UIEngineTheme theme, boolean gamePadSupport) {
        UIEngineState newUIEngineState = new UIEngineState();

        //  ----- Paramters
        newUIEngineState.resolutionWidth = Math.max(resolutionWidth, 16);
        newUIEngineState.resolutionHeight = Math.max(resolutionHeight, 16);
        newUIEngineState.resolutionWidthHalf = MathUtils.round(resolutionWidth / 2f);
        newUIEngineState.resolutionHeightHalf = MathUtils.round(resolutionHeight / 2f);
        newUIEngineState.viewportMode = viewportMode != null ? viewportMode : VIEWPORT_MODE.PIXEL_PERFECT;
        newUIEngineState.gamePadSupport = gamePadSupport;
        newUIEngineState.theme = theme;

        // ----- Config
        newUIEngineState.config = new UIEngineConfig(newUIEngineState.theme);

        // -----  App
        newUIEngineState.camera_app = UICommonUtils.camera_createCamera(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.frameBuffer_app = UICommonUtils.frameBuffer_createFrameBuffer(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.frameBuffer_app.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // -----  GUI
        newUIEngineState.spriteRenderer_ui = new WgSpriteRenderer(this.mediaManager,8024);
        //newUIEngineState.spriteRenderer_ui.setTweakResetValues(0.5f, 0.5f, 0.5f, 0f);
        //newUIEngineState.primitiveRenderer_ui = new PrimitiveRenderer(ShaderParser.parse(Tools.File.findResource("shaders/pixelui/hsl.primitive.glsl")));
        //newUIEngineState.primitiveRenderer_ui.setTweakResetValues(0.5f, 0.5f, 0.5f, 0f);


        newUIEngineState.camera_ui = UICommonUtils.camera_createCamera(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);

        newUIEngineState.frameBufferComponent_ui = UICommonUtils.frameBuffer_createFrameBuffer(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.frameBufferModal_ui = UICommonUtils.frameBuffer_createFrameBuffer(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);

        // ----- Composite
        newUIEngineState.frameBuffer_composite = UICommonUtils.frameBuffer_createFrameBuffer(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        // ----- Screen
        newUIEngineState.camera_screen = UICommonUtils.camera_createCamera(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        newUIEngineState.viewport_screen = UICommonUtils.viewport_createViewport(newUIEngineState.viewportMode, newUIEngineState.camera_screen, newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
        if (viewportMode.upscale) {
            newUIEngineState.upScaleFactor_screen = uiCommonUtils.viewport_determineUpscaleFactor(newUIEngineState.resolutionWidth, newUIEngineState.resolutionHeight);
            newUIEngineState.frameBuffer_upScaled_screen = UICommonUtils.frameBuffer_createFrameBuffer(newUIEngineState.resolutionWidth * newUIEngineState.upScaleFactor_screen, newUIEngineState.resolutionHeight * newUIEngineState.upScaleFactor_screen);
            newUIEngineState.frameBuffer_upScaled_screen.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        // -----  GUI
        newUIEngineState.windows = new Array<>();
        newUIEngineState.screenComponents = new Array<>();
        newUIEngineState.openContextMenu = null;
        newUIEngineState.pressedContextMenuItem = null;
        newUIEngineState.displayedContextMenuWidth = 0;
        newUIEngineState.openMouseTextInput = null;
        newUIEngineState.mTextInputTempHardwareMousePosition = new GridPoint2(0, 0);
        newUIEngineState.mTextInputMouse1Pressed = false;
        newUIEngineState.mTextInputMouse2Pressed = false;
        newUIEngineState.mTextInputScrollTimer = 0;
        newUIEngineState.mTextInputScrollTime = 0;
        newUIEngineState.mTextInputUnlock = false;
        newUIEngineState.keyboardInteractedUIObjectFrame = null;
        newUIEngineState.mouseInteractedUIObjectFrame = null;
        newUIEngineState.forceTooltipUpdateComponents = new Array<>();
        newUIEngineState.modalWindow = null;
        newUIEngineState.modalWindowQueue = new Queue<>();
        newUIEngineState.pressedTextField = null;
        newUIEngineState.pressedTextFieldInitCaretPosition = 0;
        newUIEngineState.focusedTextField = null;
        newUIEngineState.focusedTextField_repeatedKey = KeyCode.NONE;
        newUIEngineState.focusedTextField_repeatedKeyTimer = 0;
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
        newUIEngineState.mouseUI = new GridPoint2(newUIEngineState.resolutionWidthHalf, newUIEngineState.resolutionHeightHalf);
        newUIEngineState.mouseApp = new GridPoint2(0, 0);
        newUIEngineState.mouseDelta = new GridPoint2(0, 0);
        newUIEngineState.lastUIMouseHover = null;
        newUIEngineState.cursor = null;
        newUIEngineState.cursorArrayIndex = 0;
        newUIEngineState.mouseTool = null;
        newUIEngineState.overrideCursor = null;
        newUIEngineState.overrideCursorArrayIndex = 0;
        newUIEngineState.displayOverrideCursor = false;
        newUIEngineState.fboCursorVector = new Vector3(0, 0, 0);
        newUIEngineState.unProjectVector = new Vector2(0, 0);
        newUIEngineState.emulatedMousePosition = new Vector2(newUIEngineState.resolutionWidthHalf, newUIEngineState.resolutionHeightHalf);
        newUIEngineState.emulatedMouseDirection = new Vector2(newUIEngineState.resolutionWidthHalf, newUIEngineState.resolutionHeightHalf);
        newUIEngineState.emulatedMouseLastMouseClick = 0;
        newUIEngineState.keyBoardMouseSmoothing = new Vector2(0, 0);
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
        return newUIEngineState;
    }

    public void resize(int width, int height) {
        uiEngineState.viewport_screen.update(width, height, true);
    }

    public void update() {
        // UI
        this.updateMouseControl();
        this.updateUI(); // Main UI Updates happen here


        // Update Game
        this.uiAdapter.update();

        // Reset Input Events
        this.uiEngineState.inputEvents.reset();
    }

    private void updateMouseControl() {
        final boolean hardwareMouseEnabled = uiEngineState.config.input.hardwareMouseEnabled;
        final boolean keyboardMouseEnabled = uiEngineState.config.input.keyboardMouseEnabled;
        final boolean gamepadMouseEnabled = uiEngineState.config.input.gamePadMouseEnabled;

        if (!hardwareMouseEnabled && !keyboardMouseEnabled && !gamepadMouseEnabled) {
            mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE.DISABLED);
            mouseControl_chokeAllMouseEvents();
        } else {
            if (uiEngineState.config.input.gamePadMouseEnabled && mouseControl_gamePadMouseChokeAndTranslateEvents()) {
                mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE.GAMEPAD);
            } else if (uiEngineState.config.input.keyboardMouseEnabled && mouseControl_keyboardMouseChokeAndTranslateEvents()) {
                mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE.KEYBOARD);
            } else if (uiEngineState.config.input.hardwareMouseEnabled && mouseControl_hardwareMouseDetectUse()) {
                mouseControl_setNextMouseControlMode(MOUSE_CONTROL_MODE.HARDWARE_MOUSE);
            }
        }

        // Translate to MouseGUI position
        switch (uiEngineState.currentControlMode) {
            case GAMEPAD -> mouseControl_updateGamePadMouse();
            case KEYBOARD -> mouseControl_updateKeyBoardMouse();
            case HARDWARE_MOUSE -> mouseControl_updateHardwareMouse();
            case DISABLED -> {
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
                    uiEngineState.keyBoardMouseSmoothing.set(0f, 0f);
                }
                // Simulated
                for (int i = 0; i <= 4; i++) uiEngineState.emulatedMouseIsButtonDown[i] = false;
                uiEngineState.emulatedMouseLastMouseClick = 0;
            }

            // Set Next ControlMode
            uiEngineState.currentControlMode = nextControlMode;

            if (nextControlMode.emulated) {
                uiCommonUtils.emulatedMouse_setPosition(uiEngineState.mouseUI.x, uiEngineState.mouseUI.y);
            }
        }
    }


    private void updateUI_updateMouseTextInput() {
        if (!uiCommonUtils.mouseTextInput_isOpen())
            return;
        if (uiEngineState.config.ui.mouseInteractionsDisabled)
            return;
        MouseTextInput mouseTextInput = uiEngineState.openMouseTextInput;
        char[] characters = mouseTextInput.upperCase ? mouseTextInput.charactersUC : mouseTextInput.charactersLC;

        DIRECTION scrollDirection = DIRECTION.NONE;
        boolean mouse1Pressed, mouse2Pressed;
        int mouseScrolled = 0;


        if (uiEngineState.currentControlMode.emulated) {
            DIRECTION toDirection = DIRECTION.NONE;
            boolean moved = false;
            if (uiEngineState.emulatedMouseDirection.x >= 1) {
                moved = true;
                toDirection = DIRECTION.RIGHT;
            }
            if (uiEngineState.emulatedMouseDirection.x <= -1) {
                moved = true;
                toDirection = DIRECTION.LEFT;
            }
            if (uiEngineState.emulatedMouseDirection.y >= 1) {
                moved = true;
                toDirection = DIRECTION.DOWN;
            }
            if (uiEngineState.emulatedMouseDirection.y <= -1) {
                toDirection = DIRECTION.UP;
                moved = true;
            }

            if (moved) {
                uiEngineState.mTextInputScrollTimer++;
                if (uiEngineState.mTextInputScrollTimer > uiEngineState.mTextInputScrollTime) {
                    scrollDirection = toDirection;
                    uiEngineState.mTextInputScrollTime = 10;
                    uiEngineState.mTextInputScrollTimer = 0;
                }


            } else {
                uiEngineState.mTextInputScrollTimer = 0;
                uiEngineState.mTextInputScrollTime = 0;
            }

            // keep steady position
            uiCommonUtils.emulatedMouse_setPosition(mouseTextInput.x, mouseTextInput.y);
        } else {
            int cursorDeltaX = uiEngineState.mouseUI.x - uiEngineState.mTextInputTempHardwareMousePosition.x;
            int cursorDeltaY = uiEngineState.mouseUI.y - uiEngineState.mTextInputTempHardwareMousePosition.y;
            final int SENSITIVITY = 12;

            boolean moved = false;

            // horizontal detection
            if (cursorDeltaX > SENSITIVITY) {
                scrollDirection = DIRECTION.RIGHT;
                moved = true;
            } else if (cursorDeltaX < -SENSITIVITY) {
                scrollDirection = DIRECTION.LEFT;
                moved = true;
            }

            // vertical detection
            if (cursorDeltaY > SENSITIVITY) {
                scrollDirection = DIRECTION.UP;
                moved = true;
            } else if (cursorDeltaY < -SENSITIVITY) {
                scrollDirection = DIRECTION.DOWN;
                moved = true;
            }

            if (moved) {
                // Update reference position for next delta check
                uiEngineState.mTextInputTempHardwareMousePosition.x = uiEngineState.mouseUI.x;
                uiEngineState.mTextInputTempHardwareMousePosition.y = uiEngineState.mouseUI.y;

            }
        }

        /*
        int cursorDeltaX = uiEngineState.mouseUI.x - uiEngineState.mTextInputTempMousePosition.x;
        int cursorDeltaY = uiEngineState.mouseUI.y - uiEngineState.mTextInputTempMousePosition.y;
        final int SENSITIVITY = 12;

        boolean moved = false;

        // horizontal detection
        if (cursorDeltaX > SENSITIVITY) {
            scrollDirection = DIRECTION.RIGHT;
            moved = true;
        } else if (cursorDeltaX < -SENSITIVITY) {
            scrollDirection = DIRECTION.LEFT;
            moved = true;
        }

        // vertical detection
        if (cursorDeltaY > SENSITIVITY) {
            scrollDirection = DIRECTION.UP;
            moved = true;
        } else if (cursorDeltaY < -SENSITIVITY) {
            scrollDirection = DIRECTION.DOWN;
            moved = true;
        }

        if (moved) {
            // Update reference position for next delta check
            uiEngineState.mTextInputTempMousePosition.x = uiEngineState.mouseUI.x;
            uiEngineState.mTextInputTempMousePosition.y = uiEngineState.mouseUI.y;

            // Keep the *emulated* cursor in place visually
            // (undo the real delta so emulated cursor doesn't drift)
            if (uiEngineState.currentControlMode.emulated) {
                uiEngineState.mouseEmulatedPosition.x -= cursorDeltaX;
                uiEngineState.mTextInputTempMousePosition.x -= cursorDeltaX;
                uiEngineState.mouseEmulatedPosition.y -= cursorDeltaY;
                uiEngineState.mTextInputTempMousePosition.y -= cursorDeltaY;
            }
        }

         */


        mouse1Pressed = uiEngineState.inputEvents.mouseButtonsDown[KeyCode.Mouse.LEFT];
        mouse2Pressed = uiEngineState.inputEvents.mouseButtonsDown[KeyCode.Mouse.RIGHT];

        if (uiEngineState.inputEvents.mouseScrolled) {
            mouseScrolled = uiEngineState.inputEvents.mouseScrolledAmount < 0f ? 1 : -1;
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
        final int charsPerRow = uiEngineState.config.mouseTextInput.charsPerRow;
        int index = mouseTextInput.selectedIndex;
        int totalChars = mouseTextInput.upperCase ? mouseTextInput.charactersUC.length : mouseTextInput.charactersLC.length;

        switch (scrollDirection) {
            case NONE -> {
            }
            case LEFT -> {
                int rowStart = (index / charsPerRow) * charsPerRow;
                if (index > rowStart) {
                    index--;
                } else {
                    index = rowStart + charsPerRow - 1;
                }
            }
            case RIGHT -> {
                int rowEnd = Math.min(((index / charsPerRow) + 1) * charsPerRow - 1, totalChars - 1);
                if (index < rowEnd) {
                    index++;
                } else {
                    index = rowEnd - charsPerRow + 1;
                }
            }
            case UP -> {
                if (index - charsPerRow >= 0) {
                    index -= charsPerRow;
                } else {
                    int lastRowStart = (Math.max((totalChars - 1) / charsPerRow, 0)) * charsPerRow;
                    int col = index % charsPerRow;
                    index = Math.min(lastRowStart + col, totalChars - 1);
                }
            }
            case DOWN -> {
                if (index + charsPerRow < totalChars) {
                    index += charsPerRow;
                } else {
                    int col = index % charsPerRow;
                    index = col;
                }
            }
        }
        uiCommonUtils.mouseTextInput_selectIndex(mouseTextInput, index);


        // Confirm Character from Input
        boolean enterRegularCharacterMouse1 = false;
        boolean deleteCharacterMouse2 = false;
        boolean changeCase = false;
        if (mouse1Pressed && !uiEngineState.mTextInputMouse1Pressed) uiEngineState.mTextInputMouse1Pressed = true;
        if (!mouse1Pressed && uiEngineState.mTextInputMouse1Pressed) {
            enterRegularCharacterMouse1 = true;
            uiEngineState.mTextInputMouse1Pressed = false;
        }

        if (mouse2Pressed && !uiEngineState.mTextInputMouse2Pressed) uiEngineState.mTextInputMouse2Pressed = true;
        if (!mouse2Pressed && uiEngineState.mTextInputMouse2Pressed) {
            // Delete from Mouse 2
            deleteCharacterMouse2 = true;
            uiEngineState.mTextInputMouse2Pressed = false;
        }

        if (mouseScrolled == 1 && !mouseTextInput.upperCase) {
            changeCase = true;
        } else if (mouseScrolled == -1 && mouseTextInput.upperCase) {
            changeCase = true;
        }


        // Confirm Character from API Queue if nothing was pressed
        if (!enterRegularCharacterMouse1 && !mouseTextInput.enterCharacterQueue.isEmpty()) {
            uiCommonUtils.mouseTextInput_selectCharacter(uiEngineState.openMouseTextInput, (char) mouseTextInput.enterCharacterQueue.removeIndex(0));
            enterRegularCharacterMouse1 = true;
        }

        if (enterRegularCharacterMouse1 || deleteCharacterMouse2 || changeCase) {
            char c;

            if (changeCase) {
                c = M_TEXTINPUT_CHAR_CHANGE_CASE;
            } else if (deleteCharacterMouse2) {
                c = M_TEXTINPUT_CHAR_BACK_;
            } else {
                c = characters[mouseTextInput.selectedIndex];
            }

            switch (c) {
                // Control ChangeCase
                case M_TEXTINPUT_CHAR_CHANGE_CASE -> {
                    mouseTextInput.upperCase = !mouseTextInput.upperCase;
                    mouseTextInput.mouseTextInputAction.onChangeCase(mouseTextInput.upperCase);
                }
                // Control Delete
                case M_TEXTINPUT_CHAR_BACK_ -> {
                    uiEngineState.inputEvents.keyDown = true;
                    uiEngineState.inputEvents.keyDownKeyCodes.add(KeyCode.Key.BACKSPACE);
                    uiEngineState.inputEvents.keyUp = true;
                    uiEngineState.inputEvents.keyUpKeyCodes.add(KeyCode.Key.BACKSPACE);
                    mouseTextInput.mouseTextInputAction.onDelete();
                }
                // Control Confirm
                case M_TEXTINPUT_CHAR_ACCEPT -> {
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
        uiEngineState.mouseUI.x = 0;
        uiEngineState.mouseUI.y = 0;
        uiEngineState.mouseDelta.x = 0;
        uiEngineState.mouseDelta.y = 0;
    }

    private boolean mouseControl_hardwareMouseDetectUse() {
        return uiEngineState.inputEvents.mouseDown || uiEngineState.inputEvents.mouseUp ||
                uiEngineState.inputEvents.mouseMoved || uiEngineState.inputEvents.mouseDragged || uiEngineState.inputEvents.mouseScrolled;
    }


    private boolean mouseControl_gamePadMouseChokeAndTranslateEvents() {
        // Remove Key down input events and set to temporary variable keyBoardTranslatedKeysDown
        boolean gamepadMouseUsed = false;
        for (int i = 0; i <= UIEngineConfig.GAMEPAD_MOUSE_BUTTONS; i++) {
            int[] buttons = uiEngineState.config.input.gamepadMouseButtons(i);
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
        if (uiEngineState.config.input.gamePadMouseStickLeftEnabled) {
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
        if (uiEngineState.config.input.gamePadMouseStickRightEnabled) {
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

    private boolean mouseControl_keyboardMouseChokeAndTranslateEvents() {
        if (uiEngineState.focusedTextField != null) return false; // Disable during Textfield Input
        boolean keyboardMouseUsed = false;
        // Remove Key down input events and set to temporary variable keyBoardTranslatedKeysDown
        for (int i = 0; i <= UIEngineConfig.KEYBOARD_MOUSE_BUTTONS; i++) {
            int[] buttons = uiEngineState.config.input.keyboardMouseButtons(i);
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


    private void mouseControl_emulateMouseEvents(boolean buttonMouse1Down, boolean buttonMouse2Down, boolean buttonMouse3Down, boolean buttonMouse4Down, boolean buttonMouse5Down,
                                                 boolean buttonScrolledUp, boolean buttonScrolledDown, float cursorChangeX, float cursorChangeY
    ) {
        uiCommonUtils.emulatedMouse_setPosition(uiEngineState.emulatedMousePosition.x + cursorChangeX, uiEngineState.emulatedMousePosition.y - cursorChangeY);

        uiEngineState.emulatedMouseDirection.set(cursorChangeX, cursorChangeY);

        // Set to final
        int xNew = MathUtils.round(uiEngineState.emulatedMousePosition.x);
        int yNew = MathUtils.round(uiEngineState.emulatedMousePosition.y);

        uiEngineState.mouseDelta.x = xNew - uiEngineState.mouseUI.x;
        uiEngineState.mouseDelta.y = yNew - uiEngineState.mouseUI.y;

        uiEngineState.mouseUI.x = xNew;
        uiEngineState.mouseUI.y = yNew;

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
        if (cursorChangeX != 0 || cursorChangeY != 0) {
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
        boolean stickLeft = uiEngineState.config.input.gamePadMouseStickLeftEnabled;
        boolean stickRight = uiEngineState.config.input.gamePadMouseStickRightEnabled;


        float joystickDeadZone = uiEngineState.config.input.gamePadMouseJoystickDeadZone;
        boolean buttonLeft = (stickLeft && uiEngineState.gamePadTranslatedStickLeft.x < -joystickDeadZone) || (stickRight && uiEngineState.gamePadTranslatedStickRight.x < -joystickDeadZone);
        boolean buttonRight = (stickLeft && uiEngineState.gamePadTranslatedStickLeft.x > joystickDeadZone) || (stickRight && uiEngineState.gamePadTranslatedStickRight.x > joystickDeadZone);
        boolean buttonUp = (stickLeft && uiEngineState.gamePadTranslatedStickLeft.y > joystickDeadZone) || (stickRight && uiEngineState.gamePadTranslatedStickRight.y > joystickDeadZone);
        boolean buttonDown = (stickLeft && uiEngineState.gamePadTranslatedStickLeft.y < -joystickDeadZone) || (stickRight && uiEngineState.gamePadTranslatedStickRight.y < -joystickDeadZone);
        boolean buttonMouse1Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input.gamePadMouseButtonsMouse1);
        boolean buttonMouse2Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input.gamePadMouseButtonsMouse2);
        boolean buttonMouse3Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input.gamePadMouseButtonsMouse3);
        boolean buttonMouse4Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input.gamePadMouseButtonsMouse4);
        boolean buttonMouse5Down = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input.gamePadMouseButtonsMouse5);
        boolean buttonScrolledUp = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input.gamePadMouseButtonsScrollUp);
        boolean buttonScrolledDown = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input.gamePadMouseButtonsScrollDown);
        boolean buttonCursorSpeedUpDown = mouseControl_isTranslatedKeyCodeDown(translatedButtons, uiEngineState.config.input.gamePadMouseButtonsCursorSpeedUp);

        final float deadZoneDelta = 1f - joystickDeadZone;
        float cursorChangeX;
        if (buttonLeft) {
            cursorChangeX = Math.min(uiEngineState.gamePadTranslatedStickLeft.x, uiEngineState.gamePadTranslatedStickRight.x);
            cursorChangeX = (cursorChangeX + joystickDeadZone) / deadZoneDelta;
        } else if (buttonRight) {
            cursorChangeX = Math.max(uiEngineState.gamePadTranslatedStickLeft.x, uiEngineState.gamePadTranslatedStickRight.x);
            cursorChangeX = (cursorChangeX - joystickDeadZone) / deadZoneDelta;
        } else {
            cursorChangeX = 0;
        }

        float cursorChangeY;
        if (buttonUp) {
            cursorChangeY = Math.max(uiEngineState.gamePadTranslatedStickLeft.y, uiEngineState.gamePadTranslatedStickRight.y);
            cursorChangeY = -((cursorChangeY - joystickDeadZone) / deadZoneDelta);
        } else if (buttonDown) {
            cursorChangeY = Math.min(uiEngineState.gamePadTranslatedStickLeft.y, uiEngineState.gamePadTranslatedStickRight.y);
            cursorChangeY = -((cursorChangeY + joystickDeadZone) / deadZoneDelta);
        } else {
            cursorChangeY = 0;
        }

        final float speed = uiEngineState.config.input.gamepadMouseCursorSpeed;
        cursorChangeX *= speed;
        cursorChangeY *= speed;

        if (buttonCursorSpeedUpDown) {
            final float speedUpFactor = uiEngineState.config.input.gamepadMouseCursorSpeedUpFactor;
            cursorChangeX *= speedUpFactor;
            cursorChangeY *= speedUpFactor;
        }

        // Translate to mouse events
        mouseControl_emulateMouseEvents(buttonMouse1Down, buttonMouse2Down, buttonMouse3Down, buttonMouse4Down, buttonMouse5Down,
                buttonScrolledUp, buttonScrolledDown, cursorChangeX, cursorChangeY
        );
    }

    private void mouseControl_updateKeyBoardMouse() {
        if (uiEngineState.focusedTextField != null) return; // Disable during Textfield Input

        // Swallow & Translate keyboard events
        boolean[] translatedKeys = uiEngineState.keyBoardTranslatedKeysDown;

        boolean buttonLeft = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input.keyboardMouseButtonsLeft);
        boolean buttonRight = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input.keyboardMouseButtonsRight);
        boolean buttonUp = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input.keyboardMouseButtonsUp);
        boolean buttonDown = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input.keyboardMouseButtonsDown);
        boolean buttonMouse1Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input.keyboardMouseButtonsMouse1);
        boolean buttonMouse2Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input.keyboardMouseButtonsMouse2);
        boolean buttonMouse3Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input.keyboardMouseButtonsMouse3);
        boolean buttonMouse4Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input.keyboardMouseButtonsMouse4);
        boolean buttonMouse5Down = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input.keyboardMouseButtonsMouse5);
        boolean buttonScrolledUp = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input.keyboardMouseButtonsScrollUp);
        boolean buttonScrolledDown = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input.keyboardMouseButtonsScrollDown);
        boolean buttonCursorSpeedUpDown = mouseControl_isTranslatedKeyCodeDown(translatedKeys, uiEngineState.config.input.keyboardMouseButtonsCursorSpeedUp);

        final float smoothing = uiEngineState.config.input.keyboardMouseCursorSmoothing;
        if (buttonLeft) {
            uiEngineState.keyBoardMouseSmoothing.x = Interpolation.linear.apply(uiEngineState.keyBoardMouseSmoothing.x, -1, smoothing);
        } else if (buttonRight) {
            uiEngineState.keyBoardMouseSmoothing.x = Interpolation.linear.apply(uiEngineState.keyBoardMouseSmoothing.x, 1f, smoothing);
        } else {
            if (Math.abs(uiEngineState.keyBoardMouseSmoothing.x) < 0.001f) {
                uiEngineState.keyBoardMouseSmoothing.x = 0;
            } else {
                uiEngineState.keyBoardMouseSmoothing.x = Interpolation.linear.apply(uiEngineState.keyBoardMouseSmoothing.x, 0f, smoothing);
            }

        }

        if (buttonUp) {
            uiEngineState.keyBoardMouseSmoothing.y = Interpolation.linear.apply(uiEngineState.keyBoardMouseSmoothing.y, -1, smoothing);
        } else if (buttonDown) {
            uiEngineState.keyBoardMouseSmoothing.y = Interpolation.linear.apply(uiEngineState.keyBoardMouseSmoothing.y, 1, smoothing);
        } else {
            if (Math.abs(uiEngineState.keyBoardMouseSmoothing.y) < 0.001f) {
                uiEngineState.keyBoardMouseSmoothing.y = 0f;
            } else {
                uiEngineState.keyBoardMouseSmoothing.y = Interpolation.linear.apply(uiEngineState.keyBoardMouseSmoothing.y, 0f, smoothing);
            }
        }

        final float speed = uiEngineState.config.input.keyboardMouseCursorSpeed;
        float cursorChangeX = uiEngineState.keyBoardMouseSmoothing.x * speed;
        float cursorChangeY = uiEngineState.keyBoardMouseSmoothing.y * speed;

        if (buttonCursorSpeedUpDown) {
            final float speedUpFactor = uiEngineState.config.input.keyboardMouseCursorSpeedUpFactor;
            cursorChangeX *= speedUpFactor;
            cursorChangeY *= speedUpFactor;
        }

        // Translate to mouse events
        mouseControl_emulateMouseEvents(buttonMouse1Down, buttonMouse2Down, buttonMouse3Down, buttonMouse4Down, buttonMouse5Down,
                buttonScrolledUp, buttonScrolledDown, cursorChangeX, cursorChangeY
        );
    }

    private void mouseControl_enforceUIMouseBounds() {
        uiEngineState.mouseUI.x = Math.clamp(uiEngineState.mouseUI.x, 0, uiEngineState.resolutionWidth);
        uiEngineState.mouseUI.y = Math.clamp(uiEngineState.mouseUI.y, 0, uiEngineState.resolutionHeight);
    }

    private void mouseControl_updateGameMouseXY() {
        // MouseXGUI/MouseYGUI -> To MouseX/MouseY
        uiEngineState.fboCursorVector.x = uiEngineState.mouseUI.x;
        uiEngineState.fboCursorVector.y = Gdx.graphics.getHeight() - uiEngineState.mouseUI.y;
        uiEngineState.fboCursorVector.z = 1;
        uiEngineState.camera_app.unproject(uiEngineState.fboCursorVector, 0, 0, uiEngineState.resolutionWidth, uiEngineState.resolutionHeight);
        this.uiEngineState.mouseApp.x = (int) uiEngineState.fboCursorVector.x;
        this.uiEngineState.mouseApp.y = (int) uiEngineState.fboCursorVector.y;
    }

    private void mouseControl_updateHardwareMouse() {
        // --- GUI CURSOR ---
        // ScreenCursor To WorldCursor
        uiEngineState.unProjectVector.x = Gdx.input.getX();
        uiEngineState.unProjectVector.y = Gdx.input.getY();

        uiEngineState.viewport_screen.unproject(uiEngineState.unProjectVector);
        // WorldCursor to  FBOCursor
        uiEngineState.fboCursorVector.x = uiEngineState.unProjectVector.x;
        uiEngineState.fboCursorVector.y = Gdx.graphics.getHeight() - uiEngineState.unProjectVector.y;
        uiEngineState.fboCursorVector.z = 1;
        uiEngineState.camera_ui.unproject(uiEngineState.fboCursorVector, 0, 0, uiEngineState.resolutionWidth, uiEngineState.resolutionHeight);

        // Set to final
        uiEngineState.mouseDelta.x = MathUtils.round(uiEngineState.fboCursorVector.x - uiEngineState.mouseUI.x);
        uiEngineState.mouseDelta.y = MathUtils.round(uiEngineState.fboCursorVector.y - uiEngineState.mouseUI.y);
        uiEngineState.mouseUI.x = Math.clamp(MathUtils.round(uiEngineState.fboCursorVector.x), 0, uiEngineState.resolutionWidth);
        uiEngineState.mouseUI.y = Math.clamp(MathUtils.round(uiEngineState.fboCursorVector.y), 0, uiEngineState.resolutionHeight);
    }

    private void mouseControl_updateLastUIMouseHover() {
        uiEngineState.lastUIMouseHover = uiCommonUtils.component_getUIObjectAtPosition(uiEngineState.mouseUI.x, uiEngineState.mouseUI.y);
    }


    private void updateUI_mouseCursor() {
        /* Update Cursor*/
        if (uiEngineState.lastUIMouseHover != null) {
            // 1. GUI Cursor
            uiEngineState.cursor = uiEngineState.config.ui.cursor;
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

    private boolean updateUI_keyInteractionsKeyProcessKey(TextField focusedTextField) {
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

    private void updateUI_gamepadInteraction() {

    }

    private void updateUI_keyInteractions() {
        uiCommonUtils.setKeyboardInteractedUIObject(null);
        if (uiEngineState.config.ui.keyInteractionsDisabled)
            return;

        // Key
        if (uiEngineState.inputEvents.keyTyped) {
            final TextField focusedTextField = uiEngineState.focusedTextField;
            final boolean processKeyTyped = updateUI_keyInteractionsKeyProcessKey(focusedTextField);

            if (processKeyTyped) {
                // Into Temp variable because focuseTextField can change after executing actions
                for (int ic = 0; ic < uiEngineState.inputEvents.keyTypedCharacters.size; ic++) {
                    char keyTypedCharacter = (char) uiEngineState.inputEvents.keyTypedCharacters.get(ic);
                    uiCommonUtils.textField_typeCharacter(focusedTextField, keyTypedCharacter);
                }
                // MouseTextInput open = focus on last typed character
                if (uiCommonUtils.mouseTextInput_isOpen()) {
                    char typedChar = (char) uiEngineState.inputEvents.keyTypedCharacters.get(uiEngineState.inputEvents.keyTypedCharacters.size - 1);
                    uiCommonUtils.mouseTextInput_selectCharacter(uiEngineState.openMouseTextInput, typedChar);
                }
                uiCommonUtils.setKeyboardInteractedUIObject(focusedTextField);
            }
        }
        if (uiEngineState.inputEvents.keyDown) {
            final TextField focusedTextField = uiEngineState.focusedTextField;
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
                        uiCommonUtils.textField_executeControlKey(focusedTextField, keyDownKeyCode, isAnyShiftPressed());
                    } else if (keyDownKeyCode == KeyCode.Key.V && uiEngineState.inputEvents.keysDown[KeyCode.Key.CONTROL_LEFT]) {
                        // paste
                        String pasteContent = getClipboardContent();
                        if (pasteContent != null) {
                            char[] contentChars = pasteContent.toCharArray();
                            for (char i = 0; i < contentChars.length; i++) {
                                uiCommonUtils.textField_typeCharacter(focusedTextField, contentChars[i]);
                            }
                        }

                    } else if (keyDownKeyCode == KeyCode.Key.C && uiEngineState.inputEvents.keysDown[KeyCode.Key.CONTROL_LEFT]) {
                        // Copy
                        setClipboardContent(uiCommonUtils.textField_getMarkedContent(focusedTextField));
                    } else if (keyDownKeyCode == KeyCode.Key.X && uiEngineState.inputEvents.keysDown[KeyCode.Key.CONTROL_LEFT]) {
                        // Cut
                        setClipboardContent(uiCommonUtils.textField_removeMarkedContent(focusedTextField));
                    } else if (keyDownKeyCode == KeyCode.Key.A && uiEngineState.inputEvents.keysDown[KeyCode.Key.CONTROL_LEFT]) {
                        // Select all
                        uiCommonUtils.textField_setMarkedContent(focusedTextField, 0, focusedTextField.content.length());
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

    private void setClipboardContent(String content) {
        if (content == null) return;
        StringSelection selection = new StringSelection(content);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
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
        if (uiEngineState.config.ui.mouseInteractionsDisabled)
            return;
        // ------ MOUSE DOUBLE CLICK ------
        if (uiEngineState.inputEvents.mouseDoubleClick) {
            final Object lastUIMouseHover = uiEngineState.lastUIMouseHover;
            final boolean processMouseDoubleClick = updateUI_mouseInteractionProcessMouseclick(uiEngineState.lastUIMouseHover);

            if (processMouseDoubleClick) {
                if (lastUIMouseHover instanceof Window window) {
                    for (int ib = 0; ib < uiEngineState.inputEvents.mouseDownButtons.size; ib++) {
                        int mouseDownButton = uiEngineState.inputEvents.mouseDownButtons.get(ib);
                        if (uiEngineState.config.ui.foldWindowsOnDoubleClick && mouseDownButton == Input.Buttons.LEFT) {
                            if (window.hasTitleBar && Tools.Calc.pointRectsCollide(uiEngineState.mouseUI.x, uiEngineState.mouseUI.y, window.x, window.y + TS(window.height - 1), TS(window.width), TS())) {
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
                                    uiCommonUtils.scrollBar_calculateScrolled(scrollBarVertical, uiEngineState.mouseUI.x, uiEngineState.mouseUI.y));
                            uiEngineState.pressedScrollBarVertical = scrollBarVertical;
                        }
                        case ScrollbarHorizontal scrollBarHorizontal -> {
                            uiCommonUtils.scrollBar_pressButton(scrollBarHorizontal);
                            uiCommonUtils.scrollBar_scroll(scrollBarHorizontal,
                                    uiCommonUtils.scrollBar_calculateScrolled(scrollBarHorizontal, uiEngineState.mouseUI.x, uiEngineState.mouseUI.y));
                            uiEngineState.pressedScrollBarHorizontal = scrollBarHorizontal;
                        }
                        case ComboBox comboBox -> {
                            if (uiCommonUtils.comboBox_isOpen(comboBox)) {
                                if (Tools.Calc.pointRectsCollide(uiEngineState.mouseUI.x, uiEngineState.mouseUI.y,
                                        uiCommonUtils.component_getAbsoluteX(comboBox), uiCommonUtils.component_getAbsoluteY(comboBox),
                                        TS(comboBox.width), TS())) {
                                    // Clicked on Combobox itself -> close
                                    uiCommonUtils.comboBox_close(comboBox);
                                } else {
                                    // Clicked on Item
                                    for (int i = 0; i < comboBox.items.size; i++) {
                                        if (Tools.Calc.pointRectsCollide(uiEngineState.mouseUI.x, uiEngineState.mouseUI.y,
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
                                    uiCommonUtils.component_getRelativeMouseX(uiEngineState.mouseUI.x, frameBufferViewport),
                                    uiCommonUtils.component_getRelativeMouseY(uiEngineState.mouseUI.y, frameBufferViewport));
                            uiEngineState.pressedFramebufferViewport = frameBufferViewport;
                        }
                        case AppViewport appViewPort -> {
                            appViewPort.appViewPortAction.onPress(
                                    uiCommonUtils.component_getRelativeMouseX(uiEngineState.mouseUI.x, appViewPort),
                                    uiCommonUtils.component_getRelativeMouseY(uiEngineState.mouseUI.y, appViewPort));
                            uiEngineState.pressedAppViewPort = appViewPort;
                        }
                        case TextField textField -> {
                            int textFieldMouseX = uiCommonUtils.component_getRelativeMouseX(uiEngineState.mouseUI.x, textField);
                            uiEngineState.pressedTextField = textField;

                            int textPosition = uiCommonUtils.textField_findTextPosition(textField, textFieldMouseX);

                            uiEngineState.pressedTextFieldInitCaretPosition = textPosition;
                            uiCommonUtils.textField_setMarkedContent(textField, textPosition, textPosition);
                            uiCommonUtils.textField_setCaretPosition(textField, textPosition);

                            // Set Focus
                            uiCommonUtils.textField_focus(textField, api);


                        }
                        case Grid grid -> {
                            int tileSize = grid.bigMode ? TS2() : TS();
                            int x_grid = uiCommonUtils.component_getAbsoluteX(grid);
                            int y_grid = uiCommonUtils.component_getAbsoluteY(grid);
                            int inv_x = (uiEngineState.mouseUI.x - x_grid) / tileSize;
                            int inv_y = (uiEngineState.mouseUI.y - y_grid) / tileSize;
                            if (uiCommonUtils.grid_positionValid(grid, inv_x, inv_y)) {
                                Object pressedGridItem = grid.items[inv_x][inv_y];
                                if (pressedGridItem != null && grid.dragEnabled) {
                                    uiEngineState.draggedGridFrom.set(inv_x, inv_y);
                                    uiEngineState.draggedGridOffset.set(uiEngineState.mouseUI.x - (x_grid + (inv_x * tileSize)), uiEngineState.mouseUI.y - (y_grid + (inv_y * tileSize)));
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
                                uiEngineState.draggedListOffset.set(uiEngineState.mouseUI.x - (uiCommonUtils.component_getAbsoluteX(list)),
                                        (uiEngineState.mouseUI.y - uiCommonUtils.component_getAbsoluteY(list)) % 8);
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
                        uiEngineState.draggedWindow_offset.x = uiEngineState.mouseUI.x - uiEngineState.draggedWindow.x;
                        uiEngineState.draggedWindow_offset.y = uiEngineState.mouseUI.y - uiEngineState.draggedWindow.y;
                        // Move on top ?
                        uiCommonUtils.window_bringToFront(uiEngineState.draggedWindow);
                    }

                    // Unfocus focused textFields
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
                    case ComboBoxItem comboBoxItem -> {
                        uiCommonUtils.comboBox_selectItem(comboBoxItem);
                        if (uiEngineState.currentControlMode.emulated && comboBoxItem.addedToComboBox != null) {
                            // emulated: move mouse back to combobox on item select
                            uiCommonUtils.emulatedMouse_setPosition(uiEngineState.emulatedMousePosition.x, uiCommonUtils.component_getAbsoluteY(comboBoxItem.addedToComboBox) + TS_HALF());
                        }
                        uiCommonUtils.resetPressedComboBoxItemReference(uiEngineState);
                    }
                    case Checkbox checkBox -> {
                        checkBox.checked = !checkBox.checked;
                        checkBox.checkBoxAction.onCheck(checkBox.checked);
                        uiCommonUtils.resetPressedCheckBoxReference(uiEngineState);
                    }
                    case TextField _ -> {
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

                                        if (grid.selectedItems.contains(uiEngineState.pressedGridItem, true)) {
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

                                        if (list.selectedItems.contains(uiEngineState.pressedListItem, true))
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
                                    dragItem, uiEngineState.mouseUI.x, uiEngineState.mouseUI.y
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
                                    dragItem, dragFromX, dragFromY, uiEngineState.mouseUI.x, uiEngineState.mouseUI.y
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
            if (pressedUIObject == null)
                processMouseDraggedPressed = false;
            // Drag interaction
            Object draggedUIObject = uiCommonUtils.getDraggedUIReference(uiEngineState);
            if (draggedUIObject == null)
                processMouseDraggedDragged = false;

            if (processMouseDraggedPressed) {
                switch (pressedUIObject) {
                    case ScrollbarVertical scrolledScrollBarVertical -> {
                        uiCommonUtils.scrollBar_scroll(scrolledScrollBarVertical, uiCommonUtils.scrollBar_calculateScrolled(scrolledScrollBarVertical, uiEngineState.mouseUI.x, uiEngineState.mouseUI.y));
                    }
                    case ScrollbarHorizontal scrolledScrollBarHorizontal -> {
                        uiCommonUtils.scrollBar_scroll(scrolledScrollBarHorizontal, uiCommonUtils.scrollBar_calculateScrolled(scrolledScrollBarHorizontal, uiEngineState.mouseUI.x, uiEngineState.mouseUI.y));
                    }
                    case Knob turnedKnob -> {
                        final float BASE_SENSITIVITY = 1 / 50f;
                        float amount;
                        if (uiEngineState.currentControlMode.emulated) {
                            // emulated: keep mouse position steady
                            amount = (-uiEngineState.emulatedMouseDirection.y * BASE_SENSITIVITY) * uiEngineState.config.component.knobSensitivity;
                            uiCommonUtils.emulatedMouse_setPositionComponent(turnedKnob);
                        } else {
                            amount = (uiEngineState.mouseDelta.y * BASE_SENSITIVITY) * uiEngineState.config.component.knobSensitivity;
                        }
                        float newValue = turnedKnob.turned + amount;
                        uiCommonUtils.knob_turnKnob(turnedKnob, newValue);
                    }
                    case TextField textField -> {
                        if (uiCommonUtils.textField_isFocused(textField)) {
                            int textFieldMouseX = uiCommonUtils.component_getRelativeMouseX(uiEngineState.mouseUI.x, textField);
                            int textPosition = uiCommonUtils.textField_findTextPosition(textField, textFieldMouseX);

                            uiCommonUtils.textField_setMarkedContent(textField,
                                    Math.min(textPosition, uiEngineState.pressedTextFieldInitCaretPosition),
                                    Math.max(textPosition, uiEngineState.pressedTextFieldInitCaretPosition)
                            );
                            if (textFieldMouseX < 0) {
                                textPosition -= 1;
                            }

                            uiCommonUtils.textField_setCaretPosition(textField, textPosition);
                        }
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
                                uiEngineState.mouseUI.x - uiEngineState.draggedWindow_offset.x,
                                uiEngineState.mouseUI.y - uiEngineState.draggedWindow_offset.y);

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
                        float amount = (BASE_SENSITIVITY * uiEngineState.inputEvents.mouseScrolledAmount) * uiEngineState.config.component.knobSensitivity;
                        float newValue = knob.turned + amount;
                        uiCommonUtils.knob_turnKnob(knob, newValue);
                    }
                    case ScrollbarHorizontal scrollBarHorizontal -> {
                        float amount = (BASE_SENSITIVITY * uiEngineState.inputEvents.mouseScrolledAmount) * uiEngineState.config.component.scrollbarSensitivity;
                        uiCommonUtils.scrollBar_scroll(scrollBarHorizontal, scrollBarHorizontal.scrolled + amount);
                    }
                    case ScrollbarVertical scrollBarVertical -> {
                        float amount = (BASE_SENSITIVITY * uiEngineState.inputEvents.mouseScrolledAmount) * uiEngineState.config.component.scrollbarSensitivity;
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
                uiCommonUtils.textField_executeControlKey(uiEngineState.focusedTextField, uiEngineState.focusedTextField_repeatedKey, isAnyShiftPressed());
                uiEngineState.focusedTextField_repeatedKeyTimer = 0;
            }
        }
    }

    private boolean isAnyShiftPressed() {
        return uiEngineState.inputEvents.keysDown[KeyCode.Key.SHIFT_LEFT] || uiEngineState.inputEvents.keysDown[KeyCode.Key.SHIFT_RIGHT];
    }

    private void updateUI() {

        updateUI_animationTimer();

        if (uiCommonUtils.mouseTextInput_isOpen()) {
            updateUI_updateMouseTextInput();
        } else {
            updateUI_mouseInteractions();
        }

        updateUI_keyInteractions();

        updateUI_continuousComponentActivities();

        updateUI_executeUpdateActions();

        updateUI_notifications();

        updateUI_toolTip();

        updateUI_mouseCursor();
    }

    private void updateUI_animationTimer() {
        if (uiEngineState.config.ui.animationTimerFunction != null) {
            uiEngineState.config.ui.animationTimerFunction.updateAnimationTimer();
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
                int inv_x = (uiEngineState.mouseUI.x - x_grid) / tileSize;
                int inv_y = (uiEngineState.mouseUI.y - y_grid) / tileSize;
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
                uiEngineState.tooltip_delay_timer += uiEngineState.config.tooltip.fadeInDelay;
                if (uiEngineState.tooltip_delay_timer >= uiEngineState.config.tooltip.fadeInDelay) {
                    uiEngineState.tooltip_wait_delay = false;
                    uiEngineState.tooltip_delay_timer = 0;
                    uiEngineState.tooltip_fadePct = 0f;
                    uiEngineState.tooltip.toolTipAction.onDisplay();
                }
            } else if (uiEngineState.tooltip_fadePct < 1f) {
                uiEngineState.tooltip_fadePct = Math.clamp(uiEngineState.tooltip_fadePct + uiEngineState.config.tooltip.fadeInSpeed, 0f, 1f);
            } else {
                uiEngineState.tooltip.toolTipAction.onUpdate();
            }

            uiEngineState.fadeOutTooltip = uiEngineState.tooltip;
        } else {
            if (uiEngineState.fadeOutTooltip != null) {
                if (uiEngineState.tooltip_fadePct > 0f) {
                    uiEngineState.tooltip_fadePct = Math.clamp(uiEngineState.tooltip_fadePct - uiEngineState.config.tooltip.fadeOutSpeed, 0f, 1f);
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
                    if (notification.timer > uiEngineState.config.notification.foldTime) {
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
                        if (tooltipNotification.timer > api.config.notification.toolTipNotificationFadeoutTime) {
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
            case ComboBox comboBox -> comboBox.comboBoxAction;
            case AppViewport appViewPort -> appViewPort.appViewPortAction;
            case Image image -> image.imageAction;
            case Grid grid -> grid.gridAction;
            case List list -> list.listAction;
            case FrameBufferViewport frameBufferViewport -> frameBufferViewport.frameBufferViewportAction;
            case ScrollbarVertical scrollBarVertical -> scrollBarVertical.scrollBarAction;
            case ScrollbarHorizontal scrollBarHorizontal -> scrollBarHorizontal.scrollBarAction;
            case Tabbar tabBar -> tabBar.tabBarAction;
            case Text text -> text.textAction;
            case TextField textField -> textField.textFieldAction;
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
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

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


            this.uiAdapter.renderComposite(uiEngineState.camera_screen,
                    spriteRenderer,
                    uiEngineState.frameBuffer_app.getFlippedTextureRegion(),
                    uiEngineState.frameBufferComponent_ui.getFlippedTextureRegion(), uiEngineState.frameBufferModal_ui.getFlippedTextureRegion(),
                    uiEngineState.resolutionWidth, uiEngineState.resolutionHeight,
                    uiCommonUtils.window_isModalOpen(uiEngineState)
            );

            uiEngineState.frameBuffer_composite.end();
        }

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
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

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
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        //final PrimitiveRenderer primitiveRenderer = uiEngineState.primitiveRenderer_ui;

        spriteRenderer.setProjectionMatrix(uiEngineState.camera_ui.combined);
        spriteRenderer.setBlendFunctionLayer();
        //primitiveRenderer.setProjectionMatrix(uiEngineState.camera_ui.combined);
        //primitiveRenderer.setBlendFunctionLayer();

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

        ////spriteRenderer.setAllReset();
    }

    private void render_mouseTextInput() {
        if (!uiCommonUtils.mouseTextInput_isOpen())
            return;

        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final MouseTextInput mouseTextInput = uiEngineState.openMouseTextInput;
        final Color color1 = mouseTextInput.color;
        final Color color2 = mouseTextInput.color2;
        final Color colorFont = mouseTextInput.fontColor;
        final float textInputAlpha = color1.a;
        final char[] chars = mouseTextInput.upperCase ? mouseTextInput.charactersUC : mouseTextInput.charactersLC;

        // ===== Layout constants =====
        final int CHARS_PER_ROW = uiEngineState.config.mouseTextInput.charsPerRow; // how many characters before wrapping
        final int ROW_HEIGHT = uiEngineState.theme.ts.TS_1_AND_HALF; // vertical spacing between rows
        final int CHAR_SPACING_X = uiEngineState.theme.ts.TS_1_AND_HALF; // horizontal spacing between characters

        final int totalChars = chars.length;

        // ===== Draw all characters =====
        for (int i = 0; i < totalChars; i++) {
            char c = chars[i];

            int col = i % CHARS_PER_ROW;
            int row = i / CHARS_PER_ROW;

            // horizontally center the row on x
            int x = MathUtils.round(mouseTextInput.x + (col - CHARS_PER_ROW / 2f) * CHAR_SPACING_X);
            int y = MathUtils.round(mouseTextInput.y - row * ROW_HEIGHT);

            render_mouseTextInputCharacter(c, x, y, color1, colorFont, textInputAlpha, mouseTextInput.upperCase, i == mouseTextInput.selectedIndex && uiEngineState.mTextInputMouse1Pressed);
        }

        // ===== Selection highlight =====
        int selCol = mouseTextInput.selectedIndex % CHARS_PER_ROW;
        int selRow = mouseTextInput.selectedIndex / CHARS_PER_ROW;
        float selX = mouseTextInput.x + (selCol - CHARS_PER_ROW / 2f) * CHAR_SPACING_X;
        float selY = mouseTextInput.y - selRow * ROW_HEIGHT;

        render_setColor(spriteRenderer, color2, textInputAlpha, false);
        spriteRenderer.drawCMediaImage(uiEngineState.theme.UI_MOUSETEXTINPUT_SELECTED, selX - 2, selY - 2);
        ////spriteRenderer.setAllReset();

    }

    private void render_mouseTextInputCharacter(char c, int x, int y, Color color1, Color colorFont, float textInputAlpha, boolean upperCase, boolean pressed) {
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final int pressedIndex = pressed ? 1 : 0;

        render_setColor(spriteRenderer, color1, textInputAlpha, false);
        spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_MOUSETEXTINPUT_BUTTON, pressedIndex, x, y);

        switch (c) {
            case M_TEXTINPUT_CHAR_CHANGE_CASE, M_TEXTINPUT_CHAR_BACK_, M_TEXTINPUT_CHAR_ACCEPT -> {
                render_setColor(spriteRenderer, colorFont, colorFont.a * color1.a * textInputAlpha, false);
                CMediaImage specialCharacterSprite = switch (c) {
                    case M_TEXTINPUT_CHAR_CHANGE_CASE ->
                            upperCase ? uiEngineState.theme.UI_MOUSETEXTINPUT_UPPERCASE : uiEngineState.theme.UI_MOUSETEXTINPUT_LOWERCASE;
                    case M_TEXTINPUT_CHAR_BACK_ -> uiEngineState.theme.UI_MOUSETEXTINPUT_DELETE;
                    case M_TEXTINPUT_CHAR_ACCEPT -> uiEngineState.theme.UI_MOUSETEXTINPUT_CONFIRM;
                    default -> throw new IllegalStateException("Unexpected value: " + c);
                };
                spriteRenderer.drawCMediaImage(specialCharacterSprite, x, y);
            }
            default -> {
                render_drawFont(String.valueOf(c), x + 2, y + 2, colorFont, textInputAlpha, false);
            }
        }

        ////spriteRenderer.setAllReset();
    }

    private void render_drawCursor() {
        if (uiCommonUtils.mouseTextInput_isOpen())
            return;
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        if (uiEngineState.cursor != null) {
            int center_x = mediaManager.spriteWidth(uiEngineState.cursor) / 2;
            int center_y = mediaManager.spriteHeight(uiEngineState.cursor) / 2;
            spriteRenderer.drawCMediaSprite(uiEngineState.cursor, uiEngineState.cursorArrayIndex, uiCommonUtils.ui_getAnimationTimer(uiEngineState),
                    (uiEngineState.mouseUI.x - center_x), (uiEngineState.mouseUI.y - center_y));
        }
        ////spriteRenderer.setAllReset();
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
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final float componentAlpha = componentAlpha(component);
        final boolean componentGrayScale = componentGrayScale(component);

        render_setColor(spriteRenderer, component.color, componentAlpha, componentGrayScale);

        switch (component) {
            case ComboBox comboBox -> {
                // Menu
                if (uiCommonUtils.comboBox_isOpen(comboBox)) {
                    int widthPx = TS(comboBox.width);
                    for (int i = 0; i < comboBox.items.size; i++) {
                        int itemWidth = mediaManager.fontTextWidth(uiEngineState.config.ui.font, comboBox.items.get(i).text) + 2;
                        if (comboBox.items.get(i).comboBoxItemAction.icon() != null)
                            itemWidth += uiEngineState.theme.ts.TS;
                        widthPx = Math.max(widthPx, itemWidth);
                    }
                    int width = MathUtils.ceil((widthPx) / uiEngineState.theme.ts.TSF);
                    int height = comboBox.items.size;

                    /* Menu */
                    for (int iy = 0; iy < height; iy++) {
                        ComboBoxItem comboBoxItem = comboBox.items.get(iy);
                        boolean selected = Tools.Calc.pointRectsCollide(uiEngineState.mouseUI.x, uiEngineState.mouseUI.y, uiCommonUtils.component_getAbsoluteX(comboBox), uiCommonUtils.component_getAbsoluteY(comboBox) - TS() - TS(iy), widthPx, TS());

                        for (int ix = 0; ix < width; ix++) {
                            int index = render_get9TilesCMediaIndex(ix, iy, width, height);
                            CMediaArray comboBoxCellGraphic = selected ? uiEngineState.theme.UI_COMBO_BOX_LIST_CELL_SELECTED : uiEngineState.theme.UI_COMBO_BOX_LIST_CELL;

                            // Cell
                            ////spriteRenderer.saveState();
                            render_setColor(spriteRenderer, comboBoxItem.comboBoxItemAction.cellColor(), componentAlpha, componentGrayScale);
                            spriteRenderer.drawCMediaArray(comboBoxCellGraphic, index, uiCommonUtils.component_getAbsoluteX(comboBox) + TS(ix), uiCommonUtils.component_getAbsoluteY(comboBox) - TS(iy) - TS());
                            ////spriteRenderer.loadState();

                            // Cell - Underline
                            spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_COMBO_BOX_LIST, index, uiCommonUtils.component_getAbsoluteX(comboBox) + TS(ix), uiCommonUtils.component_getAbsoluteY(comboBox) - TS(iy) - TS());

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
                            spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_COMBO_BOX_TOP, index, uiCommonUtils.component_getAbsoluteX(comboBox) + TS(ix), uiCommonUtils.component_getAbsoluteY(comboBox));
                        }
                    }

                }
            }
            default -> {
            }
        }
        ////spriteRenderer.setAllReset();
    }

    private void render_drawContextMenu() {
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

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
                    boolean selected = Tools.Calc.pointRectsCollide(uiEngineState.mouseUI.x, uiEngineState.mouseUI.y, contextMenu.x, contextMenu.y - TS() - TS(iy), TS(uiEngineState.displayedContextMenuWidth), TS());
                    CMediaArray contextMenuCellGraphic = selected ? uiEngineState.theme.UI_CONTEXT_MENU_CELL_SELECTED : uiEngineState.theme.UI_CONTEXT_MENU_CELL;

                    // Cell
                    ////spriteRenderer.saveState();
                    render_setColor(spriteRenderer, contextMenuItem.contextMenuItemAction.cellColor(), contextMenuAlpha, false);
                    spriteRenderer.drawCMediaArray(contextMenuCellGraphic, index, contextMenu.x + TS(ix), contextMenu.y - TS(iy) - TS());
                    ////spriteRenderer.loadState();

                    // Cell Underline
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_CONTEXT_MENU, index, contextMenu.x + TS(ix), contextMenu.y - TS(iy) - TS());

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
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_CONTEXT_MENU_TOP, index, contextMenu.x + TS(ix), contextMenu.y);
                }
            }


        }


        ////spriteRenderer.setAllReset();
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
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

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
                            spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_TOOLTIP_CELL, render_get16TilesCMediaIndex(tx, y_combined, width_reference, height_reference), x + TS(tx), y + TS(y_combined));
                        }
                    }

                    // Border
                    render_setColor(spriteRenderer, tooltip.color_border, borderAlpha, false);
                    for (int tx = 0; tx < tooltip_width; tx++) {
                        // tooltip border
                        spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_TOOLTIP, render_get16TilesCMediaIndex(tx, y_combined, width_reference, tooltip_height), x + TS(tx), y + TS(y_combined));
                        // segmentborder
                        if (drawBottomborder) {
                            spriteRenderer.drawCMediaImage(uiEngineState.theme.UI_TOOLTIP_SEGMENT_BORDER, x + TS(tx), y + TS(y_combined));
                        }
                    }


                }

                // Top Border
                render_setColor(spriteRenderer, tooltip.color_border, borderAlpha, false);
                for (int tx = 0; tx < tooltip_width; tx++) {
                    // tooltip border
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_TOOLTIP_TOP, render_get3TilesCMediaIndex(tx, width_reference), x + TS(tx), y + TS(tooltip_height));
                }

            }


            // Content
            //spriteRenderer.setColorReset();

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
                        //spriteRenderer.setColorReset();
                    }

                }
                case null, default -> {
                }
            }
        }

        ////spriteRenderer.setAllReset();
    }

    private void render_drawCursorTooltip() {
        final Tooltip tooltip = uiEngineState.fadeOutTooltip != null ? uiEngineState.fadeOutTooltip : uiEngineState.tooltip;
        if (tooltip == null) return;
        if (tooltip.segments.isEmpty()) return;
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final float lineAlpha = tooltip.color_line.a * uiEngineState.tooltip_fadePct;
        final int tooltip_width = tooltipWidth(tooltip);
        if (tooltip_width == 0) return;
        final int tooltip_height = tooltipHeight(tooltip);
        if (tooltip_height == 0) return;


        final int lineLengthAbs = TS(tooltip.lineLength);
        final DIRECTION direction = switch (tooltip.direction) {
            case RIGHT ->
                    uiEngineState.mouseUI.x + lineLengthAbs > uiEngineState.resolutionWidth - TS(tooltip_width) ? DIRECTION.LEFT : DIRECTION.RIGHT;
            case LEFT ->
                    uiEngineState.mouseUI.x - TS(tooltip_width + tooltip.lineLength) < 0 ? DIRECTION.RIGHT : DIRECTION.LEFT;
            case UP ->
                    uiEngineState.mouseUI.y + lineLengthAbs > uiEngineState.resolutionHeight - TS(tooltip_height) ? DIRECTION.DOWN : DIRECTION.UP;
            case DOWN -> uiEngineState.mouseUI.y - TS(tooltip_height) < 0 ? DIRECTION.UP : DIRECTION.DOWN;
            case NONE -> throw new IllegalStateException("Unexpected value: " + tooltip.direction);
        };

        int tooltip_x = switch (direction) {
            case RIGHT ->
                    Math.clamp(uiEngineState.mouseUI.x + lineLengthAbs, 0, Math.max(uiEngineState.resolutionWidth - TS(tooltip_width), 0));
            case LEFT ->
                    Math.clamp(uiEngineState.mouseUI.x - TS(tooltip_width + tooltip.lineLength), 0, Math.max(uiEngineState.resolutionWidth - TS(tooltip_width), 0));
            case UP, DOWN ->
                    Math.clamp(uiEngineState.mouseUI.x - (TS(tooltip_width) / 2), 0, Math.max(uiEngineState.resolutionWidth - TS(tooltip_width), 0));
            case NONE -> throw new IllegalStateException("Unexpected value: " + tooltip.direction);
        };

        int tooltip_y = switch (direction) {
            case RIGHT, LEFT ->
                    Math.clamp(uiEngineState.mouseUI.y - (TS(tooltip_height) / 2), 0, Math.max(uiEngineState.resolutionHeight - TS(tooltip_height) - 1, 0));
            case UP ->
                    Math.clamp(uiEngineState.mouseUI.y + TS(tooltip.lineLength), 0, Math.max(uiEngineState.resolutionHeight - TS(tooltip_height) - 1, 0));
            case DOWN ->
                    Math.clamp(uiEngineState.mouseUI.y - TS(tooltip_height + tooltip.lineLength), 0, Math.max(uiEngineState.resolutionHeight - TS(tooltip_height) - 1, 0));
            case NONE -> throw new IllegalStateException("Unexpected value: " + tooltip.direction);
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
                case NONE -> throw new IllegalStateException("Unexpected value: " + tooltip.direction);
            };
            int yOffset = switch (direction) {
                case LEFT, RIGHT -> 0;
                case UP -> TS(i);
                case DOWN -> -TS(i + 1);
                case NONE -> throw new IllegalStateException("Unexpected value: " + tooltip.direction);
            };
            CMediaImage sprite = switch (direction) {
                case LEFT, RIGHT -> uiEngineState.theme.UI_TOOLTIP_LINE_HORIZONTAL;
                case UP, DOWN -> uiEngineState.theme.UI_TOOLTIP_LINE_VERTICAL;
                case NONE -> throw new IllegalStateException("Unexpected value: " + tooltip.direction);
            };
            spriteRenderer.drawCMediaImage(sprite, uiEngineState.mouseUI.x + xOffset, uiEngineState.mouseUI.y + yOffset);
        }
        switch (direction) {
            case LEFT, RIGHT -> {
                //
            }
            case UP, DOWN -> {
                int yOffset = direction == DIRECTION.UP ? 0 : -TS2();
                spriteRenderer.drawCMediaImage(uiEngineState.theme.UI_TOOLTIP_LINE_VERTICAL, uiEngineState.mouseUI.x, uiEngineState.mouseUI.y + yOffset);
            }
        }
    }

    private void render_drawTooltipNotifications() {
        if (uiEngineState.tooltipNotifications.isEmpty()) return;
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

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
                alpha = (1f - (tooltipNotification.timer / (float) api.config.notification.toolTipNotificationFadeoutTime));
            }

            if (tooltipNotification.tooltip != null) {
                render_drawTooltip(tooltipNotification.x, tooltipNotification.y, tooltipNotification.tooltip, alpha);
            }
        }


        //spriteRenderer.setAllReset();
    }

    private void render_drawTopNotifications() {
        if (uiEngineState.notifications.isEmpty()) return;
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        final int width = (uiEngineState.resolutionWidth % TS() == 0) ? (uiEngineState.resolutionWidth / TS()) : ((uiEngineState.resolutionWidth / TS()) + 1);
        int y = 0;
        int yOffsetSlideFade = 0;
        for (int i = 0; i < uiEngineState.notifications.size; i++) {
            Notification notification = uiEngineState.notifications.get(i);
            final float notificationAlpha = notification.color.a;

            if (notification.state == TOP_NOTIFICATION_STATE.FOLD) {
                float fadeoutProgress = (notification.timer / (float) uiEngineState.config.notification.foldTime);
                yOffsetSlideFade = yOffsetSlideFade + MathUtils.round(TS() * fadeoutProgress);
            }
            //spriteRenderer.saveState();
            render_setColor(spriteRenderer, notification.color, notificationAlpha, false);
            for (int ix = 0; ix < width; ix++) {
                spriteRenderer.drawCMediaImage(uiEngineState.theme.UI_NOTIFICATION_BAR, TS(ix), uiEngineState.resolutionHeight - TS() - TS(y) + yOffsetSlideFade);
            }
            //spriteRenderer.loadState();
            int xOffset = (TS(width) / 2) - (render_textWidth(notification.text) / 2) - notification.scroll;
            render_drawFont(notification.text, xOffset, (uiEngineState.resolutionHeight - TS() - TS(y)) + 1 + yOffsetSlideFade, notification.fontColor, notificationAlpha, false);
            y = y + 1;
        }

        //spriteRenderer.setAllReset();


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

        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final float windowAlpha = window.color.a;

        render_setColor(spriteRenderer, window.color, windowAlpha, false);

        for (int ix = 0; ix < window.width; ix++) {
            if (!window.folded) {
                for (int iy = 0; iy < window.height; iy++) {
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_WINDOW, render_getWindowCMediaIndex(ix, iy, window.width, window.height, window.hasTitleBar), window.x + TS(ix), window.y + TS(iy));
                }
            } else {
                spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_WINDOW, render_getWindowCMediaIndex(ix, (window.height - 1), window.width, window.height, window.hasTitleBar), window.x + TS(ix), window.y + (TS(window.height - 1)));
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


        //spriteRenderer.setAllReset();

    }

    /*private void render_setColor(PrimitiveRenderer primitiveRenderer, Color color, float alpha, boolean grayScale) {
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
    }*/

    private void render_setColor(WgSpriteRenderer spriteRenderer, Color color, float alpha, boolean grayScale) {
        float saturation, lightness;
        if (grayScale) {
            saturation = 0f;
            lightness = 0.45f;
        } else {
            saturation = 0.5f;
            lightness = 0.5f;
        }

        spriteRenderer.setColor(color.r,color.g,color.b, alpha);
        //spriteRenderer.setTweak(0.5f, saturation, lightness, 0.0f);
    }


    private void render_drawComponent(Component component) {
        if (render_isComponentNotRendered(component)) return;
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        //final PrimitiveRenderer primitiveRenderer = uiEngineState.primitiveRenderer_ui;
        final float componentAlpha = componentAlpha(component);
        final boolean componentGrayScale = componentGrayScale(component);

        render_setColor(spriteRenderer, component.color, componentAlpha, componentGrayScale);


        switch (component) {
            case Button button -> {
                CMediaArray buttonGraphic = (button.pressed ? uiEngineState.theme.UI_BUTTON_PRESSED : uiEngineState.theme.UI_BUTTON);
                for (int ix = 0; ix < button.width; ix++) {
                    for (int iy = 0; iy < button.height; iy++) {
                        spriteRenderer.drawCMediaArray(buttonGraphic, render_get16TilesCMediaIndex(ix, iy, button.width, button.height), uiCommonUtils.component_getAbsoluteX(button) + TS(ix), uiCommonUtils.component_getAbsoluteY(button) + TS(iy));
                    }
                }
                if (button instanceof TextButton textButton) {
                    if (textButton.text != null) {
                        render_drawFont(textButton.text, uiCommonUtils.component_getAbsoluteX(textButton) + textButton.contentOffset_x, uiCommonUtils.component_getAbsoluteY(button) + textButton.contentOffset_y,
                                textButton.fontColor, componentAlpha, componentGrayScale, 1, 2, -1,
                                textButton.buttonAction.icon(), textButton.buttonAction.iconIndex(), textButton.buttonAction.iconColor(),
                                textButton.buttonAction.iconFlipX(), textButton.buttonAction.iconFlipY());
                    }
                } else if (button instanceof ImageButton imageButton) {
                    //spriteRenderer.saveState();
                    render_setColor(spriteRenderer, imageButton.color2, componentAlpha, componentGrayScale);
                    if (imageButton.image != null)
                        spriteRenderer.drawCMediaSprite(imageButton.image, imageButton.arrayIndex, uiCommonUtils.ui_getAnimationTimer(uiEngineState), uiCommonUtils.component_getAbsoluteX(imageButton) + imageButton.contentOffset_x, uiCommonUtils.component_getAbsoluteY(imageButton) + imageButton.contentOffset_y);
                    //spriteRenderer.loadState();
                }


            }
            case Image image -> {
                if (image.image != null) {
                    final int srcWidth, srcHeight, width, height;
                    srcWidth = mediaManager.spriteWidth(image.image);
                    srcHeight = mediaManager.spriteHeight(image.image);
                    width = image.stretchToSize ? uiEngineState.theme.ts.abs(image.width) : srcWidth;
                    height = image.stretchToSize ? uiEngineState.theme.ts.abs(image.height) : srcHeight;
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
                    width = frameBufferViewport.stretchToSize ? uiEngineState.theme.ts.abs(frameBufferViewport.width) : srcWidth;
                    height = frameBufferViewport.stretchToSize ? uiEngineState.theme.ts.abs(frameBufferViewport.height) : srcHeight;

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
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_SCROLLBAR_VERTICAL, index, uiCommonUtils.component_getAbsoluteX(scrollBarVertical), uiCommonUtils.component_getAbsoluteY(scrollBarVertical) + TS(i));
                }
                int buttonYOffset = MathUtils.round(scrollBarVertical.scrolled * TS(scrollBarVertical.height - 1));
                //spriteRenderer.saveState();
                render_setColor(spriteRenderer, scrollBarVertical.color2, componentAlpha, componentGrayScale);
                spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_SCROLLBAR_BUTTON_VERTICAL, (scrollBarVertical.buttonPressed ? 1 : 0), uiCommonUtils.component_getAbsoluteX(scrollBarVertical), uiCommonUtils.component_getAbsoluteY(scrollBarVertical) + buttonYOffset);
                //spriteRenderer.loadState();
            }
            case ScrollbarHorizontal scrollBarHorizontal -> {
                for (int i = 0; i < scrollBarHorizontal.width; i++) {
                    int index = (i == 0 ? 0 : (i == (scrollBarHorizontal.width - 1) ? 2 : 1));
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_SCROLLBAR_HORIZONTAL, index, uiCommonUtils.component_getAbsoluteX(scrollBarHorizontal) + TS(i), uiCommonUtils.component_getAbsoluteY(scrollBarHorizontal));
                }
                int buttonXOffset = MathUtils.round(scrollBarHorizontal.scrolled * TS(scrollBarHorizontal.width - 1));
                //spriteRenderer.saveState();
                render_setColor(spriteRenderer, scrollBarHorizontal.color2, componentAlpha, componentGrayScale);
                spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_SCROLLBAR_BUTTON_HORIZONAL, (scrollBarHorizontal.buttonPressed ? 1 : 0), uiCommonUtils.component_getAbsoluteX(scrollBarHorizontal) + buttonXOffset, uiCommonUtils.component_getAbsoluteY(scrollBarHorizontal));
                //spriteRenderer.loadState();
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
                        drag_y = y_list + TS((uiEngineState.mouseUI.y - y_list) / TS());
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

                    boolean selected = item != null && (list.multiSelect ? list.selectedItems.contains(item, true) : (list.selectedItem == item));

                    // Cell
                    //spriteRenderer.saveState();
                    Color cellColor = item != null ? list.listAction.cellColor(item) : list.color2;
                    render_setColor(spriteRenderer, cellColor, componentAlpha, listGrayScale);
                    for (int ix = 0; ix < list.width; ix++) {
                        CMediaImage listSelectedGraphic = selected ? uiEngineState.theme.UI_LIST_CELL_SELECTED : uiEngineState.theme.UI_LIST_CELL;
                        spriteRenderer.drawCMediaImage(listSelectedGraphic, uiCommonUtils.component_getAbsoluteX(list) + TS(ix), uiCommonUtils.component_getAbsoluteY(list) + TS(itemOffsetY));
                    }
                    //spriteRenderer.loadState();

                    // Cell UnderLine
                    for (int ix = 0; ix < list.width; ix++) {
                        spriteRenderer.drawCMediaImage(uiEngineState.theme.UI_LIST, uiCommonUtils.component_getAbsoluteX(list) + TS(ix), uiCommonUtils.component_getAbsoluteY(list) + TS(itemOffsetY));
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
                        spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_LIST_DRAG, render_getListDragCMediaIndex(ix, list.width), drag_x + TS(ix), drag_y);
                    }
                }


            }
            case ComboBox comboBox -> {

                // Cell
                //spriteRenderer.saveState();
                Color cellColor = comboBox.selectedItem != null ? comboBox.selectedItem.comboBoxItemAction.cellColor() : comboBox.color2;
                render_setColor(spriteRenderer, cellColor, componentAlpha, componentGrayScale);
                for (int ix = 0; ix < comboBox.width; ix++) {
                    int index = ix == 0 ? 0 : (ix == comboBox.width - 1 ? 2 : 1);
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_COMBO_BOX_CELL, index, uiCommonUtils.component_getAbsoluteX(comboBox) + TS(ix), uiCommonUtils.component_getAbsoluteY(comboBox));
                }

                // ComboBox
                for (int ix = 0; ix < comboBox.width; ix++) {
                    int index = ix == 0 ? 0 : (ix == comboBox.width - 1 ? 2 : 1);
                    CMediaArray comboMedia = uiCommonUtils.comboBox_isOpen(comboBox) ? uiEngineState.theme.UI_COMBO_BOX_OPEN : uiEngineState.theme.UI_COMBO_BOX;
                    spriteRenderer.drawCMediaArray(comboMedia, index, uiCommonUtils.component_getAbsoluteX(comboBox) + TS(ix), uiCommonUtils.component_getAbsoluteY(comboBox));
                }


                //spriteRenderer.loadState();
                // Cell Content
                if (comboBox.selectedItem != null) {
                    render_drawFont(comboBox.selectedItem.text, uiCommonUtils.component_getAbsoluteX(comboBox), uiCommonUtils.component_getAbsoluteY(comboBox),
                            comboBox.selectedItem.fontColor, componentAlpha, componentGrayScale, 1, 2, TS(comboBox.width - 1),
                            comboBox.selectedItem.comboBoxItemAction.icon(), comboBox.selectedItem.comboBoxItemAction.iconIndex(), comboBox.selectedItem.comboBoxItemAction.iconColor(),
                            comboBox.selectedItem.comboBoxItemAction.iconFlipX(), comboBox.selectedItem.comboBoxItemAction.iconFlipY());
                }
            }
            case Knob knob -> {
                spriteRenderer.drawCMediaImage(uiEngineState.theme.UI_KNOB_BACKGROUND, uiCommonUtils.component_getAbsoluteX(knob), uiCommonUtils.component_getAbsoluteY(knob));
                render_setColor(spriteRenderer, knob.color2, componentAlpha, componentGrayScale);
                if (knob.endless) {
                    int index = MathUtils.round(knob.turned * 31);
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_KNOB_ENDLESS, index, uiCommonUtils.component_getAbsoluteX(knob), uiCommonUtils.component_getAbsoluteY(knob));
                } else {
                    int index = MathUtils.round(knob.turned * 25);
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_KNOB, index, uiCommonUtils.component_getAbsoluteX(knob), uiCommonUtils.component_getAbsoluteY(knob));
                }
            }
            case TextField textField -> {

                for (int ix = 0; ix < textField.width; ix++) {
                    int index = ix == (textField.width - 1) ? 2 : (ix == 0) ? 0 : 1;
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_TEXT_FIELD, index, uiCommonUtils.component_getAbsoluteX(textField) + TS(ix), uiCommonUtils.component_getAbsoluteY(textField));
                }

                //spriteRenderer.saveState();
                render_setColor(spriteRenderer, textField.color2, componentAlpha, componentGrayScale);
                for (int ix = 0; ix < textField.width; ix++) {
                    int index = ix == (textField.width - 1) ? 2 : (ix == 0) ? 0 : 1;
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_TEXT_FIELD_CELL, index, uiCommonUtils.component_getAbsoluteX(textField) + TS(ix), uiCommonUtils.component_getAbsoluteY(textField));
                }

                if (!textField.contentValid) {
                    render_setColor(spriteRenderer, Color.GRAY, componentAlpha, false);
                    for (int ix = 0; ix < textField.width; ix++) {
                        int index = ix == (textField.width - 1) ? 2 : (ix == 0) ? 0 : 1;
                        spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_TEXT_FIELD_CELL_VALIDATION, index, uiCommonUtils.component_getAbsoluteX(textField) + TS(ix), uiCommonUtils.component_getAbsoluteY(textField));
                    }
                }
                //spriteRenderer.loadState();

                if (textField.content != null) {
                    //String contentString = textField.content.substring(textField.offset);


                    // Marker
                    int begin = textField.markedContentBegin;
                    int end = textField.markedContentEnd;
                    if (end > begin) {
                        // Compute in text-space first
                        int drawFrom = render_textWidth(textField.content, 0, begin);
                        int drawTo = render_textWidth(textField.content, 0, end);

                        // Shift by scroll offset (in pixels)
                        int scrollOffsetPx = render_textWidth(textField.content, 0, textField.offset);
                        drawFrom -= scrollOffsetPx;
                        drawTo -= scrollOffsetPx;

                        // Clip to visible text area (not to 0-based text indices)
                        drawFrom = Math.max(drawFrom, 0);
                        drawTo = Math.min(drawTo, TS(textField.width));

                        int drawWidth = drawTo - drawFrom;
                        if (drawWidth > 0) {
                            //spriteRenderer.saveState();
                            render_setColor(spriteRenderer, textField.markerColor, componentAlpha, false);

                            int drawXFrom = uiCommonUtils.component_getAbsoluteX(textField) + drawFrom + 1;
                            drawWidth++;

                            spriteRenderer.drawCMediaImage(
                                    uiEngineState.theme.UI_PIXEL,
                                    drawXFrom,
                                    uiCommonUtils.component_getAbsoluteY(textField) + 1,
                                    drawWidth, 7
                            );

                            //spriteRenderer.loadState();
                        }
                    }


                    // Text
                    render_drawFont(textField.content, uiCommonUtils.component_getAbsoluteX(textField), uiCommonUtils.component_getAbsoluteY(textField),
                            textField.fontColor, componentAlpha, componentGrayScale, 1, 2, TS(textField.width), null, 0, null, false, false, textField.offset, textField.content.length());

                    // Caret
                    if (uiCommonUtils.textField_isFocused(textField)) {
                        int xOffset = render_textWidth(textField.content, textField.offset, textField.caretPosition) + 1;
                        if (xOffset < TS(textField.width)) {
                            spriteRenderer.drawCMediaAnimation(uiEngineState.theme.UI_TEXT_FIELD_CARET, uiCommonUtils.ui_getAnimationTimer(uiEngineState), uiCommonUtils.component_getAbsoluteX(textField) + xOffset, uiCommonUtils.component_getAbsoluteY(textField));
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
                        int m_x = uiEngineState.mouseUI.x - x_grid;
                        int m_y = uiEngineState.mouseUI.y - y_grid;
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
                        CMediaArray gridGraphic = grid.bigMode ? uiEngineState.theme.UI_GRID_X2 : uiEngineState.theme.UI_GRID;
                        boolean selected = grid.multiSelect ? grid.selectedItems.contains(item, true) : item != null && item == grid.selectedItem;
                        if (dragEnabled && dragValid && drag_x == ix && drag_y == iy) {
                            cellGraphic = grid.bigMode ? uiEngineState.theme.UI_GRID_DRAGGED_X2 : uiEngineState.theme.UI_GRID_DRAGGED;
                        } else {
                            if (selected) {
                                cellGraphic = grid.bigMode ? uiEngineState.theme.UI_GRID_CELL_SELECTED_X2 : uiEngineState.theme.UI_GRID_CELL_SELECTED;
                            } else {
                                cellGraphic = grid.bigMode ? uiEngineState.theme.UI_GRID_CELL_X2 : uiEngineState.theme.UI_GRID_CELL;
                            }
                        }

                        // Cell
                        Color cellColor = item != null ? grid.gridAction.cellColor(item) : grid.color2;
                        //spriteRenderer.saveState();
                        render_setColor(spriteRenderer, cellColor, componentAlpha, gridGrayScale);
                        int index = grid.bigMode ? render_get16TilesCMediaIndex(ix, iy, grid.width / 2, grid.height / 2) : render_get16TilesCMediaIndex(ix, iy, grid.width, grid.height);
                        spriteRenderer.drawCMediaArray(cellGraphic, index, uiCommonUtils.component_getAbsoluteX(grid) + (ix * tileSize), uiCommonUtils.component_getAbsoluteY(grid) + (iy * tileSize));
                        //spriteRenderer.loadState();

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

                    boolean tabGrayscale = componentGrayScale || tab.disabled;


                    int tabWidth = tabBar.bigIconMode ? 2 : tab.width;
                    if ((tabXOffset + tabWidth) > tabBar.width) break;

                    boolean selected = i == tabBar.selectedTab;

                    if (tabBar.bigIconMode) {
                        CMediaImage tabGraphic = selected ? uiEngineState.theme.UI_TAB_BIGICON_SELECTED : uiEngineState.theme.UI_TAB_BIGICON;
                        spriteRenderer.drawCMediaImage(tabGraphic, uiCommonUtils.component_getAbsoluteX(tabBar) + TS(tabXOffset), uiCommonUtils.component_getAbsoluteY(tabBar));
                        int selected_offset = selected ? 0 : 1;
                        render_drawIcon(tab.tabAction.icon(), uiCommonUtils.component_getAbsoluteX(tabBar) + TS(tabXOffset) + selected_offset, uiCommonUtils.component_getAbsoluteY(tabBar) - selected_offset,
                                tab.tabAction.iconColor(), componentAlpha, tabGrayscale,
                                tab.tabAction.iconIndex(), true,
                                tab.tabAction.iconFlipX(), tab.tabAction.iconFlipY());
                    } else {
                        CMediaArray tabGraphic = selected ? uiEngineState.theme.UI_TAB_SELECTED : uiEngineState.theme.UI_TAB;
                        for (int ix = 0; ix < tabWidth; ix++) {
                            spriteRenderer.drawCMediaArray(tabGraphic, render_getTabCMediaIndex(ix, tab.width), uiCommonUtils.component_getAbsoluteX(tabBar) + TS(ix) + TS(tabXOffset), uiCommonUtils.component_getAbsoluteY(tabBar));
                        }
                    }

                    if (!tabBar.bigIconMode) {
                        render_drawFont(tab.title, uiCommonUtils.component_getAbsoluteX(tabBar) + TS(tabXOffset), uiCommonUtils.component_getAbsoluteY(tabBar),
                                tab.fontColor, componentAlpha, tabGrayscale, 2, 1, TS(tabWidth),
                                tab.tabAction.icon(), tab.tabAction.iconIndex(), tab.tabAction.iconColor(),
                                tab.tabAction.iconFlipX(), tab.tabAction.iconFlipY());
                    }
                    tabXOffset += tabWidth;


                }

                topBorder = tabBar.width - tabXOffset;

                // Top Border Top
                for (int ix = 0; ix < topBorder; ix++) {
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_TAB_BORDERS, 2, uiCommonUtils.component_getAbsoluteX(tabBar) + TS(tabXOffset + ix), uiCommonUtils.component_getAbsoluteY(tabBar));
                }

                if (tabBar.border) {
                    // Bottom
                    for (int ix = 0; ix < tabBar.width; ix++) {
                        spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_TAB_BORDERS, 2, uiCommonUtils.component_getAbsoluteX(tabBar) + TS(ix), uiCommonUtils.component_getAbsoluteY(tabBar) - TS(tabBar.borderHeight));
                    }
                    // Left/Right
                    for (int iy = 0; iy < tabBar.borderHeight; iy++) {
                        int yOffset = TS(iy + 1);
                        spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_TAB_BORDERS, 0, uiCommonUtils.component_getAbsoluteX(tabBar), uiCommonUtils.component_getAbsoluteY(tabBar) - yOffset);
                        spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_TAB_BORDERS, 1, uiCommonUtils.component_getAbsoluteX(tabBar) + TS(tabBar.width - 1), uiCommonUtils.component_getAbsoluteY(tabBar) - yOffset);
                    }
                }
            }
            case Shape shape -> {
                if (shape.shapeType != null) {
                    /*
                    spriteRenderer.end();
                    primitiveRenderer.begin(GL32.GL_TRIANGLES);
                    render_setColor(primitiveRenderer, shape.color, componentAlpha, componentGrayScale);
                    primitiveRenderer.setVertexColor(shape.color);
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

                     */
                }
            }
            case Progressbar progressBar -> {
                // Background
                for (int ix = 0; ix < progressBar.width; ix++) {
                    int index = ix == 0 ? 0 : ix == (progressBar.width - 1) ? 2 : 1;
                    spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_PROGRESSBAR, index, uiCommonUtils.component_getAbsoluteX(progressBar) + TS(ix), uiCommonUtils.component_getAbsoluteY(progressBar));
                }

                // Bar
                //spriteRenderer.saveState();
                render_setColor(spriteRenderer, progressBar.color2, componentAlpha, componentGrayScale);
                int pixels = MathUtils.round(progressBar.progress * TS(progressBar.width));
                for (int ix = 0; ix < progressBar.width; ix++) {
                    int xOffset = TS(ix);
                    int index = ix == 0 ? 0 : ix == (progressBar.width - 1) ? 2 : 1;
                    if (xOffset < pixels) {
                        if (pixels - xOffset < TS()) {
                            spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_PROGRESSBAR_BAR, index, uiCommonUtils.component_getAbsoluteX(progressBar) + xOffset, uiCommonUtils.component_getAbsoluteY(progressBar), pixels - xOffset, TS());
                        } else {
                            spriteRenderer.drawCMediaArray(uiEngineState.theme.UI_PROGRESSBAR_BAR, index, uiCommonUtils.component_getAbsoluteX(progressBar) + xOffset, uiCommonUtils.component_getAbsoluteY(progressBar));
                        }
                    }
                }
                //spriteRenderer.loadState();

                if (progressBar.progressText) {
                    String percentTxt = progressBar.progressText2Decimal ? uiCommonUtils.progressBar_getProgressText2Decimal(progressBar.progress) : uiCommonUtils.progressBar_getProgressText(progressBar.progress);
                    int xOffset = (TS(progressBar.width) / 2) - (render_textWidth(percentTxt) / 2);
                    render_drawFont(percentTxt, uiCommonUtils.component_getAbsoluteX(progressBar) + xOffset, uiCommonUtils.component_getAbsoluteY(progressBar), progressBar.fontColor, componentAlpha, componentGrayScale, 0, 2);
                }
            }
            case Checkbox checkBox -> {
                CMediaArray checkBoxGraphic = checkBox.checkBoxStyle == CHECKBOX_STYLE.CHECKBOX ? uiEngineState.theme.UI_CHECKBOX_CHECKBOX : uiEngineState.theme.UI_CHECKBOX_RADIO;
                CMediaImage checkBoxCellGraphic = checkBox.checkBoxStyle == CHECKBOX_STYLE.CHECKBOX ? uiEngineState.theme.UI_CHECKBOX_CHECKBOX_CELL : uiEngineState.theme.UI_CHECKBOX_RADIO_CELL;


                //spriteRenderer.saveState();
                render_setColor(spriteRenderer, checkBox.color2, componentAlpha, componentGrayScale);
                spriteRenderer.drawCMediaImage(checkBoxCellGraphic, uiCommonUtils.component_getAbsoluteX(checkBox), uiCommonUtils.component_getAbsoluteY(checkBox));
                //spriteRenderer.loadState();

                spriteRenderer.drawCMediaArray(checkBoxGraphic, (checkBox.checked ? 1 : 0), uiCommonUtils.component_getAbsoluteX(checkBox), uiCommonUtils.component_getAbsoluteY(checkBox));

                render_drawFont(checkBox.text, uiCommonUtils.component_getAbsoluteX(checkBox) + TS(), uiCommonUtils.component_getAbsoluteY(checkBox), checkBox.fontColor, componentAlpha, componentGrayScale, 1, 1);
            }
            case AppViewport appViewPort -> {
                spriteRenderer.draw(appViewPort.textureRegion, uiCommonUtils.component_getAbsoluteX(appViewPort), uiCommonUtils.component_getAbsoluteY(appViewPort));
            }
            default -> {
            }
        }


        //spriteRenderer.setAllReset();

    }

    private void render_drawCursorDragAndDrop() {
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;

        if (uiEngineState.draggedGrid != null) {
            final Grid dragGrid = uiEngineState.draggedGrid;
            final int dragOffsetX = uiEngineState.draggedGridOffset.x;
            final int dragOffsetY = uiEngineState.draggedGridOffset.y;
            final Object dragItem = uiEngineState.draggedGridItem;
            final float dragAlpha = componentAlpha(dragGrid) * uiEngineState.config.component.gridDragAlpha;
            render_drawIcon(dragGrid.gridAction.icon(dragItem), uiEngineState.mouseUI.x - dragOffsetX, uiEngineState.mouseUI.y - dragOffsetY,
                    dragGrid.gridAction.iconColor(dragItem), dragAlpha, false,
                    dragGrid.gridAction.iconIndex(dragItem), dragGrid.bigMode,
                    dragGrid.gridAction.iconFlipX(), dragGrid.gridAction.iconFlipY());
        } else if (uiEngineState.draggedList != null) {
            final List dragList = uiEngineState.draggedList;
            final int dragOffsetX = uiEngineState.draggedListOffset.x;
            final int dragOffsetY = uiEngineState.draggedListOffset.y;
            final Object dragItem = uiEngineState.draggedListItem;
            final float dragAlpha = componentAlpha(dragList) * uiEngineState.config.component.listDragAlpha;
            String text = dragList.listAction.text(dragItem);
            render_drawFont(text, uiEngineState.mouseUI.x - dragOffsetX, uiEngineState.mouseUI.y - dragOffsetY,
                    dragList.fontColor, dragAlpha, false, 2, 1,
                    TS(dragList.width), dragList.listAction.icon(dragItem), dragList.listAction.iconIndex(dragItem), dragList.listAction.iconColor(dragItem),
                    dragList.listAction.iconFlipX(), dragList.listAction.iconFlipY());

        }

        //spriteRenderer.setAllReset();
    }

    private int render_textWidth(String text) {
        return render_textWidth(text, 0, text.length());
    }

    private int render_textWidth(String text, int start, int end) {
        if (text == null || text.length() == 0) return 0;
        return mediaManager.fontTextWidth(uiEngineState.config.ui.font, text, start, end);
    }

    private void render_drawFont(String text, int x, int y, Color color, float alpha, boolean iconGrayScale) {
        render_drawFont(text, x, y, color, alpha, iconGrayScale, 0, 0, FONT_MAX_WIDTH_NONE, null, 0, null, false, false, 0, text.length());
    }

    private void render_drawFont(String text, int x, int y, Color color, float alpha, boolean iconGrayScale, int textXOffset, int textYOffset) {
        render_drawFont(text, x, y, color, alpha, iconGrayScale, textXOffset, textYOffset, FONT_MAX_WIDTH_NONE, null, 0, null, false, false, 0, text.length());
    }

    private void render_drawFont(String text, int x, int y, Color color, float alpha, boolean iconGrayScale, int textXOffset, int textYOffset, int maxWidth) {
        render_drawFont(text, x, y, color, alpha, iconGrayScale, textXOffset, textYOffset, maxWidth, null, 0, null, false, false, 0, text.length());
    }

    private void render_drawFont(String text, int x, int y, float alpha, boolean iconGrayScale, Color color, int textXOffset, int textYOffset, int maxWidth) {
        render_drawFont(text, x, y, color, alpha, iconGrayScale, textXOffset, textYOffset, maxWidth, null, 0, null, false, false, 0, text.length());
    }

    private void render_drawFont(String text, int x, int y, Color color, float alpha, boolean iconGrayScale, int textXOffset, int textYOffset, int maxWidth, CMediaSprite icon, int iconIndex, Color iconColor, boolean iconFlipX, boolean iconFlipY) {
        render_drawFont(text, x, y, color, alpha, iconGrayScale, textXOffset, textYOffset, maxWidth, icon, iconIndex, iconColor, iconFlipX, iconFlipY, 0, text.length());
    }

    private void render_drawFont(String text, int x, int y, Color color, float alpha, boolean iconGrayScale, int textXOffset, int textYOffset, int maxWidth, CMediaSprite icon, int iconIndex, Color iconColor, boolean iconFlipX, boolean iconFlipY, int textOffset, int textLength) {
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        final BitmapFont font = mediaManager.font(uiEngineState.config.ui.font);
        final boolean withIcon = icon != null;
        if (withIcon) {
            render_drawIcon(icon, x, y, iconColor, alpha, iconGrayScale, iconIndex, false, iconFlipX, iconFlipY);
        }

        //spriteRenderer.saveState();
        spriteRenderer.setColor(Color.GRAY.r,Color.GRAY.g,Color.GRAY.r,alpha);
        font.setColor(color.r, color.g, color.b, 1f);

        if (withIcon) maxWidth -= TS();
        spriteRenderer.drawCMediaFont(uiEngineState.config.ui.font, x + (withIcon ? TS() : 0) + textXOffset, y + textYOffset, text, textOffset, textLength, false, false, maxWidth);


        //spriteRenderer.loadState();
    }

    private void render_drawIcon(CMediaSprite icon, int x, int y, Color color, float iconAlpha, boolean iconGrayscale, int arrayIndex, boolean bigMode, boolean flipX, boolean flipY) {
        if (icon == null) return;
        final WgSpriteRenderer spriteRenderer = uiEngineState.spriteRenderer_ui;
        //spriteRenderer.saveState();
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
        //spriteRenderer.loadState();
    }

    @Override
    public void dispose() {
        this.uiAdapter.dispose();

        // Renderers
        uiEngineState.spriteRenderer_ui.dispose();
        //uiEngineState.primitiveRenderer_ui.dispose();

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

    public XWgFrameBuffer getFrameBufferComposite() {
        return uiEngineState.frameBuffer_composite;
    }

    public XWgFrameBuffer getFrameBufferApp() {
        return uiEngineState.frameBuffer_app;
    }

    public XWgFrameBuffer getFrameBufferUIComponent() {
        return uiEngineState.frameBufferComponent_ui;
    }

    public XWgFrameBuffer getFrameBufferUIModal() {
        return uiEngineState.frameBufferModal_ui;
    }


    private int TS(int size) {
        return uiEngineState.theme.ts.abs(size);
    }

    private int TS() {
        return uiEngineState.theme.ts.TS;
    }

    private int TS_HALF() {
        return uiEngineState.theme.ts.TS_HALF;
    }

    private int TS2() {
        return uiEngineState.theme.ts.TS2;
    }


}
