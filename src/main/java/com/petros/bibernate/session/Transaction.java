package com.petros.bibernate.session;

import com.petros.bibernate.exception.BibernateException;

/**
 * This interface represents a database transaction.
 * It provides methods for beginning, committing, and rolling back the transaction, as well as checking its status.
 * A Transaction object is typically obtained from a Session object, and is used to manage a single unit of work in the database.
 */
public interface Transaction {

    /**
     * Begin a transaction.
     * This method marks a transaction as open.
     * If a transaction is already in progress, this method will have no effect.
     *
     */
    void begin();

    /**
     * Commit the current transaction.
     * This method is used to commit the changes made during the transaction to the underlying database.
     *
     * @throws BibernateException if the transaction is not opened
     */
    void commit();

    /**
     * Rollback the current transaction.
     * This method is used to undo the changes made during the transaction and return the database to its previous state.
     *
     * @throws BibernateException if the transaction is not opened
     */
    void rollback();

    /**
     * Check if a transaction is currently in progress.
     *
     * @return true if a transaction is in progress, false otherwise
     */
    boolean isOpen();
}
