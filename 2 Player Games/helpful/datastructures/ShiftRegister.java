package helpful.datastructures;
import java.util.Iterator;

public class ShiftRegister<E> implements Iterable<E>{
	private Object[] list;
	private int pointer, end;
	public ShiftRegister(int capacity){
		list = new Object[capacity];
		pointer = 0;
		end = 0;
	}
	public void add(E[] items){
		for (E item : items){
			add(item);
		}
	}
	public void add(E item){
		list[pointer] = item;
		pointer = wrap(pointer+1, list.length, 0);
		if (pointer == end){
			//wrapped around to the start
			end = wrap(pointer+1, list.length, 0);
		}
	}
	@SuppressWarnings("unchecked")
	public E pop(){
		if (pointer == end) return null;
		pointer = wrap(pointer-1, list.length, 0);
		E send = (E)list[pointer];
		list[pointer] = null;
		
		
		return send;
	}
	public E remove(){
		return pop();
	}
	public ShiftRegister(){
		this(10);
	}
	public int size(){
		return wrap(pointer-end, list.length, 0);
	}
	public String toString(){
		String send = "";
		for (Object item : this){
			send += (item == null ? "null" : item.toString()) + ", ";
		}
		return send;
	}
	public String fullString(){
		String send = "";
		for (int i=0;i<list.length;i++){
			send += list[i] + ", ";
		}
		return send;
	}
	public int getTop(){
		return pointer;
	}
	public int getBottom(){
		return end;
	}
	public static int wrap(int num, int top, int bottom){ //wraps a number num between top-1 and bottom inclusive
		if (top == bottom) return num; //stack over flow protection and div by 0 protection
		//includes bottom, excludes top
		if (num < top && num >= bottom){
			return num;
		}
		if (num < bottom){
			return wrap(num+top-bottom, top, bottom);
		}
		if (num >= top){
			return ((num-top)%(top-bottom))+bottom;
		}
		return num;
	}
	public Iterator<E> iterator(){
		return new ShiftRegisterIterator<E>();
	}
	private class ShiftRegisterIterator<E> implements Iterator<E>{
		private int cursor = pointer;
		public ShiftRegisterIterator(){}
		@SuppressWarnings("unchecked")
		public E next(){
			cursor = wrap(cursor-1, list.length, 0);
			return (E)list[cursor];
		}
		public boolean hasNext(){
			return cursor != end;
		}
	}
}