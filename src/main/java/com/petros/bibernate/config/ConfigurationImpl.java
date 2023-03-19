package com.petros.bibernate.config;

import com.petros.bibernate.config.properties.ReadPropertiesImpl;

import java.util.Properties;

public class ConfigurationImpl implements Configuration {

    private static Properties properties;

    public ConfigurationImpl() {
        this(new ReadPropertiesImpl());
    }

    public ConfigurationImpl(ReadPropertiesImpl readPropertiesFromProp) {
        properties = readPropertiesFromProp.getProperties();
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public String getUsername() {
        return properties.getProperty("username");
    }

    @Override
    public String getPassword() {
        return properties.getProperty("password");
    }


    @Override
    public String getUrl() {
        return properties.getProperty("jdbc-url");
    }

}
