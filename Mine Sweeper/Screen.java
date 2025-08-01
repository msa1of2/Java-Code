import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import helpful.datastructures.DLList;
import helpful.GUI.Button;
import helpful.GUI.Calibration;
import helpful.GUI.Menu;
import helpful.Rectangle;
import helpful.TextFitter;

//File reading / writing
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

//Slider
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

class Screen extends JPanel implements MouseListener, KeyListener{
	private byte[][] board; //each value = 0b0_0_000_000 - [0] = is hidden (bool) - [1] = is bomb (bool) - [2] = isFlag (bool)- [3-6] = adjacent bomb count (0-8)
	private int flagCounter, xSize, ySize, width, height, bombCount, timer;
	private boolean keyboardMovement, didFirstMove, gameOver, lost, hasScoreBoard;
	private byte[] keyboardCursor;
	private JSpinner xSpinner, ySpinner;
	private String errorMessage;
	private Image bombImage, drawBombImage, flagImage, drawFlagImage;
	private Calibration cali;
	private Button calibrationButton, backButton, start, back, retry, submitScore;
	private Menu[] menus;
	private Menu mainMenu, sweeperMenu, calibrationMenu, gameOverMenu;
	private File scoreBoard;
	private String[] scoreData, names;
	private char[] nameChars;
	private int[] scores, times;
	private long[][] cascadeMap; //stores binary data for squares explored by the cascade, O(1) look up while having significantly less memory overhead
	private TextFitter spinnerTextFitter, gameOverNameInsert;
	private long timeStart = 0L;
	
	private static byte EMPTY = (byte)0b1000_0000; //default to a hidden square
	private static int MIN_SIZE = 10; //minimum size for the dimensions of the board
	private static int MAX_SIZE = 75; //maximum size for the dimensions of the board
	private static Color[] mineColors = new Color[] {Color.magenta, Color.green, Color.red, Color.blue, new Color(165,42,42), Color.cyan, Color.black, Color.yellow};
	private static int[] NEGATIVE = new int[] {0, 0, 0, 0, 60, 0, 0, 0};
	private static int[] ZERO = new int[] {0, 126, 66, 66, 66, 66, 66, 126};
	private static int[] ONE = new int[] {0, 24, 8, 8, 8, 8, 8, 8};
	private static int[] TWO = new int[] {0, 126, 2, 2, 126, 64, 64, 126};
	private static int[] THREE = new int[] {0, 126, 2, 2, 30, 2, 2, 126};
	private static int[] FOUR = new int[] {0, 66, 66, 66, 126, 2, 2, 2};
	private static int[] FIVE = new int[] {0, 126, 64, 64, 126, 2, 2, 126};
	private static int[] SIX = new int[] {0, 126, 64, 64, 126, 66, 66, 126};
	private static int[] SEVEN = new int[] {0, 62, 2, 4, 4, 8, 8, 8};
	private static int[] EIGHT = new int[] {0, 60, 66, 66, 60, 66, 66, 60};
	private static int[] NINE = new int[] {0, 126, 66, 66, 126, 2, 2, 126};
	private static int[][] NUMBERS_PIXEL_ART = new int[][] {ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE};
	
	//saving scheme: name, score(32 bits for bombs found, 32 bits for time taken)
	private static final byte SCORE_SIZE 	= 5; //how many scores are saved in the file / leaderboard
	private static final byte NAME_SIZE		= 3; //how big the names are in memory (character count)
	
	/* TODO:
	 * Scroll bars for larger board sizes - limit max size of the board
	 * Zooming into the board to see everything better
	 * Ability to increase the bomb probability
	 */
	
	public Screen(){
		setLayout(null);
		
		setFocusable(true);
		requestFocusInWindow();
		
		addKeyListener(this);
		addMouseListener(this);
		
		keyboardMovement = false;
		didFirstMove = false;
		gameOver = false;
		lost = false;
		
		cali = new Calibration();
		
		keyboardCursor = new byte[2];
		
		nameChars = new char[NAME_SIZE];
		
		errorMessage = "";
		
		try{
			bombImage = ImageIO.read(new File("MineSweeperBomb.png"));
			flagImage = ImageIO.read(new File("MineSweeperFlag.png"));
		}
		catch (IOException e){
			bombImage = null;
			flagImage = null;
		}
		
		calibrationButton = new Button(15, 15, 100, 50, Color.magenta, "Mouse Click\nCalibration");
		
		start = new Button((int)getPreferredSize().getWidth()/2 - 100/2, (int)getPreferredSize().getHeight()/2 + 50*4/5, 100, 50, "Start");
		start.setCalibration(cali);
		
		back = new Button(0, 0, 100, 50, Color.red, "Back");
		back.setCalibration(cali);
		
		retry = new Button((int)getPreferredSize().getWidth()/2 - 100/2, 0, 100, 50, Color.yellow, "Retry");
		retry.setCalibration(cali);
		retry.setVisible(false);
		
		submitScore = new Button((int)getPreferredSize().getWidth()/2 - 150/2, (int)getPreferredSize().getHeight()/2 + 75, 150, 50, "Submit Score");
		submitScore.setCalibration(cali);
		submitScore.setVisible(false);
		
		xSpinner = new JSpinner(new SpinnerNumberModel(MIN_SIZE, MIN_SIZE, MAX_SIZE, 1));
		xSpinner.setBounds((int)getPreferredSize().getWidth()/2 - 110, (int)getPreferredSize().getHeight()/2 - 50/2, (int)getPreferredSize().getWidth()/8, (int)getPreferredSize().getHeight()/24);
		add(xSpinner);
		
		ySpinner = new JSpinner(new SpinnerNumberModel(MIN_SIZE, MIN_SIZE, MAX_SIZE, 1));
		ySpinner.setBounds((int)getPreferredSize().getWidth()/2 + 10, (int)getPreferredSize().getHeight()/2 - 50/2, (int)xSpinner.getBounds().getWidth(), (int)xSpinner.getBounds().getHeight()); //sizes are same for both
		add(ySpinner);
		
		spinnerTextFitter = new TextFitter((int)xSpinner.getBounds().getWidth()-18, (int)xSpinner.getBounds().getHeight()-4, "100");
		gameOverNameInsert = new TextFitter(200, 100, "WWW"); //placeholder sizes and largest width text for most fonts
		
		mainMenu = new Menu(true);
		sweeperMenu = new Menu();
		calibrationMenu = new Menu();
		
		mainMenu.add(xSpinner, ySpinner);
		mainMenu.addMenu(sweeperMenu, start);
		mainMenu.addMenu(calibrationMenu, calibrationButton);
		
		sweeperMenu.add(retry);
		sweeperMenu.add(submitScore);
		sweeperMenu.setBackButton(back);
		
		calibrationMenu.add(cali);
		calibrationMenu.setBackButton(back);
		
		
		menus = new Menu[] {mainMenu, sweeperMenu, calibrationMenu};
		
		scoreBoard = new File("saves.dat");
		hasScoreBoard = scoreBoard != null ? scoreBoard.canRead() : false;
		
		scoreData = readFile();
		names = getNames(scoreData);
		scores = getScores(scoreData);
		times = getTimes(scoreData);
		
		resetScores();
	}
	public Dimension getPreferredSize(){ //panel size
        return new Dimension(800, 600);
    }
	
	//Draw methods
	public void paintComponent(Graphics g){ //draw screen
		super.paintComponent(g); //Don't touch
		g.setFont(new Font("Impact", Font.PLAIN, 15));

		g.setColor(Color.darkGray);
		g.fillRect(0, 0, (int)g.getClipBounds().getWidth(), (int)g.getClipBounds().getHeight());
		
		//width / height of the grid, keeps buttons in line
		if (board != null){
			width = (int)g.getClipBounds().getWidth()/board.length;
			height = ((int)g.getClipBounds().getHeight())/(board[0].length+1);
		}
		else{
			width = (int)g.getClipBounds().getWidth()/10;
			height = ((int)g.getClipBounds().getHeight())/11;
		}
		
		//preserve the aspect ratio of the board to a 4:3 x:y of the board
		if (width / (double)height > 1) width = (height);
		else height = (width);
		
		//keep the buttons where they are supposed to be regardless of the screen size as it changes
		Rectangle bounds = start.toRect();
		if (mainMenu.isVisible()) start.setDimensions((int)g.getClipBounds().getWidth()/2 - (int)g.getClipBounds().getWidth()/8/2, (int)g.getClipBounds().getHeight()/2 + (int)g.getClipBounds().getHeight()/12*4/5, (int)g.getClipBounds().getWidth()/8, (int)g.getClipBounds().getHeight()/12);
		
		bounds = back.toRect();
		if (board != null) back.setDimensions(0, 0, 2*width, height);
		
		bounds = retry.toRect();
		if (board != null) retry.setDimensions(width*2, 0, 2*width, height);
		
		bounds = calibrationButton.toRect(); //only scale when on the main menu, dont waste resources
		if (mainMenu.isVisible()) calibrationButton.setDimensions((int)g.getClipBounds().getWidth()/53, (int)g.getClipBounds().getHeight()/40, (int)g.getClipBounds().getWidth()/8, (int)g.getClipBounds().getHeight()/12);
		
		bounds = submitScore.toRect();
		if (board != null) submitScore.setDimensions(width*xSize/2 - (int)(width*xSize/5.33)/2, (int)height*ySize/2 + (int)height*ySize/12*3/2, (int)(width*xSize/5.33), (int)height*ySize/12);
		
		if (calibrationMenu.isVisible()){
			cali.scale((int)g.getClipBounds().getWidth(), (int)g.getClipBounds().getHeight());
		}
		
		xSpinner.setBounds((int)g.getClipBounds().getWidth()/2 - (int)g.getClipBounds().getWidth()/8-5, (int)g.getClipBounds().getHeight()/2 - (int)g.getClipBounds().getHeight()/24, (int)g.getClipBounds().getWidth()/8, (int)g.getClipBounds().getHeight()/24);
		ySpinner.setBounds((int)g.getClipBounds().getWidth()/2 + +5, (int)g.getClipBounds().getHeight()/2 - (int)g.getClipBounds().getHeight()/24, (int)g.getClipBounds().getWidth()/8, (int)g.getClipBounds().getHeight()/24);
		
		spinnerTextFitter.setBounds((int)xSpinner.getBounds().getWidth()-18, (int)xSpinner.getBounds().getHeight()-4);
		Font spinnerFont = new Font("Consolas", Font.PLAIN, spinnerTextFitter.getFontSize(g, "Consolas"));
		xSpinner.setFont(spinnerFont);
		ySpinner.setFont(spinnerFont);
		
		if (sweeperMenu.isVisible()){
			if (bombImage != null && width!=0 && height!=0) drawBombImage = bombImage.getScaledInstance(width, height, 0);
			if (flagImage != null && width!=0 && height!=0) drawFlagImage = flagImage.getScaledInstance(width, height, 0);
			
			for (int x=0;x<board.length;x++){
				for (int y=0;y<board[x].length;y++){
					if (isHidden(x, y)){
						g.setColor((x+y)%2 == 0 ? new Color(0, 64, 0) : new Color(0, 128, 0));
						g.fillRect(width*x, height*y+height, width, height);
					}
					else{
						g.setColor((x+y)%2 == 0 ? Color.darkGray : Color.gray);
						g.fillRect(width*x, height*y+height, width, height);
					}
					if (isMine(x, y) && !isHidden(x, y)){
						if (bombImage != null && drawBombImage != null) g.drawImage(drawBombImage, width*x, height*y+height, null);
						else {
							g.setColor(Color.black);
							g.drawString("B", width*x, height*(y+1)-5+height);
						}
						continue;
					}
					if (isFlag(x, y)){
						if (flagImage != null && drawFlagImage != null) g.drawImage(drawFlagImage, width*x, height*y+height, null);
						else {
							g.setColor(Color.red);
							g.drawString("F", width*x, height*(y+1)-5+height);
						}
						continue;
					}
					if (getAdjacentCount(x, y) == 0)continue;
					if (isHidden(x, y)) continue;
					g.setColor(mineColors[getAdjacentCount(x, y)-1]);
					
					drawNumbers(g, getAdjacentCount(x, y), x*width, height+y*height, (width-10)/8);
				}
			}
			if (keyboardMovement){
				//the keyboard cursor highlight section
				g.setColor(new Color(128, 128, 255, 64));
				g.fillRect(keyboardCursor[0]*width, (keyboardCursor[1]+1)*height, width, height);
			}
			if (gameOver){
				//The game is now over
				g.setColor(new Color(0, 0, 0, 196));
				g.fillRect(0, height, width*xSize, height*ySize);
				g.setColor(Color.white);
				g.setFont(new Font("Consolas", Font.PLAIN, spinnerFont.getSize() + 25));
				if (lost) g.drawString("You lost :(", width*xSize/2 - g.getFontMetrics().stringWidth("You lost :(")/2, height*ySize/2+height);
				if (!lost)g.drawString("You Win! :)", width*xSize/2 - g.getFontMetrics().stringWidth("You Win! :)")/2, height*ySize/2+height);
				
				if (!lost){
					gameOverNameInsert.setBounds(width*xSize/3, height*ySize/6);
					g.setFont(new Font("Consolas", Font.PLAIN, gameOverNameInsert.getFontSize(g, "Consolas")));
					
					g.setColor(Color.black);
					g.fillRect(width*xSize/2 - width*xSize/6, height*ySize/2 - height*ySize/6, width*xSize/3, height*ySize/6);
					
					g.setColor(Color.white);
					g.drawString(new String(nameChars), width*xSize/2 - width*xSize/6+2, height*ySize/2-2);
					if (new String(nameChars).trim().length() < 3) g.fillRect(width*xSize/2 - width*xSize/6+2 + g.getFontMetrics().stringWidth(new String(nameChars).trim()), height*ySize/2 -2 -height*ySize/48, g.getFontMetrics().stringWidth("W"), height*ySize/48);
					//g.drawString((gameOverNameInsert.getFontSize(g, "Consolas")) + "", 0, 200);
				}
			}
			
			//black ribbon on the top of the screen
			g.setColor(Color.black);
			g.fillRect(0, 0, width*xSize, height);
			//flag counter
			g.setColor(Color.red);
			drawNumbers(g, flagCounter, width*xSize - height, 0, height/8);
			//Timer
			g.setColor(Color.yellow);
			drawNumbers(g, timer, width*xSize - height*4, 0, height/8);
		}
		
		if (mainMenu.isVisible()){
			g.setColor(Color.white);
			g.setFont(new Font("Consolas", Font.PLAIN, TextFitter.getFontSize(g, "X-Dimension:", "Consolas", (int)xSpinner.getBounds().getWidth(), (int)ySpinner.getBounds().getHeight())));
			g.drawString("X-Dimension:", (int)xSpinner.getBounds().getX(), (int)xSpinner.getBounds().getY() - g.getFontMetrics().getHeight()/2);
			g.drawString("Y-Dimension:", (int)ySpinner.getBounds().getX(), (int)ySpinner.getBounds().getY() - g.getFontMetrics().getHeight()/2);
			
			//highscore
			for (int i=0;i<SCORE_SIZE;i++){
				g.drawString(names[i], (int)g.getClipBounds().getWidth()/2 - g.getFontMetrics().stringWidth(names[i]) - (int)g.getFontMetrics().stringWidth("--"), 15 + (i+1)*g.getFontMetrics().getHeight());
				g.drawString("--", (int)g.getClipBounds().getWidth()/2 - (int)g.getFontMetrics().stringWidth("--")/2, 15 + (i+1)*g.getFontMetrics().getHeight());
				g.drawString(scores[i] + " | " + times[i], (int)g.getClipBounds().getWidth()/2 + (int)g.getFontMetrics().stringWidth("--"), 15 + (i+1)*g.getFontMetrics().getHeight());
			}
		}
		
		for (Menu menu : menus){
			menu.draw(g);
		}
	}
	public void drawNumbers(Graphics g, int number, int xPos, int yPos, int sizePerPixel){
		//xPos, yPos is top left corner
		if (number < 0){
			if (number > -10){
				//draw the negative sign and the number
				drawPixelArt8Pix(g, NUMBERS_PIXEL_ART[number*-1], xPos, yPos, sizePerPixel);
				drawPixelArt8Pix(g, NEGATIVE, xPos - sizePerPixel * 8, yPos, sizePerPixel);
				return;
			}
			drawNumbers(g, number/10, xPos - sizePerPixel * 8, yPos, sizePerPixel);
			number*=-1;
			number%=10;
		}
		if (number >= 10){
			drawNumbers(g, number/10, xPos - sizePerPixel * 8, yPos, sizePerPixel);
			number %= 10;
		}
		drawPixelArt8Pix(g, NUMBERS_PIXEL_ART[number], xPos, yPos, sizePerPixel);
	}
	
	//board methods
	public void generateBoard(int xSize, int ySize){
		//called on instantiation
		flagCounter = (int)Math.sqrt(xSize * ySize);
		bombCount = flagCounter;
		board = new byte[xSize][ySize];
		for (int x = 0;x<xSize;x++){
			for (int y=0;y<ySize;y++){
				board[x][y] = EMPTY; //this space is hidden from the viewer
			}
		}
		int x, y;
		for (int mine=flagCounter-1;mine>=0;mine--){
			x=(int)(Math.random()*(xSize-1));
			y=(int)(Math.random()*(ySize-1));
			if (isMine(x, y)){ //this space is already occupied
				mine++; //don't remove form the total mine count for false mines
				continue;
			}
			//empty space - place a bomb / mine there
			placeMine(x, y);
		}
		didFirstMove = false;
	}
	public boolean inBounds(int x, int y){
		return (x>=0 && y>=0 && (board != null ? x<board.length && y<board[0].length : false));
	}
	
	//Game Methods
	public void beginGame(){
		xSize = (Integer)xSpinner.getValue();
		ySize = (Integer)ySpinner.getValue();
		
		generateBoard(xSize, ySize);
		
		timeStart = System.currentTimeMillis();
		
		gameOver = false;
		didFirstMove = false;
		
		retry.setVisible(false);
		submitScore.setVisible(false);
		xSpinner.setVisible(false);
		ySpinner.setVisible(false);
	}
	public void action(int x, int y){
		if (!sweeperMenu.isVisible() || gameOver) return; //can't do crap when the game is over or its the main menu
		if (!inBounds(x, y)) return; //cant do crap outside the bounds of the space to do stuff in
		if (!didFirstMove && isMine(x, y)){
			//remove the mine from its old position and move it randomly somewhere else
			//places a new mine somewhere on the board
			for (int i=0;i<1;i++){
				int xPos=(int)(Math.random()*xSize);
				int yPos=(int)(Math.random()*ySize);
				if (isMine(xPos, yPos)){ //this space is already occupied
					i--;
					continue;
				}
				//empty space - place a bomb / mine there
				placeMine(xPos, yPos);
			}
			removeMine(x, y);
			//can continue like normal now
		}
		
		didFirstMove = true; //no matter what, the player did their first move
		
		if (isMine(x, y) && !isFlag(x, y)){ //game is now over
			doGameOver(true);
		}
		
		if (isHidden(x, y) && !isFlag(x, y)) reveal(x, y);
		
		checkGameOver();
	}
	public void flag(int x, int y){
		if (!inBounds(x, y)) return;
		if (!sweeperMenu.isVisible() || gameOver) return; //can't do crap when the game is over or its the main menu
		boolean flaged = isFlag(x, y);
		setFlag(x, y, !flaged);
		flagCounter += flaged ? 1:-1;
		checkGameOver();
	}
	public void checkGameOver(){
		//checks if the game can now be considered over and then does the game over part
		
		int flaggedCounter=0;
		//checks if all the bombs are properly flagged
		for (int x=0;x<xSize;x++){
			for (int y=0;y<ySize;y++){
				if (isMine(x, y) && isFlag(x, y)){
					flaggedCounter++;
				}
				if (!isMine(x, y) && isFlag(x, y)){
					return; //a flag is wrong, so dont bother trying to see if the bombs are all covered
				}
			}
		}
		if (flaggedCounter != bombCount) return; //flag count is wrong
		doGameOver(false);
	}
	public void doGameOver(boolean didLose){
		gameOver = true;
		retry.setVisible(true);
		lost = didLose;
		if (!lost){
			submitScore.setVisible(true);
		}
	}
	
	//normal mine methods
	private boolean isHidden(int x, int y){
		return getFlag(board[x][y], 0);
	}
	private void reveal(int x, int y){
		board[x][y] = setFlag(board[x][y], 0, false);
		DLList<Integer> callList = new DLList<Integer>();
		callList.add(packCoords(x, y));
		cascadeMap = new long[xSize/64 + (xSize%64==0?0:1)][ySize]; //creates a map of long values 64 bits each, 
		int[] dirs = new int[] {0,1,1,1,0,-1,-1,-1};
		int xDir, yDir;
		while (callList.size() > 0){
			x = unpackXCoord(callList.get(0));
			y = unpackYCoord(callList.get(0));
			callList.remove(0); //remove our current space
			board[x][y] = setFlag(board[x][y], 0, false); //reveal this space
			boolean expand = explore(x, y) == 0; //explore into the unknown
			if (!expand) continue; //this space has a number, don't trigger the bombs automatically
			
			for (int i=0;i<8;i++){
				xDir = dirs[i]; //start at top, go clockwise
				yDir = dirs[(i+2)%8];
				if (inBounds(x+xDir, y+yDir) ? !didExplore(x+xDir, y+yDir) : false) {callList.add(packCoords(x+xDir, y+yDir));explore(x+xDir,y+yDir);}//System.out.print("(" + (x+xDir) + ", " + (y+yDir) + "), ");
			}
		}
	}
	private boolean isMine(int x, int y){
		return getFlag(board[x][y], 1);
	}
	private boolean isFlag(int x, int y){
		return getFlag(board[x][y], 2);
	}
	private void setFlag(int x, int y, boolean val){
		board[x][y] = setFlag(board[x][y], 2, val);
	}
	private int getAdjacentCount(int x, int y){
		return getCount(board[x][y]);
	}
	private void placeMine(int x, int y){
		board[x][y] = setFlag(board[x][y], 1, true);
		//increase the count of the adjacent squares
		int[] dirs = new int[] {0,1,1,1,0,-1,-1,-1};
		int xDir, yDir;
		for (int i=0;i<8;i++){
			xDir = dirs[i]; //start at top, go clockwise
			yDir = dirs[(i+2)%8];
			if (inBounds(x+xDir, y+yDir)) board[x+xDir][y+yDir] = increaseCount(board[x+xDir][y+yDir], 1);
		}
	}
	private void removeMine(int x, int y){ //useful for first click survival
		board[x][y] = setFlag(board[x][y], 1, false);
		//decrease the count of the adjacent squares
		int[] dirs = new int[] {0,1,1,1,0,-1,-1,-1};
		int xDir, yDir;
		for (int i=0;i<8;i++){
			xDir = dirs[i]; //start at top, go clockwise
			yDir = dirs[(i+2)%8];
			if (inBounds(x+xDir, y+yDir)) board[x+xDir][y+yDir] = increaseCount(board[x+xDir][y+yDir], -1);
		}
	}
	
	//cascade methods
	private int packCoords(int x, int y){
		int send = (x&0xffff)<<16;
		send |= (y&0xffff);
		return send;
	}
	private int unpackXCoord(int packed){
		return (packed >> 16) & 0xffff;
	}
	private int unpackYCoord(int packed){
		return (packed & 0xffff);
	}
	private byte explore(int x, int y){
		//takes x, y coords from the cascade loop, returns the number of mines around that location
		if (cascadeMap == null) return -1;
		cascadeMap[x/64][y] = setFlag(cascadeMap[x/64][y], x%64, true);
		return (byte)getAdjacentCount(x, y);
	}
	private boolean didExplore(int x, int y){
		if (cascadeMap == null) return true;
		return getFlag(cascadeMap[x/64][y], x%64);
	}
	private long setFlag(long input, int bit, boolean value){
		if (value)
			return (long)(input | 1L<<(63-bit));
		return (long)(input & ~(1L<<63-bit));
	}
	private boolean getFlag(long input, int bit){
		return ((input >>> (63-bit)) & 1) == 1;
	}
	
	//mine data manipulation methods - assumes the input passed in is the same data storage as board / mines
	private byte setFlag(byte input, int bit, boolean value){ //sets a boolean flag
		if (value){
			return (byte)(input | 1<<(7-bit)); //sets the flag to true
		}
		return (byte)(input & ~(1<<(7-bit))); //sets the flag to false
	}
	private boolean getFlag(byte input, int bit){
		return ((input >>> (7-bit)) & 1) == 1;
	}
	private byte increaseCount(byte input, int amount){ //increases a count
		int work = ((input>>1) & 0xf);
		work += amount;
		work %= 9;
		input &= ~(0xf<<1); //clears the most recent count
		input |= (work&0xf)<<1;
		return (byte)input;
	}
	private byte getCount(byte input){
		return (byte)((input >> 1) & 0xf);
	}
	
	//pixel art methods
	public void drawPixelArt8Pix(Graphics g, int[] rows, int xStart, int yStart, int sizePerPixel){
		//xStart, yStart are top left corner
		int[] copy = new int[rows.length];
		System.arraycopy(rows, 0, copy, 0, rows.length);
		for (int y=0;y<copy.length;y++){
			for (int x=0;x<8;x++){
				if ((copy[y] & 1) == 1) g.fillRect(xStart + (8-x)*sizePerPixel, yStart + y*sizePerPixel, sizePerPixel, sizePerPixel);
				copy[y] >>>= 1;
			}
		}
	}
	public void drawPixelArt8Pix(Graphics g, int[] rows, int xStart, int yStart){
		drawPixelArt8Pix(g, rows, xStart, yStart, 1);
	}
	
	//Save file things
	public String[] readFile(){
		String[] send = new String[SCORE_SIZE];
		byte[] line = new byte[8];
		FileInputStream reader;
		try{
			reader = new FileInputStream(scoreBoard);
			reader.read(line);//read a single line for the mouse calibration
		}
		catch (IOException e){
			e.printStackTrace();
			for (int i=0;i<send.length;i++){send[i] = "ERROR";}
			return send;
		}
		
		cali.dx = toInt(line, 0);
		cali.dy = toInt(line, 4);
		
		line = new byte[NAME_SIZE+8];
		for (int i=0;i<send.length;i++){
			try{
				reader.read(line);
			}
			catch(IOException e){
				e.printStackTrace();
			}
			send[i] = new String(line, 0, NAME_SIZE);
			send[i] += ": " + toInt(line, NAME_SIZE);
			send[i] += ": " + toInt(line, NAME_SIZE+4);
		}
		return send;
	}
	public String[] getNames(String[] rawScoreData){
		String[] send = new String[SCORE_SIZE];
		if (rawScoreData == null) return send; //can't do crap to null
		char[] tempList, tempSend = new char[NAME_SIZE]; //minimize amount of ram needed (very minute difference compared to in the loop)
		for (int i=0;i<Math.min(rawScoreData.length, SCORE_SIZE);i++){
			tempList = rawScoreData[i].toCharArray(); //get the char array of the raw score data
			System.arraycopy(tempList, 0, tempSend, 0, NAME_SIZE); //copy only the part that contains the name
			send[i] = new String(tempSend); //add the name to the list
		}
		return send; //send it back
	}
	public int[] getScores(String[] rawScoreData){
		int[] send = new int[SCORE_SIZE];
		if (rawScoreData == null) return send; //can't do crap to null
		char[] tempList, nums = new char[10]; //max size of number
		String[] splitData;
		for (int i=0;i<Math.min(rawScoreData.length, SCORE_SIZE);i++){
			splitData = rawScoreData[i].split(":");
			tempList = splitData[1].toCharArray();
			System.arraycopy(tempList, 1, nums, 0, Math.min(tempList.length-1, nums.length)); //get the number from the score data
			send[i] = Integer.parseInt((new String(nums)).trim()); //turn the string into a number
			nums = new char[10]; //reset nums so excess scores don't get carried over to the next person's score
		}
		return send; //send the scores
	}
	public int[] getTimes(String[] rawScoreData){
		int[] send = new int[SCORE_SIZE];
		if (rawScoreData == null) return send; //can't do crap to null
		char[] tempList, nums = new char[10]; //max size of number
		String[] splitData;
		for (int i=0;i<Math.min(rawScoreData.length, SCORE_SIZE);i++){
			splitData = rawScoreData[i].split(":");
			tempList = splitData[2].toCharArray();
			System.arraycopy(tempList, 1, nums, 0, Math.min(tempList.length-1, nums.length)); //get the number from the score data
			send[i] = Integer.parseInt((new String(nums)).trim()); //turn the string into a number
			nums = new char[10]; //reset nums so excess scores don't get carried over to the next person's score
		}
		return send; //send the scores
	}
	public boolean save(String[] names, int[] scores, int[] times){
		//writes the names and scores to the score board file, has built in failsafes, can pass in empty lists
		if (!hasScoreBoard) return false; //cant write to what doesn't exist
		FileOutputStream writer;
		try{
			writer = new FileOutputStream(scoreBoard);
			writer.write(toBytes(cali.dx, cali.dy));
		}
		catch (IOException e){
			e.printStackTrace();
			return false;
		}
		
		byte[] line = new byte[NAME_SIZE+8];
		try{
			for (int i=0;i<Math.min(SCORE_SIZE, 8);i++){
				//write the name
				char[] letters = new char[] {'A', 'A', 'A'};
				if (names == null ? false : names.length > i) letters = names[i].toCharArray();
				for (int n=0;n<NAME_SIZE;n++){
					line[n] = (letters.length > n ? (byte)Character.toUpperCase(letters[n]) : (byte)' ');
				}
				//write the score (32 bit integer limit maximum ~ 2.1B)
				byte[] scoreBytes = toBytes(10, 15 * (int)Math.pow(10, i)); //default score, creats 1.5m second run in last place, 15 sec run in first place
				if (scores == null ? false : scores.length > i) scoreBytes = toBytes(scores[i], times[i]);
				System.arraycopy(scoreBytes, 0, line, NAME_SIZE, 8);
				//actually write to the file
				writer.write(line);
			}
		}
		catch (IOException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	//byte translation functions
	public static byte[] toBytes(int... nums){
		if (nums == null) throw new NullPointerException("Array cannot be null");
		byte[] send = new byte[nums.length*4];
		
		for (int n=0;n<nums.length;n++){
			int num = nums[n];
			for (int i=0;i<4;i++){
				send[3-i+4*n] = (byte)(num & 0xFF);
				num >>= 8;
			}
		}
		
		return send;
	}
	public static byte[] toBytes(long... nums){
		if (nums == null) throw new NullPointerException("Array cannot be null");
		byte[] send = new byte[nums.length*8];
		
		for (int n=0;n<nums.length;n++){
			long num = nums[n];
			for (int i=0;i<8;i++){
				send[7-i+8*n] = (byte)(num & 0xFF);
				num >>= 8;
			}
		}
		
		return send;
	}
	public static int toInt(byte[] array){
		return toInt(array, 0);
	}
	public static int toInt(byte[] array, int startIndex){
		if (array == null) throw new NullPointerException("Array cannot be null");
		if (array.length < startIndex + 4) throw new IllegalArgumentException("Not enough bytes to convert into integer, startIndex:" + startIndex + ", Array Size:" + array.length);
		if (startIndex >= array.length) throw new IndexOutOfBoundsException();
		int send = 0;
		for (int i = 0;i<4;i++){
			send <<= 8;
			send |= (array[startIndex+i]&0xFF);
		}
		return send;
	}
	
	//Score board functions
	public void insert(String name, int score, int time){
		if (score < scores[SCORE_SIZE-1]) return; //no insertian if the bomb count isn't impressive
		if (score == scores[SCORE_SIZE-1] && time > times[SCORE_SIZE-1]) return; //time taken for last place score isn't impressive
		for (int i=SCORE_SIZE-1;i>=0;i--){
			if (score > scores[i] && i!= 0) continue; //go up farther, our score is bigger than their score
			if (score == scores[i] && time < times[i] && i!= 0) continue; //go up farther, the scores are the same but we have a faster time
			if (time == times[i]) insert(name, score, time, i);
			else if (i==0 && score == scores[i]) insert(name, score, time, i);
			else insert(name, score, time, i+1);
			break;
		}
	}
	private void insert(String name, int score, int time, int index){
		//actually inserts the data into the lists
		for (int i=SCORE_SIZE-1;i>index;i--){
			names[i] = names[i-1];
			scores[i] = scores[i-1];
			times[i] = times[i-1];
		}
		names[index] = name;
		scores[index] = score;
		times[index] = time;
	}
	private boolean canSubmit(){
		for (int i=0;i<nameChars.length;i++){
			if ((int)nameChars[i] == 0) return false;
		}
		return true;
	}
	private void submitScore(){
		if (!canSubmit()) return;
		insert(new String(nameChars), bombCount, timer);
		save(names, scores, times);
	}
	private void resetScores(){
		//resets the scoreboard to base values
		char[] name = new char[NAME_SIZE];
		for (int i=0;i<SCORE_SIZE;i++){
			for (int n=0;n<name.length;n++){
				name[n] = (char)((SCORE_SIZE-i-1)+65);
			}
			insert(new String(name), 10, 50 + (SCORE_SIZE-1-i)*5, 0);
		}
		
		save(names, scores, times);
	}
	
	//keyboard cursor movement functions
	public void moveCursor(int xDir, int yDir){
		if (!inBounds(keyboardCursor[0]+xDir, keyboardCursor[1]+yDir)) return; //the resulting move would take the cursor out of bounds
		keyboardCursor[0] += xDir;
		keyboardCursor[1] += yDir;
	}
	
	//mouse events
	public void mouseClicked(MouseEvent e){
		errorMessage = ""; //reset any error messages
		
		if (sweeperMenu.isVisible() && width != 0 && height != 0){
			if (keyboardMovement && e.getY() > height){
				keyboardMovement = false;
				return;
			}
			//width / height haven't been created yet
			int x = (e.getX()+cali.dx)/width;
			int y = (e.getY()+cali.dy)/height-1;
			if (e.getButton() == MouseEvent.BUTTON1) action(x,y);
			if (e.getButton() == MouseEvent.BUTTON3) flag(x, y);
			repaint();
		}
		
		if (back.isClicked(e) && (sweeperMenu.isVisible() || calibrationMenu.isVisible())){
			//handled by menus
			save(names, scores, times);
			retry.setVisible(false);
			submitScore.setVisible(false);
		}
		if (start.isClicked(e) && mainMenu.isVisible()){
			beginGame();
		}
		if (retry.isClicked(e) && sweeperMenu.isVisible()){
			beginGame();
		}
		if (submitScore.isClicked(e) && sweeperMenu.isVisible() && canSubmit()){
			submitScore();
			sweeperMenu.goTo(mainMenu);
			return;
		}
		for (Menu menu : menus){
			if (menu.click(e)) break;
		}
		if (!errorMessage.equals("")){
			sweeperMenu.goTo(mainMenu); //go back to the main menu, there is an error
		}
    }
    public void mouseEntered(MouseEvent e){
        repaint();
        //mouse moves over window, window does not need to be on top
    }
    public void mouseExited(MouseEvent e){
        repaint();
        //mouse exits the window
    }
    public void mousePressed(MouseEvent e){
        repaint();
        //a button was pressed
    }
    public void mouseReleased(MouseEvent e){
        repaint();
        //a button was released
    }
	
	//Key events
	public void keyPressed(KeyEvent e){
		if (calibrationMenu.isVisible()){
			cali.keyPressed(e);
			repaint();
			return;
		}
		if (sweeperMenu.isVisible()){
			if (gameOver){
				if (!lost || true){
					//name input
					if ((e.getKeyCode() >= 0x41 && e.getKeyCode() < 0x5a)){
						//valid key press
						for (int i=0;i<NAME_SIZE;i++){
							if (nameChars[i] != (char)0) continue; //already wrote letters here
							nameChars[i] = Character.toUpperCase(e.getKeyChar()); //put the letter in its position
							if (i == NAME_SIZE-1){
								//can now submit
								submitScore.setColor(Color.green);
								submitScore.setTextColor();
							}
							break; //did it now we can leave
						}
					}
					else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
						//remove a character
						boolean removed = false;
						for (int i=NAME_SIZE-1;i>=0;i--){
							if (nameChars[i] == (char)0) continue; //haven't found the end of the current characters yet
							nameChars[i] = (char)0; //remove this character that is not 0 / nothing
							//show that you cannot submit now, a letter was removed
							submitScore.setColor(Color.darkGray);
							submitScore.setTextColor();
							removed = true;
							break; //did it so now leave
						}
					}
					else if (e.getKeyCode() == KeyEvent.VK_ENTER && canSubmit()){
						//Confirmation this is the name they want to enter
						submitScore();
					}
				}
				return;
			}
			if (!keyboardMovement){
				keyboardMovement = true;
				return;
			}
			switch (e.getKeyCode()){
				case KeyEvent.VK_S:
				case KeyEvent.VK_DOWN:
					moveCursor(0, 1);
					break;
				case KeyEvent.VK_W:
				case KeyEvent.VK_UP:
					moveCursor(0, -1);
					break;
				case KeyEvent.VK_A:
				case KeyEvent.VK_LEFT:
					moveCursor(-1, 0);
					break;
				case KeyEvent.VK_D:
				case KeyEvent.VK_RIGHT:
					moveCursor(1, 0);
					break;
				case KeyEvent.VK_Q:
				case KeyEvent.VK_SPACE:
					action(keyboardCursor[0], keyboardCursor[1]);
					break;
				case KeyEvent.VK_E:
				case KeyEvent.VK_F:
					flag(keyboardCursor[0], keyboardCursor[1]);
					break;
			}
		}
	}
	public void keyReleased(KeyEvent e){
		
	}
	public void keyTyped(KeyEvent e){
		
	}

	public void gameLoop(){
		while(true){
            try{
                Thread.sleep(100);
            }
            catch(InterruptedException ex){
                Thread.currentThread().interrupt();
            }
			if (timeStart != 0L && !gameOver) timer = (int)(System.currentTimeMillis() - timeStart)/1000;
            repaint();
        }
	}
}