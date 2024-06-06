package net.mslivo.core.engine.tools.settings;

import java.util.Objects;

public final class SettingsEntry {
    private final String name;
    private final String defaultValue;
    private final ValueValidator valueValidator;

    public SettingsEntry(String name, String defaultValue, ValueValidator valueValidator) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.valueValidator = valueValidator;
    }

    public String name() {
        return name;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public ValueValidator valueValidator() {
        return valueValidator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SettingsEntry) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.defaultValue, that.defaultValue) &&
                Objects.equals(this.valueValidator, that.valueValidator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, defaultValue, valueValidator);
    }

    @Override
    public String toString() {
        return "SettingsEntry[" +
                "name=" + name + ", " +
                "defaultValue=" + defaultValue + ", " +
                "validateFunction=" + valueValidator + ']';
    }


}
