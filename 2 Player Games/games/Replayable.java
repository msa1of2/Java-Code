package games;
public interface Replayable extends Game{
	//Progresses the replayed file by 1, does not need to change anything in the inputs, cursor is absolute - includes name and date in its count
	public String goForwards(int cursor, String[] moves) throws NumberFormatException;
	
	//Reverses the last move in the replay file, does not need to change anything in the inputs, cursor is absolute - includes name and date in its count
	public String goBack(int cursor, String[] moves) throws NumberFormatException;
	
	//returns true if there are more moves in the file to play, cursor is absolute - includes name and date in its count
	public boolean canGoForward(int cursor, String[] moves);
	
	//Returns true if there is a move to go back to, cursor is absolute - includes name and date in its count
	public boolean canGoBack(int cursor, String[] moves);
	
	//Returns the number to display to the user based on where the cursor is, used when games replay file starts with extra information
	default int displayNum(int cursor){
		return cursor-2;
	}
	
	//Returns true if there are controls the game reads / acts on that are required to view the entire game during the replay session
	default boolean canTouchBoard(){
		return false;
	}
	
	//initalizes the game to a board the user had when they originally plays it, Used when starting the replay file
	//Returns the index the cursor should start its reading, index 0 is the first move after Name, and Date
	default int initalize(String[] moves){
		this.reset();
		return 1;
	}
}