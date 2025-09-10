//read/write to file
import java.util.Scanner;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;


class Runner{
	public static void main(String[] args){
		read();
	}
	public void write(String lines){
		try (FileWriter writer = new FileWriter("TradingTestValues.txt")){ //will always exit with closing the file
            writer.write(lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	public static void read(){
		try {
            Scanner scan = new Scanner(new FileReader("TradingTestValues.txt"));
            //reads one line at a time
            while (scan.hasNextLine()){
                String[] temp = scan.nextLine().split(":");
				Trading tempTrade = new Trading(temp);
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
	}
}