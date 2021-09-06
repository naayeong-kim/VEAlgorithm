package varelim;

import java.util.ArrayList;

public class Factor {
	private ArrayList<Variable> variables;
	private ArrayList<ProbRow> table;

	public Factor(ArrayList<Variable> variables, ArrayList<ProbRow> table) {
		this.variables = variables;
		this.table = table;
	}

	public Factor(Variable var, ArrayList<ProbRow> table) {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		variables.add(var);
		if (var.getNrOfParents() != 0)
			variables.addAll(var.getParents());
		this.variables = variables;
		this.table = table;
	}

	public ProbRow get(int i) {
		return table.get(i);
	}

	public ArrayList<Variable> getVariables() {
		return variables;
	}

	public ArrayList<ProbRow> getTable() {
		return table;
	}

	public boolean contains(Variable var) {
		for (Variable v : this.variables)
			if (v.equals(var))
				return true;

		return false;
	}

	// Factor Marginalization
	public Factor marginalization(Variable v) {
		ArrayList<Variable> newVariables = new ArrayList<Variable>();
		ArrayList<ProbRow> newRows = new ArrayList<ProbRow>(); // list of rows without V
		int deletedVar = -1;

		// For the case1: when the factor has only one variable
		
		if (variables.size() == 1) {
			ArrayList<String> newValues = new ArrayList<String>();
			newRows.add(new ProbRow(newValues, sumAllProb()));
			
			return new Factor(variables, newRows);
		}

		// For the case2: there are more than one variable in the factor
		
		// 1. check if the factor contains the variable given
		for (int i = 0; i < variables.size(); i++) {
			// if so, put the index of variable we need to delete into deletedVar
			if (variables.get(i).equals(v))
				deletedVar = i;
		}

		// 2. compare each two rows in factor 
		// make integer list to check if this index's values was visited
		ArrayList<Integer> checkedIdx = new ArrayList<Integer>(); 
		for (int i = 0; i < table.size(); i++) {
			if (!checkedIdx.contains(i)) {
				// if it is not visited yet, add the index number to list
				checkedIdx.add(i); 
				Double prob = table.get(i).getProb();
				
				for (int j = i + 1; j < table.size(); j++) {
					// compare two rows' values are same
					if ( canSum(table.get(i), table.get(j), deletedVar) ) {
						// if so, add the index number of another row also to the list for avoiding row duplicated
						// and calculate new probability by adding their probabilities.
						checkedIdx.add(j); 
						prob += table.get(j).getProb();
					}
				}

				// make a new value list containing all values in factor without the value in deletedVar
				ArrayList<String> newValues = new ArrayList<String>();
				newValues = makeNewValues(table.get(i), deletedVar);
				
				// add a new row with the list of new values and the probability we calculated
				newRows.add(new ProbRow(newValues, prob));
			}
		}

		// 3. delete the variable given in the list of variables of factor
		for (int i = 0; i < variables.size(); i++)
			if (i != deletedVar)
				newVariables.add(variables.get(i));

		return new Factor(newVariables, newRows);
	}

	/**
	 * Sums the probabilities of all rows
	 */
	public double sumAllProb() {
		double prob = 0;
		for (ProbRow p : table)
			prob += p.getProb();
		
		return prob;
	}

	/**
	 * Check if two probability rows can be sum
	 */
	public boolean canSum(ProbRow p1, ProbRow p2, int deletedVar) {
		assert p1.getValues().size() == p2.getValues().size() : "rows must be of equal length";
		for (int i = 0; i < p1.getValues().size(); i++)
			if (i != deletedVar && !p1.getValues().get(i).equals(p2.getValues().get(i)))
				return false;

		return true;
	}

	/**
	 * Make new (string)list of values without the value in the position of deleted
	 * variable
	 */
	public ArrayList<String> makeNewValues(ProbRow p, int deletedVar) {
		ArrayList<String> newValues = new ArrayList<String>();
		for (int i = 0; i < p.getValues().size(); i++)
			if (!(i == deletedVar))
				newValues.add(p.getValues().get(i));

		return newValues;
	}

	// Factor Product
	public Factor product(Factor f) {
		ArrayList<Variable> newVariables = new ArrayList<Variable>();
		ArrayList<ProbRow> newProb = new ArrayList<ProbRow>();

		ArrayList<Variable> variables2 = f.getVariables();
		ArrayList<ProbRow> table2 = f.getTable();

		ArrayList<Integer> idx1 = new ArrayList<Integer>();
		ArrayList<Integer> idx2 = new ArrayList<Integer>();

		// Check if two factors share same variable
		for (int i = 0; i < variables.size(); i++) {
			for (int j = 0; j < variables2.size(); j++) {
				if (variables.get(i).equals(variables2.get(j))) {
					idx1.add(i);
					idx2.add(j);
				}
			}
		}
		
		// Compare each row in two factors to product
		for (ProbRow p1 : this.getTable()) {
			for (ProbRow p2 : table2) {
				// check two rows(p1, p2) can be product
				if (canProduct(p1, p2, idx1, idx2)) {
					// if so, make a new (string)list of values in p1 and p2
					// and product their probability
					ArrayList<String> newValues = new ArrayList<String>();
					newValues = makeNewValues(p1, p2, idx2);
					Double prob = p1.getProb() * p2.getProb();
					newProb.add(new ProbRow(newValues, prob));
				}
			}
		}

		newVariables.addAll(getVariables());
		for (int i = 0; i < variables2.size(); i++)
			if (!(idx2.contains(i)))
				newVariables.add(variables2.get(i));

		return new Factor(newVariables, newProb);
	}

	/**
	 * Check if two probability rows can be product
	 */

	public boolean canProduct(ProbRow p1, ProbRow p2, ArrayList<Integer> idx1, ArrayList<Integer> idx2) {
		for (int i = 0; i < idx1.size(); i++) {
			int commonVal1 = idx1.get(i);
			int commonVal2 = idx2.get(i);
			String valOfF1 = p1.getValues().get(commonVal1);
			String valOfF2 = p2.getValues().get(commonVal2);

			if (!(valOfF1.equals(valOfF2)))
				return false;
		}
		return true;
	}

	/**
	 * Make new (string)list of values in two probability rows
	 */
	public ArrayList<String> makeNewValues(ProbRow p1, ProbRow p2, ArrayList<Integer> idx2) {
		ArrayList<String> values = new ArrayList<String>();
		values.addAll(p1.getValues());

		for (int i = 0; i < p2.getValues().size(); i++)
			if (!(idx2.contains(i)))
				values.add(p2.getValues().get(i));

		return values;
	}

	// Factor Reduction
	public Factor reduce(Variable ob) {
		ArrayList<Variable> newVariables = variables;
		ArrayList<ProbRow> newRows = new ArrayList<ProbRow>();
		int observedVar = -1;
		for (int i = 0; i < newVariables.size(); i++) {
			if (newVariables.get(i).getName().equals(ob.getName())) {
				observedVar = i;
			}
		}
		for (ProbRow r : table) {
			if (r.getValues().get(observedVar).equals(ob.getObservedValue())) {
				newRows.add(r);
			}
		}
		return new Factor(newVariables, newRows);

	}
	
	
	
	

}