
/**
* @author Guillaume Lobet | UniversitŽ de Li�ge
* @date:  2013-02-28 
**/



import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ContrastEnhancer;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.RGBStackSplitter;
import ij.process.AutoThresholder.Method;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;


public final class Util {

	/**
	 * Return the sum of an array
	 * @param vect
	 * @return
	 */
	public static float sum(Vector<Float> vect){
		float sum = 0;
		for(int i = 0; i < vect.size(); i ++) sum = sum + vect.get(i);
		return 	sum;
	}
	
	/**
	 * Return the sum of an array
	 * @param vect
	 * @return
	 */
	public static float sum(float[] vect){
		float sum = 0;
		for(int i = 0; i < vect.length; i ++) sum = sum + vect[i];
		return 	sum;
	}
	
	/**
	 * Return the mean values of an array
	 * @param vect
	 * @return
	 */
	public static float avg(Vector<Float> vect){
		return 	sum(vect)/vect.size();
	}
	
	/**
	 * Return the mean values of an array
	 * @param vect
	 * @return
	 */
	public static float avg(float[] vect){
		return 	sum(vect)/vect.length;
	}

	/**
	 * return the maximum of an array
	 * @param vect
	 * @return
	 */
	public static float max(Vector<Float> vect){
		float max = 0;
		for(int i = 0; i < vect.size(); i++) if(vect.get(i) > max) max = vect.get(i);
		return max;
	}
	
	/**
	 * return the minimum of an array
	 * @param vect
	 * @return
	 */
	public static float min(Vector<Float> vect){
		float min = 10e9f;
		for(int i = 0; i < vect.size(); i++) if(vect.get(i) < min) min = vect.get(i);
		return min;
	}
	
	
	/**
	 * return the standart deviation of an array
	 * @param vect
	 * @return
	 */
	public static float std(Vector<Float> vect){
		float sum = 0;
		float avg = avg(vect);
		for(int i = 0; i < vect.size(); i++) sum = sum + ((vect.get(i) - avg)*(vect.get(i) - avg));
		return (float) Math.sqrt(sum/vect.size());
	}
	
	/**
	 * return the standart deviation of an array
	 * @param vect
	 * @return
	 */
	public static float std(float[] vect){
		float sum = 0;
		float avg = avg(vect);
		for(int i = 0; i < vect.length; i++) sum = sum + ((vect[i] - avg)*(vect[i] - avg));
		return (float) Math.sqrt(sum/vect.length);
	}
	
	   
	/**
	 * Return the plant age based on the experiment starting date 
	 * and the current date (contained in the QR code)
	 * @param dateStart = starting date
	 * @param dateCurent = current date
	 * @return the date different, in days.
	 */
	public static long getDAS(String dateStart, String dateCurent){
		Date start, current;
		try{ 
			DateFormat formatter ; 
			formatter = new SimpleDateFormat("yyyy-MM-dd");
			start = (Date) formatter.parse(dateStart);
			current = (Date) formatter.parse(dateCurent);
		} 
		catch(Exception e){
			System.out.println("getPlantAge failed"+e );
			return 0;
		}
		
		Calendar calStart = Calendar.getInstance();
		Calendar calToday = Calendar.getInstance();
		calStart.setTime(start);
		calToday.setTime(current);	    
	    		
		return (calToday.getTimeInMillis() - calStart.getTimeInMillis()) / (1000 * 86400);
	} 
	
	/**
	 * Try to read the QR code
	 * The function try different image manipulation to make the QR code readable 
	 * 
	 */
	public static String getQR(ImagePlus im){
	   			
		ImagePlus im1;
		ImageProcessor ip1;
		
		// Create the different tools
		RGBStackSplitter splitter = new RGBStackSplitter(), splitter2 = new RGBStackSplitter(), splitter3 = new RGBStackSplitter();

		// Process the color channels
		splitter.split(im.getStack(), true);
		splitter2.split(im.getStack(), true);
		splitter3.split(im.getStack(), true);
		
		/*
		 * First try
		 */
		
		// Red
		im1 = new ImagePlus("", splitter.red.getProcessor(1));
		ip1 = im1.getProcessor();
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		ip1.threshold(50);
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		for(int i = 0 ; i < 5; i ++) ip1.dilate(); if(!readQR(ip1).equals("")) return readQR(ip1);
		im1.flush(); im1.close();
		
		// Green
		im1 = new ImagePlus("", splitter.green.getProcessor(1));
		ip1 = im1.getProcessor();
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		ip1.threshold(50);
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		for(int i = 0 ; i < 5; i ++) ip1.dilate(); if(!readQR(ip1).equals("")) return readQR(ip1);
		im1.flush(); im1.close();
		
		// Blue
		im1 = new ImagePlus("", splitter.blue.getProcessor(1));
		ip1 = im1.getProcessor();
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		ip1.threshold(50);
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		for(int i = 0 ; i < 5; i ++) ip1.dilate(); if(!readQR(ip1).equals("")) return readQR(ip1);
		im1.flush(); im1.close();

		
		/*
		 *  Second Try
		 */
		
		// Red
		im1 = new ImagePlus("", splitter2.red.getProcessor(1));
		ip1 = im1.getProcessor();
		ip1.smooth();
		ip1.setAutoThreshold(Method.Minimum, false);
		//im1.updateImage();
		//im1.show();
		if(!readQR(ip1).equals("")) return readQR(ip1);	
		for(int i = 0 ; i < 5; i ++) ip1.dilate(); if(!readQR(ip1).equals("")) return readQR(ip1);
		im1.flush(); im1.close();

		// Green
		im1 = new ImagePlus("", splitter2.green.getProcessor(1));
		ip1 = im1.getProcessor();
		ip1.smooth();
		ip1.setAutoThreshold(Method.Minimum, false);
		if(!readQR(ip1).equals("")) return readQR(ip1);	
		for(int i = 0 ; i < 5; i ++) ip1.dilate(); if(!readQR(ip1).equals("")) return readQR(ip1);
		im1.flush(); im1.close();

		// Blue
		im1 = new ImagePlus("", splitter2.blue.getProcessor(1));
		ip1 = im1.getProcessor();
		ip1.smooth();
		ip1.setAutoThreshold(Method.Minimum, false);
		if(!readQR(ip1).equals("")) return readQR(ip1);	
		for(int i = 0 ; i < 5; i ++) ip1.dilate(); if(!readQR(ip1).equals("")) return readQR(ip1);
		im1.flush(); im1.close();

				
		/*
		 * Third Try
		 */
		
		ImageCalculator ic = new ImageCalculator();	
		ContrastEnhancer ce = new ContrastEnhancer();
		ImagePlus imR, imB, imG;
		
		imR = new ImagePlus("red", splitter3.red.getProcessor(1));
		imB = new ImagePlus("blue", splitter3.blue.getProcessor(1));
		imG = new ImagePlus("green", splitter3.green.getProcessor(1));
		
		// Red + Blue
		im1 = ic.run("Add create", imR, imB);
		ip1 = im1.getProcessor();
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		ce.stretchHistogram(ip1, 10);
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		ip1.threshold(50);
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		for(int i = 0 ; i < 5; i ++) ip1.dilate(); if(!readQR(ip1).equals("")) return readQR(ip1);
		im1.flush(); im1.close();
		
		// Red + Green
		im1 = ic.run("Add create", imR, imG);
		ip1 = im1.getProcessor();
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		ce.stretchHistogram(ip1, 10);
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		ip1.threshold(50);
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		for(int i = 0 ; i < 5; i ++) ip1.dilate(); if(!readQR(ip1).equals("")) return readQR(ip1);
		im1.flush(); im1.close();
		
		// Green + Blue
		im1 = ic.run("Add create", imG, imB);
		ip1 = im1.getProcessor();
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		ce.stretchHistogram(ip1, 10);
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		ip1.threshold(50);
		if(!readQR(ip1).equals("")) return readQR(ip1); 
		for(int i = 0 ; i < 5; i ++) ip1.dilate(); if(!readQR(ip1).equals("")) return readQR(ip1);
		im1.flush(); im1.close();
		
			
		imR.flush(); imR.close();
		imB.flush(); imB.close();
		imG.flush(); imG.close();

		return "EXPxx_GENxx_TRxx_BOXxx";
	}	
	
	/**
	 * Read the QR code in the image and retrieve its information
	 * Based upon the ZXing open-source, multi-format 1D/2D barcode image processing library.
	 * http://code.google.com/p/zxing/
	 * 
	 * @author  Elliott D. Slaughter
	 *          Stanford University, USA
	 *          elliottslaughter@gmail.com
	 *          http://elliottslaughter.com/
	 * @version 1.0
	 * @since   July, 31, 2011         
	 * @param iInit
	 * @return the text contained in the QR code
	 */
	public static String readQR(ImageProcessor ip){
		Reader reader = new QRCodeReader();
		BufferedImage myimg = ip.getBufferedImage();
		LuminanceSource source = new BufferedImageLuminanceSource(myimg);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		try {
			Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
			hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
			Result result = reader.decode(bitmap, hints);
			return result.getText();
			// On errors just return the default result.
		} 
		catch (NotFoundException e) {return "";}
		catch (ChecksumException e) {return "";}
		catch (FormatException e) {return "";}		
	} 	
	
	/**
	 * Return the HSB stack of a color image
	 * @param imp the ImagePlus
	 */
    public static ImageStack getHSBStack(ImagePlus imp) {
        int w = imp.getWidth();
        int h = imp.getHeight();
        byte[] hue,s,b;
        ImageStack hsbStack = imp.getStack();
        hue = new byte[w*h];
        s = new byte[w*h];
        b = new byte[w*h];        
        ColorProcessor cp = (ColorProcessor) imp.getProcessor();
        cp.getHSB(hue,s,b);
        hsbStack.addSlice("hue",hue);
        hsbStack.addSlice("saturation",s);
        hsbStack.addSlice("brightness",b);
        
        ImagePlus im = new ImagePlus();
        im.setStack(hsbStack);
        im.show();
        
        return hsbStack;      
   }
    
    
    /**
     * Get the unique plant id in the database
     * @param exp
     * @param gen
     * @param tr
     * @param pot
     * @param pl
     * @return
     */
	public static String getPlantID(String exp, String gen, String tr, String pot, int pl){
		return getPlantID(Integer.valueOf(exp), Integer.valueOf(gen), Integer.valueOf(tr), Integer.valueOf(pot), pl);
	}
    
    /**
	 * Get the unique plant ID in the SQL database.
	 * Requires the database to be correctly filled. 
	 */
	public static String getPlantID(int exp, int gen, int tr, int pot, int pl){
		try{
			java.sql.Connection conn = initializeSQL();
			java.sql.Statement stmt=conn.createStatement();
			
			java.sql.ResultSet rs=stmt.executeQuery("SELECT plant_id FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTablePlant+
					" WHERE experiment_id = "+exp+" AND treatment_id ="+tr+" AND stock_id ="+gen+" AND pot ="+pot+" AND plant ="+pl);
			rs.first();
			return rs.getString(1);	 	  		 
		} 
		catch(Exception e){
			IJ.log("Util.getPlantID failed: "+e);
			return "0";
		}
	}    
	
	
	/**
	 * Get the starting date of the experiment in the SQL database.
	 * Requires the database to be correctly filled. 
	 */
	public static String getStartingDate(String experiment){
		try{
			java.sql.Connection conn = initializeSQL();
			java.sql.Statement stmt = conn.createStatement();
			java.sql.ResultSet rs=stmt.executeQuery("SELECT starting_date FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableExperiment+" WHERE experiment_id = "+experiment);
			rs.first();
			return rs.getString(1);	 	  		 
		} 
		catch(Exception e){
			IJ.log("Util.getStartingDate failed: "+e);
			return "0000-00-00";
		}
	}	

	/**
	 * Get the image folder of the experiment in the SQL database.
	 * Requires the database to be correctly filled. 
	 */
	public static String getImageFolder(String experiment){
		try{
			java.sql.Connection conn = initializeSQL();
			java.sql.Statement stmt = conn.createStatement();
			java.sql.ResultSet rs=stmt.executeQuery("SELECT folder_name FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableExperiment+" WHERE experiment_id = "+experiment);
			rs.first();
			return rs.getString(1);	 	  		 
		} 
		catch(Exception e){
			IJ.log("Util.getImageFolder failed: "+e);
			return "0000";
		}
	}	
	
	/**
	 * Get the genotype name of a given genotype in the SQL database.
	 * Requires the database to be correctly filled. 
	 */
	public static String getGenotypeName(String genotype){
		try{
			java.sql.Connection conn = initializeSQL();
			java.sql.Statement stmt = conn.createStatement();
			java.sql.ResultSet rs=stmt.executeQuery("SELECT genotype_name FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableStock+" WHERE genotype_id = "+genotype);
			rs.first();
			return rs.getString(1);	 	  		 
		} 
		catch(Exception e){
			IJ.log("Util.getGenotypeName failed: "+e);
			return "0000";
		}
	}	
	
	/**
	 * Get the treatment name of a given treatment in the SQL database.
	 * Requires the database to be correctly filled. 
	 */
	public static String getTreatmentName(String treatment){
		try{
			java.sql.Connection conn = initializeSQL();
			java.sql.Statement stmt = conn.createStatement();
			java.sql.ResultSet rs=stmt.executeQuery("SELECT treatment_name FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableTreatment+" WHERE treatment_id = "+treatment);
			rs.first();
			return rs.getString(1);	 	  		 
		} 
		catch(Exception e){
			IJ.log("Util.getTreatmentName failed: "+e);
			return "0000";
		}
	}		
	
	
	/**
	 * Retrieve the information contained between two given Strings
	 * @param name: the string to process
	 * @param begin: the openning string
	 * @param end: the closing string. Must be different from "."
	 * @return the String in between
	 */
	public static String processName(String name, String begin, String end){
				
		// Remove the extension
		int cut = name.indexOf(".");
		name = name.substring(0, cut);
				
		String subName, subSubName;	
		int l = name.length();
		
		cut = name.indexOf(begin);
		if(cut != -1){
			subName = name.substring(cut+begin.length(), l);
			cut = (subName.indexOf(end) > -1) ? subName.indexOf(end) : subName.length();
			subSubName = subName.substring(0, cut);
			return (subSubName.length() > 0) ? subSubName : "0000";
		}	
		
		return "0000";
	}	
	
	/**
	 * Retrieve the information contained between an openniong string and "_"
	 * @param name = the string to process
	 * @param code = openning string
	 * @return the information in between
	 */
	public static String processName(String name, String code){		
		return processName(name, code, "_");
	}
	
	
	/**
	 * Initialize the CSV connection
	 */
	public static PrintWriter initializeCSV(String folder){	
		
		// Create the connection
		PrintWriter pw = null;
		try{ pw = new PrintWriter(new FileWriter(folder)); }
		catch(IOException e){
			IJ.log("Could not save file "+folder);
			return null;
		}
		return pw;
	}
	
	
	/**
	 * Initialize the SQL connection
	 * @return
	 */
	public static java.sql.Connection initializeSQL(){
		try{
			Class.forName(SQLServer.sqlDriver).newInstance();
			return java.sql.DriverManager.getConnection(
					SQLServer.sqlConnection+"?user="+SQLServer.sqlUsername+"&password="+SQLServer.sqlPassword);
		}
		catch(Exception e){ 
			IJ.log("SQL connection could not be established. Please verify your SQL settings");
			return null;	
		}
	}
	
	
	/**
	 * Test the SQL connection
	 * @return
	 */
	public static boolean testSQLConnection(){
		String alert = "SQL connection could not be established. Please verify your SQL settings."+"\n";
		java.sql.Statement stmt;
		
		try{stmt = Util.initializeSQL().createStatement();}
		catch(Exception e){IJ.log(alert+e); return false; }
		
		try{stmt.executeQuery("SELECT * FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableExperiment);}
		catch(Exception e){IJ.log(alert+e); return false; }
		
		try{stmt.executeQuery("SELECT * FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableLeaf);}
		catch(Exception e){IJ.log(alert+e); return false; }
		
		try{stmt.executeQuery("SELECT * FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTablePlant);}
		catch(Exception e){IJ.log(alert+e); return false; }
		
		try{stmt.executeQuery("SELECT * FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableRosette);}
		catch(Exception e){IJ.log(alert+e); return false; }
		
		//try{stmt.executeQuery("SELECT * FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableSeed);}
		//catch(Exception e){IJ.log(alert+e); return false; }
		
		try{stmt.executeQuery("SELECT * FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableStock);}
		catch(Exception e){IJ.log(alert+e); return false; }
		
		try{stmt.executeQuery("SELECT * FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableTreatment);}
		catch(Exception e){IJ.log(alert+e); return false; }
		
		return true;

	}	
	
	/**
	 * Create the image folder structure
	 * The structure is:
	 * EXPERIENCE_NAME | GENOTYPE_NAME | TREATMENT_NAME | PLANT_ID
	 */
	public static File createFolderStructure(String analysis, String experiment, String stock, String treatment, String plantID){
		
		String folder = Util.getImageFolder(experiment);
		String genotypeName = Util.getGenotypeName(stock);
		String treatmentName = Util.getTreatmentName(treatment);
				
		File d0 = new File(folder);
		File d1 = new File(d0.getAbsolutePath()+"/"+genotypeName+"/");
		File d2 = new File(d1.getAbsolutePath()+"/"+treatmentName+"/");
		File d3 = new File(d2.getAbsolutePath()+"/"+plantID+"/");
		File d4 = new File(d3.getAbsolutePath()+"/"+analysis+"/");
		
		File dirOriginal = new File(d4.getAbsolutePath()+"/originals/");
		File dirMask = new File(d4.getAbsolutePath()+"/masks/");
		
		if(!d1.exists()) try{d1.mkdir();} catch(Exception e){IJ.log("Could not create folder "+d1);}
		if(!d2.exists()) try{d2.mkdir();} catch(Exception e){IJ.log("Could not create folder "+d2);}
		if(!d3.exists()) try{d3.mkdir();} catch(Exception e){IJ.log("Could not create folder "+d3);}
		if(!d4.exists()) try{d4.mkdir();} catch(Exception e){IJ.log("Could not create folder "+d4);}
		if(!dirOriginal.exists()) try{dirOriginal.mkdir();} catch(Exception e){IJ.log("Could not create folder "+dirOriginal);}
		if(!dirMask.exists()) try{dirMask.mkdir();} catch(Exception e){IJ.log("Could not create folder "+dirMask);}
		
		return d4;
	}
	
	/**
	 * Create the image folder structure
	 * The structure is:
	 * EXPERIENCE_NAME | GENOTYPE_NAME | TREATMENT_NAME | PLANT_ID
	 */
	public static File createFolderStructure(String folder, boolean global, boolean local, boolean coord, boolean param){
				
		File d0 = new File(folder);
		//File dirOriginal = new File(d0.getAbsolutePath()+"/raws/");
		//if(!dirOriginal.exists()) try{dirOriginal.mkdir();} catch(Exception e){IJ.log("Could not create folder "+dirOriginal);}
		
		if(global){
			File dirMask = new File(d0.getAbsolutePath()+"/global/");
			//File dirConvex = new File(d0.getAbsolutePath()+"/convexhulls/");
			//if(!dirConvex.exists()) try{dirConvex.mkdir();} catch(Exception e){IJ.log("Could not create folder "+dirConvex);}
			if(!dirMask.exists()) try{dirMask.mkdir();} catch(Exception e){IJ.log("Could not create folder "+dirMask);}
		}		
		if(local){
			File dirDiff = new File(d0.getAbsolutePath()+"/local/");
			if(!dirDiff.exists()) try{dirDiff.mkdir();} catch(Exception e){IJ.log("Could not create folder "+dirDiff);}
		}
		if(coord){File dirCoord = new File(d0.getAbsolutePath()+"/shape/");
		if(!dirCoord.exists()) try{dirCoord.mkdir();} catch(Exception e){IJ.log("Could not create folder "+dirCoord);}
		}
		if(param){File dirParam = new File(d0.getAbsolutePath()+"/param/");
		if(!dirParam.exists()) try{dirParam.mkdir();} catch(Exception e){IJ.log("Could not create folder "+dirParam);}
		}
		
		return d0;
	}		
	
	/**
	 * Create the image folder structure
	 * The structure is:
	 * EXPERIENCE_NAME | GENOTYPE_NAME | TREATMENT_NAME | PLANT_ID
	 */
	public static File createFolderStructure(String folder, String stock, String treatment){
				
		File d0 = new File(folder);
		File d1 = new File(d0.getAbsolutePath()+"/"+stock+"/");
		File d2 = new File(d1.getAbsolutePath()+"/"+treatment+"/");
		
		File dirOriginal = new File(d2.getAbsolutePath()+"/originals/");
		File dirMask = new File(d2.getAbsolutePath()+"/masks/");
		
		if(!d1.exists()) try{d1.mkdir();} catch(Exception e){IJ.log("Could not create folder "+d1);}
		if(!d2.exists()) try{d2.mkdir();} catch(Exception e){IJ.log("Could not create folder "+d2);}
		if(!dirOriginal.exists()) try{dirOriginal.mkdir();} catch(Exception e){IJ.log("Could not create folder "+dirOriginal);}
		if(!dirMask.exists()) try{dirMask.mkdir();} catch(Exception e){IJ.log("Could not create folder "+dirMask);}
		
		return d2;
	}	
	
	/**
	 * Get an array of String from a String with the form "1,2,3,4"
	 * @param s
	 * @return
	 */
	public static String[] getArrayFromString(String s, String sep, boolean last){
		ArrayList<String> al = new ArrayList<String>();
		
		int index = s.indexOf(sep);
		while(index > 0){
			al.add(s.substring(0, index));
			s = s.substring(index+1, s.length());
			index = s.indexOf(sep);
		} 
		if(last) al.add(s);
		
		String[] data = new String[al.size()];
		for(int i = 0 ; i < data.length; i++) data[i] = al.get(i);
		
		return data;
	}
	
    
    
}
