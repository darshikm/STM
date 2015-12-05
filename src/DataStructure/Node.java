package STM.DataStructure;

/**
 * Created by Mukhtar on 11/3/2015.
 */
public interface Node<T>  {
    T getItem();
    void setItem(T value);
    Node<T> getNext();
    Node<T> getPrev();
    void setNext(Node<T> value);
    void setPrev(Node<T> value);
}
