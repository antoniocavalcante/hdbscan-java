package ca.ualberta.cs.experiments;

import java.io.IOException;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;
import ca.ualberta.cs.main.CoreDistances;

public class ExperimentHDBSCANStar {

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
		
		try {
			coreDistances = CoreDistances.fromFile(args[0] + ".cd", minPoints, " ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		start = System.currentTimeMillis();
		
		//  Constructs all the the MST
		long startmst = System.currentTimeMillis();
		for (int k = minPoints; k >= 1; k--) {

			UndirectedGraph mst = HDBSCANStar.constructMST(dataSet, coreDistances, k, false, new EuclideanDistance());

			mst.quicksortByEdgeWeight();
			
			Experiments.writeMSTweight("HDBSCAN", inputFile, k, mst);
			
			if (Boolean.parseBoolean(args[3])) {
				Experiments.computeOutputFiles(dataSet, mst, k, "ORI_" + inputFile);
			}
		}
		
		System.out.print(" " + (System.currentTimeMillis() - startmst));
		
		end = System.currentTimeMillis();
		duration = end - start;
		System.out.println(" " + duration);
	}
}
