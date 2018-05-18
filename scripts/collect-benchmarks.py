import matplotlib
matplotlib.use("agg")
import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
import numpy as np

def load(runtimes, k):
    for alg, r in zip(["ged", "wl-fv", "wl-sim"], runtimes):
        d = pd.read_table(r, names=["runtime"])
        d["alg"] = alg
        d["k"] = k
        yield d

def load_all():
    for k in range(3):
        runtimes = snakemake.input.get("k{}".format(k))
        yield from load(runtimes, k)


d = pd.concat(list(load_all()))
d.loc[:, "runtime"] = np.log10(d["runtime"])

# TODO group by k
# run SD and mean on it

sns.set_style("ticks")
sns.violinplot(x="alg", y="runtime", hue="k", data=d, )
ax = plt.gca()
ax.set_yticklabels(["$10^{{{:.0f}}}$".format(y) for y in ax.get_yticks()])

plt.xlabel("algorithm")

plt.savefig(snakemake.output[0], bbox_inches="tight")
