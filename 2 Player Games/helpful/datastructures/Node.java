package helpful.datastructures;
public class Node<E>{
    private E data;
    private Node<E> next, prev;
    public Node(E data){
        this.data = data;
        next = null;
        prev = null;
    }
    public E get(){
        return data;
    }
    public Node<E> next(){
        return next;
    }
    public Node<E> prev(){
        return prev;
    }
    public void setNext(Node<E> node){
        next = node;
        node.setPrev(this);
    }
    public void setPrev(Node<E> node){
        prev = node;
    }
    public void setData(E data){
        this.data = data;
    }
    public String toString(){
        return data.toString();
    }
    @SuppressWarnings("unchecked")
    public boolean equals(Object o){
        if (o instanceof Node<?>){
            Node<E> other = (Node<E>)(o);
            return other.get().equals(data);
        }
        return false;
    }
    public int compareTo(Node<E> other){
        return other.toString().compareTo(toString());
    }
}