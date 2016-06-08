package ca.ualberta.cs.util;

public class Data {

	public static Double[][] normalize(Double[][] dataSet){
		Double[][] result = new Double[dataSet.length][dataSet[0].length];
		
		for (int i = 0; i < dataSet.length; i++) {
			result[i] = normalize(dataSet[i]);
		}
		
		return result;
	}

	public static Double[] normalize(Double[] v) {
		Double[] n = new Double[v.length];
		double norm = Norm(v);
		
		if (norm != 0) {
			for (int i = 0; i < n.length; i++) {
				n[i] = v[i]/norm;
			}			
		} else {
			for (int i = 0; i < n.length; i++) {
				n[i] = 0.0;
			}
		}
		
		return n;
	}

	public static double Norm(Double[] v){
		double sum = 0;
		
		for (int i = 0; i < v.length; i++) {
			sum += v[i]*v[i];
		}
		
		return Math.sqrt(sum);
	}	
}
