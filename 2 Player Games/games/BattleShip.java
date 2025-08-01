package games;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Font;
import helpful.Rectangle;
import helpful.GUI.Button;
import helpful.GUI.Text;
import helpful.datastructures.DLList;
import helpful.Scalable;

public class BattleShip implements Game, Replayable{
	private byte[][] self, opponent, shipPlacements; //saves the ships and their hits of both screens
	//encoding method: 0 - empty, 1 - horizontal ship, 2 - vertical ship, 3 - hori ship end Left, 4 - hori ship end right, 5 - verti ship end top, 6 - verti ship end bottom, 7 - no ship - miss, +8 - hit
	private byte cursor, currentShip, winner, animationFrame; //currentShip encoding: 0 = scout(2), 1 = sub(3), 2=other(3), 3=battleship(4), 4=aircraftCarriers(5)
	final private byte[] shipLengths = new byte[] {2,3,3,4,5}; //makes it easier to find the ships length during the placement of ships
	private byte[] lastMove, lastOpponentMove; //used when recieving info of whether hit or miss, used to color the last move by the opponent different
	private boolean[] hasShips, opponentsShips, ownShips;
	private boolean showBoard, rotateVertical, isPlacing, usersTurn, removeShip, opponentReady, confirm, hasBoard;
	//are the friendly ships shown?, are the boats rotated vertically?, is the player placing their ships?, is it this user's turn?, should the ship be removed?
	private Button submitMove, left, right, place, remove, rotate, showOpponent; //submit the move, rotate ship choice left, rotate ship choice right, place ship mode, remove ship mode
	private String moves;
    private final int wid = XSIZE/10, len = YSIZE/10;
	private ConnectionHandler cH;
	//Component Variables
	private boolean isVisible;
	private int x, y;
	private Text oppReady;
	public BattleShip(int index){
		submitMove = new Button(15, 330, 170, 50, "Submit Placements");
		left = new Button(15, 150, 35, 80, "<");
		right = new Button(150, 150, 35, 80, ">");
		place = new Button(15, 240, 77, 30, Color.green, "Place");
		remove = new Button(108, 240, 77, 30, Color.red, "Remove");
		rotate = new Button(15, 285, 170, 30, "Rotate");
		showOpponent = new Button(15, 420, 170, 40, Color.red, "Show Opponent's board");
		oppReady = new Text(15, 110, 170, 40, "Opponent Ready", Text.CENTERED);
		oppReady.setColors(Color.cyan, Color.red);
		isVisible = true;
		x = (index%3)*XSIZE/3;
		y = (index/3)*YSIZE/3;
		reset();
	}
	public void drawGame(Graphics g){ //draws the board
		int xStart = 200;
		animationFrame = (byte)((animationFrame + 1) % 54); 
		g.setColor(isOver() ? Color.red : Color.green);
		g.fillRect(15, 15, 50, 50);
		if (showBoard){
			g.setColor(Color.black);
			g.fillRect(xStart, 0, XSIZE, YSIZE);
			
			//the opponent is ready but user is not
			if (isPlacing && opponentReady)
				oppReady.draw(g);
			
			//drawing the friendly ships
			for (int x=0;x<self.length;x++){
				for (int y=0;y<self[0].length;y++){
					boolean isHit = false;
					if (self[x][y] > 8){
						isHit = true;
						self[x][y]-=8; //make the data easier to work with
					}
					//boxes are going to have 6-pix padding
					drawItem(g, xStart+x*wid+3, y*len+3, "board");
					if (self[x][y] != 0 && self[x][y] != 7){
						//its a ship
						g.setColor(Color.lightGray);
					}
                    drawItem(g, xStart+x*wid+3, y*len+3, self[x][y] + "" + (isHit ? "h":" ")); //draws the proper ship
					if (isHit){
						//make sure the data is not changed after the draw function
						self[x][y]+=8;
					}
				}
			}
			//draw the last opponent's move
			if (inBounds(lastOpponentMove[0], lastOpponentMove[0]) && cH != null){ //only draw latest move in normal gameplay, not replay mode
				//exists on the board
				g.setColor(new Color(255, 255, 25, 128));
				g.fillRect(xStart + lastOpponentMove[0]*wid, lastOpponentMove[1]*len, wid, len); //draw the transparent square over the last move
			}
			//draw the cursor only if placing
			if (isPlacing) {
				//draw the outline where the ship will be placed as the cursor
				boolean hasAShip = false;
				for (boolean temp : hasShips){
					if (temp){
						hasAShip=true;
						break;
					}
				}
				int shipLength = 1;
				if (hasAShip && isPlacing){
					shipLength = shipLengths[currentShip];
				}
				
				g.setColor(new Color(255, 128, 25, 128));
				byte xMult = (byte)(rotateVertical ? 0:1);
				byte yMult = (byte)(rotateVertical ? 1:0);
				for (int i=0;i<shipLength;i++){
					g.fillRect(xStart+(cursor%10+xMult*i)*wid+3, (cursor/10+yMult*i)*len+3, wid-6, len-6);
					g.setColor(new Color(200, 25, 25, 128)); //makes the boxes after the cursor a different color to differentiate between the actual cursor and the ship outline
				}
				
				if (shipLength != 1){ //theres still a ship to place down
					g.setColor(Color.lightGray);
					if (rotateVertical){
						g.fillOval(65, 150, 15, 80);
					}
					else{
						g.fillOval(65, 150, 80, 15);
					}
					g.setFont(new Font("Arial", Font.PLAIN, 60));
					g.drawString(shipLength + "", 95, 225);
				}
			}
		}
		if (!showBoard){ //draw the opponent board
			g.setColor(Color.black);
			g.fillRect(xStart, 0, XSIZE, YSIZE);
			if (cH != null){
				//drawing user's hits / misses on enemy board
				for (int x=0;x<self.length;x++){
					for (int y=0;y<self[0].length;y++){
						//boxes are going to have 6-pix padding
						drawItem(g, xStart+x*wid+3, y*len+3, "board");
						int[] xPoints = {xStart + x*wid+3, xStart + x*wid+3 + animationFrame, xStart + (x+1)*wid-3, xStart + (x)*wid + wid-animationFrame-3};
						int[] yPoints = {y*len + animationFrame+3, (y+1)*len-3, (y)*len + len-animationFrame-3, y*len+3};
						if (opponent[x][y] == 8){ //hit
							g.setColor(Color.red);
							g.fillPolygon(xPoints, yPoints, xPoints.length); //draws the rotating square
							//byte randomNoise = (byte)(Math.random()*6+1);
							//drawItem(g, xStart+x*wid+3, y*len+3, randomNoise + "h");
						}
						if (opponent[x][y] == 7){ //miss
							g.setColor(Color.white);
							g.fillPolygon(xPoints, yPoints, xPoints.length); //draws the rotating square
							//drawItem(g, xStart+x*wid+3, y*len+3, "7");
						}
					}
				}
				//draw the cursor
				g.setColor(new Color(255, 128, 25, 128));
				g.fillRect(xStart+(cursor%10)*wid+3, (cursor/10)*len+3, wid-6, len-6);
			}
			else{ //drawing the enemies ships
				for (int x=0;x<opponent.length;x++){
					for (int y=0;y<opponent[0].length;y++){
						boolean isHit = false;
						if (opponent[x][y] > 8){
							isHit = true;
							opponent[x][y]-=8; //make the data easier to work with
						}
						//boxes are going to have 6-pix padding
						drawItem(g, xStart+x*wid+3, y*len+3, "boardEVIL");
						if (opponent[x][y] != 0 && opponent[x][y] != 7){
							//its a ship
							g.setColor(Color.lightGray);
						}
						drawItem(g, xStart+x*wid+3, y*len+3, opponent[x][y] + "" + (isHit ? "h":" ")); //draws the proper ship
						if (isHit){
							//make sure the data is not changed after the draw function
							opponent[x][y]+=8;
						}
					}
				}
			}
		}
		for (int i=0;i<self.length;i++){
			Text item = new Text(203+i*wid, YSIZE, wid-6, 27, i+"", Text.CENTERED);
			item.setColors(new Color(200, 175, 25), Color.blue);
			item.draw(g);
			item.setDims(200+XSIZE, 3+i*len, 27, len-6);
			item.draw(g);
		}
		
		if (!isOver()) submitMove.draw(g);
		if (opponentReady) showOpponent.draw(g);
		if (isPlacing){ //draw the buttons to place ships
			//currently placing the ships down
			left.draw(g);
			right.draw(g);
			place.draw(g);
			remove.draw(g);
			rotate.draw(g);
		}
		else if (!isOver() && opponentReady && cH != null){
			//NOTE: isPlacing = false
			g.setFont(new Font("Arial", Font.PLAIN, 25));
			g.setColor(Color.lightGray);
			g.drawString("Ships sunk:", 100 - g.getFontMetrics().stringWidth("Ships sunk:")/2, 310 + g.getFontMetrics().getHeight());
			
			g.drawString("Ships Left:", 100-g.getFontMetrics().stringWidth("Ships Left:")/2, 380 + g.getFontMetrics().getHeight());
			
			boolean[] userShips = checkShips();
			Text drawText = new Text(15, 325, (200-30)/5, 30, "1", Text.CENTERED);
			Color textColor = Color.black;
			Color bkgColor = Color.red;
			
			//Ships sunk display
			g.setFont(new Font("Arial", Font.PLAIN, 20));
			for (int i=0;i<opponentsShips.length;i++){
				
				if (opponentsShips[i]) bkgColor = Color.green; //ship was sunk
				else bkgColor = Color.red; //ship still exists
				drawText.setPos(15 + i*drawText.toRect().getWidth(), 310 + g.getFontMetrics().getHeight()+15);
				drawText.setColors(textColor, bkgColor);
				drawText.setText(shipLengths[i] + "");
				drawText.draw(g);
				
				if (userShips[i]) bkgColor = Color.red;
				else bkgColor = Color.green;
				drawText.setPos(15 + i*drawText.toRect().getWidth(), 380 + g.getFontMetrics().getHeight()+15);
				drawText.setColors(textColor, bkgColor);
				drawText.setText(shipLengths[i] + "");
				drawText.draw(g);
			}
		}	
	}
	public void drawGame(Scalable g){ //draws the board
		int xStart = 200;
		animationFrame = (byte)((animationFrame + 1) % 54); 
		g.setColor(isOver() ? Color.red : Color.green);
		g.fillRect(15, 15, 50, 50);
		if (showBoard){
			g.setColor(Color.black);
			g.fillRect(xStart, 0, XSIZE, YSIZE);
			
			//the opponent is ready but user is not
			if (isPlacing && opponentReady)
				oppReady.draw(g);
			
			//drawing the friendly ships
			for (int x=0;x<self.length;x++){
				for (int y=0;y<self[0].length;y++){
					boolean isHit = false;
					if (self[x][y] > 8){
						isHit = true;
						self[x][y]-=8; //make the data easier to work with
					}
					//boxes are going to have 6-pix padding
					drawItem(g, xStart+x*wid+3, y*len+3, "board");
					if (self[x][y] != 0 && self[x][y] != 7){
						//its a ship
						g.setColor(Color.lightGray);
					}
                    drawItem(g, xStart+x*wid+3, y*len+3, self[x][y] + "" + (isHit ? "h":" ")); //draws the proper ship
					if (isHit){
						//make sure the data is not changed after the draw function
						self[x][y]+=8;
					}
				}
			}
			//draw the last opponent's move
			if (inBounds(lastOpponentMove[0], lastOpponentMove[0]) && cH != null){ //only draw latest move in normal gameplay, not replay mode
				//exists on the board
				g.setColor(new Color(255, 255, 25, 128));
				g.fillRect(xStart + lastOpponentMove[0]*wid, lastOpponentMove[1]*len, wid, len); //draw the transparent square over the last move
			}
			//draw the cursor only if placing
			if (isPlacing) {
				//draw the outline where the ship will be placed as the cursor
				boolean hasAShip = false;
				for (boolean temp : hasShips){
					if (temp){
						hasAShip=true;
						break;
					}
				}
				int shipLength = 1;
				if (hasAShip && isPlacing){
					shipLength = shipLengths[currentShip];
				}
				
				g.setColor(new Color(255, 128, 25, 128));
				byte xMult = (byte)(rotateVertical ? 0:1);
				byte yMult = (byte)(rotateVertical ? 1:0);
				for (int i=0;i<shipLength;i++){
					g.fillRect(xStart+(cursor%10+xMult*i)*wid+3, (cursor/10+yMult*i)*len+3, wid-6, len-6);
					g.setColor(new Color(200, 25, 25, 128)); //makes the boxes after the cursor a different color to differentiate between the actual cursor and the ship outline
				}
				
				if (shipLength != 1){ //theres still a ship to place down
					g.setColor(Color.lightGray);
					if (rotateVertical){
						g.fillOval(65, 150, 15, 80);
					}
					else{
						g.fillOval(65, 150, 80, 15);
					}
					g.setFont(new Font("Arial", Font.PLAIN, 60));
					g.drawString(shipLength + "", 95, 225);
				}
			}
		}
		if (!showBoard){ //draw the opponent board
			g.setColor(Color.black);
			g.fillRect(xStart, 0, XSIZE, YSIZE);
			if (cH != null){
				//drawing user's hits / misses on enemy board
				for (int x=0;x<self.length;x++){
					for (int y=0;y<self[0].length;y++){
						//boxes are going to have 6-pix padding
						drawItem(g, xStart+x*wid+3, y*len+3, "board");
						int[] xPoints = {xStart + x*wid+3, xStart + x*wid+3 + animationFrame, xStart + (x+1)*wid-3, xStart + (x)*wid + wid-animationFrame-3};
						int[] yPoints = {y*len + animationFrame+3, (y+1)*len-3, (y)*len + len-animationFrame-3, y*len+3};
						if (opponent[x][y] == 8){ //hit
							g.setColor(Color.red);
							g.fillPolygon(xPoints, yPoints, xPoints.length); //draws the rotating square
							//byte randomNoise = (byte)(Math.random()*6+1);
							//drawItem(g, xStart+x*wid+3, y*len+3, randomNoise + "h");
						}
						if (opponent[x][y] == 7){ //miss
							g.setColor(Color.white);
							g.fillPolygon(xPoints, yPoints, xPoints.length); //draws the rotating square
							//drawItem(g, xStart+x*wid+3, y*len+3, "7");
						}
					}
				}
				//draw the cursor
				g.setColor(new Color(255, 128, 25, 128));
				g.fillRect(xStart+(cursor%10)*wid+3, (cursor/10)*len+3, wid-6, len-6);
			}
			else{ //drawing the enemies ships
				for (int x=0;x<opponent.length;x++){
					for (int y=0;y<opponent[0].length;y++){
						boolean isHit = false;
						if (opponent[x][y] > 8){
							isHit = true;
							opponent[x][y]-=8; //make the data easier to work with
						}
						//boxes are going to have 6-pix padding
						drawItem(g, xStart+x*wid+3, y*len+3, "boardEVIL");
						if (opponent[x][y] != 0 && opponent[x][y] != 7){
							//its a ship
							g.setColor(Color.lightGray);
						}
						drawItem(g, xStart+x*wid+3, y*len+3, opponent[x][y] + "" + (isHit ? "h":" ")); //draws the proper ship
						if (isHit){
							//make sure the data is not changed after the draw function
							opponent[x][y]+=8;
						}
					}
				}
			}
		}
		for (int i=0;i<self.length;i++){
			Text item = new Text(203+i*wid, YSIZE, wid-6, 27, i+"", Text.CENTERED);
			item.setColors(new Color(200, 175, 25), Color.blue);
			item.draw(g);
			item.setDims(200+XSIZE, 3+i*len, 27, len-6);
			item.draw(g);
		}
		
		if (!isOver()) submitMove.draw(g);
		if (opponentReady) showOpponent.draw(g);
		if (isPlacing){ //draw the buttons to place ships
			//currently placing the ships down
			left.draw(g);
			right.draw(g);
			place.draw(g);
			remove.draw(g);
			rotate.draw(g);
		}
		else if (!isOver() && opponentReady && cH != null){
			//NOTE: isPlacing = false
			g.setFont(new Font("Arial", Font.PLAIN, 25));
			g.setColor(Color.lightGray);
			g.drawString("Ships sunk:", 100 - g.getFontMetrics().stringWidth("Ships sunk:")/2, 310 + g.getFontMetrics().getHeight());
			
			g.drawString("Ships Left:", 100-g.getFontMetrics().stringWidth("Ships Left:")/2, 380 + g.getFontMetrics().getHeight());
			
			boolean[] userShips = checkShips();
			Text drawText = new Text(15, 325, (200-30)/5, 30, "1", Text.CENTERED);
			Color textColor = Color.black;
			Color bkgColor = Color.red;
			
			//Ships sunk display
			g.setFont(new Font("Arial", Font.PLAIN, 20));
			for (int i=0;i<opponentsShips.length;i++){
				
				if (opponentsShips[i]) bkgColor = Color.green; //ship was sunk
				else bkgColor = Color.red; //ship still exists
				drawText.setPos(15 + i*drawText.toRect().getWidth(), 310 + g.getFontMetrics().getHeight()+15);
				drawText.setColors(textColor, bkgColor);
				drawText.setText(shipLengths[i] + "");
				drawText.draw(g);
				
				if (userShips[i]) bkgColor = Color.red;
				else bkgColor = Color.green;
				drawText.setPos(15 + i*drawText.toRect().getWidth(), 380 + g.getFontMetrics().getHeight()+15);
				drawText.setColors(textColor, bkgColor);
				drawText.setText(shipLengths[i] + "");
				drawText.draw(g);
			}
		}	
	}
	public void onClick(MouseEvent e){ // mouse button clicked
        boolean hasAShip = false;
        for (boolean temp : hasShips){
            if (temp){
                hasAShip = true;
            }
        }
        //can only submit board placement when all the ships are placed down
		if (submitMove.click(e.getX(), e.getY()) && !hasAShip){
			if (isPlacing){
				isPlacing = false;
				//change button's states
				left.setVisible(false);
				right.setVisible(false);
				place.setVisible(false);
				remove.setVisible(false);
				submitMove.setVisible(false);
				submitMove.setPos(15, 260);
				showOpponent.setVisible(true);
				
				
				//send signal to opponent that user is ready, host goes first
				byte[] sendArray = new byte[] {5, 1};
				cH.send(sendArray); //this user is ready
				
				moves += getBoardPreGame(); //saves the board state for replay manager
				submitMove.setText("Fire!");
			}
			else if (!confirm){
				submitMove.setText("Confirm?");
				confirm = true;
			}
			else if (confirm){
				submitMove.setText("Fire!");
				confirm = false;
				attack();
			}
		}
		if (left.click(e.getX(), e.getY()) && isPlacing){
			leftLogic();
		}
		if (right.click(e.getX(), e.getY()) && isPlacing){
			rightLogic();
		}
		if (place.click(e.getX(), e.getY()) && isPlacing){
			placeLogic();
		}
		if (remove.click(e.getX(), e.getY()) && isPlacing){
			removeLogic();
		}
        if (rotate.isClicked(e) && isPlacing){
            rotateVertical = !rotateVertical;
        }
		if (showOpponent.isClicked(e) && !isPlacing && opponentReady){
			if (showBoard){
				showBoard = false; //change to show opponent board
				showOpponent.setText("Show home board");
				if (usersTurn && cH!=null) submitMove.setVisible(true); //allow option to attack
			}
			else{
				showBoard = true;
				showOpponent.setText("Show Opponent's board");
				submitMove.setVisible(false);
			}
		}
	}
	public void mousePressed(MouseEvent e){} // mouse button pressed
	public void mouseReleased(MouseEvent e){} // mouse button relased
	public void mouseEntered(MouseEvent e){} // mouse entered screen
	public void mouseExited(MouseEvent e){} // mouse exited screen
	public void keyPressed(KeyEvent e){ // key is pressed
		//left : 37  up : 38  right : 39  down : 40 r : 82 l : 76
		//WASD cursor movement
		if (e.getKeyCode() == 65 && (isPlacing || (!showBoard))){
			//to the left
			cursor--;
			if (cursor%10 == 9){
				//did a wrap around
				cursor+=10;
			}
			if (cursor < 0) cursor = 9;
			if (confirm){
				confirm = false;
				submitMove.setText("Fire!");
			}
		}
		if (e.getKeyCode() == 87 && (isPlacing || (!showBoard))){
			//above and beyond
			cursor -= 10;
			if (cursor < 0) cursor += 100;
			if (confirm){
				confirm = false;
				submitMove.setText("Fire!");
			}
		}
		if (e.getKeyCode() == 68 && (isPlacing || (!showBoard))){
			//to the right
			cursor++;
			if (cursor > 100) cursor = 90;
			if (cursor%10 == 0){
				//did a wrap around
				cursor-=10;
			}
			if (confirm){
				confirm = false;
				submitMove.setText("Fire!");
			}
		}
		if (e.getKeyCode() == 83 && (isPlacing || (!showBoard))){ //only if placing or on enemy board without confimation message
			//down down down
			cursor += 10;
			if (cursor >= 100) cursor -= 100;
			if (confirm){
				confirm = false;
				submitMove.setText("Fire!");
			}
		}
		if (e.getKeyCode() == 82 && isPlacing){
			//r key pressed
			rotateVertical = !rotateVertical;
		}
		if (e.getKeyCode() == 37 && isPlacing){
			leftLogic(); //keyboard input to change current ship
		}
		if (e.getKeyCode() == 39 && isPlacing){
			rightLogic(); //keyboard input to change current ship
		}
		if (e.getKeyCode() == 80 && isPlacing){
			//p key pressed
			placeLogic(); //keyboard input to place a ship
		}
		if (e.getKeyCode() == 8){
			if (isPlacing){
				removeLogic(); //keyboard input to remove a ship
			}
			else if (confirm){
				submitMove.setText("Fire!");
				confirm = false; //void the current attack position
			}
		}
		if (e.getKeyCode() == 10){
			//enter key pressed
			if (isPlacing){
				placeLogic();
			}
			else if (usersTurn){
				if (!confirm){
					submitMove.setText("Confirm?");
					confirm = true;
				}
				else{
					submitMove.setText("Fire!");
					confirm = false;
					attack();
				}
			}
		}
		if (e.getKeyCode() == 32){
			//space key pressed
			if (isPlacing){
				placeLogic();
			}
			else if (usersTurn){
				if (!confirm){
					submitMove.setText("Confirm?");
					confirm = true;
				}
				else{
					submitMove.setText("Fire!");
					confirm = false;
					attack();
				}
			}
		}
	}
	public void keyReleased(KeyEvent e){} //key is relased
	public void typed(KeyEvent e){} //key is typed
	public boolean isOver(){ //returns true if the game is over
		return winner() != -1;
	}
	public boolean validate(String moves){ //returns true if the last move is valid
		return true;
	}
	public int winner(){ //returns who the winner is - -1=no one - 1=user - 2=opponent
		if (isPlacing) return -1;
		if (cH != null ? cH.isClosed() : false) winner = -1; //replay manager going through the game - reset winner
		if (winner != -1) return winner+1; 
		boolean allTrue = true;
		for (boolean value : checkShips()){
			if (!value) allTrue = false;
		}
		if (allTrue){ //the opponent is the winner
			cH.send(new byte[] {5, -1, -1, 0, -1, -1, -1, -1}); //tells opponent they won
			winner = 1;
			return 2; 
		}
		if (opponentDead()){ //test whether the opponent has had all their ships killed
			if (cH != null) cH.send(new byte[] {5, -1, -1, 1, -1, -1, -1, -1});
			winner = 0; //user is the winner
			return 1;
		}
		return -1;
	}
	public String getMoves(){ //returns a list of moves of the current game
		return moves;
	}
	public void reset(){ //resets all the game states
		self = new byte[10][10];
		opponent = new byte[10][10];
		shipPlacements = new byte[shipLengths.length][3];
		lastMove = new byte[] {-1, -1};
		lastOpponentMove = new byte[] {-1, -1};
		for (int i=0;i<shipPlacements.length;i++){
			shipPlacements[i] = new byte[] {-1, -1, 0};
		}
		
		cursor = 00;
		winner = -1;
		animationFrame = 0;
		showBoard = true;
		rotateVertical = true;
		isPlacing = true;
		removeShip = false;
		opponentReady = false;
		confirm = false; //to confirm the attack square
		hasBoard = false; //to confirm if the opponent has sent their board
		
		place.setColor(Color.green);
		remove.setColor(Color.red);
		
		currentShip = 0;
		opponentsShips = new boolean[shipLengths.length];
		ownShips = new boolean[shipLengths.length];
		hasShips = new boolean[shipLengths.length];
		for (int n=0;n<shipLengths.length;n++){
			hasShips[n] = true;
		}
		moves = "BattleShip;" + this.getDate() + ";";
		usersTurn = true; //will be messed with by 2 computer connection
		
		submitMove.setVisible(true);
		submitMove.setPos(15, 330);
		submitMove.setText("Submit Placements");
		left.setVisible(true);
		right.setVisible(true);
		place.setVisible(true);
		remove.setVisible(true);
		rotate.setVisible(true);
		showOpponent.setVisible(false);
	}
	public void reset(String boardPreSet){
		reset();
		//place the ships with correct rotation
		String[] ships = boardPreSet.split("/");
		//users ships
		String[] placements = ships[0].split(";");
		for (int i=0;i<placements.length;i++){
			String[] vars = placements[i].split(":");
			int x = Integer.parseInt(vars[0]);
			int y = Integer.parseInt(vars[1]);
			boolean vertical = Integer.parseInt(vars[2]) == 1;
			placeLogic(x, y, i, vertical, self);
			shipPlacements[i] = new byte[] {(byte)(x), (byte)(y), (byte)(vertical ? 1:0)};
		}
		//opponent's ships
		placements = ships[1].split(";");
		for (int i=0;i<placements.length;i++){
			String[] vars = placements[i].split(":");
			int x = Integer.parseInt(vars[0]);
			int y = Integer.parseInt(vars[1]);
			boolean vertical = Integer.parseInt(vars[2]) == 1;
			placeLogic(x, y, i, vertical, opponent);
		}
		
		isPlacing = false;
		hasBoard = true;
		opponentReady = true;
		left.setVisible(false);
		right.setVisible(false);
		place.setVisible(false);
		remove.setVisible(false);
		rotate.setVisible(false);
		submitMove.setVisible(false);
		showOpponent.setVisible(true);
	}
	public void handleMessage(byte[] nums){
		//if (cH != null ? (cH.hasReads() && cH.peek() != null ? cH.peek()[0] == 5 : false) : false) return 
		//is a general info sent info
		//ensure it is just the isReady info
		boolean otherFalse = true;
		for (int i=2;i<nums.length;i++){
			if (nums[i] != -1){
				otherFalse = false;
			}
		}
		if (nums[1] >= 1 && otherFalse){
			opponentReady = true; //it was oppnoent saying they're ready
		}
		else if (nums[1] < 1){
			if (nums[7] == 18){ //if its a board request
				sendBoard();
			}
			if (nums[2] != -1){ //if its general info
				if (nums[2] >= 10){
					opponentsShips[nums[2]-10] = true; //that ship has sunk
				}
			
				//hit or miss
				if (!inBounds(lastMove[0], lastMove[1])){ //lastmove wasnt set up properly
					return;
				}
				if (nums[2] == 1){ //hit
					opponent[lastMove[0]][lastMove[1]] = 8;
				}
				if (nums[2] == 0){
					opponent[lastMove[0]][lastMove[1]] = 7;
				}
				lastMove = new byte[] {-1, -1};
				boolean res = opponentDead();
				if (res){
					winner = 0; //the winner is the user
					cH.send(new byte[]{5, -1, -1, 1}); //says the winner is this user, or their opponent
					if (!hasBoard) cH.send(new byte[]{5, -1, -1, -1, -1, -1, -1, 18}); //request the board
				}
			}
			if (nums[3] != -1){ //if its a win/lose info
				winner = nums[3];
				if (!hasBoard) cH.send(new byte[]{5, -1, -1, -1, -1, -1, -1, 18}); //request the board if we don't have it yet
			}
		}
	}
	public boolean move(int x, int y, int x2, int y2, boolean override, boolean usersTurn){ //The logic of what should happen given a move on this location
		//override has no meaningful affect on where things happen
		if ((isPlacing || !opponentReady) && !override) return false; //the game is currently being set up
		if (this.usersTurn != usersTurn && !override) return false; //wrong person is trying to move
		if (!inBounds(x, y)) return false; //out of bounds
		if (isOver() && !override) return false; //the game is over
		
		if (!usersTurn){
			moves += "O:" + x + ":" + y + ";"; //saves the move of opponent
			self[x][y] += (byte)(self[x][y] > 0 && self[x][y] < 7 ? 8 : 7); //sets to hit version or miss
			if (cH!=null) cH.send(new byte[] {5, -1, (byte)(self[x][y] > 8 ? 1 : 0)}); //sends whether the shot hit or missed
			lastOpponentMove = new byte[] {(byte)x, (byte)y}; //save the last move
			if (!showBoard && !override) submitMove.setVisible(true);
			
			boolean[] res = checkShips(); //returns an array of booleans for the user's ships - true = sunk, false = afloat
			boolean allTrue = true;
			//goes through all values and if one value is false, allTrue becomes false
			for (int i=0;i<res.length;i++){ 
				if (!res[i]) allTrue = false;
				if (res[i] != ownShips[i] && res[i] && !override){ //a ship was sunk, tell opponent
					cH.send(new byte[]{5, -1, (byte)(10+i), -1, -1, -1, -1, -1});
					ownShips[i] = true;
				}
			}
			if (allTrue){ //all own ships have been sunk
				//time to surrender
				winner = 1;
				winner();
			}
		}
		
		if (usersTurn){
			moves += "U:" + x + ":" + y + ";"; //saves the move of user
			if (!override) opponent[x][y] = 7; //miss placeholder until proper info comes from opponent
			else{
				opponent[x][y] += (byte)(opponent[x][y] > 0 && opponent[x][y] < 7 ? 8 : 7); //update opponents ships properly for replay manager
			}
			lastMove = new byte[] {(byte)x, (byte)y}; //for hit/miss data collection
			if (cH != null)cH.send(new byte[] {0, (byte)x, (byte)y, -1, -1, -1, -1, this.getGameID()}); //sends the move
			if (!showBoard) submitMove.setVisible(false); //hide the firing button
		}
		
		this.usersTurn = !usersTurn;
		
		return true;
	}
	public boolean keyboardSupport(){ //returns true if the code is designed to work with keyboard inputs
		return true;
	}
	public boolean mouseSupport(){ //returns true if the code is designed to work with mouse inputs
		return false;
	}
	public void debug(){
		System.out.println(submitMove.toString());
	}
	public int getBoardWidth(){ //returns the width of the board for replayManager's use
		return 10;
	}
	public int getBoardHeight(){ //returns the height of the board for replayerManager's use
		return 10;
	}
	public String getBoardAt(int x, int y){ //returns the value at a specific part of the board
		//if y>10, will return the hits on the opponent board
		if (y>=10){
            return inBounds(x,y-10) ? opponent[x][y-10] + "" : null;
        } //y < 10, will return the hits on the self board
        return inBounds(x, y) ? self[x][y] + "" : null;
	}
	public boolean setBoardAt(int x, int y, String item){ //sets the value of the board at a certain spot to a specified value, returns weather successful
		if (inBounds(x,y-10)){ //y> 10 affects the opponent board
            opponent[x][y-10] = Byte.parseByte(item);
			return true;
		}
		else if (inBounds(x,y)){ //y< 10 affects the self board
            self[x][y] = Byte.parseByte(item);
            return true;
        }
		return false;
	}
	public void drawIcon(Graphics g, int x, int y, int width, int length){
		g.setColor(Color.black);
		g.fillRect(x, y, width, length);
		
		//title
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.setColor(Color.white);
		drawStackString(g, "Battle Ship", x+5, y+5, width);
		g.drawRect(x, y, width, length);
		
		//ocean
		g.setColor(new Color(25, 25, 225));
		g.fillRect(x+2, y+142, width-4, length/4);
		
		//darker colored ship
		g.setColor(Color.darkGray);
		g.fillArc(x+2, y+105, 85, 60, -90, 92);
		g.fillRect(x+2, y+135, 42, 30);
		g.fillRect(x+2, y+120, 30, 15);
		g.fillRect(x+32, y+125, 20, 3);
		
		g.setColor(new Color(100, 100, 100));
		g.fillRect(x+2, y+155, 47, 10);
		g.fillArc(x+20, y+145, 55, 20, -90, 102);
		
		//lighter colored ship
		g.setColor(Color.lightGray);
		g.fillArc(x+106, y+105, 85, 60, 178, 92);
		g.fillRect(x+146, y+135, 42, 30);
		g.fillRect(x+158, y+120, 30, 15);
		g.fillRect(x+138, y+125, 20, 3);
		
		g.setColor(new Color(150, 150, 150));
		g.fillRect(x+141, y+155, 47, 10);
		g.fillArc(x+118, y+145, 50, 20, 178, 90);

	}
	public void drawIcon(Scalable g, int x, int y, int width, int length){
		g.setColor(Color.black);
		g.fillRect(x, y, width, length);
		
		//title
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.setColor(Color.white);
		drawStackString(g, "Battle Ship", x+5, y+5, width);
		g.drawRect(x, y, width, length);
		
		//ocean
		g.setColor(new Color(25, 25, 225));
		g.fillRect(x+2, y+142, width-4, length/4);
		
		//darker colored ship
		g.setColor(Color.darkGray);
		g.fillArc(x+2, y+105, 85, 60, -90, 92);
		g.fillRect(x+2, y+135, 42, 30);
		g.fillRect(x+2, y+120, 30, 15);
		g.fillRect(x+32, y+125, 20, 3);
		
		g.setColor(new Color(100, 100, 100));
		g.fillRect(x+2, y+155, 47, 10);
		g.fillArc(x+20, y+145, 55, 20, -90, 102);
		
		//lighter colored ship
		g.setColor(Color.lightGray);
		g.fillArc(x+106, y+105, 85, 60, 178, 92);
		g.fillRect(x+146, y+135, 42, 30);
		g.fillRect(x+158, y+120, 30, 15);
		g.fillRect(x+138, y+125, 20, 3);
		
		g.setColor(new Color(150, 150, 150));
		g.fillRect(x+141, y+155, 47, 10);
		g.fillArc(x+118, y+145, 50, 20, 178, 90);

	}
	public void setConnectionHandler(ConnectionHandler cH){
		this.cH = cH;
	}
	public byte getGameID(){
		return 3;
	}
	public String getGameName(){
		return "BattleShip";
	}
	public int[] version(){
		return new int[] {1,0,0};
	}
	public void setUsersMove(boolean move){
		this.usersTurn = move;
	}
	public boolean isUsersMove(){
		return usersTurn;
	}
	public String getBoardPreGame(){
		//magic stuff
		String send = "";
		for (byte[] ships : shipPlacements){
			send += ships[0] + ":" + ships[1] + ":" + ships[2] + ";";
		}
		return send;
	}
	public boolean isReady(){
		return !isPlacing;
	}
	public boolean opponentReady(){
		return opponentReady;
	}
	public void getInfo(byte[] array){ //sends hit/miss info
		if (array == null || cH == null) return;
		if (array.length < 2 || !cH.isConnected()) return; 
		cH.send(new byte[] {5, 0, (byte)((self[array[0]][array[1]] > 0 && self[array[0]][array[1]] < 7) ? 1:0) , -1, -1, -1, -1, -1});
	}
	public String parseBoard(byte[] ships){ //reads the placement data of opponents ships and saves it to the opponent's board and end of moves
		if (ships.length < 15) return ""; //not enough info
		String send = "";
		for (byte num : ships){send+=num+",";}
		System.out.println("Parsing: " + send);
		String added = ""; 
		//clear the friendly ships for place logic to easily place the ships on the board
		byte[][] temp = new byte[self.length][self[0].length]; //create a temp board
		/*for (int i = 0; i < self.length;i++){ //copy over friendly ships
			System.arraycopy(self[i], 0, temp[i], 0, self[0].length);
		}*/
		//self = new byte[temp.length][temp[0].length]; //clear the friendly board
		
		for (int i=0;i<shipLengths.length*3;i+=3){
			byte x = ships[i];
			byte y = ships[i+1];
			boolean vertical = ships[i+2] == 1;
			int res = placeLogic(x, y, i/3, vertical, temp);
			if (res != 0){
				//self = temp; //exit with info conserved
				System.out.println("Failed");
				return ""; //didn't place properly
			}
			added += x+":"+y+":"+ships[i+2]+";"; //to add at the end
		}
		//self = temp;
		moves += added;
		hasBoard = true;
		return added;
	}
	public int[][] getValidMoves(boolean usersMove){ //returns all the valid moves of the current board, the second dimension contains [x1, y1, x2, y2]
		DLList<int[]> moves = new DLList<int[]>();
		
		for (int y=0;y<opponent.length;y++){
			for (int x=0;x<opponent[x].length;x++){
				if (opponent[y][x] == 0) moves.add(new int[] {x, y, -1, -1});
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
	private void drawItem(Graphics g, int x, int y, String item){ //draws a part of the game depending on what item is
		if (item.equals("board")){
			//draw the board
            g.setColor(Color.blue);
            g.fillRect(x, y, wid-6, len-6);
		}
		else if (item.equals("boardEVIL")){
			g.setColor(Color.magenta);
			g.fillRect(x, y, wid-6, len-6);
		}
		else{
			//draw the pieces
            g.setColor(Color.lightGray);
            int numItem = Integer.parseInt(item.charAt(0)+"");
            switch (numItem){
                case 1:
                    //horizontal ship, mid part
                    g.fillRect(x, y+len/6, wid-6, 2*len/3-3);
                    break;
                case 2:
                    //vertical ship, mid part
                    g.fillRect(x+wid/6, y, 2*wid/3-3, len-6);
                    break;
                case 3:
                    //horizontal ship left end part (flat side on right)
                    g.fillRect(x+wid/2, y+len/6, wid/2-6, 2*len/3-3);
                    g.fillOval(x, y+len/6, wid-6, 2*len/3-3);
                    break;
                case 4:
                    //horizontal ship right end part (flat side on left)
                    g.fillRect(x, y+len/6, wid/2-6, 2*len/3-3);
                    g.fillOval(x, y+len/6, wid-6, 2*len/3-3);
                    break;
                case 5:
                    //vertical ship top end part (flat side on bottom)
                    g.fillRect(x+wid/6, y+len/2, 2*wid/3-3, len/2-6);
                    g.fillOval(x+wid/6, y, 2*wid/3-3, len-6);
                    break;
                case 6:
                    //vertical ship bottom end part (flat side on top)
                    g.fillRect(x+wid/6, y, 2*wid/3-3, len/2-6);
                    g.fillOval(x+wid/6, y, 2*wid/3-3, len-6);
                    break;
                case 7:
                    //miss
                    g.setColor(Color.white);
                    g.fillPolygon(new int[] {x - 3 + wid/2, x+wid-6, x - 3 + wid/2, x}, new int[] {y, y-3+len/2, y+len-6, y-3+len/2}, 4);
                    break;
				case 8:
					//hit on open water - placeholder - dunno if hit or not
					g.setColor(Color.black);
					g.setFont(new Font("Arial", Font.PLAIN, 25));
					g.drawString("?", x-g.getFontMetrics().stringWidth("?")/2, (int)(y+g.getFontMetrics().getHeight()*(1-21.0/59)/2));
            }
            if (item.length() >= 2 ? item.charAt(1) == 'h' : false){
                g.setColor(Color.red);
                g.fillOval(x+wid/4-2, y+len/4-2, wid/2, len/2);
            }
		}
	}
    private void leftLogic(){ //easier for multiple points to call it
        currentShip = (byte)wrap(currentShip-1, 5, 0);
        int count = 0;
        while(!hasShips[currentShip] && count <10){
            currentShip = (byte)wrap(currentShip-1, 5, 0); //find a ship that actually exists
            count++; //always exit loop
        }
    }
    private void rightLogic(){ //easier for multiple points to call it
        currentShip = (byte)wrap(currentShip+1, 5, 0);
        int count = 0;
        while (!hasShips[currentShip] && count < 10){
            currentShip = (byte)wrap(currentShip+1, 5, 0); //find a ship that actually exists
            count++; //always exit loop
        }
    }
    private int placeLogic(int x, int y, int shipNum, boolean isVertical, byte[][] board){
		if (removeShip) return 1; //shouldn't happen, just in case tho
		if (!inBounds(x, y)) return 2; //cant do things on coords not in bounds
		if (shipNum < 0 || shipNum >= shipLengths.length) return 3; //improper shipNum
		
		//actually do the placement
		byte xMult = (byte)(isVertical ? 0:1);
        byte yMult = (byte)(isVertical ? 1:0);
		
		//check all squares about to be affected are blank (0)
		for (int i=0;i<shipLengths[shipNum];i++){
			//should only increase in the bottom right directions
			if (x+i*xMult < 0 || x+i*xMult >= 10 || y+i*yMult >= 10){
				//out of bounds error
				return 4;
			}
			if (board[x+i*xMult][y+i*yMult] != 0 && board[x+i*xMult][y+i*yMult] <= 6){ //wont be triggered by misses or hits
				//theres a ship here, cant place another ship on top of it
				return 5;
			}
		}
		//actually place the ship
		for (int i=0;i<shipLengths[shipNum];i++){
			if (i==0){
				//one of the end pieces (3 or 5)

				board[x][y] = (byte)(rotateVertical ? 5 : 3);
			}
			else if (i==shipLengths[shipNum]-1){
				//the other end piece (4 or 6)
				board[x+i*xMult][y+i*yMult] = (byte)(isVertical ? 6 : 4);
			}
			else{
				//its a mid piece (1 or 2)
				board[x+i*xMult][y+i*yMult] = (byte)(isVertical ? 2 : 1);
			}
		}
		return 0;
	}
	private int placeLogic(){
        if (removeShip){
            //the remove button was true
            place.setColor(Color.green);
            remove.setColor(Color.red);
            removeShip = false;
            return 0;
        }
        else if (isPlacing && hasShips[currentShip]){
            //placing a ship
            byte xMult = (byte)(rotateVertical ? 0:1);
            byte yMult = (byte)(rotateVertical ? 1:0);
            //expanding the cursor into x,y coords for ease of use
            byte cursorX = (byte)(cursor%10);
            byte cursorY = (byte)(cursor/10);
            //direction of ship placement is down and to the right
            //check all squares about to be affected are blank (0)
            for (int i=0;i<shipLengths[currentShip];i++){
                if (cursorX+i*xMult < 0 || cursorX+i*xMult >= 10 || cursorY+i*yMult >= 10){
                    //out of bounds error, shouldn't happen but oh well
                    return 1;
                }
                if (self[cursorX+i*xMult][cursorY+i*yMult] != 0){
                    //theres a ship here, cant place another ship on top of it
                    return 2;
                }
            }
			shipPlacements[currentShip] = new byte[] {cursorX, cursorY, (byte)(rotateVertical ? 1:0)}; //saves where the ship was placed for replay purposes
            //actually place the ship
            for (int i=0;i<shipLengths[currentShip];i++){
                if (i==0){
                    //one of the end pieces (3 or 5)

                    self[cursorX][cursorY] = (byte)(rotateVertical ? 5 : 3);
                }
                else if (i==shipLengths[currentShip]-1){
                    //the other end piece (4 or 6)
                    self[cursorX+i*xMult][cursorY+i*yMult] = (byte)(rotateVertical ? 6 : 4);
                }
                else{
                    //its a mid piece (1 or 2)
                    self[cursorX+i*xMult][cursorY+i*yMult] = (byte)(rotateVertical ? 2 : 1);
                }
            }
            hasShips[currentShip] = false;
            rightLogic(); //move the current ship to one that can be placed
            return 0;
        }
        return 3;
    }
    private int removeLogic(){
        if (!removeShip){
            //the place ship button was true
            remove.setColor(Color.green);
            place.setColor(Color.red);
            removeShip = true;
			//helpful for when removing ship and have no others - might show wrong ship to place down
			leftLogic();
			rightLogic();
            return 0;
        }
        else if (isPlacing){
            byte cursorX = (byte)(cursor%10);
            byte cursorY = (byte)(cursor/10);
            byte tempTile = self[cursorX][cursorY];
            byte xMult = (byte)((tempTile == 1 || tempTile == 3 || tempTile == 4) ? 1:0);
            byte yMult = (byte)((tempTile == 2 || tempTile == 5 || tempTile == 6) ? 1:0);
            int count = 0;
            while (self[cursorX][cursorY] != 3 && self[cursorX][cursorY] != 5 && count <10){
                cursorX = (byte)Math.max(0, cursorX-xMult);
                cursorY = (byte)Math.max(0, cursorY-yMult);
                count++;
            }
            if (count >= 10){
                //should not be able to reach this normally
                return 1;
            }
            count = 0;
            while (self[cursorX][cursorY] != 4 && self[cursorX][cursorY] != 6 && count < 10){
                self[cursorX][cursorY] = 0;
                cursorX = (byte)Math.min(9, cursorX+xMult);
                cursorY = (byte)Math.min(9, cursorY+yMult);
                count++;
            }
            self[cursorX][cursorY] = 0;
            count++;
            for (int i=0;i<shipLengths.length;i++){
                if (count == shipLengths[i] && !hasShips[i]){
                    //special case to account for the two 3 ships
                    //this is the ship that was deleted
					shipPlacements[i] = new byte[] {-1, -1, 0};
                    hasShips[i] = true;
					leftLogic();
					rightLogic();
                    return 0;
                }
            }
            return 2;
        }
        return 3;
    }
    private boolean inBounds(int x, int y){
        return x>=0 && x<10 && y>=0 && y<10;
    }
	private void sendBoard(){
		//will take size 18 list
		byte[] sendArray = new byte[18];
		sendArray[0] = 1; //board set up
		sendArray[1] = 15; //size of actual board info
		sendArray[2] = this.getGameID();
		for (int i=3;i<sendArray.length;i+=3){
			System.arraycopy(shipPlacements[i/3-1], 0, sendArray, i, 3);
		}
		String send = "";
		for (byte num : sendArray){
			send += num + ", ";
		}
		System.out.println("Sending board: " + send);
		cH.send(sendArray); //will break down into size 8 arrays
	}
	private void attack(){
		byte cursorX = (byte)(cursor%10);
		byte cursorY = (byte)(cursor/10);
		if (!inBounds(cursorX, cursorY)) return; //shouldn't trigger, but for null protection
		if (opponent[cursorX][cursorY] != 0) return; //a shot was already fired there
		//System.out.println("Attacking: " + cursorX + ", " + cursorY);
		move(cursorX, cursorY, -1, -1, false, true); //user's move
	}
	private boolean[] checkShips(){ //checks all self ships, will return true for that ship if completely hit
		boolean[] send = new boolean[5];
		if (shipPlacements == null) return send; //cant go through a list that doesn't exist
		for (int i=0;i<shipPlacements.length;i++){
			//goes through all the shipPlacements and checks those ships
			byte[] ship = shipPlacements[i];
			byte xMult = (byte)((ship[2]+1)%2);
			byte yMult = ship[2];
			boolean allHit = true;
			for (int n=0;n<shipLengths[i];n++){
				if (self[ship[0] + xMult*n][ship[1] + yMult*n] < 8){
					allHit = false;
					continue;
				}
			}
			if (allHit)
				send[i] = true; //show the ship was completely hit
		}
		return send;
	}
	private boolean opponentDead(){
		//returns true if the number of hits equals the number of ship positions, returns true if all opponents ships are sunk
		byte count = 0, total = 0;
		for (int x=0;x<opponent.length;x++){
			for (int y=0;y<opponent[x].length;y++){
				if (opponent[x][y] >= 8) count ++;
			}
		}
		for (byte length : shipLengths){
			total += length;
		}
		return count >= total;
	}
	
	//Replayable methods - taken from replayManager from old code, so, roundabout in execution
	public String goForwards(int cursor, String[] moves) throws NumberFormatException{
		String[] tempMoves = moves[cursor].split(":");
		
		int x = Integer.parseInt(tempMoves[1]),
			y = Integer.parseInt(tempMoves[2]);
		boolean res = this.move(x,y,0,0,true,tempMoves[0].equals("U"));
		
		if (self[x][y] > 7) return "Hit at (" + x + ", " + y +")";
		if (self[x][y] == 7) return "Miss at (" + x + ", " + y + ")";
		return "Error: R:BS:gF";
	}
	public String goBack(int cursor, String[] moves) throws NumberFormatException{
		String[] tempMoves = moves[cursor].split(":");
		int x = Integer.parseInt(tempMoves[1]),
			y = Integer.parseInt(tempMoves[2]);
			
		String move = this.getBoardAt(x, tempMoves[0].equals("U") ? y+10 : y);
		if (move == null) return "Error: R:BS:gB";
		int val = Integer.parseInt(move);
		this.setBoardAt(x, tempMoves[0].equals("U") ? y+10 : y, (val > 7 ? (val-8)+"" : (val == 7 ? "0" : val+"")));
		
		return "Undoing a " + (val == 7 ? "Miss " : "Hit ") + "at (" + x + ", " + y +")";
	}
	public int initalize(String[] moves){
		DLList<String> userShips = new DLList<String>(), oppShips = new DLList<String>();
			int moveStart=2;
			boolean shipAdd = true;
			//find the ship placements in the saved line
			for (int count=0;shipAdd && count < 100;count++){
				String userShip = moves[count+2];
				String oppShip = moves[moves.length-count-1];
				if (!Character.isDigit(userShip.charAt(0)) || !Character.isDigit(oppShip.charAt(0))){ //if the saved move is actually a move instead of a ship placement
					shipAdd = false;
					moveStart = count;
					break;
				}
				userShips.add(userShip);
				oppShips.add(oppShip);
			}
			//create the board based on the ship placements
			String board = "";
			for (String item : userShips){
				board += item + ";";
			}
			board += "/";
			for (String item : oppShips.reverse()){
				board += item + ";";
			}
			this.reset(board);
			return moveStart-2;
	}
	public boolean canGoForward(int cursor, String[] moves){
		//if the next move is a placement data, return false
		return !Character.isDigit(moves[0].split(":")[0].charAt(0));
	}
	public boolean canGoBack(int cursor, String[] moves){
		//if the current spot is wrong and the one that would be after it, return false
		return !(Character.isDigit(moves[cursor].charAt(0)) && Character.isDigit(moves[cursor+1].charAt(0)));
	}
	public int displayNum(int cursor){
		return cursor;
	}
	public boolean canTouchBoard(){
		return true;
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