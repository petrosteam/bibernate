package com.petros.bibernate.config;

import java.util.Optional;

/**
 * This interface defines the configuration properties for a Bibernate application.
 */
public interface Configuration {

    /**
     * The key for the JDBC URL property.
     */
    String JDBC_URL = "bibernate.jdbc.url";

    /**
     * The key for the JDBC username property.
     */
    String JDBC_USERNAME = "bibernate.jdbc.username";

    /**
     * The key for the JDBC password property.
     */
    String JDBC_PASSWORD = "bibernate.jdbc.password";

    /**
     * The key for the JDBC connection pool size property.
     */
    String JDBC_POOL_SIZE = "bibernate.jdbc.connection-pool.size";

    /**
     * The key for the "show SQL" property.
     */
    String SHOW_SQL = "bibernate.show-sql";

    /**
     * The default connection pool size.
     */
    int DEFAULT_CONNECTION_POOL_SIZE = 10;


    /**
     * Gets the value of the specified property.
     *
     * @param key the property key
     * @return an optional containing the property value, or an empty optional if the property is not set
     */
    Optional<String> getProperty(String key);

    /**
     * Gets the JDBC username.
     *
     * @return the JDBC username
     */
    String getUsername();

    /**
     * Gets the JDBC password.
     *
     * @return the JDBC password
     */
    String getPassword();

    /**
     * Gets the connection pool size.
     *
     * @return the connection pool size, or the default connection pool size if the property is not set
     */
    Integer getConnectionPoolSize();

    /**
     * Gets the JDBC URL.
     *
     * @return the JDBC URL
     */
    String getUrl();

    /**
     * Checks whether to write all SQL statements to the console.
     *
     * @return true if SQL statements should be shown in the console, false otherwise
     */
    boolean showSql();

}
