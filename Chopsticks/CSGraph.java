import helpful.datastructures.DLList;

public class CSGraph{
	Node head;
	
	public CSGraph(short value){
		head = new Node(value, 0);
		
	}
	
	public Node add(Node oldNode, short toAdd, int layer){
		Node send = new Node(toAdd, layer);
		oldNode.add(send);
		return send;
	}
	public Node getHead(){
		return head;
	}
	
	public class Node{
		private Node parent;
		private DLList<Node> nextNodes;
		private DLList<Node> parentNodes;
		private short val;
		private int layer;
		private int scoreValue;
		private Node bestChild;
		public Node(short value, int layer){
			this.val = value;
			this.nextNodes = new DLList<>();
			this.parentNodes = new DLList<>();
			this.parent = null;
			this.layer = layer;
			this.scoreValue = 0;
		}
		public Node(int value, int layer){
			this((short)value, layer);
		}
		public void setChild(Node child){
			this.bestChild = child;
		}
		public Node getBestChild(){
			return this.bestChild;
		}
		public void setScore(int val){
			this.scoreValue = val;
		}
		public int getScore(){
			return scoreValue;
		}
		public void add(Node next){
			nextNodes.add(next);
			next.addParent(this);
		}
		public DLList<Node> next(){
			return nextNodes;
		}
		public void addParent(Node posParent){ //as a suggestion, only replaces newly created node's parents
			parentNodes.add(posParent);
			if (this.parent != null) return;
			this.parent = posParent;
		}
		public void setParent(Node parent){
			parentNodes.clear();
			parentNodes.add(parent);
			this.parent = parent;
		}
		public Node getParent(){
			return this.parent;
		}
		public DLList<Node> getParents(){
			return this.parentNodes;
		}
		public short get(){
			return val;
		}
		public int getLayer(){
			return this.layer;
		}
	}
}
