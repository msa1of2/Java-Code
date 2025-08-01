import java.util.Random;

public class Agent{
	private Matrix[] layers;
	private int matrixSize;
	private Random rand;
	
	public Agent(int layerCount, int matrixWidth, byte[] rawData){
		//how many layers of matrices there are, the width and height of each matrix and the data from the file
		this(layerCount, matrixWidth, rawData, 0, false);
	}
	public Agent(int layerCount, int matrixWidth, byte[] rawData, int startPos){
		//start pos is where in the raw data to start pulling values from
		this(layerCount, matrixWidth, rawData, startPos, false);
	}
	public Agent(int layerCount, int matrixWidth, byte[] rawData, int startPos, boolean applyRandomization){
		//set apply randomization to true if you want a normal based random number to be applied to the values
		
		if (layerCount <= 0) throw new IllegalArgumentException("The layers of the agent must be more than 0");
		if (matrixWidth <= 0) throw new IllegalArgumentException("The width of each matrix must be more than 0");
		if (rawData.length-startPos < matrixWidth * matrixWidth * layerCount * 8) throw new IllegalArgumentException("The amount of data given must be at least more than the amount required by all the matrixes. Given: " + (rawData.length-startPos) + ", Wanted: " + (matrixWidth * matrixWidth * layerCount * 8));
		if (startPos < 0 || startPos >= rawData.length-3) throw new IllegalArgumentException("The starting position of the data to read from must be between 0 and data length-3 inclusive");
		
		layers = new Matrix[layerCount];
		rand = new Random();
		matrixSize = matrixWidth;
		
		double[] matrixVals = new double[matrixWidth*matrixWidth]; //square matrices
		long tempData = 0;
		//System.out.println((layerCount-1)*matrixVals.length*8 + (matrixVals.length-1)*8 + 7);
		for (int i=0;i<layerCount;i++){
			
			for (int n=0;n<matrixVals.length;n++){
				
				for (int l=0;l<8;l++){
					tempData |= rawData[startPos + l + n*8 + i*matrixVals.length*8];
					tempData<<=8;
				}
				matrixVals[n] = Double.longBitsToDouble(tempData) + (applyRandomization ? rand.nextGaussian() : 0.0);
				tempData = 0;
			}
			layers[i] = new Matrix(matrixWidth, matrixWidth, matrixVals);
		}
	}
	private Agent(Matrix[] oldLayers, boolean randomize){
		if (rand == null) rand = new Random();
		this.layers = new Matrix[oldLayers.length];
		double[] vals;
		for (int i=0;i<layers.length;i++){
			vals = oldLayers[i].getAllData();
			if (!randomize){
				this.layers[i] = new Matrix(oldLayers[0].getRows(), oldLayers[0].getColumns(), vals);
				continue;
			}
			for (int n=0;n<vals.length;n++){
				vals[n] += rand.nextGaussian();
			}
			this.layers[i] = new Matrix(oldLayers[0].getRows(), oldLayers[0].getColumns(), vals);
		}
	}
	
	public int chooseAction(double[] vals){
		Matrix vec = new Matrix(layers[0].getColumns(), 1, vals);
		for (int i=0;i<layers.length;i++){
			vec = layers[i].dotProduct(vec);
		}
		
		
		double sum = 0;
		for (int i=0;i<vec.getRows();i++){
			sum += vec.get(i, 0);
		}
		double randVal = rand.nextDouble() * sum;
		sum = 0;
		for (int i=0;i<vec.getRows();i++){
			if (randVal > sum && randVal <= sum+vec.get(i,0)) return i;
			sum += vec.get(i, 0);
		}
		
		return -1;
	}
	
	public byte[] pack(){
		//returns the raw bytes used as values by this agent
		//double[] vals = new double[layers[0].getRows() * layers[0].getColumns()];
		long tempVal;
		double[] vals = new double[layers[0].getColumns() * layers[0].getRows()];
		byte[] send = new byte[layers.length * vals.length * 8];
		for (int i=0;i<layers.length;i++){ //every matrix
			vals = layers[i].getAllData();
			for (int n=0;n<vals.length;n++){ //every value
				tempVal = Double.doubleToLongBits(vals[i]);
				
				for (int l=0;l<8;l++){ //every byte from the values
					send[l + n*8 + i*layers.length*8] = (byte)(tempVal & 0xff);
					tempVal >>= 8;
				}
				
			}
		}
		
		return send;
	}
	
	public Agent dupe(){
		return new Agent(layers, true);
	}
	
	public static Agent generateRandomAgent(int layerCount, int matrixSize){
		byte[] raw = new byte[matrixSize*matrixSize*layerCount];
		return new Agent(layerCount, matrixSize, raw, 0, true);
	}
}