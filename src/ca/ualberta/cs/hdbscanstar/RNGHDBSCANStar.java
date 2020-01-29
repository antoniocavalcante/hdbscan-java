package ca.ualberta.cs.hdbscanstar;

import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.util.Dataset;

public class RNGHDBSCANStar {

	public static int[][] kNN;

	/**
	 * Calculates the core distances for each point in the data set, given some value for k.
	 * @param dataSet A double[][] where index [i][j] indicates the jth attribute of data point i
	 * @param k Each point's core distance will be it's distance to the kth nearest neighbor
	 * @param distanceFunction A DistanceCalculator to compute distances between points
	 * @return An array of core distances
	 */
	public static double[][] calculateCoreDistancesUselesslll(Dataset dataSet, int k, DistanceCalculator distanceFunction) {
		int numNeighbors = k;

		double[][] coreDistances = new double[dataSet.length()][numNeighbors];
		int[][] kNN = new int[dataSet.length()][numNeighbors];

		if (k == 1) {

			for (int point = 0; point < dataSet.length(); point++) {
				coreDistances[point][0] = 0;
				kNN[point][0] = point;
			}

			RNGHDBSCANStar.kNN = kNN;
			
			return coreDistances;
		}

		for (int point = 0; point < dataSet.length(); point++) {
			double[] kNNDistances = new double[numNeighbors];	//Sorted nearest distances found so far

			for (int i = 0; i < numNeighbors; i++) {
				kNNDistances[i] = Double.MAX_VALUE;
				kNN[point][i] = Integer.MAX_VALUE;
			}

			for (int neighbor = 0; neighbor < dataSet.length(); neighbor++) {

				double distance = dataSet.computeDistance(point, neighbor);

				//Check at which position in the nearest distances the current distance would fit:
				int neighborIndex = numNeighbors;
				while (neighborIndex >= 1 && distance < kNNDistances[neighborIndex-1]) {
					neighborIndex--;
				}

				//Shift elements in the array to make room for the current distance:
				if (neighborIndex < numNeighbors) {

					for (int shiftIndex = numNeighbors-1; shiftIndex > neighborIndex; shiftIndex--) {
						kNNDistances[shiftIndex] = kNNDistances[shiftIndex-1];
						kNN[point][shiftIndex] = kNN[point][shiftIndex-1];
					}
					kNNDistances[neighborIndex] = distance;
					kNN[point][neighborIndex] = neighbor;
				}
			}

			coreDistances[point] = kNNDistances;
		}

		RNGHDBSCANStar.kNN = kNN;
		
		return coreDistances;
	}
}
