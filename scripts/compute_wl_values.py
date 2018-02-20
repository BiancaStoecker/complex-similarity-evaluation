 # -*- coding: utf-8 -*-

import sys
from os.path import dirname, abspath
from collections import Counter


from multiprocessing.dummy import Pool # use threads
from subprocess import check_output


def calculate_wl_values(pair):
    """ Calculation of Weisfeiler-Lehman values for 0 and 1 iteration for the
        given graphs.
    """
    try:
        output = check_output("java -jar tools/wljaccard/wljaccard.jar {} {}".format(prefix_gml+pair[0], prefix_gml+pair[1]), shell=True)
        wl0, wl1, _ = output.decode("utf8").strip("\n").split("\t")
        return (pair[0], pair[1], pair[2], wl0, wl1, None)
    except Exception as e:
        return (None, None, None, None, None, e)


if __name__ == "__main__":

    number_of_processes = snakemake.threads
    pairs_per_bin = 1000


    prefix_gml = dirname(abspath(snakemake.input[1]))+"/"
    pairs_file = snakemake.input[0]

    pairs =  []
    count = Counter()
    with open(pairs_file, "rt") as f:
        for line in f:
            c1, c2, edit_sim = line.strip("\n").split("\t")
            edit_sim = float(edit_sim)
            bin_value = int(edit_sim*10)
            if count[bin_value] < pairs_per_bin and bin_value != 9:
                pairs.append((c1, c2, edit_sim))
                count[bin_value]+=1
            if sum(count.values()) == 8*pairs_per_bin:
                break

    print(count)

    p = Pool(number_of_processes) # specify number of concurrent processes
    with open(snakemake.output[0], "wt") as logfile:
        logfile.write("\t".join(["c1", "c2", "edit_sim", "wl0", "wl1"])+"\n")
        for c1, c2, edit_sim, wl0, wl1, error in p.imap(calculate_wl_values, pairs): # provide pairs
            if error is None:
                output = "\t".join([c1, c2, str(edit_sim), wl0, wl1])
                logfile.write(output+"\n")
            else:
                print(error, file=sys.stderr)

