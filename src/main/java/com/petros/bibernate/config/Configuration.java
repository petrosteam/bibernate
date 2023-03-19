package com.petros.bibernate.config;

public interface Configuration {

    String getProperty(String key);

    String getUsername();

    String getPassword();

    String getDriverName();

    String getHostname();

    String getPort();

    String getUrl();
    String getDatabaseName();

}
