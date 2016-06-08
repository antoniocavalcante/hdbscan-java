package ca.ualberta.cs.hdbscanstar;

import java.util.BitSet;
import java.util.Comparator;

public class Graph {

	public int numOfEdgesMRG;
	
	public int[] edgesA;
	public int[] edgesB;
	public double[] weights;
	
	public Integer[] sortedEdges;
	
	public BitSet inMST;

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
}
