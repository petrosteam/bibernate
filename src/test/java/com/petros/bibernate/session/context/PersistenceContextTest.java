package com.petros.bibernate.session.context;

import com.petros.bibernate.session.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceContextTest {
    private PersistenceContext persistenceContext;


    @BeforeEach
    public void setUp() {
        this.persistenceContext = new PersistenceContextImpl();
    }

    @Test
    @DisplayName("Getting cached entity from persistence context")
    void getCachedEntityTest() {
        var defaultProduct = new Product();
        defaultProduct.setId(1L);

        persistenceContext.cache(defaultProduct);
        var cachedEntity = persistenceContext.getCachedEntity(Product.class, 1);
        cachedEntity.ifPresent(cachedProduct -> assertSame(cachedProduct, defaultProduct));

        var nonExistingEntity = persistenceContext.getCachedEntity(Product.class, 2);
        assertFalse(nonExistingEntity.isPresent());
    }

    @Test
    @DisplayName("Cache entity test")
    void cache() {
        var defaultProduct = new Product();
        defaultProduct.setId(1L);
        var productOptional = persistenceContext.getCachedEntity(Product.class, 1L);
        assertFalse(productOptional.isPresent());

        persistenceContext.cache(defaultProduct);
        productOptional = persistenceContext.getCachedEntity(Product.class, 1L);
        assertTrue(productOptional.isPresent());

        productOptional.ifPresent(product -> assertSame(product, defaultProduct));
    }

    @Test
    @DisplayName("Cache is empty after persistence context is cleared")
    void clear() {
        var defaultProduct = new Product();
        defaultProduct.setId(1L);
        persistenceContext.cache(defaultProduct);
        persistenceContext.clear();
        var productOptional = persistenceContext.getCachedEntity(Product.class, 1L);
        assertFalse(productOptional.isPresent());
    }

    @Test
    @DisplayName("Testing entity snapshots")
    void testEntitySnapshot() {
       // TODO: add tests after implementing dirty checking feature
    }
}