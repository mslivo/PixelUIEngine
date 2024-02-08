package org.mslivo.core.engine.tools.persistence.settings.validator;

import org.mslivo.core.engine.tools.persistence.settings.SettingsManager;
import org.mslivo.core.engine.tools.persistence.settings.ValueValidator;

import java.util.HashSet;

public class EnumValueValidator implements ValueValidator {
    private final Class c;
    private final HashSet<Enum> allowedValuesSet;

    public EnumValueValidator(Class enumClass) {
        this(enumClass, null);
    }

    public EnumValueValidator(Class enumClass, Enum[] allowedValues) {
        this.c = enumClass;
        this.allowedValuesSet = new HashSet<>();
        if (allowedValues != null) {
            for (int i = 0; i < allowedValues.length; i++) {
                if (allowedValues[i] != null) allowedValuesSet.add(allowedValues[i]);
            }
        }
    }

    @Override
    public boolean isValueValid(String value) {
        if (!SettingsManager.isValidEnum(value, c)) return false;
        if (!this.allowedValuesSet.isEmpty() && !this.allowedValuesSet.contains(Enum.valueOf(c, value))) return false;
        return true;
    }
}
