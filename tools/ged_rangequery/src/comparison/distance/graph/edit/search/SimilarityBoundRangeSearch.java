package comparison.distance.graph.edit.search;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import comparison.DisSimilarity;
import graph.LGraph;

public class SimilarityBoundRangeSearch<V,E> implements RangeSearch<V, E> {
	
	Collection<LGraph<V, E>> database;
	List<? extends DisSimilarity<LGraph<V,E>>> lowerBounds;
	List<? extends DisSimilarity<LGraph<V,E>>> upperBounds;
	
	public SimilarityBoundRangeSearch(Collection<LGraph<V, E>> database, List<? extends DisSimilarity<LGraph<V,E>>> upperBounds, List<? extends DisSimilarity<LGraph<V,E>>> lowerBounds) {
		this.database = database;
		this.upperBounds = upperBounds;
		this.lowerBounds = lowerBounds;
	}

	@Override
	public Collection<LGraph<V, E>> search(LGraph<V, E> query, double minSim) {
		
		LinkedList<LGraph<V, E>> candidates = findCandidates(query, minSim);
		LinkedList<LGraph<V, E>> result = verifyCandidates(query, candidates, minSim);
		
		return result;
	}
	
	public LinkedList<LGraph<V, E>> findCandidates(LGraph<V, E> query, double minSim) {
		// filtering via upper bounds
		LinkedList<LGraph<V, E>> candidates = new LinkedList<>();
		for (LGraph<V, E> lg : database) {
			boolean filtered = false;
			for (DisSimilarity<LGraph<V,E>> b : upperBounds) {
				double bound = b.compute(query, lg);
				if (bound < minSim) {
					filtered = true;
					break;
				}
			}
			if (!filtered) {
				candidates.add(lg);
			}
		}
		return candidates;
	}
	
	public LinkedList<LGraph<V, E>> verifyCandidates(LGraph<V, E> query, LinkedList<LGraph<V, E>> candidates, double minSim) {
		// verification via lower bounds
		LinkedList<LGraph<V, E>> r = new LinkedList<>();
		for (LGraph<V, E> lg : candidates) {
			for (DisSimilarity<LGraph<V,E>> b : lowerBounds) {
				double bound = b.compute(query, lg);
				if (bound >= minSim) {
					r.add(lg);
					break;
				}
			}
		}
		return r;
	}


}
