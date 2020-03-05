package comparison.distance.graph.edit.bounds;

import java.util.Map.Entry;

import comparison.distance.IdentityDistance;
import comparison.distance.graph.edit.GraphEditCosts;
import datastructure.SparseFeatureVector;
import graph.Graph;
import graph.LGraph;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.properties.EdgeArray;
import graph.properties.VertexArray;

/**
 * Implements the label count lower bound used in INVES, Algorithm 4, extended to 
 * non-uniform edit costs.
 * 
 * @author kriege
 *
 */
public class LabelCount<V,E> implements GraphEditDistanceLowerBound<V,E> {

	double vertexDeletionCost;
	double vertexInsertionCost;
	double vertexLabelChange;
	double edgeDeletionCost;
	double edgeInsertionCost;
	double edgeLabelChange;
	
	public LabelCount(GraphEditCosts<V,E> graphEditCosts) {
		this.vertexDeletionCost = graphEditCosts.getVertexDeletionCost();
		this.vertexInsertionCost = graphEditCosts.getVertexInsertionCost();
		if (graphEditCosts.getVertexLabelDistance() instanceof IdentityDistance) {
			vertexLabelChange = ((IdentityDistance)graphEditCosts.getVertexLabelDistance()).getMaxDist();
		} else {
			throw new IllegalArgumentException("Unsupported vertex label distance.");
		}
		this.edgeDeletionCost = graphEditCosts.getEdgeDeletionCost();
		this.edgeInsertionCost = graphEditCosts.getEdgeInsertionCost();
		if (graphEditCosts.getEdgeLabelDistance() instanceof IdentityDistance) {
			edgeLabelChange = ((IdentityDistance)graphEditCosts.getEdgeLabelDistance()).getMaxDist();
		} else {
			throw new IllegalArgumentException("Unsupported edge label distance.");
		}

	}

	@Override
	public double compute(LGraph<V, E> lg1, LGraph<V, E> lg2) {
		
		double costs = 0;
		
		// vertex labels
		SparseFeatureVector<V> vl1 = createVertexLabelVector(lg1);
		SparseFeatureVector<V> vl2 = createVertexLabelVector(lg2);
		vl1.subtract(vl2);
		int inserts = 0;
		int deletes = 0;
		for (Entry<V, Double> l  : vl1.nonZeroEntries()) {
			if (l.getValue() < 0) {
				inserts -= l.getValue(); 
			} else {
				deletes += l.getValue();
			}
		}
		if (vertexLabelChange < vertexInsertionCost + vertexDeletionCost) {
			int min = Math.min(inserts, deletes);
			costs += min*vertexLabelChange;
			costs += (inserts-min)*vertexInsertionCost;
			costs += (deletes-min)*vertexDeletionCost;
		} else {
			costs += inserts*vertexInsertionCost;
			costs += deletes*vertexDeletionCost;

		}
		
		// edge labels
		SparseFeatureVector<E> el1 = createEdgeLabelVector(lg1);
		SparseFeatureVector<E> el2 = createEdgeLabelVector(lg2);
		el1.subtract(el2);
		inserts = 0;
		deletes = 0;
		for (Entry<E, Double> l  : el1.nonZeroEntries()) {
			if (l.getValue() < 0) {
				inserts -= l.getValue(); 
			} else {
				deletes += l.getValue();
			}
		}
		if (edgeLabelChange < edgeInsertionCost + edgeDeletionCost) {
			int min = Math.min(inserts, deletes);
			costs += min*edgeLabelChange;
			costs += (inserts-min)*edgeInsertionCost;
			costs += (deletes-min)*edgeDeletionCost;
		} else {
			costs += inserts*edgeInsertionCost;
			costs += deletes*edgeDeletionCost;
		}
		
		return costs;
	}
	
	private SparseFeatureVector<V> createVertexLabelVector(LGraph<V, E> lg) {
		Graph g = lg.getGraph();
		VertexArray<V> va = lg.getVertexLabel();
		
		SparseFeatureVector<V> r = new SparseFeatureVector<>();
		for (Vertex v : g.vertices()) {
			r.increaseByOne(va.get(v));
		}
		
		
		return r;
	}

	private SparseFeatureVector<E> createEdgeLabelVector(LGraph<V, E> lg) {
		Graph g = lg.getGraph();
		EdgeArray<E> ea = lg.getEdgeLabel();
		
		SparseFeatureVector<E> r = new SparseFeatureVector<>();
		for (Edge e : g.edges()) {
			r.increaseByOne(ea.get(e));
		}
		return r;
	}

}
