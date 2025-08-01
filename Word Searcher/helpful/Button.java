package helpful;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseEvent;
//Image
import java.awt.Image;
import java.awt.Font;
//Rectangle
import helpful.Rectangle;
import helpful.TextFitter;

public class Button{
	private int x, y, width, length;
	private Color color;
	private String text, fontStyle;
	private boolean shown, enabled;
	private Image image;
	private TextFitter textFitter;
	public Button(int x, int y, int width, int length){
		//x-POS, y-POS, width-X DIM, length-Y DIM
		this.x = x;
		this.y = y;
		
		updateSize(width, length);
		
		this.color = new Color(23, 45, 213);
		this.text = "";
		this.enabled=true;
		this.shown=true;
		this.fontStyle = "Arial";
		this.image = null;
		textFitter = new TextFitter(width, length, text);
	}
	public Button(int x, int y, int width, int length, Color color){
		this(x, y, width, length);
		this.color = color;
	}
	public Button(int x, int y, int width, int length, String text){
		this(x, y, width, length);
		this.text  = text;
		textFitter.setText(text);
	}
	public Button(int x, int y, int width, int length, Image image){
		this(x, y, width, length);
		if (image.getHeight(null) > image.getWidth(null)){
			this.image = image.getScaledInstance(-1, length, 0);
		}
		else{
			this.image = image.getScaledInstance(width, -1, 0);
		}
	}
	public Button(int x, int y, int width, int length, Color color, Image image){
		this(x, y, width, length, image);
		this.color = color;
	}
	public Button(int x, int y, int width, int length, boolean shown){
		this(x, y, width, length);
		this.shown=shown;
	}
	public Button(int x, int y, int width, int length, boolean shown, boolean enabled){
		this(x, y, width, length);
		this.shown=shown;
		this.enabled=enabled;
	}
	public Button(int x, int y, int width, int length, Color color, String text){
		this(x, y, width, length);
		this.color = color;
		this.text  = text;
		textFitter.setText(text);
	}
	public Button(int x, int y, int width, int length, Color color, String text, String fontStyle){
		this(x, y, width, length);
		this.color = color;
		this.text  = text;
		this.fontStyle = fontStyle;
		textFitter.setText(text);
	}
	public void draw(Graphics g){
		//draw the button
		g.setFont(new Font(fontStyle, Font.PLAIN, textFitter.getFontSize(g))); //makes sure the font is the currect size
		
		if (shown && image == null){
			g.setColor(color);
			g.fillRect(x, y, width, length);
			if (!text.equals("") && textFitter.getFontGood()){
				Color temp = g.getColor();
				g.setColor(getOppositeColor(temp));
				g.drawString(text, x+width/2-g.getFontMetrics().stringWidth(text)/2, y+g.getFontMetrics().getHeight()/3+length/2); //for some reason draws from bottom left corner
				//System.out.println(g.getFontMetrics());
			}
		}
		if (shown && image != null){
			g.setColor(color);
			g.fillRect(x, y, width, length);
			//draws the image centered on the button
			g.drawImage(image, x+width/2-image.getWidth(null)/2, y+length/2-image.getHeight(null)/2, color, null);
		}
	}
	public static Color getOppositeColor(Color color){
		return new Color((color.getRed()*-1+256)%256, (color.getGreen()*-1+256)%256, (color.getBlue()*-1+256)%256);
	}
	public Rectangle toRectangle(){
		return new Rectangle(this.x, this.y, this.width, this.length);
	}
	public Rectangle toRect(){
		return this.toRectangle();
	}
	public void updateSize(int width, int length){
		//for changing the size of the button
		this.width = Math.max(width, 35);
		this.length = Math.max(length, 35);
	}
	public void updatePos(int x, int y){
		//for changing the position of the button
		this.x=x;
		this.y=y;
	}
	public void updateDimensions(int x, int y, int width, int length){
		updateSize(width, length);
		updatePos(x, y);
	}
	public void updateText(String text){
		this.text = text;
		textFitter.setText(text);
	}
	public void updateColor(Color color){
		this.color = color;
	}
	public Color getColor(){
		return this.color;
	}
	public void updateFontStyle(String newFontStyle){
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
			return this.x < x && this.y < y && this.x+this.width > x && this.y+this.length > y;
		}
		return false;
	}
	public boolean isClicked(MouseEvent e){
        return (click(e.getX(), e.getY()));
    }
	public String toString(){
		//all the debug info
		return "Button at " + x + "," + y + " size of " + width + "," + length + " Color: " + color + " text: " + text + " Font Style: " + fontStyle + " Shown: " + shown + " Font Good: " + textFitter.getFontGood() + " Enabled: " + enabled + " Size: " + textFitter.getFontSize();
	}
	public String toBoundsString(){
		return "Button at " + x + "," + y + " size of " + width + "," + length;
	}
	public boolean isVisible(){
		return shown && enabled;
	}
}