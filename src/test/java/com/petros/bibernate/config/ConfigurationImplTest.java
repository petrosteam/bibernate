package com.petros.bibernate.config;

import com.petros.bibernate.config.properties.PropertiesFileLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.petros.bibernate.util.TestsConstants.TEST_PROPERTIES_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationImplTest {

    private ConfigurationImpl configuration;

    @BeforeEach
    void init() {
        var readPropertiesFromPropFile =
                new PropertiesFileLoader(TEST_PROPERTIES_PATH);
        configuration = new ConfigurationImpl(readPropertiesFromPropFile);
    }

    @Test
    void getJDBCUrlThenSuccess() {
        var expectedUsername = "jdbc:h2:mem:test";
        var actualUsername = configuration.getUrl();
        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    void getUsernameThenSuccess() {
        var expectedUsername = "username";
        var actualUsername = configuration.getUsername();
        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    void getPasswordThenSuccess() {
        var expectedPassword = "password";
        var actualPassword = configuration.getPassword();
        assertEquals(expectedPassword, actualPassword);
    }


}
