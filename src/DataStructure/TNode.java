package STM.DataStructure;

import STM.Atomic.AtomicObject;
import STM.Atomic.LockObject;
import STM.Exceptions.AbortedException;
import STM.Exceptions.PanicException;
import org.omg.CORBA.NO_IMPLEMENT;

import java.awt.*;
import java.util.logging.Logger;
import java.lang.Exception;

public class TNode<T> implements Node<T> {
    private static Logger LOGGER = Logger.getLogger(TLinkedList.class.getName());
    AtomicObject<SNode<T>> atomic;
    
    public TNode(T myItem) {
        atomic = new LockObject<>(new SNode<>(myItem));
    }


    @Override
    public T getItem() throws AbortedException, PanicException {
        T item = atomic.openRead().getItem();
        if (!atomic.validate()) throw new AbortedException();
        return item;
    }

    @Override
    public void setItem(T value) throws AbortedException, PanicException {
        atomic.openWrite().setItem(value);
    }

    @Override
    public Node<T> getNext() throws AbortedException, PanicException {
        Node<T> retNode = atomic.openRead().getNext();
        if (!atomic.validate()) throw new AbortedException();
        return retNode;
    }

    @Override
    public Node<T> getPrev() throws AbortedException, PanicException {
        Node<T> retNode = atomic.openRead().getPrev();
        if (!atomic.validate()) throw new AbortedException();
        return retNode;
    }


    @Override
    public void setNext(Node<T> value) throws AbortedException, PanicException {
        atomic.openWrite().setNext(value);
    }

    @Override
    public void setPrev(Node<T> value) throws AbortedException, PanicException {
        atomic.openWrite().setPrev(value);
    }
}