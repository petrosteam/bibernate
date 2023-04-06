package com.petros.bibernate.dao;

import com.petros.bibernate.dao.lazy.LazyList;
import com.petros.bibernate.session.model.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LazyListDelegationTest {
    @Test
    @DisplayName("Check that LazyList Delegates correctly all methods")
    public void testLazyListDelegatesMethodsCorrect() {
        Person firstPerson = new Person();
        firstPerson.setId(1L);
        firstPerson.setFirstName("Alex");
        Person replasedFirstPerson = new Person();
        firstPerson.setId(1L);
        firstPerson.setFirstName("ReplacedAlex");

        Person secondPerson = new Person();
        firstPerson.setId(2L);
        firstPerson.setFirstName("Oleg");
        Person[] personArray = new Person[1];
        personArray[0] = secondPerson;

        List<Person> personList = new ArrayList<>();
        personList.add(firstPerson);

        LazyList<Person> lazyList = new LazyList<>(() -> personList);

        assertEquals(personList.size(), lazyList.size());
        assertEquals(personList.get(0), lazyList.get(0));
        assertEquals(personList.indexOf(firstPerson), lazyList.indexOf(firstPerson));
        assertEquals(personList.isEmpty(), lazyList.isEmpty());
        assertEquals(personList.iterator().hasNext(), lazyList.iterator().hasNext());
        assertEquals(personList.toArray().length, lazyList.toArray().length);
        assertEquals(personList.toArray(personArray), lazyList.toArray(personArray));
        assertTrue(lazyList.add(secondPerson));
        lazyList.add(0, secondPerson);
        assertTrue(lazyList.remove(secondPerson));
        assertTrue(lazyList.contains(firstPerson));
        assertTrue(lazyList.containsAll(LazyList.of(firstPerson)));
        assertEquals(personList.listIterator().hasNext(), lazyList.listIterator().hasNext());
        assertEquals(personList.listIterator(0).next(), lazyList.listIterator(0).next());
        assertTrue(lazyList.addAll(List.of(secondPerson)));
        assertEquals(personList.subList(0, 1), lazyList.subList(0, 1));
        assertTrue(lazyList.addAll(1, List.of(secondPerson)));
        assertEquals(lazyList.set(1, replasedFirstPerson), personList.get(1));
        assertTrue(lazyList.removeAll(List.of(secondPerson)));
        assertTrue(lazyList.retainAll(List.of(secondPerson)));
        lazyList.clear();
        assertEquals(lazyList.lastIndexOf(replasedFirstPerson), personList.lastIndexOf(replasedFirstPerson));
        lazyList.add(0, secondPerson);
        assertEquals(lazyList.size(), personList.size());
        lazyList.remove(0);
        assertEquals(lazyList.size(), personList.size());

    }
}
