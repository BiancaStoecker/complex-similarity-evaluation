package graph;

public interface Digraph extends Graph {

	public Iterable<? extends DiVertex> vertices();
	public Iterable<? extends DiEdge> edges();
	
	public interface DiVertex extends Vertex {
		public Iterable<? extends DiEdge> inEdges();
		public Iterable<? extends DiVertex> inNeighbours();
		public int inDegree();
		public Iterable<? extends DiEdge> outEdges();
		public Iterable<? extends DiVertex> outNeighbours();
		public int outDegree();
	}

	public interface DiEdge extends Edge {
		public DiVertex getSourceVertex();
		public DiVertex getTargetVertex();
	}

}
