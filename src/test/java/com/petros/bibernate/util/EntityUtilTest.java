package com.petros.bibernate.util;

import com.petros.bibernate.exception.BibernateException;
import com.petros.bibernate.util.model.BrokenPerson;
import com.petros.bibernate.util.model.UtilNote;
import com.petros.bibernate.util.model.Person;
import com.petros.bibernate.util.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

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
    @DisplayName("getJoinColumnName returns annotation value when @JoinColumn is present")
    void getJoinColumnAnnotationPresent() throws NoSuchFieldException {
        Field personField = UtilNote.class.getDeclaredField("person");
        String joinColumnName = EntityUtil.getJoinColumnName(personField);
        assertNotNull(joinColumnName);
        assertEquals("person_id", joinColumnName);
    }
}