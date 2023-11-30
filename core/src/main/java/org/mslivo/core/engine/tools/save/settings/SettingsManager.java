package org.mslivo.core.engine.tools.save.settings;

import org.mslivo.core.engine.tools.Tools;
import org.mslivo.core.engine.tools.save.settings.functions.FileLoadFunction;
import org.mslivo.core.engine.tools.save.settings.functions.FileSaveFunction;

import java.util.HashMap;
import java.util.Properties;


public class SettingsManager {
    private final Properties properties;

    private final Properties backUp;

    private final String settingsFile;

    private final HashMap<String, SettingsEntry> entries;

    private final SaveFunction saveFunction;

    private final LoadFunction loadFunction;

    public SettingsManager(String settingsFile) throws SettingsException {
        this(settingsFile, new FileSaveFunction(), new FileLoadFunction());
        this.init();
    }


    public SettingsManager(String settingsFile, SaveFunction saveFunction, LoadFunction loadFunction) throws SettingsException {
        this.entries = new HashMap<>();
        this.properties = new Properties();
        this.backUp = new Properties();
        this.settingsFile = Tools.Text.validString(settingsFile);
        this.saveFunction = saveFunction;
        this.loadFunction = loadFunction;
        this.init();
    }

    public void init() {
        loadFunction.loadSettings(settingsFile, properties);
        validateAllProperties();
        saveFunction.saveSettings(settingsFile, properties);
    }


    public void restoreBackup() {
        if (isBackupActive()) {
            this.properties.clear();
            backUp.forEach((key, value) -> this.properties.setProperty((String) key, (String) value));
            validateAllProperties();
            saveFunction.saveSettings(settingsFile, properties);
            discardBackup();
        }
    }

    public void createBackup() {
        this.backUp.clear();
        String[] properties = this.properties.keySet().toArray(new String[this.properties.size()]);
        for (int i = 0; i < properties.length; i++) {
            String property = properties[i];
            this.backUp.setProperty(property, this.properties.getProperty(property));
        }
    }

    public boolean doesSettingDeviateFromBackup(String name) {
        if (isBackupActive()) {
            if (this.properties.get(name) != null && this.backUp.get(name) != null) {
                return !this.properties.get(name).equals(this.backUp.get(name));
            } else return this.properties.get(name) != null || this.backUp.get(name) != null;
        } else {
            return false;
        }
    }

    public boolean doesAnySettingDeviateFromBackup() {
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
    }

    public void addSetting(String name, String defaultValue, ValidateFunction validateFunction) {
        if (entries.get(name) == null) {
            SettingsEntry settingsEntry = new SettingsEntry(name, defaultValue, validateFunction);
            entries.put(settingsEntry.name(), settingsEntry);
            if (properties.getProperty(settingsEntry.name()) == null) {
                properties.setProperty(settingsEntry.name(), settingsEntry.defaultValue());
            } else {
                // already loaded
                validateProperty(settingsEntry.name());
            }
            saveFunction.saveSettings(settingsFile, properties);
        }
    }

    public void removeSettings(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) {
            properties.remove(settingsEntry.name());
            entries.remove(settingsEntry.name());
            saveFunction.saveSettings(settingsFile, properties);
        }
    }

    public void setToDefault(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) {
            set(settingsEntry.name(), settingsEntry.defaultValue());
        }
    }

    public void setAllToDefault() {
        String[] entries = this.entries.keySet().toArray(new String[this.entries.size()]);
        for (int i = 0; i < entries.length; i++) {
            setToDefault(entries[i]);
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

    public <T extends Enum<T>> void setEnum(String name, Enum<T> enumValue) {
        set(name, enumValue.name());
    }

    public boolean getBoolean(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        int value = 0;
        if (settingsEntry != null) {
            return this.properties.getProperty(settingsEntry.name()).equals("true");
        }
        return false;
    }

    public float getFloat(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        float value = 0;
        if (settingsEntry != null) {
            try {
                value = Float.parseFloat(this.properties.getProperty(settingsEntry.name()));
            } catch (Exception e) {
            }
        }
        return value;
    }

    public int getInt(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        int value = 0;
        if (settingsEntry != null) {
            try {
                value = Integer.parseInt(this.properties.getProperty(settingsEntry.name()));
            } catch (Exception e) {
            }
        }
        return value;
    }

    public static boolean isValidString(String value) {
        if (value == null) return false;
        return true;
    }

    public static boolean isValidBoolean(String value) {
        if (value == null) return false;
        try {
            Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isValidFloat(String value) {
        if (value == null) return false;
        try {
            Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static <T extends Enum<T>> boolean isValidEnum(String value, Class<T> enumClass) {
        try {
            Enum.valueOf(enumClass, value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    ;

    public static boolean isValidInt(String value) {
        if (value == null) return false;
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public String getString(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) return this.properties.getProperty(settingsEntry.name());
        return null;
    }

    public <T extends Enum<T>> T getEnum(String name, Class<T> enumClass) {
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) {
            try {
                return Enum.valueOf(enumClass, this.properties.getProperty(settingsEntry.name()));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public String[] getStringList(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) return getString(name).split(";");
        return null;
    }

    public void setStringList(String name, String[] values) {
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) set(name, String.join(";", values));
    }

    private void validateAllProperties() {
        String[] entries = this.entries.keySet().toArray(new String[this.entries.size()]);
        for (int i = 0; i < entries.length; i++) {
            validateProperty(entries[i]);
        }
    }

    private void validateProperty(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) {
            if (!settingsEntry.validateFunction().isValueValid(properties.getProperty(name))) {
                this.properties.setProperty(name, settingsEntry.defaultValue());
            }
        }
    }

    public void setString(String name, String value) {
        set(name, value);
    }

    private void set(String name, String value) {
        if (value == null) return;
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) {
            String oldValue = this.properties.getProperty(settingsEntry.name());
            properties.setProperty(settingsEntry.name(), value);
            validateProperty(settingsEntry.name());
            if (oldValue != null && !oldValue.equals(value)) {
                saveFunction.saveSettings(settingsFile, properties);
            }
        }
    }


}
