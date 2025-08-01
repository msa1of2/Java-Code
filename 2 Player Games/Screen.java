import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;

//games
import games.*;

//GUI stuff
import helpful.GUI.*;
import helpful.Scalable;

//connection stuff
import helpful.connection.*;

//datastructures
import helpful.datastructures.DLList;

//import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
//mouse events
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
//key presses
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
//JTextField enter evnet
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
//draw string fonts
import java.awt.Font;
//Image
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
//read/write to file
import java.util.Scanner;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
//for data/time things
import java.util.Date;
//window closing events - closes the socket properly
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
//border of the JTextField
import javax.swing.border.LineBorder;
//documents for the JTextField
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
//Getting all the games in the Game Folder

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class Screen extends JPanel implements KeyListener, MouseListener, WindowListener, MouseWheelListener, ActionListener{
    private Game[] games;
    //private Game drawIcon;
    private int selection, gameSize, gameChoice, replayMenuOffset, connectionScreenButton;
    private Button again, newGame, chooseGame, cancel, debug, saveGame, replayButton;
	private Button gameButton, back, settings, menuButton;
	private Button replayChooseGame, replayRemoveSave, host, join;
    private boolean isHosting;
    private ReplayManager replays;
	private String errorString;
	private ConnectionHandler connection;
	private JTextField ipReader;
	private Menu[] menus;
	private ScrollBar replayMenuScrollBar, gameSelectScrollBar;
	private Text serverAddress;
	private Scalable g;
    public Screen(){
		setLayout(null);
		
		g = new Scalable(800, 600);
		g.preserveAspectRatio(true);
		
		//games
        games = new Game[100];
		
		try{
			Path startDir = Paths.get("./games");

			Object[] temp = Files.walk(startDir).toArray(); // Prints all paths
			
			DLList<String> gameNames = new DLList<String>();
			for (Object item : temp){
				Path path = (Path)item;
				if (path.getFileName().toString().endsWith(".java")){
					String name = path.getFileName().toString().split(".java")[0];
					gameNames.add(name);
				} 
			}
			DLList<Game> gams = new DLList<Game>();
			for (String className : gameNames){
				try {
					ClassLoader classLoader = this.getClass().getClassLoader();
					Class<?> myClass = classLoader.loadClass("games."+className); // Replace with the fully qualified class name
					Game obj = (Game)myClass.getDeclaredConstructor(int.class).newInstance((int)gams.size()); // Create an instance of the class
					gams.add(obj);
					// Use the object as needed
				} catch (ClassNotFoundException e) {
					// Handle the case where the class is not found
					System.out.println("Class not found: " + e.getMessage());
				} catch(NoSuchMethodException e){
					//do nothing
				} catch (Exception e){
					e.printStackTrace();
				}
			}
			int counter = 0;
			for (Game game : gams){
				games[counter++] = game;
			}
			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		//count how many games are able to be played
		gameSize=0;
        for (int i=0;i<games.length;i++){
            if (games[i] == null){
                gameSize = i;
                break;
            }
        }

        this.selection = -1;
		this.gameChoice = -1;
		this.replayMenuOffset = 0;
		this.errorString = "";
		
		connectionScreenButton = 0;
        
        replays = null;
		
		//set up buttons
		//in game buttons
        
        debug = new Button(15, 75, 170, 30, Color.darkGray, "Debug");
		debug.setVisible(false);
		
		
		//choose game buttons
        chooseGame = new Button(15, 350, 170, 30, Color.green, "Choose");
        chooseGame.setVisible(false);
        cancel = new Button(15, 300, 170, 30, Color.red, "Cancel");
        cancel.setVisible(false);
        
		//replay buttons
        replayButton = new Button(100, 250, 200, 50, Color.darkGray, "Replay");
		gameButton = new Button(500, 250, 200, 50, Color.lightGray, "Play");
		
		//connection buttons
		host = new Button(150, 275, 200, 50, Color.lightGray, "Host");
		join = new Button(450, 240, 200, 50, Color.darkGray, "Join");
		Document ipReadDoc = new PlainDocument(){
			public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
			    if (str == null)
					return;
			    if ((getLength() + str.length()) <= 17) {
					super.insertString(offset, str, attr);
			    }
		   }
		};
		ipReader = new JTextField(ipReadDoc, "IP: ", 17);
		ipReader.setBounds(450, 300, 200, 50);
		add(ipReader);
		ipReader.setFont(new Font("Arial", Font.PLAIN, 20));
		//messing with the display of ipReader
		ipReader.setCaretColor(Color.white);
		ipReader.setBackground(Color.black);
		ipReader.setSelectedTextColor(Color.white);
		ipReader.setSelectionColor(Color.red);
		ipReader.setForeground(Color.white);
		ipReader.setBorder(new LineBorder(Color.black));
		ipReader.addActionListener(this);
		
		try {
			again = new Button(75, 12, 50, 50, Color.lightGray, ImageIO.read(new File("./images/RetryIcon.png")));
			again.setVisible(false);
			newGame = new Button(12, 12, 50, 50, Color.lightGray, ImageIO.read(new File("./images/BackArrow.png")));
			newGame.setVisible(false);
			saveGame = new Button(137, 12, 50, 50, Color.lightGray, ImageIO.read(new File("./images/SaveIcon.png")));
			saveGame.setVisible(false);
			
			settings = new Button(15, 15, 50, 50, Color.lightGray, ImageIO.read(new File("./images/SettingsIcon.png")));
			menuButton = new Button(15, 15, 50, 50, Color.lightGray, ImageIO.read(new File("./images/MenuIcon.png")));
			
			replayChooseGame = new Button(15, 80, 50, 50, Color.green, ImageIO.read(new File("./images/TriangleToTheRight.png")));
			replayChooseGame.setVisible(false);
			replayRemoveSave = new Button(15, 145, 50, 50, Color.red, ImageIO.read(new File("./images/Trashcan.png")));
			replayRemoveSave.setVisible(false);
		}
		catch (IOException e) {
            //nothing really happens, the images just dont load properly
            settings = new Button(15, 15, 50, 50, Color.lightGray, "Settings");
            menuButton = new Button(15, 15, 50, 50, Color.lightGray, "Menu");
			
            replayChooseGame = new Button(15, 80, 50, 50, Color.green, "Play");
			replayRemoveSave = new Button(15, 145, 50, 50, Color.red, "Remove");
			
			again = new Button(75, 12, 50, 50, "Again");
			newGame = new Button(12, 12, 50, 50, "Back");
			saveGame = new Button(137, 12, 50, 50, "Save Game");
        
			saveGame.setVisible(false);
			newGame.setVisible(false);
			again.setVisible(false);
            replayChooseGame.setVisible(false);
			
			
            e.printStackTrace();
			System.out.println("Issues");
        }
		
		//menus
		menus = new Menu[8];
		menus[0] = new Menu(true); //Main screen
		menus[1] = new Menu(); //Host/Join screen
		menus[2] = new Menu(); //Purgatory
		menus[3] = new Menu(); //game selection
		menus[4] = new Menu(); //actual game - for ease of use
		menus[5] = new Menu(); //replay menu
		menus[6] = new Menu(); //replay game - for ease of use
		menus[7] = new Menu(new Text(300, 50, 100, 50, "Settings")); //Settings menu
		
		//allowing the menus to go to the parents
		for (Menu menu : menus){
			menu.setBackButton(menuButton);
		}
		
		//Main Screen buttons
		menus[0].addMenu(menus[1], gameButton); //to connection screen
		menus[0].addMenu(menus[5], replayButton); //to replay selected screen
		menus[0].addMenu(menus[7], settings); //to settings screen - WIP
		menus[0].remove(menuButton);
		menus[0].add(new Text(150, 25, 500, 200, "Games", Text.CENTERED, Color.white)); //Main title
		menus[0].add(new Text(300, 520, 200, 60, "Made by Max S.\nArt by Alex S.", Text.CENTERED, Color.gray)); //credits
		
		//Host/Join screen buttons
		menus[1].addMenu(menus[2], host);
		menus[1].addMenu(menus[3], join);
		menus[1].add(ipReader);
		
		//Host's purgatory Screen
		menus[2].add(new Text(0, 165, 800, 70, "Waiting for Client to Join", Text.CENTERED, Color.white));
		serverAddress = new Text(00, 365, 800, 70, "Not Set up properly", Text.CENTERED, Color.white);
		menus[2].add(serverAddress);
		
		//Game selection Screen
		menus[3].addMenu(menus[4], chooseGame);
		menus[3].add(cancel);
		gameSelectScrollBar = new ScrollBar(800, 0, 30, 630, gameSize/3, 3, false);
		menus[3].add(gameSelectScrollBar);
		
		//Actual game menu
		menus[4].setBackButton(newGame);
		menus[4].setParent(menus[3]);
		menus[4].add(again);
		menus[4].add(saveGame);
		menus[4].add(debug);
		
		//Replay Menu / replay game selection screen
		menus[5].addMenu(menus[6], replayChooseGame);
		menus[5].add(replayRemoveSave);
		replayMenuScrollBar = new ScrollBar(read().length-14, 11);
		menus[5].add(replayMenuScrollBar);
		
		//Replay game
		menus[6].addMenu(menus[5], replayRemoveSave);
		menus[5].setParent(menus[0]); //replay game overrides as the parent in above line
		
		//Settings Menu
		//menus[7].add();
		
		//add the games to the game selection panel
		for (int i=0;i<gameSize;i++){
			menus[3].add(games[i]);
		}
    }
    public Screen(int choice){
        this();
		this.selection = choice;
    }
    public void drawThickRect(Graphics g, int x, int y, int width, int length, int thickness){
        g.fillRect(x-thickness/2, y-thickness/2, thickness, length); //top left to bottom left
        g.fillRect(x-thickness/2, y-thickness/2 + length, width+thickness, thickness); //bottom left to bottom right
        g.fillRect(x-thickness/2 + width, y-thickness/2, thickness, length); //top right to bottom right
        g.fillRect(x-thickness/2, y-thickness/2, width+thickness, thickness); //top left to top right
    }
	public void drawStackString(Graphics g, String text, int x, int y, int width){
        int line = 0, lastI=0;
		y= y+(int)(g.getFontMetrics().getHeight()*(1-21.0/59));
        String send = "";
        for (int i=0;i<text.length();i++){
            boolean newLine=false;
            if (text.charAt(i) != '\n'){
                if (g.getFontMetrics().stringWidth(send + text.charAt(i)) > width){
                    for (int j=send.length()-1;j>=0;j--){
                        if (send.charAt(j) == '-'){
                            send = send.substring(0, j);
                            i=lastI+j+(lastI != 0 ? 1:0);
                            break;
                        }
                        else if (send.charAt(j) == ' '){
                            send = send.substring(0, j);
							//weird error occurs on only the first line break like this, the first letter of the next line is cut off
                            i=lastI+j+(lastI != 0 ? 1:0);
                            break;
                        }
                    }
                    newLine = true;
                }
                else{
                    send += text.charAt(i);
                }
            }
            if (text.charAt(i) == '\n' || i == text.length()-1 || newLine){
				//line*(g.getFontMetrics().getHeight())
                g.drawString(send, x, y + line*(g.getFontMetrics().getHeight()) + 3);
                send = "";
                line++;
                lastI = i;
            }
        }
    }
	public void drawStackString(Graphics g, String text, int x, int y, int width, boolean horizontalyCentered){
		if (!horizontalyCentered){ drawStackString(g, text, x, y, width); return;} //wont call this method
		int line = 0, lastI=0;
		y= y+(int)(g.getFontMetrics().getHeight()*(1-21.0/59));
        String send = "";
        for (int i=0;i<text.length();i++){
            boolean newLine=false;
            if (text.charAt(i) != '\n'){
                if (g.getFontMetrics().stringWidth(send + text.charAt(i)) > width){
                    for (int j=send.length()-1;j>=0;j--){
                        if (send.charAt(j) == '-'){
                            send = send.substring(0, j);
                            i=lastI+j+(lastI != 0 ? 1:0);
                            break;
                        }
                        else if (send.charAt(j) == ' '){
                            send = send.substring(0, j);
							//weird error occurs on only the first line break like this, the first letter of the next line is cut off
                            i=lastI+j+(lastI != 0 ? 1:0);
                            break;
                        }
                    }
                    newLine = true;
                }
                else{
                    send += text.charAt(i);
                }
            }
            if (text.charAt(i) == '\n' || i == text.length()-1 || newLine){
				//line*(g.getFontMetrics().getHeight())
                g.drawString(send, x-g.getFontMetrics().stringWidth(send)/2, y + line*(g.getFontMetrics().getHeight()) + 3);
                send = "";
                line++;
                lastI = i;
            }
        }
	}
	public void fillOctogon(Graphics g, int x, int y, int width, int length, boolean isCentered){
		if (isCentered){
			x-=width/2;
			y-=length/2;
		}
		int[] xPoints = new int[] {x, x+width/4, x+width*3/4, x+width, x+width, x+width*3/4, x+width/4, x};
		int[] yPoints = new int[] {y+length/4, y, y, y+length/4, y+length*3/4, y+length, y+length, y+length*3/4};
		g.fillPolygon(xPoints, yPoints, 8);
	}
    public Dimension getPreferredSize(){ //panel size
        return new Dimension(Game.XSIZE+200, Game.YSIZE);
    }
    public void paintComponent(Graphics graphics){ //draw screen
        super.paintComponent(g); //Don't touch

        graphics.setColor(Color.darkGray);
        graphics.fillRect(0, 0, (int)graphics.getClipBounds().getWidth(), (int)graphics.getClipBounds().getHeight());
		//g.setColor(Color.black);
		//g.fillRect(0, 0, Game.XSIZE+200, Game.YSIZE);
		g.setBackgroundColor(Color.black);
		g.init(graphics);
		
		
		g.setFont(new Font("Arial", Font.PLAIN, 15));

        /*things to do,
         * get replay to be working properly - TTT checked, C4 checked, Checkers checked, Battleship checked, Stratego checked
         * expand to work with all the other games
		 * add Chess icon
		 * add Strateo icon
		 * Connection screen - add keyboard inputs like title screen
		 * game selection - add keyboard inputs for game selection
		 * art assets for Chess - code drawn
		 * Game selection screen - get a chat messaging service going - likely going to be in the bottom left corner
		 *
		 * Chess - Rook redesign, main body too wide, covers attack indicator
		 * Chess - King can still capture contested Units into death zone
		 * Chess - Do we want to do checks?
		 * Have replay manager display x,y coords of the board and what just happened in the game (unclear with stratego for what just happened)
         */
		 
		 //3-25-24 completely recoded checkers to be more sane
		 //4-14-24 Note to self - if adding any end-early code - identify it as such with comments (2 odd day wild goose chase)
		//close the connection and show it to be such 
		if (connection != null ? connection.isClosed() : false){
			//System.out.println(connection != null ? (connection.isClosed() + ", " + connection.toString()) : "null");
			backToMainMenu();
			//if (connection.hasError()) errorString = connection.getError();
			connection = null;
			for (Game game : games){
				if (game == null) break;
				game.setConnectionHandler(null);
			}
		}
		else if (connection != null){
			//there is a connection that is open
			if (connection.hasReads()){ //there are messages to read
				byte[] nums = connection.read();
				//System.out.println("Check: " + nums[0]); 
				if (nums[0] == 4 && !connection.isServer()){ //otherside is controling which game to play
					//board selection info
					if (selection == -1 && nums[3] < gameSize && nums[3] >= -1){
						//no game currently selected - server selecting a game to play
						gameChoice = nums[3];
						if (nums[2] == 1 && nums[3] != -1){
							chooseGameLogic(nums[3], false);
						}
					}
					else if (nums[2] == 1){
						newGameLogic(false);
					}
					else if (nums[1] == 1){
						againLogic(false);
					}
					connection.remove(); //remove the command - it was dealt with
				}
				if (nums[0] == 0 && (selection != -1 ? (nums[7] == games[selection].getGameID() && games[selection].isStandardMove()) : false)){
					//checkers isn't in general, the jumping making the moves werid so it'll be handled there
					//general move
					boolean res = games[selection].move(nums[1], nums[2], nums[3], nums[4], false, false);
					//if (!res) //move failed, send error
						//connection.send(new byte[] {3, (byte)games[selection].getGameID(), 0, -1, -1, -1, -1, -1}); //error, board sync failed
					connection.remove(); //remove the command - dealt with
				}
				if (nums[0] == 1 && selection != -1 && nums[2] == games[selection].getGameID()){
					//board set up, and game is being played, and it is the proper game
					byte size = nums[1];
					byte[] bufferArray = new byte[size]; //makes a buffer of size specified in second position
					System.arraycopy(nums, 3, bufferArray, 0, Math.min(size, 5)); //takes the first 5 data pieces and puts into buffer array
					connection.remove(); //removes the current byte array dealt with data send
					if (size > 5){
						//will occupy more than one data send array
						byte arrays = (byte)((size-5)/7 + ((size-5)%7 == 0 ? 0 : 1));
						for (int arr=0;arr<arrays;arr++){
							byte[] temp = null;
							int loops = 0;
							while (loops<100 && temp == null){
								loops++;
								temp = connection.read();
								try{
									Thread.sleep(10);
								}
								catch(InterruptedException ex){
									Thread.currentThread().interrupt();
								}
							}
							if (temp == null){
								errorString = "Error code: Sc1";
								return;
							}
							if (temp[0] == 6){
								System.arraycopy(temp, 1, bufferArray, 5 + arr*7, Math.min(7, (size-5)-arr*7));
							}
						}
					}
					games[selection].parseBoard(bufferArray);
				}
				if (nums[0] == 5 || (nums[0] == 0 && (selection != -1 ? !games[selection].isStandardMove() : false))){
					games[selection].handleMessage(nums);
					connection.remove();
				}
			}
			if (connection.hasError() && errorString.equals("")){ //if the connection handler has an error and theres no current error to display
				errorString = connection.getError();
			}
			if (menus[2].isVisible() && connection.isConnected()){ //if the host is in purgatory and there's a client
				menus[2].goTo(menus[3]);
			}
		}

        if (selection != -1 && menus[4].isVisible()){ //drawing the actual game
            //Draw the game with the buttons on the side to play
			if (connection != null ? connection.isServer() : false){
				newGame.setVisible(true);
				//menuButton.setVisible(false);
				
			}
			else{
				again.setVisible(false);
			}
			debug.setVisible(true);
			
			//stop / go sign saying whos move it is
            g.setFont(new Font("Arial", Font.PLAIN, 15));
			if (games[selection].isReady() && games[selection].opponentReady() && !games[selection].isOver()){
				if (games[selection].isUsersMove()){
					g.setColor(new Color(0, 200, 0));
					fillOctogon(g, 100, 202, 85, 85, true);
					g.setColor(Color.green);
					fillOctogon(g, 100, 202, 75, 75, true);
					g.setColor(Color.darkGray);
					drawStackString(g, "Your Move", 100, 192, 85, true);
				}
				else{
					g.setColor(new Color(200, 0, 0));
					fillOctogon(g, 100, 202, 85, 85, true);
					g.setColor(Color.red);
					fillOctogon(g, 100, 202, 75, 75, true);
					g.setColor(Color.lightGray);
					drawStackString(g, "Opponent's Move", 100, 187, 85, true);
				}
			}
			
            games[selection].drawGame(g);
			
			g.setColor(Color.white);
			g.setFont(new Font("Arial", Font.PLAIN, 15));
            if (games[selection].isOver()){
				int winner = games[selection].winner();
				saveGame.setVisible(true);
				if (connection.isServer()) again.setVisible(true);
				String text = "";
				switch(winner){
					case 1:
						text = "You Win :)";
						break;
					case 2:
						text = "You Lost :(";
						break;
					default:
						text = "It's a tie";
				}
				g.drawString(text, 100 - g.getFontMetrics().stringWidth(text)/2, Game.YSIZE/2 + (int)(g.getFontMetrics().getHeight()*(1-22.0/59))/2);
			}
			if (!errorString.equals("")){
				g.setColor(Color.white);
				drawStackString(g, errorString, 15, 400, 170);
			}
        }
        else if (selection != -1 && menus[6].isVisible()){ //replay mode active replay session
            replays.draw(g);
			if (!errorString.equals("")){
				drawStackString(g, errorString, 15, 400, 170);
			}
        }
		else if (selection == -1 && menus[5].isVisible()){ //replay mode main menu
			g.setFont(new Font("Arial", Font.PLAIN, 30));
			String writing = "Saved Games: \n";
			String[] gamesString = read();
			replayMenuOffset = replayMenuScrollBar.getStep();
			try{
				for (int i=replayMenuOffset;i<Math.min(replayMenuOffset+15, gamesString.length);i++){
					writing += "-" + gamesString[i].split(";")[0] + " on " + gamesString[i].split(";")[1] + "\n";
				}
			}
			catch(Exception e){
				//something went wrong
				writing = "There are no saved games\nThat is... unless you messed with the save file";
			}
			if (gameChoice != -1 && gameChoice < gamesString.length){
				//draw a box around the game being chosen
				g.setColor(Color.green);
				drawThickRect(g, 80, 49 + (gameChoice-replayMenuOffset)*36, 682, 36, 5);
			}
			
			//draw all the saved games to the screen
			g.setColor(Color.white);
			drawStackString(g, writing, 80, 15, 690);
			
			//draw the bar
			//int barLength = (int)(460 * Math.min(15.0 / gamesString.length, 1.0));
			//g.setColor(Color.lightGray);
			//g.fillRect(765, 70+(int)(replayMenuOffset*((460.0 - barLength)/(gamesString.length-15))), 35, barLength);
		}
        else if (menus[3].isVisible()){ //draw the game selection panel
            /*again.setVisible(false);
			newGame.setVisible(false);
			debug.setVisible(false);
			saveGame.setVisible(false);
			menuButton.setVisible(true);*/
            //Draw icons for the user to choose which game to play
            int width = Game.XSIZE/3, length = Game.YSIZE/3;
            /*for (int i=0;i<gameSize;i++){
                games[i].drawIcon(g, 200+5 + (i%3)*width, 5 + (i/3)*length, width-10, length-10);
            }*/
            if (gameChoice != -1){
                games[gameChoice].drawIcon(g, 5, 395, 200, 200);
                g.setColor(Color.green);
                drawThickRect(g, 200+(gameChoice%3) * width, (gameChoice/3)*length, width, length, 4);

                int x=20,y=215;
                //keyboard
                g.setColor(new Color(255, 255, 255));
                g.fillRect(x, y+30, 3, 30);
                g.fillRect(x, y+60, 63, 3);
                g.fillRect(x+60, y+30, 3, 30);
                g.fillRect(x, y+30, 60, 3);
                g.drawLine(x, y+45, x+59, y+45);
                g.drawLine(x+15, y+30, x+15, y+59);
                g.drawLine(x+30, y+30, x+30, y+59);
                g.drawLine(x+45, y+30, x+45, y+59);
                g.drawArc(x, y+15, 30, 30, 0, 90);
                g.drawArc(x, y-15, 30, 30, 180, 90);
                if (!games[gameChoice].keyboardSupport()){
                    //cross out the keyboard
                    g.setColor(new Color(225, 15, 15));
                    g.drawOval(x, y+15, 60, 60);
                    g.drawLine(x+8, y+66, x+51, y+23);
                }

                x=130; y=240;
                //Mouse
                g.setColor(Color.white);
                g.drawArc(x, y, 30, 30, 0, 180);
                g.drawArc(x, y-10, 30, 50, 180, 180);
                g.drawLine(x, y+15, x+29, y+15);
                g.drawLine(x+15, y, x+15, y+15);
                if (!games[gameChoice].mouseSupport()){
                    //cross out the mouse
                    g.setColor(new Color(225, 15, 15));
                    g.drawOval(x-15, y-12, 60, 60);
                    g.drawLine(x+8-15, y+51-12, x+51-15, y+8-12);
                }
            }
        }
		else if (menus[0].isVisible()){ //drawing the main menu
			
			
			//disconnected error messages
			g.setFont(new Font("Arial", Font.PLAIN, 25));
			g.setColor(Color.lightGray);
			if (!errorString.equals("")){
				drawStackString(g, errorString, 400-g.getFontMetrics().stringWidth(errorString)/2, 400, 800);
			}
		}
		else if (menus[1].isVisible()){
			if (connection != null){
				if (!connection.isConnected()){
					
				}
				else if (connection.isConnected()){ //a client connected - only two player games so go to game selection screen
					menus[2].goTo(menus[3]);
					startGaming(); //there is a client
				}
				if (!errorString.equals("")){
					drawStackString(g, errorString, 250-g.getFontMetrics().stringWidth(errorString)/2, 415, 200);
				}
			}
		}
		for (Menu menu : menus){
			menu.draw(g);
		}
    }
    public synchronized void keyPressed(KeyEvent e){
		if (menus[0].isVisible()){
			//main menu keyboard traversal - cant pick up tab presses
			if (e.getKeyCode() == 37 || e.getKeyCode() == 39 || e.getKeyCode() == 65 || e.getKeyCode() == 68){ //left or right arrow presses or a or d button presses
				if (gameButton.getColor().equals(Color.lightGray)){
					gameButton.setColor(Color.darkGray);
					replayButton.setColor(Color.lightGray);
				}
				else{
					gameButton.setColor(Color.lightGray);
					replayButton.setColor(Color.darkGray);
				}
				gameButton.setTextColor();
				replayButton.setTextColor();
				repaint();
				return;
			}
			if (e.getKeyCode() == 10 || e.getKeyCode() == 32){ //enter or space keys pressed
				if (gameButton.getColor().equals(Color.lightGray)){
					toConnectionScreen();
				}
				else{
					startReplayMode();
				}
				repaint();
				return;
			}
		}
		if (menus[1].isVisible()){
			//Connection screen, deciding if host or join a game
			if (e.getKeyCode() == 37 || e.getKeyCode() == 65){ //left or a button press
				if (connectionScreenButton-- < 0) connectionScreenButton=1;
			}
			if (e.getKeyCode() == 39 || e.getKeyCode() == 68){ //right or d button press
				if (connectionScreenButton++ > 1) connectionScreenButton=0;
			}
			host.setColor(Color.darkGray);
			join.setColor(Color.darkGray);
			switch(connectionScreenButton){
				case 0:
					host.setColor(Color.lightGray);
					break;
				case 1:
					join.setColor(Color.lightGray);
					break;
			}
			host.setTextColor();
			join.setTextColor();
			if (e.getKeyCode() == 10 || e.getKeyCode() == 32){ //enter or space keys pressed
				if (host.getColor().equals(Color.lightGray)){
					hostLogic();
				}
				else if (join.getColor().equals(Color.lightGray)){
					if (ipReader.getText().trim().equals("IP:")){
						ipReader.grabFocus();
					}
					else{
						joinLogic(ipReader.getText());
						ipReader.setText("IP: ");
					}
				}
			}
		}
		if (selection == -1){
			if (e.getKeyCode() == 8){
				//from either replayModeMenu or gameSelectionMenu to the main menu
				//back-space key press
				backToMainMenu();
				repaint();
			}
            return;
        }
		if (e.getKeyCode() == 8){
			//from an active replay session or active game
			//back-space key press
			if (menus[6].isVisible()){
				selection = -1;
				replayRemoveSave.setPos(15, 145);
				replayRemoveSave.setVisible(false);
				repaint();
				return;
			}
		}
		if (menus[6].isVisible()){
			//selection != -1
			replays.press(e);
			repaint();
			return;
		}
		
        games[selection].keyPressed(e);
        repaint();
    }
    public synchronized void keyReleased(KeyEvent e){
        if (selection == -1){
            return;
        }
		if (menus[4].isVisible())
			games[selection].keyReleased(e);
        repaint();
		
    }
    public synchronized void keyTyped(KeyEvent e) {
        if (selection == -1){
            return;
        }
		if (menus[4].isVisible())
			games[selection].typed(e);
        repaint();
    }
    public synchronized void mouseClicked(MouseEvent e){
		errorString = "";
		if (menus[0].isVisible()){ //on the menu screen
			//do nothing, all buttons handled by the menu button checker thing
		}
		else if (menus[1].isVisible()){//on the connection screen - showing if you want to host or join an active session
			if (host.isClicked(e)){
				hostLogic();
			}
			if (join.isClicked(e)){
				joinLogic(ipReader.getText());
				ipReader.setText("IP: ");
			}
			if (menuButton.isClicked(e)){
				if (connection != null) connection.end();
				connection = null;
				//backToMainMenu();
				ipReader.setText("IP:");
			}
			repaint();
		}
		else if (menus[3].isVisible()){ //game selection panel
            if (e.getX() > 200){
                //in the game selection area
                int x = (e.getX()-200)/(Game.XSIZE/3), y = e.getY() / (Game.YSIZE/3);
                Game temp = games[x + y*3];
                if (temp != null && connection.isServer()){
                    gameChoice = x+y*3;
                    //this.drawIcon=temp;
                    chooseGame.setVisible(true);
                    cancel.setVisible(true);
					connection.send(new byte[] {4, 0, 0, (byte)gameChoice, -1, -1, -1, -1}); //only server has control over the game to play
					//System.out.println("debug statement:\n"+connection.toString()+"\n\n");
                }
            }
            if (chooseGame.click(e.getX(), e.getY())){
                if ((gameChoice >=0 && gameChoice < games.length) ? games[gameChoice] != null && !menus[6].isVisible(): false){
                    //set up the game and play it
                    chooseGameLogic(gameChoice, true);
                }
            }
            if (cancel.click(e.getX(), e.getY())){
                //dont draw the icon / button once back to the choose game screen
                chooseGame.setVisible(false);
                cancel.setVisible(false);
                gameChoice=-1;
				connection.send(new byte[] {4, 0, 0, -1, -1, -1, -1, -1});
            }
			if (menuButton.isClicked(e)){
				//backToMainMenu();
				disconnect();
			}
            repaint();
        }
		else if (menus[4].isVisible()){ //On a game
			if (newGame.click(e.getX(), e.getY()) && (connection != null ? connection.isServer() : false)){ //on a game
				newGameLogic(true);
				connection.send(new byte[] {4, 0, 1, -1, -1, -1, -1, -1});
			}
			if (again.click(e.getX(), e.getY()) && (connection != null ? connection.isServer() : false)){ //on a game
				againLogic(true);
				//connection.send(new byte[] {4, 1, 0, -1, -1, -1, -1, -1});
				again.setVisible(false);
			}
			if (menuButton.isClicked(e) && selection != -1 && !menus[6].isVisible()){ //on a game
				if (connection != null ? connection.isServer() : false){
					newGameLogic(true);
					connection.send(new byte[] {4, 0, 1, -1, -1, -1, -1, -1});
				}
				else{
					disconnect();
					//backToMainMenu();
				}
			}
			if (debug.click(e.getX(), e.getY()) && selection != -1){ //on a game
				games[selection].debug();
				repaint();
			}
			if (saveGame.click(e.getX(), e.getY())){ //on a game
				//this is where the code would write the moves of the game to a file for later
				if (selection != -1 ? games[selection] != null : false){
					//null protection
					save(games[selection].getMoves());
				}
			}
			if (!menus[6].isVisible() && selection != -1){ //on a game, transfer the click to the game to play
				games[selection].onClick(e);
				games[selection].winner();
				if (games[selection].isOver()){
					again.setVisible(true);
					saveGame.setVisible(true);
				}
			}
		}
		else if (replayRemoveSave.isClicked(e) && !(gameChoice != -1 ^ selection == -1) && (menus[5].isVisible() || menus[6].isVisible())){ //removes save from either replay screen or the replaying game's screen
			String[] games = read();
			String write = "";
			for (int i=0;i<games.length;i++){
				if (gameChoice != -1 && i != gameChoice){
					write += games[i]+"\n";
				}
				if (selection != -1 && i != selection){
					write += games[i]+"\n";
				}
			}
			if (games.length == 1){
				//removed the last game
				replayChooseGame.setVisible(false);
				replayRemoveSave.setVisible(false);
			}
			saveAll(write);
			selection = -1;
			gameChoice = -1;
		}
		else if (menus[5].isVisible()){ //replay main menu
			replayMenuOffset = replayMenuScrollBar.getStep();
			if (e.getX() > 75 && e.getY() > 24 && !read()[0].equals("")){
				//click on one the saved games
				int count = Math.max(Math.min((e.getY() - 80) / 36+replayMenuOffset, read().length), 0);
				if (count != read().length){
					gameChoice = count;
					replayChooseGame.setVisible(true);
					replayRemoveSave.setPos(15, 145);
					replayRemoveSave.setVisible(true);
				}
			}
			else if (replayChooseGame.isClicked(e)){
				selection = gameChoice;
				String moves = read(gameChoice);
				String[] movesName = moves.split(";");
				boolean found = false;
				for (int i=0;i<games.length;i++){
					if (games[i] == null) break;
					//System.out.println(movesName[0] + ", " + games[i].getClass().getSimpleName() + ", " + movesName[0].equals(games[i].getClass().getSimpleName()));
					if (movesName[0].equals(games[i].getClass().getSimpleName()) && games[i] instanceof Replayable){
						replays = new ReplayManager((Replayable)games[i], read(gameChoice));
						replays.setMoves(moves);
						replayRemoveSave.setVisible(true);
						replayRemoveSave.setPos(80, 15);
						gameChoice = -1;
						found = true;
						replays.init();
						break;
					}
				}
				if (!found){
					//game was not found
					selection = -1;
					repaint();
					return;
				}
			}
			repaint();
		}
		else if (menus[6].isVisible() && replays != null){ //in a replaying game
			replays.click(e);
			if (menuButton.isClicked(e) || replayRemoveSave.isClicked(e)){
				selection = -1;
				replayRemoveSave.setPos(15, 145);
				replayRemoveSave.setVisible(false);
				replayChooseGame.setVisible(false);
			}
			if (replayRemoveSave.isClicked(e)){
				String[] games = read();
				String write = "";
				for (int i=0;i<games.length;i++){
					if (i != selection){
						write += games[i];
					}
				}
				saveAll(write);
			}
		}
		for (Menu menu : menus){
			if (menu.click(e)) {break;} //did a click
		}
        repaint();
    }
    public synchronized void mouseEntered(MouseEvent e){
        if (selection == -1 || menus[6].isVisible() || menus[5].isVisible()){
            return;
        }
        games[selection].mouseEntered(e);
        repaint();
        //mouse moves over window, window does not need to be on top
    }
    public synchronized void mouseExited(MouseEvent e){
        if (selection == -1 || menus[6].isVisible() || menus[5].isVisible()){
            return;
        }
        games[selection].mouseExited(e);
        repaint();
        //mouse exits the window
    }
    public synchronized void mousePressed(MouseEvent e){
        if (selection == -1 || menus[6].isVisible() || menus[5].isVisible()){
            return;
        }
        games[selection].mousePressed(e);
        repaint();
        //a button was pressed
    }
    public synchronized void mouseReleased(MouseEvent e){
        if (selection == -1 || menus[6].isVisible() || menus[5].isVisible()){
            return;
        }
        games[selection].mouseReleased(e);
        repaint();
        //a button was released
    }
	public synchronized void mouseWheelMoved(MouseWheelEvent e) {
		for (Menu menu : menus){
			menu.click(e);
		}
		repaint();
	}
	public synchronized void windowClosing(WindowEvent e){
		//System.out.println("Closing");
		disconnect();
	}
	public synchronized void windowDeactivated(WindowEvent e){
		//System.out.println("lost focus");
	}
	public synchronized void windowActivated(WindowEvent e){
		//System.out.println("Focused");
	}
	public synchronized void windowClosed(WindowEvent e){
		//System.out.println("Closed");
	}
	public synchronized void windowDeiconified(WindowEvent e){
		//System.out.println("Bigged");
	}
	public synchronized void windowIconified(WindowEvent e){
		//System.out.println("minimized");
	}
	public synchronized void windowOpened(WindowEvent e){
		//System.out.println("Opened");
	}
	public synchronized void actionPerformed(ActionEvent e){
		if (e.getSource() == ipReader){
			joinLogic(ipReader.getText());
			ipReader.setText("IP:");
		}
	}
	public void save(String lineToAdd){
		String write = "";
		try {
            Scanner scan = new Scanner(new FileReader("savedGames.txt"));
            //reads one line at a time
			int line=0;
            while (scan.hasNextLine()){
                write += scan.nextLine()+"\n";
				line++;
            }
        }
        catch (FileNotFoundException e) {
			errorString = "Something went wrong while saving the game";
        }
		
		write += lineToAdd + "\n";
		
		try (FileWriter writer = new FileWriter("savedGames.txt")){ //will always exit with closing the file
            writer.write(write);
        } catch (Exception e) {
			errorString = "Something went wrong while saving the game";
        }
	}
	private void saveAll(String linesToAdd){ //will completely replace the saved games file with the string inputted
		try (FileWriter writer = new FileWriter("savedGames.txt")){ //will always exit with closing the file
            writer.write(linesToAdd);
        } catch (Exception e) {
			errorString = "Something went wrong while saving the game";
        }
		replayMenuScrollBar.setSize(read().length-14);
	}
	private String[] read(){
		String send = "";
		try {
            Scanner scan = new Scanner(new FileReader("savedGames.txt"));

            int line=0;
            //reads one line at a time
            while (scan.hasNextLine()){
                line++;
                send += scan.nextLine() + "\n";
            }
        }
        catch (FileNotFoundException e) {
            errorString = "Something went wrong while reading save files";
			return new String[] {""}; //effectively empty while not throwing stupid null errors
        }
		return send.split("\n");
	}
	private String read(int index){
		String send = "";
		try {
            Scanner scan = new Scanner(new FileReader("savedGames.txt"));

            int line=0;
            //reads one line at a time
            while (scan.hasNextLine()){
                line++;
				if (line -1 == index){
					send = scan.nextLine();
					return send;
				}
				scan.nextLine();
            }
        }
        catch (FileNotFoundException e) {
            errorString = "Something went wrong while reading save files";
			return "";
        }
		System.out.println(send);
		return send;
	}
	@Deprecated
	private void startReplayMode(){
		menus[0].goTo(menus[5]);
		menus[6].setVisible(false);
	}
	private void startGaming(){
		menus[2].goTo(menus[3]);
		menus[1].setVisible(false);
	}
	private void backToMainMenu(){
		for (Menu menu : menus){
			menu.setVisible(false);
		}
		menus[0].setVisible(true);
		
		gameChoice = -1;
		selection = -1;
	}
	@Deprecated
	private void toConnectionScreen(){
		menus[0].goTo(menus[1]);
	}
	private void connect(boolean isServer){
		connection = new ConnectionHandler(ipReader.getText(), isServer);
		for (Game game : games){
			if (game != null){
				game.setConnectionHandler(connection);
			}
		}
	}
	private void disconnect(){
		if (connection == null) return; //doesn't exist
		//System.out.println("disconnect");
		byte[] nums = new byte[8];
		for (int i=0;i<nums.length;i++){
			nums[i] = Byte.MAX_VALUE;
		}
		connection.send(nums); // kick array
		
		connection.end();
		connection = null;
		for (Game game : games){
			if (game != null){
				game.setConnectionHandler(null);
			}
			if (game == null){
				break;
			}
		}
	}
	private void chooseGameLogic(int gameChoice, boolean send){
		if (gameChoice >=0 && gameChoice < gameSize){
			games[gameChoice].reset();
		}
		
		selection = gameChoice;
		if (menus[5].isVisible() && gameChoice != -1 && games[gameChoice] instanceof Replayable){
			replays = new ReplayManager((Replayable)games[gameChoice], read(gameChoice));
			/*menuButton.setVisible(false);*/
			menus[5].goTo(menus[6]); //replay menu to replay game
		}
		else if (gameChoice != -1 && menus[3].isVisible()){
			games[gameChoice].setConnectionHandler(connection);
			games[gameChoice].setUsersMove(connection.isServer());
			menus[3].goTo(menus[4]); //game selction to actual game
			if (send) 
				connection.send(new byte[] {4, 0, 1, (byte)gameChoice, -1, -1, -1, -1});
			cancel.setVisible(false);
			chooseGame.setVisible(false);
		}
		else if (menus[4].isVisible() && gameChoice == -1){
			if (!connection.isServer()) menus[4].goTo(menus[3]);
			again.setVisible(false);
			saveGame.setVisible(false);
		}
		//dont draw the icon / button once back to the choose game screen
		//chooseGame.setVisible(false);
		//cancel.setVisible(false);
		this.gameChoice=-1;
	}
	private void againLogic(boolean send){
		System.out.println("AGAIN LOGIC");
		games[selection].reset();
		games[selection].setUsersMove(connection.isServer());
		saveGame.setVisible(false);
		again.setVisible(false);
		if (send) 
			connection.send(new byte[] {4, 1, 0, -1, -1, -1, -1, -1});
		repaint();
	}
	private void newGameLogic(boolean send){
		chooseGameLogic(-1, send);
	}
	private void hostLogic(){
		menus[1].goTo(menus[2]);
		connection = new ConnectionHandler("127.0.0.1", true);
		for (Game game : games){
			if (game == null) continue;
			game.setConnectionHandler(connection);
		}
		again.setVisible(false);
		newGame.setVisible(false);
		serverAddress.setText("IP: " + connection.getServerAddress());
	}
	private void joinLogic(String ip){
		try{
			connection = new ConnectionHandler(ip, false);
			
			for (Game game : games){
				if (game == null) break;
				game.setConnectionHandler(connection);
			}
			startGaming();
		}
		catch (Exception execpt){
			errorString = "Ip not found";
			backToMainMenu();
		}
	}
	public void animate(){
        while(true){
            try{
                Thread.sleep(50);
            }
            catch(InterruptedException ex){
                Thread.currentThread().interrupt();
            }
            repaint();
        }
    }
}