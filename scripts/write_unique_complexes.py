 # -*- coding: utf-8 -*-

from collections import defaultdict
from os.path import dirname
import os

import networkx as nx

""" Given a set of simulation runs and a threshold graph (output from Tills tool
    gml2tg) for a arbitrary threshold and weight, generate one gml file with
    networkx for each unique complex = each node in the threshold graph (Tills
    tool contains an isomorphism check and all occurring nodes are unique complexes
    from the input files).
    Further write a list containing all filenames and some basic stats.
"""


def get_numbers_from_name(name):
    """ Extract the filenumer and the graphnumber from the node name
    """
    name = name.split("_")
    file_number = int(name[3][:-4]) # -> remove .gml.bak from number
    graph_number = int(name[-1])
    return(file_number, graph_number)


def parse_complexes(labels, path_input_graphs, prefix_for_output_gmls, output_file):
    """ Parse the complexes for each label and write a single gml file as well
        as some stats.
    """
    filenames_to_numbers = defaultdict(list)
    for l in labels:
        filename = "_".join(l.split("_")[:4])
        graph_number = int(l.split("_")[-1])
        filenames_to_numbers[filename].append(graph_number)

    output = open(output_file, "w")

    for filename in filenames_to_numbers:
        current_file = open(path_input_graphs+filename[:-4]+".nx.gml", "r") # .bak because of duplication for renaming, see below
        count = -1
        lines = []
        current_graphs = sorted(filenames_to_numbers[filename])
        i = 0
        current_graph = current_graphs[i]
        for line in current_file:
            if line.strip("\n") == "graph [":
                count += 1
            if count == current_graph:
                lines.append(line)
            else:
                if lines != []:
                    graph = nx.parse_gml(lines)
                    path = prefix_for_output_gmls+"{}_{}".format(filename, current_graph)
                    nx.write_gml(graph, path+".nx.gml")
                    os.system("sed '/label/d' {0}.nx.gml | sed \"s/name/label/\" > {0}.gml".format(path))
                    proteinnames = sorted(list(nx.get_node_attributes(graph,'name').values()))
                    print("{}_{}".format(filename, current_graph), graph.number_of_nodes(), graph.number_of_edges(), proteinnames, sep="\t", file=output)
                    lines = []
                    i += 1
                    if i < len(current_graphs):
                        current_graph = current_graphs[i]
                        if count == current_graph:
                            lines.append(line)
                    else:
                        break
    output.close()


if __name__ == "__main__":

    path_threshold_graph = snakemake.input[0]
    path_input_graphs = snakemake.params.input_graphs
    prefix_for_output_gmls = dirname(snakemake.output[0])+"/"
    output_file = snakemake.output[0]

    threshold_graph = nx.read_gml(path_threshold_graph)
    labels = threshold_graph.nodes(data=False) # current format output_0.005_2.5_7.gml_870 with filenumber 7 and graphnumber 870
    parse_complexes(labels, path_input_graphs, prefix_for_output_gmls, output_file)

    """ networkx does not accept multiple labels in gml format, so protein names
        are stored in the attribute "name" and the label is a unique id. The
        standard format demands them to be "label", so the following preprocessing
        is required before the tools from Till and Nils can use the gml files:
        for f in *.gml; do cp $f $f.bak; sed '/label/d' $f.bak | sed "s/name/label/" > $f; done
    """

