package helpful;
import java.awt.Font;
import java.awt.Graphics;
public class TextFitter{
	protected int width, height, fontSize;
	protected String text;
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
		return getFontSize(g, "Arial");
	}
	public int getFontSize(Graphics g, String style){
		if (textGood){
			//The font is already at a good size
			return fontSize;
		}
		fontSize = getFontSize(g, text, style, width, height);
		if (fontSize != 0) textGood = true;
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
		return getFontSize(g, text, "Arial", width, height);
	}
	public static int getFontSize(Graphics g, String text, String name, int width, int height){
		int fontSize = 100, maxSize = 0, index=0;
		String[] lines = text.split("\n");
		for (int i=0;i<lines.length;i++){
			if (g.getFontMetrics().stringWidth(lines[i]) > maxSize){
				maxSize = g.getFontMetrics().stringWidth(lines[i]);
				index = i;
			}
		}
		while (fontSize > 0){ //max of 100 iterations, will always escape
			g.setFont(new Font(name, Font.PLAIN, fontSize));
			
			int textWidth = g.getFontMetrics().stringWidth(lines[index]);
			int textHeight = g.getFontMetrics().getHeight() * lines.length;
			
			//15 pixel padding each axis
			if (textWidth < width-5 && textHeight < height-5){
				return fontSize;
			}
			fontSize--;
		}
		return fontSize;
	}
}