package games;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Font;
//for the piece's artwork
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
//helpful things
import helpful.Rectangle;
import helpful.Drawer;
import helpful.datastructures.DLList;
import helpful.GUI.*;
import helpful.Scalable;

public final class Stratego implements Game, Replayable{
	private String movesString, placements;
	private byte[][] board;
	private byte[] POI, troopNumbers, pieceToMove, lastMove, spotterMove;
	private static final byte[] maxTroopNumbers = {2, 5, 5, 2, 2, 2, 1, 1, 1, 1, 6, 1, 1}; //1-10, bomb, flag, spy
	private static final char[] troopCharNames = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'b', 'f', 's'};
	private static final String[] troopNames = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Bomb", "Flag", "Spy"};
	private byte cursor, winner;
	private int xStart;
	private boolean isPlacing, hasTroops, isRemoving, keyListen, isUsersTurn, opponentReady, hasBoard, isGuessing;
	private Button submitMove, left, right, place, remove, move, guess;
	private ConnectionHandler cH;
	private Image[] pieces;
	private Text oppReady;
	//spotter buttons / container variables
	private Container spotterButtons;
	private Button spotUp, spotLeft, spotRight, spotDown, spotChoose;
	private int direction;
	//Component Variables
	private boolean isVisible;
	private int x, y;
	public Stratego(int index){
		submitMove = new Button(15, 330, 170, 50, "Submit Placement");
		left = new Button(15, 150, 35, 80, "<");
		right = new Button(150, 150, 35, 80, ">");
		place = new Button(15, 290, 77, 30, Color.green, "Place");
		remove = new Button(108, 290, 77, 30, Color.red, "Remove");
		move = new Button(15, 290, 77, 30, Color.green, "Move");
		guess = new Button(108, 290, 77, 30, Color.red, "Guess");
		oppReady = new Text(15, 110, 170, 40, "Opponent Ready", Text.CENTERED);
		oppReady.setColors(Color.cyan, Color.red);
		
		//Spotter buttons / container
		spotterButtons = new Container(10, 395, 180, 180);
		spotUp = new Button(63, 5, 53, 53){
			public void drawCode(Graphics g){
				g.setColor(textColor);
				g.fillPolygon(new int[] {x+width/2, x+width/4, x+3*width/4}, new int[] {y+height/9, y+2*height/3, y+2*height/3}, 3);
			}
		};
		spotLeft = new Button(5, 63, 53, 53){
			public void drawCode(Graphics g){
				g.setColor(Button.getOppositeColor(color));
				g.fillPolygon(new int[] {x+width/9, x+2*width/3, x+2*width/3}, new int[] {y+height/2, y+height/4, y+3*height/4}, 3);
			}
		};
		spotRight = new Button(180-58, 63, 53, 53){
			public void drawCode(Graphics g){
				g.setColor(textColor);
				g.fillPolygon(new int[] {x+8*width/9, x+width/3, x+width/3}, new int[] {y+height/2, y+height/4, y+3*height/4}, 3);
			}
		};
		spotDown = new Button(63, 180-58, 53, 53){
			public void drawCode(Graphics g){
				g.setColor(textColor);
				g.fillPolygon(new int[] {x+width/2, x+width/4, x+3*width/4}, new int[] {y+8*height/9, y+height/3, y+height/3}, 3);
			}
		};
		spotChoose = new Button(63, 63, 53, 53){
			public void drawCode(Graphics g){
				//outline of the circle
				g.setColor(textColor);
				g.fillOval(x+4*width/32, y+4*height/32, 25*width/32, 25*height/32);
				g.setColor(color);
				g.fillOval(x+3*width/16, y+3*height/16, 11*width/16, 11*height/16);
				//the lines over the circle
				g.setColor(textColor);
				g.fillRect(x+7*width/16, y+height/16, width/8, height/4); //top line
				g.fillRect(x+width/16, y+7*height/16, width/4, height/8); //left line
				g.fillRect(x+7*width/16, y+11*height/16, width/8, height/4); //bottom line
				g.fillRect(x+11*width/16, y+7*height/16, width/4, height/8); //right line
			}
		};
		spotterButtons.add(new Shape(0, 0, 180, 180, Shape.RECT, Color.white), spotUp, spotDown, spotLeft, spotRight, spotChoose);
		//spotterButtons.setVisible(false);
		
		xStart = 0;
		reset();
		pieces = new Image[26];
		try{
			BufferedImage sheet = ImageIO.read(new File("./games/RSpy.jpg"));
			for (int y=0;y<2;y++){
				for (int x=0;x<13;x++){ //TO-DO Change when have full sprite sheet
					pieces[y*13+x] = sheet.getSubimage(x*100, y*100, 100, 100).getScaledInstance(XSIZE/10-12, YSIZE/8-12, 0);
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
			return;
		}
		isVisible = true;
		x = (index%3)*XSIZE/3;
		y = (index/3)*YSIZE/3;
	}
	public void debug(){
		for (int x=0;x<10;x++){
			for (int y=5;y<8;y++){
				placeLogic(x,y);
			}
		}
		String send = "TroopNumbers: ";
		for (int num : troopNumbers){
			send += num + ", ";
		}
		System.out.println(send);
	}
	public void drawGame(Graphics g){ //draws the board
		xStart = 200;
		g.setColor(Color.white);
		g.fillRect(xStart, 0, XSIZE, YSIZE);
		
		//draw the board
		for (int x=0;x<board.length;x++){
			for (int y=0;y<board[x].length;y++){
				boolean isOcean = false;
				g.setColor(new Color(200, 225, 25)); //bright yellow-ish
				if (board[x][y] == 20){
					g.setColor(Color.blue); //ocean tile
					isOcean = true;
				}
				else if (y==3 || y==4){
					g.setColor(new Color(128, 128, 0)); //dark yellow
				}
				g.fillRect(xStart + x*XSIZE/10+2, y*YSIZE/8+2, XSIZE/10-5, YSIZE/8-5);
				if (isOcean) continue; //dont draw on the ocean tiles
				drawItem(g, xStart + x*XSIZE/10+2, y*YSIZE/8+2, board[x][y]);
			}
			Text item = new Text(202+x*(XSIZE/10), YSIZE, XSIZE/10-6, 27, x+"", Text.CENTERED);
			item.setColors(new Color(200, 175, 25), Color.blue);
			item.draw(g);
			if (x < 8){
				item.setDims(202+XSIZE, 3+x*(YSIZE/8), 27, YSIZE/8-6);
				item.draw(g);
			}
		}
		if (inBounds(POI[0], POI[1]) && keyListen){
			g.setColor(new Color(225, 25, 225, 128));
			g.fillRect(xStart + POI[0]*XSIZE/10, POI[1]*YSIZE/8, XSIZE/10, YSIZE/8);
			
			if (pieceToMove[0] == -1){
				g.setColor(new Color(25, 225, 25, 196));
				int[] moves = new int[4];
				if (board[POI[0]][POI[1]] == 2){
					moves = getAttackPos(POI[0], POI[1], 10);
				}
				else if (!(board[POI[0]][POI[1]] == 11 || board[POI[0]][POI[1]] == 12)){
					moves = getAttackPos(POI[0], POI[1], 1);
				}
				//board of 11 or 12 aka bomb or flag cant move;
				byte[] translate = new byte[] {-1, 0, 1, 0}; //makes it easier to find xMult and yMult
				for (int i=0;i<4;i++){
					byte yMult = translate[i];
					byte xMult = translate[(i+3)%4];
					for (int n=1;n<=moves[i];n++){
						g.fillOval(xStart + (POI[0] + n*xMult)*XSIZE/10+XSIZE/10/2-XSIZE/10/4, (POI[1] + n*yMult)*YSIZE/8+YSIZE/8/2-YSIZE/8/4, XSIZE/10/2, YSIZE/8/2);
					}
				}
			}
		}
		if (isPlacing){
			//update button colors logic
			if (isRemoving){
				remove.setColor(Color.green);
				place.setColor(Color.red);
			}
			else{
				remove.setColor(Color.red);
				place.setColor(Color.green);
			}
			if (troopNumbers[cursor] != 0){
				drawItem(g, 70, 150, (byte)(cursor+1));
				g.setColor(Color.white);
				g.setFont(new Font("Arial", Font.PLAIN, 35));
				g.drawString("Count: " + troopNumbers[cursor], 100 - g.getFontMetrics().stringWidth("Count: " + troopNumbers[cursor])/2, 230 + g.getFontMetrics().getHeight());
			}
			if (opponentReady){
				oppReady.draw(g);
			}
			
			//currently placing the ships down
			
			place.draw(g);
			remove.draw(g);
			submitMove.draw(g);
		}
		else{
			//isPlacing = false
			if (inBounds(pieceToMove[0], pieceToMove[1])){
				g.setColor(new Color(225, 225, 25, 196));
				g.fillRect(xStart + pieceToMove[0]*XSIZE/10+2, pieceToMove[1]*YSIZE/8+2, XSIZE/10-5, YSIZE/8-5);
				
				g.setColor(new Color(25, 225, 25, 196));
				int[] moves = new int[4];
				if (board[pieceToMove[0]][pieceToMove[1]] == 2){
					moves = getAttackPos(pieceToMove[0], pieceToMove[1], 10);
				}
				else if (!(board[pieceToMove[0]][pieceToMove[1]] == 11 || board[pieceToMove[0]][pieceToMove[1]] == 12)){
					moves = getAttackPos(pieceToMove[0], pieceToMove[1], 1);
				}
				//board of 11 or 12 aka bomb or flag cant move;
				byte[] translate = new byte[] {-1, 0, 1, 0}; //makes it easier to find xMult and yMult
				for (int i=0;i<4;i++){
					byte yMult = translate[i];
					byte xMult = translate[(i+3)%4];
					for (int n=1;n<=moves[i];n++){
						g.fillOval(xStart + (pieceToMove[0] + n*xMult)*XSIZE/10+XSIZE/10/2-XSIZE/10/4, (pieceToMove[1] + n*yMult)*YSIZE/8+YSIZE/8/2-YSIZE/8/4, XSIZE/10/2, YSIZE/8/2);
					}
				}
			}
			if (guess.getColor() == Color.green && guess.isVisible()){
				g.setColor(Color.black);
				g.fillRect(left.toRect().getX(), left.toRect().getY(), 170, 200);
				
				spotterButtons.draw(g);
				drawItem(g, 70, 150, (byte)(cursor+1));
				g.setColor(Color.white);
				g.setFont(new Font("Arial", Font.PLAIN, 35));
			}
			if (spotterMove[2] != 0){
				byte x = spotterMove[0];
				byte y = spotterMove[1];
				//there was a spotter move
				if (spotterMove[2] < 0){ //it was our guess
					g.setColor(new Color(20, 225, 225));
				}
				if (spotterMove[2] > 0){ //it was our opponent's guess
					g.setColor(Color.red);
				}
				if (Math.abs(spotterMove[2]) == 1){//guess was correct
					g.fillOval(xStart + x*XSIZE/10+5, y*YSIZE/8+5, XSIZE/10-10, YSIZE/8-15);
					if (!(y==3 || y==4)){
						g.setColor(new Color(200, 225, 25)); //bright yellow-ish
					}
					else{
						g.setColor(new Color(128, 128, 0)); //dark yellow
					}
					g.fillOval(xStart + x*XSIZE/10+20, y*YSIZE/8+20, XSIZE/10-40, YSIZE/8-45);
				}
				else{
					//guess was incorrect
					Drawer.drawLineThick(g, xStart + x*XSIZE/10+10, y*YSIZE/8+10, xStart + (x+1)*XSIZE/10-10, (y+1)*YSIZE/8-10, 5);
					Drawer.drawLineThick(g, xStart + x*XSIZE/10+10, (y+1)*YSIZE/8-10, xStart + (x+1)*XSIZE/10-10, y*YSIZE/8+10, 5);
				}
			}
		}
		
		move.draw(g);
		guess.draw(g);
		left.draw(g);
		right.draw(g);
		if (cH != null ? (cH.hasReads() && cH.peek() != null ? cH.peek()[0] == 5 : false) : false){
			
		}
	}
	public void drawGame(Scalable g){ //draws the board
		xStart = 200;
		g.setColor(Color.white);
		g.fillRect(xStart, 0, XSIZE, YSIZE);
		
		//draw the board
		for (int x=0;x<board.length;x++){
			for (int y=0;y<board[x].length;y++){
				boolean isOcean = false;
				g.setColor(new Color(200, 225, 25)); //bright yellow-ish
				if (board[x][y] == 20){
					g.setColor(Color.blue); //ocean tile
					isOcean = true;
				}
				else if (y==3 || y==4){
					g.setColor(new Color(128, 128, 0)); //dark yellow
				}
				g.fillRect(xStart + x*XSIZE/10+2, y*YSIZE/8+2, XSIZE/10-5, YSIZE/8-5);
				if (isOcean) continue; //dont draw on the ocean tiles
				drawItem(g, xStart + x*XSIZE/10+2, y*YSIZE/8+2, board[x][y]);
			}
			Text item = new Text(202+x*(XSIZE/10), YSIZE, XSIZE/10-6, 27, x+"", Text.CENTERED);
			item.setColors(new Color(200, 175, 25), Color.blue);
			item.draw(g);
			if (x < 8){
				item.setDims(202+XSIZE, 3+x*(YSIZE/8), 27, YSIZE/8-6);
				item.draw(g);
			}
		}
		if (inBounds(POI[0], POI[1]) && keyListen){
			g.setColor(new Color(225, 25, 225, 128));
			g.fillRect(xStart + POI[0]*XSIZE/10, POI[1]*YSIZE/8, XSIZE/10, YSIZE/8);
			
			if (pieceToMove[0] == -1){
				g.setColor(new Color(25, 225, 25, 196));
				int[] moves = new int[4];
				if (board[POI[0]][POI[1]] == 2){
					moves = getAttackPos(POI[0], POI[1], 10);
				}
				else if (!(board[POI[0]][POI[1]] == 11 || board[POI[0]][POI[1]] == 12)){
					moves = getAttackPos(POI[0], POI[1], 1);
				}
				//board of 11 or 12 aka bomb or flag cant move;
				byte[] translate = new byte[] {-1, 0, 1, 0}; //makes it easier to find xMult and yMult
				for (int i=0;i<4;i++){
					byte yMult = translate[i];
					byte xMult = translate[(i+3)%4];
					for (int n=1;n<=moves[i];n++){
						g.fillOval(xStart + (POI[0] + n*xMult)*XSIZE/10+XSIZE/10/2-XSIZE/10/4, (POI[1] + n*yMult)*YSIZE/8+YSIZE/8/2-YSIZE/8/4, XSIZE/10/2, YSIZE/8/2);
					}
				}
			}
		}
		if (isPlacing){
			//update button colors logic
			if (isRemoving){
				remove.setColor(Color.green);
				place.setColor(Color.red);
			}
			else{
				remove.setColor(Color.red);
				place.setColor(Color.green);
			}
			if (troopNumbers[cursor] != 0){
				drawItem(g, 70, 150, (byte)(cursor+1));
				g.setColor(Color.white);
				g.setFont(new Font("Arial", Font.PLAIN, 35));
				g.drawString("Count: " + troopNumbers[cursor], 100 - g.getFontMetrics().stringWidth("Count: " + troopNumbers[cursor])/2, 230 + g.getFontMetrics().getHeight());
			}
			if (opponentReady){
				oppReady.draw(g);
			}
			
			//currently placing the ships down
			
			place.draw(g);
			remove.draw(g);
			submitMove.draw(g);
		}
		else{
			//isPlacing = false
			if (inBounds(pieceToMove[0], pieceToMove[1])){
				g.setColor(new Color(225, 225, 25, 196));
				g.fillRect(xStart + pieceToMove[0]*XSIZE/10+2, pieceToMove[1]*YSIZE/8+2, XSIZE/10-5, YSIZE/8-5);
				
				g.setColor(new Color(25, 225, 25, 196));
				int[] moves = new int[4];
				if (board[pieceToMove[0]][pieceToMove[1]] == 2){
					moves = getAttackPos(pieceToMove[0], pieceToMove[1], 10);
				}
				else if (!(board[pieceToMove[0]][pieceToMove[1]] == 11 || board[pieceToMove[0]][pieceToMove[1]] == 12)){
					moves = getAttackPos(pieceToMove[0], pieceToMove[1], 1);
				}
				//board of 11 or 12 aka bomb or flag cant move;
				byte[] translate = new byte[] {-1, 0, 1, 0}; //makes it easier to find xMult and yMult
				for (int i=0;i<4;i++){
					byte yMult = translate[i];
					byte xMult = translate[(i+3)%4];
					for (int n=1;n<=moves[i];n++){
						g.fillOval(xStart + (pieceToMove[0] + n*xMult)*XSIZE/10+XSIZE/10/2-XSIZE/10/4, (pieceToMove[1] + n*yMult)*YSIZE/8+YSIZE/8/2-YSIZE/8/4, XSIZE/10/2, YSIZE/8/2);
					}
				}
			}
			if (guess.getColor() == Color.green && guess.isVisible()){
				g.setColor(Color.black);
				g.fillRect(left.toRect().getX(), left.toRect().getY(), 170, 200);
				
				spotterButtons.draw(g);
				drawItem(g, 70, 150, (byte)(cursor+1));
				g.setColor(Color.white);
				g.setFont(new Font("Arial", Font.PLAIN, 35));
			}
			if (spotterMove[2] != 0){
				byte x = spotterMove[0];
				byte y = spotterMove[1];
				//there was a spotter move
				if (spotterMove[2] < 0){ //it was our guess
					g.setColor(new Color(20, 225, 225));
				}
				if (spotterMove[2] > 0){ //it was our opponent's guess
					g.setColor(Color.red);
				}
				if (Math.abs(spotterMove[2]) == 1){//guess was correct
					g.fillOval(xStart + x*XSIZE/10+5, y*YSIZE/8+5, XSIZE/10-10, YSIZE/8-15);
					if (!(y==3 || y==4)){
						g.setColor(new Color(200, 225, 25)); //bright yellow-ish
					}
					else{
						g.setColor(new Color(128, 128, 0)); //dark yellow
					}
					g.fillOval(xStart + x*XSIZE/10+20, y*YSIZE/8+20, XSIZE/10-40, YSIZE/8-45);
				}
				else{
					//guess was incorrect
					Drawer.drawLineThick(g, xStart + x*XSIZE/10+10, y*YSIZE/8+10, xStart + (x+1)*XSIZE/10-10, (y+1)*YSIZE/8-10, 5);
					Drawer.drawLineThick(g, xStart + x*XSIZE/10+10, (y+1)*YSIZE/8-10, xStart + (x+1)*XSIZE/10-10, y*YSIZE/8+10, 5);
				}
			}
		}
		
		move.draw(g);
		guess.draw(g);
		left.draw(g);
		right.draw(g);
		if (cH != null ? (cH.hasReads() && cH.peek() != null ? cH.peek()[0] == 5 : false) : false){
			
		}
	}
	public void drawIcon(Graphics g, int x, int y, int width, int length){ //draws the icon while selecting the game
		//title
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.setColor(Color.white);
		drawStackString(g, "Stratego", x+5, y+5, width);
		g.drawRect(x, y, width, length);
	}
	public void drawIcon(Scalable g, int x, int y, int width, int length){ //draws the icon while selecting the game
		//title
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		g.setColor(Color.white);
		drawStackString(g, "Stratego", x+5, y+5, width);
		g.drawRect(x, y, width, length);
	}
	public void onClick(MouseEvent e){ // mouse button clicked
		if (move.isClicked(e)){
			move.setColor(Color.green);
			guess.setColor(Color.red);
			isGuessing = false;
			left.setVisible(false);
			right.setVisible(false);
			return;
		}
		if (guess.isClicked(e)){
			move.setColor(Color.red);
			guess.setColor(Color.green);
			isGuessing = true;
			left.setVisible(true);
			right.setVisible(true);
			return;
		}
		//buttons for placing logic
		if (right.isClicked(e)){
			if (!isPlacing){
				absRotateLogic(1);
			}
			else{
				rotateLogic(1);
			}
			return;
		}
		if (left.isClicked(e)){
			if (!isPlacing){
				absRotateLogic(-1);
			}
			else{
				rotateLogic(-1);
			}
			return;
		}
		if (place.isClicked(e)){
			placeLogic(POI[0], POI[1]);
			return;
		}
		if (remove.isClicked(e)){
			removeLogic(POI[0], POI[1]);
			return;
		}
		if (submitMove.isClicked(e) && isTroopsEmpty()){
			isPlacing = false;
			right.setVisible(false);
			left.setVisible(false);
			place.setVisible(false);
			remove.setVisible(false);
			submitMove.setVisible(false);
			cH.send(new byte[] {5, 1});
			movesString += getBoardPreGame() + (isUsersTurn ? 1:0); //saves the board to placements for use later
		}
		
		if (keyListen){
			POI = new byte[] {-1, -1};
			keyListen = false;
		}
		
		//placing the pieces / moving logic
		int x = (e.getX()-xStart-9) / (XSIZE/10);
		int y = (e.getY()-30) / (YSIZE/8);
		boolean res = false;
		//spotter buttons and associated logic
		if (spotChoose.isClicked(e) && direction != 0 && inBounds(pieceToMove[0], pieceToMove[1])){
			int xMult = direction == 2 ? 1 : (direction == 4 ? -1 : 0);
			int yMult = direction == 3 ? 1 : (direction == 1 ? -1 : 0);
			res = move(pieceToMove[0]+xMult, pieceToMove[1]+yMult, -1, cursor+1, false, true);
			pieceToMove = new byte[] {-1, -1}; //only void the piece to move if it was a successful move
			//System.out.println("Guessing: " + (pieceToMove[0]+xMult) + ", " + (pieceToMove[1]+yMult));
			move.setColor(Color.green);
			guess.setColor(Color.red);
			direction = 0;
			spotterButtons.setColors(new Color(23, 45, 213));
			
			//hide the guessing buttons
			move.setVisible(false);
			guess.setVisible(false);
			left.setVisible(false);
			right.setVisible(false);
		}
		if (spotUp.isClicked(e)){
			spotterButtons.setColors(Color.red, Color.white);
			spotUp.setColors(Color.green, Color.white);
			direction = 1;
		}
		if (spotRight.isClicked(e)){
			spotterButtons.setColors(Color.red, Color.white);
			spotRight.setColors(Color.green, Color.white);
			direction = 2;
		}
		if (spotDown.isClicked(e)){
			spotterButtons.setColors(Color.red, Color.white);
			spotDown.setColors(Color.green, Color.white);
			direction = 3;
		}
		if (spotLeft.isClicked(e)){
			spotterButtons.setColors(Color.red, Color.white);
			spotLeft.setColors(Color.green, Color.white);
			direction = 4;
		}
		
		if (isPlacing){
			if ((y > 4 && isRemoving && e.getButton()==3) || (e.getButton() == 3 && !isRemoving)){
				removeLogic(x, y);
			}
			if ((e.getX() >= XSTART && y > 4 && !isRemoving && e.getButton()==1) || (e.getButton() == 1 && isRemoving)){
				//is in the proper place to put a piece
				placeLogic(x, y);
			}
			if (e.getButton() == 4){ //smooth side button on mouse - back
				rotateLogic(-1);
			}
			if (e.getButton() == 5){ //bummpy side button on mouse - forwards
				rotateLogic(1);
			}
		}
		else if (isUsersTurn){
			//isPlacing = false
			if (inBounds(pieceToMove[0], pieceToMove[1]) && inBounds(x, y) && e.getX() > XSTART){
				//there is a piece attempting to be moved
				if (move.getColor() == Color.green || !guess.isVisible()){
					res = move(pieceToMove[0], pieceToMove[1], x, y, false, true);
					
					guess.setColor(Color.red);
					//hide the guessing buttons
					move.setVisible(false);
					guess.setVisible(false);
					left.setVisible(false);
					right.setVisible(false);
				}
				else{
					//guessing mode
					res = move(x, y, -1, cursor+1, false, true);
					move.setColor(Color.green);
					guess.setColor(Color.red);
					
					//hide the guessing buttons
					move.setVisible(false);
					guess.setVisible(false);
					left.setVisible(false);
					right.setVisible(false);
				}
				if (res) pieceToMove = new byte[] {-1, -1}; //only void the piece to move if it was a successful move
			}
			else if ((inBounds(x, y) && e.getX() > XSTART) ? board[x][y] > 0 && board[x][y] < 20 : false){
				// POI is on a friendly piece && setting up the piece to move variable
				pieceToMove = new byte[] {(byte)x, (byte)y};
				if (board[x][y] == 1){ //spotters / 1s can guess opponents pieces
					move.setVisible(true);
					guess.setVisible(true);
				} 
			}
			if (!(x-pieceToMove[0]==0 && y-pieceToMove[1]==0) && pieceToMove[0] != -1 && e.getX() > XSTART) pieceToMove = new byte[] {-1, -1}; //if a click happened that cannot be a move order, void it
		}
	}
	public void mousePressed(MouseEvent e){} // mouse button pressed
	public void mouseReleased(MouseEvent e){} // mouse button relased
	public void mouseEntered(MouseEvent e){} // mouse entered screen
	public void mouseExited(MouseEvent e){} // mouse exited screen
	public void keyPressed(KeyEvent e){ // key is pressed
		//left : 37  up : 38  right : 39  down : 40
		if (!keyListen){
			keyListen = true;
			POI = new byte[] {5, 6};
			pieceToMove = new byte[] {-1, -1};
			return;
		}
		
		int[] moves = new int[4]; //index Out of Bounds protection
		if (inBounds(pieceToMove[0], pieceToMove[1])) moves = getAttackPos(pieceToMove[0], pieceToMove[1], board[pieceToMove[0]][pieceToMove[1]] == 2 ? 10 : 1);
		
		//WASD cursor movement
		if (e.getKeyCode() == 65){
			//to the left
			if (pieceToMove[0] != -1){
				if (moves[1] == 0) return; //cant move, there's no where to move to
				POI[0] = (byte)wrap(POI[0]-1, pieceToMove[0]+moves[3]+1, pieceToMove[0]-moves[1]);
				POI[1] = pieceToMove[1];
			}
			else POI[0] = (byte)wrap(POI[0]-1, 10, 0);
			
		}
		if (e.getKeyCode() == 87){
			//above and beyond
			if (isPlacing) POI[1] = (byte)wrap(POI[1]-1, 8, 5);
			else if (pieceToMove[1] != -1){
				if (moves[0] == 0) return; //cant move, there's no where to move to
				POI[1] = (byte)wrap(POI[1]-1, pieceToMove[1]+moves[2]+1, pieceToMove[1]-moves[0]);
				POI[0] = pieceToMove[0];
			}
			else POI[1] = (byte)wrap(POI[1]-1, 8, 0);
		}
		if (e.getKeyCode() == 68){
			//to the right
			if (pieceToMove[0] != -1){
				if (moves[3] == 0) return; //cant move, there's no where to move to
				POI[0] = (byte)wrap(POI[0]+1, pieceToMove[0]+moves[3]+1, pieceToMove[0]-moves[1]);
				POI[1] = pieceToMove[1];
			}
			else POI[0] = (byte)wrap(POI[0]+1, 10, 0);
		}
		if (e.getKeyCode() == 83){
			//down down down
			if (isPlacing) POI[1] = (byte)wrap(POI[1]+1, 8, 5);
			else if (pieceToMove[1] != -1){
				if (moves[2] == 0) return; //cant move, there's no where to move to
				POI[1] = (byte)wrap(POI[1]+1, pieceToMove[1]+moves[2]+1, pieceToMove[1]-moves[0]);
				POI[0] = pieceToMove[0];
			}
			else POI[1] = (byte)wrap(POI[1]+1, 8, 0);
		}
		if (e.getKeyCode() == 37 && isPlacing){
			rotateLogic(-1); //keyboard input to change current troop
		}
		if (e.getKeyCode() == 39 && isPlacing){
			rotateLogic(1); //keyboard input to change current troop
		}
		if (e.getKeyCode() == 80 && isPlacing){
			//'p' key pressed
			placeLogic(POI[0], POI[1]); //keyboard input to place a troop
		}
		if (e.getKeyCode() == 8){
			//back space key pressed
			if (isPlacing)
				removeLogic(POI[0], POI[1]); //keyboard input to remove a troop
			else
				pieceToMove = new byte[] {-1, -1};
		}
		if (e.getKeyCode() == 10){
			//enter key pressed
			if (isPlacing)
				placeLogic(POI[0], POI[1]);
			else{
				if (inBounds(pieceToMove[0], pieceToMove[1])){
					//there is a piece attempting to be moved
					boolean res = move(pieceToMove[0], pieceToMove[1], POI[0], POI[1], false, true);
					if (res || (pieceToMove[0] == POI[0] && pieceToMove[1] == POI[1])) pieceToMove = new byte[] {-1, -1}; //only void the piece to move if it was a successful move or a voiding attempt
				}
				else if (inBounds(POI[0], POI[1]) ? board[POI[0]][POI[1]] > 0 && board[POI[0]][POI[1]] < 20 : false){
					//POI is on a friendly piece
					pieceToMove = new byte[] {POI[0], POI[1]};
				}
			}	
		}
		if (e.getKeyCode() == 32){
			//space key pressed
			if (isPlacing)
				placeLogic(POI[0], POI[1]);
			else{
				if (inBounds(pieceToMove[0], pieceToMove[1])){
					//there is a piece attempting to be moved
					boolean res = move(pieceToMove[0], pieceToMove[1], POI[0], POI[1], false, true);
					if (res || (pieceToMove[0] == POI[0] && pieceToMove[1] == POI[1])) pieceToMove = new byte[] {-1, -1}; //only void the piece to move if it was a successful move
				}
				else if (inBounds(POI[0], POI[1]) ? board[POI[0]][POI[1]] > 0 && board[POI[0]][POI[1]] < 20 : false){
					//POI is on a friendly piece
					pieceToMove = new byte[] {POI[0], POI[1]};
				}
			}	
		}
		
		//check if the user pressed any of the keys corresponding with a troop to place
		for (int i=0;i<troopCharNames.length;i++){
			if (troopCharNames[i] == e.getKeyChar()){
				cursor = (byte)i;
				break;
			}
		}
	}
	public void keyReleased(KeyEvent e){} //key is relased
	public void typed(KeyEvent e){} //key is typed
	public boolean isOver(){ //returns true if the game is over
		return winner() != -1;
	}
	public int winner(){ //returns who the winner is - -1=no one - 1=player 1 - 2=player 2
		if (isPlacing) return -1;
		if (cH != null ? cH.isClosed() : true) winner = -1; //replay manager going through the game - reset winner
		if (winner != -1) return winner+1; 
		//should the user surrender?
		boolean hasUnit = false, hasFlag = false;
		for (int x=0;x<board.length;x++){
			for (int y=0;y<board[x].length;y++){
				if (board[x][y] <= 0) continue; //don't care about enemy pieces
				if (board[x][y] < 11) hasUnit = true; //a moveable unit is found
				if (board[x][y] ==12) hasFlag = true; //the flag is found
			}
		}
		if (!hasFlag || !hasUnit){
			if (cH != null) cH.send(new byte[] {5, -1, -1, 0, -1, -1, -1, -1}); //tells opponent they won
			winner = 1;
			return 2;
		}
		//go through opponent's pieces taken
		hasUnit = false;
		hasFlag = false;
		if (cH != null){ //normal game, not replay mode
			for (int cursor = 0;cursor < troopNumbers.length;cursor++){
				if (troopNumbers[cursor] < maxTroopNumbers[cursor] && cursor != 10 && cursor != 11) hasUnit = true; //only cares for pieces that can move
				if (cH!=null && troopNumbers[cursor] > maxTroopNumbers[cursor]) cH.send(new byte[] {3, 0, 1, 42, (byte)cursor, (byte)troopNumbers[cursor]}); //somehow, we killed more troops than possible
				if (troopNumbers[cursor] < 1 && cursor == 11) hasFlag = true; //if the killed count is 0 and its a flag thats alive
			}
		}
		else{ //replay mode
			for (int x=0;x<board.length;x++){
				for (int y=0;y<board[x].length;y++){
					if (board[x][y] >= 0) continue; //don't care about user pieces
					if (board[x][y]*-1 < 11) hasUnit = true; //a moveable unit is found
					if (board[x][y] ==-12) hasFlag = true; //the flag is found
				}
			}
		}
		if (!hasFlag || !hasUnit){
			if (cH != null) cH.send(new byte[] {5, -1, -1, 1, -1, -1, -1, -1}); //tells opponent they lost
			winner = 0;
			return 1;
		}
		
		return -1;
	}
	public String getMoves(){ //returns a list of moves of the current game
		return movesString;
	}
	public void reset(){ //resets all the game states
		movesString = "Stratego;" + this.getDate() + ";";
		POI = new byte[] {5, 6};
		pieceToMove = new byte[] {-1, -1};
		lastMove = new byte[] {-1, -1, -1}; //to store the x,y coords of the last move for use of if the opponent piece dies / survives
		spotterMove = new byte[] {-1, -1, 0}; //stores the move done by the spotter and if the guess was correct or not
		isPlacing = true;
		isRemoving = false;
		keyListen = false;
		opponentReady = false;
		hasBoard = false;
		winner = -1;
		
		//for placing pieces down
		troopNumbers = new byte[maxTroopNumbers.length];
		System.arraycopy(maxTroopNumbers, 0, troopNumbers, 0, troopNumbers.length);
		cursor = 0; 
		submitMove.setVisible(false);
		left.setVisible(true);
		right.setVisible(true);
		place.setVisible(true);
		remove.setVisible(true);
		placements = "";
		
		//spotters guessing logic things
		isGuessing = false;
		move.setVisible(false);
		move.setColor(Color.green);
		guess.setVisible(false);
		guess.setColor(Color.red);
		direction = 0;
		
		//create the board
		board = new byte[10][8];
		for (int x=0;x<board.length;x++){
			for (int y=0;y<board[x].length;y++){
				if ((x==2 || x==3 || x==6 || x==7) && (y==3 || y==4)){
					board[x][y] = 20; // ocean tile, helps with getting attack pos
				}
			}
		}
	}
	public void reset(String boardPreSet){
		reset();
		//do the placement of each piece where they should be
		String[] troopPlacements = boardPreSet.split("/");
		//index 0 is friendly pieces, index 1 is enemy pieces
		
		//Friendly pieces
		char[] troops = troopPlacements[0].toCharArray();
		for (int x=0;x<board.length;x++){
			for (int y=5;y<board[0].length;y++){
				byte piece=PtN("P"+troops[x*3+y-5]);
				//if not one of those, its defaulted to 0 
				board[x][y] = piece;
			}
		}
		isUsersTurn = false;
		if (troopPlacements[0].charAt(troopPlacements[0].length()-1) == '1'){
			isUsersTurn = true;
			//System.out.println("Users's turn");
		}
		//enemy pieces
		troops = troopPlacements[1].toCharArray();
		for (int x=0;x<board.length;x++){
			for (int y=0;y<3;y++){
				byte piece=PtN("P"+troops[x*3+y]);
				/*try{
					if (Byte.parseByte(troops[x*3+y]+"") <= 9){
						piece = (byte)(Byte.parseByte(troops[x*3+y]+"")+1);
					}
				}
				catch(NumberFormatException e){
					//will only trigger if above is false
					switch(troops[x*3+y]){
						case 'b':
							piece = 11;
							break;
						case 'f':
							piece = 12;
							break;
						case 's':
							piece = 13;
							break;
					}
				}*/
				//if not one of those, its defaulted to 0
				board[board.length-1-x][2-y] = (byte)(-1*piece);
			}
		}
		
		//hide the buttons that are defaulted shown
		left.setVisible(false);
		right.setVisible(false);
		place.setVisible(false);
		remove.setVisible(false);
		submitMove.setVisible(false);
		
		//make sure the variables are correct for running the game
		isPlacing = false;
		//hasBoard = true;
		opponentReady = true;
		troopNumbers = new byte[maxTroopNumbers.length];
		
	}
	public void handleMessage(byte[] nums){
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
			for (int x=0;x<board.length;x++){
				for (int y=0;y<3;y++){
					board[x][y] = -17; //no knowledge of piece but it does exist
				}
			}
		}
		else{ //nums[1] < 1
			if (nums[7] == 30){ //if its a board request
				if (isOver()) sendBoard();
			}
			else if (nums[2] != -1){ //if its general info
				//this is the type of the enemy's piece encountered
				int unit = nums[2]%100; //nums[2] is now a piece number
				boolean win = doesUnitWin(lastMove[2], unit);
				boolean givenWin = nums[2] >= 100;
				movesString += ":"+NtP(unit*-1);
				if (win != givenWin){
					//System.out.println("Connection gen info debug: " + board[lastMove[0]][lastMove[1]] + ", " + lastMove[0] + ", " + lastMove[1] + ", " + unit + ", " + win + ", " + nums[2]);
					cH.send(new byte[] {3, 1, 1, 1}); //board / sync error
					return;
				}
				if (!win){
					//enemy wins the battle
					//movesString += ":"+board[lastMove[0]][lastMove[1]];
					board[lastMove[0]][lastMove[1]] = (byte)(-1*nums[2]); //nums[2] is the unit number - guaranteed
				}
				if ((win && unit != 0 || unit == 11)) { //enemy loses the battle
					troopNumbers[(nums[2]%100)-1]++;
					//System.out.println("Draw game connection win: Adding to: " + (nums[2]%100));
				} //is the default temp placement so don't need to do anything else but increment the dead troop count of our opponent
				if (unit == lastMove[2] || unit == 11){
					board[lastMove[0]][lastMove[1]] = 0; //a tie kills both units
					//Do we need to tell opponent? 
				}
			}
			else if (nums[3] != -1){ //if its a win/lose info
				winner = nums[3];
				if (!hasBoard) cH.send(new byte[]{5, -1, -1, -1, -1, -1, -1, 30}); //request the board if we don't have it yet
			}
			else if (nums[4] != -1){ //opponenent saying what a piece at a certain square is
				byte x = nums[4];
				byte y = nums[5];
				byte num = nums[6];
				if (board[x][y] != -17 && -1*board[x][y] != num) cH.send(new byte[] {3, 0, 1, 2}); //we were lied to, send error
				if (board[x][y] < 0) board[x][y] = (byte)(-1*nums[6]);
			}
			else if (nums[5] != -1){ //opponent saying wether a guess was successful or not
				spotterMove = new byte[] {lastMove[0], lastMove[1], -2}; //default to our guess was incorrect
				if (nums[5] == 1){ //you guessed correctly
					board[lastMove[0]][lastMove[1]] = 0;
					troopNumbers[lastMove[2]-1]++;
					spotterMove[2] = -1; //our guess was correct
					lastMove = new byte[] {-1, -1, -1};
					movesString += ":1"; //Spotter guess was correct
				}
				else{
					movesString += ":-1"; //Spotter guess was incorrect
				}
			}
		}
	}
	public boolean move(int x, int y, int x2, int y2, boolean override, boolean usersTurn){
		int res = moveInt(x, y, x2, y2, override, usersTurn);
		if (res <=0) isUsersTurn = !usersTurn;
		else System.out.println("Move int debug: " + res + " x,y = " + (inBounds(x,y) ? board[x][y]:"Oob") + ", x2,y2 = " + (inBounds(x2,y2)?board[x2][y2]:"OoB") + ", " + x + ", " + y + ", " + x2 + ", " + y2);
		return res <= 0;
	}
	private int moveInt(int x, int y, int x2, int y2, boolean override, boolean usersTurn){ //The logic of what should happen given a move on this location
		if (isPlacing) return 1; //can't move if you are currently placing pieces
		if (isOver() && !override) return 7;
		
		if (x2 == -1 && inBounds(x, y)){ //spotter guess logic (x,y) = target, x2 == -1, y2 = guess of item
			if (isPositive(board[x][y]) != (usersTurn ? -1:1)) return 8; //can only target enemy's pieces
			byte[] translate = new byte[] {-1, 0, 1, 0}; //makes it easier to find xMult and yMult
			boolean foundSource = false;
			for (int i=0;i<4;i++){
				byte yMult = translate[i];
				byte xMult = translate[(i+3)%4];
				if (inBounds(x+xMult, y+yMult) ? board[x+xMult][y+yMult] == 1 : false){
					foundSource = true;
					break;
				}
			}
			//Replay Spotter Encoding
			//;x,y,guess:Correct(1)/Not Correct(-1);
			if (!foundSource && usersTurn) return 9; //didn't find the spotter that created the guess
			if (usersTurn && cH!=null){
				cH.send(new byte[] {0, (byte)(9-x), (byte)(7-y), -1, (byte)y2, -1, -1, getGameID()}); //tell the opponent where we are guessing and what our guess is
				lastMove = new byte[] {(byte)x, (byte)y, (byte)y2};
				movesString += ";" + x + "" + y + ":" +NtP(-1*y2);
				return -1;
			}
			else if (!usersTurn){
				//got the guess from the other guy
				//try to figure out who the spotter is automaticly, only works if there is a single enemy next to this piece
				int[] mults = new int[2];
				boolean moreThanOne = false;
				for (int i=0;i<4;i++){
					byte yMult = translate[i];
					byte xMult = translate[(i+3)%4];
					//System.out.println("Auto spotter finder: try: " + (inBounds(x+xMult, y+yMult) ? board[x+xMult][y+yMult] : "OoB") + ", " + (x+xMult) + ", " + (y+yMult) + ", " + x + ", " + y + ", " + xMult + ", " + yMult);
					if (inBounds(x+xMult, y+yMult) ? (board[x+xMult][y+yMult] < 0) : false){
						if (mults[0] != 0 || mults[1] != 0) moreThanOne = true;
						mults = new int[] {xMult, yMult};
					}
				}
				if (!moreThanOne){
					//single enemy or an error
					if (mults[0] != 0 || mults[1] != 0){
						if (!inBounds(x+mults[0], y+mults[1])){
							cH.send(new byte[] {3, 1, 0, 127, 3}); //somehow, the destination of the potential spotter escaped the confines of the board
						}
						if (board[x+mults[0]][y+mults[1]] == -17 || board[x+mults[0]][y+mults[1]] == -1){
							board[x+mults[0]][y+mults[1]] = -1;
						}
						else{ //board is not an unknown piece or the spotter
							cH.send(new byte[] {3, 0, 1, 127, 1});
						}
					}
					else{ //theres no piece to have spotted
						cH.send(new byte[] {3, 0, 1, 127, 2});
					}
				}
				spotterMove = new byte[] {(byte)x, (byte)y, 0};
				if (board[x][y] == y2){ //guess was correct
					board[x][y] = 0; //remove the correctly guessed piece
					spotterMove[2] = 1; //opponent guessed correctly
					cH.send(new byte[] {5, -1, -1, -1, -1, 1});
					movesString += ";" + x + "" + y + ":" +NtP(y2) + ":1";
				}
				else{ //guess was incorrect
					spotterMove[2] = 2; //opponent guessed incorrectly
					cH.send(new byte[] {5, -1, -1, -1, -1, 0});
					movesString += ";" + x + "" + y + ":" +NtP(y2) + ":-1";
				}
				return -2;
			}
			return 30;
		}
		
		if (!inBounds(x, y) || !inBounds(x2, y2)) return 2; //out of bounds
		if (isPositive(board[x][y]) != (usersTurn ? 1:-1) && !override) return 3; //cant control enemy's pieces
		if (Math.abs(board[x][y]) >= 20) return 4; //terrain pieces are larger than 20, cant control them
		if (!(board[x2][y2] == 0 || isPositive(board[x2][y2]) == (-1*isPositive(board[x][y])))) return 5; //if the destination is not empty or the enemy piece
		if (!((x-x2 == 0) ^ (y-y2 == 0))) return 6; //either moving in one direction or not moving
		
		spotterMove = new byte[] {-1, -1, 0}; //resets the spotter move
		int size = 1; //normal pieces can only move 1 square (weird bug - dont ask - this works)
		if (Math.abs(board[x][y]) == 11 || Math.abs(board[x][y]) == 12) size = 0; //bombs and flags dont move
		if (Math.abs(board[x][y]) == 2) size = 10; //a 2 piece can move the boards length in a straight line
		if (board[x][y] == -17) size = 10; //unknown piece is allowed to move like a 2
		int[] moves = getAttackPos(x, y, size);
		boolean move = false;
		
		if (x-x2 > 0 && x-x2 <= moves[1]){
			//moving leftwards && success
			move = true;
		}
		if (y-y2 > 0 && y-y2 <= moves[0]){
			//moving upwards && success
			move = true;
		}
		if (x-x2 < 0 && x2-x <= moves[3]){
			//moving rightwards && success
			move = true;
		}
		if (y-y2 < 0 && y2-y <= moves[2]){
			//moving downwards && success
			move = true;
		}
		//System.out.println("Move debug: " + moves[0] + ", " + moves[1] + ", " + moves[2] + ", " + moves[3] + ", " + move);
		if (move){
			byte item = (byte)Math.abs(board[x2][y2]), friend = (byte)Math.abs(board[x][y]);
			boolean win = doesUnitWin(friend, item);
			boolean bothDie = false;
			//System.out.println(item + ", " + friend);
			if (Math.abs(item) == Math.abs(friend)){ //weird bug that only happens on ties if we know the enemy piece and its our turn  //   && !usersTurn
				board[x][y] = 0; //its a tie so void the spot, they both die
				troopNumbers[item-1]++;
				//System.out.println("Move int tie: Adding to: " + item);
				bothDie = true;
			}
			if (Math.abs(item) == 11){
				board[x2][y2] = 0; //bombs die after being exploded
				board[x][y] = 0; //including the thing that came at them
				troopNumbers[item-1]++;
				//System.out.println("Move int tie: Adding to: Bomb");
				bothDie = true;
			}
			//Normal move replay encoding
			//;x,y,x2,y2,piece that moved,piece attacked;
			//temporarily move the piece to the destination if we have no knowledge of the 
			if (usersTurn){
				movesString += ";"+x+""+y+""+x2+""+y2+":"+NtP(friend);
				board[x2][y2] = board[x][y];
				board[x][y] = 0;
				if (item != 0 && cH != null){
					cH.send(new byte[] {5, -1, -1, -1, (byte)(9-x), (byte)(7-y), friend}); //if an attack happened, say what the attacking piece is
				}
				if (cH != null) cH.send(new byte[] {0, (byte)(9-x), (byte)(7-y), (byte)(9-x2), (byte)(7-y2), -1, -1, this.getGameID()});
				
			}
			else{ //enemy's move
				movesString += ";"+x+""+y+""+x2+""+y2+":"+NtP(friend*-1)+":"+NtP(item);
				if (win){ //their piece won the fight
					board[x2][y2] = board[x][y];
					board[x][y] = 0;
					if (cH != null) cH.send(new byte[] {5, -1, (byte)(100 + item)}); //the piece won, here's what was killed
					if (Math.max(Math.abs(x2-x), Math.abs(y2-y)) >= 2 && board[x2][y2] == -17) board[x2][y2] = -2; //only twos can move more than a single square - mark that this piece was a two
				}
				else{ //their piece lost the fight
					board[x][y] = 0; 
					if (cH != null) cH.send(new byte[] {5, -1, (byte)item}); //the piece lost, here's what killed it
					troopNumbers[friend-1]++; //keep track of what troops they had, that are now dead
					//System.out.println("Move int enemy lose: Adding to: " + friend + " item is: " + item);
				}
				
			}
			
			lastMove = new byte[] {(byte)x2, (byte)y2, friend};
			return 0;
		}
		//no logic firing - failed
		return 30;
	}
	public boolean keyboardSupport(){
		return true;
	}
	public boolean mouseSupport(){
		return true;
	}
	public int getBoardWidth(){ //returns the width of the board for replayManager's use
		return 10;
	}
	public int getBoardHeight(){ //returns the height of the board for replayerManager's use
		return 8;
	}
	public String getBoardAt(int x, int y){ //returns the value at a specific part of the board
		if (!inBounds(x, y)) return ""; //out of bounds
		return NtP(board[x][y]);
	}
	public boolean setBoardAt(int x, int y, String item){ //sets the value of the board at a certain spot to a specified value, returns weather successful
		if (!inBounds(x,y)) return false;
		if (item.length() < 2) return false;
		if (item.charAt(0) == 'P'){
			//convert from single character encoding to the number
			byte piece=PtN(item);
			board[x][y] = piece;
			//System.out.println("Board Set " + x + "," + y + " to " + piece);
			return true;
		}
		else if (item.charAt(0) == 'N'){
			//its just the number 
			byte num = Byte.parseByte(item.substring(1));
			board[x][y] = num;
			return true;
		}
		return false;
	}
	public void setConnectionHandler(ConnectionHandler cH){
		this.cH = cH;
	}
	public byte getGameID(){
		return 4;
	}
	public String getGameName(){
		return "Stratego";
	}
	public int[] version(){
		return new int[] {1,0,0};
	}
	public void setUsersMove(boolean move){
		isUsersTurn = move;
	}
	public boolean isUsersMove(){
		return isUsersTurn;
	}
	public String getBoardPreGame(){
		//magic stuff
		if (!placements.equals("")) return placements;
		String send = "";
		for (int x=0;x<board.length;x++){
			for (int y=5;y<board[0].length;y++){
				byte item = board[x][y];
				if (item > 0 && item <= troopCharNames.length){
					send += troopCharNames[item-1];
				}
			}
		}
		placements = send;
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
		if (array.length < 3 || !cH.isConnected()) return; 
		cH.send(new byte[] {5, 0, board[array[0]][array[1]], -1, -1, -1, -1, -1});
	}
	public String parseBoard(byte[] array){
		hasBoard = true; //don't need the internet traffic
		char[] remade = new char[array.length];
		for (int i=0;i<array.length;i++){
			remade[i] = (char)array[i];
		}
		String preGameBoard = new String(remade);
		movesString += ";"+preGameBoard+";";
		//System.out.println("Parsing the board now");
		return preGameBoard;
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
	private void drawItem(Graphics g, int x, int y, byte item){ //draws a part of the game depending on what item is
		//will just draw pieces
		if (item == 0){
			//theres nothing there to draw
			return;
		}
		//drawing the background of the piece
		g.setColor(Color.black);
		if (item < 0){
			g.setColor(Color.red); // neg nums are enemy troops
		}
		g.fillRect(x+1, y+1, XSIZE/10-6, YSIZE/8-6);
		boolean neg = item<0;
		if (item < 0)item = (byte)(item*-1 + 13);
		item = (byte)Math.abs(item);
		
		if (item > 0 && item <= pieces.length){
			if (pieces[item-1] != null){
				g.drawImage(pieces[item-1], x+5, y+6, null);
			}
		}
	}
	private boolean inBounds(int x, int y){
		return x>=0 && x<10 && y>=0 && y<8;
	}
	private int[] getAttackPos(int x, int y, int maxSize){
		//returns a number of squares able to be moved 
		//encoding scheme: 0-up, 1-left, 2-down, 3-right
		int[] send = new int[4]; //make sure to not draw if it has 0
		if (!inBounds(x,y)){
			return send; //empty list
		}
		if (board[x][y] == 0 || board[x][y] == 20){
			//if the position is empty or ocean
			return send; // empty list
		}
		byte[] translate = new byte[] {-1, 0, 1, 0}; //makes it easier to find xMult and yMult
		for (int i=0;i<4;i++){
			byte yMult = translate[i];
			byte xMult = translate[(i+3)%4];
			byte count = 1;
			//go through all the empty spaces for possible moves
			while (count <= maxSize && send[i] <= maxSize){
				if (inBounds(x + xMult*count, y + yMult*count) ? board[x+xMult*count][y+yMult*count] != 0:true){
					send[i] = count-1;
					break;
				}
				count++;
			}
			if (count >= maxSize){
				send[i] = count-1;
			}
			//add onto the count if its an enemy count (good for actually attacking on an enemy)
			if ((inBounds(x+xMult*(send[i]+1), y+yMult*(send[i]+1)) ? (isPositive(board[x][y]) == -1*isPositive(board[x+xMult*(send[i]+1)][y+yMult*(send[i]+1)])) : false) && send[i] < maxSize){
				send[i] ++;
			}
		}
		return send;
	}
	private int isPositive(int num){
		if (num == 0) return 0; //div by 0 error - old code, still needed 
		return num > 0 ? 1 : -1; 
	}
	private void rotateLogic(int dir){
		for (int i=0;i<troopNumbers.length;i++){
			cursor = (byte)wrap(cursor+dir, troopNumbers.length, 0);
			if (troopNumbers[cursor] != 0){
				break;
			}
		}
	}
	private void absRotateLogic(int dir){ //will not force the cursor onto a troop with some count
		cursor = (byte)wrap(cursor+dir, troopNumbers.length, 0);
	}
	private void placeLogic(int x, int y){
		if (!inBounds(x, y-5)) return; //no set piece of intrest - offset so only affecting user's side of the board
		if (troopNumbers[cursor] == 0) return; //no piece to place on board
		if (!isPlacing) return; //not changing troops right now
		if (isRemoving){
			isRemoving = false;
			return;
		}
		if (board[x][y] != 0){
			isRemoving = true;
			removeLogic(x, y); //replacing an already placed troop
			isRemoving = false;
		}
		board[x][y] = (byte)(cursor+1);
		troopNumbers[cursor]--;
		if (isTroopsEmpty()){
			submitMove.setVisible(true);
			return;
		}
		//goes to the next piece if this one ran out
		rotateLogic(-1);
		rotateLogic(1);
	}
	private void removeLogic(int x, int y){
		if (!inBounds(x, y-5)) return; //no set piece of intrest - offset so only affecting user's side of the board
		if (board[x][y] == 0) return; //cant remove an empty space
		if (!isPlacing) return; //not changing troops rn
		if (!isRemoving){
			isRemoving = true;
			return;
		}
		troopNumbers[board[x][y]-1]++;
		board[x][y] = 0;
		if (!isTroopsEmpty()){
			submitMove.setVisible(false);
		}
		//will rotate the selection troop to this one if there weren't any before
		rotateLogic(-1);
		rotateLogic(1);
	}
	private boolean isTroopsEmpty(){
		boolean send = true;
		for (byte num : troopNumbers){
			if (num != 0) send = false;
		}
		return send;
	}
	private int sendBoard(){ //used for save game feature for replay mode
		if (!isOver()) return 1; //the game is still being played, don't send confidential info
		if (cH == null) return 2; //nowhere to send board to
		if (placements.equals("")) return 3; //no info on piece's placements
		char[] charArray = placements.toCharArray();
		if (charArray.length < 30) return 4; //not enough info on piece's placements
		byte[] sendArray = new byte[charArray.length+3];
		sendArray[0] = 1;
		sendArray[1] = 30;
		sendArray[2] = this.getGameID();
		for (int i=0;i<charArray.length;i++){
			sendArray[i+3] = (byte)charArray[i];
		}
		cH.send(sendArray);
		if (hasBoard == false){
			//cH.send(new byte[]{5, -1, -1, -1, -1, -1, -1, 30});
		}
		return 0;
	}
	private boolean doesUnitWin(int unit, int enemy){
		unit = Math.abs(unit);
		enemy = Math.abs(enemy);
		boolean win = false;
		if (enemy == 11 && unit == 3){ 
			win = true; //bomb sweepers can take out bombs
		}
		else if (enemy == 13){
			win = true; //any piece can kill a spy, even a 10 if it moves to it
		}
		else if (enemy == 10 && unit == 13){
			win = true; //the spy can kill a 10
		}
		else if (unit == 13 && enemy > 0){
			win = false; //the spy moving on a piece not a ten will kill it
		}
		else if (enemy == 12){
			win = true; //all pieces can capture the flag
		}
		else if (enemy < unit){
			win = true;
		}
		else if (unit < enemy){
			win = false;
		}
		else if (unit == enemy){
			win = true;
		}
		return win;
	}
	public static String NtP(int number){
		if (number == -17) return "x";
		if (number == 0 || number > troopCharNames.length || number < -1*troopCharNames.length) {return " ";} //System.out.println("NTP FAILED: " + number);
		if (number < 0) return "-"+troopCharNames[Math.abs(number)-1];
		return troopCharNames[number-1]+"";
	}
	public static byte PtN(String item){
		if (item.length() < 1) return 0;
		if (item.charAt(0) == 'P') return PtN(item.substring(1));
		if (item.charAt(0) == 'N') return 0;
		if (item.charAt(0) == 'x') return -17;
		if (item.charAt(0) == '-' && item.length() >= 2){
			for (byte i=0;i<troopCharNames.length;i++){
				if (troopCharNames[i] == item.charAt(1)){
					return (byte)(-1*(i+1));
				}
			}
		}
		else if (item.charAt(0) == ' '){
			return 0;
		}
		else{
			for (int i=0;i<troopCharNames.length;i++){
				if (troopCharNames[i] == item.charAt(0)){
					return (byte)(i+1);
				}
			}
		}
		return 0;
	}
	
	//Replayable methods - taken from replayManager from old code, so, roundabout in execution
	public String goForwards(int cursor, String[] moves) throws NumberFormatException{
		String[] tempMove = moves[cursor].split(":");
		//System.out.println(moves[cursor]);
		//;x,y,x2,y2:userWin(pieceLost)/userLose(-1*pieceWon)/N-A(0);
		//;x,y,guess:Correct(1)/Not Correct(-1);
		boolean usersTurn = (cursor-3)%2 == 0;
		if (moves[2].charAt(moves[2].length()-1) == '0') usersTurn = !usersTurn;
		if (tempMove[0].length() == 4){ //Normal move
			int x = Integer.parseInt(tempMove[0].charAt(0)+""),
				y = Integer.parseInt(tempMove[0].charAt(1)+""),
				x2 = Integer.parseInt(tempMove[0].charAt(2)+""),
				y2 = Integer.parseInt(tempMove[0].charAt(3)+"");
			this.move(x, y, x2, y2, true, this.isUsersMove());
			if ((usersTurn ? 1:-1)*board[x][y] < 0) return "(" + x + ", " + y + ") moved to ("+ x2 + ", " + y2 + ") and lost";
			if ((usersTurn ? 1:-1)*board[x][y] > 0) return "(" + x + ", " + y + ") moved to ("+ x2 + ", " + y2 + ") and won";
			if (board[x][y] == 0) return "(" + x + ", " + y + ") moved to\n("+ x2 + ", " + y2 + ") and tied, killing both";
			return "Error: R:S:gF:m";
		}
		if (tempMove[0].length() == 2){ //spotter move
			int x = Integer.parseInt(tempMove[0].charAt(0)+""),
				y = Integer.parseInt(tempMove[0].charAt(1)+"");
			String guess = tempMove[1];
			String actualPiece = this.getBoardAt(x, y);
			if (guess.equals(actualPiece) && Integer.parseInt(tempMove[2]) == 1){
				this.setBoardAt(x, y, "P ");
				return "Spotter guessed (" + x + ", " + y + ")\nwas " + guess + " and was correct";
			}
			return "Spotter guessed (" + x + ", " + y + ")\nwas " + guess + " and was incorrect";
		}
		return "Error: R:S:gF:nm";
	}
	public String goBack(int cursor, String[] moves) throws NumberFormatException{
		String[] tempMove = moves[cursor].split(":");
		//;x,y,x2,y2:piece that died/N-A(0):if Tie(friendly piece that died);
		//;x,y,guess:Correct(1)/Not Correct(-1);
		boolean usersTurn = (cursor-3)%2 == 0;
		if (moves[2].charAt(moves[2].length()-1) == '0') usersTurn = !usersTurn;
		if (tempMove[0].length() == 4){ //Normal move
			int x = Integer.parseInt(tempMove[0].charAt(0)+""),
				y = Integer.parseInt(tempMove[0].charAt(1)+""),
				x2 = Integer.parseInt(tempMove[0].charAt(2)+""),
				y2 = Integer.parseInt(tempMove[0].charAt(3)+"");
			
			if (tempMove[1].charAt(0) == 'x'){
				//System.out.println("Activated, " + this.getBoardAt(x,y) + ", " + this.getBoardAt(x2, y2));
				
				this.setBoardAt(x, y, "P"+this.getBoardAt(x2, y2));
				this.setBoardAt(x2, y2, "P ");
			}
			else{
				this.setBoardAt(x, y, "P"+tempMove[1]);
				this.setBoardAt(x2, y2, "P"+tempMove[2]);
			}
			return "Undoing (" + x + ", " + y + ") moving to \n(" + x2 + ", " + y2 + ")";
		}
		if (tempMove[0].length() == 2){ //spotter move
			int x = Integer.parseInt(tempMove[0].charAt(0)+""),
				y = Integer.parseInt(tempMove[0].charAt(1)+""),
				correct = Integer.parseInt(tempMove[2]);
			String guess = tempMove[1];
			if (correct == 1){
				this.setBoardAt(x, y, "P"+guess);
			}
			return "Undoing Spotter guessing \n(" + x + ", " + y +") is a " + (PtN(guess) >= 0 ? troopNames[PtN(guess)] : "Unknown Piece: ERROR");
		}
		return "Error: R:S:gB:nm";
	}
	public int initalize(String[] moves){
		this.reset(moves[2] + "/" + moves[moves.length-1]);
		return 2;
	}
	public boolean canGoForward(int cursor, String[] moves){
		return !((cursor+1 < moves.length) ? moves[cursor+1].split(":")[0].length() >= 10 : false);
	}
	public boolean canGoBack(int cursor, String[] moves){
		return !((cursor >= 0) ? moves[cursor].split(":")[0].length() >= 8 : false);
	}
	public int displayNum(int cursor){
		return cursor-2;
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