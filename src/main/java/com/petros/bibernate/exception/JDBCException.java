package com.petros.bibernate.exception;

import java.sql.SQLException;

/**
 * Wraps a {@link SQLException}.  Indicates that an exception occurred during a JDBC call.
 *
 * @see SQLException
 */
public class JDBCException extends BibernateException {
    private final SQLException sqlException;
    private final String sql;

    /**
     * Constructs a JDBCException using the given information.
     *
     * @param message The message explaining the exception condition
     * @param cause   The underlying cause
     */
    public JDBCException(String message, SQLException cause) {
        this(message, cause, null);
    }

    /**
     * Constructs a JDBCException using the given information.
     *
     * @param message The message explaining the exception condition
     * @param cause   The underlying cause
     * @param sql     The sql being executed when the exception occurred
     */
    public JDBCException(String message, SQLException cause, String sql) {
        super(message, cause);
        this.sqlException = cause;
        this.sql = sql;
    }

    /**
     * Get the X/Open or ANSI SQL SQLState error code from the underlying {@link SQLException}.
     *
     * @return The X/Open or ANSI SQL SQLState error code; may return null.
     * @see SQLException#getSQLState()
     */
    public String getSQLState() {
        return sqlException.getSQLState();
    }

    /**
     * Get the vendor specific error code from the underlying {@link SQLException}.
     *
     * @return The vendor specific error code
     * @see SQLException#getErrorCode()
     */
    public int getErrorCode() {
        return sqlException.getErrorCode();
    }

    /**
     * Get the underlying {@link SQLException}.
     *
     * @return The SQLException
     */
    public SQLException getSQLException() {
        return sqlException;
    }

    /**
     * Get the actual SQL statement being executed when the exception occurred.
     *
     * @return The SQL statement; may return null.
     */
    public String getSQL() {
        return sql;
    }
}
