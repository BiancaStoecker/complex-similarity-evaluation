package io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import graph.LGraph;


/**
 * Reads a file for graph input in the specified format.
 * Allows to retrieve data step-wise to allow usage with 
 * large files without storing the whole content in memory. 
 * 
 * Example Usage:
 * <pre>
 * IteratingGraphReader it = new IteratingGraphReader("test.gml", new GMLFormat());
 * while (it.hasNext()) {
 * 	LabeledGraph<String, String> lg = it.next();
 *  // process graph
 * }
 * it.close();
 * </pre>
 */
public class GraphReader<V,E> {
	
	private static GraphInput<String,String> defaultGraphInput = new GMLFormat();

	private GraphInput<V,E> gi;
	private BufferedReader br;
	private LGraph<V, E> next;
	
	public GraphReader(BufferedReader br, GraphInput<V,E> format) {
		this.br = br;
		this.gi = format;
	}
	
	public GraphReader(String fileName, GraphInput<V,E> format) throws FileNotFoundException {
		this(new BufferedReader(new FileReader(fileName)), format);
	}
	
	public boolean hasNext() throws IOException {
		return (next != null || loadNext());
	}

	public LGraph<V, E> next() {
		LGraph<V, E> r = next;
		next = null;
		return r;
	}

	protected boolean loadNext() throws IOException {
		next = gi.read(br);
		return (next != null);
	}
	
	public void close() throws IOException {
		br.close();
	}
	
	//
	// static methods for convenience
	//
	public static <V,E> ArrayList<LGraph<V,E>> readGraphs(String file, GraphInput<V,E> gi) throws IOException {
		ArrayList<LGraph<V, E>> r = new ArrayList<LGraph<V,E>>();
		
		GraphReader<V,E> it = new GraphReader<V,E>(new BufferedReader(new FileReader(file)), gi);
		while (it.hasNext()) {
			r.add(it.next());
		}
		it.close();
		
		return r;
	}
	
	public static <V,E> LGraph<V,E> readGraph(String file, GraphInput<V,E> gi) throws IOException {
		
		GraphReader<V,E> it = new GraphReader<V,E>(new BufferedReader(new FileReader(file)), gi);
		LGraph<V,E> lg = it.hasNext() ? it.next() : null;
		it.close();
		
		return lg;
	}
	
	public static ArrayList<LGraph<String, String>> readGraphs(String file) throws IOException {
		return readGraphs(file, defaultGraphInput);
	}

	public static GraphInput<String,String> getDefaultGraphInput() {
		return defaultGraphInput;
	}

	public static void setDefaultGraphInput(GraphInput<String,String> format) {
		defaultGraphInput = format;
	}

}
