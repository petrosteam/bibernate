package com.petros.bibernate.config;

import com.petros.bibernate.config.properties.PropertiesFileLoader;
import com.petros.bibernate.config.properties.PropertiesLoader;
import com.petros.bibernate.exception.BibernateException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Slf4j
public class ConfigurationImpl implements Configuration {

    private static java.util.Properties properties;

    public ConfigurationImpl(String configPath) {
        this(new PropertiesFileLoader(configPath));
    }

    public ConfigurationImpl(PropertiesLoader propertiesFileLoader) {
        properties = propertiesFileLoader.getProperties();
    }

    @Override
    public Optional<String> getProperty(String key) {
        return ofNullable(properties.getProperty(key));
    }

    @Override
    public String getUsername() {
        return getProperty(JDBC_USERNAME).orElseThrow(() ->
                new BibernateException(String.format("Property %s must be set", JDBC_USERNAME)));
    }

    @Override
    public String getPassword() {
        return getProperty(JDBC_PASSWORD).orElseThrow(() ->
                new BibernateException(String.format("Property %s must be set", JDBC_PASSWORD)));
    }

    @Override
    public Integer getConnectionPoolSize() {
        return Integer.parseInt(getProperty(JDBC_POOL_SIZE).orElseGet(() -> {
            log.info("Property {} is not set, default value will be used: {}", JDBC_POOL_SIZE,
                    DEFAULT_CONNECTION_POOL_SIZE);
            return String.valueOf(DEFAULT_CONNECTION_POOL_SIZE);
        }));
    }

    @Override
    public String getUrl() {
        return getProperty(JDBC_URL).orElseThrow(() ->
                new BibernateException(String.format("Property %s must be set", JDBC_URL)));
    }

    @Override
    public boolean showSql() {
        return Boolean.parseBoolean(properties.getProperty(SHOW_SQL));
    }

}
