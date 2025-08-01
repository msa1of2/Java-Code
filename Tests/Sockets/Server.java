import java.net.*;
import java.io.*;

public class Server {

   public static void main(String[] args) {

       int portNumber = 1024;

       try {

            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept(); //wait for a connection
            System.out.println("Connection Successful!"); //Will print after connection is made
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //First Message Sent
			byte[] nums = new byte[10];
			for (byte i=0;i<nums.length;i++){
				nums[i] = i;
				System.out.print(nums[i] + ", ");
			}
			System.out.println();
			
            clientSocket.getOutputStream().write(nums);
			clientSocket.getInputStream().read(nums);
			
			for (int num : nums){
				System.out.print(num + ", ");
			}
			System.out.println();
           out.close();

       } catch (IOException e) {
           System.out.println("Exception caught when trying to listen on port " +
               portNumber + " or listening for a connection");
           System.out.println(e.getMessage());
       }
   }
}