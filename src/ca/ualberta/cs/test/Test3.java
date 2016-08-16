package ca.ualberta.cs.test;

import java.io.IOException;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;

public class Test3 {
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		long start, end, duration;
		
		String dataFile = "/home/toni/git/HDBSCAN_Star/experiments/data#3/2d-4c-no0.dat";
		
		Double[][] dataSet = null;

		try {
			dataSet = HDBSCANStar.readInDataSet(dataFile, " ");
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}
		
		String inputFile = dataFile.split("/")[dataFile.split("/").length - 1];		
		
		int minPoints;		
		
		minPoints = 10;
		
		System.out.println("Computing core-distances...");
		start = System.currentTimeMillis();
		
//		double[][] coreDistances2 = IncrementalHDBSCANStar.calculateCoreDistances(dataSet, minPoints, new EuclideanDistance());
		HDBSCANStar.calculateCoreDistances(dataSet, minPoints, new EuclideanDistance());
		
		end = System.currentTimeMillis();
		duration = end - start;
		
		System.out.println(inputFile + " " + minPoints + " " + duration);

		
	
	}
}
