package org.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.mslivo.core.engine.media_manager.media.CMediaCursor;
import org.mslivo.core.engine.ui_engine.gui.Window;
import org.mslivo.core.engine.ui_engine.gui.actions.UpdateAction;
import org.mslivo.core.engine.ui_engine.gui.components.Component;
import org.mslivo.core.engine.ui_engine.gui.components.button.Button;
import org.mslivo.core.engine.ui_engine.gui.components.combobox.ComboBox;
import org.mslivo.core.engine.ui_engine.gui.components.inventory.Inventory;
import org.mslivo.core.engine.ui_engine.gui.components.knob.Knob;
import org.mslivo.core.engine.ui_engine.gui.components.list.List;
import org.mslivo.core.engine.ui_engine.gui.components.map.Map;
import org.mslivo.core.engine.ui_engine.gui.components.scrollbar.ScrollBarHorizontal;
import org.mslivo.core.engine.ui_engine.gui.components.scrollbar.ScrollBarVertical;
import org.mslivo.core.engine.ui_engine.gui.components.textfield.TextField;
import org.mslivo.core.engine.ui_engine.gui.components.viewport.GameViewPort;
import org.mslivo.core.engine.ui_engine.gui.contextmenu.ContextMenu;
import org.mslivo.core.engine.ui_engine.gui.hotkeys.HotKey;
import org.mslivo.core.engine.ui_engine.gui.notification.Notification;
import org.mslivo.core.engine.ui_engine.gui.ostextinput.OnScreenTextInput;
import org.mslivo.core.engine.ui_engine.gui.tool.MouseTool;
import org.mslivo.core.engine.ui_engine.gui.tooltip.ToolTip;
import org.mslivo.core.engine.ui_engine.input.InputEvents;
import org.mslivo.core.engine.ui_engine.input.UIEngineInputProcessor;
import org.mslivo.core.engine.ui_engine.misc.MouseControlMode;
import org.mslivo.core.engine.ui_engine.misc.NestedFrameBuffer;
import org.mslivo.core.engine.ui_engine.misc.ViewportMode;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class InputState {

    /* Parameters */

    public int internalResolutionWidth, internalResolutionHeight;

    public ViewportMode viewportMode;

    public boolean gamePadSupport;


    /* #################### Graphics: Game #################### */
    public SpriteBatch spriteBatch_game;

    public TextureRegion texture_game;

    public OrthographicCamera camera_game;

    public float camera_x, camera_y, camera_z, camera_zoom;

    public int camera_width, camera_height;

    public NestedFrameBuffer frameBuffer_game;


    /* #################### Graphics: GUI #################### */

    public SpriteBatch spriteBatch_gui;
    public TextureRegion texture_gui;

    public OrthographicCamera camera_gui;

    public NestedFrameBuffer frameBuffer_gui;

    /* #################### Graphics: Upscaling #################### */

    public TextureRegion texture_upScale;
    public int factor_upScale;

    public Texture.TextureFilter textureFilter_upScale;

    public NestedFrameBuffer frameBuffer_upScale;


    /* #################### Graphics: Screen #################### */

    public SpriteBatch spriteBatch_screen;

    public Viewport viewport_screen;

    public OrthographicCamera camera_screen;

    /* #################### GUI: Added Elements #################### */

    public ArrayList<Window> windows;

    public ArrayList<Component> screenComponents;

    public Window modalWindow;

    public ArrayDeque<Window> modalWindowQueue;

    public ArrayList<Notification> notifications;

    public ArrayList<HotKey> hotKeys;
    public ArrayList<GameViewPort> gameViewPorts;

    public ArrayList<UpdateAction> singleUpdateActions;

    public ArrayDeque<UpdateAction> singleUpdateActionsRemoveQ;

    /* #################### GUI: Temporary References #################### */
    public Window draggedWindow;
    public GridPoint2 draggedWindow_offset;
    public Button pressedButton;
    public int pressedButton_timer_hold;

    public ScrollBarVertical scrolledScrollBarVertical;

    public ScrollBarHorizontal scrolledScrollBarHorizontal;

    public ToolTip tooltip;

    public float tooltip_fadeIn_pct;

    public boolean tooltip_wait_delay;

    public long tooltip_delay_timer, tooltip_fadeIn_timer;

    public ToolTip gameToolTip;
    public Object tooltip_lastHoverObject;

    public Knob turnedKnob;

    public Map pressedMap;

    public GameViewPort pressedGameViewPort;

    public TextField focusedTextField;

    public Inventory inventoryDrag_Inventory;

    public GridPoint2 inventoryDrag_from;

    public GridPoint2 inventoryDrag_offset;
    public Object inventoryDrag_Item;

    public List listDrag_List;
    public int listDrag_from_index;
    public GridPoint2 listDrag_offset;
    public Object listDrag_Item;

    public ComboBox openComboBox;

    public ContextMenu openContextMenu;

    public int displayedContextMenuWidth;

    public OnScreenTextInput openMouseTextInput;
    public int mTextInputMouseX;
    public boolean mTextInputConfirmPressed, mTextInputChangeCasePressed,mTextInputDeletePressed;
    public boolean mTextInputKeyBoardGamePadLeft;
    public boolean mTextInputKeyBoardGamePadRight;
    public int mTextInputScrollTimer;
    public int mTextInputScrollTime;
    public int mTextInputScrollSpeed;
    public boolean mTextInputTranslatedMouse1Down;

    public boolean mTextInputTranslatedMouse2Down;
    public boolean mTextInputUnlock;
    /* #################### OnScreenTextInput #################### */


    /* #################### Control #################### */
    public Object lastGUIMouseHover; // Last GUI Element the mouse hovered over
    public MouseControlMode currentControlMode;

    public GridPoint2 mouse;

    public GridPoint2 mouse_gui;

    public GridPoint2 mouse_delta;

    public CMediaCursor cursor;

    public MouseTool mouseTool;

    public boolean mouseToolPressed;

    public CMediaCursor overrideCursor;

    public boolean displayOverrideCursor;

    public Vector3 vector_fboCursor;

    public Vector2 vector2_unproject;

    public GridPoint2 simulatedMousePositionBefore;

    public long keyBoardMouseLastMouseClick;
    public float siumlatedMouseSpeedUp;
    public boolean[] keyBoardMouseIsMouseButtonDown;
    public boolean[] keyBoardTranslatedKeysDown;
    public Vector2 gamePadTranslatedStickLeft;
    public Vector2 gamePadTranslatedStickRight;
    public boolean[] gamePadTranslatedButtonsDown;

    /* #################### Misc. ####################  */

    public float animation_timer_gui;

    public Color[] colorStack;

    public int colorStackPointer;

    public ShaderProgram grayScaleShader;

    public OrthographicCamera camera_frustum; // camera for frustum testing

    public InputEvents inputEvents;
    public UIEngineInputProcessor inputProcessor;


    public int itemInfo_listIndex;
    public GridPoint2 itemInfo_inventoryPos;

    public int itemInfo_tabBarTabIndex;
    public boolean itemInfo_listValid, itemInfo_tabBarValid, itemInfo_inventoryValid;



}
