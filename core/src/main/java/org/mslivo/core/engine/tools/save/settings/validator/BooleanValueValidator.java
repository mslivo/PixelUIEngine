package org.mslivo.core.engine.tools.save.settings.validator;

import org.mslivo.core.engine.tools.save.settings.SettingsManager;
import org.mslivo.core.engine.tools.save.settings.validator.ValueValidator;

public class BooleanValueValidator implements ValueValidator {
    @Override
    public boolean isValueValid(String value) {
        return SettingsManager.isValidBoolean(value);
    }
}
