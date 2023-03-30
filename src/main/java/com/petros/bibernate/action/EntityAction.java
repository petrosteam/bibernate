package com.petros.bibernate.action;

import com.petros.bibernate.session.context.PersistenceContext;

import java.sql.Connection;

public interface EntityAction {

    void execute(Connection connection, PersistenceContext persistenceContext);

    int priority();
}
