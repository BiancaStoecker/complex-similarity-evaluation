 # -*- coding: utf-8 -*-
from os.path import dirname, abspath
from math import sqrt

import numpy as np
from scipy.stats import spearmanr, pearsonr
from scipy.spatial.distance import cosine

import matplotlib
matplotlib.use('Agg') # Must be before importing matplotlib.pyplot or pylab! Needed for calculations on server.
import matplotlib.pyplot as plt
import seaborn as sb

sb.set_style("ticks")
sb.set_color_codes()


def read_values(path):
    """ Read the similarity values.
    """
    pairs, edit_sim, wl0, wl1 = [], [], [], []
    with open(path, "r") as f:
        next(f) # skip header
        for line in f:
            c1, c2, e, w0, w1 = line.strip("\n").split("\t")
            pairs.append((c1,c2))
            edit_sim.append(float(e))
            wl0.append(float(w0))
            wl1.append(float(w1))
    return (pairs, np.array(edit_sim), np.array(wl0), np.array(wl1))


def cos_similarity(a1, a2):
    """ Return cosine similarity = 1-cosine distance of arrays a1 and a2.
    """
    return 1-cosine(a1, a2)


def plot_wl_vs_edit(w, wl_sim, edit_sim, output):
    """ Plot the joint plot of wl similarity vs. edit similarity as scatter plot
        with histograms on marginal axes for given weight w.
        Calculate and return pearson and spearman correlation as well as cosine
        similarity.
    """
    g = sb.jointplot(wl_sim, edit_sim, kind="reg", stat_func=None, joint_kws={"scatter_kws":{"alpha":0.5, "s":20, "edgecolors":"none"}})
    g.set_axis_labels("WL similarity", "Edit similarity", fontsize=22)
    #g.fig.suptitle("w={}, {} pairs".format(w, len(wl_sim)), fontsize=21)
    sb.despine()
    plt.xlim(0, 1.01)
    plt.ylim(0,1.01)
    plt.plot(list(range(0,100)),list(range(0,100)), c="orange")
    plt.tick_params(axis="both", labelsize=18)
    plt.tight_layout()
    plt.savefig(output, dpi=300)
    plt.close()
    c_pearson, p = pearsonr(wl_sim, edit_sim)
    c_spearman, p = spearmanr(wl_sim, edit_sim)
    c_cos = 1-cosine(wl_sim, edit_sim)

    return (c_pearson, c_spearman, c_cos)


def plot_correlations(c, weights, correlations, output):
    """ Plot the correlation values for all weights.
    """
    fig, ax = plt.subplots()
    plt.plot(weights, correlations, marker="o", ls="")
    plt.xlabel("Weight", fontsize=22)
    if "sqrt" in c:
        plt.ylabel("{}".format(c), fontsize=18)
    else:
        plt.ylabel("{}".format(c), fontsize=22)
    m = max(correlations)
    m_i = correlations.index(m)/len(weights)
    ax.axvline(x=m_i, ymax=m, c="grey")
    zed = [tick.label.set_fontsize(18) for tick in ax.xaxis.get_major_ticks()]
    zed = [tick.label.set_fontsize(18) for tick in ax.yaxis.get_major_ticks()]
    sb.despine()
    plt.tight_layout()
    plt.savefig(output, dpi=300)
    plt.close()


if __name__ == "__main__":


    distance_file = snakemake.input[0]
    output_prefix = dirname(abspath(snakemake.output[1]))+"/"

    (pairs, edit_sim, wl0, wl1) = read_values(distance_file)
    print(len(pairs), "pairs")

    weights = snakemake.params["w"]

    correlations_pearson, correlations_spearman, correlations_cosine = [], [], []
    mean_correlation = []


    for w in weights:
        wl_sim = w*wl0 + (1-w)*wl1
        (c_pearson, c_spearman, c_cos) = plot_wl_vs_edit(w, wl_sim, edit_sim, output_prefix+"{0:.2f}_wl_vs_edit.pdf".format(w))
        correlations_pearson.append(c_pearson)
        correlations_spearman.append(c_spearman)
        correlations_cosine.append(c_cos)
        mean_correlation.append(sqrt(c_cos * c_pearson))

    plot_correlations("Pearson correlation", weights, correlations_pearson, output_prefix+"correlations_pearson.pdf")
    plot_correlations("Spearmanr correlation", weights, correlations_spearman, output_prefix+"correlations_spearman.pdf")
    plot_correlations("Cosine similarity", weights, correlations_cosine, output_prefix+"correlations_cosine.pdf")
    plot_correlations("sqrt(cosine_sim * pearson_cor)", weights, mean_correlation, output_prefix+"correlations_mean.pdf")

    with open(snakemake.output[0], "w") as f:
        print("stat function", "max value", "index of maximum", "weight of maximum", sep="\t", file=f)
        print("pearsonr", max(correlations_pearson), correlations_pearson.index(max(correlations_pearson)), weights[correlations_pearson.index(max(correlations_pearson))], sep="\t", file=f)
        print("spearmanr", max(correlations_spearman), correlations_spearman.index(max(correlations_spearman)), weights[correlations_spearman.index(max(correlations_spearman))], sep="\t", file=f)
        print("cosine similarity", max(correlations_cosine), correlations_cosine.index(max(correlations_cosine)), weights[correlations_cosine.index(max(correlations_cosine))], sep="\t", file=f)
        print("sqrt(cosine_sim * pearson_cor)", max(mean_correlation), mean_correlation.index(max(mean_correlation)), weights[mean_correlation.index(max(mean_correlation))], sep="\t", file=f)
