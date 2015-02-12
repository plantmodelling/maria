/**
* @author Guillaume Lobet | Université de Liège
* @date: 2013-01-30
* 
* This plugin open the images contained in a given directory,
* rename based on a QR code contained in the image 
* and crop then based on user ROI selection.
* Every plant contained in the images is measured (surface, diameter) and the result is exported either
* to an CSV file or an SQL database
**/


import java.io.File;
import java.io.PrintWriter;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;

public class RosetteAnalysis {
	
	// Export parameters
	

	
	static File dirAll, dirOriginal, dirMask;	
	static File[] images; 		
	
	
	// Analysis parameters
	static float conservationFactor = 0.9f;
		
	
	// Export paramaters
	static final int QR_NAME = 0;
	static final int IM_NAME = 1;
	static final int NO_NAME = 2;
	java.sql.Connection conn;
	static boolean exportToSQL, exportToCSV, deletePreviousSQL;
	static String sqlTableRosette, sqlUsername, sqlPassword, sqlConnection, sqlDatabase, sqlTablePlant;
	static String  csvFolder, qrCode, imName;
	
	static String currentDate = "0000-00-00";
	static String startingDate = "0000-00-00";
	static String experiment, stock, treatment, pot, plantID; 
	
	static long DAS;
	static int plant, nROI, nameType;
	static double scalePix, scaleCm, scale;
	
	static float surface, convexHull, diameter, previousSurface, previousDiameter, previousConvexHull;
	
	static RoiManager roiM, roiCH;
	static Roi[] allROI;
	
	static ImagePlus currentImage, imageConvexHull, analysedImage, currentSelection;
	static ImageProcessor processorConvexHull, analysedProcessor;
		
	static ResultsTable rt;
	static Calibration cal;
	static ParticleAnalyzer pa;
	static PrintWriter pw;	
	
	/**
	 * Constructor
	 * @param f = File containing the different images
	 * @param scaleP = scale, in pixels
	 * @param scaleC = scale, in cm
	 * @param sql = do you want to export to a sql database
	 * @param csv = do you want to export to a csv file
	 * @param file = if csv = true, what is the file to save it
	 * @param table = table in the database
	 * @param usr = username for the database
	 * @param psw = password for the database
	 * @param del = delete previous measurments
	 * @param name = what to do with the names
	 */
	public RosetteAnalysis(File f,
			int scaleP,
			float scaleC,
			float consFact,
			boolean sql,
			boolean csv,
			String file,
			boolean del,
			int name){
		
		conservationFactor = consFact;
		scalePix = scaleP;
		scaleCm = scaleC;
		dirAll = f;
		exportToSQL = sql;
		exportToCSV = csv;
		deletePreviousSQL = del;
		nameType = name;
		csvFolder = file;
		
		sqlUsername = SQLServer.sqlUsername;
		sqlPassword = SQLServer.sqlPassword;
		sqlTableRosette = SQLServer.sqlTableRosette;
		sqlTablePlant = SQLServer.sqlTablePlant;
		sqlConnection = SQLServer.sqlConnection;
		sqlDatabase = SQLServer.sqlDatabase;
		
		rt = new ResultsTable();
		cal = new Calibration();
		
		// Analyze the plants
		analyze();
	}

	
	/**
	 * Perform the analysis of all the images
	 */
	public void analyze(){
		
        // Create the different directories
		images = dirAll.listFiles();
		for(int i = 0; i< images.length; i++) if(images[i].isHidden()) images[i].delete();
		images = dirAll.listFiles();
		IJ.log("Rosette size analysis started: "+dirAll.getAbsolutePath().toString());
		
		// Open the first images and set the different ROI		
		roiM = new RoiManager();
		roiM.setVisible(true);
		
		IJ.open(images[0].getAbsolutePath());
		currentImage = IJ.getImage();
	}
	
	
	
	/**
	 * Process all the images in the folder
	 * @return
	 */
	public boolean processAllImages(){

		long startD = System.currentTimeMillis();
		int counter = 0;
		int startIndex = 0;

		// Get the ROI
		currentImage.flush(); currentImage.close();
		nROI = roiM.getCount();	
		allROI = roiM.getRoisAsArray();
		roiM.close();
				
		// Initialize the CSV connection
		if(exportToCSV) Util.initializeCSV(csvFolder);
		
		// Initialize the SQL connection
		if(exportToSQL) conn = Util.initializeSQL();
		
		
		for(int i = 0; i < images.length; i++){
			
			// Open the image
			currentImage = IJ.openImage(images[i].getAbsolutePath());
			IJ.save(currentImage, dirOriginal.getAbsolutePath()+"/"+images[i].getName());
			images[i].delete();
			
			// Process the name to retrieve the experiment information
			if(nameType == QR_NAME){
				currentImage.setRoi(allROI[0]);
				currentSelection = currentImage.duplicate(); 
				
				// Get the QR and process it
				qrCode = Util.getQR(currentSelection);
				if(qrCode.endsWith("xx")) continue;
				processName(qrCode, false);
				
				// Get the DAS
				currentDate = images[i].getName().substring(0, 10);				
				if(startingDate.startsWith("0000")) startingDate = Util.getStartingDate(experiment);
				DAS = Util.getDAS(startingDate, currentDate);
				
				startIndex = 1;				
			}
			else if(nameType == IM_NAME) processName(images[i].getName(), true);
			else return false;
			
			
			// Create the folder structure to store the images
			File dirSave = Util.createFolderStructure(experiment, stock, treatment, plantID, "leaves");
			dirOriginal = new File(dirSave.getAbsolutePath()+"/originals/");
			dirMask = new File(dirSave.getAbsolutePath()+"/masks/");			
			currentSelection.flush(); currentSelection.close();
			
			// Process the different plants in the image, based on the different ROI in the manager
			for(int j = startIndex ; j < nROI; j++){
				
				// Show the progress of the operation
				IJ.showProgress(counter/(nROI*images.length));
				counter++;
				
				// Get the plant
				plant = j;
				currentImage.setRoi(allROI[j]);
				currentSelection = currentImage.duplicate();				
				Util.getPlantID(experiment, stock, treatment, pot, plant);
		
				// Measure the image
				measureImage(images[i].getName());
			
				// Close the current image
				currentSelection.flush(); currentSelection.close();
			}
		}
		
		long endD = System.currentTimeMillis();
		IJ.log(counter+" images analyzed in "+(endD - startD)/1000+" s");
		return true;
	}
	
	/**
	 * Process the image to extract all the usefull information 
	 * @param im
	 */
	private void measureImage(String title){
		
		//Get the hue value from the image
		analysedImage = new ImagePlus();	
        ImagePlus im = new ImagePlus();
        ColorProcessor cp = (ColorProcessor) currentSelection.getProcessor();        
        im.setStack(cp.getHSBStack());       
        analysedProcessor = im.getProcessor();
		im.flush(); im.close();
        
		// Threshold the hue value
		analysedProcessor.threshold(135);
		analysedImage.setProcessor(analysedProcessor);		
		

		// Set the scale		
		cal.setUnit("cm");
		cal.pixelHeight = scaleCm / scalePix;
		cal.pixelWidth = scaleCm / scalePix;
		
		// Get the surface
		Analyzer.setResultsTable(rt);
		rt.reset();
		pa = new ParticleAnalyzer(ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES | ParticleAnalyzer.CLEAR_WORKSHEET,
				Measurements.AREA, rt, 100, 10e9, 0, 0.5);
		analysedImage.setCalibration(cal);		
		pa.analyze(analysedImage);
		
		// Get the total surface of all the particules
		surface = 0;
		for(int i = 0; i < rt.getCounter(); i++){
			surface += (float) rt.getValue("Area", i);
		}
		
		
		// Get the Diameter
		
		// TODO Should make a clustering of the different particules in the image
		rt.reset();

		pa = new ParticleAnalyzer(ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES | ParticleAnalyzer.CLEAR_WORKSHEET | ParticleAnalyzer.ADD_TO_MANAGER,
				Measurements.ELLIPSE | Measurements.RECT | Measurements.AREA, rt, 100, 10e9, 0, 0.5);
		pa.analyze(analysedImage);

		if(rt.getCounter() == 0) return;

		// Get the convex hull and the diameter of the bigger particle, which is suposed to be the plant
		convexHull = -1; diameter = -1;
		float max = 0;
		int index = 0;
		if(rt.getCounter() > 0){
			for(int i = 0; i < rt.getCounter(); i++){
				if((float) rt.getValue("Major", i) > max){ 
					max = (float) rt.getValue("Major", i);
					diameter = (float) rt.getValue("Major", i);
					index = i;
				}
			}
		}
		
		rt.reset();
		
		// Convex hull
		RoiManager roi = RoiManager.getInstance();
		Roi[] roiA = roi.getRoisAsArray();
		processorConvexHull = new PolygonRoi(roiA[index].getConvexHull(), Roi.POLYGON).getMask();		
		imageConvexHull = new ImagePlus();
		imageConvexHull.setProcessor(processorConvexHull);
		processorConvexHull.invert();
		imageConvexHull.setCalibration(cal);
		processorConvexHull.autoThreshold();
		
		pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, 
				Measurements.AREA, rt, 100, 10e9);
		pa.analyze(imageConvexHull);
		
		convexHull = (float) rt.getValue("Area", 0);
		
		getPreviousMeasurments();
		
		if(diameter > (conservationFactor*previousDiameter) && convexHull > (conservationFactor*previousConvexHull) && surface > (conservationFactor*previousSurface)){
			
			// Save the binary image
			analysedProcessor = new PolygonRoi(roiA[index].getPolygon(), Roi.POLYGON).getMask();		
			analysedImage = new ImagePlus();
			analysedImage.setProcessor(analysedProcessor);
		
			IJ.save(analysedImage, dirMask.getAbsolutePath()+"/"+qrCode+"_PL"+plant+"_DAS"+DAS+"_bin.tif");
			
			//Send the data to the database
			if(exportToSQL) sendDataToSQL();

			//Send the data to the csv file
			if(exportToCSV) sendDataToCSV();			
			
			
		}
		imageConvexHull.flush(); imageConvexHull.close();
		analysedImage.flush(); analysedImage.close();
	}
	
	
	
	
	/**
	 * Send data to an CSV file
	 */
	private void sendDataToCSV(){
		pw.println(experiment +","+ stock +","+ treatment +","+ pot +","+ plant +","+ DAS +","+ currentDate +","+ surface +","+ diameter +","+ convexHull);
		pw.flush();
	}
	
	
	/**
	 * Send the measure data to the sql database (if requested by the user).
	 */
	private void sendDataToSQL(){
		java.sql.Statement stmt = null;
		String query;
		
		try{stmt = conn.createStatement();}
		catch(Exception e){IJ.log("SQL statement could not be created");}
		
		// If the data already exist in the database and the user want to override it
		if(deletePreviousSQL){
			query = "DELETE FROM "+sqlDatabase+"."+sqlTableRosette+" WHERE plant_id="+plantID+" AND date='"+currentDate+"' AND das="+DAS;
			try{stmt.execute(query);} 
			catch(Exception e){IJ.log("Overriding data failed: "+e+"\n"+query);}
		}
		
		// Send the current data
		query = "INSERT INTO "+sqlDatabase+"."+sqlTableRosette+" (plant_id, date, das, diameter, convex_hull, projected_surface) "+
				"VALUES ("+plantID+", '"+currentDate+"', "+DAS+", "+diameter+", "+convexHull+", "+surface+")";
		try{stmt.execute(query);} 
		catch(Exception e){IJ.log("RosetteAnalysis.sendDataToSQL failed: "+e+"\n"+query);}
	}
	
	
	/**
	 * Get the previous measurment for the analysed plant to compare with the current data. 
	 */
	private void getPreviousMeasurments(){
		previousDiameter = 1.5f;
		previousConvexHull = 1.5f;
		previousSurface = 1.5f;
		
		try{
			java.sql.Statement stmt=conn.createStatement();
			
			// Get the DAS for the previous measurments
			java.sql.ResultSet rs=stmt.executeQuery("SELECT max(das) FROM "+sqlDatabase+"."+sqlTableRosette+" WHERE plant_id = "+plantID+" AND das <= "+DAS);
			rs.first();
			int maxDAS = rs.getInt(1);
						
			java.sql.ResultSet rs1=stmt.executeQuery("SELECT diameter, convex_hull, projected_surface FROM "+sqlDatabase+"."+sqlTableRosette+" WHERE plant_id = "+plantID+" AND das ="+maxDAS);
			rs1.first();
			previousDiameter = rs1.getInt(1);
			previousConvexHull = rs1.getInt(2);
			previousSurface = rs1.getInt(3);

		} 
		catch(Exception e){IJ.log("RosetteAnalysis.getPreviousMeasurments failed");}
	}
	
	
	/**
	 * Process the name
	 * @param name
	 */
	private void processName(String name, boolean das){
		experiment = Util.processName(name, "EXP");
		treatment = Util.processName(name, "TR");
		stock = Util.processName(name, "GEN");
		pot = Util.processName(name, "BOX");
		if(das) DAS = Integer.valueOf(Util.processName(name, "DAS"));
	}
}
