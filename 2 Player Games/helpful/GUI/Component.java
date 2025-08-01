package helpful.GUI;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseEvent;
import helpful.Rectangle;
public interface Component{
	public Rectangle toRect(); //returns the rectangle encompessing the component
	public void draw(Graphics g); //Draws the component
	public void setVisible(boolean isVisible); //Hides the component
	public void setPos(int x, int y);
	default void setColors(Color... colors){
		//doesn't always do something
	}
	default void click(MouseEvent e){ //does click logic if it exists
		//do nothing otherwise
	}
	default boolean isScrollable(){
		return false;
	}
	default void debugDraw(Graphics g){ //Draws the bounding rectangles over every element, will always draw the rectangles
		draw(g);
		g.setColor(Color.white);
		toRect().draw(g);
	}
}