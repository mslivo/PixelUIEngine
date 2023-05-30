package org.mslivo.core.engine.tools.settings;

public record SettingsEntry(String name, String defaultValue, ValidateFunction validateFunction) {

}
