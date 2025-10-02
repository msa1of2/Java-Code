

public class CommandOfNature{
	private int playerCount;
	private final boolean isServer;
	private Stage currentStage;
	private ConnectionHandler cH;
	
	public CommandOfNature(ConnectionHandler client, boolean isServer){
		
		this.isServer = isServer;
		this.cH = client;
		this.currentStage = Stage.CONNECTION;
	}
	
	public void begin(int playerCount){
		this.playerCount = playerCount;
	}
	
	
	public enum Stage{
		CONNECTION, //Empty slot when the game hasnt started yet
		DAYBREAK,
		COMMAND,
		SHOP,
		RESET
	}
}