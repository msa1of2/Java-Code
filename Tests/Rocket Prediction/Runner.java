import javax.swing.JOptionPane;
//File reading / writing
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
//Graphics
import java.awt.Graphics2D;
import java.awt.Color;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

public class Runner{
	public static void main(String[] args){
		new Runner();
	}
	
	private double windTunnelAirVelocity;
	private double windTunnelDragForce;
	private double engineImpulse;
	private double burnTime;
	private double launchMass;
	private double fuelMass;
	private double timeStepSize;
	private double totalTime;
	
	private double dragCoeff;
	private double avgThrust;
	private double boostMass;
	private double coastMass;
	
	public Runner(){
		double[] vals = readFile();
		if (vals == null ? true : (vals.length < 8)) vals = new double[8];
		//System.out.println("Starting");
		//System.out.println(bytesToLong(longToBytes(4607182418800017408L)));
		//System.out.println("Ending");
		//if (true) return;
		
		//String name = JOptionPane.showInputDialog(null, "Air Velocity recorded in the wind tunnel:", "Input", JOptionPane.QUESTION_MESSAGE);
		vals[0] = getUserDouble("Air Velocity recorded in the wind tunnel (m/s):", vals[0]);
		windTunnelAirVelocity = vals[0];
		vals[1]= getUserDouble("Drag force applied to the rocket in the wind tunnel (N):", vals[1]);
		windTunnelDragForce = vals[1];
		
		vals[2] = getUserDouble("What is the total engine impulse (Ns):", vals[2]);
		engineImpulse = vals[2];
		vals[3] = getUserDouble("How long does the engine burn for (s):", vals[3]);
		burnTime = vals[3];
		
		vals[4] = getUserDouble("What is the total weight of the rocket right before launch (kg):", vals[4]);
		launchMass = vals[4];
		vals[5] = getUserDouble("What is the mass of the fueld being burned off in flight (kg):", vals[5]);
		fuelMass = vals[5];
		
		vals[6] = getUserDouble("What is the time step size for each step of the equation (s):", vals[6]);
		timeStepSize = vals[6];
		vals[7] = getUserDouble("What is the total time you would like to simulate (s):", vals[7]);
		totalTime = vals[7];
		
		while (timeStepSize <= 0){
			JOptionPane.showMessageDialog(null, "Error, time step size must be greater than 0", "Error", JOptionPane.ERROR_MESSAGE);
			vals[6] = getUserDouble("What is the time step size for each step of the equation (s):", vals[6]);
			timeStepSize = vals[6];
		}
		
		//print(vals);
		
		saveValues(vals);
		
		//print(readFile());
		
		dragCoeff = (windTunnelDragForce * 10000) / (windTunnelAirVelocity * windTunnelAirVelocity * 4);
		avgThrust = engineImpulse / burnTime;
		boostMass = (launchMass * 2 - fuelMass) / 2;
		coastMass = launchMass - fuelMass;
		
		int steps = (int)(totalTime / timeStepSize);
		//System.out.println(steps);
		
		double[] height = new double[steps];
		double[] velocity = new double[steps];
		double[] acceleration = new double[steps];
		double[] netForce = new double[steps];
		double[] dragForce = new double[steps];
		
		int index = compute(vals, dragCoeff, avgThrust, boostMass, coastMass, height, velocity, acceleration, netForce, dragForce);
		
		int xSize = 400;
		int ySize = 400;
		
		BufferedImage image = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.lightGray);
		g.fillRect(0, 0, xSize, ySize);
		
		double deltaX = xSize / steps;
		for (int i=0;i<steps;i++){
			g.setColor(Color.red);
			g.fillOval((int)(i*xSize / steps), ySize-((int)(height[i]/150 * ySize)+100)-4, 5,5);
			
			g.setColor(Color.green);
			g.fillOval((int)(i*xSize / steps), ySize-((int)(velocity[i]/150 * ySize)+100)-4, 5, 5);
			
			g.setColor(Color.blue);
			g.fillOval((int)(i*xSize / steps), ySize-((int)(acceleration[i]/150 * ySize)+100)-4, 5, 5);
		}
		
		g.setColor(Color.black);
		g.drawLine(0, ySize-100, xSize, ySize-100);
		g.drawString("0", 5, ySize-100+g.getFontMetrics().getHeight());
		
		g.drawLine((int)(index * xSize / steps), 0, (int)(index * xSize / steps), ySize);
		g.drawString("t(h): " + (index * timeStepSize), (int)(index * xSize / steps)+5, g.getFontMetrics().getHeight());
		
		g.drawLine(0, ySize - ((int)(height[index]/150 * ySize)+100)-4, xSize, ySize - ((int)(height[index]/150 * ySize)+100)-4);
		g.drawString("h: " + (Math.round(height[index]*100)/100.0), 5, ySize - ((int)(height[index]/150 * ySize)+100)-6);
		
		g.drawLine((int)(vals[3] / timeStepSize * xSize/steps), 0, (int)(vals[3] / timeStepSize * xSize/steps), ySize);
		g.drawString("t = " +(Math.round(vals[3]*100)/100.0), (int)(vals[3] / timeStepSize * xSize/steps)+ 5, g.getFontMetrics().getHeight());
		
		g.dispose();

		// Create an ImageIcon from the image
		ImageIcon icon = new ImageIcon(image);

		// Display the image in a JOptionPane
		JOptionPane.showMessageDialog(null, icon, "Custom Image", JOptionPane.PLAIN_MESSAGE);
	}
	
	public static int compute(double[] vals, double dragCoeff, double avgThrust, double boostMass, double coastMass, double[] height, double[] velocity, double[] acceleration, double[] netForce, double[] dragForce){
		height[0] = 0;
		velocity[0] = 0;
		acceleration[0] = 0;
		netForce[0] = 0;
		dragForce[0] = 0;
		
		double bestHeight = 0;
		int bestTimeIndex = 0;
		
		for (int t=1;t<height.length;t++){
			dragForce[t] = (velocity[t-1] > 0 ? -1 : 1) * dragCoeff * velocity[t-1] * velocity[t-1] * 0.0004; // keeps drag force acting against velocity
			netForce[t] = (t * vals[6] < vals[3]) ? avgThrust + boostMass * -9.8 + dragForce[t] : coastMass * -9.8 + dragForce[t]; //currently burning fuel : no more burn
			acceleration[t] = (t * vals[6] < vals[3]) ? netForce[t] / boostMass : netForce[t] / coastMass; //currently burning : no burn
			velocity[t] = velocity[t-1] + (acceleration[t] + acceleration[t-1]) * vals[6] / 2;
			height[t] = height[t-1] + (velocity[t] + velocity[t-1]) * vals[6] / 2;
			
			if (height[t] > bestHeight){
				bestHeight = height[t];
				bestTimeIndex = t;
			}
			if (height[t] <= 0){
				height[t] = 0;
				velocity[t] = 0;
				netForce[t] = 0;
				dragForce[t] = 0;
				acceleration[t] = 0;
			}
		}
		
		System.out.println("Best height reached at time: " + (bestTimeIndex * vals[6]) + " at a value of " + bestHeight);
		return bestTimeIndex;
	}
	
	//ease to get user values
	public static double getUserDouble(String question){
		return getUserDouble(question, 0.0);
	}
	public static double getUserDouble(String question, Double basic){
		String result;
		double send = 0.0;
		do {
			result = (String)JOptionPane.showInputDialog(null, question, "Input", JOptionPane.PLAIN_MESSAGE, null, null, basic.toString());
			if (result == null) continue;
			try{
				send = Double.parseDouble(result);
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error, expected value to be a number", "Error", JOptionPane.ERROR_MESSAGE);
				result = null;
			}
		} while (result == null);
		return send;
	}

	//read from a saved file of the previous values selected
	public static double[] readFile(){
		double[] send = new double[8];
		byte[] line = new byte[Double.BYTES];
		FileInputStream reader;
		try{
			reader = new FileInputStream(new File("nums.dat"));
			for (int i=0;i<send.length;i++){
				try{
					reader.read(line);//read a single line for the mouse calibration
					send[i] = Double.longBitsToDouble(bytesToLong(line));
				}
				catch (IOException e){
					send[i] = 0.0;
				}
			}
			
		}
		catch (IOException e){
			e.printStackTrace();
			for (int i=0;i<send.length;i++){send[i] = 0.0;}
			return send;
		}
		return send;
	}
	public static void saveValues(double[] vals){
		byte[] line = new byte[Double.BYTES];
		FileOutputStream writer;
		try{
			writer = new FileOutputStream(new File("nums.dat"));
			for (int i=0;i<vals.length;i++){
				try{
					line = longToBytes(Double.doubleToLongBits(vals[i]));
					writer.write(line);
				}
				catch (IOException e){
					System.out.println("Saving failed : " + e.getMessage());
				}
			}
		}
		catch (IOException e){
			System.out.println("Saving failed Master Loop: " + e.getMessage());
		}
	}
	
	public static void print(double[] vals){
		for (double val : vals){
			System.out.print(Double.doubleToLongBits(val) + ", ");
		}
		System.out.println("\b\b");
	}
	public static void print(byte[] bytes){
		for (byte val : bytes){
			System.out.print(val + ", ");
		}
		System.out.println("\b\b");
	}
	
	//bit manipulation
	public static long bytesToLong(byte[] bytes){
		long send = 0;
		//System.out.print("Given: ");
		//print(bytes);
		for (int i=0;i<Math.min(bytes.length, 8);i++){
			send <<= 8; //shift current number bits by 8 to the left
			send |= (bytes[7 - i] & 0xff); //add the next byte in the sequence
			//System.out.println("toLong: " + Long.toHexString(send) + ", " + bytes[7-i]);
		}
		return send;
	}
	public static byte[] longToBytes(long num){
		byte[] send = new byte[8];
		for (int i=0;i<8;i++){
			send[i] = (byte)(num & 0xff); //only grab the last 8 bits
			num >>= 8; //shift bits to right 8 spots
			//System.out.println("toBytes: " + Long.toHexString(num) + ", " + send[i]);
		}
		//System.out.print("Return: ");
		//print(send);
		return send;
	}
}