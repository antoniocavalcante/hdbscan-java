package ca.ualberta.cs.test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.openhft.chronicle.map.ChronicleMap;


public class MapDBTest {

	public static void main(String[] args) {

		ChronicleMap<Integer, Integer> map = null;
		
		try {
			map = ChronicleMap
				    .of(Integer.class, Integer.class)
				    .name("city-postal-codes-map")
				    .entries(10000000)
				    .createPersistedTo(new File("file.db"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ConcurrentMap<Integer,Integer> map2 = new ConcurrentHashMap<Integer, Integer>(10000000);		
		
		long start = System.currentTimeMillis();

		for (int i = 0; i < 10000000; i++) {
			map.put(i, i);
			map2.put(i,i);
		}
		System.out.println("finished");
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("Partial Time: " + (System.currentTimeMillis() - start));

		
		System.out.println("Total Time: " + (System.currentTimeMillis() - start));
		
	}

}
