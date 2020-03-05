package io;

import java.io.BufferedReader;
import java.io.IOException;

import graph.LGraph;


public interface GraphInput<V,A> {

	/**
	 * Reads a single graph from the reader.
	 */
	public LGraph<V, A> read(BufferedReader r) throws IOException;
	
}
