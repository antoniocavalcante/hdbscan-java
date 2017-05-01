package ca.ualberta.cs.test;
import java.io.IOException;
import java.util.Collection;
import java.util.Random;

import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.experiments.Experiments;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.hdbscanstar.MergeHierarchies;
import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;
import ca.ualberta.cs.main.Prim;
import ca.ualberta.cs.util.KdTree;

/**
 * @author Antonio Cavalcante
 *
 */
public class Test {
	
	public static double[][] dataSet = null;
	public static String datasetFile = "/home/toni/git/HDBSCAN_Star/experiments/data#6/4d-16.dat";
//	public static String datasetFile = "/home/toni/git/HDBSCAN_Star/experiments/data#6/4d-32.dat";
//	public static String datasetFile = "/home/toni/git/HDBSCAN_Star/experiments/debug/jad.dat";
//	public static String datasetFile = "/home/toni/git/HDBSCAN_Star/aloi25.txt";
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		try {
			dataSet = HDBSCANStar.readInDataSet(datasetFile, " ");
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}

		int numPoints = dataSet.length;
		
		System.out.println("Dataset size: " + numPoints);
		System.out.println("Dimensions: " + dataSet[0].length);
		
		boolean incremental = false;
		boolean compare = false;
		
//		performanceRNG(dataSet, 5, incremental);
//		correctnessRNG(dataSet, 5, incremental);
//		performanceRNGMSTs(dataSet, 2, incremental, compare);
//		testEuclideanDistance(dataSet);
		
//		mergeHierarchies(dataSet, 15);
		
		int q = 9;
		int k = 6;
		
		testKdTree(dataSet, q, k);
		testKnn(dataSet, q, k);
	}
	
	public static void testKdTree(double[][] dataSet, int q, int k) {
		
		KdTree kdTree = new KdTree(dataSet, dataSet[0].length);
		
//		System.out.println(kdTree.toString());
		
		Collection<Integer> r = kdTree.nearestNeighbourSearch(k, q);

		for (Integer ds : r) {
			System.out.print(ds + ": ");
			for (int i = 0; i < dataSet[ds].length; i++) {
				System.out.print(dataSet[ds][i] + " ");
			}
			System.out.println("  ---  " + (new EuclideanDistance()).computeDistance(dataSet[q], dataSet[ds]));
		}
		System.out.println();
	}
	
	private static void testKnn(double[][] dataSet, int q, int k) {
		int numNeighbors = k;

		int[] kNN = new int[numNeighbors];
		double[] kNNDistances = new double[numNeighbors];	//Sorted nearest distances found so far
		
		for (int i = 0; i < numNeighbors; i++) {
			kNNDistances[i] = Double.MAX_VALUE;
			kNN[i] = Integer.MAX_VALUE;
		}
		
		for (int neighbor = 0; neighbor < dataSet.length; neighbor++) {

			double distance = (new EuclideanDistance()).computeDistance(dataSet[q], dataSet[neighbor]);

			//Check at which position in the nearest distances the current distance would fit:
			int neighborIndex = numNeighbors;
			while (neighborIndex >= 1 && distance < kNNDistances[neighborIndex-1]) {
				neighborIndex--;
			}

			//Shift elements in the array to make room for the current distance:
			if (neighborIndex < numNeighbors) {

				for (int shiftIndex = numNeighbors-1; shiftIndex > neighborIndex; shiftIndex--) {
					kNNDistances[shiftIndex] = kNNDistances[shiftIndex-1];
					kNN[shiftIndex] = kNN[shiftIndex-1];
				}
				kNNDistances[neighborIndex] = distance;
				kNN[neighborIndex] = neighbor;
			}
		}
		
		
		for (int a : kNN) {
			System.out.print(a + ": ");
			for (int i = 0; i < dataSet[a].length; i++) {
				System.out.print(dataSet[a][i] + " ");
			}
			System.out.println("  ---  " + (new EuclideanDistance()).computeDistance(dataSet[q], dataSet[a]));
		}
	}
	
	public static void mergeHierarchies(double[][] dataSet, int k){
		
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, k, new EuclideanDistance());
				
		UndirectedGraph[] MSTs = new UndirectedGraph[k];
		
		// Compute RNG.
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), k, true, true, false);
		
		String inputFile = datasetFile.split("/")[datasetFile.split("/").length - 1];

		// Compute k MSTs and extract partitioning from each one.
		for (int i = 1; i < k; i++) {
			UndirectedGraph mst = Prim.constructMST(dataSet, coreDistances, i, false, RNG);
			mst.quicksortByEdgeWeight();
			MSTs[i] = mst;
			Experiments.computeOutputFiles(dataSet, mst, i, inputFile, i);
		}
		
		new MergeHierarchies(dataSet.length);
		
		// Merge MSTs.
		MergeHierarchies.merge(MSTs);
		
		// Compute MST from resulting graph and extract partitioning from it.
		UndirectedGraph mst = MergeHierarchies.constructMST(MergeHierarchies.G);
		MSTs[0] = mst;
		Experiments.computeOutputFiles(dataSet, mst, 2, inputFile, 0);
	}
	
	public static void printData(double[][] dataSet){
		for (int i = 0; i < dataSet.length; i++) {
			for (int j = 0; j < dataSet[i].length; j++) {
				System.out.print(dataSet[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println("------------------------------------");
	}
	
	public static void coreDistances(double[][] dataSet, int k){
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

	/**
	 * Compares the correct Minimum Spanning Tree and the generated from the Relative
	 * Neighborhood Graph, and check if they are the same.
	 * 
	 * @param dataSet The data set.
	 * @param maxK maximum Value of MinPoints.
	 */
	public static void correct(double[][] dataSet, int maxK, boolean debug, boolean smartFilter, boolean naiveFilter, boolean incremental) {

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
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, smartFilter, naiveFilter, incremental);
		UndirectedGraph mst2 = Prim.constructMST(dataSet, coreDistances2, maxK, false, RNG);
		mst2.quicksortByEdgeWeight();
		compareMSTs(mst1, mst2, debug);
	}


	/**
	 * Checks if two Minimum Spanning Trees are the same.
	 * 
	 * @param mst1
	 * @param mst2
	 * @return The number of different edges from the two MSTs.
	 */
	public static int compareMSTs(UndirectedGraph mst1, UndirectedGraph mst2, boolean debug) {
		int fails = 0;
		double sum1 = 0;
		double sum2 = 0;

		for (int i = 0; i < mst1.getNumEdges(); i++) {
			sum1 += mst1.getEdgeWeightAtIndex(i);
			sum2 += mst2.getEdgeWeightAtIndex(i);

			if (debug) {
				System.out.print(mst1.getEdgeWeightAtIndex(i) + "\t" + mst2.getEdgeWeightAtIndex(i) + "\t");
				System.out.print(mst1.getFirstVertexAtIndex(i) + "\t" + mst1.getSecondVertexAtIndex(i) + "\t");
				System.out.print(mst2.getFirstVertexAtIndex(i) + "\t" + mst2.getSecondVertexAtIndex(i) + "\t");
				System.out.println(mst1.getEdgeWeightAtIndex(i) == mst2.getEdgeWeightAtIndex(i));
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

	public static void compareRNGs(RelativeNeighborhoodGraph RNG1, RelativeNeighborhoodGraph RNG2) {
		if (RNG1.numOfEdges != RNG2.numOfEdges) {
			System.out.println("RNG #1: " + RNG1.numOfEdges);
			System.out.println("RNG #2: " + RNG2.numOfEdges);
		}
	}
	
	@SuppressWarnings("unused")
	public static void correctnessRNG(double[][] dataSet, int maxK, boolean incremental){
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		
		System.out.println("-----------------------------");
		System.out.println("   core-distances computed   ");
		System.out.println("-----------------------------");
		System.out.println();
		
		RelativeNeighborhoodGraph RNG1, RNG2;
		
		// smart + naive filter
		long start = System.currentTimeMillis();

		RNG1 = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, true, true, incremental);
		System.out.println("-----------------------------");
		System.out.println("RNG WSPD");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG1.numOfEdges);
		System.out.println("-----------------------------");
		
		// naive filter 
		start = System.currentTimeMillis();

		RNG2 = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK);
		System.out.println("RNG NAIVE");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG2.numOfEdges);
		System.out.println("-----------------------------");

		double cd = 0;
		
		System.out.println("RNG WSPD");
		for (int i = 0; i < RNG1.ExtendedRNG.length; i++) {
			System.out.print("[" + i + "]: ");
			for (int j : RNG1.ExtendedRNG[i].keySet()) {
				cd = RelativeNeighborhoodGraph.mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), i, j, maxK);
				System.out.print(j + " ");
			}
			System.out.println();
		}

		System.out.println("RNG NAIVE");
		for (int i = 0; i < RNG2.RNG.length; i++) {
			System.out.print("[" + i + "]: ");
			for (int j : RNG2.RNG[i]) {
				cd = RelativeNeighborhoodGraph.mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), i, j, maxK);
				System.out.print(j + " ");
			}
			System.out.println();
		}

		System.out.println("EXTRA EDGES");
//		for (int i = 0; i < RNG1.RNG.length; i++) {
//			System.out.print("[" + i + "]: ");
//			for (int j : RNG1.RNG[i]) {
//				if (!RNG2.RNG[i].contains(j)) {
//					cd = RelativeNeighborhoodGraph.mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), i, j, maxK);
//					System.out.print(j + " ");					
//				}
//			}
//			System.out.println();
//		}
	}

	public static void performanceRNG(double[][] dataSet, int maxK, boolean incremental){
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		
		System.out.println("-----------------------------");
		System.out.println("   core-distances computed   ");
		System.out.println("-----------------------------");
		System.out.println();
		
		RelativeNeighborhoodGraph RNG;
		
		// naive filter
		long start = System.currentTimeMillis();

//		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, false, true, incremental);
//		System.out.println("-----------------------------");
//		System.out.println("RNG NAIVE FILTER");
//		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
//		System.out.println("#edges: " + RNG.numOfEdges);
//		System.out.println("-----------------------------");
		
		RNG = null;
		
		// smart + naive filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, true, true, incremental);
		System.out.println("RNG SMART + NAIVE FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("-----------------------------");
		
		RNG = null;
		
		// smart filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, true, false, incremental);
		System.out.println("RNG SMART FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("-----------------------------");
		
		RNG = null;
		
		// no filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, false, false, false);
		System.out.println("RNG NO FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("-----------------------------");
		
		RNG = null;
	}

	public static void computeMSTs(double[][] dataSet, double[][] coreDistances, RelativeNeighborhoodGraph RNG, int k, boolean compare) {
		
		for (int minPoints = k; minPoints > 1; minPoints--) {
			UndirectedGraph mst = Prim.constructMST(dataSet, coreDistances, minPoints, false, RNG);
			mst.quicksortByEdgeWeight();
			
			if (compare) {
				UndirectedGraph MST = HDBSCANStar.constructMST(dataSet, coreDistances,  minPoints, false, new EuclideanDistance());
				MST.quicksortByEdgeWeight();
				
				compareMSTs(mst, MST, false);				
			}
		}
	}

	public static void performanceRNGMSTs(double[][] dataSet, int maxK, boolean incremental, boolean compare){
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		
		System.out.println("-----------------------------");
		System.out.println("   core-distances computed   ");
		System.out.println("-----------------------------");
		System.out.println();
		
		RelativeNeighborhoodGraph RNG;		
		
		// naive filter
		long start = System.currentTimeMillis();

//		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, false, true, incremental);
//		System.out.println("-----------------------------");
//		System.out.println("RNG NAIVE FILTER");
//		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
//		System.out.println("#edges: " + RNG.numOfEdges);
//		start = System.currentTimeMillis();
//		computeMSTs(dataSet, coreDistances, RNG, maxK, compare);
//		System.out.println("MSTs computation: " + (System.currentTimeMillis() - start));		
//		System.out.println("-----------------------------");
//		
//		RNG = null;
		
		// smart + naive filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, true, true, incremental);
		System.out.println("RNG SMART + NAIVE FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
//		System.out.println("#pairs that emit edges: " + RNG.pairsemitingedge);
//		System.out.println("#pairs that do not emit edges: " + RNG.pairsnotemitingedge);
//		System.out.println("#pairs that do not emit edges and contain one singleton: " + RNG.pairsatleastonesingle);
		start = System.currentTimeMillis();
		computeMSTs(dataSet, coreDistances, RNG, maxK, compare);
		System.out.println("MSTs computation: " + (System.currentTimeMillis() - start));		
		System.out.println("-----------------------------");
		
		RNG = null;
		
		// smart filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, true, false, incremental);
		System.out.println("RNG SMART FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
//		System.out.println("#pairs that emit edges: " + RNG.pairsemitingedge);
//		System.out.println("#pairs that do not emit edges: " + RNG.pairsnotemitingedge);
		start = System.currentTimeMillis();
		computeMSTs(dataSet, coreDistances, RNG, maxK, compare);
		System.out.println("MSTs computation: " + (System.currentTimeMillis() - start));		
		System.out.println("-----------------------------");
		
		RNG = null;
		
		// no filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, false, false, false);
		System.out.println("RNG NO FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
//		System.out.println("#pairs that emit edges: " + RNG.pairsemitingedge);
//		System.out.println("#pairs that do not emit edges: " + RNG.pairsnotemitingedge);
		start = System.currentTimeMillis();
		computeMSTs(dataSet, coreDistances, RNG, maxK, compare);
		System.out.println("MSTs computation: " + (System.currentTimeMillis() - start));		
		System.out.println("-----------------------------");
		
		RNG = null;
	}
	
	public static void correctHierarchy(double[][] dataSet, UndirectedGraph mst1, UndirectedGraph mst2, int minPts){
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
	public static void performance(double[][] dataSet, int maxK, double s, String method, boolean smartFilter, boolean naiveFilter, boolean incremental) {

		long start, end, duration;

		/**
		 *  HDBSCAN* 
		 **/
		System.out.println("--------------------");
		System.out.println("HDBSCAN*");
		start = System.currentTimeMillis();
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		System.out.println("Core Distances: " + (System.currentTimeMillis() - start));

		for (int k = maxK; k > 0; k--) {
			UndirectedGraph mst1 = HDBSCANStar.constructMST(dataSet, coreDistances, maxK, false, new EuclideanDistance());
			mst1.quicksortByEdgeWeight();
		}

		duration = System.currentTimeMillis() - start;

		System.out.println();
		System.out.println("Total Running Time: " + duration);
		System.out.println();

		/**
		 *  RNG-HDBSCAN* 
		 **/
		System.out.println("--------------------");
		System.out.println("RNG-HDBSCAN*");

		start = System.currentTimeMillis();
		double[][] coreDistances2 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		System.out.println("Core Distances: " + (System.currentTimeMillis() - start));

		long start1 = System.currentTimeMillis();
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, s, method, smartFilter, naiveFilter, incremental);
		System.out.println("RNG time: " + (System.currentTimeMillis() - start1));
		System.out.println("RNG size: " + RNG.numOfEdges);

		UndirectedGraph mst3 = Prim.constructMST(dataSet, coreDistances2, maxK, false, RNG);

		for (int k = maxK - 1; k > 1; k--) {
			mst3 = Prim.constructMST(dataSet, coreDistances2, k, false, RNG);
		}

		duration = System.currentTimeMillis() - start;
		
		System.out.println();
		System.out.println("Total Running Time: " + duration);
	}

	public static void single(double[][] dataSet, int maxK, double s, String method, boolean smartFilter, boolean naiveFilter, boolean incremental) {

		/**
		 *  Incremental HDBSCAN* 
		 **/
		System.out.println("--------------------");
		System.out.println("Incremental HDBSCAN*");
				
		long start = System.currentTimeMillis();
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		System.out.println("Core Distances: " + (System.currentTimeMillis() - start));

		long start1 = System.currentTimeMillis();
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, s, method, smartFilter, naiveFilter, incremental);
		System.out.println("RNG Building Time: " + (System.currentTimeMillis() - start1));
		System.out.println("RNG size: " + RNG.numOfEdges);

	}
	
	public static void testEuclideanDistance(double[][] dataSet){
		
		DistanceCalculator euclidean = new EuclideanDistance();
		Random r = new Random();
		
		long total = 0;
		
		for (int i = 0; i < 1000000000; i++) {
			int a = r.nextInt(dataSet.length - 1);
			int b = r.nextInt(dataSet.length - 1);
			
			long start = System.currentTimeMillis();
			euclidean.computeDistance(dataSet[a], dataSet[b]);
			total += System.currentTimeMillis() - start;
		}
		
		System.out.println("Total: " + total);
	}
}
