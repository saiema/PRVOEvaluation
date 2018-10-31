package avltree;

public class AvlNode {
	// Friendly data; accessible by other package routines
	public int element; // The data in the node

	public /*@ nullable @*/AvlNode left; // Left child

	public /*@ nullable @*/AvlNode right; // Right child

	public int height; // Height



    	// Constructors
	public AvlNode(final int theElement) {
		this(theElement, null, null);
	}

	public AvlNode(final int theElement, final AvlNode lt, final AvlNode rt) {
		this.element = theElement;
		this.left = lt;
		this.right = rt;
		this.height = 0;
	}

}

