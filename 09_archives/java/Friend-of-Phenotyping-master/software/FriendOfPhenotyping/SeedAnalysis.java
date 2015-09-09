/**
* @author Guillaume Lobet | Université de Liège
* @date: 2013-07-02
**/


import java.io.File;
import java.io.PrintWriter;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.RGBStackSplitter;
import ij.process.ImageProcessor;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;

public class SeedAnalysis {
	
	static final int IM_NAME = 0;
	static final int QR_NAME = 1;
	static final int NO_NAME = 2;
	
	
	// Parameters for the object extraction
	static int minSize, maxSize;
	static float minCirc, maxCirc;

	// Image parameters
	static double scalePix, scaleCm, scale;
	static File imageDir, dir0, dir1, dir2, dir3;	
	static File[] images; 		

	// Export parameters
	static boolean createSQLTable, exportToSQL, deletePreviousSQLRecord, exportToCSV;
	static String sqlTable, sqlUser, sqlPassword, sqlConnection, sqlDriver, sqlDatabase, sqlHost; 
	static String csvFileName, qrCode, imName;
	static String stock; 
	static int processName;
	java.sql.Connection conn;
	
	static ImagePlus rawImage, processedImage;
	static ImageProcessor processedImageProcessor;
		
	static ResultsTable rt = new ResultsTable();
	static RGBStackSplitter splitter = new RGBStackSplitter();
	static Analyzer analyzer = new Analyzer();
	static Calibration cal = new Calibration();
	static ParticleAnalyzer pa;
	static PrintWriter pw;	
	

	/**
	 * Contructor
	 * @param f = the folder containing the images
	 * @param csv = export to CSV file ?
	 * @param csvF = csv file to export
	 * @param sql = export to SQL database?
	 * @param del = delete Previous record in the database?
	 * @param scaleP = scale, in pixels
	 * @param scaleC = scale, in cm
	 * @param minS = minimum particle size, in px
	 * @param maxS = maximum particle size, in px
	 * @param minC = minimum particle circularity
	 * @param maxC = maximum particle circularity
	 * @param pName = type of name processing
	 */
	public SeedAnalysis(File f, boolean csv, String csvF, boolean sql, boolean del, 
			float scaleP, float scaleC, int minS, int maxS, float minC, float maxC,
			int pName){
		
		// Export parameters
		exportToSQL = sql;
		deletePreviousSQLRecord = del;
		exportToCSV = csv;
		csvFileName = csvF;
		processName = pName;

		// Image parameters
		scalePix = scaleP;
		scaleCm = scaleC;
		imageDir = f;

		// Analysis parameters
		minSize = minS;
		maxSize = maxS;
		minCirc = minC;
		maxCirc = maxC;
		
		
		if(exportToSQL){
			sqlTable = SQLServer.sqlTableSeed;
			sqlPassword = SQLServer.sqlPassword;
			sqlUser = SQLServer.sqlUsername;
			sqlDriver = SQLServer.sqlDriver;
			sqlDatabase = SQLServer.sqlDatabase;
			sqlHost = SQLServer.sqlHost;
			sqlConnection = SQLServer.sqlConnection;
		}
		rt = new ResultsTable();
		
		// Analyze the plants
		processAllImages();
	}
	
	
	/**
	 * Process all the images in the folder
	 * @return
	 */
	public boolean processAllImages(){
			
		IJ.log("Seed size analysis started: "+imageDir.getAbsolutePath().toString());

		long startD = System.currentTimeMillis();
		int counter = 0;
		
        // Create the different directories to store the processed images
		images = imageDir.listFiles();
		for(int i = 0; i< images.length; i++) if(images[i].isHidden()) images[i].delete();
		images = imageDir.listFiles();
		dir0 = new File(imageDir.getAbsolutePath()+"/raw_images/");
		dir2 = new File(imageDir.getAbsolutePath()+"/plants_mask/");
		try{ dir0.mkdir(); dir2.mkdir();}
		catch(Exception e){}
				
		// Initialize the export connection
		if(exportToCSV){
			pw = Util.initializeCSV(csvFileName);
			if(processName == NO_NAME) pw.println("image, surface, perimeter, length, width, circularity");
			else pw.println("stock, surface, perimeter, length, width, circularity");
			pw.flush();
		}
		if(exportToSQL) conn = Util.initializeSQL();
		
		// Open every image and analyse it
		for(int i = 0; i < images.length; i++){
			
			// Open the image
			rawImage = IJ.openImage(images[i].getAbsolutePath());
			IJ.save(rawImage, dir0.getAbsolutePath()+"/"+images[i].getName());
			images[i].delete();		
			
			
			// Process the name or the QR code if required
			if(processName != NO_NAME){
				if(processName == IM_NAME) stock = Util.processName(images[i].getName(), "GEN");
				else stock = Util.processName(Util.getQR(rawImage), "GEN");
			}
			
			// Analyse the image
			analyseSeeds(images[i].getName());
			
			counter++;
		}
		
		// Comptute the time taken by the plugin
		long endD = System.currentTimeMillis();
		IJ.log(counter+" images analyzed in "+(endD - startD)/1000+" s");
		return true;
	}
	
	
	/**
	 * Analyse the seeds on the image
	 * @param imageName
	 */
	private void analyseSeeds(String imageName){
		
		// Threshold the image to extract the seed objects
		splitter.split(rawImage.getStack(), true);
		processedImage = new ImagePlus("red", splitter.red.getProcessor(1));
		processedImageProcessor = processedImage.getProcessor();
		processedImageProcessor.invert();
        processedImageProcessor.setAutoThreshold("Default");
		processedImage.setProcessor(processedImageProcessor);	
		

		// Set the scale		
		cal.setUnit("cm");
		cal.pixelHeight = scaleCm / scalePix;
		cal.pixelWidth = scaleCm / scalePix;
		
		// Analyse the image
		Analyzer.setResultsTable(rt);
		rt.reset();
		pa = new ParticleAnalyzer(ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES | ParticleAnalyzer.CLEAR_WORKSHEET | ParticleAnalyzer.SHOW_OVERLAY_OUTLINES,
				Measurements.AREA | Measurements.PERIMETER | Measurements.ELLIPSE | Measurements.CIRCULARITY, 
				rt, minSize, maxSize, minCirc, maxCirc);
		processedImage.setCalibration(cal);		
		pa.analyze(processedImage);
		
		// Save the mask for check
		IJ.save(processedImage, dir2.getAbsolutePath()+"/"+imageName+"_mask");
		
		// Export the data
		for(int i = 0; i < rt.getCounter(); i++){
			if(exportToCSV){
				String head = "";
				if(processName == NO_NAME) head = imageName;
				else head = stock;
				pw.println(head+","+
						rt.getValue("Area", i)+","+
						rt.getValue("Perim.", i)+","+
						rt.getValue("Minor", i)+","+
						rt.getValue("Major", i)+","+
						rt.getValue("Circ.", i));
				pw.flush();
			}
			if(exportToSQL & processName != NO_NAME){
				sendDataToSQL(rt.getValue("Major", i), 
						rt.getValue("Minor", i), 
						rt.getValue("Area", i), 
						rt.getValue("Perim.", i), 
						rt.getValue("Circ.", i));
			}
		}
	}
	
	
	/**
	 * Send data to tha database
	 * @param length of the seed
	 * @param width of the seed
	 * @param area of the seed
	 * @param perim of the seed
	 * @param circ of the seed
	 */
	private void sendDataToSQL(double length, double width, double area, double perim, double circ){
		
		java.sql.Statement stmt = null;
		String query;
		
		// Create SQL statement
		try{stmt = conn.createStatement();}
		catch(Exception e){IJ.log("Could not create SQL statement");}	
		
		// If required, delete previous records in the database
		if(deletePreviousSQLRecord){
			query = "DELETE FROM "+sqlDatabase+"."+sqlTable+" WHERE stock_id = "+stock;
			try{stmt.execute(query);}
			catch(Exception e){IJ.log("Could not execute SQL query: "+query);}		
		}
		
		// Send the data to the database
		query = "INSERT INTO "+sqlDatabase+"."+sqlTable+" (stock_id, length, width, surface, perimeter, circularity) "+
				"VALUES ("+stock+", "+length+", "+width+", "+area+", "+perim+", "+circ+")";
		try{stmt.execute(query);}
		catch(Exception e){IJ.log("Could not execute SQL query: "+query);}		
	}

}
