import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import games.Game;
import games.Replayable;
import helpful.GUI.Button;
import helpful.GUI.Text;
import helpful.datastructures.DLList;
import helpful.Drawer;
//images
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

//until we consloidate the forwards and backwards functions in each class:
import games.Stratego;

class ReplayManager{
	private int cursor;
	private String[] moves;
	private Replayable game;
	private Button step, back, debug, restart;
	protected Text winText, lastMoveMessage;
	protected boolean initialized;
	public ReplayManager(Replayable game, String moves){
		reset(game, moves);
		step = new Button(108, 80, 77, 50, ">");
		back = new Button(15, 80, 77, 50, "<");
		winText = new Text(15, 280, 170, 50, "Error occured", Text.CENTERED);
		lastMoveMessage = new Text(15, 505, 170, 50, "Move Message", Text.CENTERED, Color.white);
		debug = new Button(15, 555, 170, 30, Color.darkGray, "Debug");
		try{
			restart = new Button(145, 15, 50, 50, Color.lightGray, ImageIO.read(new File("./images/RetryIcon.png")));
		}
		catch (IOException e) {
			restart = new Button(145, 15, 50, 50, Color.white, "Restart");
			e.printStackTrace();
		}
	}
	public void reset(Replayable game, String moves){
		this.game = game;
		this.game.reset();
		cursor = 1;
		this.moves = moves.split(";");
		initialized = false;
		game.initalize(this.moves);
	}
	public void step(){
		cursor = Math.min(cursor+1, moves.length-1);
	}
	public void back(){
		cursor = Math.max(cursor-1, 1);
	}
	public void setMoves(String moves){
		reset(game, moves);
	}
	public void draw(Graphics g){
		g.setColor(Color.black);
		g.fillRect(0,0, (int)g.getClipBounds().getWidth()-Game.XSIZE, Game.YSIZE);
		
		//draw buttons
		step.draw(g);
		back.draw(g);
		debug.draw(g);
		restart.draw(g);
		lastMoveMessage.draw(g);
		
		//draw which move we are on
		g.setColor(Color.white);
		String text = "Move: " + getDisplayNumber();
		g.setFont(new Font("Arial", Font.PLAIN, helpful.TextFitter.getFontSize(g, text, "Arial", 200, 600)));
		g.drawString(text, 100-g.getFontMetrics().stringWidth(text)/2, 145+g.getFontMetrics().getHeight());
		//Drawer.drawCenteredStackString(g, (moves != null ? moves[cursor] : "null"), 100, 300, 200);
		
		game.drawGame(g);
		
		//draw text saying who won if the game is over
		if (game.isOver()){
			switch(game.winner()){
				case 1:
					winText.setText("User won");
					winText.setColors(Color.green, Button.getOppositeColor(Color.green));
					break;
				case 2:
					winText.setText("Opponent Won");
					winText.setColors(Color.red, Button.getOppositeColor(Color.red));
					break;
				default:
					winText.setText("It's a tie");
					winText.setColors(Color.lightGray, Color.black);
			}
			winText.draw(g);
		}
	}
	public String getDisplayNumber(){
		if (moves == null) return "ERROR:RM:gDN:m";
		if (game == null) return "ERROR:RM:gDN:g";
		return game.displayNum(cursor)+"";
	}
	public boolean readMove(){
		if (game == null || moves == null){
			return false; //game was not set up properly, or moves were not set properly
		}
		try{ //make sure any errors encountered are dealt with
			//will read the move then act on it (will do the move)
			if (!game.canGoForward(cursor, moves)) return false;
			step();
			lastMoveMessage.setText(game.goForwards(cursor, moves));
			game.winner();
			
			step.setVisible(game.canGoForward(cursor, moves));
			back.setVisible(game.canGoBack(cursor, moves));
			
			return true;
		}
		catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	public boolean backMove(){
		if (game == null){
			return false; //game was not set properly
		}
		try{
			if (!game.canGoBack(cursor, moves)) return false;
			lastMoveMessage.setText(game.goBack(cursor, moves));
			back();
			game.winner();
			
			step.setVisible(game.canGoForward(cursor, moves));
			back.setVisible(game.canGoBack(cursor, moves));
			
			return true;
		}
		catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	public synchronized void click(MouseEvent e){
		if (step.isClicked(e)){
			readMove();
		}
		if (back.isClicked(e)){
			backMove();
		}
		if (debug.isClicked(e)){
			game.debug();
		}
		if (game.canTouchBoard()){ //pass the click to the game
			game.onClick(e);
		}
		if (restart.isClicked(e)){
			cursor = game.initalize(moves);
		}
	}
	public void press(KeyEvent e){
		if (e.getKeyCode() == 39){ //should be right arrow
			readMove();
		}
		if (e.getKeyCode() == 37){ //should be left arrow
			backMove();
		}
	}
	public void init(){
		if (initialized) return; //already did it
		cursor = game.initalize(moves);
		initialized = true;
		step.setVisible(game.canGoForward(cursor, moves));
		back.setVisible(game.canGoBack(cursor, moves));
	}
}