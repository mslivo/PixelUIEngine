package org.mslivo.core.engine.tools.configuration;

import org.mslivo.core.engine.tools.Tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Properties;
import java.util.function.Function;


public class ConfigurationManager {

    private Properties properties;

    private Properties backUp;

    private Path configurationFile;

    private final HashMap<String, ConfigurationEntry> entries;

    public ConfigurationManager(Path configurationFile) throws ConfigurationException {
        this.entries = new HashMap<>();
        this.properties = new Properties();
        this.init(configurationFile);
    }

    public void init(Path configurationFile) {
        this.configurationFile = configurationFile;
        if (Files.exists(configurationFile) && Files.isRegularFile(configurationFile)) {
            try {
                properties.load(Files.newInputStream(configurationFile));
            } catch (IOException e) {
                throw new ConfigurationException(e);
            }
        } else {
            if (!Tools.File.makeSureDirectoryExists(configurationFile.getParent())) {
                throw new ConfigurationException("Can't create directory " + configurationFile.getParent().toString());
            }
        }
        validateAllProperties();
        saveToFile();
    }


    public void restoreBackup() {
        if (isBackupActive()) {
            this.properties = new Properties();
            backUp.forEach((key, value) -> this.properties.setProperty((String) key, (String) value));
            validateAllProperties();
            saveToFile();
            discardBackup();
        }
    }

    public void createBackup() {
        this.backUp = new Properties();
        for (Object propertyO : this.properties.keySet()) {
            String property = (String) propertyO;
            this.backUp.setProperty(property, this.properties.getProperty(property));
        }
    }

    public boolean doesOptionDeviateFromBackup(String option) {
        if (isBackupActive()) {
            if (this.properties.get(option) != null && this.backUp.get(option) != null) {
                return !this.properties.get(option).equals(this.backUp.get(option));
            } else return this.properties.get(option) != null || this.backUp.get(option) != null;
        } else {
            return false;
        }
    }

    public boolean doesAnyOptionDeviateFromBackup() {
        if (this.backUp != null) {
            return !this.properties.equals(this.backUp);
        } else {
            return false;
        }
    }

    public boolean isBackupActive() {
        return this.backUp != null;
    }

    public void discardBackup() {
        this.backUp.clear();
        this.backUp = null;
    }

    public void addOption(String name, String defaultValue, Function<String, Boolean> validateFunction) {
        if (entries.get(name) == null) {
            ConfigurationEntry configurationEntry = new ConfigurationEntry(name, defaultValue, validateFunction);
            this.entries.put(configurationEntry.name(), configurationEntry);
            if (this.properties.getProperty(configurationEntry.name()) == null) {
                this.properties.setProperty(configurationEntry.name(), configurationEntry.defaultValue());
            } else {
                // already loaded
                validateProperty(configurationEntry.name());
            }
            saveToFile();
        }
    }

    public void removeOption(String name) {
        ConfigurationEntry configurationEntry = entries.get(name);
        if (configurationEntry != null) {
            this.properties.remove(configurationEntry.name());
            this.entries.remove(configurationEntry.name());
            saveToFile();
        }
    }

    public void setToDefault(String name) {
        ConfigurationEntry configurationEntry = entries.get(name);
        if (configurationEntry != null) {
            set(configurationEntry.name(), configurationEntry.defaultValue());
        }
    }

    public void setAllToDefault() {
        for (String optionsName : entries.keySet()) {
            setToDefault(optionsName);
        }
    }

    public void setFloat(String name, float intValue) {
        set(name, String.valueOf(intValue));
    }

    public void setInt(String name, int intValue) {
        set(name, String.valueOf(intValue));
    }

    public void setBoolean(String name, boolean boolValue) {
        set(name, boolValue ? "true" : "false");
    }

    public boolean getBoolean(String name) {
        ConfigurationEntry configurationEntry = entries.get(name);
        int value = 0;
        if (configurationEntry != null) {
            return this.properties.getProperty(configurationEntry.name()).equals("true");
        }
        return false;
    }

    public float getFloat(String name) {
        ConfigurationEntry configurationEntry = entries.get(name);
        float value = 0;
        if (configurationEntry != null) {
            try {
                value = Float.parseFloat(this.properties.getProperty(configurationEntry.name()));
            } catch (Exception e) {
            }
        }
        return value;
    }

    public int getInt(String name) {
        ConfigurationEntry configurationEntry = entries.get(name);
        int value = 0;
        if (configurationEntry != null) {
            try {
                value = Integer.parseInt(this.properties.getProperty(configurationEntry.name()));
            } catch (Exception e) {
            }
        }
        return value;
    }

    public String checkString(String value) {
        return value;
    }

    public Boolean checkBoolean(String value) {
        if (value == null) return null;
        boolean boolValue;
        try {
            boolValue = Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            return null;
        }
        return boolValue;
    }

    public Float checkFloat(String value) {
        if (value == null) return null;
        float floatValue;
        try {
            floatValue = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return null;
        }
        return floatValue;
    }

    public Integer checkInt(String value) {
        if (value == null) return null;
        int intValue;
        try {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
        return intValue;
    }

    public String get(String name) {
        ConfigurationEntry configurationEntry = entries.get(name);
        if (configurationEntry != null) {
            return this.properties.getProperty(configurationEntry.name());
        }
        return null;
    }

    public String[] getStringList(String name) {
        ConfigurationEntry configurationEntry = entries.get(name);
        if (configurationEntry != null) {
            return get(name).split(";");
        }
        return null;
    }

    public String checkStringList(String value) {
        return value;
    }

    public void setStringList(String name, String[] values) {
        ConfigurationEntry configurationEntry = entries.get(name);
        if (configurationEntry != null) {
            set(name, String.join(";", values));
        }
    }

    private void validateAllProperties() {
        for (String name : entries.keySet()) {
            validateProperty(name);
        }
    }

    private void validateProperty(String name) {
        ConfigurationEntry configurationEntry = entries.get(name);
        if (configurationEntry != null) {
            if (!configurationEntry.validate().apply(properties.getProperty(name))) {
                this.properties.setProperty(name, configurationEntry.defaultValue());
            }
        }
    }

    public void set(String name, String value) {
        if (value == null) return;
        ConfigurationEntry configurationEntry = entries.get(name);
        if (configurationEntry != null) {
            String oldValue = this.properties.getProperty(configurationEntry.name());
            this.properties.setProperty(configurationEntry.name(), value);
            validateProperty(configurationEntry.name());
            if (oldValue != null && !oldValue.equals(value)) {
                saveToFile();
            }
        }
    }

    private void saveToFile() {
        try {
            this.properties.store(Files.newOutputStream(configurationFile), null);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }

    }

}
