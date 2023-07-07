package org.cytoscape.nedrex.internal;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @modified by: Andreas Maier
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
	}

    private NeDRexService nedrexService;
    @Reference
    public void setNedrexService(NeDRexService nedrexService) {
        this.nedrexService = nedrexService;
    }

    public void unsetNedrexService(NeDRexService nedrexService) {
        if (this.nedrexService == nedrexService)
            this.nedrexService = null;
    }


    private void makePanel() throws IOException{

        String licenceURL = this.nedrexService.API_LINK + "static/licence";

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
