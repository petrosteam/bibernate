package com.petros.bibernate.session;

import com.petros.bibernate.config.Configuration;
import com.petros.bibernate.config.ConfigurationImpl;
import com.petros.bibernate.datasource.BibernateDataSource;
import com.petros.bibernate.exception.BibernateException;

import static com.petros.bibernate.config.Configuration.DEFAULT_CONNECTION_POOL_SIZE;


public class SessionFactoryImpl implements SessionFactory {

    public Configuration getConfiguration() {
        return configuration;
    }

    private final Configuration configuration;

    private static final String DEFAULT_PROPERTIES_PATH = "src/main/resources/application.properties";


    private final BibernateDataSource dataSource;
    private boolean closed = false;

    public SessionFactoryImpl() {
        this(DEFAULT_PROPERTIES_PATH);
    }

    public SessionFactoryImpl(String configPath) {
        this.configuration = new ConfigurationImpl(configPath);
        this.dataSource = new BibernateDataSource(configuration.getUrl(), configuration.getUsername(),
                configuration.getPassword(), configuration.getConnectionPoolSize());
    }

    public SessionFactoryImpl(String url, String username, String password) {
        this.configuration = new ConfigurationImpl(DEFAULT_PROPERTIES_PATH);
        this.dataSource = new BibernateDataSource(url, username, password, DEFAULT_CONNECTION_POOL_SIZE);
    }

    @Override
    public Session openSession() {
        return new SessionImpl(dataSource, configuration);
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
