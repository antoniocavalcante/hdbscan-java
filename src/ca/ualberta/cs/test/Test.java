package ca.ualberta.cs.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.hdbscanstar.MutualReachabilityGraph;
import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;

/**
 * @author toni
 *
 */
public class Test {

	public static void main(String[] args) {
		Double[][] dataSet = null;

		try {
			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/experiments/data#1/2d-128c-no1.dat", " ");
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}

		int numPoints = dataSet.length;
		System.out.println("Dataset size: " + numPoints);
		System.out.println("Dimensions: " + dataSet[0].length);

		performance(dataSet, 100);
		//		testRNG3(dataSet, 30);
		//		correct(dataSet, 9);
	}

	public static void weights(Double[][] dataSet, int k) throws IOException{
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, k, new EuclideanDistance());
		MutualReachabilityGraph MRG = new MutualReachabilityGraph(dataSet, coreDistances, new EuclideanDistance(), k);
		MRG.quicksortByEdgeWeight();
		List<String> lines = new ArrayList<String>();

		for (int i = k; i > 1; i--) {
			MRG.updateWeights(dataSet, coreDistances, new EuclideanDistance(), i);

			for (int j = 0; j < MRG.sortedEdges.length; j++) {
				lines.add(Integer.toString(j) + " " + Double.toString(MRG.weights[MRG.sortedEdges[j]]));
			}

			Files.write(Paths.get("w" + i), lines);
			MRG.bubbleSort();
			lines = new ArrayList<String>();
		}
	}

	public static void coreDistances(Double[][] dataSet, int k){
		double[][] coreDistances2 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, k, new EuclideanDistance());

		for (int j = 1; j <= k; j++) {
			double[] coreDistances = HDBSCANStar.calculateCoreDistances(dataSet, j, new EuclideanDistance());
			for (int i = 0; i < coreDistances.length; i++) {
				if (coreDistances[i] != coreDistances2[i][j - 1]) {
					System.out.println("FAIL! MinPts = " + j);
					break;
				}
			}
		}
	}

	public static void testUpdate2(Double[][] dataSet, int k1, int k2){
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, k1, new EuclideanDistance());
		MutualReachabilityGraph MRG = new MutualReachabilityGraph(dataSet, coreDistances, new EuclideanDistance(), k1);
		System.out.println("1st weight: " + MRG.weights[10]);

		MRG.updateWeights(dataSet, coreDistances, new EuclideanDistance(), k2);

		System.out.println("1st weight: " + MRG.weights[10]);
	}



	/**
	 * Compares the correct Minimum Spanning Tree and the generated from the Relative
	 * Neighborhood Graph, and check if they are the same.
	 * 
	 * @param dataSet The data set.
	 * @param maxK maximum Value of MinPoints.
	 */
	public static void correct(Double[][] dataSet, int maxK) {

		/*
		 * Maximum minPoints cannot be higher that the size of the data set 
		 */
		if (maxK > dataSet.length) maxK = dataSet.length;

		/*
		 * Prim's Algorithm from the Mutual Reachability Graph 
		 */
		double[][] coreDistances1 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		UndirectedGraph mst1 = HDBSCANStar.constructMST(dataSet, coreDistances1,  maxK, false, new EuclideanDistance());
		mst1.quicksortByEdgeWeight();

		/*
		 * Kruskal from the Relative Neighborhood Graph 
		 */
		double[][] coreDistances2 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, 0);
		UndirectedGraph mst2 = IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances2, false, new EuclideanDistance(), maxK);

		correct(mst1, mst2);
	}


	/**
	 * Checks if two Minimum Spanning Trees are the same.
	 * 
	 * @param mst1
	 * @param mst2
	 * @return The number of different edges from the two MSTs.
	 */
	public static int correct(UndirectedGraph mst1, UndirectedGraph mst2) {
		int fails = 0;
		double sum1 = 0;
		double sum2 = 0;
		boolean debug = false;

		for (int i = 0; i < mst1.getNumEdges(); i++) {
			sum1 += mst1.getEdgeWeightAtIndex(i);
			sum2 += mst2.getEdgeWeightAtIndex(i);

			if (debug) {
				System.out.print(mst1.getEdgeWeightAtIndex(i) + "\t" + mst2.getEdgeWeightAtIndex(i) + "\t");
				System.out.print(mst1.getFirstVertexAtIndex(i) + "\t" + mst1.getSecondVertexAtIndex(i) + "\t");
				System.out.print(mst2.getFirstVertexAtIndex(i) + "\t" + mst2.getSecondVertexAtIndex(i) + "\t");
				System.out.println(mst1.getEdgeWeightAtIndex(i) == mst2.getEdgeWeightAtIndex(i));

				if (mst2.getFirstVertexAtIndex(i) == mst2.getSecondVertexAtIndex(i)) {
					System.out.println("ERROR! SAME SOURCE AND DESTINATION!");
					break;
				}
			}

			if (mst1.getEdgeWeightAtIndex(i) != mst2.getEdgeWeightAtIndex(i)) {
				fails++;
			}
		}

		System.out.println("\t MST #1 \t MST #2");
		System.out.println(sum1 + "\t" + sum2);
		System.out.println("Number of edges MST #1: " + mst1.getNumEdges());
		System.out.println("Number of edges MST #2: " + mst2.getNumEdges());
		System.out.println();
		System.out.println("Fails: " + fails);
		return fails;
	}

	public static void testRNG(Double[][] dataSet, int maxK){
		double[][] coreDistances2 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, 0);

		for (int k = maxK; k > 0; k--) {
			double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, k, new EuclideanDistance());
			UndirectedGraph mst1 = HDBSCANStar.constructMST(dataSet, coreDistances, maxK, false, new EuclideanDistance());
			mst1.quicksortByEdgeWeight();

			RNG.updateWeights(dataSet, coreDistances2, new EuclideanDistance(), k);
			//			printGraph(RNG, dataSet);
			UndirectedGraph mst2 = IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances2, false, new EuclideanDistance(), maxK);

			correct(mst1, mst2);
		}
	}

	public static void testRNG2(Double[][] dataSet, int maxK){
		double[][] coreDistances2 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, 0);

		System.out.println(RNG.numOfEdgesMRG);
		printGraph(RNG, dataSet);

	}	

	public static void testRNG3(Double[][] dataSet, int maxK){
		double[][] coreDistances2 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());

		long start = System.currentTimeMillis();
		RelativeNeighborhoodGraph RNG1 = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK);
		System.out.println("RNG 1: " + (System.currentTimeMillis() - start));
		RNG1.timSort();

		start = System.currentTimeMillis();
		RelativeNeighborhoodGraph RNG2 = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, 0);
		System.out.println("RNG 2: " + (System.currentTimeMillis() - start));
		RNG2.timSort();

		//		printGraphs(RNG1, RNG2, dataSet);
	}


	public static void printGraph(RelativeNeighborhoodGraph RNG, Double[][] dataSet){
		//		for (int i = 0; i < dataSet.length; i++) {
		//			System.out.println(i + ": " + dataSet[i][0] + "," + dataSet[i][1]);
		//		}

		for (int i = 0; i < RNG.numOfEdgesMRG; i++) {
			System.out.println("(" + RNG.edgesA[i] + ", " + RNG.edgesB[i] + ") : " + RNG.weights[i] + "\t " + RNG.inMST.get(i));
		}
	}

	public static void printGraphs(RelativeNeighborhoodGraph RNG1, RelativeNeighborhoodGraph RNG2, Double[][] dataSet){
		//		for (int i = 0; i < dataSet.length; i++) {
		//			System.out.println(i + ": " + dataSet[i][0] + "," + dataSet[i][1]);
		//		}

		System.out.println("RNG #1: " + RNG1.numOfEdgesMRG);
		System.out.println("RNG #2: " + RNG2.numOfEdgesMRG);

		int m = Math.max(RNG1.numOfEdgesMRG, RNG2.numOfEdgesMRG);
		double w1 = 0;
		double w2 = 0;

		for (int i = 0; i < m; i++) {
			if (i < RNG1.numOfEdgesMRG){
				System.out.print("(" + RNG1.edgesA[RNG1.sortedEdges[i]] + ", " + RNG1.edgesB[RNG1.sortedEdges[i]] + ") : " + RNG1.weights[RNG1.sortedEdges[i]] + "\t\t");
				w1 += RNG1.weights[RNG1.sortedEdges[i]];
			}
			if (i < RNG2.numOfEdgesMRG){
				System.out.print("(" + RNG2.edgesA[RNG2.sortedEdges[i]] + ", " + RNG2.edgesB[RNG2.sortedEdges[i]] + ") : " + RNG2.weights[RNG2.sortedEdges[i]]);
				w2 += RNG2.weights[RNG2.sortedEdges[i]];
			}
			System.out.println();
		}

		System.out.println("RNG #1: " + w1);
		System.out.println("RNG #2: " + w2);
	}

	public static void correctHierarchy(Double[][] dataSet, UndirectedGraph mst1, UndirectedGraph mst2, int minPts){
		double[] pointNoiseLevels = new double[dataSet.length];
		int[] pointLastClusters = new int[dataSet.length];

		try {
			HDBSCANStar.computeHierarchyAndClusterTree(mst1, minPts, 
					false, null, "h1.csv", 
					"ct1.csv", ",", pointNoiseLevels, pointLastClusters, "vis1.vis");
		}
		catch (IOException ioe) {
			System.err.println("Error writing to hierarchy file or cluster tree file.");
			System.exit(-1);
		}


		pointNoiseLevels = new double[dataSet.length];
		pointLastClusters = new int[dataSet.length];

		try {
			HDBSCANStar.computeHierarchyAndClusterTree(mst2, minPts, 
					false, null, "h2.csv", 
					"ct2.csv", ",", pointNoiseLevels, pointLastClusters, "vis2.vis");
		}
		catch (IOException ioe) {
			System.err.println("Error writing to hierarchy file or cluster tree file.");
			System.exit(-1);
		}
	}

	@SuppressWarnings("unused")
	public static void performance(Double[][] dataSet, int maxK) {

		long start, end, duration;

		/**
		 *  HDBSCAN* 
		 **/
//		System.out.println("--------------------");
//		System.out.println("HDBSCAN*");
//		start = System.currentTimeMillis();
//		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
//		System.out.println("Core Distances: " + (System.currentTimeMillis() - start));
//
//		for (int k = maxK; k > 1; k--) {
//			//System.out.print(k + " ");
//			UndirectedGraph mst1 = HDBSCANStar.constructMST(dataSet, coreDistances, maxK, false, new EuclideanDistance());
//			mst1.quicksortByEdgeWeight();
//		}
//
//		duration = System.currentTimeMillis() - start;
//
//		System.out.println();
//		System.out.println("Total Running Time: " + duration);
//		System.out.println();

		/**
		 *  Incremental HDBSCAN* 
		 **/
		System.out.println("--------------------");
		System.out.println("Incremental HDBSCAN*");

		start = System.currentTimeMillis();
		double[][] coreDistances2 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		System.out.println("Core Distances: " + (System.currentTimeMillis() - start));

		long start1 = System.currentTimeMillis();
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, 0);
		System.out.println("RNG: " + (System.currentTimeMillis() - start1));

		UndirectedGraph mst2 = IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances2, false, new EuclideanDistance(), maxK);

		for (int k = maxK - 1; k > 1; k--) {
			//System.out.print(k + " ");
			RNG.updateWeights(dataSet, coreDistances2, new EuclideanDistance(), k);
			UndirectedGraph mst1 = IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances2, false, new EuclideanDistance(), k);
		}

		duration = System.currentTimeMillis() - start;
		
		System.out.println();
		System.out.println("Total Running Time: " + duration);
	}

	public static void testSorting(Double[][] dataSet, int k){
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, k, new EuclideanDistance());
		MutualReachabilityGraph MRG = new MutualReachabilityGraph(dataSet, coreDistances, new EuclideanDistance(), k);

		long start, end, duration;

		System.out.println("Quick Sort #1");
		start = System.currentTimeMillis();
		MRG.quicksortByEdgeWeight();
		end = System.currentTimeMillis();
		duration = end - start;
		System.out.println("Time: " + duration);

		System.out.println("Quick Sort #2");
		start = System.currentTimeMillis();
		MRG.quicksortByEdgeWeight();
		end = System.currentTimeMillis();
		duration = end - start;
		System.out.println("Time: " + duration);

		System.out.println("Bubble Sort #1");
		start = System.currentTimeMillis();
		MRG.bubbleSort();
		end = System.currentTimeMillis();
		duration = end - start;
		System.out.println("Time: " + duration);

		MRG.updateWeights(dataSet, coreDistances, new EuclideanDistance(), k - 1);

		System.out.println("Bubble Sort #2");
		start = System.currentTimeMillis();
		MRG.bubbleSort();
		end = System.currentTimeMillis();
		duration = end - start;
		System.out.println("Time: " + duration);		
	}
}
