import java.util.Scanner;
import java.io.FileReader;
import java.io.FileNotFoundException;

import java.time.Clock;

import helpful.datastructures.*;
import java.util.HashSet;

public class SpellingBee{
	public static void main(String[] args){
		if (args.length < 2){
			System.out.println("Need 6 letters of the normal letters, then a space, then the special letter, space, word size (try 10)");
			return;
		}
		if (args[0].length() < 6){
			System.out.println("Need 6 letters in the first section");
			return;
		}
		char[] normalLetters = args[0].toLowerCase().toCharArray();
		char usedLetter = args[1].toLowerCase().charAt(0);
		int size = -1;
		if (args.length >= 3) size = Integer.parseInt(args[2]);
		
		if (size < 4 && size != -1) return;
		
		new SpellingBee(normalLetters, usedLetter, size);
	}
	Trie trie;
	byte[] wheel;
	char[] charIndex;
	HashSet<String> words;
	long start, end, loops;
	int bigIncrement = 0;
	public SpellingBee(char[] norm, char special, int size){
		boolean loadAll = (size == -1); //simple boolean, records if its a set size or all the sizes
		//load all the characters into an easy to access list
		charIndex = new char[7];
		System.arraycopy(norm, 0, charIndex, 0, 6);
		charIndex[6] = special;
		
		//Get all the words in the data structure and what we'll send back to the user
		trie = WordSearcher.read();
		words = new HashSet<>();
		
		System.out.println("Searching for words with: " + new String(charIndex));
		
		start = Clock.systemUTC().millis(); //Start the clock
		
		
		//Create the wheel which holds the indexes of all the characters of the word
		wheel = new byte[size+1]; //Antinationalization (19) is longest 7 letter word found
		for (int i=0;i<wheel.length;i++){
			wheel[i] = -1; //no letter in these positions, marked with the negative value
		}
		for (int i=0;i<4;i++){wheel[i]=0;} //sets the first 4 numbers to 0, they have to be usable
		
		String attempt;
		while (wheel[size] == -1){ //will get changed as soon as it reaches the end of the list
			
			//if (wheel[0] == 6 && makeWord(wheel).length() <= 4) System.out.println(trueWheel() + ", " + getWheel());
			loops++;
			//boolean end = false;
			
			/*for (int i=0;i<wheel.length;i++){ //go through the entire possible word
				if (wheel[i] == -1) break; //no more letters to add
				//System.out.print(wheel[i+1] + "--" + i + ", ");
				if (!trie.branchesExist(makeWord(wheel, i))){ //checks if the branches exist 
					//System.out.println("Failed: " + getWheel() + ", [" + i + "], " + makeWord(wheel, i) + " <" + loops + ">");
					increment(i);
					end = true; //small optimization
					break;
				}
			}*/
			
			increment(0);
			checkForSpecial();
			
			attempt = makeWord(wheel);
			
			if (!trie.wordExists(attempt)) continue;
			words.add(makeWord(wheel));
			
			//System.out.println("New word: " + attempt + ", " + loops + ", " + trueWheel());
			
		}
		
		end = Clock.systemUTC().millis();
		
		
		System.out.println(words);
		System.out.println("Time taken: " + (end-start) + "ms");
		System.out.println("Loops taken: " + loops);
		
	}
	public void increment(int index){
		if (index < 0 || index >= wheel.length) return;
		if (index > bigIncrement){
			bigIncrement = index;
			System.out.println(bigIncrement + ", next up, Time Taken so far: " + (Clock.systemUTC().millis()-start) + "ms");
		}
		wheel[index]++;
		if (wheel[index] >= 7){
			wheel[index] = 0;
			increment(index+1);
		}
	}
	public String makeWord(byte[] numList, int endIndex){
		if (endIndex < 0 || endIndex > numList.length) return "null";
		char[] temp = new char[endIndex];
		for (int i=0;i<endIndex;i++){
			temp[i]=charIndex[numList[i]];
		}
		return new String(temp);
	}
	public String makeWord(byte[] numList){
		int i=0;
		for (i=0;i<numList.length;i++){
			if (numList[i] == -1){
				return makeWord(numList, i);
			}
		}
		return makeWord(numList, 19);
	}
	public void checkForSpecial(){
		while(true){
			for (int i=0;i<wheel.length;i++){
				if (wheel[i] == 6){
					return; //found the special, no more checking needed
				}
			}
			increment(0);
		}
	}
	public String getWheel(){
		char[] send = new char[wheel.length];
		for (int i=0;i<wheel.length;i++){
			if (wheel[wheel.length-i-1] != -1) send[i] = (char)(wheel[wheel.length-i-1]+48);
			else send[i] = ' ';
		}
		return new String(send);
	}
	public String trueWheel(){
		char[] send = new char[wheel.length];
		for (int i=0;i<wheel.length;i++){
			if (wheel[i] != -1) send[i] = (char)(wheel[i]+48);
			else send[i] = ' ';
		}
		return new String(send);
	}
	
	public void testEquals(){
		byte[] test = new byte[] {6, 0, 6, 2};
		for (int i=0;i<test.length;i++){
			if (test[i] != wheel[i])
				return;
		}
		System.out.println("Hit");
	}
}