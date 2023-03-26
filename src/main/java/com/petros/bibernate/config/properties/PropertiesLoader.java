package com.petros.bibernate.config.properties;

import java.util.Properties;

/**
 * Interface  PropertiesLoader load configuration params from file
 * Default file place src/main/resources/application.properties
 */
public interface PropertiesLoader {
    /**
     * @return set properties from configuration file
     */
    Properties getProperties();

}
