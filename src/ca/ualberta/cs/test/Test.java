package ca.ualberta.cs.test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.experiments.Experiments;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.MergeHierarchies;
import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;
import ca.ualberta.cs.main.CoreDistances;
import ca.ualberta.cs.main.Prim;
import ca.ualberta.cs.util.KdTree;

/**
 * @author Antonio Cavalcante
 *
 */
public class Test {
	
	public static double[][] dataSet = null;
//	public static String datasetFile = "/home/toni/git/HDBSCAN_Star/experiments/data#6/128d-128.dat";
	public static String datasetFile = "/home/toni/git/HDBSCAN_Star/experiments/data#6/2d-16.dat";
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
		boolean index = true;
		
//		performanceRNG(dataSet, 5, incremental, index);
//		correctnessRNG(dataSet, 5, incremental, index);
//		performanceRNGMSTs(dataSet, 2, incremental, compare, index);
//		testEuclideanDistance(dataSet);
		
//		mergeHierarchies(dataSet, 15);
		
//		performance(dataSet, 5, true, true, incremental, index);
		
		int q = 29;
		int k = 16;
		double eps = 20;
		
		KdTree kdTree = new KdTree(dataSet);

//		long start = System.currentTimeMillis();
//		testRange(dataSet, q, eps);
//		System.out.println("Total naive: " + (System.currentTimeMillis() - start));
//		
//		start = System.currentTimeMillis();
//		testKdTreeRange(dataSet, q, eps, kdTree);
//		System.out.println("Total kd-tree: " + (System.currentTimeMillis() - start));
		
		long start = System.currentTimeMillis();
		testKdTree(dataSet, q, k, kdTree);
		System.out.println("Total kd-tree: " + (System.currentTimeMillis() - start));
		
		start = System.currentTimeMillis();
		testKnn(dataSet, q, k);
		System.out.println("Total naive: " + (System.currentTimeMillis() - start));
	
//		int a = 5;
//		int b = 6;
//		
//		for (int i = 0; i < dataSet[0].length; i++) {
//			System.out.print(dataSet[a][i] + " ");
//		}
//		System.out.println();
//		
//		for (int i = 0; i < dataSet[0].length; i++) {
//			System.out.print(dataSet[b][i] + " ");
//		}
//		System.out.println();
//		
//		testEmptyLune(a, b, (new EuclideanDistance()).computeDistance(dataSet[a], dataSet[b]), kdTree);
	}
	
	public static void testEmptyLune(int a, int b, double eps, KdTree kdTree) {		
		System.out.println(kdTree.emptyLune(a, b, eps));
	}
	
	@SuppressWarnings("unused")
	public static void testKdTreeRange(double[][] dataSet, int q, double eps, KdTree kdTree) {
		
//		System.out.println(kdTree.toString());
		
		Collection<Integer> r = kdTree.range(dataSet[q], eps);
		
		System.out.println(r);
		
//		System.out.println(KdTree.time);
		
//		for (Integer ds : r) {
//			System.out.print(ds + ": ");
//			for (int i = 0; i < dataSet[ds].length; i++) {
//				System.out.print(dataSet[ds][i] + " ");
//			}
//			System.out.println("  ---  " + (new EuclideanDistance()).computeDistance(dataSet[q], dataSet[ds]));
//		}
//		System.out.println();
	}
	
	@SuppressWarnings("unused")
	private static void testRange(double[][] dataSet, int q, double eps) {
		
		Collection<Integer> r = new ArrayList<Integer>();
		
		for (int neighbor = 0; neighbor < dataSet.length; neighbor++) {

			double distance = (new EuclideanDistance()).computeDistance(dataSet[q], dataSet[neighbor]);
			
			if (distance < eps) {
				r.add(neighbor);
			}
		}
		
		System.out.println(r);
		
//		for (int a : r) {
//			System.out.print(a + ": ");
//			for (int i = 0; i < dataSet[a].length; i++) {
//				System.out.print(dataSet[a][i] + " ");
//			}
//			System.out.println("  ---  " + (new EuclideanDistance()).computeDistance(dataSet[q], dataSet[a]));
//		}
	}

	
	@SuppressWarnings("unused")
	public static void testKdTree(double[][] dataSet, int q, int k, KdTree kdTree) {
		
//		System.out.println(kdTree.toString());
		
		Collection<Integer> r = kdTree.nearestNeighbourSearch(k, q);
		
		System.out.println(KdTree.time);
		
		for (Integer ds : r) {
			System.out.print(ds + ": ");
			for (int i = 0; i < dataSet[ds].length; i++) {
				System.out.print(dataSet[ds][i] + " ");
			}
			System.out.println("  ---  " + (new EuclideanDistance()).computeDistance(dataSet[q], dataSet[ds]));
		}
		System.out.println();
	}
	
	@SuppressWarnings("unused")
	private static void testKnn(double[][] dataSet, int q, int k) {
		int numNeighbors = k;
		long time = 0;
		int[] kNN = new int[numNeighbors];
		double[] kNNDistances = new double[numNeighbors];	//Sorted nearest distances found so far
		
		for (int i = 0; i < numNeighbors; i++) {
			kNNDistances[i] = Double.MAX_VALUE;
			kNN[i] = Integer.MAX_VALUE;
		}
		
		for (int neighbor = 0; neighbor < dataSet.length; neighbor++) {
			long u = System.currentTimeMillis();
			double distance = (new EuclideanDistance()).computeDistance(dataSet[q], dataSet[neighbor]);

			time += System.currentTimeMillis() - u;
			
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
		System.out.println("Time euclidean naive: " + time);
		
		for (int a : kNN) {
			System.out.print(a + ": ");
			for (int i = 0; i < dataSet[a].length; i++) {
				System.out.print(dataSet[a][i] + " ");
			}
			System.out.println("  ---  " + (new EuclideanDistance()).computeDistance(dataSet[q], dataSet[a]));
		}
	}
	
	public static void mergeHierarchies(double[][] dataSet, int k){
		
		double[][] coreDistances = CoreDistances.calculateCoreDistances(dataSet, k, new EuclideanDistance());
		
		UndirectedGraph[] MSTs = new UndirectedGraph[k];
		
		// Compute RNG.
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), k, true, true, false, true);
		
		String inputFile = datasetFile.split("/")[datasetFile.split("/").length - 1];

		// Compute k MSTs and extract partitioning from each one.
		for (int i = 1; i < k; i++) {
			UndirectedGraph mst = Prim.constructMST(dataSet, coreDistances, i, false, RNG);
			mst.quicksortByEdgeWeight();
			MSTs[i] = mst;
			Experiments.computeOutputFiles(dataSet, coreDistances, mst, i, inputFile, i, false);
		}
		
		new MergeHierarchies(dataSet.length);
		
		// Merge MSTs.
		MergeHierarchies.merge(MSTs);
		
		// Compute MST from resulting graph and extract partitioning from it.
		UndirectedGraph mst = MergeHierarchies.constructMST(MergeHierarchies.G);
		MSTs[0] = mst;
		Experiments.computeOutputFiles(dataSet, coreDistances, mst, 2, inputFile, 0, false);
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
		double[][] coreDistances2 = CoreDistances.calculateCoreDistances(dataSet, k, new EuclideanDistance());

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
	public static void correct(double[][] dataSet, int maxK, boolean debug, boolean smartFilter, boolean naiveFilter, boolean incremental, boolean index) {

		/*
		 * Maximum minPoints cannot be higher that the size of the data set 
		 */
		if (maxK > dataSet.length) maxK = dataSet.length;

		/*
		 * Prim's Algorithm from the Mutual Reachability Graph 
		 */
		double[][] coreDistances1 = CoreDistances.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		UndirectedGraph mst1 = HDBSCANStar.constructMST(dataSet, coreDistances1,  maxK, false, new EuclideanDistance());
		mst1.quicksortByEdgeWeight();

		/*
		 * Kruskal from the Relative Neighborhood Graph 
		 */
		double[][] coreDistances2 = CoreDistances.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, smartFilter, naiveFilter, incremental, index);
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
	public static void correctnessRNG(double[][] dataSet, int maxK, boolean incremental, boolean index){
		double[][] coreDistances = CoreDistances.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());

		System.out.println("-----------------------------");
		System.out.println("   core-distances computed   ");
		System.out.println("-----------------------------");
		System.out.println();
		
		RelativeNeighborhoodGraph RNG1, RNG2, RNG3;
		
		// smart + naive filter
		long start = System.currentTimeMillis();

		RNG1 = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, true, true, incremental, false);
		System.out.println("-----------------------------");
		System.out.println("RNG WSPD - NO INDEX");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG1.numOfEdges);
		System.out.println("-----------------------------");

		start = System.currentTimeMillis();

		RNG2 = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, true, true, incremental, true);
		System.out.println("-----------------------------");
		System.out.println("RNG WSPD - KD-TREE");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG2.numOfEdges);
		System.out.println("-----------------------------");
		
		// naive filter 
		start = System.currentTimeMillis();

//		RNG3 = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK);
//		System.out.println("RNG NAIVE");
//		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
//		System.out.println("#edges: " + RNG3.numOfEdges);
//		System.out.println("-----------------------------");

		double cd = 0;
//		
//		System.out.println("RNG WSPD");
//		for (int i = 0; i < RNG1.ExtendedRNG.length; i++) {
//			System.out.print("[" + i + "]: ");
//			for (int j : RNG1.ExtendedRNG[i].keySet()) {
//				cd = RelativeNeighborhoodGraph.mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), i, j, maxK);
//				System.out.print(j + " ");
//			}
//			System.out.println();
//		}
//
//		System.out.println("RNG NAIVE");
//		for (int i = 0; i < RNG2.RNG.length; i++) {
//			System.out.print("[" + i + "]: ");
//			for (int j : RNG2.RNG[i]) {
//				cd = RelativeNeighborhoodGraph.mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), i, j, maxK);
//				System.out.print(j + " ");
//			}
//			System.out.println();
//		}
	}

	public static void performanceRNG(double[][] dataSet, int maxK, boolean incremental, boolean index){
		double[][] coreDistances = CoreDistances.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		
		System.out.println("-----------------------------");
		System.out.println("   core-distances computed   ");
		System.out.println("-----------------------------");
		System.out.println();
		
		RelativeNeighborhoodGraph RNG;
		
		// naive filter
		long start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, true, true, false, false);
		System.out.println("-----------------------------");
		System.out.println("RNG SMART + NAIVE FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("Naive Filter Time: " + RNG.timenaivefilter);
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("-----------------------------");
		
		RNG = null;
		
		// smart + naive filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, true, true, false, true);
		System.out.println("RNG SMART + INDEX FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("Index Filter Time: " + RNG.timenaivefilter);
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("-----------------------------");
		
		RNG = null;
		
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, true, false, false, false);
		System.out.println("RNG SMART FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("Smart Filtering Time: " + RNG.timenaivefilter);
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("-----------------------------");

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

	public static void performanceRNGMSTs(double[][] dataSet, int maxK, boolean incremental, boolean compare, boolean index){
		double[][] coreDistances = CoreDistances.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		
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

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, true, true, incremental, index);
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

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, true, false, incremental, index);
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

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, maxK, false, false, false, false);
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
					"ct1.csv", ",", pointNoiseLevels, pointLastClusters, "vis1.vis", null, null);
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
					"ct2.csv", ",", pointNoiseLevels, pointLastClusters, "vis2.vis", null, null);
		}
		catch (IOException ioe) {
			System.err.println("Error writing to hierarchy file or cluster tree file.");
			System.exit(-1);
		}
	}

	@SuppressWarnings("unused")
	public static void performance(double[][] dataSet, int maxK, boolean smartFilter, boolean naiveFilter, boolean incremental, boolean index) {

		long start, end, duration;

		System.out.println("--------------------");
		System.out.println("RNG COMPUTATION");
		
		start = System.currentTimeMillis();
		double[][] coreDistances2 = CoreDistances.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		System.out.println("Core Distances: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, smartFilter, naiveFilter, incremental, index);
		System.out.println("RNG time: " + (System.currentTimeMillis() - start));
		System.out.println("RNG size: " + RNG.numOfEdges);

		
		/**
		 *  RNG-HDBSCAN* 
		 **/
		System.out.println("--------------------");
		System.out.println("RNG-HDBSCAN* #1");

		start = System.currentTimeMillis();

		UndirectedGraph mst3 = Prim.constructMST(dataSet, coreDistances2, maxK, false, RNG);
		System.out.println(maxK + ": " + mst3.getTotalWeight());

		for (int k = maxK - 1; k > 1; k--) {
			mst3 = Prim.constructMST(dataSet, coreDistances2, k, false, RNG);
			System.out.println(k + ": " + mst3.getTotalWeight());
		}

		duration = System.currentTimeMillis() - start;
		
		System.out.println();
		System.out.println("Time to compute MSTs: " + duration);
		
	}

	public static void single(double[][] dataSet, int maxK, double s, String method, boolean smartFilter, boolean naiveFilter, boolean incremental, boolean index) {

		/**
		 *  Incremental HDBSCAN* 
		 **/
		System.out.println("--------------------");
		System.out.println("Incremental HDBSCAN*");
				
		long start = System.currentTimeMillis();
		double[][] coreDistances = CoreDistances.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		System.out.println("Core Distances: " + (System.currentTimeMillis() - start));

		long start1 = System.currentTimeMillis();
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, s, method, smartFilter, naiveFilter, incremental, index);
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
