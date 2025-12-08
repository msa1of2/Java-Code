import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.JOptionPane;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Frame;
import helpful.datastructures.*;
import helpful.datastructures.ShiftRegister;
import helpful.connection.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import testMods.*;

import java.util.Scanner;

import java.awt.GraphicsEnvironment;
import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.io.IOException;

import java.util.Random;

import helpful.datastructures.MassiveDLList;
import helpful.Math;
import helpful.crypto.Encryption;
import helpful.crypto.Bitcryption;

public class Runner {
	public Runner(){
		
		//file walking with mods  -  Yes!!!!!!!!
		/*
		try{
			Path startDir = Paths.get("./testMods");

			Object[] temp = Files.walk(startDir).toArray(); // Prints all paths
			
			DLList<String> modsNames = new DLList<String>();
			for (Object item : temp){
				Path path = (Path)item;
				if (path.getFileName().toString().endsWith(".class")){
					String name = path.getFileName().toString().split(".class")[0]; //
					if (!name.equals("Mod")){
						modsNames.add(name);
					}
				} 
			}
			System.out.println("Mod names: " + modsNames);
			DLList<Mod> mods = new DLList<Mod>();
			for (String className : modsNames){
				try {
					ClassLoader classLoader = this.getClass().getClassLoader();
					Class<?> myClass = classLoader.loadClass("testMods."+className); // Replace with the fully qualified class name
					Mod obj = (Mod)myClass.getDeclaredConstructor().newInstance(); // Create an instance of the class
					mods.add(obj);
					// Use the object as needed
				} catch (ClassNotFoundException e) {
					// Handle the case where the class is not found
					System.out.println("Class not found: " + e.getMessage());
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			System.out.println("Mods: " + mods);
			for (Mod mod : mods){
				mod.print();
			}
		}
		catch(IOException e){
			System.out.println("Uh oh");
		}*/
		
		
		
	}
    public static void main(String[]args){
		
		
		
		/*DLList<Integer> list = new DLList<Integer>();
		
		list.add(new Integer[] {0,1,2,3,4,5,6,7});
		
		for (int i=0;i<list.size()*2;i++){
			System.out.println("[" + i + "]:" + list.get(i));
		}*/
		
		
		
		
		
		/*String textToCopy = "Hello, Clipboard!";

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(textToCopy);
        clipboard.setContents(selection, null);

        System.out.println("Text copied to clipboard: " + textToCopy);
		//Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        if (clipboard.getContents(null) != null ? clipboard.getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor) : false) {
            try {
                System.out.println("res: " + (String) clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor));
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
        }*/
		
		/*nodeSizes = new MyArrayList<Integer>(10);
		for (int i=0;i<10;i++){
			nodeSizes.add(i);
		}
		System.out.println(nodeSizes);
		shiftAdd(2, -2);
		System.out.println(nodeSizes);*/
		
		
		/*test = new IndexMDLL<Integer>(10);
		
		test.add(-1);
		for (int i=0;i<200;i++){
			test.add(i);
		}
		System.out.println(test.paramString());
		System.out.println(test);
		System.out.println(test.remove());
		System.out.println(test.paramString());
		System.out.println(test);*/
		
		//Interface things
		/*
		interface A -> MethodsA
		interface B extends A -> MethodsA + MethodsB
		class C implements A, B -> Methods A + MethodsB - No errors thrown
		*/
		
		/*//Massive dLList stuff
		MassiveDLList<Character> test = new MassiveDLList<Character>();
		test.add(new Character[]{'a', 'b', 'c', 'd', 'e'});
		
		System.out.println(test);
		test.remove(1);
		System.out.println(test);
		test.remove(1);
		System.out.println(test);
		*/
		
		//Encryption Stuff
		/*for (int i=0;i<10000;i++){
		boolean res = Encryption.unitTest("decryption", false);
		if (!res) {System.out.println("FAIL");return;}
		}
		System.out.println("Win");*/
		//Encryption.unitTest("all", true);
		long start = System.currentTimeMillis();
		//System.out.println("Lets go in the garden, you'll find something waiting, right ther".length());
		byte[] key = "Lets go in the garden, you'll find something waiting, right there where you left it lying up-side down".getBytes();
		//String test = "How do we know if we're alive? Is there a higher power out there? what is consiousness? Are we alone in the universe? Will there be a tomorrow? Are we going to be okay? ";
		String test = "ABCDEFGHIJKLMNOPQRSTUZWXYZABCDEFGHIJKLMNOPQRSTUZWXYZ";
		Encryption bit = new Bitcryption();
		byte[] out = bit.encrypt(test.getBytes(), key, 16);
		System.out.println("Middle: '" + (new String(out)) + "' - '" + printable(out) + "' - '" + printable(test.getBytes()) + "'");
		out = bit.decrypt(out, key, 2);
		
		System.out.println("Return: '" + (new String(out)) + "'");
		System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");
		//System.out.println(makeWord(new byte[] {6, 1, 0, 1}, 4));
		
		//data strucutre stuff
		
		/*Integer[] adding = new Integer[] {1,2,3,1,2,3,1,2,3,1,2,3,1,2,3,1,2,3};
		ShiftRegister<Integer> test = new ShiftRegister<Integer>(10);
		for (Integer item : adding){
			test.add(item);
			System.out.println("TEST:" + test + " --- " + test.size() + ", " + test.getTop()  + "\n" + test.fullString() + "\n");
		}
		System.out.println("\n" + test + " --- " + test.size() + ", " + test.getTop() + "\n" + test.fullString() + "\n\n");
		int steps = test.size();
		for (int i=0;i<steps;i++){
			System.out.println("Removing: " + test.remove());
			System.out.println("TEST:" + test + " --- " + test.size() + ", " + test.getTop() + "\n" + test.fullString() + "\n");
		}*/
		
		
		//display things
		
		//JOptionPane.showConfrimDialog(null, "This is a test", "This is the title", JOptionPane.YES_NO_CANCEL_OPTION);
		//System.out.println(new String(chars));
        /*Screen screen = new Screen();
        JFrame frame = new JFrame("Screen");
        frame.add(screen);
		
		frame.addMouseListener(screen);
		frame.addMouseWheelListener(screen);
		
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
		
		frame.setLocation(300, 300);*/
		
		/*JWindow test = new JWindow();
		test.add(screen);
		test.pack();
		test.setVisible(true);
		
		test.setLocation(300, 300);*/
    }
	private static long setFlag(long input, int bit, boolean value){
		if (value)
			return (long)(input | 1L<<(63-bit));
		return (long)(input & ~(1L<<63-bit));
	}
	private static boolean getFlag(long input, int bit){
		return ((input >>> (63-bit)) & 1) == 1;
	}
	private static String toBinString(long num){
		char[] send = new char[64];
		for (int i=0;i<64;i++){
			send[i] = (num&1)==1?'1':'0';
			num>>>=1;
		}
		return new String(send);
	}
	private static String printable(byte[] nums){
		String send = "";
		for (byte num : nums){
			send += num + ", ";
		}
		return send;
	}
}
