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

		start = System.currentTimeMillis();
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, Integer.parseInt(args[1]), new EuclideanDistance());
		
		for (int k = Integer.parseInt(args[1]); k > 1; k--) {
			UndirectedGraph mst = HDBSCANStar.constructMST(dataSet, coreDistances, k, false, new EuclideanDistance());
			mst.quicksortByEdgeWeight();
			Experiments.writeMSTweight(args[0], k, mst);
		}

		end = System.currentTimeMillis();
		duration = end - start;
		System.out.println(args[0] + " " + args[1] + " " + duration);
	}
}
