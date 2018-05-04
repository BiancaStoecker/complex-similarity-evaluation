import pandas as pd

def load(runtimes, k):
    for alg, r in zip(["ged", "wl-fv", "wl-sim"], runtimes):
        d = pd.read_table(f, names=["runtime"])
        d["alg"] = alg
        d["k"] = k
        yield d

def load_all():
    for k, runtimes in zip(range(3), snakemake.input):
        yield from load(runtimes, k)


d = pd.concat(list(load_all()))

# TODO group by k
# run SD and mean on it

d.groupby()
