from swagger_server.models.gene_info import GeneInfo
from swagger_server.models.gene_info import GeneInfoIdentifiers
from swagger_server.models.transformer_info import TransformerInfo

from swagger_server.controllers.wf8_module2 import call_biggim

import json


valid_controls = ['tissue', 'total']
control_names = {'total': 'total', 'tissue': 'tissue'}
default_control_values = {'total': 64, 'tissue': 'liver'}
default_control_types = {'total': 'int', 'tissue': 'string'}


def get_control(controls, control):
    value = controls[control_names[control]] if control_names[control] in controls else default_control_values[control]
    if default_control_types[control] == 'double':
        return float(value)
    elif default_control_types[control] == 'Boolean':
        return bool(value)
    elif default_control_types[control] == 'int':
        return int(value)
    else:
        return value


def entrez_gene_id(gene: GeneInfo):
    """
        Return value of the entrez_gene_id attribute
    """
    if (gene.identifiers is not None and gene.identifiers.entrez is not None):
        if (gene.identifiers.entrez.startswith('NCBIGene:')):
            return gene.identifiers.entrez[9:]
        else:
            return gene.identifiers.entrez
    return None


def expand(query):
    controls = {control.name:control.value for control in query.controls}
    max_number = get_control(controls, 'total')
    tissue = get_control(controls, 'tissue').split(',')
    genes = [entrez_gene_id(gene) for gene in query.genes]

    output_genes = call_biggim(genes, tissue, average_columns=True, return_genes=True, N=max_number)

    genes = {}
    gene_list = []
    for gene in query.genes:
        genes[entrez_gene_id(gene)] = gene
        gene_list.append(gene)
    for gene_id in output_genes:
        if gene_id not in genes:
            gene_entrez_id = "NCBIGene:%s" % gene_id
            gene = GeneInfo(
                gene_id = gene_entrez_id,
                identifiers = GeneInfoIdentifiers(entrez = gene_entrez_id),
                attributes=[]
                )
            genes[entrez_gene_id(gene)] = gene
            gene_list.append(gene)
    return gene_list


def expander_info():
    """
        Return information for this expander
    """
    global control_names

    with open("transformer_info.json",'r') as f:
        info = TransformerInfo.from_dict(json.loads(f.read()))
        control_names = dict((name,parameter.name) for name, parameter in zip(valid_controls, info.parameters))
        return info


