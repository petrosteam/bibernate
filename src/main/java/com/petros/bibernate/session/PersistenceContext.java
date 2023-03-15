package com.petros.bibernate.session;

public class PersistenceContext {

    private static SessionFactory sessionFactory;

    public static synchronized SessionFactory createSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = new SessionFactoryImpl();
        }
        return sessionFactory;
    }
}
