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

public class QRGenerator1{
	 
	String experiment;
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
 * @param fold: folder where to save the QR codes
 * @param export: do you want to export to the database
 */
	public QRGenerator1(String exp, String fold) {
				  
		experiment = exp;
		folder = fold;
		ims = new ImageStack(250, 250);	
		
		sqlTable = SQLServer.sqlTablePlant;
		sqlDatabase = SQLServer.sqlDatabase;
		sqlPassword = SQLServer.sqlPassword;
		sqlUsername = SQLServer.sqlUsername;

		
		File d1 = new File(folder+"indiv/");
		File d2 = new File(folder+"col/");
		try{d1.mkdir();} catch(Exception e){IJ.log("Could not create folder "+d1);}
		try{d2.mkdir();} catch(Exception e){IJ.log("Could not create folder "+d2);}
		
		generateQRCodes();
	}			
	
	/**
	 * Generate the QR codes
	 * @param export
	 */
	private void generateQRCodes(){
		
		float count = 0;
		String query = "SELECT genotype_id, genotype_name, treatment_id, treatment_name, pot FROM "+sqlDatabase+".plant_full WHERE experiment_id="+experiment+" GROUP BY genotype_id, treatment_id, pot";
		try{			
			stmt = Util.initializeSQL().createStatement();
			java.sql.ResultSet rs=stmt.executeQuery(query);
			while(rs.next()){
				String QRName = rs.getString(2)+"_"+rs.getString(4)+"_"+rs.getString(5);
				String QRCode = "EXP"+experiment+"_GEN"+rs.getString(1)+"_TR"+rs.getString(3)+"_BOX"+rs.getString(5);
				createQR(QRCode, QRName);
				count++;
			}
		} 
		catch(Exception e){IJ.log("Overriding data failed: "+e+"\n"+query);
		}
		
			
		File[] images; 
			
		// Create the stack
		// Make the columns	
		File dir = new File(folder+"indiv/");
		images = dir.listFiles();
		for(int i = 0; i< images.length; i++) if(images[i].isHidden()) images[i].delete();
		images = dir.listFiles();
		for(int i = 0; i < images.length; i++){
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

