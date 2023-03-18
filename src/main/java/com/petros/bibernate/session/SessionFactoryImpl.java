package com.petros.bibernate.session;

import com.petros.bibernate.datasource.BibernateDataSource;
import com.petros.bibernate.exception.BibernateException;

import javax.sql.DataSource;

public class SessionFactoryImpl implements SessionFactory {

    public static final String DATABASE_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;";
    public static final String DATABASE_USERNAME = "sa";
    public static final String DATABASE_PASSWORD = "";
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
        return new BibernateDataSource(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
    }
}
