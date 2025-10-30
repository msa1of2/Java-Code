package helpful.datastructures;
import java.util.Iterator;
import java.io.Serializable;
public class Trie implements Iterable<Boolean>, Serializable{
	private TrieNode root;
	private int wordCount; //for funsies
	private static final boolean excludeSpace = true; //for your use to change in code
	public Trie(String[] inputs){
		this();
		add(inputs);
	}
	public Trie(){
		root = new TrieNode(false);
	}
	public void add(String[] inputs){ //add an array of strings
		for (String item : inputs){
			boolean res = add(item);
		}
	}
	public int getWordCount(){
		return this.wordCount;
	}
	public int size(){
		return this.wordCount;
	}
	public boolean add(String item){
		if (item == null) return false;
		char[] chars = item.toLowerCase().toCharArray(); //makes sure there aren't any weird edge cases with speical characters
		//can remove if wanted from here 
		for (char c : chars) {
			if(!Character.isLetter(c) && (c!=' ')) {
				return false;
			}
		} //to this line, all of it can go if you trust your source
		return add(chars, root);
	}
	private boolean add(char[] remaining, TrieNode current){ //private recursive method
		if (remaining == null) return false; //remaining is wrong - one type of exit condition
		if (current == null) return false; //the current node is wrong - null protection
		
		if (remaining.length == 0){ //this is the last item to add 
			current.setWord(true); //this sequence is a word
			wordCount++;
			return true; //no more letters to add
		}
		
		if (remaining[0] == ' ' && excludeSpace){
			char[] next = new char[remaining.length-1]; //set up the array for the next recursion step
			System.arraycopy(remaining, 1, next, 0, next.length); //copys the remaining characters bar the first item into the array going deeper into recursion
			return add(next, current); //recursion
		}
		
		if (!current.hasNext(remaining[0])){ //first time adding this char to this node
			current.add(remaining[0]); //add the trie node
		}
		
		current = current.next(remaining[0]); //update where we are supposed to be
		
		char[] next = new char[remaining.length-1]; //set up the array for the next recursion step
		System.arraycopy(remaining, 1, next, 0, next.length); //copys the remaining characters bar the first item into the array going deeper into recursion
		return add(next, current); //recursion
	}
	private TrieNode get(String item){
		return get(item.toLowerCase().toCharArray(), root);
	}
	private TrieNode get(char[] remaining, TrieNode current){
		if (remaining == null) return null; //remaining is wrong - one type of exit condition
		if (current == null) return null; //the current node is wrong - null protection
		if (remaining.length == 1){ //this is the last item
			return current.next(remaining[0]); //send the connection with that last item back
		}
		//go to the next node
		current = current.next(remaining[0]); //update where we are supposed to be
		char[] next = new char[remaining.length-1]; //set up the array for the next recursion step
		System.arraycopy(remaining, 1, next, 0, next.length); //copys the remaining characters bar the first item into the array going deeper into recursion
		return get(next, current); //recursion
	}
	public boolean wordExists(String item){
		TrieNode temp = get(item);
		if (temp == null) return false; //doesn't exist
		return temp.isWord();
	}
	public boolean isWord(String item){
		return wordExists(item);
	}
	public boolean branchesExist(String item){
		if (item.length() <= 0) return false;
		TrieNode temp = get(item);
		if (temp == null) return false; //doesn't exist
		return temp.hasAConnection();
	}
	public boolean[] existance(String item){
		TrieNode temp = get(item);
		if (temp == null) return new boolean[2]; //auto fills with two falses
		return new boolean[]{temp.isWord(), temp.hasAConnection()};
	}
	public class TrieNode implements Serializable{ //the nodes traversed by the Trie
		private TrieNode[] connections; //connections only go one way
		private boolean isWord;
		
		public TrieNode(boolean isWord){
			this.isWord = isWord;
			//the entire alphabet and space if included
			connections = new TrieNode[26 + (excludeSpace ? 0:1)]; // ? is boolean operator, returns the following "ifTrue : isFalse"
		}
		public void add(char connection){
			if (connection == ' ' && !excludeSpace){
				connections[26] = new TrieNode(false);
				return;
			}
			//can't connect to existsing TrieNode, will always create a new one
			if ((int)(connection)-97 >= 27) return;
			connections[(int)(connection)-97] = new TrieNode(false); //char - 'a'
		}
		public void setWord(boolean isWord){
			this.isWord = isWord;
		}
		public boolean hasNext(char item){
			if (item == ' ') { 
				if (!excludeSpace)
					return connections[26]!=null;
				else return false;
			}
			if ((int)(item)-97 >= 27) return false;
			return connections[(int)(item)-97] != null;
		}
		public TrieNode next(char item){ //the TrieNode connected by this character
			if (item == ' ' && !excludeSpace) return connections[26];
			if ((int)(item)-97 >= 27) return null;
			return connections[(int)(item)-97];
		}
		public boolean isWord(){
			return this.isWord;
		}
		public String toString(){
			String send = "";
			for (int i=0;i<26;i++){
				send+=((char)(i+97) + ":" + (connections[i]!=null) + ", ");
			}
			if (!excludeSpace) send += " :"+ (connections[26]!=null);
			return send;
		}
		public boolean hasAConnection(){
			for (TrieNode node : connections){
				if (node != null) return true; //found a connection
			}
			return false; //all connections are null;
		}
	}
	public Iterator<Boolean> iterator(){
		return new TrieIterator();
	}
	public String toString(){
		return recursiveString("", root);
	}
	private String recursiveString(String line, TrieNode current){
		if (current == null) return "";
		String send = "";
		if (current.isWord()) send += line + "\n";
		for (int i=0;i<(26 + (excludeSpace ? 0:1));i++){
			char item = (char)(i+97);
			if (i == 26) item = ' '; //space
			String next = line + item;
			send += recursiveString(next, current.next(item));
		}
		return send;
	}
	public class TrieIterator implements Iterator<Boolean>, Serializable{
		private TrieNode cursor = root;
		public TrieIterator(){}
		public Boolean next(){
			cursor = cursor.next('a');
			return cursor != null ? cursor.isWord() : false;
		}
		public boolean hasNext(){
			return cursor != null ? cursor.hasNext('a') : false;
		}
		public boolean next(char item){
			cursor = cursor.next(item);
			return cursor != null ? (cursor.hasAConnection() || cursor.isWord()) : false;
		}
		public boolean hasNext(char item){
			return cursor != null ? cursor.hasNext(item) : false;
		}
		public boolean isWord(){
			return cursor != null ? cursor.isWord() : false;
		}
		public TrieNode get(){
			return cursor;
		}
	}
}