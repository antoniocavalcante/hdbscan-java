package ca.ualberta.cs.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.TreeSet;

import ca.ualberta.cs.distance.EuclideanDistance;

public class KdTree {

	private int d = 0;

	public KdNode root = null;

	public static double[][] points;

	public static int axis = 0;

	public static int n = 0;

	public static long time = 0;

	private final static Comparator<Integer> COMPARATOR = new Comparator<Integer>() {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(Integer o1, Integer o2) {
			if (points[o1][axis] < points[o2][axis]) return -1;
			if (points[o1][axis] > points[o2][axis]) return 1;
			return 0;
		}
	};

	private static final int compareTo(double[] o1, double[] o2) {

		if (o1.length != o2.length) return 0;

		for (int i = 0; i < o1.length; i++) {
			if (o1[i] < o2[i]) return -1;
			if (o1[i] > o2[i]) return 1;						
		}

		return 0;
	}

	/**
	 * Default constructor.
	 */
	public KdTree() { }

	/**
	 * More efficient constructor.
	 * 
	 * @param list of XYZPoints.
	 */
	public KdTree(double[][] list) {
		points = list;
		this.d = list[0].length;

		List<Integer> l = new ArrayList<Integer>();

		for (int i = 0; i < list.length; i++) {
			l.add(i);
		}

		root = createNode(l, d, 0);
	}

	/**
	 * Create node from list of XYZPoints.
	 * 
	 * @param list of XYZPoints.
	 * @param k of the tree.
	 * @param depth depth of the node.
	 * @return node created.
	 */
	private static KdNode createNode(List<Integer> list, int k, int depth) {
		n++;

		if (list == null || list.size() == 0) return null;

		axis = depth % k;

		Collections.sort(list, COMPARATOR);
		//		System.out.println(list);

		int mediaIndex = list.size()/2;

		KdNode node = new KdNode(k, depth, list.get(mediaIndex));

		List<Integer> less = list.subList(0, mediaIndex);
		List<Integer> more = list.subList(mediaIndex+1, list.size());

		if (list.size() > 0) {

			if ((mediaIndex-1) >= 0) {
				less = list.subList(0, mediaIndex);

				if (less.size() > 0) {
					node.lesser = createNode(less, k, depth + 1);
					node.lesser.parent = node;
				}
			}

			if ((mediaIndex + 1) <= (list.size() - 1)) {
				more = list.subList(mediaIndex + 1, list.size());

				if (more.size() > 0) {
					node.greater = createNode(more, k, depth+1);
					node.greater.parent = node;
				}
			}
		}

		return node;
	}

	/**
	 * Add value to the tree. Tree can contain multiple equal values.
	 * 
	 * @param value T to add to the tree.
	 * @return True if successfully added to tree.
	 */
	public boolean add(Integer value) {
		if (value==null) return false;

		if (root==null) {
			root = new KdNode(value);
			return true;
		}

		KdNode node = root;
		while (true) {
			if (KdNode.compareTo(node.depth, node.k, node.id, value) <= 0) {
				//Lesser
				if (node.lesser == null) {
					KdNode newNode = new KdNode(d, node.depth + 1, value);
					newNode.parent = node;
					node.lesser = newNode;
					break;
				} else {
					node = node.lesser;
				}
			} else {
				//Greater
				if (node.greater == null) {
					KdNode newNode = new KdNode(d, node.depth + 1, value);
					newNode.parent = node;
					node.greater = newNode;
					break;
				} else {
					node = node.greater;
				}
			}
		}

		return true;
	}

	/**
	 * Does the tree contain the value.
	 * 
	 * @param value T to locate in the tree.
	 * @return True if tree contains value.
	 */
	public boolean contains(Integer value) {
		if (value==null) return false;

		KdNode node = getNode(this,value);
		return (node!=null);
	}

	/**
	 * Locate T in the tree.
	 * 
	 * @param tree to search.
	 * @param value to search for.
	 * @return KdNode or NULL if not found
	 */
	private static final KdNode getNode(KdTree tree, Integer value) {
		if (tree==null || tree.root==null || value==null) return null;

		KdNode node = tree.root;
		while (true) {
			if (node.id.equals(value)) {
				return node;
			} else if (KdNode.compareTo(node.depth, node.k, node.id, value)<0) {
				//Greater
				if (node.greater==null) {
					return null;
				} else {
					node = node.greater;
				}
			} else {
				//Lesser
				if (node.lesser==null) {
					return null;
				} else {
					node = node.lesser;
				}
			}
		}
	}

	/**
	 * Remove first occurrence of value in the tree.
	 * 
	 * @param value T to remove from the tree.
	 * @return True if value was removed from the tree.
	 */
	public boolean remove(Integer value) {
		if (value==null) return false;

		KdNode node = getNode(this,value);
		if (node==null) return false;

		KdNode parent = node.parent;
		if (parent!=null) {
			if (parent.lesser!=null && node.equals(parent.lesser)) {
				List<Integer> nodes = getTree(node);
				if (nodes.size()>0) {
					parent.lesser = createNode(nodes,node.k,node.depth);
					if (parent.lesser!=null) {
						parent.lesser.parent = parent;
					}
				} else {
					parent.lesser = null;
				}
			} else {
				List<Integer> nodes = getTree(node);
				if (nodes.size()>0) {
					parent.greater = createNode(nodes,node.k,node.depth);
					if (parent.greater!=null) {
						parent.greater.parent = parent;
					}
				} else {
					parent.greater = null;
				}
			}
		} else {
			//root
			List<Integer> nodes = getTree(node);
			if (nodes.size()>0) root = createNode(nodes,node.k,node.depth);
			else root = null;
		}

		return true;
	}

	/**
	 * Get the (sub) tree rooted at root.
	 * 
	 * @param root of tree to get nodes for.
	 * @return points in (sub) tree, not including root.
	 */
	public static final List<Integer> getTree(KdNode root) {
		List<Integer> list = new ArrayList<Integer>();
		if (root==null) {
			return list;
		} else {
			list.add(root.id);
		}

		if (root.lesser!=null) {
			list.add(root.lesser.id);
			list.addAll(getTree(root.lesser));
		}
		if (root.greater!=null) {
			list.add(root.greater.id);
			list.addAll(getTree(root.greater));
		}

		return list;
	}

	/**
	 * K Nearest Neighbor search
	 * 
	 * @param K Number of neighbors to retrieve. Can return more than K, if last nodes are equal distances.
	 * @param value to find neighbors of.
	 * @return collection of T neighbors.
	 */
	public Collection<Integer> nearestNeighbourSearch(int K, Integer value) {
		if (value == null || root == null)
			return Collections.emptyList();

		//Map used for results
		TreeSet<Tuple> results = new TreeSet<Tuple>(new TupleComparator());

		//Find the closest leaf node
		KdNode prev = null;
		KdNode node = root;

		while (node != null) {
			if (KdNode.compareTo(node.depth, node.k, value, node.id) <= 0) {
				//Lesser
				prev = node;
				node = node.lesser;
			} else {
				//Greater
				prev = node;
				node = node.greater;
			}
		}

		KdNode leaf = prev;

		if (leaf != null) {
			//Used to not re-examine nodes
			BitSet examined = new BitSet(points.length);

			//Go up the tree, looking for better solutions
			node = leaf;
			while (node != null) {
				//Search node
				searchNode(value, node, K, results, examined);

				node = node.parent;
			}

		}

		//Load up the collection of the results
		Collection<Integer> collection = new ArrayList<Integer>(K);

		for (Tuple t : results) {
			collection.add(t.id);
		}

		return collection;
	}

	private static final void searchNode(Integer value, KdNode node, int K, TreeSet<Tuple> results, BitSet examined) {
		examined.set(node.id);

		EuclideanDistance euclidean = new EuclideanDistance();

		//Search node
		Tuple lastNode = null;
		Double lastDistance = Double.MAX_VALUE;

		if (results.size() > 0) {
			lastNode = results.last();
			lastDistance = lastNode.distance;
		}

		double nodeDistance = euclidean.computeDistance(points[node.id], points[value]);

		if (nodeDistance < lastDistance) {
			if (results.size() == K && lastNode != null) results.remove(lastNode);
			results.add(new Tuple(node.id, nodeDistance));
		} else if (nodeDistance == lastDistance) {
			results.add(new Tuple(node.id, nodeDistance));
		} else if (results.size() < K) {
			results.add(new Tuple(node.id, nodeDistance));
		}

		lastNode = results.last();
		lastDistance = lastNode.distance;

		int axis = node.depth % node.k;

		KdNode lesser = node.lesser;
		KdNode greater = node.greater;

		//Search children branches, if axis aligned distance is less than current distance
		if (lesser != null && !examined.get(lesser.id)) {
			examined.set(lesser.id);

			double p1 = points[node.id][axis];
			double p2 = points[value][axis] - lastDistance;

			boolean lineIntersectsCube = ((p2 <= p1) ? true : false);

			//Continue down lesser branch
			if (lineIntersectsCube) searchNode(value, lesser, K, results, examined);
		}

		if (greater != null && !examined.get(greater.id)) {
			examined.set(greater.id);

			double p1 = points[node.id][axis];
			double p2 = points[value][axis] + lastDistance;

			boolean lineIntersectsCube = ((p2 >= p1) ? true : false);

			//Continue down greater branch
			if (lineIntersectsCube) searchNode(value, greater, K, results, examined);
		}
	}

	public Collection<Integer> range(double[] value, double eps) {
		if (value == null) return null;

		Collection<KdNode> results = new ArrayList<KdNode>();

		searchNodeRangeRecursive(value, eps, this.root, results);

		Collection<Integer> collection = new ArrayList<Integer>();

		for (KdNode kdNode : results) {
			collection.add(kdNode.id);
		}

		return collection;
	}

	public Collection<Integer> range(int value, double eps) {

		Collection<KdNode> results = new ArrayList<KdNode>();

		searchNodeRangeRecursive(points[value], eps, this.root, results);

		Collection<Integer> collection = new ArrayList<Integer>();

		for (KdNode kdNode : results) {
			if (kdNode.id != value) collection.add(kdNode.id);
		}

		return collection;
	}
	
	public boolean empty(double[] value, double eps) {
		
		Stack<KdNode> queue = new Stack<KdNode>();
		EuclideanDistance euclidean = new EuclideanDistance();
		
		queue.add(this.root);
		
		while (!queue.isEmpty()) {
			
			double axisDistance = -1;
			double rootDistance = euclidean.computeDistance(value, points[root.id]);

			int axis = root.depth % root.k;
			KdNode lesser = root.lesser;
			KdNode greater = root.greater;

			axisDistance = Math.abs(points[root.id][axis] - value[axis]);

			if (lesser == null || greater == null) {

				if (rootDistance <= eps && rootDistance != 0) {
					return false;
				}

			} else {

				if (axisDistance <= eps) {

					if (rootDistance <= eps && rootDistance != 0) {
						return false;
					}
					
					queue.add(lesser);
					queue.add(greater);
				} else {
					
					if(value[axis] > points[root.id][axis]) {
						queue.add(greater);
					} else if (value[axis] < points[root.id][axis]) {
						queue.add(lesser);
					} else if (value[axis] == points[root.id][axis]) {
						queue.add(lesser);
						queue.add(greater);
					}
				}
			}

		}
		
		return true;
	}
		
	public static final void searchNodeRangeRecursive(double[] value, double eps, KdNode root, Collection<KdNode> results) {

		EuclideanDistance euclidean = new EuclideanDistance();

		double axisDistance = -1;
		double rootDistance = euclidean.computeDistance(value, points[root.id]);

		int axis = root.depth % root.k;
		KdNode lesser = root.lesser;
		KdNode greater = root.greater;

		axisDistance = Math.abs(points[root.id][axis] - value[axis]);
				
		if (lesser == null && greater == null) {

			if (rootDistance < eps) {
				results.add(root);
			}

		} else {

			if (axisDistance <= eps) {
				
				if (rootDistance < eps) {
					results.add(root);
				}

				if (lesser  != null) searchNodeRangeRecursive(value, eps, lesser, results);
				if (greater != null) searchNodeRangeRecursive(value, eps, greater, results);

			} else {
				if(value[axis] > points[root.id][axis]) {
					if (greater != null) searchNodeRangeRecursive(value, eps, greater, results);
				} else if (value[axis] < points[root.id][axis]) {
					if (lesser  != null) searchNodeRangeRecursive(value, eps, lesser, results);
				} else if (value[axis] == points[root.id][axis]) {
					if (lesser  != null) searchNodeRangeRecursive(value, eps, lesser, results);
					if (greater != null) searchNodeRangeRecursive(value, eps, greater, results);					
				}
			}
		}
	}


	public boolean emptyLune(int a, int b, double eps) {
		Collection<Integer> neighborsA = range(points[a], eps);
		Collection<Integer> neighborsB = range(points[b], eps);
		
//		System.out.println(neighborsA);
//		
//		System.out.println(neighborsB);

		neighborsA.remove(b);
		neighborsB.remove(a);
		
		return Collections.disjoint(neighborsA, neighborsB);
	}


	public Collection<Integer> lune(int a, int b, double eps) {
		
		Collection<Integer> neighborsA = range(points[a], eps);
		Collection<Integer> neighborsB = range(points[b], eps);
		
//		System.out.println(neighborsA);
//		
//		System.out.println(neighborsB);

		neighborsA.addAll(neighborsB);
		
		return neighborsA;
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return TreePrinter.getString(this);
	}

	protected static class EuclideanComparator implements Comparator<KdNode> {

		private Integer point = null;


		public EuclideanComparator(Integer point) {
			this.point = point;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compare(KdNode o1, KdNode o2) {
			EuclideanDistance euclidean = new EuclideanDistance();

			Double d1 = euclidean.computeDistance(points[point], points[o1.id]);
			Double d2 = euclidean.computeDistance(points[point], points[o2.id]);

			if (d1.compareTo(d2)<0) return -1;
			else if (d2.compareTo(d1)<0) return 1;

			return compareTo(points[o1.id], points[o2.id]);
		}
	};

	protected static class TupleComparator implements Comparator<Tuple> {

		@Override
		public int compare(Tuple o1, Tuple o2) {

			if (o1.distance < o2.distance) return -1;
			else if (o1.distance > o2.distance) return 1;

			return compareTo(points[o1.id], points[o2.id]);
		}
	};

	public static class KdNode implements Comparable<KdNode> {

		public int k = 0;
		public int depth = 0;
		public Integer id = null;
		public KdNode parent = null;
		public KdNode lesser = null;
		public KdNode greater = null;

		public KdNode(Integer id) {
			this.id = id;
		}

		public KdNode(int k, int depth, Integer id) {
			this(id);
			this.k = k;
			this.depth = depth;
		}

		public static int compareTo(int depth, int k, Integer o1, Integer o2) {
			axis = depth % k;
			return COMPARATOR.compare(o1, o2);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj==null) return false;
			if (!(obj instanceof KdNode)) return false;

			KdNode kdNode = (KdNode) obj;
			if (this.compareTo(kdNode)==0) return true; 
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compareTo(KdNode o) {
			return compareTo(depth, k, this.id, o.id);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("k=").append(k);
			builder.append(" depth=").append(depth);
			builder.append(" id=").append(id.toString());
			return builder.toString();
		}
	}

	public static class Tuple {
		public int id;
		public double distance;

		public Tuple(int id, double distance) {
			super();
			this.id = id;
			this.distance = distance;
		}
	}

	public static class TreePrinter {

		public static String getString(KdTree tree) {
			if (tree.root == null) return "Tree has no nodes.";
			return getString(tree.root, "", true);
		}

		//T extends Comparable<T>
		private static <Type> String getString(KdNode node, String prefix, boolean isTail) {
			StringBuilder builder = new StringBuilder();

			if (node.parent!=null) {
				String side = "left";
				if (node.parent.greater!=null && node.id.equals(node.parent.greater.id)) side = "right";
				builder.append(prefix + (isTail ? "└── " : "├── ") + "[" + side + "] " + "depth=" + node.depth + " id=" + node.id + "\n");
			} else {
				builder.append(prefix + (isTail ? "└── " : "├── ") + "depth=" + node.depth + " id=" + node.id + "\n");
			}
			List<KdNode> children = null;
			if (node.lesser != null || node.greater != null) {
				children = new ArrayList<KdNode>(2);
				if (node.lesser != null) children.add(node.lesser);
				if (node.greater != null) children.add(node.greater);
			}
			if (children != null) {
				for (int i = 0; i < children.size() - 1; i++) {
					builder.append(getString(children.get(i), prefix + (isTail ? "    " : "│   "), false));
				}
				if (children.size() >= 1) {
					builder.append(getString(children.get(children.size() - 1), prefix + (isTail ? "    " : "│   "), true));
				}
			}

			return builder.toString();
		}
	}
}