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
			
		int minPoints = Integer.parseInt(args[1]);
		if (minPoints > dataSet.length) {
			minPoints = dataSet.length;
		}

		// Prints data set, minPoints, Run
		System.out.print(args[0] + " " + args[1] + " " + args[2]);
		
		start = System.currentTimeMillis();

		// Computes all the core-distances from 1 to minPoints
		long startcore = System.currentTimeMillis();		
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, minPoints, new EuclideanDistance());
		System.out.print(" " + (System.currentTimeMillis() - startcore));
		
		// Computes the RNG
		long startRNG = System.currentTimeMillis();
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), minPoints, Boolean.parseBoolean(args[4]), 1, "WS");
		System.out.print(" " + (System.currentTimeMillis() - startRNG));
		
		// Computes all the minPoints MSTs
		long startMSTs = System.currentTimeMillis();
		UndirectedGraph mst = IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances, false, new EuclideanDistance(), minPoints);

		Experiments.writeMSTweight("IHDBSCAN", inputFile, minPoints, mst);
				
		for (int k = minPoints - 1; k >= 1; k--) {
			RNG.updateWeights(dataSet, coreDistances, new EuclideanDistance(), k);

			mst = IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances, false, new EuclideanDistance(), k);
		
			Experiments.writeMSTweight("IHDBSCAN", inputFile, k, mst);
			
			if (Boolean.parseBoolean(args[3])) {
				Experiments.computeOutputFiles(dataSet, mst, k, "RNG_" + inputFile);	
			}
		}
		
		System.out.print(" " + (System.currentTimeMillis() - startMSTs));		
		
		end = System.currentTimeMillis();
		duration = end - start;
		
		// Data set, minPts, Time, RNG size
		System.out.println(" " + duration + " " + RNG.numOfEdgesRNG);
	}
}
