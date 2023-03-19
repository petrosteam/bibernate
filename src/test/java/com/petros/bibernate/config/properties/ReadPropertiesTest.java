package com.petros.bibernate.config.properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    @ValueSource(strings = {"username", "password"})
    void getPropertyNameThenSuccess(String property) {
        var actualPropertyName = readPropertiesFromPropFile.getProperties()
                .getProperty(property);
        assertEquals(property, actualPropertyName);
    }

}