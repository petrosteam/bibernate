package com.petros.bibernate.config;

import com.petros.bibernate.config.properties.ReadPropertiesImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.petros.bibernate.util.TestsConstants.TEST_PROPERTIES_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationImplTest {

    private ConfigurationImpl configuration;

    @BeforeEach
    void init() {
        var readPropertiesFromPropFile =
                new ReadPropertiesImpl(TEST_PROPERTIES_PATH);
        configuration = new ConfigurationImpl(readPropertiesFromPropFile);
    }

    @Test
    void getJDBCUrlThenSuccess() {
        var expectedUsername = "jdbc:h2:mem:test";
        var actualUsername = configuration.getUrl();
        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    void getDriverNameThenSuccess() {
        var expectedDriverName = "java.sql.Driver";
        var actualDriverName = configuration.getDriverName();
        assertEquals(expectedDriverName, actualDriverName);
    }


}
