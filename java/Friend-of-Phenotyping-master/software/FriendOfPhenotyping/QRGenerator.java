/**
* @author Guillaume Lobet | Université de Liège
* @date: 2013-02-15
* 
* Plugin to create QR codes in serial.
* 
**/

import ij.*;
import ij.plugin.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

public class QRGenerator{
	 
	int repetitions, nPlant, setup;
	String experiment;
	String[] genotypes, treatments, genotypeNames, treatmentNames;
	
	String folder;
	
	String sqlTable, sqlDatabase, sqlPassword, sqlUsername;	
	java.sql.Statement stmt;			
	
	ImagePlus im, montage;
	ImageStack ims;
	MontageMaker mm = new MontageMaker();
	CanvasResizer cr = new CanvasResizer();

	
/**
 * Constructor
 * 
 * @param exp: Experiment ID
 * @param gen: List of genotype IDs
 * @param treat: List of treatment IDs
 * @param set: Setup ID
 * @param rep: Number of repetitions
 * @param np: Number of plants / repetitions
 * @param si: size of the QR code, in px
 * @param col: number of columns on the final page
 * @param ro: number of rows on the final page
 * @param fold: folder where to save the QR codes
 * @param export: do you want to export to the database
 * @param er: Do you want to erase the previous SQL inputs
 */
	public QRGenerator(String exp, String[] gen, String[] genN, String[] treat, String[] treatN, int set, int rep, int np,
			String fold, boolean export) {
				  
		experiment = exp;
		repetitions = rep;
		genotypeNames = genN;
		treatmentNames = treatN;
		genotypes = gen;
		treatments = treat;
		folder = fold;
		ims = new ImageStack(250, 250);	
		setup = set;
		nPlant = np;
		
		if(export){
			sqlTable = SQLServer.sqlTablePlant;
			sqlDatabase = SQLServer.sqlDatabase;
			sqlPassword = SQLServer.sqlPassword;
			sqlUsername = SQLServer.sqlUsername;
		}
		
		File d1 = new File(folder+"indiv/");
		File d2 = new File(folder+"col/");
		try{d1.mkdir();} catch(Exception e){IJ.log("Could not create folder "+d1);}
		try{d2.mkdir();} catch(Exception e){IJ.log("Could not create folder "+d2);}
		
		generateQRCodes(export);
	}			
	
	/**
	 * Generate the QR codes
	 * @param export
	 */
	private void generateQRCodes(boolean export){


		// SQL connection
		
		if(export){
			try{stmt = Util.initializeSQL().createStatement();}
			catch(Exception e){IJ.log("SQL statement could not be created \n "+e);}
		}
						  
		// Create the codes
		float count = 0;
		for(int j = 0; j < genotypes.length; j++){
			for(int k = 0; k < treatments.length; k++){
				for(int m = 1; m <= repetitions; m++){
					String m1 = "";
					if(m < 10) m1 = "0";
					String QRName = genotypeNames[j]+"_"+treatmentNames[k].substring(0, 3)+"_"+m1+m;
					String QRCode = "EXP"+experiment+"_GEN"+genotypes[j]+"_TR"+treatments[k]+"_BOX"+m1+m;
					createQR(QRCode, QRName);
					if(export) for(int n = 1; n <= nPlant; n++) sqlExport(stmt, Integer.valueOf(experiment), Integer.valueOf(genotypes[j]), Integer.valueOf(genotypes[k]), m, n, setup);
					count++;
				}
			}
		} 
			
		File[] images; 
			
		// Create the stack
		// Make the columns	
		File dir = new File(folder+"indiv/");
		images = dir.listFiles();
		for(int i = 0; i< images.length; i++) if(images[i].isHidden()) images[i].delete();
		images = dir.listFiles();
		for(int i = 0; i < images.length; i++){
			//if(images[i].getName().startsWith("EXP")){
				ims.addSlice(IJ.openImage(images[i].getAbsolutePath()).getProcessor());															
				ims.setSliceLabel(images[i].getName(), i+1);
			//}
		}
		ims = cr.expandStack(ims, 254, 254, 2, 2);
		im = new ImagePlus("stack", ims);	

		double num = Math.ceil(count/11); 
		for(int i = 1; i <= num; i++){
			int first = 1 + ((i-1) * (11));
			int last = i * (11);
			if(last > count) last = (int) count;
			montage = mm.makeMontage2(im, 1, 11, 1, first, last, 1, 0, true);
			String pre = "";
			if(i < 10) pre = "0";
			IJ.save(montage, folder+"col/"+"QR_COL_EXP"+experiment+"_"+pre+i+".tif");
			montage.flush(); montage.close();		
		}
		double pages = num;
		
			
		// Make the final
		dir = new File(folder+"col/");
		images = dir.listFiles();
		for(int i = 0; i< images.length; i++) if(images[i].isHidden()) images[i].delete();
		images = dir.listFiles();
		ims = new ImageStack(254, 2794);
		for(int i = 0; i < images.length; i++){
			if(images[i].getName().startsWith("QR_COL")){
				ims.addSlice(IJ.openImage(images[i].getAbsolutePath()).getProcessor());															
				ims.setSliceLabel(images[i].getName(), i+1);
			}
		}
		ims = cr.expandStack(ims, 304, 2874, 25, 40);
		im = new ImagePlus("stack", ims);	

		pages =  Math.ceil(num/6);
		for(int i = 1; i <= pages; i++){
			int first = 1 + ((i-1) * (6));
			int last = i * (6);
			if(last > num) last = (int) num;
			montage = mm.makeMontage2(im, 6, 1, 1, first, last, 1, 0, false);
			IJ.save(montage, folder+"/"+"QR_EXP"+experiment+"_"+i+".tif");
			montage.flush(); montage.close();		
		}	
			
		// Delete the individual QR images

//		dir = new File(folder+"indiv/");
//		File[] files = dir.listFiles();
//		for(int i = 0; i < files.length; i++){
//			files[i].delete();
//		}
//		dir.delete();
//		dir = new File(folder+"col/");
//		files = dir.listFiles();
//		for(int i = 0; i < files.length; i++){
//			files[i].delete();
//		}
//		dir.delete();

		IJ.log((int)count+" QR codes generated on "+(int)pages+" pages.");
		IJ.log("---------------------");
	}
			
		
	/**
	 * Export the data to the SQL database
	 * @param stmt
	 * @param exp
	 * @param gen
	 * @param tr
	 * @param box
	 * @param plant
	 * @param setup
	 */
	private void sqlExport(java.sql.Statement stmt, int exp, int gen, int tr, int box, int plant, int setup){
		
		// If the data already exist in the database and the user want to override it
		try{
			stmt.execute("DELETE FROM "+sqlDatabase+"."+sqlTable+" "+
				"WHERE experiment_id="+exp+" AND stock_id="+gen+" AND setup_id="+setup+" AND treatment_id="+tr+" AND pot="+box+" AND plant="+plant);
		} 
		catch(Exception e){IJ.log("Overriding data failed: "+e+"\n"+"DELETE FROM mars."+sqlTable+" "+
				"WHERE experiment_id="+exp+" AND stock_id="+gen+" AND setup_id="+setup+" AND treatment_id="+tr+" AND pot="+box+" AND plant="+plant);
		}
		
		// Send the current data
		try{
			stmt.execute("INSERT INTO "+sqlDatabase+"."+sqlTable+" (experiment_id, stock_id, treatment_id, setup_id, pot, plant) "+
					"VALUES ("+exp+", "+gen+", "+tr+", "+setup+", "+box+", "+plant+")"); 	  		 
		} 
		catch(Exception e){
			IJ.log("sendDataToSQL failed: "+e+"\n"+"INSERT INTO mars."+sqlTable+" (experiment_id, stock_id, treatment_id, setup_id, pot, plant) "+
					"VALUES ("+exp+", "+gen+", "+tr+", "+setup+", "+box+", "+plant+")"); 	  		 
		}
		
	}
		
		
	  
    /**
     * Create the QR code based on a given string
     * @param name
     */
    private void createQR(String code, String name) {
    	ByteArrayOutputStream out = QRCode.from(code).to(ImageType.PNG).withSize(250, 250).stream();

    	try {
    		FileOutputStream fout = new FileOutputStream(new File(""+folder+"indiv/"+name+".png"));
            fout.write(out.toByteArray());
            fout.flush();
            fout.close();
        } 
    	catch (FileNotFoundException e) {} 
	    catch (IOException e) {}
    }
    
}	

