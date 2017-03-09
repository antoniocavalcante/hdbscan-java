package ca.ualberta.cs.hdbscanstar;

import java.util.HashSet;
import java.util.Stack;

import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.util.FairSplitTree;
import ca.ualberta.cs.util.Pair;
import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class RelativeNeighborhoodGraph {
	
	public Int2ObjectOpenHashMap<DistanceLevel>[] ExtendedRNG;
	
	public IntBigArrayBigList[] RNG;
	
	public double[][] dataSet;
	public double[][] coreDistances;

	public int k;

	public static final String WS = "WS";
	public static final String SS = "SS";

	public long numOfEdges = 0;

	public boolean smartFilter;
	public boolean naiveFilter;
	public boolean incremental;
	
	public boolean extended;
	
	public boolean debug = false;

	public long timenaivefilter = 0;

	public DistanceCalculator distanceFunction;

	/**
	 * Relative Neighborhood Graph naive constructor. Takes O(n³) time.
	 * 
	 * @param dataSet
	 * @param coreDistances
	 * @param distanceFunction
	 * @param k
	 */
	public RelativeNeighborhoodGraph(double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k){

		this.dataSet = dataSet;
		this.coreDistances = coreDistances;
		this.distanceFunction = distanceFunction;
		this.k = k;
		
		this.extended = false;
		
		RNG = new IntBigArrayBigList[dataSet.length];

		for (int i = 0; i < RNG.length; i++) {
			RNG[i] = new IntBigArrayBigList();
		}

		for (int i = 0; i < dataSet.length; i++) {
			for (int j = i + 1; j < dataSet.length; j++) {

				if (neighbors(dataSet, coreDistances, i, j, k)) {					
					
					RNG[i].add(j);
					RNG[j].add(i);

					numOfEdges++;
				}
			}
		}
	}


	/**
	 * Relative Neighborhood Graph constructor based on the Well-Separated Pair Decomposition.
	 * 
	 * @param dataSet
	 * @param coreDistances
	 * @param distanceFunction
	 * @param k
	 * @param s
	 * @param method
	 * @param smartFilter
	 * @param naiveFilter
	 */
	@SuppressWarnings("unchecked")
	public RelativeNeighborhoodGraph(double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k, double s, String method, boolean smartFilter, boolean naiveFilter, boolean incremental) {

		this.dataSet = dataSet;
		this.coreDistances = coreDistances;
		this.distanceFunction = distanceFunction;
		this.k = k;

		this.smartFilter = smartFilter;
		this.naiveFilter = naiveFilter;
		
		if (this.smartFilter || this.naiveFilter) { 
			this.incremental = incremental;
			
			this.extended = true;
			
			ExtendedRNG = new Int2ObjectOpenHashMap[dataSet.length];

			for (int i = 0; i < ExtendedRNG.length; i++) {
				ExtendedRNG[i] = new Int2ObjectOpenHashMap<DistanceLevel>();
			}
			
		} else {
			
			this.incremental = false;
			
			this.extended = false;
			
			RNG = new IntBigArrayBigList[dataSet.length];
			
			for (int i = 0; i < RNG.length; i++) {
				RNG[i] = new IntBigArrayBigList();
			}

		}
		

		// Builds the Fair Split Tree T from dataSet.
		FairSplitTree T = FairSplitTree.build(this.dataSet, this.coreDistances, this.k);

		// Finds all the Well-separated Pairs from T.
		findWSPD(T, s, method);

		// "Removes" unnecessary variables.
		T = null;
	}


	/**
	 * Relative Neighborhood Graph constructor based on the Well-Separated Pair Decomposition.
	 * 
	 * @param dataSet
	 * @param coreDistances
	 * @param distanceFunction
	 * @param k
	 * @param smartFilter
	 * @param naiveFilter
	 */
	public RelativeNeighborhoodGraph(double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k, boolean smartFilter, boolean naiveFilter, boolean incremental) {
		this(dataSet, coreDistances, distanceFunction, k, 1, RelativeNeighborhoodGraph.WS, smartFilter,  naiveFilter, incremental);
	}


	/**
	 * Relative Neighborhood Graph constructor based on the Well-Separated Pair Decomposition.
	 * 
	 * @param dataSet
	 * @param coreDistances
	 * @param k
	 * @param smartFilter
	 * @param naiveFilter
	 */
	public RelativeNeighborhoodGraph(double[][] dataSet, double[][] coreDistances, int k, boolean smartFilter, boolean naiveFilter, boolean incremental) {
		this(dataSet, coreDistances, new EuclideanDistance(), k, 1, RelativeNeighborhoodGraph.WS, smartFilter,  naiveFilter, incremental);
	}


	private boolean neighbors(int a, int b, int k) {

		// Smart filter.
		if (smartFilter) {

			double cdA = coreDistances[a][k-1];
			double cdB = coreDistances[b][k-1];

			double w = mutualReachabilityDistance(dataSet, coreDistances, a, b, k);

			// Check if the points are in each other's k-neighborhood.
			if (w == Math.max(cdA, cdB)) {

				int[] kNN;

				if (cdA > cdB) {
					kNN = IncrementalHDBSCANStar.kNN[a];
				} else {
					kNN = IncrementalHDBSCANStar.kNN[b];
				}

				for (int i = 0; i < kNN.length; i++) {

					if (coreDistances[kNN[i]][k-1] >= w) continue;

					double dac = mutualReachabilityDistance(dataSet, coreDistances, a, kNN[i], k);
					double dbc = mutualReachabilityDistance(dataSet, coreDistances, b, kNN[i], k);

					if (w > Math.max(dac, dbc)) {
						return false;
					}
				}

				return true;
			}

			int[] kNNa = IncrementalHDBSCANStar.kNN[a];
			int[] kNNb = IncrementalHDBSCANStar.kNN[b];

			for (int i = 0; i < kNNa.length; i++) {

				if (coreDistances[kNNa[i]][k-1] >= w) continue;

				double dac = mutualReachabilityDistance(dataSet, coreDistances, a, kNNa[i], k);
				double dbc = mutualReachabilityDistance(dataSet, coreDistances, b, kNNa[i], k);

				if (w > Math.max(dac, dbc)) {
					return false;
				}
			}		

			for (int i = 0; i < kNNb.length; i++) {

				if (coreDistances[kNNb[i]][k-1] >= w) continue;

				double dac = mutualReachabilityDistance(dataSet, coreDistances, a, kNNb[i], k);
				double dbc = mutualReachabilityDistance(dataSet, coreDistances, b, kNNb[i], k);

				if (w > Math.max(dac, dbc)) {
					return false;
				}
			}
		}

		// Naive filter.
		if (naiveFilter) {
			long start = System.currentTimeMillis();
			if (!neighbors(dataSet, coreDistances, a, b, k)) {
				return false;
			}
			timenaivefilter = timenaivefilter + (System.currentTimeMillis() - start);
		}

		return true;
	}

	private int neighbors(int a, int b, int k, boolean incremental) {

		int level = Integer.MAX_VALUE;

		if (neighbors(a, b, k)) {
			level = 0;
		}

		if (incremental) {
			
			if (level > k) {
				return level;
			}
			
			int start = 1;
			int end = k - 1;

			while (start <= end) {
				int middle;

				if (start == end) {
					middle = start;
				} else {
					middle = (int) Math.floor((end-start)/2) + start;
				}

				if (neighbors(a, b, middle)) {
					level = middle;
					end = middle - 1;
				} else {
					start = middle + 1;
				}
			}

		}

		return level;
	}

	private boolean neighbors(double[][] dataSet, double[][] coreDistances, int i, int j, int k) {
		double dij = mutualReachabilityDistance(dataSet, coreDistances, i, j, k);

		for (int m = 0; m < dataSet.length; m++) {

			double dim = mutualReachabilityDistance(dataSet, coreDistances, i, m, k);
			double djm = mutualReachabilityDistance(dataSet, coreDistances, j, m, k);

			if (dij > Math.max(dim, djm)) {
				return false;
			}
		}

		return true;
	}

	private void SBCN(FairSplitTree T1, FairSplitTree T2) {
		double d;

		HashSet<Pair> AB  = new HashSet<Pair>();

		double min = Double.MAX_VALUE;
		BigList<Integer> tempA = null;
		BigList<Integer> tempB = null;
		
		IntOpenHashSet B = new IntOpenHashSet((int)T1.getCount());
		
		for (int p1 : T1.retrieve()) {

			for (int p2 : T2.retrieve()) {

				d = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, p1, p2, k);

				if (d < min) {
					tempA = new IntBigArrayBigList();
					tempB = new IntBigArrayBigList();
				}

				if (d <= min) {
					min = d;
					tempA.add(p1);
					tempB.add(p2);
				}
			}
			
			for (int i = 0; i < tempA.size(); i++) {
				AB.add(new Pair(tempA.get(i), tempB.get(i)));
				B.add(tempB.get(i));
			}
			
			min = Double.MAX_VALUE;
		}

		
		for (int p2 : B) {
						
			for (int p1 : T1.retrieve()) {

				d = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, p1, p2, k);

				if (d < min) {
					tempA = new IntBigArrayBigList();
					tempB = new IntBigArrayBigList();
				}

				if (d <= min) {
					min = d;
					tempA.add(p1);
					tempB.add(p2);
				}
			}
			
			for (int i = 0; i < tempA.size(); i++) {

				Pair candidate = new Pair(tempA.get(i), tempB.get(i));

				if (AB.contains(candidate)) {
					
					int level = neighbors(candidate.a, candidate.b, k, incremental);

					if (level <= k) {

						if (this.extended) {
							double distance = distanceFunction.computeDistance(dataSet[candidate.a], dataSet[candidate.b]);
							
							DistanceLevel dl = new DistanceLevel(distance, level);
							
							ExtendedRNG[candidate.a].put(candidate.b, dl);
							ExtendedRNG[candidate.b].put(candidate.a, dl);
							
						} else {
							RNG[candidate.a].add(candidate.b);
							RNG[candidate.b].add(candidate.a);							
						}

						numOfEdges++;
					}					
				}
			}
			
			min = Double.MAX_VALUE;
		}
				
		tempA = null;
		tempB = null;
		AB = null;
	}

	private void findWSPD(FairSplitTree T, double s, String method) {
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

	private void findPairs(FairSplitTree T1, FairSplitTree T2, double s, String method) {

		if (separated(T1, T2, s, method)) {
			SBCN(T1, T2);
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
	 * @param method
	 * @return
	 */
	private static boolean separated(FairSplitTree T1, FairSplitTree T2, double s, String method) {

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
	@SuppressWarnings("unused")
	private static boolean rn(FairSplitTree T1, FairSplitTree T2) {
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
	private static boolean ws(FairSplitTree T1, FairSplitTree T2, double s){

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
	private static boolean ss(FairSplitTree T1, FairSplitTree T2, double s){

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

	/**
	 * @param dataSet
	 * @param coreDistances
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
	private double mutualReachabilityDistance(double[][] dataSet, double[][] coreDistances, int i, int j, int k) {
		return mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, i, j, k);
	}

	public double edgeWeight(int i, int j, int k) {
		if (this.extended) {
			return Math.max(this.ExtendedRNG[i].get(j).d, Math.max(coreDistances[i][k-1], coreDistances[j][k-1]));			
		} else {
			return mutualReachabilityDistance(dataSet, coreDistances, i, j, k);
		}			
	}

	public static class DistanceLevel {
		public double d;
		public int level;

		public DistanceLevel(double d, int level) {
			this.d = d;
			this.level = level;
		}
	}
}