# complex-similarity-evaluation
Evaluation Workflow for Protein Complex Similarity based on Weisfeiler-Lehman labeling


[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.1068297.svg)](https://doi.org/10.5281/zenodo.1068297)
[![Snakemake](https://img.shields.io/badge/snakemake-≥4.6.0-brightgreen.svg)](https://snakemake.bitbucket.io)

# Data analysis for paper: Protein Complex Similarity based on Weisfeiler-Lehman labeling

This Snakemake workflow automatically generates all results and plots from the paper.


## Usage

### Step 1: Setup system

## Requirements

Any 64-bit Linux installation with [GLIBC 2.5](http://unix.stackexchange.com/a/120381) or newer (i.e. any Linux distribution that is newer than CentOS 6).
Gurobi is required for solving the binary linear program. The libraries `libgurobi75.so`, `libGurobiJni75.so` and `gurobi.jar` must be installed system-wide or copied to the folder `lib`. A Gurobi licence key must be installed.

#### Variant a: Installing Miniconda on your system

If you are on a Linux system with [GLIBC 2.5](http://unix.stackexchange.com/a/120381) or newer (i.e. any Linux distribution that is newer than CentOS 6), you can simply install Miniconda3 with

    curl -o /tmp/miniconda.sh https://repo.continuum.io/miniconda/Miniconda3-latest-Linux-x86_64.sh && bash /tmp/miniconda.sh

Make sure to answer `yes` to the question whether your PATH variable shall be modified.
Afterwards, open a new shell/terminal.


#### Variant b: Use an existing Miniconda installation

If you want to use an existing Miniconda installation, please be aware that this is only possible if it uses Python 3 by default. You can check this via
  
    python --version

### Step 2: Update to conda >=4.4

If you have an older conda version, please update to conda >=4.4 at least via

    conda install conda=4.4

### Step 3: Setup Bioconda channel

Setup Bioconda with

    conda config --add channels defaults
    conda config --add channels conda-forge
    conda config --add channels bioconda

### Step 4: Install Snakemake

Install bioconda-utils and Snakemake >=4.6.0 with

    conda install snakemake

If you already have an older version of Snakemake, please make sure it is updated to >=4.6.0.

### Step 5: Download the workflow

First, create a working directory:

    mkdir complex-similarity-workflow
    cd complex-similarity-workflow

Then, download the workflow archive from https://doi.org/10.5281/TODO and unpack it with

    tar -xf workflow.tar.gz

### Step 6: Run the workflow

Execute the analysis workflow with Snakemake

    snakemake --use-conda --cores $CORES

With `$CORES` being set to a reasonable value for your system.
Note that it took a few hours with 64 cores.
Results can be found in the folder `plots/`.
