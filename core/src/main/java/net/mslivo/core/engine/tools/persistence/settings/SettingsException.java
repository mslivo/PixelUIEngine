package net.mslivo.core.engine.tools.persistence.settings;

public class SettingsException extends RuntimeException {

    public SettingsException(Exception e){
        super(e);
    }

    public SettingsException(String message){
        super(message);
    }
}
