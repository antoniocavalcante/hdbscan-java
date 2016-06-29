package ca.ualberta.cs.util;

import java.util.List;

public class QuickSelect {

	public static Double select(Double[][] arr, List<Integer> PUQ, int d) {

		int k = (int) arr.length/2 + 1;

		if (PUQ == null || PUQ.size() <= k)
			throw new Error();

		int[] indexes = new int[PUQ.size()];

		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = PUQ.get(i);
		}

		int from = 0, to = indexes.length - 1;

		// if from == to we reached the kth element
		while (from < to) {
			int r = from, w = to;
			double mid = arr[indexes[(r + w) / 2]][d];

			// stop if the reader and writer meets
			while (r < w) {

				if (arr[indexes[r]][d] >= mid) { // put the large values at the end
					int tmp = indexes[w];
					indexes[w] = indexes[r];
					indexes[r] = tmp;
					w--;
				} else { // the value is smaller than the pivot, skip
					r++;
				}
			}

			// if we stepped up (r++) we need to step one down
			if (arr[indexes[r]][d] > mid)
				r--;

			// the r pointer is on the end of the first k elements
			if (k <= r) {
				to = r;
			} else {
				from = r + 1;
			}
		}

		if (PUQ.size() % 2 == 0) {
			return (arr[indexes[k]][d] + arr[indexes[k-1]][d])/2;
		} else {
			return arr[indexes[k-1]][d];
		}	
	}
}