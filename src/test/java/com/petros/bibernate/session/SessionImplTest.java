package com.petros.bibernate.session;

import com.petros.bibernate.config.Configuration;
import com.petros.bibernate.dao.EntityPersister;
import com.petros.bibernate.datasource.BibernateDataSource;
import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.session.model.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;

import static com.petros.bibernate.config.Configuration.DEFAULT_CONNECTION_POOL_SIZE;
import static com.petros.bibernate.util.TestsConstants.TEST_PROPERTIES_PATH;
import static org.junit.jupiter.api.Assertions.*;
import static java.math.BigDecimal.ZERO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionImplTest {
    private Session session;
    @Spy
    private EntityPersister entityPersister;

    @BeforeEach
    public void setUpDatabase() {
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl(TEST_PROPERTIES_PATH);
        Configuration configuration = sessionFactory.getConfiguration();
        DataSource dataSource = new BibernateDataSource(configuration.getUrl(), configuration.getUsername(),
                configuration.getPassword(), DEFAULT_CONNECTION_POOL_SIZE);
        Flyway flyway = Flyway.configure().dataSource(dataSource)
                .locations("classpath:db/migration/product-test-data/other").load();
        flyway.clean();
        flyway.migrate();
        session = new SessionImpl(dataSource, entityPersister);
    }

    @Test
    @DisplayName("New entity is added to cache")
    void testPersist() {
        Product product = new Product();
        product.setProductName("Default console");
        product.setProducer("Default Producer");
        product.setPrice(BigDecimal.TEN);
        session.getTransaction().begin();
        session.persist(product);
        session.getTransaction().commit();
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
        session.getTransaction().begin();
        session.remove(product);
        session.getTransaction().commit();

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

    @Test
    @DisplayName("Transaction must be open before session.remove(..)")
    void transactionMustBeOpenBeforeDelete() {
        Product product = session.find(Product.class, 3L);
        assertThrows(BibernateException.class, () -> session.remove(product));
    }

    @Test
    @DisplayName("Transaction must be open before session.persist(..)")
    void transactionMustBeOpenBeforePersist() {
        assertThrows(BibernateException.class, () -> session.persist(createProduct()));
    }

    @Test
    @DisplayName("Session.remove(..) should be processed only after tx.commit()")
    void deleteDatabaseHitOccursAfterTxCommit() {
        Product product = session.find(Product.class, 3L);
        session.getTransaction().begin();

        session.remove(product);

        verify(entityPersister, never()).delete(any(), any());
        session.getTransaction().commit();
        verify(entityPersister, times(1)).delete(any(), any());
    }

    @Test
    @DisplayName("Session.persist(..) should be processed only after tx.commit()")
    void persistDatabaseHitOccursAfterTxCommit() {
        session.getTransaction().begin();

        session.persist(createProduct());

        verify(entityPersister, never()).insert(any(), any());
        session.getTransaction().commit();
        verify(entityPersister, times(1)).insert(any(), any());
    }

    @Test
    @DisplayName("Session.remove(..) should be processed only after session.flush()")
    void deleteDatabaseHitOccursAfterFlush() {
        Product product = session.find(Product.class, 3L);
        session.getTransaction().begin();

        session.remove(product);

        verify(entityPersister, never()).delete(any(), any());
        session.flush();
        verify(entityPersister, times(1)).delete(any(), any());
    }

    @Test
    @DisplayName("Session.persist(..) should be processed only after session.flush()")
    void persistDatabaseHitOccursAfterTxFlush() {
        session.getTransaction().begin();

        session.persist(createProduct());

        verify(entityPersister, never()).insert(any(), any());
        session.flush();
        verify(entityPersister, times(1)).insert(any(), any());
    }

    @Test
    @DisplayName("Entity only in persistent state (with initialized id and from persistent context) could be deleted")
    void doNotDeleteIfNotPersistentState() {
        session.getTransaction().begin();

        Product product = createProduct();
        product.setId(1L);
        assertThrows(BibernateException.class, () -> session.remove(product));
    }

    @Test
    @DisplayName("tx.rollback() should undo all database modifications")
    void rollbackUndoAllDatabaseChanges() {
        session.getTransaction().begin();
        int productsNumberBefore = session.findAll(Product.class).size();

        session.persist(createProduct());
        session.flush();
        try {
            session.persist(createProduct());
            doThrow(BibernateException.class).when(entityPersister).insert(any(), any());
            session.persist(createProduct());
            session.getTransaction().commit();
        } catch (Exception ex) {
            session.getTransaction().rollback();
        }
        verify(entityPersister, times(2)).insert(any(), any());
        List<Product> products = session.findAll(Product.class);
        assertEquals(productsNumberBefore + 1, products.size());
    }

    private Product createProduct() {
        Product product = new Product();
        product.setPrice(ZERO);
        product.setProductName("Super Cola");
        product.setProducer("Super factory");
        return product;
    }

    @Test
    @DisplayName("Testing flush() for changed entities")
    void testFlushEntities() {

        Product product = session.find(Product.class, 3L);
        product.setStockCount(100);
        session.flush();

        var updatedProduct = session.find(Product.class, 3L);
        assertEquals(updatedProduct.getStockCount(), 100);
    }


    @Test
    @DisplayName("Related entities with @OneToMany annotation must not be stored and checked in persistence context")
    void testFlushForRelatedEntities() {
        var note = session.find(Note.class, 1);
        var person = session.find(Person.class, 2);
        note.setPerson(person);
        session.flush();
        session.close();

        setUpDatabase();

        var selectedNote = session.find(Note.class, 1);
        assertNotEquals(note.getPerson(), selectedNote.getPerson());
    }

    @Test
    @DisplayName("Eager @OneToMany relation should be cached when parent is fetched")
    void findByIdEagerOneToManyCollectionCached() {
        Item item = session.find(Item.class, 3L);
        Item item2 = session.find(Item.class, 3L);

        assertEquals(item2, item);

        List<Bid> bids = item.getBids();
        assertEquals(3, bids.size());

        assertEquals(new BigDecimal(25000), bids.get(0).getPrice());
        assertEquals(new BigDecimal(30000), bids.get(1).getPrice());
        assertEquals(new BigDecimal(27500), bids.get(2).getPrice());

        verify(entityPersister, times(1)).findById(any(), any(), any());
        verify(entityPersister, times(2)).findAll(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Eager @ManyToOne relation should be cached when child is fetched")
    void findByIdEagerManyToOneEntityCached() {
        Bid bid = session.find(Bid.class, 3L);
        Bid bid2 = session.find(Bid.class, 3L);

        assertEquals(bid2, bid);

        assertEquals(bid.getItem(), bid2.getItem());
        assertEquals(2, bid.getItem().getBids().size());
        assertEquals(2, bid.getItem().getId());
        assertEquals("Picture", bid.getItem().getName());

        verify(entityPersister, times(2)).findById(any(), any(), any());
        verify(entityPersister, times(3)).findAll(any(), any(), any(), any());
    }
}
