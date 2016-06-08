package ca.ualberta.cs.hdbscanstar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.util.Combinations;
import ca.ualberta.cs.util.Data;

public class RelativeNeighborhoodGraph extends Graph {
	public int numOfEdgesMRG;

	public Integer[] edgesA;
	public Integer[] edgesB;
	public Double[] weights;

	public Integer[] sortedEdges;

	public BitSet inMST;

	public RelativeNeighborhoodGraph(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int minPoints, int a){

		List<Integer> tempA = new ArrayList<Integer>();
		List<Integer> tempB = new ArrayList<Integer>();
		List<Double>  tempW = new ArrayList<Double>();

		Double[][] S = Combinations.permutation(S(dataSet[0].length), dataSet[0].length);

		boolean[][] graph = new boolean[dataSet.length][dataSet.length];
		
		Map<Integer, Set<Integer>> w = Wk(dataSet, coreDistances, distanceFunction, S, minPoints);
		
		for (int v = 0; v < dataSet.length; v++) {

			for (int k = 0; k < S.length; k++) {
				
				List<Integer> cache = new ArrayList<>();
				double d = 0;				

				if (!w.get(k).isEmpty()) {
					double min = Double.MAX_VALUE;
					int closest = 0;

					for (Integer e : w.get(k)) {
						d = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, v, e, minPoints);
												
						if (d < min) {
							cache.clear();
						}
						
						if (d <= min && e != v) {							
							closest = e;
							min = d;
							cache.add(e);
						}
					}
										
					for (Integer c : cache) {
						if (!graph[v][c] && !graph[c][v]) {
							if (v < closest) {
								tempA.add(v);
								tempB.add(c);
							} else {
								tempA.add(c);
								tempB.add(v);
							}
							tempW.add(min);
														
							graph[v][c] = true;
							graph[c][v] = true;
						}
					}
				}
			}
		}
		
//		System.out.println("STEP 1: " + (System.currentTimeMillis() - s));

//		System.out.println("Initial Size: " + tempA.size());
		// Removes the edges that do not belong to RNG.
//		s = System.currentTimeMillis();
//		for (int e = tempA.size() - 1; e >= 0; e--) {
//			double dab = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, tempA.get(e), tempB.get(e), minPoints);
//
//			for (int v = 0; v < dataSet.length; v++) {
//
//				double dac = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, tempA.get(e), v, minPoints);
//				double dbc = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, tempB.get(e), v, minPoints);
//				
//				if (dac < dab && dbc < dab) {
//					tempA.remove(e);
//					tempB.remove(e);
//					tempW.remove(e);
//					System.out.println("removing");
//					break;
//				}
//			}
//		}
//		System.out.println("Final Size: " + tempA.size());
//		System.out.println("Filtering: " + (System.currentTimeMillis() - s));
		
		numOfEdgesMRG = tempA.size();

		edgesA = new Integer[tempA.size()];
		edgesB = new Integer[tempB.size()];
		weights = new Double[numOfEdgesMRG];

		sortedEdges = new Integer[numOfEdgesMRG];

		for (int i = 0; i < numOfEdgesMRG; i++) {
			sortedEdges[i] = i;
		}

		inMST = new BitSet(numOfEdgesMRG);
		inMST.clear();

		edgesA = tempA.toArray(edgesA);
		edgesB = tempB.toArray(edgesB);
		weights = tempW.toArray(weights);
	}

	public Map<Integer, Set<Integer>> Wk(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, Double[][] S, int minPoints){
		Map<Integer, Set<Integer>> Wk = new HashMap<Integer, Set<Integer>>(S.length);
		
		for (int i = 0; i < S.length; i++) {
			Wk.put(i, new HashSet<Integer>());
		}
//		System.out.println("Size: " + Wk.keySet().size());
		
		for (int w = 0; w < dataSet.length; w++) {

			double min = Double.MAX_VALUE;
			List<Integer> add = new ArrayList<Integer>();

			for (int j = 0; j < S.length; j++) {
				
				double d = MRD(S[j], Data.normalize(dataSet[w]), dataSet, distanceFunction, minPoints);
				
				if (d < min) {
					add.clear();
				}
				
				if (d <= min){
					add.add(j);
					min = d;
				}
			}

//			System.out.println(w + "\t" + add.size() + "\t" + add);
			
			for (int i = 0; i < add.size(); i++) {
				Wk.get(add.get(i)).add(w);
			}
		}

		return Wk;
	}

	public double[] setToVector(Set<Double> Sk){
		double[] result = new double[Sk.size()];
		int i = 0;

		for (double element : Sk) {
			result[i] = element;
		}

		return result;
	}

	public double distance(Double[] v, Double[] u){
		double d = 0;
		for (int i = 0; i < u.length; i++) {
			d += Math.pow((v[i] - u[i]),2);
		}
		return Math.sqrt(d);
	}

	public double coreDistance(Double[] v, Double[][] dataSet, DistanceCalculator distanceFunction, int k){
		int numNeighbors = k + 1;

		double[] kNNDistances = new double[numNeighbors];	//Sorted nearest distances found so far
		int[] kNN = new int[numNeighbors];

		for (int i = 0; i < numNeighbors; i++) {
			kNNDistances[i] = Double.MAX_VALUE;
			kNN[i] = Integer.MAX_VALUE;
		}

		for (int neighbor = 0; neighbor < dataSet.length; neighbor++) {

			double distance = distanceFunction.computeDistance(v, dataSet[neighbor]);

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

		return kNNDistances[k];
	}

	public double MRD(Double[] v, Double[] u, Double[][] dataSet, DistanceCalculator distanceFunction, int k){
		double distance = distance(v, u);
		double cdv = coreDistance(v, dataSet, distanceFunction, k);
		double cdu = coreDistance(u, dataSet, distanceFunction, k);

		return Math.max(distance, Math.max(cdv, cdu));
//		return distance;
	}

	public static Double[] u(Double[] v, Double[] w){
		return sumVector(v, Data.normalize(w));
	}

	public static Double[] sumVector(Double[] v, Double[] u){
		Double[] q = new Double[v.length];

		for (int i = 0; i < u.length; i++) {
			q[i] = v[i] + u[i];
		}

		return q;
	}

	public static Double[] S(int d){
		double g = 1/Math.sqrt(d);

		List<Double> P = new ArrayList<Double>();

		double element = -1;

		for (int i = 0; i <= Math.ceil(2/g); i++) {
			element = -1 + i*g;
			P.add(element);
		}
		
		Double[] S = new Double[(int) Math.ceil(2/g)];
		S = P.toArray(S);

		return S;
	}

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

	public boolean neighbors(Double[][] dataSet, double[][] coreDistances, int i, int j, int k){
		double dij = mutualReachabilityDistance(dataSet, coreDistances, new EuclideanDistance(), i, j, k);

		for (int m = 1; m < coreDistances.length; m++) {
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
