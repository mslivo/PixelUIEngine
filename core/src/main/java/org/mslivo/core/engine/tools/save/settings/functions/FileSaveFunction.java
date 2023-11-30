package org.mslivo.core.engine.tools.save.settings.functions;

import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.tools.save.settings.SaveFunction;
import org.mslivo.core.engine.tools.save.settings.SettingsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class FileSaveFunction implements SaveFunction {

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
}
