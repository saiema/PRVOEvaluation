package motivating.queue.initial;

import motivating.queue.QueueNode;

public class Queue {
	
	private QueueNode front;
	private QueueNode last;
	
	public Queue() {}
	
	public void enqueue(int elem) {
		QueueNode newNode = new QueueNode(elem);
		if (front == null) {
			front = newNode;
			last = newNode;
		} else {
			last.next = newNode;
			last = newNode;
		}
	}
	
	public int peek() {
		return front.value;
	}
	
	public void dequeue() {
		front = front.next;
	}
	
	public int size() {
		int count = 0;
		for (QueueNode current = front; current != null; current = current.next) {
			count++;
		}
		return count;
	}

}
