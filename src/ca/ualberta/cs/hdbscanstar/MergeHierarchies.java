package ca.ualberta.cs.hdbscanstar;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import ca.ualberta.cs.util.FibonacciHeap;
import ca.ualberta.cs.util.FibonacciHeapNode;
import ca.ualberta.cs.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

public class MergeHierarchies {
	
	public static Int2DoubleOpenHashMap[] G;
	
	public static int length;
	
	public MergeHierarchies(int n){
		MergeHierarchies.length = n;
		
		MergeHierarchies.G = new Int2DoubleOpenHashMap[MergeHierarchies.length];
		
		for (int i = 0; i < G.length; i++) {
			G[i] = new Int2DoubleOpenHashMap();
		}
	}
	
	@SuppressWarnings("unused")
	public static void merge(UndirectedGraph[] MSTs){

		for (int i = 1; i < MSTs.length; i++) {

			for (int j = 0; j < MSTs[i].getNumEdges(); j++) {
				int u = MSTs[i].verticesA[j];
				int v = MSTs[i].verticesB[j];

				Pair p = new Pair(u,v);

				if (!G[u].containsKey(v)) {
					G[u].put(v, 0);
				}
				if (!G[v].containsKey(u)) {
					G[v].put(u, 0);
				}

				double max = 0;
				double w = 0;

				for (int m = 1; m < MSTs.length; m++) {

					if (edgeInHierarchy(u, v, MSTs[m])) {
						
						if (levels(MSTs[m])/(edgeLevels(u, v, MSTs[m])) > max) {
							max = (levels(MSTs[m])/edgeLevels(u, v, MSTs[m]));
							w = edgeWeight(u, v, MSTs[m]);
						}
					}
				}
				
				G[u].put(v, w);
				G[v].put(u, w);

			}
		}
		
		for (int i = 0; i < G.length; i++) {
			for (int j : G[i].keySet()) {
				System.out.println("[" + i + ", " + j + "]: " + G[i].get(j));
			}
		}
	}

	private static double levels(UndirectedGraph MST){

		HashSet<Double> distinctW = new HashSet<>();

		for (int i = 0; i < MST.getNumEdges(); i++) {
			distinctW.add(MST.edgeWeights[i]);
		}

		return distinctW.size();
	}

	private static boolean edgeInHierarchy(int u, int v, UndirectedGraph MST) {

		for (int i = 0; i < MST.getNumEdges(); i++) {
			if (MST.verticesA[i] == u && MST.verticesB[i] == v) {
				return true;
			}

			if (MST.verticesA[i] == v && MST.verticesB[i] == u) {
				return true;
			}
		}
		
		return false;
	}

	private static double edgeLevels(int u, int v, UndirectedGraph MST) {

		boolean start = false;
		HashSet<Double> distinct = new HashSet<>();

		for (int i = 0; i < MST.getNumEdges(); i++) {

			if (MST.verticesA[i] == u && MST.verticesB[i] == v) {
				start = true;
			}

			if (MST.verticesA[i] == v && MST.verticesB[i] == u) {
				start = true;
			}
			
			if (start) {
				distinct.add(MST.getEdgeWeightAtIndex(i));
			}

		}
		
		return distinct.size();
	}

	private static double edgeWeight(int u, int v, UndirectedGraph MST) {

		for (int i = 0; i < MST.getNumEdges(); i++) {

			if (MST.verticesA[i] == u && MST.verticesB[i] == v) {
				return MST.getEdgeWeightAtIndex(i);
			}

			if (MST.verticesA[i] == v && MST.verticesB[i] == u) {
				return MST.getEdgeWeightAtIndex(i);
			}
		}

		return 0;
	}

	public static UndirectedGraph constructMST(Int2DoubleOpenHashMap[] G) {

		int selfEdgeCapacity = 0;

		//One bit is set (true) for each attached point, or unset (false) for unattached points:
		BitSet attachedPoints = new BitSet(length);

		//Each point has a current neighbor point in the tree, and a current nearest distance:
		int[] nearestMRDNeighbors = new int[length-1 + selfEdgeCapacity];
		double[] nearestMRDDistances = new double[length-1 + selfEdgeCapacity];

		FibonacciHeap<Integer> q = new FibonacciHeap<Integer>();
		HashMap<Integer, FibonacciHeapNode<Integer>> map = new HashMap<Integer, FibonacciHeapNode<Integer>>();

		for (int i = 0; i < length-1; i++) {
			nearestMRDDistances[i] = Double.MAX_VALUE;
			map.put(i, new FibonacciHeapNode<Integer>(i));
			q.insert(map.get(i), Double.MAX_VALUE);
		}

		//The MST is expanded starting with the last point in the data set:
		int numAttachedPoints = 0;

		q.insert(new FibonacciHeapNode<Integer>(length - 1), 0);

		//Continue attaching points to the MST until all points are attached:
		while (numAttachedPoints < length) {

			int currentPoint = q.removeMin().getData();

			//Attach the closest point found in this iteration to the tree:
			attachedPoints.set(currentPoint);

			numAttachedPoints++;

			for (Iterator<Integer> i = G[currentPoint].keySet().iterator(); i.hasNext();) {

				int neighbor = i.next();

				if (attachedPoints.get(neighbor) == true)
					continue;

				double w = G[currentPoint].get(neighbor);

				if (w < nearestMRDDistances[neighbor]) {
					nearestMRDDistances[neighbor] = w;
					nearestMRDNeighbors[neighbor] = currentPoint;

					q.decreaseKey(map.get(neighbor), w);
				}
			}

		}

		//Create an array for vertices in the tree that each point attached to:
		int[] otherVertexIndices = new int[length-1 + selfEdgeCapacity];
		for (int i = 0; i < length-1; i++) {
			otherVertexIndices[i] = i;
		}

		map = null;
		q = null;

		return new UndirectedGraph(length, nearestMRDNeighbors, otherVertexIndices, nearestMRDDistances);
	}
}
