package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntArray;
import net.mslivo.pixelui.engine.actions.MouseTextInputAction;
import net.mslivo.pixelui.media.MediaManager;

import java.util.ArrayList;

public final class APIMouseTextInput {
    private final API api;
    private final UIEngineState uiEngineState;
    private final UICommonUtils uiCommonUtils;
    private final MediaManager mediaManager;
    private final UIEngineConfig uiEngineConfig;


    APIMouseTextInput(API api, UIEngineState uiEngineState, UICommonUtils uiCommonUtils, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.uiCommonUtils = uiCommonUtils;
        this.mediaManager = mediaManager;
        this.uiEngineConfig = uiEngineState.config;
    }

    public final MouseTextInputAction DEFAULT_MOUSE_TEXTINPUT_ACTION = new MouseTextInputAction() {
    };

    public MouseTextInput create(int x, int y) {
        return create(x, y, DEFAULT_MOUSE_TEXTINPUT_ACTION,
                null,
                uiEngineConfig.mouseTextInput.defaultLowerCaseCharacters,
                uiEngineConfig.mouseTextInput.defaultUpperCaseCharacters);
    }

    public MouseTextInput create(int x, int y, MouseTextInputAction mouseTextInputAction) {
        return create(x, y, mouseTextInputAction,
                null,
                uiEngineConfig.mouseTextInput.defaultLowerCaseCharacters,
                uiEngineConfig.mouseTextInput.defaultUpperCaseCharacters);
    }

    public MouseTextInput create(int x, int y, MouseTextInputAction onConfirm, Character selectedCharacter) {
        return create(x, y, onConfirm,
                selectedCharacter,
                uiEngineConfig.mouseTextInput.defaultLowerCaseCharacters,
                uiEngineConfig.mouseTextInput.defaultUpperCaseCharacters
        );
    }

    public MouseTextInput create(int x, int y, MouseTextInputAction mouseTextInputAction, Character selectedCharacter, char[] charactersLC, char[] charactersUC) {
        charactersLC = charactersLC != null ? charactersLC : new char[]{};
        charactersUC = charactersUC != null ? charactersUC : new char[]{};

        MouseTextInput mouseTextInput = new MouseTextInput();
        mouseTextInput.color = new Color(uiEngineConfig.mouseTextInput.defaultColor);
        mouseTextInput.color2 = new Color(uiEngineConfig.mouseTextInput.defaultColor).mul(0.5f);
        mouseTextInput.fontColor = uiEngineConfig.ui.fontDefaultColor.cpy();
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
        mouseTextInput.charactersLC[maxCharacters] = mouseTextInput.charactersUC[maxCharacters] = UIEngine.M_TEXTINPUT_CHAR_CHANGE_CASE;
        mouseTextInput.charactersLC[maxCharacters + 1] = mouseTextInput.charactersUC[maxCharacters + 1] = UIEngine.M_TEXTINPUT_CHAR_BACK_;
        mouseTextInput.charactersLC[maxCharacters + 2] = mouseTextInput.charactersUC[maxCharacters + 2] = UIEngine.M_TEXTINPUT_CHAR_ACCEPT;
        return mouseTextInput;
    }

    public boolean isOpen() {
        return uiCommonUtils.mouseTextInput_isOpen();
    }

    public MouseTextInput currentMouseTextInput() {
        return uiEngineState.openMouseTextInput;
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
            enterCharacter(mouseTextInput, '\t');
        }
    }

    public void enterDelete(MouseTextInput mouseTextInput) {
        if (mouseTextInput == null) return;
        enterCharacter(mouseTextInput, '\b');
    }

    public void enterConfirm(MouseTextInput mouseTextInput) {
        if (mouseTextInput == null) return;
        enterCharacter(mouseTextInput, '\n');
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
        uiCommonUtils.mouseTextInput_selectCharacter(mouseTextInput, character);
    }

    public void selectIndex(MouseTextInput mouseTextInput, int index) {
        if (mouseTextInput == null) return;
        uiCommonUtils.mouseTextInput_selectIndex(mouseTextInput, index);
    }

    public void setCharacters(MouseTextInput mouseTextInput, char[] charactersLC, char[] charactersUC) {
        if (mouseTextInput == null) return;
        charactersLC = charactersLC != null ? charactersLC : new char[]{};
        charactersUC = charactersUC != null ? charactersUC : new char[]{};
        uiCommonUtils.mouseTextInput_setCharacters(mouseTextInput, charactersLC, charactersUC);
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

    public MouseTextInput createForTextField(TextField textfield) {
        ArrayList<Character> filteredLower = new ArrayList<>();
        ArrayList<Character> filteredUpper = new ArrayList<>();

        char[] defaultLower = api.config.mouseTextInput.defaultLowerCaseCharacters;
        char[] defaultUpper = api.config.mouseTextInput.defaultUpperCaseCharacters;

        int maxLen = Math.max(defaultLower.length, defaultUpper.length);
        for (int i = 0; i < maxLen; i++) {
            char lower = i < defaultLower.length ? defaultLower[i] : 0;
            char upper = i < defaultUpper.length ? defaultUpper[i] : 0;

            boolean lowerAllowed = lower != 0 && textfield.allowedCharacters.contains(lower);
            boolean upperAllowed = upper != 0 && textfield.allowedCharacters.contains(upper);

            if (lowerAllowed && upperAllowed) {
                filteredLower.add(lower);
                filteredUpper.add(upper);
            } else if (lowerAllowed) {
                filteredLower.add(lower);
                filteredUpper.add(lower);
            } else if (upperAllowed) {
                filteredLower.add(upper);
                filteredUpper.add(upper);
            }
        }

        char[] allowedLowerCaseCharacters = new char[filteredLower.size()];
        char[] allowedUpperCaseCharacters = new char[filteredUpper.size()];

        for (int i = 0; i < filteredLower.size(); i++) {
            allowedLowerCaseCharacters[i] = filteredLower.get(i);
            allowedUpperCaseCharacters[i] = filteredUpper.get(i);
        }

        int xOffset = (uiEngineState.theme.ts.abs(textfield.width) / 2)+6;
        MouseTextInput mouseTextInput = api.mouseTextInput.create(api.component.absoluteX(textfield) + xOffset, api.component.absoluteY(textfield), new MouseTextInputAction() {
            @Override
            public boolean onConfirm() {
                api.component.textfield.unFocus(textfield);
                textfield.textFieldAction.onEnter(textfield.content, textfield.contentValid);
                return true;
            }
        }, null, allowedLowerCaseCharacters, allowedUpperCaseCharacters);

        return mouseTextInput;
    }


}