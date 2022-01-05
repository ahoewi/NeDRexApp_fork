package org.cytoscape.myApp.internal.utils;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class WarningMessages {
	
	public static void showAgreementWarning() {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(null, "If you want to use the NeDRex app, you need to first agree with our terms of use. The NeDRex Terms of Use are available at: https://api.nedrex.net/static/licence", "License Agreement", JOptionPane.WARNING_MESSAGE);
				}
			}
		);
	}

}
