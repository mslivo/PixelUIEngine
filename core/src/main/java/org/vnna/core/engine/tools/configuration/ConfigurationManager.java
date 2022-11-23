package org.vnna.core.engine.tools.configuration;

import org.vnna.core.engine.tools.Tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Properties;
import java.util.function.Function;


public class ConfigurationManager {

    private Properties properties;

    private Properties backUp;

    private Path file;

    private HashMap<String, Configuration> configurations;

    private boolean initialized;

    public ConfigurationManager() throws ConfigurationException {
        this.configurations = new HashMap<>();
        this.properties = new Properties();
        this.initialized = false;
    }

    public void init(Path file) {
        if (!this.initialized) {
            this.file = file;
            if (Files.exists(file) && Files.isRegularFile(file)) {
                try {
                    properties.load(Files.newInputStream(file));
                } catch (IOException e) {
                    Tools.logError(e.getMessage());
                    throw new ConfigurationException(e);
                }
            } else {
                if (!Tools.File.makeSureDirectoryExists(file.getParent())) {
                    throw new ConfigurationException("Can't create directory " + file.getParent().toString());
                }
            }
            validateAllProperties();
            saveToFile();
            this.initialized = true;
        }
        return;
    }


    public void restoreBackup() {
        this.checkInitialized();
        if (isBackupActive()) {
            this.properties = new Properties();
            backUp.forEach((key, value) -> {
                this.properties.setProperty((String) key, (String) value);
            });
            validateAllProperties();
            saveToFile();
            discardBackup();
        }
    }

    public void createBackup() {
        this.checkInitialized();
        this.backUp = new Properties();
        for (Object propertyO : this.properties.keySet()) {
            String property = (String) propertyO;
            this.backUp.setProperty(property, this.properties.getProperty(property));
        }
    }

    public boolean doesOptionDeviateFromBackup(String option) {
        this.checkInitialized();
        if (isBackupActive()) {
            if (this.properties.get(option) != null && this.backUp.get(option) != null) {
                return !this.properties.get(option).equals(this.backUp.get(option));
            } else if (this.properties.get(option) == null && this.backUp.get(option) == null) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean doesAnyOptionDeviateFromBackup() {
        this.checkInitialized();
        if (this.backUp != null) {
            return !this.properties.equals(this.backUp);
        } else {
            return false;
        }
    }

    public boolean isBackupActive() {
        checkInitialized();
        return this.backUp != null;
    }

    public void discardBackup() {
        checkInitialized();
        this.backUp.clear();
        this.backUp = null;
    }

    public void addOption(String name, String defaultValue, Function<String, Boolean> evaluationFunction) {
        checkInitialized();
        if (configurations.get(name) == null) {
            Configuration configuration = new Configuration(name, defaultValue, evaluationFunction);
            this.configurations.put(configuration.name, configuration);
            if (this.properties.getProperty(configuration.name) == null) {
                this.properties.setProperty(configuration.name, configuration.defaultValue);
            } else {
                // already loaded
                validateProperty(configuration.name);
            }
            saveToFile();
        }
    }

    public void removeOption(String name) {
        checkInitialized();
        Configuration configuration = configurations.get(name);
        if (configuration != null) {
            this.properties.remove(configuration.name);
            this.configurations.remove(configuration.name);
            saveToFile();
        }
    }

    public void setToDefault(String name) {
        checkInitialized();
        Configuration configuration = configurations.get(name);
        if (configuration != null) {
            set(configuration.name, configuration.defaultValue);
        }
    }

    public void setAllToDefault() {
        checkInitialized();
        for (String optionsName : configurations.keySet()) {
            setToDefault(optionsName);
        }
    }

    public void setFloat(String name, float intValue) {
        checkInitialized();
        set(name, String.valueOf(intValue));
    }

    public void setInt(String name, int intValue) {
        checkInitialized();
        set(name, String.valueOf(intValue));
    }

    public void setBoolean(String name, boolean boolValue) {
        checkInitialized();
        set(name, boolValue ? "true" : "false");
    }

    public boolean getBoolean(String name) {
        checkInitialized();
        Configuration configuration = configurations.get(name);
        int value = 0;
        if (configuration != null) {
            if (this.properties.getProperty(configuration.name).equals("true")) {
                return true;
            }
        }
        return false;
    }

    public float getFloat(String name) {
        checkInitialized();
        Configuration configuration = configurations.get(name);
        float value = 0;
        if (configuration != null) {
            try {
                value = Float.parseFloat(this.properties.getProperty(configuration.name));
            } catch (Exception e) {
            }
        }
        return value;
    }

    public int getInt(String name) {
        checkInitialized();
        Configuration configuration = configurations.get(name);
        int value = 0;
        if (configuration != null) {
            try {
                value = Integer.parseInt(this.properties.getProperty(configuration.name));
            } catch (Exception e) {
            }
        }
        return value;
    }

    public String checkString(String value) {
        checkInitialized();
        if (value == null) return null;
        return value;
    }

    public Boolean checkBoolean(String value) {
        checkInitialized();
        if (value == null) return null;
        Boolean boolValue;
        try {
            boolValue = Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            return null;
        }
        return boolValue;
    }

    public Float checkFloat(String value) {
        checkInitialized();
        if (value == null) return null;
        Float floatValue;
        try {
            floatValue = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return null;
        }
        return floatValue;
    }

    public Integer checkInt(String value) {
        checkInitialized();
        if (value == null) return null;
        Integer intValue;
        try {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
        return intValue;
    }

    public String get(String name) {
        checkInitialized();
        Configuration configuration = configurations.get(name);
        if (configuration != null) {
            return this.properties.getProperty(configuration.name);
        }
        return null;
    }

    public String[] getStringList(String name) {
        checkInitialized();
        Configuration configuration = configurations.get(name);
        if (configuration != null) {
            return get(name).split(";");
        }
        return null;
    }

    public String checkStringList(String value) {
        checkInitialized();
        if (value == null) return null;
        return value;
    }

    public void setStringList(String name, String[] values) {
        checkInitialized();
        Configuration configuration = configurations.get(name);
        if (configuration != null) {
            set(name, String.join(";", values));
        }
        return;
    }

    private void checkInitialized() {
        if (!this.initialized)
            throw new ConfigurationException(ConfigurationManager.class.getSimpleName() + "not initialized.");
    }

    private void validateAllProperties() {
        for (String name : configurations.keySet()) {
            validateProperty(name);
        }
    }

    private void validateProperty(String name) {
        Configuration configuration = configurations.get(name);
        if (configuration != null) {
            if (!configuration.validate.apply(properties.getProperty(name))) {
                this.properties.setProperty(name, configuration.defaultValue);
            }
        }
    }

    public void set(String name, String value) {
        if (value == null) return;
        Configuration configuration = configurations.get(name);
        if (configuration != null) {
            String oldValue = this.properties.getProperty(configuration.name);
            this.properties.setProperty(configuration.name, value);
            validateProperty(configuration.name);
            if (oldValue != null && !oldValue.equals(value)) {
                saveToFile();
            }
        }
    }

    private void saveToFile() {
        try {
            this.properties.store(Files.newOutputStream(file), null);
        } catch (IOException e) {
            Tools.logError(e);
            throw new ConfigurationException(e);
        }

        return;
    }

}
