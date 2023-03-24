package com.petros.bibernate.session.context;

import com.petros.bibernate.session.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityKeyTest {

    @Test
    @DisplayName("Check that creation from static method works as constructor")
    public void testPersistenceContextCache() {
        var entityKeyDefault = new EntityKey(Product.class, 1);
        var entityKeyFromOfMethod = EntityKey.of(Product.class, 1);
        assertEquals(entityKeyDefault, entityKeyFromOfMethod);
    }
}