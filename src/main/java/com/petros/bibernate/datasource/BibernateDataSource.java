package com.petros.bibernate.datasource;

import com.petros.bibernate.config.Configuration;
import com.petros.bibernate.config.ConfigurationImpl;
import com.petros.bibernate.exception.BibernateException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static com.petros.bibernate.config.Configuration.*;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Slf4j
public class BibernateDataSource implements DataSource {
    private Configuration configuration;
    private final BibernateConnectionPool connectionPool;
    private static final int CONNECTION_POOL_SIZE = 10;
    private static final String DEFAULT_PROPERTIES_PATH = "src/main/resources/application.properties";

    public BibernateDataSource() {
        this(DEFAULT_PROPERTIES_PATH);
    }

    public BibernateDataSource(String configPath) {
        this.configuration = new ConfigurationImpl(configPath);
        this.connectionPool = new BibernateConnectionPool(getUrl(), getUsername(), getPassword(), getConnectionPoolSize());
    }

    public BibernateDataSource(String url, String username, String password) {
        this.connectionPool = new BibernateConnectionPool(url, username, password, CONNECTION_POOL_SIZE);
    }

    public void close() {
        connectionPool.close();
    }

    @Override
    public Connection getConnection() {
        return connectionPool.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    private String getUrl() {
        requireNonNull(configuration);
        return ofNullable(configuration.getUrl())
                .orElseThrow(() -> new BibernateException(String.format("Property %s must be set", JDBC_URL)));
    }

    private String getUsername() {
        requireNonNull(configuration);
        return ofNullable(configuration.getUsername())
                .orElseThrow(() -> new BibernateException(String.format("Property %s must be set", JDBC_USERNAME)));
    }

    private String getPassword() {
        requireNonNull(configuration);
        return ofNullable(configuration.getPassword())
                .orElseThrow(() -> new BibernateException(String.format("Property %s must be set", JDBC_PASSWORD)));
    }

    private int getConnectionPoolSize() {
        try {
            return Integer.parseInt(configuration.getProperty(JDBC_POOL_SIZE));
        } catch (Exception e) {
            log.info("Property {} is not set, default value will be used: {}", JDBC_POOL_SIZE, CONNECTION_POOL_SIZE);
            return CONNECTION_POOL_SIZE;
        }
    }
}
