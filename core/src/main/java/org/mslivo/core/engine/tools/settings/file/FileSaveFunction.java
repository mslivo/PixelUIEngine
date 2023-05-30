package org.mslivo.core.engine.tools.settings.file;

import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.tools.settings.SaveFunction;
import org.mslivo.core.engine.tools.settings.SettingsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class FileSaveFunction implements SaveFunction {

    private static final String EXTENSION = ".properties";

    @Override
    public void saveSettings(String propertiesName, Properties properties) {
        Path fileName = Path.of(propertiesName + EXTENSION);
        try {
            if (Tools.File.makeSureDirectoryExists(fileName.getParent())) {
                properties.store(Files.newOutputStream(Path.of(propertiesName + ".properties")), null);
            }
        } catch (IOException e) {
            throw new SettingsException(e);
        }
    }
}
