package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import net.mslivo.core.engine.media_manager.CMediaFont;
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

    private MouseTextInputAction defaultMouseTextInputConfirmAction() {
        return new MouseTextInputAction() {
        };
    }

    public void open(int x, int y) {
        open(x, y, defaultMouseTextInputConfirmAction(),
                null,
                uiConfig.mouseTextInput_defaultLowerCaseCharacters,
                uiConfig.mouseTextInput_defaultUpperCaseCharacters);
    }

    public void open(int x, int y, MouseTextInputAction onConfirm) {
        open(x, y, onConfirm,
                null,
                uiConfig.mouseTextInput_defaultLowerCaseCharacters,
                uiConfig.mouseTextInput_defaultUpperCaseCharacters);
    }

    public void open(int x, int y, MouseTextInputAction onConfirm, Character selectedCharacter) {
        open(x, y, onConfirm,
                selectedCharacter,
                uiConfig.mouseTextInput_defaultLowerCaseCharacters,
                uiConfig.mouseTextInput_defaultUpperCaseCharacters);
    }

    public void open(int x, int y, MouseTextInputAction mouseTextInputAction, Character selectedCharacter, char[] charactersLC, char[] charactersUC) {
        if (charactersLC == null || charactersUC == null) return;
        if (uiEngineState.openMouseTextInput != null) return;
        MouseTextInput mouseTextInput = new MouseTextInput();
        mouseTextInput.color = new Color(uiConfig.mouseTextInput_defaultColor);
        mouseTextInput.color2 = new Color(0.25f,0.25f,0.25f,1.0f);
        mouseTextInput.font = uiConfig.mouseTextInput_defaultFont;
        mouseTextInput.x = x - 6;
        mouseTextInput.y = y - 12;
        mouseTextInput.mouseTextInputAction = mouseTextInputAction;
        mouseTextInput.upperCase = false;
        mouseTextInput.selectedIndex = 0;
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
        uiEngineState.mTextInputMouseX = Gdx.input.getX();
        uiEngineState.mTextInputUnlock = false;
        uiEngineState.openMouseTextInput = mouseTextInput;
    }

    public void close() {
        UICommonUtils.mouseTextInput_close(uiEngineState);
    }

    public boolean isUpperCase() {
        if (uiEngineState.openMouseTextInput == null) return false;
        return uiEngineState.openMouseTextInput.upperCase;
    }

    public void enterChangeCase() {
        enterChangeCase(!uiEngineState.openMouseTextInput.upperCase);
    }

    public void enterChangeCase(boolean upperCase) {
        if (uiEngineState.openMouseTextInput == null) return;
        if (uiEngineState.openMouseTextInput.upperCase != upperCase) {
            enterCharacter('\t');
        }
    }

    public void enterDelete() {
        if (uiEngineState.openMouseTextInput == null) return;
        enterCharacter('\b');
    }

    public void enterConfirm() {
        if (uiEngineState.openMouseTextInput == null) return;
        enterCharacter('\n');
    }

    public void enterCharacters(String text) {
        if (uiEngineState.openMouseTextInput == null) return;
        char[] characters = text.toCharArray();
        for (int i = 0; i < characters.length; i++) enterCharacter(characters[i]);
    }

    public void enterCharacter(char character) {
        if (uiEngineState.openMouseTextInput == null) return;
        uiEngineState.mTextInputAPICharacterQueue.add(character);
    }

    public void selectCharacter(char character) {
        if (uiEngineState.openMouseTextInput == null) return;
        UICommonUtils.mouseTextInput_selectCharacter(uiEngineState.openMouseTextInput, character);
    }

    public void selectIndex(int index) {
        if (uiEngineState.openMouseTextInput == null) return;
        UICommonUtils.mouseTextInput_selectIndex(uiEngineState.openMouseTextInput, index);
    }

    public void setCharacters(char[] charactersLC, char[] charactersUC) {
        if (uiEngineState.openMouseTextInput == null) return;
        if (charactersLC == null || charactersUC == null) return;
        UICommonUtils.mouseTextInput_setCharacters(uiEngineState.openMouseTextInput, charactersLC, charactersUC);
    }

    public void setAlpha(float alpha) {
        if (uiEngineState.openMouseTextInput == null) return;
        Color color = uiEngineState.openMouseTextInput.color;
        uiEngineState.openMouseTextInput.color.set(color.r, color.g, color.b, alpha);
    }

    public void setColor(Color color) {
        if (uiEngineState.openMouseTextInput == null) return;
        uiEngineState.openMouseTextInput.color.set(color);
    }

    public void setColor2(Color color2) {
        if (uiEngineState.openMouseTextInput == null) return;
        uiEngineState.openMouseTextInput.color2.set(color2);
    }

    public void setPosition(int x, int y) {
        if (uiEngineState.openMouseTextInput == null) return;
        uiEngineState.openMouseTextInput.x = x - 6;
        uiEngineState.openMouseTextInput.y = y - 12;
    }

    public void setMouseTextInputAction(MouseTextInputAction mouseTextInputAction) {
        if (uiEngineState.openMouseTextInput == null) return;
        uiEngineState.openMouseTextInput.mouseTextInputAction = mouseTextInputAction;
    }

    public void setFont(CMediaFont font) {
        if (uiEngineState.openMouseTextInput == null) return;
        uiEngineState.openMouseTextInput.font = font;
    }


}