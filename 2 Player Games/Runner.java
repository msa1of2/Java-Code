import javax.swing.JFrame;
public class Runner{ 
	public static void main(String[] args){
		JFrame frame = new JFrame("Games");
		Screen screen = new Screen();
		frame.add(screen);
		frame.addMouseListener(screen);
		frame.addKeyListener(screen);
		frame.addWindowListener(screen);
		frame.addMouseWheelListener(screen);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		screen.animate();
	}
}