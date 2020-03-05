package comparison.distance.graph.edit.bounds;

import comparison.distance.Distance;
import graph.LGraph;

/**
 * Flags exact graph edit distances.
 * 
 * @author kriege
 *
 * @param <V>
 * @param <E>
 */
public interface GraphEditDistanceExact<V,E> extends GraphEditDistanceLowerBound<V,E>, GraphEditDistanceUpperBound<V,E> {

}
