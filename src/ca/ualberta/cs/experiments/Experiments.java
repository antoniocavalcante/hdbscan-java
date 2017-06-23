package ca.ualberta.cs.experiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import ca.ualberta.cs.SHM.HMatrix.HMatrix;
import ca.ualberta.cs.SHM.Structure.Structure;
import ca.ualberta.cs.hdbscanstar.Cluster;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.HDBSCANStarRunner;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;
import ca.ualberta.cs.hdbscanstar.HDBSCANStarRunner.WrapInt;

public class Experiments {


	public static double[][] loadData(String file) {
		double[][] dataSet = null;

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

	@SuppressWarnings("unused")
	public static void computeOutputFiles(double[][] dataSet, double[][] coreDistances, UndirectedGraph mst, int minPts, String inputFile, int label) {

		int numPoints = dataSet.length;

		File output = new File("output");

		// if the directory does not exist, create it
		if (!output.exists()) output.mkdir();

		String outputPrefix = "output/" + label + inputFile;

		double[] pointNoiseLevels = new double[numPoints];
		int[] pointLastClusters = new int[numPoints];

		ArrayList<Cluster> clusters = null;

		String hierarchyFile = outputPrefix + ".hierarchy";
		String treeFile = outputPrefix + ".tree";
		String visFile = outputPrefix + ".vis";
		String shmFile = outputPrefix + ".shm";
		String partitionFile = outputPrefix + ".partition";
		String separator = ",";

		Structure SHM = new Structure();
		HMatrix HMatrix = new HMatrix();

		HMatrix.setHMatrix(numPoints);

		SHM.setMST(mst);

		WrapInt lineCount = new WrapInt(0);

		try {
			clusters = HDBSCANStar.computeHierarchyAndClusterTree(mst, minPts, true, null, 
					hierarchyFile, treeFile, separator, 
					pointNoiseLevels, pointLastClusters, HDBSCANStarRunner.SHM_OUT, HMatrix, lineCount);

			for(int i = 0; i < coreDistances.length; i++) {
				HMatrix.getObjInstanceByID(i).setCoreDistance(coreDistances[i][minPts-1]);
			}

		} catch (IOException ioe) {
			System.err.println("Error writing to hierarchy file or cluster tree file.");
			System.exit(-1);
		}

		long startTime = System.currentTimeMillis();

		SHM.setMatrix(HMatrix);
		SHM.setHDBSCANStarClusterTree(clusters);

		//Serializing .SHM
		try (FileOutputStream outFile = new FileOutputStream(shmFile);
				ObjectOutputStream out = new ObjectOutputStream(outFile)) {
			out.writeObject(SHM);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("An error occurred while saving the .shm file, please check disk space and permissions.");
			System.exit(-1);
		}

		System.out.println(" " + (System.currentTimeMillis() - startTime));



		//Remove references to unneeded objects:
		mst = null;

		//Propagate clusters:
		//		boolean infiniteStability = HDBSCANStar.propagateTree(clusters);

		//		//Compute final flat partitioning:
		//		try {
		//
		//			HDBSCANStar.findProminentClusters(clusters, hierarchyFile, partitionFile, separator, numPoints, infiniteStability);
		//		
		//		} catch (IOException ioe) {
		//			System.err.println("Error writing to partitioning file.");
		//			System.exit(-1);
		//		}
	}
}
