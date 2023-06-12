package org.mslivo.core.engine.tools.settings.file;

import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.tools.settings.LoadFunction;
import org.mslivo.core.engine.tools.settings.SaveFunction;
import org.mslivo.core.engine.tools.settings.SettingsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class FileLoadFunction implements LoadFunction {

    private static final String EXTENSION = ".properties";

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
