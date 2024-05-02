package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import net.mslivo.core.engine.ui_engine.enums.MOUSE_CONTROL_MODE;
import net.mslivo.core.engine.ui_engine.enums.VIEWPORT_MODE;
import net.mslivo.core.engine.ui_engine.render.SpriteRenderer;
import net.mslivo.core.engine.ui_engine.render.misc.NestedFrameBuffer;
import net.mslivo.core.engine.ui_engine.ui.Window;
import net.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;
import net.mslivo.core.engine.ui_engine.ui.components.knob.Knob;
import net.mslivo.core.engine.ui_engine.ui.components.canvas.Canvas;
import net.mslivo.core.engine.ui_engine.ui.hotkeys.HotKey;
import net.mslivo.core.engine.ui_engine.ui.tool.MouseTool;
import net.mslivo.core.engine.ui_engine.ui.tooltip.ToolTip;
import net.mslivo.core.engine.media_manager.media.CMediaCursor;
import net.mslivo.core.engine.ui_engine.ui.components.button.Button;
import net.mslivo.core.engine.ui_engine.ui.components.checkbox.CheckBox;
import net.mslivo.core.engine.ui_engine.ui.components.combobox.ComboBox;
import net.mslivo.core.engine.ui_engine.ui.components.combobox.ComboBoxItem;
import net.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import net.mslivo.core.engine.ui_engine.ui.components.list.List;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollBarHorizontal;
import net.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollBarVertical;
import net.mslivo.core.engine.ui_engine.ui.components.textfield.TextField;
import net.mslivo.core.engine.ui_engine.ui.components.viewport.AppViewPort;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenu;
import net.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenuItem;
import net.mslivo.core.engine.ui_engine.ui.notification.Notification;
import net.mslivo.core.engine.ui_engine.ui.ostextinput.MouseTextInput;
import net.mslivo.core.engine.ui_engine.input.InputEvents;
import net.mslivo.core.engine.ui_engine.input.UIEngineInputProcessor;
import net.mslivo.core.engine.ui_engine.config.Config;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class InputState {

    /* ################ Constructor Parameters ################# */
    public int resolutionWidth, resolutionHeight;
    public int resolutionWidth_ui, resolutionHeight_ui;
    public VIEWPORT_MODE viewportMode;
    public boolean gamePadSupport;
    public int uiScale;
    /* ##################### Config ########################## */
    public Config config;

    /* #################### Graphics: App #################### */
    public TextureRegion texture_app;
    public OrthographicCamera camera_app;
    public NestedFrameBuffer frameBuffer_app;

    /* #################### Graphics: GUI #################### */
    public SpriteRenderer spriteRenderer_ui;
    public TextureRegion texture_ui;
    public OrthographicCamera camera_ui;
    public NestedFrameBuffer frameBuffer_ui;

    /* #################### Graphics: Screen #################### */
    public int upscaleFactor_screen;
    public TextureRegion texture_screen;
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
    public ArrayList<AppViewPort> appViewPorts;
    public ArrayList<UpdateAction> singleUpdateActions;
    public ArrayDeque<UpdateAction> singleUpdateActionsRemoveQ;

    /* #################### UI: Actively used UI References #################### */
    public Window draggedWindow;
    public GridPoint2 draggedWindow_offset;
    public Button pressedButton;
    public ScrollBarVertical pressedScrollBarVertical;
    public ScrollBarHorizontal pressedScrollBarHorizontal;
    public ToolTip tooltip;
    public float tooltip_fadeIn_pct;
    public boolean tooltip_wait_delay;
    public long tooltip_delay_timer, tooltip_fadeIn_timer;
    public ToolTip appToolTip;
    public Object tooltip_lastHoverObject;
    public Knob pressedKnob;
    public Canvas pressedCanvas;
    public AppViewPort pressedAppViewPort;
    public TextField pressedTextField;
    public int pressedTextFieldMouseX;
    public TextField focusedTextField;
    public int focusedTextField_repeatedKey;
    public long focusedTextField_repeatedKeyTimer;
    public Grid draggedGrid;
    public Grid pressedGrid;
    public CheckBox pressedCheckBox;
    public Object pressedGridItem;
    public GridPoint2 draggedGridFrom;
    public GridPoint2 draggedGridOffset;
    public Object draggedGridItem;
    public List draggedList;
    public List pressedList;
    public Object pressedListItem;
    public int draggedListFromIndex;
    public GridPoint2 draggedListOffsetX;
    public Object draggedListItem;
    public ComboBox openComboBox;
    public ComboBoxItem pressedComboBoxItem;
    public ContextMenu openContextMenu;
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
    public IntArray mTextInputAPICharacterQueue;

    /* #################### Control #################### */
    public Object lastUIMouseHover; // Last GUI Element the mouse hovered over
    public MOUSE_CONTROL_MODE currentControlMode;
    public GridPoint2 mouse_app;
    public Vector2 mouse_emulated; // Mouse Position for Keyboard/Gamepad mouse control
    public GridPoint2 mouse_ui;
    public Vector2 mouse_delta;
    public CMediaCursor cursor;
    public MouseTool mouseTool;
    public boolean mouseToolPressed;
    public CMediaCursor overrideCursor;
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

    public float animation_timer_ui;
    public Color tempSaveColor;
    public InputEvents inputEvents;
    public UIEngineInputProcessor inputProcessor;
    public int itemInfo_listIndex;
    public GridPoint2 itemInfo_gridPos;
    public int itemInfo_tabBarTabIndex;
    public boolean itemInfo_listValid, itemInfo_tabBarValid, itemInfo_gridValid;

}
