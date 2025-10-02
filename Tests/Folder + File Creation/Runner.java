import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Runner{
	public static void main(String[] args){
		System.out.println("Creating new Files / Folders");
		
		File fileCreation = new File("Created File.txt");
		try{
			fileCreation.createNewFile();
			
			FileWriter created = new FileWriter(fileCreation);
			created.write("I am a beautiful butterfly!");
			created.close();
			
		}
		catch(Exception e){e.printStackTrace();}
	}
}