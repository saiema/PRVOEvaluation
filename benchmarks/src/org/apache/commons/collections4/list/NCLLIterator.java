package org.apache.commons.collections4.list;

import java.util.ConcurrentModificationException;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.commons.collections4.OrderedIterator;

/**
 * A list iterator over the linked list.
 */
public class NCLLIterator implements ListIterator<Integer>, OrderedIterator<Integer> {

    /** The parent list */
    protected final NCLL parent;

    /**
     * The node that will be returned by {@link #next()}. If this is equal
     * to {@link AbstractLinkedList#header} then there are no more values to return.
     */
    protected NCLLNode next;

    /**
     * The index of {@link #next}.
     */
    protected int nextIndex;

    /**
     * The last node that was returned by {@link #next()} or {@link
     * #previous()}. Set to <code>null</code> if {@link #next()} or {@link
     * #previous()} haven't been called, or if the node has been removed
     * with {@link #remove()} or a new node added with {@link #add(Object)}.
     * Should be accessed through {@link #getLastNodeReturned()} to enforce
     * this behaviour.
     */
    protected NCLLNode current;

    /**
     * The modification count that the list is expected to have. If the list
     * doesn't have this count, then a
     * {@link java.util.ConcurrentModificationException} may be thrown by
     * the operations.
     */
    protected int expectedModCount;

    /**
     * Create a ListIterator for a list.
     *
     * @param parent  the parent list
     * @param fromIndex  the index to start at
     * @throws IndexOutOfBoundsException if fromIndex is less than 0 or greater than the size of the list
     */
    protected NCLLIterator(final NCLL parent, final int fromIndex)
            throws IndexOutOfBoundsException {
        this.parent = parent;
        this.expectedModCount = parent.modCount;
        this.next = parent.getNode(fromIndex, true);
        this.nextIndex = fromIndex;
    }

    /**
     * Checks the modification count of the list is the value that this
     * object expects.
     *
     * @throws ConcurrentModificationException If the list's modification
     * count isn't the value that was expected.
     */
    protected void checkModCount() {
        if (parent.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Gets the last node returned.
     *
     * @return the last node returned
     * @throws IllegalStateException If {@link #next()} or {@link #previous()} haven't been called,
     * or if the node has been removed with {@link #remove()} or a new node added with {@link #add(Object)}.
     */
    protected NCLLNode getLastNodeReturned() throws IllegalStateException {
        if (current == null) {
            throw new IllegalStateException();
        }
        return current;
    }

    public boolean hasNext() {
        return next != parent.header;
    }

    public Integer next() {
        checkModCount();
        if (!hasNext()) {
            throw new NoSuchElementException("No element at index " + nextIndex + ".");
        }
        final Integer value = next.getValue();
        current = next;
        next = next.next;
        nextIndex++;
        return value;
    }

    public boolean hasPrevious() {
        return next.previous != parent.header;
    }

    public Integer previous() {
        checkModCount();
        if (!hasPrevious()) {
            throw new NoSuchElementException("Already at start of list.");
        }
        next = next.previous;
        final Integer value = next.getValue();
        current = next;
        nextIndex--;
        return value;
    }

    public int nextIndex() {
        return nextIndex;
    }

    public int previousIndex() {
        // not normally overridden, as relative to nextIndex()
        return nextIndex() - 1;
    }

    public void remove() {
        checkModCount();
        if (current == next) {
            // remove() following previous()
            next = next.next;
            parent.removeNode(getLastNodeReturned());
        } else {
            // remove() following next()
            parent.removeNode(getLastNodeReturned());
            nextIndex--;
        }
        current = null;
        expectedModCount++;
    }

    public void set(final Integer obj) {
        checkModCount();
        getLastNodeReturned().setValue(obj);
    }

    public void add(final Integer obj) {
        checkModCount();
        parent.addNodeBefore(next, obj);
        current = null;
        nextIndex++;
        expectedModCount++;
    }

}
