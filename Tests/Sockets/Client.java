import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {

   public static void main(String[] args) {

       String hostName = "localhost";
       int portNumber = 1024;
       Scanner sc = new Scanner(System.in);

       try {

           Socket serverSocket = new Socket(hostName, portNumber);
           PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
           BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
           
			byte[] nums = new byte[10];
			serverSocket.getInputStream().read(nums);
			for (int i=0;i<nums.length;i++){
				nums[i] = (byte)func(nums[i], 0);
			}
			serverSocket.getOutputStream().write(nums);
           
          
           in.close();

       } catch (IOException e) {
           System.err.println("Couldn't get I/O for the connection to " + hostName);
           System.exit(1);
       }
   }
   public static int func(int num, int count){
	   if (num == 4 || num == 2 || num == 1 || num == 0) return count;
	   if (num%2 == 0) return func(num/2, count+1);
	   return func(3*num+1, count+1);
   }
}