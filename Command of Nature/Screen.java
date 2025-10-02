import helpful.GUI.*;
import helpful.connection.*;
import helpful.datastructures.DLList;
import helpful.datastructures.ShiftRegister;
import helpful.*;

//Graphics
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JFrame;

//mouse events
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseMotionListener;

//key presses
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

//window closing events - closes the socket properly
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

//JTextField enter evnet
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

//text input
import javax.swing.JTextField;

public class Screen extends JPanel implements KeyListener, MouseListener, WindowListener, ActionListener, MouseWheelListener, MouseMotionListener{
	public static void main(String[]args){
        Screen screen = new Screen();
        JFrame frame = new JFrame("Command of Nature");
        frame.add(screen);
		
		frame.addMouseListener(screen);
		frame.addMouseWheelListener(screen);
		frame.addKeyListener(screen);
		frame.setFocusTraversalKeysEnabled(false);
		
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
		frame.setVisible(true);
		screen.animate();
	}
	public Dimension getPreferredSize(){ //panel size
        return new Dimension(800, 600); //Game.XSIZE+200, Game.YSIZE);
    }
	
	//Graphics stuffs
	private Scalable s;
	private Font title, header, cardTitle, info, nameFont;
	
	
	//Menus
	private Button singlePlayer, multiPlayer, back, oneAI, twoAI, threeAI, host, join, start, oneVOne, twoVTwoAllies, twoVTwoEnemies;
	private Menu[] menus;
	private ShiftRegister<Integer> menuOrder;
	private JTextField name, ipInput;
	
	//Connection to server / client
	private ConnectionHandler ch;
	private Server serverHolder; //for use to stop the server when leaving
	private Thread serverRunner; //created in onClick / host clicked
	
	//Error stuffs
	private String errorMessage = "";
	
	
	/* TODO
	 * Buttons for each intermediate menu
	 * Transitions to each window
	 * CoN class to be handed the connection handler, and player count - screen only deals with the draw commands and issues the events
	 * CoN and its TODOs
	 */
	
	/* Polish
	 * Replace host intermediate choose teams with a left / right selector for each player - default all unchosen players to open side when 1 side fills up - have AI agents exist for the host to dictate which side they're on
	 */
	
	
	public Screen(){
		setLayout(null);
		
		//Graphics
		s = new Scalable(800, 600);
		s.preserveAspectRatio(true);
		s.setBackgroundColor(Color.black);
		
		//fonts
		title = new Font("Arial", Font.PLAIN, 90);
		header = title.deriveFont(45f);
		nameFont = title.deriveFont(30f);
		cardTitle = title.deriveFont(15f);
		info = title.deriveFont(10f);
		
		//Menus
		menus = new Menu[10];
		menuOrder = new ShiftRegister<Integer>(menus.length);
		menuOrder.add(0);
		/* Menu indexes and their uses
		 * 0 - Title
		 * 1 - Single Player Intermediate
		 * 2 - Multiplayer Intermediate
		 * 3 - Hosting Intermediate
		 * 4 - 2 Player Host Intermediate
		 * 5 - 3 Player Host Intermediate
		 * 6 - 4 Player Host Intermediate
		 * 7 - Joining Intermediate
		 * 8 - Settings Options
		 * 9 - Drawing the game
		 */
		back = new Button(15, 15, 50, 35, Color.red, "Back");
		
		//Title Screen
		singlePlayer = new Button(50, 350, 300, 100, Color.lightGray, "Singleplayer");
		multiPlayer = new Button(450, 350, 300, 100, Color.lightGray, "Multiplayer");
		menus[0] = new Menu(true, singlePlayer, multiPlayer){
			public void drawCode(Scalable s){
				s.setColor(Color.green);
				s.setFont(title);
				Drawer.drawCenteredStackString(s, "Command of Nature", 400, 100, 600);
			}
		};
		
		
		//Single player intermediate
		oneAI = new Button(50, 350, 300, 100, Color.lightGray, "1 AI");
		threeAI = new Button(450, 350, 300, 100, Color.lightGray, "3 AI");
		menus[1] = new Menu(false, oneAI, threeAI){
			public void drawCode(Scalable s){
				s.setColor(Color.white);
				s.setFont(title);
				Drawer.drawCenteredStackString(s, "Choose AI Count", 400, 100, 600);
			}
		};
		//Traversals to / from - handled our side, mouse clicked event
		
		
		//Multiplayer intermediate
		host = new Button(50, 350, 300, 100, Color.lightGray, "Host");
		join = new Button(450, 350, 300, 100, Color.lightGray, "Join");
		
		name = new JTextField("Name:");
		name.setBounds(300, 250, 200, 50);
		add(name);
		name.setFont(nameFont);
		
		ipInput = new JTextField("IP:");
		ipInput.setBounds(450, 450, 300, 50);
		add(ipInput);
		ipInput.setFont(nameFont);
		
		menus[2] = new Menu(false, host, join){
			public void drawCode(Scalable s){
				s.setColor(Color.white);
				s.setFont(title);
				Drawer.drawCenteredStackString(s, "Multiplayer", 400, 100, 600);
			}
		};
		menus[2].add(name, ipInput);
		
		
		//Hosting intermediate
		start = new Button(300, 500, 200, 85, Color.lightGray, "Start");
		menus[3] = new Menu(false, start){
			public void drawCode(Scalable s){
				s.setColor(Color.white);
				s.setFont(title);
				String name = "Error";
				String[] names = new String[0];
				int count = 0;
				//System.out.println(serverHolder.isClosed());
				if (serverHolder != null ? !serverHolder.isClosed() : false){
					name = serverHolder.getServerAddr();
					names = serverHolder.getServer().getClientNames();
					count = names.length;
				}
				Drawer.drawCenteredStackString(s, name, 400, 100, 800);
				s.setFont(header);
				Drawer.drawCenteredStackString(s, count + " Players Connected", 400, 300, 800);
				String displayNames = "";
				for (String item : names){
					displayNames += item + "\n";
				}
				Drawer.drawCenteredStackString(s, displayNames, 400, 350, 800);
				
				//DEBUG
				//s.setFont(info);
				//Drawer.drawCenteredStackString(s, serverHolder.getServer().getConnections(), 400, 350, 800);
			}
		};
		
		
		//2 Player host intermediate
		oneVOne = new Button(250, 325, 300, 100, Color.lightGray, "1 Vs 1");
		twoVTwoAllies = new Button(250, 450, 300, 100, Color.lightGray, "2 Vs 2 as Allies");
		twoVTwoEnemies = new Button(250, 575, 300, 100, Color.lightGray, "2 Vs 2 as Enemies");
		menus[4] = new Menu(false, oneVOne, twoVTwoAllies, twoVTwoEnemies){
			public void drawCode(Scalable s){
				s.setColor(Color.white);
				s.setFont(title);
				Drawer.drawCenteredStackString(s, "Choose fighting option for the two connected players", 400, 300, 800);
			}
		};
		
		
		//3 Player host intermediate
		menus[5] = new Menu(false){
			public void drawCode(Scalable s){
				s.setColor(Color.white);
				s.setFont(title);
				Drawer.drawCenteredStackString(s, "Choose team alignment", 400, 300, 800); //3 buttons, p1 & p2 V p3 / p1 V p2 & p3 / p1 & p3 V p2 - replace p# with proper names of connected
			}
		};
		
		
		//4 Player host intermediate - Might replace with 2 cols 4 rows choose each player's team with lighting up buttons
		menus[6] = new Menu(false){
			public void drawCode(Scalable s){
				s.setColor(Color.white);
				s.setFont(title);
				Drawer.drawCenteredStackString(s, "Choose team alignment", 400, 300, 800); // 6 buttons, 2 cols of 3 rows displaying each possiblity for each player team variants
			}
		};
		
		
		//Joining intermediate
		menus[7] = new Menu(false){
			public void drawCode(Scalable s){
				s.setColor(Color.white);
				s.setFont(title);
				Drawer.drawCenteredStackString(s, "Lobby", 400, 100, 800);
			}
		};
		//menus[7].add(ipInput);
		
		
		//Settings Menu
		menus[8] = new Menu(false);
		
		// Just for ease of drawing the Game
		menus[9] = new Menu(false);
	}
	
	//boolean drawSqr = true;
	
	//drawing the game
	public void paintComponent(Graphics g){ //draw screen
        super.paintComponent(g); //Don't touch
		s.init(g);
		
		if (menuOrder.get(0).intValue() != 10 && menuOrder.get(0).intValue() != 11 && menuOrder.get(0).intValue() != 0) //ind 10, 11 should be game states 2 player and 4 player respectfully, ind 0 is title, dont show back button there
			back.draw(s);
		
		for (Menu menu : menus){
			menu.draw(s);
		}
		
		//Draw any error codes
		if (!errorMessage.equals("")){
			s.setColor(Color.black);
			s.fillRect(400 - s.getFontMetrics().stringWidth(errorMessage)/2 - 15, 300 - s.getFontMetrics().getHeight()*Drawer.getLines(s, errorMessage, 800)/2 - 15, s.getFontMetrics().stringWidth(errorMessage) * Drawer.getLines(s, errorMessage, 800)+30, s.getFontMetrics().getHeight()+30);
			s.setColor(Color.white);
			s.setFont(header);
			Drawer.drawCenteredStackString(s, errorMessage, 400, 300 + s.getFontMetrics().getHeight()/2, 800);
		}
		
		s.setColor(Color.white);
		if (serverHolder != null) s.drawString(""+serverHolder.isRunning(), 15, 100);
		
		
		
		/*if (drawSqr){
			s.fillRect(750, 0, 50, 50);
			drawSqr = false;
		}
		else drawSqr = true;*/
	}
	
	//Client side - Info getting helpers
	public String getName(){
		return name.getText();
	}
	public String getIP(){
		if (ipInput.getText() == "IP:") return null;
		return ipInput.getText();
	}
	
	//key events
	public void keyPressed(KeyEvent e){}
	public synchronized void keyReleased(KeyEvent e){}
	public synchronized void keyTyped(KeyEvent e){}
	
	//mouse events
	public synchronized void mouseClicked(MouseEvent e){
		s.convertMouseEvent(e);
		
		//Menu Traversal stuffs
		//Menus[0] is the user choosing multiplayer or single player mode
		if (singlePlayer.isClicked(e) && menus[0].isVisible()){ //opted for single player
			menuOrder.add(1);
			menus[0].goTo(menus[1]);
		}
		else if (multiPlayer.isClicked(e) && menus[0].isVisible()){ //opted for multiplayer
			menuOrder.add(2);
			menus[0].goTo(menus[2]);
		}
		//They have chosen Multiplayer and now have decided to host the game
		else if (host.isClicked(e) && menus[2].isVisible()){
			menuOrder.add(3);
			menus[2].goTo(menus[3]);
			//Starts up the server
			serverHolder = new Server();
			serverRunner = new Thread(serverHolder);
			serverRunner.start();
			//System.out.println(serverHolder.getServerAddr());
			
			/*try{
			System.out.println(java.net.InetAddress.getLocalHost().getHostAddress());
			}
			catch (Exception ex){
				ex.printStackTrace();
			}*/
			//and the client - makes it really easy to convert into multiplayer game if the host is also connecting in the same way
			ch = new ConnectionHandler("127.0.0.1", false, 4, getName());
			//serverHolder.die();
		}
		//Player instead has opted to join an active server and has typed the IP into the text box
		else if (join.isClicked(e) && menus[2].isVisible()){ //clicking on the join button while its visible
			if (getIP() != null){
				menuOrder.add(7);
				menus[2].goTo(menus[7]);
				
				ch = new ConnectionHandler(getIP(), false, 4, getName()); //creating the client to connect to the server ip specified
			}
			else{
				errorMessage = "No IP currently looking for players";
			}
		}
		//User has pressed an incorrect button and was taken to a point they did not intend to reach - valid on all menus
		else if (back.isClicked(e)){
			
			//leaving the server after creating it
			if (menuOrder.get(0) >= 3){
				if (serverHolder != null) serverHolder.die();
				if (ch != null) ch.close();
			}
			
			menus[menuOrder.pop()].setVisible(false); //hide the previous one, remove from our list
			if (menuOrder.size() > 0)
				menus[menuOrder.get(0)].setVisible(true); //show the next one on our list
			else
				menus[0].setVisible(true); //default incase the back button takes us to an unknown location
		}
		
		
		repaint();
	}
	public synchronized void mouseEntered(MouseEvent e){}
	public synchronized void mouseExited(MouseEvent e){}
	public synchronized void mousePressed(MouseEvent e){}
	public synchronized void mouseReleased(MouseEvent e){}
	public synchronized void mouseDragged(MouseEvent e){}
	public synchronized void mouseMoved(MouseEvent e){
		
	}
	
	public synchronized void windowClosing(WindowEvent e){
		//System.out.println("Closing");
		if (serverHolder != null) serverHolder.die(); //close the server if we're leaving
		if (ch != null) ch.close();
	}
	public synchronized void windowDeactivated(WindowEvent e){}
	public synchronized void windowActivated(WindowEvent e){}
	public synchronized void windowClosed(WindowEvent e){}
	public synchronized void windowDeiconified(WindowEvent e){}
	public synchronized void windowIconified(WindowEvent e){}
	public synchronized void windowOpened(WindowEvent e){}
	
	//mouse wheel events
	public synchronized void mouseWheelMoved(MouseWheelEvent e) {
		//mk.mouseWheelMoved(e);
		repaint();
	}
	
	//action events (enter on JTextField)
	public synchronized void actionPerformed(ActionEvent e){}
	
	//infinite Loop for redrawing
	public void animate(){
        while(true){
            try{
                Thread.sleep(50);
            }
            catch(InterruptedException ex){
                Thread.currentThread().interrupt();
            }
            repaint();
        }
    }
	
	private class Server implements Runnable{
		private ConnectionHandler server;
		private boolean running;
		public Server(){
			server = new ConnectionHandler("127.0.0.1", true, 4, "Server");
			running = true;
		}
		public void run(){
			System.out.println("Running for funs started");
			byte[] message;
			while (running){
				try{
					Thread.sleep(10); //give a momentary rest before reading through all messages
				}
				catch(InterruptedException ex){
					Thread.currentThread().interrupt();
				}
				if (!server.hasBuffer()) continue; //no messages, just wait around
				
				while (server.hasBuffer()){
					message = server.pop();
					handleMessage(message);
				}
			}
			System.out.println("Running of server is over");
		}
		
		public void handleMessage(byte[] message){
			//TODO : Place holder method until server logic comes in
			System.out.print("Server Handle Message: ");
			for (byte num : message){
				System.out.print(num + ", ");
			}
			System.out.println("\b\b - messaged");
		}
		
		public String getServerAddr(){
			return server.getLocalAddressString();
		}
		
		public boolean isClosed(){
			//System.out.println("Is Server? " + server.isServer());
			return server.isClosed();
		}
		
		public int connectionCount(){
			//System.out.println(server.getConnections());
			return server.getConnections().split("\n").length-1;
		}
		
		public void die(){
			running = false;
			server.close();
		}
		
		public boolean isRunning(){
			return running;
		}
		
		public ConnectionHandler getServer(){
			return server;
		}
	}
}