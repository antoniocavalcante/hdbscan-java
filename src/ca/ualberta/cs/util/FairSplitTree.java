package ca.ualberta.cs.util;

import java.util.ArrayList;
import java.util.Collections;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;

public class FairSplitTree {

	public static double[][] S;
	private static int dimensions;
	public static Int2ObjectOpenHashMap<FairSplitTree> root;
	
	private double[][] boundingBox;
	private int parent;
	private int left;
	private int right;
	private int count;
	private int level;
	private boolean leaf;
	private int p;
	private double diameter = -1;
	private double diameterMRD = -1;

	private double maxCD = 0;
	
	public BigList<Integer> P;
	public int id;
	
	/** Receives a set S of d-dimensional points and calls the constructor to build a FairSplitTree.
	 * 
	 * @param S Set of d-dimensional points.
	 * @return FairSplitTree from S.
	 */
	public static FairSplitTree build(double[][] S) {
		FairSplitTree.S = S;
		FairSplitTree.dimensions = S[0].length;
		FairSplitTree.root = new Int2ObjectOpenHashMap<FairSplitTree>();

		BigList<Integer> P = new IntBigArrayBigList();

		for (int i = 0; i < S.length; i++) {
			P.add(i);
		}

		FairSplitTree T = new FairSplitTree(null, P, 0, 0, 1);
		root.put(1, T);
		
		return T;
	}

	
	/** Main constructor of a FairSplitTree. Receives a set of integers, representing
	 *  the points IDs and the current level of the tree.
	 * 
	 * @param P Set of points IDs.
	 * @param level Current level of the tree.
	 */
	public FairSplitTree(FairSplitTree parent, BigList<Integer> P, double maxCD, int level, int id){
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
		this.P = P;
		
		this.maxCD = maxCD;
		
		if (this.count == 0) {
			this.leaf = true;
			this.right = -1;
			this.left = -1;
		
		} else {

			// Construct Bounding Box.
			this.boundingBox = new double[2][dimensions];

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
					this.diameterMRD = Math.max(this.diameterMRD, IncrementalHDBSCANStar.coreDistances[p][IncrementalHDBSCANStar.k-1]);
				}
				
				this.diameter = (new EuclideanDistance()).computeDistance(boundingBox[0], boundingBox[1]);
				this.diameterMRD = Math.max(this.diameter, this.diameterMRD);
				
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
				BigList<Integer> right = new IntBigArrayBigList();
				BigList<Integer> left  = new IntBigArrayBigList();

				double cutPoint = (boundingBox[0][j] + boundingBox[1][j])/2;
				
				double leftMaxCd  = -Double.MAX_VALUE;
				double rightMaxCd = -Double.MAX_VALUE;
				
				for (Integer p : P) {
					if (S[p][j] < cutPoint) {
						leftMaxCd = Math.max(leftMaxCd, IncrementalHDBSCANStar.coreDistances[p][IncrementalHDBSCANStar.k-1]);
						left.add(p);
					} else {
						rightMaxCd = Math.max(rightMaxCd, IncrementalHDBSCANStar.coreDistances[p][IncrementalHDBSCANStar.k-1]);
						right.add(p);
					}
				}
				
				// Recursive call for left and right children.
				this.left  = this.id*2;
				root.put(this.left, new FairSplitTree(this, left, leftMaxCd, this.level + 1, this.left));
				this.right = this.id*2 + 1;
				root.put(this.right, new FairSplitTree(this, right, rightMaxCd, this.level + 1, this.right));
			}
		}
	}
	
	// 
	/** Returns all the points under the tree.
	 * @return List containing the IDs of the points under the tree.
	 */
	public BigList<Integer> retrieve() {		
		return this.P;
	}

//	public BigList<Integer> retrieve() {
//		Stack<Integer> s = new Stack<>();
//
//		BigList<Integer> P = new IntBigArrayBigList();
//		
//		s.add(this.id);
//		
//		while (!s.isEmpty()) {
//			int e = s.pop();
//			
//			if (root.get(e).isLeaf()) {
//				P.add(root.get(e).p);
//			} else {
//				s.add(root.get(e).left());
//				s.add(root.get(e).right());
//			}
//		}
//		
//		return P;
//	}
	
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

	public static double circleDistance(FairSplitTree T1, FairSplitTree T2) {
		return (new EuclideanDistance()).computeDistance(T1.center(), T2.center()) - T1.diameter()/2 - T2.diameter()/2;
	}

	public static double circleDistance(double[] point, FairSplitTree T) {
		return (new EuclideanDistance()).computeDistance(point, T.center()) - T.diameter()/2;
	}
	
	public double diameter() {

		if (this.isLeaf()) {
			return 0;
		}
				
		return diameter;		
	}

	public double diameterMRD() {

		if (this.isLeaf()) {
			return 0;
		}
				
		return diameterMRD;		
	}
	
	public double[] center() {
		double[] center = new double[dimensions];

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
			int i = path1.get(path1.size()-1);
			path1.add(root.get(i).parent);
		}

		while (path2.get(path2.size()-1) != 1) {
			int i = path2.get(path2.size()-1);
			path2.add(root.get(i).parent);
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
	
	public static BigList<Integer> rangeSearch(FairSplitTree root, double[] queryPoint, double r, BigList<Integer> arrayList) {
		double left  = circleDistance(queryPoint, FairSplitTree.root.get(root.left));
		double right = circleDistance(queryPoint, FairSplitTree.root.get(root.right));

		if (left < r) {
			if (FairSplitTree.root.get(root.left).isLeaf()) {
				// Add to the results.
				arrayList.add(FairSplitTree.root.get(root.left).p);
			} else {
				rangeSearch(FairSplitTree.root.get(root.left), queryPoint, r, arrayList);
			}
		}
		
		if (right < r) {
			if (FairSplitTree.root.get(root.right).isLeaf()) {
				// Add to the results
				arrayList.add(FairSplitTree.root.get(root.right).p);
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

	public double getMaxCD() {
		return maxCD;
	}

	public void setMaxCD(double maxCD) {
		this.maxCD = maxCD;
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
