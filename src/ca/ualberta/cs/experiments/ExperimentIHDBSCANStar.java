package ca.ualberta.cs.experiments;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import ca.ualberta.cs.SHM.HMatrix.HMatrix;
import ca.ualberta.cs.hdbscanstar.Runner;
import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;
import ca.ualberta.cs.hdbscanstar.Runner.Environment;
import ca.ualberta.cs.main.CoreDistances;
import ca.ualberta.cs.main.Prim;
import ca.ualberta.cs.util.DenseDataset;
import ca.ualberta.cs.util.SparseDataset;

public class ExperimentIHDBSCANStar {

	public static void main(String[] args) throws IOException {
		long start, end, duration;

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

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		Path path = Paths.get(Runner.parameters.inputFile.subSequence(0, Runner.parameters.inputFile.lastIndexOf(inputFile)) + "progress.json");

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
		Runner.environment.coreDistances = CoreDistances.calculateCoreDistances(
				Runner.environment.dataset, Runner.parameters.minPoints, Runner.parameters.distanceFunction);
		System.out.print(" " + (System.currentTimeMillis() - startcore));

		CoreDistances.coreDistancesToFile(Runner.environment.coreDistances, Runner.parameters.inputFile + "-" + Runner.parameters.minPoints + ".cd");
		CoreDistances.kNNToFile(CoreDistances.kNN, Runner.parameters.inputFile + "-" + Runner.parameters.minPoints + ".knn");
		
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
				
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(
				Runner.parameters.distanceFunction, Runner.parameters.minPoints, Runner.parameters.RNGSmart, 
				Runner.parameters.RNGNaive, Runner.parameters.RNGIncremental, Runner.parameters.index);
				
		RNG.toFile(Runner.parameters.inputFile + "-" + Runner.parameters.minPoints + ".rng");

		System.out.print(" " + (System.currentTimeMillis() - startRNG));

		// Constructs all the the MSTs.
		long mstTime = 0;
		long hierarchyTime = 0;

		long s = 0;

		//		Experiments.outputDir = parameters.inputFile.replace(inputFile, "visualization");
		Experiments.outputDir = Runner.parameters.inputFile.subSequence(0, Runner.parameters.inputFile.lastIndexOf(inputFile)) + "visualization";

		progress.addProperty("stage", "hierarchies");
		progress.addProperty("message", "Computing hierarchies...");

		state.addProperty("current", 2);
		state.addProperty("end", Runner.parameters.minPoints);

		progress.add("state", state);

		json = gson.toJson(progress);
		Files.write(json.getBytes(), path.toFile());

		for (int k = Runner.parameters.minPoints; k > 1; k--) {

			s = System.currentTimeMillis();
			UndirectedGraph mst = Prim.constructMST(Runner.environment.dataset, Runner.environment.coreDistances, k, true, RNG);			
			mst.quicksortByEdgeWeight();
			mstTime += (System.currentTimeMillis() - s);

			s = System.currentTimeMillis();
			if (Runner.parameters.outputFiles) Experiments.computeOutputFiles(
					Runner.environment.dataset, Runner.environment.coreDistances, 
					mst, k, "RNG_" + inputFile, k, Runner.parameters.compactHierarchy, 
					Runner.parameters.minClusterSize);
			hierarchyTime += (System.currentTimeMillis() - s);

			state.addProperty("current", Runner.parameters.minPoints - k + 2);
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