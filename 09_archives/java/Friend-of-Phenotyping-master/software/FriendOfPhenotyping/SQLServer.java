/**
* @author Guillaume Lobet | Université de Liège
* @date: 2013-07-16
* 
* This class manage the different SQL connection parameters 
* 
**/
public class SQLServer {

	
	private static final String SQL_HOST = "localhost";
	private static final String SQL_DATABASE = "mars";
	private static final String SQL_PASSWORD = "";
	private static final String SQL_USERNAME = "root";
	private static final String SQL_CONNECTION = "jdbc:mysql://localhost/mars";
	private static final String SQL_DRIVER = "com.mysql.jdbc.Driver";
	private static final String SQL_TABLE_ROSETTE = "rosette_size";
	private static final String SQL_TABLE_LEAF = "leaf_size";
	private static final String SQL_TABLE_PLANT = "plant";
	private static final String SQL_TABLE_SEED = "seed_size";
	private static final String SQL_TABLE_STOCK = "stock";
	private static final String SQL_TABLE_TREATMENT = "treatment";
	private static final String SQL_TABLE_EXPERIMENT = "experiment";
	
	public static String sqlHost = "localhost";
	public static String sqlDatabase = "mars";
	public static String sqlPassword = "";
	public static String sqlUsername = "root";
	public static String sqlConnection = "jdbc:mysql://localhost/mars";
	public static String sqlDriver = "com.mysql.jdbc.Driver";
	public static String sqlTableRosette = "rosette_size";
	public static String sqlTableLeaf = "leaves_size";
	public static String sqlTablePlant = "plant";
	public static String sqlTableSeed = "seed_size";
	public static String sqlTableStock = "stock";
	public static String sqlTableTreatment = "treatment";
	public static String sqlTableExperiment = "experiment";
	
	public static void setDefault(){
		sqlDatabase = SQL_DATABASE; 
		sqlHost = SQL_HOST; 
		sqlPassword = SQL_PASSWORD; 
		sqlUsername = SQL_USERNAME;
		sqlConnection = SQL_CONNECTION;
		sqlDriver = SQL_DRIVER;
		sqlTableRosette = SQL_TABLE_ROSETTE;
		sqlTableLeaf = SQL_TABLE_LEAF;
		sqlTableSeed = SQL_TABLE_SEED;
		sqlTablePlant = SQL_TABLE_PLANT;
		sqlTableStock = SQL_TABLE_STOCK;
		sqlTableTreatment  = SQL_TABLE_TREATMENT;
		sqlTableExperiment  =SQL_TABLE_EXPERIMENT;
	}
	
	public static void initialize(){
		update();
	}
	
	public static void update(){	
		sqlDatabase = F_o_P.prefs.get("sqlDatabase", SQL_DATABASE); 
		sqlHost = F_o_P.prefs.get("sqlHost", SQL_HOST); 
		sqlPassword = F_o_P.prefs.get("sqlPassword", SQL_PASSWORD); 
		sqlUsername = F_o_P.prefs.get("sqlUsername", SQL_USERNAME);
		sqlConnection = F_o_P.prefs.get("sqlConnection", SQL_CONNECTION);
		sqlDriver = F_o_P.prefs.get("sqlDriver", SQL_DRIVER);
		sqlTableRosette = F_o_P.prefs.get("sqlTableRosette", SQL_TABLE_ROSETTE);
		sqlTableLeaf = F_o_P.prefs.get("sqlTableLeaf", SQL_TABLE_LEAF);
		sqlTableSeed = F_o_P.prefs.get("sqlTableSeed", SQL_TABLE_SEED);
		sqlTablePlant = F_o_P.prefs.get("sqlTablePlant", SQL_TABLE_PLANT);
		sqlTableStock = F_o_P.prefs.get("sqlTableStock", SQL_TABLE_STOCK);
		sqlTableTreatment = F_o_P.prefs.get("sqlTableTreatment", SQL_TABLE_TREATMENT);
		sqlTableExperiment = F_o_P.prefs.get("sqlTableExperience", SQL_TABLE_EXPERIMENT);
	}
	
}
