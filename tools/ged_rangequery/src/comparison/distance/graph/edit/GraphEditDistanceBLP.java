package comparison.distance.graph.edit;

import comparison.distance.Distance;
import comparison.distance.graph.edit.bounds.GraphEditDistanceExact;
import comparison.distance.graph.edit.bounds.GraphEditDistanceLowerBound;
import comparison.distance.graph.edit.bounds.GraphEditDistanceUpperBound;
import graph.Graph;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.ImplementationHelper;
import graph.LGraph;
import net.sf.javailp.Problem;
import net.sf.javailp.Result;
import net.sf.javailp.Solver;
import net.sf.javailp.SolverFactory;
import util.solvers.SolverFactoryGurobi;

/**
 * Implements BLP based GED computation.
 * 
 * 
 * @author Nils Kriege
 * 
 */
public abstract class GraphEditDistanceBLP<V,E> implements GraphEditDistanceExact<V, E> {
	
//	SolverFactory factory = new SolverFactoryLpSolve(); // use lp_solve
//	SolverFactory factory = new SolverFactoryCPLEX();
	SolverFactory factory = new SolverFactoryGurobi();
	{
		factory.setParameter(Solver.VERBOSE, 0); 
		factory.setParameter(Solver.TIMEOUT, 100); // set timeout to 100 seconds
	}
	
	GraphEditCosts<V,E> gec;
	
	public GraphEditDistanceBLP() {
		this(new GraphEditCosts<>());
	}
	
	public GraphEditDistanceBLP(GraphEditCosts<V,E> gec) {
		this.gec = gec;
	}

	@Override
	public double compute(LGraph<V, E> lg1, LGraph<V, E> lg2) {
		return getResult(lg1, lg2).getObjective().doubleValue();
	}
	
	public void printEditPath(LGraph<V, E> lg1, LGraph<V, E> lg2) {
		Result r = getResult(lg1, lg2);
		
		Graph g1 = lg1.getGraph();
		Graph g2 = lg2.getGraph();
		
		// vertex relabeling costs
		for (Vertex i : g1.vertices()) {
			for (Vertex k : g2.vertices()) {
				if (r.getBoolean("x"+i.toString()+","+k.toString())) {
					double cost = gec.vertexRelabeling(i, lg1, k, lg2);
					System.out.println(i+" -> "+k+"\t"+cost);
				}
			}
		}
		
		// vertex deletion costs
		for (Vertex i : g1.vertices()) {
			if (r.getBoolean("u"+i.toString())) {
				double cost = gec.vertexDeletion(i, lg1);
				System.out.println(i+" -> X\t"+cost);
			}
		}
		
		// vertex insertion costs
		for (Vertex k : g2.vertices()) {
			if (r.getBoolean("v"+k.toString())) {
				double cost = gec.vertexInsertion(lg1, k, lg2);
				System.out.println("X -> "+k+"\t"+cost);
			}
		}

		// edge relabeling costs
		for (Edge ij : g1.edges()) {
			for (Edge kl : g2.edges()) {
				if (r.getBoolean("y"+ij.toString()+","+kl.toString())) {
					double cost = gec.edgeRelabeling(ij, lg1, kl, lg2);
					System.out.println(
							ImplementationHelper.toEndpointPairString(ij)+
							" -> "+
							ImplementationHelper.toEndpointPairString(kl)+
							"\t"+cost
					);
				}
			}
		}

		// edge deletion costs
		for (Edge ij : g1.edges()) {
			if (r.getBoolean("e"+ij.toString())) {
				double cost = gec.edgeDeletion(ij, lg1);
				System.out.println(ImplementationHelper.toEndpointPairString(ij)+" -> X\t"+cost);
			}
		}
		
		// edge insertion costs
		for (Edge kl : g2.edges()) {
			if (r.getBoolean("f"+kl.toString())) {
				double cost = gec.edgeInsertion(lg1, kl, lg2);
				System.out.println("X -> "+ImplementationHelper.toEndpointPairString(kl)+"\t"+cost);
			}
		}
	}
	
	protected Result getResult(LGraph<V, E> g1, LGraph<V, E> g2) {
		Problem problem = generateProblemFormulation(g1, g2);
		
		Solver solver = factory.get(); // you should use this solver only once for one problem
		
		Result r = null;
		
		// Gurobi sometimes causes strange exceptions and sometimes even causes the JVM to crash.
		// This solves the first problem.
		// The reason for this might be that 'Academic license - for non-commercial use only'
		// is printed to the same out stream as used by the program. Forwarding the program output
		// to a file via > helps.
		do {
			try {
				r = solver.solve(problem);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Warning Gurobi Exception -- trying again!" ); 
			};
		} while (r == null);
		
		return r;
	}

	
	protected abstract Problem generateProblemFormulation(LGraph<V, E> lg1, LGraph<V, E> lg2);
	
	
	

}
