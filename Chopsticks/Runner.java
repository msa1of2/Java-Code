import helpful.datastructures.DLList;
import java.util.Scanner;

public class Runner{
	
	public static final int[][] vals = new int[][] {{0,0}, {0,1}, {0,2}, {0,3}, {0,4},
								{1,1}, {1,2}, {1,3}, {1,4},
								{2,2}, {2,3}, {2,4},
								{3,3}, {3,4},
								{4,4}};
	public static final int[][][] sums  = new int[][][] {{{0,0}},
										{{0,1}},
										{{0,2}, {1,1}},
										{{0,3}, {1,2}},
										{{0,4}, {1,3}, {2,2}},
										{{1,4}, {2,3}},
										{{2,4}, {3,3}},
										{{3,4}},
										{{4,4}}};
	public static void main(String[] args){
		new Runner();
	}
	
	CSGraph.Node[] states = new CSGraph.Node[511]; //15 * 15 * 2
	int winners = 0, redWin = 0, blueWin = 0;
	
	public Runner(){
		//System.out.println(getRedTeam(331));
		//System.out.println(getBlueTeam(331));
		long startTime = System.currentTimeMillis();
		
		
		calculate(false); //red team goes first
		
		/* //printing information
		CSGraph.Node start = null;
		DLList<CSGraph.Node> toPrint = new DLList<>();
		toPrint.add(states[setTeamTurn(setRedTeam(setBlueTeam(0, 5), 5), true)]);
		
		System.out.println(printable(toPrint.get(0)));
		
		for (int i=0;i<50 && toPrint.size() > 0;i++){
			start = toPrint.remove(0);
			if (start.next().size() == 0) continue;
			//System.out.println(i + ": " + start);
			for (CSGraph.Node nexts : start.next()){
				if (nexts == null) continue;
				//if (isWinner(nexts.get())) System.out.print(printable(nexts) + " - ");
				int value = nexts.get();
				if (isWinner(value)){
					if (isRedWinner(value)){ System.out.println("redWin: " + value);}
					else {System.out.println("blueWin: " + value);}
					
				}
				toPrint.add(nexts);
			}
			//System.out.println();
		}
		
		System.out.println("\n\nWinners:\n" + winners + "\nBlue Winners:" + blueWin + "\nRed Winners: " + redWin);
		*/
		
		//System.out.println(nextValues((short)154));
		
		CSGraph.Node start = states[setTeamTurn(setRedTeam(setBlueTeam(0, 5), 5), true)];
		/*System.out.println("Start: " + printable(start));
		System.out.print("Children: ");
		for (CSGraph.Node child : start.next()){
			System.out.print(printable(child) + ", ");
		}*/
		
		long endTime = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime-startTime));
		
		String in = "";
		Scanner scan = new Scanner(System.in);
		int num;
		while (!in.equals("q")){
			System.out.println("\n\n\nChoose index, 0-size or -1 to go up:\nParent: " + printable(start.getParent()) + "\nNode  : " + printable(start) + "\nNext  : " + printable(start.next()));
			in = scan.nextLine();
			try{
				num = Integer.parseInt(in);
			}
			catch (NumberFormatException e){
				System.out.println("Bad input\n\n");
				continue;
			}
			if (num < 0 && start.getParent() != null){
				start = start.getParent();
			}
			else if (num >= 0 && num < start.next().size()){
				start = start.next().get(num);
			}
			else{
				System.out.println("Bad input");
			}
			System.out.println("\n\n");
		}
		
	}
	
	public void calculate(boolean isComputerRed){
		CSGraph g = new CSGraph(setTeamTurn(setRedTeam(setBlueTeam(0, 5), 5), true));
		states[g.getHead().get()] = g.getHead();
		DLList<CSGraph.Node> toVisit = new DLList<CSGraph.Node>();
		CSGraph.Node current = null;
		CSGraph.Node toAssign = null;
		toVisit.add(g.getHead());
		int count = 0;
		while(toVisit.size() > 0 && count < 4000){
			current = toVisit.remove(0);
			
			for (short value : nextValues(current.get())){
				//System.out.print("Next for " + current.get() + " is " + value + " \n");
				if (isWinner(value)){ //if child/next node is a winner
					if (isRedWinner(value)){ redWin++;}
					else {blueWin++;}
					winners++;
				}
				if (states[value] == null){ //new node doesn't exist yet
					//if (current.get() == 137 || current.get() == 154) System.out.println(current + ", " + value);
					toAssign = g.add(current, value, current.getLayer()+1);
					//System.out.println(toAssign + ", " + states[value]);
					states[value] = toAssign; //we now have a state for this node
					if (isWinner(value)){
						toAssign.setScore(isComputerRed ^ isRedTurn(value) ? 1 : -1); //its a winner, update its score right away
					}
					else{
						if (value == 171) System.out.println(states[value]);
						toVisit.add(toAssign); //new node, see where it leads
					}
				}
				else if (value != setTeamTurn(setRedTeam(setBlueTeam(0, 5), 5), true)){ //new node does exist above the older node
					//if (current.get() == 171) System.out.println("Add to existing: " + printable(current) + ", " + printable(states[value]));
					if (current.next().indexOf(states[value]) == -1) current.add(states[value]); //point to new node
				}
			}
			
			count++;
		}
		boolean foundAll = false;
		count = 0;
		toVisit.clear();
		toVisit.add(g.getHead());
		int childCount = 0;
		while (toVisit.size() > 0 && count < 40000000){
			current = toVisit.remove(0);
			
			if (current.getScore() != 0) continue; //if a repeat was added into the system
			
			for (CSGraph.Node child : current.next()){
				if (child.getScore() == 0){
					
					//not yet assigned score, need to go find it
					toVisit.add(child);
					continue; //go to next child
				}
				//child has a score
				//if (-child.getScore() > 0){ //we found a good score
				//System.out.println("good score: " + printable(child) + ", " + toVisit.size());
				current.setScore(-child.getScore()); //update our best score
				if (-child.getScore() > 0) current.setChild(child); //easy to access best child
				if (current.getParent() != null){ //change the parent because a child changed
					for (CSGraph.Node parent : current.getParents()){
						parent.setScore(0);
						parent.setChild(null);
						toVisit.add(parent);
					}
				}
				//}
				//otherwise bad score, move on
			}
			//if ((count & 0x3ff) == 0) System.out.println("Pass");
			//if (count % 100 == 0) printScores();
			count++;
		}
		System.out.println("\n\nFinal Count : " + count + "\n");
		printScores();
		//System.out.println("Count: " + count);
		
		/*//calculate the scores 
		while (!foundAll && count < 3){
			foundAll = true;
			for (int i=states.length-1;i>=0;i--){
				if (states[i] == null) continue;
				if (states[i].getScore() != 0){
					//if (states[i].getParent().getScore() == 0) System.out.println(printable(states[i].getParent()) + ": " + printable(states[i].getParent().next()));
					continue;
				}
				
				foundAll = false;
				
				//node exists, and doesn't have a score yet
				if (isWinner(states[i].get())){
					states[i].setScore(isComputerRed ^ isRedTurn(states[i].get()) ? 1 : -1);
					System.out.println(printable(states[i]) + ", " + (isComputerRed ^ isRedTurn(states[i].get())));
					//System.out.println("Winner: " + states[i].getLayer());
					continue; //we are a winner, move on now
				}
				int bestScore = 0;
				int childCount = 0;
				for (CSGraph.Node child : states[i].next()){
					//System.out.print(printable(states[i]) + "-" + printable(child) + ", ");
					if (child.getScore() == 0) continue; //child has nothing to offer to this parent
					if (child.getScore() < 0) System.out.println(printable(states[i]) + " -- " + printable(child));
					if (-child.getScore() >= bestScore){ //we are at or better than best
						if (-child.getScore() > bestScore) childCount=0; //new high score, reset child count
						bestScore = -child.getScore(); //new best score (0 sum game with alternating turns)
						states[i].setChild(child); //saves best child
						childCount++; //show we have multiple good children here
						//System.out.println(states[i].get() + ", " + bestScore + ", " + states[i].getLayer());
					}
				}
				//if (states[i].getScore() != 0 && bestScore == 0) System.out.println("reset alarm");
				states[i].setScore(bestScore);
			}
			System.out.println("\nPass " + count + " done\n");
			count++;
		}*/
	}
	public void printScores(){
		for (CSGraph.Node item : states){
			if (item == null) continue;
			System.out.print(String.format("%02d", item.getScore()) + ", ");
		}
		System.out.println();
	}
	public String printable(CSGraph.Node node){
		if (node == null) return "<null>";
		short val = node.get();
		return "<" + String.format("%03d", val) + " Layer: " + String.format("0x%X", node.getLayer()) + ", R:" + teamNums(getRedTeam(val)) + ", B:" + teamNums(getBlueTeam(val)) + (isRedTurn(val) ? " RT" : " BT") + " Score: " + String.format("%02d", node.getScore()) + ">";
	}
	public String printable(DLList<CSGraph.Node> list){
		String send = "";
		for (CSGraph.Node item : list){
			send += printable(item) + ", ";
		}
		return send;
	}
	public String teamNums(int val){
		return "<" + vals[val][0] + "," + vals[val][1] + ">";
	}
	
	public DLList<Short> nextValues(short num){
		DLList<Short> send = new DLList<Short>();
		
		if (isWinner(num)) return send; //there is a winner, don't do extra things
		
		boolean redTurn = isRedTurn(num);
		//System.out.println(num);
		//attack portion
		int[] attacker = vals[redTurn ? getRedTeam(num) : getBlueTeam(num)];
		int[] defender = vals[redTurn ? getBlueTeam(num) : getRedTeam(num)];
		//System.out.println("R:" + teamNums(getRedTeam(num)) + ", B:" + teamNums(getBlueTeam(num)) + (isRedTurn(num) ? " RT" : " BT") + " - ");
		//System.out.println("A:<" + attacker[0] + "," + attacker[1] + ">, D:<" + defender[0] + "," + defender[1] + ">");
		//small on small
		if (!(attacker[0] == 0 || defender[0] == 0)){
			//System.out.println("S:S");
			send.add(setTeamTurn(setRedTeam(setBlueTeam(0, redTurn ? find(attack(attacker[0], defender[0]), defender[1]) : find(attacker) ), redTurn ? find(attacker) : find(attack(attacker[0], defender[0]), defender[1]) ), !redTurn));
		}
		//small on large
		if (!(attacker[0] == 0 || defender[1] == 0)){
			//System.out.println("S:L");
			send.add(setTeamTurn(setRedTeam(setBlueTeam(0, redTurn ? find(defender[0], attack(attacker[0], defender[1])) : find(attacker) ), redTurn ? find(attacker) : find(defender[0], attack(attacker[0], defender[1])) ), !redTurn));
		}
		//large on small
		if (!(attacker[1] == 0 || defender[0] == 0)){
			//System.out.println("L:S");
			send.add(setTeamTurn(setRedTeam(setBlueTeam(0, redTurn ? find(attack(attacker[1], defender[0]), defender[1]) : find(attacker) ), redTurn ? find(attacker) : find(attack(attacker[1], defender[0]), defender[1]) ), !redTurn));
		}
		//large on large
		if (!(attacker[1] == 0 || defender[1] == 0)){
			//System.out.println("L:L");
			send.add(setTeamTurn(setRedTeam(setBlueTeam(0, redTurn ? find(defender[0], attack(attacker[1], defender[1])) : find(attacker) ), redTurn ? find(attacker) : find(defender[0], attack(attacker[1], defender[1])) ), !redTurn));
		}
		
		//swapping portion
		if (attacker[0] + attacker[1] >= sums.length) System.out.println(attacker[0] + ", " + attacker[1]);
		int[][] alts = sums[attacker[0] + attacker[1]];
		for (int[] alt : alts){
			short res = setTeamTurn(setRedTeam(setBlueTeam(0, redTurn ? find(defender) : find(alt)), redTurn ? find(alt) : find(defender)), !redTurn);
			if (res == setTeamTurn(num, !isRedTurn(num))) continue;
			send.add(res);
		}
		//System.out.println("Sending: " + send);
		return send;
	}
	
	public int attack(int attacker, int defender){
		return (attacker+defender >= 5) ? 0 : attacker + defender;
	}
	
	//find able methods
	public int find(int[] sequence){
		if (sequence[0] > sequence[1]){
			int temp = sequence[0];
			sequence[0] = sequence[1];
			sequence[1] = temp;
		}
		for (int i=0;i<vals.length;i++){
			if (vals[i][0] == sequence[0] && vals[i][1] == sequence[1]) return i;
		}
		System.out.println("Find Error, <" + sequence[0] + ", " + sequence[1] + ">");
		return -1;
	}
	public int find(int small, int large){
		return find(new int[] {small, large});
	}
	
	//win methods
	public boolean isWinner(int num){
		//returns true when one of the counts are 0
		if (getRedTeam(num) == 0 || getBlueTeam(num) == 0) return true;
		return false;
	}
	public boolean isRedWinner(int num){
		//returns true if there is a winner and the red team is the winner
		if (getRedTeam(num) == 0 && getBlueTeam(num)!= 0) return false; //red is dead, blue is alive
		if (getBlueTeam(num)== 0 && getRedTeam(num) != 0) return true;
		return false;
	}
	
	//bit manipulations
	public boolean isRedTurn(short num){
		return (num&0b1) == 1;
	}
	public int getRedTeam(int num){
		return (num & 0b0000_1111_0) >> 1;
	}
	public int getBlueTeam(int num){
		return (num & 0b1111_0000_0) >> 5;
	}
	public short setRedTeam(int prevNum, int redTeam){
		prevNum &= 0b1111_0000_1;
		prevNum |= redTeam << 1;
		return (short)prevNum;
	}
	public short setBlueTeam(int prevNum, int blueTeam){
		prevNum &= 0b0000_1111_1;
		prevNum |= blueTeam << 5;
		return (short)prevNum;
	}
	public short setTeamTurn(int prevNum, boolean isRedTurn){
		if (isRedTurn) return (short)(prevNum | 0b1);
		return (short)(prevNum & (~(0b1)));
	}
}