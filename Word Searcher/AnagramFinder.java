import java.util.Scanner;
import java.io.FileReader;
import java.io.FileNotFoundException;

import java.time.Clock;

import helpful.datastructures.*;
import java.util.HashSet;

public class AnagramFinder{
	public static void main(String[] args){
		if (args.length < 1){
			return; //need a collection of letters to use
		}
		String send = args[0].toLowerCase();
		for (int i=1;i<args.length;i++){
			send += args[i].toLowerCase();
		}
		int spaceCount=0;
		for (char item : send.toCharArray()) if (item == ' ') spaceCount++; //adds 1 for every space in the starting String
		char[] sendList = new char[send.length()-spaceCount];
		spaceCount=0;
		for (int i=0;i<sendList.length;i++){
			if (send.charAt(i+spaceCount) == ' '){
				spaceCount ++;
				i--;
				continue;
			}
			sendList[i] = send.charAt(i+spaceCount);
		}
		new AnagramFinder(new String(sendList));
	}
	Trie trie;
	char[] chars;
	int[] charIndex;
	int safeTo = 0;
	Heap<Integer> minSorter;
	HashSet<Integer> numbers, valWords;
	DLList<String> words;
	long start, end, loops;
	public AnagramFinder(String anagram){
		System.out.println("Finding Anagrams for: " + anagram);
		trie = WordSearcher.read();
		chars = anagram.toCharArray();
		charIndex = new int[chars.length];
		minSorter = new Heap<Integer>();
		numbers = new HashSet<Integer>();
		valWords = new HashSet<Integer>();
		words = new DLList<String>();
		
		//fill charsIndex with dummy numbers for now
		for (int i=0;i<charIndex.length;i++){
			charIndex[i]=i;
		}
		loops=0L;
		start = Clock.systemUTC().millis();
		for (int index = charIndex.length-1;index >= 0;index--){
			boolean working = true;
			while (working){
				loops++;
				//System.out.print("Index: " + index + " --- " );
				working = increment(index);
				if (working) index=charIndex.length-1;
				else{
					minSorter.clear();
					for (int i =index;i<charIndex.length;i++){
						minSorter.add(charIndex[i]);
					}
					for (int i =index;i<charIndex.length;i++){
						charIndex[i] = minSorter.poll();
					}
					minSorter.clear();
				}
				//printIndexs();
				
				boolean end = false;
				for (int i=0;i<index;i++){
					if (!trie.branchesExist(makeWord(charIndex, index))){
						increment(index);
						end = true;
						break;
					}
				}
				
				if (end) continue;
				String word = makeWord(charIndex, charIndex.length);
				if (!numbers.add(debugHash(charIndex)) || valWords.contains(charHash(word.toCharArray()))){
					//we've seen this before, don't do trie tests on it
					continue;
				}
				
				if (trie.wordExists(word)){
					valWords.add(charHash(word.toCharArray()));
					words.add(word);
				}
				//System.out.println(word + "\t " + debugHash(charIndex) + "\t--- " + index + "\t--- " + working);
			}
		}
		end = Clock.systemUTC().millis();
		
		System.out.println("Numbers:  --- " + numbers.size());
		System.out.println(words);
		System.out.println("Time taken: " + (end-start) + "ms");
		System.out.println("Loops taken: " + loops);
	}
	public String makeWord(int[] numList, int endIndex){
		if (endIndex > numList.length) return "null";
		char[] temp = new char[endIndex];
		for (int i=0;i<endIndex;i++){
			temp[i]=chars[charIndex[i]];
		}
		return new String(temp);
	}
	public int incrementIndex(int index){
		if (index < 0) return 0;
		boolean res = increment(index);
		if (!res) return incrementIndex(index-1);
		return index;
	}
	public String leftPad(String toPad, String character, int size){
		if (toPad.length() >=size) return toPad;
		return "";
	}
	public boolean increment(int index){
		if (index < 0 || index >= charIndex.length-1) return false; //Out of bounds errors and we won't be able to increment the last item in the list
		int min=charIndex.length, loc=index;
		for (int i=index;i<charIndex.length;i++){
			//System.out.println("Increment debug: " + charIndex[i] + ", " + min + ", " + charIndex[index]);
			if (charIndex[i] < min && charIndex[i] > charIndex[index]){
				min = charIndex[i];
				loc = i;
			}
		}
		if (min < charIndex[index] || min == charIndex.length) return false; //didn't find the next number
		//swap the two numbers
		charIndex[loc] = charIndex[index];
		charIndex[index] = min;
		return true;
	}
	public static int debugHash(int[] values){
		int send = 0;
		for (int i=0;i<values.length;i++){
			send += Math.pow(values.length, i) * values[i];
		}
		return send;
	}
	public static int charHash(char[] letters){
		int send = 0;
		for (int i=0;i<letters.length;i++){
			send += Math.pow(letters.length, i) * ((int)Character.toLowerCase(letters[i])-97);
		}
		return send;
	}
	public void printIndexs(){
		String send = "charIndex: [";
		for (int num : charIndex){
			send += num + ", ";
		}
		System.out.print(send+"] --- ");
	}
}