package org.cytoscape.nedrex.internal.ui;

import org.cytoscape.nedrex.internal.AboutPanel;
import org.cytoscape.nedrex.internal.LicensePanel;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.nedrex.internal.menuactions.AboutAction;
import org.cytoscape.nedrex.internal.menuactions.LicenseAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @author Andreas Maier
 */
public class GetComorbiditomePanel extends JPanel{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	JTextArea searchTerms;
	JPanel mainSearchPanel;
	ComorbOptionPanel optionsPanel;
	JButton importButton;
	AboutPanel aboutPanel;
	LicensePanel licensePanel;
	
	public GetComorbiditomePanel(RepoApplication app) {
		super(new GridBagLayout());
		this.app = app;
		this.aboutPanel = this.app.getAboutPanel();
		this.licensePanel = this.app.getLicensePanel();
		init();
	}
	
	private void init() {
		// Create the surrounding panel
		setPreferredSize(new Dimension(850,400));
		EasyGBC c = new EasyGBC();

		optionsPanel = new ComorbOptionPanel(app);
		optionsPanel.setMinimumSize(new Dimension(400, 250));
		add(optionsPanel, c.down().expandBoth().insets(25,5,0,5));

		// Add Query/Cancel buttons
		JPanel buttonPanel =  createControlButtons();
		add(buttonPanel, c.down().expandHoriz().insets(0,5,5,5));
	}
	
	JPanel createControlButtons() {
		JPanel buttonPanel = new JPanel();
		BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
		buttonPanel.setLayout(layout);
		
		JButton infoButton = new JButton("Info");
		infoButton.addActionListener(new AboutAction(this.app, this.aboutPanel));
		buttonPanel.add(infoButton);
		
		JButton touButton = new JButton("Terms of Use");
		touButton.addActionListener(new LicenseAction(this.app, this.licensePanel));
		buttonPanel.add(touButton);

		//importButton = new JButton(new InitialAction());
		importButton = new JButton(new ImportComorbidityAction(app, optionsPanel));

		buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
		//buttonPanel.add(cancelButton);
		buttonPanel.add(Box.createHorizontalGlue());
		// buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
		//buttonPanel.add(backButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
		buttonPanel.add(importButton);
		return buttonPanel;
	}
	

}
