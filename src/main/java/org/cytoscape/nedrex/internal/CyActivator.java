package org.cytoscape.nedrex.internal;

import java.util.Properties;

import org.cytoscape.nedrex.internal.tasks.DeselectSingleNodeTaskFactory;
import org.cytoscape.nedrex.internal.tasks.OpenEntryInDBTaskFactory;
import org.cytoscape.nedrex.internal.ui.ComorbiditomeWebServiceClient;
import org.cytoscape.nedrex.internal.ui.RepoTrialDBWebServiceClient;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NodeViewTaskFactory;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.*;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class CyActivator extends AbstractCyActivator {

	private BundleContext context;
	private CyServiceRegistrar registrar;
	
	public CyActivator() {
		super();
	}
	
	
	@Override
	public void start(BundleContext context) throws Exception {
		
		this.context = context;
		this.registrar = this.getService(CyServiceRegistrar.class);
		
		// start actual application
		registerService(new RepoApplication(this), RepoApplication.class);
		// to access the registered application
		RepoApplication app = this.getService(RepoApplication.class);
		
		{
			// Register our web service clients
			RepoTrialDBWebServiceClient client = new RepoTrialDBWebServiceClient(app);
			registerAllServices(context, client, new Properties());
			
			ComorbiditomeWebServiceClient comorbClient = new ComorbiditomeWebServiceClient(app);
			registerAllServices(context, comorbClient, new Properties());
		}
		final OpenEntryInDBTaskFactory openEntryInDBTaskFactory = new OpenEntryInDBTaskFactory();
		final Properties openProp = new Properties();
		openProp.setProperty("preferredTaskManager", "menu");
		openProp.setProperty(PREFERRED_MENU, "NeDRex[1]");
		openProp.setProperty(MENU_GRAVITY, "10.0");
		openProp.setProperty(TITLE, "Open Entry in Database");
		registerService(context, openEntryInDBTaskFactory, NodeViewTaskFactory.class, openProp);

		final DeselectSingleNodeTaskFactory deselectSingleNodeTaskFactory = new DeselectSingleNodeTaskFactory();
		final Properties deselectProp = new Properties();
		deselectProp.setProperty("preferredTaskManager", "menu");
		deselectProp.setProperty(PREFERRED_MENU, "NeDRex[1]");
		deselectProp.setProperty(MENU_GRAVITY, "0.0");
		deselectProp.setProperty(TITLE, "Deselect this node");
		registerService(context, deselectSingleNodeTaskFactory, NodeViewTaskFactory.class, deselectProp);
		
	}
	
	 /*
	  * service-related functions
	  */
	
	public <S> S getService(Class<S> cls) {
		return this.getService(this.context, cls);
	}
	
	public <S> S getService(Class<S> cls, String properties) {
		return this.getService(this.context, cls, properties);
	}
	
	public <S> void registerService(S obj, Class<S> cls) {
		registrar.registerService(obj, cls, new Properties());
	}
	
	public void registerService(Object obj, Class<?> cls, Properties properties) {
		registrar.registerService(obj, cls, properties);
	}
	
	public <S> void unregisterService(S obj, Class<S> cls) {
		registrar.unregisterService(obj, cls);
	}
	
	public <S> void unregisterAllServices(S obj) {
		registrar.unregisterAllServices(obj);
	}

}
