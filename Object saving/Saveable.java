import java.io.Serializable;
public class Saveable implements Serializable{ //whatever object to be saved must be Serializable
	private String info;
	public Saveable(String data){
		this.info = data;
	}
	public String get(){
		return this.info;
	}
	public void set(String data){
		this.info = data;
	}
}