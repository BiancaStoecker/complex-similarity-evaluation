package comparison.distance.graph.edit;

import comparison.distance.Distance;
import comparison.distance.IdentityDistance;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.properties.EdgeArray;
import graph.properties.VertexArray;
import graph.LGraph;

/**
 * Defines the costs for graph edit operations.
 * 
 * @author Nils Kriege
 *
 * @param <V> the vertex label type
 * @param <E> the edge label type
 */
public class GraphEditCosts<V,E> {
	
	double vertexDeletionCost;
	double vertexInsertionCost;
	double edgeDeletionCost;
	double edgeInsertionCost;
	Distance<? super E> edgeLabelDistance;
	Distance<? super V> vertexLabelDistance;
	
	
	public GraphEditCosts() {
		this(1, 1, 1, 1, new IdentityDistance(), new IdentityDistance());
	}
	
	public GraphEditCosts(double vertexDeletionCost, double vertexInsertionCost, 
			double edgeDeletionCost, double edgeInsertionCost, 
			Distance<? super E> edgeLabelDistance, Distance<? super V> vertexLabelDistance) {
		this.vertexDeletionCost = vertexDeletionCost;
		this.vertexInsertionCost = vertexInsertionCost;
		this.edgeDeletionCost = edgeDeletionCost;
		this.edgeInsertionCost = edgeInsertionCost;
		this.edgeLabelDistance = edgeLabelDistance;
		this.vertexLabelDistance = vertexLabelDistance;
	}
	
	
	public double vertexDeletion(Vertex v, LGraph<V, E> lg) {
		return getVertexDeletionCost();
	}
	
	public double vertexInsertion(LGraph<V, E> lgEdit, Vertex vTarget, LGraph<V, E> lgTarget) {
		return getVertexInsertionCost();
	}

	public double edgeDeletion(Edge e, LGraph<V, E> lg) {
		return getEdgeDeletionCost();
	}

	public double edgeInsertion(LGraph<V, E> lgEdit, Edge eTarget, LGraph<V, E> lgTarget) {
		return getEdgeInsertionCost();
	}
	
	public double vertexRelabeling(Vertex vEdit, LGraph<V, E> lgEdit, Vertex vTarget, LGraph<V, E> lgTarget) {
		VertexArray<V> vaEdit = lgEdit.getVertexLabel();
		VertexArray<V> vaTarget = lgTarget.getVertexLabel();
		V lEdit = vaEdit.get(vEdit);
		V lTarget = vaTarget.get(vTarget);
		
		return getVertexLabelDistance().compute(lEdit, lTarget);
	}
	
	public double edgeRelabeling(Edge eEdit, LGraph<V, E> lgEdit, Edge eTarget, LGraph<V, E> lgTarget) {
		EdgeArray<E> eaEdit = lgEdit.getEdgeLabel();
		EdgeArray<E> eaTarget = lgTarget.getEdgeLabel();
		E lEdit = eaEdit.get(eEdit);
		E lTarget = eaTarget.get(eTarget);
		
		return getEdgeLabelDistance().compute(lEdit, lTarget);
	}

	/**
	 * @return the vertexDeletionCost
	 */
	public double getVertexDeletionCost() {
		return vertexDeletionCost;
	}

	/**
	 * @return the vertexInsertionCost
	 */
	public double getVertexInsertionCost() {
		return vertexInsertionCost;
	}

	/**
	 * @return the edgeDeletionCost
	 */
	public double getEdgeDeletionCost() {
		return edgeDeletionCost;
	}

	/**
	 * @return the edgeInsertionCost
	 */
	public double getEdgeInsertionCost() {
		return edgeInsertionCost;
	}

	/**
	 * @return the edgeLabelDistance
	 */
	public Distance<? super E> getEdgeLabelDistance() {
		return edgeLabelDistance;
	}

	/**
	 * @return the vertexLabelDistance
	 */
	public Distance<? super V> getVertexLabelDistance() {
		return vertexLabelDistance;
	}

}
