package ca.ualberta.cs.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

public class Combinations {
	private static void getSubsets(List<Double> superSet, int k, int idx, Set<Double> current,List<Set<Double>> solution) {
		//successful stop clauseInteger
		if (current.size() == k) {
			solution.add(new HashSet<>(current));
			return;
		}
		//unseccessful stop clause
		if (idx == superSet.size()) return;
		Double x = superSet.get(idx);
		current.add(x);
		//"guess" x is in the subset
		getSubsets(superSet, k, idx+1, current, solution);
		current.remove(x);
		//"guess" x is not in the subset
		getSubsets(superSet, k, idx+1, current, solution);
	}

	public static List<Set<Double>> getSubsets(List<Double> superSet, int k) {
		List<Set<Double>> res = new ArrayList<>();
		getSubsets(superSet, k, 0, new HashSet<Double>(), res);

		return res;
	}

	public static Double[][] permutation(Double[] P, int d) {
		ICombinatoricsVector<Double> originalVector = Factory.createVector(P);
		Generator<Double> gen = Factory.createPermutationWithRepetitionGenerator(originalVector, d);
		
		Double[][] result = new Double[(int)gen.getNumberOfGeneratedObjects()][d];
		int i = 0;
		for (ICombinatoricsVector<Double> perm : gen){
			result[i] = perm.getVector().toArray(result[i]);
			i++;
		}
		
		return result;
	}

}
