import os
import pandas as pd

complexes = pd.read_table(snakemake.input[0], usecols=[0], squeeze=True)

subsample = complexes.sample(n=snakemake.params.n, random_state=snakemake.params.seed)

with open(snakemake.output[0], "w") as outgml:
    for f in subsample:
        with open(os.path.join("unique_complexes", f + ".gml")) as f:
            outgml.write(f.read())
