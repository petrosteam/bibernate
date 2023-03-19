package com.petros.bibernate.config.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.petros.bibernate.util.TestsConstants.TEST_PROPERTIES_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReadPropertiesTest {

    private ReadPropertiesImpl readPropertiesFromPropFile;

    @BeforeEach
    void init() {
        readPropertiesFromPropFile =
                new ReadPropertiesImpl(TEST_PROPERTIES_PATH);
    }

    @DisplayName("Check that property username is present and returned")
    @ParameterizedTest
    @CsvSource({
            "username, username",
            "password, password",
            "driver-name, java.sql.Driver",
            "jdbc-url, jdbc:h2:mem:test",
            "port, 111",
            "database-name, test",
            "hostname, localhost"
    })
    void getPropertyNameThenSuccess(String propertyName, String propertyValue) {
        var actualPropertyValue = readPropertiesFromPropFile.getProperties()
                .getProperty(propertyName);
        assertEquals(actualPropertyValue, propertyValue);
    }

}
