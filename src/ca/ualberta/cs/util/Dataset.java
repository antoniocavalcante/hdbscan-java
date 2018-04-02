package ca.ualberta.cs.util;

import java.io.IOException;

public interface Dataset {
		
	public void readInDataSet(String fileName, String delimiter) throws IOException;

	public double get(int i, int j);

	public void set(int i, int j, double d);	
	
	public int dimensions();

	public int length();
	
	public double[] row(int i);

	public double computeDistance(int i, int j);
}
