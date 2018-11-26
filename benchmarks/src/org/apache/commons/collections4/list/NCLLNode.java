package org.apache.commons.collections4.list;

/**
 * A simplified Node
 */
public class NCLLNode {

    /** A pointer to the node before this node */
    protected NCLLNode previous;
    /** A pointer to the node after this node */
    protected NCLLNode next;
    /** The object contained within this node */
    protected Integer value;

    /**
     * Constructs a new header node.
     */
    protected NCLLNode() {
        previous = this;
        next = this;
    }

    /**
     * Constructs a new node.
     *
     * @param value  the value to store
     */
    protected NCLLNode(final Integer value) {
        this.value = value;
    }

    /**
     * Constructs a new node.
     *
     * @param previous  the previous node in the list
     * @param next  the next node in the list
     * @param value  the value to store
     */
    protected NCLLNode(final NCLLNode previous, final NCLLNode next, final Integer value) {
        this.previous = previous;
        this.next = next;
        this.value = value;
    }

    /**
     * Gets the value of the node.
     *
     * @return the value
     * @since 3.1
     */
    protected Integer getValue() {
        return value;
    }

    /**
     * Sets the value of the node.
     *
     * @param value  the value
     * @since 3.1
     */
    protected void setValue(final Integer value) {
        this.value = value;
    }

    /**
     * Gets the previous node.
     *
     * @return the previous node
     * @since 3.1
     */
    protected NCLLNode getPreviousNode() {
        return previous;
    }

    /**
     * Sets the previous node.
     *
     * @param previous  the previous node
     * @since 3.1
     */
    protected void setPreviousNode(final NCLLNode previous) {
        this.previous = previous;
    }

    /**
     * Gets the next node.
     *
     * @return the next node
     * @since 3.1
     */
    protected NCLLNode getNextNode() {
        return next;
    }

    /**
     * Sets the next node.
     *
     * @param next  the next node
     * @since 3.1
     */
    protected void setNextNode(final NCLLNode next) {
        this.next = next;
    }
}
