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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
//GUI stuff
import javax.swing.JButton;
import javax.swing.JTextField;
//Image
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Screen extends JPanel implements MouseListener, MouseWheelListener, ActionListener{
	public static void main(String[]args){
        Screen screen = new Screen(args.length > 0 ? args[0] : "Test.png");
        JFrame frame = new JFrame("Screen");
        frame.add(screen);
		
		frame.addMouseListener(screen);
		frame.addMouseWheelListener(screen);
		
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
		frame.setVisible(true);
	}
	FileInputStream reader, counter;
	File toRead;
	JTextField size;
	ScrollBar bar;
	int pixelSize, numLines, screenLines, lineSize;
	long fileSize;
	public Screen(String fileName){
		setLayout(null);
		try{
			toRead = new File(fileName);
			reader = new FileInputStream(toRead);
		}
		catch(FileNotFoundException e){
			System.out.println("Yea...");
			e.printStackTrace();
		}
		try{
			fileSize = reader.getChannel().size();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		pixelSize = 5;
		
		size = new JTextField(pixelSize + "");
		size.setBounds(615, 115, 140, 30);
		add(size);
		size.addActionListener(this);
		
		numLines = (int)(pixelSize*fileSize/1800);
		screenLines = 1800/pixelSize;
		lineSize = 600/pixelSize;
		
		bar = new ScrollBar(numLines, screenLines);
		System.out.println("Size: " + numLines + ", shown: " + screenLines);
	}
	public Dimension getPreferredSize(){
        return new Dimension(800,600);
    }
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.setColor(Color.black);
		g.fillRect(0,0,800,600);
		g.setFont(new Font("Arial", Font.PLAIN, 15));
		
		try{
			reader = new FileInputStream(toRead);
		}
		catch(FileNotFoundException e){
			System.out.println("Yea...");
			e.printStackTrace();
		}
		
		byte[] line = new byte[lineSize*3];
		try{
			reader.skip(bar.getStep() * lineSize*3*3);
		}
		catch (IOException e){
			e.printStackTrace();
		}
		for (int i=0;i<screenLines;i++){
			try{
				reader.read(line);
				for (int n=0;n<line.length;n+=3){
					g.setColor(new Color(line[n] &0xFF, line[n+1] &0xFF, line[n+2] &0xFF));
					g.fillRect(pixelSize * n/3, pixelSize * i, pixelSize, pixelSize);
					
				}
			}
			catch (IOException e){
				e.printStackTrace();
			}
		}
		
		
		bar.draw(g);
		//System.out.println("Done: " + bar.getStep());
		
		g.setColor(Color.white);
		g.drawString("Current Size:", 615, 110);
	}
    public void mouseClicked(MouseEvent e) {
		bar.click(e);
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
		bar.click(e);
		repaint();
	}
	public void actionPerformed(ActionEvent e){
		if (e.getSource() == size){
			try{
				pixelSize = Integer.parseInt(size.getText());
				numLines = (int)(pixelSize*fileSize/1800);
				screenLines = 1800/pixelSize;
				lineSize = 600/pixelSize;
			}
			catch (Exception ex){
				size.setText("Error");
			}
		}
		repaint();
	}
}