package com.petros.bibernate.datasource;

import com.petros.bibernate.config.Configuration;
import com.petros.bibernate.config.ConfigurationImpl;
import com.petros.bibernate.exception.BibernateException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.petros.bibernate.config.Configuration.*;

@Slf4j
public class BibernateConnectionPool {
    private final Configuration configuration;
    private final ConcurrentLinkedQueue<BibernateConnection> pool;
    private static final int CONNECTION_MAX_SIZE = 10;

    public BibernateConnectionPool(String configPath) {
        log.info("Connection pool is going to be created");
        configuration = new ConfigurationImpl(configPath);
        pool = new ConcurrentLinkedQueue<>();
        int connectionPoolSize = getConnectionPoolSize();
        log.info("{} new connections will be created", connectionPoolSize);
        for (int i = 0; i < connectionPoolSize; i++) {
            try {
                Connection connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
                pool.add(new BibernateConnection(connection, pool));
                log.info("New physical connection has been created and stored into pool");
            } catch (SQLException e) {
                log.error("Could not create physical connection", e);
                throw new BibernateException(e);
            }
        }
    }

    public Connection getConnection() {
        return pool.poll();
    }

    public void close() {
        log.info("Connection pool is going to be closed");
        pool.forEach(logicConnection -> {
            try {
                logicConnection.closePhysical();
                log.info("Physical connection has been closed");
            } catch (SQLException e) {
                log.error("Could not close physical connection", e);
            }
        });
    }

    private String getUrl() {
        return Optional.ofNullable(configuration.getUrl())
                .orElseThrow(() -> new BibernateException(String.format("Property %s must be set", JDBC_URL)));
    }

    private String getUsername() {
        return Optional.ofNullable(configuration.getUsername())
                .orElseThrow(() -> new BibernateException(String.format("Property %s must be set", JDBC_USERNAME)));
    }

    private String getPassword() {
        return Optional.ofNullable(configuration.getPassword())
                .orElseThrow(() -> new BibernateException(String.format("Property %s must be set", JDBC_PASSWORD)));
    }

    private int getConnectionPoolSize() {
        try {
            return Integer.parseInt(configuration.getProperty(JDBC_POOL_SIZE));
        } catch (NumberFormatException e) {
            log.info("Property {} is not set, default value will be used: {}", JDBC_POOL_SIZE, CONNECTION_MAX_SIZE);
            return CONNECTION_MAX_SIZE;
        }
    }
}
