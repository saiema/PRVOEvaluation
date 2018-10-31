//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package binheap;


/**
 * @SpecField nodes: set BinomialHeapNode from this.Nodes, this.nodes.child, this.nodes.sibling, this.nodes.parent  
 *                   | this.nodes = this.Nodes.*(child @+ sibling) @- null ;
 */
/**
 * @Invariant ( all n: BinomialHeapNode | ( n in this.Nodes.*(sibling @+ child) @- null => (
 *		            ( n.parent!=null  => n.key >=  n.parent.key )  &&   
 *		            ( n.child!=null   => n !in n.child.*(sibling @+ child) @- null ) && 
 *		            ( n.sibling!=null => n !in n.sibling.*(sibling @+ child) @- null ) && 
 *		            ( ( n.child !=null && n.sibling!=null ) => (no m: BinomialHeapNode | ( m in n.child.*(child @+ sibling) @- null && m in n.sibling.*(child @+ sibling) @- null )) ) && 
 *		            ( n.degree >= 0 ) && 
 *		            ( n.child=null => n.degree = 0 ) && 
 *		            ( n.child!=null =>n.degree=#(n.child.*sibling @- null) )  && 
 *		            ( #( ( n.child @+ n.child.child.*(child @+ sibling) ) @- null ) = #( ( n.child @+ n.child.sibling.*(child @+ sibling)) @- null )  ) && 
 *		            ( n.child!=null => ( all m: BinomialHeapNode | ( m in n.child.*sibling@-null =>  m.parent = n  ) ) ) && 
 *		            ( ( n.sibling!=null && n.parent!=null ) => ( n.degree > n.sibling.degree ) )
 * ))) && 
 * ( this.size = #(this.Nodes.*(sibling @+ child) @- null) ) &&
 * ( all n: BinomialHeapNode | n in this.Nodes.*sibling @- null => ( 
 *  ( n.sibling!=null => n.degree < n.sibling.degree ) && 
 *  ( n.parent=null ) 
 *  )) ;
 */
public class BinomialHeap /*implements java.io.Serializable*/{

	private /*@ nullable @*/ BHNode Nodes;

	private int size;

	public BinomialHeap() {
		Nodes = null;
		size = 0;
	}

	// 2. Find the minimum key
	/**
	 * @Modifies_Everything
	 * 
	 * @Requires some this.nodes ; 
	 * @Ensures ( some x: BinomialHeapNode | x in this.nodes && x.key == return ) && 
	 *          ( all y : BinomialHeapNode | ( y in this.nodes && y!=return ) => return <= y.key ) ;  
	 */
	public int findMinimum() {
		return Nodes.findMinNode().key;
	}

	// 3. Unite two binomial heaps
	// helper procedure
	private void merge(/*@ nullable @*/BHNode binHeap) {
		BHNode temp1 = Nodes, temp2 = binHeap;
		while ((temp1 != null) && (temp2 != null)) {
			if (temp1.degree == temp2.degree) {
				BHNode tmp = temp2;
				temp2 = temp2.sibling;
				tmp.sibling = temp1.sibling;
				temp1.sibling = tmp;
				temp1 = tmp.sibling;
			} else {
				if (temp1.degree < temp2.degree) {
					if ((temp1.sibling == null)
							|| (temp1.sibling.degree > temp2.degree)) {
						BHNode tmp = temp2;
						temp2 = temp2.sibling;
						tmp.sibling = temp1.sibling;
						temp1.sibling = tmp;
						temp1 = tmp.sibling;
					} else {
						temp1 = temp1.sibling;
					}
				} else {
					BHNode tmp = temp1;
					temp1 = temp2;
					temp2 = temp2.sibling;
					temp1.sibling = tmp;
					if (tmp == Nodes) {
						Nodes = temp1;
					} 
				}
			}
		}

		if (temp1 == null) {
			temp1 = Nodes;
			while (temp1.sibling != null) {
				temp1 = temp1.sibling;
			}
			temp1.sibling = temp2;
		} 
	}

	// another helper procedure
	private void unionNodes(/*@ nullable @*/BHNode binHeap) {
		merge(binHeap);

		BHNode prevTemp = null, temp = Nodes , nextTemp = Nodes.sibling;
		
		while (nextTemp != null) {
			if ((temp.degree != nextTemp.degree)
					|| ((nextTemp.sibling != null) && (nextTemp.sibling.degree == temp.degree))) {
				prevTemp = temp;
				temp = nextTemp;
			} else {
				if (temp.key <= nextTemp.key) {
					temp.sibling = nextTemp.sibling;
					nextTemp.parent = temp;
					nextTemp.sibling = temp.child;
					temp.child = nextTemp;
					temp.degree++;
				} else {
					if (prevTemp == null) {
						Nodes = nextTemp;
					} else {
						prevTemp.sibling = nextTemp;
					}
					temp.parent = nextTemp;
					temp.sibling = nextTemp.child;
					nextTemp.child = temp;
					nextTemp.degree++;
					temp = nextTemp;
				}
			}

			nextTemp = temp.sibling;
		}
	}

	// 4. Insert a node with a specific value
	/**
	 * @Modifies_Everything
	 * 
	 * @Ensures some n: BinomialHeapNode | (
	 *            n !in @old(this.nodes) &&
	 *            this.nodes = @old(this.nodes) @+ n &&
	 *            n.key = value ) ;
	 */
	public void insert(int value) {
		if (value > 0) {
			BHNode temp = new BHNode(value);
			if (Nodes == null) {
				Nodes = temp;
				size = 1;
			} else {
				unionNodes(temp);
				size++;
			}
		}
	}

	// 5. Extract the node with the minimum key
	/**
	 * @Modifies_Everything
	 * 
	 * @Ensures ( @old(this).@old(Nodes)==null => ( this.Nodes = null && return = null ) ) 
	 *       && ( @old(this).@old(Nodes)!=null => ( (return in @old(this.nodes)) &&
	 *                                              ( all y : BinomialHeapNode | ( y in @old(this.nodes.key) && y.key >= return.key ) ) && 
	 *                                              (this.nodes = @old(this.nodes) @- return ) &&
	 *                                              (this.nodes.key  @+ return.key = @old(this.nodes.key) )
	 *                                             ));
	 */
	private /*@ nullable @*/BHNode extractMin() {
		if (Nodes == null)
			return null;

		BHNode temp = Nodes, prevTemp = null;
		BHNode minNode = Nodes.findMinNode();
		while (temp.key != minNode.key) {
			prevTemp = temp;
			temp = temp.sibling;
		}

		if (prevTemp == null) {
			Nodes = temp.sibling;
		} else {
			prevTemp.sibling = temp.sibling;
		}
		temp = temp.child;
		BHNode fakeNode = temp;
		while (temp != null) {
			temp.parent = null;
			temp = temp.sibling;
		}

		if ((Nodes == null) && (fakeNode == null)) {
			size = 0;
		} else {
			if ((Nodes == null) && (fakeNode != null)) {
				Nodes = fakeNode.reverse(null);
				size--;
			} else {
				if ((Nodes != null) && (fakeNode == null)) {
					size--;
				} else {
					unionNodes(fakeNode.reverse(null));
					size--;
				}
			}
		}

		return minNode;
	}

	// 6. Decrease a key value
	public void decreaseKeyValue(int old_value, int new_value) {
		BHNode temp = Nodes.findANodeWithKey(old_value);
		decreaseKeyNode(temp, new_value);
	}

	/**
	 * 
	 * @Modifies_Everything
	 * 
	 * @Requires node in this.nodes && node.key >= new_value ;
	 * 
	 * @Ensures (some other: BinomialHeapNode | other in this.nodes && other!=node && @old(other.key)=@old(node.key))
	 *          ? this.nodes.key = @old(this.nodes.key) @+ new_value
	 *          : this.nodes.key = @old(this.nodes.key) @- @old(node.key) @+ new_value ;  
	 */
	private void decreaseKeyNode(BHNode node, int new_value) {
		if (node == null)
			return;
		node.key = new_value;
		BHNode tempParent = node.parent;

		while ((tempParent != null) && (node.key < tempParent.key)) {
			int z = node.key;
			node.key = tempParent.key;
			tempParent.key = z;

			node = tempParent;
			tempParent = tempParent.parent;
		}
	}

	// 7. Delete a node with a certain key
	public void delete(int value) {
		if ((Nodes != null) && (Nodes.findANodeWithKey(value) != null)) {
			decreaseKeyValue(value, findMinimum() - 1);
			extractMin();
		}
	}


}
// end of class BinomialHeap
