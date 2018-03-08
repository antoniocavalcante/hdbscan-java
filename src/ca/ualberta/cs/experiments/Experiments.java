package ca.ualberta.cs.experiments;

import static ca.ualberta.cs.hdbscanstar.HDBSCANStar.WARNING_MESSAGE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
			dataSet = HDBSCANStar.readInDataSet(file, ",");
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
		String mstFile = outputPrefix + ".mst";
		String separator = ",";

		Structure SHM = new Structure();
		HMatrix HMatrix = new HMatrix();

		HMatrix.setHMatrix(numPoints);

		SHM.setMST(mst);

		WrapInt lineCount = new WrapInt(0);

		try {
			clusters = HDBSCANStar.computeHierarchyAndClusterTree(mst, minPts, false, null, 
					hierarchyFile, treeFile, separator, 
					pointNoiseLevels, pointLastClusters, HDBSCANStarRunner.BOTH_OUT, HMatrix, lineCount);

		} catch (IOException ioe) {
			System.err.println("Error writing to hierarchy file or cluster tree file.");
			System.exit(-1);
		}

		SHM.setMatrix(HMatrix);
		SHM.setHDBSCANStarClusterTree(clusters);

		boolean infiniteStability = HDBSCANStar.propagateTree(clusters);

		int[] flatPartitioningSHM = HDBSCANStar.findProminentClustersSHM(clusters, HMatrix);


		//Output the flat clustering result:
		try ( BufferedWriter writer = new BufferedWriter(new FileWriter(partitionFile), 32678)) {
			if (infiniteStability)
				writer.write(WARNING_MESSAGE + "\n");

			for (int i = 0; i < flatPartitioningSHM.length-1; i++) {
				writer.write(flatPartitioningSHM[i] + ",");
			}
			writer.write(flatPartitioningSHM[flatPartitioningSHM.length-1] + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		writeMST(mst, mstFile);

		//Remove references to unneeded objects:
		mst = null;
	}

	public static void writeMST(UndirectedGraph MST, String fileName) {

		try ( BufferedWriter writer = new BufferedWriter(new FileWriter(fileName), 32678)) {

			for (int i = 0; i < MST.getNumEdges(); i++) {
				writer.write(MST.verticesA[i] + " " + MST.verticesB[i] + " " + MST.edgeWeights[i] + "\n");	
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
