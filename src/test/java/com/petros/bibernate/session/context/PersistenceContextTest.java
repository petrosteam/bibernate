package com.petros.bibernate.session.context;

import com.petros.bibernate.session.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceContextTest {
    private PersistenceContext persistenceContext;

    private static Product getProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setProductName("Default Product");
        product.setProducer("Default Producer");
        product.setPrice(BigDecimal.valueOf(29900, 2));
        product.setCreatedAt(LocalDateTime.of(2023, 1, 4, 12, 0, 0));
        product.setIsAvailable(true);
        product.setStockCount(200);
        product.setWeight(0.88);
        product.setDescription("Default Product Description");
        product.setSaleDate(LocalDate.of(2023, 1, 25));
        product.setSaleTime(LocalTime.of(9, 0, 0));
        return product;
    }

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
    @DisplayName("If entity has changed field it's added to snapshot")
    void testEntitySnapshot() {
        var product = getProduct();
        persistenceContext.cache(product);
        product.setProductName("Certain product");
        List<Object> diff = persistenceContext.getSnapshotDiff();
        assertFalse(diff.isEmpty());
        assertEquals(1, diff.size());
        assertEquals(product, diff.get(0));
    }

    @Test
    @DisplayName("If entity has changed field it's added to snapshot")
    void testEntitySnapshotForManyObjects() {
        var product = getProduct();
        var product2 = getProduct();
        product2.setId(2L);
        product2.setProductName("Constant Product");

        persistenceContext.cache(product);
        persistenceContext.cache(product2);
        product.setProductName("Certain product");

        persistenceContext.cache(product);
        persistenceContext.cache(product2);
        product.setPrice(BigDecimal.TEN);
        List<Object> diff = persistenceContext.getSnapshotDiff();
        assertFalse(diff.isEmpty());
        assertEquals(1, diff.size());
        assertEquals(product, diff.get(0));
    }

    @Test
    @DisplayName("Diff is empty if snapshot is empty")
    void testEmptySnapshot() {
        List<Object> diff = persistenceContext.getSnapshotDiff();
        assertTrue(diff.isEmpty());
    }
}