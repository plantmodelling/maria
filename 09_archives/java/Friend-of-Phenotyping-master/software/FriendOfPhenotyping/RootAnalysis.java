/**
* @author Guillaume Lobet | UniversitŽ de Li�ge
* @date: 2014-06-16
* 
* Global refers to the original images in the time series
* Local refers to te difference between two successive images in the time series
*
**/

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.WaitForUserDialog;
import ij.plugin.ContrastEnhancer;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.EDM;
import ij.plugin.filter.RankFilters;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import ij.process.ImageStatistics;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;

public class RootAnalysis {
	
	// Export parameters
	static File dirAll, dirOriginal, dirMask, dirLocal, dirCoord, dirParam, dirDir;//, dirConvex;	
	static File[] images; 		
	static final int IM_NAME = 1;
	static final int NO_NAME = 2;
	static String  csvGlobalFolder, tpsCoordFolder, csvLocalFolder, csvParamFolder, csvDirFolder, csvEFDFolder, qrCode, imName, baseName;
	
	// Image paramaters
	static String experiment, stock, treatment, pot, plantID; 
	
	// Analysis parameters
	static long DAS;
	static int nROI, nameType;
	static double scalePix, scaleCm, scale, rootMinSize,
	X1,X2,X3,X4,X5,X6,X7,X8,X9,X10,Y1,Y2,Y3,Y4,Y5,Y6,Y7,Y8,Y9,Y10;
	static int nEFD, nCoord;
	static boolean blackRoots, globalAnalysis, coordAnalysis, localAnalysis, rootParameter, efdAnalysis, manualCorrection, lowContrast, directionalAnalysis;
	
	static float angle, length, diameter, feret, tortuosity, area, convexHull, rx, ry,
		depth, width, ax, ay, bx, by, efd, circ, ar, round, solidity, globalFeret, globalAngle, comY, comX,
	a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22, a23, a24, a25, a26, a27, a28, a29, a30, a31,a32,a33,a34,a35,a36,a37,a38,a39,a40,
	d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13, d14, d15, d16, d17, d18, d19, d20, d21, d22, d23, d24, d25, d26, d27, d28, d29, d30, d31, d32, d33, d34, d35, d36, d37, d38, d39, d40, 
	d41, d42, d43, d44, d45, d46, d47, d48, d49, d50, d51, d52, d53, d54, d55, d56, d57, d58, d59,d60, d61, d62, d63, d64, d65, d66, d67, d68, d69, d70, d71, d72, d73, d74, d75, d76, d77, d78, d79, d80, 
	d81, d82, d83, d84, d85, d86, d87,d88,d89,d90,d91,d92,d93,d94,d95,d96,d97,d98,d99,d100,d101,d102,d103,d104,d105,d106,d107,d108,d109,d110,d111,d112,d113,d114,d115,d116,d117,d118,d119,d120,
	d121, d122;
	static PrintWriter pwGlobal, pwCoord, pwLocal, pwParam, pwEFD, pwDir;	
	
	/**
	 * Constructor
	 * @param f = File containing the different images
	 * @param file = where to save csv file
	 * @param scaleP = scale, in pixels
	 * @param scaleC = scale, in cm
	 * @param name = what to do with the names
	 * @param date = starting date of the experiment
	 */
	public RootAnalysis(File f,
			String file,
			float scaleP,
			float scaleC,
			int name,
			boolean black,
			float minsize,
			boolean local,
			boolean global,
			boolean coord,
			boolean efd,
			boolean dir,
			boolean param,
			int nefd,
			int ncoord,
			boolean mc){
		
		// Set up the different variables
		scalePix = scaleP;
		scaleCm = scaleC;
		dirAll = f;
		nameType = name;
		csvGlobalFolder = file.substring(0, file.length()-4)+"-global.csv";
		tpsCoordFolder = file.substring(0, file.length()-4)+"-coord.tps";
		csvEFDFolder = file.substring(0, file.length()-4)+"-efd.csv";
		csvDirFolder = file.substring(0, file.length()-4)+"-dir.csv";
		csvParamFolder = file.substring(0, file.length()-4)+"-MARIA.csv";
		blackRoots = black;
		rootMinSize = minsize;
		globalAnalysis = global;
		localAnalysis = local;
		coordAnalysis = coord;
		rootParameter = param;
		efdAnalysis = efd;
		directionalAnalysis = dir;
		nEFD = nefd;
		nCoord = ncoord;
		manualCorrection = mc;
		lowContrast = false;
		
		// Analyze the plants
		analyze();
	}


	/**
	 * Perform the analysis of all the images
	 */
	public void analyze(){
		
		ImagePlus nextImage = null;
		
        // Get all the images files in the directory
		images = dirAll.listFiles();
		for(int i = 0; i< images.length; i++) if(images[i].isHidden()) images[i].delete();
		images = dirAll.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".tiff") || 
		        		name.toLowerCase().endsWith(".tif") || name.toLowerCase().endsWith(".jpeg") ||
		        		 name.toLowerCase().endsWith(".png");
		    }
		});


		IJ.log("Root image analysis started: "+dirAll.getAbsolutePath().toString());
		long startD = System.currentTimeMillis(); // Counter for the processing time
		int counter = 0;
				
		// Initialize the CSV connection
		if(globalAnalysis){
			pwGlobal = Util.initializeCSV(csvGlobalFolder);
			printGlobalCSVHeader(nameType);
		}
		if(coordAnalysis){
			pwCoord = Util.initializeCSV(tpsCoordFolder);
		}
		
		if(localAnalysis){
			pwLocal = Util.initializeCSV(csvLocalFolder);
			printLocalCSVHeader(nameType);
		}
		if(rootParameter){
			pwParam = Util.initializeCSV(csvParamFolder);
			printParamCSVHeader(nameType);
		}
		
		if(directionalAnalysis){
			pwDir = Util.initializeCSV(csvDirFolder);
			printDirectionalCSVHeader(nameType);
		}		
		if(efdAnalysis){
			pwEFD = Util.initializeCSV(csvEFDFolder);
			printEFDCSVHeader(nameType);
		}		
			
		// Navigate the different images in the time serie
		for(int i = 0; i < images.length; i++){
			// Open the image
			nextImage = IJ.openImage(images[i].getAbsolutePath());
			IJ.log("------------------------");
			IJ.log("Analysis of image "+images[i].getAbsolutePath()+ " started.");
	    	
			// Reset the ROI to the size of the image. This is done to prevent previously drawn ROI (ImageJ keep them in memory) to empede the analysis
			nextImage.setRoi(0, 0, nextImage.getWidth(), nextImage.getHeight());

			// Process the name to retrieve the experiment information
			baseName = images[i].getName();
			if(nameType == IM_NAME) processName(images[i].getName(), true);
			
			// Create the folder structure to store the images
			File dirSave; 
			dirSave = Util.createFolderStructure(dirAll.getAbsolutePath(), globalAnalysis, localAnalysis, coordAnalysis, rootParameter);
			dirMask = new File(dirSave.getAbsolutePath()+"/global/");
			dirLocal = new File(dirSave.getAbsolutePath()+"/local/");
			dirCoord = new File(dirSave.getAbsolutePath()+"/shape/");
			dirParam = new File(dirSave.getAbsolutePath()+"/param/");

			
			// Measure the image
			
			if(localAnalysis){
				IJ.log("------------------------");
				IJ.log("Starting local analysis");
				measureLocalImage(nextImage);
			}
			
			if(globalAnalysis){
				IJ.log("------------------------");
				IJ.log("Starting global analysis");
				measureGlobalImage(nextImage);
			}
			if(coordAnalysis){	
				IJ.log("Start calculating Coordinates");
				measureCoordImage(nextImage);
			}
			if(rootParameter){
				IJ.log("Get root Parameters");
				getDescriptors(nextImage);
			}
		
			// Close the current image
			nextImage.flush(); nextImage.close(); 
			counter ++;
		}
		// Compute the time taken for the analysis
		long endD = System.currentTimeMillis();
		IJ.log("------------------------");
		IJ.log("------------------------");
		IJ.log(counter+" images analyzed in "+(endD - startD)/1000+" s");		
	}
	

	/**
	 * Process the global image to extract all the usefull information 
	 * @param currentImage the current image in the time serie
	 */
	private ImagePlus measureGlobalImage(ImagePlus current){
		
		// Initiate the different ImageJ tools
		ParticleAnalyzer pa;
		EDM edm = new EDM();
		ResultsTable rt = new ResultsTable();
		Calibration cal = new Calibration();
		Calibration calDefault = new Calibration();
		boolean lowContrast = false;
		boolean largeRoot = false;
		ImagePlus nextImage;
		
		// Set the scale		
		int scalingFactor = 2;
		cal.setUnit("cm");
		cal.pixelHeight =( scalingFactor * scaleCm) / scalePix;
		cal.pixelWidth = ( scalingFactor * scaleCm) / scalePix;
		// Reset Scale
		calDefault.setUnit("px");
		calDefault.pixelHeight = 1;
		calDefault.pixelWidth = 1;
		
		// Initalisation of the image
		IJ.log("Loading the image");
		ImagePlus currentImage;
    	currentImage = current.duplicate();
    	
    	// Keep a copy of the current image for the next run
    	nextImage = current.duplicate();

    	
    	// Pre-process the image
    	IJ.log("Pre-processing the image");
    	ImageProcessor globalProcessor = currentImage.getProcessor();
    	// Resize the image to speed up the analysis
    	globalProcessor = globalProcessor.resize(globalProcessor.getWidth()/scalingFactor, globalProcessor.getHeight()/scalingFactor);
		// Convert to 8bit image if needed
    	if(globalProcessor.getBitDepth() != 8) globalProcessor = globalProcessor.convertToByte(true);
        // If the root is white on black, then invert the image
    	if(!blackRoots) globalProcessor.invert();
       
    	// 
        currentImage.setProcessor(globalProcessor);

	if(directionalAnalysis){
			
			/**
			 * Modified from the Directionality plugin of Fiji 
			 * @author Jean-Yves Tinevez jeanyves.tinevez@gmail.com
			 * @version 2.0
			 */
			
			Directionality dnlty = new Directionality();
		
			// Set fields and settings
			int nbins = 10;
			int binStart = -90;
			ImagePlus img = new ImagePlus();
			img.setProcessor(globalProcessor.duplicate().rotateLeft());
			dnlty.setImagePlus(img);
			dnlty.setMethod(Directionality.AnalysisMethod.FOURIER_COMPONENTS);
			dnlty.setBinNumber(nbins);
			dnlty.setBinStart(binStart);
			dnlty.setBuildOrientationMapFlag(true);
			
			// Do calculation
			dnlty.computeHistograms();
			ResultsTable rs = dnlty.displayResultsTable();
			for(int k = 0; k < rs.getCounter(); k++){
				for(int l = 0; l < (rs.getValueAsDouble(1, k)); l++){
					this.sendDirectionalDataToCSV(k, rs.getValueAsDouble(0, k));
				}
			}
			ImagePlus img2 = new ImagePlus("Orientation map", dnlty.getOrientationMap());
			img2.setProcessor(img2.getProcessor().duplicate().rotateRight());
			img2.show();
			IJ.save(new ImagePlus("Orientation map", dnlty.getOrientationMap()), dirLocal.getAbsolutePath()+"/"+baseName+"_orientationmap.tif");

		}	
        
        // Get the image stast to detect the presence of low contrast images or a very large root system
        IJ.log("Getting Image statistics");
        ImageStatistics istats;
        istats = currentImage.getStatistics();        
        float diff = (((float)istats.pixelCount - (float)istats.maxCount) / (float)istats.pixelCount);
        if(diff > 0.1) largeRoot = true;
        if((istats.max-istats.mode) < 10) lowContrast = true; IJ.log("Low contrast image");
        
        
		// Threshold the image
        IJ.log("Thresholding the image");
        globalProcessor.setAutoThreshold("Li"); // TODO add the threshold as a user input
        
        // Remove the small particules in the images using a MIN/MAX filter
    	RankFilters rf = new RankFilters();
        if(!lowContrast) { rf.rank(globalProcessor, 1, RankFilters.MAX);}
        else { rf.rank(globalProcessor, 1, RankFilters.MIN);}
        currentImage.setProcessor(globalProcessor);
        
        // Clean the image by removing the small particules. Might be redundant with the previous operation...
        IJ.log("Cleaning the image");
		pa = new ParticleAnalyzer(ParticleAnalyzer.SHOW_MASKS, Measurements.AREA, rt, rootMinSize/scalingFactor, 10e9, 0, 1);
		pa.analyze(currentImage);
		// Get the mask from the PArticuleAnalyser
		ImagePlus globalMask = IJ.getImage(); 
		globalMask.hide(); // Hide the mask, we do not want to display it.
		ImageProcessor globalMaskProcessor = globalMask.getProcessor();
		//globalMaskProcessor.invert();

	
		

		// TOTAL SURFACE
		// Get the surface
		IJ.save(globalMask.duplicate(), dirMask.getAbsolutePath()+"/"+baseName+"_area.tif"); // Save the image used for the area computation
		IJ.log("Getting the total surface of root system");
		rt.reset();
		Analyzer.setResultsTable(rt);
		globalMask.setCalibration(cal);
		pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA, rt, rootMinSize/scalingFactor, 10e9, 0, 1);
		pa.analyze(globalMask);

		// Get the total surface of all the particules (the different parts of the root syste�)
		area = 0;
		for(int i = 0; i < rt.getCounter(); i++){
			area += (float) rt.getValue("Area", i);			
		}
		
		// CONVEX HULL
		// Compute the distance map
		IJ.log("Computing EDM mask");
		globalMaskProcessor.invert();
		edm.run(globalMaskProcessor);
		//globalMask.setProcessor(globalMaskProcessor); globalMask.show(); if(1==1) return null;
	
		
		IJ.log("Computing best threshold");
        // Find the best Threshold and apply it
		int tr = getBestThreshold(globalMaskProcessor.duplicate(), lowContrast || largeRoot);
		globalMaskProcessor.threshold(tr);
		
		
		// Erode the mask to reduce the effect of the extra size of the EDM threshold 
		IJ.log("Finding convex hull mask");
		globalMaskProcessor.invert();
		BinaryProcessor bp = new BinaryProcessor(new ByteProcessor(globalMaskProcessor, true));
		
		// Erode the mask to get closer to the real root system shape
		for(int i=0; i < tr/1.5; i++) bp.erode();	
		globalMaskProcessor = bp.duplicate();
		globalMask.setProcessor(globalMaskProcessor);	
		
		

		
		// Add the object to the ROI manager
		globalMask.setCalibration(calDefault);		
		Analyzer.setResultsTable(rt);
		rt.reset();
		pa = new ParticleAnalyzer(ParticleAnalyzer.ADD_TO_MANAGER | ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA, rt, 0, 10e9);
		pa.analyze(globalMask);
		
		// Find the largest object in the image (the root system) in case their are still mutliple objects.
		IJ.log("Find max object");
		int index = 0;
		double max = 0;
		for(int i = 0; i < rt.getCounter(); i++){
			if(rt.getValue("Area", i) > max){
				max = rt.getValue("Area", i);
				index = i;
			};			
		}
		
		
		// Get the convex hull from the ROI manager (the largest object)
		RoiManager manager = RoiManager.getInstance();
		Roi[] roiA = manager.getRoisAsArray();
		Roi convexROI = roiA[index];
		
		if(manualCorrection){
			IJ.log("Correcting the ROI");
			ImagePlus ip = currentImage.duplicate();
			ip.setCalibration(calDefault);
			ip.show(); // Show the image
			ContrastEnhancer ce = new ContrastEnhancer();
			ce.equalize(ip);
			//ip.setRoi(cleanROI(convexROI)); // Reduce the number of point in the convexhull roi
			ip.setRoi(new PolygonRoi(convexROI.getConvexHull(), Roi.POLYGON)); // Show the ROI
			new WaitForUserDialog("Correct ROI", baseName+"\n Please correct the ROI by dragging the nodes.\n\n When done, click OK to validate").show(); // Wait for the user to correct the ROI		
			convexROI = ip.getRoi(); // Get the convex hull from the object stored in the ROI Manager	
			ip.hide(); ip.close(); ip.flush();
		}
		
		// Create a mask with the convexhull
		IJ.log("Creating convexhull mask");
		globalMaskProcessor = new PolygonRoi(convexROI.getConvexHull(), Roi.POLYGON).getMask(); 			
		ImagePlus globalConvexHull = new ImagePlus();
		globalConvexHull.setProcessor(globalMaskProcessor);
		globalConvexHull.setCalibration(cal);
		globalMaskProcessor.autoThreshold();	
		
		// Compute the Fourrier Descriptors for the ROI.
		// The number of descriptors is set by the users (nEFD)
		// This part uses the EllipticFD plugin from Thomas Boudier and Ben Tupper (EFD)
		if(efdAnalysis){
			IJ.log("EFD analysis");
			PolygonRoi roi = new PolygonRoi(convexROI.getConvexHull(), Roi.POLYGON);
			Rectangle rect = roi.getBounds();
			int n = roi.getNCoordinates();
			double[] x = new double[n];
			double[] y = new double[n];
			int[] xp = roi.getXCoordinates();
			int[] yp = roi.getYCoordinates();  
			for (int i = 0; i < n; i++){
			    x[i] = (double) (rect.x + xp[i]);
			    y[i] = (double) (rect.y + yp[i]); 
			}  
			EllipticFD efd = new EllipticFD(x, y, nEFD);
			for (int i = 0; i < efd.nFD; i++) {
				sendEFDDataToCSV(i+1, efd.ax[i], efd.ay[i],efd.bx[i],efd.by[i],efd.efd[i]);
			}
			
			// Create the roi from the EFD to display as an overlay on the saved image
		    double[][] xy = efd.createPolygon();
		    int m = xy.length;
		    int[] xefd = new int[m];
		    int[] yefd = new int[m];
		    for (int i = 0; i < m; i++){
		    	xefd[i] = (int) Math.floor(xy[i][0]);
		    	yefd[i] = (int) Math.floor(xy[i][1]);
		    }
		    PolygonRoi roi2 = new PolygonRoi(xefd, yefd, n, Roi.FREEROI);
		    
		    Overlay overlay = new Overlay(roi2); 
		    ImagePlus edfOverlay = currentImage.duplicate();
		    edfOverlay.setOverlay(overlay); 
		    edfOverlay = edfOverlay.flatten(); 
			IJ.save(edfOverlay, dirMask.getAbsolutePath()+"/"+baseName+"_edf_overlay.tif");		    
		} 
		
		
		
		
		manager.removeAll(); // Remove the element from the ROI manager
		  		

		// Save the image with the convexhull ROI overlaid
		IJ.log("Create ROI overlay");
		Roi roiToOverlay = new PolygonRoi(convexROI.getConvexHull(), Roi.POLYGON); 
	    roiToOverlay.setStrokeColor(Color.blue);
	    roiToOverlay.setStrokeWidth(5);
	    Overlay overlay = new Overlay(roiToOverlay); 
	    currentImage.setOverlay(overlay); 
	    currentImage = currentImage.flatten(); 

	    // Get shape measurments from the convexhull
		IJ.log("Get measurements");
		globalConvexHull.setCalibration(cal);	
		Analyzer.setResultsTable(rt);
		rt.reset();
		pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.CENTER_OF_MASS |
				Measurements.AREA | Measurements.RECT | Measurements.SHAPE_DESCRIPTORS | Measurements.FERET, 
				rt, 0, 10e9);
		pa.analyze(globalConvexHull);
		convexHull = (float) rt.getValue("Area", 0);
		depth = (float) rt.getValue("Height", 0);
		width = (float) rt.getValue("Width", 0);
		circ = (float) rt.getValue("Circ.", 0);
		ar = (float) rt.getValue("AR", 0);
		round = (float) rt.getValue("Round", 0);
		solidity = (float) rt.getValue("Solidity", 0);
		globalFeret = (float) rt.getValue("Feret", 0);
		globalAngle = (float) rt.getValue("FeretAngle", 0);
		comY = (float) rt.getValue("XM", 0);
		comY = (float) rt.getValue("YM", 0);
		
		
		IJ.log("Save images");
		// Save the images for post-processing check
		IJ.save(globalMask, dirMask.getAbsolutePath()+"/"+baseName+"_mask.jpeg");
		IJ.save(currentImage, dirMask.getAbsolutePath()+"/"+baseName+"_convexhull_overlay.jpeg");
		IJ.save(globalConvexHull, dirMask.getAbsolutePath()+"/"+baseName+"_convexhull.jpeg");
		
		// Send the data to the CSV file
		sendGlobalDataToCSV(nameType);

		// Close the images
		globalMask.flush(); globalMask.close();
		globalConvexHull.flush(); globalConvexHull.close();
		globalMask.close();
		
		return nextImage;
	}
	

	
	
	/**
	 * Find the best threshold value on an EDM image in order to have only one object remaining.
	 * The idea is to use the EDM image, and threshold at increasing values (starting to 0), until their is only one 
	 * object in the image. This is stopped at a max threshold value to prevent the algrithm to pick up dirts in the image
	 * @param p
	 * @return
	 */
	private int getBestThreshold(ImageProcessor p, boolean low){
		int fact = 2;
		ImagePlus temp = new ImagePlus();
		p = p.resize(p.getWidth()/fact, p.getHeight()/fact);
		ImageProcessor proc;
		ResultsTable rt = new ResultsTable();
		Analyzer.setResultsTable(rt);
		ParticleAnalyzer pa;
		int maxThrsld = 20;
		if(low){
			maxThrsld = 100;
		}
		
		boolean keepGoing = true;
		int thrshld = 1;

		while(keepGoing){
			proc = p.duplicate();
			proc.threshold(thrshld);
			temp.setProcessor(proc);
			// How many objects are in the image
			rt.reset();
			Analyzer.setResultsTable(rt);
			pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA, rt, 0, 10e9, 0, 1);
			pa.analyze(temp);
			if(rt.getCounter() == 1 || thrshld >= maxThrsld){ // If their is only one object in the image or we passed the max threshold.
				keepGoing = false;			
			}
			thrshld += 2; // increment the threshold
		}
		
		// Return the best value
		return thrshld;
	}
	
	/* Calculate coordinates of the root for further shape analysis*	
	 */
	private void measureCoordImage(ImagePlus current){
		// Initialise ImageJ parameters
		ImagePlus currentImage;
		ResultsTable rt2 = new ResultsTable();
		Analyzer an;
		Calibration cal = new Calibration();
		Calibration calDefault = new Calibration();
						
		// Set the scale		
		int scalingFactor = 3;
		cal.setUnit("cm");
		cal.pixelHeight =( scalingFactor * scaleCm) / scalePix;
		cal.pixelWidth = ( scalingFactor * scaleCm) / scalePix;
		// Reset Scale
		calDefault.setUnit("px");
		calDefault.pixelHeight = 1;
		calDefault.pixelWidth = 1;
		
		// Initalisation of the image
		IJ.log("Loading the image");
		printCoordCSVid(nameType);
		currentImage = current.duplicate();
		
		
		// Pre-process the image
		IJ.log("Pre-processing the image");
		ImageProcessor globalProcessor = currentImage.getProcessor();
		// Resize the image to speed up the analysis
		globalProcessor = globalProcessor.resize(globalProcessor.getWidth()/scalingFactor, globalProcessor.getHeight()/scalingFactor);
		// Convert to 8bit image if needed
		if(globalProcessor.getBitDepth() != 8) globalProcessor = globalProcessor.convertToByte(true);
		// If the root is white on black, then invert the image
		if(!blackRoots) globalProcessor.invert();
		   // Threshold the image
        globalProcessor.setAutoThreshold("Li");
        currentImage.setProcessor(globalProcessor);
        
        //get bounding box
        IJ.log("Make bounding box");
        IJ.run(currentImage, "Create Selection", "");
        Roi select;
        select = currentImage.getRoi();
        globalProcessor.setRoi(select.getBounds());
        globalProcessor = globalProcessor.crop();
        currentImage.setProcessor(globalProcessor);
        double w = currentImage.getWidth();
        double h = currentImage.getHeight();
        int nLayer = nCoord/2;
        int m = 2*nLayer;
        float[] xp = new float[m];
        float[] yp = new float[m];
        
      //Get base coordinate of root system
        ImagePlus secImage = currentImage.duplicate();
        secImage.setRoi(new Roi(0,0,currentImage.getWidth(),(0.03*currentImage.getHeight())));
      	ImageProcessor sec = secImage.getProcessor();
      	sec = sec.crop();
      	sec.setAutoThreshold("Li");
      	secImage.setProcessor(sec);	        
        Analyzer.setResultsTable(rt2);
        rt2.reset();
        an = new Analyzer(secImage, Measurements.LIMIT | Measurements.CENTER_OF_MASS, rt2);
        an.measure();
        //double Xmid = rt2.getValue("XM", 0);
        double Ymid = rt2.getValue("YM", 0);
        
        //Calculate coordinates
        IJ.log("Get coordinates");
        // Make rectangle (for each rectangle) 
        // Get bounding box of rectangle
		// Get coordinates of bounding box
		// Save coordinates
        for(int i = 0; i< nLayer; i++){
        ImagePlus currentSelection = currentImage.duplicate();
        float factor = (float) i/(nLayer-1);
        
        if(i == 0){currentSelection.setRoi(new Roi(0, Ymid, w, 3));
        Y1 = Ymid;}
        else if(i == (nLayer-1)){currentSelection.setRoi(new Roi(0, 0.99*h, w, 5));
        Y1 = 0.99*h;}
        else{currentSelection.setRoi(new Roi(0, factor*h, w, 3));
        Y1 = factor*h;}
        
        ImageProcessor small = currentSelection.getProcessor();
        small = small.crop();
        small.setAutoThreshold("Li");
        currentSelection.setProcessor(small);	
        
        IJ.run(currentSelection, "Create Selection", "");        
        Analyzer.setResultsTable(rt2);
        rt2.reset();
        an = new Analyzer(currentSelection, Measurements.RECT, rt2);
        an.measure();
        X1 = rt2.getValue("BX", 0);
     	sendCoordDataToCSV(nameType);
     	xp[i] = (float)X1;
     	yp[i] = (float)Y1;
     	
        double wid0 = rt2.getValue("Width", 0);
        double xx = rt2.getValue("BX", 0);
        X1 = xx+wid0;
        sendCoordDataToCSV(nameType);
        int o=m-i-1;
        xp[o] = (float)X1;
     	yp[o] = (float)Y1;
        }
        	
        
     // Make shape
     	IJ.log("Create shape overlay");
     	PolygonRoi shapeROI = new PolygonRoi(xp,yp, Roi.FREEROI);
     	shapeROI.setStrokeColor(Color.blue);
	    shapeROI.setStrokeWidth(5);
	    Overlay overlay = new Overlay(shapeROI); 
	    currentImage.setOverlay(overlay); 
	    currentImage = currentImage.flatten(); 
  
     	
     // Save the images for post-processing check
     		IJ.save(currentImage, dirCoord.getAbsolutePath()+"/"+baseName+"_shape.jpeg");
     		//IJ.save(currentSelection0, dirCoord.getAbsolutePath()+"/"+baseName+"_shape1.tif");
     		//IJ.save(currentSelection, dirCoord.getAbsolutePath()+"/"+baseName+"_shape2.tif");
     		//IJ.save(currentSelection2, dirCoord.getAbsolutePath()+"/"+baseName+"_shape3.tif");
     		//IJ.save(currentSelection3, dirCoord.getAbsolutePath()+"/"+baseName+"_shape4.tif");
     		//IJ.save(currentSelection3, dirCoord.getAbsolutePath()+"/"+baseName+"_shape5.tif");
     	
	}
	
	/**
	 * Process the local image to extract all the usefull information 
	 * @param current the current image in the time serie
	 * @param previous the previous image in the time serie
	 */
	private void measureLocalImage(ImagePlus current){
		
		// Initiate the different ImageJ tools
		ParticleAnalyzer pa;
		ResultsTable rt = new ResultsTable();
		Calibration cal = new Calibration();
		
		int scale = 2;
		// Set the scale		
		cal.setUnit("cm");
		cal.pixelHeight =( scale * scaleCm) / scalePix;
		cal.pixelWidth = ( scale * scaleCm) / scalePix;
    	
		IJ.log("Loading images");
    	ImagePlus localImage = null;
    	ImageProcessor localProcessor = null;
    	localImage = current.duplicate(); 
    	localProcessor = localImage.getProcessor();
    	localProcessor = localProcessor.resize(localProcessor.getWidth()/scale, localProcessor.getHeight()/scale);
    	if(localProcessor.getBitDepth() != 8) localProcessor = localProcessor.convertToByte(true);
    	
    	localImage.setProcessor(localProcessor);
    	ImagePlus original = localImage.duplicate();	
        if(!blackRoots) localProcessor.invert();

        
        // Get the image stast to detect the presence of low contrast images.
        IJ.log("Get image statistics");
        ImageStatistics istats;
        istats = localImage.getStatistics();        
        if((istats.max-istats.mode) < 10){lowContrast = true;}
        
        IJ.log("Thresold image");
		if(lowContrast)localProcessor.setAutoThreshold("Mean");
		else localProcessor.setAutoThreshold("Default");
		

		// Skeletonize  
//		BinaryProcessor bp = new BinaryProcessor(new ByteProcessor(localProcessor, true));
//		bp.setAutoThreshold("Mean");
////		bp.invert();
//		bp.autoThreshold();
//		bp.skeletonize();	
//		localProcessor = bp.duplicate();    
		localImage.setProcessor(localProcessor);
		localImage.setCalibration(cal);		
		
		if(directionalAnalysis){
			
			/**
			 * Modified from the Directionality plugin of Fiji 
			 * @author Jean-Yves Tinevez jeanyves.tinevez@gmail.com
			 * @version 2.0
			 */
			
			Directionality dnlty = new Directionality();
		
			// Set fields and settings
			int nbins = 10;
			int binStart = -90;
			ImagePlus img = new ImagePlus();
			img.setProcessor(localProcessor.duplicate().rotateLeft());
			dnlty.setImagePlus(img);
			dnlty.setMethod(Directionality.AnalysisMethod.FOURIER_COMPONENTS);
			dnlty.setBinNumber(nbins);
			dnlty.setBinStart(binStart);
			dnlty.setBuildOrientationMapFlag(true);
			
			// Do calculation
			dnlty.computeHistograms();
			ResultsTable rs = dnlty.displayResultsTable();
			for(int k = 0; k < rs.getCounter(); k++){
				for(int l = 0; l < (rs.getValueAsDouble(1, k)); l++){
					this.sendDirectionalDataToCSV(k, rs.getValueAsDouble(0, k));
				}
			}
			ImagePlus img2 = new ImagePlus("Orientation map", dnlty.getOrientationMap());
			img2.setProcessor(img2.getProcessor().duplicate().rotateRight());
			img2.show();
			IJ.save(new ImagePlus("Orientation map", dnlty.getOrientationMap()), dirLocal.getAbsolutePath()+"/"+baseName+"_orientationmap.tif");

		}
		
		
		// Get the surface
		Analyzer.setResultsTable(rt);
		rt.reset();
		pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET | ParticleAnalyzer.ADD_TO_MANAGER, 
				Measurements.AREA | Measurements.FERET | Measurements.RECT, rt, (rootMinSize/scale)/2 , 10e9, 0, 1);
		pa.analyze(localImage);
				
		Overlay overlay = new Overlay();
		if(manualCorrection){
			
			IJ.log("Manual correction");
			ImagePlus ip = localImage.duplicate();
			ip.show();
			ContrastEnhancer ce = new ContrastEnhancer();
			ce.equalize(ip);
			new WaitForUserDialog("Correct ROI", baseName+"\n Please correct the ROI by dragging the nodes.\n\n When done, click OK to validate").show();		
			ip.hide(); ip.close(); ip.flush();
		}		
		
		RoiManager roi = RoiManager.getInstance();
		Roi[] roiA = roi.getRoisAsArray();
		roi.removeAll();
		
		// Get the data of all the particules
		IJ.log("Get particule data");
		for(int i = 0; i < roiA.length; i++){
			
		    ImageProcessor ip = localImage.getProcessor(); 
		    ip.setRoi(roiA[i]); 
		    ImageStatistics stats = ImageStatistics.getStatistics(ip, Measurements.MEAN, localImage.getCalibration());  
			length = (float) stats.area;
			feret = (float) roiA[i].getFeretValues()[0] * (float) cal.pixelHeight;
			angle = (float) roiA[i].getFeretValues()[1] - 90;
			rx = (float) roiA[i].getBounds().x;
			ry = (float) roiA[i].getBounds().y;
			
			tortuosity = length / feret;
			// Send the data to the csv file
			sendLocalDataToCSV(nameType, 0);			
			
			// Get the convex hull from the ROI manage
		    overlay.add(roiA[i]);
			
		}
		
		IJ.log("Save images");
	    original.getProcessor().invert();
	    original.setOverlay(overlay); 
	    original.flatten(); 
			    
		IJ.save(original, dirLocal.getAbsolutePath()+"/"+baseName+"_diff_bin.tif");
	}
	
	private void getDescriptors(ImagePlus current){
		ImagePlus currentImage;
		Analyzer an;
		ResultsTable rt2 = new ResultsTable();
		
		// Initalisation of the image
		IJ.log("Loading the image");
		currentImage = current.duplicate();		
		
		// Pre-process the image
		IJ.log("Pre-processing the image");
		ImageProcessor globalProcessor = currentImage.getProcessor();
		// Resize the image to speed up the analysis (resize to half the modeled images (1pix=1mm)
		double SCALE = scalePix/scaleCm;
		double ScaleA = (double) globalProcessor.getWidth()*(SCALE/(11.81*2));
		double ScaleB = (double)globalProcessor.getHeight()*(SCALE/(11.81*2));
		globalProcessor = globalProcessor.resize((int)ScaleA, (int)ScaleB);
		// Convert to 8bit image if needed
		if(globalProcessor.getBitDepth() != 8) globalProcessor = globalProcessor.convertToByte(true);
		// If the root is white on black, then invert the image
		if(!blackRoots) globalProcessor.invert();
		   // Threshold the image
        currentImage.setProcessor(globalProcessor);
        
        IJ.log("Get diameter");
		//EDM edm = new EDM()
		ImagePlus EDMimage = currentImage.duplicate();
		IJ.run(EDMimage, "Make Binary", "");
		ImagePlus Skelimage = EDMimage.duplicate();
		IJ.run(EDMimage, "Distance Map", "");
		IJ.run(Skelimage, "Skeletonize", "");
		ImageCalculator ic = new ImageCalculator();
		ImagePlus EDMSkel = ic.run("AND create", EDMimage, Skelimage);
		IJ.setThreshold(EDMSkel, 1, 255);
		Analyzer.setResultsTable(rt2);
		rt2.reset();
		IJ.run(EDMSkel, "Create Selection", "");
		an = new Analyzer(EDMSkel, Measurements.LIMIT| Measurements.AREA | Measurements.MODE | Measurements.MEAN | Measurements.MIN_MAX , rt2);
		an.measure();
		float mode = (float) rt2.getValue("Mode", 0);
		float mean = (float) rt2.getValue("Mean", 0);
		d121 = (float) rt2.getValue("Max", 0);
		int up;
		if(mean < mode){up = (int) mode - 1;}
		else{up = (int) mode;}
		d122 = up;
		
		IJ.log("Extract Laterals");
		
		ImagePlus LateralImage = EDMSkel.duplicate();
		IJ.setThreshold(LateralImage, 1, up);
		IJ.run(LateralImage, "Convert to Mask", "");
		//ImageProcessor Lat = LateralImage.getProcessor();
		//Lat = Lat.resize(Lat.getWidth()/scalingFactor, Lat.getHeight()/scalingFactor);
		//LateralImage.setProcessor(Lat);
		
		IJ.log("Extract Primary");
		ImagePlus PrimaryImage = EDMSkel.duplicate();
		IJ.setThreshold(PrimaryImage, up+1, 255);
		IJ.run(PrimaryImage, "Convert to Mask", "");
		//ImageProcessor prim = PrimaryImage.getProcessor();
		//prim = prim.resize(prim.getWidth()/scalingFactor, prim.getHeight()/scalingFactor);
		//PrimaryImage.setProcessor(prim);
		
		
		IJ.log("Calculate parameters total image");
		calculateDescriptors(currentImage, "Total");
		d1 = a1; d2 = a2; d3 = a3; d4 = a4; d5 = a5; d6=a6; d7=a7; d8=a8; d9=a9; d10=a10; d11=a11;
		d12=a12; d13=a13; d14=a14; d15=a15;d16=a16;d17=a17;d18=a18;d19=a19;d20=a20;d21=a21;d22=a22;
		d23=a23;d24=a24;d25=a25;d26=a26; d27=a27;d28=a28;d29=a29;d30=a30;d31=a31;d32=a32;d33=a33; d34=a34;
		d35=a35;d36=a36;d37=a37;d38=a38;d39=a39;d40=a40;
		
		IJ.log("Calculate parameters Primary roots");
		calculateDescriptors(PrimaryImage, "Primary");
		d41 = a1; d42 = a2; d43 = a3; d44 = a4; d45 = a5; d46=a6; d47=a7; d48=a8; d49=a9; d50=a10; d51=a11;
		d52=a12; d53=a13; d54=a14; d55=a15;d56=a16;d57=a17;d58=a18;d59=a19;d60=a20;d61=a21;d62=a22;
		d63=a23;d64=a24;d65=a25;d66=a26; d67=a27;d68=a28;d69=a29;d70=a30;d71=a31;d72=a32;d73=a33; d74=a34;
		d75=a35;d76=a36;d77=a37;d78=a38;d79=a39;d80=a40;
		
		IJ.log("Calculate parameters Lateral roots");
		calculateDescriptors(LateralImage, "Laterals");
		d81 = a1; d82 = a2; d83 = a3; d84 = a4; d85 = a5; d86=a6; d87=a7; d88=a8; d89=a9; d90=a10; d91=a11;
		d92=a12; d93=a13; d94=a14; d95=a15;d96=a16;d97=a17;d98=a18;d99=a19;d100=a20;d101=a21;d102=a22;
		d103=a23;d104=a24;d105=a25;d106=a26;d107=a27;d108=a28;d109=a29;d110=a30;d111=a31;d112=a32;d113=a33; d114=a34;
		d115=a35;d116=a36;d117=a37;d118=a38;d119=a39;d120=a40;

	    IJ.log("Save data");
	 // Send the data to the csv file
	    sendParametersToCSV(nameType, 0);
	}
		
		private void calculateDescriptors(ImagePlus currentImage, String name){
			Analyzer an;
			ParticleAnalyzer pa;
			ResultsTable rt2 = new ResultsTable();	
			
	    //Get base coordinate of root system
      	IJ.log("get base coordinates");
        ImagePlus baseImage = currentImage.duplicate();	
        baseImage.setRoi(new Roi(0,0,currentImage.getWidth(),(0.03*currentImage.getHeight())));
      	ImageProcessor base = baseImage.getProcessor();
      	base = base.crop();
      	base.setAutoThreshold("Li");
      	baseImage.setProcessor(base);
      	IJ.run(baseImage, "Create Selection", "");
        Analyzer.setResultsTable(rt2);
        rt2.reset();
        an = new Analyzer(baseImage, Measurements.CENTER_OF_MASS, rt2);
        an.measure();
        double Ymid = rt2.getValue("YM", 0);
              
        
        baseImage.close();
        
        //Get area of the root system
        IJ.log("Get area");
        ImagePlus secImage = currentImage.duplicate();
        ImageProcessor sec = secImage.getProcessor();
        sec.setAutoThreshold("Li");
        secImage.setProcessor(sec);  
        IJ.run(secImage, "Create Selection", "");
        Analyzer.setResultsTable(rt2);
        rt2.reset();
        an = new Analyzer(secImage, Measurements.AREA | Measurements.CENTER_OF_MASS | Measurements.RECT, rt2);
        an.measure();
        a1 = (float) rt2.getValue("Area", 0);
        
        //width & depth
        IJ.log("Get width and depth");
		float width = (float) rt2.getValue("Width", 0);
		float depth = (float) rt2.getValue("Height", 0);
		
		if (width<0.00001) width=(float) 0.00001;
	    a2=width;
	    if (depth<0.00001) depth= (float) 0.00001;
	    a3=depth;
	    a4=width/depth;
	    
	    //Center of Mass
	    IJ.log("Get Center of Mass");
	    float Xmid = (float) rt2.getValue("XM", 0);
	    a5=Xmid/width;
	    //d9=bary[1]/profondeur;
	    float CMY = (float) rt2.getValue("YM", 0);
	    a6=CMY/depth;
	    
	    secImage.close();
		
	    IJ.log("Calculate density between ellipses");
	    //calcDensiteEllipse(0.25f);
	    ImagePlus currentEllips = currentImage.duplicate();
	    ImageProcessor Ellips = currentEllips.getProcessor();
	    Ellips.setAutoThreshold("Li"); 
	    currentEllips.setProcessor(Ellips);
	    OvalRoi EllipsRoi = new OvalRoi(Xmid - 0.125*width,Ymid,0.25*width,0.25*depth);
	    currentEllips.setRoi(EllipsRoi);
	    Analyzer.setResultsTable(rt2);
	    rt2.reset();
	    an = new Analyzer(currentEllips, Measurements.AREA, rt2);
	    an.measure(); 
	    float areatot1 = (float) rt2.getValue("Area", 0);
	    
	    pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA, rt2, 0, 10e9, 0, 1);
		pa.analyze(currentEllips);
		float areasel1 = 0;
		for(int i = 0; i < rt2.getCounter(); i++){
			areasel1 += (float) rt2.getValue("Area", i);			
		}
	    a7 = areasel1/areatot1;
	    
	    //calcDensiteEntreEllipses(0.25f,0.50f);
	    OvalRoi EllipsRoi2 = new OvalRoi(Xmid - 0.25*width,Ymid,0.5*width,0.5*depth);
	    currentEllips.setRoi(EllipsRoi2);       
	    Analyzer.setResultsTable(rt2);
	    rt2.reset();
	    an = new Analyzer(currentEllips, Measurements.AREA, rt2);
	    an.measure(); 
	    float areatot2 = (float) rt2.getValue("Area", 0);
	    Analyzer.setResultsTable(rt2);
	    rt2.reset();
	    pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA, rt2, 0, 10e9, 0, 1);
		pa.analyze(currentEllips);
		float areasel2 = 0;
		for(int i = 0; i < rt2.getCounter(); i++){
			areasel2 += (float) rt2.getValue("Area", i);			
		}
	    a8 = (areasel2-areasel1)/(areatot2-areatot1);
	    
	    //calcDensiteEntreEllipses(0.50f,0.75f);
	    OvalRoi EllipsRoi3 = new OvalRoi(Xmid - 0.375*width,Ymid,0.75*width,0.75*depth);
	    currentEllips.setRoi(EllipsRoi3);       
	    Analyzer.setResultsTable(rt2);
	    rt2.reset();
	    an = new Analyzer(currentEllips, Measurements.AREA, rt2);
	    an.measure(); 
	    float areatot3 = (float) rt2.getValue("Area", 0);
	    Analyzer.setResultsTable(rt2);
	    rt2.reset();
	    pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA, rt2, 0, 10e9, 0, 1);
		pa.analyze(currentEllips);
		float areasel3 = 0;
		for(int i = 0; i < rt2.getCounter(); i++){
			areasel3 += (float) rt2.getValue("Area", i);			
		}
	    a9 = (areasel3-areasel2)/(areatot3-areatot2);
	    
	    //d8=calcDensiteEntreEllipses(0.75f,1.00f);
	    OvalRoi EllipsRoi4 = new OvalRoi(Xmid - 0.5*width,Ymid,width,depth);
	    currentEllips.setRoi(EllipsRoi4);
	    Analyzer.setResultsTable(rt2);
	    rt2.reset();
	    an = new Analyzer(currentEllips, Measurements.AREA, rt2);
	    an.measure(); 
	    float areatot4 = (float) rt2.getValue("Area", 0);
	    Analyzer.setResultsTable(rt2);
	    rt2.reset();
	    pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA, rt2, 0, 10e9, 0, 1);
		pa.analyze(currentEllips);
		float areasel4 = 0;
		for(int i = 0; i < rt2.getCounter(); i++){
			areasel4 += (float) rt2.getValue("Area", i);			
		}
	    a10 = (areasel4-areasel3)/(areatot4-areatot3);
	    
	  //Create ellips overlay
	    IJ.log("Create Ellips overlay");
     	ImagePlus EllipsOverlay = currentImage.duplicate();
     	EllipsRoi.setStrokeColor(Color.blue);
	    EllipsRoi.setStrokeWidth(5);
	    Overlay Eloverlay = new Overlay(EllipsRoi); 
	    EllipsOverlay.setOverlay(Eloverlay); 
	    EllipsOverlay = EllipsOverlay.flatten(); 
     	EllipsRoi2.setStrokeColor(Color.blue);
	    EllipsRoi2.setStrokeWidth(5);
	    Overlay Eloverlay2 = new Overlay(EllipsRoi2); 
	    EllipsOverlay.setOverlay(Eloverlay2); 
	    EllipsOverlay = EllipsOverlay.flatten();  
     	EllipsRoi3.setStrokeColor(Color.blue);
	    EllipsRoi3.setStrokeWidth(5);
	    Overlay Eloverlay3 = new Overlay(EllipsRoi3); 
	    EllipsOverlay.setOverlay(Eloverlay3); 
	    EllipsOverlay = EllipsOverlay.flatten(); 
     	EllipsRoi4.setStrokeColor(Color.blue);
	    EllipsRoi4.setStrokeWidth(5);
	    Overlay Eloverlay4 = new Overlay(EllipsRoi4); 
	    EllipsOverlay.setOverlay(Eloverlay4); 
	    EllipsOverlay = EllipsOverlay.flatten(); 
	    
	    currentEllips.close();

	    IJ.log("Calculate density inside rectangles");
	  //1000*calcDensiteRectangle(-0.25*extension,0.0*profondeur,0.25*extension,0.2*profondeur)/extension;
	    ImagePlus currentRectangle = currentImage.duplicate();
	    ImageProcessor rect = currentRectangle.getProcessor();
	    rect.setAutoThreshold("Li");
	    currentRectangle.setProcessor(rect);
	    Roi RectangleRoi = new Roi((Xmid - 0.25*width),Ymid,0.5*width,0.2*depth);
	    currentRectangle.setRoi(RectangleRoi);      
	    Analyzer.setResultsTable(rt2);
	    rt2.reset();
	    pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA, rt2, 0, 10e9, 0, 1);
		pa.analyze(currentRectangle);
		float ar1 = 0;
		for(int i = 0; i < rt2.getCounter(); i++){
			ar1 += (float) rt2.getValue("Area", i);			
		}
	    a11 = 1000*ar1/width;
	    
	    //1000*calcDensiteRectangle(-0.25*extension,0.2*profondeur,0.25*extension,0.4*profondeur)/extension;
	    Roi RectangleRoi2 = new Roi(Xmid - 0.25*width,Ymid+0.2*depth,0.5*width,0.2*depth);
	    currentRectangle.setRoi(RectangleRoi2);
	    Analyzer.setResultsTable(rt2);
	    rt2.reset();
	    pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA, rt2, 0, 10e9, 0, 1);
		pa.analyze(currentRectangle);
		float ar2 = 0;
		for(int i = 0; i < rt2.getCounter(); i++){
			ar2 += (float) rt2.getValue("Area", i);			
		}
	    a12 = 1000*ar2/width;
	    
	    //1000*calcDensiteRectangle(-0.25*extension,0.4*profondeur,0.25*extension,0.6*profondeur)/extension;
	    Roi RectangleRoi3 = new Roi(Xmid - 0.25*width,Ymid+0.4*depth,0.5*width,0.2*depth);
	    currentRectangle.setRoi(RectangleRoi3);
	    Analyzer.setResultsTable(rt2);
	    rt2.reset();
	    pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA, rt2, 0, 10e9, 0, 1);
		pa.analyze(currentRectangle);
		float ar3 = 0;
		for(int i = 0; i < rt2.getCounter(); i++){
			ar3 += (float) rt2.getValue("Area", i);			
		}
	    a13 = 1000*ar3/width;
	    
	    //Create rectangles overlay
	    IJ.log("Create Rectangle overlay");
     	ImagePlus RectangleOverlay = currentImage.duplicate();
     	RectangleRoi.setStrokeColor(Color.blue);
	    RectangleRoi.setStrokeWidth(5);
	    Overlay overlay = new Overlay(RectangleRoi); 
	    RectangleOverlay.setOverlay(overlay); 
	    RectangleOverlay = RectangleOverlay.flatten(); 	    
     	RectangleRoi2.setStrokeColor(Color.blue);
	    RectangleRoi2.setStrokeWidth(5);
	    Overlay overlay2 = new Overlay(RectangleRoi2); 
	    RectangleOverlay.setOverlay(overlay2); 
	    RectangleOverlay = RectangleOverlay.flatten(); 
	    RectangleRoi3.setStrokeColor(Color.blue);
	    RectangleRoi3.setStrokeWidth(5);
	    Overlay overlay3 = new Overlay(RectangleRoi3); 
	    RectangleOverlay.setOverlay(overlay3); 
	    RectangleOverlay = RectangleOverlay.flatten();
	    
	    currentRectangle.close();
	    
	    //dcalcIndicVerticalite(0.9f);
	    IJ.log("Calculate directionality");
	    ImagePlus currentDirection = currentImage.duplicate();
	    ImageProcessor direction = currentDirection.getProcessor();
	    direction = direction.resize(direction.getWidth()/2, direction.getHeight()/2);
	    direction.setAutoThreshold("Li");
	    direction.setRoi(new OvalRoi(Xmid - 0.45*width,Ymid,0.9*width,0.9*depth));
	    currentDirection.setProcessor(direction);	 
	    Directionality dnlty = new Directionality();
		
		// Set fields and settings
		int nbins = 10;
		int binStart = -90;
		ImagePlus img = new ImagePlus();
		img.setProcessor(direction.duplicate().rotateLeft());
		dnlty.setImagePlus(img);
		dnlty.setMethod(Directionality.AnalysisMethod.FOURIER_COMPONENTS);
		dnlty.setBinNumber(nbins);
		dnlty.setBinStart(binStart);
		dnlty.setBuildOrientationMapFlag(true);
		
		// Do calculation
		dnlty.computeHistograms();
		ResultsTable rs = dnlty.displayResultsTable();
		double angle = 0;
		double tot = 0;
		for(int k = 0; k < rs.getCounter(); k++){
			for(int l = 0; l < (rs.getValueAsDouble(1, k)); l++){
				double direct = rs.getValueAsDouble(0, k);
				double prop = rs.getValueAsDouble(1, k);
				angle += Math.abs(direct)*prop;
				tot += prop;
			}
		}
		
		ImagePlus img2 = new ImagePlus("Orientation map", dnlty.getOrientationMap());
		img2.setProcessor(img2.getProcessor().duplicate().rotateRight());
	    a14=(float) ((float) angle/tot);
		rs.reset();
		
		img.close();
		currentDirection.close();
		
	    //calcLongBlancsEntreEllipses(0.001f, 0.25f);
	    IJ.log("Calculate length blancs");
	    ImagePlus currentLength1 = currentImage.duplicate();
	    OvalRoi oval = new OvalRoi(Xmid - 0.0005*width,Ymid,0.001*width,0.001*length);
	    ImageProcessor length1 = currentLength1.getProcessor();
	    length1.setColor(255);
	    length1.fill(oval);
	    length1.invert();
	    length1.setAutoThreshold("Mean");
	    currentLength1.setProcessor(length1);
	    currentLength1.setRoi(new OvalRoi(Xmid - 0.125*width,Ymid,0.25*width,0.25*depth));     
	    Analyzer.setResultsTable(rt2);
	    rt2.reset();
	    pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.FERET, rt2, 0, 10e9, 0, 1);
	    pa.analyze(currentLength1);
	    float feret1 = 0;
	    int number1 = rt2.getCounter();
	    ImagePlus length1Overlay = currentLength1.duplicate();
		for(int i = 0; i < rt2.getCounter(); i++){
			feret1 += (float) rt2.getValue("Feret", i);	
			double x1 = rt2.getValue("FeretX", i);
			double y1 = rt2.getValue("FeretY", i);
			double L = rt2.getValue("Feret", i);
			double D = rt2.getValue("FeretAngle", i);
			  if (D>90)
			     D -= 180; 
			  double A = D*Math.PI/180;
			  double x2 = x1 + Math.cos(A)*L;
			  double y2 = y1 - Math.sin(A)*L;
			  Line ell6 = new Line(x1,y1,x2,y2);
			  ell6.setStrokeColor(Color.blue);
			  ell6.setStrokeWidth(5);
			  Overlay lline = new Overlay(ell6); 
			  length1Overlay.setOverlay(lline); 
			  length1Overlay = length1Overlay.flatten(); 
		}
	    a15=feret1/number1;
	    
	    currentLength1.close();
	    
	    //d15=calcLongBlancsEntreEllipses(0.25f, 0.50f);
	    ImagePlus currentLength2 = currentImage.duplicate();
	    OvalRoi oval2 = new OvalRoi(Xmid - 0.125*width,Ymid,0.25*width,0.25*depth);
	    ImageProcessor length2 = currentLength2.getProcessor();
	    length2.setColor(255);
	    length2.fill(oval2);
	    currentLength2.setProcessor(length2);
	    length2.invert();
	    length2.setAutoThreshold("Mean");
	    currentLength2.setProcessor(length2);
	    currentLength2.setRoi(new OvalRoi(Xmid - 0.25*width,Ymid,0.5*width,0.5*depth)); 
	    Analyzer.setResultsTable(rt2);
	    rt2.reset();
	    pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.FERET, rt2, 0, 10e9, 0, 1);
	    pa.analyze(currentLength2);
	    float feret2 = 0;
	    int number2 = rt2.getCounter();
	    ImagePlus length2Overlay = currentLength2.duplicate();
		for(int i = 0; i < rt2.getCounter(); i++){
			feret2 += (float) rt2.getValue("Feret", i);		
			double x1 = rt2.getValue("FeretX", i);
			double y1 = rt2.getValue("FeretY", i);
			double L = rt2.getValue("Feret", i);
			double D = rt2.getValue("FeretAngle", i);
			  if (D>90)
			     D -= 180; 
			  double A = D*Math.PI/180;
			  double x2 = x1 + Math.cos(A)*L;
			  double y2 = y1 - Math.sin(A)*L;
			  Line ell7 = new Line(x1,y1,x2,y2);
			  ell7.setStrokeColor(Color.blue);
			  ell7.setStrokeWidth(5);
			  Overlay lline = new Overlay(ell7); 
			  length2Overlay.setOverlay(lline); 
			  length2Overlay = length2Overlay.flatten(); 
			 ;
			
		}
	    a16=feret2/number2;
	    
	    currentLength2.close();
	    
	    /**
		    * Compute the image skeleton and find its tips. 
		    */
	    IJ.log("count Tips");
			   ImagePlus Tips = currentImage.duplicate();
			   ImageProcessor ip = Tips.getProcessor();
			   ip.autoThreshold();
			   BinaryProcessor bp = new BinaryProcessor(new ByteProcessor(ip, true));
			   bp.skeletonize();	   
			   Tips.setProcessor(bp);
			   int nTips = 0;
			   for(int w = 0; w < bp.getWidth(); w++){
				   for(int h = 0; h < bp.getHeight(); h++){			   
					   if(bp.get(w, h) > 125){
						   int n = nNeighbours(bp, w, h);
						   if(n == 1){
							   nTips += 1;
						   }
					   }
				   }   
			   }
			   a17=nTips;
			   
		Tips.close();
			   
		//count roots differently
		IJ.log("max and mean pixel count");
		float sum = 0;
		float count = 0;
		float max = 0;
		ImagePlus Tips2 = currentImage.duplicate();
		IJ.run(Tips2, "Make Binary", "");
		IJ.run(Tips2, "Skeletonize", "");
		int h = (int) depth;
		int w = (int) width;
		
		
		for(int i = (h/4)-2; i > 0; i = i-1){
			Rectangle Rect2 = new Rectangle(0, i, w, 1);
			Tips2.setRoi(Rect2);
			Analyzer.setResultsTable(rt2);
			rt2.reset();
			an = new Analyzer(Tips2, Measurements.AREA | Measurements.AREA_FRACTION, rt2);
		    an.measure();
		    tot = (rt2.getValue("%Area", 0) * rt2.getValue("Area", 0)) / 100;;
			if(tot > 0){
				sum += (float) tot;
				count++;
			}
			if(tot > max) max = (float) tot;
		}
		a18 = sum/count;
		a19 = max;
		
		sum = 0;
		count = 0;
		max = 0;
		
		for(int i = (h/2)-2; i > (h/4); i = i-1){
			Rectangle Rect2 = new Rectangle(0, i, w, 1);
			Tips2.setRoi(Rect2);
			Analyzer.setResultsTable(rt2);
			rt2.reset();
			an = new Analyzer(Tips2, Measurements.AREA | Measurements.AREA_FRACTION, rt2);
		    an.measure();
		    tot = (rt2.getValue("%Area", 0) * rt2.getValue("Area", 0)) / 100;;
			if(tot > 0){
				sum += (float) tot;
				count++;
			}
			if(tot > max) max = (float) tot;
		}
		a20 = sum/count;
		a21 = max;
		
		sum = 0;
		count = 0;
		max = 0;
		
		for(int i = ((h/4)*3)-2; i > (h/2); i = i-1){
			Rectangle Rect2 = new Rectangle(0, i, w, 1);
			Tips2.setRoi(Rect2);
			Analyzer.setResultsTable(rt2);
			rt2.reset();
			an = new Analyzer(Tips2, Measurements.AREA | Measurements.AREA_FRACTION, rt2);
		    an.measure();
		    tot = (rt2.getValue("%Area", 0) * rt2.getValue("Area", 0)) / 100;;
			if(tot > 0){
				sum += (float) tot;
				count++;
			}
			if(tot > max) max = (float) tot;
		}
		a22 = sum/count;
		a23 = max;
		
		sum = 0;
		count = 0;
		max = 0;
		
		for(int i = h-2; i > ((h/4)*3); i = i-1){
			Rectangle Rect2 = new Rectangle(0, i, w, 1);
			Tips2.setRoi(Rect2);
			Analyzer.setResultsTable(rt2);
			rt2.reset();
			an = new Analyzer(Tips2, Measurements.AREA | Measurements.AREA_FRACTION, rt2);
		    an.measure();
		    tot = (rt2.getValue("%Area", 0) * rt2.getValue("Area", 0)) / 100;;
			if(tot > 0){
				sum += (float) tot;
				count++;
			}
			if(tot > max) max = (float) tot;
		}
		a24 = sum/count;
		a25 = max;
		
		sum = 0;
		count = 0;
		max = 0;
		
		for(int i = w-2; i > 0; i = i-1){
			Rectangle Rect2 = new Rectangle(i, 0, 1, h);
			Tips2.setRoi(Rect2);
			Analyzer.setResultsTable(rt2);
			rt2.reset();
			an = new Analyzer(Tips2, Measurements.AREA | Measurements.AREA_FRACTION, rt2);
		    an.measure();
		    tot = (rt2.getValue("%Area", 0) * rt2.getValue("Area", 0)) / 100;;
			if(tot > 0){
				sum += (float) tot;
				count++;
			}
			if(tot > max) max = (float) tot;
		}
		a26 = sum/count;
		a27 = max;
		
		Tips2.close();
		
		ImagePlus number = currentImage.duplicate();
	    IJ.run(number, "Make Binary", "");
		Analyzer.setResultsTable(rt2);
		rt2.reset();
		pa = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET, Measurements.AREA, rt2, 1, 10e9, 0, 1);
		pa.analyze(number);
		a28 = (float) rt2.getCounter();
		a29 = a28/depth;
		
		number.close();
		
		//get bounding box
        IJ.log("Make bounding box");
        ImagePlus coord = currentImage.duplicate();
        IJ.run(coord, "Make Binary", "");
        IJ.run(coord, "Create Selection", "");
        Roi select;
        select = coord.getRoi();
        ImageProcessor Shape = coord.getProcessor();
        Shape.setRoi(select.getBounds());
        Shape = Shape.crop();
        coord.setProcessor(Shape);
        double w1 = coord.getWidth();
        double h1 = coord.getHeight();
        int nLayer = 5;
        int m = 2*nLayer;
        float[] xp = new float[m];
        
        //Calculate coordinates
        IJ.log("Get coordinates");
        // Make rectangle (for each rectangle) 
        // Get bounding box of rectangle
		// Get coordinates of bounding box
		// Save coordinates
        for(int i = 0; i< nLayer; i++){
        ImagePlus currentSelection = coord.duplicate();
        float factor = (float) i/(nLayer-1);
        
        if(i == 0){currentSelection.setRoi(new Roi(0, Ymid, w1, 3));
        }
        else if(i == (nLayer-1)){currentSelection.setRoi(new Roi(0, 0.99*h1, w1, 5));
        }
        else{currentSelection.setRoi(new Roi(0, factor*h1, w1, 3));
        }
        
        ImageProcessor small = currentSelection.getProcessor();
        small = small.crop();
        small.setAutoThreshold("Li");
        currentSelection.setProcessor(small);	
        
        IJ.run(currentSelection, "Create Selection", "");        
        Analyzer.setResultsTable(rt2);
        rt2.reset();
        an = new Analyzer(currentSelection, Measurements.RECT, rt2);
        an.measure();
        X1 = (float) rt2.getValue("BX", 0);
     	xp[i] = (float)X1;
     	
        double wid0 = rt2.getValue("Width", 0);
        X2 = (float) wid0;
        xp[nLayer+i] = (float)X2;
        
        }
        a30=xp[0]; a31=xp[1]; a32=xp[2]; a33=xp[3]; a34=xp[4]; a35= xp[5]; a36 = xp[6]; a37=xp[7]; a38=xp[8]; a39=xp[9];
        coord.close();
        
	    // Save the images for post-processing check
 		//IJ.save(currentImage, dirParam.getAbsolutePath()+"/"+baseName+name+"_simple.tiff");
        //IJ.save(RectangleOverlay, dirParam.getAbsolutePath()+"/"+baseName+name+"_rectangles.tiff");
 		//IJ.save(EllipsOverlay, dirParam.getAbsolutePath()+"/"+baseName+name+"_ellipses.tiff");
 		//IJ.save(img2, dirParam.getAbsolutePath()+"/"+baseName+name+"_direction.tiff");
 		//IJ.save(length1Overlay, dirParam.getAbsolutePath()+"/"+baseName+name+"_blancs1.tiff");
 		//IJ.save(length2Overlay, dirParam.getAbsolutePath()+"/"+baseName+name+"_blancs2.tiff");
 		
 		RectangleOverlay.close(); EllipsOverlay.close(); img2.close(); length1Overlay.close();
 		length2Overlay.close();
 				
	}

	 /**
	    * Compute the number of black neigbours for a point
	    * @param bp
	    * @param w
	    * @param h
	    * @return
	    */
	   
		   private int nNeighbours(ImageProcessor bp, int w, int h){
			   int n = 0;
			   for(int i = w-1; i <= w+1; i++){
				   for(int j = h-1; j <= h+1; j++){
					   if(bp.getPixel(i, j) > 125) n++;
					   if(n == 3) return n-1;
				   }
			   }
			   return n-1;
		   }	
 
	
	/**
	 * Print Image ID
	 */
	private void printCoordCSVid(int type){	
		pwCoord.println("ID=" + baseName ); 
		pwCoord.println("LM=" + ((nCoord/2))*2);
		pwCoord.flush();
	}
	
	/**
	 * Send coordinates data to a TPS file
	 */
	private void sendCoordDataToCSV(int type){	
		pwCoord.println(X1 + " " + Y1);
		pwCoord.flush();
	}	
	
	/**
	 * Print Parameters CSV header
	 */
	private void printParamCSVHeader(int type){	
		if(type == NO_NAME) {//pwParam.println("image, d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13, d14, d15, d16, d17, d18, d19, d20, d21, d22, d23, d24, d25");	
		pwParam.println("image, areaTotal, width, depth, ratio w/d, CoM X, CoM Y, Densell1, Densell2, Densell3, Densell4, DensRect1, DensRect2, DensRect3, Directionality, LengthBlancs1, LengthBlancs2, nTips, meanhor4, maxhor4, meanhor3, maxhor3, meanhor2, maxhor2, meanhor1, maxhor1, meanvert, maxvert, number, numberdensity, X1,X2,X3,X4,X5,X6,X7,X8,X9,X10, areaPrim, width, depth, ratio w/d, CoM X, CoM Y, Densell1, Densell2, Densell3, Densell4, DensRect1, DensRect2, DensRect3, Directionality, LengthBlancs1, LengthBlancs2, nTips, meanhor4, maxhor4, meanhor3, maxhor3, meanhor2, maxhor2, meanhor1, maxhor1, meanvert, maxvert, number, numberdensity, X1,X2,X3,X4,X5,X6,X7,X8,X9,X10, areaLat, width, depth, ratio w/d, CoM X, CoM Y, Densell1, Densell2, Densell3, Densell4, DensRect1, DensRect2, DensRect3, Directionality, LengthBlancs1, LengthBlancs2, nTips, meanhor4, maxhor4, meanhor3, maxhor3, meanhor2, maxhor2, meanhor1, maxhor1, meanvert, maxvert, number, numberdensity, X1,X2,X3,X4,X5,X6,X7,X8,X9,X10, EDMmax, LatDiam");}
		else pwParam.println("experiment , stock , treatment , pot , das , d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13, d14, d15, d16, d17, d18, d19, d20, d21, d22, d23, d24, d25");
		pwParam.flush();
	}

	/**
	 * Send parameters data to an CSV file
	 */
	private void sendParametersToCSV(int type, int id){	
		if(type == NO_NAME) pwParam.println(baseName +","+ d1 +","+ d2 +","+ d3 +","+ d4 +","+ d5 +","+ d6 + "," + d7 + ","+ d8 +","+ d9 +","+ d10 +","+ d11 +","+ d12 + "," + d13 + "," + d14 + "," + d15 + "," + d16 + "," + d17 + "," + d18 + "," + d19 + "," + d20 + "," + d21 + "," + d22 + "," + d23 + "," + d24 + "," + d25 + "," + d26 
				+ "," + d27 + "," + d28 + "," + d29 + "," + d30 + "," + d31 + "," + d32 + "," + d33 +","+ d34 +","+ d35 +","+ d36 + "," + d37 + ","+ d38 +","+ d39 + 
				","+ d41 +","+ d42 +","+ d43 +","+ d44 +","+ d45 +","+ d46 + "," + d47 + ","+ d48 +","+ d49 +","+ d50 + "," + d51 +","+ d52 +","+ d53 +","+ d54 +","+ d55 +","+ d56 + "," + d57 + ","+ d58 +","+ d59 +","+ d60
				+ ","+ d61 +","+ d62 +","+ d63 +","+ d64 +","+ d65 +","+ d66 + "," + d67 + ","+ d68 +","+ d69 +","+ d70 + "," + d71 +","+ d72 +","+ d73 +","+ d74 +","+ d75 +","+ d76 + "," + d77 + ","+ d78 +","+ d79 
				+ ","+ d81 +","+ d82 +","+ d83 +","+ d84 +","+ d85 +","+ d86 + "," + d87 + ","+ d88 +","+ d89 +","+ d90 + "," + d91 +","+ d92 +","+ d93 +","+ d94 +","+ d95 +","+ d96 + "," + d97 + ","+ d98 +","+ d99 +","+ d100 + "," + 
				d101 +","+ d102 +","+ d103 +","+ d104 +","+ d105 +","+ d106 + "," + d107 + ","+ d108 +","+ d109 +","+ d110 + "," + d111 +","+ d112 +","+ d113 +","+ d114 +","+ d115 +","+ d116 + "," + d117 + ","+ d118 +","+ d119 + "," + d121 + "," + d122);
		else pwParam.println(experiment +","+ stock +","+ treatment +","+ pot +","+ DAS + "," + d1 +","+ d2 +","+ d3 +","+ d4 +","+ d5 +","+ d6 + "," + d7 +","+ d8 +","+ d9 +","+ d10 +","+ d11 +","+ d12 + "," + d13 + "," + d14 + "," + d15 + "," + d16 + "," + d17 + "," + d18 + "," + d19 + "," + d20 + "," + d21 + "," + d22 + "," + d23 + "," + d24 + "," + d25);
		pwParam.flush();
	}
	/**
	 * Print Local CSV header
	 */
	private void printLocalCSVHeader(int type){	
		if(type == NO_NAME) pwLocal.println("image, length, angle, vector_length, tortuosity, x, y");			
		else pwLocal.println("experiment , stock , treatment , pot , das , length, angle, vector_length, tortuosity, x, y");
		pwLocal.flush();
	}
	/**
	 * Send local data to an CSV file
	 */
	private void sendLocalDataToCSV(int type, int id){	
		if(type == NO_NAME) pwLocal.println(baseName +","+ length +","+ angle +","+ feret+","+ tortuosity+","+ rx +","+ ry);
		else pwLocal.println(experiment +","+ stock +","+ treatment +","+ pot +","+ DAS +","+ length +","+ angle +","+ feret+","+ tortuosity+","+ rx +","+ ry);
		pwLocal.flush();
	}
	
	
	/**
	 * Print EFD CSV header
	 */
	private void printEFDCSVHeader(int type){	
		pwEFD.println("image, index, ax, ay, bx, by, efd");			
		pwEFD.flush();
	}
	/**
	 * Send EFD data to an CSV file
	 */
	private void sendEFDDataToCSV(int i, double ax, double ay, double bx, double by, double efd){	
		pwEFD.println(baseName +","+ i +","+ ax +","+ ay +","+ bx+","+ by+","+ efd);
		pwEFD.flush();
	}	
	
	/**
	 * Print EFD CSV header
	 */
	private void printDirectionalCSVHeader(int type){	
		pwDir.println("image, index, angle");			
		pwDir.flush();
	}
	/**
	 * Send EFD data to an CSV file
	 */
	private void sendDirectionalDataToCSV(double i, double angle){	
		pwDir.println(baseName +","+ i +","+angle);
		pwDir.flush();
	}	
	/**
	 * Print Global CSV header
	 */
	private void printGlobalCSVHeader(int type){	
		if(type == NO_NAME) pwGlobal.println("image, area, convexhull, depth, width, circularity, ar, round, solidity, feret, feret_angle, massX, massY");			
		else pwGlobal.println("experiment , stock , treatment , pot , plant , das , area, diameter, convexHull, depth, width, circularity, ar, round, solidity, feret, feret_angle, massX, massY");
		pwGlobal.flush();
	}
	
	/**
	 * Send global data to an CSV file
	 */
	private void sendGlobalDataToCSV(int type){	
		if(type == NO_NAME) pwGlobal.println(baseName +","+ area +","+ convexHull+","+ depth+","+ width+","+ circ+","+ ar+","+ round+","+ solidity+","+ globalFeret+","+ globalAngle+","+ comX +","+ comY);
		else pwGlobal.println(experiment +","+ stock +","+ treatment +","+ pot +","+ DAS +","+ area +","+ convexHull+","+ depth+","+ width+","+ circ+","+ ar+","+ round+","+ solidity+","+ globalFeret+","+ globalAngle+","+ comX +","+ comY);
		pwGlobal.flush();
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
	
	
   /**
    * Image filter	
    * @author guillaumelobet
    */
	public class ImageFilter extends javax.swing.filechooser.FileFilter{
		public boolean accept (File f) {
			if (f.isDirectory()) {
				return true;
			}

			String extension = getExtension(f);
			if (extension != null) {
				if (extension.equals("jpg") || extension.equals("png") ||
						extension.equals("tif") || extension.equals("tiff") || extension.equals("jpeg")) return true;
				else return false;
			}
			return false;
		}
	     
		public String getDescription () {
			return "Image files (*.jpg, *png, *tiff)";
		}
	      
		public String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');
			if (i > 0 &&  i < s.length() - 1) {
				ext = s.substring(i+1).toLowerCase();
			}
			return ext;
		}
	}	
	
}
