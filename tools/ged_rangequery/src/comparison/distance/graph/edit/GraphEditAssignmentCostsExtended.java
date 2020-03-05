package comparison.distance.graph.edit;

import algorithm.assignment.AssignmentSolver;
import graph.LGraph;
import graph.Graph.Edge;
import graph.Graph.Vertex;

/**
 * Defines the costs for approximating the graph edit distance by assignments;
 * the extended cost matrix is computed based on {@link GraphEditCosts} according
 * the publication:
 * 
 * Riesen, K. & Bunke, H. 
 * Approximate graph edit distance computation by means of bipartite graph matching 
 * Image and Vision Computing , 2009, 27, 950 - 959
 * 
 * @author Nils Kriege
 *
 * @param <V>
 * @param <E>
 */
public class GraphEditAssignmentCostsExtended<V, E> extends GraphEditAssignmentCostsExtendedSimple<V, E> {
	
	AssignmentSolver solver;
	
	public GraphEditAssignmentCostsExtended(GraphEditCosts<V,E> gec) {
		super(gec);
		solver = new AssignmentSolver.Hungarian();
	}
	
	public GraphEditAssignmentCostsExtended(GraphEditCosts<V,E> gec, double alpha) {
		super(gec, alpha);
		solver = new AssignmentSolver.Hungarian();
	}
	
	@Override
	public double vertexSubstitution(Vertex vEdit, LGraph<V, E> lgEdit, Vertex vTarget, LGraph<V, E> lgTarget) {
		double rV = gec.vertexRelabeling(vEdit, lgEdit, vTarget, lgTarget);

		// add lower bound for edge costs obtained from assignment
		int n = vEdit.getDegree();
		int m = vTarget.getDegree();

		// return r when no edges present
		if (n == 0 && m == 0) return rV;
		
		double[][] C = new double[n+m][n+m];
		
		// upper left corner
		int eEditId = 0;
		for (Edge eEdit : vEdit.edges()) {
			int eTargetId = 0;
			for (Edge eTarget : vTarget.edges()) {
				C[eEditId][eTargetId] = getGraphEditCosts().edgeRelabeling(eEdit, lgEdit, eTarget, lgTarget);
				eTargetId++;
			}
			eEditId++;
		}
		
		// upper right corner
		for (int i=0; i<n; i++) {
			for (int j=m; j<n+m; j++) {
				C[i][j] = Double.POSITIVE_INFINITY;
			}
		}
		eEditId = 0;
		for (Edge eEdit : vEdit.edges()) {
			C[eEditId][m+eEditId] = getGraphEditCosts().edgeDeletion(eEdit, lgEdit);
			eEditId++;
		}
		
		// lower left corner
		for (int i=n; i<n+m; i++) {
			for (int j=0; j<m; j++) {
				C[i][j] = Double.POSITIVE_INFINITY;
			}
		}
		int eTargetId = 0;
		for (Edge eTarget : vTarget.edges()) {
			C[n+eTargetId][eTargetId] = getGraphEditCosts().edgeInsertion(lgEdit, eTarget, lgTarget);
			eTargetId++;
		}
		
		// lower right corner
		for (int i=n; i<n+m; i++) {
			for (int j=m; j<n+m; j++) {
				C[i][j] = 0;
			}
		}

		double rE = solver.minimumCost(C);
		
		return rV + alpha * rE;
	}
	


}
