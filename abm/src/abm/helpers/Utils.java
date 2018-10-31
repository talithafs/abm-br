package abm.helpers;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import abm.agents.Agent;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.SimUtilities;
import repast.simphony.util.collections.IndexedIterable;

public final class Utils {
	
	public static HashMap<String,Double> readParameters() {
		
		HashMap<String, Double> hm = null ;
		boolean isValid = true ;

        try {
            
        	Reader reader = Files.newBufferedReader(Paths.get(Constants.Paths.PARAMS_FILE));
        	CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
        	
        	String[] nextRecord;
            hm = new HashMap<String, Double>();
            
            while ((nextRecord = csvReader.readNext()) != null) {
                hm.put(nextRecord[0], Double.parseDouble(nextRecord[1]));
            }  
        } 
        catch (IOException name) {
        	System.out.println(Messages.Errors.FILE_NOT_FOUND);
        }
        
        if(hm != null) {
        	// validate
        	
        	if(!isValid) {
        		System.out.println(Messages.Errors.MALFORMED_MAP);
        	}
        }

        return hm ;
    }
	
	public static ArrayList<Integer[]> readDistribution(String type){
		
		ArrayList<Integer[]> arr = null ;
		boolean isValid = true ;
		String file = null ;
		
		if(type.equals("fir")) {
			file = Constants.Paths.DIST_FIR_FILE ;
		}
		else if(type.equals("inc")) {
			file = Constants.Paths.DIST_INC_FILE ;
		}
		else {
			System.out.println(Messages.Errors.INVALID_PARAM + " type.");
		}
		
		  try {
			  
			  Reader reader = Files.newBufferedReader(Paths.get(file));
			  CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
		  
			  String[] nextRecord;
			  arr = new ArrayList<Integer[]>();
	             
			  while ((nextRecord = csvReader.readNext()) != null) {
                
                Integer[] entry = null ;
                
                if(type != "inc") {
                	 entry = new Integer[3] ;
                     entry[0] = Integer.parseInt(nextRecord[0]) ;
                     entry[1] = Integer.parseInt(nextRecord[1]) ;
                     
                     if(nextRecord[2].equals("c")) {
                    	 entry[2] = 0 ;
                     } 
                     else if(nextRecord[2].equals("k")) {
                    	 entry[2] = 1 ;
                     }
                     else {
                    	 // message  
                     }
                     
                }
                else {
                	entry = new Integer[4] ;
                	entry[0] = Integer.parseInt(nextRecord[1]) ;
                    entry[1] = Integer.parseInt(nextRecord[2]) ;
                    entry[2] = Integer.parseInt(nextRecord[3]) ;
                    entry[3] = Integer.parseInt(nextRecord[4]) ;
                }
                
                arr.add(entry);
			  }  
	        } 
	        catch (IOException name) {
	        	System.out.println(Messages.Errors.FILE_NOT_FOUND);
	        }
		  
		  if(arr != null) {
	        	// validate
	        	
	        	if(!isValid) {
	        		System.out.println(Messages.Errors.MALFORMED_MAP);
	        	}
	        }
		
		return arr ;
	}
	
	public static String getAgentDescriptor(String[][] fields, boolean showId) {
		
		StringBuilder result = new StringBuilder();
	    String newLine = System.getProperty("line.separator");
	    int i = 0 ;
		
		if(showId) {
			result.append("-- Agent " + fields[0][1] + " --");
		    result.append(newLine);
		    i = 1 ;
		}
		
		return getDescriptor(result, fields, i, newLine).toString();
		
	}
	
	public static String getLinkDescriptor(String[][] fields, String name, Agent a, Agent b) {
		
		StringBuilder result = new StringBuilder();
	    String newLine = System.getProperty("line.separator");
		
		result.append("-- " + name + " between " + a.getId() +  " and " + b.getId() + " --" );
	    result.append(newLine);

	    return getDescriptor(result, fields, 0, newLine).toString();
	}
	
	public static String getComponentDescriptor(String[][] fields, boolean showId) {
		
		StringBuilder result = new StringBuilder();
	    String newLine = System.getProperty("line.separator");
	    int i = 0 ;
	   
		
	    if(showId) {
			result.append("-- Component " + fields[0][1] + " --");
		    result.append(newLine);
		    i = 1 ;
		}
		
		return getDescriptor(result, fields, i, newLine).toString();
	}
	
	private static StringBuilder getDescriptor(StringBuilder builder, String[][] fields, int i, String newLine) {
		
		while(i < fields.length) {
			builder.append(fields[i][0] + " : " + fields[i][1]);
			builder.append(newLine);
			i++ ;
		}
		
		builder.append("-");
		builder.append(newLine);
		
		return builder ;
	}
	
	public static <T> ArrayList<T> shuffle(IndexedIterable<T> agents){
		
		ArrayList<T> shuffled = new ArrayList<T>();
		Iterator<T> itr = agents.iterator() ;
	
		while(itr.hasNext()) {
			shuffled.add(itr.next());
		}
		
		SimUtilities.shuffle(shuffled, RandomHelper.getUniform());
		
		return shuffled ;
	}
}
