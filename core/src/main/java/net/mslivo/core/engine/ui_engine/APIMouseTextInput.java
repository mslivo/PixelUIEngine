package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntArray;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.ui.actions.MouseTextInputAction;
import net.mslivo.core.engine.ui_engine.ui.mousetextinput.MouseTextInput;

public final class APIMouseTextInput {
    private final API api;
    private final UIEngineState uiEngineState;
    private final MediaManager mediaManager;
    private final UIConfig uiConfig;

    APIMouseTextInput(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.uiConfig = uiEngineState.config;
    }

    public final MouseTextInputAction DEFAULT_MOUSE_TEXTINPUT_ACTION =  new MouseTextInputAction() {};

    public MouseTextInput create(int x, int y) {
        return create(x, y, DEFAULT_MOUSE_TEXTINPUT_ACTION,
                null,
                uiConfig.mouseTextInput_defaultLowerCaseCharacters,
                uiConfig.mouseTextInput_defaultUpperCaseCharacters);
    }

    public MouseTextInput create(int x, int y, MouseTextInputAction mouseTextInputAction) {
        return create(x, y, mouseTextInputAction,
                null,
                uiConfig.mouseTextInput_defaultLowerCaseCharacters,
                uiConfig.mouseTextInput_defaultUpperCaseCharacters);
    }

    public MouseTextInput create(int x, int y, MouseTextInputAction onConfirm, Character selectedCharacter) {
        return create(x, y, onConfirm,
                selectedCharacter,
                uiConfig.mouseTextInput_defaultLowerCaseCharacters,
                uiConfig.mouseTextInput_defaultUpperCaseCharacters
        );
    }

    public MouseTextInput create(int x, int y, MouseTextInputAction mouseTextInputAction, Character selectedCharacter, char[] charactersLC, char[] charactersUC) {
        charactersLC = charactersLC != null ? charactersLC : new char[]{};
        charactersUC = charactersUC != null ? charactersUC : new char[]{};

        MouseTextInput mouseTextInput = new MouseTextInput();
        mouseTextInput.color = new Color(uiConfig.mouseTextInput_defaultColor);
        mouseTextInput.color2 = new Color(uiConfig.mouseTextInput_defaultColor).mul(0.5f);
        mouseTextInput.fontColor = uiConfig.ui_font_defaultColor.cpy();
        mouseTextInput.x = x - 6;
        mouseTextInput.y = y - 12;
        mouseTextInput.mouseTextInputAction = mouseTextInputAction != null ? mouseTextInputAction : DEFAULT_MOUSE_TEXTINPUT_ACTION;
        mouseTextInput.upperCase = false;
        mouseTextInput.selectedIndex = 0;
        mouseTextInput.enterCharacterQueue = new IntArray();
        int maxCharacters = Math.min(charactersLC.length, charactersUC.length);
        mouseTextInput.charactersLC = new char[maxCharacters + 3];
        mouseTextInput.charactersUC = new char[maxCharacters + 3];
        for (int i = 0; i < maxCharacters; i++) {
            mouseTextInput.charactersLC[i] = charactersLC[i];
            mouseTextInput.charactersUC[i] = charactersUC[i];
            if (selectedCharacter != null && (mouseTextInput.charactersLC[i] == selectedCharacter || mouseTextInput.charactersUC[i] == selectedCharacter)) {
                mouseTextInput.selectedIndex = i;
                mouseTextInput.upperCase = mouseTextInput.charactersUC[i] == selectedCharacter;
            }
        }
        mouseTextInput.charactersLC[maxCharacters] = mouseTextInput.charactersUC[maxCharacters] = '\t';
        mouseTextInput.charactersLC[maxCharacters + 1] = mouseTextInput.charactersUC[maxCharacters + 1] = '\b';
        mouseTextInput.charactersLC[maxCharacters + 2] = mouseTextInput.charactersUC[maxCharacters + 2] = '\n';
        return mouseTextInput;
    }

    public boolean isUpperCase(MouseTextInput mouseTextInput) {
        if (mouseTextInput == null) return false;
        return mouseTextInput.upperCase;
    }

    public void enterChangeCase(MouseTextInput mouseTextInput) {
        enterChangeCase(mouseTextInput, !mouseTextInput.upperCase);
    }

    public void enterChangeCase(MouseTextInput mouseTextInput, boolean upperCase) {
        if (mouseTextInput == null) return;
        if (mouseTextInput.upperCase != upperCase) {
            enterCharacter(mouseTextInput,'\t');
        }
    }

    public void enterDelete(MouseTextInput mouseTextInput) {
        if (mouseTextInput == null) return;
        enterCharacter(mouseTextInput,'\b');
    }

    public void enterConfirm(MouseTextInput mouseTextInput) {
        if (mouseTextInput == null) return;
        enterCharacter(mouseTextInput,'\n');
    }

    public void enterCharacters(MouseTextInput mouseTextInput, String text) {
        if (mouseTextInput == null) return;
        char[] characters = text.toCharArray();
        for (int i = 0; i < characters.length; i++) enterCharacter(mouseTextInput, characters[i]);
    }

    public void enterCharacter(MouseTextInput mouseTextInput, char character) {
        if (mouseTextInput == null) return;
        mouseTextInput.enterCharacterQueue.add(character);
    }

    public void selectCharacter(MouseTextInput mouseTextInput, char character) {
        if (mouseTextInput == null) return;
        UICommonUtils.mouseTextInput_selectCharacter(mouseTextInput, character);
    }

    public void selectIndex(MouseTextInput mouseTextInput, int index) {
        if (mouseTextInput == null) return;
        UICommonUtils.mouseTextInput_selectIndex(mouseTextInput, index);
    }

    public void setCharacters(MouseTextInput mouseTextInput, char[] charactersLC, char[] charactersUC) {
        if (mouseTextInput == null) return;
        charactersLC = charactersLC != null ? charactersLC : new char[]{};
        charactersUC = charactersUC != null ? charactersUC : new char[]{};
        UICommonUtils.mouseTextInput_setCharacters(mouseTextInput, charactersLC, charactersUC);
    }

    public void setAlpha(MouseTextInput mouseTextInput, float alpha) {
        if (mouseTextInput == null) return;
        Color color = mouseTextInput.color;
        mouseTextInput.color.set(color.r, color.g, color.b, alpha);
    }

    public void setColor(MouseTextInput mouseTextInput, Color color) {
        if (mouseTextInput == null) return;
        mouseTextInput.color.set(color);
    }

    public void setColor2(MouseTextInput mouseTextInput, Color color2) {
        if (mouseTextInput == null) return;
        mouseTextInput.color2.set(color2);
    }

    public void setPosition(MouseTextInput mouseTextInput, int x, int y) {
        if (mouseTextInput == null) return;
        mouseTextInput.x = x - 6;
        mouseTextInput.y = y - 12;
    }

    public void setMouseTextInputAction(MouseTextInput mouseTextInput, MouseTextInputAction mouseTextInputAction) {
        if (mouseTextInput == null) return;
        mouseTextInput.mouseTextInputAction = mouseTextInputAction != null ? mouseTextInputAction : DEFAULT_MOUSE_TEXTINPUT_ACTION;
    }

    public void setFontColor(MouseTextInput mouseTextInput, Color color) {
        if (mouseTextInput == null) return;
        mouseTextInput.fontColor.set(color);
    }


}