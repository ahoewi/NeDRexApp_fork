package org.cytoscape.nedrex.internal;

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class CreateVisStyleAction extends AbstractCyAction{	
	private RepoApplication app;
	private Logger logger = LoggerFactory.getLogger(getClass());	
	
	public CreateVisStyleAction (RepoApplication app) {
		super("Create NeDRex visual style");
		setPreferredMenu("Apps.NeDRex");
		setMenuGravity(21.0f);
		insertSeparatorBefore();
		this.app = app;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		VisualMappingManager vmmServiceRef = app.getActivator().getService(VisualMappingManager.class);
		VisualStyleFactory visualStyleFactoryServiceRef = app.getActivator().getService(VisualStyleFactory.class);
//		VisualMappingFunctionFactory vmfFactoryC = app.getActivator().getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		VisualMappingFunctionFactory vmfFactoryD = app.getActivator().getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory vmfFactoryP = app.getActivator().getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
		
		// If the style already existed, don't recreate it
		Boolean visStyle_exist = false;
		Iterator it = vmmServiceRef.getAllVisualStyles().iterator();
		while (it.hasNext()){
			VisualStyle curVS = (VisualStyle)it.next();
			if (curVS.getTitle().equalsIgnoreCase("NeDRex"))
			{
				vmmServiceRef.setCurrentVisualStyle(curVS);
				visStyle_exist = true;
				break;
			}
		}
		if(!visStyle_exist) {
			// Create a new Visual style
			VisualStyle vs= visualStyleFactoryServiceRef.createVisualStyle("NeDRex");
			vmmServiceRef.addVisualStyle(vs);
			
			String nodeLabel = new String();
			nodeLabel = "displayName";
			String nodeType = new String();
			nodeType = "type";

			// Pass-through mapping
			PassthroughMapping pMapping = (PassthroughMapping) vmfFactoryP.createVisualMappingFunction(nodeLabel, String.class, BasicVisualLexicon.NODE_LABEL);	
			// DiscreteMapping - Set node shape and node color based on attribute value (type)
			DiscreteMapping dNShapeMapping = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction(nodeType, String.class, BasicVisualLexicon.NODE_SHAPE);
			DiscreteMapping dMapping = (DiscreteMapping) vmfFactoryD.createVisualMappingFunction(nodeType, String.class, BasicVisualLexicon.NODE_FILL_COLOR);


			// If attribute value is "disorder", map the nodeFillColor to redish...
			String key = NodeType.Disease.toString();
//			dMapping.putMapValue(key, new Color(250, 190, 190, 200));
			
//			dMapping.putMapValue(key, new Color(255, 102, 102, 255));
			dMapping.putMapValue(key, new Color(255, 51, 204, 255));
			dNShapeMapping.putMapValue(key, NodeShapeVisualProperty.DIAMOND);
			
			key = NodeType.Gene.toString();
//			dMapping.putMapValue(key, new Color(0, 204, 102, 255));
			dMapping.putMapValue(key, new Color(0, 204, 204, 155));
			dNShapeMapping.putMapValue(key, NodeShapeVisualProperty.ROUND_RECTANGLE);

			key = NodeType.Protein.toString();
			dMapping.putMapValue(key, new Color(0, 130, 200, 255));
			dNShapeMapping.putMapValue(key, NodeShapeVisualProperty.ELLIPSE);

			key = NodeType.Pathway.toString();
			dMapping.putMapValue(key, new Color(255, 153, 51, 255));
			dNShapeMapping.putMapValue(key, NodeShapeVisualProperty.ELLIPSE);

			key = NodeType.Drug.toString();
//			dMapping.putMapValue(key, new Color(145, 30, 180, 100));
			dMapping.putMapValue(key, new Color(214, 170, 227, 255));
			dNShapeMapping.putMapValue(key, NodeShapeVisualProperty.HEXAGON);
					
			vs.addVisualMappingFunction(pMapping);			
			vs.addVisualMappingFunction(dMapping);
			vs.addVisualMappingFunction(dNShapeMapping);
			//					vs.addVisualMappingFunction(cMapping);
			VisualProperty<Paint> vpNC = BasicVisualLexicon.NODE_FILL_COLOR;
			//					VisualProperty<NodeShape> vAlzheimerProperty = BasicVisualLexicon.NODE_SHAPE;
			VisualProperty<Double> vBW = BasicVisualLexicon.NODE_BORDER_WIDTH;
			//					VisualProperty<Paint> vBC = BasicVisualLexicon.NODE_BORDER_PAINT;
			VisualProperty<Paint> vpESUP = BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
			VisualProperty<Double> vpNS = BasicVisualLexicon.NODE_SIZE;
			VisualProperty<Integer> vpTransP = BasicVisualLexicon.NODE_TRANSPARENCY;

			// The green color for gene as default
			// vs.setDefaultValue(vpNC, new Color(0, 158, 142, 128));
			// The redish color for disorder as default
			vs.setDefaultValue(vpNC, new Color(255, 133, 139, 255));
			// vs.setDefaultValue(vBC, new Color(0, 0, 0, 255));
			vs.setDefaultValue(vBW, 0.0);
			// vs.setDefaultValue(vAlzheimerProperty, NodeShapeVisualProperty.ELLIPSE);
			/*VisualProperty <String> vpNL = BasicVisualLexicon.NODE_LABEL;
				vs.setDefaultValue(vpNL, "Disorder");*/
			vs.setDefaultValue(vpESUP, new Color(153, 153, 153, 255));
			vs.setDefaultValue(vpNS, 35.0);
			vs.setDefaultValue(vpTransP, 200);
			//Set the visual style in the VisualMappingManager: style = vs, view = ? 
			//vmmServiceRef.setVisualStyle(style, view);
			vmmServiceRef.setCurrentVisualStyle(vs);
			
		}

		
	}

}
