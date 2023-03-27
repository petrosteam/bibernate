package com.petros.bibernate.config;

import com.petros.bibernate.config.properties.PropertiesFileLoader;
import com.petros.bibernate.config.properties.PropertiesLoader;

public class ConfigurationImpl implements Configuration {

    private static java.util.Properties properties;

    public ConfigurationImpl(String configPath) {
        this(new PropertiesFileLoader(configPath));
    }

    public ConfigurationImpl(PropertiesLoader propertiesFileLoader) {
        properties = propertiesFileLoader.getProperties();
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public String getUsername() {
        return properties.getProperty(JDBC_USERNAME);
    }

    @Override
    public String getPassword() {
        return properties.getProperty(JDBC_PASSWORD);
    }


    @Override
    public String getUrl() {
        return properties.getProperty(JDBC_URL);
    }

}
