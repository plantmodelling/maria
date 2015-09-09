/**
* @author Guillaume Lobet | Université de Liège
* @date: 2013-01-30
* 
* This plugin open one or several images, screen them for plants and select a given number of plant based 
* on their size or diameter by removing the extremes (small and big).
* At the end of the analysis, the plugin display the original image with the plants to use highlighted in red.
* 
* 
**/


import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.RGBStackSplitter;
import ij.process.ImageProcessor;

public class ScreeningAnalysis {
	
	File[] dir;									// List of files to analyse 
	float[] value, valueOrdered;				// Size of the detected element
	int measure, minSize, iterations;			// Running parameters
	boolean mask;								// Dislay mask?
	ImagePlus im, iInit, iB, iG, iSD;			// Different images used during the analysis
	ImageProcessor ip, ipSD, ipInit;			// Different image processors used during the analysis
	ImageStack is = new ImageStack();			// 
	ImageStack isBW = new ImageStack();			//
	ResultsTable rt = new ResultsTable();		//

	float nDel;
	int ind, nInit;
	String[] sNames, names;
	
	/**
	 * Constructor
	 * @param f : path to the image file
	 * @param n	: number of plant to select
	 * @param meas : type of measurment to perfom (1 = surface, 2 = diameter)
	 * @param meth : method to use to select the data (1 = remove the extremes, 2 = minimize the variance)
	 * @param min : min particle size
	 * @param it : number of thining iterations
	 * @param m : display the mask stack of the screening
	 */
	public ScreeningAnalysis(File[] f, int meas, int min, int it, boolean m){
		dir = f;
		measure = meas;
		minSize = min;
		iterations = it;
		mask = m;
		
		// If any image is open, close it
		try{im.flush(); im.close(); iInit.flush(); iInit.close(); iB.flush(); iB.close(); iG.flush(); iG.close(); iSD.flush(); iSD.close();}
		catch(NullPointerException npe){}
	}	

	/**
	 * Analyse the image
	 */
	public int[][] process(boolean flag){
		
		RGBStackSplitter splitter = new RGBStackSplitter();
		ImageCalculator ic = new ImageCalculator();	
		ParticleAnalyzer pa;

		for(int i = 0; i < dir.length; i++){
		
			// Open the image and resize it
			im = IJ.openImage(dir[i].getAbsolutePath());
			ip = im.getProcessor();
			ipInit = ip.resize(ip.getWidth()/2, ip.getHeight()/2);
			iInit = new ImagePlus(); iInit.setProcessor(ipInit);
			im.flush(); im.close();
			
			// Add the image to the stack
			if(i == 0) is =  new ImageStack(iInit.getWidth(), iInit.getHeight());
			is.addSlice(dir[i].getName(), ipInit);

//			// Process the color channels
			splitter.split(iInit.getStack(), true);
			iB = new ImagePlus("blue", splitter.blue.getProcessor(1));
			iG = new ImagePlus("green", splitter.green.getProcessor(1));
			iSD = ic.run("Substract create", iG, iB);
			iSD.setTitle(dir[i].getName());
			iB.flush(); iB.close(); iG.flush(); iG.close();
			
			// Threshold the image
			ipSD = iSD.getProcessor();
			ipSD.threshold(2);
			ipSD.invert();
			
			for(int j = 0; j < iterations; j++) ipSD.erode();
			for(int j = 0; j < (iterations*4); j++) ipSD.dilate();
			for(int j = 0; j < iterations*2; j++) ipSD.erode();
			
			// Add the images to the BW stack
			if(i == 0) isBW =  new ImageStack(iSD.getWidth(), iSD.getHeight());
			isBW.addSlice(dir[i].getName(), ipSD);
			
			// Get the surface or diameter
			Analyzer.setResultsTable(rt);
			pa = new ParticleAnalyzer(ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES, 
					Measurements.AREA | Measurements.RECT | Measurements.ELLIPSE | Measurements.LABELS, 
					rt, minSize, 10e9);
			pa.analyze(iSD);
			iSD.flush(); iSD.close();
		}
		
		
		// Get the surface data
		nInit = rt.getCounter();
		ind = nInit-1;
		value = new float[nInit];
		names = new String[nInit];
		sNames = is.getSliceLabels();
		
		for(int i = 0; i < nInit; i++) names[i] = rt.getLabel(i);
		switch(measure){
			case 0: for(int i = 0; i < nInit; i++) value[i] = (float) rt.getValue("Area", i); break;
			case 1: for(int i = 0; i < nInit; i++) value[i] = (float) rt.getValue("Major", i);  break;
		}		
		valueOrdered = value.clone();
		Arrays.sort(valueOrdered);
		
		if(flag) return getSTDDecrease(value);
		else return null;
	}
	
	
	/**
	 * Analyse the image
	 * @param process
	 */
	public void analyse(boolean process, int nPlants){
		if(process) process(false);
		
		nDel = Math.max(nInit - nPlants, 0);


		double[] xPlot = new double[nInit];
		double[] yPlot = new double[nInit];
		for(int i = 0; i < nInit; i++){ 
			xPlot[i] = i+1;
			yPlot[i] = valueOrdered[i];
		}

		// Get the index of the plant to remove
		Vector<Float> keep;
				
		
		keep = minimizeSTD(value, (int) nDel);
		Vector<Float> v1 = new Vector<Float>();
				
		// Highlight the plants to keep
		for(int i = 0 ; i < nInit ; i++){
			if(keep.contains(value[i])){
				ImageProcessor pr = null;
						
				// Highlight in the color stack
				for(int j = 1; j <= is.getSize(); j++) if(sNames[j-1].equals(names[i])) pr = is.getProcessor(j);
				pr.setLineWidth(5);
				pr.setColor(Color.red);
				int x = (int) rt.getValue("BX", i), y = (int) rt.getValue("BY", i), w = (int) rt.getValue("Width", i), h =(int) rt.getValue("Height", i);
				pr.drawRect(x, y, w, h);
						
				// Highlight in the BW stack
				if(mask){
					for(int j = 1; j <= is.getSize(); j++) if(sNames[j-1].equals(names[i])) pr = isBW.getProcessor(j);
					pr.setLineWidth(4);
					pr.setColor(Color.black);
					pr.drawRect(x, y, w, h);
				}
						
				switch(measure){
					case 0: v1.add((float)rt.getValue("Area", i)); break;
					case 1: v1.add((float)rt.getValue("Major", i)); break;
				}
						
			}	
		}
				
		if(process){
			Plot pl1 = new Plot("All size distribution", "Plant", "Size [px]");
			pl1.setLimits(0, nInit, 0, yPlot[nInit-1]);
			pl1.addPoints(xPlot, yPlot, Plot.CIRCLE);
			pl1.setLineWidth(2);
			pl1.drawLine(0, Util.min(keep), nInit, Util.min(keep));
			pl1.setLineWidth(2);
			pl1.drawLine(0, Util.max(keep), nInit, Util.max(keep));
			pl1.show();
		}
				
		IJ.log("Average size = "+Util.avg(v1)+" +- "+Util.std(v1));
				//break;
		
		IJ.log("Number of plants found : "+nInit);
		IJ.log("Number of plants required : "+nPlants);
		IJ.log("Number of plants deleted : "+(int) nDel);
		IJ.log("------------------------------");

		im = new ImagePlus();
		im.setStack(is);
		im.show();
		
		if(mask){
			ImagePlus imBW = new ImagePlus();
			imBW.setStack(isBW);
			imBW.show();
		}
	}
	
	/**
	 * Remove r element from an array to minimize its variance
	 * @param vect
	 * @param r
	 * @return
	 */
	private Vector<Float> minimizeSTD(float[] vect, int r){
		
		IJ.log("total = "+vect.length+" / remove = "+r);
		
		// Create the array containing the element to keep
		Vector<Float> bestV = new Vector<Float>();
		for(int i = 0; i < vect.length; i++) bestV.add(vect[i]);		
		
		// Remove r element to the vector to minize the variance
		for(int i = 0; i < r; i ++){		
			Vector<Float> v = new Vector<Float>(bestV);
			float stdMin = Util.sum(v);		
			for(int j = 0; j < v.size(); j++){	
				Vector<Float> v1 = new Vector<Float>(v);
				v1.remove(j);
				if(Util.std(v1) < stdMin){
					stdMin = Util.std(v1);
					bestV = new Vector<Float>(v1);
				}
			}
		}
		return bestV;
	}
	
	/**
	 * Remove r element from an array to minimize its variance
	 * @param vect
	 * @param r
	 * @return
	 */
	private int[][] getSTDDecrease(float[] vect){
		
		int[][] var = new int[vect.length][4];
		
		// Create the array containing the element to keep
		Vector<Float> bestV = new Vector<Float>();
		for(int i = 0; i < vect.length; i++) bestV.add(vect[i]);		
		
		// Remove r element to the vector to minize the variance
		for(int i = 0; i < vect.length; i ++){		
			Vector<Float> v = new Vector<Float>(bestV);
			float stdMin = Util.sum(v);		
			for(int j = 0; j < v.size(); j++){	
				Vector<Float> v1 = new Vector<Float>(v);
				v1.remove(j);
				if(Util.std(v1) < stdMin){
					stdMin = Util.std(v1);
					bestV = new Vector<Float>(v1);
					var[i][0] = (int) ((stdMin / Util.avg(v1))*100);
					var[i][1] = (int) Util.min(bestV);
					var[i][2] = (int) Util.max(bestV);
					var[i][3] = (int) Util.avg(bestV);
					
				}
			}
		}
		return var;
	}
	
	
	public float[] getOrderedResults(){
		return valueOrdered;
	}

}
