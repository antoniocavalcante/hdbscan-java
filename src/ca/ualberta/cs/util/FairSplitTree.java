package ca.ualberta.cs.util;

import ca.ualberta.cs.distance.DistanceCalculator;
import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.ints.IntBigArrayBigList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class FairSplitTree {

	public static Dataset S;
	private static int dimensions;
	public static Long2ObjectOpenHashMap<FairSplitTree> root;

	public static int globalId;

	public static DistanceCalculator distanceFunction;

	private double[][] boundingBox;
	private long parent;
	private long left;
	private long right;
	private long count;
	private int level;
	private boolean leaf;
	private int p;
	private double diameter = -1;
	private double diameterMRD = -1;

	private double maxCD = 0;

	public BigList<Integer> P;
	public long id;

	/** Receives a set S of d-dimensional points and calls the constructor to build a FairSplitTree.
	 * 
	 * @param S Set of d-dimensional points.
	 * @return FairSplitTree from S.
	 */
	public static FairSplitTree build(Dataset S, double[][] coreDistances, int k, DistanceCalculator distanceFunction) {
		FairSplitTree.S = S;
		FairSplitTree.dimensions = S.dimensions();
		FairSplitTree.root = new Long2ObjectOpenHashMap<FairSplitTree>();

		FairSplitTree.distanceFunction = distanceFunction;

		BigList<Integer> P = new IntBigArrayBigList();

		for (int i = 0; i < S.length(); i++) {
			P.add(i);
		}

		globalId = 1;

		FairSplitTree T = new FairSplitTree(null, P, 0, 0, 1, k, coreDistances);
		root.put(1, T);

		return T;
	}


	/** Main constructor of a FairSplitTree. Receives a set of integers, representing
	 *  the points IDs and the current level of the tree.
	 * 
	 * @param P Set of points IDs.
	 * @param level Current level of the tree.
	 */
	public FairSplitTree(FairSplitTree parent, BigList<Integer> P, double maxCD, int level, long id, int k, double[][] coreDistances){

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

		// Store the ids of the points under this tree.
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
				this.boundingBox[0][i] = S.get(P.get(0), i);
				this.boundingBox[1][i] = S.get(P.get(0), i);
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

						if (S.get(p, i) < this.boundingBox[0][i]) {
							this.boundingBox[0][i] = S.get(p, i);
						}

						if (S.get(p, i) > this.boundingBox[1][i]) {
							this.boundingBox[1][i] = S.get(p, i);
						}
					}
					this.diameterMRD = Math.max(this.diameterMRD, coreDistances[p][k-1]);
				}

				this.diameter = FairSplitTree.distanceFunction.computeDistance(boundingBox[0], boundingBox[1]);
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
					if (S.get(p, j) < cutPoint) {
						leftMaxCd = Math.max(leftMaxCd, coreDistances[p][k-1]);
						left.add(p);
					} else {
						rightMaxCd = Math.max(rightMaxCd, coreDistances[p][k-1]);
						right.add(p);
					}
				}

				// Check if the split was successful or if points in P are all the same.
				if (left.isEmpty() && right.size() > 1) {
					left.add(right.remove(0));
				}
				
				// Recursive call for left and right children.
				this.left = nextId();
				root.put(this.left, new FairSplitTree(this, left, leftMaxCd, this.level + 1, this.left, k, coreDistances));

				this.right = nextId();
				root.put(this.right, new FairSplitTree(this, right, rightMaxCd, this.level + 1, this.right, k, coreDistances));

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

	public static int nextId() {
		globalId = globalId + 1;
		return globalId;
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

	public static double circleDistanceMRD(FairSplitTree T1, FairSplitTree T2) {		
		return Math.max(circleDistance(T1, T2), Math.max(T1.maxCD, T2.maxCD));
	}

	public static double circleDistance(FairSplitTree T1, FairSplitTree T2) {
		return FairSplitTree.distanceFunction.computeDistance(T1.center(), T2.center()) - T1.diameter()/2 - T2.diameter()/2;
	}

	public static double circleDistance(double[] point, FairSplitTree T) {
		return FairSplitTree.distanceFunction.computeDistance(point, T.center()) - T.diameter()/2;
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

	public long left() {
		return left;
	}

	public long right() {
		return right;
	}

	public long getCount() {
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
		result = prime * result + (int)id;
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
