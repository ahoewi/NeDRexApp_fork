package org.cytoscape.myApp.internal.utils;

import java.awt.Color;
import java.awt.Paint;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.myApp.internal.RepoApplication;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class ViewUtils {
	
	public static String MDF = "mean_dif_exp";
	//private static Logger logger = LoggerFactory.getLogger(getClass());
	
	public static VisualStyle createBiconStyle(RepoApplication app, CyNetwork network) {
		
		String styleName = network.getRow(network).get(CyNetwork.NAME, String.class);

		VisualMappingManager vmm = app.getActivator().getService(VisualMappingManager.class);
		for (VisualStyle style: vmm.getAllVisualStyles()) {
			if (style.getTitle().equals(styleName)) {
				return style;
			}
		}

		VisualStyleFactory vsf = app.getActivator().getService(VisualStyleFactory.class);
		
		VisualStyle biconStyle = vsf.createVisualStyle(styleName);

		// Set the default node size
		biconStyle.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, 35.0);
		biconStyle.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, 35.0);

		// Set the shape to an ellipse
		biconStyle.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);

		// Set the color to white
		biconStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.GRAY);

		// Set the edge color to blue
		biconStyle.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(31,41,61));
		
		// Set edge width
		biconStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 2.0);
		
		// Set the label color to black
		biconStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, Color.BLACK);

		// Set the node border width to zero
		biconStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 0.0);

		// Set the label color to black
		biconStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 10);

		// Lock node width and height
		for(VisualPropertyDependency<?> vpd: biconStyle.getAllVisualPropertyDependencies()) {
			if (vpd.getIdString().equals("nodeSizeLocked"))
				vpd.setDependency(false);
		}

		// Get all of the factories we'll need
		VisualMappingFunctionFactory continuousFactory = app.getActivator().getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		//VisualMappingFunctionFactory discreteFactory = app.getActivator().getService(VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory passthroughFactory = app.getActivator().getService(VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
		//VisualLexicon lex = app.getActivator().getService(RenderingEngineManager.class).getDefaultVisualLexicon();
		
		
		ContinuousMapping<Double, Paint> cMapping = (ContinuousMapping<Double, Paint>) continuousFactory.createVisualMappingFunction(MDF, Double.class, BasicVisualLexicon.NODE_FILL_COLOR);		
		Set <Double> mdfValues = new HashSet <Double> (network.getDefaultNodeTable().getColumn(MDF).getValues(Double.class));
		Double valmin = Collections.min(mdfValues);
		Double valminRnd = BigDecimal.valueOf(valmin).setScale(6, RoundingMode.HALF_DOWN).doubleValue();
		//valminRnd = -1.40D;
		//Double val1 = 2d;
		//BoundaryRangeValues<Paint> brv1 = new BoundaryRangeValues<Paint>(Color.RED, Color.GREEN, Color.PINK);
		//BoundaryRangeValues<Paint> brv1 = new BoundaryRangeValues<Paint>(Color.BLUE, Color.BLUE, Color.BLUE);
		BoundaryRangeValues<Paint> brv1 = new BoundaryRangeValues<Paint>(Color.BLUE, new Color(67, 147, 195), new Color(67, 147, 195));
		
		BoundaryRangeValues<Paint> brv2 = new BoundaryRangeValues<Paint>(new Color(247, 247, 247), new Color(247, 247, 247), new Color(247, 247, 247));
		
		Double valmax = Collections.max(mdfValues);
		Double valmaxRnd = BigDecimal.valueOf(valmax).setScale(6, RoundingMode.HALF_UP).doubleValue();
		//valmaxRnd = 1.96D;
		//Double val2 = 12d;
		BoundaryRangeValues<Paint> brv3 = new BoundaryRangeValues<Paint>(new Color(214, 96, 77), new Color(214, 96, 77), Color.RED);		
		//Double valmid = (valminRnd+valmaxRnd)/2d;
		cMapping.addPoint(valminRnd, brv1);
		cMapping.addPoint(0.0d, brv2);
		cMapping.addPoint(valmaxRnd, brv3);
		
		biconStyle.addVisualMappingFunction(cMapping);
		
		
		PassthroughMapping<String, String> pMapping = (PassthroughMapping<String, String>) passthroughFactory.createVisualMappingFunction("name", String.class, BasicVisualLexicon.NODE_LABEL);
		biconStyle.addVisualMappingFunction(pMapping);

		vmm.addVisualStyle(biconStyle);
		
		return biconStyle;
	}

}
