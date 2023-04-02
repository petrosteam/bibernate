package com.petros.bibernate.action;

import com.petros.bibernate.session.context.PersistenceContext;

import java.sql.Connection;

/**
 * This interface represents an action to be taken on an entity during a database transaction.
 * It provides methods for executing the action and setting its priority.
 * An EntityAction object is typically used by an ORM framework to manage the persistence of an entity in the database.
 */
public interface EntityAction {

    /**
     * Execute the entity action.
     * This method is used to execute the action on the entity using the given database connection and persistence context.
     *
     * @param connection the database connection to use for executing the action
     * @param persistenceContext the persistence context to use for managing the entity
     */
    void execute(Connection connection, PersistenceContext persistenceContext);

    /**
     * Get the priority of the entity action.
     * This method is used to determine the priority of the entity action, which is used to order multiple actions on the same entity during a transaction.
     *
     * @return the priority of the entity action
     */
    int priority();
}
