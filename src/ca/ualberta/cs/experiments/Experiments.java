package ca.ualberta.cs.experiments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import ca.ualberta.cs.hdbscanstar.UndirectedGraph;

public class Experiments {
	
	public static void writeMSTweight(String file, int minPoints, UndirectedGraph MST) {
		
		String f = file + "-" + minPoints + ".w";
		
		double weight = 0;
		for (int i = 0; i < MST.getNumEdges(); i++) {
			weight = weight + MST.getEdgeWeightAtIndex(i);
		}
		
		try {
			Files.write(Paths.get(f), (Double.toString(weight) + "\n").getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
