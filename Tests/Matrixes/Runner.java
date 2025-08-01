class Runner{
	public static void main(String[] args){
		Matrix test2 = new Matrix(5,1, new double[] {1,1,1,1,2});
		Matrix test = new Matrix(6,5);
		//Matrix test = new Matrix(3,2,new double[] {7,8,9,10,11,12});
		//Matrix test2 = new Matrix(2,3,new double[] {1,2,3,4,5,6});
		
		for (int i=0;i<test.getRows();i++){
			for (int j=0;j<test.getColumns();j++){
				test.set(i, j, i);
			}
		}
		
		//test2.transpose();
		System.out.println(test + ",\n" + test2 + "\n");
		Matrix test3 = test.dotProduct(test2);
		System.out.println(test3);
	}
}