package net.mslivo.pixelui.utils.settings;

import java.util.Properties;

public interface SettingsPersistor {
    void saveSettings(String settingsFile, Properties properties);

    void loadSettings(String settingsFile, Properties properties);
}
