
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
public class ScrollBar{
	private int x, y, width, height, step, size, shown;
	private boolean visible, arrows;
	private Color background, bar, arrow, arrowRect;
	public ScrollBar(int x, int y, int width, int height, int size, int shown){ // general constructer
		this.x = x;
		this.y = y;
		this.width = (width >= 10 ? width : 10);
		this.height = (height >= 70 ? height : 70);
		this.step = 0;
		this.shown = shown; //how many notches are shown on the screen
		this.size = (size > 0 ? size : 0);
		this.visible=true;
		this.arrows = true;
		this.background = null;
		this.bar = Color.lightGray;
		this.arrow = new Color(200, 200, 25);
		this.arrowRect = new Color(23, 45, 213);
	}
	public ScrollBar(int size){ //used in my defaults
		this(770, 0, 30, 600, size, 1);
	}
	public ScrollBar(int size, int shown){
		this(770, 0, 30, 600, size, shown);
	}
	public ScrollBar(int x, int y, int height, int size, int shown){ //useful for good arrows
		this(x, y, 30, height, size, shown);
	}
	public ScrollBar(int x, int y, int width, int height, int size, int shown, boolean showArrows){ //good for hiding arrows
		this(x, y, width, height, size, shown);
		this.arrows = showArrows;
		if (!arrows){
			//don't need a minimum height of size 70 now
			this.height = (height >= 10 ? height : 10);
		}
	}
	public ScrollBar(int x, int y, int width, int height, int size, Color backgroundColor){
		this(x, y, width, height, size);
		this.background = backgroundColor;
	}
	public int shown(){
		return shown;
	}
	public int getSize(){
		return size;
	}
	public boolean isVisible(){
		return visible;
	}
	public void setVisible(boolean isVisible){
		this.visible = isVisible;
	}
	public Rectangle toRect(){
		return new Rectangle(x, y, width, height);
	}
	public int getStep(){
		return step;
	}
	public void setBackgroundColor(Color backgroundColor){
		this.background = backgroundColor;
	}
	public void setColors(Color[] colors){
		//sets the colors of the components, requires at least 3 colors in the list
		if (colors.length < 3) return; //don't throw errors, just don't do it
		this.bar = colors[0];
		this.arrowRect = colors[1];
		this.arrow = colors[2];
	}
	public void draw(Graphics g){
		if (!visible) return; //don't draw if not visible
		if (background != null){
			g.setColor(background);
			g.fillRect(x, y, width, height);
		}
		if (arrows){
			//draw the rectangles on the top and bottom of the scroll bar
			g.setColor(this.arrowRect);
			g.fillRect(x, y, width, 30);
			g.fillRect(x, y+height-30, width, 30);
			
			g.setColor(this.arrow);
			g.fillPolygon(new int[] {x+width/2, x+5*width/6, x+width/6}, new int[] {y+5, y+25, y+25}, 3); //{y+(30)/6, y+5*(30)/6, y+5*(30)/6}
			g.fillPolygon(new int[] {x+width/2, x+5*width/6, x+width/6}, new int[] {y+height-5, y+height-25, y+height-25}, 3); //{y+height-30+5*(30)/6, y+height-30+(30)/6, y+height-30+(30)/6}
		}
		
		//Draw the actual scroll bar
		if (size == 0 || size/shown <= 1){
			//draw the full bar
			//System.out.println("bar");
			g.setColor(this.bar);
			g.fillRect(x, y+(arrows?30:0), width, height-(arrows?60:0));
		}
		else{
			//3 pixel margin around the scroll bar
			g.setColor(Color.black);
			if (background != null){
				g.setColor(background);
			}
			//System.out.println("bar" + x + ", " + y + ", " + step + ", " + height + ", " + size + ", " + width + ", " + shown);
			//g.fillRect(x-3, y+(arrows?30:0) + step*(height-(arrows?60:0))/size-3, width+6, (height-(arrows?60:0))/size+6);
			
			g.setColor(this.bar);
			g.fillRect(x, y+(arrows?30:0) + (int)(step*(height-(arrows?60:0))/(double)size), width, (int)((double)(height-(arrows?60:0))/size*shown));
		}
	}
	public void click(MouseEvent e){
		if (e instanceof MouseWheelEvent){
			changeHeight(((MouseWheelEvent)(e)).getWheelRotation());
		}
		else {
			this.click(e.getX(), e.getY());
		}
	}
	public void click(int x, int y){
		if (!toRect().intersect(x, y)) return; //not in the rectangle around the entire scroll bar
		if (new Rectangle(this.x, this.y, width, 30).intersect(x, y) && arrows){
			//top arrow click
			changeHeight(-1);
		}
		else if (new Rectangle(this.x, this.y+height-30, width, 30).intersect(x, y) && arrows){
			//bottom arrow click
			changeHeight(1);
		}
		else{
			//click on the scroll bar
			if (arrows){
				y-=30;
			}
			y /= (height-(arrows?60:0))/size; //y is now in range 0-size
			setStep(y-shown/2);
		}
	}
	public void setStep(int step){
		if (step >= size-shown+1) step = size-shown;
		if (step < 0) step = 0;
		this.step = step;
	}
	public boolean isScrollable(){
		return true;
	}
	private void changeHeight(int amount){ //negative moves upwards
		setStep(step+amount);
	}
}