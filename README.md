Document Miner
=============

Final Year Project

## Set Up
### Source Code 
Initial setup
```bash
git clone git@github.com:idf/DocumentMiner.git
cd ./DocumentMiner
git submodule init
git submodule update --recursive 
```
Normally, the `develop` branch is used.  

Update 
```bash
git submodule foreach git pull origin master
```

### Java Dependencies
This is a multi-module project, mananged by maven. You should configure `commons-util`, `km_*`, `rake4j` as module. The dependencies should be automatically resolved by maven, as indicated in pom.xml. 

### Web Dependencies 
* To install [bower](cd ./km-web/src/main/webapp/)
Change into the  directory of `bower.json`, by `cd ./km-web/src/main/webapp/`
```bash
bower install
```

### Binary Dependencies
Install [CLUTO](http://glaros.dtc.umn.edu/gkhome/cluto/cluto/download)

## Configurations
[Configurations](https://github.com/idf/DocumentMiner/blob/develop/km-common/src/main/java/km/common/Config.java)  
[Configuration XML](https://github.com/idf/DocumentMiner/blob/develop/km-common/src/main/resources/settings.xml)

## Constructing Index
Please follow the steps below to run the topic modeling (need to fix dependencies):
1. Manually add add mallet dependencies (km-mallet/lib/mallet_deps.jar) into the km-mallet module
1. Download [stopwords](http://www.lextek.com/manuals/onix/stopwords2.html) to /mallet/stoplist/en.txt
1. Run package km.crawler.postprocess.ToCSV, this takes the posts.txt as input and output as a csv format.
1. Run package km.mallet.preprocess.DataImportUnigram, this takes the csv file generated previously, and output as mallet specific format.
1. Run km.mallet.topic.TrainTopicUnigram, this takes the previous step generated file, output two files, keys and topics.

Run
1. Run package km.crawler.postprocess.SortPostPerThread, this will generate post_sorted.txt


## Indexes
###Submodules
[.gitmodules](https://github.com/idf/DocumentMiner/blob/develop/.gitmodules)  

### Utils
* [LuceneUtils](https://github.com/idf/DocumentMiner/blob/develop/km-lucene/src/main/java/util/LuceneUtils.java)

## Features
1. term co-occurrences for term query;
1. phrase co-occurrences for term query;
1. term co-occurrences for phrase query;
1. phrase co-occurrences for phrase query.  
And many more others.

### Co-occurrence process
* [README](https://github.com/idf/DocumentMiner/blob/develop/km-lucene/src/main/java/km/lucene/applets/collocations)

## Search Engine Interface in AngularJS
* Through Web Service: JavaXS
* Web dependencies: [bower.json](https://github.com/idf/DocumentMiner/blob/develop/km-web/src/main/webapp/bower.json)

## Component Diagram
![](/img/DocumentMinerComponent.png) 

