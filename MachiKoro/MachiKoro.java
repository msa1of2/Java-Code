//General drawing
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

//images
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
//import java.awt.Image;

//events
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.KeyEvent;

//helpful imports
import helpful.GUI.*;
import helpful.*;
import helpful.connection.*;
import helpful.datastructures.*;

public class MachiKoro{// implements Game{
	private static int MAX_PLAYER_COUNT = 5;
	private byte expansion = 0;
	private Landmark[] landmarks;
	private Card[] cards;
	private Player[] playersInOrder;
	private Player user;
	private ConnectionHandler cH;
	private int flags, playerCount;
	private int[] dice;
	private Scalable s;
	private Button inventory, log, clear, swapView, popupClose, popupBack, popupChoose;
	private ScrollBar forwardScroll, popupScroll; //forward scroll is used on the main shop panel, popup is used in every popup
	private DLList<byte[]> logBook; //each array will have a maximum size of 4 b/c of byte protocal in flow.txt
	private byte[] popupInfo;
	private String[] popupNames;
	private Font cardExpansionFont, shopFont, normalFont, cardBasicFont, cardPriceFont;
	
	private boolean viewingShop = true;
	
	//maximum number of flags is 32 beacause of the bit-size of the int variable
	private static int TRADE_TIME = 0; //called by activated card - opens a window to choose your card to give, open next window to choose victim (with back button if looking at cards), choose card to take from victim
	private static int STEAL_FROM_ONE = 1; //called by activated card - opens window to choose victim, upon choosing window closes they lose the money and you gain the money
	private static int STADIUM = 2; //stealing 2 from everyone
	private static int TAXES = 3; //stealing half the coins of those with amt > 10 round down
	private static int PUBLISH = 4; //stealing 1 coin from each player for every bread / coffee they own
	private static int DEMO = 5; //remove 1 of your landmarks for 8 coins
	private static int RENNOVATION = 6; //Show menu of every card currently in game and their counts, have dice roller select 1 card to rennovate - give money for number of cards rennovated - repeat for number of renno companies roller has
	private static int MOVING = 7; //Pop up menu of cards you own, select 1 card, pop up player choosing, give card to player, give roller 4 coins per card given
	private static int EXHIBIT = 8; //Pop up menu of cards you own, select 1 card to activate instead of this card -> continue from there
	private static int PARK = 9; //Add all money owned, divide by player count, ceil the count, give each player that amount of money
	
	
	public MachiKoro(int index){
		//debug stuff
		//for the icon when switched to Game component
		s = new Scalable(800, 600);
		s.preserveAspectRatio(true);
		
		user = new Player("User", landmarks, (byte)0);
		
		dice = new int[]{0, -1};
		//debug stuff ends
		
		//Actual code
		playersInOrder = new Player[MAX_PLAYER_COUNT];
		playersInOrder[0] = user; //DEBUG REMOVE
		
		//Get the number of players actually playing the game
		playerCount = 0;
		for (Player play : playersInOrder){
			if (play != null) playerCount++;
		}
		
		setCards();
		setLandmarks();
		
		//Font stuffs
		normalFont = new Font("Arial", Font.PLAIN, 30);
		cardExpansionFont = normalFont.deriveFont(45f);
		shopFont = normalFont.deriveFont(30f);
		cardBasicFont = normalFont.deriveFont(20f);
		cardPriceFont = cardBasicFont.deriveFont(15f);
		
		
		
		logBook = new DLList<byte[]>(); //will hold all the information of how the game was played
		popupInfo = new byte[20]; //holds the information to display pop-ups based on the numbers as specified in flow.txt
		popupNames = new String[popupInfo.length];
		
		//Buttons!
		//need custom buttons for each pop up
		inventory = new Button(165, 510, 200, 80, Color.lightGray, "Inventory");
		log = new Button(380, 510, 100, 80, Color.lightGray, "Log");
		clear = new Button(751, 451, 48, 48, Color.red, "Clear\nShop");
		popupChoose = new Button(300, 475, 200, 75, Color.green);
		try { //all the image based buttons
            swapView = new Button(751, 76, 48, 48, Color.white, ImageIO.read(new File("ArrowSwap.png"))); // Swaps shop view with inventory?
			popupClose = new Button(700, 50, 50, 50, Color.red, ImageIO.read(new File("Close.png")));
			popupBack = new Button(50, 50, 50, 50, Color.yellow, ImageIO.read(new File("Back.png")));
        } catch (IOException e) {
            swapView = new Button(751, 76, 48, 48, Color.lightGray, "Swap\nShop\nView");
			popupClose = new Button(700, 50, 50, 50, Color.red, "Close");
			popupBack = new Button(50, 50, 50, 50, Color.yellow, "Back");
        }
		
		//Scrollbars
		forwardScroll = new ScrollBar(720, 75, 30, 425, cards.length/4, 1);
		forwardScroll.setColors(new Color[] {Color.lightGray, Color.black, Color.black, Color.darkGray});
		popupScroll = new ScrollBar(720, 100, 30, 450, 2, 2);
		popupScroll.setColors(new Color[] {Color.lightGray, Color.black, Color.black, Color.darkGray});
	}
	
	//Game Methods
	public void drawGame(Graphics g){
		s.setBackgroundColor(new Color(25, 150, 25));
		s.init(g);
		
		s.setFont(normalFont); // money based text
		
		//do the text related stuff barring cards
		s.setColor(Color.black);
		s.drawString("$" + user.totalMoney(), 15, 55);
		s.drawString("Card Count: " + user.getCardCount(), 350-s.getFontMetrics().stringWidth("Card Count: " + user.getCardCount()), 55);
		
		
		//Draw the dice and place for the dice
		s.setColor(new Color(25, 25, 200));
		s.fillRect(375, 0, 150, 75);
		s.setColor(Color.black);
		s.drawRect(375, 0, 150, 75);
		
		//Draw the actual dice
		if (dice[1] == -1){
			drawDice(s, 425, 12, dice[0]);
		}
		else{ //drawing both dice
			drawDice(s, 391, 12, dice[0]);
			drawDice(s, 459, 12, dice[1]);
			
		}
		
		//Draw the landmarks, yellowish orange for bought, gray for yet to have bought, in order but no text
		if (landmarks.length < 7 && false){
			for (int i=0;i<landmarks.length;i++){
				if (landmarks[i].hasOwner(user)) s.setColor(Color.orange);
				else s.setColor(Color.lightGray);
				s.fillRect(550 + i*35, 12, 25, 50);
			}
		}
		else{ //too many landmarks to display, just show count of how many have been bought
			int boughtCount = 0;
			for (Landmark mark : landmarks){
				if (mark.hasOwner(user)) boughtCount++;
			}
			s.drawString("Landmarks: " + boughtCount, 550, 55);
		}
		
		
		//Draw all the players in order aka left bar - 150
		s.setColor(new Color(25, 25, 25));
		s.fillRect(0, 75, 150, 600-70);
		g.setColor(Color.white);
		s.drawString("Players:", 80 - s.getFontMetrics().stringWidth("Players:")/2, 30+40*2+5);
		s.fillRect(0, 35+45*2 + 10, 150, 1);
		
		//s.setColor(Color.white);
		s.drawRect(0, 75, 150, 600-70);
		
		
		//Draw the bottom shelf
		s.setColor(new Color(25, 25, 25));
		s.fillRect(150, 500, 650, 100);
		s.setColor(Color.white);
		s.drawRect(150, 500, 650, 100);
		inventory.draw(s);
		log.draw(s);
		
		//Draw the right bar - 50
		s.setColor(new Color(25, 25, 25));
		s.fillRect(750, 75, 50, 425);
		s.setColor(Color.white);
		s.drawRect(750, 75, 50, 425);
		clear.draw(s);
		swapView.draw(s);
		forwardScroll.draw(s);
		
		//Draw the store area
		s.setFont(shopFont);
		s.setColor(new Color(25, 25, 25));
		s.fillRect(150, 75, 570, 35);
		s.setColor(Color.white);
		s.drawRect(150, 75, 570, 35);
		
		if (viewingShop){
			s.drawString("Shop", 450 - s.getFontMetrics().stringWidth("Shop")/2, 65 + s.getFontMetrics().getHeight());
			
			s.setFont(cardBasicFont);
			drawCard(s, 154, 113, cards[0 + forwardScroll.getStep()*4], false);
			drawCard(s, 296, 113, cards[1 + forwardScroll.getStep()*4], false);
			drawCard(s, 438, 113, cards[2 + forwardScroll.getStep()*4], false);
			drawCard(s, 580, 113, cards[3 + forwardScroll.getStep()*4], false);
			
			if (4 + forwardScroll.getStep()*4 < cards.length) drawCard(s, 154, 306, cards[4 + forwardScroll.getStep()*4], false);
			if (5 + forwardScroll.getStep()*4 < cards.length) drawCard(s, 296, 306, cards[5 + forwardScroll.getStep()*4], false);
			if (6 + forwardScroll.getStep()*4 < cards.length) drawCard(s, 438, 306, cards[6 + forwardScroll.getStep()*4], false);
			if (7 + forwardScroll.getStep()*4 < cards.length) drawCard(s, 580, 306, cards[7 + forwardScroll.getStep()*4], false);
		}
		else{ //viewing landmarks
			s.drawString("Landmarks", 450 - s.getFontMetrics().stringWidth("Landmarks")/2, 65 + s.getFontMetrics().getHeight());
			
			s.setFont(cardBasicFont);
			drawCard(s, 154, 113, landmarks[0 + forwardScroll.getStep()*4], false);
			drawCard(s, 296, 113, landmarks[1 + forwardScroll.getStep()*4], false);
			drawCard(s, 438, 113, landmarks[2 + forwardScroll.getStep()*4], false);
			drawCard(s, 580, 113, landmarks[3 + forwardScroll.getStep()*4], false);
			
			if (4 + forwardScroll.getStep()*4 < landmarks.length) drawCard(s, 154, 306, landmarks[4 + forwardScroll.getStep()*4], false);
			if (5 + forwardScroll.getStep()*4 < landmarks.length) drawCard(s, 296, 306, landmarks[5 + forwardScroll.getStep()*4], false);
			if (6 + forwardScroll.getStep()*4 < landmarks.length) drawCard(s, 438, 306, landmarks[6 + forwardScroll.getStep()*4], false);
			if (7 + forwardScroll.getStep()*4 < landmarks.length) drawCard(s, 580, 306, landmarks[7 + forwardScroll.getStep()*4], false);
		}
		
		
		//Draw any pop-ups
		byte popup = popupInfo[wrap(popupInfo[0]+1, 0, 20)];
		String[] names = new String[] {"Error", "Error"};
		if (popup != 0){
			//Background
			s.setColor(Color.darkGray);
			s.fillRect(50, 50, 700, 500);
			//Title bar
			s.setColor(Color.white);
			names = popupNames[popupInfo[0]].split(";");
			s.setFont(shopFont);
			s.drawString(names[0], 400 - s.getFontMetrics().stringWidth(names[0])/2, 55 + s.getFontMetrics().getHeight());
		}
		
		switch (popup&0x7f){
			case 1: //view inventory / expand on cards - scroll bar
				
				popupScroll.draw(s);
				break;
			case 2: //choose a player
				break;
			case 3: //specific player information
				break;
			case 5: //card expansion, full view of the card
				break;
			case 7: //log, display the log based on the information in the list, with scroll bar
				break;
			case 8: //exit menu
				break;
		}
		if (popup != 0){
			//Draw nav buttons
			popupClose.draw(s);
			if ((popup&0x7f) > 1 && (popup&0x7f) < 7) popupBack.draw(s); //exclude log, exit menu and inventory from the back button
			if ((popup&0x80) ==1){
				popupChoose.setText(names[1]);
				popupChoose.draw(s);
			}
			
		}
		
	}
	public void drawDice(Scalable s, int x, int y, int num){ //checked - all looks good - 3/6/25
		num %= 10; //ensure the number given will fit on our dice
		s.setColor(Color.white);
		s.fillRect(x, y, 50, 50);
		s.setColor(Color.black);
		switch(num/2){
			case 4: //8, 9
				s.fillOval(x+19, y+2, 12, 12); //top middle
				s.fillOval(x+19, y+35, 12, 12);//bottom middle
			case 3: //6, 7
				s.fillOval(x+2, y+19, 12, 12); //middle left
				s.fillOval(x+35, y+19,12, 12); //middle right
			case 2: //4, 5
				s.fillOval(x+2, y+2, 12, 12); //top left
				s.fillOval(x+35, y+35, 12, 12); //bottom right
				//others get filled in by below conditions:
			case 1: //2, 3
				s.fillOval(x+2, y+35, 12, 12); //bottom left
				s.fillOval(x+35, y+2, 12, 12); //top right
		}
		if ((num&0b1) == 1){ //odd
			s.fillOval(x+19, y+19, 12, 12); //middle middle
		}
	}
	public void drawCard(Scalable s, int x, int y, Card card, boolean expanded){
		//get the color for the background of the card
		switch (card.getTypeNum()){
			default:
				s.setColor(Color.blue);
				break;
			case 1:
				s.setColor(new Color(50, 200, 50));
				break;
			case 2:
				s.setColor(Color.red);
				break;
			case 3:
				s.setColor(Color.magenta.darker());
				break;
			case 4:
				s.setColor(Color.yellow.darker());
				break;
		}
		
		//3:4 aspect ratio, 3 wide : 4 tall
		if (expanded){
			s.fillRect(x, y, 375, 500);
		}
		else{
			s.fillRect(x, y, 135, 189); //maximum size of 142, 212
			
			s.setColor(Color.white);
			s.setFont(cardBasicFont);
			
			if (!(card instanceof Landmark)){ //only draw activation numbers for non-landmarks
				//activation numbers
				
				int[] acts = card.getActivationNums();
				if (acts[0] == acts[1]){ //single number for activation
					s.drawString(acts[0]+"", x+71-s.getFontMetrics().stringWidth(acts[0]+"")/2, y-2+s.getFontMetrics().getHeight());
				}
				else{
					s.drawString(acts[0]+"-"+acts[1], x+71-s.getFontMetrics().stringWidth(acts[0]+"-"+acts[1])/2, y-2+s.getFontMetrics().getHeight());
				}
			}
			
			//name
			Drawer.drawCenteredStackString(s, card.getName(), x+69, y+s.getFontMetrics().getHeight()*2, 125); //centers text and splits it to stay on the card by going under the other words
			
			//count of the card
			s.setColor(Color.white);
			s.drawString(card.count()+"", x+6, y+189-6);
			
			//price
			s.setColor(new Color(150, 150, 25));
			s.fillOval(x+135-35, y+189-35, 30, 30);
			s.setColor(new Color(220, 220, 25));
			s.fillOval(x+135-30, y+189-30, 20, 20);
			s.setColor(Color.black);
			s.setFont(cardPriceFont);
			Drawer.drawCenteredStackString(s, card.cost()+"", x+135-20, y+189-27, 100);
		}
	}
	
	public int wrap(int num, int bottom, int top){
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
	
	/* UI
	  * Player tabs on the left side showing: name, current money, current card count, current activated landmark count - click for more detailed info on the player
	   * Opens obvious submenu - x in top right corner, player name top center, $ below, list of all landmarks, list of all cards x amount there
	  * Money on top left corner
	   * Card count on top left immedate left of dice section
	   * Click for logs of where money was spent and how money was earned
	  * Dice on the top center
	 * Loop:
	  * your turn -> choose 1 or 2 dice (if available) -> Roll dice (with choice) -> go through cards, find ones that activate with dice total
	  -> for each activated card, go through owners and give arguments -> shop ungrays -> choose to buy or pass -> next turn
	 */
	 
	/* TODO:
	 * Card activation evaluation
	 ** All the symbols for accessing information the cards need
	 ** Specifically: R0-inf, R#, A0-inf
	 * Display info for the cards
	 * Menu popup to display extra information -> not a class but a collection of information as to the nature to the pop up and text to be displayed
	 */
	public Landmark[] setLandmarks(){
		if (landmarks != null) return landmarks;
		landmarks = new Landmark[expansionLandmarkCount(expansion)];
		landmarks[0] = new Landmark("Train station", 4);
		landmarks[1] = new Landmark("Shopping mall", 10);
		landmarks[2] = new Landmark("Amusement Park", 16);
		landmarks[3] = new Landmark("Radio Tower", 22);
		if ((expansion & 0b1) == 0b1){ //harbor expansion
			landmarks[4] = new Landmark("City Hall", 0); //for every player, set this to purchased
			landmarks[5] = new Landmark("Harbor", 2);
			landmarks[6] = new Landmark("Airport", 30);
			
			for (Player play: playersInOrder){
				if (play == null) continue;
				play.purchaseCard(landmarks[4]);
			}
		}
		this.landmarks = landmarks;
		return landmarks;
		//base game is always going to exist - Train station (Player chooses to roll 1 or 2 die instead of only 1) - Shopping mall (Player's bread and coffer icons gain +1 income) - Amusement Park (If player rolls doubles, extra turn after this one) - Radio Tower (Player chooses to re-roll dice once every turn)
		//Harbor Expansion - bit #0 (1 bit) - city hall(gives a coin before buying phase if player has none) - Harbor (Player chooses to add two to dice roll of 10+) - Airport (Player gets 10 coins if choose to not build anything that turn)
		//Millionaire's row - bit #1 (2 bit) - None
	}
	public Card[] setCards(){
		if (cards != null) return cards;
		cards = new Card[expansionCardCount(expansion)];
		
		//base game
		cards[0] = new Card("Wheat Field",		 "Get 1 coin from the bank on anyone's turn", 			MAX_PLAYER_COUNT+1, 1, 0, 9, "1*PC", 1); //wheat icon = 9
		cards[1] = new Card("Ranch",			 "Get 1 coin from the bank on anyone's turn", 			MAX_PLAYER_COUNT+1, 1, 0, 2, "1*PC", 2); //cow icon = 2
		cards[2] = new Card("Bakery",			 "Get 1 coin from the bank on your turn only",			MAX_PLAYER_COUNT+1, 1, 1, 1, "1*PC", 2, 3); //bread icon = 1
		cards[3] = new Card("Cafe", 			 "Get 1 coin from the player who rolled the dice",		MAX_PLAYER_COUNT+1, 2, 2, 3, "1*PC", 3); //coffee icon = 3
		cards[4] = new Card("Convenience Store", "Get 3 coins from the bank on your turn only", 		MAX_PLAYER_COUNT+1, 2, 1, 1, "3*PC", 4);
		cards[5] = new Card("Forest",			 "Get 1 coin from the bank on anyone's turn", 			MAX_PLAYER_COUNT+1, 3, 0, 6, "1*PC", 5); //gear icon = 6
		cards[6] = new Card("Stadium",			 "Get 2 coins from each player on your turn only", 		playerCount, 		6, 3, 7, "2",	 6); //landmark icon = 7
		cards[7] = new Card("TV Station",		 "Get 5 coins from any one player on your turn onlu", 	playerCount, 		7, 3, 7, "5",	 6);
		cards[8] = new Card("Business Center",	 "Trade one non-establishment building with another player on your turn only", playerCount, 8, 3, 7, "0", 6);
		cards[9] = new Card("Cheese Factory",	 "Get 3 coins from the bank for each cow icon you own on your turn only", MAX_PLAYER_COUNT+1, 5, 1, 4, "3*T2*PC", 7); //industry icon = 4
		cards[10]= new Card("Furniture Factory", "Get 3 coins from the bank for each gear icon you own on your turn only",MAX_PLAYER_COUNT+1, 3, 1, 4, "3*T6*PC", 8);
		cards[11]= new Card("Mine",				 "Get 5 coins from the bank on anyone's turn",			MAX_PLAYER_COUNT+1, 6, 0, 6, "5*PC", 9);
		cards[12]= new Card("Family Restaurant", "Get 2 coins from the player who rolled the dice",		MAX_PLAYER_COUNT+1, 3, 2, 3, "2*PC", 9, 10);
		cards[13]= new Card("Apple Orchard",	 "Get 3 coins from the bank on anyone's turn",			MAX_PLAYER_COUNT+1, 3, 0, 9, "3*PC", 10);
		cards[14]= new Card("Fruit & Vegetable Market", "Get 2 coins from the bank for each wheat icon you own on your turn only",  MAX_PLAYER_COUNT+1, 2, 1, 5, "2*T9*PC", 10, 12); //fruit icon = 5
		
		if ((expansion&0b1) == 0b1){ //harbor expansion
			cards[15] = new Card("Sushi Bar", 		"If you have a harbor get 3 coins from the player who rolled the dice", 		MAX_PLAYER_COUNT+1, 2, 2, 4, "3*PC*L5",	1);
			cards[16] = new Card("Flower Orchard", 	"Get 1 coin from the bank on your anyone's turn", 								MAX_PLAYER_COUNT+1, 2, 0, 9, "1*PC", 	4);
			cards[17] = new Card("Flower Shop", 	"Get 1 coin from the bank for each Flower Garden you own on your turn only",	MAX_PLAYER_COUNT+1, 1, 1, 1, "1*R16", 	6);
			cards[18] = new Card("Publisher",		"Get 1 coin from each player for every coffee and bread icon card they have on your turn only", playerCount, 5, 3, 7, "1", 7);
			cards[19] = new Card("Pizza Joint",		"Get 1 coin from the player who rolled the dice",								MAX_PLAYER_COUNT+1, 1, 2, 3, "1*PC",	7);
			cards[20] = new Card("Tax Office",		"For each of your opponents with 10 or more coins, take half rounded down on your turn only", playerCount, 4, 3, 7, "5", 8, 9);
			cards[21] = new Card("Mackeral Boat",	"If you have a harbor, get 3 coins from the bank on anyone's turn",				MAX_PLAYER_COUNT+1, 2, 0, 2, "3*PC*L5",	8);
			cards[22] = new Card("Hamburger stand",	"Take 1 coin from the player who rolled the dice",								MAX_PLAYER_COUNT+1, 1, 2, 3, "1*PC",	8);
			cards[23] = new Card("Tuna Boat",		"On anyone's turn, the current player rolls 2 dice, if you have a harbor, you get as many coins as the dice total", MAX_PLAYER_COUNT+1, 5, 0, 2, "7", 12, 14);
			cards[24] = new Card("Food Warehouse",	"Get 2 coins from the bank for each coffee icon you own on your turn only",		MAX_PLAYER_COUNT+1, 2, 1, 4, "2*T3", 12, 13);
		}
		
		if ((expansion&0b10) == 0b10){ //millionaire's row expansion
			cards[25] = new Card("General Store",		"If you have less than 2 constructed landmarks, get 2 coins from the bank on your turn only",			MAX_PLAYER_COUNT+1, 0, 1, 1, "2*PC*(2>L#)",	2);
			cards[26] = new Card("Corn Field",			"If you have less than 2 landmarks built, get 1 coin form the bank on anyone's turn",					MAX_PLAYER_COUNT+1, 2, 0, 9, "1*PC*(2>L#)",	3, 4);
			cards[27] = new Card("Demolition Company",	"If possible, you must demolish 1 of your constructed landmarks to yeild 8 coins on your turn only",	MAX_PLAYER_COUNT+1, 2, 1, 8, "8*PC", 		4); //custom act
			cards[28] = new Card("Loan Office",			"Gain 5 coins on purchase, when activated, pay 2 coins back to the bank on your turn only",				MAX_PLAYER_COUNT+1, -5,1, 8, "-2*PC",		5,6);
			cards[29] = new Card("French Restaurant",	"If the player who rolled has 2 or more constructed landmarks, get 5 coins from them",					MAX_PLAYER_COUNT+1, 3, 2, 3, "5*PC*((L#=2)+(L#>2))", 5);
			cards[30] = new Card("Vineyard",			"Get 3 coins from the bank on anyone's turn",															MAX_PLAYER_COUNT+1, 3, 0, 9, "3*PC", 		7);
			cards[31] = new Card("Renovation Company",	"All buildings owned by every player of your choosing are closed for renovations, get 1 coin from each player for the number of buildings you close", playerCount, 4, 3, 7, "1", 8); //custom act
			cards[32] = new Card("Moving Company",		"You must a give building of your choosing to another player, you also gain 4 coins from the bank",		MAX_PLAYER_COUNT+1, 2, 1, 8, "4*PC",		9,10); //custom act
			cards[33] = new Card("Winery",				"Get 6 coins for each vineyard you own, then close this building for renovation",						MAX_PLAYER_COUNT+1, 3, 1, 4, "6*R30",		9); //custom act (give money then close it down)
			cards[34] = new Card("Tech Startup",		"At the end of each of your turns, you may add 1 coin to this card, when this card is activated, get an amount equal to the coins on this card from every player", playerCount, 1, 3, 7, "10", 10); //ui step in loop to add to this card
			cards[35] = new Card("International Exhibit hall", "Choose another card to activate for only yourself, only that card and clones you have will activate", playerCount, 7, 3, 7, "0", 10); //custom act
			cards[36] = new Card("Soda Bottling plant",	"Get 1 coin from the bank for every coffee icon owned by all players on your turn only",				MAX_PLAYER_COUNT+1, 5, 1, 4, "1*A3", 11);
			cards[37] = new Card("Park",				"Redistribute all players' coins evenly among all players, if there is an uneven amount, take the difference from the bank", playerCount, 3, 3, 7, "0", 11, 13); //custom act
			cards[38] = new Card("Members only club",	"If the player who rolled has 3 or more constructed landmarks, get all of their coins", 				MAX_PLAYER_COUNT+1, 4, 2, 3, "L#>2", 12, 14); //custom act
		}
		return cards;
	}
	public int expansionCardCount(byte expansion){
		int send = 15;
		if (expansion == 0) return send;
		send += 10;
		if (((expansion&0b1) << 1) > expansion) return send; //if this is the largest expansion requested, return early
		send += 14;
		if (((expansion&0b10)<< 1) > expansion) return send; 
		//add other expasions if they exist here, add their card counts for each expansion in order
		
		return send;
	}
	public int expansionLandmarkCount(byte expansion){
		int send = 4;
		if (expansion == 0) return send;
		send += 3;
		if (((expansion *0b1) << 1) > expansion) return send;
		//add other expansions if they exist here, add their landmark card counts for each expansion in order
		
		return send;
	}
	
	public void setFlag(int bitIndex, boolean value){
		if (value){
			flags |= (1<<bitIndex); //flips flag to 1
			return;
		}
		flags &= ~(1<<bitIndex); //flips flag to 0
		return;
	}
	public boolean getFlag(int bitIndex){
		return ((flags >> bitIndex) &0b1) == 1;
	}

	//Event handling
	public void onClick(MouseEvent e){ // mouse button clicked
		s.convertMouseEvent(e);
		//System.out.println(e.getX() + ", " + e.getY());
		if (popupInfo[wrap(popupInfo[0]+1, 0, popupInfo.length)] == 0){ //popups are down, aka no popups up
			if (inventory.isClicked(e)){ //pressing the inventory button
				popupInfo[0] = 0; //reset popups
				popupInfo[1] = 1; //type 1, no choose button
				popupNames[0] = "Inventory";
				return;
			}
			if (log.isClicked(e)){
				popupInfo[0] = 0;
				popupInfo[1] = 6; //type 6, no choose button
				popupNames[0] = "Log";
				return;
			}
			//clear.click(e);
			if (swapView.isClicked(e)){
				if (viewingShop){ //from shop to landmark viewing
					forwardScroll.setSize(landmarks.length/4);
					forwardScroll.setStep(forwardScroll.getStep());//ensures current step is withing the bounds
				}
				else{ //from landmarks to shop viewing
					forwardScroll.setSize(cards.length/4);
					forwardScroll.setStep(forwardScroll.getStep()); //ensures current step is withing the bounds
				}
				
				viewingShop = !viewingShop;
				return;
			}
			forwardScroll.click(e);
		}
		else{ //popups are up
			if (popupClose.isClicked(e)){
				popupInfo[0] = 0;
				popupInfo[1] = 0; //exit from the popups
				popupNames[0] = "Error";
				return;
			}
			if (popupBack.isClicked(e) && popupInfo[0] > 1){
				popupNames[popupInfo[0]] = "Error";
				popupInfo[popupInfo[0]] = 0; //remove old popup from memory
				popupInfo[0] = (byte)helpful.Math.max(0, popupInfo[0]-1); //min of 0 index
				return;
			}
			//popupChoose.click(e);
			popupScroll.click(e);
		}
	}
	public void mousePressed(MouseEvent e){} // mouse button pressed
	public void mouseReleased(MouseEvent e){} // mouse button relased
	public void mouseEntered(MouseEvent e){} // mouse entered screen
	public void mouseExited(MouseEvent e){} // mouse exited screen
	public void keyPressed(KeyEvent e){} // key is pressed
	public void keyReleased(KeyEvent e){} //key is relased
	public void typed(KeyEvent e){} //key is typed
	public void mouseWheelMoved(MouseWheelEvent e) { // mouse wheel moved, either up or down scroll
		if (popupInfo[wrap(popupInfo[0]+1, 0, 20)] == 0){
			//System.out.println(forwardScroll.getStep() + ", " + forwardScroll.getSize());
			forwardScroll.click(e); //only affect the store / forefront scroll bar when the popups are away
			return;
		}
		popupScroll.click(e); //affect the popup scroll bar otherwise
	}

	
	private class Player{
		private int money;
		private String name;
		private DLList<Card> cards; //just contains the copy of the bought cards, this count will be the number owned by the player
		private Landmark[] landmarks; //the landmark cards they are required to buy
		public Player(String name, Landmark[] landmarks, byte expansion){ //expansionPack is asking which expansion the player is playing which determines how many and what type of landmarks they have
			this.name = name;
			this.landmarks = landmarks;
			this.money = 3;
			this.cards = new DLList<Card>();
			
			if ((expansion & 0b1) == 0b1){
				//harbor expansion, purchase the city hall at the beginning
				purchaseCard(landmarks[4]);
			}
			
		}
		
		public int totalMoney(){
			return money;
		}
		public int getCardCount(){
			return cards.size();
		}
		
		public boolean purchaseCard(Card item){
			if (money < item.price()) return false; //not enough money to purchase card
			if (!item.canBuy()) return false; //not enough slots for player to enter
			boolean res = item.addOwner(this); //find out if the card will accept us
			if (!res) return false; //adding owner went wrong
			money -= item.price(); //we paid for this, money goes bye-bye
			if (!(item instanceof Landmark)) cards.add(item); //dont add our landmarks to our list / inventory
			return true;
		}
		public void income(int amount){
			//adds amount to our money count, works with negative numbers
			this.money += amount;
			if (money < 0) money = 0;
		}
		public int steal(int amount){
			//removes this amount from this player's savings but keeps the value equal to or above 0, returns actual amount stolen
			this.money-=amount;
			if (money < 0){
				int send = money*-1;
				money = 0;
				return amount-send;
			}
			return amount;
			
		}
		
		public int getCardCount(Card type){ //counts the number of these cards this user has
			for (Card card : cards){
				if (card.equals(type)){
					return card.count();
				}
			}
			return 0;
		}
		public int getCardIconCount(int icon){ //counts the number of cards this user has with this icon
			int send = 0;
			for (Card card : cards){
				if (card.equalsIcon(icon)){
					send += card.count();
				}
			}
			return send;
		}
		public boolean hasLandmark(int num){
			return landmarks[num] != null;
		}
		public int getUniqueTypes(){ //returns the number of unique establishment types this player has
			boolean[] seen = new boolean[Card.typeNames.length];
			for (Card card : cards){
				if (!seen[card.getTypeNum()]) seen[card.getTypeNum()] = true;
			}
			int send = 0;
			for (boolean bool : seen){
				send++;
			}
			return send;
		}
		public int getLandmarkCount(){ //returns the number of landmarks this player owns
			int send = 0;
			for (Landmark landmark : landmarks){
				if (landmark.hasOwner(this)) send++;
			}
			return send;
		}
	}
	private class Card{
		private String name, descriptiveText;
		private int cardCount, coinCost, type, icon;
		private Player[] owners;
		private String moneyEquation;
		private int lowerDiceActivation, higherDiceActivation;
		private boolean singleCopy = false; //if set to true, only 1 card per player
		
		public final static String[] typeNames = new String[] {"primary", "secondary", "restaurant", "major", "landmark"};
		public final static String[] iconNames = new String[] {"boat", "bread", "cow", "coffee", "industry", "fruit", "gear", "landmark", "suitcase", "wheat"};
		
		//constructors
		public Card(String name, String bodyText, int cardCount, int coinCost, int type, int icon, String moneyEquation, int lowerDiceActivation, int higherDiceActivation){
			this.name = name;
			this.descriptiveText = bodyText;
			this.cardCount = cardCount;
			this.coinCost = coinCost;
			this.type = type;
			this.icon = icon;
			owners = new Player[cardCount];
			this.moneyEquation = moneyEquation;
			this.lowerDiceActivation = lowerDiceActivation;
			this.higherDiceActivation = higherDiceActivation;
		}
		public Card(String name, String bodyText, int cardCount, int coinCost, int type, int icon, String moneyEquation, int diceActivation){
			this(name, bodyText, cardCount, coinCost, type, icon, moneyEquation, diceActivation, diceActivation);
		}
		public Card(String name, String bodyText, int cardCount, int coinCost, String type, String icon, String equation, int lowerDiceActivation, int higherDiceActivation){
			this(name, bodyText, cardCount, coinCost, -1, -1, equation, lowerDiceActivation, higherDiceActivation);
			for (int i=0;i<typeNames.length;i++){
				if (type.toLowerCase().equals(typeNames[i])){this.type = i;break;}
			}
			for (int i=0;i<iconNames.length;i++){
				if (icon.toLowerCase().equals(iconNames[i])){this.icon = i;break;}
			}
		}
		public Card(String name, String bodyText, int cardCount, int coinCost, String type, String icon, String equation, int diceActivation){
			this(name, bodyText, cardCount, coinCost, type, icon, equation, diceActivation, diceActivation);
		}
		
		public boolean hasOwners(){ //returns true if it has any owners
			return owners[0] != null;
		}
		public boolean hasOwner(Player player){
			//returns true if this player shows up in the owners list
			for (Player owner : owners){
				if (owner == player) return true;
			}
			return false;
		}
		public boolean isActivated(int diceTotal){
			return diceTotal <= higherDiceActivation && diceTotal >= lowerDiceActivation;
		}
		public int activate(Player diceRoller, int playerCount){
			//will go thorugh the entire player list and act on them
			if (!hasOwners()) return 1;
			
			//find where the dice roller is and start our loop from there
			int offset = 0;
			for (int i=0;i<playersInOrder.length;i++){
				if (playersInOrder[i] == diceRoller) {offset = i;break;}
			}
			
			for (int i=0;i<MAX_PLAYER_COUNT;i++){
				Player play = playersInOrder[wrap(offset+i, 0, MAX_PLAYER_COUNT)];
				if (play == null) continue;
				switch (this.type){
					case 0: //blue card - don't need to check who rolled the dice
						play.income(parseEquation(moneyEquation, play));
						break;
					case 1: //green card - only dice roller can get money
						if (play == diceRoller) play.income(parseEquation(moneyEquation, play));
						break;
					case 2: //red card - dice roller loses money based on cards of the other players - auto ordered so next in turn order will get ability to steal coins first
						if (play == diceRoller) break; //can't steal own money
						int amount = helpful.Math.max(parseEquation(moneyEquation, play), 0); //player can't go into debt
						play.income(amount); //player with card gets money from this player
						diceRoller.income(-amount); //we lose the money
						break;
					case 3: //purple cards - stealing money from others
						if (play != diceRoller) break; //purples only activate on owner's turn
						if (name.toLowerCase().equals("business center")) setFlag(TRADE_TIME, true); //custom power to force a trade of cards with another player
						if (name.toLowerCase().equals("tv station")) setFlag(STEAL_FROM_ONE, true); //custom power to steal 5 coins from any player
						if (name.toLowerCase().equals("stadium")) setFlag(STADIUM, true); //custom power to steal 2 coins from every player
						if (name.toLowerCase().equals("tax office")) setFlag(TAXES, true); //
						if (name.toLowerCase().equals("publisher")) setFlag(PUBLISH, true);
				}
			}
			//type == 1 -> all owners get money
			//type == 2 -> diceRoller gets money if they own any card
			//type == 3 -> diceRoller loses money to owners of card
			//type == 4 -> diceRoller gains money if they have this card
			//type == 5 -> nothing happens
			return 0;
			
		}
		public int parseEquation(String equation, Player player){
			/* money equation things
			1*PC

			PC = playerCount - number of this card the player owns
			T# = type (number) returns the player's count of that type of card, if just # - returns number of unique types
			L# = Landmark [number] returns 1 if activated, 0 otherwise, if just # - returns number of landmarks
			MO = Total amount of money owned by this player
			
			R# = card[number] returns the number of this card index the player owns - TODO, if just # - returns number of cards this player owns
			A# = The total number of cards of this type number from every player
			
			
			* / - + () < > =
			
			*/
			
			//convert into the count of this card by the player
			String[] splitEnd = equation.split("PC");
			equation = String.join("" + player.getCardCount(this), splitEnd);
			//convert into the total amount of money owned by this player
			splitEnd = equation.split("MO");
			equation = String.join("" + player.totalMoney(), splitEnd);
			//convert into the count of a type by the player
			splitEnd = equation.split("T");
			for (int i=1;i<splitEnd.length;i++){
				int iconNum = 0;
				for (char letter : splitEnd[i].toCharArray()){
					if (!Character.isDigit(letter)) break;
					iconNum *= 10;
					iconNum += (int)letter - 48;
				}
				if (splitEnd[i].charAt(0) == '#'){
					splitEnd[i] = player.getUniqueTypes() + splitEnd[i].substring(1);
				}
				else{
					splitEnd[i] = player.getCardIconCount(iconNum) + splitEnd[i].substring((int)java.lang.Math.floor(java.lang.Math.log10(iconNum)));
				}
			}
			equation = String.join("", splitEnd);
			//convert landmark number into 1/0
			splitEnd = equation.split("L");
			for (int i=1;i<splitEnd.length;i++){
				int landmarkNum = 0;
				for (char letter : splitEnd[i].toCharArray()){
					if (!Character.isDigit(letter)) break;
					landmarkNum *= 10;
					landmarkNum += (int)letter - 48;
				}
				if (splitEnd[i].charAt(0) == '#'){
					splitEnd[i] = player.getLandmarkCount() + splitEnd[i].substring(1);
				}
				else{
					splitEnd[i] = (player.hasLandmark(landmarkNum) ? "1":"0") + splitEnd[i].substring((int)java.lang.Math.floor(java.lang.Math.log10(landmarkNum)));
				}
			}
			equation = String.join("", splitEnd);
			return (int)helpful.Math.calculate(equation); //should only be numbers and symbols now
		}
		
		public int price(){
			return this.coinCost;
		}
		public boolean canBuy(){ //if this card has filled up with owners, will return false
			return owners[owners.length-1] == null;
		}
		public boolean addOwner(Player owner){
			if (!canBuy()) return false;
			for (int i=0;i<owners.length;i++){
				if (type == 3 || type == 4){
					//major (purple) or landmark card
					if (owners[i] == owner) return false; //cannot purchase two of these types of cards
				}
				if (owners[i] == null){
					owners[i] = owner;
					return true;
				}
			}
			return false;
		}
		
		public Card copy(){ //for use by the players to keep track of 
			return new Card(name, descriptiveText, 0, coinCost, type, icon, moneyEquation, lowerDiceActivation, higherDiceActivation);
		}
		
		public int hashCode(){
			return name.hashCode();
		}
		public boolean equals(Object other){
			if (other instanceof Card){
				Card o = (Card)other;
				return this.name.equals(o.getName()); //kinda slow but oh well
			}
			return false;
		}
		public boolean equalsType(int type){
			return this.type == type;
		}
		public boolean equalsIcon(int icon){
			return this.icon == icon;
		}
		
		public int[] getActivationNums(){
			return new int[] {lowerDiceActivation, higherDiceActivation};
		}
		public String getName(){
			return this.name;
		}
		public String getDescription(){
			return this.descriptiveText;
		}
		public int count(){
			return this.cardCount;
		}
		public int cost(){
			return this.coinCost;
		}
		public int getTypeNum(){
			return type;
		}
		public int getIconNum(){
			return icon;
		}
	}
	private class Landmark extends Card{
		public Landmark(String name, int coinCost){
			super(name, "landmark", MAX_PLAYER_COUNT, coinCost, 4, 7, "", 0, 0);
		}
	}
}