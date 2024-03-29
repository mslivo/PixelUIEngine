package net.mslivo.core.engine.tools.persistence.settings.validator;

import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.tools.persistence.settings.SettingsManager;
import net.mslivo.core.engine.tools.persistence.settings.ValueValidator;

public class NumberValueValidator implements ValueValidator {

    public final int rangeFrom, rangeTo;

    public NumberValueValidator() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public NumberValueValidator(int rangeFrom, int rangeTo) {
        this.rangeFrom = rangeFrom;
        this.rangeTo = Tools.Calc.lowerBounds(rangeTo, rangeFrom);
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
