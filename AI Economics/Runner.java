//File reading / writing
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Runner{
	
	private File aIVals;
	private FileInputStream reader;
	private FileOutputStream writer;
	
	private Agent[][] agents;
	private int[][] agentValues;
	private double[] production;
	private int width, layers;
	
	private static int TOTAL = 500;
	
	public static void main(String[] args){
		new Runner();
	}
	
	public Runner(){
		aIVals = new File("AIVals.dat");
		
		/*agents = new Agent[10][10];
		agentValues = new int[10][10];
		byte[] emptiness = new byte[10*10*10*8];
		for (int x=0;x<agents.length;x++){
			for (int y=0;y<agents.length;y++){
				agents[x][y] = new Agent(10, 10, emptiness);
			}
		}
		writeMatrices();*/
		
		
		System.out.println(runRound(10, 10, 10));
	}
	
	public int runRound(int width, int layers, int dupeAgents){
		setProduction(width);
		readMatrices(width, layers, dupeAgents);
		for (int i=0;i<10;i++){
			getChoices(); //have the AIs announce what they'll produce for 10 turns
			printValues();
		}
		int sum = 0; //calculate total amount produced by every one
		for (int i=0;i<agentValues.length;i++){
			sum += Math.abs(agentValues[i][0]);
		}
		System.out.println(sum);
		
		double mult = -2.202381 * sum +660.11905;
		for (int x=0;x<agentValues.length;x++){
			for (int y=0;y<agentValues[0].length;y++){
				agentValues[x][y] = (int)(mult * production[Math.max(0, Math.min(production.length, agentValues[x][y]))]);
			}
		}
		
		return writeMatrices();
	}
	public void printValues(){
		System.out.println("\n\n");
		for (int x=0;x<agentValues.length;x++){
			for (int y=0;y<agentValues[0].length;y++){
				System.out.print(agentValues[x][y] + ", ");
			}
			System.out.println();
		}
	}
	
	public void getChoices(){
		//int[][] nextChoices = new int[agentValues.length][agentValues[0].length];
		double[] prevVals = new double[agentValues.length];
		for (int i=0;i<agentValues.length;i++){
			prevVals[i] = (double)(agentValues[i][0] == 0 ? 1 : agentValues[i][0]);
		}
		
		for (int y=0;y<agentValues.length;y++){
			for (int x=0;x<agentValues[0].length;x++){
				agentValues[x][y] = agents[x][y].chooseAction(prevVals);
			}
		}
	}
	
	public void setProduction(int agentNum){
		if (production != null) return;
		production = new double[agentNum];
		double small = TOTAL / production.length;
		for (int i=0;i<production.length;i++){
			production[i] = small*i;
		}
	}
	
	public void readMatrices(int width, int layers, int dupeAgents){
		byte[] agentVals = new byte[width*width*layers*8];
		this.width = width;
		this.layers = layers;
		agents = new Agent[width][dupeAgents];
		agentValues = new int[width][dupeAgents];
		try{
			reader = new FileInputStream(aIVals);
		}
		catch (IOException e){
			e.printStackTrace();
			return;
		}
		for (int i=0;i<agents.length;i++){
			try{
				reader.read(agentVals);
				agents[i][0] = new Agent(layers, width, agentVals);
			}
			catch(IOException e){
				e.printStackTrace();
				agents[i][0] = new Agent(layers, width, agentVals, 0, true);
			}
			for (int n=1;n<agents[i].length;n++){
				agents[i][n] = agents[i][0].dupe();
			}
			
		}
	}
	public int writeMatrices(){ //returns the best score
		
		try{
			writer = new FileOutputStream(aIVals);
		}
		catch (IOException e){
			e.printStackTrace();
			return -1;
		}
		byte[] line = new byte[width*width*layers];
		int absBest = agentValues[0][0];
		try{
			
			for (int i=0;i<agents.length;i++){
				int bestScore = agentValues[i][0];
				int bestIndex = 0;
				for (int n=0;n<agents[0].length;n++){
					if (agentValues[i][n] > absBest) absBest = agentValues[i][n];
					if (agentValues[i][n] <= bestScore) continue;
					bestScore = agentValues[i][n];
					bestIndex = n;
				}
				
				line = agents[i][bestIndex].pack();
				
				//actually write to the file
				writer.write(line);
			}
		}
		catch (IOException e){
			e.printStackTrace();
			return -1;
		}
		return absBest;
	}
}