/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ualberta.cs.SHM.HMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;

/**
 *
 * @author Fernando Soares de Aguiar Neto.
 */
public class ObjInstance implements java.io.Serializable {
	public final int NOISE = 0; 			// Cluster ID associated to noise.

	private int id; 						// Holds the ID of an object
	private String observation; 			// Holds tips for the objects. (Image-link, description, etc.)
	private String observation2; 			// Holds tips for the objects. (Image-link, description, etc.) Not in use by Plotter.
	private String observation3; 			// Holds tips for the objects. (Image-link, description, etc.) Not in use by Plotter.
	private int label;      				// Holds the label of this object, default -1. Since labels will be provided by the user and by external algorithms.

	private TreeMap<Double, Integer> clusterIDs; 	//Holds the cluseterId values attached to the highest density.
	//Notice that you must put the noise densities into the map.
	private HashMap<Double, Integer> clusterIDsHash; 	//Holds the cluseterId values attached to the highest density.

	private double reachabilityDistance;    // Holds the reachability Distance value of this object
	private double outlierScore;			// Holds the outlier Score given to this object after applying the HDBSCAN* algorithm
	private double coreDistance;			// Holds the core distance of this object.

	private double deathLevel;           	// Holds the last density where this object is not a noise.

	private int HDBSCANPartition;			// Holds the partitioning given by HDBSCAN

	private static final long serialVersionUID = 7L;

	public ObjInstance(int id, String observation, String observation2, String observation3) {
		this.id = id;
		this.observation = observation;
		this.observation2 = observation2;
		this.observation3 = observation3;
		this.label = -1;
		this.clusterIDs = new TreeMap<Double, Integer>();
		this.clusterIDsHash = new HashMap<Double, Integer>();
		this.reachabilityDistance = -1.0;
		this.outlierScore = -1.0;
		this.coreDistance = -1.0;
		this.HDBSCANPartition = -1;
		this.deathLevel = 0.0;
	}

	public ObjInstance(int id) {
		this.id = id;
		this.observation = "";
		this.observation2 = "";
		this.observation3 = "";
		this.label = -1;
		this.clusterIDs = new TreeMap<Double, Integer>();
		this.clusterIDsHash = new HashMap<Double, Integer>();
		this.reachabilityDistance = -1.0;
		this.outlierScore = -1.0;
		this.coreDistance = -1.0;
		this.HDBSCANPartition = -1;
		this.deathLevel = 0.0;
	}

	public void updateDeathLevel() {
		//gets the Idx of the last index that is not noise.
		ArrayList<Double> keys = new ArrayList<Double>(this.clusterIDs.keySet());
		this.deathLevel = keys.get(1);
	}

	public double getDeathLevel() {
		return this.deathLevel;
	}

	public int getID() {
		return this.id;
	}

	public void setOutlierScore(double outlierScore) {
		this.outlierScore = outlierScore;
	}

	public double getOutlierScore() {
		return this.outlierScore;   	
	}

	public void setHDBSCANPartition(int HDBSCANPartition) {
		this.HDBSCANPartition = HDBSCANPartition;
	}

	public int getHDBSCANPartition() {
		return this.HDBSCANPartition;
	}

	public void setCoreDistance(double coreDistance) {
		this.coreDistance = coreDistance;
	}

	public double getCoreDistance() {
		return this.coreDistance;   	
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public int getLabel() {
		return this.label;
	}

	public void setReachabilityDistance(double reachabilityDistance) {
		this.reachabilityDistance = reachabilityDistance;
	}

	public double getReachabilityDistance() {
		return this.reachabilityDistance;
	}

	public String getObservation() {
		return this.observation;
	}

	public String getObservation2() {
		return this.observation2;
	}

	public String getObservation3() {
		return this.observation3;
	}

	public void setObservation(String observation) {
		this.observation = observation;
	}

	public void setObservation2(String observation2) {
		this.observation2 = observation2;
	}

	public void setObservation3(String observation3) {
		this.observation3 = observation3;
	}

	public TreeMap<Double, Integer> getAllClusters() {
		return this.clusterIDs;
	}

	public Set<Double> getDensities() {
		return this.clusterIDs.keySet();
	}

	public int getClusterID(double d) {	
        return this.clusterIDs.getOrDefault(this.clusterIDs.floorKey(d), NOISE);
	}

	public int getClusterIDHash(double d) {	
        return this.clusterIDsHash.getOrDefault(d, NOISE);
	}
	
	public void put(double density, int clusterID) {
		this.clusterIDs.put(density, clusterID);
		this.clusterIDsHash.put(density, clusterID);
	}

	@Override
	public String toString() {
		String out = "ID:"+this.id+" OBS:"+this.observation+"| "+" OBS2:"+this.observation2+"| "+" OBS3:"+this.observation3+"| ";

		for(double density : this.clusterIDs.keySet()) {
			out += "[D="+density+" Cl="+this.clusterIDs.get(density)+"]";
		}

		return out;
	}
}
