package net.mslivo.pixelui.utils.settings.validator;

import net.mslivo.pixelui.utils.settings.SettingsManager;
import net.mslivo.pixelui.utils.settings.ValueValidator;

import java.util.HashSet;

public class StringListValueValidator implements ValueValidator {
    private final HashSet<String> allowedValuesSet;

    public final int listSizeMin, listSizeMax;

    public final int listEntryLengthMin, listEntryLengthMax;


    public StringListValueValidator() {
        this(null, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
    }

    public StringListValueValidator(String[] allowedValues) {
        this(allowedValues, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
    }

    public StringListValueValidator(String[] allowedValues, int listSizeMin, int listSizeMax) {
        this(null, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
    }

    public StringListValueValidator(int listSizeMin, int listSizeMax) {
        this(null, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
    }

    public StringListValueValidator(int listSizeMin, int listSizeMax, int listEntryLengthMin, int listEntryLengthMax) {
        this(null, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
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
        if (value == null) return false;
        String[] stringList = value.split(SettingsManager.STRING_LIST_DELIMITER);
        if (stringList.length < listSizeMin || stringList.length > listSizeMax) return false;
        for (int i = 0; i < stringList.length; i++) {
            if (!SettingsManager.isValidString(stringList[i])) return false;
            if (!this.allowedValuesSet.isEmpty() && !this.allowedValuesSet.contains(stringList[i])) return false;
            if (value.length() < listEntryLengthMin || value.length() > listEntryLengthMax) return false;
        }
        return true;
    }
}
