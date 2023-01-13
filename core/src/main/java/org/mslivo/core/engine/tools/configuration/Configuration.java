package org.mslivo.core.engine.tools.configuration;

import java.util.function.Function;

public class Configuration {

    public final String name;

    public final String defaultValue;

    public final Function<String, Boolean> validate;

    public Configuration(String name, String defaultValue, Function<String, Boolean> validate) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.validate = validate;
    }
}
