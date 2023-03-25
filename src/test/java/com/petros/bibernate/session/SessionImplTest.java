package com.petros.bibernate.session;

import com.petros.bibernate.datasource.BibernateDataSource;
import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.session.model.Product;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SessionImplTest {
    private Session session;

    public static final String DATABASE_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

    private static final String TEST_USERNAME = "sa";
    private static final String TEST_PASSWORD = "Test_Password2023#";

    @BeforeEach
    public void setUpDatabase() {
        DataSource dataSource = new BibernateDataSource(DATABASE_URL, TEST_USERNAME, TEST_PASSWORD);
        Flyway flyway = Flyway.configure().dataSource(dataSource)
                .locations("classpath:db/migration/product-test-data/other").load();
        flyway.clean();
        flyway.migrate();
        session = new SessionImpl(dataSource);
    }

    @Test
    void flush() {
        // TODO: Implement tests for flush
    }

    @Test
    @DisplayName("New entity is added to cache")
    void testPersist() {
        Product product = new Product();
        product.setProductName("Default console");
        product.setProducer("Default Producer");
        product.setPrice(BigDecimal.TEN);
        session.persist(product);
        var selectedProduct = session.find(Product.class, 4);
        assertSame(product, selectedProduct);
    }

    @Test
    @DisplayName("Find method return previously cached value")
    void testFindForCachedValues() {
        Product product = session.find(Product.class, 1L);
        Product theSameProduct = session.find(Product.class, 1L);
        assertSame(product, theSameProduct);

        var product4 = session.find(Product.class, 3L);
        assertNotSame(product4, theSameProduct);
    }

    @Test
    @DisplayName("Find method return previously cached value")
    void testFindForNonExistValues() {
        Product product = session.find(Product.class, 5L);
        assertNull(product);
    }

    @Test
    @DisplayName("Testing remove method in session")
    void remove() {
        Product product = session.find(Product.class, 3L);

        assertNotNull(product);
        session.remove(product);

        Product deletedProduct = session.find(Product.class, 3L);
        assertNull(deletedProduct);
    }

    @Test
    @DisplayName("Testing find method when session has been closed")
    void testFindAfterSessionClose() {
        session.find(Product.class, 1L);
        session.close();
        assertThrows(BibernateException.class, () -> session.find(Product.class, 1L));
    }

    @Test
    @DisplayName("Testing persist method when session has been closed")
    void testPersistAfterSessionClose() {
        Product product = new Product();
        product.setProductName("Default console");
        product.setProducer("Default Producer");
        product.setPrice(BigDecimal.TEN);
        session.close();
        assertThrows(BibernateException.class, () -> session.persist(product));
    }

    @Test
    @DisplayName("Testing calling persist method when session has been closed")
    void testRemoveAfterSessionClose() {
        Product product = session.find(Product.class, 3L);
        session.close();
        assertThrows(BibernateException.class, () -> session.remove(product));
    }
}