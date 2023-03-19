package com.petros.bibernate.config;

import com.petros.bibernate.config.properties.ReadPropertiesImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.petros.bibernate.util.TestsConstants.TEST_PROPERTIES_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationImplTest {

    private ReadPropertiesImpl readPropertiesFromPropFile;

    @BeforeEach
    void init() {
        readPropertiesFromPropFile =
                new ReadPropertiesImpl(TEST_PROPERTIES_PATH);
    }


    @Test
    void getPropertyNameThenSuccess(String property) {
        Properties properties = readPropertiesFromPropFile.getProperties();
        var actualProperty = properties.getProperty(property);
        assertEquals(property, actualProperty);
    }

}
