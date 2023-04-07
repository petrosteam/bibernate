package com.petros.bibernate.session;

import lombok.extern.slf4j.Slf4j;

/**
 * The entry point of the application. Persistence creates a SessionFactory.
 * Example:
 * <pre>{@code
 *      ...
 *      public static void main(String[] args) {
 *          SessionFactory sessionFactory = Persistence.createSessionFactory();
 *          Session session = sessionFactory.openSession();
 *          ...
 *      }
 *      ...
 * }</pre>
 */
@Slf4j
public class Persistence {
    /**
     * Creates a SessionFactory with default configuration.
     *
     * @return a SessionFactory instance
     */
    public static SessionFactory createSessionFactory() {
        log.info("Creating SessionFactory with default configuration.");
        return new SessionFactoryImpl();
    }

    /**
     * Creates a SessionFactory with the given configuration file path.
     *
     * @param configPath the path of the configuration file
     * @return a SessionFactory instance
     */
    public static SessionFactory createSessionFactory(String configPath) {
        log.info("Creating SessionFactory with configuration file path: {}", configPath);
        return new SessionFactoryImpl(configPath);
    }

    /**
     * Creates a SessionFactory with the given connection parameters.
     *
     * @param url      the JDBC URL
     * @param username the database username
     * @param password the database password
     * @return a SessionFactory instance
     */
    public static SessionFactory createSessionFactory(String url, String username, String password) {
        log.info("Creating SessionFactory with connection details. URL: {}, username: {}, password: {}", url,
                username, password);
        return new SessionFactoryImpl(url, username, password);
    }
}
