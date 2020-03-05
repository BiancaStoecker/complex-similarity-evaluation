package comparison.distance.graph.edit.bounds;

import comparison.distance.Distance;
import graph.LGraph;

/**
 * Flags distances on graphs that are upper bounds on the exact graph edit distance.
 * 
 * @author kriege
 *
 * @param <V>
 * @param <E>
 */
public interface GraphEditDistanceUpperBound<V,E> extends Distance<LGraph<V,E>> {

}
