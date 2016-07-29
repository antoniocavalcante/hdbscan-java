package ca.ualberta.cs.test;

import java.io.IOException;
import java.util.ArrayList;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import ca.ualberta.cs.util.FairSplitTree;
import ca.ualberta.cs.util.WSPD;

public class Test2{

	public static void main(String[] args) {
		Double[][] dataSet = null;

		try {
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/experiments/data#1/2d-2c-no0.dat", " ");
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/experiments/data#2/3d-32c-no0.dat", " ");
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/test.dat", " ");
			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/jad.dat", ",");
//			dataSet = HDBSCANStar.readInDataSet("/home/toni/git/HDBSCAN_Star/j.dat", ",");

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

//		FairSplitTree.print(T);
		
//		Double[] q = {7.5, 1.5};
		Double[] q = {3.0, 3.0};

		
		ArrayList<Integer> results = FairSplitTree.rangeSearch(T, q, 2.24, new ArrayList<>());
		
		System.out.println(results);
		
//		WSPD.build(T, T, s, WSPD.WS);
		
//		for (SeparatedPair pair : WSPD.pairs) {
//			FairSplitTree T1 = pair.T1;
//			FairSplitTree T2 = pair.T2;
//			System.out.println(T1.P + ", " + T2.P);
//			System.out.println(T1.id + ", " + T2.id);
//		}
		
//		for (int i = 0; i < data.length; i++) {
//			int count = 0;
//			for (SeparatedPair pair : WSPD.pairs) {
//				
//				FairSplitTree T1 = pair.T1;
//				FairSplitTree T2 = pair.T2;
//				
//				if (T1.P.contains(i)) {
//					count = count + T2.P.size();
//				} else 
//					if (T2.P.contains(i)) {
//					count = count + T1.P.size();
//				}
//			}
////			System.out.println("Pairs containing " + i + ": " + count);
//			if (count < data.length - 1) {
//				System.out.println("ERROR!");
//				break;
//			}
//		}
		
		System.out.println("Number of WSP: " + WSPD.pairs.size());
	}
}