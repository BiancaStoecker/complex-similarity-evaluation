package io;

import java.io.IOException;
import java.io.Writer;

import graph.LGraph;


public interface GraphOutput {

	public void write(Writer w, LGraph<?, ?> lg) throws IOException;
	
}
