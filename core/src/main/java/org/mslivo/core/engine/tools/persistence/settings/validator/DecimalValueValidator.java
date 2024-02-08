package org.mslivo.core.engine.tools.persistence.settings.validator;

import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.tools.persistence.settings.SettingsManager;
import org.mslivo.core.engine.tools.persistence.settings.ValueValidator;

public class DecimalValueValidator implements ValueValidator {

    public float rangeFrom, rangeTo;

    public DecimalValueValidator() {
        this(Float.MIN_VALUE, Float.MAX_VALUE);
    }

    public DecimalValueValidator(float rangeFrom, float rangeTo) {
        this.rangeFrom = rangeFrom;
        this.rangeTo = Tools.Calc.lowerBounds(rangeTo, rangeFrom);
    }

    @Override
    public boolean isValueValid(String value) {
        if (!SettingsManager.isValidDecimal(value)) return false;
        try {
            float v = Float.parseFloat(value);
            return v >= rangeFrom && v <= rangeTo;
        } catch (Exception e) {
            return false;
        }
    }
}
