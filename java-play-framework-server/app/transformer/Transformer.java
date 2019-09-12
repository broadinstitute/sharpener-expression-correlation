package transformer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import apimodels.Attribute;
import apimodels.GeneInfo;
import apimodels.GeneInfoIdentifiers;
import apimodels.Parameter;
import apimodels.Property;
import apimodels.TransformerInfo;
import apimodels.TransformerQuery;

public class Transformer {

	private static final String NAME   = "Big GIM expression correlation";
	private static final String TOTAL  = "total";
	private static final String TISSUE = "tissue";
	private static final String DEFAUL_TOTAL  = "100";
	private static final String DEFAUL_TISSUE = "pancreas";

	private static HashMap<String,ArrayList<GeneInfo>> geneSets = new HashMap<String,ArrayList<GeneInfo>>();

	public static TransformerInfo transformerInfo() {

		TransformerInfo transformerInfo = new TransformerInfo().name(NAME);
		transformerInfo.function(TransformerInfo.FunctionEnum.EXPANDER);
		transformerInfo.description("Gene expression correlation with BigGIM");
		transformerInfo.addParametersItem(
				new Parameter()
					.name(TISSUE)
					.type(Parameter.TypeEnum.STRING)
					._default(DEFAUL_TISSUE)
					.addAllowedValuesItem("adipose_tissue")
					.addAllowedValuesItem("brain")
					.addAllowedValuesItem("liver")
					.addAllowedValuesItem("muscleSkeletal_muscle")
					.addAllowedValuesItem("pancreas")
					.addAllowedValuesItem("blood_platelet")
					.addAllowedValuesItem("cardiac_muscle")
					.addAllowedValuesItem("heart")
				);
		transformerInfo.addParametersItem(
				new Parameter()
					.name(TOTAL)
					.type(Parameter.TypeEnum.INT)
					._default(DEFAUL_TOTAL)
					.suggestedValues("from 10 to 1000")
				);

		transformerInfo.addRequiredAttributesItem("identifiers.entrez");
		return transformerInfo;
	}


	public static List<GeneInfo> produceGeneSet(final TransformerQuery query) {

		String myTOTAL  = DEFAUL_TOTAL;
		String myTISSUE = DEFAUL_TISSUE;
		for (Property property : query.getControls()) {
			if (TOTAL.equals(property.getName())) {
				myTOTAL = property.getValue();
			}
			if (TISSUE.equals(property.getName())) {
				myTISSUE = property.getValue();
			}
		}
		
		StringBuilder myGENES = new StringBuilder("");
		ArrayList<GeneInfo> genes = new ArrayList<GeneInfo>();
		for (GeneInfo geneInfo : query.getGenes()) {
			if(geneInfo.getIdentifiers() != null && geneInfo.getIdentifiers().getEntrez() != null) {
				myGENES.append(myGENES.toString().equals("") ? "" : ",").append(geneInfo.getIdentifiers().getEntrez());
			}
		}

		Runtime rt = Runtime.getRuntime();

		String[] commands = {"perl", "scripts/runBigGIM_expander.pl", myTOTAL, myTISSUE, myGENES.toString()};

		try {
			Process proc = rt.exec(commands);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

			//Read the gene list from expander
			String s;
			while ((s = stdInput.readLine()) != null) {
				String geneId = "NCBIGene:" + s;
				GeneInfo gene = new GeneInfo().geneId(geneId);
				gene.addAttributesItem(new Attribute().name("source").value(NAME).source(NAME));
				gene.setIdentifiers(new GeneInfoIdentifiers().entrez(geneId));
				genes.add(gene); 
			}

			//Print any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				System.err.println(s);
			}
		}
		catch(Exception e) {
			System.err.println(e.toString()); 
		}

		return genes;
	}

}
