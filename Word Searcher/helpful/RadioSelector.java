package helpful;
import helpful.Button;
import helpful.Rectangle;
import helpful.Drawer;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
public class RadioSelector{
	private Button[] buttons;
	private TextFitter[] textFitters;
	private int x, y, selection, width, length;
	private Rectangle collide;
	private Color bkgclr;
	public RadioSelector(int x, int y, int width, int length, int buttonsHorizontal, int buttonsVertical, String[] names, Color backgroundColor){
		buttons = new Button[Math.min(buttonsHorizontal * buttonsVertical, names.length)];
		textFitters = new TextFitter[Math.min(buttonsHorizontal * buttonsVertical, names.length)];
		int buttonWidth = width / buttonsHorizontal;
		int buttonHeight = length / buttonsVertical;
		for (int i=0;i<buttons.length;i++){
			buttons[i] = new Button(x + (i%buttonsHorizontal) * buttonWidth, y + (i/buttonsHorizontal)*buttonHeight, buttonWidth, buttonHeight, backgroundColor, names[i]);
			textFitters[i] = new TextFitter(buttonWidth - Math.min(buttonWidth, buttonHeight)-6, buttonHeight, names[i]);
		}
		this.selection = 0;
		this.x = x;
		this.y = y;
		this.width = Math.max(width, 10); //makes the minimum size 10 x 10
		this.length = Math.max(length, 10);
		this.collide = new Rectangle(x, y, width, length);
		this.bkgclr = backgroundColor;
	}
	public RadioSelector(int x, int y, int width, int length, int buttonsHorizontal, int buttonsVertical, Image[] images, Color backgroundColor){
		buttons = new Button[Math.min(buttonsHorizontal * buttonsVertical, images.length)];
		textFitters = null;
		int buttonWidth = width / buttonsHorizontal;
		int buttonHeight = length / buttonsVertical;
		for (int i=0;i<buttons.length;i++){
			buttons[i] = new Button(x + (i%buttonsHorizontal) * buttonWidth, y + (i/buttonsHorizontal)*buttonHeight, buttonWidth, buttonHeight, backgroundColor, images[i]);
		}
		this.selection = 0;
		this.x = x;
		this.y = y;
		this.width = Math.max(width, 10); //makes the minimum size 10 x 10
		this.length = Math.max(length, 10);
		this.collide = new Rectangle(x, y, width, length);
		this.bkgclr = backgroundColor;
	}
	public void draw(Graphics g){
		g.setColor(bkgclr);
		g.fillRect(x, y, width, length);
		for (int i =0;i<buttons.length;i++){
			if (buttons[i].getFontSize() == -1 || buttons[i].getText().equals("")) buttons[i].draw(g); //gets the font size of the button
			
			Rectangle temp = buttons[i].toRectangle();
			if (!buttons[i].getText().equals("") && textFitters != null){ //only draw these things if there is text on the button and the text fitters exist
				g.setColor(bkgclr);
				temp.fill(g);
				g.setColor(Button.getOppositeColor(bkgclr));
				g.setFont(new Font("Arial", Font.PLAIN, textFitters[i].getFontSize(g)));
				//System.out.println(textFitters[i].getFontSize(g) + ", " + textFitters[i].getText());
				Drawer.drawStackString(g, buttons[i].getText(), temp.getX() + 4 + Math.min(temp.getHeight(), temp.getWidth())-4, temp.getY() + 2 + temp.getHeight()/2 - g.getFontMetrics().getHeight()/2);
			}
			//draw the bubble selector on the left of the button
			g.setColor(Color.black);
			g.fillOval(temp.getX() + 2, temp.getY() + 2, Math.min(temp.getHeight(), temp.getWidth())-4, Math.min(temp.getHeight(), temp.getWidth())-4);
			g.setColor(Color.white);
			g.fillOval(temp.getX() + 4, temp.getY() + 4, Math.min(temp.getHeight(), temp.getWidth())-8, Math.min(temp.getHeight(), temp.getWidth())-8);
			if (selection == i){
				g.setColor(Color.black);
				g.fillOval(temp.getX() + 5, temp.getY()+5, Math.min(temp.getHeight(), temp.getWidth())-10, Math.min(temp.getHeight(), temp.getWidth())-10);
			}
		}
	}
	public void click(MouseEvent e){
		if (!collide.intersect(e.getX(), e.getY())) return; //no click
		for (int i=0;i<buttons.length;i++){
			if (buttons[i].isClicked(e)){
				selection = i;
				return;
			}
		}
	}
	public int getSelection(){
		return selection;
	}
}