import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import helpful.Scalable;
import helpful.Drawer;
import helpful.TextFitter;

import java.awt.Color;
import java.awt.Font;

public class Card{
	private int health, shield, boost;
	private final int attack, rowActivation, count, cost;
	private final String name;
	private BufferedImage sprite;
	private static BufferedImage atkImg, hpImg, boostImg, shieldImg, romanI, romanII, romanIII;
	private boolean isDead;
	private Element type;
	private TextFitter nameFontSize, numberFontSizes;
	
	//Bit flags for which row is activated
	public static int ROWI = 1, ROWII = 2, ROWIII=4;
	
	
	public Card(String name, int hp, int atk, int cost, int rowAct, int count, Element type, BufferedImage img){
		this.name = name; //the name of the card
		this.health = hp; //base health of this card
		this.attack = atk; //base attack power of this card
		this.cost = cost; //cost in gold of this card
		this.rowActivation = rowAct; //which rows is the card valid to operate (desc. works)
		this.count = count; //the count of this card in the shop for population at initalisation
		this.sprite = img; //the sprite to display when drawing the card
		this.type = type; //used for the drawing of the appropriate background
		
		//shield and boost are 0 at the beginning
		this.shield = 0;
		this.boost = 0;
		
		//When enabled to 'true,' values cannot be changed
		this.isDead = false;
		
		//Fits the text of the card to specified boxes and saves the size - most efficent method is to draw all cards at 1 height before moving onto the next height
		nameFontSize = new TextFitter(50, 30, name);
		numberFontSizes = new TextFitter(10, 10, "0");
		
		//Load the images used by every card
		try{
			if (atkImg == null) atkImg = ImageIO.read(new File("CardBkgs/Damage Bkg.png"));
			if (hpImg == null) hpImg = ImageIO.read(new File("CardBkgs/Health Bkg.png"));
			if (boostImg == null) boostImg = ImageIO.read(new File("CardBkgs/Boost Bkg.png"));
			if (shieldImg == null) shieldImg = ImageIO.read(new File("CardBkgs/Shield Bkg.png"));
			
			if (romanI == null) romanI = ImageIO.read(new File("CardBkgs/Roman1.png"));
			if (romanII == null) romanII = ImageIO.read(new File("CardBkgs/Roman2.png"));
			if (romanIII == null) romanIII = ImageIO.read(new File("CardBkgs/Roman3.png"));
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//--Health access and changes--
	public int getHealth(){
		return health;
	}
	//Adds amt to health, returns true if the card still has health
	public boolean deltaHealth(int amt){
		if (isDead) return false;
		this.health += amt;
		if (this.health > 0) return true;
		isDead = true;
		return false;
	}
	//Deals the amt from shield / health as appropriate, returns true if success, false if it failed somewhere
	public boolean takeDamage(int amt){
		if (isDead) return false;
		if (amt <= 0) return false;
		this.shield -= amt;
		if (this.shield < 0){
			//damage dealt to health
			deltaHealth(this.shield);
			this.shield = 0; //guarenteed wipe, also returns value to positive
			return true;
		}
		if (GlobalVars.getBit(GlobalVars.SHIELD_REMOVED_AFTER_ANY_DAMAGE, GlobalVars.settings))
			//we took an amount of damage, and the shields should be voided
			this.shield = 0;
		
		return true;
	}
	
	//--Shield access and changes--
	//Clears the shield value
	public void clearShield(){
		if (isDead) return;
		this.shield = 0;
	}
	//Adds amt to shield, protects against negative values, doesnt preform a wipe regardless of the settings
	public void deltaShield(int amt){
		if (isDead) return;
		this.shield += amt;
		if (this.shield < 0) this.shield = 0;
	}
	
	//--Damage dealing access and changes--
	public int getAttack(){
		return this.attack;
	}
	public int getDamage(){
		return this.attack + this.boost;
	}
	public int getBoost(){
		return this.boost;
	}
	//Adds amt to boost, w/ negative protection
	public void deltaBoost(int amt){
		if (isDead) return;
		this.boost += amt;
		if (this.boost < 0) this.shield = 0;
	}
	
	//--MISC access to the various variables required by the rest of the program--
	//Returns the rows this card is valid within with bit-wise representation - index 0 = Row 1, 1=R2, 2=R3
	public int getRowAct(){
		return this.rowActivation;
	}
	//Returns the total number of this card type that exist in the shop
	public int getCount(){
		return count;
	}
	//Returns the cost in gold of this card
	public int getCost(){
		return this.cost;
	}
	//Returns the boolean value representing if this card is dead or not
	public boolean isDead(){
		return isDead;
	}
	//Returns the name of this card
	public String getName(){
		return this.name;
	}
	//Returns the sprite used by the card for display purposes
	public BufferedImage getImg(){
		return this.sprite;
	}
	
	//Draws the current card to the screen at (x,y) with a height of height and width of 2/3 * height
	public void draw(Scalable s, int x, int y, int height){
		int width = height*2/3;
		//Draw the background of the card
		s.setColor(type.bkg);
		s.fillRect(x, y, width, height);
		
		//Draw the background images
		s.drawImage(atkImg, x, y, height/5, height/5, null);
		s.drawImage(boostImg, x, y+height/5, height/5, height/5, null);
		s.drawImage(hpImg, x+width-height/5, y, height/5, height/5, null);
		s.drawImage(shieldImg, x+width-height/5, y+height/5, height/5, height/5, null);
		
		//Draw the valid row activations
		s.setColor(new Color(192, 192, 192, 206));
		s.drawImage(romanI, x-height/20, y+height-height/5, height/5, height/5, null);
		if ((rowActivation & ROWI) == 0) s.fillRect(x, y+height-height/5, height/7, height/5);
		
		s.drawImage(romanII, x+height/6, y+height-height/5, height/5, height/5, null);
		if ((rowActivation & ROWII) == 0) s.fillRect(x+height/7, y+height-height/5, (int)(height/3.5), height/5);
		
		s.drawImage(romanIII, x+height/20+height*2/5, y+height-height/5, height/5, height/5, null);
		if ((rowActivation & ROWIII) == 0) s.fillRect(x+height/40+height*2/5, y+height-height/5, height/4, height/5);
		
		
		//Draw the numbers and name of the card
		s.setColor(Color.black);
		if (height < 200){
			nameFontSize.setBounds(width-4, height*2/5);
			s.setFont(new Font("Arial", Font.PLAIN, nameFontSize.getFontSize(s)));
			Drawer.drawCenteredStackString(s, name, x+width/2, y+(int)(height*2.5/5), width);
		}
		else{
			nameFontSize.setBounds(width-height*2/5, height*2/5);
			s.setFont(new Font("Arial", Font.PLAIN, nameFontSize.getFontSize(s)));
			Drawer.drawCenteredStackString(s, name, x+height*13/40, y+5, width);
		}
		
		numberFontSizes.setBounds(height/5, height/5);
		s.setFont(new Font("Arial", Font.BOLD, numberFontSizes.getFontSize(s)));
		Drawer.drawCenteredStackString(s, this.attack + "", x+width*3/20, y+height/24, height/7);
		Drawer.drawCenteredStackString(s, this.boost + "", x+width*3/20, y+height*6/24, height/7);
		Drawer.drawCenteredStackString(s, this.health + "", x+width*17/20, y+height/24, height/7);
		Drawer.drawCenteredStackString(s, this.shield + "", x+width*17/20, y+height*6/24, height/7);
		
		//Draw the main sprite when the card is big enough to appreciate the art
		if (height >= 200){
			if (sprite!= null){
				
			} 
			else{
				s.setColor(Color.magenta);
				s.fillRect(x+height*3/20, y+height*2/5, height/5, height/5);
				s.fillRect(x+height*7/20, y+height*3/5, height/5, height/5);
				s.setColor(Color.black);
				s.fillRect(x+height*3/20, y+height*3/5, height/5, height/5);
				s.fillRect(x+height*7/20, y+height*2/5, height/5, height/5);
				
			}
		}
	}
	
	public enum Element{
		DROPLET (Color.cyan, "The Droplet faction focuses on card advantage and formation manipulation with a balance between offense and defense"),
		TWIG (new Color(150, 75, 0), "The Twig faction focuses on offense with boosted damage"),
		STONE (Color.magenta, "The Pebble faction focuses on defense with shield generation"),
		LEAF (Color.green, "The Leaf faction focuses on deck and discard pile manipulation with a balance between offense and defense");
		public final String desc;
		public final Color bkg;
		Element(Color backgroundColor, String typeDescription){
			this.bkg = backgroundColor;
			this.desc = typeDescription;
		}
	}
}