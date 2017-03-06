package ca.ualberta.cs.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FormatFiles {

	public static void main(String[] args) throws IOException {
//		String fileName = "fcolormoments.txt";
		String fileName = "data_ids.txt";
//		String destFile = "data_ids.txt";

		String file2 = "/home/toni/Dropbox/UofA/Research/ALOI/aloi.txt";
		
		FileWriter fw = new FileWriter(file2, true);
		BufferedWriter bw = new BufferedWriter(fw);
		
		for (int k = 2; k <= 5; k++) {
			for (int s = 1; s <= 100; s++) {

				String file = "/home/toni/Dropbox/UofA/Research/ALOI/subset_k" + 
						k + "_" + String.format("%03d", s) + "/";
				
				System.out.println(file + fileName);
				
				try {
//					double[][] data = readInDataSet(file + fileName, " ");
//
//					String[] ids = readIds(file + "ids.txt", " ");

					String[] data = readInStrings(file + fileName);
					
					for (int i = 0; i < data.length; i++) {
						bw.write(data[i] + "\n");
					}
										
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		bw.close();
	}

	public static double[][] readInDataSet(String fileName, String delimiter) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		ArrayList<double[]> dataSet = new ArrayList<double[]>();
		int numAttributes = -1;
		int lineIndex = 0;
		String line = reader.readLine();

		while (line != null) {
			lineIndex++;
			String[] lineContents = line.split(delimiter);

			if (numAttributes == -1)
				numAttributes = lineContents.length;
			else if (lineContents.length != numAttributes)
				System.err.println("Line " + lineIndex + " of data set has incorrect number of attributes.");

			double[] attributes = new double[numAttributes];
			for (int i = 0; i < numAttributes; i++) {
				try {
					//If an exception occurs, the attribute will remain 0:
					attributes[i] = (double) Double.valueOf(lineContents[i].trim());
				}
				catch (NumberFormatException nfe) {
					System.err.println("Illegal value on line " + lineIndex + " of data set: " + lineContents[i]);
				}
			}

			dataSet.add(attributes);
			line = reader.readLine();
		}

		reader.close();
		double[][] finalDataSet = new double[dataSet.size()][numAttributes];

		for (int i = 0; i < dataSet.size(); i++) {
			finalDataSet[i] = dataSet.get(i);
		}

		return finalDataSet;
	}
	
	public static String[] readInStrings(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		ArrayList<String> dataSet = new ArrayList<String>();

		String line = reader.readLine();

		while (line != null) {
			dataSet.add(line);
			line = reader.readLine();
		}

		reader.close();
		
		String[] finalDataSet = new String[dataSet.size()];

		for (int i = 0; i < dataSet.size(); i++) {
			finalDataSet[i] = dataSet.get(i);
		}

		return finalDataSet;
	}

	
	@SuppressWarnings("resource")
	public static String[] readIds(String fileName, String delimiter) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));

		String line = reader.readLine();

		String[] lineContents = line.split(delimiter);
		
		return lineContents;
	}

	
}
