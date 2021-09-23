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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import org.cytoscape.myApp.internal.LoadNetworkTask;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.myApp.internal.menuactions.AboutAction;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class GetNetworkPanel extends JPanel{
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	JTextArea searchTerms;
	JPanel mainSearchPanel;
	SearchOptionPanel optionsPanel;
	JButton importButton;
	AboutPanel aboutPanel;
	
	public GetNetworkPanel(RepoApplication app) {
		super(new GridBagLayout());
		this.app = app;
		this.aboutPanel = this.app.getAboutPanel();
		init();
	}
	
	private void init() {
		// Create the surrounding panel
		setPreferredSize(new Dimension(800,400)); // use 800, 600 after adding Enter ID panel
		EasyGBC c = new EasyGBC();

		optionsPanel = new SearchOptionPanel(app);
		optionsPanel.setMinimumSize(new Dimension(400, 250));
		add(optionsPanel, c.down().expandBoth().insets(25,5,0,5));

		// Add Query/Cancel buttons
		JPanel buttonPanel =  createControlButtons();
		add(buttonPanel, c.down().expandHoriz().insets(0,5,5,5));
	}
	
	JPanel createSearchPanel() {
		JPanel searchPanel = new JPanel(new GridBagLayout());
		searchPanel.setPreferredSize(new Dimension(600,400));
		EasyGBC c = new EasyGBC();

		String label = "Enter names or IDs:";
		JLabel searchLabel = new JLabel(label);
		searchLabel.setToolTipText("Enter one name or identifier per line!");
		c.noExpand().anchor("northwest").insets(0,5,0,5);
		searchPanel.add(searchLabel, c);
		searchTerms = new JTextArea();
		JScrollPane jsp = new JScrollPane(searchTerms);
		c.down().expandBoth().insets(5,10,5,10);
		searchPanel.add(jsp, c);
		return searchPanel;
	}
	
	JPanel createControlButtons() {
		JPanel buttonPanel = new JPanel();
		BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
		buttonPanel.setLayout(layout);
		JButton infoButton = new JButton("Info");
		infoButton.addActionListener(new AboutAction(this.app, this.aboutPanel));
		buttonPanel.add(infoButton);

		//importButton = new JButton(new InitialAction());
		importButton = new JButton(new ImportAction(app, optionsPanel));

		buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
		//buttonPanel.add(cancelButton);
		buttonPanel.add(Box.createHorizontalGlue());
		// buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
		//buttonPanel.add(backButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
		buttonPanel.add(importButton);
		return buttonPanel;
	}
	
	// this function is not used now
	class InitialAction extends AbstractAction {
		public InitialAction() {
			super("Import");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			JSONObject payload = new JSONObject();
			// empty nodes overwrite the default node selection of : gene, protein, disorder, drug 
			// >> if single nodes without any edges are not desired in the network, empty node list should be used to overwrite the default
			List<String> nodes = new ArrayList<String>();
			List<String> edges = new ArrayList<String>();
			edges = optionsPanel.getSelectedEdgeTypes();;
			List<String> iidEvids = optionsPanel.getIIDevidence();
			List<String> drugGroups = optionsPanel.getSelectedDrugGroups();
			Boolean ppiSL = optionsPanel.getSelfLoop();
			List<Integer> taxIDs = new ArrayList<Integer>();
			taxIDs.add(9606);
			if (optionsPanel.allTaxIDSelected()) {
				taxIDs.add(-1);
			}
			System.out.println("This is the selected threshold: " + optionsPanel.getThreshold());
			String networkName = optionsPanel.getNetworkName();
			logger.info("The entered name of the new network by user: " + networkName);
			//System.out.println("The entered name of the new network by user: " + networkName);
			
			
			Boolean concise = false;
			payload.put("nodes", nodes);
			payload.put("edges", edges);
			payload.put("iid_evidence", iidEvids);
			payload.put("ppi_self_loops", ppiSL);
			payload.put("taxid", taxIDs);
			payload.put("concise", concise);
			payload.put("disgenet_threshold", optionsPanel.getThreshold());
			payload.put("include_omim", optionsPanel.includeOMIM());
			payload.put("drug_groups", drugGroups);
			
			logger.info("The post JSON converted to string: " + payload.toString());


			/*CloseableHttpClient client = HttpClients.createDefault();			
			CloseableHttpResponse response = null;*/
			HttpPost post = new HttpPost(Constant.API_LINK+"graph_builder");
			
			//HttpClient client = HttpClientBuilder.create().build();
			HttpClient client = new DefaultHttpClient();
			
			//post.setEntity(new StringEntity(strEntity, ContentType.APPLICATION_FORM_URLENCODED));
			post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
			String uidd = new String();
			logger.info("The post: "+ post.toString());
			
			try {
				HttpResponse response = client.execute(post);
				HttpEntity entity = response.getEntity();
				logger.info("The response entity is: " + entity);
				BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));
				String line = "";
				logger.info("Response entity: ");
				while ((line = rd.readLine()) != null) {
					System.out.println(line);
					logger.info("The uri of the response to the post: "+line + "\n");
					uidd = line;
				  }
				EntityUtils.consume(entity);
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} /*finally {
				try {
					response.close();
				} catch (Exception e3) {
					e3.printStackTrace();
				}
			}*/
			
			//InputStream entityStream = entity1.getContent();
			
			//BufferedReader reader = new BufferedReader(new InputStreamReader(entityStream));
			//HttpResponse response = client.execute(post);
			
			//// now GET
			String uid = uidd.replace("\"", "");
			HttpGet request = new HttpGet(Constant.API_LINK+"graph_details/"+uid);
			try {
				HttpResponse response = client.execute(request);
				boolean Success = false;
				boolean Failed = false;
				  
				  // we're letting it build for t*10 seconds
				for (int t=0; t<60; t++) {
					BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
					String line = "";
					while ((line = rd.readLine()) != null) {
						System.out.println(line);
						logger.info(line);
						if (line.contains("completed"))
							Success=true;
						if (line.contains("failed")) {
							Failed=true;
						}
					}
					if(Success) {
						System.out.println("Built was success!!!");
						logger.info("Built was successful!");
						//String urlp = "http://repotrial.bioswarm.net:29492/v2/graph_download/"+uid+".graphml";
						//String urlp = "http://repotrial.bioswarm.net:29492/v2/graph_download_v2/"+uid+"/"+networkName+".graphml";
						String urlp = "";
						if (!networkName.equals("")) {
//							urlp = "https://api.repotrial.net/graph_download_v2/"+uid+"/"+networkName+".graphml";
							urlp = Constant.API_LINK+"graph_download_v2/"+uid+"/"+networkName+".graphml";
						}
						else {
//							urlp = "https://api.repotrial.net/graph_download/"+uid+".graphml";
							urlp = Constant.API_LINK+"graph_download/"+uid+".graphml";
						}
						//String urlp = "https://api.repotrial.net/graph_download_v2/"+uid+"/"+networkName+".graphml";
						DialogTaskManager taskmanager = app.getActivator().getService(DialogTaskManager.class);
						taskmanager.execute(new TaskIterator(new LoadNetworkTask(app,urlp)));
						break;
					}
					if (Failed) {
						logger.info("The build is failed!");
						break;
					}
					response = client.execute(request);
					try {
						logger.info("Waiting for build to complete, sleeping for 10 seconds...");
						Thread.sleep(10000);
					} catch (InterruptedException e0) {
						// TODO Auto-generated catch block
						e0.printStackTrace();
					}

				} 
				  
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
		}

	}

}
