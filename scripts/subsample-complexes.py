import os
import pandas as pd

complexes = pd.read_table(snakemake.input[0], usecols=[0], squeeze=True)

for i, ni in enumerate(snakemake.params.n):
    subsample = complexes.sample(n=ni, random_state=snakemake.params.seed)

    with open(snakemake.output[i], "w") as outgml:
        for f in subsample:
            with open(os.path.join("unique_complexes", f + ".gml")) as f:
                outgml.write(f.read())
