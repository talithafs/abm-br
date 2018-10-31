package abm.helpers;

public class Logger {

	private static Logger instance = new Logger();
	
	// class cannot be instantiated
	private Logger(){}

	//Get the only object available
	public static Logger getInstance(){
		return instance;
	}

	public boolean log(String line){
		System.out.println(line);
		return true ;
	}

}
