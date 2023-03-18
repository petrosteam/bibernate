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

    private static SessionFactory sessionFactory;

    public static synchronized SessionFactory createSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = new SessionFactoryImpl();
        }
        return sessionFactory;
    }
}
