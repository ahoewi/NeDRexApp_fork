package org.cytoscape.myApp.internal;

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
public class InfoBox {

    RepoApplication app;
    Object[] options;
    String message;
    String title;
    boolean hide;
    JCheckBox checkbox;
    JCheckBox licensebox;
    JButton buttonPaper;
    JButton buttonTutorial;
    JButton buttonLicense;
    String linkURI;
    String tutorialURI;
    private String licenseURI = Constant.API_LINK + "static/licence";
    private Logger logger = LoggerFactory.getLogger(getClass());

    public InfoBox(RepoApplication app, String message){
        this.app = app;
        this.message = message;
        this.title = "Information";
        this.checkbox = new JCheckBox("Do not show this message again for the rest of this session");
        this.options = new Object[]{"Continue", "Cancel"};
        this.hide = false;
    }
    
    public InfoBox(RepoApplication app, String message, String tutorialURI){
        this.app = app;
        this.tutorialURI = tutorialURI;
        this.buttonTutorial = new JButton();
        buttonTutorial.setText("<HTML> For more info visit <FONT color=\"#000099\"><U> our tutorial</U></FONT>.<br><br><br></HTML>");
        buttonTutorial.setHorizontalAlignment(SwingConstants.LEFT);
        buttonTutorial.setBorderPainted(false);
        buttonTutorial.setOpaque(false);
        buttonTutorial.setBackground(Color.WHITE);
        try {
        	buttonTutorial.addActionListener(new OpenInWeb(new URI(tutorialURI)));
        } catch (URISyntaxException e) {
            logger.info("button action listener failed");
        }
        this.message = message;
        this.title = "Information";
        this.checkbox = new JCheckBox("Do not show this message again for the rest of this session");
        this.options = new Object[]{"Continue", "Cancel"};
        this.hide = false;
    }
    
    public InfoBox(RepoApplication app, String message, String tutorialURI, Boolean drug_involved){
        this.app = app;
        this.tutorialURI = tutorialURI;
        this.buttonTutorial = new JButton();
        buttonTutorial.setText("<HTML> For more info visit <FONT color=\"#000099\"><U> our tutorial</U></FONT>.<br></HTML>");
        buttonTutorial.setHorizontalAlignment(SwingConstants.LEFT);
        buttonTutorial.setBorderPainted(false);
        buttonTutorial.setOpaque(false);
        buttonTutorial.setBackground(Color.WHITE);
        try {
        	buttonTutorial.addActionListener(new OpenInWeb(new URI(tutorialURI)));
        } catch (URISyntaxException e) {
            logger.info("button action listener failed");
        }
        this.buttonLicense = new JButton();
        buttonLicense.setText("<HTML> Using NeDRex is subject to agreeing with terms of use described in the <FONT color=\"#000099\"><U>NeDRex End User License Agreement</U></FONT>.</HTML>");
        buttonLicense.setHorizontalAlignment(SwingConstants.LEFT);
        buttonLicense.setBorderPainted(false);
        buttonLicense.setOpaque(false);
        buttonLicense.setBackground(Color.WHITE);
        try {
        	buttonLicense.addActionListener(new OpenInWeb(new URI(licenseURI)));
        } catch (URISyntaxException e) {
            logger.info("button action listener failed");
        }
        this.message = message;
        this.title = "Information";
        this.checkbox = new JCheckBox("Do not show this message again for the rest of this session");
        this.licensebox = new JCheckBox("I agree with the NeDRex Terms of Use available at: https://api.nedrex.net/static/licence");
        this.options = new Object[]{"Continue", "Cancel"};
        this.hide = false;
    }

    /*public InfoBox(RepoApplication app, String message, String linkMessage, String linkURI){
        this.app = app;
        this.linkURI = linkURI;
        this.buttonPaper = new JButton();
        buttonPaper.setText("<HTML> This algorithm was developed by <FONT color=\"#000099\"><U>" + linkMessage + "</U></FONT> <br><br><br></HTML>");
        buttonPaper.setHorizontalAlignment(SwingConstants.LEFT);
        buttonPaper.setBorderPainted(false);
        buttonPaper.setOpaque(false);
        buttonPaper.setBackground(Color.WHITE);
        try {
        	buttonPaper.addActionListener(new OpenInWeb(new URI(linkURI)));
        } catch (URISyntaxException e) {
            logger.info("button action listener failed");
        }
        this.message = message;
        this.title = "Information";
        this.checkbox = new JCheckBox("Do not show this message again for the rest of this session");
        this.options = new Object[]{"Continue", "Cancel"};
        this.hide = false;
    }*/
    
    public InfoBox(RepoApplication app, String message, String linkMessage, String linkURI, String tutorialURI){
        this.app = app;
        this.linkURI = linkURI;
        this.buttonPaper = new JButton();
        this.buttonTutorial = new JButton();
        buttonPaper.setText("<HTML> This algorithm was developed by <FONT color=\"#000099\"><U>" + linkMessage + "</U></FONT> <br></HTML>");
        buttonPaper.setHorizontalAlignment(SwingConstants.LEFT);
        buttonPaper.setBorderPainted(false);
        buttonPaper.setOpaque(false);
        buttonPaper.setBackground(Color.WHITE);
        try {
        	buttonPaper.addActionListener(new OpenInWeb(new URI(linkURI)));
        } catch (URISyntaxException e) {
            logger.info("button action listener failed");
        }
        buttonTutorial.setText("<HTML> For more info visit <FONT color=\"#000099\"><U> our tutorial</U></FONT>.<br><br><br></HTML>");
        buttonTutorial.setHorizontalAlignment(SwingConstants.LEFT);
        buttonTutorial.setBorderPainted(false);
        buttonTutorial.setOpaque(false);
        buttonTutorial.setBackground(Color.WHITE);
        try {
        	buttonTutorial.addActionListener(new OpenInWeb(new URI(tutorialURI)));
        } catch (URISyntaxException e) {
            logger.info("button action listener failed");
        }
        this.message = message;
        this.title = "Information";
        this.checkbox = new JCheckBox("Do not show this message again for the rest of this session");
        this.options = new Object[]{"Continue", "Cancel"};
        this.hide = false;
    }
    
    public InfoBox(RepoApplication app, String message, String linkMessage, String linkURI, String tutorialURI, Boolean drug_involved){
        this.app = app;
        this.linkURI = linkURI;
        this.buttonPaper = new JButton();
        this.buttonTutorial = new JButton();
        buttonPaper.setText("<HTML> This algorithm was developed by <FONT color=\"#000099\"><U>" + linkMessage + "</U></FONT> <br></HTML>");
        buttonPaper.setHorizontalAlignment(SwingConstants.LEFT);
        buttonPaper.setBorderPainted(false);
        buttonPaper.setOpaque(false);
        buttonPaper.setBackground(Color.WHITE);
        try {
        	buttonPaper.addActionListener(new OpenInWeb(new URI(linkURI)));
        } catch (URISyntaxException e) {
            logger.info("button action listener failed");
        }
        buttonTutorial.setText("<HTML> For more info visit <FONT color=\"#000099\"><U> our tutorial</U></FONT>.<br></HTML>");
        buttonTutorial.setHorizontalAlignment(SwingConstants.LEFT);
        buttonTutorial.setBorderPainted(false);
        buttonTutorial.setOpaque(false);
        buttonTutorial.setBackground(Color.WHITE);
        try {
        	buttonTutorial.addActionListener(new OpenInWeb(new URI(tutorialURI)));
        } catch (URISyntaxException e) {
            logger.info("button action listener failed");
        }
        this.buttonLicense = new JButton();
        buttonLicense.setText("<HTML> Using NeDRex is subject to agreeing with terms of use described in the <FONT color=\"#000099\"><U>NeDRex End User License Agreement</U></FONT>.</HTML>");
        buttonLicense.setHorizontalAlignment(SwingConstants.LEFT);
        buttonLicense.setBorderPainted(false);
        buttonLicense.setOpaque(false);
        buttonLicense.setBackground(Color.WHITE);
        try {
        	buttonLicense.addActionListener(new OpenInWeb(new URI(licenseURI)));
        } catch (URISyntaxException e) {
            logger.info("button action listener failed");
        }
        this.message = message;
        this.title = "Information";
        this.checkbox = new JCheckBox("Do not show this message again for the rest of this session");
        this.licensebox = new JCheckBox("I agree with the NeDRex Terms of Use available at: https://api.nedrex.net/static/licence");
        this.options = new Object[]{"Continue", "Cancel"};
        this.hide = false;
    }
    
    

    public int showMessage(){
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.PAGE_AXIS));
        messagePanel.add(new JLabel(message));
        if(buttonPaper != null) {
            messagePanel.add(buttonPaper);
        }
        if(buttonTutorial != null) {
            messagePanel.add(buttonTutorial);
        }
        if(buttonLicense != null) {
            messagePanel.add(buttonLicense);
        }
        if(licensebox != null) {
            messagePanel.add(licensebox);
            JLabel dummy = new JLabel(" ");
            messagePanel.add(dummy);
        }
        
        messagePanel.add(checkbox);
        return JOptionPane.showOptionDialog(
                app.getCySwingApplication().getJFrame(),
                messagePanel,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);
    }

    public Object[] getOptions() {
        return options;
    }

    public void setOptions(Object[] options) {
        this.options = options;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public JCheckBox getCheckbox() {
        return checkbox;
    }
    
    public JCheckBox getLicensbox() {
        return licensebox;
    }

    static class OpenInWeb implements ActionListener {
        URI uri;

        public OpenInWeb(URI uri){
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

