package ca.ualberta.cs.experiments;

import java.io.IOException;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;


public class ExperimentHDBSCANStar {

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

		String inputFile = args[0].split("/")[args[0].split("/").length - 1];
		
		start = System.currentTimeMillis();
		
		int minPoints = Integer.parseInt(args[1]);
		if (minPoints > dataSet.length) {
			minPoints = dataSet.length;
		}
		
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, minPoints, new EuclideanDistance());
		
		for (int k = minPoints; k >= 1; k--) {
			UndirectedGraph mst = HDBSCANStar.constructMST(dataSet, coreDistances, k, false, new EuclideanDistance());
			mst.quicksortByEdgeWeight();
			
			// Outputs the weight of the MST being generated in a file for comparison purposes.
			Experiments.writeMSTweight("HDBSCAN", inputFile, k, mst);
			
			// Generates the hierarchy for mst.
//			String h = "ORI_" + inputFile;
//			Experiments.computeOutputFiles(dataSet, mst, k, h);
		}

		end = System.currentTimeMillis();
		duration = end - start;
		System.out.println(args[0] + " " + args[1] + " " + duration);
	}
}
