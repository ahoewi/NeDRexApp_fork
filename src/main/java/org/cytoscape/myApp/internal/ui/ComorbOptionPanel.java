package org.cytoscape.myApp.internal.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.myApp.internal.RepoApplication;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class ComorbOptionPanel extends JPanel{
	
	private RepoApplication app;
	
	JPanel edgeOptions;
	JCheckBox edgeTypeGD, edgeTypeGP, edgeTypePP, edgeTypeDrP, edgeTypeDrDis, edgeTypeDD, edgeTypePwP;
	JCheckBox agreed;
	JCheckBox phicor;
	JCheckBox pval;
	JCheckBox induced;	
	JSlider phicorMin;
	JSlider pvalMax;
	JTextField phicorField;
	JTextField pvalField;
	JTextField newNetworkName;
	
	public ComorbOptionPanel(RepoApplication app) {
		this.app = app;
		
		initOptions();
	}
	
	private void initOptions() {
		setPreferredSize(new Dimension(625,150));
		EasyGBC c = new EasyGBC();
		
		JPanel phiCorOptions = createPhiCorOptions();
		add(phiCorOptions, c.down().expandBoth().insets(7,5,0,5));
		
		JPanel pValOptions = createPValOptions();
		add(pValOptions, c.down().expandBoth().insets(7,5,0,5));
		
		JPanel newNetworkOptions = createnewNetOptions();
		add(newNetworkOptions, c.down().expandBoth().insets(7,5,0,5));
		
		JPanel licenseAgreement = createLicenseAgreement();
		add(licenseAgreement, c.down().expandBoth().insets(7,5,0,5));
	}
	

	
	JPanel createPhiCorOptions() {
		JPanel phiCorOptionPanel = new JPanel(new GridBagLayout());
		phiCorOptionPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		EasyGBC c = new EasyGBC();
		JLabel optionsLabel = new JLabel("<html><b>Filtering based on Phi-correlation:</b></html>");
		c.anchor("west").insets(0,5,0,5);
		phiCorOptionPanel.add(optionsLabel, c);
		
		c.right().noExpand().insets(0,5,0,2);		
		phicor = new JCheckBox("Minimum value:", false);
		phicor.setToolTipText("Check if you want to filter out edges with phi correlation below this value.");
		phiCorOptionPanel.add(phicor, c);
		
		c.right().expandHoriz().insets(0, 5, 0, 2);
		phicorMin = new JSlider(0, 50, 0);
		phicorMin.setEnabled(false);
		phicorMin.setMajorTickSpacing(10);
		phicorMin.setMinorTickSpacing(2);
		phicorMin.setPaintTicks(true);
	    java.util.Hashtable<Integer,JLabel> labelTable = new java.util.Hashtable<Integer,JLabel>();
	    labelTable.put(new Integer(50), new JLabel("0.5"));
	    labelTable.put(new Integer(40), new JLabel("0.4"));
	    labelTable.put(new Integer(30), new JLabel("0.3"));
	    labelTable.put(new Integer(20), new JLabel("0.2"));
	    labelTable.put(new Integer(10), new JLabel("0.1"));
	    labelTable.put(new Integer(0), new JLabel("0.0"));
	    phicorMin.setLabelTable(labelTable);
	    phicorMin.setPaintLabels(true);

	    phicorMin.setToolTipText("<html>" +
	    		"Minimum threshold of Phi correlation for filtering comorbidity associations. Default: 0 (returns all edges)"+
	    		"<br>Phi correlation is a measure of association for two binary variables. In the comorbiditome, it has a value between 0 and 1."
	    		+ "<br>The higher the value, indicates more evidence for two diseases co-occuring more frequently than expected by chance." +
	    		"</html>");
	    
	    phiCorOptionPanel.add(phicorMin, c);
	    
	    phicor.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                	phicorMin.setEnabled(true);
                }
                else if(e.getStateChange() == ItemEvent.DESELECTED){
                	phicorMin.setEnabled(false);
                }
                validate();
                repaint();
            }
        });
		
	    c.right().expandHoriz().insets(0, 5, 0, 2);
	    phicorField = new JTextField(3);
		phiCorOptionPanel.add(phicorField, c);
		
		phicorMin.addChangeListener(new ChangeListener() {
	    	@Override
	    	public void stateChanged(ChangeEvent e) {
//	            JSlider slider = (JSlider) e.getSource();
	    		phicorField.setText(String.valueOf((double)phicorMin.getValue()/100));
	        }
	    });
	  		
		return phiCorOptionPanel;
	}
		
	JPanel createPValOptions() {
		JPanel pValOptionPanel = new JPanel(new GridBagLayout());
		pValOptionPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		EasyGBC c = new EasyGBC();
		JLabel optionsLabel = new JLabel("<html><b>Filtering based on P-value:</b></html>");
		c.anchor("west").insets(0,5,0,5);
		pValOptionPanel.add(optionsLabel, c);
		
		c.right().noExpand().insets(0,5,0,2);		
		pval = new JCheckBox("Maximum value:", false);
		pval.setToolTipText("Check if you want to filter out edges with P-values above this value.");
		pValOptionPanel.add(pval, c);
		
		c.right().expandHoriz().insets(0, 5, 0, 2);
		pvalMax = new JSlider(0, 50, 50);
		pvalMax.setEnabled(false);
		pvalMax.setMajorTickSpacing(10);
		pvalMax.setMinorTickSpacing(2);
		pvalMax.setPaintTicks(true);
	    java.util.Hashtable<Integer,JLabel> labelTable = new java.util.Hashtable<Integer,JLabel>();
	    labelTable.put(new Integer(50), new JLabel("0.05"));
	    labelTable.put(new Integer(40), new JLabel("0.04"));
	    labelTable.put(new Integer(30), new JLabel("0.03"));
	    labelTable.put(new Integer(20), new JLabel("0.02"));
	    labelTable.put(new Integer(10), new JLabel("0.01"));
	    labelTable.put(new Integer(0), new JLabel("0.0"));
	    pvalMax.setLabelTable(labelTable);
	    pvalMax.setPaintLabels(true);

	    pvalMax.setToolTipText("<html>" +
	    		"Maximum threshold of P_value for filtering comorbidity associations. Default: 0.05 (returns all edges)"+
	    		"<br>One-tailed Fisher's exact test followed by Benjamini-Hochberg correction for multiple testing (P<=0.05)"
	    		+ "is applied to determine the significance of comorbidity associations."
	    		+ "<br>You can apply more restrictive P-value threshold by setting the value here." +
	    		"</html>");
	    
	    pValOptionPanel.add(pvalMax, c);
	    
	    pval.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                	pvalMax.setEnabled(true);
                }
                else if(e.getStateChange() == ItemEvent.DESELECTED){
                	pvalMax.setEnabled(false);
                }
                validate();
                repaint();
            }
        });
		
	    c.right().expandHoriz().insets(0, 5, 0, 2);
	    pvalField = new JTextField(4);
	    pValOptionPanel.add(pvalField, c);
		
	    pvalMax.addChangeListener(new ChangeListener() {
	    	@Override
	    	public void stateChanged(ChangeEvent e) {
//	            JSlider slider = (JSlider) e.getSource();
	    		pvalField.setText(String.valueOf((double)pvalMax.getValue()/1000));
	        }
	    });
	  		
		return pValOptionPanel;
	}	 
	
	JPanel createnewNetOptions() {		
		JPanel additionalOptionPanel = new JPanel(new GridBagLayout());
		additionalOptionPanel.setPreferredSize(new Dimension(650,30));
		additionalOptionPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		EasyGBC c = new EasyGBC();
		JLabel optionsLabel = new JLabel("<html><b>Network Options:</b></html>");		
		c.anchor("west").insets(0,5,0,5);
		additionalOptionPanel.add(optionsLabel, c);
		
		c.right().noExpand().insets(0,10,0,1);
		JLabel netName = new JLabel("Network name");
		netName.setToolTipText("Enter the name you would like to be assigned to the loaded network. It is required to enter a name here!");
		additionalOptionPanel.add(netName, c);

		newNetworkName = new JTextField("Comorbiditome");
		c.right().expandHoriz().insets(0,10,0,5);
		newNetworkName.setColumns(15);
		additionalOptionPanel.add(newNetworkName, c);
		
		c.right().expandHoriz().insets(0,10,0,5);
		JLabel dummy = new JLabel("");
		additionalOptionPanel.add(dummy, c);
		
		c.right().expandHoriz().insets(0,10,0,5);
		induced = new JCheckBox("Induced subnetwork", false);
		induced.setToolTipText("<html>"+"Check if you want to import an induced subnetwork of comorbiditome with the diseases selected in the current network. "
				+ "<br>This will not return the whole Comorbiditome but a subnetwork of it"
				+ "</html>");
		additionalOptionPanel.add(induced, c);
		
		return additionalOptionPanel;
	}
	
	JPanel createLicenseAgreement() {

		JPanel licenseAgreementPanel = new JPanel(new GridBagLayout());
		licenseAgreementPanel.setPreferredSize(new Dimension(600,30));
		licenseAgreementPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		EasyGBC c = new EasyGBC();
		JLabel agreementLabel = new JLabel("<html><b>License Agreement:</b></html>");		
		c.anchor("west").insets(0,5,0,5);
		licenseAgreementPanel.add(agreementLabel, c);
		
		c.right().noExpand().insets(0,10,0,5);		
		agreed = new JCheckBox("I agree with the NeDRex Terms of Use.", false);
		agreed.setToolTipText("If you want to use the NeDRex app, you need to first agree with our terms of use. The NeDRex Terms of Use are available at: https://api.nedrex.net/static/licence");
		licenseAgreementPanel.add(agreed, c);
		return licenseAgreementPanel;
	}

	public Double getPhiCorMin() {
		return (double) phicorMin.getValue()/100;
	}
	
	public Double getPValMax() {
		return (double) pvalMax.getValue()/1000;
	}
	
	public Boolean includePValMax() {
		return pval.isSelected();
	}
	
	public Boolean includePhiCorMin() {
		return phicor.isSelected();
	}
	
	public Boolean isInduced() {
		return induced.isSelected();
	}
	
	public String getNetworkName() {
		return newNetworkName.getText();
	}
	
	public Boolean getAgreementStatus() {
		return agreed.isSelected();
	}

}
