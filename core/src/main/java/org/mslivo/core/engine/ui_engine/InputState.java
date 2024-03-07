package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.mslivo.core.engine.media_manager.media.CMediaCursor;
import org.mslivo.core.engine.ui_engine.render.ImmediateBatch;
import org.mslivo.core.engine.ui_engine.ui.Window;
import org.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;
import org.mslivo.core.engine.ui_engine.ui.components.button.Button;
import org.mslivo.core.engine.ui_engine.ui.components.checkbox.CheckBox;
import org.mslivo.core.engine.ui_engine.ui.components.combobox.ComboBox;
import org.mslivo.core.engine.ui_engine.ui.components.combobox.ComboBoxItem;
import org.mslivo.core.engine.ui_engine.ui.components.grid.Grid;
import org.mslivo.core.engine.ui_engine.ui.components.knob.Knob;
import org.mslivo.core.engine.ui_engine.ui.components.list.List;
import org.mslivo.core.engine.ui_engine.ui.components.map.Canvas;
import org.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollBarHorizontal;
import org.mslivo.core.engine.ui_engine.ui.components.scrollbar.ScrollBarVertical;
import org.mslivo.core.engine.ui_engine.ui.components.textfield.TextField;
import org.mslivo.core.engine.ui_engine.ui.components.viewport.GameViewPort;
import org.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenu;
import org.mslivo.core.engine.ui_engine.ui.contextmenu.ContextMenuItem;
import org.mslivo.core.engine.ui_engine.ui.hotkeys.HotKey;
import org.mslivo.core.engine.ui_engine.ui.notification.Notification;
import org.mslivo.core.engine.ui_engine.ui.ostextinput.MouseTextInput;
import org.mslivo.core.engine.ui_engine.ui.tool.MouseTool;
import org.mslivo.core.engine.ui_engine.ui.tooltip.ToolTip;
import org.mslivo.core.engine.ui_engine.input.InputEvents;
import org.mslivo.core.engine.ui_engine.input.UIEngineInputProcessor;
import org.mslivo.core.engine.ui_engine.config.Config;
import org.mslivo.core.engine.ui_engine.enums.MOUSE_CONTROL_MODE;
import org.mslivo.core.engine.ui_engine.render.NestedFrameBuffer;
import org.mslivo.core.engine.ui_engine.enums.VIEWPORT_MODE;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class InputState {

    /* ################ Constructor Parameters ################# */
    public int internalResolutionWidth, internalResolutionHeight;
    public VIEWPORT_MODE viewportMode;
    public boolean spriteRenderer;
    public boolean immediateRenderer;
    public boolean gamePadSupport;
    public Config config;

    /* #################### Graphics: Game #################### */
    public SpriteBatch spriteBatch_game;
    public ImmediateBatch immediateBatch_game;
    public TextureRegion texture_game;
    public OrthographicCamera camera_game;
    public NestedFrameBuffer frameBuffer_game;

    /* #################### Graphics: GUI #################### */
    public SpriteBatch spriteBatch_ui;
    public ImmediateBatch immediateBatch_ui;
    public TextureRegion texture_ui;
    public OrthographicCamera camera_ui;
    public NestedFrameBuffer frameBuffer_ui;

    /* #################### Graphics: Screen #################### */
    public int upscaleFactor_screen;
    public TextureRegion texture_screen;
    public Texture.TextureFilter textureFilter_screen;
    public NestedFrameBuffer frameBuffer_screen;
    public SpriteBatch spriteBatch_screen;
    public Viewport viewport_screen;
    public OrthographicCamera camera_screen;

    /* #################### UI: Added Elements #################### */

    public ArrayList<Window> windows;
    public ArrayList<Component> screenComponents;
    public Window modalWindow;
    public ArrayDeque<Window> modalWindowQueue;
    public ArrayList<Notification> notifications;
    public ArrayList<HotKey> hotKeys;
    public ArrayList<GameViewPort> gameViewPorts;
    public ArrayList<UpdateAction> singleUpdateActions;
    public ArrayDeque<UpdateAction> singleUpdateActionsRemoveQ;

    /* #################### UI: Actively used UI References #################### */
    public Window draggedWindow;
    public GridPoint2 draggedWindow_offset;
    public Button pressedButton;
    public ScrollBarVertical scrolledScrollBarVertical;
    public ScrollBarHorizontal scrolledScrollBarHorizontal;
    public ToolTip tooltip;
    public float tooltip_fadeIn_pct;
    public boolean tooltip_wait_delay;
    public long tooltip_delay_timer, tooltip_fadeIn_timer;
    public ToolTip gameToolTip;
    public Object tooltip_lastHoverObject;
    public Knob turnedKnob;
    public Canvas pressedCanvas;
    public GameViewPort pressedGameViewPort;
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
    public GridPoint2 mouse_game;
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
    public ShaderProgram grayScaleShader;
    public OrthographicCamera camera_frustum; // camera for frustum testing
    public InputEvents inputEvents;
    public UIEngineInputProcessor inputProcessor;
    public int itemInfo_listIndex;
    public GridPoint2 itemInfo_gridPos;
    public int itemInfo_tabBarTabIndex;
    public boolean itemInfo_listValid, itemInfo_tabBarValid, itemInfo_gridValid;

}
