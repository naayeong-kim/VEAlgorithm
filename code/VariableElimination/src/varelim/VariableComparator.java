package varelim;

import java.util.Comparator;

public class VariableComparator implements Comparator <Variable>{

	@Override
	public int compare(Variable o1, Variable o2) {
		if (o1.getNrOfParents() < o2.getNrOfParents())
			return -1 ;
		else if (o1.getNrOfParents() > o2.getNrOfParents())
			return 1 ;
		else
			return 0;
	}

}