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

    public ConfigurationManager(Path file) {
        this.file = file;
        this.configurations = new HashMap<>();


        this.properties = new Properties();

        if (Files.exists(file) && Files.isRegularFile(file)) {
            try {
                properties.load(Files.newInputStream(file));
            } catch (IOException e) {
                Tools.logError(e);
            }
        } else {
            Tools.File.makeSureDirectoryExists(file.getParent());
        }

        validateAllProperties();
        syncToFile();
    }


    public void loadTemporaryBackup() {
        if (this.backUp != null) {
            this.properties = new Properties();
            backUp.forEach((key, value) -> {
                this.properties.setProperty((String) key, (String) value);
            });
            validateAllProperties();
            syncToFile();
            discardBackup();
        }
    }

    public boolean deviatesFromBackup(String option) {
        if (this.backUp != null) {
            if(this.properties.get(option) == null && this.backUp.get(option) == null){
                return false;
            }else if(this.properties.get(option) == null && this.backUp.get(option) != null){
                return true;
            }else if(this.properties.get(option) != null && this.backUp.get(option) == null){
                return true;
            }else{
                return !this.properties.get(option).equals(this.backUp.get(option));
            }
        } else {
            return false;
        }
    }

    public boolean anyDeviateFromBackup() {
        if (this.backUp != null) {
            return !this.properties.equals(this.backUp);
        } else {
            return false;
        }
    }

    public void discardBackup() {
        if (this.backUp != null) {
            this.backUp.clear();
            this.backUp = null;
        }
    }

    public void createTemporaryBackup() {
        this.backUp = new Properties();
        for (Object propertyO : this.properties.keySet()) {
            String property = (String) propertyO;
            this.backUp.setProperty(property, this.properties.getProperty(property));
        }
    }


    public void addOption(String name, String defaultValue, Function<String, Boolean> function) {
        if (configurations.get(name) == null) {
            Configuration configuration = new Configuration(name, defaultValue, function);
            this.configurations.put(configuration.name, configuration);
            if (this.properties.getProperty(configuration.name) == null) {
                this.properties.setProperty(configuration.name, configuration.defaultValue);
            } else {
                // already loaded
                validateProperty(configuration.name);
            }
            syncToFile();
        }
    }

    public void removeOption(String name) {
        Configuration configuration = configurations.get(name);
        if (configuration != null) {
            this.properties.remove(configuration.name);
            this.configurations.remove(configuration.name);
            syncToFile();
        }
    }

    public void setToDefault(String name) {
        Configuration configuration = configurations.get(name);
        if (configuration != null) {
            set(configuration.name, configuration.defaultValue);
        }
    }

    public void setAllToDefault() {
        for (String optionsName : configurations.keySet()) {
            setToDefault(optionsName);
        }
    }

    public void setFloat(String name, float intValue) {
        set(name, String.valueOf(intValue));
    }

    public void setInt(String name, int intValue) {
        set(name, String.valueOf(intValue));
    }

    public void setBool(String name, boolean boolValue) {
        set(name, boolValue ? "true" : "false");
    }

    public boolean getBoolean(String name) {
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
        if (value == null) return null;
        return value;
    }

    public Boolean checkBoolean(String value) {
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
        Configuration configuration = configurations.get(name);
        if (configuration != null) {
            return this.properties.getProperty(configuration.name);
        }
        return null;
    }

    public String[] getStringList(String name) {
        Configuration configuration = configurations.get(name);
        if (configuration != null) {
            return get(name).split(";");
        }
        return null;
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
                syncToFile();
            }
        }
    }

    public void setStringList(String name, String[] values) {
        Configuration configuration = configurations.get(name);
        if (configuration != null) {
            set(name, String.join(";", values));
        }
        return;
    }


    private void syncToFile() {
        try {
            this.properties.store(Files.newOutputStream(file), null);
        } catch (Exception e) {
            Tools.logError(e);
        }
    }

}
