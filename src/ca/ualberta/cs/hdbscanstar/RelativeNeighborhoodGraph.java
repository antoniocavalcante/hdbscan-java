package ca.ualberta.cs.hdbscanstar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.util.FairSplitTree;
import ca.ualberta.cs.util.Pair;

public class RelativeNeighborhoodGraph extends Graph {
	public static Double[][] dataSet;
	public static double[][] coreDistances;
	public static int k;

	public static final String WS = "WS";
	public static final String SS = "SS";

	public int numOfEdgesRNG;

	public Integer[] edgesA;
	public Integer[] edgesB;
	public Double[] weights;

	public Integer[] sortedEdges;

	public BitSet inMST;

	public List<Integer> A;
	public List<Integer> B;
	public List<FairSplitTree> C;
	public List<Double> W;


	/**
	 * Relative Neighborhood Graph naive constructor. Takes O(n³) time.
	 * 
	 * @param dataSet
	 * @param coreDistances
	 * @param distanceFunction
	 * @param k
	 */
	public RelativeNeighborhoodGraph(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k){
		A = new ArrayList<Integer>();
		B = new ArrayList<Integer>();
		W = new ArrayList<Double>();

		RelativeNeighborhoodGraph.dataSet = dataSet;
		RelativeNeighborhoodGraph.coreDistances = coreDistances;
		RelativeNeighborhoodGraph.k = k;

		for (int i = 0; i < dataSet.length; i++) {
			for (int j = i + 1; j < dataSet.length; j++) {

				if (neighbors(dataSet, coreDistances, i, j, k)) {
					A.add(i);
					B.add(j);
					W.add(mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, i, j, k));
				}
			}
		}

		numOfEdgesRNG = A.size();
		edgesA =  new Integer[numOfEdgesRNG];
		edgesB =  new Integer[numOfEdgesRNG];
		weights = new Double[numOfEdgesRNG];

		sortedEdges = new Integer[numOfEdgesRNG];

		inMST = new BitSet(numOfEdgesRNG);
		inMST.clear();

		for (int i = 0; i < numOfEdgesRNG; i++) {
			sortedEdges[i] = i;
		}

		edgesA = A.toArray(edgesA);
		edgesB = B.toArray(edgesB);
		weights = W.toArray(weights);

		// Cleaning no longer needed structures.
		A = null;
		B = null;
		W = null;
	}


	/**
	 * Relative Neighborhood Graph constructor based on the Well-Separated Pair Decomposition.
	 * 
	 * @param dataSet
	 * @param coreDistances
	 * @param distanceFunction
	 * @param k
	 * @param filter
	 * @param s
	 * @param method
	 */
	public RelativeNeighborhoodGraph(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k, boolean filter, double s, String method) {

		// Builds the Fair Split Tree T from dataSet.
		FairSplitTree T = FairSplitTree.build(dataSet);

		System.out.println("Fair Split Tree: " + T.root.size() + " nodes...");

		// Initializes attributes to store the RNG edges initially.
		A = new ArrayList<Integer>();
		B = new ArrayList<Integer>();
		C = new ArrayList<FairSplitTree>();		
		W = new ArrayList<Double>();

		ArrayList<Integer> finalA = new ArrayList<Integer>();
		ArrayList<Integer> finalB = new ArrayList<Integer>();
		ArrayList<FairSplitTree> finalC = new ArrayList<FairSplitTree>();
		ArrayList<Double> finalW = new ArrayList<Double>();

		RelativeNeighborhoodGraph.dataSet = dataSet;
		RelativeNeighborhoodGraph.coreDistances = coreDistances;
		RelativeNeighborhoodGraph.k = k;

		// Finds all the Well-separated Pairs from T.
		findWSPD(T, s, method);

		System.out.println("Super RNG constructed!");

		System.out.println("Number of edges to filter: " + W.size());

		boolean naiveFilter = false;

		if (naiveFilter) {
			for (int e = 0; e < A.size(); e++) {
				if (neighbors(dataSet, coreDistances, A.get(e), B.get(e), k)) {
					finalA.add(A.get(e));
					finalB.add(B.get(e));
					finalW.add(W.get(e));
					finalC.add(C.get(e));
				}
			}
		}

		if (filter) {

			// boolean variable that tells whether the end points of this edge are neighbors or not.
			boolean neighbors;

			for (int e = 0; e < A.size(); e++) {

				neighbors = true;

				double cdA = coreDistances[A.get(e)][k-1];
				double cdB = coreDistances[B.get(e)][k-1];

				double w = W.get(e);

				// In this first case, we check if the points are in each other's k-neighborhood.
				if (w == Math.max(cdA, cdB)) {

					int[] kNN;

					if (cdA > cdB) {
						kNN = IncrementalHDBSCANStar.kNN[A.get(e)];
					} else {
						kNN = IncrementalHDBSCANStar.kNN[B.get(e)];
					}

					for (int i = 0; i < kNN.length; i++) {

						if (coreDistances[kNN[i]][k-1] > w) break;

						double dac = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, A.get(e), kNN[i], k);
						double dbc = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, B.get(e), kNN[i], k);

						if (w > Math.max(dac, dbc)) {
							neighbors = false;
							break;
						}
					}

					if (neighbors) {
						finalA.add(A.get(e));
						finalB.add(B.get(e));
						finalW.add(W.get(e));
						finalC.add(C.get(e));
					}

					continue;
				}


				if (neighbors(dataSet, coreDistances, A.get(e), B.get(e), k)) {
					finalA.add(A.get(e));
					finalB.add(B.get(e));
					finalW.add(W.get(e));
					finalC.add(C.get(e));
				}

//				if (!neighbors) {
//					continue;
//				} else {
//					// Tries to find a point in the smaller ancestor on the FairSplitTree for the nodes that contains both points.
//
//					for (Integer v : C.get(e).retrieve()) {
//
//						double dac = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, A.get(e), v, k);
//						double dbc = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, B.get(e), v, k);
//
//						if (w > Math.max(dac, dbc)) {
//							neighbors = false;
//							break;
//						}
//					}
//
//				}
//
//				if (!neighbors) {
//					continue;
//				} else {
//
//					ArrayList<Integer> nA = new ArrayList<Integer>();
//
//					FairSplitTree.rangeSearch(T, dataSet[A.get(e)], W.get(e), nA);
//
//					for (int i = 0; i < nA.size(); i++) {
//
//						double dim = mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), A.get(e), nA.get(i), k);
//						double djm = mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), B.get(e), nA.get(i), k);
//
//						if (W.get(e) > Math.max(dim, djm)) {
//							neighbors = false;
//							break;
//						}	
//					}
//				}
//
//				if (!neighbors) {
//					continue;
//				} else {
//
//					finalA.add(A.get(e));
//					finalB.add(B.get(e));
//					finalW.add(W.get(e));
//					finalC.add(C.get(e));
//				}
			}
		}

		if (filter) {
			numOfEdgesRNG = finalW.size();			
		} else {
			numOfEdgesRNG = W.size();
		}

		edgesA =  new Integer[numOfEdgesRNG];
		edgesB =  new Integer[numOfEdgesRNG];
		weights = new Double[numOfEdgesRNG];

		sortedEdges = new Integer[numOfEdgesRNG];

		inMST = new BitSet(numOfEdgesRNG);
		inMST.clear();

		for (int i = 0; i < numOfEdgesRNG; i++) {
			sortedEdges[i] = i;
		}

		if (filter) {
			edgesA = finalA.toArray(edgesA);
			edgesB = finalB.toArray(edgesB);
			weights = finalW.toArray(weights);			
		} else {
			edgesA = A.toArray(edgesA);
			edgesB = B.toArray(edgesB);
			weights = W.toArray(weights);
		}

		// Cleaning no longer needed structures.
		A = null;
		B = null;
		C = null;
		W = null;

		finalA = null;
		finalB = null;
		finalC = null;
		finalW = null;		
	}

	public void SBCN(FairSplitTree T1, FairSplitTree T2, Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k) {
		double d;

		HashMap<Pair, FairSplitTree> tmp  = new HashMap<Pair, FairSplitTree>();

		double min = Double.MAX_VALUE;
		ArrayList<Integer> tempA = new ArrayList<Integer>();
		ArrayList<Integer> tempB = new ArrayList<Integer>();

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
		}

		for (int i = 0; i < tempA.size(); i++) {
			Pair p;

			if (tempA.get(i) < tempB.get(i)) {
				p = new Pair(tempA.get(i), tempB.get(i));
			} else {
				p = new Pair(tempB.get(i), tempA.get(i));
			}

			if (!tmp.containsKey(p)) tmp.put(p, FairSplitTree.parent(T1, T2));
		}

		min = Double.MAX_VALUE;
		tempA = new ArrayList<Integer>();
		tempB = new ArrayList<Integer>();

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
		}

		for (int i = 0; i < tempA.size(); i++) {
			Pair p;

			if (tempA.get(i) < tempB.get(i)) {
				p = new Pair(tempA.get(i), tempB.get(i));
			} else {
				p = new Pair(tempB.get(i), tempA.get(i));
			}

			if (!tmp.containsKey(p)) tmp.put(p, FairSplitTree.parent(T1, T2));
		}

		for (Pair p : tmp.keySet()) {
			A.add(p.a);
			B.add(p.b);

			C.add(tmp.get(p));

			W.add(mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, p.a, p.b, k));
		}
		
		if (A.size() % 1000000 == 0) System.out.println(A.size());
		
		tempA = null;
		tempB = null;
	}

	public boolean neighbors(Double[][] dataSet, double[][] coreDistances, int i, int j, int k){
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
			FairSplitTree current = FairSplitTree.root.get(stack.pop());

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
			if (T1.diameter() <= T2.diameter()) {
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

		if (d >= s*Math.max(T1.diameter(), T2.diameter())) {
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

		if (d >= s*Math.min(T1.diameter(), T2.diameter())) {
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
	public static double mutualReachabilityDistance(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int i, int j, int k) {
		double mutualReachabiltiyDistance = distanceFunction.computeDistance(dataSet[i], dataSet[j]);

		if (coreDistances[i][k - 1] > mutualReachabiltiyDistance)
			mutualReachabiltiyDistance = coreDistances[i][k - 1];

		if (coreDistances[j][k - 1] > mutualReachabiltiyDistance)
			mutualReachabiltiyDistance = coreDistances[j][k - 1];

		return mutualReachabiltiyDistance;
	}

	/**
	 * @param dataSet
	 * @param coreDistances
	 * @param distanceFunction
	 * @param k
	 */
	public void updateWeights(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k) {
		for (int i = 0; i < numOfEdgesRNG; i++) {
			this.weights[sortedEdges[i]] = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, this.edgesA[sortedEdges[i]], this.edgesB[sortedEdges[i]], k);
		}
	}

	public Integer[] timSort(){
		Comparator<Integer> c = new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				if (weights[o1] < weights[o2]) {
					return -1;
				}
				if (weights[o1] > weights[o2]) {					
					return 1;
				}
				return 0;
			}
		};

		TimSort.sort(sortedEdges, c);

		return sortedEdges;
	}
}
