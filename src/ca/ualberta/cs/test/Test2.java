package ca.ualberta.cs.test;

import java.io.IOException;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.util.FairSplitTree;
import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;

public class Test2{

	public static void main(String[] args) {
		Double[][] dataSet = null;

		try {
			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/experiments/debug/jad.dat", " ");
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}

		int numPoints = dataSet.length;
		System.out.println("Dataset size: " + numPoints);
		System.out.println("Dimensions: " + dataSet[0].length);
		
		long start = System.currentTimeMillis();
		
		wspd(dataSet, 2);
		
		System.out.println("Time: " + (System.currentTimeMillis() - start));
	}
	
	public static void wspd(Double[][] data, double s){
		IncrementalHDBSCANStar.calculateCoreDistances(data, 4, new EuclideanDistance());
		
		FairSplitTree T = FairSplitTree.build(data);
		
//		Double[] q = {7.5, 1.5};
		Double[] q = {8.0, 4.0};

		
		BigList<Integer> results = FairSplitTree.rangeSearch(T, q, 1, new IntBigArrayBigList());
		
		System.out.println(results);
		
		if (results.isEmpty()) {
			System.out.println("AAAAAAAAA");
		}
	}
}