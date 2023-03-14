package com.petros.bibernate.exception;

/**
 * The base exception type for Bibernate exceptions.
 * <p/>
 * Note that all {@link java.sql.SQLException SQLExceptions} will be wrapped in some form of
 * {@link JDBCException}.
 */
public class BibernateException extends RuntimeException {
    /**
     * Constructs a BibernateException using the given exception message.
     *
     * @param message The message explaining the reason for the exception
     */
    public BibernateException(String message) {
        super(message);
    }

    /**
     * Constructs a BibernateException using the given message and underlying cause.
     *
     * @param cause The underlying cause.
     */
    public BibernateException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a BibernateException using the given message and underlying cause.
     *
     * @param message The message explaining the reason for the exception.
     * @param cause   The underlying cause.
     */
    public BibernateException(String message, Throwable cause) {
        super(message, cause);
    }
}
