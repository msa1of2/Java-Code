package helpful.datastructures;
public class Stack<E>{
    private DLList<E> stack;
    private int cursor;
    public Stack(){
        cursor = 0;
        stack = new DLList<E>();
    }
    public void push(E o){
        stack.add(o);
        cursor++;
    }
    public E pop(){
        cursor--;
        E send = stack.get(stack.size()-1);
        stack.remove(stack.size()-1);
        return send;
    }
    public E peek(){
        return stack.get(-1);
    }
    public int size(){
        return cursor;
    }
    public boolean isEmpty(){
        return stack.isEmpty();
    }
    public String toString(){
        return stack.toString();
    }
}