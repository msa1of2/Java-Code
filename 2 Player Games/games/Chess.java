package games;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Font;
import helpful.Rectangle;
import helpful.GUI.Button;
import helpful.datastructures.DLList;
import helpful.Scalable;
import helpful.Scalable;

public class Chess implements Game{
	private String[] board; //encoding scheme - ' ':Empty, 'p':Pawn, 'r':Rook, 'n':Knight, 'b':Bishop, 'q':Queen, 'k':king uppercase being player 2's pieces
	private boolean[][] attackedPos; //true if the square is being attacked by an enemy
	private int[] POI;
	private static int[] knightMoves = new int[] {1,2,2,1,-1,-2,-2,-1}, indexTranslation = new int[] {1,1,1,0,-1,-1,-1,0};;
	private int move;
    private boolean isUpgrading, isOver, isUsersTurn;
    private Button upgradeQueen, upgradeRook, upgradeKnight, upgradeBishop;
	private int xStart;
	private String moves;
	private ConnectionHandler cH;
	//Component Variables
	private boolean isVisible;
	private int x, y;
	public Chess(int index){
		reset();
		upgradeQueen= new Button(15, 250, 170, 30, "Upgrade to Queen");
        upgradeRook = new Button(15, 295, 170, 30, "Upgrade to Rook");
        upgradeKnight = new Button(15, 340, 170, 30, "Upgrade to Knight");
        upgradeBishop = new Button(15, 385, 170, 30, "Upgrade to Bishop");
		isVisible = true;
		x = (index%3)*XSIZE/3;
		y = (index/3)*YSIZE/3;
	}
	public void drawGame(Graphics g){ //draws the board
		xStart = Math.max((int)g.getClipBounds().getWidth()-XSIZE, 200);
		if (isOver){
			g.setColor(Color.black);
			g.fillRect(xStart, 0, XSIZE, YSIZE);
			g.setColor(Color.white);
			g.setFont(new Font("Arial", Font.PLAIN, 50));
			g.drawString("Player " + winner() + " Won", xStart + XSIZE/2 - g.getFontMetrics().stringWidth("Player " + winner() + " Won")/2, YSIZE/2 + (int)(g.getFontMetrics().getHeight()*(1-23.0/59))/2);
			return;
		}
		for (int y=0;y<board.length;y++){
			for (int x=0;x<Math.min(board[0].length(),8);x++){
				
				//draw the board
				if (x == POI[0] && y == POI[1]){
					g.setColor(Color.magenta);
				}
				else if ((x+y)%2==0){
                    g.setColor(Color.darkGray);
                }
                else{
                    g.setColor(Color.lightGray);
                }
                g.fillRect(xStart + x * XSIZE/8, y*YSIZE/8, XSIZE/8, YSIZE/8);
				
				//draw enemy attacks
				g.setColor(new Color(128, 25, 25));
				if (attackedPos[x][y]) g.fillOval(xStart + (int)((x+0.25)*XSIZE/8), (int)((y+0.25)*YSIZE/8), XSIZE/16, YSIZE/16);
				
				//draw the pieces
				g.setColor(Character.isUpperCase(board[y].charAt(x)) ? Color.red : Color.black);
				if (board[y].charAt(x) != ' '){
					this.drawItem(g, x, y, board[y].charAt(x) + "");
					//g.drawString(board[y].charAt(x) + "", xStart + x*XSIZE/8+5, (y+1)*YSIZE/8-5);
					//g.fillRect(xStart + x*XSIZE/8+10, y*YSIZE/8+10, XSIZE/8-20, YSIZE/8-20);
				}
				
				if (inBounds(POI[0], POI[1]) ? (board[POI[1]].charAt(POI[0]) == ' ' || Character.isUpperCase(board[POI[1]].charAt(POI[0]))) : true){ //ensure the piece of intrest is in the bounds of the board
					POI = new int[] {-1, -1}; //if the space is empty, do nothing, extra protection against edge cases
					continue;
				}
				
				//draw the attack possibilites of the piece of intrest
				int[] attackPos = calcMoves(POI[0], POI[1]);
				for (int n =0;n<attackPos.length;n++){
					//System.out.println(indexTranslation[(n+7)%8] + ", " + indexTranslation[(n+1)%8]);
					if (board[POI[1]].charAt(POI[0]) != 'n'){
						for (int i=1;i<=attackPos[n];i++){
							g.setColor(Color.green);
							if (attackedPos[POI[0]-indexTranslation[(n+3)%8]*i][POI[1]-indexTranslation[(n+1)%8]*i]) g.setColor(Color.cyan);
							g.fillOval((int)(xStart + (POI[0]-indexTranslation[(n+3)%8]*i+0.25)*XSIZE/8), (int)((POI[1]-indexTranslation[(n+1)%8]*i+0.25)*YSIZE/8), XSIZE/16, YSIZE/16);
						}
					}
					else if (attackPos[n] >= 1){
						g.setColor(Color.green);
						if (attackedPos[POI[0]+knightMoves[(n)%8]][POI[1]+knightMoves[(n+2)%8]]) g.setColor(Color.cyan);
						g.fillOval((int)(xStart + (POI[0]+knightMoves[(n)%8]+0.25)*XSIZE/8), (int)((POI[1]+knightMoves[(n+2)%8]+0.25)*YSIZE/8), XSIZE/16, YSIZE/16);
					}
				}
				
			}
		}
        if (isUpgrading){
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            //drawStackString(g, "What will you upgrade your pawn to?", 15, 160, 170);
            upgradeQueen.draw(g);
            upgradeRook.draw(g);
            upgradeKnight.draw(g);
            upgradeBishop.draw(g);
        }
	}
	public void drawGame(Scalable g){ //draws the board
		xStart = Math.max((int)g.getClipBounds().getWidth()-XSIZE, 200);
		if (isOver){
			g.setColor(Color.black);
			g.fillRect(xStart, 0, XSIZE, YSIZE);
			g.setColor(Color.white);
			g.setFont(new Font("Arial", Font.PLAIN, 50));
			g.drawString("Player " + winner() + " Won", xStart + XSIZE/2 - g.getFontMetrics().stringWidth("Player " + winner() + " Won")/2, YSIZE/2 + (int)(g.getFontMetrics().getHeight()*(1-23.0/59))/2);
			return;
		}
		for (int y=0;y<board.length;y++){
			for (int x=0;x<Math.min(board[0].length(),8);x++){
				
				//draw the board
				if (x == POI[0] && y == POI[1]){
					g.setColor(Color.magenta);
				}
				else if ((x+y)%2==0){
                    g.setColor(Color.darkGray);
                }
                else{
                    g.setColor(Color.lightGray);
                }
                g.fillRect(xStart + x * XSIZE/8, y*YSIZE/8, XSIZE/8, YSIZE/8);
				
				//draw enemy attacks
				g.setColor(new Color(128, 25, 25));
				if (attackedPos[x][y]) g.fillOval(xStart + (int)((x+0.25)*XSIZE/8), (int)((y+0.25)*YSIZE/8), XSIZE/16, YSIZE/16);
				
				//draw the pieces
				g.setColor(Character.isUpperCase(board[y].charAt(x)) ? Color.red : Color.black);
				if (board[y].charAt(x) != ' '){
					this.drawItem(g, x, y, board[y].charAt(x) + "");
					//g.drawString(board[y].charAt(x) + "", xStart + x*XSIZE/8+5, (y+1)*YSIZE/8-5);
					//g.fillRect(xStart + x*XSIZE/8+10, y*YSIZE/8+10, XSIZE/8-20, YSIZE/8-20);
				}
				
				if (inBounds(POI[0], POI[1]) ? (board[POI[1]].charAt(POI[0]) == ' ' || Character.isUpperCase(board[POI[1]].charAt(POI[0]))) : true){ //ensure the piece of intrest is in the bounds of the board
					POI = new int[] {-1, -1}; //if the space is empty, do nothing, extra protection against edge cases
					continue;
				}
				
				//draw the attack possibilites of the piece of intrest
				int[] attackPos = calcMoves(POI[0], POI[1]);
				for (int n =0;n<attackPos.length;n++){
					//System.out.println(indexTranslation[(n+7)%8] + ", " + indexTranslation[(n+1)%8]);
					if (board[POI[1]].charAt(POI[0]) != 'n'){
						for (int i=1;i<=attackPos[n];i++){
							g.setColor(Color.green);
							if (attackedPos[POI[0]-indexTranslation[(n+3)%8]*i][POI[1]-indexTranslation[(n+1)%8]*i]) g.setColor(Color.cyan);
							g.fillOval((int)(xStart + (POI[0]-indexTranslation[(n+3)%8]*i+0.25)*XSIZE/8), (int)((POI[1]-indexTranslation[(n+1)%8]*i+0.25)*YSIZE/8), XSIZE/16, YSIZE/16);
						}
					}
					else if (attackPos[n] >= 1){
						g.setColor(Color.green);
						if (attackedPos[POI[0]+knightMoves[(n)%8]][POI[1]+knightMoves[(n+2)%8]]) g.setColor(Color.cyan);
						g.fillOval((int)(xStart + (POI[0]+knightMoves[(n)%8]+0.25)*XSIZE/8), (int)((POI[1]+knightMoves[(n+2)%8]+0.25)*YSIZE/8), XSIZE/16, YSIZE/16);
					}
				}
				
			}
		}
        if (isUpgrading){
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            //drawStackString(g, "What will you upgrade your pawn to?", 15, 160, 170);
            upgradeQueen.draw(g);
            upgradeRook.draw(g);
            upgradeKnight.draw(g);
            upgradeBishop.draw(g);
        }
	}
	public void debug(){
		String send = "";
		for (int x =0;x<attackedPos.length;x++){
			for (int y=0;y<attackedPos[x].length;y++){
				send += attackedPos[y][x]?1:0;
			}
			send+="\n";
		}
		System.out.println(send + " Attack board");
	}
	public void drawIcon(Graphics g, int x, int y, int width, int length){
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.setColor(Color.white);
		drawStackString(g, "Chess", x+5, y+5, width);
		g.drawRect(x, y, width, length);
	}
	public void drawIcon(Scalable g, int x, int y, int width, int length){
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.setColor(Color.white);
		drawStackString(g, "Chess", x+5, y+5, width);
		g.drawRect(x, y, width, length);
	}
	public void onClick(MouseEvent e){ // mouse button clicked
        int xClick = (e.getX() - xStart - 8) / (XSIZE/8), //adjustments for where the mouse clicks on windows
            yClick = (e.getY() - 30) / (YSIZE/8);
        if ((xClick != POI[0] || yClick != POI[1]) && inBounds(POI[0], POI[1]) && isUsersTurn){
            boolean result = move(POI[0], POI[1], xClick, yClick, false, true);
            if (result){ //only stop next logic if the move was successful
                if (board[yClick].charAt(xClick) == 'p' && yClick == 0){
                    //pawn promotion
                    isUpgrading = true;
                    POI = new int[] {xClick, yClick}; //guarenteed to be in bounds
                }
                else{
                    POI = new int[] {-1, -1}; //reset the piece of intrest
                }
                
                return;
            }
        }
        if (isUpgrading){
			//notify enemy of any/all promotions we make
            if (upgradeQueen.isClicked(e)){
                board[POI[1]] = board[POI[1]].substring(0, POI[0]) + 'q' + board[POI[1]].substring(POI[0]+1,8);
                isUpgrading = false;
				cH.send(new byte[] {5, (byte)(7-POI[0]), (byte)(7-POI[1]), (byte)'Q'});
				POI = new int[] {-1, -1}; //reset the piece of intrest
            }
            if (upgradeRook.isClicked(e)){
                board[POI[1]] = board[POI[1]].substring(0, POI[0]) + 'r' + board[POI[1]].substring(POI[0]+1,8);
                isUpgrading = false;
				cH.send(new byte[] {5, (byte)(7-POI[0]), (byte)(7-POI[1]), (byte)'R'});
				POI = new int[] {-1, -1}; //reset the piece of intrest
            }
            if (upgradeKnight.isClicked(e)){
                board[POI[1]] = board[POI[1]].substring(0, POI[0]) + 'n' + board[POI[1]].substring(POI[0]+1,8);
                isUpgrading = false;
				cH.send(new byte[] {5, (byte)(7-POI[0]), (byte)(7-POI[1]), (byte)'N'});
				POI = new int[] {-1, -1}; //reset the piece of intrest
            }
            if (upgradeBishop.isClicked(e)){
                board[POI[1]] = board[POI[1]].substring(0, POI[0]) + 'b' + board[POI[1]].substring(POI[0]+1,8);
                isUpgrading = false;
				cH.send(new byte[] {5, (byte)(7-POI[0]), (byte)(7-POI[1]), (byte)'B'});
				 POI = new int[] {-1, -1}; //reset the piece of intrest
            }
        }
        else if (isUsersTurn){
    		POI = new int[] {xClick, yClick};
    		if (inBounds(xClick, yClick) ? board[POI[1]].charAt(POI[0]) == ' ' || Character.isUpperCase(board[POI[1]].charAt(POI[0])) : true){
    			POI = new int[] {-1, -1}; //if the space is empty, do nothing
    			return;
    		}
        }
		else{
			POI = new int[] {-1, -1};
		}
	}
	public void mousePressed(MouseEvent e){} // mouse button pressed
	public void mouseReleased(MouseEvent e){} // mouse button relased
	public void mouseEntered(MouseEvent e){} // mouse entered screen
	public void mouseExited(MouseEvent e){} // mouse exited screen
	public void keyPressed(KeyEvent e){ // key is pressed
		
	}
	public void keyReleased(KeyEvent e){} //key is relased
	public void typed(KeyEvent e){} //key is typed
	public boolean isOver(){ //returns true if the game is over
		return winner() != -1;
	}
	public boolean validate(String moves){ //returns true if the last move is valid
		return true;
	}
	public int winner(){ //returns who the winner is - -1=no one - 1=player 1 - 2=player 2
		boolean homeKing=false, enemyKing=false;
		for (int y=0;y<board.length;y++){
			for (int x=0;x<board[0].length();x++){
				if (board[y].charAt(x) == 'k'){
					homeKing = true;
				}
				if (board[y].charAt(x) == 'K'){
					enemyKing = true;
				}
			}
		}
		isOver = (homeKing ? (enemyKing ? -1:1) : (enemyKing ? 2:3)) != -1;
		return homeKing ? (enemyKing ? -1:1) : (enemyKing ? 2:3);
	}
	public String getMoves(){ //returns a list of moves of the current game
		return moves;
	}
	public void reset(){ //resets all the game states
		board = new String[8];
		attackedPos = new boolean[board.length][board.length];
		for (int i=0;i<board.length;i++){
			board[i] = "        ";
		}
		board[0] = "RNBQKBNR";
		board[1] = "PPPPPPPP";
		board[6] = "pppppppp";
		board[7] = "rnbqkbnr";
		POI = new int[] {-1, -1};
		move = 0;
		xStart = 0;
		isOver = false;
		moves = "Chess;" + this.getDate() + ";";
		calcAttacks();
	}
	public void reset(String boardPreSet){
		//there is no different board set up
		reset();
	}
	public void handleMessage(byte[] nums){
		byte x = nums[1];
		byte y = nums[2];
		char item = (char)nums[3];
		setBoardAt(x, y, item+"");
	}
	public boolean move(int x, int y, int x2, int y2, boolean override, boolean usersTurn){ //The logic of what should happen given a move on this location
		
		//System.out.println("Chess Debug: 0, " + x + ", " + y + ", " + x2 + ", " + y2 + ", " + override + ", " + usersTurn + " --- " + this.getBoardAt(x, y) + ", " + this.getBoardAt(x2, y2));
		int res = moveInt(x, y, x2, y2, override, usersTurn);
		System.out.println("Chess Debug: " + res + ", " + x + ", " + y + ", " + x2 + ", " + y2 + ", " + override + ", " + usersTurn + " --- " + this.getBoardAt(x, y) + ", " + this.getBoardAt(x2, y2));
		if (res <= 0){
			isUsersTurn = !usersTurn;
			return true;
		}
		return false;
	}
	public int moveInt(int x, int y, int x2, int y2, boolean override, boolean usersTurn){
		int[] possibleMoves = calcMoves(x,y);
		if (!inBounds(x, y)) return 2;
		if (!inBounds(x2, y2)) return 3;
		if (!isEnemy(x, y, x2, y2)) return 4;
		if (board[y].charAt(x) =='k' && attackedPos[x2][y2]) return 5;
		
		String send ="Attack Pos: ";
		for (int num : possibleMoves){
			send += num + ", ";
		}
		System.out.println(send);
		
		boolean canMove = false;
		if (inBounds(x, y) ? Character.toLowerCase(board[y].charAt(x)) == 'n' : false){
			//special knight move-set
			int xChange = Math.abs(x2-x);
			int yChange = Math.abs(y2-y);
			if ((xChange == 1 && yChange == 2) || (xChange == 2 && yChange == 1)){
				canMove = true;
			}
		}
		if (Math.abs(x-x2) == 0 ^ Math.abs(y-y2)==0){
			//a straight line move up down left or right
			if (x2 > x && possibleMoves[2] >= (x2-x)){
				//move to the right, ensure the possible distance travled is more than the actual distance traveled
				canMove = true;
			}
			if (x > x2 && possibleMoves[6] >= (x-x2)){
				//move to the left
				canMove = true;
			}
			if (y2 > y && possibleMoves[4] >= (y2-y)){
				//move downwards
				canMove = true;
			}
			if (y > y2 && possibleMoves[0] >= (y-y2)){
				//move upwards
				canMove = true;
			}
		}
		else if (Math.abs(x-x2) == Math.abs(y-y2)){
			//a combo move on a diagonol
			if (x2 > x && y2 > y && possibleMoves[3] >= x2-x){
				//bottom right move
				canMove = true;
			}
			if (x2 > x && y > y2 && possibleMoves[1] >= x2-x){
				//top right move
				canMove = true;
			}
			if (x > x2 && y2 > y && possibleMoves[5] >= x-x2){
				//bottom left move
				canMove = true;
			}
			if (x > x2 && y > y2 && possibleMoves[7] >= x-x2){
				//top left move
				canMove = true;
			}
		}
		//actually move the piece to the designateed spot
		if (canMove || override){
			board[y2] = board[y2].substring(0,x2) + board[y].charAt(x) + board[y2].substring(x2+1,8);
			board[y] = board[y].substring(0,x) + ' ' + board[y].substring(x+1,8);
			if (cH != null && usersTurn) cH.send(new byte[] {0, (byte)(7-x), (byte)(7-y), (byte)(7-x2), (byte)(7-y2), 0, 0, this.getGameID()}); //say when a move happens
			calcAttacks();
			return 0;
		}
		return 1;
	}
	public boolean keyboardSupport(){ //returns true if the code is designed to work with keyboard inputs
		return false;
	}
	public boolean mouseSupport(){ //returns true if the code is designed to work with mouse inputs
		return true;
	}
	public int getBoardWidth(){ //returns the width of the board for replayManager's use
		return 8;
	}
	public int getBoardHeight(){ //returns the height of the board for replayerManager's use
		return 8;
	}
	public String getBoardAt(int x, int y){ //returns the value at a specific part of the board
		return inBounds(x, y) ? board[y].charAt(x) + "" : "NULL";
	}
	public boolean setBoardAt(int x, int y, String item){ //sets the value of the board at a certain spot to a specified value, returns weather successful
		if (!inBounds(x,y)){
			return false;
		}
		char charItem = item.charAt(0);
		if (item.equals("knight")){
			charItem = 'n';
		}
		board[y] = board[y].substring(0,x) + charItem + board[y].substring(x+1,8);
		return true;
	}
	public void setConnectionHandler(ConnectionHandler cH){
		this.cH = cH;
	}
	public byte getGameID(){
		return 5;
	}
	public String getGameName(){
		return "Chess";
	}
	public int[] version(){
		return new int[] {0,1,0};
	}
	public void setUsersMove(boolean move){
		isUsersTurn = move;
		if (!move){
			//We dont go first, swap king and queen
			setBoardAt(3, 0, "K");
			setBoardAt(4, 0, "Q");
			setBoardAt(3, 7, "k");
			setBoardAt(4, 7, "q");
		}
	}
	public boolean isUsersMove(){
		return isUsersTurn;
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
		cH.send(new byte[] {5, 0, (byte)board[array[0]].charAt(array[1]), -1, -1, -1, -1, -1});
	}
	public String parseBoard(byte[] array){ //not secretive - not needed
		return "";
	}
	public int[][] getValidMoves(boolean usersMove){ //returns all the valid moves of the current board, the second dimension contains [x1, y1, x2, y2]
		DLList<int[]> moves = new DLList<int[]>();
		
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
	private void drawItem(Graphics g, int xItem, int yItem, String item){ //draws a part of the game depending on what item is
		if (item.equals("board")){
			for (int y=0;y<board.length;y++){
				for (int x=0;x<Math.min(board[0].length(),8);x++){
					//draw the board
					if ((x+y)%2==0){
						g.setColor(Color.darkGray);
					}
					else{
						g.setColor(Color.lightGray);
					}
					g.fillRect(xStart + x * XSIZE/8, y*YSIZE/8, XSIZE/8, YSIZE/8);
				}
			}
		}
		else{
			//draw each piece here
			g.setFont(new Font("Arial", Font.PLAIN, 35));
			char charItem = item.charAt(0);
			g.setColor(Color.green);
			if (Character.isUpperCase(charItem)){
				g.setColor(Color.red);
			}
			charItem = Character.toLowerCase(charItem);
			switch (charItem){
				case 'p':
					g.fillOval((xStart + xItem*XSIZE/8)+10, (yItem*XSIZE/8)+55, 55, 18);
					g.fillOval((xStart + xItem*XSIZE/8)+22, (yItem*XSIZE/8)+12, 30, 30);
					g.fillPolygon(new int[] {(xStart + xItem*XSIZE/8)+37, (xStart + xItem*XSIZE/8)+13, (xStart + xItem*XSIZE/8)+62}, new int[] {(yItem*XSIZE/8)+20, (yItem*XSIZE/8)+65, (yItem*XSIZE/8)+65}, 3);
					break;
				case 'r':
					g.fillRect((xStart + xItem*XSIZE/8)+15, (yItem*XSIZE/8)+18, 46, 52);
					g.fillRect((xStart + xItem*XSIZE/8)+10, (yItem*XSIZE/8)+13, 55, 7);
					g.fillRect((xStart + xItem*XSIZE/8)+10, (yItem*XSIZE/8)+5, 12, 8);
					g.fillRect((xStart + xItem*XSIZE/8)+30, (yItem*XSIZE/8)+5, 16, 8);
					g.fillRect((xStart + xItem*XSIZE/8)+53, (yItem*XSIZE/8)+5, 12, 8);
					break;
				case 'n':
					g.fillPolygon(new int[] {(xStart + xItem*XSIZE/8)+10, (xStart + xItem*XSIZE/8)+34, (xStart + xItem*XSIZE/8)+12, (xStart + xItem*XSIZE/8)+5, (xStart + xItem*XSIZE/8)+34, (xStart + xItem*XSIZE/8)+50, (xStart + xItem*XSIZE/8)+60, (xStart + xItem*XSIZE/8)+55}, new int[] {(yItem*XSIZE/8)+64, (yItem*XSIZE/8)+30, (yItem*XSIZE/8)+39, (yItem*XSIZE/8)+30, (yItem*XSIZE/8)+5, (yItem*XSIZE/8)+7, (yItem*XSIZE/8)+34, (yItem*XSIZE/8)+59}, 8);
					setBackgroundColor(g, xItem, yItem);
					g.fillOval((xStart + xItem*XSIZE/8)+35, (yItem*XSIZE/8)+12, 5, 5);
					g.fillOval((xStart + xItem*XSIZE/8)+27, (yItem*XSIZE/8)+11, 5, 5);
					break;
				case 'b':
					g.fillOval((xStart + xItem*XSIZE/8)+10, (yItem*XSIZE/8)+55, 55, 18);
					g.fillPolygon(new int[] {(xStart + xItem*XSIZE/8)+15, (xStart + xItem*XSIZE/8)+37, (xStart + xItem*XSIZE/8)+60}, new int[] {(yItem*XSIZE/8)+66, (yItem*XSIZE/8)+20, (yItem*XSIZE/8)+64}, 3);
					g.fillOval((xStart + xItem*XSIZE/8)+17, (yItem*XSIZE/8)+2, 40, 40);
					setBackgroundColor(g, xItem, yItem);
					g.fillPolygon(new int[] {(xStart + xItem*XSIZE/8)+27, (xStart + xItem*XSIZE/8)+22, (xStart + xItem*XSIZE/8)+32, (xStart + xItem*XSIZE/8)+36}, new int[] {(yItem*XSIZE/8)+4, (yItem*XSIZE/8)+5, (yItem*XSIZE/8)+14, (yItem*XSIZE/8)+12}, 4);
					break;
				case 'q':
					g.fillOval((xStart + xItem*XSIZE/8)+10, (yItem*XSIZE/8)+55, 55, 18);
					g.fillPolygon(new int[] {(xStart + xItem*XSIZE/8)+15, (xStart + xItem*XSIZE/8)+37, (xStart + xItem*XSIZE/8)+60}, new int[] {(yItem*XSIZE/8)+64, (yItem*XSIZE/8)+20, (yItem*XSIZE/8)+64}, 3);
					g.fillOval((xStart + xItem*XSIZE/8)+17, (yItem*XSIZE/8)+2, 40, 40);
					g.fillRect((xStart + xItem*XSIZE/8)+17, (yItem*XSIZE/8)+2, 40, 20);
					setBackgroundColor(g, xItem, yItem);
					g.drawString("Q", (xStart + xItem*XSIZE/8)+25, (yItem*XSIZE/8)+5+(int)(g.getFontMetrics().getHeight()*(1-21.0/59)));
					break;
				case 'k':
					g.fillOval((xStart + xItem*XSIZE/8)+10, (yItem*XSIZE/8)+55, 55, 18);
					g.fillPolygon(new int[] {(xStart + xItem*XSIZE/8)+15, (xStart + xItem*XSIZE/8)+37, (xStart + xItem*XSIZE/8)+60}, new int[] {(yItem*XSIZE/8)+64, (yItem*XSIZE/8)+20, (yItem*XSIZE/8)+64}, 3);
					g.fillOval((xStart + xItem*XSIZE/8)+17, (yItem*XSIZE/8)+2, 40, 40);
					g.fillRect((xStart + xItem*XSIZE/8)+17, (yItem*XSIZE/8)+2, 40, 20);
					setBackgroundColor(g, xItem, yItem);
					g.drawString("K", (xStart + xItem*XSIZE/8)+25, (yItem*XSIZE/8)+5+(int)(g.getFontMetrics().getHeight()*(1-21.0/59)));
					break;
			}
		}
	}
	private boolean inBounds(int x, int y){
		return x>=0 && x<8 && y>=0 && y<8;
	}
	private int[] calcMoves(int x, int y){
		int[] send = new int[8]; //index pos: 0-top, 1-top right, 2-right, 3-bottom right, 4-bottom, 5-bottom left, 6-left, 7-top left
		if (!inBounds(x, y)){
			return send; //an empty list of size 8
		}
		int dir = Character.isLowerCase(board[y].charAt(x)) ? 1:-1;
		char piece = Character.toLowerCase(board[y].charAt(x));
		if (piece == ' '){
			return send; //empty list return
		}
		if (piece == 'p'){ //pawn movements
			//pawn
			//go two steps forward only for the first move, on home row
			send[dir ==1 ? 0:4] = getStraightEmptyLine(x, y, 0, 1*dir, ((y==6&&dir==1) || (y==1&&dir==-1)) ? 2:1, false);
			if (!isEmpty(x-1, y-1*dir) && isEnemy(x, y, x-1, y-1*dir)){
				//top left has enemy
				send[dir==1?7:5]++;
			}
			if (!isEmpty(x+1, y-1*dir) && isEnemy(x, y, x+1,y-1*dir)){
				//top right has enemy
				send[dir==1?1:3]++;
			}
		}
		if (piece == 'r' || piece == 'q' || piece == 'k'){ //rook movements, part of king/queen movements
			//Rook
			send[0] = getStraightEmptyLine(x, y, 0, 1, (piece == 'k' ? 1:8)); //straight forward
			send[2] = getStraightEmptyLine(x, y, -1, 0, (piece == 'k' ? 1:8)); //to the right
			send[4] = getStraightEmptyLine(x, y, 0, -1, (piece == 'k' ? 1:8)); //straight downwards
			send[6] = getStraightEmptyLine(x, y, 1, 0, (piece == 'k' ? 1:8)); //to the left
		}
		if (piece == 'n'){ //kight movements
			for (int i=0;i<knightMoves.length;i++){
				//{1,2,2,1,-1,-2,-2,-1}
				// ^ !inBounds(x+knightMoves[(i)%8], y+knightMoves[(i+2)%8])
				//System.out.println("Checking: " + (x+knightMoves[(i)%8]) + ":" + (y+knightMoves[(i+2)%8]));
				send[i] = (isEmpty(x+knightMoves[(i)%8], y+knightMoves[(i+2)%8]) || 
						   isEnemy(x, y, x+knightMoves[(i)%8], y+knightMoves[(i+2)%8])) ? 1:0;
			}
		}
		if (piece == 'b' || piece == 'q' || piece == 'k'){ //bishop movements, part of king/queen movements
			send[7] = getStraightEmptyLine(x, y, 1, 1, (piece == 'k' ? 1:8)); //top left
			send[1] = getStraightEmptyLine(x, y, -1, 1, (piece == 'k' ? 1:8)); //top right
			send[3] = getStraightEmptyLine(x, y, -1, -1, (piece == 'k' ? 1:8)); //bottom right
			send[5] = getStraightEmptyLine(x, y, 1, -1, (piece == 'k' ? 1:8)); //bottom left
		}
		if (piece == 'k'){ //king cant move to a square where it can die next turn
			for (int i=0;i<send.length;i++){
				int xVal = indexTranslation[(5+i)%8];
				int yVal = indexTranslation[(7+i)%8];
				if (inBounds(x+xVal, y+yVal) ? attackedPos[x+xVal][y+yVal] : true) send[i] = 0;
			}
		}
		return send;
	}
	private int getStraightEmptyLine(int x, int y, int xValue, int yValue, int maxValue, boolean includeEnemies){
		if (!inBounds(x,y)){
			return 0;
		}
		int send = 0;
		for (int i=1;i<=maxValue && isEmpty(x-i*xValue,y-i*yValue);i++){
			//System.out.println(x-i*xValue + ", " + (y-i*yValue));
			send++;
		}
		//System.out.println((inBounds(x-(send+1)*xValue, y-(send+1)*yValue) ? board[y-(send+1)*yValue].charAt(x-(send+1)*xValue) : "OoB")+ ", " + (x-(send+1)*xValue) + ", " + (y-(send+1)*yValue));
		if (includeEnemies && isEnemy(x, y, x-(send+1)*xValue, y-(send+1)*yValue) && !isEmpty(x-(send+1)*xValue, y-(send+1)*yValue)){
			
			//include the enemies
			send++;
		}
		return send;
	}
	private int getStraightEmptyLine(int x, int y, int xValue, int yValue, int maxValue){
		return getStraightEmptyLine(x, y, xValue, yValue, maxValue, true);
	}
	private boolean isEmpty(int x, int y){
		if (!inBounds(x, y)){
			return false;
		}
		return board[y].charAt(x) == ' ';
	}
	private void setBackgroundColor(Graphics g, int x, int y){
		g.setColor((x+y)%2==0 ? Color.darkGray : Color.lightGray);
	}
	private int isPositive(char letter){
		int num = Integer.parseInt(letter+"");
		if (num ==0) return 0;
		if (num > 0) return 1;
		return -1;
	}
	private boolean isEnemy(int x, int y, int enyX, int enyY){
		if (!inBounds(x, y) || !inBounds(enyX, enyY)) return false;
		if (board[enyY].charAt(enyX) == ' ') return true; //can always attack an empty square
		boolean friend = Character.isUpperCase(board[y].charAt(x));
		boolean enemy  = Character.isUpperCase(board[enyY].charAt(enyX));
		return enemy ^ friend;
	}
	public boolean isStandardMove(){
		return true;
	}
	private void calcAttacks(){
		attackedPos = new boolean[board.length][board[0].length()];
		int[] attacks;
		for (int y=0;y<board.length;y++){
			for (int x=0;x<board[y].length();x++){
				if (!Character.isUpperCase(board[y].charAt(x))) continue; //only intrested in enemy attacks
				//index pos: 0-top, 1-top right, 2-right, 3-bottom right, 4-bottom, 5-bottom left, 6-left, 7-top left
				attacks = calcMoves(x, y);
				for (int i=0;i<attacks.length;i++){
					if (board[y].charAt(x) != 'N' && board[y].charAt(x) != 'P'){
						int xVal = indexTranslation[(7+i)%8];
						int yVal = indexTranslation[(5+i)%8];
						for (int distance=1;distance<=attacks[i] && inBounds(x+xVal*distance, y+yVal*distance);distance++){
							attackedPos[x+xVal*distance][y+yVal*distance] = true;
						}
					}
					else if (board[y].charAt(x) == 'P'){
						if (inBounds(x-1, y+1)) attackedPos[x-1][y+1] = true;
						if (inBounds(x+1, y+1)) attackedPos[x+1][y+1] = true;
					}
					else if (attacks[i] >= 1){
						attackedPos[x+knightMoves[(i)%8]][y+knightMoves[(i+2)%8]] = true;
					}
				}
			}
		}
		
	}
	
	public String print(int[] nums){
		String send = "";
		for (int num : nums){
			send += num + ", ";
		}
		return send;
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