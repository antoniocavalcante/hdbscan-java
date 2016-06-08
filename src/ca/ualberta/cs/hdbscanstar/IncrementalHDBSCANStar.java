package ca.ualberta.cs.hdbscanstar;

import java.util.ArrayList;
import java.util.HashMap;

import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;

public class IncrementalHDBSCANStar {

	/**
	 * Constructs the minimum spanning tree of mutual reachability distances for the data set, given
	 * the core distances for each point.
	 * @param dataSet A double[][] where index [i][j] indicates the jth attribute of data point i
	 * @param coreDistances An array of core distances for each data point
	 * @param selfEdges If each point should have an edge to itself with weight equal to core distance
	 * @param distanceFunction A DistanceCalculator to compute distances between points
	 * @return An MST for the data set using the mutual reachability distances
	 */

	public static UndirectedGraph kruskal(Double[][] dataSet, RelativeNeighborhoodGraph G, double[][] coreDistances, boolean selfEdges, DistanceCalculator distanceFunction, int k) {
		int n = dataSet.length;

		int selfEdgeCapacity = 0;
		
		if (selfEdges)
			selfEdgeCapacity = n;

		int[] A = new int[n - 1 + selfEdgeCapacity];
		int[] B = new int[n - 1 + selfEdgeCapacity];
		double[] MSTweights = new double[n - 1 + selfEdgeCapacity];

		Integer[] sortedEdges =  new Integer[G.numOfEdgesMRG];

		sortedEdges = G.timSort();

		//Kruskal
		UF uf = new UF(n);
		int m = 0;
		
		for (int i = 0; i < G.numOfEdgesMRG; i++) {
			int e = sortedEdges[i];
			
			if (!uf.connected(G.edgesA[e], G.edgesB[e])) {
				uf.union(G.edgesA[e], G.edgesB[e]);
				A[m] = G.edgesA[e];
				B[m] = G.edgesB[e];
				MSTweights[m] = G.weights[e];
				G.inMST.set(e);
				m++;
			}
			
			if (m == n - 1) {
				break;
			}
		}
		
		if (uf.count() > 1) System.err.println("Disconnected input graph!");
		
		if (selfEdges) {
			for (int i = n-1; i < n*2-1; i++) {
				int vertex = i - (n-1);
				A[i] = vertex;
				B[i] = vertex;
				MSTweights[i] = coreDistances[vertex][k];
			}
		}

		return new UndirectedGraph(n, A, B, MSTweights);
	}

	public static UndirectedGraph kruskal(Double[][] dataSet, MutualReachabilityGraph G, double[][] coreDistances, boolean selfEdges, DistanceCalculator distanceFunction, int k) {
		int n = dataSet.length;

		int selfEdgeCapacity = 0;
		if (selfEdges)
			selfEdgeCapacity = n;

		int[] A = new int[n - 1 + selfEdgeCapacity];
		int[] B = new int[n - 1 + selfEdgeCapacity];
		double[] MSTweights = new double[n - 1 + selfEdgeCapacity];

		System.out.println("Number of edges: " + G.numOfEdgesMRG);
		Integer[] sortedEdges =  new Integer[G.numOfEdgesMRG];

		sortedEdges = G.timSort();

		//Kruskal
		UF uf = new UF(n);
		int m = 0;

		for (int i = 0; i < G.numOfEdgesMRG; i++) {
			int e = sortedEdges[i];
			if (!uf.connected(G.edgesA[e], G.edgesB[e])) {
				uf.union(G.edgesA[e], G.edgesB[e]);
				A[m] = G.edgesA[e];
				B[m] = G.edgesB[e];
				MSTweights[m] = G.weights[e];
				G.inMST.set(e);
				m++;
			}

			if (m == n - 1) {
				break;
			}			
		}

		if (selfEdges) {
			for (int i = n-1; i < n*2-1; i++) {
				int vertex = i - (n-1);
				A[i] = vertex;
				B[i] = vertex;
				MSTweights[i] = coreDistances[vertex][k];
			}
		}

		return new UndirectedGraph(n, A, B, MSTweights);
	}


	/**
	 * Constructs the minimum spanning tree of mutual reachability distances for the data set, given
	 * the core distances for each point.
	 * @param dataSet A double[][] where index [i][j] indicates the jth attribute of data point i
	 * @param coreDistances An array of core distances for each data point
	 * @param selfEdges If each point should have an edge to itself with weight equal to core distance
	 * @param distanceFunction A DistanceCalculator to compute distances between points
	 * @return An MST for the data set using the mutual reachability distances
	 */
	public static UndirectedGraph updateMST(Double[][] dataSet, MutualReachabilityGraph MRG, double[][] coreDistances, boolean selfEdges, DistanceCalculator distanceFunction, int k) {
		int n = dataSet.length;

		int selfEdgeCapacity = 0;
		if (selfEdges)
			selfEdgeCapacity = n;

		MRG.updateWeights(dataSet, coreDistances, new EuclideanDistance(), k);

		int[] A = new int[n - 1 + selfEdgeCapacity];
		int[] B = new int[n - 1 + selfEdgeCapacity];
		double[] MSTweights = new double[n - 1 + selfEdgeCapacity];

		Integer[] sortedEdges =  new Integer[MRG.numOfEdgesMRG];

		// Apply TimSort to the Edges before updating the MST
		//		sortedEdges = MRG.timSort();
		sortedEdges = MRG.bubbleSort();

		UF uf = new UF(n);
		int count = 0;
		int m = 0;
		for (int i = 0; i < sortedEdges.length; i++) {
			int e = sortedEdges[i];

			if (!MRG.control.get(e) && count == 0) {
				uf.union(MRG.edgesA[e], MRG.edgesB[e]);
				A[m] = MRG.edgesA[e];
				B[m] = MRG.edgesB[e];
				MSTweights[m] = MRG.weights[e];
				MRG.inMST.set(e);
				m++;
			} else {
				if (uf.find(MRG.edgesA[e]) != uf.find(MRG.edgesB[e])) {
					//edge e belongs to the MST
					uf.union(MRG.edgesA[e], MRG.edgesB[e]);
					A[m] = MRG.edgesA[e];
					B[m] = MRG.edgesB[e];
					MSTweights[m] = MRG.weights[e];
					m++;
					count++;
					MRG.inMST.set(e);
				} else {
					//edge e does not belongs to the MST
					if (MRG.inMST.get(e)) {
						count--;
					}
					MRG.inMST.set(e, false);
				}
			}

			if (m == n - 1) {
				break;
			}
		}

		//If necessary, attach self edges:
		if (selfEdges) {
			for (int i = n-1; i < n*2-1; i++) {
				int vertex = i - (n-1);
				A[i] = vertex;
				B[i] = vertex;
				MSTweights[i] = coreDistances[vertex][k];
			}
		}

		return new UndirectedGraph(n, A, B, MSTweights);
	}


	/**
	 * Calculates the core distances for each point in the data set, given some value for k.
	 * @param dataSet A double[][] where index [i][j] indicates the jth attribute of data point i
	 * @param k Each point's core distance will be it's distance to the kth nearest neighbor
	 * @param distanceFunction A DistanceCalculator to compute distances between points
	 * @return An array of core distances
	 */
	public static double[][] calculateCoreDistances(Double[][] dataSet, int k, DistanceCalculator distanceFunction) {
		int numNeighbors = k + 1;
		MutualReachabilityGraph.neighbors = new HashMap<Integer, ArrayList<Integer>>(dataSet.length);
		double[][] coreDistances = new double[dataSet.length][numNeighbors];

		if (k == 1) {
			for (int point = 0; point < dataSet.length; point++) {
				coreDistances[point][0] = 0;
			}
			return coreDistances;
		}

		for (int point = 0; point < dataSet.length; point++) {
			double[] kNNDistances = new double[numNeighbors];	//Sorted nearest distances found so far
			int[] kNN = new int[numNeighbors];

			for (int i = 0; i < numNeighbors; i++) {
				kNNDistances[i] = Double.MAX_VALUE;
				kNN[i] = Integer.MAX_VALUE;
			}

			for (int neighbor = 0; neighbor < dataSet.length; neighbor++) {

				double distance = distanceFunction.computeDistance(dataSet[point], dataSet[neighbor]);

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

			coreDistances[point] = kNNDistances;

		}

		return coreDistances;
	}
}
