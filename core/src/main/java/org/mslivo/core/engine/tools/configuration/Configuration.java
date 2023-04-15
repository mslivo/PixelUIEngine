package org.mslivo.core.engine.tools.configuration;

import java.util.function.Function;

public record Configuration(String name, String defaultValue, Function<String, Boolean> validate) {

}
