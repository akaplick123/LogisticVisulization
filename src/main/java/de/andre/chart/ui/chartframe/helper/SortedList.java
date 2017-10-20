package de.andre.chart.ui.chartframe.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * a simple list which can easily be sorted (when needed)
 * 
 * @author andre
 *
 * @param <T>
 *            element type
 */
public class SortedList<T> implements List<T> {
    private final ArrayList<T> data = new ArrayList<>();
    /** <code>true</code> if no items out of order */ 
    private boolean isSorted = true;

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, T element) {
	data.add(index, element);
	this.isSorted = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#add(java.lang.Object)
     */
    public boolean add(T e) {
	boolean result = data.add(e);
	this.isSorted = false;
	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends T> c) {
	boolean result = data.addAll(c);
	this.isSorted = false;
	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection<? extends T> c) {
	boolean result = data.addAll(index, c);
	this.isSorted = false;
	return result;
    }

    /**
     * request a sort with given comparator. Is list is called twice with
     * different comparators only the first will be applied unless new items
     * were added in between.
     * 
     * @param comparator
     *            a comparator
     */
    public void singleSort(Comparator<T> comparator) {
	if (!isSorted) {
	    Collections.sort(data, comparator);
	    isSorted = true;
	}
    }

    public void clear() {
	data.clear();
    }

    public boolean contains(Object o) {
	return data.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
	return data.containsAll(c);
    }

    public boolean equals(Object o) {
	return data.equals(o);
    }

    public T get(int index) {
	return data.get(index);
    }

    public int hashCode() {
	return data.hashCode();
    }

    public int indexOf(Object o) {
	return data.indexOf(o);
    }

    public boolean isEmpty() {
	return data.isEmpty();
    }

    public Iterator<T> iterator() {
	return data.iterator();
    }

    public int lastIndexOf(Object o) {
	return data.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
	return data.listIterator();
    }

    public ListIterator<T> listIterator(int index) {
	return data.listIterator(index);
    }

    public T remove(int index) {
	return data.remove(index);
    }

    public boolean remove(Object o) {
	return data.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
	return data.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
	return data.retainAll(c);
    }

    public T set(int index, T element) {
	return data.set(index, element);
    }

    public int size() {
	return data.size();
    }

    public List<T> subList(int fromIndex, int toIndex) {
	return data.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
	return data.toArray();
    }

    @SuppressWarnings("hiding")
    public <T> T[] toArray(T[] a) {
	return data.toArray(a);
    }

    public String toString() {
	return data.toString();
    }

    public void trimToSize() {
	data.trimToSize();
    }
}
