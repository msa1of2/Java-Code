package helpful.connection;

import java.util.Scanner;
import helpful.connection.*;

public class Debug{
	public static void main(String[]args){
		ConnectionHandler cH = null;
		Thread temp = null;
		
		System.out.println("Running");
		
		Scanner scan = new Scanner(System.in);
		String input = "";
		while(!input.equals("0") && (cH != null ? !cH.isClosed() : true)){
			System.out.println("Closed? " + (cH != null ? cH.isClosed() : "null"));
			input = scan.nextLine();
			
			if (input.equals("1")){
				cH = new ConnectionHandler("127.0.0.1", true, 4, "Server");
				temp = new Thread(new Run(cH), "Server thread");
				temp.start();
				System.out.println("new Server made");
			}
			if (input.equals("2")){
				cH = new ConnectionHandler("127.0.0.1", false, 4, "Client");
				temp = new Thread(new Run(cH), "Client thread");
				temp.start();
				System.out.println("new client made");
			}
			if (input.charAt(0) == '3' && cH != null){
				cH.send(ByteTranslater.toBytes(input.substring(1).toCharArray()), "Client");
				System.out.println("Server Sent : " + input.substring(1));
			}
			if (input.charAt(0) == '4' && cH != null){
				cH.send(ByteTranslater.toBytes(input.substring(1).toCharArray()), "Server");
				System.out.println("Client Sent : " + input.substring(1));
			}
			if (input.equals("check") && cH != null){
				System.out.println(cH.getConnections());
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