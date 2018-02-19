import numpy as np

shell.executable("bash")

configfile: "config.yaml"

weights = np.arange(0.0,1.01, 0.01)

rule all:
    input:
        "tables/max_correlations.tsv",
        "plots/correlations_cosine.pdf",
        "plots/correlations_pearson.pdf"


rule compute_threshold_graph:
    conda:
        "envs/java.yaml"
    params:
        w0 = config["weight_threshold_graph"],
        w1 =  int(100-float(config["weight_threshold_graph"])),
        t = config["min_thresh"]
    input:
        config["input_graphs"]
    output:
        expand("threshold_graph/threshold_graph_{w0}_{t}.gml", w0=config["weight_threshold_graph"], t= config["min_thresh"])

    shell:
        "export out_dir=`dirname \"{output}\"` ;"
        "java -jar ../tools/gml2tg/gml2tg-90c77e.jar --in {input} --out $out_dir --thresh {params.t} --wl0weight {params.w0} --wl1weight {params.w1} ;"
        "mv \"${{out_dir}}/001/thresholdGraph_{params.t}.gml\" {output} ;"
        "rm -r \"${{out_dir}}/001/\""


rule write_unique_complexes:
    conda:
        "envs/graph-analysis.yaml"
    params:
        w0= config["weight_threshold_graph"],
        w1 =  int(100-float(config["weight_threshold_graph"])),
        t = config["min_thresh"],
        input_graphs = config["input_graphs_nx"]
    input:
        expand("threshold_graph/threshold_graph_{w0}_{t}.gml", w0=config["weight_threshold_graph"], t= config["min_thresh"])
    output:
        "unique_complexes/unique_complexes.tsv"
    script:
        "scripts/write_unique_complexes.py"


rule generate_candidate_pairs:
    conda:
        "envs/graph-analysis.yaml"
    input:
        "unique_complexes/unique_complexes.tsv"
    output:
        "unique_complexes/sorted_unique_candidate_pairs.tsv.gz"
    script:
        "scripts/generate_candidate_pairs.py"


rule calculate_edit_similarities:
    conda:
        "envs/java.yaml"
    input:
        "unique_complexes/sorted_unique_candidate_pairs.tsv.gz"
    output:
        "similarities/edit_similarities.tsv",
        "similarities/edit_similarities_overflow.tsv"
    threads:
        64
    script:
        "scripts/compute_edit_distances.py"


rule calculate_wl_values:
    conda:
        "envs/java.yaml"
    input:
        "similarities/edit_similarities.tsv",
        "unique_complexes/unique_complexes.tsv"
    output:
        "similarities/all_similarities.tsv"
    threads:
        64
    script:
        "scripts/compute_wl_values.py"


rule compare_distances:
    conda:
        "envs/plot_results.yaml"
    params:
        w = weights
    input:
        "similarities/all_similarities.tsv"
    output:
        "tables/max_correlations.tsv",
        expand("plots/{w:.2f}_wl_vs_edit.pdf", w=weights),
        "plots/correlations_cosine.pdf",
        "plots/correlations_pearson.pdf"
    script:
        "scripts/compare_distances.py"
