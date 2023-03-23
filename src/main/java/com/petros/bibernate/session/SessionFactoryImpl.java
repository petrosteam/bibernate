package com.petros.bibernate.session;

import com.petros.bibernate.datasource.BibernateDataSource;
import com.petros.bibernate.exception.BibernateException;

public class SessionFactoryImpl implements SessionFactory {

    private final BibernateDataSource dataSource;
    private boolean closed = false;

    public SessionFactoryImpl() {
        this.dataSource = new BibernateDataSource();
    }

    @Override
    public Session openSession() {
        return new SessionImpl(dataSource);
    }

    @Override
    public void close() throws BibernateException {
        closed = true;
        dataSource.close();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

}
