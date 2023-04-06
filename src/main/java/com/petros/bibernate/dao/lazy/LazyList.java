package com.petros.bibernate.dao.lazy;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * LazyList is a wrapper class for java.util.List with a Lazy access to List.
 * Delegates all methods to the base implementation of List.
 *
 * @param <T> - class of elements in collection.
 */
public class LazyList<T> implements List<T> {

    public Supplier<List<?>> listSupplier;
    public List<T> internalList;

    public LazyList(Supplier<List<?>> listSupplier) {
        this.listSupplier = listSupplier;
    }

    @SuppressWarnings("unchecked")
    private List<T> getInternalList() {
        if (internalList == null) {
            internalList = (List<T>) listSupplier.get();
        }
        return internalList;
    }

    @Override
    public int size() {
        return getInternalList().size();
    }

    public boolean isEmpty() {
        return getInternalList().isEmpty();
    }

    public boolean contains(Object o) {
        return getInternalList().contains(o);
    }

    public Iterator<T> iterator() {
        return getInternalList().iterator();
    }

    public Object[] toArray() {
        return getInternalList().toArray();
    }

    public <T1> T1[] toArray(T1[] a) {
        return getInternalList().toArray(a);
    }

    public boolean add(T t) {
        return getInternalList().add(t);
    }

    public boolean remove(Object o) {
        return getInternalList().remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return getInternalList().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return getInternalList().addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return getInternalList().addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return getInternalList().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return getInternalList().retainAll(c);
    }

    @Override
    public void clear() {
        getInternalList().clear();
    }

    @Override
    public T get(int index) {
        return getInternalList().get(index);
    }

    @Override
    public T set(int index, T element) {
        return getInternalList().set(index, element);
    }

    @Override
    public void add(int index, T element) {
        getInternalList().add(index, element);
    }

    @Override
    public T remove(int index) {
        return getInternalList().remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return getInternalList().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getInternalList().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return getInternalList().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return getInternalList().listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return getInternalList().subList(fromIndex, toIndex);
    }

    @SafeVarargs
    public static <E> List<E> of(E... elements) {
        return List.of(elements);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        getInternalList().forEach(action);
    }
}
