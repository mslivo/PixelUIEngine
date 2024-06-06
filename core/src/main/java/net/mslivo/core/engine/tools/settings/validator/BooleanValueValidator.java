package net.mslivo.core.engine.tools.settings.validator;

import net.mslivo.core.engine.tools.settings.SettingsManager;
import net.mslivo.core.engine.tools.settings.ValueValidator;

public class BooleanValueValidator implements ValueValidator {
    @Override
    public boolean isValueValid(String value) {
        return SettingsManager.isValidBoolean(value);
    }
}
