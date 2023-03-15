package com.petros.bibernate.session;

import com.petros.bibernate.exception.BibernateException;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public class SessionFactoryImpl implements SessionFactory {

    private final DataSource dataSource;

    public SessionFactoryImpl() {
        this.dataSource = initializeDataSource();
    }

    @Override
    public Session openSession() {
        return new SessionImpl(dataSource);
    }

    @Override
    public void close() throws BibernateException {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    private static DataSource initializeDataSource() {
        JdbcDataSource datasource = new JdbcDataSource();
        datasource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        datasource.setUser("sa");
        datasource.setPassword("");
        return datasource;
    }
}
