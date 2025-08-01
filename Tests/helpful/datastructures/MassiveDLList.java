package helpful.datastructures;
import helpful.datastructures.MyArrayList;
import java.util.Iterator;
public class MassiveDLList<E> implements Iterable<E>{
	protected MyArrayList<Node<E>> elements;
	protected MyArrayList<Integer> nodeSizes; // first 24 bits - total position to beginning, last 8 bits - total size to next node
	protected Node<E> head, tail;
	
	protected int size, DLListSize; //Size tracks size of the entire list, DLListSize just tracks where the end of the tail is
	
	public static final int DEFAULT_NODE_SIZES = 127; //Size between nodes when adding to the end, max size of 127 for bit reservation size limits
	
	public MassiveDLList(){
		this(50);
	}
	public MassiveDLList(int size){
		elements = new MyArrayList<Node<E>>(size);
		nodeSizes = new MyArrayList<Integer>(size);
		
		head = new Node<E>(null); //start of the list
		tail = new Node<E>(null); //end of the Nodes
		head.setNext(tail); //connect them
		elements.set(0, head); //lock the head in the first slot
		
		this.size = 0;
		this.DLListSize = 0; //only added to when adding onto tail aka tail gets pushed forward
	}
	public MassiveDLList(E[] items){
		this(items.length/DEFAULT_NODE_SIZES);
		add(items);
	}
	
	//Mutating items to the list
	public void add(E item){
		Node<E> newNode = new Node<E>(item); //create the new node
		int i=0, total=0;
		for (i=0;i<elements.size();i++){
			if (elements.get(i) == null) break;
		}
		
		if (i!=0)i--;
		
		tail.prev().setNext(newNode); //do connection stuff
		newNode.setNext(tail);
		
		if (DLListSize % DEFAULT_NODE_SIZES == 0){
			int index = DLListSize/DEFAULT_NODE_SIZES; //save the index for easier use
			elements.set(index, newNode); //tail got far enough, staple this node into the list for easier access
			
			if (elements.size() > nodeSizes.size()) nodeSizes.add(0);
			setNodeSize(index, 1);
			if (index > 0) setTotal(index, getTotal(index-1) + getNodeSize(index-1));
		}
		else
			addTo(i);
		
		if (elements.get(DLListSize / DEFAULT_NODE_SIZES) == null) //special case when list is forcibly emptied and refilled
			elements.set(0, newNode);
		
		
		//increase size
		size++;
		DLListSize++; //increase tail location
	}
	public void add(E[] items){
		for (E item : items){
			add(item);
		}
	}
	public void add(E item, int index){
		//adds the item so that it is at index: index, will throw errors if the index is less than 0 or bigger than the current size of the list
		//This method will not change the staple node near it, it might 
		//index--; //spot fix for new element going on the place after the index
		if (index >= size || index < 0) throw new IndexOutOfBoundsException("Index: " + index + " out of bounds for Size: " + size); //index protection
		if (index == size){ //end of list, use other method
			add(item);
			return;
		}
		
		Node<E> temp = getNode(index);
		add(item, index, temp);
	}
	protected void add(E item, int index, Node<E> node){
		if (index > size || index < 0) throw new IndexOutOfBoundsException("Index: " + index + " out of bounds for Size: " + size); //index protection
		if (node == null) throw new IllegalArgumentException("Node cannot be null");
		if (index == size){ //end of list, use other method
			this.add(item);
			return;
		}
		Node<E> create = new Node<E>(item);
		node.prev().setNext(create);
		create.setNext(node);
		
		int ind = getLeftIndex(index);
		
		//special case where the index we are inserting into is 0
		if (index == 0){
			elements.set(ind, create);
		}
		
		addTo(ind);
		size++;
		if (index == size-1) DLListSize++;//we are adding to the place where the DLList will be influenced, should hopefully be never triggered
		checkNewStaple(ind, index, create);
	}
	public boolean remove(E item){ //single item removal, returns true if the item was found in the list and successfully removed
		int elementIndex = elements.indexOf(new Node<E>(item));
		if (elementIndex != -1){ //this element is stapled down, will need to replace
			Node<E> node = elements.get(elementIndex);
			if (node.next() == tail || node.next() == null) elements.set(elementIndex, null); //replace the node with emptiness
			else elements.set(elementIndex, node.next()); //replace the node with the next one
			//remove from sequence
			node.prev().setNext(node.next());
			//disconect with node lists
			node.setPrev(null);
			node.setNext(null);
			removeFrom(elementIndex); //reduce size of this element
			size--;
			return true;
		}
		//item not found among the stapled items, go searching for it
		Node<E> curr = head.next();
		elementIndex = 0;
		for (int i=0;i<size && (curr!=null ? curr.get() != item : false);i++){
			if (elements.get(elementIndex).equals(curr)) elementIndex ++;
			curr = curr.next();
		}
		if (curr != null ? curr.get() == item : false){
			curr.prev().setNext(curr.next());//remove from sequence
			//disconect with node lists
			curr.setPrev(null);
			curr.setNext(null);
			removeFrom(elementIndex > 0 ? elementIndex-1 : 0);
			size--;
			return true;
		}
		return false;
	}
	public boolean[] remove(E[] items){// bulk removal, returns an array of booleans representing if that value was removed or not, does not change items[]
		boolean[] send = new boolean[items.length];
		for (int i=0;i<items.length;i++){
			send[i] = remove(items[i]);
		}
		return send;
	}
	public boolean remove(int index){ //single index removal, returns true if successfully removed (always unless index <0 or >=size
		if (index >= size || index < 0) throw new IndexOutOfBoundsException("Index: " + index + " out of bounds for Size: " + size);
		Node<E> toRemove = getNode(index);
		//find out if the element is stapled or not
		int elementIndex = getLeftIndex(index);
		//System.out.println("MDLL remove int: " + elementIndex);
		if (elementIndex != -1 && index == getTotal(elementIndex)){
			if (toRemove.next() == tail || toRemove.next() == null) elements.set(elementIndex, null); //replace the node with emptiness
			else elements.set(elementIndex, toRemove.next()); //replace the node with the next one
			//remove from sequence
			toRemove.prev().setNext(toRemove.next());
			//disconect with node list
			toRemove.setPrev(null);
			toRemove.setNext(null);
			removeFrom(elementIndex);
			size--;
			return true;
		}
		elementIndex = nearestStapleIndex(index);
		toRemove.prev().setNext(toRemove.next());//remove from sequence
		//disconect with node list
		toRemove.setPrev(null);
		toRemove.setNext(null);
		removeFrom(elementIndex);
		size--;
		if (elementIndex == elements.size()-1) DLListSize--; //removing from the final section, reduce the DLList size 
		return true;
		
		//Param string is showing the node sizes list is not changing 
	}
	public boolean[] remove(int[] indexs){
		boolean[] send = new boolean[indexs.length];
		for (int i=0;i<indexs.length;i++){
			send[i] = remove(indexs[i]);
		}
		return send;
	}
	
	//Getting values from this structure
	public E get(int index){
		return getNode(index).get();
	}
	public int size(){
		return size;
	}
	public int capacity(){
		return elements.capacity();
	}
	
	//Node methods
	protected Node<E> getNode(int index){
		if (index >= size || index < 0) throw new IndexOutOfBoundsException("Index: " + index + " out of bounds for Size: " + size);
		int i=0, total=0;
		for (i=0;i<elements.size();i++){
			total += getNodeSize(i);
			if (total > index) break;
		}
		//approach it from a node on the right, using each nodes .next() function
		if (getNodeSize(i)-(total-index) < getNodeSize(i)/2 || (i+1 < elements.size() ? elements.get(i+1) == null : true)){
			return getNodeOffset(elements.get(i), getNodeSize(i)-(total-index));
		}
		//approach from node on the left, using each node's .prev() function
		return getNodeOffset(elements.get(i+1), -1*(total-index));
		
	}
	protected Node<E> getNodeOffset(Node<E> node, int amount){
		//negative numbers go previous, positive numbers go next
		if (amount > 0){
			for (int i=0;i<amount && node != null;i++){
				node = node.next();
			}
			return node;
		}
		for (int i=0;i<amount*-1 && node != null;i++){
			node = node.prev();
		}
		return node;
	}
	protected Node<E> nearestStaple(int index){
		return elements.get(nearestStapleIndex(index));
	}
	protected int nearestStapleIndex(int index){
		if (index >= size || index < 0) throw new IndexOutOfBoundsException("Index: " + index + " out of bounds for Size: " + size);
		int i=0, total=0;
		for (i=0;i<elements.size();i++){
			total += getNodeSize(i);
			if (total > index) break;
		}
		//approach it from a node on the left, using each nodes .next() function
		if (getNodeSize(i)-(total-index) < getNodeSize(i)/2 || (i+1 < elements.size() ? elements.get(i+1) == null : true)){
			return i;
		}
		//approach from node on the right, using each node's .prev() function
		return i+1;
	}
	protected void checkNewStaple(int leftIndex, int nodeIndex, Node<E> node){
		if (getNodeSize(leftIndex) < 2*DEFAULT_NODE_SIZES) return; //Size not big enough to warrent a new staple node
		node = getNodeOffset(node, getNodeSize(leftIndex)/2 - (nodeIndex - getTotal(leftIndex))); //node is now the exact middle node of this section
		shiftAdd(leftIndex+1, node); //staples node to element list, sets the size to 0
		//change the nodeSize for this index and the left index to match the new list
		int nodeSize = getNodeSize(leftIndex);
		int total = getTotal(leftIndex);
		setNodeSize(leftIndex, nodeSize/2);
		setNodeSize(leftIndex+1, nodeSize/2 + (nodeSize%2)); //the odd number will go to the new staple
		setTotal(leftIndex+1, total + nodeSize/2);
	}
	protected void shift(int index){
		if (nodeSizes.size() < index) return;
		//shifts the nodeSizes and elements lists down by one starting at index
		if (nodeSizes.size() == nodeSizes.capacity()){ //if there is no room for the shift to take place
			nodeSizes.increaseSize();
			elements.increaseSize();
		}
		//actual shift
		for (int i=nodeSizes.size();i>=index;i--){
			nodeSizes.set(i, nodeSizes.get(i-1));
			elements.set(i, elements.get(i-1));
		}
		//set new values to preset empty ones
		nodeSizes.set(index, 0);
		elements.set(index, null);
	}
	protected void shiftAdd(int index, Node<E> node){
		//shifts the nodeSizes and elements lists down by one to the index, sets the elements of that index to the node, sets the size to 0
		shift(index);
		elements.set(index, node);
		nodeSizes.set(index, 0);
	}
	
	//get the index of the left staple node / nodeSizes
	protected int getLeftIndex(int index){
		if (index >= size || index < 0) throw new IndexOutOfBoundsException("Index: " + index + " out of bounds for Size: " + size);
		int i=0, total=0;
		for (i=0;i<elements.size();i++){
			total += getNodeSize(i);
			if (total > index) break;
		}
		return i--;
	}
	
	//count functions
	protected void addTo(int index){
		increaseTotal(index+1, 1);
		increaseNodeSize(index, 1);
	}
	protected void removeFrom(int index){
		decreaseTotal(index+1, 1);
		decreaseNodeSize(index, 1);
	}
	protected void increaseTotal(int index, int amount){
		//increase the total section of the count at this index and all the index that come after it
		int add; //reduce memory useage
		for (int i=index;i<nodeSizes.size();i++){
			add = amount + getTotal(i) & 0xff_ff_ff; //first 24 bits saved
			nodeSizes.set(i, nodeSizes.get(i) & 0xff); //reset Total to 0
			nodeSizes.set(i, nodeSizes.get(i) | (add << 8)); //put the total back in there
		}
		//System.out.println("MDLL increase Total <--");
	}
	protected void decreaseTotal(int index, int amount){
		increaseTotal(index, ~amount+1); //amount * -1
	}
	protected void increaseNodeSize(int index, int amount){
		amount += getNodeSize(index) & 0xff;
		nodeSizes.set(index, nodeSizes.get(index) & 0xff_ff_ff_00); //reset node size to 0
		nodeSizes.set(index, nodeSizes.get(index) | amount); //forces the new amount to be an 8 bit size number
	}
	protected void decreaseNodeSize(int index, int amount){
		increaseNodeSize(index, ~amount+1); //amount * -1
	}
	protected int getTotal(int index){
		return (nodeSizes.get(index) >>> 8) & 0xff_ff_ff;
	}
	protected int getNodeSize(int index){
		return (nodeSizes.get(index) & 0xff);
	}
	protected void setTotal(int index, int total){
		nodeSizes.set(index, nodeSizes.get(index) & 0xff); //saves whatever value relative size is, voids total size
		nodeSizes.set(index, nodeSizes.get(index) | (total << 8)); //sets total to this new value
	}
	protected void setNodeSize(int index, int nodeSize){
		nodeSizes.set(index, nodeSizes.get(index) & 0xff_ff_ff_00); //saves total number while voiding nodeSize info
		nodeSizes.set(index, nodeSizes.get(index) | (nodeSize&0xff)); //saves only the last 8 bits of the node size
	}
	
	//String info
	public String toString(){
		String send = "";
		Node<E> curr = elements.get(0);
		while(curr != null ? curr.get() != null : false){
			send += curr.get().toString() + ", ";
			curr = curr.next();
		}
		return send + " -- " + paramString() + " -- \n" + toFullString();
	}
	public String paramString(){
		String send = "";
		for (int i =0;i<elements.size();i++){
			send += i + ": [" +elements.get(i) + "] {" + getNodeSize(i) + "," + getTotal(i) + "}"; 
		}
		return send;
	}
	public String toFullString(){
		String send = "";
		Node<E> curr = head.next();
		while(curr != null ? curr.get() != null : false){
			send += "[" + curr.prev() + " <- " + curr.get().toString() + " -> " + curr.next() + "], ";
			curr = curr.next();
		}
		return send.replaceAll("\n", "~").replaceAll("\t", "!");
	}
	
	//iterator stuff
	public Iterator<E> iterator(){
		return new MDLListIterator();
	}
	protected class MDLListIterator implements Iterator<E>{
		private MassiveDLList<E>.Node<E> cursor = head;
		public MDLListIterator(){}
		public E next(){
			cursor = cursor.next();
			return cursor.get();
		}
		public boolean hasNext(){
			return cursor.next() != null ? cursor.next().get() != null : false;
		}
	}
	
	protected class Node<E>{
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
			if (node != null)node.setPrev(this);
		}
		public void setPrev(Node<E> node){
			prev = node;
		}
		public void setData(E data){
			this.data = data;
		}
		public String toString(){
			return data == null ? "\'Null\'" : data.toString();
		}
		public String toFullString(){
			return "[" + prev + "<-" + toString() + "->" + next + "]";
		}
		@SuppressWarnings("unchecked")
		public boolean equals(Object o){
			if (o instanceof MassiveDLList<?>.Node<?>){
				Node<E> other = (Node<E>)(o);
				return other.get().equals(data);
			}
			return false;
		}
		public int compareTo(Node<E> other){
			return other.toString().compareTo(toString());
		}
	}
	
}