package helpful.datastructures;
import java.util.Iterator;

public class MyArrayList<E> implements Iterable<E>{
    private Object[] list;
    private int size, capacity;

    public MyArrayList(){
        capacity = 10;
        size=0;
        list = new Object[capacity];
    }
    public MyArrayList(int size){
        capacity = size;
        this.size=0;
        list = new Object[capacity];
    }
	public MyArrayList(int size, E element){
		capacity=size+1;
		this.size=size;
		list = new Object[capacity];
		for (int i=0;i<size;i++){
			list[i] = element;
		}
	}
	public void clear(){
		this.size = 0;
		list = new Object[capacity];
	}
    public void changeSize(int newSize){
        Object[] temp = new Object[newSize];
        for (int i=0;i<list.length;i++){
            temp[i] = list[i];
        }
        list = temp;
        this.capacity = newSize;
    }
    public boolean add(E element){
        list[size] = element;
        size++;
        if (size == capacity){
            changeSize(capacity+10);
        }
        return true;
    }
    public void add(int index, E element){
        for (int i=size;i>=index;i--){
            if (i!=0){
                list[i] = list[i-1];
            }
        }
        list[index] = element;
        size++;
        if (size == capacity){
            changeSize(capacity+10);
        }
    }
    @SuppressWarnings("unchecked")
    public E get(int index){
        if (index >= 0 && ((index < size && size > 0) || (size ==0 && index == 0))){
            return (E) list[index];
        }
        System.out.println(index + ", " + size);
        throw new IndexOutOfBoundsException();
    }
    @SuppressWarnings("unchecked")
    public E remove(int index){
        E send = (E)list[index]; 

        for (int i=index;i<size;i++){
            list[i] = list[i+1];
        }

        size--;
        return send;
    }
    public E remove(E element){
        E send = element;
        for (int i = 0;i<size;i++){
            if (list[i].equals(element)){
                for (int j=i;j<size;j++){
                    list[j] = list[j+1];
                }
                size--;
                break; //only find one element
            }
        }
        return send;
    }
    public void set(int index, E element){
        list[index] = element;
    }
    public String toString(){
        String send = "";
        for (int i=0;i<size;i++){
            send += (i+1) + ", " + list[i].toString() + "\n";
        }
        return send;
    }
	public int indexOf(E item){
		for (int i=0;i<size;i++){
			if (item.equals(list[i])){
				return i;
			}
		}
		return -1;
	}
    @SuppressWarnings("unchecked")
    public void swap(int index1, int index2){
        E temp = (E)list[index1];
        list[index1] = list[index2];
        list[index2] = temp;
    }
    public int size(){
        return size;
    }
	public int capacity(){
		return capacity;
	}
	public Iterator<E> iterator(){
		return new MyArrayListIterator();
	}
	private void removeItr(int index){
		remove(index);
	}
	private class MyArrayListIterator implements Iterator<E>{
		private int cursor=0;
		public MyArrayListIterator(){}
		@SuppressWarnings("unchecked")
		public E next(){
			return (E)list[cursor++];
		}
		public boolean hasNext(){
			return cursor < size;
		}
		public void remove(){
			if (cursor == -1 || cursor == 0){
				return; //cant remove the one before this if it doesn't exist
			}
			removeItr(cursor-1);
		}
	}
}