import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
//events
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
//Image
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
//import helpful.GUI.Text;
import helpful.GUI.*;
import helpful.Drawer;
import helpful.datastructures.ShiftRegister;
import helpful.Scalable;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;

import javax.swing.JButton;

//text transforms
import java.awt.geom.AffineTransform;

//slider
import javax.swing.JSlider;

//spinner thing
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class Screen extends JPanel implements MouseListener, MouseWheelListener, KeyListener{
	TextArea testing;
	HeightFluids hf;
	Scalable s;
	JButton test;
	public static void main(String[]args){
        Screen screen = new Screen();
        JFrame frame = new JFrame("Screen");
		
		MenuBar bar = new MenuBar();
		Menu main1, main2, main3;
		main1 = new Menu("One");
		main2 = new Menu("Two");
		main3 = new Menu("Three");
		
		//bar.add(main1);
		bar.setHelpMenu(main1);
		bar.add(main2);
		bar.add(main3);
		
		main1.add(new MenuItem("1"));
		main1.add(new MenuItem("2"));
		main1.add(new MenuItem("3"));
		
		main2.add(new MenuItem("a"));
		main2.add(new MenuItem("b"));
		main2.add(new MenuItem("c"));
		
		main3.add(new MenuItem("A"));
		main3.add(new MenuItem("B"));
		main3.add(new MenuItem("C"));
		
        frame.add(screen);
		frame.setMenuBar(bar);
		
		frame.addMouseListener(screen);
		frame.addMouseWheelListener(screen);
		frame.addKeyListener(screen);
		frame.setFocusTraversalKeysEnabled(false);
		
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
		frame.setVisible(true);
	}
	public Screen(){
		/*
		testing = new TextArea(100, 100, 500, 300, 10, new Font("Consolas", Font.PLAIN, 15), true, false, true, TextArea.getDefaultColors());
		setLayout(null);
		testing.add("a\tb\tc\td\te\tf\tg\th\ti\tj\tk\tl\tm\tn\to\tp");
		testing.repaintSubscription(this);
		
		requestFocusInWindow();
		//this.addKeyListener(testing);
		this.addMouseListener(testing);
		this.addMouseMotionListener(testing);
		*/
		hf = new HeightFluids(5, 5);
		hf.add(2, 2, 1000);
		
		//s = new Scalable(800, 600);
		//s.preserveAspectRatio(true);
		/*test = new JButton("TEst");
		test.setBounds(300, 100, 100, 100);
		add(test);*/
	}
	public Dimension getPreferredSize(){
        return new Dimension(800,600);
    }
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		/*s.setBackgroundColor(Color.darkGray);
		s.init(g);
		//g.setColor(Color.darkGray);
		//g.fillRect(0, 0, 800, 600);
		
		//Font test = new Font("Arial", Font.PLAIN, 12);
		
		//g.setColor(Color.white);
		//s.setColor(Color.white);
		//g.setFont(test);
		//s.setFont(test);
		
		//g.drawString("test1", 400, 300);
		//s.drawString("Test2", 400, 400);
		
		
		Font test = new Font("Arial", Font.PLAIN, 24);
		AffineTransform rotation = new AffineTransform();
		rotation.rotate(1.57);
		Font test2 = test.deriveFont(rotation);
		g.setFont(test);
		g.setColor(Color.white);
		g.drawString("Test", 300, 300);
		
		//g.setFont(test2);
		s.drawString("Test", 300, 300, 180);*/
		
		hf.draw(g, 0, 0, 800, 600);
		//testing.draw(g);
	}
    public void mouseClicked(MouseEvent e) {
		//testing.mouseClicked(e);
		hf.update();
		repaint();
    }
    public void mousePressed(MouseEvent e) {
		
		repaint();
    }
    public void mouseReleased(MouseEvent e) {
		
		repaint();
    }
    public void mouseEntered(MouseEvent e) {
    
		repaint();
    }
    public void mouseExited(MouseEvent e) {
        
		repaint();
    }
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		repaint();
	}
	public void keyPressed(KeyEvent e){
		testing.keyPressed(e);
		//System.out.println(e.getExtendedKeyCode() + ", " + e.getKeyCode() + ", " + e.getKeyChar() + ", " + e.getKeyLocation() + ", " + e.isActionKey() + ", " + e.getModifiers() + "<" + e.paramString() + ">");
        repaint();
    }
    public void keyReleased(KeyEvent e){
        
        repaint();
    }
    public void keyTyped(KeyEvent e) {
		repaint();
	}
}