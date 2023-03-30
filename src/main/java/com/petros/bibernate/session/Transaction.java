package com.petros.bibernate.session;

public interface Transaction {

    void begin();

    void commit();

    void rollback();

    boolean isOpen();
}
