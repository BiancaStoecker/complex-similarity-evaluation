package algorithm.assignment;

import java.util.Arrays;
import java.util.Comparator;


/**
 * Base class for assignment algorithms. All implementations should 
 * allow n x m cost matrices with n != m. 
 * 
 * @author Nils Kriege
 *
 */
public abstract class AssignmentSolver {
	
	/**
	 * Computes an assignment. Unmatched elements should be assigned
	 * to -1.
	 * 
	 * @param costs the costs matrix
	 * @return an assignment
	 */
	public abstract int[] solve(double[][] costs);
	
	public double minimumCost(double[][] costs) {
		return assignmentCost(solve(costs), costs);
	}
	
	public static double assignmentCost(int[] assignment, double[][] costs) {
		double c = 0;
		for (int i=0; i<assignment.length; i++) {
			if(assignment[i]>0)// -1 for unmatched elements if matrix is not quadratic 
			{
				c += costs[i][assignment[i]];
			}
		}
		return c;		
	}

	/**
	 * Hungarian algorithm. Solves the problem exactly in cubic time.
	 */
	public static class Hungarian extends AssignmentSolver {
		
		@Override
		public int[] solve(double[][] costs) {
			if (costs.length == 0) return new int[0];
			HungarianAlgorithm ha = new HungarianAlgorithm(costs);
			return ha.execute();
		}
	
	}
	
	/**
	 * VolgenantJonker algorithm. Solves the problem exactly (faster than hungarian for quadratic matrices).
	 */
	public static class VolgenantJonker extends AssignmentSolver {
		
		@Override
		public int[] solve(double[][] costs) {
			if (costs.length == 0) return new int[0];
			VolgenantJonkerAlgorithm vj = new VolgenantJonkerAlgorithm(costs);
			return vj.execute();
		}
	
	}
	
	/**
	 * Greedy algorithm sorting all edges according to their weight.
	 * The running time is O(n^2 log n); 
	 * 
	 * @author Nils Kriege
	 */
	public static class GreedySort extends AssignmentSolver {

		@Override
		public int[] solve(double[][] costs) {
			
			int n = costs.length;
			int m = costs[0].length;
			
			double[] C = new double[n*m]; 
			for (int i=0; i<n; i++) {
				System.arraycopy(costs[i], 0, C, i*m, m);
			}
			
	        Integer[] idx = new Integer[n*m];
	        for (int i=0; i<idx.length; i++) {
	            idx[i] = i;
	        }

			// sort indices according to edge weights
	        //
	        // TODO this comparator based sorting is extremely slow
	        // Arrays.sort(C) only takes 10% of the time.
	        // Implement a sorting strategy that returns the permutation
			Comparator<Integer> comparator = new Comparator<Integer>() {
				public int compare(Integer a, Integer b) {
					return Double.compare(C[a], C[b]);
				}
			};
			Arrays.sort(idx, comparator);

			// greedy assignment
			int[] assignment = new int[n];
			Arrays.fill(assignment, -1);
			
			boolean[] indexU = new boolean [n];
			boolean[] indexV = new boolean [m];
			int assigned = 0;
			for(int x : idx) {
				int i = x/m;
				int j = x%m;
				if(!indexU[i] && !indexV[j]) {
					indexU[i] = true;
					indexV[j] = true;
					assignment[i]=j;
					if (++assigned == Math.min(n, m)) return assignment;
				}
			}
			
			return assignment;
		}
		
	}
	
	/**
	 * Column-wise greedy algorithm. The running time is O(n^2);
	 * 
	 * @author Nils Kriege
	 */
	public static class GreedyBasic extends AssignmentSolver {
		
		@Override
		public int[] solve(double[][] costs) {
			
			int n = costs.length;
			int m = costs[0].length;
			
			// greedy assignment
			int[] assignment = new int[n];
			Arrays.fill(assignment, -1);
			
			boolean[] matched = new boolean [m];
			for (int i=0; i<n; i++) {
				double min = Double.POSITIVE_INFINITY;
				int jMin = -1;
				for (int j=0; j<m; j++) {
					if (!matched[j] && (costs[i][j] < min || jMin == -1)) {
						min = costs[i][j];
						jMin = j;
					}
				}
				assignment[i] = jMin;
				if (jMin != -1) matched[jMin] = true;
			}

			
			return assignment;
		}
		
	}
}
