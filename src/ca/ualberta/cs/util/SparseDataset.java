package ca.ualberta.cs.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import ca.ualberta.cs.distance.DistanceCalculator;
import it.unimi.dsi.fastutil.ints.Int2DoubleLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;

public class SparseDataset implements Dataset {

	public SparseMatrix sparseMatrix;
	public int nnz = 0;
	
	public DistanceCalculator distanceFunction;
	
	public SparseDataset(String fileName, String delimiter, DistanceCalculator distanceFunction) throws IOException {
		this.distanceFunction = distanceFunction;
		this.readInDataSet(fileName, delimiter);
	}
	
	@Override
	public void readInDataSet(String fileName, String delimiter) throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
	
		int n = Integer.parseInt(reader.readLine());
		int numAttributes = Integer.parseInt(reader.readLine());
		
		this.nnz = Integer.parseInt(reader.readLine());
		
		// Initializes the sparse matrix.		
		this.sparseMatrix = new SparseMatrix(n, numAttributes);		
		
		String line = reader.readLine();
				
		while (line != null) {

			String[] lineContents = line.split(delimiter);
			
			this.sparseMatrix.set(Integer.parseInt(lineContents[0]) - 1, Integer.parseInt(lineContents[1]), Integer.parseInt(lineContents[2]));
			
			line = reader.readLine();
		}

		reader.close();
	}

	@Override
	public double get(int i, int j) {
		return sparseMatrix.get(i, j);
	}

	@Override
	public void set(int i, int j, double d) {
		sparseMatrix.set(i, j, d);
	}

	@Override
	public int dimensions() {
		return sparseMatrix.dimensions;
	}

	@Override
	public int length() {
		return sparseMatrix.length;
	}

	@Override
	public double[] row(int i) {
		double[] result = new double[this.dimensions()];
		
		for (int j = 0; j < result.length; j++) {
			if (sparseMatrix.m[i].containsKey(j)) {
				result[j] = sparseMatrix.m[i].get(j);
			} else {
				result[j] = 0;
			}
		}

		return result;
	}

	public double[][] rows(int p, int q) {		
		
//		IntAVLTreeSet columns = new IntAVLTreeSet(sparseMatrix.m[p].keySet());
		IntLinkedOpenHashSet columns = new IntLinkedOpenHashSet(sparseMatrix.m[p].keySet());
		columns.addAll(sparseMatrix.m[q].keySet());
		
		int[] cols = columns.toIntArray();
		
		columns = null;
		
		double[][] result = new double[2][cols.length];
				
		for (int j = 0; j < cols.length; j++) {
				
			if (sparseMatrix.m[p].containsKey(cols[j])) {			
				result[0][j] = sparseMatrix.m[p].get(cols[j]);
			} else {
				result[0][j] = 0;
			}

			if (sparseMatrix.m[q].containsKey(cols[j])) {
				result[1][j] = sparseMatrix.m[q].get(cols[j]);
			} else {
				result[1][j] = 0;
			}

		}

		return result;
	}

	@Override
	public double computeDistance(int i, int j) {
		double[][] rows = this.rows(i, j);
		
		return this.distanceFunction.computeDistance(rows[0], rows[1]);
	}
	
	public class SparseMatrix {
		
		public Int2DoubleLinkedOpenHashMap[] m;
		
		public int dimensions;
		public int length;
		
		public SparseMatrix(int row, int col) {
			m = new Int2DoubleLinkedOpenHashMap[row];
			
			for (int i = 0; i < m.length; i++) {
				m[i] = new Int2DoubleLinkedOpenHashMap();
			}
			
			this.length = row;
			this.dimensions = col;
		}
		
		public void set(int i, int j, double value) {
			m[i].put(j, value);			
		}
		
		public double get(int i, int j) {
			return m[i].get(j);
		}
	}
	
}
