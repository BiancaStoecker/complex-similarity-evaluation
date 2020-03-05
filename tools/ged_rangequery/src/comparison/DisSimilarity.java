package comparison;

import java.util.List;
/**
 * Interface for the comparison of objects, either similarity or
 * dissimilarity measures.
 * 
 * @author kriege
 *
 * @param <T> type of the objects that can be compared
 * by this implementation
 */
public interface DisSimilarity<T> {

	/**
	 * Computes the (dis)similarity for the given pair of objects.
	 * @param g1 first object under comparison
	 * @param g2 second object under comparison
	 * @return the (dis)similarity
	 */
	public double compute(T g1, T g2);
	
	/**
	 * Computes the (dis)similarity for all pairs from the set of objects.
	 * 
	 * Note: This method should be overwritten if a preprocessing on the 
	 * elements in the set allows more efficient pair-wise computation.
	 * 
	 * @param set all objects
	 * @return symmetric matrix of (dis)similarities
	 */
	default double[][] computeAll(List<? extends T> set) {
		int n = set.size();
		double[][] r = new double[n][n];
		
		for (int i=0; i<n; i++) {
			System.out.print('.');
			T e1 = set.get(i);
			for (int j=i; j<n; j++) {
				T e2 = set.get(j);
				r[i][j] = r[j][i] = this.compute(e1, e2);
			}
		}
		System.out.println();
		return r;
	}
	
	/**
	 * Computes the (dis)similarity for all pairs from AxB.
	 * 
	 * Note: This method should be overwritten if a preprocessing on the 
	 * elements in the set allows more efficient pair-wise computation.
	 * 
	 * @param setA
	 * @param setB
	 * @return matrix of (dis)similarities
	 */
	default double[][] computeAll(List<? extends T> setA, List<? extends T> setB) {
		int n = setA.size();
		int m = setB.size();
		double[][] r = new double[n][m];
		
		for (int i=0; i<n; i++) {
			System.out.print('.');
			T e1 = setA.get(i);
			for (int j=0; j<m; j++) {
				T e2 = setB.get(j);
				r[i][j] = this.compute(e1, e2);
			}
		}
		System.out.println();
		return r;
	}
		
}
