package ca.ualberta.cs.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;

public class CoreDistances {

	public static double[][] dataSet = null;

	public static double[][] coreDistances = null;
	public static int[][] kNN = null;
	
	public static void main(String[] args) {

		try {
			dataSet = HDBSCANStar.readInDataSet(args[0], " ");
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}

		calculateCoreDistances(dataSet, Integer.parseInt(args[1]), new EuclideanDistance());
		
		System.out.println("Saving core-distances to file: " + args[0] + ".cd");
		coreDistancesToFile(coreDistances, args[0] + ".cd");
		
		System.out.println("Saving kNN to file: " + args[0] + ".knn");
		kNNToFile(kNN, args[0] + ".knn");
		
		System.out.println("---------------------------------------");
	}

	/**
	 * Calculates the core distances for each point in the data set, given some value for k.
	 * @param dataSet A double[][] where index [i][j] indicates the jth attribute of data point i
	 * @param k Each point's core distance will be it's distance to the kth nearest neighbor
	 * @param distanceFunction A DistanceCalculator to compute distances between points
	 * @return An array of core distances
	 */
	public static double[][] calculateCoreDistances(double[][] dataSet, int k, DistanceCalculator distanceFunction) {
		int numNeighbors = k;
		double[][] coreDistances = new double[dataSet.length][numNeighbors];
		int[][] kNN = new int[dataSet.length][numNeighbors];

		IncrementalHDBSCANStar.k = k;

		if (k == 1) {

			for (int point = 0; point < dataSet.length; point++) {
				coreDistances[point][0] = 0;
			}

			return coreDistances;
		}

		for (int point = 0; point < dataSet.length; point++) {
			double[] kNNDistances = new double[numNeighbors];	//Sorted nearest distances found so far

			for (int i = 0; i < numNeighbors; i++) {
				kNNDistances[i] = Double.MAX_VALUE;
				kNN[point][i] = Integer.MAX_VALUE;
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
						kNN[point][shiftIndex] = kNN[point][shiftIndex-1];
					}
					kNNDistances[neighborIndex] = distance;
					kNN[point][neighborIndex] = neighbor;
				}
			}

			coreDistances[point] = kNNDistances;
		}

		CoreDistances.kNN = kNN;
		CoreDistances.coreDistances = coreDistances;

		return coreDistances;
	}

	public static void coreDistancesToFile(double[][] array, String file) {

		Path path = Paths.get(file);

		try {
			BufferedWriter writer = Files.newBufferedWriter(path);

			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[i].length; j++) {
					writer.write(array[i][j] + " ");
				}
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void kNNToFile(int[][] array, String file) {

		Path path = Paths.get(file);

		try {
			BufferedWriter writer = Files.newBufferedWriter(path);

			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[i].length; j++) {
					writer.write(array[i][j] + " ");
				}
				writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * @param coreDistancesFile
	 * @param minPoints
	 * @param delimiter
	 * @return
	 * @throws IOException
	 */
	public static double[][] fromFile(String coreDistancesFile, int minPoints, String delimiter) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(coreDistancesFile));

		ArrayList<double[]> dataSet = new ArrayList<double[]>();

		int numAttributes = -1;
		int lineIndex = 0;
		String line = reader.readLine();

		while (line != null) {
			lineIndex++;
			String[] lineContents = line.split(delimiter);

			if (numAttributes == -1)
				numAttributes = Math.min(lineContents.length, minPoints);
			else if (Math.min(lineContents.length, minPoints) != numAttributes)
				System.err.println("Line " + lineIndex + " of data set has incorrect number of attributes.");

			double[] attributes = new double[numAttributes];
			for (int i = 0; i < numAttributes; i++) {
				try {
					//If an exception occurs, the attribute will remain 0:
					attributes[i] = (double) Double.parseDouble(lineContents[i]);
				}
				catch (NumberFormatException nfe) {
					System.err.println("Illegal value on line " + lineIndex + " of data set: " + lineContents[i]);
				}
			}

			dataSet.add(attributes);
			line = reader.readLine();
		}

		reader.close();
		double[][] coreDistances = new double[dataSet.size()][numAttributes];

		for (int i = 0; i < dataSet.size(); i++) {
			coreDistances[i] = dataSet.get(i);
		}

		return coreDistances;
	}
	
	/**
	 * @param coreDistancesFile
	 * @param minPoints
	 * @param delimiter
	 * @return
	 * @throws IOException
	 */
	public static int[][] knnFromFile(String coreDistancesFile, int minPoints, String delimiter) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(coreDistancesFile));

		ArrayList<int[]> tmp = new ArrayList<int[]>();

		int numAttributes = -1;
		int lineIndex = 0;
		String line = reader.readLine();

		while (line != null) {
			lineIndex++;
			String[] lineContents = line.split(delimiter);

			if (numAttributes == -1)
				numAttributes = Math.min(lineContents.length, minPoints);
			else if (Math.min(lineContents.length, minPoints) != numAttributes)
				System.err.println("Line " + lineIndex + " of data set has incorrect number of attributes.");

			int[] attributes = new int[numAttributes];
			for (int i = 0; i < numAttributes; i++) {
				try {
					//If an exception occurs, the attribute will remain 0:
					attributes[i] = (int) Integer.parseInt(lineContents[i]);
				}
				catch (NumberFormatException nfe) {
					System.err.println("Illegal value on line " + lineIndex + " of data set: " + lineContents[i]);
				}
			}

			tmp.add(attributes);
			line = reader.readLine();
		}

		reader.close();
		int[][] knn = new int[tmp.size()][numAttributes];

		for (int i = 0; i < tmp.size(); i++) {
			knn[i] = tmp.get(i);
		}

		return knn;
	}
}
