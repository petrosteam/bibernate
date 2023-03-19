package com.petros.bibernate.config.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadPropertiesFromPropImpl implements ReadProperties {

    private static final Properties properties = new Properties();

    @Override
    public Properties getProperties() {
        try (var file = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(file);
            return properties;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
