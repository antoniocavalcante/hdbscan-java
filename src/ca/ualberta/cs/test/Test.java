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
 * @author Antonio Cavalcante
 *
 */
public class Test {
	public static double[][] dataSet = null;
	public static String datasetFile = "/home/toni/git/HDBSCAN_Star/experiments/data#6/4d-16.dat";
	public static void main(String[] args) {

		try {
			dataSet = HDBSCANStar.readInDataSet(datasetFile, " ");
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/experiments/debug/4d-16-2.dat", " ");
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/experiments/debug/4d-16.dat", " ");
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/experiments/debug/4p.dat", " ");
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/experiments/debug/jad.dat", " ");
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/experiments/data#2/2d.data", " ");
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/test2.dat", " ");
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/4.dat", " ");
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/jad.dat", " ");
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/j.dat", ",");
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}

		int numPoints = dataSet.length;
		System.out.println("Dataset size: " + numPoints);
		System.out.println("Dimensions: " + dataSet[0].length);
		
		single(dataSet, 128, 1, RelativeNeighborhoodGraph.WS, true);
//		performance(dataSet, 16, 1, WSPD.WS, false);
//		performanceRNG(dataSet, 1, false, 1, RelativeNeighborhoodGraph.WS, true);
//		correct(dataSet, 16, false, 1, WSPD.WS);
	
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
	
	public static void weights(double[][] dataSet, int k) throws IOException{
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

	public static void testUpdate2(double[][] dataSet, int k1, int k2){
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
	public static void correct(double[][] dataSet, int maxK, boolean debug, double s, String method) {

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
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, true, s, method);
//		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK);
		UndirectedGraph mst2 = IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances2, false, new EuclideanDistance(), maxK);

		correct(mst1, mst2, debug);
	}


	/**
	 * Checks if two Minimum Spanning Trees are the same.
	 * 
	 * @param mst1
	 * @param mst2
	 * @return The number of different edges from the two MSTs.
	 */
	public static int correct(UndirectedGraph mst1, UndirectedGraph mst2, boolean debug) {
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

	public static void testRNG(double[][] dataSet, int maxK, boolean debug, double s, String method){
		double[][] coreDistances2 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, true, s, method);

		for (int k = maxK; k > 0; k--) {
			double[][] coreDistances = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, k, new EuclideanDistance());
			UndirectedGraph mst1 = HDBSCANStar.constructMST(dataSet, coreDistances, maxK, false, new EuclideanDistance());
			mst1.quicksortByEdgeWeight();

			RNG.updateWeights(dataSet, coreDistances2, new EuclideanDistance(), k);
			//			printGraph(RNG, dataSet);
			UndirectedGraph mst2 = IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances2, false, new EuclideanDistance(), maxK);

			correct(mst1, mst2, debug);
		}
	}

	public static void performanceRNG(double[][] dataSet, int maxK, boolean debug, double s, String method, boolean filter){
		double[][] coreDistances2 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		
		long start = System.currentTimeMillis();
		RelativeNeighborhoodGraph RNG1 = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK);
		System.out.println("Naive RNG construction: " + (System.currentTimeMillis() - start));
		RNG1.timSort();
		
		System.out.println("-----------------------------");
		
		start = System.currentTimeMillis();
		RelativeNeighborhoodGraph RNG2 = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, filter, s, method);
		System.out.println("WSPD RNG construction: " + (System.currentTimeMillis() - start));
		RNG2.timSort();

		System.out.println("RNG #1 (NAIVE): " + RNG1.numOfEdgesRNG);
		System.out.println("RNG #2 (sWSPD): " + RNG2.numOfEdgesRNG);
		
		if (debug) {
			printGraphs(RNG1, RNG2);
		}
	}
	
	/** Receives two Relative Neighborhood Graphs and print them.
	 * @param RNG1 Relative Neighborhood Graph #1
	 * @param RNG2 Relative Neighborhood Graph #2
	 */
	public static void printGraphs(RelativeNeighborhoodGraph RNG1, RelativeNeighborhoodGraph RNG2){

		int m = Math.max(RNG1.numOfEdgesRNG, RNG2.numOfEdgesRNG);
		double w1 = 0;
		double w2 = 0;

		for (int i = 0; i < m; i++) {
			if (i < RNG1.numOfEdgesRNG){
				System.out.print("[1](" + RNG1.edgesA.get(RNG1.sortedEdges[i]) + ", " + RNG1.edgesB.get(RNG1.sortedEdges[i]) + ") : " + RNG1.weights.get(RNG1.sortedEdges[i]) + "\t\t");
				w1 += RNG1.weights.get(RNG1.sortedEdges[i]);
			}
			if (i < RNG2.numOfEdgesRNG){
				System.out.print("[2] (" + RNG2.edgesA.get(RNG2.sortedEdges[i]) + ", " + RNG2.edgesB.get(RNG2.sortedEdges[i]) + ") : " + RNG2.weights.get(RNG2.sortedEdges[i]));
				w2 += RNG2.weights.get(RNG2.sortedEdges[i]);
			}
			if (i < Math.min(RNG1.numOfEdgesRNG, RNG2.numOfEdgesRNG)) {
				System.out.print("\t\t" + (RNG1.weights.get(RNG1.sortedEdges[i]).compareTo(RNG2.weights.get(RNG2.sortedEdges[i]))));
			}
			
			System.out.println();
		}

		System.out.println("RNG #1: " + w1);
		System.out.println("RNG #2: " + w2);
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
	public static void performance(double[][] dataSet, int maxK, double s, String method, boolean filter) {

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
		 *  Incremental HDBSCAN* 
		 **/
		System.out.println("--------------------");
		System.out.println("Incremental HDBSCAN*");

		start = System.currentTimeMillis();
		double[][] coreDistances2 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance());
		System.out.println("Core Distances: " + (System.currentTimeMillis() - start));

		long start1 = System.currentTimeMillis();
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, filter, s, method);
		System.out.println("RNG time: " + (System.currentTimeMillis() - start1));
		System.out.println("RNG size: " + RNG.numOfEdgesRNG);

		UndirectedGraph mst2 = IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances2, false, new EuclideanDistance(), maxK);

		for (int k = maxK - 1; k > 1; k--) {
			RNG.updateWeights(dataSet, coreDistances2, new EuclideanDistance(), k);
			UndirectedGraph mst1 = IncrementalHDBSCANStar.kruskal(dataSet, RNG, coreDistances2, false, new EuclideanDistance(), k);
		}

		duration = System.currentTimeMillis() - start;
		
		System.out.println();
		System.out.println("Total Running Time: " + duration);
	}

	public static void single(double[][] dataSet, int maxK, double s, String method, boolean filter) {

		/**
		 *  Incremental HDBSCAN* 
		 **/
		System.out.println("--------------------");
		System.out.println("Incremental HDBSCAN*");
		
		long startVA = System.currentTimeMillis();
		RelativeNeighborhoodGraph.VAFileInit(datasetFile);
		System.out.println("VA-File: " + (System.currentTimeMillis() - startVA));
		
		long start = System.currentTimeMillis();
		double[][] coreDistances2 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, maxK, new EuclideanDistance(), 0);
		System.out.println("Core Distances: " + (System.currentTimeMillis() - start));

		long start1 = System.currentTimeMillis();
		RelativeNeighborhoodGraph RNG = new RelativeNeighborhoodGraph(dataSet, coreDistances2, new EuclideanDistance(), maxK, filter, s, method);
		System.out.println("RNG Building Time: " + (System.currentTimeMillis() - start1));
		System.out.println("RNG size: " + RNG.numOfEdgesRNG);

	}

	
	public static void testSorting(double[][] dataSet, int k){
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
