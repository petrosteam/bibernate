package com.petros.bibernate.session;

import com.petros.bibernate.exception.BibernateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the Transaction interface.
 */
@Slf4j
@RequiredArgsConstructor
public class TransactionImpl implements Transaction {

    private final Session session;
    private boolean open = false;

    @Override
    public void begin() {
        log.trace("Beginning a new transaction");
        open = true;
    }

    @Override
    public void commit() {
        log.trace("Committing the transaction");
        requireOpenTransaction();
        session.flush();
        open = false;
    }

    @Override
    public void rollback() {
        log.info("Rolling back the transaction");
        requireOpenTransaction();
        open = false;
        session.clear();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    /**
     * Ensures that the transaction is open before committing or rolling back.
     */
    private void requireOpenTransaction() {
        if (!open) {
            throw new BibernateException("Transaction is closed. tx.begin() must be invoked before tx.commit() or tx" +
                    ".rollback()");
        }
    }
}
