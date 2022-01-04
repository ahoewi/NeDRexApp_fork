package org.cytoscape.myApp.internal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class LicensePanel extends JPanel{
	private RepoApplication app;
    private JDialog licenseDialog;
    private Logger logger = LoggerFactory.getLogger(getClass());
    JPanel infoPanel;
    JTextArea textArea;
    JFrame licenseFrame;
    private JScrollPane scroll;
	
	public LicensePanel (RepoApplication app) {
		super();
        this.app = app;
        this.setBackground(Color.WHITE);
        infoPanel = new JPanel();
        infoPanel.setBackground(Color.WHITE);
        add(infoPanel);
        infoPanel.setLayout(new BorderLayout()); 
//        topPanel = new JPanel();
//        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
//        topPanel.setBackground(Color.WHITE);
//        leftPanel = new JPanel();
//        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
//        leftPanel.setBackground(Color.WHITE);
//        centerPanel = new JPanel();
//        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
//        centerPanel.setBackground(Color.WHITE);
//        rightPanel = new JPanel();
//        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
//        rightPanel.setBackground(Color.WHITE);
//        makeURIs();
	}
	
    private void makePanel() throws IOException{

        /*JLabel agreementLabel = new JLabel("License Agreement!");
        agreementLabel.setFont(new Font("Helvetica", Font.PLAIN, 24));
        agreementLabel.setForeground(new Color(20, 62, 130));
        agreementLabel.setBorder(new EmptyBorder(10,170,10,170));
        infoPanel.add(agreementLabel);*/

        String licenceURL = Constant.API_LINK + "static/licence";

        HttpGet request = new HttpGet(licenceURL);
        HttpClient client = new DefaultHttpClient();
        
        String licenseText = "";
        try {
			HttpResponse response = client.execute(request);
			String  responseText = "";
			BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				responseText = line;
				licenseText = licenseText + "\n" + responseText;
//				licenseText = licenseText + responseText;
				}			
											  
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        
        licenseText = licenseText + "\n\n";
        logger.info("The license text: " + licenseText);      
        
        licenseFrame = new JFrame("License Agreement");
        textArea = new JTextArea(licenseText, 70, 80);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        licenseFrame.add(scroll);
        licenseFrame.pack();
        licenseFrame.setVisible(true);
        infoPanel.add(licenseFrame);
       
        		
    }
    
    public void activate() throws IOException {
        makePanel();
        this.app.getActivator().registerService(this, JPanel.class);
        this.setVisible(true);
        licenseDialog = this.app.getLicenseDialog();
        licenseDialog.getContentPane().add(this);
        licenseDialog.pack();
        licenseDialog.setVisible(true);
    }

    public void deactivate() {
        this.infoPanel.removeAll();
        this.app.getActivator().unregisterAllServices(this);
        licenseDialog.setVisible(false);
    }

}
