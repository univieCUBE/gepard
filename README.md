[![Anaconda-Server Badge](https://anaconda.org/bioconda/gepard/badges/platforms.svg)](https://anaconda.org/bioconda/gepard)
[![Anaconda-Server Badge](https://anaconda.org/bioconda/gepard/badges/license.svg)](https://anaconda.org/bioconda/gepard)
[![Anaconda-Server Badge](https://anaconda.org/bioconda/gepard/badges/downloads.svg)](https://anaconda.org/bioconda/gepard)
[![Anaconda-Server Badge](https://anaconda.org/bioconda/gepard/badges/installer/conda.svg)](https://conda.anaconda.org/bioconda)

Gepard
===========================
Genome Pair Rapid Dotter (gepard)


## Table of Contents

   * [Introduction](#introduction)
   * [Installation](#installation)
     * [Using Docker](#using-docker)
     * [Using Conda](#using-conda)  
   * [Cite](#cite)


## Introduction

Gepard provides a user-friendly, interactive application for the quick creation of dotplots. It utilizes suffix arrays to reduce the time complexity of dotplot calculation to Theta(m*log n). A client-server mode, which is a novel feature for dotplot creation software, allows the user to calculate dotplots and color them by functional annotation without any prior downloading of sequence or annotation data.

## Installation

### Using Docker

First you must have [Docker](https://docs.docker.com/get-docker/) installed and running.  
Secondly have look at the availabe Gepard containers at https://quay.io/repository/biocontainers/gepard?tab=tags.  
Then:
  ```
# get the chosen AGAT version
docker pull quay.io/biocontainers/gepard:2.1.0--hdfd78af_0
# use an AGAT's tool e.g. agat_convert_sp_gxf2gxf
docker run quay.io/biocontainers/gepard:2.1.0--hdfd78af_0 gepardcmd
  ```

### Using Conda

  ```
  # create gepard environment and install gepard
  conda create --name gepard -c bioconda gepard
  # activate gepard environment
  conda activate gepard
  # run gepard
  gepardcmd
  ```

## Cite
Jan Krumsiek, Roland Arnold, Thomas Rattei, Gepard: a rapid and sensitive tool for creating dotplots on genome scale, Bioinformatics, Volume 23, Issue 8, 15 April 2007, Pages 1026–1028, https://doi.org/10.1093/bioinformatics/btm039
