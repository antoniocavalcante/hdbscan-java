package ca.ualberta.cs.hdbscanstar;

import java.util.ArrayList;

import ca.ualberta.cs.SHM.HMatrix.HMatrix;
import ca.ualberta.cs.distance.AngularDistance;
import ca.ualberta.cs.distance.CosineSimilarity;
import ca.ualberta.cs.distance.DistanceCalculator;
import ca.ualberta.cs.distance.EuclideanDistance;
import ca.ualberta.cs.distance.ManhattanDistance;
import ca.ualberta.cs.distance.PearsonCorrelation;
import ca.ualberta.cs.distance.SquaredEuclideanDistance;
import ca.ualberta.cs.distance.SupremumDistance;
import ca.ualberta.cs.util.Dataset;

public class Runner {

	protected static final String FILE_FLAG = "file=";
	protected static final String CONSTRAINTS_FLAG = "constraints=";
	protected static final String MIN_PTS_FLAG = "minPts=";
	protected static final String MIN_CL_SIZE_FLAG = "minClSize=";
	protected static final String COMPACT_FLAG = "compact=";
	protected static final String DISTANCE_FUNCTION_FLAG = "dist_function=";
	protected static final String OUT_TYPE_FLAG = "outputExtension=";
	protected static final String OUT_FLAG = "output=";
	protected static final String RNG_FILTER_FLAG = "filter=";

	protected static final String INDEX_FLAG = "index=";
	protected static final String INCREMENTAL_FLAG = "incremental=";

	protected static final String SPARSE_FLAG = "sparse=";

	protected static final String SEPARATOR_FLAG = "separator=";

	public static final String SHM_OUT = "shm";
	public static final String VIS_OUT = "vis";
	public static final String TXT_OUT = "vis";
	public static final String DEFAULT_OUT = "default";	//See checkInputParameters method for the default value
	public static final String BOTH_OUT = "both";

	public static final String RNG_FILTER_SMART = "smart";
	public static final String RNG_FILTER_NAIVE = "naive";
	public static final String RNG_FILTER_BOTH = "both";
	public static final String RNG_FILTER_NONE = "none";

	protected static final String EUCLIDEAN_DISTANCE = "euclidean";
	protected static final String COSINE_SIMILARITY = "cosine";
	protected static final String PEARSON_CORRELATION = "pearson";
	protected static final String MANHATTAN_DISTANCE = "manhattan";
	protected static final String SUPREMUM_DISTANCE = "supremum";
	protected static final String ANGULAR_DISTANCE = "angular";
	protected static final String SQUARE_EUCLIDEAN_DISANCE = "sqeuclidean";

	public static HDBSCANStarParameters parameters = null;
	public static Environment environment = null;

	
	/**
	 * Prints a help message that explains the usage of HDBSCANStarRunner, and then exits the program.
	 */
	protected static void printHelpMessageAndExit() {
		System.out.println();

		System.out.println("Executes the HDBSCAN* algorithm, which produces a hierarchy, cluster tree, " +
				"flat partitioning, and outlier scores for an input data set.");
		System.out.println("Usage: java -jar HDBSCANStar.jar file=<input file> minPts=<minPts value> " + 
				"minClSize=<minClSize value> [constraints=<constraints file>] [compact={true,false}] " + 
				"[dist_function=<distance function>]" +
				"[outputExtension={both, shm, csv}]");
		System.out.println("By default the hierarchy produced is non-compact (full), and euclidean distance is used.");
		System.out.println("Example usage: \"java -jar HDBSCANStar.jar file=input.csv minPts=4 minClSize=4\"");
		System.out.println("Example usage: \"java -jar HDBSCANStar.jar file=collection.csv minPts=6 minClSize=1 " + 
				"constraints=collection_constraints.csv dist_function=manhattan\"");
		System.out.println("Example usage: \"java -jar HDBSCANStar.jar file=data_set.csv minPts=8 minClSize=8 " + 
				"compact=true\"");
		System.out.println("In cases where the source is compiled, use the following: \"java HDBSCANStarRunner " +
				"file=data_set.csv minPts=8 minClSize=8 compact=true\"");
		System.out.println();

		System.out.println("The input data set file must be a comma-separated value (CSV) file, where each line " +
				"represents an object, with attributes separated by commas.");
		System.out.println("The algorithm will produce seven files: the hierarchy, cluster tree, final flat partitioning, outlier scores, and auxiliary files for visualization (.cl3, .vis and .mst).");
		System.out.println();

		System.out.println("The hierarchy file will be named <input>_hierarchy.csv for a non-compact " + 
				"(full) hierarchy, and <input>_compact_hierarchy.csv for a compact hierarchy.");
		System.out.println("The hierarchy file will have the following format on each line:");
		System.out.println("<hierarchy scale (epsilon radius)>,<label for object 1>,<label for object 2>,...,<label for object n>");
		System.out.println("Noise objects are labelled zero.");
		System.out.println();

		System.out.println("The cluster tree file will be named <input>_tree.csv");
		System.out.println("The cluster tree file will have the following format on each line:");
		System.out.println("<cluster label>,<birth level>,<death level>,<stability>,<gamma>," + 
				"<virtual child cluster gamma>,<character_offset>,<parent>");
		System.out.println("<character_offset> is the character offset of the line in the hierarchy " + 
				"file at which the cluster first appears.");
		System.out.println();

		System.out.println("The final flat partitioning file will be named <input>_partition.csv");
		System.out.println("The final flat partitioning file will have the following format on a single line:");
		System.out.println("<label for object 1>,<label for object 2>,...,<label for object n>");
		System.out.println();

		System.out.println("The outlier scores file will be named <input>_outlier_scores.csv");
		System.out.println("The outlier scores file will be sorted from 'most inlier' to 'most outlier', " + 
				"and will have the following format on each line:");
		System.out.println("<outlier score>,<object id>");
		System.out.println("<object id> is the zero-indexed line on which the object appeared in the input file.");
		System.out.println();

		System.out.println("The auxiliary visualization file will be named <input>_visulization.vis and will be the file you must open at the visualization tool.");
		System.out.println("The auxiliary cluster tree file will be named <input>_clusterTree.cl3.");
		System.out.println("The auxiliary minimum spanning tree file will be named <input>_MST.mst.");
		System.out.println("These files are only used by the visualization module and its algortihms.");
		System.out.println();

		System.out.println("The optional input constraints file can be used to provide constraints for " + 
				"the algorithm (semi-supervised flat partitioning extraction).");
		System.out.println("If this file is not given, only stability will be used to selected the " + 
				"most prominent clusters (unsupervised flat partitioning extraction).");
		System.out.println("This file must be a comma-separated value (CSV) file, where each line " +
				"represents a constraint, with the two zero-indexed objects and type of constraint " +
				"separated by commas.");
		System.out.println("Use 'ml' to specify a must-link constraint, and 'cl' to specify a cannot-link constraint.");
		System.out.println();

		System.out.println("The optional compact flag can be used to specify if the hierarchy saved to file " +
				"should be the full or the compact one (this does not affect the final partitioning or cluster tree).");
		System.out.println("The full hierarchy includes all levels where objects change clusters or " + 
				"become noise, while the compact hierarchy only includes levels where clusters are born or die.");
		System.out.println();

		System.out.println("Possible values for the optional dist_function flag are:");
		System.out.println("euclidean: Euclidean Distance, d = sqrt((x1-y1)^2 + (x2-y2)^2 + ... + (xn-yn)^2)");
		System.out.println("cosine: Cosine Similarity, d = 1 - ((X.Y) / (||X||*||Y||))");
		System.out.println("pearson: Pearson Correlation, d = 1 - (cov(X,Y) / (std_dev(X) * std_dev(Y)))");
		System.out.println("manhattan: Manhattan Distance, d = |x1-y1| + |x2-y2| + ... + |xn-yn|");
		System.out.println("supremum: Supremum Distance, d = max[(x1-y1), (x2-y2), ... ,(xn-yn)]");
		System.out.println();

		System.exit(0);
	}
	
	/**
	 * Parses out the input parameters from the program arguments.  Prints out a help message and
	 * exits the program if the parameters are incorrect.
	 * @param args The input arguments for the program
	 * @return Input parameters for HDBSCAN*
	 */
	public static HDBSCANStarParameters checkInputParameters(String[] args, HMatrix HMatrix) {
		HDBSCANStarParameters parameters = new HDBSCANStarParameters();

		//Read in the input arguments and assign them to variables:
		for (String argument : args) {

			// Assign input file:
			if (argument.startsWith(FILE_FLAG) && argument.length() > FILE_FLAG.length())
				parameters.inputFile = argument.substring(FILE_FLAG.length());

			// Assign constraints file:
			if (argument.startsWith(CONSTRAINTS_FLAG) && argument.length() > CONSTRAINTS_FLAG.length())
				parameters.constraintsFile = argument.substring(CONSTRAINTS_FLAG.length());

			// Assign minPoints:
			else if (argument.startsWith(MIN_PTS_FLAG) && argument.length() > MIN_PTS_FLAG.length()) {
				try {
					parameters.minPoints = Integer.parseInt(argument.substring(MIN_PTS_FLAG.length()));
					HMatrix.setParam_minPts(parameters.minPoints);
				}
				catch (NumberFormatException nfe) {
					System.out.println("Illegal value for minPts.");
				}
			}

			// Assign minClusterSize:
			else if (argument.startsWith(MIN_CL_SIZE_FLAG) && argument.length() > MIN_CL_SIZE_FLAG.length()) {
				try {
					parameters.minClusterSize = Integer.parseInt(argument.substring(MIN_CL_SIZE_FLAG.length()));
					HMatrix.setParam_minClSize(parameters.minClusterSize);
				}
				catch (NumberFormatException nfe) {
					System.out.println("Illegal value for minClSize.");
				}
			}

			// Assign compact hierarchy:
			else if (argument.startsWith(COMPACT_FLAG) && argument.length() > COMPACT_FLAG.length()) {
				parameters.compactHierarchy = Boolean.parseBoolean(argument.substring(COMPACT_FLAG.length()));
				HMatrix.setIsCompact(parameters.compactHierarchy);
			}

			// Assign distance function:
			else if (argument.startsWith(DISTANCE_FUNCTION_FLAG) && argument.length() > DISTANCE_FUNCTION_FLAG.length()) {
				String functionName = argument.substring(DISTANCE_FUNCTION_FLAG.length());

				if (functionName.equals(EUCLIDEAN_DISTANCE))
				{
					parameters.distanceFunction = new EuclideanDistance();
					HMatrix.setParam_distanceFunction(EUCLIDEAN_DISTANCE);
				}
				else if (functionName.equals(COSINE_SIMILARITY))
				{
					parameters.distanceFunction = new CosineSimilarity();
					HMatrix.setParam_distanceFunction(COSINE_SIMILARITY);
				}
				else if (functionName.equals(PEARSON_CORRELATION))
				{
					parameters.distanceFunction = new PearsonCorrelation();
					HMatrix.setParam_distanceFunction(PEARSON_CORRELATION);
				}
				else if (functionName.equals(MANHATTAN_DISTANCE))
				{
					parameters.distanceFunction = new ManhattanDistance();
					HMatrix.setParam_distanceFunction(MANHATTAN_DISTANCE);
				}
				else if (functionName.equals(SUPREMUM_DISTANCE))
				{
					parameters.distanceFunction = new SupremumDistance();
					HMatrix.setParam_distanceFunction(SUPREMUM_DISTANCE);
				}
				else if (functionName.equals(ANGULAR_DISTANCE))
				{
					parameters.distanceFunction = new AngularDistance();
					HMatrix.setParam_distanceFunction(ANGULAR_DISTANCE);
				} 
				else if (functionName.equals(SQUARE_EUCLIDEAN_DISANCE))
				{
					parameters.distanceFunction = new SquaredEuclideanDistance();
					HMatrix.setParam_distanceFunction(SQUARE_EUCLIDEAN_DISANCE);
				}
				else
					parameters.distanceFunction = null;

			}

			// Assign output type file:
			else if (argument.startsWith(OUT_TYPE_FLAG) && argument.length() > OUT_TYPE_FLAG.length()) {		
				String outType = argument.substring(OUT_TYPE_FLAG.length());

				switch (outType) {
				//for now, the option to generate only the .shm file is unavailable
				case DEFAULT_OUT:
					parameters.outType = BOTH_OUT;
					break;
				case SHM_OUT:
					parameters.outType = SHM_OUT;
					break;
				case VIS_OUT:
					parameters.outType = VIS_OUT;
					break;
				default:
					parameters.outType = BOTH_OUT;
					break;
				}
			}

			//Assign if the output files will be computed or not.
			else if (argument.startsWith(OUT_FLAG) && argument.length() > OUT_FLAG.length()) {
				parameters.outputFiles = Boolean.parseBoolean(argument.substring(OUT_FLAG.length()));
			}

			//Assign the type of filter of the RNG.
			else if (argument.startsWith(RNG_FILTER_FLAG) && argument.length() > RNG_FILTER_FLAG.length()) {
				String rngFilter = argument.substring(RNG_FILTER_FLAG.length());				

				switch (rngFilter) {
				//for now, the option to generate only the .shm file is unavailable
				case RNG_FILTER_BOTH:
					parameters.RNGNaive = true;
					parameters.RNGSmart = true;
					break;
				case RNG_FILTER_NAIVE:
					parameters.RNGNaive = true;
					parameters.RNGSmart = false;
					break;
				case RNG_FILTER_SMART:
					parameters.RNGNaive = false;
					parameters.RNGSmart = true;
					break;
				case RNG_FILTER_NONE:
					parameters.RNGNaive = false;
					parameters.RNGSmart = false;
					break;					
				default:
					parameters.RNGNaive = false;
					parameters.RNGSmart = true;
					break;
				}
			}

			//Assign if an index will be used or not.
			else if (argument.startsWith(INDEX_FLAG) && argument.length() > INDEX_FLAG.length()) {
				parameters.index = Boolean.parseBoolean(argument.substring(INDEX_FLAG.length()));
			}

			//Assign if the RNG filter is incremental or not.
			else if (argument.startsWith(INCREMENTAL_FLAG) && argument.length() > INCREMENTAL_FLAG.length()) {
				parameters.RNGIncremental = Boolean.parseBoolean(argument.substring(INCREMENTAL_FLAG.length()));
			}

			//Assign if the data is sparse or not.
			else if (argument.startsWith(SPARSE_FLAG) && argument.length() > SPARSE_FLAG.length()) {
				parameters.sparse = Boolean.parseBoolean(argument.substring(SPARSE_FLAG.length()));
			}

			//Assign a custom separator.
			else if (argument.startsWith(SEPARATOR_FLAG) && argument.length() > SEPARATOR_FLAG.length()) {
				parameters.separator = argument.substring(SEPARATOR_FLAG.length());
			}
		}

		//Check that each input parameter has been assigned:
		if (parameters.inputFile == null) {
			System.out.println("Missing input file name.");
			printHelpMessageAndExit();
		}
		else if (parameters.minPoints == null) {
			System.out.println("Missing value for minPts.");
			printHelpMessageAndExit();
		}
		else if (parameters.minClusterSize == null) {
			System.out.println("Missing value for minClSize");
			printHelpMessageAndExit();
		}
		else if (parameters.distanceFunction == null) {
			System.out.println("Missing distance function.");
			printHelpMessageAndExit();
		}
		else if (parameters.separator == null) {
			System.out.println("Missing separator.");
			printHelpMessageAndExit();
		}

		//Generate names for output files:
		String inputName = parameters.inputFile;
		if (parameters.inputFile.contains("."))
			inputName = parameters.inputFile.substring(0, parameters.inputFile.lastIndexOf("."));

		if (parameters.compactHierarchy)
			parameters.hierarchyFile = inputName + "_compact_hierarchy.csv";
		else
			parameters.hierarchyFile = inputName + "_hierarchy.csv";

		parameters.clusterTreeFile = inputName + "_tree.csv";
		parameters.partitionFile = inputName + "_partition.csv";
		parameters.outlierScoreFile = inputName + "_outlier_scores.csv";
		parameters.visualizationFile = inputName + "_visualization.vis";
		parameters.clusterTreeSerializableFile = inputName + "_clusterTree.cl3";
		parameters.MSTSerializableFile = inputName + "_MST.mst";
		parameters.shmFile = inputName + ".shm";

		return parameters;
	}
	
	/**
	 * Simple class for storing input parameters.
	 */
	public static class HDBSCANStarParameters {
		public String inputFile;
		public String constraintsFile;
		public Integer minPoints;
		public Integer minClusterSize;
		public boolean compactHierarchy;
		public DistanceCalculator distanceFunction;

		public String outType;
		public String shmFile;
		public String hierarchyFile;
		public String clusterTreeFile;
		public String partitionFile;
		public String outlierScoreFile;
		public String visualizationFile;
		public String clusterTreeSerializableFile;
		public String MSTSerializableFile;

		public boolean outputFiles;

		public boolean RNGNaive;
		public boolean RNGSmart;
		public boolean RNGIncremental;
		public boolean index;

		public boolean sparse;

		public String separator;
	}

	public static class Environment {
		public Dataset dataset;
		public double[][] coreDistances;
		public int[][] kNN;

		public ArrayList<Constraint> constraints;

		public int[] mpts;
	}

	public static class WrapInt{
		public int value;

		public WrapInt(int value) {
			this.value = value;
		}

		public void inc() {
			this.value++;
		}

		public void setValue(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
	}
}
