package org.mslivo.core.engine.tools.configuration;

import java.util.function.Function;

public record ConfigurationEntry(String name, String defaultValue, Function<String, Boolean> validate) {

}
