import helpful.datastructures.*;
import helpful.*;

import java.util.Scanner;
import java.io.FileReader;
import java.io.FileNotFoundException;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
//events
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class WordSearcher extends JPanel implements MouseListener, MouseWheelListener, ActionListener{
	private Data[] found;
	private JTextField search;
	private Button searchButton;
	private ScrollBar wordHeight;
	private Trie words;
	private char[][] grid;
	private TextFitter charSize, wordDisplay[];
	private Data focus;
	private int focusIndex;
	public static void main(String[] args){
		/*TODO:
		 * Display the letters full uppercase
		 * When adding words, print the data associated with them
		 */
		
		if (args.length != 0){ //add to the database of words, just to make sure the program has what is being asked
			Trie send = new Trie();
			String gridLine = "";
			boolean addToGrid = true;
			send.add(args);
			try { //if there are extra words to add
				Scanner scan = new Scanner(new FileReader("words.txt"));
				//reads the words
				while (scan.hasNextLine()){
					String temp = scan.nextLine();
					
					if (temp.equals("")){addToGrid = false; gridLine+="\n";}
					if (!temp.toUpperCase().equals(temp) && !addToGrid) send.add(temp);
					if (addToGrid) gridLine+=temp + "\n";
				}
				try (FileWriter writer = new FileWriter("words.txt")){ //will always exit with closing the file
					writer.write(gridLine + send.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		WordSearcher screen = new WordSearcher();
        JFrame frame = new JFrame("Screen");
        frame.add(screen);
		
		frame.addMouseListener(screen);
		frame.addMouseWheelListener(screen);
		
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
		frame.setVisible(true);
	}
	public static void addTo(Trie send, File file){
		try { //if there are extra words to add
			Scanner scan = new Scanner(new FileReader(file));
			//reads the words
			String temp = "";
			while (scan.hasNextLine()){
				temp = scan.nextLine();
				if (temp.length() > 2) send.add(temp);
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public WordSearcher(){
		setLayout(null);
		String[] lines = readGrid();
		words = read();
		
		grid = new char[lines.length][lines[0].length()];
		for (int i=0;i<lines.length;i++){
			grid[i] = lines[i].toCharArray();
		}
		
		found = searchForWords(words, grid);
		
		search = new JTextField("Search:");
		search.setBounds(600, 0, 170, 50);
		add(search);
		search.addActionListener(this);
		
		if (found.length > 0) focus = found[0];
		
		charSize = new TextFitter(600/(grid.length*8/5), 600, "A");
		wordDisplay = new TextFitter[11];
		for (int i=0;i<wordDisplay.length;i++){
			wordDisplay[i] = new TextFitter(170, 50, "C");
		}
		wordHeight = new ScrollBar(found.length);
		wordHeight.setBackgroundColor(Color.black);
	}
	public static String[] readGrid(){
		String[] send = null;
		DLList<String> lineBuffer = new DLList<String>();
        try {
			Scanner scan = new Scanner(new FileReader("words.txt"));
			//reads one line at a time
			while (scan.hasNextLine()){
				String temp = scan.nextLine();
				if (temp.equals("")) break;
				lineBuffer.add(temp);
				//System.out.println("adding: " + temp);
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		send = new String[lineBuffer.size()];
		int i=0;
		for (String item : lineBuffer){
			send[i] = item;
			i++;
		}
        return send;
	}
	public static Trie read(){
		return read("words.txt");
    }
	public static Trie read(String fileName){
		Trie send = new Trie();
		boolean addThings = false;
		int line= 0, count=0;
        try {
			Scanner scan = new Scanner(new FileReader(fileName));
			//reads one line at a time
			while (scan.hasNextLine()){
				String temp = scan.nextLine();
				if (!temp.toUpperCase().equals(temp) && addThings){
					send.add(temp);
				}
				if (temp.equals("")) addThings = true;
				line++;
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        return send;
	}
	public Data[] searchForWords(Trie words, char[][] grid){
		DLList<Data> dataBuff = new DLList<Data>();
		for (int x=0;x<grid.length;x++){
			for (int y=0;y<grid[0].length;y++){
				//going through the entire grid positions
				Trie.TrieIterator it = words.new TrieIterator();
				char[] word = new char[Math.max(grid.length, grid[0].length)];
				for (int direction=0;direction<7;direction++){
					//going through every direction from this position
					it = words.new TrieIterator();
					it.next(grid[x][y]);
					word[0] = grid[x][y];
					int[] mults = getMults(direction);
					if (!inBounds(grid, x+mults[1], y+mults[0])) continue;
					char nextChar = grid[x+mults[1]]
					[y+mults[0]];
					for(int step =2;it.hasNext(nextChar)&&step<100;step++){
						//in each direction
						if (it.next(nextChar)){
							word[step-1] = nextChar;
							//System.out.println(word);
						}
						if (it.isWord()){
							char[] fullWord = new char[step];
							System.arraycopy(word, 0, fullWord, 0, step);
							dataBuff.add(new Data(y, x, direction, step-1, true, new String(fullWord)));
						}
						if (!inBounds(grid, x+mults[1]*step, y+mults[0]*step)) break;
						nextChar = grid[x+mults[1]*step][y+mults[0]*step];
					}
				}
			}
		}
		Data[] send = new Data[dataBuff.size()];
		dataBuff.mergeSort();
		int i=0;
		for (Data item : dataBuff){
			send[i] = item;
			i++;
		}
		return send;
	}
	public static boolean inBounds(char[][] grid, int x, int y){
		return x>=0 && x<grid.length && y>=0 && y <grid[0].length;
	}
	public static int[] getMults(int direction){
		int[] send = new int[]{0,0};
		if (direction >7 || direction < 0) return send;
		if (direction > 4) send[0] = -1;
		else if (direction<4 && direction>0) send[0] = 1;
		if (direction == 7 || direction <= 1) send[1] = -1;
		else if (direction >= 3 && direction <=5) send[1] = 1;
		return send;
	}
	public static String toEnglishDir(int direction){
		int[] mults = getMults(direction);
		if (mults[0] < 0){
			if (mults[1] < 0) return  "Top Left    ";
			if (mults[1] == 0) return "Left        ";
			if (mults[1] > 0) return  "Bottom Left ";
		}
		if (mults[0] ==0){
			if (mults[1] < 0) return  "Up          ";
			if (mults[1] > 0) return  "Down        ";
		}
		if (mults[0] > 0){
			if (mults[1] < 0) return  "Top Right   ";
			if (mults[1] == 0) return "Right       ";
			if (mults[1] > 0) return  "Bottom Right";
		}
		return "No Movement";
	}
	private class Data{
		boolean found;
		int x, y, dir, length;
		String word;
		public Data(int x, int y, int direction, int length, boolean found, String word){
			this.x = x;
			this.y = y;
			this.dir = direction;
			this.length = length;
			this.found = found;
			this.word = word;
		}
		public int getX(){
			return this.x;
		}
		public int getY(){
			return this.y;
		}
		public int[] getCoords(){
			return new int[] {this.x, this.y};
		}
		public int getDirection(){
			return this.dir;
		}
		public int getLength(){
			return length;
		}
		public boolean isFound(){
			return this.found;
		}
		public String getWord(){
			return word;
		}
		public boolean equals(Object other){
			if (other instanceof Data){
				Data o = (Data)other;
				return o.getWord().equals(word);
			}
			return false;
		}
		public String toString(){
			return word;
		}
		public String toFullString(){
			return "Data at: (" + y + "," + x +"), Direction: " + toEnglishDir(dir) + ", word: " + word;
		}
	}
	public Dimension getPreferredSize(){
        return new Dimension(800,600);
    }
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.setColor(Color.black);
		g.fillRect(0,0,800,600);
		
		//draw the highlight around the characters in the word
		g.setColor(new Color(25, 225, 25));
		if (focus != null){
			int x = focus.getX()*600/grid[0].length+200/grid[0].length;
			int y = focus.getY()*600/grid.length+200/grid.length;
			int endX = (focus.getX() + focus.getLength() * getMults(focus.getDirection())[0])*600/grid[0].length+200/grid[0].length;
			int endY = (focus.getY() + focus.getLength() * getMults(focus.getDirection())[1])*600/grid.length+200/grid.length;
			Drawer.drawLineThick(g, x, y, endX, endY, 600/grid[0].length);
			Drawer.fillCenteredCircle(g, x, y, 300/grid[0].length);
			Drawer.fillCenteredCircle(g, endX, endY, 300/grid[0].length);
			//System.out.println(focus.getX() + ", " + focus.getY() + ", " + focus.getLength() + ", " + (focus.getY() + focus.getLength() * getMults(focus.getDirection())[0]) + ", " + (focus.getX() + focus.getLength() * getMults(focus.getDirection())[1]) + ", " + focus.getDirection());
		}
		
		//draw the grid
		g.setColor(Color.white);
		int size = charSize.getFontSize(g);
		g.setFont(new Font("Arial", Font.PLAIN, size));
		for (int x=0;x<grid[0].length;x++){
			for (int y=0;y<grid.length;y++){
				Drawer.drawAbsCenteredStackString(g, "" + grid[y][x], x*600/grid[0].length+200/grid[0].length, y*600/grid.length+200/grid.length, 600);
			}
		}
		
		//draw the words found on the gui
		for (int i=wordHeight.getStep();i < found.length && i-wordHeight.getStep()<11;i++){
			String word = found[i].getWord();
			
			//background box around the text
			g.setColor(Color.darkGray);
			if (focusIndex == i){
				g.setColor(Color.green);
			}
			g.fillRect(600, 50+(i-wordHeight.getStep())*50, 170, 50);
			
			//white border around the box
			g.setColor(Color.white);
			g.drawRect(600, 50+(i-wordHeight.getStep())*50, 170, 50);
			
			//the actual words sized to fit in the box
			wordDisplay[i-wordHeight.getStep()].setText(word);
			g.setFont(new Font("Arial", Font.PLAIN, wordDisplay[i-wordHeight.getStep()].getFontSize(g)));
			Drawer.drawStackString(g, word, 600, 50+(i-wordHeight.getStep())*50+3);
		}
		wordHeight.draw(g);
	}
    public void mouseClicked(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
		if (e.getX() >= 600){
			int y = (e.getY()-30)/50-1;
			focusIndex = Math.min(y+wordHeight.getStep(), found.length-1);
			focus = found[focusIndex];
		}
		repaint();
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
	public void mouseWheelMoved(MouseWheelEvent e) {
		wordHeight.click(e);
		repaint();
	}
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == search){
			for (int i=0;i<found.length;i++){
				if (found[i].getWord().equals(search.getText())){
					focusIndex = i;
					focus = found[i];
					wordHeight.setStep(i);
				}
			}
		}
		repaint();
	}
}