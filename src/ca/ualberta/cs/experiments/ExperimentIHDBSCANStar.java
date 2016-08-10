package ca.ualberta.cs.experiments;

import java.io.IOException;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;

public class ExperimentIHDBSCANStar {

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
		
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, 
				coreDistances, new EuclideanDistance(), minPoints, Boolean.parseBoolean(args[3]), Double.parseDouble(args[2]), args[4]);

		IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances, false, new EuclideanDistance(), minPoints);
		
		for (int k = minPoints - 1; k >= 1; k--) {
			RNG.updateWeights(dataSet, coreDistances, new EuclideanDistance(), k);

			UndirectedGraph mst = IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances, false, new EuclideanDistance(), k);
		
			// Outputs the weight of the MST being generated in a file for comparison purposes.
			Experiments.writeMSTweight(inputFile, k, mst);
			
			// Generates the hierarchy for mst.
			String h = "RNG_" + inputFile;
			Experiments.computeOutputFiles(dataSet, mst, k, h);
		}
		
		end = System.currentTimeMillis();
		duration = end - start;
		
		// Data set, minPts, Time, RNG size, filter
		System.out.println(args[0] + " " + args[1] + " " + duration + " " + RNG.numOfEdgesMRG + " " + args[3]);
	}
}
