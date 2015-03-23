Document Miner
=============

Final Year Project

##Set Up
Branch `develop`:
```bash
git clone git@github.com:idf/DocumentMiner.git
git checkout develop
git submodule foreach git pull origin master
```

##Indexes
###Submodules
[.gitmodules](https://github.com/idf/DocumentMiner/blob/develop/.gitmodules)  

###Utils
* [LuceneUtils](https://github.com/idf/DocumentMiner/blob/develop/km-lucene/src/main/java/util/LuceneUtils.java)

##Features
1. term co-occurrences for term query;
1. phrase co-occurrences for term query;
1. term co-occurrences for phrase query;
1. phrase co-occurrences for phrase query.  
And many more others.

###Co-occurrence process
* [README](https://github.com/idf/DocumentMiner/blob/develop/km-lucene/src/main/java/km/lucene/applets/collocations)


##Configurations
[Configurations](https://github.com/idf/DocumentMiner/blob/develop/km-common/src/main/java/km/common/Config.java)  
[Configuration XML](https://github.com/idf/DocumentMiner/blob/develop/km-common/src/main/resources/settings.xml)

##Search Engine Interface in AngularJS
* Through Web Service: JavaXS
* Web dependencies: [bower](https://github.com/idf/DocumentMiner/blob/develop/km-web/src/main/webapp/bower.json)

In the directory of `bower.json`:
```bash
bower install
```
<!-- 
##Component Diagram
![](/img/DocumentMinerComponent.png) 
-->
