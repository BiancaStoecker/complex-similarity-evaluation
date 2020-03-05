package comparison.distance.graph.edit.bounds;

import algorithm.assignment.AssignmentSolver;
import comparison.distance.graph.edit.GraphEditAssignmentCosts;
import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import graph.LGraph;

// TODO change extends hierarchy!
public class GraphEditDistanceAssignmentValue<V,E> extends GraphEditDistanceAssignment<V,E>  {

	public GraphEditDistanceAssignmentValue(GraphEditAssignmentCosts<V,E> assignmentCosts) {
		// Note: We have to use an exact assignment solver!
		super(new AssignmentSolver.Hungarian(), null, assignmentCosts);
	}
	
	@Override
	public double compute(LGraph<V, E> lg1, LGraph<V, E> lg2) {
		double[][] C = costMatrix(lg1, lg2);
		return solver.minimumCost(C);
	}
	

}
