package ca.ualberta.cs.test;

import java.io.IOException;

import ca.ualberta.cs.hdbscanstar.HDBSCANStar;

public class Test2{

	public static void main(String[] args) {
		Double[][] dataSet = null;

		try {
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/experiments/debug/jad.dat", " ");
			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/experiments/debug/2p.dat", " ");
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
//		IncrementalHDBSCANStar.calculateCoreDistances(data, 1, new EuclideanDistance());
//		
//		FairSplitTree T = FairSplitTree.build(data);
//		FairSplitTree.print(T);
//		System.out.println(T.getLeft().getMaxCD());
//		System.out.println(T.getRight().getMaxCD());
//		
//		RelativeNeighborhoodGraph.rn(T.getLeft(), T.getRight());

		double a = -Double.MIN_VALUE;
		double b = 0.0;
		
		System.out.println(Math.max(a, b));
	}
}