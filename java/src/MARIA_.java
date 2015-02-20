/**
* @author Guillaume Lobet | Universit� de Li�ge
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


public class MARIA_ extends PlugInFrame{
	
	static MARIA_ instance, fop;
	public static Preferences prefs;


	private static final long serialVersionUID = -2516812747038073446L;

	public MARIA_() {
		super("Friend of phenotyping");
		
		new MARIAInterface();
	}
	
	public static void main(String args[]) {
		
	    prefs = Preferences.userRoot().node("/ImageJ/plugins");
	    
		ImageJ ij = new ImageJ();
		fop = new MARIA_();
		ij.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				fop.dispose();
				System.exit(0);
			}
		});
	}
}
