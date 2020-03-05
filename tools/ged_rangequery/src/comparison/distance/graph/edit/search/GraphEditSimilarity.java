package comparison.distance.graph.edit.search;

import comparison.DisSimilarity;
import comparison.distance.Distance;
import graph.LGraph;

public class GraphEditSimilarity<V,E> implements DisSimilarity<LGraph<V,E>> {
	
	Distance<LGraph<V,E>> ged;
	
	public GraphEditSimilarity(Distance<LGraph<V,E>> ged) {
		this.ged = ged;
	}

	@Override
	public double compute(LGraph<V, E> g1, LGraph<V, E> g2) {
		double dist = ged.compute(g1, g2);
		int size1 = g1.getGraph().getVertexCount() + g1.getGraph().getEdgeCount();
		int size2 = g2.getGraph().getVertexCount() + g2.getGraph().getEdgeCount();
		double size = size1+size2;
		return (size-dist)/(size+dist);
	}

}
