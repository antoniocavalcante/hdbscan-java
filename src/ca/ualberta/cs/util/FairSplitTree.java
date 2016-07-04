package ca.ualberta.cs.util;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.MutualReachabilityGraph;

public class FairSplitTree {

	private static Double[][] S;
	private static int dimensions;

	private Double[][] boundingBox;
	private FairSplitTree left;
	private FairSplitTree right;
	private int count;
	private int level;
	private boolean leaf;
	private int p;
	private double diameter = -1;
	
	public List<Integer> P;
	public int id;
	
	/** Receives a set S of d-dimensional points and calls the constructor to build a FairSplitTree.
	 * 
	 * @param S Set of d-dimensional points.
	 * @return FairSplitTree from S.
	 */
	public static FairSplitTree build(Double[][] S) {
		FairSplitTree.S = S;
		FairSplitTree.dimensions = S[0].length;
		
//		System.out.println("Data set size: " + S.length);
//		System.out.println("Dimensions: " + dimensions);
		
		ArrayList<Integer> P = new ArrayList<Integer>();

		for (int i = 0; i < S.length; i++) {
			P.add(i);
		}
		
		return new FairSplitTree(P, 0, 1);
	}

	
	/** Main constructor of a FairSplitTree. Receives a set of integers, representing
	 *  the points IDs and the current level of the tree.
	 * 
	 * @param P Set of points IDs.
	 * @param level Current level of the tree.
	 */
	public FairSplitTree(ArrayList<Integer> P, int level, int id){
		// Update the id of the tree.
		this.id = id;
		
		// Update the level of the tree.
		this.setLevel(level);
		
		// Check the cardinality of the set, to distinguish leaf from internal nodes.
		this.setCount(P.size());
	
		// Store the id's of the points under this tree.
		this.P = P;
		
		if (this.count == 0) {
			this.leaf = true;
			this.right = null;
			this.left = null;
		
		} else {

			// Construct Bounding Box.
			this.boundingBox = new Double[2][dimensions];

			// Initialize Bounding Box.
			for (int i = 0; i < dimensions; i++) {
				this.boundingBox[0][i] = S[P.get(0)][i];
				this.boundingBox[1][i] = S[P.get(0)][i];
			}
			
			if (this.count == 1) {
				this.leaf = true;
				this.right = null;
				this.left = null;
				this.p = P.get(0);
				
			} else {
				// This should be an internal node.
				this.leaf = false;
				
				for (Integer p : P) {
					for (int i = 0; i < dimensions; i++) {

						if (S[p][i] < this.boundingBox[0][i]) {
							this.boundingBox[0][i] = S[p][i];
						}

						if (S[p][i] > this.boundingBox[1][i]) {
							this.boundingBox[1][i] = S[p][i];
						}
					}
				}
				
				// Find the dimension j where the hyper-rectangle edge is larger.
				int j = 0;
				double max = 0;

				for (int i = 0; i < boundingBox.length; i++) {

					double d = (boundingBox[1][i] - boundingBox[0][i]);
					
					if (d > max) {
						j = i;
						max = d;
					}
				}
				
				// Split this dimension in two hyper-rectangles.
				ArrayList<Integer> right = new ArrayList<Integer>();
				ArrayList<Integer> left  = new ArrayList<Integer>();

				double cutPoint = (boundingBox[0][j] + boundingBox[1][j])/2;
				
				for (Integer p : P) {
					if (S[p][j] < cutPoint) {
						left.add(p);
					} else {
						right.add(p);
					}
				}
				
				// Recursive call for left and right children.
				this.left  = new FairSplitTree(left,  this.level + 1, this.id*2);
				this.right = new FairSplitTree(right, this.level + 1, this.id*2 + 1);
			}
		}
	}
	
	// 
	/** Returns all the points under the tree.
	 * @return List containing the IDs of the points under the tree.
	 */
	public List<Integer> retrieve() {
		return this.P;
	}

	public static void print(FairSplitTree T) {
		System.out.print("|");
		for (int i = 0; i < T.level; i++) {
			System.out.print("-");
		}
		System.out.print(" ");
		System.out.println("id " + T.id + ": " + T.P + " - diameter: " + T.diameter());
		if (!T.isLeaf()) print(T.left);
		if (!T.isLeaf()) print(T.right);
	}
	
	public static double boxDistance(FairSplitTree T1, FairSplitTree T2) {

		boolean[] overlap = new boolean[FairSplitTree.dimensions];
		double[] distance = new double[FairSplitTree.dimensions];
		
		int count = 0;
		
		for (int i = 0; i < FairSplitTree.dimensions; i++) {
			if (T1.boundingBox[1][i] >= T2.boundingBox[0][i] && T1.boundingBox[0][i] <= T2.boundingBox[1][i]) {
				overlap[i] = true;
				count++;
			} else {
				overlap[i] = false;
			}
			
			distance[i] = Math.min(Math.abs(T1.boundingBox[0][i]-T2.boundingBox[1][i]), Math.abs(T1.boundingBox[1][i]-T2.boundingBox[0][i]));
		}
		
		// There is overlapping.
		if (count == dimensions - 1) {
			for (int i = 0; i < overlap.length; i++) {
				if (!overlap[i]) {
					return distance[i];
				}
			}
		}

		if (count == dimensions) {
			return 0;
		}
		
		double d = 0;

		for (int i = 0; i < distance.length; i++) {
			d = d + Math.pow(distance[i], 2);
		}
		
		return Math.sqrt(d);
	}
	
//	public static double distance(FairSplitTree T1, FairSplitTree T2) {
//		return (new EuclideanDistance()).computeDistance(T1.center(), T2.center()) - T1.diameter/2 - T2.diameter/2;
//	}
//	
//	public double distance(FairSplitTree T) {
//		return distance(this, T);
//	}
	
	public double diameter() {

		if (diameter == -1) {
			if (this.leaf) {
				this.diameter = 0;
			} else {
				this.diameter = (new EuclideanDistance()).computeDistance(boundingBox[0], boundingBox[1]);
			}
		}

		return diameter;		
	}
	
	public Double[] center() {
		Double[] center = new Double[dimensions];

		for (int i = 0; i < center.length; i++) {
			center[i] = (boundingBox[0][i] + boundingBox[1][i])/2;
		}

		return center;
	}
	
	public int getDimensions() {
		return dimensions;
	}

	public void setDimensions(int dimensions) {
		FairSplitTree.dimensions = dimensions;
	}

	public FairSplitTree getLeft() {
		return left;
	}

	public void setLeft(FairSplitTree left) {
		this.left = left;
	}

	public FairSplitTree getRight() {
		return right;
	}

	public void setRight(FairSplitTree right) {
		this.right = right;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public int getP() {
		return p;
	}

	public void setP(int p) {
		this.p = p;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FairSplitTree other = (FairSplitTree) obj;
		if (id != other.id)
			return false;
		return true;
	}
		
}
