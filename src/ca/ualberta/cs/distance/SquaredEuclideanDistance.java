/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ualberta.cs.distance;

/**
 *
 * @author Kriti
 */
public class SquaredEuclideanDistance implements DistanceCalculator{
    public SquaredEuclideanDistance() {
	}

	// ------------------------------ PUBLIC METHODS ------------------------------
	
	public double computeDistance(Double[] attributesOne, Double[] attributesTwo) {
		double distance = 0;
		
		for (int i = 0; i < attributesOne.length && i < attributesTwo.length; i++) {
			distance+= ((attributesOne[i] - attributesTwo[i]) * (attributesOne[i] - attributesTwo[i]));
		}
		
		return distance;
	}
	
	
	public String getName() {
		return "sq-euclidean";
	}
}
