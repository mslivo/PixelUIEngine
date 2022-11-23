package org.vnna.core.engine.tools.configuration;

public class ConfigurationException extends RuntimeException {

    public ConfigurationException(Exception e){
        super(e);
    }

    public ConfigurationException(String message){
        super(message);
    }
}
