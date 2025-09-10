//read/write to file
import java.util.Scanner;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

class Runner{
	public static void main(String[] args){
		boolean test = false;
		int repeats = 1, best=0, bestIndex=0;
		Random random = new Random();
		if (args.length != 0){
			if (args[0].equals("write")){
				String send=":", writeString = "";
				for (int i=0;i<25;i++){
					send += "0;";
				}
				writeString = "5;5;5;5;5;2" + send + send + send + send + send + ":0;0;0;0;0;0;0;0;0;0;:"+ "\n";
				write(writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString + writeString);
				return;
			}
			else if (args[0].equals("test")){
				test = true;
				repeats = 10000;
			}
			else{
				try{
					repeats = Integer.parseInt(args[0]);
				}
				catch (NumberFormatException e){
					repeats = 1;
				}
			}
		}
		ArrayList<String[]> rawInfo = read();
		if (rawInfo == null){
			//error occured, state and exit
			System.out.println("Error occured with reading");
			return;
		}
		ArrayList<Trader> traders = new ArrayList<Trader> (20);
		
		for (String[] info : rawInfo){
			Trader tempTrade = new Trader(info, 100L);
			traders.add(tempTrade);
		}
		Trader savior = traders.get(0);
		long bestScoreSavior = 0L;
		for (int p=0;p<repeats;p++){
			int[] market = new int[25];
			for (int i=0;i<market.length;i++){
				//market[i] = (int)((random.nextGaussian()+3)*25);
				market[i] = (int)(100/market.length*i);
			}
			
			for (Trader tempTrade : traders){
				int last=0, lastLast=0;
				for (int i=0;i<market.length;i++){
					tempTrade.trade(market[i], last, lastLast); //do a round of trading
					//save the last few prices for the bot
					lastLast = last;
					last = market[i];
				}
			}
			
			int bestScoreIndex=0;
			long bestScore=traders.get(0).getScore();
			boolean allEqual = true;
			for (int i=1;i<traders.size();i++){
				if (traders.get(i).getScore() > bestScore && !test){
					bestScore = traders.get(i).getScore();
					bestScoreIndex = i;
				}
				if (traders.get(i).getScore() != bestScore && !test){
					allEqual = false;
				}
			}
			
			if (allEqual){
				//the bots preformed the same, choose a random one
				bestScoreIndex = (int)(Math.random() * traders.size());
				
			}
			
			best = (int)bestScore;
			bestIndex = bestScoreIndex;
			if (!test){
				for (Trader trade : traders){
					if (trade != traders.get(bestScoreIndex)){
						trade.mutate(50);
					}
				}
			}
			if (test && p%1000 == 0) System.out.print(bestScore + ", ");
			if (p%100 ==0 && bestScore != 100){
				if (bestScore > bestScoreSavior){
					savior = traders.get(bestScoreIndex);
					bestScoreSavior = bestScore;
					//System.out.print(" >> ");
				}
				else if (bestScore < bestScoreSavior){
					traders.set(bestScoreIndex, savior);
					System.out.print(" >/> ");
				}
			}
			if (test && !allEqual){
				for (Trader temp : traders){
					System.out.println(temp.getScore());
				}
			}
		}
		String filePack = "";
		for (Trader temp : traders){
			filePack += temp.filePack() + "\n";
		}
		write(filePack);
		System.out.println(best + " @ " + bestIndex);
	}
	public static void write(String lines){
		try (FileWriter writer = new FileWriter("Values.txt")){ //will always exit with closing the file
            writer.write(lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	public static ArrayList<String[]> read(){
		ArrayList<String[]> send = new ArrayList<String[]>(10);
		try {
            Scanner scan = new Scanner(new FileReader("Values.txt"));
            //reads one line at a time
            while (scan.hasNextLine()){
                send.add(scan.nextLine().split(":"));
            }
			return send;
			
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
		return null;
	}
}