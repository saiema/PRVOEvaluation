package motivating.queue.improved;

import motivating.queue.QueueNode;

public class ImprovedQueue {
	
	private QueueNode front;
	private QueueNode last;
	
	public ImprovedQueue() {}
	
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
		if (size() == 0) throw new IllegalStateException("peek on empty queue");
		return front.value;
	}
	
	public void dequeue() {
		front = front.next;
		if (front == null) {
			last = null;
		}
	}
	
	public int size() {
		int count = 0;
		for (QueueNode current = front; current != null; current = current.next) {
			count++;
		}
		return count;
	}

}
