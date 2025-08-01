package games;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
//for data/time things
import java.util.Date;
import helpful.GUI.Component;
import helpful.Rectangle;

public interface Game extends Component{
	final static int XSIZE = 600;
	final static int YSIZE = 600;
	final static int XSTART = 200;
	public void drawGame(Graphics g); //draws the board
	public void onClick(MouseEvent e); // mouse button clicked
	public void mousePressed(MouseEvent e); // mouse button pressed
	public void mouseReleased(MouseEvent e); // mouse button relased
	public void mouseEntered(MouseEvent e); // mouse entered screen
	public void mouseExited(MouseEvent e); // mouse exited screen
	public void keyPressed(KeyEvent e); // key is pressed
	public void keyReleased(KeyEvent e); //key is relased
	public void typed(KeyEvent e); //key is typed
	public boolean isOver(); //returns true if the game is over
	public int winner(); //returns who the winner is - -1=no one - 1=player 1 - 2=player 2
	public String getMoves(); //returns a list of moves of the current game
	public void reset(); //resets all the game states
	public void reset(String boardPreSet); //resets all the variables and sets the board to the passed in string, useful for replayManager
	public void handleMessage(byte[] message); //will be given any message that starts with 5 (or 0 if the standardMove method returns false) while removing it from ConnectionHandler's buffer - includes the 5
	public boolean move(int x, int y, int x2, int y2, boolean override, boolean usersTurn); //The logic of what should happen given a move on this location, the override is for the sockets and replay manager use, will circumvent the checks and blindly trusts it
	public boolean keyboardSupport(); //returns true if the code is designed to work with keyboard inputs
	public boolean mouseSupport(); //returns true if the code is designed to work with mouse inputs
	public int getBoardWidth(); //returns the width of the board for replayManager's use
	public int getBoardHeight(); //returns the height of the board for replayerManager's use
	//@Deprecated
	public String getBoardAt(int x, int y); //returns the value at a specific part of the board
	//@Deprecated
	public boolean setBoardAt(int x, int y, String item); //sets the value of the board at a certain spot to a specified value, returns weather successful
	public void setConnectionHandler(ConnectionHandler cH); //sets the connection handler that deal with sending / recieving messages
	public byte getGameID(); //holds the id of the game, each game implementing this should have a unique id
	public String getGameName(); //returns the name of the game being played
	public int[] version(); //returns the version number of the game standardized to be similar to MainRelease,VersionRelease,BugFix similar to how minecraft updates its versions
	public void setUsersMove(boolean move); //sets if the user to supposed to move or not
	public boolean isUsersMove(); //returns true if it's the users move next
	public String getBoardPreGame(); //returns the initial board - useful for replay mode on secrective games
	public boolean isReady(); //for the screen to display whos turn it is - useful for the secrective games
	public boolean opponentReady(); //for the screen to display if the opponent is ready for the game
	public void getInfo(byte[] args); //for the secretive games and any others for general info regarding the args, sends the info to their opponenent in method
	public String parseBoard(byte[] board); //parses an array of bytes into board positions, good for replaymanager of secretive games
	public int[][] getValidMoves(boolean usersMove); //returns all the valid moves of the current board, the second dimension contains [x1, y1, x2, y2]
	public void drawMoves(Graphics g, int[][] possMoves); //draws all the possible moves passed in
	default boolean isStandardMove(){
		return true;
	}
	default int wrap(int num, int top, int bottom){
		if (top == bottom) return num; //stack over flow protection and div by 0 protection
		//includes bottom, excludes top
		if (num < top && num >= bottom){
			return num;
		}
		if (num < bottom){
			return wrap(num+top-bottom, top, bottom);
		}
		if (num >= top){
			return ((num-top)%(top-bottom))+bottom;
		}
		return num;
	}
	default void drawStackString(Graphics g, String text, int x, int y, int width){
        helpful.Drawer.drawStackString(g, text, x, y, width);
    }
	default void drawIcon(Graphics g, int x, int y, int xSize, int ySize){ //draws the icon while selecting the game
		g.setColor(Color.white);
		g.drawRect(x, y, xSize, ySize);
	}
	default void debug(){
		System.out.println("debug for " + this.getClass().getSimpleName());
	}
	default String getDate(){
		String[] days= (new Date(System.currentTimeMillis())).toString().split(" ");
        String send = days[1] + " " + days[2] + " " + days[3] + " " + days[5];
        return send;
	}
	default Rectangle toRect(){
		return new Rectangle(0, 0, XSIZE/3, YSIZE/3);
	}
}