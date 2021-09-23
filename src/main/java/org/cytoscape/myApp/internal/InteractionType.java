package org.cytoscape.myApp.internal;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public enum InteractionType {

	gene_disease(NodeType.Gene, NodeType.Disease, "GeneAssociatedWithDisorder", "gene_associated_with_disorder"),
	gene_protein(NodeType.Protein, NodeType.Gene, "ProteinEncodedBy", "protein_encoded_by"),
	protein_protein(NodeType.Protein, NodeType.Protein, "ProteinInteractsWithProtein", "protein_interacts_with_protein"),
	// added the following types for new Diseasome based on shared Gene
	disease_disease(NodeType.Disease, NodeType.Disease, "DiseaseSharedGeneDisease", "disease-sharedGene-disease"),
	// added the following types for new Diseasome based on shared Drug
	disease_drug_disease(NodeType.Disease, NodeType.Disease, "DiseaseSharedDrugDisease", "disease-sharedDrug-disease"),
//	disease_is_disease(NodeType.Disease, NodeType.Disease, "DisorderIsADisorder", "disorder_is_subtype_of_disorder"),
	disease_is_disease(NodeType.Disease, NodeType.Disease, "DisorderIsSubtypeOfDisorder", "disorder_is_subtype_of_disorder"),
	protein_pathway(NodeType.Protein, NodeType.Pathway, "ProteinInPathway", "protein_in_pathway"),
	drug_protein(NodeType.Drug, NodeType.Protein, "DrugHasTarget", "drug_has_target"),
	drug_gene(NodeType.Drug, NodeType.Gene, "DrugHasGeneTarget", ""),
	gene_gene(NodeType.Gene, NodeType.Gene, "GeneSharedDiseaseGene", ""),
	protein_disease(NodeType.Protein, NodeType.Disease, "Protein_Disorder", ""),
	drug_disease(NodeType.Drug, NodeType.Disease, "DrugHasIndication", "drug_has_indication"),
	// added the following types for Drugome
	drug_indication_drug (NodeType.Drug, NodeType.Drug, "DrugSharedIndicationDrug", "drug-sharedIndication_drug"),
	drug_target_drug (NodeType.Drug, NodeType.Drug, "DrugSharedTargetDrug", "drug-sharedTarget_drug");
		

	private static Logger logger = LoggerFactory.getLogger(InteractionType.class);
	
	private NodeType source, target;
	private String name;
	private String apiname;
	private InteractionType(NodeType source, NodeType target, String name, String apiname) {
		this.source = source; this.target = target; this.name = name; this.apiname = apiname;
	}
	
	public NodeType getSourceType() {
		return source;
	}
	
	public NodeType getTargetType() {
		return target;
	}
	
	public String getAPIname() {
		return this.apiname;
	}
	
	@Override public String toString() {
		return this.name;
	}
	
	// It is not called anywhere >> to del
	/**
	 * For any edge specified return the interaction type object by querying the edge table row that contains the edge
	 * @param edge edge to determine interaction type for
	 * @return 
	 * @throws Exception if interaction type is not valid
	 */
	public static InteractionType determine(CyEdge edge) throws Exception {
		
		//String edgeTypeCol = "labelE";
		String edgeTypeCol = "type";
		
		CyNetwork network = edge.getSource().getNetworkPointer();
		if (network == null) { logger.warn("There is no network pointer!"); }
		
		CyRow row = network.getRow(edge);
		if (row == null) { logger.warn("There is no row!"); }
					
		//String interaction = row.get(CyEdge.INTERACTION, String.class);
		String interaction = row.get(edgeTypeCol, String.class);

		if (interaction.equals(InteractionType.gene_disease.toString())) { //later: NcbiGeneAssociatedWithOmimDisorderMerged
			return InteractionType.gene_disease;
		} else if (interaction.equals(InteractionType.gene_protein.toString())) { //later: NcbiGeneHasUniProtProduct
			return InteractionType.gene_protein;
		} else if (interaction.equals(InteractionType.protein_protein.toString())) { //later: ProteinProteinInteractionEdge
			return InteractionType.protein_protein;
		} else if (interaction.equals(InteractionType.disease_disease.toString())) {
			return InteractionType.disease_disease;
		} else if (interaction.equals(InteractionType.gene_gene.toString())) {
			return InteractionType.gene_gene;
		} else if(interaction.equals(InteractionType.protein_pathway.toString())) {
			return InteractionType.protein_pathway;
		} else if(interaction.equals(InteractionType.drug_protein.toString())) {
			return InteractionType.drug_protein;
		}
		else {
			logger.warn("No valid interaction type for this edge,...");
			throw new Exception("No Interaction Type for this edge!");
		}
	}

}
