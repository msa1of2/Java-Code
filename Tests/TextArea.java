//Graphics + stuff imports
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Color;
//custom importds
import helpful.Rectangle;
import helpful.GUI.Component;
import helpful.datastructures.MyArrayList;
import helpful.datastructures.DLList;
import helpful.datastructures.IndexMDLL;
import helpful.datastructures.ShiftRegister;
//Clip board imports
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import java.io.IOException;

public class TextArea implements MouseListener, MouseMotionListener, KeyListener, Component{
	protected IndexMDLL<Character> text;
	protected ShiftRegister<String> lastCommands; //command abbr: {a-(added text)-(index)}, {r-(removed Text)-(index)}
	protected Rectangle bounds;
	protected Color bkgcolor, textColor, selectedTextColor, selectionColor, caretColor;
	protected Font font;
	protected boolean isVisible, wordWrap, expandable, textChanged, isSelecting; //if Visible, if the words will wrap to new line upon running out of bounds, if more lines can be added beyond the height of this text area,
	protected int selectionStart, selectionSize, column, pad;
	protected MyArrayList<DLList<Character>> splitText;
	protected int fontMetrics[]; //height is 0, then ascii table / char value to key code
	protected javax.swing.JPanel repaint;
	
	/* TO DO:
	 * vertical expandability - scroll bar / move with cursor
	 * mouse click recalibration with new tab spaces 
	 */
	
	public TextArea(int x, int y, int width, int height, int lines, Font font){
		this(x, y, width, height, lines, font, true, true, true, getDefaultColors());
	}
	public TextArea(int x, int y, int width, int height, int lines, Font font, boolean isVisible, boolean wordWrap, boolean expandable, Color... colors){
		//x, y, width are required to create this - represent the (x,y) position of the top left corner, width is the x-dimension of the rectangle
		//height can be 0 or -1 to indicate it should be dependent on the fontMetrics and line count
		//lines is the number of lines this should start displaying with
		//font is the font to use when displaying the text
		//isVisible determines if the TextArea should be visible on creation or not
		//wordWrap determines if lines going outside the bounds of this box should be drawn or not
		this.bounds = new Rectangle(x,y,width,height);
		this.isVisible = isVisible;
		this.wordWrap = wordWrap;
		this.expandable = expandable;
		this.text = new IndexMDLL<Character>(50);
		this.lastCommands = new ShiftRegister<String>(5); //how long the command length should be
		this.font = font;
		setColors(colors);
		selectionStart = 0;
		selectionSize = 0;
		column = 0;
		pad = 5;
		textChanged = true;
	}
	//Constructor that generates the area with only the fontMetrics and line count
	public TextArea(int x, int y, int width, int lines, Font font){
		this(x, y, width, getFontMetrics(font).getHeight()*lines, lines, font, true, true, true, getDefaultColors());
	}
	public TextArea(int x, int y, int width, int lines, Font font, boolean isVisible, boolean wordWrap, boolean expandable, Color... colors){
		this(x, y, width, getFontMetrics(font).getHeight()*lines, lines, font, isVisible, wordWrap, expandable, colors);
	}
	
	//Component Methods
	//returns the rectangle encompessing the component
	public Rectangle toRect(){
		return bounds;
	}
	//Draws the component
	public void draw(Graphics g){ 
		if (!isVisible) return;
		g.setClip(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
		g.setColor(bkgcolor);
		bounds.fill(g);
		g.setFont(font);
		
		setSplitText();
		
		//Drawing the text bit by bit - draws text / selection information / caret
		int x=0, y=0, index=0, sum = 0;
		int[] start = indexToCoords(selectionStart), end = indexToCoords(selectionStart + selectionSize);
		g.setColor(textColor);
		if (!expandable && getWidth(getText(0, text.getIndex())) >= bounds.getWidth() - pad*2){
			sum = -(getWidth(getText(0, text.getIndex())) - (bounds.getWidth() - pad));
		}
		//if we aren't expandable, make sum negative to push it back so that the last few characters are the only ones that exist
		for (DLList<Character> line : splitText){
			for (Character letter : line){
				
				if (inSelection(index)){
					//draw background of the selected text
					g.setColor(selectionColor);
					g.fillRect(bounds.getX()+pad + sum, bounds.getY()+pad + (int)(getHeight() * 0.4) + y*getHeight(), getWidth(letter.charValue(), sum), getHeight()*4/5);
					
					//draw text on background
					g.setColor(selectedTextColor);
					if (letter.charValue() == '\t'){}
						//g.drawString(tabSpace(x), bounds.getX()+pad+sum, bounds.getY()+pad + (y+1)*getHeight());
					else
						g.drawString(""+letter, bounds.getX()+pad + sum, bounds.getY()+pad + (y+1)*getHeight());
					
					//reset the color back to normal text coloring
					g.setColor(textColor);
				}
				else{
					//normal text drawing
					if (letter.charValue() == '\t')
						g.drawString(tabSpace(x), bounds.getX()+pad+sum, bounds.getY()+pad + (y+1)*getHeight());
					else
						g.drawString(""+letter, bounds.getX()+pad + sum, bounds.getY()+pad + (y+1)*getHeight());
					
					
				}
				
				//draw the caret
				if (index == text.getIndex()){
					g.setColor(caretColor);
					g.fillRect(bounds.getX()+pad + sum, bounds.getY()+pad + (int)(getHeight() * 0.4) + y*getHeight(), g.getFont().getSize() / 10, getHeight()*4/5);
				
					//reset the text color for normal operations
					g.setColor(textColor);
				}
				
				sum += (getWidth(letter.charValue()) * (letter.charValue() == '\t' ? tabLength(x) : 1));
				x += (letter.charValue() == '\t' ? tabLength(x) : 1);
				index++;
			}
			if (line.size() == 0){
				//just an empty space, still need to display some items tho
				if (inSelection(index)){
					//draw background of the selected text
					g.setColor(selectionColor);
					g.fillRect(bounds.getX()+pad + sum, bounds.getY()+pad + (int)(getHeight() * 0.4) + y*getHeight(), 1, getHeight()*4/5);
					
					//reset the color back to normal text coloring
					g.setColor(textColor);
				}
				if (index == text.getIndex()){
					g.setColor(caretColor);
					g.fillRect(bounds.getX()+pad + sum, bounds.getY()+pad + (int)(getHeight() * 0.4) + y*getHeight(), g.getFont().getSize() / 10, getHeight()*4/5);
				
					//reset the text color for normal operations
					g.setColor(textColor);
				}
			}
			
			if (text.getIndex() == index){ //drawing the caret at the end of a line
				g.setColor(caretColor);
				g.fillRect(bounds.getX()+pad + sum, bounds.getY()+pad + (int)(getHeight() * 0.4) + y*getHeight(), g.getFont().getSize() / 10, getHeight()*4/5);
				
				//reset the text color for normal operations
				g.setColor(textColor);
			}
			
			y++;
			index++;
			x=0;
			sum = 0;
		}
		g.setClip(null);
		
		
		g.setColor(Color.red);
		int[] tempCoords = indexToCoords(text.getIndex());
		g.drawString(text.cursorFullString() + " -- " + text.getIndex() + ", (" + tempCoords[0] + ", " + tempCoords[1] + ")", 15, 15);
		for (int i=0;i<lastCommands.size();i++){
			g.drawString(lastCommands.get(i), 15, 50 + i*getHeight());
		}
		
		g.fillRect(bounds.getX() + pad, bounds.getY() + pad, 226, getHeight()/2);
	}
	//Hides the component
	public void setVisible(boolean isVisible){
		this.isVisible = isVisible;
	}
	public void setPos(int x, int y){ //changes the position of the textArea
		bounds.setX(x);
		bounds.setY(y);
	}
	public void repaintSubscription(javax.swing.JPanel panel){
		repaint = panel; //for our ability to redraw our section of the panel when wanted
	}
	
	//Caret methods
	//Find the x,y of text for the cursor, returns int[] with the elements : {x, y}
	public int[] findIndexPos(int index){
		setSplitText();
		return findIndexPos(splitText, index);
	}
	public int[] findIndexPos(MyArrayList<DLList<Character>> displayText, int index){
		int[] send = new int[2];
		if (index <= 0) return send;
		if (index >= text.size()) return new int[] {displayText.get(displayText.size()-1).size(), displayText.size()-1};
		int currentSize =0;
		for (int i=0;i<displayText.size();i++){
			currentSize+=displayText.get(i).size()+1; //we know the size of every item to the next DLList
			if (currentSize >= index){
				//i is now the y position of the caret
				send[1] = i;
				
				send[0] = displayText.get(i).size() - (currentSize - index)+1;
				if (text.get(index-1) != null ? text.get(index-1) == '\n' : false){
					send[0] = 0;
					send[1]++;
				}
				
				break; //we found the y, no more traversing required
			}
		}
		return send; //if not found, will return a {0, 0} or start of the text
	}
	//Determines if the current font is monospaced for the graphics object g
	/*public boolean isMonospaced(){
		return getWidth('i') != 0 && getWidth('W') == getWidth('i');
	}*/
	//moves the caret in the direction specified
	public void moveCaret(int xDir, int yDir){
		if (xDir < 0){
			for (;xDir<0;xDir++){
				text.left(); //update the cursor on the text
			}
			setColumn();
		}
		if (xDir > 0){
			for (;xDir>0;xDir--){
				text.right();
			}
			setColumn();
		}
		if (yDir != 0){
			int[] tempCoords = indexToCoords(text.getIndex());
			tempCoords[1] += yDir;
			if (tempCoords[1] >= splitText.size()){
				tempCoords[1] = splitText.size()-1;
				tempCoords[0] = splitText.get(splitText.size()-1).size();
				column = tempCoords[0];
			}
			if (tempCoords[1] < 0){
				tempCoords[1] = 0;
				tempCoords[0] = 0;
				column = 0;
			}
			//System.out.println("before: " + tempCoords[0] + ", " + tempCoords[1]);
			text.goTo(coordsToIndex(tempCoords));
			/*if (indexToCoords(text.getIndex())[0] < tempCoords[0]){ //not enough characters in this line
				column = tempCoords[0]; //save the desired column
			}
			else column = indexToCoords(text.getIndex())[0];*/
			//if (){
				tempCoords[0] = java.lang.Math.min(column, splitText.get(tempCoords[1]).size());
				text.goTo(coordsToIndex(tempCoords));
				//System.out.println("after: " + tempCoords[0] + ", " + tempCoords[1]);
			//}
		}
	}
	public void moveSelection(int xDir, int yDir){
		if (selectionSize == 0) //the selection is starting, save where it begins from
			selectionStart = text.getIndex();
		int tempPos = text.getIndex(); //save where the cursor is before the movement
		moveCaret(xDir, yDir); //move the cursor whereever it needs to go
		selectionSize += text.getIndex() - tempPos; //update the size of the selection
	}
	public int coordsToIndex(int x, int y){
		if (y < 0 || x < 0) return 0;
		setSplitText(); //splitText = split(text, '\n');
		if (y >= splitText.size()) return text.size();
		int send = 0;
		
		for (int i=0;i<java.lang.Math.min(y, splitText.size());i++){
			send += splitText.get(i).size()+1 ;//+ (splitText.size() ==i+2 ? 1 : 0); //counting each line to the y-level
			//System.out.print(i + ", ");
		}
		//System.out.println("\n" + java.lang.Math.min(y, splitText.size()) + " =/= " + splitText.size() + " -- " + y + " -- " + send + ", " + splitText.get(java.lang.Math.min(y, splitText.size())).size());
		if (x >= splitText.get(java.lang.Math.min(y, splitText.size())).size()){
			return send + splitText.get(y).size(); //we are at the far end of the line of intrest
		}
		return send + x;
	}
	public int coordsToIndex(int[] coords){
		if (coords.length < 2) return 0;
		return coordsToIndex(coords[0], coords[1]);
	}
	public int[] indexToCoords(int index){
		int[] send = new int[] {0, 0};
		if (index < 0 || index > text.size()) return send; //default invalid responses with 0
		setSplitText();//useful for the y-index
		int total = 0;
		for (int i=0;i<splitText.size();i++){
			if (total + splitText.get(i).size() >= index){ //the next line would be too far 
				send[1] = i; //found y value
				break;
			}
			total += splitText.get(i).size()+1; //add to our total
		}
		int x = 0;
		boolean breakout = false;
		for (Character letter : splitText.get(send[1])){
			send[0] = x;
			if (total >= index){
				breakout = true;
				break;
			}
			x++;
			total++;
			
		}
		if (!breakout && splitText.get(send[1]).size() != 0) send[0]++;
		return send;
	}
	
	//Color methods
	//sets all the colors given by an array of colors
	public void setColors(Color... colors){
		if (colors.length < 5) throw new IllegalArgumentException("Array length must be 5, got " + colors.length + " instead");
		bkgcolor = colors[0];
		textColor = colors[1];
		selectedTextColor = colors[2];
		selectionColor = colors[3];
		caretColor = colors[4];
	}
	//returns the current colors in the same order setColors wants them in
	public Color[] getColors(){
		Color[] send = new Color[5];
		send[0] = bkgcolor;
		send[1] = textColor;
		send[2] = selectedTextColor;
		send[3] = selectionColor;
		send[4] = caretColor;
		return send;
	}
	protected static Color[] getDefaultColors(){
		//background is white, text is black, selected text is gray, selection color is blueish, caret is black
		return new Color[] {Color.white, Color.black, Color.lightGray, new Color(25, 100, 225), Color.black};
	}
	
	//keyboard Methods
	public void keyPressed(KeyEvent e){
		if (e.getKeyCode() == KeyEvent.VK_LEFT){
			if (hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK)){
				goToNextOr(-1, ' ', '\n');
				setColumn();
			}
			else if (hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)){
				if (selectionSize == 0)
					selectionStart = text.getIndex();
				int tempPos = text.getIndex();
				goToNextOr(-1, ' ', '\n');
				selectionSize += text.getIndex() - tempPos;
				setColumn();
			}
			else if (hasOnlyMaskDown(e, KeyEvent.SHIFT_DOWN_MASK))
				moveSelection(-1, 0);
			else { //not extending the selection
				if (selectionSize == 0) //normal movement
					moveCaret(-1, 0);
				else{ 
					text.goTo(selectionSize < 0 ? selectionStart+selectionSize : selectionStart); //go to the start of the selection
				}//voiding the selection
				selectionSize = 0;
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT){
			if (hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK)){
				goToNextOr(1, ' ', '\n');
				if (text.getIndex() != text.size()) text.left();
				setColumn();
			}
			else if (hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)){
				if (selectionSize == 0)
					selectionStart = text.getIndex();
				int tempPos = text.getIndex();
				goToNextOr(1, ' ', '\n');
				if (text.getIndex() != text.size()) text.left();
				selectionSize += text.getIndex() - tempPos;
				setColumn();
			}
			else if (hasOnlyMaskDown(e, KeyEvent.SHIFT_DOWN_MASK))
				moveSelection(1, 0);
			else{ //not extending the selection
				if (selectionSize == 0) //normal movement
					moveCaret(1, 0);
				else{
					text.goTo(selectionSize > 0 ? selectionStart+selectionSize : selectionStart); //go to the end of the selection
				}//voiding the selection
				selectionSize = 0;
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_UP){
			if (hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK)){
				goToNext(-1, '\n');
				setColumn();
			}
			else if (hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)){
				if (selectionSize == 0)
					selectionStart = text.getIndex();
				int tempPos = text.getIndex();
				goToNext(-1, '\n');
				selectionSize += text.getIndex() - tempPos;
				setColumn();
			}
			else if (hasOnlyMaskDown(e, KeyEvent.SHIFT_DOWN_MASK))
				moveSelection(0, -1);
			else{
				if (selectionSize == 0)
					moveCaret(0, -1);
				else
					text.goTo(selectionSize < 0 ? selectionStart+selectionSize : selectionStart); //go to beginning of selection
				selectionSize = 0; //void the selection - again if needed
			}
		}
		else if (e.getKeyCode() == KeyEvent.VK_DOWN){
			if (hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK)){
				goToNext(1, '\n');
				if (text.getIndex() != text.size()) text.left();
				setColumn();
			}
			else if (hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)){
				if (selectionSize == 0)
					selectionStart = text.getIndex();
				int tempPos = text.getIndex();
				goToNext(1, '\n');
				if (text.getIndex() != text.size()) text.left();
				selectionSize += text.getIndex() - tempPos;
				setColumn();
			}
			else if (hasOnlyMaskDown(e, KeyEvent.SHIFT_DOWN_MASK))
				moveSelection(0, 1);
			else{
				if (selectionSize == 0)
					moveCaret(0, 1);
				else
					text.goTo(selectionSize > 0 ? selectionStart+selectionSize : selectionStart); //go to end of selection
				selectionSize = 0; //void the selection - again if needed
			}
		}
		else if (((e.getKeyChar() != '\uFFFF' && e.getKeyCode() != KeyEvent.VK_DELETE && e.getKeyCode() != KeyEvent.VK_ESCAPE && e.getKeyCode() != KeyEvent.VK_BACK_SPACE) || e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) && (hasOnlyMaskDown(e, KeyEvent.SHIFT_DOWN_MASK) || hasOnlyMaskDown(e, 0))){ //normal key typing
			//get modifiers returns extra keys being pressed during normal key press, this is making sure the modifiers I don't want accounted for don't input text
			if (e.getKeyCode() == KeyEvent.VK_ENTER && !expandable){
				return; //can't create a new line when not expandable
			}
			if (selectionSize != 0){
				//we currently have a selection going
				boolean did = removeSelection();
			}
			lastCommands.add("a-" + e.getKeyChar() + "-" + text.getIndex());
			text.add(e.getKeyChar()); //add whatever letter we got
			setColumn();
		}
		else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE){ //removing characters
			if (selectionSize != 0){
				lastCommands.add("r-"+getSelectionText()+"-"+ (selectionSize < 0 ? selectionStart+selectionSize : selectionStart));
				removeSelection(); //voiding the entire selection
			}
			else{
				lastCommands.add("r-"+text.getCursor().charValue()+"-"+(text.getIndex()-1));
				text.remove();
			}
			setColumn();
		}
		else if (e.getKeyCode() == KeyEvent.VK_V && hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK)){ //paste command
			if (selectionSize != 0) removeSelection();
		
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); //used with ctrl + v

			if (clipboard.getContents(null) != null ? clipboard.getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor) : false) {
				try {
					char[] letters = ((String) clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor)).toCharArray();
					Character[] chars = new Character[letters.length];
					for (int i=0;i<letters.length;i++){
						chars[i] = letters[i];
					}
					lastCommands.add("a-"+new String(letters)+"-" + text.getIndex());
					text.add(chars);
				} catch (UnsupportedFlavorException | IOException ex) {
					ex.printStackTrace();
				}
			}
			column = indexToCoords(text.getIndex())[0]; //adding keys
		}
		else if (e.getKeyCode() == KeyEvent.VK_C && hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK)){ //copy command
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();//used with ctrl+c
			StringSelection selection = new StringSelection(getSelectionText());
			clipboard.setContents(selection, null);
		}
		else if (e.getKeyCode() == KeyEvent.VK_X && hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK)){ //cut command
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();//used with ctrl+c
			StringSelection selection = new StringSelection(getSelectionText());
			clipboard.setContents(selection, null);
			
			removeSelection(); //delete the selection since this is a cut command
		}
		else if (e.getKeyCode() == KeyEvent.VK_A && hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK)){ //select all command
			selectionStart = 0;
			selectionSize = text.size();
		}
		else if (e.getKeyCode() == KeyEvent.VK_Z && hasOnlyMaskDown(e, KeyEvent.CTRL_DOWN_MASK)){ //undo command
			undo();
		}
		textChanged = true;
	}
	public void keyReleased(KeyEvent e){
		
	}
	public void keyTyped(KeyEvent e){
		System.out.println("typed");
		
	}
	public boolean hasOnlyMaskDown(KeyEvent e, int keyMask){ //returns true when only this specific key mask is pushed down (key masks are the shift/ctrl/alt keys)
		return ((e.getModifiersEx() & (KeyEvent.ALT_DOWN_MASK | KeyEvent.ALT_GRAPH_DOWN_MASK | KeyEvent.BUTTON1_DOWN_MASK | KeyEvent.BUTTON2_DOWN_MASK | KeyEvent.BUTTON3_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK)) == keyMask);
	}
	public void undo(){
		if (lastCommands.size() == 0) return;
		String lastCommand = lastCommands.pop(); //get our last command while removing the last stored command
		boolean removedText = lastCommand.charAt(0) == 'r';
		String commandText = lastCommand.substring(2, lastCommand.lastIndexOf('-'));
		int index = Integer.parseInt(lastCommand.substring(lastCommand.lastIndexOf('-')+1));
		
		if (removedText){
			//undo the removal by adding all the words to the index
			char[] letters = commandText.toCharArray();
			for (int i=0;i<letters.length;i++){
				text.add(letters[i], i+index);
			}
			text.goTo(index + letters.length);
		}
		else{
			text.remove(index, index+commandText.length());
			text.goTo(index);
		}
	}
	
	//mouse methods
	public void mouseClicked(MouseEvent e){
		if (!(e.getX() >= bounds.getX() && e.getX() <= bounds.getX()+bounds.getWidth() && e.getY() >= bounds.getY() && e.getY() <= bounds.getY()+bounds.getHeight())) return;
		text.goTo(coordsToIndex(worldToCoords(e.getX(), e.getY())));
		repaint.repaint(0L, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){
        selectionStart = coordsToIndex(worldToCoords(e.getX(), e.getY()));
		selectionSize = 0;
		//isSelecting = true;
    }
    public void mouseReleased(MouseEvent e){
		
	}
	public void mouseDragged(MouseEvent e){
		int temp = coordsToIndex(worldToCoords(e.getX(), e.getY()));
		selectionSize = temp - selectionStart;
		isSelecting = selectionSize!=0;
		text.goTo(temp);
		repaint.repaint(0L, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}
	public void mouseMoved(MouseEvent e){}
	public int[] worldToCoords(int x, int y){
		//initalize our helper items
		if (fontMetrics == null) setFontMetrics();
		setSplitText();
		
		int[] send = new int[2];
		
		//first find our y-axis
		send[1] = Math.max(0, Math.min(splitText.size()-1, (y-bounds.getY()-pad)/getHeight())); //sets y-value to be between 0 and whatever length our text is
		//then find our x-axis using the y-axis found to clamp the x-axis to the proper numbers
		int cx = 0, sum = 0, tabX=0;
		for (Character letter : splitText.get(send[1])){
			if (sum >= x - bounds.getX()-pad) break;
			sum += getWidth(letter.charValue(), tabX);
			cx += 1;
			tabX += letter.charValue() == '\t' ? tabLength(tabX) : 1;
			System.out.println("WorldToCoords: " + sum + ", " + (x-bounds.getX()-pad) + ", " + getWidth(letter.charValue(), tabX) + ", " +tabX + ", " + (letter.charValue() == '\t' ? 't' : letter.charValue()));
		}
		send[0] = Math.max(0, Math.min(splitText.get(send[1]).size(), cx));
		System.out.println("worldToCoords debug: " + (x-bounds.getX()-pad) + ", " + (send[0]*getWidth(' ')) + ", " + sum + ", " + cx + ", " + tabX);
		return send;
	}
	
	//helpers
	public static FontMetrics getFontMetrics(Font font){
		return (new Canvas()).getFontMetrics(font);
	}
	public static <T> MyArrayList<DLList<T>> split(IndexMDLL<T> list, T splitItem){
		//splits list into a list of lists excluding the splitItem, each primary list contains the elements until the next instance of splitItem, the sublists are the collections of items between splitItems
		int count = 0;
		MyArrayList<DLList<T>> arrayList = new MyArrayList<>(10);
		arrayList.add(new DLList<T>());
		for (T item : list){
			if (item.equals(splitItem)){
				count++;
				arrayList.add(new DLList<T>());
			}
			else arrayList.get(count).add(item);
		}
		return arrayList;
	}
	public boolean removeSelection(){
		boolean did;
		if (selectionSize > 0) did = text.remove(selectionStart, selectionStart + selectionSize); //remove all the old letters
		else did = text.remove(selectionStart+selectionSize, selectionStart);
		//System.out.println(did);
		selectionSize = 0;
		return did;
	}
	public void setColumn(){
		column = indexToCoords(text.getIndex())[0];
	}
	public String getSelectionText(){
		char[] letters = new char[java.lang.Math.abs(selectionSize)];
		int counter=0;
		boolean selectionStarted = false;
		for (Character letter : text){
			if (counter == (selectionSize > 0 ? selectionStart : selectionSize + selectionStart) || selectionStarted){ //we found the beginning of the text
				letters[counter - (selectionSize > 0 ? selectionStart : selectionSize + selectionStart)] = letter.charValue();
				selectionStarted = true;
			}
			counter++;
			if (counter >= (selectionSize < 0 ? selectionStart : selectionSize + selectionStart)){ //we found the end of the text
				break;
			}
		}
		return new String(letters);
	}
	public boolean goToNext(int dir, char item){
		//dir should be 1 or -1, other values will have no affect
		if (dir > 0) {text.right();text.right();}
		else text.left();
		
		while ((dir > 0 ? text.canGoRight() : text.canGoLeft())){
			//System.out.println(text.cursorFullString());
			if (text.getCursor() == null) return false;
			if (text.getCursor().charValue() == item) return true;
			if (dir > 0) text.right();
			else text.left();
		}
		return false;
	}
	public boolean goToNextOr(int dir, char... items){
		//will move the cursor to the first item from the items list it finds
		//dir should be 1 or -1, other values will have no affect
		if (dir > 0) {text.right();text.right();}
		else text.left();
		
		while ((dir > 0 ? text.canGoRight() : text.canGoLeft())){
			//System.out.println(text.cursorFullString());
			if (text.getCursor() == null) return false;
			for (char item : items){
				if (text.getCursor().charValue() == item) return true;
			}
			if (dir > 0) text.right();
			else text.left();
		}
		if (text.getIndex() == text.size()) text.right();
		return false;
	}
	public boolean inSelection(int index){
		if (selectionSize == 0) return false;
		return (index >= (selectionSize < 0 ? selectionStart+selectionSize : selectionStart) && index < (selectionSize < 0 ? selectionStart : selectionStart+selectionSize));
	}
	public String getText(int start){
		return getText(start, text.size());
	}
	public String getText(int start, int end){
		if (start == end) return "";
		IndexMDLL<Character> sub = text.subArray(start, end);
		char[] letters = new char[sub.size()];
		
		for (int i=0;i<letters.length;i++){
			letters[i] = sub.getCursor();
			sub.right();
		}
		return new String(letters);
	}
	public int tabLength(int xValue){
		return 4 - (xValue%4);
	}
	public String tabSpace(int xValue){
		switch (xValue%4){
			case 0:
				return "    ";
			case 1:
				return "   ";
			case 2:
				return "  ";
			case 3:
				return " ";
		}
		return "";
	}
	
	//split text function
	public void setSplitText(){
		if (!textChanged) return; //don't waste resources when nothing changed yet
		
		if (!expandable){
			splitText = new MyArrayList<DLList<Character>>(10);
			splitText.set(0, new DLList<Character>(text));
			return;
		}
		
		splitText = split(text, '\n');
		
		if (!wordWrap || !expandable){ // we aren't wrapping the text so just skip the wraping to bounds part or we aren't expandable so we cant go with new lines
			textChanged = false;
			return;
		}
		
		int lineSum = 0, index = 0, innerIndex = 0, spaceIndex = 0;
		for (int i=0;i<splitText.size();i++){
			lineSum = 0;
			index = -1;
			innerIndex = 0;
			spaceIndex = 0;
			for (Character letter : splitText.get(i)){
				index++;
				if (lineSum < bounds.getWidth() - 15){
					lineSum += getWidth(letter.charValue());
					continue;
				}
				//we need to add a new line here or before this
				for (Character let : splitText.get(i)){
					if (innerIndex >= index) break;
					innerIndex++;
					if (let.charValue() == ' ') spaceIndex = innerIndex;
				}
				if (spaceIndex==0) spaceIndex = innerIndex;
				DLList<Character> newLine = splitText.get(i).split(spaceIndex-1);
				splitText.get(i).remove(splitText.get(i).size()-1);
				splitText.add(i+1, newLine);
				//System.out.println("Split at : " + spaceIndex +"\n" + splitText);
				break;
			}
		}
		
		
		textChanged = false;
	}
	
	//better font metric functions - don't need to lug around an entire class for it
	public void setFontMetrics(){
		FontMetrics fm = getFontMetrics(font);
		fontMetrics = new int[96];
		fontMetrics[0] = fm.getHeight();
		for (int i=0;i<95;i++){
			fontMetrics[i+1] = fm.charWidth((char)(i+32));
		}
	}
	public int getWidth(DLList<Character> list){
		int sum = 0;
		for (Character item : list){
			sum += getWidth(item.charValue());
		}
		return sum == 0 ? 5 : sum;
	}
	public int getWidth(String items){
		int sum = 0;
		for (char item : items.toCharArray()){
			sum += getWidth(item);
		}
		return sum == 0 ? 5 : sum;
	}
	public int getWidth(char item){
		if (fontMetrics == null) setFontMetrics();
		return (((int)item)-31 >= 1 && ((int)item)-31 < fontMetrics.length) ? fontMetrics[((int)item)-31] : fontMetrics[' '-31]; //defaults to 0 if it cant find the character mentioned
	}
	public int getWidth(char item, int xPos){
		if (fontMetrics == null) setFontMetrics();
		if (item == '\t') return (tabLength(xPos)-1) * getWidth(' ');
		return getWidth(item);
	}
	public int getHeight(){
		if (fontMetrics == null) setFontMetrics();
		return fontMetrics[0];
	}
	public boolean isMonospaced(){
		if (fontMetrics == null) setFontMetrics();
		int size = fontMetrics[1];
		for (int i=2;i<fontMetrics.length;i++){
			if (size != fontMetrics[i]) return false;
		}
		return true;
	}
	
	//text methods
	public void insert(char letter, int location){
		
	}
	public String toString(DLList<Character> list){
		char[] intermediate = new char[list.size()];
		int index = 0;
		for (Character item : list){
			intermediate[index++] = item.charValue();
		}
		return new String(intermediate);
	}
	
	//test use methods
	public String toString(){
		return text.toString();
	}
	public void add(String letters){
		for (Character letter : letters.toCharArray()){
			text.add(letter);
		}
		text.goTo(0);
	}
}