import matplotlib
matplotlib.use("agg")
import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
import numpy as np

def load(runtimes, k):
    for alg, r in zip(["ges\n10k pairs", "wl-fv-2\n100 complexes", "wl-sim-2\n10k pairs", "wl-fv-3\n100 complexes", "wl-sim-3\n10k pairs"], runtimes):
        d = pd.read_table(r, names=["runtime"])
        d["alg"] = alg.upper()
        d["run"] = k + 1
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
sns.violinplot(x="alg", y="runtime", hue="run", data=d)
ax = plt.gca()
ax.set_yticklabels(["$10^{{{:.0f}}}$".format(y) for y in ax.get_yticks()])
plt.tick_params(axis="both", labelsize=10)
plt.xlabel("                       2 iterations                   3 iterations",   fontsize=14)
plt.ylabel("Runtime [ns]", fontsize=14)
sns.despine()
plt.savefig(snakemake.output[0], bbox_inches="tight")
