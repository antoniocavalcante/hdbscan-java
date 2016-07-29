package ca.ualberta.cs.hdbscanstar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.util.FairSplitTree;
import ca.ualberta.cs.util.Pair;
import ca.ualberta.cs.util.SeparatedPair;
import ca.ualberta.cs.util.WSPD;

public class RelativeNeighborhoodGraph extends Graph {
	public int numOfEdgesMRG;

	public Integer[] edgesA;
	public Integer[] edgesB;
	public Double[] weights;

	public Integer[] sortedEdges;

	public List<Integer> parents;
	
	public BitSet inMST;

	public RelativeNeighborhoodGraph(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k){
		ArrayList<Integer> A = new ArrayList<Integer>();
		ArrayList<Integer> B = new ArrayList<Integer>();
		ArrayList<Double> W = new ArrayList<Double>();

		for (int i = 0; i < dataSet.length; i++) {
			for (int j = i + 1; j < dataSet.length; j++) {

				if (neighbors(dataSet, coreDistances, i, j, k)) {
					A.add(i);
					B.add(j);
					W.add(mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, i, j, k));
				}
			}
		}

		numOfEdgesMRG = A.size();
		edgesA =  new Integer[numOfEdgesMRG];
		edgesB =  new Integer[numOfEdgesMRG];
		weights = new Double[numOfEdgesMRG];

		sortedEdges = new Integer[numOfEdgesMRG];

		inMST = new BitSet(numOfEdgesMRG);
		inMST.clear();

		for (int i = 0; i < numOfEdgesMRG; i++) {
			sortedEdges[i] = i;
		}

		edgesA = A.toArray(edgesA);
		edgesB = B.toArray(edgesB);
		weights = W.toArray(weights);
	}


	public RelativeNeighborhoodGraph(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k, boolean filter, double s, String method) {

		// Builds the Fair Split Tree T from dataSet.
		FairSplitTree T = FairSplitTree.build(dataSet);

		// Finds all the Well-separated Pairs from T.
		WSPD.build(T, T, s, method);

		ArrayList<Integer> A = new ArrayList<Integer>();
		ArrayList<Integer> B = new ArrayList<Integer>();
		
		ArrayList<FairSplitTree> C = new ArrayList<FairSplitTree>();
		
		ArrayList<Double> W = new ArrayList<Double>();
		
		HashMap<Pair, FairSplitTree> temp = new HashMap<>();

		temp = BCN(dataSet, coreDistances, distanceFunction, k, filter);

		for (Pair p : temp.keySet()) {
			A.add(p.a);
			B.add(p.b);
			
			C.add(temp.get(p));
			
			W.add(mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, p.a, p.b, k));
		}

		if (filter) {
			
			for (int e = A.size() - 1; e >= 0; e--) {
				
				double cdA = coreDistances[A.get(e)][k-1];
				double cdB = coreDistances[B.get(e)][k-1];

				double w = W.get(e);

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
							A.remove(e);
							B.remove(e);
							W.remove(e);
							break;
						}							
					}
					
				} else {
					
					// Tries to find a point in the smaller ancestor on the FairSplitTree for the nodes that contains both points.
					
					boolean keepLooking = true;
					
					for (Integer v : C.get(e).P) {

						double dac = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, A.get(e), v, k);
						double dbc = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, B.get(e), v, k);

						if (dac < w && dbc < w) {
							A.remove(e);
							B.remove(e);
							W.remove(e);
							keepLooking = false;
							break;
						}
					}
					
					// If no point in the lune was found in the previous step, then try with the neighborhood of each point.
					if (keepLooking) {
						ArrayList<Integer> neighbors = new ArrayList<>();
						
						neighbors.addAll(FairSplitTree.rangeSearch(T, dataSet[A.get(e)], W.get(e), new ArrayList<Integer>()));
						neighbors.addAll(FairSplitTree.rangeSearch(T, dataSet[B.get(e)], W.get(e), new ArrayList<Integer>()));

						for (Integer v : neighbors) {

							double dac = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, A.get(e), v, k);
							double dbc = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, B.get(e), v, k);

							if (dac < w && dbc < w) {
								A.remove(e);
								B.remove(e);
								W.remove(e);
								break;
							}
						}	
					}
				}
			}
		}
		
		numOfEdgesMRG = A.size();
		edgesA =  new Integer[numOfEdgesMRG];
		edgesB =  new Integer[numOfEdgesMRG];
		weights = new Double[numOfEdgesMRG];

		sortedEdges = new Integer[numOfEdgesMRG];

		inMST = new BitSet(numOfEdgesMRG);
		inMST.clear();

		for (int i = 0; i < numOfEdgesMRG; i++) {
			sortedEdges[i] = i;
		}

		edgesA = A.toArray(edgesA);
		edgesB = B.toArray(edgesB);
		weights = W.toArray(weights);
	}

	public HashMap<Pair, FairSplitTree> BCN(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k, boolean filter) {
		double d;

		HashMap<Pair, FairSplitTree> tmp = new HashMap<>();

		for (SeparatedPair pair : WSPD.pairs) {

			FairSplitTree T1 = pair.T1;
			FairSplitTree T2 = pair.T2;

			List<Integer> P1 = T1.retrieve();
			List<Integer> P2 = T2.retrieve();

			for (Integer p1 : P1) {
				double min = Double.MAX_VALUE;

				ArrayList<Integer> tempA = new ArrayList<Integer>();
				ArrayList<Integer> tempB = new ArrayList<Integer>();

				for (Integer p2 : P2) {

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

					if (!tmp.containsKey(p)) tmp.put(p, FairSplitTree.parent(T1, T2));
				}
			}

			for (Integer p2 : P2) {
				double min = Double.MAX_VALUE;

				ArrayList<Integer> tempA = new ArrayList<Integer>();
				ArrayList<Integer> tempB = new ArrayList<Integer>();

				for (Integer p1 : P1) {

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

					if (!tmp.containsKey(p)) tmp.put(p, FairSplitTree.parent(T1, T2));
				}
			}
		}

		return tmp;
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

	public static double mutualReachabilityDistance(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int i, int j, int k) {
		double mutualReachabiltiyDistance = distanceFunction.computeDistance(dataSet[i], dataSet[j]);

		if (coreDistances[i][k - 1] > mutualReachabiltiyDistance)
			mutualReachabiltiyDistance = coreDistances[i][k - 1];
		
		if (coreDistances[j][k - 1] > mutualReachabiltiyDistance)
			mutualReachabiltiyDistance = coreDistances[j][k - 1];

		return mutualReachabiltiyDistance;
	}

	public void updateWeights(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k) {
		for (int i = 0; i < numOfEdgesMRG; i++) {
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
