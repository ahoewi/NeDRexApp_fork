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
public class ComorbiditomeWebServiceClient extends AbstractWebServiceGUIClient implements NetworkImportWebServiceClient{
	
	RepoApplication app;
	static final String APP_DESCRIPTION
			= "<html>" +
			"The <b>NeDRex</b> platform (Network-based Drug Repurposing and Exploration) is a user-friendly platform for network-based " +
			"disease module discovery and drug repurposing. <b>NeDRex</b> is built of three main components: the <b>NeDRexDB</b>, the <b>NeDRexApp</b> and the <b>NeDRexAPI</b>. " +
			"<br>By querying NeDRexDB, you can download the disease-disease network constructed based on the comorbidity data from Estonia Biobank."+
			"<br> <br> A tutorial for the NeDRexApp can be found " +
			"<a href=\"https://nedrex.net/tutorial\">here</a>. " +
			""			
            + "<br><br><b>License Agreement:</b> By using the NeDRex platform you are entering a legally binding agreement. Using the NeDRex platform implies that you agree to the Terms of Use. "
            + "If you do not agree with the Terms of Use, do not use the platform. The Terms of Use are available at: <a href=\"https://api.nedrex.net/static/licence\">https://api.nedrex.net/static/licence</a>"
			+ "</html>";



	public ComorbiditomeWebServiceClient(RepoApplication app) {
		super(NeDRexService.API_LINK,
				"NeDRex: comodbidity network query",
				APP_DESCRIPTION);
		this.app = app;
		super.gui = new GetComorbiditomePanel(app);
	}
	
	@Override
	public TaskIterator createTaskIterator(Object query) {
		if (query == null)
			throw new NullPointerException("null query");
		return new TaskIterator();
	}

}
