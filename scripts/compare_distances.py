 # -*- coding: utf-8 -*-
from os.path import dirname, abspath
from math import sqrt

import numpy as np
from scipy.stats import spearmanr, pearsonr
from scipy.spatial.distance import cosine

import matplotlib
matplotlib.use('Agg') # Must be before importing matplotlib.pyplot or pylab! Needed for calculations on server.
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
import seaborn as sb
from pandas import DataFrame

sb.set_style("ticks")
sb.set_color_codes()


def read_values(path):
    """ Read the similarity values.
    """
    pairs, edit_sim, wl0, wl1, wl2 = [], [], [], [], []
    with open(path, "r") as f:
        next(f) # skip header
        for line in f:
            c1, c2, e, w0, w1, w2 = line.strip("\n").split("\t")
            pairs.append((c1,c2))
            edit_sim.append(float(e))
            wl0.append(float(w0))
            wl1.append(float(w1))
            wl2.append(float(w2))
    return (pairs, np.array(edit_sim), np.array(wl0), np.array(wl1), np.array(wl2))


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



def plot_correlations(c, weights, correlations, output):
    """ Plot the correlation values for all weights.
    """
    fig, ax = plt.subplots()
    plt.plot(weights, correlations, marker="o", ls="")
    plt.xlabel("Weight $\mathregular{w_1}$ = 1-$\mathregular{w_0}$", fontsize=22)
    if "sqrt" in c:
        plt.ylabel("{}".format(c), fontsize=18)
    else:
        plt.ylabel("{}".format(c), fontsize=22)
    m = max(correlations)
    m_i = list(reversed(correlations)).index(m)/len(weights)
    ax.axvline(x=m_i, ymax=m, c="grey")
    zed = [tick.label.set_fontsize(18) for tick in ax.xaxis.get_major_ticks()]
    zed = [tick.label.set_fontsize(18) for tick in ax.yaxis.get_major_ticks()]
    sb.despine()
    plt.text(m_i,m+0.002, str(round(m, 3)), fontsize=15, transform=ax.transData)
    plt.text(m_i+0.05,min(correlations), "$\mathregular{w_0}$ = "+str(round((1-m_i), 2))+"\n$\mathregular{w_1}$ = "+str(round(m_i, 2)), fontsize=18, transform=ax.transData)
    plt.tight_layout()
    plt.savefig(output, dpi=300)
    plt.close()


def plot_correlations_3_iter(c, weight_triple, weights, correlations, output):
    """ Plot the correlation values for all weights.
    """
    n = len(weights)
    wt = np.array(weight_triple)*100
    data = np.ones((n,n))
    mask = np.zeros((n,n))
    for index, (w0, w1, w2) in enumerate(wt):
        data[int(round(w2))][int(round(w1))] = correlations[index]
    for i in range(n):
        for j in range(n):
            if data[i][j] == 1:
                mask[i][j] = True
            else:
                mask[i][j] = False
    data= DataFrame((data))
    fig, ax = plt.subplots()
    ax = sb.heatmap(data, mask=mask,vmin=0.7, vmax=1, xticklabels=100, yticklabels=100, cmap="YlGnBu", square=True, annot=False, fmt='g',cbar=True, cbar_kws={}, rasterized=True) # "log":True
    ax.invert_yaxis()
    plt.xlabel("$\mathregular{w_1}$", fontsize=22)
    plt.ylabel("$\mathregular{w_2}$", fontsize=22)
    plt.tight_layout()
    plt.savefig(output, dpi=300)
    plt.close()




def evaluate_2_iter(weights, wl0, wl1, edit_sim, output_prefix3):
    """
    """
    correlations_pearson, correlations_spearman, correlations_cosine = [], [], []
    mean_correlation = []

    rev_w = []
    for w in weights:
        wl_sim = w*wl0 + (1-w)*wl1
        plot_wl_vs_edit((1-w), wl_sim, edit_sim, output_prefix+"{0:.2f}_wl_vs_edit.pdf".format((1-w)))

        c_pearson, p = pearsonr(wl_sim, edit_sim)
        c_spearman, p = spearmanr(wl_sim, edit_sim)
        c_cos = 1-cosine(wl_sim, edit_sim)

        correlations_pearson.append(c_pearson)
        correlations_spearman.append(c_spearman)
        correlations_cosine.append(c_cos)
        mean_correlation.append(sqrt(c_cos * c_pearson))
        rev_w.append(1-w)


    plot_correlations("Pearson correlation", rev_w, correlations_pearson, output_prefix+"correlations_pearson.pdf")
    plot_correlations("Spearmanr correlation", rev_w, correlations_spearman, output_prefix+"correlations_spearman.pdf")
    plot_correlations("Cosine similarity", rev_w, correlations_cosine, output_prefix+"correlations_cosine.pdf")
    plot_correlations("sqrt(cosine_sim * pearson_cor)", rev_w, mean_correlation, output_prefix+"correlations_mean.pdf")

    return (correlations_pearson, correlations_spearman, correlations_cosine, mean_correlation)


def evaluate_3_iter(weights, wl0, wl1, wl2, edit_sim, output_prefix):
    """
    """
    correlations_pearson, correlations_spearman, correlations_cosine = [], [], []
    mean_correlation = []

    weight_triple = []
    for w1 in weights:
        for w2 in weights:
            if w1+w2 > 1:
                continue
            w0 = 1-(w1+w2)
            weight_triple.append((w0,w1,w2))
            wl_sim = w0*wl0 + w1*wl1 + w2*wl2
            #((1-w0), wl_sim, edit_sim, output_prefix+"{0:.2f}_{1:.2f}_{2:.2f}_wl_vs_edit.pdf".format(w0, w1, w2))

            c_pearson, p = pearsonr(wl_sim, edit_sim)
            c_spearman, p = spearmanr(wl_sim, edit_sim)
            c_cos = 1-cosine(wl_sim, edit_sim)

            correlations_pearson.append(c_pearson)
            correlations_spearman.append(c_spearman)
            correlations_cosine.append(c_cos)
            mean_correlation.append(sqrt(c_cos * c_pearson))

    plot_correlations_3_iter("Pearson correlation", weight_triple, weights, correlations_pearson, output_prefix+"correlations_pearson3.pdf")
    plot_correlations_3_iter("Spearmanr correlation", weight_triple, weights, correlations_spearman, output_prefix+"correlations_spearman3.pdf")
    plot_correlations_3_iter("Cosine similarity", weight_triple, weights, correlations_cosine, output_prefix+"correlations_cosine3.pdf")
    plot_correlations_3_iter("sqrt(cosine_sim * pearson_cor)", weight_triple, weights, mean_correlation, output_prefix+"correlations_mean3.pdf")

    return (weight_triple, correlations_pearson, correlations_spearman, correlations_cosine, mean_correlation)



if __name__ == "__main__":


    distance_file = snakemake.input[0]
    output_prefix = dirname(abspath(snakemake.output[1]))+"/"
    output_prefix3 = dirname(abspath(snakemake.output[4]))+"/"

    (pairs, edit_sim, wl0, wl1, wl2) = read_values(distance_file)
    print(len(pairs), "pairs")

    weights = snakemake.params["w"]

    corr_pearson_2, corr_spearman_2, corr_cosine_2, mean_correlation_2 = evaluate_2_iter(weights, wl0, wl1, edit_sim, output_prefix)

    weight_triple, corr_pearson_3, corr_spearman_3, corr_cosine_3, mean_correlation_3 = evaluate_3_iter(weights, wl0, wl1, wl2, edit_sim, output_prefix3)


    with open(snakemake.output[0], "w") as f:
        print("stat function", "max value", "index of maximum", "weight of maximum", sep="\t", file=f)
        print("pearsonr 2iter", max(corr_pearson_2), corr_pearson_2.index(max(corr_pearson_2)), weights[corr_pearson_2.index(max(corr_pearson_2))], sep="\t", file=f)
        print("spearmanr 2iter", max(corr_spearman_2), corr_spearman_2.index(max(corr_spearman_2)), weights[corr_spearman_2.index(max(corr_spearman_2))], sep="\t", file=f)
        print("cosine similarity 2iter", max(corr_cosine_2), corr_cosine_2.index(max(corr_cosine_2)), weights[corr_cosine_2.index(max(corr_cosine_2))], sep="\t", file=f)
        print("sqrt(cosine_sim * pearson_cor) 2iter", max(mean_correlation_2), mean_correlation_2.index(max(mean_correlation_2)), weights[mean_correlation_2.index(max(mean_correlation_2))], sep="\t", file=f)

        print("pearsonr 3iter", max(corr_pearson_3), corr_pearson_3.index(max(corr_pearson_3)), weight_triple[corr_pearson_3.index(max(corr_pearson_3))], sep="\t", file=f)
        print("spearmanr 3iter", max(corr_spearman_3), corr_spearman_3.index(max(corr_spearman_3)), weight_triple[corr_spearman_3.index(max(corr_spearman_3))], sep="\t", file=f)
        print("cosine similarity 3iter", max(corr_cosine_3), corr_cosine_3.index(max(corr_cosine_3)), weight_triple[corr_cosine_3.index(max(corr_cosine_3))], sep="\t", file=f)
        print("sqrt(cosine_sim * pearson_cor) 3iter", max(mean_correlation_3), mean_correlation_3.index(max(mean_correlation_3)), weight_triple[mean_correlation_3.index(max(mean_correlation_3))], sep="\t", file=f)

