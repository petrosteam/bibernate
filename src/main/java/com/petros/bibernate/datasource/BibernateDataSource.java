package com.petros.bibernate.datasource;


import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * The BibernateDataSource class provides a custom implementation of the DataSource interface for use in a Bibernate
 * framework.
 * <p>
 * The class implements the DataSource interface and provides the required methods for obtaining a Connection object.
 */
@Slf4j
public class BibernateDataSource implements DataSource {
    private final BibernateConnectionPool connectionPool;


    public BibernateDataSource(String url, String username, String password, int connectionPoolSize) {
        this.connectionPool = new BibernateConnectionPool(url, username, password, connectionPoolSize);
    }

    public void close() {
        connectionPool.close();
        log.trace("Connection pool has been closed");
    }

    @Override
    public Connection getConnection() {
        log.trace("Connection retrieved from the connection pool.");
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
    public int getLoginTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
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
}
