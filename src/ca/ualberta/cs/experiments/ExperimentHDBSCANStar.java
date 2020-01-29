package ca.ualberta.cs.experiments;

import java.io.IOException;

import ca.ualberta.cs.SHM.HMatrix.HMatrix;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.Runner;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;
import ca.ualberta.cs.hdbscanstar.Runner.Environment;
import ca.ualberta.cs.main.CoreDistances;
import ca.ualberta.cs.util.DenseDataset;
import ca.ualberta.cs.util.SparseDataset;

public class ExperimentHDBSCANStar {

	public static void main(String[] args) {
		long start;
		
		HMatrix HMatrix = new HMatrix();

		Runner.parameters = Runner.checkInputParameters(args, HMatrix);

		String inputFile = Runner.parameters.inputFile.split("/")[Runner.parameters.inputFile.split("/").length - 1];
		
		Runner.environment = new Environment();

		
		try {
			if (Runner.parameters.sparse) {
				Runner.environment.dataset = new SparseDataset(Runner.parameters.inputFile, ",", Runner.parameters.distanceFunction);
			} else {
				Runner.environment.dataset = new DenseDataset(Runner.parameters.inputFile, ",", Runner.parameters.distanceFunction);
			}				

		} catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}

		if (Runner.parameters.minPoints > Runner.environment.dataset.length()) {	
			Runner.parameters.minPoints = Runner.environment.dataset.length();
		}

		
		// Prints data set, minPoints, Run
		System.out.print(Runner.parameters.inputFile + " " + Runner.parameters.minPoints);
		
		// Computes all the core-distances from 1 to minPoints
		long startcore = System.currentTimeMillis();
		Runner.environment.coreDistances = CoreDistances.calculateCoreDistances(
				Runner.environment.dataset, Runner.parameters.minPoints, Runner.parameters.distanceFunction);
		System.out.print(" " + (System.currentTimeMillis() - startcore));

		CoreDistances.coreDistancesToFile(Runner.environment.coreDistances, Runner.parameters.inputFile + "-" + Runner.parameters.minPoints + ".cd");
		
		start = System.currentTimeMillis();
		
		Experiments.outputDir = Runner.parameters.inputFile.subSequence(0, Runner.parameters.inputFile.lastIndexOf(inputFile)) + "visualization";
		
		// Constructs all the the MSTs.
		long mstTime = 0;
		long hierarchyTime = 0;
		
		long s = 0;
		
		for (int k = Runner.parameters.minPoints; k > 1; k--) {
			s = System.currentTimeMillis();
			UndirectedGraph mst = HDBSCANStar.constructMST(Runner.environment.dataset, Runner.environment.coreDistances, k, true, new EuclideanDistance());			
			mst.quicksortByEdgeWeight();
			mstTime += (System.currentTimeMillis() - s);
					
			s = System.currentTimeMillis();
			if (Runner.parameters.outputFiles) Experiments.computeOutputFiles(
					Runner.environment.dataset, Runner.environment.coreDistances, 
					mst, k, "ORI_" + inputFile, k, Runner.parameters.compactHierarchy, 
					Runner.parameters.minClusterSize);
			
			hierarchyTime += (System.currentTimeMillis() - s);
		}

		System.out.print(" " + mstTime + " " + hierarchyTime);
		
		System.out.println(" " + (System.currentTimeMillis() - start));
	}
}
