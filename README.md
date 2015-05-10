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

Additional dependencies
```bash
wget https://bootswatch.com/yeti/bootstrap.css -O bower_components/bootstrap/dist/css/bootstrap-yeti.css
```

### Binary Dependencies
Download [CLUTO](http://glaros.dtc.umn.edu/gkhome/cluto/cluto/download)

## Configurations
[Configurations](https://github.com/idf/DocumentMiner/blob/develop/km-common/src/main/java/km/common/Config.java)  
[Configuration XML](https://github.com/idf/DocumentMiner/blob/develop/km-common/src/main/resources/settings.xml)
* the logic of configuration is controlled by [Settings.java](https://github.com/idf/DocumentMiner/blob/develop/km-common/src/main/java/km/common/Settings.java)

## Generate Offline Data 
### Topic Modeling 
1. Manually add add mallet dependencies (km-mallet/lib/mallet_deps.jar) into the km-mallet module
1. Download [stopwords](http://www.lextek.com/manuals/onix/stopwords2.html) to /mallet/stoplist/en.txt
1. Run km.crawler.postprocess.ToCSV, this takes the posts.txt as input and output as a csv format.
1. Run km.mallet.preprocess.DataImportUnigram, this takes the csv file generated previously, and output as mallet specific format.
1. Run km.mallet.topic.TrainTopicUnigram, this takes the previous step generated file, output two files, keys and topics.

### Indexing
1. Run km.crawler.postprocess.SortPostPerThread, this will generate post_sorted.txt
1. Run km.lucene.indexing.PostIndexer, this will generate post index.

### Clustering
1. Run km.lucene.applets.collocations.Driver to get RAKE index based on post clustering. 
* It may takes some time.

### Collocation Analysis
1. Run km.lucene.applets.collocations.TermCollocationExtractor to see the collocation results in CLI.

### Web
1. Install Glassfish server [download Java EE](http://www.oracle.com/technetwork/java/javaee/downloads/java-ee-sdk-7-downloads-1956236.html)


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

