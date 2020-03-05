package comparison.distance.graph.edit;

import graph.LGraph;
import graph.Graph.Edge;
import graph.Graph.Vertex;

/**
 * Defines the costs for approximating the graph edit distance by assignments;
 * the extended cost matrix incorporates the number edges only without performing
 * an assignment as {@link GraphEditAssignmentCostsExtended}. When there are no
 * edge relabeling costs, this is equivalent to {@link GraphEditAssignmentCostsExtended}
 * but more efficient.
 * 
 * @author Nils Kriege
 *
 * @param <V>
 * @param <E>
 */
public class GraphEditAssignmentCostsExtendedSimple<V, E> implements GraphEditAssignmentCosts<V, E> {
	
	GraphEditCosts<V,E> gec;
	double alpha = 1;
	
	public GraphEditAssignmentCostsExtendedSimple(GraphEditCosts<V,E> gec) {
		this.gec = gec;
	}
	
	public GraphEditAssignmentCostsExtendedSimple(GraphEditCosts<V,E> gec, double alpha) {
		this.gec = gec;
		this.alpha = alpha;
	}
	
	public GraphEditCosts<V, E> getGraphEditCosts() {
		return gec;
	}	
	
	public double vertexSubstitution(Vertex vEdit, LGraph<V, E> lgEdit, Vertex vTarget, LGraph<V, E> lgTarget) {
		double rV = gec.vertexRelabeling(vEdit, lgEdit, vTarget, lgTarget);
		double rE = 0;
		// add lower bound for edge costs obtained from assignment
		int n = vEdit.getDegree();
		int m = vTarget.getDegree();
		
		if (n>m) {
			rE += (n-m)*getGraphEditCosts().edgeDeletion(null, lgEdit);
		}
		if (n<m) {
			rE += (m-n)*getGraphEditCosts().edgeInsertion(lgEdit, null, lgTarget);
		}
		
		return rV + alpha * rE;
	}
	
	public double vertexDeletion(Vertex v, LGraph<V, E> lg) {
		double rV = gec.vertexDeletion(v, lg);
		double rE = 0;
		// add costs for all edge deletions
		for (Edge e : v.edges()) {
			rE += getGraphEditCosts().edgeDeletion(e, lg);
		}
		return rV + alpha * rE;
	}
	
	public double vertexInsertion(LGraph<V, E> lgEdit, Vertex vTarget, LGraph<V, E> lgTarget) {
		double rV = gec.vertexInsertion(lgEdit, vTarget, lgTarget);
		double rE = 0;
		// add costs for all edge insertions
		for (Edge eTarget : vTarget.edges()) {
			rE += getGraphEditCosts().edgeInsertion(lgEdit, eTarget, lgTarget);
		}
		return rV + alpha * rE;
	}
	
	/**
	 * Sets a factor applied for modifying the costs caused by edges.
	 * @param alpha
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}
	
}
