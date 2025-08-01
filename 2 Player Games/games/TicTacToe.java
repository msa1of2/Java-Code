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

public class TicTacToe implements Game, Replayable{
	private int[][] board;
	private boolean isUsersMove;
	private String moves;
	private String winRow;
	private int[] keyboard; //saves the location of the keyboard inputs
	private boolean keyListen;
	private ConnectionHandler cH;
	private int move;
	//Component Variables
	private boolean isVisible;
	private int x, y;
	public TicTacToe(int index){
		reset();
		isVisible = true;
		x = (index%3)*XSIZE/3;
		y = (index/3)*YSIZE/3;
	}
	public void drawIcon(Graphics g, int x, int y, int width, int length){ //draws the icon while selecting the game
		g.setColor(Color.black);
		g.fillRect(x, y, width, length);
		
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.setColor(Color.white);
		drawStackString(g, "Tic-Tac-Toe", x+5, y+5, width);
		g.drawRect(x, y, width, length);
		//Draw a basic board
		g.setColor(Color.lightGray);
		g.fillRect(x+3, y+length/2, width/2-4, length/2-2);
		g.fillRect(x + width/2+2, y+length/2, width/2-4, length/2-2);
		//Draw an x
		g.setColor(Color.red);
		g.drawLine(x+3, y+length/2, x+width/2-2, y+length-2);
		g.drawLine(x+3, y+length-2, x+width/2-2, y+length/2);
		//draw a circle
		g.setColor(Color.blue);
		g.drawOval(x+width/2+2, y+length/2, width/2-4, length/2-2);
	}
	public void drawIcon(Scalable g, int x, int y, int width, int length){ //draws the icon while selecting the game
		g.setColor(Color.black);
		g.fillRect(x, y, width, length);
		
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.setColor(Color.white);
		drawStackString(g, "Tic-Tac-Toe", x+5, y+5, width);
		g.drawRect(x, y, width, length);
		//Draw a basic board
		g.setColor(Color.lightGray);
		g.fillRect(x+3, y+length/2, width/2-4, length/2-2);
		g.fillRect(x + width/2+2, y+length/2, width/2-4, length/2-2);
		//Draw an x
		g.setColor(Color.red);
		g.drawLine(x+3, y+length/2, x+width/2-2, y+length-2);
		g.drawLine(x+3, y+length-2, x+width/2-2, y+length/2);
		//draw a circle
		g.setColor(Color.blue);
		g.drawOval(x+width/2+2, y+length/2, width/2-4, length/2-2);
	}
	public void drawGame(Graphics g){ //draws the board
		//g.fillRect(xStart, 0, XSIZE, YSIZE);
		for (int i=0;i<3;i++){
			for (int j=0;j<3;j++){
				if (!winRow.equals("")){
					//there is a win detected
					int row = Integer.parseInt(winRow.charAt(1)+"");
					if (row == i && winRow.charAt(0) == 'V'){
						g.setColor(Color.green);
					}
					else if (row == j && winRow.charAt(0) == 'H'){
						g.setColor(Color.green);
					}
					else if ((i==j && winRow.equals("D1")) || (i+j==2 && winRow.equals("D2"))){
						g.setColor(Color.green);
					}
					else{
						g.setColor(Color.lightGray);
					}
				}
				else{
					if (keyboard[0] == i && keyboard[1] == j && keyListen){
						g.setColor(Color.magenta);
					}
					else{
						///no win detected
						g.setColor(Color.lightGray);
					}
				}
				Color bgColor = g.getColor();
				
				g.fillRect(i * (XSIZE/3) +203, j * (YSIZE/3) +3, (XSIZE/3-6), (YSIZE/3-6));
				g.setColor(Color.black);
				g.drawRect(i * (XSIZE/3) +203, j * (YSIZE/3) +3, (XSIZE/3-6), (YSIZE/3-6));
				if (board[i][j] == 1){
					g.setColor(Color.blue);
					int[] xPoints = new int[] {i*(XSIZE/3)+206, i*(XSIZE/3)+203, (i+1)*(XSIZE/3)+194, (i+1)*(XSIZE/3)+197};
					int[] yPoints = new int[] {j*(YSIZE/3)+3, j*(YSIZE/3)+6, (j+1)*(YSIZE/3)-3, (j+1)*(YSIZE/3)-6};
					g.fillPolygon(xPoints, yPoints, xPoints.length);
					yPoints = new int[] {(j+1)*(YSIZE/3)-3, (j+1)*(YSIZE/3)-6, j*(YSIZE/3)+3, j*(YSIZE/3)+6};
					g.fillPolygon(xPoints, yPoints, xPoints.length);
				}
				if (board[i][j] == 2){
					g.setColor(Color.red);
					g.fillOval(i*(XSIZE/3)+206, j * (YSIZE/3) +6, (XSIZE/3-12), (YSIZE/3-12));
					g.setColor(bgColor);
					g.fillOval(i*(XSIZE/3)+(XSIZE/24)+200, j*(YSIZE/3)+(YSIZE/24), (XSIZE/12+YSIZE/6), (YSIZE/12+YSIZE/6));
				}
			}
			//Draw the x,y coords on the sides
			Text item = new Text(203+i*(XSIZE/3), YSIZE, XSIZE/3-6, 27, i+"", Text.CENTERED);
			item.setColors(new Color(200, 175, 25), Color.blue);
			item.draw(g);
			item.setDims(200+XSIZE, 3+i*(YSIZE/3), 27, YSIZE/3-6);
			item.draw(g);
		}
		
		//check for moves from the opponent
		
	}
	public void drawGame(Scalable g){ //draws the board
		//g.fillRect(xStart, 0, XSIZE, YSIZE);
		for (int i=0;i<3;i++){
			for (int j=0;j<3;j++){
				if (!winRow.equals("")){
					//there is a win detected
					int row = Integer.parseInt(winRow.charAt(1)+"");
					if (row == i && winRow.charAt(0) == 'V'){
						g.setColor(Color.green);
					}
					else if (row == j && winRow.charAt(0) == 'H'){
						g.setColor(Color.green);
					}
					else if ((i==j && winRow.equals("D1")) || (i+j==2 && winRow.equals("D2"))){
						g.setColor(Color.green);
					}
					else{
						g.setColor(Color.lightGray);
					}
				}
				else{
					if (keyboard[0] == i && keyboard[1] == j && keyListen){
						g.setColor(Color.magenta);
					}
					else{
						///no win detected
						g.setColor(Color.lightGray);
					}
				}
				Color bgColor = g.getColor();
				
				g.fillRect(i * (XSIZE/3) +203, j * (YSIZE/3) +3, (XSIZE/3-6), (YSIZE/3-6));
				g.setColor(Color.black);
				g.drawRect(i * (XSIZE/3) +203, j * (YSIZE/3) +3, (XSIZE/3-6), (YSIZE/3-6));
				if (board[i][j] == 1){
					g.setColor(Color.blue);
					int[] xPoints = new int[] {i*(XSIZE/3)+206, i*(XSIZE/3)+203, (i+1)*(XSIZE/3)+194, (i+1)*(XSIZE/3)+197};
					int[] yPoints = new int[] {j*(YSIZE/3)+3, j*(YSIZE/3)+6, (j+1)*(YSIZE/3)-3, (j+1)*(YSIZE/3)-6};
					g.fillPolygon(xPoints, yPoints, xPoints.length);
					yPoints = new int[] {(j+1)*(YSIZE/3)-3, (j+1)*(YSIZE/3)-6, j*(YSIZE/3)+3, j*(YSIZE/3)+6};
					g.fillPolygon(xPoints, yPoints, xPoints.length);
				}
				if (board[i][j] == 2){
					g.setColor(Color.red);
					g.fillOval(i*(XSIZE/3)+206, j * (YSIZE/3) +6, (XSIZE/3-12), (YSIZE/3-12));
					g.setColor(bgColor);
					g.fillOval(i*(XSIZE/3)+(XSIZE/24)+200, j*(YSIZE/3)+(YSIZE/24), (XSIZE/12+YSIZE/6), (YSIZE/12+YSIZE/6));
				}
			}
			//Draw the x,y coords on the sides
			Text item = new Text(203+i*(XSIZE/3), YSIZE, XSIZE/3-6, 27, i+"", Text.CENTERED);
			item.setColors(new Color(200, 175, 25), Color.blue);
			item.draw(g);
			item.setDims(200+XSIZE, 3+i*(YSIZE/3), 27, YSIZE/3-6);
			item.draw(g);
		}
		
		//check for moves from the opponent
		
	}
	public boolean move(int x, int y, int x2, int y2, boolean override, boolean usersTurn){//The logic of what should happen given a move on this location
		if (!(0<=x && x<3 && 0<=y && y<3)){
			return false; //outside the bounds of the board
		}
		if (board[x][y] != 0){ //the spot has already been claimed
			return false;
		}
		if (!winRow.equals("")){
			return false; //win detected somewhere
		}
		if (cH == null && !override) return false; // no connection handler, and not replay mode
		if (isUsersMove != usersTurn && !override) return false; // The person who tried to move isn't their turn yet
		board[x][y] = usersTurn ? 1:2;
		
		if (usersTurn && cH != null) cH.send(new byte[] {0, (byte)x, (byte)y, -1, -1, 0, -1, this.getGameID()});
		isUsersMove = !usersTurn;
		
		moves += x + ":" + y + ";"; //add to the current move list
		winner(); //is there a winner?
		return true;
	}
	public String getMoves(){//returns a list of moves of the current game
		return moves;
	} 
	public void onClick(MouseEvent e){ // mouse button clicked
		if (!keyListen && isUsersMove){
			//self is x / blue
			move((e.getX()-200)/(XSIZE/3), e.getY()/(XSIZE/3), 0, 0, false, true);
			//move = ((move+1)%2); // locks the move var between 1 and 2;
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
			if (code == 87 || code == 38){ // up w or up arrow
				keyboard[1] = wrap(keyboard[1]-1, 3, 0);
			}
			else if (code == 65 || code == 37){ //left a or left arrow
				keyboard[0] = wrap(keyboard[0]-1, 3, 0);
			}
			else if (code == 83 || code == 40){ // down s or down arrow
				keyboard[1] = wrap(keyboard[1]+1, 3, 0);
			}
			else if (code == 68 || code == 39){ // right d or right arrow
				keyboard[0] = wrap(keyboard[0]+1, 3, 0);
			}
			else if ((code == 10 || code == 32) && isUsersMove){ // submit move - enter button or space
				move(keyboard[0], keyboard[1], 0, 0, false, true);
				//move = ((move+1)%2); // locks the move var between 1 and 2;
			}
			else{
				//System.out.println(e.getKeyChar() + ", " + e.getKeyLocation() + ", " + e.isActionKey() + ", " + e.getKeyCode());
			}
		}
		else{
			keyListen = true;
			keyboard = new int[] {1,1};
		}
	}
	public void keyReleased(KeyEvent e){} //key is relased
	public void typed(KeyEvent e){} //key is typed
	public boolean isOver(){ //returns true if the game is over
		return (winner() != -1 || !hasEmptySpace());
	}
	public int winner(){ //returns who the winner is - -1=no one - 1=player 1 - 2=player 2
		for (int i=0;i<3;i++){
			//Vertical check
			if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != 0){
				winRow="V" + i;
				return board[i][0];
			}
			//Horizontal Check
			if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != 0){
				winRow="H" + i;
				return board[0][i];
			}
		}
		//diaganol checks
		if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[2][2] != 0){
			winRow="D1";
			return board[0][0];
		}
		if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[1][1] != 0){
			winRow="D2";
			return board[1][1];
		}
		winRow = ""; //help with replay manager
		return -1;
	}
	public void reset(){ //resets all the game states
		board = new int[3][3];
		isUsersMove = false;
		moves = "TicTacToe;" + this.getDate() + ";";
		move = 1;
		winRow="";
		keyboard = new int[2];
		keyListen = false;
	}
	public void reset(String boardPreSet){
		//there is no different board set up
		reset();
	}
	public void handleMessage(byte[] nums){
		System.out.println("ERROR: C:hM");
	}
	public boolean hasEmptySpace(){
		for (int i=0;i<board.length;i++){
			for (int j=0;j<board[i].length;j++){
				if (board[i][j] == 0){
					return true;
				}
			}
		}
		return false;
	}
	public boolean keyboardSupport(){
		return true;
	}
	public boolean mouseSupport(){
		return true;
	}
	public int getBoardWidth(){ //returns the width of the board for replayManager's use
		return 3;
	}
	public int getBoardHeight(){ //returns the height of the board for replayerManager's use
		return 3;
	}
	public String getBoardAt(int x, int y){ //returns the value at a specific part of the board
		return inBounds(x, y) ? board[x][y] + "" : "NULL";
	}
	public boolean setBoardAt(int x, int y, String item){ //sets the value of the board at a certain spot to a specified value, returns weather successful
		if (!inBounds(x,y)){
			return false;
		}
		board[x][y] = Integer.parseInt(item);
		return true;
	}
	public void setConnectionHandler(ConnectionHandler cH){
		this.cH = cH;
	}
	public byte getGameID(){
		return 0;
	}
	public String getGameName(){
		return "TicTacToe";
	}
	public int[] version(){
		return new int[] {1,0,0};
	}
	public void setUsersMove(boolean move){
		this.isUsersMove = move;
	}
	public boolean isUsersMove(){
		return this.isUsersMove;
	}
	public String getBoardPreGame(){
		return "N/A";
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
		
		for (int y=0;y<board.length;y++){
			for (int x=0;x<board[y].length;x++){
				if (board[x][y] == 0) moves.add(new int[] {x, y, -1, -1});
			}
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
		
	}
	private boolean inBounds(int x, int y){
		return x>=0 && x<3 && y>=0 && y<3; 
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