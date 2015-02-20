/**
* @author Guillaume Lobet | UniversitŽ de Li�ge
* @date: 2013-01-30
* 
* Create the interface for the Friend of Phenotyping ImageJ plugin
* 
**/


import ij.IJ;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class MARIAInterface extends JFrame implements ItemListener, ActionListener, ChangeListener{
	
//------------------------------------	
// GENERAL
	static String chooseFolder = "[Please choose a folder]"; 
	static String chooseFile = "[Please choose a file]"; 
	JTabbedPane tp;
	Color dGreen = new Color(53, 146, 31);

//------------------------------------	
// ROOT PANEL
	JButton rootAnalysisButton, rootFolderButton, rootCSVButton; 
	JTextField rootImageFolder, rootCSVFile;
	JTextField rootScalePix, rootScaleCm, rootStartingDate, rootMinSize, nEFD, nCoord;
	JCheckBox rootExportCSV, blackRoots, rootGlobal, rootCoord, rootLocal, rootParameters, rootEFD, manualCorrection, rootDirectional;
	JComboBox rootNameJCB;
	RootAnalysis ran; 
	JSlider rootMinSizeSlider;	

	private static final long serialVersionUID = -6312379859247272449L;

	/**
	 * Constructor
	 */
	public MARIAInterface(){
		build();	
	}
	
	/**
	 * Build the interface
	 */
	private void build(){
		this.setTitle("Friend of Phenotyping"); 					
		this.setSize(800,680); 									
		this.setLocationRelativeTo(null); 						
		this.setResizable(true) ; 								
		this.getContentPane().add(getPanel());
		this.setVisible(true);
	}

	/**
	 * Create the panel
	 * @return
	 */
	private JPanel getPanel(){	

	      tp = new JTabbedPane();	      
	      Font font = new Font("Dialog", Font.PLAIN, 12);
	      tp.setFont(font);
	      tp.addTab("MARIA", getAboutTab());
	      tp.addTab("Root Analysis", getRootTab());
	      	      
	      // Final container
	      JPanel container = new JPanel(new BorderLayout()) ; 								
	      container.add(tp);

	      return container;
	}

	/**
	 * Create the "About" tab for the interface
	 * @return
	 */
	private JScrollPane getAboutTab(){


		ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/MARIA_logo.png")));
		JLabel logo = new JLabel("",icon, JLabel.CENTER);
		logo.setPreferredSize(new Dimension(300, 250));

		// About

		JTextPane aboutPane = new JTextPane();
		aboutPane.setEditable(false);

		aboutPane.setText(displayAboutText());
		SimpleAttributeSet bSet = new SimpleAttributeSet();
		StyleConstants.setAlignment(bSet, StyleConstants.ALIGN_CENTER);
		StyledDocument doc = aboutPane.getStyledDocument();
		doc.setParagraphAttributes(0, doc.getLength(), bSet, false);

		JScrollPane aboutView = new JScrollPane(aboutPane);
		aboutView.setBorder(BorderFactory.createLineBorder(Color.gray));

		JPanel aboutBox = new JPanel(new BorderLayout());
		aboutBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		aboutBox.add(aboutView, BorderLayout.NORTH);

		// Disclaimer

		JTextPane disclaimerPane = new JTextPane();
		disclaimerPane.setEditable(false);

		disclaimerPane.setText(displayDisclaimerText());
		SimpleAttributeSet bSet1 = new SimpleAttributeSet();
		StyleConstants.setAlignment(bSet1, StyleConstants.ALIGN_CENTER);
		StyledDocument doc1 = disclaimerPane.getStyledDocument();
		doc1.setParagraphAttributes(0, doc1.getLength(), bSet1, false);

		JScrollPane disclaimerView = new JScrollPane(disclaimerPane);
		disclaimerView.setBorder(BorderFactory.createLineBorder(Color.gray));

		JPanel disclaimerBox = new JPanel(new BorderLayout());
		disclaimerBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		disclaimerBox.add(disclaimerView, BorderLayout.NORTH);

		JPanel p1 = new JPanel(new BorderLayout());
		p1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		p1.add(logo, BorderLayout.NORTH);
		p1.add(aboutBox, BorderLayout.CENTER);
		p1.add(disclaimerBox, BorderLayout.SOUTH);

	      return new JScrollPane(p1);
	}

	

	/**
	 * root analysis panel
	 * @return
	 */
	private JScrollPane getRootTab(){
	      
		
	      rootMinSizeSlider = new JSlider(JSlider.HORIZONTAL, 0, 600, 50);
	      rootMinSizeSlider.addChangeListener(this);
	      rootMinSizeSlider.setMajorTickSpacing(100);
	      rootMinSizeSlider.setMinorTickSpacing(10);
	      rootMinSizeSlider.setPaintLabels(true);  
	      rootMinSizeSlider.setSnapToTicks(true);
	      rootMinSizeSlider.setPaintTicks(true);
	      
		
	      rootAnalysisButton = new JButton("Run analysis");
	      rootAnalysisButton.setActionCommand("RUN_ANALYSIS_root");
	      rootAnalysisButton.addActionListener(this);
	      rootAnalysisButton.setEnabled(true);
	      	      
	      rootFolderButton = new JButton("Choose folder");
	      rootFolderButton.setActionCommand("IMAGE_FOLDER_root");
	      rootFolderButton.addActionListener(this);
	      
//	      rootImageFolder = new JTextField("[Choose a folder]",25);
	      rootImageFolder = new JTextField("/Users/guillaumelobet/Desktop/FoP/pouches",30);
	     
	      //rootCSVFile = new JTextField("[Choose file]", 20);
	      //rootCSVFile = new JTextField("/Users/guillaumelobet/Dropbox/research/projects/research/rhizotron_analysis/scripts/Root-System-Analysis/r/data/automated_analysis_2.csv", 40);
	      rootCSVFile = new JTextField("/Users/guillaumelobet/Desktop/test.csv", 35);
	      rootCSVFile.setEnabled(true);
	      
	      rootExportCSV = new JCheckBox("Send to CSV file", true);
	      rootExportCSV.addItemListener(this);
	      
	      blackRoots = new JCheckBox("Black roots", true);
	      rootGlobal = new JCheckBox("Global Analysis", false);
	      manualCorrection = new JCheckBox("Manual correction", false);
	      rootGlobal.addItemListener(this);
	      rootCoord = new JCheckBox("Root Coordinates", false);
	      rootCoord.addItemListener(this);
	      rootLocal = new JCheckBox("Local Analysis", false);
	      rootParameters = new JCheckBox("Image Descriptors (MARIA)", true);
	      rootParameters.addItemListener(this);
	      rootDirectional = new JCheckBox("Directional Analysis", false);	     	      
	      rootEFD = new JCheckBox("EFD Analysis", false);
	      rootEFD.addItemListener(this);

	      rootCSVButton = new JButton("Choose folder");
	      rootCSVButton.setActionCommand("CSV_FOLDER_root");
	      rootCSVButton.addActionListener(this);
	      rootCSVButton.setEnabled(true);
	      
	      String[] nameType = {"Process QR", "Process image name", "Use image name"};
	      rootNameJCB = new JComboBox(nameType);
	      rootNameJCB.setSelectedIndex(2);
	      rootNameJCB.addItemListener(this);

	      nEFD = new JTextField("5", 4);
	      nCoord = new JTextField("10", 4);
	      //int nts = new File(rootImageFolder.getText()).listFiles().length;
	      rootScalePix = new JTextField("2020", 5);	      
	      rootScaleCm = new JTextField("23.5", 5);	
	      rootStartingDate = new JTextField("2014-01-01", 10);
	      //rootStartingDate.setEnabled(false);
	      
	      rootMinSize = new JTextField(""+rootMinSizeSlider.getValue(),3);
	      rootMinSize.setEditable(false);
	      
	      // csv subpanel
	      JPanel panel3 = new JPanel();
	      panel3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints gbc2 = new GridBagConstraints();
	      gbc2.anchor = GridBagConstraints.WEST;
	      GridBagLayout gbl3 = new GridBagLayout();
	      panel3.setLayout(gbl3);

	      gbc2.gridx = 0;
	      gbc2.gridy = 0;
	      panel3.add(new JLabel("Image identification:"), gbc2);
	      gbc2.gridx = 1;
	      panel3.add(rootNameJCB, gbc2);
	      
//	      gbc2.gridx = 0;
//	      gbc2.gridy = 1;
//	      panel3.add(rootExportSQL, gbc2);
//	      gbc2.gridx = 1;
//	      panel3.add(rootOverrideSQL, gbc2);
	      
	      gbc2.gridx = 0;
	      gbc2.gridy = 2;
	      panel3.add(rootExportCSV, gbc2);	      
	      gbc2.gridx = 1;
	      panel3.add(rootCSVFile, gbc2);
	      gbc2.gridx = 2;
	      panel3.add(rootCSVButton, gbc2);
	      
	      JPanel panel4 = new JPanel(new BorderLayout());
	      panel4.setBorder(BorderFactory.createLineBorder(Color.gray));
	      panel4.add(panel3, BorderLayout.WEST);
	      
	      JLabel exportTitle1 = new JLabel("Export Parameters");
	      Font f = exportTitle1.getFont();
	      exportTitle1.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
	      
	      JPanel exportPanelAll = new JPanel(new BorderLayout());
	      exportPanelAll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      exportPanelAll.add(exportTitle1, BorderLayout.NORTH);
	      exportPanelAll.add(panel4, BorderLayout.SOUTH);
	      
	      /*
	       * Parameters
	       */
	      GridBagLayout gbl2 = new GridBagLayout();

	      JPanel panel = new JPanel(new BorderLayout());
	      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      	      
	      JLabel chooseSaveLabel = new JLabel("Image folder:");
	      
	      JPanel paramPanel1 = new JPanel();
	      paramPanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints gbc3 = new GridBagConstraints();
	      gbc3.anchor = GridBagConstraints.WEST;
	      paramPanel1.setLayout(gbl2);
	      
	      
	      gbc3.gridy = 0;
	      gbc3.gridx = 0;
	      paramPanel1.add(chooseSaveLabel, gbc3);
	      gbc3.gridx = 1;
	      paramPanel1.add(rootImageFolder, gbc3);
	      gbc3.gridx = 2;
	      paramPanel1.add(rootFolderButton, gbc3);     
 
	      gbc3.gridy = 2;
	      gbc3.gridx = 0;
	      paramPanel1.add(rootGlobal, gbc3);
	      gbc3.gridx = 1;
	      paramPanel1.add(rootLocal, gbc3);	
	      
	      gbc3.gridy = 3;
	      gbc3.gridx = 0;
	      paramPanel1.add(rootParameters, gbc3);
	      
	      gbc3.gridy = 4;
	      gbc3.gridx = 0;
	      paramPanel1.add(rootCoord, gbc3);
	      gbc3.gridx = 1;
	      paramPanel1.add(nCoord, gbc3);
	      
	      gbc3.gridy = 5;
	      gbc3.gridx = 0;
	      paramPanel1.add(rootEFD, gbc3);
	      gbc3.gridx = 1;
	      paramPanel1.add(nEFD, gbc3);

	      gbc3.gridy = 6;
	      gbc3.gridx = 0;
	      paramPanel1.add(rootDirectional, gbc3);
	      
	      gbc3.gridy = 7;
	      gbc3.gridx = 0;
	      paramPanel1.add(manualCorrection, gbc3);
	      
	      // Parameters panel
	      JPanel paramPanel2 = new JPanel(new BorderLayout());
	      paramPanel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      paramPanel2.add(paramPanel1, BorderLayout.WEST);
	     
	      JLabel paramTitle1 = new JLabel("Analysis options");
	      Font f3 = paramTitle1.getFont();
	      paramTitle1.setFont(f3.deriveFont(f3.getStyle() ^ Font.BOLD));
	      JPanel paramTitlePanel = new JPanel(new BorderLayout());
	      paramTitlePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	      paramTitlePanel.add(paramTitle1, BorderLayout.WEST);
	      
	      JPanel paramPanelAll = new JPanel(new BorderLayout());
	      paramPanelAll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      paramPanelAll.add(paramTitlePanel, BorderLayout.NORTH);
	      paramPanelAll.add(paramPanel2, BorderLayout.CENTER);	        
      
	      
	      JPanel scalePanel1 = new JPanel();
	      scalePanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints gbc4 = new GridBagConstraints();
	      gbc4.anchor = GridBagConstraints.WEST;
	      scalePanel1.setLayout(gbl2);
	      
	      gbc4.gridy = 0;
	      gbc4.gridx = 0;
	      scalePanel1.add(new JLabel("Image scale:"), gbc4);
	      gbc4.gridx = 1;
	      scalePanel1.add(new JLabel("Pixels: "), gbc4);
	      gbc4.gridx = 2;
	      scalePanel1.add(rootScalePix, gbc4);	
	      gbc4.gridx = 4;
	      
	      gbc4.gridy = 1;
	      gbc4.gridx = 1;
	      scalePanel1.add(new JLabel("Cm: "), gbc4);
	      gbc4.gridx = 2;
	      scalePanel1.add(rootScaleCm, gbc4);
 
	      
//	      gbc4.gridy = 2;
//	      gbc4.gridx = 0;
//	      scalePanel1.add(new JLabel("Starting date: "), gbc4);
//	      gbc4.gridx = 2;
//	      scalePanel1.add(rootStartingDate, gbc4);	


	     
	      gbc4.gridy = 4;
	      gbc4.gridx = 0;
	      scalePanel1.add(new JLabel("Min root size: "), gbc4);
	      gbc4.gridx = 2;
	      scalePanel1.add(rootMinSizeSlider, gbc4);
	      gbc4.gridx = 3;
	      scalePanel1.add(rootMinSize, gbc4);

	      
	      gbc4.gridy = 5;
	      gbc4.gridx = 2;
	      scalePanel1.add(blackRoots, gbc4);	
	      
	      JPanel scalePanel2 = new JPanel(new BorderLayout());
	      scalePanel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      scalePanel2.add(scalePanel1, BorderLayout.WEST);
	     
	      JLabel scaleTitle1 = new JLabel("Analysis Parameters");
	      Font f4 = scaleTitle1.getFont();
	      scaleTitle1.setFont(f4.deriveFont(f4.getStyle() ^ Font.BOLD));
	      JPanel scaleTitlePanel = new JPanel(new BorderLayout());
	      scaleTitlePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	      scaleTitlePanel.add(scaleTitle1, BorderLayout.WEST);
	      
	      JPanel scalePanelAll = new JPanel(new BorderLayout());
	      scalePanelAll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      scalePanelAll.add(scaleTitlePanel, BorderLayout.NORTH);
	      scalePanelAll.add(scalePanel2, BorderLayout.CENTER);	   
	      
	      
	      // All parameters panel
	      
	      JPanel paramPanel3 = new JPanel(new BorderLayout());
	      paramPanel3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      paramPanel3.add(paramPanelAll, BorderLayout.NORTH);
	      paramPanel3.add(scalePanelAll, BorderLayout.CENTER);
	      paramPanel3.add(exportPanelAll, BorderLayout.SOUTH);
	     
	      JPanel buttonBox = new JPanel(new BorderLayout());
	      buttonBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.X_AXIS));
	      buttonBox.add(rootAnalysisButton); 
	      
		    
	      JPanel buttonPanel = new JPanel(new BorderLayout());
	      buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      buttonPanel.add(buttonBox, BorderLayout.EAST); 
	      
	      panel.add(paramPanel3, BorderLayout.NORTH);
	      panel.add(buttonPanel, BorderLayout.SOUTH);
	      
	      return new JScrollPane(panel);
	}


	

	/**
	 * 
	 */
	public void stateChanged(ChangeEvent e) {
		Object item = e.getSource();
				
		
	}
	
	
	/**
	 * 
	 * @param e
	 */
	public void itemStateChanged(ItemEvent e) {
		Object item = e.getItem();
		
	}
	
	
		
	/**
	 * Action definition
	 */
	public void actionPerformed(ActionEvent ae) {

		

//---------------------------------------------------------------------------------------------------		
// root ANALYSIS
		if (ae.getActionCommand() == "CSV_FOLDER_root") { 
	    	  
			JFileChooser fc = new JFileChooser();
			csvFilter csvf = new csvFilter ();
			fc.setFileFilter(csvf);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = fc.showDialog(this, "Save");
			   
			if (returnVal == JFileChooser.APPROVE_OPTION){ 
				String fName = fc.getSelectedFile().toString();
				if(!fName.endsWith(".csv")) fName = fName.concat(".csv");
				rootCSVFile.setText(fName);
			}
			else IJ.log("Open command cancelled.");     
		}	
		
		else if (ae.getActionCommand() == "IMAGE_FOLDER_root") {
			
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showDialog(this, "Choose folder");
			
			if (returnVal == JFileChooser.APPROVE_OPTION){ 
				String fName = fc.getSelectedFile().toString();
				if(!fName.endsWith("/")) fName = fName+"/";
				rootImageFolder.setText(fName);
				File f = new File(fName);
				File[] images = f.listFiles();
			}
			else IJ.log("Folder command cancelled.");     
		}		
		
		else if(ae.getActionCommand() == "RUN_ANALYSIS_root"){
			if (rootImageFolder.getText().equals("")){
				IJ.log("Please choose an image folder");
				return;
			}
			if(rootExportCSV.isSelected() && rootCSVFile.getText().equals("")){
				IJ.log("Please choose a csv folder");
				return;
			}
			Thread ra = new Thread(new Runnable() {
		 		public void run() {	
					new RootAnalysis(new File(rootImageFolder.getText()), 
							rootCSVFile.getText(),
							Float.valueOf(rootScalePix.getText()), 
							Float.valueOf(rootScaleCm.getText()), 
							rootNameJCB.getSelectedIndex(),
							blackRoots.isSelected(),
							Float.valueOf(rootMinSize.getText()),
							rootLocal.isSelected(),
							rootGlobal.isSelected(),
							rootCoord.isSelected(),
							rootEFD.isSelected(),
							rootDirectional.isSelected(),
							rootParameters.isSelected(),
							Integer.valueOf(nEFD.getText()),
							Integer.valueOf(nCoord.getText()),
							manualCorrection.isSelected()
							);	
				}
			});
			ra.start();	
		}		
	
	}


	/**
	 * Image Filter
	 * @author guillaumelobet
	 */
	public class ImageFilter extends javax.swing.filechooser.FileFilter{
		public boolean accept (File f) {
			if (f.isDirectory()) {
				return true;
			}

			String extension = getExtension(f);
			if (extension != null) {
				if (extension.equals("tif") || extension.equals("tiff") || extension.equals("jpg") ) return true;
				else return false;
			}
			return false;
		}
	     
		public String getDescription () {
			return "Image file (*.tif, *.jpg)";
		}
	      
		public String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');
			if (i > 0 &&  i < s.length() - 1) {
				ext = s.substring(i+1).toLowerCase();
			}
			return ext;
		}
	}
   
   /**
    * CSV filter	
    * @author guillaumelobet
    */
	public class csvFilter extends javax.swing.filechooser.FileFilter{
		public boolean accept (File f) {
			if (f.isDirectory()) {
				return true;
			}

			String extension = getExtension(f);
			if (extension != null) {
				if (extension.equals("csv")) return true;
				else return false;
			}
			return false;
		}
	     
		public String getDescription () {
			return "Comma-separated values file (*.csv)";
		}
	      
		public String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');
			
			if (i > 0 &&  i < s.length() - 1) {
				ext = s.substring(i+1).toLowerCase();
			}
			return ext;
		}
	}
	/**
	 * Display the about text
	 * @return
	 */
	public String displayDisclaimerText(){

		String text = "\nThis plugin is provided 'as is' and 'with all faults'. We makes no representations or warranties \n"
				+ " of any kind concerning the safety, suitability, lack of viruses, inaccuracies, typographical errors, or other\n"
				+ " harmful components of this plugin. There are inherent dangers in the use of any software, and you are solely\n"
				+ " responsible for determining whether this plugin is compatible with your equipment and other software installed\n"
				+ " on your equipment. You are also solely responsible for the protection of your equipment and backup of your data\n"
				+ " and we will not be liable for any damages you may suffer in connection with using, \n"
				+ "modifying, or distributing this plugin.\n";
		return text;
	}

	/**
	 * Display the about text
	 * @return
	 */
	public String displayAboutText(){

		String text = "\n MARIA is a plugin created and maintained by\n-\n"
				+"Guillaume Lobet - University of Liége\n"
				+ "guillaume.lobet@ulg.ac.be\n"
				+ "@guillaumelobet\n-\n"
				+"Iko Koevoets - University of Utrecht\n"
				+"Loic Pages - INRA Avignon\n";
		return text;
	}	
	
}
