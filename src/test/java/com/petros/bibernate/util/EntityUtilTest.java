package com.petros.bibernate.util;

import com.petros.bibernate.annotation.Id;
import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.session.model.Product;
import com.petros.bibernate.util.model.BrokenPerson;
import com.petros.bibernate.util.model.Person;
import com.petros.bibernate.util.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class EntityUtilTest {

    @Test
    @DisplayName("getTableName returns annotation value when @Table is present")
    void getTableNameAnnotationTablePresent() {
        String tableName = EntityUtil.getTableName(Person.class);
        assertNotNull(tableName);
        assertEquals("persons", tableName);
    }

    @Test
    @DisplayName("getTableName returns class name when @Table is absent")
    void getTableNameAnnotationTableAbsent() {
        String tableName = EntityUtil.getTableName(User.class);
        assertNotNull(tableName);
        assertEquals("User", tableName);
    }

    @Test
    @DisplayName("getColumnName returns annotation value when @Column is present")
    void getColumnNameAnnotationColumnPresent() throws NoSuchFieldException {
        Field firstNameField = Person.class.getDeclaredField("firstName");
        String columnName = EntityUtil.getColumnName(firstNameField);
        assertEquals("first_name", columnName);
    }

    @Test
    @DisplayName("getColumnName returns field name when @Column is absent")
    void getColumnNameAnnotationColumnAbsent() throws NoSuchFieldException {
        Field priceField = Person.class.getDeclaredField("price");
        String columnName = EntityUtil.getColumnName(priceField);
        assertEquals("price", columnName);
    }

    @Test
    @DisplayName("getIdField returns field when only one @Id is present")
    void getIdFieldOnlyOneAnnotationIdPresent() {
        Field idField = EntityUtil.getIdField(Person.class);
        assertNotNull(idField);
        assertEquals("id", idField.getName());
    }

    @Test
    @DisplayName("getIdField throws exception when more than one @Id is present")
    void getIdFieldMoreThanOneIdAnnotationPresent() {
        BibernateException ex = assertThrows(BibernateException.class, () -> EntityUtil.getIdField(User.class));
        assertEquals("Entity User must contain exactly one field annotated with @Id", ex.getMessage());
    }

    @Test
    @DisplayName("getIdField throws exception when @Id is absent")
    void getIdFieldIdAnnotationAbsent() {
        BibernateException ex = assertThrows(BibernateException.class, () -> EntityUtil.getIdField(BrokenPerson.class));
        assertEquals("Entity BrokenPerson must contain exactly one field annotated with @Id", ex.getMessage());
    }

    @Test
    @DisplayName("getIdField throws exception when @Id is absent")
    void getFieldValuesFromEntity() throws IllegalAccessException {
        Product product = new Product();
        product.setId(1L);
        product.setProducer("Default Producer");
        product.setProductName("Default Product");
        product.setPrice(BigDecimal.TEN);
        product.setDescription("Default Description");
        product.setIsAvailable(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setSaleDate(LocalDate.now());
        product.setStockCount(1);
        var fields = product.getClass().getDeclaredFields();
        var values = EntityUtil.getEntityFields(product).toArray();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            field.setAccessible(true);
            var fieldValue = field.get(product);
            var value = values[i];
            assertEquals(value, fieldValue);
        }
    }
}