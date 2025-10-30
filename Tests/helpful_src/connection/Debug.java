package helpful.connection;

import java.util.Scanner;
import helpful.connection.*;

public class Debug{
	public static void main(String[]args){
		ConnectionHandler cH = null;
		Thread temp = null;
		byte[] local = ConnectionHandler.getLocalAddress();
		System.out.println("Local IP: " + (0xff&local[0]) + "." + (0xff&local[1]) + "." + (0xff&local[2]) + "." + (0xff&local[3]) + "\nConnect to: ");
		
		Scanner scan = new Scanner(System.in);
		String addr = scan.nextLine();
		String input="";
		while(!input.equals("0") && (cH != null ? !cH.isClosed() : true)){
			System.out.println("\n\nClosed? " + (cH != null ? cH.isClosed() : "null"));
			System.out.println("1 - Create Server Ex 1 <Name>"
							  +"\n2 - Create Client Ex. 2 <Name>"
							  +"\n3 - Send Message Ex. 3 <Name> <Message>"
							  +"\n4 - Find shortest path to destination Ex. 4 <Name>"
							  +"\n5 - List all the names of the connected clients");
			input = scan.nextLine();
			
			
			if (input.charAt(0) == '1'){
				cH = new ConnectionHandler(addr, true, 4, input.substring(2));
				//System.out.println("<" + input.substring(2) + ">");
				temp = new Thread(new Run(cH), "Server thread");
				temp.start();
				System.out.println("new Server made");
			}
			if (input.charAt(0) == '2'){
				cH = new ConnectionHandler(addr, false, 4, input.substring(2));
				temp = new Thread(new Run(cH), "Client thread");
				temp.start();
				System.out.println("new client made");
			}
			if (input.charAt(0) == '3' && cH != null){
				String name = input.substring(2, input.indexOf(' ', 2));
				String message = input.substring(input.indexOf(' ', 2)+1);
				//System.out.println("<" + name + "> <" + message + ">");
				int res = cH.send(ByteTranslater.toBytes(message.toCharArray()), name);
				if (res == 0) System.out.println("Sending: " + message + ", to: " + name);
				else System.out.println("Message failed to send, error code: " + res);
			}
			if (input.charAt(0) == '4' && cH != null){
				String name = input.substring(2);
				System.out.println("\nThe path there is found through this sequence: \n\n" + cH.getPathTo(name));
			}
			if (input.charAt(0) == '5' && cH != null){
				String[] names = cH.getClientNames();
				String nameString = "";
				for (String name : names){
					nameString += name + "\n";
				}
				System.out.println("All the names:\n" + nameString + "\b");
			}
			if (input.equals("check") && cH != null){
				System.out.println("\n\n" + cH.getConnections());
				System.out.println("\n\n" + cH.getConnString());
			}
		}
		if (cH != null) cH.close();
		if (temp != null) temp.interrupt();
		System.out.println("Done");
	}
	public static class Run implements Runnable{
		ConnectionHandler cH;
		public Run(ConnectionHandler cH){
			this.cH = cH;
		}
		public void run(){
			while (true){
				try{
					Thread.sleep(50);
				}
				catch(InterruptedException ex){
					Thread.currentThread().interrupt();
					return;
				}
				if (!cH.hasReads()) continue;
				byte[] dat = cH.pop();
				System.out.println("Thread receiver area:" + dat.length);
				System.out.println(Thread.currentThread().getName() + ": " + (dat.length >=2 ? new String(ByteTranslater.toChars(dat)):"null"));
				System.out.println(cH.getLocalName() + " : " + cH.getAvgDelay() + "ms");
			}
		}
	}
}