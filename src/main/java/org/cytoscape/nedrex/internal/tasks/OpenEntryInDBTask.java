package org.cytoscape.nedrex.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * NeDRex App
 * @author Judith Bernett
 */

public class OpenEntryInDBTask extends AbstractTask {

    private URI uri = null;
    private String databaseID;
    private String database;
    private String type;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public OpenEntryInDBTask(String uriDrugBank, String databaseID, String database) {
        this.databaseID = databaseID;
        this.database = database;
        this.type = "";
        switch (database){
            case "DrugBank":
                this.type = "Drug";
                break;
            case "Uniprot":
                this.type = "Protein";
                break;
            case "Monarch Initiative Explorer":
                this.type = "Disease";
                break;
            case "NCBI":
                this.type = "Gene";
                break;
            case "Reactome":
                this.type = "Pathway";
                break;
        }
        String all = uriDrugBank + databaseID;
        try {
            this.uri = new URI(all);
        }catch (URISyntaxException uriException){
            logger.info("URI Syntax Exception");
        }
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        JFrame frame = new JFrame("Link to Database");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setSize(450,200);
        frame.setLocationRelativeTo(null);
        Container container = frame.getContentPane();
        container.setLayout(new FlowLayout(FlowLayout.LEFT));
        if(!database.equals("") & uri != null) {
            JLabel label = new JLabel("View this " + type + " in " + database +": ");
            container.add(label);
            JButton button = new JButton();
            setButtonProperties(button);
            button.addActionListener(new OpenInDBActionListener(frame, uri));
            container.add(button);
            if(database.equals("Monarch Initiative Explorer")){
                JLabel label2 = new JLabel("View this " + type + " in OLS by EMBL-EBI: ");
                container.add(label2);
                JButton button2 = new JButton();
                setButtonProperties(button2);
                String secondLink = "https://www.ebi.ac.uk/ols/search?q=" + databaseID + "&ontology=mondo";
                URI uri2 = new URI(secondLink);
                button2.addActionListener(new OpenInDBActionListener(frame, uri2));
                container.add(button2);

            }
        }else {
            JLabel label = new JLabel("Please choose a node with either a DrugBank ID, a Uniprot ID, a Mondo ID, an Entrez ID or a Reactome ID for this task");
            container.add(label);
        }
        frame.setVisible(true);

    }

    private void setButtonProperties(JButton button){
        button.setText("<HTML> <FONT color=\"#000099\"><U>" + databaseID + "</U></FONT> </HTML>");
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setBackground(Color.WHITE);
        button.setToolTipText(uri.toString());
    }

    static class OpenInDBActionListener implements ActionListener{
        JFrame frame;
        URI uri;

        public OpenInDBActionListener(JFrame frame, URI uri){
            this.frame = frame;
            this.uri = uri;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(this.uri);
                    this.frame.dispose();
                } catch (IOException ie) {
                    System.err.println("URI not valid");
                }
            } else {
                System.err.println("Desktop not supported");
            }
        }
    }

}
