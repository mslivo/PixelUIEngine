package net.mslivo.pixelui.utils.settings;

public class SettingsException extends RuntimeException {

    public SettingsException(Exception e){
        super(e);
    }

    public SettingsException(String message){
        super(message);
    }
}
