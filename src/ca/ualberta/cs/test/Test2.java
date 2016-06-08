package ca.ualberta.cs.test;

import java.io.IOException;

import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;
import ca.ualberta.cs.util.Combinations;
import ca.ualberta.cs.util.Data;

public class Test2{

	public static void main(String[] args) {
		Double[][] dataSet = null;

		try {
			dataSet = HDBSCANStar.readInDataSet("x.dat", ",");
			dataSet = Data.normalize(dataSet);
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}
	    

		Double[][] result = Combinations.permutation(RelativeNeighborhoodGraph.S(dataSet[0].length), dataSet[0].length);
				
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].length; j++) {
				System.out.print(result[i][j] + " ");
			}
			System.out.println();
		}
	}
	
}