package org.mslivo.core.engine.tools.persistence.settings.validator;

import org.mslivo.core.engine.tools.persistence.settings.SettingsManager;
import org.mslivo.core.engine.tools.persistence.settings.ValueValidator;

public class BooleanValueValidator implements ValueValidator {
    @Override
    public boolean isValueValid(String value) {
        return SettingsManager.isValidBoolean(value);
    }
}
