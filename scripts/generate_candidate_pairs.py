 # -*- coding: utf-8 -*-

from collections import defaultdict
import itertools as it
import gzip

from os.path import dirname

from multiset import Multiset

""" Iterate systematically over the ~100000 unique complexes and generate a list
    of candidate pairs for calculation of edit distance. Current criterion: at
    least one common protein, note for each pair the number of common proteins.
"""


if __name__ == "__main__":

    prefix_complexes_gml = dirname(snakemake.input[0])+"/"
    complex_list_file = snakemake.input[0]
    output =  snakemake.output[0]

    complex_list = []
    with open(complex_list_file, "r") as f:
        complex_list = [(name, int(nodes), int(edges), Multiset(labels.replace("[","").replace("]","").replace(" ","").replace("'","").split(","))) for (name, nodes, edges, labels) in [line.strip("\n").split("\t") for line in f] if int(nodes) <= 20]
    label_to_complexes = defaultdict(set)
    complex_to_proteinnames = dict()
    for complex_file, nodes, edges, proteinnames in complex_list:
        for p in proteinnames:
            label_to_complexes[p].add(complex_file)
        complex_to_proteinnames[complex_file] = proteinnames

    count = 0
    pairs = set()
    for protein in label_to_complexes:
        print(protein, len(label_to_complexes[protein]), count, flush=True)
        for (c1, c2) in it.combinations(sorted(label_to_complexes[protein]), 2):
            l1 = complex_to_proteinnames[c1]
            l2 = complex_to_proteinnames[c2]
            if abs(len(l1)-len(l2)) > 10:
                continue
            common = len(l1.intersection(l2))
            if (common, c1, c2) not in pairs:
                count +=1
                pairs.add((common, c1, c2))

    with gzip.open(output, "wt") as output_file:
        for (common, c1, c2) in sorted(list(pairs), reverse=True):
            print(common, c1, c2, sep="\t", file=output_file)

    print(count)
