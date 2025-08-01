package helpful.GUI;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseEvent;
//Image
import java.awt.Image;
import java.awt.Font;
//Rectangle
import helpful.Rectangle;
import helpful.TextFitter;

public class Button implements Component{
	protected int x, y, width, height;
	protected Color color, textColor;
	private String text, fontStyle;
	private boolean shown, enabled;
	private Image image;
	private TextFitter textFitter;
	public Button(int x, int y, int width, int height){
		//x-POS, y-POS, width-X DIM, height-Y DIM
		this.x = x;
		this.y = y;
		
		setSize(width, height);
		
		this.color = new Color(23, 45, 213);
		this.textColor = getOppositeColor(this.color);
		this.text = "";
		this.enabled=true;
		this.shown=true;
		this.fontStyle = "Arial";
		this.image = null;
		textFitter = new TextFitter(width, height, text);
	}
	public Button(int x, int y, int width, int height, Color color){
		this(x, y, width, height);
		this.color = color;
		this.textColor = getOppositeColor(this.color);
	}
	public Button(int x, int y, int width, int height, String text){
		this(x, y, width, height);
		this.text  = text;
		textFitter.setText(text);
	}
	public Button(int x, int y, int width, int height, Image image){
		this(x, y, width, height);
		if (image.getHeight(null) > image.getWidth(null)){
			this.image = image.getScaledInstance(-1, height-6, 0);
		}
		else{
			this.image = image.getScaledInstance(width-6, -1, 0);
		}
	}
	public Button(int x, int y, int width, int height, Color color, Image image){
		this(x, y, width, height, image);
		this.color = color;
		this.textColor = getOppositeColor(this.color);
	}
	public Button(int x, int y, int width, int height, boolean shown){
		this(x, y, width, height);
		this.shown=shown;
	}
	public Button(int x, int y, int width, int height, boolean shown, boolean enabled){
		this(x, y, width, height);
		this.shown=shown;
		this.enabled=enabled;
	}
	public Button(int x, int y, int width, int height, Color color, String text){
		this(x, y, width, height);
		this.color = color;
		this.text  = text;
		textFitter.setText(text);
		this.textColor = getOppositeColor(this.color);
	}
	public Button(int x, int y, int width, int height, Color color, String text, String fontStyle){
		this(x, y, width, height);
		this.color = color;
		this.text  = text;
		this.fontStyle = fontStyle;
		textFitter.setText(text);
		this.textColor = getOppositeColor(this.color);
	}
	public final void draw(Graphics g){
		if (!shown) return;
		//draw the button
		if (image == null){ //no image to draw
			g.setColor(color);
			g.fillRect(x, y, width, height);
			drawCode(g);
		}
		else{ //image exists
			g.setColor(color);
			g.fillRect(x, y, width, height);
			//draws the image centered on the button
			g.drawImage(image, x+width/2-image.getWidth(null)/2, y+height/2-image.getHeight(null)/2, color, null);
			
		}
	}
	public void drawCode(Graphics g){
		g.setFont(new Font(fontStyle, Font.PLAIN, textFitter.getFontSize(g))); //makes sure the font is the currect size
		if (!text.equals("") && textFitter.getFontGood()){
			g.setColor(textColor);
			g.drawString(text, x+width/2-g.getFontMetrics().stringWidth(text)/2, y+g.getFontMetrics().getHeight()/3+height/2); //for some reason draws from bottom left corner
			//System.out.println(g.getFontMetrics());
		}
	}
	public static Color getOppositeColor(Color color){
		return new Color((color.getRed()*-1+256)%256, (color.getGreen()*-1+256)%256, (color.getBlue()*-1+256)%256);
	}
	public Rectangle toRectangle(){
		return new Rectangle(this.x, this.y, this.width, this.height);
	}
	public Rectangle toRect(){
		return this.toRectangle();
	}
	public void setSize(int width, int height){
		//for changing the size of the button
		this.width = Math.max(width, 35);
		this.height = Math.max(height, 35);
	}
	public void setPos(int x, int y){
		//for changing the position of the button
		this.x=x;
		this.y=y;
	}
	public void setDimensions(int x, int y, int width, int height){
		setSize(width, height);
		setPos(x, y);
	}
	public void setText(String text){
		this.text = text;
		textFitter.setText(text);
	}
	public void setTextColor(){
		this.textColor = getOppositeColor(this.color);
	}
	public void setTextColor(Color textColor){
		this.textColor = textColor;
	}
	public void setColor(Color color){
		this.color = color;
	}
	public Color getColor(){
		return this.color;
	}
	public void setFontStyle(String newFontStyle){
		this.fontStyle = newFontStyle;
	}
	public void hide(){//hides the button
		this.shown=false;
	}
	public void show(){//shows the button
		this.shown=true;
	}
	public void setVisible(boolean value){//sets the buttons visibility
		this.shown = value;
		this.enabled=value;
	}
	public void enable(){
		this.enabled=true;
	}
	public void disable(){
		this.enabled=false;
	}
	public int getFontSize(){
		return textFitter.getFontSize();
	}
	public String getText(){
		return this.text;
	}
	public boolean click(int x, int y){
		//weird bug happens on each click: the y coord reported is 30 pix below where the cursor is located
		if (enabled && shown){
			return this.x < x && this.y < y-30 && this.x+this.width > x && this.y+this.height > y-30;
		}
		return false;
	}
	public void click(MouseEvent e){} //satifies Component interface - button clicks only return booleans, not voids
	public boolean isClicked(MouseEvent e){
        return (click(e.getX(), e.getY()));
    }
	public String toString(){
		//all the debug info
		return "Button at " + x + "," + y + " size of " + width + "," + height + " Color: " + color + " Text Color: " + textColor + " text: " + text + " Image: " + image + " Font Style: " + fontStyle + " Shown: " + shown + " Font Good: " + textFitter.getFontGood() + " Enabled: " + enabled + " Size: " + textFitter.getFontSize();
	}
	public String toBoundsString(){
		return "Button at " + x + "," + y + " size of " + width + "," + height;
	}
	public boolean isVisible(){
		return shown && enabled;
	}
	public void setColors(Color... colors){
		//sets the colors of the components, requires at least 3 colors in the list
		if (colors.length < 1) return;
		this.color = colors[0];
		this.textColor = getOppositeColor(color);
		if (colors.length >= 2){
			this.textColor = colors[1];
		}
	}
}