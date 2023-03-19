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
    public String getDriverName() {
        return properties.getProperty("driver-name");
    }

    @Override
    public String getHostname() {
        return properties.getProperty("hostname");
    }

    @Override
    public String getPort() {
        return properties.getProperty("port");
    }

    @Override
    public String getUrl() {
        return properties.getProperty("jdbc-url");
    }

    @Override
    public String getDatabaseName() {
        return properties.getProperty("database-name");
    }
}
