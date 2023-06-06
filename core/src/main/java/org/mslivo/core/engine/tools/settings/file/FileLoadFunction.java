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
        Path fileName = Path.of(settingsFile+EXTENSION);

        if (Files.exists(fileName) && Files.isRegularFile(fileName)) {
            try {
                properties.load(Files.newInputStream(fileName));
            } catch (IOException e) {
                throw new SettingsException(e);
            }
        }
    }
}
