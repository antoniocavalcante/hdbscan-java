/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ualberta.cs.SHM.HMatrix;

import java.io.Serializable;

import it.unimi.dsi.fastutil.doubles.DoubleBigArrayBigList;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;

/**
 *
 * @author Fernando Soares de Aguiar Neto.
 */
public class ObjInstance implements Serializable {

	private int id; 						// Holds the ID of an object

	private DoubleBigArrayBigList densityLevels;
	private IntBigArrayBigList clusterID;
	
	private double reachabilityDistance;    // Holds the Reachability Distance value of this object.
	private double outlierScore;			// Holds the outlier Score given to this object after applying the HDBSCAN* algorithm.
	private double coreDistance;			// Holds the core distance of this object.

	private double deathLevel;           	// Holds the last density where this object is not a noise.

	private int HDBSCANPartition;			// Holds the partitioninObjInstanceg given by HDBSCAN*

	private static final long serialVersionUID = 7L;

	public ObjInstance(int id) {
		this.id = id;
		this.densityLevels = new DoubleBigArrayBigList();
		this.clusterID = new IntBigArrayBigList();
		this.reachabilityDistance = -1.0;
		this.outlierScore = -1.0;
		this.coreDistance = -1.0;
		this.HDBSCANPartition = -1;
		this.deathLevel = 0.0;		
	}

	public void updateDeathLevel() {
		this.deathLevel = this.densityLevels.getDouble(this.densityLevels.size64() - 2);
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

	public void setReachabilityDistance(double reachabilityDistance) {
		this.reachabilityDistance = reachabilityDistance;
	}

	public double getReachabilityDistance() {
		return this.reachabilityDistance;
	}

	public IntBigArrayBigList getClusterID() {
		return clusterID;
	}

	public void setClusterID(IntBigArrayBigList clusterID) {
		this.clusterID = clusterID;
	}
	
	public DoubleBigArrayBigList getDensities() {
		return this.densityLevels;
	}

	public int getClusterID(double d) {
						
		long lo = 0;
		long hi = this.densityLevels.size64() - 1;
		long middle = 0;
		
		while (lo < hi) {
			
			middle = (int) Math.floor((lo + hi)/2);
			
			if (d == this.densityLevels.getDouble(middle)) {				
				return this.clusterID.getInt(middle);
			}
			
			if (d < this.densityLevels.getDouble(middle) && d > this.densityLevels.getDouble(middle + 1)) {				
				return this.clusterID.getInt(middle + 1);
			}
			
			if (d < this.densityLevels.getDouble(middle)) {
				lo = middle + 1;
				continue;
			}
			
			if (d > this.densityLevels.getDouble(middle)) {
				hi = middle;
				continue;
			}
		}

		return this.clusterID.getInt(middle);
	}	
	
	public void put(double density, int clusterID) {
		this.densityLevels.add(density);
		this.clusterID.add(clusterID);
	}

	@Override
	public String toString() {
		String out = "ID: " + this.id;

		for(double density : this.densityLevels) {
			out += "[D = " + density + " Cl= "+ this.getClusterID(density) + "]";
		}

		return out;
	}
}
