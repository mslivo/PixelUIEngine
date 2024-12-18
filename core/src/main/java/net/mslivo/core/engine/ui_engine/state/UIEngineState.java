package net.mslivo.core.engine.ui_engine.state;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.ui_engine.constants.MOUSE_CONTROL_MODE;
import net.mslivo.core.engine.ui_engine.constants.TILE_SIZE;
import net.mslivo.core.engine.ui_engine.constants.VIEWPORT_MODE;
import net.mslivo.core.engine.ui_engine.rendering.NestedFrameBuffer;
import net.mslivo.core.engine.ui_engine.rendering.PrimitiveRenderer;
import net.mslivo.core.engine.ui_engine.rendering.SpriteRenderer;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.state.input.UIInputEvents;
import net.mslivo.core.engine.ui_engine.state.input.UIInputProcessor;
import net.mslivo.core.engine.ui_engine.ui.Window;
import net.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;
import net.mslivo.core.engine.ui_engine.ui.components.button.Button;
import net.mslivo.core.engine.ui_engine.ui.components.canvas.Canvas;
import net.mslivo.core.engine.ui_engine.ui.components.checkbox.Checkbox;
import net.mslivo.core.engine.ui_engine.ui.components.combobox.Combobox;
import net.mslivo.core.engine.ui_engine.ui.components.combobox.ComboboxItem;
import net.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import net.mslivo.core.engine.ui_engine.ui.components.knob.Knob;
import net.mslivo.core.engine.ui_engine.ui.components.list.List;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollbarHorizontal;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollbarVertical;
import net.mslivo.core.engine.ui_engine.ui.components.textfield.Textfield;
import net.mslivo.core.engine.ui_engine.ui.components.viewport.AppViewport;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.Contextmenu;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenuItem;
import net.mslivo.core.engine.ui_engine.ui.hotkeys.HotKey;
import net.mslivo.core.engine.ui_engine.ui.mousetextinput.MouseTextInput;
import net.mslivo.core.engine.ui_engine.ui.mousetool.MouseTool;
import net.mslivo.core.engine.ui_engine.ui.notification.Notification;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;

import java.util.ArrayDeque;
import java.util.ArrayList;

public final class UIEngineState {

    /* ################ Constructor Parameters ################# */
    public int resolutionWidth, resolutionHeight;
    public int resolutionWidthHalf, resolutionHeightHalf;
    public VIEWPORT_MODE viewportMode;
    public boolean gamePadSupport;
    public TILE_SIZE tileSize;

    /* ##################### Config ########################## */
    public UIConfig config;

    /* #################### Graphics: App #################### */

    public OrthographicCamera camera_app;
    public NestedFrameBuffer frameBuffer_app;

    /* #################### Graphics: GUI #################### */
    public SpriteRenderer spriteRenderer_ui;
    public PrimitiveRenderer primitiveRenderer_ui;
    public OrthographicCamera camera_ui;
    public NestedFrameBuffer frameBuffer_uiComponent;
    public NestedFrameBuffer frameBuffer_uiModal;

    /* #################### Graphics: Screen #################### */
    public int upscaleFactor_screen;
    public Texture.TextureFilter textureFilter_screen;
    public NestedFrameBuffer frameBuffer_screen;
    public Viewport viewport_screen;

    /* #################### UI: Added Elements #################### */

    public ArrayList<Window> windows;
    public ArrayList<Component> screenComponents;
    public Window modalWindow;
    public ArrayDeque<Window> modalWindowQueue;
    public ArrayList<Notification> notifications;
    public ArrayList<HotKey> hotKeys;
    public ArrayList<AppViewport> appViewPorts;
    public ArrayList<UpdateAction> singleUpdateActions;
    public ArrayDeque<UpdateAction> singleUpdateActionsRemoveQueue;

    /* #################### UI: Actively used UI References #################### */
    public Window draggedWindow;
    public GridPoint2 draggedWindow_offset;
    public Button pressedButton;
    public ScrollbarVertical pressedScrollBarVertical;
    public ScrollbarHorizontal pressedScrollBarHorizontal;
    public Tooltip tooltip;
    public Tooltip fadeOutTooltip;
    public float tooltip_fadePct;
    public boolean tooltip_wait_delay;
    public float tooltip_delay_timer;
    public Tooltip appToolTip;
    public Object tooltip_lastHoverObject;
    public Knob pressedKnob;
    public Canvas pressedCanvas;
    public AppViewport pressedAppViewPort;
    public Textfield pressedTextField;
    public int pressedTextFieldMouseX;
    public Textfield focusedTextField;
    public int focusedTextField_repeatedKey;
    public long focusedTextField_repeatedKeyTimer;
    public Grid draggedGrid;
    public Grid pressedGrid;
    public Checkbox pressedCheckBox;
    public Object pressedGridItem;
    public GridPoint2 draggedGridFrom;
    public GridPoint2 draggedGridOffset;
    public Object draggedGridItem;
    public List draggedList;
    public List pressedList;
    public Object pressedListItem;
    public int draggedListFromIndex;
    public GridPoint2 draggedListOffset;
    public Object draggedListItem;
    public Combobox openComboBox;
    public ComboboxItem pressedComboBoxItem;
    public Contextmenu openContextMenu;
    public ContextMenuItem pressedContextMenuItem;
    public int displayedContextMenuWidth;
    public Object keyboardInteractedUIObjectFrame;
    public Object mouseInteractedUIObjectFrame;

    /* #################### MouseTextInput #################### */
    public MouseTextInput openMouseTextInput;
    public int mTextInputMouseX;
    public boolean mTextInputMouse1Pressed, mTextInputMouse2Pressed, mTextInputMouse3Pressed;
    public boolean mTextInputGamePadLeft;
    public boolean mTextInputGamePadRight;
    public int mTextInputScrollTimer;
    public int mTextInputScrollTime;
    public int mTextInputScrollSpeed;
    public boolean mTextInputTranslatedMouse1Down;
    public boolean mTextInputTranslatedMouse2Down;
    public boolean mTextInputTranslatedMouse3Down;
    public boolean mTextInputUnlock;

    /* #################### Control #################### */
    public Object lastUIMouseHover; // Last GUI Element the mouse hovered over
    public MOUSE_CONTROL_MODE currentControlMode;
    public GridPoint2 mouse_app;
    public Vector2 mouse_emulated; // Mouse Position for Keyboard/Gamepad mouse control
    public GridPoint2 mouse_ui;
    public Vector2 mouse_delta;
    public CMediaSprite cursor;
    public int cursorArrayIndex;
    public MouseTool mouseTool;
    public CMediaSprite overrideCursor;
    public int overrideCursorArrayIndex;
    public boolean displayOverrideCursor;
    public Vector3 vector_fboCursor;
    public Vector2 vector2_unproject;
    public boolean[] keyBoardTranslatedKeysDown;
    public Vector2 keyBoardMouseSpeedUp;
    public Vector2 gamePadTranslatedStickLeft;
    public Vector2 gamePadTranslatedStickRight;
    public boolean[] gamePadTranslatedButtonsDown;
    public long emulatedMouseLastMouseClick;
    public boolean[] emulatedMouseIsButtonDown;

    /* #################### Misc. ####################  */

    public UIInputEvents inputEvents;
    public UIInputProcessor inputProcessor;
    public int itemInfo_listIndex;
    public GridPoint2 itemInfo_gridPos;
    public int itemInfo_tabBarTabIndex;
    public boolean itemInfo_listValid, itemInfo_tabBarValid, itemInfo_gridValid;
    public Color tempFontColor;

}
