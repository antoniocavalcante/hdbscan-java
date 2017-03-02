package ca.ualberta.cs.main;

import java.util.BitSet;

import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.hdbscanstar.RelativeNeighborhoodGraph;
import ca.ualberta.cs.hdbscanstar.UndirectedGraph;

public class Prim {
	
	public static int minKey(double key[], BitSet mstSet) {
        // Initialize min value
        double min = Double.MAX_VALUE;
        
        int min_index = key.length;
 
        for (int v = 0; v < key.length; v++)
            if (mstSet.get(v) == false && key[v] < min) {
                min = key[v];
                min_index = v;
            }

        return min_index;
    }
	
	public static UndirectedGraph constructMST(double[][] dataSet, double[][] coreDistances, int minPoints,	boolean selfEdges, DistanceCalculator distanceFunction, RelativeNeighborhoodGraph RNG) {

		minPoints--;
		int selfEdgeCapacity = 0;
		if (selfEdges)
			selfEdgeCapacity = dataSet.length;

		//One bit is set (true) for each attached point, or unset (false) for unattached points:
		BitSet attachedPoints = new BitSet(dataSet.length);

		//Each point has a current neighbor point in the tree, and a current nearest distance:
		int[] nearestMRDNeighbors = new int[dataSet.length-1 + selfEdgeCapacity];
		double[] nearestMRDDistances = new double[dataSet.length-1 + selfEdgeCapacity];

		for (int i = 0; i < dataSet.length-1; i++) {
			nearestMRDDistances[i] = Double.MAX_VALUE;
		}

		//The MST is expanded starting with the last point in the data set:
		int numAttachedPoints = 0;

//		nearestMRDDistances[dataSet.length-1] = 0;
//		nearestMRDNeighbors[dataSet.length-1] = -1;
		
		//Continue attaching points to the MST until all points are attached:
		while (numAttachedPoints < dataSet.length) {

			double nearestMRDDistance = Double.MAX_VALUE;

			int currentPoint = minKey(nearestMRDDistances, attachedPoints);
			
			//Attach the closest point found in this iteration to the tree:			
			attachedPoints.set(currentPoint);
			
			numAttachedPoints++;
			
			//Iterate through all unattached points, updating distances using the current point:
			for (int neighbor : RNG.RNG[currentPoint]) {
				if (currentPoint == neighbor)
					continue;
				if (attachedPoints.get(neighbor) == true)
					continue;

				double distance = distanceFunction.computeDistance(dataSet[currentPoint], dataSet[neighbor]);

				double mutualReachabiltiyDistance = distance;
				if (coreDistances[currentPoint][minPoints] > mutualReachabiltiyDistance)
					mutualReachabiltiyDistance = coreDistances[currentPoint][minPoints];
				if (coreDistances[neighbor][minPoints] > mutualReachabiltiyDistance)
					mutualReachabiltiyDistance = coreDistances[neighbor][minPoints];

				if (mutualReachabiltiyDistance < nearestMRDDistances[neighbor]) {
					nearestMRDDistances[neighbor] = mutualReachabiltiyDistance;
					nearestMRDNeighbors[neighbor] = currentPoint;
				}

				//Check if the unattached point being updated is the closest to the tree:
				if (nearestMRDDistances[neighbor] <= nearestMRDDistance) {
					nearestMRDDistance = nearestMRDDistances[neighbor];
//					nearestMRDPoint = neighbor;
				}
			}

//			attachedPoints.set(nearestMRDPoint);
//			numAttachedPoints++;
//			currentPoint = minKey(nearestMRDDistances, attachedPoints);
		}

		//Create an array for vertices in the tree that each point attached to:
		int[] otherVertexIndices = new int[dataSet.length-1 + selfEdgeCapacity];
		for (int i = 0; i < dataSet.length-1; i++) {
			otherVertexIndices[i] = i;
		}

		//If necessary, attach self edges:
		if (selfEdges) {
			for (int i = dataSet.length-1; i < dataSet.length*2-1; i++) {
				int vertex = i - (dataSet.length-1);
				nearestMRDNeighbors[i] = vertex;
				otherVertexIndices[i] = vertex;
				nearestMRDDistances[i] = coreDistances[vertex][minPoints];
			}
		}

		return new UndirectedGraph(dataSet.length, nearestMRDNeighbors, otherVertexIndices, nearestMRDDistances);
	}


}
