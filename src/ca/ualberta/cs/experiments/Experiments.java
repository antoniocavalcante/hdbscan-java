package ca.ualberta.cs.experiments;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Experiments {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fileName = "/home/toni/git/HDBSCAN_Star/results/incremental_hdbscan_star_results";
		List<String> list = new ArrayList<>();
		List<String> compiled = new ArrayList<>();

		try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {

			//br returns as stream and convert it into a List
			list = br.lines().collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}
	
		int count = 1;
		long time = 0;
		
		for (String line : list) {
			String[] tmp = line.split(", ");
			String dataset = tmp[0];
			int minPoints = Integer.parseInt(tmp[1]);
			time = time + Integer.parseInt(tmp[2]);
			count++;
			
			if (count == 11) {
				time = time/10;
				compiled.add(dataset + ", " + minPoints + ", " + time);
				time = 0;
				count = 1;
			}
		}
		
		try {
			Files.write(Paths.get("incremental_hdbscan_avg"), compiled);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
