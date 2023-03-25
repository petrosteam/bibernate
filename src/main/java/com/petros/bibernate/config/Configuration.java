package com.petros.bibernate.config;

public interface Configuration {

    String JDBC_URL = "bibernate.jdbc.url";
    String JDBC_USERNAME = "bibernate.jdbc.username";
    String JDBC_PASSWORD = "bibernate.jdbc.password";
    String JDBC_POOL_SIZE = "bibernate.jdbc.connection-pool.size";

    String getProperty(String key);

    String getUsername();

    String getPassword();

    String getUrl();

}
