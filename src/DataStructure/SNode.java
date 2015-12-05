package STM.DataStructure;

import STM.Atomic.Copyable;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Mukhtar on 11/3/2015.
 */
public class SNode<T> implements Node<T>, Copyable<SNode<T>> {

    Node<T> next, prev;
    T item;

    public SNode(){};

    public SNode(T myItem) {
        item = myItem;
        next = null;
        prev = null;
    }

    @Override
    public T getItem() {
        return item;
    }

    @Override
    public void setItem(T value) {
        item = value;
    }

    @Override
    public Node<T> getNext() {
        return next;
    }

    @Override
    public Node<T> getPrev() {
        return prev;
    }

    @Override
    public void setNext(Node<T> value) {
        next = (value);
    }

    @Override
    public void setPrev(Node<T> value) {
        prev = (value);
    }

    @Override
    public void copyTo(SNode<T> target) {
        target.item = item;
        target.next = next;
        target.prev = prev;
    }
}
