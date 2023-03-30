package com.petros.bibernate.session;

import com.petros.bibernate.exception.BibernateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionImpl implements Transaction {

    private final Session session;
    private boolean open = false;

    @Override
    public void begin() {
        open = true;
    }

    @Override
    public void commit() {
        requireOpenTransaction();
        open = false;
        session.flush();
    }

    @Override
    public void rollback() {
        requireOpenTransaction();
        open = false;
        session.clear();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    private void requireOpenTransaction() {
        if (!open) {
            throw new BibernateException("Transaction is closed. tx.begin() must be invoked before tx.commit() or tx.rollback()");
        }
    }
}
