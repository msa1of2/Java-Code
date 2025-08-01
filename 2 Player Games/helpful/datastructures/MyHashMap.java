package helpful.datastructures;
import java.util.Iterator;

public class MyHashMap<K, V> implements Iterable<V>{
    private Object[] hashArray;
    private int size;
    private MyHashSet<K> keySet;
    public MyHashMap(){
        hashArray = new Object[65536];
        size = 0;
        keySet = new MyHashSet<K>();
    }
    public void put(K key, V value){
        if (keySet.contains(key)){
            hashArray[key.hashCode()] = value;
        }
        else{
            hashArray[key.hashCode()] = value;
            keySet.add(key);
        }
        size++;
    }
    @SuppressWarnings("unchecked")
    public V get(Object key){
        return (V)hashArray[key.hashCode()];
    }
    @SuppressWarnings("unchecked")
    public V remove(Object key){
        K keys = (K)key;
		System.out.println(keySet.contains(keys) + ", " + keys.hashCode() + ", " + hashArray.length + ", " + keys);
        if (keySet.contains(keys) && keys.hashCode() >0 && keys.hashCode() <= hashArray.length){
            V val = (V)hashArray[keys.hashCode()];
            hashArray[keys.hashCode()] = null;
            keySet.remove(keys);
            size--;
            return val;
        }
        return null;
    }
    public int size(){
        return size;
    }
    public MyHashSet<K> keySet(){
        return keySet;
    }
    public boolean containsKey(K key){
        return keySet.contains(key);
    }
    public String toString(){
        String send = "{";
        Node<K> keys = keySet.toDLList().getHead().next();
        for (int i = 0;i < keySet.toDLList().size();i++){
            send += keys.get().toString() + "=" + hashArray[keys.get().hashCode()];
            keys = keys.next();
            if (i != keySet.toDLList().size()-1){
                send+=", ";
            }
        }
        return send + "}";
    }
    public void clear(){
        hashArray = new Object[9999];
        size = 0;
        keySet = new MyHashSet<K>();
    }
    public K getKey(K key){
        if (keySet.contains(key)){
            return keySet.get(key);
        }
        return null;
    }
	private boolean removeItr(Object key){
		return remove(key) != null;
	}
	public Iterator<V> iterator(){
		return new MyHashMapIterator();
	}
	private class MyHashMapIterator implements Iterator<V>{
		private Node<K> cursor = keySet.toDLList().getHead();
		public MyHashMapIterator(){}
		public V next(){
			cursor = cursor.next();
			return get(cursor.get());
		}
		public boolean hasNext(){
			return cursor.next() == null ? false : cursor.next().get() != null;
		}
		public void remove(){
			if (cursor == keySet.toDLList().getHead() || cursor.prev() == keySet.toDLList().getHead()){
				return; //cant remove non-existant things
			}
			System.out.println(removeItr(cursor.prev()));
		}
	}
}