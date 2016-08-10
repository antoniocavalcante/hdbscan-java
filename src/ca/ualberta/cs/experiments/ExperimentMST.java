package ca.ualberta.cs.experiments;

import java.io.IOException;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;

public class ExperimentMST {
	public static void main(String[] args) throws IOException {
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

		int minPoints = Integer.parseInt(args[1]);
		if (minPoints > dataSet.length) {
			minPoints = dataSet.length;
		}
		
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, minPoints, new EuclideanDistance());
		
		// Computes the first MST w.r.t minPoints = 1 (same as Euclidean Distance).
		UndirectedGraph mst = HDBSCANStar.constructMST(dataSet, coreDistances, 1, false, new EuclideanDistance());
		mst.quicksortByEdgeWeight();

		String inputFile = args[0].split("/")[args[0].split("/").length - 1];		
		
		for (int k = minPoints; k >= 1; k--) {
			// Updates weights of the MST.
			mst.updateWeights(dataSet, coreDistances, new EuclideanDistance(), k);
			mst.quicksortByEdgeWeight();

			// Outputs the weight of the MST being generated in a file for comparison purposes.
			Experiments.writeMSTweight(inputFile, k, mst);
			
			// Generates the hierarchy for mst.
			String h = "MST_" + inputFile;
			Experiments.computeOutputFiles(dataSet, mst, k, h);
			mst.restoreEdges();			
		}

		end = System.currentTimeMillis();
		duration = end - start;
		System.out.println(args[0] + " " + args[1] + " " + duration);
	}
}
