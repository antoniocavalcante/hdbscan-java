package ca.ualberta.cs.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import ca.ualberta.cs.hdbscanstar.IncrementalHDBSCANStar;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

public class WSPD {

	public static final String WS = "WS";
	public static final String SS = "SS";
	
//	public static HashSet<SeparatedPair> pairs = new HashSet<SeparatedPair>();
    public static ChronicleMap<Integer, SeparatedPair> pairs;
    
    public static int count;
    
	public static void build(FairSplitTree T1, FairSplitTree T2, double s, String method){
		File file = new File("/tmp/pairs-" + System.nanoTime() + ".map");
	    file.deleteOnExit();
	    
	    ChronicleMapBuilder<Integer, SeparatedPair> builder = ChronicleMapBuilder.of(Integer.class, SeparatedPair.class).entries((long)(Math.pow(FairSplitTree.S.length, 2)));
	    builder.averageValueSize(8);
	    
	    count = 1;
	    
	    try {
			pairs = builder.createPersistedTo(file);
			System.out.println("AQUI");	
			WSPD.build(T1, T2, s, 0, method);
			
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void build(FairSplitTree T1, FairSplitTree T2, double s, int level, String method){
		
		// Check the case where both trees are distinct leafs.
		if (T1.isLeaf() && T2.isLeaf()) {
			// Output T1 and T2 as a WSP.

			// One pair will be added to the set, then the count has to be incremented.
			count++;

			if (T1.id != T2.id) {

				if (T1.id < T2.id) {
					pairs.put(count, new SeparatedPair(T1.id, T2.id));
				} else {
					pairs.put(count, new SeparatedPair(T2.id, T1.id));
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
			
			// One pair will be added to the set, then the count has to be incremented.
			count++;
			
			if (T1.id < T2.id) {
				pairs.put(count, new SeparatedPair(T1.id, T2.id));
			} else {
				pairs.put(count, new SeparatedPair(T2.id, T1.id));
			}

			return;
		}
		
		// Call recursion twice.
		build(T1.getLeft(), T2, s, level + 1, method);
		build(T1.getRight(), T2, s, level + 1, method);
//		if(WSPD.pairs.size() % 10000 == 0) System.out.println(WSPD.pairs.size());
	}
		
	public static boolean separated(FairSplitTree T1, FairSplitTree T2, double s, String method) {
		
		if (T1.id == T2.id) {
			return false;
		}
		
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
//		double d = FairSplitTree.boxDistance(T1, T2);
		
		double d = FairSplitTree.circleDistance(T1, T2);
		
		if (d >= s*Math.max(T1.diameter(), T2.diameter())) {
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
//		double d = FairSplitTree.boxDistance(T1, T2);

		double d = FairSplitTree.circleDistance(T1, T2);
		
		double min = Math.min(T1.diameter(), T2.diameter());
		
//		if (min == 0) min = Math.max(T1.diameter(), T2.diameter()); 
		
		if (d >= s*min) {
			return true;
		} else {
			return false;
		}
	}
}
