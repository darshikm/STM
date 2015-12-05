package STM.DataStructure;

import java.util.HashMap;
import java.util.logging.Logger;

public class TLinkedList<T> {
	
	private TNode<T> head, tail;
	private static Logger LOGGER = Logger.getLogger(TLinkedList.class.getName());
	
	public TLinkedList (T min, T max) {
		head = new TNode<>(min);
		tail = new TNode<>(max);
		head.setNext(tail);
		tail.setPrev(head);
	}
	
	public void add(T value) {
		Node<T> temp = new TNode<>(value), last;
		last = tail.getPrev();
		last.setNext(temp);
		temp.setPrev(last);
		tail.setPrev(temp);
		temp.setNext(tail);
	}
	
	public boolean remove(T value) {
		Node<T> last = head.getNext();
		boolean found = false;

		while (last != tail) {
			if(last.getItem() == value) {
				found = true;
				break;
			}
			else {
                LOGGER.info("Node item := " + last.getItem());
                last = last.getNext();
            }
		}

		if(found) {
			Node<T> temp = last.getPrev();
			temp.setNext(last.getNext());
			last.getNext().setPrev(temp);
			LOGGER.severe("Found the node to be deleted!");
		}
		else LOGGER.severe("Could not find the node!");
		return found;
	}

	public void printAll() {
		Node<T> runner = head;
        LOGGER.info("Making Thread.." + Thread.currentThread().getName() +
        "; print the LinkedList contents");
		while (runner.getNext() != tail) {
			runner = runner.getNext();
			LOGGER.info(runner.getItem() + " -> ");
		}
	}
}
