package ca.ualberta.cs.experiments;

import java.io.IOException;

import ca.ualberta.cs.SHM.HMatrix.HMatrix;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.HDBSCANStarRunner;
import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;
import ca.ualberta.cs.hdbscanstar.HDBSCANStarRunner.HDBSCANStarParameters;
import ca.ualberta.cs.main.CoreDistances;
import ca.ualberta.cs.main.Prim;

public class ExperimentIHDBSCANStar {

	public static void main(String[] args) {
		long start, end, duration;
		
//		Structure SHM = new Structure();
		HMatrix HMatrix = new HMatrix();

		//Parse input parameters from program arguments:
		HDBSCANStarParameters parameters = HDBSCANStarRunner.checkInputParameters(args, HMatrix);
		
		String inputFile = parameters.inputFile.split("/")[parameters.inputFile.split("/").length - 1];
		
		double[][] dataSet = null;
		
		try {
			dataSet = HDBSCANStar.readInDataSet(parameters.inputFile, ",");
		} catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.out.println(ioe.toString());
			System.exit(-1);
		}
				
//		int minPoints = Integer.parseInt(args[1]);
		int minPoints = parameters.minPoints;
		
		if (minPoints > dataSet.length) {	
			minPoints = dataSet.length;
		}

		// Prints data set, minPoints, Run
		// System.out.print(args[0] + " " + args[1] + " " + args[2]);
		System.out.print(parameters.inputFile + " " + parameters.minPoints + " " + parameters.runNumber);
		
		// Computes all the core-distances from 1 to minPoints

		long startcore = System.currentTimeMillis();
		double[][] coreDistances = CoreDistances.calculateCoreDistances(dataSet, parameters.minPoints, parameters.distanceFunction);
		System.out.print(" " + (System.currentTimeMillis() - startcore));
		
//		double[][] coreDistances = null;
//		int[][] kNN = null;
//		
//		try {
//			coreDistances = CoreDistances.fromFile(args[0] + "-" + args[8] + ".cd", minPoints, " ");
//			kNN = CoreDistances.knnFromFile(args[0] + "-" + args[8] + ".knn", minPoints, " ");
//
//			IncrementalHDBSCANStar.kNN = kNN;
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		IncrementalHDBSCANStar.k = Integer.parseInt(args[1]);
				
		start = System.currentTimeMillis();
		
		// Computes the RNG
		long startRNG = System.currentTimeMillis();

		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, 
				parameters.distanceFunction, parameters.minPoints, parameters.RNGSmart, 
				parameters.RNGNaive, parameters.RNGIncremental, parameters.index);
		
		System.out.print(" " + (System.currentTimeMillis() - startRNG));
		
		// Constructs all the the MSTs.
		long mstTime = 0;
		long hierarchyTime = 0;
		
		long s = 0;
		
		for (int k = parameters.minPoints; k > 1; k--) {
			
			s = System.currentTimeMillis();
			UndirectedGraph mst = Prim.constructMST(dataSet, coreDistances, k, false, RNG);			
			mst.quicksortByEdgeWeight();
			mstTime += (System.currentTimeMillis() - s);
		
			s = System.currentTimeMillis();
			if (parameters.outputFiles) Experiments.computeOutputFiles(dataSet, coreDistances, mst, k, "RNG_" + inputFile, k, parameters.compactHierarchy);
			hierarchyTime += (System.currentTimeMillis() - s);			
		}
		
		System.out.print(" " + mstTime + " " + hierarchyTime);		
		
		end = System.currentTimeMillis();
		duration = end - start;
		
		// Data set, minPts, Time, RNG size
		System.out.println(" " + duration + " " + RNG.numOfEdges);
	}
}