package com.petros.bibernate.config.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesFileLoader implements PropertiesLoader {

    private static final Properties properties = new Properties();

    public PropertiesFileLoader(String propertiesPath) {
        try (var file = new FileInputStream(propertiesPath)) {
            properties.load(file);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
