 # -*- coding: utf-8 -*-
from os.path import dirname, abspath
from math import sqrt

from collections import defaultdict
import numpy as np

#import matplotlib
#matplotlib.use('Agg') # Must be before importing matplotlib.pyplot or pylab! Needed for calculations on server.
import matplotlib.pyplot as plt
import matplotlib.colors as mcolors
import seaborn as sb
#from pandas import DataFrame

sb.set_style("ticks")
sb.set_color_codes()


def read_values(path):
    """ Read the times.
    """
    user_sys_times, tooltimes1, tooltimes2, tooltimes3 = defaultdict(lambda: defaultdict(list)), defaultdict(lambda: defaultdict(list)), defaultdict(lambda: defaultdict(list)), defaultdict(lambda: defaultdict(list))
    with open(path, "r") as f:
        next(f) # skip header
        for line in f:
            line = line.strip("\n").split(";")
            if len(line) == 9:
                algo, thresh, run, usertime, systime, cpu, wallclocktime, tooltime1, tooltime2 = line
            elif len(line) ==10:
                algo, thresh, run, usertime, systime, cpu, wallclocktime, tooltime1, tooltime2, tooltime3 = line
                tooltimes3[algo][int(run)].append(float(tooltime3))
            tooltimes1[algo][int(run)].append(float(tooltime1))
            tooltimes2[algo][int(run)].append(float(tooltime2))
            user_sys_times[algo][int(run)].append(float(usertime)+float(systime))



    return (user_sys_times, tooltimes1, tooltimes2, tooltimes3)


def label(i, label):
    """ Set label only in run 0
    """
    if i == 0:
        return label
    else:
        return ""


def plot_stacked_bars(tooltimes1, tooltimes2, tooltimes3, querysize, cores):
    """
    """
    N = 5
    algos =["wl_minhash", "wl_linear", "ges"]


    labels_0 = ["FV", "index", "queries"]
    labels_1 = ["FV", "queries"]
    labels_2 = ["filter", "verification"]
    labels = [labels_0, labels_1, labels_2]

    color1 = ('purple','darkgreen','b')
    color2 = ('m', 'g', 'c')

    ind = np.arange(N)    # the x locations for the groups
    width = 1/13          # the width of the bars: can also be len(x) sequence

    fig, ax = plt.subplots()

    for j, algo in enumerate(algos):   #algos
        for i in (0,1,2):   #runs
            rects1 = ax.bar(ind + (i + 3 * j) * width + j * width, tooltimes1[algo][i], width, color=color1[j], linewidth=1, edgecolor="k", label=label(i, algo+": "+labels[j][0]))
            rects1_2 = ax.bar(ind + (i + 3 * j) * width + j * width, tooltimes2[algo][i], width, color=color2[j], bottom=tooltimes1[algo][i], linewidth=1, edgecolor="k", label=label(i, algo+": "+labels[j][1]))
            if algo == "wl_minhash":
                new_bottom = [t1+t2 for t1,t2 in zip(tooltimes1[algo][i], tooltimes2[algo][i])]
                rects1_3 = ax.bar(ind + (i + 3 * j) * width + j * width, tooltimes3[algo][i], width, color="pink", bottom=new_bottom, linewidth=1, edgecolor="k", label=label(i, algo+": "+labels[j][2]))

    ax.legend(loc=1, bbox_to_anchor=(1.25, 1.15), fontsize=7, frameon=False, markerscale=0.8)
    ax.set_xticks(ind + width * 5)
    ax.set_xticklabels(('0.5', '0.6', '0.7', '0.8', '0.9'))

    plt.xlabel("Similarity threshold")
    plt.ylabel("Runnig time (minutes)")
    plt.title("times measured by tools\n{} cores per job, querysize {}".format(cores, querysize))
    if cores==12:
        if querysize ==100:
            plt.ylim(0.1,15)
        else:
            plt.ylim(0.1,75)
    else:
        if querysize ==100:
            plt.ylim(0.1,65)
        else:
            plt.ylim(0.1,300)

    plt.yscale("log")

    sb.despine()

    plt.savefig("barplot_tooltimes_{}_cores_{}_queries_log.pdf".format(cores, querysize), bbox_inches = "tight")
    #plt.show()


def plot_stacked_bars_sys(user_sys_times, querysize, cores):
    """
    """
    N = 5
    algos =["wl_minhash", "wl_linear", "ges"]


    #labels = ["usertime", "systime"]


    color1 = ('purple','darkgreen','b')
    color2 = ('m', 'g', 'c')

    ind = np.arange(N)    # the x locations for the groups
    width = 1/13          # the width of the bars: can also be len(x) sequence

    fig, ax = plt.subplots()

    for j, algo in enumerate(algos):   #algos
        for i in (0,1,2):   #runs
            rects1 = ax.bar(ind + (i + 3 * j) * width + j * width, user_sys_times[algo][i], width, color=color1[j], linewidth=1, edgecolor="k", label=label(i, algo))
            #rects1_2 = ax.bar(ind + (i + 3 * j) * width + j * width, systimes[algo][i], width, color=color2[j], bottom=usertime[algo][i], linewidth=1, edgecolor="k", label=algo+": "+labels[1])


    ax.legend(loc=1, bbox_to_anchor=(1.13, 1.15), fontsize=7, frameon=False)
    ax.set_xticks(ind + width * 5)
    ax.set_xticklabels(('0.5', '0.6', '0.7', '0.8', '0.9'))

    plt.xlabel("Similarity threshold")
    plt.ylabel("Runnig time (minutes)")
    plt.title("user+systime\n{} cores per job, querysize {}".format(cores, querysize))
    if cores == 12:
        if querysize ==100:
            plt.ylim(0.1,50)
        else:
            plt.ylim(0.1,250)
    else:
        if querysize ==100:
            plt.ylim(0.1,65)
        else:
            plt.ylim(0.1,300)

    plt.yscale("log")

    sb.despine()

    plt.savefig("barplot_user+systime_{}_cores_{}_queries_log.pdf".format(cores, querysize))
    #plt.show()


if __name__ == "__main__":

    #querysize = [100,500]
    #cores =12
    #for q in querysize:
        #path = "times_{}_12cores.csv".format(q)
        #(user_sys_times, tooltimes1, tooltimes2, tooltimes3) = read_values(path)
        ##print(tooltimes1["wl_linear"])

        #plot_stacked_bars(tooltimes1, tooltimes2, tooltimes3, q, cores)

        #plot_stacked_bars_sys(user_sys_times, q, cores)


    q =500
    cores=1
    #path = "times_100_1core.csv"
    path="../database_search_1_cores/times_500.csv"
    (user_sys_times, tooltimes1, tooltimes2, tooltimes3) = read_values(path)
    #print(tooltimes1["wl_linear"])

    plot_stacked_bars(tooltimes1, tooltimes2, tooltimes3, q, cores)
    plot_stacked_bars_sys(user_sys_times, q, cores)

