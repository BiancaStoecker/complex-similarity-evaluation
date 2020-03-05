package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import graph.AdjListGraph;
import graph.Digraph;
import graph.ExtendibleGraph;
import graph.Graph;
import graph.Graph.Edge;
import graph.Graph.Vertex;
import graph.LGraph;
import graph.properties.EdgeArray;
import graph.properties.VertexArray;


/**
 * Reads and writes GML (Graph Modeling Language) files.
 * 
 * Note: This implementation is kept simple and does not
 * support all features of the standard! It is not robust
 * and expects new lines for each key word.
 * 
 * Note: Only String and numeric labels and properties 
 * are handled correctly!
 */
public class GMLFormat implements GraphInput<String,String>, GraphOutput {
	
	public void write(Writer w, LGraph<?, ?> lg) throws IOException {
		Graph g = lg.getGraph();
		VertexArray<?> va = lg.getVertexLabel();
		EdgeArray<?> ea = lg.getEdgeLabel();
		
		w.write("graph ["+"\n");
		w.write("\t"+"directed "+(g instanceof Digraph ? "1":"0")+"\n");
		
		for (String s : g.getProperties()) {
			String label = labelToString(g.getProperty(s));
			w.write("\t"+s+" "+label+"\n");
		}

		for (Vertex v : g.vertices()) {
			w.write("\t"+"node ["+"\n");
			w.write("\t\t"+"id "+v.getIndex()+"\n");
			Object label = va.get(v);
			if (label != null) {
				w.write("\t\t"+"label "+labelToString(label)+"\n");
			}
			w.write("\t"+"]"+"\n");
		}
		
		for (Edge e: g.edges()) {
			w.write("\t"+"edge ["+"\n");
			w.write("\t\t"+"source "+e.getFirstVertex().getIndex()+"\n");
			w.write("\t\t"+"target "+e.getSecondVertex().getIndex()+"\n");
			Object label = ea.get(e);
			if (label != null) {
				w.write("\t\t"+"label "+labelToString(label)+"\n");
			}
			w.write("\t"+"]"+"\n");
		}
		
		w.write("]"+"\n");
		w.flush();
	}
	
	public LGraph<String, String> read(BufferedReader r) throws IOException {

		String line;
		while ((line = nextLine(r)) != null) {
			
			// handle comments
			if (line.startsWith("#")) continue;
			
			if (line.equals("graph [")) {
				ExtendibleGraph g = new AdjListGraph();
				VertexArray<String> va = new VertexArray<String>(g,true);
				EdgeArray<String> ea = new EdgeArray<String>(g,true);
				HashMap<Integer,Integer> mapId = new HashMap<Integer, Integer>();

				while (!(line = nextLine(r)).equals("]")) {
					if (line.endsWith("[")) {
						if (line.startsWith("node")) {
							Vertex v = g.createVertex();
							while (!(line = nextLine(r)).equals("]")) {
								String[] kv = line.split(" ");
								if (kv[0].equals("id")) {
									 mapId.put(Integer.valueOf(kv[1]), v.getIndex());
								} else if (kv[0].equals("label")) {
									va.set(v, stringToLabel(kv[1]));
								}
								// TODO handle other properties
							}
						} else if (line.startsWith("edge")) {
							Vertex source = null;
							Vertex target = null;
							String label = "";
							while (!(line = nextLine(r)).equals("]")) {
								String[] kv = line.split(" ");
								if (kv[0].equals("label")) {
									label = stringToLabel(kv[1]);
								} else if (kv[0].equals("source")) {
									source = g.getVertex(mapId.get(Integer.valueOf(kv[1])));
								} else if (kv[0].equals("target")) {
									target = g.getVertex(mapId.get(Integer.valueOf(kv[1])));
								}
								// TODO handle other properties
							}
							Edge e = g.createEdge(source, target);
							ea.set(e, label);
						}
					} else { // handle as property
						String[] kv = line.split(" ");
						String v = stringToLabel(kv[1]);
						if (kv[0].equals("directed")) { // ignore as property
							if (v.equals("1")) {
								throw new IOException("Directed graphs are currently not supported.");
							}
						} else {
							g.setProperty(kv[0], v);
						}
					}
				}
				return new LGraph<String, String>(g, va, ea);
			}
		}
		
		return null;
	}
	
	private static String labelToString(Object o) {
		if (o instanceof Number) {
			return o.toString();
		} else {
			return "\""+o.toString()+"\"";
		}
	}
	
	private static String stringToLabel(String v) {
		if (v.startsWith("\"") && v.endsWith("\"") && v.length()>1) {
			v = v.substring(1, v.length()-1); 
		}
		return v;
	}

	
	private static String nextLine(BufferedReader r) throws IOException {
		String s = r.readLine();
		return (s == null) ? s : s.trim().replaceAll("\\s+", " ");
	}
	
//	/**
//	 * Test
//	 */
//	public static void main(String[] args) throws IOException {
//		
//		GMLFormat gml = new GMLFormat();
//		
//		GraphWriter gw = new GraphWriter("test.gml", gml);
//		for (int i=0; i<10; i++) {
//			EditableGraph g = new AdjListGraph();
//			GraphTools.addRandomGraph(5, 8, g);
//			g.setProperty("class", 1);
//			LabeledGraph<String, String> lg = GraphTools.createRandomStringLabeling(g, 3, 4);
//			gw.write(lg);
//		}
//		gw.close();
//		
//		LinkedList<LabeledGraph<String, String>> lgs = new LinkedList<LabeledGraph<String,String>>();
//		
//		IteratingGraphReader iter = new IteratingGraphReader("test.gml", gml);
//		while (iter.hasNext()) {
//			lgs.add(iter.next());
//		}
//		iter.close();
//		
//		gw = new GraphWriter("test2.gml", gml);
//		for (LabeledGraph<String, String> lg : lgs) {
//			gw.write(lg);
//		}
//		gw.close();
		
//		write(new OutputStreamWriter(System.out), lg);
		
//		GraphFrame.createGraphFrame(lg);
//		
//		StringWriter sw = new StringWriter();
//		gml.write(sw, lg);
//		StringBuffer sb = sw.getBuffer();
//		BufferedReader br = new BufferedReader(new StringReader(sb.toString()));
//		
//		LabeledGraph<String, String> lg2 = gml.read(br);
//		
//		GraphFrame.createGraphFrame(lg2);
////		write(new OutputStreamWriter(System.out), lg2);
//	}
	
}
