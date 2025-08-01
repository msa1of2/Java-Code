package helpful.datastructures;
public class Graph<E>{
    private MyHashMap<E, MyHashSet<Pair<E, Integer>>> map;

    public Graph() {
        this.map = new MyHashMap<E, MyHashSet<Pair<E, Integer>>>();
    }
    public void add(E data){
        if (!map.keySet().contains(data)){
            map.put(data, new MyHashSet<Pair<E, Integer>>());
        }
    }
    public void add(E[] data){
        for (int i=0;i<data.length;i++){
            add(data[i]);
        }
    }
    public MyHashSet<E> get(E data){
		MyHashSet<E> send = new MyHashSet<E>();
		for (Pair<E, Integer> item : map.get(data)){
			send.add(item.getKey());
		}
        return send;
    }
    public int getWeight(E node1, E node2){
        if (map.keySet().contains(node1) && map.keySet().contains(node2)){
            MyHashSet<Pair<E, Integer>> set = map.get(node1);
            for (Pair<E, Integer> item : set){
                if (item.getKey().equals(node2)){
                    return item.getValue();
                }
            }
            return -2;
        }
        return -1;
    }
    public E getKey(E data){
        return map.getKey(data);
    }
    public void addEdge(E data1, E data2, int value){
        if (!(map.containsKey(data1) && map.containsKey(data2))){
            return;
        }
        map.get(data1).add(new Pair<E, Integer>(map.keySet().get(data2), value));
        map.get(data2).add(new Pair<E, Integer>(map.keySet().get(data1), value));
    }
    public void remove(E data){
        if (!map.containsKey(data)){
            return;
        } 
        for (Pair<E, Integer> nodePair : map.get(data)){
			E node = nodePair.getKey();
            map.get(node).remove(new Pair<E, Integer> (data, 0));
        }
        map.remove(data);
    }
    public String toString(){
        String send = "";
        for (E key : map.keySet()){
            send+=(key + " -> ");
            for (Pair<E, Integer> valuePair : map.get(key)){
				E value = valuePair.getKey();
                send+=(valuePair.toString() + " ");
            }
            send+="\n";
        }
        return send;
    }
    public E breadthSearch(E startNode, E data){
        int visited = 0;
        MyHashSet<E> visitedNodes = new MyHashSet<E>();
        MyHashSet<E> toVisitNodes = new MyHashSet<E>();
        toVisitNodes.add(startNode);
        while (visited < map.size()){
            MyHashSet<E> tempList = new MyHashSet<E>();
            for (E node : toVisitNodes){
                if (node.equals(data)){
                    return node;
                }
                visitedNodes.add(node);
                visited++;
				
				//add only unvisited nodes
                for (Pair<E, Integer> value : map.get(new Pair<E, Integer> (node, 0))){
                    if (!visitedNodes.contains(value.getKey()) && !tempList.contains(value.getKey())){
                        tempList.add(value.getKey());
                    }
                }
            }
            toVisitNodes = tempList;
        }
        return null;
    }
    public E depthSearchRecursion(E node, E data){
        MyHashSet<Pair<E, Integer>> visited = new MyHashSet<Pair<E, Integer>>();
        return recursionDepthRunner(node, data, visited);
    }
    public E recursionDepthRunner(E node, E data, MyHashSet<Pair<E, Integer>> visited){
        if (visited.contains(new Pair<E, Integer> (node, 0))){ //already visited the node before
            return null;
        }
        visited.add(new Pair<E, Integer> (node, 0));
        if (data.equals(node)){ //data is in the node, found it
            return node;
        }
        for (Pair<E, Integer> valuePair : map.get(node)){
			E value = valuePair.getKey();
            E temp = recursionDepthRunner(value, data, visited);
            if (temp != null){ //make sure the return data is valid
                return temp;
            }
        }
        return null;
    }
    public MyHashSet<Pair<E, Integer>> getNeighbors(E element) {
        return map.get(element);
    }
    public void clear(){
        map.clear();
    }
    public MyHashSet<E> keySet(){
        return map.keySet();
    }
    public E getKey(int num){
        return map.keySet().get(num);
    }
    public int shortestPath(E startNode, E endNode){
        MyHashMap<E, Pair<E, Integer>> lengthToStart = new MyHashMap<E, Pair<E, Integer>>();
        
        MyHashSet<E> toVisitNodes = new MyHashSet<E>();
        toVisitNodes.add(startNode);
        lengthToStart.put(startNode, new Pair<E, Integer>(startNode, 0));
        while (toVisitNodes.size() > 0){
            //get the first node on the stack-ish thing
            E node = toVisitNodes.get(0);
            //dont need to visit it again
            toVisitNodes.remove(node);
            //go through all connections of the current node
            for (Pair<E, Integer> value : map.get(node)){
                //if we have seen the connection before, check if we shoould update it
                if (lengthToStart.containsKey(value.getKey())){
                    //if the new length is shorter, update it
                    if (value.getValue() + lengthToStart.get(node).getValue() < lengthToStart.get(value.getKey()).getValue()){
                        //this node has a shorter path to the start node than what is stored
                        lengthToStart.put(value.getKey(), new Pair<E, Integer>(node, value.getValue() +lengthToStart.get(node).getValue()));
                        //add all of the nodes connected, there might be a shorter path than what was discovered
                        for (Pair<E, Integer> tempNode : map.get(node)){
                            //make certain we'll visit it again
                            toVisitNodes.add(tempNode.getKey());
                        }
                    }
                }
                else{
                    //this node has not been visited before, put in the current length
                    lengthToStart.put(value.getKey(), new Pair<E, Integer>(node, value.getValue() +lengthToStart.get(node).getValue()));
                    //add all of the nodes connected after the shortest path was updated
                    for (Pair<E, Integer> tempNode : map.get(node)){
                        toVisitNodes.add(tempNode.getKey());
                    }
                }
            }
        }
        //return the length 
        return lengthToStart.get(endNode).getValue();
    }
	private class Pair<E, V>{
		private E key;
		private V value;
		public Pair(E key, V value){
			this.key = key;
			this.value = value;
		}
		public E getKey(){
			return key;
		}
		public V getValue(){
			return value;
		}
		public String toString(){
			return key + ":" + value;
		}
		public int hashCode(){
			return key.hashCode();
		}
		@SuppressWarnings("unchecked")
		public boolean equals(Object o){
			if (o instanceof Pair){
				Pair<E,V> p = (Pair<E,V>)o;
				return p.getKey().equals(key);
			}
			return false;
		}
	}
}