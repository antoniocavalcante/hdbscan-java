package ca.ualberta.cs.main;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;

import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;
import ca.ualberta.cs.util.Dataset;
import ca.ualberta.cs.util.FibonacciHeap;
import ca.ualberta.cs.util.FibonacciHeapNode;

public class Prim {

	@SuppressWarnings("unused")
	private static class Pair implements Comparable<Pair> {
		public int vertex;
		public double priority;

		public Pair(int vertex, double priority) {
			this.vertex = vertex;
			this.priority = priority;
		}

		@Override
		public int compareTo(Pair o) {
			return Double.compare(this.priority, o.priority);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + vertex;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair) obj;
			if (vertex != other.vertex)
				return false;
			return true;
		}
	}

	public static UndirectedGraph constructMST(Dataset dataSet, double[][] coreDistances, int minPoints,	boolean selfEdges, RelativeNeighborhoodGraph RNG) {

		int selfEdgeCapacity = 0;
		if (selfEdges)
			selfEdgeCapacity = dataSet.length();

		//One bit is set (true) for each attached point, or unset (false) for unattached points:
		BitSet attachedPoints = new BitSet(dataSet.length());

		//Each point has a current neighbor point in the tree, and a current nearest distance:
		int[] nearestMRDNeighbors = new int[dataSet.length()-1 + selfEdgeCapacity];
		double[] nearestMRDDistances = new double[dataSet.length()-1 + selfEdgeCapacity];

		FibonacciHeap<Integer> q = new FibonacciHeap<Integer>();
		HashMap<Integer, FibonacciHeapNode<Integer>> map = new HashMap<Integer, FibonacciHeapNode<Integer>>();

		for (int i = 0; i < dataSet.length()-1; i++) {
			nearestMRDDistances[i] = Double.MAX_VALUE;
			map.put(i, new FibonacciHeapNode<Integer>(i));
			q.insert(map.get(i), Double.MAX_VALUE);
		}

		//The MST is expanded starting with the last point in the data set:
		int numAttachedPoints = 0;

		q.insert(new FibonacciHeapNode<Integer>(dataSet.length() - 1), 0);

		//Continue attaching points to the MST until all points are attached:
		while (numAttachedPoints < dataSet.length()) {

			int currentPoint = q.removeMin().getData();

			//Attach the closest point found in this iteration to the tree:
			attachedPoints.set(currentPoint);

			numAttachedPoints++;

			if (RNG.extended) {

				//Iterate through all unattached points, updating distances using the current point:
				for (Iterator<Integer> i = RNG.ExtendedRNG[currentPoint].keySet().iterator(); i.hasNext();) {

					int neighbor = i.next();

					if (attachedPoints.get(neighbor) == true)
						continue;

					if (RNG.incremental && RNG.ExtendedRNG[currentPoint].get(neighbor).level > minPoints) {
						i.remove();
						continue;
					}					

					double mutualReachabiltiyDistance = RNG.edgeWeight(currentPoint, neighbor, minPoints);

					if (mutualReachabiltiyDistance < nearestMRDDistances[neighbor]) {
						nearestMRDDistances[neighbor] = mutualReachabiltiyDistance;
						nearestMRDNeighbors[neighbor] = currentPoint;

						q.decreaseKey(map.get(neighbor), mutualReachabiltiyDistance);
					}
				}
				
			} else {

				for (Iterator<Integer> i = RNG.RNG[currentPoint].iterator(); i.hasNext();) {

					int neighbor = i.next();

					if (attachedPoints.get(neighbor) == true)
						continue;

					double mutualReachabiltiyDistance = RNG.edgeWeight(currentPoint, neighbor, minPoints);

					if (mutualReachabiltiyDistance < nearestMRDDistances[neighbor]) {
						nearestMRDDistances[neighbor] = mutualReachabiltiyDistance;
						nearestMRDNeighbors[neighbor] = currentPoint;

						q.decreaseKey(map.get(neighbor), mutualReachabiltiyDistance);
					}
				}
			}
		}

		//Create an array for vertices in the tree that each point attached to:
		int[] otherVertexIndices = new int[dataSet.length()-1 + selfEdgeCapacity];
		for (int i = 0; i < dataSet.length()-1; i++) {
			otherVertexIndices[i] = i;
		}

		//If necessary, attach self edges:
		if (selfEdges) {
			for (int i = dataSet.length()-1; i < dataSet.length()*2-1; i++) {
				int vertex = i - (dataSet.length()-1);
				nearestMRDNeighbors[i] = vertex;
				otherVertexIndices[i] = vertex;
				nearestMRDDistances[i] = coreDistances[vertex][minPoints];
			}
		}

		map = null;
		q = null;

		return new UndirectedGraph(dataSet.length(), nearestMRDNeighbors, otherVertexIndices, nearestMRDDistances);
	}
}