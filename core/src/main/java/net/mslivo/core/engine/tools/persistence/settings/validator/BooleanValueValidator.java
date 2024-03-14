package net.mslivo.core.engine.tools.persistence.settings.validator;

import net.mslivo.core.engine.tools.persistence.settings.SettingsManager;
import net.mslivo.core.engine.tools.persistence.settings.ValueValidator;

public class BooleanValueValidator implements ValueValidator {
    @Override
    public boolean isValueValid(String value) {
        return SettingsManager.isValidBoolean(value);
    }
}
