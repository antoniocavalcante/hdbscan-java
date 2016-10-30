package ca.ualberta.cs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;

public class FairSplitTree {

	public static Double[][] S;
	private static int dimensions;
	public static HashMap<Integer, FairSplitTree> root;

	private Double[][] boundingBox;
	private int parent;
	private int left;
	private int right;
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
		FairSplitTree.root = new HashMap<Integer, FairSplitTree>();
		ArrayList<Integer> P = new ArrayList<Integer>();

		for (int i = 0; i < S.length; i++) {
			P.add(i);
		}

		FairSplitTree T = new FairSplitTree(null, P, 0, 1);
		root.put(1, T);
		
		return T;
	}

	
	/** Main constructor of a FairSplitTree. Receives a set of integers, representing
	 *  the points IDs and the current level of the tree.
	 * 
	 * @param P Set of points IDs.
	 * @param level Current level of the tree.
	 */
	public FairSplitTree(FairSplitTree parent, ArrayList<Integer> P, int level, int id){
		// Update parent.
		if (id == 1){
			this.parent = 1;
		} else {
			this.parent = parent.id;
		}
		// Update the id of the tree.
		this.id = id;
		
		// Update the level of the tree.
		this.setLevel(level);
		
		// Check the cardinality of the set, to distinguish leaf from internal nodes.
		this.setCount(P.size());
	
		// Store the id's of the points under this tree.
//		this.P = P;
		this.P = null;
		
		if (this.count == 0) {
			this.leaf = true;
			this.right = -1;
			this.left = -1;
		
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
				this.right = -1;
				this.left = -1;
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
					this.diameter = Math.max(this.diameter, IncrementalHDBSCANStar.coreDistances[p][IncrementalHDBSCANStar.k-1]);
				}
				
				this.diameter = Math.max(this.diameter, (new EuclideanDistance()).computeDistance(boundingBox[0], boundingBox[1]));
				
				// Find the dimension j where the hyper-rectangle edge is larger.
				int j = 0;
				double max = 0;

				for (int i = 0; i < boundingBox[0].length; i++) {

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
				this.left  = this.id*2;
				root.put(this.left, new FairSplitTree(this, left,  this.level + 1, this.left));
				this.right = this.id*2 + 1;
				root.put(this.right, new FairSplitTree(this, right, this.level + 1, this.right));
			}
		}
	}
	
	// 
	/** Returns all the points under the tree.
	 * @return List containing the IDs of the points under the tree.
	 */
//	public List<Integer> retrieve() {		
//		return this.P;
//	}

	public BigList<Integer> retrieve() {
		Stack<Integer> s = new Stack<>();

		BigList<Integer> P = new IntBigArrayBigList();
		
		s.add(this.id);
		
		while (!s.isEmpty()) {
			int e = s.pop();
			
			if (root.get(e).isLeaf()) {
				P.add(root.get(e).p);
			} else {
				s.add(root.get(e).left());
				s.add(root.get(e).right());
			}
		}
		
		return P;
	}
	
	public static void print(FairSplitTree T) {
		System.out.print("|");
		for (int i = 0; i < T.level; i++) {
			System.out.print("-");
		}
		System.out.print(" ");
		System.out.println("id " + T.id + ": " + T.P + " - diameter: " + T.diameter());
		if (!T.isLeaf()) print(T.getLeft());
		if (!T.isLeaf()) print(T.getRight());
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

		// There is overlapping in all dimensions, meaning a portion of the boxes are inside the other one. Distance is 0 in this case.
		if (count == dimensions) {
			return 0;
		}

		// There is overlapping in zero dimensions. In this case, Pitagoras is applied.
		double d = 0;

		if (count == 0) {
			for (int i = 0; i < overlap.length; i++) {
				d = d + Math.pow(distance[i], 2);
			}
			return Math.sqrt(d);
		}
		
		// There is overlapping in more than one dimension. Distance between plans.
		double min = Double.MAX_VALUE;
		
		if (count > 0) {
			for (int i = 0; i < distance.length; i++) {
				if (overlap[i]) {
					min = Math.min(distance[i], min);
				}
			}
		
			return min;
		}
		System.out.println("HERE");
		return 0;
	}

	public static double boxDistance(Double[] point, FairSplitTree T) {

		boolean[] overlap = new boolean[FairSplitTree.dimensions];
		double[] distance = new double[FairSplitTree.dimensions];
		
		int count = 0;
		
		for (int i = 0; i < FairSplitTree.dimensions; i++) {
			if (point[i] >= T.boundingBox[0][i] && point[i] <= T.boundingBox[1][i]) {
				overlap[i] = true;
				count++;
			} else {
				overlap[i] = false;
			}
			
			distance[i] = Math.min(Math.abs(point[i]-T.boundingBox[1][i]), Math.abs(point[i]-T.boundingBox[0][i]));
		}
		
		// There is overlapping.
		if (count == dimensions - 1) {
			for (int i = 0; i < overlap.length; i++) {
				if (!overlap[i]) {
					return distance[i];
				}
			}
		}

		// Point is inside the FairSplitTree.
		if (count == dimensions) {
			return 0;
		}
		
		double d = 0;

		for (int i = 0; i < distance.length; i++) {
			d = d + Math.pow(distance[i], 2);
		}
		
		return Math.sqrt(d);
	}

	public static double circleDistance(FairSplitTree T1, FairSplitTree T2) {
		return (new EuclideanDistance()).computeDistance(T1.center(), T2.center()) - T1.diameter()/2 - T2.diameter()/2;
	}

	public static double circleDistance(Double[] point, FairSplitTree T) {
		return (new EuclideanDistance()).computeDistance(point, T.center()) - T.diameter()/2;
	}
	
	public double diameter() {

		if (this.isLeaf()) {
			return 0;
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
	
	public static FairSplitTree parent(FairSplitTree T1, FairSplitTree T2) {
		ArrayList<Integer> path1 = new ArrayList<Integer>();
		ArrayList<Integer> path2 = new ArrayList<Integer>();
		
		path1.add(T1.parent);
		path2.add(T2.parent);
		
		// Stores path from T1 to the root.
		while (path1.get(path1.size()-1) != 1) {
			path1.add(root.get(path1.get(path1.size()-1)).parent);
		}

		while (path2.get(path2.size()-1) != 1) {
			path2.add(root.get(path2.get(path2.size()-1)).parent);
		}
		
		Collections.sort(path1);
		Collections.sort(path2);
				
		for (int i = 0; i < Math.min(path1.size(), path2.size()); i++) {
			if (path1.get(i) != path2.get(i)) {
				int id = path1.get(i-1);

				path1 = null;
				path2 = null;
				
				return root.get(id);
			}
		}

		path1 = null;
		path2 = null;
		
		return root.get(1);
	}
	
	public static FairSplitTree parent(FairSplitTree T) {
		return root.get(T.parent);
	}
	
	public static ArrayList<Integer> rangeSearch(FairSplitTree root, Double[] queryPoint, double r, ArrayList<Integer> arrayList) {
		double left  = circleDistance(queryPoint, FairSplitTree.root.get(root.left));
		double right = circleDistance(queryPoint, FairSplitTree.root.get(root.right));

		if (left <= r) {
			if (FairSplitTree.root.get(root.left).isLeaf()) {
				// Add to the results.
				arrayList.addAll(FairSplitTree.root.get(root.left).retrieve());
			} else {
				rangeSearch(FairSplitTree.root.get(root.left), queryPoint, r, arrayList);
			}
		}
		
		if (right <= r) {
			if (FairSplitTree.root.get(root.right).isLeaf()) {
				// Add to the results
				arrayList.addAll(FairSplitTree.root.get(root.right).retrieve());
			} else {
				rangeSearch(FairSplitTree.root.get(root.right), queryPoint, r, arrayList);
			}
		}
		
		return arrayList;
	}
	
	public int getDimensions() {
		return dimensions;
	}

	public void setDimensions(int dimensions) {
		FairSplitTree.dimensions = dimensions;
	}

	public FairSplitTree getLeft() {
		return FairSplitTree.root.get(left);
	}

	public FairSplitTree getRight() {
		return FairSplitTree.root.get(right);
	}

	public int left() {
		return left;
	}

	public int right() {
		return right;
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
