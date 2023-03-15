package com.petros.bibernate.session.crud;

import com.petros.bibernate.session.Persistence;
import com.petros.bibernate.session.Session;
import com.petros.bibernate.session.SessionFactory;
import com.petros.bibernate.session.model.Product;
import lombok.SneakyThrows;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FindByTest {

    public static final String DB_URl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;";
    public static final String DB_USERNAME = "sa";
    public static final String DB_PASSWORD = "";

    static DataSource dataSource;

    @SneakyThrows
    @BeforeAll
    static void init() {
        String initDbQueries = getSqlFromFile("db/schema.sql");
        dataSource = initDataSource(DB_URl, DB_USERNAME, DB_PASSWORD);
        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.execute(initDbQueries);
            }
        }
    }

    @SneakyThrows
    @BeforeEach
    void setUp() {
        String fillDataQueries = getSqlFromFile("db/data.sql");
        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                statement.execute("truncate table products;");
                statement.execute(fillDataQueries);
            }
        }
    }

    @Test
    void testFindById() {
        SessionFactory sessionFactory = Persistence.createSessionFactory();
        Session session = sessionFactory.openSession();
        Product product = session.find(Product.class, 1L);

        assertNotNull(product);
        assertEquals(1L, product.getId());
        assertEquals("Play Station", product.getProductName());
        assertEquals("Sony", product.getProducer());
        assertEquals(BigDecimal.valueOf(24900, 2), product.getPrice());
    }

    private static DataSource initDataSource(String url, String username, String password) {
        var datasource = new JdbcDataSource();
        datasource.setURL(url);
        datasource.setUser(username);
        datasource.setPassword(password);
        return datasource;
    }

    private static String getSqlFromFile(String fileName) {
        try {
            Objects.requireNonNull(fileName);
            URL fileUrl = FindByTest.class.getClassLoader().getResource(fileName);
            URI uri = Objects.requireNonNull(fileUrl).toURI();
            try (var lines = Files.lines(Paths.get(uri))) {
                return lines.collect(Collectors.joining("\n"));
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}