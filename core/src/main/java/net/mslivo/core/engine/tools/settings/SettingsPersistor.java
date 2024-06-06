package net.mslivo.core.engine.tools.settings;

import java.util.Properties;

public interface SettingsPersistor {
    void saveSettings(String settingsFile, Properties properties);

    void loadSettings(String settingsFile, Properties properties);
}
