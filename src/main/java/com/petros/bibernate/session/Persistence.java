package com.petros.bibernate.session;

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
public class Persistence {

    public static SessionFactory createSessionFactory() {
        return new SessionFactoryImpl();
    }

    public static SessionFactory createSessionFactory(String configPath) {
        return new SessionFactoryImpl(configPath);
    }

    public static SessionFactory createSessionFactory(String url, String username, String password) {
        return new SessionFactoryImpl(url, username, password);
    }
}
