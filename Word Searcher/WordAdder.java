import helpful.datastructures.*;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.File;
import java.util.Scanner;
import java.io.FileReader;
import java.io.FileNotFoundException;

public class WordAdder{
	Trie base, toAdd;
	
	public static void main(String[] args){
		new WordAdder();
	}
	
	public WordAdder(){
		base = WordSearcher.read();
		toAdd = read("toAdd.txt");
		base.add(toAdd.toString().split("\n"));
		String[] gridLine = WordSearcher.readGrid();
		try (FileWriter writer = new FileWriter("words.txt")){ //will always exit with closing the file
			writer.write(String.join("\n", gridLine) +"\n\n" + base.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try (FileWriter writer = new FileWriter("toAdd.txt")){
			writer.write("Done");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Trie read(String fileName){
		Trie send = new Trie();
        try {
			Scanner scan = new Scanner(new FileReader(fileName));
			//reads one line at a time
			while (scan.hasNextLine()){
				send.add(scan.nextLine().toLowerCase());
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        return send;
	}
	
}
