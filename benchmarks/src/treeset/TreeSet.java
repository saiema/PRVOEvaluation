/*
 * @(#)TreeMap.java	1.56 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package treeset;

/**
 * Red-Black tree based implementation of the <tt>SortedMap</tt> interface.
 * This class guarantees that the map will be in ascending key order, sorted
 * according to the <i>natural order</i> for the key's class (see
 * <tt>Comparable</tt>), or by the comparator provided at creation time,
 * depending on which constructor is used.<p>
 *
 * This implementation provides guaranteed log(n) time cost for the
 * <tt>containsKey</tt>, <tt>get</tt>, <tt>put</tt> and <tt>remove</tt>
 * operations.  Algorithms are adaptations of those in Cormen, Leiserson, and
 * Rivest's <I>Introduction to Algorithms</I>.<p>
 *
 * Note that the ordering maintained by a sorted map (whether or not an
 * explicit comparator is provided) must be <i>consistent with equals</i> if
 * this sorted map is to correctly implement the <tt>Map</tt> interface.  (See
 * <tt>Comparable</tt> or <tt>Comparator</tt> for a precise definition of
 * <i>consistent with equals</i>.)  This is so because the <tt>Map</tt>
 * interface is defined in terms of the equals operation, but a map performs
 * all key comparisons using its <tt>compareTo</tt> (or <tt>compare</tt>)
 * method, so two keys that are deemed equal by this method are, from the
 * standpoint of the sorted map, equal.  The behavior of a sorted map
 * <i>is</i> well-defined even if its ordering is inconsistent with equals; it
 * just fails to obey the general contract of the <tt>Map</tt> interface.<p>
 *
 * <b>Note that this implementation is not synchronized.</b> If multiple
 * threads access a map concurrently, and at least one of the threads modifies
 * the map structurally, it <i>must</i> be synchronized externally.  (A
 * structural modification is any operation that adds or deletes one or more
 * mappings; merely changing the value associated with an existing key is not
 * a structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the map.  If no
 * such object exists, the map should be "wrapped" using the
 * <tt>Collections.synchronizedMap</tt> method.  This is best done at creation
 * time, to prevent accidental unsynchronized access to the map: 
 * <pre>
 *     Map m = Collections.synchronizedMap(new TreeMap(...));
 * </pre><p>
 *
 * The iterators returned by all of this class's "collection view methods" are
 * <i>fail-fast</i>: if the map is structurally modified at any time after the
 * iterator is created, in any way except through the iterator's own
 * <tt>remove</tt> or <tt>add</tt> methods, the iterator throws a
 * <tt>ConcurrentModificationException</tt>.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis. 
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:   <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i><p>
 *
 * This class is a member of the 
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Josh Bloch and Doug Lea
 * @version 1.56, 01/23/03
 * @see Map
 * @see HashMap
 * @see Hashtable
 * @see Comparable
 * @see Comparator
 * @see Collection
 * @see Collections#synchronizedMap(Map)
 * @since 1.2
 */

/**
 * @Invariant ( this.RED==false ) && 
 *		( this.BLACK==true ) &&
 *		( this.root.parent in null ) &&
 *		( this.root!=null => this.root.color = this.BLACK ) && 
 *		( all n: TreeSetEntry | n in this.root.*(left @+ right @+ parent) @- null => ( 
 *				(n.key != null ) &&
 *				( n.left != null => n.left.parent = n ) &&
 *				( n.right != null => n.right.parent = n ) &&
 *				( n.parent != null => n in n.parent.(left @+ right) ) &&
 *				( n !in n.^parent ) &&
 *				( all x : TreeSetEntry | (( x in n.left.^(left @+ right) @+ n.left @- null ) => ( n.key > x.key )) ) &&
 *				( all x : TreeSetEntry | (( x in n.right.^(left @+ right) @+ n.right @- null ) => ( x.key > n.key ))) &&
 *				( n.color = this.RED && n.parent != null => n.parent.color = this.BLACK ) && 
 *				( ( n.left=null && n.right=null ) => ( n.blackHeight=1 ) ) &&
 *				( n.left!=null && n.right=null => ( 
 *				      ( n.left.color = this.RED ) && 
 *				      ( n.left.blackHeight = 1 ) && 
 *				      ( n.blackHeight = 1 )  
 *				)) &&
 *				( n.left=null && n.right!=null =>  ( 
 *				      ( n.right.color = this.RED ) && 
 *				      ( n.right.blackHeight = 1 ) && 
 *				      ( n.blackHeight = 1 ) 
 *				 )) && 
 *				( n.left!=null && n.right!=null && n.left.color=this.RED && n.right.color=this.RED => ( 
 *				        ( n.left.blackHeight = n.right.blackHeight ) && 
 *				        ( n.blackHeight = n.left.blackHeight ) 
 *				)) && 
 *				( n.left!=null && n.right!=null && n.left.color=this.BLACK && n.right.color=this.BLACK => ( 
 *				        ( n.left.blackHeight=n.right.blackHeight ) && 
 *				        ( n.blackHeight=n.left.blackHeight + 1 )  
 *				)) && 
 *				( n.left!=null && n.right!=null && n.left.color=this.RED && n.right.color=this.BLACK => ( 
 *				        ( n.left.blackHeight=n.right.blackHeight + 1 ) && 
 *				        ( n.blackHeight = n.left.blackHeight  )  
 *				)) && 
 *				( n.left!=null && n.right!=null && n.left.color=this.BLACK && n.right.color=this.RED => ( 
 *				        ( n.right.blackHeight=n.left.blackHeight + 1 ) && 
 *				        ( n.blackHeight = n.right.blackHeight  )  )) 
 *				)) ; 
 */
/**
 * @SpecField entries : set TreeSetEntry from this.root, this.entries.left, this.entries.right, this.entries.parent, this.entries.color, this.entries.key | 
 *            this.entries = this.root.*(left @+ right) @- null ;
 */
public //ADDED public
class TreeSet {
	private /*@ nullable @*/transient TreeSetEntry root = null;

	/**
	 * The number of entries in the tree
	 */
	private transient int size = 0;

	/**
	 * The number of structural modifications to the tree.
	 */
	private transient int modCount = 0;

	private /*static*/ static final boolean RED = false;
	//ADDED private	and static
	private /*static*/ static final boolean BLACK = true;

	/*
	 * Returns <tt>true</tt> if this map contains a mapping for the specified
	 * key.
	 *
	 * @param key key whose presence in this map is to be tested.
	 * 
	 * @return <tt>true</tt> if this map contains a mapping for the
	 *            specified key.
	 * @throws ClassCastException if the key cannot be compared with the keys
	 *                  currently in the map.
	 * @throws NullPointerException key is <tt>null</tt> and this map uses
	 *                  natural ordering, or its comparator does not tolerate
	 *            <tt>null</tt> keys.
	 */
	/**
	 * @Ensures return = true <=> (some n: TreeSetEntry | n in this.entries && n.key=aKey) ;
	 */
	public boolean contains(int aKey) {
		return getEntry(aKey) != null;
	}

	/**
	 * Returns this map's entry for the given key, or <tt>null</tt> if the map
	 * does not contain an entry for the key.
	 *
	 * @return this map's entry for the given key, or <tt>null</tt> if the map
	 *                does not contain an entry for the key.
	 * @throws ClassCastException if the key cannot be compared with the keys
	 *                  currently in the map.
	 * @throws NullPointerException key is <tt>null</tt> and this map uses
	 *                  natural order, or its comparator does not tolerate *
	 *                  <tt>null</tt> keys.
	 */
	private TreeSetEntry getEntry(int key) {
		TreeSetEntry p = root;
		while (p != null) {

			if (key == p.key)
				return p;
			else if (key < p.key)
				p = p.left;
			else
				p = p.right;
		}
		return null;
	}

	/*
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for this key, the old
	 * value is replaced.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @return previous value associated with specified key, or <tt>null</tt>
	 *         if there was no mapping for key.  A <tt>null</tt> return can
	 *         also indicate that the map previously associated <tt>null</tt>
	 *         with the specified key.
	 * @throws    ClassCastException key cannot be compared with the keys
	 *            currently in the map.
	 * @throws NullPointerException key is <tt>null</tt> and this map uses
	 *         natural order, or its comparator does not tolerate
	 *         <tt>null</tt> keys.
	 */
	/**
	 * @Modifies_Everything
	 * 
	 * @Ensures ( (some n: TreeSetEntry | n in @old(this.entries) && n.key=aKey ) => (this.entries = @old(this.entries)) ) &&
	 *          ( (no   n: TreeSetEntry | n in @old(this.entries) && n.key=aKey ) => ( some m: TreeSetEntry | ( this.entries = @old(this.entries)  @+ m ) && m.key=aKey ) ) ;
	 */
	public boolean add(int aKey) {
		TreeSetEntry t = root;

		if (t == null) {
			incrementSize();
			root = new TreeSetEntry(aKey, null);
			return false;
		}

		while (true) {
			if (aKey == t.key) {
				return true;
			} else if (aKey < t.key) {
				if (t.left != null) {
					t = t.left;
				} else {
					incrementSize();
					t.left = new TreeSetEntry(aKey, t);
					fixAfterInsertion(t.left);
					return false;
				}
			} else { // cmp > 0
				if (t.right != null) {
					t = t.right;
				} else {
					incrementSize();
					t.right = new TreeSetEntry(aKey, t);
					fixAfterInsertion(t.right);
					return false;
				}
			}
		}
	}

	private void incrementSize() {
		modCount++;
		size++;
	}

	/**
	 * Balancing operations.
	 *
	 * Implementations of rebalancings during insertion and deletion are
	 * slightly different than the CLR version.  Rather than using dummy
	 * nilnodes, we use a set of accessors that deal properly with null.  They
	 * are used to avoid messiness surrounding nullness checks in the main
	 * algorithms.
	 */

	private static boolean colorOf(TreeSetEntry p) {
		boolean black = false;
		return (p == null ? black : p.color);
	}

	private static TreeSetEntry parentOf(TreeSetEntry p) {
		return (p == null ? null : p.parent);
	}

	private static void setColor(TreeSetEntry p, boolean c) {
		if (p != null)
			p.color = c;
	}

	private static TreeSetEntry leftOf(TreeSetEntry p) {
		return (p == null) ? null : p.left;
	}

	private static TreeSetEntry rightOf(TreeSetEntry p) {
		return (p == null) ? null : p.right;
	}

	/** From CLR **/
	private void rotateLeft(TreeSetEntry p) {
		TreeSetEntry r = p.right;
		p.right = r.left;
		if (r.left != null)
			r.left.parent = p;
		r.parent = p.parent;
		if (p.parent == null)
			root = r;
		else if (p.parent.left == p)
			p.parent.left = r;
		else
			p.parent.right = r;
		r.left = p;
		p.parent = r;
	}

	/** From CLR **/
	private void rotateRight(TreeSetEntry p) {
		TreeSetEntry l = p.left;
		p.left = l.right;
		if (l.right != null)
			l.right.parent = p;
		l.parent = p.parent;
		if (p.parent == null)
			root = l;
		else if (p.parent.right == p)
			p.parent.right = l;
		else
			p.parent.left = l;
		l.right = p;
		p.parent = l;
	}

	/** From CLR **/
	private void fixAfterInsertion(final TreeSetEntry entry) {
		TreeSetEntry x = entry;
		x.color = RED;

		while (x != null && x != root && x.parent.color == RED) {
			if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
				TreeSetEntry y = rightOf(parentOf(parentOf(x)));
				if (colorOf(y) == RED) {
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				} else {
					if (x == rightOf(parentOf(x))) {
						x = parentOf(x);
						rotateLeft(x);
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					if (parentOf(parentOf(x)) != null)
						rotateRight(parentOf(parentOf(x)));
				}
			} else {
				TreeSetEntry y = leftOf(parentOf(parentOf(x)));
				if (colorOf(y) == RED) {
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				} else {
					if (x == leftOf(parentOf(x))) {
						x = parentOf(x);
						rotateRight(x);
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					if (parentOf(parentOf(x)) != null)
						rotateLeft(parentOf(parentOf(x)));
				}
			}
		}
		root.color = BLACK;
	}

	/*
	 * Removes the mapping for this key from this TreeMap if present.
	 *
	 * @param  key key for which mapping should be removed
	 * @return previous value associated with specified key, or <tt>null</tt>
	 *         if there was no mapping for key.  A <tt>null</tt> return can
	 *         also indicate that the map previously associated
	 *         <tt>null</tt> with the specified key.
	 * 
	 * @throws    ClassCastException key cannot be compared with the keys
	 *            currently in the map.
	 * @throws NullPointerException key is <tt>null</tt> and this map uses
	 *         natural order, or its comparator does not tolerate
	 *         <tt>null</tt> keys.
	 */
	/**
	 * @Modifies_Everything
	 * 
	 * @Ensures all n: TreeSetEntry | (n in this.entries <=> (n in @old(this.entries) && n.key!=aKey))
	 */
	public boolean remove(int aKey) {
		TreeSetEntry p = getEntry(aKey);
		if (p == null)
			return false;

		deleteEntry(p);
		return true;
	}

	/**
	 * Delete node p, and then rebalance the tree.
	 */
	private void deleteEntry(TreeSetEntry p) {
		decrementSize();

		// If strictly internal, copy successor's element to p and then make p
		// point to successor.
		if (p.left != null && p.right != null) {
			TreeSetEntry s = successor(p);
			p.key = s.key;

			p = s;
		} // p has 2 children

		// Start fixup at replacement node, if it exists.
		TreeSetEntry replacement = (p.left != null ? p.left : p.right);

		if (replacement != null) {
			// Link replacement to parent
			replacement.parent = p.parent;
			if (p.parent == null)
				root = replacement;
			else if (p == p.parent.left)
				p.parent.left = replacement;
			else
				p.parent.right = replacement;

			// Null out links so they are OK to use by fixAfterDeletion.
			p.left = p.right = p.parent = null;

			// Fix replacement
			if (p.color == BLACK)
				fixAfterDeletion(replacement);
		} else if (p.parent == null) { // return if we are the only node.
			root = null;
		} else { //  No children. Use self as phantom replacement and unlink.
			if (p.color == BLACK)
				fixAfterDeletion(p);

			if (p.parent != null) {
				if (p == p.parent.left)
					p.parent.left = null;
				else if (p == p.parent.right)
					p.parent.right = null;
				p.parent = null;
			}
		}
	}

	/** From CLR **/
	private void fixAfterDeletion(final TreeSetEntry entry) {
		TreeSetEntry x = entry;

		while (x != root && colorOf(x) == BLACK) {
			if (x == leftOf(parentOf(x))) {
				TreeSetEntry sib = rightOf(parentOf(x));

				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateLeft(parentOf(x));
					sib = rightOf(parentOf(x));
				}

				if (colorOf(leftOf(sib)) == BLACK
						&& colorOf(rightOf(sib)) == BLACK) {
					setColor(sib, RED);
					x = parentOf(x);
				} else {
					if (colorOf(rightOf(sib)) == BLACK) {
						setColor(leftOf(sib), BLACK);
						setColor(sib, RED);
						rotateRight(sib);
						sib = rightOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(rightOf(sib), BLACK);
					rotateLeft(parentOf(x));
					x = root;
				}
			} else { // symmetric
				TreeSetEntry sib = leftOf(parentOf(x));

				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateRight(parentOf(x));
					sib = leftOf(parentOf(x));
				}

				if (colorOf(rightOf(sib)) == BLACK
						&& colorOf(leftOf(sib)) == BLACK) {
					setColor(sib, RED);
					x = parentOf(x);
				} else {
					if (colorOf(leftOf(sib)) == BLACK) {
						setColor(rightOf(sib), BLACK);
						setColor(sib, RED);
						rotateLeft(sib);
						sib = leftOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(leftOf(sib), BLACK);
					rotateRight(parentOf(x));
					x = root;
				}
			}
		}

		setColor(x, BLACK);
	}

	private void decrementSize() {
		modCount++;
		size--;
	}

	/*
	 * Returns the successor of the specified Entry, or null if no such.
	 */
	private TreeSetEntry successor(TreeSetEntry t) {
		if (t == null)
			return null;
		else if (t.right != null) {
			TreeSetEntry p = t.right;
			while (p.left != null)
				p = p.left;
			return p;
		} else {
			TreeSetEntry p = t.parent;
			TreeSetEntry ch = t;
			while (p != null && ch == p.right) {
				ch = p;
				p = p.parent;
			}
			return p;
		}
	}
	
	//No public method uses TreeSetEntry as output/input 
	private static class TreeSetEntry {
		//All class fields and methods that semantically should be private where put as such
		private int key;	
		private /*@ nullable @*/TreeSetEntry left = null;
		private /*@ nullable @*/TreeSetEntry right = null;
		private /*@ nullable @*/TreeSetEntry parent;
		private boolean color = false;

		/**
		 * Make a new cell with given key, value, and parent, and with 
		 * <tt>null</tt> child links, and BLACK color. 
		 */
		protected TreeSetEntry(int key, TreeSetEntry parent) {
			this.key = key;

			this.parent = parent;
		}

		/**
		 * Returns the key.
		 *
		 * @return the key.
		 */
		public int getKey() {
			return key;
		}

		public int hashCode() {
			return key;
		}

	}

	
}
