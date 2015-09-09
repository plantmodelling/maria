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

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.BackgroundSubtracter;
import ij.plugin.filter.RGBStackSplitter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

public class PetriProcessor {
	
	File dir, dir0, dir1, dir2;	
	File[] images; 		
		
	String imName1, imName2, currentDate, startingDate;
	String exp, gen, tr, pot; 
	
	long DAS;
	int pl, plID, nROI, threshold;
	float surface, convexHull, diameter;
	boolean rotate, remove;
	
	RoiManager roiM;
	Roi[] allROI;
	
	ImagePlus ip, iB, iG, ipM;
	ImageProcessor ipCH, ipSD;
		
	RGBStackSplitter splitter = new RGBStackSplitter();
	ImageCalculator ic = new ImageCalculator();	
	BackgroundSubtracter bs = new BackgroundSubtracter();
	PolygonRoi pROI, chROI;
	
	
	
	/**
	 * Constructor for the QR processor
	 * @param f
	 * @param date
	 * @param sf
	 * @param scaleP
	 * @param scaleC
	 */
	public PetriProcessor(File f, String date, boolean rot, boolean rem){
		
		// Set the different parameters
		startingDate = date;
		dir = f;
		rotate = rot;
		remove = rem;
		
        // Create the different directories
		images = dir.listFiles();
		IJ.log("images = "+images.length);
		for(int i = 0; i< images.length; i++) if(images[i].isHidden()) images[i].delete();
		images = dir.listFiles();
		dir0 = new File(dir.getAbsolutePath()+"/raw_images/");
		dir1 = new File(dir.getAbsolutePath()+"/sorted_images/");
		try{ dir0.mkdir(); dir1.mkdir();}
		catch(Exception e){}
		
		IJ.log("Petri processing started: "+dir.getAbsolutePath().toString());
		
		// Set the different ROI		
		roiM = new RoiManager();
		roiM.setVisible(true);
		
		ip = IJ.openImage(images[0].getAbsolutePath());
		ip.show();
	}	
	
	/**
	 * Process all the images in the folder
	 * @return
	 */
	public boolean processAllImages(){

		long startD = System.currentTimeMillis();
		int counter = 0;
		
		// Get the ROI
		ip.flush(); ip.close();
		
		nROI = roiM.getCount();	
		allROI = roiM.getRoisAsArray();
		roiM.close();
				
		IJ.log("Could not recognize the following images: ");
		
		for(int i = 0; i < images.length; i++){
			ip = IJ.openImage(images[i].getAbsolutePath());
			IJ.save(ip, dir0.getAbsolutePath()+"/"+images[i].getName());
			images[i].delete();
			
			// Get the QR code
			ip.setRoi(allROI[0]);
			ipM = ip.duplicate(); 
			String qr = Util.getQR(ipM);
			if(!qr.endsWith("xx")) processQR(qr);
			currentDate = images[i].getName().substring(0, 10);
			DAS = Util.getDAS(startingDate, currentDate);
			if(!qr.endsWith("xx")){
				imName1 = "EXP"+exp+"_GEN"+gen+"_TR"+tr+"_BOX"+pot+"_DAS"+DAS+".tif";
				imName2 = "EXP"+exp+"_GEN"+gen+"-WT_TR"+tr+"_BOX"+pot+"_DAS"+DAS+".tif";
			}
			else{
				imName1 = images[i].getName()+"_DAS"+DAS+".tif";
				imName2 = images[i].getName()+"-WT"+"_DAS"+DAS+".tif";
				//imName1 = qr+"_DAS"+DAS+".tif";
				//imName2 = "WT"+qr+"_DAS"+DAS+".tif";
			}
			ipM.flush(); ipM.close();
			
			
			// Get the mutant
			ip.setRoi(allROI[1]);
			ipM = ip.duplicate(); 
			processImage(ipM, dir1.getAbsolutePath()+"/"+imName1);
			ipM.flush(); ipM.close();
			counter++;

			// Get the wild type
			ip.setRoi(allROI[2]);
			ipM = ip.duplicate(); 
			processImage(ipM, dir1.getAbsolutePath()+"/"+imName2);
			ipM.flush(); ipM.close();
			counter++;
			
			ip.flush(); ip.close();
		}
		
		long endD = System.currentTimeMillis();
		IJ.log(counter+" images analyzed in "+(endD - startD)/1000+" s");
		return true;
	}	
	
	/**
	 * Process the QR code string to retreive the plant data
	 * QR string as to be like "EXP1_GEN2_TR1_BOX5"
	 * @param s
	 */
	private void processQR(String s){
		
		IJ.log(s);
		
		String qr = s;	
		int l = qr.length();
		int cut1, cut2;

		cut2 = qr.indexOf("_");
		cut1 = qr.indexOf("P");
		exp = qr.substring(cut1+1, cut2);
		String qrNew = qr.substring(cut2+1, l);
		
		cut2 = qrNew.indexOf("_");
		cut1 = qrNew.indexOf("N");
		gen = qrNew.substring(cut1+1, cut2);
		qrNew = qrNew.substring(cut2+1, qrNew.length());
		
		cut2 = qrNew.indexOf("_");
		cut1 = qrNew.indexOf("R");
		tr = qrNew.substring(cut1+1, cut2);
		qrNew = qrNew.substring(cut2+1, qrNew.length());
		
		cut2 = qrNew.indexOf("_");
		cut1 = qrNew.indexOf("X");
		pot = qrNew.substring(cut1+1, qrNew.length());
	}    
	
	/**
	 * Process the image to make it more analysable (if thats a word :)-) with SmartRoot
	 * @param im
	 */
	private void processImage(ImagePlus im, String file){
		
//		ImagePlus iG, iR, im1, im2, im3;
		ImagePlus im1, im2, im3;
//		ImageProcessor ipR, ip1, ip2, ip3;
		ImageProcessor ip1, ip2, ip3;
		
		int mean = (int) im.getStatistics().mean / 2;
		IJ.log("mean = "+mean);
		
//		ip1 = im.getProcessor().duplicate();
//		im1 = new ImagePlus(); im1.setProcessor(ip1);
//		
//		ip2 = im.getProcessor().duplicate();
//		im2 = new ImagePlus(); im2.setProcessor(ip2);
//		
		im1 = im.duplicate(); im2 = im.duplicate();
		
		ImageConverter iConv = new ImageConverter(im1);
		iConv.convertToGray8();		
		ip1 = im1.getProcessor(); ip1.subtract(mean); ip1.invert(); im1.setProcessor(ip1);
		
		ImageConverter iConv2 = new ImageConverter(im2);
		iConv2.convertToGray8();		
		ip2 = im2.getProcessor(); ip2.subtract(mean); im2.setProcessor(ip2);

//		// Process the color channels
//		splitter.split(ipM.getStack(), true);
//		iR = new ImagePlus("red", splitter.red.getProcessor(1));
//		ipR = iR.getProcessor();
//		ipR.invert(); iR.setProcessor(ipR);
//		iG = new ImagePlus("green", splitter.green.getProcessor(1));
//		im1 = ic.run("Substract create", iR, iG);
//		iR.flush(); iR.close(); iG.flush(); iG.close();
					
		im3 = new ImagePlus();
		im3 = ic.run("Substract create", im1, im2);
		ip3 = im3.getProcessor();

		
		// Improve the image
		ip3.smooth(); ip3.smooth();
		if(remove) bs.rollingBallBackground(ip3, 100, false, true, false, false, false);
		ip3.smooth(); ip3.smooth();
		if(rotate) ip3.rotate(180);

		im3.setProcessor(ip3);
		
		IJ.save(im3, file);
	}
	
	
}
