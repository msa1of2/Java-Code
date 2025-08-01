package helpful.datastructures;
import java.util.Iterator;

public class MyArrayList<E> implements Iterable<E>{
    private Object[] list;
    private int size, capacity, sizeChange;

    public MyArrayList(){
        capacity = 10;
		sizeChange = 10;
        size=0;
        list = new Object[capacity];
    }
    public MyArrayList(int size){
        capacity = size;
        this.size=0;
		sizeChange = 10;
        list = new Object[capacity];
    }
	public MyArrayList(int size, E element){
		capacity=size+1;
		sizeChange = 10;
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
    public void increaseSize(){
		changeSize(size+sizeChange);
	}
	private void changeSize(int newSize){
		if (newSize < size) throw new IllegalArgumentException("New Size " + newSize + " too small for an array of size " + size);
        Object[] temp = new Object[newSize];
        System.arraycopy(list, 0, temp, 0, list.length);
		list = temp;
        this.capacity = newSize;
    }
	public void changeSizeChange(int newChangeSize){
		this.sizeChange = newChangeSize;
	}
    public boolean add(E element){
        list[size] = element;
        size++;
        if (size == capacity){
            changeSize(capacity+sizeChange);
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
            changeSize(capacity+sizeChange);
        }
    }
    @SuppressWarnings("unchecked")
    public E get(int index){
        if (index >= 0 && ((index < size && size > 0) || (size ==0 && index == 0))){
            return (E) list[index];
        }
        throw new IndexOutOfBoundsException("Index: " + index + " out of bounds for Size: " + size);
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
		if (index < 0) return;
		if (index > list.length){
			changeSize(list.length + (list.length-index) + sizeChange);
			size += list.length-index-1;
		}
		if (index == size) size++;
        list[index] = element;
    }
    public String toString(){
        String send = "";
        for (int i=0;i<size;i++){
            send += (i) + ", " + (list[i] == null ? "null" : list[i].toString()) + "\n";
        }
        return send;
    }
	public int indexOf(E item){
		if (item == null) return -1;
		for (int i=0;i<size;i++){
			if (item == list[i]){
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