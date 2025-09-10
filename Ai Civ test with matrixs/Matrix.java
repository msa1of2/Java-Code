class Matrix{
	private int rows, cols;
	private double[] data;
	
	public Matrix(int rows, int cols){
		this.rows = rows;
		this.cols = cols;
		this.data = new double[rows*cols];
	}
	public Matrix(int rows, int cols, double[] data){
		this.rows = rows;
		this.cols = cols;
		if (data.length != rows*cols){
			double[] temp = new double[rows*cols];
			for (int i =0;i<rows*cols;i++){
				double value = 0;
				try {
					value = data[i];
				}
				catch (ArrayIndexOutOfBoundsException e){
					
				}
				temp[i] = value;
			}
			data = temp;
		}
		this.data = data;
	}
	public int getRows(){
		return rows;
	}
	public int getColumns(){
		return cols;
	}
	public double get(int row, int col){
		if (row >= rows || row < 0 || col >= cols || col < 0){
			return -1; //out of bounds
		}
		
		return data[row*cols + col];
	}
	public void set(int row, int col, double info){
		if (row >= rows || row < 0 || col >= cols || col < 0){
			return; //out of bounds
		}
		data[row*cols+col] = info;
	}
	public void add(Matrix other){
		if (other.getRows() != this.rows || other.getColumns() != this.cols){
			return; //sizes do not match
		}
		//sizes are now the same
		for (int r=0;r<rows;r++){
			for (int c=0;c<cols;c++){
				this.set(r, c, this.get(r,c) + other.get(r,c));
			}
		}
	}
	public void scalar(double value){
		//sizes are now the same
		for (int r=0;r<rows;r++){
			for (int c=0;c<cols;c++){
				this.set(r, c, this.get(r,c) * value);
			}
		}
	}
	public void subtract(Matrix other){
		other.scalar(-1);
		this.add(other);
	}
	public void transpose(){
		double[] temp = new double[rows*cols];
		for (int c=0;c<cols;c++){
			for (int r=0;r<rows;r++){
				temp[c*rows+r] = this.get(r,c);
			}
		}
		data = temp;
		int tempInt = rows;
		rows = cols;
		cols = tempInt;
	}
	public void dotProduct(Matrix other){
		if (other.getRows() != this.cols){
			return; //sizes do not match
		}
		//This = left matrix, other = right matrix
		double[] temp = new double[other.getColumns() * this.getRows()];
		for (int r=0;r<this.getRows();r++){
			for (int c=0;c<other.getColumns();c++){
				int sum = 0;
				for (int i=0;i<this.getColumns();i++){
					sum += other.get(i,c) * this.get(r,i);
					//System.out.print(other.get(i,c)+ ", " + this.get(r,i));
				}
				temp[r*other.getColumns()+c] = sum;
			}
		}
		this.data = temp;
		cols = other.getColumns();
	}
	public String toString(){
		String send = "[";
		for (int i=0;i<data.length;i++){
			if (i%cols == 0 && i != 0){
				send += "\n";
			}
			send += data[i];
			if (i != data.length-1){
				send += ", ";
			}
			
		}
		return send + "]";
	}
	public String filePack(){
        String send= "";
        for (int c=0;c<data.length;c++){
            send += data[c];
        }
        send+=":";
        return send;
    }
	public void mutate(int size){
		for (int i=0;i<data.length;i++){
			for (int n=0;n<size;n++){
				data[i] = data[i]+(Math.random()*2)-1;
			}
		}
	}
	
	/*public static final Matrix dotProductTestThing(Matrix other){
		if (other.getRows() != this.rows || other.getColumns() != this.cols){
			return null; //sizes do not match
		}
		//sizes are now the same
		Matrix send = new Matrix(rows, cols)
		for (int r=0;r<rows;r++){
			for (int c=0;c<cols;c++){
				send.set(r, c, this.get(r,c) * other.get(r,c));
			}
		}
		return send;
	}*/
}