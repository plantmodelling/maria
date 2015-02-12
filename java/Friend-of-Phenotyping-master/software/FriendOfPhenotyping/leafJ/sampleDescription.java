package leafJ;
//Class of user-defined sample description for a set of leafs
//includes methods for reading and writing defaults as well as getting user input
//Julin Maloof
//Nov 3,2011

//April 6, 2011
//Working to make user customizable input


import ij.*;
import ij.gui.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import java.awt.Button;

//check for configuration file
//if no configuration file, present a generic table
//if there is a configuration file, load it.
//ask if these defaults should be modified
//show editable table to modify
//save table


public class sampleDescription {
	private GenericDialog gd = new GenericDialog("Sample Description");
	public Boolean saveRois;
	private OptionsTable ot;
	private ArrayList<String> fieldNames;
	private ArrayList<String> description;
	private String filename; //the filename of the image being analyzed
	//private String[] defaults = new String[fieldNames.length];
	//private String filepath = "leaf_measure_defaults.txt";
	//private File defaultFile = new File(filepath);


	public sampleDescription() {
//		try {
//			SwingUtilities.invokeAndWait(new Runnable() { 
//				public void run () {
//					//read defaults if they exist
					ot = new OptionsTable(null);
//				}
//			});
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public void setDescription(String filename) {
		this.filename = filename;
		getInput();
	}

	public void getInput() {
		//allow user to define some inputs
		ArrayList<String> fieldType = new ArrayList<String>();

		//set up and display dialog box
		gd.addMessage("Enter or select values for each field, or click button below to edit this table.");

		for (int i = 0; i < ot.getNumberOfFields();i++){
			if (ot.getFieldValues(i).length==0) {
				gd.addStringField(ot.getFieldName(i),"",20);
				fieldType.add("String");
			} else {
				gd.addChoice(ot.getFieldName(i),ot.getFieldValues(i), "");
				fieldType.add("Choice");
			}
		}

		//		gd.addCheckbox("Save Set, Dissected by, and Measured by, as defaults?",true);
		gd.addCheckbox("Save Rois to file?",true);
		gd.setCancelLabel("Edit these options");
		gd.setOKLabel("Continue");
		gd.showDialog();

		if(gd.wasCanceled()) {
			gd.dispose();
			ot.editTable();
			gd = new GenericDialog("Sample Description");
			getInput();
		} else {
			saveRois = gd.getNextBoolean();

			//populate ArrayList with contents of dialog box
			fieldNames = ot.getFieldNames();

			description = new ArrayList<String>(fieldNames.size());			

			for (int i = 0; i < fieldNames.size(); i++) {
				if (fieldType.get(i) == "String") {
					description.add(gd.getNextString());
				} else if (fieldType.get(i) == "Choice") {
					description.add(gd.getNextChoice());
				}
			}

			fieldNames.add(0, "file");
			description.add(0, filename);	


		}

	}

	public String getFieldNames() {
		//returns field names as a tab-delimited String
		String s = new String();
		for(String f : fieldNames) {
			s = s + f + "\t";
		}
		return s.trim();
	}

	public String getDescription() {
		//returns sample description as a tab-delimited String
		String s = new String();
		for(String d : description) {
			if (d.length()==0) d = ".";
			s = s + d + "\t";
		}
		return s.trim();
	}

	public String getTruncatedDescription() {
		//used for creating filename for writing ROIs
		String s = new String();
		for(String d : description) {
			if(d.length() > 0) {
				s = s + d.substring(0, Math.min(4, (d.length()-1))) + "_";
			}
		}
		return s.substring(0,(s.length()-2)); //trims final "_"
	}	

}
