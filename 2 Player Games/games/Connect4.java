package games;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Font;
import helpful.Rectangle;
import helpful.datastructures.DLList;
import helpful.GUI.Text;
import helpful.Scalable;

public class Connect4 implements Game, Replayable{
	private int[][] board;
	private int[] heights;
	private int move;
	private String moves;
	private String winRow;
	private int[] keyboard; //saves the location of the keyboard inputs
	private boolean keyListen;
	private ConnectionHandler cH;
	//Component Variables
	private boolean isVisible;
	private int x, y;
	public Connect4(int index){
		reset();
		isVisible = true;
		x = (index%3)*XSIZE/3;
		y = (index/3)*YSIZE/3;
	}
	public void drawGame(Graphics g){ //draws the board
		int xStart = 200;
		g.setColor(Color.blue);
		g.fillRect(xStart, 0, XSIZE, YSIZE);
		int count =0;
		boolean startFind=false;
		for (int i=0;i<board.length;i++){
			for (int j=0;j<board[i].length;j++){
				if (!winRow.equals("")){
					//win detected
					int row = Integer.parseInt(winRow.charAt(1)+"");
					int start = Integer.parseInt(winRow.charAt(2)+"");
					if (i==row && j==start && winRow.charAt(0)=='d'){
						startFind=true;
					}
					//Vertical
					if (i == row && j >= start && count< 4 && winRow.charAt(0) == 'V'){
						g.setColor(Color.green);
						count++;
					}
					//Horizontal
					else if (j==row && i>=start && count<4 && winRow.charAt(0) == 'H'){
						g.setColor(Color.green);
						count++;
					}
					//top left to bottom right
					else if (i>=row && j>=start &&i-row==j-start && count<4 && winRow.charAt(0)=='D'){
						g.setColor(Color.green);
						count++;
					}
					//bottom left to top right
					else if (i+j == row+start && count<4 && winRow.charAt(0)=='d' && startFind){
						g.setColor(Color.green);
						count++;
					}
					else{
						g.setColor(Color.blue);
					}
					g.fillRect(xStart + i*XSIZE/7, j*YSIZE/6, XSIZE/7, YSIZE/6);
				}
				else if (keyListen && i==keyboard[0] && j == (5-heights[keyboard[0]])){
					//win not detected and using the keys as input
					g.setColor(Color.magenta);
					g.fillRect(xStart + keyboard[0]*XSIZE/7, (5-heights[keyboard[0]])*YSIZE/6, XSIZE/7, YSIZE/6);
				}
				g.setColor(Color.black);
				if (board[i][j] == 1){
					g.setColor(Color.red);
				}
				else if (board[i][j] == 2){
					g.setColor(Color.yellow);
				}
				g.fillOval(xStart + i*XSIZE/7+5, j*YSIZE/6+5, XSIZE/7-10, YSIZE/6-10);
				
			}
			Text item = new Text(200+i*(XSIZE/7), YSIZE+2, XSIZE/7-6, 27, i+"", Text.CENTERED);
			item.setColors(new Color(200, 175, 25), Color.blue);
			item.draw(g);
			if (i < 6){
				item.setDims(202+XSIZE, 3+i*(YSIZE/6), 27, YSIZE/6-6);
				item.draw(g);
			}
		}
	}
	public void drawGame(Scalable g){ //draws the board
		int xStart = 200;
		g.setColor(Color.blue);
		g.fillRect(xStart, 0, XSIZE, YSIZE);
		int count =0;
		boolean startFind=false;
		for (int i=0;i<board.length;i++){
			for (int j=0;j<board[i].length;j++){
				if (!winRow.equals("")){
					//win detected
					int row = Integer.parseInt(winRow.charAt(1)+"");
					int start = Integer.parseInt(winRow.charAt(2)+"");
					if (i==row && j==start && winRow.charAt(0)=='d'){
						startFind=true;
					}
					//Vertical
					if (i == row && j >= start && count< 4 && winRow.charAt(0) == 'V'){
						g.setColor(Color.green);
						count++;
					}
					//Horizontal
					else if (j==row && i>=start && count<4 && winRow.charAt(0) == 'H'){
						g.setColor(Color.green);
						count++;
					}
					//top left to bottom right
					else if (i>=row && j>=start &&i-row==j-start && count<4 && winRow.charAt(0)=='D'){
						g.setColor(Color.green);
						count++;
					}
					//bottom left to top right
					else if (i+j == row+start && count<4 && winRow.charAt(0)=='d' && startFind){
						g.setColor(Color.green);
						count++;
					}
					else{
						g.setColor(Color.blue);
					}
					g.fillRect(xStart + i*XSIZE/7, j*YSIZE/6, XSIZE/7, YSIZE/6);
				}
				else if (keyListen && i==keyboard[0] && j == (5-heights[keyboard[0]])){
					//win not detected and using the keys as input
					g.setColor(Color.magenta);
					g.fillRect(xStart + keyboard[0]*XSIZE/7, (5-heights[keyboard[0]])*YSIZE/6, XSIZE/7, YSIZE/6);
				}
				g.setColor(Color.black);
				if (board[i][j] == 1){
					g.setColor(Color.red);
				}
				else if (board[i][j] == 2){
					g.setColor(Color.yellow);
				}
				g.fillOval(xStart + i*XSIZE/7+5, j*YSIZE/6+5, XSIZE/7-10, YSIZE/6-10);
				
			}
			Text item = new Text(200+i*(XSIZE/7), YSIZE+2, XSIZE/7-6, 27, i+"", Text.CENTERED);
			item.setColors(new Color(200, 175, 25), Color.blue);
			item.draw(g);
			if (i < 6){
				item.setDims(202+XSIZE, 3+i*(YSIZE/6), 27, YSIZE/6-6);
				item.draw(g);
			}
		}
	}
	public void drawIcon(Graphics g, int x, int y, int width, int length){ //draws the icon while selecting the game
		g.setColor(Color.black);
		g.fillRect(x, y, width, length);
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.setColor(Color.white);
		drawStackString(g, "Connect 4", x+5, y+5, width);
		g.drawRect(x, y, width, length);
		//Draw a basic 2 piece board
		g.setColor(Color.blue);
		g.fillRect(x+2,y+length/2,width-4,length/2-2);
		//Draw the two pieces
		g.setColor(Color.red);
		g.fillOval(x+2, y+length/2, width/2-2, length/2-2);
		g.setColor(Color.yellow);
		g.fillOval(x+2 + width/2+2, y+length/2, width/2-4, length/2-2);
	}
	public void drawIcon(Scalable g, int x, int y, int width, int length){ //draws the icon while selecting the game
		g.setColor(Color.black);
		g.fillRect(x, y, width, length);
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.setColor(Color.white);
		drawStackString(g, "Connect 4", x+5, y+5, width);
		g.drawRect(x, y, width, length);
		//Draw a basic 2 piece board
		g.setColor(Color.blue);
		g.fillRect(x+2,y+length/2,width-4,length/2-2);
		//Draw the two pieces
		g.setColor(Color.red);
		g.fillOval(x+2, y+length/2, width/2-2, length/2-2);
		g.setColor(Color.yellow);
		g.fillOval(x+2 + width/2+2, y+length/2, width/2-4, length/2-2);
	}
	public boolean move(int x, int y, int x2, int y2, boolean override, boolean usersTurn){ //The logic of what should happen given a move on this location
		//override has no meaningful affect on the move function
		if (!(0<=x && x<7)){
			return false; //outside the bounds of the board
		}
		if (board[x][0] != 0){ //that column is full
			return false;
		}
		if (isOver()){ //the game is over, no more moves
			return false; 
		}
		if ((usersTurn ? move != 0 : move!=1) && !override){  //cant move if its not your turn
			return false;
		}
		if (cH == null && !override) return false; //not replay mode and no connection
		if (!override) y=5-heights[x];//find height of current column if normal gameplay
		
		
		heights[x]++; //increase the height on that column
		board[x][y] = usersTurn ? 1:2; //set move

		if (usersTurn) cH.send(new byte[] {0, (byte)x, -1, -1, -1, 0, -1, this.getGameID()});
		move = usersTurn ? 1 : 0;
		
		moves += x + ":" + y + ";"; //add to the current move list
		winner(); //is there a winner?
		return true;
	}
	public void onClick(MouseEvent e){ // mouse button clicked
		if (!keyListen && this.move==0){
			boolean result = move((e.getX()-200)/(XSIZE/7), 0, 0, 0, false, true);
			if (!result) return; //move failed, return
		}else{
			keyListen=false;
			keyboard = new int[2];
		}
	}
	public void mousePressed(MouseEvent e){} // mouse button pressed
	public void mouseReleased(MouseEvent e){} // mouse button relased
	public void mouseEntered(MouseEvent e){} // mouse entered screen
	public void mouseExited(MouseEvent e){} // mouse exited screen
	public void keyPressed(KeyEvent e){ // key is pressed
		if (keyListen && !isOver()){
			int code = e.getKeyCode();
			if (code == 65 || code == 37){ //left a or left arrow
				do {
					keyboard[0] = wrap(keyboard[0]-1, 7, 0);
				}while (heights[keyboard[0]] >= 5 && !isOver());
			}
			else if (code == 68 || code == 39){ // right d or right arrow
				do {
					keyboard[0] = wrap(keyboard[0]+1, 7, 0);
				}while (heights[keyboard[0]] >= 5 && !isOver());
			}
			else if ((code == 10 || code == 32) && move == 0){ // submit move - enter button or space
				boolean result = move(keyboard[0], keyboard[1], 0, 0, false, true);
				if (!result) return; //move failed, return
			}
		}
		else{
			keyListen = true;
			keyboard = new int[] {3,2};
		}
	}
	public void keyReleased(KeyEvent e){} //key is relased
	public void typed(KeyEvent e){} //key is typed
	public boolean isOver(){ //returns true if a player has won or the board is full
		boolean empty=false;
		for (int i : heights){
			if (i!=5){
				//there is a space left to place on some row
				empty=true;
			}
		}
		return winner() != -1 || !empty;
	}
	public int winner(){ //returns who the winner is - -1=no one - 1=player 1 - 2=player 2
		winRow = "";
		//Do vertical checks
		for (int i=0;i<7;i++){
			for (int j=0;j<3;j++){
				if (board[i][j] == board[i][j+1] && board[i][j+1] == board[i][j+2] && board[i][j+2] == board[i][j+3] && board[i][j] != 0){
					winRow="V" + i + j;
					return board[i][j];
				}
			}
		}
		//Do Horizontal checks
		for (int j=0;j<6;j++){
			for (int i=0;i<4;i++){
				if (board[i][j] == board[i+1][j] && board[i+1][j] == board[i+2][j] && board[i+2][j] == board[i+3][j] && board[i][j] != 0){
					winRow="H"+j+i;
					return board[i][j];
				}
			}
		}
		//Do Diagonol checks
		for (int i=0;i<4;i++){
			for (int j=0;j<3;j++){
				if (board[i][j] == board[i+1][j+1] && board[i+1][j+1] == board[i+2][j+2] && board[i+2][j+2] == board[i+3][j+3] && board[i][j] != 0){
					//top left to bottom right
					winRow="D"+i+j;
					return board[i][j];
				}
				if (board[i][j+3] == board[i+1][j+2] && board[i+1][j+2] == board[i+2][j+1] && board[i+2][j+1] == board[i+3][j] && board[i][j+3] != 0){
					//bottom left to top right
					winRow="d"+i+(j+3);
					return board[i][j+3];
				}
			}
		}
		return -1;
	}
	public String getMoves(){ //returns a list of moves of the current game
		return moves;
	}
	public void reset(){ //resets all the game states
		board = new int[7][6];
		move=0;
		moves = "Connect4;" + this.getDate() + ";";
		winRow="";
		keyboard=new int[2];
		keyListen=false;
		heights = new int[7];
	}
	public void reset(String boardPreSet){
		//there is no different board set up
		reset();
	}
	public void handleMessage(byte[] nums){
		System.out.println("ERROR: C4:hM");
	}
	public boolean keyboardSupport(){
		return true;
	}
	public boolean mouseSupport(){
		return true;
	}
	public int getBoardWidth(){ //returns the width of the board for replayManager's use
		return 7;
	}
	public int getBoardHeight(){ //returns the height of the board for replayerManager's use
		return 6;
	}
	public String getBoardAt(int x, int y){ //returns the value at a specific part of the board
		return board[x][y] + "";
	}
	public boolean setBoardAt(int x, int y, String item){ //sets the value of the board at a certain spot to a specified value, returns weather successful
		if (x< 0 || x>=7 || y<0 || y>=6){
			//out of bounds
			return false;
		}
		board[x][y] = Integer.parseInt(item);
		return true;
	}
	public void setConnectionHandler(ConnectionHandler cH){
		this.cH = cH;
	}
	public byte getGameID(){
		return 1;
	}
	public String getGameName(){
		return "Connect4";
	}
	public int[] version(){
		return new int[] {1,0,0};
	}
	public void setUsersMove(boolean move){
		if (move){ 
			this.move = 0;
		}
		else{
			this.move = 1;
		}
	}
	public boolean isUsersMove(){
		return this.move == 0;
	}
	public String getBoardPreGame(){
		return "";
	}
	public boolean isReady(){
		return true;
	}
	public boolean opponentReady(){
		return true;
	}
	public void getInfo(byte[] array){
		if (array == null || cH == null) return;
		if (array.length < 2 || !cH.isConnected()) return;
		cH.send(new byte[] {5, 0, (byte)board[array[0]][array[1]], -1, -1, -1, -1, -1});
	}
	public String parseBoard(byte[] array){ //not secretive - not needed
		return "";
	}
	public int[][] getValidMoves(boolean usersMove){ //returns all the valid moves of the current board, the second dimension contains [x1, y1, x2, y2]
		DLList<int[]> moves = new DLList<int[]>();
		
		for (int x=0;x<heights.length;x++){
			if (heights[x] < 6) moves.add(new int[] {x, heights[x], -1, -1});
		}
		
		int[][] send = new int[moves.size()][moves.get(0).length];
		int i=0;
		for (int[] item : moves){
			send[i] = item;
			i++;
		}
		return send;
	}
	public void drawMoves(Graphics g, int[][] possMoves){ //draws all the possible moves passed in
		g.setColor(new Color(25, 225, 25, 196));
		for (int[] item : possMoves){
			g.fillRect(XSTART + item[0]*XSIZE/7, (5-item[1])*YSIZE/6, XSIZE/7, YSIZE/6);
		}
	}
	
	//Replayable methods - taken from replayManager from old code, so, roundabout in execution
	public String goForwards(int cursor, String[] moves) throws NumberFormatException{
		String[] tempMoves = moves[cursor].split(":");
		int x = Integer.parseInt(tempMoves[0]),
			y = Integer.parseInt(tempMoves[1]);
		this.move(x,y,0,0,true,cursor%2==0);
		return "Move at (" + x + ", " + y + ")";
	}
	public String goBack(int cursor, String[] moves) throws NumberFormatException{
		String[] tempMoves = moves[cursor].split(":");
		int x = Integer.parseInt(tempMoves[0]),
			y = Integer.parseInt(tempMoves[1]);
		this.setBoardAt(x,y,"0");
		return "Undoing (" + x + ", " + y + ")";
	}
	public boolean canGoForward(int cursor, String[] moves){
		return cursor < moves.length-1;
	}
	public boolean canGoBack(int cursor, String[] moves){
		return cursor > 2;
	}
	
	
	//Component methods
	public void setVisible(boolean isVisible){
		this.isVisible = isVisible;
	}
	public void draw(Graphics g){
		drawIcon(g, 200+5+x, 5+y, XSIZE/3-10, YSIZE/3-10);
	}
	public Rectangle toRect(){
		return new Rectangle(200+5+x, 5+y, XSIZE/3-10, YSIZE/3-10);
	}
	public void setPos(int x, int y){
		this.x = x;
		this.y = y;
	}
}