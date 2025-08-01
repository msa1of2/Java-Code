package helpful;
import java.awt.Font;
import java.awt.Graphics;
public class TextFitter{
	private int width, height, fontSize;
	private String text;
	private boolean textGood;
	public TextFitter(int width, int height, String text){
		this.width = width;
		this.height = height;
		this.text = text;
		this.textGood = false;
		this.fontSize = 100;
	}
	public int getFontSize(){
		if (textGood) return fontSize;
		return -1;
	}
	public int getFontSize(Graphics g){
		if (textGood){
			//The font is already at a good size
			return fontSize;
		}
		while (fontSize > 0){ //max of 100 iterations, will always escape
			g.setFont(new Font("Arial", Font.PLAIN, fontSize));
			
			int width = g.getFontMetrics().stringWidth(text);
			int height = g.getFontMetrics().getHeight();
			
			//15 pixel padding each axis
			if (width < this.width-5 && height < this.height-5){
				textGood=true;
				return fontSize;
			}
			this.fontSize--;
		}
		return fontSize;
	}
	public void setText(String text){
		this.text = text;
		this.textGood = false;
		this.fontSize = 100;
	}
	public boolean getFontGood(){
		return this.textGood;
	}
	public String getText(){
		return this.text;
	}
	public static int getFontSize(Graphics g, String text, int width, int height){
		int fontSize = 100;
		while (fontSize > 0){ //max of 100 iterations, will always escape
			g.setFont(new Font("Arial", Font.PLAIN, fontSize));
			
			int textWidth = g.getFontMetrics().stringWidth(text);
			int textHeight = g.getFontMetrics().getHeight();
			
			//15 pixel padding each axis
			if (textWidth < width-5 && textHeight < height-5){
				return fontSize;
			}
			fontSize--;
		}
		return fontSize;
	}
}