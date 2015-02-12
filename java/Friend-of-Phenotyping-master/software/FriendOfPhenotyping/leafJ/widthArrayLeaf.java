package leafJ;
import java.lang.Math;
import java.util.Arrays;

import ij.*;


//remove IJ imports after debug

public class widthArrayLeaf {
	// class to hold an array of leaf widths
	//this is an alternative to sumArray, which holds pixel sums
	//the important thing about this class is not the array
	//but the associated methods.

	//current implementation has the diff[0] = to the bottom of the leaf.
	public int verbose = 0; //mostly not implemented yet
	private double[][] positions;		//array of x1, y1, x2, y2 positions
	//first dimension is scan #
	//second position is x1, y1, x2, y2
	private int[] widths; 			//will hold the pixel widths from each row of one image
	private float[] diff;			//difference in widths between two rows
	private int top = -1;			//top of petiole
	private int bottom = -1;		//bottom of petiole
	private boolean diffCalc = false;	//has the difference calculate been run yet?
	private int runningWindow = 6;	 	//default value for running window size
	private double windowThreshold;
	private double threshold;
	private double[] tmp;


	private double widthThreshold;		//width at petiole top should be less than this threshold

	private int window;			//larger window for looking for big changes

	public widthArrayLeaf(int length) { //will initiate the widthArrayLeaf
		//length should correspond to the length of the
		//image or ROI
		widths = new int[length];
		diff = new float[length];
		positions = new double[length][4];
	}

	public void setWidth(int width, int i) { //add a width to the array
		widths[i] = width;
	}

	public void setWidth(double[] xypos, int width, int i) { // add a width and position
		//position should be sent as x1, y1, x2,y2
		positions[i] = xypos;
		widths[i] = width;
	}

	public int[] getWidths() {
		return widths;
	}

	public double getWidth(int i) {
		return widths[i];
	}

	public void printDiff() {
		IJ.log("printDiff");
		for (int i = 0;i < diff.length; i++) IJ.log(IJ.d2s(i,0) + " " + IJ.d2s(diff[i]));
	}

	public boolean validTop(int i){
		//debugger method to find which rows are considered valid
		if (i + window > diff.length -1) return false;
		else {
			//IJ.log("i: " + IJ.d2s(i,0) + " window: " + IJ.d2s(window) + " diff.length: " + IJ.d2s(diff.length));
			if (diff[i] == 0 || !(diff[i+runningWindow] < threshold && getMax(diff,i,i+window) < windowThreshold)) return false;
			else return true;
		}//else
	}

	public void findBottom(){
		//the bottom is defined as the first scan row with a width > width threshold
		//old definition was first non-zero width scan.
		int i = 0;
		while(widths[i] <widthThreshold & i < widths.length) {
			i++;
		}
		bottom = i;
	}


	public void findTop(){
		//rewritten to start from bottom and work up

		//method to determine valid endpoints for the top of the petiole
		//scan up the array of widths until the difference exceeds a threshold


		if (!diffCalc) calcDifference();
		int i = runningWindow;
		if (verbose > 0) {
			IJ.log("findTop");
			IJ.log("Threshold: " + IJ.d2s(threshold) + " windowThreshold: "
			       + IJ.d2s(windowThreshold) + " widthThreshold: " + IJ.d2s(widthThreshold));
		} //verbose
		
		if (verbose >1) {
				IJ.log("First row i: " + IJ.d2s(i,0) + " diff[i]: " + IJ.d2s(diff[i]) + " diff[i+runningWindow]" +
				       IJ.d2s(diff[i+runningWindow]) + " getMax(diff,i,i-runningWindow): " + IJ.d2s(getMax(diff,i,i-runningWindow))
				       + " widths[i]: " + IJ.d2s(widths[i]));
				IJ.log(" width at: " + IJ.d2s(i + (int) Math.round(widths.length/5)) + " is " +
				       IJ.d2s(widths[i + (int) Math.round(widths.length/5)]));
		} //verbose

		//move up petiole while
		while(diff[i] == 0 //no difference in width
		        || widths[i] < widthThreshold 	//width is too small (less than threshold)
		        								//currently width must not be in the narrowest 5%
		        						   		// This prevents errant calls at the very base of the petiole.
		        
		        || diff[i] < threshold			//difference is less than diffThreshold.
		        								//this is the main method for detecting boundary
		        								//Currently the threshold is 90%.  ie the change in width must be
		        								//above the 90% largest difference in width.
		        				
		        || getMax(diff,i-runningWindow,i) > windowThreshold //& max differences is greater than window threshold
		        //the idea is to not let abrupt changes at the beginning mess things up.
		        //this ensures that the region below the current test area is relatively even.

		        || (
		        		(widths[Math.min(widths.length-1, i + (int) Math.round(widths.length/5))] < 1.5*widths[i] ) &&
		        		i + widths.length/5 <= widths.length //long petioles and leafs placed at an angle necessitate this; will fire if query point is near end of leaf
		        		)
		        		)
				// if we are at the junction then moving 20% up the leaf should be much wider than where we are.
		{
			if (verbose >1) {
				IJ.log("i: " + IJ.d2s(i,0) + " diff[i]: " + IJ.d2s(diff[i]) + " diff[i+runningWindow]" +
						IJ.d2s(diff[i+runningWindow]) + " getMax(diff,i,i-runningWindow): " + IJ.d2s(getMax(diff,i,i-runningWindow))
						+ " widths[i]: " + IJ.d2s(widths[i]));
				if ((i + (int) Math.round(widths.length/5)) < widths.length) { 
					IJ.log(" width at: " + IJ.d2s(i + (int) Math.round(widths.length/5)) + " is " +
							IJ.d2s(widths[i + (int) Math.round(widths.length/5)]));
				} else {
					IJ.log("out of bounds");
				}
				IJ.log("diff[i] == 0 " + (diff[i] == 0));
				IJ.log("widths[i] < widthThreshold " + (widths[i] < widthThreshold));
				IJ.log("getMax(diff,i-runningWindow,i) > windowThreshold " + (getMax(diff,i-runningWindow,i) > windowThreshold));
				IJ.log("(widths[Math.min(widths.length-1, i + (int) Math.round(widths.length/5))] < 1.5*widths[i] )" +
						((widths[Math.min(widths.length-1, i + (int) Math.round(widths.length/5))] < 1.5*widths[i] )));
				IJ.log("i + widths.length/5 <= widths.length" + (i + widths.length/5 <= widths.length));
				IJ.log("and of the last two: " + ((widths[Math.min(widths.length-1, i + (int) Math.round(widths.length/5))] < 1.5*widths[i] ) &&
		        		i + widths.length/5 <= widths.length ));
				
			} //verbose
			i++;
			if ((i+runningWindow) >= widths.length-1) break; //should be an exception here
		} //while
		top = i;
	} //findtop


	public void calcDifference() {//calculate difference in widths using a running window approach
		int stop = diff.length-1-runningWindow;
		if (verbose > 0) IJ.log("calcDifference");
		for (int i = runningWindow; i<=stop;i++){
			//changed to get rid of conversion to absolute number.
			//the leaf should be increasing in width at the petiole end.
			diff[i] = (mean(widths,i,i+runningWindow) - mean(widths,i-runningWindow,i));
			if (verbose > 1) IJ.log("i:" + IJ.d2s(i) + " width:" + IJ.d2s(widths[i]) + " diff:" + IJ.d2s(diff[i]));
		}
		setThresholds();
		diffCalc = true;
	}

	public void setRunningWindow(int window) {
		runningWindow = window;
	}

	public int getRunningWindow() {
		return runningWindow;
	}


	public int getTop(){
		return top;
	}

	public double[] getTopLine(){
		//returns x1, y1, x2, y2, which describes a line that intersects
		//the top of the petiole

		if (positions[top][0] > positions[top][2]) { //need to switch so that x1 is left of x2
			tmp = new double[4];
			tmp[0] = positions[top][2];
			tmp[1] = positions[top][3];
			tmp[2] = positions[top][0];
			tmp[3] = positions[top][1];
			return(tmp);
		} else {
			return positions[top];
		}
	}

	public double[] getBottomLine(){
		//returns x1, y1, x2, y1, which describes a line that intersects
		//the bottom of the petiole

		if (positions[bottom][0] > positions[bottom][2]) { //need to switch so that x1 is left of x2
			tmp = new double[4];
			tmp[0] = positions[bottom][2];
			tmp[1] = positions[bottom][3];
			tmp[2] = positions[bottom][0];
			tmp[3] = positions[bottom][1];
			return(tmp);
		} else {
			return positions[bottom];
		}
	}


	public int getBottom() {
		return bottom;
	}

	private float getMax(float[] array,int start,int end) { //return maximum value in a float[]
		float max = array[start];
		for (int i = start; i < end; i++) {
			if (array[i] > max)
				max = array[i];
		}
		return max;
	}

	private float mean(int[] array,int start,int end) { //calculate mean of an integer array
		float result = 0;
		for(int i=start;i<=end;i++) {
			result += array[i];
		}
		return result/runningWindow;
	}

	private void setThresholds() {
		float[] tmpDiff = new float[diff.length];
		System.arraycopy(diff,0,tmpDiff,0,diff.length);
		Arrays.sort(tmpDiff); //a sorted array of the differences in width
		int i = 0;
		while (tmpDiff[i] == 0) i++;		//find where non-zero data starts
		int nonZeroLength = tmpDiff.length-i;
		threshold = tmpDiff[(int) Math.round(.90*nonZeroLength) + i];	//threshold is 90%
		windowThreshold=threshold;
		window = nonZeroLength/7;
		int[] tmpWidths = new int[widths.length];
		System.arraycopy(widths,0,tmpWidths,0,widths.length);
		Arrays.sort(tmpWidths);
		while (tmpWidths[i] == 0) i++;		//find where non-zero data starts
		nonZeroLength = tmpWidths.length-i;
		widthThreshold = tmpWidths[(int) Math.round(.05*nonZeroLength) + i]  ; //5th percentile
		//perhaps still need to deal with runningWindow i.e. adjust based on length.

	}

	public void printThreshold(){
		IJ.log("threshold: " + IJ.d2s(threshold) + " windowThreshold: " + IJ.d2s(windowThreshold) +
		       " window: " + IJ.d2s(window));
	}
}
