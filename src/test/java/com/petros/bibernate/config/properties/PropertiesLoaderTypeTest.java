package com.petros.bibernate.config.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.petros.bibernate.util.TestsConstants.TEST_PROPERTIES_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertiesLoaderTypeTest {

    private PropertiesFileLoader readPropertiesFromPropFile;

    @BeforeEach
    void init() {
        readPropertiesFromPropFile =
                new PropertiesFileLoader(TEST_PROPERTIES_PATH);
    }

    @DisplayName("Check that property username is present and returned")
    @ParameterizedTest
    @CsvSource({
            "bibernate.jdbc.url, jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            "bibernate.jdbc.username, sa",
            "bibernate.jdbc.password, 'Test_Password2023#'",
    })
    void getPropertyNameThenSuccess(String propertyName, String propertyValue) {
        var actualPropertyValue = readPropertiesFromPropFile.getProperties()
                .getProperty(propertyName);
        assertEquals(actualPropertyValue, propertyValue);
    }

}
