package ca.ualberta.cs.experiments;

import java.io.IOException;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;
import ca.ualberta.cs.util.HAI;

public class ExperimentMST {
	public static void main(String[] args) {
		long start, end, duration;
		
		Double[][] dataSet = null;

		try {
			dataSet = HDBSCANStar.readInDataSet(args[0], " ");
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}
		
		start = System.currentTimeMillis();
	
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, Integer.parseInt(args[1]), new EuclideanDistance());
		
		// Computes the first MST w.r.t minPoints = 1 (same as Euclidean Distance).
		UndirectedGraph mst = HDBSCANStar.constructMST(dataSet, coreDistances, 1, false, new EuclideanDistance());
		mst.quicksortByEdgeWeight();
		
		String inputFile = args[0].split("/")[args[0].split("/").length - 1];
		
		String baseDir = "tmp/";
		
		for (int k = Integer.parseInt(args[1]); k >= 1; k--) {
			// Updates weights of the MST.
			mst.updateWeights(dataSet, coreDistances, new EuclideanDistance(), k);
			mst.quicksortByEdgeWeight();
			
			// Computes the MST from the MRG.
			UndirectedGraph mst2 = HDBSCANStar.constructMST(dataSet, coreDistances, k, false, new EuclideanDistance());
			mst2.quicksortByEdgeWeight();
			
			// Generates the hierarchy for mst.
			String h1 = "MST_" + inputFile;
			computeHierarchy(dataSet, mst, k, h1);
			mst.restoreEdges();
			
			// Generates the hierarchy for mst2.
			String h2 = "MRG_" + inputFile;
			computeHierarchy(dataSet, mst2, k, h2);
						
			// Compares both hierarchies with HAI.
			double index = HAI.evaluate(baseDir + k + h1 + ".hierarchy", baseDir + k + h2 + ".hierarchy");

			System.out.println(args[0] + " " + k + " " + mst.getTotalWeight() + " " + mst2.getTotalWeight() + " " + index);			
		}

		end = System.currentTimeMillis();
		duration = end - start;
//		System.out.println(args[0] + " " + args[1] + " " + duration);
	}
	
	public static void computeHierarchy(Double[][] dataSet, UndirectedGraph mst, int minPts, String inputFile) {
		
		int numPoints = dataSet.length;
		
		String outputPrefix = "tmp/" + minPts + inputFile;
		
		double[] pointNoiseLevels = new double[numPoints];
		int[] pointLastClusters = new int[numPoints];
		
		try {
//			long startTime = System.currentTimeMillis();
			HDBSCANStar.computeHierarchyAndClusterTree(mst, minPts, 
					true, null, outputPrefix + ".hierarchy", 
					outputPrefix + ".tree", ",", pointNoiseLevels, pointLastClusters, outputPrefix + ".vis");
//			System.out.println("Time to compute hierarchy and cluster tree (ms): " + (System.currentTimeMillis() - startTime));
		
		} catch (IOException ioe) {
			System.err.println("Error writing to hierarchy file or cluster tree file.");
			System.exit(-1);
		}
	}
}
