package ca.ualberta.cs.hdbscanstar;

import java.util.HashSet;
import java.util.Stack;

import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.util.FairSplitTree;
import ca.ualberta.cs.util.Pair;
import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;

public class RelativeNeighborhoodGraph extends Graph {
	public static double[][] dataSet;
	public static double[][] coreDistances;
	public static int k;

	public static final String WS = "WS";
	public static final String SS = "SS";

	public static long numOfEdgesRNG = 0;

	public static BigList<Integer>[] RNG;
	
//	/**
//	 * Relative Neighborhood Graph naive constructor. Takes O(n³) time.
//	 * 
//	 * @param dataSet
//	 * @param coreDistances
//	 * @param distanceFunction
//	 * @param k
//	 */
//	public RelativeNeighborhoodGraph(double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k){
//		A = new IntBigArrayBigList();
//		B = new IntBigArrayBigList();
//		W = new DoubleBigArrayBigList();
//
//		RelativeNeighborhoodGraph.dataSet = dataSet;
//		RelativeNeighborhoodGraph.coreDistances = coreDistances;
//		RelativeNeighborhoodGraph.k = k;
//
//		for (int i = 0; i < dataSet.length; i++) {
//			for (int j = i + 1; j < dataSet.length; j++) {
//
//				if (neighbors(dataSet, coreDistances, i, j, k)) {
//					A.add(i);
//					B.add(j);
//					W.add(mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, i, j, k));
//				}
//			}
//		}
//
//		numOfEdgesRNG = A.size();
//
//		edgesA =  A;
//		edgesB =  B;
//		weights = W;
//
//		sortedEdges = new Integer[numOfEdgesRNG];
//
//		for (int i = 0; i < numOfEdgesRNG; i++) {
//			sortedEdges[i] = i;
//		}
//
//		// Cleaning no longer needed structures.
//		A = null;
//		B = null;
//		W = null;
//	}


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

		RNG = (BigList<Integer>[]) new BigList[dataSet.length];
		
		for (int i = 0; i < RNG.length; i++) {
			RNG[i] = new IntBigArrayBigList();
		}
		
		// Builds the Fair Split Tree T from dataSet.
		FairSplitTree T = FairSplitTree.build(dataSet);

		// Finds all the Well-separated Pairs from T.
		findWSPD(T, s, method);

		for (int i = 0; i < RNG.length; i++) {
			numOfEdgesRNG += RNG[i].size();
		}
		
		numOfEdgesRNG = numOfEdgesRNG/2;
		
//		boolean naiveFilter = false;
//
//		BigList<Integer> finalA = new IntBigArrayBigList();
//		BigList<Integer> finalB = new IntBigArrayBigList();
//		BigList<Double> finalW = new DoubleBigArrayBigList();

//		if (naiveFilter) {
//			for (int e = 0; e < A.size(); e++) {
//				if (neighbors(dataSet, coreDistances, A.get(e), B.get(e), k)) {
//					finalA.add(A.get(e));
//					finalB.add(B.get(e));
//					finalW.add(W.get(e));
//				}
//			}
//		}

//		if (filter) {
//
//			// boolean variable that tells whether the end points of this edge are neighbors or not.
//			boolean neighbors;
//
//			for (int e = 0; e < A.size(); e++) {
//
//				neighbors = true;
//
//				double cdA = coreDistances[A.get(e)][k-1];
//				double cdB = coreDistances[B.get(e)][k-1];
//
//				double w = W.get(e);
//
//				// In this first case, we check if the points are in each other's k-neighborhood.
//				if (w == Math.max(cdA, cdB)) {
//
//					int[] kNN;
//
//					if (cdA > cdB) {
//						kNN = IncrementalHDBSCANStar.kNN[A.get(e)];
//					} else {
//						kNN = IncrementalHDBSCANStar.kNN[B.get(e)];
//					}
//
//					for (int i = 0; i < kNN.length; i++) {
//
//						if (coreDistances[kNN[i]][k-1] > w) break;
//
//						double dac = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, A.get(e), kNN[i], k);
//						double dbc = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, B.get(e), kNN[i], k);
//
//						if (w > Math.max(dac, dbc)) {
//							neighbors = false;
//							break;
//						}
//					}
//
//					if (neighbors) {
//						finalA.add(A.get(e));
//						finalB.add(B.get(e));
//						finalW.add(W.get(e));
//					}
//
//					continue;
//				}
//
//				if (neighbors(dataSet, coreDistances, A.get(e), B.get(e), k)) {
//					finalA.add(A.get(e));
//					finalB.add(B.get(e));
//					finalW.add(W.get(e));
//				}
//			}
//		}
//
//		if (filter) {
//			numOfEdgesRNG = finalW.size();			
//		} else {
//			numOfEdgesRNG = W.size();
//		}
//
//		sortedEdges = new Integer[numOfEdgesRNG];
//
//		for (int i = 0; i < numOfEdgesRNG; i++) {
//			sortedEdges[i] = i;
//		}
//
//		if (filter) {
//			edgesA = finalA;
//			edgesB = finalB;
//			weights = finalW;			
//		} else {
//			edgesA = A;
//			edgesB = B;
//			weights = W;
//		}
//
//		// Cleaning no longer needed structures.
//		A = null;
//		B = null;
//		W = null;
//
//		finalA = null;
//		finalB = null;
//		finalW = null;
//		
//		T = null;
	}

	public void SBCN(FairSplitTree T1, FairSplitTree T2, double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k) {
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
			RNG[p.a].add(p.b);
			RNG[p.b].add(p.a);
		}

		tempA = null;
		tempB = null;
		tmpAB = null;
		tmpBA = null;
	}

	public boolean neighbors(double[][] dataSet, double[][] coreDistances, int i, int j, int k){
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

	public void findWSPD(FairSplitTree T, double s, String method) {
		Stack<Integer> stack = new Stack<Integer>();

		stack.add(T.id);

		while (!stack.isEmpty()) {
			int i = stack.pop();

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
			SBCN(T1, T2, dataSet, coreDistances, new EuclideanDistance(), k);
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

		double dab = Math.max(2*r, Math.max(T1.getMaxCD(), T2.getMaxCD()));

		BigList<Integer> result = FairSplitTree.rangeSearch(FairSplitTree.root.get(1), q, r, new IntBigArrayBigList());

		//		BigList<Integer> result = VAFileRangeQuery(q, r);

		if (result.isEmpty()) {
			return true;
		} else {
			for (Integer i : result) {

				double dac = Math.max(FairSplitTree.circleDistance(dataSet[i], T1) + T1.diameter(), Math.max(coreDistances[i][k-1], T1.getMaxCD()));
				double dbc = Math.max(FairSplitTree.circleDistance(dataSet[i], T2) + T2.diameter(), Math.max(coreDistances[i][k-1], T2.getMaxCD()));

				if (dab > Math.max(dac, dbc)) {
					return false;
				}

			}

			return true;
		}
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
