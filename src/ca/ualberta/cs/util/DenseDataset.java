package ca.ualberta.cs.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import ca.ualberta.cs.distance.DistanceCalculator;

public class DenseDataset implements Dataset {

	private double[][] data;
	
	public DistanceCalculator distanceFunction;
	
	public DenseDataset(String fileName, String delimiter, DistanceCalculator distanceFunction) throws IOException {
		this.distanceFunction = distanceFunction;	
		this.readInDataSet(fileName, delimiter);
	}
	
	@Override
	public void readInDataSet(String fileName, String delimiter) throws IOException {

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
					attributes[i] = Double.parseDouble(lineContents[i]);
				}
				catch (NumberFormatException nfe) {
					System.err.println("Illegal value on line " + lineIndex + " of data set: " + lineContents[i]);
				}
			}

			dataSet.add(attributes);
			line = reader.readLine();
		}

		reader.close();
		data = new double[dataSet.size()][numAttributes];

		for (int i = 0; i < dataSet.size(); i++) {
			data[i] = dataSet.get(i);
		}
	}

	@Override
	public double get(int i, int j) {
		return data[i][j];
	}

	@Override
	public void set(int i, int j, double d) {
		data[i][j] = d;
	}
	
	@Override
	public int dimensions() {
		return data[0].length;
	}

	@Override
	public int length() {
		return data.length;
	}

	@Override
	public double[] row(int i) {
		return data[i];
	}
	
	@Override
	public double computeDistance(int i, int j) {		
		return this.distanceFunction.computeDistance(data[i], data[j]);
	}

}
