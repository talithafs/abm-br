package abm.helpers;

public final class Messages {
	
	private Messages() { } 
	
	public static final class Errors {
		
		public static final String FILE_NOT_FOUND = "File was not found.";
		public static final String MALFORMED_MAP = "Parameters' hash map is not valid.";
		public static final String INVALID_PARAM = "One or more parameters are not valid:";
	}
	
	public static final class Warnings {
		
		public static final String RESETING_PARAM = "Trying to reset an already initalized parameter:" ;
	}
	
	public static final class Infos {
		
	}

}
