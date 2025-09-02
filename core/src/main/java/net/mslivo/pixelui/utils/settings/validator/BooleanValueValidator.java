package net.mslivo.pixelui.utils.settings.validator;

import net.mslivo.pixelui.utils.settings.SettingsManager;
import net.mslivo.pixelui.utils.settings.ValueValidator;

public class BooleanValueValidator implements ValueValidator {
    @Override
    public boolean isValueValid(String value) {
        return SettingsManager.isValidBoolean(value);
    }
}
