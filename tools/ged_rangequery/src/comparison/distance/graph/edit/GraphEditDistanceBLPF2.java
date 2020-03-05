package comparison.distance.graph.edit;


import graph.Digraph;
import graph.Digraph.DiEdge;
import graph.Digraph.DiVertex;
import graph.Graph;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.LGraph;
import net.sf.javailp.Linear;
import net.sf.javailp.OptType;
import net.sf.javailp.Problem;
import net.sf.javailp.VarType;

/**
 * Implements BLP based GED computation according to F2 of 
 * 
 * New binary linear programming formulation to compute the 
 * graph edit distance
 * https://doi.org/10.1016/j.patcog.2017.07.029
 * 
 * 
 * @author Nils Kriege
 *
 */
public class GraphEditDistanceBLPF2<V,E> extends GraphEditDistanceBLP<V, E> {
	
	public GraphEditDistanceBLPF2() {
		super();
	}
	
	public GraphEditDistanceBLPF2(GraphEditCosts<V,E> gec) {
		this.gec = gec;
	}
	
	@Override
	public double compute(LGraph<V, E> lg1, LGraph<V, E> lg2) {
		Graph g1 = lg1.getGraph();
		Graph g2 = lg2.getGraph();
		
		// compute constant gamma
		double gamma = 0;
		for (Vertex i : g1.vertices()) {
			gamma += gec.vertexDeletion(i, lg1);
		}
		for (Vertex k : g2.vertices()) {
			gamma += gec.vertexInsertion(lg1, k, lg2);
		}
		for (Edge ij : g1.edges()) {
			gamma += gec.edgeDeletion(ij, lg1);
		}
		for (Edge kl : g2.edges()) {
			gamma += gec.edgeInsertion(lg1, kl, lg2);
		}

		return super.compute(lg1, lg2)+gamma;
	}
	
	@Override
	public void printEditPath(LGraph<V, E> lg1, LGraph<V, E> lg2) {
		throw new UnsupportedOperationException();
	}
	

	@Override
	protected Problem generateProblemFormulation(LGraph<V, E> lg1, LGraph<V, E> lg2) {
		Graph g1 = lg1.getGraph();
		Graph g2 = lg2.getGraph();
		
		Problem problem = new Problem();
		
		// ----------------------------------
		// generate objective function
		// ----------------------------------
		Linear objective = new Linear();
		
		// vertex costs
		for (Vertex i : g1.vertices()) {
			for (Vertex k : g2.vertices()) {
				double costs = gec.vertexRelabeling(i, lg1, k, lg2);
				costs -= gec.vertexDeletion(i, lg1);
				costs -= gec.vertexInsertion(lg1, k, lg2);
				objective.add(costs, "x"+i.toString()+","+k.toString());
			}
		}
		
		// edge costs
		for (Edge ij : g1.edges()) {
			for (Edge kl : g2.edges()) {
				double costs = gec.edgeRelabeling(ij, lg1, kl, lg2);
				costs -= gec.edgeDeletion(ij, lg1);
				costs -= gec.edgeInsertion(lg1, kl, lg2);
				objective.add(costs, "y"+ij.toString()+","+kl.toString());
			}
		}
		
		problem.setObjective(objective, OptType.MIN);
		

		// ----------------------------------
		// generate constraints
		// ----------------------------------
		
		// vertex mapping constraints
		for (Vertex i : g1.vertices()) {
			Linear constraint = new Linear();
			for (Vertex k : g2.vertices()) {
				constraint.add(1, "x"+i.toString()+","+k.toString());
			}
			problem.add("v_map1_"+i, constraint, "<=", 1);
		}
		for (Vertex k : g2.vertices()) {
			Linear constraint = new Linear();
			for (Vertex i : g1.vertices()) {
				constraint.add(1, "x"+i.toString()+","+k.toString());
			}
			problem.add("v_map2_"+k, constraint, "<=", 1);
		}
		
		if (g1 instanceof Digraph) {
			Digraph dig1 = ((Digraph)g1);
			Digraph dig2 = ((Digraph)g2);
			// directed edge mapping constraints
			for (DiVertex k : dig2.vertices()) {
				for (DiEdge ij : dig1.edges()) {
					Linear constraint = new Linear();
					for (DiEdge kl : k.outEdges()) {
						constraint.add(1, "y"+ij.toString()+","+kl.toString());
					}
					DiVertex i = ij.getSourceVertex();
					constraint.add(-1, "x"+i.toString()+","+k.toString());
					problem.add("e_map_"+k+","+ij, constraint, "<=", 0);
				}
			}
			for (DiVertex l : dig2.vertices()) {
				for (DiEdge ij : dig1.edges()) {
					Linear constraint = new Linear();
					for (DiEdge kl : l.inEdges()) {
						constraint.add(1, "y"+ij.toString()+","+kl.toString());
					}
					DiVertex j = ij.getTargetVertex();
					constraint.add(-1, "x"+j.toString()+","+l.toString());
					problem.add("e_map_"+l+","+ij, constraint, "<=", 0);
				}
			}	

		} else {
			// undirected edge mapping constraints
			for (Vertex k : g2.vertices()) {
				for (Edge ij : g1.edges()) {
					Linear constraint = new Linear();
					for (Edge kl : k.edges()) {
						constraint.add(1, "y"+ij.toString()+","+kl.toString());
					}
					Vertex i = ij.getFirstVertex();
					Vertex j = ij.getSecondVertex();
					constraint.add(-1, "x"+i.toString()+","+k.toString());
					constraint.add(-1, "x"+j.toString()+","+k.toString());
					problem.add("e_map_"+k+","+ij, constraint, "<=", 0);
				}
			}		
		}
		
		for (Object var : problem.getVariables()) {
			problem.setVarType(var, VarType.BOOL);
		}
		
		return problem;
	}

}
