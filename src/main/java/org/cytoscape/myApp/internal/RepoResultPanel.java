package org.cytoscape.myApp.internal;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.myApp.internal.tasks.BiConTask;
import org.cytoscape.myApp.internal.tasks.DrugBasedValidTask;
import org.cytoscape.myApp.internal.tasks.DrugValidationTask;
import org.cytoscape.myApp.internal.tasks.JointValidationTask;
import org.cytoscape.myApp.internal.tasks.MechBasedValidTask;
import org.cytoscape.myApp.internal.tasks.ModuleValidationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeDRex App
 * @author Sepideh Sadegh, Judith Bernett
 */
public class RepoResultPanel extends JPanel implements CytoPanelComponent{
	
	private RepoApplication app;
	//private JPanel btnPanel;
	JButton btnClose;
	JScrollPane scrollPane;
	JScrollPane scrollPaneTable;
	//JPanel bigScrollPanePanel;
	JPanel scrollPanePanel;
	JPanel scrollPanePanelTable;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public RepoResultPanel(RepoApplication app) {
		super();
		this.app = app;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		/*btnPanel = new JPanel();
		add(btnPanel);
		btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));*/

		scrollPanePanel = new JPanel();
		scrollPanePanel.setLayout(new BoxLayout(scrollPanePanel, BoxLayout.Y_AXIS));
		scrollPane = new JScrollPane(scrollPanePanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.getViewport().setBackground(Color.WHITE);
		add(scrollPane);
		add(Box.createRigidArea(new Dimension(0,5)));

		scrollPanePanelTable = new JPanel();
		scrollPanePanelTable.setLayout(new BoxLayout(scrollPanePanelTable, BoxLayout.Y_AXIS));
		scrollPaneTable = new JScrollPane(scrollPanePanelTable);
		scrollPaneTable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneTable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPaneTable.getViewport().setBackground(Color.WHITE);
		add(scrollPaneTable);

		btnClose = new JButton("Close result panel");
		btnClose.addActionListener(new CloseResultPanelAction(this));
		add(btnClose);
		this.repaint();

	}
	
	public void activate() {
		// register
		this.app.getActivator().registerService(this, CytoPanelComponent.class);
		
		// focus on this panel
		CytoPanel cyto_panel = app.getCySwingApplication().getCytoPanel(getCytoPanelName());
		if (cyto_panel.getState() == CytoPanelState.HIDE) {
			cyto_panel.setState(CytoPanelState.DOCK);
		}
		
		setVisible(true);
		cyto_panel.setSelectedIndex(cyto_panel.indexOfComponent(getComponent()));
	}

	public void activateFromBicon(BiConTask biConTask){
		JLabel label = new JLabel("Clustermap for file " + biConTask.getInputFile().getName());
		JLabel settingslabel = new JLabel("Settings: min " + biConTask.getLgMin().getValue() + ", max " + biConTask.getLgMax().getValue());
		scrollPanePanel.add(label);
		scrollPanePanel.add(settingslabel);
		scrollPanePanel.add(Box.createRigidArea(new Dimension(0,5)));

		Image image = biConTask.getHeatmap();
		ImageIcon icon = new ImageIcon(image);
		Image newimg = image.getScaledInstance(400, 285,  java.awt.Image.SCALE_SMOOTH);
		icon = new ImageIcon(newimg);  // transform it back
		JLabel picLabel = new JLabel(icon);
		scrollPanePanel.add(picLabel);
		scrollPanePanel.add(Box.createRigidArea(new Dimension(0,5)));

		JButton downloadButton = new JButton("Download this image");
		downloadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);
				int result = fileChooser.showOpenDialog(scrollPane);
				if (result == JFileChooser.APPROVE_OPTION) {
					File directory = fileChooser.getSelectedFile();
					String name = biConTask.getInputFile().getName() + "_" + biConTask.getLgMin().getValue() + "_" + biConTask.getLgMax().getValue();
					File saveFile = new File(directory.getAbsolutePath() + "/" + name + ".png");
					logger.info("File for heatmap: " + saveFile);
					try {
						ImageIO.write((RenderedImage) image, "png", saveFile);
					} catch (IOException e) {

					}
				}
			}
		});
		scrollPanePanel.add(downloadButton);

		JButton deleteButton = new JButton("Delete this image");
		scrollPanePanel.add(deleteButton);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				scrollPanePanel.remove(label);
				scrollPanePanel.remove(settingslabel);
				scrollPanePanel.remove(picLabel);
				scrollPanePanel.remove(downloadButton);
				scrollPanePanel.remove(deleteButton);
				scrollPanePanel.repaint();
			}
		});

		String[] columnNames = {"Patient Group", "Patient ID"};
		JLabel label2 = new JLabel("Table for file " + biConTask.getInputFile().getName());
		label2.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel settingslabel2 = new JLabel("Settings: min " + biConTask.getLgMin().getValue() + ", max " + biConTask.getLgMax().getValue());
		settingslabel2.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPanePanelTable.add(label2);
		scrollPanePanelTable.add(settingslabel2);
		scrollPanePanelTable.add(Box.createRigidArea(new Dimension(0,5)));
		HashMap<String, Set<String>> patientgroups = biConTask.getPatientgroups();

		Object[][] data = new Object[patientgroups.get("patients1").size() + patientgroups.get("patients2").size()][2];
		int count = 0;
		for (String s : patientgroups.get("patients1")) {
			data[count][0] = "Group 1";
			data[count][1] = s;
			count++;
		}
		for (String s : patientgroups.get("patients2")) {
			data[count][0] = "Group 2";
			data[count][1] = s;
			count++;
		}
		JTable jTable = new JTable(data, columnNames);
		jTable.getColumnModel().getColumn(0).setMaxWidth(150);
		jTable.getColumnModel().getColumn(1).setMaxWidth(350);

		JScrollPane tableScrollPane = new JScrollPane(jTable);
		tableScrollPane.setMaximumSize(new Dimension(500,150));
		tableScrollPane.setMinimumSize(new Dimension(500,150));
		tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		tableScrollPane.getViewport().setBackground(Color.WHITE);
		tableScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPanePanelTable.add(tableScrollPane);
		scrollPanePanelTable.add(Box.createRigidArea(new Dimension(0,5)));

		JButton deleteButton2 = new JButton("Delete this table");
		deleteButton2.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPanePanelTable.add(deleteButton2);
		deleteButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				scrollPanePanelTable.remove(label2);
				scrollPanePanelTable.remove(settingslabel2);
				scrollPanePanelTable.remove(tableScrollPane);
				scrollPanePanelTable.remove(deleteButton2);
				scrollPanePanelTable.repaint();
			}
		});

		activate();
	}
	
	public void activateFromDrugValidation(DrugValidationTask drValTask){
		JLabel label = new JLabel("<html><b>Validation of the candidate drugs returned by NeDRex</b></html>");
		
		JLabel settingslabel = new JLabel("Settings: number of permutations " +  drValTask.getPermutations().toString() + ", considering " + drValTask.getApproved()
		+ " drugs in NeDRexDB");
		JLabel pvallabel = new JLabel("Empirical p-value: " + drValTask.getPVal());
		JLabel pvalDCGlabel = new JLabel("DCG-based empirical p-value: " + drValTask.getPValDCG());
		JLabel decriptionlabel = new JLabel("Description of the validation run: " + drValTask.getDescription());
		scrollPanePanel.add(Box.createVerticalStrut(10));
		scrollPanePanel.add(label);
		scrollPanePanel.add(Box.createVerticalStrut(10));
		scrollPanePanel.add(settingslabel);
		scrollPanePanel.add(decriptionlabel);
		scrollPanePanel.add(Box.createVerticalStrut(10));
		scrollPanePanel.add(pvallabel);
		scrollPanePanel.add(pvalDCGlabel);
		scrollPanePanel.add(Box.createVerticalStrut(50));
//		scrollPanePanel.add(Box.createRigidArea(new Dimension(0,5)));

		JButton deleteButton = new JButton("Delete this result");
		scrollPanePanel.add(deleteButton);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				scrollPanePanel.remove(label);
				scrollPanePanel.remove(pvallabel);
				scrollPanePanel.remove(pvalDCGlabel);
				scrollPanePanel.remove(decriptionlabel);
				scrollPanePanel.remove(settingslabel);
				scrollPanePanel.remove(deleteButton);
				scrollPanePanel.repaint();
			}
		});

		activate();
	}
	
	public void activateFromJointValidation(JointValidationTask jointValTask){
//	public void activateFromJointValidation(MechBasedValidTask mechValTask){
		JLabel label = new JLabel("<html><b>Joint validation of the disease module and drugs returned by NeDRex</b></html>");
		JLabel settingslabel = new JLabel("Settings: number of permutations " +  jointValTask.getPermutations().toString() + ", considering " + jointValTask.getApproved()
		+ " drugs in NeDRexDB");
		JLabel pvallabel = new JLabel("Empirical p-value: " + jointValTask.getPVal());
		JLabel pvalpreclabel = new JLabel("Empirical p-value (based on precision): " + jointValTask.getPValPrec());
		JLabel decriptionlabel = new JLabel("Description of the validation run: " + jointValTask.getDescription());
		scrollPanePanel.add(Box.createVerticalStrut(10));
		scrollPanePanel.add(label);
		scrollPanePanel.add(Box.createVerticalStrut(10));
		scrollPanePanel.add(settingslabel);
		scrollPanePanel.add(decriptionlabel);
		scrollPanePanel.add(Box.createVerticalStrut(10));
		scrollPanePanel.add(pvallabel);
		scrollPanePanel.add(pvalpreclabel);
		scrollPanePanel.add(Box.createVerticalStrut(50));
//		scrollPanePanel.add(Box.createRigidArea(new Dimension(0,5)));

		JButton deleteButton = new JButton("Delete this result");
		scrollPanePanel.add(deleteButton);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				scrollPanePanel.remove(label);
				scrollPanePanel.remove(pvallabel);
				scrollPanePanel.remove(pvalpreclabel);
				scrollPanePanel.remove(decriptionlabel);
				scrollPanePanel.remove(settingslabel);
				scrollPanePanel.remove(deleteButton);
				scrollPanePanel.repaint();
			}
		});

		activate();
	}
	
	public void activateFromModuleValidation(ModuleValidationTask moduleValTask){
		JLabel label = new JLabel("<html><b>Validation of the disease module returned by NeDRex</b></html>");
		JLabel settingslabel = new JLabel("Settings: number of permutations " +  moduleValTask.getPermutations().toString() + ", considering " + moduleValTask.getApproved()
		+ " drugs in NeDRexDB");
		JLabel pvallabel = new JLabel("Empirical p-value: " + moduleValTask.getPVal());
		JLabel pvalpreclabel = new JLabel("Empirical p-value (based on precision): " + moduleValTask.getPValPrec());
		JLabel decriptionlabel = new JLabel("Description of the validation run: " + moduleValTask.getDescription());
		scrollPanePanel.add(Box.createVerticalStrut(10));
		scrollPanePanel.add(label);
		scrollPanePanel.add(Box.createVerticalStrut(10));
		scrollPanePanel.add(settingslabel);
		scrollPanePanel.add(decriptionlabel);
		scrollPanePanel.add(Box.createVerticalStrut(10));
		scrollPanePanel.add(pvallabel);
		scrollPanePanel.add(pvalpreclabel);
		scrollPanePanel.add(Box.createVerticalStrut(50));

		JButton deleteButton = new JButton("Delete this result");
		scrollPanePanel.add(deleteButton);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				scrollPanePanel.remove(label);
				scrollPanePanel.remove(pvallabel);
				scrollPanePanel.remove(pvalpreclabel);
				scrollPanePanel.remove(decriptionlabel);
				scrollPanePanel.remove(settingslabel);
				scrollPanePanel.remove(deleteButton);
				scrollPanePanel.repaint();
			}
		});

		activate();
		}

	public void deactivate() {
		this.app.getActivator().unregisterAllServices(this);
//		result_area.setText("");
//		scrollPane_output.setBorder(BorderFactory.createTitledBorder("NeDRex results:"));
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	@Override
	public String getTitle() {
		
		return this.app.getAppName();
	}

	@Override
	public Icon getIcon() {
		return null;
	}

}
