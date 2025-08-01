package helpful.datastructures;
import java.util.Iterator;

public class MyHashSet<E> implements Iterable<E>{
    private Object[] arr;
    private int size;
    private DLList<E> list;

    public MyHashSet(){
        size=675;
        arr = new Object[size];
        list = new DLList<E>();
    }
	public MyHashSet(MyHashSet<E> other){
		size = other.size();
		arr = new Object[size];
		list = new DLList<E>();
		for (E item : other){
			this.add(item);
		}
	}
    public boolean add(E val){
        int location = val.hashCode();
        if (location > arr.length){
            updateSize(location+37);
        }
        if (arr[location] == null){
            arr[location] = val;
            list.add(val);
            return true;
        }
        return false;
    }
    public void updateSize(int size){
        Object[] temp = new Object[size];
        for (int i = 0; i < arr.length; i++){
            temp[i] = arr[i];
        }
        arr = temp;
        this.size = size;
    }
    public void clear(){
        arr = new Object[size];
        list.clear();
    }
    public boolean contains(E val){
        int location = val.hashCode();
        if (location >= arr.length || location <0){
            return false;
        }
        return arr[location] != null;
    }
    public boolean remove(E val){
        int location = val.hashCode();
        if (location > arr.length){
            return false;
        }
        if (arr[location] != null){
            arr[location] = null;
            boolean win = list.remove(val);
            return true;
        }
        return false;
    }
    public int size(){
        return list.size();
    }
    public DLList<E> toDLList(){
        return list;
    }
	public void sortList(){
        list.mergeSort();
    }
    public String toString(){
        return list.toString();
    }
    @SuppressWarnings("unchecked")
    public E get(E data){
        int hashCode = data.hashCode();
        if (hashCode > arr.length){
            return null;
        }
        return (E)arr[hashCode];
    }
    public E get(int num){
        return list.get(num);
    }
	@SuppressWarnings("unchecked")
	public void set(E data){
		int loc = data.hashCode();
		boolean res = add(data);
		if (res) return; //added successfully
		list.remove((E)arr[loc]);
		arr[loc] = data;
		list.add(data);
	}
	public Iterator<E> iterator(){
		return list.iterator();
	}
}