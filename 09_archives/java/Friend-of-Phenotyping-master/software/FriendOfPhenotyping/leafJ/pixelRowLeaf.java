package leafJ;
import ij.*;
import ij.process.*;
import java.util.Arrays;

public class pixelRowLeaf {
 	private double[] pixels;				//a row of pixels
 	private double bg = 255;				//bg value
	private int threshold;
	public int verbose = 0;
 	
 	public pixelRowLeaf (int width) {
 		pixels = new double[width];
 		}
	
	public void setPixels (ImageProcessor ip,int x,int y,int width) {
		//method to retrieve a row of data from the ip
		int[] tmp = new int[width];
		ip.getRow(x,y,tmp,width);
		for(int i = 0; i < width; i++){
			pixels[i] = tmp[i]; //getRow can't take a double[]
		}
		}
		
	public void setPixels (ImageProcessor ip, double x1, double y1, double x2, double y2) {
		//method to retrieve a non-horizontal line of data from the ip
		pixels = ip.getLine(x1,y1,x2,y2);
		if (verbose > 1) IJ.log(Arrays.toString(pixels));
		}
		
	public double[] getPixels() {
		return pixels;
		}
		
	public double getPixel(int i){
		return pixels[i];
		}
		
	public double getSum () { //sums the pixel values in the row
		double pixelSum = 0;
		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] >= bg) pixels[i] = 255;
			pixelSum +=  (255-pixels[i]);
		} //for
			return pixelSum;
		} //getSum method
		
	public void setThreshold (ImageProcessor ip){
		//needed for getWidth.  Not needed for the pixelsum method
		threshold = (int) Math.round(ip.getAutoThreshold()*1.25);
		}
		
	public void setThreshold (double maxThreshold) {
		//needed for getWidth.  Not needed for the pixelsum method
		threshold = (int) Math.round(maxThreshold);
	}
		
	public int getWidth () { 	//alternative to getSum
					//calculate width of hypocotyl
					//based on thresholds
		int start = 0;
		int end = 0;
		for (int i = 0; i < pixels.length; i++) {
			//should rewrite this with a clever hash or list
			if (start==0 && pixels[i] < threshold) start = i;
			if (pixels[i] < threshold) end = i;
		}
		return (end-start);
	}
		
	public int[] getWidth(int center) {	//modified to record and use center information
					//calculate width of petiole
					//based on thresholds
					//return center and start and end 
		int start;
		int end;
		int width = 0;
		if (getMin(pixels) > threshold) return(new int[] {0,-1}); //no object in scan line
		if (center < 0) {	//this means that we are looking at the bottom
					//and need to find the hypocotyl center
			//find center
			//assume that best position is widest position.
			if (verbose > 0) IJ.log("getWidth center < 0");
			start = 0;
			end = 0;
			boolean lookForStart = true;
			for (int i = 0; i < pixels.length; i++) {
				//should rewrite this with a clever hash or list
				if (lookForStart && pixels[i] < threshold) {
					start = i;
					lookForStart = false;
				}
				if (!lookForStart && (pixels[i] > threshold)) { //end of current blob
					end = i-1;
					if ((end-start) > width) { //widest object so far
						width = end - start;
						center = (int) start + width/2;
						start = 0;
						end = 0;
						lookForStart = true;
					} //if end - start
				} //if pixels	
				if (verbose > 1) IJ.log("intensity: " + IJ.d2s(pixels[i]) + " start: " + IJ.d2s(start) + " end: " + IJ.d2s(end) + " width: " + IJ.d2s(width) + " center: " + IJ.d2s(center) + " i: " + IJ.d2s(i));
			}// for
			if (verbose > 0) IJ.log("first row width: " + IJ.d2s(width) + " center: " + IJ.d2s(center));
		} // if center
		else { //can use previous center information
			//find  width of this row using previous center information
			//find center
			//return width and center
			if (verbose > 0) IJ.log("getWidth center > 0");
			
			//need to deal with the possibility that the center of the last row
			//is not actually on the petiole/leaf in this row.
			//in that case we should walk out in both directions until we find it.
			
			int originalCenter = center;
			int step = 1;
			
			//make sure we are somewhere on the leaf/petiole:
			while ((center > 0) && (center < pixels.length) && pixels[center] > threshold) {
				center = originalCenter + step;
				step = step*-1;
				if (step > 0) step = step +1;
				if (verbose > 2) IJ.log("center: " + IJ.d2s(center,0) + " step: " + IJ.d2s(step,0));
			}	
			
			start = center;
			end = center;
			if (verbose > 1) IJ.log("looking for start");
			while (start > 0 && start < pixels.length && pixels[start-1] < threshold) { 
				start--;  				// start is still on the object
									// work left until end is found
				if (verbose > 1) {
					width = end - start;			
					center = (int) start + width/2;
					IJ.log("intensity: " + IJ.d2s(pixels[start]) + " start: " + IJ.d2s(start) + " end: " + IJ.d2s(end) + " width: " + IJ.d2s(width) + " center: " + IJ.d2s(center));
				} // if verbose
			}//while start
			if (verbose > 1) IJ.log("looking for end");
			while (end+2 < pixels.length && pixels[end+1] < threshold) {
				end++;	//work right until end is no longer on object
				if (verbose > 1) {
					width = end - start;			
					center = (int) start + width/2;					
					IJ.log("intensity: " + IJ.d2s(pixels[end]) + " start: " + IJ.d2s(start) + " end: " + IJ.d2s(end) + " width: " + IJ.d2s(width) + " center: " + IJ.d2s(center));
				} // if verbose
			} //while 
			width = end - start;			
			center = (int) start + width/2;
			if (verbose > 0) IJ.log("width: " + IJ.d2s(width) + " center: " + IJ.d2s(center));
		}// else
		int[] tmpReturn= {width,center};
		return (tmpReturn);			
	} // getWidth(int center)
		
		
	public void setBg(){ //should only be called when setRow has already been issued
				//should only be called when row contains only bg
				//take minimum in row as bg
				//alternatively could try taking quantile approach
		bg = pixels[0];
		for(int i = 1; i < pixels.length;i++) {
			if (pixels[i] < bg) bg = pixels[i];
			}
		bg = Math.round(bg*.9);
		//IJ.write("BG " + IJ.d2s(bg));
		}
		
	public void setBg(double BG) {
		bg = BG;
		}
		
	public double getBg() {
		return bg;
		}
		
	public int getBG() {
		return (int) bg;
	}
	
	private double getMin(double[] a) {
		double min = a[0];
		for (int i = 1; i < a.length; i++) {
			if (a[i] < min)
				min = a[i];
		}
		return min;
	}

}//class