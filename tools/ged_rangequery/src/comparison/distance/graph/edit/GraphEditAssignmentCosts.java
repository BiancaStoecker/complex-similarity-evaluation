package comparison.distance.graph.edit;

import graph.LGraph;
import graph.Graph.Vertex;

/**
 * Defines the costs for approximating the graph edit distance by assignments.
 * 
 * @author Nils Kriege
 *
 * @param <V>
 * @param <E>
 */
public interface GraphEditAssignmentCosts<V, E> {
	
	/**
	 * @param vEdit vertex to be substituted
	 * @param lgEdit start graph
	 * @param vTarget target vertex
	 * @param lgTarget target graph
	 * @return the costs of substituting vEdit with vTarget
	 */
	public double vertexSubstitution(Vertex vEdit, LGraph<V, E> lgEdit, Vertex vTarget, LGraph<V, E> lgTarget);
	
	/**
	 * @param v vertex to be deleted
	 * @param lg graph to which v belongs
	 * @return the costs of deleting v in lg
	 */
	public double vertexDeletion(Vertex v, LGraph<V, E> lg);
	
	/**
	 * @param lgEdit start graph
	 * @param vTarget vertex to be inserted
	 * @param lgTarget target graph
	 * @return the costs of inserting vTarget
	 */
	public double vertexInsertion(LGraph<V, E> lgEdit, Vertex vTarget, LGraph<V, E> lgTarget);
	
}
