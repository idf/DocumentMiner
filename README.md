Knowledge Miner 
=============

Final Year Project

##Set Up
Branch `develop`:
```bash
git clone git@github.com:idf/TopicModeling.git
git checkout develop
git submodule foreach git pull origin master
```

##Indexes
###Submodules
[.gitmodules](https://github.com/zhangdanyangg/TopicModeling/blob/develop/.gitmodules)

###Utils
* [LuceneUtils](https://github.com/zhangdanyangg/TopicModeling/blob/develop/km-lucene/src/main/java/util/LuceneUtils.java)

##Features
1. Term collocation with terms
1. Term collocation with phrases
1. Phrase collocation with terms
1. Phrase collocation with phraes

###Co-occurrence process
* [README](https://github.com/zhangdanyangg/TopicModeling/blob/develop/km-lucene/src/main/java/km/lucene/applets/collocations)


##Configurations
[Configurations](https://github.com/idf/DocumentMiner/blob/develop/km-common/src/main/java/km/common/Config.java)

##Search Engine Interface in AngularJS
* Through Web Service: Javaxs
* Web dependencies: [bower](https://github.com/zhangdanyangg/KnowledgeMiner/blob/develop/km-web/src/main/webapp/bower.json)

In the directory of `bower.json`:
```bash
bower install
```

##Component Diagram
![](/img/DocumentMinerComponent.png)