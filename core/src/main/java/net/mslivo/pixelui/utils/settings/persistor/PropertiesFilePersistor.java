package net.mslivo.pixelui.utils.settings.persistor;

import net.mslivo.pixelui.utils.settings.SettingsException;
import net.mslivo.pixelui.utils.settings.SettingsPersistor;
import net.mslivo.pixelui.utils.Tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PropertiesFilePersistor implements SettingsPersistor {

    private static final String EXTENSION = ".properties";

    @Override
    public void saveSettings(String settingsFile, Properties properties) {
        String fileString = settingsFile;
        if(!settingsFile.endsWith(EXTENSION)) fileString += EXTENSION;

        Path file = Path.of(fileString);
        try {
            if (Tools.File.makeSureDirectoryExists(file.getParent())) {
                properties.store(Files.newOutputStream(file), null);
            }
        } catch (IOException e) {
            throw new SettingsException(e);
        }
    }

    @Override
    public void loadSettings(String settingsFile, Properties properties) {
        String fileString = settingsFile;
        if(!settingsFile.endsWith(EXTENSION)) fileString += EXTENSION;

        Path file = Path.of(fileString);
        if (Files.exists(file) && Files.isRegularFile(file)) {
            try {
                properties.load(Files.newInputStream(file));
            } catch (IOException e) {
                throw new SettingsException(e);
            }
        }
    }
}
