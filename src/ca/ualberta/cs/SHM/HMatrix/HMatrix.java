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

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author fsan
 */
public class HMatrix implements Serializable {
	protected ArrayList<Double> densities;
	protected ObjInstance[] matrix;
	protected int maxClusterID;
	protected int maxLabelValue;    		// Holds the maximum label value used it starts as -1 (unlabelled).
	protected Color[] colors;
	protected int[] objectOrderbyID;    	// HM <ID, array position>, maps an ID to its position on the ArrayList.
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

	private static final long serialVersionUID = 10L;

	public HMatrix() {
		this.densities = new ArrayList<Double>();
		this.maxClusterID = 0;
		this.maxLabelValue = -1;
		this.isCompact = false;
		this.hasReachabilities = false;
		this.infiniteStability = false;

		this.param_minClSize = 1;
		this.param_minPts = 1;
		this.param_distanceFunction = "euclidean";
		this.param_distanceMatrixUsed  = false;
	}
	
	public void setHMatrix(int n) {
		this.matrix = new ObjInstance[n];
		this.objectOrderbyID = new int[n];
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

	public void setIsShaved(boolean shaved) {
		this.isShaved = shaved;
	}

	public boolean getIsShaved() {   
		return this.isShaved;
	}

	public ArrayList<Double> getDensities() {
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
		return this.matrix[i].getClusterID(this.densities.get(j));
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

	public Double getDensity(int i)	{
		return this.densities.get(i);
	}
	
	public void lexicographicSort()	{

		List<ObjInstance> aux = Arrays.asList(this.matrix);
				
		Collections.sort(aux, new Comparator<ObjInstance>() {
			@Override
			public int compare(ObjInstance o1, ObjInstance o2) {

				for (int i = densities.size() - 1; i >= 0 ; i--) {
					double k = densities.get(i);
					
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

	// This method is called to clear both matrix and densities data structures.
	public void clearAll() {
		this.matrix = null;
		this.densities.clear();
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