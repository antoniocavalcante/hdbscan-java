/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ualberta.cs.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Computes the HAI for two hierarchies. It is an index that considers the smallest level in the hierarchy
 * at which two objects have the same labels and then counts the number of objects that have the same annotation as 
 * these objects.
 * August 17
 * @author Kriti
 */
public class HAI{
    
	
	/**
	 * Receives the path for a hierarchy file and stores it into an array of doubles.
	 * 
	 * @param file Hierarchy file.
	 * @return Array of double h.
	 */
	public static double[][] loadHierarchyFromFile(String file) {
		
		Path path = Paths.get(file);
		List<String> lines = null;
		
		// Reading all the lines in the hierarchy file.
		try {
			lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Array that will store the hierarchy read from file.
		double[][] h = new double[lines.size()][lines.get(0).split(",").length - 1];
		
		// Temporary array that will initially keep the lines read from file.
		String[][] s = new String[lines.size()][lines.get(0).split(",").length - 1];
		
		int index = 0;
		
		// Transforming the List of Strings into an Array of Strings;
		for (String l : lines) {
			s[index] = l.split(",");
			index++;
		}
		
		// Going through the array of Strings, converting each element into a double and storing it in the final array.
		for (int i = 0; i < s.length; i++) {
			for (int j = 0; j < s[0].length - 1; j++) {
				h[i][j] = Double.parseDouble(s[i][j]);
			}
		}
		
		// Removing reference to no longer needed object.
		s = null;
		
		return h;
	}
	
    public static double evaluate(double[][] h2, double[][] h1) {
        return compare(h1, h2);
    }
    
    public static double evaluate(String h1, String h2) {
    	return compare(loadHierarchyFromFile(h1), loadHierarchyFromFile(h2));
    }
    
    private static double compare(double[][] h1, double[][] h2) {
		
		double num_of_objects = h1[0].length - 1;

		double fact = 1/(num_of_objects * num_of_objects);
		
		double inter = 0; 
		double s1 = 0;
		double s2 = 0;
		
		
		for (int i = 1; i < h1[0].length; i++) {
			for (int j = 1; j < h2[0].length; j++) {

				if (i==j)
					continue;

				s1 = find(i,j, h1)/num_of_objects;
				s2 = find(i,j, h2)/num_of_objects;
					
				inter = inter + Math.abs(s1-s2);
			}
		}
		
		return (1 - (fact * inter));
	}

	public static double find(int i, int j, double[][] h) {
		
		for (int l = (h.length) - 1; l >= 0; l--) {
			
			if (h[l][i] != 0 && (h[l][i] == h[l][j])) {

				// not in the last row
				if (l != (h.length) - 1) {
					
					int c_no = (int) h[l][i];
					int count = 0;
					
					for (int k = 1; k < h[0].length; k++){
						if ((int) h[l][k] == c_no) count++;
					}

					return count;
					
				} else {
					return 0;
				}
			}
		}
		
		return 0;
	}
}
