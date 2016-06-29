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

	// Method to find the Bichromatic Closest Pairs.
	public RelativeNeighborhoodGraph(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k, boolean filter, double s, String method) {
		double d;

		// Builds the Fair Split Tree T from dataSet.
//		long startTree = System.currentTimeMillis();
		FairSplitTree T = FairSplitTree.build(dataSet);
//		System.out.println("Fair Split Tree construction: " + (System.currentTimeMillis() - startTree));

		// Finds all the Well-separated Pairs from T.
//		long startWSPD = System.currentTimeMillis();
		WSPD.build(T, T, s, method);
//		System.out.println("WSPD construction: " + (System.currentTimeMillis() - startWSPD));

		ArrayList<Integer> A = new ArrayList<Integer>();
		ArrayList<Integer> B = new ArrayList<Integer>();
		ArrayList<Double> W = new ArrayList<Double>();

		HashMap<Pair, Double> temp = new HashMap<>();

		for (SeparatedPair pair : WSPD.pairs) {

			FairSplitTree T1 = pair.T1;
			FairSplitTree T2 = pair.T2;

			List<Integer> P1 = T1.retrieve();
			List<Integer> P2 = T2.retrieve();

			for (Integer p1 : P1) {
				double min = Double.MAX_VALUE;

				ArrayList<Integer> tempA = new ArrayList<Integer>();
				ArrayList<Integer> tempB = new ArrayList<Integer>();
				ArrayList<Double> tempW = new ArrayList<Double>();

				for (Integer p2 : P2) {

					d = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, p1, p2, k);

					if (d < min) {
						tempA.clear();
						tempB.clear();
						tempW.clear();
					}

					if (d <= min) {
						min = d;
						tempA.add(p1);
						tempB.add(p2);
						tempW.add(d);
					}
				}

				for (int i = 0; i < tempA.size(); i++) {
					Pair p;

					if (tempA.get(i) < tempB.get(i)) {
						p = new Pair(tempA.get(i), tempB.get(i));
					} else {
						p = new Pair(tempB.get(i), tempA.get(i));
					}

					if (!temp.containsKey(p)) temp.put(p, tempW.get(i));
				}
			}

			for (Integer p2 : P2) {
				double min = Double.MAX_VALUE;

				ArrayList<Integer> tempA = new ArrayList<Integer>();
				ArrayList<Integer> tempB = new ArrayList<Integer>();
				ArrayList<Double> tempW = new ArrayList<Double>();

				for (Integer p1 : P1) {

					d = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, p1, p2, k);

					if (d < min) {
						tempA.clear();
						tempB.clear();
						tempW.clear();
					}

					if (d <= min) {
						min = d;
						tempA.add(p2);
						tempB.add(p1);
						tempW.add(d);
					}
				}

				for (int i = 0; i < tempA.size(); i++) {
					Pair p;

					if (tempA.get(i) < tempB.get(i)) {
						p = new Pair(tempA.get(i), tempB.get(i));
					} else {
						p = new Pair(tempB.get(i), tempA.get(i));
					}

					if (!temp.containsKey(p)) temp.put(p, tempW.get(i));
				}
			}
		}
		
		for (Pair p : temp.keySet()) {
			A.add(p.a);
			B.add(p.b);
			W.add(temp.get(p));
		}

		if (filter) {
			
			for (int e = A.size() - 1; e >= 0; e--) {
				double dab = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, A.get(e), B.get(e), k);

				for (int v = 0; v < dataSet.length; v++) {

					double dac = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, A.get(e), v, k);
					double dbc = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, B.get(e), v, k);

					if (dac < dab && dbc < dab) {
						A.remove(e);
						B.remove(e);
						W.remove(e);
						break;
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

	private static double mutualReachabilityDistance(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int i, int j, int k) {
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
