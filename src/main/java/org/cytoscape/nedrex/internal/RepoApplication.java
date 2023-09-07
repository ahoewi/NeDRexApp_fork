package org.cytoscape.nedrex.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.nedrex.internal.menuactions.*;
import org.cytoscape.nedrex.internal.utils.ApiRoutesUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class RepoApplication {
	private CyActivator activator;
	private CySwingApplication application;
	private RepoResultPanel resultPanel;
	private String app_name = "NeDRex";
	private String version = "1.0-test"; // we update this as we do for AboutPanel

	private JDialog aboutDialog;
	private JDialog licenseDialog;
	private AboutPanel aboutPanel;
	private LicensePanel licensePanel;
//	private JDialog quickSelectDialog;
	private JDialog quickSelectDialog2;
	private QuickSelectPanel2 quickSelectPanel2;

	private NeDRexService nedrexService;

	private ApiRoutesUtil apiRoutesUtil;

	public RepoApplication(CyActivator activator, NeDRexService nedrexService, ApiRoutesUtil apiRoutesUtil) {
		this.activator = activator;
		this.nedrexService = nedrexService;
		this.apiRoutesUtil = apiRoutesUtil;
		this.application = activator.getService(CySwingApplication.class);
		this.resultPanel = new RepoResultPanel (this);

		aboutDialog = new JDialog(application.getJFrame(), true);
		aboutDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		centerOnScreen(aboutDialog);

		this.aboutPanel = new AboutPanel(this);

		this.aboutDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.aboutDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				aboutPanel.deactivate();
			}
		});
		
		licenseDialog = new JDialog(application.getJFrame(), true);
		licenseDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		centerOnScreen(licenseDialog);

		this.licensePanel = new LicensePanel(this);

		this.licenseDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.licenseDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				licensePanel.deactivate();
			}
		});
		
		this.application.addAction(new QuickStartAction(this));

		this.quickSelectDialog2 = new JDialog(application.getJFrame(), true);
		this.quickSelectDialog2.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		centerOnScreen(this.quickSelectDialog2);
		
		this.quickSelectPanel2 = new QuickSelectPanel2(this);

		this.quickSelectDialog2.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.quickSelectDialog2.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				quickSelectPanel2.deactivate();
			}
		});
//		this.application.addAction(new QuickSelectAction(quickSelectPanel));
		this.application.addAction(new QuickSelectAction2(quickSelectPanel2));
		this.application.addAction(new GetDiseaseGenesAction(this));
		this.application.addAction(new GetDiseaseDrugsAction(this));
		
		this.application.addAction(new DiamondAction(this));
		this.application.addAction(new MuSTapiAction(this));
		this.application.addAction(new BiConAction(this, resultPanel));
		
		this.application.addAction(new TargetDrugsAction(this));
		this.application.addAction(new ClosenessDrugAction(this));
		this.application.addAction(new TrustRankDrugAction(this));
		
//		this.application.addAction(new DrugBasedValidAction(this, resultPanel));
//		this.application.addAction(new MechanismBasedValidAction(this, resultPanel));
		this.application.addAction(new DrugValidationAction(this, resultPanel));
		this.application.addAction(new ModuleValidationAction(this, resultPanel));
		this.application.addAction(new JointValidationAction(this, resultPanel));
		
		this.application.addAction(new SelectNodeFileAction(this));
		this.application.addAction(new SelectAllNodeOfTypeAction(this));
		this.application.addAction(new SelectNeighborNodeOfTypeAction(this));
		
		// By loading any clustered data as table in the network and an input file containing the id/name of diseases, 
		//it creates a view of graph with only clusters of the input diseases, relevant for internal use
//		this.application.addAction(new ViewClusterAction(this));
				
		this.application.addAction(new NeighborModuleAction(this));
		this.application.addAction(new DisToDrugCandidAction(this));
		this.application.addAction(new DrugToDisCandidAction(this));
		this.application.addAction(new MuSTAction(this));
		
		this.application.addAction(new InducedSubnetAction(this));
		this.application.addAction(new MapSelectionAction(this));
		
		this.application.addAction(new DiseasomeAction(this));
		this.application.addAction(new DrugomeAction(this));

		this.application.addAction(new ICD10toMONDOAction(this));

		this.application.addAction(new CreateVisStyleAction(this));
		
		this.application.addAction(new ShowResultPanelAction(resultPanel));
		
		this.application.addAction(new AboutAction(this, aboutPanel));
		
		this.application.addAction(new LicenseAction(this, licensePanel));

	}

	public NeDRexService getNedrexService() {
		return nedrexService;
	}

	public ApiRoutesUtil getApiRoutesUtil() {
		return apiRoutesUtil;
	}

	public JDialog getQuickSelectDialog2(){
		return this.quickSelectDialog2;
	}

	public JDialog getAboutDialog(){
		return this.aboutDialog;
	}

	public AboutPanel getAboutPanel() {
		return aboutPanel;
	}
	
	public LicensePanel getLicensePanel() {
		return licensePanel;
	}
	
	public JDialog getLicenseDialog(){
		return this.licenseDialog;
	}

	public void centerOnScreen(final Component c) {
		final int width = c.getWidth();
		final int height = c.getHeight();
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = ((screenSize.width / 2) - (width / 2))/2;
		int y = ((screenSize.height / 2) - (height / 2))/2;
		c.setLocation(x, y);
	}
	
	
	/*
	 * Cytoscape-specific helper functions
	 */
	
	public CyApplicationManager getApplicationManager() {
		return activator.getService(CyApplicationManager.class);
	}
	
	public CyNetworkManager getNetworkManager() {
		return activator.getService(CyNetworkManager.class);
	}
	
	public Set<CyNetwork> getNetworkSet(){
		return getNetworkManager().getNetworkSet();
	}
	
	public CyNetwork getCurrentNetwork() {
		return getApplicationManager().getCurrentNetwork();
	}
	
	public void deselectCurrentNetwork() {		
		getApplicationManager().setCurrentNetwork(null);
	}
	
	public CyNetworkView getCurrentNetworkView() {
		return getApplicationManager().getCurrentNetworkView();
	}
	
	public CyNetworkViewManager getNetworkViewManager() {
		return activator.getService(CyNetworkViewManager.class);
	}
	
	public CyLayoutAlgorithmManager getCyLayoutAlgorithmManager() {
		return activator.getService(CyLayoutAlgorithmManager.class);
		
	}
	
	public TaskManager getTaskManager() {
		return activator.getService(TaskManager.class);
	}
	
	/*
	 * Standard getters
	 */
	
	public CyActivator getActivator() {
		return activator;
	}

	public CySwingApplication getCySwingApplication() {
		return application;
	}

	public String getAppName() {
		return app_name;
	}
	
	public String getVersion() throws IOException {
		Properties props = new Properties();
        props.load(Objects.requireNonNull(AboutPanel.class.getClassLoader().getResourceAsStream("project.properties")));
        version = props.getProperty("version");
		return version;
	}
}
