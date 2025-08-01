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

public class Checkers implements Game, Replayable{
    private int[][] board; //saves the entire board
    private int move; // saves whos turn it is
    private String moves; //saves the current moves of the game
    private int[] POI, attackPos; //saves the location of the keyboard inputs, and the piece of intreset
    private int xStart, winner;
    private boolean lockJump; // locks the piece selected if they can jump for a second time in a row
    private boolean debugBool; //turns on/off printing for debug purposes
    private Button endJumpString;
	private ConnectionHandler cH;
	//Component Variables
	private boolean isVisible;
	private int x, y;
    public Checkers(int index){
        reset();
        endJumpString = new Button(5, 395, 190, 50, "End Jumps");
        xStart = 200;
		isVisible = true;
		x = (index%3)*XSIZE/3;
		y = (index/3)*YSIZE/3;
    }
	public void drawGame(Graphics g){ //draws the board
        xStart = Math.max((int)g.getClipBounds().getWidth()-XSIZE, 200);
        g.setColor(Color.darkGray);
        g.fillRect(xStart, 0, XSIZE, YSIZE);
        
		for (int x=0;x<8;x++){
            for (int y=0;y<8;y++){
                if (POI[0] == x && POI[1] == y){
                    g.setColor(Color.magenta); // draw the pice of intrest's magenta border
                }
                else{
                    g.setColor(Color.lightGray);
                }
				if ((x+y)%2==0){
					g.fillRect(xStart + x*XSIZE/8, y*YSIZE/8, XSIZE/8, YSIZE/8);
				}

                //draw the pieces
                if (board[x][y] < 0){
                    g.setColor(Color.black);
                    g.fillOval(xStart + x*XSIZE/8+5, y*YSIZE/8+5, XSIZE/8-10, YSIZE/8-10);
                }
                if (board[x][y] > 0){
                    g.setColor(Color.red);
                    g.fillOval(xStart + x*XSIZE/8+5, y*YSIZE/8+5, XSIZE/8-10, YSIZE/8-10);
                }
                if (Math.abs(board[x][y]) == 2){
                    //king piece
                    g.setColor(Color.lightGray);
                    g.setFont(new Font("Arial", Font.PLAIN, 35));
                    g.drawString("K", xStart + x*XSIZE/8+5 + XSIZE/16 - g.getFontMetrics().stringWidth("K"), y*YSIZE/8+5 + YSIZE/16 + g.getFontMetrics().getHeight()/4);
                    g.setFont(new Font("Arial", Font.PLAIN, 15));
                }
            }
        }
		if (inBounds(POI[0], 0)){
            g.setColor(new Color(25, 235, 25, 75));
            int dir = isPositive(move);
            for (int i=0;i<attackPos.length;i+=2){
                if (Math.abs(board[POI[0]][POI[1]]) != 2 && i>=4){
                    break; // cant go backwards until its a king piece
                }
                if (attackPos[i] != -1 && attackPos[i+1] != -1){
                    g.fillRect(xStart + attackPos[i] * XSIZE/8, attackPos[i+1]*YSIZE/8, XSIZE/8, YSIZE/8);
                    if (debugBool){
                        g.setColor(Color.white);
                        g.drawString(attackPos[i] + ":" + attackPos[i+1], xStart + attackPos[i] * XSIZE/8, attackPos[i+1]*YSIZE/8);
                        g.setColor(new Color(25, 235, 25, 75));
						System.out.println(attackPos[i] + ", " + attackPos[i+1] + " drawing");
                    }
                }
            }
			
        }
        if (lockJump){
            endJumpString.setVisible(true);
        }
		else{
			endJumpString.setVisible(false);
		}
		if (winner != -1){
			g.setFont(new Font("Arial", Font.PLAIN, 35));
			g.setColor(Color.white);
			g.fillRect(xStart+XSIZE/2-g.getFontMetrics().stringWidth("Winner: Player: " + winner)/2, YSIZE/2-10 - g.getFontMetrics().getHeight(), g.getFontMetrics().stringWidth("Winner: Player: " + winner), g.getFontMetrics().getHeight());
            g.setColor(Color.black);
            g.drawString("Winner: Player " + winner, xStart+XSIZE/2 - g.getFontMetrics().stringWidth("Winner: Player: " + winner)/2, YSIZE/2 - g.getFontMetrics().getHeight()/2);
            g.setFont(new Font("Arial", Font.PLAIN, 15));
            //return;
        }
		endJumpString.draw(g);
	}
	public void drawGame(Scalable g){ //draws the board
        xStart = Math.max((int)g.getClipBounds().getWidth()-XSIZE, 200);
        g.setColor(Color.darkGray);
        g.fillRect(xStart, 0, XSIZE, YSIZE);
        
		for (int x=0;x<8;x++){
            for (int y=0;y<8;y++){
                if (POI[0] == x && POI[1] == y){
                    g.setColor(Color.magenta); // draw the pice of intrest's magenta border
                }
                else{
                    g.setColor(Color.lightGray);
                }
				if ((x+y)%2==0){
					g.fillRect(xStart + x*XSIZE/8, y*YSIZE/8, XSIZE/8, YSIZE/8);
				}

                //draw the pieces
                if (board[x][y] < 0){
                    g.setColor(Color.black);
                    g.fillOval(xStart + x*XSIZE/8+5, y*YSIZE/8+5, XSIZE/8-10, YSIZE/8-10);
                }
                if (board[x][y] > 0){
                    g.setColor(Color.red);
                    g.fillOval(xStart + x*XSIZE/8+5, y*YSIZE/8+5, XSIZE/8-10, YSIZE/8-10);
                }
                if (Math.abs(board[x][y]) == 2){
                    //king piece
                    g.setColor(Color.lightGray);
                    g.setFont(new Font("Arial", Font.PLAIN, 35));
                    g.drawString("K", xStart + x*XSIZE/8+5 + XSIZE/16 - g.getFontMetrics().stringWidth("K"), y*YSIZE/8+5 + YSIZE/16 + g.getFontMetrics().getHeight()/4);
                    g.setFont(new Font("Arial", Font.PLAIN, 15));
                }
            }
        }
		if (inBounds(POI[0], 0)){
            g.setColor(new Color(25, 235, 25, 75));
            int dir = isPositive(move);
            for (int i=0;i<attackPos.length;i+=2){
                if (Math.abs(board[POI[0]][POI[1]]) != 2 && i>=4){
                    break; // cant go backwards until its a king piece
                }
                if (attackPos[i] != -1 && attackPos[i+1] != -1){
                    g.fillRect(xStart + attackPos[i] * XSIZE/8, attackPos[i+1]*YSIZE/8, XSIZE/8, YSIZE/8);
                    if (debugBool){
                        g.setColor(Color.white);
                        g.drawString(attackPos[i] + ":" + attackPos[i+1], xStart + attackPos[i] * XSIZE/8, attackPos[i+1]*YSIZE/8);
                        g.setColor(new Color(25, 235, 25, 75));
						System.out.println(attackPos[i] + ", " + attackPos[i+1] + " drawing");
                    }
                }
            }
			
        }
        if (lockJump){
            endJumpString.setVisible(true);
        }
		else{
			endJumpString.setVisible(false);
		}
		if (winner != -1){
			g.setFont(new Font("Arial", Font.PLAIN, 35));
			g.setColor(Color.white);
			g.fillRect(xStart+XSIZE/2-g.getFontMetrics().stringWidth("Winner: Player: " + winner)/2, YSIZE/2-10 - g.getFontMetrics().getHeight(), g.getFontMetrics().stringWidth("Winner: Player: " + winner), g.getFontMetrics().getHeight());
            g.setColor(Color.black);
            g.drawString("Winner: Player " + winner, xStart+XSIZE/2 - g.getFontMetrics().stringWidth("Winner: Player: " + winner)/2, YSIZE/2 - g.getFontMetrics().getHeight()/2);
            g.setFont(new Font("Arial", Font.PLAIN, 15));
            //return;
        }
		endJumpString.draw(g);
	}
	public void drawIcon(Graphics g, int x, int y, int width, int length){ //draws the icon while selecting the game
        g.setColor(Color.white);
        g.drawRect(x, y, width, length);
		
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.setColor(Color.white);
		drawStackString(g, "Checkers", x+5, y+5, width);
		
		g.setColor(Color.darkGray);
		g.fillRect(x+3, y+length/2, width/2-4, length/2-2);
		
		g.setColor(Color.lightGray);
		g.fillRect(x + width/2+2, y+length/2, width/2-4, length/2-2);
		
		g.setColor(Color.red);
		g.fillOval(x+3, y+length/2, width/2-4, length/2-2);
		
		g.setColor(Color.black);
		g.fillOval(x + width/2+2, y+length/2, width/2-4, length/2-2);
    }
	public void drawIcon(Scalable g, int x, int y, int width, int length){ //draws the icon while selecting the game
        g.setColor(Color.white);
        g.drawRect(x, y, width, length);
		
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.setColor(Color.white);
		drawStackString(g, "Checkers", x+5, y+5, width);
		
		g.setColor(Color.darkGray);
		g.fillRect(x+3, y+length/2, width/2-4, length/2-2);
		
		g.setColor(Color.lightGray);
		g.fillRect(x + width/2+2, y+length/2, width/2-4, length/2-2);
		
		g.setColor(Color.red);
		g.fillOval(x+3, y+length/2, width/2-4, length/2-2);
		
		g.setColor(Color.black);
		g.fillOval(x + width/2+2, y+length/2, width/2-4, length/2-2);
    }
	public void onClick(MouseEvent e){ // mouse button clicked
		//puts x and y in terms of board[][]
		int x=(e.getX()-(xStart+6))/(XSIZE/8), y = (e.getY()-30)/(YSIZE/8);
		if (lockJump && endJumpString.click(e.getX(), e.getY())){
			move = nextMove(move);
			lockJump=false;
			if (cH != null) cH.send(new byte[] {0, -1, -1, -1, -1, 0, -1, this.getGameID()});
			System.out.println("sent");
		}
		
		if (!inBounds(x,y)){
			return; //out of bounds, dont do logic
		}
		
		if (POI[0] == -1 && ((e.getX()-206)/(XSIZE/8) + 8*e.getY()/YSIZE)%2 != 1){
			//select a new piece of intrest
			if ((board[x][y] != 0 ? (Math.abs(board[x][y])/board[x][y] == move) : false) && !lockJump){
				POI= new int[] {x, y}; //saves the piece of intrest that will move
				//save the possible attack positons 
				attackPos = getAttackPos(POI[0], POI[1]);
				
			}
		}
		else{
			//actually move a piece
			for (int i=0;i<attackPos.length;i+=2){
				if (inBounds(POI[0], POI[1]) ? (Math.abs(board[POI[0]][POI[1]]) != 2 && i>=4) : false){
					break; // cant go backwards until its a king piece
				}
				if (x == attackPos[i] && y == attackPos[i+1] && move == -1){
					int result = moveInt(POI[0], POI[1], x, y, false, true);
					if (debugBool) System.out.println(result);
					break;
				}
			}
			POI=new int[] {x, y};
			attackPos = getAttackPos(POI[0], POI[1]);
		}
		if (inBounds(POI[0], POI[1]) ? board[POI[0]][POI[1]] >= 0 : true){ //piece of intrest is not a piece or its the enemies piece
			POI = new int[] {-1, -1};
		}
		if (inBounds(POI[0], POI[1]) && !lockJump){ //keep values in bounds
			if (isPositive(board[POI[0]][POI[1]]) != move){
				POI = new int[] {-1, -1};
			}
		}
		
	}
	public void mousePressed(MouseEvent e){} // mouse button pressed
    public void mouseReleased(MouseEvent e){} // mouse button relased
    public void mouseEntered(MouseEvent e){} // mouse entered screen
    public void mouseExited(MouseEvent e){} // mouse exited screen
    public void keyPressed(KeyEvent e){} // key is pressed
    public void keyReleased(KeyEvent e){} //key is relased
    public void typed(KeyEvent e){} //key is typed
    public boolean isOver(){ //returns true if the game is over
		return winner() != -1;
    }
    public boolean validate(String moves){ //returns true if the last move is valid
        return true;
    }
    public int winner(){ //returns who the winner is - -1=no one - 1=player 1 - 2=player 2
		winner = -1;
        boolean no1=true, no2=true;
        for (int x=0;x<board.length;x++){
            for (int y=0;y<board[x].length;y++){
				if (debugBool) System.out.println(x + ", " + y + ", " + isPositive(board[x][y]) + ", " + board[x][y] + ", " + getAttackPosInt(x,y) + ", " + (getAttackPosInt(x,y)%4));
                if (isPositive(board[x][y]) == 1 && (Math.abs(board[x][y]) == 2 ? getAttackPosInt(x, y) != 0 : getAttackPosInt(x,y)%4 != 0)){
                    no1 = false;
                }
                else if (isPositive(board[x][y]) == -1 && (Math.abs(board[x][y]) == 2 ? getAttackPosInt(x, y) != 0 : getAttackPosInt(x,y)%4 != 0)){
                    no2 = false;
                }
            }
        }
        if (no1 && !no2){ //werid names cause old code and don't wanna rename it all
			winner = 1;
            return 1;
        }
        if (no2 && !no1){
			winner = 2;
            return 2;
        }
        return -1;
    }
	public String getMoves(){ //returns a list of moves of the current game
        return moves;
    }
	public void reset(){ //resets all the game states
        move = -1;
        moves = "Checkers;" + this.getDate() + ";";
        POI = new int[] {-1, -1, 0, 0}; //stores the x,y of piece and the two possible x locations it might be able to move to
        attackPos = new int[4];
        lockJump=false;
        winner=-1;
        board = new int[8][8];
		//user>0
		//opponent<0
        for (int x=0;x<8;x++){
            for (int y=0;y<8;y++){
                if (y<3 && (x+y)%2==0){
                    board[x][y] = 1;
                }
                if (y>=5 && (x+y)%2==0){
                    board[x][y] = -1; 
                }
            }
        }
    }
	public void reset(String boardPreSet){
		//there is no different board set up
		reset();
	}
	public void handleMessage(byte[] nums){
		if (nums[0] == 0 && nums[7] == this.getGameID()){
			//move
			if (nums[1] == -1){
				//recieved a next move signal
				move = nextMove(move);
				cH.remove();
				return;
			}
			int res = moveInt(nums[1], nums[2], nums[3], nums[4], false, false);
			if (res > 0) cH.send(new byte[] {3, 1, 0, (byte)res, -1, -1, -1, -1});
			if (nums[5] < 1 && res == -1){
				//jumps if any are over
				move = nextMove(move);
			}
		}
	}
	public boolean move(int x, int y, int x2, int y2, boolean override, boolean usersTurn){ //satisfy Game functions
        int result = moveInt(x,y,x2,y2, override, usersTurn);
		if (debugBool) System.out.println("bool move(int, int, int, int, bool, bool):" + result);
		return result <= 0; //for debug purposes
    }
	public void debug(){
		debugBool = !debugBool;
	}
	public boolean keyboardSupport(){
		return false;
	}
	public boolean mouseSupport(){
		return true;
	}
	public int getBoardWidth(){ //returns the width of the board for replayManager's use
		return 8;
	}
	public int getBoardHeight(){ //returns the height of the board for replayerManager's use
		return 8;
	}
	public String getBoardAt(int x, int y){ //returns the value at a specific part of the board
		double xTemp = ((x-(xStart+6))/(XSIZE/8))/2.0;
		if ((xTemp+((y+1)%2)*0.5)%1 == 0.5){
			x = (int)(xTemp); //only set x to be a value when its on an actual piece
		}
		return inBounds(x, y) ? board[x][y] + "" : "NULL";
	}
	public boolean setBoardAt(int x, int y, String item){ //sets the value of the board at a certain spot to a specified value, returns weather successful
		double xTemp = ((x-(xStart+6))/(XSIZE/8))/2.0;
		if ((xTemp+((y+1)%2)*0.5)%1 == 0.5){
			x = (int)(xTemp); //only set x to be a value when its on an actual piece
		}
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
		return 2;
	}
	public String getGameName(){
		return "Checkers";
	}
	public int[] version(){
		return new int[] {1,0,0};
	}
	public void setUsersMove(boolean move){
		if (move) this.move = -1;
		else this.move = 1;
	}
	public boolean isUsersMove(){
		return this.move == -1;
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
		
		for (int x=0;x<board.length;x++){
			for (int y=0;y<board[0].length;y++){
				if (board[x][y] > 0 && usersMove){
					//user's piece
					
				}
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
	public boolean isStandardMove(){
		return false;
	}
	private int isPositive(int val){
        if (val != 0) return Math.abs(val)/val; //im doing this so much already to make sure the pieces moving are correct, just gonna make it a function now
        return 0; //to solve the divide by 0 errors
    }
    private int moveInt(int x, int y, int x2, int y2, boolean override, boolean usersTurn){ //The logic of what should happen given a move on this location returns ints for error codes
        //xs, ys in board[][] terms
        int yDiff = Math.abs(y-y2);
        //error code returns for debugging purposes
        if (y==y2){
            //the y's have to be different
            //the y's have to move in the correct direction
            //the coords have to be in the game space
            return 1;
        }
        else if (yDiff == 0){
            return 2;
        }
        if (!inBounds(x,y)){
            return 3;
        }
        if (!inBounds(x2,y2)){
            return 4;
        }
        else if (isPositive(board[x][y]) != move && !override){
            //can only move your own pieces
            return 12; 
        }
        else if (board[x2][y2] != 0){
            //destination has to be empty
            return 13;
        }
		if (yDiff == 1 && !lockJump){
			//normal move, x diff is 1
			board[x2][y2] = board[x][y];
            board[x][y] = 0;
            moves += x+":"+y+":"+x2+":"+y2;

            //upgrade the piece to a king
            if (((usersTurn && y2 == 7) || (!usersTurn && y2 == 0)) && Math.abs(board[x2][y2]) != 2){
                board[x2][y2] = move*2;
                moves+=":U:"+board[x2][y2];
            }
            moves += ";";
            move = nextMove(move);
            winner=winner();
			if (usersTurn) cH.send(new byte[] {0, (byte)(7-x), (byte)(7-y), (byte)(7-x2), (byte)(7-y2), 0, -1, this.getGameID()});
            return 0;
		}
		if (yDiff == 2){
			//jump logic
			int jumpDir = canUserJump(x, y); //save the direction that the piece is jumping
			//1: left front x-1 y+2, 2: right front x+1 y+2, 4: left back x-1 y-2, 8: right back x+1 y-2
			int dir = isPositive(board[x][y]);

			if (debugBool) System.out.println(x + ", " + y + ", " + x2 + ", " + y2 + ", " + override + ", " + usersTurn + ", " + jumpDir + " Move Int debug area");
            if (jumpDir >= 8){
                //Jump to the back right
                jumpDir-=8;
                if (Math.abs(board[x][y]) == 2 && x+2 == x2 && y-2*dir == y2){
                    //has to be a king piece to move backwards
                    board[x2][y2] = board[x][y];
                    board[x][y]=0;
                    moves += x+":"+y+":"+x2+":"+y2+":C:"+(x+1)+":"+(y-dir)+":"+board[x+1][y-dir];
                    board[x+1][y-dir] = 0;
                }
            }
            if (jumpDir >= 4){
                //Jump to the back left
                jumpDir-=4;
                if (Math.abs(board[x][y]) == 2 && x-2 == x2 && y-2*dir == y2){
                    //has to be a king piece to move backwards
                    board[x2][y2] = board[x][y];
                    board[x][y]=0;
					moves += x+":"+y+":"+x2+":"+y2+":C:"+(x-1)+":"+(y-dir)+":"+board[x-1][y-dir];
                    board[x-1][y-dir] = 0;
                }
            }
            if (jumpDir >= 2){
                jumpDir -= 2;
                //Jump to the front right
                if (x+2 == x2 && y+2*dir == y2){
                    board[x2][y2] = board[x][y];
                    board[x][y] = 0;
					moves += x+":"+y+":"+x2+":"+y2+":C:"+(x+1)+":"+(y+dir)+":"+board[x+1][y+dir];
                    board[x+1][y+dir] = 0;
                }
            }
            if (jumpDir == 1){
                //JumpDir can either be 1 or 0 now
                jumpDir=0;
                if (x-2 == x2 && y+2*dir == y2){
                    board[x2][y2] = board[x][y];
                    board[x][y] = 0;
					moves += x+":"+y+":"+x2+":"+y2+":C:"+(x-1)+":"+(y+dir)+":"+board[x-1][y+dir];
                    board[x-1][y+dir] = 0;
                }
            }

            //upgrade the piece to a king
            if ((!usersTurn && y2 == 7) || (usersTurn && y2 == 0)){
                board[x2][y2] = move*2;
                moves += ":U:"+board[x2][y2];
            }

            if (canUserJump(x2, y2) == 0 && usersTurn){ //no more jumps to do in a row
                move = nextMove(move);
                lockJump=false;
            }
			else if (!override && usersTurn){ //there are some jumps the player might want to take
                lockJump = true; //make sure the player can only jump the current piece
            }
            winner=winner();
            moves +=";";
			if (usersTurn) cH.send(new byte[] {0, (byte)(7-x), (byte)(7-y), (byte)(7-x2), (byte)(7-y2), (byte)(lockJump ? 1 : 0), -1, this.getGameID()});
            return -1;
		}
        //No logic was fired, somehow
        return 30;
	}
	private int[] getAttackPos(int x, int y){
		//the data being sent back
        int[] send = new int[8];
        for (int i=0;i < 8; i++){
            send[i] = -1; //preset all possible moves to none
        }
		if (!inBounds(x,y)){
			return send; //full of none values (-1)'s
		}
		if (board[x][y] == 0){
			return send; //thers nothing to move
		}
		int dir = isPositive(board[x][y]); // easy access of direction of movement for the piece
		
		//if (debugBool) System.out.println("\n\ngetAttackPos(int,int) debug area on:(" + x + "," + y + ") is <" + (inBounds(x,y)?board[x][y]:"OoB") + ">\n");
		
		send[0] = x+1;
        send[2] = x-1;
		for (int i=0;i<2;i++){
			if (inBounds(send[i*2], y+dir) ? board[send[i*2]][y+dir] == 0 : false){
				send[i*2+1] = y+dir;
				//if (debugBool) System.out.print(send[i*2] + ", " + send[i*2+1] + "----");
			}
		}
		
        if (Math.abs(board[x][y]) == 2){ //only able to move backwards with king pieces
            send[4] = x+1;
            send[6] = x-1;
			for (int i=2;i<4;i++){
				if (inBounds(send[i*2], y-dir) ? board[send[i*2]][y-dir] == 0 : false){
					send[i*2+1] = y-dir;
					//if (debugBool) System.out.print(send[i*2] + ", " + send[i*2+1] + " ----  ");
				}
			}
		}
		//if(debugBool) System.out.println(x + ", " + y);
		
		//jump logic
		int jumps = canUserJump(x, y);
        if (jumps >= 8){
            jumps-=8; //x+2, y-2
			
            send[4] = x+2;
			send[5] = y-2*dir;
        }
		if (jumps >= 4){
			jumps-=4; //x-2, y-2
			
			send[6] = x-2;
			send[7] = y-2*dir;
		}
		if (jumps >= 2){
			jumps-=2; //x+2, y+2
			
			send[0] = x+2;
			send[1] = y+2*dir;
		}
		if (jumps == 1){ //it can only be 1 now
			jumps=0; //x-2, y+2
			
			send[2] = x-2;
			send[3] = y+2*dir;
		}
		//set to -1 if not in bounds
        if (!inBounds(send[0], send[1])){
            send[0] = -1;
        }
        if (!inBounds(send[2], send[3])){
            send[2] = -1;
        }
        if (!inBounds(send[4],send[5])){
            send[4] = -1;
        }
        if (!inBounds(send[6],send[7])){
            send[6] = -1;
        }
		if (debugBool && false){
            for (int i=0;i<send.length;i++){
                System.out.print(send[i] + ", ");
            }
            System.out.println("\n");
        }
        return send;
    }
	private int canUserJump(int x, int y){
        //will return an int based on which direction the jump can occur, 0 for no jump
        //1: left front, 2: right front, 4: left back, 8: right back
        //int dir = Math.abs(move)/move;
        int send = 0;
        //make sure the x, y are in the bounds of the board
        if (!inBounds(x,y)){
            return 0; //no jump - out of bounds
        }
		int dir = isPositive(board[x][y]);

		//jumping forwards
		//check to make sure both squares in bounds, the first square has an enemy, the second square is empty
		if ((inBounds(x-1, y+2*dir) && inBounds(x-2,y+2*dir)) ? (isPositive(board[x-1][y+dir]) == -1*isPositive(board[x][y]) && board[x-2][y+2*dir] == 0):false){
			send=1; //x-2, y+2
		}
		if ((inBounds(x+1, y+2*dir) && inBounds(x+2,y+2*dir)) ? (isPositive(board[x+1][y+dir]) == -1*isPositive(board[x][y]) && board[x+2][y+2*dir] == 0):false){
			send+=2; //x+2, y+2
		}
		
		if (Math.abs(board[x][y]) != 2) return send; //is not a king, cannot jump backwards, send jumps prematurely
		
		//jumpingBackwards
		if ((inBounds(x-1, y-2*dir) && inBounds(x-2,y-2*dir)) ? (isPositive(board[x-1][y-dir]) == -1*isPositive(board[x][y]) && board[x-2][y-2*dir] == 0):false){
			send += 4; //x-2, y-2
		}
		if ((inBounds(x+1, y-2*dir) && inBounds(x+2,y-2*dir)) ? (isPositive(board[x+1][y-dir]) == -1*isPositive(board[x][y]) && board[x+2][y-2*dir] == 0):false){
			send += 8; //x+2, y-2
		}
        //debug insertion area:
        if (debugBool) System.out.println(x + ", " + y + ", " + dir + " :: " + send + ", canUserJump debug area");
        return send;
    }
    private int getAttackPosInt(int x, int y){
        int[] temp = getAttackPos(x, y);
        int send = 0;
        for (int i=0;i<temp.length;i+=2){
            if (temp[i] != -1){
                send += Math.pow(2,i/2);
            }
        }
        return send;
    }
    private boolean inBounds(int x, int y){
        return x>=0 && x<8 && y>=0 && y<8;
    }
	private int nextMove(int currentMove){
        //this equation is very long and multiple iterations have been found all over, this function just shrinks dev error
        return (int)(Math.floor(currentMove/2.0)*-2-1);
    }
	
	//Replayable methods - taken from replayManager from old code, so, roundabout in execution
	public String goForwards(int cursor, String[] moves) throws NumberFormatException{
		String[] tempMoves = moves[cursor].split(":");
		int x = Integer.parseInt(tempMoves[0]),
			y = Integer.parseInt(tempMoves[1]),
			x2 = Integer.parseInt(tempMoves[2]),
			y2 = Integer.parseInt(tempMoves[3]);
		boolean res = this.move(x,y,x2,y2,true,cursor%2==1);
		for (int i=3;i<tempMoves.length;i++){
			if (Character.isAlphabetic(tempMoves[i].charAt(0))){
				if (tempMoves[i].charAt(0) == 'U'){
					this.setBoardAt(x2,y2, tempMoves[i+1]);
				}
			}
		}
		return "(" + x + ", "+ y + ") moves to (" + x2 + ", " + y2 + ")";
	}
	public String goBack(int cursor, String[] moves) throws NumberFormatException{
		String[] tempMoves = moves[cursor].split(":");
		int x = Integer.parseInt(tempMoves[0]),
			y = Integer.parseInt(tempMoves[1]),
			x2 = Integer.parseInt(tempMoves[2]),
			y2 = Integer.parseInt(tempMoves[3]);
		this.setBoardAt(x, y, this.getBoardAt(x2, y2));
		this.setBoardAt(x2, y2, "0");
		for (int i=3;i<tempMoves.length;i++){
			if (Character.isAlphabetic(tempMoves[i].charAt(0))){
				if (tempMoves[i].charAt(0) == 'U'){
					this.setBoardAt(x,y, ""+(Integer.parseInt(tempMoves[i+1])/2));
				}
				if (tempMoves[i].charAt(0) == 'C'){
					int x3 = Integer.parseInt(tempMoves[i+1]),
						y3 = Integer.parseInt(tempMoves[i+2]);
					this.setBoardAt(x3, y3, tempMoves[i+3]);
				}
			}
		}
		return "Undoing (" + x + ", "+ y + ") moving to (" + x2 + ", " + y2 + ")";
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
		drawIcon(g, XSTART+5+x, 5+y, XSIZE/3-10, YSIZE/3-10);
	}
	public Rectangle toRect(){
		return new Rectangle(XSTART+5+x, 5+y, XSIZE/3-10, YSIZE/3-10);
	}
	public void setPos(int x, int y){
		this.x = x;
		this.y = y;
	}
}