package com.petros.bibernate.datasource;

import com.petros.bibernate.exception.BibernateException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A connection pool for Bibernate that maintains a queue of {@link BibernateConnection} instances.
 */
@Slf4j
public class BibernateConnectionPool {
    private final Queue<BibernateConnection> pool;

    /**
     * Constructs a new connection pool with the specified parameters.
     *
     * @param url                the JDBC URL to connect to
     * @param username           the username to use when connecting
     * @param password           the password to use when connecting
     * @param connectionPoolSize the size of the connection pool
     */
    public BibernateConnectionPool(String url, String username, String password, int connectionPoolSize) {
        log.info("Connection pool is going to be created");
        pool = new LinkedBlockingQueue<>();
        log.info("{} new connections will be created", connectionPoolSize);
        for (int i = 0; i < connectionPoolSize; i++) {
            try {
                Connection connection = DriverManager.getConnection(url, username, password);
                pool.add(new BibernateConnection(connection, pool));
                log.info("New physical connection has been created and stored into pool");
            } catch (SQLException e) {
                log.error("Could not create physical connection", e);
                throw new BibernateException(e);
            }
        }
    }

    /**
     * Gets a connection from the pool.
     */
    public Connection getConnection() {
        return pool.poll();
    }

    /**
     * Closes all connections in the pool.
     */
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
}
