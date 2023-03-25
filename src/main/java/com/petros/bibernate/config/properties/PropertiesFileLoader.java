package com.petros.bibernate.config.properties;

import java.io.FileInputStream;
import java.io.IOException;

public class PropertiesFileLoader implements PropertiesLoader {

    private static final java.util.Properties properties = new java.util.Properties();

    public PropertiesFileLoader(String propertiesPath) {
        try (var file = new FileInputStream(propertiesPath)) {
            properties.load(file);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public java.util.Properties getProperties() {
        return properties;
    }
}
