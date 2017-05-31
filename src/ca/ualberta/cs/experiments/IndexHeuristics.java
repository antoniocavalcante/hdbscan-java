package ca.ualberta.cs.experiments;

import java.io.IOException;
import java.util.Random;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.util.KdTree;

public class IndexHeuristics {

	public static double[][] dataSet = null;

	public static long sequential(double[][] dataSet, int q, int k) {
		long start = System.currentTimeMillis();
		
		int[] kNN = new int[k];
		double[] kNNDistances = new double[k];	//Sorted nearest distances found so far
		
		for (int i = 0; i < k; i++) {
			kNNDistances[i] = Double.MAX_VALUE;
			kNN[i] = Integer.MAX_VALUE;
		}
		
		for (int neighbor = 0; neighbor < dataSet.length; neighbor++) {

			double distance = (new EuclideanDistance()).computeDistance(dataSet[q], dataSet[neighbor]);

			
			//Check at which position in the nearest distances the current distance would fit:
			int neighborIndex = k;
			while (neighborIndex >= 1 && distance < kNNDistances[neighborIndex-1]) {
				neighborIndex--;
			}

			//Shift elements in the array to make room for the current distance:
			if (neighborIndex < k) {

				for (int shiftIndex = k-1; shiftIndex > neighborIndex; shiftIndex--) {
					kNNDistances[shiftIndex] = kNNDistances[shiftIndex-1];
					kNN[shiftIndex] = kNN[shiftIndex-1];
				}
				kNNDistances[neighborIndex] = distance;
				kNN[neighborIndex] = neighbor;
			}
		}

		return (System.currentTimeMillis() - start);		
	}

	public static long index(int q, int k, KdTree kdTree) {				
		long start = System.currentTimeMillis();

		kdTree.nearestNeighbourSearch(k, q);

		return (System.currentTimeMillis() - start);
	}

	public static void main(String[] args) {

		// Loads data set.
		try {
			dataSet = HDBSCANStar.readInDataSet(args[0], " ");
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}

		// Loads number of neighbors k.
		int k = Integer.parseInt(args[1]);

		// Loads the number of queries.
		int numQueries = Integer.parseInt(args[2]);

		// Creates index.
		KdTree kdTree = new KdTree(dataSet);

		long sequentialTime = 0;
		long indexTime = 0;

		Random randomGenerator = new Random();

		// Runs numQueries random kNN queries using sequential scan and index.
		for (int i = 0; i < numQueries; i++) {
			// Selects random point.
			int q = randomGenerator.nextInt(dataSet.length);
			
			// Runs index.
			indexTime += index(q, k, kdTree);

			// Runs sequential.
			sequentialTime += sequential(dataSet, q, k);
		}

//		sequentialTime = sequentialTime/numQueries;
//		indexTime = indexTime/numQueries;

		System.out.println("Ind Time: " + indexTime);
		System.out.println("Seq Time: " + sequentialTime);
		
		if (indexTime < sequentialTime) {
			System.out.println("true");
		} else {
			System.out.println("false");
		}
	}
}
