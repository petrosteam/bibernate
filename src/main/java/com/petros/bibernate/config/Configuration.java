package com.petros.bibernate.config;

/**
 * The Configuration interface defines methods for accessing configuration parameters
 * such as properties, username, password, and URL.
 *
 * @see com.petros.bibernate.config.properties.PropertiesLoader
 */
public interface Configuration {

    String JDBC_URL = "bibernate.jdbc.url";
    String JDBC_USERNAME = "bibernate.jdbc.username";
    String JDBC_PASSWORD = "bibernate.jdbc.password";
    String JDBC_POOL_SIZE = "bibernate.jdbc.connection-pool.size";

    /**
     * Retrieves the value of the configuration parameter associated with the specified key.
     *
     * @param key the key of the parameter to retrieve
     * @return string with value param
     */
    String getProperty(String key);

    /**
     * Retrieves the username used for authentication to the database.
     *
     * @return username value from file properties
     */
    String getUsername();

    /**
     * Retrieves the password used for authentication to the database.
     *
     * @return password value from file properties
     */
    String getPassword();

    /**
     * Retrieves the JDBC_URL used for accessing to the database.
     *
     * @return jdbc_url format value from file properties
     */
    String getUrl();

}
