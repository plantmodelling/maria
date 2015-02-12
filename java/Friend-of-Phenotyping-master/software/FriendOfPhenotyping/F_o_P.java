/**
* @author Guillaume Lobet | Université de Liège
* @date: 2013-02-15
* 
* Plugin containing usefull function for the analysis of Arabidopsis rosette size
* 
**/

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import ij.ImageJ;
import ij.plugin.frame.PlugInFrame;


public class F_o_P extends PlugInFrame{
	
	static F_o_P instance, fop;
	public static Preferences prefs;


	private static final long serialVersionUID = -2516812747038073446L;

	public F_o_P() {
		super("Friend of phenotyping");
		
		new FoPInterface();
	}
	
	public static void main(String args[]) {
		
	    prefs = Preferences.userRoot().node("/ImageJ/plugins");
	    SQLServer.initialize();
	    
		ImageJ ij = new ImageJ();
		fop = new F_o_P();
		ij.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				fop.dispose();
				System.exit(0);
			}
		});
	}
}
