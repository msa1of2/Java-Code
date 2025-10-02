import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JFrame;

import helpful.Scalable;

public class Debug extends JPanel{
	public static void main(String[]args){
        Debug screen = new Debug();
        JFrame frame = new JFrame("Command of Nature");
        frame.add(screen);
		
		/*frame.addMouseListener(screen);
		frame.addMouseWheelListener(screen);
		frame.addKeyListener(screen);
		frame.setFocusTraversalKeysEnabled(false);*/
		
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
		frame.setVisible(true);
		//screen.animate();
	}
	public Dimension getPreferredSize(){ //panel size
        return new Dimension(800, 600); //Game.XSIZE+200, Game.YSIZE);
    }
	
	private Scalable s;
	private Card test;
	
	public Debug(){
		setLayout(null);
		
		//Graphics
		s = new Scalable(800, 600);
		s.preserveAspectRatio(true);
		s.setBackgroundColor(Color.darkGray);
		
		test = new Card("Test", 1, 2, 3, Card.ROWI , 5, Card.Element.DROPLET, null);
		test.deltaBoost(1);
		test.deltaShield(2);
	}
	
	public void paintComponent(Graphics g){ //draw screen
        super.paintComponent(g); //Don't touch
		s.init(g);
		
		/*
		//One player board
		test.draw(s, 400, 0, 75);
		test.draw(s, 300, 0, 75);
		test.draw(s, 500, 0, 75);
		
		test.draw(s, 350, 75, 75);
		test.draw(s, 450, 75, 75);
		
		test.draw(s, 400, 150, 75);
		
		test.draw(s, 400, 225, 75);
		
		test.draw(s, 350, 300, 75);
		test.draw(s, 450, 300, 75);
		
		test.draw(s, 400, 375, 75);
		test.draw(s, 300, 375, 75);
		test.draw(s, 500, 375, 75);*/
		
		//two player board
		test.draw(s, 250, 0, 75);
		test.draw(s, 300, 0, 75);
		test.draw(s, 350, 0, 75);
		test.draw(s, 400, 0, 75);
		test.draw(s, 450, 0, 75);
		test.draw(s, 500, 0, 75);
		
		test.draw(s, 300, 75, 75);
		test.draw(s, 350, 75, 75);
		test.draw(s, 400, 75, 75);
		test.draw(s, 450, 75, 75);
		
		test.draw(s, 350, 150, 75);
		test.draw(s, 400, 150, 75);
		
		test.draw(s, 350, 225, 75);
		test.draw(s, 400, 225, 75);
		
		test.draw(s, 300, 300, 75);
		test.draw(s, 350, 300, 75);
		test.draw(s, 400, 300, 75);
		test.draw(s, 450, 300, 75);
		
		test.draw(s, 250, 375, 75);
		test.draw(s, 300, 375, 75);
		test.draw(s, 350, 375, 75);
		test.draw(s, 400, 375, 75);
		test.draw(s, 450, 375, 75);
		test.draw(s, 500, 375, 75);
		
		//highlighted card example
		test.draw(s, 0, 32, 350);
		
		//hand showcase
		test.draw(s, 560, 450, 150);
		test.draw(s, 455, 450, 150);
		test.draw(s, 350, 450, 150);
		test.draw(s, 245, 450, 150);
		test.draw(s, 140, 450, 150);
		
		
		//test.draw(s, 0, 300, 150);
		//test.draw(s, 500, 200, 300);
	}
	
}