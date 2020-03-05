package cli.ged;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import comparison.DisSimilarity;
import comparison.distance.IdentityDistance;
import comparison.distance.graph.edit.GraphEditAssignmentCostsExtendedSimple;
import comparison.distance.graph.edit.GraphEditCosts;
import comparison.distance.graph.edit.GraphEditDistanceAssignment;
import comparison.distance.graph.edit.GraphEditDistanceBLPF2;
import comparison.distance.graph.edit.bounds.BranchSimple;
import comparison.distance.graph.edit.bounds.LabelCount;
import comparison.distance.graph.edit.search.GraphEditSimilarity;
import comparison.distance.graph.edit.search.SimilarityBoundRangeSearch;
import graph.LGraph;
import io.GraphReader;

public class GEDSearch {
	
	public static void main(String[] args) throws IOException {
		
		String dbFileName = args[0];
		String queryFileName = args[1];
		double threshold = Double.parseDouble(args[2]);
		String outFileName = args[3];
			
		ArrayList<LGraph<String, String>> db = GraphReader.readGraphs(dbFileName);
		System.out.println("Database graphs: "+db.size());
		ArrayList<LGraph<String, String>> queries = GraphReader.readGraphs(queryFileName);
		System.out.println("Query graphs: "+queries.size());
		
		// set up range search
		GraphEditCosts<String, String> gec = new GraphEditCosts<>(1, 1, 1, 1, new IdentityDistance(), new IdentityDistance(2d));

		LinkedList<DisSimilarity<LGraph<String,String>>> upperBounds = new LinkedList<>();
		upperBounds.add(new GraphEditSimilarity<>(new LabelCount<String,String>(gec)));
		upperBounds.add(new GraphEditSimilarity<>(new BranchSimple<String,String>(gec)));
		
		LinkedList<DisSimilarity<LGraph<String,String>>> lowerBounds = new LinkedList<>();
		lowerBounds.add(new GraphEditSimilarity<>(new GraphEditDistanceAssignment<>(gec, new GraphEditAssignmentCostsExtendedSimple<>(gec))));
		lowerBounds.add(new GraphEditSimilarity<>(new GraphEditDistanceBLPF2<>(gec)));
		
		SimilarityBoundRangeSearch<String, String> rs = new SimilarityBoundRangeSearch<>(db, upperBounds, lowerBounds);
		

		long totalFilterTime = 0;
		long totalVerificationTime = 0;
		for (int i=0; i<queries.size(); i++) {
			LGraph<String, String> query = queries.get(i);
			System.out.println("Processing query "+i+" (|V|="+query.getGraph().getVertexCount()+", |E|="+query.getGraph().getEdgeCount()+")");

			// Filter
			long startTime = System.nanoTime();
			LinkedList<LGraph<String, String>> candidates = rs.findCandidates(query, threshold);
			long duration = System.nanoTime() - startTime;
			totalFilterTime += duration;
			System.out.println("\t Candidates: "+candidates.size()+"\t Time [ms]:"+((double)duration)/1000/1000);
			
			// Verification
			startTime = System.nanoTime();
			LinkedList<LGraph<String, String>> result = rs.verifyCandidates(query, candidates, threshold);
			duration = System.nanoTime() - startTime;
			totalVerificationTime += duration;
			System.out.println("\t Result:     "+result.size()+"\t Time [ms]: "+((double)duration)/1000/1000);
			System.out.println();
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFileName, true));
		bw.write("filter time (msec) sim="+threshold+","+((double)totalFilterTime)/1000/1000+"\n");
		bw.write("verification time (msec) sim="+threshold+","+((double)totalVerificationTime)/1000/1000+"\n");
		bw.close();
	}
}
