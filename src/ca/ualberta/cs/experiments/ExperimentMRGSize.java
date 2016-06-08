package ca.ualberta.cs.experiments;

import java.io.IOException;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.hdbscanstar.MutualReachabilityGraph;

public class ExperimentMRGSize {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		long start, end, duration;
		
		Double[][] dataSet = null;

		try {
			dataSet = HDBSCANStar.readInDataSet(args[0], ",");
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}

		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, Integer.parseInt(args[1]), new EuclideanDistance());
		MutualReachabilityGraph MRG = new MutualReachabilityGraph(dataSet, coreDistances, new EuclideanDistance(), Integer.parseInt(args[1]));
		
		System.out.println("DataSet: " + args[0] + ", Max MinPoints: " + args[1] + ", Size: " + MRG.numOfEdgesMRG);
	}
}
