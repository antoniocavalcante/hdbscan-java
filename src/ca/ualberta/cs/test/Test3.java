package ca.ualberta.cs.test;

import java.io.IOException;
import java.util.ArrayList;

import com.sun.xml.internal.ws.config.metro.util.ParserUtil;

import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.hdbscanstar.HDBSCANStar;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDList;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDListIter;
import de.lmu.ifi.dbs.elki.database.ids.KNNList;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.query.range.RangeQuery;
import de.lmu.ifi.dbs.elki.database.relation.DBIDView;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.ArrayAdapterDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.parser.NumberVectorLabelParser;
import de.lmu.ifi.dbs.elki.datasource.parser.Parser;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.index.vafile.VAFile;
import de.lmu.ifi.dbs.elki.index.vafile.VAFile.Factory;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

public class Test3 {

	//	static String dataset = "/home/toni/git/HDBSCAN_Star/hierarchical-3d2d1d.csv";
//	static String dataset = "/home/toni/git/HDBSCAN_Star/abc.data";
	static String dataset = "/home/toni/git/HDBSCAN_Star/experiments/data#6/2d-1024.dat";
	
	static double[][] data = null;
	
	// query point
	static double[] querypoint = new double[] {0.5, 0.5};

	static double eps = 160;

	public static void main(String[] args) {
		
		try {
			data = HDBSCANStar.readInDataSet(dataset, " ");
		}
		catch (IOException ioe) {
			System.err.println("Error reading input data set file.");
			System.exit(-1);
		}

		long startVA = System.currentTimeMillis();
		vaquery();
//		System.out.println("VA 1: " + (System.currentTimeMillis() - startVA));
		
		startVA = System.currentTimeMillis();
		vaquery2(data);
//		System.out.println("VA 2: " + (System.currentTimeMillis() - startVA));
		
		long startNaive = System.currentTimeMillis();
		naivequery(data);
		System.out.println("Naive: " + (System.currentTimeMillis() - startNaive));

	}
	
	public static void vaquery2(double[][] data){
		DatabaseConnection dbc = new ArrayAdapterDatabaseConnection(data);
		
		Database db = new StaticArrayDatabase(dbc, null);

		db.initialize();

		Relation<DoubleVector> relation = db.getRelation(TypeUtil.DOUBLE_VECTOR_FIELD);

		VAFile<DoubleVector> vafile = new VAFile<DoubleVector>(1024, relation, 32);
		
		vafile.initialize();
	
		DistanceQuery<DoubleVector> dq = db.getDistanceQuery(relation, EuclideanDistanceFunction.STATIC);
		
		RangeQuery<DoubleVector> knn = vafile.getRangeQuery(dq);
		
		DoubleVector q = new DoubleVector(querypoint);
		long start = System.currentTimeMillis();
		
		knn.getRangeForObject(q, eps);
		
		System.out.println("VA #2: " + (System.currentTimeMillis() - start));
	}

	public static void vaquery(){
		// VA-File Parameters
		ListParameterization spatparams = new ListParameterization();
		spatparams.addParameter(StaticArrayDatabase.Parameterizer.INDEX_ID, VAFile.Factory.class);
		spatparams.addParameter(VAFile.Factory.PARTITIONS_ID, 4);

		spatparams.addParameter(FileBasedDatabaseConnection.Parameterizer.INPUT_ID, dataset);

		// Initialize VA-File
		Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, spatparams);
		db.initialize();
		
		Relation<DoubleVector> rep = db.getRelation(TypeUtil.DOUBLE_VECTOR_FIELD);

		DistanceQuery<DoubleVector> dist = db.getDistanceQuery(rep, EuclideanDistanceFunction.STATIC);
		
		// Do a range query
		DoubleVector q = new DoubleVector(querypoint);
		
		RangeQuery<DoubleVector> rangeq = db.getRangeQuery(dist, eps);

		long start1 = System.currentTimeMillis();
		DoubleDBIDList ids = rangeq.getRangeForObject(q, eps);
		System.out.println("VA #1: " + (System.currentTimeMillis() - start1));

//		System.out.println(ids.size());
	}

	
	public static void naivequery(double[][] data){
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		for (int i = 0; i < data.length; i++) {
			if ((new EuclideanDistance()).computeDistance(querypoint, data[i]) < eps) {
				result.add(i);
			}
		}
		
//		System.out.println(result.size());
	}
}