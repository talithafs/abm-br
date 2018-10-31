package abm.creators;

import java.util.ArrayList;


public interface AgentsCreator<T> {

	public ArrayList<T> create(ArrayList<Integer[]> distribution);

}
