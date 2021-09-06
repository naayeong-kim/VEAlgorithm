package varelim;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class VEAlgorithm {
	ArrayList<Variable> vs;
	ArrayList<Table> ps;
	Variable query;
	ArrayList<Variable> obs;
	PriorityQueue<Variable> order;
	ArrayList<Factor> factorList = new ArrayList<Factor>();

	public VEAlgorithm(ArrayList<Variable> variables, ArrayList<Table> probabilities, Variable query,
			ArrayList<Variable> observed) {
		this.vs = variables;
		this.ps = probabilities;
		this.query = query;
		this.obs = observed;
	}

	public void Algorithm() {
		this.order = this.decideOrdering();
		factorList = this.variablesIntoFactor();

		for (int k = 0; k < factorList.size(); k++) {
			for (int j = 0; j < obs.size(); j++) {
				for (int i = 0; i < factorList.get(k).getVariables().size(); i++) {
					// if so, store the index of variable we need to delete in deletedVar
					if (factorList.get(k).getVariables().get(i).equals(obs.get(j))) {
						Factor newF = factorList.get(k).reduce(obs.get(j));
						factorList.remove(factorList.get(k));
						factorList.add(newF);
					}
				}
			}
		}

		// for each variable in elimination order do :
		while (!order.isEmpty()) {
			ArrayList<Factor> factorListProduct = new ArrayList<Factor>();
			ArrayList<Factor> toRemove = new ArrayList<Factor>();
			Variable v = order.poll();

			// find all factors containing variable v
			for (int i = 0; i < factorList.size(); i++) {
				Factor tempFac = factorList.get(i);
				if (tempFac.getVariables().contains(v)) {
					// add factor to the list of factors that need to be multiplied (product)
					factorListProduct.add(tempFac);
					toRemove.add(tempFac); ///////////////
				}
			}

			// multiply all factors containing variable v
			while (factorListProduct.size() != 1) {
				Factor newF = factorListProduct.get(0).product(factorListProduct.get(1));
				factorListProduct.remove(0);
				factorListProduct.remove(0);
				factorListProduct.add(0, newF);
			}

			// marginalise out variable to obtain new factor
			// add new Factor to factorList
			Factor newF = factorListProduct.get(0).marginalization(v);
			factorList.add(newF);
			for (Factor r : toRemove)
				factorList.remove(r);
		}

		if (!(checkOrderMethod())) {
			while (factorList.size() != 1) {
				Factor newF = factorList.get(0).product(factorList.get(1));
				factorList.remove(0);
				factorList.remove(0);
				factorList.add(0, newF);
			}
		}

		// normalise result of factor of query and print result
		Table resultTable = normalise();
		System.out.println(resultTable.toString());
	}

	// to normalise the final result
	private Table normalise() {
		ArrayList<ProbRow> resultProbRow = new ArrayList<ProbRow>();
		double sum = 0;
		// normalise the probabilities for the factor of query
		for (int i = 0; i < factorList.get(0).getTable().size(); i++) {
			sum += factorList.get(0).getTable().get(i).getProb();
		}
		for (int i = 0; i < factorList.get(0).getTable().size(); i++) {
			double prob = factorList.get(0).getTable().get(i).getProb() / sum;
			resultProbRow.add(new ProbRow(factorList.get(0).getTable().get(i).getValues(), prob));
		}

		return new Table(query, resultProbRow);
	}

	private ArrayList<Factor> variablesIntoFactor() {
		ArrayList<Factor> factorList = new ArrayList<Factor>();
		for (Variable v : this.vs) {
			ArrayList<Variable> var = new ArrayList<Variable>();
			if (this.order.contains(v) || v.equals(query)) {
				var.add(v);
				if (v.getNrOfParents() >= 1)
					var.addAll(v.getParents());

				for (Table t : this.ps) {
					if (t.getVariable().equals(v))
						factorList.add(new Factor(var, t.getTable()));
				}
			}
		}
		return factorList;
	}

	private PriorityQueue<Variable> decideOrdering() {
		Comparator<Variable> comparator = new VariableComparator();
		PriorityQueue<Variable> order = new PriorityQueue<Variable>(vs.size(), comparator);
		ArrayList<Variable> parentsOfQuery = ParentsOfQuery();
		ArrayList<Variable> parentsOfObs = ParentsOfObservedValue();
		for (Variable v : vs) {
			if (checkOrderMethod()) {
				if (!(v.equals(query)) && parentsOfQuery.contains(v))
					order.add(v);
			} else {
				for (Variable ob : obs) {
					if (v.equals(ob))
						order.add(v);
					if (!(v.equals(query)) && parentsOfObs.contains(v))
						order.add(v);
				}
			}
		}
		return order;

	}

	private ArrayList<Variable> ParentsOfQuery() {
		ArrayList<Variable> parents = new ArrayList<Variable>();
		ArrayList<Variable> temp = new ArrayList<Variable>();
		temp.add(query);
		while (!(temp.isEmpty())) {
			if (temp.get(0).hasParents()) {
				temp.addAll(temp.get(0).getParents());
				parents.addAll(temp.get(0).getParents());
			}
			temp.remove(0);
		}
		return parents;
	}

	private ArrayList<Variable> ParentsOfObservedValue() {
		ArrayList<Variable> parents = new ArrayList<Variable>();
		ArrayList<Variable> temp = new ArrayList<Variable>();
		temp.addAll(obs);
		while (!(temp.isEmpty())) {
			if (temp.get(0).hasParents()) {
				temp.addAll(temp.get(0).getParents());
				parents.addAll(temp.get(0).getParents());
			}
			temp.remove(0);
		}
		return parents;
	}

	private boolean checkOrderMethod() {
		boolean forward = false;
		ArrayList<Variable> parents = new ArrayList<Variable>();
		ArrayList<Variable> temp = new ArrayList<Variable>();
		temp.add(query);
		while (!(temp.isEmpty())) {
			if (temp.get(0).hasParents()) {
				temp.addAll(temp.get(0).getParents());
				parents.addAll(temp.get(0).getParents());
			}
			temp.remove(0);
		}
		if (parents.containsAll(obs))
			forward = true;
		return forward;
	}

}