package com.petros.bibernate.session;

import com.petros.bibernate.exception.BibernateException;

import java.util.List;

/**
 * The main runtime interface between a Java application and Bibernate. This is the
 * central API class abstracting the notion of a persistence service.<br>
 * <br>
 * The lifecycle of a <tt>Session</tt> is bounded by the beginning and end of a logical
 * transaction.<br>
 * <br>
 * The main function of the <tt>Session</tt> is to offer create, read and delete operations
 * for instances of mapped entity classes. Instances may exist in one of three states:<br>
 * <br>
 * <i>transient:</i> never persistent, not associated with any <tt>Session</tt><br>
 * <i>persistent:</i> associated with a unique <tt>Session</tt><br>
 * <i>detached:</i> previously persistent, not associated with any <tt>Session</tt><br>
 * <br>
 * <p>
 * If the <tt>Session</tt> throws an exception, the transaction must be rolled back
 * and the session discarded.
 *
 * @see SessionFactory
 */
public interface Session {
    /**
     * Force this session to flush. Must be called at the end of a
     * unit of work, before committing the transaction and closing the
     * session.
     * <p/>
     * <i>Flushing</i> is the process of synchronizing the underlying persistent
     * store with persistable state held in memory.
     *
     * @throws BibernateException Indicates problems flushing the session or
     *                            talking to the database.
     */
    void flush() throws BibernateException;

    /**
     * Make a transient instance persistent.
     * <p/>
     *
     * @param object a transient instance to be made persistent
     */
    <T> void persist(T object);

    /**
     * Find by primary key.
     * Search for an entity of the specified class and primary key.
     * If the entity instance is contained in the persistence context,
     * it is returned from there.
     *
     * @param entityClass entity class
     * @param primaryKey  primary key
     * @return the found entity instance or null if the entity does
     * not exist
     */
    <T> T find(Class<T> entityClass, Object primaryKey);

    /**
     * Find by entity class.
     * Search for all entities of the specified class.
     * It does not check persistence context but calls database directly.
     *
     * @param entityClass entity class
     * @return the found entity instances or an empty list
     */
    <T> List<T> findAll(Class<T> entityClass);

    /**
     * Remove the entity instance.
     *
     * @param entity entity instance
     * @throws IllegalArgumentException if the instance is not an
     *                                  entity or is a detached entity
     */
    <T> void remove(T entity);

    /**
     * End the session by releasing the JDBC connection and cleaning up.
     *
     * @throws BibernateException Indicates problems cleaning up.
     */
    void close();

    /**
     * Removes all entities from persistence context and action queue
     *
     */
    void clear();

    /**
     * Creates a new transaction or returns an existing one
     *
     * @return transaction instance
     */
    Transaction getTransaction();

}
