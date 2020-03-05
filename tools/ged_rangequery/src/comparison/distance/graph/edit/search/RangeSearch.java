package comparison.distance.graph.edit.search;

import java.util.Collection;

import graph.LGraph;

public interface RangeSearch<V,E> {
	
	public Collection<LGraph<V,E>> search(LGraph<V,E> query, double maxDist);
	

}
