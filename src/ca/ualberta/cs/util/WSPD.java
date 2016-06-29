package ca.ualberta.cs.util;

import java.util.HashSet;

public class WSPD {

	public static final String WS = "WS";
	public static final String SS = "SS";
	
	public static HashSet<SeparatedPair> pairs = new HashSet<SeparatedPair>();
	
	public static void build(FairSplitTree T1, FairSplitTree T2, double s, String method){
		WSPD.build(T1, T2, s, 0, method);
	}
	
	public static void build(FairSplitTree T1, FairSplitTree T2, double s, int level, String method){
		
		// Check the case where both trees are distinct leafs.
		if (T1.isLeaf() && T2.isLeaf()) {
			// Output T1 and T2 as a WSP.
			
			if (T1.id != T2.id) {

				if (T1.id < T2.id) {
					pairs.add(new SeparatedPair(T1, T2));
				} else {
					pairs.add(new SeparatedPair(T2, T1));
				}
			}
			
			return;
		}

		// Check the diameters and swap.
		if (T1.diameter() < T2.diameter()) {
			FairSplitTree tmp = T2;
			T2 = T1;
			T1 = tmp;
		}

		// Check if T1 and T2 are well-separated.
		if (separated(T1, T2, s, method)) {

			if (T1.id < T2.id) {
				pairs.add(new SeparatedPair(T1, T2));
			} else {
				pairs.add(new SeparatedPair(T2, T1));
			}

			return;
		}

		// Call recursion twice.
		build(T1.getLeft(), T2, s, level + 1, method);
		build(T1.getRight(), T2, s, level + 1, method);
		
	}

	public static boolean separated(FairSplitTree T1, FairSplitTree T2, double s, String method) {
		if (method == SS) {
			return ss(T1, T2, s);
		} else {
			return ws(T1, T2, s);
		}		
	}
	
	/**
	 * Receives two Fair Split Trees T1 and T2 and returns whether they are
	 * well separated or not.
	 * 
	 * @param T1 FairSplitTree #1.
	 * @param T2 FairSplitTree #2.
	 * @param s Separation factor.
	 * @return true if T1 and T2 are well separated, false otherwise. 
	 */
	public static boolean ws(FairSplitTree T1, FairSplitTree T2, double s){

		if (FairSplitTree.boxDistance(T1, T2) >= s*Math.max(T1.diameter(), T2.diameter())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Receives two Fair Split Trees T1 and T2 and returns whether they are
	 * semi-separated or not.
	 * 
	 * @param T1 FairSplitTree #1.
	 * @param T2 FairSplitTree #2.
	 * @param s Separation factor.
	 * @return true if T1 and T2 are well separated, false otherwise. 
	 */
	public static boolean ss(FairSplitTree T1, FairSplitTree T2, double s){

		if (FairSplitTree.boxDistance(T1, T2) > s*Math.min(T1.diameter(), T2.diameter())) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void findPairs(FairSplitTree T1, FairSplitTree T2, double s) {
		
		if (ws(T1, T2, s)) {

			if (T1.id < T2.id) {
				pairs.add(new SeparatedPair(T1, T2));
			} else {
				pairs.add(new SeparatedPair(T2, T1));
			}

			return;

		} else {
			if (T1.diameter() <= T2.diameter()) {
				findPairs(T1, T2.getLeft(), s);
				findPairs(T1, T2.getRight(), s);
			} else {
				findPairs(T1.getLeft(), T2, s);
				findPairs(T1.getRight(), T2, s);
			}
		}
	}
	
	public static void findWSPD(FairSplitTree T, double s) {
		System.out.println("Calling for " + T.getLeft().id + " and " + T.getRight().id);
		findPairs(T.getLeft(), T.getRight(), s);

		if (!T.getLeft().isLeaf() && !T.getRight().isLeaf()) {
			findWSPD(T.getLeft(), s);
			findWSPD(T.getRight(), s);
		}
	}
}
