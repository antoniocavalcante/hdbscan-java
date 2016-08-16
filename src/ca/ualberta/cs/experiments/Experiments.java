package ca.ualberta.cs.experiments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import ca.ualberta.cs.hdbscanstar.Cluster;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;

public class Experiments {
	
	
	public static Double[][] loadData(String file) {
		Double[][] dataSet = null;

		try {
			dataSet = HDBSCANStar.readInDataSet(file, " ");
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}

		return dataSet;
	}
	
	public static void writeMSTweight(String method, String file, int minPoints, UndirectedGraph MST) {
		
		String f = method + ".w";
				
		try {
			Files.write(Paths.get(f), (file + " " + Integer.toString(minPoints) + " " + Double.toString(MST.getTotalWeight()) + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void computeOutputFiles(Double[][] dataSet, UndirectedGraph mst, int minPts, String inputFile) {
		
		int numPoints = dataSet.length;
		
		String outputPrefix = "tmp/" + minPts + inputFile;
		
		double[] pointNoiseLevels = new double[numPoints];
		int[] pointLastClusters = new int[numPoints];
		
		ArrayList<Cluster> clusters = null;
		
		String hierarchyFile = outputPrefix + ".hierarchy";
		String treeFile = outputPrefix + ".tree";
		String visFile = outputPrefix + ".vis";
		String partitionFile = outputPrefix + ".partition";
		String separator = ",";
		try {

			clusters = HDBSCANStar.computeHierarchyAndClusterTree(mst, minPts, true, null, hierarchyFile, treeFile, separator, pointNoiseLevels, pointLastClusters, visFile);
		
		} catch (IOException ioe) {
			System.err.println("Error writing to hierarchy file or cluster tree file.");
			System.exit(-1);
		}
		
		//Remove references to unneeded objects:
		mst = null;
		
		//Propagate clusters:
		boolean infiniteStability = HDBSCANStar.propagateTree(clusters);

		//Compute final flat partitioning:
		try {

			HDBSCANStar.findProminentClusters(clusters, hierarchyFile, partitionFile, separator, numPoints, infiniteStability);
		
		} catch (IOException ioe) {
			System.err.println("Error writing to partitioning file.");
			System.exit(-1);
		}
	}
}
