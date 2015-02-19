Knowledge Miner 
=============

Final Year Project

##Set Up
Branch `develop`:
```bash
git clone git@github.com:zhangdanyangg/TopicModeling.git
git checkout develop
git submodule foreach git pull origin master
```

##Indexes
###Submodules
[.gitmodules](https://github.com/zhangdanyangg/TopicModeling/blob/develop/.gitmodules)

###Utils
* [LuceneUtils](https://github.com/zhangdanyangg/TopicModeling/blob/develop/km-lucene/src/main/java/util/LuceneUtils.java)

###Co-occurrence process
* [README](https://github.com/zhangdanyangg/TopicModeling/blob/develop/km-lucene/src/main/java/km/lucene/applets/collocations)

###Search Engine Interface in AngularJS
* Through Web Service: Javaxs
* Web dependencies: [bower](https://github.com/zhangdanyangg/KnowledgeMiner/blob/develop/km-web/src/main/webapp/bower.json)

In the directory of `bower.json`:
```bash
bower install
```