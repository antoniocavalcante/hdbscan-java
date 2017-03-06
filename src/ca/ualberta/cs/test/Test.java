package ca.ualberta.cs.test;
import java.io.IOException;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;
import ca.ualberta.cs.main.Prim;

/**
 * @author Antonio Cavalcante
 *
 */
public class Test {
	
	public static double[][] dataSet = null;
	public static String datasetFile = "/home/toni/git/HDBSCAN_Star/experiments/data#6/4d-16.dat";
//	public static String datasetFile = "/home/toni/git/HDBSCAN_Star/experiments/debug/jad.dat";
	
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
		
//		performanceRNG(dataSet, 16);
//		correctnessRNG(dataSet, 1);
		performanceRNGMSTs(dataSet, 16);

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
	
	public static void correctnessRNG(double[][] dataSet, int maxK){
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		
		System.out.println("-----------------------------");
		System.out.println("   core-distances computed   ");
		System.out.println("-----------------------------");
		System.out.println();
		
		RelativeNeighborhoodGraph RNG1, RNG2;
		
		// naive filter
		long start = System.currentTimeMillis();

		RNG1 = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, true, true, true);
		System.out.println("-----------------------------");
		System.out.println("RNG WSPD");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG1.numOfEdges);
		System.out.println("Naive filtering time: " + RNG1.timenaivefilter);
		System.out.println("-----------------------------");
		
		// smart + naive filter 
		start = System.currentTimeMillis();

		RNG2 = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK);
		System.out.println("RNG NAIVE");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG2.numOfEdges);
		System.out.println("Naive filtering time: " + RNG2.timenaivefilter);
		System.out.println("-----------------------------");

//		double cd = 0;
//		
//		System.out.println("RNG WSPD");
//		for (int i = 0; i < RNG1.RNG.length; i++) {
//			System.out.print("[" + i + "]: ");
//			for (int j : RNG1.RNG[i].keySet()) {
//				cd = RelativeNeighborhoodGraph.mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), i, j, maxK);
//				System.out.print("(" + j + ", " + cd + ")" + " ");
//			}
//			System.out.println();
//		}
//
//		System.out.println("RNG NAIVE");
//		for (int i = 0; i < RNG2.RNG.length; i++) {
//			System.out.print("[" + i + "]: ");
//			for (int j : RNG2.RNG[i].keySet()) {
//				cd = RelativeNeighborhoodGraph.mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), i, j, maxK);
//				System.out.print("(" + j + ", " + cd + ")" + " ");
//			}
//			System.out.println();
//		}		
	}

	public static void performanceRNG(double[][] dataSet, int maxK){
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		
		System.out.println("-----------------------------");
		System.out.println("   core-distances computed   ");
		System.out.println("-----------------------------");
		System.out.println();
		
		RelativeNeighborhoodGraph RNG;
		
		// naive filter
		long start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, false, true, true);
		System.out.println("-----------------------------");
		System.out.println("RNG NAIVE FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("Naive filtering time: " + RNG.timenaivefilter);
		System.out.println("-----------------------------");
		
		RNG = null;
		
		// smart + naive filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, true, true, true);
		System.out.println("RNG SMART + NAIVE FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("Naive filtering time: " + RNG.timenaivefilter);
		System.out.println("-----------------------------");
		
		RNG = null;
		
		// smart filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, true, false, true);
		System.out.println("RNG SMART FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("Naive filtering time: " + RNG.timenaivefilter);
		System.out.println("-----------------------------");
		
		RNG = null;
		
		// no filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, false, false, true);
		System.out.println("RNG NO FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("Naive filtering time: " + RNG.timenaivefilter);
		System.out.println("-----------------------------");
		
		RNG = null;
	}

	public static void computeMSTs(double[][] dataSet, double[][] coreDistances, RelativeNeighborhoodGraph RNG, int k) {
		
		for (int minPoints = k; minPoints > 1; minPoints--) {
			UndirectedGraph mst = Prim.constructMST(dataSet, coreDistances, minPoints, false, RNG);
			mst.quicksortByEdgeWeight();
			
//			UndirectedGraph MST = HDBSCANStar.constructMST(dataSet, coreDistances,  minPoints, false, new EuclideanDistance());
//			MST.quicksortByEdgeWeight();
//			
//			compareMSTs(mst, MST, false);
		}
	}

	public static void performanceRNGMSTs(double[][] dataSet, int maxK){
		double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		
		System.out.println("-----------------------------");
		System.out.println("   core-distances computed   ");
		System.out.println("-----------------------------");
		System.out.println();
		
		RelativeNeighborhoodGraph RNG;		
		
		// naive filter
		long start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, false, true, true);
		System.out.println("-----------------------------");
		System.out.println("RNG NAIVE FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("Naive filtering time: " + RNG.timenaivefilter);
		start = System.currentTimeMillis();
		computeMSTs(dataSet, coreDistances, RNG, maxK);
		System.out.println("MSTs computation: " + (System.currentTimeMillis() - start));		
		System.out.println("-----------------------------");
		
		RNG = null;
		
		// smart + naive filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, true, true, true);
		System.out.println("RNG SMART + NAIVE FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("Naive filtering time: " + RNG.timenaivefilter);
		start = System.currentTimeMillis();
		computeMSTs(dataSet, coreDistances, RNG, maxK);
		System.out.println("MSTs computation: " + (System.currentTimeMillis() - start));		
		System.out.println("-----------------------------");
		
		RNG = null;
		
		// smart filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, true, false, true);
		System.out.println("RNG SMART FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("Naive filtering time: " + RNG.timenaivefilter);
		start = System.currentTimeMillis();
		computeMSTs(dataSet, coreDistances, RNG, maxK);
		System.out.println("MSTs computation: " + (System.currentTimeMillis() - start));		
		System.out.println("-----------------------------");
		
		RNG = null;
		
		// no filter 
		start = System.currentTimeMillis();

		RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances, new EuclideanDistance(), maxK, false, false, true);
		System.out.println("RNG NO FILTER");
		System.out.println("Running Time: " + (System.currentTimeMillis() - start));
		System.out.println("#edges: " + RNG.numOfEdges);
		System.out.println("Naive filtering time: " + RNG.timenaivefilter);
		start = System.currentTimeMillis();
		computeMSTs(dataSet, coreDistances, RNG, maxK);
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
}
