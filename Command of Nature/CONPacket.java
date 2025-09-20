import helpful.datastructures.DLList;

public class CONPacket extends helpful.connection.Packet{
	
	private DLList<Byte> conData;
	private Stage stage;
	private int turn;
	
	public CONPacket(Stage stage, int turn){
		conData = new DLList<Byte>();
		this.count = HEADERSIZE;
		this.data[count++] = stage.toByte();
		this.data[count++] = (byte)turn;
	}
	
	
	public byte[] send(InetAddress reciever, int port, int sendersPort, boolean... booleanValues){
		this.data = super.send(reciever, port, sendersPort, 0, booleanValues);
		setSizeHeader(conData.size());
		for (Byte val : conData){
			this.data[count++] = val.byteValue();
		}
		return this.data;
	}
	
	public boolean add(byte[] addition){
		if (conData == null) return false;
		conData.add(addition);
		return true;
	}
	public boolean add(byte addition){
		if (conData == null) return false;
		return conData.add(addition);;
	}
	
	public void init(){
		this.stage = Stage.byteToStage(data[HEADERSIZE]);
		this.turn = (int)data[HEADERSIZE+1];
	}
	
	public static CONPacket read(InputStream stream){
		CONPacket out = (CONPacket)super.read(stream);
		out.init(); //initalises the easy to read variables from the data passed in
		return out;
	}
	
	public enum Stage{
		INFO,
		RULES,
		DAYBREAK,
		ATTACK,
		SHOP,
		RESET;
		
		public byte toByte(){
			return stageToByte(this);
		}
		public static byte stageToByte(Stage stage){
			for (int i=0;i<Stage.values().length;i++){
				if (stage == Stage.values()[i]) return i;
			}
			return -1;
		}
		public static Stage byteToStage(byte num){
			if (num < 0) throw new IllegalArgumentException("Stage Index must be positive");
			return Stage.values()[num];
		}
	}
	public enum Error{
		None,          //Everything is fine and dandy
		WrongValue,    //Used for wrong stage, turn or response
		NoFunds,       //No money to buy things
		NoAP,          //No action points to do moves
		NoFactionAP,   //No Faction specific action points to spend
		NoCardFound,   //Used when index for a card is empty, or a global card look up doesnt exist
		CardActFailed, //When the card activation requirements are not met but the client still wants to activate it
		HandSyncFail;  //When the server and client lose the sync over the client's hand
		
		public byte toByte(){
			return errorToByte(this);
		}
		public static byte errorToByte(Error error){
			for (int i=0;i<Error.values().length;i++){
				if (error == Error.vlaues()[i]) return i;
			}
			return -1;
		}
		public static Error byteToError(byte num){
			if (num < 0) throw new IllegalArgumentException("Error Index must be positive");
			return Error.values()[num];
		}
	}
}