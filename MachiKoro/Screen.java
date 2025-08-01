import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JFrame;

//mouse events
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;

//key presses
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

//JTextField enter evnet
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

//text input
import javax.swing.JTextField;


class Screen extends JPanel implements KeyListener, MouseListener, ActionListener, MouseWheelListener{
	
	public static void main(String[]args){
        Screen screen = new Screen();
        JFrame frame = new JFrame("Screen");
        frame.add(screen);
		
		frame.addMouseListener(screen);
		frame.addMouseWheelListener(screen);
		frame.addKeyListener(screen);
		frame.setFocusTraversalKeysEnabled(false);
		
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
		frame.setVisible(true);
	}
	
	MachiKoro mk;
	public Screen(){
		setLayout(null);
		mk = new MachiKoro(0);
	}
	public Dimension getPreferredSize(){ //panel size
        return new Dimension(800, 600); //Game.XSIZE+200, Game.YSIZE);
    }
    public void paintComponent(Graphics g){ //draw screen
        super.paintComponent(g); //Don't touch
        //g.setFont(new Font("Arial", Font.PLAIN, 15));

        /*g.setColor(Color.darkGray);
        g.fillRect(0, 0, (int)g.getClipBounds().getWidth(), (int)g.getClipBounds().getHeight());*/
		
		mk.drawGame(g);
	}
	
	//key events
	public void keyPressed(KeyEvent e){}
	public synchronized void keyReleased(KeyEvent e){}
	public synchronized void keyTyped(KeyEvent e){}
	
	//mouse events
	public synchronized void mouseClicked(MouseEvent e){
		mk.onClick(e);
		repaint();
	}
	public synchronized void mouseEntered(MouseEvent e){}
	public synchronized void mouseExited(MouseEvent e){}
	public synchronized void mousePressed(MouseEvent e){}
	public synchronized void mouseReleased(MouseEvent e){}
	
	//mouse wheel events
	public synchronized void mouseWheelMoved(MouseWheelEvent e) {
		mk.mouseWheelMoved(e);
		repaint();
	}
	
	//action events (enter on JTextField)
	public synchronized void actionPerformed(ActionEvent e){}
	
}