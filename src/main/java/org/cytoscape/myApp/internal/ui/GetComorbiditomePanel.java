package org.cytoscape.myApp.internal.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.cytoscape.myApp.internal.AboutPanel;
import org.cytoscape.myApp.internal.Constant;
import org.cytoscape.myApp.internal.ImportAction;
import org.cytoscape.myApp.internal.LicensePanel;
import org.cytoscape.myApp.internal.LoadNetworkTask;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.menuactions.AboutAction;
import org.cytoscape.myApp.internal.menuactions.LicenseAction;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
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
