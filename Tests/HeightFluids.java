import java.awt.Graphics;
import java.awt.Color;

public class HeightFluids{
	public int[][] cells; //the actual cells
	public int[][][] vecs; //debug info
	public static final int[] dirs = new int[]{1,0,-1,0}; //all the possible directions to look when updating
	public HeightFluids(int xSize, int ySize){
		cells = new int[xSize][ySize];
		vecs = new int[xSize][ySize][2];
		
		/*int[] test = new int[2];
		for (int i=0;i<dirs.length;i++){
			getDirs(test, i);
			System.out.println(test[0] + ", " + test[1]);
		}*/
	}
	
	public void draw(Graphics g, int xStart, int yStart, int xSize, int ySize){
		int xCellSize = xSize / cells.length;
		int yCellSize = ySize / cells[0].length;
		int maxHeight = 0;
		for (int x=0;x<cells.length;x++){
			for (int y=0;y<cells[0].length;y++){
				if (cells[x][y] > maxHeight) maxHeight = cells[x][y];
			}
		}
		
		for (int x=0;x<cells.length;x++){
			for (int y=0;y<cells[0].length;y++){
				//System.out.println((cells[x][y] + ", " + (float)maxHeight));
				g.setColor(new Color(0f, 0f, Math.max(0, cells[x][y]) / (float)maxHeight));
				g.fillRect(x*xCellSize + xStart, y*yCellSize + yStart, xCellSize, yCellSize);
				g.setColor(Color.white);
				g.drawString(vecs[x][y][0] + ", " + vecs[x][y][1] + " - " + cells[x][y], x*xCellSize + xStart, y*yCellSize + yStart + g.getFontMetrics().getHeight());
			}
		}
		
		
	}
	
	public void add(int x, int y, int value){
		cells[x][y] = value;
	}
	
	public void update(){
		System.out.println("\n\n\n");
		int xVec=0, yVec=0;
		int[] dir=new int[2]; //where the water is going to move
		int[][] newcells = new int[cells.length][cells[0].length]; //replacement list for the next tick
		int diff, xDiff, yDiff;
		for (int x=0;x<cells.length;x++){
			for (int y=0;y<cells[0].length;y++){
				//reset the x/y vectors before messing with the next cell
				xVec = 0;
				yVec = 0;
				
				for (int i=0;i<dirs.length;i++){
					getDirs(dir, i); //convert the index into an x/y differential
					System.out.print(" Dirs " + i + ": " + (x+dir[0]) + ", " + (y+dir[1]));
					if (!inBounds(x + dir[0], y + dir[1])) continue; //ensure the cell we're looking at exists
					
					xVec += (cells[x+dir[0]][y+dir[1]] - cells[x][y] ) * dir[0];
					yVec += (cells[x+dir[0]][y+dir[1]] - cells[x][y] ) * dir[1];
					
					
				}
				if (!inBounds(x + signPosZero(xVec), y + signPosZero(yVec))) {System.out.println(" X " + x + ", Y " + y);continue;} //if where it wants to go is out of the map, dont bother
				
				//if (sign(xVec) < 0) x-=1;
				//if (sign(yVec) < 0 && y > 0) y-=1;
				
				xDiff = Math.max(0, (int)((cells[x][y] - cells[x+signPosZero(xVec)][y])/2 * (xVec == 0 ? 0.5 : Math.abs(xVec / (double)(Math.abs(xVec)+Math.abs(yVec))))) );
				yDiff = Math.max(0, (int)((cells[x][y] - cells[x][y+signPosZero(yVec)])/2 * (yVec == 0 ? 0.5 : Math.abs(yVec / (double)(Math.abs(xVec)+Math.abs(yVec))))) );
				//if (x == 2 && y == 2)System.out.println("MAGIC: " + xVec + ", " + yVec + ", " + xDiff + ", " + yDiff + ", " + ((cells[x][y] - cells[x+xVec==0?1:sign(xVec)][y])/2) + ", " + ((cells[x][y] - cells[x][y+yVec==0?1:sign(yVec)])/2));
				//if (xVec == 0 && yVec == 0) System.out.println("DIFFS: " + xDiff + ", " + yDiff);
				System.out.println(" X " + x + ", Y " + y + " Diffs " + xDiff + ", "+ yDiff + " Cells " + cells[x][y] + ", " + cells[x+signPosZero(xVec)][y] + ", " + cells[x][y+signPosZero(yVec)] + " Vecs " + xVec + ", " + yVec);
				if (xVec == 0){
					if (inBounds(x+1, y)) {newcells[x+1][y]+=xDiff;newcells[x][y]-=xDiff;}
					if (inBounds(x-1, y)) {newcells[x-1][y]+=xDiff;newcells[x][y]-=xDiff;}
				}
				else {newcells[x+signPosZero(xVec)][y] += xDiff; newcells[x][y] -= xDiff;}
				if (yVec == 0){
					if (inBounds(x, y+1)) {newcells[x][y+1]+=yDiff;newcells[x][y]-=yDiff;}
					if (inBounds(x, y-1)) {newcells[x][y-1]+=yDiff;newcells[x][y]-=yDiff;}
				}
				else{ newcells[x][y+signPosZero(yVec)] += yDiff; newcells[x][y] -= yDiff;}
				//diff = (cells[x][y] - cells[x + sign(xVec)][y + sign(yVec)])/2;
				//cells[x + sign(xVec)][y + sign(yVec)] += diff;
				//cells[x][y] -= diff;
				newcells[x][y] += cells[x][y];
				vecs[x][y] = new int[] {xDiff, yDiff};
			}
		}
		cells = newcells;
	}
	
	private boolean inBounds(int x, int y){ //returns true if the values are within the bounds of the simulation
		return x >= 0 && y >= 0 && x < cells.length && y < cells[0].length;
	}
	
	public void getDirs(int[] dir, int dirIndex){ //converts index into a 2-axis diraction
		dir[1] = dirs[(dirIndex + 2) % dirs.length];
		dir[0] = dirs[(dirIndex + 1) % dirs.length];
	}
	public int sign(int value){ //returns the sign of the number (1, -1, 0)
		return value < 0 ? -1 : (value > 0 ? 1 : 0);
	}
	public int signPosZero(int value){ //returns 1 when if its 0 
		return value < 0 ? -1 : 1;
	}
}