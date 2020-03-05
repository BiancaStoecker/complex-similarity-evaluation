package comparison.distance.graph.edit.bounds;

import algorithm.assignment.AssignmentSolver;
import comparison.distance.graph.edit.GraphEditAssignmentCosts;
import comparison.distance.graph.edit.GraphEditAssignmentCostsExtendedSimple;
import comparison.distance.graph.edit.GraphEditCosts;

/**
 * Implements the BRANCH heuristic as described in the paper:
 * 
 * David B. Blumenthal, Nicolas Boria, Johann Gamper, Sébastien Bougleux, Luc Brun:
 * Comparing heuristics for graph edit distance computation. VLDB J. 29(1): 419-458 (2020)
 * 
 * No assignment between edges is computed, see {@link GraphEditAssignmentCostsExtendedSimple}.
 * 
 * This is a variant of the BP algorithm and implemented by extending that implementation.
 * 
 * @author kriege
 *
 */
public class BranchSimple<V,E> extends GraphEditDistanceAssignmentValue<V,E> implements GraphEditDistanceLowerBound<V,E> {

	AssignmentSolver solver;
	GraphEditAssignmentCosts<V,E> assignmentCosts;
	
	public BranchSimple(GraphEditCosts<V,E> graphEditCosts) {
		super(new GraphEditAssignmentCostsExtendedSimple<>(graphEditCosts, 0.5));
	}




}
