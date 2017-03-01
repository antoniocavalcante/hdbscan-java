package ca.ualberta.cs.hdbscanstar;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.util.FairSplitTree;
import ca.ualberta.cs.util.Pair;
import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class RelativeNeighborhoodGraph {

	public static IntOpenHashSet[] RNG;

	public static double[][] dataSet;
	public static double[][] coreDistances;

	public static int k;

	public static final String WS = "WS";
	public static final String SS = "SS";

	public static long numOfEdgesRNG = 0;

	public static boolean filter;
	
	public static boolean debug = false;
	
	public static BitSet in;
	
	/**
	 * Relative Neighborhood Graph naive constructor. Takes O(n³) time.
	 * 
	 * @param dataSet
	 * @param coreDistances
	 * @param distanceFunction
	 * @param k
	 */
	@SuppressWarnings("unchecked")
	public RelativeNeighborhoodGraph(double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k){

		RelativeNeighborhoodGraph.dataSet = dataSet;
		RelativeNeighborhoodGraph.coreDistances = coreDistances;
		RelativeNeighborhoodGraph.k = k;

		RNG = new IntOpenHashSet[dataSet.length];

		for (int i = 0; i < RNG.length; i++) {
			RNG[i] = new IntOpenHashSet();
		}

		for (int i = 0; i < dataSet.length; i++) {
			for (int j = i + 1; j < dataSet.length; j++) {

				if (neighbors(dataSet, coreDistances, i, j, k)) {
					RNG[i].add(j);
					RNG[j].add(i);
					numOfEdgesRNG++;
				}
			}
		}
	}


	/**
	 * Relative Neighborhood Graph constructor based on the Well-Separated Pair Decomposition.
	 * 
	 * @param dataSet
	 * @param coreDistances
	 * @param distanceFunctionimport java.util.List;
	 * @param k
	 * @param filter
	 * @param s
	 * @param method
	 */
	@SuppressWarnings("unchecked")
	public RelativeNeighborhoodGraph(double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k, boolean filter, double s, String method) {

		RelativeNeighborhoodGraph.dataSet = dataSet;
		RelativeNeighborhoodGraph.coreDistances = coreDistances;
		RelativeNeighborhoodGraph.k = k;

		RelativeNeighborhoodGraph.filter = filter;

		RNG = new IntOpenHashSet[dataSet.length];

		for (int i = 0; i < RNG.length; i++) {
			RNG[i] = new IntOpenHashSet();
		}

		// Builds the Fair Split Tree T from dataSet.
		FairSplitTree T = FairSplitTree.build(dataSet);

		// Finds all the Well-separated Pairs from T.
		findWSPD(T, s, method);

		T = null;
	}

	public boolean neighbors(int a, int b, int k, DistanceCalculator distanceFunction) {

//		double cdA = coreDistances[a][k-1];
//		double cdB = coreDistances[b][k-1];
//
//		double w = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, a, b, k);
//
//		// Check if the points are in each other's k-neighborhood.
//		if (w == Math.max(cdA, cdB)) {
//
//			int[] kNN;
//
//			if (cdA > cdB) {
//				kNN = IncrementalHDBSCANStar.kNN[a];
//			} else {
//				kNN = IncrementalHDBSCANStar.kNN[b];
//			}
//
//			for (int i = 0; i < kNN.length; i++) {
//
//				if (coreDistances[kNN[i]][k-1] >= w) continue;
//
//				double dac = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, a, kNN[i], k);
//				double dbc = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, b, kNN[i], k);
//
//				if (w > Math.max(dac, dbc)) {
//					return false;
//				}
//			}
//
//			return true;
//		}
//		
//		int[] kNNa = IncrementalHDBSCANStar.kNN[a];
//		int[] kNNb = IncrementalHDBSCANStar.kNN[b];
//
//		for (int i = 0; i < kNNa.length; i++) {
//
//			if (coreDistances[kNNa[i]][k-1] >= w) continue;
//
//			double dac = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, a, kNNa[i], k);
//			double dbc = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, b, kNNa[i], k);
//
//			if (w > Math.max(dac, dbc)) {
//				return false;
//			}
//		}		
//
//		for (int i = 0; i < kNNb.length; i++) {
//
//			if (coreDistances[kNNb[i]][k-1] >= w) continue;
//
//			double dac = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, a, kNNb[i], k);
//			double dbc = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, b, kNNb[i], k);
//
//			if (w > Math.max(dac, dbc)) {
//				return false;
//			}
//		}		
		
		// Filter Naive - OFF
		if (!neighbors(dataSet, coreDistances, a, b, k)) {
			return false;
		}
		
		return true;
	}

	public boolean neighbors(double[][] dataSet, double[][] coreDistances, int i, int j, int k) {
		double dij = mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), i, j, k);

		for (int m = 0; m < coreDistances.length; m++) {
			double dim = mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), i, m, k);
			double djm = mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), j, m, k);

			if (dij > Math.max(dim, djm)) {
				return false;
			}
		}

		return true;
	}

	public void filter(int k){
		for (int i = 0; i < RNG.length; i++) {
			for (Iterator<Integer> iter = RNG[i].iterator(); iter.hasNext();) {

				Integer m = iter.next();
				if (i < m) {
					if (!neighbors(i, m, k, (new EuclideanDistance()))) {
						iter.remove();
						RNG[m].remove(i);

						numOfEdgesRNG--;
					}	
				}
			}			
		}
	}
	
	public void SBCN(FairSplitTree T1, FairSplitTree T2, DistanceCalculator distanceFunction) {
		double d;

		HashSet<Pair> tmpAB  = new HashSet<Pair>();
		HashSet<Pair> tmpBA  = new HashSet<Pair>();

		double min = Double.MAX_VALUE;
		BigList<Integer> tempA = new IntBigArrayBigList();
		BigList<Integer> tempB = new IntBigArrayBigList();

		for (Integer p1 : T1.retrieve()) {

			for (Integer p2 : T2.retrieve()) {

				d = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, p1, p2, k);

				if (d < min) {
					tempA.clear();
					tempB.clear();
				}

				if (d <= min) {
					min = d;
					tempA.add(p1);
					tempB.add(p2);
				}
			}

			for (int i = 0; i < tempA.size(); i++) {
				Pair p;

				if (tempA.get(i) < tempB.get(i)) {
					p = new Pair(tempA.get(i), tempB.get(i));
				} else {
					p = new Pair(tempB.get(i), tempA.get(i));
				}

				if (!tmpAB.contains(p)) tmpAB.add(p);
			}

			min = Double.MAX_VALUE;
		}

		tempA = new IntBigArrayBigList();
		tempB = new IntBigArrayBigList();

		for (Integer p2 : T2.retrieve()) {

			for (Integer p1 : T1.retrieve()) {

				d = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, p1, p2, k);

				if (d < min) {
					tempA.clear();
					tempB.clear();
				}

				if (d <= min) {
					min = d;
					tempA.add(p2);
					tempB.add(p1);
				}
			}

			for (int i = 0; i < tempA.size(); i++) {
				Pair p;

				if (tempA.get(i) < tempB.get(i)) {
					p = new Pair(tempA.get(i), tempB.get(i));
				} else {
					p = new Pair(tempB.get(i), tempA.get(i));
				}

				if (tmpAB.contains(p)) tmpBA.add(p);
			}

			min = Double.MAX_VALUE;
		}

		for (Pair p : tmpBA) {

			if (filter) {

				if (neighbors(p.a, p.b, k, distanceFunction)) {
					RNG[p.a].add(p.b);
					RNG[p.b].add(p.a);

					numOfEdgesRNG++;
				}

			} else {

				RNG[p.a].add(p.b);
				RNG[p.b].add(p.a);

				numOfEdgesRNG++;				
			}
		}

		tempA = null;
		tempB = null;
		tmpAB = null;
		tmpBA = null;
	}

	public void findWSPD(FairSplitTree T, double s, String method) {
		Stack<Long> stack = new Stack<Long>();

		stack.add(T.id);

		while (!stack.isEmpty()) {
			long i = stack.pop();

			FairSplitTree current = FairSplitTree.root.get(i);

			if (!current.getLeft().isLeaf()) {
				stack.add(current.left());
			}

			if (!current.getRight().isLeaf()) {
				stack.add(current.right());
			}
			
			findPairs(current.getLeft(), current.getRight(), s, method);
		}

		stack = null;
	}

	public void findPairs(FairSplitTree T1, FairSplitTree T2, double s, String method) {

		if (separated(T1, T2, s, method)) {
			SBCN(T1, T2, new EuclideanDistance());
		} else {
			if (T1.diameterMRD() <= T2.diameterMRD()) {
				findPairs(T1, T2.getLeft() , s, method);
				findPairs(T1, T2.getRight(), s, method);
			} else {
				findPairs(T1.getLeft(),  T2, s, method);
				findPairs(T1.getRight(), T2, s, method);
			}
		}
	}


	/**
	 * @param T1
	 * @param T2
	 * @param s
	 * @return
	 */
	public static boolean separated(FairSplitTree T1, FairSplitTree T2, double s) {
		return ws(T1, T2, s);
	}

	/**
	 * @param T1
	 * @param T2
	 * @param s
	 * @param method
	 * @return
	 */
	public static boolean separated(FairSplitTree T1, FairSplitTree T2, double s, String method) {

		if (T1.id == T2.id) {
			return false;
		}

		if (method == SS) {
			return ss(T1, T2, s);
		} else {
			return ws(T1, T2, s);
		}		
	}

	/** Method to check whether two FairSplitTrees might emit a RNG edge.
	 * It does not work currently, the core-distance of the points in each subtree would have to be checked.
	 * @param T1
	 * @param T2
	 * @return
	 */
	public static boolean rn(FairSplitTree T1, FairSplitTree T2) {
		double r = FairSplitTree.circleDistance(T1, T2)/2;

		double[] q = new double[T1.center().length];

		for (int i = 0; i < q.length; i++) {
			q[i] = (T1.center()[i] + T2.center()[i])/2;
		}

		BigList<Integer> result = FairSplitTree.rangeSearch(FairSplitTree.root.get(1), q, r, new IntBigArrayBigList());

		if (result.isEmpty()) {
			return true;
		}

		return false;
	}

	/**
	 * Receives two Fair Split Trees T1 and T2 and returns whether they are
	 * well separated or not.
	 * 
	 * @param T1 FairSplitTree #1.
	 * @param T2 FairSplitTree #2.
	 * @param s Separation factor.
	 * @return true if T1 and T2 are well separated, false otherwise. 
	 */
	public static boolean ws(FairSplitTree T1, FairSplitTree T2, double s){

		double d = FairSplitTree.circleDistance(T1, T2);
		
		if (d >= s*Math.max(T1.diameterMRD(), T2.diameterMRD())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Receives two Fair Split Trees T1 and T2 and returns whether they are
	 * semi-separated or not.
	 * 
	 * @param T1 FairSplitTree #1.
	 * @param T2 FairSplitTree #2.
	 * @param s Separation factor.
	 * @return true if T1 and T2 are well separated, false otherwise. 
	 */
	public static boolean ss(FairSplitTree T1, FairSplitTree T2, double s){

		double d = FairSplitTree.circleDistance(T1, T2);

		if (d >= s*Math.min(T1.diameterMRD(), T2.diameterMRD())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param dataSet
	 * @param coreDistances
	 * @param distanceFunction
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
	public static double mutualReachabilityDistance(double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int i, int j, int k) {
		double mutualReachabiltiyDistance = distanceFunction.computeDistance(dataSet[i], dataSet[j]);

		if (coreDistances[i][k - 1] > mutualReachabiltiyDistance)
			mutualReachabiltiyDistance = coreDistances[i][k - 1];

		if (coreDistances[j][k - 1] > mutualReachabiltiyDistance)
			mutualReachabiltiyDistance = coreDistances[j][k - 1];

		return mutualReachabiltiyDistance;
	}
}
