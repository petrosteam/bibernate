package com.petros.bibernate.config.properties;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * A class that loads properties from a file.
 */
@Slf4j
public class PropertiesFileLoader implements PropertiesLoader {

    private static final Properties properties = new Properties();

    /**
     * Constructs a new {@code PropertiesFileLoader} object and loads the properties from the specified file.
     *
     * @param propertiesPath the path to the properties file
     * @throws RuntimeException if an I/O error occurs while reading the file
     */
    public PropertiesFileLoader(String propertiesPath) {
        try (var file = new FileInputStream(propertiesPath)) {
            properties.load(file);
        } catch (IOException ex) {
            log.error("Error loading properties file: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns the loaded properties.
     *
     * @return the loaded properties
     */
    @Override
    public Properties getProperties() {
        return properties;
    }
}
