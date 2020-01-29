package ca.ualberta.cs.hdbscanstar;

import static ca.ualberta.cs.hdbscanstar.HDBSCANStar.WARNING_MESSAGE;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ca.ualberta.cs.SHM.HMatrix.HMatrix;
import ca.ualberta.cs.SHM.Structure.Structure;
import ca.ualberta.cs.main.CoreDistances;
import ca.ualberta.cs.main.Prim;
import ca.ualberta.cs.util.DenseDataset;

/**
 * Entry point for the HDBSCAN* algorithm.
 * @author zjullion
 */
public class RNGHDBSCANStarRunner extends Runner {

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

		//Parse input parameters from program arguments:
		HDBSCANStarRunner.parameters = Runner.checkInputParameters(args, HMatrix);

		// Store data set, core distances and nearest neighbors.
		HDBSCANStarRunner.environment = new Environment();
				
		System.out.println("Running HDBSCAN* on " + Runner.parameters.inputFile + " with minPts=" + Runner.parameters.minPoints + 
				", minClSize=" + Runner.parameters.minClusterSize + ", constraints=" + Runner.parameters.constraintsFile + 
				", compact=" + Runner.parameters.compactHierarchy + ", dist_function=" + Runner.parameters.distanceFunction.getName());
		
		// Load input file.
		try {
			HDBSCANStarRunner.environment.dataset = new DenseDataset(HDBSCANStarRunner.parameters.inputFile, ",", Runner.parameters.distanceFunction);
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}
		
		int numPoints = Runner.environment.dataset.length();

		if(HDBSCANStarRunner.parameters.outType.equals(HDBSCANStarRunner.VIS_OUT)) {
			HMatrix = null;
		} else {
			HMatrix.setHMatrix(numPoints);
		}
		
		// Read in constraints:
		ArrayList<Constraint> constraints = null;
		if (HDBSCANStarRunner.parameters.constraintsFile != null) {
			try {
				constraints = HDBSCANStar.readInConstraints(HDBSCANStarRunner.parameters.constraintsFile, ",");
			}
			catch (IOException e) {
				System.err.println("Error reading constraints file.");
				System.exit(-1);
			}
		}

		//Compute core distances:
		long startTime = System.currentTimeMillis();
		HDBSCANStarRunner.environment.coreDistances = CoreDistances.calculateCoreDistances(HDBSCANStarRunner.environment.dataset, Runner.parameters.minPoints, Runner.parameters.distanceFunction);
		System.out.println("Time to compute core distances (ms): " + (System.currentTimeMillis() - startTime));
		
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(HDBSCANStarRunner.parameters.distanceFunction, Runner.parameters.minPoints, 1, "WS", true, false, false, true);
		
		UndirectedGraph mst;

		for (int k = Runner.parameters.minPoints; k > 1; k--) {
			
			// Resets HMatrix if SHM output.
			if(!HDBSCANStarRunner.parameters.outType.equals(HDBSCANStarRunner.VIS_OUT)) {
				HMatrix.reset();
				HMatrix.setHMatrix(numPoints);
			}
			
			// Compute minimum spanning tree.
			startTime = System.currentTimeMillis();

			System.out.println("Computing MST for minPts = " + k);

			mst = Prim.constructMST(HDBSCANStarRunner.environment.dataset, Runner.environment.coreDistances, k, false, RNG);

			System.out.println("Time to calculate MST (ms): " + (System.currentTimeMillis() - startTime));

			double[] pointNoiseLevels = new double[numPoints];
			int[] pointLastClusters = new int[numPoints];

			// Compute hierarchy and cluster tree:
			ArrayList<Cluster> clusters = null;
			WrapInt lineCount = new WrapInt(0);
			
			try {
				startTime = System.currentTimeMillis();
				clusters = HDBSCANStar.computeHierarchyAndClusterTree(mst, Runner.parameters.minClusterSize, 
						HDBSCANStarRunner.parameters.compactHierarchy, constraints, k + Runner.parameters.hierarchyFile, 
						k + Runner.parameters.clusterTreeFile, ",", pointNoiseLevels, pointLastClusters, k + Runner.parameters.visualizationFile, HMatrix, lineCount);
				System.out.println("Time to compute hierarchy and cluster tree (ms): " + (System.currentTimeMillis() - startTime));
			}
			catch (IOException ioe) {
				System.err.println("Error writing to hierarchy file or cluster tree file.");
				System.exit(-1);
			}
			
			// Propagate clusters:
			boolean infiniteStability = HDBSCANStar.propagateTree(clusters);

			//Compute final flat partitioning using just the SHM
			if(!HDBSCANStarRunner.parameters.outType.equals(HDBSCANStarRunner.VIS_OUT))
			{                    
				HMatrix.setInfiniteStability(infiniteStability);
				//Compute final flat partitioning:
				try {
					startTime = System.currentTimeMillis();
					int[] flatPartitioningSHM = HDBSCANStar.findProminentClustersSHM(clusters, HMatrix);

					if(!HDBSCANStarRunner.parameters.outType.equals(HDBSCANStarRunner.VIS_OUT))
					{
						for(int i = 0; i < flatPartitioningSHM.length; i++)
						{
							HMatrix.getObjInstanceByID(i).setHDBSCANPartition(flatPartitioningSHM[i]);
						}
					}

					//Output the flat clustering result:
					try ( BufferedWriter writer = new BufferedWriter(new FileWriter(HDBSCANStarRunner.parameters.partitionFile), 32678)) {
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

					int[] flatPartitioning = HDBSCANStar.findProminentClusters(clusters, 1, Runner.parameters.hierarchyFile, Runner.parameters.partitionFile, 
							",", numPoints, infiniteStability);

					if(!HDBSCANStarRunner.parameters.outType.equals(HDBSCANStarRunner.VIS_OUT))
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
						HDBSCANStarRunner.environment.coreDistances, k, Runner.parameters.outlierScoreFile, ",", infiniteStability, Runner.parameters.outType, HMatrix);
				System.out.println("Time to compute outlier scores (ms): " + (System.currentTimeMillis() - startTime));
			}
			catch (IOException ioe) {
				System.err.println("Error writing to outlier score file.");
				System.exit(-1);
			}			
		}

		//Remove references to unneeded objects:
		HDBSCANStarRunner.environment.dataset = null;

		//Remove references to unneeded objects:
		mst = null;

		System.out.println("Overall runtime (ms): " + (System.currentTimeMillis() - overallStartTime));
	}
}