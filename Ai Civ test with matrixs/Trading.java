class Trader{
    private int[] numberOfNodes;
    private Matrix[] matrixs;
    private int currentPrice, currentVolume, thresholdOfSale, thresholdOfBuy, currentMoney;
    public Trader(String[] rawInfo){
        String[] info = rawInfo[0].split(";");
        numberOfNodes = new int[info.length];
        matrixs = new Matrix[info.length-2];
        int currentSize = 1;
        for(int i = 2; i < info.length; i++){
            numberOfNodes[i-2] = Integer.parseInt(info[i]);
            int previousNum = 5; //set to number of values passed in at the beginning
            if (i!=2){
                previousNum = numberOfNodes[i-2-1];
            }
            String[] data = rawInfo[currentSize].split(";");
            double[] nums = new double[data.length];
            for (int j=0;j<data.length;j++){
                nums[j] = Double.parseDouble(data[j]);
            }
            matrixs[i-2] = new Matrix(previousNum, numberOfNodes[i-2], nums);
            currentSize ++; //increase the current number of layers passed

            //trading values stores:
            //currentPrice, currentVolume, thresholdOfSale, thresholdOfBuy, currentMoney
            //Output:
            //percentageOfVolumeToAffect, shouldSell
            //shouldSell is bewteen -1, and 1 (positive is selling), PercentageOfVolumeToAffect is between 0 and 1
        }
		
		for (int i=0;i<info.length;i++){
			System.out.println(info[i]);
		}
		for (int i=0;i<matrixs.length;i++){
			System.out.println(matrixs[i]);
		}
    }
    public void trade(int price){
        currentPrice = price;
        Matrix start = new Matrix(1, 5, new double[]{price, currentVolume, thresholdOfSale, thresholdOfBuy, currentMoney});
        for (int i=0;i<matrixs.length;i++){
            start.dotProduct(matrixs[i]);
        }
    }
	public void mutate(){
		for (Matrix matrix : matrixs){
			matrix.mutate(10);
		}
	}
    public String filePack(){
        String send = "", maxtrixSend = "";
        send += thresholdOfSale + ";" + thresholdOfBuy;
        for (int i=0;i<matrixs.length;i++){
            send += ";" + matrixs[i].getColumns();
            matrixSend += matrixs[i].filePack();
        }
        
        return send+":"+matrixSend;
    }
}