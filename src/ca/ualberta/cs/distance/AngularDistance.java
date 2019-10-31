package ca.ualberta.cs.distance;

public class AngularDistance implements DistanceCalculator {

	@Override
	public double computeDistance(double[] attributesOne, double[] attributesTwo) {
		return Math.acos(1 - (new CosineSimilarity().computeDistance(attributesOne, attributesTwo)))/Math.PI;
	}

	@Override
	public String getName() {
		return "angular";
	}

}
