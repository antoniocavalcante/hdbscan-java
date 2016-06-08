package ca.ualberta.cs.hdbscanstar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import ca.ualberta.cs.distance.DistanceCalculator;

public class MutualReachabilityGraph extends Graph {

	public static HashMap<Integer, ArrayList<Integer>> neighbors;
	public int numOfEdgesMRG;
	public static UF uf;

	public double[] XYw;
	
	public int[] edgesA;
	public int[] edgesB;
	public double[] weights;
	public BitSet inMST;
	public BitSet control;
	public Integer[] sortedEdges;
	public int change;

	public MutualReachabilityGraph(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k) {
		numOfEdgesMRG += uf.count() - 1;

		edgesA =  new int[numOfEdgesMRG];
		edgesB =  new int[numOfEdgesMRG];
		weights = new double[numOfEdgesMRG];

		inMST = new BitSet(numOfEdgesMRG);
		control = new BitSet(numOfEdgesMRG);

		inMST.clear();
		control.clear();

		sortedEdges = new Integer[numOfEdgesMRG];
		
		for (int i = 0; i < numOfEdgesMRG; i++) {
			sortedEdges[i] = i;
		}

		int index = 0;

		// Generates an edge between each point and its k nearest neighbors

		for (Integer i : neighbors.keySet()) {
			for (Integer j : neighbors.get(i)) {
				if (i != j) {
					edgesA[index] = i;
					edgesB[index] = j;

					double mutualReachabiltiyDistance = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, i, j, k);

					weights[index] = mutualReachabiltiyDistance;
					index++;
				}
			}
		}
	}


	public void updateWeights(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int k) {
		for (int i = 0; i < numOfEdgesMRG; i++) {
			this.weights[sortedEdges[i]] = mutualReachabilityDistance(dataSet, coreDistances, distanceFunction, this.edgesA[sortedEdges[i]], this.edgesB[sortedEdges[i]], k);
		}
	}

	private static double mutualReachabilityDistance(Double[][] dataSet, double[][] coreDistances, DistanceCalculator distanceFunction, int i, int j, int k) {
		double mutualReachabiltiyDistance = distanceFunction.computeDistance(dataSet[i], dataSet[j]);

		if (coreDistances[i][k - 1] > mutualReachabiltiyDistance)
			mutualReachabiltiyDistance = coreDistances[i][k - 1];
		if (coreDistances[j][k - 1] > mutualReachabiltiyDistance)
			mutualReachabiltiyDistance = coreDistances[j][k - 1];

		return mutualReachabiltiyDistance;
	}

	/**
	 * Quicksorts the graph by edge weight in descending order.  This quicksort implementation is 
	 * iterative and in-place.
	 */
	public Integer[] quicksortByEdgeWeight() {
		if (this.weights.length <= 1)
			return sortedEdges;

		int[] startIndexStack = new int[this.weights.length/2];
		int[] endIndexStack = new int[this.weights.length/2];

		startIndexStack[0] = 0;
		endIndexStack[0] = this.weights.length-1;
		int stackTop = 0;

		while (stackTop >= 0) {
			int startIndex = startIndexStack[stackTop];
			int endIndex = endIndexStack[stackTop];
			stackTop--;

			int pivotIndex = this.selectPivotIndex(startIndex, endIndex);
			pivotIndex = this.partition(startIndex, endIndex, pivotIndex);

			if (pivotIndex > startIndex+1) {
				startIndexStack[stackTop+1] = startIndex;
				endIndexStack[stackTop+1] = pivotIndex-1;
				stackTop++;
			}

			if (pivotIndex < endIndex-1) {
				startIndexStack[stackTop+1] = pivotIndex+1;
				endIndexStack[stackTop+1] = endIndex;
				stackTop++;
			}
		}
		return sortedEdges;
	}


	/**
	 * Quicksorts the graph in the interval [startIndex, endIndex] by edge weight.
	 * @param startIndex The lowest index to be included in the sort
	 * @param endIndex The highest index to be included in the sort
	 */
	@SuppressWarnings("unused")
	private void quicksort(int startIndex, int endIndex) {
		if (startIndex < endIndex) {
			int pivotIndex = this.selectPivotIndex(startIndex, endIndex);
			pivotIndex = this.partition(startIndex, endIndex, pivotIndex);
			this.quicksort(startIndex, pivotIndex-1);
			this.quicksort(pivotIndex+1, endIndex);
		}
	}

	/**
	 * Returns a pivot index by finding the median of edge weights between the startIndex, endIndex,
	 * and middle.
	 * @param startIndex The lowest index from which the pivot index should come
	 * @param endIndex The highest index from which the pivot index should come
	 * @return A pivot index
	 */
	private int selectPivotIndex(int startIndex, int endIndex) {
		if (startIndex - endIndex <= 1)
			return startIndex;

		int first = this.sortedEdges[startIndex];
		int middle = this.sortedEdges[startIndex + (endIndex-startIndex)/2];
		int last = this.sortedEdges[endIndex];

		if (weights[first] <= weights[middle]) {
			if (weights[middle] <= weights[last])
				return startIndex + (endIndex-startIndex)/2;
			else if (weights[last] >= weights[first])
				return endIndex;
			else
				return startIndex;
		}
		else {
			if (weights[first] <= weights[last])
				return startIndex;
			else if (weights[last] >= weights[middle])
				return endIndex;
			else
				return startIndex + (endIndex-startIndex)/2;
		}
	}


	/**
	 * Partitions the array in the interval [startIndex, endIndex] around the value at pivotIndex.
	 * @param startIndex The lowest index to  partition
	 * @param endIndex The highest index to partition
	 * @param pivotIndex The index of the edge weight to partition around
	 * @return The index position of the pivot edge weight after the partition
	 */
	private int partition(int startIndex, int endIndex, int pivotIndex) {
		double pivotValue = this.weights[sortedEdges[pivotIndex]];
		this.swapEdges(pivotIndex, endIndex);
		int lowIndex = startIndex;

		for (int i = startIndex; i < endIndex; i++) {
			if (this.weights[sortedEdges[i]] < pivotValue) {
				this.swapEdges(i, lowIndex);
				lowIndex++;
			}
		}

		this.swapEdges(lowIndex, endIndex);
		return lowIndex;
	}


	/**
	 * Swaps the vertices and edge weights between two index locations in the graph.
	 * @param indexOne The first index location
	 * @param indexTwo The second index location
	 */
	private void swapEdges(int indexOne, int indexTwo) {
		if (indexOne == indexTwo)
			return;

		int tmp = this.sortedEdges[indexOne];
		this.sortedEdges[indexOne] = this.sortedEdges[indexTwo];
		this.sortedEdges[indexTwo] = tmp;
	}


	public Integer[] bubbleSort(){
		boolean swapped = false;

		int start = 0;
		int end = sortedEdges.length - 1;

		control.clear();

		do {
			swapped = false;

			for (int i = end; i > start; i--) {
				if (weights[sortedEdges[i - 1]] > weights[sortedEdges[i]]) {
					int tmp = sortedEdges[i - 1];
					sortedEdges[i - 1] = sortedEdges[i];
					sortedEdges[i] = tmp;
					swapped = true;

					if (!inMST.get(sortedEdges[i - 1]) && inMST.get(sortedEdges[i])) {
						control.set(sortedEdges[i - 1], true);
					}
				}
			}
			start++;

		} while (swapped);

		return sortedEdges;
	}

	public Integer[] bubbleSort(int start, int end){
		boolean swapped = false;
		int lastSorted = start;

		control.clear();

		do {
			swapped = false;

			for (int i = end; i > lastSorted; i--) {
				if (weights[sortedEdges[i - 1]] > weights[sortedEdges[i]]) {
					int tmp = sortedEdges[i - 1];
					sortedEdges[i - 1] = sortedEdges[i];
					sortedEdges[i] = tmp;
					swapped = true;

					if (!inMST.get(sortedEdges[i - 1]) && inMST.get(sortedEdges[i])) {
						control.set(sortedEdges[i - 1], true);
					}
				}
			}
			lastSorted++;

		} while (swapped && lastSorted < end);

		return sortedEdges;
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
	
	public Integer[] timSort2(Integer[] sortedEdges, int t){
		Comparator<Integer> c = new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				if (XYw[o1] < XYw[o2]) {
					return -1;
				}
				if (XYw[o1] > XYw[o2]) {					
					return 1;
				}
				return 0;
			}
		};

		TimSort.sort(sortedEdges, c);
		TimSort.sort(sortedEdges, 0, t, c);
		return sortedEdges;
	}
}
