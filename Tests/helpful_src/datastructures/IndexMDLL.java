package helpful.datastructures;
import java.util.Iterator;

public class IndexMDLL<E> extends MassiveDLList<E> implements Iterable<E>{
	protected int cursorIndex;
	protected Node<E> cursor;
	public IndexMDLL(){
		super();
		cursor = head.next();
	}
	public IndexMDLL(int size){
		super(size);
		cursor = head.next();
	}
	public IndexMDLL(E[] items){
		super(items);
		cursor = head.next();
	}
	
	//info adders / changers
	public void add(E item){
		reset();
		if (cursorIndex == size){
			super.add(item);
			cursorIndex++;
			return;
		}
		super.add(item, cursorIndex, cursor);
		cursorIndex++;
	}
	public void add(E[] items){
		reset();
		for (E item : items){
			add(item);
		}
	}
	public boolean remove(){
		if (cursorIndex == 0) return false;
		reset();
		//System.out.println("IMDLL remove: " + cursor + ", " + cursorIndex);
		//System.out.println((cursor == tail) + " -- " + cursor + ", " + cursorIndex);
		boolean did = super.remove(cursorIndex-1);
		cursorIndex--;
		//cursor = cursor.prev();
		//System.out.println("IMDLL remove: " + cursor + ", " + cursorIndex);
		return did;
	}
	public boolean remove(int start, int end){
		if (end < start) return false;
		boolean did = true, temp = false;
		for (int i=start;i<end;i++){
			temp = super.remove(start);
			if (!temp) did = false;
		}
		
		if (cursor == null ? true : cursor.next() == null || cursor.prev() == null){ //if the cursor fell off during the mass removal
			cursorIndex = size > 0 ? (start >= size ? size-1 : start) : 0; //reset the cursor to a known value
			cursor = size > 0 ? getNode(start >= size ? size-1 : start) : head.next();
			if (start >= size) right();
		}
		if (cursorIndex >= end) cursorIndex -= (end-start);
		
		return did;
	}
	public boolean set(E item){
		reset();
		cursor.setData(item);
		return true;
	}
	
	//info getters
	public int getIndex(){
		return cursorIndex;
	}
	public E getCursor(){
		reset();
		return cursor.prev() == null ? null : cursor.prev().get();
	}
	public String cursorFullString(){
		return cursor.toFullString();
	}
	
	//cursor movements
	public void goTo(int index){
		reset();
		if (index != size){
			cursor = getNode(index);
			cursorIndex = index;
		}
		else{
			cursor = getNode(index-1);
			right();
			cursorIndex = index;
		}
	}
	public void move(int amount){
		reset();
		cursor = getNodeOffset(cursor, amount);
		cursorIndex += amount;
	}
	public void left(){
		reset();
		if (cursor.prev() != null && cursor.prev() != head){
			cursor = cursor.prev();
			cursorIndex--;
		}
	}
	public void right(){
		reset();
		//System.out.println("IMDLL right: " + cursor + ", " + cursor.next());
		if (cursor.next() != null){
			cursor = cursor.next();
			cursorIndex++;
		}
	}
	public boolean canGoLeft(){
		return cursor != null ? cursor.prev() != null : false;
	}
	public boolean canGoRight(){
		return cursor != null ? cursor.next() != null : false;
	}
	
	//ToString method
	/*//ToString method
	public String toString(){
		String send = "";
		Node<E> curr = elements.get(0);
		while(curr != null ? curr.get() != null : false){
			send += curr.get().toString();
			curr = curr.next();
		}
		return send;
	}*/
	public IndexMDLL subArray(int start){
		return subArray(start, size);
	}
	public IndexMDLL<E> subArray(int start, int end){ //returns an indexMDLL array from this arrays start element +1 to the end element inclusive
		if (start >= end) throw new IllegalArgumentException("Start value of " + start + " cannot be greater than or equal to the end value of " + end);
		IndexMDLL<E> send = new IndexMDLL<E>(((end-start) / DEFAULT_NODE_SIZES)+1);
		Node<E> endNode = end == size ? (getNode(end-1).next()) : getNode(end);
		Node<E> copier = getNode(start);
		while (copier != endNode){
			send.add(copier.get());
			copier = copier.next();
		}
		return send;
	}
	
	//reset the cursor if it ever falls off
	protected void reset(){
		if (cursor != null) return;
		cursor = head.next();
		cursorIndex = 0;
	}
	
	//iterator stuff
	public Iterator<E> iterator(){
		return new MDLListIterator();
	}
}