package leafJ;
//Class to represent a single leaf and contain associated methods

//Modified from the hypocotyl class by Julin Maloof
//Oct 06, 2011


import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.frame.*;


public class leaf {
	public int verbose = 0;
	private int top;		//petiole top.  Should probably change name
	private int bottom;		//petiole bottom.
	private int width;		//width of leaf ROI
	private int height;		//height of leaf ROI
	private int ROI_top;		 //ROI boundaries 
	private int ROI_bottom;		 //ROI boundaries 
	private int ROI_left;		 //ROI boundaries 
	private int ROI_right;		 //ROI boundaries 
	
	private double angle;	 	//angle of major axis
	private double A;		//angle in radians
	
	private boolean right;		//is leaf leaning right?
	private boolean wide;		//is leaf oriented horizontally?
//	private boolean inverted;	//is leaf upside down (petiole above blade)?
	
	private double[] topLine;	//x1, y1, x2, y2, of line intersecting petiole top
	private double[] bottomLine;	//x1, y1, x2, y2, of line intersecting petiole bottom
		//but note bottom should be equivalent to the border of the ROI
		//(which border depends on orientation)
						

	
	private float[] leafCenter_x;
	private	float[] leafCenter_y;
		// will hold center positions of the leaf, starting from the bottom
		
	private float[] petiole_x;
	private float[] petiole_y;
		//will hold center positions of petiole only
	
	int arrayLength;
		
	private widthArrayLeaf widths;
	
	public void setLeaf(ResultsTable rt, int row, Calibration cal) { //set dimensions
		//this is based on the results of the particle finder
		//can this be done without passing the whole rt?
		if (verbose > 0) IJ.log("setLeaf");
		width = (int) cal.getRawX(rt.getValue("Width",row));
		height = (int) cal.getRawY(rt.getValue("Height",row));
		ROI_left = (int) cal.getRawX(rt.getValue("BX",row));
		ROI_top = (int) cal.getRawY(rt.getValue("BY",row));
		ROI_bottom = ROI_top + height;
		ROI_right = ROI_left + width;
		angle = rt.getValue("Angle",row);
		//Note: Angle of 0 = "3 O'clock"
		A = Math.toRadians(angle); //Angle of leaf from horizontal
		wide = (angle < 45 || angle > 135); //true if leaf is more horizontally oriented
		right = (angle < 90); //true if leaf is leaning to right
		if (wide) {
			arrayLength = (int) (width + Math.abs(Math.tan(A)*height));
			if (verbose > 0) IJ.log("wide: widths array extent = " + IJ.d2s(width + Math.abs(Math.tan(A)*height)));
		} else {
			arrayLength = (int) (height + Math.abs(width/Math.tan(A)));
			if (verbose > 0) IJ.log("tall: widths array extent = " + IJ.d2s(height + Math.abs(width/Math.tan(A))));
		}
		widths = new widthArrayLeaf(arrayLength);
		leafCenter_x = new float[arrayLength];
		leafCenter_y = new float[arrayLength];
		if (verbose > 0) {
			IJ.log("setHyp. ROI_left: "+
			IJ.d2s(ROI_left) + " ROI_top: " +
			IJ.d2s(ROI_top) + " w: " +
			IJ.d2s(width) + " h: " +
			IJ.d2s(height));
			IJ.log("wide: " + wide + " leaning right: " + right);
		} //if verbose

	}
	
	public int getTop() {
		return top;
	}
	
	public void scanLeaf(ImageProcessor ip) {
		//this determines widths and width center of the leaf
		//scan along the leaf with the scanline perpindicular 
		//to the axis of the leaf
		
		/*will march along the leaf at what is hopefully a 90 degree angle from the axis 
		of the leaf.  At each step calculate width and store it.
		*/
		ip.setColor(10);
		ip.setInterpolate(true);
		
		if(verbose > 0) {
			IJ.log("angle: " + IJ.d2s(angle) + " radians: " + IJ.d2s(A) + " tan(A): " + IJ.d2s(Math.tan(A)));
		}
		
		if (wide) {
			scanWide(ip);
		} else {
			scanTall(ip);
		}
		
	} //scanLeaf
	
	private void scanWide(ImageProcessor ip) {
		//scan along the leaf, one row at a time.
		double x1, x2, y1, y2;
		x1 = y1 = x2 = y2 = -1;
		//Will use some trig to calculate a line for scanning along leaf
		//x1,y1 and x2,y2 are start and end points of scan line
		
		int i = 0; //0 will represent beginning of scan
		
		int[] widthCenter = {0, -1};	 //see below.  to hold results of getWidth
		if (right) {//leaf leaning to right; scan from left to right
			x1 = x2 = ROI_left;
			y1 = y2 = ROI_bottom; 

			while(x2 < ROI_right  && i < arrayLength) {
				if (verbose > 1) IJ.log("first while, x2: " + IJ.d2s(x2));
				x2++;
				y1 = ROI_bottom - (x2-ROI_left)/Math.tan(A);
				if (y1 < ROI_top) { // outside of ROI!
					y1 = ROI_top;
					x1 = x2 - height*Math.tan(A);
				} //if y1 < ROI_top
				widthCenter = processScanLine(x1, y1, x2, y2, i, ip, widthCenter);
				i++;
			} //while x2
			while (x1 < ROI_right && y2 > ROI_top &&i < arrayLength) {
				if (verbose > 1) IJ.log("second while: x1, y2: " + IJ.d2s(x1) + " " + IJ.d2s(y2));
				//x1 and y2 need to advance
				x1++;
				y2 = ROI_top + (ROI_right-x1)/Math.tan(A);
				widthCenter = processScanLine(x1, y1, x2, y2, i, ip, widthCenter);
				i++;		
			} //while x1
		}  else { //leaf leaning to the left
			x1 = x2 = ROI_right;
			y1 = y2 = ROI_bottom;
			while (x2 >= ROI_left && i < arrayLength){
				//x1, y1 are now to the right of the leaf
				//start at bottom right
				//x2 moves left, y2 stays at the bottom
				//y1 moves up
				x2--;
				y1 = ROI_bottom + (ROI_right-x2)/Math.tan(A); //Math.tan(A) will be negative
				if (y1 < ROI_top) { //outside of ROI!
					//once y1 has moved to top, the x1 starts moving left
					y1 = ROI_top;
					x1 = x2 - height*Math.tan(A);//note tan(A) will be negative.
				} //y1 < ROI_left
				widthCenter = processScanLine(x1, y1, x2, y2, i, ip, widthCenter);
				i++;
			}//while x2
			while (x1 >= ROI_left && y2 > ROI_top && i < arrayLength) {
				//now x2,y2 should be at bottom left
				//x1 will move left, y2 should move up
				x1--;
				y2 = ROI_top - (x1-ROI_left)/Math.tan(A);
				widthCenter = processScanLine(x1, y1, x2, y2, i, ip, widthCenter);
				i++;
			} //while x1
		} // else	
	}//scanWide
	
	private void scanTall(ImageProcessor ip) {
		//scan along the leaf, one row at a time.
		//Will use some trig to calculate a line for scanning along leaf
		//x1,y1 and x2,y2 are start and end points of scan line

		double x1, x2, y1, y2;
			
		int i = 0; //0 will represent bottom of leaf
		
		int[] widthCenter = {0, -1};	 //see below.  to hold results of getWidth
		y1 = y2 = ROI_bottom;
		if (right) x1 = x2 = ROI_left; else x1 = x2 = ROI_right;
		while(y1 > ROI_top && i < arrayLength) {	
			//set x1, y1, and x2, y2, according to current y position
			y1--; //step up
			if (right) { //leaf leaning to the right
				x2 = ((ROI_bottom - y1) * Math.tan(A)) + ROI_left;
				if (x2 > ROI_right) { // outside of ROI!
					x2 = ROI_right;
					y2 = y1 + width/Math.tan(A);
				}
			}  else { //leaf leaning to the left
				x2 = ROI_right + ((ROI_bottom - y1) * Math.tan(A));
					//the tangent will be negative so we add here
				if (x2 < ROI_left) { //outside of ROI!
					x2 = ROI_left;
					y2 = y1 - width/Math.tan(A);
				}
			} // else			
			widthCenter = processScanLine(x1, y1, x2, y2, i, ip, widthCenter);
			i++;
		}//while y1
		while(y2 > ROI_top && i < arrayLength) {
			//x1,y1 are now at top corner
			//need to bring x2, y2 along to their top corner
			y2--; //step up
			if (right) {
				x1 = ROI_right - Math.abs(Math.tan(A))*(y2-ROI_top);
			} else {
				x1 = ROI_left + Math.abs(Math.tan(A))*(y2-ROI_top);
			} //else
			widthCenter = processScanLine(x1, y1, x2, y2, i, ip, widthCenter);
			i++;
		}//while y2
	}//scanTall
	
	private int[] processScanLine(double x1, double y1, double x2, double y2, int i, ImageProcessor ip, int[] widthCenter) {
		pixelRowLeaf row = new pixelRowLeaf((int)  Math.ceil(Math.min(height,width)/Math.sin(A)));
			//set length according to maximum possible length of scan line
			
		row.setBg(ip.getMinThreshold());
		row.setThreshold(ip.getMaxThreshold());
		row.setPixels(ip,x1,y1,x2,y2);
		double[] tmp = {x1,y1,x2,y2};
		double xC,yC; 		//leaf center at each scan line
		widthCenter = row.getWidth(widthCenter[1]);
		widths.setWidth(tmp,widthCenter[0],i);
		
		if (right) {
			xC = x1 + widthCenter[1] * Math.sin(A);
			yC = y1 + widthCenter[1]  * Math.cos(A);
		} else {
			xC = x1 - widthCenter[1] * Math.sin(A);
			yC = y1 - widthCenter[1]  * Math.cos(A);
		}
		
		leafCenter_x[i] = (float) xC;
		leafCenter_y[i] = (float) yC;
		
		if (verbose > 1) {
			IJ.log("i: " + IJ.d2s(i) + " " +
				"x1: " + IJ.d2s(x1) + " " +
				"y1: " + IJ.d2s(y1) + " " +
				"x2: " + IJ.d2s(x2) + " " +
				"y2: " + IJ.d2s(y2) + " " +
				"xC: " + IJ.d2s(xC) + " " +
				"yC: " + IJ.d2s(yC));
		}
		
		//if (verbose > 2) ip.drawPixel((int) xC, (int) yC); 
		
		if ((i % 5 == 0) & (verbose > 3)) {
			ip.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
		}
		
		return(widthCenter);
	} //processScanLine
			
	public void findPetiole(ImageProcessor ip) {
		/*find ends of the petiole
		uses width information from scanLeaf*/
		
		ip.setValue(0);
		widths.findTop();
		top = widths.getTop();
//		topLine = widths.getTopLine();
		//set topLine based on petiole center
		//this line will be drawn across the leaf so that the magicWand
		//will work.
		topLine = new double[] {
			leafCenter_x[top] - Math.abs(Math.sin(A) * widths.getWidth(top) / 1.4) , //x1
			leafCenter_y[top] - Math.cos(A) * widths.getWidth(top) / 1.4 , //y1
			leafCenter_x[top] + Math.abs(Math.sin(A) * widths.getWidth(top) / 1.4), //x2
			leafCenter_y[top] + Math.cos(A) * widths.getWidth(top) / 1.4};  //y2

		ip.drawLine((int) topLine[0],(int) topLine[1],(int) topLine[2],(int) topLine[3]);
		
		//set bottomLine
		widths.findBottom();
		bottom = widths.getBottom();
		bottomLine = new double[] {leafCenter_x[bottom] - Math.abs(Math.sin(A) * widths.getWidth(bottom) / 1.8), //x1
			leafCenter_y[bottom] - Math.cos(A) * widths.getWidth(bottom) / 1.8, //y1
			leafCenter_x[bottom] + Math.abs(Math.sin(A) * widths.getWidth(bottom) / 1.8), //x2
			leafCenter_y[bottom] + Math.cos(A) * widths.getWidth(bottom) / 1.8};  //y2
		

		ip.drawLine((int) bottomLine[0],(int) bottomLine[1],(int) bottomLine[2],(int) bottomLine[3]);
		if (verbose > 1) IJ.log("bottom: " + IJ.d2s(bottom,0));
		if (verbose > 1) IJ.log("bottom line " +  IJ.d2s(bottomLine[0]) +" "+
			IJ.d2s(bottomLine[1]) +" "+
			IJ.d2s(bottomLine[2]) +" "+
			IJ.d2s(bottomLine[3]));
		
		//trim to set petiole_xy
		petiole_x = new float[top-bottom+1];
		petiole_y = new float[top-bottom+1];

		System.arraycopy(leafCenter_x,bottom,petiole_x,0,top-bottom+1);
		System.arraycopy(leafCenter_y,bottom,petiole_y,0,top-bottom+1);

	}// findPetiole(ImageProcessor ip)
	
	@SuppressWarnings("unused")
	private void printArray(float[] testArray, String title) {
		IJ.log("----------------");
		IJ.log("array " + title);
		for(int i = 0; i < testArray.length; i++) {
			IJ.log(IJ.d2s(i) + " " + IJ.d2s(testArray[i]));
		}
	}
	
	public void addBladeToManager(ImagePlus imp, ImageProcessor ip, RoiManager rm,int leaf) {
		if (verbose > 0) IJ.log("starting find blade");
		//method to find the blade
		//one approach is to use the wand, using pixels just on the far
		//side of the petiole as a starting point.
		//if we drew the "top line" in as a white line first then that should work
		//also might want to do some filling of holes after blade is selected.
		//alternatively we could use the width arrays to create a selection area	
		//alternatively convert to binary and use outline tool.
			
		//will try the wand
		//create a wand
		Wand w = new Wand(ip);
		
		//draw top line in white
		ip.setColor(255);
		//ip.setLineWidth(2);
		ip.drawLine((int) topLine[0],(int) topLine[1],(int) topLine[2],(int) topLine[3]);
		
		//apply the wand
		w.autoOutline((int) leafCenter_x[Math.min(top+10,leafCenter_x.length-1)],
				(int) leafCenter_y[Math.min(top+10,leafCenter_y.length-1)],
				ip.getMinThreshold(),
				ip.getMaxThreshold()*.9,
				Wand.FOUR_CONNECTED);
		//if (w.npoints > 0){
			PolygonRoi bladeROI = new PolygonRoi(w.xpoints, w.ypoints, w.npoints, Roi.POLYGON);
			rm.add(imp, bladeROI, leaf);//need to change leaf number handling
			rm.select(leaf*2+1);
			rm.runCommand("Rename", String.valueOf(leaf*2 + 2) + ": Blade " + String.valueOf(leaf+1));
		//}
	}
		
	public void addPetioleToManager(ImagePlus imp, ImageProcessor ip, RoiManager rm,int leaf){
	//a new attempt at addToManager
	//goal is to have a linear ROI for the petiole defined and added to the manager
	//strategy is to use the information from width center to define the petiole and ROI

	int nPoints = 10;// number of points for ROI;
 	int[] roi_x = new int[nPoints];
	int[] roi_y = new int[nPoints];
	double step =  (double) (petiole_x.length-1)/(nPoints-1); //step size to get points
	for(int i = 0; i < nPoints; i++) {
		roi_x[i] = (int) Math.round(petiole_x[(int) Math.round(i*step)]);
		roi_y[i] = (int) Math.round(petiole_y[(int) Math.round(i*step)]);
	} 
	
	PolygonRoi petioleROI = new PolygonRoi(roi_x, roi_y, nPoints,Roi.POLYLINE);
//	PolygonRoi petioleROI = new PolygonRoi(petiole_x, petiole_y, petiole_x.length,Roi.POLYLINE);
	rm.add(imp, petioleROI, leaf);
	rm.select(leaf*2);
	rm.runCommand("Rename", String.valueOf(leaf*2 + 1) + ": Petiole " + String.valueOf(leaf+1));
}

/*private int[] addArray(int[] a, int[]b) {
	if (verbose > 2) IJ.log("start addArray");
	int[]c = new int[a.length];
	for(int i = 0; i < a.length;i++) {
		c[i] = a[i] + b[i];
	}
	if (verbose > 2) IJ.log("end addArray");
	return c;
}*/
	

		
}//class leaf
