package helpful.datastructures;
import java.util.Iterator;
import helpful.datastructures.MyArrayList;
public class Heap<E extends Comparable<E>> implements Iterable<E>{
    private MyArrayList<E> items;
    public Heap(int capacity){
        this.items = new MyArrayList<E>(capacity);
    }
    public Heap(){
        this.items= new MyArrayList<E>(50);
    }
    public void add(E item){
        items.add(item);
        swapUp(items.size()-1);
    }
	public void add(E[] items){
		for (E item : items){
			add(item);
		}
	}
    private void swapUp(int index){
        //int layer = (int)(Math.log(index) / Math.log(2));
        //boolean isLeft = index % 2 == 1;
        int parent = (index-1)/2;
        if (parent < 0 || parent == index){
            return; //nowhere to swap to
        }
        if (items.get(parent).compareTo(items.get(index)) > 0){
            E temp = items.get(parent);
            items.set(parent, items.get(index));
            items.set(index, temp);
            swapUp(parent);
        }
    }
    private void swapDown(int index){
        int left = index*2+1;
        int right = index*2+2;
        if (left >= items.size() || right >= items.size()){
            return; //nowhere to swap to
        }
        if (items.get(right).compareTo(items.get(left)) >= 0){
            E temp = items.get(left);
            items.set(left, items.get(index));
            items.set(index, temp);
            swapDown(left);
        }
        else{
            E temp = items.get(right);
            items.set(right, items.get(index));
            items.set(index, temp);
            swapDown(right);
        }
    }
    public String toString(){
        String send = "";
        int num = 1;
        for (int i=1;i<=items.size();i++){
            if (i == num +1){
                send += "\n";
                num *= 2;
                num++;
            }
            send += items.get(i-1) + " - ";
        }
        return send;
    }
    public E poll(){
        if (size() > 0){
            E send = items.get(0);
            remove(send);
            return send;
        }
        return null;
    }
    public E peek(){
        return items.get(0);
    }
    public void remove(E item){
        for (int i=0;i<items.size();i++){
            if (items.get(i).equals(item)){
                items.swap(i, items.size()-1);
                items.remove(items.size()-1);
                swapDown(i);
                return;
            }
        }
    }
    public boolean isEmpty(){
        return items.size() == 0;
    }
    public int size(){
        return items.size();
    }
    public E get(int index){
        return items.get(index);
    }
    public String toNiceString(){
        String send = "";
        for (int i=0;i<items.size();i++){
            send += items.get(i).toString() + "\n";
        }
        return send;
    }
	public boolean contains(E item){
		for (E thing : items){
			if (item.equals(thing)){
				return true;
			}
		}
		return false;
	}
	public E get(E item){
		for (E thing : items){
			if (item.equals(thing)){
				return thing;
			}
		}
		return null;
	}
    public Iterator<E> iterator(){
        return items.iterator();
    }
	public MyArrayList<E> getArrayList(){
		return items;
	}
	public int search(E item){
		return items.indexOf(item);
	}
}