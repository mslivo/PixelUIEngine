package org.mslivo.core.engine.tools.settings;

import java.util.Properties;

public interface LoadFunction {
    void loadSettings(String propertiesName, Properties properties);

}