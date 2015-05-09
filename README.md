Document Miner
=============

Final Year Project

## Set Up
### Source Code 
Branch `develop`:
Initial setup
```bash
git clone git@github.com:idf/DocumentMiner.git
cd ./DocumentMiner
git submodule init
git submodule update --recursive 
```

Update 
```bash
git submodule foreach git pull origin master
```

### Java Dependencies
Mananged by maven. <!-- TODO --> 

### Web Dependencies 
* To install [bower](cd ./km-web/src/main/webapp/)
Change into the  directory of `bower.json`, by `cd ./km-web/src/main/webapp/`
```bash
bower install
```


## Configurations
[Configurations](https://github.com/idf/DocumentMiner/blob/develop/km-common/src/main/java/km/common/Config.java)  
[Configuration XML](https://github.com/idf/DocumentMiner/blob/develop/km-common/src/main/resources/settings.xml)

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

