package com.petros.bibernate.dao;

import com.petros.bibernate.datasource.BibernateDataSource;
import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.session.model.Product;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("ci-server")
@Testcontainers
public class EntityPersisterTest {
    private static final String TEST_DATABASE_NAME = "test_db";
    private static final String TEST_USERNAME = "sa";
    private static final String TEST_PASSWORD = "Test_Password2023#";

    private EntityPersister entityPersister;
    private BibernateDataSource dataSource;

    @Container
    private static final MySQLContainer<?> MYSQL_CONTAINER = createMySQLContainer();
    @Container
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = createPostgreSQLContainer();
    @Container
    private static final MSSQLServerContainer<?> MSSQL_CONTAINER = createMSSQLContainer();

    private static MySQLContainer<?> createMySQLContainer() {
        return new MySQLContainer<>("mysql:8.0")
                .withDatabaseName(TEST_DATABASE_NAME)
                .withUsername(TEST_USERNAME)
                .withPassword(TEST_PASSWORD)
                .withExposedPorts(3306);
    }

    private static PostgreSQLContainer<?> createPostgreSQLContainer() {
        return new PostgreSQLContainer<>("postgres:13")
                .withDatabaseName(TEST_DATABASE_NAME)
                .withUsername(TEST_USERNAME)
                .withPassword(TEST_PASSWORD)
                .withExposedPorts(5432);
    }

    private static MSSQLServerContainer<?> createMSSQLContainer() {
        return new MSSQLServerContainer<>()
                .withPassword(TEST_PASSWORD)
                .withInitScript("init_mssql.sql")
                .withEnv("ACCEPT_EULA", "Y");
    }

    private void setUpDatabaseType(DatabaseType databaseType) {
        String jdbcUrl;
        String subfolder;
        switch (databaseType) {
            case MYSQL -> {
                jdbcUrl = MYSQL_CONTAINER.getJdbcUrl();
                subfolder = "/other";
            }
            case H2 -> {
                jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
                subfolder = "/other";
            }
            case POSTGRES -> {
                jdbcUrl = POSTGRES_CONTAINER.getJdbcUrl();
                subfolder = "/postgres";
            }
            case MSSQL -> {
                jdbcUrl = MSSQL_CONTAINER.getJdbcUrl();
                subfolder = "/mssql";
            }
            default -> throw new BibernateException("Unsupported database type");
        }
        setUpDatabase(jdbcUrl, subfolder);
    }

    private void setUpDatabase(String url, String subFolder) {
        dataSource = new BibernateDataSource(url, TEST_USERNAME, TEST_PASSWORD);
        Flyway flyway = Flyway.configure().dataSource(dataSource)
                .locations("classpath:db/migration/product-test-data" + subFolder).load();
        flyway.clean();
        flyway.migrate();
        entityPersister = new EntityPersister(dataSource);
    }

    @AfterEach
    public void shoutDown() {
        dataSource.close();
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the findById method with a known entity ID")
    public void testFindById(DatabaseType databaseType) {
        setUpDatabaseType(databaseType);
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

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the findOne method with a known entity field and value")
    public void testFindOne(DatabaseType databaseType) throws NoSuchFieldException {
        setUpDatabaseType(databaseType);
        Product product = entityPersister.findOne(Product.class, Product.class.getDeclaredField("id"), 2);

        assertNotNull(product);
        assertEquals(2L, product.getId());
        assertEquals("XBox", product.getProductName());
        assertEquals("Microsoft", product.getProducer());
        assertEquals(BigDecimal.valueOf(21500, 2), product.getPrice());
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the findOne method with non-existed entity")
    public void testFindOneNotFound(DatabaseType databaseType) throws NoSuchFieldException {
        setUpDatabaseType(databaseType);
        Product product = entityPersister.findOne(Product.class, Product.class.getDeclaredField("id"), 8);

        assertNull(product);
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the findOne method with a field that return several products")
    public void testFindOneReturnSeveral(DatabaseType databaseType) {
        setUpDatabaseType(databaseType);

        assertThrows(BibernateException.class, () -> entityPersister.findOne(Product.class,
                Product.class.getDeclaredField("producer"), "Sony"));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the findAll method with a known entity field and value")
    public void testFindAll(DatabaseType databaseType) throws NoSuchFieldException {
        setUpDatabaseType(databaseType);

        List<Product> products = entityPersister
                .findAll(Product.class, Product.class.getDeclaredField("producer"), "Sony");
        assertEquals(2, products.size());
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the insert method with a new entity")
    public void testInsertNewEntity(DatabaseType databaseType) {
        setUpDatabaseType(databaseType);

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
        assertEquals(product.getWeight(), foundProduct.getWeight(), 0.0001);
        assertEquals(product.getDescription(), foundProduct.getDescription());
        assertEquals(product.getSaleDate(), foundProduct.getSaleDate());
        assertEquals(product.getSaleTime(), foundProduct.getSaleTime());
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    public void testInsertNullEntity(DatabaseType databaseType) {
        setUpDatabaseType(databaseType);

        assertThrows(NullPointerException.class, () -> entityPersister.insert(null));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the update method")
    public void testUpdate(DatabaseType databaseType) {
        setUpDatabaseType(databaseType);


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

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the update method with a non-existing entity")
    public void testUpdateNonExistingEntity(DatabaseType databaseType) {
        setUpDatabaseType(databaseType);

        Product product = new Product();
        product.setId(4L);
        product.setProductName("Nintendo Switch");
        product.setProducer("Nintendo");
        product.setPrice(BigDecimal.valueOf(29900, 2));

        assertThrows(BibernateException.class, () -> entityPersister.update(product), "Failed to update entity in the" +
                " database");
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the update method with null entity")
    public void testUpdateWithNullEntity(DatabaseType databaseType) {
        setUpDatabaseType(databaseType);

        assertThrows(NullPointerException.class, () -> entityPersister.update(null));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the update method with entity with null @Id field")
    public void testUpdateEntityWithNullId(DatabaseType databaseType) {
        setUpDatabaseType(databaseType);

        Product product = new Product();
        product.setProductName("Nintendo Switch");
        product.setProducer("Nintendo");
        product.setPrice(BigDecimal.valueOf(29900, 2));

        assertThrows(BibernateException.class, () -> entityPersister.update(product), "ID field is null");
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the delete method with an existing entity")
    public void testDelete(DatabaseType databaseType) {
        setUpDatabaseType(databaseType);

        Product product = entityPersister.findById(Product.class, 1L);

        assertNotNull(product);
        entityPersister.delete(product);

        Product deletedProduct = entityPersister.findById(Product.class, 1L);
        assertNull(deletedProduct);
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the delete method with a non-existing entity")
    public void testDeleteNonExistingEntity(DatabaseType databaseType) {
        setUpDatabaseType(databaseType);

        Product product = new Product();
        product.setId(5L);
        product.setProductName("Non-existing Product");
        product.setProducer("Non-existing Producer");
        product.setPrice(BigDecimal.valueOf(99900, 2));

        assertThrows(BibernateException.class, () -> entityPersister.delete(product), "Failed to delete entity from " +
                "the database");
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the delete method with null entity")
    public void testDeleteWithNullEntity(DatabaseType databaseType) {
        setUpDatabaseType(databaseType);

        assertThrows(NullPointerException.class, () -> entityPersister.delete(null));
    }

    @ParameterizedTest
    @EnumSource(DatabaseType.class)
    @DisplayName("Test the delete method with entity with null @Id field")
    public void testDeleteEntityWithNullId(DatabaseType databaseType) {
        setUpDatabaseType(databaseType);

        Product product = new Product();
        product.setProductName("Nintendo Switch");
        product.setProducer("Nintendo");
        product.setPrice(BigDecimal.valueOf(29900, 2));

        assertThrows(BibernateException.class, () -> entityPersister.delete(product), "ID field is null");
    }

    public enum DatabaseType {
        H2, POSTGRES, MYSQL, MSSQL
    }
}
