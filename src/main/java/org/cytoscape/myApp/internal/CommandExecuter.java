package org.cytoscape.myApp.internal;

import java.util.Map;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class CommandExecuter implements TaskObserver{
	
	private RepoApplication app;
	CommandExecutorTaskFactory commandTaskFactory = null;
	SynchronousTaskManager<?> taskManager = null;
	
	public CommandExecuter(RepoApplication app) {
		this.app = app;
	}

	public void executeCommand(String namespace, String command, Map<String, Object> args, TaskObserver observer) {
		if (commandTaskFactory == null)
			commandTaskFactory = app.getActivator().getService(CommandExecutorTaskFactory.class);

		if (taskManager == null)
			taskManager = app.getActivator().getService(SynchronousTaskManager.class);
		TaskIterator ti = commandTaskFactory.createTaskIterator(namespace, command, args, observer);
		taskManager.execute(ti);
	}
	@Override
	public void taskFinished(ObservableTask task) {
		// TODO Auto-generated method stub
	}

	@Override
	public void allFinished(FinishStatus finishStatus) {
		// TODO Auto-generated method stub		
	}
	
	

}
