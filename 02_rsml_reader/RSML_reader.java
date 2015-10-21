
/**
 * @author Xavier Draye - Universit� catholique de Louvain
 * @author Guillaume Lobet - Universit� de Li�ge
 *   
 * Main class for the RSML improter
 */

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;

import ij.*;

public class RSML_reader {

	private static RSML_reader instance;
	public static String path, output, data;
	public static boolean printData = false;

	/**
	 * Constructor
	 */
	public RSML_reader() {
     
      // Retrieve all the rsml files
      File f = new File(path);
      System.out.println(f.getAbsolutePath());
      File[] rsml = f.listFiles(new FilenameFilter() {
    	  public boolean accept(File directory, String fileName) {
    		  return fileName.endsWith(".rsml");
    	  }
      });
      
      // Open the different RSML files, retriev their data and get their size.
      RootModel model;
      PrintWriter pw = null;
      
      if(printData){
    	  try{pw = new PrintWriter(new FileWriter(data));}
    	  catch(IOException e){System.out.println("Could not save file "+data);}
    	  pw.println("image,tot_root_length,width,depth,direction,n_primary,tot_prim_length,mean_prim_length,mean_prim_diameter,mean_lat_density,n_laterals," +
    		  " tot_lat_length,mean_lat_length,mean_lat_diameter,mean_lat_angle,tree_size,tree_magnitude,tree_altitude,tree_index");
      }
      int percent = 0;
      float progression = 0;
      for(int i = 0; i < rsml.length; i++){
    	  model = new RootModel(rsml[i].getAbsolutePath());
    	  if(model.getNRoot() > 0){
    		  // Save the image
    		  ImagePlus ip = new ImagePlus(rsml[i].getName(),model.createImage(false, 0, true, false, true));  
    		  
    		  progression = (i/rsml.length)*100;
    		  if(progression > percent){    			
    			  System.out.println(percent+" % of the rsml files converted. "+(rsml.length-i)+" files remaining.");
    			  percent = percent + 10;
    		  }
    		  
    		  // System.out.println(output+System.getProperty("file.separator")+rsml[i].getName()+".jpg");
    		  IJ.save(ip, output+System.getProperty("file.separator")+rsml[i].getName()+".jpg");	   
    		  
    		  // Save the data
    		  if(printData) model.sendImageData(pw, rsml[i].getName());
    	  } 	  	
      }
      if(printData) pw.flush();
	}

   /**
    * Get instance
    * @return
    */
   public static RSML_reader getInstance() {return instance; }

   
   /**
    * Main class
    * @param args
    */
   @SuppressWarnings("unused")
   public static void main(String args[]) {
	   if(args.length > 0){
		   path = args[0];
		   if(args.length > 1) output = args[1];
		   else output = path;
		   if(args.length > 2){
			   printData = true;
			   data = args[2];
		   }
		   RSML_reader ie = new RSML_reader();
	   }
	   else System.out.println("No path specified");
	   System.exit(0);
   }

}




