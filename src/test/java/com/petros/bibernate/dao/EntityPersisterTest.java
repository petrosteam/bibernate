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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        Flyway flyway = Flyway.configure().dataSource(dataSource)
                .locations("classpath:db/migration/product-test-data").load();
        flyway.clean();
        flyway.migrate();
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
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0, 0), product.getCreatedAt());
        assertTrue(product.getIsAvailable());
        assertEquals(100, product.getStockCount());
        assertEquals(3.0, product.getWeight());
        assertEquals("Play Station console", product.getDescription());
        assertEquals(LocalDate.of(2023, 1, 10), product.getSaleDate());
        assertEquals(LocalTime.of(9, 0, 0), product.getSaleTime());
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
        product.setCreatedAt(LocalDateTime.of(2023, 1, 4, 12, 0, 0));
        product.setIsAvailable(true);
        product.setStockCount(200);
        product.setWeight(0.88);
        product.setDescription("Nintendo Switch console");
        product.setSaleDate(LocalDate.of(2023, 1, 25));
        product.setSaleTime(LocalTime.of(9, 0, 0));

        Product insertedProduct = entityPersister.insert(product);

        assertNotNull(insertedProduct.getId());
        assertEquals(4L, insertedProduct.getId());
        assertEquals(product, insertedProduct);

        // Find the inserted product by ID
        Product foundProduct = entityPersister.findById(Product.class, insertedProduct.getId());

        // Check if the retrieved product is not null
        assertNotNull(foundProduct);

        // Compare all the fields of the original product and the retrieved product
        assertEquals(product.getId(), foundProduct.getId());
        assertEquals(product.getProductName(), foundProduct.getProductName());
        assertEquals(product.getProducer(), foundProduct.getProducer());
        assertEquals(product.getPrice(), foundProduct.getPrice());
        assertEquals(product.getCreatedAt(), foundProduct.getCreatedAt());
        assertEquals(product.getIsAvailable(), foundProduct.getIsAvailable());
        assertEquals(product.getStockCount(), foundProduct.getStockCount());
        assertEquals(product.getWeight(), foundProduct.getWeight());
        assertEquals(product.getDescription(), foundProduct.getDescription());
        assertEquals(product.getSaleDate(), foundProduct.getSaleDate());
        assertEquals(product.getSaleTime(), foundProduct.getSaleTime());
    }

    @Test
    public void testInsertNullEntity() {
        assertThrows(NullPointerException.class, () -> entityPersister.insert(null));
    }

    @Test
    @DisplayName("Test the update method")
    public void testUpdate() {

        Product product = new Product();
        product.setProductName("Nintendo Switch");
        product.setProducer("Nintendo");
        product.setPrice(BigDecimal.valueOf(29900, 2));

        entityPersister.insert(product);

        product.setProductName("Updated Product Name");
        product.setProducer("Updated Producer");
        product.setPrice(BigDecimal.valueOf(88800, 2));

        entityPersister.update(product);

        Product updatedProduct = entityPersister.findById(Product.class, product.getId());
        assertNotNull(updatedProduct);
        assertEquals(product.getId(), updatedProduct.getId());
        assertEquals(product.getProductName(), updatedProduct.getProductName());
        assertEquals(product.getProducer(), updatedProduct.getProducer());
        assertEquals(product.getPrice(), updatedProduct.getPrice());
    }

    @Test
    @DisplayName("Test the update method with a non-existing entity")
    public void testUpdateNonExistingEntity() {
        Product product = new Product();
        product.setId(4L);
        product.setProductName("Nintendo Switch");
        product.setProducer("Nintendo");
        product.setPrice(BigDecimal.valueOf(29900, 2));

        assertThrows(BibernateException.class, () -> entityPersister.update(product), "Failed to update entity in the" +
                " database");
    }

    @Test
    @DisplayName("Test the update method with null entity")
    public void testUpdateWithNullEntity() {
        assertThrows(NullPointerException.class, () -> entityPersister.update(null));
    }

    @Test
    @DisplayName("Test the update method with entity with null @Id field")
    public void testUpdateEntityWithNullId() {
        Product product = new Product();
        product.setProductName("Nintendo Switch");
        product.setProducer("Nintendo");
        product.setPrice(BigDecimal.valueOf(29900, 2));

        assertThrows(BibernateException.class, () -> entityPersister.update(product), "ID field is null");
    }

    @Test
    @DisplayName("Test the delete method with an existing entity")
    public void testDelete() {
        Product product = entityPersister.findById(Product.class, 1L);

        assertNotNull(product);
        entityPersister.delete(product);

        Product deletedProduct = entityPersister.findById(Product.class, 1L);
        assertNull(deletedProduct);
    }

    @Test
    @DisplayName("Test the delete method with a non-existing entity")
    public void testDeleteNonExistingEntity() {
        Product product = new Product();
        product.setId(5L);
        product.setProductName("Non-existing Product");
        product.setProducer("Non-existing Producer");
        product.setPrice(BigDecimal.valueOf(99900, 2));

        assertThrows(BibernateException.class, () -> entityPersister.delete(product), "Failed to delete entity from " +
                "the database");
    }

    @Test
    @DisplayName("Test the delete method with null entity")
    public void testDeleteWithNullEntity() {
        assertThrows(NullPointerException.class, () -> entityPersister.delete(null));
    }

    @Test
    @DisplayName("Test the delete method with entity with null @Id field")
    public void testDeleteEntityWithNullId() {
        Product product = new Product();
        product.setProductName("Nintendo Switch");
        product.setProducer("Nintendo");
        product.setPrice(BigDecimal.valueOf(29900, 2));

        assertThrows(BibernateException.class, () -> entityPersister.delete(product), "ID field is null");
    }
}
