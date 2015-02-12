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
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

public class FoPInterface extends JFrame implements ItemListener, ActionListener, ChangeListener{
	
//------------------------------------	
// GENERAL
	static String chooseFolder = "[Please choose a folder]"; 
	static String chooseFile = "[Please choose a file]"; 
	JTabbedPane tp;
	Color dGreen = new Color(53, 146, 31);
	
//------------------------------------	
// SEED PANEL
	JButton seedAnalysisButton, seedFolderButton, seedCSVButton;
	JTextField seedImageFolder, seedCSVFile; 
	JTextField seedMinSize, seedMaxSize, seedMinCirc, seedMaxCirc, seedScalePix, seedScaleCm;
	JCheckBox seedExportCSV, seedExportSQL, seedOverrideSQL;
	JComboBox seedNameJCB;
	JSlider seedMinSizeSlider, seedMaxSizeSlider, seedMinCircSlider, seedMaxCircSlider;
//------------------------------------	
// LEAF PANEL
	JButton leafAnalysisButton, leafFolderButton, leafCSVButton, leafROIButton;
	JTextField leafImageFolder, leafCSVFile; 
	JTextField leafScalePix, leafScaleCm;
	JCheckBox leafExportCSV, leafExportSQL, leafOverrideSQL;
	JComboBox leafNameJCB;
	LeafAnalysis la;
		
//------------------------------------	
// SQL PANEL
	JTextField sqlDatabase, sqlHost, sqlConnection, sqlDriver, sqlUsername, sqlPassword;
	JTextField sqlTableRosette, sqlTableLeaf, sqlTablePlant, sqlTableSeed, sqlTableStock, 
		sqlTableExperience, sqlTableTreatment;	
	JButton sqlUpdateButton, sqlResetButton, sqlTestConnectionButton;

//------------------------------------		
// QR PANEL	
	ArrayList<String> qrTreatmentID = new ArrayList<String>();
	ArrayList<String> qrTreatmentName = new ArrayList<String>();
	ArrayList<String> qrStockID = new ArrayList<String>();
	ArrayList<String> qrStockName = new ArrayList<String>();
	ArrayList<String> qrExperimentID = new ArrayList<String>();
	ArrayList<String> qrExperimentName = new ArrayList<String>();
	ArrayList<Integer> qrSetupID = new ArrayList<Integer>();
	ArrayList<String> qrSetupName = new ArrayList<String>();
	JList qrExpList, qrStockList, qrTreatmentList, qrSetupList;
	JScrollPane qrTreatmentScroll, qrExperimentScroll, qrSetupScroll, qrStockScroll;
	JPanel qrPanelManual, qrPanelDB;
	JButton qrRunButton, qrFolderButton;
	JTextField qrExperiment, qrGenotypes, qrTreatments, 
		qrRepetitions,qrRepetitionsMan, qrPath, 
		qrSetup, qrPlant;
	JCheckBox qrExportToSQL, qrManual;

//------------------------------------	
// ROOT PANEL
	JButton rootAnalysisButton, rootFolderButton, rootCSVButton; 
	JTextField rootImageFolder, rootCSVFile;
	JTextField rootScalePix, rootScaleCm, rootStartingDate, rootMinSize, nEFD, nCoord;
	JCheckBox rootExportCSV, blackRoots, rootGlobal, rootCoord, rootLocal, rootParameters, rootEFD, manualCorrection, rootDirectional;
	JComboBox rootNameJCB;
	RootAnalysis ran; 
	JSlider rootMinSizeSlider;	

//------------------------------------	
// ROSETTE PANEL
	JButton rosetteAnalysisButton, rosetteFolderButton, rosetteCSVButton, rosetteROIButton; 
	JTextField rosetteImageFolder, rosetteCSVFile;
	JTextField rosetteScalePix, rosetteScaleCm, rosetteConservationFactor;
	JCheckBox rosetteExportCSV, rosetteExportSQL, rosetteOverrideSQL;
	JComboBox rosetteNameJCB;
	JSlider rosetteConservationSlider;
	RosetteAnalysis ra; 

//------------------------------------	
// PETRI PANEL
	JButton petriROIButton, petriRunButton, petriFolderButton;
	JTextField petriImageFolder, petriStartingDate, petriScalePix, petriScaleCm;
	JCheckBox petriRotate, petriRemoveBackground;
	PetriProcessor pp;
	
//------------------------------------	
// SCREENING PANEL
	JComboBox screeningMeasureJCB;
	JButton screeningRunButton, screeningFileButton, screeningProcessingButton;
	JTextField screeningNPlants, screeningMinSize, screeningIterations, screeningPlants;
	JLabel screeningSTDev;
	JTextArea screeningImages;
	JCheckBox screeningMask, screeningProcessing;
	JSlider screeningMinSizeSlider, screeningIterationsSlider, screeningPlantSlider, screeningSTDevSlider;
	JPanel screeningDynamicBox;
	int[][] screeningResults;
	ScreeningAnalysis sa;
	ChartPanel screeningChartPanel1, screeningChartPanel2;
	XYSeriesCollection screeningLineSerie1, screeningLineSerie2;
	
	
	private static final long serialVersionUID = -6312379859247272449L;

	/**
	 * Constructor
	 */
	public FoPInterface(){
		build();	
	}
	
	/**
	 * Build the interface
	 */
	private void build(){
		this.setTitle("Friend of Phenotyping"); 					
		this.setSize(800,650); 									
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
	      tp.addTab("QR Generator", getQRPanel());
	      tp.addTab("Seedling Screening", getScreeningPanel());
	      tp.addTab("Rosette Analysis", getRosettePanel());
	      tp.addTab("Root System Analysis", getRootPanel());
	      tp.addTab("Leaf Analysis", getLeafPanel());
	      tp.addTab("Seed Analysis", getSeedPanel());
	      tp.addTab("Petri Processor", getPetriPanel());
	      tp.addTab("SQL settings", getSQLPanel());
	      
	      tp.setSelectedIndex(3);
	      
	      // Final container
	      JPanel container = new JPanel(new BorderLayout()) ; 								
	      container.add(tp);

	      return container;
	}
		
	/**
	 * QR code generator panel
	 * @return
	 */
	private JScrollPane getQRPanel(){

		// Retrieve the informations from the database 
		if(Util.testSQLConnection()) setQRLists();
		else{
			qrExperimentScroll = new JScrollPane();
//			qrSetupScroll = new JScrollPane();
//			qrTreatmentScroll = new JScrollPane();
//			qrStockScroll = new JScrollPane();
		}
		
		JLabel expLabel = new JLabel("Experience:"),   
				folderLabel = new JLabel("Where to save the codes:");
//				genLabel = new JLabel("Genotypes:"),
//				trLabel = new JLabel("Treatments:"),
//				seLabel = new JLabel("Setup:"),
//				repLabel = new JLabel("Number of repetitions:"),
//				plantLabel = new JLabel("Number of plants/repetitions:");

		JLabel expLabelMan = new JLabel("Experience:"),   
				genLabelMan = new JLabel("Genotypes:"),
				trLabelMan = new JLabel("Treatments:"),
				repLabelMan = new JLabel("Number of repetitions:");
		

	    qrExportToSQL = new JCheckBox("Send to SQL database", false);	      
	    qrManual = new JCheckBox("Enter informations manually", false);	      
	    qrManual.addItemListener(this);
	    
		qrPlant = new JTextField("6", 3);
		qrRepetitions = new JTextField("3", 3);
		qrPath = new JTextField(chooseFolder, 25);
		
		qrRepetitionsMan = new JTextField("3", 3);
		qrExperiment = new JTextField("3", 5);
		qrGenotypes = new JTextField("1,2,3", 10);
		qrTreatments = new JTextField("1,2,3", 10);

		qrFolderButton = new JButton("Choose folder");
		qrFolderButton.setActionCommand("QR_FOLDER");
		qrFolderButton.addActionListener(this);
		
		qrRunButton = new JButton("Create QR codes");
		qrRunButton.setActionCommand("CREATE_QR");
		qrRunButton.addActionListener(this);
		
	    GridBagLayout gbl = new GridBagLayout();

	    // Global parameters
	    
	    JPanel panelGlobal = new JPanel();
	    panelGlobal.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.anchor = GridBagConstraints.WEST;
	    panelGlobal.setLayout(gbl);
	    
	    gbc.gridy = 1;
	    gbc.gridx = 0;
	    panelGlobal.add(folderLabel, gbc);
	    gbc.gridx = 1;
	    panelGlobal.add(qrPath, gbc);
	    gbc.gridx = 2;
	    panelGlobal.add(qrFolderButton, gbc);
	  
//	    gbc.gridx = 2;
//	    panel1.add(exportToSQL, gbc);
	    
	    JLabel text1 = new JLabel("Global parameters");
	    Font f5 = text1.getFont();
	    text1.setFont(f5.deriveFont(f5.getStyle() ^ Font.BOLD));
	    JPanel titlePanelGlobal = new JPanel(new BorderLayout());
	    titlePanelGlobal.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	    titlePanelGlobal.add(text1, BorderLayout.WEST);

	    JPanel panelGlobalWrapper = new JPanel(new BorderLayout());
	    panelGlobalWrapper.setBorder(BorderFactory.createLineBorder(Color.gray));
	    panelGlobalWrapper.add(panelGlobal, BorderLayout.WEST);
	    
	    JPanel panelGlobalAll = new JPanel(new BorderLayout());
	    panelGlobalAll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    panelGlobalAll.add(titlePanelGlobal, BorderLayout.NORTH);
	    panelGlobalAll.add(panelGlobalWrapper, BorderLayout.CENTER);
	    
	    //--------------------------------
	    // Experiment info 1
	    
	    JPanel panelInfo1 = new JPanel();
	    panelInfo1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    GridBagConstraints gbc1 = new GridBagConstraints();
	    gbc1.anchor = GridBagConstraints.WEST;
	    panelInfo1.setLayout(gbl);
	    	    
	    int i = 0;
	    gbc1.gridy = 0;
//	    gbc1.gridx = i++;
	    panelInfo1.add(expLabel, gbc1); 
//	    gbc1.gridx = i++;
//	    panelInfo1.add(new JLabel("  "), gbc1); 
//	    gbc1.gridx = i++;
//	    panelInfo1.add(genLabel, gbc1);    
//	    gbc1.gridx = i++;
//	    panelInfo1.add(new JLabel("  "), gbc1); 
//	    gbc1.gridx = i++;
//	    panelInfo1.add(trLabel, gbc1);  
//	    gbc1.gridx = i++;
//	    panelInfo1.add(new JLabel("  "), gbc1); 
//	    gbc1.gridx = i++;
//	    panelInfo1.add(seLabel, gbc1);
//	    gbc1.gridx = i++;
//	    panelInfo1.add(new JLabel("  "), gbc1); 
//	    gbc1.gridx = i++;
//	    panelInfo1.add(repLabel, gbc1);
//	    gbc1.gridx = i++;
//	    panelInfo1.add(new JLabel("  "), gbc1); 
//	    gbc1.gridx = i++;
//	    panelInfo1.add(plantLabel, gbc1);
	    
	    i = 0;
	    gbc1.gridy = 1;
//	    gbc1.gridx = i++; i++; 
	    panelInfo1.add(qrExperimentScroll, gbc1);
//	    gbc1.gridx = i++; i++;
//	    panelInfo1.add(qrStockScroll, gbc1);
//	    gbc1.gridx = i++; i++;
//	    panelInfo1.add(qrTreatmentScroll, gbc1);
//	    gbc1.gridx = i++; i++;
//	    panelInfo1.add(qrSetupScroll, gbc1);
//	    gbc1.gridx = i++; i++;
//	    panelInfo1.add(qrRepetitions, gbc1);
//	    gbc1.gridx = i++; i++;
//	    panelInfo1.add(qrPlant, gbc1);

	    // Experiment info 2
	    
	    JPanel panelInfo2 = new JPanel();
	    panelInfo2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    GridBagConstraints gbc2 = new GridBagConstraints();
	    gbc2.anchor = GridBagConstraints.WEST;
	    panelInfo2.setLayout(gbl);
	    
//	    i = 0;
//	    gbc2.gridy = i++;
//	    gbc2.gridx = 0; 
//	    panelInfo2.add(repLabel, gbc2);
//	    gbc2.gridy = i++;
//	    panelInfo2.add(qrRepetitions, gbc2);
//	    gbc2.gridy = i++;
//	    panelInfo2.add(plantLabel, gbc2);
//	    gbc2.gridy = i++;
//	    panelInfo2.add(qrPlant, gbc2);
	    
	    
	    JLabel text2 = new JLabel("Experiment informations");
	    Font f2 = text2.getFont();
	    text2.setFont(f2.deriveFont(f2.getStyle() ^ Font.BOLD));
	    JPanel titlePanelInfo = new JPanel(new BorderLayout());
	    titlePanelInfo.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	    titlePanelInfo.add(text2, BorderLayout.WEST);
	    titlePanelInfo.add(qrExportToSQL, BorderLayout.EAST);

	    qrPanelDB = new JPanel(new BorderLayout());
	    qrPanelDB.setBorder(BorderFactory.createLineBorder(Color.gray));    
	    qrPanelDB.setLayout(new BoxLayout(qrPanelDB, BoxLayout.X_AXIS));
	    qrPanelDB.add(Box.createHorizontalGlue());
	    qrPanelDB.add(panelInfo1);
	    qrPanelDB.add(panelInfo2);
	    
	    JPanel panelInfoAll = new JPanel(new BorderLayout());
	    panelInfoAll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    panelInfoAll.add(titlePanelInfo, BorderLayout.NORTH);
	    panelInfoAll.add(qrPanelDB, BorderLayout.CENTER);
	    
	    // -------------------------------
	    // Manual entry of the parameters
	    
	    qrPanelManual = new JPanel();
	    qrPanelManual.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    GridBagConstraints gbc3 = new GridBagConstraints();
	    gbc3.anchor = GridBagConstraints.WEST;
	    qrPanelManual.setLayout(gbl);
	    
	    i = 0;
	    gbc3.gridy = i++;
	    gbc3.gridx = 0; 
	    qrPanelManual.add(expLabelMan, gbc3);
	    gbc3.gridy = i++;
	    qrPanelManual.add(qrExperiment, gbc3);
	    gbc3.gridy = i++;
	    qrPanelManual.add(repLabelMan, gbc3);
	    gbc3.gridy = i++;
	    qrPanelManual.add(qrRepetitionsMan, gbc3);
	    gbc3.gridx = 1; 
	    gbc3.gridy = 0;
	    qrPanelManual.add(new JLabel("    "), gbc3);	    
	    
	    i = 0;
	    gbc3.gridx = 2; 
	    gbc3.gridy = i++;
	    qrPanelManual.add(trLabelMan, gbc3);
	    gbc3.gridy = i++;
	    qrPanelManual.add(qrTreatments, gbc3);	    
	    gbc3.gridy = i++;
	    qrPanelManual.add(genLabelMan, gbc3);
	    gbc3.gridy = i++;
	    qrPanelManual.add(qrGenotypes, gbc3);

	    JPanel panelInfoManualWrapper = new JPanel(new BorderLayout());
	    panelInfoManualWrapper.setBorder(BorderFactory.createLineBorder(Color.gray));    
	    panelInfoManualWrapper.add(qrPanelManual, BorderLayout.WEST);
	    
	    JPanel titlePanelManualInfo = new JPanel(new BorderLayout());
	    titlePanelManualInfo.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	    titlePanelManualInfo.add(qrManual, BorderLayout.EAST);
	    
	    JPanel panelInfoManualAll = new JPanel(new BorderLayout());
	    panelInfoManualAll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    panelInfoManualAll.add(titlePanelManualInfo, BorderLayout.NORTH);
	    panelInfoManualAll.add(panelInfoManualWrapper, BorderLayout.CENTER);
	    
	    Component[] cp = qrPanelManual.getComponents();
	    for(int k = 0; k < cp.length; k++) cp[k].setEnabled(false);
	    
	    
	    //--------------------------------
	    
	    
	    // Button
	    
	    JPanel buttonPanel = new JPanel(new BorderLayout());
	    buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	    buttonPanel.add(qrRunButton, BorderLayout.EAST);
	      
		JPanel panel = new JPanel(new BorderLayout());
	    panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	    panel.add(panelGlobalAll, BorderLayout.NORTH);
	    panel.add(panelInfoAll, BorderLayout.CENTER);
	    panel.add(panelInfoManualAll, BorderLayout.SOUTH);
	    
	    
		JPanel panelQR = new JPanel(new BorderLayout());
	    panelQR.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	    panelQR.add(panel, BorderLayout.NORTH);
	    panelQR.add(buttonPanel, BorderLayout.SOUTH);
	    
		return new JScrollPane(panelQR);
	}
	
	
	/**
	 * root analysis panel
	 * @return
	 */
	private JScrollPane getRootPanel(){
	      
		
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
	 * Seedlind screening panel
	 * @return
	 */
	private JScrollPane getScreeningPanel(){
	      // Seedling screening
	     
	      screeningImages = new JTextArea(chooseFolder, 5, 35);
	      JScrollPane screeningImagesScroll = new JScrollPane(screeningImages);
	      //screeningImagesScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      screeningImages.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      
	      screeningMinSizeSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 500);
	      screeningMinSizeSlider.addChangeListener(this);
	      screeningMinSizeSlider.setMajorTickSpacing(200);
	      screeningMinSizeSlider.setMinorTickSpacing(50);
	      screeningMinSizeSlider.setPaintLabels(true);  
	      screeningMinSizeSlider.setSnapToTicks(true);
	      screeningMinSizeSlider.setPaintTicks(true);
	      
	      screeningIterationsSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 2);
	      screeningIterationsSlider.addChangeListener(this);
	      screeningIterationsSlider.setMajorTickSpacing(1);
	      screeningIterationsSlider.setPaintLabels(true);  
	      screeningIterationsSlider.setSnapToTicks(true);
	      screeningIterationsSlider.setPaintTicks(true);
	      
	      String[] screeningTypes = {"Surface", "Diameter"};
	      screeningMeasureJCB = new JComboBox(screeningTypes);
	      screeningMeasureJCB.setSelectedIndex(0);
	      
	      screeningRunButton = new JButton("Run screening");
	      screeningRunButton.setActionCommand("RUN_SCREENING");
	      screeningRunButton.addActionListener(this);
	      
	      screeningProcessingButton = new JButton("Run pre-processing");
	      screeningProcessingButton.setActionCommand("RUN_SCREENING_PROCESSING");
	      screeningProcessingButton.addActionListener(this);
	      screeningProcessingButton.setEnabled(false);

	      screeningFileButton = new JButton("Choose image(s)");
	      screeningFileButton.setActionCommand("SCREENING_IMAGE");
	      screeningFileButton.addActionListener(this);
	      	      
	      screeningNPlants = new JTextField("20", 5);
	      screeningMinSize = new JTextField(""+screeningMinSizeSlider.getValue(), 3);
	      screeningMinSize.setEditable(false);
	      screeningIterations = new JTextField(""+screeningIterationsSlider.getValue(), 3);
	      screeningIterations.setEditable(false);
	      
	      screeningMask = new JCheckBox("Display Mask", false);	    
	      
	      screeningProcessing = new JCheckBox("Pre-process the images", false);	      
	      screeningProcessing.addItemListener(this);
	      
	      JPanel subpanel1 = new JPanel();
	      subpanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints gbc1 = new GridBagConstraints();
	      gbc1.anchor = GridBagConstraints.WEST;
	      GridBagLayout gbl1 = new GridBagLayout();
	      subpanel1.setLayout(gbl1);		

	      JPanel panel1 = new JPanel(new BorderLayout());
	      panel1.setBorder(BorderFactory.createLineBorder(Color.gray));
	      panel1.add(subpanel1, BorderLayout.WEST);
		      
	      JLabel title1 = new JLabel("Images parameters");
	      Font f1 = title1.getFont();
	      title1.setFont(f1.deriveFont(f1.getStyle() ^ Font.BOLD)); 
			
	      JPanel box1 = new JPanel(new BorderLayout());
	      box1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      box1.add(title1, BorderLayout.NORTH);
	      box1.add(panel1, BorderLayout.SOUTH);
			
	      gbc1.gridx = 0;
	      gbc1.gridy = 0;
	      subpanel1.add(new JLabel("Images to analyse:"), gbc1);
	      gbc1.gridx = 1;
	      subpanel1.add(screeningImagesScroll, gbc1);	
	      gbc1.gridx = 2;
	      subpanel1.add(screeningFileButton, gbc1);	

	 
	      
	      JPanel subpanel2 = new JPanel();
	      subpanel2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints gbc2 = new GridBagConstraints();
	      gbc2.anchor = GridBagConstraints.WEST;
	      GridBagLayout gbl2 = new GridBagLayout();
	      subpanel2.setLayout(gbl2);
	      
	      JPanel panel2 = new JPanel(new BorderLayout());
	      panel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      panel2.add(subpanel2, BorderLayout.WEST);
		      
	      JLabel title2 = new JLabel("Analysis parameters");
	      Font f2 = title2.getFont();
	      title2.setFont(f2.deriveFont(f2.getStyle() ^ Font.BOLD)); 
			
	      JPanel box2 = new JPanel(new BorderLayout());
	      box2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      box2.add(title2, BorderLayout.NORTH);
	      box2.add(panel2, BorderLayout.SOUTH);
			
	      gbc2.gridx = 0;
	      gbc2.gridy = 1;
	      subpanel2.add(new JLabel("Number of plants to keep:"), gbc2);
	      gbc2.gridx = 1;
	      subpanel2.add(screeningNPlants, gbc2);
	      gbc2.gridx = 3;
	      subpanel2.add(screeningMask, gbc2);
	      
	      gbc2.gridx = 0;
	      gbc2.gridy = 2;
	      subpanel2.add(new JLabel("Screening method:"), gbc2);
	      gbc2.gridx = 1;
	      subpanel2.add(screeningMeasureJCB, gbc2);
	      
	      gbc2.gridx = 0;
	      gbc2.gridy = 3;
	      subpanel2.add(new JLabel("Minimum size (px):"), gbc2);
	      gbc2.gridx = 1;
	      subpanel2.add(screeningMinSize, gbc2);
	      gbc2.gridx = 2;
	      subpanel2.add(screeningMinSizeSlider, gbc2);
	      
	      gbc2.gridx = 0;
	      gbc2.gridy = 4;
	      subpanel2.add(new JLabel("Thining iterations:"), gbc2);
	      gbc2.gridx = 1;
	      subpanel2.add(screeningIterations, gbc2);
	      gbc2.gridx = 2;
	      subpanel2.add(screeningIterationsSlider, gbc2);
	      
	      
	      screeningDynamicBox = new JPanel(new BorderLayout());
//	      screeningDynamicBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//	      GridBagConstraints gbc3 = new GridBagConstraints();
//	      gbc3.anchor = GridBagConstraints.WEST;
//	      GridBagLayout gbl3 = new GridBagLayout();
//	      screeningDynamicBox.setLayout(gbl3);
	      
	      JPanel panel3 = new JPanel(new BorderLayout());
	      panel3.setBorder(BorderFactory.createLineBorder(Color.gray));
	      panel3.add(screeningDynamicBox, BorderLayout.WEST);
		      
	      JLabel subtitle3 = new JLabel("Output parameters");
	      Font f3 = subtitle3.getFont();
	      subtitle3.setFont(f3.deriveFont(f3.getStyle() ^ Font.BOLD)); 
			
	      JPanel title3 = new JPanel(new BorderLayout());
	      title3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      title3.add(subtitle3, BorderLayout.WEST);
	      title3.add(screeningProcessing, BorderLayout.EAST);
	      
	      JPanel box3 = new JPanel(new BorderLayout());
	      box3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      box3.add(title3, BorderLayout.NORTH);
	      box3.add(panel3, BorderLayout.SOUTH);      
	      
	      JPanel allBoxes = new JPanel(new BorderLayout());
	      allBoxes.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      allBoxes.setLayout(new BoxLayout(allBoxes, BoxLayout.Y_AXIS));
	      allBoxes.add(box1);
	      allBoxes.add(box2);
	      allBoxes.add(box3);
	      
	      
	      JPanel buttons = new JPanel(new BorderLayout());
	      buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	      buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
	      buttons.add(screeningProcessingButton);
	      buttons.add(screeningRunButton);

	      JPanel buttonPanel = new JPanel(new BorderLayout());
	      buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      buttonPanel.add(buttons, BorderLayout.EAST);
	      
	      JPanel panel = new JPanel(new BorderLayout());
	      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      panel.add(allBoxes, BorderLayout.NORTH);
	      panel.add(buttonPanel, BorderLayout.SOUTH);
	      
	      return new JScrollPane(panel);
	}
	
	/**
	 * Rosette analysis panel
	 * @return
	 */
	private JScrollPane getRosettePanel(){
	      
	      rosetteConservationSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 9);
	      rosetteConservationSlider.addChangeListener(this);
	      rosetteConservationSlider.setMajorTickSpacing(10);
	      rosetteConservationSlider.setPaintLabels(true);  
	      rosetteConservationSlider.setMinorTickSpacing(1);
	      rosetteConservationSlider.setSnapToTicks(true);
	      rosetteConservationSlider.setPaintTicks(true);
	      
	      Enumeration<Integer> e1 = rosetteConservationSlider.getLabelTable().keys();
	      while (e1.hasMoreElements()) {
	    	  Integer i = e1.nextElement();
	          JLabel label = (JLabel) rosetteConservationSlider.getLabelTable().get(i);
	          label.setText(String.valueOf(i / 10));          
	      }
		
		
	      rosetteAnalysisButton = new JButton("Run analysis");
	      rosetteAnalysisButton.setActionCommand("RUN_ANALYSIS_ROSETTE");
	      rosetteAnalysisButton.addActionListener(this);
	      rosetteAnalysisButton.setEnabled(false);
	      
	      rosetteROIButton = new JButton("Set ROI's");
	      rosetteROIButton.setActionCommand("SET_ROI");
	      rosetteROIButton.addActionListener(this);
	      	      
	      rosetteFolderButton = new JButton("Choose folder");
	      rosetteFolderButton.setActionCommand("IMAGE_FOLDER_ROSETTE");
	      rosetteFolderButton.addActionListener(this);
	      
	      rosetteImageFolder = new JTextField("[Choose a folder]",25);
	      
	      rosetteCSVFile = new JTextField("[Choose file]", 20);
	      rosetteCSVFile.setEnabled(false);
	      
	      rosetteExportCSV = new JCheckBox("Send to CSV file", false);
	      rosetteExportCSV.addItemListener(this);

	      rosetteOverrideSQL = new JCheckBox("Overide previous measurments", false);
	      
	      rosetteExportSQL = new JCheckBox("Send to SQL database", true);
	      rosetteExportSQL.addItemListener(this);
	      
	      rosetteCSVButton = new JButton("Choose folder");
	      rosetteCSVButton.setActionCommand("CSV_FOLDER_ROSETTE");
	      rosetteCSVButton.addActionListener(this);
	      rosetteCSVButton.setEnabled(false);
	      
	      String[] nameType = {"Process QR", "Process image name", "Use image name"};
	      rosetteNameJCB = new JComboBox(nameType);
	      rosetteNameJCB.setSelectedIndex(0);
	      rosetteNameJCB.addItemListener(this);

	      rosetteScalePix = new JTextField("2020", 3);	      
	      rosetteScaleCm = new JTextField("23.5", 3);	
	      rosetteConservationFactor = new JTextField(""+((float)rosetteConservationSlider.getValue())/10, 3);	
	      rosetteConservationFactor.setEditable(false);
	      
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
	      panel3.add(rosetteNameJCB, gbc2);
	      
	      gbc2.gridx = 0;
	      gbc2.gridy = 1;
	      panel3.add(rosetteExportSQL, gbc2);
	      gbc2.gridx = 1;
	      panel3.add(rosetteOverrideSQL, gbc2);
	      
	      gbc2.gridx = 0;
	      gbc2.gridy = 2;
	      panel3.add(rosetteExportCSV, gbc2);	      
	      gbc2.gridx = 1;
	      panel3.add(rosetteCSVFile, gbc2);
	      gbc2.gridx = 2;
	      panel3.add(rosetteCSVButton, gbc2);
	      
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
	      paramPanel1.add(rosetteImageFolder, gbc3);
	      gbc3.gridx = 2;
	      paramPanel1.add(rosetteFolderButton, gbc3);     

	      // Parameters panel
	      JPanel paramPanel2 = new JPanel(new BorderLayout());
	      paramPanel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      paramPanel2.add(paramPanel1, BorderLayout.WEST);
	     
	      JLabel paramTitle1 = new JLabel("Image Parameters");
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
	      scalePanel1.add(rosetteScalePix, gbc4);	
	      
	      gbc4.gridy = 1;
	      gbc4.gridx = 1;
	      scalePanel1.add(new JLabel("Cm: "), gbc4);
	      gbc4.gridx = 2;
	      scalePanel1.add(rosetteScaleCm, gbc4);

	      
	      gbc4.gridy = 2;
	      gbc4.gridx = 0;
	      scalePanel1.add(new JLabel("Conservation factor:  "), gbc4);
	      gbc4.gridx = 1;
	      scalePanel1.add(rosetteConservationFactor, gbc4);
	      gbc4.gridx = 2;
	      scalePanel1.add(rosetteConservationSlider, gbc4);

	      
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
	      buttonBox.add(rosetteAnalysisButton); 
	      buttonBox.add(rosetteROIButton);
	      
		    
	      JPanel buttonPanel = new JPanel(new BorderLayout());
	      buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      buttonPanel.add(buttonBox, BorderLayout.EAST); 
	      
	      panel.add(paramPanel3, BorderLayout.NORTH);
	      panel.add(buttonPanel, BorderLayout.SOUTH);
	      
	      return new JScrollPane(panel);
	}

	/**
	 * Leaf analysis panel
	 * @return
	 */
	private JScrollPane getLeafPanel(){
	      

		leafExportCSV = new JCheckBox("Send to CSV file", false);
		leafExportCSV.addItemListener(this);
		leafCSVFile = new JTextField(chooseFile, 20);
		leafCSVFile.setEnabled(false);
	      
		leafCSVButton = new JButton("Choose folder");
		leafCSVButton.setActionCommand("CSV_FOLDER_LEAF");
		leafCSVButton.addActionListener(this);
		leafCSVButton.setEnabled(false);
		
		leafROIButton = new JButton("Set ROI's");
	    leafROIButton.setActionCommand("SET_ROI_LEAF");
	    leafROIButton.addActionListener(this);
	      
		leafExportSQL = new JCheckBox("Send to SQL database", true);
		leafExportSQL.addItemListener(this);
		leafOverrideSQL = new JCheckBox("Overide previous measurments", false);
	      
		leafImageFolder = new JTextField(chooseFolder,25);
				
		String[] nameType = {"Process QR", "Process image name", "Use image name"};
		leafNameJCB = new JComboBox(nameType);
		leafNameJCB.setSelectedIndex(2);
		leafNameJCB.addItemListener(this);
    
		leafScalePix = new JTextField("2745", 3);	      
		leafScaleCm = new JTextField("12.7",3);	    
		
		leafAnalysisButton = new JButton("Run analysis");
		leafAnalysisButton.setActionCommand("RUN_ANALYSIS_LEAF");
		leafAnalysisButton.addActionListener(this);
		leafAnalysisButton.setEnabled(false);
	      	      
		leafFolderButton = new JButton("Choose folder");
		leafFolderButton.setActionCommand("IMAGE_FOLDER_LEAF");
		leafFolderButton.addActionListener(this);
		
		JPanel subpanel1 = new JPanel();
		subpanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.anchor = GridBagConstraints.WEST;
		GridBagLayout gbl1 = new GridBagLayout();
		subpanel1.setLayout(gbl1);
		
		JPanel panel1 = new JPanel(new BorderLayout());
		panel1.setBorder(BorderFactory.createLineBorder(Color.gray));
		panel1.add(subpanel1, BorderLayout.WEST);
	      
		JLabel title1 = new JLabel("Image parameters");
		Font f = title1.getFont();
		title1.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD)); 
		
		JPanel box1 = new JPanel(new BorderLayout());
		box1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		box1.add(title1, BorderLayout.NORTH);
		box1.add(panel1, BorderLayout.SOUTH);
		
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		subpanel1.add(new JLabel("Image folder:"), gbc1);
		gbc1.gridx = 1;
		subpanel1.add(leafImageFolder, gbc1);
		gbc1.gridx = 2;
		subpanel1.add(leafFolderButton, gbc1);
		

		
		JPanel subpanel2 = new JPanel();
		subpanel2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.anchor = GridBagConstraints.WEST;
		GridBagLayout gbl2 = new GridBagLayout();
		subpanel2.setLayout(gbl2);		

		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.setBorder(BorderFactory.createLineBorder(Color.gray));
		panel2.add(subpanel2, BorderLayout.WEST);
	      
		JLabel title2 = new JLabel("Analysis parameters");
		Font f2 = title2.getFont();
		title2.setFont(f2.deriveFont(f2.getStyle() ^ Font.BOLD)); 
		
		JPanel box2 = new JPanel(new BorderLayout());
		box2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		box2.add(title2, BorderLayout.NORTH);
		box2.add(panel2, BorderLayout.SOUTH);
		
		
		gbc2.gridx = 0;
		gbc2.gridy = 0;
		subpanel2.add(new JLabel("Image scale:   "), gbc2);
		gbc2.gridx = 1;
		subpanel2.add(new JLabel("Pixels: "), gbc2);	
		gbc2.gridx = 2;
		subpanel2.add(leafScalePix, gbc2);

		gbc2.gridx = 1;
		gbc2.gridy = 1;
		subpanel2.add(new JLabel("Cm: "), gbc2);		
		gbc2.gridx = 2;
		subpanel2.add(leafScaleCm, gbc2);		
		
		JPanel subpanel3 = new JPanel();
		subpanel3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		GridBagConstraints gbc3 = new GridBagConstraints();
		gbc3.anchor = GridBagConstraints.WEST;
		GridBagLayout gbl3 = new GridBagLayout();
		subpanel3.setLayout(gbl3);
		
		JPanel panel3 = new JPanel(new BorderLayout());
		panel3.setBorder(BorderFactory.createLineBorder(Color.gray));
		panel3.add(subpanel3, BorderLayout.WEST);
	      
		JLabel title3 = new JLabel("Export parameters");
		Font f3 = title3.getFont();
		title3.setFont(f3.deriveFont(f3.getStyle() ^ Font.BOLD)); 
		
		JPanel box3 = new JPanel(new BorderLayout());
		box3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		box3.add(title3, BorderLayout.NORTH);
		box3.add(panel3, BorderLayout.SOUTH);
		
		gbc3.gridx = 0;
		gbc3.gridy = 0;
		subpanel3.add(new JLabel("Image identification:"), gbc3);
		gbc3.gridx = 1;
		subpanel3.add(leafNameJCB, gbc3);

		gbc3.gridx = 0;
		gbc3.gridy = 1;
		subpanel3.add(leafExportSQL, gbc3);
		gbc3.gridx = 1;
		subpanel3.add(leafOverrideSQL, gbc3);
		
		gbc3.gridx = 0;
		gbc3.gridy = 2;
		subpanel3.add(leafExportCSV, gbc3);
		gbc3.gridx = 1;
		subpanel3.add(leafCSVFile, gbc3);
		gbc3.gridx = 2;
		subpanel3.add(leafCSVButton, gbc3);
			
		
		JPanel boxes = new JPanel(new BorderLayout());
		boxes.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		boxes.add(box1, BorderLayout.NORTH);		
		boxes.add(box2, BorderLayout.CENTER);		
		boxes.add(box3, BorderLayout.SOUTH);		
		
		JPanel button1 = new JPanel(new BorderLayout());
		button1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		button1.add(leafAnalysisButton, BorderLayout.EAST);		
		
	    JPanel buttonSubPanel = new JPanel(new BorderLayout());
	    buttonSubPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    buttonSubPanel.setLayout(new BoxLayout(buttonSubPanel, BoxLayout.X_AXIS));
	    buttonSubPanel.add(leafAnalysisButton); 
	    buttonSubPanel.add(leafROIButton);
	    
	    JPanel buttonPanel = new JPanel(new BorderLayout());
	    buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	    buttonPanel.add(buttonSubPanel, BorderLayout.EAST); 
		
		JPanel panel = new JPanel(new BorderLayout());	      
		button1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(boxes, BorderLayout.NORTH);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		return new JScrollPane(panel);
	}
	
	
	/**
	 * Seed analysis panel
	 * @return
	 */
	private JScrollPane getSeedPanel(){
	      
	      /*
	       *  Export panel
	       */
            
//	      csvFolderNameSeed = new JTextField("[Choose file]", 20);
	      seedCSVFile = new JTextField(chooseFolder, 20);
	      seedCSVFile.setEnabled(false);
	      
	      seedExportCSV = new JCheckBox("Send to CSV file", false);
	      seedExportCSV.addItemListener(this);

	      seedOverrideSQL = new JCheckBox("Overide previous measurments", false);
	      
	      seedExportSQL = new JCheckBox("Send to SQL database", true);
	      seedExportSQL.addItemListener(this);
	      
	      seedCSVButton = new JButton("Choose folder");
	      seedCSVButton.setActionCommand("CSV_FOLDER_SEED");
	      seedCSVButton.addActionListener(this);
	      seedCSVButton.setEnabled(false);
	      
	      String[] nameType = {"Process QR", "Process image name", "Use image name"};
	      seedNameJCB = new JComboBox(nameType);
	      seedNameJCB.setSelectedIndex(1);
	      seedNameJCB.addItemListener(this);
	      
	      
	      seedAnalysisButton = new JButton("Run analysis");
	      seedAnalysisButton.setActionCommand("RUN_ANALYSIS_SEED");
	      seedAnalysisButton.addActionListener(this);
	      seedAnalysisButton.setEnabled(true);
	      	      
	      seedFolderButton = new JButton("Choose folder");
	      seedFolderButton.setActionCommand("IMAGE_FOLDER_SEED");
	      seedFolderButton.addActionListener(this);
	      
	      seedImageFolder = new JTextField("[Choose a folder]",25);
	      
	      // Sliders
	      
	      seedMinSizeSlider = new JSlider(JSlider.HORIZONTAL, 0, 600, 100);
	      seedMinSizeSlider.addChangeListener(this);
	      seedMinSizeSlider.setMajorTickSpacing(200);
	      seedMinSizeSlider.setMinorTickSpacing(10);
	      seedMinSizeSlider.setPaintLabels(true);  
	      seedMinSizeSlider.setSnapToTicks(true);
	      seedMinSizeSlider.setPaintTicks(true);
	      
	      seedMaxSizeSlider = new JSlider(JSlider.HORIZONTAL, 0, 600, 200);
	      seedMaxSizeSlider.addChangeListener(this);
	      seedMaxSizeSlider.setMajorTickSpacing(200);
	      seedMaxSizeSlider.setMinorTickSpacing(10);
	      seedMaxSizeSlider.setPaintLabels(true);  
	      seedMaxSizeSlider.setSnapToTicks(true);
	      seedMaxSizeSlider.setPaintTicks(true);
	      
	      seedMinCircSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 7);
	      seedMinCircSlider.addChangeListener(this);
	      seedMinCircSlider.setMajorTickSpacing(10);
	      seedMinCircSlider.setMinorTickSpacing(1);
	      seedMinCircSlider.setSnapToTicks(true);
	      seedMinCircSlider.setPaintLabels(true);  
	      seedMinCircSlider.setPaintTicks(true);
	      
	      seedMaxCircSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, 10);
	      seedMaxCircSlider.addChangeListener(this);
	      seedMaxCircSlider.setMajorTickSpacing(10);
	      seedMaxCircSlider.setPaintLabels(true);  
	      seedMaxCircSlider.setMinorTickSpacing(1);
	      seedMaxCircSlider.setSnapToTicks(true);
	      seedMaxCircSlider.setPaintTicks(true);
	      
	      Enumeration<Integer> e1 = seedMinCircSlider.getLabelTable().keys();
	      while (e1.hasMoreElements()) {
	    	  Integer i = e1.nextElement();
	          JLabel label = (JLabel) seedMinCircSlider.getLabelTable().get(i);
	          label.setText(String.valueOf(i / 10));          
	      }
	      
	      Enumeration<Integer> e2 = seedMaxCircSlider.getLabelTable().keys();
	      while (e2.hasMoreElements()) {
	    	  Integer i = e2.nextElement();
	          JLabel label = (JLabel) seedMaxCircSlider.getLabelTable().get(i);
	          label.setText(String.valueOf(i / 10));          
	      }
	      
	      seedScalePix = new JTextField("2745", 5);	      
	      seedScaleCm = new JTextField("12.7",5);	      
	      seedMinSize = new JTextField(""+seedMinSizeSlider.getValue(),3);
	      seedMinSize.setEditable(false);
	      seedMaxSize = new JTextField(""+seedMaxSizeSlider.getValue(),3);
	      seedMaxSize.setEditable(false);
	      seedMinCirc = new JTextField(""+seedMinCircSlider.getValue(),3);
	      seedMinCirc.setEditable(false);
	      seedMaxCirc = new JTextField(""+seedMaxCircSlider.getValue(),3);
	      seedMaxCirc.setEditable(false);
	      
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
	      panel3.add(seedNameJCB, gbc2);
	      
	      gbc2.gridx = 0;
	      gbc2.gridy = 1;
	      panel3.add(seedExportSQL, gbc2);
	      gbc2.gridx = 1;
	      panel3.add(seedOverrideSQL, gbc2);
	      
	      gbc2.gridx = 0;
	      gbc2.gridy = 2;
	      panel3.add(seedExportCSV, gbc2);	      
	      gbc2.gridx = 1;
	      panel3.add(seedCSVFile, gbc2);
	      gbc2.gridx = 2;
	      panel3.add(seedCSVButton, gbc2);
	      
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
	      	      
	      JPanel paramPanel1 = new JPanel();
	      paramPanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints gbc3 = new GridBagConstraints();
	      gbc3.anchor = GridBagConstraints.WEST;
	      paramPanel1.setLayout(gbl2);
	      
	      
	      gbc3.gridy = 0;
	      gbc3.gridx = 0;
	      paramPanel1.add(new JLabel("Image folder:"), gbc3);
	      gbc3.gridx = 1;
	      paramPanel1.add(seedImageFolder, gbc3);
	      gbc3.gridx = 2;
	      paramPanel1.add(seedFolderButton, gbc3);     

	      // Parameters panel
	      JPanel paramPanel2 = new JPanel(new BorderLayout());
	      paramPanel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      paramPanel2.add(paramPanel1, BorderLayout.WEST);
	     
	      JLabel paramTitle1 = new JLabel("Image Parameters");
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
	      scalePanel1.add(new JLabel("Image scale: "), gbc4);
	      gbc4.gridx = 1;
	      scalePanel1.add(new JLabel("Pixels: "), gbc4);
	      gbc4.gridx = 2;
	      scalePanel1.add(seedScalePix, gbc4);
	      
	      gbc4.gridy = 1;
	      gbc4.gridx = 1;
	      scalePanel1.add(new JLabel("Cm: "), gbc4);
	      gbc4.gridx = 2;
	      scalePanel1.add(seedScaleCm, gbc4);
	      
	      gbc4.gridy = 2;
	      gbc4.gridx = 0;
	      scalePanel1.add(new JLabel("Seed size (px): "), gbc4);
	      gbc4.gridx = 1;
	      scalePanel1.add(new JLabel("Min:"), gbc4);
	      gbc4.gridx = 2;
	      scalePanel1.add(seedMinSize, gbc4);	
	      gbc4.gridx = 3;
	      scalePanel1.add(seedMinSizeSlider, gbc4);	
	      
	      gbc4.gridy = 3;
	      gbc4.gridx = 1;
	      scalePanel1.add(new JLabel("Max:"), gbc4);
	      gbc4.gridx = 2;
	      scalePanel1.add(seedMaxSize, gbc4);	
	      gbc4.gridx = 3;
	      scalePanel1.add(seedMaxSizeSlider, gbc4);	

	      
	      gbc4.gridy = 4;
	      gbc4.gridx = 0;
	      scalePanel1.add(new JLabel("Seed circularity:  "), gbc4);
	      gbc4.gridx = 1;
	      scalePanel1.add(new JLabel("Min: "), gbc4);
	      gbc4.gridx = 2;
	      scalePanel1.add(seedMinCirc, gbc4);	
	      gbc4.gridx = 3;
	      scalePanel1.add(seedMinCircSlider, gbc4);
	      
	      gbc4.gridy = 5;
	      gbc4.gridx = 1;
	      scalePanel1.add(new JLabel("Max: "), gbc4);
	      gbc4.gridx = 2;
	      scalePanel1.add(seedMaxCirc, gbc4);
	      gbc4.gridx = 3;
	      scalePanel1.add(seedMaxCircSlider, gbc4);
	      
	      JPanel scalePanel2 = new JPanel(new BorderLayout());
	      scalePanel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      scalePanel2.add(scalePanel1, BorderLayout.WEST);
	     
	      JLabel scaleTitle1 = new JLabel("Analysis parameters");
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
	      
	      JPanel buttonPanel = new JPanel(new BorderLayout());
	      buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      buttonPanel.add(seedAnalysisButton, BorderLayout.EAST); 
	      
	      JPanel panel = new JPanel(new BorderLayout());
	      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      panel.add(paramPanel3, BorderLayout.NORTH);
	      panel.add(buttonPanel, BorderLayout.SOUTH);
	      
	      return new JScrollPane(panel);
	}
	

	/**
	 * Size analysis panel
	 * @return
	 */
	private JScrollPane getSQLPanel(){
	           
	      sqlDatabase = new JTextField(F_o_P.prefs.get("sqlDatabase", SQLServer.sqlDatabase), 20);
	      sqlHost = new JTextField(F_o_P.prefs.get("sqlHost", SQLServer.sqlHost), 20);
	      sqlConnection = new JTextField(SQLServer.sqlConnection, 20);
	      sqlDriver = new JTextField(SQLServer.sqlDriver, 20);
	      sqlDatabase = new JTextField(SQLServer.sqlDatabase, 20);
	      sqlUsername = new JTextField(SQLServer.sqlUsername, 20);	      
	      sqlPassword = new JTextField(SQLServer.sqlPassword, 20);	
      
	      sqlTableRosette = new JTextField(SQLServer.sqlTableRosette, 20);
	      sqlTableLeaf = new JTextField(SQLServer.sqlTableLeaf, 20);
	      sqlTablePlant = new JTextField(SQLServer.sqlTablePlant, 20);
	      
	      sqlTableSeed = new JTextField(SQLServer.sqlTableSeed, 20);
	      sqlTableExperience = new JTextField(SQLServer.sqlTableExperiment, 20);
	      sqlTableStock = new JTextField(SQLServer.sqlTableStock, 20);
	      sqlTableTreatment = new JTextField(SQLServer.sqlTableTreatment, 20);		
		
	      JLabel exportLabel1 = new JLabel("Driver:");
	      JLabel exportLabel2 = new JLabel("Connection:");
	      JLabel exportLabel3 = new JLabel("Host");
	      JLabel exportLabel4 = new JLabel("Database:");
	      
	      JLabel exportLabel5 = new JLabel("Username:");
	      JLabel exportLabel6 = new JLabel("Password:");
	      
	      JLabel exportLabel7 = new JLabel("Rosette size:");
	      JLabel exportLabel8 = new JLabel("Leaf size:");
	      JLabel exportLabel9 = new JLabel("Seed size:");
	      
	      JLabel exportLabel10 = new JLabel("Experience:");
	      JLabel exportLabel11 = new JLabel("Stocks:");
	      JLabel exportLabel12 = new JLabel("Treatments:");
	      JLabel exportLabel13 = new JLabel("Plants:");

	      
	      JLabel titleLabel1 = new JLabel("Connection parameters:");
	      Font f1 = titleLabel1.getFont();
	      titleLabel1.setFont(f1.deriveFont(f1.getStyle() ^ Font.BOLD));
	      JPanel titlePanel1 = new JPanel(new BorderLayout());
	      titlePanel1.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	      titlePanel1.add(titleLabel1, BorderLayout.WEST);
	      
	      JPanel subPanel1 = new JPanel();
	      subPanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints gbc1 = new GridBagConstraints();
		  gbc1.anchor = GridBagConstraints.WEST;
	      GridBagLayout gbl1 = new GridBagLayout();
	      subPanel1.setLayout(gbl1);
	      JPanel panel1 = new JPanel(new BorderLayout());
	      panel1.setBorder(BorderFactory.createLineBorder(Color.gray));
	      panel1.add(subPanel1, BorderLayout.WEST);
	      
	      JPanel box1 = new JPanel(new BorderLayout());
	      box1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      box1.add(titlePanel1, BorderLayout.NORTH);
	      box1.add(panel1, BorderLayout.CENTER);	      
	      
	      gbc1.gridx = 0;
	      gbc1.gridy = 0;
	      subPanel1.add(exportLabel1, gbc1);
	      gbc1.gridx = 1;
	      subPanel1.add(sqlDriver, gbc1);
	      gbc1.gridx = 0;
	      gbc1.gridy = 1;
	      subPanel1.add(exportLabel2, gbc1);
	      gbc1.gridx = 1;
	      subPanel1.add(sqlConnection, gbc1);
	      gbc1.gridx = 0;
	      gbc1.gridy = 2;
	      subPanel1.add(exportLabel3, gbc1);
	      gbc1.gridx = 1;
	      subPanel1.add(sqlHost, gbc1);
	      gbc1.gridx = 0;
	      gbc1.gridy = 3;
	      subPanel1.add(exportLabel4, gbc1);
	      gbc1.gridx = 1;
	      subPanel1.add(sqlDatabase, gbc1);
	      
	      
	      JLabel titleLabel2 = new JLabel("Database parameters:");
	      Font f2 = titleLabel2.getFont();
	      titleLabel2.setFont(f2.deriveFont(f2.getStyle() ^ Font.BOLD));
	      JPanel titlePanel2 = new JPanel(new BorderLayout());
	      titlePanel2.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	      titlePanel2.add(titleLabel2, BorderLayout.WEST);
	      
	      JPanel subPanel2 = new JPanel();
	      subPanel2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints gbc2 = new GridBagConstraints();
		  gbc2.anchor = GridBagConstraints.WEST;
	      GridBagLayout gbl2 = new GridBagLayout();
	      subPanel2.setLayout(gbl2);
	      JPanel panel2 = new JPanel(new BorderLayout());
	      panel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      panel2.add(subPanel2, BorderLayout.WEST);
	      
	      JPanel box2 = new JPanel(new BorderLayout());
	      box2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      box2.add(titlePanel2, BorderLayout.NORTH);
	      box2.add(panel2, BorderLayout.CENTER);	      
	      
	      gbc2.gridx = 0;
	      gbc2.gridy = 0;
	      subPanel2.add(exportLabel5, gbc2);
	      gbc2.gridx = 1;
	      subPanel2.add(sqlUsername, gbc2);
	      gbc2.gridx = 0;
	      gbc2.gridy = 1;
	      subPanel2.add(exportLabel6, gbc2);
	      gbc2.gridx = 1;
	      subPanel2.add(sqlPassword, gbc2);	      
	      
	      
	      
	      
	      JLabel titleLabel3 = new JLabel("Data table parameters:");
	      Font f3 = titleLabel3.getFont();
	      titleLabel3.setFont(f3.deriveFont(f3.getStyle() ^ Font.BOLD));
	      JPanel titlePanel3 = new JPanel(new BorderLayout());
	      titlePanel3.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	      titlePanel3.add(titleLabel3, BorderLayout.WEST);
	      
	      JPanel subPanel3 = new JPanel();
	      subPanel3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints gbc3 = new GridBagConstraints();
	      gbc3.anchor = GridBagConstraints.WEST;
	      GridBagLayout gbl3 = new GridBagLayout();
	      subPanel3.setLayout(gbl3);
	      JPanel panel3 = new JPanel(new BorderLayout());
	      panel3.setBorder(BorderFactory.createLineBorder(Color.gray));
	      panel3.add(subPanel3, BorderLayout.WEST);
	      
	      JPanel box3 = new JPanel(new BorderLayout());
	      box3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      box3.add(titlePanel3, BorderLayout.NORTH);
	      box3.add(panel3, BorderLayout.CENTER);

	      gbc3.gridx = 0;
	      gbc3.gridy = 0;
	      subPanel3.add(exportLabel7, gbc3);
	      gbc3.gridx = 1;
	      subPanel3.add(sqlTableRosette, gbc3);
	      gbc3.gridx = 0;
	      gbc3.gridy = 1;
	      subPanel3.add(exportLabel8, gbc3);
	      gbc3.gridx = 1;
	      subPanel3.add(sqlTableLeaf, gbc3);
	      gbc3.gridx = 0;
	      gbc3.gridy = 2;
	      subPanel3.add(exportLabel9, gbc3);
	      gbc3.gridx = 1;
	      subPanel3.add(sqlTableSeed, gbc3);
	      
	      
	      
	      JLabel titleLabel4 = new JLabel("Measurment table parameters:");
	      Font f4 = titleLabel4.getFont();
	      titleLabel4.setFont(f4.deriveFont(f4.getStyle() ^ Font.BOLD));
	      JPanel titlePanel4 = new JPanel(new BorderLayout());
	      titlePanel4.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
	      titlePanel4.add(titleLabel4, BorderLayout.WEST);
      	      
	      JPanel subPanel4 = new JPanel();
	      subPanel4.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints gbc4 = new GridBagConstraints();
		  gbc4.anchor = GridBagConstraints.WEST;
	      GridBagLayout gbl4 = new GridBagLayout();
	      subPanel4.setLayout(gbl4);
	      JPanel panel4 = new JPanel(new BorderLayout());
	      panel4.setBorder(BorderFactory.createLineBorder(Color.gray));
	      panel4.add(subPanel4, BorderLayout.WEST);
	      
	      JPanel box4 = new JPanel(new BorderLayout());
	      box4.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      box4.add(titlePanel4, BorderLayout.NORTH);
	      box4.add(panel4, BorderLayout.CENTER);
	      
	      gbc4.gridx = 0;
	      gbc4.gridy = 0;
	      subPanel4.add(exportLabel10, gbc4);
	      gbc4.gridx = 1;
	      subPanel4.add(sqlTableExperience, gbc4);
	      gbc4.gridx = 0;
	      gbc4.gridy = 1;
	      subPanel4.add(exportLabel11, gbc4);
	      gbc4.gridx = 1;
	      subPanel4.add(sqlTableStock, gbc4);
	      gbc4.gridx = 0;
	      gbc4.gridy = 2;
	      subPanel4.add(exportLabel12, gbc4);
	      gbc4.gridx = 1;
	      subPanel4.add(sqlTableTreatment, gbc4);
	      gbc4.gridx = 0;
	      gbc4.gridy = 3;
	      subPanel4.add(exportLabel13, gbc4);
	      gbc4.gridx = 1;
	      subPanel4.add(sqlTablePlant, gbc4);
	      	      

	      JPanel boxes1 = new JPanel(new BorderLayout());
	      boxes1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      boxes1.add(box1, BorderLayout.WEST);
	      boxes1.add(box4, BorderLayout.EAST);
	      
	      JPanel boxes2 = new JPanel(new BorderLayout());
	      boxes2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      boxes2.add(box2, BorderLayout.WEST);
	      boxes2.add(box3, BorderLayout.EAST);
	      
	      JPanel allBoxes = new JPanel(new BorderLayout());
	      allBoxes.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      allBoxes.setLayout(new BoxLayout(allBoxes, BoxLayout.Y_AXIS));
	      allBoxes.add(boxes1);
	      allBoxes.add(boxes2);
		  
	      // All parameters panel
	      sqlUpdateButton = new JButton("Save settings");
	      sqlUpdateButton.setActionCommand("SQL_UPDATE");
	      sqlUpdateButton.addActionListener(this);
	      
	      sqlResetButton = new JButton("Reset to default settings");
	      sqlResetButton.setActionCommand("SQL_RESET");
	      sqlResetButton.addActionListener(this);
	      
	      sqlTestConnectionButton = new JButton("Test Connection");
	      sqlTestConnectionButton.setActionCommand("SQL_TEST");
	      sqlTestConnectionButton.addActionListener(this);
	      
	      JPanel buttons = new JPanel(new BorderLayout());
	      buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
	      buttons.add(Box.createHorizontalGlue());
	      buttons.add(sqlResetButton);
	      buttons.add(sqlTestConnectionButton);
	      buttons.add(sqlUpdateButton);

	      JPanel allButtons = new JPanel(new BorderLayout());
	      allButtons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      allButtons.add(buttons, BorderLayout.EAST);
	      
	      JPanel panel = new JPanel(new BorderLayout());
	      panel.add(allBoxes, BorderLayout.NORTH);
	      panel.add(allButtons, BorderLayout.SOUTH);
	      
	      return new JScrollPane(panel);
	}


	/**
	 * Size analysis panel
	 * @return
	 */
	private JScrollPane getPetriPanel(){
	        
	      /*
	       * Parameters
	       */

	      petriRotate = new JCheckBox("Rotate image", true);	      
	      petriRemoveBackground = new JCheckBox("Substract background", false);	      
	      
	      petriRunButton = new JButton("Run image preparation");
	      petriRunButton.setActionCommand("RUN_PETRI");
	      petriRunButton.addActionListener(this);
	      petriRunButton.setEnabled(false);
	      
	      petriROIButton = new JButton("SET ROI's");
	      petriROIButton.setActionCommand("PETRI_ROI");
	      petriROIButton.addActionListener(this);
	      	      
	      petriFolderButton = new JButton("Choose folder");
	      petriFolderButton.setActionCommand("PETRI_FOLDER");
	      petriFolderButton.addActionListener(this);
	      
	      petriImageFolder = new JTextField("",20);

	      petriStartingDate = new JTextField("2013-01-31", 10);
	      petriStartingDate.setAlignmentX(Component.LEFT_ALIGNMENT);	
	      
	      
	      JPanel subpanel1 = new JPanel();
	      subpanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints gbc1 = new GridBagConstraints();
	      gbc1.anchor = GridBagConstraints.WEST;
	      GridBagLayout gbl1 = new GridBagLayout();
	      subpanel1.setLayout(gbl1);		

	      JPanel panel1 = new JPanel(new BorderLayout());
	      panel1.setBorder(BorderFactory.createLineBorder(Color.gray));
	      panel1.add(subpanel1, BorderLayout.WEST);
		      
	      JLabel title1 = new JLabel("Images parameters");
	      Font f1 = title1.getFont();
	      title1.setFont(f1.deriveFont(f1.getStyle() ^ Font.BOLD)); 
			
	      JPanel box1 = new JPanel(new BorderLayout());
	      box1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      box1.add(title1, BorderLayout.NORTH);
	      box1.add(panel1, BorderLayout.SOUTH);
	      
	      gbc1.gridy = 1;
	      gbc1.gridx = 0;
	      subpanel1.add(new JLabel("Choose image folder"), gbc1);
	      gbc1.gridx = 1;
	      subpanel1.add(petriImageFolder, gbc1);
	      gbc1.gridx = 2;
	      subpanel1.add(petriFolderButton, gbc1);  
	      
	      
	      JPanel subpanel2 = new JPanel();
	      subpanel2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      GridBagConstraints gbc2 = new GridBagConstraints();
	      gbc2.anchor = GridBagConstraints.WEST;
	      GridBagLayout gbl2 = new GridBagLayout();
	      subpanel2.setLayout(gbl2);		

	      JPanel panel2 = new JPanel(new BorderLayout());
	      panel2.setBorder(BorderFactory.createLineBorder(Color.gray));
	      panel2.add(subpanel2, BorderLayout.WEST);
		      
	      JLabel title2 = new JLabel("Analysis Parameters");
	      Font f2 = title2.getFont();
	      title2.setFont(f2.deriveFont(f2.getStyle() ^ Font.BOLD)); 
			
	      JPanel box2 = new JPanel(new BorderLayout());
	      box2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      box2.add(title2, BorderLayout.NORTH);
	      box2.add(panel2, BorderLayout.SOUTH);
	      
	      
	      gbc2.gridy = 0;
	      gbc2.gridx = 0;
	      subpanel2.add(new JLabel("Experiment starting date: "), gbc2);
	      gbc2.gridx = 1;
	      subpanel2.add(petriStartingDate, gbc2);
	      
	      gbc2.gridy = 2;
	      gbc2.gridx = 0;
	      subpanel2.add(petriRotate, gbc2);
	      gbc2.gridy = 3;
	      gbc2.gridx = 0;
	      subpanel2.add(petriRemoveBackground, gbc2);
	      	      
	      
	      // All parameters panel
	      
	      JPanel allBoxes = new JPanel(new BorderLayout());
	      allBoxes.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      allBoxes.setLayout(new BoxLayout(allBoxes, BoxLayout.Y_AXIS));
	      allBoxes.add(box1);
	      allBoxes.add(box2);
	      
	      JPanel buttonPanel = new JPanel(new BorderLayout());
	      buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
	      buttonPanel.add(petriRunButton); 
	      buttonPanel.add(petriROIButton); 
	      
	      JPanel buttonBox = new JPanel(new BorderLayout());
	      buttonBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      buttonBox.add(buttonPanel, BorderLayout.EAST);
	      
	      JPanel panel = new JPanel(new BorderLayout());
	      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	      panel.add(allBoxes, BorderLayout.NORTH);
	      panel.add(buttonBox, BorderLayout.SOUTH);
	      
	      return new JScrollPane(panel);
	}
	
	

	/**
	 * 
	 */
	public void stateChanged(ChangeEvent e) {
		Object item = e.getSource();
		
//------------------------------------------------------------		
// SEEDS			
		if(item == seedMinSizeSlider){
	    	Integer val = seedMinSizeSlider.getValue();
	        seedMinSize.setText(val.toString());
	        if(val > seedMaxSizeSlider.getValue()) seedMaxSizeSlider.setValue(val);
		}

		
		if(item == seedMaxSizeSlider){
	    	Integer val = seedMaxSizeSlider.getValue();
	        seedMaxSize.setText(val.toString());
	        if(val < seedMinSizeSlider.getValue()) seedMinSizeSlider.setValue(val);
		}
		
		if(item == seedMinCircSlider){
	    	Integer val = seedMinCircSlider.getValue();
	    	Float val1 = (float) val/10;
	        seedMinCirc.setText(val1.toString());
	        if(val > seedMaxCircSlider.getValue()) seedMaxCircSlider.setValue(val);
		}
		
		if(item == seedMaxCircSlider){
	    	Integer val = seedMaxCircSlider.getValue();
	    	Float val1 = (float) val/10;
	    	seedMaxCirc.setText(val1.toString());
	        if(val < seedMinCircSlider.getValue()) seedMinCircSlider.setValue(val);
		}
		
//------------------------------------------------------------		
// SCREENING		
		if(item == screeningMinSizeSlider){
	    	Integer val = screeningMinSizeSlider.getValue();
	        screeningMinSize.setText(val.toString());
		}
		if(item == screeningIterationsSlider){
	    	Integer val = screeningIterationsSlider.getValue();
	        screeningIterations.setText(val.toString());
		}
		if(item == screeningPlantSlider){
			Integer val = screeningPlantSlider.getValue();
			screeningSTDev.setText(""+(int)screeningResults[screeningResults.length - val][0]+"%");
			screeningPlants.setText(""+val);
		    if(screeningResults[screeningResults.length - val][0] > 20) screeningSTDev.setForeground(Color.red); 
		    else screeningSTDev.setForeground(dGreen); 
		    

		    // Update the lines on th charts
		    screeningLineSerie1.removeAllSeries();
			XYSeries line1 = new XYSeries("");
			line1.add(0,  screeningResults[screeningResults.length - val][1]);
			line1.add(screeningResults.length, screeningResults[screeningResults.length - val][1]);
			XYSeries line2 = new XYSeries("");
			line2.add(0,  screeningResults[screeningResults.length - val][2]);
			line2.add(screeningResults.length, screeningResults[screeningResults.length - val][2]);
			XYSeries line3 = new XYSeries("");
			line3.add(0,  screeningResults[screeningResults.length - val][3]);
			line3.add(screeningResults.length, screeningResults[screeningResults.length - val][3]);
			screeningLineSerie1.addSeries(line1);
			screeningLineSerie1.addSeries(line2);
			screeningLineSerie1.addSeries(line3);
			screeningChartPanel1.repaint();
		    
		    
			screeningLineSerie2.removeAllSeries();
			XYSeries line4 = new XYSeries("XYGraph");
			line4.add(screeningPlantSlider.getValue(), 0);
			line4.add(screeningPlantSlider.getValue(), screeningResults[0][0]);
			screeningLineSerie2.addSeries(line4);
			screeningChartPanel2.repaint(); 
		         
		}
		
//------------------------------------------------------------		
// ROSETTE	
		if(item == rosetteConservationSlider){
	    	Integer val = rosetteConservationSlider.getValue();
	    	Float val1 = (float) val/10;
	    	rosetteConservationFactor.setText(val1.toString());
		}
	}
	
	
	/**
	 * 
	 * @param e
	 */
	public void itemStateChanged(ItemEvent e) {
		Object item = e.getItem();
		
//---------------------------------------------------------------------------------------------------		
// QR PANEL
		if (item == qrManual){
		    Component[] cp = qrPanelManual.getComponents();
		    Component[] cp1 =qrPanelDB.getComponents();
		    for(int k = 0; k < cp.length; k++) cp[k].setEnabled(qrManual.isSelected());
		    for(int k = 0; k < cp1.length; k++){
		    	try{
		    		JPanel jp = (JPanel) cp1[k];
		    		Component[] subcp = jp.getComponents();
		    		for(int j = 0; j < subcp.length; j++)subcp[j].setEnabled(!qrManual.isSelected());
		    	}
		    	catch(java.lang.ClassCastException cce){}
		    }
		    if(qrManual.isSelected()) qrExportToSQL.setSelected(false);
		    qrExportToSQL.setEnabled(!qrManual.isSelected());
		    qrExpList.setEnabled(!qrManual.isSelected());
		    qrTreatmentList.setEnabled(!qrManual.isSelected());
		    qrStockList.setEnabled(!qrManual.isSelected());
		    qrSetupList.setEnabled(!qrManual.isSelected());
		}	    	  
		
//---------------------------------------------------------------------------------------------------		
// ROSETTE PANEL
		if (item == rosetteExportCSV){
			rosetteCSVFile.setEnabled(rosetteExportCSV.isSelected());
			this.rosetteCSVButton.setEnabled(rosetteExportCSV.isSelected());
			if(!rosetteExportCSV.isSelected() && !rosetteExportSQL.isSelected()) rosetteExportSQL.setSelected(true);	
		}	
		if (item == rosetteExportSQL){
			rosetteOverrideSQL.setEnabled(rosetteExportSQL.isSelected());
			if(!rosetteExportCSV.isSelected() && !rosetteExportSQL.isSelected()) rosetteExportCSV.setSelected(true);	
		}
		
		if (item == rosetteNameJCB){
			if(rosetteNameJCB.getSelectedIndex() == 2) rosetteExportSQL.setSelected(false);
		}
		
//---------------------------------------------------------------------------------------------------		
// SEED PANEL
		
		if(item == seedMinSize){
			
			
		}
		if (item == seedExportCSV){
			seedCSVFile.setEnabled(seedExportCSV.isSelected());
			this.seedCSVButton.setEnabled(seedExportCSV.isSelected());
			if(!seedExportCSV.isSelected() && !seedExportSQL.isSelected()) seedExportSQL.setSelected(true);	
		}	
		if (item == seedExportSQL){
			seedOverrideSQL.setEnabled(seedExportSQL.isSelected());
			if(!seedExportCSV.isSelected() && !seedExportSQL.isSelected()) seedExportCSV.setSelected(true);	
		}
		
		if (item == seedNameJCB){
			if(seedNameJCB.getSelectedIndex() == 2) seedExportSQL.setSelected(false);
		}	
		
		
//---------------------------------------------------------------------------------------------------		
// LEAF PANEL
		if (item == leafExportCSV){
			leafCSVFile.setEnabled(leafExportCSV.isSelected());
			this.leafCSVButton.setEnabled(leafExportCSV.isSelected());
			if(!leafExportCSV.isSelected() && !leafExportSQL.isSelected()) leafExportSQL.setSelected(true);	
		}	
		if (item == leafExportSQL){
			leafOverrideSQL.setEnabled(leafExportSQL.isSelected());
			if(!leafExportCSV.isSelected() && !leafExportSQL.isSelected()) leafExportCSV.setSelected(true);	
		}
		
		if (item == leafNameJCB){
			if(leafNameJCB.getSelectedIndex() == 2) leafExportSQL.setSelected(false);
		}		
	
	
//---------------------------------------------------------------------------------------------------		
// SCREENING PANEL
		if (item == screeningProcessing){
			screeningProcessingButton.setEnabled(screeningProcessing.isSelected());
			screeningNPlants.setEnabled(!screeningProcessing.isSelected());
			screeningRunButton.setEnabled(!screeningProcessing.isSelected());
		}
		
		
	}
	
	
		
	/**
	 * Action definition
	 */
	public void actionPerformed(ActionEvent ae) {

//---------------------------------------------------------------------------------------------------		
// SEED ANALYSIS
		if (ae.getActionCommand() == "CSV_FOLDER_SEED") { 
			
			JFileChooser fc = new JFileChooser();
			csvFilter csvf = new csvFilter ();
			fc.setFileFilter(csvf);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = fc.showDialog(FoPInterface.this, "Save");
			
			if (returnVal == JFileChooser.APPROVE_OPTION){ 
				String fName = fc.getSelectedFile().toString();
				if(!fName.endsWith(".csv")) fName = fName.concat(".csv");
				seedCSVFile.setText(fName);
			}
			else IJ.log("Open command cancelled.");     
		}	
		
		else if (ae.getActionCommand() == "IMAGE_FOLDER_SEED") {
			
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showDialog(FoPInterface.this, "Choose folder");
			
			if (returnVal == JFileChooser.APPROVE_OPTION){ 
				String fName = fc.getSelectedFile().toString();
				if(!fName.endsWith("/")) fName = fName+"/";
				seedImageFolder.setText(fName);
			}
			else IJ.log("Folder command cancelled.");     
		}	
		
		else if (ae.getActionCommand() == "RUN_ANALYSIS_SEED"){  

			if (seedImageFolder.getText().equals("")){
				IJ.log("Please choose an image folder");
				return;
			}
			if(seedExportCSV.isSelected() && seedCSVFile.getText().equals("")){
				IJ.log("Please choose a csv folder");
				return;
			}
			if(seedExportSQL.isSelected() && seedNameJCB.getSelectedIndex() == 2){
				IJ.log("Cannot export to SQL without processing name. Please choose an other option");
				return;
			}
			new SeedAnalysis(new File(seedImageFolder.getText()), 
					seedExportCSV.isSelected(), 
					seedCSVFile.getText(),
					seedExportSQL.isSelected(), 
					seedOverrideSQL.isSelected(),
					Float.valueOf(seedScalePix.getText()), 
					Float.valueOf(seedScaleCm.getText()), 
					Integer.valueOf(seedMinSize.getText()), 
					Integer.valueOf(seedMaxSize.getText()), 
					Float.valueOf(seedMinCirc.getText()), 
					Float.valueOf(seedMaxCirc.getText()), 
					seedNameJCB.getSelectedIndex()
					);
		}		

//---------------------------------------------------------------------------------------------------		
// root ANALYSIS
		else if (ae.getActionCommand() == "CSV_FOLDER_root") { 
	    	  
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
		
//---------------------------------------------------------------------------------------------------		
// SQL PARAMETERS	
		else if (ae.getActionCommand() == "SQL_UPDATE") {
			updateSQL();
		}
		
		else if (ae.getActionCommand() == "SQL_RESET") {
			resetSQL();
		}
		
		else if (ae.getActionCommand() == "SQL_TEST") {
			testSQL();
		}
		
		
		
//---------------------------------------------------------------------------------------------------		
// ROSETTE ANALYSIS
		else if (ae.getActionCommand() == "CSV_FOLDER_ROSETTE") { 
	    	  
			JFileChooser fc = new JFileChooser();
			csvFilter csvf = new csvFilter ();
			fc.setFileFilter(csvf);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = fc.showDialog(FoPInterface.this, "Save");
			   
			if (returnVal == JFileChooser.APPROVE_OPTION){ 
				String fName = fc.getSelectedFile().toString();
				if(!fName.endsWith(".csv")) fName = fName.concat(".csv");
				rosetteCSVFile.setText(fName);
			}
			else IJ.log("Open command cancelled.");     
		}	
		
		else if (ae.getActionCommand() == "SET_ROI_ROSETTE"){  

			if(rosetteNameJCB.getSelectedIndex() == 2){
				IJ.log("Please choose an other image name processing method");
				return;
			}
			if (rosetteImageFolder.getText().equals("")){
				IJ.log("Please choose an image folder");
				return;
			}
			if( this.rosetteExportCSV.isSelected() && rosetteCSVFile.getText().equals("")){
				IJ.log("Please choose a csv folder");
				return;
			}
			ra = new RosetteAnalysis(new File(rosetteImageFolder.getText()), 
					Integer.valueOf(rosetteScalePix.getText()),
					Float.valueOf(rosetteScaleCm.getText()),
					Float.valueOf(rosetteConservationFactor.getText()),
					rosetteExportSQL.isSelected(),
					rosetteExportCSV.isSelected(),
					rosetteCSVFile.getText(),
					rosetteOverrideSQL.isSelected(),
					rosetteNameJCB.getSelectedIndex());    	  
		}
		
		else if(ae.getActionCommand() == "RUN_ANALYSIS_ROSETTE"){
			if(ra.processAllImages()) IJ.log("Image analysis done");
		}

//---------------------------------------------------------------------------------------------------						
// QR CODES
		else if (ae.getActionCommand() == "QR_FOLDER") {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showDialog(FoPInterface.this, "Choose directory");
			
			if (returnVal == JFileChooser.APPROVE_OPTION){ 
				String fName = fc.getSelectedFile().toString();
				if(!fName.endsWith("/")) fName = fName+"/";
				qrPath.setText(fName);
			}
			else IJ.log("Save command cancelled.");     
		}	
	      
		else if(ae.getActionCommand() == "CREATE_QR"){
			createQR();
		}

//---------------------------------------------------------------------------------------------------					
// LEAF ANALYSIS				
		else if (ae.getActionCommand() == "IMAGE_FOLDER_LEAF") {
			
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showDialog(FoPInterface.this, "Choose folder");
			
			if (returnVal == JFileChooser.APPROVE_OPTION){ 
				String fName = fc.getSelectedFile().toString();
				if(!fName.endsWith("/")) fName = fName+"/";
				leafImageFolder.setText(fName);
			}
			else IJ.log("Folder command cancelled.");     
		}		

		else if (ae.getActionCommand() == "CSV_FOLDER_LEAF") { 
			    	  
			JFileChooser fc = new JFileChooser();
			csvFilter csvf = new csvFilter ();
			fc.setFileFilter(csvf);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = fc.showDialog(FoPInterface.this, "Save");
					   
			if (returnVal == JFileChooser.APPROVE_OPTION){ 
				String fName = fc.getSelectedFile().toString();
				if(!fName.endsWith(".csv")) fName = fName.concat(".csv");
				leafCSVFile.setText(fName);
			}
			else IJ.log("Open command cancelled.");     
		}
		      			
		else if(ae.getActionCommand() == "SET_ROI_LEAF"){
			if (leafImageFolder.getText().equals(chooseFolder)){
				IJ.log("Please choose an image folder");
				return;
			}
			if(leafExportCSV.isSelected() && leafCSVFile.getText().equals(chooseFile)){
				IJ.log("Please choose a csv folder");
				return;
			}
			if(leafExportSQL.isSelected() && leafNameJCB.getSelectedIndex() == 2){
				IJ.log("Cannot export to SQL without processing name. Please choose an other option");
				return;
			}
			la = new LeafAnalysis(new File(leafImageFolder.getText()),
					leafExportSQL.isSelected(),
					leafOverrideSQL.isSelected(),
					leafExportCSV.isSelected(),
					leafCSVFile.getText(),
					Integer.valueOf(leafScalePix.getText()),
					Float.valueOf(leafScaleCm.getText()),
					leafNameJCB.getSelectedIndex()					
					);	
			leafAnalysisButton.setEnabled(true);
			leafROIButton.setEnabled(false);
		}
		
		else if(ae.getActionCommand() == "RUN_ANALYSIS_LEAF"){
			if(la.processAllImages()) IJ.log("Leaf image analysis done");
		}
			      		

//---------------------------------------------------------------------------------------------------					
// PETRI ANALYSIS			
		if (ae.getActionCommand() == "PETRI_FOLDER") {
			
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = fc.showDialog(FoPInterface.this, "Choose folder");
			
			if (returnVal == JFileChooser.APPROVE_OPTION){ 
				String fName = fc.getSelectedFile().toString();
				if(!fName.endsWith("/")) fName = fName+"/";
				petriImageFolder.setText(fName);
			}
			else IJ.log("Folder command cancelled.");     
		}
		
		else if(ae.getActionCommand() == "RUN_PETRI"){
			if(pp.processAllImages()) IJ.log("Petri Processor done");
		}
		
		else if(ae.getActionCommand() == "PETRI_ROI"){
			if (petriImageFolder.getText().equals("")){
				IJ.log("Please choose an image folder");
				return;
			}

			pp = new PetriProcessor(new File(petriImageFolder.getText()), petriStartingDate.getText(), petriRotate.isSelected(), petriRemoveBackground.isSelected());	
			petriRunButton.setEnabled(true);				
		}			
		
//---------------------------------------------------------------------------------------------------					
// SCREENING ANALYSIS			
		else if (ae.getActionCommand() == "RUN_SCREENING"){
			
			if(screeningProcessing.isSelected()) sa.analyse(false, Integer.valueOf(screeningPlants.getText()));
			else{
				String[] filesNames = Util.getArrayFromString(screeningImages.getText(), "\n", false);
				File[] files = new File[filesNames.length];
				for(int i = 0; i < (filesNames.length); i++) files[i] = new File(filesNames[i]);
				
				sa = new ScreeningAnalysis(files, 
						screeningMeasureJCB.getSelectedIndex(), 
						Integer.valueOf(screeningMinSize.getText()), 
						Integer.valueOf(screeningIterations.getText()), 
						screeningMask.isSelected());
				sa.analyse(true, Integer.valueOf(screeningNPlants.getText()));
			}
						
		}
		
		else if (ae.getActionCommand() == "RUN_SCREENING_PROCESSING"){
			screeningProcessing();

		}	
		
		else if (ae.getActionCommand() == "SCREENING_IMAGE"){
			JFileChooser fc = new JFileChooser();
			ImageFilter imf = new ImageFilter ();
			fc.setFileFilter(imf);
			fc.setName("Choose images to screen");
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setMultiSelectionEnabled(true);
			int returnVal = fc.showDialog(FoPInterface.this, "Select Images");
			if (returnVal == JFileChooser.APPROVE_OPTION){ 
				File[] f = fc.getSelectedFiles();
				String str = "";
				for(int i = 0; i < f.length; i++){
					str = str.concat(f[i].getAbsolutePath()+"\n");
				}
				screeningImages.setText(str);
			}
			else IJ.log("Image selection cancelled."); 
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
	 * Get the different treatments in the database
	 * @param userSQL
	 * @param pswSQL
	 * @param db
	 */
	private void getTreatments(){
		qrTreatmentID = new ArrayList<String>();
		qrTreatmentName = new ArrayList<String>();	
		try{
			Class.forName(SQLServer.sqlDriver).newInstance();
			java.sql.Connection conn = java.sql.DriverManager.getConnection(
					SQLServer.sqlConnection+"?user="+SQLServer.sqlUsername+"&password="+SQLServer.sqlPassword
					);
			java.sql.Statement stmt=conn.createStatement();
			java.sql.ResultSet rs=stmt.executeQuery(
					"SELECT treatment_id, treatment_name FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableTreatment
					);
			while(rs.next()){
				qrTreatmentID.add(rs.getString(1));
				qrTreatmentName.add(rs.getString(2));
			}
		} 
		catch(Exception e){
			IJ.log("getTreatments failed: "+e);
		}		
	}
	
	
	/**
	 * Get the different seed stocks in the database
	 * @param userSQL
	 * @param pswSQL
	 * @param db
	 * @return
	 */
	private void getStocks(){
		qrStockID = new ArrayList<String>();
		qrStockName = new ArrayList<String>();	
		try{
			Class.forName(SQLServer.sqlDriver).newInstance();
			java.sql.Connection conn = java.sql.DriverManager.getConnection(
					SQLServer.sqlConnection+"?user="+SQLServer.sqlUsername+"&password="+SQLServer.sqlPassword
					);
			java.sql.Statement stmt=conn.createStatement();
			java.sql.ResultSet rs=stmt.executeQuery(
					"SELECT stock_id, genotype_name FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableStock
					);
			while(rs.next()){
				qrStockID.add(rs.getString(1));
				qrStockName.add(rs.getString(2));
			}
		} 
		catch(Exception e){
			IJ.log("getStocks failed: "+e);
		}
	}
	
	/**
	 * Get the different experiments in the database
	 * @param userSQL
	 * @param pswSQL
	 * @param db
	 * @return
	 */
	private void getExperiments(){
		qrExperimentID = new ArrayList<String>();
		qrExperimentName = new ArrayList<String>();	
		try{
			Class.forName(SQLServer.sqlDriver).newInstance();
			java.sql.Connection conn = java.sql.DriverManager.getConnection(
					SQLServer.sqlConnection+"?user="+SQLServer.sqlUsername+"&password="+SQLServer.sqlPassword
					);
			java.sql.Statement stmt=conn.createStatement();
			java.sql.ResultSet rs=stmt.executeQuery(
					"SELECT experiment_id, experiment_name FROM "+SQLServer.sqlDatabase+"."+SQLServer.sqlTableExperiment
					);
			while(rs.next()){
				qrExperimentID.add(rs.getString(1));
				qrExperimentName.add(rs.getString(2));
			}
		} 
		catch(Exception e){
			IJ.log("getExperiments failed: "+e);
		}		
	}
	
	/**
	 * Get the different setups in the database
	 * @param userSQL
	 * @param pswSQL
	 * @param db
	 * @return
	 */
	private void getSetups(){
		qrSetupID = new ArrayList<Integer>();
		qrSetupName = new ArrayList<String>();	
		try{
			Class.forName(SQLServer.sqlDriver).newInstance();
			java.sql.Connection conn = java.sql.DriverManager.getConnection(
					SQLServer.sqlConnection+"?user="+SQLServer.sqlUsername+"&password="+SQLServer.sqlPassword
					);
			java.sql.Statement stmt=conn.createStatement();
			java.sql.ResultSet rs=stmt.executeQuery("" +
					"SELECT setup_id, setup_name FROM "+SQLServer.sqlDatabase+".setup "
					);
			while(rs.next()){
				qrSetupID.add(rs.getInt(1));
				qrSetupName.add(rs.getString(2));
			}
		} 
		catch(Exception e){
			IJ.log("getSetups failed: "+e);
		}		
	}
	

	/**
	 * Create the QR codes
	 */
	private void createQR(){

		if (qrPath.getText().equals(chooseFolder)){
			IJ.log("Please choose a folder to save the QR codes");
			return;
		}
		
		
		if(!this.qrManual.isSelected()){
			new QRGenerator1(qrExperimentID.get(qrExpList.getSelectedIndex()), qrPath.getText());

//			int[] stInd = qrStockList.getSelectedIndices();
//			String[] genotypes = new String[stInd.length];
//			String[] genotypeNames = new String[stInd.length];
//			for(int i = 0; i < genotypes.length; i++){ 
//				genotypeNames[i] = qrStockName.get(stInd[i]);
//				genotypes[i] = qrStockID.get(stInd[i]);
//			}
//		
//			int[] trInd = qrTreatmentList.getSelectedIndices();
//			String[] treatments = new String[trInd.length];
//			String[] treatmentNames = new String[trInd.length];
//			for(int i = 0; i < treatments.length; i++){ 
//				treatmentNames[i] = qrTreatmentName.get(trInd[i]);
//				treatments[i] = qrTreatmentID.get(trInd[i]);
//			}
//		
//			new QRGenerator(qrExperimentID.get(qrExpList.getSelectedIndex()),
//					genotypes, genotypeNames,
//					treatments, treatmentNames,
//					qrSetupID.get(qrSetupList.getSelectedIndex()), 
//					Integer.valueOf(qrRepetitions.getText()),
//					Integer.valueOf(qrPlant.getText()),
//					qrPath.getText(),
//					qrExportToSQL.isSelected()
//				);
		}
		else{	
			String[] genotypes = Util.getArrayFromString(qrGenotypes.getText(), ",", true);
			String[] treatments = Util.getArrayFromString(qrTreatments.getText(), ",", true);
			
			new QRGenerator(qrExperiment.getText(),
					genotypes, genotypes,
					treatments, treatments,
					qrSetupID.get(qrSetupList.getSelectedIndex()), 
					Integer.valueOf(qrRepetitionsMan.getText()),
					0,
					qrPath.getText(),
					qrExportToSQL.isSelected()
			);		
		}
	}
	
	
	/**
	 * Update the SQL parameters
	 */
	private void updateSQL(){
		F_o_P.prefs.put("sqlDatabase", sqlDatabase.getText());
		F_o_P.prefs.put("sqlHost", sqlHost.getText());
		F_o_P.prefs.put("sqlDriver", sqlDriver.getText());
		F_o_P.prefs.put("sqlConnection", sqlConnection.getText());
		F_o_P.prefs.put("sqlPassword", sqlPassword.getText());
		F_o_P.prefs.put("sqlUsername", sqlUsername.getText());
		F_o_P.prefs.put("sqlTableRosette", sqlTableRosette.getText());
		F_o_P.prefs.put("sqlTableLeaf", sqlTableLeaf.getText());
		F_o_P.prefs.put("sqlTableSeed", sqlTableSeed.getText());
		F_o_P.prefs.put("sqlTableExperience", sqlTableExperience.getText());
		F_o_P.prefs.put("sqlTableTreatment", sqlTableTreatment.getText());
		F_o_P.prefs.put("sqlTableStock", sqlTableStock.getText());
		F_o_P.prefs.put("sqlTablePlant", sqlTablePlant.getText());
		
		SQLServer.update();
	}
	
	/**
	 * Reset the SQL parameters
	 */
	private void resetSQL(){
		
		SQLServer.setDefault();
		
		sqlConnection.setText(SQLServer.sqlConnection);
		sqlHost.setText(SQLServer.sqlHost);
		sqlDriver.setText(SQLServer.sqlDriver);
		sqlDatabase.setText(SQLServer.sqlDatabase);
		sqlUsername.setText(SQLServer.sqlUsername);
		sqlPassword.setText(SQLServer.sqlPassword);
		sqlTableRosette.setText(SQLServer.sqlTableRosette);
		sqlTableLeaf.setText(SQLServer.sqlTableLeaf);
		sqlTableSeed.setText(SQLServer.sqlTableSeed);
		sqlTableExperience.setText(SQLServer.sqlTableExperiment);
		sqlTableStock.setText(SQLServer.sqlTableStock);
		sqlTableTreatment.setText(SQLServer.sqlTableTreatment);
		sqlTablePlant.setText(SQLServer.sqlTablePlant);
		
		updateSQL();
	}
	

/**
 * test the SQL parameters
 */
private void testSQL(){
	updateSQL();
	if(Util.testSQLConnection()){
		qrExperimentScroll.removeAll();
		setQRLists();
		qrExperimentScroll.revalidate();
		qrExperimentScroll.repaint();
	}
	
}


private void setQRLists(){
	getExperiments();
	getSetups();
	getStocks();
	getTreatments();

	String[] e = new String[qrExperimentID.size()]; for(int i = 0; i < qrExperimentID.size(); i++) e[i] = qrExperimentName.get(i);
	qrExpList = new JList(e);
	qrExpList.setLayoutOrientation(JList.VERTICAL);
	qrExpList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	qrExpList.setSelectedIndex(0);
	qrExpList.setVisibleRowCount(8);
	qrExpList.setFixedCellWidth(150);
	qrExperimentScroll = new JScrollPane(qrExpList);

	String[] s = new String[qrStockID.size()]; for(int i = 0; i < qrStockID.size(); i++) s[i] = qrStockName.get(i);
	qrStockList = new JList(s);
	qrStockList.setLayoutOrientation(JList.VERTICAL);
	qrStockList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	qrStockList.setSelectedIndex(0);
	qrStockList.setVisibleRowCount(8);
	qrStockScroll = new JScrollPane(qrStockList);

	String[] se = new String[qrSetupID.size()]; for(int i = 0; i < qrSetupID.size(); i++) se[i] = qrSetupName.get(i);
	qrSetupList = new JList(se);
	qrSetupList.setLayoutOrientation(JList.VERTICAL);
	qrSetupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	qrSetupList.setSelectedIndex(0);
	qrSetupList.setVisibleRowCount(8);
	qrSetupScroll = new JScrollPane(qrSetupList);

	String[] tr = new String[qrTreatmentID.size()]; for(int i = 0; i < qrTreatmentID.size(); i++) tr[i] = qrTreatmentName.get(i);
	qrTreatmentList = new JList(tr);
	qrTreatmentList.setLayoutOrientation(JList.VERTICAL);
	qrTreatmentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	qrTreatmentList.setSelectedIndex(0);
	qrTreatmentList.setVisibleRowCount(8);
	qrTreatmentScroll = new JScrollPane(qrTreatmentList);
}

private void screeningProcessing(){
	screeningRunButton.setEnabled(true);
	screeningProcessingButton.setEnabled(false);
	
	String[] filesNames = Util.getArrayFromString(screeningImages.getText(), "\n", false);
	File[] files = new File[filesNames.length];
	for(int i = 0; i < (filesNames.length); i++) files[i] = new File(filesNames[i]);
	
	sa = new ScreeningAnalysis(files, 
			screeningMeasureJCB.getSelectedIndex(), 
			Integer.valueOf(screeningMinSize.getText()), 
			Integer.valueOf(screeningIterations.getText()), 
			screeningMask.isSelected());	
	
	screeningResults = sa.process(true);

	
	// Generate the graph with the deviations
	XYSeries series1 = new XYSeries("");
	float[] sizes = sa.getOrderedResults();
	for(int i = 0; i < sizes.length; i++) series1.add(i, sizes[i]);
	XYSeriesCollection dataset1 = new XYSeriesCollection();
	dataset1.addSeries(series1);

	XYSeries line1 = new XYSeries("");
	line1.add(0,  screeningResults[0][1]);
	line1.add(screeningResults.length, screeningResults[0][1]);
	XYSeries line2 = new XYSeries("");
	line2.add(0,  screeningResults[0][2]);
	line2.add(screeningResults.length, screeningResults[0][2]);
	XYSeries line3 = new XYSeries("");
	line3.add(0,  screeningResults[0][3]);
	line3.add(screeningResults.length, screeningResults[0][3]);
	screeningLineSerie1 = new XYSeriesCollection();
	screeningLineSerie1.addSeries(line1);
	screeningLineSerie1.addSeries(line2);
	screeningLineSerie1.addSeries(line3);
	
	JFreeChart chart1 = ChartFactory.createScatterPlot(
			"Plant size", "plant", "value [px]", dataset1, PlotOrientation.VERTICAL, false, false, false
	);
	   
	XYPlot plot1 = (XYPlot) chart1.getPlot();
	XYItemRenderer scatterRenderer1 = plot1.getRenderer();
	StandardXYItemRenderer regressionRenderer1 = new StandardXYItemRenderer();
	regressionRenderer1.setBaseSeriesVisibleInLegend(false);
	plot1.setDataset(1, screeningLineSerie1);
	plot1.setRenderer(1, regressionRenderer1);
	Shape cross1 = ShapeUtilities.createDiagonalCross(1, 1);
	Stroke dashed =  new BasicStroke(1.0f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f, new float[] {2.0f}, 2.0f);
	scatterRenderer1.setSeriesShape(0, cross1);
	for (int i = 0; i < 2; i++) {
		scatterRenderer1.setSeriesPaint(i, Color.red);
		regressionRenderer1.setSeriesPaint(i, Color.green);
		regressionRenderer1.setSeriesStroke(i, dashed);
	}
	regressionRenderer1.setSeriesPaint(2, Color.green);
	chart1.setBackgroundPaint(null);			   
	screeningChartPanel1 = new ChartPanel(chart1);
	screeningChartPanel1.setPreferredSize(new Dimension(355, 250));
	

	
	// Generate the graph with the plant sizes
	XYSeries series2 = new XYSeries("XYGraph");
	for(int i = 0; i < screeningResults.length; i++) series2.add(screeningResults.length - i, screeningResults[i][0]);
	XYSeriesCollection dataset2 = new XYSeriesCollection();
	dataset2.addSeries(series2);
	
	XYSeries line4 = new XYSeries("");
	line4.add(screeningResults.length, 0);
	line4.add(screeningResults.length, screeningResults[0][0]);
	screeningLineSerie2 = new XYSeriesCollection();
	screeningLineSerie2.addSeries(line4);
	
	JFreeChart chart2 = ChartFactory.createScatterPlot(
			"Deviation", "number of plant", "standart deviation [%]", dataset2, PlotOrientation.VERTICAL, false, false, false
	);
	
	XYPlot plot2 = (XYPlot) chart2.getPlot();
	XYItemRenderer scatterRenderer2 = plot2.getRenderer();
	StandardXYItemRenderer regressionRenderer2 = new StandardXYItemRenderer();
	regressionRenderer2.setBaseSeriesVisibleInLegend(false);
	plot2.setDataset(1, screeningLineSerie2);
	plot2.setRenderer(1, regressionRenderer2);
	scatterRenderer2.setSeriesShape(0, cross1);
	for (int i = 0; i < dataset2.getSeriesCount(); i++) {
		scatterRenderer2.setSeriesPaint(i,Color.blue);
		regressionRenderer2.setSeriesPaint(i, Color.green);
	}
	chart2.setBackgroundPaint(null);			   
	screeningChartPanel2 = new ChartPanel(chart2);
	screeningChartPanel2.setPreferredSize(new Dimension(355, 250));
	
	
    
    screeningPlantSlider = new JSlider(JSlider.HORIZONTAL, 1, screeningResults.length, screeningResults.length);
    screeningPlantSlider.addChangeListener(this);
    screeningPlantSlider.setMajorTickSpacing(screeningResults.length/5);
    screeningPlantSlider.setMinorTickSpacing(1);
    screeningPlantSlider.setPaintLabels(true);  
    screeningPlantSlider.setSnapToTicks(true);
    screeningPlantSlider.setPaintTicks(true);
    
    screeningSTDev = new JLabel(""+(int)screeningResults[0][0]+"%");
    //screeningSTDev.setEditable(false);
    Font f3 = screeningSTDev.getFont();
    screeningSTDev.setFont(f3.deriveFont(f3.getStyle() ^ Font.BOLD)); 
    if(screeningResults[0][0] > 20) screeningSTDev.setForeground(Color.red); 
    else screeningSTDev.setForeground(dGreen); 
    
    screeningPlants = new JTextField(""+screeningResults.length, 3);
    
    JPanel subpanel = new JPanel(new BorderLayout());
    subpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    GridBagConstraints gbc1 = new GridBagConstraints();
    gbc1.anchor = GridBagConstraints.WEST;
    GridBagLayout gbl1 = new GridBagLayout();
    subpanel.setLayout(gbl1);
    
//    gbc1.gridx = 0;
//    gbc1.gridy = 0;
//    subpanel.add(new JLabel("Variance [%]: "), gbc1);
//    gbc1.gridx = 1;
//    subpanel.add(screeningSTDev, gbc1);

    gbc1.gridy = 0;
    gbc1.gridx = 0;
    subpanel.add(new JLabel("Plants to keep: "), gbc1);
    gbc1.gridx = 1;
    subpanel.add(screeningPlants, gbc1);
    gbc1.gridx = 2;
    subpanel.add(screeningPlantSlider, gbc1);
    gbc1.gridx = 3;
    subpanel.add(new JLabel("   "), gbc1);
    gbc1.gridx = 4;
    subpanel.add(new JLabel("Variance: "), gbc1);
    gbc1.gridx = 5;
    subpanel.add(screeningSTDev, gbc1);
    
    JPanel subpanel2= new JPanel(new BorderLayout());
    subpanel2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    subpanel2.add(screeningChartPanel1, BorderLayout.WEST);
    subpanel2.add(screeningChartPanel2, BorderLayout.EAST);
    
    screeningDynamicBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    screeningDynamicBox.add(subpanel, BorderLayout.NORTH);
    screeningDynamicBox.add(subpanel2, BorderLayout.SOUTH);
    
	tp.revalidate();
	tp.updateUI();
	validate();
	
}


}
