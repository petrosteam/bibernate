package com.petros.bibernate.config;

public interface Configuration {

    String getProperty(String key);

    String getUsername();

    String getPassword();

    String getUrl();

}
