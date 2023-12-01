package org.mslivo.core.engine.tools.save.settings.validator;

import org.mslivo.core.engine.tools.save.settings.SettingsManager;

public class EnumValueValidator implements ValueValidator {

    private Class c;

    public EnumValueValidator(Class c) {
        this.c = c;
    }

    @Override
    public boolean isValueValid(String value) {
        return SettingsManager.isValidEnum(value, c);
    }
}
