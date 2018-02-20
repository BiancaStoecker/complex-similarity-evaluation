 # -*- coding: utf-8 -*-

import sys
from os.path import dirname, abspath
import gzip
from collections import Counter


from multiprocessing.dummy import Pool # use threads
from subprocess import check_output



def calculate_edit_distance(pair):
    """ Calculation of graph edit distance for the given graphs, then
        transformation into edit similarity.
    """
    try:
        output = check_output("tools/ged/ged {} {}".format(prefix+pair[0], prefix+pair[1]), shell=True)
        n1, e1, n2, e2, edit = output.decode("utf8").split("\n")[1].split("\t")
        g_h = int(n1) + int(e1) + int(n2) + int(e2)
        edit = float(edit)
        edit_sim = (g_h - edit) / (g_h + edit)
        bin_value = int(edit_sim*10)
        if count[bin_value] < remaining_counts[bin_value]:
            count[bin_value]+=1
            return (pair[0], pair[1], edit_sim, None)
        else:
             return (pair[0], pair[1], edit_sim, "bin {} full (new value {})".format(bin_value, edit_sim))
    except Exception as e:
        return (None, None, None, e)



if __name__ == "__main__":

    number_of_processes = snakemake.threads
    max_number_of_pairs = 650000
    start = 0

    prefix = dirname(abspath(snakemake.input[0]))+"/"
    candidate_pairs_file = snakemake.input[0]

    pairs =  []
    with gzip.open(candidate_pairs_file, "rt") as f:
        for i in range(start+max_number_of_pairs):
            line = next(f)
            if i < start:
                continue
            common, c1, c2 = line.strip("\n").split("\t")
            pairs.append((c1+".gml", c2+".gml"))

    # in case you want to repeat for a later start of pairs and count forward instead of starting from scratch with more pairs
    previous_counts = Counter({4: 0, 3: 0, 2: 0, 1: 0, 5: 0, 6: 0, 8: 0, 7: 0,  9: 0})

    max_count = 1000
    remaining_counts = previous_counts.copy()
    for c in remaining_counts:
        remaining_counts[c] = max_count-previous_counts[c]
    count = Counter()

    p = Pool(number_of_processes) # specify number of concurrent processes
    with open(snakemake.output[0], "wt") as logfile:
        with open(snakemake.output[1], "wt") as overflow:
            for c1, c2, output, error in p.imap(calculate_edit_distance, pairs): # provide pairs
                if error is None:
                    output = "\t".join([c1, c2, str(output)])
                    logfile.write(output+"\n")
                else:
                    if c1 is not None:
                        output = "\t".join([c1, c2, str(output)])
                        overflow.write(output+"\n")
                    else:
                        print(error, file=sys.stderr)

    print(count)
