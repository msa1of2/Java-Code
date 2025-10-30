package helpful.datastructures;
public class Queue<E>{
    private DLList<E> queue;
    private int cursor;
    public Queue(){
        cursor = 0;
        queue = new DLList<E>();
    }
    public Queue(int size){
        this();
    }
    public void enqueue(E o){
        //System.out.println(queue.isEmpty());
        queue.add(o);
        cursor++;
    }
    public E dequeue(){
        E send = queue.get(0);
        queue.remove(0);
        cursor--;
        return send;
    }
    public E peek(){
        return queue.get(0);
    }
    public int size(){
        return cursor;
    }
    public boolean isEmpty(){
        return queue.isEmpty();
    }
    public String toString(){
        return queue.toString();
    }
}