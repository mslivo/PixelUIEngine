package net.mslivo.core.engine.tools.settings.validator;

import net.mslivo.core.engine.tools.settings.SettingsManager;
import net.mslivo.core.engine.tools.settings.ValueValidator;

public class NumberValueValidator implements ValueValidator {

    public final int rangeFrom, rangeTo;

    public NumberValueValidator() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public NumberValueValidator(int rangeFrom, int rangeTo) {
        this.rangeFrom = rangeFrom;
        this.rangeTo = Math.max(rangeTo, rangeFrom);
    }

    @Override
    public boolean isValueValid(String value) {
        if (!SettingsManager.isValidNumber(value)) return false;
        try {
            int v = Integer.parseInt(value);
            return v >= rangeFrom && v <= rangeTo;
        } catch (Exception e) {
            return false;
        }
    }
}
