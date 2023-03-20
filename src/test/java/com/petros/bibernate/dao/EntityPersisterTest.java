package com.petros.bibernate.dao;

import com.petros.bibernate.datasource.BibernateDataSource;
import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.session.model.Product;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EntityPersisterTest {
    public static final String DATABASE_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    public static final String DATABASE_USERNAME = "sa";
    public static final String DATABASE_PASSWORD = "";

    // TODO: 18.03.2023 After Configuration class created, grab properties from file
//    public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/postgres";
//    public static final String DATABASE_USERNAME = "postgres";
//    public static final String DATABASE_PASSWORD = "123qwe";
    private EntityPersister entityPersister;

    @BeforeEach
    public void setUp() {
        // Create a database and run the Flyway migration
        DataSource dataSource = new BibernateDataSource(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration/product-test-data").load().migrate();
        // Initialize the EntityPersister with the H2 data source
        entityPersister = new EntityPersister(dataSource);
    }

    @Test
    @DisplayName("Test the findById method with a known entity ID")
    public void testFindById() {
        Product product = entityPersister.findById(Product.class, 1L);

        assertNotNull(product);
        assertEquals(1L, product.getId());
        assertEquals("Play Station", product.getProductName());
        assertEquals("Sony", product.getProducer());
        assertEquals(BigDecimal.valueOf(24900, 2), product.getPrice());
    }

    @Test
    @DisplayName("Test the findOne method with a known entity field and value")
    public void testFindOne() throws NoSuchFieldException {
        Product product = entityPersister.findOne(Product.class, Product.class.getDeclaredField("id"), 2);

        assertNotNull(product);
        assertEquals(2L, product.getId());
        assertEquals("XBox", product.getProductName());
        assertEquals("Microsoft", product.getProducer());
        assertEquals(BigDecimal.valueOf(21500, 2), product.getPrice());
    }

    @Test
    @DisplayName("Test the findOne method with non-existed entity")
    public void testFindOneNotFound() throws NoSuchFieldException {
        Product product = entityPersister.findOne(Product.class, Product.class.getDeclaredField("id"), 8);

        assertNull(product);
    }

    @Test
    @DisplayName("Test the findOne method with a field that return several products")
    public void testFindOneReturnSeveral() {
        assertThrows(BibernateException.class, () -> entityPersister.findOne(Product.class,
                Product.class.getDeclaredField("producer"), "Sony"));
    }

    @Test
    @DisplayName("Test the findAll method with a known entity field and value")
    public void testFindAll() throws NoSuchFieldException {
        List<Product> products = entityPersister
                .findAll(Product.class, Product.class.getDeclaredField("producer"), "Sony");
        assertEquals(2, products.size());
    }

    @Test
    @DisplayName("Test the insert method with a new entity")
    public void testInsertNewEntity() {
        Product product = new Product();
        product.setProductName("Nintendo Switch");
        product.setProducer("Nintendo");
        product.setPrice(BigDecimal.valueOf(29900, 2));

        Product insertedProduct = entityPersister.insert(product);

        assertNotNull(insertedProduct.getId());
        assertEquals(4L, insertedProduct.getId());
        assertEquals(product, insertedProduct);
    }

    @Test
    public void testInsertNullEntity() {
        assertThrows(NullPointerException.class, () -> entityPersister.insert(null));
    }

}
