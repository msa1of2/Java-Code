package JarCreationtest;
class Runner{
	public static void main(String[] args){
		System.out.println("Hellow random people!");
		try{
			Thread.sleep(5000);
		}
		catch(InterruptedException ex){
			Thread.currentThread().interrupt();
		}
	}
}