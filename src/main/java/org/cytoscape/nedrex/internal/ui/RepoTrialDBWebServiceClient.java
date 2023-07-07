package org.cytoscape.nedrex.internal.ui;

import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.swing.AbstractWebServiceGUIClient;
import org.cytoscape.nedrex.internal.NeDRexService;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.work.TaskIterator;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 * @modified by: Andreas Maier
 */
public class RepoTrialDBWebServiceClient extends AbstractWebServiceGUIClient implements NetworkImportWebServiceClient{
	
	RepoApplication app;
	static final String APP_DESCRIPTION
			= "<html>" +
			"The <b>NeDRex</b> platform (Network-based Drug Repurposing and Exploration) is a user-friendly platform for network-based " +
			"disease module discovery and drug repurposing. <b>NeDRex</b> is built of three main components: the <b>NeDRexDB</b>, the <b>NeDRexApp</b> and the <b>NeDRexAPI</b>. " +
			"The <b>NeDRexDB</b> integrates data from various biomedical databases such as: " +
			"<ul>" +
			"<li> OMIM</li>" +
			"<li> DisGeNET</li>" +
			"<li>UniProt</li>" +
			"<li>NCBI gene info</li>" +
			"<li>IID</li>" +
			"<li>MONDO</li>" +
			"<li>DrugBank</li>" +
			"<li>DrugCentral</li>" +
			"</ul>	 " +
			"Integration of multiple databases enables us to construct heterogeneous networks representing distinct types of " +
			"biomedical entities (e.g. disorders, genes, drugs) and the associations between them. These networks can be " +
			"accessed and explored by NeDRexApp. To this end, you need to load the networks through this Import Network Panel, "+
			"after specifying the desired types of associations."+
			"<br> <br> A tutorial for the NeDRexApp can be found " +
			"<a href=\"https://nedrex.net/tutorial\">here</a>. " +
			""+
			"<br>For more information about the versions of databases integrated in the current version of the <b>NeDRexDB</b> use the <font color = \\\"#143e82\\\">Info</font> button."
			+ "<br><br><b>NeDRexDB</b> contains information from the <b>Online Mendelian Inheritance in Man® (OMIM®)</b> database, "
            + "which has been obtained under a license from the Johns Hopkins University.  NeDRexDB does not "
            + "represent the entire, unmodified OMIM® database, which is available in its entirety at <a href=\"https://omim.org/downloads\">https://omim.org/downloads</a><br>"
            + "<br><br><b>License Agreement:</b> By using the NeDRex platform you are entering a legally binding agreement. Using the NeDRex platform implies that you agree to the Terms of Use. "
            + "If you do not agree with the Terms of Use, do not use the platform. The Terms of Use are available at: <a href=\"https://api.nedrex.net/static/licence\">https://api.nedrex.net/static/licence</a>"
			+ "</html>";





	public RepoTrialDBWebServiceClient(RepoApplication app) {
		super(NeDRexService.API_LINK,
				"NeDRex: network query from NeDRexDB",
				APP_DESCRIPTION);
		this.app = app;
		super.gui = new GetNetworkPanel(app);
	}

	@Override
	public TaskIterator createTaskIterator(Object query) {
		if (query == null)
			throw new NullPointerException("null query");
		return new TaskIterator();
	}

}
