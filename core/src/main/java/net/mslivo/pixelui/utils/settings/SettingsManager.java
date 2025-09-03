package net.mslivo.pixelui.utils.settings;

import com.badlogic.gdx.utils.ObjectMap;
import net.mslivo.pixelui.utils.Tools;
import net.mslivo.pixelui.utils.settings.persistor.PropertiesFilePersistor;
import net.mslivo.pixelui.utils.settings.validator.StringValueValidator;

import java.util.Properties;


public class SettingsManager {
    private final Properties properties;

    private final String settingsFile;

    private final ObjectMap<String, SettingsEntry> entries;

    private final SettingsPersistor settingsPersistor;

    public static final String STRING_LIST_DELIMITER = ";";

    public SettingsManager(String path) throws SettingsException {
        this(path, new PropertiesFilePersistor());
        this.init();
    }

    public SettingsManager(String path, SettingsPersistor settingsPersistor) throws SettingsException {
        this.entries = new ObjectMap<>();
        this.properties = new Properties();
        this.settingsFile = Tools.Text.validString(path);
        this.settingsPersistor = settingsPersistor;
        this.init();
    }

    public void init() {
        settingsPersistor.loadSettings(settingsFile, properties);
        validateAllProperties();
        settingsPersistor.saveSettings(settingsFile, properties);
    }

    public Properties exportSettings(){
        Properties result = new Properties();
        String[] properties = this.properties.keySet().toArray(new String[0]);
        for (int i = 0; i < properties.length; i++) {
            String property = properties[i];
            result.setProperty(property, this.properties.getProperty(property));
        }
        return result;
    }

    public void importSettings(Properties importFrom){
        this.properties.clear();
        importFrom.forEach((key, value) -> this.properties.setProperty((String) key, (String) value));
        validateAllProperties();
        return;
    }

    public void addSetting(String name, String defaultValue) {
        addSetting(name, defaultValue, new StringValueValidator());
    }

    public void addSetting(String name, String defaultValue, ValueValidator valueValidator) {
        if (entries.get(name) == null) {
            SettingsEntry settingsEntry = new SettingsEntry(name, defaultValue, valueValidator);
            entries.put(settingsEntry.name(), settingsEntry);
            if (properties.getProperty(settingsEntry.name()) == null) {
                properties.setProperty(settingsEntry.name(), settingsEntry.defaultValue());
            } else {
                // already loaded
                validateProperty(settingsEntry.name());
            }
            settingsPersistor.saveSettings(settingsFile, properties);
        }
    }

    public void removeSettings(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) {
            properties.remove(settingsEntry.name());
            entries.remove(settingsEntry.name());
            settingsPersistor.saveSettings(settingsFile, properties);
        }
    }

    public void setToDefault(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) {
            set(settingsEntry.name(), settingsEntry.defaultValue());
        }
    }

    public void setAllToDefault() {
        ObjectMap.Keys<String> keys = this.entries.keys();
        while (keys.hasNext())
            setToDefault(keys.next());
    }

    private void validateAllProperties() {
        ObjectMap.Keys<String> keys = this.entries.keys();
        while (keys.hasNext())
            validateProperty(keys.next());

    }

    private void validateProperty(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) {
            if (!settingsEntry.valueValidator().isValueValid(properties.getProperty(name))) {
                this.properties.setProperty(name, settingsEntry.defaultValue());
            }
        }
    }

    /* ##### Value Setters ##### */

    public void setDecimal(String name, float decimalValue) {
        set(name, String.valueOf(decimalValue));
    }

    public float getDecimal(String name) {
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

    public static boolean isValidDecimal(String value) {
        if (value == null) return false;
        try {
            Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }


    public void setNumber(String name, int numberValue) {
        set(name, String.valueOf(numberValue));
    }

    public int getNumber(String name) {
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


    public static boolean isValidNumber(String value) {
        if (value == null) return false;
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public void setBoolean(String name, boolean boolValue) {
        set(name, boolValue ? "true" : "false");
    }

    public boolean getBoolean(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        int value = 0;
        if (settingsEntry != null) {
            return this.properties.getProperty(settingsEntry.name()).equals("true");
        }
        return false;
    }

    public static boolean isValidBoolean(String value) {
        if (value == null) return false;
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }


    public void setString(String name, String value) {
        set(name, value);
    }

    public String getString(String name) {
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) return this.properties.getProperty(settingsEntry.name());
        return null;
    }

    public void setStringList(String name, String[] values) {
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) setString(name, String.join(STRING_LIST_DELIMITER, values));
    }

    public String[] getStringList(String name) {
        String value = getString(name);
        if (value != null) return value.split(STRING_LIST_DELIMITER);
        return null;
    }

    public static boolean isValidString(String value) {
        return value != null;
    }

    public <T extends Enum<T>> void setEnum(String name, Enum<T> enumValue) {
        set(name, enumValue.name());
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

    public static boolean isValidEnum(String value, Class enumClass) {
        try {
            Enum.valueOf(enumClass, value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    private void set(String name, String value) {
        if (value == null) return;
        SettingsEntry settingsEntry = entries.get(name);
        if (settingsEntry != null) {
            String oldValue = this.properties.getProperty(settingsEntry.name());
            properties.setProperty(settingsEntry.name(), value);
            validateProperty(settingsEntry.name());
            if (oldValue != null && !oldValue.equals(value)) {
                settingsPersistor.saveSettings(settingsFile, properties);
            }
        }
    }


}
