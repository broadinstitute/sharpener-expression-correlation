import json
import sys
from pprint import pprint
from wf8_module1 import doid_to_genes_and_tissues
from wf8_module2 import call_biggim
import numpy as np
import pandas as pd
import time

maxNumber  = int(sys.argv[1])
tissues    = sys.argv[2].split(',')
inputGenes = sys.argv[3].split(',')

outputGenes = call_biggim(inputGenes, tissues, average_columns=True, return_genes=True, N=maxNumber)
print(outputGenes)
