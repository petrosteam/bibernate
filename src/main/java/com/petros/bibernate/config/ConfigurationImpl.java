package com.petros.bibernate.config;

import com.petros.bibernate.config.properties.ReadPropertiesFromPropImpl;

import java.util.Properties;

public class ConfigurationImpl implements Configuration {

    Properties properties = new ReadPropertiesFromPropImpl().getProperties();

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

        //TODO create url from params
        return null;
    }
}
