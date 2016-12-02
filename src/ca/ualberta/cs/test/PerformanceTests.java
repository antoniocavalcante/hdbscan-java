package ca.ualberta.cs.test;

import java.io.IOException;

import ca.ualberta.cs.hdbscanstar.HDBSCANStar;

public class PerformanceTests{

	public static void main(String[] args) {
		double[][] dataSet = null;

		String file = "/home/toni/git/HDBSCAN_Star/experiments/data#6/4d-1024.dat";
		
		try {
			dataSet = HDBSCANStar.readInDataSet(file, " ");
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}


	}
	

}