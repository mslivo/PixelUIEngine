package org.mslivo.core.engine.tools.settings;

import java.util.function.Function;

public record SettingsEntry(String name, String defaultValue, Function<String, Boolean> validate) {

}
