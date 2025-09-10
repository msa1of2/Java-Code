//file saving librarys
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
//just for useage
import java.util.Scanner;
//serializable is required for saving
import java.io.Serializable;

public class Runner{
	public static void main(String args[]){
		new Runner();
	}
	public Runner(){
		String ans = "";
		Saveable dataObject = new Saveable("Nothing in object yet");
		Scanner scan = new Scanner(System.in);
		while (!ans.equals("4") && !ans.equals("quit")){
			System.out.println("Choose an option to do:\n"
								+"1. Read the file and display the data object\n"
								+"2. Edit the current data object\n"
								+"3. Save the current dataobject to the file\n"
								+"4. Quit the program\n"
								+"(Enter number choice or first word)\n");
			ans = scan.nextLine().toLowerCase();
			if (ans.equals("1") || ans.equals("read")){
				dataObject = read();
				System.out.println("Data object: \n" + dataObject.get()); 
			}
			if (ans.equals("2") || ans.equals("edit")){
				System.out.println("What will you replace the data object with?");
				
				String newMessage = scan.nextLine();
				
				System.out.print("Replacing \"" + dataObject.get() + "\"");
				
				dataObject.set(newMessage);
				
				System.out.println(" to \"" + dataObject.get() + "\"");
			}
			if (ans.equals("3") || ans.equals("save")){
				boolean res = save(dataObject);
				if (res)
					System.out.println("Data object successfully saved");
				else
					System.out.println("Data object is NOT SAVED");
			}
			
			System.out.println("\n\n");
		}
	}
	
	public boolean save(Serializable obj){ //saves the object to the file
		 try {
            FileOutputStream fos = new FileOutputStream("saveFile.dat");
            ObjectOutputStream out = new ObjectOutputStream(fos);

            out.writeObject(obj);

            fos.close();
            out.close();
			return true;
        } catch (Exception ex) {
            ex.printStackTrace();
			return false;
        }
	}
	public Saveable read(){ //reads the object from the file
		Saveable send = null;
		try {
            FileInputStream fis = new FileInputStream("saveFile.dat");
            ObjectInputStream in = new ObjectInputStream(fis);

            send = (Saveable) in.readObject();

            fis.close(); 
            in.close();
        } catch (FileNotFoundException ex) {
            System.out.println("File not found");
        } catch (Exception ex) {
            //No information in the file, reset to default (not needed in any program, but good for edge cases)
            send = new Saveable("Saved data");
        }
        return send;
	}
	//If you plan to use nested clasees, all nests and parents must be serializable
	/*public class Saveable implements Serializable{ //whatever object to be saved must be Serializable
		private String info;
		public Saveable(String data){
			this.info = data;
		}
		public String get(){
			return this.info;
		}
		public void set(String data){
			this.info = data;
		}
	}*/ //if saveable is created like this, it'll throw error galore
}
//you could put saveable here, but that is gernaly frowned upon
