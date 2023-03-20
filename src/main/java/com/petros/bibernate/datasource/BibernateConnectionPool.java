package com.petros.bibernate.datasource;

import com.petros.bibernate.exception.BibernateException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BibernateConnectionPool {
    private final ConcurrentLinkedQueue<BibernateConnection> pool;
    private final int CONNECTION_MAX_SIZE = 10;

    public BibernateConnectionPool(String url, String username, String password) {
        pool = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < CONNECTION_MAX_SIZE; i++) {
            try {
                Connection connection = DriverManager.getConnection(url, username, password);
                pool.add(new BibernateConnection(connection, pool));
            } catch (SQLException e) {
                throw new BibernateException(e);
            }
        }
    }

    public Connection getConnection() {
        return pool.poll();
    }
}
