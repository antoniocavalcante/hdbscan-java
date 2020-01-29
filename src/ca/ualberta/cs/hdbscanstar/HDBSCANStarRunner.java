package ca.ualberta.cs.hdbscanstar;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import ca.ualberta.cs.SHM.HMatrix.HMatrix;
import ca.ualberta.cs.SHM.Structure.Structure;
import ca.ualberta.cs.main.CoreDistances;
import ca.ualberta.cs.util.DenseDataset;

import static ca.ualberta.cs.hdbscanstar.HDBSCANStar.WARNING_MESSAGE;

/**
 * Entry point for the HDBSCAN* algorithm.
 * @author zjullion
 */
public class HDBSCANStarRunner extends Runner {

	/**
	 * Runs the HDBSCAN* algorithm given an input data set file and a value for minPoints and
	 * minClusterSize.  Note that the input file must be a comma-separated value (CSV) file, and
	 * that all of the output files will be CSV files as well.  The flags "file=", "minPts=",
	 * "minClSize=", "constraints=", and "distance_function=" should be used to specify the input 
	 * data set file, value for minPoints, value for minClusterSize, input constraints file, and 
	 * the distance function to use, respectively.
	 * @param args The input arguments for the algorithm
	 */
	public static void main(String[] args) {

		long overallStartTime = System.currentTimeMillis();

		Structure SHM = new Structure();
		HMatrix HMatrix = new HMatrix();

		// Parse input parameters from program arguments.
		parameters = checkInputParameters(args, HMatrix);

		// Store data set, core distances and nearest neighbors.
		environment = new Environment();
		
		System.out.println("Running HDBSCAN* on " + parameters.inputFile + " with minPts=" + parameters.minPoints + 
				", minClSize=" + parameters.minClusterSize + ", constraints=" + parameters.constraintsFile + 
				", compact=" + parameters.compactHierarchy + ", dist_function=" + parameters.distanceFunction.getName() +
				", outputExtension="+ parameters.outType);

		// Load input file.
		try {
			environment.dataset = new DenseDataset(parameters.inputFile, parameters.separator, parameters.distanceFunction);		
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}
		
		// Number of points in the data set.
		int numPoints = environment.dataset.length();

		
		// Check if it does not uses SHM later.
		if(parameters.outType.equals(VIS_OUT)) {
			HMatrix = null;
		} else {
			HMatrix.setHMatrix(numPoints);
		}

		// Load constraints.
		if (parameters.constraintsFile != null) {
			try {
				environment.constraints = HDBSCANStar.readInConstraints(parameters.constraintsFile, ",");
			}
			catch (IOException e) {
				System.err.println("Error reading constraints file.");
				System.exit(-1);
			}
		}

		// Compute core distances.
		long startTime = System.currentTimeMillis();
		environment.coreDistances = CoreDistances.calculateCoreDistances(environment.dataset, parameters.minPoints, parameters.distanceFunction);
		System.out.println("Time to compute core distances (ms): " + (System.currentTimeMillis() - startTime));

		// Compute Minimum Spanning Tree.
		startTime = System.currentTimeMillis();
		UndirectedGraph mst = HDBSCANStar.constructMST(environment.dataset, environment.coreDistances, parameters.minPoints, true, parameters.distanceFunction);
		mst.quicksortByEdgeWeight();

		System.out.println("Time to calculate MST (ms): " + (System.currentTimeMillis() - startTime));

		// Generating MST file (or updating structure) before freeing the MST data.
		// If some .SHM file is being created
		if(!parameters.outType.equals(VIS_OUT))
		{
			SHM.setMST(mst);
		}

		// If the .vis structure is used.
		if(!parameters.outType.equals(SHM_OUT))
		{
			try (FileOutputStream outFile = new FileOutputStream(parameters.MSTSerializableFile);
					ObjectOutputStream out2 = new ObjectOutputStream(outFile))
			{                
				out2.writeObject(mst);
			} 
			catch(IOException e)
			{
				System.out.println("An error ocurred while writing the MST serialized file");
				e.printStackTrace();
				System.exit(-1);
			}
		}

		double[] pointNoiseLevels = new double[numPoints];
		int[] pointLastClusters   = new int[numPoints];

		// Compute Hierarchy and Cluster Tree.
		WrapInt lineCount = new WrapInt(0);
		ArrayList<Cluster> clusters = null;
		try {
			startTime = System.currentTimeMillis();

			clusters = HDBSCANStar.computeHierarchyAndClusterTree(mst, parameters.minClusterSize,
					parameters.compactHierarchy, environment.constraints, parameters.hierarchyFile, 
					parameters.clusterTreeFile, ",", pointNoiseLevels, pointLastClusters, parameters.outType, HMatrix, lineCount);

			for(int i = 0; i < environment.coreDistances.length; i++)
			{
				HMatrix.getObjInstanceByID(i).setCoreDistance(environment.coreDistances[i][parameters.minPoints-1]);
			}

			System.out.println("Time to compute hierarchy and cluster tree (ms): " + (System.currentTimeMillis() - startTime));
		}
		catch (IOException ioe) {
			System.err.println("Error writing to hierarchy file or cluster tree file.");
			System.exit(-1);
		}

		//Remove references to unneeded objects:
		mst = null;

		//Propagate clusters:
		boolean infiniteStability = HDBSCANStar.propagateTree(clusters);

		//Compute final flat partitioning using just the SHM
		if(!parameters.outType.equals(VIS_OUT))
		{                    
			HMatrix.setInfiniteStability(infiniteStability);
			//Compute final flat partitioning:
			try {
				startTime = System.currentTimeMillis();
				int[] flatPartitioningSHM = HDBSCANStar.findProminentClustersSHM(clusters, HMatrix);

				if(!parameters.outType.equals(VIS_OUT))
				{
					for(int i = 0; i < flatPartitioningSHM.length; i++)
					{
						HMatrix.getObjInstanceByID(i).setHDBSCANPartition(flatPartitioningSHM[i]);
					}
				}

				//Output the flat clustering result:
				try ( BufferedWriter writer = new BufferedWriter(new FileWriter(parameters.partitionFile), 32678)) {
					if (infiniteStability)
						writer.write(WARNING_MESSAGE + "\n");

					for (int i = 0; i < flatPartitioningSHM.length-1; i++) {
						writer.write(flatPartitioningSHM[i] + ",");
					}
					writer.write(flatPartitioningSHM[flatPartitioningSHM.length-1] + "\n");
				}
				System.out.println("Time to find flat result"+/*using just the SHM structure:*/" (ms): " + (System.currentTimeMillis() - startTime));
			}
			catch (IOException ioe) {
				System.err.println("Error writing to partitioning file.");
				System.exit(-1);
			}

		}
		else // Comment if you wish to compare the time
		{
			// Compute final flat partitioning:
			try {
				startTime = System.currentTimeMillis();

				int[] flatPartitioning = HDBSCANStar.findProminentClusters(clusters, 1, parameters.hierarchyFile, parameters.partitionFile, 
						",", numPoints, infiniteStability);

				if(!parameters.outType.equals(VIS_OUT))
				{
					for(int i = 0; i < flatPartitioning.length; i++)
					{
						HMatrix.getObjInstanceByID(i).setHDBSCANPartition(flatPartitioning[i]);
					}
				}
				System.out.println("Time to find flat result (ms): " + (System.currentTimeMillis() - startTime));
			}
			catch (IOException ioe) {
				System.err.println("Error writing to partitioning file.");
				System.exit(-1);
			}
		}

		// Compute outlier scores for each point:
		try {
			startTime = System.currentTimeMillis();
			HDBSCANStar.calculateOutlierScores(clusters, pointNoiseLevels, pointLastClusters, 
					environment.coreDistances, parameters.minPoints, parameters.outlierScoreFile, ",", infiniteStability, parameters.outType, HMatrix);
			System.out.println("Time to compute outlier scores (ms): " + (System.currentTimeMillis() - startTime));
		}
		catch (IOException ioe) {
			System.err.println("Error writing to outlier score file.");
			System.exit(-1);
		}

		// Updating structure SHM
		if(!parameters.outType.equals(VIS_OUT)) {
			startTime = System.currentTimeMillis();
			SHM.setMatrix(HMatrix);
			SHM.setHDBSCANStarClusterTree(clusters);

			// Serializing SHM
			try {
				Kryo kryo = new Kryo();

				FileOutputStream outFile = new FileOutputStream(parameters.shmFile);
				Output output = new Output(new DeflaterOutputStream(outFile, new Deflater(Deflater.BEST_SPEED, true)));

				kryo.writeObject(output, SHM);
				output.close();

			} catch (Exception ex) {
				System.out.println("An error occurred while saving the .shm file, please check disk space and permissions.");
				System.exit(-1);
			}

			System.out.println("Time to save the .shm file (ms): " + (System.currentTimeMillis() - startTime));
		}

		//Generating .vis file
		if(!parameters.outType.equals(HDBSCANStarRunner.SHM_OUT)) {
			String out = "";
			if(!parameters.compactHierarchy)
			{
				out = "1\n";

			}
			else
			{
				out = "0\n";
			}
			out = out + parameters.inputFile +"\n";
			out = out + parameters.minClusterSize +"\n";
			out = out + parameters.minPoints + "\n";
			out = out + parameters.distanceFunction.getName() + "\n";
			out = (infiniteStability) ? out + "1\n": out + "0\n";
			out = out + Integer.toString(lineCount.getValue());

			try(BufferedWriter visualizationWriter = new BufferedWriter(new FileWriter(parameters.visualizationFile), 32678))
			{
				visualizationWriter.write(out);
			}
			catch(IOException e)
			{
				System.out.println("An error ocurred while writing the visualization file");
				System.exit(-1);
			}

			//In case of no .shm generated, generate a serializable version of the clusterTree
			try (FileOutputStream outFile = new FileOutputStream(parameters.clusterTreeSerializableFile);
					ObjectOutputStream out2 = new ObjectOutputStream(outFile))
			{                
				out2.writeObject(clusters);
			} 
			catch(IOException e)
			{
				System.out.println("An error ocurred while writing the cluster Tree serialized file");
				System.exit(-1);
			}


		}

		System.out.println("Overall runtime (ms): " + (System.currentTimeMillis() - overallStartTime));
	}
}