/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections4.list;

import java.util.NoSuchElementException;

/**
 * Simplified NodeCachingLinkedList, a merge of NodeCachingLinkedList and AbstractLinkedList holding only int values
 */
public class NCLL implements Iterable<Integer> {
	
	/**
     * A {@link NCLLNode} which indicates the start and end of the list and does not
     * hold a value. The value of <code>next</code> is the first item in the
     * list. The value of of <code>previous</code> is the last item in the list.
     */
    transient NCLLNode header;

    /** The size of the list */
    transient int size;

    /** Modification count for iterators */
    transient int modCount;
    
    
    /**
     * The default value for {@link #maximumCacheSize}.
     */
    private static final int DEFAULT_MAXIMUM_CACHE_SIZE = 20;

    /**
     * The first cached node, or <code>null</code> if no nodes are cached.
     * Cached nodes are stored in a singly-linked list with
     * <code>next</code> pointing to the next element.
     */
    private transient NCLLNode firstCachedNode;

    /**
     * The size of the cache.
     */
    private transient int cacheSize;

    /**
     * The maximum size of the cache.
     */
    private int maximumCacheSize;

    //-----------------------------------------------------------------------
    /**
     * Constructor that creates.
     */
    public NCLL() {
        this(DEFAULT_MAXIMUM_CACHE_SIZE);
    }

    /**
     * Constructor that species the maximum cache size.
     *
     * @param maximumCacheSize  the maximum cache size
     */
    public NCLL(final int maximumCacheSize) {
        this.maximumCacheSize = maximumCacheSize;
        init();
    }
    
    
    //---------------------PUBLIC-----------------------------------
    
    public NCLLIterator iterator() {
        return listIterator();
    }

    public NCLLIterator listIterator() {
        return new NCLLIterator(this, 0);
    }

    public NCLLIterator listIterator(final int fromIndex) {
        return new NCLLIterator(this, fromIndex);
    }
    
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Integer get(final int index) {
        final NCLLNode node = getNode(index, false);
        return node.getValue();
    }
    
    public int indexOf(final Integer value) {
        int i = 0;
        for (NCLLNode node = header.next; node != header; node = node.next) {
            if (isEqualValue(node.getValue(), value)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int lastIndexOf(final Integer value) {
        int i = size - 1;
        for (NCLLNode node = header.previous; node != header; node = node.previous) {
            if (isEqualValue(node.getValue(), value)) {
                return i;
            }
            i--;
        }
        return -1;
    }

    public boolean contains(final Integer value) {
        return indexOf(value) != -1;
    }

    public Integer[] toArray() {
        return toArray(new Integer[size]);
    }

    public Integer[] toArray(Integer[] array) {
        // Extend the array if needed
        if (array.length < size) {
            array = new Integer[size];
        }
        // Copy the values into the array
        int i = 0;
        for (NCLLNode node = header.next; node != header; node = node.next, i++) {
            array[i] = node.getValue();
        }
        // Set the value after the last value to null
        if (array.length > size) {
            array[size] = null;
        }
        return array;
    }
    
    public boolean add(final Integer value) {
        addLast(value);
        return true;
    }

    public void add(final int index, final Integer value) {
        final NCLLNode node = getNode(index, true);
        addNodeBefore(node, value);
    }
    
    public Integer remove(final int index) {
        final NCLLNode node = getNode(index, false);
        final Integer oldValue = node.getValue();
        removeNode(node);
        return oldValue;
    }

    public boolean removeValue(final Integer value) {
        for (NCLLNode node = header.next; node != header; node = node.next) {
            if (isEqualValue(node.getValue(), value)) {
                removeNode(node);
                return true;
            }
        }
        return false;
    }
    
    public Integer set(final int index, final Integer value) {
        final NCLLNode node = getNode(index, false);
        final Integer oldValue = node.getValue();
        updateNode(node, value);
        return oldValue;
    }

    public void clear() {
        removeAllNodes();
    }
    
    public Integer getFirst() {
        final NCLLNode node = header.next;
        if (node == header) {
            throw new NoSuchElementException();
        }
        return node.getValue();
    }

    public Integer getLast() {
        final NCLLNode node = header.previous;
        if (node == header) {
            throw new NoSuchElementException();
        }
        return node.getValue();
    }

    public boolean addFirst(final Integer o) {
        addNodeAfter(header, o);
        return true;
    }

    public boolean addLast(final Integer o) {
        addNodeBefore(header, o);
        return true;
    }

    public Integer removeFirst() {
        final NCLLNode node = header.next;
        if (node == header) {
            throw new NoSuchElementException();
        }
        final Integer oldValue = node.getValue();
        removeNode(node);
        return oldValue;
    }

    public Integer removeLast() {
        final NCLLNode node = header.previous;
        if (node == header) {
            throw new NoSuchElementException();
        }
        final Integer oldValue = node.getValue();
        removeNode(node);
        return oldValue;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof NCLL == false) {
            return false;
        }
        final NCLL other = (NCLL) obj;
        if (other.size() != size()) {
            return false;
        }
        final NCLLIterator it1 = listIterator();
        final NCLLIterator it2 = other.listIterator();
        while (it1.hasNext() && it2.hasNext()) {
            final Integer o1 = it1.next();
            final Integer o2 = it2.next();
            if (!(o1 == null ? o2 == null : o1.equals(o2))) {
                return false;
            }
        }
        return !(it1.hasNext() || it2.hasNext());
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (final Integer e : this) {
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    @Override
    public String toString() {
        if (size() == 0) {
            return "[]";
        }
        final StringBuilder buf = new StringBuilder(16 * size());
        buf.append('[');

        final NCLLIterator it = iterator();
        boolean hasNext = it.hasNext();
        while (hasNext) {
            final Object value = it.next();
            buf.append(value == this ? "(this Collection)" : value);
            hasNext = it.hasNext();
            if (hasNext) {
                buf.append(", ");
            }
        }
        buf.append(']');
        return buf.toString();
    }
    
    //--------------------------------------------------------------

    /**
     * The equivalent of a default constructor, broken out so it can be called
     * by any constructor and by <code>readObject</code>.
     * Subclasses which override this method should make sure they call super,
     * so the list is initialised properly.
     */
    protected void init() {
        header = createHeaderNode();
    }
    
    /**
     * Gets the maximum size of the cache.
     *
     * @return the maximum cache size
     */
    protected int getMaximumCacheSize() {
        return maximumCacheSize;
    }

    /**
     * Sets the maximum size of the cache.
     *
     * @param maximumCacheSize  the new maximum cache size
     */
    protected void setMaximumCacheSize(final int maximumCacheSize) {
        this.maximumCacheSize = maximumCacheSize;
        shrinkCacheToMaximumSize();
    }

    /**
     * Reduce the size of the cache to the maximum, if necessary.
     */
    protected void shrinkCacheToMaximumSize() {
        // Rich Dougherty: This could be more efficient.
        while (cacheSize > maximumCacheSize) {
            getNodeFromCache();
        }
    }

    /**
     * Gets a node from the cache. If a node is returned, then the value of
     * {@link #cacheSize} is decreased accordingly. The node that is returned
     * will have <code>null</code> values for next, previous and element.
     *
     * @return a node, or <code>null</code> if there are no nodes in the cache.
     */
    protected NCLLNode getNodeFromCache() {
        if (cacheSize == 0) {
            return null;
        }
        final NCLLNode cachedNode = firstCachedNode;
        firstCachedNode = cachedNode.next;
        cachedNode.next = null; // This should be changed anyway, but defensively
                                // set it to null.
        cacheSize--;
        return cachedNode;
    }

    /**
     * Checks whether the cache is full.
     *
     * @return true if the cache is full
     */
    protected boolean isCacheFull() {
        return cacheSize >= maximumCacheSize;
    }

    /**
     * Adds a node to the cache, if the cache isn't full.
     * The node's contents are cleared to so they can be garbage collected.
     *
     * @param node  the node to add to the cache
     */
    protected void addNodeToCache(final NCLLNode node) {
        if (isCacheFull()) {
            // don't cache the node.
            return;
        }
        // clear the node's contents and add it to the cache.
        final NCLLNode nextCachedNode = firstCachedNode;
        node.previous = null;
        node.next = nextCachedNode;
        node.setValue(null);
        firstCachedNode = node;
        cacheSize++;
    }

    //-----------------------------------------------------------------------
    /**
     * Creates a new node, either by reusing one from the cache or creating
     * a new one.
     *
     * @param value  value of the new node
     * @return the newly created node
     */
    protected NCLLNode createNode(final Integer value) {
        final NCLLNode cachedNode = getNodeFromCache();
        if (cachedNode == null) {
            return createNewNode(value);
        }
        cachedNode.setValue(value);
        return cachedNode;
    }

    /**
     * Removes the node from the list, storing it in the cache for reuse
     * if the cache is not yet full.
     *
     * @param node  the node to remove
     */
    protected void removeNode(final NCLLNode node) {
        removeNodeFromList(node);
        addNodeToCache(node);
    }

    /**
     * Removes all the nodes from the list, storing as many as required in the
     * cache for reuse.
     *
     */
    protected void removeAllNodes() {
        // Add the removed nodes to the cache, then remove the rest.
        // We can add them to the cache before removing them, since
        // {@link AbstractLinkedList.removeAllNodes()} removes the
        // nodes by removing references directly from {@link #header}.
        final int numberOfNodesToCache = Math.min(size, maximumCacheSize - cacheSize);
        NCLLNode node = header.next;
        for (int currentIndex = 0; currentIndex < numberOfNodesToCache; currentIndex++) {
            final NCLLNode oldNode = node;
            node = node.next;
            addNodeToCache(oldNode);
        }
        removeAllNodesFromList();
    }
    
    
    //---------------------PROTECTED--------------------------------
    
    
    /**
     * Compares two values for equals.
     * This implementation uses the equals method.
     * Subclasses can override this to match differently.
     *
     * @param value1  the first value to compare, may be null
     * @param value2  the second value to compare, may be null
     * @return true if equal
     */
    protected boolean isEqualValue(final Integer value1, final Integer value2) {
        return value1 == value2 || (value1 == null ? false : value1.equals(value2));
    }

    /**
     * Updates the node with a new value.
     * This implementation sets the value on the node.
     * Subclasses can override this to record the change.
     *
     * @param node  node to update
     * @param value  new value of the node
     */
    protected void updateNode(final NCLLNode node, final Integer value) {
        node.setValue(value);
    }

    /**
     * Creates a new node with previous, next and element all set to null.
     * This implementation creates a new empty Node.
     * Subclasses can override this to create a different class.
     *
     * @return  newly created node
     */
    protected NCLLNode createHeaderNode() {
        return new NCLLNode();
    }

    /**
     * Creates a new node with the specified properties.
     * This implementation creates a new Node with data.
     * Subclasses can override this to create a different class.
     *
     * @param value  value of the new node
     * @return a new node containing the value
     */
    protected NCLLNode createNewNode(final Integer value) {
        return new NCLLNode(value);
    }

    /**
     * Creates a new node with the specified object as its
     * <code>value</code> and inserts it before <code>node</code>.
     * <p>
     * This implementation uses {@link #createNode(Object)} and
     * {@link #addNode(AbstractLinkedList.Node,AbstractLinkedList.Node)}.
     *
     * @param node  node to insert before
     * @param value  value of the newly added node
     * @throws NullPointerException if <code>node</code> is null
     */
    protected void addNodeBefore(final NCLLNode node, final Integer value) {
        final NCLLNode newNode = createNode(value);
        addNode(newNode, node);
    }

    /**
     * Creates a new node with the specified object as its
     * <code>value</code> and inserts it after <code>node</code>.
     * <p>
     * This implementation uses {@link #createNode(Object)} and
     * {@link #addNode(AbstractLinkedList.Node,AbstractLinkedList.Node)}.
     *
     * @param node  node to insert after
     * @param value  value of the newly added node
     * @throws NullPointerException if <code>node</code> is null
     */
    protected void addNodeAfter(final NCLLNode node, final Integer value) {
        final NCLLNode newNode = createNode(value);
        addNode(newNode, node.next);
    }

    /**
     * Inserts a new node into the list.
     *
     * @param nodeToInsert  new node to insert
     * @param insertBeforeNode  node to insert before
     * @throws NullPointerException if either node is null
     */
    protected void addNode(final NCLLNode nodeToInsert, final NCLLNode insertBeforeNode) {
        nodeToInsert.next = insertBeforeNode;
        nodeToInsert.previous = insertBeforeNode.previous;
        insertBeforeNode.previous.next = nodeToInsert;
        insertBeforeNode.previous = nodeToInsert;
        size++;
        modCount++;
    }

    /**
     * Removes the specified node from the list.
     *
     * @param node  the node to remove
     * @throws NullPointerException if <code>node</code> is null
     */
    protected void removeNodeFromList(final NCLLNode node) {
        node.previous.next = node.next;
        node.next.previous = node.previous;
        size--;
        modCount++;
    }

    /**
     * Removes all nodes by resetting the circular list marker.
     */
    protected void removeAllNodesFromList() {
        header.next = header;
        header.previous = header;
        size = 0;
        modCount++;
    }

    /**
     * Gets the node at a particular index.
     *
     * @param index  the index, starting from 0
     * @param endMarkerAllowed  whether or not the end marker can be returned if
     * startIndex is set to the list's size
     * @return the node at the given index
     * @throws IndexOutOfBoundsException if the index is less than 0; equal to
     * the size of the list and endMakerAllowed is false; or greater than the
     * size of the list
     */
    protected NCLLNode getNode(final int index, final boolean endMarkerAllowed) throws IndexOutOfBoundsException {
        // Check the index is within the bounds
        if (index < 0) {
            throw new IndexOutOfBoundsException("Couldn't get the node: " +
                    "index (" + index + ") less than zero.");
        }
        if (!endMarkerAllowed && index == size) {
            throw new IndexOutOfBoundsException("Couldn't get the node: " +
                    "index (" + index + ") is the size of the list.");
        }
        if (index > size) {
            throw new IndexOutOfBoundsException("Couldn't get the node: " +
                    "index (" + index + ") greater than the size of the " +
                    "list (" + size + ").");
        }
        // Search the list and get the node
        NCLLNode node;
        if (index < size / 2) {
            // Search forwards
            node = header.next;
            for (int currentIndex = 0; currentIndex < index; currentIndex++) {
                node = node.next;
            }
        } else {
            // Search backwards
            node = header;
            for (int currentIndex = size; currentIndex > index; currentIndex--) {
                node = node.previous;
            }
        }
        return node;
    }
    
    //--------------------------------------------------------------
    

}
