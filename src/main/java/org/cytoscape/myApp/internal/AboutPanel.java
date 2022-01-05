package org.cytoscape.myApp.internal;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Properties;
/**
 * NeDRex App
 * @author Judith Bernett
 */
public class AboutPanel extends JPanel {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private RepoApplication app;
    private JDialog aboutDialog;
    JPanel infoPanel;
    JPanel topPanel;
    JPanel leftPanel;
    JPanel centerPanel;
    JPanel rightPanel;
    String reposcapeVersion;
    String repoTrialDBVersion;
    URI uniprotURI;
    URI uniprotLicense;
    String uniprotDate;
    URI omimURI;
    URI omimLicense;
    String omimDate;
    URI iidURI;
    URI iidLicense;
    String iidDate;
    String iidVersion;
    URI reactomeURI;
    URI reactomeLicense;
    String reactomeDate;
    URI drugbankURI;
    URI drugbankLicense;
    String drugbankDate;
    URI disgenetURI;
    URI disgenetLicense;
    String disgenetDate;
    String disgenetVersion;
    URI drugCentralURI;
    URI drugCentralLicense;
    String drugCentralDate;
    String drugCentralVersion;
    URI mondoURI;
    String mondoDate;
    URI ncbiURI;
    URI ncbiLicense;
    String ncbiDate;
    URI tutorialURI;
    URI citationURI;
    URI nedrexURI;
//    URI interproURI;
//    URI interproLicense;
//    String interproDate;
//    String interproVersion;

    public AboutPanel(RepoApplication app){
        super();
        this.app = app;
        this.setBackground(Color.WHITE);
        infoPanel = new JPanel();
        infoPanel.setBackground(Color.WHITE);
        add(infoPanel);
        infoPanel.setLayout(new BorderLayout());
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        topPanel.setBackground(Color.WHITE);
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        leftPanel.setBackground(Color.WHITE);
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
        centerPanel.setBackground(Color.WHITE);
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
        rightPanel.setBackground(Color.WHITE);
        makeURIs();
    }

    private void makeURIs(){
        try {
            this.uniprotURI = new URI("https://www.uniprot.org/");
            this.uniprotLicense = new URI("https://www.uniprot.org/help/license");
            this.omimURI = new URI("https://omim.org/");
            this.omimLicense = new URI("https://www.omim.org/help/agreement");
            this.iidURI = new URI("https://www.accessdata.fda.gov/scripts/cder/iig/index.Cfm");
            this.iidLicense = new URI("https://www.fda.gov/media/128687/download");
            this.reactomeURI = new URI("https://reactome.org/");
            this.reactomeLicense = new URI("https://reactome.org/license");
            this.drugbankURI = new URI("https://go.drugbank.com/");
            this.drugbankLicense = new URI("https://go.drugbank.com/about");
            this.disgenetURI = new URI("https://www.disgenet.org/");
            this.disgenetLicense = new URI("https://www.disgenet.org/legal");
            this.drugCentralURI = new URI("https://drugcentral.org/");
            this.drugCentralLicense = new URI("https://drugcentral.org/privacy");
            this.mondoURI = new URI("https://www.ebi.ac.uk/ols/ontologies/mondo");
            this.ncbiURI = new URI("https://www.ncbi.nlm.nih.gov/");
            this.ncbiLicense = new URI("https://www.ncbi.nlm.nih.gov/home/about/policies/");
            this.tutorialURI = new URI(Constant.TUTORIAL_LINK);
            this.citationURI = new URI(Constant.CITATION_LINK);
            this.nedrexURI = new URI(Constant.NEDREX_LINK);
//            this.interproURI = new URI("https://www.ebi.ac.uk/interpro/");
//            this.interproLicense = new URI("https://www.ebi.ac.uk/interpro/about/interpro/");
        }catch (URISyntaxException us){
            logger.info("URI syntax exception");
        }
    }

    private void makePanel(){

        JLabel welcomeLabel = new JLabel("Welcome to NeDRexApp!");
        welcomeLabel.setFont(new Font("Helvetica", Font.PLAIN, 24));
        welcomeLabel.setForeground(new Color(20, 62, 130));
        welcomeLabel.setBorder(new EmptyBorder(10,170,10,170));
        topPanel.add(welcomeLabel, BorderLayout.PAGE_START);

//        String infoURL = "https://api.repotrial.net/static/metadata";
        String infoURL = Constant.API_LINK + "static/metadata";
        
        JSONObject jsonInformation;

        HttpGet request = new HttpGet(infoURL);
        HttpClient client = new DefaultHttpClient();

        try {
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
            String responseText = rd.readLine();

            JSONParser parser = new JSONParser();
            jsonInformation = (JSONObject) parser.parse(responseText);
            logger.info("The source databases of the response json object: " + jsonInformation.get("source_databases"));
            JSONObject source_databases = (JSONObject) jsonInformation.get("source_databases");

            updateInfo(jsonInformation, source_databases);
            
            JLabel repoScapeLabel = new JLabel("<html><b>NeDRexApp Version: </b>" + reposcapeVersion + "</html>");
            repoScapeLabel.setBorder(new EmptyBorder(10,10,10,10));
            repoScapeLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
            topPanel.add(repoScapeLabel);
            //addLabel(repoScapeLabel, infoPanel);
            JLabel description = new JLabel(
                    "<html>" +
                            "The NeDRexApp is one of the main components of <font color = \"#143e82\">NeDRex</font>, a network medicine platform for <font color = \"#143e82\">disease<br> " +
                            "module identification</font> and <font color = \"#143e82\">drug repurposing</font>. NeDRexApp makes all the <font color = \"#143e82\">network algorithms of NeDRex</font><br>" +
                            "available in Cytoscape. The algorithms are applied to the constructed heterogeneous networks in <br>" +
                            "the backend.<br><br>"
                            + "NeDRexApp also enables querying the <font color = \"#143e82\">NeDRexDB</font> knowledgebase via API. Users can send requests to <br>"
                            + "build custom heterogeneous networks and load them in Cytoscape via: <br>"
                            + "File --> Import --> Network from Public Databases... --> NeDRex: network query from NeDRexDB <br><br>"
                            + "<b>NeDRexDB</b> contains information from the <b>Online Mendelian Inheritance in Man® (OMIM®)</b> database,<br>"
                            + "which has been obtained under a license from the Johns Hopkins University.  NeDRexDB does not <br>"
                            + "represent the entire, unmodified OMIM® database, which is available in its entirety at <br>"
                            + "https://omim.org/downloads." +
                            "</html>");
            description.setBorder(new EmptyBorder(0,10,0,10));
            description.setFont(new Font("Helvetica", Font.PLAIN, 14));
            topPanel.add(description);
            
            JLabel nedrexLabel = new JLabel("<html>For more information about the platform and <b>Terms of Use</b> please visit:</html>");
            nedrexLabel.setBorder(new EmptyBorder(10,10,1,10));
            nedrexLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
            topPanel.add(nedrexLabel); 
            addButton("https://nedrex.net", nedrexURI, topPanel);
            
            JLabel tutorialLabel = new JLabel("<html> You can find the detailed documentation and tutorial for <b>NeDRexApp</b> together with example use cases at </html>");
            tutorialLabel.setBorder(new EmptyBorder(10,10,1,10));
            tutorialLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
            topPanel.add(tutorialLabel);            
            addButton("https://nedrex.net/tutorial", tutorialURI, topPanel);
            
            JLabel citationLabel = new JLabel("<html> <b>Citation:</b> If you use NeDRex app in your research, please cite our paper: </html>");
            citationLabel.setBorder(new EmptyBorder(10,10,1,10));
            citationLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
            topPanel.add(citationLabel);            
            addButton("https://www.nature.com/articles/s41467-021-27138-2", citationURI, topPanel);
            
            JLabel databaseLabel = new JLabel("<html> The current version of the <b>NeDRexDB</b> (" + repoTrialDBVersion+") uses " + "the following versions of databases: </html>");
            databaseLabel.setBorder(new EmptyBorder(10,10,1,10));
            databaseLabel.setFont(new Font("Helvetica", Font.PLAIN, 14));
            topPanel.add(databaseLabel);
                       
            infoPanel.add(topPanel, BorderLayout.PAGE_START);

            addButton("Uniprot", uniprotURI, leftPanel);
            databaseLabel = new JLabel("<html><b>Uniprot date: </b>" + uniprotDate + "</html>");
            addLabel(databaseLabel, centerPanel);
            addButton("License", uniprotLicense, rightPanel);

            addButton("OMIM", omimURI, leftPanel);
            databaseLabel = new JLabel("<html><b>OMIM date: </b>" + omimDate + "</html>");
            addLabel(databaseLabel, centerPanel);
            addButton("License", omimLicense, rightPanel);

            addButton("IID", iidURI, leftPanel);
            databaseLabel = new JLabel("<html><b>IID date: </b>" + iidDate + "<b>, Version: </b>" + iidVersion + "</html>");
            addLabel(databaseLabel, centerPanel);
            addButton("License", iidLicense, rightPanel);

            addButton("Reactome", reactomeURI, leftPanel);
            databaseLabel = new JLabel("<html><b>Reactome date: </b>" + reactomeDate + "</html>");
            addLabel(databaseLabel, centerPanel);
            addButton("License", reactomeLicense, rightPanel);

            addButton("Drugbank", drugbankURI, leftPanel);
            databaseLabel = new JLabel("<html><b>Drugbank date: </b>" + drugbankDate + "</html>");
            addLabel(databaseLabel, centerPanel);
            addButton("License", drugbankLicense, rightPanel);

            addButton("DisGeNet", disgenetURI, leftPanel);
            databaseLabel = new JLabel("<html><b>DisGeNet date: </b>" + disgenetDate + "<b>, Version: </b>" + disgenetVersion + "</html>");
            addLabel(databaseLabel, centerPanel);
            addButton("License", disgenetLicense, rightPanel);

            addButton("DrugCentral", drugCentralURI, leftPanel);
            databaseLabel = new JLabel("<html><b>DrugCentral Date: </b>" + drugCentralDate + "<b>, Version: </b>" + drugCentralVersion + "</html>");
            addLabel(databaseLabel, centerPanel);
            addButton("License", drugCentralLicense, rightPanel);

            addButton("Mondo", mondoURI, leftPanel);
            databaseLabel = new JLabel("<html><b>Mondo Date: </b>" + mondoDate + "</html>");
            addLabel(databaseLabel, centerPanel);
            addButton("License", mondoURI, rightPanel);

            addButton("NCBI", ncbiURI, leftPanel);
            databaseLabel = new JLabel("<html><b>NCBI Date: </b>" + ncbiDate + "</html>");
            addLabel(databaseLabel, centerPanel);
            addButton("License", ncbiLicense, rightPanel);

//            addButton("Interpro", interproURI, leftPanel);
//            databaseLabel = new JLabel("<html><b>Interpro date: </b>" + interproDate + "<b>, Version: </b>" + interproVersion + "</html>");
//            addLabel(databaseLabel, centerPanel);
//            addButton("License", interproLicense, rightPanel);

            infoPanel.add(leftPanel, BorderLayout.LINE_START);
            infoPanel.add(centerPanel, BorderLayout.CENTER);
            infoPanel.add(rightPanel ,BorderLayout.LINE_END);

        } catch (IOException | ParseException e1) {
            e1.printStackTrace();
        }
    }

    private void updateInfo(JSONObject jsonInformation, JSONObject source_databases){
        this.repoTrialDBVersion = (String) jsonInformation.get("version");
        Properties props = new Properties();
        try {
            props.load(Objects.requireNonNull(AboutPanel.class.getClassLoader().getResourceAsStream("project.properties")));
            this.reposcapeVersion = props.getProperty("version");
        }catch (IOException io){
            logger.info("IO Exception for pom file");
        }
        this.uniprotDate = getDate("uniprot", source_databases);
        this.omimDate = getDate("omim", source_databases);
        this.iidDate = getDate("iid", source_databases);
        this.iidVersion = getVersion("iid", source_databases);
        this.reactomeDate = getDate("reactome", source_databases);
        this.drugbankDate = getDate("drugbank", source_databases);
        this.disgenetDate = getDate("disgenet", source_databases);
        this.disgenetVersion = getVersion("disgenet", source_databases);
        this.drugCentralDate = getDate("drug_central", source_databases);
        this.drugCentralVersion = getVersion("drug_central", source_databases);
        this.mondoDate = getDate("mondo", source_databases);
        this.ncbiDate = getDate("ncbi", source_databases);
//        this.interproDate = getDate("interpro", source_databases);
//        this.interproVersion = getVersion("interpro", source_databases);

    }

    private String getDate(String name, JSONObject jsonObject){
        return (String) ((JSONObject) jsonObject.get(name)).get("date");
    }

    private String getVersion(String name, JSONObject jsonObject){
        return (String) ((JSONObject) jsonObject.get(name)).get("version");
    }

    private void addLabel(JLabel label,JPanel panel){
        label.setBorder(new EmptyBorder(10,10,10,10));
        label.setFont(new Font("Helvetica", Font.PLAIN, 14));
        panel.add(label);
    }

    private void setButtonProperties(JButton button, String text, URI uri){
        button.setText("<HTML> <FONT color=\"#000099\" style=\"font-family:Helvetica;font-size:14pt\"><U>" + text + "</U></FONT> </HTML>");
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setBackground(Color.WHITE);
        button.setToolTipText(uri.toString());
    }

    private void addButton(String text, URI uri, JPanel panel){
        JButton button = new JButton();
        setButtonProperties(button, text, uri);
        button.addActionListener(new OpenInDBActionListener(uri));
        button.setBorder(new EmptyBorder(10,10,10,10));
        button.setHorizontalAlignment(JButton.LEFT);
        panel.add(button);
    }

    public void activate() {
        makePanel();
        this.app.getActivator().registerService(this, JPanel.class);
        this.setVisible(true);
        aboutDialog = this.app.getAboutDialog();
        aboutDialog.getContentPane().add(this);
        aboutDialog.pack();
        aboutDialog.setVisible(true);
    }

    public void deactivate() {
        this.infoPanel.removeAll();
        this.topPanel.removeAll();
        this.leftPanel.removeAll();
        this.rightPanel.removeAll();
        this.centerPanel.removeAll();
        this.app.getActivator().unregisterAllServices(this);
        aboutDialog.setVisible(false);
    }

     static class OpenInDBActionListener implements ActionListener {
        URI uri;

        public OpenInDBActionListener(URI uri){
            this.uri = uri;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(this.uri);
                } catch (IOException ie) {
                    System.err.println("URI not valid");
                }
            } else {
                System.err.println("Desktop not supported");
            }
        }
    }
}
