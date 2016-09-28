package ca.ualberta.cs.experiments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import ca.ualberta.cs.util.HAI;

public class RunHAIonALOI {

	static final String PCA  = "aloiPCA_k";
	static final String TS88 = "";
	static final String suffix = ".data.hierarchy";

	static final String RNG = "RNG";
	static final String MST = "MST";
	static final String ORI = "ORI";
	
	static final String baseDir = "/home/toni/Dropbox/UofA/Research/Results/ALOI/output-data#4/tmp/";
	
	static final String outputDir = "/home/toni/Dropbox/UofA/Research/Results/ALOI/output-data#4/";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String setNumber = "067";
		
		for (int k = 2; k <= 5 ; k++) {
			run(RNG, PCA, k, setNumber);
			run(MST, PCA, k, setNumber);
			run(ORI, PCA, k, setNumber);
		}
	}

//	public static void run(String method, String set, int k, String setNumber) {
//		
//		int minPoints = Math.min(25*k, 100) - 1;
//
//		String h1 = baseDir + 1 + method + "_" + set + k + "_" + setNumber + suffix;
//		
//		for (int i = 2; i < minPoints; i++) {
//			String h2 = baseDir + i + method + "_" + set + k + "_" + setNumber + suffix;
//			
//			double hai = HAI.evaluate(h1, h2);
//						
//			try {
//				Files.write(Paths.get(outputDir + k + "_" + method + ".HAI"), (Integer.toString(i) + " " + Double.toString(hai) + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			h1 = h2;
//		}
//
//	}

	public static void run(String method, String set, int k, String setNumber) {
		
		int minPoints = Math.min(25*k, 100) - 1;

		String h1 = baseDir + 1 + method + "_" + set + k + "_" + setNumber + suffix;
		
		for (int i = 2; i < 49; i++) {
			String h2 = baseDir + i + method + "_" + set + k + "_" + setNumber + suffix;
			
			double hai = HAI.evaluate(h1, h2);
						
			try {
				Files.write(Paths.get(outputDir + k + "_" + method + ".HAI"), (Integer.toString(i) + " " + Double.toString(hai) + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			h1 = h2;
		}

	}	
}
