package helpful.datastructures;
import java.util.Iterator;

public class DLList<E> implements Iterable<E>{
    private Node<E> head, tail;
    private int size;
    public DLList(){
        head = new Node<E>(null);
        tail = new Node<E>(null);
        head.setNext(tail);
        size = 0;
    }
    public DLList(int size, E element){
        head = new Node<E>(null);
        tail = new Node<E>(null);
        head.setNext(tail);
        this.size = 0;
        for (int i=0;i<size;i++){
            add(element);
        }
    }
    public DLList(E[] elements){
        head = new Node<E>(null);
        tail = new Node<E>(null);
        head.setNext(tail);
        size=0;
        for (int i=0;i<elements.length;i++){
            add(elements[i]);
        }
    }
	public int search(E item){
		Node<E> temp = head.next();
		for (int i=0;i<size;i++){
			if (temp.get().equals(item)){
				return i;
			}
			temp = temp.next();
		}
		return -1;
	}
    public boolean add(E element){
        Node<E> temp = tail.prev();
        temp.setNext(new Node<E>(element));
        temp.next().setNext(tail);
        size++;
        return true;
    }
    public void add(int index, E element){ 
        if (index > 0 && index < size){
            Node<E> temp = getNode(index-1);
            Node<E> newNode = new Node<E>(element);
            newNode.setNext(temp.next()); //might need to add a set prev, but double check this
            temp.setNext(newNode);
            size++;
            return;
        }
        System.out.println("Index: " + index + " out of bounds: " + size);
    }
	public void add(E[] elements){
		for (int i=0;i<elements.length;i++){
			add(elements[i]);
		}
	}
	public void add(DLList<? extends E> elements){
		for (E item : elements){
			add(item);
		}
	}
    public E get(int index){
		Node<E> temp = getNode(index);
        return temp != null ? temp.get() : null;
    }
    public Node<E> getHead(){
        return head;
    }
    public Node<E> getTail(){
        return tail;
    }
    public Node<E> getNode(int index){
		if (index < 0 && index > size*-1+1){
			//can ask for -index values
			index += size;
		}
		if (index <= size*-1){
			return null;
		}
		index%=size; //allow it to go above the actual size of the list by wrapping around back to the beginning
        Node<E> current = head.next();
        if (index < size/2){
            for (int i=0;i<index;i++){
                current = current.next();
            }
        }
        else{
            current = tail.prev();
            for (int i=size-1;i>index;i--){
                current = current.prev();
            }
        }
        return current;
    }
    public String toString(){
        String text = "";
        Node<E> temp = head.next();
        for (int i=0;i<size;i++){
            if (temp.get() == null){
                System.out.println(text + "error found, null value at index: " + i + "");
                return text + "";
            }
            text = text + temp.get().toString()+"";
            temp = temp.next();
            if (i<size-1){
                text += ", ";
            }
        }
        return text +"";
    }
    public E remove(int index){
        Node<E> temp;
        E send = null;
        if (index < size/2){
            temp = head.next();
            for (int i=0;i<index;i++){
                temp = temp.next();
            }
        }
        else{
            temp = tail.prev();
            for (int i=size-1;i>index;i--){
                temp = temp.prev();
            }
        }
        send = temp.get();
        temp.prev().setNext(temp.next());
        size--;
        return send;
    }
    public boolean remove(E item){ //double check this
        Node<E> temp = head;
        for (int i=0;i<size;i++){ //will end, goes through entire list
            if (temp.next().get().equals(item)){
                // the next item is the one to remove
                temp.setNext(temp.next().next());//Skip it
                size--;
                return true;
            }
            temp = temp.next();
        }
        return false;
    }
    public void set(int index, E element){
        Node<E> temp;
        if (index < size/2){
            temp = head.next();
            for (int i=0;i<index;i++){
                temp = temp.next();
            }
        }
        else{
            temp = tail.prev();
            for (int i=size-1;i>index;i--){
                temp = temp.prev();
            }
        }
        temp.setData(element);
    }
    public void swap(Node<E> node1, Node<E> node2){
        E temp = node2.get();
        node2.setData(node1.get());
        node1.setData(temp);
    }
    public void swap(int index1, int index2){
        swap(getNode(index1), getNode(index2));
    }
    public int size(){
        return size;
    }
    public boolean isEmpty(){
        return size < 1;
    }
    public void scramble(){
        Node<E> node1 = head.next();
        while (node1.next() != null){
            swap(node1, getNode((int)(Math.random()*(size-1))));
            node1 = node1.next();
        }
    }
    public void clear(){
        head.setNext(tail);
        size = 0;
    }
    public boolean contains(E[] list){
        // returns true if all the elements in the list are somewhere in this list
        if (size < list.length){
            return false;
        }
        for (int i=0;i<list.length;i++){
            boolean found = false;
            Node<E> current = head.next();
            for (int j=0;j<size;j++){
                if (current.get().equals(list[i])){
                    found = true;
                    break;
                }
            }
            if (!found){
                return false;
            }
        }
        return true;
    }
    public int indexOf(E element){
        Node<E> current = head.next();
        for (int i=0;i<size;i++){
            if (current.get().equals(element)){
                return i;
            }
            current = current.next();
        }
        return -1;
    }
    public int reverseIndexOf(E element){
        Node<E> current = tail.prev();
        for (int i=size-1;i>=0;i--){
            if (current.get().equals(element)){
                return i;
            }
            current = current.prev();
        }
        return -1;
    }
    private DLList<E> mergeSortRecursive(DLList<E> list){
        if (list.size() <= 1){
            return list;
        }
        int mid = list.size() / 2;
        int dif = list.size() % 2;
        DLList<E> leftList = new DLList<E>();
        DLList<E> rightList = new DLList<E>();

        Node<E> current = list.getNode(0);
        for (int i=0;i<mid;i++){
            leftList.add(current.get());
            current = current.next();
        }
        for (int i=mid;i<list.size();i++){
            rightList.add(current.get());
            current = current.next();
        }
        leftList = mergeSortRecursive(leftList);
        rightList = mergeSortRecursive(rightList);

        DLList<E> send = new DLList<E>();
        Node<E> leftNode = leftList.getNode(0);
        Node<E> rightNode = rightList.getNode(0);
        for (int i=0;i<list.size();i++){
            if (leftNode.get() != null && rightNode.get() != null){

                if (leftNode.get().toString().compareTo(rightNode.get().toString()) < 0){
                    send.add(leftNode.get());
                    leftNode = leftNode.next();
                }
                else if (leftNode.get().toString().compareTo(rightNode.get().toString()) >= 0){
                    send.add(rightNode.get());
                    rightNode = rightNode.next();
                }
            }
            else if (leftNode.get() == null){
                while (rightNode.get() != null){
                    send.add(rightNode.get());
                    rightNode = rightNode.next();
                }
            }
            else if (rightNode.get() == null){
                while (leftNode.get() != null){
                    send.add(leftNode.get());
                    leftNode = leftNode.next();
                }
            }

        }
        return send;
    }
    public void mergeSort(){
        head = mergeSortRecursive(this).getHead();
        Node<E> current = head.next();
        while (current.next() != null){
            current = current.next();
        }
        tail = current;
    }
	public DLList<E> reverse(){
		DLList<E> temp = new DLList<E>();
		Node<E> tempNode = tail.prev();
		while (tempNode != null ? tempNode.get() != null : false){
			temp.add(tempNode.get());
			tempNode = tempNode.prev();
		}
		return temp;
	}
	private void removeItr(E item){
		remove(item);
	}
	public Iterator<E> iterator(){
		return new DLListIterator();
	}
	private class DLListIterator implements Iterator<E>{
		private Node<E> cursor = head;
		public DLListIterator(){}
		public E next(){
			cursor = cursor.next();
			return cursor.get();
		}
		public boolean hasNext(){
			return cursor.next() != null ? cursor.next().get() != null : false;
		}
		public void remove(){
			if (cursor == head || cursor.prev() == head){
				return; //cant remove non-existant things
			}
			removeItr(cursor.prev().get());
		}
	}
}