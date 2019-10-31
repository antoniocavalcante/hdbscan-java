package ca.ualberta.cs.experiments;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import ca.ualberta.cs.SHM.HMatrix.HMatrix;
import ca.ualberta.cs.hdbscanstar.HDBSCANStarRunner;
import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;
import ca.ualberta.cs.hdbscanstar.HDBSCANStarRunner.HDBSCANStarParameters;
import ca.ualberta.cs.main.CoreDistances;
import ca.ualberta.cs.main.Prim;
import ca.ualberta.cs.util.Dataset;
import ca.ualberta.cs.util.DenseDataset;
import ca.ualberta.cs.util.SparseDataset;

public class ExperimentIHDBSCANStar {

	public static void main(String[] args) throws IOException {
		long start, end, duration;
		
//		Structure SHM = new Structure();
		HMatrix HMatrix = new HMatrix();

		//Parse input parameters from program arguments:
		HDBSCANStarParameters parameters = HDBSCANStarRunner.checkInputParameters(args, HMatrix);
		
		String inputFile = parameters.inputFile.split("/")[parameters.inputFile.split("/").length - 1];
		
		Dataset dataSet = null;
		
		try {

			if (parameters.sparse) {
				dataSet = new SparseDataset(parameters.inputFile, ",", parameters.distanceFunction);
			} else {
				dataSet = new DenseDataset(parameters.inputFile, ",", parameters.distanceFunction);
			}				

		} catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.out.println(ioe.toString());
			System.exit(-1);
		}
				
//		int minPoints = Integer.parseInt(args[1]);
		
		if (parameters.minPoints > dataSet.length()) {	
			parameters.minPoints = dataSet.length();
		}

		// Prints data set, minPoints, Run
		// System.out.print(args[0] + " " + args[1] + " " + args[2]);
		System.out.print(parameters.inputFile + " " + parameters.minPoints + " " + parameters.runNumber);
		
		// Computes all the core-distances from 1 to minPoints
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		Path path = Paths.get(parameters.inputFile.replace(inputFile, "progress.json"));
		Path path = Paths.get(parameters.inputFile.subSequence(0, parameters.inputFile.lastIndexOf(inputFile)) + "progress.json");
		
		JsonObject progress = new JsonObject();
		progress.addProperty("stage", "core-distances");
		progress.addProperty("message", "Computing core-distances...");
		
		JsonObject state = new JsonObject();
		state.addProperty("current", 0);
		state.addProperty("end", 1);

		progress.add("state", state);
		
		String json = gson.toJson(progress);
		Files.write(json.getBytes(), path.toFile());
		
		long startcore = System.currentTimeMillis();
		double[][] coreDistances = CoreDistances.calculateCoreDistances(dataSet, parameters.minPoints, parameters.distanceFunction);
		System.out.print(" " + (System.currentTimeMillis() - startcore));

		CoreDistances.coreDistancesToFile(coreDistances, parameters.inputFile + "-" + parameters.minPoints + ".cd");
		CoreDistances.kNNToFile(CoreDistances.kNN, parameters.inputFile + "-" + parameters.minPoints + ".knn");
		
		progress.addProperty("stage", "rng");
		progress.addProperty("message", "Computing RNG...");

		state.addProperty("current", 0);
		state.addProperty("end", 1);

		progress.add("state", state);
		
		json = gson.toJson(progress);
		Files.write(json.getBytes(), path.toFile());
		
		start = System.currentTimeMillis();
		
		// Computes the RNG
		long startRNG = System.currentTimeMillis();
		
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, 
				parameters.distanceFunction, parameters.minPoints, parameters.RNGSmart, 
				parameters.RNGNaive, parameters.RNGIncremental, parameters.index);
		
		RNG.toFile(parameters.inputFile + "-" + parameters.minPoints + ".rng");
		
		System.out.print(" " + (System.currentTimeMillis() - startRNG));
		
		// Constructs all the the MSTs.
		long mstTime = 0;
		long hierarchyTime = 0;
		
		long s = 0;

//		Experiments.outputDir = parameters.inputFile.replace(inputFile, "visualization");
		Experiments.outputDir = parameters.inputFile.subSequence(0, parameters.inputFile.lastIndexOf(inputFile)) + "visualization";
		
		progress.addProperty("stage", "hierarchies");
		progress.addProperty("message", "Computing hierarchies...");

		state.addProperty("current", 2);
		state.addProperty("end", parameters.minPoints);

		progress.add("state", state);
		
		json = gson.toJson(progress);
		Files.write(json.getBytes(), path.toFile());
//		int k = parameters.minPoints;
		for (int k = parameters.minPoints; k > 1; k--) {
			
			s = System.currentTimeMillis();
			UndirectedGraph mst = Prim.constructMST(dataSet, coreDistances, k, true, RNG);			
			mst.quicksortByEdgeWeight();
			mstTime += (System.currentTimeMillis() - s);
			
//			System.out.println();
//			for (int i = 0; i < mst.getNumEdges(); i++) {
//				System.out.println(mst.getFirstVertexAtIndex(i) + " " + mst.getSecondVertexAtIndex(i) + " : " + mst.getEdgeWeightAtIndex(i));
//			}
			
			s = System.currentTimeMillis();
			if (parameters.outputFiles) Experiments.computeOutputFiles(dataSet, coreDistances, mst, k, "RNG_" + inputFile, k, parameters.compactHierarchy, parameters.minClusterSize);
			hierarchyTime += (System.currentTimeMillis() - s);
			
			state.addProperty("current", parameters.minPoints - k + 2);
			progress.add("state", state);
			json = gson.toJson(progress);
			Files.write(json.getBytes(), path.toFile());

		}
		
		System.out.print(" " + mstTime + " " + hierarchyTime);		
		
		end = System.currentTimeMillis();
		duration = end - start;
		
		// Data set, minPts, Time, RNG size
		System.out.println(" " + duration + " " + RNG.numOfEdges);
		
		progress.addProperty("stage", "meta-clustering");
		progress.addProperty("message", "Meta-clustering...");

		state.addProperty("current", 0);
		state.addProperty("end", 1);

		progress.add("state", state);
		
		json = gson.toJson(progress);
		Files.write(json.getBytes(), path.toFile());

	}
}