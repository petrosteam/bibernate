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
        log.trace("Getting property with key: {}", key);
        return ofNullable(properties.getProperty(key));
    }

    @Override
    public String getUsername() {
        log.trace("Getting username property");
        return getProperty(JDBC_USERNAME).orElseThrow(() -> handlePropertyNotFoundException(JDBC_USERNAME));
    }

    @Override
    public String getPassword() {
        log.trace("Getting password property");
        return getProperty(JDBC_PASSWORD).orElseThrow(() -> handlePropertyNotFoundException(JDBC_PASSWORD));
    }

    @Override
    public Integer getConnectionPoolSize() {
        log.trace("Getting connection pool size property");
        return Integer.parseInt(getProperty(JDBC_POOL_SIZE).orElseGet(() -> {
            log.info("Property {} is not set, default value will be used: {}", JDBC_POOL_SIZE,
                    DEFAULT_CONNECTION_POOL_SIZE);
            return String.valueOf(DEFAULT_CONNECTION_POOL_SIZE);
        }));
    }

    @Override
    public String getUrl() {
        log.trace("Getting URL property");
        return getProperty(JDBC_URL).orElseThrow(() -> handlePropertyNotFoundException(JDBC_URL));
    }

    @Override
    public boolean showSql() {
        log.trace("Getting show SQL property");
        return Boolean.parseBoolean(properties.getProperty(SHOW_SQL));
    }

    private BibernateException handlePropertyNotFoundException(String propertyName) {
        String errorMessage = String.format("Property %s must be set", propertyName);
        log.error(errorMessage);
        return new BibernateException(errorMessage);
    }
}
