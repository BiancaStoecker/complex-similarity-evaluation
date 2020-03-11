# vi:syntax=python

import numpy as np

shell.executable("bash")

configfile: "config.yaml"

weights = np.arange(0.0,1.01, 0.01)

cpus = [ 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59, 61, 63, 65, 67, 69, 71 ]

rule all:
    input:
        #"tables/max_correlations.tsv",
        #"plots/2iter/correlations_cosine.pdf",
        #"plots/2iter/correlations_pearson.pdf",
        #"plots/3iter/correlations_cosine.pdf",
        #"plots/3iter/correlations_pearson.pdf",
        #"plots/benchmarks.{seed}.pdf".format(**config),
        "subsampling/subsample.{seed}_100.gml".format(**config),
        #expand("database_search_{cores}_cores/ges_{seed}_{n}_{thresh}_{run}/ges_{seed}_{n}_{thresh}_{run}.csv", cores=config["cores"], seed=config["seed"], n=config["subsample_n"], thresh=config["thresh"], run=config["runs"]),
        expand("database_search_{cores}_cores/wl_{minhash}_{seed}_{n}_{thresh}_{false_negative_rate}_{run}/wl_{minhash}_{seed}_{n}_{thresh}_{false_negative_rate}_{run}.csv", cores=config["cores"], seed=config["seed"],minhash=["minhash", "linear"], n=config["subsample_n"], thresh=config["thresh"], false_negative_rate=config["false_negative_rate"], run=config["runs"]),
        expand("database_search_{cores}_cores/times_{n}.csv", cores=config["cores"], n=config["subsample_n"])


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
        "java -jar tools/gml2tg/gml2tg-90c77e.jar --in {input} --out $out_dir --thresh {params.t} --wl0weight {params.w0} --wl1weight {params.w1} ;"
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
        expand("threshold_graph/threshold_graph_{w0}_{t}.gml", w0 = config["weight_threshold_graph"], t = config["min_thresh"])
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
        "envs/ged.yaml"
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
        "plots/2iter/correlations_cosine.pdf",
        "plots/2iter/correlations_pearson.pdf",
        "plots/3iter/correlations_cosine3.pdf",
        "plots/3iter/correlations_pearson3.pdf",
        #expand("plots/2iter/{w:.2f}_wl_vs_edit.pdf", w=weights)
        #expand("plots3iter/{w:.2f}_wl_vs_edit.pdf", w=weights)
    script:
        "scripts/compare_distances.py"


rule subsample_complexes:
    input:
        "unique_complexes/unique_complexes.tsv"
    output:
        expand("subsampling/subsample.{seed}_{n}.gml", seed=config["seed"], n=config["subsample_n"])
    params:
        seed=config["seed"],
        n=config["subsample_n"],
    conda:
        "envs/plot_results.yaml"
    script:
        "scripts/subsample-complexes.py"


rule benchmark_wl_2iter:
    input:
        "subsampling/subsample.{seed}_100.gml"
    output:
        fvtimes="benchmarks/wl-fv_2iter.{seed}.{k}.txt",
        simtimes="benchmarks/wl-sim_2iter.{seed}.{k}.txt"
    conda:
        "envs/java.yaml"
    resources:
        benchmark=1
    shell:
        "java -jar tools/wljaccard/wljaccardtimes.jar --simtimes {output.simtimes} --fvtimes {output.fvtimes} --gmlfile {input}"


rule benchmark_wl_3iter:
    input:
        "subsampling/subsample.{seed}_100.gml"
    output:
        fvtimes="benchmarks/wl-fv_3iter.{seed}.{k}.txt",
        simtimes="benchmarks/wl-sim_3iter.{seed}.{k}.txt"
    conda:
        "envs/java.yaml"
    resources:
        benchmark=1
    shell:
        "java -jar tools/wljaccard/wljaccardtimes.jar --simtimes {output.simtimes} --fvtimes {output.fvtimes} --gmlfile {input} --iter 3"


rule benchmark_ged:
    input:
        "subsampling/subsample.{seed}_100.gml"
    output:
        "benchmarks/ged.{seed}.{k}.txt"
    params:
        input=lambda w, input: os.path.abspath(input[0]),
        output=lambda w, output: os.path.abspath(output[0])
    log:
        "logs/benchmark-ged/{seed}.{k}.log"
    conda:
        "envs/ged.yaml"
    resources:
        benchmark=1
    shell:
        "tools/ged/ged {params.input} {params.output} --runtime > {log} 2>&1"


collect = lambda k: expand("benchmarks/{tool}.{{seed}}.{k}.txt", tool=["ged", "wl-fv_2iter", "wl-sim_2iter", "wl-fv_3iter", "wl-sim_3iter"], k=k)


rule collect_benchmarks:
    input:
        k0=collect(0),
        k1=collect(1),
        k2=collect(2)
    output:
        "plots/benchmarks.{seed}.pdf"
    conda:
        "envs/plot_results.yaml"
    script:
        "scripts/collect-benchmarks.py"


rule database_search_wl:
    input:
        "subsampling/subsample.{seed}_{n}.gml"
    output:
        "database_search_{cores}_cores/wl_{minhash}_{seed}_{n}_{thresh}_{false_negative_rate}_{run}/wl_{minhash}_{seed}_{n}_{thresh}_{false_negative_rate}_{run}.csv"
    params:
        minhash=lambda wildcards: "--useminhashing" if "minhash"==wildcards.minhash else "",
        cpu_list = lambda wildcards: str(cpus[(int(wildcards.run) * 12)])
        #cpu_list = lambda wildcards: str(cpus[(int(wildcards.run) * 12):(int(wildcards.run) * 12 + 12)]).replace(" ", "")[1:-1]
    conda:
        "envs/java.yaml"
    shell:
        "export out_dir=`dirname \"{output}\"` ;"
        "/usr/bin/time -v numactl --physcpubind={params.cpu_list} --membind=1 -- java -jar tools/rangequery/rangequery-29dfc07.jar --queries {input} --dataset simulated_complexes/true_constraints/ --resultfolder $out_dir --thresh {wildcards.thresh} --falsenegativerate {wildcards.false_negative_rate} {params.minhash} &> $out_dir/log.txt ;"
        "mv \"${{out_dir}}/001/perf001.csv\" {output} ;"
        "rm -r \"${{out_dir}}/001/\" ;"


rule database_search_ges:
    input:
        "subsampling/subsample.{seed}_{n}.gml"
    output:
        "database_search_{cores}_cores/ges_{seed}_{n}_{thresh}_{run}/ges_{seed}_{n}_{thresh}_{run}.csv"
    params:
        cpu_list = lambda wildcards: str(cpus[(int(wildcards.run) * 12)])
        #cpu_list = lambda wildcards: str(cpus[(int(wildcards.run) * 12):(int(wildcards.run) * 12 + 12)]).replace(" ", "")[1:-1]
    conda:
        "envs/java.yaml"
    shell:
        "export out_dir=`dirname \"{output}\"` ;"
        "/usr/bin/time -v numactl --physcpubind={params.cpu_list} --membind=1 -- tools/ged_rangequery/ged_rangequery ../../simulated_complexes/true_constraints.gml ../../{input} {wildcards.thresh} ../../{output} &> $out_dir/log.txt"


rule extract_times:
    input:
        expand("database_search_{cores}_cores/ges_{seed}_{n}_{thresh}_{run}/ges_{seed}_{n}_{thresh}_{run}.csv", cores=config["cores"], seed=config["seed"], n=config["subsample_n"], thresh=config["thresh"], run=config["runs"]),
        expand("database_search_{cores}_cores/wl_{minhash}_{seed}_{n}_{thresh}_{false_negative_rate}_{run}/wl_{minhash}_{seed}_{n}_{thresh}_{false_negative_rate}_{run}.csv", cores=config["cores"], seed=config["seed"],minhash=["minhash", "linear"], n=config["subsample_n"], thresh=config["thresh"], false_negative_rate=config["false_negative_rate"], run=config["runs"])
    output:
        "database_search_{cores}_cores/times_{n}.csv"
    shell:
        "scripts/generate_csv.sh {wildcards.n} database_search_{wildcards.cores}_cores"


rule compare_database_search:
    input:
        expand("database_search_{cores}_cores/times_{n}.csv", cores=config["cores"], n=config["subsample_n"])
    #output:
        #"plots/database_search_{cores}_cores_runtime.pdf"
    #conda:
        #"envs/plot_results.yaml"
    #script:
        #"scripts/compare_database_search_{cores}_cores.py"
