package ca.ualberta.cs.experiments;

import java.io.IOException;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;

public class ExperimentIncrementalHDBSCANStar {

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
		
		double s = Double.parseDouble(args[2]);
		boolean filter = Boolean.parseBoolean(args[3]);
		String method = args[4];
		
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, Integer.parseInt(args[1]), new EuclideanDistance());
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), Integer.parseInt(args[1]), filter, s, method);
		IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances, false, new EuclideanDistance(), Integer.parseInt(args[1]));

		for (int k = Integer.parseInt(args[1]) - 1; k > 1; k--) {
			RNG.updateWeights(dataSet, coreDistances, new EuclideanDistance(), k);
			IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances, false, new EuclideanDistance(), k);
		}
		
		end = System.currentTimeMillis();
		duration = end - start;
		System.out.println("DataSet: " + args[0] + ", Max MinPoints: " + args[1] + ", Time: " + duration);
	}
}
