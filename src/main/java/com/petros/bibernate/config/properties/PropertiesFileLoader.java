package com.petros.bibernate.config.properties;

import java.io.FileInputStream;
import java.io.IOException;

public class PropertiesFileLoader implements PropertiesLoader {

    public static final String APPLICATION_PROPERTIES_PATH = "src/main/resources/application.properties";
    private static final java.util.Properties properties = new java.util.Properties();

    public PropertiesFileLoader() {
        this(APPLICATION_PROPERTIES_PATH);
    }

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
