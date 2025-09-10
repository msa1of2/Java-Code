class Trader{
	private Matrix[] matrixs;
	private int[] numberOfNodes;
	private long money, volume;
	public Trader(String[] rawInfo, long startingMoney){
		String[] info = rawInfo[0].split(";");
		matrixs = new Matrix[info.length];
		numberOfNodes = new int[info.length];
		for(int i = 0; i < info.length; i++){
            numberOfNodes[i] = Integer.parseInt(info[i]);
            int previousNum = 5; //set to number of values passed in at the beginning
            if (i!=0){
                previousNum = numberOfNodes[i-1];
            }
            String[] data = rawInfo[i+1].split(";");
            double[] nums = new double[data.length];
            for (int j=0;j<data.length;j++){
                nums[j] = Double.parseDouble(data[j]);
            }
            matrixs[i] = new Matrix(previousNum, numberOfNodes[i], nums);
			//System.out.println(matrixs.length + ", " + matrixs[i].getRows() + ", " + matrixs[i].getColumns() + ", " + matrixs[i].get(4,4));

            //trading values stores:
            //currentPrice, currentVolume, currentMoney
            //Output:
            //amountOfVolumeToAffect (1-1000), shouldSell
            //shouldSell is bewteen -1, and 1 (positive is selling), PercentageOfVolumeToAffect is between 0 and 1
        }
		money=startingMoney;
		volume = 0L;
		//System.out.println("\n" + matrixs[0]);
	}
	public void trade(int price, int lastPrice, int lastLastPrice){
		Matrix temp = new Matrix(1, 5, new double[] {price, volume, money, lastPrice, lastLastPrice});
		for (int i=0;i<matrixs.length;i++){
			temp.dotProduct(matrixs[i]);
			if (i != matrixs.length-1){
				//not the last matrix multiplcation
				for (int r=0;r<temp.getRows();r++){
					for (int c=0;c<temp.getColumns();c++){
						temp.set(r,c, normalize(temp.get(r,c)));
					}
				}
			}
			else{
				//the last matrix multiplcation
				//System.out.println(temp);
				temp.set(0, 0, normalize(temp.get(0,0)/10)*1000);
				temp.set(0, 1, normalize(temp.get(0,1)));
				//System.out.println(temp + "after");
			}
		}
		//temp now holds the values of volumeToAffect and shoudSell in 0,0 and 0,1 respectively
		//System.out.println(temp);
		
		if (temp.get(0,1) >0){
			//selling items
			int selling = Math.max(0, (int)Math.min(temp.get(0,0), (double)volume));
			
			//keep selling smaller than or equal to the amount that can be sold
			volume-=selling;
			money += selling * price;
		}
		else if (temp.get(0,1) < 0){
			//buying items
			//System.out.println(volume + ", " + money + ", " + volume / money)
			int buying = Math.max(0, (int)(Math.min(temp.get(0,0)*price, (double)(volume*price))/price)); //double check the vol/mon code
			//System.out.println("Buy " + buying  + ", at " + price);
			//Keep buying smaller than or equal to the amount that can be bought given this amount of money
			volume += buying;
			money -= buying * price;
		}
	}
	private double normalize(double input){
		return Math.atan(input)*2/Math.PI;
	}
	public long getScore(){
		return money + (long)(volume*0.1); //money plays a much bigger role in deciding if its a good bot than volume
	}
	public void mutate(int size){
		for (Matrix matrix : matrixs){
			matrix.mutate(size);
		}
	}
	public String filePack(){
		String before = "", send = "";
		for (int i = 0;i<matrixs.length;i++){
			before += matrixs[i].getColumns() + ";";
			send += matrixs[i].filePack();
		}
		return before + ":" + send;
	}
	public long getVolume(){
		return volume;
	}
	public long getMoney(){
		return money;
	}
}