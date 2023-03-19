package com.petros.bibernate.config.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadPropertiesImpl implements ReadProperties {

    public static final String APPLICATION_PROPERTIES_PATH = "src/main/resources/application.properties";
    private static final Properties properties = new Properties();

    public ReadPropertiesImpl() {
        this(APPLICATION_PROPERTIES_PATH);
    }

    public ReadPropertiesImpl(String propertiesPath) {
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
