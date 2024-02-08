package org.mslivo.core.engine.tools.persistence.settings.validator;

import org.mslivo.core.engine.tools.persistence.settings.SettingsManager;
import org.mslivo.core.engine.tools.persistence.settings.ValueValidator;

import java.util.HashSet;

public class StringListValueValidator implements ValueValidator {
    private final HashSet<String> allowedValuesSet;

    public final int listSizeMin, listSizeMax;

    public final int listEntryLengthMin, listEntryLengthMax;


    public StringListValueValidator() {
        this(null, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public StringListValueValidator(String[] allowedValues) {
        this(allowedValues, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public StringListValueValidator(String[] allowedValues, int listSizeMin, int listSizeMax) {
        this(null, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public StringListValueValidator(int listSizeMin, int listSizeMax) {
        this(null, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public StringListValueValidator(int listSizeMin, int listSizeMax, int listEntryLengthMin, int listEntryLengthMax) {
        this(null, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public StringListValueValidator(String[] allowedValues, int listSizeMin, int listSizeMax, int listEntryLengthMin, int listEntryLengthMax) {
        this.listSizeMin = listSizeMin;
        this.listSizeMax = listSizeMax;
        this.listEntryLengthMin = listEntryLengthMin;
        this.listEntryLengthMax = listEntryLengthMax;
        this.allowedValuesSet = new HashSet<>();
        if (allowedValues != null) {
            for (int i = 0; i < allowedValues.length; i++) {
                if (allowedValues[i] != null) allowedValuesSet.add(allowedValues[i]);
            }
        }
    }

    @Override
    public boolean isValueValid(String value) {
        String[] stringList = value.split(SettingsManager.STRING_LIST_DELIMITER);
        if (stringList == null) return false;
        if (stringList.length < listSizeMin || stringList.length > listSizeMax) return false;
        for (int i = 0; i < stringList.length; i++) {
            if (!SettingsManager.isValidString(stringList[i])) return false;
            if (!this.allowedValuesSet.isEmpty() && !this.allowedValuesSet.contains(stringList[i])) return false;
            if (value.length() < listEntryLengthMin || value.length() > listEntryLengthMax) return false;
        }
        return true;
    }
}
