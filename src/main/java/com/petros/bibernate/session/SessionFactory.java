package com.petros.bibernate.session;

import com.petros.bibernate.exception.BibernateException;

/**
 * The main contract here is the creation of {@link Session} instances.  Usually
 * an application has a single {@link SessionFactory} instance and threads
 * servicing client requests obtain {@link Session} instances from this factory.
 * <p/>
 * The internal state of a {@link SessionFactory} is immutable.  Once it is created
 * this internal state is set.  This internal state includes all of the metadata
 * about Object/Relational Mapping.
 * <p/>
 * Implementors <strong>must</strong> be threadsafe.
 */
public interface SessionFactory extends AutoCloseable {

    /**
     * Open a {@link Session}.
     * <p/>
     *
     * @return The created session.
     * @throws com.petros.bibernate.exception.BibernateException Indicates a problem opening the session; pretty rare
     * here.
     */
    Session openSession();

    /**
     * Destroy this <tt>SessionFactory</tt> and release all resources (caches,
     * connection pools, etc).
     * <p/>
     * It is the responsibility of the application to ensure that there are no
     * open {@link Session sessions} before calling this method as the impact
     * on those {@link Session sessions} is indeterminate.
     * <p/>
     * No-ops if already {@link #isClosed closed}.
     *
     * @throws com.petros.bibernate.exception.BibernateException Indicates an issue closing the factory.
     */
    void close() throws BibernateException;

    /**
     * Is this factory already closed?
     *
     * @return True if this factory is already closed; false otherwise.
     */
    boolean isClosed();
}
