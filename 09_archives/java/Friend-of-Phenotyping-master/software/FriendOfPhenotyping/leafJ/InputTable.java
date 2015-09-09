package leafJ;
//Class to hold and display user customizable values for sample description
//Julin Maloof
//April 6, 2012



import ij.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class InputTable {
	String[] columnNames = {
			"genotype",
			"treatment",
			"replicate",
			"measured by"
			};
	
	Object[][] data = {
			{"col","ler"},
			{"sun","shade"},
			{"1","2","3","4","5"}
	};
	
	JTable table = new JTable(data,columnNames);		
	
	JScrollPane scrollPane = new JScrollPane(table);
	
	
}