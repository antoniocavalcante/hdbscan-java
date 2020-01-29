/**
 * 
 */
package ca.ualberta.cs.hdbscanstar;

/**
 * @author toni
 *
 */
public class MutualReachabilityDistance {

	/**
	 * @param i
	 * @param j
	 * @param k
	 * @return
	 */
	public static double mutualReachabilityDistance(int i, int j, int k) {
		double mutualReachabiltiyDistance = Runner.environment.dataset.computeDistance(i, j);

		if (Runner.environment.coreDistances[i][k - 1] > mutualReachabiltiyDistance)
			mutualReachabiltiyDistance = Runner.environment.coreDistances[i][k - 1];

		if (Runner.environment.coreDistances[j][k - 1] > mutualReachabiltiyDistance)
			mutualReachabiltiyDistance = Runner.environment.coreDistances[j][k - 1];

		return mutualReachabiltiyDistance;
	}
}