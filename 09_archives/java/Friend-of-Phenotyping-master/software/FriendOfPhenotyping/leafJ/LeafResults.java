package leafJ;
//leaf results class
//uses TextPanel to display results of measurements.

import ij.*;
import ij.text.*;
import ij.plugin.frame.*;
import ij.plugin.filter.*;
import ij.measure.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;

public class LeafResults {
	private TextPanel tp;
	private TextWindow tw;
	private int verbose = 1;
	
	public LeafResults(String title) {
		//determine if there is already a text panel with this title
		Frame f = WindowManager.getFrame(title);
		if (f==null || !(f instanceof TextWindow)) {
			if (verbose > 1) IJ.log("need to create new text panel");
			tw = new TextWindow(title,"",800,600);
		} else {
			tw = (TextWindow) f;
		}
		tp = tw.getTextPanel();
	}
	
	public void setHeadings(String headings) {
		//to keep track:               0             1           2          3              4             5            6
		for (String j : new String[] {"leaf","petioleLength","bladeLength","bladeWidth","bladeArea","bladePerimeter","bladeCircularity"}) {
				headings = headings + "\t" + j;
		}
		tp.updateColumnHeadings(headings.trim());
	}
	
	public void addResults (sampleDescription sd, RoiManager rm, ImageProcessor ip, ImagePlus imp) {
		//go through ROI manager an ROI at a time
		//add to results.
		//one row per leaf
		//each row has the sample description, then the leaf number, then petiole length, blade length, width, area
		
		//safer:
		//populate a leaf array first, then write it to the table
		
		//EllipseFitter ef = new EllipseFitter();//not needed any more?
		
		if (verbose > 0) IJ.log("addResults");
		
		ResultsTable rt = new ResultsTable();	//will temporarily hold the results from the Blade Analyzer. 
		Analyzer petioleAnalyzer = new Analyzer(imp,  Measurements.PERIMETER , rt);
		Analyzer bladeAnalyzer = new Analyzer(imp, Measurements.AREA + Measurements.CIRCULARITY + Measurements.ELLIPSE + Measurements.PERIMETER + Measurements.LABELS, rt);
		
		Roi[] rois = rm.getRoisAsArray();
		

		String[][] data = new String[rois.length][7] ;	//this is probably longer than we need it but allows
								//for the possibility of each ROI being its own leaf
		for (int i = 0; i < rois.length; i++) { //loop through each object in the array
			if (verbose > 1) IJ.log("Add results ROI: " + IJ.d2s(i));
			rm.select(i);				//needed for Analyzer
			String[] name = rois[i].getName().split(" "); //Split the ROI name into components
			int leaf = new Integer(name[2]);
			if (verbose > 1) IJ.log("Add results leaf: " + IJ.d2s(leaf));			
			if (name[1].equals("Petiole")) {
				if (verbose > 1) IJ.log("Roi is a petiole");			
				data[leaf][0] = name[2]; //should be redundant!
				petioleAnalyzer.measure();
				data[leaf][1] = IJ.d2s(rt.getValue("Perim.",rt.getCounter()-1),3);
			}
			if (name[1].equals("Blade")) {
				if (verbose > 1) IJ.log("Roi is a blade");			
				data[leaf][0] = name[2];
				bladeAnalyzer.measure();
				if (verbose > 1) IJ.log("rt counter is " + IJ.d2s(rt.getCounter()));							
				data[leaf][2] = IJ.d2s(rt.getValue("Major",rt.getCounter()-1),3);
				data[leaf][3] = IJ.d2s(rt.getValue("Minor",rt.getCounter()-1),3);
				data[leaf][4] = IJ.d2s(rt.getValue("Area",rt.getCounter()-1),3);
				data[leaf][5] = IJ.d2s(rt.getValue("Perim.",rt.getCounter()-1),3);
				data[leaf][6] = IJ.d2s(rt.getValue("Circ.",rt.getCounter()-1),3);
			}
		}
		
		for (int i = 1; i < rois.length;i++) { //loop through the collected data and add it to the text panel
			if (verbose > 1) IJ.log("adding data to text panel, iteration " + IJ.d2s(i,0));
			if(data[i][2] == null) break;  //we have reached the end of the data
							//this is needed because the data[][] may be longer than the actual data.
			String tmp = sd.getDescription();			//will create a String to hold the line of data to add to text panel

			for(int k = 0; k < data[0].length; k++) {
				tmp = tmp + "\t" + data[i][k];
			}
			if (verbose > 1) IJ.log(tmp);
			tp.appendLine(tmp);
		}
		tw.toFront();
	}
		

				
				
/* 			arraycopy(sd,0,data,0,sd.length) // get sample description added to data array
			data[sd.length+1] = toString(i + 1); // place ROI number
			data[sd.length+2] =  */
			
			
			
	

		
		
	
	public void show() {
		tp.updateDisplay();
	}
}
