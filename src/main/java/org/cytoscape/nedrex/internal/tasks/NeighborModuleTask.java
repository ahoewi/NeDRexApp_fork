package org.cytoscape.nedrex.internal.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.nedrex.internal.CommandExecuter;
import org.cytoscape.nedrex.internal.ModelUtil;
import org.cytoscape.nedrex.internal.ModulFunctions;
import org.cytoscape.nedrex.internal.RepoApplication;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class NeighborModuleTask extends AbstractTask{
	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@ProvidesTitle
	public String getTitle() {return "Set Neighbor Module Parameters";}
	
	@Tunable(description="Protein-Protein", groups="Edge types to include in the module:      ", params="displayState=uncollapsed",
			tooltip="Include 1st neighboring Protein-Protein interactions.",
			gravity = 3.0)
	public boolean ppi = false;
	
	@Tunable(description="Protein/Gene-Disorder", groups="Edge types to include in the module:      ", params="displayState=uncollapsed",
			tooltip="Include disorders associated with proteins/genes.",
			gravity = 4.0)
	public boolean pgd = false;
	
	@Tunable(description="Collapse proteins on genes", groups="Collapse proteins on their encoding genes:      ",
			tooltip="Collapse proteins on their encoding genes in the created module.",
			gravity = 5.0)
	public boolean clpsPG = false;
	
	@Tunable(description="Use custom name for the result network", groups="Result network",
			tooltip = "Select, if you would like to use your own name for the result network, otherwise a default name will be assigned",
			gravity = 6.0)
	public Boolean set_net_name = false;

	@Tunable(description="Name of the result network", 
			groups="Result network", 
			dependsOn="set_net_name=true",
			tooltip="Enter the name you would like to have assigned to the result network",
			gravity = 6.0)
	public String new_net_name = new String();
	
	public NeighborModuleTask(RepoApplication app) {
		this.app = app;
	}
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
			
		CyNetwork network = app.getCurrentNetwork();
		CyNetworkNaming namingUtil = app.getActivator().getService(CyNetworkNaming.class);
		CyTable nodeTable = network.getDefaultNodeTable();
		
		Collection <CyRow> selNRows = nodeTable.getMatchingRows("selected", true);
		Set <CyNode> selCyNodes = new HashSet<CyNode> ();
		for (CyRow snr: selNRows) {
			//selNodes.add(snr.get("SUID", Long.class));
			selCyNodes.add(network.getNode(snr.get("SUID", Long.class)));
			System.out.println("Selected node: " + snr.get("name", String.class));
		}

		// use setNeighP for source nodes of edges proteins-disease
		Map<String, Set<String>> sPtDmap = new HashMap<String, Set<String>>();
		Map<CyNode, Set<CyNode>> sPtDCyNodemap = new HashMap<CyNode, Set<CyNode>>();
		Set <CyNode> endDNode = new HashSet<CyNode> ();
		Set<CyNode> setNeighP = new HashSet<CyNode>();
		
		if (ppi) {
//			Set<CyNode> setNeighP = new HashSet<CyNode>();
			List<CyEdge> ppEdge = ModulFunctions.firstNeighPP(network, selCyNodes, setNeighP);
			if (pgd) {
				// neighbEdges: set of gene-protein edges, only used if clpsPG=false
				Set <CyEdge> neighbEdges = new HashSet<CyEdge> ();
				// neighbDEdges: set of gene-disease edges if clpsPG=false
				Set <CyEdge> neighbDEdges = new HashSet<CyEdge> ();
				// set of source nodes
				if (!clpsPG) {
					// endNode: Set of genes , neighbEdges are prot-gene edges, neighbDEdges are gene-disease edges
					Set<CyNode> endNode = ModulFunctions.protsGenesDis(network, setNeighP, neighbEdges, neighbDEdges);					
					for (CyEdge ce: neighbDEdges) {
						Collection <CyRow> matchedERows = network.getDefaultEdgeTable().getMatchingRows("SUID", ce.getSUID());
						for (CyRow cr: matchedERows) {
							cr.set("selected", true);
						}
					}
					
					for (CyEdge ce: neighbEdges) {
						Collection <CyRow> matchedERows = network.getDefaultEdgeTable().getMatchingRows("SUID", ce.getSUID());
						for (CyRow cr: matchedERows) {
							cr.set("selected", true);
						}
					}
				}
				
				else if (clpsPG) {					
					for (CyNode n: setNeighP) {
						sPtDmap.put(nodeTable.getRow(n.getSUID()).get("name", String.class), new HashSet<String>());
						sPtDCyNodemap.put(n, new HashSet<CyNode>());
					}
					endDNode = ModulFunctions.protsDis(network, setNeighP, sPtDmap, sPtDCyNodemap);

					for (CyNode n : endDNode) {
						Collection <CyRow> matchedNRows = network.getDefaultNodeTable().getMatchingRows("SUID", n.getSUID());
						for (CyRow crr: matchedNRows) {
							crr.set("selected", true);
						}
					}
				}
				
			}
			
			/// selecting found 1st neighboring proteins for the initially selected set of proteins
			Set<CyEdge> ppEdgeSet = new HashSet<CyEdge>(ppEdge);
			System.out.println("Find matching proteins in "+ ppEdgeSet.size() + " edges");
			for (CyEdge ce: ppEdgeSet) {
				Collection <CyRow> matchedERows = network.getDefaultEdgeTable().getMatchingRows("SUID", ce.getSUID());
				for (CyRow cr: matchedERows) {
					cr.set("selected", true);
				}
			}
			System.out.println("Identified "+ setNeighP.size() + " neighboring proteins");
			
//			logger.info("The set of neighboring proteins including seeds to start brokerage score calculations: " + setNeighP);
//			ModulFunctions.compute_brokerage(network, setNeighP);
						
		}
		
		
		else if (!ppi && pgd) {
			
			if (!clpsPG) {
//				Set<CyNode> setNeighP = selCyNodes;
				setNeighP = selCyNodes;
				Set <CyEdge> neighbEdges = new HashSet<CyEdge> ();
				Set <CyEdge> neighbDEdges = new HashSet<CyEdge> ();
				// endNode: Set of genes , neighbEdges are prot-gene edges, neighbDEdges are gene-disease edges
				Set<CyNode> endNode = ModulFunctions.protsGenesDis(network, setNeighP, neighbEdges, neighbDEdges);
				
				for (CyEdge ce: neighbDEdges) {
					Collection <CyRow> matchedERows = network.getDefaultEdgeTable().getMatchingRows("SUID", ce.getSUID());
					for (CyRow cr: matchedERows) {
						cr.set("selected", true);
					}
				}
				
				for (CyEdge ce: neighbEdges) {
					Collection <CyRow> matchedERows = network.getDefaultEdgeTable().getMatchingRows("SUID", ce.getSUID());
					for (CyRow cr: matchedERows) {
						cr.set("selected", true);
					}
				}
				
				List<CyEdge> ppEdge = new ArrayList<CyEdge>();
				for (CyNode n1: setNeighP) {
					for (CyNode n2: setNeighP) {
						// to exclude self edges
						if (n1 != n2) {
							ppEdge.addAll(network.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY));
						}					
					}			
				}
				Set<CyEdge> ppEdgeSet = new HashSet<CyEdge>(ppEdge);			
				for (CyEdge ce: ppEdgeSet) {
					Collection <CyRow> matchedERows = network.getDefaultEdgeTable().getMatchingRows("SUID", ce.getSUID());
					for (CyRow cr: matchedERows) {
						cr.set("selected", true);
					}
				}
			}
			
			else if (clpsPG) {
//				Set<CyNode> setNeighP = selCyNodes;
				setNeighP = selCyNodes;
				for (CyNode n: setNeighP) {
					sPtDmap.put(nodeTable.getRow(n.getSUID()).get("name", String.class), new HashSet<String>());
					sPtDCyNodemap.put(n, new HashSet<CyNode>());
				}
				/*Set<List<String>> sourcePtargetDset = new HashSet<List<String>>();
				for (CyNode n: setNeighP) {
					//nodeTable.getRow(n.getSUID()).get("name", String.class);
					List<String> stEdge = new ArrayList<String>(2);
					stEdge.add(nodeTable.getRow(n.getSUID()).get("name", String.class));
					sourcePtargetDset.add(stEdge);
				}*/
				//logger.info("The set of 1st neighboring proteins before function protsDis:" + setNeighP);
				endDNode = ModulFunctions.protsDis(network, setNeighP, sPtDmap, sPtDCyNodemap);
				
//				logger.info("The list of protein-disorder edges to be added:" + sPtDmap);
//				logger.info("The set of disorder nodes to be added:" + endDNode);

				for (CyNode n : endDNode) {
					Collection <CyRow> matchedNRows = network.getDefaultNodeTable().getMatchingRows("SUID", n.getSUID());
					for (CyRow crr: matchedNRows) {
						crr.set("selected", true);
					}
				}
				
				List<CyEdge> ppEdge = new ArrayList<CyEdge>();
				for (CyNode n1: setNeighP) {
					for (CyNode n2: setNeighP) {
						// to exclude self edges
						if (n1 != n2) {
							ppEdge.addAll(network.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY));
						}					
					}			
				}
				
				Set<CyEdge> ppEdgeSet = new HashSet<CyEdge>(ppEdge);			
				for (CyEdge ce: ppEdgeSet) {
					Collection <CyRow> matchedERows = network.getDefaultEdgeTable().getMatchingRows("SUID", ce.getSUID());
					for (CyRow cr: matchedERows) {
						cr.set("selected", true);
					}
				}
	
			}
			
		}
		
		String netSuid = network.getSUID().toString();
		
		String newNetName = new String();
		if (!set_net_name) {		
			String  netName = "Neighbor_module";
			newNetName = namingUtil.getSuggestedNetworkTitle(netName);
		}
		else if (set_net_name) {
			newNetName = namingUtil.getSuggestedNetworkTitle(new_net_name);
		}
		
	////// creating a network from selected nodes & edges, by command:			
		Map<String, Object> args = new HashMap<>();
		//network create edgeList=selected, nodeList=selected, source=current
		args.put("edgeList", "selected");
		args.put("nodeList", "selected");
		//excludeEdges: Unless this is set to true, edges that connect nodes in the nodeList are implicitly included
		args.put("excludeEdges", true);
		args.put("source", "current");
		args.put("networkName", newNetName);
		CommandExecuter cmdex = new CommandExecuter(app);
		cmdex.executeCommand("network", "create", args, null);
		
		if (ppi) {
			System.out.println("Computing SPD");
			ModulFunctions.compute_brokerage(network, ModelUtil.getNetworkWithName(app, newNetName), setNeighP);
			System.out.println("Computed SPD");
		}
		insertTasksAfterCurrentTask(new CreateNewEdgesTask(app, sPtDmap, sPtDCyNodemap, netSuid, newNetName));
		insertTasksAfterCurrentTask(new DeselectAll(app, network));
		
	}

}
