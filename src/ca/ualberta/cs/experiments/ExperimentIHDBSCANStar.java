package ca.ualberta.cs.experiments;

import java.io.IOException;

import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;
import ca.ualberta.cs.main.CoreDistances;
import ca.ualberta.cs.main.Prim;

public class ExperimentIHDBSCANStar {

	public static void main(String[] args) {
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
		
		// Computes all the core-distances from 1 to minPoints
//		long startcore = System.currentTimeMillis();
//		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, minPoints, new EuclideanDistance());
//		System.out.print(" " + (System.currentTimeMillis() - startcore));
		
		double[][] coreDistances = null;
		int[][] kNN = null;
		
		try {
			coreDistances = CoreDistances.fromFile(args[0] + "-" + args[1] + ".cd", minPoints, " ");
			kNN = CoreDistances.knnFromFile(args[0] + "-" + args[1] + ".knn", minPoints, " ");

			IncrementalHDBSCANStar.kNN = kNN;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		IncrementalHDBSCANStar.k = Integer.parseInt(args[1]);
		
		start = System.currentTimeMillis();
		
		// Computes the RNG
		long startRNG = System.currentTimeMillis();

		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, minPoints, Boolean.parseBoolean(args[3]), Boolean.parseBoolean(args[4]), Boolean.parseBoolean(args[5]), Boolean.parseBoolean(args[6]));
		System.out.print(" " + (System.currentTimeMillis() - startRNG));

		// Computes all the minPoints MSTs
		long startMSTs = System.currentTimeMillis();
		
		for (int k = minPoints; k >= 1; k--) {

			UndirectedGraph mst = Prim.constructMST(dataSet, coreDistances, k, false, RNG);			
			mst.quicksortByEdgeWeight();
			
			Experiments.writeMSTweight("IHDBSCAN", inputFile, k, mst);
			
			if (Boolean.parseBoolean(args[6])) Experiments.computeOutputFiles(dataSet, mst, k, "RNG_" + inputFile, k);
		}
		
		System.out.print(" " + (System.currentTimeMillis() - startMSTs));		
		
		end = System.currentTimeMillis();
		duration = end - start;
		
		// Data set, minPts, Time, RNG size
		System.out.println(" " + duration + " " + RNG.numOfEdges);
	}
}
