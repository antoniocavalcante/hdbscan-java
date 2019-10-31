/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ualberta.cs.SHM.HMatrix;

import ca.ualberta.cs.distance.CosineSimilarity;
import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.distance.ManhattanDistance;
import ca.ualberta.cs.distance.PearsonCorrelation;
import ca.ualberta.cs.distance.SupremumDistance;
import it.unimi.dsi.fastutil.doubles.DoubleBigArrayBigList;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;
import it.unimi.dsi.fastutil.ints.IntOpenHashBigSet;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author fsan
 */
public class HMatrix implements Serializable {
	protected DoubleBigArrayBigList densities;
	public ObjInstance[] matrix;
	protected int maxClusterID;
	protected int maxLabelValue;    		// Holds the maximum label value used it starts as -1 (unlabelled).
	protected Color[] colors;
	public int[] objectOrderbyID;    	// HM <ID, array position>, maps an ID to its position on the ArrayList.
	protected boolean isShaved;             // Flag to tell if this file is a complete or a shaved hierarchy.

	private boolean hasReachabilities;		// Says if the reachability distances were set before saving.
	private boolean infiniteStability;		// Given from HDBSCAN*.

	//Parameters used in HDBSCAN*
	private String param_inputFile;
	private String param_distanceFunction;
	private int param_minClSize;
	private int param_minPts;
	private boolean param_distanceMatrixUsed;
	private boolean isCompact;

	private int next = 0;

	private IntBigArrayBigList level;
	
	private static final long serialVersionUID = 10L;

	public HMatrix() {
		this.densities = new DoubleBigArrayBigList();
		this.maxClusterID = 0;
		this.maxLabelValue = -1;
		this.isCompact = false;
		this.hasReachabilities = false;
		this.infiniteStability = false;

		this.param_minClSize = 1;
		this.param_minPts = 1;
		this.param_distanceFunction = "euclidean";
		this.param_distanceMatrixUsed  = false;

		this.level = new IntBigArrayBigList();
	}

	public void setHMatrix(int n) {
		this.matrix = new ObjInstance[n];
		this.objectOrderbyID = new int[n];
		
		this.level.add(0);
		this.level.add(n-1);
	}

	public void setVisParams(String inputFile, int minClSize, int minPts, String distanceFunction, boolean distanceMatrixUsed, boolean isCompact) {
		this.setParam_inputFile(inputFile);
		this.setParam_minClSize(minClSize);
		this.setParam_minPts(minPts);
		this.setParam_distanceFunction(distanceFunction);
		this.setParam_distanceMatrixUsed(distanceMatrixUsed);
		this.setIsCompact(isCompact);
	}

	public void setParam_inputFile(String inputFile) {
		this.param_inputFile = inputFile;
	}

	public String getParam_inputFile() {
		return this.param_inputFile;
	}

	public void setIsCompact(boolean isCompact) {
		this.isCompact = isCompact;
	}

	public boolean getIsCompact() {
		return this.isCompact;
	}

	public void setInfiniteStability(boolean infiniteStability) {
		this.infiniteStability = infiniteStability;
	}

	public boolean getInfiniteStability() {
		return this.infiniteStability;
	}

	public void setHasReachabilities(boolean hasReachabilities) {
		this.hasReachabilities = hasReachabilities;
	}

	public boolean hasReachabilities() {
		return this.hasReachabilities;
	}

	public int getPositionByID(int ID) {
		return this.objectOrderbyID[ID];
	}

	public int getIDByPosition(int position) {
		return this.matrix[position].getID();
	}
	
	public void setIsShaved(boolean shaved) {
		this.isShaved = shaved;
	}

	public boolean getIsShaved() {   
		return this.isShaved;
	}

	public DoubleBigArrayBigList getDensities() {
		return this.densities;
	}

	public ObjInstance[] getMatrix() {
		return this.matrix;
	}

	public int getMaxClusterID() {
		return this.maxClusterID;
	}

	public int getMaxLabelValue() {
		return this.maxLabelValue;
	}

	// This method increases the MaxLabel to the value passed as parameter, only if its greater than the old value.
	public void increaseMaxLabelValue(int newValue) {
		this.maxLabelValue = Math.max(newValue, this.maxLabelValue);
	}

	// This method forces the maximum value to be the one passed as parameter.
	public void setMaxLabelValue(int maxLabelValue) {
		this.maxLabelValue = maxLabelValue;
	}

	public void setColor(Color[] colors) {
		this.colors = colors;
	}

	public Color[] getColors() {
		return this.colors;
	}

	public void setMaxClusterID(int maxClusterID) {
		this.maxClusterID = maxClusterID;
	}

	//Notice that it does not use the ID, but the index (order) used on the arrayList.
	//Also it expects the actual density, not an index.
	//it returns ObjInstance.NOISE when no key was found.
	public int getClusterID(int i, double density) {
		return this.matrix[i].getClusterID(density);        
	}

	// Similar to the one above, the only difference is that it automatically gets the j-th density (greatest to lowest order)
	// Notice that this method does not assert the existence of any of the indexes (i,j) used. This must be handled by the developer.
	public int getClusterID(int i, int j) {
		return this.matrix[i].getClusterID(this.densities.getDouble(j));
	}

	public void add(ObjInstance newOI) {
		this.matrix[next] = newOI;
		this.objectOrderbyID[newOI.getID()] = next;
		this.next++;
	}

	public ObjInstance getObjInstanceByID(int id) {
		return this.matrix[this.objectOrderbyID[id]];
	}

	public ObjInstance getObjInstance(int i) {
		return this.matrix[i];
	}

	public Double getDensity(long i)	{
		return this.densities.getDouble(i);
	}

	public void lexicographicSort()	{

		List<ObjInstance> aux = Arrays.asList(this.matrix);

		Collections.sort(aux, new Comparator<ObjInstance>() {
			@Override
			public int compare(ObjInstance o1, ObjInstance o2) {

				for (long i = densities.size64() - 1; i >= 0 ; i--) {
					double k = densities.getDouble(i);

					int l1 = o1.getClusterID(k);
					int l2 = o2.getClusterID(k);

					if (l1 < l2) return -1;
					if (l1 > l2) return 1;						
				}

				return 0;
			}
		});

		// Converts the list back to a matrix.
		this.matrix = aux.toArray(this.matrix);

		// Taking note of the new position of the object, this way you can find it by ID directly.
		for(int i = 0; i < this.matrix.length; i++) {
			this.objectOrderbyID[this.matrix[i].getID()] = i;
		}
	}

	public void lexicographicSortIgnoreNoise()	{

		List<ObjInstance> aux = Arrays.asList(this.matrix);

		Collections.sort(aux, new Comparator<ObjInstance>() {
			@Override
			public int compare(ObjInstance o1, ObjInstance o2) {

				for (int i = 0; i <= densities.size64() - 1; i++) {
					double k = densities.getDouble(i);

					int l1 = o1.getClusterID(k);
					int l2 = o2.getClusterID(k);

					if (l1 < l2) return -1;
					if (l1 > l2) return 1;						
				}

				return 0;
			}
		});

		// Converts the list back to a matrix.
		this.matrix = aux.toArray(this.matrix);

		// Taking note of the new position of the object, this way you can find it by ID directly.
		for(int i = 0; i < this.matrix.length; i++) {
			this.objectOrderbyID[this.matrix[i].getID()] = i;
		}
	}

	public void lexicographicSortIgnoreNoiseDynamic(int[] labels, double d, BufferedWriter hierarchyWriter)	{
		
		if (d == 0) return;
		
		for (int i = 0; i < level.size64() - 1; i = i + 2) {

			int s = level.getInt(i);
			int e = level.getInt(i+1);

			Arrays.sort(this.matrix, s, e+1, new Comparator<ObjInstance>() {
				@Override
				public int compare(ObjInstance o1, ObjInstance o2) {

					int l1 = labels[o1.getID()];
					int l2 = labels[o2.getID()];

//					if (l1 == 0 || l2 == 0) {
//						if (o1.getID() < o2.getID()) return -1;
//						if (o1.getID() > o2.getID()) return  1;
//					}
					
					if (l1 < l2) return -1;
					if (l1 > l2) return  1;		
					
					if (o1.getID() < o2.getID()) return -1;
					if (o1.getID() > o2.getID()) return  1;

					return 0;
				}
			});

		}

		try {
			this.level = this.toFileDynamic(labels, d, hierarchyWriter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeOrder(BufferedWriter orderWriter) throws IOException {
		String delimiter = ",";
		
		// Taking note of the new position of the object, this way you can find it by ID directly.
		for(int i = 0; i < this.matrix.length; i++) {
			this.objectOrderbyID[this.matrix[i].getID()] = i;
		}
		
		// Write Order to File
		for (int i = 0; i < this.objectOrderbyID.length - 1; i++) {
			orderWriter.write(this.matrix[i].getID() + delimiter);
		}
		orderWriter.write(this.matrix[this.objectOrderbyID.length - 1].getID() + "\n");
	}
	
	public IntBigArrayBigList toFileDynamic(int[] labels, double d, BufferedWriter hierarchyWriter) throws IOException {

		IntBigArrayBigList level = new IntBigArrayBigList();
		
		String delimiter = ",";

		StringBuilder builder = new StringBuilder();

		builder.append(d);

		for (int j = 0; j < this.level.size64(); j += 2) {

			int s = this.level.getInt(j);
			int e = this.level.getInt(j+1);
			
			// Case where the cluster hasn't changed from previous level OR it's the first level.
//			if (labels[getIDByPosition(s)] == labels[getIDByPosition(e)]) {
//				// Checks if this is the root of the hierarchy.
//				if (labels[getIDByPosition(s)] != 1) {
//					level.add(s);
//					level.add(e);
//					continue;
//				}
//			}

			int last = -1;

			while (s <= e) {
				
				if (labels[getIDByPosition(s)] != 0) {
					last = last(labels, s, e, labels[getIDByPosition(s)], labels.length);
					
					// Add to the string builder and to the current level.
					builder.append(delimiter + labels[getIDByPosition(s)] + ":" + s + "-" + last);

					level.add(s);
					level.add(last);
					
					s = last + 1;
				} else {
					s = s + 1;
				}
			}
		}
		
		String output = builder.toString();
		if (output != "") {
			hierarchyWriter.write(output + "\n");
		}

		return level;
	}

    
	public int last(int arr[], int low, int high, int x, int n) {

		if (high >= low) {
            
			int mid = low + (high - low)/2;
			
	        if (mid == high)
	        	return mid;	        
			if (( mid == n-1 || x < arr[getIDByPosition(mid+1)] || arr[getIDByPosition(mid+1)] == 0) && arr[getIDByPosition(mid)] == x)
                 return mid;
            else if (x < arr[getIDByPosition(mid)] || arr[getIDByPosition(mid)] == 0)
                return last(arr, low, (mid - 1), x, n);
            else
                return last(arr, (mid + 1), high, x, n);
        }
        
        return -1;
    }
	
	
	// This method is called to clear both matrix and densities data structures.
	public void clearAll() {
		this.matrix = null;
		this.densities.clear();
	}

	public void toFile(BufferedWriter hierarchyWriter) throws IOException {
		String delimiter = ",";

		// Write Order to File
		for (int i = 0; i < this.objectOrderbyID.length - 1; i++) {
			hierarchyWriter.write(this.matrix[i].getID() + delimiter);
		}
		hierarchyWriter.write(this.matrix[this.objectOrderbyID.length - 1].getID() + "\n");

		IntOpenHashBigSet writtenClusters = new IntOpenHashBigSet();

		writtenClusters.add(0);

		// Write Hierarchy to File
		for (double d : this.getDensities()) {

			StringBuilder builder = new StringBuilder();

			if (d == 0) {
				break;
			}

			int i = 0;
			int cluster = this.getClusterID(i, d);

			while (writtenClusters.contains(cluster) && i < this.objectOrderbyID.length - 1) {
				i++;
				cluster = this.getClusterID(i, d);
			}

			if (!writtenClusters.contains(cluster)) {
				builder.append(d + delimiter + cluster + ":" + i + "-");
			}

			for (int p = i; p < this.objectOrderbyID.length; p++) {

				int currentCluster = this.getClusterID(p, d);

				if (currentCluster != cluster) {

					// Noise || Ignored Cluster -> Noise || Ignored Cluster
					if (writtenClusters.contains(cluster) && writtenClusters.contains(currentCluster)) {
						cluster = currentCluster;
						continue;
					}

					// Cluster -> Noise || Ignored Cluster.
					if (cluster != 0 && (currentCluster == 0 || writtenClusters.contains(currentCluster))) {
						builder.append(p - 1);

						//						writtenClusters.add(cluster);

						cluster = currentCluster;

					}

					// Cluster -> Cluster.
					if (cluster != 0 && currentCluster != 0 && !writtenClusters.contains(currentCluster)) {
						builder.append(p - 1);

						//						writtenClusters.add(cluster);

						cluster = currentCluster;

						builder.append(delimiter + cluster + ":" + p + "-");
					}

					// Noise || Ignored Cluster -> Cluster.
					if (cluster == 0 && currentCluster != 0 && !writtenClusters.contains(currentCluster)) {
						cluster = currentCluster;

						builder.append(delimiter + cluster + ":" + p + "-");
					}

				}

				if (p == this.objectOrderbyID.length - 1 && !writtenClusters.contains(currentCluster)) {			
					builder.append(Integer.toString(p));

					//					writtenClusters.add(cluster);
				}
			}
			String output = builder.toString();
			if (output != "") {
				hierarchyWriter.write(output + "\n");
			}	
		}
	}

	public void print() {
		System.out.println("Densities");
		System.out.println(this.densities.toString());

		System.out.println();
		System.out.println("H Matrix");
		for(int i = 0 ; i < this.matrix.length ; i++ ) {
			System.out.println(this.matrix[i].toString());
		}
	}

	/**
	 * @return the param_minClSize
	 */
	public int getParam_minClSize() {
		return param_minClSize;
	}

	/**
	 * @param param_minClSize the param_minClSize to set
	 */
	public void setParam_minClSize(int param_minClSize) {
		this.param_minClSize = param_minClSize;
	}

	/**
	 * @return the param_minPts
	 */
	public int getParam_minPts() {
		return param_minPts;
	}

	/**
	 * @param param_minPts the param_minPts to set
	 */
	public void setParam_minPts(int param_minPts) {
		this.param_minPts = param_minPts;
	}

	/**
	 * @return the param_distanceFunction
	 */
	public DistanceCalculator getParam_distanceFunction() {
		final String EUCLIDEAN_DISTANCE 	= "euclidean";
		final String COSINE_SIMILARITY 		= "cosine";
		final String PEARSON_CORRELATION 	= "pearson";
		final String MANHATTAN_DISTANCE 	= "manhattan";
		final String SUPREMUM_DISTANCE 		= "supremum";

		if (this.param_distanceFunction.equals(EUCLIDEAN_DISTANCE)) {
			return new EuclideanDistance();
		} else if (this.param_distanceFunction.equals(COSINE_SIMILARITY)) {
			return new CosineSimilarity();
		} else if (this.param_distanceFunction.equals(PEARSON_CORRELATION)) {
			return new PearsonCorrelation();
		} else if (this.param_distanceFunction.equals(MANHATTAN_DISTANCE)) {
			return new ManhattanDistance();
		} else if (this.param_distanceFunction.equals(SUPREMUM_DISTANCE)) {
			return new SupremumDistance();
		} else {
			return null;
		}
	}

	/**
	 * @param param_distanceFunction the param_distanceFunction to set
	 */
	public void setParam_distanceFunction(String param_distanceFunction) {
		this.param_distanceFunction = param_distanceFunction;
	}

	/**
	 * @return the param_distanceMatrixUsed
	 */
	public boolean isParam_distanceMatrixUsed() {
		return param_distanceMatrixUsed;
	}

	/**
	 * @param param_distanceMatrixUsed the param_distanceMatrixUsed to set
	 */
	public void setParam_distanceMatrixUsed(boolean param_distanceMatrixUsed) {
		this.param_distanceMatrixUsed = param_distanceMatrixUsed;
	}

}
