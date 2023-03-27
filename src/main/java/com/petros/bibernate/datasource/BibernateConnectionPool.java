package com.petros.bibernate.datasource;

import com.petros.bibernate.exception.BibernateException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class BibernateConnectionPool {
    private final Queue<BibernateConnection> pool;

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
}
