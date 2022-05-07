# NLPIR ICTCLAS analysis for Elasticsearch

An analysis plugin for Elasticsearch by using ICTCLAS/NLPIR

Analyzer: `ictclas_search`, `ictclas_index`

Supported version: 8.2.0 ~ 7.14.2

# Build

## 1. make config file `gradle.properties`

``` properties
    elasticsearchVersion = 7.15.2
```

## 2. build 

    ./gradlew build -x test


# Install 

    elasticsearch-plugin install file://analysis-ictclas.zip


# Config (MUST)

1. Go to the plugin folder of elasticsearch
2. Add NLPIR.user to Data folder
3. Config the config.yml with licenseCode if your license need a license code
4. If you don't have commercial license, use one month free license from https://github.com/NLPIR-team/NLPIR


