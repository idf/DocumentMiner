#Driver

##Configurations
* Whether to re-run the clustering (CLUTO) process: `final boolean RE_RUN_CLUSTER`
* Whether to re-run the RAKE indexing process: `final boolean RE_RUN_RAKE_INDEX`
* Top k of the co-occurrence terms where k is defined by: `final int TOP`


##File Naming
Suffix of a file:
```java
String.format("%s-%d", methodology, number_of_clusters);
```
