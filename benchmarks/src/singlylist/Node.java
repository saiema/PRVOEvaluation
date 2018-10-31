package singlylist;

import java.io.Serializable;


/**
 *StrictlySortedSinglyLinkedList's nodes
 *@author
 */

public  class Node implements Serializable{

	public  static final long serialVersionUID = 2; 
	public int value;

	transient int _index;
	public Node next;

	public String toString() {
		return "[" + value + "]";
	}


		
	
}
