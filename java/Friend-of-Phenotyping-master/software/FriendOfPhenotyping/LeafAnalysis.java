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


import java.awt.Color;
import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;

import leafJ.*;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.RGBStackSplitter;
import ij.plugin.frame.RoiManager;
import ij.process.AutoThresholder.Method;
import ij.process.ImageProcessor;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;

public class LeafAnalysis {
	
	
	// Export paramaters
	static final int QR_NAME = 0;
	static final int IM_NAME = 1;
	static final int NO_NAME = 2;
	boolean exportToSQL, exportToCSV, sqlOverridePrevious;
	String csvFileName;
	java.sql.Connection conn;

	// Analaysis parameters
	static double scalePix, scaleCm, scale;
	int nameType;


	// Image parameters
	static float totalSurface;
	static int nLeaves;
	static String experiment, stock, treatment, pot, plant, plantID;
	static File imageFolder, dirOriginal, dirMask;	
	String currentDate, startingDate = "0000"; 
	static File[] images; 		
	long DAS;	 
	static int nROI;
	

	
	RoiManager imageROIManager;
	static Roi[] allROIImage;
	
	RoiManager leafROIManager;
	Roi[] allROILeaf;
	
	static ImagePlus currentImage, currentSelection, processedImage;
	static ImageProcessor ipCH, processedImageProcessor;
		
	static ResultsTable rt = new ResultsTable();
	static ResultsTable rtAll = new ResultsTable();
	static RGBStackSplitter splitter = new RGBStackSplitter();
	static ImageCalculator ic = new ImageCalculator();	
	static Analyzer analyzer = new Analyzer();
	static Calibration cal = new Calibration();
	static ParticleAnalyzer pa;
	static PolygonRoi pROI, chROI;
	static PrintWriter pw;	
	
	
	public LeafAnalysis(File f, 
			boolean sql, 
			boolean del, 
			boolean csv, 
			String csvF, 
			int scaleP, 
			float scaleC, 
			int pname){
		
		imageFolder= f;
		
		// Export option
		exportToSQL = sql;
		exportToCSV = csv;
		csvFileName = csvF;
		sqlOverridePrevious = del;
		
		// Analysis options
		scalePix = scaleP;
		scaleCm = scaleC;
		nameType = pname;
		
		// Analyze the plants
		analyze();
	}	
	
	/**
	 * Perform the analysis of all the images
	 */
	public void analyze(){
		
        // Create the different directories
		images = imageFolder.listFiles();
		for(int i = 0; i< images.length; i++) if(images[i].isHidden()) images[i].delete();
		images = imageFolder.listFiles();
		IJ.log("Leaf size analysis started: "+imageFolder.getAbsolutePath().toString());
		
		// Open the first images and set the different ROI		
		imageROIManager = new RoiManager();
		imageROIManager.setVisible(true);
		leafROIManager = new RoiManager();
		
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
		nROI = imageROIManager.getCount();	
		allROIImage = imageROIManager.getRoisAsArray();
		imageROIManager.setVisible(false);
		imageROIManager.removeAll();
		imageROIManager.close();
				
		// Initialize the export connection
		if(exportToCSV){
			pw = Util.initializeCSV(csvFileName);
			if(nameType == NO_NAME) pw.println("image, leaf, petiole_length, length, width, area, perimeter, circularity");
			else pw.println("experience, genotype, treatment, box, plant, leaf, petiole_length, length, width, area, perimeter, circularity");
			pw.flush();
		}		
		if(exportToSQL) conn = Util.initializeSQL(); 
		
		
		for(int i = 0; i < images.length; i++){
			// open the image
			currentImage = IJ.openImage(images[i].getAbsolutePath());
			
			// Process the name to retrieve the experiment information
			if(nameType == QR_NAME){
				currentImage.setRoi(allROIImage[0]);
				currentSelection = currentImage.duplicate(); 
				
				// Get the QR and process it
				String qrCode = Util.getQR(currentSelection);
				if(qrCode.endsWith("xx")) continue;
				processName(qrCode, false);
				
				// Get the DAS
				currentDate = images[i].getName().substring(0, 10);				
				if(startingDate.startsWith("0000")) startingDate = Util.getStartingDate(experiment);
				DAS = Util.getDAS(startingDate, currentDate);
				plantID = Util.getPlantID(experiment, stock, treatment, pot, Integer.valueOf(plant));

				// Create the directories
				File dirSave = Util.createFolderStructure(experiment, stock, treatment, plantID, "leaves");
				dirOriginal = new File(dirSave.getAbsolutePath()+"/originals/");
				dirMask = new File(dirSave.getAbsolutePath()+"/masks/");

				startIndex = 1;				
			}
			else if(nameType == IM_NAME){
				processName(images[i].getName(), true);
				
				// Create the directories
//				File dirSave = Util.createFolderStructure(experiment, stock, treatment, plantID, "leaves");
//				dirOriginal = new File(dirSave.getAbsolutePath()+"/originals/");
//				dirMask = new File(dirSave.getAbsolutePath()+"/masks/");
//				IJ.log(dirSave.getAbsolutePath());
			}
			
			// Check if the folder were correctly created
			if(dirOriginal == null || dirMask == null){
				dirOriginal = new File(imageFolder.getAbsolutePath()+"/originals/");
				dirMask = new File(imageFolder.getAbsolutePath()+"/masks/");
				if(!dirOriginal.exists()) try{dirOriginal.mkdir();} catch(Exception e){IJ.log("Could not create folder "+dirOriginal);}
				if(!dirMask.exists()) try{dirMask.mkdir();} catch(Exception e){IJ.log("Could not create folder "+dirMask);}		
			}
						
			// Save the original image
			IJ.save(currentImage, dirOriginal.getAbsolutePath()+"/"+images[i].getName());
			images[i].delete();

			
			// Process the different plants in the image, based on the different ROI in the manager
			nLeaves = 0;
			totalSurface = 0;
			String name = images[i].getName();
			for(int j = startIndex ; j < nROI; j++){
				
				// Show the progress of the operation
				//IJ.showProgress(counter/(nROI*images.length));
				counter++;
				
				// Get the plant
				currentImage.setRoi(allROIImage[j]);
				currentSelection = currentImage.duplicate();				
				
				// Analyse the individual leaves
				nLeaves = analyseLeaf(name, nLeaves, j);
				
				// Analyse all the leaves
				if(exportToSQL){
					float[] data = analyseSurface(name);
					totalSurface += data[0];
					nLeaves += data[1];
				}	
				
				// Close the current image
				currentSelection.flush(); currentSelection.close();
			}	
			
			// Send the synthetic data to the database
			if(exportToSQL) sendDataToSQL(totalSurface, nLeaves);			
		}
		
		// Comptute the time taken by the plugin
		long endD = System.currentTimeMillis();
		IJ.log(counter+" images analyzed in "+(endD - startD)/1000+" s");
		return true;
	}
	
	
	/**
	 * Analyse the total leaf surface present in the image
	 * @param iName
	 */
	private float[] analyseSurface(String iName){	

		float[] data = new float[2];
		data[0] = 0;
		data[1] = 0;
		
		splitter.split(currentSelection.getStack(), true);
		processedImage = new ImagePlus("blue", splitter.blue.getProcessor(1));

		// Threshold the image
		processedImageProcessor = processedImage.getProcessor();
		processedImageProcessor.setAutoThreshold("MaxEntropy");

		for(int i = 0; i < 5; i++) processedImageProcessor.dilate();
		for(int i = 0; i < 5; i++) processedImageProcessor.erode();
		
		processedImage.setProcessor(processedImageProcessor);		
		
		// Save the mask
		IJ.save(processedImage, dirMask.getAbsolutePath()+"/"+iName+"_all_mask");

		// Set the scale		
		cal.setUnit("cm");
		cal.pixelHeight = scaleCm / scalePix;
		cal.pixelWidth = scaleCm / scalePix;
		
		// Analyse the image
		Analyzer.setResultsTable(rtAll);
		rtAll.reset();
		pa = new ParticleAnalyzer(ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES | ParticleAnalyzer.CLEAR_WORKSHEET,
				Measurements.AREA, rtAll, 500, 10e9, 0, 0.5);
		processedImage.setCalibration(cal);		
		pa.analyze(processedImage);
		for(int i = 0; i < rtAll.getCounter(); i++) data[0] += (float) rtAll.getValue("Area", i);
		data[1] = rtAll.getCounter();
	
		return data; 
	}
	
	
	
	/**
	 * Analyse the leaves contained in the images
	 * @author  Julin Maloof
				Plant Biology, University of California, Davis
				jnmaloof@ucdavis.edu
	 * @param im
	 */

	private int analyseLeaf(String iName, int count, int selection){
				
		// Delete previous measurments
		imageROIManager.runCommand("Deselect"); 
		imageROIManager.runCommand("Show None"); 
		while(imageROIManager.getSelectedRoisAsArray().length > 0){
			imageROIManager.select(0); 
			imageROIManager.runCommand("Delete"); 
		}
		imageROIManager.removeAll();

		// Threshold the image
		splitter.split(currentSelection.getStack(), true);
		processedImage = new ImagePlus("blue", splitter.blue.getProcessor(1));
		processedImageProcessor = processedImage.getProcessor();
		processedImageProcessor.setAutoThreshold(Method.MaxEntropy, false);
		processedImage.setProcessor(processedImageProcessor);	
		
		for(int i = 0; i < 5; i++) processedImageProcessor.dilate();
		for(int i = 0; i < 5; i++) processedImageProcessor.erode();
				
		double minParticleSize = 5000;
				
		
		ImagePlus timp = new ImagePlus("temporary",processedImageProcessor.crop()); //temporary ImagePlus to hold current ROI
		ImageProcessor tip = timp.getProcessor(); //ImageProcessor associated with timp
		tip.setAutoThreshold("MaxEntropy");

		//tip.setThreshold(minThreshold, maxThreshold, ImageProcessor.NO_LUT_UPDATE);
		
		// Analyse the image
		cal = timp.getCalibration();
		cal.setUnit("cm");
		cal.pixelHeight = scaleCm / scalePix;
		cal.pixelWidth = scaleCm / scalePix;
		
		rt = new ResultsTable();

		pa = new ParticleAnalyzer(ParticleAnalyzer.SHOW_NONE | ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES
			,Measurements.RECT+Measurements.ELLIPSE, rt, minParticleSize, Double.POSITIVE_INFINITY,.25,1);
			
		pa.analyze(timp,tip);
				
		//IJ.setColumnHeadings("Plant	Top	Bottom");
		
		leaf[] leaves = new leaf[rt.getCounter()];	//an array of leaves
		
		int[][] xpos = new int[rt.getCounter()][];	//an array to hold leaf position
		Comparator<int[]> sorter = new Sort2D(1);	//to sort the xpos[][]

		//need to sort leaves based on x position
		//first go through the results table to get x position
		//then sort before setting up leaves and processing
		
		//Find boundary x position for each leaf
		for (int leafCurrent = 0; leafCurrent < rt.getCounter();leafCurrent++) {
			xpos[leafCurrent] = new int[] {leafCurrent, (int) rt.getValue("BX",leafCurrent)};
		}
		
		//sort the array based on x position
		Arrays.sort(xpos,sorter);

		//set and process leaves
		for (int i = 0; i < xpos.length;i++) {	// Move through the leaves
			int leafCurrent = xpos[i][0];
			leaves[leafCurrent] = new leaf();
			leaves[leafCurrent].setLeaf(rt, leafCurrent, cal);		//set initial attributes
	
			leaves[leafCurrent].scanLeaf(tip);			//do a scan across the length to determine widths
			leaves[leafCurrent].findPetiole(tip);			//

			leaves[leafCurrent].addPetioleToManager(timp, tip, imageROIManager, i);
			leaves[leafCurrent].addBladeToManager(timp, tip, imageROIManager, i);
		}

		//for leafCurrent
		//timp = WindowManager.getCurrentImage();
		timp.setProcessor(iName, tip);
		timp.setCalibration(cal);
	

		// Measure data
		
		ResultsTable rt2 = new ResultsTable();	//will temporarily hold the results from the Blade Analyzer. 
		Analyzer petioleAnalyzer = new Analyzer(timp,  Measurements.PERIMETER , rt2);
		Analyzer bladeAnalyzer = new Analyzer(timp, Measurements.AREA + Measurements.CIRCULARITY + 
				Measurements.ELLIPSE + Measurements.PERIMETER + Measurements.LABELS, rt2);
		
		allROILeaf = imageROIManager.getRoisAsArray();

		double[][] data = new double[allROILeaf.length][7];		//this is probably longer than we need it but allows
															//for the possibility of each ROI being its own leaf
		for (int i = 0; i < allROILeaf.length; i++) { 			//loop through each object in the array
			//roiM.select(i);									//needed for Analyzer
			timp.setRoi(allROILeaf[i]);
			String[] name = allROILeaf[i].getName().split(" "); 	//Split the ROI name into components
			try{
				int leaf = new Integer(name[2]) - 1;
				if (name[1].equals("Petiole")) {
					data[leaf][0] = Float.valueOf(name[2]); 					//should be redundant!
					petioleAnalyzer.measure();
					data[leaf][1] = (double) rt2.getValue("Perim.",rt2.getCounter()-1);
				}
				if (name[1].equals("Blade")) {
					data[leaf][0] = Float.valueOf(name[2]);
					bladeAnalyzer.measure();
					data[leaf][2] = (double) rt2.getValue("Major",rt2.getCounter()-1);
					data[leaf][3] = (double) rt2.getValue("Minor",rt2.getCounter()-1);
					data[leaf][4] = (double) rt2.getValue("Area",rt2.getCounter()-1);
					data[leaf][5] = (double) rt2.getValue("Perim.",rt2.getCounter()-1);
					data[leaf][6] = (double) rt2.getValue("Circ.",rt2.getCounter()-1);
				}			
			}catch(Exception e){}
		}

		int leafNum = 0;
		// Export the data
		for(int i = 0; i < allROILeaf.length/2; i++){
			if(exportToCSV && data[i][5] < 1000){
				String firstCols;
				if(nameType != NO_NAME) firstCols = experiment+","+stock+","+treatment+","+pot+","+plant+",";
				else firstCols = iName+",";
				leafNum = count + (int)data[i][0];
				pw.println(firstCols+leafNum+","+data[i][1]+","+data[i][2]+","+data[i][3]+","+data[i][4]+","+data[i][5]+","+data[i][6]);
				pw.flush();
			}
			if(exportToSQL && data[i][5] < 1000){
				leafNum = count + (int)data[i][0];
				plantID = Util.getPlantID(experiment, stock, treatment, pot, Integer.valueOf(plant));
				sendDataToSQL(leafNum, data[i][1], data[i][2], data[i][3], data[i][4], data[i][5], data[i][6]);
			}	
		}
		
		// Save the mask with the ROI
		int n = allROILeaf.length;
		imageROIManager.runCommand("Deselect");
		Overlay ov = new  Overlay();
		for (int i = 0; i < n; i++) {
			allROILeaf[i].setStrokeWidth(8);
			ov.add(allROILeaf[i]);
		} 
		ov.setStrokeColor(Color.green);
		timp.setOverlay(ov);
		timp.flatten();
		IJ.save(timp, dirMask.getAbsolutePath()+"/"+iName+"_indiv_mask_"+selection);

		// Clear the list of roi for the next round
		imageROIManager.runCommand("Deselect"); 
		imageROIManager.runCommand("Show None"); 
		for (int i = n-1; i >= 0; i--) { 
			imageROIManager.select(i); 
			imageROIManager.runCommand("Delete"); 
		} 
		for (int i = (rt.getCounter()-1); i >= 0; i--) rt.deleteRow(i);  
		
		timp.flush(); timp.close();	


		return leafNum;
	}
	
	
	
	/**
	 * Send the measured data to the sql database (if requested by the user).
	 */
	private void sendDataToSQL(double leaf, double plength, double length, double width, double area, double perim, double circ){
		java.sql.Statement stmt = null;
		
		try{stmt = conn.createStatement();}
		catch(Exception e){IJ.log("SQL statement could not be created");}
		
		// Send the current data
		String query = "INSERT INTO "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableLeaf+" (plant_id, leaf, das, petiole_length, length, width, surface, perimeter, circularity) "+
				"VALUES ("+plantID+", "+DAS+", "+leaf+", "+plength+", "+length+", "+width+", "+area+", "+perim+", "+circ+")";
		try{stmt.execute(query);} 
		catch(Exception e){IJ.log("sendDataToSQL failed: "+e+"\n"+query);}
	}
	
	
	/**
	 * Send the data to the SQL database. The data are inserted in the existing plant database.
	 * @param area
	 * @param nLeaves
	 */
	private void sendDataToSQL(float area, int nLeaves){
		java.sql.Statement stmt = null;
		int max = 0;
		
		try{stmt = conn.createStatement();}
		catch(Exception e){IJ.log("SQL statement could not be created");}
		
		
		// Send the current data
		try{
			java.sql.ResultSet rs=stmt.executeQuery("SELECT max(das) FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableRosette+" WHERE plant_id="+plantID);
			rs.first();
			max = rs.getInt(1);	 	  		 
		} 
		catch(Exception e){IJ.log("get max DAS failed: "+e);}
		
		String query = "UPDATE "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableRosette+" SET real_surface = "+area+", leaf_number = "+nLeaves+" WHERE plant_id="+plantID+" AND das="+max;		
		try{stmt.execute(query);} 
		catch(Exception e){IJ.log("sendDataToSQL failed: "+e+"\n"+query);}
		
	}	
	
	
	/** Comparator */
	public class Sort2D implements Comparator<int[]> {
		private int column;
		public Sort2D(int column) {
			this.column = column;
		}
			
		public int compare(int[] one, int[] two) {
			return one[column]-two[column];
		}
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
		plant = Util.processName(name, "PLANT");
		if(das) DAS = Integer.valueOf(Util.processName(name, "DAS"));
	}
	
	
}
