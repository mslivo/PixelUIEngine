package org.mslivo.core.engine.tools.save.settings;

import java.util.Objects;

public final class SettingsEntry {
    private final String name;
    private final String defaultValue;
    private final ValidateFunction validateFunction;

    public SettingsEntry(String name, String defaultValue, ValidateFunction validateFunction) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.validateFunction = validateFunction;
    }

    public String name() {
        return name;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public ValidateFunction validateFunction() {
        return validateFunction;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SettingsEntry) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.defaultValue, that.defaultValue) &&
                Objects.equals(this.validateFunction, that.validateFunction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, defaultValue, validateFunction);
    }

    @Override
    public String toString() {
        return "SettingsEntry[" +
                "name=" + name + ", " +
                "defaultValue=" + defaultValue + ", " +
                "validateFunction=" + validateFunction + ']';
    }


}
