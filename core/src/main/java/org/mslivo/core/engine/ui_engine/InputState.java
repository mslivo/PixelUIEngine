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
import org.mslivo.core.engine.ui_engine.gui.Window;
import org.mslivo.core.engine.ui_engine.gui.actions.UpdateAction;
import org.mslivo.core.engine.ui_engine.gui.components.Component;
import org.mslivo.core.engine.ui_engine.gui.components.button.Button;
import org.mslivo.core.engine.ui_engine.gui.components.checkbox.CheckBox;
import org.mslivo.core.engine.ui_engine.gui.components.combobox.ComboBox;
import org.mslivo.core.engine.ui_engine.gui.components.combobox.ComboBoxItem;
import org.mslivo.core.engine.ui_engine.gui.components.inventory.Inventory;
import org.mslivo.core.engine.ui_engine.gui.components.knob.Knob;
import org.mslivo.core.engine.ui_engine.gui.components.list.List;
import org.mslivo.core.engine.ui_engine.gui.components.map.Map;
import org.mslivo.core.engine.ui_engine.gui.components.scrollbar.ScrollBarHorizontal;
import org.mslivo.core.engine.ui_engine.gui.components.scrollbar.ScrollBarVertical;
import org.mslivo.core.engine.ui_engine.gui.components.textfield.TextField;
import org.mslivo.core.engine.ui_engine.gui.components.viewport.GameViewPort;
import org.mslivo.core.engine.ui_engine.gui.contextmenu.ContextMenu;
import org.mslivo.core.engine.ui_engine.gui.contextmenu.ContextMenuItem;
import org.mslivo.core.engine.ui_engine.gui.hotkeys.HotKey;
import org.mslivo.core.engine.ui_engine.gui.notification.Notification;
import org.mslivo.core.engine.ui_engine.gui.ostextinput.MouseTextInput;
import org.mslivo.core.engine.ui_engine.gui.tool.MouseTool;
import org.mslivo.core.engine.ui_engine.gui.tooltip.ToolTip;
import org.mslivo.core.engine.ui_engine.input.InputEvents;
import org.mslivo.core.engine.ui_engine.input.UIEngineInputProcessor;
import org.mslivo.core.engine.ui_engine.misc.config.Config;
import org.mslivo.core.engine.ui_engine.misc.enums.MOUSE_CONTROL_MODE;
import org.mslivo.core.engine.ui_engine.misc.render.ImmediateRenderer;
import org.mslivo.core.engine.ui_engine.misc.render.NestedFrameBuffer;
import org.mslivo.core.engine.ui_engine.misc.enums.VIEWPORT_MODE;

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
    public ImmediateRenderer imRenderer_game;
    public TextureRegion texture_game;
    public OrthographicCamera camera_game;
    public NestedFrameBuffer frameBuffer_game;

    /* #################### Graphics: GUI #################### */
    public SpriteBatch spriteBatch_gui;
    public ImmediateRenderer imRenderer_gui;
    public TextureRegion texture_gui;
    public OrthographicCamera camera_gui;
    public NestedFrameBuffer frameBuffer_gui;

    /* #################### Graphics: Screen #################### */
    public int upscaleFactor_screen;
    public TextureRegion texture_screen;
    public Texture.TextureFilter textureFilter_screen;
    public NestedFrameBuffer frameBuffer_screen;
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

    /* #################### GUI: Actively used UI References #################### */
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
    public TextField pressedTextField;
    public int pressedTextFieldMouseX;
    public TextField focusedTextField;
    public Inventory draggedInventory;
    public Inventory pressedInventory;
    public CheckBox pressedCheckBox;
    public Object pressedInventoryItem;
    public GridPoint2 draggedInventoryFrom;
    public GridPoint2 draggedInventoryOffset;
    public Object draggedInventoryItem;
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
    public boolean mTextInputConfirmPressed, mTextInputChangeCasePressed,mTextInputDeletePressed;
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
    public Object lastGUIMouseHover; // Last GUI Element the mouse hovered over
    public MOUSE_CONTROL_MODE currentControlMode;
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
    public boolean[] keyBoardTranslatedKeysDown;
    public Vector2 keyBoardMouseSpeedUp;
    public Vector2 gamePadTranslatedStickLeft;
    public Vector2 gamePadTranslatedStickRight;
    public boolean[] gamePadTranslatedButtonsDown;
    public long simulatedMouseLastMouseClick;
    public boolean[] simulatedMouseIsButtonDown;
    public Vector2 simulatedMouseGUIPosition;

    /* #################### Misc. ####################  */

    public float animation_timer_gui;
    public Color tempSaveColor;
    public ShaderProgram grayScaleShader;
    public OrthographicCamera camera_frustum; // camera for frustum testing
    public InputEvents inputEvents;
    public UIEngineInputProcessor inputProcessor;
    public int itemInfo_listIndex;
    public GridPoint2 itemInfo_inventoryPos;
    public int itemInfo_tabBarTabIndex;
    public boolean itemInfo_listValid, itemInfo_tabBarValid, itemInfo_inventoryValid;

}
