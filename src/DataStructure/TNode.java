package STM.DataStructure;

import STM.Atomic.AtomicObject;
import STM.Atomic.LockObject;
import STM.Exceptions.AbortedException;
import org.omg.CORBA.NO_IMPLEMENT;

import java.util.logging.Logger;

public class TNode<T> implements Node<T> {
    private static Logger LOGGER = Logger.getLogger(TLinkedList.class.getName());
    AtomicObject<SNode<T>> atomic;
    
    public TNode(T myItem) {
        atomic = new LockObject<>(new SNode<>(myItem));
    }

    @Override
    public T getItem() {
        T item = null;
        try {
            item = atomic.openRead().getItem();
            if (!atomic.validate())
                throw new AbortedException();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    @Override
    public void setItem(T value) {
        try {
            atomic.openWrite().setItem(value);
            if (!atomic.validate())
                throw new AbortedException();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Node<T> getNext() {
        Node<T> retNode = null;
        try {
            retNode = atomic.openRead().getNext();
            if (!atomic.validate())
                throw new AbortedException();
        } catch (Exception e) {
            LOGGER.info("getNext open read threw an exception");
            e.printStackTrace();
        }
        return retNode;
    }

    @Override
    public Node<T> getPrev() {
        Node<T> retNode = null;
        try {
            retNode = atomic.openRead().getPrev();
            if (!atomic.validate())
                throw new AbortedException();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retNode;
    }


    @Override
    public void setNext(Node<T> value) {
        try {
            atomic.openWrite().setNext(value);
            if (!atomic.validate())
                throw new AbortedException();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPrev(Node<T> value) {
        try {
            atomic.openWrite().setPrev(value);
            if (!atomic.validate())
                throw new AbortedException();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
