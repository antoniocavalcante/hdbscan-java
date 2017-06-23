package ca.ualberta.cs.experiments;

import java.io.IOException;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;


public class ExperimentMST {
	
	public static void main(String[] args) throws IOException {
		long start, end, duration;
		
		double[][] dataSet = null;

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
		
		//  Constructs the MST w.r.t Euclidean Distance (e.g. minPoints = 1)
		long startmst = System.currentTimeMillis();
		UndirectedGraph mst = HDBSCANStar.constructMST(dataSet, coreDistances, 1, false, new EuclideanDistance());
		mst.quicksortByEdgeWeight();
		System.out.print(" " + (System.currentTimeMillis() - startmst));

		// Updates this the edges weights of the MST w.r.t. minPoints = k
		long startupdate = System.currentTimeMillis();
		for (int k = minPoints; k >= 1; k--) {
			mst.updateWeights(dataSet, coreDistances, new EuclideanDistance(), k);
			mst.quicksortByEdgeWeight();

			Experiments.writeMSTweight("MST", inputFile, k, mst);
			
			if (Boolean.parseBoolean(args[3])) {
				Experiments.computeOutputFiles(dataSet, coreDistances, mst, k, "MST_" + inputFile, k);
				mst.restoreEdges();
			}
		}
		System.out.print(" " + (System.currentTimeMillis() - startupdate));

		end = System.currentTimeMillis();
		duration = end - start;
		System.out.println(" " + duration);
	}
}
