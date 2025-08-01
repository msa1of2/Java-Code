package games;
import java.net.*;
import java.io.*;
import java.lang.NullPointerException;
import java.lang.IndexOutOfBoundsException;
import java.util.LinkedList;

public class ConnectionHandler{
	private boolean isServer, isConnected, raiseError;
	private Socket client;
	private ServerSocket server;
	private Thread listener;
	private LinkedList<byte[]> buffer;
	private Receiver runner;
	private String errorMessage;
	public ConnectionHandler(String ipAddr, boolean isServer){
		//isServer = false;
		try{
			this.isServer = isServer;
			this.raiseError = false;
			errorMessage = "";
			if (isServer){
				try{
					server = new ServerSocket(2049);
				}
				catch (BindException e){
					//there is already a server being hosted on that port - really should never happen but idk
					errorMessage = "Already hosting a server";
					this.raiseError = true;
					return;
				}
				buffer = new LinkedList<byte[]>();
				runner = new Receiver(server, buffer);
				listener = new Thread(runner);
			}
			else{
				client = new Socket(ipAddr, 2049);
				buffer = new LinkedList<byte[]>();
				runner = new Receiver(client, buffer);
				listener = new Thread(runner);
			}
		
			//server = null;
			isConnected = false;
			
			listener.start();
			
		}
		catch (UnknownHostException e){
			this.raiseError = true;
			errorMessage = "Unkown Host";
		}
		catch(ConnectException e){
			this.raiseError = true;
			errorMessage = "Connection Refused";
		}
		catch (Exception e){
			e.printStackTrace();
			this.raiseError = true;
			errorMessage = e.toString();
			return;
		}
	}
	public boolean hasError(){
		return raiseError || (runner != null ? !runner.error().equals("") : false);
	}
	public String getError(){
		if (raiseError){
			String temp = errorMessage;
			errorMessage = "No error";
			raiseError = false;
			return temp;
		}
		else if (!runner.error().equals("")){
			String temp = runner.error();
			runner.resetError();
			return temp;
		}
		return "";
	}
	public void send(byte[] array){
		if (isServer && !isConnected) return; //no one to send to
		//normalizes the size to 8
		if (array.length > 8){ //cuts into multiple arrays of size 8
			byte[] temp;
			for (int times=0;times<(int)(Math.ceil(array.length/7.0));times++){
				temp = new byte[] {6, -1, -1, -1, -1, -1, -1, -1};
				System.arraycopy(array, times*7+(times==0?0:1), temp, times!=0?1:0, Math.min(times!=0?7:8, array.length-(times*7+(times==0?0:1))));
				send(temp);
			}
			return; // dealt with - all data sent properly
		}
		if (array.length < 8){ //fills empty data with 0s
			byte[] temp = new byte[] {-1, -1, -1, -1, -1, -1, -1, -1};
			System.arraycopy(array, 0, temp, 0, array.length);
			array = temp;
		}
		
		String send = "";
		for (byte num : array){send += num + ", ";}
		System.out.println("Sending : " + send);
		
		try{
			
			if (isServer) runner.getListen().getOutputStream().write(array);
			else client.getOutputStream().write(array);
		}
		catch (SocketException e){
			end(); //issue happens, end the connection
		}
		catch (Exception e){
			System.err.println(e);
			System.out.println("send function");
		}
		//end();
	}
	public String getServerAddress(){
		try{
			if (isServer && server != null) return InetAddress.getLocalHost().getHostAddress();
		}
		catch(UnknownHostException e){
			return "Unkown host exception occured";
		}
		catch(Exception e){
			return "General Exception occured";
		}
		return "Error occured";
	}
	public boolean hasReads(){
		return !buffer.isEmpty();
	}
	public byte[] read(){
		return buffer.poll();
	}
	public byte[] peek(){
		return buffer.peek();
	}
	public void remove(){
		buffer.remove();
	}
	public LinkedList<byte[]> readList(){
		return buffer;
	}
	public void end(){ //kills the listening thread
		
		listener.interrupt();
		try{
			if (client != null) client.close();
			if (isServer && server != null) server.close();
			isConnected = false;
			runner.setClosed(true);
			System.out.println("Connection ended properly");
		}
		catch (Exception e){
			e.printStackTrace();
			System.out.println("End function failed in ConnectionHandler");
		}
	}
	public void sendKick(){
		byte[] array = new byte[8];
		for (int i=0;i<array.length;i++){
			array[i] = Byte.MAX_VALUE;
		}
		send(array);
	}
	public boolean isConnected(){
		return isConnected;
	}
	public boolean isServer(){
		return isServer;
	}
	public boolean isClosed(){
		/*if (isServer) return server.isClosed();
		return client.isClosed();*/
		return runner == null ? true : runner.isClosed();
	}
	public String toString(){
		if (isServer) return "isServer " + (server == null ? "server null" : server.toString()) + ", " + (runner == null ? "runner null" : runner.toString());
		return "isClient " + client.toString() + ", " + runner.toString();
	}
	//add a thing where the server can text a full byte array of highest values to close the client on other end
	private class Receiver implements Runnable{
		private Socket listen;
		private ServerSocket serverListen;
		private boolean isServer;
		private LinkedList<byte[]> buffer, internalBuffer;
		private boolean isClosed;
		private String errorString;
		private byte size;
		public Receiver(Socket listen, LinkedList<byte[]> buffer){
			this.listen = listen;
			this.buffer = buffer;
			this.serverListen = null;
			isServer = false;
			isClosed = false;
			errorString = "";
			internalBuffer = new LinkedList<byte[]>();
			size = 0;
		}
		public Receiver(ServerSocket listen, LinkedList<byte[]> buffer){
			this.serverListen = listen;
			this.buffer = buffer;
			this.listen = null;
			isServer = true;
			isClosed = false;
			errorString = "";
			internalBuffer = new LinkedList<byte[]>();
			size = 0;
		}
		public Socket getListen(){
			return listen;
		}
		public String toString(){
			return (listen != null ? "listen "+listen.toString()+","+listen.isClosed() : "listen null") + ", " + (serverListen != null ? "serverListen "+serverListen.toString()+","+serverListen.isClosed() : "server null") + ", " + isClosed;
		}
		public String error(){
			return errorString;
		}
		public void resetError(){
			errorString = "";
		}
		public void run(){
			while(!isClosed){
				try{
					Thread.sleep(5);
				}
				catch(InterruptedException ex){
					Thread.currentThread().interrupt();
				}
				if (!isServer ? listen.isClosed() : false) return;
				//if (isServer ? serverListen.isClosed() : false) return;
				if (!isConnected && isServer){
					try{
						listen = serverListen.accept();
						client = listen;
						isConnected = true;
					}
					catch (IOException e) {
					   System.out.println("Exception caught when trying to listen on port " + serverListen.getLocalPort() + " or listening for a connection");
					   System.out.println(e.getMessage());
				    }
				}
				else{
					//due to the linked list nature, the adding and removing don't interfere with each other, don't need to be syncronized
					byte[] nums = new byte[8];
					try{
						if (listen != null) listen.getInputStream().read(nums);
						if (nums[0] == Byte.MAX_VALUE){
							//possible kick occured
							boolean kick = true;
							for (byte num : nums){
								if (num != Byte.MAX_VALUE){
									System.out.println(num);
									kick = false;
								}
							}
							if (kick){
								errorString = "Disconnected from other user";
								end();
								return;
							}
						}
						if (true){
							String send = "recieved: {";
							for (byte num : nums){
								send += num + ", ";
							}
							System.out.println(send + "}");
						}
						if (nums[0] == 3){
							String send = "recieved: {";
							for (byte num : nums){
								send += num + ", ";
							}
							sendKick();
							errorString = send + "}";
							end();
							return;
						}
						buffer.add(nums);
						/*//change it so if a multi array message comes through this'll pick it up, put it in its internal buffer, recombine into larger array and put that into the actual buffer
						if (nums[0] == 1 || nums[0] == 2 || size > 0){
							if ((nums[0] == 1 || nums[0] == 2) && size == 0){ 
								size = nums[1];
								byte[] temp = new byte[] {-1, -1, -1, -1, -1, -1, -1, -1};
								System.arraycopy(nums, 2, temp, 0, Math.min(size, 5));
								if (size <= 5){
									size = 0; //reset size
								}
								else{
									
								}
							}
						}*/
					}
					catch (SocketException e){
						end();
						return;
					}
					catch (Exception e){
						//replace false with "isServer ? serverListen.isClosed() : false" - debug things - getting info
						if ((!isServer ? listen.isClosed() : false) || (false)){ //conection closed from other user
							end();
							return;
						}
						System.err.println(e);
						System.out.println("recieved loop " + this.toString());
					}
				}
			}
			isClosed = true; //while loop exited - no longer listening - be closed
		}
		public void setClosed(boolean closed){
			/*for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
				System.out.print(ste + "\n");
			}*/
			isClosed = closed;
		}
		public boolean isClosed(){
			return isClosed;
		}
	}
}
